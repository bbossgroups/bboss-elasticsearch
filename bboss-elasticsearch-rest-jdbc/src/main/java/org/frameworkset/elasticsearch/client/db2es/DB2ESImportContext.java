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

import org.frameworkset.elasticsearch.client.DataTranPlugin;
import org.frameworkset.elasticsearch.client.context.BaseImportContext;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public class DB2ESImportContext extends BaseImportContext implements DBContext{
	protected  DataTranPlugin buildDataTranPlugin(){
		return new DBDataTranPlugin(this);
	}

	public DB2ESImportContext(){
		super(new DB2ESImportConfig());
	}
	public DB2ESImportContext(DB2ESImportConfig baseImportConfig){
		super(baseImportConfig);
	}

	@Override
	public String getSql() {
		return ((DB2ESImportConfig)baseImportConfig).getSql();
	}

	@Override
	public String getSqlFilepath() {
		return ((DB2ESImportConfig)baseImportConfig).getSqlFilepath();
	}

	@Override
	public String getSqlName() {
		return ((DB2ESImportConfig)baseImportConfig).getSqlName();
	}

	@Override
	public void setSql(String sql) {
		((DB2ESImportConfig)baseImportConfig).setSql(sql);
	}
}
