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

import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.spi.assemble.PropertiesContainer;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:31
 * @author biaoping.yin
 * @version 1.0
 */
public class BaseBuilder {
	/**
	 * 打印任务日志
	 */
	protected boolean printTaskLog = false;

	/**
	 * 定时任务拦截器
	 */
	protected List<CallInterceptor> callInterceptors;
	protected String applicationPropertiesFile;
	protected boolean freezen;
	/**抽取数据的sql语句*/
	protected String sql;

	protected String sqlName;


	/**是否启用sql日志，true启用，false 不启用，*/
	protected boolean showSql;

	/**抽取数据的sql语句*/
	protected String dbName;
	/**抽取数据的sql语句*/
	protected String dbDriver;
	/**抽取数据的sql语句*/
	protected String dbUrl;
	/**抽取数据的sql语句*/
	protected String dbUser;
	/**抽取数据的sql语句*/
	protected String dbPassword;
	/**抽取数据的sql语句*/
	protected String validateSQL;
	/**抽取数据的sql语句*/
	protected boolean usePool = false;

	/**批量获取数据大小*/
	protected int batchSize = 1000;



	/**
	 * use parallel import:
	 *  true yes
	 *  false no
	 */
	protected boolean parallel;
	/**
	 * parallel import work thread nums,default 200
	 */
	protected int threadCount = 200;
	/**
	 * 并行队列大小，默认1000
	 */
	protected int queue = 1000;
	/**
	 * 是否同步等待批处理作业结束，true 等待 false 不等待
	 */
	protected boolean asyn;
	/**
	 * 并行执行过程中出现异常终端后续作业处理，已经创建的作业会执行完毕
	 */
	protected boolean continueOnError;


	protected Integer jdbcFetchSize;
	protected void buildDBConfig(){
		if(!this.freezen) {
			PropertiesContainer propertiesContainer = new PropertiesContainer();
			if(this.applicationPropertiesFile == null) {
				propertiesContainer.addConfigPropertiesFile("application.properties");
			}
			else{
				propertiesContainer.addConfigPropertiesFile(applicationPropertiesFile);
			}
			this.dbName  = propertiesContainer.getProperty("db.name");
			this.dbUser  = propertiesContainer.getProperty("db.user");
			this.dbPassword  = propertiesContainer.getProperty("db.password");
			this.dbDriver  = propertiesContainer.getProperty("db.driver");
			this.dbUrl  = propertiesContainer.getProperty("db.url");
			String _usePool = propertiesContainer.getProperty("db.usePool");
			if(_usePool != null && !_usePool.equals(""))
				this.usePool  = Boolean.parseBoolean(_usePool);
			this.validateSQL  = propertiesContainer.getProperty("db.validateSQL");

			String _showSql = propertiesContainer.getProperty("db.showsql");
			if(_showSql != null && !_showSql.equals(""))
				this.showSql  = Boolean.parseBoolean(_showSql);

			String _jdbcFetchSize = propertiesContainer.getProperty("db.jdbcFetchSize");
			if(_jdbcFetchSize != null && !_jdbcFetchSize.equals(""))
				this.jdbcFetchSize  = Integer.parseInt(_jdbcFetchSize);



		}
	}
}
