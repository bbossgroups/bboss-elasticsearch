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
import java.util.List;

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
	protected String refreshOption;
	private String detectNoopField;
	private String docasupsertField;
	private Object detectNoop;
	private Object docasupsert;
	private Boolean returnSource;
	protected String parentIdField;
	protected Object esParentIdValue;
	private Long ifSeqNo;
	private Long ifPrimaryTerm;

	protected List<String> sourceUpdateExcludes;
	protected List<String> sourceUpdateIncludes;
	protected String timeout = "30s";
	protected String masterTimeout = "30s";
	protected Integer waitForActiveShards;
	protected String refresh;
	protected String idField;
	public String getRefreshOption() {
		return refreshOption;
	}

	public void setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
	}

	public void setEsParentIdValue(Object esParentIdValue) {
		this.esParentIdValue = esParentIdValue;
	}

	public Object getEsParentIdValue() {
		return esParentIdValue;
	}

	public void setRefresh(String refresh) {
		this.refresh = refresh;
	}

	public String getRefresh() {
		return refresh;
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


	public Long getIfSeqNo() {
		return ifSeqNo;
	}

	public void setIfSeqNo(Long ifSeqNo) {
		this.ifSeqNo = ifSeqNo;
	}

	public Long getIfPrimaryTerm() {
		return ifPrimaryTerm;
	}

	public void setIfPrimaryTerm(Long ifPrimaryTerm) {
		this.ifPrimaryTerm = ifPrimaryTerm;
	}
	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public String getParentIdField() {
		return parentIdField;
	}

	public void setParentIdField(String parentIdField) {
		this.parentIdField = parentIdField;
	}

	public Object getDocasupsert() {
		return docasupsert;
	}

	public void setDocasupsert(Object docasupsert) {
		this.docasupsert = docasupsert;
	}

	public Object getDetectNoop() {
		return detectNoop;
	}

	public void setDetectNoop(Object detectNoop) {
		this.detectNoop = detectNoop;
	}

	public Boolean getReturnSource() {
		return returnSource;
	}

	public void setReturnSource(Boolean returnSource) {
		this.returnSource = returnSource;
	}


	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getMasterTimeout() {
		return masterTimeout;
	}

	public void setMasterTimeout(String masterTimeout) {
		this.masterTimeout = masterTimeout;
	}

	public Integer getWaitForActiveShards() {
		return waitForActiveShards;
	}

	public void setWaitForActiveShards(Integer waitForActiveShards) {
		this.waitForActiveShards = waitForActiveShards;
	}

	public List<String> getSourceUpdateExcludes() {
		return sourceUpdateExcludes;
	}

	public List<String> getSourceUpdateIncludes() {
		return sourceUpdateIncludes;
	}

	public void setSourceUpdateExcludes(List<String> sourceUpdateExcludes) {
		this.sourceUpdateExcludes = sourceUpdateExcludes;
	}

	public void setSourceUpdateIncludes(List<String> sourceUpdateIncludes) {
		this.sourceUpdateIncludes = sourceUpdateIncludes;
	}
}
