package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.frameworkset.elasticsearch.serial.ESHitDeserializer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SearchHit  implements Serializable {
	@JsonProperty("_index")
	private String index;//"_index": "trace-2017.09.01",
	@JsonProperty("_type")
	private String  type;
	@JsonProperty("_id")
	private String  id;
	@JsonProperty("_score")
	private int  score;
	@JsonProperty("_source")

	private Object source;
	private Map<String,List<Object>> fields;
	@JsonProperty("_version")
	private int version;

	private Map<String,List<Object>> highlight;
	private Object[] sort;
	public SearchHit() {
		// TODO Auto-generated constructor stub
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}

	public Object getSource() {
		return source;
	}
	@JsonDeserialize(using = ESHitDeserializer.class)
	public void setSource(Object source) {
		this.source = source;
	}
	public Object[] getSort() {
		return sort;
	}
	public void setSort(Object[] sort) {
		this.sort = sort;
	}

	public Map<String, List<Object>> getFields() {
		return fields;
	}

	public void setFields(Map<String, List<Object>> fields) {
		this.fields = fields;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Map<String, List<Object>> getHighlight() {
		return highlight;
	}

	public void setHighlight(Map<String, List<Object>> highlight) {
		this.highlight = highlight;
	}
}
