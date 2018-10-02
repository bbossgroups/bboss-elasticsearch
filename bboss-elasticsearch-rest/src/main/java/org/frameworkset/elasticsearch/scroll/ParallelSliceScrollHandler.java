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

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/4 12:33
 * @author biaoping.yin
 * @version 1.0
 */
public class ParallelSliceScrollHandler<T> extends DefualtSliceScrollHandler<T> {
	private Lock lockSetSliceResponse = new ReentrantLock();
	public ParallelSliceScrollHandler(ESDatas<T> firstResponse){
		super(firstResponse);
	}
	@Override
	public void handle(ESDatas<T> response) throws Exception {

		List<T> datas = firstResponse.getDatas();
		if(datas != null) {
			try {
				lockSetSliceResponse.lock();
				datas.addAll(response.getDatas());
			} finally {
				lockSetSliceResponse.unlock();
			}
		}
	}
}
