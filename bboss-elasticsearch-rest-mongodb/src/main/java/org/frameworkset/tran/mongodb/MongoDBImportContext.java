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

import com.mongodb.DBObject;
import com.mongodb.client.model.DBCollectionFindOptions;
import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.tran.DataTranPlugin;
import org.frameworkset.nosql.mongodb.ClientMongoCredential;
import org.frameworkset.tran.db.DBImportContext;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class MongoDBImportContext extends DBImportContext implements MongoDBContext{
	protected MongoDBImportConfig mongoDBImportConfig;


	protected abstract DataTranPlugin buildDataTranPlugin();
//	{
//		return new MongoDB2ESInputPlugin(this);
//	}
	protected void init(BaseImportConfig baseImportConfig){
		super.init(baseImportConfig);
		mongoDBImportConfig = (MongoDBImportConfig)baseImportConfig;
	}
	public MongoDBImportContext(){
		this(new MongoDBImportConfig());

	}
	public MongoDBImportContext(MongoDBImportConfig baseImportConfig){
		super(baseImportConfig);

	}

	public List<ClientMongoCredential> getCredentials() {
		return mongoDBImportConfig.getCredentials();
	}
	public String getName() {
		return mongoDBImportConfig.getName();
	}
	public DBObject getFetchFields(){
		return mongoDBImportConfig.getFetchFields();
	}
	@Override
	public String getDB() {
		return mongoDBImportConfig.getDB();
	}

	@Override
	public String getDBCollection() {
		return mongoDBImportConfig.getDBCollection();
	}

	@Override
	public DBObject getQuery() {
		return mongoDBImportConfig.getQuery();
	}

	@Override
	public DBCollectionFindOptions getDBCollectionFindOptions() {
		return mongoDBImportConfig.getDBCollectionFindOptions();
	}


	public String getServerAddresses() {
		return mongoDBImportConfig.getServerAddresses();
	}


	public String getOption() {
		return mongoDBImportConfig.getOption();
	}


	public String getWriteConcern() {
		return mongoDBImportConfig.getWriteConcern();
	}



	public String getReadPreference() {
		return mongoDBImportConfig.getReadPreference();
	}



	public Boolean getAutoConnectRetry() {
		return mongoDBImportConfig.getAutoConnectRetry();
	}



	public int getConnectionsPerHost() {
		return mongoDBImportConfig.getConnectionsPerHost();
	}


	public int getMaxWaitTime() {
		return mongoDBImportConfig.getMaxWaitTime();
	}



	public int getSocketTimeout() {
		return mongoDBImportConfig.getSocketTimeout();
	}



	public int getConnectTimeout() {
		return mongoDBImportConfig.getConnectTimeout();
	}



	public int getThreadsAllowedToBlockForConnectionMultiplier() {
		return mongoDBImportConfig.getThreadsAllowedToBlockForConnectionMultiplier();
	}



	public Boolean getSocketKeepAlive() {
		return mongoDBImportConfig.getSocketKeepAlive();
	}



	public String getMode() {
		return mongoDBImportConfig.getMode();
	}
}
