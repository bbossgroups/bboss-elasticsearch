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
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ElasticSearchClient;
import org.frameworkset.elasticsearch.client.ElasticSearchClientFactory;
import org.frameworkset.elasticsearch.scroll.thread.ThreadPoolFactory;
import org.frameworkset.elasticsearch.template.BaseTemplateContainerImpl;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.assemble.GetProperties;
import org.frameworkset.spi.support.ApplicationObjectSupport;
import org.frameworkset.util.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
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
 * {@link ElasticSearch}</p> It is recommended as a secondary step
 * the ElasticSearch indexes are optimized for the specified serializer. This is
 * not handled by the sink but is typically done by deploying a config template
 * alongside the ElasticSearch deploy</p>
 * <p>
 * http://www.elasticsearch.org/guide/reference/api/admin-indices-templates.
 * html
 */
public class ElasticSearch extends ApplicationObjectSupport {

	private static final Logger logger = LoggerFactory
			.getLogger(ElasticSearch.class);
	protected static final int defaultBatchSize = 100;
	protected final Pattern pattern = Pattern.compile(TTL_REGEX,
			Pattern.CASE_INSENSITIVE);

	private ClientInterface detaultClientInterface;


	public String getElasticSearchName() {
		return elasticSearchName;
	}

	public void setElasticSearchName(String elasticSearchName) {
		this.elasticSearchName = elasticSearchName;
	}

	private String elasticSearchName;
	protected Properties elasticsearchPropes;
	protected Properties extendElasticsearchPropes;
	protected int batchSize = defaultBatchSize;
	protected long ttlMs = DEFAULT_TTL;
	protected String indexName = DEFAULT_INDEX_NAME;
	protected String indexType = DEFAULT_INDEX_TYPE;
	protected String dslMappingDir;
	protected String elasticUser = "";
	protected String elasticPassword = "";
	protected Matcher matcher = pattern.matcher("");

	protected String[] restServerAddresses = null;

	public void setDslMappingDir(String dslMappingDir) {
		this.dslMappingDir = dslMappingDir;
	}

	public String getDslMappingDir() {
		return dslMappingDir;
	}

	public String getOrigineRestServerAddresses() {
		return origineRestServerAddresses;
	}

	public void setOrigineRestServerAddresses(String origineRestServerAddresses) {
		this.origineRestServerAddresses = origineRestServerAddresses;
	}

	protected String origineRestServerAddresses;
	
	protected ElasticSearchClient restClient = null;
	protected ExecutorService sliceScrollQueryExecutorService;
	protected ExecutorService scrollQueryExecutorService;
	protected int sliceScrollThreadCount = 50;
	protected int sliceScrollThreadQueue = 100;
	protected long sliceScrollBlockedWaitTimeout = 0l;

	protected int scrollThreadCount = 50;
	protected int scrollThreadQueue = 100;
	protected long scrollBlockedWaitTimeout = 0l;

	public boolean isIncludeTypeName() {
		return includeTypeName;
	}

	public void setIncludeTypeName(boolean includeTypeName) {
		this.includeTypeName = includeTypeName;
	}

	protected boolean includeTypeName = false;

	protected IndexNameBuilder indexNameBuilder;

	/**
	 * Create an {@link ElasticSearch} configured using the supplied
	 * configuration
	 */
	public ElasticSearch() {

	}



	
	String[] getRestServerAddresses() {
		return restServerAddresses;
	}

	public FastDateFormat getIndexDateFormat(){
		return this.indexNameBuilder.getFastDateFormat();
	}

	

	String getIndexName() {
		return indexName;
	}


	String getIndexType() {
		return indexType;
	}

	long getTTLMs() {
		return ttlMs;
	}

	 
	IndexNameBuilder getIndexNameBuilder() {
		return indexNameBuilder;
	}

	/**
	 * 返回默认的elasticsearch客户端工具api，单例模式，多线程安全
	 * @return
	 */
	public ClientInterface getRestClientUtil() {

		if(detaultClientInterface == null) {
			if (restClient != null) {
				synchronized (this) {
					if(detaultClientInterface != null)
						return detaultClientInterface;
					return detaultClientInterface = this.restClient.getClientUtil(this.indexNameBuilder);
				}
			}
			else
				return null;
		}
		else {
			return detaultClientInterface;
		}
	}
	private Map<String,ClientInterface> configClientUtis = new HashMap<String,ClientInterface>();
	public ClientInterface getConfigRestClientUtil(String configFile) {
		ClientInterface clientInterface = configClientUtis.get(configFile);
		if(clientInterface != null)
			return clientInterface;
		else {
			if (restClient != null) {
				synchronized (configClientUtis) {
					clientInterface = configClientUtis.get(configFile);
					if(clientInterface != null)
						return clientInterface;
					clientInterface = this.restClient.getConfigClientUtil(this.indexNameBuilder, configFile);
					configClientUtis.put(configFile,clientInterface);
					return clientInterface;
				}
			}
			else {
				return null;
			}
		}
	}
	public ClientInterface getConfigRestClientUtil(BaseTemplateContainerImpl templateContainer) {
		ClientInterface clientInterface = configClientUtis.get(templateContainer.getNamespace());
		if(clientInterface != null)
			return clientInterface;
		else {
			if (restClient != null) {
				synchronized (configClientUtis) {
					clientInterface = configClientUtis.get(templateContainer.getNamespace());
					if(clientInterface != null)
						return clientInterface;
					clientInterface = this.restClient.getConfigClientUtil(this.indexNameBuilder, templateContainer);
					configClientUtis.put(templateContainer.getNamespace(),clientInterface);
					return clientInterface;
				}
			}
			else {
				return null;
			}
		}
	}
 

	public Object executeRequest(String path, String entity) throws Exception {
		ClientInterface clientUtil = this.getRestClientUtil();

		return clientUtil.executeRequest(path, entity);
	}

	public String getConfigContainerInfo(){
		if(this.getApplicationContext() != null)
			return getApplicationContext().getConfigfile();
		else{
			return "ElasticSearch Configs";
		}
	}
	private boolean fromspringboot;

	public void configure(){
		configureWithConfigContext(null);
	}
	public void configureWithConfigContext(GetProperties configContext) {
		if(configContext != null && configContext instanceof BaseApplicationContext)
			this.setApplicationContext((BaseApplicationContext)configContext);
		if(logger.isInfoEnabled()) {
			try {

				logger.info("Start Elasticsearch Datasource[{}] from springboot[{}]:{}", this.getElasticSearchName(), this.isFromspringboot(), SimpleStringUtil.object2json(elasticsearchPropes));
				if(!this.isFromspringboot()){
					if(logger.isDebugEnabled()){
						Exception exception = new Exception("Debug Elasticsearch Datasource["+this.getElasticSearchName()+"] start trace:if use spring boot and unload spring boot config right,please get the reason from question-answer document:https://esdoc.bbossgroups.com/#/question-answer ,if not ignore this message.");
						logger.debug("",exception);
					}
				}
			}
			catch (Exception e){

			}
		}
		origineRestServerAddresses = elasticsearchPropes.getProperty(REST_HOSTNAMES);
		if (SimpleStringUtil.isNotEmpty(origineRestServerAddresses)) {
			origineRestServerAddresses = origineRestServerAddresses.trim();

			restServerAddresses = origineRestServerAddresses.split(",");
		}
		
//		Preconditions.checkState(serverAddresses != null
//				&& serverAddresses.length > 0, "Missing Param:" + HOSTNAMES);


		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(INDEX_NAME))) {
			this.indexName = elasticsearchPropes.getProperty(INDEX_NAME);
		}

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(INDEX_TYPE))) {
			this.indexType = elasticsearchPropes.getProperty(INDEX_TYPE);
		}

		
		String bz = elasticsearchPropes.getProperty(BATCH_SIZE);
		if (SimpleStringUtil.isNotEmpty(bz)) {
			try {
				this.batchSize = Integer.parseInt(bz);
			}
			catch (Exception e){
				logger.warn(bz,e);
			}
		}
//		CLIENT_sliceScrollThreadCount = "elasticsearch.sliceScrollThreadCount";
//		public static final String CLIENT_sliceScrollThreadQueue
		String _sliceScrollThreadCount = elasticsearchPropes.getProperty(CLIENT_sliceScrollThreadCount);
		if (SimpleStringUtil.isNotEmpty(_sliceScrollThreadCount)) {
			try {
				this.sliceScrollThreadCount = Integer.parseInt(_sliceScrollThreadCount);
			}
			catch (Exception e){
				logger.warn(_sliceScrollThreadCount,e);
			}
		}
		String _sliceScrollBlockedWaitTimeout = elasticsearchPropes.getProperty(CLIENT_sliceScrollBlockedWaitTimeout);
		if (SimpleStringUtil.isNotEmpty(_sliceScrollBlockedWaitTimeout)) {
			try {
				this.sliceScrollBlockedWaitTimeout = Long.parseLong(_sliceScrollBlockedWaitTimeout);
			}
			catch (Exception e){
				logger.warn(_sliceScrollBlockedWaitTimeout,e);
			}
		}
		String _includeTypeName = elasticsearchPropes.getProperty(CLIENT_includeTypeName);
		if (SimpleStringUtil.isNotEmpty(_includeTypeName)) {
			try {
				this.includeTypeName = Boolean.parseBoolean(_includeTypeName);
			}
			catch (Exception e){
				logger.warn(_includeTypeName,e);
			}
		}
		String _sliceScrollThreadQueue = elasticsearchPropes.getProperty(CLIENT_sliceScrollThreadQueue);
		if (SimpleStringUtil.isNotEmpty(_sliceScrollThreadQueue)) {
			try {
				this.sliceScrollThreadQueue = Integer.parseInt(_sliceScrollThreadQueue);
			}
			catch (Exception e){
				logger.warn(_sliceScrollThreadQueue,e);
			}
		}
		String _scrollThreadCount = elasticsearchPropes.getProperty(CLIENT_scrollThreadCount);
		if (SimpleStringUtil.isNotEmpty(_scrollThreadCount)) {
			try {
				this.scrollThreadCount = Integer.parseInt(_scrollThreadCount);
			}
			catch (Exception e){
				logger.warn(_scrollThreadCount,e);
			}
		}
		String _scrollBlockedWaitTimeout = elasticsearchPropes.getProperty(CLIENT_scrollBlockedWaitTimeout);
		if (SimpleStringUtil.isNotEmpty(_scrollBlockedWaitTimeout)) {
			try {
				this.scrollBlockedWaitTimeout = Long.parseLong(_scrollBlockedWaitTimeout);
			}
			catch (Exception e){
				logger.warn(_scrollBlockedWaitTimeout,e);
			}
		}
		String _scrollThreadQueue = elasticsearchPropes.getProperty(CLIENT_scrollThreadQueue);
		if (SimpleStringUtil.isNotEmpty(_scrollThreadQueue)) {
			try {
				this.scrollThreadQueue = Integer.parseInt(_scrollThreadQueue);
			}
			catch (Exception e){
				logger.warn(_scrollThreadQueue,e);
			}
		}
		String ttl = elasticsearchPropes.getProperty(TTL);
		if (SimpleStringUtil.isNotEmpty(ttl)) {
			this.ttlMs = parseTTL(ttl);
			logger.info("elasticsearch.TTL:"+ttlMs+",config value is:"+ttl);
//			Preconditions.checkState(ttlMs > 0, TTL
//					+ " must be greater than 0 or not set.");
		}

		dslMappingDir = ElasticSearchHelper.getDslfileMappingDir();

		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty("elasticUser"))) {
			elasticUser = elasticsearchPropes.getProperty("elasticUser");
		}
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty("elasticPassword"))) {
			elasticPassword = elasticsearchPropes.getProperty("elasticPassword");
		}
		String indexNameBuilderClass = getIndexNameBuilderClass();

		try {
			@SuppressWarnings("unchecked")
			Class<? extends IndexNameBuilder> clazz
					= (Class<? extends IndexNameBuilder>) Class
					.forName(indexNameBuilderClass);
			indexNameBuilder = clazz.newInstance();
//      indexnameBuilderContext.put(INDEX_NAME, indexName);
			indexNameBuilder.configure(elasticsearchPropes);
			if(configContext == null)//booter的情况下，从外部启动
				this.start();

		} catch (Exception e) {
			throw new ElasticSearchException("Could not instantiate index name builder.",e);
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
	
	protected String getIndexNameBuilderClass(){
		String indexNameBuilderClass = DEFAULT_INDEX_NAME_BUILDER_CLASS;
		if (SimpleStringUtil.isNotEmpty(elasticsearchPropes.getProperty(INDEX_NAME_BUILDER))) {
			indexNameBuilderClass = elasticsearchPropes.getProperty(INDEX_NAME_BUILDER);
		}
		return indexNameBuilderClass;
	}

	protected void start() {
		ElasticSearchClientFactory clientFactory = new ElasticSearchClientFactory();


		try {

			
			if(this.restServerAddresses != null && this.restServerAddresses.length > 0) {
				logger.info("Start ElasticSearch rest client:"+origineRestServerAddresses);
				restClient = clientFactory.getClient(this,ElasticSearchClientFactory.RestClient, restServerAddresses, this.elasticUser, this.elasticPassword,
						  extendElasticsearchPropes);
				restClient.configure(elasticsearchPropes);
				restClient.init();
				logger.info("ElasticSearch rest client started.");
				BaseApplicationContext.addShutdownHook(new Runnable() {
					@Override
					public void run() {
						stop();
					}
				});
			}
		} catch (Exception ex) {
			logger.error("ElasticSearch Rest Client started failed", ex);

			if (restClient != null) {
				restClient.close();

			}
		}


	}


	private boolean stoped = false;
	public synchronized void stop() {
		if(stoped )
			return;
		stoped = true;
		logger.info("ElasticSearch client stopping");
		if (restClient != null) {
			restClient.close();
		}
		this.configClientUtis.clear();
		this.detaultClientInterface = null;
		if(this.sliceScrollQueryExecutorService != null){
			this.sliceScrollQueryExecutorService.shutdown();
		}

		if(this.scrollQueryExecutorService != null){
			this.scrollQueryExecutorService.shutdown();
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
	protected long parseTTL(String ttl) {
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

	public void setElasticsearchPropes(Properties elasticsearchPropes) {
		this.elasticsearchPropes = elasticsearchPropes;
	}

	public Properties getExtendElasticsearchPropes() {
		return extendElasticsearchPropes;
	}
	public ExecutorService getSliceScrollQueryExecutorService(){
		if(sliceScrollQueryExecutorService != null)
			return sliceScrollQueryExecutorService;
		synchronized (this) {
			if(sliceScrollQueryExecutorService != null)
				return sliceScrollQueryExecutorService;
			if (this.sliceScrollQueryExecutorService == null) {
				sliceScrollQueryExecutorService = ThreadPoolFactory.buildSliceScrollThreadPool(this.sliceScrollThreadCount, this.sliceScrollThreadQueue,this.sliceScrollBlockedWaitTimeout);
			}
		}
		return this.sliceScrollQueryExecutorService;
	}


	public ExecutorService getScrollQueryExecutorService(){
		if(scrollQueryExecutorService != null)
			return scrollQueryExecutorService;
		synchronized (this) {
			if(scrollQueryExecutorService != null)
				return scrollQueryExecutorService;
			if (this.scrollQueryExecutorService == null) {
				scrollQueryExecutorService = ThreadPoolFactory.buildScrollThreadPool(this.scrollThreadCount, this.scrollThreadQueue,this.scrollBlockedWaitTimeout);
			}
		}
		return this.scrollQueryExecutorService;
	}


	public boolean isFromspringboot() {
		return fromspringboot;
	}

	public void setFromspringboot(boolean fromspringboot) {
		this.fromspringboot = fromspringboot;
	}


}
