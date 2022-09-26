# bboss http负载均衡器使用指南

bboss http一个简单而功能强大的http/https负载均衡器模块，基于http/https协议实现客户端-服务端点到点的负载均衡和集群容灾功能，本文介绍其使用方法。

项目源码

https://github.com/bbossgroups/bboss-http

httpproxy 案例：基于apollo进行配置管理、节点自动发现、路由规则自动切换，源码地址

https://github.com/bbossgroups/httpproxy-apollo

# 1.负载均衡器特色

bboss http基于http/https协议实现客户端-服务端点到点的负载均衡和集群容灾功能，具有以下特色

```properties
1.服务负载均衡（目前提供RoundRobin负载算法）
2.服务健康检查
3.服务容灾故障恢复
4.服务自动发现（zk，etcd，consul，eureka，db，其他第三方注册中心）
5.本地地址清单与远程配置中心动态地址管理相结合，远程不可用时，采用本地地址
6.动态监听路由变化
7.分组服务管理
可以配置多组服务集群地址，每一组地址清单支持的配置格式：
http://ip:port
https://ip:port
ip:port（默认http协议）
多个地址用逗号分隔
8.服务安全认证（配置basic账号和口令）
9.服务采用http连接池
10.主备路由/异地灾备特色

 10.1.负载均衡器主备功能，如果主节点全部挂掉，请求转发到可用的备用节点，如果备用节点也挂了，就抛出异常，如果主节点恢复正常，那么请求重新发往主节点 
 10.2. 异地灾备，服务采用异地灾备模式部署，服务优先调用本地，当本地服务全部挂掉，服务请求转发到异地服务，如果本地服务部分恢复或者全部恢复，那么请求重新发往本地服务
11.提供丰富的rpc api方法

```

![](images/httpproxy5.jpg)


# 2.导入http负载均衡器

在工程中导入以下maven坐标即可

```xml
<dependency>
   <groupId>com.bbossgroups</groupId>
   <artifactId>bboss-http</artifactId>
   <version>6.0.6</version>
</dependency>
```

如果是gradle工程，导入方法如下：

```groovy
implementation 'com.bbossgroups:bboss-http:6.0.6'
```

# 3.负载均衡组件
![](images/httpproxy1.jpg)

![](images/httpproxy2.jpg)

![](images/httpproxy3.jpg)

```java
org.frameworkset.spi.remote.http.HttpRequestProxy
```

## 3.1 负载均衡组件API

### 3.1.1 初始化http proxy

HttpRequestProxy.startHttpPools(Map configs);  --通过在代码中设置参数，初始化httpproxy

HttpRequestProxy.startHttpPools(String configFile);--加载配置文件（classpath相对路径）中设置参数，初始化httpproxy

#### **1）加载配置文件启动示例**

```java
//加载配置文件，启动负载均衡器
HttpRequestProxy.startHttpPools("application.properties");
```

#### 2）加载Map属性配置启动负载均衡器示例

**简单的配置和启动**

```java
Map<String,Object> configs = new HashMap<String,Object>();

configs.put("http.health","/health.html");//health监控检查地址必须配置，否则将不会启动健康检查机制

//如果指定hosts那么就会采用配置的地址作为初始化地址清单
configs.put("http.hosts，","192.168.137.1:9200,192.168.137.2:9200,192.168.137.3:9200");

HttpRequestProxy.startHttpPools(configs);
```

**启动时指定服务发现机制**

```java
       Map<String,Object> configs = new HashMap<String,Object>();

      DemoHttpHostDiscover demoHttpHostDiscover = new DemoHttpHostDiscover();
      configs.put("http.discoverService",demoHttpHostDiscover);//设置服务发现组件


      configs.put("http.health","/health.html");//health监控检查地址必须配置，否则将不会启动健康检查机制
//如果指定hosts那么就会采用配置的地址作为初始化地址清单，后续通过discoverService服务发现的地址都会加入到清单中，去掉的服务也会从清单中剔除
configs.put("http.hosts，","192.168.137.1:9200,192.168.137.2:9200,192.168.137.3:9200");
 
      HttpRequestProxy.startHttpPools(configs);
```

#### **3）加载apollo配置启动httpproxy**

指定apollo命名空间和配置参数变化监听器（自定义）

```java
	/**
     * 从apollo加载配置启动http proxy：
	 * 配置了两个连接池：default,schedule
     * apollo namespace:  application
     * 服务地址变化发现监听器： org.frameworkset.http.client.AddressConfigChangeListener
	 */

	HttpRequestProxy.startHttpPoolsFromApollo("application","org.frameworkset.http.client.AddressConfigChangeListener");
```
指定apollo命名空间并监听服务节点及路由规则变化
```java
       /**
       * 1.服务健康检查
       * 2.服务负载均衡
       * 3.服务容灾故障恢复
       * 4.服务自动发现（apollo，zk，etcd，consul，eureka，db，其他第三方注册中心）
       * 配置了两个连接池：default,report
       * 本示例演示基于apollo提供配置管理、服务自动发现以及灰度/生产，主备切换功能
       */

  HttpRequestProxy.startHttpPoolsFromApolloAwaredChange("application");
```

### 3.1.2 调用服务API及示例

HttpRequestProxy.httpGetforString

HttpRequestProxy.httpXXX

HttpRequestProxy.sendXXX

提供了两套方法：一套方法是带服务组名称的方法，一套方法是不带服务组名称的方法（默认default服务组）

服务地址都是相对地址，例如：/testBBossIndexCrud，最终地址会被解析为

http://ip:port/testBBossIndexCrud 或者 https://ip:port/testBBossIndexCrud

默认服务组示例

```java
//以get方式发送请求
String data = HttpRequestProxy.httpGetforString("/testBBossIndexCrud");
//以get方式发送请求,将返回的json数据封装为AgentRule对象
AgentRule agentRule = HttpRequestProxy.httpGetforObject("/testBBossIndexCrud?id=1",AgentRule.class);
//以RequestBody方式，将params对象转换为json报文post方式推送到服务端，将相应json报文转换为AgentRule对象返回
AgentRule agentRule = HttpRequestProxy.sendJsonBody( params, "/testBBossIndexCrud",AgentRule.class);
//以post方式发送请求,将返回的json数据封装为AgentRule对象,方法第二个参数为保存请求参数的map对象
AgentRule data = HttpRequestProxy.httpPostForObject("/testBBossIndexCrud",(Map)null,AgentRule.class);
//以post方式发送请求,将返回的json数据封装为AgentRule对象List集合,方法第二个参数为保存请求参数的map对象
				List<AgentRule> datas = HttpRequestProxy.httpPostForList("/testBBossIndexCrud",(Map)null,AgentRule.class);
//以post方式发送请求,将返回的json数据封装为AgentRule对象Set集合,方法第二个参数为保存请求参数的map对象
				Set<AgentRule> dataSet = HttpRequestProxy.httpPostForSet("/testBBossIndexCrud",(Map)null,AgentRule.class);
//以post方式发送请求,将返回的json数据封装为AgentRule对象Map集合,方法第二个参数为保存请求参数的map对象
				Map<String,AgentRule> dataMap = HttpRequestProxy.httpPostForMap("/testBBossIndexCrud",(Map)null,String.class,AgentRule.class);
```

指定服务组示例

```java
String data = HttpRequestProxy.httpGetforString("report","/testBBossIndexCrud");
AgentRule agentRule = HttpRequestProxy.httpGetforObject("report","/testBBossIndexCrud",AgentRule.class);
AgentRule agentRule = HttpRequestProxy.sendJsonBody("report", params, "/testBBossIndexCrud",AgentRule.class);

AgentRule data = HttpRequestProxy.httpPostForObject("report","/testBBossIndexCrud",(Map)null,AgentRule.class);
				List<AgentRule> datas = HttpRequestProxy.httpPostForList("report","/testBBossIndexCrud",(Map)null,AgentRule.class);
				Set<AgentRule> dataSet = HttpRequestProxy.httpPostForSet("report","/testBBossIndexCrud",(Map)null,AgentRule.class);
				Map<String,AgentRule> dataMap = HttpRequestProxy.httpPostForMap("report","/testBBossIndexCrud",(Map)null,String.class,AgentRule.class);
```

## 3.2 http负载均衡器配置和启动

http负载均衡器配置非常简单，可以通过配置文件方式和代码方式对http负载均衡器进行配置

### 3.2.1 配置文件方式

在配置文件中添加以下内容-resources\application.properties

```properties
http.poolNames = default,schedule
##http连接池配置
http.timeoutConnection = 5000
http.timeoutSocket = 50000
http.connectionRequestTimeout=10000
http.retryTime = 0
http.maxLineLength = -1
http.maxHeaderCount = 200
http.maxTotal = 200
http.defaultMaxPerRoute = 100
http.soReuseAddress = false
http.soKeepAlive = false
http.timeToLive = 3600000
http.keepAlive = 3600000
http.keystore =
http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
http.hostnameVerifier =

# 服务代理配置
# 服务全认证账号配置
http.authAccount=elastic
http.authPassword=changeme
# ha proxy 集群负载均衡地址配置
http.hosts=192.168.137.1:808,192.168.137.1:809,192.168.137.1:810
# https服务必须带https://协议头
#http.hosts=https://192.168.137.1:808,https://192.168.137.1:809,https://192.168.137.1:810

# 健康检查服务
http.health=/health
# 健康检查定时时间间隔，单位：毫秒，默认3秒
http.healthCheckInterval=3000
# 服务地址自动发现功能
http.discoverService=org.frameworkset.http.client.DemoHttpHostDiscover
# 定时运行服务发现方法时间间隔，单位：毫秒，默认10秒
http.discoverService.interval=10000
##告警服务使用的http连接池配置
schedule.http.timeoutConnection = 5000
schedule.http.timeoutSocket = 50000
schedule.http.connectionRequestTimeout=10000
schedule.http.retryTime = 0
schedule.http.maxLineLength = -1
schedule.http.maxHeaderCount = 200
schedule.http.maxTotal = 200
schedule.http.defaultMaxPerRoute = 100
schedule.http.soReuseAddress = false
schedule.http.soKeepAlive = false
schedule.http.timeToLive = 3600000
schedule.http.keepAlive = 3600000
schedule.http.keystore =
schedule.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
schedule.http.hostnameVerifier =
# 告警服务使用服务代理配置
# 服务全认证账号配置
schedule.http.authAccount=elastic
schedule.http.authPassword=changeme
# ha proxy 集群负载均衡地址配置
schedule.http.hosts=192.168.137.1:808,192.168.137.1:809,192.168.137.1:810
# https服务必须带https://协议头
# schedule.http.hosts=https://192.168.137.1:808,https://192.168.137.1:809,https://192.168.137.1:810

# 健康检查服务
schedule.http.health=/health
# 健康检查定时时间间隔，单位：毫秒，默认3秒
schedule.http.healthCheckInterval=3000
# 服务地址自动发现功能
schedule.http.discoverService=org.frameworkset.http.client.DemoHttpHostDiscover
# 定时运行服务发现方法时间间隔，单位：毫秒，默认10秒
schedule.http.discoverService.interval=10000
```

上面配置了default和schedule两组服务配置，每组包含两部分内容：

- http连接池配置
- 服务负载均衡配置

http连接池配置这里不着重说明，只介绍服务负载均衡相关配置

```properties
# 服务代理配置
# 服务全认证账号和口令配置
http.authAccount=elastic
http.authPassword=changeme
# ha proxy 集群负载均衡地址配置，初始地址清单，
# 还可以通过http.discoverService动态发现新的负载地址、移除关停的负载地址，也可以不配置初始地址
# 这样初始地址完全由http.discoverService对应的服务发现功能来提供
http.hosts=192.168.137.1:808,192.168.137.1:809,192.168.137.1:810
# https服务必须带https://协议头
#http.hosts=https://192.168.137.1:808,https://192.168.137.1:809,https://192.168.137.1:810
# 健康检查服务，服务端提供的一个监控服务检查地址，当服务节点不可用时，就会启动健康检查,根据healthCheckInterval参数，按一定的时间间隔探测health对应的服务是否正常，如果正常，那么服务即可用，健康检查线程停止（直到服务不可用时，再次启动检查机制），否则继续监测
http.health=/health
# 健康检查定时时间间隔，单位：毫秒，默认3秒
http.healthCheckInterval=3000
# 服务地址自动发现功能，必须继承抽象类org.frameworkset.spi.remote.http.proxy.HttpHostDiscover
# 实现抽象方法discover
http.discoverService=org.frameworkset.http.client.DemoHttpHostDiscover
```

 org.frameworkset.http.client.DemoHttpHostDiscover的实现如下：

```java
package org.frameworkset.http.client;
 

import org.frameworkset.spi.assemble.GetProperties;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.frameworkset.spi.remote.http.HttpHost;
import org.frameworkset.spi.remote.http.proxy.HttpHostDiscover;
import org.frameworkset.spi.remote.http.proxy.HttpServiceHostsConfig;

import java.util.ArrayList;
import java.util.List;


public class DemoHttpHostDiscover extends HttpHostDiscover {
   private int count = 0;
   @Override
   protected List<HttpHost> discover(HttpServiceHostsConfig httpServiceHostsConfig,
                             ClientConfiguration configuration,
                             GetProperties context) {
	  //直接构造并返回三个服务地址的列表对象
      List<HttpHost> hosts = new ArrayList<HttpHost>();
       // https服务必须带https://协议头,例如https://192.168.137.1:808
      HttpHost host = new HttpHost("192.168.137.1:808");
      hosts.add(host);
      if(count != 2) {//模拟添加和去除节点
         host = new HttpHost("192.168.137.1:809");
         hosts.add(host);
      }
      else{
         System.out.println("aa");
      }
      host = new HttpHost("192.168.137.1:810");
      hosts.add(host);
      count ++;
      return hosts;
   }
    /**
	 * 返回null或者false，忽略对返回的null或者空的hosts进行处理；
	 * 返回true，要对null或者空的hosts进行处理，这样会导致所有的地址不可用
	 *
	 * @return 默认返回null
	 */
	protected Boolean handleNullOrEmptyHostsByDiscovery(){
		return null;
	}
}
```

### 3.2.2 加载配置文件启动负载均衡器

```java
HttpRequestProxy.startHttpPools("application.properties");
```

### 3.2.3 代码方式配置和启动负载均衡器

#### 单集群

配置和启动      

```java
 Map<String,Object> configs = new HashMap<String,Object>();
 configs.put("http.health","/health");//health监控检查地址必须配置，否则将不会启动健康检查机制 

DemoHttpHostDiscover demoHttpHostDiscover = new DemoHttpHostDiscover();
  configs.put("http.discoverService",demoHttpHostDiscover);//注册服务发现机制，服务自动发现（zk，etcd，consul，eureka，db，其他第三方注册中心）

 
 //启动负载均衡器
  HttpRequestProxy.startHttpPools(configs);
```

服务调用示例

```java
 String data = HttpRequestProxy.httpGetforString("/testBBossIndexCrud");//获取字符串报文
Map data = HttpRequestProxy.httpGetforObject("/testBBossIndexCrud",Map.class);//获取对象数据
```

#### 多集群

配置和启动：两个集群default,report

```java
/**
       * 1.服务健康检查
       * 2.服务负载均衡
       * 3.服务容灾故障恢复
       * 4.服务自动发现（zk，etcd，consul，eureka，db，其他第三方注册中心）
       * 配置了两个服务集群组：default,report
       */
      Map<String,Object> configs = new HashMap<String,Object>();
      configs.put("http.poolNames","default,report");
	//default组配置	
      configs.put("http.health","/health");//health监控检查地址必须配置，否则将不会启动健康检查机制

      DemoHttpHostDiscover demoHttpHostDiscover = new DemoHttpHostDiscover();
      configs.put("http.discoverService",demoHttpHostDiscover);

      //report组配置
      configs.put("report.http.health","/health");//health监控检查地址必须配置，否则将不会启动健康检查机制

      configs.put("report.http.discoverService","org.frameworkset.http.client.DemoHttpHostDiscover");
     //启动负载均衡器
      HttpRequestProxy.startHttpPools(configs);
```

服务调用

```java
 String data = HttpRequestProxy.httpGetforString("/testBBossIndexCrud");//在default集群上执行请求，无需指定集群名称

 String data = HttpRequestProxy.httpGetforString("report","/testBBossIndexCrud");//在report集群上执行请求
```

### 3.2.4 配置参数中使用环境变量

可以在配置参数中使用环境变量，环境变量引用方式：

简单方式

http.routing=#[area]

混合方式

http.routing=#[area]dddd#[sadfasdf]

需要在os(linux,unix,windows)中配置名称为area和sadfasdf的环境变量。

以http.routing为例进行说明。

为了避免配置文件混乱，可以将http.routing对应的值做成环境变量，配置文件中只需要引用环境变量名称即可：

```properties
http.routing=#[area]
#bboss支持混合模式的变量和常量拼接，例如
#http.routing=#[county]aaac#[area]
```

这样我们在os(linux,unix,windows)中配置名称为area的环境变量即可。

### 3.2.5 spring boot配置和使用http proxy

示例工程源码获取地址：https://github.com/bbossgroups/bestpractice/tree/master/springboot-starter

首先需要在工程中导入bboss spring boot starter的maven坐标：

```xml
<dependency>
  <groupId>com.bbossgroups</groupId>
  <artifactId>bboss-spring-boot-starter</artifactId>
  <version>6.0.5</version>
</dependency>
```

#### 3.2.5.1 单http服务池

在spring boot配置文件[application.properties](https://github.com/bbossgroups/bestpractice/tree/master/springboot-starter/resources/application.properties)中添加http proxy的配置参数

```properties
##es client http服务配置
spring.bboss.http.name=default
spring.bboss.http.timeoutConnection = 5000
spring.bboss.http.timeoutSocket = 5000
spring.bboss.http.connectionRequestTimeout=5000
spring.bboss.http.retryTime = 1
spring.bboss.http.maxLineLength = -1
spring.bboss.http.maxHeaderCount = 200
spring.bboss.http.maxTotal = 400
spring.bboss.http.defaultMaxPerRoute = 200
spring.bboss.http.soReuseAddress = false
spring.bboss.http.soKeepAlive = false
spring.bboss.http.timeToLive = 3600000
spring.bboss.http.keepAlive = 3600000
spring.bboss.http.keystore =
spring.bboss.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
spring.bboss.http.hostnameVerifier =
#每隔多少毫秒校验空闲connection，自动释放无效链接
# -1 或者0不检查
spring.bboss.http.validateAfterInactivity=2000
# 每次获取connection时校验连接，true，校验，false不校验，有性能开销，推荐采用
# validateAfterInactivity来控制连接是否有效
# 默认值false
spring.bboss.http.staleConnectionCheckEnabled=false
#* 自定义重试控制接口，必须实现接口方法
#* public interface CustomHttpRequestRetryHandler  {
#* 	public boolean retryRequest(IOException exception, int executionCount, HttpContext context,ClientConfiguration configuration);
#* }
#* 方法返回true，进行重试，false不重试
spring.bboss.http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.ConnectionResetHttpRequestRetryHandler

# 服务代理配置
# 服务全认证账号配置

spring.bboss.http.authAccount=elastic
spring.bboss.http.authPassword=changeme
# ha proxy 集群负载均衡地址配置
#spring.bboss.http.hosts=192.168.137.1:808,192.168.137.1:809,192.168.137.1:810
spring.bboss.http.hosts=192.168.137.1:9200
# 健康检查服务
spring.bboss.http.health=/
spring.bboss.http.healthCheckInterval=1000
# 服务地址自动发现功能
#spring.bboss.http.discoverService.serviceClass=com.test.DiscoverService
# 定时运行服务发现方法时间间隔，单位：毫秒，默认10秒
spring.bboss.http.discoverService.interval=10000

# handleNullOrEmptyHostsByDiscovery
#false，忽略对返回的null或者空的hosts进行处理
#true，要对null或者空的hosts进行处理，这样会导致所有的地址不可用
http.discoverService.handleNullOrEmptyHostsByDiscovery=false
```

服务调用示例

```java
/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frameworkset.sqlexecutor;


import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.spi.boot.BBossStarter;
import org.frameworkset.spi.remote.http.HttpRequestProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * testCase必须和spring boot application启动类在同一个包路径下面，否则会报错：
 * java.lang.IllegalStateException: Unable to find a @SpringBootConfiguration, you need to use @Context

 * @author yinbp [122054810@qq.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest

public class BBossStarterTestCase {
	@Autowired
	private BBossStarter bbossStarterDefault;
	private static Logger logger = LoggerFactory.getLogger(BBossStarterTestCase.class);
    @Test
    public void testMultiBBossESStarterDefault() throws Exception {
		Map params = new HashMap();
		params.put("testp","aaaaa");
		params.put("dff","zzzz");
		List<Map> datas = HttpRequestProxy.httpPostForList("default","/demoproject/file/getUserInfo.page",params,Map.class);
		logger.info(SimpleStringUtil.object2json(datas));
    }


	@Test
	public void testFirstQuery() throws Exception {

		List<Map> datas = SQLExecutor.queryListWithDBName(Map.class,"default","select * from user");
		logger.info(SimpleStringUtil.object2json(datas));
	}

}

```

关键代码，需要在组件中导入starter组件：

```java
	@Autowired
	private BBossStarter bbossStarterDefault;
```


#### 3.2.5.2 多http服务池配置

在spring boot配置文件[application-multi.properties](https://github.com/bbossgroups/bestpractice/tree/master/springboot-starter/resources/application-multi.properties)中添加http proxy的配置参数

```properties
##es client http服务配置
spring.bboss.default.http.name=default
spring.bboss.default.http.timeoutConnection = 5000
spring.bboss.default.http.timeoutSocket = 5000
spring.bboss.default.http.connectionRequestTimeout=5000
spring.bboss.default.http.retryTime = 1
spring.bboss.default.http.maxLineLength = -1
spring.bboss.default.http.maxHeaderCount = 200
spring.bboss.default.http.maxTotal = 400
spring.bboss.default.http.defaultMaxPerRoute = 200
spring.bboss.default.http.soReuseAddress = false
spring.bboss.default.http.soKeepAlive = false
spring.bboss.default.http.timeToLive = 3600000
spring.bboss.default.http.keepAlive = 3600000
spring.bboss.default.http.keystore =
spring.bboss.default.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
spring.bboss.default.http.hostnameVerifier =
#每隔多少毫秒校验空闲connection，自动释放无效链接
# -1 或者0不检查
spring.bboss.default.http.validateAfterInactivity=2000
# 每次获取connection时校验连接，true，校验，false不校验，有性能开销，推荐采用
# validateAfterInactivity来控制连接是否有效
# 默认值false
spring.bboss.default.http.staleConnectionCheckEnabled=false
#* 自定义重试控制接口，必须实现接口方法
#* public interface CustomHttpRequestRetryHandler  {
#* 	public boolean retryRequest(IOException exception, int executionCount, HttpContext context,ClientConfiguration configuration);
#* }
#* 方法返回true，进行重试，false不重试
spring.bboss.default.http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.ConnectionResetHttpRequestRetryHandler

# 服务代理配置
# 服务全认证账号配置

spring.bboss.default.http.authAccount=elastic
spring.bboss.default.http.authPassword=changeme
# ha proxy 集群负载均衡地址配置
#spring.bboss.default.http.hosts=192.168.137.1:808,192.168.137.1:809,192.168.137.1:810
spring.bboss.default.http.hosts=10.13.11.117:8082
# 健康检查服务
spring.bboss.default.http.health=/
spring.bboss.default.http.healthCheckInterval=1000
# 服务地址自动发现功能
#spring.bboss.default.http.discoverService.serviceClass=com.test.DiscoverService
# 定时运行服务发现方法时间间隔，单位：毫秒，默认10秒
spring.bboss.default.http.discoverService.interval=10000

# handleNullOrEmptyHostsByDiscovery
#false，忽略对返回的null或者空的hosts进行处理
#true，要对null或者空的hosts进行处理，这样会导致所有的地址不可用
spring.bboss.default.http.discoverService.handleNullOrEmptyHostsByDiscovery=false

# 数据库数据源配置
spring.bboss.default.db.name = firstds
spring.bboss.default.db.user = root
spring.bboss.default.db.password = 123456
spring.bboss.default.db.driver = com.mysql.jdbc.Driver
spring.bboss.default.db.url = jdbc:mysql://10.13.11.117:3306/mysql
spring.bboss.default.db.usePool = true
spring.bboss.default.db.validateSQL = select 1




##second es client http服务配置
spring.bboss.second.http.name=second
spring.bboss.second.http.timeoutConnection = 5000
spring.bboss.second.http.timeoutSocket = 5000
spring.bboss.second.http.connectionRequestTimeout=5000
spring.bboss.second.http.retryTime = 1
spring.bboss.second.http.maxLineLength = -1
spring.bboss.second.http.maxHeaderCount = 200
spring.bboss.second.http.maxTotal = 400
spring.bboss.second.http.secondMaxPerRoute = 200
spring.bboss.second.http.soReuseAddress = false
spring.bboss.second.http.soKeepAlive = false
spring.bboss.second.http.timeToLive = 3600000
spring.bboss.second.http.keepAlive = 3600000
spring.bboss.second.http.keystore =
spring.bboss.second.http.keyPassword =
# ssl 主机名称校验，是否采用second配置，
# 如果指定为second，就采用secondHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
spring.bboss.second.http.hostnameVerifier =
#每隔多少毫秒校验空闲connection，自动释放无效链接
# -1 或者0不检查
spring.bboss.second.http.validateAfterInactivity=2000
# 每次获取connection时校验连接，true，校验，false不校验，有性能开销，推荐采用
# validateAfterInactivity来控制连接是否有效
# 默认值false
spring.bboss.second.http.staleConnectionCheckEnabled=false
#* 自定义重试控制接口，必须实现接口方法
#* public interface CustomHttpRequestRetryHandler  {
#* 	public boolean retryRequest(IOException exception, int executionCount, HttpContext context,ClientConfiguration configuration);
#* }
#* 方法返回true，进行重试，false不重试
spring.bboss.second.http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.ConnectionResetHttpRequestRetryHandler

# 服务代理配置
# 服务全认证账号配置

spring.bboss.second.http.authAccount=elastic
spring.bboss.second.http.authPassword=changeme
# ha proxy 集群负载均衡地址配置
#spring.bboss.second.http.hosts=192.168.137.1:808,192.168.137.1:809,192.168.137.1:810
spring.bboss.second.http.hosts=10.13.11.117:8082
# 健康检查服务
spring.bboss.second.http.health=/
spring.bboss.second.http.healthCheckInterval=1000
# 服务地址自动发现功能
#spring.bboss.second.http.discoverService.serviceClass=com.test.DiscoverService
# 定时运行服务发现方法时间间隔，单位：毫秒，默认10秒
spring.bboss.second.http.discoverService.interval=10000

# handleNullOrEmptyHostsByDiscovery
#false，忽略对返回的null或者空的hosts进行处理
#true，要对null或者空的hosts进行处理，这样会导致所有的地址不可用
spring.bboss.second.http.discoverService.handleNullOrEmptyHostsByDiscovery=false

# 数据库数据源配置
spring.bboss.second.db.name = secondds
spring.bboss.second.db.user = root
spring.bboss.second.db.password = 123456
spring.bboss.second.db.driver = com.mysql.jdbc.Driver
spring.bboss.second.db.url = jdbc:mysql://10.13.11.117:3306/mysql
spring.bboss.second.db.usePool = true
spring.bboss.second.db.validateSQL = select 1


```

服务调用实例：

```java
/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frameworkset.sqlexecutor;


import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.spi.boot.BBossStarter;
import org.frameworkset.spi.remote.http.HttpRequestProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * testCase必须和spring boot application启动类在同一个包路径下面，否则会报错：
 * java.lang.IllegalStateException: Unable to find a @SpringBootConfiguration, you need to use @Context
 * 多集群演示功能测试用例，spring boot配置项以spring.bboss.集群名称开头，例如：
 * spring.bboss.db.firstds
 * spring.bboss.db.secondds
 *  spring.bboss.http.default
 *  spring.bboss.http.second
 * 两个集群通过  com.frameworkset.sqlexecutor.MultiSTartConfigurer
 * 对应的配置文件为application-multi.properties文件
 * @author yinbp [122054810@qq.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest

@ActiveProfiles("multi")
public class MultiBBossStartersTestCase {
	@Autowired
	private BBossStarter bbossStarterDefault;
	private static Logger logger = LoggerFactory.getLogger(MultiBBossStartersTestCase.class);
    @Test
    public void testMultiBBossESStarterDefault() throws Exception {
		Map params = new HashMap();
		params.put("testp","aaaaa");
		params.put("dff","zzzz");
		List<Map> datas = HttpRequestProxy.httpPostForList("default","/demoproject/file/getUserInfo.page",params,Map.class);
		logger.info(SimpleStringUtil.object2json(datas));
    }

	@Test
	public void testMultiBBossESStarterSecond() throws Exception {
		Map params = new HashMap();
		params.put("testp","aaaaa");
		params.put("dff","zzzz");
		List<Map> datas = HttpRequestProxy.sendJsonBodyForList("second",params,"/demoproject/file/getUserInfo.page",Map.class);
		logger.info(SimpleStringUtil.object2json(datas));
	}

	@Test
	public void testFirstQuery() throws Exception {

		List<Map> datas = SQLExecutor.queryListWithDBName(Map.class,"firstds","select * from user");
		logger.info(SimpleStringUtil.object2json(datas));
	}

	@Test
	public void testSecondQuery() throws Exception {

		List<Map> datas = SQLExecutor.queryListWithDBName(Map.class,"secondds","select * from user");
		logger.info(SimpleStringUtil.object2json(datas));
	}
}

```

关键代码，需要在组件中导入starter组件：

```java
	@Autowired
	private BBossStarter bbossStarterDefault;
```

### 3.2.6 failAllContinue配置
failAllContinue配置
如果所有节点都被标记为不可用时，可以通过控制开关设置返回故障节点用于处理请求，如果请求能够被正常处理则将节点标记为正常节点 默认值true 非spring boot项目配置

     http.failAllContinue = true
spring boot配置项

     spring.bboss.http.failAllContinue = true

# 4 使用负载均衡器调用服务

HttpRequestProxy组件提供了非常丰富的服务调用API，可以去查看对应的api了解。

## 4.1 一个简单的案例

使用负载均衡器调用服务，在指定服务集群组report调用rest服务/testBBossIndexCrud,返回json字符串报文，通过循环调用，测试负载均衡机制

```java
@Test
   public void testGet(){
      String data = HttpRequestProxy.httpGetforString("report","/testBBossIndexCrud");
      System.out.println(data);
      do {
         try {
            data = HttpRequestProxy.httpGetforString("report","/testBBossIndexCrud");
         } catch (Exception e) {
            e.printStackTrace();
         }
         try {
            Thread.sleep(3000l);
         } catch (Exception e) {
            break;
         }
         try {
            data = HttpRequestProxy.httpGetforString("report","/testBBossIndexCrud");
         } catch (Exception e) {
            e.printStackTrace();
         }
         try {
            data = HttpRequestProxy.httpGetforString("report","/testBBossIndexCrud");
         } catch (Exception e) {
            e.printStackTrace();
         }
//       break;
      }
      while(true);
   }
```



## 4.2 返回简单结果对象或者 基本数据类型

```java
@ResponseBody
public Map<String, Object> queryUrl(String isParam,String url ) {

    Map<String, Object> ajaxData = new HashMap<>();
    ajaxData.put(HNanConstant.resultCode, HNanConstant.SUCCESS);
    ajaxData.put(HNanConstant.resultInfo, "");
    Map params = new HashMap();
    params.put("isParam",isParam);
    params.put("url",url);
    try {
        //返回简单结果对象或者 基本数据类型
        String data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", String.class);//返回String
        int data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", int.class);//返回int
                short data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", short.class);//返回short
                long data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", long.class);//返回long
                float data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", float.class);//返回float
                double data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", double.class);//返回double
                boolean data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", boolean.class);//返回boolean
        Map data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", Map.class);//返回map对象
        PoBean data = HttpRequestProxy.sendJsonBody(params,  "/visualops/webpage/queryUrl.api", PoBean.class);//返回普通java对象
        if(data != null){
            ajaxData.put("data", data);
        }
        else {
            ajaxData.put("data", url);
        }
    } catch (Exception e) {
        log.error("queryUrl error", e);
        ajaxData.put(HNanConstant.resultCode, HNanConstant.FAIL);
    }
    return ajaxData;

}
```

## 4.3 返回集合对象

可以非常方便地返回List，Set，Map集合对象

```java
public @ResponseBody
Map<String, Object> queryErrorMessage(HttpServletRequest request) {
    String param = request.getParameter("data");
    Map<String, Object> ajaxData = new HashMap<>();
    ajaxData.put(HNanConstant.resultCode, HNanConstant.SUCCESS);
    ajaxData.put(HNanConstant.resultInfo, "");
    try {
       List<ErrorInfo> result = HttpRequestProxy.sendJsonBodyForList(param, "/visualops/webpage/queryErrorMessage.api", ErrorInfo.class);
            Set<ErrorInfo> set = HttpRequestProxy.sendJsonBodyForSet(param, "/visualops/webpage/queryErrorMessage.api", ErrorInfo.class);
            Map<String,ErrorInfo> map = HttpRequestProxy.sendJsonBodyForMap(param, "/visualops/webpage/queryErrorMessage.api", String.class,ErrorInfo.class);
        ajaxData.put("data", result);
    } catch (Exception e) {
        log.error("queryErrorMessage error", e);
        ajaxData.put(HNanConstant.resultCode, HNanConstant.FAIL);
    }
    return ajaxData;
}
```

总之，通过HttpRequestProxy调用服务就像我们调用本地类方法一样，支持post、get、put、delete、head http请求处理方法，提供or mapping机制，可以根据实际需要使用相关api即可。

# 5.服务发现机制的两种工作模式

本文开头介绍了http负载均衡器服务发现支持从各种数据源获取和发现服务地址：

zookeeper，etcd，consul，eureka，db，其他第三方注册中心

为了支持第三方注册中心，服务发现机制的提供两种工作模式：

## 5.1 主动发现模式

bboss通过调用http.discoverService配置的服务发现方法，定时从数据库和注册中心中查询最新的服务地址数据清单，本文上面介绍的http.discoverService就是一种主动定时发现模式

## 5.2 被动发现模式

监听apollo，zookeeper，etcd，consul，eureka等配置中心中管理的服务节点地址清单数据变化，监听路由规则变化，更新本地服务器地址清单，适用于发布订阅模式,对应的api

```java
//当路由规则发生变化时，可以调用下面的api更新
HttpProxyUtil.changeRouting(String poolName,String newCurrentRounte) //切换服务组poolName对应的路由规则
//当服务节点发生变化时，可以调用下面的api更新
public static void handleDiscoverHosts(String poolName, List<HttpHost> hosts) //更新服务节点

//当路由规则和服务节点同时发生变化时，调用下面的api更新
	/**
	 *
	 * @param poolName 服务组名称
	 * @param hosts 新的主机节点信息
	 * @param newCurrentRounte 新的路由组
	 */
	public static void handleDiscoverHosts(String poolName, List<HttpHost> hosts,String newCurrentRounte)
```



被动发现模式示例代码如下：

```java
//模拟被动获取监听地址清单
List<HttpHost> hosts = new ArrayList<HttpHost>();
// https服务必须带https://协议头,例如https://192.168.137.1:808
HttpHost host = new HttpHost("192.168.137.1:808");
hosts.add(host);

   host = new HttpHost("192.168.137.1:809");
   hosts.add(host);

host = new HttpHost("192.168.137.1:810");
hosts.add(host);
//将被动获取到的地址清单加入服务地址组report中，动态节点发现
HttpProxyUtil.handleDiscoverHosts("report",hosts);

//动态路由切换

HttpProxyUtil.changeRouting(String poolName,String newCurrentRounte)
```
## 5.3 基于apollo配置中心被动发现模式示例
首先定义一个apollo 监听器
```java
package org.frameworkset.http.client;


import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.frameworkset.spi.remote.http.HttpHost;
import org.frameworkset.spi.remote.http.proxy.HttpProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2020/8/2 20:07
 * @author biaoping.yin
 * @version 1.0
 */
public class AddressConfigChangeListener implements ConfigChangeListener {
	private static Logger logger = LoggerFactory.getLogger(AddressConfigChangeListener.class);
	private void handleDiscoverHosts(String _hosts,String poolName){
		if(_hosts != null && !_hosts.equals("")){
			String[] hosts = _hosts.split(",");
			List<HttpHost> httpHosts = new ArrayList<HttpHost>();
			HttpHost host = null;
			for(int i = 0; i < hosts.length; i ++){
				host = new HttpHost(hosts[i]);
				httpHosts.add(host);
			}
			//将被动获取到的地址清单加入服务地址组report中
			HttpProxyUtil.handleDiscoverHosts(poolName,httpHosts);
		}
	}
	/**
	 * //模拟被动获取监听地址清单
	 * List<HttpHost> hosts = new ArrayList<HttpHost>();
	 * // https服务必须带https://协议头,例如https://192.168.137.1:808
	 * HttpHost host = new HttpHost("192.168.137.1:808");
	 * hosts.add(host);
	 *
	 *    host = new HttpHost("192.168.137.1:809");
	 *    hosts.add(host);
	 *
	 * host = new HttpHost("192.168.137.1:810");
	 * hosts.add(host);
	 * //将被动获取到的地址清单加入服务地址组report中
	 * HttpProxyUtil.handleDiscoverHosts("schedule",hosts);
	 */
	public void onChange(ConfigChangeEvent changeEvent) {
		logger.info("Changes for namespace {}", changeEvent.getNamespace());
		ConfigChange change = null;
		for (String key : changeEvent.changedKeys()) {
			if(key.equals("schedule.http.hosts")){//schedule集群
				change = changeEvent.getChange(key);
				logger.info("Found change - key: {}, oldValue: {}, newValue: {}, changeType: {}", change.getPropertyName(), change.getOldValue(), change.getNewValue(), change.getChangeType());
				if(change.getChangeType() == PropertyChangeType.MODIFIED) {
					String _hosts = change.getNewValue();
					handleDiscoverHosts(_hosts, "schedule");
				}

			}
			else if(key.equals("http.hosts")){//default集群
				change = changeEvent.getChange(key);
				logger.info("Found change - key: {}, oldValue: {}, newValue: {}, changeType: {}", change.getPropertyName(), change.getOldValue(), change.getNewValue(), change.getChangeType());
				if(change.getChangeType() == PropertyChangeType.MODIFIED) {
					String _hosts = change.getNewValue();
					handleDiscoverHosts(_hosts, "default");
				}


			}

		}
	}
}
```
从apollo加载配置启动http proxy：
```java
    @Before
	public void startPool(){
//		HttpRequestProxy.startHttpPools("application.properties");
		/**
         * 从apollo加载配置启动http proxy：
		 * 配置了两个连接池：default,schedule
         * apollo namespace:  application
         * 服务地址变化发现监听器： org.frameworkset.http.client.AddressConfigChangeListener
		 */

		HttpRequestProxy.startHttpPoolsFromApollo("application","org.frameworkset.http.client.AddressConfigChangeListener");
	}
	@Test
	public void testGet(){
		String data = null;
		try {
            //服务调用
			data = HttpRequestProxy.httpGetforString("schedule", "/testBBossIndexCrud");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		System.out.println(data);
		do {
			try {
				data = HttpRequestProxy.httpGetforString("schedule","/testBBossIndexCrud");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(3000l);
			} catch (Exception e) {
				break;
			}

		}
		while(true);
	}
```
Apollo配置使用参考文档：<https://esdoc.bbossgroups.com/#/apollo-config>

直接调用http proxy api监听路由变化和节点变化

```java
       /**
       * 1.服务健康检查
       * 2.服务负载均衡
       * 3.服务容灾故障恢复
       * 4.服务自动发现（apollo，zk，etcd，consul，eureka，db，其他第三方注册中心）
       * 配置了两个连接池：default,report
       * 本示例演示基于apollo提供配置管理、服务自动发现以及灰度/生产，主备切换功能
       */


      HttpRequestProxy.startHttpPoolsFromApolloAwaredChange("application");
```
# 6.主备和异地灾备配置和服务发现

主备和异地灾备配置和服务发现

地址格式配置，其中routing标识服务地址或者路由标记

ip:port|routing

例如：

```properties
#指定了每个地址对应的地区信息，可以按照地区信息进行路由
http.hosts=192.168.137.1:808|beijing,192.168.137.1:809|beijing,192.168.137.1:810|shanghai
```

指定beijing地区路由信息或者主节点标识信息

```properties
# 指定本地区信息，系统按地区部署时，指定地区信息，
# 不同的地区请求只路由到本地区（beijing）对应的服务器，shanghai的服务器作为backup服务器，
# 当本地(beijing)的服务器都不可用时，才将请求转发到可用的上海服务器
http.routing=beijing
```

指定shanghai地区路由信息或者主节点标识信息

```properties
# 指定本地区信息，系统按地区部署时，指定地区信息，
# 不同的地区请求只路由到本地区（shanghai）对应的服务器，beijing的服务器作为backup服务器，
# 当本地(shanghai)的服务器都不可用时，才将请求转发到可用的beijing服务器
http.routing=shanghai
```

为了避免配置文件混乱，可以将http.routing对应的值做成环境变量，配置文件中只需要引用环境变量名称即可：

```properties
http.routing=#[area]
#bboss支持混合模式的变量和常量拼接，例如
#http.routing=#[county]aaac#[area]
```

这样我们在os(linux,unix,windows)中配置名称为area的环境变量即可。

带路由信息的服务发现机制：可以动态变化服务地址的routing信息

```java
package org.frameworkset.http.client;


import org.frameworkset.spi.assemble.GetProperties;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.frameworkset.spi.remote.http.HttpHost;
import org.frameworkset.spi.remote.http.proxy.HttpHostDiscover;
import org.frameworkset.spi.remote.http.proxy.HttpServiceHostsConfig;

import java.util.ArrayList;
import java.util.List;

public class DemoHttpHostDiscover extends HttpHostDiscover {
	private int count = 0;
	@Override
	protected List<HttpHost> discover(HttpServiceHostsConfig httpServiceHostsConfig,
									  ClientConfiguration configuration,
									  GetProperties context) {

		List<HttpHost> hosts = new ArrayList<HttpHost>();
		HttpHost host = new HttpHost("192.168.137.1:808|beijing");
		hosts.add(host);
		if(count != 2) {//模拟添加和去除节点
			host = new HttpHost("192.168.137.1:809|beijing");
			hosts.add(host);
		}
		else{
			System.out.println("aa");
		}
        //可以动态变化服务地址的routing信息，模拟改变路由信息
		if(count > 10 && count < 15) {
			host = new HttpHost("192.168.137.1:810|beijing");
		}
		else{
			host = new HttpHost("192.168.137.1:810|shanghai");
		}
		hosts.add(host);
		count ++;
		return hosts;
	}
    /**
	 * 返回null或者false，忽略对返回的null或者空的hosts进行处理；
	 * 返回true，要对null或者空的hosts进行处理，这样会导致所有的地址不可用
	 *
	 * @return 默认返回null
	 */
	protected Boolean handleNullOrEmptyHostsByDiscovery(){
		return null;
	}
}

```

# 7.健康检查服务

可以通过http.health属性指定健康检查服务，服务为相对地址，不需要指定ip和端口，例如：

- 设置默认集群组健康服务

```java
configs.put("http.health","/health.html");//health监控检查地址必须配置，否则将不会启动健康检查机
```

- 设置特定集群组健康服务

```java
configs.put("report.http.health","/health.html");//health监控检查地址必须配置，否则将不会启动健康检查机
```
- 健康检查服务开启条件
http.healthCheckInterval必须大于0，单位毫秒，默认值-1,设置示例：
http.healthCheckInterval=1000
同时必须设置http.health健康检查服务，例如：
http.health=/health.html

**bboss以get方式发送http.health对应的健康检查服务请求，健康检查服务只需要响应状态码为200-300即认为服务节点健康可用**。

# 8.安全认证

bboss支持http basic认证机制，亦可以通过添加header参数实现jwt token认证机制。

http basic认证机制设置

```properties
# 服务全认证账号配置
schedule.http.authAccount=elastic
schedule.http.authPassword=changeme
```

jwt token认证设置，直接申请jwt token，并经token设置到http header中，模拟token有效期管理的组件(可以根据实际场景调整)：[TokenManger](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/TokenManager.java)

```java
//判断服务token是否过期，如果过期则需要重新调用token服务申请token
			TokenInfo tokenInfo = tokenManager.getTokenInfo();
			String token = "Bearer " + tokenInfo.getAccess_token();//"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZWZhdWx0XzYxNTE4YjlmM2UyYmM3LjEzMDI5OTkxIiwiaWF0IjoxNjMyNzM0MTExLCJuYmYiOjE2MzI3MzQxMTEsImV4cCI6MTYzMjc0MTMxMSwiZGV2aWNlX2lkIjoiYXBwMDMwMDAwMDAwMDAwMSIsImFwcF9pZCI6ImFwcDAzIiwidXVpZCI6ImFkZmRhZmFkZmFkc2ZlMzQxMzJmZHNhZHNmYWRzZiIsInNlY3JldCI6ImFwcDAzMVEyVzNFd29ybGQxMzU3OVBhc3NBU0RGIiwiaXNzdWVfdGltZSI6MTYzMjczNDExMSwiand0X3NjZW5lIjoiZGVmYXVsdCJ9.mSl-JBUV7gTUapn9yV-VLfoU7dm-gxC7pON62DnD-9c";
			Map<String,String> headers = new LinkedHashMap<>();
			
			headers.put("Authorization",token);
			Map datas = HttpRequestProxy.sendJsonBody("jwtservice",params,"/api/queryUsers.api", headers,Map.class);
```

ssl证书配置，参考文档：[设置ssl证书](https://esdoc.bbossgroups.com/#/development?id=_265-https%e5%8d%8f%e8%ae%ae%e9%85%8d%e7%bd%ae)



# 9.开发交流



bboss http交流QQ群：21220580,166471282

**bboss http微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



