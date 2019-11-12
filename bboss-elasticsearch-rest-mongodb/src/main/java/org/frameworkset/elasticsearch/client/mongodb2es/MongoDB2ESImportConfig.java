package org.frameworkset.elasticsearch.client.mongodb2es;
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

import com.mongodb.DBObject;
import com.mongodb.client.model.DBCollectionFindOptions;
import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.nosql.mongodb.ClientMongoCredential;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/9/20 11:46
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2ESImportConfig extends BaseImportConfig {
	private String name;
	private String serverAddresses;
	private String option;
	private String writeConcern;
	private String readPreference;
	private Boolean autoConnectRetry = true;
	private DBObject fetchFields;


	private List<ClientMongoCredential> credentials;
	private int connectionsPerHost = 50;

	private int maxWaitTime = 120000;
	private int socketTimeout = 0;
	private int connectTimeout = 15000;


	/**是否启用sql日志，true启用，false 不启用，*/
	private int threadsAllowedToBlockForConnectionMultiplier = 5;
	private Boolean socketKeepAlive = false;

	private String mode;
	private DBCollectionFindOptions dbCollectionFindOptions;
	private DBObject query;
	private String dbCollection;
	private String db;

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

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public DBCollectionFindOptions getDBCollectionFindOptions() {
		return this.dbCollectionFindOptions;
	}

	public DBObject getQuery() {
		return query;
	}

	public String getDBCollection() {
		return dbCollection;
	}

	public String getDB() {
		return db;
	}

	public void setDbCollectionFindOptions(DBCollectionFindOptions dbCollectionFindOptions) {
		this.dbCollectionFindOptions = dbCollectionFindOptions;
	}

	public void setQuery(DBObject query) {
		this.query = query;
	}

	public void setDbCollection(String dbCollection) {
		this.dbCollection = dbCollection;
	}

	public void setDb(String db) {
		this.db = db;
	}
	public List<ClientMongoCredential> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<ClientMongoCredential> credentials) {
		this.credentials = credentials;
	}

	public DBObject getFetchFields() {
		return fetchFields;
	}

	public void setFetchFields(DBObject fetchFields) {
		this.fetchFields = fetchFields;
	}
}
