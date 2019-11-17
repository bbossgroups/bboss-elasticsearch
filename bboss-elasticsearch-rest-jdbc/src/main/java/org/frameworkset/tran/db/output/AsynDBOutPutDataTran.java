package org.frameworkset.tran.db.output;

import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.tran.AsynTranResultSet;
import org.frameworkset.elasticsearch.client.tran.Data;
import org.frameworkset.elasticsearch.client.tran.TranResultSet;

import java.util.concurrent.CountDownLatch;

public abstract class AsynDBOutPutDataTran<T> extends DBOutPutDataTran<T> {
	protected AsynTranResultSet esTranResultSet;
	private CountDownLatch countDownLatch;
	protected void init(){
		super.init();
		esTranResultSet = (AsynTranResultSet)jdbcResultSet;

	}


	public AsynDBOutPutDataTran(TranResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet,importContext);
	}
	public AsynDBOutPutDataTran(TranResultSet jdbcResultSet, ImportContext importContext, CountDownLatch countDownLatch) {
		super(jdbcResultSet,importContext);
		this.countDownLatch = countDownLatch;
	}
//	public void appendData(ESDatas datas){
//		esTranResultSet.appendData(new ESDatasWraper(datas));
//	}
	protected abstract void appendInData(T data);
	protected void appendData(Data datas){
		esTranResultSet.appendData(datas);
	}


	public void stop(){
		esTranResultSet.stop();
		super.stop();
	}

	@Override
	public String tran() throws ESDataImportException {
		try {
			return super.tran();
		}
		finally {
			if(this.countDownLatch != null)
				countDownLatch.countDown();
		}
	}
}
