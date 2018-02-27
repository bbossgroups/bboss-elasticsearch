package org.frameworkset.elasticsearch.client;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.handler.ESAggBucketHandle;
import org.frameworkset.elasticsearch.serial.ESTypeReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class ResultUtil {
	public static  Long longValue(Object num,Long defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Long)
		{
			return ((Long)num);
		}else if(num instanceof Double)
		{
			return ((Double)num).longValue();
		}else if(num instanceof Integer){
			return ((Integer)num).longValue();
		}
		else if(num instanceof Float)
		{
			return ((Float)num).longValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).longValue();
		}
		else
		{
			return Long.parseLong(num.toString());
		}
	}

	public static  Integer intValue(Object num,Integer defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Integer)
		{
			return ((Integer)num);
		}
		else if(num instanceof Long)
		{
			return ((Long)num).intValue();
		}else if(num instanceof Double)
		{
			return ((Double)num).intValue();
		}
		else if(num instanceof Float)
		{
			return ((Float)num).intValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).intValue();
		}
		else
		{
			return Integer.parseInt(num.toString());
		}
	}

	public static  Float floatValue(Object num,Float defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Float)
		{
			return (Float)num;
		}else if(num instanceof Double)
		{
			return ((Double)num).floatValue();
		}else if(num instanceof Integer){
			return ((Integer)num).floatValue();
		}
		else  if(num instanceof Long)
		{
			return ((Long)num).floatValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).floatValue();
		}
		else
		{
			return Float.parseFloat(num.toString());
		}
	}

	public static  Double doubleValue(Object num,Double defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Double)
		{
			return (Double)num;
		}else if(num instanceof Float)
		{
			return ((Float)num).doubleValue();
		}else if(num instanceof Integer){
			return ((Integer)num).doubleValue();
		}
		else  if(num instanceof Long)
		{
			return ((Long)num).doubleValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).doubleValue();
		}
		else
		{

			return Double.parseDouble(num.toString());
		}
	}
	/**
	 * 处理lucene特殊字符
	 * @param condition
	 * @return
	 */
	public static String handleLuceneSpecialChars(String condition){
		if(condition == null || condition.equals("")){
			return condition;
		}
		condition = condition.replace(":","/:");
		condition = condition.replace("-","/-");
		condition = condition.replace("+","/+");
		condition = condition.replace("&","/&");
		condition = condition.replace("!","/!");
		condition = condition.replace("{","/{");
		condition = condition.replace("}","/}");
		condition = condition.replace("(","/(");
		condition = condition.replace(")","/)");
		condition = condition.replace("|","/|");

		condition = condition.replace("~","/~");
		condition = condition.replace("*","/*");
		condition = condition.replace("?","/?");
		condition = condition.replace("/","//");
//		condition = condition.replace("\"","/\"");

		return condition;
	}

	/**
	 * 处理es特殊字符
	 * @param condition
	 * @return
	 */
	public static String handleElasticSearchSpecialChars(String condition){
		if(condition == null || condition.equals("")){
			return condition;
		}
		condition = condition.replace(":","\\:");
		condition = condition.replace("-","\\-");
		condition = condition.replace("+","\\+");
		condition = condition.replace("&","\\&");
		condition = condition.replace("!","\\!");
		condition = condition.replace("{","\\{");
		condition = condition.replace("}","\\}");
		condition = condition.replace("(","\\(");
		condition = condition.replace(")","\\)");
		condition = condition.replace("|","\\|");

		condition = condition.replace("~","\\~");
		condition = condition.replace("*","\\*");
		condition = condition.replace("?","\\?");
		condition = condition.replace("/","\\/");


		return condition;
	}
	public static void buildLongAggHit(LongAggHit longRangeHit, Map<String,Object> bucket, String stats){
		longRangeHit.setKey((String)bucket.get("key"));
		longRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		longRangeHit.setMax(longValue(stats_.get("max"),0l));
		longRangeHit.setMin(longValue(stats_.get("min"),0l));
		longRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		longRangeHit.setSum(longValue(stats_.get("sum"),0l));
	}
	public static void buildFloatAggHit(FloatAggHit floatRangeHit, Map<String,Object> bucket, String stats){
		floatRangeHit.setKey((String)bucket.get("key"));
		floatRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		floatRangeHit.setMax(floatValue(stats_.get("max"),0f));
		floatRangeHit.setMin(floatValue(stats_.get("min"),0f));
		floatRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		floatRangeHit.setSum(floatValue(stats_.get("sum"),0f));
	}

	public static void buildDoubleAggHit(DoubleAggHit doubleAggHit, Map<String,Object> bucket, String stats){
		doubleAggHit.setKey((String)bucket.get("key"));
		doubleAggHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		doubleAggHit.setMax(doubleValue(stats_.get("max"),0d));
		doubleAggHit.setMin(doubleValue(stats_.get("min"),0d));
		doubleAggHit.setAvg(doubleValue(stats_.get("avg"),0d));
		doubleAggHit.setSum(doubleValue(stats_.get("sum"),0d));
	}

	public static void buildLongAggRangeHit(LongAggRangeHit longRangeHit, Map<String,Object> bucket, String stats, String key){
		longRangeHit.setKey(key);
		longRangeHit.setFrom(longValue(bucket.get("from"),null));
		longRangeHit.setTo(longValue(bucket.get("to"),null));
		longRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		longRangeHit.setMax(longValue(stats_.get("max"),0l));
		longRangeHit.setMin(longValue(stats_.get("min"),0l));
		longRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		longRangeHit.setSum(longValue(stats_.get("sum"),0l));
	}
	public static void buildFloatAggRangeHit(FloatAggRangeHit floatRangeHit, Map<String,Object> bucket, String stats, String key){
		floatRangeHit.setKey(key);
		floatRangeHit.setFrom(floatValue(bucket.get("from"),null));
		floatRangeHit.setTo(floatValue(bucket.get("to"),null));
		floatRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		floatRangeHit.setMax(floatValue(stats_.get("max"),0f));
		floatRangeHit.setMin(floatValue(stats_.get("min"),0f));
		floatRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		floatRangeHit.setSum(floatValue(stats_.get("sum"),0f));
	}
	public static void buildDoubleAggRangeHit(DoubleAggRangeHit doubleRangeHit,Map<String,Object> bucket,String stats,String key){
		doubleRangeHit.setKey(key);
		doubleRangeHit.setFrom(doubleValue(bucket.get("from"),null));
		doubleRangeHit.setTo(doubleValue(bucket.get("to"),null));
		doubleRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		doubleRangeHit.setMax(doubleValue(stats_.get("max"),0d));
		doubleRangeHit.setMin(doubleValue(stats_.get("min"),0d));
		doubleRangeHit.setAvg(doubleValue(stats_.get("avg"),0d));
		doubleRangeHit.setSum(doubleValue(stats_.get("sum"),0d));
	}

	public static  <T extends AggHit> ESAggDatas<T> buildESAggDatas(RestResponse searchResult,Class<T> type,String aggs,String stats,ESAggBucketHandle<T> aggBucketHandle){


		Map<String,Map<String,Object>> aggregations = searchResult.getAggregations();
		if(aggregations != null){
			Map<String,Object> traces = aggregations.get(aggs);
			Object _buckets = traces.get("buckets");
			ESAggDatas<T> ret = new ESAggDatas<T>();
			ret.setAggregations(aggregations);
			ret.setTotalSize(searchResult.getSearchHits().getTotal());

			if(_buckets instanceof List) {
				List<Map<String, Object>> buckets = (List<Map<String, Object>>) _buckets;
				List<T> datas = new ArrayList<>(buckets.size());
				ret.setAggDatas(datas);
				for (Map<String, Object> bucket : buckets) {
					try {
						T obj = type.newInstance();
						if(obj instanceof LongAggRangeHit){
							buildLongAggRangeHit((LongAggRangeHit) obj, bucket,  stats,null);
						}else if(obj instanceof FloatAggRangeHit){
							buildFloatAggRangeHit((FloatAggRangeHit) obj, bucket,  stats,null);
						}else if(obj instanceof DoubleAggRangeHit){
							buildDoubleAggRangeHit((DoubleAggRangeHit) obj, bucket,  stats,null);
						} else if (obj instanceof LongAggHit) {
							buildLongAggHit((LongAggHit) obj, bucket,  stats);
						} else if(obj instanceof FloatAggHit){
							buildFloatAggHit((FloatAggHit) obj, bucket,  stats);
						}else if(obj instanceof DoubleAggHit){
							buildDoubleAggHit((DoubleAggHit) obj, bucket,  stats);
						}
						if(aggBucketHandle != null)
						{
							aggBucketHandle.bucketHandle(searchResult,bucket,obj,null);
						}

						datas.add(obj);
					} catch (InstantiationException e) {
						throw new ElasticSearchException(e);
					} catch (IllegalAccessException e) {
						throw new ElasticSearchException(e);
					}
				}


			}
			else
			{
				Map<String,Map<String, Object>> buckets = (Map<String,Map<String, Object>>) _buckets;
				List<T> datas = new ArrayList<>(buckets.size());
				ret.setAggDatas(datas);
				Iterator<Map.Entry<String, Map<String, Object>>> iterable = buckets.entrySet().iterator();
				Map<String, Object> bucket = null;
				Map.Entry<String, Map<String, Object>> entry = null;
				String key = null;
				T obj = null;
				while(iterable.hasNext()){
					entry = iterable.next();
					key = entry.getKey();
					bucket = entry.getValue();
					try {
						obj = type.newInstance();

						if (obj instanceof LongAggRangeHit) {
							buildLongAggRangeHit((LongAggRangeHit) obj, bucket,  stats,key);
						} else if (obj instanceof DoubleAggRangeHit)
						{
							buildDoubleAggRangeHit((DoubleAggRangeHit) obj, bucket,  stats,key);
						}else if (obj instanceof FloatAggRangeHit)
						{
							buildFloatAggRangeHit((FloatAggRangeHit) obj, bucket,  stats,key);
						}
						if(aggBucketHandle != null)
						{
							aggBucketHandle.bucketHandle(searchResult,bucket,obj,key);
						}
						datas.add(obj);
					}catch (InstantiationException e) {
						throw new ElasticSearchException(e);
					} catch (IllegalAccessException e) {
						throw new ElasticSearchException(e);
					}
				}
			}
			return ret;
		}


		return null;
	}
	/**
	 * {
	 "took": 14,
	 "timed_out": false,
	 "_shards": {
	 "total": 12,
	 "successful": 12,
	 "skipped": 0,
	 "failed": 0
	 },
	 "hits": {
	 "total": 54,
	 "max_score": 0,
	 "hits": []
	 },
	 "aggregations": {
	 "applicationsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 },
	 {
	 "key": "testweb1",
	 "doc_count": 17,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 17
	 }
	 ]
	 }
	 },
	 {
	 "key": "dubbo-test",
	 "doc_count": 4,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 3
	 },
	 {
	 "key": 1,
	 "doc_count": 1
	 }
	 ]
	 }
	 },
	 {
	 "key": "httptest",
	 "doc_count": 3,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 3
	 }
	 ]
	 }
	 }
	 ]
	 }
	 }
	 }
	 * @param map
	 * @param metrics
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggBuckets(Map<String,?> map , String metrics, ESTypeReference<T> typeReference){
		if(map != null) {
			Map<String,Object> metrics_ = (Map<String,Object>)map.get(metrics);
			if(metrics_ != null){
				return (T)metrics_.get("buckets");
			}
		}
		return (T)null;
	}



	/**
	 * {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 },
	 * @param map
	 * @param metrics successsums
	 * @return
	 */
	public static Map<String,Object> getAggregationMetrics(Map<String,?> map ,String metrics ){
		if(map != null){
			return (Map<String,Object>)map.get(metrics);
		}
		return (Map<String,Object>)null;
	}

	/**
	 * {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 },
	 * @param map
	 * @param metrics successsums->buckets
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggBuckets(Map<String,?> map ,String metrics,Class<T> typeReference){
		if(map != null) {
			Map<String, Object> metrics_ = (Map<String, Object>) map.get(metrics);
			if (metrics_ != null) {
				return (T) metrics_.get("buckets");
			}
		}
		return (T)null;
	}
	/**
	 * {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 },
	 * @param map
	 * @param metrics successsums->buckets

	 * @return
	 */
	public static Object getAggBuckets(Map<String,?> map ,String metrics){
		if(map != null){
			Map<String,Object> metrics_ = (Map<String,Object>)map.get(metrics);
			if(metrics_ != null){
				return metrics_.get("buckets");
			}
		}
		return null;
	}

	/**
	 * {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 }
	 *
	 * @param map
	 * @param metrics successsums->doc_count_error_upper_bound
	 *                successsums->sum_other_doc_count
	 * @param attribute maybe doc_count_error_upper_bound sum_other_doc_count buckets
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggAttribute(Map<String,?> map ,String metrics,String attribute,ESTypeReference<T> typeReference){
		if(map != null) {
			Map<String, Object> metrics_ = (Map<String, Object>) map.get(metrics);
			if (metrics_ != null) {
				return (T) metrics_.get(attribute);
			}
		}
		return (T)null;
	}
	/**
	 * {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 }
	 *
	 * @param map
	 * @param metrics successsums->doc_count_error_upper_bound
	 *                successsums->sum_other_doc_count
	 * @param attribute maybe doc_count_error_upper_bound sum_other_doc_count buckets
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public static <T> T getAggAttribute(Map<String,?> map ,String metrics,String attribute,Class<T> typeReference){
		if(map != null) {
			Map<String, Object> metrics_ = (Map<String, Object>) map.get(metrics);
			if (metrics_ != null) {
				return (T) metrics_.get(attribute);
			}
		}
		return (T)null;
	}

	/**
	 * {
	 "key": "demoproject",
	 "doc_count": 30,
	 "successsums": {
	 "doc_count_error_upper_bound": 0,
	 "sum_other_doc_count": 0,
	 "buckets": [
	 {
	 "key": 0,
	 "doc_count": 30
	 }
	 ]
	 }
	 }
	 *
	 * @param map
	 * @param metrics successsums->doc_count_error_upper_bound
	 *                successsums->sum_other_doc_count
	 * @param attribute maybe doc_count_error_upper_bound sum_other_doc_count buckets
	 * @return
	 */
	public static Object getAggAttribute(Map<String,?> map ,String metrics,String attribute){
		if(map != null) {
			Map<String, Object> metrics_ = (Map<String, Object>) map.get(metrics);
			if (metrics_ != null) {
				return metrics_.get(attribute);
			}
		}
		return null;
	}


}
