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

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/4 12:55
 * @author biaoping.yin
 * @version 1.0
 */
public interface SliceScrollResultInf<T> {

	//辅助方法，用来累计每次scroll获取到的记录数
	public void incrementSize(int size);

	public ESDatas<T> getSliceResponse();
	public void complete();

	public void setSliceResponse(ESDatas<T> sliceResponse);

	public long getRealTotalSize();

	public ScrollHandler<T> getScrollHandler() ;
	public ScrollHandler<T> setScrollHandler(ScrollHandler<T> scrollHandler);
	public ScrollHandler<T> setScrollHandler(ESDatas<T> sliceResponse) throws Exception;
}
