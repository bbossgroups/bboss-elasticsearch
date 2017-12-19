package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 查询的结果集
 * @param <T>
 */
public class ESDatas<T> implements Serializable {
	/**
	 * 总的记录数
	 */
	private long totalSize;
	/**
	 * 当前获取的记录集合
	 */
	private List<T> datas;
	private Map<String,Map<String,Object>> aggregations;


	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public List<T> getDatas() {
		return datas;
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
}
