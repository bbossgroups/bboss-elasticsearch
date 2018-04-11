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
	public static <T> List<T> getInnerHits(Map<String,Map<String,InnerSearchHits>> innerHits,String indexType, Class<T> type){
		if(innerHits == null || innerHits.size() == 0)
			return null;
		Map<String,InnerSearchHits> hits = innerHits.get(indexType);
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
	}

	public static List getInnerHits(Map<String,Map<String,InnerSearchHits>> innerHits,String indexType){
		if(innerHits == null || innerHits.size() == 0)
			return null;
		Map<String,InnerSearchHits> hits = innerHits.get(indexType);
		if(hits != null){
			InnerSearchHits ihits = hits.get("hits");
			if(ihits != null){
				List<InnerSearchHit> temp = ihits.getHits();
				if(temp.size() == 0)
					return null;

				List ts = new ArrayList<Object>(temp.size());
				for(int i = 0; i < temp.size(); i ++){
					ts.add(temp.get(i).getSource());
				}
				return ts;

			}
		}
		return null;
	}
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

	public static  void buildESBaseData(BaseSearchHit hit,ESBaseData esBaseData){
		esBaseData.setFields(hit.getFields());
		esBaseData.setHighlight( hit.getHighlight());
		esBaseData.setId(hit.getId());
		esBaseData.setScore(hit.getScore());
		esBaseData.setSort(hit.getSort());
		esBaseData.setType(hit.getType());
		esBaseData.setVersion(hit.getVersion());
		esBaseData.setIndex(hit.getIndex());
		esBaseData.setParent(hit.getParent());
		esBaseData.setRouting(hit.getRouting());
		esBaseData.setFound(hit.isFound());
		esBaseData.setNested(hit.getNested());
		esBaseData.setInnerHits(hit.getInnerHits());
	}

	public static  void buildESId(BaseSearchHit hit,ESId esBaseData){

		esBaseData.setId(hit.getId());

	}
	public static  void injectBaseData(Object data,BaseSearchHit hit,boolean isESBaseData,boolean isESId){

		if (isESBaseData) {
			buildESBaseData(hit, (ESBaseData) data);
		} else if (isESId) {
			buildESId(hit, (ESId) data);
		}
	}

	public static  void injectInnerHitBaseData(Map<String, Map<String,InnerSearchHits>> innerHits){
		Iterator<Map.Entry<String, Map<String, InnerSearchHits>>> iterator = innerHits.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, Map<String, InnerSearchHits>> entry = iterator.next();
			Map<String, InnerSearchHits> value = entry.getValue();
			InnerSearchHits hitsEntryValue = value.get("hits");
			if(hitsEntryValue != null){
				List<InnerSearchHit> innerSearchHits = hitsEntryValue.getHits();
				if(innerSearchHits != null && innerSearchHits.size() > 0){
					Object source = innerSearchHits.get(0).getSource();
					boolean isESBaseData = ESBaseData.class.isAssignableFrom(source.getClass());
					boolean isESId = false;
					if(!isESBaseData){
						isESId = ESId.class.isAssignableFrom(source.getClass());
					}
					if(isESBaseData || isESId) {
						for (int i = 0; i < innerSearchHits.size(); i++) {
							InnerSearchHit innerSearchHit = innerSearchHits.get(i);
							source = innerSearchHit.getSource();
							if (source != null) {
								injectBaseData(source, innerSearchHit, isESBaseData, isESId);
							}
						}
					}
				}
			}

		}

	}
	public static  <T> T buildObject(RestResponse result, Class<T> type){
		if(result == null){
			return null;
		}
		RestResponse restResponse = (RestResponse) result;
		List<SearchHit> searchHits = restResponse.getSearchHits().getHits();
		if (searchHits != null && searchHits.size() > 0) {
			SearchHit hit = searchHits.get(0);
			if(SearchHit.class.isAssignableFrom(type)){
				//处理源对象
				Object data =  hit.getSource();
				if(data != null) {

					boolean isESBaseData = ESBaseData.class.isAssignableFrom(data.getClass());
					boolean isESId = false;
					if(!isESBaseData){
						isESId = ESId.class.isAssignableFrom(data.getClass());
					}
					injectBaseData(data,hit,isESBaseData,isESId);
				}
				//处理InnerHit对象
				Map<String, Map<String,InnerSearchHits>> innerHits = hit.getInnerHits();
				if(innerHits != null && innerHits.size() > 0){
					injectInnerHitBaseData(innerHits);
				}
				return (T)hit;
			}
			else{
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
				boolean isESId = false;
				if(!isESBaseData){
					isESId = ESId.class.isAssignableFrom(type);
				}
				T data = (T) hit.getSource();
				if (isESBaseData) {
					buildESBaseData(hit, (ESBaseData) data);
				}
				else if(isESId)
				{
					buildESId(hit,(ESId )data);
				}
				return data;
			}

		}
		return null;


	}


	public static  <T> List<T> buildObjects(MGetDocs results, Class<T> type){
		if(results == null){
			return null;
		}
		List<SearchHit> hits = results.getDocs();
		if(hits == null ){
			return null;
		}
		List<T> docs  = new ArrayList<T>(hits.size());
		for(SearchHit result:hits) {
			docs.add (buildObject(  result,  type));
		}
		return docs;


	}
	public static  <T> T buildObject(SearchHit result, Class<T> type){
		if(result == null){
			return null;
		}
		if(SearchHit.class.isAssignableFrom(type)){
			//处理源对象
			Object data =  result.getSource();
			if(data != null) {
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(data.getClass());
				boolean isESId = false;
				if(!isESBaseData){
					isESId = ESId.class.isAssignableFrom(data.getClass());
				}
				injectBaseData(data,result,isESBaseData,isESId);
			}
			//处理InnerHit对象
			Map<String, Map<String,InnerSearchHits>> innerHits = result.getInnerHits();
			if(innerHits != null && innerHits.size() > 0){
				injectInnerHitBaseData(innerHits);
			}
			return (T)result;
		}
		boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
		boolean isESId = false;
		if(!isESBaseData){
			isESId = ESId.class.isAssignableFrom(type);
		}
		SearchHit hit = result;
		if(hit.isFound()) {
			T data = (T) hit.getSource();
			if (isESBaseData) {
				buildESBaseData(hit, (ESBaseData) data);
			}
			else if(isESId)
			{
				buildESId(hit,(ESId )data);
			}
			return data;
		}
		else {
			return null;
		}


	}
	public static <T> ESDatas<T> buildESDatas(RestResponse result,Class<T> type){
//		if(result instanceof ErrorResponse){
//			throw new ElasticSearchException(SimpleStringUtil.object2json(result));
//		}
		ESDatas<T> datas = new ESDatas<T>();
		RestResponse restResponse = (RestResponse)result;
		List<SearchHit> searchHits = null;
		if(restResponse.getSearchHits() != null) {
			datas.setTotalSize(restResponse.getSearchHits().getTotal());
			searchHits = restResponse.getSearchHits().getHits();
		}
		datas.setScrollId(restResponse.getScrollId());
		if(SearchHit.class.isAssignableFrom(type)){

			datas.setAggregations(restResponse.getAggregations());
			if(searchHits != null && searchHits.size() > 0) {
				Object obj = searchHits.get(0).getSource();
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(obj.getClass());
				boolean isESId = false;
				if (!isESBaseData) {
					isESId = ESId.class.isAssignableFrom(obj.getClass());
				}

				for (int i = 0; i < searchHits.size(); i++) {
					SearchHit hit = searchHits.get(i);

					//处理源对象
					Object data = hit.getSource();
					if (data != null) {
						ResultUtil.injectBaseData(data, hit, isESBaseData, isESId);
					}

					//处理InnerHit对象
					Map<String, Map<String, InnerSearchHits>> innerHits = hit.getInnerHits();
					if (innerHits != null && innerHits.size() > 0) {
						ResultUtil.injectInnerHitBaseData(innerHits);
					}
				}
			}
			datas.setDatas((List<T>) searchHits);
		}
		else{
			if(searchHits != null && searchHits.size() > 0) {
				List<T> hits = new ArrayList<T>(searchHits.size());
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
				boolean isESId = false;
				if (!isESBaseData) {
					isESId = ESId.class.isAssignableFrom(type);
				}
				T data = null;
				for (SearchHit hit : searchHits) {
					data = (T) hit.getSource();
					hits.add(data);
					if (data != null) {
						ResultUtil.injectBaseData(data, hit, isESBaseData, isESId);
					}
					//处理InnerHit对象
					Map<String, Map<String, InnerSearchHits>> innerHits = hit.getInnerHits();
					if (innerHits != null && innerHits.size() > 0) {
						ResultUtil.injectInnerHitBaseData(innerHits);
					}
//					if (isESBaseData) {
//						buildESBaseData(hit, (ESBaseData) data);
//					} else if (isESId) {
//						buildESId(hit, (ESId) data);
//					}

				}

				datas.setDatas(hits);
			}
			datas.setAggregations(restResponse.getAggregations());
		}

		return datas;
	}

}
