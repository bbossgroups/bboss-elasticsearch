package org.frameworkset.elasticsearch.client;

import com.frameworkset.util.SimpleStringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.SliceRunTask;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.entity.sql.ColumnMeta;
import org.frameworkset.elasticsearch.entity.sql.SQLRestResponse;
import org.frameworkset.elasticsearch.entity.sql.SQLRestResponseHandler;
import org.frameworkset.elasticsearch.entity.sql.SQLResult;
import org.frameworkset.elasticsearch.entity.suggest.*;
import org.frameworkset.elasticsearch.handler.*;
import org.frameworkset.elasticsearch.scroll.DefualtScrollHandler;
import org.frameworkset.elasticsearch.scroll.ParallelSliceScrollResult;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;
import org.frameworkset.elasticsearch.scroll.SliceScrollResultInf;
import org.frameworkset.elasticsearch.serial.ESInnerHitSerialThreadLocal;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.elasticsearch.serial.SerialContext;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.elasticsearch.template.ESInfo;
import org.frameworkset.json.JsonTypeReference;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @see <p>https://my.oschina.net/bboss/blog/1556866</p>
 */
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

	/**
	 * 获取索引表字段信息
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public List<IndexField> getIndexMappingFields(String index,final String indexType) throws ElasticSearchException{
		try{
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
								IndexField indexField = BuildTool.buildIndexField(field,fields,null);
							}
							break;

						}
						return null;

					} else {
						HttpEntity entity = response.getEntity();
						if (entity != null )
							throw new ElasticSearchException(new StringBuilder().append("Unexpected response : " ).append( EntityUtils.toString(entity)).toString(),status);
						else
							throw new ElasticSearchException("Unexpected response status: " + status,status);
					}
				}
			});
			return fields;
		}
		catch(ElasticSearchException e){
			return (List<IndexField>)ResultUtil.hand404HttpRuntimeException(e,Object.class,ResultUtil.OPERTYPE_getIndice);
		}

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
	public String getDynamicIndexName(String indexName){
		return this.indexNameBuilder.getIndexName(indexName);
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
		return addDocuments(indexName, indexType,  beans,(String) null);
	}
	public String addDocuments(String indexName, String indexType,  List<?> beans,String refreshOption) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Object bean:beans) {
			try {
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"index");
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
//		if(beans == null || beans.size() == 0)
//			return null;
//		StringBuilder builder = new StringBuilder();
//		BBossStringWriter writer = new BBossStringWriter(builder);
//		for(Map bean:beans) {
//			try {
//				BuildTool.evalBuilk(writer,indexName,indexType,bean,"index",docIdKey,(String)null);
//			} catch (IOException e) {
//				throw new ElasticSearchException(e);
//			}
//		}
//		writer.flush();
//		if(refreshOption == null)
//			return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
//		else
//			return this.client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
		return addDocuments(  indexName,   indexType,  beans,  docIdKey,(String)null,  refreshOption);
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
	public String addDateMapDocuments(String indexName, String indexType, List<Map> beans,ClientOptions clientOptions) throws ElasticSearchException{
		return addMapDocuments(this.indexNameBuilder.getIndexName(indexName),   indexType,  beans,  clientOptions);
	}
	public String addMapDocuments(String indexName, String indexType, List<Map> beans,ClientOptions clientOptions) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Map bean:beans) {
			try {
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"index",clientOptions);
			} catch (IOException e) {
				throw new ElasticSearchException(e);
			}
		}
		writer.flush();
		if(clientOptions == null || clientOptions.getRefreshOption() == null)
			return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		else
			return this.client.executeHttp("_bulk?"+clientOptions.getRefreshOption(),ClientUtil.HTTP_POST);
	}
	public String addDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Map bean:beans) {
			try {
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"index",docIdKey,parentIdKey);
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




	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocument(String indexName, String indexType, Object bean) throws ElasticSearchException{
		return this.addDocument(indexName,indexType,bean,(String)null);
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
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		Object id = BuildTool.getId(bean,beanInfo);
		Object parentId = BuildTool.getParentId(bean,beanInfo);
		Object routing = BuildTool.getRouting(bean,beanInfo);
		return addDocument(indexName, indexType, bean,id,parentId,routing,refreshOption);

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
	public String addDocument(String indexName, String indexType, Object bean,Object docId,Object parentId,String refreshOption) throws ElasticSearchException{
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		Object routing = BuildTool.getRouting(bean,beanInfo);
		return addDocument( indexName,  indexType,  bean, docId, parentId, (Object)routing, refreshOption);
	}
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocument(String indexName, String indexType, Object params,ClientOptions clientOptions) throws ElasticSearchException{
		if(params instanceof Map){
			return addMapDocument(indexName,indexType,(Map)params,clientOptions);
		}
		Object docId = null;
		Object parentId = null;
		Object routing = null;
		String refreshOption = null;
		if(clientOptions != null) {
			refreshOption = clientOptions.getRefreshOption();
			ClassUtil.ClassInfo beanClassInfo = ClassUtil.getClassInfo(params.getClass());
			docId = clientOptions.getIdField() != null ? BuildTool.getId(params, beanClassInfo, clientOptions.getIdField()) : null;
			parentId = clientOptions.getParentIdField() != null ? BuildTool.getParentId(params, beanClassInfo, clientOptions.getParentIdField()) : null;
			routing = clientOptions.getRountField() != null ? BuildTool.getRouting(params, beanClassInfo, clientOptions.getRountField()) : null;
		}
		return addDocument(  indexName,   indexType,   params,  docId,  parentId,  routing,  refreshOption);
	}
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocument(String indexName, String indexType, Object bean,ClientOptions clientOptions) throws ElasticSearchException{
		return addDocument(  this.indexNameBuilder.getIndexName(indexName),   indexType,   bean,  clientOptions);
	}
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addMapDocument(String indexName, String indexType, Map params,ClientOptions clientOptions) throws ElasticSearchException{
		Object docId = null;
		Object parentId = null;
		Object routing = null;
		String refreshOption = null;
		if(clientOptions != null) {
			refreshOption = clientOptions.getRefreshOption();
			docId = clientOptions.getIdField() != null ? params.get(clientOptions.getIdField()) : null;
			parentId = clientOptions.getParentIdField() != null ? params.get( clientOptions.getParentIdField()) : null;
			routing = clientOptions.getRountField() != null ? params.get(clientOptions.getRountField()) : null;
		}
		return addDocument(  indexName,   indexType,   params,  docId,  parentId,  routing,  refreshOption);
	}

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addMapDocument(String indexName, String indexType, Map bean) throws ElasticSearchException{
		return addDocument(  indexName,   indexType,   bean,null,null,null,(String )null);
	}

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateMapDocument(String indexName, String indexType, Map bean) throws ElasticSearchException{
		return addDocument(  this.indexNameBuilder.getIndexName(indexName),   indexType,   bean,null,null,null,(String )null);
	}
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public  String addDateMapDocument(String indexName, String indexType, Map params,ClientOptions clientOptions) throws ElasticSearchException{
		return addMapDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,   params,clientOptions);
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
	public String addDocument(String indexName, String indexType, Object bean,Object docId,Object parentId,Object routing,String refreshOption) throws ElasticSearchException{
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
			if(routing != null){
				builder.append("&routing=").append(routing);
			}
		}
		else{
			if(parentId != null){
				builder.append("?parent=").append(parentId);
				if(routing != null){
					builder.append("&routing=").append(routing);
				}
			}
			else if(routing != null){
				builder.append("?routing=").append(routing);
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
		return updateDocuments(indexName, indexType, beans,(String)null);
	}
	public String updateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException{

		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Object bean:beans) {
			try {
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"update");
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
	public  String updateDocumentsWithIdKey(String indexName, String indexType, List<Map> beans,String docIdKey) throws ElasticSearchException
	{
		return updateDocuments( indexName,  indexType, beans, docIdKey,(String)null);
	}
	public String updateDocumentsWithIdKey(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey) throws ElasticSearchException{
		return updateDocuments( indexName,  indexType, beans, docIdKey,parentIdKey);
	}
	public  String updateDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException
	{
		return updateDocuments(  indexName,   indexType,   beans,  docIdKey,(String )null,  refreshOption);
	}

	public String updateDocuments(String indexName, String indexType, List<?> beans,ClientOptions clientOptions) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Object bean:beans) {
			try {
//				BuildTool.evalBuilk(writer,indexName,indexType,bean,"update");
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"update",clientOptions);
			} catch (IOException e) {
				throw new ElasticSearchException(e);
			}
		}
		writer.flush();
		if(clientOptions != null && clientOptions.getRefreshOption() != null) {
			return this.client.executeHttp(new StringBuilder().append("_bulk?")
														.append( clientOptions.getRefreshOption()).toString(), builder.toString(), ClientUtil.HTTP_POST);
		}
		else {
			return this.client.executeHttp("_bulk", builder.toString(), ClientUtil.HTTP_POST);
		}
	}
	public String updateDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Map bean:beans) {
			try {
//				BuildTool.evalBuilk(writer,indexName,indexType,bean,"update");
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"update",docIdKey,parentIdKey);
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
		try{
			return client.executeHttp("/_template/"+template,ClientUtil.HTTP_DELETE);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_deleteTempate);
		}
	}
	
	/**
	 * 查询模板
	 * @param template
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String getTempate(String template) throws ElasticSearchException {
		try {
			return client.executeHttp("/_template/" + template, ClientUtil.HTTP_GET);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_getTemplate);
		}
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
		try {
			return this.client.executeHttp(BuildTool.buildGetDocumentRequest(indexName, indexType, documentId, options), ClientUtil.HTTP_GET);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_getDocument);
		}
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
		try {
			return this.client.executeHttp(path,ClientUtil.HTTP_GET);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_getDocument);
		}
	}


	/**
	 * 获取json格式文档Source,不带索引元数据
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/1/_source 其中1为文档id
	 * @return
	 * @throws ElasticSearchException
	 */
	public String getDocumentSource(String path) throws ElasticSearchException{
		try {
			return this.client.executeHttp(path,ClientUtil.HTTP_GET);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_getDocument);
		}
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
		try {
			SearchHit searchResult = this.client.executeRequest(path,null,   new GetDocumentResponseHandler( beanType),ClientUtil.HTTP_GET);
			return ResultUtil.buildObject(searchResult, beanType);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,beanType,ResultUtil.OPERTYPE_getDocument);
		}
	}



	/**
	 * 获取文档Source对象，不带索引元数据
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/1/_source 其中1为文档id
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T getDocumentSource(String path,Class<T> beanType) throws ElasticSearchException{
		try {
			T searchResult = (T) this.client.executeRequest(path,null,   new GetDocumentSourceResponseHandler( beanType),ClientUtil.HTTP_GET);
			return searchResult;
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,beanType,ResultUtil.OPERTYPE_getDocument);
		}
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
		try{
			SearchHit searchResult = this.client.executeRequest(BuildTool.buildGetDocumentRequest(  indexName,   indexType,  documentId,  options),null,   new GetDocumentResponseHandler( beanType),ClientUtil.HTTP_GET);

			return ResultUtil.buildObject(searchResult, beanType);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,beanType,ResultUtil.OPERTYPE_getDocument);
		}

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
		try {
			MapSearchHit searchResult = this.client.executeRequest(BuildTool.buildGetDocumentRequest(indexName, indexType, documentId, options), null, new GetDocumentHitResponseHandler(), ClientUtil.HTTP_GET);
			return searchResult;
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,MapSearchHit.class,ResultUtil.OPERTYPE_getDocument);
		}
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

		try {
			return this.client.executeHttp(new StringBuilder().append(indexName).append("/").append(indexType).append("/").append(id).toString(), ClientUtil.HTTP_DELETE);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_deleteDocument);
		}
	}
	public   String deleteDocument(String indexName, String indexType, String id,String refreshOption) throws ElasticSearchException{
		try {
			return this.client.executeHttp(new StringBuilder().append(indexName).append("/").append(indexType).append("/")
					.append(id).append("?").append(refreshOption).toString(),ClientUtil.HTTP_DELETE);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_deleteDocument);
		}
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
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T>  sql(Class<T> beanType , String entity ,Map params) throws ElasticSearchException {
		return null;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public  <T> List<T>  sql(Class<T> beanType , String entity ,Object bean) throws ElasticSearchException {
		return null;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T>  sql(Class<T> beanType,  String entity) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest("/_xpack/sql",entity,   new SQLRestResponseHandler());
		return ResultUtil.buildSQLResult(result,beanType);
	}

	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T>  fetchQuery(Class<T> beanType , String entity ,Map params) throws ElasticSearchException {
		return null;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public  <T> SQLResult<T> fetchQuery(Class<T> beanType , String entity , Object bean) throws ElasticSearchException {
		return null;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T>  fetchQuery(Class<T> beanType,  String entity) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest("/_xpack/sql",entity,   new SQLRestResponseHandler());
		SQLResult<T> datas = ResultUtil.buildFetchSQLResult(  result,  beanType,  (SQLResult<T> )null);
		datas.setClientInterface(this);
		return datas;
	}



	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> T  sqlObject(Class<T> beanType , String entity ,Map params) throws ElasticSearchException {
		return null;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> T  sqlObject(Class<T> beanType , String entity ,Object bean) throws ElasticSearchException {
		return null;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param entity

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  sqlObject(Class<T> beanType,  String entity ) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest("/_xpack/sql",entity,   new SQLRestResponseHandler());
		return ResultUtil.buildSQLObject(result,beanType);
	}
	public String closeSQLCursor(String cursor) throws ElasticSearchException {
		return this.client.executeRequest("/_xpack/sql/close",
				new StringBuilder().append("{\"cursor\": \"").append(cursor).append("\"}").toString(),
				new ESStringResponseHandler(),HTTP_POST);
	}

	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param oldPage
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T> fetchQueryByCursor(Class<T> beanType, SQLResult<T> oldPage ) throws ElasticSearchException {
		if(oldPage.getCursor() == null ){
			return null;
		}
		SQLRestResponse result = this.client.executeRequest("/_xpack/sql",
														new StringBuilder().append("{\"cursor\": \"").append(oldPage.getCursor()).append("\"}").toString(),
														new SQLRestResponseHandler());

		SQLResult<T> datas = ResultUtil.buildFetchSQLResult(  result,  beanType,  oldPage);
		datas.setClientInterface(this);
		return datas;
	}

	/**
	 * 发送es restful sql请求下一页数据，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param cursor
	 * @param metas
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T> fetchQueryByCursor(Class<T> beanType, String cursor, ColumnMeta[] metas) throws ElasticSearchException{
		if(cursor == null ){
			return null;
		}
		SQLRestResponse result = this.client.executeRequest("/_xpack/sql",
				new StringBuilder().append("{\"cursor\": \"").append(cursor).append("\"}").toString(),
				new SQLRestResponseHandler());

		SQLResult<T> datas = ResultUtil.buildFetchSQLResult(  result,  beanType,  metas);
		datas.setClientInterface(this);
		return datas;
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
	public <T> T discover(String path, String action,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return this.client.discover(path,action,responseHandler);
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
		try {
			if (pretty)
				return this.client.executeHttp(index + "/_mapping?pretty", ClientUtil.HTTP_GET);
			else
				return this.client.executeHttp(index + "/_mapping", ClientUtil.HTTP_GET);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_getIndice);
		}
	}

	public <T> T getIndexMapping(String index,boolean pretty,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		try {
			if (pretty)
				return this.client.executeRequest(index + "/_mapping?pretty", null, responseHandler, ClientUtil.HTTP_GET);
			else
				return this.client.executeRequest(index + "/_mapping", null, responseHandler, ClientUtil.HTTP_GET);
		}
		catch(ElasticSearchException e){
			return (T)ResultUtil.hand404HttpRuntimeException(e,Object.class,ResultUtil.OPERTYPE_getIndice);
		}
	}
	/**
	 * 删除索引文档
	 * @param path /twitter/_doc/1
	 *             /twitter/_doc/1?routing=kimchy
	 *             /twitter/_doc/1?timeout=5m
	 * @return
	 */
	public String deleteByPath(String path) throws ElasticSearchException{
		try{
			return this.client.executeHttp(path,ClientUtil.HTTP_DELETE);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_deleteDocument);
		}
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
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,boolean.class,ResultUtil.OPERTYPE_existIndice);
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
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,boolean.class,ResultUtil.OPERTYPE_existIndiceType);
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
	public long count(String index,String template,Object params)  throws ElasticSearchException{
		throw new ElasticSearchException("Un implements method");
	}

	public long countAll(String index)  throws ElasticSearchException{
		String queryAll = "{\"query\": {\"match_all\": {}}}";
		MapRestResponse searchResult = this.client.executeRequest(new StringBuilder().append(index).append("/_count").toString(),
				queryAll,   new ElasticSearchMapResponseHandler(  ));
//		if(searchResult instanceof ErrorResponse){
//			throw new ElasticSearchException(SimpleStringUtil.object2json(searchResult));
//		}
		return searchResult.getCount();
	}

	/**
	 * 检索索引所有数据
	 * @param index
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchAll(String index,  Class<T> type) throws ElasticSearchException{

		return searchAll(  index,    DEFAULT_FETCHSIZE , type) ;

	}

	/**
	 * 检索索引所有数据
	 * @param index
	 * @param fetchSize
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public  <T> ESDatas<T> searchAll(String index,  int fetchSize ,Class<T> type) throws ElasticSearchException{
		return searchAll(index,  fetchSize ,(ScrollHandler<T>) null,type);
	}

	/**
	 * 检索索引所有数据
	 * @param index
	 * @param fetchSize 指定每批次返回的数据，不指定默认为5000
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchAll(String index,  int fetchSize ,ScrollHandler<T> scrollHandler,Class<T> type) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		String queryAll = builder.append("{ \"size\":").append(fetchSize).append(",\"query\": {\"match_all\": {}}}").toString();
		builder.setLength(0);
		return this.scroll(builder.append(index).append("/_search").toString(),queryAll,"10m",type,scrollHandler);
	}



	/**
	 * 检索索引所有数据,每批次返回默认为5000条数据，
	 * @param index
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchAll(String index,ScrollHandler<T> scrollHandler , Class<T> type) throws ElasticSearchException{
		return searchAll(  index,  DEFAULT_FETCHSIZE ,  scrollHandler,type);
	}

	/***************************slice searchAll start****************************/

	/**
	 * 并行检索索引所有数据
	 * @param index
	 * @param fetchSize 指定每批次返回的数据，不指定默认为5000
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchAllParallel(String index,  int fetchSize ,Class<T> type,int thread) throws ElasticSearchException{
		return searchAllParallel(index,  fetchSize,(ScrollHandler<T>)null,type,thread);
	}

	/**
	 * 并行检索索引所有数据,每批次返回默认为5000条数据，
	 * @param index
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchAllParallel(String index,  Class<T> type,int thread) throws ElasticSearchException{
			return searchAllParallel(index,  ClientInterface.DEFAULT_FETCHSIZE,type,thread);
	}

	/**
	 * 并行检索索引所有数据
	 * @param index
	 * @param fetchSize 指定每批次返回的数据，不指定默认为5000
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchAllParallel(String index,  final int fetchSize ,ScrollHandler<T> scrollHandler,final Class<T> type,int max) throws ElasticSearchException{
		SliceScroll sliceScroll = new SliceScroll() {
			@Override
			public String buildSliceDsl(int sliceId, int max) {
				StringBuilder builder = new StringBuilder();
				String sliceDsl = builder.append("{\"slice\": {\"id\": ").append(sliceId).append(",\"max\": ")
						.append(max).append("},\"size\":").append(fetchSize).append(",\"query\": {\"match_all\": {}}}").toString();
				return sliceDsl;
//				return buildSliceDsl(i,max, params, dslTemplate);
			}
		};

		return _slice(index+"/_search",  scrollHandler,type,max,"1m",sliceScroll);

	}
	/**
	 * 并行检索索引所有数据
	 * @param path
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param max 并行度，线程数
	 * @param sliceScroll
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	protected <T> ESDatas<T> _slice(String path,  ScrollHandler<T> scrollHandler,final Class<T> type,int max,
								  final String scroll,SliceScroll sliceScroll) throws ElasticSearchException{



		long starttime = System.currentTimeMillis();
		//scroll slice分页检索

//		final CountDownLatch countDownLatch = new CountDownLatch(max);//线程任务完成计数器，每个线程对应一个sclice,每运行完一个slice任务,countDownLatch计数减去1

		final String _path = path.indexOf('?') < 0 ? new StringBuilder().append(path).append("?scroll=").append(scroll).toString() :
				new StringBuilder().append(path).append("&scroll=").append(scroll).toString();

		ExecutorService executorService = this.client.getSliceScrollQueryExecutorService();
		List<Future> tasks = new ArrayList<Future>();
		//辅助方法，用来累计每次scroll获取到的记录数
		final ParallelSliceScrollResult sliceScrollResult = new ParallelSliceScrollResult();
		if(scrollHandler != null)
			sliceScrollResult.setScrollHandler(scrollHandler);

		try {
//			SliceRunTask<T> sliceRunTask = null;
			SerialContext serialContext = ESInnerHitSerialThreadLocal.buildSerialContext();
			for (int j = 0; j < max; j++) {//启动max个线程，并行处理每个slice任务
				String sliceDsl = sliceScroll.buildSliceDsl(j,max);
//				final String sliceDsl = builder.append("{\"slice\": {\"id\": ").append(i).append(",\"max\": ")
//									.append(max).append("},\"size\":").append(fetchSize).append(",\"query\": {\"match_all\": {}}}").toString();

				runSliceTask(j,_path,sliceDsl,scroll,type,sliceScrollResult,
						executorService,tasks,serialContext );
			}
		}
		finally {
			waitTasksComplete(tasks);
		}

		//打印处理耗时和实际检索到的数据
		if(logger.isDebugEnabled()) {
			long endtime = System.currentTimeMillis();
			logger.debug("Slice scroll query耗时：" + (endtime - starttime) + ",realTotalSize：" + sliceScrollResult.getRealTotalSize());
		}


		sliceScrollResult.complete();
		return sliceScrollResult.getSliceResponse();
	}
	public <T> void runSliceTask(int sliceId,String path,String sliceDsl,  String scroll,  Class<T> type,  ParallelSliceScrollResult sliceScrollResult,ExecutorService executorService,List<Future> tasks,SerialContext serialContext ){
		SliceRunTask<T> sliceRunTask = new SliceRunTask<T>(this,sliceId,path,sliceDsl,scroll,type,sliceScrollResult,   serialContext);
		tasks.add(executorService.submit(sliceRunTask));
	}
	protected void waitTasksComplete(final List<Future> tasks){

		for (Future future : tasks) {
			try {
				future.get();
			} catch (ExecutionException e) {
				logger.error("",e);
			}catch (Exception e) {
				logger.error("",e);
			}
		}

	}

	/**
	 * 并行检索索引所有数据,每批次返回默认为5000条数据，
	 * @param index
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> searchAllParallel(String index,ScrollHandler<T> scrollHandler,  Class<T> type,int thread) throws ElasticSearchException{
		return searchAllParallel(index,  DEFAULT_FETCHSIZE,scrollHandler,type,thread);
	}

	public  <T> void _doSliceScroll(int i,String path,
									String entity,
									String scroll,Class<T> type,

									SliceScrollResultInf<T> sliceScrollResult) throws Exception {
		try{
			RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
			ESDatas<T> sliceResponse = ResultUtil.buildESDatas(result,type);

			List<T> sliceDatas = sliceResponse.getDatas();
			String scrollId = sliceResponse.getScrollId();
//			System.out.println("sliceDatas:"+i+":" + sliceDatas);
//			System.out.println("scrollId:"+i+":" + scrollId);
			List<String> scrollIds = null;
			if (scrollId != null) {
				scrollIds = new ArrayList<String>();
				scrollIds.add(scrollId);
			}

			if (sliceDatas != null && sliceDatas.size() > 0) {//每页100条记录，迭代scrollid，遍历scroll分页结果
				ScrollHandler<T> _scrollHandler = sliceScrollResult.getScrollHandler();
				if (_scrollHandler == null) {
					_scrollHandler = sliceScrollResult.setScrollHandler(sliceResponse);
				}
				else {
					_scrollHandler.handle(sliceResponse);
					sliceScrollResult.setSliceResponse(sliceResponse);
				}
				sliceScrollResult.incrementSize(sliceDatas.size());//统计实际处理的文档数量
				ESDatas<T> _sliceResponse = null;
				List<T> _sliceDatas = null;
				do {
					_sliceResponse = searchScroll(scroll, scrollId, type);
					String sliceScrollId = _sliceResponse.getScrollId();
					if (sliceScrollId != null)
						scrollIds.add(sliceScrollId);
					//处理完毕后清除scroll上下文信息

					_sliceDatas = _sliceResponse.getDatas();
					if (_sliceDatas == null || _sliceDatas.size() == 0) {
						break;
					}
					_scrollHandler.handle(_sliceResponse);
					sliceScrollResult.incrementSize(_sliceDatas.size());//统计实际处理的文档数量
				} while (true);
			}
			//处理完毕后清除scroll上下文信息
			if(scrollIds != null && scrollIds.size() > 0) {
				try {
					deleteScrolls(scrollIds);
				}
				catch (Exception e){

				}
//			System.out.println(scrolls);
			}

		} catch (ElasticSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new ElasticSearchException("slice query task["+i+"] failed:",e);
		}
	}

	/************************************slice searchAll end*****************************/


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

	/**
	 * 一次性返回scroll检索结果
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public  <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Map params,Class<T> type) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}


	/**
	 * scroll检索,每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Map params,Class<T> type,ScrollHandler<T> scrollHandler)
			throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}




	/**
	 * 一次性返回scroll检索结果
	 * @param path like agentstat/_search
	 * @param entity
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scroll(String path,String entity,String scroll ,Class<T> type) throws ElasticSearchException{
		return scroll(path,entity,  scroll , type,(ScrollHandler<T>)null);

	}


	/**
	 * scroll检索,每次检索结果交给scrollHandler回调函数处理
	 * @param path
	 * @param entity
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scroll(String path,String entity,String scroll ,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException{
		try {
			path = path.indexOf('?') < 0 ? new StringBuilder().append(path).append("?scroll=").append(scroll).toString() :
					new StringBuilder().append(path).append("&scroll=").append(scroll).toString();

			RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
			ESDatas<T> response =  ResultUtil.buildESDatas(result,type);
			boolean useDefaultScrollHandler = false;
			if(scrollHandler == null){
				scrollHandler = new DefualtScrollHandler<T>(response);
				useDefaultScrollHandler = true;
			}
			else{
				scrollHandler.handle(response);
			}
			List<T> datas = response.getDatas();//第一页数据

			List<String> scrollIds = null;//用于记录每次scroll的scrollid，便于检索完毕后清除
//			long totalSize = response.getTotalSize();//总记录数
			String scrollId = response.getScrollId();//第一次的scrollid
			if (scrollId != null) {
				scrollIds = new ArrayList<String>();
				scrollIds.add(scrollId);
			}
//		System.out.println("totalSize:"+totalSize);
//		System.out.println("scrollId:"+scrollId);
			if (datas != null && datas.size() > 0) {//每页1000条记录，通过迭代scrollid，遍历scroll分页结果
				ESDatas<T> _response = null;
				List<T> _datas = null;
				do {

					_response = searchScroll(scroll, scrollId, type);
					scrollId = _response.getScrollId();//每页的scrollid
					if (scrollId != null)
						scrollIds.add(scrollId);

					_datas = _response.getDatas();//每页的纪录数
					if (_datas == null || _datas.size() == 0) {
						break;
					} else {
						scrollHandler.handle(_response);
					}

				} while (true);
			}

			//清除scroll上下文信息,虽然说超过1分钟后，scrollid会自动失效，
			//但是手动删除不用的scrollid，是一个好习惯
			if (scrollIds != null && scrollIds.size() > 0) {
				try {
					deleteScrolls(scrollIds);
				}
				catch (Exception e){

				}
			}
			if(!useDefaultScrollHandler)//结果自行处理，所以清空默认结果
				response.setDatas(null);
			return response;
		}
		catch (ElasticSearchException e){
			throw e;
		}
		catch (Exception e){
			throw new ElasticSearchException(e);
		}
	}


	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain id,max varialbe
	 *    <property name="scrollSliceQuery">
	 *         <![CDATA[
	 *          {
	 *            "slice": {
	 *                 "id": #[id],
	 *                 "max": #[max]
	 *             },
	 *             "size":#[size],
	 *             "query": {
	 *                 "term" : {
	 *                     "gc.jvmGcOldCount" : 3
	 *                 }
	 *             }
	 *         }
	 *         ]]>
	 *     </property>
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSlice(String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}
	public <T> ESDatas<T> scrollSliceParallel(String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}

	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain id,max varialbe
	 *    <property name="scrollSliceQuery">
	 *         <![CDATA[
	 *          {
	 *            "slice": {
	 *                 "id": #[id],
	 *                 "max": #[max]
	 *             },
	 *             "size":#[size],
	 *             "query": {
	 *                 "term" : {
	 *                     "gc.jvmGcOldCount" : 3
	 *                 }
	 *             }
	 *         }
	 *         ]]>
	 *     </property>
	 * @param scroll
	 * @param type
	 * @param scrollHandler 每次检索结果会被异步交给handle来处理
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSlice(String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type,
									  ScrollHandler<T> scrollHandler) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}

	public <T> ESDatas<T> scrollSliceParallel(String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type,
									  ScrollHandler<T> scrollHandler) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}

	/**
	 * scroll检索,每次检索结果交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Object params,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}
	/**
	 * 一次性返回scroll检索结果
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Object params,Class<T> type) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}
	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain id,max varialbe
	 *    <property name="scrollSliceQuery">
	 *         <![CDATA[
	 *          {
	 *            "slice": {
	 *                 "id": #[id],
	 *                 "max": #[max]
	 *             },
	 *             "size":#[size],
	 *             "query": {
	 *                 "term" : {
	 *                     "gc.jvmGcOldCount" : 3
	 *                 }
	 *             }
	 *         }
	 *         ]]>
	 *     </property>
	 * @param scroll
	 * @param type
	 * @param scrollHandler 每次检索结果会被异步交给handle来处理
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSlice(String path,final String dslTemplate,final Object params ,int max,
									  final String scroll  ,final Class<T> type,
									  ScrollHandler<T> scrollHandler,boolean parallel) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
	}
	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain id,max varialbe
	 *    <property name="scrollSliceQuery">
	 *         <![CDATA[
	 *          {
	 *            "slice": {
	 *                 "id": #[id],
	 *                 "max": #[max]
	 *             },
	 *             "size":#[size],
	 *             "query": {
	 *                 "term" : {
	 *                     "gc.jvmGcOldCount" : 3
	 *                 }
	 *             }
	 *         }
	 *         ]]>
	 *     </property>
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSlice(String path,final String dslTemplate,final Object params ,int max,
									  final String scroll  ,final Class<T> type,boolean parallel) throws ElasticSearchException{
		throw new java.lang.UnsupportedOperationException();
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

	/****************/

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Map params,Class<T> type,String aggs) throws ElasticSearchException{

		return null;
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Object params,Class<T> type,String aggs) throws ElasticSearchException{
		return null;
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Map params,Class<T> type,String aggs,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException{

		return null;
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Object params,Class<T> type,String aggs,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException{
		return null;
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Class<T> type,String aggs) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( ));
		return ResultUtil.buildESAggDatas(result,type,aggs,null,(ESAggBucketHandle<T>)null);
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Class<T> type,String aggs,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException{
		RestResponse result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( ));
		return ResultUtil.buildESAggDatas(result,type,aggs,null,aggBucketHandle);
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
			 return this.client.executeRequest(path,entity,  new ESMapResponseHandler());
		 }
	
	 public String dropIndice(String index)  throws ElasticSearchException {
		 	try {
				return this.client.executeHttp(index + "?pretty", ClientUtil.HTTP_DELETE);
			}
			catch(ElasticSearchException e){
				return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_dropIndice);
			}
		 
	 }
	 
	 
	 /**
	  * 更新索引定义
	  * @param indexMapping
	  * @return
	  * @throws ElasticSearchException
	  */
	 public String updateIndiceMapping(String action,String indexMapping)  throws ElasticSearchException {
	 	try {
			return this.client.executeHttp(action, indexMapping, ClientUtil.HTTP_POST);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_updateIndiceMapping);
		}
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

	/**
	 * health status index                         uuid                   pri rep docs.count docs.deleted store.size pri.store.size
	 * 获取elasticsearch索引监控数据
	 * @return
	 * @throws ElasticSearchException
	 */
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
				Map<Integer,IndiceHeader> indiceHeaders = null;
		        while(true){
		            String line = br.readLine();
		            if(line == null)
		                break;
		            if(i == 0){
						indiceHeaders = BuildTool.buildIndiceHeaders(line);
		                i ++;
		                continue;
		            }
		            
		            ESIndice esIndice = BuildTool.buildESIndice(  line,  format,indiceHeaders);
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
		try {
			return this.client.executeHttp(path, entity, ClientUtil.HTTP_POST);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_updateDocument);
		}
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
		return updateDocument(index,indexType,id,params,(String)null);
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
		return updateDocument(index,indexType,id,params,(String)null);
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

		return updateDocument(index,indexType,id,params,refreshOption,(Boolean)null,(Boolean)null);
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

		return updateDocument(index,indexType,id,params,refreshOption,(Boolean)null,(Boolean)null);
	}


	/*********************************************************/

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
	public String updateDocument(String index,String indexType,Object id,Object params,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException{
		return updateDocument(index,indexType,id,params,(String)null,  detect_noop,  doc_as_upsert);
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
	public String updateDocument(String index,String indexType,Object id,Map params,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException{
		return updateDocument(index,indexType,id,params,(String)null,  detect_noop,  doc_as_upsert);
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
	public String updateDocument(String index,String indexType,Object id,Map params,String refreshOption,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException{
//		StringBuilder path = new StringBuilder();
//		if(indexType == null || indexType.equals(""))
//			path.append(index).append("/").append(id).append("/_update");
//		else
//			path.append(index).append("/").append(indexType).append("/").append(id).append("/_update");
//		if(refreshOption != null){
//			path.append("?").append(refreshOption);
//		}
//		StringBuilder builder = new StringBuilder();
//		builder.append(" {\"doc\":");
//		Writer writer = new BBossStringWriter(builder);
//		SerialUtil.object2json(params,writer);
//		if(detect_noop != null){
//			builder.append(",\"detect_noop\":").append(detect_noop);
//		}
//		if(doc_as_upsert != null){
//			builder.append(",\"doc_as_upsert\":").append(doc_as_upsert);
//		}
//		builder.append("}");
//		try {
//			String searchResult = this.client.executeHttp(path.toString(), builder.toString(), ClientUtil.HTTP_POST);
//
//			return searchResult;
//		}
//		catch(ElasticSearchException e){
//			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_updateDocument);
//		}
		return _update(index,indexType,
				id, params, refreshOption, detect_noop, doc_as_upsert);
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
	 * @param detect_noop default null
	 * @param doc_as_upsert default null
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
	public String updateDocument(String index,String indexType,
								 Object id,Object params,String refreshOption,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException{
		return _update(index,indexType,
				 id, params, refreshOption, detect_noop, doc_as_upsert);
	}

	private String _update(String index,String indexType,
						   Object id,Object params,String refreshOption,Object detect_noop,Object doc_as_upsert){
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
		if(detect_noop != null){
			builder.append(",\"detect_noop\":").append(detect_noop);
		}
		if(doc_as_upsert != null){
			builder.append(",\"doc_as_upsert\":").append(doc_as_upsert);
		}
		builder.append("}");
		try {
			String searchResult = this.client.executeHttp(path.toString(), builder.toString(), ClientUtil.HTTP_POST);

			return searchResult;
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_updateDocument);
		}
	}

	public String updateDocument(String index,String indexType,Object params,UpdateOptions updateOptions) throws ElasticSearchException{
		Object id = null;
		String refreshOption = null;
		Object detect_noop = null;
		Object doc_as_upsert = null;

		if(updateOptions != null) {
			refreshOption = updateOptions.getRefreshOption();
			if(!(params instanceof Map)) {
				ClassUtil.ClassInfo beanClassInfo = ClassUtil.getClassInfo(params.getClass());

				id = updateOptions.getDocIdField() != null ? BuildTool.getId(params, beanClassInfo, updateOptions.getDocIdField()) : null;
				detect_noop = updateOptions.getDetectNoopField() != null ? BuildTool.getParentId(params, beanClassInfo, updateOptions.getDetectNoopField()) : null;
				doc_as_upsert = updateOptions.getDocasupsertField() != null ? BuildTool.getRouting(params, beanClassInfo, updateOptions.getDocasupsertField()) : null;
			}
			else{
				Map _params = (Map)params;
				id = updateOptions.getDocIdField() != null ? _params.get(updateOptions.getDocIdField()) : null;
				detect_noop = updateOptions.getDetectNoopField() != null ? _params.get( updateOptions.getDetectNoopField()) : null;
				doc_as_upsert = updateOptions.getDocasupsertField() != null ? _params.get(updateOptions.getDocasupsertField()) : null;
			}

		}
		return this._update(  index,  indexType,
				  id,  params,  refreshOption,  detect_noop,  doc_as_upsert);
	}




	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param sourceIndice
	 * @param destIndice
	 * @return
	 */
	public String reindex(String sourceIndice,String destIndice){
		String reindex = new StringBuilder().append("{\"source\": {\"index\": \"").append(sourceIndice).append("\"},\"dest\": {\"index\": \"").append(destIndice).append("\"}}").toString();
		return this.client.executeHttp("_reindex",reindex,ClientUtil.HTTP_POST);

	}

	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param sourceIndice
	 * @param destIndice
	 * @return
	 */
	public String reindex(String sourceIndice,String destIndice,String versionType){
		String reindex = new StringBuilder().append("{\"source\": {\"index\": \"").append(sourceIndice).append("\"},\"dest\": {\"index\": \"").append(destIndice).append("\",\"version_type\": \"").append(versionType).append("\"}}").toString();
		return this.client.executeHttp("_reindex",reindex,ClientUtil.HTTP_POST);
	}

	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param sourceIndice
	 * @param destIndice
	 * @return
	 */
	public String reindex(String sourceIndice,String destIndice,String opType,String conflicts){
		if(conflicts == null || conflicts.equals("")) {
			String reindex = new StringBuilder().append("{\"source\": {\"index\": \"").append(sourceIndice).append("\"},\"dest\": {\"index\": \"").append(destIndice).append("\",\"op_type\": \"").append(opType).append("\"}}").toString();
			return this.client.executeHttp("_reindex", reindex, ClientUtil.HTTP_POST);
		}
		else
		{
			String reindex = new StringBuilder().append("{\"conflicts\": \"").append(conflicts).append("\",\"source\": {\"index\": \"").append(sourceIndice).append("\"},\"dest\": {\"index\": \"").append(destIndice).append("\",\"op_type\": \"").append(opType).append("\"}}").toString();
			return this.client.executeHttp("_reindex", reindex, ClientUtil.HTTP_POST);
		}
	}

	/**
	 * Associating the alias alias with index indice
	 * more detail see :
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html
	 * @param indice
	 * @param alias
	 * @return
	 */
	public String addAlias(String indice,String alias){
		String aliasJson = new StringBuilder().append("{\"actions\": [{\"add\": {\"index\":\"").append(indice).append("\",\"alias\": \"").append(alias).append("\"}}]}").toString();
		return this.client.executeHttp("_aliases",aliasJson,ClientUtil.HTTP_POST);
	}

	/**
	 * removing that same alias [alias] of [indice]
	 * more detail see :
	 * 	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html
	 * @param indice
	 * @param alias
	 * @return
	 */
	public String removeAlias(String indice,String alias){
		String removeAlias = new StringBuilder().append("{\"actions\": [{\"remove\": {\"index\":\"").append(indice).append("\",\"alias\": \"").append(alias).append("\"}}]}").toString();
		return this.client.executeHttp("_aliases",removeAlias,ClientUtil.HTTP_POST);
	}

	public ESInfo getESInfo(String templateName){
		return  null;
	}

	@Override
	public String closeIndex(String index) {
		String closeIndex = new StringBuilder().append("/").append(index).append("/_close").toString();
		return this.client.executeHttp(closeIndex,ClientUtil.HTTP_POST);
	}

	@Override
	public String openIndex(String index) {
		String closeIndex = new StringBuilder().append("/").append(index).append("/_open").toString();
		return this.client.executeHttp(closeIndex,ClientUtil.HTTP_POST);
	}

	/**
	 * 指定对象集合的文档id字段
	 */
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Field
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentsWithIdOptions(String indexName, String indexType, List<Object> beans,String docIdField,String refreshOption) throws ElasticSearchException{
		return addDocumentsWithIdField(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans,docIdField,refreshOption);
	}
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField) throws ElasticSearchException{
		return addDocumentsWithIdField(  this.indexNameBuilder.getIndexName(indexName),   indexType,    beans,  docIdField);
	}
	public String addDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String refreshOption) throws ElasticSearchException{
//		if(beans == null || beans.size() == 0)
//			return null;
//		StringBuilder builder = new StringBuilder();
//		BBossStringWriter writer = new BBossStringWriter(builder);
//		for(Object bean:beans) {
//			try {
//				BuildTool.evalBuilk(writer,indexName,indexType,bean,"index",docIdField,(String)null);
//			} catch (IOException e) {
//				throw new ElasticSearchException(e);
//			}
//		}
//		writer.flush();
//		if(refreshOption == null)
//			return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
//		else
//			return this.client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
		return addDocumentsWithIdField(  indexName,   indexType,  beans,  docIdField,(String )null,  refreshOption);
	}
	public String addDocumentsWithIdField(String indexName, String indexType,  List<Object> beans,String docIdField) throws ElasticSearchException{
		return addDocumentsWithIdField(  indexName,   indexType,   beans, docIdField,(String )null,(String )null);
	}


	/**********************/
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField,String refreshOption) throws ElasticSearchException{
		return addDocumentsWithIdField(this.indexNameBuilder.getIndexName(indexName),   indexType,   beans,  docIdField,  parentIdField,  refreshOption);
	}
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField) throws ElasticSearchException
	{
		return addDocumentsWithIdParentField(this.indexNameBuilder.getIndexName(indexName),   indexType,     beans,docIdField,parentIdField);
	}

	public String addDateDocuments(String indexName, String indexType, List<?> beans,ClientOptions clientOptions) throws ElasticSearchException{
		return addDocuments(  this.indexNameBuilder.getIndexName(indexName),   indexType,  beans,  clientOptions);
	}
	public   String addDocuments(String indexName, String indexType, List<?> beans,ClientOptions clientOptions) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Object bean:beans) {
			try {
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"index",clientOptions);
			} catch (IOException e) {
				throw new ElasticSearchException(e);
			}
		}
		writer.flush();
		if(clientOptions == null || clientOptions.getRefreshOption() == null)
			return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		else
			return this.client.executeHttp("_bulk?"+clientOptions.getRefreshOption(),builder.toString(),ClientUtil.HTTP_POST);
	}
	public  String addDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField,String refreshOption) throws ElasticSearchException{
		if(beans == null || beans.size() == 0)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		for(Object bean:beans) {
			try {
				BuildTool.evalBuilk(writer,indexName,indexType,bean,"index",docIdField,parentIdField);
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
	public   String addDocumentsWithIdParentField(String indexName, String indexType,  List<Object> beans,String docIdField,String parentIdField) throws ElasticSearchException{
		return addDocumentsWithIdField(indexName, indexType, beans,docIdField, parentIdField,(String)null);
	}


}
