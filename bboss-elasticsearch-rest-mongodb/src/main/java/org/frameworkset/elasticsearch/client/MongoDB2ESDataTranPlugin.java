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

import com.mongodb.*;
import com.mongodb.client.model.DBCollectionFindOptions;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.schedule.ImportIncreamentConfig;
import org.frameworkset.elasticsearch.client.tran.BaseDataTranPlugin;
import org.frameworkset.elasticsearch.client.tran.DataTranPlugin;
import org.frameworkset.nosql.mongodb.MongoDB;
import org.frameworkset.nosql.mongodb.MongoDBConfig;
import org.frameworkset.nosql.mongodb.MongoDBHelper;

import java.util.Date;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 14:35
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2ESDataTranPlugin extends BaseDataTranPlugin implements DataTranPlugin {

	private MongoDB2ESContext es2DBContext;
	protected void init(ImportContext importContext){
		super.init(importContext);
		es2DBContext = (MongoDB2ESContext)importContext;

	}


	public MongoDB2ESDataTranPlugin(ImportContext importContext){
		super(importContext);


	}

	@Override
	public void beforeInit() {
		this.initES(importContext.getApplicationPropertiesFile());
		initMongoDB();
		initOtherDSes(importContext.getConfigs());


	}

	protected void initMongoDB(){
		MongoDBConfig mongoDBConfig = new MongoDBConfig();
		mongoDBConfig.setName(es2DBContext.getName());
		mongoDBConfig.setServerAddresses(es2DBContext.getServerAddresses());
		mongoDBConfig.setOption(es2DBContext.getOption());//private String option;
		mongoDBConfig.setWriteConcern(es2DBContext.getWriteConcern());//private String writeConcern;
		mongoDBConfig.setReadPreference(es2DBContext.getReadPreference());//private String readPreference;
		mongoDBConfig.setAutoConnectRetry(es2DBContext.getAutoConnectRetry());//private Boolean autoConnectRetry = true;

		mongoDBConfig.setConnectionsPerHost(es2DBContext.getConnectionsPerHost());//private int connectionsPerHost = 50;

		mongoDBConfig.setMaxWaitTime(es2DBContext.getMaxWaitTime());//private int maxWaitTime = 120000;
		mongoDBConfig.setSocketTimeout(es2DBContext.getSocketTimeout());//private int socketTimeout = 0;
		mongoDBConfig.setConnectTimeout(es2DBContext.getConnectTimeout());//private int connectTimeout = 15000;


		/**是否启用sql日志，true启用，false 不启用，*/
		mongoDBConfig.setThreadsAllowedToBlockForConnectionMultiplier(es2DBContext.getThreadsAllowedToBlockForConnectionMultiplier());//private int threadsAllowedToBlockForConnectionMultiplier;
		mongoDBConfig.setSocketKeepAlive(es2DBContext.getSocketKeepAlive());//private Boolean socketKeepAlive = false;

		mongoDBConfig.setMode( es2DBContext.getMode());
		MongoDBHelper.init(mongoDBConfig);
	}
	@Override
	public void afterInit(){
	}


	private void commonImportData() throws Exception {

		DBObject dbObject = es2DBContext.getQuery();
		if(dbObject == null)
			dbObject = new BasicDBObject();

		exportESData(  dbObject);
		/**
		 * JDBCResultSet jdbcResultSet = new JDBCResultSet();
		 * 		jdbcResultSet.setResultSet(resultSet);
		 * 		jdbcResultSet.setMetaData(statementInfo.getMeta());
		 * 		jdbcResultSet.setDbadapter(statementInfo.getDbadapter());
		 * 		DB2ESDataTran db2ESDataTran = new DB2ESDataTran(jdbcResultSet,importContext);
		 *
		 * 		db2ESDataTran.tran(  );
		 */
	}

	private void exportESData(DBObject dbObject){
		MongoDB mogodb = MongoDBHelper.getMongoDB(es2DBContext.getName());
		DB db = mogodb.getDB(es2DBContext.getDB());
		DBCollection dbCollection = db.getCollection(es2DBContext.getDBCollection());
		DBCollectionFindOptions dbCollectionFindOptions = null;
		if(es2DBContext.getDBCollectionFindOptions() != null){
			dbCollectionFindOptions = es2DBContext.getDBCollectionFindOptions();
			dbCollectionFindOptions.batchSize(importContext.getFetchSize());
		}
		else
		{
			dbCollectionFindOptions = new DBCollectionFindOptions();
			dbCollectionFindOptions.batchSize(importContext.getFetchSize());
		}


//		dbCollectionFindOptions.

		DBCursor dbCursor = dbCollection.find(dbObject,dbCollectionFindOptions);
		MongoDB2ESResultSet mongoDB2ESResultSet = new MongoDB2ESResultSet(importContext,dbCursor);
		MongoDB2ESDataTran mongoDB2ESDataTran = new MongoDB2ESDataTran(mongoDB2ESResultSet,importContext);
		mongoDB2ESDataTran.tran();

	}
	private void increamentImportData() throws Exception {

		DBObject dbObject = es2DBContext.getQuery();
		if(dbObject == null)
			dbObject = new BasicDBObject();
		putLastParamValue((BasicDBObject)dbObject);
		exportESData(  dbObject);
	}
	public void putLastParamValue(BasicDBObject query){
		if(this.lastValueType == ImportIncreamentConfig.NUMBER_TYPE) {
			query.append(getLastValueVarName(),
					new BasicDBObject("$gt", this.currentStatus.getLastValue()));
//			params.put(getLastValueVarName(), this.currentStatus.getLastValue());
		}
		else{
			Object lv = null;
			if(this.currentStatus.getLastValue() instanceof Date) {
				lv = this.currentStatus.getLastValue();
//				params.put(getLastValueVarName(), this.currentStatus.getLastValue());
			}
			else {
				if(this.currentStatus.getLastValue() instanceof Long) {
					lv =   new Date((Long)this.currentStatus.getLastValue());
				}
				else if(this.currentStatus.getLastValue() instanceof Integer){
					lv =  new Date(((Integer) this.currentStatus.getLastValue()).longValue());
				}
				else if(this.currentStatus.getLastValue() instanceof Short){
					lv =  new Date(((Short) this.currentStatus.getLastValue()).longValue());
				}
				else{
					lv =  new Date(((Number) this.currentStatus.getLastValue()).longValue());
				}
			}
			query.append(getLastValueVarName(),
					new BasicDBObject("$gt", lv));
		}
		if(isPrintTaskLog()){
			logger.info(new StringBuilder().append("Current values: ").append(query).toString());
		}
	}

	public void doImportData()  throws ESDataImportException{


			try {
				if (!isIncreamentImport()) {

					commonImportData( );

				} else {

					increamentImportData( );

				}
			} catch (ESDataImportException e) {
				throw e;
			} catch (Exception e) {
				throw new ESDataImportException(e);
			}

	}

	@Override
	public String getLastValueVarName() {
		return importContext.getLastValueClumnName();
	}
}
