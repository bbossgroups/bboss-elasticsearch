package org.frameworkset.elasticsearch.handler;

import org.frameworkset.elasticsearch.entity.RestResponse;

import java.util.Map;

/**
 * 聚合查询bucket处理接口
 */
public interface ESAggBucketHandle<T> {
	/**
	 * 聚合操作自定义指标处理函数
	 * @param result 检索结果
	 * @param bucket 存放聚合桶bucket中对应的metrics统计指标值
	 * @param obj 封装指标数据的业务对象
	 * @param key 桶的对应的指标键值名称
	 */
	void bucketHandle(RestResponse result, Map<String, Object> bucket, T obj, String key);
}
