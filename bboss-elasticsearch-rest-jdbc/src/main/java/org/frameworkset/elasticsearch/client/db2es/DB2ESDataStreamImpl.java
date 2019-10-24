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

import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据库同步到Elasticsearch
 */
public class DB2ESDataStreamImpl extends DataStream{
	private ESJDBC esjdbc;


	private static Logger logger = LoggerFactory.getLogger(DataStream.class);
	private boolean inited;
//	public void setExternalTimer(boolean externalTimer) {
//		this.esjdbc.setExternalTimer(externalTimer);
//	}
	private Lock lock = new ReentrantLock();

	public void init(){
		if(inited )
			return;
		if(esjdbc == null){
			throw new ESDataImportException("ESJDBC is null.");
		}

		try {
			lock.lock();
			this.importContext = new DB2ESImportContext(esjdbc);


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



	@Override
	public String getConfigString() {
		return this.toString();
	}



	public void setEsjdbc(ESJDBC esjdbc){
		this.esjdbc = esjdbc;
	}












}
