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

import java.util.Date;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/8 17:37
 * @author biaoping.yin
 * @version 1.0
 */
public class Status implements java.lang.Cloneable{
	private int id;
	private long time;
	/**
	 * 0 数字类型
	 * 1 日期类型
	 */
	private int lastValueType;
	private Object lastValue;
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append("id:").append(id)
				.append(",time:").append(new Date(time))
				.append(",lastValue:").append(lastValueType == 0?this.lastValue:new Date((Long)lastValue));
		return ret.toString();
	}


	public Object getLastValue() {
		return lastValue;
	}

	public void setLastValue(Object lastValue) {
		this.lastValue = lastValue;
	}

	public int getLastValueType() {
		return lastValueType;
	}

	public void setLastValueType(int lastValueType) {
		this.lastValueType = lastValueType;
	}
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
