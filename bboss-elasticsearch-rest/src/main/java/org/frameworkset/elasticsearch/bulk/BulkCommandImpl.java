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
 * @Date 2019/12/7 12:58
 * @author biaoping.yin
 * @version 1.0
 */
public class BulkCommandImpl extends BaseBulkCommand{
	private static Logger logger = LoggerFactory.getLogger(BulkCommandImpl.class);

	public BulkCommandImpl(BulkProcessor bulkProcessor) {
        super(bulkProcessor);


	}



    @Override
    protected void clear(){
        super.clearDatas();
    }

    @Override
    public String getDataString(){
        if(batchBulkDatas == null || batchBulkDatas.size() == 0){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        BBossStringWriter writer = null;
        try {
            writer = new BBossStringWriter(builder);
            for (BulkData bulkData : batchBulkDatas) {
                try {
                    BuildTool.evalBuilk(writer, bulkData, this.clientInterface.isUpper7());
                } catch (IOException e) {
                    throw new ElasticSearchException(e);
                }
            }
            writer.flush();

            return builder.toString();


        }
        finally {
            builder.setLength(0);
            builder = null;
            writer = null;
        }
    }


    /**
     * 获取记录数
     * @return
     */
    @Override
	public int getBulkDataRecords(){
		if(batchBulkDatas != null)
			return this.batchBulkDatas.size();
		else
			return 0;
	}



    /**
     * 达到最大批处理记录数或者记录占用内存达到最大允许内存大小，则返回true，false返回false
     * @return
     */
    @Override
    public boolean touchBatchSize(BulkConfig bulkConfig) {

        if (getBulkDataRecords() >= bulkConfig.getBulkSizes()) {
            return true;
        }

        return false;

    }
}
