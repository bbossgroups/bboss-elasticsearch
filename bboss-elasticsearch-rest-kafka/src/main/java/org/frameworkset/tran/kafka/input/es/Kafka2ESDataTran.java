package org.frameworkset.tran.kafka.input.es;

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.TranResultSet;
import org.frameworkset.tran.es.output.AsynESOutPutDataTran;
import org.frameworkset.tran.kafka.KafkaData;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Kafka2ESDataTran extends AsynESOutPutDataTran<List> {

	public Kafka2ESDataTran(TranResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet,importContext);
	}

	@Override
	public void appendInData(List data) {
		super.appendData(new KafkaData(data));
	}

	public Kafka2ESDataTran(TranResultSet jdbcResultSet, ImportContext importContext, String cluster) {
		super(jdbcResultSet,importContext, cluster);
	}

	public Kafka2ESDataTran(TranResultSet jdbcResultSet, ImportContext importContext, String esCluster, CountDownLatch countDownLatch) {
		super(jdbcResultSet, importContext, esCluster, countDownLatch);
	}

	public Kafka2ESDataTran(TranResultSet jdbcResultSet, ImportContext importContext, CountDownLatch countDownLatch) {
		super(jdbcResultSet, importContext, countDownLatch);
	}
}
