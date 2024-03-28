# Elasticsearch search after分页查询案例分享

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

Elasticsearch search after分页查询案例分享

 Just like regular searches, you can [use `from` and `size` to page through search results](https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html), up to the first 10,000 hits. If you want to retrieve more hits, use PIT with [`search_after`](https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html#search-after).

# 1.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置es客户端



# 2.编写创建索引表和初始化数据方法

创建索引表和初始化数据的组件[DocumentCRUD ](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD7.java)实现本文不做重点介绍，请访问[视频教程](https://pan.baidu.com/s/1kXjAOKn)了解：

```java
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

```xml
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

```java
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

```java
	@Test
	public void testSeachAfter() throws ParseException {
		DocumentSearchAfter documentSearchAfter = new DocumentSearchAfter();
		documentSearchAfter.initIndiceAndData();//创建索引表并导入searchAfter分页测试数据
		documentSearchAfter.doSeachAfter();//执行searchAfter分页操作
	}
```

# 6.基于pit机制的search after

基于point in time机制的search after，参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html#search-after

Elasticsearch官方search after dsl案例：

```console
{
  "size": 10000,
  "query": {
    "match" : {
      "user.id" : "elkbee"
    }
  },
  "pit": {
    "id":  "46ToAwMDaWR5BXV1aWQyKwZub2RlXzMAAAAAAAAAACoBYwADaWR4BXV1aWQxAgZub2RlXzEAAAAAAAAAAAEBYQADaWR5BXV1aWQyKgZub2RlXzIAAAAAAAAAAAwBYgACBXV1aWQyAAAFdXVpZDEAAQltYXRjaF9hbGw_gAAAAA==", 
    "keep_alive": "1m"
  },
  "sort": [
    {"@timestamp": {"order": "asc", "format": "strict_date_optional_time_nanos"}}
     {"_shard_doc": "desc"}
  ],
  "search_after": [                                
    "2021-05-20T05:30:04.832Z",
    4294967298
  ],
  "track_total_hits": false                        
}
```

在bboss中配置动态执行的pit search after dsl脚本

```xml
<property name="queryPidSearchAfter"><![CDATA[{
              "size": #[size],
              "query": {
                "match" : {
                    "logOperuser.keyword":#[user]
                }
              },
              "pit": {
                    "id":  #[pid],
                    "keep_alive": "1m"
              },
              #if($_shard_doc)
              "search_after": [
                #[timestamp],
                 #[_shard_doc]
              ]
              ,
              #end
              "sort": [
                {"collecttime": "asc"},
                {"_shard_doc": "desc"}
              ]
            }
     ]]>
    </property>
```

通过bboss执行pit search after操作：

```java
ClientInterface clientUtil = bbossESStarter.getConfigRestClient("esmapper/demo7.xml");
        //申请pitid
        PitId pitId = clientUtil.requestPitId("dbdemofull","1m");
        logger.info("pitId.getId() {}",pitId.getId());
        Map params = new HashMap();
        params.put("size",100);
        params.put("user","admin");
        String pid = pitId.getId();

        String prePid = null;
        List<String> pids = new ArrayList<>();
        pids.add(pid);
        do {

            params.put("pid",pid);
            prePid = pid;
            ESDatas<MetaMap> datas = clientUtil.searchList("/_search", "queryPidSearchAfter", params, MetaMap.class);
            pid = datas.getPitId();
            pids.add(pid);
            //当前页的记录集
            List<MetaMap> metaMaps = datas.getDatas();
            if(metaMaps != null && metaMaps.size() > 0 ){
                MetaMap metaMap = metaMaps.get(metaMaps.size() - 1);
                Object[] sort = metaMap.getSort();
                //search after 排序字段值和shard_doc信息
                params.put("timestamp",sort[0]);
                params.put("_shard_doc",sort[1]);
            }

            if(metaMaps.size() < 100){
                //达到最后一页，分页查询结束
                break;
            }
            logger.info("datas.getPitId() {}", pid);
           // logger.info(clientUtil.deletePitId(prePid));

        }while (true);
        String pre = null;
        for(int i =0 ; i < pids.size(); i ++){
            if(pre == null)
                pre = pids.get(i);
            else{
                System.out.println("pre:"+pre + "\r\n" + "now:"+pids.get(i)  + "\r\n" + "now equals pre:"+pre.equals(pids.get(i)) );
                pre = pids.get(i);
            }
            logger.info(clientUtil.deletePitId(pids.get(i)));//删除pid信息
        }
```

# 6.参考文档

<https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-search-after.html>

案例对应源码工程：

<https://github.com/bbossgroups/elasticsearch-example>

# 开发交流



bboss elasticsearch交流QQ群：21220580,166471282,3625720,154752521,166471103,166470856

**bboss elasticsearch微信公众号：**

<img src="images/qrcode.jpg"  height="200" width="200">



