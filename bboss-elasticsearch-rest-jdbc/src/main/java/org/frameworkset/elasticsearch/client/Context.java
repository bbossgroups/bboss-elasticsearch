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

import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/11 17:48
 * @author biaoping.yin
 * @version 1.0
 */
public interface Context {
	public Context addFieldValue(String fieldName,Object value);
	public Context addFieldValue(String fieldName,String dateFormat,Object value);
	public Context addFieldValue(String fieldName,String dateFormat,Object value,String locale,String timeZone);
	public Context addIgnoreFieldMapping(String dbColumnName);
	public ESJDBC getEsjdbc();
	public Object getValue(String fieldName) throws Exception;
	public List<FieldMeta> getFieldValues();
	public Map<String,FieldMeta> getFieldMetaMap();
	public FieldMeta getMappingName(String colName);
	Object getEsId() throws Exception;

	String getEsIdField();
}
