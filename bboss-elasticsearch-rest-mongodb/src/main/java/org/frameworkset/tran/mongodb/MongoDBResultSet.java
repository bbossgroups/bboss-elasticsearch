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

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.DefaultTranMetaData;
import org.frameworkset.tran.TranMeta;
import org.frameworkset.tran.TranResultSet;
import org.frameworkset.elasticsearch.client.util.TranUtil;

import java.util.Date;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/8/3 12:27
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDBResultSet implements TranResultSet {
	private ImportContext importContext ;
	private DBCursor dbCursor;
	private DBObject record;
	public MongoDBResultSet(ImportContext importContext, DBCursor dbCursor) {
		this.importContext = importContext;
		this.dbCursor = dbCursor;
	}


	@Override
	public Object getValue(int i, String colName, int sqlType) throws ESDataImportException {
		return getValue(  colName);
	}

	@Override
	public Object getValue(String colName) throws ESDataImportException {
		Object value = record.get(colName);
		if(value != null) {
			if (colName.equals("_id") && value instanceof ObjectId) {
				return ((ObjectId)value).toString();
			}

		}
		return value;

	}

	@Override
	public Object getValue(String colName, int sqlType) throws ESDataImportException {
		return getValue(  colName);
	}

	@Override
	public Date getDateTimeValue(String colName) throws ESDataImportException {
		Object value = getValue(  colName);
		if(value == null)
			return null;
		return TranUtil.getDateTimeValue(colName,value,importContext);

	}

	@Override
	public boolean next() throws ESDataImportException {
		boolean hasNext = dbCursor.hasNext();
		if( hasNext){
			record = dbCursor.next();
		}
		return hasNext;
	}

	@Override
	public TranMeta getMetaData() {
		return new DefaultTranMetaData(record.keySet());
	}
}
