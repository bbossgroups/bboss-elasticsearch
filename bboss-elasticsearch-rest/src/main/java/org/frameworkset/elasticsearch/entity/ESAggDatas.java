package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ESAggDatas<T>  implements Serializable {
	private long totalSize;
	private List<T> aggDatas;
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
}
