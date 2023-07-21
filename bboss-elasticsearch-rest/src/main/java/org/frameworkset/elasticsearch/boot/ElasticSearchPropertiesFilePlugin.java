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
import org.frameworkset.spi.assemble.GetProperties;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.spi.assemble.plugin.PropertiesFilePlugin;

import java.util.Map;

public class ElasticSearchPropertiesFilePlugin implements PropertiesFilePlugin {
//	private static String elasticSearchConfigFiles = "conf/elasticsearch.properties,application.properties,config/application.properties";
    private static String elasticSearchConfigFiles = "application.properties";
	private static Map configProperties;
	/**
	 * 0: 外部自定义配置文件
	 * 1：外部自定义属性
	 * -1:采用默认配置文件
	 */
	private static int initType = -1;
	public static void init(String elasticSearchConfigFiles){
		if(elasticSearchConfigFiles != null && elasticSearchConfigFiles.trim().length() > 0){
			ElasticSearchPropertiesFilePlugin.elasticSearchConfigFiles = elasticSearchConfigFiles.trim();
			initType = 0;
		}
	}

	/**
	 * 直接从map中装载初始化话es所需要的属性，用于从zookeeper/consul/etcd/ Eureka动态加载es配置
	 * @param configProperties
	 */
	public static void init(Map configProperties){
		if(configProperties != null && configProperties.size() > 0){
			ElasticSearchPropertiesFilePlugin.configProperties = configProperties;
			initType = 1;
		}
	}
	public int getInitType(BaseApplicationContext applicationContext,Map<String,String> extendsAttributes, PropertiesContainer propertiesContainer){
		return initType;
	}

	@Override
	public void restore(BaseApplicationContext applicationContext,Map<String,String> extendsAttributes, PropertiesContainer propertiesContainer) {
//		elasticSearchConfigFiles = "conf/elasticsearch.properties,application.properties,config/application.properties";
        elasticSearchConfigFiles = "application.properties";
		configProperties = null;
		/**
		 * 0: 外部自定义配置文件
		 * 1：外部自定义属性
		 * -1:采用默认配置文件
		 */
		initType = -1;
	}

	@Override
	public void afterLoaded(GetProperties applicationContext, PropertiesContainer propertiesContainer) {

	}

	@Override
	public Map getConfigProperties(BaseApplicationContext applicationContext,Map<String,String> extendsAttributes, PropertiesContainer propertiesContainer) {
		return configProperties;
	}

	@Override
	public String getFiles(BaseApplicationContext applicationContext,Map<String,String> extendsAttributes, PropertiesContainer propertiesContainer) {
		return elasticSearchConfigFiles;
	}
}
