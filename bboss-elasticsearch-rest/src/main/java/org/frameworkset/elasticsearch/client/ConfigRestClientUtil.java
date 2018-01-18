package org.frameworkset.elasticsearch.client;

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.handler.ESAggBucketHandle;
import org.frameworkset.elasticsearch.handler.ElasticSearchResponseHandler;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.elasticsearch.template.ESTemplateHelper;
import org.frameworkset.elasticsearch.template.ESUtil;
import org.frameworkset.spi.remote.http.MapResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 通过配置文件加载模板
 */
public class ConfigRestClientUtil extends RestClientUtil {
	private static Logger logger = LoggerFactory.getLogger(ConfigRestClientUtil.class);
	//	public String to_char(String date,String format)
//    {
//    	 SimpleDateFormat f = new SimpleDateFormat(format);
//    	 return f.format(SimpleStringUtil.stringToDate(date,format));
//
//    }
//
//    public String to_char(String date)
//    {
//    	return to_char(date,this.FORMART_ALL);
//
//    }
	private static String java_date_format = "yyyy-MM-dd HH:mm:ss";
	private String configFile;
	private ESUtil esUtil = null;

	public ConfigRestClientUtil(ElasticSearchClient client, IndexNameBuilder indexNameBuilder, String configFile) {
		super(client, indexNameBuilder);
		this.configFile = configFile;
		this.esUtil = ESUtil.getInstance(configFile);
	}

 

	 

	/**
	 * @param path
	 * @return
	 */
	public Object executeRequest(String path) throws ElasticSearchException {
		return executeRequest(path, null);
	}

	public Object execute() throws ElasticSearchException {
		return client.execute(this.bulkBuilder.toString());
	}

	 

	





	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	@Override
	public String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException {
		return addDocuments(  indexName,   indexType,  addTemplate,   beans,  null);

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
	public String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		for(Object bean:beans) {
			ESTemplateHelper.evalBuilkTemplate(esUtil,builder,indexName,indexType,addTemplate,bean,"index");
		}
		if(refreshOption == null)
			return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		else
			return this.client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
	}
	
	
	

	/**
	 * 批量更新索引，对于按时间分区存储的索引，需要应用程序自行处理带日期时间的索引名称
	 * @param indexName
	 * @param indexType
	 * @param updateTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		for(Object bean:beans) {
			ESTemplateHelper.evalBuilkTemplate(esUtil,builder,indexName,indexType,updateTemplate,bean,"update");
		}
		return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
	}
	public String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		for(Object bean:beans) {
			ESTemplateHelper.evalBuilkTemplate(esUtil,builder,indexName,indexType,updateTemplate,bean,"update");
		}
		return this.client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
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
		return addDocuments(  this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate, beans);
	}
	
	public String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		return addDocuments(  this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate, beans,refreshOption);
	}

	/**
	 * 创建索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException{
		return addDocument(  indexName,   indexType,  addTemplate,   bean,null);
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
		builder.setLength(0);
		path = this.client.executeHttp(path,ESTemplateHelper.evalDocumentTemplate(esUtil,builder,indexType,indexName,addTemplate,bean,"create"),ClientUtil.HTTP_POST);
		builder = null;
		return path;
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
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate,   bean);
	}

	public String addDateDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate,   bean,refreshOption);
	}


	@Override
	public String executeRequest(String path, String templateName) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Map) null));
	}

	@Override
	public String executeRequest(String path, String templateName, Map params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
	}

	@Override
	public String executeRequest(String path, String templateName, Object params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
	}

	public <T> T executeRequest(String path, String templateName, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeRequest(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, params),   responseHandler);
//		return this.client.executeRequest(path, evalTemplate(templateName, params), responseHandler);
	}


	public <T> T executeRequest(String path, String templateName, Object params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeRequest(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), responseHandler);
//		return this.client.executeRequest(path, evalTemplate(templateName, params), responseHandler);
	}

	@Override
	public String delete(String path, String string) {
		// TODO Auto-generated method stub
		return super.delete(path, string);
	}

	@Override
	public String executeHttp(String path, String templateName, String action) throws ElasticSearchException {
		// TODO Auto-generated method stub
//		return this.client.executeHttp(path, evalTemplate(templateName, (Object) null),action);
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null),   action);
	}


	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param templateName
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  executeHttp(String path, String templateName,String action,Map params,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, params),   action,responseHandler);
	}

	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param templateName 请求报文
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public String executeHttp(String path, String templateName,Map params, String action) throws ElasticSearchException{
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, params),   action);
	}
	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param templateName
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  executeHttp(String path, String templateName,String action,Object bean,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, bean),   action,responseHandler);
	}

	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param templateName 请求报文
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public String executeHttp(String path, String templateName,Object bean, String action) throws ElasticSearchException{
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, bean),   action);
	}


	@Override
	public <T> T executeRequest(String path, String templateName, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		// TODO Auto-generated method stub
//		return this.client.executeRequest(path, evalTemplate(templateName, (Object) null), responseHandler);
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), responseHandler);
	}

	@Override
	public MapRestResponse search(String path, String templateName, Map params) throws ElasticSearchException {
//		return super.executeRequest(path, evalTemplate(templateName, params), new ElasticSearchResponseHandler());
		return super.search(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
	}

	@Override
	public MapRestResponse search(String path, String templateName, Object params) throws ElasticSearchException {
//		return super.executeRequest(path, evalTemplate(templateName, params), new ElasticSearchResponseHandler());
		return super.search(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
	}

	@Override
	public MapRestResponse search(String path, String templateName) throws ElasticSearchException {
		return super.search(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null));
//		return super.executeRequest(path, evalTemplate(templateName, (Object) null), new ElasticSearchResponseHandler());
	}

	@Override
	public RestResponse search(String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
//		return super.executeRequest(path, evalTemplate(templateName, params), new ElasticSearchResponseHandler(type));
		return super.search(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), type);
	}


	@Override
	public RestResponse search(String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
//		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
		return super.search(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), type);
	}

	@Override
	public RestResponse search(String path, String templateName, Class<?> type) throws ElasticSearchException {
//		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
		return super.search(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object)null), type);
	}


	public <T> ESDatas<T> searchList(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, this.evalTemplate(templateName, params), new ElasticSearchResponseHandler(type));
//		return buildESDatas(result, type);
		return super.searchList(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),type);
	}

	public <T> ESDatas<T> searchList(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
//		return buildESDatas(result, type);
		return super.searchList(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),type);
	}

	public <T> ESDatas<T> searchList(String path, String templateName, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
//		return buildESDatas(result, type);
		return super.searchList(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, (Map)null),type);
	}


	public <T> T searchObject(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
//		return buildObject(result, type);
		return super.searchObject(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),type);
	}

	public <T> T searchObject(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		return super.searchObject(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),type);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
//		return buildObject(result, type);
	}

	public <T> T searchObject(String path, String templateName, Class<T> type) throws ElasticSearchException {
		return super.searchObject(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null),type);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
//		return buildObject(result, type);

	}


	@Override
	public RestResponse search(String path, String templateName, Map params, ESTypeReferences type) throws ElasticSearchException {
		return super.search(  path,ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type);
	}

	@Override
	public RestResponse search(String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException {
		return super.search(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type);
//		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
	}

	@Override
	public RestResponse search(String path, String templateName, ESTypeReferences type) throws ElasticSearchException {
		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
	}


	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName) throws ElasticSearchException {
		return super.updateIndiceMapping(action, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null));
	}

	/**
	 * 创建索引定义
	 * curl -XPUT 'localhost:9200/test?pretty' -H 'Content-Type: application/json' -d'
	 * {
	 * "settings" : {
	 * "number_of_shards" : 1
	 * },
	 * "mappings" : {
	 * "type1" : {
	 * "properties" : {
	 * "field1" : { "type" : "text" }
	 * }
	 * }
	 * }
	 * }
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String createIndiceMapping(String indexName, String templateName) throws ElasticSearchException {
		return super.createIndiceMapping(indexName, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null));
	}


	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName, Object parameter) throws ElasticSearchException {
		return super.updateIndiceMapping(action, ESTemplateHelper.evalTemplate(esUtil,templateName, parameter));
	}

	/**
	 * 创建索引定义
	 * curl -XPUT 'localhost:9200/test?pretty' -H 'Content-Type: application/json' -d'
	 * {
	 * "settings" : {
	 * "number_of_shards" : 1
	 * },
	 * "mappings" : {
	 * "type1" : {
	 * "properties" : {
	 * "field1" : { "type" : "text" }
	 * }
	 * }
	 * }
	 * }
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String createIndiceMapping(String indexName, String templateName, Object parameter) throws ElasticSearchException {
		return super.createIndiceMapping(indexName, ESTemplateHelper.evalTemplate(esUtil,templateName, parameter));
	}

	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName, Map parameter) throws ElasticSearchException {
		return super.updateIndiceMapping(action, ESTemplateHelper.evalTemplate(esUtil,templateName, parameter));
	}

	/**
	 * 创建索引定义
	 * curl -XPUT 'localhost:9200/test?pretty' -H 'Content-Type: application/json' -d'
	 * {
	 * "settings" : {
	 * "number_of_shards" : 1
	 * },
	 * "mappings" : {
	 * "type1" : {
	 * "properties" : {
	 * "field1" : { "type" : "text" }
	 * }
	 * }
	 * }
	 * }
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String createIndiceMapping(String indexName, String templateName, Map parameter) throws ElasticSearchException {
		return super.createIndiceMapping(indexName, ESTemplateHelper.evalTemplate(esUtil,templateName, parameter));
	}

	public Map<String, Object> searchMap(String path, String templateName, Map params) throws ElasticSearchException {
		return super.searchMap(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
//		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new MapResponseHandler());
	}


	@SuppressWarnings("unchecked")
	public Map<String, Object> searchMap(String path, String templateName, Object params) throws ElasticSearchException {
		return super.searchMap(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
//		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new MapResponseHandler());
	}

	/**
	 * @param path
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> searchMap(String path, String templateName) throws ElasticSearchException {
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new MapResponseHandler());
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Map params, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type,   aggs,   stats);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Object params, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params), type, aggs, stats);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAgg(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null),   type,   aggs,   stats);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Map params, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type,   aggs,   stats, aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Object params, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params), type, aggs, stats,  aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null),   type,   aggs,   stats, aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}


	@Override
	public String createTempate(String template, String templateName) throws ElasticSearchException {
		return super.createTempate(template,ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)null));
	}

	@Override
	public String createTempate(String template, String templateName,Object params) throws ElasticSearchException {
		return super.createTempate(template,ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)params));
	}

	@Override
	public String createTempate(String template, String templateName,Map params) throws ElasticSearchException {
		return super.createTempate(template,ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)params));
	}

}
