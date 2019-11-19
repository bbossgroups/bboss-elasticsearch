package org.frameworkset.tran;
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
import org.frameworkset.elasticsearch.client.schedule.SQLInfo;
import org.frameworkset.elasticsearch.client.task.TaskFailedException;
import org.frameworkset.tran.db.DBContext;

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
