package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.frameworkset.elasticsearch.serial.ESTypeReference;

import java.io.Serializable;
import java.util.Map;

public abstract  class BaseRestResponse implements Serializable {
	@JsonIgnore
	private int reponseStatus;
	@JsonProperty("_scroll_id")
	private String scrollId;
	private long took;
	@JsonProperty("timed_out")
	private boolean timedOut;
	@JsonProperty("_shards")
	private Shards shards;


	@JsonProperty("pit_id")
	private String pitId;
	/**
	 * The count API allows to easily execute a query and get the number of matches for that query. It can be executed across one or more indices and across one or more types. The query can either be provided using a simple query string as a parameter, or using the Query DSL defined within the request body. Here is an example:

	 PUT /twitter/_doc/1?refresh
	 {
	 "user": "kimchy"
	 }

	 GET /twitter/_doc/_count?q=user:kimchy

	 GET /twitter/_doc/_count
	 {
	 "query" : {
	 "term" : { "user" : "kimchy" }
	 }
	 }
	 * @return
	 */
	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	private long count;

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
		if(aggregations != null) {
			Map<String, Object> map = aggregations.get(metrics);
			if (map != null) {
				return (T) map.get("buckets");
			}
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
		if(aggregations != null) {
			Map<String, Object> map = aggregations.get(metrics);
			if (map != null) {
				return (T) map.get("buckets");
			}
		}
		return (T)null;
	}

	/**
	 *
	 * @param metrics
	 * @return
	 */
	public Object getAggBuckets(String metrics){
		if(aggregations != null) {
			Map<String, Object> map = aggregations.get(metrics);
			if (map != null) {
				return map.get("buckets");
			}
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
		if(aggregations != null) {
			Map<String, Object> map = aggregations.get(metrics);
			if (map != null) {
				return (T) map.get(attribute);
			}
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
		if(aggregations != null) {
			Map<String, Object> map = aggregations.get(metrics);
			if (map != null) {
				return (T) map.get(attribute);
			}
		}
		return (T)null;
	}

	/**
	 *
	 * @param metrics

	 * @return
	 */
	public Object getAggAttribute(String metrics,String attribute){
		if(aggregations != null) {
			Map<String, Object> map = aggregations.get(metrics);
			if (map != null) {
				return map.get(attribute);
			}
		}
		return null;
	}

	/**
	 *
	 * @param metrics
	 * @return
	 */
	public Map<String,Object> getAggregationMetrics(String metrics){
		if(aggregations != null) {
			Map<String, Object> map = aggregations.get(metrics);

			return map;
		}
		return null;
	}


	public String getScrollId() {
		return scrollId;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}

	public int getReponseStatus() {
		return reponseStatus;
	}

	public void setReponseStatus(int reponseStatus) {
		this.reponseStatus = reponseStatus;
	}

	public String getPitId() {
		return pitId;
	}

	public void setPitId(String pitId) {
		this.pitId = pitId;
	}
}
