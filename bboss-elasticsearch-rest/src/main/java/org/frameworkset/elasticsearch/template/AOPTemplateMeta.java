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

	@Override
	public Object getExtendAttribute(String extendAttribute) {
		return pro.getExtendAttribute(extendAttribute);
	}

	@Override
	public Object getObject() {
		return pro.getObject();
	}

	@Override
	public boolean getBooleanExtendAttribute(String extendAttribute, boolean defaultValue) {
		return pro.getBooleanExtendAttribute(extendAttribute,defaultValue);
	}
}
