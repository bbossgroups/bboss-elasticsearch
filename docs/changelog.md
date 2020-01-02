

**The best Elasticsearch Highlevel Rest  Client API-----[bboss](https://esdoc.bbossgroups.com/#/README)**   v5.9.9 发布。

https://esdoc.bbossgroups.com/#/quickstart

https://esdoc.bbossgroups.com/#/development

# **主要功能特色**

1. ElasticSearch兼容性:1.x,2.x,5.x,6.x,7.x,+

2. JDK兼容性： jdk 1.7+

3. Spring boot兼容性:1.x,2.x

4. ORM和DSL二者兼顾，类mybatis方式操作ElasticSearch,

5. 支持[SQL](https://esdoc.bbossgroups.com/#/Elasticsearch-SQL-ORM)和[JDBC](https://esdoc.bbossgroups.com/#/Elasticsearch-JDBC)

6. 提供快速而高效的数据导入ES工具

7. APM开源产品pinpoint官方Elasticsearch bboss 客户端性能监控插件，插件地址： 

   https://github.com/naver/pinpoint/tree/master/plugins/elasticsearch-bboss
   
# v6.0.0 功能改进
1.修复低版本jackson兼容性问题：Conflicting property name definitions: '_source'

# v5.9.9 功能改进
1.数据同步工具改进：可以指定导入的target Elasticsearch和source Elasticsearch数据源名称

2.BulkCommand增加处理开始时间和结束时间字段

3.BulkProcessor改进:由被动bulk模式调整为主动bulk模式，减少内存占用，处理速度更快

4.ClientInterface接口增加获取Elasticsearch集群版本号和集群信息的api

# v5.9.8 功能改进

1.改进BulkProcessor shutdown机制:调用shutDown停止方法后，BulkProcessor不会接收新的请求，但是会处理完所有已经进入bulk队列的数据

参考文档：https://esdoc.bbossgroups.com/#/bulkProcessor

2.改进BulkProcessor bulk任务处理结果回调机制：增加对error和exception的bulk任务回调方法

参考文档：https://esdoc.bbossgroups.com/#/bulkProcessor

```java
    /**
	 * 有数据处理失败回调方法
	 * @param bulkCommand
	 * @param result Elasticsearch返回的response报文,包含的失败记录情况
	 */
	public void errorBulk(BulkCommand bulkCommand,String result);
	
    /**
     * 处理异常回调方法
     * @param bulkCommand
     * @param exception
     */
    public void exceptionBulk(BulkCommand bulkCommand,Throwable exception);
```

3.BulkProcessor增加timeout/masterTimeout/refresh/waitForActiveShards/routing/pipeline控制参数,

参数含义参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

参数使用参考文档：
https://esdoc.bbossgroups.com/#/bulkProcessor				

//下面的参数都是bulk url请求的参数：RefreshOption和其他参数只能二选一，配置了RefreshOption（类似于refresh=true&&aaaa=bb&cc=dd&zz=ee这种形式，将相关参数拼接成合法的url参数格式）就不能配置其他参数， 其中的refresh参数控制bulk操作结果强制refresh入elasticsearch，便于实时查看数据，测试环境可以打开，生产不要设置
```java
BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();

				bulkProcessorBuilder .setTimeout("100s")
			.setMasterTimeout("50s")
				.setRefresh("true")
				.setWaitForActiveShards(2)
			.setRouting("1") //(Optional, string) Target the specified primary shard.
				.setPipeline("1") // (Optional, string) ID of the pipeline to use to preprocess incoming documents.
```
4.客户端API改进：

addDocuments/updateDocuments ：与bulkprocessor处理逻辑整合

addDocument/updateDocument ：改进ClientOptions对象，增加一系列控制参数

```java
	private String pipeline;
	private String opType;
	private Boolean returnSource;
	private Long ifSeqNo;
	private Long ifPrimaryTerm;

	private List<String> sourceUpdateExcludes;
	private List<String> sourceUpdateIncludes;
	private String timeout;
	private String masterTimeout ;
	private Integer waitForActiveShards;
	private String refresh;
	/**单文档操作：文档id*/
	private Object id;
	/**单文档操作：文档id*/
	private Object parentId;
```

参数对应的作用和使用场景，参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

使用示例：更新文档

```java
ClientOptions updateOptions = new ClientOptions();
		List<String> sourceUpdateIncludes = new ArrayList<String>();
		sourceUpdateIncludes.add("name");
		updateOptions.setSourceUpdateIncludes(sourceUpdateIncludes);//es 7不起作用
		updateOptions.setDetectNoop(false)
				.setDocasupsert(false)
				.setReturnSource(true)
//				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("demoId")
//				.setVersion(2).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
//				.setIfPrimaryTerm(2l)
//				.setIfSeqNo(3l)
//				.setPipeline("1")
				.setEsRetryOnConflict(2)
				.setTimeout("100s")
		.setWaitForActiveShards(1)
		.setRefresh("true");
				//.setMasterTimeout("10s")
				;
		//更新文档
		response = clientUtil.updateDocument("demo",//索引表
				"demo",//索引类型
				demo
		,updateOptions);
```

批量添加文档

```java
ClientOptions clientOptions = new ClientOptions();
		clientOptions.setRefreshOption("refresh=true");
		clientOptions.setIdField("demoId");
		String response = clientUtil.addDocuments(
				demos,clientOptions);//为了测试效果,启用强制刷新机制，实际线上环境去掉最后一个参数"refresh=true"
```

添加文档

```java
ClientOptions addOptions = new ClientOptions();

		addOptions
				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("demoId")
//				.setVersion(2).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
//				.setIfPrimaryTerm(1l)
//				.setIfSeqNo(13l)
//				.setPipeline("1")
				.setTimeout("100s")
				.setWaitForActiveShards(1)
				.setRefresh("true")
				.setRouting(1);
		//.setMasterTimeout("10s")
		;
		String response = clientUtil.addDocument("demo",//索引表
				demo,addOptions);
```

在bulk记录中使用控制参数

```java
		ClientOptions clientOptions = new ClientOptions();
		clientOptions.setIdField("id")//通过clientOptions指定map中的key为id的字段值作为文档_id，
		          .setEsRetryOnConflict(1)
//							.setPipeline("1")

				.setOpType("index")
				.setIfPrimaryTerm(2l)
				.setIfSeqNo(3l)
		;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("name","duoduo1");
		data.put("id",1);
		bulkProcessor.insertData("bulkdemo","bulkdemo",data,clientOptions);
		ClientOptions deleteclientOptions = new ClientOptions();
		 

		deleteclientOptions.setEsRetryOnConflict(1);
		//.setPipeline("1")
		bulkProcessor.deleteData("bulkdemo","bulkdemo","1",deleteclientOptions);、
		
				ClientOptions updateOptions = new ClientOptions();
//		List<String> sourceUpdateExcludes = new ArrayList<String>();
//		sourceUpdateExcludes.add("name");
		//					updateOptions.setSourceUpdateExcludes(sourceUpdateExcludes); //es 7不起作用
		List<String> sourceUpdateIncludes = new ArrayList<String>();
		sourceUpdateIncludes.add("name");

		/**
		 * ersion typesedit
		 * In addition to the external version type, Elasticsearch also supports other types for specific use cases:
		 *
		 * internal
		 * Only index the document if the given version is identical to the version of the stored document.
		 * external or external_gt
		 * Only index the document if the given version is strictly higher than the version of the stored document or if there is no existing document. The given version will be used as the new version and will be stored with the new document. The supplied version must be a non-negative long number.
		 * external_gte
		 * Only index the document if the given version is equal or higher than the version of the stored document. If there is no existing document the operation will succeed as well. The given version will be used as the new version and will be stored with the new document. The supplied version must be a non-negative long number.
		 * The external_gte version type is meant for special use cases and should be used with care. If used incorrectly, it can result in loss of data. There is another option, force, which is deprecated because it can cause primary and replica shards to diverge.
		 */
		updateOptions.setSourceUpdateIncludes(sourceUpdateIncludes);//es 7不起作用
		updateOptions.setDetectNoop(false)
				.setDocasupsert(false)
				.setReturnSource(true)
//				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("id")
				//.setVersion(10).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
				.setIfPrimaryTerm(2l)
				.setIfSeqNo(3l)
				.setPipeline("1")
		;
		bulkProcessor.updateData("bulkdemo","bulkdemo",data,updateOptions);
```

在数据同步中使用全局控制参数

```java
ClientOptions clientOptions = new ClientOptions();
//		clientOptions.setPipeline("1");
		clientOptions.setRefresh("true");
//		routing
//				(Optional, string) Target the specified primary shard.
		clientOptions.setRouting("2");
		clientOptions.setTimeout("50s");
		clientOptions.setWaitForActiveShards(2);
		importBuilder.setClientOptions(clientOptions);
```

在数据同步datarefactor中使用记录级控制参数

```java
ClientOptions clientOptions = new ClientOptions();
					clientOptions
							.setEsRetryOnConflict(1)
//							.setPipeline("1")

							.setOpType("index")
							.setIfPrimaryTerm(2l)
							.setIfSeqNo(3l)
					;//create or index
					context.setClientOptions(clientOptions);
```

5.数据同步改进：

5.1 数据同步到Elasticsearch，增加增、删、改数据的同步，Context接口添加以下三个方法来控制增、删、改数据的同步

context.markRecoredInsert();//添加，默认值

context.markRecoredUpdate();//修改

context.markRecoredDelete();//delete

5.2 可以在记录级指定每条记录对应的index和indexType

使用示例：这里根据随机值指定记录操作，可以根据实际的值进行控制

```java
final Random random = new Random();
		importBuilder.setDataRefactor(new DataRefactor() {
			@Override
			public void refactor(Context context) throws Exception {
				int r = random.nextInt(3);
				if(r == 1) {
					ClientOptions clientOptions = new ClientOptions();
					clientOptions
							.setEsRetryOnConflict(1)
//							.setPipeline("1")

							.setOpType("index")
							.setIfPrimaryTerm(2l)
							.setIfSeqNo(3l)
					;//create or index
					context.setClientOptions(clientOptions);
					context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
				}
				else if(r == 0) {
					ClientOptions clientOptions = new ClientOptions();

					clientOptions.setDetectNoop(false)
							.setDocasupsert(false)
							.setReturnSource(true)
							.setEsRetryOnConflict(3)
					;//设置文档主键，不设置，则自动产生文档id;
					context.setClientOptions(clientOptions);
					context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
					context.markRecoredUpdate();
				}
				else if(r == 2){
					ClientOptions clientOptions = new ClientOptions();
					clientOptions.setEsRetryOnConflict(2);
//							.setPipeline("1");
					context.setClientOptions(clientOptions);
					context.markRecoredDelete();
					context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
				}

			}
		});
```



# v5.9.7 功能改进

1.完善数据同步功能：增加flushInterval参数，单位毫秒，值大于0时，对于异步消息处理组件数据长时间没有达到指定的batchSize记录条数时，强制将已经接收到的数据进行入库处理

2.增加BulkProcessor：异步收集增、删、改请求，并进行排队，达到一定的请求数量后，进行bulk批量入库处理，可以根据flushInterval参数(单位毫秒)，值大于0时，对于异步消息处理组件数据长时间没有达到指定的batchSize记录条数时，强制将已经接收到的数据进行bulk入库处理，参考文档：

https://esdoc.bbossgroups.com/#/bulkProcessor

3.增加elasticsearch-elasticsearch数据同步功能，Demo地址：

https://github.com/bbossgroups/elasticsearch-elasticsearch

4.Elasticsearch同步功能改进：增加ignoreNullValueField控制参数，true是忽略null值存入elasticsearch，false是存入（默认值）

importBuilder.setIgnoreNullValueField(true); 

5.Client Api改进：http连接池增加 evictExpiredConnections配置，true 控制HttpClient实例使用后台线程主动地从连接池中驱逐过期连接，默认值为true

6.bug修复：ElasticSearchHelper.getElasticSearchSink(String elasticSearch)方法传入default数据源名称时，后台报异常信息

# v5.9.6 功能改进

1.修复数据同步bug：application.properties文件中不配置db相关的选项时，同步作业报错

2.完善数据同步任务统计信息记录

3.解决mongodb-elasticsearch增量数据同步增量状态记录主键没有正确生成的问题

# v5.9.5 功能改进
1.修改bug：slice scroll parral和scroll parrel查询有个bug，变量名称写错了，手误导致，但是问题很严重，会导致数据重复，请升级版本到5.9.5！

2.数据同步模块扩展：增加数据库到数据库的数据同步功能

# v5.9.3 功能改进

  1.将数据同步模块从elasticsearch模块剥离，单独形成gradle工程 ，github地址： 

  https://github.com/bbossgroups/bboss-elastic-tran

  2.数据同步工具功能扩展：

-   增加mongodb-db同步模块,支持mongodb各个版本，各种主流数据库，案例：


  https://github.com/bbossgroups/mongodb-elasticsearch

-   增加kafka1x-elasticsearch同步模块兼容kafka_2.12-0.10.2.0系列版本,elasticsearch各个版本：案例


  https://github.com/bbossgroups/kafka1x-elasticsearch

-   增加kafka2x-elasticsearch同步模块兼容kafka_2.12-2.3.0 系列版本,elasticsearch各个版本，案例：


  https://github.com/bbossgroups/kafka2x-elasticsearch

3. 调整同步程序包路径，api兼容旧版本



# v5.9.2 功能改进

1.增加MetaMap类：MetaMap继承HashMap，为map 增加meta元数据相关的属性信息，参考示例：

```java
//创建批量创建文档的客户端对象，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		MetaMap newDemo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"1",//文档id
				MetaMap.class
		);
		System.out.println(newDemo);
		System.out.println("getId:"+newDemo.getId());
		System.out.println("getIndex:"+newDemo.getIndex());
		System.out.println("getNode:"+newDemo.getNode());
		System.out.println("getShard:"+newDemo.getShard());
		System.out.println("getType:"+newDemo.getType());
		System.out.println("getExplanation:"+newDemo.getExplanation());
		System.out.println("getFields:"+newDemo.getFields());
		System.out.println("getHighlight:"+newDemo.getHighlight());
		System.out.println("getInnerHits:"+newDemo.getInnerHits());
		System.out.println("getNested:"+newDemo.getNested());
		System.out.println("getPrimaryTerm:"+newDemo.getPrimaryTerm());
		System.out.println("getScore:"+newDemo.getScore());
		System.out.println("getSeqNo:"+newDemo.getSeqNo());
		System.out.println("getVersion:"+newDemo.getVersion());
		System.out.println("getParent:"+newDemo.getParent());
		System.out.println("getRouting:"+newDemo.getRouting());
		System.out.println("getSort:"+newDemo.getSort());
		System.out.println("isFound:"+newDemo.isFound());
```
2.修改自定义启动客户端bug：设置数字参数和boolean参数不起作用问题修改

3.数据同步工具：增加mongodb到elasticsearch同步功能，工具demo：

https://github.com/bbossgroups/mongodb-elasticsearch

 参考文档：[mongodb-elasticsearch数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_4-mongodb-elasticsearch数据同步使用方法) 

4.数据同步工具改进：如果同步字段中包含名称为_id的属性，则忽略之，否则会导致数据无法正常导入Elasticsearch

5.增加慢请求服务调用日志信息记录，增加慢dsl时间条件参数配置

  slowDslThreshold单位：ms 毫秒

```properties
  elasticsearch.slowDslThreshold=10000
```

  spring boot配置项  

```properties
spring.elasticsearch.bboss.elasticsearch.slowDslThreshold=10000
```

  需要截取掉过长的dsl:超过2048的dsl将被截取掉超过的内容，替换为.....

  如果没有指定回调处理接口，将直接打印警告信息到日志文件，WARN级别
  否则调用SlowDslCallback接口方法slowDslHandle传递慢dsl的相关信息：

```java
  public interface SlowDslCallback {
      	void slowDslHandle( SlowDsl slowDsl);
      }
```

​      slowDslCallback配置： 

```properties
  elasticsearch.slowDslCallback=org.bboss.elasticsearchtest.crud.TestSlowDslCallback
```

​     spring boot配置项     

```properties
spring.elasticsearch.bboss.elasticsearch.slowDslCallback=org.bboss.elasticsearchtest.crud.TestSlowDslCallback
```



# v5.9.1 功能改进
修复bug：按时间增量同步问题,导致任务重启后同步报错

# v5.9.0 功能改进
1.数据同步工具改进：完善增量数据同步机制，增量字段无需排序即可实现增量同步功能，提升同步性能

改进后部分类（比如Context）包路径做了调整，但是api完全兼容，可以到以下地址下载改进后最新的工具:

- jdk timer和quartz demo工程
  https://github.com/bbossgroups/db-elasticsearch-tool

- xxl-job demo工程
  https://github.com/bbossgroups/db-elasticsearch-xxjob


2.数据同步工具bug修复：解决增量同步状态更新可能存在的不正确问题

3.数据同步工具改进：增加数据同步监控指标数据的采集，参考文档：[数据同步任务执行统计信息获取](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2317-%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%9f%e8%ae%a1%e4%bf%a1%e6%81%af%e8%8e%b7%e5%8f%96)

4.数据同步工具新增功能：elasticsearch到db的数据同步功能，支持全量、增量定时同步，内置jdk timer同步器，支持quartz、xxl-job任务调度引擎，使用参考：

 [elasticsearch-db数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_3-elasticsearch-db%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bd%bf%e7%94%a8%e6%96%b9%e6%b3%95) 

5.简化数据同步程序开发调试工作：可以直接在同步程序的main中方法进行同步功能开发和调试

6.Scroll/Slice scroll检索功能改进：支持在hanlder结果回调处理函数中中断scroll/slice croll的执行，参考文档：

 https://esdoc.bbossgroups.com/#/Scroll-SliceScroll-api 

7.json库fastjackson升级到2.10.0 

# v5.8.9 功能改进

1.改进检索Meta数据：增加seqNo和primaryTerm属性。

2.includeTypeName配置默认设置为false

3.BUG fixed: sql查询日期处理问题 [#11](https://github.com/bbossgroups/bboss-elasticsearch/issues/11) 


# v5.8.8 功能改进

1.改进检索Meta数据：增加explanation属性。

2.增加一组meta注解，用于在对象中注入检索元数据，使用参考PO对象

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/MetaDemo.java

![](/images/metaanno.png)

# v5.8.6 功能改进

1.新增bboss-elasticsearch-rest-entity模块，方便bboss相关的实体bean被第三方项目引用。

2.兼容性完善：支持最新的spring boot版本2.1.6.RELEASE

# v5.8.3 功能改进

1.httpproxy负载均衡器：Map配置代码方式启动httpproxy，增加对环境变量的支持，参考文档

2.数据同步工具：解决oracle时间Timestamp问题

3.数据同步工具：解决可能存在的数据序列化问题

# v5.8.2 功能改进

1.增加URL参数检索API，参考文档：[通过URL参数检索文档](https://esdoc.bbossgroups.com/#/document-crud?id=通过url参数检索文档)

# v5.8.1 功能改进

1. 升级bboss框架到5.5.2

2. 升级bboss http到5.5.3

3. 属性配置支持从jvm system环境参数和OS 环境变量取值，例如：

   ```properties
   #引用环境变量job_executor_ip
   
   xxl.job.executor.ip=#[job_executor_ip]
   ```

   

4. 增加script函数管理api（新增、删除、获取脚本函数）

5. 完善http负载均衡组件


# v5.8.0 功能改进

1. 升级框架到5.5.2
2. 增加forcemerge方法
3. Elasticsearch响应报文长度为0情况处理
4. http负载均衡组件增加主备路由功能

# v5.7.8 功能改进

1. 升级bboss版本为5.5.0
2. 增加http负载均衡组件，参考文档：https://esdoc.bbossgroups.com/#/httpproxy
3. 完善http组件，升级httpcomponents client版本为4.5.9
4. ClientOption/UpdateOption 添加属性：version/versionType/routing/Docasupsert/DetectNoop/EsRetryOnConflict

# v5.7.5 功能改进

1. 支持第三方用途多数据源配置和加载，可以同步数据过程中加载这些数据，通过这些数据源查找数据，组合同步到es中
2. 持久层消除对jackson json包的依赖 
3. 持久层增加对es jdbc 6.4.x,6.5.x,6.6.x,7.x的支持
4. 对于默认的持久层不能识别的driver，采用DBNone默认适配器并给出警告信息，而不是抛出异常

```java
if(log.isWarnEnabled()){
				log.warn("Unknown JDBC driver: {}: Adapter DBNonewill be used or define one and resitry it to bboss.",driver);
			}
```



# v5.7.3 功能改进

1. 改进searchAllParrel方法：增加对es 2.x的兼容性处理
2. 改进数据库同步到es的db事务机制：增加是否启用在datarefactor中开启db事务


# v5.7.2 功能改进

1. 同步mysql大数据表到Elasticsearch，增加[mysql内置流处理机制](https://esdoc.bbossgroups.com/#/db-es-tool?id=_513-mysql-resultset-stream%e6%9c%ba%e5%88%b6%e8%af%b4%e6%98%8e)的支持

# v5.7.1 功能改进

1. Fixed addDocumentsWithIdKey null point exception since 5.6.8
2. Spring booter start module support set retryInterval(timeunit:ms) parameter

# v5.7.0 功能改进

1. 修复健康检查不起作用的bug: 应用启动时es没有启动，当es起来后，客户端一直提示es不可用
2. 同步数据工具支持达梦数据库到elasticsearch数据同步

# v5.6.9 功能改进

1. 修改v5.6.8数据同步bug：索引type类型多了一个双引号 

# v5.6.8 功能改进

1. 更新jackson版本号为2.9.8 
2. 更新bboss版本号为5.3.1
3. 增加[ESIndex注解](https://esdoc.bbossgroups.com/#/document-crud?id=esindex%e6%b3%a8%e8%a7%a3%e4%bd%bf%e7%94%a8)，用于配置bean的动态索引名称和索引类型
4. 如果http端口被错误配置为transport 9300端口，给出相应的出错提示

# v5.6.7 功能改进

1. 改进scroll并行查询机制，支撑Elasticsearch pinpoint apm插件

# v5.6.6 功能改进

1. 数据同步工具改进：改进xxjob的支持，增加shard分片任务执行机制
2. 完善故障节点检测日志信息

# v5.6.5 功能改进

1. 数据同步工具改进：增加dbAdaptor属性配置，通过定制自己的dbAdaptor可以非常方便地实现bboss本身不支持的数据库的数据同步工作

2. 数据同步工具改进：支持xxjob分布式定时任务引擎来调度同步作业任务

3. 数据同步工具改进：支持quartz定时任务引擎来调度同步作业任务

4. 数据同步工具改进：过滤器Context增加修改字段名称title为新名称newTitle并且修改字段的值api，使用方法，

   ```java
   //修改字段名称title为新名称newTitle，并且修改字段的值
   context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
   ```

5. 更新bboss框架版本5.3.0

# v5.6.3 功能改进

1. 调整可变长数组为固定长度数组
2. 更新bboss框架版本5.2.9

# v5.6.2 功能改进

1. 增加地理信息库模块，同步数据时，可以将ip字段对应的ip转换为对应的运营商、城市国家、地理坐标信息
2. 改进增量数据同步功能：增加配置增量状态存储到外部数据库，以便分布式任务调度执行时共享增量同步状态信息

# v5.6.1 功能改进

1. Elasticsearch 7.0.0兼容性改造：[提供一组不带索引类型的API](Elasticsearch-7-API.md)，涉及批处理api和数据同步工具
2. Elasticsearch 7.0.0兼容性改造：处理hits.total类型为Object的问题，涉及获取文档api和检索api

   3.Elasticsearch 7.0.0兼容性改造：处理bulk处理时routing字段名称变更问题，涉及批处理api和数据同步工具

# v5.6.0 功能改进

1.修改bboss框架版本号为5.2.7

2.http连接池超时，sockettimeout，connectiontimeout异常信息添加超时时间信息

3.修改数据同步任务TaskCall中的空指针异常

# v5.5.9 功能改进

解决从http连接池获取连接超时，将服务器标注为不可用问题

# v5.5.8 功能改进

1. 增加更新索引配置通用方法
2. 增加更新集群setting通用api 
3. 增加synflush indice api
4. 增加获取表明日期格式的方法
5. 数据同步工具bug修复：oracle.sql.TIMESTAMP类型的增量字段取值转换不正确

# v5.5.7 功能改进：
1.修复bug：处理构建文档时，可能存在的格式不正确的问题

2.修复bug：scrollParallel查询时，没有数据记录报任务存在空指针问题 

3.api改进：增加管理索引副本的方法

4.数据同步：处理持久层检索map对象时，clob或者blob对象自动转换为String的功能

5.数据同步：数据同步工具开启全局db connection共享事务，在数据导入任务执行过程中，开启共享连接功能，避免重复从连接池中申请连接，提升导入性能

# v5.5.6 功能改进：

1. agg统计：将Integer类型key强制转换String类型错误bug

2. dsl中片段变量，可以引用外部文件中定义的片段

   片段引用测试用例：[TestPianduan.java](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/dsl/TestPianduan.java)  [pianduan.xml](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/pianduan.xml)  [outpianduanref.xml](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/outpianduanref.xml)

3. 增加数据导入任务异常处理回调方法:[参考文档](https://esdoc.bbossgroups.com/#/db-es-tool?id=_47-%E8%AE%BE%E7%BD%AE%E4%BB%BB%E5%8A%A1%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C%E5%9B%9E%E8%B0%83%E5%A4%84%E7%90%86%E5%87%BD%E6%95%B0)

# **v5.5.3 功能改进：**

1. 完善orm保存对象到es序列化机制，避免非ESBaseData和ESId对象的相关属性被忽略掉

2. 数据同步工具支持mysql8,tidb,hive

3. 完善http组件：自定义重试机制 
   http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.ConnectionResetHttpRequestRetryHandler
   空闲连接校验频率，单位毫秒，>0起作用http.validateAfterInactivity=3000
   每次获取connection时校验连接，true，校验，默认false
   http.staleConnectionCheckEnabled=false 
   
4. 依赖的http组件版本升级：
   httpclient, version: '4.5.6'
   httpcore, version: '4.4.11'
   httpmime, version: '4.5.6'
   
5. 完善框架打印的日志信息

6. 修复释放资源时抛出空指针异常

7. 数据同步工具改进：可以按日期时间自动分表，使用方法importBuilder
   .setIndex("dbdemo-{yyyy.MM.dd}") //通过{yyyy.MM.dd}设置按日期分索引表
   
8. 数据同步工具改进：解决忽略字段名称小写时不起作用的问题

9. 新增GeoPoint和GeoShape两个对象

10. 增加单值聚合查询的api和测试用例
    参考文档：<https://esdoc.bbossgroups.com/#/agg>
    
11. 增加open/close index方法

12. 批量修改/添加文档api完善：增加指定对象字段名称对应的值作为文档id和文档父id

13. 增加一组通用api：通过ClientOptions/UpdateOptions指定控制参数对应的对象字段，替代原有的@ESId,@ESParentId等注解

14. 优化dsl配置文件热加载机制：解决jar中dsl配置文件热加载问题

15. 新增一个基于spring boot2的web demo
    <https://github.com/bbossgroups/es_bboss_web>
    
16. 修复数据同步工具bug：解决增量同步线程池重复创建问题，建议大家将版本升级到5.5.3

    本问题会导致长时间运行抛出以下错误：

    ![img](images\oom.png)

17. 完善对elasticsearch 1.x版本的支持,searchallparallel方法支持es 1.x版本

18. 数据同步工具：elasticsearch同步到dbes增加scroll parallel导出功能

19. 数据导出工具: 任务执行结果处理接口，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务进行相应处理，参考文档：
    <https://esdoc.bbossgroups.com/#/db-es-tool>

20. 数据同步工具：规范并修改相关类的名称

21. sclice scroll检索内部采用异步方式执行每个scroll查询结果

22. scroll检索增加异步处理每个scroll查询结果的功能

23. 数据同步工具：增加在过滤器中过滤记录功能 

24. Innerhit检索时层级超过2级的对象（继承ESBaseData对象）中没有设置文档id等信息问题修复

更多功能改进请浏览：[commit](https://gitee.com/bboss/bboss-elastic/commits/master)

# **bboss elasticsearch 使用参考文档** 

[https://esdoc.bbossgroups.com](https://esdoc.bbossgroups.com/)  



# 相关链接

- bboss-elastic 的详细介绍：[点击查看](README.md)
- bboss-elastic 的下载地址：[点击下载](https://gitee.com/bboss/bboss-elastic)

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>
<img src="images/alipay.png"  height="200" width="200">



