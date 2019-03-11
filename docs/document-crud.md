# Elasticsearch添加修改删除索引文档

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

本文介绍如何采用bboss es添加/修改/查询/删除/批量删除elasticsearch索引文档，直接看代码。

# 添加/修改文档

```java

TAgentInfo agentInfo = new TAgentInfo() ;
agentInfo.setIp("192.168.137.1")；//ip属性作为文档唯一标识，根据ip值对应的索引文档存在与否来决定添加或者修改操作
//设置地理位置坐标
agentInfo.setLocation("28.292781,117.238963");
//设置其他属性
。。。。
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();	
//添加/修改文档
clientUtil.addDocument("agentinfo",//索引名称
                       "agentinfo",//索引类型
                        agentInfo);//索引数据对象

TAgentInfo的结构如下：
public class TAgentInfo implements java.io.Serializable{
	private String hostname;
	@ESId   //ip属性作为文档唯一标识，根据ip值对应的索引文档存在与否来决定添加或者修改操作
	private String ip;
	private String ports;
	private String agentId;
    private String location;
	private String applicationName;
	private int serviceType;
	private int pid;
	private String agentVersion;
	private String vmVersion;
    //日期类型
	private Date startTimestampDate;
	private Date endTimestampDate;
	private long startTimestamp;
	private long endTimestamp;
	private int endStatus;
	private String serverMetaData;
	private String jvmInfo;	
}
//删除索引文档
clientUtil.deleteDocument("agentinfo",//索引表
      "agentinfo",//索引类型
      "192.168.137.1");//文档id

//批量删除索引文档
clientUtil.deleteDocuments("agentinfo",//索引表
      "agentinfo",//索引类型
      "192.168.137.1","192.168.137.2","192.168.137.3");//文档ids
```

# 批量添加/修改文档

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
List<Demo> demos = new ArrayList<>();
Demo demo = new Demo();
demo.setDemoId(2l);
demo.setAgentStarttime(new Date());
demo.setApplicationName("blackcatdemo2");
demo.setContentbody("this is content body2");
demo.setName("刘德华");
demos.add(demo);
demo = new Demo();
demo.setDemoId(3l);
demo.setAgentStarttime(new Date());
demo.setApplicationName("blackcatdemo3");
demo.setContentbody("四大天王，这种文化很好，中华人民共和国");
demo.setName("张学友");
demos.add(demo);
//批量添加/修改文档
String response = clientUtil.addDocuments("demo",//索引表
"demo",//索引类型
demos);
```

# 根据文档id获取文档对象

```java
//根据文档id获取文档对象，返回Demo对象
Demo demo = clientUtil.getDocument("demo",//索引表
      "demo",//索引类型
      "2",//文档id
      Demo.class);
//根据文档id获取文档对象，返回Map对象
Map map = clientUtil.getDocument("demo",//索引表
      "demo",//索引类型
      "2",//文档id
      Map.class);      
```

# 检索文档

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
//执行查询操作
ESDatas<TAgentInfo> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<TAgentInfo>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("trace-*/_search",//查询操作，查询indices trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                TAgentInfo.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致
```



# 执行多表查询操作

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
//执行多表查询操作
ESDatas<TAgentInfo> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<TAgentInfo>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("trace1,trace2/_search",//查询操作，同时查询trace1,trace2中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                TAgentInfo.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致
```

检索文档对应的dsl语句定义：esmapper/estrace/ESTracesMapper.xml

```xml
 
    <!--
    全文检索查询条件
    -->
    <property name="qcondition">
        <![CDATA[
        "bool": {
            "filter": [
                 {"range": {
                        "startTime": {
                            "gte": #[startTime],
                            "lt": #[endTime],
                            "format": "epoch_millis"
                        }
                 }}
                #if($application && !$application.equals("_all"))
                ,
                {"term": {
                    "applicationName.keyword": #[application]
                }}
                #end
                #if($queryStatus.equals("success"))
                  ,
                  {"term": {
                       "err": 0
                  }}
                #elseif($queryStatus.equals("error"))
                  ,
                  {"term": {
                       "err": 1
                  }}
                #end
            ]
            #if($queryCondition && !$queryCondition.equals(""))
                 ,
                "must": [
                    {
                        "query_string": {
                            "query": #[queryCondition],
                            "analyze_wildcard": true,
                            #if(!$searchFields)
                                "fields": ["rpc","params","agentId","applicationName","endPoint","remoteAddr"]
                            #else
                                "fields":[
                                #foreach($field in $searchFields)
                                      #if($velocityCount > 0),#end #[searchFields[$velocityCount]]
                                #end
                                ]
                            #end
                        }
                    }
                ]
            #end
        }]]>
    </property>

    <!--
    query dsl
    -->
    <property name="queryServiceByCondition">
        <![CDATA[{
             
            "query": {
                @{qcondition}
            },

            "highlight": {
                "pre_tags": [
                    "<mark>"
                ],
                "post_tags": [
                    "</mark>"
                ],
                "fields": {
                    "*": {}
                },
                "fragment_size": 2147483647
            }
        }]]></property>
```

