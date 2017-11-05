package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public abstract class BaseSearchHits implements Serializable {
	private long total;
	@JsonProperty("max_score")
	private Long maxScore;

	public BaseSearchHits() {
		// TODO Auto-generated constructor stub
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public Long getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(Long maxScore) {
		this.maxScore = maxScore;
	}
}
