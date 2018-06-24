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
					ElasticSearchBoot.boot(ps);
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
				ElasticSearchBoot.boot(ps);
			else {
				log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
			}
		}

	}
}
