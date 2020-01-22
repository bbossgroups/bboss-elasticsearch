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
import org.frameworkset.spi.assemble.AOPValueHandler;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.spi.assemble.ValueContainer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/17 14:36
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseTemplateContainerImpl implements TemplateContainer{
//	protected ESUtil esUtil;
	protected String namespace;
	protected Map<String,TemplateMeta> templateMetas;
	public BaseTemplateContainerImpl(String namespace) {
//		this.esUtil = esUtil;
		this.namespace = namespace;
	}

//	public void setEsUtil(ESUtil esUtil) {
//		this.esUtil = esUtil;
//	}

	public String getNamespace(){
		return namespace;
	}


	/**
	 * 根据命名空间获取对应的
	 * @param namespace
	 * @return
	 */
	protected  abstract Map<String,TemplateMeta> loadTemplateMetas(String namespace);
	private Map<String,TemplateMeta> _loadTemplateMetas(String namespace){
		final Map<String,TemplateMeta> templateMetaMap = loadTemplateMetas(namespace);
		if(templateMetaMap != null && templateMetaMap.size() > 0){
			//对dsl模板进行宏变量替换和特殊字符处理
			Iterator<Map.Entry<String, TemplateMeta>> iterator = templateMetaMap.entrySet().iterator();
			final PropertiesContainer configProperties = new PropertiesContainer();
			final ESServiceProviderManager esServiceProviderManager = new ESServiceProviderManager();
			final ValueContainer valueContainer = new ValueContainer (){
				@Override
				public String getMacroVariableValue(String text) {
					BaseTemplateMeta inTemplateMeta = (BaseTemplateMeta)templateMetaMap.get(text);
					if(inTemplateMeta != null){
						if(inTemplateMeta.isParsered())
							return (String)inTemplateMeta.getDslTemplate();
						else{ //递归分析引用片段
							//todo:防止递归引用
							evalValue(inTemplateMeta, configProperties,esServiceProviderManager,this );

							return (String)inTemplateMeta.getDslTemplate();
						}
					}
					else {//引用片段值为null
						return null;
					}
				}
			};
			while(iterator.hasNext()){
				Map.Entry<String, TemplateMeta> templateMetaEntry = iterator.next();
				BaseTemplateMeta templateMeta = (BaseTemplateMeta)templateMetaEntry.getValue();
				evalValue(templateMeta, configProperties,esServiceProviderManager,valueContainer );
				templateMeta.setParsered(true);
			}
		}
		return templateMetaMap;
	}
	public void evalValue(BaseTemplateMeta templateMeta, PropertiesContainer configProperties, AOPValueHandler valueHandler, ValueContainer valueContainer)
	{
		String value = (String)templateMeta.getDslTemplate();
		//先进行特殊字符转换
		if(value != null && !value.equals("")){
			value = configProperties.escapeValue(value, valueHandler);
		}
		//再进行片段解析
		String resultValue = configProperties.evalValue(value, valueHandler,valueContainer);

		templateMeta.setDslTemplate(resultValue);
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
	public Set getTempalteNames(){
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
		return ESUtil.defaultPerKeyDSLStructionCacheSize;
	}

	public boolean isAlwaysCacheDslStruction(){
		return ESUtil.defaultAlwaysCacheDslStruction;
	}

	public synchronized void reinit(ESUtil esUtil){
		try {
			Map<String, TemplateMeta> temp = this._loadTemplateMetas(namespace);
			if(temp != null){
				this.templateMetas = temp;
			}
			else {
				templateMetas.clear();
			}
			esUtil.clearTemplateDatas();
			esUtil.buildTemplateDatas(this);
		}
		catch (Exception e){

		}


	}
	public void monitor(DaemonThread daemonThread, ResourceInitial resourceTempateRefresh){
		daemonThread.addResource(new ResourceNameSpace() {
			@Override
			public long getLastModifyTimestamp() {
				return getLastModifyTime( namespace);
			}
		},resourceTempateRefresh);
	}
}
