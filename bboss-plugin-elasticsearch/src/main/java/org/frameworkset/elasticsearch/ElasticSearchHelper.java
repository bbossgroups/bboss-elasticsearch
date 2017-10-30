package org.frameworkset.elasticsearch;

import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.spi.DefaultApplicationContext;

public class ElasticSearchHelper {
	private static DefaultApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
	public static final String DEFAULT_SEARCH = "elasticSearch";
	private static ElasticSearch elasticSearchSink = null;
	public ElasticSearchHelper() {
		// TODO Auto-generated constructor stub
	}
	private static void init(){
		if(elasticSearchSink == null)
			elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
	}

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
	
	public static ClientUtil getRestClientUtil(){
		init();
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return elasticSearchSink.getRestClientUtil();
	}
	
	public static ClientUtil getRestClientUtil(String elasticSearch){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getRestClientUtil();
	}
	
	
	public static Client getClient(){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		return elasticSearchSink.getClient();
	}
	
	public static  Client getClient(String elasticSearch){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getClient();
	}
	
	
	public static ClientUtil getConfigRestClientUtil(String configFile){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}
	
	public static ClientUtil getConfigRestClientUtil(String elasticSearch,String configFile){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}
	
	
	
	

}
