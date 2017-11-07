package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.ElasticSearchEventSerializer;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.event.Event;

public class ConfigEventRestClientUtil extends ConfigRestClientUtil implements EventClientUtil{

	public ConfigEventRestClientUtil(ElasticSearchClient client, IndexNameBuilder indexNameBuilder, String configFile) {
		super(  client,   indexNameBuilder,   configFile) ;
			
	}

	@Override
	public void addEvent(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer)
			throws ElasticSearchException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateIndexs(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) {
		// TODO Auto-generated method stub
		
	}

}
