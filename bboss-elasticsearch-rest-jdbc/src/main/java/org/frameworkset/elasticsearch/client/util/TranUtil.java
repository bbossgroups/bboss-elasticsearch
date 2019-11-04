package org.frameworkset.elasticsearch.client.util;
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

import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.util.annotations.DateFormateMeta;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/4 12:57
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class TranUtil {
	public static Date getDateTimeValue(String colName, Object value, ImportContext importContext) throws ESDataImportException {
		if(value == null)
			return null;
		if(value instanceof Date)
			return (Date)value;
		else if(value instanceof Long ){
			return new Date((Long)value);
		}
		else if(value instanceof String){
			DateFormat dateFormat = null;
			if(importContext.getDateFormat()!=null){
				DateFormateMeta dateFormateMeta = DateFormateMeta.buildDateFormateMeta(importContext.getDateFormat(),importContext.getLocale(),importContext.getTimeZone());
				dateFormat = dateFormateMeta.toDateFormat();

			}
			else{
				dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
			}
			try {
				return dateFormat.parse((String)value);
			} catch (ParseException e) {
				throw new ESDataImportException("Illegment colName["+colName+"] date value:"+(String)value,e);
			}
		}
		else{
			throw new ESDataImportException("Illegment colName["+colName+"] date value:"+(String)value);
		}
	}
}
