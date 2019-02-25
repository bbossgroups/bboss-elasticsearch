package org.frameworkset.elasticsearch.scroll.thread;
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

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/4 15:42
 * @author biaoping.yin
 * @version 1.0
 */
public class ThreadPoolFactory {
	public static ExecutorService buildSliceScrollThreadPool(int sliceScrollThreadCount,int sliceScrollThreadQueue,long sliceScrollBlockedWaitTimeout){
//		ExecutorService executor = Executors.newFixedThreadPool(this.getThreadCount(), new ThreadFactory() {
//			@Override
//			public Thread newThread(Runnable r) {
//				return new DBESThread(r);
//			}
//		});

		ExecutorService blockedExecutor = new ThreadPoolExecutor(sliceScrollThreadCount, sliceScrollThreadCount,
				0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(sliceScrollThreadQueue),
				new ThreadFactory() {
					private java.util.concurrent.atomic.AtomicInteger threadCount = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						int num = threadCount.incrementAndGet();
						return new ESSliceScrollThread(r,num);
					}
				},new BlockedTaskRejectedExecutionHandler("Slice Scroll Query",  sliceScrollBlockedWaitTimeout));
		return blockedExecutor;
	}

	public static ExecutorService buildScrollThreadPool(int scrollThreadCount,int scrollThreadQueue,long scrollBlockedWaitTimeout){
//		ExecutorService executor = Executors.newFixedThreadPool(this.getThreadCount(), new ThreadFactory() {
//			@Override
//			public Thread newThread(Runnable r) {
//				return new DBESThread(r);
//			}
//		});

		ExecutorService blockedExecutor = new ThreadPoolExecutor(scrollThreadCount, scrollThreadCount,
				0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(scrollThreadQueue),
				new ThreadFactory() {
					private AtomicInteger threadCount = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						int num = threadCount.incrementAndGet();
						return new ESScrollThread(r,num);
					}
				},new BlockedTaskRejectedExecutionHandler( "Scroll Query", scrollBlockedWaitTimeout));
		return blockedExecutor;
	}
}
