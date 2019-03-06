# Elasticsearch Delete/UpdateByQuery案例

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

Elasticsearch Delete/UpdateByQuery案例分享

本文涉及技术点：

1. DeleteByQuery/UpdateByQuery
2. Count文档统计Api



# 1.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置es客户端



# 2.编写创建索引表和初始化数据方法

创建索引表和初始化数据的组件[DocumentCRUD ](https://gitee.com/bboss/eshelloword/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)实现本文不做重点介绍，请访问[视频教程](https://pan.baidu.com/s/1kXjAOKn)了解：

```
/**
 * 创建索引表并导入测试数据
 */
public void initIndiceAndData(){
   DocumentCRUD documentCRUD = new DocumentCRUD();
   documentCRUD.testCreateIndice();//创建索引表
   documentCRUD.testBulkAddDocument();//导入测试数据
}
```



# 3.定义DeleteByQuery/UpdateByQuery对应的Dsl脚本

新建配置文件-esmapper/byquery.xml

```
<properties>
    <!--
        updateByquery
        deleteByquery
        dsl配置之文件
    -->
    <property name="updateByQuery">
        <![CDATA[
         {
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
            }
        }
        ]]>
    </property>

    <property name="deleteByQuery">
        <![CDATA[
         {
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
            }
        }
        ]]>
    </property>
</properties>
```



# 4.实现DeleteByQuery功能

定义实现DeleteByQuery功能的方法

```
    /**
	 * 验证DeleteByQuery功能
	 * @throws ParseException
	 */
	public void deleteByQuery() throws ParseException {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/byquery.xml");
		//设定DeleteByQuery查询条件,通过map传递变量参数值,key对于dsl中的变量名称
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
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00").getTime());
		params.put("endTime",new Date().getTime());
		//通过count api先查询数据是否存在
		long totalSize = clientUtil.count("demo","deleteByQuery",params);

		System.out.println("---------------------------------删除前查询，验证数据是否存在:totalSize="+totalSize);
		if(totalSize > 0) {//如果有数据，则通过by query删除之，为了实时查看删除效果，启用了强制刷新机制
			//执行DeleteByQuery操作
			String result = clientUtil.deleteByQuery("demo/_delete_by_query?refresh", "deleteByQuery", params);
			System.out.println(result);

			//删除后再次查询，验证数据是否被删除成功
			totalSize = clientUtil.count("demo","deleteByQuery",params);
			System.out.println("---------------------------------删除后再次查询，验证数据是否被删除:totalSize="+totalSize);
		}

	}
```



# 5.执行DeleteByQuery junit单元测试方法

```
    @Test
	public void testDeleteByQuery() throws ParseException {
		DeleteUdateByQuery deleteUdateByQuery = new DeleteUdateByQuery();
		deleteUdateByQuery.initIndiceAndData();//初始化索引表和测试数据
		deleteUdateByQuery.deleteByQuery();//执行deleteByquery
	}
```



# 6.定义updatebyquery方法

定义两个updatebyquery方法，一个带query dsl，一个不带query dsl：

```
    /**
	 * 验证simpleUpdateByQuery功能
	 */
	public void simpleUpdateByQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		String result = clientUtil.updateByQuery("demo/_update_by_query?conflicts=proceed");
		System.out.println("******************simpleUpdateByQuery result:"+result);
	}

	/**
	 * 验证带查询条件UpdateByQuery功能
	 * @throws ParseException
	 */
	public void updateByQueryWithCondition() throws ParseException {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/byquery.xml");
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
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00").getTime());
		params.put("endTime",new Date().getTime());
		String result = clientUtil.updateByQuery("demo/_update_by_query?conflicts=proceed","updateByQuery",params);
		System.out.println("******************updateByQueryWithCondition result:"+result);

	}
```



# 7.执行updatebyquery junit单元测试方法

```
@Test
public void testUpdateByQuery() throws ParseException {
   DeleteUdateByQuery deleteUdateByQuery = new DeleteUdateByQuery();
   deleteUdateByQuery.initIndiceAndData();//初始化索引表和测试数据
   deleteUdateByQuery.simpleUpdateByQuery();
   deleteUdateByQuery.updateByQueryWithCondition();
}
```



# 8.参考文档

<https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html>

<https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html>

本文示例代码对应的源码工程地址：

<https://gitee.com/bboss/eshelloword>

本文对应的java文件：

<https://gitee.com/bboss/eshelloword/blob/master/src/main/java/org/bboss/elasticsearchtest/byquery/DeleteUdateByQuery.java>

<https://gitee.com/bboss/eshelloword/blob/master/src/test/java/org/bboss/elasticsearchtest/crud/DeleteUdateByQueryTest.java>

本文对应的dsl配置文件：

<https://gitee.com/bboss/eshelloword/blob/master/src/main/resources/esmapper/byquery.xml>