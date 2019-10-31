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
import com.frameworkset.common.poolman.SQLExecutor;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.scroll.HandlerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 15:19
 * @author biaoping.yin
 * @version 1.0
 */
public class ESDirectExporterScrollHandler<T> extends ESExporterScrollHandler<T>  {

	private static Logger logger = LoggerFactory.getLogger(ESDirectExporterScrollHandler.class);
//	private ESTranResultSet esTranResultSet ;
	public ESDirectExporterScrollHandler(ImportContext importContext, ConfigSQLExecutor configSQLExecutor, ES2DBDataTran es2DBDataTran ) {
		super(  importContext,   configSQLExecutor,   es2DBDataTran);
	}

	public void handle(ESDatas<T> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果

//		ES2DBDataTran es2DBDataTran = new ES2DBDataTran(esTranResultSet,importContext);



		long totalSize = response.getTotalSize();
		List<T> datas = response.getDatas();
		int batchNo = importContext.getExportCount().increamentCount();
		if(totalSize == 0 || datas == null || datas.size() == 0){
			if(logger.isInfoEnabled()){
				logger.info("Igonre Execute export task {}:zero or null datas.",batchNo);
			}
			return;

		}
		final int batchSize = importContext.getStoreBatchSize();
		if(logger.isInfoEnabled()){
			logger.info("Execute task {} start.",batchNo);
		}
		if(es2DBContext.getSql() == null) {
			configSQLExecutor.executeBatch(importContext.getDbConfig().getDbName(), es2DBContext.getSqlName(),
					datas, batchSize, es2DBContext.getBatchHandler());
		}
		else{
			SQLExecutor.executeBatch(importContext.getDbConfig().getDbName(),
					es2DBContext.getSql(), datas, batchSize, es2DBContext.getBatchHandler());
		}
		if(logger.isInfoEnabled()){
			logger.info("Execute task {} complete and export data {} record.",batchNo,datas.size());
		}

	}


}
