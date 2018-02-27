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
	 * @return
	 */
	public Object getAggBuckets(String metrics){
		Map<String,Object> map = aggregations.get(metrics);
		if(map != null){
			return map.get("buckets");
		}
		return null;
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
	public Object getAggAttribute(String metrics,String attribute){
		Map<String,Object> map = aggregations.get(metrics);
		if(map != null){
			return map.get(attribute);
		}
		return null;
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




}
