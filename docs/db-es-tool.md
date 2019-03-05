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

支持的Elasticsearch版本： 1.x,2.x,5.x,6.x,+

支持海量PB级数据同步导入功能

**支持设置数据bulk导入任务结果处理回调函数，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务通过error方法进行相应处理**

下面详细介绍本案例。



# 1.案例对应的源码

批量导入：<https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/db2es/TestDB2ESImport.java>

定时增量导入：<https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/db2es/ScheduleImportTaskTest.java>



# 2.在工程中导入jdbc es maven坐标

```
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
<version>5.5.3</version>
</dependency>
```

本文从mysql数据库表td_cms_document导入数据到es中，除了导入上述maven坐标，还需要额外导入mysql驱动坐标：

```
<dependency>
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
<version>5.1.40</version>
</dependency>
```



# 3.配置es地址

新建application.properties文件，内容为：

```
elasticsearch.rest.hostNames=10.21.20.168:9200
## 集群地址用逗号分隔
#elasticsearch.rest.hostNames=10.180.211.27:9200,10.180.211.28:9200,10.180.211.29:9200
```



# 4.编写简单的导入代码



## **4.1同步批量导入**

```
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



## **4.2 异步批量导入**

```
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



## 4.3 一个有字段属性映射的稍微复杂案例实现

```
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



## 4.4 指定自定义文档id机制：

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



## 4.5 定时增量导入

源码文件 <https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/db2es/ScheduleImportTaskTest.java>

```
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

```
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



# 4.6 定时全量导入

```
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



# 4.7 设置任务执行结果回调处理函数

我们通过importBuilder的setExportResultHandler方法设置任务执行结果回调处理函数，函数实现接口即可：

org.frameworkset.elasticsearch.client.ExportResultHandler

```
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



# 5.作业参数配置

在使用[db2es-booter](https://gitee.com/bboss/db2es-booter)时，为了避免调试过程中不断打包发布数据同步工具，可以将部分控制参数配置到启动配置文件resources/application.properties

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



# 6 开发交流

完整的数据导入demo工程

<https://gitee.com/bboss/db2es-booter>

**elasticsearch技术交流：166471282**

**elasticsearch技术交流：**

**![img](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)**