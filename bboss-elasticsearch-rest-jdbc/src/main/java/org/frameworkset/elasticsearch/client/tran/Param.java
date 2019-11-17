package org.frameworkset.elasticsearch.client.tran;
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

import com.frameworkset.util.VariableHandler;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/15 16:10
 * @author biaoping.yin
 * @version 1.0
 */
public class Param {
	private int index;
	private Object value;
	private String name;
	private VariableHandler.Variable variable;
	public String getName() {
		return name;
	}
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("{name:").append(name)
				.append(",value:").append(value)
				.append(",postion:").append(index).append("}");
		return builder.toString();
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public VariableHandler.Variable getVariable() {
		return variable;
	}

	public void setVariable(VariableHandler.Variable variable) {
		this.variable = variable;
	}
}
