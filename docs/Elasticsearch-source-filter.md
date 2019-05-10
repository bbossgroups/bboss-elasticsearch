# Elasticsearch source filter检索案例

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

Elasticsearch source filter检索案例分享



# 1.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置es客户端

先理解一下es source filter作用:通过在_source中指定includes和excludes，控制查询结果中哪些source字段要返回、哪些source字段不需要返回，source filter的dsl定义语法如下：

```json
{
    "_source": {
        "includes": [ "obj1.*", "obj2.*" ],
        "excludes": [ "*.description" ]
    },
    "query" : {
        "term" : { "user" : "kimchy" }
    }
}
```

本文演示动态从外部传入includes和excludes实现source filter功能，适用于includes和excludes动态变化的source filter场景。



# 2.定义source filter dsl语句

首先，在[DocumentCRUD ](https://gitee.com/bboss/eshelloword/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)案例对应的dsl配置文件esmapper/demo.xml中添加searchSourceFilter：

```xml
    <property name="searchSourceFilter">
        <![CDATA[{
        #if($includes || $excludes) ## 只有指定了includes或者excludes才需要添加source filter
            "_source": {
                #if($includes ) ##设置includes filter
                    "includes": [
                        #foreach($include in $includes)
                             #if($velocityCount > 0),#end "$include"
                        #end
                    ]
                    #if($excludes ),#end ##如果还存在排斥字段，则需要加一个逗号
                #end
                #if($excludes )  ##设置excludes filter
                    "excludes": [
                        #foreach($exclude in $excludes)
                             #if($velocityCount > 0),#end "$exclude"
                        #end
                    ]
                #end
            },
        #end
            "query": {
                "bool": {
                    "filter": [
                    #if($applicationNames && $applicationNames.size() > 0) ##只有传递了需要检索的应用名称集合，才需要添加下面的条件
                        {  ## 多值检索，查找多个应用名称对应的文档记录
                            "terms": {
                                "applicationName.keyword":[
                                    #foreach($applicationName in $applicationNames)
                                         #if($velocityCount > 0),#end "$applicationName"
                                    #end
                                ]
                            }
                        },
                    #end
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



# 3.定义source filter检索方法

在[DocumentCRUD ](https://gitee.com/bboss/eshelloword/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)中增加方法：

```java
    /**
	 * SourceFilter检索文档
	 * @throws ParseException
	 */
	public void testSearchSourceFilter() throws ParseException {
		//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
		//设定查询条件,通过map传递变量参数值,key对于dsl中的变量名称
		//dsl中有四个变量
		//        applicationName1
		//        applicationName2
		//        startTime
		//        endTime
		Map<String,Object> params = new HashMap<String,Object>();
		//设置applicationName1和applicationName2两个变量的值，将多个应用名称放到list中，通过list动态传递参数
		List<String> datas = new ArrayList<String>();
		datas.add("blackcatdemo2");
		datas.add("blackcatdemo3");
		params.put("applicationNames",datas);

		List<String> includes = new ArrayList<String>(); //定义要返回的source字段
		includes.add("agentStarttime");
		includes.add("applicationName");
		params.put("includes",includes);

		List<String> excludes = new ArrayList<String>(); //定义不需要返回的source字段
		excludes.add("contentbody");
		excludes.add("demoId");
		params.put("excludes",excludes);


		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//设置时间范围,时间参数接受long值
		//说明： 也可以接受日期类型，如果传入Date类型的时间并且通过map传参，则需要手动进行日期格式转换成字符串格式的日期串，通过entity传参则不需要
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00").getTime());
		params.put("endTime",new Date().getTime());

		//执行查询，demo为索引表，_search为检索操作action
		ESDatas<Demo> esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
				clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
						"searchSourceFilter",//esmapper/demo.xml中定义的dsl语句
						params,//变量参数
						Demo.class);//返回的文档封装对象类型
		//获取总记录数
		long totalSize = esDatas.getTotalSize();
		System.out.println(totalSize);
		//获取结果对象列表，最多返回1000条记录
		List<Demo> demos = esDatas.getDatas();

		//以下是返回原始检索json报文检索代码
//		String json = clientUtil.executeRequest("demo/_search",//demo为索引表，_search为检索操作action
//				"searchSourceFilter",//esmapper/demo.xml中定义的dsl语句
//				params);

	}
```



# 4.验证source filter功能

通过junit执行测试方法验证source filter功能:

```java
    @Test
	public void testSearchSourceFilter() throws ParseException {
		DocumentCRUD documentCRUD = new DocumentCRUD();
		//删除/创建文档索引表
		documentCRUD.testCreateIndice();
		//添加/修改单个文档
		documentCRUD.testAddAndUpdateDocument();
		//批量添加文档
		documentCRUD.testBulkAddDocument();
		//不带sourceFilter检索文档
		documentCRUD.testSearch();
		//批量修改文档
		documentCRUD.testBulkUpdateDocument();

		//带sourcefilter的文档检索操作
		documentCRUD.testSearchSourceFilter();
	}
```

执行结果：

![img](https://static.oschina.net/uploads/space/2018/0418/105405_2bXT_94045.png)



# 5.参考文档

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-source-filtering.html>

案例对应源码工程：

<https://gitee.com/bboss/eshelloword>

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>

<img src="images/alipay.png"  height="200" width="200">

