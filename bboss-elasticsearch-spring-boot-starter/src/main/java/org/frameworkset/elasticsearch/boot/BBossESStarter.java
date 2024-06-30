package org.frameworkset.elasticsearch.boot;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.frameworkset.common.poolman.util.DBConf;
import com.frameworkset.common.poolman.util.SQLManager;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.template.BaseTemplateContainerImpl;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.spi.assemble.PropertiesInterceptor;
import org.frameworkset.spi.assemble.PropertyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


public class BBossESStarter  extends BaseESProperties{
	@Autowired
	private BBossESProperties properties;
	private static final Logger log = LoggerFactory.getLogger(BBossESStarter.class);
	public void start() {
		PropertiesContainer propertiesContainer = null;
		if(this.getElasticsearch() == null) {
			if (properties.getElasticsearch() != null) {
				Map ps = properties.buildProperties();
				if (ps != null && ps.size() > 0) {
					ElasticsearchBootResult elasticsearchBootResult = ElasticSearchBoot.boot(ps, true);
					propertiesContainer = elasticsearchBootResult.getPropertiesContainer();
				}

				else {
					log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
				}
			}
		}
		else{
			if(properties.getDslfile() != null && this.getDslfile() == null)
				this.setDslfile(properties.getDslfile());
			if(properties.getPropertiesInterceptor() != null && this.getPropertiesInterceptor() == null){
				this.setPropertiesInterceptor(properties.getPropertiesInterceptor());
			}
			Map ps = buildProperties();
			if (ps != null && ps.size() > 0) {
				ElasticsearchBootResult elasticsearchBootResult = ElasticSearchBoot.boot(ps, true);
				propertiesContainer = elasticsearchBootResult.getPropertiesContainer();
//				propertiesContainer = ElasticSearchBoot.boot(ps, true);
			}
			else {
				log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
			}
		}
		/**
		 * 启动数据源
		 * //数据源相关配置，可选项，可以在外部启动数据源
		 * 					importBuilder.setDbName("test")
		 * 							.setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
		 * 							//mysql stream机制一 通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
		 * //					.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false")
		 * //					.setJdbcFetchSize(3000)//启用mysql stream机制1，设置jdbcfetchsize大小为3000
		 * 							//mysql stream机制二  jdbcFetchSize为Integer.MIN_VALUE即可，url中不需要设置useCursorFetch=true参数，这里我们使用机制二
		 * 							.setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false")
		 * 							.setJdbcFetchSize(Integer.MIN_VALUE)//启用mysql stream机制二,设置jdbcfetchsize大小为Integer.MIN_VALUE
		 * 							.setDbUser("root")
		 * 							.setDbPassword("123456")
		 * 							.setValidateSQL("select 1")
		 * 							.setUsePool(false);//是否使用连接池
		 */
		String temp = null;
		if(this.propertiesInterceptor == null){
			temp = properties.getPropertiesInterceptor();
		}
		else{
			temp = propertiesInterceptor;
		}
		if(temp != null && !temp.trim().equals("")){
			temp = temp.trim();

		}
		if(this.getDb() == null ){
			if(properties.getDb() != null && properties.getDb().getUrl() != null){
				if(propertiesContainer != null)
					propertiesContainer.interceptorValues(properties.getDb());
				else{
					try {
						Class clz = Class.forName(temp);
						PropertiesInterceptor propertiesInterceptor = (PropertiesInterceptor) clz.newInstance();
						PropertyContext propertyContext = new PropertyContext();
						propertyContext.setValue(properties.getDb());
						propertiesInterceptor.convert(propertyContext);
					}
					catch (Exception e){
						log.error("Init Ds "+temp,e);
					}
				}
				initDS(properties.getDb());
			}
		}
		else{
			if(getDb().getUrl() != null)
				if(propertiesContainer != null)
					propertiesContainer.interceptorValues(getDb());
				else{
					try {
						Class clz = Class.forName(temp);
						PropertiesInterceptor propertiesInterceptor = (PropertiesInterceptor) clz.newInstance();
						PropertyContext propertyContext = new PropertyContext();
						propertyContext.setValue(getDb());
						propertiesInterceptor.convert(propertyContext);
					}
					catch (Exception e){
						log.error("Init Ds "+temp,e);
					}
				}
				initDS(getDb());
		}

	}
	private void initDS(Db db){
		DBConf temConf = new DBConf();
		temConf.setPoolname(db.getName());
		temConf.setDriver(db.getDriver());
		temConf.setJdbcurl(db.getUrl());
		temConf.setUsername(db.getUser());
		temConf.setPassword(db.getPassword());
        temConf.setBalance(db.getBalance());
        temConf.setEnableBalance(SimpleStringUtil.isNotEmpty(db.getEnableBalance())&& db.getEnableBalance().equals("true"));
		temConf.setReadOnly(null);
		temConf.setTxIsolationLevel(null);
		temConf.setValidationQuery(db.getValidateSQL());
		temConf.setJndiName(db.getName()+"_jndi");
		temConf.setInitialConnections(db.getInitSize() != null?Integer.parseInt(db.getInitSize()):10);
		temConf.setMinimumSize(db.getMinIdleSize() != null?Integer.parseInt(db.getMinIdleSize()):10);
		temConf.setMaximumSize(db.getMaxSize() != null?Integer.parseInt(db.getMaxSize()):50);
		temConf.setUsepool(db.getUsePool() != null?Boolean.parseBoolean(db.getUsePool()):true);
		temConf.setExternal(false);
		temConf.setExternaljndiName(null);
		temConf.setShowsql(db.getShowSql()!= null?Boolean.parseBoolean(db.getShowSql()):true);
		temConf.setEncryptdbinfo(false);
		temConf.setQueryfetchsize(db.getJdbcFetchSize() != null?Integer.parseInt(db.getJdbcFetchSize()):0);
		temConf.setDbAdaptor(db.getDbAdaptor());
		temConf.setDbtype(db.getDbtype());
		boolean ff = db.getColumnLableUpperCase() == null ? false:db.getColumnLableUpperCase().equals("true");


		temConf.setColumnLableUpperCase(ff);

		ff = db.getEnableShutdownHook() == null ? true:db.getEnableShutdownHook().equals("true");
		temConf.setEnableShutdownHook(ff);
		SQLManager.startPool(temConf);

	}

	private ClientInterface restClient;

	/**
	 * Get default elasticsearch server ClientInterface
	 * @return
	 */
	public ClientInterface getRestClient(){
		if(restClient == null) {
			synchronized (this) {
				if(restClient == null) {
					restClient = ElasticSearchHelper.getRestClientUtil();
				}
			}
		}
		return restClient;
	}



	/**
	 *  Get Special elasticsearch server ClientInterface
	 * @param elasticsearchName elasticsearch server name which defined in bboss spring boot application configfile
	 * @return
	 */
	public ClientInterface getRestClient(String elasticsearchName){

		return ElasticSearchHelper.getRestClientUtil(elasticsearchName);

	}

	/**
	 * Get default elasticsearch server ConfigFile ClientInterface
	 * @param configFile
	 * @return
	 */
	public ClientInterface getConfigRestClient(String configFile){

		return ElasticSearchHelper.getConfigRestClientUtil(configFile);

	}

	/**
	 * Get Special elasticsearch server ConfigFile ClientInterface
	 * @param elasticsearchName elasticsearch server name which defined in bboss spring boot application configfile
	 * @param configFile
	 * @return
	 */
	public ClientInterface getConfigRestClient(String elasticsearchName,String configFile){

		return ElasticSearchHelper.getConfigRestClientUtil(elasticsearchName,configFile);

	}

	/**
	 * Get default elasticsearch server ConfigFile ClientInterface
	 * @param templateContainer
	 * @return
	 */
	public ClientInterface getConfigRestClient(BaseTemplateContainerImpl templateContainer){

		return ElasticSearchHelper.getConfigRestClientUtil(templateContainer);

	}

	/**
	 * Get Special elasticsearch server ConfigFile ClientInterface
	 * @param elasticsearchName elasticsearch server name which defined in bboss spring boot application configfile
	 * @param templateContainer
	 * @return
	 */
	public ClientInterface getConfigRestClient(String elasticsearchName, BaseTemplateContainerImpl templateContainer){

		return ElasticSearchHelper.getConfigRestClientUtil(elasticsearchName,templateContainer);

	}
}
