package org.frameworkset.elasticsearch.scroll;
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

import org.frameworkset.elasticsearch.entity.ESDatas;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/4 12:55
 * @author biaoping.yin
 * @version 1.0
 */
public class ParallelSliceScrollResult<T> implements SliceScrollResultInf<T> {
	//用来存放实际slice检索总记录数
	private volatile long realTotalSize = 0L;
	private ESDatas<T> sliceResponse;
	private Lock lockIncrementSize = new ReentrantLock();
	private Lock lockSetSliceResponse = new ReentrantLock();
	private ScrollHandler<T> scrollHandler;
	private boolean useDefaultScrollHandler = false;

	public ParallelSliceScrollResult(){
	}

	//辅助方法，用来累计每次scroll获取到的记录数
	public void incrementSize(int size){
		try {
			lockIncrementSize.lock();
			this.realTotalSize = this.realTotalSize + size;
		}
		finally {
			lockIncrementSize.unlock();
		}
	}

	public ESDatas<T> getSliceResponse() {
		return sliceResponse;
	}
	public void complete(){
		if(sliceResponse == null){
			return;
		}
		this.sliceResponse.setTotalSize(this.realTotalSize);
		if(!useDefaultScrollHandler)//结果自行处理，所以清空默认结果
			this.sliceResponse.setDatas(null);
	}

	public void setSliceResponse(ESDatas<T> sliceResponse) {
		if(this.sliceResponse != null)
			return;
		try {
			lockSetSliceResponse.lock();
			if(this.sliceResponse != null)
				return;
			this.sliceResponse = sliceResponse;
		}
		finally {
			lockSetSliceResponse.unlock();
		}

	}

	public long getRealTotalSize(){
		return this.realTotalSize;
	}

	public ScrollHandler<T> getScrollHandler() {
		return scrollHandler;
	}
	public ScrollHandler<T> setScrollHandler(ScrollHandler<T> scrollHandler){
		this.scrollHandler = scrollHandler;
		if(scrollHandler instanceof DefualtScrollHandler){
			useDefaultScrollHandler = true;
		}
		return this.scrollHandler;
	}
	public ScrollHandler<T> setScrollHandler(ESDatas<T> sliceResponse,HandlerInfo handlerInfo) throws Exception {
		if (this.scrollHandler != null) {
			this.scrollHandler.handle(sliceResponse,  handlerInfo);
			return this.scrollHandler;
		}
		boolean inited = false;
		try {
			lockSetSliceResponse.lock();
			if (this.scrollHandler != null) {
				inited = true;
			}
			else{
				useDefaultScrollHandler = true;
				this.sliceResponse = sliceResponse;
				this.scrollHandler = new ParallelSliceScrollHandler<T>(sliceResponse);
				return this.scrollHandler;
			}

		}
		finally {
			lockSetSliceResponse.unlock();
		}
		if(inited){
			this.scrollHandler.handle(sliceResponse,  handlerInfo);
		}
		return this.scrollHandler;

	}
}
