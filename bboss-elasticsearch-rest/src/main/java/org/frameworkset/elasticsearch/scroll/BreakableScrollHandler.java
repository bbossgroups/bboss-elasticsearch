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

/**
 * <p>Description: 可以终端scroll查询的处理器</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/4 11:45
 * @author biaoping.yin
 * @version 1.0
 */
public interface  BreakableScrollHandler {
	public void setBreaked(boolean breaked);
	/**
	 * 是否中断scroll查询
	 * @return
	 */
	public boolean isBreaked();
//	/**
//	 * 更加错误异常信息，判断是否在出错的情况下继续进行数据处理，全局配置
//	 * @return
//	 */
//	public boolean isContinueOneError(Throwable throwable);
}
