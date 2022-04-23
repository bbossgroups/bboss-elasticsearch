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

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.DefaultApplicationContext;
import org.frameworkset.spi.assemble.GetProperties;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.spi.remote.http.ClientConfiguration;

import java.util.List;
import java.util.Map;

public abstract class ElasticSearchConfigBoot {
	private static boolean inited = false;
	public static ElasticsearchBootResult boot(){
		return boot(false);
	}

	/**
	 *
	 * @param forceBoot 强制启动
	 */
	public static ElasticsearchBootResult boot(boolean forceBoot){
		if(inited) {
			if(!forceBoot)
				return null;
		}
		ElasticsearchBootResult elasticsearchBootResult = null;
		synchronized (ElasticSearchConfigBoot.class) {
			if(inited){
				if(!forceBoot)
					return null;
			}
			try {
				elasticsearchBootResult = new ElasticsearchBootResult();
				final BaseApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch-boot-config.xml",forceBoot);
				String _elasticsearchServerNames = context.getExternalProperty("elasticsearch.serverNames", "default");
				String[] elasticsearchServerNames = _elasticsearchServerNames.split(",");

				//初始化Http连接池
				GetProperties getProperties = new WrapperGetProperties(context);
				ClientConfiguration.bootClientConfiguations(elasticsearchServerNames, getProperties );
				ClientConfiguration.bootHealthCheckClientConfiguations(elasticsearchServerNames,getProperties);
				//初始化ElasticSearchServer
				List<String> initedElasticsearchs = ElasticSearchHelper.booter(elasticsearchServerNames, context,forceBoot,false);
				elasticsearchBootResult.setInitedElasticsearchs(initedElasticsearchs);
			}
			finally {
				inited = true;
			}
		}
		return elasticsearchBootResult;




	}
	public static ElasticsearchBootResult boot(Map properties){
		return boot(  properties,false);
	}
	public static ElasticsearchBootResult boot(Map properties,boolean fromspringboot){
		PropertiesContainer propertiesContainer = new PropertiesContainer();
		ElasticsearchBootResult elasticsearchBootResult = new ElasticsearchBootResult();
		synchronized (ElasticSearchConfigBoot.class) {

			propertiesContainer.addAll(properties);
			String _elasticsearchServerNames = propertiesContainer.getExternalProperty("elasticsearch.serverNames", "default");
			String[] elasticsearchServerNames = _elasticsearchServerNames.split(",");
			//初始化Http连接池
			GetProperties getProperties = new WrapperGetProperties(propertiesContainer);
			ClientConfiguration.bootClientConfiguations(elasticsearchServerNames, getProperties);
			ClientConfiguration.bootHealthCheckClientConfiguations(elasticsearchServerNames, getProperties);
			//初始化ElasticSearchServer
			List<String> initedElasticsearchs =  ElasticSearchHelper.booter(elasticsearchServerNames,   propertiesContainer,true,fromspringboot);
			elasticsearchBootResult.setInitedElasticsearchs(initedElasticsearchs);
		}
		elasticsearchBootResult.setPropertiesContainer(propertiesContainer);
		return elasticsearchBootResult;




	}

}
