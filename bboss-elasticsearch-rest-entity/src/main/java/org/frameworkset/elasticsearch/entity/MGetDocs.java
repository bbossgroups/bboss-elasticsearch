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

import java.io.Serializable;
import java.util.List;

/**
 * mget结果封装对象
 */
public class MGetDocs implements Serializable{
	private List<SearchHit> docs;

	public List<SearchHit> getDocs() {
		return docs;
	}

	public void setDocs(List<SearchHit> docs) {
		this.docs = docs;
	}
}
