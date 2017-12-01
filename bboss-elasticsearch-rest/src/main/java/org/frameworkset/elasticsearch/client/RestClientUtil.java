package org.frameworkset.elasticsearch.client;

import com.frameworkset.util.SimpleStringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.handler.*;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.json.JsonTypeReference;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.spi.remote.http.MapResponseHandler;
import org.frameworkset.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class RestClientUtil extends ClientUtil{
	private static Logger logger = LoggerFactory.getLogger(RestClientUtil.class);
	protected ElasticSearchRestClient client;
	protected StringBuilder bulkBuilder;
	protected IndexNameBuilder indexNameBuilder;

	public RestClientUtil(ElasticSearchClient client,IndexNameBuilder indexNameBuilder) {
		this.client = (ElasticSearchRestClient)client;
		this.indexNameBuilder = indexNameBuilder;
	}
	private void handleFields(Map<String,Object> subFileds,String fieldName,List<IndexField> fields){
		if(subFileds == null || subFileds.size() == 0)
			return ;
		Iterator<Map.Entry<String,Object>> iterator = subFileds.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String,Object> entry = iterator.next();
			IndexField indexField = buildIndexField(entry, fields,fieldName);
		}

	}

	private Boolean parseBoolean(Object norms){
		if(norms == null){
			return null;
		}
		if(norms instanceof Boolean){
			return (Boolean)norms;
		}
		else if(norms instanceof Map){
			return (Boolean) ((Map) norms).get("enabled");
		}
		return null;
	}
	private IndexField buildIndexField(Map.Entry<String,Object> field,List<IndexField> fields,String parentFieldName){
//		Map.Entry<String,Object> field = fileds.next();
		IndexField indexField = new IndexField();
		String fieldName = null;
		if(parentFieldName != null){
			fieldName = parentFieldName + "."+field.getKey();
		}
		else {
			fieldName = field.getKey();
		}
		indexField.setFieldName(fieldName);
		Map<String,Object> fieldInfo = (Map<String,Object>)field.getValue();
		indexField.setType((String)fieldInfo.get("type"));
		indexField.setIgnoreAbove(ClientUtil.intValue(fieldInfo.get("ignore_above"),null));
		indexField.setAnalyzer((String)fieldInfo.get("analyzer"));
		indexField.setNormalizer((String)fieldInfo.get("normalizer"));
		indexField.setBoost((Integer)fieldInfo.get("boost"));
		indexField.setCoerce(parseBoolean( fieldInfo.get("coerce")));
		indexField.setCopyTo((String)fieldInfo.get("copy_to"));
		indexField.setDocValues(parseBoolean(fieldInfo.get("doc_values")));//setCoerce();
		indexField.setDynamic(parseBoolean(fieldInfo.get("doc_values")));	//dynamic
		indexField.setEnabled(parseBoolean(fieldInfo.get("enabled")));			//enabled
		indexField.setFielddata(parseBoolean(fieldInfo.get("fielddata")));	//fielddata
		indexField.setFormat((String)fieldInfo.get("format"));		//	format
		indexField.setIgnoreMalformed(parseBoolean(fieldInfo.get("ignore_malformed")));//Coerce();	//		ignore_malformed
		indexField.setIncludeInAll(parseBoolean(fieldInfo.get("include_in_all")));	//include_in_all
		indexField.setIndexOptions((String)fieldInfo.get("index_options"));
		indexField.setIndex(parseBoolean(fieldInfo.get("index")));	//
		indexField.setFields((Map<String,Object>)fieldInfo.get("fields"));	//

		indexField.setNorms(parseBoolean(fieldInfo.get("norms")));//	norms
		indexField.setNullValue(fieldInfo.get("null_value"));	//
		indexField.setPositionIncrementGap((Integer)fieldInfo.get("position_increment_gap"));
		indexField.setProperties((Map<String,Object>)fieldInfo.get("properties"));	//
		indexField.setSearchAnalyzer((String)fieldInfo.get("search_analyzer"));	//search_analyzer
		indexField.setSimilarity((String)fieldInfo.get("similarity"));	//
		indexField.setStore(parseBoolean(fieldInfo.get("store")));	//store
		indexField.setTermVector((String)fieldInfo.get("term_vector"));	//
		fields.add(indexField);
		handleFields(indexField.getFields(), fieldName,fields);
		return indexField;
	}
	/**
	 * 获取索引表字段信息
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public List<IndexField> getIndexMappingFields(String index,final String indexType) throws ElasticSearchException{
		final List<IndexField> fields = new ArrayList<IndexField>();
		getIndexMapping(index,false,new ResponseHandler<Void>(){

			@Override
			public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();

				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					/**
					 * Map<indexName,Mapping<Type,Properties<fieldName<"fileds",Map<subFieldName,IndexField>>,Type>>>
					 */
					Map<String,Object> map = SimpleStringUtil.json2ObjectWithType(entity.getContent(),new JsonTypeReference<Map<String,Object>>(){});
					Iterator<Map.Entry<String,Object>> entries = map.entrySet().iterator();
					while(entries.hasNext()){
						Map.Entry<String,Object> entry = entries.next();//去最新的映射版本，区别于每天的索引表版本
						Map<String,Map<String,Object>> mapping = (Map<String, Map<String,Object>>) entry.getValue();
						Map<String,Map<String,Object>> typeProperties = (Map<String,Map<String,Object>>)mapping.get("mappings").get(indexType);
						Map<String,Object> 	properties = 	(Map<String,Object>)typeProperties.get("properties");
						Iterator<Map.Entry<String,Object>> fileds = properties.entrySet().iterator();
						while(fileds.hasNext()){
							Map.Entry<String,Object> field = fileds.next();
							IndexField indexField = buildIndexField(field,fields,null);
						}
						break;

					}
					return null;

				} else {
					HttpEntity entity = response.getEntity();
					if (entity != null )
						throw new ElasticSearchException(new StringBuilder().append("Unexpected response : " ).append( EntityUtils.toString(entity)).toString());
					else
						throw new ElasticSearchException("Unexpected response status: " + status);
				}
			}
		});
		return fields;
	}

	
	 
	public String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans) throws ElasticSearchException{
		return null;
	}
	public   String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		return null;
	}
	@Override
	public String executeRequest(String path, String templateName,Map params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	/**************************************创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocument(String indexName, String indexType, Object bean) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean);
	}

	public String addDateDocument(String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean,refreshOption);
	}
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocuments(String indexName, String indexType, List<?> beans) throws ElasticSearchException{
		return addDocuments(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans);
	}

	public String addDateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException{
		return addDocuments(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans,refreshOption);
	}

	/**************************************创建或者修改文档结束**************************************************************/

	/***************************添加或者修改文档开始************************************/
	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
	 * @param beans
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocuments(String indexName, String indexType, List<?> beans) throws ElasticSearchException{
		return addDocuments(indexName, indexType,  beans,null);
	}
	public String addDocuments(String indexName, String indexType,  List<?> beans,String refreshOption) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Object bean:beans) {
			try {
				evalBuilk(writer,indexName,indexType,bean,"index");
			} catch (IOException e) {
				throw new ElasticSearchException(e);
			}
		}
		writer.flush();
		if(refreshOption == null)
			return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		else
			return this.client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
	}
	protected void buildMeta(StringBuilder builder ,String indexType,String indexName, Object params,String action){
		Object id = this.getId(params);
		if(id != null)
			builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : \"").append(id).append("\" } }\n");
		else
			builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\" } }\n");
	}
	protected void buildMeta(Writer writer ,String indexType,String indexName, Object params,String action) throws IOException {
		Object id = this.getId(params);
		if(id != null) {
			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			writer.write("\", \"_id\" : \"");
			writer.write(String.valueOf(id));
			writer.write("\" } }\n");
		}
		else {

			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			writer.write("\" } }\n");
		}
	}
	private void evalBuilk( Writer writer,String indexName, String indexType, Object param, String action) throws IOException {

		if (param != null) {
			buildMeta(  writer ,  indexType,  indexName,   param,action);
			if(!action.equals("update")) {
				SerialUtil.object2json(param,writer);
				writer.write("\n");
			}
			else
			{
				writer.write("{\"doc\":");
				SerialUtil.object2json(param,writer);
				writer.write("}\n");
			}
		}

	}

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocument(String indexName, String indexType, Object bean) throws ElasticSearchException{
		return this.addDocument(indexName,indexType,bean,null);
	}
	protected Object getId(Object bean){
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		ClassUtil.PropertieDescription pkProperty = beanInfo.getPkProperty();
		if(pkProperty == null)
			return null;
		return beanInfo.getPropertyValue(bean,pkProperty.getName());
	}
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param refreshOption
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocument(String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		Object id = this.getId(bean);
		builder.append(indexName).append("/").append(indexType);
		if(id != null){
			builder.append("/").append(id);
		}
		if(refreshOption != null ){
			builder.append("?").append(refreshOption);
		}
		String path = builder.toString();
		builder = null;
		path = this.client.executeHttp(path, SerialUtil.object2json(bean),ClientUtil.HTTP_POST);
		return path;
	}


	public String updateDocuments(String indexName, String indexType, List<?> beans) throws ElasticSearchException{
		return updateDocuments(indexName, indexType, beans,null);
	}
	public String updateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException{

		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Object bean:beans) {
			try {
				evalBuilk(writer,indexName,indexType,bean,"update");
			} catch (IOException e) {
				throw new ElasticSearchException(e);
			}
		}
		writer.flush();
		if(refreshOption != null) {
			return this.client.executeHttp("_bulk?" + refreshOption, builder.toString(), ClientUtil.HTTP_POST);
		}
		else {
			return this.client.executeHttp("_bulk", builder.toString(), ClientUtil.HTTP_POST);
		}
	}

	/***************************添加或者修改文档结束************************************/
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException{
		return null;
	}
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @param refreshOption
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String addDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException{
		return null;
	}

	public String addDateDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException{
		return null;
	}

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException{
		return null;
	}
	
	@Override 
	public String executeRequest(String path, String templateName,Object params) throws ElasticSearchException {
		// TODO Auto-generated method stub
				return null;
	}
	 /**
	  * 
	  * @param path
	  * @return
	  */
	 public Object executeRequest(String path) throws ElasticSearchException 
	 {
		 return executeRequest( path,null);
	 }
 

	@Override
	public String deleteDocuments(String indexName, String indexType, String... ids) throws ElasticSearchException {
		StringBuilder builder = new StringBuilder();
		for(String id:ids) {
			builder.append("{ \"delete\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : \"").append(id).append("\" } }\n");
		}
		return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		
	}
	
	public String deleteDocumentsWithrefreshOption(String indexName, String indexType, String refreshOption,String... ids) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		for(String id:ids) {
			builder.append("{ \"delete\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : \"").append(id).append("\" } }\n");
		}
		return this.client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
	}
	
	
	/**
	 * 删除模板
	 * @param template
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteTempate(String template) throws ElasticSearchException {
		return client.executeHttp("/_template/"+template,ClientUtil.HTTP_DELETE);
	}
	
	/**
	 * 查询模板
	 * @param template
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String getTempate(String template) throws ElasticSearchException {
		return client.executeHttp("/_template/"+template,ClientUtil.HTTP_GET);
	}
	
	/**
	 * 查询所有模板
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String getTempate() throws ElasticSearchException {
		return client.executeHttp("/_template",ClientUtil.HTTP_GET);
	}
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException
	{
		return null;
	}
	public String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		return null;
	}

	@Override
	/**
	 * only use by config rest clientutil
	 */
	public String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException {
//		StringBuilder builder = new StringBuilder();
//		for(Object id:beans) {
//			builder.append("{ \"index\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : \"").append(id).append("\" } }\n");
//		}
//		return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		return null;

	}
	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException
	{
		return null;
	}

	/**
	 * 获取文档，通过options设置获取文档的参数
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public String getDocument(String indexName, String indexType,String documentId,Map<String,Object> options) throws ElasticSearchException{

		return this.client.executeHttp(buildGetDocumentRequest(  indexName,   indexType,  documentId,  options),ClientUtil.HTTP_GET);
	}

	private String buildGetDocumentRequest(String indexName, String indexType,String documentId,Map<String,Object> options){
		StringBuilder builder = new StringBuilder();
		builder.append("/").append(indexName).append("/").append(indexType).append("/").append(documentId);
		if(options != null){
			builder.append("?");
			Iterator<Map.Entry<String, Object>> iterable = options.entrySet().iterator();
			boolean first = true;
			while(iterable.hasNext()){
				Map.Entry<String, Object> entry = iterable.next();
				if(first) {
					builder.append(entry.getKey()).append("=").append(entry.getValue());
					first = false;
				}
				else
				{
					builder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
				}
			}
		}
		return builder.toString();
	}

	/**
	 * 获取json格式的文档
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public String getDocument(String indexName, String indexType,String documentId) throws ElasticSearchException{
		return getDocument(indexName, indexType,documentId,(Map<String,Object>)null);
	}

	/**
	 * 获取文档对象
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T getDocument(String indexName, String indexType,String documentId,Class<T> beanType) throws ElasticSearchException{
		return getDocument(  indexName,   indexType,  documentId,(Map<String,Object>)null,beanType);
	}

	/**
	 * 获取文档，通过options设置获取文档的参数
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T getDocument(String indexName, String indexType,String documentId,Map<String,Object> options,Class<T> beanType) throws ElasticSearchException{
		SearchHit searchResult = this.client.executeRequest(buildGetDocumentRequest(  indexName,   indexType,  documentId,  options),null,   new GetDocumentResponseHandler( beanType),ClientUtil.HTTP_GET);

		return buildObject(searchResult, beanType);

	}

	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public   MapSearchHit getDocumentHit(String indexName, String indexType,String documentId,Map<String,Object> options) throws ElasticSearchException{
		MapSearchHit searchResult = this.client.executeRequest(buildGetDocumentRequest(  indexName,   indexType,  documentId,  options),null,   new GetDocumentHitResponseHandler( ),ClientUtil.HTTP_GET);
		return searchResult;
	}

	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public   MapSearchHit getDocumentHit(String indexName, String indexType,String documentId) throws ElasticSearchException{
		return getDocumentHit(indexName, indexType,documentId,(Map<String,Object> )null);
	}




	@Override
	public String deleteDocument(String indexName, String indexType, String id) throws ElasticSearchException {

		return this.client.executeHttp(new StringBuilder().append(indexName).append("/").append(indexType).append("/").append(id).toString(),ClientUtil.HTTP_DELETE);
	}
	public   String deleteDocument(String indexName, String indexType, String id,String refreshOption) throws ElasticSearchException{
		return this.client.executeHttp(new StringBuilder().append(indexName).append("/").append(indexType).append("/")
				.append(id).append("?").append(refreshOption).toString(),ClientUtil.HTTP_DELETE);
	}
	@Override
	public String executeRequest(String path, String entity) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return this.client.executeRequest(path,entity);
	}
	
	@Override
	public <T> T executeRequest(String path, String entity,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return this.client.executeRequest(path,entity,  responseHandler);
	}
	
	
	@Override
	public String executeHttp(String path, String action) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return this.client.executeHttp(path,action);
	}
	@Override
	public String executeHttp(String path, String entity,String action) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return this.client.executeHttp(path,entity,action);
	}

	@Override
	public <T> T executeHttp(String path, String entity, String action, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return null;
	}

	@Override
	public String executeHttp(String path, String entity, Map params, String action) throws ElasticSearchException {
		return null;
	}

	@Override
	public <T> T executeHttp(String path, String entity, String action, Object bean, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return null;
	}

	@Override
	public String executeHttp(String path, String entity, Object bean, String action) throws ElasticSearchException {
		return null;
	}

	@Override
	public <T> T executeHttp(String path, String action,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return this.client.executeHttp(path,action,responseHandler);
	}
	@Override
	public <T> T  executeHttp(String path, String entity,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return this.client.executeHttp(path,entity,action,responseHandler);
	}
	
	public String getIndexMapping(String index) throws ElasticSearchException{
		return this.getIndexMapping(index,true);

	}
	public <T> T getIndexMapping(String index,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		return this.getIndexMapping(index,true,responseHandler);

	}

	public String getIndexMapping(String index,boolean pretty) throws ElasticSearchException{
		if(pretty)
			return this.client.executeHttp(index+"/_mapping?pretty",ClientUtil.HTTP_GET);
		else
			return this.client.executeHttp(index+"/_mapping",ClientUtil.HTTP_GET);
	}

	public <T> T getIndexMapping(String index,boolean pretty,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		if(pretty)
			return this.client.executeRequest(index+"/_mapping?pretty",null,responseHandler,ClientUtil.HTTP_GET);
		else
			return this.client.executeRequest(index+"/_mapping",null,responseHandler,ClientUtil.HTTP_GET);
	}
	@Override
	public String delete(String path, String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T executeRequest(String path, String templateName,Map params,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		return null;
	}
	
	 
	public <T> T  executeRequest(String path, String templateName,Object params,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		return null;
	}
	@Override
	public MapRestResponse search(String path, String templateName, Map params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public MapRestResponse search(String path, String templateName, Object params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public MapRestResponse search(String path, String entity) throws ElasticSearchException {
		MapRestResponse searchResult = this.client.executeRequest(path,entity,   new ElasticSearchMapResponseHandler(  ));
//		if(searchResult instanceof ErrorResponse){
//			throw new ElasticSearchException(SimpleStringUtil.object2json(searchResult));
//		}
		return searchResult;
	}

	@Override
	public RestResponse search(String path, String templateName, Map params,Class<?> type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public RestResponse search(String path, String templateName, Object params,Class<?> type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public RestResponse search(String path, String entity,Class<?> type) throws ElasticSearchException {
		RestResponse searchResult = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
//		if(searchResult instanceof ErrorResponse){
//			throw new ElasticSearchException(SimpleStringUtil.object2json(searchResult));
//		}
		return searchResult;
	}

	protected <T> ESDatas<T> buildESDatas(RestResponse result,Class<T> type){
//		if(result instanceof ErrorResponse){
//			throw new ElasticSearchException(SimpleStringUtil.object2json(result));
//		}
		ESDatas<T> datas = new ESDatas<T>();
		RestResponse restResponse = (RestResponse)result;
		datas.setTotalSize(restResponse.getSearchHits().getTotal());
		List<SearchHit> searchHits = restResponse.getSearchHits().getHits();
		if(SearchHit.class.isAssignableFrom(type)){

			datas.setAggregations(restResponse.getAggregations());
			if(searchHits != null && searchHits.size() > 0) {
				Object obj = searchHits.get(0).getSource();
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(obj.getClass());
				boolean isESId = false;
				if (!isESBaseData) {
					isESId = ESId.class.isAssignableFrom(obj.getClass());
				}

				for (int i = 0; i < searchHits.size(); i++) {
					SearchHit hit = searchHits.get(i);
					if(isESBaseData || isESId) {
						//处理源对象
						Object data = hit.getSource();
						if (data != null) {
							injectBaseData(data, hit, isESBaseData, isESId);
						}
					}
					//处理InnerHit对象
					Map<String, Map<String, InnerSearchHits>> innerHits = hit.getInnerHits();
					if (innerHits != null && innerHits.size() > 0) {
						injectInnerHitBaseData(innerHits);
					}
				}
			}
			datas.setDatas((List<T>) searchHits);
		}
		else{
			List<T> hits = new ArrayList<T>(searchHits.size());
			boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
			boolean isESId = false;
			if(!isESBaseData){
				isESId = ESId.class.isAssignableFrom(type);
			}
			T data = null;
			for(SearchHit hit:searchHits){
				data = (T) hit.getSource();
				hits.add(data);
				if(isESBaseData) {
					buildESBaseData(  hit,  (ESBaseData)data);
				}
				else if(isESId)
				{
					buildESId(hit,(ESId )data);
				}
			}
			datas.setAggregations(restResponse.getAggregations());
			datas.setDatas(hits);
		}

		return datas;
	}
	public <T> ESDatas<T> searchList(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	public <T> ESDatas<T> searchList(String path, String templateName, Object params,Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	public <T> ESDatas<T> searchList(String path, String entity, Class<T> type) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
		return buildESDatas(result,type);
	}

	public <T> T searchObject(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
	 	return null;
	}

	public <T> T searchObject(String path, String templateName, Object params,Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	protected void buildESBaseData(BaseSearchHit hit,ESBaseData esBaseData){
		esBaseData.setFields(hit.getFields());
		esBaseData.setHighlight( hit.getHighlight());
		esBaseData.setId(hit.getId());
		esBaseData.setScore(hit.getScore());
		esBaseData.setSort(hit.getSort());
		esBaseData.setType(hit.getType());
		esBaseData.setVersion(hit.getVersion());
		esBaseData.setIndex(hit.getIndex());
	}
	protected void buildESId(BaseSearchHit hit,ESId esBaseData){

		esBaseData.setId(hit.getId());

	}
	protected void injectBaseData(Object data,BaseSearchHit hit,boolean isESBaseData,boolean isESId){

		if (isESBaseData) {
			buildESBaseData(hit, (ESBaseData) data);
		} else if (isESId) {
			buildESId(hit, (ESId) data);
		}
	}

	protected void injectInnerHitBaseData(Map<String, Map<String,InnerSearchHits>> innerHits){
		Iterator<Map.Entry<String, Map<String, InnerSearchHits>>> iterator = innerHits.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, Map<String, InnerSearchHits>> entry = iterator.next();
			Map<String, InnerSearchHits> value = entry.getValue();
			InnerSearchHits hitsEntryValue = value.get("hits");
			if(hitsEntryValue != null){
				List<InnerSearchHit> innerSearchHits = hitsEntryValue.getHits();
				if(innerSearchHits != null && innerSearchHits.size() > 0){
					Object source = innerSearchHits.get(0).getSource();
					boolean isESBaseData = ESBaseData.class.isAssignableFrom(source.getClass());
					boolean isESId = false;
					if(!isESBaseData){
						isESId = ESId.class.isAssignableFrom(source.getClass());
					}
					if(isESBaseData || isESId) {
						for (int i = 0; i < innerSearchHits.size(); i++) {
							InnerSearchHit innerSearchHit = innerSearchHits.get(i);
							source = innerSearchHit.getSource();
							if (source != null) {
								injectBaseData(source, innerSearchHit, isESBaseData, isESId);
							}
						}
					}
				}
			}

		}

	}
	protected <T> T buildObject(RestResponse result, Class<T> type){
		if(result == null){
			return null;
		}
		RestResponse restResponse = (RestResponse) result;
		List<SearchHit> searchHits = restResponse.getSearchHits().getHits();
		if (searchHits != null && searchHits.size() > 0) {
			SearchHit hit = searchHits.get(0);
			if(SearchHit.class.isAssignableFrom(type)){
				//处理源对象
				Object data =  hit.getSource();
				if(data != null) {

					boolean isESBaseData = ESBaseData.class.isAssignableFrom(data.getClass());
					boolean isESId = false;
					if(!isESBaseData){
						isESId = ESId.class.isAssignableFrom(data.getClass());
					}
					injectBaseData(data,hit,isESBaseData,isESId);
				}
				//处理InnerHit对象
				Map<String, Map<String,InnerSearchHits>> innerHits = hit.getInnerHits();
				if(innerHits != null && innerHits.size() > 0){
					injectInnerHitBaseData(innerHits);
				}
				return (T)hit;
			}
			else{
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
				boolean isESId = false;
				if(!isESBaseData){
					isESId = ESId.class.isAssignableFrom(type);
				}
				T data = (T) hit.getSource();
				if (isESBaseData) {
					buildESBaseData(hit, (ESBaseData) data);
				}
				else if(isESId)
				{
					buildESId(hit,(ESId )data);
				}
				return data;
			}

		}
		return null;


	}


	protected <T> T buildObject(SearchHit result, Class<T> type){
		if(result == null){
			return null;
		}
		if(SearchHit.class.isAssignableFrom(type)){
			//处理源对象
			Object data =  result.getSource();
			if(data != null) {
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(data.getClass());
				boolean isESId = false;
				if(!isESBaseData){
					isESId = ESId.class.isAssignableFrom(data.getClass());
				}
				injectBaseData(data,result,isESBaseData,isESId);
			}
			//处理InnerHit对象
			Map<String, Map<String,InnerSearchHits>> innerHits = result.getInnerHits();
			if(innerHits != null && innerHits.size() > 0){
				injectInnerHitBaseData(innerHits);
			}
			return (T)result;
		}
		boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
		boolean isESId = false;
		if(!isESBaseData){
			isESId = ESId.class.isAssignableFrom(type);
		}
		SearchHit hit = result;
		if(hit.isFound()) {
			T data = (T) hit.getSource();
			if (isESBaseData) {
				buildESBaseData(hit, (ESBaseData) data);
			}
			else if(isESId)
			{
				buildESId(hit,(ESId )data);
			}
			return data;
		}
		else {
			return null;
		}


	}
	public <T> T searchObject(String path, String entity, Class<T> type) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
		return buildObject(result, type);
	}

	private void buildLongAggHit(LongAggHit longRangeHit,Map<String,Object> bucket,String stats){
		longRangeHit.setKey((String)bucket.get("key"));
		longRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		longRangeHit.setMax(longValue(stats_.get("max"),0l));
		longRangeHit.setMin(longValue(stats_.get("min"),0l));
		longRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		longRangeHit.setSum(longValue(stats_.get("sum"),0l));
	}
	private void buildFloatAggHit(FloatAggHit floatRangeHit,Map<String,Object> bucket,String stats){
		floatRangeHit.setKey((String)bucket.get("key"));
		floatRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		floatRangeHit.setMax(floatValue(stats_.get("max"),0f));
		floatRangeHit.setMin(floatValue(stats_.get("min"),0f));
		floatRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		floatRangeHit.setSum(floatValue(stats_.get("sum"),0f));
	}

	private void buildDoubleAggHit(DoubleAggHit doubleAggHit,Map<String,Object> bucket,String stats){
		doubleAggHit.setKey((String)bucket.get("key"));
		doubleAggHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		doubleAggHit.setMax(doubleValue(stats_.get("max"),0d));
		doubleAggHit.setMin(doubleValue(stats_.get("min"),0d));
		doubleAggHit.setAvg(doubleValue(stats_.get("avg"),0d));
		doubleAggHit.setSum(doubleValue(stats_.get("sum"),0d));
	}

	private void buildLongAggRangeHit(LongAggRangeHit longRangeHit,Map<String,Object> bucket,String stats,String key){
		longRangeHit.setKey(key);
		longRangeHit.setFrom(longValue(bucket.get("from"),null));
		longRangeHit.setTo(longValue(bucket.get("to"),null));
		longRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		longRangeHit.setMax(longValue(stats_.get("max"),0l));
		longRangeHit.setMin(longValue(stats_.get("min"),0l));
		longRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		longRangeHit.setSum(longValue(stats_.get("sum"),0l));
	}
	private void buildFloatAggRangeHit(FloatAggRangeHit floatRangeHit,Map<String,Object> bucket,String stats,String key){
		floatRangeHit.setKey(key);
		floatRangeHit.setFrom(floatValue(bucket.get("from"),null));
		floatRangeHit.setTo(floatValue(bucket.get("to"),null));
		floatRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		floatRangeHit.setMax(floatValue(stats_.get("max"),0f));
		floatRangeHit.setMin(floatValue(stats_.get("min"),0f));
		floatRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		floatRangeHit.setSum(floatValue(stats_.get("sum"),0f));
	}
	private void buildDoubleAggRangeHit(DoubleAggRangeHit doubleRangeHit,Map<String,Object> bucket,String stats,String key){
		doubleRangeHit.setKey(key);
		doubleRangeHit.setFrom(doubleValue(bucket.get("from"),null));
		doubleRangeHit.setTo(doubleValue(bucket.get("to"),null));
		doubleRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		doubleRangeHit.setMax(doubleValue(stats_.get("max"),0d));
		doubleRangeHit.setMin(doubleValue(stats_.get("min"),0d));
		doubleRangeHit.setAvg(doubleValue(stats_.get("avg"),0d));
		doubleRangeHit.setSum(doubleValue(stats_.get("sum"),0d));
	}

	protected  <T extends AggHit> ESAggDatas<T> buildESAggDatas(RestResponse searchResult,Class<T> type,String aggs,String stats,ESAggBucketHandle<T> aggBucketHandle){
		
		 
		Map<String,Map<String,Object>> aggregations = searchResult.getAggregations();
		if(aggregations != null){
			Map<String,Object> traces = aggregations.get(aggs);
			Object _buckets = traces.get("buckets");
			ESAggDatas<T> ret = new ESAggDatas<T>();
			ret.setTotalSize(searchResult.getSearchHits().getTotal());

			if(_buckets instanceof List) {
				List<Map<String, Object>> buckets = (List<Map<String, Object>>) _buckets;
				List<T> datas = new ArrayList<>(buckets.size());
				ret.setAggDatas(datas);
				for (Map<String, Object> bucket : buckets) {
					try {
						T obj = type.newInstance();
						if(obj instanceof LongAggRangeHit){
							buildLongAggRangeHit((LongAggRangeHit) obj, bucket,  stats,null);
						}else if(obj instanceof FloatAggRangeHit){
							buildFloatAggRangeHit((FloatAggRangeHit) obj, bucket,  stats,null);
						}else if(obj instanceof DoubleAggRangeHit){
							buildDoubleAggRangeHit((DoubleAggRangeHit) obj, bucket,  stats,null);
						} else if (obj instanceof LongAggHit) {
							buildLongAggHit((LongAggHit) obj, bucket,  stats);
						} else if(obj instanceof FloatAggHit){
							buildFloatAggHit((FloatAggHit) obj, bucket,  stats);
						}else if(obj instanceof DoubleAggHit){
							buildDoubleAggHit((DoubleAggHit) obj, bucket,  stats);
						}
						if(aggBucketHandle != null)
						{
							aggBucketHandle.bucketHandle(searchResult,bucket,obj,null);
						}

						datas.add(obj);
					} catch (InstantiationException e) {
						throw new ElasticSearchException(e);
					} catch (IllegalAccessException e) {
						throw new ElasticSearchException(e);
					}
				}


			}
			else
			{
				Map<String,Map<String, Object>> buckets = (Map<String,Map<String, Object>>) _buckets;
				List<T> datas = new ArrayList<>(buckets.size());
				ret.setAggDatas(datas);
				Iterator<Map.Entry<String, Map<String, Object>>> iterable = buckets.entrySet().iterator();
				Map<String, Object> bucket = null;
				Map.Entry<String, Map<String, Object>> entry = null;
				String key = null;
				T obj = null;
				while(iterable.hasNext()){
					entry = iterable.next();
					key = entry.getKey();
					bucket = entry.getValue();
					try {
						obj = type.newInstance();

						if (obj instanceof LongAggRangeHit) {
							buildLongAggRangeHit((LongAggRangeHit) obj, bucket,  stats,key);
						} else if (obj instanceof DoubleAggRangeHit)
						{
							buildDoubleAggRangeHit((DoubleAggRangeHit) obj, bucket,  stats,key);
						}else if (obj instanceof FloatAggRangeHit)
						{
							buildFloatAggRangeHit((FloatAggRangeHit) obj, bucket,  stats,key);
						}
						if(aggBucketHandle != null)
						{
							aggBucketHandle.bucketHandle(searchResult,bucket,obj,key);
						}
						datas.add(obj);
					}catch (InstantiationException e) {
						throw new ElasticSearchException(e);
					} catch (IllegalAccessException e) {
						throw new ElasticSearchException(e);
					}
				}
			}
			return ret;
		}

		 
		return null;
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Map params,Class<T> type,String aggs,String stats) throws ElasticSearchException{

		return null;
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Object params,Class<T> type,String aggs,String stats) throws ElasticSearchException{
		return null;
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Map params,Class<T> type,String aggs,String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException{

		return null;
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Object params,Class<T> type,String aggs,String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException{
		return null;
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Class<T> type,String aggs,String stats) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( ));
		return buildESAggDatas(result,type,aggs,stats,(ESAggBucketHandle<T>)null);
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Class<T> type,String aggs,String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( ));
		return buildESAggDatas(result,type,aggs,stats,aggBucketHandle);
	}

	@Override
	public String createTempate(String template, String entity) throws ElasticSearchException {
		return this.client.executeHttp("_template/"+template,entity,ClientUtil.HTTP_PUT);
	}

	@Override
	public String createTempate(String template, String templateName,Object params) throws ElasticSearchException {
		return null;
	}

	@Override
	public String createTempate(String template, String templateName,Map params) throws ElasticSearchException {
		return null;
	}


	@Override
	public RestResponse search(String path, String templateName, Map params,ESTypeReferences type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public RestResponse search(String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public RestResponse search(String path, String entity,ESTypeReferences type) throws ElasticSearchException {
		return this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
	}
	
	
	 public Map<String,Object>  searchMap(String path, String templateName,Map params) throws ElasticSearchException{
		 return null;
	 }
		
	 
		public Map<String,Object> searchMap(String path, String templateName,Object params) throws ElasticSearchException{
			return null;
		}
		/**
		  * 
		  * @param path
		  * @param entity
		  * @return
		  */
		 @SuppressWarnings("unchecked")
		public Map<String,Object> searchMap(String path, String entity) throws ElasticSearchException {
			 // TODO Auto-generated method stub
			 return this.client.executeRequest(path,entity,  new MapResponseHandler());
		 }
	
	 public String dropIndice(String index)  throws ElasticSearchException {
		 return this.client.executeHttp(index+"?pretty",ClientUtil.HTTP_DELETE);
		 
	 }
	 
	 
	 /**
	  * 更新索引定义
	  * @param indexMapping
	  * @return
	  * @throws ElasticSearchException
	  */
	 public String updateIndiceMapping(String action,String indexMapping)  throws ElasticSearchException {
		 return this.client.executeHttp(action,indexMapping,ClientUtil.HTTP_POST);
	 }
	 
	 /**
	  * 创建索引定义
	  * curl -XPUT 'localhost:9200/test?pretty' -H 'Content-Type: application/json' -d'
		{
		    "settings" : {
		        "number_of_shards" : 1
		    },
		    "mappings" : {
		        "type1" : {
		            "properties" : {
		                "field1" : { "type" : "text" }
		            }
		        }
		    }
		}
	  * @param indexMapping
	  * @return
	  * @throws ElasticSearchException
	  */
	 public String createIndiceMapping(String indexName,String indexMapping)  throws ElasticSearchException {
		 return this.client.executeHttp(indexName,indexMapping,ClientUtil.HTTP_PUT);
	 }
	@Override
	public String updateIndiceMapping(String action, String templateName, Object parameter)
			throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String createIndiceMapping(String indexName, String templateName, Object parameter)
			throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String updateIndiceMapping(String action, String templateName, Map parameter) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String createIndiceMapping(String indexName, String templateName, Map parameter)
			throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	  * 获取索引定义
	  * @param index
	  * @return
	  * @throws ElasticSearchException
	  */
	 public String getIndice(String index)  throws ElasticSearchException {
//		 String response = (String)client.executeHttp(index+"/_mapping?pretty",ClientUtil.HTTP_GET);
//		 return response;
		 return this.getIndexMapping(index,true);
	 }
	 public List<ESIndice> getIndexes() throws ElasticSearchException{
		 String data = this.client.executeHttp("_cat/indices?v",HTTP_GET);
         logger.debug(data);

         if(SimpleStringUtil.isNotEmpty(data)){
			try {
				List<ESIndice> indices = extractIndice(data);
				 return indices;
			} catch (IOException e) {
				throw new ElasticSearchException(e);
			}
        	
         }
         return null;
	 }
	 
	 public List<ESIndice> extractIndice(String data) throws IOException {
	        Reader reader = null;
	        BufferedReader br = null;
	        try{
	        	reader = new StringReader(data);
	            br = new BufferedReader(reader);
		        List<ESIndice> indices = new ArrayList<ESIndice>();
		        int i = 0;
		        SimpleDateFormat format = new SimpleDateFormat(client.getDateFormat());
		        while(true){
		            String line = br.readLine();
		            if(line == null)
		                break;
		            if(i == 0){
		                i ++;
		                continue;
		            }
		            
		            ESIndice esIndice = buildESIndice(  line,  format);
		            //如果索引已经过时，则清除过时索引数据
//		            if(esIndice.getGenDate() != null && esIndice.getGenDate().before(deadLine)){
		                indices.add(esIndice);
//		            }
		
		        }
		        return indices;
	        }
	        finally
	        {
	        	 if(reader != null)
					try {
						reader.close();
					} catch (IOException e) {
						 
					}
	        	 if(br != null)
	        		 try {
	 					br.close();
	 				} catch (IOException e) {
	 					 
	 				}
	        }


	    }
	    
	    private ESIndice buildESIndice(String line, SimpleDateFormat format)
	    {
	    	StringBuilder token = new StringBuilder();
	        ESIndice esIndice = new ESIndice();

	        int k = 0;
	        for(int j = 0; j < line.length(); j ++){
	            char c = line.charAt(j);
	            if(c != ' '){
	                token.append(c);
	            }
	            else {
	                if(token.length() == 0)
	                    continue;
	                switch (k ){
	                    case 0:
	                        esIndice.setHealth(token.toString());
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 1:
	                        esIndice.setStatus(token.toString());
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 2:
	                        esIndice.setIndex(token.toString());
	                        putGendate(  esIndice,  format);
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 3:
	                        esIndice.setUuid(token.toString());
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 4:
	                        esIndice.setPri(Integer.parseInt(token.toString()));
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 5:
	                        esIndice.setRep(Integer.parseInt(token.toString()));
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 6:
	                        esIndice.setDocsCcount(Long.parseLong(token.toString()));
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 7:
	                        esIndice.setDocsDeleted(Long.parseLong(token.toString()));
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 8:
	                        esIndice.setStoreSize(token.toString());
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    case 9:
	                        esIndice.setPriStoreSize(token.toString());
	                        token.setLength(0);
	                        k ++;
	                        break;
	                    default:
	                        break;

	                }
	            }
	        }
	        esIndice.setPriStoreSize(token.toString());
	        return esIndice;
	    }
	    private void putGendate(ESIndice esIndice,SimpleDateFormat format){
	    	int dsplit = esIndice.getIndex().lastIndexOf('-');

	        try {
	        	if(dsplit > 0){
		            String date = esIndice.getIndex().substring(dsplit+1);
		            esIndice.setGenDate((Date)format.parseObject(date));
	        	}

	        } catch (Exception e) {

	        }
	    }
	    public String refreshIndexInterval(String indexName,int interval) throws ElasticSearchException{
	    	return this.client.executeHttp(new StringBuilder().append(indexName).append("/_settings").toString(), new StringBuilder().append("{  \"index\" : {  \"refresh_interval\" : \"").append(interval).append("s\"   } }").toString(), HTTP_PUT);
	    }
	    public String refreshIndexInterval(String indexName,String indexType,int interval) throws ElasticSearchException{
	    	return this.client.executeHttp(new StringBuilder().append(indexName).append("/").append(indexType).append("/_settings").toString(), new StringBuilder().append("{  \"index\" : {  \"refresh_interval\" : \"").append(interval).append("s\"    } }").toString(), HTTP_PUT);
	    }
	    
	    public String refreshIndexInterval(int interval,boolean preserveExisting) throws ElasticSearchException{
	    	if(preserveExisting)
	    		return this.client.executeHttp("_all/_settings?preserve_existing=true", new StringBuilder().append("{  \"index\" : {  \"refresh_interval\" : \"").append(interval).append("s\"    } }").toString(), HTTP_PUT);
	    	else
	    		return this.client.executeHttp("_all/_settings?preserve_existing=false", new StringBuilder().append("{  \"index\" : {  \"refresh_interval\" : \"").append(interval).append("s\"    } }").toString(), HTTP_PUT);
	    }
	    
	    public String refreshIndexInterval(int interval) throws ElasticSearchException{
	    	return refreshIndexInterval(interval,false);
	    }
		 
 
}
