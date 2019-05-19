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

import org.frameworkset.elasticsearch.client.*;
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

	public ESJDBC getEsjdbc() {
		return esjdbc;
	}

	private String datas;
	private ESJDBC esjdbc;
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

	public void setEsjdbc(ESJDBC esjdbc) {
		this.esjdbc = esjdbc;
	}



	private static Logger logger = LoggerFactory.getLogger(TaskCommand.class);
	public String execute(){
		String data = null;
		if(this.esjdbc.getMaxRetry() > 0){
			if(this.tryCount >= this.esjdbc.getMaxRetry())
				throw new TaskFailedException("task execute failed:reached max retry times "+this.esjdbc.getMaxRetry());
		}
		this.tryCount ++;
		if(esjdbc.isDebugResponse()) {

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
		}
		return data;
	}

	public int getTryCount() {
		return tryCount;
	}


}
