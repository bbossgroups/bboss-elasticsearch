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
package org.frameworkset.elasticsearch;

import com.frameworkset.util.SimpleStringUtil;
import com.google.common.base.Throwables;
import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.client.*;
import org.frameworkset.elasticsearch.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.frameworkset.elasticsearch.ElasticSearchSinkConstants.*;

/**
 * A sink which reads events from a channel and writes them to ElasticSearch
 * based on the work done by https://github.com/Aconex/elasticflume.git.</p>
 * <p>
 * This sink supports batch reading of events from the channel and writing them
 * to ElasticSearch.</p>
 * <p>
 * Indexes will be rolled daily using the format 'indexname-YYYY-MM-dd' to allow
 * easier management of the index</p>
 * <p>
 * This sink must be configured with with mandatory parameters detailed in
 * {@link ElasticSearchSinkConstants}</p> It is recommended as a secondary step
 * the ElasticSearch indexes are optimized for the specified serializer. This is
 * not handled by the sink but is typically done by deploying a config template
 * alongside the ElasticSearch deploy</p>
 * <p>
 * http://www.elasticsearch.org/guide/reference/api/admin-indices-templates.
 * html
 */
public class JavaElasticSearch extends ElasticSearch {

	private static final Logger logger = LoggerFactory
			.getLogger(JavaElasticSearch.class);
	
	protected String clusterName = DEFAULT_CLUSTER_NAME;
	 
	protected String clientType = DEFAULT_CLIENT_TYPE;
	protected String[] transportServerAddresses = null;
	protected ElasticSearchTransportClient transportClient = null;
	protected EventElasticSearchClient eventRestClient = null;
	private ElasticSearchIndexRequestBuilderFactory indexRequestFactory;
	public static final String EventTimeBasedIndexNameBuilderClass =
	          "org.frameworkset.elasticsearch.EventTimeBasedIndexNameBuilder";
	private ElasticSearchEventSerializer eventSerializer;

	/**
	 * Create an {@link ElasticSearch} configured using the supplied
	 * configuration
	 */
	public JavaElasticSearch() {

	}



	
	

	protected String[] getTransportServerAddresses() {
		return transportServerAddresses;
	}

	protected String getClusterName() {
		return clusterName;
	}

	 

	protected ElasticSearchEventSerializer getEventSerializer() {
		return eventSerializer;
	}
	protected IndexNameBuilder getIndexNameBuilder() {
		return indexNameBuilder;
	}

	 

	public EventClientUtil getTransportClientUtil() {
		if(transportClient != null)
			return this.transportClient.getEventClientUtil(this.indexNameBuilder);
		else
			return  null;
	}

	/**
	 * 获取es client对象
	 *
	 * @return
	 */
	public Client getClient() {
		if(transportClient != null)
			return ((TransportClientUtil)transportClient.getEventClientUtil(indexNameBuilder)).getClient();
		else
			return null;
	}
	public EventClientUtil getEventRestClientUtil() {
		if(eventRestClient != null)
			return this.eventRestClient.getEventClientUtil(this.indexNameBuilder);
		else
			return null;
	}

	public EventClientUtil getConfigEventRestClientUtil(String configFile) {
		if(eventRestClient != null)
			return this.eventRestClient.getConfigEventClientUtil(this.indexNameBuilder,configFile);
		else
			return null;
	} 
	
	public ClientInterface getRestClientUtil() {
		if(eventRestClient != null)
			return this.eventRestClient.getEventClientUtil(this.indexNameBuilder);
		else
			return null;
	}

	public ClientInterface getConfigRestClientUtil(String configFile) {
		if(eventRestClient != null)
			return this.eventRestClient.getConfigEventClientUtil(this.indexNameBuilder,configFile);
		else
			return null;
	} 
	/**
	 * @param datas
	 * @throws EventDeliveryException
	 */
	public Object addIndexs(java.util.List<Event> datas,String options) throws EventDeliveryException {
		return addIndexs(datas, null,options);

	}
	public Object addIndexs(java.util.List<Event> datas, ElasticSearchEventSerializer elasticSearchEventSerializer,String options) throws EventDeliveryException {
		/**
		 * 优先采用tcp协议
		 */
		EventClientUtil clientUtil = getTransportClientUtil() ;
		if(clientUtil == null)
			clientUtil = this.getEventRestClientUtil();
		try {

			int count;
			for (count = 0; count < datas.size(); ++count) {
				Event event = datas.get(count);
				if (event == null) {
					break;
				}
				if (event.getTTL() != null && event.getTTL() <= 0l)
					event.setTTL(ttlMs);
				String realIndexType = event.getIndexType() == null ? BucketPath.escapeString(indexType, event.getHeaders()) : event.getIndexType();
				event.setIndexType(realIndexType);
				clientUtil.addEvent(event, elasticSearchEventSerializer);
			}


			return clientUtil.execute( options);

		} catch (EventDeliveryException ex) {

//	        logger.error(
//	            "Exception in rollback. Rollback might not have been successful.",
//	            ex);
			throw ex;

		} catch (Exception ex) {

//	        logger.error(
//	            "Exception in rollback. Rollback might not have been successful.",
//	            ex);
			throw new EventDeliveryException(ex);

		}
		catch (Throwable ex) {

//	        logger.error(
//	            "Exception in rollback. Rollback might not have been successful.",
//	            ex);
			throw new EventDeliveryException(ex);

		}
	}

	
	/**
	 * 更新索引
	 *
	 * @param datas
	 * @param elasticSearchEventSerializer
	 * @throws EventDeliveryException
	 */
	public Object updateIndexs(java.util.List<Event> datas, ElasticSearchEventSerializer elasticSearchEventSerializer,String options) throws EventDeliveryException {
		EventClientUtil clientUtil = this.getTransportClientUtil();
		if(clientUtil == null)
			clientUtil = this.getEventRestClientUtil();
		try {

			int count;
			for (count = 0; count < datas.size(); ++count) {
				Event event = datas.get(count);


				if (event == null) {
					break;
				}
				if (event.getTTL() != null && event.getTTL() <= 0l)
					event.setTTL(ttlMs);
				String realIndexType = event.getIndexType() == null ? BucketPath.escapeString(indexType, event.getHeaders()) : event.getIndexType();
				event.setIndexType(realIndexType);
				clientUtil.updateIndexs(event, elasticSearchEventSerializer);
			}


			return clientUtil.execute( options);

		} catch (EventDeliveryException ex) {

//        logger.error(
//            "Exception in rollback. Rollback might not have been successful.",
//            ex);
			throw ex;

		} catch (Exception ex) {

//        logger.error(
//            "Exception in rollback. Rollback might not have been successful.",
//            ex);
			throw new EventDeliveryException(ex);

		}
	}

	public Object updateIndexs(java.util.List<Event> datas,String options) throws EventDeliveryException {
		return updateIndexs(datas, null,  options);

	}

	public Object deleteIndexs(String indexName, String indexType,String options, String... ids) throws EventDeliveryException {
		EventClientUtil clientUtil = this.getTransportClientUtil();
		if(clientUtil == null){
			clientUtil = this.getEventRestClientUtil();
			return clientUtil.deleteDocuments(indexName, indexType, ids);
		}
		else {
			try {

				clientUtil.deleteDocuments(indexName, indexType, ids);
				return clientUtil.execute( options);

			} catch (EventDeliveryException ex) {

				throw ex;

			} catch (Throwable ex) {

//		        logger.error(
//		            "Exception in rollback. Rollback might not have been successful.",
//		            ex);
				throw new EventDeliveryException(ex);

			}
		}
	}
	protected String getIndexNameBuilderClass(){
		String indexNameBuilderClass = EventTimeBasedIndexNameBuilderClass;
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(INDEX_NAME_BUILDER))) {
			indexNameBuilderClass = elasticsearchPropes.getProperty(INDEX_NAME_BUILDER);
		}
		return indexNameBuilderClass;
	}
	@Override
	public void configure() {
	
		 
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(TRANSPORT_HOSTNAMES))) {
			transportServerAddresses =
					elasticsearchPropes.getProperty(TRANSPORT_HOSTNAMES).trim().split(",");
		}
//		Preconditions.checkState(serverAddresses != null
//				&& serverAddresses.length > 0, "Missing Param:" + HOSTNAMES);


		 

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(CLUSTER_NAME))) {
			this.clusterName = elasticsearchPropes.getProperty(CLUSTER_NAME);
		}

		  

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(CLIENT_TYPE))) {
			clientType = elasticsearchPropes.getProperty(CLIENT_TYPE);
		}

		 

		String serializerClazz = null;//DEFAULT_SERIALIZER_CLASS;
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(SERIALIZER))) {
			serializerClazz = elasticsearchPropes.getProperty(SERIALIZER);
		}



		try {

			if (serializerClazz != null && !serializerClazz.equals("")) {
				Class<? extends ElasticSearchEventSerializer> clazz = (Class<? extends ElasticSearchEventSerializer>) Class
						.forName(serializerClazz);
				ElasticSearchEventSerializer serializer = clazz.newInstance();

				if (serializer instanceof ElasticSearchIndexRequestBuilderFactory) {
					indexRequestFactory
							= (ElasticSearchIndexRequestBuilderFactory) serializer;
					indexRequestFactory.configure(elasticsearchPropes);
				} else if (serializer instanceof ElasticSearchEventSerializer) {
					eventSerializer = (ElasticSearchEventSerializer) serializer;
					eventSerializer.configure(elasticsearchPropes);
				} else {
					throw new IllegalArgumentException(serializerClazz
							+ " is not an ElasticSearchEventSerializer");
				}
			}
		} catch (Exception e) {
			logger.error("Could not instantiate event serializer.", e);
			Throwables.propagate(e);
		}
		
		super.configure();


	 
	}
	@Override
	protected void start() {
		ElasticSearchEventClientFactory clientFactory = new ElasticSearchEventClientFactory();

		try {
			if(this.transportServerAddresses != null && this.transportServerAddresses.length > 0) {
				logger.info("Start ElasticSearch Transport client");
				transportClient = (ElasticSearchTransportClient) clientFactory.getClient(this,ElasticSearchClientFactory.TransportClient, transportServerAddresses, this.elasticUser, this.elasticPassword,
						clusterName, eventSerializer, indexRequestFactory, extendElasticsearchPropes);
				transportClient.configure(elasticsearchPropes);
				transportClient.init();
				logger.info("ElasticSearch Transport client started.");
			}

		} catch (Exception ex) {
			logger.error("ES Transport Client started failed", ex);

			if (transportClient != null) {
				transportClient.close();

			}
		}

		try {

			
			if(this.restServerAddresses != null && this.restServerAddresses.length > 0) {
				logger.info("Start ElasticSearch rest client");
				eventRestClient = clientFactory.getClient(this,ElasticSearchClientFactory.RestClient, restServerAddresses, this.elasticUser, this.elasticPassword,
						clusterName, eventSerializer, indexRequestFactory,extendElasticsearchPropes);
				eventRestClient.configure(elasticsearchPropes);
				eventRestClient.init();
				this.restClient = eventRestClient;
				
				logger.info("ElasticSearch Rest client started.");
			}



		} catch (Exception ex) {
			logger.error("ElasticSearch Rest Client started failed", ex);

			if (eventRestClient != null) {
				eventRestClient.close();

			}
		}


	}


	public void stop() {
		logger.info("ElasticSearch client stopping");
		super.stop();
		if (transportClient != null) {
			transportClient.close();
		}
	}

	 

}
