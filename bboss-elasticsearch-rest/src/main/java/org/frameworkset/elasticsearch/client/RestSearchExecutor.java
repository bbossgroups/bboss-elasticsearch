package org.frameworkset.elasticsearch.client;
/**
 * Copyright 2008 biaoping.yin
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

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.SlowDsl;
import org.frameworkset.elasticsearch.handler.ESStringResponseHandler;
import org.frameworkset.spi.remote.http.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/7/15 21:03
 * @author biaoping.yin
 * @version 1.0
 */
public class RestSearchExecutor {
	private static final Logger logger = LoggerFactory.getLogger(RestSearchExecutor.class);
	private Map<String, String> headers;
	private String httpPool;
	private ElasticSearchClient elasticSearchClient;
	public RestSearchExecutor(Map<String, String> headers,String httpPool,ElasticSearchClient elasticSearchClient){
		this.headers = headers;
		this.httpPool = httpPool;
		this.elasticSearchClient = elasticSearchClient;
	}
	public String execute(String url,String entity,ESStringResponseHandler responseHandler) throws Exception {
		Integer slowDslThreshold = elasticSearchClient.slowDslThreshold();
		if(slowDslThreshold == null) {
			String response = HttpRequestUtil.sendJsonBody(httpPool, entity, url, headers, responseHandler);
			return response;
		}
		else{
			long start = System.currentTimeMillis();
			try {
				String response = HttpRequestUtil.sendJsonBody(httpPool, entity, url, headers, responseHandler);
				return response;
			}
			finally {
				long end = System.currentTimeMillis();
				long time = end - start;
				if (time > slowDslThreshold.intValue()) {
					if (elasticSearchClient.getSlowDslCallback() == null) {
						if(logger.isWarnEnabled()) {
							logger.warn("Slow request[{}] took time:{} ms > slowDslThreshold[{} ms], use DSL[{}]", url, time, slowDslThreshold.intValue(), RestSearchExecutorUtil.chunkEntity(entity));

						}
					}else {
						SlowDsl slowDsl = new SlowDsl();
						slowDsl.setUrl(url);
						slowDsl.setTime(time);
						slowDsl.setSlowDslThreshold(slowDslThreshold);
						slowDsl.setEntity(entity);
						slowDsl.setStartTime(new Date(start));
						slowDsl.setEndTime(new Date(end));
						elasticSearchClient.getSlowDslCallback().slowDslHandle( slowDsl);
					}

				}
			}
		}

	}
	/**

	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeHttp(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
//		return _executeHttp(  url,   entity,  action, responseHandler);
		Integer slowDslThreshold = elasticSearchClient.slowDslThreshold();
		if(slowDslThreshold == null) {
			return RestSearchExecutorUtil.__executeHttp(    httpPool,  headers,  url,   entity,  action,  responseHandler);
		}

		else{
			long start = System.currentTimeMillis();
			try {
				return RestSearchExecutorUtil.__executeHttp(    httpPool,  headers,  url,   entity,  action,  responseHandler);
			}
			finally {
				long end = System.currentTimeMillis();
				long time = end - start;
				if (time > slowDslThreshold.intValue()) {
					if (elasticSearchClient.getSlowDslCallback() == null) {
						if(logger.isWarnEnabled()) {
							logger.warn("Slow request[{}] action[{}] took time:{} ms > slowDslThreshold[{} ms], use DSL[{}]", url,action, time, slowDslThreshold.intValue(),  RestSearchExecutorUtil.chunkEntity(entity));

						}
					}else {
						SlowDsl slowDsl = new SlowDsl();
						slowDsl.setUrl(url);
						slowDsl.setAction(action);
						slowDsl.setTime(time);
						slowDsl.setSlowDslThreshold(slowDslThreshold);
						slowDsl.setEntity(entity);
						slowDsl.setStartTime(new Date(start));
						slowDsl.setEndTime(new Date(end));
						elasticSearchClient.getSlowDslCallback().slowDslHandle( slowDsl);
					}

				}
			}
		}
	}


	/**

	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T discoverHost(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
		Integer slowDslThreshold = elasticSearchClient.slowDslThreshold();
		if(slowDslThreshold == null) {
			return RestSearchExecutorUtil.__executeHttp(    httpPool,  headers,  url,   entity,  action,  responseHandler);
		}

		else{
			long start = System.currentTimeMillis();
			try {
				return RestSearchExecutorUtil.__executeHttp(    httpPool,  headers,  url,   entity,  action,  responseHandler);
			}
			finally {
				long end = System.currentTimeMillis();
				long time = end - start;
				if (time > slowDslThreshold.intValue()) {
					if (elasticSearchClient.getSlowDslCallback() == null) {
						if(logger.isWarnEnabled()) {
							logger.warn("Slow request[{}] action[{}] took time:{} ms > slowDslThreshold[{} ms], use DSL[{}]", url,action, time, slowDslThreshold.intValue(), RestSearchExecutorUtil.chunkEntity(entity));

						}
					}else {
						SlowDsl slowDsl = new SlowDsl();
						slowDsl.setUrl(url);
						slowDsl.setAction(action);
						slowDsl.setTime(time);
						slowDsl.setSlowDslThreshold(slowDslThreshold);
						slowDsl.setEntity(entity);
						slowDsl.setStartTime(new Date(start));
						slowDsl.setEndTime(new Date(end));
						elasticSearchClient.getSlowDslCallback().slowDslHandle( slowDsl);
					}

				}
			}
		}
	}




	/**
	 * @param url
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public String executeSimpleRequest(String url, String entity,ESStringResponseHandler responseHandler) throws Exception {
		Integer slowDslThreshold = elasticSearchClient.slowDslThreshold();
		if(slowDslThreshold == null) {
			String response = null;
			if (entity == null) {
				response = HttpRequestUtil.httpPostforString(httpPool, url, null, headers, responseHandler);
			} else {
				response = HttpRequestUtil.sendJsonBody(httpPool, entity, url, headers, responseHandler);
			}

			return response;
		}
		else {
			long start = System.currentTimeMillis();
			try {
				String response = null;
				if (entity == null) {
					response = HttpRequestUtil.httpPostforString(httpPool, url, null, headers, responseHandler);
				} else {
					response = HttpRequestUtil.sendJsonBody(httpPool, entity, url, headers, responseHandler);
				}

				return response;
			} finally {
				long end = System.currentTimeMillis();
				long time = end - start;
				if (time > slowDslThreshold.intValue()) {
					if (elasticSearchClient.getSlowDslCallback() == null) {
						if(logger.isWarnEnabled()) {
							logger.warn("Slow request[{}] took time:{} ms > slowDslThreshold[{} ms], use DSL[{}]", url, time, slowDslThreshold.intValue(), RestSearchExecutorUtil.chunkEntity(entity));

						}
					}else {
						SlowDsl slowDsl = new SlowDsl();
						slowDsl.setUrl(url);
						slowDsl.setTime(time);
						slowDsl.setSlowDslThreshold(slowDslThreshold);
						slowDsl.setEntity(entity);
						slowDsl.setStartTime(new Date(start));
						slowDsl.setEndTime(new Date(end));
						elasticSearchClient.getSlowDslCallback().slowDslHandle(  slowDsl);
					}

				}
			}
		}
	}
	/**
	 * @param entity
	 * @param responseHandler
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeRequest(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
		Integer slowDslThreshold = elasticSearchClient.slowDslThreshold();
		if(slowDslThreshold == null) {

			return RestSearchExecutorUtil._executeRequest(httpPool,headers,url, entity,action, responseHandler);
		}

		else{
			long start = System.currentTimeMillis();
			try {
				return RestSearchExecutorUtil._executeRequest(httpPool,headers,url, entity,action, responseHandler);
			}
			finally {
				long end = System.currentTimeMillis();
				long time = end - start;
				if (time > slowDslThreshold.intValue()) {
					if (elasticSearchClient.getSlowDslCallback() == null) {
						if(logger.isWarnEnabled()) {
							logger.warn("Slow request[{}] action[{}] took time:{} ms > slowDslThreshold[{} ms],use DSL[{}] ", url,action, time, slowDslThreshold.intValue(), RestSearchExecutorUtil.chunkEntity(entity));

						}
					}else {
						SlowDsl slowDsl = new SlowDsl();
						slowDsl.setUrl(url);
						slowDsl.setAction(action);
						slowDsl.setTime(time);
						slowDsl.setSlowDslThreshold(slowDslThreshold);
						slowDsl.setEntity(entity);
						slowDsl.setStartTime(new Date(start));
						slowDsl.setEndTime(new Date(end));
						elasticSearchClient.getSlowDslCallback().slowDslHandle(  slowDsl);
					}

				}
			}
		}

	}



	public String getClusterVersionInfo(){
		return this.elasticSearchClient.getClusterVersionInfo();
	}

}
