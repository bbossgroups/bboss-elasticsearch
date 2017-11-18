package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * elastic search 结果基础对象,用于业务对象的基础类,所有属性允许反序列化，不允许序列化
 */

public abstract class ESBaseData implements Serializable{
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private String  type;
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private String  id;
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private Map<String,List<Object>> fields;
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private int version;
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private String index;//"_index": "trace-2017.09.01",
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private Map<String,List<Object>> highlight;
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private Object[] sort;
	@JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
	private int  score;


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

	public Object[] getSort() {
		return sort;
	}

	public void setSort(Object[] sort) {
		this.sort = sort;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}


}
