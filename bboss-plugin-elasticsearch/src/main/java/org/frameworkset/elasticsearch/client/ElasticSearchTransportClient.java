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

import com.google.common.annotations.VisibleForTesting;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.frameworkset.elasticsearch.*;
import org.frameworkset.elasticsearch.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import static org.frameworkset.elasticsearch.ElasticSearchSinkConstants.DEFAULT_PORT;

//import org.elasticsearch.node.Node;
//import org.elasticsearch.node.NodeBuilder;

public class ElasticSearchTransportClient implements ElasticSearchClient {

	public static final Logger logger = LoggerFactory.getLogger(ElasticSearchTransportClient.class);
	private Properties extendElasticsearchPropes;
	private InetSocketTransportAddress[] serverAddresses;
	private ElasticSearchEventSerializer serializer;
	private ElasticSearchIndexRequestBuilderFactory indexRequestBuilderFactory;
	private String elasticUser;
	private String elasticPassword;
	private ElasticSearch elasticSearch;
	private String clusterName;

	private Client client;

	@VisibleForTesting
	InetSocketTransportAddress[] getServerAddresses() {
		return serverAddresses;
	}

	 

	/**
	 * Transport client for external cluster
	 * 
	 * @param hostNames
	 * @param clusterName
	 * @param serializer
	 * @throws UnknownHostException
	 */
	public ElasticSearchTransportClient(ElasticSearch elasticSearch,String[] hostNames, String elasticUser, String elasticPassword,
			String clusterName, ElasticSearchEventSerializer serializer,Properties extendElasticsearchPropes) throws UnknownHostException {
		this.extendElasticsearchPropes = extendElasticsearchPropes;
		this.elasticSearch = elasticSearch;
		configureHostnames(hostNames);
		this.elasticUser = elasticUser;
		this.elasticPassword = elasticPassword;
		this.serializer = serializer;
		this.clusterName = clusterName;
		
	}

	public ElasticSearchTransportClient(ElasticSearch elasticSearch, String[] hostNames, String elasticUser, String elasticPassword,
										String clusterName, ElasticSearchIndexRequestBuilderFactory indexBuilder, Properties extendElasticsearchPropes) throws UnknownHostException {
		this.extendElasticsearchPropes = extendElasticsearchPropes;
		configureHostnames(hostNames);
		this.indexRequestBuilderFactory = indexBuilder;
		this.elasticUser = elasticUser;
		this.elasticPassword = elasticPassword;
		this.clusterName = clusterName;
		
	}
	public void init(){
		openClient(clusterName);
	}

	// /**
	// * Local transport client only for testing
	// *
	// * @param indexBuilderFactory
	// */
	// public
	// ElasticSearchTransportClient(ElasticSearchIndexRequestBuilderFactory
	// indexBuilderFactory) {
	// this.indexRequestBuilderFactory = indexBuilderFactory;
	// openLocalDiscoveryClient();
	// }
	//
	// /**
	// * Local transport client only for testing
	// *
	// * @param serializer
	// */
	// public ElasticSearchTransportClient(ElasticSearchEventSerializer
	// serializer) {
	// this.serializer = serializer;
	// openLocalDiscoveryClient();
	// }

	/**
	 * Used for testing
	 *
	 * @param client
	 *            ElasticSearch Client
	 * @param serializer
	 *            Event Serializer
	 */
	public ElasticSearchTransportClient(Client client, ElasticSearchEventSerializer serializer) {
		this.client = client;
		this.serializer = serializer;
	}

	/**
	 * Used for testing
	 */
	public ElasticSearchTransportClient(Client client, ElasticSearchIndexRequestBuilderFactory requestBuilderFactory)
			throws IOException {
		this.client = client;
		requestBuilderFactory.createIndexRequest(client, null, null, null);
	}

	private void configureHostnames(String[] hostNames) throws UnknownHostException {
		logger.warn(Arrays.toString(hostNames));
		serverAddresses = new InetSocketTransportAddress[hostNames.length];
		for (int i = 0; i < hostNames.length; i++) {
			String[] hostPort = hostNames[i].trim().split(":");
			String host = hostPort[0].trim();
			int port = hostPort.length == 2 ? Integer.parseInt(hostPort[1].trim()) : DEFAULT_PORT;
			// serverAddresses[i] = new InetSocketTransportAddress(host, port);
			serverAddresses[i] = new InetSocketTransportAddress(InetAddress.getByName(host), port);
		}
	}

	@Override
	public void close() {
		if (client != null) {
			client.close();
		}
		client = null;
	}

//	@Override
//	public void addEvent(Event event, IndexNameBuilder indexNameBuilder, String indexType, long ttlMs)
//			throws Exception {
//		if (bulkRequestBuilder == null) {
//			bulkRequestBuilder = client.prepareBulk();
//		}
//
//		IndexRequestBuilder indexRequestBuilder = null;
//		if (indexRequestBuilderFactory == null) {
//			XContentBuilder bytesStream = null;
//			try {
//				bytesStream = serializer.getContentBuilder(event);
//				indexRequestBuilder = client.prepareIndex(indexNameBuilder.getIndexName(event), indexType)
//						.setSource(bytesStream);
//			} finally {
//				if (bytesStream != null) {
//					// bytesStream.cl
//				}
//			}
//
//		} else {
//			indexRequestBuilder = indexRequestBuilderFactory.createIndexRequest(client,
//					indexNameBuilder.getIndexPrefix(event), indexType, event);
//		}
//
//		if (ttlMs > 0) {
//			indexRequestBuilder.setTTL(ttlMs);
//		}
//		bulkRequestBuilder.add(indexRequestBuilder);
//	}

	/**
	 * Open client to elaticsearch cluster
	 * 
	 * @param clusterName
	 */
	private void openClient(String clusterName) {
		logger.info("Using ElasticSearch hostnames: {} ", Arrays.toString(serverAddresses));

		Settings settings = null;
		org.elasticsearch.common.settings.Settings.Builder builder = null;
		if (this.elasticUser != null && !this.elasticUser.equals("")) {
			builder = Settings.builder().put("cluster.name", clusterName)
					.put("xpack.security.user", this.elasticUser + ":" + this.elasticPassword);
					// .put("shield.user",
					// this.elasticUser+":"+this.elasticPassword)
			
		} else {
			builder = Settings.builder().put("cluster.name", clusterName);
			
		}
		
		settings = builder.build();
		if(this.extendElasticsearchPropes != null && extendElasticsearchPropes.size() > 0){
			Iterator<Entry<Object, Object>> iterator = extendElasticsearchPropes.entrySet().iterator();
			while(iterator.hasNext()){
				builder.put(extendElasticsearchPropes);
			}
		}
		try{
			TransportClient transportClient = this.elasticUser != null && !this.elasticUser.equals("")
					? new PreBuiltXPackTransportClient(settings) : new PreBuiltTransportClient(settings);
			// TransportClient transportClient = new TransportClient(settings);
			for (InetSocketTransportAddress host : serverAddresses) {
				transportClient.addTransportAddress(host);
			}
			if (client != null) {
				client.close();
			}
			client = transportClient;
		}
		catch(RuntimeException e){
			e.printStackTrace();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
	}

	// /*
	// * FOR TESTING ONLY...
	// *
	// * Opens a local discovery node for talking to an elasticsearch server
	// running
	// * in the same JVM
	// */
	// private void openLocalDiscoveryClient() {
	// logger.info("Using ElasticSearch AutoDiscovery mode");
	// Node node = NodeBuilder.nodeBuilder().client(true).local(true).node();
	// if (client != null) {
	// client.close();
	// }
	// client = node.client();
	// }

	@Override
	public void configure(Properties elasticsearchPropes) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	public BulkRequestBuilder prepareBulk() {
		// TODO Auto-generated method stub
		return  client.prepareBulk();
	}

	 
 

 
	public IndexRequestBuilder createIndexRequest(IndexNameBuilder indexNameBuilder, Event event,ElasticSearchEventSerializer elasticSearchEventSerializer) throws IOException {

		IndexRequestBuilder indexRequestBuilder = null;
		if (indexRequestBuilderFactory == null) {
			XContentBuilder bytesStream = null;
			try {
				bytesStream = elasticSearchEventSerializer == null ?serializer.getContentBuilder(event):elasticSearchEventSerializer.getContentBuilder(event);
				indexRequestBuilder = client.prepareIndex(indexNameBuilder.getIndexName(event), event.getIndexType()).setSource(bytesStream);
			} finally {
				if (bytesStream != null) {
					// bytesStream.cl
				}
			}

		} else {
			indexRequestBuilder = indexRequestBuilderFactory.createIndexRequest(client, indexNameBuilder.getIndexPrefix(event),
					event.getIndexType(), event);
		}

		if (event.getTTL() > 0) {
			indexRequestBuilder.setTTL(event.getTTL());
		}
		return indexRequestBuilder;

	}
	
	
	public UpdateRequestBuilder updateIndexRequest( Event event,ElasticSearchEventSerializer elasticSearchEventSerializer) throws IOException {

		UpdateRequestBuilder indexRequestBuilder = null;
		 
			XContentBuilder bytesStream = null;
			try {
				bytesStream = elasticSearchEventSerializer == null ?serializer.getContentBuilder(event):elasticSearchEventSerializer.getContentBuilder(event);
				indexRequestBuilder = client.prepareUpdate(event.getIndexPrefix(), event.getIndexType(),event.getId())
						.setDoc(bytesStream);
			} finally {
				if (bytesStream != null) {
					// bytesStream.cl
				}
			}

		 

		 
		return indexRequestBuilder;

	}



	@Override
	public ClientUtil getClientUtil(IndexNameBuilder indexNameBuilder) {
		// TODO Auto-generated method stub
		return new TransportClientUtil(this,indexNameBuilder);
	}

	@Override
	public ClientUtil getConfigClientUtil(IndexNameBuilder indexNameBuilder, String configFile) {
		return null;
	}

	public DeleteRequestBuilder deleteIndex(String indexName, String indexType, String id) throws ElasticSearchException {
		return client.prepareDelete(indexName, indexType, id);
	}

	public Client getClient() {
		return client;
	}
}
