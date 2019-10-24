package org.frameworkset.elasticsearch.client;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.handle.ResultSetHandler;
import com.frameworkset.orm.transaction.TransactionManager;
import org.frameworkset.elasticsearch.client.schedule.ScheduleService;
import org.frameworkset.nosql.mongodb.MongoDB;
import org.frameworkset.nosql.mongodb.MongoDBConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据库同步到Elasticsearch
 */
public class MongoDB2ESDataStreamImpl extends DataStream{
	private ESMongoDB esmongoDB;
	private ScheduleService scheduleService;
	private static Logger logger = LoggerFactory.getLogger(DataStream.class);
	private boolean inited;
	public void setExternalTimer(boolean externalTimer) {
		this.esmongoDB.setExternalTimer(externalTimer);
	}
	private Lock lock = new ReentrantLock();
	public void init(){
		if(inited )
			return;
		if(esmongoDB == null){
			throw new ESDataImportException("ESMongoDB is null.");
		}

		try {
			lock.lock();
			this.initES(esmongoDB.getApplicationPropertiesFile());
			this.initMongoDB(esmongoDB.getConfig());
//			initOtherDSes(esmongoDB.getConfigs());
			this.initSQLInfo();
			this.initSchedule();
			inited = true;
		}
		catch (Exception e) {
			inited = true;
			throw new ESDataImportException(e);
		}
		finally{


			lock.unlock();
		}
	}
	private MongoDB mongoDB;
	protected void initMongoDB(MongoDBConfig dbConfig){
		this.mongoDB = new MongoDB();
		this.mongoDB.init(dbConfig);
//		if(SimpleStringUtil.isNotEmpty(dbConfig.getDbDriver()) && SimpleStringUtil.isNotEmpty(dbConfig.getDbUrl())) {
//			SQLUtil.startPool(dbConfig.getDbName(),//数据源名称
//					dbConfig.getDbDriver(),//oracle驱动
//					dbConfig.getDbUrl(),//mysql链接串
//					dbConfig.getDbUser(), dbConfig.getDbPassword(),//数据库账号和口令
//					null,//"false",
//					null,// "READ_UNCOMMITTED",
//					dbConfig.getValidateSQL(),//数据库连接校验sql
//					dbConfig.getDbName()+"_jndi",
//					dbConfig.getInitSize(),
//					dbConfig.getMinIdleSize(),
//					dbConfig.getMaxSize(),
//					dbConfig.isUsePool(),
//					false,
//					null, dbConfig.isShowSql(), false,dbConfig.getJdbcFetchSize() == null?0:dbConfig.getJdbcFetchSize(),dbConfig.getDbtype(),dbConfig.getDbAdaptor()
//			);
//		}
	}

	@Override
	public void stop() {
		if(esmongoDB != null)
			this.esmongoDB.stop();
	}

	@Override
	public String getConfigString() {
		return this.toString();
	}

	/**
	 *
	 * @throws ESDataImportException
	 */
	public void execute() throws ESDataImportException{

		try {
			this.init();
			if(this.scheduleService == null) {//一次性执行数据导入操作
				long importStartTime = System.currentTimeMillis();
				firstImportData();
				long importEndTime = System.currentTimeMillis();
				if(esmongoDB != null && this.esmongoDB.isPrintTaskLog() && logger.isInfoEnabled())
					logger.info(new StringBuilder().append("Execute job Take ").append((importEndTime - importStartTime)).append(" ms").toString());
			}
			else{//定时增量导入数据操作
				if(!scheduleService.isExternalTimer()) {//内部定时任务引擎
					scheduleService.timeSchedule();
				}
				else{ //外部定时任务引擎执行的方法，比如quartz之类的
					scheduleService.externalTimeSchedule();
				}
			}
		}
		catch (Exception e) {
			throw new ESDataImportException(e);
		}
		finally{

		}
	}
	public void initSQLInfo(){


		esmongoDB.setStatusTableId("");
	}
	public void setESMongoDB(ESMongoDB esmongoDB){
		this.esmongoDB = esmongoDB;
	}

	public void initSchedule(){
		if(this.esmongoDB.getScheduleConfig() != null) {
			this.scheduleService = new ScheduleService();
			this.scheduleService.init(this.esmongoDB);
		}
	}



	private void firstImportData() throws Exception {
		ResultSetHandler resultSetHandler = new DefaultResultSetHandler(esmongoDB,esmongoDB.getBatchSize());
		if(esmongoDB.getDataRefactor() == null || !esmongoDB.getDbConfig().isEnableDBTransaction()){
			if (esmongoDB.getExecutor() == null) {
				SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esmongoDB.getDbConfig().getDbName(), esmongoDB.getSql());
			} else {
				esmongoDB.getExecutor().queryWithDBNameByNullRowHandler(resultSetHandler, esmongoDB.getDbConfig().getDbName(), esmongoDB.getSqlName());
			}
		}
		else {

			TransactionManager transactionManager = new TransactionManager();
			try {
				transactionManager.begin(TransactionManager.RW_TRANSACTION);
				if (esmongoDB.getExecutor() == null) {
					SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esmongoDB.getDbConfig().getDbName(), esmongoDB.getSql());
				} else {
					esmongoDB.getExecutor().queryWithDBNameByNullRowHandler(resultSetHandler, esmongoDB.getDbConfig().getDbName(), esmongoDB.getSqlName());
				}
				transactionManager.commit();
			} finally {
				transactionManager.releasenolog();
			}
		}
	}






}
