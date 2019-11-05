package org.frameworkset.elasticsearch.client.task;
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
import org.frameworkset.elasticsearch.client.metrics.ImportCount;
import org.frameworkset.elasticsearch.client.TranErrorWrapper;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.metrics.TaskMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

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
//		taskCommand.getDataSize() = currentSize;
//		this.totalCount = totalCount;
//		this.printTaskLog = db2ESImportContext.isPrintTaskLog();
//		this.db2ESImportContext = db2ESImportContext;
//	}

	public TaskCall(TaskCommand taskCommand,
					TranErrorWrapper errorWrapper){
		this.taskCommand = taskCommand;
		this.errorWrapper = errorWrapper;
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
	protected boolean isPrintTaskLog(){
		return db2ESImportContext.isPrintTaskLog() && logger.isInfoEnabled();
	}
	public static <DATA,RESULT> RESULT call(TaskCommand<DATA,RESULT> taskCommand){
		ImportContext importContext = taskCommand.getImportContext();
		ImportCount importCount = taskCommand.getImportCount();
		TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
		taskMetrics.setJobStartTime(importCount.getJobStartTime());
		taskMetrics.setTaskStartTime(new Date());
		try {
			RESULT data = taskCommand.execute();
			long[] metrics = importCount.increamentSuccessCount((long)taskCommand.getDataSize());
			taskMetrics.setTotalSuccessRecords(metrics[0]);
			taskMetrics.setTotalRecords(metrics[1]);
			taskMetrics.setSuccessRecords((long)taskCommand.getDataSize());
			taskMetrics.setTaskEndTime(new Date());
			if (importContext.getExportResultHandler() != null) {//处理返回值
				try {
					importContext.getExportResultHandler().handleResult(taskCommand, data);
				}
				catch (Exception e){
					logger.warn("",e);
				}
			}
			return data;
		}
		catch (ElasticSearchException e){
			long[] metrics = importCount.increamentFailedCount(taskCommand.getDataSize());
			taskMetrics.setFailedRecords(taskCommand.getDataSize());
			taskMetrics.setTotalRecords(metrics[1]);
			taskMetrics.setTotalFailedRecords(metrics[0]);
			taskMetrics.setTaskEndTime(new Date());
			if (importContext.getExportResultHandler() != null) {
				try {
					importContext.getExportResultHandler().handleException(taskCommand, e);
				}
				catch (Exception ee){
					logger.warn("",e);
				}
			}
			throw e;
		}
		catch (Exception e){
			long[] metrics = importCount.increamentFailedCount(taskCommand.getDataSize());
			taskMetrics.setFailedRecords(taskCommand.getDataSize());
			taskMetrics.setTotalRecords(metrics[1]);
			taskMetrics.setTotalFailedRecords(metrics[0]);
			taskMetrics.setTaskEndTime(new Date());
			if (importContext.getExportResultHandler() != null) {
				try {
					importContext.getExportResultHandler().handleException(taskCommand, e);
				}
				catch (Exception ee){
					logger.warn("",e);
				}
			}
			throw new ElasticSearchException(e);
		}

	}

	@Override
	public void run()   {
		if(!errorWrapper.assertCondition()) {
			if(logger.isWarnEnabled())
				logger.warn(new StringBuilder().append("Task[").append(taskCommand.getTaskNo()).append("] Assert Execute Condition Failed, Ignore").toString());
			return;
		}
		long start = System.currentTimeMillis();
		StringBuilder info = null;
		if(isPrintTaskLog()) {
			info = new StringBuilder();
		}
		try {
			if(isPrintTaskLog()) {

					info.append("Task[").append(taskCommand.getTaskNo()).append("] starting ......");
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
			if(isPrintTaskLog()) {
				long end = System.currentTimeMillis();
				info.setLength(0);
				info.append("Task[").append(taskCommand.getTaskNo()).append("] finish,import ")
						.append(taskCommand.getDataSize())
						.append(" records,Total import ")
						.append(taskCommand.getTaskMetrics().getTotalSuccessRecords()).append(" records,Take time:")
						.append((end - start)).append("ms");
				logger.info(info.toString());
			}
		}
		catch (Exception e){
			errorWrapper.setError(e);

			if(!db2ESImportContext.isContinueOnError()) {
				if (isPrintTaskLog()) {
					long end = System.currentTimeMillis();
					info.setLength(0);
					info.append("Task[").append(taskCommand.getTaskNo()).append("] failed: ")
						.append(taskCommand.getDataSize())
						.append(" records, Take time:").append((end - start)).append("ms");
					logger.info(info.toString());
				}
				throw new TaskFailedException(new StringBuilder().append("Task[").append(taskCommand.getTaskNo()).append("] Execute Failed: ")
						.append(taskCommand.getDataSize())
						.append(" records,").toString(), e);
			}
			else
			{
				if(isPrintTaskLog()) {
					long end = System.currentTimeMillis();
					info.setLength(0);
					info.append("Task[").append(taskCommand.getTaskNo()).append("] failed: ")
						.append(taskCommand.getDataSize())
						.append(" records,but continue On Error! Take time:").append((end - start)).append("ms");
					logger.info(info.toString(),e);
				}

			}

		}



	}
}
