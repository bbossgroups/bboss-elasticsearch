package org.frameworkset.tran.kafka.input.es;
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

import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.tran.kafka.KafkaExportBuilder;
import org.frameworkset.tran.kafka.KafkaImportConfig;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public class Kafka2ESExportBuilder extends KafkaExportBuilder {

	public static Kafka2ESExportBuilder newInstance(){
		return new Kafka2ESExportBuilder();
	}


	@Override
	public DataStream builder() {
		super.builderConfig();
//		this.buildDBConfig();
//		this.buildStatusDBConfig();
		try {
			logger.info("Kafka to Elasticsearch Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		KafkaImportConfig es2DBImportConfig = new KafkaImportConfig();
		super.buildImportConfig(es2DBImportConfig);
		es2DBImportConfig.setCheckinterval(this.getCheckinterval());
		es2DBImportConfig.setDiscardRejectMessage(this.isDiscardRejectMessage());
		es2DBImportConfig.setKafkaConfigs(this.getKafkaConfigs());
		es2DBImportConfig.setKafkaTopic(this.getKafkaTopic());
		es2DBImportConfig.setPollTimeOut(this.getPollTimeOut());
		es2DBImportConfig.setConsumerThreads(this.getConsumerThreads());
		es2DBImportConfig.setCodec(this.getCodec());
		Kafka2ESDataStreamImpl dataStream = new Kafka2ESDataStreamImpl();
		dataStream.setImportConfig(es2DBImportConfig);
		return dataStream;
	}

}
