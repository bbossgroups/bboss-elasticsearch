package org.frameworkset.elasticsearch.entity;

import java.util.List;
public class SearchHits  extends BaseSearchHits{

	private List<SearchHit> hits;
	public SearchHits() {
		// TODO Auto-generated constructor stub
	}

	public List<SearchHit> getHits() {
		return hits;
	}
	public void setHits(List<SearchHit> hits) {
		this.hits = hits;
	}

}
