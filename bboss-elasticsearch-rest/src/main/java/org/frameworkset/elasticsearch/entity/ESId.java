package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ESId {
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
