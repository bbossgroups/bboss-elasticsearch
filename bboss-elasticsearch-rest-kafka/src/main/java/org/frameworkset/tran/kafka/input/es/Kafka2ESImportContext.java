package org.frameworkset.tran.kafka.input.es;
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

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.DataTranPlugin;
import org.frameworkset.tran.ESDataImportException;
import org.frameworkset.tran.kafka.KafkaImportConfig;
import org.frameworkset.tran.kafka.KafkaImportContext;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public class Kafka2ESImportContext extends KafkaImportContext {

	private static final String Kafka2ESInputPlugin = "org.frameworkset.tran.kafka.input.es.Kafka2ESInputPlugin";
	public Kafka2ESImportContext(KafkaImportConfig importConfig) {
		super(importConfig);
	}

	protected DataTranPlugin buildDataTranPlugin()
	{

		try {
			Class<DataTranPlugin> clazz = (Class<DataTranPlugin>) Class.forName(Kafka2ESInputPlugin);
			return clazz.getConstructor(ImportContext.class).newInstance(this);// Kafka2ESInputPlugin(this);
		} catch (ClassNotFoundException e) {
			throw new ESDataImportException(Kafka2ESInputPlugin,e);
		} catch (InstantiationException e) {
			throw new ESDataImportException(Kafka2ESInputPlugin,e);
		} catch (InvocationTargetException e) {
			throw new ESDataImportException(Kafka2ESInputPlugin,e);
		} catch (NoSuchMethodException e) {
			throw new ESDataImportException(Kafka2ESInputPlugin,e);
		} catch (IllegalAccessException e) {
			throw new ESDataImportException(Kafka2ESInputPlugin,e);
		}


	}


}
