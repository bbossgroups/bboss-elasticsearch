package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 聚合查询，并且返回结果集
 * @param <Hits>
 * @param <Aggs>
 */
public class ESHitAggDatas<Hits,Aggs>  implements Serializable {
	private long totalSize;
	private List<Aggs> aggDatas;
	/**
	 * 当前获取的记录集合
	 */
	private List<Hits> hitDatas;
	public Map<String, Map<String, Object>> getAggregations() {
		return aggregations;
	}

	public void setAggregations(Map<String, Map<String, Object>> aggregations) {
		this.aggregations = aggregations;
	}

	private Map<String,Map<String,Object>> aggregations;
}
