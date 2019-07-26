package org.frameworkset.elasticsearch.client.schedule;
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
import com.frameworkset.common.poolman.handle.ResultSetHandler;
import com.frameworkset.common.poolman.util.SQLUtil;
import com.frameworkset.orm.transaction.TransactionManager;
import org.frameworkset.elasticsearch.client.*;
import org.frameworkset.util.tokenizer.TextGrammarParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/8 17:31
 * @author biaoping.yin
 * @version 1.0
 */
public class ScheduleService {
	private static Logger logger = LoggerFactory.getLogger(ScheduleService.class);
	private volatile Status currentStatus;
	private volatile Status firstStatus;
	private volatile boolean insertedCheck = false;
	private Lock insertedCheckLock = new ReentrantLock();
	public boolean isExternalTimer() {
		return externalTimer;
	}

	public void setExternalTimer(boolean externalTimer) {
		this.externalTimer = externalTimer;
	}

	/**
	 * 采用外部定时任务引擎执行定时任务控制变量：
	 * false 内部引擎，默认值
	 * true 外部引擎
	 */
	protected boolean externalTimer;
	private ESJDBC esjdbc;
	private String updateSQL ;
	private String insertSQL;
	private String createStatusTableSQL;
	private String selectSQL;
	private String existSQL;
	private int lastValueType = 0;
//	private int id = 1;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Date initLastDate = null;
	private String statusDbname;
	private String statusTableName;
	private String statusStorePath;
	private String lastValueClumnName;

//	private StoreStatusTask storeStatusTask;

	public void setIncreamentImport(boolean increamentImport) {
		this.increamentImport = increamentImport;
	}

	public String getLastValueClumnName(){
		return this.lastValueClumnName;
	}

	private boolean increamentImport = true;
	private SQLInfo sqlInfo;
	public SQLInfo getSqlInfo() {
		return sqlInfo;
	}

	public String getLastValueVarName(){
		return this.sqlInfo != null?this.sqlInfo.getLastValueVarName():null;
	}

	private Timer timer ;
	public void addStatus(Status currentStatus) throws Exception {
		SQLExecutor.insertWithDBName(statusDbname,insertSQL,currentStatus.getId(),currentStatus.getTime(),currentStatus.getLastValue(),lastValueType);
	}
	public void updateStatus(Status currentStatus) throws Exception {
		SQLExecutor.updateWithDBName(statusDbname,updateSQL, currentStatus.getTime(), currentStatus.getLastValue(), lastValueType,currentStatus.getId());
	}
	private void initLastValueStatus(boolean update) throws Exception {
		Status currentStatus = new Status();
		currentStatus.setId(esjdbc.getStatusTableId());
		currentStatus.setTime(new Date().getTime());
		if(lastValueType == 1) {
			currentStatus.setLastValue(initLastDate.getTime());
		}
		else if(esjdbc.getConfigLastValue() != null){

			currentStatus.setLastValue(esjdbc.getConfigLastValue());
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
		insertedCheck = true;
		logger.info("init LastValue Status: "+currentStatus.toString());
	}
	private void scheduleImportData(final int batchSize) throws Exception {
		if(!esjdbc.assertCondition()) {
			if(logger.isWarnEnabled())
				logger.warn(new StringBuilder().append("Task Assert Execute Condition Failed, Ignore").toString());
			return;
		}
//		SQLInfo sqlInfo = getLastValueSQL();
		ResultSetHandler resultSetHandler = new DefaultResultSetHandler(esjdbc,batchSize);


		if(sqlInfo.getParamSize() == 0) {
			if(esjdbc.getDataRefactor() == null || !esjdbc.getDbConfig().isEnableDBTransaction()){
				if (esjdbc.getExecutor() == null) {
					SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql());
				} else {
					esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), (Map) null);
				}
			}
			else {
				TransactionManager transactionManager = new TransactionManager();
				try {
					transactionManager.begin(TransactionManager.RW_TRANSACTION);
					if (esjdbc.getExecutor() == null) {
						SQLExecutor.queryWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql());
					} else {
						esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), (Map) null);
					}
					transactionManager.commit();
				} finally {
					transactionManager.releasenolog();
				}
			}

		}
		else {
			 if(!this.isIncreamentImport()){
			 	esjdbc.setForceStop();
			 }
			 else {
				 if(esjdbc.getDataRefactor() == null || !esjdbc.getDbConfig().isEnableDBTransaction()){
					 if (esjdbc.getExecutor() == null) {
						 SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql(), getParamValue());
					 } else {
						 esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), getParamValue());

					 }
				 }
				 else {
					 TransactionManager transactionManager = new TransactionManager();
					 try {
						 transactionManager.begin(TransactionManager.RW_TRANSACTION);
						 if (esjdbc.getExecutor() == null) {
							 SQLExecutor.queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSql(), getParamValue());
						 } else {
							 esjdbc.getExecutor().queryBeanWithDBNameByNullRowHandler(resultSetHandler, esjdbc.getDbConfig().getDbName(), esjdbc.getSqlName(), getParamValue());

						 }
					 } finally {
						 transactionManager.releasenolog();
					 }
				 }

			 }
		}

	}
	private void preCall(TaskContext taskContext){
		List<CallInterceptor> callInterceptors = esjdbc.getCallInterceptors();
		if(callInterceptors == null || callInterceptors.size() == 0)
			return;
		for(CallInterceptor callInterceptor: callInterceptors){
			try{
				callInterceptor.preCall(taskContext);
			}
			catch (Exception e){
				logger.error("preCall failed:",e);
			}
		}

	}
	private void afterCall(TaskContext taskContext){
		List<CallInterceptor> callInterceptors = esjdbc.getCallInterceptors();
		if(callInterceptors == null || callInterceptors.size() == 0)
			return;
		CallInterceptor callInterceptor = null;
		for(int j = callInterceptors.size() - 1; j >= 0; j --){
			callInterceptor = callInterceptors.get(j);
			try{
				callInterceptor.afterCall(taskContext);
			}
			catch (Exception e){
				logger.error("afterCall failed:",e);
			}
		}
	}

	private void throwException(TaskContext taskContext,Exception e){
		List<CallInterceptor> callInterceptors = esjdbc.getCallInterceptors();
		if(callInterceptors == null || callInterceptors.size() == 0)
			return;
		CallInterceptor callInterceptor = null;
		for(int j = callInterceptors.size() - 1; j >= 0; j --){
			callInterceptor = callInterceptors.get(j);
			try{
				callInterceptor.throwException(taskContext,e);
			}
			catch (Exception e1){
				logger.error("afterCall failed:",e1);
			}
		}

	}
	public void timeSchedule() throws Exception {
//		scheduleImportData(esjdbc.getBatchSize());

		timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
//				TaskContext taskContext = new TaskContext(esjdbc);
//				try {
//					preCall(taskContext);
//					scheduleImportData(esjdbc.getScheduleBatchSize());
//					afterCall(taskContext);
//				}
//				catch (Exception e){
//					throwException(taskContext,e);
//					logger.error("scheduleImportData failed:",e);
//				}
				externalTimeSchedule();
			}
		};
		Date scheduleDate = esjdbc.getScheduleConfig().getScheduleDate();
		Long delay = esjdbc.getScheduleConfig().getDeyLay();
		if(scheduleDate != null) {
			if (esjdbc.getScheduleConfig().getFixedRate() != null && esjdbc.getScheduleConfig().getFixedRate()) {

				timer.scheduleAtFixedRate(timerTask, scheduleDate, esjdbc.getScheduleConfig().getPeriod());
			} else {
				if(esjdbc.getScheduleConfig().getPeriod() != null) {
					timer.schedule(timerTask, scheduleDate, esjdbc.getScheduleConfig().getPeriod());
				}
				else{
					timer.schedule(timerTask, scheduleDate);
				}

			}
		}
		else  {
			if(delay == null){
				delay = 1000L;
			}
			if (esjdbc.getScheduleConfig().getFixedRate() != null && esjdbc.getScheduleConfig().getFixedRate()) {

				timer.scheduleAtFixedRate(timerTask, delay, esjdbc.getScheduleConfig().getPeriod());
			} else {
				if(esjdbc.getScheduleConfig().getPeriod() != null) {
					timer.schedule(timerTask, delay, esjdbc.getScheduleConfig().getPeriod());
				}
				else{
					timer.schedule(timerTask, delay);
				}

			}
		}



	}
	public void externalTimeSchedule()  {

		TaskContext taskContext = new TaskContext(esjdbc);
		long importStartTime = System.currentTimeMillis();
		try {

			preCall(taskContext);
			scheduleImportData(esjdbc.getScheduleBatchSize());
			afterCall(taskContext);
		}
		catch (Exception e){
			throwException(taskContext,e);
			logger.error("scheduleImportData failed:",e);
		}
		finally {
			long importEndTime = System.currentTimeMillis();
			if(esjdbc != null && this.esjdbc.isPrintTaskLog() && logger.isInfoEnabled())
				logger.info(new StringBuilder().append("Execute schedule job Take ").append((importEndTime - importStartTime)).append(" ms").toString());
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

	private void initStatusStore(){
		if(this.isIncreamentImport()) {
			statusTableName = this.esjdbc.getLastValueStoreTableName();
			if (statusTableName == null) {
				statusTableName = "increament_tab";
			}
//			throw new ESDataImportException("Must set lastValueStoreTableName by ImportBuilder.");
			if (this.esjdbc.getLastValueStorePath() == null || this.esjdbc.getLastValueStorePath().equals("")) {
//				throw new ESDataImportException("Must set lastValueStorePath by ImportBuilder.");
				statusStorePath = "StatusStoreDB";
			} else {
				statusStorePath = this.esjdbc.getLastValueStorePath();
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
	private void initDatasource()  {
		if(this.isIncreamentImport()) {
			if(this.esjdbc.getStatusDbConfig() == null) {
				statusDbname = this.esjdbc.getDbConfig().getDbName() + "_config";
				String dbJNDIName = this.esjdbc.getDbConfig().getDbName() + "_config";
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
				DBConfig statusDBConfig = esjdbc.getStatusDbConfig();

				statusDbname = esjdbc.getStatusDbConfig().getDbName();
				if(!statusDbname.equals(esjdbc.getDbConfig().getDbName())){
					String dbJNDIName = statusDbname + "_config";
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

			if (esjdbc.getDateLastValueColumn() != null) {
				this.lastValueType = ImportIncreamentConfig.TIMESTAMP_TYPE;
			} else if (esjdbc.getNumberLastValueColumn() != null) {
				this.lastValueType = ImportIncreamentConfig.NUMBER_TYPE;

			} else if (esjdbc.getLastValueType() != null) {
				this.lastValueType = esjdbc.getLastValueType();
			} else {
				this.lastValueType = ImportIncreamentConfig.NUMBER_TYPE;
			}


			existSQL = new StringBuilder().append("select 1 from ").append(statusTableName).toString();
			selectSQL = new StringBuilder().append("select id,lasttime,lastvalue,lastvaluetype from ")
					.append(statusTableName).append(" where id=?").toString();
			updateSQL = new StringBuilder().append("update ").append(statusTableName)
					.append(" set lasttime = ?,lastvalue = ? ,lastvaluetype= ? where id=?").toString();
			insertSQL = new StringBuilder().append("insert into ").append(statusTableName)
					.append(" (id,lasttime,lastvalue,lastvaluetype) values(?,?,?,?)").toString();
		}
	}

	/**
	 * 更新数据导入作业状态
	 * @param time
	 * @param currentValue
	 */
	public void updateStatus(long time,Object currentValue){
		this.currentStatus.setTime(time);
		this.currentStatus.setLastValue(currentValue);
	}
	private void initTableAndStatus(){
		if(this.isIncreamentImport()) {
			try {
				initLastDate = dateFormat.parse("1970-01-01");
				//SQLExecutor.updateWithDBName("gencode","drop table BBOSS_GENCODE");
				SQLExecutor.queryObjectWithDBName(int.class, statusDbname, existSQL);

			} catch (Exception e) {
				String tsql = createStatusTableSQL;
				logger.info(statusTableName + " table not exist，" + statusTableName + "：" + tsql + ".");
				try {
					SQLExecutor.updateWithDBName(statusDbname, tsql);
					logger.info("table " + statusTableName + " create success：" + tsql + ".");

				} catch (Exception e1) {
					logger.info("table " + statusTableName + " create success：" + tsql + ".", e1);
					throw new ESDataImportException(e1);

				}
			}
			try {
				/**
				 * 初始化数据检索起始状态信息
				 */
				currentStatus = SQLExecutor.queryObjectWithDBName(Status.class, statusDbname, selectSQL, esjdbc.getStatusTableId());
				if (currentStatus == null) {
					initLastValueStatus(false);
				} else {
					if (this.esjdbc.isFromFirst()) {
						initLastValueStatus(true);
					} else {
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
				currentStatus.setId(esjdbc.getStatusTableId());
				currentStatus.setTime(new Date().getTime());
				this.firstStatus = (Status) currentStatus.clone();
				this.currentStatus = currentStatus;
			}
			catch (Exception e){
				throw new ESDataImportException(e);
			}


		}
	}
//	private void startStoreStatusTask(){
//		storeStatusTask = new StoreStatusTask(this);
//		storeStatusTask.start();
//	}
	public void init(ESJDBC esjdbc){
		this.esjdbc = esjdbc;
		initSQLInfo();
		initLastValueClumnName();
		if(sqlInfo != null
				&& sqlInfo.getParamSize() > 0
				&& !this.isIncreamentImport()){
			throw new TaskFailedException("Parameter variables cannot be set in non-incremental import SQL statements："+esjdbc.getSql());
		}
		initStatusStore();
		initDatasource();
		initTableAndStatus();
		this.externalTimer = this.esjdbc.isExternalTimer();
		this.esjdbc.setScheduleService(this);
//		startStoreStatusTask();
	}

	public void initLastValueClumnName(){
		if(lastValueClumnName != null){
			return ;
		}

		if (esjdbc.getDateLastValueColumn() != null) {
			lastValueClumnName = esjdbc.getDateLastValueColumn();
		} else if (esjdbc.getNumberLastValueColumn() != null) {
			lastValueClumnName = esjdbc.getNumberLastValueColumn();
		} else if (this.getLastValueVarName() != null) {
			if(logger.isInfoEnabled())
				logger.info(new StringBuilder().append("NumberLastValueColumn and DateLastValueColumn not setted,use LastValueVarName[")
						.append(this. getLastValueVarName())
						.append("] in sql[ ").append(esjdbc.getSql()).append("]").toString());
			lastValueClumnName =  getLastValueVarName();
		}

		if (lastValueClumnName == null){
			setIncreamentImport(false);
		}


	}

	private void initSQLInfo(){
		String originSQL = esjdbc.getSql();
		List<TextGrammarParser.GrammarToken> tokens =
				TextGrammarParser.parser(originSQL, "#[", "]");
		SQLInfo _sqlInfo = new SQLInfo();
		int paramSize = 0;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < tokens.size(); i ++){
			TextGrammarParser.GrammarToken token = tokens.get(i);
			if(token.texttoken()){
				builder.append(token.getText());
			}
			else {
				builder.append("?");
				if(paramSize == 0){
					_sqlInfo.setLastValueVarName(token.getText());
				}
				paramSize ++;

			}
		}
		_sqlInfo.setParamSize(paramSize);
		_sqlInfo.setSql(builder.toString());
		this.sqlInfo = _sqlInfo;


	}



	public SQLInfo getLastValueSQL(){

		return this.sqlInfo;
	}

	public Map getParamValue(){
		Map params = new HashMap();
		if(this.lastValueType == ImportIncreamentConfig.NUMBER_TYPE) {
			params.put(this.sqlInfo.getLastValueVarName(), this.currentStatus.getLastValue());
		}
		else{
			if(this.currentStatus.getLastValue() instanceof Date)
				params.put(this.sqlInfo.getLastValueVarName(), this.currentStatus.getLastValue());
			else {
				if(this.currentStatus.getLastValue() instanceof Long) {
					params.put(this.sqlInfo.getLastValueVarName(), new Date((Long)this.currentStatus.getLastValue()));
				}
				else if(this.currentStatus.getLastValue() instanceof Integer){
					params.put(this.sqlInfo.getLastValueVarName(), new Date(((Integer) this.currentStatus.getLastValue()).longValue()));
				}
				else if(this.currentStatus.getLastValue() instanceof Short){
					params.put(this.sqlInfo.getLastValueVarName(), new Date(((Short) this.currentStatus.getLastValue()).longValue()));
				}
				else{
					params.put(this.sqlInfo.getLastValueVarName(), new Date(((Number) this.currentStatus.getLastValue()).longValue()));
				}
			}
		}
		if(logger.isInfoEnabled()){
			logger.info(new StringBuilder().append("Current values: ").append(params).toString());
		}
		return params;
	}
	public void stop(){
		try {
			if (timer != null)
				timer.cancel();
		}
		catch (Exception e){
			logger.error("",e);
		}
//		try {
//			this.storeStatusTask.interrupt();
//		}catch (Exception e){
//			logger.error("",e);
//		}
		try {
			SQLUtil.stopPool(this.statusDbname);
		}
		catch (Exception e){
			logger.error("",e);
		}
		this.esjdbc.destroy();

	}

	public void flushLastValue(Object lastValue) {
		this.currentStatus.setTime(System.currentTimeMillis());
		this.currentStatus.setLastValue(lastValue);
		if(this.isIncreamentImport())
			this.storeStatus();
	}

	public Status getFirstStatus() {
		return firstStatus;
	}

	public void setFirstStatus(Status firstStatus) {
		this.firstStatus = firstStatus;
	}

	public boolean isIncreamentImport() {
		return increamentImport;
	}
}
