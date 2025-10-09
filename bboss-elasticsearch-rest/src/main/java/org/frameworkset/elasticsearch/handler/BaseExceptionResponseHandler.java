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

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.spi.remote.http.BaseResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/7/19 15:59
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseExceptionResponseHandler<T> extends BaseResponseHandler<T> implements ESExceptionWrapper {
	protected ElasticSearchException elasticSearchException;
	protected static Logger _logger =  LoggerFactory.getLogger(BaseExceptionResponseHandler.class);

	@Override
	public ElasticSearchException getElasticSearchException() {
		return elasticSearchException;
	}
	public void clean(){
		elasticSearchException = null;
	}
	protected Object handleException(String url,HttpEntity entity ,int status) throws IOException, ParseException {

		return handleException(url, entity ,status, null);
	}
    private void trucateData(StringBuilder builder){
        if(isTruncateLogBody() && requestBody != null && requestBody.length() > 4096){
            builder.append(requestBody.substring(0,4095)).append("......");
        }
        else {
            builder.append(requestBody);
        }
    }
	protected Object handleException(String url,HttpEntity entity ,int status,String charSet) throws IOException, ParseException {

		if(status == 404){//在有些场景下面，404不能作为异常抛出，这里作一次桥接，避免不必要的exception被apm性能监控工具探测到

			if (entity != null) {
				if(_logger.isDebugEnabled()) {
					_logger.debug(new StringBuilder().append("Request url:").append(url).append(",status:").append(status).toString());
				}
                StringBuilder msg = new StringBuilder();
				if(charSet == null ) {
                    msg.append("Request url:").append(url);
                    msg.append("\r\nResponseBody:").append(EntityUtils.toString(entity));
					this.elasticSearchException = new ElasticSearchException(msg.toString(), status);
				}
				else{
                    msg.append("Request url:").append(url);
                    msg.append("\r\nResponseBody:").append(EntityUtils.toString(entity, charSet));
					this.elasticSearchException = new ElasticSearchException(msg.toString(), status);
				}
			}
			else
				this.elasticSearchException = new ElasticSearchException(new StringBuilder().append("Request url:").append(url)
						.append(",Unexpected response status: ").append( status).toString(), status);

			return null;
		}
		else {
            if(!isEnableSetRequestBody()) {
                if (entity != null) {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug(new StringBuilder().append("Request url:").append(url).append(",status:").append(status).toString());
                    }
                    StringBuilder msg = new StringBuilder();
                    if (charSet == null) {
                        msg.append("Request url:").append(url);                       
                        msg.append("\r\nResponseBody:").append(EntityUtils.toString(entity));
                        throw new ElasticSearchException(msg.toString(), status);
                    } else {
                        msg.append("Request url:").append(url);
                        msg.append("\r\nResponseBody:").append(EntityUtils.toString(entity, charSet));
                        throw new ElasticSearchException(msg.toString(), status);
                    }
                } else {

                    throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url)
                            .append(",Unexpected response status: ").append(status).toString(), status);
                }
            }
            else{
                StringBuilder msg = new StringBuilder();
                if (entity != null) {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug(new StringBuilder().append("Request url:").append(url).append(",status:").append(status).toString());
                    }
                    if (charSet == null) {
                        msg.append("Request url:").append(url);
                        msg.append("\r\nRequestBody:");
                        trucateData(msg);
                        msg.append("\r\nResponseBody:").append(EntityUtils.toString(entity));

                    } else {
                        msg.append("Request url:").append(url);
                        msg.append("\r\nRequestBody:");
                        trucateData(msg);
                        msg.append("\r\nResponseBody:").append(EntityUtils.toString(entity, charSet));

                    }
                } else {
                    msg.append("Request url:").append(url)
                            .append(",Unexpected response status: ").append(status);
                    msg.append("\r\nRequestBody:");
                    trucateData(msg);
                   

                }
                throw new ElasticSearchException(msg.toString(), status);
            }
		}
	}


}
