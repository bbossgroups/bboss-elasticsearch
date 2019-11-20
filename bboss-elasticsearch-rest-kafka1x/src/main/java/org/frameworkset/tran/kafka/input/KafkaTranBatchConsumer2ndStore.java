package org.frameworkset.tran.kafka.input;
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


import kafka.message.MessageAndMetadata;
import org.apache.kafka.common.serialization.Deserializer;
import org.frameworkset.plugin.kafka.KafkaBatchConsumer2ndStore;
import org.frameworkset.tran.Record;
import org.frameworkset.tran.es.output.AsynESOutPutDataTran;
import org.frameworkset.tran.kafka.KafkaContext;
import org.frameworkset.tran.kafka.KafkaImportConfig;
import org.frameworkset.tran.kafka.KafkaMapRecord;
import org.frameworkset.tran.kafka.KafkaStringRecord;
import org.frameworkset.tran.kafka.codec.CodecUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/9/28 10:41
 * @author biaoping.yin
 * @version 1.0
 */
public class KafkaTranBatchConsumer2ndStore extends KafkaBatchConsumer2ndStore {
	private KafkaContext kafkaContext;
	private Deserializer valueDeserializer;
	private Deserializer keyDeserializer;

	public KafkaTranBatchConsumer2ndStore(AsynESOutPutDataTran asynESOutPutDataTran, KafkaContext kafkaContext) {
		this.asynESOutPutDataTran = asynESOutPutDataTran;
		this.kafkaContext = kafkaContext;
		valueDeserializer = CodecUtil.getDeserializer(kafkaContext.getValueCodec());
		keyDeserializer = CodecUtil.getDeserializer(kafkaContext.getKeyCodec());
	}

	private AsynESOutPutDataTran asynESOutPutDataTran;
	@Override
	public void store(List<MessageAndMetadata<byte[], byte[]>> messages) throws Exception {

		List<Record> records = parserData(messages);
		asynESOutPutDataTran.appendInData(records);
	}

	@Override
	public void store(MessageAndMetadata<byte[], byte[]> message) throws Exception {
		List<MessageAndMetadata<byte[], byte[]>> messages = new ArrayList<>();
		messages.add(message);
		store(messages);
	}

	protected List<Record> parserData(List<MessageAndMetadata<byte[], byte[]>> messages) {
		List<Record> results = new ArrayList<>();
		for(int k = 0; k < messages.size(); k ++) {
			MessageAndMetadata<byte[], byte[]> consumerRecord = messages.get(k);
			if (this.kafkaContext.getValueCodec() == KafkaImportConfig.CODEC_JSON) {
				Object value = valueDeserializer.deserialize(consumerRecord.topic(),consumerRecord.message());
				if (value instanceof List) {
					List<Map> rs = (List<Map>) value;

					for (int i = 0; i < rs.size(); i++) {
						results.add(new KafkaMapRecord(consumerRecord.key(), rs.get(i)));
					}

				} else {
					results.add( new KafkaMapRecord(consumerRecord.key(), (Map<String, Object>) value));
				}
			} else if (this.kafkaContext.getValueCodec() == KafkaImportConfig.CODEC_TEXT) {
				Object value = valueDeserializer.deserialize(consumerRecord.topic(),consumerRecord.message());
				if (value instanceof List) {
					List<String> rs = (List<String>) value;

					for (int i = 0; i < rs.size(); i++) {
						results.add(new KafkaStringRecord(consumerRecord.key(), rs.get(i)));
					}
					//return new KafkaMapRecord((ConsumerRecord<Object, List<Map<String, Object>>>) data);
				} else {
					results.add( new KafkaStringRecord(consumerRecord.key(), (String) value));
				}
			} else {
				Object value = valueDeserializer.deserialize(consumerRecord.topic(),consumerRecord.message());

				if (value instanceof List) {
					List rs = (List) value;

					for (int i = 0; i < rs.size(); i++) {
						Object v = rs.get(i);
						if (v instanceof Map) {
							results.add(new KafkaMapRecord(consumerRecord.key(), (Map<String, Object>) v));
						} else {
							results.add(new KafkaStringRecord(consumerRecord.key(), (String) v));
						}
					}
					//return new KafkaMapRecord((ConsumerRecord<Object, List<Map<String, Object>>>) data);
				} else if (value instanceof Map) {
					results.add( new KafkaMapRecord(consumerRecord.key(), (Map<String, Object>) value));
				} else if (value instanceof String) {
					results.add(new KafkaStringRecord(consumerRecord.key(), (String) value));
				}
				throw new IllegalArgumentException(new StringBuilder().append("unknown consumerRecord with codec[").append(this.kafkaContext.getValueCodec()).append("]").append(consumerRecord.toString()).toString());
			}
		}
		return results;
	}

	@Override
	public void closeService() {
		if(valueDeserializer != null){
			valueDeserializer.close();
		}
		if(keyDeserializer != null){
			keyDeserializer.close();
		}
	}

}
