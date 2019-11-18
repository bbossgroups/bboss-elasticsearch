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

import org.frameworkset.elasticsearch.client.ExportResultHandler;
import org.frameworkset.elasticsearch.client.WrapedExportResultHandler;
import org.frameworkset.elasticsearch.client.config.BaseImportBuilder;
import org.frameworkset.tran.DefualtExportResultHandler;

import java.util.Properties;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class KafkaExportBuilder extends BaseImportBuilder {
	private Properties kafkaConfigs = new Properties();
	private String kafkaTopic;
	private long checkinterval = 3000l;
	private int consumerThreads;
	private int pollTimeOut;

	public String getCodec() {
		return codec;
	}

	public KafkaExportBuilder setCodec(String codec) {
		this.codec = codec;
		return this;
	}

	/**
	 * json
	 * text
	 */
	private String codec;

	/**
	 * 并行消费处理消息
	 */
	private boolean discardRejectMessage = false ;


	@Override
	protected WrapedExportResultHandler buildExportResultHandler(ExportResultHandler exportResultHandler) {
		return new DefualtExportResultHandler(exportResultHandler);
	}

	public Properties getKafkaConfigs() {
		return kafkaConfigs;
	}

	public KafkaExportBuilder addKafkaConfig(String key, Object value){
		kafkaConfigs.put(key,value);
		return this;
	}

	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public KafkaExportBuilder setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
		return this;
	}

	public long getCheckinterval() {
		return checkinterval;
	}

	public KafkaExportBuilder setCheckinterval(long checkinterval) {
		this.checkinterval = checkinterval;
		return this;
	}



	public int getPollTimeOut() {
		return pollTimeOut;
	}

	public KafkaExportBuilder setPollTimeOut(int pollTimeOut) {
		this.pollTimeOut = pollTimeOut;
		return this;
	}

	public boolean isDiscardRejectMessage() {
		return discardRejectMessage;
	}

	public KafkaExportBuilder setDiscardRejectMessage(boolean discardRejectMessage) {
		this.discardRejectMessage = discardRejectMessage;
		return this;
	}

	public int getConsumerThreads() {
		return consumerThreads;
	}

	public KafkaExportBuilder setConsumerThreads(int consumerThreads) {
		this.consumerThreads = consumerThreads;
		return this;
	}
}
