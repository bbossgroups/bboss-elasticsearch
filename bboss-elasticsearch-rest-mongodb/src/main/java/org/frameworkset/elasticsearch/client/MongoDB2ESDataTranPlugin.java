package org.frameworkset.elasticsearch.client;
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

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.estodb.ESExporterScrollHandler;
import org.frameworkset.elasticsearch.client.tran.BaseDataTranPlugin;
import org.frameworkset.elasticsearch.client.tran.DataTranPlugin;
import org.frameworkset.nosql.mongodb.MongoDBConfig;
import org.frameworkset.nosql.mongodb.MongoDBHelper;

import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 14:35
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2ESDataTranPlugin extends BaseDataTranPlugin implements DataTranPlugin {

	private MongoDB2ESContext es2DBContext;
	protected void init(ImportContext importContext){
		super.init(importContext);
		es2DBContext = (MongoDB2ESContext)importContext;

	}


	public MongoDB2ESDataTranPlugin(ImportContext importContext){
		super(importContext);


	}

	@Override
	public void beforeInit() {
		this.initES(importContext.getApplicationPropertiesFile());
		initMongoDB();
		initOtherDSes(importContext.getConfigs());


	}

	protected void initMongoDB(){
		MongoDBConfig mongoDBConfig = new MongoDBConfig();
		mongoDBConfig.setName(es2DBContext.getName());
		mongoDBConfig.setServerAddresses(es2DBContext.getServerAddresses());
		mongoDBConfig.setOption(es2DBContext.getOption());//private String option;
		mongoDBConfig.setWriteConcern(es2DBContext.getWriteConcern());//private String writeConcern;
		mongoDBConfig.setReadPreference(es2DBContext.getReadPreference());//private String readPreference;
		mongoDBConfig.setAutoConnectRetry(es2DBContext.getAutoConnectRetry());//private Boolean autoConnectRetry = true;

		mongoDBConfig.setConnectionsPerHost(es2DBContext.getConnectionsPerHost());//private int connectionsPerHost = 50;

		mongoDBConfig.setMaxWaitTime(es2DBContext.getMaxWaitTime());//private int maxWaitTime = 120000;
		mongoDBConfig.setSocketTimeout(es2DBContext.getSocketTimeout());//private int socketTimeout = 0;
		mongoDBConfig.setConnectTimeout(es2DBContext.getConnectTimeout());//private int connectTimeout = 15000;


		/**是否启用sql日志，true启用，false 不启用，*/
		mongoDBConfig.setThreadsAllowedToBlockForConnectionMultiplier(es2DBContext.getThreadsAllowedToBlockForConnectionMultiplier());//private int threadsAllowedToBlockForConnectionMultiplier;
		mongoDBConfig.setSocketKeepAlive(es2DBContext.getSocketKeepAlive());//private Boolean socketKeepAlive = false;

		mongoDBConfig.setMode( es2DBContext.getMode());
		MongoDBHelper.init(mongoDBConfig);
	}
	@Override
	public void afterInit(){
	}


	private void commonImportData(ESExporterScrollHandler<Map> esExporterScrollHandler) throws Exception {
//		Map params = es2DBContext.getParams() != null ?es2DBContext.getParams():new HashMap();
//		params.put("size", importContext.getFetchSize());//每页5000条记录
//		if(es2DBContext.isSliceQuery()){
//			params.put("sliceMax",es2DBContext.getSliceSize());
//		}
//		exportESData(  esExporterScrollHandler,  params);
	}

	private void exportESData(ESExporterScrollHandler<Map> esExporterScrollHandler,Map params){

//		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
//		//scroll上下文有效期1分钟；大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险
//
//		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(es2DBContext.getDslFile());
//
//		ESDatas<Map> response = null;
//		if(!es2DBContext.isSliceQuery()) {
//
//			if(importContext.isParallel() && esExporterScrollHandler instanceof ESDirectExporterScrollHandler) {
//				response = clientUtil.scrollParallel(es2DBContext.getQueryUrl(),
//						es2DBContext.getDslName(), es2DBContext.getScrollLiveTime(),
//						params, Map.class, esExporterScrollHandler);
//			}
//			else
//			{
//				response = clientUtil.scroll(es2DBContext.getQueryUrl(),
//						es2DBContext.getDslName(), es2DBContext.getScrollLiveTime(),
//						params, Map.class, esExporterScrollHandler);
//			}
//		}
//		else{
//			response = clientUtil.scrollSliceParallel(es2DBContext.getQueryUrl(), es2DBContext.getDslName(),
//					params, es2DBContext.getScrollLiveTime(),Map.class, esExporterScrollHandler);
//		}
//		if(logger.isInfoEnabled()) {
//			if(response != null) {
//				logger.info("Export compoleted and export total {} records.", response.getTotalSize());
//			}
//			else{
//				logger.info("Export compoleted and export no records or failed.");
//			}
//		}
	}
	private void increamentImportData(ESExporterScrollHandler<Map> esExporterScrollHandler) throws Exception {
//		Map params = es2DBContext.getParams() != null ?es2DBContext.getParams():new HashMap();
//		params.put("size", importContext.getFetchSize());//每页5000条记录
//		if(es2DBContext.isSliceQuery()){
//			params.put("sliceMax",es2DBContext.getSliceSize());
//		}
//		putLastParamValue(params);
//		exportESData(  esExporterScrollHandler,  params);

	}

	public void doImportData()  throws ESDataImportException{
//		ESTranResultSet jdbcResultSet = new ESTranResultSet(importContext);
//
//		if(es2DBContext.getBatchHandler() != null)
//		{
//			ES2DBDataTran es2DBDataTran = new ES2DBDataTran(jdbcResultSet,importContext);
//			ESDirectExporterScrollHandler esDirectExporterScrollHandler = new ESDirectExporterScrollHandler(importContext,
//					executor,es2DBDataTran);
//			try {
//				if (!isIncreamentImport()) {
//
//					commonImportData(esDirectExporterScrollHandler);
//
//				} else {
//
//					increamentImportData(esDirectExporterScrollHandler);
//
//				}
//			} catch (ESDataImportException e) {
//				throw e;
//			} catch (Exception e) {
//				throw new ESDataImportException(e);
//			}
//		}
//		else {
//
//			final CountDownLatch countDownLatch = new CountDownLatch(1);
//			final ES2DBDataTran es2DBDataTran = new ES2DBDataTran(jdbcResultSet,importContext,countDownLatch);
//			ESExporterScrollHandler<Map> esExporterScrollHandler = new ESExporterScrollHandler<Map>(importContext, executor,
//					es2DBDataTran);
//
//
//
//
//			try {
//				Thread tranThread = new Thread(new Runnable() {
//					@Override
//					public void run() {
//						es2DBDataTran.tran();
//					}
//				});
//				tranThread.start();
//				if (!isIncreamentImport()) {
//
//					commonImportData(esExporterScrollHandler);
//
//				} else {
//
//					increamentImportData(esExporterScrollHandler);
//
//				}
//			} catch (ESDataImportException e) {
//				throw e;
//			} catch (Exception e) {
//				throw new ESDataImportException(e);
//			}
//			finally {
//				jdbcResultSet.reachEend();
//				try {
//					countDownLatch.await();
//				} catch (InterruptedException e) {
//					if(logger.isErrorEnabled())
//						logger.error("",e);
//				}
//			}
//
//		}
	}

	@Override
	public String getLastValueVarName() {
		return importContext.getLastValueClumnName();
	}
}
