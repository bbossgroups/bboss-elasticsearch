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

	private String name;
	private String serverAddresses;
	private String option;
	private String writeConcern;
	private String readPreference;
	private Boolean autoConnectRetry = true;

	private int connectionsPerHost = 50;

	private int maxWaitTime = 120000;
	private int socketTimeout = 0;
	private int connectTimeout = 15000;

	public static final String mysql_createStatusTableSQL = "CREATE TABLE $statusTableName ( ID bigint(10) NOT NULL, lasttime bigint(10) NOT NULL, lastvalue bigint(10) NOT NULL, lastvaluetype int(1) NOT NULL, PRIMARY KEY(ID)) ENGINE=InnoDB";
	public static final String oracle_createStatusTableSQL = "CREATE TABLE $statusTableName ( ID NUMBER(10) NOT NULL, lasttime NUMBER(10) NOT NULL, lastvalue NUMBER(10) NOT NULL, lastvaluetype NUMBER(1) NOT NULL,constraint $statusTableName_PK primary key(ID))";

	/**是否启用sql日志，true启用，false 不启用，*/
	private int threadsAllowedToBlockForConnectionMultiplier;
	private Boolean socketKeepAlive = false;

	public String getCreateStatusTableSQL(String dbtype){
		if(dbtype.equals("mysql")){
			return mysql_createStatusTableSQL;
		}
		else if(dbtype.equals("oracle")){
			return oracle_createStatusTableSQL;
		}
		throw new ESDataImportException("getCreateStatusTableSQL failed: unsupport dbtype "+ dbtype);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerAddresses() {
		return serverAddresses;
	}

	public void setServerAddresses(String serverAddresses) {
		this.serverAddresses = serverAddresses;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getWriteConcern() {
		return writeConcern;
	}

	public void setWriteConcern(String writeConcern) {
		this.writeConcern = writeConcern;
	}

	public String getReadPreference() {
		return readPreference;
	}

	public void setReadPreference(String readPreference) {
		this.readPreference = readPreference;
	}

	public Boolean getAutoConnectRetry() {
		return autoConnectRetry;
	}

	public void setAutoConnectRetry(Boolean autoConnectRetry) {
		this.autoConnectRetry = autoConnectRetry;
	}

	public int getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public void setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public int getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	public void setThreadsAllowedToBlockForConnectionMultiplier(int threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}

	public Boolean getSocketKeepAlive() {
		return socketKeepAlive;
	}

	public void setSocketKeepAlive(Boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
	}
}
