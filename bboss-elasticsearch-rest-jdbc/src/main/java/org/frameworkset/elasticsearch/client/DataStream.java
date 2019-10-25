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

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataStream {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	protected ImportContext importContext;


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

	public void stop() {
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


	public void init() {
	}
}
