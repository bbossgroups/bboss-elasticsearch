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

import com.frameworkset.common.poolman.BatchHandler;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.estodb.ES2DB;
import org.frameworkset.elasticsearch.client.estodb.ES2DBDataStreamImpl;
import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public class ExportBuilder extends BaseBuilder{
	private static Logger logger = LoggerFactory.getLogger(ExportBuilder.class);
	private Integer insertBatchSize ;
	private String scrollLiveTime = "100m";
	private BatchHandler<Map> batchHandler;
	private Map params;
	/**indexName/_search*/
	private String queryUrl;
	private String dsl2ndSqlFile;
	private String dslName;
	private boolean sliceQuery;
	private int sliceSize;
	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(SimpleStringUtil.object2json(this));
		return ret.toString();
	}

	private ES2DB buildES2DBConfig(){
		ES2DB es2DB = new ES2DB();



		es2DB.setDsl2ndSqlFile(this.dsl2ndSqlFile);
		es2DB.setSqlName(sqlName);
		es2DB.setSql(this.sql);
		DBConfig dbConfig = new DBConfig();
		es2DB.setDbConfig(dbConfig);
		dbConfig.setDbName(dbName);
		dbConfig.setShowSql(showSql);
		es2DB.setBatchSize(this.batchSize);
		dbConfig.setJdbcFetchSize(this.jdbcFetchSize);
		es2DB.setQueryUrl(this.queryUrl);
		es2DB.setScrollLiveTime(this.scrollLiveTime);
		dbConfig.setDbDriver(this.dbDriver);
		dbConfig.setDbUrl(this.dbUrl);
		dbConfig.setDbUser(this.dbUser);
		dbConfig.setDbPassword(this.dbPassword);
		dbConfig.setValidateSQL(this.validateSQL);
		dbConfig.setUsePool(this.usePool);
		es2DB.setApplicationPropertiesFile(this.applicationPropertiesFile);
		es2DB.setParallel(this.parallel);
		es2DB.setThreadCount(this.threadCount);
		es2DB.setQueue(this.queue);
		es2DB.setAsyn(this.asyn);
		es2DB.setContinueOnError(this.continueOnError);
		es2DB.setBatchSize(this.batchSize);
		es2DB.setInsertBatchSize(this.insertBatchSize);
		es2DB.setBatchHandler(this.batchHandler);
		es2DB.setDslName(this.dslName);
		es2DB.setSliceQuery(this.sliceQuery);
		es2DB.setSliceSize(this.sliceSize);
		es2DB.setParams(this.params);

/**
 if(this.scheduleBatchSize != null)
 esjdbcResultSet.setScheduleBatchSize(this.scheduleBatchSize);
 else
 esjdbcResultSet.setScheduleBatchSize(this.batchSize);
 esjdbcResultSet.setCallInterceptors(this.callInterceptors);
 esjdbcResultSet.setUseLowcase(this.useLowcase);
 esjdbcResultSet.setPrintTaskLog(this.printTaskLog);
 esjdbcResultSet.setEsIdGenerator(esIdGenerator);*/
		return es2DB;
	}
	public boolean isParallel() {
		return parallel;
	}

	public ExportBuilder setParallel(boolean parallel) {
		this.parallel = parallel;
		return this;
	}
	public DataStream builder(){
		this.buildDBConfig();
		try {
			logger.info("ES2DB Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		ES2DB esjdbcResultSet = this.buildES2DBConfig();
		ES2DBDataStreamImpl dataStream = new ES2DBDataStreamImpl();
		dataStream.setEs2DB(esjdbcResultSet);
		return dataStream;
	}

	public boolean isPrintTaskLog() {
		return printTaskLog;
	}

	public ExportBuilder setPrintTaskLog(boolean printTaskLog) {
		this.printTaskLog = printTaskLog;
		return this;
	}

	public List<CallInterceptor> getCallInterceptors() {
		return callInterceptors;
	}

	public ExportBuilder setCallInterceptors(List<CallInterceptor> callInterceptors) {
		this.callInterceptors = callInterceptors;
		return this;
	}

	public String getApplicationPropertiesFile() {
		return applicationPropertiesFile;
	}

	public ExportBuilder setApplicationPropertiesFile(String applicationPropertiesFile) {
		this.applicationPropertiesFile = applicationPropertiesFile;
		return this;
	}

	public boolean isFreezen() {
		return freezen;
	}

	public ExportBuilder setFreezen(boolean freezen) {
		this.freezen = freezen;
		return this;
	}

	public String getSql() {
		return sql;
	}

	public ExportBuilder setSql(String sql) {
		this.sql = sql;
		return this;
	}


	public String getSqlName() {
		return sqlName;
	}

	public ExportBuilder setSqlName(String sqlName) {
		this.sqlName = sqlName;
		return this;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public ExportBuilder setShowSql(boolean showSql) {
		this.showSql = showSql;
		return this;
	}

	public String getDbName() {
		return dbName;
	}

	public ExportBuilder setDbName(String dbName) {
		this.dbName = dbName;
		return this;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public ExportBuilder setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
		return this;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public ExportBuilder setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
		return this;
	}

	public String getDbUser() {
		return dbUser;
	}

	public ExportBuilder setDbUser(String dbUser) {
		this.dbUser = dbUser;
		return this;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public ExportBuilder setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
		return this;
	}

	public String getValidateSQL() {
		return validateSQL;
	}

	public ExportBuilder setValidateSQL(String validateSQL) {
		this.validateSQL = validateSQL;
		return this;
	}

	public boolean isUsePool() {
		return usePool;
	}

	public ExportBuilder setUsePool(boolean usePool) {
		this.usePool = usePool;
		return this;
	}



	public int getBatchSize() {
		return batchSize;
	}

	public ExportBuilder setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}



	public Integer getInsertBatchSize() {
		return insertBatchSize;
	}

	public ExportBuilder setInsertBatchSize(Integer insertBatchSize) {
		this.insertBatchSize = insertBatchSize;
		return this;
	}

	public String getQueryUrl() {
		return queryUrl;
	}

	public ExportBuilder setQueryUrl(String queryUrl) {
		this.queryUrl = queryUrl;
		return this;
	}

	public BatchHandler<Map> getBatchHandler() {
		return batchHandler;
	}

	public ExportBuilder setBatchHandler(BatchHandler<Map> batchHandler) {
		this.batchHandler = batchHandler;
		return this;
	}

	public String getDsl2ndSqlFile() {
		return dsl2ndSqlFile;
	}

	public ExportBuilder setDsl2ndSqlFile(String dsl2ndSqlFile) {
		this.dsl2ndSqlFile = dsl2ndSqlFile;
		return this;
	}

	public String getDslName() {
		return dslName;
	}

	public ExportBuilder setDslName(String dslName) {
		this.dslName = dslName;
		return this;
	}

	public String getScrollLiveTime() {
		return scrollLiveTime;
	}

	public ExportBuilder setScrollLiveTime(String scrollLiveTime) {
		this.scrollLiveTime = scrollLiveTime;
		return this;
	}

	public boolean isSliceQuery() {
		return sliceQuery;
	}

	public ExportBuilder setSliceQuery(boolean sliceQuery) {
		this.sliceQuery = sliceQuery;
		return this;
	}

	public int getSliceSize() {
		return sliceSize;
	}

	public ExportBuilder setSliceSize(int sliceSize) {
		this.sliceSize = sliceSize;
		return this;
	}
	public ExportBuilder addParam(String key,Object value){
		if(params == null)
			params = new HashMap();
		this.params.put(key,value);
		return this;
	}
}
