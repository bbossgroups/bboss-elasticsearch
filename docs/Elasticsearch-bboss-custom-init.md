# 基于自定义配置文件/Map属性集初始化bboss es方法介绍

本文涉及的案例源码和工程地址

gitee工程地址

https://gitee.com/bboss/eshelloword-booter

github地址

https://github.com/bbossgroups/eshelloword-booter

自定义初始化java类

https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/custominit/CustormInitAndBoot.java

https://github.com/bbossgroups/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/custominit/CustormInitAndBoot.java

# 1.指定配置文件启动和初始化

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

# 2.代码中配置参数启动和初始化

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

## 单个Elasticsearch数据源案例

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

## 多Elasticsearch数据源案例

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


# 3.停止elasticsearch数据源

```java
ElasticSearchHelper.stopElasticsearch("default");//指定要停止的数据源名称
```

# 开发交流

参考文档：[快速开始bboss](https://esdoc.bbossgroups.com/#/quickstart)

bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



