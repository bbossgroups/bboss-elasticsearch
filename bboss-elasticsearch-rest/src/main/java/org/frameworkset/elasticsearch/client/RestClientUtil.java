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
import org.frameworkset.elasticsearch.entity.suggest.*;
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
	/**
	 * ".security",".watches",
	 * 清除监控表
	 */
	protected String[] monitorIndices = new String[]{
			".monitoring*",".triggered_watches",
			".watcher-history*",".ml*"
	};
	protected String monitorIndicesString = ".monitoring*,.triggered_watches,.watcher-history*,.ml*";

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
		indexField.setIgnoreAbove(ResultUtil.intValue(fieldInfo.get("ignore_above"),null));
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
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentWithId(String indexName, String indexType, Object bean,Object docId) throws ElasticSearchException{
		return addDocumentWithId(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean,docId);
	}

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentWithId(String indexName, String indexType, Object bean,Object docId,Object parentId) throws ElasticSearchException{
		return addDocumentWithId(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean,docId,parentId);
	}
	public String addDateDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId) throws ElasticSearchException{
		return addDocumentWithId(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean,(Object)null,parentId);
	}


	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
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
	public String addDateDocument(String indexName, String indexType, Object bean,Object docId,String refreshOption) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean,docId,refreshOption);
	}

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
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
	public String addDateDocument(String indexName, String indexType, Object bean,Object docId,Object parentId,String refreshOption) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean,docId,  parentId,refreshOption);
	}

	public String addDateDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId,String refreshOption) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,     bean,(Object)null,  parentId,refreshOption);
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
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentsWithIdKey(String indexName, String indexType, List<Map> beans,String docIdKey) throws ElasticSearchException{
		return addDocumentsWithIdKey(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans,docIdKey);
	}

	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String addDateDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException{
		return addDocuments(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans,docIdKey,refreshOption);
	}
	public String addDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Map bean:beans) {
			try {
				evalBuilk(writer,indexName,indexType,bean,"index",docIdKey,(String)null);
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
	public String addDocumentsWithIdKey(String indexName, String indexType,  List<Map> beans,String docIdKey) throws ElasticSearchException{
		return addDocuments(indexName, indexType, beans,docIdKey,(String )null);
	}

/******/
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentsWithIdKey(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey) throws ElasticSearchException{
		return addDocumentsWithIdKey(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans,docIdKey,parentIdKey);
	}

	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String addDateDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException{
		return addDocuments(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans,docIdKey,parentIdKey,refreshOption);
	}
	public String addDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Map bean:beans) {
			try {
				evalBuilk(writer,indexName,indexType,bean,"index",docIdKey,parentIdKey);
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
	public String addDocumentsWithIdKey(String indexName, String indexType,  List<Map> beans,String docIdKey,String parentIdKey) throws ElasticSearchException{
		return addDocuments(indexName, indexType, beans,docIdKey, parentIdKey,(String)null);
	}

	protected void buildMeta(StringBuilder builder ,String indexType,String indexName, Object params,String action){
		Object id = this.getId(params);
		Object parentId = this.getParentId(params);
		if(id != null) {
			builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName)
					.append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : ");
			this.buildId(id,builder);
			if(parentId != null){
				builder.append(",\"parent\":");
				this.buildId(parentId,builder);
			}
			builder.append(" } }\n");
		}
		else {
			if(parentId == null)
				builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\" } }\n");
			else{
				builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\"");
				builder.append(",\"parent\":");
				this.buildId(parentId,builder);
				builder.append(" } }\n");
			}
		}
	}
	protected void buildId(Object id,StringBuilder builder){
		if (id instanceof String) {
			builder.append("\"").append(id).append("\"");

		}
		else{
			builder.append(id);
		}
	}
	protected void buildId(Object id,Writer writer) throws IOException {
		if (id instanceof String) {
			writer.write("\"");
			writer.write((String)id);
			writer.write("\"");

		}
		else{
			writer.write(String.valueOf(id));
		}
	}

	protected void buildMeta(Writer writer ,String indexType,String indexName, Object params,String action) throws IOException {
		Object id = this.getId(params);
		Object parentId = this.getParentId(params);
		buildMeta(  writer ,  indexType,  indexName,   params,  action,  id,  parentId);
	}

	protected void buildMetaWithDocIdKey(Writer writer ,String indexType,String indexName, Map params,String action,String docIdKey,String parentIdKey) throws IOException {
		Object id = docIdKey != null ?params.get(docIdKey):null;
		Object parentId = parentIdKey != null ?params.get(parentIdKey):null;
		buildMeta(  writer ,  indexType,  indexName,   params,  action,  id,  parentId);
	}

	protected void buildMeta(Writer writer ,String indexType,String indexName, Object params,String action,Object id,Object parentId) throws IOException {

		if(id != null) {
			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			writer.write("\", \"_id\" : ");
			this.buildId(id,writer);
			if(parentId != null){
				writer.write(", \"parent\" : ");
				this.buildId(parentId,writer);
			}
			writer.write(" } }\n");
		}
		else {

			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			if(parentId != null){
				writer.write(", \"parent\" : ");
				this.buildId(parentId,writer);
			}
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

	private void evalBuilk( Writer writer,String indexName, String indexType, Map param, String action,String docIdKey,String parentIdKey) throws IOException {

		if (param != null) {
			buildMetaWithDocIdKey(  writer ,  indexType,  indexName,   param,action,docIdKey,parentIdKey);
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
		ClassUtil.PropertieDescription pkProperty = beanInfo.getEsIdProperty();
//		if(pkProperty == null)
//			pkProperty = beanInfo.getPkProperty();
		if(pkProperty == null)
			return null;
		return beanInfo.getPropertyValue(bean,pkProperty.getName());
	}

	protected Object getParentId(Object bean){
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		ClassUtil.PropertieDescription pkProperty = beanInfo.getEsParentProperty();
//		if(pkProperty == null)
//			pkProperty = beanInfo.getPkProperty();
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

		Object id = this.getId(bean);
		Object parentId = this.getParentId(bean);
		return addDocument(indexName, indexType, bean,id,parentId,refreshOption);
//		StringBuilder builder = new StringBuilder();
//		Object id = this.getId(bean);
//		builder.append(indexName).append("/").append(indexType);
//		if(id != null){
//			builder.append("/").append(id);
//		}
//		if(refreshOption != null ){
//			builder.append("?").append(refreshOption);
//		}
//		String path = builder.toString();
//		builder = null;
//		path = this.client.executeHttp(path, SerialUtil.object2json(bean),ClientUtil.HTTP_POST);
//		return path;
	}

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocumentWithId(String indexName, String indexType, Object bean,Object docId) throws ElasticSearchException{
		return addDocument(indexName, indexType, bean,docId,null);
	}

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocumentWithId(String indexName, String indexType, Object bean,Object docId,Object parentId) throws ElasticSearchException{
		return addDocument(indexName, indexType, bean,docId,  parentId,(String)null);
	}

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId) throws ElasticSearchException{
		return addDocument(indexName, indexType, bean,(Object)null,  parentId,(String)null);
	}

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
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
	public String addDocument(String indexName, String indexType, Object bean,Object docId,String refreshOption) throws ElasticSearchException{
		return addDocument(  indexName,   indexType,   bean,docId,(Object)null,refreshOption);
	}

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
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
	public String addDocument(String indexName, String indexType, Object bean,Object docId,Object parentId,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		Object id = docId;
		builder.append(indexName).append("/").append(indexType);
		if(id != null){
			builder.append("/").append(id);
		}
		if(refreshOption != null ){
			builder.append("?").append(refreshOption);
			if(parentId != null){
				builder.append("&parent=").append(parentId);
			}
		}
		else{
			if(parentId != null){
				builder.append("?parent=").append(parentId);
			}
		}
		String path = builder.toString();
		builder = null;
		path = this.client.executeHttp(path, SerialUtil.object2json(bean),ClientUtil.HTTP_POST);
		return path;
	}

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
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
	public String addDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId,String refreshOption) throws ElasticSearchException{
		return addDocument(  indexName,   indexType,   bean,(Object )null,  parentId,  refreshOption);
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
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
	 * @param path twitter/_delete_by_query?routing=1
	 *             twitter/_doc/_delete_by_query?conflicts=proceed
	 *             twitter/_delete_by_query
	 *             twitter/_delete_by_query?scroll_size=5000
	 *
	 * @param entity
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteByQuery(String path,String entity) throws ElasticSearchException{
		return this.client.executeHttp(path,entity,ClientUtil.HTTP_POST);
	}
	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
	 * @param path twitter/_delete_by_query?routing=1
	 *             twitter/_doc/_delete_by_query?conflicts=proceed
	 *             twitter/_delete_by_query
	 *             twitter/_delete_by_query?scroll_size=5000
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteByQuery(String path,String templateName,Map params) throws ElasticSearchException{
		return null;
	}
	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
	 * @param path twitter/_delete_by_query?routing=1
	 *             twitter/_doc/_delete_by_query?conflicts=proceed
	 *             twitter/_delete_by_query
	 *             twitter/_delete_by_query?scroll_size=5000
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteByQuery(String path,String templateName,Object params) throws ElasticSearchException{
		return null;
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

	/**
	 * 获取json格式文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/0
	 *             twitter/_doc/0?_source=false
	 *             twitter/_doc/0?_source_include=*.id&_source_exclude=entities
	 *             twitter/_doc/0?_source=*.id,retweeted
	 *             twitter/_doc/1?stored_fields=tags,counter
	 *             twitter/_doc/2?routing=user1&stored_fields=tags,counter
	 *             twitter/_doc/1/_source
	 *             GET twitter/_doc/2?routing=user1
	 *
	 * @param path
	 * @return
	 * @throws ElasticSearchException
	 */
	public String getDocumentByPath(String path) throws ElasticSearchException{
		return this.client.executeHttp(path,ClientUtil.HTTP_GET);
	}


	/**
	 * 获取json格式文档Source,不带索引元数据
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/1/_source 其中1为文档id
	 * @return
	 * @throws ElasticSearchException
	 */
	public String getDocumentSource(String path) throws ElasticSearchException{
		return this.client.executeHttp(path,ClientUtil.HTTP_GET);
	}

	/**
	 * 获取json格式文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/0
	 *             twitter/_doc/0?_source=false
	 *             twitter/_doc/0?_source_include=*.id&_source_exclude=entities
	 *             twitter/_doc/0?_source=*.id,retweeted
	 *             twitter/_doc/1?stored_fields=tags,counter
	 *             twitter/_doc/2?routing=user1&stored_fields=tags,counter
	 *             twitter/_doc/1/_source
	 *             GET twitter/_doc/2?routing=user1
	 *
	 * @param path
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T getDocumentByPath(String path,Class<T> beanType) throws ElasticSearchException{
		SearchHit searchResult = this.client.executeRequest(path,null,   new GetDocumentResponseHandler( beanType),ClientUtil.HTTP_GET);
		return ResultUtil.buildObject(searchResult, beanType);
	}



	/**
	 * 获取文档Source对象，不带索引元数据
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/1/_source 其中1为文档id
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T getDocumentSource(String path,Class<T> beanType) throws ElasticSearchException{
		T searchResult = (T) this.client.executeRequest(path,null,   new GetDocumentSourceResponseHandler( beanType),ClientUtil.HTTP_GET);
		return searchResult;
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

		return ResultUtil.buildObject(searchResult, beanType);

	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param path _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param entity
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocuments(String path,String entity,Class<T> type)  throws ElasticSearchException{
		MGetDocs searchResult = (MGetDocs) this.client.executeRequest(path,entity,   new MGetDocumentsSourceResponseHandler( type),ClientUtil.HTTP_POST);

		return ResultUtil.buildObjects(searchResult, type);
	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param path _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param templateName
	 * @param params
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocuments(String path,String templateName,Object params,Class<T> type)  throws ElasticSearchException{
		return null;
	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param path _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param templateName
	 * @param params
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocuments(String path,String templateName,Map params,Class<T> type)  throws ElasticSearchException{
		return null;
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

	/**
	 * 没有报文的请求处理api
	 * @param path 请求url相对路径，可以带参数
	 * @param action
	 * @return
	 * @throws ElasticSearchException
	 */
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
	/**
	 * 删除索引文档
	 * @param path /twitter/_doc/1
	 *             /twitter/_doc/1?routing=kimchy
	 *             /twitter/_doc/1?timeout=5m
	 * @return
	 */
	public String deleteByPath(String path) throws ElasticSearchException{
		return this.client.executeHttp(path,ClientUtil.HTTP_DELETE);
	}
	/**
	 * 判断索引是否存在
	 * @param indiceName
	 * @return
	 * @throws ElasticSearchException
	 */
	public boolean existIndice(String indiceName) throws ElasticSearchException{
		try {
			executeHttp(indiceName, ClientInterface.HTTP_HEAD);
			return true;
		}
		catch(ElasticSearchException exception){
			String msg = exception.getMessage();
			if(msg.endsWith("Unexpected response status: 404"))
				return false;
			else
				throw exception;
		}

	}

	/**
	 * 判断所引类型是否存在
	 * @param indiceName
	 * @param type
	 * @return
	 * @throws ElasticSearchException
	 */
	public boolean existIndiceType(String indiceName,String type) throws ElasticSearchException{
		try {
			executeHttp(new StringBuilder(indiceName).append("/_mapping/").append(type).toString(), ClientInterface.HTTP_HEAD);
			return true;
		}
		catch(ElasticSearchException exception){
			String msg = exception.getMessage();
			if(msg.endsWith("Unexpected response status: 404"))
				return false;
			else
				throw exception;
		}
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

	public long count(String index,String entity)  throws ElasticSearchException{
		MapRestResponse searchResult = this.client.executeRequest(new StringBuilder().append(index).append("/_count").toString(),
				entity,   new ElasticSearchMapResponseHandler(  ));
//		if(searchResult instanceof ErrorResponse){
//			throw new ElasticSearchException(SimpleStringUtil.object2json(searchResult));
//		}
		return searchResult.getCount();
	}
	public long count(String index,String template,Map params)  throws ElasticSearchException{
		throw new ElasticSearchException("Un implements method");
	}
	public long count(String path,String template,Object params)  throws ElasticSearchException{
		throw new ElasticSearchException("Un implements method");
	}

	@Override
	public TermRestResponse termSuggest(String path, String entity) throws ElasticSearchException {
		TermRestResponse searchResult = this.client.executeRequest(path,entity,   new ElasticSearchTermResponseHandler( ));
		return searchResult;
	}

	@Override
	public PhraseRestResponse phraseSuggest(String path, String entity) throws ElasticSearchException {
		PhraseRestResponse searchResult = this.client.executeRequest(path,entity,   new ElasticSearchPhraseResponseHandler( ));
		return searchResult;
	}

	@Override
	public CompleteRestResponse complateSuggest(String path, String entity, Class<?> type) throws ElasticSearchException {
		RestResponse searchResult = this.client.executeRequest(path,entity,   new CompleteElasticSearchResponseHandler( type));
		return (CompleteRestResponse)searchResult;
	}

	@Override
	public CompleteRestResponse complateSuggest(String path, String entity) throws ElasticSearchException {
		RestResponse searchResult = this.client.executeRequest(path,entity,   new CompleteElasticSearchResponseHandler( Map.class));
		return (CompleteRestResponse)searchResult;
	}

	public CompleteRestResponse complateSuggest(String path, String templateName,Map params) throws ElasticSearchException{
		return null;
	}

	public CompleteRestResponse complateSuggest(String path, String templateName,Object params) throws ElasticSearchException{
		return null;
	}
	@Override
	public TermRestResponse termSuggest(String path, String templateName, Object params) throws ElasticSearchException {
		return null;
	}

	@Override
	public PhraseRestResponse phraseSuggest(String path, String templateName, Object params) throws ElasticSearchException {
		return null;
	}

	@Override
	public TermRestResponse termSuggest(String path, String templateName, Map params) throws ElasticSearchException {
		return null;
	}

	@Override
	public PhraseRestResponse phraseSuggest(String path, String templateName, Map params) throws ElasticSearchException {
		return null;
	}



	@Override
	public CompleteRestResponse complateSuggest(String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
		return null;
	}

	@Override
	public CompleteRestResponse complateSuggest(String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
		return null;
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

	/**
	 * scroll search
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-scroll.html
	 * @param scroll
	 * @param scrollId
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchScroll(String scroll,String scrollId ,Class<T> type) throws ElasticSearchException{
		StringBuilder entity = new StringBuilder();
		entity.append("{\"scroll\" : \"").append(scroll).append("\",\"scroll_id\" : \"").append(scrollId).append("\"}");
		RestResponse result = this.client.executeRequest("_search/scroll",entity.toString(),   new ElasticSearchResponseHandler( type));
		return ResultUtil.buildESDatas(result,type);
	}

	/**
	 * scroll search
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-scroll.html
	 * @param scroll
	 * @param scrollId

	 * @return
	 * @throws ElasticSearchException
	 */
	public String searchScroll(String scroll,String scrollId ) throws ElasticSearchException{
		StringBuilder entity = new StringBuilder();
		entity.append("{\"scroll\" : \"").append(scroll).append("\",\"scroll_id\" : \"").append(scrollId).append("\"}");
		String result = this.client.executeHttp("_search/scroll",entity.toString(),ClientUtil.HTTP_GET);
		return result;
	}


	/**
	 * 清理scrollId
	 * @param scrollIds
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteScrolls(String ... scrollIds) throws ElasticSearchException{
		if(scrollIds == null || scrollIds.length == 0)
			return null;
		StringBuilder entity = new StringBuilder();
		entity.append("{\"scroll_id\" : [");
		for(int i = 0; i < scrollIds.length; i ++){
			String scrollId = scrollIds[i];
			if(i > 0)
				entity.append(",");
			entity.append("\"").append(scrollId).append("\"");
		}
		entity.append("]}");
		String result = this.client.executeHttp("_search/scroll",entity.toString(),ClientUtil.HTTP_DELETE);
		return result;
	}

	/**
	 * 清理scrollId
	 * @param scrollIds
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteScrolls(List<String> scrollIds) throws ElasticSearchException{
		if(scrollIds == null || scrollIds.size() == 0)
			return null;
		StringBuilder entity = new StringBuilder();
		entity.append("{\"scroll_id\" : [");
		for(int i = 0; i < scrollIds.size(); i ++){
			String scrollId = scrollIds.get(i);
			if(i > 0)
				entity.append(",");
			entity.append("\"").append(scrollId).append("\"");
		}
		entity.append("]}");
		String result = this.client.executeHttp("_search/scroll",entity.toString(),ClientUtil.HTTP_DELETE);
		return result;
	}

	/**
	 * 清理all scrollId
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteAllScrolls() throws ElasticSearchException{
		String result = this.client.executeHttp("_search/scroll/_all",ClientUtil.HTTP_DELETE);
		return result;
	}


	public <T> ESDatas<T> searchList(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	public <T> ESDatas<T> searchList(String path, String templateName, Object params,Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	public <T> ESDatas<T> searchList(String path, String entity, Class<T> type) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
		return ResultUtil.buildESDatas(result,type);
	}

	public <T> T searchObject(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
	 	return null;
	}

	public <T> T searchObject(String path, String templateName, Object params,Class<T> type) throws ElasticSearchException {
	 	return null;
	}

	public <T> T searchObject(String path, String entity, Class<T> type) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
		return ResultUtil.buildObject(result, type);
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
		return ResultUtil.buildESAggDatas(result,type,aggs,stats,(ESAggBucketHandle<T>)null);
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Class<T> type,String aggs,String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( ));
		return ResultUtil.buildESAggDatas(result,type,aggs,stats,aggBucketHandle);
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

	/**
	 * 删除所有监控索引
	 * .security,.monitoring*,.watches,.triggered_watches,.watcher-history*,.ml*
	 * @return
	 */
	    public String cleanAllXPackIndices() throws ElasticSearchException{

			StringBuilder ret = new StringBuilder();
			for(String monitor:monitorIndices) {
				try {
					ret.append(this.client.executeHttp(java.net.URLEncoder.encode(monitor, "UTF-8") + "?pretty", HTTP_DELETE)).append("\n");
				}
				catch (Exception e){
					ret.append(e.getMessage()).append("\n");
				}
			}
			return ret.toString();

		}


	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param path test/_doc/1
	 *             test/_doc/1/_update
	 *
	 *
	 * @param entity
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByPath(String path,String entity) throws ElasticSearchException{
		return this.client.executeHttp(path,entity,ClientUtil.HTTP_POST);
	}

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param path test/_doc/1
	 *             test/_doc/1/_update
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByPath(String path,String templateName,Map params) throws ElasticSearchException{
		return null;
	}

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param path test/_doc/1
	 *             test/_doc/1/_update
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByPath(String path,String templateName,Object params) throws ElasticSearchException{
		return null;
	}

	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByQuery(String path) throws ElasticSearchException{
		return this.client.executeHttp(path,ClientUtil.HTTP_POST);
	}
	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 *             twitter/_doc/_update_by_query?conflicts=proceed
	 *             twitter/_update_by_query
	 *             twitter,blog/_doc,post/_update_by_query
	 *             twitter/_update_by_query?routing=1
	 *             twitter/_update_by_query?scroll_size=100
	 *             twitter/_update_by_query?pipeline=set-foo
	 *
	 *
	 * @param entity
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByQuery(String path,String entity) throws ElasticSearchException{
		return this.client.executeHttp(path,entity,ClientUtil.HTTP_POST);
	}

	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 *             twitter/_doc/_update_by_query?conflicts=proceed
	 *             twitter/_update_by_query
	 *             twitter,blog/_doc,post/_update_by_query
	 *             twitter/_update_by_query?routing=1
	 *             twitter/_update_by_query?scroll_size=100
	 *             twitter/_update_by_query?pipeline=set-foo
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByQuery(String path,String templateName,Map params) throws ElasticSearchException{
		return null;
	}

	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 *             twitter/_doc/_update_by_query?conflicts=proceed
	 *             twitter/_update_by_query
	 *             twitter,blog/_doc,post/_update_by_query
	 *             twitter/_update_by_query?routing=1
	 *             twitter/_update_by_query?scroll_size=100
	 *             twitter/_update_by_query?pipeline=set-foo
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByQuery(String path,String templateName,Object params) throws ElasticSearchException{
		return null;
	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param index _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param indexType
	 * @param type
	 * @param ids
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocuments(String index,String indexType,Class<T> type,Object ... ids)  throws ElasticSearchException{
		if(ids == null || ids.length ==0)
			return null;
		StringBuilder path = new StringBuilder();
		if(indexType == null || indexType.equals(""))
			path.append(index).append("/_mget");
		else
			path.append(index).append("/").append(indexType).append("/_mget");
		StringBuilder builder = new StringBuilder();
		builder.append(" {\"ids\":");
		Writer writer = new BBossStringWriter(builder);
		SerialUtil.object2json(ids,writer);
		builder.append("}");
		MGetDocs searchResult = (MGetDocs) this.client.executeRequest(path.toString(),builder.toString(),   new MGetDocumentsSourceResponseHandler( type),ClientUtil.HTTP_POST);

		return ResultUtil.buildObjects(searchResult, type);

	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param index _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param indexType
	 * @param ids
	 * @return
	 * @throws ElasticSearchException
	 */
	public String mgetDocuments(String index,String indexType,Object ... ids)  throws ElasticSearchException{
		if(ids == null || ids.length ==0)
			return null;
		StringBuilder path = new StringBuilder();
		if(indexType == null || indexType.equals(""))
			path.append(index).append("/_mget");
		else
			path.append(index).append("/").append(indexType).append("/_mget");
		StringBuilder builder = new StringBuilder();
		builder.append(" {\"ids\":");
		Writer writer = new BBossStringWriter(builder);
		SerialUtil.object2json(ids,writer);
		builder.append("}");
		String searchResult = this.client.executeHttp(path.toString(),builder.toString(),   ClientUtil.HTTP_POST);

		return searchResult;

	}

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocument(String index,String indexType,Object id,Object params) throws ElasticSearchException{
		return updateDocument(index,indexType,id,params,null);
	}

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocument(String index,String indexType,Object id,Map params) throws ElasticSearchException{
		return updateDocument(index,indexType,id,params,null);
	}


	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
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
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocument(String index,String indexType,Object id,Map params,String refreshOption) throws ElasticSearchException{
		StringBuilder path = new StringBuilder();
		if(indexType == null || indexType.equals(""))
			path.append(index).append("/").append(id).append("/_update");
		else
			path.append(index).append("/").append(indexType).append("/").append(id).append("/_update");
		if(refreshOption != null){
			path.append("?").append(refreshOption);
		}
		StringBuilder builder = new StringBuilder();
		builder.append(" {\"doc\":");
		Writer writer = new BBossStringWriter(builder);
		SerialUtil.object2json(params,writer);
		builder.append("}");
		String searchResult = this.client.executeHttp(path.toString(),builder.toString(),   ClientUtil.HTTP_POST);

		return searchResult;
	}

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
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
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocument(String index,String indexType,Object id,Object params,String refreshOption) throws ElasticSearchException{
		StringBuilder path = new StringBuilder();
		if(indexType == null || indexType.equals(""))
			path.append(index).append("/").append(id).append("/_update");
		else
			path.append(index).append("/").append(indexType).append("/").append(id).append("/_update");
		if(refreshOption != null){
			path.append("?").append(refreshOption);
		}
		StringBuilder builder = new StringBuilder();
		builder.append(" {\"doc\":");
		Writer writer = new BBossStringWriter(builder);
		SerialUtil.object2json(params,writer);
		builder.append("}");
		String searchResult = this.client.executeHttp(path.toString(),builder.toString(),   ClientUtil.HTTP_POST);

		return searchResult;
	}

}
