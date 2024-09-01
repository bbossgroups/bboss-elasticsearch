# Elasticsearch文档增删改查操作介绍

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

本文介绍通过bboss实现Elasticsearch索引文档添加/修改/查询/删除/批量删除功能。
# 准备工作
先在项目中导入bboss，导入bboss请参考文档：[Quick Start](https://esdoc.bbossgroups.com/#/quickstart)

bboss操作Elasticsearch都是通过ClientInterface接口，spring boot项目环境和非spring boot项目环境获取ClientInterface接口实例的方法不一样，分别介绍一下：
spring boot环境：

```java
    @Autowired
    private BBossESStarter bbossESStarter;//代码中注入加载spring boot配置的BBossESStarter
    //通过bbossESStarter获取ClientInterface接口实例：Create a client tool to load configuration files, single instance multithreaded security
    ClientInterface configClientUtil = bbossESStarter.getConfigRestClient(mappath);
    //通过bbossESStarter获取ClientInterface接口实例：Build a create/modify/get/delete document client object, single instance multi-thread security
    ClientInterface clientUtil = bbossESStarter.getRestClient();    
```

非spring boot环境：

```java
//创建加载配置文件的客户端实例，单实例多线程安全
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo.xml");
//创建直接操作dsl的客户端实例，单实例多线程安全
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil() ;
```

下面以非spring boot环境进行介绍。

# 1. 添加修改文档

## 1.1 添加bean记录

```java

TAgentInfo agentInfo = new TAgentInfo() ;
agentInfo.setIp("192.168.137.1")；//ip属性作为文档唯一标识，根据ip值对应的索引文档存在与否来决定添加或者修改操作
//设置地理位置坐标
agentInfo.setLocation("28.292781,117.238963");
//设置其他属性
。。。。
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();	
//添加/修改文档，如果文档id存在则修改，不存在则插入
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

## 1.2 添加map记录

```java
//创建创建/修改/获取/删除文档的客户端对象，单实例多线程安全
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
//构建一个对象，日期类型，字符串类型属性演示
Map demo = new LinkedHashMap();
demo.put("demoId","2");//文档id，唯一标识，@PrimaryKey注解标示,如果demoId已经存在做修改操作，否则做添加文档操作
demo.put("agentStarttime",new Date());
demo.put("applicationName","blackcatdemo2");
demo.put("contentbody","this-is content body2");
demo.put("agentStarttime",new Date());
demo.put("name","|刘德华");
demo.put("orderId","NFZF15045871807281445364228");
demo.put("contrastStatus",2);

//向固定index demo添加或者修改文档,如果demoId已经存在做修改操作，否则做添加文档操作，返回处理结果
/**
 //通过@ESId注解的字段值设置文档id
 String response = clientUtil.addDocument("demo"//索引表

 demo);
 */
/**
 //直接指定文档id
 String response = clientUtil.addDocumentWithId("demo",//索引表

 demo,2l);
 */
//强制刷新
ClientOptions addOptions = new ClientOptions();
addOptions.setIdField("orderId");
//如果orderId对应的文档已经存在则更新，不存在则插入新增
String response = clientUtil.addDocument("demo",//索引表
      demo,addOptions);
```

上面的实例中，orderId作为文档标识保存，同时也会作为一个文档字段保存，可以通过以下配置，控制不将orderId字段作为普通字段保存：

```java
addOptions.setIdField("orderId");
addOptions.setPersistMapDocId(false);//控制不将orderId字段作为普通字段保存
```



# 2. 批量添加修改文档

## 2.1 批量添加bean记录

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
//批量添加/修改文档，如果文档id存在则修改，不存在则插入
String response = clientUtil.addDocuments("demo",//索引表
"demo",//索引类型
demos);
```

## 2.2 批量添加map记录

```java
/**
 * 批量添加map记录
 * @throws ParseException
 */
public void testAddAndUpdateMapDocuments() throws ParseException {
   //创建创建/修改/获取/删除文档的客户端对象，单实例多线程安全
   ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
   List<Map> datas = new ArrayList<Map>();
   //构建一个对象，日期类型，字符串类型属性演示
   Map demo = new LinkedHashMap();
   demo.put("demoId","2");//文档id，唯一标识，@PrimaryKey注解标示,如果demoId已经存在做修改操作，否则做添加文档操作
   demo.put("agentStarttime",new Date());
   demo.put("applicationName","blackcatdemo2");
   demo.put("contentbody","this-is content body2");
   demo.put("agentStarttime",new Date());
   demo.put("name","|刘德华");
   demo.put("orderId","NFZF15045871807281445364228");
   demo.put("contrastStatus",2);
   datas.add(demo);

   demo = new LinkedHashMap();
   demo.put("demoId","3");//文档id，唯一标识，@PrimaryKey注解标示,如果demoId已经存在做修改操作，否则做添加文档操作
   demo.put("agentStarttime",new Date());
   demo.put("applicationName","blackcatdemo3");
   demo.put("contentbody","this-is content body3");
   demo.put("agentStarttime",new Date());
   demo.put("name","张三");
   demo.put("orderId","NFZF15045871807281445364228");
   demo.put("contrastStatus",3);
   datas.add(demo);


   demo = new LinkedHashMap();
   demo.put("demoId","4");//文档id，唯一标识，@PrimaryKey注解标示,如果demoId已经存在做修改操作，否则做添加文档操作
   demo.put("agentStarttime",new Date());
   demo.put("applicationName","blackcatdemo4");
   demo.put("contentbody","this-is content body4");
   demo.put("agentStarttime",new Date());
   demo.put("name","李四");
   demo.put("orderId","NFZF15045871807281445364229");
   demo.put("contrastStatus",4);
   datas.add(demo);

   //向固定index demo添加或者修改文档,如果demoId已经存在做修改操作，否则做添加文档操作，返回处理结果
   /**
    //通过@ESId注解的字段值设置文档id
    String response = clientUtil.addDocument("demo"//索引表

    demo);
    */
   /**
    //直接指定文档id
    String response = clientUtil.addDocumentWithId("demo",//索引表

    demo,2l);
    */
   //强制刷新
   ClientOptions addOptions = new ClientOptions();
   addOptions.setIdField("orderId");
   //如果orderId对应的文档已经存在则更新，不存在则插入新增
   String response = clientUtil.addDocuments("demo",//索引表
         datas,addOptions);


}
```

上面的实例中，orderId作为文档标识保存，同时也会作为一个文档字段保存，可以通过以下配置，控制不将orderId字段作为普通字段保存：

```java
addOptions.setIdField("orderId");
addOptions.setPersistMapDocId(false);//控制不将orderId字段作为普通字段保存
```



## 2.3 控制批量添加响应报文内容

为了提升性能，并没有把所有响应数据都返回，过滤掉了部分数据，可以自行设置FilterPath进行控制
```java
		ClientOptions clientOptions = new ClientOptions();
		clientOptions.setRefreshOption("refresh=false");//为了测试效果,能够实时查看数据，启用强制刷新机制，可是修改为"refresh=true"
		//为了提升性能，并没有把所有响应数据都返回，过滤掉了部分数据，可以自行设置FilterPath进行控制
		clientOptions.setFilterPath("took,errors,items.*.error");
		//批量添加或者修改2万个文档，将两个对象添加到索引表demo中，批量添加2万条记录耗时1.8s，
		String response = clientUtil.addDocuments("demo",//索引表
									demos,//批量处理数据集合
									clientOptions);

```
# 3. 根据文档id获取文档对象

```java
//根据文档id获取文档对象，返回Demo对象
Demo demo = clientUtil.getDocument("demo",//索引表
     // "demo",//索引类型,es 7以下版本才需要设定
      "2",//文档id
      Demo.class);
//根据文档id获取文档对象，返回Map对象
Map map = clientUtil.getDocument("demo",//索引表
     // "demo",//索引类型,es 7以下版本才需要设定
      "2",//文档id
      Map.class);      
```

# 4. @ESIndex注解使用

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



## 4.1 api清单

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

## 4.2 定义带ESIndex注解的实体

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

## 4.3 api使用

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

# 5. 检索文档



封装查询条件的对象TraceExtraCriteria定义：

```java
package org.frameworkset.elasticsearch.byquery;

import java.util.Date;
import java.util.List;


public class TraceExtraCriteria {
  private List<String> searchFields;
  private Date startTime;
  private Date endTime;
  private String application;
  private String queryCondition;
  public List<String> getChannelApplications() {
    return channelApplications;
  }
  public String getApplication(){
      return application;
  }
  public void setApplication(String application){
      this.application = application;
  }
   public String getQueryCondition(){
      return queryCondition;
  }
  public void setQueryCondition(String queryCondition){
      this.queryCondition = queryCondition;
  }  
  public void setSearchFields(List<String> searchFields) {
    this.searchFields = searchFields;
  }
  public List<String>  getSearchFields() {
    return this.searchFields ;
  }
  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }
}
```

检索文档对应的dsl语句queryServiceByCondition定义：esmapper/estrace/ESTracesMapper.xml

```xml
 <properties>
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
</properties>
```

执行检索的java代码

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
        List<String> searchFields = new ArrayList<>();
        searchFields.add("aaa");
         searchFields.add("bbb");
        traceExtraCriteria.setSearchFields(searchFields);
        traceExtraCriteria.setApplication("test");
		traceExtraCriteria.setStartTime(new Date(1516304868072l));
		traceExtraCriteria.setEndTime(new Date(1516349516377l));
        traceExtraCriteria.setQueryCondition("asdfasfd");
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



# 6. 返回索引元数据的检索操作

检索文档的时候，除了返回要检索的业务数据，同时也可以返回索引元数据信息，可以通过以下方式返回索引文档的元数据信息。

1.对象类继承父类ESBaseData：

```java
org.frameworkset.elasticsearch.entity.ESBaseData
```

返回高亮检索和父子查询信息也可以通过集成ESBaseData实现。

2.如果只需要返回文档id则继承类ESId：

```java
org.frameworkset.elasticsearch.entity.ESId
```

3.元数据注解

ESBaseData和ESId使用比较方便，但是如果对象本身的属性很容易和这两个父类中的属性产生同名冲突的问题，这时候我们可以采用元数据注解来实现索引文档元数据的注入，参考文档：

[元数据使用介绍](https://esdoc.bbossgroups.com/#/client-annotation?id=_2%e5%85%83%e6%95%b0%e6%8d%ae%e6%b3%a8%e8%a7%a3)

返回类型为Map对象时，如需返回索引文档元数据，则可以将返回类型指定为继承HashMap的子类 MetaMap

```java
org.frameworkset.elasticsearch.entity.MetaMap
```

## 6.1 ESBaseData和ESId使用实例

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

ESId使用

```java
/**
 * 测试实体，可以从ESId对象继承id属性，检索时会将文档的一下文档id设置到对象实例中
 */
public class Demo extends ESId {
   private Object dynamicPriceTemplate;
   //设定文档标识字段
   
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

## 6.2 带元数据的Map对象MetaMap使用

检索返回对象类型为Map时，如果需要同时返回元数据，则可以将返回类型指定为继承LinkedHashMap的子类 org.frameworkset.elasticsearch.entity.MetaMap，使用示例如下：

### 6.2.2 单文档检索

```java
    //创建批量创建文档的客户端对象，单实例多线程安全
        ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
        //单文档检索
        MetaMap newDemo = clientUtil.getDocument("demo",//索引表
                "demo",//索引类型
                "1",//文档id
                MetaMap.class
        );//指定返回对象类型为MetaMap
        //打印metamap数据和返回的文档元数据信息
            System.out.println(newDemo);
            System.out.println("getId:"+newDemo.getId());//文档_id
            System.out.println("getIndex:"+newDemo.getIndex());//索引名称
            System.out.println("getNode:"+newDemo.getNode());
            System.out.println("getShard:"+newDemo.getShard());
            System.out.println("getType:"+newDemo.getType());//索引类型
            System.out.println("getExplanation:"+newDemo.getExplanation());
            System.out.println("getFields:"+newDemo.getFields());
            System.out.println("getHighlight:"+newDemo.getHighlight());//高亮信息
            System.out.println("getInnerHits:"+newDemo.getInnerHits());
            System.out.println("getNested:"+newDemo.getNested());
            System.out.println("getPrimaryTerm:"+newDemo.getPrimaryTerm());
            System.out.println("getScore:"+newDemo.getScore());//索引评分
            System.out.println("getSeqNo:"+newDemo.getSeqNo());
            System.out.println("getVersion:"+newDemo.getVersion());//索引文档版本号
            System.out.println("getParent:"+newDemo.getParent());//父文档_id
            System.out.println("getRouting:"+newDemo.getRouting());
            System.out.println("getSort:"+newDemo.getSort());
            System.out.println("isFound:"+newDemo.isFound());
            System.out.println("matchedQueries:"+newDemo.getMatchedQueries());

       
```

### 6.2.5 列表检索

```java
		Map params = new HashMap();
        params.put("size",100);
        params.put("name","jack");
//列表检索
        ESDatas<MetaMap> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<MetaMap>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("trace-*/_search",//查询操作，查询indices trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                parmas,//查询条件,Map<key,value>
                                MetaMap.class);//指定返回对象类型为MetaMap
//获取结果对象列表
        List<MetaMap> demos = data.getDatas();
        //获取总记录数
        long totalSize = data.getTotalSize();
        //遍历列表，并从MetaMap中获取检索元数据
        for(int i = 0; demos != null && i < demos.size(); i ++){
              //单文档检索
            MetaMap newDemo = demos.get(i);
            //打印metamap数据和返回的文档元数据信息
            System.out.println(newDemo);
            System.out.println("getId:"+newDemo.getId());//文档_id
            System.out.println("getIndex:"+newDemo.getIndex());//索引名称
            System.out.println("getNode:"+newDemo.getNode());
            System.out.println("getShard:"+newDemo.getShard());
            System.out.println("getType:"+newDemo.getType());//索引类型
            System.out.println("getExplanation:"+newDemo.getExplanation());
            System.out.println("getFields:"+newDemo.getFields());
            System.out.println("getHighlight:"+newDemo.getHighlight());//高亮信息
            System.out.println("getInnerHits:"+newDemo.getInnerHits());
            System.out.println("getNested:"+newDemo.getNested());
            System.out.println("getPrimaryTerm:"+newDemo.getPrimaryTerm());
            System.out.println("getScore:"+newDemo.getScore());//索引评分
            System.out.println("getSeqNo:"+newDemo.getSeqNo());
            System.out.println("getVersion:"+newDemo.getVersion());//索引文档版本号
            System.out.println("getParent:"+newDemo.getParent());//父文档_id
            System.out.println("getRouting:"+newDemo.getRouting());
            System.out.println("getSort:"+newDemo.getSort());
            System.out.println("isFound:"+newDemo.isFound());
             System.out.println("matchedQueries:"+newDemo.getMatchedQueries());
        }
```



## 6.3 索引文档元数据注解使用

ESBaseData和ESId使用比较方便，但是如果对象本身的属性很容易和这两个父类中的属性产生同名冲突的问题，这时候我们可以采用元数据注解来实现索引文档元数据的注入，参考文档：

[元数据注解使用](https://esdoc.bbossgroups.com/#/client-annotation?id=_2%e5%85%83%e6%95%b0%e6%8d%ae%e6%b3%a8%e8%a7%a3)

# 7. 通过URL参数检索文档

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

# 8. 执行多表查询操作

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

# 9. 从多表中检索一个文档

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

# 10.从所有索引中检索数据

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
//执行查询操作
ESDatas<Map> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<Map>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("_search",//查询操作，查询所有索引中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria,//查询条件封装对象
                                Map.class);//指定返回的map对象类型
//获取结果对象列表
        List<Map> demos = data.getDatas();
        //获取总记录数
        long totalSize = data.getTotalSize();
```



# 11. 通过count统计索引文档数量

## 11.1 count by condition

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/ESTracesMapper.xml");
long count = clientUtil.count("trace1",//查询操作，查询indices trace-*中符合条件的数据
                                "queryServiceByCondition",//通过名称引用配置文件中的query dsl语句
                                traceExtraCriteria);//查询条件封装对象
```

## 11.2 count all documents

```java
ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
long count  = clientInterface.countAll("trace");
```

# 12.修改文档

```java

Demo demo = new Demo();//定义第二个对象
		demo.setDemoId(3l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo3");
		demo.setContentbody("四大\"天王，这种文化很好，中华人民共和国");
		demo.setName("张学友\t\n\r");
		demo.setOrderId("NFZF15045871807281445364228");
		demo.setContrastStatus(2);
ClientOptions updateOptions = new ClientOptions();
	 
		updateOptions.setDetectNoop(false)
				.setDocasupsert(false)
				.setReturnSource(false)
//				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("demoId")
//				.setVersion(2).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
//				.setIfPrimaryTerm(2l)
//				.setIfSeqNo(3l)
//				.setPipeline("1")
				.setEsRetryOnConflict(2)
				.setTimeout("100s")
		.setWaitForActiveShards(1)
		.setRefresh("true");
				//.setMasterTimeout("10s")
				;
		//更新不存在的文档
		String response = clientUtil.updateDocument("demo",//索引表
				"demo",//索引类型

				demo
		,updateOptions);
		System.out.println(response);
```

# 13.批量修改文档

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
ClientOptions updateOptions = new ClientOptions();
	 
		updateOptions
            //.setDetectNoop(false)
				//.setDocasupsert(false)
				//.setReturnSource(false)
//				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("demoId")
//				.setVersion(2).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
//				.setIfPrimaryTerm(2l)
//				.setIfSeqNo(3l)
//				.setPipeline("1")
				//.setEsRetryOnConflict(2)
				//.setTimeout("100s")
		//.setWaitForActiveShards(1)
		.setRefresh("true");
//批量修改文档
String response = clientUtil.updateDocuments("demo",//索引表
"demo",//索引类型
demos,updateOptions);
```

# 14.删除文档

## 14.1 删除单条文档

ES 7以下版本根据文档id删除文档

```java
	ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
//     //删除文档
    clientUtil.deleteDocument("demo",//索引表
          "demo",//索引类型
          "2");//文档id
```

ES 7+版本根据文档id删除文档，不需要索引类型
```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
//删除文档
    clientUtil.deleteDocumentNew("demo",//索引表         
          "2");//文档id
```

## 14.2 批量删除文档
ES 7+版本根据文档id数组批量删除文档，不需要索引类型

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
//批量删除文档
		clientUtil.deleteDocuments("demo",//索引表
				new String[]{"2","3"});//批量删除文档ids
```
ES 7以下版本根据文档id数组批量删除文档

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();	
//批量删除文档
		clientUtil.deleteDocuments("demo",//索引表
				"demo",//索引类型
				new String[]{"2","3"});//批量删除文档ids
```

## 14.3 通过deleteByQuery删除文档

[DeleteByQuery/UpdateByQuery对应的Dsl脚本](https://esdoc.bbossgroups.com/#/update-delete-byquery?id=_3定义deletebyqueryupdatebyquery对应的dsl脚本)

# 15 一组便捷查询工具方法使用示例
```java
    @Test
                
                	/**
                	 * 根据属性获取文档json报文
                	 * @param indexName
                	 * @param fieldName
                	 * @param blackcatdemo2
                	 * @return
                	 * @throws ElasticSearchException
                	 */
                	public void getDocumentByField() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		String document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2");
                	}
                	@Test
                	public void getDocumentByField1() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map<String,Object> options = new HashMap<String, Object>();
                		String document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2",options);
                	}
                
                	/**
                	 * 根据属性获取文档json报文
                	 * @return
                	 * @throws ElasticSearchException
                	 */
                	@Test
                	public void getDocumentByFieldLike3() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		String document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2");
                	}
                
                	@Test
                	public void getDocumentByFieldLike1(){
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map<String,Object> options = new HashMap<String, Object>();
                		String document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2",options);
                	}
                	@Test
                	public void getDocumentByField2() throws ElasticSearchException{
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2",Map.class);
                	}
                	/**
                	 * 根据属性获取type类型文档对象
                
                	 * @return
                	 * @throws ElasticSearchException
                	 */
                	@Test
                	public void getDocumentByField3() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map<String,Object> options = new HashMap<String, Object>();
                		Map document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2",Map.class,options);
                	}
                
                	@Test
                	public void getDocumentByFieldLike() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,null);
                	}
                
                	/**
                	 * 根据属性获取type类型文档对象
                
                	 * @return
                	 * @throws ElasticSearchException
                	 */
                	@Test
                	public void getDocumentByFieldLike2() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map<String,Object> options = new HashMap<String, Object>();
                		Map document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,null);
                	}
  
  
  ​              
  ​              
  ​              
  ​              	@Test
  ​              	public void searchListByField() {
  ​              		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
  ​              		ESDatas<Map> documents = clientInterface.searchListByField("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10);
  ​              	}
  ​              
                	/**
                	 * 根据属性获取type类型文档对象
                
                	 * @return
                	 * @throws ElasticSearchException
                	 */
                	@Test
                	public void searchListByField1() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map<String,Object> options = new HashMap<String, Object>();
                		ESDatas<Map> documents = clientInterface.searchListByField("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10,options);
                	}
  
  
  ​              
  ​              	@Test
  ​              	public void searchListByFieldLike1() {
  ​              		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
  ​              		ESDatas<Map> documents = clientInterface.searchListByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10);
  ​              	}
  ​              
                	/**
                	 * 根据属性获取type类型文档对象
                
                	 * @return
                	 * @throws ElasticSearchException
                	 */
                	@Test
                	public void searchListByFieldLike() {
                		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
                		Map<String,Object> options = new HashMap<String, Object>();
                		ESDatas<Map> documents = clientInterface.searchListByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10,options);
                	}          
    
```
# 16.msearch检索

通过msearch检索可以在一次http请求中执行多个检索请求，将每个请求的结果封装到List返回。

## 16.1 msearch dsl定义

```xml
<property name="msearchList" escapeQuoted="false"><![CDATA[
{ }
#"""{
  "size": #[size],
  "query": {
    "match" : {
        "logOperuser.keyword":#[user]
    }
  },
  "sort": [
    {"collecttime": "asc"}
  ]
}"""
{"index": "dbdemofull"}
{"query" : {"match_all" : {}}}
 ]]>
</property>
```

如果一个请求对应的dsl有多行，可以使用#"""query dsl"""进行封装，例如：

```json
#"""{
  "size": #[size],
  "query": {
    "match" : {
        "logOperuser.keyword":#[user]
    }
  },
  "sort": [
    {"collecttime": "asc"}
  ]
}"""
```

同时在xml元素property增加escapeQuoted="false"属性配置，避免解析后在单个dsl两边自动添加双引号

## 16.1 执行msearch 

返回ESDatas<MetaMap\>列表

```java
public void testMSearch(){
    ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo7.xml");

    Map params = new HashMap();
    params.put("size",100);
    params.put("user","admin");
    List<ESDatas<MetaMap>> esDatas = clientUtil.msearchList("dbdemofull/_msearch","msearchList",params,MetaMap.class);
    for(int i = 0; esDatas != null && i < esDatas.size(); i ++){
        ESDatas<MetaMap> esData = esDatas.get(i);
        List<MetaMap> metaMaps = esData.getDatas();//msearch 中每个search的记录集合
        logger.info(String.valueOf(metaMaps.size()));
    }


}
```

返回RestResponse列表

```java
public void testMSearchRestResponse(){
    ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo7.xml");

    Map params = new HashMap();
    params.put("size",100);
    params.put("user","admin");
    List<RestResponse> esDatas = clientUtil.msearch("dbdemofull/_msearch","msearchList",params,MetaMap.class);
    for(int i = 0; esDatas != null && i < esDatas.size(); i ++){
        RestResponse esData = esDatas.get(i);
        logger.info(SimpleStringUtil.object2json(esData));
    }


}
```

参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html



# 17. 开发交流
QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />



交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">





