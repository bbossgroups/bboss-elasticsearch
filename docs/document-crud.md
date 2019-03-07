# Elasticsearch添加修改删除索引文档

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

本文介绍如何采用bboss es添加/修改/查询/删除/批量删除elasticsearch索引文档，直接看代码。

```java
添加/修改文档：

TAgentInfo agentInfo = new TAgentInfo() ;
agentInfo.setIp("192.168.137.1")；//ip属性作为文档唯一标识，根据ip值对应的索引文档存在与否来决定添加或者修改操作
//设置地理位置坐标
agentInfo.setLocation("28.292781,117.238963");
//设置其他属性
。。。。
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();				
clientUtil.addDocument("agentinfo",//索引名称
                       "agentinfo",//索引类型
                        agentInfo);//索引数据对象
//执行查询操作
ESDatas<TAgentInfo> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<TAgentInfo>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("trace-*/_search",//查询操作，查询indices trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                TAgentInfo.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致
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

参考文档：

<https://my.oschina.net/bboss/blog/1556866>