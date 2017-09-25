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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import org.elasticsearch.client.Client;
import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.elasticsearch.client.ElasticSearchClient;
import org.frameworkset.elasticsearch.client.ElasticSearchClientFactory;
import org.frameworkset.elasticsearch.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class ElasticSearch {

	private static final Logger logger = LoggerFactory
			.getLogger(ElasticSearch.class);
	private static final int defaultBatchSize = 100;
	private final Pattern pattern = Pattern.compile(TTL_REGEX,
			Pattern.CASE_INSENSITIVE);
	private Properties elasticsearchPropes;
	private Properties extendElasticsearchPropes;
	private int batchSize = defaultBatchSize;
	private long ttlMs = DEFAULT_TTL;
	private String clusterName = DEFAULT_CLUSTER_NAME;
	private String indexName = DEFAULT_INDEX_NAME;
	private String indexType = DEFAULT_INDEX_TYPE;
	private String clientType = DEFAULT_CLIENT_TYPE;
	private String elasticUser = "";
	private String elasticPassword = "";
	private Matcher matcher = pattern.matcher("");

	private String[] restServerAddresses = null;
	private String[] transportServerAddresses = null;
	private ElasticSearchClient restClient = null;
	private ElasticSearchClient transportClient = null;

	private ElasticSearchIndexRequestBuilderFactory indexRequestFactory;
	private ElasticSearchEventSerializer eventSerializer;
	private IndexNameBuilder indexNameBuilder;

	/**
	 * Create an {@link ElasticSearch} configured using the supplied
	 * configuration
	 */
	public ElasticSearch() {

	}



	@VisibleForTesting
	String[] getRestServerAddresses() {
		return restServerAddresses;
	}

	@VisibleForTesting
	String[] getTransportServerAddresses() {
		return transportServerAddresses;
	}

	@VisibleForTesting
	String getClusterName() {
		return clusterName;
	}

	@VisibleForTesting
	String getIndexName() {
		return indexName;
	}

	@VisibleForTesting
	String getIndexType() {
		return indexType;
	}

	@VisibleForTesting
	long getTTLMs() {
		return ttlMs;
	}

	@VisibleForTesting
	ElasticSearchEventSerializer getEventSerializer() {
		return eventSerializer;
	}

	@VisibleForTesting
	IndexNameBuilder getIndexNameBuilder() {
		return indexNameBuilder;
	}

	public ClientUtil getRestClientUtil() {
		if(restClient != null)
			return this.restClient.getClientUtil(this.indexNameBuilder);
		else
			return null;
	}

	public ClientUtil getConfigRestClientUtil(String configFile) {
		if(restClient != null)
			return this.restClient.getConfigClientUtil(this.indexNameBuilder,configFile);
		else
			return null;
	}

	public ClientUtil getTransportClientUtil() {
		if(transportClient != null)
			return this.transportClient.getClientUtil(this.indexNameBuilder);
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
			return transportClient.getClientUtil(indexNameBuilder).getClient();
		else
			return null;
	}


	public Object addIndexs(java.util.List<Event> datas, ElasticSearchEventSerializer elasticSearchEventSerializer) throws EventDeliveryException {
		logger.debug("processing...");
		/**
		 * 优先采用tcp协议
		 */
		ClientUtil clientUtil = getTransportClientUtil() ;
		if(clientUtil == null)
			clientUtil = this.getRestClientUtil();
		try {

			int count;
			for (count = 0; count < datas.size(); ++count) {
				Event event = datas.get(count);


				if (event == null) {
					break;
				}
				if (event.getTTL() <= 0l)
					event.setTTL(ttlMs);
				String realIndexType = event.getIndexType() == null ? BucketPath.escapeString(indexType, event.getHeaders()) : event.getIndexType();
				event.setIndexType(realIndexType);
				clientUtil.addEvent(event, elasticSearchEventSerializer);
			}


			return clientUtil.execute();

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
	 * @param datas
	 * @throws EventDeliveryException
	 */
	public Object addIndexs(java.util.List<Event> datas) throws EventDeliveryException {
		return addIndexs(datas, null);

	}

	public Object executeRequest(String path, String entity) throws Exception {
		ClientUtil clientUtil = this.getRestClientUtil();

		return clientUtil.executeRequest(path, entity);
	}

	public Object executeRequest(String path) throws Exception {
		ClientUtil clientUtil = this.getRestClientUtil();

		return clientUtil.executeRequest(path, (String) null);
	}

	/**
	 * 更新索引
	 *
	 * @param datas
	 * @param elasticSearchEventSerializer
	 * @throws EventDeliveryException
	 */
	public Object updateIndexs(java.util.List<Event> datas, ElasticSearchEventSerializer elasticSearchEventSerializer) throws EventDeliveryException {
		logger.debug("processing...");
		ClientUtil clientUtil = this.getTransportClientUtil();
		if(clientUtil == null)
			clientUtil = this.getRestClientUtil();
		try {

			int count;
			for (count = 0; count < datas.size(); ++count) {
				Event event = datas.get(count);


				if (event == null) {
					break;
				}
				if (event.getTTL() <= 0l)
					event.setTTL(ttlMs);
				String realIndexType = event.getIndexType() == null ? BucketPath.escapeString(indexType, event.getHeaders()) : event.getIndexType();
				event.setIndexType(realIndexType);
				clientUtil.updateIndexs(event, elasticSearchEventSerializer);
			}


			return clientUtil.execute();

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

	public Object updateIndexs(java.util.List<Event> datas) throws EventDeliveryException {
		return updateIndexs(datas, null);

	}

	public Object deleteIndex(String indexName, String indexType, String... ids) throws EventDeliveryException {
		ClientUtil clientUtil = this.getTransportClientUtil();
		if(clientUtil == null){
			clientUtil = this.getRestClientUtil();
		}
		try {

			clientUtil.deleteIndex(indexName, indexType, ids);
			return clientUtil.execute();

		} catch (EventDeliveryException ex) {

			throw ex;

		} catch (Throwable ex) {

//		        logger.error(
//		            "Exception in rollback. Rollback might not have been successful.",
//		            ex);
			throw new EventDeliveryException(ex);

		}
	}

	public void configure() {

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(REST_HOSTNAMES))) {
			restServerAddresses =
					elasticsearchPropes.getProperty(REST_HOSTNAMES).trim().split(",");
		}
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(TRANSPORT_HOSTNAMES))) {
			transportServerAddresses =
					elasticsearchPropes.getProperty(TRANSPORT_HOSTNAMES).trim().split(",");
		}
//		Preconditions.checkState(serverAddresses != null
//				&& serverAddresses.length > 0, "Missing Param:" + HOSTNAMES);


		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(INDEX_NAME))) {
			this.indexName = elasticsearchPropes.getProperty(INDEX_NAME);
		}

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(INDEX_TYPE))) {
			this.indexType = elasticsearchPropes.getProperty(INDEX_TYPE);
		}

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(CLUSTER_NAME))) {
			this.clusterName = elasticsearchPropes.getProperty(CLUSTER_NAME);
		}

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(BATCH_SIZE))) {
			this.batchSize = Integer.parseInt(elasticsearchPropes.getProperty(BATCH_SIZE));
		}
		String ttl = elasticsearchPropes.getProperty(TTL);
		if (SimpleStringUtil.isNotEmpty(ttl)) {
			this.ttlMs = parseTTL(ttl);
			logger.info("elasticsearch.TTL:"+ttlMs+",config value is:"+ttl);
//			Preconditions.checkState(ttlMs > 0, TTL
//					+ " must be greater than 0 or not set.");
		}

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(CLIENT_TYPE))) {
			clientType = elasticsearchPropes.getProperty(CLIENT_TYPE);
		}

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty("elasticUser"))) {
			elasticUser = elasticsearchPropes.getProperty("elasticUser");
		}
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty("elasticPassword"))) {
			elasticPassword = elasticsearchPropes.getProperty("elasticPassword");
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


		String indexNameBuilderClass = DEFAULT_INDEX_NAME_BUILDER_CLASS;
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(INDEX_NAME_BUILDER))) {
			indexNameBuilderClass = elasticsearchPropes.getProperty(INDEX_NAME_BUILDER);
		}



		try {
			@SuppressWarnings("unchecked")
			Class<? extends IndexNameBuilder> clazz
					= (Class<? extends IndexNameBuilder>) Class
					.forName(indexNameBuilderClass);
			indexNameBuilder = clazz.newInstance();
//      indexnameBuilderContext.put(INDEX_NAME, indexName);
			indexNameBuilder.configure(elasticsearchPropes);
			this.start();

		} catch (Exception e) {
			logger.error("Could not instantiate index name builder.", e);
			Throwables.propagate(e);
		}



//		Preconditions.checkState(SimpleStringUtil.isNotEmpty(indexName),
//				"Missing Param:" + INDEX_NAME);
//		Preconditions.checkState(SimpleStringUtil.isNotEmpty(indexType),
//				"Missing Param:" + INDEX_TYPE);
//		Preconditions.checkState(SimpleStringUtil.isNotEmpty(clusterName),
//				"Missing Param:" + CLUSTER_NAME);
//		Preconditions.checkState(batchSize >= 1, BATCH_SIZE
//				+ " must be greater than 0");
	}

	public void start() {
		ElasticSearchClientFactory clientFactory = new ElasticSearchClientFactory();

		logger.info("ElasticSearch client started");

		try {
			if(this.transportServerAddresses != null && this.transportServerAddresses.length > 0) {
				transportClient = clientFactory.getClient(ElasticSearchClientFactory.TransportClient, transportServerAddresses, this.elasticUser, this.elasticPassword,
						clusterName, eventSerializer, indexRequestFactory, extendElasticsearchPropes);
				transportClient.configure(elasticsearchPropes);
			}





		} catch (Exception ex) {
			logger.error("ES Transport Client started failed", ex);

			if (transportClient != null) {
				transportClient.close();

			}
		}

		try {


			if(this.restServerAddresses != null && this.restServerAddresses.length > 0) {
				restClient = clientFactory.getClient(ElasticSearchClientFactory.RestClient, restServerAddresses, this.elasticUser, this.elasticPassword,
						clusterName, eventSerializer, indexRequestFactory, extendElasticsearchPropes);
				restClient.configure(elasticsearchPropes);
			}



		} catch (Exception ex) {
			logger.error("ES Rest Client started failed", ex);

			if (restClient != null) {
				restClient.close();

			}
		}


	}


	public void stop() {
		logger.info("ElasticSearch client stopping");
		if (restClient != null) {
			restClient.close();
		}
		if (transportClient != null) {
			transportClient.close();
		}
	}

	/*
	 * Returns TTL value of ElasticSearch index in milliseconds when TTL specifier
	 * is "ms" / "s" / "m" / "h" / "d" / "w". In case of unknown specifier TTL is
	 * not set. When specifier is not provided it defaults to days in milliseconds
	 * where the number of days is parsed integer from TTL string provided by
	 * user. <p> Elasticsearch supports ttl values being provided in the format:
	 * 1d / 1w / 1ms / 1s / 1h / 1m specify a time unit like d (days), m
	 * (minutes), h (hours), ms (milliseconds) or w (weeks), milliseconds is used
	 * as default unit.
	 * http://www.elasticsearch.org/guide/reference/mapping/ttl-field/.
	 *
	 * @param ttl TTL value provided by user in flume configuration file for the
	 * sink
	 *
	 * @return the ttl value in milliseconds
	 */
	private long parseTTL(String ttl) {
		matcher = matcher.reset(ttl);
		while (matcher.find()) {
			if (matcher.group(2).equals("ms")) {
				return Long.parseLong(matcher.group(1));
			} else if (matcher.group(2).equals("s")) {
				return TimeUnit.SECONDS.toMillis(Integer.parseInt(matcher.group(1)));
			} else if (matcher.group(2).equals("m")) {
				return TimeUnit.MINUTES.toMillis(Integer.parseInt(matcher.group(1)));
			} else if (matcher.group(2).equals("h")) {
				return TimeUnit.HOURS.toMillis(Integer.parseInt(matcher.group(1)));
			} else if (matcher.group(2).equals("d")) {
				return TimeUnit.DAYS.toMillis(Integer.parseInt(matcher.group(1)));
			} else if (matcher.group(2).equals("w")) {
				return TimeUnit.DAYS.toMillis(7 * Integer.parseInt(matcher.group(1)));
			} else if (matcher.group(2).equals("")) {
				logger.info("TTL qualifier is empty. Defaulting to day qualifier.");
				return TimeUnit.DAYS.toMillis(Integer.parseInt(matcher.group(1)));
			} else {
				logger.debug("Unknown TTL qualifier provided. Setting TTL to 0.");
				return 0;
			}
		}
		logger.info("TTL not provided. Skipping the TTL config by returning 0.");
		return -1;
	}



	public Properties getExtendElasticsearchPropes() {
		return extendElasticsearchPropes;
	}

}
