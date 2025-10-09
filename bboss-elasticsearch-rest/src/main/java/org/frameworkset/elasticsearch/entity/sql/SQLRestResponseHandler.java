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
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.spi.remote.http.URLResponseHandler;
import org.frameworkset.spi.remote.http.callback.ExecuteIntercepter;

import java.io.IOException;
import java.io.InputStream;

import static org.frameworkset.spi.remote.http.ResponseUtil.entityEmpty;

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
    private ExecuteIntercepter executeIntercepter;

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getUrl() {
		return url;
	}

    @Override
    public ExecuteIntercepter getExecuteIntercepter() {
        return executeIntercepter;
    }

    @Override
    public void setExecuteIntercepter(ExecuteIntercepter executeIntercepter) {
        this.executeIntercepter = executeIntercepter;
    }

    @Override
	public SQLRestResponse handleResponse(ClassicHttpResponse response) throws ClientProtocolException, IOException, ParseException {
		int status = response.getCode();

		if (org.frameworkset.spi.remote.http.ResponseUtil.isHttpStatusOK( status)) {
			HttpEntity entity = response.getEntity();
			if (entity != null ) {

				InputStream inputStream = null;
				try {
					SQLRestResponse searchResponse = null;
					inputStream = entity.getContent();
					if(entityEmpty(entity,inputStream)){
						return null;
					}
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
