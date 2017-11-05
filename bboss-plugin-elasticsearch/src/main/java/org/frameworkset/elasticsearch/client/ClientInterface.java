package org.frameworkset.elasticsearch.client;

import org.apache.http.client.ResponseHandler;
import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.ElasticSearchEventSerializer;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.event.Event;
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
	public <T> T getIndexMapping(String index,ResponseHandler<T> responseHandler) throws ElasticSearchException;
	public <T> T getIndexMapping(String index,boolean pretty,ResponseHandler<T> responseHandler) throws ElasticSearchException;

	/**
	 * 获取索引表
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public List<IndexField> getIndexMappingFields(String index,String indexType) throws ElasticSearchException;
	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException;
	public abstract String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 * 创建索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException;
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


	public abstract String deleteDocument(String indexName, String indexType, String id) throws ElasticSearchException;

	public abstract void addEvent(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException;

	public abstract void updateIndexs(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException;

	public abstract Object execute() throws Exception;

	public abstract Client getClient();

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

	public abstract String executeHttp(String path, String action) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
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

	public abstract RestResponse search(String path, String templateName, Map params) throws ElasticSearchException;


	public abstract RestResponse search(String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract RestResponse search(String path, String entity) throws ElasticSearchException;


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

	public abstract String createTempate(String template, String templateName,Map params) throws ElasticSearchException ;


}
