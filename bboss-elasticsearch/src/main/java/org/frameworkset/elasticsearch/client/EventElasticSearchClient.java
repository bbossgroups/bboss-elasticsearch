package org.frameworkset.elasticsearch.client;

import java.util.Properties;

import org.frameworkset.elasticsearch.IndexNameBuilder;

public interface EventElasticSearchClient extends ElasticSearchClient{
	void configure(Properties elasticsearchPropes);

	/**
	 * Close connection to elastic search in client
	 */
	void close();
	EventClientUtil getEventClientUtil(IndexNameBuilder indexNameBuilder);
	public EventClientUtil getConfigEventClientUtil(IndexNameBuilder indexNameBuilder,String configFile);
	
	public void init();
}
