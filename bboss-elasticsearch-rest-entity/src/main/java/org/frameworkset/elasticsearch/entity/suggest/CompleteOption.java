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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.frameworkset.elasticsearch.serial.ESHitDeserializer;

public class CompleteOption {
	@JsonProperty("text")
	private String text;
	@JsonProperty("_index")
	private String index;
	@JsonProperty("_type")
	private String type;
	@JsonProperty("_id")
	private String id;
	@JsonProperty("_score")
	private double score;
	@JsonProperty("_source")
	private Object source;
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}


	public Object getSource() {
		return source;
	}
	@JsonDeserialize(using = ESHitDeserializer.class)
	public void setSource(Object source) {
		this.source = source;
	}
}
