package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.ElasticSearchEventSerializer;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.event.Event;

public interface EventClientUtil extends ClientInterface {

	public   void addEvent(Event event,ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException ;
	public Object execute() throws ElasticSearchException;
	public void updateIndexs(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer);
	 
}
