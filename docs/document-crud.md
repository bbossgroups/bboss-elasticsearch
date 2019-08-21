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

clientUtil.addDocument("agentinfo",//索引名称
                       "agentinfo",//索引类型
                        agentInfo,//索引数据对象
                        "refresh=true");//强制实时刷新

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
      new String[]{"192.168.137.1","192.168.137.2","192.168.137.3"});//文档ids
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

# @ESIndex注解使用

bboss 5.6.8新增了一组添加和修改文档的api，这组api没有带indexName和indextype参数，对应的索引和索引type在po对象中通过@ESIndex注解来指定。

@ESIndex对应的demo地址：

<https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DemoWithESIndex.java>

<https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java>

@ESIndex提供了两个属性name和type，使用方法：

```java
@ESIndex(name="demowithesindex") //es 7不需要指定type
@ESIndex(name="demowithesindex",type="demowithesindex")
索引名称由demowithesindex和日期类型字段agentStarttime通过yyyy.MM.dd格式化后的值拼接而成
索引类型为demowithesindex
@ESIndex(name="demowithesindex-{agentStarttime,yyyy.MM.dd}",type="demowithesindex")
索引名称由demowithesindex和当前日期通过yyyy.MM.dd格式化后的值拼接而成
索引类型为type字段对应的值
@ESIndex(name="demowithesindex-{dateformat=yyyy.MM.dd}",type="{type}")
索引名称由demowithesindex和日期类型字段agentStarttime通过yyyy.MM.dd格式化后的值拼接而成
索引类型为type字段对应的值
@ESIndex(name="demowithesindex-{field=agentStarttime,dateformat=yyyy.MM.dd}",type="{field=type}")
```



## api清单

```java
/**
 * 创建或者更新索引文档
 *  indexName，  indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public   String addDocument(Object bean) throws ElasticSearchException;
/**
 * 创建或者更新索引文档
 *  indexName，  indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param bean
 * @param clientOptions
 * @return
 * @throws ElasticSearchException
 */
public String addDocument(Object bean,ClientOptions clientOptions) throws ElasticSearchException;

/**
 * indexName，   indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param params
 * @param updateOptions
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(Object params,UpdateOptions updateOptions) throws ElasticSearchException;

/**
 * indexName，   indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param documentId
 * @param params
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(Object documentId,Object params) throws ElasticSearchException;

/**
 * indexName，   indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param beans
 * @return
 * @throws ElasticSearchException
 */
public String addDocuments(List<?> beans) throws ElasticSearchException;

/**
 * indexName，   indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param beans
 * @param clientOptions
 * @return
 * @throws ElasticSearchException
 */
public String addDocuments(List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;
/**
 *
 * indexName，   indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param beans
 * @param clientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
 * @return
 * @throws ElasticSearchException
 */
public String updateDocuments( List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;

/**
 * indexName，   indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
 * @param beans
 * @return
 * @throws ElasticSearchException
 */
public   String updateDocuments( List<?> beans) throws ElasticSearchException;
```

## 定义带ESIndex注解的实体

简单用法

```java
@ESIndex(name="demowithesindex",type="demowithesindex")
public class DemoWithESIndex  {
   private Object dynamicPriceTemplate;
   //设定文档标识字段
   @ESId(readSet = true,persistent = false)
   private Long demoId;
   。。。。。。。
} 
```

复杂用法

```java
/**
 * 测试实体，可以从ESBaseData对象继承meta属性，检索时会将文档的一下meta属性设置到对象实例中
 */
@ESIndex(name="demowithesindex-{agentStarttime,yyyy.MM.dd}",type="demowithesindex")
public class DemoWithESIndex extends ESBaseData {
   private Object dynamicPriceTemplate;
   //设定文档标识字段
   @ESId(readSet = true,persistent = false)
   private Long demoId;
   。。。。。。。
} 
```

## api使用

```java
/**
 * 批量导入20002条数据
 */
public void testBulkAddDocumentsWithESIndex() {
   //创建批量创建文档的客户端对象，单实例多线程安全
   ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
   clientUtil.dropIndice("demowithesindex-*");
   List<DemoWithESIndex> demos = new ArrayList<DemoWithESIndex>();
   DemoWithESIndex demo = null;
   long start = System.currentTimeMillis();
   for(int i = 0 ; i < 20002; i ++) {
      demo = new DemoWithESIndex();//定义第一个对象
      demo.setDemoId((long)i);
      demo.setAgentStarttime(new Date());
      demo.setApplicationName("blackcatdemo"+i);
      demo.setContentbody("this is content body"+i);
      if(i % 2 == 0) {
         demo.setName("刘德华喜欢唱歌" + i);
      }
      else{
         demo.setName("张学友不喜欢唱歌" + i);
      }

      demo.setOrderId("NFZF15045871807281445364228");
      demo.setContrastStatus(2);
      demos.add(demo);//添加第一个对象到list中
   }
   //批量添加或者修改2万个文档，将两个对象添加到索引表demo中，批量添加2万条记录耗时1.8s，
   ClientOptions clientOptions = new ClientOptions();
   clientOptions.setRefreshOption("refresh=true");
   clientOptions.setIdField("demoId");
   String response = clientUtil.addDocuments(
         demos,clientOptions);//为了测试效果,启用强制刷新机制，实际线上环境去掉最后一个参数"refresh=true"
   long end = System.currentTimeMillis();
   System.out.println("BulkAdd 20002 Documents elapsed:"+(end - start)+"毫秒");
   start = System.currentTimeMillis();
   String datasr = ElasticSearchHelper.getRestClientUtil().executeHttp("demowithesindex-*/_search","{\"size\":1000,\"query\": {\"match_all\": {}}}",ClientInterface.HTTP_POST);
   System.out.println(datasr);
   //scroll查询2万条记录：0.6s，参考文档：https://my.oschina.net/bboss/blog/1942562
   ESDatas<Demo> datas = clientUtil.scroll("demowithesindex-*/_search","{\"size\":1000,\"query\": {\"match_all\": {}}}","1m",Demo.class);
   end = System.currentTimeMillis();
   System.out.println("scroll SearchAll 20002 Documents elapsed:"+(end - start)+"毫秒");
   int max = 6;
   Map params = new HashMap();
   params.put("sliceMax", max);//最多6个slice，不能大于share数
   params.put("size", 1000);//每页1000条记录

   datas = clientUtil.scrollSlice("demowithesindex-*/_search","scrollSliceQuery", params,"1m",Demo.class);
   //scroll上下文有效期1分钟
   //scrollSlice 并行查询2万条记录：0.1s，参考文档：https://my.oschina.net/bboss/blog/1942562
   start = System.currentTimeMillis();
   datas = clientUtil.scrollSliceParallel("demowithesindex-*/_search","scrollSliceQuery", params,"1m",Demo.class);
   end = System.currentTimeMillis();
   System.out.println("scrollSlice SearchAll 20002 Documents elapsed:"+(end - start)+"毫秒");
   if(datas != null){
      System.out.println("scrollSlice SearchAll datas.getTotalSize():"+datas.getTotalSize());
      if(datas.getDatas() != null)
         System.out.println("scrollSlice SearchAll datas.getDatas().size():"+datas.getDatas().size());
   }
   long count = clientUtil.countAll("demowithesindex-*");

   System.out.println("addDocuments-------------------------" +count);

}
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
//获取结果对象列表
        List<TAgentInfo> demos = data.getDatas();
        //获取总记录数
        long totalSize = data.getTotalSize();
```

# 返回索引元数据的检索操作

检索文档的时候，除了返回要检索的业务数据，同时也可以返回索引元数据信息，只是返回的对象类必须继承父类：

org.frameworkset.elasticsearch.entity.ESBaseData

高亮检索和父子查询信息都通过这个机制实现，下面举例说明：

首先定义一个Demo对象，继承ESBaseData

```java
/**
 * 测试实体，可以从ESBaseData对象继承meta属性，检索时会将文档的一下meta属性设置到对象实例中
 */
public class Demo extends ESBaseData {
   private Object dynamicPriceTemplate;
   //设定文档标识字段
   @ESId(readSet = true,persistent = false)
   private Long demoId;
   private String contentbody;
   /**  当在mapping定义中指定了日期格式时，则需要指定以下两个注解,例如
    *
    "agentStarttime": {
    "type": "date",###指定多个日期格式
    "format":"yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
    }
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Column(dataformat = "yyyy-MM-dd HH:mm:ss.SSS")
    */

   protected Date agentStarttime;
   private String applicationName;
   private String orderId;
   private int contrastStatus;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   private String name;

   public String getContentbody() {
      return contentbody;
   }

   public void setContentbody(String contentbody) {
      this.contentbody = contentbody;
   }

   public Date getAgentStarttime() {
      return agentStarttime;
   }

   public void setAgentStarttime(Date agentStarttime) {
      this.agentStarttime = agentStarttime;
   }

   public String getApplicationName() {
      return applicationName;
   }

   public void setApplicationName(String applicationName) {
      this.applicationName = applicationName;
   }

   public Long getDemoId() {
      return demoId;
   }

   public void setDemoId(Long demoId) {
      this.demoId = demoId;
   }

   public Object getDynamicPriceTemplate() {
      return dynamicPriceTemplate;
   }

   public void setDynamicPriceTemplate(Object dynamicPriceTemplate) {
      this.dynamicPriceTemplate = dynamicPriceTemplate;
   }

   public String getOrderId() {
      return orderId;
   }

   public void setOrderId(String orderId) {
      this.orderId = orderId;
   }

   public int getContrastStatus() {
      return contrastStatus;
   }

   public void setContrastStatus(int contrastStatus) {
      this.contrastStatus = contrastStatus;
   }
}
```



执行查询，demo对象中除了包含返回的业务数据，还包含索引相关的元数据，下面是演示代码：

```java
    /**
    * 检索文档
    * @throws ParseException
    */
   public void testSearch() throws ParseException {
      //创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
      ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
      //设定查询条件,通过map传递变量参数值,key对于dsl中的变量名称
      //dsl中有四个变量
      //        applicationName1
      //        applicationName2
      //        startTime
      //        endTime
      Map<String,Object> params = new HashMap<String,Object>();
      //设置applicationName1和applicationName2两个变量的值
      params.put("applicationName1","blackcatdemo2");
      params.put("applicationName2","blackcatdemo3");
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      //设置时间范围,时间参数接受long值
      params.put("startTime",dateFormat.parse("2017-09-02 00:00:00"));
      params.put("endTime",new Date());
      //执行查询，demo为索引表，_search为检索操作action
      ESDatas<Demo> esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
            clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
            "searchDatas",//esmapper/demo.xml中定义的dsl语句
            params,//变量参数
            Demo.class);//返回的文档封装对象类型

      long count = clientUtil.count("demo","searchDatas",//esmapper/demo.xml中定义的dsl语句
            params);//变量参数
      //获取结果对象列表，最多返回1000条记录
      List<Demo> demos = esDatas.getDatas();

      for(int i = 0; demos != null && i < demos.size(); i ++){
         Demo demo = demos.get(i);
         //获取索引元数据
         Double score = demo.getScore();//文档评分
         String indexName = demo.getIndex();//索引名称
         String indexType = demo.getType();//索引type
         Map<String,Object> nested = demo.getNested();//文档neste信息
         Map<String,Map<String, InnerSearchHits>> innerHits = demo.getInnerHits();//文档父子查询数据
         Map<String,List<Object>> highlight = demo.getHighlight();//高亮检索数据
         Map<String,List<Object>> fields = demo.getFields();//检索字段信息
         long version = demo.getVersion();//文档版本号
         Object parent = demo.getParent();//文档父docId
         Object routing = demo.getRouting();//文档路由信息
         String id = demo.getId();//文档docId
          Object[] sort = demo.getSort();//排序信息
      }


      long totalSize = esDatas.getTotalSize();
      System.out.println(totalSize);
```

# 通过URL参数检索文档

通过url参数检索文档，参数参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html

```java

	@Test
	public void testQueryObject(){
		//batchUuid:b13e998a-78c7-48f5-b067-d4b6d0b044a4

		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		Map data  = clientInterface.searchObject("terminalcontent-*/_search?q=batchUuid:b13e998a-78c7-48f5-b067-d4b6d0b044a4&size=1&terminate_after=1",Map.class);
		System.out.println(data);
	}

	@Test
	public void testQueryList(){
		//batchUuid:b13e998a-78c7-48f5-b067-d4b6d0b044a4

		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> data  = clientInterface.searchList("terminalcontent-*/_search?q=requestType:httprequest",Map.class);
		System.out.println(data.getDatas());
		System.out.println(data.getTotalSize());
	}
```

# 执行多表查询操作

执行多表查询操作，逗号分隔表名称

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
//执行多表查询操作，逗号分隔表名称
ESDatas<TAgentInfo> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<TAgentInfo>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("trace1,trace2/_search",//查询操作，同时查询trace1,trace2中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                TAgentInfo.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致
//获取结果对象列表
        List<TAgentInfo> demos = data.getDatas();
        //获取总记录数
        long totalSize = data.getTotalSize();
```

执行多表查询操作，通配符匹配多表（适合按日期分表的场景）：

trace-2009.09.18

trace-2009.09.19

可以通过trace-*同时检索这两张表的数据：

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
//执行多表查询操作，逗号分隔表名称
ESDatas<TAgentInfo> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<TAgentInfo>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("trace-*/_search",//查询操作，同时查询trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                TAgentInfo.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致
//获取结果对象列表
        List<TAgentInfo> demos = data.getDatas();
        //获取总记录数
        long totalSize = data.getTotalSize();
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

# 从多表中检索一个文档

```java
public TerminalMessages getTerminalBase(String batchUuid) {   
    Map<String,String> params = new HashMap<>(1);   params.put("batchUuid",batchUuid);   
    return clientInterface.searchObject("terminalbase-*/_search","getTerminalBase",params,TerminalMessages.class);
}
```

对应的dsl-getTerminalBase定义

```xml
<!--
            根据请求id获取服务端报文
        -->
    <property name="getTerminalBase">
        <![CDATA[{
            "query": {
                "bool": {
                    "filter": [
                        {
                            "term":{
                                "batchUuid":#[batchUuid]
                            }
                        }
                    ]
                }
            },
            "size":1
        }]]>
    </property>
```

# 通过count统计索引文档数量

## count by condition

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
long count = clientUtil.count("trace1",//查询操作，查询indices trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria);//查询条件封装对象
```

## count all documents

```java
ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
long count  = clientInterface.countAll("trace");
```

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>
<img src="images/alipay.png"  height="200" width="200">

