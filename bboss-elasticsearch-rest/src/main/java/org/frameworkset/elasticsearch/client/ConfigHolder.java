package org.frameworkset.elasticsearch.client;
/**
 * Copyright 2022 bboss
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
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.template.ConfigDSLUtil;
import org.frameworkset.elasticsearch.template.TemplateContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2022/7/1
 * @author biaoping.yin
 * @version 1.0
 */
public class ConfigHolder {
	private Map<String, ConfigDSLUtil> configDSLUtils = new HashMap<String, ConfigDSLUtil>();
	private DaemonThread damon = null;
	private String holderName;
	public ConfigHolder(String holderName){
		this.holderName = holderName;
	}

	public ConfigHolder(){
		this.holderName = "Default";
	}
	public ConfigDSLUtil getConfigDSLUtil(String templateFile){
		return getConfigDSLUtil(ElasticSearchHelper.getDslfileMappingDir(),templateFile);
	}
	public ConfigDSLUtil getConfigDSLUtil(TemplateContainer templateContainer) {
		String namespace = templateContainer.getNamespace();
		ConfigDSLUtil sqlUtil = configDSLUtils.get(namespace);
		if(sqlUtil != null)
			return sqlUtil;
		synchronized(configDSLUtils)
		{
			sqlUtil = configDSLUtils.get(namespace);
			if(sqlUtil != null)
				return sqlUtil;
			sqlUtil = new ConfigDSLUtil(this,templateContainer);

			configDSLUtils.put(namespace, sqlUtil);
			checkESUtil(sqlUtil);


		}

		return sqlUtil;
	}
	public ConfigDSLUtil getConfigDSLUtil(String dslMappingDir, String templateFile){
		ConfigDSLUtil configDSLUtil = configDSLUtils.get(templateFile);
		if(configDSLUtil != null)
			return configDSLUtil;
		synchronized(configDSLUtils)
		{
			configDSLUtil = configDSLUtils.get(templateFile);
			if(configDSLUtil != null)
				return configDSLUtil;
			configDSLUtil = new ConfigDSLUtil( this, dslMappingDir,templateFile);

			configDSLUtils.put(templateFile, configDSLUtil);
			checkESUtil(configDSLUtil);

		}

		return configDSLUtil;
//		return configDSLUtils.get(name);
	}
	public void destory()
	{
		if(configDSLUtils != null)
		{
			Iterator<Map.Entry<String, ConfigDSLUtil>> it = configDSLUtils.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry<String, ConfigDSLUtil> entry = it.next();
				entry.getValue()._destroy();
			}
			configDSLUtils.clear();
//			configDSLUtils = null;
		}
	}
	public List<String> getTemplateFiles()
	{
		Iterator<String> it = configDSLUtils.keySet().iterator();
		List<String> files = new ArrayList<String>();
		while(it.hasNext())
			files.add(it.next());
		return files;
	}


	public void stopmonitor()
	{
		try {
			if(damon != null)
			{
				damon.stopped();
				damon = null;
			}
		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}

	private static Logger log = LoggerFactory.getLogger(ConfigDSLUtil.class);
	private Object lock = new Object();
	private void checkESUtil(ConfigDSLUtil configDSLUtil){
		TemplateContainer templateContainer = configDSLUtil.getTemplateContext();
		long refresh_interval = ElasticSearchHelper.getDslfileRefreshInterval();
		if(refresh_interval > 0)
		{
			if(damon == null)
			{
				synchronized(lock)
				{
					if(damon == null)
					{
						damon = new DaemonThread(refresh_interval,holderName+ " DSL Refresh Worker");
						damon.start();

					}
				}
			}
			templateContainer.monitor(damon,new ConfigDSLUtil.ResourceTempateRefresh(configDSLUtil));
//			damon.addFile(fileUrl,templateNamespace, new ResourceTempateRefresh(sqlutil));
		}
		else{
			log.debug(holderName+ " DSL Refresh Interval:"+refresh_interval+",ignore hotload DSL ["+templateContainer.getNamespace()+"]");
		}

	}


}
