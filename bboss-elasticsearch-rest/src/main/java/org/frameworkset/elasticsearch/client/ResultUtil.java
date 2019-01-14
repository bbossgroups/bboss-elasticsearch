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

import com.frameworkset.util.ColumnEditorInf;
import com.frameworkset.util.FieldToColumnEditor;
import com.frameworkset.util.NoSupportTypeCastException;
import com.frameworkset.util.ValueObjectUtil;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.entity.sql.ColumnMeta;
import org.frameworkset.elasticsearch.entity.sql.SQLRestResponse;
import org.frameworkset.elasticsearch.entity.sql.SQLResult;
import org.frameworkset.elasticsearch.handler.ESAggBucketHandle;
import org.frameworkset.elasticsearch.serial.ESTypeReference;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.spi.remote.http.HttpRuntimeException;
import org.frameworkset.util.BigFile;
import org.frameworkset.util.ClassUtil;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.frameworkset.util.annotations.wraper.ColumnWraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;

public abstract class ResultUtil {
	private static Logger logger = LoggerFactory.getLogger(ResultUtil.class);
	public static final int OPERTYPE_getDocument = 0;
	public static final int OPERTYPE_updateDocument = 1;
	public static final int OPERTYPE_deleteDocument = 2;

	public static final int OPERTYPE_getTemplate = 3;
	public static final int OPERTYPE_getIndice = 4;
	public static final int OPERTYPE_existIndice = 5;

	public static final int OPERTYPE_existIndiceType = 6;
	public static final int OPERTYPE_dropIndice = 7;
	public static final int OPERTYPE_deleteTempate = 8;
	public static final int OPERTYPE_updateIndiceMapping = 8;

	public static final Boolean exist = new Boolean(false);


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
		if(stats == null)
			return;
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		longRangeHit.setMax(longValue(stats_.get("max"),0l));
		longRangeHit.setMin(longValue(stats_.get("min"),0l));
		longRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		longRangeHit.setSum(longValue(stats_.get("sum"),0l));
	}
	public static void buildFloatAggHit(FloatAggHit floatRangeHit, Map<String,Object> bucket, String stats){
		floatRangeHit.setKey((String)bucket.get("key"));
		floatRangeHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		if(stats == null)
			return;
		Map<String,Object> stats_ = (Map<String,Object>)bucket.get(stats);
		floatRangeHit.setMax(floatValue(stats_.get("max"),0f));
		floatRangeHit.setMin(floatValue(stats_.get("min"),0f));
		floatRangeHit.setAvg(floatValue(stats_.get("avg"),0f));
		floatRangeHit.setSum(floatValue(stats_.get("sum"),0f));
	}

	public static void buildDoubleAggHit(DoubleAggHit doubleAggHit, Map<String,Object> bucket, String stats){
		doubleAggHit.setKey((String)bucket.get("key"));
		doubleAggHit.setDocCount(longValue(bucket.get("doc_count"),0l));
		if(stats == null)
			return;
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
		if(stats == null)
			return;
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
		if(stats == null)
			return;
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
		if(stats == null)
			return;
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
				List<T> datas = new ArrayList<T>(buckets.size());
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
				List<T> datas = new ArrayList<T>(buckets.size());
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
		ClassUtil.ClassInfo classInfo = null;

		ClassUtil.PropertieDescription  injectAnnotationESId = null;
		ClassUtil.PropertieDescription  injectAnnotationESParentId = null;
		while(iterator.hasNext()){
			Map.Entry<String, Map<String, InnerSearchHits>> entry = iterator.next();
			Map<String, InnerSearchHits> value = entry.getValue();
			InnerSearchHits hitsEntryValue = value.get("hits");
			if(hitsEntryValue != null){
				List<InnerSearchHit> innerSearchHits = hitsEntryValue.getHits();
				if(innerSearchHits != null && innerSearchHits.size() > 0){
					Object source = innerSearchHits.get(0).getSource();
					classInfo = ClassUtil.getClassInfo(source.getClass());

					injectAnnotationESId = classInfo.getEsIdProperty();
					injectAnnotationESParentId = classInfo.getEsParentProperty();
					boolean isESBaseData = ESBaseData.class.isAssignableFrom(classInfo.getClazz());
					boolean isESId = false;
					if(!isESBaseData){
						isESId = ESId.class.isAssignableFrom(source.getClass());
					}
					if(isESBaseData || isESId || (injectAnnotationESId != null && injectAnnotationESId.isEsIdReadSet())
							|| (injectAnnotationESParentId != null && injectAnnotationESParentId.isEsIdReadSet())) {
						for (int i = 0; i < innerSearchHits.size(); i++) {
							InnerSearchHit innerSearchHit = innerSearchHits.get(i);
							source = innerSearchHit.getSource();
							if (source != null) {
								if(injectAnnotationESId != null && injectAnnotationESId.isEsIdReadSet())
									injectAnnotationESId(injectAnnotationESId,  source,innerSearchHit);
								if(injectAnnotationESParentId != null && injectAnnotationESParentId.isEsIdReadSet())
									injectAnnotationESParentId(injectAnnotationESParentId,  source,innerSearchHit);
								if(isESBaseData || isESId)
									injectBaseData(source, innerSearchHit, isESBaseData, isESId);
							}
						}
					}
				}
			}

		}

	}

	/**
	 * 如果对象有ESId注解标识的字段，则注入parent和
	 * @param data
	 */
	private static void injectAnnotationESId(ClassUtil.PropertieDescription  injectAnnotationESId,Object data ,BaseSearchHit hit){
		if(data == null)
			return;
		Object id = hit.getId();
//		ClassUtil.PropertieDescription propertieDescription = classInfo.getEsIdProperty() ;
		_injectAnnotationES( injectAnnotationESId,  data ,  hit,id );

	}

	/**
	 * 如果对象有ESId注解标识的字段，则注入parent和
	 * @param data
	 */
	private static void injectAnnotationESParentId(ClassUtil.PropertieDescription  injectAnnotationESParentId,Object data ,BaseSearchHit hit){
		if(data == null)
			return;
		Object id = hit.getParent();
//		ClassUtil.PropertieDescription propertieDescription = classInfo.getEsParentProperty() ;
		_injectAnnotationES( injectAnnotationESParentId,  data ,  hit,id);

	}
	/**
	 * 如果对象有ESId注解标识的字段，则注入parent和
	 * @param data
	 */
	private static void _injectAnnotationES(ClassUtil.PropertieDescription propertieDescription,Object data ,BaseSearchHit hit,Object id ){
		if(propertieDescription != null && propertieDescription.isEsIdReadSet()){

			try {
				propertieDescription.setValue(data,ValueObjectUtil.typeCast(id,propertieDescription.getPropertyType()));
			} catch (IllegalAccessException e) {
				logger.warn("设置属性失败："+propertieDescription.toString(),e);
			} catch (InvocationTargetException e) {
				logger.warn("设置属性失败："+propertieDescription.toString(),e.getTargetException());
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
					ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(data.getClass());

					ClassUtil.PropertieDescription injectAnnotationESId = classInfo.getEsIdProperty();
					ClassUtil.PropertieDescription injectAnnotationESParentId = classInfo.getEsParentProperty();
					if(injectAnnotationESId != null && injectAnnotationESId.isEsIdReadSet())
						injectAnnotationESId(injectAnnotationESId,  data,hit);
					if(injectAnnotationESParentId != null && injectAnnotationESParentId.isEsIdReadSet())
						injectAnnotationESParentId(injectAnnotationESParentId,  data,hit);
					boolean isESBaseData = ESBaseData.class.isAssignableFrom(classInfo.getClazz());
					boolean isESId = false;
					if(!isESBaseData){
						isESId = ESId.class.isAssignableFrom(classInfo.getClazz());
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
				ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(type);
				ClassUtil.PropertieDescription injectAnnotationESId = classInfo.getEsIdProperty();
				ClassUtil.PropertieDescription injectAnnotationESParentId = classInfo.getEsParentProperty();
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(type);
				boolean isESId = false;
				if(!isESBaseData){
					isESId = ESId.class.isAssignableFrom(type);
				}
				T data = (T) hit.getSource();
				if(injectAnnotationESId != null && injectAnnotationESId.isEsIdReadSet())
					injectAnnotationESId(injectAnnotationESId,  data,hit);
				if(injectAnnotationESParentId != null  && injectAnnotationESParentId.isEsIdReadSet())
					injectAnnotationESParentId(injectAnnotationESParentId,  data,hit);
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
				ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(data.getClass());
				ClassUtil.PropertieDescription injectAnnotationESId = classInfo.getEsIdProperty();
				ClassUtil.PropertieDescription injectAnnotationESParentId = classInfo.getEsParentProperty();

				if(injectAnnotationESId != null && injectAnnotationESId.isEsIdReadSet())
					injectAnnotationESId(injectAnnotationESId,  data,result);
				if(injectAnnotationESParentId != null && injectAnnotationESParentId.isEsIdReadSet())
					injectAnnotationESParentId(injectAnnotationESParentId,  data,result);
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(classInfo.getClazz());
				boolean isESId = false;
				if(!isESBaseData){
					isESId = ESId.class.isAssignableFrom(classInfo.getClazz());
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
			ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(type);
			ClassUtil.PropertieDescription injectAnnotationESId = classInfo.getEsIdProperty();
			ClassUtil.PropertieDescription injectAnnotationESParentId = classInfo.getEsParentProperty();
			if(injectAnnotationESId != null  && injectAnnotationESId.isEsIdReadSet())
				injectAnnotationESId(injectAnnotationESId,  data,hit);
			if(injectAnnotationESParentId != null  && injectAnnotationESParentId.isEsIdReadSet())
				injectAnnotationESParentId(injectAnnotationESParentId,  data,hit);
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

	public static <T> SQLResult<T> buildFetchSQLResult(SQLRestResponse result,Class<T> beanType,SQLResult<T> oldPage) {
		if(oldPage != null)
			result.setColumns(oldPage.getColumns());
		List<T> datas = ResultUtil.buildSQLResult(result, beanType);
		SQLResult<T> _result = new SQLResult<T>();
		_result.setColumns(result.getColumns());
		_result.setRows(result.getRows());
		_result.setCursor(result.getCursor());
		_result.setDatas(datas);
		_result.setBeanType(beanType);
		return _result;
	}

	public static <T> SQLResult<T> buildFetchSQLResult(SQLRestResponse result,Class<T> beanType,ColumnMeta[] metas) {

		result.setColumns(metas);
		List<T> datas = ResultUtil.buildSQLResult(result, beanType);
		SQLResult<T> _result = new SQLResult<T>();
		_result.setColumns(result.getColumns());
		_result.setRows(result.getRows());
		_result.setCursor(result.getCursor());
		_result.setDatas(datas);
		_result.setBeanType(beanType);
		return _result;
	}
	/**
	 * 构建sql查询多条记录对象集合结果
	 * @param result
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> buildSQLResult(SQLRestResponse result,Class<T> type){
		if(result == null || result.getRows() == null || result.getRows().size() == 0)
			return null;
		if(result.getRows() != null && result.getRows().size() > 0) {
			List<Object[]> rows = result.getRows();
			List<T> datas = new ArrayList<T>(rows.size());
			ColumnMeta[] metas = result.getColumns();
			if (Map.class.isAssignableFrom(type)) {
				Map<String,Object> data = null;
				for(int i = 0; i < rows.size(); i ++) {
					Object[] row = rows.get(i);
					data = new HashMap<String,Object>(metas.length);
					for (int j = 0; j < metas.length; j++) {
						data.put(metas[j].getName(),row[j]);
					}
					datas.add((T)data);
				}

			} else {
				DateFormat defaultDateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
				T valueObject = null;
				for(int i = 0; i < rows.size(); i ++) {
					Object[] row = rows.get(i);
					valueObject = buildObject(type,  metas,  row,defaultDateFormat);
					datas.add(valueObject);
				}
			}
			return datas;
		}
		else {
			return null;
		}

	}

	/**
	 * 构建sql查询单条记录对象结果
	 * @param result
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> T buildSQLObject(SQLRestResponse result,Class<T> type){
		if(result == null)
			return null;
		if(result.getRows() != null && result.getRows().size() > 0) {
			List<Object[]> rows = result.getRows();

			ColumnMeta[] metas = result.getColumns();
			if (Map.class.isAssignableFrom(type)) {
				Map<String,Object> data = null;

					Object[] row = rows.get(0);
					data = new HashMap<String,Object>(metas.length);
					for (int j = 0; j < metas.length; j++) {
						data.put(metas[j].getName(),row[j]);
					}
					return (T)data;


			} else {
				T valueObject = null;
				Object[] row = rows.get(0);
				DateFormat defaultDateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
				valueObject = buildObject(type,  metas,  row,defaultDateFormat);
				return valueObject;
			}

		}
		else {
			return null;
		}

	}

	private static <T> T buildObject(Class<T> valueObjectType,ColumnMeta[] meta,Object[] row,DateFormat defaultDateFormat){
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(valueObjectType);
		T valueObject = null;
		if(!beanInfo.isPrimary())
		{
			try {
				valueObject = valueObjectType.newInstance();
			} catch (InstantiationException e1) {
				throw new ElasticSearchException(e1);
			} catch (IllegalAccessException e1) {
				throw new ElasticSearchException(e1);
			}
			//			BeanInfo beanInfo;
			//			try {
			//
			//				beanInfo = Introspector.getBeanInfo(valueObjectType);
			//			} catch (IntrospectionException e1) {
			//				throw new NestedSQLException(e1);
			//			}

			List<ClassUtil.PropertieDescription> attributes = beanInfo.getPropertyDescriptors();

			for (int n = 0; attributes != null && n < attributes.size(); n++) {
				ClassUtil.PropertieDescription attribute = attributes.get(n);
				ColumnWraper cl = attribute.getColumn();
				if(attribute.getIgnoreORMapping() != null || (cl != null && cl.ignorebind()))
					continue;
				String attrName = attribute.getName();


				//				if(attrName.equals("class"))
				//					continue;
				String annotationName = null;
				if(BigFile.class.isAssignableFrom(attribute.getPropertyType()) )//不支持大字段转换为BigFile接口
					continue;

				ColumnEditorInf editor = null;

				try {
					if(cl != null)
					{
						editor = cl.editor();
						annotationName = cl.name();
						if(annotationName != null && !annotationName.equals(""))
						{
							attrName = annotationName;
						}
					}
				} catch (Exception e1) {
					logger.info(attribute.getName() + " is not a field of bean[" +valueObjectType.getClass().getCanonicalName() + "].");
				}
				for (int i = 0; i < meta.length; i++) {
					ColumnMeta columnMeta = meta[i];
					String columnName = columnMeta.getName();
					if(!attrName.equals(columnName))
						continue;
					Class type = attribute.getPropertyType();
					Object propsVal = null;
					Object rowValue = row[i];
					try {
						//					propsVal = ValueExchange.getValueFromResultSet(rs, columnName,
						//														stmtInfo.getMeta().getColumnType(i + 1),
						//														type,
						//														stmtInfo.getDbname());
						propsVal = getValueFromRow(rowValue,
								type,
								editor,cl,columnMeta,defaultDateFormat);

					} catch (Exception e) {
						StringBuilder err = new StringBuilder(
								"Build ValueObject for ResultSet Get Column[")
								.append(columnName).append("] from  ResultSet to ").append(valueObjectType.getClass().getCanonicalName()).append(".")
								.append(attrName).append("[")
								.append(type.getName()).append("] failed:").append(
										e.getMessage());
						logger.error(err.toString(), e);
						break;
					}

					try {
						if(attribute.canwrite())
						{
							attribute.setValue(valueObject, propsVal);
						}
						//						attribute.getWriteMethod().invoke(valueObject,
						//								new Object[] { propsVal });
						break;
					} catch (Exception e) {
						StringBuilder err = new StringBuilder(
								"Build ValueObject for ResultSet Get Column[")
								.append(columnName).append("] from  ResultSet to ").append(valueObject).append(".")
								.append(attrName).append("[")
								.append(type.getName()).append("] failed:").append(
										e.getMessage());
						//						System.out.println(err);
						logger.error(err.toString(), e);
						break;
					}

				}

			}
		}
		else
		{

			valueObject = (T)getValueFromRow(row[0],
					valueObjectType,
					 (ColumnEditorInf)null,(ColumnWraper)null,meta[0],defaultDateFormat);
		}
		return valueObject;
	}

	public static Object getValueFromRow(Object value, Class javaType,ColumnEditorInf editor,ColumnWraper columnWraper,ColumnMeta columnMeta,DateFormat defaultDateFormat) {


		if(editor == null  || editor instanceof FieldToColumnEditor)
		{
			if(value == null)
				return ValueObjectUtil.getDefaultValue(javaType);

			return convert(value, value.getClass(), javaType,  columnWraper,  columnMeta,  defaultDateFormat);
		}
		else
		{
			return editor.getValueFromObject(columnWraper,value);
		}


	}

	public static boolean isDateType(ColumnMeta columnMeta){
		return columnMeta.getType().equals("date");
	}
	private static Object convert(Object value, Class type, Class javaType,ColumnWraper columnWraper,ColumnMeta columnMeta,DateFormat defaultDateFormat) {
		try {
			if(javaType == null || value == null)
				return ValueObjectUtil.getDefaultValue(javaType);
			if(columnWraper == null){
				if(!isDateType(columnMeta)) {
					return ValueObjectUtil.typeCast(value, value.getClass(), javaType);
				}
				else{
					return ValueObjectUtil.typeCastWithDateformat(value, value.getClass(), javaType,defaultDateFormat);
				}
			}
			else {
				if(!isDateType(columnMeta)) {
					return ValueObjectUtil.typeCast(value, value.getClass(), javaType);
				}
				else{
					if(columnWraper.getDateFormateMeta() == null){
						return ValueObjectUtil.typeCastWithDateformat(value, value.getClass(), javaType,defaultDateFormat);
					}
					else{
						DateFormateMeta dateFormateMeta = columnWraper.getDateFormateMeta();
						return ValueObjectUtil.typeCastWithDateformat(value, value.getClass(), javaType, dateFormateMeta.toDateFormat());
					}
				}

			}
		} catch (NumberFormatException e) {
			throw new ElasticSearchException(e);
		} catch (IllegalArgumentException e) {
			throw new ElasticSearchException(e);
		} catch (NoSupportTypeCastException e) {
			throw new ElasticSearchException(e);
		} catch (Exception e) {
			throw new ElasticSearchException(e);
		}
//		return null;
	}
	public static <T> ESDatas<T> buildESDatas(RestResponse result,Class<T> type){
//		if(result instanceof ErrorResponse){
//			throw new ElasticSearchException(SimpleStringUtil.object2json(result));
//		}
		ESDatas<T> datas = new ESDatas<T>();
		RestResponse restResponse = (RestResponse)result;
		datas.setRestResponse(restResponse);
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
				ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(obj.getClass());
				ClassUtil.PropertieDescription injectAnnotationESId = classInfo.getEsIdProperty();
				ClassUtil.PropertieDescription injectAnnotationESParentId = classInfo.getEsParentProperty();
				boolean isESBaseData = ESBaseData.class.isAssignableFrom(classInfo.getClazz());
				boolean isESId = false;
				if (!isESBaseData) {
					isESId = ESId.class.isAssignableFrom(classInfo.getClazz());
				}

				for (int i = 0; i < searchHits.size(); i++) {
					SearchHit hit = searchHits.get(i);

					//处理源对象
					Object data = hit.getSource();
					if (data != null) {
						if(injectAnnotationESId != null && injectAnnotationESId.isEsIdReadSet())
							injectAnnotationESId(injectAnnotationESId,  data,hit);
						if(injectAnnotationESParentId != null&& injectAnnotationESParentId.isEsIdReadSet())
							injectAnnotationESParentId(injectAnnotationESParentId,  data,hit);
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
				ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(type);
				ClassUtil.PropertieDescription injectAnnotationESId = classInfo.getEsIdProperty();
				ClassUtil.PropertieDescription injectAnnotationESParentId = classInfo.getEsParentProperty();
				boolean isESId = false;
				if (!isESBaseData) {
					isESId = ESId.class.isAssignableFrom(type);
				}
				T data = null;
				for (SearchHit hit : searchHits) {
					data = (T) hit.getSource();
					hits.add(data);
					if (data != null) {
						if(injectAnnotationESId != null && injectAnnotationESId.isEsIdReadSet())
							injectAnnotationESId(injectAnnotationESId,  data,hit);
						if(injectAnnotationESParentId != null && injectAnnotationESParentId.isEsIdReadSet())
							injectAnnotationESParentId(injectAnnotationESParentId,  data,hit);
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

	/**
	 *
	 * @param e
	 * @param type
	 * @param operType 0:getDocument 1:updateDocument 2:deleteDocument 3:getTemplate 4:getIndice 5:existIndice 6 existIndiceType 7:dropIndice 8:deleteTempate
	 * @param <T>
	 * @return
	 */
	public static <T> T hand404HttpRuntimeException(ElasticSearchException e,Class<T> type,int operType){
		Throwable throwable = e.getCause();
		if(throwable == null || !(throwable instanceof HttpRuntimeException)){
			if(e.getHttpStatusCode() == 404){
				String errorInfo = e.getMessage();
				if(operType == ResultUtil.OPERTYPE_getDocument) {
//						Map data = SimpleStringUtil.json2Object(errorInfo, HashMap.class);
//						Boolean found = (Boolean) data.get("found");
//						if (found != null && found == false)
//						{
					return (T) null;
//						}
				}
				else if(operType == ResultUtil.OPERTYPE_getTemplate) {
					return (T) null;

				}
				else if(operType == ResultUtil.OPERTYPE_getIndice) {
					return (T) null;

				}
				else if(operType == ResultUtil.OPERTYPE_existIndice) {
					return (T)ResultUtil.exist;

				}
				else if(operType == ResultUtil.OPERTYPE_existIndiceType) {
					return (T)ResultUtil.exist;

				}
				else if(operType == ResultUtil.OPERTYPE_dropIndice) {
//						return (T)ResultUtil.exist;

				}
				else if(operType == ResultUtil.OPERTYPE_deleteTempate) {
//						return (T)ResultUtil.exist;

				}
				else if(String.class.isAssignableFrom(type)){
					return (T)errorInfo;
				}
			}
		}
		else{

			HttpRuntimeException httpRuntimeException = (HttpRuntimeException)throwable;
			if(httpRuntimeException.getHttpStatusCode() == 404){
				String errorInfo = httpRuntimeException.getMessage();

				if(operType == ResultUtil.OPERTYPE_getDocument) {
//						Map data = SimpleStringUtil.json2Object(errorInfo, HashMap.class);
//						Boolean found = (Boolean) data.get("found");
//						if (found != null && found == false)
//						{
					return (T) null;
//						}
				}
				else if(operType == ResultUtil.OPERTYPE_getTemplate) {
					return (T) null;

				}
				else if(operType == ResultUtil.OPERTYPE_getIndice) {
					return (T) null;

				}
				else if(operType == ResultUtil.OPERTYPE_existIndice) {
					return (T)ResultUtil.exist;

				}
				else if(operType == ResultUtil.OPERTYPE_existIndiceType) {
					return (T)ResultUtil.exist;

				}
				else if(operType == ResultUtil.OPERTYPE_dropIndice) {
//						return (T)ResultUtil.exist;

				}
				else if(operType == ResultUtil.OPERTYPE_deleteTempate) {
//						return (T)ResultUtil.exist;

				}
				else if(String.class.isAssignableFrom(type)){
					return (T)errorInfo;
				}

			}
		}
		throw e;
	}
}
