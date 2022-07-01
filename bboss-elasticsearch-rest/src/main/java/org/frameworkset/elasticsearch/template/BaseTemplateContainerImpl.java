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

import com.frameworkset.daemon.ResourceNameSpace;
import com.frameworkset.util.DaemonThread;
import com.frameworkset.util.ResourceInitial;
import org.frameworkset.elasticsearch.client.ConfigHolder;
import org.frameworkset.spi.assemble.AOPValueHandler;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.spi.assemble.ValueContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/17 14:36
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseTemplateContainerImpl implements TemplateContainer{
	private static Logger logger = LoggerFactory.getLogger(BaseTemplateContainerImpl.class);
	protected String namespace;
	protected Map<String,TemplateMeta> templateMetas;
	protected ConfigHolder configHolder;
	public BaseTemplateContainerImpl(String namespace) {
		this.namespace = namespace;
	}

	public synchronized void setConfigHolder(ConfigHolder configHolder) {
		if(this.configHolder == null)
			this.configHolder = configHolder;
	}

	public String getNamespace(){
		return namespace;
	}


	/**
	 * 根据命名空间获取对应的
	 * @param namespace
	 * @return
	 */
	protected  abstract Map<String,TemplateMeta> loadTemplateMetas(String namespace);
	/**
	public Object getRealPropertyValue(TemplateMeta pro){
		String templateFile = (String)pro.getReferenceNamespace();
		if(templateFile == null)
			return pro.getDslTemplate();
		else{
			String templateName = (String)pro.getReferenceTemplateName();
			if(templateName == null)
			{
				logger.warn(new StringBuilder().append("The DSL template ")
						.append(pro.getName()).append(" in the DSl namespace ")
						.append(templateFile)
						.append(" is defined as a reference to the DSL template in another configuration namespace ")
						.append(templateFile)
						.append(", but the name of the DSL template statement to be referenced is not specified by the templateName attribute[")
						.append(templateName).append("]").toString());
				return null;
			}
			else
			{
				ESUtil.ESRef ref = new ESUtil.ESRef(templateName,templateFile,pro.getName());
				return ref.getTemplate();

			}
		}
	}*/
	private Map<String,TemplateMeta> _loadTemplateMetas(String namespace){
		final Map<String,TemplateMeta> templateMetaMap = loadTemplateMetas(namespace);
		if(templateMetaMap != null && templateMetaMap.size() > 0){
			//对dsl模板进行宏变量替换和特殊字符处理
			Iterator<Map.Entry<String, TemplateMeta>> iterator = templateMetaMap.entrySet().iterator();
			final PropertiesContainer configProperties = new PropertiesContainer();
			final ESServiceProviderManager esServiceProviderManager = new ESServiceProviderManager(configHolder);
			final ValueContainer valueContainer = new ValueContainer(){
				@Override
				public String getMacroVariableValue(List<String> parentLinks,String text) {
					BaseTemplateMeta inTemplateMeta = (BaseTemplateMeta)templateMetaMap.get(text);
					if(inTemplateMeta != null){
						if(inTemplateMeta.isParsered())
							return (String)inTemplateMeta.getDslTemplate();
						else{ //递归分析引用片段

							evalValue(parentLinks,inTemplateMeta, configProperties,esServiceProviderManager,this );

							return (String)inTemplateMeta.getDslTemplate();
						}
					}
					else {//引用片段值未定义，返回null
						return null;
					}
				}
			};
			List<String> parentLinks = null;
			while(iterator.hasNext()){
				Map.Entry<String, TemplateMeta> templateMetaEntry = iterator.next();
				BaseTemplateMeta templateMeta = (BaseTemplateMeta)templateMetaEntry.getValue();
				if(templateMeta.isParsered()){
					continue;
				}
				parentLinks = new ArrayList<String>();
				parentLinks.add(templateMeta.getName());
				evalValue(parentLinks,templateMeta, configProperties,esServiceProviderManager,valueContainer );
			}
		}
		return templateMetaMap;
	}


	public void evalValue(List<String> parentLinks,BaseTemplateMeta templateMeta, PropertiesContainer configProperties, AOPValueHandler valueHandler, ValueContainer valueContainer)
	{
		String value = (String)templateMeta.getDslTemplate();
		//先进行特殊字符转换
		if(value != null && !value.equals("")){
			value = configProperties.escapeValue(value, valueHandler,templateMeta.isEscapeQuoted());
		}
		//再进行片段解析
		String resultValue = configProperties.evalValue(parentLinks,value, valueHandler,valueContainer);

		templateMeta.setDslTemplate(resultValue);
		templateMeta.setParsered(true);
	}
	protected abstract long getLastModifyTime(String namespace);
	private synchronized void init(){
		if(templateMetas == null){
			templateMetas = _loadTemplateMetas(namespace);
			if(templateMetas == null){
				templateMetas = new HashMap<String, TemplateMeta>(0);
			}
		}

	}
	public Set<String> getTempalteNames(){
		if(templateMetas == null){
			init();
		}
		return templateMetas.keySet();
	}

	public TemplateMeta getProBean(String templateName){
		if(templateMetas == null){
			init();
		}
		return templateMetas.get(templateName);
	}

	public void destroy(boolean clearContext){
		if(templateMetas != null)
			templateMetas.clear();
	}

	public int getPerKeyDSLStructionCacheSize(){
		return ConfigDSLUtil.defaultPerKeyDSLStructionCacheSize;
	}

	public boolean isAlwaysCacheDslStruction(){
		return ConfigDSLUtil.defaultAlwaysCacheDslStruction;
	}

	public synchronized void reinit(ConfigDSLUtil configDSLUtil){
		try {
			Map<String, TemplateMeta> temp = this._loadTemplateMetas(namespace);
			if(temp != null){
				this.templateMetas = temp;
			}
			else {
				templateMetas.clear();
			}
//			esUtil.clearTemplateDatas();
			configDSLUtil.buildTemplateDatas(this);
		}
		catch (Exception e){
			logger.warn("reinit namespace"+namespace+" failed:",e);
		}


	}
	public void monitor(DaemonThread daemonThread, ResourceInitial resourceTempateRefresh){
		ResourceNameSpace resourceNameSpace = new ResourceNameSpace() {
			@Override
			public long getLastModifyTimestamp() {
				return getLastModifyTime( namespace);
			}
		};
		resourceNameSpace.setNameSpace(namespace);
		daemonThread.addResource(resourceNameSpace,resourceTempateRefresh);
	}
}
