# Quick Start

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

Introduce Elasticsearch [Bboss](README.md):

1. A highlevel rest client.
2. A high performence o/r mapping rest client.
3. A dsl and sql rest client.

# Integration Bboss

First add the maven dependency of BBoss to your pom.xml:

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
            <version>5.9.5</version>
        </dependency>
```

If it's a spring boot project, you can replace the Maven coordinate above with the following Maven coordinate:

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>5.9.5</version>
        </dependency>
```

# Configuration and use Bboss

Next, add the Elasticsearch addresses to the application.properties file under the project resource directory, and create a new one if the file does not exist:

## 1.**common maven project config**

```properties
elasticsearch.rest.hostNames=10.21.20.168:9200

#Cluster addresses are separated by commas

#elasticsearch.rest.hostNames=10.180.211.27:9200,10.180.211.28:9200,10.180.211.29:9200
```

If the HTTPS protocol is on, add the https protocol header to the elasticsearch address:

```properties
elasticsearch.rest.hostNames=https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282
```

If x-pack or searchguard security authentication is enabled, configure the account and password with the following two properties in application.properties:

```properties
# x-pack or searchguard security authentication and password configuration
elasticUser=elastic
elasticPassword=changeme
```

And create a test jsp file named testElasticsearch.jsp ，Common project use ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil() to get a elasticsearch rest client api instance：

```java
<%@ page import="org.frameworkset.elasticsearch.ElasticSearchHelper" %>
<%@ page import="org.frameworkset.elasticsearch.client.ClientInterface" %>
<%@ page import="org.frameworkset.elasticsearch.entity.ESDatas" %>
<%@ page import="org.frameworkset.elasticsearch.scroll.ScrollHandler" %>
<%@ page import="org.frameworkset.elasticsearch.scroll.HandlerInfo" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.frameworkset.common.poolman.SQLExecutor" %>
<%@ page language="java" pageEncoding="UTF-8"%>

<%
	ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
	//get elasticsearch cluster state
	String result = clientUtil.executeHttp("_cluster/state?pretty",ClientInterface.HTTP_GET);

	//check indice twitter and index type tweet exist or not.
	boolean exist1 = clientUtil.existIndiceType("twitter","tweet");
	out.println("twitter  tweet type exist:"+exist1);
	//check indice twitter exist or not
	exist1 = clientUtil.existIndice("twitter");
	out.println("twitter exist:"+exist1);
	//count documents in indice twitter
	long count = clientUtil.countAll("twitter");
	out.println(count);

	//Get All documents of indice twitter,DEFAULT_FETCHSIZE is 5000
	ESDatas<Map> esDatas = clientUtil.searchAll("twitter", Map.class);

	//Get All documents of indice twitter,Set fetchsize to 10000, Using ScrollHandler to process each batch of datas.
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
	out.println("searchAllParallel:ok");
%>
```

Put the jsp file into your web project , run it in browser, then see the execution result of bboss .

A maven project demo github url:

https://github.com/bbossgroups/elasticsearch-example

## **2.spring boot maven project config**

```properties
spring.elasticsearch.bboss.elasticsearch.rest.hostNames=127.0.0.1:9200
```

If the HTTPS protocol is on, add the https protocol header to the elasticsearch address:

```properties
spring.elasticsearch.bboss.elasticsearch.rest.hostNames=https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282
```

If x-pack or searchguard security authentication is enabled, configure the account and password with the following two properties in application.properties:

```properties
##support x-pack and searchguard
spring.elasticsearch.bboss.elasticUser=elastic
spring.elasticsearch.bboss.elasticPassword=changeme
```

Spring boot use @Autowired	BBossESStarter  to get a elasticsearch rest client api instance：

```java
    @Autowired
	private BBossESStarter bbossESStarter;
//Create a client tool to load configuration files, single instance multithreaded security
	ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
		//Build a create/modify/get/delete document client object, single instance multi-thread security
		ClientInterface clientUtil = bbossESStarter.getRestClient();	

```
Spring boot Test Service example:  

The dsl xml file this example used [esmapper/demo.xml](https://github.com/bbossgroups/es_bboss_web/blob/master/src/main/resources/esmapper/demo.xml)

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

/**
 * @author yinbp[yin-bp@163.com]
 */
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

//		String json = clientUtil.executeRequest("demo/_search",//demo as the index table, _search as the search action
//				"searchDatas",//DSL statement name defined in esmapper/demo.xml
//				params);//Query parameters

//		String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);
		//Gets the total number of records
		long totalSize = esDatas.getTotalSize();
		DemoSearchResult demoSearchResult = new DemoSearchResult();
		demoSearchResult.setDemos(demos);
		demoSearchResult.setTotalSize(totalSize);
		return demoSearchResult;
	}
}
```

A spring boot Web demo github url:

<https://github.com/bbossgroups/es_bboss_web>

# Note

Common project use ElasticSearchHelper to get a elasticsearch rest client api instance： 

```java
//创建加载配置文件的客户端工具，单实例多线程安全
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo.xml");
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil() ;
```

Spring boot use BBossESStarter to get a elasticsearch rest client api instance:

```java
    @Autowired
	private BBossESStarter bbossESStarter;
//Create a client tool to load configuration files, single instance multithreaded security
	ClientInterface clientUtil = bbossESStarter.getConfigRestClient(mappath);
		//Build a create/modify/get/delete document client object, single instance multi-thread security
		ClientInterface clientUtil = bbossESStarter.getRestClient();	
```

Elasticsearch bboss development document:

[Development Document](development.md)



# 从源码构建Elasticsearch BBoss

First Get source code from https://github.com/bbossgroups/bboss-elasticsearch

Then change to cmd window under directory bboss-elasticsearch and run gradle build command：

```properties
gradle install
```

Gradle environmenet install and config document: https://esdoc.bbossgroups.com/#/bboss-build

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>
<img src="images/alipay.png"  height="200" width="200">

