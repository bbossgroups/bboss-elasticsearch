//package org.frameworkset.elasticsearch.client;
//
//import com.fasterxml.jackson.core.JsonFactory;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonToken;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpHost;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.ResponseHandler;
//import org.frameworkset.elasticsearch.ElasticSearch;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.frameworkset.spi.remote.http.ResponseUtil.entityEmpty;
//
///**
// * 主动发现：自动发现es 主机节点
// */
//public class HostDiscover extends Thread{
//	private final JsonFactory jsonFactory;
//	private static Logger logger = LoggerFactory.getLogger(HostDiscover.class);
//	private Scheme scheme = HostDiscover.Scheme.HTTP;
//	private long discoverInterval  = 10000l;
//	private ClientInterface clientInterface ;
//	private ElasticSearch elasticSearch;
//	private ElasticSearchRestClient elasticSearchRestClient;
//	public HostDiscover(String elasticsearchName,ElasticSearchRestClient elasticSearchRestClient ){
//		super("ElasticSearch["+elasticsearchName+"] HostDiscover Thread");
//		this.jsonFactory = new JsonFactory();
//		this.elasticSearchRestClient = elasticSearchRestClient;
//		this.elasticSearch = elasticSearchRestClient.getElasticSearch();
//		this.clientInterface = elasticSearch.getRestClientUtil();
//		this.scheme =  !elasticSearchRestClient.isUseHttps()? Scheme.HTTP:Scheme.HTTPS;
//
//		this.setDaemon(true);
//	}
//	boolean stop = false;
//	public synchronized void stopCheck(){
//
//		if(stop )
//			return;
//		this.stop = true;
//		this.interrupt();
//        try {
//            this.join();
//        } catch (InterruptedException e) {
//        }
//    }
//
//	private void handleDiscoverHosts(List<HttpHost> httpHosts){
//		List<ESAddress> hosts = new ArrayList<ESAddress>();
//		for(HttpHost host:httpHosts){
//			ESAddress esAddress = new ESAddress(host.toString(),elasticSearch.getHealthPath());
//			hosts.add(esAddress);
//		}
//		List<ESAddress> newAddress = new ArrayList<ESAddress>();
//		//恢复移除节点
//		elasticSearchRestClient.recoverRemovedNodes(hosts);
//		//识别新增节点
//		for(int i = 0; i < hosts.size();i ++){
//			ESAddress address = new ESAddress(hosts.get(i).toString(),elasticSearch.getHealthPath());
//			if(!elasticSearchRestClient.containAddress(address)){
//				newAddress.add(address);
//			}
//		}
//		//处理新增节点
//		if(newAddress.size() > 0) {
//			if (logger.isInfoEnabled()) {
//				logger.info(new StringBuilder().append("Discovery new elasticsearch[").append(elasticSearch.getElasticSearchName()).append("] node [").append(newAddress).append("].").toString());
//			}
//			elasticSearchRestClient.addAddresses(newAddress);
//		}
//		//处理删除节点
//		elasticSearchRestClient.handleRemoved( hosts);
//	}
//	@Override
//	public void run() {
//		do {
//			if(this.stop)
//				break;
//			try {
//				clientInterface.discover("_nodes/http",ClientInterface.HTTP_GET, new ResponseHandler<Void>() {
//
//					@Override
//					public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
//						int status = response.getStatusLine().getStatusCode();
//						if (org.frameworkset.spi.remote.http.ResponseUtil.isHttpStatusOK( status)) {
//							List<HttpHost> hosts = readHosts(response.getEntity());
//							handleDiscoverHosts(hosts);
//
//						} else {
//
//						}
//						return null;
//					}
//				});
//
//			} catch (Exception e) {
//				if (logger.isInfoEnabled())
//					logger.info(new StringBuilder().append("Discovery elasticsearch[").append(elasticSearch.getElasticSearchName()).append("] node failed:").toString(),e);
//			}
//			try {
//				sleep(discoverInterval);
//			} catch (InterruptedException e) {
//				break;
//			}
//		}while(true);
//
//	}
//
//	private List<HttpHost> readHosts(HttpEntity entity) throws IOException {
//
//		InputStream inputStream = null;
//
//
//		Throwable var3 = null;
//
//		try {
//			inputStream = entity.getContent();
//
//			if(entityEmpty(entity,inputStream)){
//				throw new IOException(new StringBuilder().append("Read Hosts from http entity for elasticsearch[").append(elasticSearch.getElasticSearchName()).append("] failed: entity contentLength = 0 " ).toString());
//			}
//			JsonParser parser = this.jsonFactory.createParser(inputStream);
//			if (parser.nextToken() != JsonToken.START_OBJECT) {
//				throw new IOException(new StringBuilder().append("expected data to start with an object for elasticsearch[").append(elasticSearch.getElasticSearchName()).append("]").toString());
//			} else {
//				ArrayList hosts = new ArrayList();
//
//				while(true) {
//					while(true) {
//						do {
//							if (parser.nextToken() == JsonToken.END_OBJECT) {
//								ArrayList var18 = hosts;
//								return var18;
//							}
//						} while(parser.getCurrentToken() != JsonToken.START_OBJECT);
//
//						if ("nodes".equals(parser.getCurrentName())) {
//							while(parser.nextToken() != JsonToken.END_OBJECT) {
//								JsonToken token = parser.nextToken();
//
//								assert token == JsonToken.START_OBJECT;
//
//								String nodeId = parser.getCurrentName();
//								HttpHost sniffedHost = readHost(nodeId, parser,this.scheme);
//								if (sniffedHost != null) {
//									if(logger.isTraceEnabled())
//										logger.trace(new StringBuilder().append("Adding node [" ).append( nodeId ).append( "] for elasticsearch[").append(elasticSearch.getElasticSearchName()).append("]").toString());
//									hosts.add(sniffedHost);
//								}
//							}
//						} else {
//							parser.skipChildren();
//						}
//					}
//				}
//			}
//		} catch (IOException var16) {
//			var3 = var16;
//			throw var16;
//		} finally {
//			if (inputStream != null) {
//				if (var3 != null) {
//					try {
//						inputStream.close();
//					} catch (Throwable var15) {
//
//					}
//				} else {
//					inputStream.close();
//				}
//			}
//
//		}
//	}
//
//	private  HttpHost readHost(String nodeId, JsonParser parser, Scheme scheme) throws IOException {
//		HttpHost httpHost = null;
//		String fieldName = null;
//
//		while(true) {
//			label41:
//			while(parser.nextToken() != JsonToken.END_OBJECT) {
//				if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
//					fieldName = parser.getCurrentName();
//				} else if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
//					if (!"http".equals(fieldName)) {
//						parser.skipChildren();
//					} else {
//						while(true) {
//							while(true) {
//								if (parser.nextToken() == JsonToken.END_OBJECT) {
//									continue label41;
//								}
//
//								if (parser.getCurrentToken() == JsonToken.VALUE_STRING && "publish_address".equals(parser.getCurrentName())) {
//									URI boundAddressAsURI = URI.create(scheme + "://" + publishAddressHandle(parser.getValueAsString()));
//									httpHost = new HttpHost(boundAddressAsURI.getHost(), boundAddressAsURI.getPort(), boundAddressAsURI.getScheme());
//								} else if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
//									parser.skipChildren();
//								}
//							}
//						}
//					}
//				}
//			}
//
//			if (httpHost == null) {
//				if(logger.isDebugEnabled())
//				logger.debug(new StringBuilder().append("skipping node [" )
//						.append( nodeId ).append( "] with http disabled  for elasticsearch[").append(elasticSearch.getElasticSearchName()).append("]").toString());
//				return null;
//			}
//
//			return httpHost;
//		}
//	}
//	public static String publishAddressHandle(String publishAddress){
//		int i = publishAddress.indexOf("/");
//		if(i >= 0){
//			return publishAddress.substring(i + 1);
//		}
//		else
//			return publishAddress;
//	}
//
//
//
//	public static enum Scheme {
//		HTTP("http"),
//		HTTPS("https");
//
//		private final String name;
//
//		private Scheme(String name) {
//			this.name = name;
//		}
//
//		public String toString() {
//			return this.name;
//		}
//	}
//}
