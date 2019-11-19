package org.frameworkset.tran.kafka;
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

import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.context.BaseImportContext;

import java.util.Properties;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class KafkaImportContext extends BaseImportContext implements KafkaContext {
	private KafkaImportConfig kafkaImportConfig;

	@Override
	protected void init(BaseImportConfig baseImportConfig) {
		super.init(baseImportConfig);
		this.kafkaImportConfig = (KafkaImportConfig)baseImportConfig;
	}

	public KafkaImportContext(KafkaImportConfig importConfig) {
		super(importConfig);
	}



	@Override
	public Properties getKafkaConfigs() {
		return kafkaImportConfig.getKafkaConfigs();
	}

	@Override
	public String getKafkaTopic() {
		return kafkaImportConfig.getKafkaTopic();
	}
	public long getCheckinterval() {
		return kafkaImportConfig.getCheckinterval();
	}



	public boolean isDiscardRejectMessage() {
		return kafkaImportConfig.isDiscardRejectMessage();
	}



	public int getConsumerThreads() {
		return kafkaImportConfig.getConsumerThreads();
	}



	public int getPollTimeOut() {
		return kafkaImportConfig.getPollTimeOut();
	}

	public String getValueCodec(){
		return kafkaImportConfig.getValueCodec();
	}

	public String getKeyCodec(){
		return kafkaImportConfig.getKeyCodec();
	}
}
