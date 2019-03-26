# RestClientUtil和ConfigRestClientUtil区别说明

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

# 区别

RestClientUtil and ConfigRestClientUtil have essentially the same API, with the following differences:

**RestClientUtil** directly executes the DSL defined in the code.

**ConfigRestClientUtil** gets the DSL defined in the configuration file by the DSL name and executes it. 

# RestClientUtil example

## sql查询

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();//define an instanceof RestClientUtil,It's single instance, multithreaded secure.  

List<Map> json = clientUtil.sql(Map.class,"{\\"query\\": \\"SELECT * FROM demo\\"}");  
```

## dsl查询

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
String dsl = "{\"size\":1000,\"query\": {\"match_all\": {}},\"sort\": [\"_doc\"]}";
//执行查询，demo为索引表，_search为检索操作action
ESDatas<Demo> esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
      clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
            dsl,//dsl语句
            Demo.class);//返回的文档封装对象类型
```

**注意**：**bboss本身没有提供querybuilder工具，可以借助第三方工具构建好了后转换为dsl，然后调用RestClientUtil的api来执行即可**



# ConfigRestClientUtil example

## sql查询

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//define an instanceof ConfigRestClientUtil,It's single instance, multithreaded secure.  
Map params = new HashMap();  
params.put("channelId",1);  
List<Map> json = clientUtil.sql(Map.class,"sqlQuery",params);  
```

the dsl config file:sql.xml ,We can define a lot of DSLs in the configuration file

```xml
<properties>  
    <!--  
        sql query  
    -->  
    <property name="sqlQuery">  
        <![CDATA[  
         {"query": "SELECT * FROM dbclobdemo where channelId=#[channelId]"}  
        ]]>  
    </property>  
</properties>  
```

## dsl查询

```java
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

      long count = clientUtil.count("demo/_search","searchDatas",//esmapper/demo.xml中定义的dsl语句
            params);//变量参数
      //获取结果对象列表，最多返回1000条记录
      List<Demo> demos = esDatas.getDatas();

//    String json = clientUtil.executeRequest("demo/_search",//demo为索引表，_search为检索操作action
//          "searchDatas",//esmapper/demo.xml中定义的dsl语句
//          params);

//    String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);
      //获取总记录数
      long totalSize = esDatas.getTotalSize();
      System.out.println(totalSize);
```

dsl定义：

```xml
<!--
    一个简单的检索dsl,中有四个变量
    applicationName1
    applicationName2
    startTime
    endTime
    通过map传递变量参数值

    变量语法参考文档：
-->
<property name="searchDatas">
    <![CDATA[{
        "query": {
            "bool": {
                "filter": [
                    {  ## 多值检索，查找多个应用名称对应的文档记录
                        "terms": {
                            "applicationName.keyword": [#[applicationName1],#[applicationName2]]
                        }
                    },
                    {   ## 时间范围检索，返回对应时间范围内的记录，接受long型的值
                        "range": {
                            "agentStarttime": {
                                "gte": #[startTime],##统计开始时间
                                "lt": #[endTime]  ##统计截止时间
                            }
                        }
                    }
                ]
            }
        },
        ## 最多返回1000条记录
        "size":1000
    }]]>
</property>
```