package org.frameworkset.tran;
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
import org.frameworkset.elasticsearch.client.util.TranUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/28 22:37
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class AsynBaseTranResultSet<T extends Data> implements AsynTranResultSet<T> {
	private Record record;
	private List<Object> records;
	private int pos = 0;
	private int size;
	public static int STATUS_STOP = 1;
	private int status;
	private BlockingQueue<T> queue ;
	protected ImportContext importContext;
	public AsynBaseTranResultSet(ImportContext importContext) {
		queue = new ArrayBlockingQueue<T>(importContext.getTranDataBufferQueue());
		this.importContext = importContext;
	}
	protected abstract Record buildRecord(Object data);

	public void appendData(T datas){

		try {
			queue.put(datas);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		this.esDatas = datas;
//		this.records = esDatas.getDatas();
//		size = records !=null ?records.size():0;
//		pos = 0;
//		totalSize = esDatas.getTotalSize();
//		this.handlerInfo = handlerInfo;


	}

	@Override
	public Object getValue(int i, String colName, int sqlType) throws ESDataImportException {
		return getValue(  colName);
	}

	@Override
	public Object getValue(String colName) throws ESDataImportException {
		return record.getValue(colName);
	}

	@Override
	public Object getValue(String colName, int sqlType) throws ESDataImportException {
		return getValue(  colName);
	}

	@Override
	public Date getDateTimeValue(String colName) throws ESDataImportException {
		Object value = getValue(  colName);
		if(value == null)
			return null;
		return TranUtil.getDateTimeValue(colName,value,importContext);

	}
	public void stop(){
		status = STATUS_STOP;
	}
	private boolean reachEnd;
	public void reachEend(){
		this.reachEnd = true;
	}

	@Override
	public boolean next() throws ESDataImportException {
		if( pos < size){
			record = buildRecord(records.get(pos));
			pos ++;
			return true;
		}
		else{
			if(status == STATUS_STOP){
				return false;
			}
			try {

				T datas = queue.poll(1000, TimeUnit.MILLISECONDS);
				if(status == STATUS_STOP){
					return false;
				}
				if(datas != null){
					this.records = datas.getDatas();
					size = records != null ? records.size():0;
				}
				if(datas == null || size == 0)
				{

					do{
						datas = queue.poll(1000, TimeUnit.MILLISECONDS);
						if(status == STATUS_STOP ){
							return false;
						}
						if(datas == null){
							if(reachEnd)
								break;
							continue;
						}
						this.records = datas.getDatas();
						size = records != null ? records.size():0;
						if(size > 0)
							break;
					}while (true);
					if(datas == null && reachEnd){
						return false;
					}
				}

				pos = 0;
				record = buildRecord(records.get(pos));
				pos ++;
				return true;
			} catch (InterruptedException e) {
				return false;
			}
		}
	}

	@Override
	public TranMeta getMetaData() {
		return new DefaultTranMetaData(record.getKeys());
	}
}
