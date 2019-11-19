package org.frameworkset.elasticsearch.client.estodb;

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.TranResultSet;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.tran.db.output.AsynDBOutPutDataTran;

import java.util.concurrent.CountDownLatch;

public class ES2DBOutPutDataTran extends AsynDBOutPutDataTran<ESDatas> {


	public ES2DBOutPutDataTran(TranResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet, importContext);
	}

	public ES2DBOutPutDataTran(TranResultSet jdbcResultSet, ImportContext importContext, CountDownLatch countDownLatch) {
		super(jdbcResultSet, importContext, countDownLatch);
	}



	public void appendInData(ESDatas datas){
		super.appendData(new ESDatasWraper(datas));
	}
}
