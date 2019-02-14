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
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.MGetDocs;
import org.frameworkset.elasticsearch.serial.ESSerialThreadLocal;

import java.io.IOException;

public class MGetDocumentsSourceResponseHandler extends BaseResponsehandler implements ResponseHandler<MGetDocs> {


	public MGetDocumentsSourceResponseHandler(Class type){
		super(type );
	}
	@Override
	public MGetDocs handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int status = initStatus(  response);
		if (status >= 200 && status < 300) {
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				try {

					ESSerialThreadLocal.setESTypeReferences(types);
					return super.converJson(entity, MGetDocs.class);
				} catch (Exception e) {
					throw new ElasticSearchException(e,status);
				}
				finally {
					ESSerialThreadLocal.clean();
				}
			}
			return null;
		} else {
			HttpEntity entity = response.getEntity();
//			if (entity != null ) {
//				throw new ElasticSearchException(EntityUtils.toString(entity),status);
//			}
//			else
//				throw new ElasticSearchException("Unexpected response status: " + status,status);
			return (MGetDocs)super.handleException(entity,status);
		}
	}
}
