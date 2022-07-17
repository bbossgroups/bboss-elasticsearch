package org.frameworkset.elasticsearch.boot;
/**
 * Copyright 2022 bboss
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

import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.util.ResourceStartResult;

import java.util.List;

/**
 * <p>Description: 封装启动Elasticsearch数据源结果</p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2022/4/23
 * @author biaoping.yin
 * @version 1.0
 */
public class ElasticsearchBootResult extends ResourceStartResult {
	/**
	 * 加载的属性配置container
	 */
	private PropertiesContainer propertiesContainer;
//	/**
//	 * 初始化的Elasticsearch数据源清单
//	 */
//	private List<String> initedElasticsearchs;

	public PropertiesContainer getPropertiesContainer() {
		return propertiesContainer;
	}

	public void setPropertiesContainer(PropertiesContainer propertiesContainer) {
		this.propertiesContainer = propertiesContainer;
	}

//	public List<String> getInitedElasticsearchs() {
//		Map<String,Object> rs = this.getResourceStartResult();
//		if(rs != null && rs.size() > 0){
//
//		}
//		return initedElasticsearchs;
//	}

	public void setInitedElasticsearchs(List<String> initedElasticsearchs) {
//		this.initedElasticsearchs = initedElasticsearchs;
		this.addInitedElasticsearchs(initedElasticsearchs);
	}

	public void addInitedElasticsearchs(List<String> initedElasticsearchs) {
		for(int i = 0; initedElasticsearchs != null && i < initedElasticsearchs.size(); i ++) {
			String es = initedElasticsearchs.get(i);
			this.addResourceStartResult(es);
		}
	}
}
