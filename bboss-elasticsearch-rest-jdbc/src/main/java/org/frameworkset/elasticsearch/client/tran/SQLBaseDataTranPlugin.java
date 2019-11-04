package org.frameworkset.elasticsearch.client.tran;
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

import bboss.org.apache.velocity.VelocityContext;
import com.frameworkset.common.poolman.ConfigSQLExecutor;
import com.frameworkset.common.poolman.util.SQLUtil;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.DBConfig;
import org.frameworkset.elasticsearch.client.task.TaskFailedException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.db2es.DBContext;
import org.frameworkset.elasticsearch.client.schedule.SQLInfo;
import org.frameworkset.soa.BBossStringWriter;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/31 22:37
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class SQLBaseDataTranPlugin extends BaseDataTranPlugin {
	public SQLBaseDataTranPlugin(ImportContext importContext) {
		super(importContext);
	}
	protected SQLInfo sqlInfo;
	protected ConfigSQLExecutor executor;
	protected DBContext dbContext;
	protected void init(ImportContext importContext){
		dbContext = (DBContext)importContext;
	}
	private VelocityContext buildVelocityContext()
	{


		VelocityContext context_ = new VelocityContext();

//		com.frameworkset.common.poolman.Param temp = null;
//		if(sqlparams != null && sqlparams.size()>0)
//		{
//
//			Iterator<Map.Entry<String, com.frameworkset.common.poolman.Param>> it = sqlparams.entrySet().iterator();
//			while(it.hasNext())
//			{
//				Map.Entry<String, com.frameworkset.common.poolman.Param> entry = it.next();
//				temp = entry.getValue();
//
//				if(!temp.getType().equals(NULL))
//					context_.put(entry.getKey(), temp.getData());
//			}
//		}
		return context_;

	}
	protected String parserSQL(org.frameworkset.persitent.util.SQLInfo sqlinfo){
		String sql = null;
		if(sqlinfo.istpl())
		{
			sqlinfo.getSqltpl().process();//识别sql语句是不是真正的velocity sql模板
			if(sqlinfo.istpl())
			{
				VelocityContext vcontext = buildVelocityContext();//一个context是否可以被同时用于多次运算呢？

				BBossStringWriter sw = new BBossStringWriter();
				sqlinfo.getSqltpl().merge(vcontext,sw);
				sql = sw.toString();
			}
			else
			{
				sql = sqlinfo.getSql();
			}

		}
		else
		{
			sql = sqlinfo.getSql();
		}
		return sql;
	}

	@Override
	public void destroy() {
		super.destroy();

		this.stopDS(importContext.getDbConfig());
		this.stopOtherDSES(importContext.getConfigs());

//		this.importContext.destroy();

	}



	protected void initDS(DBConfig dbConfig){
		if(dbConfig != null && SimpleStringUtil.isNotEmpty(dbConfig.getDbDriver()) && SimpleStringUtil.isNotEmpty(dbConfig.getDbUrl())) {
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

	private void stopDS(DBConfig dbConfig){
		if(dbConfig != null && SimpleStringUtil.isNotEmpty(dbConfig.getDbDriver()) && SimpleStringUtil.isNotEmpty(dbConfig.getDbUrl())){
			try {
				SQLUtil.stopPool(dbConfig.getDbName());
			} catch (Exception e) {
				if(logger.isErrorEnabled())
					logger.error("SQLUtil.stopPool("+dbConfig.getDbName()+") failed:",e);
			}
		}
	}

	private void stopOtherDSES(List<DBConfig> dbConfigs){

		if(dbConfigs != null && dbConfigs.size() > 0){
			for(DBConfig dbConfig:dbConfigs){
				stopDS(dbConfig);
			}
		}
	}



	@Override
	public void afterInit(){
		if(sqlInfo != null
				&& sqlInfo.getParamSize() > 0
				&& !this.isIncreamentImport()){
			throw new TaskFailedException("Parameter variables cannot be set in non-incremental import SQL statements："+dbContext.getSql());
		}
//		this.externalTimer = this.importContext.isExternalTimer();
	}

}
