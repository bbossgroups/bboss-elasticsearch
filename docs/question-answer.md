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

​	