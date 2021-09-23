package org.frameworkset.elasticsearch.entity;
/**
 * Copyright 2020 bboss
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

import java.util.LinkedHashMap;

/**
 * <p>Description: 构建一个带key字段的map</p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2021/9/23 15:25
 * @author biaoping.yin
 * @version 1.0
 */
public class KeyMap<K,V> extends LinkedHashMap<K,V> {
	/**
	 * 标识记录key信息
	 */
	private Object key;

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}
}
