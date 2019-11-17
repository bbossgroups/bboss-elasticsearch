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

import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.elasticsearch.client.ExportResultHandler;
import org.frameworkset.elasticsearch.client.WrapedExportResultHandler;
import org.frameworkset.tran.DefualtExportResultHandler;
import org.frameworkset.tran.mongodb.MongoDBExportBuilder;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2DBExportBuilder extends MongoDBExportBuilder {
	public static MongoDB2DBExportBuilder newInstance(){
		return new MongoDB2DBExportBuilder();
	}

	@Override
	protected DataStream createDataStream() {
		return new MongoDB2DBDataStreamImpl();
	}
	@Override
	protected WrapedExportResultHandler buildExportResultHandler(ExportResultHandler exportResultHandler) {
		return new DefualtExportResultHandler<Object,Object>(exportResultHandler);
	}
}
