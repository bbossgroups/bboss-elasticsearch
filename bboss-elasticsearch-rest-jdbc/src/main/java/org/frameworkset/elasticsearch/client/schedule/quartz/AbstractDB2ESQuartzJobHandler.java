package org.frameworkset.elasticsearch.client.schedule.quartz;
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

import org.frameworkset.elasticsearch.client.schedule.ExternalScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/4/20 22:51
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class AbstractDB2ESQuartzJobHandler  {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected ExternalScheduler externalScheduler;
	private Lock lock = new ReentrantLock();
	public abstract void init();
	public void execute(){
		try {
			lock.lock();
			externalScheduler.execute(null);

		}
		finally {
			lock.unlock();
		}
	}

	public void destroy(){
		if(externalScheduler != null){
			externalScheduler.destroy();
		}
	}
}
