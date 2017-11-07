package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
public class RestResponse  extends BaseRestResponse {
	

    @JsonProperty("hits")
    private SearchHits searchHits;

	public SearchHits getSearchHits() {
		return searchHits;
	}
	public void setSearchHits(SearchHits searchHits) {
		this.searchHits = searchHits;
	}

	

}
