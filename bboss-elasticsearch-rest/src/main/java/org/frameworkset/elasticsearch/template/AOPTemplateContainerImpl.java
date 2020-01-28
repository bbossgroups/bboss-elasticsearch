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
}
