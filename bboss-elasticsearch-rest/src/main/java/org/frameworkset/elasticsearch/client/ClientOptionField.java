package org.frameworkset.elasticsearch.client;
/**
 * Copyright 2008 biaoping.yin
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

import java.io.Serializable;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/31 23:48
 * @author biaoping.yin
 * @version 1.0
 */
public class ClientOptionField implements Serializable {
	private String parentIdField;
	private String idField;
	private String esRetryOnConflictField;

	public String getParentIdField() {
		return parentIdField;
	}

	public void setParentIdField(String parentIdField) {
		this.parentIdField = parentIdField;
	}

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public String getRountField() {
		return rountField;
	}

	public void setRountField(String rountField) {
		this.rountField = rountField;
	}

	private String rountField;
	private String refreshOption;

	public String getRefreshOption() {
		return refreshOption;
	}

	public void setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
	}

	public String getEsRetryOnConflictField() {
		return esRetryOnConflictField;
	}

	public void setEsRetryOnConflictField(String esRetryOnConflictField) {
		this.esRetryOnConflictField = esRetryOnConflictField;
	}
}
