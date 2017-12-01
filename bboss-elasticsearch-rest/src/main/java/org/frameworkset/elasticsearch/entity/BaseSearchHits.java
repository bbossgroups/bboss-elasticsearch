package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public abstract class BaseSearchHits implements Serializable {
	private long total;
	@JsonProperty("max_score")
	private Double maxScore;

	public BaseSearchHits() {
		// TODO Auto-generated constructor stub
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public Double getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
	}
}
