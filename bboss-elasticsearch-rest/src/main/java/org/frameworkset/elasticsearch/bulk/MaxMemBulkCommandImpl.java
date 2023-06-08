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

import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.client.BuildTool;
import org.frameworkset.soa.BBossStringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2023/03/311 12:58
 * @author biaoping.yin
 * @version 1.0
 */
public class MaxMemBulkCommandImpl extends BaseBulkCommand{
	private static Logger logger = LoggerFactory.getLogger(MaxMemBulkCommandImpl.class);
    private int records;
    private BBossStringWriter writer ;


	public MaxMemBulkCommandImpl(BulkProcessor bulkProcessor) {
        super(bulkProcessor);
        StringBuilder builder = new StringBuilder();
        writer = new BBossStringWriter(builder);
	}
    @Override
    protected void clear(){
        if(this.writer != null){
            try {
                writer.close();
            } catch (IOException e) {

            }
            writer = null;
        }
        super.clearDatas();
    }

    /**
     * 达到最大批处理记录数或者记录占用内存达到最大允许内存大小，则返回true，false返回false
     * @return
     */
    @Override
    public boolean touchBatchSize(BulkConfig bulkConfig) {

        if (getBulkDataRecords() >= bulkConfig.getBulkSizes()
                || (getBulkDataMemSize() >= bulkConfig.getMaxMemSize())) {
            return true;
        }

        return false;

    }

    @Override
    public String getDataString(){
        return writer != null? writer.toString():null;
    }

	public void addBulkData(BulkData bulkData){
        records ++;
        try {
            super.addBulkData(bulkData);
            BuildTool.evalBuilk(writer, bulkData, this.clientInterface.isUpper7());
        } catch (IOException e) {
            throw new ElasticSearchException(e);
        }
	}

    /**
     * 获取记录数
     * @return
     */
    @Override
	public int getBulkDataRecords(){
        return records;
	}

    /**
     * 获取记录占用内存大小
     * @return
     */
    public int getBulkDataMemSize(){
        if(writer != null && this.writer.getBuffer() != null)
            return this.writer.getBuffer().length();
        else
            return 0;
    }
}
