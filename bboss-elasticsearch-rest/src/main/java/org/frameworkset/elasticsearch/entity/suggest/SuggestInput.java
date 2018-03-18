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

/**
 * The completion suggester provides auto-complete/search-as-you-type functionality. This is a navigational feature to guide users to relevant results as they are typing, improving search precision. It is not meant for spell correction or did-you-mean functionality like the term or phrase suggesters.

 Ideally, auto-complete functionality should be as fast as a user types to provide instant feedback relevant to what a user has already typed in. Hence, completion suggester is optimized for speed. The suggester uses data structures that enable fast lookups, but are costly to build and are stored in-memory.
 */
public class SuggestInput {
	private String input;
	private Integer weight;

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}
}
