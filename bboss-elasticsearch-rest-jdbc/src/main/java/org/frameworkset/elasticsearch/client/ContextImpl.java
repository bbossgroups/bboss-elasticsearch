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

import java.util.ArrayList;
import java.util.HashMap;
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
public class ContextImpl implements Context {
	private List<FieldMeta> fieldValues ;
	private Map<String,FieldMeta> fieldMetaMap;
	private ESJDBC esjdbc;
	public ContextImpl(ESJDBC esjdbc){
		this.esjdbc = esjdbc;
	}
	public List<FieldMeta> getFieldValues(){
		return this.fieldValues;
	}
	public Map<String,FieldMeta> getFieldMetaMap(){
		return this.fieldMetaMap;
	}
	@Override
	public Context addFieldValue(String fieldName, Object value) {
		if(this.fieldValues == null)
			fieldValues = new ArrayList<FieldMeta>();
		ImportBuilder.addFieldValue(fieldValues,fieldName,value);
		return this;
	}

	@Override
	public Context addFieldValue(String fieldName, String dateFormat, Object value) {
		if(this.fieldValues == null)
			fieldValues = new ArrayList<FieldMeta>();
		ImportBuilder.addFieldValue(fieldValues,fieldName,dateFormat,value,esjdbc.getLocale(),esjdbc.getTimeZone());
		return this;
	}

	@Override
	public Context addFieldValue(String fieldName, String dateFormat, Object value, String locale, String timeZone) {
		if(this.fieldValues == null)
			fieldValues = new ArrayList<FieldMeta>();
		ImportBuilder.addFieldValue(fieldValues,fieldName,dateFormat,value,locale,timeZone);
		return this;
	}

	@Override
	public Context addIgnoreFieldMapping(String dbColumnName) {
		if(fieldMetaMap == null){
			fieldMetaMap = new HashMap<String,FieldMeta>();
		}
		ImportBuilder.addIgnoreFieldMapping(fieldMetaMap,dbColumnName);
		return this;
	}

	public ESJDBC getEsjdbc() {
		return esjdbc;
	}
	public Object getValue(String fieldName) throws Exception{
		return esjdbc.getValue(fieldName);
	}
	public FieldMeta getMappingName(String colName){
		if(fieldMetaMap != null) {
			FieldMeta fieldMeta = this.fieldMetaMap.get(colName.toLowerCase());
			if (fieldMeta != null) {
				return fieldMeta;
			}
		}
		return esjdbc.getMappingName(colName);
	}

	@Override
	public Object getEsId() throws Exception {

		return esjdbc.getEsIdGenerator().genId(this);
	}

	@Override
	public String getEsIdField() {
		return esjdbc.getEsIdField();
	}
}
