package org.frameworkset.elasticsearch.boot;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.frameworkset.util.SimpleStringUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseESProperties {
	private String name;
	private String elasticUser;
	private String elasticPassword;
	private Elasticsearch elasticsearch;
	private Http http;
	private Db db;

	public Ip getIp() {
		return ip;
	}

	public void setIp(Ip ip) {
		this.ip = ip;
	}

	private Ip ip;


	private Dslfile dslfile;
	public Dslfile getDslfile() {
		return dslfile;
	}

	public void setDslfile(Dslfile dslfile) {
		this.dslfile = dslfile;
	}
	public BaseESProperties(){
	}
	public Http getHttp() {
		return http;
	}

	public void setHttp(Http http) {
		this.http = http;
	}

	public Db getDb() {
		return db;
	}

	public void setDb(Db db) {
		this.db = db;
	}



	public String getElasticUser() {
		return elasticUser;
	}

	public void setElasticUser(String elasticUser) {
		this.elasticUser = elasticUser;
	}

	public String getElasticPassword() {
		return elasticPassword;
	}

	public void setElasticPassword(String elasticPassword) {
		this.elasticPassword = elasticPassword;
	}

	public Elasticsearch getElasticsearch() {
		return elasticsearch;
	}

	public void setElasticsearch(Elasticsearch elasticsearch) {
		this.elasticsearch = elasticsearch;
	}




	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public static class Ip{
		private String serviceUrl;
		private String cachesize;
		private String database;

		public String getServiceUrl() {
			return serviceUrl;
		}

		public void setServiceUrl(String serviceUrl) {
			this.serviceUrl = serviceUrl;
		}

		public String getCachesize() {
			return cachesize;
		}

		public void setCachesize(String cachesize) {
			this.cachesize = cachesize;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public String getAsnDatabase() {
			return asnDatabase;
		}

		public void setAsnDatabase(String asnDatabase) {
			this.asnDatabase = asnDatabase;
		}

		private String asnDatabase;
		private String ispConverter;
		private String ip2regionDatabase;

		public String getIspConverter() {
			return ispConverter;
		}

		public void setIspConverter(String ispConverter) {
			this.ispConverter = ispConverter;
		}

		public String getIp2regionDatabase() {
			return ip2regionDatabase;
		}

		public void setIp2regionDatabase(String ip2regionDatabase) {
			this.ip2regionDatabase = ip2regionDatabase;
		}
	}
	public static class Db{
		private String name;
		private String user;
		private String password;
		private String driver;
		private String url;
		private String usePool;
		private String validateSQL;
		private String maxSize;
		private String minIdleSize;
		private String initSize;
		private String showSql;
		private String dbtype;
		private String dbAdaptor;
		private String jdbcFetchSize;
		private String datasources;
		public String getColumnLableUpperCase() {
			return columnLableUpperCase;
		}

		public void setColumnLableUpperCase(String columnLableUpperCase) {
			this.columnLableUpperCase = columnLableUpperCase;
		}

		private String columnLableUpperCase ;
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDriver() {
			return driver;
		}

		public void setDriver(String driver) {
			this.driver = driver;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsePool() {
			return usePool;
		}

		public void setUsePool(String usePool) {
			this.usePool = usePool;
		}

		public String getValidateSQL() {
			return validateSQL;
		}

		public void setValidateSQL(String validateSQL) {
			this.validateSQL = validateSQL;
		}


		public String getMaxSize() {
			return maxSize;
		}

		public void setMaxSize(String maxSize) {
			this.maxSize = maxSize;
		}




		public String getInitSize() {
			return initSize;
		}

		public void setInitSize(String initSize) {
			this.initSize = initSize;
		}

		public String getShowSql() {
			return showSql;
		}

		public void setShowSql(String showSql) {
			this.showSql = showSql;
		}

		public String getDbtype() {
			return dbtype;
		}

		public void setDbtype(String dbtype) {
			this.dbtype = dbtype;
		}

		public String getDbAdaptor() {
			return dbAdaptor;
		}

		public void setDbAdaptor(String dbAdaptor) {
			this.dbAdaptor = dbAdaptor;
		}

		public String getJdbcFetchSize() {
			return jdbcFetchSize;
		}

		public void setJdbcFetchSize(String jdbcFetchSize) {
			this.jdbcFetchSize = jdbcFetchSize;
		}

		public String getMinIdleSize() {
			return minIdleSize;
		}

		public void setMinIdleSize(String minIdleSize) {
			this.minIdleSize = minIdleSize;
		}

		public String getDatasources() {
			return datasources;
		}

		public void setDatasources(String datasources) {
			this.datasources = datasources;
		}
	}
	public static class Http{
		private String keystore;
		private String keyPassword;
		private String hostnameVerifier;
		private String timeoutConnection;
		private String timeoutSocket;
		private String connectionRequestTimeout;
		private String automaticRetriesDisabled;
		private String backoffAuth;
		private String encodedAuthCharset;
		private String retryTime;
		private String retryInterval;
		private String maxLineLength;
		private String maxHeaderCount;
		private String maxTotal;
		private String defaultMaxPerRoute;
		private String soReuseAddress;
		private String soKeepAlive;
		private String timeToLive;
		private String validateAfterInactivity;
		private String keystoreAlias;
		private String trustAlias;
		private String supportedProtocols;
		private String truststore;
		private String trustPassword;

		public void setEncodedAuthCharset(String encodedAuthCharset) {
			this.encodedAuthCharset = encodedAuthCharset;
		}

		public String getEncodedAuthCharset() {
			return encodedAuthCharset;
		}

		public String getHttpClientBuilderCallback() {
			return httpClientBuilderCallback;
		}

		public void setBackoffAuth(String backoffAuth) {
			this.backoffAuth = backoffAuth;
		}

		public String getBackoffAuth() {
			return backoffAuth;
		}

		public void setHttpClientBuilderCallback(String httpClientBuilderCallback) {
			this.httpClientBuilderCallback = httpClientBuilderCallback;
		}

		private String httpClientBuilderCallback;
		/**
		 * 每次获取connection时校验连接，true，校验，false不校验，有性能开销，推荐采用
		 * validateAfterInactivity来控制连接是否有效
		 * 默认值false
		 */
		private String staleConnectionCheckEnabled = "false";

		public String getEvictExpiredConnections() {
			return evictExpiredConnections;
		}

		public void setEvictExpiredConnections(String evictExpiredConnections) {
			this.evictExpiredConnections = evictExpiredConnections;
		}

		private String evictExpiredConnections = "true";
		/**
		 * 自定义重试控制接口，必须实现接口方法
		 * public interface CustomHttpRequestRetryHandler  {
		 * 	public boolean retryRequest(IOException exception, int executionCount, HttpContext context,ClientConfiguration configuration);
		 * }
		 * 方法返回true，进行重试，false不重试
		 */
		private String customHttpRequestRetryHandler;

		public String getTimeoutConnection() {
			return timeoutConnection;
		}

		public void setTimeoutConnection(String timeoutConnection) {
			this.timeoutConnection = timeoutConnection;
		}

		public String getTimeoutSocket() {
			return timeoutSocket;
		}

		public void setTimeoutSocket(String timeoutSocket) {
			this.timeoutSocket = timeoutSocket;
		}

		public String getConnectionRequestTimeout() {
			return connectionRequestTimeout;
		}

		public void setConnectionRequestTimeout(String connectionRequestTimeout) {
			this.connectionRequestTimeout = connectionRequestTimeout;
		}

		public String getRetryTime() {
			return retryTime;
		}

		public void setRetryTime(String retryTime) {
			this.retryTime = retryTime;
		}

		public String getMaxLineLength() {
			return maxLineLength;
		}

		public void setMaxLineLength(String maxLineLength) {
			this.maxLineLength = maxLineLength;
		}

		public String getMaxHeaderCount() {
			return maxHeaderCount;
		}

		public void setMaxHeaderCount(String maxHeaderCount) {
			this.maxHeaderCount = maxHeaderCount;
		}

		public String getMaxTotal() {
			return maxTotal;
		}

		public void setMaxTotal(String maxTotal) {
			this.maxTotal = maxTotal;
		}

		public String getDefaultMaxPerRoute() {
			return defaultMaxPerRoute;
		}

		public void setDefaultMaxPerRoute(String defaultMaxPerRoute) {
			this.defaultMaxPerRoute = defaultMaxPerRoute;
		}

		public String getSoReuseAddress() {
			return soReuseAddress;
		}

		public void setSoReuseAddress(String soReuseAddress) {
			this.soReuseAddress = soReuseAddress;
		}

		public String getSoKeepAlive() {
			return soKeepAlive;
		}

		public void setSoKeepAlive(String soKeepAlive) {
			this.soKeepAlive = soKeepAlive;
		}

		public String getTimeToLive() {
			return timeToLive;
		}

		public void setTimeToLive(String timeToLive) {
			this.timeToLive = timeToLive;
		}

		public String getKeepAlive() {
			return keepAlive;
		}

		public void setKeepAlive(String keepAlive) {
			this.keepAlive = keepAlive;
		}

		private String keepAlive;



		public String getKeystore() {
			return keystore;
		}

		public void setKeystore(String keystore) {
			this.keystore = keystore;
		}

		public String getKeyPassword() {
			return keyPassword;
		}

		public void setKeyPassword(String keyPassword) {
			this.keyPassword = keyPassword;
		}

		public String getHostnameVerifier() {
			return hostnameVerifier;
		}

		public void setHostnameVerifier(String hostnameVerifier) {
			this.hostnameVerifier = hostnameVerifier;
		}


		public String getValidateAfterInactivity() {
			return validateAfterInactivity;
		}

		public void setValidateAfterInactivity(String validateAfterInactivity) {
			this.validateAfterInactivity = validateAfterInactivity;
		}

		public String getCustomHttpRequestRetryHandler() {
			return customHttpRequestRetryHandler;
		}

		public void setCustomHttpRequestRetryHandler(String customHttpRequestRetryHandler) {
			this.customHttpRequestRetryHandler = customHttpRequestRetryHandler;
		}

		public String isStaleConnectionCheckEnabled() {
			return staleConnectionCheckEnabled;
		}

		public void setStaleConnectionCheckEnabled(String staleConnectionCheckEnabled) {
			this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
		}

		public String getRetryInterval() {
			return retryInterval;
		}

		public void setRetryInterval(String retryInterval) {
			this.retryInterval = retryInterval;
		}

		public String getKeystoreAlias() {
			return keystoreAlias;
		}

		public void setKeystoreAlias(String keystoreAlias) {
			this.keystoreAlias = keystoreAlias;
		}

		public String getTrustAlias() {
			return trustAlias;
		}

		public void setTrustAlias(String trustAlias) {
			this.trustAlias = trustAlias;
		}

		public String getSupportedProtocols() {
			return supportedProtocols;
		}

		public void setSupportedProtocols(String supportedProtocols) {
			this.supportedProtocols = supportedProtocols;
		}

		public String getTruststore() {
			return truststore;
		}

		public void setTruststore(String truststore) {
			this.truststore = truststore;
		}

		public String getTrustPassword() {
			return trustPassword;
		}

		public void setTrustPassword(String trustPassword) {
			this.trustPassword = trustPassword;
		}

		public String getAutomaticRetriesDisabled() {
			return automaticRetriesDisabled;
		}

		public void setAutomaticRetriesDisabled(String automaticRetriesDisabled) {
			this.automaticRetriesDisabled = automaticRetriesDisabled;
		}
	}

	public static class Elasticsearch{
		private Rest rest;
		private String dateFormat;
		private String timeZone;
		private String ttl;
		private String showTemplate;
		private String sliceScrollThreadCount;
		private String sliceScrollThreadQueue;
		private String sliceScrollBlockedWaitTimeout;
		private String healthCheckInterval;
		private String failAllContinue;
		private String slowDslThreshold;
		private String useHttps;

		public String getFailAllContinue() {
			return failAllContinue;
		}

		public void setFailAllContinue(String failAllContinue) {
			this.failAllContinue = failAllContinue;
		}

		public void setUseHttps(String useHttps) {
			this.useHttps = useHttps;
		}

		public String getUseHttps() {
			return useHttps;
		}

		public String getSlowDslCallback() {
			return slowDslCallback;
		}

		public void setSlowDslCallback(String slowDslCallback) {
			this.slowDslCallback = slowDslCallback;
		}

		private String slowDslCallback;
		public String getIncludeTypeName() {
			return includeTypeName;
		}

		public void setIncludeTypeName(String includeTypeName) {
			this.includeTypeName = includeTypeName;
		}

		private String includeTypeName;
		private String scrollThreadCount;
		private String scrollThreadQueue;
		private String scrollBlockedWaitTimeout;
		private String discoverHost;
		private String version;
		public Rest getRest() {
			return rest;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
		
		public String getVersion() {
			return version;
		}
		
		public void setRest(Rest rest) {
			this.rest = rest;
		}

		public String getDateFormat() {
			return dateFormat;
		}

		public void setDateFormat(String dateFormat) {
			this.dateFormat = dateFormat;
		}

		public String getTimeZone() {
			return timeZone;
		}

		public void setTimeZone(String timeZone) {
			this.timeZone = timeZone;
		}

		public String getTtl() {
			return ttl;
		}

		public void setTtl(String ttl) {
			this.ttl = ttl;
		}

		public String getShowTemplate() {
			return showTemplate;
		}

		public void setShowTemplate(String showTemplate) {
			this.showTemplate = showTemplate;
		}

		public String getDiscoverHost() {
			return discoverHost;
		}

		public void setDiscoverHost(String discoverHost) {
			this.discoverHost = discoverHost;
		}

		public String getSliceScrollThreadQueue() {
			return sliceScrollThreadQueue;
		}

		public void setSliceScrollThreadQueue(String sliceScrollThreadQueue) {
			this.sliceScrollThreadQueue = sliceScrollThreadQueue;
		}

		public String getSliceScrollThreadCount() {
			return sliceScrollThreadCount;
		}

		public void setSliceScrollThreadCount(String sliceScrollThreadCount) {
			this.sliceScrollThreadCount = sliceScrollThreadCount;
		}

		public String getSliceScrollBlockedWaitTimeout() {
			return sliceScrollBlockedWaitTimeout;
		}

		public void setSliceScrollBlockedWaitTimeout(String sliceScrollBlockedWaitTimeout) {
			this.sliceScrollBlockedWaitTimeout = sliceScrollBlockedWaitTimeout;
		}

		public String getScrollBlockedWaitTimeout() {
			return scrollBlockedWaitTimeout;
		}

		public void setScrollBlockedWaitTimeout(String scrollBlockedWaitTimeout) {
			this.scrollBlockedWaitTimeout = scrollBlockedWaitTimeout;
		}

		public String getScrollThreadQueue() {
			return scrollThreadQueue;
		}

		public void setScrollThreadQueue(String scrollThreadQueue) {
			this.scrollThreadQueue = scrollThreadQueue;
		}

		public String getScrollThreadCount() {
			return scrollThreadCount;
		}

		public void setScrollThreadCount(String scrollThreadCount) {
			this.scrollThreadCount = scrollThreadCount;
		}

		public String getHealthCheckInterval() {
			return healthCheckInterval;
		}

		public void setHealthCheckInterval(String healthCheckInterval) {
			this.healthCheckInterval = healthCheckInterval;
		}

		public String getSlowDslThreshold() {
			return slowDslThreshold;
		}

		public void setSlowDslThreshold(String slowDslThreshold) {
			this.slowDslThreshold = slowDslThreshold;
		}
	}
	public static class Dslfile{
		private String refreshInterval;
		private String dslMappingDir;

		public String getRefreshInterval() {
			return refreshInterval;
		}

		public void setRefreshInterval(String refreshInterval) {
			this.refreshInterval = refreshInterval;
		}

		public void setDslMappingDir(String dslMappingDir) {
			this.dslMappingDir = dslMappingDir;
		}

		public String getDslMappingDir() {
			return dslMappingDir;
		}
	}
	public static class Rest{
		private String hostNames;
		public String getHostNames() {
			return hostNames;
		}

		public void setHostNames(String hostNames) {
			this.hostNames = hostNames;
		}


	}

	/**
	 * ##default集群配配置
	 * elasticUser=elastic
	 * elasticPassword=changeme
	 *
	 * #elasticsearch.rest.hostNames=10.1.236.88:9200
	 * #elasticsearch.rest.hostNames=127.0.0.1:9200
	 * #elasticsearch.rest.hostNames=10.21.20.168:9200
	 * elasticsearch.rest.hostNames=127.0.0.1:9200
	 * #elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
	 * elasticsearch.dateFormat=yyyy.MM.dd
	 * elasticsearch.timeZone=Asia/Shanghai
	 * elasticsearch.ttl=2d
	 * #在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
	 * elasticsearch.showTemplate=true
	 * elasticsearch.discoverHost=false
	 *
	 * ##default连接池配置
	 * http.timeoutConnection = 400000
	 * http.timeoutSocket = 400000
	 * http.connectionRequestTimeout=400000
	 * http.retryTime = 1
	 * http.maxLineLength = -1
	 * http.maxHeaderCount = 200
	 * http.maxTotal = 400
	 * http.defaultMaxPerRoute = 200
	 * http.soReuseAddress = false
	 * http.soKeepAlive = false
	 * http.timeToLive = 3600000
	 * http.keepAlive = 3600000
	 * http.keystore =
	 * http.keyPassword =
	 * # ssl 主机名称校验，是否采用default配置，
	 * # 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
	 * http.hostnameVerifier =
	 *
	 * # dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
	 * dslfile.refreshInterval = -1
	 *
	 *
	 * # 演示数据库数据导入elasticsearch源配置
	 * db.name = test
	 * db.user = root
	 * db.password = 123456
	 * db.driver = com.mysql.jdbc.Driver
	 * db.url = jdbc:mysql://localhost:3306/bboss
	 * db.usePool = false
	 * db.validateSQL = select 1
	 * @return
	 */

	public Map buildProperties(){
		Map properties = new HashMap();
		String _name = "";
		if(SimpleStringUtil.isNotEmpty(this.name)){
			properties.put("elasticsearch.serverNames",this.name);
			_name = name+".";
		}
		else {

		}
		if(SimpleStringUtil.isNotEmpty(this.elasticUser)){
			properties.put(_name+"elasticUser",this.elasticUser);
		}

//		##default集群配配置
		if(SimpleStringUtil.isNotEmpty(this.elasticPassword)){
			properties.put(_name+"elasticPassword",this.elasticPassword);
		}

		if(this.getElasticsearch() != null){
			if(this.getElasticsearch().getRest() != null){
				if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getRest().getHostNames()))
					properties.put(_name+"elasticsearch.rest.hostNames",this.getElasticsearch().getRest().getHostNames());
			}
			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getDateFormat()))
				properties.put(_name+"elasticsearch.dateFormat",this.getElasticsearch().getDateFormat());
			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getTimeZone()))
				properties.put(_name+"elasticsearch.timeZone",this.getElasticsearch().getTimeZone());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getTtl()))
				properties.put(_name+"elasticsearch.ttl",this.getElasticsearch().getTtl());

			//#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getShowTemplate()))
				properties.put(_name+"elasticsearch.showTemplate",this.getElasticsearch().getShowTemplate());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getDiscoverHost()))
				properties.put(_name+"elasticsearch.discoverHost",this.getElasticsearch().getDiscoverHost());
			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getVersion()))
				properties.put(_name+"elasticsearch.version",this.getElasticsearch().getVersion());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getSliceScrollThreadCount()))
				properties.put(_name+"elasticsearch.sliceScrollThreadCount",this.getElasticsearch().getSliceScrollThreadCount());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getSliceScrollThreadQueue()))
				properties.put(_name+"elasticsearch.sliceScrollThreadQueue",this.getElasticsearch().getSliceScrollThreadQueue());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getSliceScrollBlockedWaitTimeout()))
				properties.put(_name+"elasticsearch.sliceScrollBlockedWaitTimeout",this.getElasticsearch().getSliceScrollBlockedWaitTimeout());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getScrollThreadCount()))
				properties.put(_name+"elasticsearch.scrollThreadCount",this.getElasticsearch().getScrollThreadCount());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getScrollThreadQueue()))
				properties.put(_name+"elasticsearch.scrollThreadQueue",this.getElasticsearch().getScrollThreadQueue());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getScrollBlockedWaitTimeout()))
				properties.put(_name+"elasticsearch.scrollBlockedWaitTimeout",this.getElasticsearch().getScrollBlockedWaitTimeout());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getIncludeTypeName()))
				properties.put(_name+"elasticsearch.includeTypeName",this.getElasticsearch().getIncludeTypeName());
			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getHealthCheckInterval()))
				properties.put(_name+"elasticsearch.healthCheckInterval",this.getElasticsearch().getHealthCheckInterval());
			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getFailAllContinue()))
				properties.put(_name+"elasticsearch.failAllContinue",this.getElasticsearch().getFailAllContinue());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getSlowDslThreshold()))
				properties.put(_name+"elasticsearch.slowDslThreshold",this.getElasticsearch().getSlowDslThreshold());

			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getSlowDslCallback()))
				properties.put(_name+"elasticsearch.slowDslCallback",this.getElasticsearch().getSlowDslCallback());
			if(SimpleStringUtil.isNotEmpty(this.getElasticsearch().getUseHttps()))
				properties.put(_name+"elasticsearch.useHttps",this.getElasticsearch().getUseHttps());

		}


		//##http连接池配置
		if(this.getHttp() != null){
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getTimeoutConnection()))
				properties.put(_name+"http.timeoutConnection",this.getHttp().getTimeoutConnection());
			if(SimpleStringUtil.isNotEmpty(this.elasticUser)){
				properties.put(_name+"http.authAccount",this.elasticUser);

			}

//		##default集群配配置
			if(SimpleStringUtil.isNotEmpty(this.elasticPassword)){

				properties.put(_name+"http.authPassword",this.elasticPassword);
			}

			if(SimpleStringUtil.isNotEmpty(this.getHttp().getTimeoutSocket()))
				properties.put(_name+"http.timeoutSocket",this.getHttp().getTimeoutSocket());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getConnectionRequestTimeout()))
				properties.put(_name+"http.connectionRequestTimeout",this.getHttp().getConnectionRequestTimeout());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getAutomaticRetriesDisabled()))
				properties.put(_name+"http.automaticRetriesDisabled",this.getHttp().getAutomaticRetriesDisabled());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getBackoffAuth()))
				properties.put(_name+"http.backoffAuth",this.getHttp().getBackoffAuth());

			if(SimpleStringUtil.isNotEmpty(this.getHttp().getEncodedAuthCharset()))
				properties.put(_name+"http.encodedAuthCharset",this.getHttp().getEncodedAuthCharset());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getRetryTime()))
				properties.put(_name+"http.retryTime",this.getHttp().getRetryTime());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getRetryInterval()))
				properties.put(_name+"http.retryInterval",this.getHttp().getRetryInterval());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getMaxLineLength()))
				properties.put(_name+"http.maxLineLength",this.getHttp().getMaxLineLength());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getMaxHeaderCount()))
				properties.put(_name+"http.maxHeaderCount",this.getHttp().getMaxHeaderCount());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getMaxTotal()))
				properties.put(_name+"http.maxTotal",this.getHttp().getMaxTotal());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getDefaultMaxPerRoute()))
				properties.put(_name+"http.defaultMaxPerRoute",this.getHttp().getDefaultMaxPerRoute());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getSoReuseAddress()))
				properties.put(_name+"http.soReuseAddress",this.getHttp().getSoReuseAddress());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getSoKeepAlive()))
				properties.put(_name+"http.soKeepAlive",this.getHttp().getSoKeepAlive());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getTimeToLive()))
				properties.put(_name+"http.timeToLive",this.getHttp().getTimeToLive());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getKeepAlive()))
				properties.put(_name+"http.keepAlive",this.getHttp().getKeepAlive());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getKeystore()))
				properties.put(_name+"http.keystore",this.getHttp().getKeystore());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getKeyPassword()))
				properties.put(_name+"http.keyPassword",this.getHttp().getKeyPassword());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getHostnameVerifier()))
				properties.put(_name+"http.hostnameVerifier",this.getHttp().getHostnameVerifier());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getValidateAfterInactivity() ))
				properties.put(_name+"http.validateAfterInactivity",this.getHttp().getValidateAfterInactivity());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().isStaleConnectionCheckEnabled()))
				properties.put(_name+"http.staleConnectionCheckEnabled",this.getHttp().isStaleConnectionCheckEnabled());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getCustomHttpRequestRetryHandler()))
				properties.put(_name+"http.customHttpRequestRetryHandler",this.getHttp().getCustomHttpRequestRetryHandler());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getEvictExpiredConnections()))
				properties.put(_name+"http.evictExpiredConnections",this.getHttp().getEvictExpiredConnections());
/**
 * keystoreAlias
 * trustAlias
 * supportedProtocols
 * truststore
 * trustPassword
 */
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getKeystoreAlias()))
				properties.put(_name+"http.keystoreAlias",this.getHttp().getKeystoreAlias());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getTrustAlias() ))
				properties.put(_name+"http.trustAlias",this.getHttp().getTrustAlias());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getSupportedProtocols()))
				properties.put(_name+"http.supportedProtocols",this.getHttp().getSupportedProtocols());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getTruststore()))
				properties.put(_name+"http.truststore",this.getHttp().getTruststore());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getTrustPassword()))
				properties.put(_name+"http.trustPassword",this.getHttp().getTrustPassword());
			if(SimpleStringUtil.isNotEmpty(this.getHttp().getHttpClientBuilderCallback()))
				properties.put(_name + "http.httpClientBuilderCallback",this.getHttp().getHttpClientBuilderCallback());
		}

		if(dslfile != null){
			properties.put("dslfile.refreshInterval",dslfile.getRefreshInterval());
			if(dslfile.getDslMappingDir() != null && !dslfile.getDslMappingDir().trim().equals("")) {
				properties.put("dslfile.dslMappingDir", dslfile.getDslMappingDir());
			}

		}


		if(this.ip != null){
			if(SimpleStringUtil.isNotEmpty(this.ip.getDatabase()))
				properties.put("ip.database",this.ip.getDatabase());
			if(SimpleStringUtil.isNotEmpty(this.ip.getAsnDatabase()))
				properties.put("ip.asnDatabase",this.ip.getAsnDatabase());
			if(SimpleStringUtil.isNotEmpty(this.ip.getCachesize()))
				properties.put("ip.cachesize",this.ip.getCachesize());
			if(SimpleStringUtil.isNotEmpty(this.ip.getServiceUrl()))
				properties.put("ip.serviceUrl",this.ip.getServiceUrl());
			if(SimpleStringUtil.isNotEmpty(this.ip.getIspConverter()))
				properties.put("ip.ispConverter",this.ip.getIspConverter());
			if(SimpleStringUtil.isNotEmpty(this.ip.getIp2regionDatabase()))
				properties.put("ip.ip2regionDatabase",this.ip.getIp2regionDatabase());


		}
		return properties;
	}
}
