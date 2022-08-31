package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * elastic search 结果基础对象,用于业务对象的基础类,所有属性允许反序列化，不允许序列化
 */
public abstract class ESBaseData implements Serializable{
	private String  type;
	private String  id;
	private Map<String,List<Object>> fields;
	private String[] matchedQueries;//["",""],
	private long version;
	private String index;//"_index": "trace-2017.09.01",
	private Map<String,List<Object>> highlight;
	private Object[] sort;
	private Double  score;
	private Object parent;
	private Object routing;
	private boolean found;
	private Map<String,Object> nested;
	private Map<String,Map<String,InnerSearchHits>> innerHits;
	private String shard;//"_index": "trace-2017.09.01",
	private String node;//"_index": "trace-2017.09.01",

	private Explanation explanation;//"_index": "trace-2017.09.01",
	private long seqNo;//"_index": "trace-2017.09.01",
	private long primaryTerm;//"_index": "trace-2017.09.01",
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

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
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

	public String getShard() {
		return shard;
	}

	public void setShard(String shard) {
		this.shard = shard;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Explanation getExplanation() {
		return explanation;
	}

	public void setExplanation(Explanation explanation) {
		this.explanation = explanation;
	}

	public long getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(long seqNo) {
		this.seqNo = seqNo;
	}

	public long getPrimaryTerm() {
		return primaryTerm;
	}

	public void setPrimaryTerm(long primaryTerm) {
		this.primaryTerm = primaryTerm;
	}

	public void setMatchedQueries(String[] matchedQueries) {
		this.matchedQueries = matchedQueries;
	}

	public String[] getMatchedQueries() {
		return matchedQueries;
	}
}
