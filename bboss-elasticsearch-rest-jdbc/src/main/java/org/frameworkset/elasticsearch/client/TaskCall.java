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
import org.frameworkset.elasticsearch.client.db2es.TaskCommandImpl;
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
	private String refreshOption;
	private ClientInterface clientInterface;
	private String datas;
	private ErrorWrapper errorWrapper;
	private int taskNo;
	private ImportCount totalCount;
	private boolean printTaskLog;
	private int currentSize;
	public TaskCall(String refreshOption ,String datas,ErrorWrapper errorWrapper,int taskNo,ImportCount totalCount,int currentSize,boolean printTaskLog){
		this.refreshOption = refreshOption;
		this.clientInterface = errorWrapper.getClientInterface();
		this.datas = datas;
		this.errorWrapper = errorWrapper;
		this.taskNo = taskNo;
		this.currentSize = currentSize;
		this.totalCount = totalCount;
		this.printTaskLog = printTaskLog;
	}

	public static String call(String refreshOption,ClientInterface clientInterface,String datas,ESJDBC esjdbc){
		TaskCommandImpl taskCommand = new TaskCommandImpl();
		taskCommand.setClientInterface(clientInterface);
		taskCommand.setRefreshOption(refreshOption);
		taskCommand.setDatas(datas);
		taskCommand.setEsjdbc(esjdbc);
		try {
			String data = taskCommand.execute();
			/**
			 if(esjdbc.isDebugResponse()) {

			 if (refreshOption == null) {
			 data = clientInterface.executeHttp("_bulk", datas, ClientUtil.HTTP_POST);
			 logger.info(data);
			 } else {
			 data = clientInterface.executeHttp("_bulk?" + refreshOption, datas, ClientUtil.HTTP_POST);
			 logger.info(data);
			 }


			 }
			 else{
			 if(esjdbc.isDiscardBulkResponse() && esjdbc.getExportResultHandler() == null) {
			 ESVoidResponseHandler esVoidResponseHandler = new ESVoidResponseHandler();
			 if (refreshOption == null) {
			 clientInterface.executeHttp("_bulk", datas, ClientUtil.HTTP_POST,esVoidResponseHandler);
			 } else {
			 clientInterface.executeHttp("_bulk?" + refreshOption, datas, ClientUtil.HTTP_POST,esVoidResponseHandler);
			 }
			 if(esVoidResponseHandler.getElasticSearchException() != null)
			 throw esVoidResponseHandler.getElasticSearchException();
			 return null;
			 }
			 else{

			 if (refreshOption == null) {
			 data = clientInterface.executeHttp("_bulk", datas, ClientUtil.HTTP_POST);

			 } else {
			 data = clientInterface.executeHttp("_bulk?" + refreshOption, datas, ClientUtil.HTTP_POST);
			 }

			 }


			 }*/
			if (esjdbc.getExportResultHandler() != null) {//处理返回值
				esjdbc.getExportResultHandler().handleResult(taskCommand, data);
			}
			return data;
		}
		catch (ElasticSearchException e){
			esjdbc.getExportResultHandler().handleException(taskCommand, e);
			throw e;
		}
		catch (Exception e){
			esjdbc.getExportResultHandler().handleException(taskCommand, e);
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
			if(printTaskLog) {
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
			call(refreshOption,clientInterface,datas,errorWrapper.getESJDBC());
			totalSize = totalCount.increamentTotalCount((long)currentSize);
		}
		catch (Exception e){
			errorWrapper.setError(e);
			if(printTaskLog) {
				long end = System.currentTimeMillis();
				info.setLength(0);
				info.append("Task[").append(this.taskNo).append("] failed,take time:").append((end - start)).append("ms");
				logger.info(info.toString());
			}

			if(!errorWrapper.getESJDBC().isContinueOnError())
				throw new TaskFailedException(new StringBuilder().append("Task[").append(this.taskNo).append("] Execute Failed").toString(),e);
			else
			{
				if(logger.isErrorEnabled())
					logger.error(new StringBuilder().append("Task[").append(this.taskNo).append("] Execute Failed,but continue On Error!").toString(),e);
			}
		}
		if(printTaskLog) {
			long end = System.currentTimeMillis();
			info.setLength(0);
			info.append("Task[").append(this.taskNo).append("] finish,import ").append(this.currentSize).append(" records,Total import ").append(totalSize).append(" records,Take time:").append((end - start)).append("ms");
			logger.debug(info.toString());
		}


	}
}
