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

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.config.BaseImportBuilder;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class DBExportBuilder extends BaseImportBuilder {

	protected String sqlFilepath;
	protected String sqlName;
	protected String sql;
	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(SimpleStringUtil.object2json(this));
		return ret.toString();
	}


	protected void buildDBImportConfig(DBImportConfig dbImportConfig){
		dbImportConfig.setSqlFilepath(sqlFilepath);
		dbImportConfig.setSqlName(sqlName);
		dbImportConfig.setSql(this.sql);
	}




	public String getSql() {
		return sql;
	}

	public DBExportBuilder setSql(String sql) {
		this.sql = sql;
		return this;
	}


	public String getSqlName() {
		return sqlName;
	}

	public DBExportBuilder setSqlName(String sqlName) {
		this.sqlName = sqlName;
		return this;
	}



	public String getSqlFilepath() {
		return sqlFilepath;
	}

	public void setSqlFilepath(String sqlFilepath) {
		this.sqlFilepath = sqlFilepath;
	}
}
