package org.frameworkset.elasticsearch.client.db2es;
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

import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.elasticsearch.client.TaskCommand;
import org.frameworkset.elasticsearch.client.TaskFailedException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.handler.ESVoidResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/1 11:32
 * @author biaoping.yin
 * @version 1.0
 */
public class TaskCommandImpl implements TaskCommand<String,String> {
	private String refreshOption;

	public ImportContext getImportContext() {
		return db2ESImportContext;
	}

	public void setImportContext(ImportContext db2ESImportContext) {
		this.db2ESImportContext = db2ESImportContext;
	}

	private ImportContext db2ESImportContext;
	private ClientInterface clientInterface;

	public String getRefreshOption() {
		return refreshOption;
	}

	public ClientInterface getClientInterface() {
		return clientInterface;
	}

	public String getDatas() {
		return datas;
	}


	private String datas;
	private int tryCount;

	public void setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
	}

	public void setClientInterface(ClientInterface clientInterface) {
		this.clientInterface = clientInterface;
	}

	public void setDatas(String datas) {
		this.datas = datas;
	}





	private static Logger logger = LoggerFactory.getLogger(TaskCommand.class);
	public String execute(){
		String data = null;
		if(this.db2ESImportContext.getMaxRetry() > 0){
			if(this.tryCount >= this.db2ESImportContext.getMaxRetry())
				throw new TaskFailedException("task execute failed:reached max retry times "+this.db2ESImportContext.getMaxRetry());
		}
		this.tryCount ++;
		if(db2ESImportContext.isDebugResponse()) {

			if (refreshOption == null) {
				data = clientInterface.executeHttp("_bulk", datas, ClientUtil.HTTP_POST);
				if(logger.isInfoEnabled())
					logger.info(data);
			} else {
				data = clientInterface.executeHttp("_bulk?" + refreshOption, datas, ClientUtil.HTTP_POST);
				if(logger.isInfoEnabled())
					logger.info(data);
			}
		}
		else{
			if(db2ESImportContext.isDiscardBulkResponse() && db2ESImportContext.getExportResultHandler() == null) {
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
		}
		return data;
	}

	public int getTryCount() {
		return tryCount;
	}


}
