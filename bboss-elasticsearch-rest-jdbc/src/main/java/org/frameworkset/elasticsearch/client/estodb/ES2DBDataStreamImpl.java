package org.frameworkset.elasticsearch.client.estodb;/*
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

/**
 * 数据库同步到Elasticsearch
 */
public class ES2DBDataStreamImpl extends DataStream{
	private ES2DB es2DB;
	private static Logger logger = LoggerFactory.getLogger(DataStream.class);

	public void execute() throws ESDataImportException{
		if(es2DB == null){
			throw new ESDataImportException("es2DB is null.");
		}
		try {
			initES(es2DB.getApplicationPropertiesFile());
			initDS(es2DB.getDbConfig());
			es2DB.exportData2DB();
		}
		catch (Exception e) {
			throw new ESDataImportException(e);
		}
		finally{

		}
	}

	@Override
	public void stop() {

	}




	public void setEs2DB(ES2DB es2DB) {
		this.es2DB = es2DB;
	}
}
