package org.frameworkset.elasticsearch.client;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.frameworkset.common.poolman.StatementInfo;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.elasticsearch.client.schedule.ImportIncreamentConfig;
import org.frameworkset.elasticsearch.client.schedule.ScheduleConfig;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;

public class ImportBuilder {
	private static Logger logger = LoggerFactory.getLogger(ImportBuilder.class);
	/**
	 * 打印任务日志
	 */
	private boolean printTaskLog = false;
	private ImportBuilder(){

	}
	/**
	 * 定时任务拦截器
	 */
	private List<CallInterceptor> callInterceptors;
	private String applicationPropertiesFile;
	private boolean freezen;
	/**抽取数据的sql语句*/
	private String sql;
	private String sqlFilepath;
	private String sqlName;
	private Integer jdbcFetchSize;
	private EsIdGenerator esIdGenerator = ESJDBC.DEFAULT_EsIdGenerator;
	
	/**是否启用sql日志，true启用，false 不启用，*/
	private boolean showSql;

	public boolean isShowSql() {
		return showSql;
	}

	public ImportBuilder setShowSql(boolean showSql) {
		this.showSql = showSql;
		return this;
	}
	public Boolean getUseLowcase() {
		return useLowcase;
	}

	public ImportBuilder setUseLowcase(Boolean useLowcase) {
		this.useLowcase = useLowcase;
		return this;
	}

	private Boolean useLowcase;
	/**抽取数据的sql语句*/
	private String dbName;
	/**抽取数据的sql语句*/
	private String dbDriver;
	/**抽取数据的sql语句*/
	private String dbUrl;
	/**抽取数据的sql语句*/
	private String dbUser;
	/**抽取数据的sql语句*/
	private String dbPassword;
	/**抽取数据的sql语句*/
	private String validateSQL;
	/**抽取数据的sql语句*/
	private boolean usePool = false;
	/**抽取数据的sql语句*/
	private String refreshOption;
	/**抽取数据的sql语句*/
	private int batchSize = 1000;
	private Integer scheduleBatchSize ;
	private String index;
	/**抽取数据的sql语句*/
	private String indexType;
	/**抽取数据的sql语句*/
	private String esIdField;
	/**抽取数据的sql语句*/
	private String esParentIdField;
	/**抽取数据的sql语句*/
	private String esParentIdValue;
	/**抽取数据的sql语句*/
	private String routingField;
	/**抽取数据的sql语句*/
	private String routingValue;
	/**抽取数据的sql语句*/
	private Boolean esDocAsUpsert;
	/**抽取数据的sql语句*/
	private Integer esRetryOnConflict;
	/**抽取数据的sql语句*/
	private Boolean esReturnSource;
	/**抽取数据的sql语句*/
	private String esVersionField;
	/**抽取数据的sql语句*/
	private Object esVersionValue;
	/**抽取数据的sql语句*/
	private String esVersionType;
	/**抽取数据的sql语句*/
	private Boolean useJavaName;
	/**抽取数据的sql语句*/
	private String dateFormat;
	/**抽取数据的sql语句*/
	private String locale;
	/**抽取数据的sql语句*/
	private String timeZone;
	/**抽取数据的sql语句*/
	private ResultSet resultSet;
	/**抽取数据的sql语句*/
	private StatementInfo statementInfo;
	/**
	 * 是否不需要返回响应，不需要的情况下，可以设置为true，
	 * 提升性能，如果debugResponse设置为true，那么强制返回并打印响应到日志文件中
	 */
	private boolean discardBulkResponse = true;
	/**是否调试bulk响应日志，true启用，false 不启用，*/
	private boolean debugResponse;
	private ScheduleConfig scheduleConfig;
	private ImportIncreamentConfig importIncreamentConfig;

	public boolean isFreezen() {
		return freezen;
	}

	public void setFreezen(boolean freezen) {
		this.freezen = freezen;
	}

	public String getSql() {
		return sql;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public String getValidateSQL() {
		return validateSQL;
	}

	public boolean isUsePool() {
		return usePool;
	}

	public String getRefreshOption() {
		return refreshOption;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public String getIndex() {
		return index;
	}

	public String getIndexType() {
		return indexType;
	}

	public String getEsIdField() {
		return esIdField;
	}

	public String getEsParentIdField() {
		return esParentIdField;
	}

	public String getRoutingField() {
		return routingField;
	}

	public String getRoutingValue() {
		return routingValue;
	}

	public Boolean getEsDocAsUpsert() {
		return esDocAsUpsert;
	}

	public Integer getEsRetryOnConflict() {
		return esRetryOnConflict;
	}

	public Boolean getEsReturnSource() {
		return esReturnSource;
	}

	public String getEsVersionField() {
		return esVersionField;
	}

	public String getEsVersionType() {
		return esVersionType;
	}

	public Boolean getUseJavaName() {
		return useJavaName;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getLocale() {
		return locale;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public boolean isContinueOnError() {
		return continueOnError;
	}

	private Map<String,FieldMeta> fieldMetaMap = new HashMap<String,FieldMeta>();

	private List<FieldMeta> fieldValues = new ArrayList<FieldMeta>();
	private DataRefactor dataRefactor;
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
	/**
	 * 并行队列大小，默认1000
	 */
	private int queue = 1000;
	/**
	 * 是否同步等待批处理作业结束，true 等待 false 不等待
	 */
	private boolean asyn;
	/**
	 * 并行执行过程中出现异常终端后续作业处理，已经创建的作业会执行完毕
	 */
	private boolean continueOnError;
	public static ImportBuilder newInstance(){
		return new ImportBuilder();
	}
	public ImportBuilder setResultSet(ResultSet resultSet){
		this.resultSet = resultSet;
		return this;
	}

	public ImportBuilder setStatementInfo(StatementInfo statementInfo){
		this.statementInfo = statementInfo;
		return this;
	}
	private FieldMeta buildFieldMeta(String dbColumnName,String esFieldName ,String dateFormat){
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setDbColumnName(dbColumnName);
		fieldMeta.setEsFieldName(esFieldName);
		fieldMeta.setIgnore(false);
		fieldMeta.setDateFormateMeta(dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,  timeZone));
		return fieldMeta;
	}

	private static FieldMeta buildIgnoreFieldMeta(String dbColumnName){
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setDbColumnName(dbColumnName);

		fieldMeta.setIgnore(true);
		return fieldMeta;
	}
	private FieldMeta buildFieldMeta(String dbColumnName,String esFieldName ,String dateFormat,String locale,String timeZone){
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setDbColumnName(dbColumnName);
		fieldMeta.setEsFieldName(esFieldName);
		fieldMeta.setIgnore(false);
		fieldMeta.setDateFormateMeta(dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone));
		return fieldMeta;
	}
	public ImportBuilder addFieldMapping(String dbColumnName,String esFieldName){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName,null ));
		return this;
	}

	public ImportBuilder addIgnoreFieldMapping(String dbColumnName){
		addIgnoreFieldMapping(fieldMetaMap, dbColumnName);
		return this;
	}

	public static void addIgnoreFieldMapping(Map<String,FieldMeta> fieldMetaMap,String dbColumnName){
		fieldMetaMap.put(dbColumnName.toLowerCase(),buildIgnoreFieldMeta(  dbColumnName));
	}

	public ImportBuilder addFieldMapping(String dbColumnName,String esFieldName,String dateFormat){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat));
		return this;
	}

	public ImportBuilder addFieldMapping(String dbColumnName,String esFieldName,String dateFormat,String locale,String timeZone){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat,  locale,  timeZone));
		return this;
	}






	public ImportBuilder setTimeZone(String timeZone) {
		this.timeZone = timeZone;
		return this;
	}

	public ImportBuilder setLocale(String locale) {
		this.locale = locale;
		return this;
	}

	public ImportBuilder setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	public ImportBuilder setUseJavaName(Boolean useJavaName) {
		this.useJavaName = useJavaName;
		return this;
	}

	public ImportBuilder setEsVersionType(String esVersionType) {
		this.esVersionType = esVersionType;
		return this;
	}

	public ImportBuilder setEsVersionField(String esVersionField) {
		this.esVersionField = esVersionField;
		return this;
	}

	public ImportBuilder setEsReturnSource(Boolean esReturnSource) {
		this.esReturnSource = esReturnSource;
		return this;
	}

	public ImportBuilder setEsRetryOnConflict(Integer esRetryOnConflict) {
		this.esRetryOnConflict = esRetryOnConflict;
		return this;
	}

	public ImportBuilder setEsDocAsUpsert(Boolean esDocAsUpsert) {
		this.esDocAsUpsert = esDocAsUpsert;
		return this;
	}

	public ImportBuilder setRoutingValue(String routingValue) {
		this.routingValue = routingValue;
		return this;
	}

	public ImportBuilder setRoutingField(String routingField) {
		this.routingField = routingField;
		return this;
	}

	public ImportBuilder setEsParentIdField(String esParentIdField) {
		this.esParentIdField = esParentIdField;
		return this;
	}

	public ImportBuilder setEsIdField(String esIdField) {
		this.esIdField = esIdField;
		return this;
	}

	private void buildDBConfig(){
		if(!this.freezen) {
			PropertiesContainer propertiesContainer = new PropertiesContainer();
			if(this.applicationPropertiesFile == null) {
				propertiesContainer.addConfigPropertiesFile("application.properties");
			}
			else{
				propertiesContainer.addConfigPropertiesFile(applicationPropertiesFile);
			}
			this.dbName  = propertiesContainer.getProperty("db.name");
			this.dbUser  = propertiesContainer.getProperty("db.user");
			this.dbPassword  = propertiesContainer.getProperty("db.password");
			this.dbDriver  = propertiesContainer.getProperty("db.driver");
			this.dbUrl  = propertiesContainer.getProperty("db.url");
			String _usePool = propertiesContainer.getProperty("db.usePool");
			if(_usePool != null && !_usePool.equals(""))
				this.usePool  = Boolean.parseBoolean(_usePool);
			this.validateSQL  = propertiesContainer.getProperty("db.validateSQL");
			
			String _showSql = propertiesContainer.getProperty("db.showsql");
			if(_showSql != null && !_showSql.equals(""))
				this.showSql  = Boolean.parseBoolean(_showSql);
			
			String _jdbcFetchSize = propertiesContainer.getProperty("db.jdbcFetchSize");
			if(_jdbcFetchSize != null && !_jdbcFetchSize.equals(""))
				this.jdbcFetchSize  = Integer.parseInt(_jdbcFetchSize);
			
			 

		}
	}
	private ESJDBC buildESConfig(){
		ESJDBC esjdbcResultSet = new ESJDBC();
		esjdbcResultSet.setImportBuilder(this);
//		esjdbcResultSet.setMetaData(statementInfo.getMeta());
//		esjdbcResultSet.setResultSet(resultSet);
		esjdbcResultSet.setDateFormat(dateFormat);
		esjdbcResultSet.setLocale(locale);
		esjdbcResultSet.setTimeZone(this.timeZone);
		esjdbcResultSet.setEsDocAsUpsert(this.esDocAsUpsert);
		esjdbcResultSet.setEsIdField(this.esIdField);
		esjdbcResultSet.setEsParentIdField(esParentIdField);
		esjdbcResultSet.setEsParentIdValue(esParentIdValue);
		esjdbcResultSet.setEsRetryOnConflict(esRetryOnConflict);
		esjdbcResultSet.setEsReturnSource(esReturnSource);
		esjdbcResultSet.setEsVersionField(esVersionField);
		esjdbcResultSet.setEsVersionValue(esVersionValue);
		esjdbcResultSet.setEsVersionType(esVersionType);

		esjdbcResultSet.setRoutingField(this.routingField);
		esjdbcResultSet.setRoutingValue(this.routingValue);
		esjdbcResultSet.setUseJavaName(this.useJavaName);
		esjdbcResultSet.setFieldMetaMap(this.fieldMetaMap);
		esjdbcResultSet.setFieldValues(fieldValues);
		esjdbcResultSet.setDataRefactor(this.dataRefactor);
		esjdbcResultSet.setSqlFilepath(this.sqlFilepath);
		esjdbcResultSet.setSqlName(sqlName);
		if(SimpleStringUtil.isNotEmpty(sql))
			esjdbcResultSet.setSql(this.sql);

		esjdbcResultSet.setDbName(dbName);
		esjdbcResultSet.setShowSql(showSql);
		esjdbcResultSet.setRefreshOption(this.refreshOption);
		esjdbcResultSet.setBatchSize(this.batchSize);
		esjdbcResultSet.setJdbcFetchSize(this.jdbcFetchSize);
		esjdbcResultSet.setIndex(index);
		esjdbcResultSet.setIndexPattern(this.splitIndexName(index));
		esjdbcResultSet.setIndexType(indexType);
		esjdbcResultSet.setDbDriver(this.dbDriver);
		esjdbcResultSet.setDbUrl(this.dbUrl);
		esjdbcResultSet.setDbUser(this.dbUser);
		esjdbcResultSet.setDbPassword(this.dbPassword);
		esjdbcResultSet.setValidateSQL(this.validateSQL);
		esjdbcResultSet.setUsePool(this.usePool);
		esjdbcResultSet.setApplicationPropertiesFile(this.applicationPropertiesFile);
		esjdbcResultSet.setParallel(this.parallel);
		esjdbcResultSet.setThreadCount(this.threadCount);
		esjdbcResultSet.setQueue(this.queue);
		esjdbcResultSet.setAsyn(this.asyn);
		esjdbcResultSet.setContinueOnError(this.continueOnError);
		/**
		 * 是否不需要返回响应，不需要的情况下，可以设置为true，
		 * 提升性能，如果debugResponse设置为true，那么强制返回并打印响应到日志文件中
		 */
		esjdbcResultSet.setDiscardBulkResponse(this.discardBulkResponse);
		/**是否调试bulk响应日志，true启用，false 不启用，*/
		esjdbcResultSet.setDebugResponse(this.debugResponse);
		esjdbcResultSet.setScheduleConfig(this.scheduleConfig);//定时任务配置
		esjdbcResultSet.setImportIncreamentConfig(this.importIncreamentConfig);//增量数据配置

		if(this.scheduleBatchSize != null)
			esjdbcResultSet.setScheduleBatchSize(this.scheduleBatchSize);
		else
			esjdbcResultSet.setScheduleBatchSize(this.batchSize);
		esjdbcResultSet.setCallInterceptors(this.callInterceptors);
		esjdbcResultSet.setUseLowcase(this.useLowcase);
		esjdbcResultSet.setPrintTaskLog(this.printTaskLog);
		esjdbcResultSet.setEsIdGenerator(esIdGenerator);
		return esjdbcResultSet;
	}
	public DataStream builder(){
		this.buildDBConfig();
		try {
			logger.info("DB2ES Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		ESJDBC esjdbcResultSet = this.buildESConfig();
		DataStream dataStream = new DataStream();
		dataStream.setEsjdbc(esjdbcResultSet);
		return dataStream;
	}

	public ImportBuilder setIndexType(String indexType) {
		this.indexType = indexType;
		return this;
	}

	public ImportBuilder setIndex(String index) {
		this.index = index;
		return this;
	}

	public ImportBuilder setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public ImportBuilder setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
		return this;
	}

	public ImportBuilder setDbName(String dbName) {
		freezen = true;
		this.dbName = dbName;
		return this;
	}

	public ImportBuilder setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public ImportBuilder setDbDriver(String dbDriver) {
		freezen = true;
		this.dbDriver = dbDriver;
		return this;
	}

	public ImportBuilder setDbUrl(String dbUrl) {
		freezen = true;
		this.dbUrl = dbUrl;
		return this;
	}

	public ImportBuilder setDbUser(String dbUser) {
		freezen = true;
		this.dbUser = dbUser;
		return this;
	}

	public ImportBuilder setDbPassword(String dbPassword) {
		freezen = true;
		this.dbPassword = dbPassword;
		return this;
	}

	public ImportBuilder setValidateSQL(String validateSQL) {
		freezen = true;
		this.validateSQL = validateSQL;
		return this;
	}

	public ImportBuilder setUsePool(boolean usePool) {
		freezen = true;
		this.usePool = usePool;
		return this;
	}

	public String getApplicationPropertiesFile() {
		return applicationPropertiesFile;
	}

	public void setApplicationPropertiesFile(String applicationPropertiesFile) {
		this.applicationPropertiesFile = applicationPropertiesFile;
	}

	public boolean isParallel() {
		return parallel;
	}

	public ImportBuilder setParallel(boolean parallel) {
		this.parallel = parallel;
		return this;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public ImportBuilder setThreadCount(int threadCount) {
		this.threadCount = threadCount;
		return this;
	}

	public int getQueue() {
		return queue;
	}

	public void setQueue(int queue) {
		this.queue = queue;
	}

	public boolean isAsyn() {
		return asyn;
	}

	public ImportBuilder setAsyn(boolean asyn) {
		this.asyn = asyn;
		return this;
	}

	public ImportBuilder setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
		return this;
	}

	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public ImportBuilder addFieldValue(String fieldName,Object value){
		addFieldValue(  fieldValues,  fieldName,  value);
		return this;
	}

	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param dateFormat
	 * @param value
	 * @return
	 */
	public ImportBuilder addFieldValue(String fieldName,String dateFormat,Object value){
		addFieldValue(  fieldValues,  fieldName,  dateFormat,  value,  locale,  timeZone);
		return this;
	}
	public ImportBuilder addFieldValue(String fieldName,String dateFormat,Object value,String locale,String timeZone){
		addFieldValue(  fieldValues,  fieldName,  dateFormat,  value,  locale,  timeZone);
		return this;
	}
	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public static void addFieldValue(List<FieldMeta> fieldValues,String fieldName,Object value){
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setEsFieldName(fieldName);
		fieldMeta.setValue(value);
		fieldValues.add(fieldMeta);
	}


	public static void addFieldValue(List<FieldMeta> fieldValues,String fieldName,String dateFormat,Object value,String locale,String timeZone){
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setEsFieldName(fieldName);
		fieldMeta.setValue(value);
		fieldMeta.setDateFormateMeta(buildDateFormateMeta( dateFormat,  locale,  timeZone));
		fieldValues.add(fieldMeta);

	}

	public DateFormateMeta buildDateFormateMeta(String dateFormat){
		return dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone);
	}

	public static DateFormateMeta buildDateFormateMeta(String dateFormat,String locale,String timeZone){
		return dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone);
	}

	public DataRefactor getDataRefactor() {
		return dataRefactor;
	}

	public ImportBuilder setDataRefactor(DataRefactor dataRefactor) {
		this.dataRefactor = dataRefactor;
		return this;
	}

	public String getEsParentIdValue() {
		return esParentIdValue;
	}

	public void setEsParentIdValue(String esParentIdValue) {
		this.esParentIdValue = esParentIdValue;
	}

	public Object getEsVersionValue() {
		return esVersionValue;
	}

	public ImportBuilder setEsVersionValue(Object esVersionValue) {
		this.esVersionValue = esVersionValue;
		return this;
	}

	public boolean isDiscardBulkResponse() {
		return discardBulkResponse;
	}

	public ImportBuilder setDiscardBulkResponse(boolean discardBulkResponse) {
		this.discardBulkResponse = discardBulkResponse;
		return this;
	}

	public boolean isDebugResponse() {
		return debugResponse;
	}

	public ImportBuilder setDebugResponse(boolean debugResponse) {
		this.debugResponse = debugResponse;
		return this;
	}


	public ImportBuilder setPeriod(Long period) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setPeriod(period);
		return this;
	}


	public ImportBuilder setDeyLay(Long deyLay) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setDeyLay(deyLay);
		return this;
	}



	public ImportBuilder setScheduleDate(Date scheduleDate) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setScheduleDate(scheduleDate);
		return this;
	}

	public ImportBuilder setFixedRate(Boolean fixedRate) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setFixedRate(fixedRate);
		return this;
	}

	public ScheduleConfig getScheduleConfig() {
		return scheduleConfig;
	}

	public ImportIncreamentConfig getImportIncreamentConfig() {
		return importIncreamentConfig;
	}

	public ImportBuilder setDateLastValueColumn(String dateLastValueColumn) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setDateLastValueColumn(dateLastValueColumn);
		return this;
	}


	public ImportBuilder setNumberLastValueColumn(String numberLastValueColumn) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setNumberLastValueColumn(numberLastValueColumn);
		return this;
	}


	public ImportBuilder setLastValueStorePath(String lastValueStorePath) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueStorePath(lastValueStorePath);
		return this;
	}



	public ImportBuilder setLastValueStoreTableName(String lastValueStoreTableName) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueStoreTableName(lastValueStoreTableName);
		return this;
	}

	public ImportBuilder setFromFirst(boolean fromFirst) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setFromFirst(fromFirst);
		return this;
	}

	public ImportBuilder setLastValue(Long lastValue) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValue(lastValue);
		return this;
	}

	public ImportBuilder setLastValueType(int lastValueType) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueType(lastValueType);
		return this;
	}


	public ImportBuilder setSqlFilepath(String sqlFilepath) {
		this.sqlFilepath = sqlFilepath;
		return this;
	}

	public ImportBuilder setJdbcFetchSize(Integer jdbcFetchSize) {
		this.jdbcFetchSize = jdbcFetchSize;
		return  this;
	}

	public Integer getScheduleBatchSize() {
		return scheduleBatchSize;
	}

	public ImportBuilder setScheduleBatchSize(Integer scheduleBatchSize) {
		this.scheduleBatchSize = scheduleBatchSize;
		return this;
	}
	public ImportBuilder addCallInterceptor(CallInterceptor interceptor){
		if(this.callInterceptors == null){
			this.callInterceptors = new ArrayList<CallInterceptor>();
		}
		this.callInterceptors.add(interceptor);
		return this;
	}

	public boolean isPrintTaskLog() {
		return printTaskLog;
	}

	public ImportBuilder setPrintTaskLog(boolean printTaskLog) {
		this.printTaskLog = printTaskLog;
		return this;
	}

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(SimpleStringUtil.object2json(this));
		return ret.toString();
	}

	public String getSqlName() {
		return sqlName;
	}

	public ImportBuilder setSqlName(String sqlName) {
		this.sqlName = sqlName;
		return this;
	}

	public ImportBuilder setEsIdGenerator(EsIdGenerator esIdGenerator) {
		if(esIdGenerator != null)
			this.esIdGenerator = 	esIdGenerator;
		return this;
	}

	private IndexPattern splitIndexName(String indexPattern){
		int idx = indexPattern.indexOf("{");
		int end = -1;
		if(idx > 0){
			end = indexPattern.indexOf("}");
			IndexPattern _indexPattern = new IndexPattern();
			_indexPattern.setIndexPrefix(indexPattern.substring(0,idx));
			_indexPattern.setDateFormat(indexPattern.substring(idx + 1,end));
			if(end < indexPattern.length()){
				_indexPattern.setIndexEnd(indexPattern.substring(end+1));
			}
			return _indexPattern;
		}
		return null;


	}
}
