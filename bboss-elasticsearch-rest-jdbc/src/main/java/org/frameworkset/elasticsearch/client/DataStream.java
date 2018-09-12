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
import com.frameworkset.common.poolman.StatementInfo;
import com.frameworkset.common.poolman.handle.ResultSetHandler;
import com.frameworkset.common.poolman.util.SQLUtil;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.boot.ElasticSearchBoot;
import org.frameworkset.elasticsearch.client.schedule.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class DataStream {
	private ESJDBC esjdbc;
	private ScheduleService scheduleService;
	private static Logger logger = LoggerFactory.getLogger(DataStream.class);

	public void db2es() throws ESDataImportException{
		if(esjdbc == null){
			throw new ESDataImportException("ESJDBC is null.");
		}
		try {
			initES();
			initDS();
			this.initSchedule();
			importData();
		}
		catch (Exception e) {
			throw new ESDataImportException(e);
		}
		finally{

		}
	}
	public void setEsjdbc(ESJDBC esjdbc){
		this.esjdbc = esjdbc;
	}
	private void initES(){
		if(SimpleStringUtil.isNotEmpty(esjdbc.getApplicationPropertiesFile() ))
			ElasticSearchBoot.boot(esjdbc.getApplicationPropertiesFile());
	}
	private void initSchedule(){
		if(this.esjdbc.getScheduleConfig() != null) {
			this.scheduleService = new ScheduleService();
			this.scheduleService.init(this.esjdbc);
		}
	}
	private void initDS(){
		if(SimpleStringUtil.isNotEmpty(esjdbc.getDbDriver()) && SimpleStringUtil.isNotEmpty(esjdbc.getDbUrl())) {			 
			SQLUtil.startPool(esjdbc.getDbName(),//数据源名称
					esjdbc.getDbDriver(),//oracle驱动
					esjdbc.getDbUrl(),//mysql链接串
					esjdbc.getDbUser(), esjdbc.getDbPassword(),//数据库账号和口令
					null,//"false",
					null,// "READ_UNCOMMITTED",
					esjdbc.getValidateSQL(),//数据库连接校验sql
					esjdbc.getDbName()+"_jndi",
					10,
					10,
					20,
					esjdbc.isUsePool(),
					false,
					null, esjdbc.isShowSql(), false,esjdbc.getJdbcFetchSize() == null?0:esjdbc.getJdbcFetchSize()
			);			 
		}
	}


	private void firstImportData() throws Exception {
		SQLExecutor.queryWithDBNameByNullRowHandler(new ResultSetHandler() {
			@Override
			public void handleResult(ResultSet resultSet, StatementInfo statementInfo) throws Exception {
				esjdbc.setResultSet(resultSet);
				esjdbc.setMetaData(statementInfo.getMeta());
				JDBCRestClientUtil jdbcRestClientUtil = new JDBCRestClientUtil();
				jdbcRestClientUtil.addDocuments(esjdbc.getIndex(), esjdbc.getIndexType(), esjdbc, esjdbc.getRefreshOption(), esjdbc.getBatchSize());
			}
		}, esjdbc.getDbName(), esjdbc.getSql());
	}



	private void importData() throws Exception {
		if(this.scheduleService == null) {//一次性执行数据导入操作
			firstImportData();
		}
		else{//定时增量导入数据操作

			scheduleService.timeSchedule();
		}
	}


}
