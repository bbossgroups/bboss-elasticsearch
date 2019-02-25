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

import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.scroll.HandlerInfo;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;
import org.frameworkset.elasticsearch.scroll.SliceScrollResultInf;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/2/25 11:46
 * @author biaoping.yin
 * @version 1.0
 */
public class ScrollTask<T> implements Runnable {
	private ScrollHandler<T> scrollHandler;
	private ESDatas<T> response;
//	private int taskId;
	private HandlerInfo handlerInfo;
	private SliceScrollResultInf<T> sliceScrollResult;

	public ScrollTask(ScrollHandler<T> scrollHandler, ESDatas<T> response, HandlerInfo handlerInfo) {
		this.scrollHandler = scrollHandler;
		this.response = response;
		this.handlerInfo = handlerInfo;

	}

	public ScrollTask(ScrollHandler<T> scrollHandler, ESDatas<T> response, HandlerInfo handlerInfo, SliceScrollResultInf<T> sliceScrollResult) {
		this.scrollHandler = scrollHandler;
		this.response = response;
		this.handlerInfo = handlerInfo;
		this.sliceScrollResult = sliceScrollResult;
	}

	@Override
	public void run() {
		try {
			scrollHandler.handle(response,handlerInfo);
			if(sliceScrollResult != null)
				sliceScrollResult.incrementSize(response.getDatas().size());//统计实际处理的文档数量
		}  catch (	ElasticSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new ElasticSearchException("scroll result handle task["+handlerInfo.getTaskId()+"] failed:",e);
	}
	}
}
