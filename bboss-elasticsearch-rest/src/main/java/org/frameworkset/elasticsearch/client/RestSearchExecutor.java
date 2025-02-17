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
import org.frameworkset.elasticsearch.entity.LogDsl;
import org.frameworkset.elasticsearch.handler.ESStringResponseHandler;
import org.frameworkset.spi.remote.http.HttpRequestProxy;
import org.frameworkset.spi.remote.http.URLResponseHandler;
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
//	private Map<String, String> headers;
	private String httpPool;
	private ElasticSearchClient elasticSearchClient;
	private String discoverHttpPool;
	public RestSearchExecutor(String httpPool,String discoverHttpPool, ElasticSearchClient elasticSearchClient){
		this.httpPool = httpPool;
		this.discoverHttpPool = discoverHttpPool;
		this.elasticSearchClient = elasticSearchClient;
	}
	private void logDsl(long start,String url,String action,String dsl,int resultCode ){
		LogDslCallback logDslCallback = null;
		LogDslCallback logSlowDslCallback = null;
		Integer slowDslThreshold = elasticSearchClient.slowDslThreshold();

		logDslCallback = elasticSearchClient.getLogDslCallback();

		if(slowDslThreshold != null && slowDslThreshold > 0) {
			logSlowDslCallback = elasticSearchClient.getSlowDslCallback();
		}
        long end = System.currentTimeMillis();
        long time = end - start;
		if(logDslCallback != null){
			LogDsl slowDsl = new LogDsl();
			slowDsl.setUrl(url);
			slowDsl.setAction(action);
			slowDsl.setTime(time);
			slowDsl.setDsl(dsl);
			if(slowDslThreshold != null)
				slowDsl.setSlowDslThreshold(slowDslThreshold);
			slowDsl.setStartTime(new Date(start));
			slowDsl.setEndTime(new Date(end));
			slowDsl.setResultCode(resultCode);

            logDslCallback.logDsl( slowDsl);
		}
        else if(logSlowDslCallback != null && time >= slowDslThreshold){
            LogDsl slowDsl = new LogDsl();
            slowDsl.setUrl(url);
            slowDsl.setAction(action);
            slowDsl.setTime(time);
            slowDsl.setDsl(dsl);

            slowDsl.setSlowDslThreshold(slowDslThreshold);
            slowDsl.setStartTime(new Date(start));
            slowDsl.setEndTime(new Date(end));
            slowDsl.setResultCode(resultCode);
            logSlowDslCallback.logDsl( slowDsl);
        }
	}
	public String execute(String url,String entity,ESStringResponseHandler responseHandler) throws Exception {

		long start = System.currentTimeMillis();
		int resultCode = 0;
		try {
			return HttpRequestProxy.sendJsonBody(httpPool, entity, url, (Map<String, String>)null, responseHandler);
		}
		catch(Exception e){
			resultCode = 1;
			throw e;
		}
		finally {
			if(responseHandler.getUrl() != null )//转换为具体的es节点请求url
				url = responseHandler.getUrl();
			logDsl( start, url, "post",entity, resultCode );

		}



	}
	/**

	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeHttp(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {

		int resultCode = 0;
		long start = System.currentTimeMillis();
		try {
			return RestSearchExecutorUtil.__executeHttp(httpPool, (Map<String, String>) null, url, entity, action, responseHandler);
		}
		catch(Exception e){
			resultCode = 1;
			throw e;
		}
		finally {
			if(responseHandler instanceof  URLResponseHandler ) {//转换为具体的es节点请求url
				String temp = ((URLResponseHandler)responseHandler).getUrl();
				if(temp != null)
					url = temp;
			}
			logDsl( start, url, action,entity, resultCode );

		}

	}


	/**

	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T discoverHost(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {

			int resultCode = 0;
			long start = System.currentTimeMillis();
			try {
				return RestSearchExecutorUtil.__executeHttp(    httpPool,  (Map<String, String>)null,  url,   entity,  action,  responseHandler);
			}
			catch(Exception e){
				resultCode = 1;
				throw e;
			}
			finally {
				if(responseHandler instanceof  URLResponseHandler ) {//转换为具体的es节点请求url
					String temp = ((URLResponseHandler)responseHandler).getUrl();
					if(temp != null)
						url = temp;
				}
				logDsl( start, url, action,entity, resultCode );

			}

	}




	/**
	 * @param url
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public String executeSimpleRequest(String url, String entity,ESStringResponseHandler responseHandler) throws Exception {
		long start = System.currentTimeMillis();
		int resultCode = 0;
		try {
			String response = null;
			if (entity == null) {
				response = HttpRequestProxy.httpPostforString(httpPool, url, null, (Map<String, String>)null, responseHandler);
			} else {
				response = HttpRequestProxy.sendJsonBody(httpPool, entity, url, (Map<String, String>)null, responseHandler);
			}

			return response;
		}
		catch(Exception e){
			resultCode = 1;
			throw e;
		}
		finally {
			String temp = responseHandler.getUrl();
			if(temp != null)
				url = temp;
			logDsl( start, url, "post",entity, resultCode );

		}

	}
	/**
	 * @param entity
	 * @param responseHandler
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeRequest(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
		long start = System.currentTimeMillis();
		int resultCode = 0;
		try {
			return RestSearchExecutorUtil._executeRequest(httpPool,(Map<String, String>)null,url, entity,action, responseHandler);
		}
		catch(Exception e){
			resultCode = 1;
			throw e;
		}
		finally {
			if(responseHandler instanceof  URLResponseHandler ) {//转换为具体的es节点请求url
				String temp = ((URLResponseHandler)responseHandler).getUrl();
				if(temp != null)
					url = temp;
			}
			logDsl( start, url, action,entity, resultCode );

		}
	}



	public String getClusterVersionInfo(){
		return this.elasticSearchClient.getClusterVersionInfo();
	}

}
