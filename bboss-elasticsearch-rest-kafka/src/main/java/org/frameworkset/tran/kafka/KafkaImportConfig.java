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

import java.util.Properties;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/9/20 11:46
 * @author biaoping.yin
 * @version 1.0
 */
public class KafkaImportConfig extends BaseImportConfig {
	private Properties kafkaConfigs = null;
	private String kafkaTopic;
	private long checkinterval = 3000l;
	private int consumerThreads;
	private int pollTimeOut;
	public static final String CODEC_TEXT = "text";
	public static final String CODEC_LONG = "long";
	public static final String CODEC_JSON = "json";
	public static final String CODEC_INTEGER = "int";
	/**
	 * json
	 * text
	 */
	private String valueCodec;

	/**
	 * json
	 * text
	 */
	private String keyCodec;
	/**
	 * 并行消费处理消息
	 */
	private boolean discardRejectMessage = false ;
	public void setKafkaConfigs(Properties kafkaConfigs) {
		this.kafkaConfigs = kafkaConfigs;
	}

	public void setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
	}

	public Properties getKafkaConfigs(){
		return kafkaConfigs;
	}

	public String getKafkaTopic(){
		return kafkaTopic;
	}

	public long getCheckinterval() {
		return checkinterval;
	}

	public void setCheckinterval(long checkinterval) {
		this.checkinterval = checkinterval;
	}

	public boolean isDiscardRejectMessage() {
		return discardRejectMessage;
	}

	public void setDiscardRejectMessage(boolean discardRejectMessage) {
		this.discardRejectMessage = discardRejectMessage;
	}

	public int getConsumerThreads() {
		return consumerThreads;
	}

	public void setConsumerThreads(int threads) {
		this.consumerThreads = threads;
	}

	public int getPollTimeOut() {
		return pollTimeOut;
	}

	public void setPollTimeOut(int pollTimeOut) {
		this.pollTimeOut = pollTimeOut;
	}

	public String getValueCodec() {
		return valueCodec;
	}

	public void setValueCodec(String valueCodec) {
		this.valueCodec = valueCodec;
	}

	public String getKeyCodec() {
		return keyCodec;
	}

	public void setKeyCodec(String keyCodec) {
		this.keyCodec = keyCodec;
	}
}
