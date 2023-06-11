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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/12/7 9:44
 * @author biaoping.yin
 * @version 1.0
 */
public class BulkProcessorBuilder {
	private static Logger logger = LoggerFactory.getLogger(BulkProcessorBuilder.class);
	private BulkConfig bulkConfig;
	public BulkProcessorBuilder(){
		bulkConfig = new BulkConfig();
	}
	public BulkProcessorBuilder setBulkRetryHandler(BulkRetryHandler bulkRetryHandler) {
		this.bulkConfig.setBulkRetryHandler( bulkRetryHandler);
		return this;
	}


	public BulkProcessorBuilder setRetryTimes(int retryTimes) {
		this.bulkConfig.setRetryTimes( retryTimes);
		return this;
	}



	public BulkProcessorBuilder setRetryInterval(long retryInterval) {
		this.bulkConfig.setRetryInterval( retryInterval);
		return this;
	}


	public BulkProcessorBuilder setBulkSizes(int bulkSizes) {
		bulkConfig.setBulkSizes(bulkSizes);
		return this;
	}





	public BulkProcessorBuilder setFlushInterval(long flushInterval) {
		this.bulkConfig.setFlushInterval(flushInterval);
		return this;
	}
	public BulkProcessorBuilder setWorkThreadQueue(int workThreadQueue) {
		this.bulkConfig.setWorkThreadQueue( workThreadQueue);
		return this;
	}

//	public BulkProcessorBuilder setBulkQueue(int bulkQueue) {
//		this.bulkConfig.setBulkQueue(bulkQueue);
//		return this;
//	}


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
		if(logger.isInfoEnabled()){
			logger.info("Build Elasticsearch client BulkProcessor["+this.bulkConfig.getBulkProcessorName()+"] start.....");
		}
		BulkProcessor bulkProcessor = new BulkProcessor(this.bulkConfig);
//		bulkProcessor.init();
		if(logger.isInfoEnabled()){
			logger.info("Build  Elasticsearch client BulkProcessor["+this.bulkConfig.getBulkProcessorName()+"] completed.");
		}
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
//	public BulkProcessorBuilder setPollTimeOut(long pollTimeOut) {
//		this.bulkConfig.setPollTimeOut( pollTimeOut);
//		return this;
//	}
	public BulkProcessorBuilder setBulkRejectMessage(String bulkRejectMessage) {
		if(bulkRejectMessage == null)
			bulkRejectMessage = "Reject bulk processor";
		this.bulkConfig.setBulkRejectMessage( bulkRejectMessage);
		return this;
	}
	public BulkProcessorBuilder setBulkProcessorName(String bulkProcessorName) {
		if(bulkProcessorName == null)
			bulkProcessorName = "BulkProcessor";
		this.bulkConfig.setBulkProcessorName( bulkProcessorName);
		return this;
	}



	public BulkProcessorBuilder setTimeout(String timeout) {
		this.bulkConfig.setTimeout( timeout);
		return this;
	}

	public BulkProcessorBuilder setMasterTimeout(String masterTimeout) {
		this.bulkConfig.setMasterTimeout( masterTimeout);
		return this;
	}

	public BulkProcessorBuilder setWaitForActiveShards(Integer waitForActiveShards) {
		this.bulkConfig.setWaitForActiveShards( waitForActiveShards);
		return this;
	}

	public BulkProcessorBuilder setRefresh(String refresh) {
		this.bulkConfig.setRefresh( refresh);
		return this;
	}

	public BulkProcessorBuilder setPipeline(String pipeline) {
		this.bulkConfig.setPipeline( pipeline);
		return this;
	}

	public BulkProcessorBuilder setRouting(Object routing) {
		this.bulkConfig.setRouting( routing);
		return this;
	}
	public BulkProcessorBuilder setFilterPath(String filterPath){
		this.bulkConfig.setFilterPath(filterPath);
		return this;
	}
    /**
     * 设置批量记录占用内存最大值，以字节为单位，达到最大值时，执行一次bulk操作
     * 可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作
     * maxMemSize参数默认值为0，不起作用，只有>0才起作用
     */
    public BulkProcessorBuilder setMaxMemSize(int maxMemSize) {
        this.bulkConfig.setMaxMemSize(maxMemSize);
        return this;
    }
}
