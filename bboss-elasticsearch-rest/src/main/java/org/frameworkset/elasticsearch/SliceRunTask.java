package org.frameworkset.elasticsearch;
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

import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ExecuteRequestUtil;
import org.frameworkset.elasticsearch.scroll.ParallelSliceScrollResult;
import org.frameworkset.elasticsearch.serial.SerialContext;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/12/23 17:35
 * @author biaoping.yin
 * @version 1.0
 */
public class SliceRunTask<T> implements Runnable {
	private int sliceId;
	private String path;
	private String sliceDsl;
	private   String scroll;
	private   Class<T> type;
	private   ParallelSliceScrollResult sliceScrollResult;
	private ClientInterface restClientUtil;
	private SerialContext serialContext ;
	public SliceRunTask(ClientInterface restClientUtil, int sliceId, String path, String sliceDsl, String scroll, Class<T> type,
						ParallelSliceScrollResult sliceScrollResult, SerialContext serialContext){
		this.restClientUtil = restClientUtil;
		this.sliceId = sliceId;
		this.path = path;
		this.sliceDsl = sliceDsl;
		this.scroll = scroll;
		this.type = type;
		this.sliceScrollResult = sliceScrollResult;
		this.serialContext = serialContext;
	}
	@Override
	public void run() {
		try {
			if(serialContext != null){
				this.serialContext.continueSerialTypes();
			}
			ExecuteRequestUtil._doSliceScroll(restClientUtil.getClient(), sliceId, path,
					sliceDsl,
					scroll, type,
					sliceScrollResult,true);

		} catch (ElasticSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new ElasticSearchException("slice query task["+sliceId+"] failed:",e);
		}
		finally {
			if(serialContext != null){
				this.serialContext.clean();
			}
		}
	}
}
