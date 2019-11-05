package org.frameworkset.elasticsearch.client.metrics;
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

import com.frameworkset.util.UUID;

import java.util.Date;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/10/15 19:20
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class ImportCount {
	protected Date jobStartTime;
	protected Date jobEndTime;
	protected String jobNo;
	protected long totalCount;
	protected long failedCount;
	protected long successCount;
	protected long ignoreTotalCount;
	public ImportCount(){
		jobNo = UUID.randomUUID().toString();
		this.jobStartTime = new Date();
	}
	public abstract long getTotalCount() ;

	public abstract long getFailedCount() ;


	public abstract long getIgnoreTotalCount() ;
	public abstract long[] increamentFailedCount(long failedCount) ;
	public abstract long increamentIgnoreTotalCount() ;


	public abstract long getSuccessCount() ;
	public abstract long[] increamentSuccessCount(long successCount) ;

	public Date getJobStartTime() {
		return jobStartTime;
	}

	public Date getJobEndTime() {
		return jobEndTime;
	}

	public void setJobEndTime(Date jobEndTime) {
		this.jobEndTime = jobEndTime;
	}

	public String getJobNo() {
		return jobNo;
	}
}
