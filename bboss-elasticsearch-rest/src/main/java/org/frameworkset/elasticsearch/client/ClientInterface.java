package org.frameworkset.elasticsearch.client;

import org.apache.http.client.ResponseHandler;

import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.handler.ESAggBucketHandle;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;

import java.util.List;
import java.util.Map;

public interface ClientInterface {
	public final String HTTP_GET = "get";
	public final String HTTP_POST = "post";
	public final String HTTP_DELETE = "delete";
	public final String HTTP_PUT = "put";
	public final String HTTP_HEAD = "head";

	public abstract String deleteDocuments(String indexName, String indexType, String... ids) throws ElasticSearchException;
	public abstract String deleteDocumentsWithrefreshOption(String indexName, String indexType, String refreshOption,String... ids) throws ElasticSearchException;
	public <T> T getIndexMapping(String index,ResponseHandler<T> responseHandler) throws ElasticSearchException;
	public <T> T getIndexMapping(String index,boolean pretty,ResponseHandler<T> responseHandler) throws ElasticSearchException;

	/**
	 * 获取索引表
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public List<IndexField> getIndexMappingFields(String index,String indexType) throws ElasticSearchException;

	/***************************读取模板文件添加或者修改文档开始************************************/
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
	public abstract String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException;
	public abstract String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException;

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
	public abstract String addDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException;

	public abstract String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans) throws ElasticSearchException;
	public abstract String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans,String refreshOption) throws ElasticSearchException;

	/***************************读取模板文件添加或者修改文档结束************************************/

	/***************************添加或者修改文档开始************************************/
	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
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
	public abstract String addDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
	public abstract String addDocuments(String indexName, String indexType,  List<?> beans) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocument(String indexName, String indexType, Object bean) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param refreshOption
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocument(String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException;

	public abstract String updateDocuments(String indexName, String indexType, List<?> beans) throws ElasticSearchException;
	public abstract String updateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;

	/***************************添加或者修改文档结束************************************/
	/**
	 * 获取json格式文档
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocument(String indexName, String indexType,String documentId) throws ElasticSearchException;
	/**
	 * 获取json格式文档，通过options设置获取文档的参数
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocument(String indexName, String indexType,String documentId,Map<String,Object> options) throws ElasticSearchException;

	/**
	 * 获取文档,返回类型可以继承ESBaseData(这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocument(String indexName, String indexType,String documentId,Class<T> beanType) throws ElasticSearchException;

	/**
	 * 获取文档，通过options设置获取文档的参数，返回类型可以继承ESBaseData(这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocument(String indexName, String indexType,String documentId,Map<String,Object> options,Class<T> beanType) throws ElasticSearchException;



	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract MapSearchHit getDocumentHit(String indexName, String indexType,String documentId,Map<String,Object> options) throws ElasticSearchException;

	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract MapSearchHit getDocumentHit(String indexName, String indexType,String documentId) throws ElasticSearchException;

	/**************************************创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocument(String indexName, String indexType, Object bean) throws ElasticSearchException;

	public abstract String addDateDocument(String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocuments(String indexName, String indexType, List<?> beans) throws ElasticSearchException;

	public abstract String addDateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;

	/**************************************创建或者修改文档结束**************************************************************/


	/**************************************基于query dsl配置文件脚本创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException;

	public abstract String addDateDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException;

	public abstract String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException;

	/**************************************基于query dsl配置文件脚本创建或者修改文档结束**************************************************************/

	public abstract String deleteDocument(String indexName, String indexType, String id) throws ElasticSearchException;
	public abstract String deleteDocument(String indexName, String indexType, String id,String refreshOption) throws ElasticSearchException;
	 
	 

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract Object executeRequest(String path, String entity) throws ElasticSearchException;


	/**
	 * @param path
	 * @return
	 */
	public abstract Object executeRequest(String path) throws ElasticSearchException;

	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param action
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String executeHttp(String path, String action) throws ElasticSearchException;

	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T executeHttp(String path, String action,ResponseHandler<T> responseHandler) throws ElasticSearchException ;

	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param entity
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T  executeHttp(String path, String entity,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException ;

		/**
		 * 发送es restful请求，获取String类型json报文
		 * @param path
		 * @param entity 请求报文
		 * @param action get,post,put,delete
		 * @return
		 * @throws ElasticSearchException
		 */
	public abstract String executeHttp(String path, String entity, String action) throws ElasticSearchException;

	/**
	 * @param path
	 * @param string
	 * @return
	 */
	public abstract String delete(String path, String string);

	public abstract String getIndexMapping(String index) throws ElasticSearchException;

	public abstract String getIndexMapping(String index,boolean pretty) throws ElasticSearchException;

	public abstract String executeRequest(String path, String templateName, Map params) throws ElasticSearchException;


	public abstract String executeRequest(String path, String templateName, Object params) throws ElasticSearchException;


	public abstract <T> T executeRequest(String path, String entity, ResponseHandler<T> responseHandler) throws ElasticSearchException;

	public abstract <T> T executeRequest(String path, String templateName, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException;


	public abstract <T> T executeRequest(String path, String templateName, Object params, ResponseHandler<T> responseHandler) throws ElasticSearchException;

	public abstract MapRestResponse search(String path, String templateName, Map params) throws ElasticSearchException;


	public abstract MapRestResponse search(String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract MapRestResponse search(String path, String entity) throws ElasticSearchException;


	public abstract Map<String, Object> searchMap(String path, String templateName, Map params) throws ElasticSearchException;


	public abstract Map<String, Object> searchMap(String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract Map<String, Object> searchMap(String path, String entity) throws ElasticSearchException;


	/**
	 * 获取索引定义
	 *
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getIndice(String index) throws ElasticSearchException;

	/**
	 * 删除索引定义
	 *
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String dropIndice(String index) throws ElasticSearchException;

	/**
	 * 更新索引定义
	 *
	 * @param indexMapping
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateIndiceMapping(String action, String indexMapping) throws ElasticSearchException;

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
	 * @param indexMapping
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String createIndiceMapping(String indexName, String indexMapping) throws ElasticSearchException;


	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateIndiceMapping(String action, String templateName, Object parameter) throws ElasticSearchException;

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
	public abstract String createIndiceMapping(String indexName, String templateName, Object parameter) throws ElasticSearchException;

	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateIndiceMapping(String action, String templateName, Map parameter) throws ElasticSearchException;

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
	public abstract String createIndiceMapping(String indexName, String templateName, Map parameter) throws ElasticSearchException;

	public abstract List<ESIndice> getIndexes() throws ElasticSearchException;

	public abstract String refreshIndexInterval(String indexName, int interval) throws ElasticSearchException;

	public abstract String refreshIndexInterval(String indexName, String indexType, int interval) throws ElasticSearchException;

	public abstract String refreshIndexInterval(int interval, boolean preserveExisting) throws ElasticSearchException;

	public abstract String refreshIndexInterval(int interval) throws ElasticSearchException;

	public abstract RestResponse search(String path, String templateName, Map params, Class<?> type) throws ElasticSearchException;

	public abstract RestResponse search(String path, String templateName, Object params, Class<?> type) throws ElasticSearchException;

	public abstract RestResponse search(String path, String entity, Class<?> type) throws ElasticSearchException;


	public abstract RestResponse search(String path, String templateName, Map params, ESTypeReferences type) throws ElasticSearchException;

	public abstract RestResponse search(String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException;

	public abstract RestResponse search(String path, String entity, ESTypeReferences type) throws ElasticSearchException;

	public abstract <T> ESDatas<T> searchList(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException;

	public abstract <T> ESDatas<T> searchList(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException;

	public abstract <T> ESDatas<T> searchList(String path, String entity, Class<T> type) throws ElasticSearchException;

	public abstract <T> T searchObject(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException;

	public abstract <T> T searchObject(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException;

	public abstract <T> T searchObject(String path, String entity, Class<T> type) throws ElasticSearchException;

	/**
	 * 聚合查询方法
	 *
	 * @param path   es查询请求相对地址
	 * @param entity es聚合查询模板
	 * @param params es聚合查询参数
	 * @param type   es聚合查询结果类
	 * @param aggs   es聚合查询结果名称
	 * @param stats  es聚合查询统计结果名称
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Map params, Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Object params, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Map params, Class<T> type, String aggs, String stats) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Object params, Class<T> type, String aggs, String stats) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Class<T> type, String aggs, String stats) throws ElasticSearchException;
	public abstract String createTempate(String template,String entity)  throws ElasticSearchException;

	public abstract String createTempate(String template, String templateName,Object params) throws ElasticSearchException ;
	/**
	 * 删除模板
	 * @param template
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String deleteTempate(String template) throws ElasticSearchException ;
	public abstract String createTempate(String template, String templateName,Map params) throws ElasticSearchException ;
	/**
	 * 查询模板
	 * @param template
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getTempate(String template) throws ElasticSearchException ;
	
	/**
	 * 查询所有模板
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getTempate() throws ElasticSearchException ;

}
