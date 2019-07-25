package org.frameworkset.elasticsearch.entity.suggest;/*
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
import org.frameworkset.elasticsearch.entity.RestResponse;

import java.util.List;
import java.util.Map;

/**
 * complate suggest & context suggest
 */
public class CompleteRestResponse extends RestResponse {
	public Map<String, List<CompleteSuggest>> getSuggests() {
		return suggests;
	}

	public void setSuggests(Map<String, List<CompleteSuggest>> suggests) {
		this.suggests = suggests;
	}

	@JsonProperty("suggest")
	private Map<String,List<CompleteSuggest>> suggests;
}
