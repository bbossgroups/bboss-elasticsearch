package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ESAggDatas<T>  implements Serializable {
	private long totalSize;
	/**
	 * 多值聚合查询结果集合
	 */
	private List<T> aggDatas;
	/**
	 * 单值聚合查询结果
	 */
	private T singleAggData;
	public Map<String,?> getAggregations() {
		return aggregations;
	}

	public void setAggregations(Map<String,?> aggregations) {
		this.aggregations = aggregations;
	}

	private Map<String,?> aggregations;
	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public List<T> getAggDatas() {
		return aggDatas;
	}

	public void setAggDatas(List<T> aggDatas) {
		this.aggDatas = aggDatas;
	}

	public T getSingleAggData() {
		return singleAggData;
	}

	public void setSingleAggData(T singleAggData) {
		this.singleAggData = singleAggData;
	}
}
