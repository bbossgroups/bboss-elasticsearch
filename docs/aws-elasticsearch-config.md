# 基于HttpClient自定义机制实现AWS Elasticsearch请求签名

本文介绍基于Elasticsearch client bboss提供的自定义HttpClient机制，实现客户端与亚马逊aws elasticsearch之间的数据加密签名功能。

# 1.集成bboss和aws客户端

bboss的集成可以根据自己项目环境分别参考文档：

[普通项目集成](common-project-with-bboss.md) 

[spring boot项目集成](spring-booter-with-bboss.md)

导入aws maven坐标

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-core</artifactId>
    <version>1.11.327</version>
</dependency>
```

# 2.aws签名实现

首先基于bboss提供的自定义httpclientbuilder功能实现一个HttpClientBuilderCallback类：

```java
import org.apache.http.impl.client.HttpClientBuilder;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.frameworkset.spi.remote.http.callback.HttpClientBuilderCallback;


public class HttpClientBuilderCallbackDemo implements HttpClientBuilderCallback {

   public HttpClientBuilder customizeHttpClient(HttpClientBuilder builder, ClientConfiguration clientConfiguration) {
       
 
   String AWS_ACCESS_KEY_ID = "XXX";//替换成自己的
   String AWS_SECRET_ACCESS_KEY = "XXXXX";//替换成自己的

       AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);//亚马逊凭证信息
       AWS4Signer signer = new AWS4Signer();
       AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(credentials);
       signer.setServiceName("es");//亚马逊签名信息
       signer.setRegionName("us-east-1");
	//定义request拦截器，嵌入aws签名认证机制	
       HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
       "es", signer, awsCredentialsProvider);
       builder.addInterceptorLast(interceptor);
       return builder;
   }
}
```

AWSRequestSigningApacheInterceptor（aws官方提供）的实现如下：

```java
package org.bboss.elasticsearchtest.aws;

/*
 * Copyright 2012-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.Signer;
import com.amazonaws.http.HttpMethodName;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.http.protocol.HttpCoreContext.HTTP_TARGET_HOST;

/**
 * An {@link HttpRequestInterceptor} that signs requests using any AWS {@link Signer}
 * and {@link AWSCredentialsProvider}.
 */
public class AWSRequestSigningApacheInterceptor implements HttpRequestInterceptor {
    /**
     * The service that we're connecting to. Technically not necessary.
     * Could be used by a future Signer, though.
     */
    private final String service;

    /**
     * The particular signer implementation.
     */
    private final Signer signer;

    /**
     * The source of AWS credentials for signing.
     */
    private final AWSCredentialsProvider awsCredentialsProvider;

    /**
     * @param service                service that we're connecting to
     * @param signer                 particular signer implementation
     * @param awsCredentialsProvider source of AWS credentials for signing
     */
    public AWSRequestSigningApacheInterceptor(final String service,
                                              final Signer signer,
                                              final AWSCredentialsProvider awsCredentialsProvider) {
        this.service = service;
        this.signer = signer;
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(request.getRequestLine().getUri());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI", e);
        }

        // Copy Apache HttpRequest to AWS DefaultRequest
        DefaultRequest<?> signableRequest = new DefaultRequest<>(service);

        HttpHost host = (HttpHost) context.getAttribute(HTTP_TARGET_HOST);
        if (host != null) {
            signableRequest.setEndpoint(URI.create(host.toURI()));
        }
        final HttpMethodName httpMethod =
                HttpMethodName.fromValue(request.getRequestLine().getMethod());
        signableRequest.setHttpMethod(httpMethod);
        try {
            signableRequest.setResourcePath(uriBuilder.build().getRawPath());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI", e);
        }

        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest =
                    (HttpEntityEnclosingRequest) request;
            if (httpEntityEnclosingRequest.getEntity() != null) {
                signableRequest.setContent(httpEntityEnclosingRequest.getEntity().getContent());
            }
        }
        signableRequest.setParameters(nvpToMapParams(uriBuilder.getQueryParams()));
        signableRequest.setHeaders(headerArrayToMap(request.getAllHeaders()));

        // Sign it
        signer.sign(signableRequest, awsCredentialsProvider.getCredentials());

        // Now copy everything back
        request.setHeaders(mapToHeaderArray(signableRequest.getHeaders()));
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest =
                    (HttpEntityEnclosingRequest) request;
            if (httpEntityEnclosingRequest.getEntity() != null) {
                BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
                basicHttpEntity.setContent(signableRequest.getContent());
                httpEntityEnclosingRequest.setEntity(basicHttpEntity);
            }
        }
    }

    /**
     * @param params list of HTTP query params as NameValuePairs
     * @return a multimap of HTTP query params
     */
    private static Map<String, List<String>> nvpToMapParams(final List<NameValuePair> params) {
        Map<String, List<String>> parameterMap = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        for (NameValuePair nvp : params) {
            List<String> argsList =
                    parameterMap.computeIfAbsent(nvp.getName(), k -> new ArrayList<>());
            argsList.add(nvp.getValue());
        }
        return parameterMap;
    }

    /**
     * @param headers modeled Header objects
     * @return a Map of header entries
     */
    private static Map<String, String> headerArrayToMap(final Header[] headers) {
        Map<String, String> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Header header : headers) {
            if (!skipHeader(header)) {
                headersMap.put(header.getName(), header.getValue());
            }
        }
        return headersMap;
    }

    /**
     * @param header header line to check
     * @return true if the given header should be excluded when signing
     */
    private static boolean skipHeader(final Header header) {
        return ("content-length".equalsIgnoreCase(header.getName())
                && "0".equals(header.getValue())) // Strip Content-Length: 0
                || "host".equalsIgnoreCase(header.getName()); // Host comes from endpoint
    }

    /**
     * @param mapHeaders Map of header entries
     * @return modeled Header objects
     */
    private static Header[] mapToHeaderArray(final Map<String, String> mapHeaders) {
        Header[] headers = new Header[mapHeaders.size()];
        int i = 0;
        for (Map.Entry<String, String> headerEntry : mapHeaders.entrySet()) {
            headers[i++] = new BasicHeader(headerEntry.getKey(), headerEntry.getValue());
        }
        return headers;
    }
}
```

# 3.配置HttpClientBuilderCallback

在bboss配置文件application.properties中设置HttpClientBuilderCallback

spring boot配置：

```properties
spring.elasticsearch.bboss.http.httpClientBuilderCallback=com.example.esbboss.HttpClientBuilderCallbackDemo
```

普通项目配置：

```properties
http.httpClientBuilderCallback=org.bboss.elasticsearchtest.aws.HttpClientBuilderCallbackDemo
```

配置好后即可基于bboss api操作和访问aws elasticsearch了。

#  **4.开发交流** 



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">