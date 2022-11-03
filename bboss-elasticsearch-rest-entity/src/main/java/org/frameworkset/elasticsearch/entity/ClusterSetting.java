package org.frameworkset.elasticsearch.entity;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/24 11:43
 * @author biaoping.yin
 * @version 1.0
 */
public class ClusterSetting implements Serializable {
	/**
	 * 场景1
	 */
	private boolean persistent;
	private String key;
	private Object value;
	/**
	 * 场景2
	 */
	private Map<String,Object> transientSettings;
	private Map<String,Object> persistentSettings;

	public boolean containTransientSettings(){
		return transientSettings != null && transientSettings.size() > 0;
	}
	public boolean containPersistentSettings(){
		return persistentSettings != null && persistentSettings.size() > 0;
	}
	public ClusterSetting addTransientSetting(String name,Object value){
		if(transientSettings == null){
			transientSettings = new LinkedHashMap<>();
		}
		transientSettings.put(name,value);
		return this;
	}

	public ClusterSetting addPersistentSetting(String name,Object value){
		if(persistentSettings == null){
			persistentSettings = new LinkedHashMap<>();
		}
		persistentSettings.put(name,value);
		return this;
	}

	public Map<String, Object> getPersistentSettings() {
		return persistentSettings;
	}

	public Map<String, Object> getTransientSettings() {
		return transientSettings;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
