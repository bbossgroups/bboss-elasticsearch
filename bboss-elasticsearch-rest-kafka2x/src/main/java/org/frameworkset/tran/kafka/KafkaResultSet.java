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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.AsynBaseTranResultSet;
import org.frameworkset.tran.Record;

import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/8/3 12:27
 * @author biaoping.yin
 * @version 1.0
 */
public class KafkaResultSet extends AsynBaseTranResultSet {
	protected KafkaContext kafkaContext;
	public KafkaResultSet(ImportContext importContext) {
		super(importContext);
		kafkaContext = (KafkaContext)importContext;
	}

	@Override
	protected Record buildRecord(Object data) {
		if(this.kafkaContext.getValueCodec() == KafkaImportConfig.CODEC_JSON) {
			return new KafkaMapRecord((ConsumerRecord<Object, Map<String, Object>>) data);
		}
		else if(this.kafkaContext.getValueCodec() == KafkaImportConfig.CODEC_TEXT){
			return new KafkaStringRecord((ConsumerRecord<Object, String>) data);
		}
		else{
			ConsumerRecord consumerRecord = (ConsumerRecord)data;
			Object value = consumerRecord.value();
			if(value instanceof Map){
				return new KafkaMapRecord(consumerRecord);
			}
			else if(value instanceof String){
				return new KafkaStringRecord(consumerRecord);
			}
			throw new IllegalArgumentException(new StringBuilder().append("unknown consumerRecord").append(consumerRecord.toString()).toString());
		}
	}


}
