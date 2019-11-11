package org.frameworkset.elasticsearch.client.tran;
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

import org.frameworkset.elasticsearch.client.ExportResultHandler;
import org.frameworkset.elasticsearch.client.WrapedExportResultHandler;
import org.frameworkset.elasticsearch.client.task.TaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/1 10:20
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseExportResultHandler<DATA,RESULT> implements WrapedExportResultHandler<DATA,RESULT> {
	protected   Logger logger = LoggerFactory.getLogger(this.getClass());
	private ExportResultHandler exportResultHandler;
	public BaseExportResultHandler(ExportResultHandler exportResultHandler){
		this.exportResultHandler = exportResultHandler;
	}
	public void success(TaskCommand<DATA,RESULT> taskCommand, RESULT result){
		this.exportResultHandler.success(  taskCommand,   result);
	}
	public void error(TaskCommand<DATA,RESULT> taskCommand, RESULT result){
		this.exportResultHandler.error(  taskCommand,   result);
	}

	@Override
	public void exception(TaskCommand<DATA,RESULT> taskCommand, Exception exception) {
		this.exportResultHandler.exception(  taskCommand,   exception);
	}

	@Override
	public int getMaxRetry() {
		return this.exportResultHandler.getMaxRetry();
	}

	/**
	 * 处理导入数据结果，如果失败则可以通过重试失败数据
	 * @param taskCommand
	 * @param result
	 *
	 */
	public void handleResult(TaskCommand<DATA,RESULT> taskCommand, RESULT result){

		success(  taskCommand,   result);


	}

	/**
	 * 处理导入数据结果，如果失败则可以通过重试失败数据
	 * @param taskCommand
	 * @param exception
	 *
	 */
	public void handleException(TaskCommand<DATA,RESULT> taskCommand, Exception exception){
		try {
			exception(taskCommand, exception);
		}
		catch (Exception e){
			logger.warn("Handle Task Exception failed:",e);
		}
	}


}
