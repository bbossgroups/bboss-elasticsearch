package org.frameworkset.elasticsearch.client.schedule;
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
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/9 21:17
 * @author biaoping.yin
 * @version 1.0
 */
public class SQLInfo {
	private String sql;
	private int paramSize;
	private String lastValueVarName;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public int getParamSize() {
		return paramSize;
	}

	public void setParamSize(int paramSize) {
		this.paramSize = paramSize;
	}

	public String getLastValueVarName() {
		return lastValueVarName;
	}

	public void setLastValueVarName(String lastValueVarName) {
		this.lastValueVarName = lastValueVarName;
	}
}
