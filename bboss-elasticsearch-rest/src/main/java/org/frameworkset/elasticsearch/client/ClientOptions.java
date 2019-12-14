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
public class ClientOptions extends UpdateOptions {
	private String esRetryOnConflictField;
	private String versionField;
	private String versionTypeField;
	private String routingField;

	private Object esRetryOnConflict;
	private Object version;
	private Object versionType;
	private Object routing;

	private String pipeline;
	private String opType;
	public String getRoutingField() {
		return routingField;
	}

	public void setRoutingField(String routingField) {
		this.routingField = routingField;
	}



	public String getEsRetryOnConflictField() {
		return esRetryOnConflictField;
	}

	public void setEsRetryOnConflictField(String esRetryOnConflictField) {
		this.esRetryOnConflictField = esRetryOnConflictField;
	}

	public String getVersionField() {
		return versionField;
	}

	public void setVersionField(String versionField) {
		this.versionField = versionField;
	}

	public String getVersionTypeField() {
		return versionTypeField;
	}

	public void setVersionTypeField(String versionTypeField) {
		this.versionTypeField = versionTypeField;
	}

	public Object getEsRetryOnConflict() {
		return esRetryOnConflict;
	}

	public void setEsRetryOnConflict(Object esRetryOnConflict) {
		this.esRetryOnConflict = esRetryOnConflict;
	}

	public Object getVersion() {
		return version;
	}

	public void setVersion(Object version) {
		this.version = version;
	}

	public Object getVersionType() {
		return versionType;
	}

	public void setVersionType(Object versionType) {
		this.versionType = versionType;
	}

	public Object getRouting() {
		return routing;
	}

	public void setRouting(Object routing) {
		this.routing = routing;
	}


	public String getPipeline() {
		return pipeline;
	}

	public void setPipeline(String pipeline) {
		this.pipeline = pipeline;
	}

	public String getOpType() {
		return opType;
	}

	public void setOpType(String opType) {
		this.opType = opType;
	}
}
