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

import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.BaseDataTranPlugin;
import org.frameworkset.tran.DataTranPlugin;
import org.frameworkset.tran.kafka.KafkaContext;
import org.frameworkset.tran.kafka.KafkaResultSet;
import org.frameworkset.tran.kafka.input.es.Kafka2ESDataTran;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 14:35
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseKafkaInputPlugin extends BaseDataTranPlugin implements DataTranPlugin {
	protected KafkaContext kafkaContext;
	protected void init(ImportContext importContext){
		super.init(importContext);
		kafkaContext = (KafkaContext)importContext;

	}


	public BaseKafkaInputPlugin(ImportContext importContext){
		super(importContext);


	}
	@Override
	public void importData() throws ESDataImportException {


			long importStartTime = System.currentTimeMillis();
			this.doImportData();
			long importEndTime = System.currentTimeMillis();
			if( isPrintTaskLog())
				logger.info(new StringBuilder().append("Execute job Take ").append((importEndTime - importStartTime)).append(" ms").toString());


	}

	@Override
	public void beforeInit() {
		initKafkaInputPlugin();

	}

	protected void initKafkaInputPlugin(){
		/**
		 * 	<property name="kafkabatchconsumerstore"
		 * 			  class="org.frameworkset.plugin.kafka.TestKafkaBatchConsumer2ndStore" init-method="init"
		 * 			  f:batchsize="1000"
		 * 			  f:checkinterval="10000"
		 * 			  f:pollTimeOut="2000"
		 * 			  f:consumerPropes="attr:consumerPropes" f:topic="blackcatbatchstore"
		 * 			  f:partitions="4" />
		 */

	}
	@Override
	public void afterInit(){
	}

	protected abstract void initKafkaTranBatchConsumer2ndStore(Kafka2ESDataTran kafka2ESDataTran) throws Exception;

	public void doImportData()  throws ESDataImportException{
		KafkaResultSet kafkaResultSet = new KafkaResultSet(this.importContext);
//		final CountDownLatch countDownLatch = new CountDownLatch(1);
		final Kafka2ESDataTran kafka2ESDataTran = new Kafka2ESDataTran(kafkaResultSet,importContext);

		Thread tranThread = null;
		try {
			tranThread = new Thread(new Runnable() {
				@Override
				public void run() {
					kafka2ESDataTran.tran();
				}
			},"kafka-elasticsearch-Tran");
			tranThread.start();

			this.initKafkaTranBatchConsumer2ndStore(kafka2ESDataTran);
		} catch (ESDataImportException e) {
			throw e;
		} catch (Exception e) {
			throw new ESDataImportException(e);
		}
		finally {
//			kafkaResultSet.reachEend();
//			try {
//				countDownLatch.await();
//			} catch (InterruptedException e) {
//				if(logger.isErrorEnabled())
//					logger.error("",e);
//			}
		}

	}

	@Override
	public String getLastValueVarName() {
		return importContext.getLastValueClumnName();
	}
}
