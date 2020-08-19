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

import org.frameworkset.spi.assemble.Pro;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/17 14:51
 * @author biaoping.yin
 * @version 1.0
 */
public class AOPTemplateMeta implements TemplateMeta{
	private Pro pro;
	public AOPTemplateMeta(Pro pro){
		this.pro = pro;
	}


	/**
	 * 返回模板名称
	 * @return
	 */
	public String getName(){
		return pro.getName();
	}

	@Override
	public Boolean getCache() {
		return pro.getBooleanExtendAttribute(TemplateContainer.NAME_cache,true);//标识缓存dsl解析语法树;
	}


	@Override
	public boolean isEscapeQuoted() {
		return pro.isEscapeQuoted();
	}

	@Override
	public String getReferenceNamespace() {
		return (String)pro.getExtendAttribute(TemplateContainer.NAME_templateFile);
	}

	@Override
	public String getReferenceTemplateName() {
		return (String)pro.getExtendAttribute(TemplateContainer.NAME_templateName);
	}

	@Override
	public Boolean getVtpl() {
		return pro.getBooleanExtendAttribute(TemplateContainer.NAME_istpl,true);//标识sql语句是否为velocity模板;
	}

	@Override
	public Boolean getMultiparser() {
		return pro.getBooleanExtendAttribute(TemplateContainer.NAME_multiparser,getVtpl());//如果sql语句为velocity模板，则在批处理时是否需要每条记录都需要分析sql语句;
	}

	@Override
	public Object getDslTemplate() {
		return pro.getObject();
	}


}
