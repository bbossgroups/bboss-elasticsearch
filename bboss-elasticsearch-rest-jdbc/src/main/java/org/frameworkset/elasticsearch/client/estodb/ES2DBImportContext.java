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
import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.context.BaseImportContext;
import org.frameworkset.elasticsearch.client.tran.DataTranPlugin;
import org.frameworkset.tran.db.output.TranSQLInfo;

import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public class ES2DBImportContext extends BaseImportContext implements ES2DBContext{
	private ES2DBImportConfig es2DBImportConfig;

	public TranSQLInfo getSqlInfo() {
		return sqlInfo;
	}

	public void setSqlInfo(TranSQLInfo sqlInfo) {
		this.sqlInfo = sqlInfo;
	}

	private TranSQLInfo sqlInfo;
	protected  DataTranPlugin buildDataTranPlugin(){
		return new ES2DBDataTranPlugin(this);
	}
	protected void init(BaseImportConfig baseImportConfig){
		es2DBImportConfig = (ES2DBImportConfig)baseImportConfig;
	}
	public ES2DBImportContext(){
		this(new ES2DBImportConfig());

	}
	public ES2DBImportContext(ES2DBImportConfig baseImportConfig){
		super(baseImportConfig);

	}

	@Override
	public String getSql() {
		return es2DBImportConfig.getSql();
	}


	@Override
	public String getSqlFilepath() {
		return es2DBImportConfig.getDsl2ndSqlFile();
	}

	@Override
	public String getSqlName() {
		return es2DBImportConfig.getSqlName();
	}

	@Override
	public void setSql(String sql) {
		es2DBImportConfig.setSql(sql);
	}

	@Override
	public Map getParams() {
		return es2DBImportConfig.getParams();
	}

	@Override
	public boolean isSliceQuery() {
		return es2DBImportConfig.isSliceQuery();
	}

	@Override
	public int getSliceSize() {
		return es2DBImportConfig.getSliceSize();
	}



	@Override
	public String getQueryUrl() {
		return es2DBImportConfig.getQueryUrl();
	}

	@Override
	public String getDslName() {
		return es2DBImportConfig.getDslName();
	}

	@Override
	public String getScrollLiveTime() {
		return es2DBImportConfig.getScrollLiveTime();
	}

	@Override
	public String getDslFile() {
		return es2DBImportConfig.getDsl2ndSqlFile();
	}
	public BatchHandler<Map> getBatchHandler(){
		return es2DBImportConfig.getBatchHandler();
	}
}
