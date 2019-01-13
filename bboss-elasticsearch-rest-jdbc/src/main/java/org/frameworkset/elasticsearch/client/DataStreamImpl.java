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

import com.frameworkset.common.poolman.ConfigSQLExecutor;
import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.handle.ResultSetHandler;
import org.frameworkset.elasticsearch.client.schedule.ScheduleService;
import org.frameworkset.persitent.util.SQLInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * 数据库同步到Elasticsearch
 */
public class DataStreamImpl extends DataStream{
	private ESJDBC esjdbc;
	private ScheduleService scheduleService;
	private static Logger logger = LoggerFactory.getLogger(DataStream.class);

	public void execute() throws ESDataImportException{
		db2es();
	}

	/**
	 *
	 * @throws ESDataImportException
	 * @deprecated use execute()
	 */
	public void db2es() throws ESDataImportException{
		if(esjdbc == null){
			throw new ESDataImportException("ESJDBC is null.");
		}
		try {
			initES(esjdbc.getApplicationPropertiesFile());
			initDS(esjdbc.getDbConfig());
			initSQLInfo();
			this.initSchedule();
			importData();
		}
		catch (Exception e) {
			throw new ESDataImportException(e);
		}
		finally{

		}
	}
	private void initSQLInfo(){

		if(esjdbc.getSql() == null || esjdbc.getSql().equals("")){

			try {
				ConfigSQLExecutor executor = new ConfigSQLExecutor(esjdbc.getSqlFilepath());
				SQLInfo sqlInfo = executor.getSqlInfo(esjdbc.getSqlName());
				esjdbc.setSql(sqlInfo.getSql());
				esjdbc.setExecutor(executor);
			}
			catch (SQLException e){
				throw new ESDataImportException(e);
			}

		}
	}
	public void setEsjdbc(ESJDBC esjdbc){
		this.esjdbc = esjdbc;
	}

	private void initSchedule(){
		if(this.esjdbc.getScheduleConfig() != null) {
			this.scheduleService = new ScheduleService();
			this.scheduleService.init(this.esjdbc);
		}
	}



	private void firstImportData() throws Exception {
		ResultSetHandler resultSetHandler = new DefaultResultSetHandler(esjdbc,esjdbc.getBatchSize());
		if(esjdbc.getExecutor() == null) {
			SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql());
		}
		else
		{
			esjdbc.getExecutor().queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName());
		}
	}



	protected void importData() throws Exception {
		if(this.scheduleService == null) {//一次性执行数据导入操作
			firstImportData();
		}
		else{//定时增量导入数据操作
			scheduleService.timeSchedule();
		}
	}


}
