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

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/25 15:03
 * @author biaoping.yin
 * @version 1.0
 */
public interface WrapedExportResultHandler<DATA,RESULT> extends ExportResultHandler<DATA,RESULT> {
	/**
	 * 处理导入数据结果，如果失败则可以通过重试失败数据
	 * @param taskCommand
	 * @param exception
	 *
	 */
	public void handleException(TaskCommand<DATA,RESULT> taskCommand, Exception exception);
	/**
	 * 处理导入数据结果，如果失败则可以通过重试失败数据
	 * @param taskCommand
	 * @param result
	 *
	 */
	public void handleResult(TaskCommand<DATA,RESULT> taskCommand, RESULT result);
}
