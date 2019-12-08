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
		worker = new Thread(new Worker(),bulkConfig.getBulkProcessorName()+"-bulkdataqueue-handle-work");
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

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data
	 * @param clientOptions
	 */
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
			BulkData bulkData = new BulkData(BulkData.UPDATE,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

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
			BulkData bulkData = new BulkData(BulkData.DELETE,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

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
		try {
			BulkData bulkData = new BulkData(BulkData.INSERT,datas);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(clientOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

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
		try {
			BulkData bulkData = new BulkData(BulkData.UPDATE,datas);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(updateOptions);
			this.dataQueue.put(bulkData);
		} catch (InterruptedException e) {

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

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas 待删除的文档_id集合
	 * @param updateOptions
	 */
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
	private void handleCollection(BulkData bulkData,List<BulkData> commandBulkDatas,IntegerWraper j,List<BulkCommand> bulkCommands ){
		BulkCommand bulkCommand = null;
		if(bulkData.isCollection()){
			List<Object> datas = bulkData.getDatas();
			BulkData innerBulkData = null;
			for(int k = 0; k < datas.size(); k ++){
				if(j.get() < bulkConfig.getBulkSizes()) {
					innerBulkData = new BulkData(bulkData.getType(), datas.get(k));
					innerBulkData.setClientOptions(bulkData.getClientOptions());
					innerBulkData.setIndex(bulkData.getIndex());
					innerBulkData.setIndexType(bulkData.getIndexType());
					commandBulkDatas.add(innerBulkData);
					j.increment();
				}
				else{
					bulkCommand = new BulkCommand(commandBulkDatas,this);
					bulkCommands.add(bulkCommand);
					j.reset();
					commandBulkDatas = new ArrayList<BulkData>(bulkConfig.getBulkSizes());
					innerBulkData = new BulkData(bulkData.getType(), datas.get(k));
					innerBulkData.setClientOptions(bulkData.getClientOptions());
					innerBulkData.setIndex(bulkData.getIndex());
					innerBulkData.setIndexType(bulkData.getIndexType());
					commandBulkDatas.add(innerBulkData);
					j.increment();
				}
			}
		}
		else{
			commandBulkDatas.add(bulkData);
			j.increment();
		}

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
	private List<BulkCommand> buildBulkCommands(List<BulkData> batchBulkDatas){
		List<BulkCommand> bulkCommands = new ArrayList<BulkCommand>();
		List<BulkData> commandBulkDatas = null;
		BulkCommand bulkCommand = null;
		BulkData bulkData = null;
		IntegerWraper j = new IntegerWraper();
		for(int i = 0; i < batchBulkDatas.size(); i ++) {
			if(commandBulkDatas == null)
				commandBulkDatas = new ArrayList<BulkData>(bulkConfig.getBulkSizes());
			bulkData = batchBulkDatas.get(i);
			if(j.get() < bulkConfig.getBulkSizes()){
				handleCollection(  bulkData, commandBulkDatas,  j, bulkCommands );
			}
			else{
				bulkCommand = new BulkCommand(commandBulkDatas,this);
				bulkCommands.add(bulkCommand);
				j.reset();
				commandBulkDatas = new ArrayList<BulkData>(bulkConfig.getBulkSizes());
				handleCollection(  bulkData, commandBulkDatas,  j, bulkCommands );
			}

		}
		if(j.get() > 0){
			bulkCommand = new BulkCommand(commandBulkDatas,this);
			bulkCommands.add(bulkCommand);
		}
		return bulkCommands;
	}

	class Worker implements Runnable{

		@Override
		public void run() {
			long pollStartTime = System.currentTimeMillis();
			List<BulkData> batchBulkDatas = null;
			BulkCommand bulkCommand = null;
			long totalSize = 0;
			int dataSize = 0;
			do {
				try {

					BulkData bulkData = dataQueue.poll(bulkConfig.getPollTimeOut(), TimeUnit.MILLISECONDS);
					if (status == 1) {
						break;
					}


					if (bulkData != null) {
						pollStartTime = System.currentTimeMillis();
						if(batchBulkDatas == null){
							batchBulkDatas = new ArrayList<BulkData>();
						}
						dataSize = dataSize + bulkData.getDataSize();//实际记录大小，因为bulkData为collection时，数据大小应该以collection大小之和
						totalSize = totalSize + bulkData.getDataSize();
						batchBulkDatas.add(bulkData);
						if(dataSize == bulkConfig.getBulkSizes()){
							bulkCommand = new BulkCommand(batchBulkDatas,BulkProcessor.this);
							batchBulkDatas = null;
							dataSize = 0;

							executor.submit(bulkCommand);

						}
						else if(dataSize > bulkConfig.getBulkSizes()){
							List<BulkCommand> bulkCommands = buildBulkCommands(batchBulkDatas);
							batchBulkDatas.clear();
							batchBulkDatas = null;
							dataSize = 0;
							for(BulkCommand bulkCommand1:bulkCommands){
								executor.submit(bulkCommand1);
							}
						}

					}
					else{
						if (bulkConfig.getFlushInterval() > 0) {
							long interval = System.currentTimeMillis() - pollStartTime;
							if (interval > bulkConfig.getFlushInterval()) {
								// force flush
								if(dataSize > 0){
									bulkCommand = new BulkCommand(batchBulkDatas,BulkProcessor.this);
									batchBulkDatas = null;
									dataSize = 0;
									pollStartTime = System.currentTimeMillis();
									executor.submit(bulkCommand);
								}
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
