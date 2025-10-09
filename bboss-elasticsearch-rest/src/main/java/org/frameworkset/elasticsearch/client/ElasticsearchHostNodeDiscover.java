package org.frameworkset.elasticsearch.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.spi.assemble.GetProperties;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.frameworkset.spi.remote.http.HttpHost;
import org.frameworkset.spi.remote.http.proxy.HttpHostDiscover;
import org.frameworkset.spi.remote.http.proxy.HttpServiceHostsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.frameworkset.spi.remote.http.ResponseUtil.entityEmpty;

/**
 * 主动发现：自动发现es 主机节点
 */
public class ElasticsearchHostNodeDiscover extends HttpHostDiscover {
	private final JsonFactory jsonFactory;
	private static Logger logger = LoggerFactory.getLogger(ElasticsearchHostNodeDiscover.class);
	private long discoverInterval  = 10000l;
 	public ElasticsearchHostNodeDiscover( ){
		super();
		this.jsonFactory = new JsonFactory();
 
	}
 

    @Override
    protected List<HttpHost> discover(HttpServiceHostsConfig httpServiceHostsConfig, ClientConfiguration configuration, GetProperties context) {
        
        try {
            List<HttpHost> hosts = new ArrayList<>();
            ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(configuration.getBeanName());
            clientInterface.executeHttp("_nodes/http",ClientInterface.HTTP_GET, new HttpClientResponseHandler<Void>() {

                @Override
                public Void handleResponse(ClassicHttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getCode();
                    if (org.frameworkset.spi.remote.http.ResponseUtil.isHttpStatusOK( status)) {
                        List<HttpHost> hostsNew = readHosts(response.getEntity(),configuration);
                        if(hostsNew != null && hostsNew.size() > 0 )
                            hosts.addAll(hostsNew);

                    } 
                    return null;
                }
            });
            return hosts;

        } catch (Exception e) {
            if (logger.isInfoEnabled())
                logger.info(new StringBuilder().append("Discovery elasticsearch[").append(configuration.getBeanName()).append("] node failed:").toString(),e);
        }
        return null;
    }
 

	private List<HttpHost> readHosts(HttpEntity entity, ClientConfiguration configuration) throws IOException {

		InputStream inputStream = null;


		Throwable var3 = null;

		try {
			inputStream = entity.getContent();

			if(entityEmpty(entity,inputStream)){
				throw new IOException(new StringBuilder().append("Read Hosts from http entity for elasticsearch[")
                        .append(configuration.getBeanName()).append("] failed: entity contentLength = 0 " ).toString());
			}
			JsonParser parser = this.jsonFactory.createParser(inputStream);
			if (parser.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException(new StringBuilder().append("expected data to start with an object for elasticsearch[")
                        .append(configuration.getBeanName()).append("]").toString());
			} else {
				ArrayList hosts = new ArrayList();

				while(true) {
					while(true) {
						do {
							if (parser.nextToken() == JsonToken.END_OBJECT) {
								ArrayList var18 = hosts;
								return var18;
							}
						} while(parser.getCurrentToken() != JsonToken.START_OBJECT);

						if ("nodes".equals(parser.getCurrentName())) {
							while(parser.nextToken() != JsonToken.END_OBJECT) {
								JsonToken token = parser.nextToken();

								assert token == JsonToken.START_OBJECT;

								String nodeId = parser.getCurrentName();
								HttpHost sniffedHost = readHost(nodeId, parser, configuration);
								if (sniffedHost != null) {
									if(logger.isTraceEnabled())
										logger.trace(new StringBuilder().append("Adding node [" ).append( nodeId ).append( "] for elasticsearch[")
                                                .append(configuration.getBeanName()).append("]").toString());
									hosts.add(sniffedHost);
								}
							}
						} else {
							parser.skipChildren();
						}
					}
				}
			}
		} catch (IOException var16) {
			var3 = var16;
			throw var16;
		} finally {
			if (inputStream != null) {
				if (var3 != null) {
					try {
						inputStream.close();
					} catch (Throwable var15) {

					}
				} else {
					inputStream.close();
				}
			}

		}
	}

	private  HttpHost readHost(String nodeId, JsonParser parser,  ClientConfiguration configuration) throws IOException {
		HttpHost httpHost = null;
		String fieldName = null;

		while(true) {
			label41:
			while(parser.nextToken() != JsonToken.END_OBJECT) {
				if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
					fieldName = parser.getCurrentName();
				} else if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
					if (!"http".equals(fieldName)) {
						parser.skipChildren();
					} else {
						while(true) {
							while(true) {
								if (parser.nextToken() == JsonToken.END_OBJECT) {
									continue label41;
								}

								if (parser.getCurrentToken() == JsonToken.VALUE_STRING && "publish_address".equals(parser.getCurrentName())) {
//									URI boundAddressAsURI = URI.create(scheme + "://" + publishAddressHandle(parser.getValueAsString()));
									httpHost = new HttpHost(publishAddressHandle(parser.getValueAsString()));
								} else if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
									parser.skipChildren();
								}
							}
						}
					}
				}
			}

			if (httpHost == null) {
				if(logger.isDebugEnabled())
				logger.debug(new StringBuilder().append("skipping node [" )
						.append( nodeId ).append( "] with http disabled  for elasticsearch[").append(configuration.getBeanName()).append("]").toString());
				return null;
			}

			return httpHost;
		}
	}
	public static String publishAddressHandle(String publishAddress){
		int i = publishAddress.indexOf("/");
		if(i >= 0){
			return publishAddress.substring(i + 1);
		}
		else
			return publishAddress;
	}

 
}
