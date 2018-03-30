package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.ElasticSearchEventSerializer;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestEventClientUtil extends RestClientUtil implements EventClientUtil{
	private static Logger logger = LoggerFactory.getLogger(RestEventClientUtil.class);
	protected ElasticSearchRestEventClient client;
	protected StringBuilder bulkBuilder;
	protected IndexNameBuilder indexNameBuilder;

	public RestEventClientUtil(ElasticSearchClient client,IndexNameBuilder indexNameBuilder) {
		super(client,  indexNameBuilder);
		this.client = (ElasticSearchRestEventClient)client;
		this.indexNameBuilder = indexNameBuilder;
	}
	   

	public   void addEvent(Event event,ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException {
	    if (bulkBuilder == null) {
	    	 bulkBuilder = new StringBuilder();
	    }
	    client.createIndexRequest(bulkBuilder, indexNameBuilder, event,  elasticSearchEventSerializer);
	     
	  }

	 
	  
	public Object execute(String options) throws ElasticSearchException {
		  return client.execute(this.bulkBuilder.toString(),  options);
	  }


	@Override
	public void updateIndexs(Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) {
		// TODO Auto-generated method stub
		
	}

	  
		 
 
}
