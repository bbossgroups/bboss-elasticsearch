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

import com.frameworkset.common.poolman.util.SQLUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.template.BaseTemplateContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


public class BBossESStarter  extends BaseESProperties{
	@Autowired
	private BBossESProperties properties;
	private static final Logger log = LoggerFactory.getLogger(BBossESStarter.class);
	public void start() {
		if(this.getElasticsearch() == null) {
			if (properties.getElasticsearch() != null) {
				Map ps = properties.buildProperties();
				if (ps != null && ps.size() > 0)
					ElasticSearchBoot.boot(ps,true);
				else {
					log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
				}
			}
		}
		else{
			if(properties.getDslfile() != null && this.getDslfile() == null)
				this.setDslfile(properties.getDslfile());
			Map ps = buildProperties();
			if (ps != null && ps.size() > 0)
				ElasticSearchBoot.boot(ps,true);
			else {
				log.info("BBoss Elasticsearch Rest Client properties is not configed in spring application.properties file.Ignore load bboss elasticsearch rest client through spring boot starter.");
			}
		}
		/**
		 * 启动数据源
		 * //数据源相关配置，可选项，可以在外部启动数据源
		 * 					importBuilder.setDbName("test")
		 * 							.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
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

		if(this.getDb() == null ){
			if(properties.getDb() != null && properties.getDb().getUrl() != null){
				initDS(properties.getDb());
			}
		}
		else{
			if(getDb().getUrl() != null)

				initDS(getDb());
		}

	}
	private void initDS(Db db){
		SQLUtil.startPool(db.getName(),//数据源名称
				db.getDriver(),//jdbc驱动
				db.getUrl(),//mysql链接串
				db.getUser(), db.getPassword(),//数据库账号和口令
				null,//"false",
				null,// "READ_UNCOMMITTED",
				db.getValidateSQL(),//数据库连接校验sql
				db.getName()+"_jndi",
				db.getInitSize() != null?Integer.parseInt(db.getInitSize()):10,
				db.getMinIdleSize() != null?Integer.parseInt(db.getMinIdleSize()):10,
				db.getMaxSize() != null?Integer.parseInt(db.getMaxSize()):50,
				db.getUsePool() != null?Boolean.parseBoolean(db.getUsePool()):true,
				false,
				null, db.getShowSql()!= null?Boolean.parseBoolean(db.getShowSql()):true, false,
				db.getJdbcFetchSize() != null?Integer.parseInt(db.getJdbcFetchSize()):0,
				db.getDbtype(),db.getDbAdaptor()
		);
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
