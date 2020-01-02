package org.frameworkset.elasticsearch.entity;/*
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.frameworkset.elasticsearch.serial.ESInnerHitDeserializer;

import java.util.Map;

public class InnerSearchHit  extends BaseSearchHit{

	@JsonProperty("_source")
	@JsonDeserialize(using = ESInnerHitDeserializer.class)
	private Object source;

	public InnerSearchHit() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 获取map类型的source对象
	 * @return
	 */
	public Map<String,Object> asMap(){
		if(source == null)
			return null;
		return (Map<String,Object>)source;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}


}
