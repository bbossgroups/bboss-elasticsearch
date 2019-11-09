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

