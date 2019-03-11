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
public class DB2ESExportResultHandler implements ExportResultHandler<String,String>{
	private static Logger logger = LoggerFactory.getLogger(DB2ESExportResultHandler.class);
	private ExportResultHandler exportResultHandler;
	public DB2ESExportResultHandler(ExportResultHandler exportResultHandler){
		this.exportResultHandler = exportResultHandler;
	}
	public void success(TaskCommand<String,String> taskCommand, String result){
		this.exportResultHandler.success(  taskCommand,   result);
	}
	public void error(TaskCommand<String,String> taskCommand, String result){
		this.exportResultHandler.error(  taskCommand,   result);
	}

	@Override
	public void exception(TaskCommand<String, String> taskCommand, Exception exception) {
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
	public void handleResult(TaskCommand<String,String> taskCommand, String result){

		if(result == null){
			error(  taskCommand,   result);
			return ;
		}
		int errorStartIdx = result.indexOf("\"errors\":");
		if(errorStartIdx < 0) {
			error(taskCommand, result);
			return;
		}
		int errorEndIdx = result.indexOf(",",errorStartIdx);
		if(errorEndIdx < 0){
			error(taskCommand, result);
			return;
		}
		String errorInfo = result.substring(errorStartIdx,errorEndIdx);
		if(errorInfo.equals("\"errors\":false")){
			success(  taskCommand,   result);
		}
		else{
			error(  taskCommand,   result);

		}

	}

	/**
	 * 处理导入数据结果，如果失败则可以通过重试失败数据
	 * @param taskCommand
	 * @param exception
	 *
	 */
	public void handleException(TaskCommand<String,String> taskCommand, Exception exception){
		try {
			exception(taskCommand, exception);
		}
		catch (Exception e){
			logger.warn("Handle Task Exception failed:",e);
		}
	}
}
