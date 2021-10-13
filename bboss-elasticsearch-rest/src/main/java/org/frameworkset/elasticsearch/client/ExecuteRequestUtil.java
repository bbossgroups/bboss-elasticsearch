package org.frameworkset.elasticsearch.client;
/**
 * Copyright 2020 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.SliceRunTask;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.entity.RestResponse;
import org.frameworkset.elasticsearch.handler.ElasticSearchResponseHandler;
import org.frameworkset.elasticsearch.scroll.*;
import org.frameworkset.elasticsearch.scroll.thread.ScrollTask;
import org.frameworkset.elasticsearch.serial.ESInnerHitSerialThreadLocal;
import org.frameworkset.elasticsearch.serial.SerialContext;
import org.frameworkset.elasticsearch.template.ESTemplateHelper;
import org.frameworkset.elasticsearch.template.ESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2021/10/13 11:04
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class ExecuteRequestUtil {
	private static Logger logger = LoggerFactory.getLogger(ExecuteRequestUtil.class);
	/**
	 * scroll search
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-scroll.html
	 * @param scroll
	 * @param scrollId
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public static  <T> ESDatas<T> searchScroll(ElasticSearchRestClient client,String scroll,String scrollId ,Class<T> type) throws ElasticSearchException{

		if(!client.isV1()) {
			StringBuilder entity = new StringBuilder();
			entity.append("{\"scroll\" : \"").append(scroll).append("\",\"scroll_id\" : \"").append(scrollId).append("\"}");
			RestResponse result = client.executeRequest("_search/scroll", entity.toString(), new ElasticSearchResponseHandler(type));
			return ResultUtil.buildESDatas(result,type);
		}
		else {
			StringBuilder path = new StringBuilder();
			path.append("_search/scroll?scroll=").append( scroll ).append( "&scroll_id=" ).append( scrollId);
			RestResponse result = client.executeHttp(path.toString(), ClientUtil.HTTP_GET, new ElasticSearchResponseHandler(type));
			return ResultUtil.buildESDatas(result, type);
		}
	}
	public static <T> void runSliceScrollTask(List<Future> tasks,ScrollHandler<T> _scrollHandler,
									   ESDatas<T> sliceResponse, HandlerInfo handlerInfo,
									   SliceScrollResultInf<T> sliceScrollResult,
									   ExecutorService executorService){
		ScrollTask<T> scrollTask = new ScrollTask<T>(_scrollHandler, sliceResponse, handlerInfo,sliceScrollResult);
		tasks.add(executorService.submit(scrollTask));
	}
	public static <T> void _doSliceScroll(ElasticSearchRestClient client,int i, String path,
									String entity,
									String scroll, Class<T> type,

									SliceScrollResultInf<T> sliceScrollResult, boolean parallel) throws Exception {
		List<Future> tasks = null;
		try{
			if(sliceScrollResult.isBreaked()){ //other slice query has breaked continue to slice scroll query.
				return;
			}
			RestResponse result = client.executeRequest(path,entity,   new ElasticSearchResponseHandler( type));
			ESDatas<T> sliceResponse = ResultUtil.buildESDatas(result,type);
			int taskId = 0;
			List<T> sliceDatas = sliceResponse.getDatas();
			String scrollId = sliceResponse.getScrollId();
			ExecutorService executorService = parallel ?client.getScrollQueryExecutorService():null;
//			System.out.println("sliceDatas:"+i+":" + sliceDatas);
//			System.out.println("scrollId:"+i+":" + scrollId);
			Set<String> scrollIds = null;
			if (scrollId != null) {
				scrollIds = new TreeSet<String>();
				scrollIds.add(scrollId);
			}
			boolean useDefaultHandler = false;
			if (sliceDatas != null && sliceDatas.size() > 0) {//每页100条记录，迭代scrollid，遍历scroll分页结果
				tasks = new ArrayList<Future>();
				ScrollHandler<T> _scrollHandler = sliceScrollResult.getScrollHandler();
				HandlerInfo handlerInfo = new HandlerInfo();
				handlerInfo.setTaskId(taskId);
//				handlerInfo.setScrollId(scrollId);
				handlerInfo.setSliceId(i);
				ScrollTask<T> scrollTask = null;
				taskId ++;
				if (_scrollHandler == null) {
					useDefaultHandler = true;
					_scrollHandler = sliceScrollResult.setScrollHandler(sliceResponse,handlerInfo);
					sliceScrollResult.incrementSize(sliceDatas.size());//统计实际处理的文档数量
				}
				else {
					if(parallel) {
//						scrollTask = new  ScrollTask<T>(_scrollHandler, sliceResponse, handlerInfo,sliceScrollResult);
//						tasks.add(executorService.submit(scrollTask));
						runSliceScrollTask( tasks,  _scrollHandler,
								sliceResponse,   handlerInfo,
								sliceScrollResult,
								executorService);
					}
					else {
						_scrollHandler.handle(sliceResponse, handlerInfo);
						sliceScrollResult.incrementSize(sliceDatas.size());//统计实际处理的文档数量
					}
					sliceScrollResult.setSliceResponse(sliceResponse);
				}

				ESDatas<T> _sliceResponse = null;
				List<T> _sliceDatas = null;
				do {
					if(sliceScrollResult.isBreaked()){
						break;
					}
					_sliceResponse = searchScroll(client,scroll, scrollId, type);
					String sliceScrollId = _sliceResponse.getScrollId();
					if (sliceScrollId != null)
						scrollIds.add(sliceScrollId);
					//处理完毕后清除scroll上下文信息

					_sliceDatas = _sliceResponse.getDatas();
					if (_sliceDatas == null || _sliceDatas.size() == 0) {
						break;
					}
					handlerInfo = new HandlerInfo();
					handlerInfo.setTaskId(taskId);
					handlerInfo.setSliceId(i);
					handlerInfo.setScrollId(scrollId);
					taskId ++;
					scrollId = sliceScrollId;
					if(!useDefaultHandler ) {
						if(parallel) {
//							scrollTask = new ScrollTask<T>(_scrollHandler, sliceResponse, handlerInfo,sliceScrollResult);
//							tasks.add(executorService.submit(scrollTask));
							runSliceScrollTask( tasks,  _scrollHandler,
									_sliceResponse,   handlerInfo,
									sliceScrollResult,
									executorService);
						}
						else{
							_scrollHandler.handle(_sliceResponse, handlerInfo);
							sliceScrollResult.incrementSize(_sliceDatas.size());//统计实际处理的文档数量
						}
					}
					else {
						_scrollHandler.handle(_sliceResponse, handlerInfo);
						sliceScrollResult.incrementSize(_sliceDatas.size());//统计实际处理的文档数量
					}

				} while (true);
			}
			if(tasks != null && tasks.size() > 0)
				waitTasksComplete(tasks);
			//处理完毕后清除scroll上下文信息
			if(scrollIds != null && scrollIds.size() > 0) {
				try {
					deleteScrolls(client,scrollIds);
				}
				catch (Exception e){

				}
//			System.out.println(scrolls);
			}

		} catch (ElasticSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new ElasticSearchException("slice query task["+i+"] failed:",e);
		}
		finally {
			if(tasks != null && tasks.size() > 0)
				waitTasksComplete(tasks);
		}
	}
	public static String deleteScrolls(ElasticSearchRestClient client,List<String> scrollIds) throws ElasticSearchException{
		if(scrollIds == null || scrollIds.size() == 0 )
			return null;
		return deleteScrolls( client,scrollIds.iterator());
	}
	/**
	 * 清理scrollId
	 * @param scrollIds
	 * @return
	 * @throws ElasticSearchException
	 */
	public static String deleteScrolls(ElasticSearchRestClient client,Set<String> scrollIds) throws ElasticSearchException{
		if(scrollIds == null || scrollIds.size() == 0 )
			return null;
		return deleteScrolls(  client,scrollIds.iterator());
	}
	/**
	 * 清理scrollId
	 * @param scrollIds
	 * @return
	 * @throws ElasticSearchException
	 */
	public static String deleteScrolls(ElasticSearchRestClient client,String [] scrollIds) throws ElasticSearchException{
		if(scrollIds == null || scrollIds.length == 0)
			return null;
		if(!client.isV1()) {
			StringBuilder entity = new StringBuilder();
			entity.append("{\"scroll_id\" : [");
			for (int i = 0; i < scrollIds.length; i++) {
				String scrollId = scrollIds[i];
				if (i > 0)
					entity.append(",");
				entity.append("\"").append(scrollId).append("\"");
			}
			entity.append("]}");
			String result = client.executeHttp("_search/scroll", entity.toString(), ClientUtil.HTTP_DELETE);
			return result;
		}
		else{
//			for (int i = 0; i < scrollIds.length; i++) {
//				String scrollId = scrollIds[i];
//				this.client.executeHttp("_search/scroll?scroll_id="+scrollId, ClientUtil.HTTP_DELETE);
//			}

			if(logger.isTraceEnabled()){
				logger.trace(new StringBuilder().append("Elasticsearch ").append(client.getEsVersion() )
						.append( " do not support delete scrollId.").toString());
			}
			return "";
		}
	}
	private static String deleteScrolls(ElasticSearchRestClient client,Iterator<String> scrollIds) throws ElasticSearchException{

		if(!client.isV1()) {
			StringBuilder entity = new StringBuilder();
			entity.append("{\"scroll_id\" : [");

			int i = 0;
			for (; scrollIds.hasNext(); ) {

				String scrollId = scrollIds.next();
				if (i > 0)
					entity.append(",");
				i ++;
				entity.append("\"").append(scrollId).append("\"");
			}
			entity.append("]}");
			String result = client.executeHttp("_search/scroll", entity.toString(), ClientUtil.HTTP_DELETE);
			return result;
		}
		else{
//			String result = null;
//////			for (int i = 0; i < scrollIds.size(); i++) {
//////				String scrollId = scrollIds.get(i);
//////				result = this.client.executeHttp("_search/scroll?scroll_id="+scrollId, ClientUtil.HTTP_DELETE);
//////			}
//////			return result;
			if(logger.isTraceEnabled()){
				logger.trace(new StringBuilder().append("Elasticsearch ").append(client.getEsVersion() )
						.append( " do not support delete scrollId.").toString());
			}
			return "";

		}
	}
	public static void waitTasksComplete(final List<Future> tasks){

		for (Future future : tasks) {
			try {
				future.get();
			} catch (ExecutionException e) {
				logger.error("",e);
			}catch (Exception e) {
				logger.error("",e);
			}
		}
		tasks.clear();

	}
	public static  <T> ESDatas<T> _scrollSlice(ElasticSearchRestClient client,ESUtil esUtil,final String path, final String dslTemplate, final Map params ,
											   final String scroll  , final Class<T> type,
											   ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		long starttime = System.currentTimeMillis();
		//scroll slice分页检索
		Integer mx = (Integer) params.get("sliceMax");
		if(mx == null)
			throw new ElasticSearchException("Slice parameters exception: must set sliceMax in params!");
		final int max = mx.intValue();
//		final CountDownLatch countDownLatch = new CountDownLatch(max);//线程任务完成计数器，每个线程对应一个sclice,每运行完一个slice任务,countDownLatch计数减去1

		String _path = path.indexOf('?') < 0 ? new StringBuilder().append(path).append("?scroll=").append(scroll).toString() :
				new StringBuilder().append(path).append("&scroll=").append(scroll).toString();
		//辅助方法，用来累计每次scroll获取到的记录数
		SliceScrollResult sliceScrollResult = new SliceScrollResult();
		if(scrollHandler != null)
			sliceScrollResult.setScrollHandler(scrollHandler);
		for (int j = 0; j < max; j++) {//启动max个线程，并行处理每个slice任务
			if(sliceScrollResult.isBreaked())
				break;
			int i = j;
			try {
				params.put("sliceId", i);

				_doSliceScroll(client, i, _path,
						ESTemplateHelper.evalTemplate(esUtil,dslTemplate, params),
						scroll, type,
						sliceScrollResult,false);


			} catch (ElasticSearchException e) {
				throw e;
			} catch (Exception e) {
				throw new ElasticSearchException("slice query task["+i+"] failed:",e);
			}
		}

		//打印处理耗时和实际检索到的数据
		if(logger.isDebugEnabled()) {
			long endtime = System.currentTimeMillis();
			logger.debug("Slice scroll query耗时：" + (endtime - starttime) + ",realTotalSize：" + sliceScrollResult.getRealTotalSize());
		}


		sliceScrollResult.complete();
		return sliceScrollResult.getSliceResponse();
	}
	public static <T> void runSliceTask(ClientInterface clientInterface,int sliceId,String path,String sliceDsl,  String scroll,  Class<T> type,  ParallelSliceScrollResult sliceScrollResult,ExecutorService executorService,List<Future> tasks,SerialContext serialContext ){
		SliceRunTask<T> sliceRunTask = new SliceRunTask<T>(clientInterface,sliceId,path,sliceDsl,scroll,type,sliceScrollResult,   serialContext);
		tasks.add(executorService.submit(sliceRunTask));
	}
	/**
	 * 并行检索索引所有数据
	 * @param path
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param max 并行度，线程数
	 * @param sliceScroll
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public static <T> ESDatas<T> _slice(ClientInterface clientInterface,String path,  ScrollHandler<T> scrollHandler,final Class<T> type,int max,
									final String scroll,SliceScroll sliceScroll) throws ElasticSearchException{



		long starttime = System.currentTimeMillis();
		//scroll slice分页检索

//		final CountDownLatch countDownLatch = new CountDownLatch(max);//线程任务完成计数器，每个线程对应一个sclice,每运行完一个slice任务,countDownLatch计数减去1

		final String _path = path.indexOf('?') < 0 ? new StringBuilder().append(path).append("?scroll=").append(scroll).toString() :
				new StringBuilder().append(path).append("&scroll=").append(scroll).toString();

		ExecutorService executorService = clientInterface.getClient().getSliceScrollQueryExecutorService();
		List<Future> tasks = new ArrayList<Future>();
		//辅助方法，用来累计每次scroll获取到的记录数
		final ParallelSliceScrollResult sliceScrollResult = new ParallelSliceScrollResult();
		if(scrollHandler != null)
			sliceScrollResult.setScrollHandler(scrollHandler);

		try {
//			SliceRunTask<T> sliceRunTask = null;
			SerialContext serialContext = ESInnerHitSerialThreadLocal.buildSerialContext();
			for (int j = 0; j < max; j++) {//启动max个线程，并行处理每个slice任务
				if(sliceScrollResult.isBreaked())
					break;
				String sliceDsl = sliceScroll.buildSliceDsl(j,max);
//				final String sliceDsl = builder.append("{\"slice\": {\"id\": ").append(i).append(",\"max\": ")
//									.append(max).append("},\"size\":").append(fetchSize).append(",\"query\": {\"match_all\": {}}}").toString();

				runSliceTask(clientInterface,j,_path,sliceDsl,scroll,type,sliceScrollResult,
						executorService,tasks,serialContext );
			}
		}
		finally {
			waitTasksComplete(tasks);
		}

		//打印处理耗时和实际检索到的数据
		if(logger.isDebugEnabled()) {
			long endtime = System.currentTimeMillis();
			logger.debug("Slice scroll query耗时：" + (endtime - starttime) + ",realTotalSize：" + sliceScrollResult.getRealTotalSize());
		}


		sliceScrollResult.complete();
		return sliceScrollResult.getSliceResponse();
	}

	public static <T> ESDatas<T> scrollSliceParallel(ClientInterface clientInterface,final ESUtil esUtil,String path,final String dslTemplate,final Map params ,
											  final String scroll  ,final Class<T> type,
											  ScrollHandler<T> scrollHandler) throws ElasticSearchException{
		Integer mx = (Integer) params.get("sliceMax");
		if(mx == null)
			throw new ElasticSearchException("Slice parameters exception: must set sliceMax in params!");
		final int max = mx.intValue();
		SliceScroll sliceScroll = new SliceScroll() {
			@Override
			public String buildSliceDsl(int sliceId, int max) {
				final Map _params = new HashMap();
				_params.putAll(params);
				_params.put("sliceId", sliceId);
				String sliceDsl = ESTemplateHelper.evalTemplate(esUtil,dslTemplate, _params);
				return sliceDsl;
//				return buildSliceDsl(i,max, params, dslTemplate);
			}
		};
		return ExecuteRequestUtil._slice(clientInterface,path,  scrollHandler,type,max, scroll, sliceScroll);

	}

	public static String _addDocuments(ElasticSearchRestClient client, ESUtil esUtil,String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		for(Object bean:beans) {
			ESTemplateHelper.evalBuilkTemplate(esUtil,builder,indexName,indexType,addTemplate,bean,"index",client.isUpper7());
		}
		if(refreshOption == null)
			return client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		else
			return client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
	}
	public static String _updateDocuments(ElasticSearchRestClient client, ESUtil esUtil,String indexName, String indexType,String updateTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		for(Object bean:beans) {
			ESTemplateHelper.evalBuilkTemplate(esUtil,builder,indexName,indexType,updateTemplate,bean,"update",client.isUpper7());
		}
		if(refreshOption != null && !refreshOption.equals(""))
			return client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
		else
			return client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
	}
}
