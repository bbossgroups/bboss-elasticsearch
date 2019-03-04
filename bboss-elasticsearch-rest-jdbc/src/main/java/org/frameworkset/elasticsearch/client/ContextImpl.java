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

import java.math.BigDecimal;
import java.util.*;

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
	private boolean drop;
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
		DB2ESImportBuilder.addFieldValue(fieldValues,fieldName,value);
		return this;
	}

	@Override
	public Context addFieldValue(String fieldName, String dateFormat, Object value) {
		if(this.fieldValues == null)
			fieldValues = new ArrayList<FieldMeta>();
		DB2ESImportBuilder.addFieldValue(fieldValues,fieldName,dateFormat,value,esjdbc.getLocale(),esjdbc.getTimeZone());
		return this;
	}

	@Override
	public Context addFieldValue(String fieldName, String dateFormat, Object value, String locale, String timeZone) {
		if(this.fieldValues == null)
			fieldValues = new ArrayList<FieldMeta>();
		DB2ESImportBuilder.addFieldValue(fieldValues,fieldName,dateFormat,value,locale,timeZone);
		return this;
	}

	@Override
	public Context addIgnoreFieldMapping(String dbColumnName) {
		if(fieldMetaMap == null){
			fieldMetaMap = new HashMap<String,FieldMeta>();
		}
		DB2ESImportBuilder.addIgnoreFieldMapping(fieldMetaMap,dbColumnName);
		return this;
	}

	public ESJDBC getEsjdbc() {
		return esjdbc;
	}

	@Override
	public long getLongValue(String fieldName) throws Exception {
		Object value = this.getValue(fieldName);
		return ResultUtil.longValue(value,0l);

	}

	@Override
	public double getDoubleValue(String fieldName) throws Exception {
		Object value = this.getValue(fieldName);
		return ResultUtil.doubleValue(value,0d);
	}

	@Override
	public float getFloatValue(String fieldName) throws Exception {
		Object value = this.getValue(fieldName);
		return ResultUtil.floatValue(value,0f);
	}

	@Override
	public int getIntegerValue(String fieldName) throws Exception {
		Object value = this.getValue(fieldName);
		return ResultUtil.intValue(value,0);
	}

	@Override
	public Date getDateValue(String fieldName) throws Exception {
		Object value = this.getValue(fieldName);
		if(value == null)
			return null;
		else if(value instanceof Date){
			return (Date)value;

		}
		else if(value instanceof BigDecimal){
			return new Date(((BigDecimal)value).longValue());
		}
		else if(value instanceof Long){
			return new Date(((Long)value).longValue());
		}
		throw new IllegalArgumentException("Convert date value failed:"+value );
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

	public boolean isDrop() {
		return drop;
	}

	public void setDrop(boolean drop) {
		this.drop = drop;
	}
}
