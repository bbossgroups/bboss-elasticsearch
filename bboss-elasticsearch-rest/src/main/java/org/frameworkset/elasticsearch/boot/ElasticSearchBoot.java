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

import org.frameworkset.spi.assemble.PropertiesContainer;

import java.util.Map;

public abstract class ElasticSearchBoot {
	/**
	 * 根据指定的配置文件初始化elasticsearch客户端工具
	 * @param configFile 指定1到多个多个ElasticSearch属性配置文件，对应的路径格式为（多个用逗号分隔），例如：
	 * conf/elasticsearch.properties,application.properties,config/application.properties
	 * 上述的文件都是在classpath下面即可，如果需要指定绝对路径，格式为：
	 * file:d:/conf/elasticsearch.properties,file:d:/application.properties,config/application.properties
	 *
	 * 说明：带file:前缀表示后面的路径为绝对路径
	 */
	public final static void boot(String configFile){
		ElasticSearchPropertiesFilePlugin.init(configFile);
		ElasticSearchConfigBoot.boot();

	}

	/**
	 * 按照默认的配置文件初始化elasticsearch客户端工具
	 * conf/elasticsearch.properties,application.properties,config/application.properties
	 */
	public final static void boot(){
		ElasticSearchPropertiesFilePlugin.init((String)null);
		ElasticSearchConfigBoot.boot();

	}

	/**
	 * 按照默认的配置文件初始化elasticsearch客户端工具
	 * conf/elasticsearch.properties,application.properties,config/application.properties
	 */
	public final static PropertiesContainer boot(Map configProperties){
		return boot(  configProperties,false);
	}
	public final static PropertiesContainer boot(Map configProperties, boolean fromspringboot){

		return ElasticSearchConfigBoot.boot(configProperties,fromspringboot);

	}
}
