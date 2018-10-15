package org.frameworkset.elasticsearch.client.schedule;
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

/**
 * <p>Description: 数据导入作业拦截器,每次定时任务执行前或者执行完毕后触发</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/10/15 11:46
 * @author biaoping.yin
 * @version 1.0
 */
public interface CallInterceptor {
	public void preCall(TaskContext taskContext);
	public void afterCall(TaskContext taskContext);
	public void throwException(TaskContext taskContext,Exception e);
}
