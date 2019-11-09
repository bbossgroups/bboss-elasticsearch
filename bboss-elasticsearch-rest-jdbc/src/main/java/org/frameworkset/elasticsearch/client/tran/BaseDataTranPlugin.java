package org.frameworkset.elasticsearch.client.tran;
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

import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.util.SQLUtil;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.boot.ElasticSearchBoot;
import org.frameworkset.elasticsearch.client.DBConfig;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.TranErrorWrapper;
import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.schedule.ImportIncreamentConfig;
import org.frameworkset.elasticsearch.client.schedule.ScheduleService;
import org.frameworkset.elasticsearch.client.schedule.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 16:55
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class BaseDataTranPlugin implements DataTranPlugin {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private boolean increamentImport = true;
	private ExportCount exportCount;
	public ExportCount getExportCount() {
		return exportCount;
	}


	public BaseDataTranPlugin(ImportContext importContext){
		this.importContext = importContext;
		init(importContext);
	}
	protected void init(ImportContext importContext){

	}
	@Override
	public ImportContext getImportContext() {
		return importContext;
	}

	public void setImportContext(ImportContext importContext) {
		this.importContext = importContext;
	}

	protected ImportContext importContext;
	protected volatile Status currentStatus;
	protected volatile Status firstStatus;
	protected String updateSQL ;
	protected String insertSQL;
	protected String createStatusTableSQL;
	protected String selectSQL;
	protected String existSQL;
	protected int lastValueType = 0;
	//	protected int id = 1;

	protected Date initLastDate = null;
	protected String statusDbname;
	protected String statusTableName;
	protected String statusStorePath;
	protected String lastValueClumnName;
	protected ScheduleService scheduleService;
	protected boolean isPrintTaskLog(){
		return importContext.isPrintTaskLog() && logger.isInfoEnabled();
	}
	@Override
	public void importData() throws ESDataImportException {

		if(this.scheduleService == null) {//一次性执行数据导入操作

			long importStartTime = System.currentTimeMillis();
			this.doImportData();
			long importEndTime = System.currentTimeMillis();
			if( isPrintTaskLog())
				logger.info(new StringBuilder().append("Execute job Take ").append((importEndTime - importStartTime)).append(" ms").toString());
		}
		else{//定时增量导入数据操作
			try {
				if (!this.importContext.isExternalTimer()) {//内部定时任务引擎
					scheduleService.timeSchedule( importContext);
				} else { //外部定时任务引擎执行的方法，比如quartz之类的
					scheduleService.externalTimeSchedule( importContext);
				}
			}
			catch (ESDataImportException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new ESDataImportException(e);
			}
		}

	}





	public abstract void beforeInit();
	public abstract void afterInit();
	@Override
	public void init() {
		exportCount = new ExportCount();
		beforeInit();
		this.initSchedule();
		initLastValueClumnName();
		initStatusStore();
		initDatasource();
		initTableAndStatus();
		afterInit();
//		this.externalTimer = this.esjdbc.isExternalTimer();
//		this.esjdbc.setDataTranPlugin(this);
	}
	public String getLastValueClumnName(){
		return this.lastValueClumnName;
	}
	public boolean isContinueOnError(){
		return this.importContext.isContinueOnError();
	}

	@Override
	public void destroy() {
		if(scheduleService != null){
			scheduleService.stop();
		}
		try {
			if(statusDbname != null && !statusDbname.equals(""))
				SQLUtil.stopPool(this.statusDbname);
		}
		catch (Exception e){
			logger.error("Stop status db pool["+statusDbname+"] failed:",e);
		}


	}


	public void putLastParamValue(Map params){
		if(this.lastValueType == ImportIncreamentConfig.NUMBER_TYPE) {
			params.put(getLastValueVarName(), this.currentStatus.getLastValue());
		}
		else{
			if(this.currentStatus.getLastValue() instanceof Date)
				params.put(getLastValueVarName(), this.currentStatus.getLastValue());
			else {
				if(this.currentStatus.getLastValue() instanceof Long) {
					params.put(getLastValueVarName(), new Date((Long)this.currentStatus.getLastValue()));
				}
				else if(this.currentStatus.getLastValue() instanceof Integer){
					params.put(getLastValueVarName(), new Date(((Integer) this.currentStatus.getLastValue()).longValue()));
				}
				else if(this.currentStatus.getLastValue() instanceof Short){
					params.put(getLastValueVarName(), new Date(((Short) this.currentStatus.getLastValue()).longValue()));
				}
				else{
					params.put(getLastValueVarName(), new Date(((Number) this.currentStatus.getLastValue()).longValue()));
				}
			}
		}
		if(isPrintTaskLog()){
			logger.info(new StringBuilder().append("Current values: ").append(params).toString());
		}
	}



	public Map getParamValue(){
		Map params = new HashMap();
		if(this.lastValueType == ImportIncreamentConfig.NUMBER_TYPE) {
			params.put(getLastValueVarName(), this.currentStatus.getLastValue());
		}
		else{
			if(this.currentStatus.getLastValue() instanceof Date)
				params.put(getLastValueVarName(), this.currentStatus.getLastValue());
			else {
				if(this.currentStatus.getLastValue() instanceof Long) {
					params.put(getLastValueVarName(), new Date((Long)this.currentStatus.getLastValue()));
				}
				else if(this.currentStatus.getLastValue() instanceof Integer){
					params.put(getLastValueVarName(), new Date(((Integer) this.currentStatus.getLastValue()).longValue()));
				}
				else if(this.currentStatus.getLastValue() instanceof Short){
					params.put(getLastValueVarName(), new Date(((Short) this.currentStatus.getLastValue()).longValue()));
				}
				else{
					params.put(getLastValueVarName(), new Date(((Number) this.currentStatus.getLastValue()).longValue()));
				}
			}
		}
		if(isPrintTaskLog()){
			logger.info(new StringBuilder().append("Current values: ").append(params).toString());
		}
		return params;
	}
	public void initLastValueClumnName(){
		if(lastValueClumnName != null){
			return ;
		}

		if (importContext.getDateLastValueColumn() != null) {
			lastValueClumnName = importContext.getDateLastValueColumn();
		} else if (importContext.getNumberLastValueColumn() != null) {
			lastValueClumnName = importContext.getNumberLastValueColumn();
		} else if (this.getLastValueVarName() != null) {
//			if(logger.isInfoEnabled())
//				logger.info(new StringBuilder().append("NumberLastValueColumn and DateLastValueColumn not setted,use LastValueVarName[")
//						.append(this. getLastValueVarName())
//						.append("] in sql[ ").append(getSql()).append("]").toString());
			lastValueClumnName =  getLastValueVarName();
		}

		if (lastValueClumnName == null){
			setIncreamentImport(false);
		}


	}

	private void initLastValueStatus(boolean update) throws Exception {
		Status currentStatus = new Status();
		currentStatus.setId(importContext.getStatusTableId());
		currentStatus.setTime(new Date().getTime());
		if(lastValueType == ImportIncreamentConfig.TIMESTAMP_TYPE) {
			if(importContext.getConfigLastValue() != null){
				if(importContext.getConfigLastValue() instanceof Long){
					currentStatus.setLastValue(new Date((Long)importContext.getConfigLastValue()));
				}
				else if(importContext.getConfigLastValue() instanceof Date) {
					currentStatus.setLastValue(importContext.getConfigLastValue());
				}

				else{
					if(logger.isInfoEnabled()) {
						logger.info("Last Value Illegal:{}", importContext.getConfigLastValue());
					}
					throw new ESDataImportException("Last Value Illegal:"+importContext.getConfigLastValue() );
				}
			}
			else {
				currentStatus.setLastValue(initLastDate);
			}
		}
		else if(importContext.getConfigLastValue() != null){

			currentStatus.setLastValue(importContext.getConfigLastValue());
		}
		else{
			currentStatus.setLastValue(0);
		}


		currentStatus.setLastValueType(lastValueType);
		if(!update)
			addStatus(currentStatus);
		else
			updateStatus(currentStatus);
		this.currentStatus = currentStatus;
		this.firstStatus = (Status) currentStatus.clone();
//		insertedCheck = true;
		if(logger.isInfoEnabled())
			logger.info("Init LastValue Status: "+currentStatus.toString());
	}


	


	protected void initTableAndStatus(){
		if(this.isIncreamentImport()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				initLastDate = dateFormat.parse("1970-01-01");
				//SQLExecutor.updateWithDBName("gencode","drop table BBOSS_GENCODE");
				SQLExecutor.queryObjectWithDBName(int.class, statusDbname, existSQL);

			} catch (Exception e) {
				String tsql = createStatusTableSQL;
				if(logger.isInfoEnabled())
					logger.info(statusTableName + " table not exist，" + statusTableName + "：" + tsql + ".");
				try {
					SQLExecutor.updateWithDBName(statusDbname, tsql);
					if(logger.isInfoEnabled())
						logger.info("table " + statusTableName + " create success：" + tsql + ".");

				} catch (Exception e1) {
					if(logger.isInfoEnabled())
						logger.info("table " + statusTableName + " create success：" + tsql + ".", e1);
					throw new ESDataImportException(e1);

				}
			}
			try {
				/**
				 * 初始化数据检索起始状态信息
				 */
				currentStatus = SQLExecutor.queryObjectWithDBName(Status.class, statusDbname, selectSQL, importContext.getStatusTableId());
				if (currentStatus == null) {
					initLastValueStatus(false);
				} else {
					if (importContext.isFromFirst()) {
						initLastValueStatus(true);
					} else {
						if(currentStatus.getLastValueType() == ImportIncreamentConfig.TIMESTAMP_TYPE){
							Object lastValue = currentStatus.getLastValue();
							if(lastValue instanceof Long){
								currentStatus.setLastValue(new Date((Long)lastValue));
							}
						}
						this.firstStatus = (Status) currentStatus.clone();
					}
				}
			} catch (Exception e) {
				throw new ESDataImportException(e);
			}
		}
		else{

			try {
				Status currentStatus = new Status();
				currentStatus.setId(importContext.getStatusTableId());
				currentStatus.setTime(new Date().getTime());
				this.firstStatus = (Status) currentStatus.clone();
				this.currentStatus = currentStatus;
			}
			catch (Exception e){
				throw new ESDataImportException(e);
			}


		}
	}

	protected void initStatusStore(){
		if(this.isIncreamentImport()) {
			statusTableName = importContext.getLastValueStoreTableName();
			if (statusTableName == null) {
				statusTableName = "increament_tab";
			}
//			throw new ESDataImportException("Must set lastValueStoreTableName by ImportBuilder.");
			if (importContext.getLastValueStorePath() == null || importContext.getLastValueStorePath().equals("")) {
//				throw new ESDataImportException("Must set lastValueStorePath by ImportBuilder.");
				statusStorePath = "StatusStoreDB";
			} else {
				statusStorePath = importContext.getLastValueStorePath();
			}
		}



//		if(this.esjdbc.getImportIncreamentConfig().getDateLastValueColumn() == null
//				&& this.esjdbc.getImportIncreamentConfig().getNumberLastValueColumn() == null
//				)
//			throw new ESDataImportException("Must set dateLastValueColumn or numberLastValueColumn by ImportBuilder.");

	}

	/**
	 * 初始化增量采集数据状态保存数据源
	 */
	protected void initDatasource()  {
		if(this.isIncreamentImport()) {
			if(importContext.getStatusDbConfig() == null) {
				statusDbname =  "_status_datasource";
				String dbJNDIName ="_status_datasource_jndi";
				try {
					createStatusTableSQL = new StringBuilder().append("create table " ).append( statusTableName)
							.append( " (ID number(10),lasttime number(10),lastvalue number(10),lastvaluetype number(1),PRIMARY KEY (ID))").toString();
					File dbpath = new File(statusStorePath);
					logger.info("initDatasource dbpath:" + dbpath.getCanonicalPath());
					SQLUtil.startPool(statusDbname,
							"org.sqlite.JDBC",
							"jdbc:sqlite://" + dbpath.getCanonicalPath(),
							"root", "root",
							null,//"false",
							null,// "READ_UNCOMMITTED",
							"select 1",
							dbJNDIName,
							10,
							10,
							20,
							true,
							false,
							null, false, false
					);
				} catch (Exception e) {
					throw new ESDataImportException(e);
				}
			}
			else{
				DBConfig statusDBConfig = importContext.getStatusDbConfig();

				statusDbname = importContext.getStatusDbConfig().getDbName();
				if(statusDbname == null || statusDbname.trim().equals(""))
					statusDbname =  "_status_datasource";

				if(statusDBConfig.getDbDriver() != null && !statusDBConfig.getDbDriver().trim().equals("")){
					String dbJNDIName = statusDbname+"_jndi";
					try {

						SQLUtil.startPool(statusDbname,
								statusDBConfig.getDbDriver(),
								statusDBConfig.getDbUrl(),
								statusDBConfig.getDbUser(), statusDBConfig.getDbPassword(),
								null,//"false",
								null,// "READ_UNCOMMITTED",
								statusDBConfig.getValidateSQL(),
								dbJNDIName,
								10,
								10,
								20,
								true,
								false,
								null, false, false
						);
					} catch (Exception e) {
						throw new ESDataImportException(e);
					}
				}
				createStatusTableSQL = statusDBConfig.getStatusTableDML();
				if(createStatusTableSQL == null){
					createStatusTableSQL = statusDBConfig.getCreateStatusTableSQL(SQLUtil.getPool(statusDbname).getDBType());
				}
				createStatusTableSQL = createStatusTableSQL.replace("$statusTableName",statusTableName);
			}

			if (importContext.getDateLastValueColumn() != null) {
				this.lastValueType = ImportIncreamentConfig.TIMESTAMP_TYPE;
			} else if (importContext.getNumberLastValueColumn() != null) {
				this.lastValueType = ImportIncreamentConfig.NUMBER_TYPE;

			} else if (importContext.getLastValueType() != null) {
				this.lastValueType = importContext.getLastValueType();
			} else {
				this.lastValueType = ImportIncreamentConfig.NUMBER_TYPE;
			}
			/**
			 * 回填值类型
			 */
			importContext.setLastValueType(this.lastValueType);


			existSQL = new StringBuilder().append("select 1 from ").append(statusTableName).toString();
			selectSQL = new StringBuilder().append("select id,lasttime,lastvalue,lastvaluetype from ")
					.append(statusTableName).append(" where id=?").toString();
			updateSQL = new StringBuilder().append("update ").append(statusTableName)
					.append(" set lasttime = ?,lastvalue = ? ,lastvaluetype= ? where id=?").toString();
			insertSQL = new StringBuilder().append("insert into ").append(statusTableName)
					.append(" (id,lasttime,lastvalue,lastvaluetype) values(?,?,?,?)").toString();
		}
	}

	public void setIncreamentImport(boolean increamentImport) {
		this.increamentImport = increamentImport;
	}
	public boolean isIncreamentImport() {
		return increamentImport;
	}
	public Status getCurrentStatus(){
		return this.currentStatus;
	}
	public void flushLastValue(Object lastValue) {
		if(lastValue != null) {
			this.currentStatus.setTime(System.currentTimeMillis());

			this.currentStatus.setLastValue(lastValue);
			if (this.isIncreamentImport())
				this.storeStatus();
		}
	}
	public void storeStatus()  {
//		if(!insertedCheck){
//			try {
//				insertedCheckLock.lock();
//				if (!insertedCheck) {
//					addStatus(currentStatus);
//					insertedCheck = true;
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				insertedCheckLock.unlock();
//			}
//
//		}
//		else{
		try {
			updateStatus(currentStatus);
		} catch (Exception e) {
			throw new ESDataImportException(e);
		}
//		}

	}
	public void addStatus(Status currentStatus) throws Exception {
		Object lastValue = !importContext.isLastValueDateType()?currentStatus.getLastValue():((Date)currentStatus.getLastValue()).getTime();
		SQLExecutor.insertWithDBName(statusDbname,insertSQL,currentStatus.getId(),currentStatus.getTime(),lastValue,lastValueType);
	}
	public void updateStatus(Status currentStatus) throws Exception {
		Object lastValue = !importContext.isLastValueDateType()?currentStatus.getLastValue():((Date)currentStatus.getLastValue()).getTime();
		SQLExecutor.updateWithDBName(statusDbname,updateSQL, currentStatus.getTime(), lastValue, lastValueType,currentStatus.getId());
	}





	public ScheduleService getScheduleService(){
		return this.scheduleService;
	}


//	public Object getLastValue() throws ESDataImportException {
//
//
//			if(getLastValueClumnName() == null){
//				return null;
//			}
//
////			if (this.importIncreamentConfig.getDateLastValueColumn() != null) {
////				return this.getValue(this.importIncreamentConfig.getDateLastValueColumn());
////			} else if (this.importIncreamentConfig.getNumberLastValueColumn() != null) {
////				return this.getValue(this.importIncreamentConfig.getNumberLastValueColumn());
////			}
////			else if (this.dataTranPlugin.getSqlInfo().getLastValueVarName() != null) {
////				return this.getValue(this.dataTranPlugin.getSqlInfo().getLastValueVarName());
////			}
//			if(this.getLastValueType() == null || this.getLastValueType().intValue() ==  ImportIncreamentConfig.NUMBER_TYPE)
//				return this.getValue(getLastValueClumnName());
//			else if(this.getLastValueType().intValue() ==  ImportIncreamentConfig.TIMESTAMP_TYPE){
//				return this.getDateTimeValue(getLastValueClumnName());
//			}
//			return null;
//
//
//	}



	//	private String indexType;
	private TranErrorWrapper errorWrapper;
	public TranErrorWrapper getErrorWrapper() {
		return errorWrapper;
	}

	public void setErrorWrapper(TranErrorWrapper errorWrapper) {
		this.errorWrapper = errorWrapper;
	}


	private volatile boolean forceStop = false;
	public void setForceStop(){
		this.forceStop = true;
	}
	/**
	 * 判断执行条件是否成立，成立返回true，否则返回false
	 * @return
	 */
	public boolean assertCondition(){
		if(forceStop)
			return false;
		if(errorWrapper != null)
			return errorWrapper.assertCondition();
		return true;
	}

	/**
	 * 判断执行条件是否成立，成立返回true，否则返回false
	 * @return
	 */
	public boolean assertCondition(Exception e){
		if(errorWrapper != null)
			return errorWrapper.assertCondition(e);
		return true;
	}
	protected void initES(String applicationPropertiesFile){
		if(SimpleStringUtil.isNotEmpty(applicationPropertiesFile ))
			ElasticSearchBoot.boot(applicationPropertiesFile);
	}

	public void initSchedule(){
		if(importContext.getScheduleConfig() != null) {
			this.scheduleService = new ScheduleService();
			this.scheduleService.init(importContext);
		}
	}
//	@Override
//	public Object getValue(String columnName) throws ESDataImportException {
//		return importContext.getValue(columnName);
//	}
//
//	@Override
//	public Object getDateTimeValue(String columnName) throws ESDataImportException {
//		return importContext.getDateTimeValue(columnName);
//	}

}
