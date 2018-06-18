package org.frameworkset.elasticsearch.boot;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.assemble.plugin.PropertiesFilePlugin;

import java.util.Map;

public class ElasticSearchPropertiesFilePlugin implements PropertiesFilePlugin {
	private static String elasticSearchConfigFiles = "conf/elasticsearch.properties,application.properties,config/application.properties";
	private static Map configProperties;
	public static void init(String elasticSearchConfigFiles){
		if(elasticSearchConfigFiles != null && elasticSearchConfigFiles.trim().length() > 0){
			ElasticSearchPropertiesFilePlugin.elasticSearchConfigFiles = elasticSearchConfigFiles.trim();
		}
	}

	/**
	 * 直接从map中装载初始化话es所需要的属性，用于从zookeeper/consul/etcd/ Eureka动态加载es配置
	 * @param configProperties
	 */
	public static void init(Map configProperties){
		if(configProperties != null && configProperties.size() > 0){
			ElasticSearchPropertiesFilePlugin.configProperties = configProperties;
		}
	}
	@Override
	public Map getConfigProperties(BaseApplicationContext applicationContext) {
		return configProperties;
	}

	@Override
	public String getFiles(BaseApplicationContext applicationContext) {
		return elasticSearchConfigFiles;
	}
}
