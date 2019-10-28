package org.frameworkset.elasticsearch.client.context;
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

import com.frameworkset.orm.annotation.BatchContext;
import com.frameworkset.orm.annotation.ESIndexWrapper;
import org.frameworkset.elasticsearch.client.ColumnData;
import org.frameworkset.elasticsearch.client.DataRefactor;
import org.frameworkset.elasticsearch.client.FieldMeta;
import org.frameworkset.elasticsearch.client.ResultUtil;
import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.db2es.DB2ESImportBuilder;
import org.frameworkset.elasticsearch.client.tran.TranMeta;
import org.frameworkset.elasticsearch.client.tran.TranResultSet;
import org.frameworkset.spi.geoip.IpInfo;

import java.math.BigDecimal;
import java.text.DateFormat;
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

	private Map<String,String> newfieldNames;
	private Map<String,ColumnData> newfieldName2ndColumnDatas;
	private BaseImportConfig esjdbc;
	private TranResultSet jdbcResultSet;
	private BatchContext batchContext;
	private boolean drop;
	private ImportContext importContext;
	public ContextImpl(ImportContext importContext, TranResultSet jdbcResultSet, BatchContext batchContext){
		this.esjdbc = importContext.getImportConfig();
		this.importContext = importContext;
		this.jdbcResultSet = jdbcResultSet;
		this.batchContext = batchContext;
	}
	public TranMeta getMetaData(){
		return jdbcResultSet.getMetaData();

	}
	public Boolean getEsReturnSource() {
		return esjdbc.getEsReturnSource();
	}
	public Boolean getEsDocAsUpsert() {
		return this.esjdbc.getEsDocAsUpsert();
	}
	public List<FieldMeta> getESJDBCFieldValues() {
		return esjdbc.getFieldValues();
	}
	public Boolean getUseLowcase() {
		return esjdbc.getUseLowcase();
	}
	public Object getValue(     int i,String  colName,int sqlType) throws Exception {
		return jdbcResultSet.getValue(i,colName,sqlType);
	}
	public Boolean getUseJavaName() {
		return esjdbc.getUseJavaName();
	}
	public DateFormat getDateFormat(){
		return esjdbc.getFormat();
	}
	public void refactorData() throws Exception{
		DataRefactor dataRefactor = esjdbc.getDataRefactor();
		if(dataRefactor != null){

			dataRefactor.refactor(this);

		}
	}
	public ImportContext getImportContext(){
		return importContext;
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


	public String getDBName(){
		return esjdbc.getDbConfig().getDbName();
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
		return jdbcResultSet.getValue(fieldName);
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

	@Override
	public IpInfo getIpInfo(String fieldName) throws Exception{
		Object _ip = jdbcResultSet.getValue(fieldName);
		if(_ip == null){
			return null;
		}
		if(esjdbc.getGeoIPUtil() != null) {
			return esjdbc.getGeoIPUtil().getAddressMapResult(String.valueOf(_ip));
		}
		return null;
	}

	@Override
	public IpInfo getIpInfoByIp(String ip) {
		if(esjdbc.getGeoIPUtil() != null) {
			return esjdbc.getGeoIPUtil().getAddressMapResult(ip);
		}
		return null;
	}

	/**
	 * 重命名字段和并设置修改后字段值
	 * @param fieldName
	 * @param newFieldName
	 * @param newFieldValue
	 * @throws Exception
	 */
	public void newName2ndData(String fieldName, String newFieldName, Object newFieldValue)throws Exception{
		this.addFieldValue(newFieldName,newFieldValue);//将long类型的时间戳转换为Date类型
		//忽略旧的名称
		if(!fieldName.equals(newFieldName))
			this.addIgnoreFieldMapping(fieldName);
	}


	public BatchContext getBatchContext() {
		return batchContext;
	}

	public  Object getParentId() throws Exception {
		if(esjdbc.getEsParentIdField() != null) {
			return jdbcResultSet.getValue(esjdbc.getEsParentIdField());
		}
		else
			return esjdbc.getEsParentIdValue();
	}

	public Object getRouting() throws Exception{

		Object routing =  jdbcResultSet.getValue(esjdbc.getRoutingField());
		if(routing == null)
			routing = esjdbc.getRoutingValue();
		return routing;
	}
	public Object getEsRetryOnConflict(){
		return esjdbc.getEsRetryOnConflict();
	}
	public ESIndexWrapper getESIndexWrapper(){
		return esjdbc.getEsIndexWrapper();
	}

	public Object getVersion() throws Exception {
		Object version = esjdbc.getEsVersionField() !=null? jdbcResultSet.getValue(esjdbc.getEsVersionField()):esjdbc.getEsVersionValue();
		return version;
	}

	public Object getEsVersionType(){
		return esjdbc.getEsVersionType();
	}
}
