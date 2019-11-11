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
import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.context.ImportContext;

/**
 * 数据库同步到Elasticsearch
 */
public class ES2DBDataStreamImpl extends DataStream {
	private ES2DBImportConfig es2DBImportConfig;
	protected ImportContext buildImportContext(BaseImportConfig importConfig){
		return new ES2DBImportContext(es2DBImportConfig);
	}
//	public void execute() throws ESDataImportException{
//		if(es2DBImportConfig == null){
//			throw new ESDataImportException("es2DB is null.");
//		}
//		try {
//			/** fix
//			initES(es2DB.getApplicationPropertiesFile());
//			initDS(es2DB.getDbConfig());
//			initOtherDSes(es2DB.getConfigs());*/
//			this.importContext = new ES2DBImportContext(es2DBImportConfig);
////			es2DBImportConfig.exportData2DB();
//		}
//		catch (Exception e) {
//			throw new ESDataImportException(e);
//		}
//		finally{
//
//		}
//	}




	public void setEs2DBImportConfig(ES2DBImportConfig es2DBImportConfig) {
		this.es2DBImportConfig = es2DBImportConfig;
		this.importConfig = es2DBImportConfig;
	}
}
