package org.frameworkset.elasticsearch.entity;

import java.util.List;
import java.util.Map;

/**
 * 查询的结果集
 * @param <T>
 */
public class ESDatas<T> extends BaseHitsTotal {

	/**
	 * 当前获取的记录集合
	 */
	private List<T> datas;
	private Map<String,Map<String,Object>> aggregations;
	private String scrollId;
	private BaseRestResponse restResponse;



	public List<T> getDatas() {
		return datas;
	}
	public String getPitId() {
		return restResponse != null ? restResponse.getPitId():null;
	}
	public void setDatas(List<T> datas) {
		this.datas = datas;
	}

	public Map<String, Map<String,Object>> getAggregations() {
		return aggregations;
	}
	public List<Map<String, Object>> getAggregationBuckets(String buckets) {
		if(aggregations != null && aggregations.size() > 0) {
			Map<String,Object> temp = aggregations.get(buckets);
			if(temp != null){
				return (List<Map<String, Object>>)temp.get("buckets");
			}
		}
		return null;
	}




	public void setAggregations(Map<String, Map<String,Object>> aggregations) {
		this.aggregations = aggregations;
	}

	public String getScrollId() {
		return scrollId;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}

	public BaseRestResponse getRestResponse() {
		return restResponse;
	}

	public void setRestResponse(BaseRestResponse restResponse) {
		this.restResponse = restResponse;
	}

	public String getTotalRelation() {
		return totalRelation;
	}

	public void setTotalRelation(String totalRelation) {
		this.totalRelation = totalRelation;
	}
}
