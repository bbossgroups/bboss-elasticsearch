# 集成aws-elasticsearch

使用Elasticsearch client bboss可以非常方便地操作亚马逊(aws)开源的elasticsearch，下面介绍具体的对接方法

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

# 2.aws签名认证机制实现

首先基于bboss提供的自定义httpclientbuilder功能实现一个HttpClientBuilderCallback类：

```java
import org.apache.http.impl.client.HttpClientBuilder;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.frameworkset.spi.remote.http.callback.HttpClientBuilderCallback;


public class HttpClientBuilderCallbackDemo implements HttpClientBuilderCallback {

   public HttpClientBuilder customizeHttpClient(HttpClientBuilder builder, ClientConfiguration clientConfiguration) {
       
       AWSCredentials credentials = new BasicAWSCredentials("asdfdf", "xsafasdf");//亚马逊凭证信息
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