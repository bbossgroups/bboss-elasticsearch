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

import com.frameworkset.common.poolman.ConfigSQLExecutor;
import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.elasticsearch.client.schedule.ImportIncreamentConfig;
import org.frameworkset.elasticsearch.client.schedule.ScheduleConfig;
import org.frameworkset.elasticsearch.client.schedule.ScheduleService;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.elasticsearch.util.ESJDBCResultSet;
import org.frameworkset.persitent.util.JDBCResultSet;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ESJDBC extends JDBCResultSet implements ESJDBCResultSet {
	private static Logger logger = LoggerFactory.getLogger(ESJDBC.class);
	private ScheduleService scheduleService;
	private String indexType;
	private ErrorWrapper errorWrapper;
	private volatile boolean forceStop = false;
	public static EsIdGenerator DEFAULT_EsIdGenerator = new DefaultEsIdGenerator();
	private EsIdGenerator esIdGenerator = DEFAULT_EsIdGenerator;
	/**
	 * 打印任务日志
	 */
	private boolean printTaskLog = false;
	public void setForceStop(){
		this.forceStop = true;
	}
	/**
	 * 定时任务拦截器
	 */
	private List<CallInterceptor> callInterceptors;
	public ErrorWrapper getErrorWrapper() {
		return errorWrapper;
	}

	public void setErrorWrapper(ErrorWrapper errorWrapper) {
		this.errorWrapper = errorWrapper;
	}
	
	/**
	 * 判断执行条件是否成立，成立返回true，否则返回false
	 * @return
	 */
	public boolean assertCondition(){
		if(forceStop)
			return false;
		if(errorWrapper != null)
			return errorWrapper.assertCondition();
		return true;
	}

	/**
	 * 判断执行条件是否成立，成立返回true，否则返回false
	 * @return
	 */
	public boolean assertCondition(Exception e){
		if(errorWrapper != null)
			return errorWrapper.assertCondition(e);
		return true;
	}

	public String getApplicationPropertiesFile() {
		return applicationPropertiesFile;
	}

	public void setApplicationPropertiesFile(String applicationPropertiesFile) {
		this.applicationPropertiesFile = applicationPropertiesFile;
	}
	private ImportBuilder importBuilder;
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
	private boolean usePool = false;
	private String esIdField;
	private String esParentIdField;
	private String esParentIdValue;
	private String routingField;
	private String routingValue;
	private Boolean esDocAsUpsert;
	private Integer esRetryOnConflict;
	private Boolean esReturnSource;
	private String esVersionField;
	private Object esVersionValue;
	private String esVersionType;
	private Boolean useJavaName;

	public Boolean getUseLowcase() {
		return useLowcase;
	}

	public void setUseLowcase(Boolean useLowcase) {
		this.useLowcase = useLowcase;
	}

	private Boolean useLowcase;
	private String dateFormat;
	private String locale;
	private String timeZone;
	private DateFormat format;
	/**
	 * 以字段的小写名称为key
	 */
	private Map<String,FieldMeta> fieldMetaMap;
	private List<FieldMeta> fieldValues;
	private DataRefactor dataRefactor;
	private String sql;
	private String sqlFilepath;
	private String sqlName;
	private Integer jdbcFetchSize;
	private String dbName;
	private ConfigSQLExecutor executor;

	private String refreshOption;
	private int batchSize = 1000;
	private Integer scheduleBatchSize ;
	private String index;
	private IndexPattern indexPattern;
	private String dbDriver;
	private String dbUrl;
	private String dbUser;

	private String dbPassword;
	private String validateSQL;
	private AtomicInteger rejectCounts = new AtomicInteger();
	private boolean asyn;
	/**
	 * 并行执行过程中出现异常终端后续作业处理，已经创建的作业会执行完毕
	 */
	private boolean continueOnError = true;

	/**
	 * 是否不需要返回响应，不需要的情况下，可以设置为true，默认为true
	 * 提升性能，如果debugResponse设置为true，那么强制返回并打印响应到日志文件中
	 */
	private boolean discardBulkResponse = true;
	/**是否调试bulk响应日志，true启用，false 不启用，*/
	private boolean debugResponse;

	/**是否启用sql日志，true启用，false 不启用，*/
	private boolean showSql;


	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}
	private ScheduleConfig scheduleConfig;
	private ImportIncreamentConfig importIncreamentConfig;

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getValidateSQL() {
		return validateSQL;
	}

	public void setValidateSQL(String validateSQL) {
		this.validateSQL = validateSQL;
	}

	public String getSql() {
		return sql;
	}


	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getRefreshOption() {
		return refreshOption;
	}

	public void setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}



	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public boolean next() throws SQLException {
		return resultSet.next();
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public Integer getEsRetryOnConflict() {
		return esRetryOnConflict;
	}

	public void setEsRetryOnConflict(Integer esRetryOnConflict) {
		this.esRetryOnConflict = esRetryOnConflict;
	}

	public Boolean getEsDocAsUpsert() {
		return esDocAsUpsert;
	}

	public void setEsDocAsUpsert(Boolean esDocAsUpsert) {
		this.esDocAsUpsert = esDocAsUpsert;
	}

	public String getRoutingValue() {
		return routingValue;
	}

	public void setRoutingValue(String routingValue) {
		this.routingValue = routingValue;
	}

	public String getRoutingField() {
		return routingField;
	}

	public void setRoutingField(String routingField) {
		this.routingField = routingField;
	}

	public String getEsParentIdField() {
		return esParentIdField;
	}

	public void setEsParentIdField(String esParentIdField) {
		this.esParentIdField = esParentIdField;
	}

	public String getEsIdField() {
		return esIdField;
	}

	public void setEsIdField(String esIdField) {
		this.esIdField = esIdField;
	}

	public Boolean getEsReturnSource() {
		return esReturnSource;
	}

	public void setEsReturnSource(Boolean esReturnSource) {
		this.esReturnSource = esReturnSource;
	}

	public String getEsVersionField() {
		return esVersionField;
	}

	public void setEsVersionField(String esVersionField) {
		this.esVersionField = esVersionField;
	}

	public String getEsVersionType() {
		return esVersionType;
	}

	public void setEsVersionType(String esVersionType) {
		this.esVersionType = esVersionType;
	}

	public Boolean getUseJavaName() {
		return useJavaName;
	}

	public void setUseJavaName(Boolean useJavaName) {
		this.useJavaName = useJavaName;
	}
	public DateFormateMeta getDateFormateMeta(){
		return DateFormateMeta.buildDateFormateMeta(this.dateFormat,this.locale,this.timeZone);
	}

	public DateFormat getFormat() {
		if(format == null)
		{
			DateFormateMeta dateFormateMeta = getDateFormateMeta();
			if(dateFormateMeta == null){
				dateFormateMeta = SerialUtil.getDateFormateMeta();
			}
			format = dateFormateMeta.toDateFormat();
		}
		return format;
	}

	public void setFormat(DateFormat format) {
		this.format = format;
	}

	public Map<String, FieldMeta> getFieldMetaMap() {
		return fieldMetaMap;
	}

	public void destroy(){
		this.format = null;
	}

	public void setFieldMetaMap(Map<String, FieldMeta> fieldMetaMap) {
		this.fieldMetaMap = fieldMetaMap;
	}

	public FieldMeta getMappingName(String colName){
		if(fieldMetaMap != null)
			return this.fieldMetaMap.get(colName);
		return null;
	}

	public boolean isUsePool() {
		return usePool;
	}

	public void setUsePool(boolean usePool) {
		this.usePool = usePool;
	}

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public ExecutorService buildThreadPool(){
//		ExecutorService executor = Executors.newFixedThreadPool(this.getThreadCount(), new ThreadFactory() {
//			@Override
//			public Thread newThread(Runnable r) {
//				return new DBESThread(r);
//			}
//		});

		ExecutorService blockedExecutor = new ThreadPoolExecutor(this.getThreadCount(), this.getThreadCount(),
				0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(this.getQueue()),
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						return new DBESThread(r);
					}
				},new BlockedTaskRejectedExecutionHandler(rejectCounts));
		return blockedExecutor;
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

	public void setAsyn(boolean asyn) {
		this.asyn = asyn;
	}

	public boolean isContinueOnError() {
		return continueOnError;
	}

	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

	public List<FieldMeta> getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(List<FieldMeta> fieldValues) {
		this.fieldValues = fieldValues;
	}

	public DataRefactor getDataRefactor() {
		return dataRefactor;
	}

	public void setDataRefactor(DataRefactor dataRefactor) {
		this.dataRefactor = dataRefactor;
	}

	public Object getValue(  int i, String colName) throws Exception
	{
		Object value = this.resultSet.getObject(i+1);
		return value;
	}

	public Object getValue( String colName) throws Exception
	{
		if(colName == null)
			return null;
		Object value = this.resultSet.getObject(colName);
		return value;
	}

	public void refactorData(Context context) throws Exception {
		if(this.dataRefactor != null){

			dataRefactor.refactor(context);

		}
	}

	public ImportBuilder getImportBuilder() {
		return importBuilder;
	}

	public void setImportBuilder(ImportBuilder importBuilder) {
		this.importBuilder = importBuilder;
	}

	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public ESJDBC addFieldValue(String fieldName,Object value){
		this.importBuilder.addFieldValue(fieldName,value);
		return this;
	}

	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param dateFormat
	 * @param value
	 * @return
	 */
	public ESJDBC addFieldValue(String fieldName,String dateFormat,Object value){
		this.importBuilder.addFieldValue(fieldName,dateFormat,value);
		return this;
	}

	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param dateFormat
	 * @param value
	 * @return
	 */
	public ESJDBC addFieldValue(String fieldName,String dateFormat,Object value,String locale,String timeZone){
		this.importBuilder.addFieldValue(fieldName,dateFormat,value,  locale,  timeZone);
		return this;
	}

	public ESJDBC addFieldMapping(String dbColumnName,String esFieldName){
		this.importBuilder.addFieldMapping(dbColumnName,  esFieldName);
		return this;
	}

	public ESJDBC addIgnoreFieldMapping(String dbColumnName){
		this.importBuilder.addIgnoreFieldMapping(dbColumnName);
		return this;
	}

	public ESJDBC addFieldMapping(String dbColumnName,String esFieldName,String dateFormat){
		this.importBuilder.addFieldMapping(dbColumnName,  esFieldName,  dateFormat);
		return this;
	}

	public ESJDBC addFieldMapping(String dbColumnName,String esFieldName,String dateFormat,String locale,String timeZone){
		this.importBuilder.addFieldMapping(dbColumnName,  esFieldName,  dateFormat,locale,  timeZone);
		return this;
	}

	@Override
	public String getEsParentIdValue() {
		return esParentIdValue;
	}

	public void setEsParentIdValue(String esParentIdValue) {
		this.esParentIdValue = esParentIdValue;
	}

	@Override
	public Object getEsVersionValue() {
		return esVersionValue;
	}

	public void setEsVersionValue(Object esVersionValue) {
		this.esVersionValue = esVersionValue;
	}

	public boolean isDiscardBulkResponse() {
		return discardBulkResponse;
	}

	public void setDiscardBulkResponse(boolean discardBulkResponse) {
		this.discardBulkResponse = discardBulkResponse;
	}

	public boolean isDebugResponse() {
		return debugResponse;
	}

	public void setDebugResponse(boolean debugResponse) {
		this.debugResponse = debugResponse;
	}

	public ScheduleConfig getScheduleConfig() {
		return scheduleConfig;
	}

	public void setScheduleConfig(ScheduleConfig scheduleConfig) {
		this.scheduleConfig = scheduleConfig;
	}

	public ImportIncreamentConfig getImportIncreamentConfig() {
		return importIncreamentConfig;
	}

	public String getLastValueStoreTableName() {
		return importIncreamentConfig != null?importIncreamentConfig.getLastValueStoreTableName():null;
	}

	public String getLastValueStorePath() {
		return importIncreamentConfig != null?importIncreamentConfig.getLastValueStorePath():null;
	}

	public String getDateLastValueColumn() {
		return importIncreamentConfig != null?importIncreamentConfig.getDateLastValueColumn():null;
	}
	public String getNumberLastValueColumn() {
		return importIncreamentConfig != null?importIncreamentConfig.getNumberLastValueColumn():null;
	}

	public Integer getLastValueType() {
		return importIncreamentConfig != null?importIncreamentConfig.getLastValueType():null;
	}
	public void setImportIncreamentConfig(ImportIncreamentConfig importIncreamentConfig) {
		this.importIncreamentConfig = importIncreamentConfig;
	}

	public boolean isFromFirst() {
		return importIncreamentConfig != null?importIncreamentConfig.isFromFirst():false;
	}

	public Long getConfigLastValue() {
		return importIncreamentConfig != null?importIncreamentConfig.getLastValue():null;
	}

	public String getSqlFilepath() {
		return sqlFilepath;
	}

	public void setSqlFilepath(String sqlFilepath) {
		this.sqlFilepath = sqlFilepath;
	}

	public Integer getJdbcFetchSize() {
		return jdbcFetchSize;
	}

	public void setJdbcFetchSize(Integer jdbcFetchSize) {
		this.jdbcFetchSize = jdbcFetchSize;
	}

	public ScheduleService getScheduleService() {
		return scheduleService;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public Integer getScheduleBatchSize() {
		return scheduleBatchSize;
	}

	public void setScheduleBatchSize(Integer scheduleBatchSize) {
		this.scheduleBatchSize = scheduleBatchSize;
	}



	public Object getLastValue() throws Exception {

		if(scheduleService != null) {

			if(scheduleService.getLastValueClumnName() == null){
				return null;
			}

//			if (this.importIncreamentConfig.getDateLastValueColumn() != null) {
//				return this.getValue(this.importIncreamentConfig.getDateLastValueColumn());
//			} else if (this.importIncreamentConfig.getNumberLastValueColumn() != null) {
//				return this.getValue(this.importIncreamentConfig.getNumberLastValueColumn());
//			}
//			else if (this.scheduleService.getSqlInfo().getLastValueVarName() != null) {
//				return this.getValue(this.scheduleService.getSqlInfo().getLastValueVarName());
//			}

			return this.getValue(scheduleService.getLastValueClumnName());

		}
		return null;
	}

	public void flushLastValue(Object lastValue) {
		if(scheduleService != null && lastValue != null)
			this.scheduleService.flushLastValue(lastValue);
	}
	public void stop(){
		if(scheduleService != null) {
			scheduleService.stop();
		}
	}

	public List<CallInterceptor> getCallInterceptors() {
		return callInterceptors;
	}

	public void setCallInterceptors(List<CallInterceptor> callInterceptors) {
		this.callInterceptors = callInterceptors;
	}

	public boolean isPrintTaskLog() {
		return printTaskLog;
	}

	public void setPrintTaskLog(boolean printTaskLog) {
		this.printTaskLog = printTaskLog;
	}

	public String getSqlName() {
		return sqlName;
	}

	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}

	public ConfigSQLExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(ConfigSQLExecutor executor) {
		this.executor = executor;
	}

	public EsIdGenerator getEsIdGenerator() {
		return esIdGenerator;
	}

	public void setEsIdGenerator(EsIdGenerator esIdGenerator) {
		if(esIdGenerator != null)
			this.esIdGenerator = esIdGenerator;
	}

	public IndexPattern getIndexPattern() {
		return indexPattern;
	}

	public void setIndexPattern(IndexPattern indexPattern) {
		this.indexPattern = indexPattern;
	}

	public String buildIndexName(){
		if(this.indexPattern == null){
			return this.index;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(this.indexPattern.getDateFormat());
		String date = dateFormat.format(new Date());
		StringBuilder builder = new StringBuilder();
		builder.append(indexPattern.getIndexPrefix()).append(date);
		if(indexPattern.getIndexEnd() != null){
			builder.append(indexPattern.getIndexEnd());
		}
		return builder.toString();
	}
}
