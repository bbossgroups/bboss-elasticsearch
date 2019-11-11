package org.frameworkset.elasticsearch.client;/*
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
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DataStream {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected ImportContext importContext;
	protected BaseImportConfig importConfig ;

	private boolean inited;
	//	public void setExternalTimer(boolean externalTimer) {
//		this.esjdbc.setExternalTimer(externalTimer);
//	}
	private Lock lock = new ReentrantLock();
	protected abstract ImportContext buildImportContext(BaseImportConfig importConfig);

	/**
	 *
	 * @throws ESDataImportException
	 */
	public void execute() throws ESDataImportException{

		try {
			this.init();
			importContext.importData();
//			if(this.scheduleService == null) {//一次性执行数据导入操作
//
//				long importStartTime = System.currentTimeMillis();
////				firstImportData();
//				this.dataTranPlugin.importData(new ImportContext() {
//
//				});
//				long importEndTime = System.currentTimeMillis();
//				if( this.dataTranPlugin.isPrintTaskLog() && logger.isInfoEnabled())
//					logger.info(new StringBuilder().append("Execute job Take ").append((importEndTime - importStartTime)).append(" ms").toString());
//			}
//			else{//定时增量导入数据操作
//				if(!this.dataTranPlugin.isExternalTimer()) {//内部定时任务引擎
//					scheduleService.timeSchedule();
//				}
//				else{ //外部定时任务引擎执行的方法，比如quartz之类的
//					scheduleService.externalTimeSchedule();
//				}
//			}
		}
		catch (Exception e) {
			throw new ESDataImportException(e);
		}
		finally{

		}
	}

	public void destroy() {
		if(importContext != null)
			this.importContext.destroy();



//		this.esjdbc.stop();
	}

	public String getConfigString() {
		return configString;
	}

	public void setConfigString(String configString) {
		this.configString = configString;
	}

	private String configString;


	public void init(){
		if(inited ) {
			importContext.resume();
			return;
		}
		if(importConfig == null){
			throw new ESDataImportException("import Config is null.");
		}

		try {
			lock.lock();
			this.importContext = this.buildImportContext(importConfig);


//			this.initES(esjdbc.getApplicationPropertiesFile());
//			this.initDS(esjdbc.getDbConfig());
//			initOtherDSes(esjdbc.getConfigs());
//			this.initSQLInfo();
//			this.initSchedule();
			inited = true;
		}
		catch (Exception e) {
			inited = true;
			throw new ESDataImportException(e);
		}
		finally{


			lock.unlock();
		}
	}
}
