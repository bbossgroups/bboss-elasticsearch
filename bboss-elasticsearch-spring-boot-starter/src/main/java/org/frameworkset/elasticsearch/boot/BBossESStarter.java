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
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


public class BBossESStarter  extends BaseESProperties{
	@Autowired
	private BBossESProperties properties;
	private static final Logger log = LoggerFactory.getLogger(BBossESStarter.class);
	public void start() {
		if(this.getElasticsearch() == null) {
			if (properties.getElasticsearch() != null) {
				Map ps = properties.buildProperties();
				if (ps != null && ps.size() > 0)
					ElasticSearchBoot.boot(ps,true);
				else {
					log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
				}
			}
		}
		else{
			if(properties.getDslfile() != null && this.getDslfile() == null)
				this.setDslfile(properties.getDslfile());
			Map ps = buildProperties();
			if (ps != null && ps.size() > 0)
				ElasticSearchBoot.boot(ps,true);
			else {
				log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
			}
		}

	}

	private ClientInterface restClient;

	/**
	 * Get default elasticsearch server ClientInterface
	 * @return
	 */
	public ClientInterface getRestClient(){
		if(restClient == null) {
			synchronized (this) {
				if(restClient == null) {
					restClient = ElasticSearchHelper.getRestClientUtil();
				}
			}
		}
		return restClient;
	}

	/**
	 * Get Special elasticsearch server ConfigFile ClientInterface
	 * @param elasticsearchName elasticsearch server name which defined in bboss spring boot application configfile
	 * @param configFile
	 * @return
	 */
	public ClientInterface getConfigRestClient(String elasticsearchName,String configFile){

		return ElasticSearchHelper.getConfigRestClientUtil(elasticsearchName,configFile);

	}

	/**
	 *  Get Special elasticsearch server ClientInterface
	 * @param elasticsearchName elasticsearch server name which defined in bboss spring boot application configfile
	 * @return
	 */
	public ClientInterface getRestClient(String elasticsearchName){

		return ElasticSearchHelper.getRestClientUtil(elasticsearchName);

	}

	/**
	 * Get default elasticsearch server ConfigFile ClientInterface
	 * @param configFile
	 * @return
	 */
	public ClientInterface getConfigRestClient(String configFile){

		return ElasticSearchHelper.getConfigRestClientUtil(configFile);

	}
}
