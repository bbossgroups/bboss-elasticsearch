package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

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

}
