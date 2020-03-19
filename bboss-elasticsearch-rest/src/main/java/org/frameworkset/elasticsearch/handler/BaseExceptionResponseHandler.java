package org.frameworkset.elasticsearch.handler;
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
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.spi.remote.http.BaseResponseHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/7/19 15:59
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseExceptionResponseHandler extends BaseResponseHandler implements ESExceptionWrapper {
	protected ElasticSearchException elasticSearchException;

	@Override
	public ElasticSearchException getElasticSearchException() {
		return elasticSearchException;
	}
	protected Object handleException(String url,HttpEntity entity ,int status) throws IOException {

		if(status == 404){//在有些场景下面，404不能作为异常抛出，这里作一次桥接，避免不必要的exception被apm性能监控工具探测到
			if (entity != null) {
				String info = putURL( url,EntityUtils.toString(entity));
				this.elasticSearchException = new ElasticSearchException(info, status);
//				this.elasticSearchException = new ElasticSearchException(EntityUtils.toString(entity), status);
			}
			else
				this.elasticSearchException = new ElasticSearchException(new StringBuilder().append("Request url:").append(url).append(",Unexpected response status: ").append( status).toString(), status);

			return null;
		}
		else {
			if (entity != null)
				throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).append(",").append(EntityUtils.toString(entity)).toString(), status);
			else
				throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).append(",Unexpected response status: ").append( status).toString(), status);
		}
	}
	protected String putURL(String url,String response){
		if(response != null && !response.equals("")) {
			Map data = SimpleStringUtil.json2Object(response, Map.class);
			data.put("request_url", url);
			return SimpleStringUtil.object2json(data);
		}
		else{
			Map data = new HashMap(2);
			data.put("request_url", url);
			return SimpleStringUtil.object2json(data);
		}
	}
}
