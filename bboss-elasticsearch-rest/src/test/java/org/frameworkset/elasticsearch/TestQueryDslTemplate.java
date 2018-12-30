package org.frameworkset.elasticsearch;/*
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

import org.frameworkset.elasticsearch.template.ESTemplateHelper;
import org.frameworkset.elasticsearch.template.ESUtil;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestQueryDslTemplate {
	@Test
	public void testdslParser(){
		ESUtil esUtil = ESUtil.getInstance("estrace/testquerydsl.xml");
		Map<String,Object > params = new HashMap<String,Object>();
		params.put("application","testweb");
		params.put("startTime",123456);
		params.put("endTime",456789);
		params.put("date",new Date());
		System.out.println(ESTemplateHelper.evalTemplate(esUtil,"querySqlTraces",params));
	}

	@Test
	public void testBeandslParser(){
		ESUtil esUtil = ESUtil.getInstance("estrace/testquerydsl.xml");
		DSLParma params = new DSLParma();
		params.setApplication("testweb");
		params.setStartTime(123456);
		params.setEndTime(456789);
		params.setDate(new Date());
		System.out.println(ESTemplateHelper.evalTemplate(esUtil,"querySqlTraces",params));

		params = new DSLParma();
		params.setApplication("testweb1");
		params.setStartTime(123456);
		params.setEndTime(456789);
		params.setDate(new Date());
		System.out.println(ESTemplateHelper.evalTemplate(esUtil,"querySqlTraces",params));

		params = new DSLParma();
		params.setApplication("testweb2");
		params.setStartTime(123456);
		params.setEndTime(456789);
		params.setDate(new Date());
		System.out.println(ESTemplateHelper.evalTemplate(esUtil,"querySqlTraces",params));

		params = new DSLParma();
		params.setApplication("testweb3");
		params.setStartTime(123456);
		params.setEndTime(456789);
		params.setDate(new Date());
		System.out.println(ESTemplateHelper.evalTemplate(esUtil,"querySqlTraces",params));
	}


}
