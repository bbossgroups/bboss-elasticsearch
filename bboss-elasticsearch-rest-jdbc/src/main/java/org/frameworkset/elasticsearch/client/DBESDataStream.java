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

import com.frameworkset.common.poolman.util.SQLUtil;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.boot.ElasticSearchBoot;

import java.util.List;

public abstract class DBESDataStream extends DataStream{



	protected void initOtherDSes(List<DBConfig> dbConfigs){
		if(dbConfigs != null && dbConfigs.size() > 0){
			for (DBConfig dbConfig:dbConfigs){
				initDS( dbConfig);
			}
		}
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




}
