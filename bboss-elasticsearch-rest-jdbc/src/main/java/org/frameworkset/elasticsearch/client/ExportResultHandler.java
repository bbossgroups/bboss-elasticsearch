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

import org.frameworkset.elasticsearch.client.task.TaskCommand;

/**
 * <p>Description: 任务执行结果处理接口，<DATA,RESULT>中的DATA标识处理数据的类型，RESULT标识返回结果的类型</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/1 10:20
 * @author biaoping.yin
 * @version 1.0
 */
public interface ExportResultHandler<DATA,RESULT> {
	/**
	 * 导入任务成功时执行的回调方法，<DATA,RESULT>中的DATA标识处理数据的类型，RESULT标识返回结果的类型
	 * @param taskCommand 导入任务相关信息：导入数据，es刷新机制，clientInterface,失败重试次数
	 * @param result 导入结果，类型由RESULT决定
	 */
	public void success(TaskCommand<DATA,RESULT> taskCommand, RESULT result);
	/**
	 * 导入任务存在错误时执行的回调方法，<DATA,RESULT>中的DATA标识处理数据的类型，RESULT标识返回结果的类型
	 * @param taskCommand 导入任务相关信息：导入数据，es刷新机制，clientInterface,失败重试次数
	 * @param result 导入结果，类型由RESULT决定
	 */
	public void error(TaskCommand<DATA,RESULT> taskCommand, RESULT result);

	/**
	 * 导入任务抛出Exception时执行的回调方法，<DATA,RESULT>中的DATA标识处理数据的类型，RESULT标识返回结果的类型
	 * @param taskCommand 导入任务相关信息：导入数据，es刷新机制，clientInterface,失败重试次数
	 * @param exception 导入异常
	 */
	public void exception(TaskCommand<DATA,RESULT> taskCommand, Exception exception);

	/**
	 * 如果对于执行有错误的任务，可以进行修正后重新执行，通过本方法
	 * 返回允许的最大重试次数
	 * @return
	 */
	public int getMaxRetry();


}
