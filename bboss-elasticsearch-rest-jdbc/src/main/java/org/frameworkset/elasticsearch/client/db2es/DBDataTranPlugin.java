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
import com.frameworkset.orm.transaction.TransactionManager;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.schedule.SQLInfo;
import org.frameworkset.tran.DataTranPlugin;
import org.frameworkset.tran.SQLBaseDataTranPlugin;
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
public class DBDataTranPlugin extends SQLBaseDataTranPlugin implements DataTranPlugin {

	public DBDataTranPlugin(ImportContext importContext){
		super(importContext);


	}


	public void initSQLInfo(){

		if(dbContext.getSql() == null || dbContext.getSql().equals("")){

			if(dbContext.getSqlFilepath() != null && !dbContext.getSqlFilepath().equals(""))
				try {
					ConfigSQLExecutor executor = new ConfigSQLExecutor(dbContext.getSqlFilepath());
					org.frameworkset.persitent.util.SQLInfo sqlInfo = executor.getSqlInfo(importContext.getDbConfig().getDbName(),dbContext.getSqlName());
					this.executor = executor;
					dbContext.setSql(sqlInfo.getSql());
				}
				catch (SQLException e){
					throw new ESDataImportException(e);
				}

		}
		if(dbContext.getSql() != null && !dbContext.getSql().equals("")) {
			importContext.setStatusTableId(dbContext.getSql().hashCode());
			initSQLInfoParams();
		}

	}
	private void initSQLInfoParams(){
		String originSQL = dbContext.getSql();
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
	@Override
	public void beforeInit() {
		this.initES(importContext.getApplicationPropertiesFile());
		this.initDS(importContext.getDbConfig());
		initOtherDSes(importContext.getConfigs());
		this.initSQLInfo();

	}

	public String getLastValueVarName(){
		return this.sqlInfo != null?this.sqlInfo.getLastValueVarName():null;
	}




	private void commonImportData(ResultSetHandler resultSetHandler) throws Exception {
		if(importContext.getDataRefactor() == null || !importContext.getDbConfig().isEnableDBTransaction()){
			if (executor == null) {
				SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSql());
			} else {
				executor.queryWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSqlName());
			}
		}
		else {

			TransactionManager transactionManager = new TransactionManager();
			try {
				transactionManager.begin(TransactionManager.RW_TRANSACTION);
				if (executor == null) {
					SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSql());
				} else {
					executor.queryWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSqlName());
				}
				transactionManager.commit();
			} finally {
				transactionManager.releasenolog();
			}
		}
	}

	private void increamentImportData(ResultSetHandler resultSetHandler) throws Exception {
		if(importContext.getDataRefactor() == null || !importContext.getDbConfig().isEnableDBTransaction()){
			if (executor == null) {
				SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSql(), getParamValue());
			} else {
				executor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSqlName(), getParamValue());

			}
		}
		else {
			TransactionManager transactionManager = new TransactionManager();
			try {
				transactionManager.begin(TransactionManager.RW_TRANSACTION);
				if (executor == null) {
					SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSql(), getParamValue());
				} else {
					executor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), dbContext.getSqlName(), getParamValue());

				}
			} finally {
				transactionManager.releasenolog();
			}
		}
	}

	public void doImportData()  throws ESDataImportException{

		ResultSetHandler resultSetHandler = new DefaultResultSetHandler(importContext);

		try {
			if (sqlInfo.getParamSize() == 0) {
//			if(importContext.getDataRefactor() == null || !importContext.getDbConfig().isEnableDBTransaction()){
//				if (executor == null) {
//					SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSql());
//				} else {
//					executor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSqlName(), (Map) null);
//				}
//			}
//			else {
//				TransactionManager transactionManager = new TransactionManager();
//				try {
//					transactionManager.begin(TransactionManager.RW_TRANSACTION);
//					if (executor == null) {
//						SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSql());
//					} else {
//						executor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSqlName(), (Map) null);
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
//				if(importContext.getDataRefactor() == null || !importContext.getDbConfig().isEnableDBTransaction()){
//					if (executor == null) {
//						SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSql(), getParamValue());
//					} else {
//						executor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSqlName(), getParamValue());
//
//					}
//				}
//				else {
//					TransactionManager transactionManager = new TransactionManager();
//					try {
//						transactionManager.begin(TransactionManager.RW_TRANSACTION);
//						if (executor == null) {
//							SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSql(), getParamValue());
//						} else {
//							executor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, importContext.getDbConfig().getDbName(), importContext.getSqlName(), getParamValue());
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








}
