package org.frameworkset.elasticsearch.serial;
/**
 * Copyright 2020 bboss
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

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ConfigRestClientUtil;
import org.junit.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2020/9/15 15:50
 * @author biaoping.yin
 * @version 1.0
 */
public class CharEscapeUtilTest {
	@Test
	public void escape(){
		StringWriter writer = new StringWriter();
		CharEscapeUtil charEscapeUtil = new CustomCharEscapeUtil(writer,false);
		charEscapeUtil.writeString("/+\"\\", true);
		System.out.println(writer.toString());
		writer = new StringWriter();
		charEscapeUtil = new CustomCharEscapeUtil(writer,true);
		charEscapeUtil.writeString("&/+\"\\", true);
		System.out.println(writer.toString());
		ClientInterface util = (ConfigRestClientUtil) ElasticSearchHelper.getConfigRestClientUtil("demo7.xml");
		Map params = new HashMap();
		params.put("aaa","_&/+\"\\.");
		System.out.println(util.evalConfigDsl("testesencode",params));
	}
}
