# 基于自定义配置文件/Map属性集初始化bboss es方法介绍

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

ElasticSearchBoot.boot(properties);
```

支持单数据源和多数据源配置，下面举例说明。

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
properties.put("elasticsearch.rest.hostNames","10.13.11.6:9200");
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
properties.put("default.elasticsearch.rest.hostNames","10.13.11.6:9200");
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

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>

<img src="images/alipay.png"  height="200" width="200">

