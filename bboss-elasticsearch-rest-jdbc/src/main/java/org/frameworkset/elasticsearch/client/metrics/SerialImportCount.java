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
public class SerialImportCount extends ImportCount{

	public SerialImportCount(){
		jobNo = UUID.randomUUID().toString();
		this.jobStartTime = new Date();
	}
	public long getTotalCount() {
			return totalCount;
	}

	public long getFailedCount() {

			return failedCount;
	}


	public long getIgnoreTotalCount() {
			return ignoreTotalCount;

	}
	public long[] increamentFailedCount(long failedCount) {
			this.failedCount = failedCount+this.failedCount;
			this.totalCount = totalCount + failedCount;
			return new long[]{this.failedCount,this.totalCount};
	}
	public long increamentIgnoreTotalCount() {
			this.ignoreTotalCount ++;
			this.totalCount ++;
			return ignoreTotalCount;
	}


	public long getSuccessCount() {
			return successCount;

	}
	public long[] increamentSuccessCount(long successCount) {
			this.successCount = this.successCount+successCount;
			this.totalCount = totalCount + successCount;
			return new long[]{this.successCount,this.totalCount};
	}


}
