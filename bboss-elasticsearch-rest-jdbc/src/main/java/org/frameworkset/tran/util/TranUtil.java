package org.frameworkset.tran.util;
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
import com.frameworkset.util.VariableHandler;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.persitent.util.SQLInfo;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.tran.db.output.DBOutPutContext;
import org.frameworkset.tran.db.output.TranSQLInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/18 0:18
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class TranUtil {
	public static void initSQLInfo(DBOutPutContext dbContext, ImportContext importContext) throws ESDataImportException {
		TranSQLInfo sqlInfo = new TranSQLInfo();

		ConfigSQLExecutor configSQLExecutor = new ConfigSQLExecutor(dbContext.getSqlFilepath());

		try {
			SQLInfo sqlinfo = configSQLExecutor.getSqlInfo(importContext.getDbConfig().getDbName(), dbContext.getSqlName());
			sqlInfo.setOriginSQL(sqlinfo.getSql());
			String sql = parserSQL(  sqlinfo);

			VariableHandler.SQLStruction sqlstruction = sqlinfo.getSqlutil().getSQLStruction(sqlinfo,sql);
			sql = sqlstruction.getSql();
			sqlInfo.setSql(sql);
			List<VariableHandler.Variable> vars = sqlstruction.getVariables();
			sqlInfo.setVars(vars);
			dbContext.setSqlInfo(sqlInfo);
		} catch (SQLException e) {
			throw new ESDataImportException("Init SQLInfo failed",e);
		}


	}
	private static VelocityContext buildVelocityContext()
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
	public static String parserSQL(org.frameworkset.persitent.util.SQLInfo sqlinfo){
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
}
