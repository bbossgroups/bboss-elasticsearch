package org.frameworkset.elasticsearch.client.db2es;
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

import com.frameworkset.common.poolman.ConfigSQLExecutor;
import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.handle.ResultSetHandler;
import com.frameworkset.common.poolman.util.SQLUtil;
import com.frameworkset.orm.transaction.TransactionManager;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.*;
import org.frameworkset.elasticsearch.client.schedule.SQLInfo;
import org.frameworkset.util.tokenizer.TextGrammarParser;

import java.sql.SQLException;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 14:35
 * @author biaoping.yin
 * @version 1.0
 */
public class DBDataTranPlugin extends BaseDataTranPlugin implements DataTranPlugin {
	private SQLInfo sqlInfo;
	private ESJDBC esjdbc;
	public DBDataTranPlugin(DB2ESImportContext importContext,ESJDBC esjdbc){
		super(importContext);
		this.esjdbc = esjdbc;
	}

	public void initSQLInfo(){

		if(esjdbc.getSql() == null || esjdbc.getSql().equals("")){

			try {
				ConfigSQLExecutor executor = new ConfigSQLExecutor(esjdbc.getSqlFilepath());
				org.frameworkset.persitent.util.SQLInfo sqlInfo = executor.getSqlInfo(esjdbc.getSqlName());
				esjdbc.setSql(sqlInfo.getSql());
				esjdbc.setExecutor(executor);
			}
			catch (SQLException e){
				throw new ESDataImportException(e);
			}

		}
		esjdbc.getImportIncreamentConfig().setStatusTableId(esjdbc.getSql().hashCode());
		initSQLInfoParams();

	}
	private void initSQLInfoParams(){
		String originSQL = esjdbc.getSql();
		List<TextGrammarParser.GrammarToken> tokens =
				TextGrammarParser.parser(originSQL, "#[", "]");
		SQLInfo _sqlInfo = new SQLInfo();
		int paramSize = 0;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < tokens.size(); i ++){
			TextGrammarParser.GrammarToken token = tokens.get(i);
			if(token.texttoken()){
				builder.append(token.getText());
			}
			else {
				builder.append("?");
				if(paramSize == 0){
					_sqlInfo.setLastValueVarName(token.getText());
				}
				paramSize ++;

			}
		}
		_sqlInfo.setParamSize(paramSize);
		_sqlInfo.setSql(builder.toString());
		this.sqlInfo = _sqlInfo;


	}
	public SQLInfo getSqlInfo() {
		return sqlInfo;
	}








	private void commonImportData(ResultSetHandler resultSetHandler) throws Exception {
		if(esjdbc.getDataRefactor() == null || !esjdbc.getDbConfig().isEnableDBTransaction()){
			if (esjdbc.getExecutor() == null) {
				SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql());
			} else {
				esjdbc.getExecutor().queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName());
			}
		}
		else {

			TransactionManager transactionManager = new TransactionManager();
			try {
				transactionManager.begin(TransactionManager.RW_TRANSACTION);
				if (esjdbc.getExecutor() == null) {
					SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql());
				} else {
					esjdbc.getExecutor().queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName());
				}
				transactionManager.commit();
			} finally {
				transactionManager.releasenolog();
			}
		}
	}

	private void increamentImportData(ResultSetHandler resultSetHandler) throws Exception {
		if(esjdbc.getDataRefactor() == null || !esjdbc.getDbConfig().isEnableDBTransaction()){
			if (esjdbc.getExecutor() == null) {
				SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql(), getParamValue());
			} else {
				esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), getParamValue());

			}
		}
		else {
			TransactionManager transactionManager = new TransactionManager();
			try {
				transactionManager.begin(TransactionManager.RW_TRANSACTION);
				if (esjdbc.getExecutor() == null) {
					SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql(), getParamValue());
				} else {
					esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), getParamValue());

				}
			} finally {
				transactionManager.releasenolog();
			}
		}
	}

	public void doImportData()  throws ESDataImportException{

		ResultSetHandler resultSetHandler = new DefaultResultSetHandler((DB2ESImportContext)importContext);

		try {
			if (sqlInfo.getParamSize() == 0) {
//			if(esjdbc.getDataRefactor() == null || !esjdbc.getDbConfig().isEnableDBTransaction()){
//				if (esjdbc.getExecutor() == null) {
//					SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql());
//				} else {
//					esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), (Map) null);
//				}
//			}
//			else {
//				TransactionManager transactionManager = new TransactionManager();
//				try {
//					transactionManager.begin(TransactionManager.RW_TRANSACTION);
//					if (esjdbc.getExecutor() == null) {
//						SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql());
//					} else {
//						esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), (Map) null);
//					}
//					transactionManager.commit();
//				} finally {
//					transactionManager.releasenolog();
//				}
//			}
				commonImportData(resultSetHandler);

			} else {
				if (!isIncreamentImport()) {
					setForceStop();
				} else {
//				if(esjdbc.getDataRefactor() == null || !esjdbc.getDbConfig().isEnableDBTransaction()){
//					if (esjdbc.getExecutor() == null) {
//						SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql(), getParamValue());
//					} else {
//						esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), getParamValue());
//
//					}
//				}
//				else {
//					TransactionManager transactionManager = new TransactionManager();
//					try {
//						transactionManager.begin(TransactionManager.RW_TRANSACTION);
//						if (esjdbc.getExecutor() == null) {
//							SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql(), getParamValue());
//						} else {
//							esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), getParamValue());
//
//						}
//					} finally {
//						transactionManager.releasenolog();
//					}
//				}
					increamentImportData(resultSetHandler);

				}
			}
		}
		catch (ESDataImportException e){
			throw e;
		}
		catch (Exception e){
			throw new ESDataImportException(e);
		}
	}


	public String getLastValueVarName(){
		return this.sqlInfo != null?this.sqlInfo.getLastValueVarName():null;
	}


	@Override
	public void destroy() {
		super.destroy();
		this.esjdbc.destroy();
	}



	protected void initDS(DBConfig dbConfig){
		if(SimpleStringUtil.isNotEmpty(dbConfig.getDbDriver()) && SimpleStringUtil.isNotEmpty(dbConfig.getDbUrl())) {
			SQLUtil.startPool(dbConfig.getDbName(),//数据源名称
					dbConfig.getDbDriver(),//oracle驱动
					dbConfig.getDbUrl(),//mysql链接串
					dbConfig.getDbUser(), dbConfig.getDbPassword(),//数据库账号和口令
					null,//"false",
					null,// "READ_UNCOMMITTED",
					dbConfig.getValidateSQL(),//数据库连接校验sql
					dbConfig.getDbName()+"_jndi",
					dbConfig.getInitSize(),
					dbConfig.getMinIdleSize(),
					dbConfig.getMaxSize(),
					dbConfig.isUsePool(),
					false,
					null, dbConfig.isShowSql(), false,dbConfig.getJdbcFetchSize() == null?0:dbConfig.getJdbcFetchSize(),dbConfig.getDbtype(),dbConfig.getDbAdaptor()
			);
		}
	}
	protected void initOtherDSes(List<DBConfig> dbConfigs){
		if(dbConfigs != null && dbConfigs.size() > 0){
			for (DBConfig dbConfig:dbConfigs){
				initDS( dbConfig);
			}
		}
	}



	@Override
	public void beforeInit() {
		this.initES(esjdbc.getApplicationPropertiesFile());
		this.initDS(esjdbc.getDbConfig());
		initOtherDSes(esjdbc.getConfigs());
		this.initSQLInfo();

	}

	@Override
	public void afterInit(){
		if(sqlInfo != null
				&& sqlInfo.getParamSize() > 0
				&& !this.isIncreamentImport()){
			throw new TaskFailedException("Parameter variables cannot be set in non-incremental import SQL statements："+esjdbc.getSql());
		}
//		this.externalTimer = this.esjdbc.isExternalTimer();
	}







}
