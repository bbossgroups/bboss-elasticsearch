package org.frameworkset.elasticsearch.client;

import org.apache.http.client.ResponseHandler;
import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.ElasticSearchEventSerializer;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.event.Event;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;

import java.util.List;
import java.util.Map;

public interface ClientUtil {
	public final String HTTP_GET = "get";
	public final String HTTP_POST = "post";
	public final String HTTP_DELETE = "delete";
	public final String HTTP_PUT = "put";

	public void deleteIndex(String indexName, String indexType, String... ids) throws ElasticSearchException;

	public void addEvent(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException;

	public void updateIndexs(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException;

	public Object execute() throws Exception;

	public Client getClient();

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public Object executeRequest(String path, String entity) throws ElasticSearchException;


	/**
	 * @param path
	 * @return
	 */
	public Object executeRequest(String path) throws ElasticSearchException;

	public String executeHttp(String path, String action) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public String executeHttp(String path, String entity, String action) throws ElasticSearchException;

	/**
	 * @param path
	 * @param string
	 * @return
	 */
	public String delete(String path, String string);

	public String getIndexMapping(String index) throws ElasticSearchException;

	public String executeRequest(String path, String templateName, Map params) throws ElasticSearchException;


	public String executeRequest(String path, String templateName, Object params) throws ElasticSearchException;


	public <T> T executeRequest(String path, String entity, ResponseHandler<T> responseHandler) throws ElasticSearchException;

	public <T> T executeRequest(String path, String templateName, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException;


	public <T> T executeRequest(String path, String templateName, Object params, ResponseHandler<T> responseHandler) throws ElasticSearchException;

	public SearchResult search(String path, String templateName, Map params) throws ElasticSearchException;


	public SearchResult search(String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public SearchResult search(String path, String entity) throws ElasticSearchException;


	public Map<String, Object> searchMap(String path, String templateName, Map params) throws ElasticSearchException;


	public Map<String, Object> searchMap(String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public Map<String, Object> searchMap(String path, String entity) throws ElasticSearchException;


	/**
	 * 获取索引定义
	 *
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public String getIndice(String index) throws ElasticSearchException;

	/**
	 * 删除索引定义
	 *
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public String dropIndice(String index) throws ElasticSearchException;

	/**
	 * 更新索引定义
	 *
	 * @param indexMapping
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String indexMapping) throws ElasticSearchException;

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
	public String createIndiceMapping(String indexName, String indexMapping) throws ElasticSearchException;


	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName, Object parameter) throws ElasticSearchException;

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
	public String createIndiceMapping(String indexName, String templateName, Object parameter) throws ElasticSearchException;

	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName, Map parameter) throws ElasticSearchException;

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
	public String createIndiceMapping(String indexName, String templateName, Map parameter) throws ElasticSearchException;

	public List<ESIndice> getIndexes() throws ElasticSearchException;

	public String refreshIndexInterval(String indexName, int interval) throws ElasticSearchException;

	public String refreshIndexInterval(String indexName, String indexType, int interval) throws ElasticSearchException;

	public String refreshIndexInterval(int interval, boolean preserveExisting) throws ElasticSearchException;

	public String refreshIndexInterval(int interval) throws ElasticSearchException;

	public SearchResult search(String path, String templateName, Map params, Class<?> type) throws ElasticSearchException;

	public SearchResult search(String path, String templateName, Object params, Class<?> type) throws ElasticSearchException;

	public SearchResult search(String path, String entity, Class<?> type) throws ElasticSearchException;


	public SearchResult search(String path, String templateName, Map params, ESTypeReferences type) throws ElasticSearchException;

	public SearchResult search(String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException;

	public SearchResult search(String path, String entity, ESTypeReferences type) throws ElasticSearchException;

	public <T> ESDatas<T> searchList(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException;

	public <T> ESDatas<T> searchList(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException;

	public <T> ESDatas<T> searchList(String path, String entity, Class<T> type) throws ElasticSearchException;

	public <T> T searchObject(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException;

	public <T> T searchObject(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException;

	public <T> T searchObject(String path, String entity, Class<T> type) throws ElasticSearchException;

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
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Map params, Class<T> type, String aggs, String stats) throws ElasticSearchException;

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Object params, Class<T> type, String aggs, String stats) throws ElasticSearchException;

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String entity, Class<T> type, String aggs, String stats) throws ElasticSearchException;
	public String createTempate(String template,String entity)  throws ElasticSearchException;

	public String createTempate(String template, String templateName,Object params) throws ElasticSearchException ;

	public String createTempate(String template, String templateName,Map params) throws ElasticSearchException ;

}
