package org.frameworkset.elasticsearch;

import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.EventClientUtil;

public class EventElasticSearchHelper extends ElasticSearchHelper{
	 
	protected static JavaElasticSearch javaElasticSearchSink = null;
	protected static void init(){
		if(javaElasticSearchSink == null){
			javaElasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, JavaElasticSearch.class);
			elasticSearchSink = javaElasticSearchSink;
		}
	}
	/**
	 * 获取默认应的es服务器java api transport接口
	 * @return
	 */
	public static Client getClient(){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		return javaElasticSearchSink.getClient();
	}

	/**
	 * 获取elasticSearch对应的es服务器java api transport接口
	 * @param elasticSearch
	 * @return
	 */
	public static  Client getClient(String elasticSearch){
		JavaElasticSearch elasticSearchSink = (JavaElasticSearch)getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getClient();
	}
	
	
	/**
	 * 获取elasticSearch对应的elasticSearch服务器对象
	 * @param elasticSearch
	 * @return
	 */
	public static JavaElasticSearch getJavaElasticSearchSink(String elasticSearch){
		return (JavaElasticSearch)getElasticSearchSink(elasticSearch);
	}
	
	public static JavaElasticSearch getJavaElasticSearchSink(){
		return (JavaElasticSearch)getElasticSearchSink();
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件
	 * @return
	 */
	public static EventClientUtil getRestEventClientUtil(){
		init();
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return javaElasticSearchSink.getEventRestClientUtil();
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件,所有的操作直接在elasticSearch对应的es服务器上操作
	 * @param elasticSearch
	 * @return
	 */
	public static EventClientUtil getRestEventClientUtil(String elasticSearch){
		JavaElasticSearch elasticSearchSink = getJavaElasticSearchSink( elasticSearch);
		return elasticSearchSink.getEventRestClientUtil();
	}

 
	/**
	 * 加载query dsl配置文件，在默认的es服务器上执行所有操作
	 * @param configFile
	 * @return
	 */
	public static EventClientUtil getConfigEventRestClientUtil(String configFile){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		return javaElasticSearchSink.getConfigEventRestClientUtil(configFile);
	}

	/**
	 * 加载query dsl配置文件，在elasticSearch参数对应的es服务器上执行所有操作
	 * @param elasticSearch
	 * @param configFile
	 * @return
	 */
	public static EventClientUtil getConfigEventRestClientUtil(String elasticSearch,String configFile){
		JavaElasticSearch elasticSearchSink = getJavaElasticSearchSink( elasticSearch);
		return elasticSearchSink.getConfigEventRestClientUtil(configFile);
	}

	

}
