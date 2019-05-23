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

import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.spi.assemble.PropertiesContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:31
 * @author biaoping.yin
 * @version 1.0
 */
public class BaseBuilder {
	protected DBConfig dbConfig ;
	protected DBConfig statusDbConfig ;


	/**
	 * 采用外部定时任务引擎执行定时任务控制变量：
	 * false 内部引擎，默认值
	 * true 外部引擎
	 */
	protected boolean externalTimer;
	/**
	 * 打印任务日志
	 */
	protected boolean printTaskLog = false;

	/**
	 * 定时任务拦截器
	 */
	protected List<CallInterceptor> callInterceptors;
	protected String applicationPropertiesFile;
	protected boolean freezen;
	protected boolean statusFreezen;
	protected List<DBConfig> configs;
	/**抽取数据的sql语句*/
	protected String sql;

	protected String sqlName;


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
	protected int batchSize = 1000;



	/**
	 * use parallel import:
	 *  true yes
	 *  false no
	 */
	protected boolean parallel;
	/**
	 * parallel import work thread nums,default 200
	 */
	protected int threadCount = 200;
	/**
	 * 并行队列大小，默认1000
	 */
	protected int queue = 1000;
	/**
	 * 是否同步等待批处理作业结束，true 等待 false 不等待
	 */
	protected boolean asyn;
	/**
	 * 并行执行过程中出现异常终端后续作业处理，已经创建的作业会执行完毕
	 */
	protected boolean continueOnError;


//	protected Integer jdbcFetchSize;
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
}
