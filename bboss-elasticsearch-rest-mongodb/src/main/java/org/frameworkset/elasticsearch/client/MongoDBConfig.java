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

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 16:42
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDBConfig {
	private String statusTableDML;
	private Integer jdbcFetchSize;
	private String dbDriver;
	private String dbUrl;
	private String dbUser;

	private String dbPassword;

	private int initSize = 10;
	private int minIdleSize = 10;
	private int maxSize = 20;

	public static final String mysql_createStatusTableSQL = "CREATE TABLE $statusTableName ( ID bigint(10) NOT NULL, lasttime bigint(10) NOT NULL, lastvalue bigint(10) NOT NULL, lastvaluetype int(1) NOT NULL, PRIMARY KEY(ID)) ENGINE=InnoDB";
	public static final String oracle_createStatusTableSQL = "CREATE TABLE $statusTableName ( ID NUMBER(10) NOT NULL, lasttime NUMBER(10) NOT NULL, lastvalue NUMBER(10) NOT NULL, lastvaluetype NUMBER(1) NOT NULL,constraint $statusTableName_PK primary key(ID))";

	/**是否启用sql日志，true启用，false 不启用，*/
	private boolean showSql;
	private boolean usePool = false;
	/**
	 * dbtype专用于设置不支持的数据库类型名称和数据库适配器，方便用户扩展不支持的数据库的数据导入
	 * 可选字段，设置了dbAdaptor可以不设置dbtype，默认为数据库driver类路径
	 */
	private String dbtype ;
	/**
	 * dbAdaptor专用于设置不支持的数据库类型名称和数据库适配器，方便用户扩展不支持的数据库的数据导入
	 * dbAdaptor必须继承自com.frameworkset.orm.adapter.DB或者其继承DB的类
	 */
	private String dbAdaptor;
	/**
	 * 事务管理机制只有在一次性全量单线程导入的情况下才有用
	 */
	private boolean enableDBTransaction = false;
	public String getDbDriver() {
		return dbDriver;
	}

	public String getCreateStatusTableSQL(String dbtype){
		if(dbtype.equals("mysql")){
			return mysql_createStatusTableSQL;
		}
		else if(dbtype.equals("oracle")){
			return oracle_createStatusTableSQL;
		}
		throw new ESDataImportException("getCreateStatusTableSQL failed: unsupport dbtype "+ dbtype);
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

	private String validateSQL;


	public Integer getJdbcFetchSize() {
		return jdbcFetchSize;
	}

	public void setJdbcFetchSize(Integer jdbcFetchSize) {
		this.jdbcFetchSize = jdbcFetchSize;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	private String dbName;

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public boolean isUsePool() {
		return usePool;
	}

	public void setUsePool(boolean usePool) {
		this.usePool = usePool;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMinIdleSize() {
		return minIdleSize;
	}

	public void setMinIdleSize(int minIdleSize) {
		this.minIdleSize = minIdleSize;
	}

	public int getInitSize() {
		return initSize;
	}

	public void setInitSize(int initSize) {
		this.initSize = initSize;
	}

	public String getStatusTableDML() {
		return statusTableDML;
	}

	public void setStatusTableDML(String statusTableDML) {
		this.statusTableDML = statusTableDML;
	}

	public String getDbtype() {
		return dbtype;
	}

	public void setDbtype(String dbtype) {
		this.dbtype = dbtype;
	}

	public String getDbAdaptor() {
		return dbAdaptor;
	}

	public void setDbAdaptor(String dbAdaptor) {
		this.dbAdaptor = dbAdaptor;
	}

	public boolean isEnableDBTransaction() {
		return enableDBTransaction;
	}

	public void setEnableDBTransaction(boolean enableDBTransaction) {
		this.enableDBTransaction = enableDBTransaction;
	}
}
