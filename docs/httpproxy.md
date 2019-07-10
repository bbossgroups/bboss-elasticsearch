# bboss http负载均衡器使用指南

bboss http一个简单而功能强大的http负载均衡器模块，基于http协议实现客户端点到点的负载均衡和集群容灾功能，本文介绍其使用方法。

项目源码
https://github.com/bbossgroups/bboss-http

# 1.负载均衡器特色

bboss http基于http协议实现客户端点到点的负载均衡和集群容灾功能，具有以下特色

```properties
1.服务负载均衡（目前提供RoundRobin负载算法）
2.服务健康检查
3.服务容灾故障恢复
4.服务自动发现（zk，etcd，consul，eureka，db，其他第三方注册中心）
5.分组服务管理
可以配置多组服务集群地址，每一组地址清单支持的配置格式：
http://ip:port
https://ip:port
ip:port（默认http协议）
多个地址用逗号分隔
6.服务安全认证（配置basic账号和口令）
7.主备路由/异地灾备特色

 7.1.负载均衡器主备功能，如果主节点全部挂掉，请求转发到可用的备用节点，如果备用节点也挂了，就抛出异常，如果主节点恢复正常，那么请求重新发往主节点 
 7.2. 异地灾备，服务采用异地灾备模式部署，服务优先调用本地，当本地服务全部挂掉，服务请求转发到异地服务，如果本地服务部分恢复或者全部恢复，那么请求重新发往本地服务


```



# 2.导入http负载均衡器

在工程中导入以下maven坐标即可

```xml
<dependency>
   <groupId>com.bbossgroups</groupId>
   <artifactId>bboss-http</artifactId>
   <version>5.5.3</version>
</dependency>
```

如果是gradle工程，导入方法如下：

```
implementation 'com.bbossgroups:bboss-http:5.5.3'
```

# 3.负载均衡组件

```
org.frameworkset.spi.remote.http.HttpRequestProxy
```

## 3.1 负载均衡组件API

### 3.1.1 启动相关的api及示例

HttpRequestProxy.startHttpPools(Map configs);

HttpRequestProxy.startHttpPools(String configFile);

加载配置文件启动示例

```java
//加载配置文件，启动负载均衡器
HttpRequestProxy.startHttpPools("application.properties");
```

加载Map属性配置启动负载均衡器示例-

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



## 3.3 使用负载均衡器调用服务

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

# 4.服务发现机制的两种工作模式

本文开头介绍了http负载均衡器服务发现支持从各种数据源获取和发现服务地址：

zookeeper，etcd，consul，eureka，db，其他第三方注册中心

为了支持第三方注册中心，服务发现机制的提供两种工作模式：

**主动发现模式**：bboss通过调用http.discoverService配置的服务发现方法，定时从数据库和注册中心中查询最新的服务地址数据清单，本文上面介绍的http.discoverService就是一种主动定时发现模式

**被动发现模式**：监听zookeeper，etcd，consul，eureka数据变化，适用于发布订阅模式

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
//将被动获取到的地址清单加入服务地址组report中
HttpProxyUtil.handleDiscoverHosts("report",hosts);
```

# 5.主备和异地灾备配置和服务发现

主备和异地灾备配置和服务发现

地址格式配置，其中routing标识服务地址或者路由标记

ip:port|routing

例如：

```properties
#指定了每个地址对应的地区信息，可以按照地区信息进行路由
http.hosts=192.168.137.1:808|beijing,192.168.137.1:809|beijing,192.168.137.1:810|shanghai
```

指定本地区信息或者主节点标识信息

```properties
# 指定本地区信息，系统按地区部署时，指定地区信息，
# 不同的地区请求只路由到本地区（beijing）对应的服务器，shanghai的服务器作为backup服务器，
# 当本地(beijing)的服务器都不可用时，才将请求转发到可用的上海服务器
http.routing=beijing
```

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

# 6.健康检查服务

可以通过http.health属性指定健康检查服务，服务为相对地址，不需要指定ip和端口，例如：

- 设置默认集群组健康服务

```java
configs.put("http.health","/health.html");//health监控检查地址必须配置，否则将不会启动健康检查机
```

- 设置特定集群组健康服务

```java
configs.put("report.http.health","/health.html");//health监控检查地址必须配置，否则将不会启动健康检查机
```

**bboss以get方式发送http.health对应的健康检查服务请求，健康检查服务只需要响应状态码为200-300即认为服务节点健康可用**。

# 7.开发交流



bboss http交流：166471282

**bboss http微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 8.支持我们

<div align="left"></div>
<img src="images/alipay.png"  height="200" width="200">

