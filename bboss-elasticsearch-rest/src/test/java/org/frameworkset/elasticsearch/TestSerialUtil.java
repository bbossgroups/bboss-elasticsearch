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

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class TestSerialUtil {
	@Test
	public void test(){
		JsonObj jsonObj = new JsonObj();
		jsonObj.setId("2");
		jsonObj.setAgentStarttime(new Date());
		jsonObj.setApplicationName("sss");
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		clientInterface.addDocument("testserial","testserial",jsonObj);
		jsonObj = clientInterface.getDocument("testserial","testserial","2",JsonObj.class);
		System.out.println(SerialUtil.object2json(jsonObj));
		jsonObj = SimpleStringUtil.json2Object(SerialUtil.object2json(jsonObj),JsonObj.class);
		//DateFormateMeta dateFormateMeta = DateFormateMeta.buildDateFormateMeta("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null,"UTC");

		//System.out.println(dateFormateMeta.toDateFormat().format(jsonObj.getAgentStarttime()));
	}
	@Test
	public void testDate() throws ParseException {
		DateFormateMeta dateFormateMeta = DateFormateMeta.buildDateFormateMeta("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null,"UTC");
		String date = dateFormateMeta.toDateFormat().format(new Date());
		System.out.println(date);

		dateFormateMeta = DateFormateMeta.buildDateFormateMeta("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null,"UTC");
		DateFormat dateFormat = dateFormateMeta.toDateFormat();
		Date date_ = dateFormat.parse(date);
		System.out.println(date_);
	}
}
