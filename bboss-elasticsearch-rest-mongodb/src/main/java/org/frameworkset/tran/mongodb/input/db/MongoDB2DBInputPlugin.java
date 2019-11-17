package org.frameworkset.tran.mongodb.input.db;
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
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.mongodb.MongoDBResultSet;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/15 22:22
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2DBInputPlugin extends MongoDBInputPlugin{
	public MongoDB2DBInputPlugin(ImportContext importContext) {
		super(importContext);
	}

	@Override
	protected void doTran(DBCursor dbCursor) {
		MongoDBResultSet mongoDB2ESResultSet = new MongoDBResultSet(importContext,dbCursor);
		MongoDB2DBDataTran mongoDB2ESDataTran = new MongoDB2DBDataTran(mongoDB2ESResultSet,importContext);
		mongoDB2ESDataTran.tran();
	}
}
