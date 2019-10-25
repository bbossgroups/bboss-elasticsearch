package org.frameworkset.elasticsearch.client.db2es;/*
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

import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB2ESImportConfig extends BaseImportConfig {
	private static Logger logger = LoggerFactory.getLogger(DB2ESImportConfig.class);


	private String sql;
	private String sqlFilepath;
	private String sqlName;




	public String getSql() {
		return sql;
	}


	public void setSql(String sql) {
		this.sql = sql;
	}





//	public String getIndex() {
//		return index;
//	}
//
//	public void setIndex(String index) {
//		this.index = index;
//	}
//
//	public String getIndexType() {
//		return indexType;
//	}
//
//	public void setIndexType(String indexType) {
//		this.indexType = indexType;
//	}



//	public String getLastValueStoreTableName() {
//		return importIncreamentConfig != null?importIncreamentConfig.getLastValueStoreTableName():null;
//	}
//
//	public String getLastValueStorePath() {
//		return importIncreamentConfig != null?importIncreamentConfig.getLastValueStorePath():null;
//	}
//
//	public String getDateLastValueColumn() {
//		return importIncreamentConfig != null?importIncreamentConfig.getDateLastValueColumn():null;
//	}
//	public String getNumberLastValueColumn() {
//		return importIncreamentConfig != null?importIncreamentConfig.getNumberLastValueColumn():null;
//	}
//
//	public Integer getLastValueType() {
//		return importIncreamentConfig != null?importIncreamentConfig.getLastValueType():null;
//	}
//	public void setImportIncreamentConfig(ImportIncreamentConfig importIncreamentConfig) {
//		this.importIncreamentConfig = importIncreamentConfig;
//	}
//
//	public boolean isFromFirst() {
//		return importIncreamentConfig != null?importIncreamentConfig.isFromFirst():false;
//	}
//
//	public Long getConfigLastValue() {
//		return importIncreamentConfig != null?importIncreamentConfig.getLastValue():null;
//	}

	public String getSqlFilepath() {
		return sqlFilepath;
	}

	public void setSqlFilepath(String sqlFilepath) {
		this.sqlFilepath = sqlFilepath;
	}



	public String getSqlName() {
		return sqlName;
	}

	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}



//	public IndexPattern getIndexPattern() {
//		return indexPattern;
//	}
//
//	public void setIndexPattern(IndexPattern indexPattern) {
//		this.indexPattern = indexPattern;
//	}

//	public String buildIndexName(){
//		if(this.indexPattern == null){
//			return this.index;
//		}
//		SimpleDateFormat dateFormat = new SimpleDateFormat(this.indexPattern.getDateFormat());
//		String date = dateFormat.format(new Date());
//		StringBuilder builder = new StringBuilder();
//		builder.append(indexPattern.getIndexPrefix()).append(date);
//		if(indexPattern.getIndexEnd() != null){
//			builder.append(indexPattern.getIndexEnd());
//		}
//		return builder.toString();
//	}

}
