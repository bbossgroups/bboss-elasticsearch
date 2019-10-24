package org.frameworkset.elasticsearch.client.db2es;
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
import org.frameworkset.elasticsearch.client.*;
import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.elasticsearch.client.schedule.ScheduleConfig;
import org.frameworkset.elasticsearch.client.schedule.ScheduleService;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/21 16:13
 * @author biaoping.yin
 * @version 1.0
 */
public class DB2ESImportContext implements ImportContext {
	private ESJDBC esjdbc;
//	private JDBCResultSet jdbcResultSet;
	private DataTranPlugin dataTranPlugin;
	public DB2ESImportContext(){
		this(new ESJDBC());
	}
	public DB2ESImportContext(ESJDBC esjdbc ){
		this.esjdbc = esjdbc;
		dataTranPlugin = new DBDataTranPlugin(this,esjdbc);
		dataTranPlugin.init();
	}
	public String getRefreshOption(){
		return esjdbc.getRefreshOption();
	}
	public boolean isPrintTaskLog(){
		return esjdbc.isPrintTaskLog();
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
		return esjdbc.isContinueOnError();
	}

	@Override
	public boolean assertCondition() {
		return dataTranPlugin.assertCondition();
	}
	public List<CallInterceptor> getCallInterceptors(){
		return esjdbc.getCallInterceptors();
	}

	@Override
	public void doImportData() {
		if(dataTranPlugin != null)
			dataTranPlugin.doImportData();
	}

	public ScheduleConfig getScheduleConfig(){
		return esjdbc.getScheduleConfig();
	}
	public Boolean getFixedRate(){
		return esjdbc.getScheduleConfig().getFixedRate();
	}
	public ScheduleService getScheduleService(){
		return dataTranPlugin.getScheduleService();
	}
	public ESJDBC getEsjdbc() {
		return esjdbc;
	}

	public int getMaxRetry(){
		return esjdbc.getMaxRetry();
	}

	public boolean isAsyn(){
		return esjdbc.isAsyn();
	}

	public boolean isDebugResponse(){
		return esjdbc.isDebugResponse();
	}

	public boolean isDiscardBulkResponse(){
		return esjdbc.isDiscardBulkResponse();
	}
	public DB2ESExportResultHandler getExportResultHandler(){
		return esjdbc.getExportResultHandler();
	}



	public void flushLastValue(Object lastValue){
		this.dataTranPlugin.flushLastValue(lastValue);
	}
	public Integer getLastValueType() {
		return esjdbc.getLastValueType();
	}

	@Override
	public DBConfig getStatusDbConfig() {
		return esjdbc.getStatusDbConfig();
	}

	@Override
	public boolean isExternalTimer() {
		return esjdbc.isExternalTimer();
	}

	public String getLastValueClumnName(){
		return dataTranPlugin.getLastValueClumnName();
	}
	public String getNumberLastValueColumn(){
		return esjdbc.getNumberLastValueColumn();
	}

	@Override
	public Object getConfigLastValue() {
		return esjdbc.getConfigLastValue();
	}

	@Override
	public String getLastValueStoreTableName() {
		return esjdbc.getLastValueStoreTableName();
	}

	@Override
	public String getLastValueStorePath() {
		return esjdbc.getLastValueStorePath();
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


	public int getThreadCount(){
		return esjdbc.getThreadCount();
	}
	public boolean isParallel(){
		return esjdbc.isParallel();
	}
	public void stop(){
		this.dataTranPlugin.destroy();
	}
	public int getQueue(){
		return esjdbc.getQueue();
	}
	public ESIndexWrapper getEsIndexWrapper(){
		return esjdbc.getEsIndexWrapper();
	}


	public void setEsIndexWrapper(ESIndexWrapper esIndexWrapper) {
		this.esjdbc.setEsIndexWrapper( esIndexWrapper);
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
		return esjdbc.getScheduleBatchSize();
	}
	public int getStatusTableId(){
		return this.esjdbc.getStatusTableId();
	}
	public boolean isFromFirst(){
		return esjdbc.isFromFirst();
	}

	@Override
	public String getDateLastValueColumn() {
		return esjdbc.getDateLastValueColumn();
	}
}
