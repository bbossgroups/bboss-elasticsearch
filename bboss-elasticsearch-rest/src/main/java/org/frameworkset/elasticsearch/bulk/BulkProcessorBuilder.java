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

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/12/7 9:44
 * @author biaoping.yin
 * @version 1.0
 */
public class BulkProcessorBuilder {
	private BulkConfig bulkConfig;
	public BulkProcessorBuilder(){
		bulkConfig = new BulkConfig();
	}



	public BulkProcessorBuilder setBulkSizes(int bulkSizes) {
		bulkConfig.setBulkSizes(bulkSizes);
		return this;
	}


	public BulkProcessorBuilder setBulkFailRetry(int bulkFailRetry) {
		bulkConfig.setBulkFailRetry(bulkFailRetry);
		return this;
	}


	public BulkProcessorBuilder setFlushInterval(long flushInterval) {
		this.bulkConfig.setFlushInterval(flushInterval);
		return this;
	}


	public BulkProcessorBuilder setBulkQueue(int bulkQueue) {
		this.bulkConfig.setBulkQueue(bulkQueue);
		return this;
	}


	public BulkProcessorBuilder setWorkThreads(int workThreads) {
		this.bulkConfig.setWorkThreads(workThreads);
		return this;
	}
	public BulkProcessorBuilder addBulkInterceptor(BulkInterceptor bulkInterceptor){
		this.bulkConfig.addBulkInterceptor(bulkInterceptor);
		return this;
	}
	public BulkProcessorBuilder setBlockedWaitTimeout(long blockedWaitTimeout) {
		bulkConfig.setBlockedWaitTimeout(blockedWaitTimeout);
		return this;
	}


	public BulkProcessorBuilder setWarnMultsRejects(int warnMultsRejects) {
		this.bulkConfig.setWarnMultsRejects( warnMultsRejects);
		return this;
	}
	public BulkProcessor build(){
		if(bulkConfig == null){
			throw new BulkProcessorException("build BulkProcessor failed:bulkConfig is null.");
		}
		BulkProcessor bulkProcessor = new BulkProcessor(this.bulkConfig);
		bulkProcessor.init();
		return bulkProcessor;
	}

	/**
	 * 设置elasticsearch集群数据源名称
	 * @param elasticsearch
	 * @return
	 */
	public BulkProcessorBuilder setElasticsearch(String elasticsearch) {
		bulkConfig.setElasticsearch(elasticsearch);
		return this;
	}
	public BulkProcessorBuilder setRefreshOption(String refreshOption) {
		bulkConfig.setRefreshOption(refreshOption);
		return this;
	}
}
