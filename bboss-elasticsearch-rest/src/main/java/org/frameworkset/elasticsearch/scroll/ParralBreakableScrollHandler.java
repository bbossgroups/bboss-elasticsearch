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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>Description: 可以中断并行scroll查询的处理器</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/4 11:45
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class ParralBreakableScrollHandler<T> implements BreakableScrollHandler, ScrollHandler<T>{
	private boolean breaked ;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock r = lock.readLock();
	private Lock w = lock.writeLock();
	/**
	 * 是否中断scroll查询
	 * @return
	 */
	public boolean isBreaked(){
		try {
			r.lock();
			return breaked;
		}
		finally {
			r.unlock();
		}

	}
	public void setBreaked(boolean breaked){
		try {
			w.lock();
			this.breaked = breaked;
		}
		finally {
			w.unlock();
		}

	}

//	/**
//	 * 更加错误异常信息，判断是否在出错的情况下继续进行数据处理，全局配置
//	 * @return
//	 */
//	public boolean isContinueOneError(Throwable throwable);
}
