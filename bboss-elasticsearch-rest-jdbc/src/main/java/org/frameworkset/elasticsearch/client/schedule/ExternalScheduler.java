package org.frameworkset.elasticsearch.client.schedule;
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

import org.frameworkset.elasticsearch.client.DB2ESImportBuilder;
import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.elasticsearch.client.ESDataImportException;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/4/13 12:59
 * @author biaoping.yin
 * @version 1.0
 */
public class ExternalScheduler {
	private DataStreamBuilder dataStreamBuilder;
	private DataStream dataStream;
	public void dataStream(DataStreamBuilder dataStreamBuilder){
		this.dataStreamBuilder = dataStreamBuilder;
	}

	public void execute(){
		if(dataStream == null) {
			DB2ESImportBuilder db2ESImportBuilder = dataStreamBuilder.builder();
			if(!db2ESImportBuilder.isExternalTimer())
				db2ESImportBuilder.setExternalTimer(true);
			dataStream = db2ESImportBuilder.builder();
			if(dataStream == null)
			{
				throw new ESDataImportException("ExternalScheduler failed: datastream build failed");
			}

//			dataStream.init();

		}
		dataStream.execute();
	}

	public void destroy(){
		if(this.dataStream != null){
			this.dataStream.stop();
		}
	}
}
