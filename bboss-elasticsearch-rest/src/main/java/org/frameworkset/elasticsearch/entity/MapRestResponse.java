package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MapRestResponse extends BaseRestResponse{


    @JsonProperty("hits")
    private MapSearchHits searchHits;

	public MapRestResponse() {
		// TODO Auto-generated constructor stub
	}

	public MapSearchHits getSearchHits() {
		return searchHits;
	}
	public void setSearchHits(MapSearchHits searchHits) {
		this.searchHits = searchHits;
	}

	

}
