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

<https://github.com/bbossgroups/elasticsearch-example>

# scroll和slice scroll
先了解一下scroll和slice scroll特点
1. scroll串行从elasticserch查询拉取数据，bboss可以并行处理也可以串行处理scroll拉取回来数据
2. slice scroll查询可以串行和并行从elasticserch查询拉取拉取数据，bboss亦可以并行或者串行处理slice scroll拉取回来的数据 

# **1.dsl配置文件定义**

首先定义本文需要的dsl配置文件

[esmapper/scroll.xml](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/scroll.xml)

```xml
<properties>
    <!--
    简单的scroll query案例，复杂的条件修改query dsl即可
    -->
    <property name="scrollQuery">
        <![CDATA[
         {
            "size":#[size],
            "query": {"match_all": {}},
            "sort": [
                "_doc"
            ]
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
            "query": {"match_all": {}},
            "sort": [
                "_doc"
            ]
        }
        ]]>
    </property>
</properties>
```

下面介绍scroll各种用法，对应的测试类文件为：[**TestScrollAPIQuery**](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/scroll/TestScrollAPIQuery.java)



#  2.基本scroll api使用

```java
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

## 串行

```java
	@Test
	public void testSimleScrollAPIHandler(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll分页检索
		
		Map params = new HashMap();
		params.put("size", 5000);//每页5000条记录
		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟；大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险
		ESDatas<Map> response = clientUtil.scroll("demo/_search", "scrollQuery", "1m", params, Map.class, new ScrollHandler<Map>() {
			public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果
				List<Map> datas = response.getDatas();
				long totalSize = response.getTotalSize();
				System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
			}
		});

		System.out.println("response realzie:"+response.getTotalSize());

	}
```

## 串行中断

在一定的条件下，可以中断整个串行scroll的处理，通过抽象类SerialBreakableScrollHandler的setBreaked(true)方法来实现中断串行scroll执行：

```java
@Test
	public void testSimleBreakableScrollAPIHandler(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll分页检索
		Map params = new HashMap();
		params.put("size", 10);//每页5000条记录
		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟
		final AtomicInteger count = new AtomicInteger();
		ESDatas<Map> response = clientUtil.scroll("demo/_search", "scrollQuery", "1m", params, Map.class, new SerialBreakableScrollHandler<Map>() {
			public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果
				List<Map> datas = response.getDatas();
				long totalSize = response.getTotalSize();
				int test = count.incrementAndGet();
//				final AtomicInteger count = new AtomicInteger();
				if(test % 2 == 1) //到第三条数据时，中断scroll执行
				 	this.setBreaked(true);
				System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
			}
		});

		System.out.println("response realzie:"+response.getTotalSize());

	}
```



## 并行

```java
@Test
public void testSimleScrollParallelAPIHandler(){
   ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
   //scroll分页检索
   Map params = new HashMap();
   params.put("size", 5000);//每页5000条记录
   //采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
   //scroll上下文有效期1分钟
   ESDatas<Map> response = clientUtil.scrollParallel("demo/_search", "scrollQuery", "1m", params, Map.class, new ScrollHandler<Map>() {
      public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果
         List<Map> datas = response.getDatas();
         long totalSize = response.getTotalSize();
         System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
      }
   });

   System.out.println("response realzie:"+response.getTotalSize());

}
```

## 并行中断

在一定的条件下，可以中断整个并行scroll的处理，通过抽象类ParralBreakableScrollHandler的setBreaked(true)方法来实现中断并行scroll执行：

```java
@Test
	public void testSimleBreakableScrollParallelAPIHandler(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll分页检索
		Map params = new HashMap();
		params.put("size", 10);//每页5000条记录
		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		final AtomicInteger count = new AtomicInteger();
		//scroll上下文有效期1分钟
		ESDatas<Map> response = clientUtil.scrollParallel("demo/_search", "scrollQuery", "1m", params, Map.class, new ParralBreakableScrollHandler<Map>() {
			public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果
				List<Map> datas = response.getDatas();
				long totalSize = response.getTotalSize();
				int test = count.incrementAndGet();
				if(test % 2 == 1) //到第三条数据时，中断scroll执行
					this.setBreaked(true);
				System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
			}
		});

		System.out.println("response realzie:"+response.getTotalSize());

	}
```



# 4.slice api使用



##  串行

```java
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

```java
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

```java
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
					public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果
						List<Map> datas = response.getDatas();
						long totalSize = response.getTotalSize();
						System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
					}
				});//串行，如果数据量大建议采用并行模式
		long totalSize = sliceResponse.getTotalSize();

		System.out.println("totalSize:"+totalSize);
	}
```

## 串行中断

在一定的条件下，可以中断整个串行slice scroll的处理，通过抽象类SerialBreakableScrollHandler的setBreaked(true)方法来实现中断串行slice scroll执行：

```java
@Test
	public void testSimpleBreakableSliceScrollApiHandler() {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll slice分页检索,max对应并行度
		int max = 6;
		Map params = new HashMap();
		params.put("sliceMax", max);//最多6个slice，不能大于share数，必须使用sliceMax作为变量名称
		params.put("size", 10);//每页1000条记录
		//采用自定义handler函数处理每个slice scroll的结果集后，sliceResponse中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟
		final AtomicInteger count = new AtomicInteger();
		ESDatas<Map> sliceResponse = clientUtil.scrollSlice("demo/_search",
				"scrollSliceQuery", params,"1m",Map.class, new SerialBreakableScrollHandler<Map>() {
					public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果
						List<Map> datas = response.getDatas();
						long totalSize = response.getTotalSize();
						int test = count.incrementAndGet();
						int r = test % 7;
						if(r == 6) //到第6条数据时，中断scroll执行
							this.setBreaked(true);
						System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
					}
				});//串行
		long totalSize = sliceResponse.getTotalSize();

		System.out.println("totalSize:"+totalSize);
	}
```



## 并行

```java
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
					public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果,注意结果是异步检索的
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

elasticsearch.sliceScrollThreadCount 默认值50

elasticsearch.sliceScrollThreadQueue 默认值100

## 并行中断

在一定的条件下，可以中断整个并行slice scroll的处理，通过抽象类ParralBreakableScrollHandler的setBreaked(true)方法来实现中断并行slice scroll执行：

```java
@Test
	public void testSimpleBreakableSliceScrollApiParralHandler() {
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
		//scroll slice分页检索,max对应并行度
		int max = 6;
		Map params = new HashMap();
		params.put("sliceMax", max);//最多6个slice，不能大于share数，必须使用sliceMax作为变量名称
		params.put("size", 1000);//每页1000条记录
		//采用自定义handler函数处理每个slice scroll的结果集后，sliceResponse中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟
		final AtomicInteger count = new AtomicInteger();
		ESDatas<Map> sliceResponse = clientUtil.scrollSliceParallel("demo/_search",
				"scrollSliceQuery", params,"1m",Map.class, new ParralBreakableScrollHandler<Map>() {
					public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果,注意结果是异步检索的
						List<Map> datas = response.getDatas();
						long totalSize = response.getTotalSize();
						int test = count.incrementAndGet();
						int r = test % 7;
						if(r == 6) //到第6条数据时，中断scroll执行
							this.setBreaked(true);
						System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
					}
				});//并行

		long totalSize = sliceResponse.getTotalSize();
		System.out.println("totalSize:"+totalSize);

	}
```



## ES之间数据导入导出

可以使用并行方式执行slice scroll操作来实现ES之间数据导入导出，将一个elasticsearch的数据导入另外一个elasticsearch，需要在application.properties文件中定义两个es集群配置：default(默认集群，源集群),es233（目标集群），default对应目标集群，es233对应源集群，目标集群和源集群的elasticsearch版本可以为elasticsearch 1.x,2.x,5.x,6.x,7.x，+；[参考配置](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/applicationtwo.properties)

### 源库索引全量数据导入

```java
/**
 * 并行方式执行slice scroll操作：将一个es的数据导入另外一个es数据，需要在application.properties文件中定义default和es233的两个集群
 */
@Test
public void testSimpleSliceScrollApiParralHandlerExport() {
   ClientInterface clientUtil522 = ElasticSearchHelper.getRestClientUtil("default");//定义一个对应目标集群default的客户端组件实例
  
   final ClientInterface clientUtil234 = ElasticSearchHelper.getRestClientUtil("es233"); //定义一个对应源集群es233的客户端组件实例   
   
   //从源集群索引demo中按每批10000笔记录查询数据，在handler中通过addDocuments将批量检索出的数据导入目标库
   ESDatas<Map> sliceResponse = clientUtil522.searchAll("demo",10000, new ScrollHandler<Map>() {
            public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果,注意结果是异步检索的
               List<Map> datas = response.getDatas();
                 clientUtil234.addDocuments("index233","indextype233",datas);
               //将分批查询的数据导入目标集群索引index233，索引类型为indextype233，如果是elasticsearch 7以上的版本，可以去掉索引类型参数，例如：
                //clientUtil234.addDocuments("index233",datas);
               long totalSize = response.getTotalSize();
               System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
            }
         },Map.class //指定检索的文档封装类型
         ,6);//6个工作线程并发导入

   long totalSize = sliceResponse.getTotalSize();
   System.out.println("totalSize:"+totalSize);

}
```



### 源库索引有查询条件的导入

可以通过配置文件定义导入数据的dsl语句，将符合条件的数据导入目标库：其中esmapper/scroll.xml配置文件参考章节[**1.dsl配置文件定义**](https://esdoc.bbossgroups.com/#/Scroll-SliceScroll-api?id=_1dsl配置文件定义)

```java
/**
 * 并行方式执行slice scroll操作：将一个es的数据导入另外一个es数据，需要在application.properties文件中定义default和es233的两个集群
 */
@Test
public void testSimpleSliceScrollApiParralHandlerExport() {
   ClientInterface clientUtil522 = ElasticSearchHelper.getConfigRestClientUtil("default","esmapper/scroll.xml");//定义一个对应源集群default的客户端组件实例，并且加载配置了scrollSliceQuery dsl的xml配置文件
  
   final ClientInterface clientUtil234 = ElasticSearchHelper.getRestClientUtil("es233"); //定义一个对应目标集群es233的客户端组件实例
   //scroll slice分页检索,max对应并行度，与源表shards数一致即可
   int max = 6;
   Map params = new HashMap();
   params.put("sliceMax", max);//最多6个slice，不能大于share数，必须使用sliceMax作为变量名称
   params.put("size", 5000);//每批5000条记录
   //采用自定义handler函数处理每个slice scroll的结果集后，sliceResponse中只会包含总记录数，不会包含记录集合
   //scroll上下文有效期1分钟，从源集群索引demo中查询数据
   ESDatas<Map> sliceResponse = clientUtil522.scrollSliceParallel("demo/_search",
         "scrollSliceQuery", params,"1m",Map.class, new ScrollHandler<Map>() {
            public void handle(ESDatas<Map> response, HandlerInfo handlerInfo) throws Exception {//自己处理每次scroll的结果,注意结果是异步检索的
               List<Map> datas = response.getDatas();
                 clientUtil234.addDocuments("index233","indextype233",datas);
               //将分批查询的数据导入目标集群索引index233，索引类型为indextype233，如果是elasticsearch 7以上的版本，可以去掉索引类型参数，例如：
                //clientUtil234.addDocuments("index233",datas);
               long totalSize = response.getTotalSize();
               System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
            }
         });

   long totalSize = sliceResponse.getTotalSize();
   System.out.println("totalSize:"+totalSize);

}
```



# 6 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



