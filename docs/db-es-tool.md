# 数据库数据导入Elasticsearch案例分享

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

*The best elasticsearch highlevel java rest api-----bboss* 

基于bboss持久层和bboss elasticsearch客户端实现数据库数据导入es案例分享（支持各种数据库和各种es版本）

通过bboss，可以非常方便地将数据库表数据导入到es中：

- 支持逐条数据导入
- 批量数据导入
- 批量数据多线程并行导入
- 定时全量（串行/并行）数据导入
- 定时增量（串行/并行）数据导入

支持的数据库： mysql,maridb，postgress,oracle ,sqlserver,db2,tidb,hive等

支持的Elasticsearch版本： 1.x,2.x,5.x,6.x,7.x,+

支持海量PB级数据同步导入功能

支持将ip转换为对应的运营商和城市地理坐标位置信息

**支持设置数据bulk导入任务结果处理回调函数，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务通过error和exception方法进行相应处理**

支持多种定时任务执行引擎：

- jdk timer （内置）
- quartz
- xxjob

下面详细介绍本案例。



# 1.案例对应的源码

批量导入：https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java

定时增量导入：https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java



# 2.在工程中导入jdbc es maven坐标

```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
<version>5.6.9</version>
</dependency>
```
如果需要增量导入，还需要导入sqlite驱动：

```xml
<dependency>
      <groupId>org.xerial</groupId>
      <artifactId>sqlite-jdbc</artifactId>
      <version>3.23.1</version>
      <scope>compile</scope>
 </dependency>
```

如果需要使用xxjob来调度作业任务，还需要导入坐标：

```xml
<dependency>
      <groupId>com.xuxueli</groupId>
      <artifactId>xxl-job-core</artifactId>
      <version>2.0.2</version>
      <scope>compile</scope>
 </dependency>
```

本文从mysql数据库表td_cms_document导入数据到es中，除了导入上述maven坐标，还需要额外导入mysql驱动坐标(其他数据库驱动程序自行导入)：

```xml
<dependency>
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
<version>5.1.40</version>
</dependency>
```

# 3.索引表结构定义

Elasticsearch会在我们导入数据的情况下自动创建索引mapping结构，如果对mapping结构有特定需求或者自动创建的结构不能满足要求，可以自定义索引mapping结构，在导入数据之前创建好自定义的mapping结构或者mapping模板即可，具体定义和创建方法参考文档： [Elasticsearch索引表和索引表模板管理](index-indextemplate.md) 

# 4.配置es地址

新建application.properties文件，内容为：

```
elasticsearch.rest.hostNames=10.21.20.168:9200
## 集群地址用逗号分隔
#elasticsearch.rest.hostNames=10.180.211.27:9200,10.180.211.28:9200,10.180.211.29:9200
```



# 5.编写简单的导入代码

批量导入关键配置：

importBuilder.setBatchSize(5000)

mysql需要在application.properties文件配置连接串和指定fetch相关的useCursorFetch和jdbcFetchSize参数：

```properties
db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false

db.jdbcFetchSize = 10000

```

## **5.1同步批量导入**

```java
	public void testSimpleImportBuilder(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		try {
			//清除测试表数据
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbclobdemo");
		}
		catch (Exception e){

		}
		//数据源相关配置，可选项，可以在外部启动数据源
		importBuilder.setDbName("test")
				.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效  
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(false);//是否使用连接池


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑
		importBuilder.setSql("select * from td_cms_document");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //必填项
				.setRefreshOption(null)//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(5000)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setJdbcFetchSize(10000);//设置数据库的查询fetchsize，同时在mysql url上设置useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，jdbcFetchSize必须和useCursorFetch参数配合使用，否则不会生效  


		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();
	}
```

可以直接运行上述代码，查看数据导入效果。



## **5.2 异步批量导入**

异步批量导入关键配置：

```java
        importBuilder.setParallel(true);//设置为多线程异步并行批量导入
		importBuilder.setQueue(100);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(200);//设置批量导入线程池工作线程数量
```

示例代码如下：

```java
	public void testSimpleLogImportBuilderFromExternalDBConfig(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		try {
			//清除测试表
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbdemo");
		}
		catch (Exception e){

		}
        //数据源相关配置，可选项，可以在外部启动数据源
		importBuilder.setDbName("test")
				.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true")//通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效  
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(false);//是否使用连接池


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑
		importBuilder.setSql("select * from td_sm_log");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbdemo") //必填项
				.setIndexType("dbdemo") //必填项
				.setRefreshOption(null)//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(5000)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setJdbcFetchSize(10000);//设置数据库的查询fetchsize，同时在mysql url上设置useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，jdbcFetchSize必须和useCursorFetch参数配合使用，否则不会生效  

		/**
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入
		importBuilder.setQueue(100);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(200);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行 
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
		importBuilder.setRefreshOption("refresh"); // 为了实时验证数据导入的效果，强制刷新数据，生产环境请设置为null或者不指定
		
		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();
		
		long count = ElasticSearchHelper.getRestClientUtil().countAll("dbdemo");
		System.out.println("数据导入完毕后索引表dbdemo中的文档数量:"+count);
	}
```

说明：从数据库检索数据放入批处理列表，到达batchsize就提交一次作业，最多threadcount个工作线程并行处理作业，如果线程都在忙，没有空闲的工作线程，那么作业就会放到队列里面排队，如果队列也满了，则会阻塞等待释放的队列位置，每等待100次打印一次等待次数的日志。

batchsize，queue，threadcount的配置要结合服务器的内存和cpu配置来设置，设置大了容易内存溢出，设置小了影响处理速度，所以要权衡考虑。
![img](https://oscimg.oschina.net/oscnet/a76c3326532dbe769ee9c9ff8ed4b3fe0c5.jpg)

导入的时候需要观察elasticsearch服务端的write线程池的状态，如果出现reject任务的情况，就需要调优elasticsearch配置参数：

thread_pool.bulk.queue_size: 1000   es线程等待队列长度

thread_pool.bulk.size: 10   线程数量，与cpu的核数对应

## 5.3 一个有字段属性映射的稍微复杂案例实现

```java
	public void testImportBuilder(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		try {
			//清除测试表
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbclobdemo");
		}
		catch (Exception e){

		}
		//数据源相关配置，可选项，可以在外部启动数据源
		importBuilder.setDbName("test")
				.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true")//通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效  
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(false);//是否使用连接池


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑
		importBuilder.setSql("select * from td_cms_document");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //必填项
				.setRefreshOption(null)//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setEsIdField("documentId")//可选项
				.setEsParentIdField(null) //可选项,如果不指定，es自动为文档产生id
				.setRoutingValue(null) //可选项		importBuilder.setRoutingField(null);
				.setEsDocAsUpsert(true)//可选项
				.setEsRetryOnConflict(3)//可选项
				.setEsReturnSource(false)//可选项
				.setEsVersionField(null)//可选项
				.setEsVersionType(null)//可选项
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") //可选项,默认日期格式
				.setLocale("zh_CN")  //可选项,默认locale
				.setTimeZone("Etc/UTC")  //可选项,默认时区
				.setBatchSize(5000)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setJdbcFetchSize(10000);//设置数据库的查询fetchsize，同时在mysql url上设置useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，jdbcFetchSize必须和useCursorFetch参数配合使用，否则不会生效  

		/**
		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
		 */
		importBuilder.addFieldMapping("document_id","docId")
					 .addFieldMapping("docwtime","docwTime")
					 .addIgnoreFieldMapping("channel_id");//添加忽略字段
		/**
		 * 为每条记录添加额外的字段和值
		 * 可以为基本数据类型，也可以是复杂的对象
		 */
		importBuilder.addFieldValue("testF1","f1value");
		importBuilder.addFieldValue("testInt",0);
		importBuilder.addFieldValue("testDate",new Date());
		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
		TestObject testObject = new TestObject();
		testObject.setId("testid");
		testObject.setName("jackson");
		importBuilder.addFieldValue("testObject",testObject);

        /**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
			    //可以根据条件定义是否丢弃当前记录				
				//if(something is true) {
				//	context.setDrop(true);
				//	return;
				//}
				CustomObject customObject = new CustomObject();
				customObject.setAuthor((String)context.getValue("author"));
				customObject.setTitle((String)context.getValue("title"));
				customObject.setSubtitle((String)context.getValue("subtitle"));
				context.addFieldValue("docInfo",customObject);//如果还需要构建更多的内部对象，可以继续构建

				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
				context.addIgnoreFieldMapping("author");
				context.addIgnoreFieldMapping("title");
				context.addIgnoreFieldMapping("subtitle");
			}
		});

		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();
	}
```



## 5.4 指定自定义文档id机制：

```
importBuilder.setEsIdGenerator(new EsIdGenerator() {
			//如果指定EsIdGenerator，则根据下面的方法生成文档id，
			// 否则根据setEsIdField方法设置的字段值作为文档id，
			// 如果既没有配置EsIdField也没有指定EsIdGenerator，则由es自动生成文档id

			@Override
			public Object genId(Context context) throws Exception {
				return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
			}
		});
```



## 5.5 定时增量导入

源码文件 <https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java>

```java
	public void testSimpleLogImportBuilderFromExternalDBConfig(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		//增量定时任务不要删表，但是可以通过删表来做初始化操作
//		try {
//			//清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
//			ElasticSearchHelper.getRestClientUtil().dropIndice("dbdemo");
//		}
//		catch (Exception e){
//
//		}

		//数据源相关配置，可选项，可以在外部启动数据源
		importBuilder.setDbName("test")
				.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true")//通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效  
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(true);//是否使用连接池

		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，设置增量变量log_id
		importBuilder.setSql("select * from td_sm_log where log_id > #[log_id]");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbdemo") //必填项
				.setIndexType("dbdemo") //必填项
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(5000)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setJdbcFetchSize(10000);//设置数据库的查询fetchsize，同时在mysql url上设置useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，jdbcFetchSize必须和useCursorFetch参数配合使用，否则不会生效  
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				     .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
					 .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
//		importBuilder.setNumberLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
//		importBuilder.setNumberLastValueColumn("log_id");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
		importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("testdb");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点
//		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
		importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
		// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型

//		importBuilder.

		/**
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
		importBuilder.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id
		importBuilder.setDebugResponse(true);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在需要的时候才，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度
		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作

		System.out.println();

		
	}
```

可以为同步定时任务指定执行拦截器，示例如下：

```java
        //设置任务执行拦截器，可以添加多个
		importBuilder.addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				System.out.println("preCall");
			}

			@Override
			public void afterCall(TaskContext taskContext) {
				System.out.println("afterCall");
			}

			@Override
			public void throwException(TaskContext taskContext, Exception e) {
				System.out.println("throwException");
			}
		}).addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				System.out.println("preCall 1");
			}

			@Override
			public void afterCall(TaskContext taskContext) {
				System.out.println("afterCall 1");
			}

			@Override
			public void throwException(TaskContext taskContext, Exception e) {
				System.out.println("throwException 1");
			}
		});
```



## 5.6 定时全量导入

```java
	public void testSimpleLogImportBuilderFromExternalDBConfig(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		

		//数据源相关配置，可选项，可以在外部启动数据源
		importBuilder.setDbName("test")
				.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true")//通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效  
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(true);//是否使用连接池

		//指定导入数据的sql语句，必填项，定时全量导入不需要在sql中设置增量字段
		importBuilder.setSql("select * from td_sm_log ");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbdemo") //必填项
				.setIndexType("dbdemo") //必填项
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(5000)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setJdbcFetchSize(10000);//设置数据库的查询fetchsize，同时在mysql url上设置useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，jdbcFetchSize必须和useCursorFetch参数配合使用，否则不会生效  
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				     .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
					 .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次




		/**
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
		importBuilder.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id
		importBuilder.setDebugResponse(true);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在需要的时候才，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度
		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作

		System.out.println();

		
	}
```

## 5.7 定时任务调度说明

定时增量导入的关键配置：

**sql语句指定增量字段**

//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，设置增量变量log_id
		importBuilder.setSql("select * from td_sm_log where log_id > **#[log_id]**");

bboss自动提取log_id作为增量字段，目前支持number和timestamp两种类型，如果是时间戳，还需要指定一下类型：

```java
importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE );//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
		// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
```

对于修改增量的同步，一般用修改时间戳来作为增量同步字段，同时将数据库记录主键作为文档ID：

```java
importBuilder.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id
```

指定定时timer**

```java
importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				     .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
					 .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
```

上面说明的是基于jdk timer组件的定时调度，bboss还可以通过[quartz](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzImportTask.java)、[xx-job](https://github.com/bbossgroups/db-elasticsearch-xxjob)、elastic-job（目前未支持）来定时调度同步作业，进行以下配置指示bboss采用外部任务调度器：

```java
//采用外部定时任务
importBuilder.setExternalTimer(true);
```

采用分布式作业调度引擎时，定时增量导入需要指定增量状态存储数据库：[保存增量状态的数据源配置](https://esdoc.bbossgroups.com/#/db-es-tool?id=%e4%bf%9d%e5%ad%98%e5%a2%9e%e9%87%8f%e7%8a%b6%e6%80%81%e7%9a%84%e6%95%b0%e6%8d%ae%e6%ba%90%e9%85%8d%e7%bd%ae)

## 5.8 基于xxjob 同步DB-Elasticsearch数据

bboss结合xxjob分布式定时任务调度引擎，可以非常方便地实现强大的shard分片分布式同步数据库数据到Elasticsearch功能，比如从一个10亿的数据表中同步数据，拆分为10个任务分片节点执行，每个节点同步1个亿，速度会提升10倍左右；同时提供了同步作业的故障迁移容灾能力。

### 首先定义一个xxjob的同步作业

```java
package org.frameworkset.elasticsearch.imp.jobhandler;


import com.xxl.job.core.util.ShardingUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.DB2ESImportBuilder;
import org.frameworkset.elasticsearch.client.schedule.ExternalScheduler;
import org.frameworkset.elasticsearch.client.schedule.ImportIncreamentConfig;
import org.frameworkset.elasticsearch.client.schedule.xxjob.AbstractDB2ESXXJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: 使用quartz等外部环境定时运行导入数据，需要设置：</p>
 * importBuilder.setExternalTimer(true);
 */
public class XXJobImportTask extends AbstractDB2ESXXJobHandler {
   private static Logger logger = LoggerFactory.getLogger(XXJobImportTask.class);
   public void init(){
      // 可参考Sample示例执行器中的示例任务"ShardingJobHandler"了解试用

      externalScheduler = new ExternalScheduler();
      //params是xxjob admin管理控制台为作业配置的参数数据 
      externalScheduler.dataStream((Object params)->{
         //在作业中获取分片信息
         ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
         int index = 0;
         if(shardingVO != null) {
            index = shardingVO.getIndex();
            logger.info("index:>>>>>>>>>>>>>>>>>>>" + shardingVO.getIndex());
            logger.info("total:>>>>>>>>>>>>>>>>>>>" + shardingVO.getTotal());
         }
         logger.info("params:>>>>>>>>>>>>>>>>>>>" + params);
         DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
         //增量定时任务不要删表，但是可以通过删表来做初始化操作
//    if(dropIndice) {
         try {
            //清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
            String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("quartz");
            System.out.println(repsonse);
         } catch (Exception e) {
         }
//    }


         //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
         // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
         // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
         // log_id和数据库对应的字段一致,就不需要设置setNumberLastValueColumn和setNumberLastValueColumn信息，
         // 但是需要设置setLastValueType告诉工具增量字段的类型
		 //在sql中设置分片相关的参数,以index为例简单说明，可以结合index和param参数实现更加复杂的分片机制
         importBuilder.setSql("select * from td_sm_log where shardColumn ="+ index +" and log_id > #[log_id]");
         importBuilder.addIgnoreFieldMapping("remark1");
//    importBuilder.setSql("select * from td_sm_log ");
         /**
          * es相关配置
          */
         importBuilder
               .setIndex("quartz") //必填项
               .setIndexType("quartz") //必填项
//          .setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
               .setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
               .setUseLowcase(false)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
               .setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
               .setBatchSize(10);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

         //定时任务配置，
         //采用内部定时任务
//    importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
////                .setScheduleDate(date) //指定任务开始执行时间：日期
//          .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
//          .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
         //采用外部定时任务
         importBuilder.setExternalTimer(true);
         //定时任务配置结束
 
         //增量配置开始
//    importBuilder.setNumberLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
//    importBuilder.setNumberLastValueColumn("log_id");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
         importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
         importBuilder.setLastValueStorePath("logtable_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
         importBuilder.setLastValueStoreTableName("logs"+index);//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab，如果xxjob是shard分片模式运行，每个分片需要独立的增量记录桩体表
         importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
         // 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
         //增量配置结束

         //映射和转换配置开始
//    /**
//     * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//     * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
//     */
//    importBuilder.addFieldMapping("document_id","docId")
//          .addFieldMapping("docwtime","docwTime")
//          .addIgnoreFieldMapping("channel_id");//添加忽略字段
//
//
//    /**
//     * 为每条记录添加额外的字段和值
//     * 可以为基本数据类型，也可以是复杂的对象
//     */
//    importBuilder.addFieldValue("testF1","f1value");
//    importBuilder.addFieldValue("testInt",0);
//    importBuilder.addFieldValue("testDate",new Date());
//    importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
//    TestObject testObject = new TestObject();
//    testObject.setId("testid");
//    testObject.setName("jackson");
//    importBuilder.addFieldValue("testObject",testObject);
//
//    /**
//     * 重新设置es数据结构
//     */
//    importBuilder.setDataRefactor(new DataRefactor() {
//       public void refactor(Context context) throws Exception  {
//          CustomObject customObject = new CustomObject();
//          customObject.setAuthor((String)context.getValue("author"));
//          customObject.setTitle((String)context.getValue("title"));
//          customObject.setSubtitle((String)context.getValue("subtitle"));
//          customObject.setIds(new int[]{1,2,3});
//          context.addFieldValue("docInfo",customObject);//如果还需要构建更多的内部对象，可以继续构建
//
//          //上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//          context.addIgnoreFieldMapping("author");
//          context.addIgnoreFieldMapping("title");
//          context.addIgnoreFieldMapping("subtitle");
//       }
//    });
         //映射和转换配置结束

         /**
          * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
          */
         importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
         importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
         importBuilder.setThreadCount(5);//设置批量导入线程池工作线程数量
         importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
         importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
         importBuilder.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id
//    importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在调试需要的时候才打开，log日志级别同时要设置为INFO
//    importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度

         importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
         importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
         /**
          importBuilder.setEsIdGenerator(new EsIdGenerator() {
          //如果指定EsIdGenerator，则根据下面的方法生成文档id，
          // 否则根据setEsIdField方法设置的字段值作为文档id，
          // 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id

          @Override
          public Object genId(Context context) throws Exception {
          return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
          }
          });
          */
         /**
          * 执行数据库表数据导入es操作
          */
         return importBuilder;
      });

   }


}
```

### 作业注册配置

然后将作业配置到application.propperties中：

```properties
##
# 作业任务配置
# xxl.job.task为前置配置多个数据同步任务，后缀XXJobImportTask和OtherTask将xxjob执行任务的名称
# 作业程序都需要继承抽象类org.frameworkset.elasticsearch.client.schedule.xxjob.AbstractDB2ESXXJobHandler
# public void init(){
#     externalScheduler = new ExternalScheduler();
#     externalScheduler.dataStream(()->{
#         DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
#              编写导入作业任务配置逻辑，参考文档：https://esdoc.bbossgroups.com/#/db-es-tool
#         return    importBuilder;
#       }
# }
#

xxl.job.task.XXJobImportTask = org.frameworkset.elasticsearch.imp.jobhandler.XXJobImportTask
## xxl.job.task.otherTask = org.frameworkset.elasticsearch.imp.jobhandler.OtherTask
```

### 任务构建和运行

1. 下载完整的demo
   https://github.com/bbossgroups/db-elasticsearch-xxjob

2.  安装和配置gradle

   https://esdoc.bbossgroups.com/#/bboss-build

3. 在工程db-elasticsearch-xxjob根目录下运行gradle构建指令

   gradle clean releaseVersion

4. 启动作业

构建成功后，将会在工程目录下面生成可部署的二进制包：

build/distributions/db-elasticsearch-xxjob-released.zip

解压运行里面的指令，即可启动作业：

windows: restart.bat 

linux: restart.sh

### xxjob运行效果

任务启动后，可以在xxjob的挂你控制台看到刚注册的执行器和作业：

注册好的作业执行器

![作业执行器](images\xxjob-executor.png)

编辑执行器

![编辑执行器](images\xxjobexe-editor.png)

任务管理

![任务管理](images\joblist.png)

可以在后面的操作去，执行、启动作业，查看作业调度执行日志，修改作业参数和运行模式：

![](images\jobedit.png)

运行报表

![运行报表](images\jobstatic.png)

## 5.9 灵活控制文档数据结构

bboss提供org.frameworkset.elasticsearch.client.DataRefactor接口来提供对数据记录的自定义处理功能，这样就可以灵活控制文档数据结构，举例说明如下：

```java
final AtomicInteger s = new AtomicInteger(0);
      /**
       * 重新设置es数据结构
       */
      importBuilder.setDataRefactor(new DataRefactor() {
         public void refactor(Context context) throws Exception  {
            //可以根据条件定义是否丢弃当前记录
            //context.setDrop(true);return;
            if(s.incrementAndGet() % 2 == 0) {
               context.setDrop(true);
               return;
            }
            //空值处理，判断字段content的值是否为空
            if(context.getValue("content") == null){
               context.addFieldValue("content","");//将content设置为""
            }

            CustomObject customObject = new CustomObject();
            customObject.setAuthor((String)context.getValue("author"));
            customObject.setTitle((String)context.getValue("title"));
            customObject.setSubtitle((String)context.getValue("subtitle"));

            customObject.setIds(new int[]{1,2,3});
            context.addFieldValue("author",customObject);
            long testtimestamp = context.getLongValue("testtimestamp");//将long类型的时间戳转换为Date类型
            context.addFieldValue("testtimestamp",new Date(testtimestamp));//将long类型的时间戳转换为Date类型
//修改字段名称title为新名称newTitle，并且修改字段的值
				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
				

            //上述属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性

            
            context.addIgnoreFieldMapping("subtitle");

            //关联查询数据,单值查询
            //sql中有多个条件用逗号分隔追加
				Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
																"select * from head where billid = ? and othercondition= ?",
																context.getIntegerValue("billid"),"otherconditionvalue");
             
            //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("headdata",headdata);
            //关联查询数据,多值查询
            List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
                  "select * from facedata where billid = ?",
                  context.getIntegerValue("billid"));
            //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("facedatas",facedatas);
         }
      });
      //映射和转换配置结束
```

***注意：内嵌的数据库查询会有性能损耗，在保证性能的前提下，尽量将内嵌的sql合并的外部查询数据的整体的sql中，或者采用缓存技术消除内部sql查询。***

## 5.10 IP转换为地理坐标城市运营商信息

在DataRefactor中，可以获取ip对应的运营商和区域信息，举例说明：

```java
/**
       * 重新设置es数据结构，获取ip对应的运营商和区域信息案例
       */
      importBuilder.setDataRefactor(new DataRefactor() {
         public void refactor(Context context) throws Exception  {
    
            /**
             * 获取ip对应的运营商和区域信息，remoteAddr字段存放ip信息
             */
            IpInfo ipInfo = context.getIpInfo("remoteAddr");
            context.addFieldValue("ipInfo",ipInfo);
            context.addFieldValue("collectTime",new Date());
            
         }
      });
```

需要在appliction.properties文件中配置geoip的ip地址信息库：

```properties
ip.cachesize = 2000
# geoip的ip地址信息库下载地址https://dev.maxmind.com/geoip/geoip2/geolite2/
ip.database = E:/workspace/hnai/terminal/geolite2/GeoLite2-City.mmdb
ip.asnDatabase = E:/workspace/hnai/terminal/geolite2/GeoLite2-ASN.mmdb
```

## 5.11 设置任务执行结果回调处理函数

我们通过importBuilder的setExportResultHandler方法设置任务执行结果以及异常回调处理函数，函数实现接口即可：

org.frameworkset.elasticsearch.client.ExportResultHandler

```java
//设置数据bulk导入任务结果处理回调函数，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务通过error方法进行相应处理
importBuilder.setExportResultHandler(new ExportResultHandler<String,String>() {
   @Override
   public void success(TaskCommand<String,String> taskCommand, String result) {
      String datas = taskCommand.getDatas();//执行的批量数据
      System.out.println(result);//打印成功结果
   }

   @Override
   public void error(TaskCommand<String,String> taskCommand, String result) {
      //具体怎么处理失败数据可以自行决定,下面的示例显示重新导入失败数据的逻辑：
      // 从result中分析出导入失败的记录，然后重新构建data，设置到taskCommand中，重新导入，
      // 支持的导入次数由getMaxRetry方法返回的数字决定
      // String failDatas = ...;
      //taskCommand.setDatas(failDatas);
      //taskCommand.execute();
      String datas = taskCommand.getDatas();//执行的批量数据
      System.out.println(result);//打印成功结果
   }
@Override
			public void exception(TaskCommand<String, String> taskCommand, Exception exception) {
				//任务执行抛出异常，失败处理方法,特殊的异常可以调用taskCommand的execute方法重试
     			if(need retry)
     				taskCommand.execute();
			}
   /**
    * 如果对于执行有错误的任务，可以进行修正后重新执行，通过本方法
    * 返回允许的最大重试次数
    * @return
    */
   @Override
   public int getMaxRetry() {
      return -1;
   }
});
```

## 5.12 灵活指定索引名称和索引类型

```java
importBuilder
				.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //必填项
```

index和type可以有以下几种动态生成方法：

```java
索引名称由demowithesindex和日期类型字段agentStarttime通过yyyy.MM.dd格式化后的值拼接而成
dbclobdemo-{agentStarttime,yyyy.MM.dd}
 
索引名称由demowithesindex和当前日期通过yyyy.MM.dd格式化后的值拼接而成
demowithesindex-{dateformat=yyyy.MM.dd}

索引名称由demowithesindex和日期类型字段agentStarttime通过yyyy.MM.dd格式化后的值拼接而成
demowithesindex-{field=agentStarttime,dateformat=yyyy.MM.dd}

索引类型为typeFieldName字段对应的值:
{field=typeFieldName}
或者{typeFieldName}
```



# 6.数据导入工具使用方法

上面介绍了数据库数据同步到数据库的各种用法，bboss还提供了一个样板demo工程:[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)，用来将写好的同步代码打包发布成可以运行的二进制包上传到服务器运行，[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)提供了现成的运行指令和jvm配置文件：

## 同步代码主程序定义-Dbdemo

Dbdemo-提供了上述文中提供的各种导入数据的方法，可以根据自己的要求实效自己的方法逻辑，然后在Dbdemo的main方法中指定要执行的方法即可：

```java
public static void main(String args[]){
   Dbdemo dbdemo = new Dbdemo();
   boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值
   dbdemo.scheduleFullImportData(  dropIndice);
}
```

## es数据源配置

```properties
# elasticsearch客户端配置
##x-pack或者searchguard账号和口令
elasticUser=elastic
elasticPassword=changeme

#elasticsearch.rest.hostNames=10.1.236.88:9200
#elasticsearch.rest.hostNames=127.0.0.1:9200
#elasticsearch.rest.hostNames=10.21.20.168:9200
elasticsearch.rest.hostNames=192.168.137.1:9200
#elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
elasticsearch.dateFormat=yyyy.MM.dd
elasticsearch.timeZone=Asia/Shanghai
elasticsearch.ttl=2d
#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
elasticsearch.showTemplate=false
elasticsearch.discoverHost=false

##elasticsearch客户端使用的http连接池配置
http.timeoutConnection = 5000
http.timeoutSocket = 50000
http.connectionRequestTimeout=10000
http.retryTime = 1
http.maxLineLength = -1
http.maxHeaderCount = 200
http.maxTotal = 200
http.defaultMaxPerRoute = 100
http.soReuseAddress = false
http.soKeepAlive = false
http.timeToLive = 3600000
http.keepAlive = 3600000
http.keystore =
http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
http.hostnameVerifier =

# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
dslfile.refreshInterval = 3000
```

## 数据库数据源配置

以mysql未来介绍数据源配置：

```properties
db.name = test  #数据源名称，持久层通过dbname引用定义的数据源
db.user = root
db.password = 123456
db.driver = com.mysql.jdbc.Driver
db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
db.usePool = true
db.validateSQL = select 1
db.jdbcFetchSize = 10000
db.showsql = true
```

## 自定义适配器数据源配置

定义达梦数据库的适配器： 

 dbAdaptor专用于设置不支持的数据库类型名称和数据库适配器，方便用户扩展不支持的数据库的数据导入

 dbAdaptor必须继承自com.frameworkset.orm.adapter.DB或者其继承DB的类 

```java
package org.frameworkset.elasticsearch.imp;
import com.frameworkset.orm.adapter.DBOracle;

/**
 * <p>Description: 达梦数据库adaptor</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/4/21 13:28
 * @author biaoping.yin
 * @version 1.0
 */
public class DMAdaptor extends DBOracle {
}
```

在配置文件application.properties中指定自定义数据源适配器

```properties
# 国产数据库达梦数据源配置，展示额外定制的达梦dbAdaptor，
# 通过定制自己的dbAdaptor可以非常方便地实现bboss本身不支持的数据库的数据同步工作
#   /**
#   * dbtype专用于设置不支持的数据库类型名称和数据库适配器，方便用户扩展不支持的数据库的数据导入
#   * 可选字段，设置了dbAdaptor可以不设置dbtype，默认为数据库driver类路径
#   */
#  private String dbtype ;
#  /**
#   * dbAdaptor专用于设置不支持的数据库类型名称和数据库适配器，方便用户扩展不支持的数据库的数据导入
#   * dbAdaptor必须继承自com.frameworkset.orm.adapter.DB或者其继承DB的类
#   */
db.name = test
db.user = username
db.password = password
db.driver = dm.jdbc.driver.DmDriver
db.url = jdbc:dm://localhost:12345/dbname
db.usePool = true
db.validateSQL = select 1
db.jdbcFetchSize = 10000
db.showsql = true
db.dbtype = dm
#指定dm数据库适配器
db.dbAdaptor = org.frameworkset.elasticsearch.imp.DMAdaptor
```

api方式配置自定义适配器：

```java
importBuilder.setDbAdaptor("org.frameworkset.elasticsearch.imp.DMAdaptor");
```

## 保存增量状态的数据源配置

采用分布式作业调度引擎时，定时增量导入需要指定保存增量状态的数据源：

```properties
# 增量导入状态存储数据源配置，默认采用sqlite，增量导入装存储到本地的sqlite数据库中，采用分布式的外部定时任务引擎时，
# 就不能将状态存储到本地，需要采用外部的数据库（mysql,oracle等）来存储增量导入状态。
# 如果做了config.db配置，则采用配置的的数据源，必须指定创建statusTableName的建表语句，每种数据库对应的语法做适当调整
# create table $statusTableName  (ID number(2),lasttime number(10),lastvalue number(10),lastvaluetype number(1),PRIMARY KEY (ID))
#
# 一般情况下不需要使用外部状态数据源，除非采用分布式的外部定时任务引擎，
# 外部状态数据源可以直接使用上面的导入数据源
config.db.name=test
#config.db.name = testconfig
#config.db.user = root
#config.db.password = 123456
#config.db.driver = com.mysql.jdbc.Driver
#config.db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
#config.db.usePool = true
#config.db.validateSQL = select 1
#config.db.jdbcFetchSize = 10000
#config.db.showsql = true
### mysql
#config.db.statusTableDML = CREATE TABLE $statusTableName ( ID bigint(10) NOT NULL AUTO_INCREMENT, lasttime bigint(10) NOT NULL, lastvalue bigint(10) NOT NULL, lastvaluetype int(1) NOT NULL, PRIMARY KEY(ID)) ENGINE=InnoDB
```

## 测试以及调试同步代码

在test源码目录新增DbdemoTest类，并添加main方法，在其中添加Dbdemo执行代码即可运行调试同步代码：

```java
public static void main(String args[]){

      long t = System.currentTimeMillis();
      Dbdemo dbdemo = new Dbdemo();
      boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值
//    dbdemo.scheduleImportData(  dropIndice);//定时增量导入
      dbdemo.scheduleFullImportData(dropIndice);//定时全量导入
//    dbdemo.scheduleRefactorImportData(dropIndice);//定时全量导入，在context中排除remark1字段

//    dbdemo.scheduleFullAutoUUIDImportData(dropIndice);//定时全量导入，自动生成UUID
//    dbdemo.scheduleDatePatternImportData(dropIndice);//定时增量导入，按日期分表yyyy.MM.dd
   }
```

## 测试调试过程中异常说明

**如果在运行的过程中，出现以下问题，则说明在eclipse或者idea中开发调试的时候直接运行了Dbdemo，正确的做法是运行test下面的DbdemoTest：参考**[测试调试方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=%E6%B5%8B%E8%AF%95%E4%BB%A5%E5%8F%8A%E8%B0%83%E8%AF%95%E5%90%8C%E6%AD%A5%E4%BB%A3%E7%A0%81)

```
16:04:11.306 [main] ERROR org.frameworkset.elasticsearch.ElasticSearch - ElasticSearch Rest Client started failed
org.frameworkset.elasticsearch.client.NoServerElasticSearchException: All elasticServer [http://127.0.0.1:9200] can't been connected.
	at org.frameworkset.elasticsearch.client.RoundRobinList.get(RoundRobinList.java:97) ~[bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient._executeHttp(ElasticSearchRestClient.java:522) ~[bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient.discover(ElasticSearchRestClient.java:649) ~[bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient.discover(ElasticSearchRestClient.java:475) ~[bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.RestClientUtil.discover(RestClientUtil.java:1448) ~[bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient.initVersionInfo(ElasticSearchRestClient.java:194) ~[bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.ElasticSearchRestClient.init(ElasticSearchRestClient.java:232) ~[bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.ElasticSearch.start(ElasticSearch.java:363) [bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.ElasticSearchHelper.booter(ElasticSearchHelper.java:170) [bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot.boot(ElasticSearchConfigBoot.java:54) [bboss-elasticsearch-rest-booter-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot.boot(ElasticSearchConfigBoot.java:28) [bboss-elasticsearch-rest-booter-5.5.3.jar:?]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[?:1.8.0_162]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[?:1.8.0_162]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:1.8.0_162]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[?:1.8.0_162]
	at org.frameworkset.elasticsearch.ElasticSearchHelper.init(ElasticSearchHelper.java:279) [bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.ElasticSearchHelper.getRestClientUtil(ElasticSearchHelper.java:334) [bboss-elasticsearch-rest-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.imp.Dbdemo.scheduleFullImportData(Dbdemo.java:779) [classes/:?]
	at org.frameworkset.elasticsearch.imp.Dbdemo.main(Dbdemo.java:41) [classes/:?]

** ERROR: Unable to parse XML file poolman.xml: java.io.FileNotFoundException: E:\workspace\bbossgroups\security\poolman.xml (系统找不到指定的文件。)
16:04:12.781 [Timer-0] ERROR org.frameworkset.elasticsearch.client.schedule.ScheduleService - scheduleImportData failed:
java.lang.NullPointerException: 获取默认数据源名称失败：请确保数据源正常启动，检查配置文件是否配置正确.
	at com.frameworkset.common.poolman.util.SQLManager.getDefaultDBName(SQLManager.java:405) ~[bboss-persistent-5.2.2.jar:?]
	at com.frameworkset.common.poolman.PreparedDBUtil.<init>(PreparedDBUtil.java:97) ~[bboss-persistent-5.2.2.jar:?]
	at com.frameworkset.common.poolman.SQLInfoDBUtil.<init>(SQLInfoDBUtil.java:24) ~[bboss-persistent-5.2.2.jar:?]
	at com.frameworkset.common.poolman.SQLInfoExecutor.queryWithDBNameByNullRowHandler(SQLInfoExecutor.java:1449) ~[bboss-persistent-5.2.2.jar:?]
	at com.frameworkset.common.poolman.SQLExecutor.queryWithDBNameByNullRowHandler(SQLExecutor.java:1405) ~[bboss-persistent-5.2.2.jar:?]
	at org.frameworkset.elasticsearch.client.schedule.ScheduleService.scheduleImportData(ScheduleService.java:137) ~[bboss-elasticsearch-rest-jdbc-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.schedule.ScheduleService.access$200(ScheduleService.java:44) ~[bboss-elasticsearch-rest-jdbc-5.5.3.jar:?]
	at org.frameworkset.elasticsearch.client.schedule.ScheduleService$1.run(ScheduleService.java:219) [bboss-elasticsearch-rest-jdbc-5.5.3.jar:?]
	at java.util.TimerThread.mainLoop(Timer.java:555) [?:1.8.0_162]
	at java.util.TimerThread.run(Timer.java:505) [?:1.8.0_162]

Process finished with exit code -1
```



## 查看任务执行详细日志

如果要查看任务执行过程中的详细日志，只需设置以下参数即可：

```
importBuilder.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
```

这样在任务执行的时候会打印如下日志信息：

```
15:47:45.704 [DB2ESImportThread-1] DEBUG org.frameworkset.elasticsearch.client.TaskCall - Task[39] finish,import 10 records,Total import 390 records,Take time:432ms
15:47:45.704 [DB2ESImportThread-1] INFO  org.frameworkset.elasticsearch.client.TaskCall - Task[41] starting ......
15:47:45.704 [DB2ESImportThread-2] INFO  org.frameworkset.elasticsearch.client.TaskCall - Task[40] starting ......
15:47:46.238 [DB2ESImportThread-1] DEBUG org.frameworkset.elasticsearch.client.TaskCall - Task[41] finish,import 10 records,Total import 420 records,Take time:534ms
15:47:46.238 [DB2ESImportThread-2] DEBUG org.frameworkset.elasticsearch.client.TaskCall - Task[40] finish,import 10 records,Total import 410 records,Take time:534ms
15:47:46.238 [DB2ESImportThread-1] INFO  org.frameworkset.elasticsearch.client.TaskCall - Task[42] starting ......
15:47:46.530 [DB2ESImportThread-1] DEBUG org.frameworkset.elasticsearch.client.TaskCall - Task[42] finish,import 8 records,Total import 428 records,Take time:292ms
15:47:46.530 [main] INFO  org.frameworkset.elasticsearch.client.JDBCRestClientUtil - Complete tasks:43,Total import 428 records.
```



## 数据导入不完整原因分析及处理

如果在任务执行完毕后，发现es中的数据与数据库源表的数据不匹配，可能的原因如下：

**1.并行执行的过程中存在失败的任务（比如服务端超时），这种情况通过setExportResultHandler设置的exception监听方法进行定位分析**

```java
 public void exception(TaskCommand<String, String> taskCommand, Exception exception) {
//任务执行抛出异常，失败处理方法,特殊的异常可以调用taskCommand的execute方法重试
     if(need retry)
     	taskCommand.execute();
}
```

解决办法：

a) 优化elasticsearch服务器配置(加节点，加内存和cpu等运算资源，调优网络性能等)

b) 调整同步程序导入线程数、批处理batchSize参数，降低并行度。

```java
importBuilder.setBatchSize(10000);//每次bulk批处理的记录条数
importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
importBuilder.setQueue(100);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
```

c) 对于read或者等待超时的异常，亦可以调整bboss的application.properties文件中的http timeout时间参数

http.timeoutConnection = 50000

http.timeoutSocket = 50000



**2.任务执行完毕，但是存在es的bulk拒绝记录或者数据内容不合规的情况，这种情况就通过setExportResultHandler设置的error监听方法进行定位分析**

bulk拒绝记录解决办法：

a) 优化elasticsearch服务器配置(加节点，加内存和cpu等运算资源，调优网络性能等)

调整elasticsearch的相关线程和队列：调优elasticsearch配置参数

thread_pool.bulk.queue_size: 1000   es线程等待队列长度

thread_pool.bulk.size: 10   线程数量，与cpu的核数对应

b) 调整同步程序导入线程数、批处理batchSize参数，降低并行度。

数据内容不合规解决办法：拿到执行的原始批量数据，分析错误信息对应的数据记录，进行修改，然后重新导入失败的记录即可

```java
@Override
         public void error(TaskCommand<String,String> taskCommand, String result) {
            //任务执行完毕，但是结果中包含错误信息
            //具体怎么处理失败数据可以自行决定,下面的示例显示重新导入失败数据的逻辑：
            // 从result中分析出导入失败的记录，然后重新构建data，设置到taskCommand中，重新导入，
            // 支持的导入次数由getMaxRetry方法返回的数字决定
              String datas = taskCommand.getDatas();//拿到执行的原始批量数据，分析错误信息对应的数据记录，进行修改，然后重新导入失败的记录即可
            // String failDatas = ...;
            //taskCommand.setDatas(failDatas);
            //taskCommand.execute();
           
//          System.out.println(result);//打印成功结果
         }
```

## 作业运行jvm内存配置

修改jvm.options,设置作业运行需要的jvm内存，按照比例调整Xmx和MaxNewSize参数：

```properties
-Xms1g
-Xmx1g
-XX:NewSize=512m
-XX:MaxNewSize=512m
```

Xms和Xmx保持一样，NewSize和MaxNewSize保持一样，Xmx和MaxNewSize大小保持的比例可以为3:1或者2:1

## 发布版本

代码写好并经过调试后，就可以执行gradle指令构建发布db-elasticsearch-tool运行包，需要安装最新版本的gradle并配置好gradle环境变量。

gradle安装和配置参考文档：https://esdoc.bbossgroups.com/#/bboss-build

我们可以在cmd行，idea、eclipse中运行打包指令，以工程目录cmd窗口为例：

```gradle
先切换到工程的根目录
cd D:\workspace\bbossesdemo\db2es-booter
gradle clean releaseVersion
```

构建成功后，将会在工程目录下面生成可部署的二进制包：

build/distributions/db2es-booter-1.0.0-released.zip

包的目录结构如下：

![img](https://esdoc.bbossgroups.com/_images/db-es-dist.png)

运行里面的即可启动作业：

windows: restart.bat 

linux: restart.sh

# 7 作业参数配置

在使用[db2es-booter](https://github.com/bbossgroups/db-elasticsearch-tool)时，为了避免调试过程中不断打包发布数据同步工具，可以将部分控制参数配置到启动配置文件resources/application.properties

中,然后在代码中通过以下方法获取配置的参数：

```
#工具主程序
mainclass=org.frameworkset.elasticsearch.imp.Dbdemo

# 参数配置
# 在代码中获取方法：CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值false
dropIndice=false
```

在代码中获取参数dropIndice方法：

```
boolean dropIndice = CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值false
```

另外可以在resources/application.properties配置控制作业执行的一些参数，例如工作线程数，等待队列数，批处理size等等：

```
queueSize=50
workThreads=10
batchSize=20
```

在作业执行方法中获取并使用上述参数：

```
int batchSize = CommonLauncher.getIntProperty("batchSize",10);//同时指定了默认值
int queueSize = CommonLauncher.getIntProperty("queueSize",50);//同时指定了默认值
int workThreads = CommonLauncher.getIntProperty("workThreads",10);//同时指定了默认值
importBuilder.setBatchSize(batchSize);
importBuilder.setQueue(queueSize);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(workThreads);//设置批量导入线程池工作线程数量
```



# 8 开发交流

完整的数据导入demo工程

github：[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)

码云：<https://gitee.com/bboss/db2es-booter>



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>

<img src="images/alipay.png"  height="200" width="200">

