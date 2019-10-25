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

import com.frameworkset.orm.annotation.ESIndexWrapper;
import org.frameworkset.elasticsearch.client.DBConfig;
import org.frameworkset.elasticsearch.client.DataRefactor;
import org.frameworkset.elasticsearch.client.DataTranPlugin;
import org.frameworkset.elasticsearch.client.WrapedExportResultHandler;
import org.frameworkset.elasticsearch.client.config.BaseImportConfig;
import org.frameworkset.elasticsearch.client.schedule.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public abstract  class BaseImportContext implements ImportContext {
	protected BaseImportConfig baseImportConfig;
//	private JDBCResultSet jdbcResultSet;
	private DataTranPlugin dataTranPlugin;
	public BaseImportContext(){

	}
	public BaseImportContext(BaseImportConfig baseImportConfig){
		this.baseImportConfig = baseImportConfig;
		dataTranPlugin = buildDataTranPlugin();
		dataTranPlugin.init();
	}
	@Override
	public void setStatusTableId(int hashCode) {
		baseImportConfig.setStatusTableId(hashCode);
	}

	@Override
	public DBConfig getDbConfig() {
		return baseImportConfig.getDbConfig();
	}
	public DataRefactor getDataRefactor(){
		return baseImportConfig.getDataRefactor();
	}

	public String getApplicationPropertiesFile(){
		return baseImportConfig.getApplicationPropertiesFile();
	}

	public List<DBConfig> getConfigs(){
		return baseImportConfig.getConfigs();
	}

	protected abstract DataTranPlugin buildDataTranPlugin();

	public String getRefreshOption(){
		return baseImportConfig.getRefreshOption();
	}
	public boolean isPrintTaskLog(){
		return baseImportConfig.isPrintTaskLog();
	}

	@Override
	public void destroy() {
		if(dataTranPlugin != null){
			dataTranPlugin.destroy();
		}
	}

	@Override
	public void importData() {
		if(dataTranPlugin != null){
			dataTranPlugin.importData();
		}
	}

	public boolean isContinueOnError(){
		return baseImportConfig.isContinueOnError();
	}

	@Override
	public boolean assertCondition() {
		return dataTranPlugin.assertCondition();
	}
	public List<CallInterceptor> getCallInterceptors(){
		return baseImportConfig.getCallInterceptors();
	}

	@Override
	public void doImportData() {
		if(dataTranPlugin != null)
			dataTranPlugin.doImportData();
	}

	public ScheduleConfig getScheduleConfig(){
		return baseImportConfig.getScheduleConfig();
	}
	public Boolean getFixedRate(){
		return baseImportConfig.getScheduleConfig().getFixedRate();
	}
	public ScheduleService getScheduleService(){
		return dataTranPlugin.getScheduleService();
	}
	public BaseImportConfig getImportConfig() {
		return baseImportConfig;
	}

	public int getMaxRetry(){
		return baseImportConfig.getMaxRetry();
	}

	public boolean isAsyn(){
		return baseImportConfig.isAsyn();
	}

	public boolean isDebugResponse(){
		return baseImportConfig.isDebugResponse();
	}

	public boolean isDiscardBulkResponse(){
		return baseImportConfig.isDiscardBulkResponse();
	}
	public WrapedExportResultHandler getExportResultHandler(){
		return baseImportConfig.getExportResultHandler();
	}



	public void flushLastValue(Object lastValue){
		this.dataTranPlugin.flushLastValue(lastValue);
	}
	public Integer getLastValueType() {
		return baseImportConfig.getLastValueType();
	}
	public Status getCurrentStatus(){
		return this.dataTranPlugin.getCurrentStatus();
	}

	@Override
	public DBConfig getStatusDbConfig() {
		return baseImportConfig.getStatusDbConfig();
	}

	@Override
	public boolean isExternalTimer() {
		return baseImportConfig.isExternalTimer();
	}

	public String getLastValueClumnName(){
		return dataTranPlugin.getLastValueClumnName();
	}
	public String getNumberLastValueColumn(){
		return baseImportConfig.getNumberLastValueColumn();
	}

	@Override
	public Object getConfigLastValue() {
		return baseImportConfig.getConfigLastValue();
	}

	@Override
	public String getLastValueStoreTableName() {
		return baseImportConfig.getLastValueStoreTableName();
	}

	@Override
	public String getLastValueStorePath() {
		return baseImportConfig.getLastValueStorePath();
	}


//	public Object getValue(String columnName) throws ESDataImportException {
//		try {
//			return jdbcResultSet.getValue(columnName);
//		}
//		catch (Exception e){
//			throw new ESDataImportException(e);
//		}
//	}
//
//	public Object getDateTimeValue(String columnName) throws ESDataImportException {
//		try {
//			return jdbcResultSet.getDateTimeValue(columnName);
//		}
//		catch (Exception e){
//			throw new ESDataImportException(e);
//		}
//
//	}

	public DataTranPlugin getDataTranPlugin() {
		return dataTranPlugin;
	}

	public Object max(Object oldValue,Object newValue){
		if(newValue == null)
			return oldValue;
//		this.getLastValueType()
		if(this.getLastValueType() == ImportIncreamentConfig.TIMESTAMP_TYPE) {
			Date oldValueDate = (Date)oldValue;
			Date newValueDate = (Date)newValue;
			if(newValueDate.after(oldValueDate))
				return newValue;
			else
				return oldValue;
		}
		else{
//			Method compareTo = oldValue.getClass().getMethod("compareTo");
			if(oldValue instanceof BigDecimal || newValue instanceof BigDecimal){
				int e = ((BigDecimal)oldValue).compareTo ((BigDecimal)newValue);
				if(e < 0)
					return newValue;
				else
					return oldValue;
			}
			else if(oldValue instanceof Double || newValue instanceof Double){
				int e = Double.compare(((Number)oldValue).doubleValue(), ((Number)newValue).doubleValue());
				if(e < 0)
					return newValue;
				else
					return oldValue;
			}
			else if(oldValue instanceof Float || newValue instanceof Float){
				int e = Float.compare(((Number)oldValue).floatValue(), ((Number)newValue).floatValue());
				if(e < 0)
					return newValue;
				else
					return oldValue;
			}
			else if(oldValue instanceof Long || newValue instanceof Long){
				int e = Long.compare(((Number)oldValue).longValue(), ((Number)newValue).longValue());
				if(e < 0)
					return newValue;
				else
					return oldValue;
			}
			else {
				int e = Integer.compare(((Number)oldValue).intValue(), ((Number)newValue).intValue());
				if(e < 0)
					return newValue;
				else
					return oldValue;
			}

		}
	}

	public void setLastValueType(int lastValueType){
		this.baseImportConfig.setLastValueType(lastValueType);
	}
	public int getThreadCount(){
		return baseImportConfig.getThreadCount();
	}
	public boolean isParallel(){
		return baseImportConfig.isParallel();
	}
	public void stop(){
		this.dataTranPlugin.destroy();
	}
	public int getQueue(){
		return baseImportConfig.getQueue();
	}
	public ESIndexWrapper getEsIndexWrapper(){
		return baseImportConfig.getEsIndexWrapper();
	}


	public void setEsIndexWrapper(ESIndexWrapper esIndexWrapper) {
		this.baseImportConfig.setEsIndexWrapper( esIndexWrapper);
	}

//	public Object getValue(String columnName) throws ESDataImportException {
//		try {
//			return jdbcResultSet.getValue(columnName);
//		}
//		catch (Exception e){
//			throw new ESDataImportException(e);
//		}
//	}
//
//	public Object getDateTimeValue(String columnName) throws ESDataImportException {
//		try {
//			return jdbcResultSet.getDateTimeValue(columnName);
//		}
//		catch (Exception e){
//			throw new ESDataImportException(e);
//		}
//
//	}

	public int getStoreBatchSize(){
		return baseImportConfig.getScheduleBatchSize();
	}
	public int getStatusTableId(){
		return this.baseImportConfig.getStatusTableId();
	}
	public boolean isFromFirst(){
		return baseImportConfig.isFromFirst();
	}

	@Override
	public String getDateLastValueColumn() {
		return baseImportConfig.getDateLastValueColumn();
	}


}
