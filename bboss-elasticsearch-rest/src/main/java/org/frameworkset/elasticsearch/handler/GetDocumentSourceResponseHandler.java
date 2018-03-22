package org.frameworkset.elasticsearch.handler;/*
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.spi.remote.http.BaseResponseHandler;

import java.io.IOException;

public class GetDocumentSourceResponseHandler extends BaseResponseHandler implements ResponseHandler {
	private Class type;
	public GetDocumentSourceResponseHandler(Class type){
		this.type = type;
	}
	@Override
	public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		if (status >= 200 && status < 300) {
			HttpEntity entity = response.getEntity();
			try {

				if(entity != null)
					return super.converJson(entity,type);
			}
			catch (Exception e){
				throw new ElasticSearchException(e);
			}
			return null;
		} else {
			HttpEntity entity = response.getEntity();
			if (entity != null ) {
				throw new ElasticSearchException(EntityUtils.toString(entity));
			}
			else
				throw new ElasticSearchException("Unexpected response status: " + status);
		}
	}
}
