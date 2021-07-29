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

import org.frameworkset.elasticsearch.entity.LogDsl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/13 12:52
 * @author biaoping.yin
 * @version 1.0
 */
public class LoggerDslCallback implements LogDslCallback{
	private static final Logger logger = LoggerFactory.getLogger(LoggerDslCallback.class);

	public void logDsl(LogDsl slowDsl){
		if(logger.isInfoEnabled()) {
			logger.info("Request[{}] action[{}] took time:{} ms ], use DSL[{}]",
					slowDsl.getUrl(),slowDsl.getAction(), slowDsl.getTime(),   RestSearchExecutorUtil.chunkEntity(slowDsl.getDsl()));

		}
	}
}
