package org.frameworkset.elasticsearch;

import java.util.List;

import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.spi.DefaultApplicationContext;

public class ElasticSearchHelper {
	protected static DefaultApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
	public static final String DEFAULT_SEARCH = "elasticSearch";
	protected static ElasticSearch elasticSearchSink = null;
	public ElasticSearchHelper() {
		// TODO Auto-generated constructor stub
	}
	protected static void init(){
		if(elasticSearchSink == null)
			elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
	}

	/**
	 * 获取elasticSearch对应的elasticSearch服务器对象
	 * @param elasticSearch
	 * @return
	 */
	public static ElasticSearch getElasticSearchSink(String elasticSearch){
		if(elasticSearch == null || elasticSearch.equals("")) {
			init();
			return elasticSearchSink;
		}
		ElasticSearch elasticSearchSink = context.getTBeanObject(elasticSearch, ElasticSearch.class);
		return elasticSearchSink;
	}
	
	public static ElasticSearch getElasticSearchSink(){
		init();
		return elasticSearchSink;
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件
	 * @return
	 */
	public static ClientInterface getRestClientUtil(){
		init();
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return elasticSearchSink.getRestClientUtil();
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件,所有的操作直接在elasticSearch对应的es服务器上操作
	 * @param elasticSearch
	 * @return
	 */
	public static ClientInterface getRestClientUtil(String elasticSearch){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getRestClientUtil();
	}

 
	/**
	 * 加载query dsl配置文件，在默认的es服务器上执行所有操作
	 * @param configFile
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(String configFile){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}

	/**
	 * 加载query dsl配置文件，在elasticSearch参数对应的es服务器上执行所有操作
	 * @param elasticSearch
	 * @param configFile
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(String elasticSearch,String configFile){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}

	/**
	 * 管理接口：添加rest服务器
	 * @param hosts
	 */
	public static void addHttpServer(List<String> hosts){

	}

}
