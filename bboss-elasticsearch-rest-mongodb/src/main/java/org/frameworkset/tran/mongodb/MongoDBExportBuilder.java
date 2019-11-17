package org.frameworkset.tran.mongodb;
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

import com.frameworkset.util.SimpleStringUtil;
import com.mongodb.DBObject;
import com.mongodb.client.model.DBCollectionFindOptions;
import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.elasticsearch.client.ExportResultHandler;
import org.frameworkset.elasticsearch.client.WrapedExportResultHandler;
import org.frameworkset.nosql.mongodb.ClientMongoCredential;
import org.frameworkset.tran.db.DBExportBuilder;
import org.frameworkset.tran.es.ESExportResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class MongoDBExportBuilder extends DBExportBuilder {

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


	/**是否启用sql日志，true启用，false 不启用，*/
	private int threadsAllowedToBlockForConnectionMultiplier = 5;
	private Boolean socketKeepAlive = false;

	private String mode;

	private DBCollectionFindOptions dbCollectionFindOptions;
	private DBObject query;
	private DBObject fetchFields;
	private String dbCollection;
	private String db;
	private List<ClientMongoCredential> credentials;
	public String getName() {
		return name;
	}

	public MongoDBExportBuilder buildClientMongoCredential(String database, String userName,
														   String password, String mechanism){
		if(credentials == null){
			credentials = new ArrayList<>();
		}
		ClientMongoCredential clientMongoCredential = new ClientMongoCredential();
		clientMongoCredential.setDatabase(database);
		clientMongoCredential.setMechanism(mechanism);
		clientMongoCredential.setUserName(userName);
		clientMongoCredential.setPassword(password);
		credentials.add(clientMongoCredential);
		return this;
	}

	public MongoDBExportBuilder setName(String name) {
		this.name = name;
		return this;
	}
//	public static MongoDBExportBuilder newInstance(){
//		return new MongoDBExportBuilder();
//	}
	public String getServerAddresses() {
		return serverAddresses;
	}

	public MongoDBExportBuilder setServerAddresses(String serverAddresses) {
		this.serverAddresses = serverAddresses;
		return this;
	}

	public String getOption() {
		return option;
	}

	public MongoDBExportBuilder setOption(String option) {
		this.option = option;
		return this;
	}

	public String getWriteConcern() {
		return writeConcern;
	}

	public MongoDBExportBuilder setWriteConcern(String writeConcern) {
		this.writeConcern = writeConcern;
		return this;
	}

	public String getReadPreference() {
		return readPreference;
	}

	public MongoDBExportBuilder setReadPreference(String readPreference) {
		this.readPreference = readPreference;
		return this;
	}

	public Boolean getAutoConnectRetry() {
		return autoConnectRetry;
	}

	public MongoDBExportBuilder setAutoConnectRetry(Boolean autoConnectRetry) {
		this.autoConnectRetry = autoConnectRetry;
		return this;
	}

	public int getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public MongoDBExportBuilder setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
		return this;
	}

	public int getMaxWaitTime() {
		return maxWaitTime;
	}

	public MongoDBExportBuilder setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
		return this;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public MongoDBExportBuilder setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
		return this;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public MongoDBExportBuilder setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public int getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	public MongoDBExportBuilder setThreadsAllowedToBlockForConnectionMultiplier(int threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
		return this;
	}

	public Boolean getSocketKeepAlive() {
		return socketKeepAlive;
	}

	public MongoDBExportBuilder setSocketKeepAlive(Boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
		return this;
	}

	public String getMode() {
		return mode;
	}

	public MongoDBExportBuilder setMode(String mode) {
		this.mode = mode;
		return this;
	}

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(SimpleStringUtil.object2json(this));
		return ret.toString();
	}

	@Override
	protected WrapedExportResultHandler buildExportResultHandler(ExportResultHandler exportResultHandler) {
		return new ESExportResultHandler(exportResultHandler);
	}
	protected abstract DataStream createDataStream();

	public DataStream builder(){
		super.builderConfig();
//		this.buildDBConfig();
//		this.buildStatusDBConfig();
		try {
			logger.info("Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		MongoDBImportConfig es2DBImportConfig = new MongoDBImportConfig();
		super.buildImportConfig(es2DBImportConfig);
		es2DBImportConfig.setName(this.name);
		es2DBImportConfig.setServerAddresses(serverAddresses);
		es2DBImportConfig.setOption(option);//private String option;
		es2DBImportConfig.setWriteConcern(writeConcern);//private String writeConcern;
		es2DBImportConfig.setReadPreference(readPreference);//private String readPreference;
		es2DBImportConfig.setAutoConnectRetry(autoConnectRetry);//private Boolean autoConnectRetry = true;

		es2DBImportConfig.setConnectionsPerHost(connectionsPerHost);//private int connectionsPerHost = 50;

		es2DBImportConfig.setMaxWaitTime(maxWaitTime);//private int maxWaitTime = 120000;
		es2DBImportConfig.setSocketTimeout(socketTimeout);//private int socketTimeout = 0;
		es2DBImportConfig.setConnectTimeout(connectTimeout);//private int connectTimeout = 15000;


		/**是否启用sql日志，true启用，false 不启用，*/
		es2DBImportConfig.setThreadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);//private int threadsAllowedToBlockForConnectionMultiplier;
		es2DBImportConfig.setSocketKeepAlive(socketKeepAlive);//private Boolean socketKeepAlive = false;

		es2DBImportConfig.setMode( mode);

		es2DBImportConfig.setDbCollectionFindOptions( this.dbCollectionFindOptions);
		es2DBImportConfig.setQuery( this.query);
		es2DBImportConfig.setDbCollection( this.dbCollection);
		es2DBImportConfig.setDb( this.db);
		es2DBImportConfig.setCredentials(this.credentials);
//		MongoDB2ESDataStreamImpl dataStream = new MongoDB2ESDataStreamImpl();
//		dataStream.setMongoDB2ESImportConfig(es2DBImportConfig);
		super.buildDBImportConfig(es2DBImportConfig);
		DataStream dataStream = this.createDataStream();
		dataStream.setImportConfig(es2DBImportConfig);
		return dataStream;
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

	public MongoDBExportBuilder setDbCollectionFindOptions(DBCollectionFindOptions dbCollectionFindOptions) {
		this.dbCollectionFindOptions = dbCollectionFindOptions;
		return this;
	}

	public MongoDBExportBuilder setQuery(DBObject dbObject) {
		this.query = dbObject;
		return this;
	}

	public MongoDBExportBuilder setDbCollection(String dbCollection) {
		this.dbCollection = dbCollection;
		return this;
	}

	public MongoDBExportBuilder setDb(String db) {
		this.db = db;
		return this;
	}


	public DBObject getFetchFields() {
		return fetchFields;
	}

	public MongoDBExportBuilder setFetchFields(DBObject fetchFields) {
		this.fetchFields = fetchFields;
		return this;
		
	}
}
