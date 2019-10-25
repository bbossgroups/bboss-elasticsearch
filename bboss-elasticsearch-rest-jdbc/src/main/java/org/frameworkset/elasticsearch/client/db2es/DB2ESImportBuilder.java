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

import com.frameworkset.common.poolman.StatementInfo;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.elasticsearch.client.ExportResultHandler;
import org.frameworkset.elasticsearch.client.WrapedExportResultHandler;
import org.frameworkset.elasticsearch.client.config.BaseImportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class DB2ESImportBuilder extends BaseImportBuilder {
	private static Logger logger = LoggerFactory.getLogger(DB2ESImportBuilder.class);
	protected String sqlFilepath;
	protected String sql;
	protected String sqlName;

	protected DB2ESImportBuilder(){

	}





	public DB2ESImportBuilder setShowSql(boolean showSql) {
		_setShowSql(showSql);

		return this;
	}

	/**抽取数据的sql语句*/
	private ResultSet resultSet;
	/**抽取数据的sql语句*/
	private StatementInfo statementInfo;


	public String getSql() {
		return sql;
	}




	public static DB2ESImportBuilder newInstance(){
		return new DB2ESImportBuilder();
	}
	public DB2ESImportBuilder setResultSet(ResultSet resultSet){
		this.resultSet = resultSet;
		return this;
	}

	public DB2ESImportBuilder setStatementInfo(StatementInfo statementInfo){
		this.statementInfo = statementInfo;
		return this;
	}



	public DB2ESImportBuilder setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public DataStream builder(){
		super.builderConfig();
		try {
			logger.info("DB2ES Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		DB2ESImportConfig esjdbc = new DB2ESImportConfig();
//		esjdbc.setImportBuilder(this);
		super.buildImportConfig(esjdbc);
//		esjdbcResultSet.setMetaData(statementInfo.getMeta());
//		esjdbcResultSet.setResultSet(resultSet);

		esjdbc.setSqlFilepath(this.sqlFilepath);
		esjdbc.setSqlName(sqlName);
		if(SimpleStringUtil.isNotEmpty(sql))
			esjdbc.setSql(this.sql);
		DB2ESDataStreamImpl  dataStream = new DB2ESDataStreamImpl();
		dataStream.setEsjdbc(esjdbc);
		dataStream.setConfigString(this.toString());
		dataStream.init();
		return dataStream;
	}




	public DB2ESImportBuilder setSqlFilepath(String sqlFilepath) {
		this.sqlFilepath = sqlFilepath;
		return this;
	}

	public String getSqlName() {
		return sqlName;
	}

	public DB2ESImportBuilder setSqlName(String sqlName) {
		this.sqlName = sqlName;
		return this;
	}

	@Override
	protected WrapedExportResultHandler buildExportResultHandler(ExportResultHandler exportResultHandler) {
		DB2ESExportResultHandler db2ESExportResultHandler = new DB2ESExportResultHandler(exportResultHandler);
		return db2ESExportResultHandler;
	}


//	private IndexPattern splitIndexName(String indexPattern){
//		int idx = indexPattern.indexOf("{");
//		int end = -1;
//		if(idx > 0){
//			end = indexPattern.indexOf("}");
//			IndexPattern _indexPattern = new IndexPattern();
//			_indexPattern.setIndexPrefix(indexPattern.substring(0,idx));
//			_indexPattern.setDateFormat(indexPattern.substring(idx + 1,end));
//			if(end < indexPattern.length()){
//				_indexPattern.setIndexEnd(indexPattern.substring(end+1));
//			}
//			return _indexPattern;
//		}
//		return null;
//
//
//	}


}
