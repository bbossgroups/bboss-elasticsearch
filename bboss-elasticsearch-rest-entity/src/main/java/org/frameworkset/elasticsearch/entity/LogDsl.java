package org.frameworkset.elasticsearch.entity;
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
 * @Date 2019/11/13 14:08
 * @author biaoping.yin
 * @version 1.0
 */
public class LogDsl {
	/**
	 * 慢dsl输出阈值
	 */
	private  int slowDslThreshold;

	public int getSlowDslThreshold() {
		return slowDslThreshold;
	}

	public void setSlowDslThreshold(int slowDslThreshold) {
		this.slowDslThreshold = slowDslThreshold;
	}
	/**
	 * elasticsearch rest http服务请求地址
	 */
	private String url;
	/**
	 * http request method：post,get,put,delete
	 */
	private String action;
	/**
	 * request handle elapsed ms
	 */
	private long time;
	/**
	 * elasticsearch dsl
	 */
	private  String dsl;
	/**
	 * request handle begin time.
	 */
	private Date startTime;
	/**
	 * request handle end time.
	 */
	private Date endTime;



	/**
	 * 0 - dsl执行成功
	 * 1 - dsl执行异常
	 */
	private int resultCode;
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getDsl() {
		return dsl;
	}

	public void setDsl(String dsl) {
		this.dsl = dsl;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String result(){
		return resultCode == 0?"success":"failed";
	}
}
