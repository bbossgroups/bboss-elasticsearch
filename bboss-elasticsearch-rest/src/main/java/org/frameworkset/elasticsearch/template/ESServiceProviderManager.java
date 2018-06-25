/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.frameworkset.elasticsearch.template;

import org.frameworkset.elasticsearch.serial.CharEscapeUtil;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.assemble.ServiceProviderManager;

public class ESServiceProviderManager extends ServiceProviderManager {
	public static String var_pre = "@{";
	public static String var_end = "}";
	public static String jsonEscapePre = "@\"\"\"";
	public static String jsonEscapeEnd = "\"\"\"";
	public ESServiceProviderManager(BaseApplicationContext applicationContext, String charset) {
		super(applicationContext, charset);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getEscapePre(){
		return jsonEscapePre;
	}
	@Override
	public String getEscapeEnd(){
		return jsonEscapeEnd;
	}

	public ESServiceProviderManager(BaseApplicationContext applicationContext) {
		super(applicationContext);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getVarpre(){
		return var_pre;
	}
	@Override
	public String getVarend(){
		return var_end;
	}
	@Override
	public boolean findVariableFromSelf(){
		return true;
	}
	@Override
	public void escapeValue(String value,StringBuilder builder){
		CharEscapeUtil charEscapeUtil = new CharEscapeUtil(new BBossStringWriter(builder));
		charEscapeUtil.writeString(value,true);
	}

}
