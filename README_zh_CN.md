# Elasticsearch Bboss

Bboss is a good elasticsearch Java rest client. It operates and accesses elasticsearch in a way similar to mybatis.

# Environmental requirements

JDK requirement: JDK 1.7+
Elasticsearch version requirements: 2. X,5. X,6. X,+

# How to use Elasticsearch BBoss.

First add the maven dependency of BBoss to your pom.xml:

```xml
       <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
            <version>5.3.8</version>
        </dependency>
```

If it's a spring boot project, you can replace the Maven coordinate above with the following Maven coordinate:

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>5.3.8</version>
        </dependency>
```



Next, add the Elasticsearch addresses to the application.properties file under the project resource directory, and create a new one if the file does not exist:

```properties
elasticsearch.rest.hostNames=10.21.20.168:9200

#Cluster addresses are separated by commas

#elasticsearch.rest.hostNames=10.180.211.27:9200,10.180.211.28:9200,10.180.211.29:9200
```

And last  create a jsp file named testElasticsearch.jsp :

```jsp
<%@ page import="org.frameworkset.elasticsearch.ElasticSearchHelper" %>
<%@ page import="org.frameworkset.elasticsearch.client.ClientInterface" %>
<%@ page import="org.frameworkset.elasticsearch.entity.ESDatas" %>
<%@ page import="org.frameworkset.elasticsearch.scroll.ScrollHandler" %>
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
		public void handle(ESDatas<Map> esDatas) throws Exception {
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

Put the file into the web project that has been connected to pinpoint, run the program, log on pinpoint to see the execution effect of bboss plugin.

# Elasticsearch Java Demos

The following Demo and related documentation is compatible with Elasticsearch 2. X,5. X,6. X,+ versions

## Elasticsearch demo for Java project:

https://github.com/bbossgroups/elasticsearch-example

## Elasticsearch demo for spring booter 1.x,2.x

https://github.com/bbossgroups/elasticsearch-spring-boot-starter-example

# Elasticsearch Java rest client bboss fast integration documentation

[How to use Elasticsearch BBoss](https://my.oschina.net/bboss/blog/2999092)

[Spring boot integration ElasticSearch case sharing](https://my.oschina.net/bboss/blog/1835601)

[Quickly integrate Elasticsearch Restful API case sharing](https://my.oschina.net/bboss/blog/1801273)

# Elasticsearch BBoss easy developer tutorial

[High-performance elasticsearch ORM library bboss use introduction](https://my.oschina.net/bboss/blog/1556866)

# bboss-elastic

不错的elasticsearch客户端工具包,bboss es开发套件采用类似于mybatis的方式操作elasticsearch

jdk要求： jdk 1.7+

elasticsearch版本要求：2.x,5.x,6.x,+

## 快速集成和应用 

非spring boot项目：
https://my.oschina.net/bboss/blog/1801273 

spring boot项目：
https://my.oschina.net/bboss/blog/1835601

详细配置说明参考文档：
https://my.oschina.net/bboss/blog/1556866

# elastic search配置和使用

elastic search配置和使用参考文档

https://my.oschina.net/bboss/blog/1556866 

# 完整的demo

https://github.com/bbossgroups/eshelloword-booter

https://github.com/bbossgroups/eshelloword-spring-boot-starter

https://github.com/bbossgroups/elasticsearchdemo

# bboss elastic特点

https://www.oschina.net/p/bboss-elastic

# 版本升级注意事项

v5.0.5.7及后续版本废弃@PrimaryKey注解，改用@ESId注解来标注索引_id的值

v5.0.6.0及后续版本的dsl配置变量语法变更：

$xxx模式变量将直接输出变量的原始值，不会对变量进行特殊字符转义处理，也不会对变量进行日期格式化处理

请在代码中自行对$xxx模式变量进行特殊转移字符处理和日期格式化处理

只有#[xxx]格式的变量才会对特殊字符进行自动转义处理和日期格式化处理，同时为其增加escape布尔值属性，

用来控制是否对#[xxx]模式变量进行自动化转义处理，false禁用转义处理，true启用，默认启用

# 数据导入导出

增加定时任务，增量导入导出功能，目前提供了全量导入功能

## elasticsearch技术交流群:166471282 

## elasticsearch微信公众号:bbossgroup   

![GitHub Logo](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)

## License

The BBoss Framework is released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0