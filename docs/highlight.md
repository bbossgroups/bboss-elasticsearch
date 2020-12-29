## Elasticsearch关键词高亮检索

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

# 1.准备工作

参考文档《[集成Elasticsearch Restful API案例](common-project-with-bboss.md)》导入和配置es客户端



# 2.创建索引表和初始化数据

通过组件[DocumentCRUD ](https://github.com/bbossgroups/eshelloword-booter/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)来创建索引表和初始化数据，[DocumentCRUD ](https://github.com/bbossgroups/eshelloword-booter/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)组件实现本文不做重点介绍：

```java
/**
 * 创建索引表，并导入高亮检索功能需要的测试数据
 */
public void initIndiceAndData(){
   DocumentCRUD documentCRUD = new DocumentCRUD();
   documentCRUD.testCreateIndice();
   documentCRUD.testBulkAddDocuments();
}
```



# 3.高亮检索功能实现



## 3.1 定义高亮检索dsl

在文件[esmapper/demo.xml](https://github.com/bbossgroups/bboss/eshelloword-booter/blob/master/src/main/resources/esmapper/demo.xml)中增加testHighlightSearch配置：

dsl中变量语法参考文档：[开发指南](https://esdoc.bbossgroups.com/#/development?id=_53-dsl%E9%85%8D%E7%BD%AE%E8%A7%84%E8%8C%83)

```xml
    <!--
        一个简单的检索dsl,中有四个变量
        name 全文检索字段
        startTime
        endTime
        通过map传递变量参数值

         
    -->
    <property name="testHighlightSearch">
        <![CDATA[{
            "query": {
                "bool": {
                    "filter": [
                        {   ## 时间范围检索，返回对应时间范围内的记录，接受long型的值
                            "range": {
                                "agentStarttime": {
                                    "gte": #[startTime],##统计开始时间
                                    "lt": #[endTime]  ##统计截止时间
                                }
                            }
                        }
                    ],
                    "must": [
                        #*
                        {
                            "query_string": {
                                "query": #[condition],
                                "analyze_wildcard": true
                            }
                        }
                        *#
                        {
                        ## 全文检索参考文档  https://www.elastic.co/guide/en/elasticsearch/reference/6.2/full-text-queries.html
                            "match_phrase" : {
                                "name" : {
                                    "query" : #[condition]
                                }
                            }
                        }
                    ]
                }
            },
            ## 最多返回1000条记录
            "size":1000,
            ## 高亮检索定义，参考文档：https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-highlighting.html
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
        }]]>
    </property>
```



## 3.2 编写高亮检索代码

创建检索类-org.bboss.elasticsearchtest.HighlightSearch

在其中定义以下方法

```java
	public void highlightSearch() throws ParseException {
		//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
		//设定查询条件,通过map传递变量参数值,key对于dsl中的变量名称
		//dsl中有三个变量
		//        condition
		//        startTime
		//        endTime
		Map<String,Object> params = new HashMap<String,Object>();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//设置时间范围,时间参数接受long值
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00"));
		params.put("endTime",new Date());
		params.put("condition","喜欢唱歌");//全文检索条件，匹配上的记录的字段值对应的匹配内容都会被高亮显示
		//执行查询，demo为索引表，_search为检索操作action
		ESDatas<Demo> esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
				clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
						"testHighlightSearch",//esmapper/demo.xml中定义的dsl语句
						params,//变量参数
						Demo.class);//返回的文档封装对象类型
		//获取总记录数
		long totalSize = esDatas.getTotalSize();
		System.out.println(totalSize);
		//获取结果对象列表，最多返回1000条记录
		List<Demo> demos = esDatas.getDatas();
		for(int i = 0; demos != null && i < demos.size(); i ++){//遍历检索结果列表
			Demo demo = demos.get(i);
			//记录中匹配上检索条件的所有字段的高亮内容
			Map<String,List<Object>> highLights = demo.getHighlight();
			Iterator<Map.Entry<String, List<Object>>> entries = highLights.entrySet().iterator();
			while(entries.hasNext()){
				Map.Entry<String, List<Object>> entry = entries.next();
				String fieldName = entry.getKey();
				System.out.print(fieldName+":");
				List<Object> fieldHighLightSegments = entry.getValue();
				for (Object highLightSegment:fieldHighLightSegments){
					/**
					 * 在dsl中通过<mark></mark>来标识需要高亮显示的内容，然后传到web ui前端的时候，通过为mark元素添加css样式来设置高亮的颜色背景样式
					 * 例如：
					 * <style type="text/css">
					 *     .mark,mark{background-color:#f39c12;padding:.2em}
					 * </style>
					 */
					System.out.println(highLightSegment);
				}
			}
		}
	}
```

## 3.3 检索结果中存放高亮关键字数据

高亮检索返回的索引字段高亮关键字信息被封装以Map<String,List<Object>>类型包含在每条索引文档中返回，bboss支持三种方式存放索引字段高亮关键字信息

**方式一 结果对象继承ESBaseData**

结果对象可以继承ESBaseData抽象类，字段高亮关键字信息将被自动设置到父类ESBaseData的属性：

```java
Map<String,List<Object>> highlight;
```

例如：本案例中的[Demo](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/Demo.java)对象

```java
public class Demo extends ESBaseData {
    。。。。。。。
}
```

**方式二 使用@ESMetaHighlight注解**

在对象中定义带@ESMetaHighlight注解类型为Map<String,List<Object>>的字段，例如：

```java
public class Demo  {
    @ESMetaHighlight //文档对应的高亮检索信息
	Map<String,List<Object>> highlights;
    。。。。。。。
}
```

方式 二优于方式一，不需为了高亮信息在对象中引入其他的元数据字段，同时也可以按照自己的要求命名高亮字段的名称，避免命名冲突

跟多元数据信息，可以参考文档：

[元数据注解](https://esdoc.bbossgroups.com/#/client-annotation?id=_2元数据注解)

**方式三 使用MetaMap**

如果我们既要使用Map对象来封装文档数据，又想返回高亮检索信息，那么可以使用MetaMap类型来替代Map类型，例如：


```java
		//设定查询条件,通过map传递变量参数值,key对于dsl中的变量名称
		//dsl中有三个变量
		//        condition
		//        startTime
		//        endTime
		Map<String,Object> params = new HashMap<String,Object>();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//设置时间范围,时间参数接受long值
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00"));
		params.put("endTime",new Date());
		params.put("condition","喜欢唱歌");//全文检索条件，匹配上的记录的字段值对应的匹配内容都会被高亮显示
//列表检索
        ESDatas<MetaMap> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<MetaMap>结果集和符合条件的总记录数totalSize
            = clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
                                "testHighlightSearch",//通过名称引用配置文件中的query dsl语句
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
            //打印metamap文档高亮信息
           
            System.out.println("getHighlight:"+newDemo.getHighlight());//高亮信息
            。。。。
        }
```

metamap的使用可以参考文档：

[带元数据的map对象metamap使用](https://esdoc.bbossgroups.com/#/document-crud?id=_62-带元数据的map对象metamap使用)

# 4.运行检索功能

定义junit测试用例：

```
import org.junit.Test;

import java.text.ParseException;

public class HighlightSearchTest {

	@Test
	public void testHighlightSearch() throws ParseException {
		HighlightSearch highlightSearch = new HighlightSearch();
		highlightSearch.initIndiceAndData();
		highlightSearch.highlightSearch();
	}
	@Test
	public void testHighlightSearchOther() throws ParseException {
		HighlightSearch highlightSearch = new HighlightSearch();
		highlightSearch.initIndiceAndData();
		highlightSearch.highlightSearchOther();
	}
}
```

在idea或者eclipse中运行测试用例即可，输出检索到的高亮内容信息如下：

```
name:刘德华<mark>喜</mark><mark>欢</mark><mark>唱</mark><mark>歌</mark>454
name:刘德华<mark>喜</mark><mark>欢</mark><mark>唱</mark><mark>歌</mark>488
name:刘德华<mark>喜</mark><mark>欢</mark><mark>唱</mark><mark>歌</mark>508
name:刘德华<mark>喜</mark><mark>欢</mark><mark>唱</mark><mark>歌</mark>518
```



# 5.完整的demo实例工程

<https://github.com/bbossgroups/eshelloword-booter>



# 6.参考文档

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-highlighting.html>

https://esdoc.bbossgroups.com/#/common-project-with-bboss

# 7.开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">


