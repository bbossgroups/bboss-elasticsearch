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
import org.frameworkset.util.annotations.DateFormateMeta;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ImportBuilder {
	private ImportBuilder(){

	}
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
		fieldMeta.setDateFormateMeta(dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat));
		return fieldMeta;
	}
	private FieldMeta buildFieldMeta(String dbColumnName,String esFieldName ,String dateFormat,String locale,String timeZone){
		FieldMeta fieldMeta = new FieldMeta();
		fieldMeta.setDbColumnName(dbColumnName);
		fieldMeta.setEsFieldName(esFieldName);
		fieldMeta.setDateFormateMeta(dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone));
		return fieldMeta;
	}
	public ImportBuilder addFieldMapping(String dbColumnName,String esFieldName){
		this.fieldMetaMap.put(dbColumnName.toUpperCase(),buildFieldMeta(  dbColumnName,  esFieldName,null ));
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

	public ESJDBC builder(){
		ESJDBC esjdbcResultSet = new ESJDBC();
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
		return esjdbcResultSet;
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
		this.dbName = dbName;
		return this;
	}

	public ImportBuilder setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public ImportBuilder setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
		return this;
	}

	public ImportBuilder setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
		return this;
	}

	public ImportBuilder setDbUser(String dbUser) {
		this.dbUser = dbUser;
		return this;
	}

	public ImportBuilder setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
		return this;
	}

	public ImportBuilder setValidateSQL(String validateSQL) {
		this.validateSQL = validateSQL;
		return this;
	}

	public ImportBuilder setUsePool(boolean usePool) {
		this.usePool = usePool;
		return this;
	}
}
