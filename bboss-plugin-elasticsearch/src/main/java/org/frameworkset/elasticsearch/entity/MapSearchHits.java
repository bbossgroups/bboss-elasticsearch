package org.frameworkset.elasticsearch.entity;

import java.util.List;

public class MapSearchHits extends BaseSearchHits{

	private List<MapSearchHit> hits;
	public MapSearchHits() {
		// TODO Auto-generated constructor stub
	}

	public List<MapSearchHit> getHits() {
		return hits;
	}
	public void setHits(List<MapSearchHit> hits) {
		this.hits = hits;
	}

}
