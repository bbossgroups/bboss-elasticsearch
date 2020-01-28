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

import com.frameworkset.util.DaemonThread;
import com.frameworkset.util.ResourceInitial;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.DefaultApplicationContext;
import org.frameworkset.spi.assemble.Pro;
import org.frameworkset.spi.runtime.BaseStarter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/17 14:36
 * @author biaoping.yin
 * @version 1.0
 */
public class AOPTemplateContainerImpl implements TemplateContainer{
	private BaseApplicationContext templatecontext;
	public AOPTemplateContainerImpl( BaseApplicationContext templatecontext) {
		this.templatecontext = templatecontext;
	}
	public AOPTemplateContainerImpl(String dslpath){
		templatecontext = DefaultApplicationContext.getApplicationContext(dslpath);
	}

	public String getNamespace(){
		return templatecontext.getConfigfile();
	}

	public Set getTempalteNames(){
		return templatecontext.getPropertyKeys();
	}

	public TemplateMeta getProBean(String templateName){
		return new AOPTemplateMeta(templatecontext.getProBean(templateName));
	}

	public void destroy(boolean clearContext){
		templatecontext.destroy(clearContext);
	}

	public int getPerKeyDSLStructionCacheSize(){
		return templatecontext.getIntProperty(TemplateContainer.NAME_perKeyDSLStructionCacheSize,ESUtil.defaultPerKeyDSLStructionCacheSize);
	}
	public boolean isAlwaysCacheDslStruction(){
		return templatecontext.getBooleanProperty(TemplateContainer.NAME_alwaysCacheDslStruction,ESUtil.defaultAlwaysCacheDslStruction);
	}
	public synchronized void reinit(ESUtil esUtil){
		String file = templatecontext.getConfigfile();
		templatecontext.removeCacheContext();
		ESSOAFileApplicationContext essoaFileApplicationContext = new ESSOAFileApplicationContext(file);
		if(essoaFileApplicationContext.getParserError() == null) {
//			esUtil.clearTemplateDatas();
			templatecontext.destroy(false);
			templatecontext = essoaFileApplicationContext;
//			templatecontext = new ESSOAFileApplicationContext(file);
			esUtil.buildTemplateDatas(this);
//			trimValues();
//			destroyed = false;
		}
		else{
			templatecontext.restoreCacheContext();
		}
	}
	public void monitor(DaemonThread daemonThread,ResourceInitial resourceTempateRefresh){
		daemonThread.addFile(templatecontext.getConfigFileURL(),this.getNamespace(), resourceTempateRefresh);
	}

	public List<TemplateMeta> getTemplateMetas(final String namespace){

		final List<TemplateMeta> templateMetaList = new ArrayList<TemplateMeta>();
		this.templatecontext.start(new BaseStarter() {
			public void start(Pro pro, BaseApplicationContext ioc) {
				Object _service = ioc.getBeanObject(pro.getName());
				if (_service == null || pro.getName().equals(TemplateContainer.NAME_perKeyDSLStructionCacheSize) || pro.getName().equals(TemplateContainer.NAME_alwaysCacheDslStruction))
					return;
				BaseTemplateMeta baseTemplateMeta = new BaseTemplateMeta();
				baseTemplateMeta.setName(pro.getName());
				baseTemplateMeta.setNamespace(namespace);
				String templateFile = (String) pro.getExtendAttribute(TemplateContainer.NAME_templateFile);
				if (templateFile == null) {
					Object o = pro.getObject();
					if (o != null && o instanceof String) {

						String value = (String) o;
						baseTemplateMeta.setDslTemplate(value);
						baseTemplateMeta.setVtpl(pro.getBooleanExtendAttribute(TemplateContainer.NAME_istpl, true));//如果sql语句为velocity模板，则在批处理时是否需要每条记录都需要分析sql语句;
						//标识sql语句是否为velocity模板;
						baseTemplateMeta.setMultiparser(pro.getBooleanExtendAttribute(TemplateContainer.NAME_multiparser, baseTemplateMeta.getVtpl()));
						templateMetaList.add(baseTemplateMeta);
					}
				} else {
					String templateName = (String) pro.getExtendAttribute(TemplateContainer.NAME_templateName);
					;
					if (templateName == null) {
						logger.warn(new StringBuilder().append("Ignore this DSL template ")
								.append(pro.getName()).append(" in the DSl file ")
								.append(getNamespace())
								.append(" is defined as a reference to the DSL template in another configuration file ")
								.append(templateFile)
								.append(", but the name of the DSL template statement to be referenced is not specified by the templateName attribute, for example:\r\n")
								.append("<property name= \"querySqlTraces\"\r\n")
								.append("templateFile= \"esmapper/estrace/ESTracesMapper.xml\"\r\n")
								.append("templateName= \"queryTracesByCriteria\"/>").toString());
					} else {
						baseTemplateMeta.setReferenceNamespace(templateFile);
						baseTemplateMeta.setReferenceTemplateName(templateName);
						baseTemplateMeta.setVtpl(false);
						baseTemplateMeta.setMultiparser(false);
						templateMetaList.add(baseTemplateMeta);
					}
				}
			}
		});
		return templateMetaList;


	}
}
