package org.frameworkset.elasticsearch;

import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.spi.DefaultApplicationContext;

public class ElasticSearchHelper {
	private static DefaultApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
	public static final String DEFAULT_SEARCH = "elasticSearch";
	private static ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
	public ElasticSearchHelper() {
		// TODO Auto-generated constructor stub
	}
	
	public static ElasticSearch getElasticSearchSink(String elasticSearch){
		if(elasticSearch == null || elasticSearch.equals(""))
			return elasticSearchSink;
		ElasticSearch elasticSearchSink = context.getTBeanObject(elasticSearch, ElasticSearch.class);
		return elasticSearchSink;
	}
	
	public static ElasticSearch getElasticSearchSink(){
		return elasticSearchSink;
	}
	
	public static ClientUtil getRestClientUtil(){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return elasticSearchSink.getRestClientUtil();
	}
	
	public static ClientUtil getRestClientUtil(String elasticSearch){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getRestClientUtil();
	}
	
	
	public static Client getClient(){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return elasticSearchSink.getClient();
	}
	
	public static  Client getClient(String elasticSearch){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getClient();
	}
	
	
	public static ClientUtil getConfigRestClientUtil(String configFile){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}
	
	public static ClientUtil getConfigRestClientUtil(String elasticSearch,String configFile){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}
	
	
	
	

}
