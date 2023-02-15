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
import org.frameworkset.util.concurrent.ThreadPoolFactory;
import org.frameworkset.util.shutdown.ShutdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private BulkCommand bulkCommand ;
	private long lastAppendDataTime = -1;
	private Lock r = new ReentrantLock();
	private Lock w = new ReentrantLock();
	public BulkConfig getBulkConfig() {
		return bulkConfig;
	}

	private BulkCommand buildBulkCommand(){

		return new BulkCommand(this);
	}

	private BulkConfig bulkConfig;
	private Flush flush;
	private ExecutorService executor ;

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
	private  void stop(){
		this.status = 1;
		synchronized (flush) {
			flush.notify();
		}
	}
	public String getRefreshOption() {
		return bulkConfig.getRefreshOption();
	}
	public BulkProcessor(BulkConfig bulkConfig){
		this.bulkConfig = bulkConfig;
	}

	public synchronized void increamentTotalsize(int totalSize){
		r.lock();
		try {

			this.totalSize = this.totalSize + totalSize;
		}
		finally {
			r.unlock();
		}

	}
	public void init(){
		if(inited)
			return;
        synchronized (this) {
            if(inited)
                return;

            clientInterface = ElasticSearchHelper.getRestClientUtil(bulkConfig.getElasticsearch());
            this.bulkCommand = buildBulkCommand();
            executor = ThreadPoolFactory.buildThreadPool(bulkConfig.getBulkProcessorName(), bulkConfig.getBulkRejectMessage(),
                    this.bulkConfig.getWorkThreads(), this.bulkConfig.getWorkThreadQueue(),
                    this.bulkConfig.getBlockedWaitTimeout()
                    , this.bulkConfig.getWarnMultsRejects());
//		dataQueue =  new ArrayBlockingQueue<BulkData>(bulkConfig.getBulkQueue());
            if (bulkConfig.getFlushInterval() > 0) {
                flush = new Flush("Elasticsearch[" + (bulkConfig.getElasticsearch() != null ? bulkConfig.getElasticsearch() : "default") + "]-" + bulkConfig.getBulkProcessorName() + "-flush-thread");
                flush.start();
            }
            ShutdownUtil.addShutdownHook(new Runnable() {
                @Override
                public void run() {
                    shutDown();
                }
            });
            this.inited = true;
        }
	}

	private boolean touchBatchSize(){

		if(this.bulkCommand != null && this.bulkCommand.getBulkDataSize() >= bulkConfig.getBulkSizes()){
			return true;
		}
		else{
			return false;
		}
	}

	public long getLastAppendDataTime(){
		return lastAppendDataTime;
	}
	private void appendBulkData(BulkData bulkData){
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}
//			lastAppendDataTime = System.currentTimeMillis();
//			this.bulkCommand.addBulkData(bulkData);
//			if(this.touchBatchSize()){
//				this.execute(true);
//			}

			_appendBulkData( bulkData);


		}
		finally {
			w.unlock();
		}
	}

	private void _appendBulkData(BulkData bulkData){
		lastAppendDataTime = System.currentTimeMillis();
		this.bulkCommand.addBulkData(bulkData);
		if(this.touchBatchSize()){
			this.execute(true);
		}
	}

	private void forceFlush(long flushInterval){
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}

			long interval = System.currentTimeMillis() - lastAppendDataTime;
			if (interval > flushInterval && bulkCommand.getBulkDataSize() > 0) {
				execute(true);
			}

		}
		finally {
			w.unlock();
		}
	}

	private void forceExecute(){
		w.lock();
		try {


			if (bulkCommand !=null && bulkCommand.getBulkDataSize() > 0) {
				execute(false);
			}

		}
		finally {
			w.unlock();
		}
	}
	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data
	 * @param clientOptions Object中有@ESId指定的文档id字段或者clientOptions设置了esidfield,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertData(String index,String indexType,Object data,ClientOptions clientOptions){
            init();
			assertShutdown();
			BulkData bulkData = new BulkData(BulkData.INSERT,data);
			bulkData.setIndex(index);
			bulkData.setIndexType(indexType);
			bulkData.setClientOptions(clientOptions);
			appendBulkData( bulkData);
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data Object中有@ESId指定的文档id字段,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertData(String index,String indexType,Object data){
        init();
		assertShutdown();
		BulkData bulkData = new BulkData(BulkData.INSERT,data);
		bulkData.setIndex(index);
		bulkData.setIndexType(indexType);
		appendBulkData( bulkData);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data
	 * @param clientOptions Object中有@ESId指定的文档id字段或者clientOptions设置了esidfield,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertData(String index,Object data,ClientOptions clientOptions){
		insertData( index,(String)null, data,clientOptions);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data Object中有@ESId指定的文档id字段,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertData(String index,Object data){
		insertData( index,(String)null, data,null);
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data
	 * @param updateOptions Object中有@ESId指定的文档id字段或者clientOptions设置了esidfield
	 */
	public void updateData(String index, String indexType, Object data, ClientOptions updateOptions){
//		try {
        init();
        assertShutdown();
        BulkData bulkData = new BulkData(BulkData.UPDATE,data);
        bulkData.setIndex(index);
        bulkData.setIndexType(indexType);
        bulkData.setClientOptions(updateOptions);
//			this.dataQueue.put(bulkData);
        appendBulkData( bulkData);
//		} catch (InterruptedException e) {
//			logger.info("InterruptedException");
//		}
	}

	 /**
	  *  ES 1.x,2.x,5.x,6.x,7.x,+
			* @param index
	 * @param indexType
	 * @param data Object中有@ESId指定的文档id字段
	 */
	public void updateData(String index, String indexType, Object data){
//		try {
        init();
		assertShutdown();
		BulkData bulkData = new BulkData(BulkData.UPDATE,data);
		bulkData.setIndex(index);
		bulkData.setIndexType(indexType);
//		bulkData.setClientOptions(updateOptions);
//			this.dataQueue.put(bulkData);
		appendBulkData( bulkData);
//		} catch (InterruptedException e) {
//			logger.info("InterruptedException");
//		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data Object中必须要要有@ESId指定的文档id字段
	 *
	 */
	public void updateData(String index,  Object data){
		updateData(  index,   null, data,null);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param data
	 * @param updateOptions Object中有@ESId指定的文档id字段或者clientOptions设置了esidfield
	 */
	public void updateData(String index,  Object data, ClientOptions updateOptions){
		updateData(  index, (String)null,  data,  updateOptions);
	}



	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data 待删除的文档_id
	 * @param updateOptions
	 */
	public void deleteData(String index,String indexType,Object data, ClientOptions updateOptions){
//		try {
        init();
        assertShutdown();
        BulkData bulkData = new BulkData(BulkData.DELETE,data);
        bulkData.setIndex(index);
        bulkData.setIndexType(indexType);
        bulkData.setClientOptions(updateOptions);
        appendBulkData( bulkData);
//			bulkCommand.addBulkData(bulkData);
//			this.dataQueue.put(bulkData);
//		} catch (InterruptedException e) {
//			logger.info("InterruptedException");
//		}
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param data 待删除的文档_id
	 */
	public void deleteData(String index,String indexType,Object data){
//		try {
        init();
		assertShutdown();
		BulkData bulkData = new BulkData(BulkData.DELETE,data);
		bulkData.setIndex(index);
		bulkData.setIndexType(indexType);
//		bulkData.setClientOptions(updateOptions);
		appendBulkData( bulkData);
//			bulkCommand.addBulkData(bulkData);
//			this.dataQueue.put(bulkData);
//		} catch (InterruptedException e) {
//			logger.info("InterruptedException");
//		}
	}

	private void execute(boolean initBuilCommand){
		executor.submit(bulkCommand);
		if(initBuilCommand)
			bulkCommand = this.buildBulkCommand();
		else{
			bulkCommand = null;
		}
	}
	/**
	 * ES 7.x,+
	 * @param index
	 * @param data 待删除的文档_id
	 * @param updateOptions
	 */
	public void deleteDataWithClientOptions(String index,Object data, ClientOptions updateOptions){
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
	 * @param clientOptions Object中有@ESId指定的文档id字段或者clientOptions设置了esidfield,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertDatas(String index,String indexType,List<?> datas,ClientOptions clientOptions){
        init();
		if(datas == null || datas.size() == 0)
			return;
		assertShutdown();
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}
			for(Object data:datas) {
				BulkData bulkData = new BulkData(BulkData.INSERT, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
				bulkData.setClientOptions(clientOptions);
				_appendBulkData( bulkData);
			}

		}
		finally {
			w.unlock();
		}

	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas Object中有@ESId指定的文档id字段,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertDatas(String index,String indexType,List<?> datas){
        init();
		if(datas == null || datas.size() == 0)
			return;
		assertShutdown();
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}
			for(Object data:datas) {
				BulkData bulkData = new BulkData(BulkData.INSERT, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
//				bulkData.setClientOptions(clientOptions);
				_appendBulkData( bulkData);
			}

		}
		finally {
			w.unlock();
		}

	}


	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas
	 * @param clientOptions Object中有@ESId指定的文档id字段或者clientOptions设置了esidfield,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertDatas(String index,List<?> datas,ClientOptions clientOptions){
		insertDatas( index,(String)null, datas,clientOptions);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas Object中有@ESId指定的文档id字段,则根据id字段值设置docid,否则自动生成文档id
	 */
	public void insertDatas(String index,List<?> datas){
		insertDatas( index,(String)null, datas,null);
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas
	 * @param updateOptions Object中有@ESId指定的文档id字段或者updateOptions设置了esidfield
	 */
	public void updateDatas(String index, String indexType, List<?> datas, ClientOptions updateOptions){
        init();
		if(datas == null || datas.size() == 0)
			return;
		assertShutdown();
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}
			for(Object data:datas) {
				BulkData bulkData = new BulkData(BulkData.UPDATE, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
				bulkData.setClientOptions(updateOptions);
				_appendBulkData( bulkData);
			}

		}
		finally {
			w.unlock();
		}
	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas  Object中必须要有@ESId注解设置的文档id信息
	 */
	public void updateDatas(String index, String indexType, List<?> datas){
        init();
		if(datas == null || datas.size() == 0)
			return;
		assertShutdown();
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}
			for(Object data:datas) {
				BulkData bulkData = new BulkData(BulkData.UPDATE, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
//				bulkData.setClientOptions(updateOptions);
				_appendBulkData( bulkData);
			}

		}
		finally {
			w.unlock();
		}
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas Object中必须要要有@ESId指定的文档id字段
	 */
	public void updateDatas(String index,  List<?> datas){
		updateDatas(  index,   (String)null, datas,null);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas
	 * @param updateOptions Object中有@ESId指定的文档id字段或者updateOptions设置了esidfield
	 */
	public void updateDatas(String index, List<?> datas, ClientOptions updateOptions){
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
	public void deleteDatas(String index,String indexType,List<?> datas, ClientOptions updateOptions){
        init();
		if(datas == null || datas.size() == 0){
			return ;
		}
		assertShutdown();
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}
			for(Object data :datas) {
				BulkData bulkData = new BulkData(BulkData.DELETE, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
				bulkData.setClientOptions(updateOptions);
				_appendBulkData( bulkData);
			}
		}
		finally {
			w.unlock();
		}

	}

	/**
	 * ES 1.x,2.x,5.x,6.x,7.x,+
	 * @param index
	 * @param indexType
	 * @param datas 待删除的文档_id集合
	 */
	public void deleteDatas(String index,String indexType,List<?> datas){
        init();
		if(datas == null || datas.size() == 0){
			return ;
		}
		assertShutdown();
		w.lock();
		try {

			if(bulkCommand == null){
				return;
			}
			for(Object data :datas) {
				BulkData bulkData = new BulkData(BulkData.DELETE, data);
				bulkData.setIndex(index);
				bulkData.setIndexType(indexType);
//				bulkData.setClientOptions(updateOptions);
				_appendBulkData( bulkData);
			}
		}
		finally {
			w.unlock();
		}

	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas 待删除的文档_id集合
	 * @param updateOptions
	 */
	public void deleteDatas(String index,List<?> datas, ClientOptions updateOptions){
		deleteDatas(  index,(String)null,  datas,  updateOptions);
	}

	/**
	 * ES 7.x,+
	 * @param index
	 * @param datas 待删除的文档_id集合
	 */
	public void deleteDatas(String index, List<?> datas){
		deleteDatas(  index,(String)null,  datas,  (ClientOptions)null);
	}

	public long getFailedSize() {
		return failedSize;
	}


	private long totalSize = 0;
	private long failedSize = 0;
	public long getTotalSize() {
		return totalSize;
	}
	public   void increamentFailedSize(int failedSize){
		r.lock();
		try {

			this.failedSize = this.failedSize + failedSize;
		}
		finally {
			r.unlock();
		}
	}

	public int getBulkSizes() {
		return bulkConfig.getBulkSizes();
	}

	/**
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
*/
	class Flush extends Thread{
		public Flush(String name){
			super(name);
		}

		@Override
		public void run() {
			long flushInterval = bulkConfig.getFlushInterval();

			while(true) {

				try {
					synchronized (this) {
						wait(flushInterval);
					}
					forceFlush(flushInterval);
					if(!isShutdown()) {
						continue;
					}
					else
					{

						break;
					}



				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}
    private boolean shutdown;
	/**
	 * 调用shutDown停止方法后，BulkProcessor不会接收新的请求，但是会处理完所有已经进入bulk队列的数据
	 */
	public  void shutDown(){
        if(shutdown)
            return;
        synchronized(this) {
            if(shutdown)
                return;
            shutdown = true;
            if (!inited) {
                return;
            }
        }

		if(logger.isInfoEnabled())
			logger.info("ShutDown BulkProcessor[{}] begin.....",this.bulkConfig.getBulkProcessorName());
		stop();
//		try{
//			if(flush != null){
//				flush.interrupt();
//			}
//		}
//		catch (Exception e){
//
//		}
		try {
			this.forceExecute();
		}
		catch (Throwable e){

		}
		if(executor != null){
			try {
				if(logger.isInfoEnabled())
					logger.info("ShutDown BulkProcessor["+this.bulkConfig.getBulkProcessorName()+"] thread executor pool  begin......");
				executor.shutdown();
//				if(logger.isInfoEnabled()){
//					logger.info("BulkProcessor process total success records {} failed records {}.",totalSize,failedSize);
//				}
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
