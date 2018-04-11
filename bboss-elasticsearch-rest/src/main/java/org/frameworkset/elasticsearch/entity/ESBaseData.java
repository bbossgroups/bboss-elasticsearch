package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * elastic search 结果基础对象,用于业务对象的基础类,所有属性允许反序列化，不允许序列化
 */
public abstract class ESBaseData implements Serializable{
	private String  type;
	private Object  id;
	private Map<String,List<Object>> fields;
	private int version;
	private String index;//"_index": "trace-2017.09.01",
	private Map<String,List<Object>> highlight;
	private Object[] sort;
	private Double  score;
	private Object parent;
	private Object routing;
	private boolean found;
	private Map<String,Object> nested;
	private Map<String,Map<String,InnerSearchHits>> innerHits;
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
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

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}


	public Object getRouting() {
		return routing;
	}

	public void setRouting(Object routing) {
		this.routing = routing;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}

	public Map<String, Object> getNested() {
		return nested;
	}

	public void setNested(Map<String, Object> nested) {
		this.nested = nested;
	}

	public Map<String, Map<String, InnerSearchHits>> getInnerHits() {
		return innerHits;
	}

	public void setInnerHits(Map<String, Map<String, InnerSearchHits>> innerHits) {
		this.innerHits = innerHits;
	}
}
