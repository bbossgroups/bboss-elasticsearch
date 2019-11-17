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

import com.frameworkset.common.poolman.BatchHandler;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.elasticsearch.client.ExportResultHandler;
import org.frameworkset.elasticsearch.client.WrapedExportResultHandler;
import org.frameworkset.tran.DefualtExportResultHandler;
import org.frameworkset.tran.db.DBExportBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public class ES2DBExportBuilder extends DBExportBuilder {

	private String scrollLiveTime = "100m";

	private transient BatchHandler<Map> batchHandler;
	private Map params;
	/**indexName/_search*/
	private String queryUrl;
	private String dsl2ndSqlFile;
	private String dslName;
	private boolean sliceQuery;
	private int sliceSize;

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(SimpleStringUtil.object2json(this));
		return ret.toString();
	}

	@Override
	protected WrapedExportResultHandler buildExportResultHandler(ExportResultHandler exportResultHandler) {
		return new DefualtExportResultHandler<String,String>(exportResultHandler);
	}


	public DataStream builder(){
		super.builderConfig();
//		this.buildDBConfig();
//		this.buildStatusDBConfig();
		try {
			logger.info("ES2DB Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		ES2DBImportConfig es2DBImportConfig = new ES2DBImportConfig();
		super.buildImportConfig(es2DBImportConfig);
		es2DBImportConfig.setDsl2ndSqlFile(this.dsl2ndSqlFile);
		if(this.getSqlFilepath() == null){
			this.setSqlFilepath(this.dsl2ndSqlFile);
		}
//		es2DBImportConfig.setSqlFilepath(dsl2ndSqlFile);
//		es2DBImportConfig.setSqlName(sqlName);
//		es2DBImportConfig.setSql(this.sql);
		super.buildDBImportConfig(es2DBImportConfig);

		es2DBImportConfig.setQueryUrl(this.queryUrl);
		es2DBImportConfig.setScrollLiveTime(this.scrollLiveTime);


		es2DBImportConfig.setBatchHandler(this.batchHandler);
		es2DBImportConfig.setDslName(this.dslName);
		es2DBImportConfig.setSliceQuery(this.sliceQuery);
		es2DBImportConfig.setSliceSize(this.sliceSize);
		es2DBImportConfig.setParams(this.params);
		ES2DBDataStreamImpl dataStream = new ES2DBDataStreamImpl();
		dataStream.setImportConfig(es2DBImportConfig);
		return dataStream;
	}




	public String getQueryUrl() {
		return queryUrl;
	}

	public ES2DBExportBuilder setQueryUrl(String queryUrl) {
		this.queryUrl = queryUrl;
		return this;
	}

	public BatchHandler<Map> getBatchHandler() {
		return batchHandler;
	}

	public ES2DBExportBuilder setBatchHandler(BatchHandler<Map> batchHandler) {
		this.batchHandler = batchHandler;
		return this;
	}

	public String getDsl2ndSqlFile() {
		return dsl2ndSqlFile;
	}

	public ES2DBExportBuilder setDsl2ndSqlFile(String dsl2ndSqlFile) {
		this.dsl2ndSqlFile = dsl2ndSqlFile;
		return this;
	}

	public String getDslName() {
		return dslName;
	}

	public ES2DBExportBuilder setDslName(String dslName) {
		this.dslName = dslName;
		return this;
	}

	public String getScrollLiveTime() {
		return scrollLiveTime;
	}

	public ES2DBExportBuilder setScrollLiveTime(String scrollLiveTime) {
		this.scrollLiveTime = scrollLiveTime;
		return this;
	}

	public boolean isSliceQuery() {
		return sliceQuery;
	}

	public ES2DBExportBuilder setSliceQuery(boolean sliceQuery) {
		this.sliceQuery = sliceQuery;
		return this;
	}

	public int getSliceSize() {
		return sliceSize;
	}

	public ES2DBExportBuilder setSliceSize(int sliceSize) {
		this.sliceSize = sliceSize;
		return this;
	}
	public ES2DBExportBuilder addParam(String key, Object value){
		if(params == null)
			params = new HashMap();
		this.params.put(key,value);
		return this;
	}

}
