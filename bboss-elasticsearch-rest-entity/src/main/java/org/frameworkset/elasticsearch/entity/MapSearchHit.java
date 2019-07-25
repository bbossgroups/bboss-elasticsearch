package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MapSearchHit extends BaseSearchHit {

	@JsonProperty("_source")
	private Map<String,Object> source;

	public MapSearchHit() {
		// TODO Auto-generated constructor stub
	}


	public Map<String,Object> getSource() {
		return source;
	}

	public void setSource(Map<String,Object> source) {
		this.source = source;
	}

}
