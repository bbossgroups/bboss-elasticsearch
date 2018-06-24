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

public class BBossESStarter {
	private static final Logger log = LoggerFactory.getLogger(BBossESStarter.class);
	public void start(BBossESProperties properties) {
		if(properties.buildProperties() != null
				&& properties.buildProperties().size() > 0)
			ElasticSearchBoot.boot(properties.buildProperties());
		else{
			log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
		}
	}
}
