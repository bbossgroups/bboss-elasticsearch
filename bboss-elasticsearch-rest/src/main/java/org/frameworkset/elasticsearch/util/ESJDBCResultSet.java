package org.frameworkset.elasticsearch.util;
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

import java.text.DateFormat;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/6 22:22
 * @author biaoping.yin
 * @version 1.0
 */
public interface ESJDBCResultSet {


	public String getEsIdField() ;

	public String getEsParentIdField() ;
	public String getEsParentIdValue() ;

	public String getRoutingField() ;

	public String getRoutingValue() ;

	public Boolean getEsDocAsUpsert() ;

	public Integer getEsRetryOnConflict() ;

	public Boolean getEsReturnSource() ;

	public String getEsVersionField() ;
	public Object getEsVersionValue() ;
	public String getEsVersionType() ;

	public Boolean getUseJavaName() ;

	public String getDateFormat() ;

	public String getLocale() ;

	public String getTimeZone() ;

	public DateFormat getFormat() ;
	public Object getValue(String fieldName) throws Exception;
}
