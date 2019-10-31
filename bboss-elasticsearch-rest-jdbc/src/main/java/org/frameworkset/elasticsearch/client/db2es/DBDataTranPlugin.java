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

import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.handle.ResultSetHandler;
import com.frameworkset.orm.transaction.TransactionManager;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.tran.DataTranPlugin;
import org.frameworkset.elasticsearch.client.tran.SQLBaseDataTranPlugin;

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
