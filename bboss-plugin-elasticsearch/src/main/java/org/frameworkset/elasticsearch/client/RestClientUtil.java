package org.frameworkset.elasticsearch.client;

import com.frameworkset.util.SimpleStringUtil;
import org.apache.http.client.ResponseHandler;
import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.ElasticSearchEventSerializer;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.event.Event;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.spi.remote.http.MapResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class RestClientUtil implements ClientUtil{
	private static Logger logger = LoggerFactory.getLogger(RestClientUtil.class);
	protected ElasticSearchRestClient client;
	protected StringBuilder bulkBuilder;
	protected IndexNameBuilder indexNameBuilder;
	public RestClientUtil(ElasticSearchClient client,IndexNameBuilder indexNameBuilder) {
		this.client = (ElasticSearchRestClient)client;
		this.indexNameBuilder = indexNameBuilder;
	}
	public   void addEvent(Event event,ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException {
	    if (bulkBuilder == null) {
	    	 bulkBuilder = new StringBuilder();
	    }
	    client.createIndexRequest(bulkBuilder, indexNameBuilder, event,  elasticSearchEventSerializer);
	     
	  }

	@Override
	public void updateIndexs(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException {

	}
	public String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans) throws ElasticSearchException{
		return null;
	}
	@Override
	public String executeRequest(String path, String templateName,Map params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
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
	public Object execute() throws ElasticSearchException {
		  return client.execute(this.bulkBuilder.toString());
	  }

	@Override
	public Client getClient() {
		return null;
	}

	@Override
	public String deleteDocuments(String indexName, String indexType, String... ids) throws ElasticSearchException {
		StringBuilder builder = new StringBuilder();
		for(String id:ids) {
			builder.append("{ \"delete\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : \"").append(id).append("\" } }\n");
		}
		return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		
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
	 * 获取文档
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
	 * 获取文档
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
		SearchResult searchResult = this.client.executeRequest(buildGetDocumentRequest(  indexName,   indexType,  documentId,  options),null,   new GetDocumentResponseHandler( beanType),ClientUtil.HTTP_GET);

		return buildObject(searchResult, beanType);

	}




	@Override
	public String deleteDocument(String indexName, String indexType, String id) throws ElasticSearchException {

		return this.client.executeHttp(new StringBuilder().append(indexName).append("/").append(indexType).append("/").append(id).toString(),ClientUtil.HTTP_DELETE);
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
	
	public String getIndexMapping(String index) throws ElasticSearchException{
		return this.client.executeHttp(index+"/_mapping?pretty",ClientUtil.HTTP_GET);
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
	public SearchResult search(String path, String templateName, Map params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SearchResult search(String path, String templateName, Object params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SearchResult search(String path, String entity) throws ElasticSearchException {
		SearchResult searchResult = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler(  ));
		if(searchResult instanceof ErrorResponse){
			throw new ElasticSearchException(SimpleStringUtil.object2json(searchResult));
		}
		return searchResult;
	}

	@Override
	public SearchResult search(String path, String templateName, Map params,Class<?> type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SearchResult search(String path, String templateName, Object params,Class<?> type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SearchResult search(String path, String entity,Class<?> type) throws ElasticSearchException {
		SearchResult searchResult = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
		if(searchResult instanceof ErrorResponse){
			throw new ElasticSearchException(SimpleStringUtil.object2json(searchResult));
		}
		return searchResult;
	}

	protected <T> ESDatas<T> buildESDatas(SearchResult result,Class<T> type){
		if(result instanceof ErrorResponse){
			throw new ElasticSearchException(SimpleStringUtil.object2json(result));
		}
		ESDatas<T> datas = new ESDatas<T>();
		RestResponse restResponse = (RestResponse)result;
		datas.setTotalSize(restResponse.getSearchHits().getTotal());
		List<SearchHit> searchHits = restResponse.getSearchHits().getHits();
		List<T> hits = new ArrayList<T>(searchHits.size());
		boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
		T data = null;
		for(SearchHit hit:searchHits){
			data = (T) hit.getSource();
			hits.add(data);
			if(isESBaseData) {
				buildESBaseData(  hit,  (ESBaseData)data);
			}

		}
		datas.setAggregations(restResponse.getAggregations());
		datas.setDatas(hits);
		return datas;
	}
	public <T> ESDatas<T> searchList(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	public <T> ESDatas<T> searchList(String path, String templateName, Object params,Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	public <T> ESDatas<T> searchList(String path, String entity, Class<T> type) throws ElasticSearchException{
		SearchResult result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
		return buildESDatas(result,type);
	}

	public <T> T searchObject(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
	 	return null;
	}

	public <T> T searchObject(String path, String templateName, Object params,Class<T> type) throws ElasticSearchException {
	 	return null;
	}
	protected void buildESBaseData(SearchHit hit,ESBaseData esBaseData){
		esBaseData.setFields(hit.getFields());
		esBaseData.setHighlight( hit.getHighlight());
		esBaseData.setId(hit.getId());
		esBaseData.setScore(hit.getScore());
		esBaseData.setSort(hit.getSort());
		esBaseData.setType(hit.getType());
		esBaseData.setVersion(hit.getVersion());
		esBaseData.setIndex(hit.getIndex());
	}
	protected <T> T buildObject(SearchResult result, Class<T> type){
		if(result == null){
			return null;
		}
		if(result instanceof ErrorResponse){
			throw new ElasticSearchException(SimpleStringUtil.object2json(result));
		}
		if(result instanceof RestResponse) {
			RestResponse restResponse = (RestResponse) result;
			List<SearchHit> searchHits = restResponse.getSearchHits().getHits();
			if (searchHits != null && searchHits.size() > 0) {
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
				SearchHit hit = searchHits.get(0);
				T data = (T) hit.getSource();
				if (isESBaseData) {
					buildESBaseData(hit, (ESBaseData) data);
				}
				return data;
			}
			return null;
		}
		else
		{
			boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
			SearchHit hit = (SearchHit)result;
			if(hit.isFound()) {
				T data = (T) hit.getSource();
				if (isESBaseData) {
					buildESBaseData(hit, (ESBaseData) data);
				}
				return data;
			}
			else {
				return null;
			}
		}

	}
	public <T> T searchObject(String path, String entity, Class<T> type) throws ElasticSearchException{
		SearchResult result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
		return buildObject(result, type);
	}
	private Long longValue(Object num,Long defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Long)
		{
			return ((Long)num);
		}else if(num instanceof Double)
		{
			return ((Double)num).longValue();
		}else if(num instanceof Integer){
			return ((Integer)num).longValue();
		}
		else if(num instanceof Float)
		{
			return ((Float)num).longValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).longValue();
		}
		else
		{
			return Long.parseLong(num.toString());
		}
	}

	private Float floatValue(Object num,Float defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Float)
		{
			return (Float)num;
		}else if(num instanceof Double)
		{
			return ((Double)num).floatValue();
		}else if(num instanceof Integer){
			return ((Integer)num).floatValue();
		}
		else  if(num instanceof Long)
		{
			return ((Long)num).floatValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).floatValue();
		}
		else
		{
			return Float.parseFloat(num.toString());
		}
	}

	private Double doubleValue(Object num,Double defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Double)
		{
			return (Double)num;
		}else if(num instanceof Float)
		{
			return ((Float)num).doubleValue();
		}else if(num instanceof Integer){
			return ((Integer)num).doubleValue();
		}
		else  if(num instanceof Long)
		{
			return ((Long)num).doubleValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).doubleValue();
		}
		else
		{

			return Double.parseDouble(num.toString());
		}
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

	protected  <T extends AggHit> ESAggDatas<T> buildESAggDatas(SearchResult result,Class<T> type,String aggs,String stats){
		if(result instanceof ErrorResponse){
			throw new ElasticSearchException(SimpleStringUtil.object2json(result));
		}
		else
		{
			RestResponse searchResult =(RestResponse)result;
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
					while(iterable.hasNext()){
						Map.Entry<String, Map<String, Object>> entry = iterable.next();
						String key = entry.getKey();
						try {
							T obj = type.newInstance();
							if (obj instanceof LongAggRangeHit) {
								buildLongAggRangeHit((LongAggRangeHit) obj, entry.getValue(),  stats,key);
							} else if (obj instanceof DoubleAggRangeHit)
							{
								buildDoubleAggRangeHit((DoubleAggRangeHit) obj, entry.getValue(),  stats,key);
							}else if (obj instanceof FloatAggRangeHit)
							{
								buildFloatAggRangeHit((FloatAggRangeHit) obj, entry.getValue(),  stats,key);
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

		}
		return null;
	}
	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Map params,Class<T> type,String aggs,String stats) throws ElasticSearchException{

		return null;
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Object params,Class<T> type,String aggs,String stats) throws ElasticSearchException{
		return null;
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path,String entity,Class<T> type,String aggs,String stats) throws ElasticSearchException{
		SearchResult result = this.client.executeRequest(path,entity,   new ElasticSearchResponseHandler( ));
		return buildESAggDatas(result,type,aggs,stats);
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
	public SearchResult search(String path, String templateName, Map params,ESTypeReferences type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SearchResult search(String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SearchResult search(String path, String entity,ESTypeReferences type) throws ElasticSearchException {
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
		 return this.client.executeHttp(indexName+"?pretty",indexMapping,ClientUtil.HTTP_POST);
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
		 String response = (String)client.executeHttp(index+"/_mapping?pretty",ClientUtil.HTTP_GET);
		 return response;
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
