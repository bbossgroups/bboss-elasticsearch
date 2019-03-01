package org.frameworkset.elasticsearch.client.estodb;
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

import com.frameworkset.common.poolman.BatchHandler;
import com.frameworkset.common.poolman.ConfigSQLExecutor;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.DBConfig;
import org.frameworkset.elasticsearch.client.ExportResultHandler;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 15:10
 * @author biaoping.yin
 * @version 1.0
 */
public class ES2DB {
	//scroll分页检索，每批查询数据大小
	private int batchSize = 5000;
	private Integer insertBatchSize ;

	private String sql ;//= "insert into batchtest (name) values(?)";
	private String queryUrl;// = "demo/_search";
	private String dslName ;//= "scrollQuery";
	private String sqlName ;//= "insertSQL";
	private String dsl2ndSqlFile;// = "esmapper/dsl2ndSqlFile.xml";
	private String scrollLiveTime ;//= "100m";
	private BatchHandler<Map> batchHandler;
	private DBConfig dbConfig;
	private ConfigSQLExecutor configSQLExecutor;
	private ExportCount exportCount;
	/**
	 * use parallel import:
	 *  true yes
	 *  false no
	 */
	private boolean parallel;
	/**
	 * parallel import work thread nums,default 200
	 */
	private int threadCount = 200;
	private int queue = Integer.MAX_VALUE;
	private String applicationPropertiesFile;
	/**
	 * 是否同步等待批处理作业结束，true 等待 false 不等待
	 */
	private boolean asyn;

	private boolean sliceQuery;
	private int sliceSize;
	/**
	 * 并行执行过程中出现异常终端后续作业处理，已经创建的作业会执行完毕
	 */
	private boolean continueOnError = true;
	private ExportResultHandler exportResultHandler;
	private static Logger logger = LoggerFactory.getLogger(ES2DB.class);
	public Map getParams() {
		return params;
	}

	public void setParams(Map params) {
		this.params = params;
	}

	private Map params;

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}


	public String getDslName() {
		return dslName;
	}

	public void setDslName(String dslName) {
		this.dslName = dslName;
	}

	public String getSqlName() {
		return sqlName;
	}

	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}

	public String getDsl2ndSqlFile() {
		return dsl2ndSqlFile;
	}

	public void setDsl2ndSqlFile(String dsl2ndSqlFile) {
		this.dsl2ndSqlFile = dsl2ndSqlFile;
	}

	public String getScrollLiveTime() {
		return scrollLiveTime;
	}

	public void setScrollLiveTime(String scrollLiveTime) {
		this.scrollLiveTime = scrollLiveTime;
	}

	public BatchHandler getBatchHandler() {
		return batchHandler;
	}

	public void setBatchHandler(BatchHandler batchHandler) {
		this.batchHandler = batchHandler;
	}

	public void exportData2DB(){
		Map params = getParams() != null ?getParams():new HashMap();
		params.put("size", getBatchSize());//每页5000条记录
		if(this.sliceQuery){
			params.put("sliceMax",this.sliceSize);
		}
		final int insertBatchSize = this.insertBatchSize == null ?this.getBatchSize():this.insertBatchSize;
		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟；大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险
		configSQLExecutor = getSql() == null ?new ConfigSQLExecutor(getDsl2ndSqlFile()):null;
		exportCount = new ExportCount();
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(this.getDsl2ndSqlFile());
		ESExporterScrollHandler esExporterScrollHandler = new ESExporterScrollHandler(this);
		ESDatas<Map> response = null;
		if(!this.sliceQuery) {
			if(!this.isParallel()) {
				response = clientUtil.scroll(getQueryUrl(), getDslName(), getScrollLiveTime(), params, Map.class, esExporterScrollHandler);
			}
			else
			{
				response = clientUtil.scrollParallel(getQueryUrl(), getDslName(), getScrollLiveTime(), params, Map.class, esExporterScrollHandler);
			}
		}
		else{
			response = clientUtil.scrollSliceParallel(getQueryUrl(), getDslName(),  params, getScrollLiveTime(),Map.class, esExporterScrollHandler);
		}
		if(logger.isInfoEnabled()) {
			if(response != null) {
				logger.info("Export compoleted and export total {} records.", response.getTotalSize());
			}
			else{
				logger.info("Export compoleted and export no records or failed.");
			}
		}
	}

	public DBConfig getDbConfig() {
		return dbConfig;
	}

	public void setDbConfig(DBConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	public String getApplicationPropertiesFile() {
		return applicationPropertiesFile;
	}

	public void setApplicationPropertiesFile(String applicationPropertiesFile) {
		this.applicationPropertiesFile = applicationPropertiesFile;
	}

	public int getQueue() {
		return queue;
	}

	public void setQueue(int queue) {
		this.queue = queue;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	public boolean isAsyn() {
		return asyn;
	}

	public void setAsyn(boolean asyn) {
		this.asyn = asyn;
	}

	public boolean isContinueOnError() {
		return continueOnError;
	}

	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	public Integer getInsertBatchSize() {
		return insertBatchSize;
	}

	public void setInsertBatchSize(Integer insertBatchSize) {
		this.insertBatchSize = insertBatchSize;
	}

	public String getQueryUrl() {
		return queryUrl;
	}

	public void setQueryUrl(String queryUrl) {
		this.queryUrl = queryUrl;
	}

	public boolean isSliceQuery() {
		return sliceQuery;
	}

	public void setSliceQuery(boolean sliceQuery) {
		this.sliceQuery = sliceQuery;
	}

	public int getSliceSize() {
		return sliceSize;
	}

	public void setSliceSize(int sliceSize) {
		this.sliceSize = sliceSize;
	}
	public ConfigSQLExecutor getConfigSQLExecutor(){
		return this.configSQLExecutor;
	}



	public ExportCount getExportCount() {
		return exportCount;
	}

	public ExportResultHandler getExportResultHandler() {
		return exportResultHandler;
	}

	public void setExportResultHandler(ExportResultHandler exportResultHandler) {
		this.exportResultHandler = exportResultHandler;
	}
}
