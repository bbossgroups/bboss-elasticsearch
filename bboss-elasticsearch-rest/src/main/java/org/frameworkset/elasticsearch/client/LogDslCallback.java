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

/**
 * <p>Description:
 *
 * dsl输出组件logDslCallback使用方法
 *
 * 通过实现接口org.frameworkset.elasticsearch.client.LogDslCallback，可以将dsl输出到自己需要的地方，LogDslCallback实现实例-将dsl执行信息输出到日志文件中
 *
 *
 * package org.frameworkset.elasticsearch.client;
 *
 * import org.frameworkset.elasticsearch.entity.LogDsl;
 * import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory;
 *
 * public class LoggerDslCallback implements LogDslCallback{
 *    private static final Logger logger = LoggerFactory.getLogger(LoggerDslCallback.class);
 *
 *    public void logDsl(LogDsl logDsl){
 *       if(logger.isInfoEnabled()) {
 *       			logger.info("Request[{}] action[{}] took time:{} ms ], use DSL[{}],execute result:{}",
 *       					logDsl.getUrl(),logDsl.getAction(), logDsl.getTime(),   logDsl.getDsl(),logDsl.result());
 *
 *                        }
 *    }
 * }
 *
 *
 * 然后在配置文件中配置logDslCallback：
 * 非spring boot项目
 *
 *
 * elasticsearch.logDslCallback=org.frameworkset.elasticsearch.client.LoggerDslCallback
 *
 *
 * springboot项目
 *
 *
 * spring.elasticsearch.bboss.elasticsearch.logDslCallback=org.frameworkset.elasticsearch.client.LoggerDslCallback
 *
 *
 * </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/13 12:52
 * @author biaoping.yin
 * @version 1.0
 */
public interface LogDslCallback {

	void logDsl(LogDsl logDsl);
}
