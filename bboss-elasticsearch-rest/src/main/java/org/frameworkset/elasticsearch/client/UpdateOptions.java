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
 * <p>Description: 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
 * 可以在ClientOption中指定以下参数：
 * 	private String parentIdField;
 * 	private String idField;
 * 	private String esRetryOnConflictField;
 * 	private String versionField;
 * 	private String versionTypeField;
 * 	private String rountField;
 * 	private String refreshOption;
 * </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/31 23:48
 * @author biaoping.yin
 * @version 1.0
 */
public class UpdateOptions implements Serializable {
	private String refreshOption;
	private String detectNoopField;
	private String docasupsertField;

	private String docIdField;
	public String getRefreshOption() {
		return refreshOption;
	}

	public void setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
	}

	public String getDetectNoopField() {
		return detectNoopField;
	}

	public void setDetectNoopField(String detectNoopField) {
		this.detectNoopField = detectNoopField;
	}

	public String getDocasupsertField() {
		return docasupsertField;
	}

	public void setDocasupsertField(String docasupsertField) {
		this.docasupsertField = docasupsertField;
	}

	public String getDocIdField() {
		return docIdField;
	}

	public void setDocIdField(String docIdField) {
		this.docIdField = docIdField;
	}



}
