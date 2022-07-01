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

import org.frameworkset.elasticsearch.client.ConfigHolder;
import org.frameworkset.spi.SOAFileApplicationContext;
import org.frameworkset.spi.assemble.ServiceProviderManager;

import java.net.URL;

public class ESSOAFileApplicationContext extends SOAFileApplicationContext {

	private ConfigHolder configHolder;
	public ESSOAFileApplicationContext(ConfigHolder configHolder, String baseDir, String file) {
		super(baseDir,file,false);
		this.configHolder = configHolder;
	}

	public ESSOAFileApplicationContext(ConfigHolder configHolder,String baseDir,URL file, String path) {
		super(baseDir,file, path);
		this.configHolder = configHolder;
	}

	public ESSOAFileApplicationContext(ConfigHolder configHolder,String baseDir,String file, String charset) {
		super(baseDir,file, charset);
		this.configHolder = configHolder;
	}
	
	@Override
	protected ServiceProviderManager _getServiceProviderManager() {
		// TODO Auto-generated method stub
		return new ESServiceProviderManager(configHolder,this);
	}
	
	protected ServiceProviderManager _getServiceProviderManagerWithCharset(String charset)
	{
		return new ESServiceProviderManager(configHolder,this,charset);
	}

}
