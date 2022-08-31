package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BaseSearchHit implements Serializable {
	@JsonProperty("_index")
	private String index;//"_index": "trace-2017.09.01",
	@JsonProperty("_type")
	private String  type;
	@JsonProperty("_id")
	private String  id;
	@JsonProperty("_score")
	private Double  score;
	private Map<String,List<Object>> fields;
	@JsonProperty("_version")
	private long version;
	@JsonProperty("_routing")
	private Object routing;
	@JsonProperty("_parent")
	private Object parent;
	private boolean found;
	private Map<String,List<Object>> highlight;
	private Object[] sort;
	@JsonProperty("_nested")
	private Map<String,Object> nested;
	@JsonProperty("inner_hits")
	private Map<String,Map<String,InnerSearchHits>> innerHits;
	@JsonProperty("_shard")
	private String shard;//"_index": "trace-2017.09.01",
	@JsonProperty("_node")
	private String node;//"_index": "trace-2017.09.01",

	@JsonProperty("_explanation")
	private Explanation explanation;//"_index": "trace-2017.09.01",
	@JsonProperty("_seq_no")
	private long seqNo;//"_index": "trace-2017.09.01",
	@JsonProperty("_primary_term")
	private long primaryTerm;//"_index": "trace-2017.09.01",
	@JsonProperty("matched_queries")
	private String[] matchedQueries;//["",""],

	public Map<String, Map<String,InnerSearchHits>> getInnerHits() {
		return innerHits;
	}

	public void setInnerHits(Map<String, Map<String,InnerSearchHits>> innerHits) {
		this.innerHits = innerHits;
	}

	/**  moved to ResultUtil
	public <T> List<T> getInnerHits(String indexType, Class<T> type){
		if(this.innerHits == null || this.innerHits.size() == 0)
			return null;
		Map<String,InnerSearchHits> hits = this.innerHits.get(indexType);
		if(hits != null){
			InnerSearchHits ihits = hits.get("hits");
			if(ihits != null){
				List<InnerSearchHit> temp = ihits.getHits();
				if(temp.size() == 0)
					return null;
				if(InnerSearchHit.class.isAssignableFrom(type))
				{
					return (List<T>)temp;
				}
				else{
					List<T> ts = new ArrayList<T>(temp.size());
					for(int i = 0; i < temp.size(); i ++){
						ts.add((T) temp.get(i).getSource());
					}
					return ts;
				}
			}
		}
		return null;
	}*/
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
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
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

	public Explanation getExplanation() {
		return explanation;
	}

	public void setExplanation(Explanation explanation) {
		this.explanation = explanation;
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
