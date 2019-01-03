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
import org.frameworkset.elasticsearch.handler.ESStringResponseHandler;
import org.frameworkset.spi.remote.http.HttpRequestUtil;

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
	private Map<String, String> headers;
	private String httpPool;
	private ElasticSearchClient elasticSearchClient;
	public RestSearchExecutor(Map<String, String> headers,String httpPool,ElasticSearchClient elasticSearchClient){
		this.headers = headers;
		this.httpPool = httpPool;
		this.elasticSearchClient = elasticSearchClient;
	}
	public String execute(String url,String entity,ESStringResponseHandler responseHandler) throws Exception {

		String response = HttpRequestUtil.sendJsonBody(httpPool,entity, url, headers,responseHandler);
		return response;

	}
	/**

	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeHttp(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
		return _executeHttp(  url,   entity,  action, responseHandler);
	}

	/**

	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	private <T> T _executeHttp(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {

		T response = null;
		if (entity == null){
			if(action == null)
				response = HttpRequestUtil.httpPostforString(httpPool,url, null, headers, responseHandler);
			else if(action == ClientUtil.HTTP_POST )
				response = HttpRequestUtil.httpPostforString(httpPool,url, null, headers,responseHandler);
			else if( action == ClientUtil.HTTP_PUT)
				response = HttpRequestUtil.httpPutforString(httpPool,url, null, headers,responseHandler);
			else if(action == ClientUtil.HTTP_GET)
				response = HttpRequestUtil.httpGetforString(httpPool,url, headers,responseHandler);
			else if(action == ClientUtil.HTTP_DELETE)
				response = HttpRequestUtil.httpDelete(httpPool,url, null, headers,responseHandler);
			else if(action == ClientUtil.HTTP_HEAD)
				response = HttpRequestUtil.httpHead(httpPool,url, null, headers,responseHandler);
			else
				throw new IllegalArgumentException("not support http action:"+action);
		}
		else
		{
			if(action == ClientUtil.HTTP_POST )
				response = HttpRequestUtil.sendJsonBody(httpPool,entity, url, headers,responseHandler);
			else if( action == ClientUtil.HTTP_PUT)
			{
				response = HttpRequestUtil.putJson(httpPool,entity, url, headers,responseHandler);
			}
			else if(action == ClientUtil.HTTP_DELETE)
				response = HttpRequestUtil.httpDelete(httpPool,url,entity, null, headers,responseHandler);
			else
				throw new IllegalArgumentException("not support http action:"+action);
		}
		return response;
	}

	/**

	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T discoverHost(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
		return _executeHttp(  url,   entity,  action, responseHandler);
	}




	/**
	 * @param url
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public String executeSimpleRequest(String url, String entity,ESStringResponseHandler responseHandler) throws Exception {
		String response = null;
		if (entity == null) {
			response = HttpRequestUtil.httpPostforString(httpPool,url, null, headers,  responseHandler);
//			response = HttpRequestUtil.httpPostforString(httpPool,url, null, headers);
		}
		else {
			response = HttpRequestUtil.sendJsonBody(httpPool,entity, url, headers,  responseHandler);
//			response = HttpRequestUtil.sendJsonBody(httpPool,entity, url, headers);
		}

		return response;
	}
	/**
	 * @param entity
	 * @param responseHandler
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeRequest(String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
		T response = null;
		if (entity == null){
			if(action == null)
				response = HttpRequestUtil.httpPostforString(httpPool,url, null, headers,  responseHandler);
			else if(action == ClientUtil.HTTP_POST )
				response = HttpRequestUtil.httpPostforString(httpPool,url, null, headers,  responseHandler);
			else if( action == ClientUtil.HTTP_PUT)
				response = HttpRequestUtil.httpPutforString(httpPool,url, null, headers,  responseHandler);
			else if(action == ClientUtil.HTTP_GET)
				response = HttpRequestUtil.httpGetforString(httpPool,url, headers,  responseHandler);
			else if(action == ClientUtil.HTTP_DELETE)
				response = HttpRequestUtil.httpDelete(httpPool,url, null, headers,  responseHandler);
			else if(action == ClientUtil.HTTP_HEAD)
				response = HttpRequestUtil.httpHead(httpPool,url, null, headers,  responseHandler);
			else
				throw new java.lang.IllegalArgumentException("not support http action:"+action);
		}
		else
		{
			if(action == ClientUtil.HTTP_POST )
				response = HttpRequestUtil.sendJsonBody(httpPool,entity, url, headers,  responseHandler);
			else if( action == ClientUtil.HTTP_PUT)
			{
				response = HttpRequestUtil.putJson(httpPool,entity, url, headers,  responseHandler);
			}
			else if(action == ClientUtil.HTTP_DELETE)
				response = HttpRequestUtil.httpDelete(httpPool,url, entity,null, headers,  responseHandler);
			else
				throw new java.lang.IllegalArgumentException("not support http action:"+action);

		}
		return response;
	}

	public String getClusterVersionInfo(){
		return this.elasticSearchClient.getClusterVersionInfo();
	}

}
