# Quick Start

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

Introduce Elasticsearch [Bboss](README.md):

1. A highlevel rest client.
2. A high performence o/r mapping rest client.
3. A dsl and sql rest client.

First add the maven dependency of BBoss to your pom.xml:

```xml
<dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
            <version>5.6.1</version>
        </dependency>
```

If it's a spring boot project, you can replace the Maven coordinate above with the following Maven coordinate:

```xml
<dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>5.6.1</version>
        </dependency>
```

Next, add the Elasticsearch addresses to the application.properties file under the project resource directory, and create a new one if the file does not exist:

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

And last create a jsp file named testElasticsearch.jsp :

```jsp
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

The Web demo github url:

<https://github.com/bbossgroups/es_bboss_web>

bboss elasticsearch document:

[Development Document](development.md)



# 从源码构建Elasticsearch BBoss

First Get source code from https://github.com/bbossgroups/bboss-elasticsearch

Then change to cmd window under directory bboss-elasticsearch and run gradle build command：

```properties
gradle install
```

Gradle environmenet install and config document: https://esdoc.bbossgroups.com/#/bboss-build

