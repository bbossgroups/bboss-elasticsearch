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

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.tran.AsynBaseTranResultSet;
import org.frameworkset.elasticsearch.client.tran.DataTranPlugin;
import org.frameworkset.elasticsearch.client.tran.SQLBaseDataTranPlugin;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.template.ESInfo;
import org.frameworkset.tran.util.TranUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 14:35
 * @author biaoping.yin
 * @version 1.0
 */
public class ES2DBDataTranPlugin extends SQLBaseDataTranPlugin implements DataTranPlugin {

	private ES2DBContext es2DBContext;
	protected void init(ImportContext importContext){
		super.init(importContext);
		es2DBContext = (ES2DBContext)importContext;

	}


	public ES2DBDataTranPlugin(ImportContext importContext){
		super(importContext);


	}

	@Override
	public void beforeInit() {
		this.initES(importContext.getApplicationPropertiesFile());
		this.initDS(importContext.getDbConfig());
		initOtherDSes(importContext.getConfigs());


	}
	@Override
	public void afterInit(){
		TranUtil.initSQLInfo(es2DBContext,importContext);
		this.initDSLInfo();
	}
	public void initDSLInfo(){
		if(isIncreamentImport() && es2DBContext.getDslFile() != null && !es2DBContext.getDslFile().equals("")) {
			try {
				ClientInterface clientInterface = ElasticSearchHelper.getConfigRestClientUtil(es2DBContext.getDslFile());
				ESInfo esInfo = clientInterface.getESInfo(es2DBContext.getDslName());
				importContext.setStatusTableId(esInfo.getTemplate().hashCode());
			} catch (Exception e) {
				throw new ESDataImportException(e);
			}
		}
	}



	private void commonImportData(BaseESExporterScrollHandler<Map> esExporterScrollHandler) throws Exception {
		Map params = es2DBContext.getParams() != null ?es2DBContext.getParams():new HashMap();
		params.put("size", importContext.getFetchSize());//每页5000条记录
		if(es2DBContext.isSliceQuery()){
			params.put("sliceMax",es2DBContext.getSliceSize());
		}
		exportESData(  esExporterScrollHandler,  params);
	}

	private void exportESData(BaseESExporterScrollHandler<Map> esExporterScrollHandler,Map params){

		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟；大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(es2DBContext.getDslFile());

		ESDatas<Map> response = null;
		if(!es2DBContext.isSliceQuery()) {

			if(importContext.isParallel() && esExporterScrollHandler instanceof ESDirectExporterScrollHandler) {
				response = clientUtil.scrollParallel(es2DBContext.getQueryUrl(),
						es2DBContext.getDslName(), es2DBContext.getScrollLiveTime(),
						params, Map.class, esExporterScrollHandler);
			}
			else
			{
				response = clientUtil.scroll(es2DBContext.getQueryUrl(),
						es2DBContext.getDslName(), es2DBContext.getScrollLiveTime(),
						params, Map.class, esExporterScrollHandler);
			}
		}
		else{
			response = clientUtil.scrollSliceParallel(es2DBContext.getQueryUrl(), es2DBContext.getDslName(),
					params, es2DBContext.getScrollLiveTime(),Map.class, esExporterScrollHandler);
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
	private void increamentImportData(BaseESExporterScrollHandler<Map> esExporterScrollHandler) throws Exception {
		Map params = es2DBContext.getParams() != null ?es2DBContext.getParams():new HashMap();
		params.put("size", importContext.getFetchSize());//每页5000条记录
		if(es2DBContext.isSliceQuery()){
			params.put("sliceMax",es2DBContext.getSliceSize());
		}
		putLastParamValue(params);
		exportESData(  esExporterScrollHandler,  params);

	}

	public void doImportData()  throws ESDataImportException{


		if(es2DBContext.getBatchHandler() != null)
		{

			ESDirectExporterScrollHandler esDirectExporterScrollHandler = new ESDirectExporterScrollHandler(importContext,
					executor);
			try {
				if (!isIncreamentImport()) {

					commonImportData(esDirectExporterScrollHandler);

				} else {

					increamentImportData(esDirectExporterScrollHandler);

				}
			} catch (ESDataImportException e) {
				throw e;
			} catch (Exception e) {
				throw new ESDataImportException(e);
			}
		}
		else {
			AsynBaseTranResultSet jdbcResultSet = new AsynBaseTranResultSet(importContext);
			final CountDownLatch countDownLatch = new CountDownLatch(1);
			final ES2DBOutPutDataTran es2DBDataTran = new ES2DBOutPutDataTran(jdbcResultSet,importContext,countDownLatch);
			ESExporterScrollHandler<Map> esExporterScrollHandler = new ESExporterScrollHandler<Map>(importContext, executor,
					es2DBDataTran);
			try {
				Thread tranThread = new Thread(new Runnable() {
					@Override
					public void run() {
						es2DBDataTran.tran();
					}
				});
				tranThread.start();
				if (!isIncreamentImport()) {

					commonImportData(esExporterScrollHandler);

				} else {

					increamentImportData(esExporterScrollHandler);

				}
			} catch (ESDataImportException e) {
				throw e;
			} catch (Exception e) {
				throw new ESDataImportException(e);
			}
			finally {
				jdbcResultSet.reachEend();
				try {
					countDownLatch.await();
				} catch (InterruptedException e) {
					if(logger.isErrorEnabled())
						logger.error("",e);
				}
			}

		}
	}

	@Override
	public String getLastValueVarName() {
		return importContext.getLastValueClumnName();
	}
}
