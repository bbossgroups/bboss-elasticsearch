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
public class DBConfig {

	private Integer jdbcFetchSize;
	private String dbDriver;
	private String dbUrl;
	private String dbUser;

	private String dbPassword;

	private int initSize = 10;
	private int minIdleSize = 10;
	private int maxSize = 20;

	/**是否启用sql日志，true启用，false 不启用，*/
	private boolean showSql;
	private boolean usePool = false;
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
}
