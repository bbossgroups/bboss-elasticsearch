package org.frameworkset.elasticsearch.bulk;
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
 * @Date 2019/12/7 12:58
 * @author biaoping.yin
 * @version 1.0
 */
public interface BulkCommand extends Runnable{
    /**
     * 获取已追加总记录数据
     * @return
     */
    public long getAppendRecords();
    public boolean touchBatchSize(BulkConfig bulkConfig);
    public Date getBulkCommandStartTime();

	public void setBulkCommandStartTime(Date bulkCommandStartTime);

	public Date getBulkCommandCompleteTime() ;
	public long getElapsed();

	public void setBulkCommandCompleteTime(Date bulkCommandCompleteTime) ;


	public String getRefreshOption() ;

	public String getFilterPath();


    /**
     * 获取已处理成功总记录数据
     * @return
     */
	public long getTotalSize();

    /**
     * 获取处理失败总记录数据
     * @return
     */
	public long getTotalFailedSize();

	public BulkProcessor getBulkProcessor();


    public String getDataString();

	public void addBulkData(BulkData bulkData);

    /**
     * 获取记录数
     * @return
     */
	public int getBulkDataRecords();

}
