package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class SearchHits  implements Serializable {
	private long total;
	@JsonProperty("max_score")
	private Long maxScore;
	private List<SearchHit> hits;
	public SearchHits() {
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
	public List<SearchHit> getHits() {
		return hits;
	}
	public void setHits(List<SearchHit> hits) {
		this.hits = hits;
	}

}
