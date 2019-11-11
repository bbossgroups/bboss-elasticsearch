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

import com.mongodb.DBObject;
import com.mongodb.client.model.DBCollectionFindOptions;
import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.context.BaseImportContext;
import org.frameworkset.elasticsearch.client.tran.DataTranPlugin;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2ESImportContext extends BaseImportContext implements MongoDB2ESContext{
	private MongoDB2ESImportConfig es2DBImportConfig;


	protected  DataTranPlugin buildDataTranPlugin(){
		return new MongoDB2ESDataTranPlugin(this);
	}
	protected void init(BaseImportConfig baseImportConfig){
		es2DBImportConfig = (MongoDB2ESImportConfig)baseImportConfig;
	}
	public MongoDB2ESImportContext(){
		this(new MongoDB2ESImportConfig());

	}
	public MongoDB2ESImportContext(MongoDB2ESImportConfig baseImportConfig){
		super(baseImportConfig);

	}


	public String getName() {
		return es2DBImportConfig.getName();
	}

	@Override
	public String getDB() {
		return es2DBImportConfig.getDB();
	}

	@Override
	public String getDBCollection() {
		return es2DBImportConfig.getDBCollection();
	}

	@Override
	public DBObject getQuery() {
		return es2DBImportConfig.getQuery();
	}

	@Override
	public DBCollectionFindOptions getDBCollectionFindOptions() {
		return es2DBImportConfig.getDBCollectionFindOptions();
	}


	public String getServerAddresses() {
		return es2DBImportConfig.getServerAddresses();
	}


	public String getOption() {
		return es2DBImportConfig.getOption();
	}


	public String getWriteConcern() {
		return es2DBImportConfig.getWriteConcern();
	}



	public String getReadPreference() {
		return es2DBImportConfig.getReadPreference();
	}



	public Boolean getAutoConnectRetry() {
		return es2DBImportConfig.getAutoConnectRetry();
	}



	public int getConnectionsPerHost() {
		return es2DBImportConfig.getConnectionsPerHost();
	}


	public int getMaxWaitTime() {
		return es2DBImportConfig.getMaxWaitTime();
	}



	public int getSocketTimeout() {
		return es2DBImportConfig.getSocketTimeout();
	}



	public int getConnectTimeout() {
		return es2DBImportConfig.getConnectTimeout();
	}



	public int getThreadsAllowedToBlockForConnectionMultiplier() {
		return es2DBImportConfig.getThreadsAllowedToBlockForConnectionMultiplier();
	}



	public Boolean getSocketKeepAlive() {
		return es2DBImportConfig.getSocketKeepAlive();
	}



	public String getMode() {
		return es2DBImportConfig.getMode();
	}
}
