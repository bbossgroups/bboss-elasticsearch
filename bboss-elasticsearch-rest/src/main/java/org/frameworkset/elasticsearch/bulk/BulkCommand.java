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
import org.frameworkset.elasticsearch.client.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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
	private Date bulkCommandStartTime;
	private Date bulkCommandCompleteTime;


	public Date getBulkCommandStartTime() {
		return bulkCommandStartTime;
	}

	public void setBulkCommandStartTime(Date bulkCommandStartTime) {
		this.bulkCommandStartTime = bulkCommandStartTime;
	}

	public Date getBulkCommandCompleteTime() {
		return bulkCommandCompleteTime;
	}

	public void setBulkCommandCompleteTime(Date bulkCommandCompleteTime) {
		this.bulkCommandCompleteTime = bulkCommandCompleteTime;
	}
	public BulkCommand(List<BulkData> batchBulkDatas,BulkProcessor bulkProcessor) {
		this.batchBulkDatas = batchBulkDatas;
		this.bulkProcessor = bulkProcessor;
		this.clientInterface = bulkProcessor.getClientInterface();
	}

	public String getRefreshOption() {
		return bulkProcessor.getRefreshOption();
	}

	private void directRun(List<BulkInterceptor> bulkInterceptors ){
		String result = clientInterface.executeBulk(this);
		bulkProcessor.increamentTotalsize(this.getBulkDataSize());


		boolean hasError = ResultUtil.bulkResponseError(result);
		if (!hasError) {
			for (int i = 0; bulkInterceptors != null && i < bulkInterceptors.size(); i++) {
				BulkInterceptor bulkInterceptor = bulkInterceptors.get(i);
				try {

					bulkInterceptor.afterBulk(this, result);
				} catch (Exception e) {
					if (logger.isErrorEnabled())
						logger.error("bulkInterceptor.afterBulk", e);
				}
			}
		} else {
			for (int i = 0; bulkInterceptors != null && i < bulkInterceptors.size(); i++) {
				BulkInterceptor bulkInterceptor = bulkInterceptors.get(i);
				try {

					bulkInterceptor.errorBulk(this, result);
				} catch (Exception e) {
					if (logger.isErrorEnabled())
						logger.error("bulkInterceptor.errorBulk", e);
				}
			}
		}
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
				if(logger.isErrorEnabled())
					logger.error("bulkInterceptor.beforeBulk",e);
			}
		}
		BulkRetryHandler bulkRetryHandler = bulkConfig.getBulkRetryHandler();
		int retryTimes = bulkConfig.getRetryTimes();
		if(bulkRetryHandler == null || retryTimes <= 0){//当有异常发生时，不需要重试
			try {
				this.setBulkCommandStartTime(new Date(System.currentTimeMillis()));
				directRun( bulkInterceptors );
			}
			catch (Throwable throwable){
				this.bulkProcessor.increamentFailedSize(this.getBulkDataSize());
				for (int i = 0; bulkInterceptors != null && i < bulkInterceptors.size(); i++) {
					BulkInterceptor bulkInterceptor = bulkInterceptors.get(i);
					try {
						bulkInterceptor.exceptionBulk(this,throwable);
					}
					catch(Exception e){
						logger.error("bulkInterceptor.errorBulk",e);
					}
				}
			}
			finally {
				this.setBulkCommandCompleteTime(new Date(System.currentTimeMillis()));
				if(batchBulkDatas != null) {
					this.batchBulkDatas.clear();
					batchBulkDatas = null;
				}
			}
		}
		else{
			try {
				this.setBulkCommandStartTime(new Date(System.currentTimeMillis()));
				Exception exception = null;
				int count = 0;
				long retryInterval = bulkConfig.getRetryInterval();
				do {
					if(count > 0){
						if(logger.isInfoEnabled()){
							logger.info("Retry bulkprocess {} times.",count);
						}
					}
					try {
						directRun(bulkInterceptors);
						exception = null;
						break;
					}
					catch (Exception e){
						exception = e;
						if(!bulkRetryHandler.neadRetry(e,this) || count == retryTimes){//异常不需要重试或者达到最大重试次数，中断重试
							break;
						}
						else {
							if(logger.isErrorEnabled()){
								logger.error("Exception occur and  Retry process will be take.",e);
							}
							count ++;
							if(retryInterval > 0l){
								try {
									Thread.sleep(retryInterval);
								}
								catch (Exception interupt){
									break;
								}
							}
							continue;
						}
					}
				}while(true);
				if(exception != null){
					throw exception;
				}

			}
			catch (Throwable throwable){
				this.bulkProcessor.increamentFailedSize(this.getBulkDataSize());
				for (int i = 0; bulkInterceptors != null && i < bulkInterceptors.size(); i++) {
					BulkInterceptor bulkInterceptor = bulkInterceptors.get(i);
					try {
						bulkInterceptor.exceptionBulk(this,throwable);
					}
					catch(Exception e){
						logger.error("bulkInterceptor.errorBulk",e);
					}
				}
			}
			finally {
				this.setBulkCommandCompleteTime(new Date(System.currentTimeMillis()));
				if(batchBulkDatas != null) {
					this.batchBulkDatas.clear();
					batchBulkDatas = null;
				}
			}
		}

	}

	public long getTotalSize(){
		return bulkProcessor.getTotalSize();
	}
	public long getTotalFailedSize(){
		return bulkProcessor.getFailedSize();
	}

	public BulkProcessor getBulkProcessor() {
		return bulkProcessor;
	}

	public List<BulkData> getBatchBulkDatas() {
		return batchBulkDatas;
	}

	public void addBulkData(BulkData bulkData){
		this.batchBulkDatas.add(bulkData);

	}

	public int getBulkDataSize(){
		if(batchBulkDatas != null)
			return this.batchBulkDatas.size();
		else
			return 0;
	}
}
