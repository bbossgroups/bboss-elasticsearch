# Elasticsearch Sliced Scroll分页检索案例分享

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

The best elasticsearch highlevel java rest api-----[bboss](README.md) 

Elasticsearch Sliced Scroll分页检索案例分享 

我们在文章《[Elasticsearch Scroll分页检索案例分享](scroll.md)》中介绍了elasticsearch scroll的基本用法，本文介绍Elasticsearch Sliced Scroll分页检索功能。



# 1.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置es客户端



# 2.定义Sliced Scroll检索dsl

创建配置文件-在resources目录下定义文件scroll.xml

```
esmapper/scroll.xml
```

文件内容包含Sliced Scroll检索dsl语句-scrollSliceQuery

```
<property name="scrollSliceQuery">
        <![CDATA[
         {
           "slice": {
                "id": $id,
                "max": $max
            },
            "size":$size,
            "query": {
                "term" : {
                    "gc.jvmGcOldCount" : 3
                }
            }
        }
        ]]>
    </property>
```



# 3.串行方式执行slice检索

```
/**
 * 串行方式执行slice scroll操作
 */
@Test
public void testSliceScroll() {
	ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
	List<String> scrollIds = new ArrayList<>();
	long starttime = System.currentTimeMillis();
	//scroll slice分页检索
	int max = 6;
	long realTotalSize = 0;
	for (int i = 0; i < max; i++) {
		Map params = new HashMap();
		params.put("id", i);
		params.put("max", max);//最多6个slice，不能大于share数
		params.put("size", 100);//每页100条记录
		ESDatas<Map> sliceResponse = clientUtil.searchList("agentstat-*/_search?scroll=1m",
				"scrollSliceQuery", params,Map.class);
		List<Map> sliceDatas = sliceResponse.getDatas();
		realTotalSize = realTotalSize + sliceDatas.size();
		long totalSize = sliceResponse.getTotalSize();
		String scrollId = sliceResponse.getScrollId();
		if (scrollId != null)
			scrollIds.add(scrollId);
		System.out.println("totalSize:" + totalSize);
		System.out.println("scrollId:" + scrollId);
		if (sliceDatas != null && sliceDatas.size() >= 100) {//每页100条记录，迭代scrollid，遍历scroll分页结果
			do {
				sliceResponse = clientUtil.searchScroll("1m", scrollId, Map.class);
				String sliceScrollId = sliceResponse.getScrollId();
				if (sliceScrollId != null)
					scrollIds.add(sliceScrollId);
				sliceDatas = sliceResponse.getDatas();
				if (sliceDatas == null || sliceDatas.size() < 100) {
					break;
				}
				realTotalSize = realTotalSize + sliceDatas.size();
			} while (true);
		}
	}
      //打印处理耗时和实际检索到的数据
	long endtime = System.currentTimeMillis();
	System.out.println("耗时："+(endtime - starttime)+",realTotalSize："+realTotalSize);
	//查询存在es服务器上的scroll上下文信息
	String scrolls = clientUtil.executeHttp("_nodes/stats/indices/search", ClientUtil.HTTP_GET);
	System.out.println(scrolls);
	//处理完毕后清除scroll上下文信息
	if(scrollIds.size() > 0) {
		scrolls = clientUtil.deleteScrolls(scrollIds);
		System.out.println(scrolls);
	}
	//清理完毕后查看scroll上下文信息
	scrolls = clientUtil.executeHttp("_nodes/stats/indices/search", ClientUtil.HTTP_GET);
	System.out.println(scrolls);
}
```



# 4.并行方式执行slice检索

```
//用来存放实际slice检索总记录数
long realTotalSize ;
//辅助方法，用来累计每次scroll获取到的记录数
synchronized void incrementSize(int size){
	this.realTotalSize = this.realTotalSize + size;
}
/**
 * 并行方式执行slice scroll操作
 */
@Test
public void testParralSliceScroll() {
	final ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
	final List<String> scrollIds = new ArrayList<>();
	long starttime = System.currentTimeMillis();
	//scroll slice分页检索
	final int max = 6;
	final CountDownLatch countDownLatch = new CountDownLatch(max);//线程任务完成计数器，每个线程对应一个sclice,每运行完一个slice任务,countDownLatch计数减去1

	for (int j = 0; j < max; j++) {//启动max个线程，并行处理每个slice任务
		final int i = j;
		Thread sliceThread = new Thread(new Runnable() {//多线程并行执行scroll操作做，每个线程对应一个sclice

			@Override
			public void run() {
				Map params = new HashMap();
				params.put("id", i);
				params.put("max", max);//最多6个slice，不能大于share数
				params.put("size", 100);//每页100条记录
				ESDatas<Map> sliceResponse = clientUtil.searchList("agentstat-*/_search?scroll=1m",
						"scrollSliceQuery", params,Map.class);
				List<Map> sliceDatas = sliceResponse.getDatas();
				incrementSize( sliceDatas.size());//统计实际处理的文档数量
				long totalSize = sliceResponse.getTotalSize();
				String scrollId = sliceResponse.getScrollId();
				if (scrollId != null)
					scrollIds.add(scrollId);
				System.out.println("totalSize:" + totalSize);
				System.out.println("scrollId:" + scrollId);
				if (sliceDatas != null && sliceDatas.size() >= 100) {//每页100条记录，迭代scrollid，遍历scroll分页结果
					do {
						sliceResponse = clientUtil.searchScroll("1m", scrollId, Map.class);
						String sliceScrollId = sliceResponse.getScrollId();
						if (sliceScrollId != null)
							scrollIds.add(sliceScrollId);
						sliceDatas = sliceResponse.getDatas();
						if (sliceDatas == null || sliceDatas.size() < 100) {
							break;
						}
						incrementSize( sliceDatas.size());//统计实际处理的文档数量
					} while (true);
				}
				countDownLatch.countDown();//slice检索完毕后计数器减1
			}

		});
		sliceThread.start();//启动线程
	}
	try {
		countDownLatch.await();//等待所有的线程执行完毕,计数器变成0
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
      //打印处理耗时和实际检索到的数据
	long endtime = System.currentTimeMillis();
	System.out.println("耗时："+(endtime - starttime)+",realTotalSize："+realTotalSize);
	//查询存在es服务器上的scroll上下文信息
	String scrolls = clientUtil.executeHttp("_nodes/stats/indices/search", ClientUtil.HTTP_GET);
//		System.out.println(scrolls);
	//处理完毕后清除scroll上下文信息
	if(scrollIds.size() > 0) {
		scrolls = clientUtil.deleteScrolls(scrollIds);
//			System.out.println(scrolls);
	}
	//清理完毕后查看scroll上下文信息
	scrolls = clientUtil.executeHttp("_nodes/stats/indices/search", ClientUtil.HTTP_GET);
//		System.out.println(scrolls);
}
```

通过串行运行和并行运行结果比较，并行处理的性能要好很多，实际检索到的文档数量等价一致。



# 5.参考文档

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-scroll.html>



# 6.开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />


交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">


