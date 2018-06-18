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

import java.sql.ResultSet;

public class DataStream {
	private ESJDBC esjdbc;
	public void db2es(ESJDBC esjdbc) throws ESDataImportException{
		this.esjdbc = esjdbc;
		try {
			initDS();
			importData();
		}
		catch (Exception e) {
			throw new ESDataImportException(e);
		}
	}
	private void initDS(){
		if(SimpleStringUtil.isNotEmpty(esjdbc.getDbDriver()) && SimpleStringUtil.isNotEmpty(esjdbc.getDbUrl())) {
			if(!esjdbc.isUsePool()) {
				SQLUtil.startNoPool(esjdbc.getDbName(),//数据源名称
						esjdbc.getDbDriver(),//oracle驱动
						esjdbc.getDbUrl(),//mysql链接串
						esjdbc.getDbUser(), esjdbc.getDbPassword(),//数据库账号和口令
						esjdbc.getValidateSQL()//数据库连接校验sql
				);
			}
			else{
				SQLUtil.startPool(esjdbc.getDbName(),//数据源名称
						esjdbc.getDbDriver(),//oracle驱动
						esjdbc.getDbUrl(),//mysql链接串
						esjdbc.getDbUser(), esjdbc.getDbPassword(),//数据库账号和口令
						esjdbc.getValidateSQL()//数据库连接校验sql
				);
			}
		}
	}

	private void importData() throws Exception {
		SQLExecutor.queryWithDBNameByNullRowHandler(new ResultSetHandler() {
			@Override
			public void handleResult(ResultSet resultSet, StatementInfo statementInfo) throws Exception {
				esjdbc.setResultSet(resultSet);
				esjdbc.setMetaData(statementInfo.getMeta());
				JDBCRestClientUtil jdbcRestClientUtil = new JDBCRestClientUtil();
				jdbcRestClientUtil.addDocuments(esjdbc.getIndex(),esjdbc.getIndexType(),esjdbc,esjdbc.getRefreshOption(),esjdbc.getBatchSize());
			}
		},esjdbc.getDbName(),esjdbc.getSql());
	}
}
