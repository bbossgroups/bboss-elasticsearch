# Spring boot整合Elasticsearch案例分享

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />
 

  **The best elasticsearch highlevel java rest api-----bboss**       

Spring boot整合ElasticSearch HighLevel Rest Client案例分享，本文涉及内容

- 集成bboss es starter
- 单es集群整合
- 多es集群整合

本文内容适合于:

- spring boot 1.x,2.x,3.x
- elasticsearch 1.x,2.x,5.x,6.x,7.x,8.x,+

本文案例涉及源码下载地址：

- 普通spring boot案例

https://git.oschina.net/bboss/eshelloword-spring-boot-starter

- web服务spring boot案例

https://git.oschina.net/bboss/springboot-elasticsearch

- 更多bboss案例可以浏览：

https://esdoc.bbossgroups.com/#/Elasticsearch-demo

# 从源码构建Elasticsearch BBoss

参考文档

 https://esdoc.bbossgroups.com/#/bboss-build

# 1.集成bboss es starter 

在spring boot项目中导入bboss es starter 

maven工程

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-datatran-jdbc</artifactId>
            <version>7.1.1</version>
        </dependency>
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>7.1.1</version>
        </dependency>
```

gradle工程

```groovy
compile "com.bbossgroups.plugins:bboss-datatran-jdbc:7.1.1"
compile "com.bbossgroups.plugins:bboss-elasticsearch-spring-boot-starter:7.1.1"
```



# 2.创建spring boot启动类

新建Application类：

```java
package org.bboss.elasticsearchtest.springboot;


import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 *  @author yinbp [122054810@qq.com]
 *  
 */

@SpringBootApplication

public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

   
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }
}
```

Application类中定义了一个main方法用来启动spring boot测试应用。

Application类将被用于启动下面两个测试用例：

- 单es集群测试用例
- 多es集群测试用例



# 3.单es集群配置和使用



## 3.1 配置单es集群

bboss es starter配置单集群可以采用properties文件也可以采用yml进行配置，二者任选其一。



### 3.1.1 application.properties

修改spring boot配置文件application.properties内容

```properties
##ES集群配置，支持x-pack和searchguard
spring.elasticsearch.bboss.elasticUser=elastic
spring.elasticsearch.bboss.elasticPassword=changeme


spring.elasticsearch.bboss.elasticsearch.rest.hostNames=10.21.20.168:9200
#spring.elasticsearch.bboss.elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
##https配置，添加https://协议头
#spring.elasticsearch.bboss.default.elasticsearch.rest.hostNames=https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282
spring.elasticsearch.bboss.elasticsearch.dateFormat=yyyy.MM.dd
spring.elasticsearch.bboss.elasticsearch.timeZone=Asia/Shanghai
spring.elasticsearch.bboss.elasticsearch.ttl=2d
#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
spring.elasticsearch.bboss.elasticsearch.showTemplate=true
spring.elasticsearch.bboss.elasticsearch.discoverHost=false
# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
spring.elasticsearch.bboss.dslfile.refreshInterval = -1

##es client http连接池配置
spring.elasticsearch.bboss.http.timeoutConnection = 50000
spring.elasticsearch.bboss.http.timeoutSocket = 50000
spring.elasticsearch.bboss.http.connectionRequestTimeout=50000
spring.elasticsearch.bboss.http.retryTime = 1
spring.elasticsearch.bboss.http.maxLineLength = -1
spring.elasticsearch.bboss.http.maxHeaderCount = 200
spring.elasticsearch.bboss.http.maxTotal = 400
spring.elasticsearch.bboss.http.defaultMaxPerRoute = 200
spring.elasticsearch.bboss.http.soReuseAddress = false
spring.elasticsearch.bboss.http.soKeepAlive = false
spring.elasticsearch.bboss.http.timeToLive = 3600000
spring.elasticsearch.bboss.http.keepAlive = 3600000
spring.elasticsearch.bboss.http.keystore =
spring.elasticsearch.bboss.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
spring.elasticsearch.bboss.http.hostnameVerifier =

## 数据库数据源配置，使用db-es数据导入功能时需要配置
#spring.elasticsearch.bboss.db.name = test
#spring.elasticsearch.bboss.db.user = root
#spring.elasticsearch.bboss.db.password = 123456
#spring.elasticsearch.bboss.db.driver = com.mysql.jdbc.Driver
#spring.elasticsearch.bboss.db.url = jdbc:mysql://localhost:3306/bboss
#spring.elasticsearch.bboss.db.usePool = false
#spring.elasticsearch.bboss.db.validateSQL = select 1
```

***单ES集群配置项都是以spring.elasticsearch.bboss开头。***



### 3.1.2 application.yml

**如果采用application.yml配置，内容如下**

```yaml
spring:
  elasticsearch:
    bboss:
      elasticUser: elastic
      elasticPassword: changeme
      elasticsearch:
        rest:
          hostNames: 192.168.8.25:9200
          ##hostNames: 192.168.8.25:9200,192.168.8.26:9200,192.168.8.27:9200  ##集群地址配置
        dateFormat: yyyy.MM.dd
        timeZone: Asia/Shanghai        
        showTemplate: true
        discoverHost: false
      dslfile:
        refreshInterval: -1
      http:
         timeoutConnection: 5000
         timeoutSocket: 5000
         connectionRequestTimeout: 5000
         retryTime: 1
         maxLineLength: -1
         maxHeaderCount: 200
         maxTotal: 400
         defaultMaxPerRoute: 200
         soReuseAddress: false
         soKeepAlive: false
         timeToLive: 3600000
         keepAlive: 3600000
         keystore:
         keyPassword:
         hostnameVerifier:
```



## 3.2 单集群测试用例

Spring boot 通过@Autowired BBossESStarter 来获取Elasticsearch rest client interface实例：

```java
    @Autowired
    private BBossESStarter bbossESStarter;
//Create a client tool to load dsl xml configuration files(获取加载读取dsl xml配置文件的api接口实例，可以在代码里面直接通过dsl配置名称引用dsl即可）, single instance multithreaded security
    ClientInterface configClientUtil = bbossESStarter.getConfigRestClient("esmapper/gateway.xml");
        //Build a create/modify/get/delete document client object(获取不需要读取dsl xml配置文件的api接口实例，可以在代码里面直接编写dsl), single instance multi-thread security
        ClientInterface clientUtil = bbossESStarter.getRestClient();   
```

### 3.2.1 定义测试用例

编写es单集群测试用例BBossESStarterTestCase

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
package org.bboss.elasticsearchtest.springboot;


import org.frameworkset.elasticsearch.client.ClientInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单集群演示功能测试用例，spring boot配置项以spring.elasticsearch.bboss开头
 * 对应的配置文件为application.properties文件
 * @author  yinbp [122054810@qq.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BBossESStarterTestCase {
	@Autowired
	private BBossESStarter bbossESStarter;
	@Autowired
	DocumentCRUD documentCRUD;

    @Test
    public void testBbossESStarter() throws Exception {
//        System.out.println(bbossESStarter);

		//验证环境,获取es状态
//		String response = serviceApiUtil.restClient().executeHttp("_cluster/state?pretty",ClientInterface.HTTP_GET);

//		System.out.println(response);
		//判断索引类型是否存在，false表示不存在，正常返回true表示存在
		boolean exist = bbossESStarter.getRestClient().existIndiceType("twitter","tweet");

		//判读索引是否存在，false表示不存在，正常返回true表示存在
		exist =  bbossESStarter.getRestClient().existIndice("twitter");

		exist =  bbossESStarter.getRestClient().existIndice("agentinfo");

    }

    @Test
	public void testCRUD() throws Exception {

		//删除/创建文档索引表
		documentCRUD.testCreateIndice();
		//添加/修改单个文档

		documentCRUD.testAddAndUpdateDocument();
		//批量添加文档
		documentCRUD.testBulkAddDocument();
		//检索文档
		documentCRUD.testSearch();
		//批量修改文档
		documentCRUD.testBulkUpdateDocument();

		//检索批量修改后的文档
		documentCRUD.testSearch();
		//带list复杂参数的文档检索操作
		documentCRUD.testSearchArray();
		//带from/size分页操作的文档检索操作
		documentCRUD.testPagineSearch();
		//带sourcefilter的文档检索操作
		documentCRUD.testSearchSourceFilter();

		documentCRUD.updateDemoIndice();
		documentCRUD.testBulkAddDocuments();
	}

	@Test
	public void testPerformaceCRUD() throws Exception {

		//删除/创建文档索引表
		documentCRUD.testCreateIndice();

		documentCRUD.testBulkAddDocuments();
	}

}
```

```java
@Service
public class DocumentCRUD {
    private final Logger logger = LoggerFactory.getLogger(DocumentCRUD.class);
    @Autowired
    private BBossESStarter bbossESStarter;
    //DSL config file path
    private final String mappath = "esmapper/demo.xml";


    public void dropAndCreateAndGetIndice(){
        //Create a client tool to load configuration files(获取加载读取dsl xml配置文件的api接口实例，可以在代码里面直接通过dsl的配置名称引用dsl即可）, single instance multithreaded security
        ClientInterface clientUtil = bbossESStarter.getConfigRestClient("esmapper/gateway.xml");
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
                "demo",//idnex type
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
                "demo",//idnex type
                demo,"refresh=true");

        //Get the document object according to the document id, and return the Demo object
        demo = clientUtil.getDocument("demo",//indice name
                "demo",//idnex type
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
                "demo",//idnex type
                demo,"refresh=true");


        //Get the modified document object according to the document id and return the json message string
        response = clientUtil.getDocument("demo",//indice name
                "demo",//idnex type
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
                "demo",//idnex type
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
        ClientInterface clientUtil = bbossESStarter.getConfigRestClient("esmapper/gateway.xml");
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

//        String json = clientUtil.executeRequest("demo/_search",//demo as the index table, _search as the search action
//                "searchDatas",//DSL statement name defined in esmapper/demo.xml
//                params);//Query parameters

//        String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);
        //Gets the total number of records
        long totalSize = esDatas.getTotalSize();
        DemoSearchResult demoSearchResult = new DemoSearchResult();
        demoSearchResult.setDemos(demos);
        demoSearchResult.setTotalSize(totalSize);
        return demoSearchResult;
    }
}
```



### 3.2.2 运行测试用例

直接通过junit运行上述测试用例即可。

其中

```properties
BBossESStarter：由bboss提供，直接在代码中声明引用，并使用即可=
DocumentCRUD=各种增删改查操作实例，在demo工程中提供
```



# 4.多ES集群测试用例



## 4.1 配置多es集群

bboss es starter配置多集群可以采用properties文件也可以采用yml进行配置，二者任选其一。



### 4.1.1properties配置

修改spring boot配置文件application-multi-datasource.properties，内容如下：

```properties
##多集群配置样例，如果需要做多集群配置，请将参照本文内容修改application.properties文件内容
spring.elasticsearch.bboss.default.name = default
##default集群配配置
spring.elasticsearch.bboss.default.elasticUser=elastic
spring.elasticsearch.bboss.default.elasticPassword=changeme


spring.elasticsearch.bboss.default.elasticsearch.rest.hostNames=10.21.20.168:9200
#spring.elasticsearch.bboss.default.elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
spring.elasticsearch.bboss.default.elasticsearch.dateFormat=yyyy.MM.dd
spring.elasticsearch.bboss.default.elasticsearch.timeZone=Asia/Shanghai
spring.elasticsearch.bboss.default.elasticsearch.ttl=2d
#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
spring.elasticsearch.bboss.default.elasticsearch.showTemplate=true
spring.elasticsearch.bboss.default.elasticsearch.discoverHost=false

##default连接池配置
spring.elasticsearch.bboss.default.http.timeoutConnection = 50000
spring.elasticsearch.bboss.default.http.timeoutSocket = 50000
spring.elasticsearch.bboss.default.http.connectionRequestTimeout=50000
spring.elasticsearch.bboss.default.http.retryTime = 1
spring.elasticsearch.bboss.default.http.maxLineLength = -1
spring.elasticsearch.bboss.default.http.maxHeaderCount = 200
spring.elasticsearch.bboss.default.http.maxTotal = 400
spring.elasticsearch.bboss.default.http.defaultMaxPerRoute = 200
spring.elasticsearch.bboss.default.http.keystore =
spring.elasticsearch.bboss.default.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
spring.elasticsearch.bboss.default.http.hostnameVerifier =

##logs集群配置
spring.elasticsearch.bboss.logs.name = logs
spring.elasticsearch.bboss.logs.elasticUser=elastic
spring.elasticsearch.bboss.logs.elasticPassword=changeme


spring.elasticsearch.bboss.logs.elasticsearch.rest.hostNames=127.0.0.1:9200

#spring.elasticsearch.bboss.default.elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
spring.elasticsearch.bboss.logs.elasticsearch.dateFormat=yyyy.MM.dd
spring.elasticsearch.bboss.logs.elasticsearch.timeZone=Asia/Shanghai
spring.elasticsearch.bboss.logs.elasticsearch.ttl=2d
#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
spring.elasticsearch.bboss.logs.elasticsearch.showTemplate=true
spring.elasticsearch.bboss.logs.elasticsearch.discoverHost=false

##logs集群对应的连接池配置
spring.elasticsearch.bboss.logs.http.timeoutConnection = 50000
spring.elasticsearch.bboss.logs.http.timeoutSocket = 50000
spring.elasticsearch.bboss.logs.http.connectionRequestTimeout=50000
spring.elasticsearch.bboss.logs.http.retryTime = 1
spring.elasticsearch.bboss.logs.http.maxLineLength = -1
spring.elasticsearch.bboss.logs.http.maxHeaderCount = 200
spring.elasticsearch.bboss.logs.http.maxTotal = 400
spring.elasticsearch.bboss.logs.http.defaultMaxPerRoute = 200
# https证书配置
spring.elasticsearch.bboss.logs.http.keystore =
spring.elasticsearch.bboss.logs.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
spring.elasticsearch.bboss.logs.http.hostnameVerifier =
# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
spring.elasticsearch.bboss.dslfile.refreshInterval = -1
```

配置说明：

上面配置了两个集群：default和logs

每个集群配置项的前缀为：spring.elasticsearch.bboss.集群名字，其中的集群名字是一个自定义的逻辑名称，用来在client api中引用集群。

*注意：这里的集群名字是根据集群的作用命名的名字，和Elasticsearch本身的cluster.name没有关系，可自行根据需要命名*

default集群的配置项前缀为：

```properties
spring.elasticsearch.bboss.default=
```

logs集群的配置项前缀为：

```properties
spring.elasticsearch.bboss.logs=
```

同时每个集群的配置项目里面必须包含name项目的配置

default集群name配置：

```properties
spring.elasticsearch.bboss.default.name = default
```

logs集群name配置：

```properties
##logs集群配置
spring.elasticsearch.bboss.logs.name = logs
```



### 4.1.2 yml配置（默认）

```yaml
spring:
  elasticsearch:
    bboss:
      default:
        name: default
        elasticUser: elastic
        elasticPassword: changeme
        elasticsearch:
          rest:
            hostNames: 127.0.0.1:9200
            ##hostNames: 127.0.0.1:9200,127.0.0.1:9201,127.0.0.1:9202 
          dateFormat: yyyy.MM.dd
          timeZone: Asia/Shanghai
          showTemplate: true
          discoverHost: false
        dslfile:
          refreshInterval: -1
        http:
           timeoutConnection: 5000
           timeoutSocket: 5000
           connectionRequestTimeout: 5000
           retryTime: 1
           maxLineLength: -1
           maxHeaderCount: 200
           maxTotal: 400
           defaultMaxPerRoute: 200
           soReuseAddress: false
           soKeepAlive: false
           timeToLive: 3600000
           keepAlive: 3600000
           keystore:
           keyPassword:
           hostnameVerifier:
      logs:
          name: logs
          elasticUser: elastic
          elasticPassword: changeme
          elasticsearch:
            rest:
              hostNames: 127.0.0.1:9200
            dateFormat: yyyy.MM.dd
            timeZone: Asia/Shanghai
            ttl: 2d
            showTemplate: true
            discoverHost: false
          dslfile:
            refreshInterval: -1
          http:
             timeoutConnection: 5000
             timeoutSocket: 5000
             connectionRequestTimeout: 5000
             retryTime: 1
             maxLineLength: -1
             maxHeaderCount: 200
             maxTotal: 400
             defaultMaxPerRoute: 200
             soReuseAddress: false
             soKeepAlive: false
             timeToLive: 3600000
             keepAlive: 3600000
             keystore:
             keyPassword:
             hostnameVerifier:
```



## 4.2 定义加载多es集群配置的spring boot Configuration类

### 4.2.1 新建类MultiESSTartConfigurer，声明分别对应两个不同集群的bboss工厂组件

```java
package org.bboss.elasticsearchtest.springboot;
/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.boot.BBossESStarter;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 配置多个es集群
 * 指定多es数据源profile：multi-datasource
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

说明：

MultiESSTartConfigurer通过以下两个方法分别加载default和logs两个es集群的配置

default集群配置加载

```java
@Primary
@Bean(initMethod = "start")
@ConfigurationProperties("spring.elasticsearch.bboss.default")
public BBossESStarter bbossESStarterDefault()
```

logs集群配置加载

```java
@Bean(initMethod = "start")
@ConfigurationProperties("spring.elasticsearch.bboss.logs")
public BBossESStarter bbossESStarterLogs()
```

BBossESStarter bbossESStarterDefault  对应spring.elasticsearch.bboss.default配置的elasticsearch集群

BBossESStarter bbossESStarterLogs  对应spring.elasticsearch.bboss.logs配置的elasticsearch集群

两个组件的声明都是必须的，在程序中只要用其中任意一个都可以获取到两个对应集群的ClientInterface组件，具体看后面的示例。

### 4.2.2 通过BBossEsstarter或者指定的es数据客户端组件的方法

默认default数据源

```java
    @Autowired
	@Qualifier("bbossESStarterDefault")
	private BBossESStarter bbossESStarterDefault;
//Create a client tool to load configuration files, single instance multithreaded security，指定default数据源的名称
    ClientInterface clientUtil = bbossESStarterDefault.getConfigRestClient("default","esmapper/gateway.xml");
        //Build a create/modify/get/delete document client object, single instance multi-thread security，指定default数据源的名称
        ClientInterface clientUtil = bbossESStarterDefault.getRestClient("default");    
```

logs数据源

```java
    @Autowired
	@Qualifier("bbossESStarterLogs")
	private BBossESStarter bbossESStarterLogs;
//Create a client tool to load configuration files, single instance multithreaded security，指定logs数据源的名称
    ClientInterface clientUtil = bbossESStarterLogs.getConfigRestClient("logs","esmapper/gateway.xml");
        //Build a create/modify/get/delete document client object, single instance multi-thread security，指定logs数据源的名称
        ClientInterface clientUtil = bbossESStarterLogs.getRestClient("logs");    
```

## 4.3 多es集群测试用例

### 4.3.1 定义测试用例

多es集群测试用例MultiBBossESStartersTestCase

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
package org.bboss.elasticsearchtest.springboot;


import org.frameworkset.elasticsearch.client.ClientInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 多集群演示功能测试用例，spring boot配置项以spring.elasticsearch.bboss.集群名称开头，例如：
 * spring.elasticsearch.bboss.default 默认es集群
 * spring.elasticsearch.bboss.logs  logs es集群
 * 两个集群通过 org.bboss.elasticsearchtest.springboot.MultiESSTartConfigurer加载
 * 对应的配置文件为application-multi-datasource.properties文件
 * 通过ActiveProfiles指定并激活多es集群配置：multi-datasource 
 * @author yinbp [122054810@qq.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("multi-datasource")
public class MultiBBossESStartersTestCase {
    @Autowired
	@Qualifier("bbossESStarterDefault")
	private BBossESStarter bbossESStarterDefault;
    @Autowired
	@Qualifier("bbossESStarterLogs")
	private BBossESStarter bbossESStarterLogs;
	@Autowired
	MultiESDocumentCRUD multiESDocumentCRUD;
    @Test
    public void testMultiBBossESStarters() throws Exception {

		//验证环境,获取es状态
//		String response = bbossESStarterDefault.getRestClient().executeHttp("_cluster/state?pretty",ClientInterface.HTTP_GET);
//		System.out.println(response);


		//判断索引类型是否存在，false表示不存在，正常返回true表示存在
		boolean exist = bbossESStarterDefault.getRestClient().existIndiceType("twitter","tweet");
		System.out.println("default twitter/tweet:"+exist);
		//获取logs对应的Elasticsearch集群客户端，并进行existIndiceType操作
		exist = bbossESStarterDefault.getRestClient("logs").existIndiceType("twitter","tweet");
		System.out.println("logs twitter/tweet:"+exist);
		//获取logs对应的Elasticsearch集群客户端，判读索引是否存在，false表示不存在，正常返回true表示存在
		exist = bbossESStarterDefault.getRestClient("logs").existIndice("twitter");
		System.out.println("logs  twitter:"+exist);
		//获取logs对应的Elasticsearch集群客户端，判断索引是否定义
		exist = bbossESStarterDefault.getRestClient("logs").existIndice("agentinfo");
		System.out.println("logs agentinfo:"+exist);
    }

	@Test
	public void testCRUD() throws Exception {

		//删除/创建文档索引表
		multiESDocumentCRUD.testCreateIndice();
		//添加/修改单个文档

		multiESDocumentCRUD.testAddAndUpdateDocument();
		//批量添加文档
		multiESDocumentCRUD.testBulkAddDocument();
		//检索文档
		multiESDocumentCRUD.testSearch();
		//批量修改文档
		multiESDocumentCRUD.testBulkUpdateDocument();

		//检索批量修改后的文档
		multiESDocumentCRUD.testSearch();
		//带list复杂参数的文档检索操作
		multiESDocumentCRUD.testSearchArray();
		//带from/size分页操作的文档检索操作
		multiESDocumentCRUD.testPagineSearch();
		//带sourcefilter的文档检索操作
		multiESDocumentCRUD.testSearchSourceFilter();

		multiESDocumentCRUD.updateDemoIndice();
		multiESDocumentCRUD.testBulkAddDocuments();
	}

	@Test
	public void testPerformaceCRUD() throws Exception {

		//删除/创建文档索引表
		multiESDocumentCRUD.testCreateIndice();

		multiESDocumentCRUD.testBulkAddDocuments();
	}
}
```

### 4.3.2 运行测试用例

直接通过junit运行上述测试用例即可。

其中

```java
BBossESStarter bbossESStarterDefault：实现由bboss提供，在直接在代码中MultiESSTartConfigurer 定义
MultiESDocumentCRUD:各种增删改查操作实例，在demo工程中提供
```

# 5 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="images/qrcode.jpg"  height="200" width="200">



