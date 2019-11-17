package org.frameworkset.elasticsearch.client.estodb;
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
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.scroll.HandlerInfo;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 15:19
 * @author biaoping.yin
 * @version 1.0
 */
public class ESExporterScrollHandler<T>  extends BaseESExporterScrollHandler<T>{

	protected ES2DBOutPutDataTran es2DBDataTran ;
	public ESExporterScrollHandler(ImportContext importContext,ConfigSQLExecutor configSQLExecutor,ES2DBOutPutDataTran es2DBDataTran ) {
		super(  importContext,  configSQLExecutor);
		this.es2DBDataTran = es2DBDataTran;
		this.es2DBDataTran.setBreakableScrollHandler(this);
	}

	public void handle(ESDatas<T> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果

//		ES2DBDataTran es2DBDataTran = new ES2DBDataTran(esTranResultSet,importContext);
		es2DBDataTran.appendInData(response);


		/**
		long totalSize = response.getTotalSize();
		List<T> datas = response.getDatas();
		int batchNo = importContext.getExportCount().increamentCount();
		if(totalSize == 0 || datas == null || datas.size() == 0){
			if(logger.isInfoEnabled()){
				logger.info("Igonre Execute export task {}:zero or null datas.",batchNo);
			}
			return;

		}
		final int insertBatchSize = es2DBContext.getInsertBatchSize() == null ?importContext.getStoreBatchSize():es2DBContext.getInsertBatchSize();
		if(logger.isInfoEnabled()){
			logger.info("Execute task {} start.",batchNo);
		}
		if(es2DBContext.getSql() == null) {
			configSQLExecutor.executeBatch(importContext.getDbConfig().getDbName(), es2DBContext.getSqlName(),
					datas, insertBatchSize, es2DBContext.getBatchHandler());
		}
		else{
			SQLExecutor.executeBatch(importContext.getDbConfig().getDbName(),
					es2DBContext.getSql(), datas, insertBatchSize, es2DBContext.getBatchHandler());
		}
		if(logger.isInfoEnabled()){
			logger.info("Execute task {} complete and export data {} record.",batchNo,datas.size());
		}
		 */
	}


}
