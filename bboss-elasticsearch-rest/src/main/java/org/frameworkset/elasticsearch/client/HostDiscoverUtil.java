package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.ElasticSearch;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 被动发现：自动发现es 主机节点
 */
public class HostDiscoverUtil{
	private static Logger logger = LoggerFactory.getLogger(HostDiscoverUtil.class);
	/**
	 * 默认Elasticsearch数据源
	 * @param hosts
	 *
	 */
	public static synchronized void handleDiscoverHosts(String[] hosts){
		handleDiscoverHosts(hosts,"default");
	}
	/**
	 * @param hosts
	 * @param elasticsearch
	 */
	public static synchronized void handleDiscoverHosts(String[] hosts,String elasticsearch){
		if(elasticsearch == null ){
			throw new IllegalArgumentException("elasticsearch can not be null.");
		}
        ElasticSearch elasticSearch = ElasticSearchHelper.getElasticSearchSink(elasticsearch);
        if(elasticSearch == null ){
            throw new IllegalArgumentException("elasticSearch["+elasticsearch+"] is null.");
        }

        List<ESAddress> addressList = new ArrayList<ESAddress>();
		for(String host:hosts){
			ESAddress esAddress = new ESAddress(host.trim(),elasticSearch.getHealthPath());
			addressList.add(esAddress);
		}

		ElasticSearchClient elasticSearchRestClient = elasticSearch.getRestClient();
		List<ESAddress> newAddress = new ArrayList<ESAddress>();
		//恢复移除节点
		elasticSearchRestClient.recoverRemovedNodes(addressList);
		//识别新增节点
		for(int i = 0; i < addressList.size();i ++){
			ESAddress address = addressList.get(i);
			if(!elasticSearchRestClient.containAddress(address)){
				newAddress.add(address);
			}
		}
		//处理新增节点
		if(newAddress.size() > 0) {
			if (logger.isInfoEnabled()) {
				logger.info(new StringBuilder().append("Discovery new elasticsearch[").append(elasticSearch.getElasticSearchName()).append("] node [").append(newAddress).append("].").toString());
			}
			elasticSearchRestClient.addAddresses(newAddress);
		}
		//处理删除节点
		elasticSearchRestClient.handleRemoved(addressList);
	}

	/**
	 * 动态切换是否打印dsl到控制台开关标识
	 * 默认dedault es数据源
	 * @param showdsl
	 */
	public static synchronized void swithShowdsl(boolean showdsl){
		swithShowdsl(  showdsl,"default");
	}

	/**
	 * 动态切换是否打印dsl到控制台开关标识
	 * 指定 es数据源
	 * @param showdsl
	 * @param elasticsearch es数据源名称
	 */
	public static synchronized void swithShowdsl(boolean showdsl,String elasticsearch){
		ElasticSearch elasticSearch = ElasticSearchHelper.getElasticSearchSink(elasticsearch);
		if(elasticSearch == null ){
			throw new IllegalArgumentException("elasticSearch["+elasticsearch+"] is null.");
		}

		ElasticSearchClient elasticSearchRestClient = elasticSearch.getRestClient();
		elasticSearchRestClient.setShowTemplate(showdsl);
	}

}
