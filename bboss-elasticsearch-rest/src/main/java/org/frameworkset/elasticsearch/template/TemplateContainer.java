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

import java.util.Set;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/17 14:36
 * @author biaoping.yin
 * @version 1.0
 */
public interface TemplateContainer {
	public final String NAME_perKeyDSLStructionCacheSize = "perKeyDSLStructionCacheSize";
	public final String NAME_alwaysCacheDslStruction = "alwaysCacheDslStruction";
	public final String NAME_templateFile = "templateFile";
	public final String NAME_templateName = "templateName";
	public final String NAME_istpl = "istpl";
	public final String NAME_cache = "cacheDsl";
	public final String NAME_multiparser = "multiparser";
	String getNamespace();

	Set<String> getTempalteNames();

	TemplateMeta getProBean(String key);

	void destroy(boolean b);

	void reinit(ESUtil esUtil);

	/**
	 * 命名空间对应的全局每一个template对应的dsl语法缓冲区大小
	 * @return
	 */
	int getPerKeyDSLStructionCacheSize();

	/**
	 * 命名空间对应的全局是否开启每一个template对应的dsl语法缓冲机制
	 * @return
	 */
	boolean isAlwaysCacheDslStruction();

	void monitor(DaemonThread daemonThread, ResourceInitial resourceTempateRefresh);
}
