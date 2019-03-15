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

import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.assemble.LinkConfigFile;
import org.frameworkset.spi.assemble.Pro;
import org.frameworkset.spi.assemble.ProviderParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/15 10:48
 * @author biaoping.yin
 * @version 1.0
 */
public class ESSOAProviderParser  extends ProviderParser {
	private static Logger logger = LoggerFactory.getLogger(ESSOAProviderParser.class);
	public ESSOAProviderParser(BaseApplicationContext applicationContext, String file, LinkConfigFile linkfile) {
		super(applicationContext, file, linkfile);
	}

	public ESSOAProviderParser(BaseApplicationContext applicationContext) {
		super(applicationContext);
	}

//	@Override
//	public Pro _getRealProperty(String name) {
//		return super._getRealProperty(name);
//	}
	@Override
	public Object getRealPropertyValue(Pro pro){
		String templateFile = (String)pro.getExtendAttribute("templateFile");
		if(templateFile == null)
			return pro.getValue();
		else{
			String templateName = (String)pro.getExtendAttribute("templateName");
			if(templateName == null)
			{
				logger.warn(new StringBuilder().append("The DSL template ")
						.append(pro.getName()).append(" in the DSl file ")
						.append(applicationContext.getConfigfile())
						.append(" is defined as a reference to the DSL template in another configuration file ")
						.append(templateFile)
						.append(", but the name of the DSL template statement to be referenced is not specified by the templateName attribute, for example:\r\n")
						.append("<property name= \"querySqlTraces\"\r\n")
						.append("templateFile= \"esmapper/estrace/ESTracesMapper.xml\"\r\n")
						.append("templateName= \"queryTracesByCriteria\"/>").toString());
				return null;
			}
			else
			{
				ESUtil.ESRef ref = new ESUtil.ESRef(templateName,templateFile,pro.getName());
				return ref.getTemplate();

			}
		}
	}
}
