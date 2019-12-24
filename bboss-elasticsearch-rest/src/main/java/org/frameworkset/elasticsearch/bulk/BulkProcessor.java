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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger logger = LoggerFactory.getLogger(BulkProcessor.class);
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
		worker = new Thread(new Worker(),bulkConfig.getBulkProcessorName()+"-bulkdataqueue-handle-work");
		worker.start();
		BaseApplicationContext.addShutdownHook(new Runnable() {
			@Override
			public void run() {
				shutDown();
			}
		});
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data
	 * @param clientOptions
	 */
	public void insertData(String index,String indexType,Object data,ClientOptions clientOptions){
		try {
			assertShutdown();
			BulkData bulkData = new BulkData(BulkData.INSERT,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(clientOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {
			logger.info("InterruptedException");
		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data
	 * @param clientOptions
	 */
	public void insertData(String index,Object data,ClientOptions clientOptions){
		insertData( index,(String)null, data,clientOptions);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data
	 */
	public void insertData(String index,Object data){
		insertData( index,(String)null, data,null);
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data
	 * @param updateOptions
	 */
	public void updateData(String index, String indexType, Object data, ClientOptions updateOptions){
		try {
			assertShutdown();
			BulkData bulkData = new BulkData(BulkData.UPDATE,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {
			logger.info("InterruptedException");
		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data
	 */
	public void updateData(String index,  Object data){
		updateData(  index,   null, data,null);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data
	 * @param updateOptions
	 */
	public void updateData(String index,  Object data, ClientOptions updateOptions){
		updateData(  index, (String)null,  data,  updateOptions);
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data
	 * @param updateOptions
	 */
	public void deleteData(String index,String indexType,Object data, ClientOptions updateOptions){
		try {
			assertShutdown();
			BulkData bulkData = new BulkData(BulkData.DELETE,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {
			logger.info("InterruptedException");
		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data 待删除的文档_id
	 * @param updateOptions
	 */
	public void deleteData(String index,Object data, ClientOptions updateOptions){
		deleteData(  index,(String)null,  data,  updateOptions);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data 待删除的文档_id
	 */
	public void deleteData(String index, Object data){
		deleteData(  index,(String)null,  data,  (ClientOptions)null);
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas
	 * @param clientOptions
	 */
	public void insertDatas(String index,String indexType,List<Object> datas,ClientOptions clientOptions){
		if(datas == null || datas.size() == 0)
			return;
		try {
			assertShutdown();
			for(Object data:datas) {
				BulkData bulkData = new BulkData(BulkData.INSERT, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
				bulkData.setClientOptions(clientOptions);
				this.dataQueue.put(bulkData);
			}
		} catch (InterruptedException e) {
			logger.info("InterruptedException");
		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas
	 * @param clientOptions
	 */
	public void insertDatas(String index,List<Object> datas,ClientOptions clientOptions){
		insertDatas( index,(String)null, datas,clientOptions);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas
	 */
	public void insertDatas(String index,List<Object> datas){
		insertDatas( index,(String)null, datas,null);
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas
	 * @param updateOptions
	 */
	public void updateDatas(String index, String indexType, List<Object> datas, ClientOptions updateOptions){
		if(datas == null || datas.size() == 0)
			return;
		try {
			assertShutdown();
			for(Object data:datas) {
				BulkData bulkData = new BulkData(BulkData.UPDATE, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
				bulkData.setClientOptions(updateOptions);
				this.dataQueue.put(bulkData);
			}
		} catch (InterruptedException e) {
			logger.info("InterruptedException");
		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas
	 */
	public void updateDatas(String index,  List<Object> datas){
		updateDatas(  index,   (String)null, datas,null);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas
	 * @param updateOptions
	 */
	public void updateDatas(String index, List<Object> datas, ClientOptions updateOptions){
		updateDatas(  index, (String)null,  datas,  updateOptions);
	}

	public boolean isShutdown(){
		return this.status == 1;
	}

	public void assertShutdown(){
		if(isShutdown())
			throw new BulkProcessorException("Bulk processor is Shutdown.");
	}
	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas 待删除的文档_id集合
	 * @param updateOptions
	 */
	public void deleteDatas(String index,String indexType,List<Object> datas, ClientOptions updateOptions){
		if(datas == null || datas.size() == 0){
			return ;
		}
		try {
			assertShutdown();
			for(Object data :datas) {
				BulkData bulkData = new BulkData(BulkData.DELETE, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
				bulkData.setClientOptions(updateOptions);
				this.dataQueue.put(bulkData);
			}
		} catch (InterruptedException e) {
			logger.info("InterruptedException");
		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas 待删除的文档_id集合
	 * @param updateOptions
	 */
	public void deleteDatas(String index,List<Object> datas, ClientOptions updateOptions){
		deleteDatas(  index,(String)null,  datas,  updateOptions);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas 待删除的文档_id集合
	 */
	public void deleteDatas(String index, List<Object> datas){
		deleteDatas(  index,(String)null,  datas,  (ClientOptions)null);
	}

	static class IntegerWraper{
		int j = 0;
		public IntegerWraper(){

		}
		public int increment(){
			j ++;
			return j;
		}
		public void reset(){
			j = 0;
		}
		public int get(){
			return j;
		}
	}

	public boolean isQueueEmpty() {
		return queueEmpty;
	}

	private boolean queueEmpty;
	long totalSize = 0;

	public long getTotalSize() {
		return totalSize;
	}

	class Worker implements Runnable{

		@Override
		public void run() {
			long pollStartTime = System.currentTimeMillis();
			List<BulkData> batchBulkDatas = null;
			BulkCommand bulkCommand = null;

			int dataSize = 0;
			do {
				try {

					BulkData bulkData = dataQueue.poll(bulkConfig.getPollTimeOut(), TimeUnit.MILLISECONDS);
					if (bulkData != null) {
						pollStartTime = System.currentTimeMillis();
						if(batchBulkDatas == null){
							batchBulkDatas = new ArrayList<BulkData>();
						}
						dataSize ++;//实际记录大小，因为bulkData为collection时，数据大小应该以collection大小之和
						totalSize ++;
						batchBulkDatas.add(bulkData);
						if(dataSize >= bulkConfig.getBulkSizes()){
							bulkCommand = new BulkCommand(batchBulkDatas,BulkProcessor.this);
							batchBulkDatas = null;
							dataSize = 0;
							executor.submit(bulkCommand);
						}
					}
					else{
						boolean forceFlush = false;
						if (bulkConfig.getFlushInterval() > 0) {
							long interval = System.currentTimeMillis() - pollStartTime;
							if (interval > bulkConfig.getFlushInterval()) {
								forceFlush = true;
							}
						}
						if (isShutdown()) {
							forceFlush = true;
						}

						// force flush
						if(forceFlush && dataSize > 0){
							bulkCommand = new BulkCommand(batchBulkDatas,BulkProcessor.this);
							batchBulkDatas = null;
							dataSize = 0;
							pollStartTime = System.currentTimeMillis();
							executor.submit(bulkCommand);
						}
						if(!isShutdown())
							continue;
						else {
							if(logger.isInfoEnabled()){
								logger.info("BulkProcessor process total {} data requests.",totalSize);
							}
							queueEmpty = true;
							synchronized (BulkProcessor.this){
								BulkProcessor.this.notifyAll();
							}

							break;
						}
					}


				} catch (InterruptedException e) {
					break;
				}
			}while(true);
		}
	}

	/**
	 * 调用shutDown停止方法后，BulkProcessor不会接收新的请求，但是会处理完所有已经进入bulk队列的数据
	 */
	public void shutDown(){
		if(logger.isInfoEnabled())
			logger.info("ShutDown BulkProcessor[{}] begin.....",this.bulkConfig.getBulkProcessorName());
		stop();
		while(true) {
			if(!queueEmpty) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {

				}
			}
			else{
				break;
			}

		}

		if(executor != null){
			try {
				if(logger.isInfoEnabled())
					logger.info("ShutDown BulkProcessor["+this.bulkConfig.getBulkProcessorName()+"] thread executor pool  begin......");
				executor.shutdown();
				if(logger.isInfoEnabled())
					logger.info("ShutDown BulkProcessor["+this.bulkConfig.getBulkProcessorName()+"] thread executor pool complete.");
			}
			catch(Exception e){
				if(logger.isErrorEnabled())
					logger.error("ShutDown BulkProcessor["+this.bulkConfig.getBulkProcessorName()+"] thread executor pool failed:",e);
			}
		}
		if(logger.isInfoEnabled())
			logger.info("ShutDown BulkProcessor[{}] complete.",this.bulkConfig.getBulkProcessorName());

	}

}
