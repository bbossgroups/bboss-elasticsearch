# 高性能elasticsearch ORM开发库使用介绍

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)



​    **The best elasticsearch highlevel java rest api-----bboss**       

​    bboss是一款高性能elasticsearch ORM开发库， 以类似于mybatis管理和配置sql的方式,采用xml文件管理elasticsearch的dsl脚本，在dsl脚本中可以使用变量、dsl片段、foreach循环、逻辑判断、注释；支持在线修改、自动热加载dsl配置文件，开发和调试非常方便。bboss对原生elasticsearch restful api、elasticsearch java orm api、elasticsearch sql都提供了很好的支持。

bboss elasticsearch管理dsl的方式：

- 类似于mybatis，以xml管理dsl
- 可扩展机制：基于数据库、redis等方式管理dsl

​    **bboss elasticsearch默认以类似于mybatis的方式,使用xml文件管理elasticsearch的dsl脚本，在dsl脚本中可以使用变量、脚本片段、foreach循环、逻辑判断、注释**；也可基于扩展机制，实现数据库、redis等方式管理dsl语句；配置管理的dsl语句支持在线修改、自动热加载，开发和调试非常方便。



**如果喜欢直接使用query dsl（es的官方语言），但是又不想在代码里面编写冗长的dsl拼接串的话，可以考虑采用 bboss。**

​    **bboss elasticsearch jdk兼容性： jdk 1.8+**

  **bboss es restful组件不依赖elasticsearch官方任何jar文件，兼容所有elasticsearch版本:1.x,2.x,5.x,6.x,7.x,8.x,+**   ,兼容spring boot 1.x,2,x

   bboss与es官方客户端的对比：[bboss es对比直接使用es客户端的优势](bboss-vs-es.md)

  bboss elasticsearch开发环境搭建和开发入门视频教程：[下载](https://pan.baidu.com/s/1kXjAOKn)

首先介绍如何在项目中导入和配置elasticsearch开发包，spring boot的导入和配置请参考文档：

# 从源码构建Elasticsearch BBoss

First Get source code from https://github.com/bbossgroups/bboss-elasticsearch

Then change to cmd window under directory bboss-elasticsearch and run gradle build command：

```
gradle publishToMavenLocal
```

Gradle environmenet install and config document: https://esdoc.bbossgroups.com/#/bboss-build



# **1** 快速集成和应用 

- 所有类型项目：[common-project-with-bboss](common-project-with-bboss.md) 
- spring boot 项目：[spring-booter-with-bboss](spring-booter-with-bboss.md)
- aws elasticsearch集成： [aws-elasticsearch-config](aws-elasticsearch-config.md) 



# **2 elasticsearch配置**

运行bboss es需要一个application.properties文件，放到资源目录（resources）目录下即可。

本文主要以非spring boot项目配置为例，spring boot项目请参考：

https://esdoc.bbossgroups.com/#/spring-booter-with-bboss

配置文件[**application.properties**](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/application.properties)主要配置说明如下：

## **2.1 es服务器账号和口令配置**

如果启用了x-pack或者searchguard安全认证，那么通过下面两个属性配置账号和口令：

elasticUser=elastic

elasticPassword=changeme

如果需要配置加密口令，那么可以通过配置属性拦截器来对加密口令进行识别处理，参考文档：

[使用外部属性加载拦截器](https://doc.bbossgroups.com/#/aop/IntroduceIoc?id=_6-使用外部属性加载拦截器)

## **2.2 Elasticsearch 服务器http地址和端口配置**



### **2.2.1 ES单节点配置**

elasticsearch.rest.hostNames=127.0.0.1:9200



### **2.2.2 ES集群节点配置**

Elasticsearch集群地址采用逗号分隔即可，如果开启了discovery机制可以只配置部分节点；

**如果启用了ES的client node模式则只能配置所有client node的地址即可，并且关闭discovery机制**

elasticsearch.rest.hostNames=127.0.0.1:9200,127.0.0.1:9201,127.0.0.1:9202



## 2.3 集群节点自动发现

bboss提供了非常方便的elasticsearch节点动态发现机制，能够动态发现elasticsearch服务端的节点的变化（新增节点，删除节点等），从而调整客户端的地址清单；支持主动和被动两种节点发现模式。

如果启用了节点自动发现机制，我们只需要在elasticsearch.rest.hostNames中配置几个初始节点即可：

```properties
elasticsearch.rest.hostNames=127.0.0.1:9200,127.0.0.1:9201,127.0.0.1:9202
```



### 2.3.1 主动发现模式

主动模式集群节点自动发现机制通过elasticsearch.discoverHost参数来控制，true开启，false关闭

elasticsearch.discoverHost=false

如果开启discover机制，客户端就会具备自动发现新增的ES集群节点的能力，elasticsearch.rest.hostNames中就可以只配置初始的几个节点，不需要配置全部节点。

**注意**：

访问容器环境/云环境部署的Elasticsearch时或者通过Elasticsearch proxy client协调节点访问时，请关闭：elasticsearch.discoverHost=false

这种情况，可以通过被动监听的方式通过bboss提供的api来动态修改客户端地址列表

### 2.3.2 被动发现模式

被动发现模式通过组件org.frameworkset.elasticsearch.client.HostDiscoverUtil提供的静态方法api来支持：

```java
/**
 * 默认Elasticsearch数据源
 * @param hosts 最新的地址清单
 *
 */
public static synchronized void handleDiscoverHosts(String[] hosts)
/**
 * @param hosts 最新的地址清单
 * @param elasticsearch elasticsearch数据源
 */
public static synchronized void handleDiscoverHosts(String[] hosts,String elasticsearch)
```

如果节点发生变化时，可以调用上面的方法之一来处理，例如：

```java
String hosts[] = {"127.0.0.1:9200","127.0.0.1:9201","127.0.0.1:9202","127.0.0.1:9203"};
HostDiscoverUtil.handleDiscoverHosts(hosts);//default es数据源
HostDiscoverUtil.handleDiscoverHosts(hosts,"loges");//指定loges数据源
```

注意：需要将elasticsearch.discoverHost设置为false

elasticsearch.discoverHost=false



### 2.3.3 基于apollo配置中心的节点发现机制

直接在apollo 对应的bboss elasticsearch配置启动文件中指定节点发现监听器：

src\main\resources\conf\elasticsearch-boot-config.xml

```xml
<properties>
    <!--
       指定apolloNamespace属性配置namespace
       指定configChangeListener属性，设置elasticsearch节点自动发现监听器
    -->

    <config apolloNamespace="application"
            configChangeListener="org.frameworkset.apollo.ESNodeChangeListener"/>
 </properties>
```

注意：需要将elasticsearch.discoverHost设置为false

elasticsearch.discoverHost=false

参考apollo管理配置文档：

https://esdoc.bbossgroups.com/#/apollo-config

### 2.3.4 故障节点健康检查配置

通过下面的参数开启故障节点健康检查机制以及设置故障节点健康检查探测时间间隔（单位毫秒），为-1时关闭检查机制：

```
elasticsearch.healthCheckInterval=5000
```
### 2.3.5 failAllContinue配置
如果所有节点都被标记为不可用时，可以通过控制开关设置返回故障节点用于处理请求，如果请求能够被正常处理则将节点标记为正常节点
默认值true
非spring boot项目配置
```properties
        elasticsearch.failAllContinue = true
```
spring boot配置项
```properties
        spring.elasticsearch.bboss.elasticsearch.failAllContinue = true
```


## 2.4 DSL调试日志开关和日志组件配置

### 2.4.1 DSL脚本调试日志开关

DSL脚本调试日志开关，将showTemplate设置为true，同时将日志级别设置为INFO，则会将query dsl脚本输出到日志文件中：

```properties
    elasticsearch.showTemplate=true
```
spring boot配置项
```properties
        spring.elasticsearch.bboss.elasticsearch.showTemplate=true
```

bboss可以通过常用的日志组件输出真实的dsl语句，支持的日志组件有：log4j（默认）,log4j2,logback,javalog等。

log4j的配置实例：

```properties
log4j.rootLogger=INFO,CONSOLE
##bboss需要的category配置
log4j.category.com.frameworkset = INFO, COMMON_FILE
log4j.category.org.frameworkset = INFO, COMMON_FILE
log4j.category.org.apache = INFO, COMMON_FILE
log4j.category.bboss=INFO, COMMON_FILE

###################
# Console Appender
###################
log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.Threshold=INFO
log4j.appender.CONSOLE.Target=System.out
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.ConversionPattern=[%d{yyyy-MM-dd HH:mm:ss}][%p]%x[%c] %m%n



#####################
# Common Rolling File Appender
#####################
log4j.appender.COMMON_FILE=org.apache.log4j.RollingFileAppender
log4j.appender.COMMON_FILE.Threshold=INFO
log4j.appender.COMMON_FILE.File=common.log
log4j.appender.COMMON_FILE.Append=true
log4j.appender.COMMON_FILE.MaxFileSize=10240KB
log4j.appender.COMMON_FILE.MaxBackupIndex=10
log4j.appender.COMMON_FILE.layout=org.apache.log4j.PatternLayout
log4j.appender.COMMON_FILE.layout.ConversionPattern=[%d{yyyy-MM-dd HH:mm:ss}][%p]%x[%c] %m%n
```

如果是spring boot项目，那么在spring boot项目的配置文件找那个加入以下内容：

以application.properties为例：

```properties
logging.level.org.bboss=INFO
logging.level.bboss=INFO
logging.level.com.frameworkset=INFO
logging.level.org.frameworkset=INFO

logging.level.org.apache=INFO
```



注意：

在生产环境请关闭：elasticsearch.showTemplate=false

### 2.4.2 日志组件切换

如果需要使用其他日志组件，那么只需要在bboss的maven坐标中排除log4j的包，导入其他日志组件的依赖库即可，以log4j 2为示例进行说明：

#### 非spring boot项目

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-datatran-jdbc</artifactId>
            <version>7.0.0</version>
            <exclusions>
                <exclusion>
                    <artifactId>slf4j-log4j12</artifactId>
                    <groupId>org.slf4j</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <version>2.17.2</version>
        </dependency>
<!-- https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.17.2</version>
        </dependency>
```



#### spring boot项目

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>7.0.0</version>
            <exclusions>
                <exclusion>
                    <artifactId>slf4j-log4j12</artifactId>
                    <groupId>org.slf4j</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <version>2.17.2</version>
        </dependency>
<!-- https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.17.2</version>
        </dependency>
```

### 2.4.3 动态切换Dsl日志打印

org.frameworkset.elasticsearch.client.HostDiscoverUtil组件提供了以下方法来动态切换Dsl日志打印功能：

```java
/**
 * 动态切换是否打印dsl到控制台开关标识
 * 默认dedault es数据源
 * @param showdsl
 */
public static synchronized void swithShowdsl(boolean showdsl)

/**
 * 动态切换是否打印dsl到控制台开关标识
 * 指定 es数据源
 * @param showdsl
 * @param elasticsearch es数据源名称
 */
public static synchronized void swithShowdsl(boolean showdsl,String elasticsearch)
```

### 2.4.4 基于apollo配置中心动态切换Dsl日志打印

直接在apollo 对应的bboss elasticsearch配置启动文件中指定节点发现监听器：

src\main\resources\conf\elasticsearch-boot-config.xml

```xml
<properties>
    <!--
       指定apolloNamespace属性配置namespace
       指定configChangeListener属性，设置elasticsearch节点自动发现和动态切换Dsl日志打印开关监听器
    -->

    <config apolloNamespace="application"
            configChangeListener="org.frameworkset.apollo.ESNodeChangeListener"/>
 </properties>
```

参考apollo管理配置文档：

https://esdoc.bbossgroups.com/#/apollo-config

### 2.4.5 dsl输出组件logDslCallback使用方法

通过组件logDslCallback中回调接口方法可以自定义采集dsl的执行信息行为

```java
public void logDsl(LogDsl logDsl);
```
参数LogDsl封装了以下信息

 ```java
    /**
	 * 慢dsl输出阈值
	 */
	private  int slowDslThreshold;
	
	/**
	 * elasticsearch rest http服务请求地址
	 */
	private String url;
	/**
	 * http request method：post,get,put,delete
	 */
	private String action;
	/**
	 * request handle elapsed ms
	 */
	private long time;
	/**
	 * elasticsearch dsl
	 */
	private  String dsl;
	/**
	 * request handle begin time.
	 */
	private Date startTime;
	/**
	 * request handle end time.
	 */
	private Date endTime;

	/**
	 * 0 - dsl执行成功
	 * 1 - dsl执行异常
	 */
	private int resultCode;
 ```

通过实现接口org.frameworkset.elasticsearch.client.LogDslCallback，可以将dsl输出到自己需要的地方，LogDslCallback实现实例-将dsl执行信息输出到日志文件中

```java
package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.entity.LogDsl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerDslCallback implements LogDslCallback{
   private static final Logger logger = LoggerFactory.getLogger(LoggerDslCallback.class);

   public void logDsl(LogDsl logDsl){
      if(logger.isInfoEnabled()) {
      			logger.info("Request[{}] action[{}] took time:{} ms ], use DSL[{}],execute result:{}",
      					logDsl.getUrl(),logDsl.getAction(), logDsl.getTime(),   logDsl.getDsl(),logDsl.result());
      
      		}
   }
}
```

然后在配置文件中配置logDslCallback：
非spring boot项目

```properties
elasticsearch.logDslCallback=org.frameworkset.elasticsearch.client.LoggerDslCallback
```

springboot项目

```properties
spring.elasticsearch.bboss.elasticsearch.logDslCallback=org.frameworkset.elasticsearch.client.LoggerDslCallback
```



## **2.5 按日期动态产生的索引索引名称的日期格式**

elasticsearch.dateFormat=yyyy.MM.dd

固定index对应的添加文档api addDocument（单文档接口）/addDocuments(批量接口)

动态Index对应的添加文档api addDateDocument（单文档接口）/addDateDocuments(批量接口)

下面是一个对比案例：

```java
//向固定index demo添加或者修改文档,如果demoId已经存在做修改操作，否则做添加文档操作，返回处理结果
String response = clientUtil.addDocument("demo",//索引表
      "demo",//索引类型
      demo);

//向动态index demo-yyyy.MM.dd这种添加或者修改文档,如果demoId已经存在做修改操作，否则做添加文档操作，返回处理结果
//elasticsearch.dateFormat=yyyy.MM.dd 按照日期生成动态index名称，例如：
// 到月 elasticsearch.dateFormat=yyyy.MM demo-2018.03
// 到天 elasticsearch.dateFormat=yyyy.MM.dd demo-2018.03.14
// 到小时 elasticsearch.dateFormat=yyyy.MM.dd.HH demo-2018.03.14.11
// 到分钟 elasticsearch.dateFormat=yyyy.MM.dd.HH.mm demo-2018.03.14.11.18
String response = clientUtil.addDateDocument("demo",//索引表
      "demo",//索引类型
      demo);
```



## **2.6 http协议配置**

### 2.6.1 连接池数量配置

 \## 总共允许的最大连接数:节点数n x defaultMaxPerRoute

http.maxTotal = 600

\## 每个地址允许的最大连接数

http.defaultMaxPerRoute = 200

### 2.6.2 重试机制配置

\##连接失败重试次数，默认-1，小于等于0时不重试

http.retryTime = 3

##重试时间间隔，单位：毫秒，默认值-1,直接重试，>0时需要等待给定的时间后再重试

```
http.retryInterval=1000
```

##automaticRetriesDisabled开关，关闭重试机制

http.automaticRetriesDisabled=false

没有指定重试机制http.retryTime为0或-1的情况下，如果automaticRetriesDisabled为false，在通讯则失败时自动重试3次，否则不重试

##自定义重试判断逻辑 ，在http.retryTime>0的情况下起作用，判断哪些场景下需要进行重试：

```properties
#* 自定义重试控制接口，必须实现接口方法
#* public interface CustomHttpRequestRetryHandler  {
#*     public boolean retryRequest(IOException exception, int executionCount, HttpContext context,ClientConfiguration configuration);
#* }
#* 方法返回true，进行重试，false不重试
# http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.DefaultHttpRequestRetryHandler
```

可以设置为自己实现的重试控制组件：

http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.ConnectionResetHttpRequestRetryHandler

### 2.6.3 保活机制配置

- **推荐配置：**主动清理过期的连接，过期时间由timeToLive设置，如果已经设置了validateAfterInactivity，则可以关闭evictExpiredConnections，控制HttpClient实例使用后台线程主动地从连接池中驱逐过期连接，过期时间由timeToLive指定，timeToLive单位毫秒，默认值为true，false 禁用


http.evictExpiredConnections=true

http.timeToLive=3600000 单位毫秒

- 推荐配置：单位：毫秒，当获取连接时，判断连接是否已经空闲了validateAfterInactivity对应的时间，如果是则对连接进行有效性校验，无效链接直接清理掉，>0起作用，默认值 -1


http.validateAfterInactivity=2000

- 不推荐配置：每次获取connection时校验连接，true，校验，默认false


http.staleConnectionCheckEnabled=false 



### 2.6.4 超时时间配置

```properties
#建立连接超时时间，单位：毫秒
http.timeoutConnection = 10000
#socket通讯超时时间，如果在通讯过程中出现sockertimeout异常，可以适当调整timeoutSocket参数值，单位：毫秒
http.timeoutSocket = 50000
#申请连接超时时间，设置为0不超时，单位：毫秒
http.connectionRequestTimeout=10000
```

### **2.6.5 https协议配置**

如果开启了Elasticsearch https协议，则需要在elasticsearch地址中添加https://协议头并且设置elasticsearch.useHttps为true

```properties
elasticsearch.useHttps=true
elasticsearch.rest.hostNames=https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282
```

spring boot对应的配置：

```properties
spring.elasticsearch.bboss.elasticsearch.useHttps=true
spring.elasticsearch.bboss.elasticsearch.rest.hostNames=https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282
```

Elasticsearch 启用https协议后，如果不想在客户端使用ssl证书则不需要进行ssl证书配置，如果需要使用ssl证书，bboss支持以下六种方式配置ssl证书参数。

如果是spring boot项目，在对应的参数名称添加前缀即可：spring.elasticsearch.bboss.

#### 1 Using PEM certificates

| 参数名称                | 说明                                               |
| ----------------------- | -------------------------------------------------- |
| http.pemCert            | pem证书路径，String                                |
| http.pemtrustedCA       | trustedHTTPCertificates证书路径，String            |
| http.supportedProtocols | ssl协议版本，String，默认值：TLSv1.2,TLSv1.1,TLSv1 |
| http.pemkeyPassword     | 私钥pem证书口令，String                            |
| http.pemKey             | 私钥pem证书路径，String                            |

配置示例（search guard）：

```properties
# Using PEM certificates
http.pemCert = D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources/sgadmin.crtfull.pem
http.pemtrustedCA = D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources/chain-ca.pem
http.pemKey = D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources/sgadmin.key.pem
http.pemkeyPassword = 7240a0366eb6a764103e
```

#### 2 Using the keystore and truststore file（JKS）

| 参数名称                | 说明                                                     |
| ----------------------- | -------------------------------------------------------- |
| http.keystoreAlias      | 可选，String                                             |
| http.trustAlias         | 可选，String                                             |
| http.supportedProtocols | 可选，ssl协议版本，String，默认值：TLSv1.2,TLSv1.1,TLSv1 |
| http.truststore         | truststore证书文件路径，证书类型为JKS                    |
| http.trustPassword      | truststore证书口令，String                               |
| http.keystore           | keystore证书路径，证书类型为JKS                          |
| http.keyPassword        | keystore证书口令                                         |

配置示例（search guard）：

```properties
# Using the keystore- and truststore file
 http.keystore = D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources/sgadmin-keystore.jks
 http.keyPassword = 7240a0366eb6a764103e
 http.truststore = D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources/truststore.jks
 http.trustPassword = 6aa4bd79096852203a5b
```

#### 3 Using the keystore 

| 参数名称                | 说明                                                     |
| ----------------------- | -------------------------------------------------------- |
| http.supportedProtocols | ssl协议版本，可选，String，默认值：TLSv1.2,TLSv1.1,TLSv1 |
| http.keystore           | keystore证书路径，证书类型为JKS                          |
| http.keyPassword        | keystore证书口令                                         |

配置示例：


```properties
# Using the keystore file
 http.keystore = D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources/sgadmin-keystore.jks
 http.keyPassword = 7240a0366eb6a764103e
 
```

#### 4 Using the crt文件

| 参数名称     | 说明                |
| ------------ | ------------------- |
| http.pemCert | pem证书路径，String |

配置示例：


```properties
# Using the crt file
 http.pemCert = /path/to/ca.crt
 
```

#### 5 Using the truststore文件

| 参数名称           | 说明                                            |
| ------------------ | ----------------------------------------------- |
| http.truststore    | truststore证书文件路径，证书类型为pkcs12或者JKS |
| http.trustPassword | truststore证书口令，可选项                      |

配置示例：


```properties
# Using the crt file
 http.truststore = /path/to/truststore.p12
 # 可选项
 http.trustPassword = 6aa4bd79096852203a5b
 
```

#### 6 Using the keystore and truststore file（pkcs12）

| 参数名称           | 说明                                     |
| ------------------ | ---------------------------------------- |
| http.truststore    | truststore证书文件路径，证书类型为pkcs12 |
| http.trustPassword | truststore证书口令，可选项               |
| http.keystore      | keystore证书路径，证书类型为pkcs12       |
| http.keyPassword   | keystore证书口令                         |

配置示例：


```properties
# Using the truststore file
 http.truststore = /path/to/your/truststore.p12
 # 可选项
 http.trustPassword = 6aa4bd79096852203a5b
 
 # Using the crt file
 http.keystore = /path/to/your/keystore.p12
 # 可选项
 http.keyPassword = 6aa4bd79096852203a5b
 
```



## 2.7 DSL加载机制配置

### 2.7.1 热加载扫描时间间隔

\# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制

```properties
dslfile.refreshInterval = -1
```



### 2.7.2 指定dsl配置文件根目录

dsl配置文件默认在classpath路径下查找，可以通过参数dslfile.dslMappingDir指定dsl配置文件的存放目录：

```properties
dslfile.dslMappingDir=D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources
```

spring boot对应配置：

```properties
spring.elasticsearch.bboss.dslfile.dslMappingDir=D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources
```

### 2.7.3 基于数据库管理dsl

bboss除了通过xml配置文件来管理dsl，还可以通过数据库来管理dsl，详细介绍参考文档：

https://esdoc.bbossgroups.com/#/db-dsl

## 2.8 设置scroll查询线程池线程数和等待队列长度

### 设置slice scroll查询对应的线程数和等待队列数
elasticsearch.sliceScrollThreadCount=50

elasticsearch.sliceScrollThreadQueue=100

elasticsearch.sliceScrollBlockedWaitTimeout=0  #单位毫秒



### 设置scroll查询对应的线程数和等待队列数
elasticsearch.scrollThreadCount=50

elasticsearch.scrollThreadQueue=100

elasticsearch.scrollBlockedWaitTimeout=0   #单位毫秒

 [Scroll-SliceScroll-api](Scroll-SliceScroll-api.md) 

## 2.9 Elasticsearch 7.x 索引类型兼容性配置
Elasticsearch 7.0 索引类型兼容性配置，false禁用索引类型，es默认禁用，如果需要在Elasticsearch 7.x向下兼容es6和5的indextype，可以在bboss中配置elasticsearch.includeTypeName 属性，配置为true开启，false 不开启（默认false，不开启）

```properties
## 设置为true，兼容ES 6的indexType
## 设置为false（默认值），不能再index mapping和index Template中包含indexType
elasticsearch.includeTypeName = true
## spring boot中对应的配置项为
# spring.elasticsearch.bboss.elasticsearch.includeTypeName = true
```

## 2.10 采集慢Dsl数据

bboss能够非常方便地采集耗时慢的dsl操作， 可以将慢dsl打印到日志文件或者发送给接收慢dsl的回调接口函数 。与之相关的两个参数和含义说明如下：

```properties
# 定义慢dsl耗时阈值，执行时间长超过指定阈值的dsl为慢dsl，必须指定大于0的数值
# 如果要关闭慢dsl采集功能，注释掉elasticsearch.slowDslThreshold配置即可
elasticsearch.slowDslThreshold=10000
# 指定采集慢dsl的回调函数,对应的函数必须实现以下接口：
#   public interface LogDslCallback {
#          void logDsl(LogDsl slowDsl);
#      }  
#  方法参数SlowDsl包含了慢dsl的相关信息：
#    url 处理请求的rest服务地址
#    action  处理请求的http method，例如post,get,put,delete，值有可能为null
#    time   dsl执行耗时，单位：毫秒(ms)
#    slowDslThreshold 慢dsl阈值，执行时间超过这个时间的dsl才被判定位慢dsl
#    entity  对应的dsl内容，可能为null
#    startTime  请求处理开始时间
#    endTime   请求处理结束时间
#  如果不需要回调函数则注释elasticsearch.slowDslCallback即可，这样将会把慢dsl打印到日志文件中（日志 #  输出级别为WARN），打印到日志文件中的entity如果超过2048个字节，则会被截取掉超过部分的内容，并用.....
#  替代表示dsl被截取了，传递给回调函数的entity没有这个限制
elasticsearch.slowDslCallback=org.bboss.elasticsearchtest.crud.DefaultSlowDslCallback 
```

对应的spring boot配置为：

```properties
spring.elasticsearch.bboss.elasticsearch.slowDslThreshold = 10000
spring.elasticsearch.bboss.elasticsearch.slowDslCallback=org.bboss.elasticsearchtest.crud.DefaultSlowDslCallback
```

 如果没有指定回调处理接口，将直接打印警告信息到日志文件，WARN级别 ， 如果dsl过长，需要截取掉部分内容再打印到日志文件中:超过2048的dsl将被截取掉超过的内容，替换为.....，否则调用LogDslCallback接口方法logDsl传递慢dsl的相关信息 

组件示例：

```java
public class DefaultSlowDslCallback implements LogDslCallback{

   private static final Logger logger = LoggerFactory.getLogger(DefaultSlowDslCallback.class);
   @Override
   public void logDsl(LogDsl slowDsl){
      if(logger.isWarnEnabled()) {
         logger.warn("Slow request[{}] action[{}] took time:{} ms > slowDslThreshold[{} ms], use DSL[{}],execute result:{}",
               slowDsl.getUrl(),slowDsl.getAction(), slowDsl.getTime(), slowDsl.getSlowDslThreshold(),  RestSearchExecutorUtil.chunkEntity(slowDsl.getDsl()),slowDsl.result());

      }
   }

}
```



***完成导入和配置，接下来就可以开始使用bboss操作和访问elasticsearch了。***



# 3 操作和访问elasticsearch模式

bboss操作和访问elasticsearch提供两种模式，分别对应两个组件：

RestClientUtil：通用组件，提供所有不依赖dsl的功能，也可以直接接收dsl。

ConfigRestClientUtil：加载配置文件中的dsl来实现对es的操作

这两个组件在不同的环境实例化的方法有所区别

## 3.1 获取客户端组件实例

### **3.1.1 普通项目环境**

通过org.frameworkset.elasticsearch.ElasticSearchHelper中提供的静态工厂方法获取其单实例对象，这些单实例对象是多线程并发安全的：

```java
//创建加载配置文件的客户端工具，单实例多线程安全
//Get a ConfigRestClientUtil instance
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo.xml");
//Build a RestClientUtil instance, single instance multi-thread security
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil() ;
```

### **3.1.2 spring boot环境**

通过BBossESStarter中提供的工厂方法获取其单实例对象，这些单实例对象是多线程并发安全的：

```java
   @Autowired
	private BBossESStarter bbossESStarter;
//Get a ConfigRestClientUtil instance to load configuration files, single instance multithreaded security
	ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
		//Build a RestClientUtil instance, single instance multi-thread security
		ClientInterface clientUtil = bbossESStarter.getRestClient();	
```

下面基于普通项目的管理实例方式来讲解RestClientUtil和ConfigRestClientUtil的用法

## **3.1** 加载配置文件中的dsl来实现对es的操作模式

public static ClientInterface getConfigRestClientUtil(String configFile)

public static ClientInterface getConfigRestClientUtil(String elasticSearch,String configFile) //elasticsearch参数指定了bboss中多集群配

置中的逻辑es集群名称，关于多集群配置请参考文档：

[快速集成Elasticsearch Restful API案例分享](common-project-with-bboss.md)  中的章节【***2.2多集群配置***】

**通过这两个方法获取到的ClientInterface实例是多线程安全的、单实例对象**

**加载配置文件中的dsl操作实例参考本文章节：【4.1 配置es查询dsl】和【4.2 ormapping操作示例】**

### 创建加载配置文件中的dsl的ConfigRestClientUtil示例

```java
//加载配置文件，创建es客户端工具包，在默认es数据源操作
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesqlMapper.xml");

//加载配置文件，创建es客户端工具包，在指定的es数据源order操作
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("order","estrace/ESTracesqlMapper.xml");
```



## **3.2** 所有不依赖dsl的功能，或直接接收dsl模式

public static ClientInterface getRestClientUtil()

public static ClientInterface getRestClientUtil(String elasticSearch) //elasticsearch参数指定了bboss中多集群配置中的逻辑es集群名称，

关于多集群配置请参考文档：

[快速集成Elasticsearch Restful API案例分享](common-project-with-bboss.md)  中的章节【***2.2多集群配置***】

获取实例方法

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();

ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil("othercluster");
```

**通过这两个方法获取到的ClientInterface实例是多线程安全的、单实例对象**

### 创建RestClientUtil直接操作dsl使用示例

```java
	public void testDirectDslQuery(){
		String queryAll = "{\"query\": {\"match_all\": {}}}";
        //在默认的es数据源操作
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
        //在order es数据源操作
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil("order");
		ESDatas<Demo> esDatas =clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
				queryAll,//queryAll变量对应的dsl语句
				Demo.class);
		//获取结果对象列表
		List<Demo> demos = esDatas.getDatas();

		//获取总记录数
		long totalSize = esDatas.getTotalSize();
		System.out.println(totalSize);
	}
```



getConfigRestClientUtil方法获取的ClientInterface实例是getRestClientUtil方法获取到的ClientInterface实例的子类，所以同样具备后者的所有功能。加载配置文件api和不加载配置文件api都是一致的，区别就是加载配置文件api传递的是dsl在配置文件中dsl对应的名称，如果配置文件中的dsl带有参数变量，还需要传递参数（map方式、bean方式传入即可）。

[RestClientUtil和ConfigRestClientUtil区别说明](RestClientUtil-ConfigRestClientUtil.md)



# **4 基本功能**



## **4.1 配置es查询dsl**

在resources下创建配置文件[estrace/ESTracesqlMapper.xml](https://github.com/bbossgroups/elasticsearch-gradle-example/blob/master/src/main/resources/esmapper/estrace/ESTracesMapper.xml)，配置一个query dsl脚本，名称为queryServiceByCondition，我们将在后面的ClientInterface 组件中通过queryServiceByCondition引用这个脚本，脚本内容定义如下：

```xml
<properties>
   <property name="queryServiceByCondition">
        <![CDATA[
        {
            "sort": [  ##排序
                {
                    "startTime": {
                        "order": "desc"
                    }
                }
            ],
            #if($lastStartTime > 0)//search_after分页查询
            "search_after": [#[lastStartTime]],
            #end
            "size": 100, ##每次返回100条记录
            "query": {
                "bool":{
                    "filter": [
                        {"term": {  ##精确查找
                            "applicationName": #[application]
                        }}
                        #if($queryStatus.equals("success"))
                          ,
                          {"term": { ##精确查找

                               "err": 0
                          }}
                        #elseif($queryStatus.equals("error"))
                          ,
                          {"term": { ##精确查找

                               "err": 1
                          }}
                        #end
                        ,
                        {"range": { ##指定时间范围
                            "startTime": {
                                "gte": #[startTime],
                                "lt": #[endTime]
                            }
                        }}
                    ]
                    
                    #if($queryCondition && !$queryCondition.equals(""))
                     ,
                     "must" : {
                        "multi_match" : { ##分词检索，指定坐在多个field执行检索
                          "query" : #[queryCondition],

                          "fields" : [ "agentId", "applicationName" ,"endPoint","params","remoteAddr","rpc","exceptionInfo"]
                        }
                     }
                    #end
                }
            },
            "aggs": {
                "applicationsums": {
                      "terms": {
                        "field": "applicationName.keyword",##按应用名称进行统计计数
                        "size":10000
                      },
                      "aggs":{
                            "successsums" : {
                                "terms" : {
                                    "field" : "err" ##按err标识统计每个应用的成功数和失败数，0标识成功，1标识失败
                                }
                            },
                            "elapsed_ranges" : {
                                "range" : {
                                    "field" : "elapsed", ##按响应时间分段统计
                                    "keyed" : true,
                                    "ranges" : [
                                        { "key" : "1秒", "to" : 1000 },
                                        { "key" : "3秒", "from" : 1000, "to" : 3000 },
                                        { "key" : "5秒", "from" : 3000, "to" : 5000 },
                                        { "key" : "5秒以上", "from" : 5000 }
                                    ]
                                }
                            }
                      }
                }
            }
        }]]>
    </property>

</properties>
```



## **4.2 ormapping操作示例**

加载query dsl文件,并执行查询操作

```java
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
//加载配置文件，创建es客户端工具包
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesqlMapper.xml");

//构建查询条件对象
TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
traceExtraCriteria.setApplication("testweb88");
DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
traceExtraCriteria.setStartTime(dateFormat.parse("2017-09-02 00:00:00").getTime());
traceExtraCriteria.setEndTime(dateFormat.parse("2017-09-13 00:00:00").getTime());

// 检索条件
String queryCondition = (request.getParameter("queryCondition"));
// 设置检索条件
traceExtraCriteria.setQueryCondition(queryCondition);
// 查询状态：all 全部 success 处理成功 fail 处理失败
String queryStatus = request.getParameter("queryStatus");
traceExtraCriteria.setQueryStatus(queryStatus);
//设置分页数据起点，以时间为起点
String lastStartTimeStr = request.getParameter("lastStartTime");
if(lastStartTimeStr != null && !lastStartTimeStr.equals("")) {
    Long lastStartTime = Long.parseLong(lastStartTimeStr);
    traceExtraCriteria.setLastStartTime(lastStartTime);
}

//执行查询操作，查询可以是简单的检索查询，也可以结合聚合查询
ESDatas<Traces> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<Traces>结果集、符合条件的总记录数totalSize、以及聚合查询的结果
            = clientUtil.searchList"trace-*/_search",//查询操作，查询indices trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                Traces.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致
List<Traces> traceList = data.getDatas();//获取查询到的记过集
long totalSize = data.getTotalSize();//获取总记录数
List<Map<String, Object>> applicationsums= data.getAggregationBuckets("applicationsums");//同时可以做聚合查询，获取聚合查询结果集
for (int i = 0; i < applicationsums .size(); i++) {
			 
	Map<String, Object> map = applicationsums.get(i);
			
	//获取子聚合查询结果：成功数和失败数
	List<Map<String, Object>> appstatic = (List<Map<String, Object>>)ResultUtil.getAggBuckets(map, "successsums");
 
	//获取响应时间分段统计信息
	Map<String, Map<String, Object>> appPeriodstatic = (Map<String, Map<String, Object>>)ResultUtil.getAggBuckets(map, "elapsed_ranges");
}
```



## 4.3 文档批量创建或者修改

按日期分表

```java
 //一个完整的批量添加和修改索引文档的案例  
SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		List<Demo> demos = new ArrayList<>();
		Demo demo = new Demo();
		demo.setDemoId(2l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo2");
		demo.setContentbody("this is content body2");
		demos.add(demo);

		demo = new Demo();
		demo.setDemoId(3l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo3");
		demo.setContentbody("this is content body3");
		demos.add(demo);

		//批量创建文档
		String response = clientUtil.addDateDocuments("demo",//索引表
				"demo",//索引类型
				demos);

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		//批量更新文档
		demo.setContentbody("updated");
		response = clientUtil.updateDocuments("demo-"+date,"demo",demos);
		System.out.println("updateDateDocument-------------------------");

		System.out.println(response);
        //获取索引文档，json格式
		response = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"2");//文档id
		System.out.println("getDocument-------------------------");
		System.out.println(response);
        //获取索引文档，返回Demo对象类型
		demo = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"3",//文档id
				Demo.class);
```

不按日期分表

```java
 //一个完整的批量添加和修改索引文档的案例  
 
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		List<Demo> demos = new ArrayList<>();
		Demo demo = new Demo();
		demo.setDemoId(2l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo2");
		demo.setContentbody("this is content body2");
		demos.add(demo);

		demo = new Demo();
		demo.setDemoId(3l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo3");
		demo.setContentbody("this is content body3");
		demos.add(demo);

		//批量创建文档
		String response = clientUtil.addDocuments("demo",//索引表
				"demo",//索引类型
				demos);

		System.out.println("addDocuments-------------------------");
		System.out.println(response);

		//批量更新文档
		demo.setContentbody("updated");
		response = clientUtil.updateDocuments("demo","demo",demos);
		System.out.println("updateDateDocument-------------------------");

		System.out.println(response);
        //获取索引文档，json格式
		response = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"2");//文档id
		System.out.println("getDocument-------------------------");
		System.out.println(response);
        //获取索引文档，返回Demo对象类型
		demo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"3",//文档id
				Demo.class);
```

批量删除文档的示例，这里不举例说明，请访问github [demo](https://github.com/bbossgroups/elasticsearch-gradle-example)



## 4.4 添加/修改索引文档

```java
添加/修改文档

TAgentInfo agentInfo = (TAgentInfo) dataObj;
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();				
clientUtil.addDocument("agentinfo",//索引名称
                       "agentinfo",//索引类型
                        agentInfo);//索引数据对象
//执行查询操作
ESDatas<TAgentInfo> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<TAgentInfo>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList"trace-*/_search",//查询操作，查询indices trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                TAgentInfo.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致
TAgentInfo的结构如下：

public class TAgentInfo implements java.io.Serializable{
	private String hostname;
	@ESId    //ip属性作为文档唯一标识，根据ip值对应的索引文档存在与否来决定添加或者修改操作
	private String ip;
	private String ports;
	private String agentId;
	private String applicationName;
	private int serviceType;
	private int pid;
	private String agentVersion;
	private String vmVersion;
    //日期类型
	private Date startTimestampDate;
	private Date endTimestampDate;
	private long startTimestamp;
	private long endTimestamp;
	private int endStatus;
	private String serverMetaData;
	private String jvmInfo;

	
}
```

**注意事项：如果对象的属性不需要存入索引中，则在字段的定义加上@JsonIgnore注解，例如：**

```java
@JsonIgnore
private Integer sqlEndElapsed;
```



## 根据文档ID获取单个文档

```java
        //创建批量创建文档的客户端对象，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil(); 
        //获取索引文档，json格式
		String response = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"2");//文档id
		System.out.println("getDocument-------------------------");
		System.out.println(response);
        //获取索引文档，返回Demo对象类型
		Demo demo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"3",//文档id
				Demo.class);
        //获取索引文档，返回Map对象类型
		Map demo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"3",//文档id
				Map.class);
```



## 根据条件检索单个对象和对象列表

```java
        //创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);       
        Map<String,Object> params = new HashMap<String,Object>();
		//设置applicationName1和applicationName2两个变量的值
		params.put("applicationName1","blackca\"tdemo2");
		params.put("applicationName2","blackcat\"demo3");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//设置时间范围,时间参数接受long值
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00"));
		params.put("endTime",new Date());
		//执行查询，返回列表结果 ，demo为索引表，_search为检索操作action
		ESDatas<Demo> esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
				clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
						"searchWithCustomEscape",//esmapper/demo.xml中定义的dsl语句
						params,//变量参数
						Demo.class);//返回的文档封装对象类型
       //获取结果对象列表
		List<Demo> demos = esDatas.getDatas();
 
		//获取总记录数
		long totalSize = esDatas.getTotalSize();
		System.out.println(totalSize); 
       //执行查询，返回单个对象
		Demo single = clientUtil.searchObject("demo/_search",//demo为索引表，_search为检索操作action
				"searchWithCustomEscape",//esmapper/demo.xml中定义的dsl语句
				params,//变量参数
				Demo.class );
```



## 4.5 文档_id和parentid的设置规则

除了es能够自动生成文档_id属性，bboss提供了三种指定文档_id和parentid的方法：

1. 注解@[E](https://my.oschina.net/u/2523458)SId和@ESParentId
2. ClientInterface接口方法参数
3. ClientOptions（新增/修改）/UpdateOptions(修改控制)



### 4.5.1 注解@ESId和@ESParentId

添加索引文档时，es会自动设置文档_id属性，如果需要人工指定_id值，只需要在对象属性上设置注解**@ESId**即可，例如：

```java
@ESId //ip属性作为文档唯一标识，根据ip值对应的索引文档存在与否来决定添加或者修改操作
private String ip;
```

@ESId同样适用于文档批量创建和修改操作

另外一个注解@ESParentId用来表示父子关系,在[父子关系检索案例](elasticsearch5-parent-child.md)中有介绍。

ESId和ESParentId两个注解在添加/修改文档、批量添加/修改文档操中指定文档的_id和parent属性，如果不指定，es自动生成_id属性，parent必须手工指定。



### 4.5.2 ClientInterface接口方法参数

除了通过ESId和ESParentId这两个注解来指定文档id和parentid，ClientInterface接口中还提供了一组方法来提供docid和parentid两个参数来指定文档id和parentid。

- 单文档添加/修改-直接指定文档id和parentid的值

```java
	public abstract String addDocumentWithId(String indexName, String indexType, Object bean,Object docId) throws ElasticSearchException;

	 
	public abstract String addDocumentWithId(String indexName, String indexType, Object bean,Object docId,Object parentId) throws ElasticSearchException;

	 
	public abstract String addDocument(String indexName, String indexType, Object bean,Object docId,Object parentId,String refreshOption) throws ElasticSearchException;

	 
	public abstract String addDocument(String indexName, String indexType, Object bean,Object docId,String refreshOption) throws ElasticSearchException;

    public String addDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId) throws ElasticSearchException;

	public String addDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId,String refreshOption) throws ElasticSearchException;

	public String addDateDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId) throws ElasticSearchException;

	public String addDateDocumentWithParentId(String indexName, String indexType, Object bean,Object parentId,String refreshOption) throws ElasticSearchException;
```

- 批量文档添加和修改-指定文档id和parentId对应的对象字段名称

```java
	/**
	 * 指定对象集合的文档id字段
	 */
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Field
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdOptions(String indexName, String indexType, List<Object> beans,String docIdField,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的字段名称
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField) throws ElasticSearchException;
	public abstract String addDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdField(String indexName, String indexType,  List<Object> beans,String docIdField) throws ElasticSearchException;


	/**********************/
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的字段名称
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的字段名称
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField) throws ElasticSearchException;
	public abstract String addDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdParentField(String indexName, String indexType,  List<Object> beans,String docIdField,String parentIdField) throws ElasticSearchException;
```

批量bean类型导入文档，如需指定docid和parentid,必须通过[E](https://my.oschina.net/u/2523458)SId和ESParentId两个注解或者ClientOptions/UpdateOptions指定docid属性和parentid属性

批量map类型导入文档，如需指定docid，必须通过制定一个map里面的key或者ClientOptions/UpdateOptions指定key对应的value作为docid，必须设置docidKey参数：

```java
public String addDateDocuments(String indexName, String indexType, List<Map> beans, String docIdKey, String refreshOption) 

public String addDateDocumentsWithIdKey(String indexName, String indexType, List<Map> beans, String docIdKey) throws ElasticSearchException

public abstract String addDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException;
public abstract String addDocumentsWithIdKey(String indexName, String indexType,  List<Map> beans,String docIdKey) throws ElasticSearchException;
```



### 4.5.3 ClientOptions/UpdateOptions

ClientOptions:主要用于新增/修改操作，可以指定以下属性：

```java
	private String parentIdField;
	private String idField;
	private String esRetryOnConflictField;
	private String versionField;
	private String versionTypeField;
	private String rountField;
	private String refreshOption;
    /**
	 * 自动按照日期分表：日期通过参数指定elasticsearch.dateFormat=yyyy.MM.dd
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocuments(String indexName, String indexType, List<?> beans,ClientOptions ClientOptions) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocuments(String indexName, String indexType, List<?> beans,ClientOptions ClientOptions) throws ElasticSearchException;
	/**************************************创建或者修改文档结束**************************************************************/
```

ClientOptions使用示例：

```java
ClientOptions clientOption = new ClientOptions();
clientOption.setRefreshOption("refresh=true");//为了测试效果,启用强制刷新机制，实际线上环境去掉最后一个参数"refresh=true"，线上环境谨慎设置这个参数
clientOption.setIdField("demoId");//设置文档id对应的字段
//批量添加或者修改2万个文档，将两个对象添加到索引表demo中，批量添加2万条记录耗时1.8s，
String response = clientUtil.addDocuments("demo",//索引表
      "demo",//索引类型
      demos,clientOption);
```

UpdateOptions：主要用户修改,可以设置以下属性

```java
	private String refreshOption;
	private String detectNoopField;
	private String docasupsertField;
	private String parentIdField;
	private String docIdField;
```

 

```java
    /**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param params
	 * @param updateOptions 指定更新的相关参数

	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocument(String index,String indexType,Object params,UpdateOptions updateOptions) throws ElasticSearchException;
```



##  



## 4.6 删除/批量删除索引文档

```java
//删除索引文档
clientUtil.deleteDocument("demo",//索引表
      "demo",//索引类型
      "5");//文档id

//批量删除索引文档
clientUtil.deleteDocuments("demo",//索引表
      "demo",//索引类型
      "1","2","3");//文档ids
```



## 4.7 设置索引刷新机制

往elasticsearch（批量）添加/修改/删除索引时，并不会立即生效，本节介绍通过bboss api来指定刷新机制：

```java
public abstract String addDocument(String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException;
public abstract String addDocuments(String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException;

public abstract String updateDocument(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
public abstract String updateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
public abstract String addDateDocument(String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException;
public abstract String addDateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
```

bboss可以在相关的api上指定不同的refreshOption值来指定索引刷新策略：

```java
refresh=wait_for
refresh=false
refresh=true  //强制刷新
refresh  //强制刷新
refreshOption参数说明如下
   
   refresh： Empty string or true
   refresh=true：
	Refresh the relevant primary and replica shards (not the whole index) immediately after the 
operation occurs, so that the updated document appears in search results immediately. This should 
ONLY be done after careful thought and verification that it does not lead to poor performance, both 
from an indexing and a search standpoint.
	refresh=wait_for:
	Wait for the changes made by the request to be made visible by a refresh before replying. This 
doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch 
automatically refreshes shards that have changed every index.refresh_interval which defaults to one 
second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the 
APIs that support it will also cause a refresh, in turn causing already running requests with 
refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some 
point after the request returns.
```

refreshOption 使用实例：

```java
		//强制刷新
		String response = clientUtil.addDocument("demo",//索引表
				"demo",//索引类型
				demo,"refresh=true");
```

refreshOption参数可以用于指定其他的控制参数（4.8章节详细介绍），采用标准的url请求参数格式，例如：

```properties
refresh=true&version=1
```



## 4.8 指定控制参数

### 4.8.1 基于refreshOption参数指定添加/修改文档控制参数

bboss提供了带refreshOption参数一系列api，通过refreshOption参数，可以利用url参数的格式设定es操作的相关控制参数，例如：

refresh=true&e&version=1

下面列出了org.frameworkset.elasticsearch.client.ClientInterface接口提供的部分带refreshOption参数的api：

```java
public abstract String deleteDocument(String indexName, String indexType, String id,String refreshOption) throws ElasticSearchException;
	public abstract String addDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
public abstract String addDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException;

public abstract String addDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException;
public abstract String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException;

public abstract String addDateDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException;

public abstract String addDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField,String refreshOption) throws ElasticSearchException;

public abstract String addDocumentsWithIdField(String indexName, String indexType, List<Object> beans,String docIdField,String refreshOption) throws ElasticSearchException;

public abstract String updateDocuments(String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
	public abstract String updateDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException;
	public abstract String updateDocuments(String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException;
public abstract String addDocument(String indexName, String indexType, Object bean,Object docId,String refreshOption) throws ElasticSearchException;
public abstract String addDocument(String indexName, String indexType, Object bean,Object docId,Object parentId,String refreshOption) throws ElasticSearchException;

	public abstract String deleteDocumentsWithrefreshOption(String indexName, String indexType, String refreshOption,String[] ids) throws ElasticSearchException;

public String updateDocument(String index,String type,Object id,Object params,String refreshOption) throws ElasticSearchException;

	public String updateDocument(String index,String type,Object id,Map params,String refreshOption) throws ElasticSearchException;
```

[【4.7】](https://esdoc.bbossgroups.com/#/development?id=_47-%e8%ae%be%e7%bd%ae%e7%b4%a2%e5%bc%95%e5%88%b7%e6%96%b0%e6%9c%ba%e5%88%b6)小节介绍了控制定时刷新的refresh参数，refreshOption中还可以指定其他文档操作的控制参数：

| `retry_on_conflict`      | In between the get and indexing phases of the update, it is possible that another process might have already updated the same document. By default, the update will fail with a version conflict exception. The `retry_on_conflict` parameter controls how many times to retry the update before finally throwing an exception. |
| ------------------------ | ------------------------------------------------------------ |
| `routing`                | Routing is used to route the update request to the right shard and sets the routing for the upsert request if the document being updated doesn’t exist. Can’t be used to update the routing of an existing document. |
| `timeout`                | Timeout waiting for a shard to become available.             |
| `wait_for_active_shards` | The number of shard copies required to be active before proceeding with the update operation. See [here](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-index_.html#index-wait-for-active-shards) for details. |
| `refresh`                | Control when the changes made by this request are visible to search. See [*?refresh*](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-refresh.html). |
| `_source`                | Allows to control if and how the updated source should be returned in the response. By default the updated source is not returned. See [`source filtering`](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-source-filtering.html) for details. |
| `version`                | The update API uses the Elasticsearch’s versioning support internally to make sure the document doesn’t change during the update. You can use the `version` parameter to specify that the document should only be updated if its version matches the one specified. |

具体的用法如下：

指定修改的文档版本号

```java
		//强制刷新
		String response = clientUtil.addDocument("demo",//索引表
				"demo",//索引类型
				demo,"version=1");
```

指定文档版本号同时强制刷新：

```java
		//强制刷新
		String response = clientUtil.addDocument("demo",//索引表
				"demo",//索引类型
				demo,"refresh=true&version=1");
```

### 4.8.2 基于ClientOption/UpdateOption指定添加/修改文档控制参数

可以基于ClientOption/UpdateOption指定控制参数，bboss带ClientOption/UpdateOption参数的部分api如下：

```java
	public abstract String addDocument(Object bean,ClientOptions clientOptions) throws ElasticSearchException;
	public abstract String addDocument(String indexName, String indexType, Object bean,ClientOptions clientOptions) throws ElasticSearchException;
public abstract String addDateMapDocument(String indexName, String indexType, Map bean,ClientOptions clientOptions) throws ElasticSearchException;


	public abstract String addDocuments(List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;

	public String updateDocument(Object params,UpdateOptions updateOptions) throws ElasticSearchException;

	public abstract String updateDocuments( List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;

	public abstract String updateDocuments(String indexName, String indexType, List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;

	public String updateDocument(String index,String indexType,Object params,UpdateOptions updateOptions) throws ElasticSearchException;
```

使用案例

ClientOptions:主要用于新增/修改操作，可以指定以下属性：

```java
	private String parentIdField;
	private String idField;
	private String esRetryOnConflictField;
	private String versionField;
	private String versionTypeField;
	private String rountField;
	private String refreshOption;
```

其中的refreshOption值的指定用法可以参考章节4.8.1 基于refreshOption参数指定控制参数，参数格式如下：

refresh=true&e&version=1

其他参数根据对应的api来进行配置即可

ClientOptions示例代码

```java
//批量添加或者修改2万个文档，将两个对象添加到索引表demo中，批量添加2万条记录耗时1.8s，
		ClientOptions clientOptions = new ClientOptions();
		clientOptions.setRefreshOption("refresh=true&version=2");//为了测试效果,启用强制刷新机制，实际线上环境去掉最后一个参数"refresh=true"
		clientOptions.setIdField("demoId");//指定采用demoId座位文档id
		String response = clientUtil.addDocuments(
				demos,clientOptions);
```

UpdateOptions：主要用于更新操作的控制参数，包含以下属性

```java
private String refreshOption;
private String detectNoopField;
private String docasupsertField;
private String parentIdField;
private String docIdField;
```
UpdateOptions的使用方法和ClientOptions类似，就不举例介绍了.

### 4.8.3 指定检索控制参数

#### 关于search_type的介绍

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-search-type.html

Elasticsearch is very flexible and allows to control the type of search to execute on a **per search request** basis. The type can be configured by setting the **search_type** parameter in the query string. The types are:

##### Query Then Fetch

Parameter value: **query_then_fetch**.

The request is processed in two phases. In the first phase, the query is forwarded to **all involved shards**. Each shard executes the search and generates a sorted list of results, local to that shard. Each shard returns **just enough information** to the coordinating node to allow it merge and re-sort the shard level results into a globally sorted set of results, of maximum length `size`.

During the second phase, the coordinating node requests the document content (and highlighted snippets, if any) from **only the relevant shards**.

**Note：**This is the default setting, if you do not specify a `search_type` in your request.

##### Dfs, Query Then Fetch

Parameter value: **dfs_query_then_fetch**.

Same as "Query Then Fetch", except for an initial scatter phase which goes and computes the distributed term frequencies for more accurate scoring.

#### 检索的时候指定search_type

```java
 ClientInterface clientUtil = ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesqlMapper.xml");
        Map<String,Object> queryMap = new HashMap<>();
        // 指定商品类目作为过滤器
        queryMap.put("titleName","雨伞");
        // 指定需要field_value_factor运算的参数
        queryMap.put("valueFactorName","sales");
        // testFieldValueFactor 就是上文定义的dsl模板名，queryMap 为查询条件，Student为实体类
        //通过search_type参数指定search_type值即可，两个值：query_then_fetch和dfs_query_then_fetch
        ESDatas<Item> esDatast = clientUtil.searchList("items/_search?search_type=dfs_query_then_fetch", "testFieldValueFactor", queryMap, Item.class);
        List<Item> esCrmOrderStudentList = esDatast.getDatas();
        logger.debug(esCrmOrderStudentList.toString());
        System.out.println(esCrmOrderStudentList.toString());
```

检索的其他参数参考都可以参考search_type参数的使用方法来追加到请求action url路径后面。

## 4.9 通过URL参数来实现检索操作

可以通过Url参数来实现文档检索操作，参考文档：

[通过url参数检索文档](https://esdoc.bbossgroups.com/#/document-crud?id=通过url参数检索文档)



## 4.10 @JsonProperty注解使用

当elasticsearch索引表字段名称和java bean的字段名称不一致的情况下，采用@JsonProperty注解用来定义elasticsearch和java bean的field名称转换映射关系，使用实例如下：

```java
@JsonProperty("max_score")
private Double maxScore;
```

## 4.11 在es7+使用的api

es7+版本将去掉indexType，因此bboss提供了一组不带indexType的api，参考文档：

 [Elasticsearch-7-API](Elasticsearch-7-API.md) 

## 4.12 DSL中使用变量案例

在DSL配置文件中定义dsl时，可以使用变量来设置检索时需要参数和查询条件，在代码中通过Map或者Java PO对象传递实际的参数值，本节举例说明。

### 定义包含变量的dsl

在工程的resources目录下面准备一个xml配置文件demo.xml，其中定义名称为searchDatas的dsl

```xml
<properties>
<property name="searchDatas">
        <![CDATA[{
            "query": {
                "bool": {
                    "filter": [
                        {  ## Multi-terms search, find multiple application name corresponding to the document records
                            "terms": {
                                "applicationName.keyword": [#[applicationName1],#[applicationName2]]
                            }
                        },
                        {   ## Time range search, return the corresponding time range of records
                            "range": {
                                "agentStarttime": {
                                    "gte": #[startTime],##Statistical start time
                                    "lt": #[endTime]  ##Statistical end time
                                }
                            }
                        }
                    ]
                }
            },
            ## Up to 1000 records are returned
            "size":#[size]
        }]]>
    </property>
</properties>    
```

在上面的dsl中使用了applicationName1、applicationName2、startTime、endTime、size5个变量，还包含了## 开头的注释，这样就准备好了dsl配置文件了，相关语法可以参考章节：【[5.3 dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl%e9%85%8d%e7%bd%ae%e8%a7%84%e8%8c%83)】，接下来看看在代码里面如何加载配置文件，传递检索需要的变量参数值，实现检索操作

### spring boot 检索代码

spring boot项目通过bbossESStarter组件来创建加载配置文件demo.xml的ClientInterface实例

```java
//Create a load DSL file demo.xml client instance to retrieve documents use spring boot helper component bbossESStarter, single instance multithread security
		ClientInterface clientUtil = bbossESStarter.getConfigRestClient("demo.xml");
		//Set query conditions, pass variable parameter values via map,key for variable names in DSL
		//通过Map或者Java PO对象传递实际的参数值
		Map<String,Object> params = new HashMap<String,Object>();
		//Set the values of applicationName1 and applicationName2 variables
		params.put("applicationName1","app1");//设置变量applicationName1值
		params.put("applicationName2","app2");//设置变量applicationName2值
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Set the time range, and accept the Date or long value as the time parameter
		try {
            //设置变量startTime值
			params.put("startTime",dateFormat.parse("2017-09-02 00:00:00"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
        //设置变量endTime值
		params.put("endTime",new Date());
        //设置变量size值
        params.put("size",1000);


		//Execute the query
		ESDatas<Demo> esDatas =  //ESDatas contains a collection of currently retrieved records, up to 1000 records, specified by the size attribute in the DSL
				clientUtil.searchList("demo/_search",//demo as the indice, _search as the search action
				"searchDatas",//DSL statement name defined in demo.xml
				params,//Query parameters,传递上面定义的变量参数map
				Demo.class);//Data object type Demo returned


		//Gets a list of result objects and returns max up to 1000 records (specified in DSL)
		List<Demo> demos = esDatas.getDatas();

//		String json = clientUtil.executeRequest("demo/_search",//demo as the index table, _search as the search action
//				"searchDatas",//DSL statement name defined in esmapper/demo.xml
//				params);//Query parameters

//		String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);
		//Gets the total number of records
		long totalSize = esDatas.getTotalSize();
```

### 普通项目检索代码

普通项目通过ElasticSearchHelper组件来创建加载配置文件demo.xml的ClientInterface实例

```java
//Create a load DSL file demo.xml client instance to retrieve documents use ElasticSearchHelper component , single instance multithread security
		//创建加载配置文件的客户端工具，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("demo.xml");
		//Set query conditions, pass variable parameter values via map,key for variable names in DSL
		//通过Map或者Java PO对象传递实际的参数值
		Map<String,Object> params = new HashMap<String,Object>();
		//Set the values of applicationName1 and applicationName2 variables
		params.put("applicationName1","app1");//设置变量applicationName1值
		params.put("applicationName2","app2");//设置变量applicationName2值
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Set the time range, and accept the Date or long value as the time parameter
		try {
            //设置变量startTime值
			params.put("startTime",dateFormat.parse("2017-09-02 00:00:00"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
        //设置变量endTime值
		params.put("endTime",new Date());
        //设置变量size值
        params.put("size",1000);


		//Execute the query
		ESDatas<Demo> esDatas =  //ESDatas contains a collection of currently retrieved records, up to 1000 records, specified by the size attribute in the DSL
				clientUtil.searchList("demo/_search",//demo as the indice, _search as the search action
				"searchDatas",//DSL statement name defined in esmapper/demo.xml
				params,//Query parameters,传递上面定义的变量参数map
				Demo.class);//Data object type Demo returned


		//Gets a list of result objects and returns max up to 1000 records (specified in DSL)
		List<Demo> demos = esDatas.getDatas();

//		String json = clientUtil.executeRequest("demo/_search",//demo as the index table, _search as the search action
//				"searchDatas",//DSL statement name defined in esmapper/demo.xml
//				params);//Query parameters

//		String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);
		//Gets the total number of records
		long totalSize = esDatas.getTotalSize();
```

## 4.13  索引增删改查详解

https://esdoc.bbossgroups.com/#/document-crud



# 5 进阶



## 5.1 操作Elasticsearch通用服务API

Elasticsearch提供了非常多的rest api服务，参考文档：

 https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html 

bboss对里面的常用的服务进行了很好的封装，详见[ClientInterface](https://github.com/bbossgroups/bboss-elasticsearch/blob/master/bboss-elasticsearch-rest/src/main/java/org/frameworkset/elasticsearch/client/ClientInterface.java)接口。通过ClientInterface 接口提供的通用executeHttp api，可以非常方便地实现未封装的Elasticsearch rest 服务调用

### 5.1.1 通用服务API

通过ClientInterface 接口提供的通用executeHttp api，可以非常方便地实现未封装的Elasticsearch rest 服务调用：

```java
        /**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param templateName 请求报文
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public String executeHttp(String path, String templateName,Map params, String action) throws ElasticSearchException
	/**
	 * 发送es restful请求，返回String类型json报文
	 * @param path
	 * @param templateName 请求报文dsl名称，在配置文件中指定
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public String executeHttp(String path, String templateName,Object bean, String action) throws ElasticSearchException

	/**
	 * 
	 * @param path
	 * @param templateName
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	@Override
	public String executeHttp(String path, String templateName, String action) throws ElasticSearchException
	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param templateName
	 * @param action get,post,put,delete
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  executeHttp(String path, String templateName,String action,Map params,ResponseHandler<T> responseHandler) throws ElasticSearchException
	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param templateName
	 * @param action get,post,put,delete
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  executeHttp(String path, String templateName,String action,Object bean,ResponseHandler<T> responseHandler) throws ElasticSearchException
```

通过ClientInterface 提供的这个通用http api，我们可以非常方便地实现es中所有不带请求报文的功能

```java
    /**
	 * 没有报文的请求处理api
	 * @param path 请求url相对路径，可以带参数
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	@Override
	public String executeHttp(String path, String action) throws ElasticSearchException
```

### 5.1.2 通用服务API案例

通用api的使用案例：**path**参数为相对路径，不需要带ip和端口，在application.properties文件中统一配置

```java
	public void testTempate() throws ParseException{

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTemplate.xml");
		//创建模板
		String response = clientUtil.createTempate("demotemplate_1",//模板名称
				"demoTemplate");//模板对应的脚本名称，在esmapper/estrace/ESTemplate.xml中配置
		System.out.println("createTempate-------------------------");
		System.out.println(response);
		//通过通用api获取模板
		/**
		 * 指定模板
		 * /_template/demoTemplate_1
		 * /_template/demoTemplate*
		 * 所有模板 /_template
		 *
		 */
		String template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET-------------------------");
		System.out.println(template);
		//删除模板
		template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_DELETE);
		System.out.println("HTTP_DELETE-------------------------");
		System.out.println(template);

		template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET after delete-------------------------");
		System.out.println(template);
	}
```

### 5.1.3 通用服务API传递控制参数

我们可以在请求服务相对路径中添加各种控制参数，控制es的操作行为：

/_template/demotemplate_1?pretty&aa=bb

这里以格式化报文为例说明，在请求服务相对路径中添加pretty控制参数，控制响应报文输出格式

```java
//通过通用api获取模板
		/**
		 * 指定模板
		 * /_template/demoTemplate_1
		 * /_template/demoTemplate*
		 * 所有模板 /_template
		 *
		 */
		String template = clientUtil.executeHttp("/_template/demotemplate_1?pretty",ClientUtil.HTTP_GET);//在请求服务相对路径中添加pretty控制参数，控制响应报文输出格式
		System.out.println("HTTP_GET-------------------------");
		System.out.println(template);
```
### 5.3.4 通过通用服务api做检索操作

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTemplate.xml");
Map params = 。。。。
String datasr = clientUtil.executeHttp("demowithesindex-*/_search","querydsl",params,ClientInterface.HTTP_POST);
```

## **5.2 多elasticsearch服务器集群支持**

bboss对多集群elasticsearch的支持非常棒，可以在客户端接口ClientInterface 实例级别指定操作的elasticsearch数据源名称，亦可以在客户端接口ClientInterface的方法级别指定操作的elasticsearch数据源名称，下面分别介绍其使用方法

### 5.2.1 普通maven项目多ES集群数据源客户端组件定义方法

初始化bboss elasticsearch组件ClientInterface 时，可以指定elasticsearch服务器，支持在指定的elasticsearch服务器集群进行操作,例如：

```java

//没有dsl配置文件
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil("logs");//指定集群名称logs
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();//默认组件方法
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil("default");//默认组件方法

//有dsl配置文件
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("logs",configFile);//指定集群名称logs
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesqlMapper.xml");//默认组件方法
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("default","estrace/ESTracesqlMapper.xml");//默认组件方法
```

logs对应的es集群服务器相关参数配置，请参考文档：

[maven项目多集群配置](https://esdoc.bbossgroups.com/#/common-project-with-bboss?id=_22%e5%a4%9a%e9%9b%86%e7%be%a4%e9%85%8d%e7%bd%ae)



### 5.2.2 spring boot项目多ES集群数据源客户端组件初始化方法

默认default数据源

```java
    @Autowired
    private BBossESStarter bbossESStarter;
//Create a client tool to load configuration files, single instance multithreaded security
    ClientInterface clientUtil = bbossESStarter.getConfigRestClient("default",mappath);
        //Build a create/modify/get/delete document client object, single instance multi-thread security
        ClientInterface clientUtil = bbossESStarter.getRestClient("default");    
```

logs数据源

```java
    @Autowired
    private BBossESStarter bbossESStarter;
//Create a client tool to load configuration files, single instance multithreaded security
    ClientInterface clientUtil = bbossESStarter.getConfigRestClient("logs",mappath);
        //Build a create/modify/get/delete document client object, single instance multi-thread security
        ClientInterface clientUtil = bbossESStarter.getRestClient("logs");    
```


logs对应的es集群服务器相关参数配置，请参考文档：

[spring boot多集群配置](https://esdoc.bbossgroups.com/#/spring-booter-with-bboss?id=_4%e5%a4%9aes%e9%9b%86%e7%be%a4%e6%b5%8b%e8%af%95%e7%94%a8%e4%be%8b)



### 5.2.3 在接口方法上指定datasourceName

ClientInterface接口提供了一系列直接指定数据源的api，涉及的组件有RestClientUtil（支撑直接操作dsl或者无dsl的方法）和ConfigRestClientUtil

```java
org.frameworkset.elasticsearch.client.ClientInterfaceWithESDatasource
```

```java
addDocumentWithCluster(String datasourceName,...)
addDocumentsWithCluster(String datasourceName,...)
deleteDocumentWithCluster(String datasourceName,...)    
deleteDocumentsWithCluster(String datasourceName,...)
getDocumentWithCluster(String datasourceName,...)
getDocumentByFieldWithCluster(String datasourceName,...)
getDocumentByFieldLikeWithCluster(String datasourceName,...)
searchListWithCluster(String datasourceName,...)
searchObjectWithCluster(String datasourceName,...)
searchWithCluster(String datasourceName,...)
sqlObjectWithCluster(String datasourceName,...)
sqlWithCluster(String datasourceName,...)
scrollParallelWithCluster(String datasourceName,...)
scrollWithCluster(String datasourceName,...)
scrollSliceParallelWithCluster(String datasourceName,...)
scrollSliceWithCluster(String datasourceName,...)
```

示例代码-searchListWithCluster

```java
ESDatas<Demo> esDatas = 
            clientUtil.searchListWithCluster(datasourceName,//指定操作的Elasticsearch集群数据源
                  "demo/_search",//demo为索引表，_search为检索操作action
            "searchDatas",//esmapper/demo7.xml中定义的dsl语句
            params,//变量参数
            Demo.class);//返回的文档封装对象类型
      //获取结果对象列表，最多返回1000条记录
      List<Demo> demos = esDatas.getDatas();

//    String json = clientUtil.executeRequest("demo/_search",//demo为索引表，_search为检索操作action
//          "searchDatas",//esmapper/demo7.xml中定义的dsl语句
//          params);

//    String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);
      //获取总记录数
      long totalSize = esDatas.getTotalSize();
```

### 5.2.4 数据源之间的引用

在一些情况下，可能会用到数据源之间的引用关系，例如，本来是分库的default和detail两个elasticsearch数据源，因存储方案调整，detail也需放入default数据源对应elasticsearch，为了不调整应用程序，直接将detail数据源指向default数据源的引用即可，使用方法如下：

普通工程项目

```properties
##多集群配置样例，如果需要做多集群配置，请将参照本文内容修改application.properties文件内容
# default: 指标数据源
# detail: 明细数据源
elasticsearch.serverNames = default,detail
。。。。
# 拨测明细数据elasticsearch集群配置
# 指标数据库保持一致，直接引用即可
detail.elasticsearch.referExternal=default
```

spring boot项目
```properties
##多集群配置样例，如果需要做多集群配置，请将参照本文内容修改application.properties文件内容
# default: 指标数据源
# detail: 明细数据源
spring.elasticsearch.bboss.elasticsearch.serverNames = default,detail
。。。。
# 拨测明细数据elasticsearch集群配置
# 指标数据库保持一致，直接引用即可
spring.elasticsearch.bboss.detail.elasticsearch.referExternal=default
```


## 5.3 dsl配置规范



### **5.3.1 查询dsl动态脚本语法**

​      bboss elasticsearch采用xml文件管理elasticsearch的dsl脚本，在dsl脚本中可以使用变量、foreach循环、逻辑判断、注释；配置文件支持在线修改、自动热加载，开发和调试非常方便。

#### 5.3.1.1 **变量**

脚本中变量定义语法有两种:#[xxx],$xxx,**尽可能地在脚本中使用#[xxx]方式的变量，在#[]类型变量中还可以指定属性，后面举例说明。对于**#[xxx]类型**变量值中包含的可能破坏dsl json语法结构的特殊字符（例如回车换行符等），框架会自动进行转义处理；**$xxx类型变量直接输出原始值（特殊字符不做转移处理），$xxx类型变量可以用于if/else和foreach循环控制变量，而#[xxx]不可以**。**   

**正确用法**

**判断List集合datas不为空并且datas的size大于0**

*#if（$datas && $datas.size()> 0)*

*#foreach($bb in $datas)*

*#end*

*#end* 

**错误用法**

*#if（#[xxxxx] > 0)*

*#foreach($bb in #[datas])*

*#end*

*#end* 



#### **5.3.1.2 片段引用**

@{pianduan}

支持引用同文件内片段和跨文件片段引用，后面专门介绍。

#### **5.3.1.3 script脚本封装语法**

```javascript
@"""
  ctx._source.last = params.last;
  ctx._source.nick = params.nick
"""
```

#### **5.3.1.4 SQL语句回车换行符替换语法**

\#""" """,包含在这个中间的dsl片段中包含的回车换行符会被替换成空格，使用示例及注意事项:

```xml
<property name="sqlPagineQuery">
    <![CDATA[
     {
     ## 指示sql语句中的回车换行符会被替换掉开始符,注意dsl注释不能放到sql语句中，否则会有问题，因为sql中的回车换行符会被去掉，导致回车换行符后面的语句变道与注释一行
     ##  导致dsl模板解析的时候部分sql段会被去掉
        "query": #"""
                SELECT * FROM dbclobdemo
                    where channelId=#[channelId]
         """,
         ## 指示sql语句中的回车换行符会被替换掉结束符
        "fetch_size": #[fetchSize]
     }
    ]]>
</property>
```

#### **5.3.1.5 foreach循环语法**

\#foreach-#end

  foreach循环内置循环变量：$velocityCount，不需要从外部传入,使用示例：

```velocity
#foreach($xxx in $xxxs)
     #if($velocityCount > 0) ,#end
     {"term": {
     	"applicationName": #[xxxs[$velocityCount]]
     }}
#end
```

嵌套foreach循环中使用循环变量

```velocity
#foreach($subxxxs in $xxxs)
     ## 定义保存外部循环变量velocityCount的临时变量
     #set($outIndex = $velocityCount)
     #foreach($item in $subxxxs.innerList)
         #if($outIndex > 0) ,#end
         {"term": {
            "applicationName": #[xxxs[$outIndex]->innerList[$velocityCount]]
         }}
	#end
#end
```



#### **5.3.1.6 逻辑判断语法**

\#if-#else-#end,#if-#elseif-#else-#end

#### **5.3.1.7 变量值逻辑判断**

\#if($xxxx) ##变量值不为null判断（类似java语法 if(xxxx != null)） 

\#end

\#if(!$xxxx) ##变量值为null判断（类似java语法 if(xxxx == null)） 

\#end

\#if($xxxx && !$xxxx.equals("")) ##变量值不为null判断且不等于""判断（类似java语法 if(xxxx != null && !xxx.equals(""))） 

\#end

\#if($xxxx > 0) ##变量值大于某个值判断,其他类似（类似java语法 if(xxxx > 0)） 

\#end

判断List集合不为null并且size大于0

\#if($datas && $datas.size() > 0)

\#end

逻辑判断还可以包含各种组合 && ||操作。

#### **5.3.1.8 在dsl中定义和修改$模式变量**

定义变量

\#set($needComma = true)

修改$变量值

\#set($needComma = false)

### **5.3.2 在dsl中使用注释**

dsl注释是用多个#号来标识的，大段注释用 #* 和\*#包起来
单行注释：

```
##注释内容
```

多行注释：

```
#*
注释内容
*#
```

使用示例

```xml
<property name="searchAfterAggs">
        <![CDATA[
            ## 通过searchafter实现分页查询
            #if($lastStartTime && $lastStartTime > 0)
                #if($orderBy && $orderBy.equals("elapsed"))
                "search_after": [#[lastElapsed],#[lastStartTime],"trace#$lastId"],
                #else
                "search_after": [#[lastStartTime],"trace#$lastId"],
                #end
            #end
            "size": $pageSize,
            "sort": [
                 #if($orderBy && $orderBy.equals("elapsed")){"elapsed": "desc"},#end
                {"startTime": "desc"},
                {"_uid": "desc"}
            ],
            #* 
              应用服务调用次数聚合统计：
              按每分钟统计服务调用次数
            *#  
            "aggs": {
                "traces_date_histogram": {
                    "date_histogram": {
                        "field": "starttimeDate",
                        "interval": "1m",
                        "time_zone": "Asia/Shanghai",
                        "min_doc_count": 0
                    }
                }
            },]]>
    </property>
```



### **5.3.3 #[application]变量使用** 

- #### **#[application]**

变量**application**在替换值时，如果是字符串类型会在值的两边加上"",例如

带变量**application**的脚本：

```json
{"term": {
                            "applicationName": #[application]
                        }}
```

如果变量application为String类型，值为testweb,那么替换后得到:

```json
{"term": {
                            "applicationName": "testweb"
                        }}
```

如果变量application为数字类型，值为100,那么替换后得到:

```json
{"term": {
                            "applicationName": 100
                        }}
```

变量格式#[aaa]所有格式： 
\#[aaa] 简单的变量属性引用 

\#[aaa->bb] 如果aaa是一个bean对象，这个变量格式表示了对aaa对象的bb属性的引用，如果aaa是一个map对象，这个变量格式表示了对aaa对象的key为bb的元素值引用

#[aaa[key]] 引用map对象aaa中key所对应的value数据,引用map元素的等价方法#[aaa->key] 
#[aaa[0]] （一维数组中的第一个元素，或者list中的第一个元素,具体取决于aaa变量是一个数组还是list对象） 
#[aaa\[0\][1]]（二维数组中的第一维度的第二个个元素，或者list中的第一个元素的数第二个组元素或者list第第二个元素,具体取决于aaa变量是每一维度是数组还是list对象） 

#[aaa\[0][1]...]（多维数组中的第一维度的第二个个元素的n维元素，或者list中的第一个元素的第二个数组元素或者list第二个元素的n维元素引用,具体取决于aaa变量是每一维度是数组还是list对象） 

#[aaa\[key][0]] 引用map对象aaa中key所对应的value的第一个元素，取决于value的类型是数组还是list,等价引用方法#[aaa->key[0]] 

 

以上就是全部的类型，每种类型可以任意组合，例如： 
#[aaa->bb[0]] 
#[aaa[0]->bb[0]] 

\#[aaa[0]->bb[0]->fff] 
\#[aaa[0]->bb[0]->cc[keyname]] 
\#[aaa[0]->bb->cc[keyname]] 
等等 

- #### #[application]变量属性

另外，可以在#[]变量中指定escapeCount,serialJson,quoted、lpad、rpad、escape、esEncode、dateformat/locale/timezone属性，属性和变量名称用逗号分隔：

\#[变量名,quoted=false,lpad=xxx,rpad=ddd]

\#[变量名,quoted=false,lpad=xxx|3,rpad=ddd|4]

\#[dynamicPriceTemplate->goodName,escapeCount=2]

\#[dynamicPriceTemplate->rules[$velocityCount],serialJson=true]

**#[testVar,serialJson=true]**

说明如下：

- **serialJson** boolean类型，通过属性serialJson指示框架直接将对象序列化为json数据,使用案例：

\#[dynamicPriceTemplate->rules[$velocityCount],serialJson=true]

\#[testVar,serialJson=true]

- **escapeCount** int类型，在脚本中，含有特殊字符的goodName需要转义2次,使用案例：

\#[dynamicPriceTemplate->goodName,escapeCount=2]

- **quoted**  boolean类型，控制是否为字符串变量和日期变量串两头添加"号，true添加，false不加，默认为true，一般在不需要自动加"号的情况下使用，示例如下：

```java
"asdfaf#[application,quoted=false]s"
```

变量application的值为testweb，解析后的效果如下：

```java
"asdfaftestwebs"
```

- **lpad、rpad** 在通过lpad（左边追加）和rpad（右边追加）变量值两头追加字符串，同时可以通过|指定一个数字，表示追加多少次，示例如下：

简单的例子：

```javascript
"#[application,quoted=false,lpad=#]s"
```

变量的值为testweb，解析后的效果如下：

```java
"#testwebs"
```

带倍数的例子

```java
"ddd#[application,quoted=false,lpad=#|2,rpad=#|3]s"
```

变量的值为testweb，解析后的效果如下：

```java
"ddd##testweb###s"
```

- **dateformat/locale/timezone** 一组时间相关的属性，用来对时间类型的变量进行转换和处理，示例如下：

```json
"term": {
    "startDate": #[date,dateformat=yyyy-MM-dd HH:mm:ss,locale=zh_CN,timezone=Asia/Shanghai]
}
```

变量值设置为new Date(),那么解析后的效果如下：

```json
 "term": {
     "startDate": "2018-01-20 12:52:35"
  }
```

注意：

1. 在map中传递日期类型参数，则可以通过**dateformat/locale/timezone属性**在变量中指定所需要的日期格式，如果不指定则默认采用utc时区的日期格式：

```json
"term": {
    "startDate": #[date,dateformat=yyyy-MM-dd'T'HH:mm:ss.SSS'Z',timezone=Etc/UTC],
    "endDate": #[date,dateformat=yyyy-MM-dd HH:mm:ss,timezone=Asia/Shanghai]
}
```

```json
"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null,"Etc/UTC"
```

2. 在bean实体对象中日期类型field，**dateformat/locale/timezone属性优先起作用，**注解@JsonFormat，@Column 来指定自定义日期格式其次：

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
@Column(dataformat = "yyyy-MM-dd HH:mm:ss") 
protected Date agentStarttime;
```

​     如果不指定注解@JsonFormat，@Column，最后默认为日期类型的bean属性采用utc时区的日期格式：

```json
"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null,"Etc/UTC"
```

- **escape** 用于控制是否对参数值进行特殊字符转义处理，true 处理 false 不处理，默认为空，这时候如果是map传递的参数，默认转义；如果是bean实体传值，如果bean属性指定了@Column(escape="false"),则按照注解中设置的escape属性值来控制是否转义，如果没有在column注解中制定escape，则默认转义处理

​        escape使用实例：   

```json
"term": {
     "applicationName": #[applicationName,escape=false]
 }
 ## 不转义，两边不追加双引号，等价于$applicationName
"term": {
     "applicationName": #[applicationName,escape=false,quote=false]
 }
```

- **esEncode** boolean 类型，默认值false（不转义elasticsearch操作符），true（转义elasticsearch操作符），用于在query_string中对应不要作为elasticsearch操作符字符（\+ - = && || ! ( ) { } [ ] ^ " ~ * ? : \ /）进行转义处理，例如：

  ```json
  {
    "query" : {
      "query_string" : {
        "query" : "kimchy\\!",
        "fields"  : ["user"]
      }
    }
  }
  ```

​      使用bboss时只需要传入变量值kimchy!，然后用#[xxx,esEncode=true]可控制操作符转义处理：

```json
{
  "query" : {
    "query_string" : {
      "query" : #[condition,esEncode=true],   ## condition变量传入的值为kimchy!
      "fields"  : ["user"]
    }
  }
}
```



### **5.3.4 $application类型变量使用**

$类型的变量，只是做值替换，所以对于""这样的类型修饰符，需要自己在query dsl中加上，在类型明确的情况下，使用$变量比较合适，举例说明：

带变量$**application**的脚本：

```json
{"term": {
                            "applicationName": "$application"
                        }}
```

如果变量application为String类型，值为testweb,那么替换后得到:

```json
{"term": {
                            "applicationName": "testweb"
                        }}
```

如果变量application为数字类型，值为100,那么替换后得到:

```json
{"term": {
         "applicationName": "100" ##数字100被当成String处理了，这种情况下可能会出现不可预知的问题
}}
```

$方式的变量还用于逻辑判断和foreach循环。

变量如果是一个对象，可以通过以下方式引用对象中定义的属性：

```java
$customer.Address
$purchase.Total
或者
$customer.getAddress()
$purchase.getTotal()
```

List或者Array数组的size长度获取和为空判断方法：

```
$myarray.isEmpty()

$myarray.size()
```

- 在dsl中定义$类型变量

除了外部传参的$变量,还可以在dsl中定义自己的$变量，例如：

变量定义

```json
#set( $hasParam = false )
```

然后在dsl其他地方可以修改变量的值

```json
#set( $hasParam = true )
```

案例：

```xml
   <property name="qcondition">
        <![CDATA[
         #set( $hasParam = false ) ## 定义一个是否已经有参数条件的bool变量，控制后续的条件是否前面加逗号
        "bool": {
            "filter": [
                #if($application && !$application.equals("_all"))
                 #set( $hasParam = true )
                {"term": {
                    "applicationName": #[application]
                }}
                #end
                #if($queryStatus.equals("success"))                 
                  #if($hasParam),#end
                  {"term": {

                       "err": 0
                  }}
                #elseif($queryStatus.equals("error"))                  
                  #if($hasParam),#end
                  {"term": {

                       "err": 1
                  }}
                #end
            ],
            "must": [
                #if($queryCondition && !$queryCondition.equals(""))
                {
                    "query_string": {
                        "query": #[queryCondition],
                        "analyze_wildcard": true,
                        "all_fields": true
                    }
                },
                #end
                {
                    "range": {
                        "startTime": {
                            "gte": #[startTime],
                            "lt": #[endTime],
                            "format": "epoch_millis"
                        }
                    }
                }
            ]
        }]]>
    </property>
```

### 5.3.5 \#[xxx]和$xxx两种模式变量的区别

本小节总结#[xxx]和$xxx两种模式变量的区别：

1. \#[xxx]自动对变量值中的特殊字符进行转义处理，而$xxx不会进行处理直接输出原始值
2. \#[xxx]自动在String类型变量两边加上双引号""，如果不需要双引号，就写成这样：#[xxx,quoted=false]
3. \#[xxx]自动对日期类型变量值采用Utc标准时间格式（yyyy-MM-dd'T'HH\\:mm\\:ss.SSS'Z'）进行格式化处理，而$xxx不会进行处理直接输出原始值
4. 因此如果$xxx模式变量参数中包含有特殊字符或者是日期类型，请在程序中自行处理好
5. $xxx可用于逻辑判断、循环处理语句，#[xxx]不能用于逻辑判断、循环处理语句
6. $xxx变量参数值拼接到dsl中需要特别注意，如果变量的值不确定，变化频繁，在高并发的场景下回导致严重的性能问题；$xxx用在foreach和if/else语法中不会存在这个问题

**建议**:**在dsl拼接中采用#[xxx]替代$xxx模式变量，在foreach和if/else语法中使用$xxx.**

 **${xxx}都是硬解析的，除了if-else和foreach条件、foreach循环下标变量velocityCount中使用，其他地方都尽量避免使用  ${xxx}模式变量**

### 5.3.6 foreach循环中使用#[xxx]模式变量及优化

可以下foreach循环中结合循环计数器$velocityCount使用#[xxx]模式变量，例如：

```json


"terms":{"sourceFields":[
    					 #foreach($sourceField in $sourceFields) 
                             #if($velocityCount > 0),#end 
                             #[sourceFields[$velocityCount]] 
                         #end 
                        ]
        }


```

某些情况（比如list集合、数组或者对象转json的转换处理场景）的的foreach循环可以进行优化编写，可以将上面的foreach循环优化为以下形式：

```json
"terms":{"sourceFields":#[sourceFields,serialJson=true]}
```



### **5.3.7 @{pianduan}-片段变量使用**

@{}类型变量用于在query dsl中引用脚本片段。很多的dsl脚本会包含一些公共内容，比如查询条件，聚合操作脚本等等，可以把这些公共部分抽取出来定义成dsl片段；另外，一些复杂的搜索聚合查询的dsl脚本很长，由很多比较通用独立的部分组成，这样也可以将独立部分剥离形成片段，这样dsl的结构更加清晰，更加易于维护。**片段定义一定要定义在引用片段的dsl脚本前面**，片段引用变量示例如下：

定义片段searchAfterAggs和qcondition：

```xml
<!--
        分页查询和按日期分钟统计片段
        应用：链路检索和统计查询，rpc时间段统计查询
    -->
    <property name="searchAfterAggs">
        <![CDATA[
            #if($lastStartTime && $lastStartTime > 0)
                #if($orderBy && $orderBy.equals("elapsed"))
                "search_after": [#[lastElapsed],#[lastStartTime],"trace#$lastId"],
                #else
                "search_after": [#[lastStartTime],"trace#$lastId"],
                #end
            #end
            "size": #[pageSize],
            "sort": [
                 #if($orderBy && $orderBy.equals("elapsed")){"elapsed": "desc"},#end
                {"startTime": "desc"},
                {"_uid": "desc"}
            ],
            "aggs": {
                "traces_date_histogram": {
                    "date_histogram": {
                        "field": "starttimeDate",
                        "interval": "1m",
                        "time_zone": "Asia/Shanghai",
                        "min_doc_count": 0
                    }
                }
            },]]>
    </property>
    <!--
    查询条件片段
    -->
    <property name="qcondition">
        <![CDATA[
        "bool": {
            "filter": [
                #if($application && !$application.equals("_all"))
                {"term": {
                    "applicationName": #[application]
                }}
                #end
                #if($queryStatus.equals("success"))
                  #if($application && !$application.equals("_all")),#end
                  {"term": {

                       "err": 0
                  }}
                #elseif($queryStatus.equals("error"))
                  #if($application && !$application.equals("_all")),#end
                  {"term": {

                       "err": 1
                  }}
                #end
            ],
            "must": [
                #if($queryCondition && !$queryCondition.equals(""))
                {
                    "query_string": {
                        "query": #[queryCondition],
                        "analyze_wildcard": true,
                        "all_fields": true
                    }
                },
                #end
                {
                    "range": {
                        "startTime": {
                            "gte": #[startTime],
                            "lt": #[endTime],
                            "format": "epoch_millis"
                        }
                    }
                }
            ]
        }]]>
    </property>
```

引用片段：

```xml
<property name="queryServiceByCondition">
        <![CDATA[{
            "version": true,
            @{searchAfterAggs} //片段引用，此处是一个占位符，在系统加载配置文件时候，
                                 直接被searchAfterAggs对应的片段替换
            "query": {
                @{qcondition} //片段引用，此处是一个占位符，在系统加载配置文件时候，
                                直接被qcondition对应的片段替换
            },
            "_source": {
                "excludes": []
            },

            "highlight": {
                "pre_tags": [
                    "<mark>"
                ],
                "post_tags": [
                    "</mark>"
                ],
                "fields": {
                    "*": {
                        "highlight_query": {
                            @{qcondition}  //片段引用，此处是一个占位符，在系统加载配置文件时候，
                                             直接被qcondition对应的片段替换
                        }
                    }
                },
                "fragment_size": 2147483647
            }
        }]]></property>
```

片段变量只是一个占位符，在系统第一次加载配置文件时候，直接被qcondition对应的片段内容替换。

片段定义中同样可以引用其他片段。

亦可以引用其他文件中定义的dsl片段(外部片段引用bboss 5.5.6才支持)，例如：

[esmapper/pianduan.xml](https://github.com/bbossgroups/elasticsearch-example/tree/master/src/main/resources/esmapper/pianduan.xml)

```xml
<property name="pianduan">
    <![CDATA["query": {
                "bool": {
                    "must": [{
                        "term": {
                            "cityId": #[cityId]
                        }
                    },
                    {
                        "terms": {
                            "titleId": #[titleId,serialJson=true]
                        }
                    },
                    {
                        "bool": {
                            "should": [{
                                "term": {
                                    "deptId1": #[deptId1]
                                }
                            },
                            {
                                "term": {
                                    "deptId2": #[deptId2]
                                }
                            },
                            {
                                "term": {
                                    "deptId3": #[deptId3]
                                }
                            }]
                        }
                    }]
                }
            }]]>
</property>
```

外部片段声明和引用：[esmapper/outpianduanref.xml](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/outpianduanref.xml)

```xml
<property name="outPianduanRef"
              templateFile="esmapper/pianduan.xml"
              templateName="pianduan"/>
<property name="testoutPianduan">
        <![CDATA[{ ## 最多返回1000条记录
            size: #[size],
            @{outPianduanRef}
        }]]>
    </property>
```



### **5.3.8 不同dsl配置文件之间的dsl语句引用**

bboss elastic还支持不同dsl配置文件之间的dsl引用,例如：

```xml
  <!--
    querySqlTraces直接引用链路查询模板文件esmapper/estrace/ESTracesMapper.xml中定义的查询dsl语句
    queryTracesByCriteria
  -->
  <property name="querySqlTraces"
            templateFile="esmapper/estrace/ESTracesMapper.xml"
            templateName="queryTracesByCriteria"/>
```

说明：querySqlTraces直接引用链路查询模板文件esmapper/estrace/ESTracesMapper.xml中定义的查询dsl语句queryTracesByCriteria，**注意这里只是引用，在热加载机制中，当原始定义文件对应的dsl语句被修改，引用的地方也会同时被修改**。



### **5.3.9 sql语句中回车换行符替换指示语法以及变量使用注意事项**

\#""" """,包含在这个中间的dsl片段中包含的回车换行符会被替换成空格，使用示例及注意事项:

```xml
<property name="sqlPagineQuery">
    <![CDATA[
     {
     ## 指示sql语句中的回车换行符会被替换掉开始符,注意dsl注释不能放到sql语句中，否则会有问题，因为sql中的回车换行符会被去掉，导致回车换行符后面的语句变道与注释一行
     ##  导致dsl模板解析的时候部分sql段会被去掉
        "query": #"""
                SELECT * FROM dbclobdemo
                    where channelId=#[channelId]
         """,
         ## 指示sql语句中的回车换行符会被替换掉结束符
        "fetch_size": #[fetchSize]
     }
    ]]>
</property>
```

![img](https://oscimg.oschina.net/oscnet/2e1115df01f4ef89faa689ce4747870db82.jpg)

比较复杂的情况就是在多行sql中引用多行片段，需要通过在property元素上加escapeQuoted="false"控制引用的片段不加""，否则会在SQL中出现双引号，破坏sql语法结构，下面是一个实例：

```xml
   <!--
           通用sql字段列表，可以被其他sql引用 
   -->
   <property name="sqlPianduan" escapeQuoted="false">
       <![CDATA[
         #"""
           channelId,
           application,
           applicationName,
           address,
           timeDate,
           day
         """
       ]]>
   </property>
   <!--
       分页sql query
       每页显示 fetch_size对应的记录条数
   
   -->
   <property name="sqlPagineQueryUsePianduan">
       <![CDATA[
        {
        ## 指示sql语句中的回车换行符会被替换掉开始符,注意dsl注释不能放到sql语句中，否则会有问题，因为sql中的回车换行符会被去掉，导致回车换行符后面的语句变道与注释一行
        ##  导致dsl模板解析的时候部分sql段会被去掉
           "query": #"""
                   SELECT
                   @{sqlPianduan}
                   FROM dbclobdemo
                   where channelId=#[channelId]
            """,
            ## 指示sql语句中的回车换行符会被替换掉结束符
           "fetch_size": #[fetchSize]
        }
       ]]>
   </property>

```



### **5.3.10 变量使用注意事项**

bboss dsl语法支持#[channelId]和$channelId两种模式的变量定义，在sql语句中使用变量需要注意几个地方，下面的举例说明。

如果channelId是数字，那么以下两条sql语句的写法是等价的

SELECT * FROM dbclobdemo where channelId=#[channelId]

SELECT * FROM dbclobdemo where channelId=$channelId

如果channelId是字符串，那么分两种情况处理

**情况1 channelId中不会包含回车换行之类的特殊字符,那么下面两种写法等价**

SELECT * FROM dbclobdemo where channelId='#[channelId,quoted=false]'

SELECT * FROM dbclobdemo where channelId='$channelId'

\#[channelId,quoted=false]中必须包含属性quoted=false，指示框架不要在值的两边加双"号，否则会破坏sql语法

**情况2 channelId中会或者可能会包含回车换行之类的特殊字符,那么只能采用以下的写法**

SELECT * FROM dbclobdemo where channelId='#[channelId,quoted=false]'

**bboss执行sql分页查询方法：**

```java
/**
 * 配置文件中的sql dsl检索,返回Map类型集合，亦可以返回自定义的对象集合
 */
@Test
public void testObjectSQLQueryFromDSL(){
   ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
   //设置sql查询的参数

   Map params = new HashMap();
   params.put("channelId",1);
   params.put("fetchSize",1);
   SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"sqlPagineQuery",params);

   do{
      List<DocObject> datas = sqlResult.getDatas();
      if(datas == null || datas.size() == 0){
         System.out.println(0);//处理数据
         break;
      }
      else{
         System.out.println(datas.size());//处理数据
         sqlResult = sqlResult.nextPage();//获取下一页数据

      }

   }while(true);

}
```



### **5.3.11** 文本块脚本配置语法用法

在一些脚本或者字段值中可能存在一个值占多行的场景，那么在dsl配置中，elasticsearch官方dsl语法""" """来包含多行脚本，例如

```json
	       {
              "last": """
              asdfasdfasdf
                asdfasdfasdfasdfasdf
              """,
              "nick": "test"
            }
```

bboss提供了dsl 语法了对多行值进行处理，最终会转换为elasticsearch官方dsl语法：

```json
@"""

多行值

多行值

"""
```

- 简单脚本案例

字段中的多行值案例 

```xml
    <property name="scriptPianduan">
        <![CDATA[
            "params": {
              "last": @"""
              asdfasdfasdf
                asdfasdfasdfasdfasdf
              """,
              "nick": #[nick]
            }
        ]]>
    </property>
```

以下是一个script的应用案例

```xml
    <property name="scriptPianduan">
        <![CDATA[
            "params": {
              "last": #[last],
              "nick": #[nick]
            }
        ]]>
    </property>
    <property name="scriptDsl">
        <![CDATA[{
          "script": {
            "lang": "painless",
            "source": @""" ## 脚本开始
              ctx._source.last = params.last;
              ctx._source.nick = params.nick
            """,## 脚本结束
            @{scriptPianduan}
          }
        }]]>
    </property>
```

执行上述脚本的java代码示例：

```java
    private String mappath = "esmapper/demo.xml";

	public void updateDocumentByScriptPath(){
		DocumentCRUD documentCRUD = new DocumentCRUD();
		documentCRUD.testCreateIndice();
		documentCRUD.testBulkAddDocument();
		//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
		Map<String,Object> params = new HashMap<String,Object>();
		 
		params.put("last","gaudreau");
		params.put("nick","hockey");
		clientUtil.updateByPath("demo/demo/2/_update?refresh","scriptDsl",params);
		String doc = clientUtil.getDocument("demo","demo","2");
		System.out.println(doc);

	}
```

- 更加复杂的案例 

```xml
    <property name="scriptPianduan1">
        <![CDATA[
            "params": {
              "last": #[last],
              "nick": #[nick],
              "goodsId": #[dynamicPriceTemplate->goodsId],
              "rules":[
                   #foreach($rule in $dynamicPriceTemplate.rules)
                       #if($velocityCount > 0),#end
                   {
                        "ruleId": #[dynamicPriceTemplate->rules[$velocityCount]->ruleId],
                        "ruleCount": #[dynamicPriceTemplate->rules[$velocityCount]->ruleCount],
                        "ruleExist": #[dynamicPriceTemplate->rules[$velocityCount]->ruleExist]
                    }
                   #end
              ]
            }
        ]]>
    </property>

    <property name="scriptPianduan2">
        <![CDATA[
            "params": {
              "last": #[last],
              "nick": #[nick],
              "goodsId": #[dynamicPriceTemplate->goodsId],
              "rules":[
                   #foreach($rule in $dynamicPriceTemplate.rules)
                       #if($velocityCount > 0),#end
                       #[dynamicPriceTemplate->rules[$velocityCount],serialJson=true] ## 通过属性serialJson指示框架直接将对象序列化为json数据
                   #end
              ]
            }
        ]]>
    </property>

    <property name="scriptPianduan">
        <![CDATA[
            "params": {
              "last": #[last],
              "nick": #[nick],
              "goodsId": #[dynamicPriceTemplate->goodsId],
              "rules":[
                   #foreach($rule in $dynamicPriceTemplate.rules)
                       #if($velocityCount > 0),#end
                   {

                        "ruleId": "$rule.ruleId",
                        "ruleCount": $rule.ruleCount,
                        "ruleExist":  $rule.ruleExist

                    }
                   #end
              ]
            }
        ]]>
    </property>
    <property name="scriptDsl">
        <![CDATA[{
          "script": {
            "lang": "painless",
            "source": @"""
              ctx._source.last = params.last;
              ctx._source.nick = params.nick;
              ctx._source.rules = params.rules
            """,
            @{scriptPianduan}
          }
        }]]>
    </property>

    <property name="scriptDslByQuery">
        <![CDATA[{
          "query": {
            "bool": {
              "must": [
                {
                  "term": {
                    "_id": #[id]
                  }
                }
              ]
            }
          },
          "script": {
            "lang": "painless",
            "source": @"""
              ctx._source.last = params.last;
              ctx._source.nick = params.nick;
              ctx._source.goodName = #[dynamicPriceTemplate->goodName,escapeCount=2];#*在脚本中，含有特殊字符的goodName需要转义2次*#
              ctx._source.goodsId = #[dynamicPriceTemplate->goodsId];
              ctx._source.dynamicPriceTemplate.goodsId = params.goodsId;
              ctx._source.rules = params.rules
            """,
            @{scriptPianduan2}
          }
        }]]>
    </property>
```

对应的java代码：

```java
    public void updateDocumentByScriptQueryPath(){
		//初始化数据，会创建type为demo的indice demo，并添加docid为2的文档
		DocumentCRUD documentCRUD = new DocumentCRUD();
		documentCRUD.testCreateIndice();
		documentCRUD.testBulkAddDocument();
		//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
		Map<String,Object> params = new HashMap<String,Object>();
		DynamicPriceTemplate dynamicPriceTemplate = new DynamicPriceTemplate();
		dynamicPriceTemplate.setGoodsId(1);
		dynamicPriceTemplate.setGoodName("asd\"国家");
		List<Rule> ruleList = new ArrayList<Rule>();
		Rule rule = new Rule();
		rule.setRuleCount(100);
		rule.setRuleExist(true);
		rule.setRuleId("asdfasdfasdf");
		ruleList.add(rule);

		rule = new Rule();
		rule.setRuleCount(101);
		rule.setRuleExist(false);
		rule.setRuleId("bbbb");
		ruleList.add(rule);

		rule = new Rule();
		rule.setRuleCount(103);
		rule.setRuleExist(true);
		rule.setRuleId("ccccc");
		ruleList.add(rule);
		dynamicPriceTemplate.setRules(ruleList);


		//为id为2的文档增加last和nick两个属性
		params.put("last","gaudreau");
		params.put("nick","hockey");
		params.put("id",3);
		params.put("dynamicPriceTemplate",dynamicPriceTemplate);
		//通过script脚本为id为2的文档增加last和nick两个属性，为了演示效果强制refresh，实际环境慎用
		clientUtil.updateByPath("demo/demo/_update_by_query?refresh","scriptDslByQuery",params);
		//获取更新后的文档，会看到新加的2个字段属性
		String doc = clientUtil.getDocument("demo","demo","3");
		System.out.println(doc);

	}
```



### **5.3.12 逻辑判断和foreach循环用法**

逻辑判断语法：#if-#else-#end,#if-#elseif-#else-#end

循环语法：#foreach-#end

循环控制计数器变量：$velocityCount

举例说明如下：



#### 案例1：#if-#end 和#foreach-#end相结合

如果集合中元素类型不确定，使用#[]类型变量

```java
{
#foreach($ldxxbh in $ldxxbhs) ## foreach循环，变量$ldxxbhs是一个list集合,$ldxxbh对应循环中的元素变量，
                                对应当前遍历的元素
   #if($velocityCount > 0),#end  ## $velocityCount是foreach循环控制变量
        "v$velocityCount":#[ldxxbhs[$velocityCount]]  ## 拼接每个元素到脚本中，如果集合中元素类型不确定，使用#[]类型变量，同时又结合$velocityCount做集合的下标索引
   
#end  
}    
```

传入一个List集合的属性ldxxbhs，包含以下元素值：

{"aa",1,"bb",33,"cc"}

经过解析得到最终的query dsl脚本为：

```json
{
   "v0":"aa",
   "v1":1,
   "v2":"bb",
   "v3":33,
   "v4":"cc" 
}
```



#### 案例2：循环List或者数组中的bean对象，并访问每个bean对象属性id

```java
"dynamic_price_template.rules":#foreach($rule in $rules)#if($velocityCount > 0),#end #[rules[$velocityCount]->id]  #end 
```



#### 案例3：动态排序字段案例-#[xxx]模式变量（适合各种场景） 

```json
#if($sortColumn)  ##动态排序字段案例
	"sort":[                   
		#foreach( $column in $sortColumn)
			#if($velocityCount > 0),#end
			{
				#[sortColumn[$velocityCount]->sort] :{
					"order" : #[sortColumn[$velocityCount]->order]
				}
			}
		#end
	],
#end
```



#### 案例4：动态排序字段案例-$xxx模式变量 （适合集合值固定且长度固定并不包含特殊字符的场景）        

```json
#if($sortColumn) 
	"sort":[                   
        #foreach( $column in $sortColumn)
            #if($velocityCount > 0),#end
            {
               "$column.Sort" :{
                   "order": "$column.Order"
                }
            }
        #end
   ],            
#end                
```



#### 案例5：循环遍历map对象的key和值-$xxx模式变量

适合集合值固定且长度固定并不包含特殊字符的场景 

```json
#if($sortColumn) #从map中获取所有的排序字段
	"sort":[             
        
            #foreach( $key in $columnMap.keySet() ) 
            #if($velocityCount > 0),#end
            {
                "$key": {
                   "order" :"$columnMap.get($key)"
                }				 
            }
            #end
   ],            
#end 
```

案例6：循环遍历map对象的key和值-#[xxx]模式变量 

适合集合值不固定，或者长度不固定，或者可能包含特殊字符等多种场景 

将map中key-value拼接为多个match检索条件：

```json
        {
            "query": {
                "bool": {
                    "must": [
                        #foreach($key in $condition.keySet())
                        #if($velocityCount > 0),#end
                        {
                            "match": {
                                "$key": #[condition[$key]]
                            }
                        }
                        #end
                    ]
                }
            },
            "size": 1000
        }
```

排序字段遍历拼接：

```json
#if($sortColumn) #从map中获取所有的排序字段
	"sort":[             
        
            #foreach( $key in $columnMap.keySet() ) 
            #if($velocityCount > 0),#end
            {
               "$key": {
                   "order" :#[columnMap[$key]]
                }				 
            }
            #end
   ],            
#end 
```



#### 案例7 #if-#else-#end结合$xxx变量

适合于集合元素少，值固定的场景，且值中不包含破坏json格式的特殊字符，否则参考案例8或者案例9

```java
#if(!$searchFields && $searchFields.size() == 0)
    "fields": ["rpc","params","agentId","applicationName","endPoint","remoteAddr"]
#else
    "fields":[
          #foreach($field in $searchFields)
             #if($velocityCount > 0),#end "$field" //拼接每个元素到脚本中，如果集合中元素类型确定，使用$xxx类型变量
          #end
     ]
#end
```



#### 案例8 #if-#else-#end结合#[xxx]变量(适合各种场景)

如果集合元素，使用$xxx类型变量，以拼接检索字段为例: 

```java
#if(!$searchFields && $searchFields.size() == 0)
    "fields": ["rpc","params","agentId","applicationName","endPoint","remoteAddr"]
#else
    "fields":[
          #foreach($field in $searchFields)
             #if($velocityCount > 0),#end #[searchFields[$velocityCount]]
          #end
     ]
#end
```



#### 案例9 #if-#else-#end结合#[xxx,serialJson=true]变量



(适合各种场景)

```javascript
#if(!$searchFields && $searchFields.size() == 0)
    "fields": ["rpc","params","agentId","applicationName","endPoint","remoteAddr"]
#else
    "fields":#[searchFields,serialJson=true]
#end
```

本案例其实是案例8的一种简单的写法，我们将

```json
"fields":[
          #foreach($field in $searchFields)
             #if($velocityCount > 0),#end #[searchFields[$velocityCount]]
          #end
     ]
```

借助于serialJson=true属性直接将集合searchFields处理为对应的json集合格式，转换为下面的写法：

```json
"fields":#[searchFields,serialJson=true]
```

同样的可以对terms检索进行处理，例如：

```json
 {
                        "terms": {
                            "titleId": [
          #foreach($field in $titleIds)
             #if($velocityCount > 0),#end #[titleIds[$velocityCount]]
          #end
     ]
                        }
                    }
```

借助于serialJson=true属性直接将集合searchFields处理为对应的json集合格式，转换为下面的写法：

```json
 {
                        "terms": {
                            "titleId": #[titleIds,serialJson=true]
                        }
                    }
```



#### 案例10 foreach循环嵌套遍历

遍历list

```xml
 <property name="dynamicInnerDsl">
        <![CDATA[{ ## 最多返回1000条记录
            size: #[size],
            "query": {
                "bool": {
                    "must": [
                    
                    #foreach($innerList in $outList)
                        #set($outVelocityCount = $velocityCount)
                        #foreach($item in $innerList)
                        #if($velocityCount > 0), #end
                        {
                            "xxxx": {
                                "xxxx": #[outList[$outVelocityCount][$velocityCount]]
                            }
                        }
                        #end
                    #end
                    ]
                }
            }
        }]]>
    </property>
```

遍历map

foreach嵌套dsl脚本定义

```xml
    <property name="dynamicInnerDsl">
        <![CDATA[{ ## 最多返回1000条记录
            size: #[size],
            "query": {
                "bool": {
                    "must": [
                    #set($needComma = false)
                    #foreach($condition in $conditions.entrySet())
                        #foreach($item in $condition.value.entrySet())
                        #if($needComma), #else #set($needComma = true) #end
                        {
                            "$condition.key": {
                                "$item.key": #[conditions[$condition.key][$item.key],serialJson=true]
                            }
                        }
                        #end
                    #end
                    ]
                }
            }
        }]]>
    </property>
```

传递参数和解析上述dsl的java方法代码

```java
	@Test
	public void dynamicInnerDsl(){
		Map conditions = new HashMap<String,Map<String,Object>>();
		Map<String,Object> term = new HashMap<String, Object>();
		term.put("terma","tavalue");
		term.put("termb","tbvalue");
		term.put("termc","tcvalue");
		conditions.put("term",term);

		Map<String,Object> terms = new HashMap<String, Object>();
		terms.put("termsa",new String[]{"tavalue","tavalue1"});
		terms.put("termsb",new String[]{"tbvalue","tbvalue1"});
		terms.put("termsc",new String[]{"tcvalue","tcvalue1"});
		conditions.put("terms",terms);

		Map params = new HashMap();
		params.put("conditions",conditions);
		params.put("size",1000);
		//加载配置文件中的dsl信息，解析dsl语句dynamicInnerDsl
		ESUtil esUtil = ESUtil.getInstance("esmapper/dsl.xml");
		String parseResult = ESTemplateHelper.evalTemplate(esUtil,"dynamicInnerDsl",params);
		//打印解析结果
		System.out.println(parseResult);

	}
```

运行上述代码打印出来的实际dsl

```json
{
    "size": 1000,
    "query": {
        "bool": {
            "must": [
                {
                    "terms": {
                        "termsc": ["tcvalue","tcvalue1"]
                    }
                },
                {
                    "terms": {
                        "termsa": ["tavalue","tavalue1"]
                    }
                },
                {
                    "terms": {
                        "termsb": ["tbvalue","tbvalue1"]
                    }
                },
                {
                    "term": {
                        "termb": "tbvalue"
                    }
                },
                {
                    "term": {
                        "termc": "tcvalue"
                    }
                },
                {
                    "term": {
                        "terma": "tavalue"
                    }
                }
            ]
        }
    }
}
```



#### 案例11 综合案例

```json
{
    "query": {
        "bool": {
            "filter": [
                #if($channelApplications && $channelApplications.size() > 0)
                {
                    "terms": {
                        "applicationName.keyword": [
                        #foreach($application in $channelApplications) ## 遍历列表channelApplications，列表中对象类型Application包含属性applicationName
                           #if($velocityCount > 0),#end #[channelApplications[$velocityCount]->applicationName] ## 引用列表中元素属性applicationName的语法
                        #end
                        ]
                    }
                },
                #end
                {"range": {
                        "startTime": {
                            "gte": #[startTime],##统计开始时间
                            "lt": #[endTime]  ##统计截止时间
                        }
                    }
                }
            ]
        }
    },
    "size":0,
    "aggs": {
        "applicationsums": {
              "terms": {
                "field": "applicationName.keyword",##按应用名称进行统计计数
                "size":10000
              },
              "aggs":{
                    "successsums" : {
                        "terms" : {
                            "field" : "err" ##按err标识统计每个应用的成功数和失败数，0标识成功，1标识失败
                        }
                    },
                    "elapsed_ranges" : {
                        "range" : {
                            "field" : "elapsed", ##按响应时间分段统计
                            "keyed" : true,
                            "ranges" : [
                                { "key" : "1秒", "to" : 1000 },
                                { "key" : "3秒", "from" : 1000, "to" : 3000 },
                                { "key" : "5秒", "from" : 3000, "to" : 5000 },
                                { "key" : "5秒以上", "from" : 5000 }
                            ]
                        }
                    }
              }
        }
    }
}
```



### 5.3.13 日期类型使用方法

bboss对于日期类型的映射处理比较简单，分为两种情况：

第一种情况，采用默认的时间格式和utc时区定义，mapping field定义如下：

```json
"agentStarttime": {
    "type": "date"     
}
```

那么我们在对象中也只需要定义一个日期类型的字段与之对应即可： 

```java
private Date agentStarttime;
```

第二种情况，定义mapping field时指定了时间格式：

```json
"agentStarttime": {
    "type": "date",
     "format":"yyyy-MM-dd HH:mm:ss"
},
```

那么我们在对象中除了定义日期类型的字段，还要为字段加上时间格式的注解： 

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@Column(dataformat = "yyyy-MM-dd HH:mm:ss")
protected Date agentStarttime;
```

其中的pattern和dataformat必须和mapping中指定的格式一致。



### 5.3.14 dsl配置文件中关于dsl解析语法树缓存相关配置

可以在配置文件中直接指定dsl语法解析缓存参数（一般采用默认配置即可）

perKeyDSLStructionCacheSize:dsl配置文件中对应的dsl可以缓存的解析后的最大dsl语法结构个数

alwaysCacheDslStruction：布尔值，单个dsl超过perKeyDSLStructionCacheSize指定的dsl个数后，是否继续缓存dsl语句，true 缓存，并且清除不经常使用的dsl，false 不缓存（默认值），每次都硬解析。

```xml
<property name="perKeyDSLStructionCacheSize" value="2000"/>
<property name="alwaysCacheDslStruction" value="false"/>
```

配置样例：

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/dslcacheexample.xml

如果在程序运行过程中出现以下日志：

```properties
the=number of real dsl cache records exceeded the maximum cache size n allowed by DSL structure cache
```

那么可能存在两个原因：

1. perKeyDSLStructionCacheSize配置过小
    如果是这种情况，可以在dsl xml配置文件中，添加或者修改perKeyDSLStructionCacheSize参数，将值设置更大一些：
    <property name="perKeyDSLStructionCacheSize" value="4000"/>
2. dsl中使用的$var模式变量，而且值变化频繁，导致缓存命中率下降并使得缓存的dsl 结构超过perKeyDSLStructionCacheSize对应的数字，所以告警
   如果是这种情况，一个是调大perKeyDSLStructionCacheSize参数值，另外一个必须将dsl中的$var变量写法调整为#[var]方式，例如：

```xml
 <property name="updateByQuery">
        <![CDATA[
         {
          "query": {
            "has_child": {
              "type":       "employee",
              "score_mode": "max",
              "query": {
                "match": {
                  "name": $name  ##查询包含员工名称的公司信息，并修改之
                }
              }
            }
          }
        }
        ]]>
    </property>
```

将dsl中变量$name修改为 #[name]即可：

```xml
 <property name="updateByQuery">
        <![CDATA[
         {
          "query": {
            "has_child": {
              "type":       "employee",
              "score_mode": "max",
              "query": {
                "match": {
                  "name": #[name]  ##查询包含员工名称的公司信息，并修改之
                }
              }
            }
          }
        }
        ]]>
    </property>
```

如果不想缓存dsl语法结构，可以设置cacheDsl为false：

```properties
ds配置文件中xml节点property元素增加cacheDsl属性=指示框架是否启用dsl语法结构缓存机制，如果启用则只在第一次对dsl进行硬解析dsl语法结构，后续从缓冲中读取解析好的语法结构；如果禁用，则每次都硬解析dsl语法结构，默认true
```

使用cacheDsl禁用dsl语法结构缓存示例：

```xml
<property name="updateByQuery" cacheDsl="false">
        <![CDATA[
         {
          "query": {
            "has_child": {
              "type":       "employee",
              "score_mode": "max",
              "query": {
                "match": {
                  "name": #[name]  ##查询包含员工名称的公司信息，并修改之
                }
              }
            }
          }
        }
        ]]>
    </property>
```



## 5.4 一些小技巧



### 5.4.1 多条件判断逗号追加问题处理技巧

经常在dsl中碰到很多个条件的动态组合，中间的,号出现的位置不确定，例如：

```json
{
	"from": #[from],
	"size": #[size],
	"query": {
		"bool": {
			"filter": [
				#if($orderId)
					{
						"term": {
							"orderId.keyword":  #[orderId]
						}
					},
				#end		
			   #if($key)
					{
						"term": {
							"key.keyword":  #[key]
						}
					},
				#end
				#if($orderId)
					{
						"term": {
							"orderId.keyword":  #[orderId]
						}
					}
				#end
			]
		}
	}
}
```

逗号放到正确的位置的技巧如下：定义一个boolean局部变量来控制，变量定义语法为：

\#set( $needComma = false )

加上变量后的dsl如下：

```json
{
	"from": #[from],
	"size": #[size],
	"query": {
		"bool": {
			"filter": [
			    #set( $needComma = false )
				#if($orderId)
					{
						"term": {
							"orderId.keyword":  #[orderId]
						}
					}
					#set( $needComma = true )
				#end
				 #if($key)
				  	#if($needComma),#else #set( $needComma = true ) #end	
					{
						"term": {
							"key.keyword":  #[key]
						}
					},
				#end
				#if($name)
					#if($needComma), #end	
					{
						"term": {
							"name.keyword":  #[name]
						}
					}
				#end
				
			]
		}
	}
}
```

 

## 5.5 基于数据库管理dsl介绍

参考文档：

https://esdoc.bbossgroups.com/#/db-dsl

# **6 代码演示和文档资料**



## 6.1 完整query dsl定义文件

[esmapper/estrace/ESTracesMapper.xml](https://github.com/bbossgroups/elasticsearch-gradle-example/blob/master/src/main/resources/esmapper/estrace/ESTracesMapper.xml)

> ```xml
> <!--es
> https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl-term-query.html
> https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl-range-query.html
> -->
> ```



## 6.2 索引表模板新增和删除

[ESTemplate.xml文件定义](https://github.com/bbossgroups/elasticsearch-gradle-example/blob/master/src/main/resources/esmapper/estrace/ESTemplate.xml)

```java
public void testTempate() throws ParseException{

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTemplate.xml");
		//创建模板
		String response = clientUtil.createTempate("demotemplate_1",//模板名称
				"demoTemplate");//模板对应的脚本名称，在esmapper/estrace/ESTemplate.xml中配置
		System.out.println("createTempate-------------------------");
		System.out.println(response);
		//获取模板
		/**
		 * 指定模板
		 * /_template/demoTemplate_1
		 * /_template/demoTemplate*
		 * 所有模板 /_template
		 *
		 */
		String template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET-------------------------");
		System.out.println(template);
		//删除模板
		template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_DELETE);
		System.out.println("HTTP_DELETE-------------------------");
		System.out.println(template);

		template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET after delete-------------------------");
		System.out.println(template);
	}
```



## 6.3 创建和查询索引表模板

[ESTemplate.xml文件定义](https://github.com/bbossgroups/elasticsearch-gradle-example/blob/master/src/main/resources/esmapper/estrace/ESTemplate.xml)

> ```java
> public void testCreateTempate() throws ParseException{
> 
>    ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTemplate.xml");
>    //创建模板
>    String response = clientUtil.createTempate("demotemplate_1",//模板名称
>          "demoTemplate");//模板对应的脚本名称，在estrace/ESTemplate.xml中配置
>    System.out.println("createTempate-------------------------");
>    System.out.println(response);
>    //获取模板
>    /**
>     * 指定模板
>     * /_template/demoTemplate_1
>     * /_template/demoTemplate*
>     * 所有模板 /_template
>     *
>     */
>    String template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
>    System.out.println("HTTP_GET-------------------------");
>    System.out.println(template);
> 
> }
> ```
>
>  



## 6.4 更新x-pack license

```java
public void testLicense(){
        ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTemplate.xml");
   
		String ttt = clientUtil.executeHttp("_xpack/license?acknowledge=true","license",ClientUtil.HTTP_PUT);
		System.out.println(ttt);
//		ttt = clientUtil.createTempate("tracesql_template","traceSQLTemplate");
	}
```



## 6.5 创建索引表

```java
 ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
String ret = clientUtil.createIndiceMapping("trace", "createTraceIndice") ;
```



## 6.6 获取索引表结构



### json格式

> ```java
> public void testGetmapping(){
>    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
>    String date = format.format(new Date());
>   ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
>    System.out.println(clientUtil.getIndexMapping("demo-*"));
>    clientUtil.dropIndice("demo-"+date);
> }
> ```
>
>  



### 字段列表

```java
public void testQueryDocMapping(){
   ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
   List<IndexField> fields = clientUtil.getIndexMappingFields("trace-*",//索引表名称
                                                              "trace");//索引类型
   System.out.println(fields.size());
}
```



## 6.7 单文档操作

> ```java
> public void testAddDateDocument() throws ParseException{
>    testGetmapping();
>    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
>    String date = format.format(new Date());
>    ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
>    Demo demo = new Demo();
>    demo.setDemoId(5l);
>    demo.setAgentStarttime(new Date());
>    demo.setApplicationName("blackcatdemo");
>    demo.setContentbody("this is content body");
>    //根据dsl脚本创建索引文档，将文档保存到当天的索引表中demo-2018.02.03
>    String response = clientUtil.addDateDocument("demo",//索引表,自动添加日期信息到索引表名称中
>          "demo",//索引类型
>          "createDemoDocument",//创建文档对应的脚本名称，在esmapper/estrace/ESTracesMapper.xml中配置
>          demo);
> 
>    System.out.println("addDateDocument-------------------------");
>    System.out.println(response);
>   //根据文档id获取索引文档,返回json格式
>    response = clientUtil.getDocument("demo-"+date,//索引表，手动指定日期信息
>          "demo",//索引类型
>          "5");
>    System.out.println("getDocument-------------------------");
>    System.out.println(response);
> //根据文档id获取索引文档,返回Demo对象
>    demo = clientUtil.getDocument("demo-"+date,//索引表
>          "demo",//索引类型
>          "5",//索引文档ID
>          Demo.class);
> }
> ```
>
> 创建索引文档脚本：createDemoDocument
>
> ```xml
>  <property name="createDemoDocument">
>         <![CDATA[{"applicationName" : #[applicationName],"agentStarttime" : #[agentStarttime],"contentbody" : #[contentbody]}]]>
>     </property>
> ```
>
>  



## 6.8 批量操作

> ```java
> public void testBulkAddDateDocument() throws ParseException{
>    testGetmapping();
>    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
>    String date = format.format(new Date());
>    ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
>    List<Demo> demos = new ArrayList<>();
>    Demo demo = new Demo();
>    demo.setDemoId(2l);
>    demo.setAgentStarttime(new Date());
>    demo.setApplicationName("blackcatdemo2");
>    demo.setContentbody("this is content body2");
>    demos.add(demo);
> 
>    demo = new Demo();
>    demo.setDemoId(3l);
>    demo.setAgentStarttime(new Date());
>    demo.setApplicationName("blackcatdemo3");
>    demo.setContentbody("this is content body3");
>    demos.add(demo);
> 
>    //批量添加索引文档
>    String response = clientUtil.addDateDocuments("demo",//索引表
>          "demo",//索引类型
>          "createDemoDocument",//创建文档对应的脚本名称，在esmapper/estrace/ESTracesMapper.xml中配置
>          demos);
> 
>    System.out.println("addDateDocument-------------------------");
>    System.out.println(response);
> 
>    response = clientUtil.getDocument("demo-"+date,//索引表
>          "demo",//索引类型
>          "2");
>    System.out.println("getDocument-------------------------");
>    System.out.println(response);
> 
>    demo = clientUtil.getDocument("demo-"+date,//索引表
>          "demo",//索引类型
>          "3",//索引文档ID
>          Demo.class);
> }
> ```



## 6.9 管理类api演示

管理类api演示，以健康状态和集群状态为例进行说明，其他服务调整服务地址即可。更多的服务参考elasticsearch官方文档地址：[集群](https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster.html)  [Cat](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat.html)

```java
    @Test
	public void clusterHeathCheck(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		//返回json格式健康状态
		String heath = clientUtil.executeHttp("_cluster/health?pretty",ClientInterface.HTTP_GET);
		System.out.println(heath);

	}

	@Test
	public void clusterState(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		//返回json格式集群状态
		String state = clientUtil.executeHttp("_cluster/state?pretty",ClientInterface.HTTP_GET);
		System.out.println(state);

	}

	@Test
	public void clusterMapState(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		//返回map类型集群状态信息
		Map<String,Object> state = clientUtil.executeHttp("_cluster/state",ClientInterface.HTTP_GET,new MapResponseHandler());
		System.out.println(state);

	}
```



## 6.10 执行列子代码：[TestMain.java](https://github.com/bbossgroups/elasticsearch-gradle-example/blob/master/src/test/java/org/frameworkset/elasticsearch/TestMain.java)

> ```java
> public static void main(String[] args) throws ParseException {
>    ESTest esTest = new ESTest();
>    //测试模板管理功能
>    esTest.testTempate();
>    //重新创建模板
>    esTest.testCreateTempate();
>    //向当天的索引表中添加文档
>    esTest.testAddDateDocument();
>    //批量创建文档
>    esTest.testBulkAddDateDocument();
>    //获取索引映射结构
>    esTest.testGetmapping();
> }
> ```



## 6.11 一个聚合查询的案例

<https://esdoc.bbossgroups.com/#/agg>



## 6.12 suggest使用案例

<https://github.com/bbossgroups/elasticsearch-gradle-example/blob/master/src/test/java/org/frameworkset/elasticsearch/suggest>

<https://github.com/bbossgroups/elasticsearch-gradle-example/blob/master/src/main/resources/esmapper/estrace/suggest.xml>



## 6.13 Elasticsearch Mget、GetDocSource、索引部分字段更新案例

<https://esdoc.bbossgroups.com/#/Elasticsearch-Mget-GetDocSource-partupdate>



## 6.14 scroll分页检索案例

<https://esdoc.bbossgroups.com/#/scroll>

<https://esdoc.bbossgroups.com/#/Scroll-SliceScroll-api>



## 6.15 Elasticsearch地理位置维护及检索案例

<https://esdoc.bbossgroups.com/#/Elasticsearch-geo>



## 6.16 Elasticsearch 父子关系维护和检索案例

https://esdoc.bbossgroups.com/#/elasticsearch5-parent-child



## 6.17 Elasticsearch Delete/UpdateByquery案例

<https://esdoc.bbossgroups.com/#/update-delete-byquery>



# 7 相关资料



## **7.1 完整的demo**

**https://github.com/bbossgroups/eshelloword-booter**（基于maven）

<https://github.com/bbossgroups/eshelloword-spring-boot-starter>（基于maven）

<https://github.com/bbossgroups/elasticsearch-gradle-example> （基于gradle）



## **7.2 bboss elasticsearch特点**

<https://www.oschina.net/p/bboss-elastic>



## **7.3 elasticsearch dsl语法官方文档**

<https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html>

<https://www.elastic.co/guide/en/elasticsearch/reference/current/search.html>

<https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations.html>

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html

https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html

## 7.4 elasticsearch常用监控地址

elasticsearch常用监控地址：将ip地址调整为服务器ip即可

<http://192.168.137.1:9200/_nodes/http?pretty> 

<http://192.168.137.1:9200/_nodes/stats?pretty> 

<http://192.168.137.1:9200/_cluster/health?pretty> 

<http://192.168.137.1:9200/_cluster/stats?pretty>

http://192.168.137.1:9200/_settings?pretty

http://192.168.137.1:9200/_cat/indices?v


## **7.4 velocity官方文档：**

<http://velocity.apache.org/engine/1.7/user-guide.html>



## 7.5 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



