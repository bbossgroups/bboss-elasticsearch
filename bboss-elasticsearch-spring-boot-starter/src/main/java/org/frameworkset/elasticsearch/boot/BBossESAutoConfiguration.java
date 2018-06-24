package org.frameworkset.elasticsearch.boot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
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
@Configuration
@ConditionalOnClass(BBossESStarter.class)
@EnableConfigurationProperties(BBossESProperties.class)
public class BBossESAutoConfiguration {
	private final BBossESProperties properties;
	public BBossESAutoConfiguration(BBossESProperties properties){
		this.properties = properties;

	}
	@Bean
	@ConditionalOnMissingBean
	public BBossESStarter getBbossESStarter() {
		BBossESStarter bBossESStarter = new BBossESStarter();
		bBossESStarter.start(properties);
		return bBossESStarter;
	}
}
