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
	at com.ai.terminal.init.ESInit.init(ESInit.java:131)
	at com.ai.terminal.init.ESInit.reset(ESInit.java:124)
	at com.ai.terminal.init.ESInit.main(ESInit.java:157)
```

​	

## 问题分析

没有加载到application.properties配置文件

## 问题处理

1.如果配置文件是在test源码目录，那么运行代码的时候就需要在test目录下面编写测试用例运行

2.如果是在spring boot环境下面运行代码，那么就需要按照spring boot的方式使用和运行代码
spring booter操作代码样例：

```java
@Service
public class DocumentCRUD {
   private Logger logger = LoggerFactory.getLogger(DocumentCRUD.class);
   @Autowired
   private BBossESStarter bbossESStarter;
   //DSL config file path
   private String mappath = "esmapper/demo.xml";


   public void dropAndCreateAndGetIndice(){
      //Create a client tool to load configuration files, single instance multithreaded security
      ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
      ..//ommited
       

   }



   public void addAndUpdateDocument()  {
      //Build a create/modify/get/delete document client object, single instance multi-thread security
      ClientInterface clientUtil = bbossESStarter.getRestClient();
     ..//ommited


   }
```

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

1.如果返回的对象是Map类型，那么直接使用MetaMap作为返回类型即可

2.如果是po对象，那么可以通过元数据注解来标注对象里面作为元数据属性的字段
参考文档： [元数据注解](https://esdoc.bbossgroups.com/#/client-annotation?id=_2元数据注解) 

3.接触ESId和ESBaseData对象（不推荐使用，继承的父类中定义的属性可能会和对象本身的属性冲突）

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

perKeyDSLStructionCacheSize参数含义，参考文档：

[dsl配置文件中关于dsl解析语法树缓存相关配置](https://esdoc.bbossgroups.com/#/development?id=_5314-dsl配置文件中关于dsl解析语法树缓存相关配置)



# 问题6 Conflicting property name definitions: '_source'

![image-20200102105212923](images\jackson_source.png)

## 问题分析

jackson版本过低，例如2.3.2及以下的版本会报以上问题

## 问题处理

两个解决办法，二选一

- 升级jackson 为2.9.9及以上版本

![img](images\jacson.png)

- 升级bboss到6.0.1