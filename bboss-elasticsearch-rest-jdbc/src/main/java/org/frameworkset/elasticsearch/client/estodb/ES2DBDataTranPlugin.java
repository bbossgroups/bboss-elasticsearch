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

import com.frameworkset.common.poolman.ConfigSQLExecutor;
import com.frameworkset.common.poolman.util.SQLUtil;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.DBConfig;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.TaskFailedException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.schedule.SQLInfo;
import org.frameworkset.elasticsearch.client.tran.BaseDataTranPlugin;
import org.frameworkset.elasticsearch.client.tran.DataTranPlugin;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.util.tokenizer.TextGrammarParser;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 14:35
 * @author biaoping.yin
 * @version 1.0
 */
public class ES2DBDataTranPlugin extends BaseDataTranPlugin implements DataTranPlugin {
	private SQLInfo sqlInfo;
	private ConfigSQLExecutor executor;
	private ES2DBContext dbContext;

	public ES2DBDataTranPlugin(ImportContext importContext){
		super(importContext);
		dbContext = (ES2DBContext)importContext;

	}
	public void initSQLInfo(){

		if(dbContext.getSql() == null || dbContext.getSql().equals("")){

			if(dbContext.getSqlFilepath() != null && !dbContext.getSqlFilepath().equals(""))
			try {
				ConfigSQLExecutor executor = new ConfigSQLExecutor(dbContext.getSqlFilepath());
				org.frameworkset.persitent.util.SQLInfo sqlInfo = executor.getSqlInfo(dbContext.getSqlName());
				this.executor = executor;
				dbContext.setSql(sqlInfo.getSql());
			}
			catch (SQLException e){
				throw new ESDataImportException(e);
			}

		}
		if(dbContext.getSql() != null && !dbContext.getSql().equals("")) {
			importContext.setStatusTableId(dbContext.getSql().hashCode());
			initSQLInfoParams();
		}

	}
	private void initSQLInfoParams(){
		String originSQL = dbContext.getSql();
		List<TextGrammarParser.GrammarToken> tokens =
				TextGrammarParser.parser(originSQL, "#[", "]");
		SQLInfo _sqlInfo = new SQLInfo();
		int paramSize = 0;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < tokens.size(); i ++){
			TextGrammarParser.GrammarToken token = tokens.get(i);
			if(token.texttoken()){
				builder.append(token.getText());
			}
			else {
				builder.append("?");
				if(paramSize == 0){
					_sqlInfo.setLastValueVarName(token.getText());
				}
				paramSize ++;

			}
		}
		_sqlInfo.setParamSize(paramSize);
		_sqlInfo.setSql(builder.toString());
		this.sqlInfo = _sqlInfo;


	}
	public SQLInfo getSqlInfo() {
		return sqlInfo;
	}








	private void commonImportData(ESExporterScrollHandler<Map> esExporterScrollHandler) throws Exception {
		Map params = dbContext.getParams() != null ?dbContext.getParams():new HashMap();
		params.put("size", importContext.getStoreBatchSize());//每页5000条记录
		if(dbContext.isSliceQuery()){
			params.put("sliceMax",dbContext.getSliceSize());
		}
		exportESData(  esExporterScrollHandler,  params);
	}

	private void exportESData(ESExporterScrollHandler<Map> esExporterScrollHandler,Map params){

		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟；大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(dbContext.getDslFile());

		ESDatas<Map> response = null;
		if(!dbContext.isSliceQuery()) {
			if(!importContext.isParallel()) {
				response = clientUtil.scroll(dbContext.getQueryUrl(), dbContext.getDslName(), dbContext.getScrollLiveTime(), params, Map.class, esExporterScrollHandler);
			}
			else
			{
				response = clientUtil.scrollParallel(dbContext.getQueryUrl(),
						dbContext.getDslName(), dbContext.getScrollLiveTime(),
						params, Map.class, esExporterScrollHandler);
			}
		}
		else{
			response = clientUtil.scrollSliceParallel(dbContext.getQueryUrl(), dbContext.getDslName(),  params, dbContext.getScrollLiveTime(),Map.class, esExporterScrollHandler);
		}
		if(logger.isInfoEnabled()) {
			if(response != null) {
				logger.info("Export compoleted and export total {} records.", response.getTotalSize());
			}
			else{
				logger.info("Export compoleted and export no records or failed.");
			}
		}
	}
	private void increamentImportData(ESExporterScrollHandler<Map> esExporterScrollHandler) throws Exception {
		Map params = dbContext.getParams() != null ?dbContext.getParams():new HashMap();
		params.put("size", importContext.getStoreBatchSize());//每页5000条记录
		if(dbContext.isSliceQuery()){
			params.put("sliceMax",dbContext.getSliceSize());
		}
		params.put(importContext.getLastValueClumnName(),getParamValue());
		exportESData(  esExporterScrollHandler,  params);

	}

	public void doImportData()  throws ESDataImportException{

		ESExporterScrollHandler<Map> esExporterScrollHandler = new ESExporterScrollHandler<Map>(importContext,executor);

		try {
			if (!isIncreamentImport()) {

				commonImportData(esExporterScrollHandler);

			} else {

				increamentImportData(  esExporterScrollHandler);

			}
		}
		catch (ESDataImportException e){
			throw e;
		}
		catch (Exception e){
			throw new ESDataImportException(e);
		}
	}



	public String getLastValueVarName(){
		return this.sqlInfo != null?this.sqlInfo.getLastValueVarName():null;
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
	public void beforeInit() {
		this.initES(importContext.getApplicationPropertiesFile());
		this.initDS(importContext.getDbConfig());
		initOtherDSes(importContext.getConfigs());
		this.initSQLInfo();

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
