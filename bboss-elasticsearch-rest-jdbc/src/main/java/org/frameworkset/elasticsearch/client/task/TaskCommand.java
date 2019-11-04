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

import org.frameworkset.elasticsearch.client.ImportCount;
import org.frameworkset.elasticsearch.client.context.ImportContext;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/1 11:32
 * @author biaoping.yin
 * @version 1.0
 */
public interface TaskCommand<DATA,RESULT> {

	public DATA getDatas() ;

	public TaskMetrics getTaskMetrics();
	public void setDatas(DATA datas) ;


	public RESULT execute();

	public int getTryCount() ;
	public ImportContext getImportContext();
	public ImportCount getImportCount();
	public long getDataSize();
	public int getTaskNo();
	public String getJobNo();


}
