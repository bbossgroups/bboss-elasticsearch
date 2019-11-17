package org.frameworkset.tran.mongodb;
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

import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.tran.TranMeta;

import java.util.Set;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/27 12:13
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDBTranMetaData implements TranMeta {
	public MongoDBTranMetaData(Set<String> keys) {
		this.keys = keys.toArray(new String[0]);
	}

	private String[] keys;
	@Override
	public int getColumnCount()  throws ESDataImportException {

			return keys.length;

	}

	@Override
	public String getColumnLabelByIndex(int i)  throws ESDataImportException{
		return keys[i];
	}

	@Override
	public int getColumnTypeByIndex(int i)  throws ESDataImportException{
		return -1;
	}

	@Override
	public String getColumnJavaNameByIndex(int i)  throws ESDataImportException{
		return keys[i];

	}

	@Override
	public String getColumnLabelLowerByIndex(int i)  throws ESDataImportException{
		return keys[i].toLowerCase();

	}
}
