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

import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.mongodb.MongoDBDataStreamImpl;
import org.frameworkset.tran.mongodb.MongoDBImportConfig;
import org.frameworkset.tran.mongodb.input.es.MongoDB2ESImportContext;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/17 11:44
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2DBDataStreamImpl extends MongoDBDataStreamImpl {
	@Override
	protected ImportContext buildImportContext(BaseImportConfig importConfig) {
		return new MongoDB2ESImportContext((MongoDBImportConfig)importConfig);
	}
}
