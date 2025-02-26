# 自定义bboss elasticsearch数据源

bboss默认提供了以下三种方式配置和管理Elasticsearch数据源：

- [resources/application.properties](https://esdoc.bbossgroups.com/#/common-project-with-bboss)（适用于spring boot和非spring boot项目）
- [resources/application.yaml](https://esdoc.bbossgroups.com/#/spring-booter-with-bboss)（只适用于spring boot）文件配置和管理Elasticsearch数据源。
- [apollo](https://esdoc.bbossgroups.com/#/springboot-bbosses-apollo)和[nacos](https://esdoc.bbossgroups.com/#/nacos-config)配置中心管理Elasticsearch数据源

亦可以自行定义配置和管理bboss elasticsearch数据源，本文介绍具体的方法。

## 1.指定配置文件启动和初始化

指定配置文件启动和初始化Elasticsearch bboss

```java
/**
 * boot操作必须在所有的ClientInterface组件创建之前调用
 *  按照默认的配置文件初始化elasticsearch客户端工具
 *     conf/elasticsearch.properties,application.properties,config/application.properties
 */
//ElasticSearchBoot.boot();

/**
 * boot操作必须在所有的ClientInterface组件创建之前调用
 * 根据指定的配置文件初始化elasticsearch客户端工具
 * @param configFile 指定1到多个多个ElasticSearch属性配置文件，对应的路径格式为（多个用逗号分隔），例如：
 * conf/elasticsearch.properties,application.properties,config/application.properties
 * 上述的文件都是在classpath下面即可，如果需要指定绝对路径，格式为：
 * file:d:/conf/elasticsearch.properties,file:d:/application.properties,config/application.properties
 *
 * 说明：带file:前缀表示后面的路径为绝对路径
 */
ElasticSearchBoot.boot("myapplication.properties");
//ElasticSearchBoot.boot("file:/home/elasticsearch/myapplication.properties");
```

## 2.代码中配置参数启动和初始化

所有的属性通过Map对象传入并初始化ES rest client组件：

```java
Map properties = new HashMap();

properties.put("xxx","value");

...........

ElasticsearchBootResult elasticsearchBootResult = ElasticSearchBoot.boot(properties);
```

支持单数据源和多数据源配置，多数据源配置需要通过elasticsearch.serverNames指定数据源的名称，多个用逗号分割，

然后添加对应数据源的属性时，需要在属性名称前添加数据源名称前缀，例如：
```java
properties.put("elasticsearch.serverNames","es233");
properties.put("es233.elasticUser","elastic");
properties.put("es233.elasticPassword","changeme");
```
如果没有指定数据源前缀，那么都是针对default数据源的配置，另外通过boot方法启动数据源时，如果对应名称的数据源已经存在，将忽略该数据源的启动

boot方法执行后将返回本次启动的数据源清单和加载的属性信息container对象：ElasticsearchBootResult
```java

ElasticsearchBootResult elasticsearchBootResult  = ElasticSearchBoot.boot(properties);
ElasticsearchBootResult包含以下两个属性：
public class ElasticsearchBootResult {
	/**
	 * 加载的属性配置container
	 */	
	private PropertiesContainer propertiesContainer;
	/**
	 * 初始化的Elasticsearch数据源清单
	 */
	private List<String> initedElasticsearchs;
}
```
下面详细介绍。

### 2.1 单个Elasticsearch数据源案例

```java
Map properties = new HashMap();
/**
 * 这里只设置必须的配置项，其他的属性参考配置文件：resources/application.properties
 *
 */
//认证账号和口令配置，如果启用了安全认证才需要，支持xpack和searchguard
properties.put("elasticUser","elastic");
properties.put("elasticPassword","changeme");
//es服务器地址和端口，多个用逗号分隔
properties.put("elasticsearch.rest.hostNames","127.0.0.1:9200");
//是否在控制台打印dsl语句，log4j组件日志级别为INFO或者DEBUG
properties.put("elasticsearch.showTemplate","true");
//集群节点自动发现
properties.put("elasticsearch.discoverHost","true");
properties.put("http.timeoutSocket","60000");
properties.put("http.timeoutConnection","40000");
properties.put("http.connectionRequestTimeout","70000");
ElasticSearchBoot.boot(properties);
```

### 2.2 多Elasticsearch数据源案例

```java
Map properties = new HashMap();

/**
 * 多集群配置样例
 * 这里只设置必须的配置项，其他的属性参考配置文件：resources/application.properties.multicluster
 */

// 注意：多数据源配置时，首先必须声明每个数据源的名称
// 声明两个es数据源的名称，代码里面通过这个名称指定对应的数据源
//default为默认的Elasitcsearch数据源名称，es233对应了一个elasticsearch 2.3.3的Elasitcsearch集群数据源
properties.put("elasticsearch.serverNames","default,es233");

/**
 * 默认的default数据源配置，每个配置项可以加default.前缀，也可以不加
 */
//认证账号和口令配置，如果启用了安全认证才需要，支持xpack和searchguard
//properties.put("default.elasticUser","elastic");
//properties.put("default.elasticPassword","changeme");
properties.put("default.elasticUser","elastic");
properties.put("default.elasticPassword","changeme");
//es服务器地址和端口，多个用逗号分隔
properties.put("default.elasticsearch.rest.hostNames","192.168.137.1:9200");
//是否在控制台打印dsl语句，log4j组件日志级别为INFO或者DEBUG
properties.put("default.elasticsearch.showTemplate","true");
//集群节点自动发现
properties.put("default.elasticsearch.discoverHost","true");
properties.put("default.http.timeoutSocket","60000");
properties.put("default.http.timeoutConnection","40000");
properties.put("default.http.connectionRequestTimeout","70000");

/**
 * es233数据源配置，每个配置项必须以es233.前缀开始
 */
properties.put("es233.elasticUser","elastic");
properties.put("es233.elasticPassword","changeme");
//es服务器地址和端口，多个用逗号分隔
properties.put("es233.elasticsearch.rest.hostNames","192.168.137.1:9200");
//是否在控制台打印dsl语句，log4j组件日志级别为INFO或者DEBUG
properties.put("es233.elasticsearch.showTemplate","true");
//集群节点自动发现
properties.put("es233.elasticsearch.discoverHost","true");
properties.put("es233.http.timeoutSocket","60000");
properties.put("es233.http.timeoutConnection","40000");
properties.put("es233.http.connectionRequestTimeout","70000");
ElasticSearchBoot.boot(properties);
```
通过boot方法启动数据源时，如果对应名称的数据源已经存在，将忽略该数据源的启动。

数据源启动后，可以通过特定的方法获取特定数据源ClientInterface实例，参考文档：

[获取多数据源ClientInterface方法](https://esdoc.bbossgroups.com/#/development?id=_521-%e6%99%ae%e9%80%9amaven%e9%a1%b9%e7%9b%ae%e5%a4%9aes%e9%9b%86%e7%be%a4%e6%95%b0%e6%8d%ae%e6%ba%90%e5%ae%a2%e6%88%b7%e7%ab%af%e7%bb%84%e4%bb%b6%e5%ae%9a%e4%b9%89%e6%96%b9%e6%b3%95)

### 2.3 基于Kerberos认证数据源案例

#### 2.3.1 基于参数配置模式

```java
	Map properties = new HashMap();
		/**
		 * 这里只设置必须的配置项，其他的属性参考配置文件：resources/application.properties
		 *
		 */
		//认证账号和口令配置，如果启用了安全认证才需要，支持xpack和searchguard
		properties.put("elasticsearch.serverNames","es233");
		//es服务器地址和端口，多个用逗号分隔
		properties.put("es233.elasticsearch.rest.hostNames","192.168.137.1:9200");
		//是否在控制台打印dsl语句，log4j组件日志级别为INFO或者DEBUG
		properties.put("es233.elasticsearch.showTemplate","true");
		//集群节点自动发现
		properties.put("es233.elasticsearch.discoverHost","true");

        
//        # kerberos安全认证配置
        properties.put("es233.http.kerberos.principal","elastic/admin@BBOSSGROUPS.COM");
        properties.put("es233.http.kerberos.keytab","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/elastic.keytab");
        properties.put("es233.http.kerberos.krb5Location","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/krb5.conf");
        properties.put("es233.http.kerberos.useTicketCache","false");
        //#http.kerberos.useKeyTab=true
        
        //#Krb5 in GSS API needs to be refreshed so it does not throw the error
        //#Specified version of key is not available
        properties.put("es233.http.kerberos.refreshKrb5Config","false");
        
        properties.put("es233.http.kerberos.storeKey","true");
        properties.put("es233.http.kerberos.doNotPrompt","true");
        properties.put("es233.http.kerberos.isInitiator","true");
        properties.put("es233.http.kerberos.debug","true");
        properties.put("es233.http.kerberos.loginContextName","Krb5Login");
        properties.put("es233.http.kerberos.useSubjectCredsOnly","true");
        

		ElasticSearchBoot.boot(properties);
```

#### 2.3.2 基于jaas配置模式

```java
 Map properties = new HashMap();
        /**
         * 这里只设置必须的配置项，其他的属性参考配置文件：resources/application.properties
         *
         */
        //认证账号和口令配置，如果启用了安全认证才需要，支持xpack和searchguard
        properties.put("elasticsearch.serverNames","es233");
        //es服务器地址和端口，多个用逗号分隔
        properties.put("es233.elasticsearch.rest.hostNames","192.168.137.1:9200");
        //是否在控制台打印dsl语句，log4j组件日志级别为INFO或者DEBUG
        properties.put("es233.elasticsearch.showTemplate","true");
        //集群节点自动发现
        properties.put("es233.elasticsearch.discoverHost","true");


//        # kerberos安全认证配置
        properties.put("es233.http.kerberos.krb5Location","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/krb5.conf");
        properties.put("es233.http.kerberos.loginConfig","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/jaas.conf");
        properties.put("es233.http.kerberos.loginContextName","test");
        properties.put("es233.http.kerberos.debug","true");


        ElasticSearchBoot.boot(properties);
```

#### 2.3.3 基于Serverrealm配置模式

Serverrealm配置模式只需增加serverRealmPath服务地址配置或者serverRealm参数配置（二选一），其他Kerberos参数可以参考2.3.1和2.3.2章节配置

```java
Map properties = new HashMap();
        /**
         * 这里只设置必须的配置项，其他的属性参考配置文件：resources/application.properties
         *
         */
        //认证账号和口令配置，如果启用了安全认证才需要，支持xpack和searchguard
        properties.put("elasticsearch.serverNames","es233");
        //es服务器地址和端口，多个用逗号分隔
        properties.put("es233.elasticsearch.rest.hostNames","192.168.137.1:9200");
        //是否在控制台打印dsl语句，log4j组件日志级别为INFO或者DEBUG
        properties.put("es233.elasticsearch.showTemplate","true");
        //集群节点自动发现
        properties.put("es233.elasticsearch.discoverHost","true");

 
//        kerberos安全认证配置
 // properties.put("es233.http.kerberos.serverRealm","elastic/hadoop.bbossgroups.com@BBOSSGROUPS.COM");  
  properties.put("es233.http.kerberos.serverRealmPath","/elasticsearch/serverrealm");        properties.put("es233.http.kerberos.useSubjectCredsOnly","false");       properties.put("es233.http.kerberos.krb5Location","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/krb5.conf");
        properties.put("es233.http.kerberos.loginConfig","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/jaas.conf");
        properties.put("es233.http.kerberos.loginContextName","test");
        properties.put("es233.http.kerberos.debug","true");


        ElasticSearchBoot.boot(properties);
```

Kerberos认证参考资料：

https://esdoc.bbossgroups.com/#/development?id=_212-kerberos%e8%ae%a4%e8%af%81%e9%85%8d%e7%bd%ae

## 3.停止elasticsearch数据源

```java
ElasticSearchHelper.stopElasticsearch("default");//指定要停止的数据源名称
```

## 4.参考资料

本文涉及的案例源码和工程地址

gitee工程地址

https://gitee.com/bboss/eshelloword-booter

github地址

https://github.com/bbossgroups/eshelloword-booter

自定义初始化案例

https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/custominit/CustormInitAndBoot.java

https://github.com/bbossgroups/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/custominit/CustormInitAndBoot.java

基于Kerberos认证的自定义初始化案例

https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/custominit/CustormInitAndBootKerberosAuth.java

## 4. 开发交流

参考文档：[快速开始bboss](https://esdoc.bbossgroups.com/#/quickstart)

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />



交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">





## 5.支持我们

如果您正在使用bboss，或是想支持我们继续开发，您可以通过如下方式支持我们：

1.Star并向您的朋友推荐或分享

[bboss elasticsearch client](https://gitee.com/bboss/bboss-elastic)🚀

[数据采集&流批一体化处理](https://gitee.com/bboss/bboss-elastic-tran)🚀

2.通过[爱发电 ](https://afdian.net/a/bbossgroups)直接捐赠，或者扫描下面二维码进行一次性捐款赞助，请作者喝一杯咖啡☕️





<img src="images/alipay.png"  height="200" width="200">

<img src="images/wchat.png" style="zoom:50%;" />

非常感谢您对开源精神的支持！❤您的捐赠将用于bboss社区建设、QQ群年费、网站云服务器租赁费用。



