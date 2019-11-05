package org.frameworkset.elasticsearch.client.task;
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

import org.frameworkset.elasticsearch.client.metrics.ImportCount;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.metrics.TaskMetrics;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/4 16:50
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseTaskCommand<DATA,RESULT> implements TaskCommand<DATA,RESULT> {
	protected ImportCount importCount;
	protected ImportContext importContext;
	protected TaskMetrics taskMetrics;
	protected long dataSize;
	public long getDataSize(){
		return dataSize;
	}
	public TaskMetrics getTaskMetrics(){
		return taskMetrics;
	}
	public int getTaskNo(){
		return taskMetrics.getTaskNo();
	}
	public String getJobNo(){
		return taskMetrics.getJobNo();
	}
	@Override
	public ImportContext getImportContext() {
		return importContext;
	}

	public BaseTaskCommand(ImportCount importCount, ImportContext importContext,long dataSize,int taskNo,String jobNo){
		this.importCount = importCount;
		this.importContext =  importContext;
		this.dataSize = dataSize;
		this.taskMetrics = new TaskMetrics();
		taskMetrics.setTaskNo(taskNo);
		taskMetrics.setJobNo(jobNo);
	}
	public ImportCount getImportCount(){
		return this.importCount;
	}
}
