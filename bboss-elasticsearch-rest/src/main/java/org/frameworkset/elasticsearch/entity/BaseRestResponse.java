package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.frameworkset.elasticsearch.serial.ESTypeReference;

import java.io.Serializable;
import java.util.Map;

public abstract  class BaseRestResponse implements Serializable {

	private long took;
	@JsonProperty("timed_out")
	private boolean timedOut;
	@JsonProperty("_shards")
	private Shards shards;

	private Map<String,Map<String,Object>> aggregations;

	public long getTook() {
		return took;
	}
	public void setTook(long took) {
		this.took = took;
	}

	public Shards getShards() {
		return shards;
	}
	public void setShards(Shards shards) {
		this.shards = shards;
	}

	public boolean isTimedOut() {
		return timedOut;
	}
	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}
	public Map<String, Map<String,Object>> getAggregations() {
		return aggregations;
	}
	public void setAggregations(Map<String, Map<String,Object>> aggregations) {
		this.aggregations = aggregations;
	}

	public <T> T getAggBuckets(String metrics,Class<T> type){
		Map<String,Object> map = aggregations.get(metrics);
		if(map != null){
			return (T)map.get("buckets");
		}
		return (T)null;
	}

	/**
	 *
	 * @param metrics
	 * @param typeReference 容器类型对象
	 * @param <T>
	 * @return
	 */
	public <T> T getAggBuckets(String metrics,ESTypeReference<T> typeReference){
		Map<String,Object> map = aggregations.get(metrics);
		if(map != null){
			return (T)map.get("buckets");
		}
		return (T)null;
	}

	/**
	 *
	 * @param metrics
	 * @param typeReference 容器类型对象
	 * @param <T>
	 * @return
	 */
	public <T> T getAggAttribute(String metrics,String attribute,ESTypeReference<T> typeReference){
		Map<String,Object> map = aggregations.get(metrics);
		if(map != null){
			return (T)map.get(attribute);
		}
		return (T)null;
	}

	/**
	 *
	 * @param metrics
	 * @param typeReference 容器类型对象
	 * @param <T>
	 * @return
	 */
	public <T> T getAggAttribute(String metrics,String attribute,Class<T> typeReference){
		Map<String,Object> map = aggregations.get(metrics);
		if(map != null){
			return (T)map.get(attribute);
		}
		return (T)null;
	}

	/**
	 *
	 * @param metrics
	 * @return
	 */
	public Map<String,Object> getAggregationMetrics(String metrics){
		Map<String,Object> map = aggregations.get(metrics);

		return map;
	}

	/**
	 * {
	 "took": 14,
	 "timed_out": false,
	 "_shards": {
	 "total": 12,
	 "successful": 12,
	 "skipped": 0,
	 "failed": 0
	 },
	 "hits": {
	 "total": 54,
	 "max_score": 0,
	 "hits": []
	 },
	 "aggregations": {
	 "applicationsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 },
	 {
	 "key": "testweb1",
	 "doc_count": 17,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 17
	 }
	 ]
	 }
	 },
	 {
	 "key": "dubbo-test",
	 "doc_count": 4,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 3
	 },
	 {
	 "key": 1,
	 "doc_count": 1
	 }
	 ]
	 }
	 },
	 {
	 "key": "httptest",
	 "doc_count": 3,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 3
	 }
	 ]
	 }
	 }
	 ]
	 }
	 }
	 }
	 * @param map
	 * @param metrics
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggBuckets(Map<String,Object> map ,String metrics,ESTypeReference<T> typeReference){
		Map<String,Object> metrics_ = (Map<String,Object>)map.get(metrics);
		if(metrics_ != null){
			return (T)metrics_.get("buckets");
		}
		return (T)null;
	}

	/**
	 * {
		 "key": "demoproject",
		 "doc_count": 30,
		 "successsums": {
			 "doc_count_error_upper_bound": 0,
			 "sum_other_doc_count": 0,
			 "buckets": [
				 {
				 "key": 0,
				 "doc_count": 30
				 }
			 ]
		 }
	 },
	 * @param map
	 * @param metrics successsums
	 * @return
	 */
	public static Map<String,Object> getAggregationMetrics(Map<String,Object> map ,String metrics ){
		if(map != null){
			return (Map<String,Object>)map.get(metrics);
		}
		return (Map<String,Object>)null;
	}

	/**
	 * {
		 "key": "demoproject",
		 "doc_count": 30,
		 "successsums": {
			 "doc_count_error_upper_bound": 0,
			 "sum_other_doc_count": 0,
			 "buckets": [
				 {
				 "key": 0,
				 "doc_count": 30
				 }
			 ]
		 }
	 },
	 * @param map
	 * @param metrics successsums->buckets
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggBuckets(Map<String,Object> map ,String metrics,Class<T> typeReference){
		Map<String,Object> metrics_ = (Map<String,Object>)map.get(metrics);
		if(metrics_ != null){
			return (T)metrics_.get("buckets");
		}
		return (T)null;
	}

	/**
	 * {
		 "key": "demoproject",
		 "doc_count": 30,
		 "successsums": {
			 "doc_count_error_upper_bound": 0,
			 "sum_other_doc_count": 0,
			 "buckets": [
				 {
					 "key": 0,
					 "doc_count": 30
				 }
			 ]
		 }
	 }
	 *
	 * @param map
	 * @param metrics successsums->doc_count_error_upper_bound
	 *                successsums->sum_other_doc_count
	 * @param attribute maybe doc_count_error_upper_bound sum_other_doc_count buckets
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggAttribute(Map<String,Object> map ,String metrics,String attribute,ESTypeReference<T> typeReference){
		Map<String,Object> metrics_ = (Map<String,Object>)map.get(metrics);
		if(metrics_ != null){
			return (T)metrics_.get(attribute);
		}
		return (T)null;
	}
	/**
	 * {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 }
	 *
	 * @param map
	 * @param metrics successsums->doc_count_error_upper_bound
	 *                successsums->sum_other_doc_count
	 * @param attribute maybe doc_count_error_upper_bound sum_other_doc_count buckets
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggAttribute(Map<String,Object> map ,String metrics,String attribute,Class<T> typeReference){
		Map<String,Object> metrics_ = (Map<String,Object>)map.get(metrics);
		if(metrics_ != null){
			return (T)metrics_.get(attribute);
		}
		return (T)null;
	}



}
