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
import org.frameworkset.nosql.mongodb.ClientMongoCredential;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/28 14:11
 * @author biaoping.yin
 * @version 1.0
 */
public interface MongoDBContext {

	public String getName() ;

	public String getDB();
	public String getDBCollection();
	public DBObject getQuery();
	public DBCollectionFindOptions getDBCollectionFindOptions();
	public String getServerAddresses() ;

	public String getOption() ;

	public String getWriteConcern();

	public String getReadPreference() ;

	public Boolean getAutoConnectRetry() ;

	public int getConnectionsPerHost() ;

	public int getMaxWaitTime() ;

	public int getSocketTimeout() ;

	public int getConnectTimeout() ;

	public int getThreadsAllowedToBlockForConnectionMultiplier();

	public Boolean getSocketKeepAlive() ;
	public DBObject getFetchFields();

	public String getMode() ;
	public List<ClientMongoCredential> getCredentials() ;
}
