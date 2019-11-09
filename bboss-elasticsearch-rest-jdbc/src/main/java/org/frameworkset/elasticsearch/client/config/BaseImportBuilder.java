package org.frameworkset.elasticsearch.client.config;
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

import com.frameworkset.orm.annotation.ESIndexWrapper;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.*;
import org.frameworkset.elasticsearch.client.db2es.DB2ESImportConfig;
import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.elasticsearch.client.schedule.ImportIncreamentConfig;
import org.frameworkset.elasticsearch.client.schedule.ScheduleConfig;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.util.annotations.DateFormateMeta;

import java.util.*;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:31
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseImportBuilder {
	private DBConfig dbConfig ;
	private DBConfig statusDbConfig ;
	private Integer fetchSize = 5000;

	public boolean isSortLastValue() {
		return sortLastValue;
	}

	public BaseImportBuilder setSortLastValue(boolean sortLastValue) {
		this.sortLastValue = sortLastValue;
		return this;
	}

	private boolean sortLastValue = true;
	/**
	 * 是否不需要返回响应，不需要的情况下，可以设置为true，
	 * 提升性能，如果debugResponse设置为true，那么强制返回并打印响应到日志文件中
	 */
	private boolean discardBulkResponse = true;
	/**是否调试bulk响应日志，true启用，false 不启用，*/
	private boolean debugResponse;
	private ScheduleConfig scheduleConfig;
	private ImportIncreamentConfig importIncreamentConfig;
	public boolean isExternalTimer() {
		return externalTimer;
	}


	/**
	 * 采用外部定时任务引擎执行定时任务控制变量：
	 * false 内部引擎，默认值
	 * true 外部引擎
	 */
	private boolean externalTimer;
	/**
	 * 打印任务日志
	 */
	private boolean printTaskLog = false;

	/**
	 * 定时任务拦截器
	 */
	private List<CallInterceptor> callInterceptors;
	private String applicationPropertiesFile;
	private boolean freezen;
	private boolean statusFreezen;
	private List<DBConfig> configs;


	/**
	 * 是否删除indice
	 */
	private boolean dropIndice;

//	/**是否启用sql日志，true启用，false 不启用，*/
//	protected boolean showSql;
//
//	/**抽取数据的sql语句*/
//	protected String dbName;
//	/**抽取数据的sql语句*/
//	protected String dbDriver;
//	/**抽取数据的sql语句*/
//	protected String dbUrl;
//	/**抽取数据的sql语句*/
//	protected String dbUser;
//	/**抽取数据的sql语句*/
//	protected String dbPassword;
//	/**抽取数据的sql语句*/
//	protected String validateSQL;
//	/**抽取数据的sql语句*/
//	protected boolean usePool = false;

	/**批量获取数据大小*/
	private int batchSize = 1000;



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

	public Boolean getUseLowcase() {
		return useLowcase;
	}

	public BaseImportBuilder setUseLowcase(Boolean useLowcase) {
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

//	private Integer jdbcFetchSize;
	protected ExportResultHandler exportResultHandler;
	protected void buildDBConfig(){
		if(!freezen) {
			PropertiesContainer propertiesContainer = new PropertiesContainer();

			if(this.applicationPropertiesFile == null) {
				propertiesContainer.addConfigPropertiesFile("application.properties");
			}
			else{
				propertiesContainer.addConfigPropertiesFile(applicationPropertiesFile);
			}
			String dbName  = propertiesContainer.getProperty("db.name");
			if(dbName == null || dbName.equals(""))
				return;
			dbConfig = new DBConfig();
			_buildDBConfig(propertiesContainer,dbName,dbConfig, "");
		}
	}

	public ExportResultHandler getExportResultHandler() {
		return exportResultHandler;
	}

	public BaseImportBuilder setExportResultHandler(ExportResultHandler exportResultHandler) {
		this.exportResultHandler = exportResultHandler;
		return this;
	}


	public boolean isDropIndice() {
		return dropIndice;
	}

	public void setDropIndice(boolean dropIndice) {
		this.dropIndice = dropIndice;
	}
	public BaseImportBuilder setEsIdGenerator(EsIdGenerator esIdGenerator) {
		if(esIdGenerator != null)
			this.esIdGenerator = 	esIdGenerator;
		return this;
	}
	protected void buildStatusDBConfig(){
		if(!statusFreezen) {
			PropertiesContainer propertiesContainer = new PropertiesContainer();
			String prefix = "config.";
			if(this.applicationPropertiesFile == null) {
				propertiesContainer.addConfigPropertiesFile("application.properties");
			}
			else{
				propertiesContainer.addConfigPropertiesFile(applicationPropertiesFile);
			}
			String dbName  = propertiesContainer.getProperty(prefix+"db.name");
			if(dbName == null || dbName.equals(""))
				return;

			statusDbConfig = new DBConfig();
			_buildDBConfig(propertiesContainer,dbName,statusDbConfig, "config.");
		}
	}
	protected void builderConfig(){
		this.buildDBConfig();
		this.buildStatusDBConfig();
		this.buildOtherDBConfigs();

	}
	/**
	 * 在数据导入过程可能需要使用的其他数据名称，需要在配置文件中定义相关名称的db配置
	 */
	protected void buildOtherDBConfigs(){

			PropertiesContainer propertiesContainer = new PropertiesContainer();

			if(this.applicationPropertiesFile == null) {
				propertiesContainer.addConfigPropertiesFile("application.properties");
			}
			else{
				propertiesContainer.addConfigPropertiesFile(applicationPropertiesFile);
			}
			String thirdDatasources = propertiesContainer.getProperty("thirdDatasources");
			if(thirdDatasources == null || thirdDatasources.equals(""))
				return;
			String[] names = thirdDatasources.split(",");
			List<DBConfig> configs = new ArrayList<DBConfig>();
			for(int i = 0; i < names.length; i ++ ) {
				String prefix = names[i].trim();
				if(prefix.equals(""))
					continue;


				DBConfig statusDbConfig = new DBConfig();
				_buildDBConfig(propertiesContainer, prefix, statusDbConfig, prefix+".");
				configs.add(statusDbConfig);
			}
			this.configs = configs;

	}


	protected void _buildDBConfig(PropertiesContainer propertiesContainer, String dbName,DBConfig dbConfig,String prefix){



		dbConfig.setDbName(dbName);
		String dbUser  = propertiesContainer.getProperty(prefix+"db.user");
		dbConfig.setDbUser(dbUser);
		String dbPassword  = propertiesContainer.getProperty(prefix+"db.password");
		dbConfig.setDbPassword(dbPassword);
		String dbDriver  = propertiesContainer.getProperty(prefix+"db.driver");
		dbConfig.setDbDriver(dbDriver);

		boolean enableDBTransaction = propertiesContainer.getBooleanProperty(prefix+"db.enableDBTransaction",false);
		dbConfig.setEnableDBTransaction(enableDBTransaction);
		String dbUrl  = propertiesContainer.getProperty(prefix+"db.url");
		dbConfig.setDbUrl(dbUrl);
		String _usePool = propertiesContainer.getProperty(prefix+"db.usePool");
		if(_usePool != null && !_usePool.equals("")) {
			boolean usePool = Boolean.parseBoolean(_usePool);
			dbConfig.setUsePool(usePool);
		}
		String validateSQL  = propertiesContainer.getProperty(prefix+"db.validateSQL");
		dbConfig.setValidateSQL(validateSQL);

		String _showSql = propertiesContainer.getProperty(prefix+"db.showsql");
		if(_showSql != null && !_showSql.equals("")) {
			boolean showSql = Boolean.parseBoolean(_showSql);
			dbConfig.setShowSql(showSql);
		}

		String _jdbcFetchSize = propertiesContainer.getProperty(prefix+"db.jdbcFetchSize");
		if(_jdbcFetchSize != null && !_jdbcFetchSize.equals("")) {
			int jdbcFetchSize = Integer.parseInt(_jdbcFetchSize);
			dbConfig.setJdbcFetchSize(jdbcFetchSize);
		}
		String _initSize = propertiesContainer.getProperty(prefix+"db.initSize");
		if(_initSize != null && !_initSize.equals("")) {
			int initSize = Integer.parseInt(_initSize);
			dbConfig.setInitSize(initSize);
		}
		String _minIdleSize = propertiesContainer.getProperty(prefix+"db.minIdleSize");
		if(_minIdleSize != null && !_minIdleSize.equals("")) {
			int minIdleSize = Integer.parseInt(_minIdleSize);
			dbConfig.setMinIdleSize(minIdleSize);
		}
		String _maxSize = propertiesContainer.getProperty(prefix+"db.maxSize");
		if(_maxSize != null && !_maxSize.equals("")) {
			int maxSize = Integer.parseInt(_maxSize);
			dbConfig.setMaxSize(maxSize);
		}
		String statusTableDML  = propertiesContainer.getProperty(prefix+"db.statusTableDML");
		dbConfig.setStatusTableDML(statusTableDML);
//
//		/**
//		 * dbtype专用于设置不支持的数据库类型名称和数据库适配器，方便用户扩展不支持的数据库的数据导入
//		 * 可选字段，设置了dbAdaptor可以不设置dbtype，默认为数据库driver类路径
//		 */
//		private String dbtype ;
//		/**
//		 * dbAdaptor专用于设置不支持的数据库类型名称和数据库适配器，方便用户扩展不支持的数据库的数据导入
//		 * dbAdaptor必须继承自com.frameworkset.orm.adapter.DB或者其继承DB的类
//		 */
//		private String dbAdaptor;
		String dbAdaptor  = propertiesContainer.getProperty(prefix+"db.dbAdaptor");
		dbConfig.setDbAdaptor(dbAdaptor);
		String dbtype  = propertiesContainer.getProperty(prefix+"db.dbtype");
		dbConfig.setDbtype(dbtype);
	}
	public String getDbName() {
		return dbConfig.getDbName();
	}

	public String getDbDriver() {
		return dbConfig.getDbDriver();
	}

	public String getDbUrl() {
		return dbConfig.getDbUrl();
	}

	public String getDbUser() {
		return dbConfig.getDbUser();
	}

	public String getDbPassword() {
		return dbConfig.getDbPassword();
	}

	public String getValidateSQL() {
		return dbConfig.getValidateSQL();
	}

	public boolean isUsePool() {
		return dbConfig.isUsePool();
	}
	public boolean isShowSql() {
		return dbConfig.isShowSql();
	}
	protected void _setJdbcFetchSize(Integer jdbcFetchSize) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		dbConfig.setJdbcFetchSize(jdbcFetchSize);

	}

	public void _setDbPassword(String dbPassword) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		dbConfig.setDbPassword(dbPassword);

	}

	public BaseImportBuilder setDbName(String dbName) {
		_setDbName(  dbName);
		return this;
	}



	public BaseImportBuilder setDbDriver(String dbDriver) {
		_setDbDriver(  dbDriver);
		return this;
	}
	public BaseImportBuilder setEnableDBTransaction(boolean enableDBTransaction) {
		_setEnableDBTransaction(  enableDBTransaction);
		return this;
	}


	public BaseImportBuilder setDbUrl(String dbUrl) {
		_setDbUrl( dbUrl);
		return this;
	}

	public BaseImportBuilder setDbAdaptor(String dbAdaptor) {
		_setDbAdaptor(  dbAdaptor);
		return this;

	}

	public BaseImportBuilder setDbtype(String dbtype) {
		_setDbtype(  dbtype);
		return this;
	}

	public BaseImportBuilder setDbUser(String dbUser) {
		_setDbUser(  dbUser);
		return this;
	}

	public BaseImportBuilder setDbPassword(String dbPassword) {
		_setDbPassword(  dbPassword);
		return this;
	}

	public BaseImportBuilder setValidateSQL(String validateSQL) {
		_setValidateSQL(  validateSQL);
		return this;
	}

	public BaseImportBuilder setUsePool(boolean usePool) {
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

	public BaseImportBuilder setParallel(boolean parallel) {
		this.parallel = parallel;
		return this;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public BaseImportBuilder setThreadCount(int threadCount) {
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

	public BaseImportBuilder setAsyn(boolean asyn) {
		this.asyn = asyn;
		return this;
	}

	public BaseImportBuilder setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
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

	public BaseImportBuilder setEsVersionValue(Object esVersionValue) {
		this.esVersionValue = esVersionValue;
		return this;
	}

	public boolean isDiscardBulkResponse() {
		return discardBulkResponse;
	}

	public BaseImportBuilder setDiscardBulkResponse(boolean discardBulkResponse) {
		this.discardBulkResponse = discardBulkResponse;
		return this;
	}

	public boolean isDebugResponse() {
		return debugResponse;
	}

	public BaseImportBuilder setDebugResponse(boolean debugResponse) {
		this.debugResponse = debugResponse;
		return this;
	}


	public BaseImportBuilder setPeriod(Long period) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setPeriod(period);
		return this;
	}


	public BaseImportBuilder setDeyLay(Long deyLay) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setDeyLay(deyLay);
		return this;
	}



	public BaseImportBuilder setScheduleDate(Date scheduleDate) {
		if(scheduleConfig == null){
			scheduleConfig = new ScheduleConfig();
		}
		this.scheduleConfig.setScheduleDate(scheduleDate);
		return this;
	}

	public BaseImportBuilder setFixedRate(Boolean fixedRate) {
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

	public BaseImportBuilder setDateLastValueColumn(String dateLastValueColumn) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setDateLastValueColumn(dateLastValueColumn);
		return this;
	}


	public BaseImportBuilder setNumberLastValueColumn(String numberLastValueColumn) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setNumberLastValueColumn(numberLastValueColumn);
		return this;
	}


	public BaseImportBuilder setLastValueStorePath(String lastValueStorePath) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueStorePath(lastValueStorePath);
		return this;
	}



	public BaseImportBuilder setLastValueStoreTableName(String lastValueStoreTableName) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueStoreTableName(lastValueStoreTableName);
		return this;
	}

	public BaseImportBuilder setFromFirst(boolean fromFirst) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setFromFirst(fromFirst);
		return this;
	}

	public BaseImportBuilder setLastValue(Object lastValue) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValue(lastValue);
		return this;
	}

	public BaseImportBuilder setLastValueType(int lastValueType) {
		if(importIncreamentConfig == null){
			importIncreamentConfig = new ImportIncreamentConfig();
		}
		this.importIncreamentConfig.setLastValueType(lastValueType);
		return this;
	}

	public BaseImportBuilder setJdbcFetchSize(Integer jdbcFetchSize) {
		_setJdbcFetchSize(  jdbcFetchSize);
		return  this;
	}

	public Integer getScheduleBatchSize() {
		return scheduleBatchSize;
	}

	public BaseImportBuilder setScheduleBatchSize(Integer scheduleBatchSize) {
		this.scheduleBatchSize = scheduleBatchSize;
		return this;
	}
	public BaseImportBuilder addCallInterceptor(CallInterceptor interceptor){
		if(this.callInterceptors == null){
			this.callInterceptors = new ArrayList<CallInterceptor>();
		}
		this.callInterceptors.add(interceptor);
		return this;
	}

	public boolean isPrintTaskLog() {
		return printTaskLog;
	}

	public BaseImportBuilder setPrintTaskLog(boolean printTaskLog) {
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

	public void _setShowSql(boolean showSql) {
		this.freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		dbConfig.setShowSql(showSql);


	}

	public void _setDbName(String dbName) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}

		dbConfig.setDbName(dbName);

	}

	public void _setDbDriver(String dbDriver) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		this.dbConfig.setDbDriver(dbDriver);
	}
	public void _setEnableDBTransaction(boolean enableDBTransaction) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		dbConfig.setEnableDBTransaction(enableDBTransaction);
	}

	public void _setDbAdaptor(String dbAdaptor) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		this.dbConfig.setDbAdaptor(dbAdaptor);
	}

	public void _setDbtype(String dbtype) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		this.dbConfig.setDbtype(dbtype);
	}

	public void _setDbUrl(String dbUrl) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		dbConfig.setDbUrl(dbUrl);
	}

	public void _setDbUser(String dbUser) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		this.dbConfig.setDbUser(dbUser);
	}

	public void _setValidateSQL(String validateSQL) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		dbConfig.setValidateSQL(validateSQL);
	}

	public void _setUsePool(boolean usePool) {
		freezen = true;
		if(this.dbConfig == null){
			this.dbConfig = new DBConfig();
		}
		dbConfig.setUsePool(usePool);
	}


	public List<DBConfig> getConfigs() {
		return configs;
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


	public BaseImportBuilder setTimeZone(String timeZone) {
		this.timeZone = timeZone;
		return this;
	}

	public BaseImportBuilder setLocale(String locale) {
		this.locale = locale;
		return this;
	}

	public BaseImportBuilder setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}
	public String getLocale() {
		return locale;
	}

	public String getTimeZone() {
		return timeZone;
	}
	/**抽取数据的sql语句*/
	private String dateFormat;
	/**抽取数据的sql语句*/
	private String locale;
	/**抽取数据的sql语句*/
	private String timeZone;
	private EsIdGenerator esIdGenerator = DB2ESImportConfig.DEFAULT_EsIdGenerator;
	private Map<String,FieldMeta> fieldMetaMap = new HashMap<String,FieldMeta>();

	private List<FieldMeta> fieldValues = new ArrayList<FieldMeta>();
	private DataRefactor dataRefactor;
	public DateFormateMeta buildDateFormateMeta(String dateFormat){
		return dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone);
	}

	public static DateFormateMeta buildDateFormateMeta(String dateFormat,String locale,String timeZone){
		return dateFormat == null?null:DateFormateMeta.buildDateFormateMeta(dateFormat,locale,timeZone);
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
	public BaseImportBuilder addFieldMapping(String dbColumnName, String esFieldName){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName,null ));
		return this;
	}

	public BaseImportBuilder addIgnoreFieldMapping(String dbColumnName){
		addIgnoreFieldMapping(fieldMetaMap, dbColumnName);
		return this;
	}

	public static void addIgnoreFieldMapping(Map<String,FieldMeta> fieldMetaMap, String dbColumnName){
		fieldMetaMap.put(dbColumnName.toLowerCase(),buildIgnoreFieldMeta(  dbColumnName));
	}

	public BaseImportBuilder addFieldMapping(String dbColumnName, String esFieldName, String dateFormat){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat));
		return this;
	}

	public BaseImportBuilder addFieldMapping(String dbColumnName, String esFieldName, String dateFormat, String locale, String timeZone){
		this.fieldMetaMap.put(dbColumnName.toLowerCase(),buildFieldMeta(  dbColumnName,  esFieldName ,dateFormat,  locale,  timeZone));
		return this;
	}


	/**
	 * 补充额外的字段和值
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public BaseImportBuilder addFieldValue(String fieldName, Object value){
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
	public BaseImportBuilder addFieldValue(String fieldName, String dateFormat, Object value){
		addFieldValue(  fieldValues,  fieldName,  dateFormat,  value,  locale,  timeZone);
		return this;
	}
	public BaseImportBuilder addFieldValue(String fieldName, String dateFormat, Object value, String locale, String timeZone){
		addFieldValue(  fieldValues,  fieldName,  dateFormat,  value,  locale,  timeZone);
		return this;
	}



	public DataRefactor getDataRefactor() {
		return dataRefactor;
	}

	public BaseImportBuilder setDataRefactor(DataRefactor dataRefactor) {
		this.dataRefactor = dataRefactor;
		return this;
	}

	public BaseImportBuilder setExternalTimer(boolean externalTimer) {
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

	public BaseImportBuilder setPagine(boolean pagine) {
		this.pagine = pagine;
		return this;
	}
	//是否采用分页抽取数据
	private boolean pagine ;


	protected void buildImportConfig(BaseImportConfig baseImportConfig){

//		esjdbcResultSet.setMetaData(statementInfo.getMeta());
//		esjdbcResultSet.setResultSet(resultSet);
		baseImportConfig.setDateFormat(dateFormat);
		baseImportConfig.setLocale(locale);
		baseImportConfig.setTimeZone(this.timeZone);
		baseImportConfig.setEsDocAsUpsert(this.esDocAsUpsert);
		baseImportConfig.setEsIdField(this.esIdField);
		baseImportConfig.setEsParentIdField(esParentIdField);
		baseImportConfig.setEsParentIdValue(esParentIdValue);
		baseImportConfig.setEsRetryOnConflict(esRetryOnConflict);
		baseImportConfig.setEsReturnSource(esReturnSource);
		baseImportConfig.setEsVersionField(esVersionField);
		baseImportConfig.setEsVersionValue(esVersionValue);
		baseImportConfig.setEsVersionType(esVersionType);
		baseImportConfig.setFetchSize(this.fetchSize);
		baseImportConfig.setRoutingField(this.routingField);
		baseImportConfig.setRoutingValue(this.routingValue);
		baseImportConfig.setUseJavaName(this.useJavaName);
		baseImportConfig.setFieldMetaMap(this.fieldMetaMap);
		baseImportConfig.setFieldValues(fieldValues);
		baseImportConfig.setDataRefactor(this.dataRefactor);
		baseImportConfig.setSortLastValue(this.sortLastValue);
//		DBConfig dbConfig = new DBConfig();
		baseImportConfig.setDbConfig(dbConfig);
		baseImportConfig.setStatusDbConfig(statusDbConfig);

		baseImportConfig.setConfigs(this.configs);
		baseImportConfig.setRefreshOption(this.refreshOption);
		baseImportConfig.setBatchSize(this.batchSize);
		if(index != null) {
			ESIndexWrapper esIndexWrapper = new ESIndexWrapper(index, indexType);
			baseImportConfig.setEsIndexWrapper(esIndexWrapper);
		}
//		importConfig.setIndex(index);
//		importConfig.setIndexPattern(this.splitIndexName(index));
//		importConfig.setIndexType(indexType);

		baseImportConfig.setApplicationPropertiesFile(this.applicationPropertiesFile);
		baseImportConfig.setParallel(this.parallel);
		baseImportConfig.setThreadCount(this.threadCount);
		baseImportConfig.setQueue(this.queue);
		baseImportConfig.setAsyn(this.asyn);
		baseImportConfig.setContinueOnError(this.continueOnError);
		/**
		 * 是否不需要返回响应，不需要的情况下，可以设置为true，
		 * 提升性能，如果debugResponse设置为true，那么强制返回并打印响应到日志文件中
		 */
		baseImportConfig.setDiscardBulkResponse(this.discardBulkResponse);
		/**是否调试bulk响应日志，true启用，false 不启用，*/
		baseImportConfig.setDebugResponse(this.debugResponse);
		baseImportConfig.setScheduleConfig(this.scheduleConfig);//定时任务配置
		baseImportConfig.setImportIncreamentConfig(this.importIncreamentConfig);//增量数据配置

		if(this.scheduleBatchSize != null)
			baseImportConfig.setScheduleBatchSize(this.scheduleBatchSize);
		else
			baseImportConfig.setScheduleBatchSize(this.batchSize);
		baseImportConfig.setCallInterceptors(this.callInterceptors);
		baseImportConfig.setUseLowcase(this.useLowcase);
		baseImportConfig.setPrintTaskLog(this.printTaskLog);
		baseImportConfig.setEsIdGenerator(esIdGenerator);
		if(this.exportResultHandler != null){

			baseImportConfig.setExportResultHandler(buildExportResultHandler( exportResultHandler));
		}
		baseImportConfig.setPagine(this.pagine);
		baseImportConfig.setTranDataBufferQueue(this.tranDataBufferQueue);
	}
	protected abstract WrapedExportResultHandler buildExportResultHandler(ExportResultHandler exportResultHandler);
	public BaseImportBuilder setIndexType(String indexType) {
		this.indexType = indexType;
		return this;
	}

	public BaseImportBuilder setIndex(String index) {
		this.index = index;
		return this;
	}

	public BaseImportBuilder setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public BaseImportBuilder setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
		return this;
	}


	public boolean isFreezen() {
		return freezen;
	}

	public void setFreezen(boolean freezen) {
		this.freezen = freezen;
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






	public BaseImportBuilder setUseJavaName(Boolean useJavaName) {
		this.useJavaName = useJavaName;
		return this;
	}

	public BaseImportBuilder setEsVersionType(String esVersionType) {
		this.esVersionType = esVersionType;
		return this;
	}

	public BaseImportBuilder setEsVersionField(String esVersionField) {
		this.esVersionField = esVersionField;
		return this;
	}

	public BaseImportBuilder setEsReturnSource(Boolean esReturnSource) {
		this.esReturnSource = esReturnSource;
		return this;
	}

	public BaseImportBuilder setEsRetryOnConflict(Integer esRetryOnConflict) {
		this.esRetryOnConflict = esRetryOnConflict;
		return this;
	}

	public BaseImportBuilder setEsDocAsUpsert(Boolean esDocAsUpsert) {
		this.esDocAsUpsert = esDocAsUpsert;
		return this;
	}

	public BaseImportBuilder setRoutingValue(String routingValue) {
		this.routingValue = routingValue;
		return this;
	}

	public BaseImportBuilder setRoutingField(String routingField) {
		this.routingField = routingField;
		return this;
	}

	public BaseImportBuilder setEsParentIdField(String esParentIdField) {
		this.esParentIdField = esParentIdField;
		return this;
	}

	public BaseImportBuilder setEsIdField(String esIdField) {
		this.esIdField = esIdField;
		return this;
	}


	public boolean isContinueOnError() {
		return continueOnError;
	}


	public Integer getFetchSize() {
		return fetchSize;
	}

	public BaseImportBuilder setFetchSize(Integer fetchSize) {
		this.fetchSize = fetchSize;
		return this;
	}
	public abstract DataStream builder();
	public BaseImportBuilder setTranDataBufferQueue(int tranDataBufferQueue) {
		this.tranDataBufferQueue = tranDataBufferQueue;
		return this;
	}

	/**
	 * 源数据批量预加载队列大小，需要用到的最大缓冲内存为：
	 *  tranDataBufferQueue * fetchSize * 单条记录mem大小
	 */
	private int tranDataBufferQueue = 10;
}
