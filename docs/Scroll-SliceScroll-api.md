# Elasticsearch Scroll和Slice Scroll查询API使用案例

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

Elasticsearch Scroll和Slice Scroll查询API使用案例

**the best elasticsearch highlevel java rest api-----bboss**     

本文内容

1. 基本scroll api使用
2. 基本scroll api与自定义scorll结果集handler函数结合使用
3. slice api使用（并行/串行）
4. slice api使用与自定义scorll结果集handler函数结合使用（并行/串行）

本文对应的maven源码工程：

<https://gitee.com/bboss/eshelloword-booter>



# **1.dsl配置文件定义**

首先定义本文需要的dsl配置文件

[esmapper/scroll.xml](https://gitee.com/bboss/eshelloword-booter/blob/master/src/main/resources/esmapper/scroll.xml)

```
<properties>
    <!--
    简单的scroll query案例，复杂的条件修改query dsl即可
    -->
    <property name="scrollQuery">
        <![CDATA[
         {
            "size":#[size],
            "query": {"match_all": {}}
        }
        ]]>
    </property>
    <!--
        简单的slice scroll query案例，复杂的条件修改query dsl即可
    -->
    <property name="scrollSliceQuery">
        <![CDATA[
         {
           "slice": {
                "id": #[sliceId], ## 必须使用sliceId作为变量名称
                "max": #[sliceMax] ## 必须使用sliceMax作为变量名称
            },
            "size":#[size],
            "query": {"match_all": {}}
        }
        ]]>
    </property>
</properties>
```

下面介绍scroll各种用法，对应的测试类文件为：[**TestScrollAPIQuery**](https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/scroll/TestScrollAPIQuery.java)



#  2.基本scroll api使用

```
	@Test
	public void testSimleScrollAPI(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll分页检索

		Map params = new HashMap();
		params.put("size", 10000);//每页10000条记录
		//scroll上下文有效期1分钟,每次scroll检索的结果都会合并到总得结果集中；数据量大时存在oom内存溢出风险，大数据量时可以采用handler函数来处理每次scroll检索的结果(后面介绍)
		ESDatas<Map> response = clientUtil.scroll("demo/_search","scrollQuery","1m",params,Map.class);
		List<Map> datas = response.getDatas();
		long realTotalSize = datas.size();
		long totalSize = response.getTotalSize();
		System.out.println("totalSize:"+totalSize);
		System.out.println("realTotalSize:"+realTotalSize);
		System.out.println("countAll:"+clientUtil.countAll("demo"));
	}
```



# 3.基本scroll api与自定义scorll结果集handler函数结合使用

```
	@Test
	public void testSimleScrollAPIHandler(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll分页检索
		
		Map params = new HashMap();
		params.put("size", 5000);//每页5000条记录
		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟；大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险
		ESDatas<Map> response = clientUtil.scroll("demo/_search", "scrollQuery", "1m", params, Map.class, new ScrollHandler<Map>() {
			public void handle(ESDatas<Map> response, HandlerInfo handlerInfo)) throws Exception {//自己处理每次scroll的结果
				List<Map> datas = response.getDatas();
				long totalSize = response.getTotalSize();
				System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
			}
		});

		System.out.println("response realzie:"+response.getTotalSize());

	}
```



# 4.slice api使用



##  串行

```
	/**
	 * 串行方式执行slice scroll操作
	 */
	@Test
	public void testSimpleSliceScrollApi() {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		
		//scroll slice分页检索,max对应并行度，一般设置为与索引表的shards数一致
		int max = 6;
		
		Map params = new HashMap();
		params.put("sliceMax", max);//建议不要大于索引表的shards数
		params.put("size", 100);//每页100条记录
		//scroll上下文有效期1分钟,每次scroll检索的结果都会合并到总得结果集中；数据量大时存在oom内存溢出风险，大数据量时可以采用handler函数来处理每次slice scroll检索的结果(后面介绍)
		ESDatas<Map> sliceResponse = clientUtil.scrollSlice("demo/_search",
				"scrollSliceQuery", params,"1m",Map.class);//串行；如果数据量大，建议采用并行方式来执行
		System.out.println("totalSize:"+sliceResponse.getTotalSize());
		System.out.println("realSize size:"+sliceResponse.getDatas().size());
	}
```



##  并行

```
	/**
	 * 并行方式执行slice scroll操作
	 */
	@Test
	public void testSimpleSliceScrollApiParral() {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		
		//scroll slice分页检索,max对应并行度，一般设置为与索引表的shards数一致
		int max = 6;
		
		Map params = new HashMap();
		params.put("sliceMax", max);//这里设置6个slice，建议不要大于索引表的shards数，必须使用sliceMax作为变量名称
		params.put("size", 100);//每页100条记录
		//scroll上下文有效期2分钟,每次scroll检索的结果都会合并到总得结果集中；数据量大时存在oom内存溢出风险，大数据量时可以采用handler函数来处理每次scroll检索的结果(后面介绍)
		ESDatas<Map> sliceResponse = clientUtil.scrollSliceParallel("demo/_search",
				"scrollSliceQuery", params,"2m",Map.class);//表示并行，会从slice scroll线程池中申请sliceMax个线程来并行执行slice scroll检索操作，大数据量多个shared分片的情况下建议采用并行模式
		System.out.println("totalSize:"+sliceResponse.getTotalSize());
		System.out.println("realSize size:"+sliceResponse.getDatas().size());

	}
```



# 5.slice api使用与自定义scorll结果集handler函数结合使用



## 串行

```
	/**
	 * 串行方式执行slice scroll操作
	 */
	@Test
	public void testSimpleSliceScrollApiHandler() {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		
		//scroll slice分页检索,max对应并行度，一般设置为与索引表的shards数一致
		int max = 6;
		
		Map params = new HashMap();
		params.put("sliceMax", max);//这里设置6个slice，建议不要大于索引表的shards数，必须使用sliceMax作为变量名称
		params.put("size", 1000);//每页1000条记录
		//采用自定义handler函数处理每个slice scroll的结果集后，sliceResponse中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟,大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险
		ESDatas<Map> sliceResponse = clientUtil.scrollSlice("demo/_search",
				"scrollSliceQuery", params,"1m",Map.class, new ScrollHandler<Map>() {
					public void handle(ESDatas<Map> response, HandlerInfo handlerInfo)) throws Exception {//自己处理每次scroll的结果
						List<Map> datas = response.getDatas();
						long totalSize = response.getTotalSize();
						System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
					}
				});//串行，如果数据量大建议采用并行模式
		long totalSize = sliceResponse.getTotalSize();

		System.out.println("totalSize:"+totalSize);
	}
```



## 并行

```
	/**
	 * 并行方式执行slice scroll操作
	 */
	@Test
	public void testSimpleSliceScrollApiParralHandler() {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		
		//scroll slice分页检索,max对应并行度，一般设置为与索引表的shards数一致
		int max = 6;
		
		Map params = new HashMap();
		params.put("sliceMax", max);//这里设置6个slice，建议不要大于索引表的shards数，必须使用sliceMax作为变量名称
		params.put("size", 1000);//每页1000条记录
		//采用自定义handler函数处理每个slice scroll的结果集后，sliceResponse中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟,大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险
		ESDatas<Map> sliceResponse = clientUtil.scrollSliceParallel("demo/_search",
				"scrollSliceQuery", params,"1m",Map.class, new ScrollHandler<Map>() {
					public void handle(ESDatas<Map> response, HandlerInfo handlerInfo)) throws Exception {//自己处理每次scroll的结果,注意结果是异步检索的
						List<Map> datas = response.getDatas();
						long totalSize = response.getTotalSize();
						System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
					}
				});//表示并行，会从slice scroll线程池中申请sliceMax个线程来并行执行slice scroll检索操作，大数据量多个shared分片的情况下建议采用并行模式

		long totalSize = sliceResponse.getTotalSize();
		System.out.println("totalSize:"+totalSize);

	}
```

我们可以在application.properties文件中增加以下配置来设置slice scroll查询线程池线程数和等待队列长度：

elasticsearch.sliceScrollThreadCount 默认值500

elasticsearch.sliceScrollThreadQueue 默认值500



# 6 开发交流

**elasticsearch技术交流：166471282**

**elasticsearch：**

**![bbossgroups](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)**