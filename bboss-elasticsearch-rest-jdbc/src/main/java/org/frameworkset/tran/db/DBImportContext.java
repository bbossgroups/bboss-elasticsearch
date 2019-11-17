package org.frameworkset.tran.db;
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

import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.context.BaseImportContext;
import org.frameworkset.tran.db.output.DBOutPutContext;
import org.frameworkset.tran.db.output.TranSQLInfo;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class DBImportContext extends BaseImportContext implements DBOutPutContext {
	protected DBImportConfig dbImportConfig;

	public TranSQLInfo getSqlInfo() {
		return sqlInfo;
	}

	public void setSqlInfo(TranSQLInfo sqlInfo) {
		this.sqlInfo = sqlInfo;
	}

	private TranSQLInfo sqlInfo;

	protected void init(BaseImportConfig baseImportConfig){
		dbImportConfig = (DBImportConfig)baseImportConfig;
	}
	public DBImportContext(){
	}
	public DBImportContext(BaseImportConfig baseImportConfig){
		super(baseImportConfig);

	}

	@Override
	public String getSql() {
		return dbImportConfig.getSql();
	}


	@Override
	public String getSqlFilepath() {
		return dbImportConfig.getSqlFilepath();
	}

	@Override
	public String getSqlName() {
		return dbImportConfig.getSqlName();
	}

	@Override
	public void setSql(String sql) {
		dbImportConfig.setSql(sql);
	}





}
