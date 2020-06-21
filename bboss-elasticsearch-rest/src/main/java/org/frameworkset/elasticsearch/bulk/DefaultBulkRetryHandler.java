package org.frameworkset.elasticsearch.bulk;
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

import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.frameworkset.elasticsearch.client.NoServerElasticSearchException;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * <p>Description: 默认重试机制</p>
 * <p>if (exception instanceof HttpHostConnectException     //NoHttpResponseException 重试
 * 				|| exception instanceof ConnectTimeoutException //连接超时重试
 * 				|| exception instanceof UnknownHostException
 * 				|| exception instanceof NoHttpResponseException
 * //              || exception instanceof SocketTimeoutException    //响应超时不重试，避免造成业务数据不一致
 * 		) {
 *
 * 			return true;
 *                }
 *
 * 		if(exception instanceof SocketException){
 * 			String message = exception.getMessage();
 * 			if(message != null && message.trim().equals("Connection reset")) {
 * 				return true;
 *            }
 *        }
 *
 * 		return false;</p>
 * <p>Copyright (c) 2020</p>
 * @Date 2020/6/20 23:46
 * @author biaoping.yin
 * @version 1.0
 */
public class DefaultBulkRetryHandler implements BulkRetryHandler{
	@Override
	public boolean neadRetry(Exception exception, BulkCommand bulkCommand) {
		if (exception instanceof HttpHostConnectException     //NoHttpResponseException 重试
				|| exception instanceof ConnectTimeoutException //连接超时重试
				|| exception instanceof UnknownHostException
				|| exception instanceof NoHttpResponseException
				|| exception instanceof NoServerElasticSearchException
//              || exception instanceof SocketTimeoutException    //响应超时不重试，避免造成业务数据不一致
		) {

			return true;
		}

		if(exception instanceof SocketException){
			String message = exception.getMessage();
			if(message != null && message.trim().equals("Connection reset")) {
				return true;
			}
		}

		return false;
	}
}
