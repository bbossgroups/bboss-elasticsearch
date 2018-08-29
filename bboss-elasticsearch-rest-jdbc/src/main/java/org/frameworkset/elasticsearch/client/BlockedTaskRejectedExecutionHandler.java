package org.frameworkset.elasticsearch.client;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/8/29 21:39
 * @author biaoping.yin
 * @version 1.0
 */
public class BlockedTaskRejectedExecutionHandler implements RejectedExecutionHandler {
	private static Logger logger = LoggerFactory.getLogger(BlockedTaskRejectedExecutionHandler.class);
	private AtomicInteger rejectCounts;
	public BlockedTaskRejectedExecutionHandler(AtomicInteger rejectCounts){
		this.rejectCounts = rejectCounts;
	}

	/**
	 * Always log per 1000 mults rejects.
	 * @param r
	 * @param executor
	 */
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		int counts = rejectCounts.incrementAndGet();
		if(logger.isInfoEnabled()) {
			int t = counts % 1000;
			if (t == 0) {
				logger.info(new StringBuilder().append("Task[Import DB Data to Elasticsearch] rejected  ").append(counts).append(" times.").toString());
			}
		}
//
		try {
			executor.getQueue().put(r);
		} catch (InterruptedException e1) {
			throw new RejectedExecutionException(e1);
		}
	}
}
