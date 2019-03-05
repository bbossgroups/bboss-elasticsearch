## Elasticsearch关键词高亮检索

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

# 1.准备工作

参考文档《[集成Elasticsearch Restful API案例](common-project-with-bboss.md)》导入和配置es客户端



# 2.创建索引表和初始化数据

通过组件[DocumentCRUD ](https://gitee.com/bbossgroups/eshelloword-booter/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)来创建索引表和初始化数据，[DocumentCRUD ](https://gitee.com/bbossgroups/eshelloword-booter/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)组件实现本文不做重点介绍：

```
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

在文件[esmapper/demo.xml](https://gitee.com/bbossgroups/eshelloword-booter/blob/master/src/main/resources/esmapper/demo.xml)中增加testHighlightSearch配置：

```
    <!--
        一个简单的检索dsl,中有四个变量
        name 全文检索字段
        startTime
        endTime
        通过map传递变量参数值

        变量语法参考文档：https://my.oschina.net/bboss/blog/1556866
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

```
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

<https://gitee.com/bbossgroups/eshelloword-booter>



# 6.参考文档

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-highlighting.html>

<https://my.oschina.net/bboss/blog/1801273>



# 7.开发交流

**elasticsearch技术交流群：166471282**

**elasticsearch微信公众号：**

**![bboss微信公众号：bbossgroups](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)**