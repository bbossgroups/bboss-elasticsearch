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

import org.frameworkset.elasticsearch.bulk.BulkActionConfig;

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
public class ClientOptions    implements BulkActionConfig,Serializable {
	private String esRetryOnConflictField;
	private String versionField;
	private String versionTypeField;
	private String routingField;

	private Object esRetryOnConflict;
	private Object version;
	private Object versionType;
	private Object routing;
	private String filterPath;
	private String pipeline;
	private String opType;
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

	private List<String> sourceUpdateExcludes;
	private List<String> sourceUpdateIncludes;
	private String timeout;
	private String masterTimeout ;
	private Integer waitForActiveShards;
	private String refresh;
	private String idField;

    /**
     * 添加文档时，如果数据采用Map封装，控制是否保存文档id字段到记录中，true 保存  false 不保存，默认值true
     * 如果设置了idField，并且不想持久化这个字段值到文档中，可以设置persistMapDocId为false
     */
    private boolean persistMapDocId = true;
	/**单文档操作：文档id*/
	private Object id;
	/**单文档操作：文档id*/
	private Object parentId;
	/**
	 * 设置script脚本对应的字段名称（bean属性，map为key）
	 * 批处理时采用，其他操作不起作用
	 */
	private String scriptField;


    /**
     * 脚本中已经涵盖了doc部分内容，无需自动添加doc部分内容
     */
    private boolean haveScriptDoc;




    public String getRefreshOption() {
		return refreshOption;
	}

	public ClientOptions setRefreshOption(String refreshOption) {
		this.refreshOption = refreshOption;
		return this;
	}

	public ClientOptions setEsParentIdValue(Object esParentIdValue) {
		this.esParentIdValue = esParentIdValue;
		return this;
	}

	public Object getEsParentIdValue() {
		return esParentIdValue;
	}

	public ClientOptions setRefresh(String refresh) {
		this.refresh = refresh;
		return this;
	}

	public String getRefresh() {
		return refresh;
	}

	public String getDetectNoopField() {
		return detectNoopField;
	}

	public ClientOptions setDetectNoopField(String detectNoopField) {
		this.detectNoopField = detectNoopField;
		return this;
	}

	public String getDocasupsertField() {
		return docasupsertField;
	}

	public ClientOptions setDocasupsertField(String docasupsertField) {
		this.docasupsertField = docasupsertField;
		return this;
	}


	public Long getIfSeqNo() {
		return ifSeqNo;
	}

	public ClientOptions setIfSeqNo(Long ifSeqNo) {
		this.ifSeqNo = ifSeqNo;
		return this;
	}

	public Long getIfPrimaryTerm() {
		return ifPrimaryTerm;
	}

	public ClientOptions setIfPrimaryTerm(Long ifPrimaryTerm) {
		this.ifPrimaryTerm = ifPrimaryTerm;
		return this;
	}
	public String getIdField() {
		return idField;
	}

	public ClientOptions setIdField(String idField) {
		this.idField = idField;
		return this;
	}

	public String getParentIdField() {
		return parentIdField;
	}

	public ClientOptions setParentIdField(String parentIdField) {
		this.parentIdField = parentIdField;
		return this;
	}

	public Object getDocasupsert() {
		return docasupsert;
	}

	public ClientOptions setDocasupsert(Object docasupsert) {
		this.docasupsert = docasupsert;
		return this;
	}

	public Object getDetectNoop() {
		return detectNoop;
	}

	public ClientOptions setDetectNoop(Object detectNoop) {
		this.detectNoop = detectNoop;
		return this;
	}

	public Boolean getReturnSource() {
		return returnSource;
	}

	public ClientOptions setReturnSource(Boolean returnSource) {
		this.returnSource = returnSource;
		return this;
	}


	public String getTimeout() {
		return timeout;
	}

	public ClientOptions setTimeout(String timeout) {
		this.timeout = timeout;
		return this;
	}

	public String getMasterTimeout() {
		return masterTimeout;
	}

	public ClientOptions setMasterTimeout(String masterTimeout) {
		this.masterTimeout = masterTimeout;
		return this;
	}

	public Integer getWaitForActiveShards() {
		return waitForActiveShards;
	}

	public ClientOptions setWaitForActiveShards(Integer waitForActiveShards) {
		this.waitForActiveShards = waitForActiveShards;
		return this;
	}

	public List<String> getSourceUpdateExcludes() {
		return sourceUpdateExcludes;
	}

	public List<String> getSourceUpdateIncludes() {
		return sourceUpdateIncludes;
	}

	public ClientOptions setSourceUpdateExcludes(List<String> sourceUpdateExcludes) {
		this.sourceUpdateExcludes = sourceUpdateExcludes;
		return this;
	}

	public ClientOptions setSourceUpdateIncludes(List<String> sourceUpdateIncludes) {
		this.sourceUpdateIncludes = sourceUpdateIncludes;
		return this;
	}
	public String getRoutingField() {
		return routingField;
	}

	public ClientOptions setRoutingField(String routingField) {
		this.routingField = routingField;
		return this;
	}



	public String getEsRetryOnConflictField() {
		return esRetryOnConflictField;
	}

	public ClientOptions setEsRetryOnConflictField(String esRetryOnConflictField) {
		this.esRetryOnConflictField = esRetryOnConflictField;
		return this;
	}

	public String getVersionField() {
		return versionField;
	}

	public ClientOptions setVersionField(String versionField) {
		this.versionField = versionField;
		return this;
	}

	public String getVersionTypeField() {
		return versionTypeField;
	}

	public ClientOptions setVersionTypeField(String versionTypeField) {
		this.versionTypeField = versionTypeField;
		return this;
	}

	public Object getEsRetryOnConflict() {
		return esRetryOnConflict;
	}

	public ClientOptions setEsRetryOnConflict(Object esRetryOnConflict) {
		this.esRetryOnConflict = esRetryOnConflict;
		return this;
	}

	public Object getVersion() {
		return version;
	}

	public ClientOptions setVersion(Object version) {
		this.version = version;
		return this;
	}

	public Object getVersionType() {
		return versionType;
	}

	public ClientOptions setVersionType(Object versionType) {
		this.versionType = versionType;
		return this;
	}

	public Object getRouting() {
		return routing;
	}

	public ClientOptions setRouting(Object routing) {
		this.routing = routing;
		return this;
	}


	public String getPipeline() {
		return pipeline;
	}


	public ClientOptions setFilterPath(String filterPath) {
		this.filterPath = filterPath;
        return this;
	}

	@Override
	public String getFilterPath() {
		return filterPath;
	}

	public ClientOptions setPipeline(String pipeline) {
		this.pipeline = pipeline;
		return this;
	}

	public String getOpType() {
		return opType;
	}

	public ClientOptions setOpType(String opType) {
		this.opType = opType;
		return this;
	}

	public Object getId() {
		return id;
	}

	public ClientOptions setId(Object id) {
		this.id = id;
        return this;
	}

	public Object getParentId() {
		return parentId;
	}

	public ClientOptions setParentId(Object parentId) {
		this.parentId = parentId;
        return this;
	}

	public String getScriptField() {
		return scriptField;
	}

	public ClientOptions setScriptField(String scriptField) {
		this.scriptField = scriptField;
        return this;
	}
    public boolean isHaveScriptDoc() {
        return haveScriptDoc;
    }

    public ClientOptions setHaveScriptDoc(boolean haveScriptDoc) {
        this.haveScriptDoc = haveScriptDoc;
        return this;
    }

    public boolean isPersistMapDocId() {
        return persistMapDocId;
    }

    /**
     * 添加文档时，如果数据采用Map封装，控制是否保存文档id字段到记录中，true 保存  false 不保存，默认值true
     * 如果设置了idField，并且不想持久化这个字段值到文档中，可以设置persistMapDocId为false
     */
    public ClientOptions setPersistMapDocId(boolean persistMapDocId) {
        this.persistMapDocId = persistMapDocId;
        return this;
    }
}
