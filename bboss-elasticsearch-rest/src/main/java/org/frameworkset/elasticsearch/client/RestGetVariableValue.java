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

import com.frameworkset.orm.annotation.ESIndexWrapper;
import org.frameworkset.util.ClassUtil;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/5/6 21:36
 * @author biaoping.yin
 * @version 1.0
 */
public class RestGetVariableValue implements ESIndexWrapper.GetVariableValue {
	private ClassUtil.ClassInfo beanInfo;
	private Object params;
	public RestGetVariableValue(ClassUtil.ClassInfo beanInfo,Object params){
		this.beanInfo = beanInfo;
		this.params = params;
	}
	@Override
	public Object getValue(String property) {
		return beanInfo.getPropertyValue(params,property);
	}
}
