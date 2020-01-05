# Elasticsearch search after分页查询案例分享

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

Elasticsearch search after分页查询案例分享



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
   documentCRUD.testBulkAddDocuments();//导入测试数据
}
```



# 3.定义searchAfter dsl配置文件

新建配置文件-esmapper/searchafter.xml

定义searchAfterDSL

```
<properties>
    <property name="searchAfterDSL">
        <![CDATA[{
            #*
               以demoId,_uid为searchAfter的分页条件 _uid值格式type#_id 由索引类型加#号加id拼接而成
               如果按照多个字段的值做searchAter分页，则用逗号分隔每个值
            *#
            #if($searchAfterId) ## 第一次检索的时候，没有searchAfterId变量，只有做翻页操作的时候才会传递代表分页起点的searchAfterId变量进来
                 "search_after": [#[demoId],"demo#[searchAfterId,quoted=false,lpad=#]"],
            #end
            "size": $pageSize, ##searchAfter分页每页最多显示的记录条数
            #*
                searchAfter分页检索时，必须用翻页字段作为排序字段，这里是demoId和_uid两个字段,如果是多个字段，则按照searchAfter的顺序来设置对应的排序属性
            *#
            "sort": [
                {"demoId": "asc"},
                {"_uid": "asc"}
            ],
            ## 其他检索条件，按照时间范围查询数据，所有数据按照上面的searchAter机制进行分页
            "query": {
                "bool": {
                    "filter": [
                        {
                            "range": {
                                "agentStarttime": {
                                    "gte": #[startTime],
                                    "lt": #[endTime]
                                }
                            }
                        }
                    ]
                }
            }
        }]]>
    </property>
</properties>
```



# 4.定义实现searchAfter的代码

```
    public void doSeachAfter() throws ParseException {
		//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/searchafter.xml");
		Map params = new HashedMap();//定义检索条件，将dsl中需要的参数放置到params中
		params.put("pageSize",100);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00").getTime());
		params.put("endTime",new Date().getTime());
		//执行查询，demo为索引表，_search为检索操作action
		ESDatas<Demo> esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
				clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
						"searchAfterDSL",//esmapper/demo.xml中定义的dsl语句
						params,//变量参数
						Demo.class);//返回的文档封装对象类型
		//获取结果对象列表，最多返回1000条记录
		List<Demo> demos = esDatas.getDatas();
		//获取总记录数
		long totalSize = esDatas.getTotalSize();

		do{
			if(demos != null)
				System.out.println("返回当前页记录数:"+demos.size());
			if(demos != null && demos.size() == 100) { //还有数据，则通过searchAfter继续获取下一页数据
				String searchAfterId = (String) demos.get(99).getId();//获取最后一条记录的_id值
				params.put("searchAfterId", searchAfterId);//设置searchAfterId为分页起点_id值
				long demoId =  demos.get(99).getDemoId();//获取最后一条记录的demoId值
				params.put("demoId", demoId);//设置searchAfterId为分页起点demoId值
				esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
						clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
								"searchAfterDSL",//esmapper/demo.xml中定义的dsl语句
								params,//变量参数
								Demo.class);//返回的文档封装对象类型

				demos = esDatas.getDatas();

			}
			else{//如果是最后一页，没有数据返回或者获取的记录条数少于100结束分页操作
				break;
			}
		}while(true);
		System.out.println("总记录数:"+totalSize);

	}
```



# 5.通过junit单元测试用例运行案例

```
	@Test
	public void testSeachAfter() throws ParseException {
		DocumentSearchAfter documentSearchAfter = new DocumentSearchAfter();
		documentSearchAfter.initIndiceAndData();//创建索引表并导入searchAfter分页测试数据
		documentSearchAfter.doSeachAfter();//执行searchAfter分页操作
	}
```



# 6.参考文档

<https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-search-after.html>

案例对应源码工程：

<https://gitee.com/bboss/eshelloword>

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



