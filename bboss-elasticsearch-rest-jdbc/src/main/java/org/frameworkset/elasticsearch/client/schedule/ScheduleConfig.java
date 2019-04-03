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
 * @Date 2018/9/7 17:14
 * @author biaoping.yin
 * @version 1.0
 */
public class ScheduleConfig {

	private Date scheduleDate;
	private Long deyLay;
	private Long period;
	private Boolean fixedRate;

	public boolean isExternalTimer() {
		return externalTimer;
	}

	public void setExternalTimer(boolean externalTimer) {
		this.externalTimer = externalTimer;
	}

	/**
	 * 采用外部定时任务引擎执行定时任务控制变量：
	 * false 内部引擎，默认值
	 * true 外部引擎
	 */
	protected boolean externalTimer;

	public Date getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(Date scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public Long getDeyLay() {
		return deyLay;
	}

	public void setDeyLay(Long deyLay) {
		this.deyLay = deyLay;
	}

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	public Boolean getFixedRate() {
		return fixedRate;
	}

	public void setFixedRate(Boolean fixedRate) {
		this.fixedRate = fixedRate;
	}
}
