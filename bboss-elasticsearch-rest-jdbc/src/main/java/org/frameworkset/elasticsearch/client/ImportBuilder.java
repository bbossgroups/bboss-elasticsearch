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
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.util.annotations.DateFormateMeta;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportBuilder {
	private ImportBuilder(){

	}
	private String applicationPropertiesFile;
	private boolean freezen;
	private String sql;
	private String dbName;
	private String dbDriver;
	private String dbUrl;
	private String dbUser;
	private String dbPassword;
	private String validateSQL;
	private boolean usePool = false;
	private String refreshOption;
	private int batchSize = 1000;
	private String index;
	private String indexType;
	private String esIdField;
	private String esParentIdField;
	private String routingField;
	private String routingValue;
	private Boolean esDocAsUpsert;
	private Integer esRetryOnConflict;
	private Boolean esReturnSource;
	private String esVersionField;
	private String esVersionType;
	private Boolean useJavaName;
	private String dateFormat;
	private String locale;
	private String timeZone;
	private ResultSet resultSet;
	private StatementInfo statementInfo;
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
	private int queue = Integer.MAX_VALUE;
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

	private FieldMeta buildIgnoreFieldMeta(String dbColumnName){
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
		this.fieldMetaMap.put(dbColumnName.toUpperCase(),buildFieldMeta(  dbColumnName,  esFieldName,null ));
		return this;
	}

	public ImportBuilder addIgnoreFieldMapping(String dbColumnName){
		this.fieldMetaMap.put(dbColumnName.toUpperCase(),buildIgnoreFieldMeta(  dbColumnName));
		return this;
	}

	public ImportBuilder addFieldMapping(String dbColumnName,String esFieldName,String dateFormat){
		this.fieldMetaMap.put(dbColumnName.toUpperCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat));
		return this;
	}

	public ImportBuilder addFieldMapping(String dbColumnName,String esFieldName,String dateFormat,String locale,String timeZone){
		this.fieldMetaMap.put(dbColumnName.toUpperCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat,  locale,  timeZone));
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
		esjdbcResultSet.setEsRetryOnConflict(esRetryOnConflict);
		esjdbcResultSet.setEsReturnSource(esReturnSource);
		esjdbcResultSet.setEsVersionField(esVersionField);
		esjdbcResultSet.setEsVersionType(esVersionType);

		esjdbcResultSet.setRoutingField(this.routingField);
		esjdbcResultSet.setRoutingValue(this.routingValue);
		esjdbcResultSet.setUseJavaName(this.useJavaName);
		esjdbcResultSet.setFieldMetaMap(this.fieldMetaMap);
		esjdbcResultSet.setFieldValues(fieldValues);
		esjdbcResultSet.setDataRefactor(this.dataRefactor);
		esjdbcResultSet.setSql(this.sql);
		esjdbcResultSet.setDbName(dbName);
		esjdbcResultSet.setRefreshOption(this.refreshOption);
		esjdbcResultSet.setBatchSize(this.batchSize);
		esjdbcResultSet.setIndex(index);
		esjdbcResultSet.setIndexType(indexType);
		esjdbcResultSet.setDbDriver(this.dbDriver);
		esjdbcResultSet.setDbUrl(this.dbUrl);
		esjdbcResultSet.setDbUser(this.dbUser);
		esjdbcResultSet.setDbPassword(this.dbPassword);
		esjdbcResultSet.setValidateSQL(this.validateSQL);
		esjdbcResultSet.setApplicationPropertiesFile(this.applicationPropertiesFile);
		esjdbcResultSet.setParallel(this.parallel);
		esjdbcResultSet.setThreadCount(this.threadCount);
		esjdbcResultSet.setQueue(this.queue);
		esjdbcResultSet.setAsyn(this.asyn);
		esjdbcResultSet.setContinueOnError(this.continueOnError);
		return esjdbcResultSet;
	}
	public DataStream builder(){
		this.buildDBConfig();
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
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setEsFieldName(fieldName);
		fieldMeta.setValue(value);
		this.fieldValues.add(fieldMeta);
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
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setEsFieldName(fieldName);
		fieldMeta.setValue(value);
		fieldMeta.setDateFormateMeta(buildDateFormateMeta( dateFormat));
		this.fieldValues.add(fieldMeta);
		return this;
	}
	public ImportBuilder addFieldValue(String fieldName,String dateFormat,Object value,String locale,String timeZone){
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setEsFieldName(fieldName);
		fieldMeta.setValue(value);
		fieldMeta.setDateFormateMeta(buildDateFormateMeta( dateFormat,  locale,  timeZone));
		this.fieldValues.add(fieldMeta);
		return this;
	}

	public DateFormateMeta buildDateFormateMeta(String dateFormat){
		return dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone);
	}

	public DateFormateMeta buildDateFormateMeta(String dateFormat,String locale,String timeZone){
		return dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone);
	}

	public DataRefactor getDataRefactor() {
		return dataRefactor;
	}

	public ImportBuilder setDataRefactor(DataRefactor dataRefactor) {
		this.dataRefactor = dataRefactor;
		return this;
	}
}
