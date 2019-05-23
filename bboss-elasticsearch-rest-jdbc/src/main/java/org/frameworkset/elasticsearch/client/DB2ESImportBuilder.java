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
import com.frameworkset.orm.annotation.ESIndexWrapper;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.elasticsearch.client.schedule.ImportIncreamentConfig;
import org.frameworkset.elasticsearch.client.schedule.ScheduleConfig;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;

public class DB2ESImportBuilder extends BaseBuilder{
	private static Logger logger = LoggerFactory.getLogger(DB2ESImportBuilder.class);
	protected String sqlFilepath;
	/**
	 * 是否删除indice
	 */
	private boolean dropIndice;

	public boolean isExternalTimer() {
		return externalTimer;
	}

	public DB2ESImportBuilder setExternalTimer(boolean externalTimer) {
		this.externalTimer = externalTimer;
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setExternalTimer(externalTimer);
		return this;
	}



	public boolean isPagine() {
		return pagine;
	}

	public DB2ESImportBuilder setPagine(boolean pagine) {
		this.pagine = pagine;
		return this;
	}
	//是否采用分页抽取数据
	protected boolean pagine ;
	protected DB2ESImportBuilder(){

	}


	protected EsIdGenerator esIdGenerator = ESJDBC.DEFAULT_EsIdGenerator;


	public DB2ESImportBuilder setShowSql(boolean showSql) {
		_setShowSql(showSql);

		return this;
	}
	public Boolean getUseLowcase() {
		return useLowcase;
	}

	public DB2ESImportBuilder setUseLowcase(Boolean useLowcase) {
		this.useLowcase = useLowcase;
		return this;
	}

	private Boolean useLowcase;
	/**抽取数据的sql语句*/
	private String refreshOption;
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

	public static DB2ESImportBuilder newInstance(){
		return new DB2ESImportBuilder();
	}
	public DB2ESImportBuilder setResultSet(ResultSet resultSet){
		this.resultSet = resultSet;
		return this;
	}

	public DB2ESImportBuilder setStatementInfo(StatementInfo statementInfo){
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
	public DB2ESImportBuilder addFieldMapping(String dbColumnName, String esFieldName){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName,null ));
		return this;
	}

	public DB2ESImportBuilder addIgnoreFieldMapping(String dbColumnName){
		addIgnoreFieldMapping(fieldMetaMap, dbColumnName);
		return this;
	}

	public static void addIgnoreFieldMapping(Map<String,FieldMeta> fieldMetaMap,String dbColumnName){
		fieldMetaMap.put(dbColumnName.toLowerCase(),buildIgnoreFieldMeta(  dbColumnName));
	}

	public DB2ESImportBuilder addFieldMapping(String dbColumnName, String esFieldName, String dateFormat){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat));
		return this;
	}

	public DB2ESImportBuilder addFieldMapping(String dbColumnName, String esFieldName, String dateFormat, String locale, String timeZone){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat,  locale,  timeZone));
		return this;
	}






	public DB2ESImportBuilder setTimeZone(String timeZone) {
		this.timeZone = timeZone;
		return this;
	}

	public DB2ESImportBuilder setLocale(String locale) {
		this.locale = locale;
		return this;
	}

	public DB2ESImportBuilder setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	public DB2ESImportBuilder setUseJavaName(Boolean useJavaName) {
		this.useJavaName = useJavaName;
		return this;
	}

	public DB2ESImportBuilder setEsVersionType(String esVersionType) {
		this.esVersionType = esVersionType;
		return this;
	}

	public DB2ESImportBuilder setEsVersionField(String esVersionField) {
		this.esVersionField = esVersionField;
		return this;
	}

	public DB2ESImportBuilder setEsReturnSource(Boolean esReturnSource) {
		this.esReturnSource = esReturnSource;
		return this;
	}

	public DB2ESImportBuilder setEsRetryOnConflict(Integer esRetryOnConflict) {
		this.esRetryOnConflict = esRetryOnConflict;
		return this;
	}

	public DB2ESImportBuilder setEsDocAsUpsert(Boolean esDocAsUpsert) {
		this.esDocAsUpsert = esDocAsUpsert;
		return this;
	}

	public DB2ESImportBuilder setRoutingValue(String routingValue) {
		this.routingValue = routingValue;
		return this;
	}

	public DB2ESImportBuilder setRoutingField(String routingField) {
		this.routingField = routingField;
		return this;
	}

	public DB2ESImportBuilder setEsParentIdField(String esParentIdField) {
		this.esParentIdField = esParentIdField;
		return this;
	}

	public DB2ESImportBuilder setEsIdField(String esIdField) {
		this.esIdField = esIdField;
		return this;
	}


	private ESJDBC buildESJDBCConfig(){
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
//		DBConfig dbConfig = new DBConfig();
		esjdbcResultSet.setDbConfig(dbConfig);
		esjdbcResultSet.setStatusDbConfig(statusDbConfig);

		esjdbcResultSet.setConfigs(this.configs);
		esjdbcResultSet.setRefreshOption(this.refreshOption);
		esjdbcResultSet.setBatchSize(this.batchSize);
		ESIndexWrapper esIndexWrapper = new ESIndexWrapper(index,indexType);
		esjdbcResultSet.setEsIndexWrapper(esIndexWrapper);
//		esjdbcResultSet.setIndex(index);
//		esjdbcResultSet.setIndexPattern(this.splitIndexName(index));
//		esjdbcResultSet.setIndexType(indexType);

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
		if(this.exportResultHandler != null){
			DB2ESExportResultHandler db2ESExportResultHandler = new DB2ESExportResultHandler(this.exportResultHandler);
			esjdbcResultSet.setExportResultHandler(db2ESExportResultHandler);
		}
		esjdbcResultSet.setPagine(this.pagine);
		return esjdbcResultSet;
	}

	public DataStream builder(){
		this.buildDBConfig();
		this.buildStatusDBConfig();
		this.buildOtherDBConfigs();
		try {
			logger.info("DB2ES Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		ESJDBC esjdbcResultSet = this.buildESJDBCConfig();
		DB2ESDataStreamImpl  dataStream = new DB2ESDataStreamImpl();
		dataStream.setEsjdbc(esjdbcResultSet);
		dataStream.setConfigString(this.toString());
		dataStream.init();
		return dataStream;
	}


	public DB2ESImportBuilder setIndexType(String indexType) {
		this.indexType = indexType;
		return this;
	}

	public DB2ESImportBuilder setIndex(String index) {
		this.index = index;
		return this;
	}

	public DB2ESImportBuilder setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public DB2ESImportBuilder setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
		return this;
	}

	public DB2ESImportBuilder setDbName(String dbName) {
		_setDbName(  dbName);
		return this;
	}

	public DB2ESImportBuilder setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public DB2ESImportBuilder setDbDriver(String dbDriver) {
		_setDbDriver(  dbDriver);
		return this;
	}
	public DB2ESImportBuilder setEnableDBTransaction(boolean enableDBTransaction) {
		_setEnableDBTransaction(  enableDBTransaction);
		return this;
	}


	public DB2ESImportBuilder setDbUrl(String dbUrl) {
		_setDbUrl( dbUrl);
		return this;
	}

	public DB2ESImportBuilder setDbAdaptor(String dbAdaptor) {
		_setDbAdaptor(  dbAdaptor);
		return this;

	}

	public DB2ESImportBuilder setDbtype(String dbtype) {
		_setDbtype(  dbtype);
		return this;
	}

	public DB2ESImportBuilder setDbUser(String dbUser) {
		_setDbUser(  dbUser);
		return this;
	}

	public DB2ESImportBuilder setDbPassword(String dbPassword) {
		_setDbPassword(  dbPassword);
		return this;
	}

	public DB2ESImportBuilder setValidateSQL(String validateSQL) {
		_setValidateSQL(  validateSQL);
		return this;
	}

	public DB2ESImportBuilder setUsePool(boolean usePool) {
		_setUsePool(  usePool);
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

	public DB2ESImportBuilder setParallel(boolean parallel) {
		this.parallel = parallel;
		return this;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public DB2ESImportBuilder setThreadCount(int threadCount) {
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

	public DB2ESImportBuilder setAsyn(boolean asyn) {
		this.asyn = asyn;
		return this;
	}

	public DB2ESImportBuilder setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
		return this;
	}

	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public DB2ESImportBuilder addFieldValue(String fieldName, Object value){
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
	public DB2ESImportBuilder addFieldValue(String fieldName, String dateFormat, Object value){
		addFieldValue(  fieldValues,  fieldName,  dateFormat,  value,  locale,  timeZone);
		return this;
	}
	public DB2ESImportBuilder addFieldValue(String fieldName, String dateFormat, Object value, String locale, String timeZone){
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

	public DB2ESImportBuilder setDataRefactor(DataRefactor dataRefactor) {
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

	public DB2ESImportBuilder setEsVersionValue(Object esVersionValue) {
		this.esVersionValue = esVersionValue;
		return this;
	}

	public boolean isDiscardBulkResponse() {
		return discardBulkResponse;
	}

	public DB2ESImportBuilder setDiscardBulkResponse(boolean discardBulkResponse) {
		this.discardBulkResponse = discardBulkResponse;
		return this;
	}

	public boolean isDebugResponse() {
		return debugResponse;
	}

	public DB2ESImportBuilder setDebugResponse(boolean debugResponse) {
		this.debugResponse = debugResponse;
		return this;
	}


	public DB2ESImportBuilder setPeriod(Long period) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setPeriod(period);
		return this;
	}


	public DB2ESImportBuilder setDeyLay(Long deyLay) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setDeyLay(deyLay);
		return this;
	}



	public DB2ESImportBuilder setScheduleDate(Date scheduleDate) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setScheduleDate(scheduleDate);
		return this;
	}

	public DB2ESImportBuilder setFixedRate(Boolean fixedRate) {
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

	public DB2ESImportBuilder setDateLastValueColumn(String dateLastValueColumn) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setDateLastValueColumn(dateLastValueColumn);
		return this;
	}


	public DB2ESImportBuilder setNumberLastValueColumn(String numberLastValueColumn) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setNumberLastValueColumn(numberLastValueColumn);
		return this;
	}


	public DB2ESImportBuilder setLastValueStorePath(String lastValueStorePath) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueStorePath(lastValueStorePath);
		return this;
	}



	public DB2ESImportBuilder setLastValueStoreTableName(String lastValueStoreTableName) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueStoreTableName(lastValueStoreTableName);
		return this;
	}

	public DB2ESImportBuilder setFromFirst(boolean fromFirst) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setFromFirst(fromFirst);
		return this;
	}

	public DB2ESImportBuilder setLastValue(Long lastValue) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValue(lastValue);
		return this;
	}

	public DB2ESImportBuilder setLastValueType(int lastValueType) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueType(lastValueType);
		return this;
	}


	public DB2ESImportBuilder setSqlFilepath(String sqlFilepath) {
		this.sqlFilepath = sqlFilepath;
		return this;
	}

	public DB2ESImportBuilder setJdbcFetchSize(Integer jdbcFetchSize) {
		_setJdbcFetchSize(  jdbcFetchSize);
		return  this;
	}

	public Integer getScheduleBatchSize() {
		return scheduleBatchSize;
	}

	public DB2ESImportBuilder setScheduleBatchSize(Integer scheduleBatchSize) {
		this.scheduleBatchSize = scheduleBatchSize;
		return this;
	}
	public DB2ESImportBuilder addCallInterceptor(CallInterceptor interceptor){
		if(this.callInterceptors == null){
			this.callInterceptors = new ArrayList<CallInterceptor>();
		}
		this.callInterceptors.add(interceptor);
		return this;
	}

	public boolean isPrintTaskLog() {
		return printTaskLog;
	}

	public DB2ESImportBuilder setPrintTaskLog(boolean printTaskLog) {
		this.printTaskLog = printTaskLog;
		return this;
	}

	private String configString;
	public String toString(){
		if(configString != null)
			return configString;
		try {
			StringBuilder ret = new StringBuilder();
			ret.append(SimpleStringUtil.object2json(this));
			return configString = ret.toString();
		}
		catch (Exception e){
			configString = "";
			return configString;
		}
	}

	public String getSqlName() {
		return sqlName;
	}

	public DB2ESImportBuilder setSqlName(String sqlName) {
		this.sqlName = sqlName;
		return this;
	}

	public DB2ESImportBuilder setEsIdGenerator(EsIdGenerator esIdGenerator) {
		if(esIdGenerator != null)
			this.esIdGenerator = 	esIdGenerator;
		return this;
	}

//	private IndexPattern splitIndexName(String indexPattern){
//		int idx = indexPattern.indexOf("{");
//		int end = -1;
//		if(idx > 0){
//			end = indexPattern.indexOf("}");
//			IndexPattern _indexPattern = new IndexPattern();
//			_indexPattern.setIndexPrefix(indexPattern.substring(0,idx));
//			_indexPattern.setDateFormat(indexPattern.substring(idx + 1,end));
//			if(end < indexPattern.length()){
//				_indexPattern.setIndexEnd(indexPattern.substring(end+1));
//			}
//			return _indexPattern;
//		}
//		return null;
//
//
//	}

	public ExportResultHandler getExportResultHandler() {
		return exportResultHandler;
	}

	public DB2ESImportBuilder setExportResultHandler(ExportResultHandler exportResultHandler) {
		this.exportResultHandler = exportResultHandler;
		return this;
	}

	public boolean isDropIndice() {
		return dropIndice;
	}

	public void setDropIndice(boolean dropIndice) {
		this.dropIndice = dropIndice;
	}
}
