package org.frameworkset.elasticsearch.template;
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
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/22 21:01
 * @author biaoping.yin
 * @version 1.0
 */
public class BaseTemplateMeta implements TemplateMeta{
	private String name;
	private String namespace;
	private String dslTemplate;
	private String referenceNamespace;
	private String referenceTemplateName;
	private Boolean vtpl = true;

	public boolean isParsered() {
		return parsered;
	}

	public void setParsered(boolean parsered) {
		this.parsered = parsered;
	}

	private boolean parsered;


	private Boolean multiparser;
	@Override
	public String getReferenceNamespace() {
		return null;
	}

	@Override
	public String getReferenceTemplateName() {
		return null;
	}

	public void setMultiparser(Boolean multiparser) {
		this.multiparser = multiparser;
	}

	public void setVtpl(Boolean vtpl) {
		this.vtpl = vtpl;
	}

	@Override
	public Boolean getVtpl() {
		return vtpl;
	}

	@Override
	public Boolean getMultiparser() {
		return true;
	}

	public Object getDslTemplate(){
		return dslTemplate;
	}
	/**
	 * 返回模板名称
	 * @return
	 */
	public String getName(){
		return name;
	}

	public void setDslTemplate(String newDslTemplate) {
		this.dslTemplate = newDslTemplate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setReferenceNamespace(String referenceNamespace) {
		this.referenceNamespace = referenceNamespace;
	}

	public void setReferenceTemplateName(String referenceTemplateName) {
		this.referenceTemplateName = referenceTemplateName;
	}

}
