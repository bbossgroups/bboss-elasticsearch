/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.frameworkset.elasticsearch.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.common.bytes.BytesReference;
import org.frameworkset.elasticsearch.ElasticSearch;
import org.frameworkset.elasticsearch.ElasticSearchEventSerializer;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.EventIndexNameBuilder;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.JavaElasticSearch;
import org.frameworkset.elasticsearch.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

//import org.apache.http.client.HttpClient;
//import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Rest ElasticSearch client which is responsible for sending bulks of events to
 * ElasticSearch using ElasticSearch HTTP API. This is configurable, so any
 * config params required should be taken through this.
 */
public class ElasticSearchRestEventClient extends ElasticSearchRestClient implements EventElasticSearchClient {

	 
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchRestEventClient.class);
	protected final ElasticSearchEventSerializer serializer;
	protected JavaElasticSearch javaElasticSearch = null;
     
	public ElasticSearchRestEventClient(ElasticSearch elasticSearch,String[] hostNames, String elasticUser, String elasticPassword,
								   ElasticSearchEventSerializer serializer, Properties extendElasticsearchPropes) {
		super(elasticSearch,  hostNames,   elasticUser,   elasticPassword,
			       extendElasticsearchPropes);
		javaElasticSearch = (JavaElasticSearch)elasticSearch;
		 
		this.serializer = serializer;
		 
 
		
	}
	

 

	 

	public void createIndexRequest(StringBuilder bulkBuilder, IndexNameBuilder indexNameBuilder, Event event, ElasticSearchEventSerializer elasticSearchEventSerializer) throws ElasticSearchException {

		try {
			BytesReference content = elasticSearchEventSerializer == null ? serializer.getContentBuilder(event).bytes() :
					elasticSearchEventSerializer.getContentBuilder(event).bytes();
			Map<String, Map<String, String>> parameters = new HashMap<String, Map<String, String>>();
			Map<String, String> indexParameters = new HashMap<String, String>();
			indexParameters.put(ElasticSearchRestClient.INDEX_PARAM, ((EventIndexNameBuilder)indexNameBuilder).getIndexName(event));
			indexParameters.put(ElasticSearchRestClient.TYPE_PARAM, event.getIndexType());
			if (event.getTTL() > 0) {
				indexParameters.put(ElasticSearchRestClient.TTL_PARAM, Long.toString(event.getTTL()) + "ms");
			}
			parameters.put(ElasticSearchRestClient.INDEX_OPERATION_NAME, indexParameters);

			Gson gson = new Gson();

			bulkBuilder.append(gson.toJson(parameters));
			bulkBuilder.append("\n");
//	      bulkBuilder.append(content.toBytesArray().toUtf8());
			bulkBuilder.append(content.utf8ToString());
			bulkBuilder.append("\n");
		} catch (IOException e) {
			throw new ElasticSearchException(e);
		}

	}


	@Override
	public EventClientUtil getEventClientUtil(IndexNameBuilder indexNameBuilder) {
		// TODO Auto-generated method stub
				return new RestEventClientUtil(this, indexNameBuilder);
	}


	@Override
	public EventClientUtil getConfigEventClientUtil(IndexNameBuilder indexNameBuilder, String configFile) {
		// TODO Auto-generated method stub
				return new ConfigEventRestClientUtil(this, indexNameBuilder,configFile);
	}

 


	
	
}
