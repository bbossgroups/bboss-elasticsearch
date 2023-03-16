# 常见问题分析和处理

# 问题1 连接默认的127.0.0.1:9200地址

```java
org.frameworkset.elasticsearch.client.NoServerElasticSearchException: All elasticServer [http://127.0.0.1:9200] can't been connected.
	at org.frameworkset.elasticsearch.client.RoundRobinList.get(RoundRobinList.java:97)
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient._executeHttp(ElasticSearchRestClient.java:588)
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient.executeHttp(ElasticSearchRestClient.java:559)
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient.executeHttp(ElasticSearchRestClient.java:722)
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient.executeHttp(ElasticSearchRestClient.java:533)
	at org.frameworkset.elasticsearch.client.RestClientUtil.getTempate(RestClientUtil.java:1010)
	
```

​	

## 问题分析

出现上面的问题原因分析：

1.没有正确加载application.properties配置文件

2.spring boot环境中获取elasticsearch bboss客户端组件实例方式不对

## 问题处理

### 1.非spring boot环境application.properties文件没有正确加载问题处理

非spring boot环境,如果不存在application.properties文件，那么参考文档建立文件：

https://esdoc.bbossgroups.com/#/quickstart

### 2.spring boot环境中获取ClientInterface实例方式不对问题处理

spring boot环境中获取ClientInterface实例方式不对问题处理,则参考以下方式获取ClientInterface实例。

需要按照spring boot的方式使用和运行代码，spring boot获取客户端组件实例以及操作代码样例：

```java
@Service
public class DocumentCRUD {

	
   private Logger logger = LoggerFactory.getLogger(DocumentCRUD.class);
    //如果是单个集群，必须导入下面的组件才能正确加载客户端配置
   @Autowired
   private BBossESStarter bbossESStarter;
   //如果是多集群，需要通过Qualifier指定每个集群的Starter名称，多集群配置starter初始化参考下面的多集群配置-开始
   @Autowired
   @Qualifier("bbossESStarterDefault")
   private BBossESStarter bbossESStarterDefault;
	@Autowired
	@Qualifier("bbossESStarterLogs") 
   private BBossESStarter bbossESStarterLogs;
	//如果是多集群，需要通过Qualifier指定每个集群的Starter名称，多集群配置starter初始化参考下面的多集群配置-结束
   //DSL config file path
   private String mappath = "esmapper/demo.xml";


   public void dropAndCreateAndGetIndice(){
      //Create a client tool to load configuration files, single instance multithreaded security
       //通过bbossESStarter获取ClientInterface实例
      ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
      ..//ommited
       

   }



   public void addAndUpdateDocument()  {
      //Build a create/modify/get/delete document client object, single instance multi-thread security
      ClientInterface clientUtil = bbossESStarter.getRestClient();
     ..//ommited


   }
```

多集群配置初始化：
```java
/**
* 多个es集群starter初始化

  */
  @Configuration
  public class MultiESSTartConfigurer {
  @Primary
  @Bean(initMethod = "start")
  @ConfigurationProperties("spring.elasticsearch.bboss.default")
  public BBossESStarter bbossESStarterDefault(){
  return new BBossESStarter();

  }

  @Bean(initMethod = "start")
  @ConfigurationProperties("spring.elasticsearch.bboss.logs")
  public BBossESStarter bbossESStarterLogs(){
  return new BBossESStarter();

  }

}
```

如果获取实例方式不正确，我们可以在启动日志中看到类似以下信息：Start Elasticsearch Datasource[default] from springboot[false]

```shell
Start Elasticsearch Datasource[default] from springboot[false]:{"elasticsearch.ttl":"2d","elasticsearch.timeZone":"Asia/Shanghai","elasticsearch.client":"restful","elasticsearch.includeTypeName":"false","elasticsearch.sliceScrollThreadCount":"50","elasticsearch.scrollThreadQueue":"200","elasticsearch.httpPool":"default","elasticsearch.healthCheckInterval":"3000","elasticsearch.dateFormat":"yyyy.MM.dd","elasticUser":"","elasticsearch.scrollBlockedWaitTimeout":"0","elasticsearch.sliceScrollThreadQueue":"100","elasticsearch.showTemplate":"false","elasticsearch.rest.hostNames":"127.0.0.1:9200","elasticsearch.discoverHost":"false","elasticsearch.scrollThreadCount":"50","elasticPassword":"","elasticsearch.sliceScrollBlockedWaitTimeout":"0"}
```

如果正确的话应该是：Start Elasticsearch Datasource[default] from springboot[true]

可以将log日志级别设置为debug，这样可以定位到具体哪个地方获取ClientInterface实例的方式不正确，应用启动时将在日志文件中打印相应的异常堆栈信息，例如：

```shell
java.lang.Exception: Debug Elasticsearch Datasource[default] start trace:if use spring boot and unload spring boot config right,please get the reason from question-answer document:https://esdoc.bbossgroups.com/#/question-answer ,if not ignore this message.
	at org.frameworkset.elasticsearch.ElasticSearch.configureWithConfigContext(ElasticSearch.java:271)
	at org.frameworkset.elasticsearch.ElasticSearchHelper.booter(ElasticSearchHelper.java:186)
	at org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot.boot(ElasticSearchConfigBoot.java:57)
	at org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot.boot(ElasticSearchConfigBoot.java:29)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.frameworkset.elasticsearch.ElasticSearchHelper.init(ElasticSearchHelper.java:342)
	at org.frameworkset.elasticsearch.ElasticSearchHelper.getConfigRestClientUtil(ElasticSearchHelper.java:445)
	at org.bboss.elasticsearchtest.crud.DocumentCRUD7.testCreateIndice(DocumentCRUD7.java:229)
	at org.bboss.elasticsearchtest.crud.DocumentCRUD7Test.main(DocumentCRUD7Test.java:50)
```

其中，org.bboss.elasticsearchtest.crud.DocumentCRUD7.testCreateIndice(DocumentCRUD7.java:229)就是不正确获取实例方式的代码行。

# 问题2 添加文档时，如何将值为null的字段忽略掉

在对象上面添加注解@JsonInclude(Include.NON_NULL),例如:

```java
@JsonInclude(Include.NON_NULL) 
public class Order {
  ....
}
```
# 问题3 检索文档时，如何在返回的对象或者map中包含文档id等元数据信息

在返回的对象或者map中包含文档id等元数据信息，有以下三种途径:

1.如果返回的对象是Map类型，那么直接使用MetaMap作为返回类型即可（MetaMap为java.util.HashMap的子类，包含了文档id等元数据信息属性 ）

2.如果是po对象，那么可以通过元数据注解来标注对象里面作为元数据属性的字段
参考文档： [元数据注解](https://esdoc.bbossgroups.com/#/client-annotation?id=_2元数据注解) 

3.po对象继承包含文档id等元数据信息的抽象类ESId和ESBaseData（不推荐使用，继承的父类中定义的属性可能会和对象本身的属性冲突）

# 问题4 Socket Timeout问题处理
![](images\sockettimeout.png)

如果检索返回数据过程耗时比较长，那么可能报sockettimeout异常，解决问题的办法主要有：

1.优化检索的dsl，提升dsl执行效率

2.限制返回数据的大小

3.优化Elasticsearch配置，例如：集群扩容，加内存

4.调整客户端sockettimeout参数配置

这里我们说明一下在bboss里面对sockettimeout参数配置的方法，非常简单在appliction.properties文件中调整以下参数值即可，单位：毫秒

http.timeoutSocket = 50000

和通讯时长相关的其他几个参数：

连接建立超时时间，单位：毫秒

http.timeoutConnection = 5000

从连接池中获取连接超时时间，单位：毫秒

http.connectionRequestTimeout=10000

5. 关闭写入强制refresh机制

6. 采用批量异步写入

https://esdoc.bbossgroups.com/#/bulkProcessor


# 问题5 the number of real dsl cache records exceeded the maximum cache size n allowed by DSL structure cache

如果在程序运行过程中出现以下日志：

```properties
the number of real dsl cache records exceeded the maximum cache size n allowed by DSL structure cache
```

那么可能存在两个原因：

1. perKeyDSLStructionCacheSize配置过小
   如果是这种情况，可以在dsl xml配置文件中，添加或者修改perKeyDSLStructionCacheSize参数，将值设置更大一些：
     <property name="perKeyDSLStructionCacheSize" value="4000"/>
2. dsl中使用的$var模式变量，而且值变化频繁，导致缓存命中率下降并使得缓存的dsl 结构超过perKeyDSLStructionCacheSize对应的数字，所以告警
   如果是这种情况，一个是调大perKeyDSLStructionCacheSize参数值，同时将dsl中的$var变量写法调整为#[var]方式（foreach、if/else条件表达式中$xxx变量不需调整），例如：

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

perKeyDSLStructionCacheSize参数含义，参考文档：

[dsl配置文件中关于dsl解析语法树缓存相关配置](https://esdoc.bbossgroups.com/#/development?id=_5314-dsl配置文件中关于dsl解析语法树缓存相关配置)

[dsl动态语法](https://esdoc.bbossgroups.com/#/development?id=_53-dsl配置规范)

最新版本bboss提供了dsl 结构缓存区溢出保护机制，当dsl 结构缓存区块溢出时，自动关闭dsl 结构缓存机制，因此可以将bboss升级到6.8.6或以上版本，从而解决该问题，从以下地址获取最新版本信息：

https://esdoc.bbossgroups.com/#/changelog

# 问题6 Conflicting property name definitions: '_source'

![image-20200102105212923](images\jackson_source.png)

## 问题分析

jackson版本过低，例如2.3.2及以下的版本会报以上问题

## 问题处理

两个解决办法，二选一

- 升级jackson 为2.9.9及以上版本

![img](images\jacson.png)

- 升级bboss到6.8.6

# 问题7 mysql大表数据同步慢

经常碰到同步mysql大表数据慢，半天没反应的情况

## 问题分析

没有正确启用mysql jdbc 流处理查询机制

## 问题处理

参考文档解决：[启用mysql 流处理机制](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2814-mysql-resultset-stream%e6%9c%ba%e5%88%b6%e8%af%b4%e6%98%8e)

