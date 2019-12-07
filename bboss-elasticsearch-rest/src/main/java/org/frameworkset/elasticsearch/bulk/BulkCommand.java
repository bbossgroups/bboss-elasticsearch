package org.frameworkset.elasticsearch.bulk;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/12/7 12:58
 * @author biaoping.yin
 * @version 1.0
 */
public class BulkCommand implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(BulkCommand.class);
	private List<BulkData> batchBulkDatas;
	private BulkProcessor bulkProcessor;
	private ClientInterface clientInterface;

	public BulkCommand(List<BulkData> batchBulkDatas,BulkProcessor bulkProcessor) {
		this.batchBulkDatas = batchBulkDatas;
		this.bulkProcessor = bulkProcessor;
		this.clientInterface = bulkProcessor.getClientInterface();
	}
	public BulkCommand(List<BulkData> batchBulkDatas) {
		this.batchBulkDatas = batchBulkDatas;
	}
	public String getRefreshOption() {
		return bulkProcessor.getRefreshOption();
	}
	@Override
	public void run() {
		BulkConfig bulkConfig = bulkProcessor.getBulkConfig();
		List<BulkInterceptor> bulkInterceptors = bulkConfig.getBulkInterceptors();

		for (int i = 0; bulkInterceptors != null && i < bulkInterceptors.size(); i++) {
			BulkInterceptor bulkInterceptor = bulkInterceptors.get(i);
			try {
				bulkInterceptor.beforeBulk(this);
			}
			catch(Exception e){
				logger.error("bulkInterceptor.beforeBulk",e);
			}
		}
		try {
			String result = clientInterface.executeBulk(this);
			for (int i = 0; bulkInterceptors != null && i < bulkInterceptors.size(); i++) {
				BulkInterceptor bulkInterceptor = bulkInterceptors.get(i);
				try {
					bulkInterceptor.afterBulk(this,result);
				}
				catch(Exception e){
					logger.error("bulkInterceptor.afterBulk",e);
				}
			}
		}

		catch (Throwable throwable){
			for (int i = 0; bulkInterceptors != null && i < bulkInterceptors.size(); i++) {
				BulkInterceptor bulkInterceptor = bulkInterceptors.get(i);
				try {
					bulkInterceptor.errorBulk(this,throwable);
				}
				catch(Exception e){
					logger.error("bulkInterceptor.errorBulk",e);
				}
			}
		}


	}

	public BulkProcessor getBulkProcessor() {
		return bulkProcessor;
	}

	public List<BulkData> getBatchBulkDatas() {
		return batchBulkDatas;
	}
}
