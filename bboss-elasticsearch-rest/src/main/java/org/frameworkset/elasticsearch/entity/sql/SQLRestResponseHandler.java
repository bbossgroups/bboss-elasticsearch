package org.frameworkset.elasticsearch.entity.sql;
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

import com.frameworkset.util.SimpleStringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.spi.remote.http.URLResponseHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/16 21:45
 * @author biaoping.yin
 * @version 1.0
 */
public class SQLRestResponseHandler implements URLResponseHandler<SQLRestResponse> {
	private String url;

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public SQLRestResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();

		if (status >= 200 && status < 300) {
			HttpEntity entity = response.getEntity();
			if (entity != null ) {
				InputStream inputStream = null;
				try {
					SQLRestResponse searchResponse = null;
					inputStream = entity.getContent();
					searchResponse = SimpleStringUtil.json2Object(inputStream, SQLRestResponse.class);
					return searchResponse;
				}
				catch (Exception e){
					throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).toString(),e);
				}
				finally {
					inputStream.close();
				}
			}
			return null;
		} else {
			HttpEntity entity = response.getEntity();
//			if (entity != null)
//				throw new ElasticSearchException(EntityUtils.toString(entity), status);
//			else
//				throw new ElasticSearchException("Unexpected response status: " + status, status);
			if (entity != null )
				throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).append(",").append(EntityUtils.toString(entity)).toString());
			else
				throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).append(",Unexpected response status: ").append( status).toString());
		}
	}
}
