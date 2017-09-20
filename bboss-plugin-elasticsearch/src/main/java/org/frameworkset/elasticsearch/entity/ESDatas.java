package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.List;

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
}
