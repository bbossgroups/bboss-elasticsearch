# Elasticsearch Mget、GetDocSource、局部更新索引案例

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

# 1.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置es客户端bboss



# 2.mget操作

简单而直观的多文档获取案例

```java
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		//获取json报文

        String response  = clientUtil.mgetDocuments("dbdemo",//索引表
                new String[]{"TT5A8JQBcIsteX3sNfdw","TD5A8JQBcIsteX3sNfdw"});//文档id清单，如果是数字类型，请用Integer之类的封装对象
		System.out.println(response);
		//获取封装成对象的文档列表，此处是Map对象，还可以是其他用户定义的对象类型


        List<Map> docs = clientUtil.mgetDocuments("dbdemo",//索引表
                Map.class,//返回文档对象类型
                new String[]{"TT5A8JQBcIsteX3sNfdw","TD5A8JQBcIsteX3sNfdw"});//文档id清单
		System.out.println(docs);
	 
```

通过执行dsl获取多个文档的内容案例

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/mget7.xml");
		//通过执行dsl获取多个文档的内容
		List<String> ids = new ArrayList<String>();
		ids.add("TT5A8JQBcIsteX3sNfdw");
		ids.add("TD5A8JQBcIsteX3sNfdw");
		Map params = new HashMap();
		params.put("ids",ids);
		String response = clientUtil.executeHttp("_mget",
												 "testMget",//dsl定义名称
												 params, //存放文档id的参数
				                                 ClientUtil.HTTP_POST);
		System.out.println(response);
		List<Map> docs = clientUtil.mgetDocuments("_mget",
												"testMget",//dsl定义名称
												 params, //存放文档id的参数
												 Map.class);//返回文档对象类型
		System.out.println(docs);
```

dsl定义-esmapper/estrace/mget7.xml

```xml
<!--
GET /_mget
{
            "docs" : [
                {
                    "_index" : "dbdemo",
                   
                    "_id" : "TT5A8JQBcIsteX3sNfdw"
                },
                {
                     "_index" : "dbdemo",
                     
                    "_id" : "TD5A8JQBcIsteX3sNfdw"
                }
            ]
        }
-->
<properties>
    <property name="testMget">
        <![CDATA[
    
            {
                "docs" : [
                #foreach($id in $ids)
                    #if($foreach.index > 0),#end
                    {
                        "_index" : "dbdemo",
                        "_id" : "$id"
                    }
                #end
                ]
            }
            ]]>
    </property>
</properties>
```

完整案例地址

Elasticsearch 7+及以上版本案例：https://gitee.com/bboss/elasticsearchdemo/blob/master/src/test/java/org/frameworkset/elasticsearch/TestMGet7.java

Elasticsearch 6及以下版本案例：https://gitee.com/bboss/elasticsearchdemo/blob/master/src/test/java/org/frameworkset/elasticsearch/TestMGet.java

# 3.更新索引文档部分信息案例

简单api案例

```java
        Map params = new HashMap();
		Date date = new Date();
		params.put("eventTimestamp",date.getTime());
		params.put("eventTimestampDate",date);
        params.put("location","28.292781,117.238963");
		/**
		 * 更新索引部分内容
		 */
		ClientInterface restClientUtil = ElasticSearchHelper.getRestClientUtil();
		String response = restClientUtil.updateDocument("agentinfo",//索引表名称
				"agentinfo",//索引type
				"pdpagent",//索引id
				params,//待更新的索引字段信息
				"refresh");//强制刷新索引
		System.out.println(response);
```

采用dsl案例

```java
ClientInterface configRestClientUtil = 
ElasticSearchHelper.getConfigRestClientUtil("esmapper/agentstat.xml");
		Map params = new HashMap();
		Date date = new Date();
		params.put("eventTimestamp",date.getTime());
		params.put("eventTimestampDate",date);
params.put("location","28.292781,117.238963");
		/**
		 * 采用dsl更新索引部分内容,dsl定义和路径api可以参考文档：
		 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
		 */
		StringBuilder path = new StringBuilder();
        String docid = "pdpagent";
        String parentId = "parentId";
		path.append("agentinfo/agentinfo/").append(docid ).append("/_update?refresh");//自行拼接rest api地址
        //path.append("&routing=").append(parentId );//如果需要指定routing，则使用routing参数
		configRestClientUtil.updateByPath(path.toString(),
						"updateAgentInfoEndtime",//更新文档内容的dsl配置名称
				         params);
```

dsl文件定义-esmapper/agentstat.xml

```xml
<properties>
    <!--
    POST test/_doc/1/_update
         {
             "doc" : {
             "name" : "new_name"
             }
         }
    -->
    <property name="updateAgentInfoEndtime">
        <![CDATA[
         {
             "doc" : {
                "endTimestamp" : #[eventTimestamp],
                "endTimestampDate" : #[eventTimestampDate],
                "location":#[location]
             }
         }
        ]]>
    </property>
</properties>
```

我们可以设置控制参数来更新文档的版本等信息，参考文档：

[https://esdoc.bbossgroups.com/#/development](https://esdoc.bbossgroups.com/#/development?id=_48-%e4%b8%ba%e6%b7%bb%e5%8a%a0%e4%bf%ae%e6%94%b9%e6%96%87%e6%a1%a3%e6%8c%87%e5%ae%9a%e6%8e%a7%e5%88%b6%e5%8f%82%e6%95%b0)

# 4.GetDocSource案例

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		//获取json报文索引source，不返回索引元数据
		String response = clientUtil.getDocumentSource("agentinfo/agentinfo/10.21.20.168/_source");
		System.out.println(response);
		//获取对象类型source，此处对象类型是map，可以指定自定义的对象类型，不返回索引元数据
		Map data = clientUtil.getDocumentSource("agentinfo/agentinfo/10.21.20.168/_source",Map.class);
		System.out.println(data);
		//请求地址格式说明：
		// index/indexType/docId/_source
		// 实例如下：
		// "agentinfo/agentinfo/10.21.20.168/_source"
```



# 5.几种经典的获取文档数据案例

根据文档id获取

```java
        //根据文档id获取文档对象，返回json报文字符串
		String response = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"2");//w

		System.out.println("打印结果：getDocument-------------------------");
		System.out.println(response);
		//根据文档id获取文档对象，返回Demo对象
		demo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"2",//文档id
				Demo.class);
```

根据rest url获取

```java
        ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		String response = clientUtil.getDocumentByPath("agentinfo/agentinfo/10.21.20.168");
		System.out.println(response);
		Map data = clientUtil.getDocumentByPath("agentinfo/agentinfo/10.21.20.168",Map.class);
		System.out.println(data);
		//请求地址格式说明：
		// index/indexType/docId
		// 实例如下：
		// "agentinfo/agentinfo/10.21.20.168"
```

# 开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />



交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">



 
