# Quick Start
![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

Elasticsearch [Bboss](https://esdoc.bbossgroups.com/#/README)--高性能Elasticsearch Java RestClient 

1. A highlevel rest client.

2. A high performence o/r mapping rest client.

3. A dsl and sql rest client.

4. Support Elasticsearch 1.x,2.x,5.x,6.x,7.x,8.x,+

5. Support Spring boot 1.x,2.x


# 1.Bboss集成

导入BBoss maven坐标:

```xml
    <dependency>
        <groupId>com.bbossgroups.plugins</groupId>
        <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
        <version>6.7.2</version>
    </dependency>
```
如果是spring boot项目，还需导入以下maven坐标:

```xml
    <dependency>
        <groupId>com.bbossgroups.plugins</groupId>
        <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
        <version>6.7.2</version>
    </dependency>
```
# 2.Bboss配置和使用

在项目resources目录下修改application.properties文件（如果不存在则新建application.properties文件），根据项目类型做添加相应配置:

## 2.1 普通maven 项目配置

```properties
#Cluster addresses are separated by commas

#elasticsearch.rest.hostNames=10.180.211.27:9200,10.180.211.28:9200,10.180.211.29:9200

elasticsearch.rest.hostNames=10.21.20.168:9200
```

如需启用HTTPS协议, 则在elasticsearch地址前面添加https协议头:

```properties
elasticsearch.rest.hostNames=https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282
```


如果启用了 x-pack or searchguard 安全认证机制, 则还需在application.properties文件配置认证账号和口令:

```properties
#x-pack or searchguard security authentication and password configuration
elasticUser=elastic
elasticPassword=changeme
```

接下来就可以编写java代码验证集成是否成功:

```java
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.scroll.HandlerInfo;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestInit {
   @Test
   public void init(){

      ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
      //get elasticsearch cluster state
      String result = clientUtil.executeHttp("_cluster/state?pretty",ClientInterface.HTTP_GET);
    
      //check indice twitter and index type tweet exist or not.
      //适用于elasticsearch 6及以下版本有类型，7以上版本无类型
      boolean exist1 = clientUtil.existIndiceType("twitter","tweet");
      System.out.println("twitter  tweet type exist:"+exist1);
      //适用于 Elasticsearch7以上的版本，check indice twitter exist or not，
      exist1 = clientUtil.existIndice("twitter");
      System.out.println("twitter exist:"+exist1);
      //count documents in indice twitter
      long count = clientUtil.countAll("twitter");
      System.out.println(count);
    
      //Get All documents of indice twitter,DEFAULT_FETCHSIZE is 5000
      //返回对象类型为Map，也可以指定为特定的PO对象，适用于记录数量不大的表
      ESDatas<Map> esDatas = clientUtil.searchAll("twitter", Map.class);
      //从esDatas中获取检索到的记录集合
      List<Map> datas = esDatas.getDatas();
      //从esDatas中获取检索到的记录总数
      long totalSize = esDatas.getTotalSize();
    
      //Get All documents of indice twitter,Set fetchsize to 10000, Using ScrollHandler to process each batch of datas.
      //指定批处理器分批次处理数据，适用于记录数量比较大的全表表数据查询
      clientUtil.searchAll("twitter",10000,new ScrollHandler<Map>() {
         public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
            List<Map> dataList = esDatas.getDatas();
            System.out.println("TotalSize:"+esDatas.getTotalSize());
            if(dataList != null) {
               System.out.println("dataList.size:" + dataList.size());
            }
            else
            {
               System.out.println("dataList.size:0");
            }
            //do something other such as do a db query.
            //SQLExecutor.queryList(Map.class,"select * from td_sm_user");
         }
      },Map.class);
      //Use slice parallel scoll query all documents of indice  twitter by 2 thread tasks. DEFAULT_FETCHSIZE is 5000
      //You can also use ScrollHandler to process each batch of datas on your own.
      esDatas = clientUtil.searchAllParallel("twitter", Map.class,2);
      //指定批处理器分批次处理数据（适用于数据量比较大的表），并行检索和处理表数据源，线程数量为2，
      clientUtil.searchAllParallel("twitter",10000,new ScrollHandler<Map>() {
         public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
            List<Map> dataList = esDatas.getDatas();
            System.out.println("TotalSize:"+esDatas.getTotalSize());
            if(dataList != null) {
               System.out.println("dataList.size:" + dataList.size());
            }
            else
            {
               System.out.println("dataList.size:0");
            }
            //do something other such as do a db query.
            //SQLExecutor.queryList(Map.class,"select * from td_sm_user");
         }
      },Map.class,2);
   }
}
```
maven project案例下载地址:

https://gitee.com/bboss/eshelloword-booter

参考文档

集成和配置Elasticsearch bboss：

https://esdoc.bbossgroups.com/#/common-project-with-bboss

## 2.2 spring boot maven 项目配置

```properties
spring.elasticsearch.bboss.elasticsearch.rest.hostNames=127.0.0.1:9200
```


如需启用HTTPS协议, 则在elasticsearch地址前面添加https协议头:

```properties
spring.elasticsearch.bboss.elasticsearch.rest.hostNames=https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282
```


如果启用了 x-pack or searchguard 安全认证机制, 则还需在application.properties文件配置认证账号和口令:

```properties
##support x-pack and searchguard
spring.elasticsearch.bboss.elasticUser=elastic
spring.elasticsearch.bboss.elasticPassword=changeme
```


Spring boot 需要使用注解@Autowired 注入一个BBossESStarter对象来获取elasticsearch rest client api 实例对象（每次获取到的实例是单实例多线程安全的）：

```java
@Autowired
private BBossESStarter bbossESStarter;//Create a client tool to load configuration files, single instance multithreaded security
ClientInterface configClientUtil = bbossESStarter.getConfigRestClient(mappath);
    //Build a create/modify/get/delete document client object, single instance multi-thread security
ClientInterface clientUtil = bbossESStarter.getRestClient();    
```
Spring boot 示例代码:

The dsl xml file this example used [esmapper/demo.xml](https://gitee.com/bboss/springboot-elasticsearch/blob/master/src/main/resources/esmapper/demo.xml)
```java
package com.example.esbboss.service;

import com.example.esbboss.entity.Demo;
import com.example.esbboss.entity.DemoSearchResult;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.boot.BBossESStarter;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DocumentCRUD7 {
   private Logger logger = LoggerFactory.getLogger(DocumentCRUD7.class);
   @Autowired
   private BBossESStarter bbossESStarter;
   //DSL config file path
   private String mappath = "esmapper/demo7.xml";


   public void dropAndCreateAndGetIndice(){
      //Create a client tool to load configuration files, single instance multithreaded security
      ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
      try {
         //To determine whether the indice demo exists, it returns true if it exists and false if it does not
         boolean exist = clientUtil.existIndice("demo");

         //Delete mapping if the indice demo already exists
         if(exist) {
            String r = clientUtil.dropIndice("demo");
            logger.debug("clientUtil.dropIndice(\"demo\") response:"+r);
    
         }
         //Create index demo
         clientUtil.createIndiceMapping("demo",//The indice name
               "createDemoIndice");//Index mapping DSL script name, defined createDemoIndice in esmapper/demo.xml
         String demoIndice = clientUtil.getIndice("demo");//Gets the newly created indice structure
         logger.info("after createIndiceMapping clientUtil.getIndice(\"demo\") response:"+demoIndice);
      } catch (ElasticSearchException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }



   public void addAndUpdateDocument()  {
      //Build a create/modify/get/delete document client object, single instance multi-thread security
      ClientInterface clientUtil = bbossESStarter.getRestClient();
      //Build an object as index document
      Demo demo = new Demo();
      demo.setDemoId(2l);//Specify the document id, the unique identity, and mark with the @ESId annotation. If the demoId already exists, modify the document; otherwise, add the document
      demo.setAgentStarttime(new Date());
      demo.setAgentStarttimezh(new Date());
      demo.setApplicationName("blackcatdemo2");
      demo.setContentbody("this is content body2");
      demo.setName("liudehua");
      demo.setOrderId("NFZF15045871807281445364228");
      demo.setContrastStatus(2);


      //Add the document and force refresh
      String response = clientUtil.addDocument("demo",//indice name
            demo,"refresh=true");



      logger.debug("Print the result：addDocument-------------------------");
      logger.debug(response);
    
      demo = new Demo();
      demo.setDemoId(3l);//Specify the document id, the unique identity, and mark with the @ESId annotation. If the demoId already exists, modify the document; otherwise, add the document
      demo.setAgentStarttime(new Date());
      demo.setApplicationName("blackcatdemo3");
      demo.setContentbody("this is content body3");
      demo.setName("zhangxueyou");
      demo.setOrderId("NFZF15045871807281445364228");
      demo.setContrastStatus(3);
      demo.setAgentStarttime(new Date());
      demo.setAgentStarttimezh(new Date());
    
      //Add the document and force refresh
      response = clientUtil.addDocument("demo",//indice name
            demo,"refresh=true");
    
      //Get the document object according to the document id, and return the Demo object
      demo = clientUtil.getDocument("demo",//indice name
            "2",//document id
            Demo.class);
    
      //update document
      demo = new Demo();
      demo.setDemoId(2l);//Specify the document id, the unique identity, and mark with the @ESId annotation. If the demoId already exists, modify the document; otherwise, add the document
      demo.setAgentStarttime(new Date());
      demo.setApplicationName("blackcatdemo2");
      demo.setContentbody("this is modify content body2");
      demo.setName("刘德华modify\t");
      demo.setOrderId("NFZF15045871807281445364228");
      demo.setContrastStatus(2);
      demo.setAgentStarttimezh(new Date());
      //Execute update and force refresh
      response = clientUtil.addDocument("demo",//index name
            demo,"refresh=true");


      //Get the modified document object according to the document id and return the json message string
      response = clientUtil.getDocument("demo",//indice name
            "2");//document id
      logger.debug("Print the modified result:getDocument-------------------------");
      logger.debug(response);




      logger.debug("Print the modified result：getDocument-------------------------");
      logger.debug(response);


   }

   public void deleteDocuments(){
      //Build a create/modify/get/delete document client object, single instance multi-thread security
      ClientInterface clientUtil = bbossESStarter.getRestClient();
      //Batch delete documents
      clientUtil.deleteDocuments("demo",//indice name
            new String[]{"2","3"});//Batch delete document ids
   }

   /**
    * Use slice parallel scoll query all documents of indice demo by 2 thread tasks. DEFAULT_FETCHSIZE is 5000
    */
   public void searchAllPararrel(){
      ClientInterface clientUtil = bbossESStarter.getRestClient();
      ESDatas<Demo> esDatas = clientUtil.searchAllParallel("demo", Demo.class,2);
   }



   /**
    * Search the documents
    */
   public DemoSearchResult search()   {
      //Create a load DSL file client instance to retrieve documents, single instance multithread security
      ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
      //Set query conditions, pass variable parameter values via map,key for variable names in DSL
      //There are four variables in the DSL:
      //        applicationName1
      //        applicationName2
      //        startTime
      //        endTime
      Map<String,Object> params = new HashMap<String,Object>();
      //Set the values of applicationName1 and applicationName2 variables
      params.put("applicationName1","blackcatdemo2");
      params.put("applicationName2","blackcatdemo3");
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      //Set the time range, and accept the long value as the time parameter
      try {
         params.put("startTime",dateFormat.parse("2017-09-02 00:00:00").getTime());
      } catch (ParseException e) {
         e.printStackTrace();
      }
      params.put("endTime",new Date().getTime());


      //Execute the query
      ESDatas<Demo> esDatas =  //ESDatas contains a collection of currently retrieved records, up to 1000 records, specified by the size attribute in the DSL
            clientUtil.searchList("demo/_search",//demo as the indice, _search as the search action
            "searchDatas",//DSL statement name defined in esmapper/demo.xml
            params,//Query parameters
            Demo.class);//Data object type Demo returned
      //Gets a list of result objects and returns max up to 1000 records (specified in DSL)
      List<Demo> demos = esDatas.getDatas();

//    String json = clientUtil.executeRequest("demo/_search",//demo as the index table, _search as the search action
//          "searchDatas",//DSL statement name defined in esmapper/demo.xml
//          params);//Query parameters
//    String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);
      //Gets the total number of records
      long totalSize = esDatas.getTotalSize();
      DemoSearchResult demoSearchResult = new DemoSearchResult();
      demoSearchResult.setDemos(demos);
      demoSearchResult.setTotalSize(totalSize);
      return demoSearchResult;
   }


}
```
A spring boot Web demo github url:包含本文案例及dsl xml配置文件demo7.xml

https://gitee.com/bboss/springboot-elasticsearch

参考文档

Spring boot集成和配置Elasticsearch:

https://esdoc.bbossgroups.com/#/spring-booter-with-bboss

Springboot集成bboss Elasticsearch和Apollo:

https://esdoc.bbossgroups.com/#/springboot-bbosses-apollo

# 3.获取Elasticsearch客户端组件实例方法

一般项目通过ElasticSearchHelper获取 elasticsearch rest client api 实例：

```java
//创建加载配置文件的客户端实例，单实例多线程安全
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo.xml");
//创建直接操作dsl的客户端实例，单实例多线程安全
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil() ;
```

Spring boot项目使用BBossESStarter获取 elasticsearch rest client api实例:

```java
@Autowired
private BBossESStarter bbossESStarter;//Create a client tool to load configuration files, single instance multithreaded security
ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
    //Build a create/modify/get/delete document client object, single instance multi-thread security
    ClientInterface clientUtil = bbossESStarter.getRestClient();    
```
Elasticsearch bboss 开发指南:

https://esdoc.bbossgroups.com/#/development

# 4.从源码构建Elasticsearch BBoss

首先下载下面的两个源码工程 

https://gitee.com/bboss/bboss-elastic

https://gitee.com/bboss/bboss-elastic-tran

然后通过gradle依次按顺序构建bboss-elasticsearch和bboss-elastic-tran：

gradle clean publishToMavenLocal
Gradle环境搭建和配置教程 

https://esdoc.bbossgroups.com/#/bboss-build

# 5.开发交流

bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

![img](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)