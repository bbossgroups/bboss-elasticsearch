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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/12/4 14:16
 * @author biaoping.yin
 * @version 1.0
 */
public class BulkConfig implements BulkActionConfig{
	private List<BulkInterceptor> bulkInterceptors;
	private long blockedWaitTimeout;
	private int warnMultsRejects;
	private String elasticsearch;
	private String bulkProcessorName = "BulkProcessor";
	private String bulkRejectMessage = "Reject bulk processor";
//	private long pollTimeOut = 1000;
	private String refreshOption;
	/**
	 * 失败重试机制
	 */
	private BulkRetryHandler bulkRetryHandler;
	private int retryTimes = 3;
	private long retryInterval = 0l;
	/**
	 * 记录数达到bulkRecords指定的条数时执行一次bulk操作
	 */
	private int bulkSizes;


    /**
     * 设置批量记录占用内存最大值，以字节为单位，达到最大值时，执行一次bulk操作
     * 可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作
     * maxMemSize参数默认值为0，不起作用，只有>0才起作用
     */
    private int maxMemSize;

	/**
	 * 强制bulk操作时间，单位毫秒，如果自上次bulk操作flushInterval毫秒后，数据量没有满足
	 */
	private long flushInterval;
//	private int bulkQueue = 10000;
	private int workThreads = 20;
	private int workThreadQueue = 100;

	private String timeout;
	private String masterTimeout ;
	private Integer waitForActiveShards;
	private String refresh;
	private String pipeline;
	private Object routing;
	/**
	 * {
	 *   "took" : 0,
	 *   "errors" : true,
	 *   "items" : [
	 *     {
	 *       "index" : {
	 *         "_index" : "metrics-test-report",
	 *         "_type" : "_doc",
	 *         "_id" : null,
	 *         "status" : 400,
	 *         "error" : {
	 *           "type" : "illegal_argument_exception",
	 *           "reason" : "only write ops with an op_type of create are allowed in data streams"
	 *         }
	 *       }
	 *     }
	 *   ]
	 * }
	 */
	public static final String ERROR_FILTER_PATH = "took,errors,items.*._index,items.*._type,items.*._id,items.*.status,items.*.error";
	public static final String ERROR_FILTER_PATH_ONLY = "took,errors,items.*.error";
	public static final String FILTER_PATH_EMPTY = "";
	public static final String FILTER_PATH_DISABLE = "FILTER_PATH_DEFAULT";

	private String filterPath = FILTER_PATH_EMPTY;
	public int getBulkSizes() {
		return bulkSizes;
	}

	public BulkConfig setBulkSizes(int bulkSizes) {
		this.bulkSizes = bulkSizes;
		return this;
	}


	public List<BulkInterceptor> getBulkInterceptors() {
		return bulkInterceptors;
	}


	public long getFlushInterval() {
		return flushInterval;
	}

	public BulkConfig setFlushInterval(long flushInterval) {
		this.flushInterval = flushInterval;
		return this;
	}
//
//	public int getBulkQueue() {
//		return bulkQueue;
//	}
//
//	public BulkConfig setBulkQueue(int bulkQueue) {
//		this.bulkQueue = bulkQueue;
//		return this;
//	}

	public int getWorkThreads() {
		return workThreads;
	}

	public BulkConfig setWorkThreads(int workThreads) {
		this.workThreads = workThreads;
		return this;
	}
	public BulkConfig addBulkInterceptor(BulkInterceptor bulkInterceptor){
		if(bulkInterceptors == null){
			bulkInterceptors = new ArrayList<BulkInterceptor>();
		}
		bulkInterceptors.add(bulkInterceptor);
		return this;
	}

	public int getWorkThreadQueue() {
		return workThreadQueue;
	}

	public BulkConfig setWorkThreadQueue(int workThreadQueue) {
		this.workThreadQueue = workThreadQueue;
		return this;
	}

	public long getBlockedWaitTimeout() {
		return blockedWaitTimeout;
	}

	public BulkConfig setBlockedWaitTimeout(long blockedWaitTimeout) {
		this.blockedWaitTimeout = blockedWaitTimeout;
		return this;
	}

	public int getWarnMultsRejects() {
		return warnMultsRejects;
	}

	public BulkConfig setWarnMultsRejects(int warnMultsRejects) {
		this.warnMultsRejects = warnMultsRejects;
		return this;
	}

	public String getBulkProcessorName() {
		return bulkProcessorName;
	}

	public BulkConfig setBulkProcessorName(String bulkProcessorName) {
		this.bulkProcessorName = bulkProcessorName;
		return this;
	}

	public String getBulkRejectMessage() {
		return bulkRejectMessage;
	}

	public BulkConfig setBulkRejectMessage(String bulkRejectMessage) {
		this.bulkRejectMessage = bulkRejectMessage;
		return this;
	}
//
//	public long getPollTimeOut() {
//		return pollTimeOut;
//	}
//
//	public BulkConfig setPollTimeOut(long pollTimeOut) {
//		this.pollTimeOut = pollTimeOut;
//		return this;
//	}

	public String getElasticsearch() {
		return elasticsearch;
	}

	public BulkConfig setElasticsearch(String elasticsearch) {
		this.elasticsearch = elasticsearch;
		return this;
	}

	public String getRefreshOption() {
		return refreshOption;
	}

	public BulkConfig setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
		return this;
	}

	public String getPipeline() {
		return pipeline;
	}

	public BulkConfig setPipeline(String pipeline) {
		this.pipeline = pipeline;
		return this;
	}

	public String getRefresh() {
		return refresh;
	}

	public BulkConfig setRefresh(String refresh) {
		this.refresh = refresh;
		return this;
	}

	public Integer getWaitForActiveShards() {
		return waitForActiveShards;
	}

	public BulkConfig setWaitForActiveShards(Integer waitForActiveShards) {
		this.waitForActiveShards = waitForActiveShards;
		return this;
	}

	public String getMasterTimeout() {
		return masterTimeout;
	}

	public BulkConfig setMasterTimeout(String masterTimeout) {
		this.masterTimeout = masterTimeout;
		return this;
	}

	public String getTimeout() {
		return timeout;
	}

	public BulkConfig setTimeout(String timeout) {
		this.timeout = timeout;
		return this;
	}

	public Object getRouting() {
		return routing;
	}

	public void setRouting(Object routing) {
		this.routing = routing;
	}

	public BulkRetryHandler getBulkRetryHandler() {
		return bulkRetryHandler;
	}

	public void setBulkRetryHandler(BulkRetryHandler bulkRetryHandler) {
		this.bulkRetryHandler = bulkRetryHandler;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public long getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

	public String getFilterPath() {
		return filterPath;
	}

	public void setFilterPath(String filterPath) {
		this.filterPath = filterPath;
	}

    /**

     * 设置批量记录占用内存最大值，以字节为单位，达到最大值时，执行一次bulk操作
     * 可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作
     * maxMemSize参数默认值为0，不起作用，只有>0才起作用
     */
    public int getMaxMemSize() {
        return maxMemSize;
    }
    /**

     * 设置批量记录占用内存最大值，以字节为单位，达到最大值时，执行一次bulk操作
     * 可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作
     * maxMemSize参数默认值为0，不起作用，只有>0才起作用
     */
    public void setMaxMemSize(int maxMemSize) {
        this.maxMemSize = maxMemSize;
    }
}
