# Elasticsearch Scroll分页检索案例分享

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

Elasticsearch Scroll分页检索案例分享



# 1.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置es客户端bboss



# 2.定义scroll检索dsl

首先定义一个简单的scroll dsl检索脚本

```
<properties>
    <property name="scrollQuery">
        <![CDATA[
         {           
           ## 这里都是用常量在操作，实际场景中可以参数化变量
            "size":1000,
            "query": {
                "term" : {
                    "gc.jvmGcOldCount" : 3 ##参数值可以定义为变量，通过参数传递进来
                }
            }
        }
        ]]>
    </property>
</properties>
```



# 3.Scroll检索代码

```
@Test
	public void testScroll(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll分页检索，将检索结果映射为Map对象，也可以映射为自定义的实体对象
		ESDatas<Map> response = clientUtil.searchList("agentstat-*/_search?scroll=1m",
                              "scrollQuery",//对于dsl脚本名称，在esmapper/scroll.xml文件中配置
                               Map.class);
		List<Map> datas = response.getDatas();//第一页数据
		List<String > scrollIds = new ArrayList<>();//用于记录每次scroll的scrollid，便于检索完毕后清除
		long totalSize = response.getTotalSize();//总记录数
		String scrollId = response.getScrollId();//第一次的scrollid
		if(scrollId != null)
			scrollIds.add(scrollId);
		System.out.println("totalSize:"+totalSize);
		System.out.println("scrollId:"+scrollId);
		if(datas != null && datas.size() > 0) {//每页1000条记录，通过迭代scrollid，遍历scroll分页结果
			do {

				response = clientUtil.searchScroll("1m",scrollId,Map.class);
				scrollId = response.getScrollId();//每页的scrollid
				if(scrollId != null)
					scrollIds.add(scrollId);
				datas = response.getDatas();//每页的纪录数
				if(datas == null || datas.size() == 0){
					break;
				}
			} while (true);
		}
		//查询并打印存在于es服务器上的scroll上下文信息
		String scrolls = clientUtil.executeHttp("_nodes/stats/indices/search", ClientUtil.HTTP_GET);
		System.out.println(scrolls);
		//清除scroll上下文信息,虽然说超过1分钟后，scrollid会自动失效，
        //但是手动删除不用的scrollid，是一个好习惯
		if(scrollIds.size() > 0) {
			scrolls = clientUtil.deleteScrolls(scrollIds);
			System.out.println(scrolls);
		}
		//清理完毕后查看scroll上下文信息
		scrolls = clientUtil.executeHttp("_nodes/stats/indices/search", ClientUtil.HTTP_GET);
		System.out.println(scrolls);
	}
```



# 4.Scroll案例项目地址和代码文件

项目地址：

<https://github.com/bbossgroups/elasticsearchdemo/>

scroll检索对应的代码和脚本文件：

<https://github.com/bbossgroups/elasticsearchdemo/blob/master/src/main/resources/esmapper/scroll.xml>

<https://github.com/bbossgroups/elasticsearchdemo/blob/master/src/test/java/org/frameworkset/elasticsearch/TestScrollQuery.java>

# 开发交流



bboss elasticsearch交流QQ群：21220580,166471282,3625720,154752521,166471103,166470856

**bboss elasticsearch微信公众号：**

<img src="images/qrcode.jpg"  height="200" width="200">



