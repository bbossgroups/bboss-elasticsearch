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
public abstract class BaseBulkCommand implements BulkCommand{
	private static Logger logger = LoggerFactory.getLogger(BaseBulkCommand.class);
    protected BulkProcessor bulkProcessor;
	protected ClientInterface clientInterface;
    protected Date bulkCommandStartTime;
    protected Date bulkCommandCompleteTime;

    public Date getBulkCommandStartTime() {
		return bulkCommandStartTime;
	}

	public void setBulkCommandStartTime(Date bulkCommandStartTime) {
		this.bulkCommandStartTime = bulkCommandStartTime;
	}

	public Date getBulkCommandCompleteTime() {
		return bulkCommandCompleteTime;
	}
	public long getElapsed(){
		if(bulkCommandCompleteTime != null && bulkCommandStartTime != null){
			return bulkCommandCompleteTime.getTime() - bulkCommandStartTime.getTime();
		}
		return 0;
	}

	public void setBulkCommandCompleteTime(Date bulkCommandCompleteTime) {
		this.bulkCommandCompleteTime = bulkCommandCompleteTime;
	}
	public BaseBulkCommand(BulkProcessor bulkProcessor) {
		this.bulkProcessor = bulkProcessor;
		this.clientInterface = bulkProcessor.getClientInterface();
	}

	public String getRefreshOption() {
		return bulkProcessor.getRefreshOption();
	}

	public String getFilterPath(){
		return bulkProcessor.getBulkConfig().getFilterPath();
	}

	private void directRun(List<BulkInterceptor> bulkInterceptors ){
		String result = clientInterface.executeBulk(this);
		bulkProcessor.increamentTotalsize(this.getBulkDataRecords());


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
		result = null;
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
				this.setBulkCommandStartTime(new Date());
				directRun( bulkInterceptors );
				this.setBulkCommandCompleteTime(new Date());
			}
			catch (Throwable throwable){
				this.setBulkCommandCompleteTime(new Date());
				this.bulkProcessor.increamentFailedSize(this.getBulkDataRecords());
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

                clear();
			}
		}
		else{
			try {
				this.setBulkCommandStartTime(new Date());
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
				this.setBulkCommandCompleteTime(new Date());
				if(exception != null){
					throw exception;
				}

			}
			catch (Throwable throwable){
				this.setBulkCommandCompleteTime(new Date());
				this.bulkProcessor.increamentFailedSize(this.getBulkDataRecords());
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

				clear();
			}
		}

	}
    protected abstract void clear();

	public long getTotalSize(){
		return bulkProcessor.getTotalSize();
	}

    /**
     * 获取已追加总记录数据
     * @return
     */
    public long getAppendRecords() {
        return bulkProcessor.getAppendRecords();
    }
	public long getTotalFailedSize(){
		return bulkProcessor.getFailedSize();
	}

	public BulkProcessor getBulkProcessor() {
		return bulkProcessor;
	}


}
