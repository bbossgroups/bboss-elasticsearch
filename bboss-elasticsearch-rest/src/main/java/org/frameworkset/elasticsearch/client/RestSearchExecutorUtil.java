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
public abstract class RestSearchExecutorUtil {
	public static String chunkEntity(String entity){
		if(entity == null){
			return entity;
		}
		else{
			if(entity.length() > 2048){
				return new StringBuilder().append(entity.substring(0,2048)).append("......").toString();
			}
			else {
				return entity;
			}
		}

	}


	public static <T> T __executeHttp(String httpPool,Map<String, String> headers,String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception{
		T response = null;
		if (entity == null) {
			if (action == null)
				response = HttpRequestUtil.httpPostforString(httpPool, url, null, headers, responseHandler);
			else if (action == ClientUtil.HTTP_POST)
				response = HttpRequestUtil.httpPostforString(httpPool, url, null, headers, responseHandler);
			else if (action == ClientUtil.HTTP_PUT)
				response = HttpRequestUtil.httpPutforString(httpPool, url, null, headers, responseHandler);
			else if (action == ClientUtil.HTTP_GET)
				response = HttpRequestUtil.httpGetforString(httpPool, url, headers, responseHandler);
			else if (action == ClientUtil.HTTP_DELETE)
				response = HttpRequestUtil.httpDelete(httpPool, url, null, headers, responseHandler);
			else if (action == ClientUtil.HTTP_HEAD)
				response = HttpRequestUtil.httpHead(httpPool, url, null, headers, responseHandler);
			else
				throw new IllegalArgumentException("not support http action:" + action);
		} else {
			if (action == ClientUtil.HTTP_POST)
				response = HttpRequestUtil.sendJsonBody(httpPool, entity, url, headers, responseHandler);
			else if (action == ClientUtil.HTTP_PUT) {
				response = HttpRequestUtil.putJson(httpPool, entity, url, headers, responseHandler);
			} else if (action == ClientUtil.HTTP_DELETE)
				response = HttpRequestUtil.httpDelete(httpPool, url, entity, null, headers, responseHandler);
			else
				throw new IllegalArgumentException("not support http action:" + action);
		}
		return response;
	}





	public static  <T> T _executeRequest(String httpPool,Map<String, String> headers,String url, String entity,String action,ResponseHandler<T> responseHandler) throws Exception {
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
				throw new IllegalArgumentException("not support http action:"+action);
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
				throw new IllegalArgumentException("not support http action:"+action);

		}
		return response;
	}



}
