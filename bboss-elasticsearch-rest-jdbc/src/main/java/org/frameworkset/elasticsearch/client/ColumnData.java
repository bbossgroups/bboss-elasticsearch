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

import java.io.Serializable;

/**
 * <p>Description: 数据预处理封装对象，处理完毕原始名称和值后，将新的值和新的名称
 * 存入ColumnData
 * </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/4/20 13:54
 * @author biaoping.yin
 * @version 1.0
 */

public class ColumnData implements Serializable {
	private String newFieldName;
	private Object newFieldValue;

	public String getNewFieldName() {
		return newFieldName;
	}

	public void setNewFieldName(String newFieldName) {
		this.newFieldName = newFieldName;
	}

	public Object getNewFieldValue() {
		return newFieldValue;
	}

	public void setNewFieldValue(Object newFieldValue) {
		this.newFieldValue = newFieldValue;
	}
}
