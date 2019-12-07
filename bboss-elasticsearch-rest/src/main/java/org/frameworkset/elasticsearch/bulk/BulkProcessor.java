package org.frameworkset.elasticsearch.bulk;
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

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ClientOptions;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.util.concurrent.ThreadPoolFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/12/4 14:13
 * @author biaoping.yin
 * @version 1.0
 */
public class BulkProcessor {
	public BulkConfig getBulkConfig() {
		return bulkConfig;
	}

	private BulkConfig bulkConfig;
	private Thread worker;
	private ExecutorService executor ;
	private BlockingQueue<BulkData> dataQueue ;

	public ClientInterface getClientInterface() {
		return clientInterface;
	}

	private ClientInterface clientInterface;
	private boolean inited;
	/**
	 * 0 正常
	 * 1 stop;
	 */
	private int status;
	private void stop(){
		this.status = 1;
	}
	public String getRefreshOption() {
		return bulkConfig.getRefreshOption();
	}
	public BulkProcessor(BulkConfig bulkConfig){
		this.bulkConfig = bulkConfig;
	}
	public void init(){
		if(inited)
			return;
		this.inited = true;
		clientInterface = ElasticSearchHelper.getRestClientUtil(bulkConfig.getElasticsearch());
		executor = ThreadPoolFactory.buildThreadPool(bulkConfig.getBulkProcessorName(),bulkConfig.getBulkRejectMessage(),
													this.bulkConfig.getWorkThreads(),this.bulkConfig.getWorkThreadQueue(),
													this.bulkConfig.getBlockedWaitTimeout()
													,this.bulkConfig.getWarnMultsRejects());
		dataQueue =  new ArrayBlockingQueue<BulkData>(bulkConfig.getBulkQueue());
		worker = new Thread(new Worker());
		worker.start();
		BaseApplicationContext.addShutdownHook(new Runnable() {
			@Override
			public void run() {
				shutDown();
			}
		});
	}
	public void shutDown(){
		stop();
		if(worker != null){
			synchronized (worker) {
				worker.interrupt();
			}
		}
		if(executor != null){
			executor.shutdown();
		}

	}
	public void insertData(String index,String indexType,Object data,ClientOptions clientOptions){
		try {
			BulkData bulkData = new BulkData(BulkData.INSERT,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(clientOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

		}
	}
	public void insertData(String index,Object data,ClientOptions clientOptions){
		insertData( index,(String)null, data,clientOptions);
	}

	public void insertData(String index,Object data){
		insertData( index,(String)null, data,null);
	}
	public void updateData(String index, String indexType, Object data, ClientOptions updateOptions){
		try {
			BulkData bulkData = new BulkData(BulkData.UPDATE,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

		}
	}
	public void updateData(String index,  Object data){
		updateData(  index,   null, data,null);
	}
	public void updateData(String index,  Object data, ClientOptions updateOptions){
		updateData(  index, (String)null,  data,  updateOptions);
	}

	public void deleteData(String index,String indexType,Object data, ClientOptions updateOptions){
		try {
			BulkData bulkData = new BulkData(BulkData.DELETE,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

		}
	}
	public void deleteData(String index,Object data, ClientOptions updateOptions){
		deleteData(  index,(String)null,  data,  updateOptions);
	}
	public void deleteData(String index, Object data){
		deleteData(  index,(String)null,  data,  (ClientOptions)null);
	}
	public void insertDatas(String index,String indexType,List<Object> datas,ClientOptions clientOptions){
		try {
			BulkData bulkData = new BulkData(BulkData.INSERT,datas);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(clientOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

		}
	}
	public void insertDatas(String index,List<Object> datas,ClientOptions clientOptions){
		insertDatas( index,(String)null, datas,clientOptions);
	}

	public void insertDatas(String index,List<Object> datas){
		insertDatas( index,(String)null, datas,null);
	}


	public void updateDatas(String index, String indexType, List<Object> datas, ClientOptions updateOptions){
		try {
			BulkData bulkData = new BulkData(BulkData.UPDATE,datas);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

		}
	}
	public void updateDatas(String index,  List<Object> datas){
		updateDatas(  index,   (String)null, datas,null);
	}
	public void updateDatas(String index, List<Object> datas, ClientOptions updateOptions){
		updateDatas(  index, (String)null,  datas,  updateOptions);
	}

	public void deleteDatas(String index,String indexType,List<Object> datas, ClientOptions updateOptions){
		try {
			BulkData bulkData = new BulkData(BulkData.DELETE,datas);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

		}
	}
	public void deleteDatas(String index,List<Object> datas, ClientOptions updateOptions){
		deleteDatas(  index,(String)null,  datas,  updateOptions);
	}
	public void deleteDatas(String index, List<Object> datas){
		deleteDatas(  index,(String)null,  datas,  (ClientOptions)null);
	}


	class Worker implements Runnable{

		@Override
		public void run() {
			long pollStartTime = System.currentTimeMillis();
			List<BulkData> batchBulkDatas = null;
			BulkCommand bulkCommand = null;
			do {
				try {

					BulkData bulkData = dataQueue.poll(bulkConfig.getPollTimeOut(), TimeUnit.MILLISECONDS);
					if (status == 1) {
						break;
					}


					if (bulkData != null) {
						if(batchBulkDatas == null){
							batchBulkDatas = new ArrayList<BulkData>();
						}
						batchBulkDatas.add(bulkData);
						if(batchBulkDatas.size() >= bulkConfig.getBulkSizes()){
							bulkCommand = new BulkCommand(batchBulkDatas,BulkProcessor.this);
							executor.submit(bulkCommand);
							batchBulkDatas = null;
						}
					}
					else{
						if (bulkConfig.getFlushInterval() > 0) {
							long interval = System.currentTimeMillis() - pollStartTime;
							if (interval > bulkConfig.getFlushInterval()) {
								// force flush
								if(batchBulkDatas.size() > 0){
									bulkCommand = new BulkCommand(batchBulkDatas,BulkProcessor.this);
									executor.submit(bulkCommand);
									batchBulkDatas = null;
								}
								pollStartTime = System.currentTimeMillis();
							}
						}
						continue;
					}


				} catch (InterruptedException e) {
					break;
				}
			}while(true);
		}
	}
}
