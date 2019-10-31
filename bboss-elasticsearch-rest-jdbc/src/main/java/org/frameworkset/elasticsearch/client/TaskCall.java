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

import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/8/29 21:27
 * @author biaoping.yin
 * @version 1.0
 */
public class TaskCall implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(TaskCall.class);
	private TranErrorWrapper errorWrapper;
	private int taskNo;
	private ImportCount totalCount;
	private boolean printTaskLog;
	private int currentSize;
	private ImportContext db2ESImportContext;
	private TaskCommand taskCommand;
//	public TaskCall(ImportContext db2ESImportContext  , String datas,
//					TranErrorWrapper errorWrapper,
//					ClientInterface clientInterface ,
//					int taskNo, ImportCount totalCount,
//					int currentSize ){
//
//		this.refreshOption = db2ESImportContext.getRefreshOption();
//		this.clientInterface = clientInterface;
//		this.datas = datas;
//		this.errorWrapper = errorWrapper;
//		this.taskNo = taskNo;
//		this.currentSize = currentSize;
//		this.totalCount = totalCount;
//		this.printTaskLog = db2ESImportContext.isPrintTaskLog();
//		this.db2ESImportContext = db2ESImportContext;
//	}

	public TaskCall(TaskCommand taskCommand,
					TranErrorWrapper errorWrapper,
					int taskNo, ImportCount totalCount,
					int currentSize ){
		this.taskCommand = taskCommand;
		this.errorWrapper = errorWrapper;
		this.taskNo = taskNo;
		this.currentSize = currentSize;
		this.totalCount = totalCount;
		this.printTaskLog = db2ESImportContext.isPrintTaskLog();
		this.db2ESImportContext = taskCommand.getImportContext();
	}

//	public static String call(String refreshOption, ClientInterface clientInterface, String datas, ImportContext db2ESImportContext){
//		TaskCommandImpl taskCommand = new TaskCommandImpl();
//		taskCommand.setClientInterface(clientInterface);
//		taskCommand.setRefreshOption(refreshOption);
//		taskCommand.setDatas(datas);
//		taskCommand.setImportContext(db2ESImportContext);
//		return call(taskCommand);
//	}

	public static <DATA,RESULT> RESULT call(TaskCommand<DATA,RESULT> taskCommand){
		ImportContext importContext = taskCommand.getImportContext();
		try {
			RESULT data = taskCommand.execute();

			if (importContext.getExportResultHandler() != null) {//处理返回值
				importContext.getExportResultHandler().handleResult(taskCommand, data);
			}
			return data;
		}
		catch (ElasticSearchException e){
			if (importContext.getExportResultHandler() != null) {
				importContext.getExportResultHandler().handleException(taskCommand, e);
			}
			throw e;
		}
		catch (Exception e){
			if (importContext.getExportResultHandler() != null) {
				importContext.getExportResultHandler().handleException(taskCommand, e);
			}
			throw new ElasticSearchException(e);
		}
	}

	@Override
	public void run()   {
		if(!errorWrapper.assertCondition()) {
			if(logger.isWarnEnabled())
				logger.warn(new StringBuilder().append("Task[").append(this.taskNo).append("] Assert Execute Condition Failed, Ignore").toString());
			return;
		}
		long start = System.currentTimeMillis();
		StringBuilder info = null;
		if(printTaskLog) {
			info = new StringBuilder();
		}
		long totalSize = 0;
		try {
			if(printTaskLog&& logger.isInfoEnabled()) {

					info.append("Task[").append(this.taskNo).append("] starting ......");
					logger.info(info.toString());

			}
//			if(logger.isDebugEnabled()) {
//				if (refreshOption == null) {
//					String data = clientInterface.executeHttp("_bulk", datas, ClientUtil.HTTP_POST);
//					logger.debug(data);
//				} else {
//					String data = clientInterface.executeHttp("_bulk?" + refreshOption, datas, ClientUtil.HTTP_POST);
//					logger.debug(data);
//				}
//			}
//			else{
//				if (refreshOption == null) {
//					clientInterface.executeHttp("_bulk", datas, ClientUtil.HTTP_POST);
//				} else {
//					clientInterface.executeHttp("_bulk?" + refreshOption, datas, ClientUtil.HTTP_POST);
//				}
//			}
			call(taskCommand);
			totalSize = totalCount.increamentTotalCount((long)currentSize);
		}
		catch (Exception e){
			errorWrapper.setError(e);
			if(printTaskLog && logger.isInfoEnabled()) {
				long end = System.currentTimeMillis();
				info.setLength(0);
				info.append("Task[").append(this.taskNo).append("] failed,take time:").append((end - start)).append("ms");
				logger.info(info.toString());
			}

			if(!db2ESImportContext.isContinueOnError())
				throw new TaskFailedException(new StringBuilder().append("Task[").append(this.taskNo).append("] Execute Failed").toString(),e);
			else
			{
				if(logger.isErrorEnabled())
					logger.error(new StringBuilder().append("Task[").append(this.taskNo).append("] Execute Failed,but continue On Error!").toString(),e);
			}
		}
		if(printTaskLog&& logger.isInfoEnabled()) {
			long end = System.currentTimeMillis();
			info.setLength(0);
			info.append("Task[").append(this.taskNo).append("] finish,import ").append(this.currentSize).append(" records,Total import ").append(totalSize).append(" records,Take time:").append((end - start)).append("ms");
			logger.info(info.toString());
		}


	}
}
