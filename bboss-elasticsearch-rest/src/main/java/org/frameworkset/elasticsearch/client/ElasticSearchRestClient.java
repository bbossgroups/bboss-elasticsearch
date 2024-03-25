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

import com.frameworkset.util.SimpleStringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.*;
import org.frameworkset.elasticsearch.handler.BaseExceptionResponseHandler;
import org.frameworkset.elasticsearch.handler.ESStringResponseHandler;
import org.frameworkset.elasticsearch.template.BaseTemplateContainerImpl;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.frameworkset.util.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;

//import org.apache.http.client.HttpClient;
//import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Rest ElasticSearch client which is responsible for sending bulks of events to
 * ElasticSearch using ElasticSearch HTTP API. This is configurable, so any
 * config params required should be taken through this.
 */
public class ElasticSearchRestClient implements ElasticSearchClient {

	public static final String INDEX_OPERATION_NAME = "index";
	public static final String INDEX_PARAM = "_index";
	public static final String TYPE_PARAM = "_type";
	public static final String TTL_PARAM = "_ttl";
	public static final String BULK_ENDPOINT = "_bulk";
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchRestClient.class);
	protected final RoundRobinList serversList;
	protected Properties extendElasticsearchPropes;
	protected String httpPool;
	protected String elasticUser;
	protected String elasticPassword;
	protected long healthCheckInterval = -1l;

	public boolean isFailAllContinue() {
		return failAllContinue;
	}

	/**
	 * 如果所有节点都挂掉，是否允许使用挂掉节点处理请求
	 */
	protected boolean failAllContinue = true;

	protected RestSearchExecutor restSeachExecutor;
//	private HttpClient httpClient;
//	protected Map<String, String> headers = new HashMap<String, String>();
	protected boolean showTemplate = false;

	protected List<ESAddress> addressList;
 
	protected FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy.MM.dd",
      TimeZone.getTimeZone("Etc/UTC"));
    protected Integer slowDslThreshold;
	protected String dateFormat = "yyyy.MM.dd";
	/**
	 * 默认分表策略日期格式
	 */
	protected String dayDateFormat = "yyyy.MM.dd";
	protected String monthDateFormat = "yyyy.MM";
	protected String yearDateFormat = "yyyy";

	protected TimeZone timeZone = TimeZone.getTimeZone("Etc/UTC");
	protected  boolean discoverHost = false;
	protected LogDslCallback slowDslCallback;
	protected LogDslCallback logDslCallback;
	private boolean useHttps;

	public String getDayDateFormat() {
		return dayDateFormat;
	}

	public String getMonthDateFormat() {
		return monthDateFormat;
	}

	public String getYearDateFormat() {
		return yearDateFormat;
	}

	public boolean isUseHttps() {
		return useHttps;
	}
	public String getElasticsearchName(){
		return elasticSearch.getElasticSearchName();
	}
	public ElasticSearch getElasticSearch() {
		return elasticSearch;
	}
	public Integer slowDslThreshold(){
		return slowDslThreshold;
	}
	public LogDslCallback getSlowDslCallback(){
		return slowDslCallback;
	}

	public LogDslCallback getLogDslCallback(){
		return logDslCallback;
	}
	protected ElasticSearch elasticSearch;
	protected HealthCheck healthCheck = null;
	protected HostDiscover hostDiscover;
	private Map clusterInfo ;
	private String esVersion;
	private String distribution;
	private boolean v1 ;

	public boolean isLower5() {
		return lower5;
	}

	public void setLower5(boolean lower5) {
		this.lower5 = lower5;
	}

	private boolean lower5;

	public boolean isUpper7() {
		return upper7;
	}

	public void setUpper7(boolean upper7) {
		this.upper7 = upper7;
	}

	/**
	 * 是否高于或者等于es 7
	 */
	private boolean upper7 ;
	private int version;

	/**
	 * 是否高于或者等于es 8
	 */
	private boolean upper8 ;
	private String clusterVersionInfo;

	private String clusterVarcharInfo;

	public Map<String, ESAddress> getAddressMap() {
		return addressMap;
	}

	public int getVersion() {
		return version;
	}

	public boolean isUpper8() {
		return upper8;
	}

	private final Map<String,ESAddress> addressMap = new HashMap<String,ESAddress>();
	public ElasticSearchRestClient(ElasticSearch elasticSearch,String[] hostNames, String elasticUser, String elasticPassword,
								     Properties extendElasticsearchPropes) {
		this.extendElasticsearchPropes = extendElasticsearchPropes;
		this.elasticSearch = elasticSearch;
		addressList = new ArrayList<ESAddress>();
		for(String host:hostNames){
			ESAddress esAddress = new ESAddress(host,elasticSearch.getHealthPath());
			addressList.add(esAddress);
			addressMap.put(esAddress.getAddress(),esAddress);
		}
		serversList = new RoundRobinList(addressList);
//		httpClient = new DefaultHttpClient();
		this.elasticUser = elasticUser;
		this.elasticPassword = elasticPassword;
	}

	public boolean containAddress(ESAddress address){
		return addressMap.containsKey(address.getAddress());
	}
	public void handleRemoved(List<ESAddress> hosts){
		boolean hasHosts = true;
		if(hosts == null || hosts.size() == 0){//没有可用节点
			hasHosts = false;
		}
		Iterator<Map.Entry<String, ESAddress>> iterator = this.addressMap.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, ESAddress> esAddressEntry = iterator.next();
			String host = esAddressEntry.getKey();
			ESAddress address = esAddressEntry.getValue();
			if(hasHosts) {
				boolean exist = false;
				for (ESAddress httpHost : hosts) {
					if (httpHost.toString().equals(host)) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					address.setStatus(2);
					if(logger.isInfoEnabled()){
						logger.info("ElasticSearch Node["+address.toString()+"] is down or removed.");
					}
				}
			}
			else {
				address.setStatus(2);
				if(logger.isInfoEnabled()){
					logger.info("ElasticSearch Node["+address.toString()+"] is down  or removed.");
				}
			}

		}

	}
	public void addAddresses(List<ESAddress> address){
		this.serversList.addAddresses(address);
		if(this.healthCheck != null){
			this.healthCheck.checkNewAddresses(address);
		}
		for(ESAddress host:address){
			addressMap.put(host.getAddress(),host);
		}
		if(logger.isInfoEnabled()){
			StringBuilder info = new StringBuilder();
			info.append("All Live ElasticSearch Server:");
			Iterator<Map.Entry<String, ESAddress>> iterator = this.addressMap.entrySet().iterator();
			boolean firsted = true;
			while(iterator.hasNext()){
				Map.Entry<String, ESAddress> esAddressEntry = iterator.next();
				String host = esAddressEntry.getKey();

				if(firsted){
					info.append(host);
					firsted = false;
				}
				else{
					info.append(",").append(host);
				}
			}
			logger.info(info.toString());
		}
	}

	private void initVersionInfo(){
		try {
			//获取es的实际版本信息
			this.getElasticSearch().getRestClientUtil().discover("/", ClientInterface.HTTP_GET, new ResponseHandler<Void>() {

				@Override
				public Void handleResponse(HttpResponse response) throws IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						clusterVarcharInfo = entity != null ? EntityUtils.toString(entity) : null;
						if (logger.isInfoEnabled()) {
							logger.info("Elasticsearch Server Info:\n" + clusterVarcharInfo);
						}
						clusterInfo = SimpleStringUtil.json2Object(clusterVarcharInfo, Map.class);
						Object version = clusterInfo.get("version");
						if (version instanceof Map) {
							Map vinfo = (Map) version;
							String _esVersion = String.valueOf(vinfo.get("number"));
							if(vinfo.get("distribution") != null) {
								String _distribution = String.valueOf(vinfo.get("distribution"));

								distribution = _distribution;
							}
							if(_esVersion != null && !_esVersion.equals(""))
								esVersion = _esVersion;
							
							clusterVersionInfo = "clusterName:" + clusterInfo.get("cluster_name") + ",version:" + esVersion;
						} else {
							clusterVersionInfo = "clusterName:" + clusterInfo.get("cluster_name") + ",version:" + version;
						}
					} else {

					}
					return null;
				}
			});
			
		}
		catch (Exception e){
			logger.warn("Init Elasticsearch Cluster Version Information failed:",e);
		}
		if (esVersion != null) {

			if(distribution != null && 	distribution.toLowerCase().indexOf("opensearch") >= 0){
				version = 7;
				upper7 = true;
			}
			else {
				int idx = esVersion.indexOf(".");
				if (idx > 0) {
					String max = esVersion.substring(0, idx);
					try {

						int v = Integer.parseInt(max);

						version = v;
						if (v == 1) {
//						if (esVersion.startsWith("1.")) {
//							v1 = true;
//						}
							v1 = true;
						}
						if (v >= 8) {
							upper8 = true;
						}
						else if (v >= 7) {
							upper7 = true;
						}

						if (v < 5) {
							lower5 = true;
						}
					} catch (Exception e) {

					}
				}
			}
		}
	}
    public static final String _xpack6_sql_restapi = "/_xpack/sql";
    public static final String _xpack8_sql_restapi = "/_sql";

    private String sqlRestapi = _xpack8_sql_restapi;
	private String healthPool;
	private String discoverPool;

	public String getHealthPool() {
		return healthPool;
	}

    public String getSqlRestapi() {
        return sqlRestapi;
    }

    public boolean healthCheckEnabled(){
		return healthCheckInterval > 0;
	}
	public void init() {
		//Authorization
//		if (elasticUser != null && !elasticUser.equals(""))
//			headers.put("Authorization", getHeader(elasticUser, elasticPassword));
		discoverPool = ClientConfiguration.getHealthPoolName(this.httpPool);
		healthPool = discoverPool;
		restSeachExecutor = new RestSearchExecutor(this.httpPool,discoverPool,this);
		if(healthCheckInterval > 0) {
			logger.info("Start Elasticsearch healthCheck thread,you can set elasticsearch.healthCheckInterval=-1 in "+this.elasticSearch.getConfigContainerInfo()+" to disable healthCheck thread.");

			healthCheck = new HealthCheck(this.getElasticSearch().getElasticSearchName(),healthPool,addressList, healthCheckInterval);
			healthCheck.run();
		}
		else {
			logger.info("Elasticsearch healthCheck disable,you can set elasticsearch.healthCheckInterval=3000 in "+this.elasticSearch.getConfigContainerInfo()+" to enabled healthCheck thread.");

		}
		initVersionInfo();
		if(discoverHost) {

			logger.info("Start elastic discoverHost thread,to distabled set elasticsearch.discoverHost=false in "+this.elasticSearch.getConfigContainerInfo()+".");

			HostDiscover hostDiscover = new HostDiscover(this.getElasticSearch().getElasticSearchName(),this);

			hostDiscover.start();
			this.hostDiscover = hostDiscover;
		}
		else {
			logger.info("Discover Elasticsearch Host is disabled,to enabled set elasticsearch.discoverHost=true  in "+this.elasticSearch.getConfigContainerInfo()+".");
		}

	}


	/**
	public static String getHeader(String user, String password) {
		String auth = user + ":" + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(encodedAuth);
	}*/

	@Override
	public void configure(Properties elasticsearchPropes) {
		String dateFormatString = elasticsearchPropes.getProperty(TimeBasedIndexNameBuilder.DATE_FORMAT);

	    String timeZoneString = elasticsearchPropes.getProperty(TimeBasedIndexNameBuilder.TIME_ZONE);
	    
	    String showTemplate_ = elasticsearchPropes.getProperty("elasticsearch.showTemplate");
	    String httpPool = elasticsearchPropes.getProperty("elasticsearch.httpPool");

	    if(httpPool == null || httpPool.equals("")){
			httpPool = "default";
		}
		this.httpPool = httpPool;
	    if(showTemplate_ != null && showTemplate_.equals("true")){
	    	this.showTemplate = true;
	    }

	    if (SimpleStringUtil.isEmpty(dateFormatString)) {
	      dateFormatString = TimeBasedIndexNameBuilder.DEFAULT_DATE_FORMAT;
	    }
	    if (SimpleStringUtil.isEmpty(timeZoneString)) {
	      timeZoneString = TimeBasedIndexNameBuilder.DEFAULT_TIME_ZONE;
	    }
	    this.dateFormat = dateFormatString;
	    this.timeZone = TimeZone.getTimeZone(timeZoneString);
	    fastDateFormat = FastDateFormat.getInstance(dateFormatString,
	        TimeZone.getTimeZone(timeZoneString));
	    String healthCheckInterval_ = elasticsearchPropes.getProperty("elasticsearch.healthCheckInterval");
		if(healthCheckInterval_ == null){
			this.healthCheckInterval = 3000l;
		}
		else{
			try {
				this.healthCheckInterval = Long.parseLong(healthCheckInterval_);
			}
			catch (Exception e){
				logger.error("Parse Long healthCheckInterval parameter failed:"+healthCheckInterval_,e);
			}
		}

		String failAllContinue_ = elasticsearchPropes.getProperty("elasticsearch.failAllContinue");
		if(failAllContinue_ != null && failAllContinue_.equals("false")){
			this.failAllContinue = false;
		}
		String _slowDslThreshold = elasticsearchPropes.getProperty("elasticsearch.slowDslThreshold");
		if(_slowDslThreshold != null){
			try {
				this.slowDslThreshold = Integer.parseInt(_slowDslThreshold);
			}
			catch (Exception e){
				logger.error("Parse Long slowDslThreshold parameter failed:"+_slowDslThreshold,e);
			}
		}
        
        String _sqlRestapi = elasticsearchPropes.getProperty("elasticsearch.sqlRestapi");
        if(_sqlRestapi != null && !_sqlRestapi.equals("")){
            this.sqlRestapi = _sqlRestapi;            
        }


		String _logDslCallback = elasticsearchPropes.getProperty("elasticsearch.logDslCallback");
		if(_logDslCallback != null){
			try {
				this.logDslCallback = (LogDslCallback) Class.forName(_logDslCallback).newInstance();
			}
			catch (Exception e){
				logger.error("Parse logDslCallback parameter failed:"+_logDslCallback,e);
			}
			catch (Throwable e){
				logger.error("Parse logDslCallback parameter failed:"+_logDslCallback,e);
			}
		}
        if(slowDslThreshold != null && slowDslThreshold > 0) {
            String _slowDslCallback = elasticsearchPropes.getProperty("elasticsearch.slowDslCallback");
            if (_slowDslCallback != null) {
                try {
                    this.slowDslCallback = (LogDslCallback) Class.forName(_slowDslCallback).newInstance();
                } catch (Exception e) {
                    logger.error("Parse slowDslCallback parameter failed:" + _slowDslCallback, e);
                } catch (Throwable e) {
                    logger.error("Parse slowDslCallback parameter failed:" + _slowDslCallback, e);
                }
            } else {

                this.slowDslCallback = new DefaultSlowDslCallback();
            }
        }
		String discoverHost_ = elasticsearchPropes.getProperty("elasticsearch.discoverHost");
		if(discoverHost_ != null && !discoverHost_.equals("")){
			try {
				this.discoverHost = Boolean.parseBoolean(discoverHost_);
			}
			catch (Exception e){
				logger.error("Parse Boolean discoverHost parameter failed:"+discoverHost_,e);
			}
		}
		String version_ = elasticsearchPropes.getProperty("elasticsearch.version");
		if(version_ != null && !version_.equals("")){
			esVersion = version_;
		}
		else{
			esVersion = "7.0.0";
		}


		String useHttps_ = elasticsearchPropes.getProperty("elasticsearch.useHttps");
		if(useHttps_ != null && !useHttps_.equals("")){
			try {
				this.useHttps = Boolean.parseBoolean(useHttps_);
			}
			catch (Exception e){
				logger.error("Parse Boolean useHttps parameter failed:"+useHttps_,e);
			}
		}


	}
	private boolean closed = false;
	@Override
	public synchronized void close() {
		if(closed )
			return;
		closed = true;
		if(hostDiscover != null){
			hostDiscover.stopCheck();
			hostDiscover = null;
		}

		if(healthCheck != null){
			healthCheck.stopCheck();
			healthCheck = null;
		}

		ClientConfiguration.stopHttpClient( httpPool);

	}

	
	private ElasticSearchException handleConnectionPoolTimeOutException(String url,ConnectionPoolTimeoutException ex){
		ClientConfiguration configuration = ClientConfiguration.getClientConfiguration(this.httpPool);
		if(configuration == null){
			return new ElasticSearchException(ex);
		}
		else{
			StringBuilder builder = new StringBuilder();
			builder.append(url).append(" Wait Connection timeout for ").append(configuration.getConnectionRequestTimeout()).append("ms for idle http connection from http connection pool.");

			return new ElasticSearchException(builder.toString(),ex);
		}
	}

	private NoServerElasticSearchException handleConnectionTimeOutException(String url,ConnectTimeoutException ex){
		ClientConfiguration configuration = ClientConfiguration.getClientConfiguration(this.httpPool);
		if(configuration == null){
			return new NoServerElasticSearchException(url,ex);
		}
		else{
			StringBuilder builder = new StringBuilder();
			builder.append(url).append(" http connection timeout for ").append(configuration.getTimeoutConnection()).append("ms.");

			return new NoServerElasticSearchException(builder.toString(),ex);
		}
	}

	private ElasticSearchException handleSocketTimeoutException(String url,SocketTimeoutException ex){
		ClientConfiguration configuration = ClientConfiguration.getClientConfiguration(this.httpPool);
		if(configuration == null){
			StringBuilder builder = new StringBuilder();
			builder.append(url).append(" handle Socket Timeout ");
			return new ElasticSearchException(builder.toString(),ex);
		}
		else{
			StringBuilder builder = new StringBuilder();
			builder.append(url).append(" handle Socket Timeout for ").append(configuration.getTimeoutSocket()).append("ms.");

			return new ElasticSearchException(builder.toString(),ex);
		}
	}


	public String execute(final String entity,String options) throws ElasticSearchException {
		String endpoint = BULK_ENDPOINT;
		if(options != null){
			endpoint = new StringBuilder().append(endpoint).append("?").append(options).toString();
		}
		final ESStringResponseHandler responseHandler = new ESStringResponseHandler();
		return _executeHttp(endpoint, responseHandler,  new ExecuteRequest() {
			@Override
			public Object execute(ESAddress host,String url,int triesCount) throws Exception {
				Object response = null;

				if(showTemplate ){
					if(logger.isInfoEnabled()) {
						logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
					}

				}
				response = restSeachExecutor.execute(url,entity,responseHandler);
				return response;
			}
		});
		/**
		int triesCount = 0;
		String response = null;
		Throwable e = null;
		ESAddress host = null;
		String url = null;


		while (true) {


			try {
				host = serversList.get(failAllContinue || !this.healthCheckEnabled());
				url = new StringBuilder().append(host.getAddress()).append( "/" ).append( endpoint).toString();

//				response = HttpRequestUtil.sendJsonBody(httpPool,entity, url, this.headers);
				if(this.showTemplate ){
					if(logger.isInfoEnabled()) {
						logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
					}

				}
				ESStringResponseHandler responseHandler = new ESStringResponseHandler();
				response = restSeachExecutor.execute(url,entity,responseHandler);
				e = getException(  responseHandler );
				break;
			}
			catch (HttpHostConnectException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

            } catch (UnknownHostException ex) {
            	host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
            	if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

            }
			catch (NoRouteToHostException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (NoHttpResponseException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (ConnectionPoolTimeoutException ex){//连接池获取connection超时，直接抛出

				e = handleConnectionPoolTimeOutException( ex);
				break;
			}
            catch (ConnectTimeoutException connectTimeoutException){
				host.setStatus(1);
				e = handleConnectionTimeOutException(connectTimeoutException);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
			}
//			catch (IOException ex) {
//				host.setStatus(1);
//				if (triesCount < serversList.size()) {//失败尝试下一个地址
//					triesCount++;
//					e = ex;
//					continue;
//				} else {
//					e = ex;
//					break;
//				}
//
//            }
			catch (SocketTimeoutException ex) {
				e = handleSocketTimeoutException( ex);
				break;
			}
			catch (NoServerElasticSearchException ex){
				e = ex;

				break;
			}
			catch (ClientProtocolException ex){
				host.setStatus(1);
				e = ex;
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
				//throw new ElasticSearchException(new StringBuilder().append("Request[").append(url).append("] handle failed: must use http/https protocol port such as 9200,do not use transport such as 9300.").toString(),ex);
			}
			catch (ElasticSearchException ex) {
				e = ex;
				break;
			}
			catch (Exception ex) {
				e = ex;
				break;
			}
			catch (Throwable ex) {
				e = ex;
				break;
			}



		}
		if (e != null){
			if(e instanceof ElasticSearchException)
				throw (ElasticSearchException)e;
			throw new ElasticSearchException(e);
		}
		return response;
		*/

	}

	@Override
	public ClientUtil getClientUtil(IndexNameBuilder indexNameBuilder) {
		// TODO Auto-generated method stub
		return new RestClientUtil(this, indexNameBuilder);
	}

	@Override
	public ClientUtil getConfigClientUtil(IndexNameBuilder indexNameBuilder,String configFile) {
		// TODO Auto-generated method stub
		return new ConfigRestClientUtil(this, indexNameBuilder,configFile);
	}
	public ClientUtil getConfigClientUtil(IndexNameBuilder indexNameBuilder, BaseTemplateContainerImpl templateContainer){
		// TODO Auto-generated method stub
		return new ConfigRestClientUtil( templateContainer,this, indexNameBuilder);
	}
	public String executeHttp(String path,String action) throws ElasticSearchException{
		return executeHttp(path, null,  action) ;
	}

	public <T> T executeHttp(String path,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		return executeHttp(path, null,  action, responseHandler) ;
	}

	public <T> T discover(String path,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		return discover(path, null,  action, responseHandler) ;
	}

	private String getPath(String host,String path){
		String url = path.equals("") || path.startsWith("/")?
				new StringBuilder().append(host).append(path).toString()
				:new StringBuilder().append(host).append("/").append(path).toString();
		return url;
	}
	/**
	 * 
	 * @param path
	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeHttp(String path, String entity,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return _executeHttp(path, entity,action,responseHandler,false);
	}

	/**
	 *
	 * @param path
	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	private <T> T _executeHttp(String path, final String entity,final String action,final ResponseHandler<T> responseHandler,final boolean discoverHost) throws ElasticSearchException {
		return _executeHttp(path,   responseHandler, new ExecuteRequest() {
			@Override
			public Object execute(ESAddress host,String url,int triesCount) throws Exception {
				Object response = null;

				if(!discoverHost) {
					if(showTemplate && !discoverHost ){
						if(logger.isInfoEnabled()) {
							if(entity != null)
								logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
							else
								logger.info("ElasticSearch http request endpoint:{},retry:{}",url,triesCount);
						}


					}
					response = restSeachExecutor.executeHttp(url, entity, action, responseHandler);
				}
				else {
					response = restSeachExecutor.discoverHost(url, entity, action, responseHandler);
				}
				return response;
			}
		});
//		int triesCount = 0;
//		T response = null;
//		Throwable e = null;
//
//		ESAddress host = null;
//		String url = null;
//		while (true) {
//			try {
//				host = serversList.get(!healthCheckEnabled());
//				url = getPath(host.getAddress(),path);
//
//				if(!discoverHost) {
//					if(this.showTemplate && !discoverHost ){
//						if(logger.isInfoEnabled()) {
//							if(entity != null)
//								logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
//							else
//								logger.info("ElasticSearch http request endpoint:{},retry:{}",url,triesCount);
//						}
//
//
//					}
//					response = this.restSeachExecutor.executeHttp(url, entity, action, responseHandler);
//				}
//				else {
//					response = this.restSeachExecutor.discoverHost(url, entity, action, responseHandler);
//				}
//
//				e = getException(  responseHandler );
//				break;
//			} catch (HttpHostConnectException ex) {
//				host.setStatus(1);
//				e = new NoServerElasticSearchException(ex);
//				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
//					triesCount++;
//					continue;
//				} else {
//					break;
//				}
//
//			} catch (UnknownHostException ex) {
//				host.setStatus(1);
//				e = new NoServerElasticSearchException(ex);
//				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
//					triesCount++;
//					continue;
//				} else {
//					break;
//				}
//
//			}
//			catch (NoRouteToHostException ex) {
//				host.setStatus(1);
//				e = new NoServerElasticSearchException(ex);
//				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
//					triesCount++;
//					continue;
//				} else {
//					break;
//				}
//
//			}
//			catch (NoHttpResponseException ex) {
//				host.setStatus(1);
//				e = new NoServerElasticSearchException(ex);
//				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
//					triesCount++;
//					continue;
//				} else {
//					break;
//				}
//
//			}
//			catch (ConnectionPoolTimeoutException ex){//连接池获取connection超时，直接抛出
//
//				e = handleConnectionPoolTimeOutException( ex);
//				break;
//			}
//			catch (ConnectTimeoutException connectTimeoutException){
//				host.setStatus(1);
//				e = handleConnectionTimeOutException(connectTimeoutException);
//				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
//					triesCount++;
//					continue;
//				} else {
//					break;
//				}
//			}
////			catch (IOException ex) {
////				host.setStatus(1);
////				if (triesCount < serversList.size()) {//失败尝试下一个地址
////					triesCount++;
////					e = ex;
////					continue;
////				} else {
////					e = ex;
////					break;
////				}
////
////            }
//			catch (SocketTimeoutException ex) {
//				e = handleSocketTimeoutException( ex);
//				break;
//			}
//			catch (NoServerElasticSearchException ex){
//				e = ex;
//				break;
//			}
//			catch (ClientProtocolException ex){
//				host.setStatus(1);
//				e = ex;
//				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
//					triesCount++;
//					continue;
//				} else {
//					break;
//				}
//				//throw new ElasticSearchException(new StringBuilder().append("Request[").append(url).append("] handle failed: must use http/https protocol port such as 9200,do not use transport such as 9300.").toString(),ex);
//			}
//			catch (ElasticSearchException ex) {
//				e = ex;
//				break;
//			}
//			catch (Exception ex) {
//				e = ex;
//				break;
//			}
//			catch (Throwable ex) {
//				e = ex;
//				break;
//			}
//		}
//		if (e != null){
//			if(e instanceof ElasticSearchException)
//				throw (ElasticSearchException)e;
//			throw new ElasticSearchException(e);
//		}
//		return response;
	}

	/**
	 *
	 * @param path

	 * @return
	 * @throws ElasticSearchException
	 */
	private <T> T _executeHttp(String path, ResponseHandler<T> responseHandler,ExecuteRequest executeRequest) throws ElasticSearchException {
		int triesCount = 0;
		T response = null;
		Throwable e = null;

		ESAddress host = null;
		String url = null;
		while (true) {
			try {
				host = serversList.get(failAllContinue || !healthCheckEnabled());
				url = getPath(host.getAddress(),path);
				if(responseHandler != null && responseHandler instanceof  BaseExceptionResponseHandler){
					((BaseExceptionResponseHandler)responseHandler).clean();
				}
				response = (T)executeRequest.execute(host,url,triesCount);
				//如果是故障节点，则设置为正常节点
				host.recover();
				e = getException(  responseHandler );
				break;
			} catch (HttpHostConnectException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(url,ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			} catch (UnknownHostException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(url,ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (NoRouteToHostException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(url,ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (NoHttpResponseException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(url,ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (ConnectionPoolTimeoutException ex){//连接池获取connection超时，直接抛出
				if(host.failedCheck()){//如果是故障节点，则设置为正常节点
					host.onlySetStatus(0);
				}
				e = handleConnectionPoolTimeOutException(url, ex);
				break;
			}
			catch (ConnectTimeoutException connectTimeoutException){
				host.setStatus(1);
				e = handleConnectionTimeOutException(url,connectTimeoutException);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
			}
//			catch (IOException ex) {
//				host.setStatus(1);
//				if (triesCount < serversList.size()) {//失败尝试下一个地址
//					triesCount++;
//					e = ex;
//					continue;
//				} else {
//					e = ex;
//					break;
//				}
//
//            }
			catch (SocketTimeoutException ex) {
				if(host.failedCheck()){//如果是故障节点，则设置为正常节点
					host.onlySetStatus(0);
				}
				e = handleSocketTimeoutException(url, ex);
				break;
			}
			catch (NoServerElasticSearchException ex){
				e = ex;
				break;
			}
			catch (ClientProtocolException ex){
				host.setStatus(1);
				e = ex;
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
				//throw new ElasticSearchException(new StringBuilder().append("Request[").append(url).append("] handle failed: must use http/https protocol port such as 9200,do not use transport such as 9300.").toString(),ex);
			}
			catch (ElasticSearchException ex) {
				if(host.failedCheck()){//如果是故障节点，则设置为正常节点
					host.onlySetStatus(0);
				}
				e = ex;
				break;
			}
			catch (Exception ex) {
				if(host.failedCheck()){//如果是故障节点，则设置为正常节点
					host.onlySetStatus(0);
				}
				e = ex;
				break;
			}
			catch (Throwable ex) {
				if(host.failedCheck()){//如果是故障节点，则设置为正常节点
					host.onlySetStatus(0);
				}
				e = ex;
				break;
			}
		}
		if (e != null){
			if(e instanceof ElasticSearchException)
				throw (ElasticSearchException)e;
			throw new ElasticSearchException(e);
		}
		return response;
	}


	/**
	 *
	 * @param path
	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T discover(String path, String entity,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return _executeHttp(path, entity,action,responseHandler,true);
	}

	/**
	 *
	 * @param path
	 * @param entity
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public String executeHttp(String path, String entity,String action) throws ElasticSearchException {
		return executeHttp( path,  entity, action,new ESStringResponseHandler());
	}

	public String executeRequest(String path, final String entity) throws ElasticSearchException {
		final ESStringResponseHandler responseHandler = new ESStringResponseHandler();
		return _executeHttp(path,   responseHandler, new ExecuteRequest() {
			@Override
			public Object execute(ESAddress host,String url,int triesCount) throws Exception {
				Object response = null;

				if(showTemplate ){
					if(logger.isInfoEnabled()) {
						if(entity != null)
							logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
						else
							logger.info("ElasticSearch http request endpoint:{},retry:{}",url,triesCount);
					}
				}

				response = restSeachExecutor.executeSimpleRequest(url,entity,responseHandler);
				return response;
			}
		});
		/**
		int triesCount = 0;
		String response = null;
		Throwable e = null;

		ESAddress host = null;
		String url = null;
		while (true) {

			try {
				host = serversList.get(!healthCheckEnabled());
				url =  getPath(host.getAddress(),path);
//				if (entity == null)
//					response = HttpRequestUtil.httpPostforString(url, null, this.headers);
//				else
//					response = HttpRequestUtil.sendJsonBody(entity, url, this.headers);
				if(this.showTemplate ){
					if(logger.isInfoEnabled()) {
						if(entity != null)
							logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
						else
							logger.info("ElasticSearch http request endpoint:{},retry:{}",url,triesCount);
					}
				}
				ESStringResponseHandler responseHandler = new ESStringResponseHandler();
				response = this.restSeachExecutor.executeSimpleRequest(url,entity,responseHandler);
				e = getException(  responseHandler );
				break;
			} 
			
			catch (HttpHostConnectException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
                
            } catch (UnknownHostException ex) {
            	host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
            	if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
                 
            }
			catch (NoRouteToHostException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}

			catch (NoHttpResponseException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (ConnectionPoolTimeoutException ex){//连接池获取connection超时，直接抛出

				e = handleConnectionPoolTimeOutException( ex);
				break;
			}
			catch (ConnectTimeoutException connectTimeoutException){
				host.setStatus(1);
				e = handleConnectionTimeOutException(connectTimeoutException);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
			}
//			catch (IOException ex) {
//				host.setStatus(1);
//				if (triesCount < serversList.size()) {//失败尝试下一个地址
//					triesCount++;
//					e = ex;
//					continue;
//				} else {
//					e = ex;
//					break;
//				}
//
//            }
			catch (SocketTimeoutException ex) {
				e = handleSocketTimeoutException( ex);
				break;
			}
			catch (NoServerElasticSearchException ex){
					e = ex;
				break;
			}
			catch (ClientProtocolException ex){
				host.setStatus(1);
				e = ex;
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
//				throw new ElasticSearchException(new StringBuilder().append("Request[").append(url).append("] handle failed: must use http/https protocol port such as 9200,do not use transport such as 9300.").toString(),ex);
			}
			catch (ElasticSearchException ex) {
				throw ex;
			}
		
			catch (Exception ex) {
				e = ex;
				break;
			}
			catch (Throwable ex) {
				e = ex;
				break;
			}
		}
		if (e != null){
			throw new ElasticSearchException(e);
		}
		return response;*/
	}
	public <T> T executeRequest(String path, String entity,ResponseHandler<T> responseHandler) throws ElasticSearchException{
		return executeRequest(path, entity,responseHandler,ClientUtil.HTTP_POST);
	}
	private Exception getException(ResponseHandler responseHandler ){
		if(responseHandler instanceof BaseExceptionResponseHandler){
			return ((BaseExceptionResponseHandler)responseHandler).getElasticSearchException();
		}
		return null;
	}
	/**
	 * 需要补充容错机制
	 * @param path
	 * @param entity
	 * @param responseHandler
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T executeRequest(String path, final String entity,final ResponseHandler<T> responseHandler,final String action) throws ElasticSearchException {
		return _executeHttp(path,  responseHandler, new ExecuteRequest() {
			@Override
			public Object execute(ESAddress host,String url,int triesCount) throws Exception {
				Object response = null;

				if(showTemplate  ){
					if(logger.isInfoEnabled()) {
						if(entity != null)
							logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
						else
							logger.info("ElasticSearch http request endpoint:{},retry:{}",url,triesCount);
					}


				}
				response = restSeachExecutor.executeRequest(url,entity,action,responseHandler);
				return response;
			}
		});
		/**
		T response = null;
		int triesCount = 0;
		Throwable e = null;

		ESAddress host = null;
		String url = null;
		while (true) {
			try {
				host = serversList.get(!healthCheckEnabled());
				url =  getPath(host.getAddress(),path);
				if(this.showTemplate  ){
					if(logger.isInfoEnabled()) {
						if(entity != null)
							logger.info("ElasticSearch http request endpoint:{},retry:{},request body:\n{}",url,triesCount,entity);
						else
							logger.info("ElasticSearch http request endpoint:{},retry:{}",url,triesCount);
					}


				}
				response = this.restSeachExecutor.executeRequest(url,entity,action,responseHandler);
				e = getException(  responseHandler );
				break;
			} catch (HttpHostConnectException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;

					continue;
				} else {

					break;
				}
                
            } catch (UnknownHostException ex) {
            	host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
            	if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;

					continue;
				} else {

					break;
				}
                 
            }
			catch (NoRouteToHostException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (NoHttpResponseException ex) {
				host.setStatus(1);
				e = new NoServerElasticSearchException(ex);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}

			}
			catch (ConnectionPoolTimeoutException ex){//连接池获取connection超时，直接抛出

				e = handleConnectionPoolTimeOutException( ex);
				break;
			}
			catch (ConnectTimeoutException connectTimeoutException){
				host.setStatus(1);
				e = handleConnectionTimeOutException(connectTimeoutException);
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
			}
//			catch (IOException ex) {
//				host.setStatus(1);
//				if (triesCount < serversList.size()) {//失败尝试下一个地址
//					triesCount++;
//					e = ex;
//					continue;
//				} else {
//					e = ex;
//					break;
//				}
//
//            }
			catch (SocketTimeoutException ex) {
				e = handleSocketTimeoutException( ex);
				break;
			}
			catch (NoServerElasticSearchException ex){
//				if(e == null){
//					e = ex;
//				}
//				else {
//					e = new ElasticSearchException(ex.getMessage(),e);
//				}
				e = ex;
				break;
			}
			catch (ClientProtocolException ex){
				host.setStatus(1);
				e = ex;
				if (triesCount < serversList.size() - 1) {//失败尝试下一个地址
					triesCount++;
					continue;
				} else {
					break;
				}
				//throw new ElasticSearchException(new StringBuilder().append("Request[").append(url).append("] handle failed: must use http/https protocol port such as 9200,do not use transport such as 9300.").toString(),ex);
			}
			catch (ElasticSearchException ex) {
				throw ex;
			}

			catch (Exception ex) {
				e = ex;
				break;
			}
			catch (Throwable ex) {
				e = ex;
				break;
			}
//			throw new ElasticSearchException(e);

		}
		if (e != null){
			if(e instanceof ElasticSearchException)
				throw (ElasticSearchException)e;
			throw new ElasticSearchException(e);
		}
		return response;*/
	}

	public String getDistribution() {
		return distribution;
	}

	private interface ExecuteRequest{
		Object execute(ESAddress host, String url, int triesCount) throws Exception;
	}


	public FastDateFormat getFastDateFormat() {
		return fastDateFormat;
	}
	 

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public boolean isShowTemplate() {
		return showTemplate;
	}

	public void setShowTemplate(boolean showTemplate) {
		this.showTemplate = showTemplate;
	}

	public void recoverRemovedNodes(List<ESAddress> hosts) {
		if(hosts == null || hosts.size() == 0){
			return;
		}
		for(ESAddress httpHost: hosts) {
			ESAddress address = this.addressMap.get(httpHost.toString());
			if(address != null  ){
				if(address.getStatus() == 2){//节点还原
					address.onlySetStatus(0);
					if(logger.isInfoEnabled()){
						logger.info(new StringBuilder().append("Recover Removed Node [").append(address.toString()).append("] to clusters addresses list.").toString());
					}
				}
			}
		}
	}

	public Map getClusterInfo() {
		return clusterInfo;
	}
	public String getClusterVarcharInfo(){
		return this.clusterVarcharInfo;
	}
	public String getClusterVersionInfo(){
		return this.clusterVersionInfo;
	}
	public ExecutorService getSliceScrollQueryExecutorService(){
		return this.elasticSearch.getSliceScrollQueryExecutorService();
	}

	public ExecutorService getScrollQueryExecutorService(){
		return this.elasticSearch.getScrollQueryExecutorService();
	}


	public boolean isV1() {
		return v1;
	}



	public String getEsVersion() {
		return esVersion;
	}

	public void setEsVersion(String esVersion) {
		this.esVersion = esVersion;
	}

	public String getDiscoverPool() {
		return discoverPool;
	}
}
