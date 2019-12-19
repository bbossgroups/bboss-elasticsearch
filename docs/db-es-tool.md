# 数据导入Elasticsearch案例分享

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

*The best elasticsearch highlevel java rest api-----[bboss](https://esdoc.bbossgroups.com/#/README)* 

bboss数据同步可以方便地实现多种数据源之间的数据同步功能，也可以从kafka1x和kafka2x系列版本消费数据写入elasticsearch，后续还会支持更多的数据源之间的数据同步，本文介绍各种数据同步案例（支持各种数据库和各种es版本）

![](images\datasyn.png)

通过bboss，可以非常方便地实现：

1. 将数据库表数据同步到Elasticsearch
2. 将数据库表数据同步到数据库表
3. 将Elasticsearch数据同步到数据库表
4. 将Elasticsearch数据同步到Elasticsearch
5. 将mongodb数据同步到Elasticsearch
6. 将mongodb数据同步到数据库表
7. kafka数据导入elasticsearch，支持kafka_2.12-0.10.2.0系列版本和kafka_2.12-2.3.0 系列版本

导入的方式支持

- 支持逐条数据导入
- 批量数据导入
- 批量数据多线程并行导入
- 定时全量（串行/并行）数据导入
- 定时增量（串行/并行）数据导入

支持的数据库： mysql,maridb，postgress,oracle ,sqlserver,db2,tidb,hive，mongodb等

支持的Elasticsearch版本： 1.x,2.x,5.x,6.x,7.x,+

支持海量PB级数据同步导入功能

支持将ip转换为对应的运营商和城市地理坐标位置信息

**支持设置数据bulk导入任务结果处理回调函数，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务通过error和exception方法进行相应处理**

支持多种定时任务执行引擎：

- jdk timer （内置）
- quartz
- xxl-job分布式调度引擎，基于分片调度机制实现海量数据快速同步能力

bboss另一个显著的特色就是直接基于java语言来编写数据同步作业程序，基于强大的java语言和第三方工具包，能够非常方便地加工和处理需要同步的源数据，然后将最终的数据保存到目标库（Elasticsearch或者数据库）；同时也可以非常方便地在idea或者eclipse中调试和运行同步作业程序，调试无误后，通过bboss提供的gradle脚本，即可构建和发布出可部署到生产环境的同步作业包。因此，对广大的java程序员来说，bboss无疑是一个轻易快速上手的数据同步利器。

​	下面我们通过案例来介绍mongodb-elasticsearch的使用方法，你会发现整个过程下来，开发一个同步作业，其实就是在用大家熟悉的方式做一个简单的java开发编程的事情。

下面详细介绍本案例。



# 1.准备工作

## 1.1 在工程中导入jdbc es maven坐标

```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
<version>5.9.8</version>
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
mysql 5.x驱动依赖包
```xml
<dependency>
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
<version>5.1.40</version>
</dependency>
```
mysql 8.x驱动依赖包(mysql 8必须采用相应版本的驱动，否则不能正确运行)
```xml
<dependency>
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
<version>8.0.16</version>
</dependency>
```
# 2.数据库表数据导入到Elasticsearch

## 2.1.案例对应的源码

批量导入：https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java

定时增量导入：https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java

## 2.2.索引表结构定义

Elasticsearch会在我们导入数据的情况下自动创建索引mapping结构，如果对mapping结构有特定需求或者自动创建的结构不能满足要求，可以自定义索引mapping结构，在导入数据之前创建好自定义的mapping结构或者mapping模板即可，具体定义和创建方法参考文档： [Elasticsearch索引表和索引表模板管理](index-indextemplate.md) 

## 2.2.配置es地址

新建application.properties文件，内容为：

```
elasticsearch.rest.hostNames=10.21.20.168:9200
## 集群地址用逗号分隔
#elasticsearch.rest.hostNames=10.180.211.27:9200,10.180.211.28:9200,10.180.211.29:9200
```



## 2.3.编写简单的导入代码

批量导入关键配置：

importBuilder.setBatchSize(5000)

mysql提供两种处理机制支持海量数据的导入，一种机制是在application.properties文件配置连接串和指定fetch相关的useCursorFetch和jdbcFetchSize参数：

```properties
db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false

##mysql 8 url配置样例
#db.url = jdbc:mysql://192.168.0.188:3308/braineex?useCursorFetch=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true
db.jdbcFetchSize = 10000

```

另外一种机制可以参考文档章节：

[2.3.14 Mysql ResultSet Stream机制说明](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2314-mysql-resultset-stream%e6%9c%ba%e5%88%b6%e8%af%b4%e6%98%8e)

### 2.3.1同步批量导入

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
				.setIndexType("dbclobdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
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



### **2.3.2 异步批量导入**

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
				.setIndexType("dbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
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
		//获取索引表dbdemo中的数据总量，如果没有设置refreshOption,es插入数据会有几秒的延迟（具体的延迟多久取决于index refresh interval配置），所以countAll统计出来的结果不一定准确
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

### 2.3.3 一个有字段属性映射的稍微复杂案例实现

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
				.setIndexType("dbclobdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
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



### 2.3.4 设置文档id机制

bboss充分利用elasticsearch的文档id生成机制，同步数据的时候提供了以下3种生成文档Id的机制：

1. 不指定文档ID机制：直接使用Elasticsearch自动生成文档ID

2. 指定表字段，对应的字段值作为Elasticsearch文档ID

   importBuilder.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id

3. 自定义文档id机制配置

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



### 2.3.5 定时增量导入

定时机制配置

```java
//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束
```

支持按照数字字段和时间字段进行增量导入，增量导入sql的语法格式：

```sql
select * from td_sm_log where log_id > #[log_id]
```

通过#[xxx],指定变量，变量可以在sql中出现多次：

```sql
select * from td_sm_log where log_id > #[log_id] and other_id = #[log_id]
```

数字类型增量导入配置：

```java
importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型

importBuilder.setNumberLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
```

日期类型增量导入配置

```java
importBuilder.setDateLastValueColumn("log_id");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段

importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.TIMESTAMP_TYPE数字类型
```

详细的增量导入案例：

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
				.setIndexType("dbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(5000)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setJdbcFetchSize(10000);//设置数据库的查询fetchsize，同时在mysql url上设置useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，jdbcFetchSize必须和useCursorFetch参数配合使用，否则不会生效  
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				     .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
					 .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
//		importBuilder.setNumberLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
//		importBuilder.setDateLastValueColumn("log_id");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
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

		

		
	}
```



### 2.3.6 定时全量导入

定时机制配置

```java
//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束
```

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
				.setIndexType("dbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
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

		

		
	}
```

### 2.3.7 定时任务指定执行拦截器使用

可以为同步定时任务指定执行拦截器，示例如下：

```java
        //设置任务执行拦截器，可以添加多个
		importBuilder.addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				System.out.println("preCall");
                //可以在这里做一些重置初始化操作，比如删mapping之类的
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



### 2.3.8 定时任务调度说明

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

指定定时timer

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

### 2.3.9 增量导入注意事项

#### 2.3.9.1 排序设置

bboss 5.8.9及之前的版本需要注意：如果增量字段默认自带排序功能（比如采用主键id作为增量字段），则sql语句不需要显式对查询的数据进行排序，否则需要在sql语句中显式基于增量字段升序排序：

```java
importBuilder.setSql("select * from td_sm_log where update_date > #[log_id] order by update_date asc");
```

bboss 5.9.0及后续的版本已经内置了对增量字段值的排序功能，所以在sql或者dsl中不需要额外进行排序设置，可以提升导入性能。

#### 2.3.9.2 增量状态存储数据库

采用分布式作业调度引擎时，定时增量导入需要指定增量状态存储数据库：[保存增量状态的数据源配置](https://esdoc.bbossgroups.com/#/db-es-tool?id=_246-%e4%bf%9d%e5%ad%98%e5%a2%9e%e9%87%8f%e7%8a%b6%e6%80%81%e7%9a%84%e6%95%b0%e6%8d%ae%e6%ba%90%e9%85%8d%e7%bd%ae)

#### 2.3.9.3 设置增量同步增量字段起始值

可以指定增量字段的起始值，不指定的情况下数字默认起始值0,日期默认起始值:1970-01-01

指定日期字段增量同步起始值：

```java
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date);//增量起始值配置
		}
		catch (Exception e){
			e.printStackTrace();
		}
```

指定数字字段增量同步起始值：
```java
		 
		try {
			 
			importBuilder.setLastValue(100);//增量起始值配置
		}
		catch (Exception e){
			e.printStackTrace();
		}
```

### 2.3.10 灵活控制文档数据结构

可以通过importBuilder全局扩展添加字段到es索引中：

```java
        importBuilder.addFieldValue("testF1","f1value");
		importBuilder.addFieldValue("testInt",0);
		importBuilder.addFieldValue("testDate",new Date());
		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
        importBuilder.addIgnoreFieldMapping("subtitle");//全局忽略字段
        importBuilder.addFieldMapping("dbcolumn","esFieldColumn");//全局添加字段名称映射
```

如果需要针对单条记录，bboss提供org.frameworkset.tran.DataRefactor接口和Context接口像结合来提供对数据记录的自定义处理功能，这样就可以灵活控制文档数据结构，通过context可以对当前记录做以下调整：

1.添加字段

2.修改字段名称映射关系

3.修改字段值

4.移除记录

5.获取ip地理位置信息等

举例说明如下：

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

***注意：***

***1.内嵌的数据库查询会有性能损耗，在保证性能的前提下，尽量将内嵌的sql合并的外部查询数据的整体的sql中，或者采用缓存技术消除内部sql查询。***

**2.一定要注意全局级和记录级调整区别：在DataRefactor接口中只能用Context来调整数据字段映射和字段添加修改和移除操作**

![](images\datarefactor.png)

### 2.3.11 IP转换为地理坐标城市运营商信息

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

首先从以下地址下载ip信息库：

 https://dev.maxmind.com/geoip/geoip2/geolite2/#Downloads 

然后在application.properties文件中配置对应的ip信息库文件地址

```properties
ip.cachesize = 2000
# geoip的ip地址信息库下载地址https://dev.maxmind.com/geoip/geoip2/geolite2/
ip.database = E:/workspace/hnai/terminal/geolite2/GeoLite2-City.mmdb
ip.asnDatabase = E:/workspace/hnai/terminal/geolite2/GeoLite2-ASN.mmdb
```

### 2.3.12 设置任务执行结果回调处理函数

我们通过importBuilder的setExportResultHandler方法设置任务执行结果以及异常回调处理函数，函数实现接口即可：

org.frameworkset.tran.ExportResultHandler

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

### 2.3.13 灵活指定索引名称和索引类型

```java
importBuilder
				.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //elasticsearch7之前必填项，之后的版本不需要指定
```

index和type可以有以下几种动态生成方法：

```properties
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

示例如下：

```java
importBuilder
				.setIndex("demo-{dateformat=yyyy.MM.dd}") //必填项
				.setIndexType("dbclobdemo") //elasticsearch7之前必填项，之后的版本不需要指定
```

```java
importBuilder
				.setIndex("demo-{agentStarttime,yyyy.MM.dd}") //必填项
				.setIndexType("dbclobdemo") //elasticsearch7之前必填项，之后的版本不需要指定
```



### 2.3.14 Mysql ResultSet Stream机制说明

同步Mysql 大数据表到Elasticsearch时，针对jdbc fetchsize（ResultSet Stream）的使用比较特殊，mysql提供了两种机制来处理：

**机制一** mysql 5以后的版本采用jdbc url串参数useCursorFetch=true以及配置fetchsize属性来实现，bboss在application.properties中做如下配置即可：

```properties
db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
db.jdbcFetchSize = 10000
```

**机制二**  配置jdbcFetchSize为最小整数来采用mysql的默认实现机制，db url中不要带useCursorFetch参数（适用mysql各版本）

```properties
# 注意：url中不要带useCursorFetch参数
db.url = jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false
# Integer.MIN_VALUE
db.jdbcFetchSize = -2147483648
```

在代码中使用机制二：

```java
        //数据源相关配置，可选项，可以在外部启动数据源
        importBuilder.setDbName("test")
                .setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
                .setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false")//没有带useCursorFetch=true参数，jdbcFetchSize参数配置为-2147483648，否则不会生效  
                 .setJdbcFetchSize(-2147483648);
                .setDbUser("root")
                .setDbPassword("123456")
                .setValidateSQL("select 1")
                .setUsePool(true);//是否使用连接池
```

机制二需要bboss elasticsearch [5.7.2](https://esdoc.bbossgroups.com/#/changelog?id=v572-%E5%8A%9F%E8%83%BD%E6%94%B9%E8%BF%9B)以后的版本才支持。

### 2.3.15 用配置文件来管理同步sql

如果同步的sql很长，那么可以在配置文件中管理同步的sql

**首先定义一个xml sql配置文件**

在工程resources目录下创建一个名称为sql.xml的配置文件（路径可以自己设定，如果有子目录，那么在setSqlFilepath方法中带上相对路径即可），内容如下：

```xml
<?xml version="1.0" encoding='UTF-8'?>
<properties>
    <description>
        <![CDATA[
   配置数据导入的sql
 ]]>
    </description>
    <!--增量导入sql-->
    <!--<property name="demoexport"><![CDATA[select * from td_sm_log where log_id > #[log_id]]]></property>-->
    <!--全量导入sql-->
    <property name="demoexportFull"><![CDATA[select * from td_sm_log ]]></property>

</properties>
```

**然后，利用api指定配置文件相对classpath路径和对应sql配置名称即可**

```java
importBuilder.setSqlFilepath("sql.xml")
           .setSqlName("demoexportFull");
```

### 2.3.16 设置ES数据导入控制参数

可以通过以下方法设置数据导入Elasticsearch的各种控制参数，例如routing,esid,parentid,refresh策略，版本信息等等：

```java
importBuilder.setEsIdField("documentId")//可选项，es自动为文档产生id
				.setEsParentIdField("documentParentid") //可选项,如果不指定，文档父子关系父id对应的字段
				.setRoutingField("routingId") //可选项		importBuilder.setRoutingValue("1");
				.setEsDocAsUpsert(true)//可选项
				.setEsRetryOnConflict(3)//可选项
				.setEsReturnSource(false)//可选项
				.setEsVersionField(“versionNo”)//可选项
				.setEsVersionType("internal")//可选项
                .setRefreshOption("refresh=true&version=1");//可选项，通过RefreshOption可以通过url参数的方式任意组合各种控制参数
```

参考文档：

 [基于refreshoption参数指定添加修改文档控制参数](https://esdoc.bbossgroups.com/#/development?id=_481-基于refreshoption参数指定添加修改文档控制参数) 

Elasticsearch控制参数参考文档：

 https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html 

### 2.3.17 数据同步任务执行统计信息获取

通过数据同步任务执行结果回调处理函数，可以获取到每个任务的详细执行统计信息：

```java
importBuilder.setExportResultHandler(new ExportResultHandler() {
			@Override
			public void success(TaskCommand<String,String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
			}

			@Override
			public void error(TaskCommand<String,String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
			}

			@Override
			public void exception(TaskCommand<String,String> taskCommand, Exception exception) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
			}

			@Override
			public int getMaxRetry() {
				return 0;
			}
		});

```

输出的结果如下：

```json
{
    "jobStartTime": 1572920403541, //作业开始时间，date类型，输出json时被转换为long值
    "taskStartTime": 1572920403571,//当前任务开始时间，date类型，输出json时被转换为long值
    "taskEndTime": 1572920403585,//当前任务结束时间，date类型，输出json时被转换为long值
    "totalRecords": 4, //作业处理总记录数
    "totalFailedRecords": 0,//作业处理总失败记录数
    "totalIgnoreRecords": 0,//作业处理总忽略记录数
    "totalSuccessRecords": 4,//作业处理总成功记录数
    "successRecords": 2,//当前任务处理总成功记录数
    "failedRecords": 0,//当前任务处理总失败记录数
    "ignoreRecords": 0,//当前任务处理总忽略记录数    
    "taskNo": 3,//当前任务编号
    "jobNo": "eece3d34320b490a980d3f501cb7ae8c" //任务对应的作业编号，一个作业会被拆分为多个任务执行
}
```

### 2.3.18 设置并行导入参数

代码里面加上下面参数，可以并行导入，导入速度会更快
```java
importBuilder.setParallel(true);//设置为多线程并行批量导入
importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
```

## 2.4.DB-ES数据同步工具使用方法

上面介绍了数据库数据同步到数据库的各种用法，bboss还提供了一个样板demo工程:[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)，用来将写好的同步代码打包发布成可以运行的二进制包上传到服务器运行，[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)提供了现成的运行指令和jvm配置文件。

### 2.4.1 环境准备

首先需要从Github下载最新的工具源码：

https://github.com/bbossgroups/db-elasticsearch-tool

[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)是一个gradle工程，因此需要安装最新版本的gradle并配置好gradle环境变量，gradle安装和配置参考文档：

https://esdoc.bbossgroups.com/#/bboss-build

安装和配置好gradle，就可以将db-elasticsearch-tool工程导入idea或者eclipse，然后进行数据同步逻辑的开发、调试以及构建打包工作。

### 2.4.2 同步代码主程序定义-Dbdemo

Dbdemo-提供了上述文中提供的各种导入数据的方法，可以根据自己的要求实效自己的方法逻辑，然后在Dbdemo的main方法中指定要执行的方法即可：

```java
public static void main(String args[]){
   Dbdemo dbdemo = new Dbdemo();
   boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值
   dbdemo.scheduleFullImportData(  dropIndice);
}
```

dbdemo类部分内容：

```java
package org.frameworkset.elasticsearch.imp;


import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.tran.*;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.ImportIncreamentConfig;
import org.frameworkset.tran.schedule.TaskContext;
import org.frameworkset.runtime.CommonLauncher;
import org.frameworkset.spi.geoip.IpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Description: 同步处理程序，如需调试同步功能，
 * 请运行测试用例DbdemoTest中调试</p>

 */
public class Dbdemo {
   private static Logger logger = LoggerFactory.getLogger(Dbdemo.class);
   public static void main(String args[]){
      Dbdemo dbdemo = new Dbdemo();
      boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值
      dbdemo.scheduleFullImportData(  dropIndice);
   }

   
   /**
    * elasticsearch地址和数据库地址都从外部配置文件application.properties中获取，加载数据源配置和es配置
    */
   public void scheduleFullImportData(boolean dropIndice){
      DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
      dropIndice = CommonLauncher.getBooleanAttribute("dropIndice",true);//同时指定了默认值
      int batchSize = CommonLauncher.getIntProperty("batchSize",10);//同时指定了默认值
      int queueSize = CommonLauncher.getIntProperty("queueSize",50);//同时指定了默认值
      int workThreads = CommonLauncher.getIntProperty("workThreads",10);//同时指定了默认值
      //增量定时任务不要删表，但是可以通过删表来做初始化操作
      if(dropIndice) {
         try {
            //清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
            String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("dbdemo");
            System.out.println(repsonse);
         } catch (Exception e) {
         }
      }
      //设置数据bulk导入任务结果处理回调函数，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务通过error方法进行相应处理
      importBuilder.setExportResultHandler(new ExportResultHandler<String,String>() {
         @Override
         public void success(TaskCommand<String,String> taskCommand, String result) {
            String datas = taskCommand.getDatas();//执行的批量数据
//          System.out.println(result);//打印成功结果
         }

         @Override
         public void error(TaskCommand<String,String> taskCommand, String result) {
            //任务执行完毕，但是结果中包含错误信息
            //具体怎么处理失败数据可以自行决定,下面的示例显示重新导入失败数据的逻辑：
            // 从result中分析出导入失败的记录，然后重新构建data，设置到taskCommand中，重新导入，
            // 支持的导入次数由getMaxRetry方法返回的数字决定
            // String failDatas = ...;
            //taskCommand.setDatas(failDatas);
            //taskCommand.execute();
            String datas = taskCommand.getDatas();//执行的批量数据
//          System.out.println(result);//打印成功结果
         }

         @Override
         public void exception(TaskCommand<String, String> taskCommand, Exception exception) {
            //任务执行抛出异常，失败处理方法
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

      //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
      // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
      // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
      // log_id和数据库对应的字段一致,就不需要设置setNumberLastValueColumn和setDateLastValueColumn信息，
      // 但是需要设置setLastValueType告诉工具增量字段的类型

      importBuilder.setSql("select * from td_sm_log ");

//    importBuilder.setSql("select * from td_sm_log ");
      /**
       * es相关配置
       */
      importBuilder
            .setIndex("dbdemo") //必填项
            .setIndexType("dbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//          .setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
            .setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
            .setUseLowcase(false)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
            .setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
            .setBatchSize(batchSize);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

      //定时任务配置，
      importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//              .setScheduleDate(date) //指定任务开始执行时间：日期
            .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
            .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
      //定时任务配置结束
//
//    //设置任务执行拦截器，可以添加多个
//    importBuilder.addCallInterceptor(new CallInterceptor() {
//       @Override
//       public void preCall(TaskContext taskContext) {
//          System.out.println("preCall");
//       }
//
//       @Override
//       public void afterCall(TaskContext taskContext) {
//          System.out.println("afterCall");
//       }
//
//       @Override
//       public void throwException(TaskContext taskContext, Exception e) {
//          System.out.println("throwException");
//       }
//    }).addCallInterceptor(new CallInterceptor() {
//       @Override
//       public void preCall(TaskContext taskContext) {
//          System.out.println("preCall 1");
//       }
//
//       @Override
//       public void afterCall(TaskContext taskContext) {
//          System.out.println("afterCall 1");
//       }
//
//       @Override
//       public void throwException(TaskContext taskContext, Exception e) {
//          System.out.println("throwException 1");
//       }
//    });
//    //设置任务执行拦截器结束，可以添加多个
      //增量配置开始
//    importBuilder.setNumberLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
//    importBuilder.setDateLastValueColumn("log_id");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
      importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
      importBuilder.setLastValueStorePath("logtable_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//    importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
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
      final AtomicInteger s = new AtomicInteger(0);

      /**
       * 重新设置es数据结构
       */
      importBuilder.setDataRefactor(new DataRefactor() {
         public void refactor(Context context) throws Exception  {
            //可以根据条件定义是否丢弃当前记录
            //context.setDrop(true);return;
//          if(s.incrementAndGet() % 2 == 0) {
//             context.setDrop(true);
//             return;
//          }
            //空值处理，判断字段content的值是否为空
            if(context.getValue("content") == null){
               context.addFieldValue("content","");//将content设置为""
            }
            context.addFieldValue("content","");//将content设置为""
            CustomObject customObject = new CustomObject();
            customObject.setAuthor((String)context.getValue("author"));
            customObject.setTitle((String)context.getValue("title"));
            customObject.setSubtitle((String)context.getValue("subtitle"));

            customObject.setIds(new int[]{1,2,3});
            context.addFieldValue("author",customObject);

            long testtimestamp = context.getLongValue("testtimestamp");//将long类型的时间戳转换为Date类型
            context.addFieldValue("testtimestamp",new Date(testtimestamp));//将long类型的时间戳转换为Date类型

//          context.addIgnoreFieldMapping("title");
            //上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//          context.addIgnoreFieldMapping("author");

            //修改字段名称title为新名称newTitle，并且修改字段的值
            context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
            context.addIgnoreFieldMapping("subtitle");
            /**
             * 获取ip对应的运营商和区域信息
             */
            IpInfo ipInfo = context.getIpInfo("remoteAddr");
            context.addFieldValue("ipInfo",ipInfo);
            context.addFieldValue("collectTime",new Date());
            /**
            //关联查询数据,单值查询
            Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
                                                "select * from head where billid = ? and othercondition= ?",
                                                context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
            //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("headdata",headdata);
            //关联查询数据,多值查询
            List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
                  "select * from facedata where billid = ?",
                  context.getIntegerValue("billid"));
            //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("facedatas",facedatas);
             */
         }
      });
      //映射和转换配置结束

      /**
       * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
       */
      importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
      importBuilder.setQueue(queueSize);//设置批量导入线程池等待队列长度
      importBuilder.setThreadCount(workThreads);//设置批量导入线程池工作线程数量
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
      DataStream dataStream = importBuilder.builder();
      dataStream.execute();//执行导入操作

      System.out.println();


   }

  
}
```

Dbdemo完整的内容参考：

<https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java>

Dbdemo对应功能测试和debug类DbdemoTest（开发测试调试的时候跑这个）：

<https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/test/java/org/frameworkset/elasticsearch/imp/DbdemoTest.java>

### 2.4.3 es数据源配置

修改配置文件src\test\resources\application.properties

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

### 2.4.4 数据库数据源配置

修改配置文件src\test\resources\application.properties，以mysql未来介绍数据源配置：

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

### 2.4.5 自定义适配器数据源配置

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

在配置文件src\test\resources\application.properties中指定自定义数据源适配器

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

### 2.4.6 保存增量状态的数据源配置

采用分布式作业调度引擎时，定时增量导入需要指定保存增量状态的数据源：

修改配置文件src\test\resources\application.properties

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

### 2.4.7 测试以及调试同步代码

在Dbdemo类添加main方法，在其中添加Dbdemo执行代码，即可运行调试同步代码：

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





### 2.4.8 查看任务执行详细日志

如果要查看任务执行过程中的详细日志，只需设置以下参数即可：

```java
importBuilder.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
```

这样在任务执行的时候会打印如下日志信息：

```
15:47:45.704 [DB2ESImportThread-1] DEBUG org.frameworkset.tran.TaskCall - Task[39] finish,import 10 records,Total import 390 records,Take time:432ms
15:47:45.704 [DB2ESImportThread-1] INFO  org.frameworkset.tran.TaskCall - Task[41] starting ......
15:47:45.704 [DB2ESImportThread-2] INFO  org.frameworkset.tran.TaskCall - Task[40] starting ......
15:47:46.238 [DB2ESImportThread-1] DEBUG org.frameworkset.tran.TaskCall - Task[41] finish,import 10 records,Total import 420 records,Take time:534ms
15:47:46.238 [DB2ESImportThread-2] DEBUG org.frameworkset.tran.TaskCall - Task[40] finish,import 10 records,Total import 410 records,Take time:534ms
15:47:46.238 [DB2ESImportThread-1] INFO  org.frameworkset.tran.TaskCall - Task[42] starting ......
15:47:46.530 [DB2ESImportThread-1] DEBUG org.frameworkset.tran.TaskCall - Task[42] finish,import 8 records,Total import 428 records,Take time:292ms
15:47:46.530 [main] INFO  org.frameworkset.tran.JDBCRestClientUtil - Complete tasks:43,Total import 428 records.
```



### 2.4.9 数据导入不完整原因分析及处理

如果在任务执行完毕后，发现es中的数据与数据库源表的数据不匹配，可能的原因如下：

**1.并行执行的过程中存在失败的任务（比如服务端超时），这种情况通过setExportResultHandler设置的exception监听方法进行定位分析**

参考章节【[5.12 设置任务执行结果回调处理函数](https://esdoc.bbossgroups.com/#/db-es-tool?id=_512-设置任务执行结果回调处理函数)】

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

c) 对于read或者等待超时的异常，亦可以调整配置文件src\test\resources\application.properties中的http timeout时间参数

http.timeoutConnection = 50000

http.timeoutSocket = 50000



**2.任务执行完毕，但是存在es的bulk拒绝记录或者数据内容不合规的情况，这种情况就通过setExportResultHandler设置的error监听方法进行定位分析**

参考章节【[5.12 设置任务执行结果回调处理函数](https://esdoc.bbossgroups.com/#/db-es-tool?id=_512-设置任务执行结果回调处理函数)】

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

### 2.4.9 作业运行jvm内存配置

修改jvm.options,设置作业运行需要的jvm内存，按照比例调整Xmx和MaxNewSize参数：

```properties
-Xms1g
-Xmx1g
-XX:NewSize=512m
-XX:MaxNewSize=512m
```

Xms和Xmx保持一样，NewSize和MaxNewSize保持一样，Xmx和MaxNewSize大小保持的比例可以为3:1或者2:1

### 2.4.10 发布版本

代码写好并经过调试后，就可以执行gradle指令构建发布db-elasticsearch-tool运行包，需要安装最新版本的gradle并配置好gradle环境变量。

gradle安装和配置参考文档：https://esdoc.bbossgroups.com/#/bboss-build

我们可以在cmd行，idea、eclipse中运行打包指令，以工程目录cmd窗口为例：

```gradle
先切换到工程的根目录
cd D:\workspace\bbossesdemo\db-elasticsearch-tool
release.bat
```

构建成功后，将会在工程目录下面生成可部署的二进制包：

build/distributions/db-elasticsearch-tool-1.0.0-released.zip

包的目录结构如下：

![img](_images/db-es-dist.png)

运行里面的即可启动作业：

windows: restart.bat 

linux: restart.sh

## 2.5 作业参数配置

在使用[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)时，为了避免调试过程中不断打包发布数据同步工具，可以将需要调整的参数配置到启动配置文件src\test\resources\application.properties中,然后在代码中通过以下方法获取配置的参数：



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

另外可以在src\test\resources\application.properties配置控制作业执行的一些参数，例如工作线程数，等待队列数，批处理size等等：

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

**注意：这些参数只有在正式发布后，用shell脚本启动作业才会从配置文件中读取并生效，所以需要指定默认值，在开发调试的时候采用参数默认值来运行作业。**

## 2.6 基于xxjob 同步DB-Elasticsearch数据

bboss结合xxjob分布式定时任务调度引擎，可以非常方便地实现强大的shard分片分布式同步数据库数据到Elasticsearch功能，比如从一个10亿的数据表中同步数据，拆分为10个任务分片节点执行，每个节点同步1个亿，速度会提升10倍左右；同时提供了同步作业的故障迁移容灾能力。

### 2.6.1 首先定义一个xxjob的同步作业

```java
package org.frameworkset.elasticsearch.imp.jobhandler;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.xxl.job.core.util.ShardingUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.tran.DB2ESImportBuilder;
import org.frameworkset.tran.schedule.ExternalScheduler;
import org.frameworkset.tran.schedule.ImportIncreamentConfig;
import org.frameworkset.tran.schedule.xxjob.AbstractDB2ESXXJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: 使用xxl-job,quartz等外部定时任务调度引擎导入数据，需要设置：</p>
 * importBuilder.setExternalTimer(true);
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/4/13 13:45
 * @author biaoping.yin
 * @version 1.0
 */
public class XXJobImportTask extends AbstractDB2ESXXJobHandler {
	private static Logger logger = LoggerFactory.getLogger(XXJobImportTask.class);
	public void init(){
		// 可参考Sample示例执行器中的示例任务"ShardingJobHandler"了解试用

		externalScheduler = new ExternalScheduler();
		externalScheduler.dataStream((Object params)->{
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
//		if(dropIndice) {
			try {
				//清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
				String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("quartz");
				System.out.println(repsonse);
			} catch (Exception e) {
			}
//		}


			//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
			// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
			// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
			// log_id和数据库对应的字段一致,就不需要设置setNumberLastValueColumn和setNumberLastValueColumn信息，
			// 但是需要设置setLastValueType告诉工具增量字段的类型

			importBuilder.setSql("select * from td_sm_log where log_id > #[log_id]");
			importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
			/**
			 * es相关配置
			 */
			importBuilder
					.setIndex("quartz") //必填项
					.setIndexType("quartz") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
					.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
					.setUseLowcase(false)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
					.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
					.setBatchSize(10);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

			//定时任务配置，
			//采用内部定时任务
//		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
////					 .setScheduleDate(date) //指定任务开始执行时间：日期
//				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
//				.setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
			//采用外部定时任务
			importBuilder.setExternalTimer(true);
			//定时任务配置结束
//
//		//设置任务执行拦截器，可以添加多个
//		importBuilder.addCallInterceptor(new CallInterceptor() {
//			@Override
//			public void preCall(TaskContext taskContext) {
//				System.out.println("preCall");
//			}
//
//			@Override
//			public void afterCall(TaskContext taskContext) {
//				System.out.println("afterCall");
//			}
//
//			@Override
//			public void throwException(TaskContext taskContext, Exception e) {
//				System.out.println("throwException");
//			}
//		}).addCallInterceptor(new CallInterceptor() {
//			@Override
//			public void preCall(TaskContext taskContext) {
//				System.out.println("preCall 1");
//			}
//
//			@Override
//			public void afterCall(TaskContext taskContext) {
//				System.out.println("afterCall 1");
//			}
//
//			@Override
//			public void throwException(TaskContext taskContext, Exception e) {
//				System.out.println("throwException 1");
//			}
//		});
//		//设置任务执行拦截器结束，可以添加多个
			//增量配置开始
//		importBuilder.setNumberLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
			importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
			importBuilder.setLastValueStorePath("logtable_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
			importBuilder.setLastValueStoreTableName("logs"+index);//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab，增量状态表会自动创建。如果xxl-job是shard分片模式运行，
																   // 需要独立的表来记录每个分片增量同步状态，
																   // 并且采用xxl-job等分布式任务调度引擎时，同步状态表必须存放于db.config=test指定的数据源，不能采用本地sqlite数据库
			importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
			// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
			//增量配置结束

			//映射和转换配置开始
//		/**
//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
//		 */
//		importBuilder.addFieldMapping("document_id","docId")
//				.addFieldMapping("docwtime","docwTime")
//				.addIgnoreFieldMapping("channel_id");//添加忽略字段
//
//
//		/**
//		 * 为每条记录添加额外的字段和值
//		 * 可以为基本数据类型，也可以是复杂的对象
//		 */
//		importBuilder.addFieldValue("testF1","f1value");
//		importBuilder.addFieldValue("testInt",0);
//		importBuilder.addFieldValue("testDate",new Date());
//		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
//		TestObject testObject = new TestObject();
//		testObject.setId("testid");
//		testObject.setName("jackson");
//		importBuilder.addFieldValue("testObject",testObject);
//
//		/**
//		 * 重新设置es数据结构
//		 */
//		importBuilder.setDataRefactor(new DataRefactor() {
//			public void refactor(Context context) throws Exception  {
//				CustomObject customObject = new CustomObject();
//				customObject.setAuthor((String)context.getValue("author"));
//				customObject.setTitle((String)context.getValue("title"));
//				customObject.setSubtitle((String)context.getValue("subtitle"));
//				customObject.setIds(new int[]{1,2,3});
//				context.addFieldValue("docInfo",customObject);//如果还需要构建更多的内部对象，可以继续构建
//
//				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//				context.addIgnoreFieldMapping("author");
//				context.addIgnoreFieldMapping("title");
//				context.addIgnoreFieldMapping("subtitle");
//			}
//		});
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
//		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在调试需要的时候才打开，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度

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

### 2.6.2 作业注册配置

然后将作业配置到application.propperties中：

```properties
##
# 作业任务配置
# xxl.job.task为前置配置多个数据同步任务，后缀XXJobImportTask和OtherTask将xxjob执行任务的名称
# 作业程序都需要继承抽象类org.frameworkset.tran.schedule.xxjob.AbstractDB2ESXXJobHandler
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

### 2.6.3  任务构建和运行

1. 下载完整的demo
   https://github.com/bbossgroups/db-elasticsearch-xxjob

2. 安装和配置gradle

   https://esdoc.bbossgroups.com/#/bboss-build

3. 在工程db-elasticsearch-xxjob根目录下运行gradle构建指令

   release.bat

4. 启动作业

构建成功后，将会在工程目录下面生成可部署的二进制包：

build/distributions/db-elasticsearch-xxjob-released.zip

解压运行里面的指令，即可启动作业：

windows: restart.bat 

linux: restart.sh

### 2.6.4  xxjob运行效果

任务启动后，可以在xxjob的挂你控制台看到刚注册的执行器和作业：

注册好的作业执行器

![作业执行器](images/xxjob-executor.png)

编辑执行器

![编辑执行器](images/xxjobexe-editor.png)

任务管理

![任务管理](images/joblist.png)

可以在后面的操作去，执行、启动作业，查看作业调度执行日志，修改作业参数和运行模式：

![](images/jobedit.png)

运行报表

![运行报表](images/jobstatic.png)

# 3 Elasticsearch-db数据同步使用方法

完整的示例工程：

https://github.com/bbossgroups/db-elasticsearch-tool

工程基于gradle管理，可以参考文档配置gradle环境：

https://esdoc.bbossgroups.com/#/bboss-build

## 3.1 同步参数设置

Elasticsearch-db数据同步使用方法和DB-Elasticsearch同步的使用方法类似，支持全量、增量定时同步功能， 内置jdk timer同步器，支持quartz、xxl-job任务调度引擎 ，这里就不具体举例说明，大家可以下载demo研究即可，Elasticsearch-db数据同步基本和DB-Elasticsearch同步的参数配置差不多，这里介绍一下Elasticsearch-DB同步特有的参数：

```java
                importBuilder.setBatchSize(2) //批量写入数据库的数据量
                             .setFetchSize(10); //按批从elasticsearch拉取数据的大小
 
                importBuilder.setDsl2ndSqlFile("dsl2ndSqlFile.xml")//配置从Elasticsearch检索数据的DSl语句和往数据库插入数据的insert sql语句
				.setDslName("scrollSliceQuery")//指定配置文件中dsl的名称
				.setScrollLiveTime("10m")//指定scroll上下文的有效时间
				.setSliceQuery(true) //指定是否是slicescroll查询
				.setSliceSize(5) //指定slice scroll查询的slice数量
				.setSqlName("insertSQLnew") //指定数据库插入数据的insert sql语句
				.setQueryUrl("dbdemo/_search") //设置需要检索的索引表和对应的操作
//				//配置dsl中需要用到的参数及参数值
				.addParam("var1","v1")
				.addParam("var2","v2")
				.addParam("var3","v3");
```

dsl2ndSqlFile.xml放置到工程resources目录下即可，示例内容如下：

```xml
<?xml version="1.0" encoding='UTF-8'?>
<properties>
    <description>
        <![CDATA[
            配置数据导入的dsl和sql
         ]]>
    </description>
    <!--
          条件片段
     -->
    <property name="queryCondition">
        <![CDATA[
         "query": {
                "bool": {
                    "filter": [
                        ## 可以设置同步数据的过滤参数条件
                        #*
                        {
                            "term": {
                                "var1.keyword": #[var1]
                            }
                        },
                        {
                            "term": {
                                "var2.keyword": #[var2]
                            }
                        },
                        {
                            "term": {
                                "var3.keyword": #[var3]
                            }
                        },
                        *#
                        ## 根据fullImport参数控制是否设置增量检索条件，true 全量检索 false增量检索
                        #if(!$fullImport)
                        {   ## 增量检索范围，可以是时间范围，也可以是数字范围，这里采用的是数字增量字段
                            "range": {
                                #if($logId)
                                "logId": {
                                    "gt": #[logId] ## 数字增量检索字段
                                }
                                #end
                                #if($logOpertime)
                                "logOpertime": {
                                    "gt": #[logOpertime] ## 时间增量检索字段
                                }
                                #end
                            }
                        }
                        #end
                    ]
                }
            }
        ]]>
    </property>

    <!--
       简单的scroll query案例，复杂的条件修改queryCondition即可
       -->
    <property name="scrollQuery">
        <![CDATA[
         {
            "size":#[size],
            @{queryCondition}
        }
        ]]>
    </property>
    <!--
        简单的slice scroll query案例，复杂的条件修改queryCondition即可
    -->
    <property name="scrollSliceQuery">
        <![CDATA[
         {
           "slice": {
                "id": #[sliceId], ## 必须使用sliceId作为变量名称
                "max": #[sliceMax] ## 必须使用sliceMax作为变量名称
            },
            "size":#[size],
            @{queryCondition}
        }
        ]]>
    </property>


    <!--
    插入数据sql
    -->
    <property name="insertSQLnew">
        <![CDATA[INSERT INTO batchtest ( name, author, content, title, optime, oper, subtitle, collecttime,ipinfo)
                VALUES ( #[operModule],  ## 来源dbdemo索引中的 operModule字段
                         #[author], ## 通过datarefactor增加的字段
                         #[logContent], ## 来源dbdemo索引中的 logContent字段
                         #[title], ## 通过datarefactor增加的字段
                         #[logOpertime], ## 来源dbdemo索引中的 logOpertime字段
                         #[logOperuser],  ## 来源dbdemo索引中的 logOperuser字段
                         #[subtitle], ## 通过datarefactor增加的字段
                         #[collecttime], ## 通过datarefactor增加的字段
                         #[ipinfo]) ## 通过datarefactor增加的地理位置信息字段
]]>
    </property>
</properties>


```

### 基于时间戳增量同步-采用scroll机制

从es中查询数据导入数据库案例,基于时间戳增量同步，采用slicescroll检索

```java
public class ES2DBScrollTimestampDemo {
	public static void main(String[] args){
		ES2DBScrollTimestampDemo esDemo = new ES2DBScrollTimestampDemo();
		esDemo.scheduleScrollRefactorImportData();
		System.out.println("complete.");
	}



	public void scheduleScrollRefactorImportData(){
		ES2DBExportBuilder importBuilder = new ES2DBExportBuilder();
		importBuilder.setBatchSize(2).setFetchSize(10);


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// log_id和数据库对应的字段一致,就不需要设置setNumberLastValueColumn和setNumberLastValueColumn信息，
		// 但是需要设置setLastValueType告诉工具增量字段的类型

		/**
		 * es相关配置
		 */
		importBuilder
				.setDsl2ndSqlFile("dsl2ndSqlFile.xml")
				.setDslName("scrollQuery")
				.setScrollLiveTime("10m")
//				.setSliceQuery(true)
//				.setSliceSize(5)
				.setSqlName("insertSQLnew")
				.setQueryUrl("dbdemo/_search")

//				//添加dsl中需要用到的参数及参数值
				.addParam("var1","v1")
				.addParam("var2","v2")
				.addParam("var3","v3");

		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束

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
//		//设置任务执行拦截器结束，可以添加多个
		//增量配置开始
//		importBuilder.setNumberLastValueColumn("logId");//指定数字增量查询字段变量名称
		importBuilder.setDateLastValueColumn("logOpertime");//手动指定日期增量查询字段变量名称
		importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("es2dbdemo_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
		// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
		importBuilder.setLastValue(new Date());
		//增量配置结束

		//映射和转换配置开始
//		/**
//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
//		 */
//		importBuilder.addFieldMapping("document_id","docId")
//				.addFieldMapping("docwtime","docwTime")
//				.addIgnoreFieldMapping("channel_id");//添加忽略字段
//
//
//		/**
//		 * 为每条记录添加额外的字段和值
//		 * 可以为基本数据类型，也可以是复杂的对象
//		 */
//		importBuilder.addFieldValue("testF1","f1value");
//		importBuilder.addFieldValue("testInt",0);
//		importBuilder.addFieldValue("testDate",new Date());
//		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
//		TestObject testObject = new TestObject();
//		testObject.setId("testid");
//		testObject.setName("jackson");
//		importBuilder.addFieldValue("testObject",testObject);
		importBuilder.addFieldValue("author","作者");

		/**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				//可以根据条件定义是否丢弃当前记录
				//context.setDrop(true);return;
//				if(s.incrementAndGet() % 2 == 0) {
//					context.setDrop(true);
//					return;
//				}


				context.addFieldValue("author","duoduo");
				context.addFieldValue("title","解放");
				context.addFieldValue("subtitle","中国人民解放了");
				context.addFieldValue("collecttime",new Date());//

//				context.addIgnoreFieldMapping("title");
				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//				context.addIgnoreFieldMapping("author");

//				//修改字段名称title为新名称newTitle，并且修改字段的值
//				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
				context.addIgnoreFieldMapping("subtitle");
				/**
				 * 获取ip对应的运营商和区域信息
				 */
				Map ipInfo = (Map)context.getValue("ipInfo");
				if(ipInfo != null)
					context.addFieldValue("ipinfo", SimpleStringUtil.object2json(ipInfo));
				else{
					context.addFieldValue("ipinfo", "");
				}
				DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
				Date optime = context.getDateValue("logOpertime",dateFormat);
				context.addFieldValue("logOpertime",optime);
				context.addFieldValue("collecttime",new Date());

				/**
				 //关联查询数据,单值查询
				 Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
				 "select * from head where billid = ? and othercondition= ?",
				 context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
				 //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
				 context.addFieldValue("headdata",headdata);
				 //关联查询数据,多值查询
				 List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
				 "select * from facedata where billid = ?",
				 context.getIntegerValue("billid"));
				 //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
				 context.addFieldValue("facedatas",facedatas);
				 */
			}
		});
		//映射和转换配置结束

		/**
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
//		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在调试需要的时候才打开，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度
		importBuilder.setPrintTaskLog(true);
		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false

		/**
		 * 执行es数据导入数据库表操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作
	}
}
```

### 基于数字增量同步-采用slicescroll机制

从es中查询数据导入数据库案例,基于数字类型增量同步，采用slicescroll检索

```java
public class ES2DBSliceScrollResultCallbackDemo {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public static void main(String[] args){
		ES2DBSliceScrollResultCallbackDemo esDemo = new ES2DBSliceScrollResultCallbackDemo();
		esDemo.scheduleSlieRefactorImportData();;
//		esDemo.directExport();
//		esDemo.exportData();
//		esDemo.exportSliceData();
//		esDemo.exportSliceDataWithInnerhit();
//		esDemo.exportDataUseSQL();
//		esDemo.exportParallelData();
		System.out.println("complete.");
	}


	public void scheduleSlieRefactorImportData(){
		ES2DBExportBuilder importBuilder = new ES2DBExportBuilder();
		importBuilder.setBatchSize(2).setFetchSize(10);


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// log_id和数据库对应的字段一致,就不需要设置setNumberLastValueColumn和setNumberLastValueColumn信息，
		// 但是需要设置setLastValueType告诉工具增量字段的类型

		/**
		 * es相关配置
		 */
		importBuilder
				.setDsl2ndSqlFile("dsl2ndSqlFile.xml")
				.setDslName("scrollSliceQuery")
				.setScrollLiveTime("10m")
				.setSliceQuery(true)
				.setSliceSize(5)
				.setSqlName("insertSQLnew")
				.setQueryUrl("dbdemo/_search")

//				//添加dsl中需要用到的参数及参数值
				.addParam("var1","v1")
				.addParam("var2","v2")
				.addParam("var3","v3");

		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束

		importBuilder.setExportResultHandler(new ExportResultHandler() {
			@Override
			public void success(TaskCommand taskCommand, Object result) {
				System.out.println("success");
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(SimpleStringUtil.object2json(taskMetrics));
			}

			@Override
			public void error(TaskCommand taskCommand, Object result) {
				System.out.println("error");
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(SimpleStringUtil.object2json(taskMetrics));
			}

			@Override
			public void exception(TaskCommand taskCommand, Exception exception) {
				System.out.println("exception");
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(SimpleStringUtil.object2json(taskMetrics));
			}

			@Override
			public int getMaxRetry() {
				return -1;
			}
		});
//		//设置任务执行拦截器结束，可以添加多个
		//增量配置开始
		importBuilder.setNumberLastValueColumn("logId");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
//		importBuilder.setDateLastValueColumn("log_id");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
		importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("es2dbdemo_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
		importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
		// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
		//增量配置结束

		//映射和转换配置开始
//		/**
//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
//		 */
//		importBuilder.addFieldMapping("document_id","docId")
//				.addFieldMapping("docwtime","docwTime")
//				.addIgnoreFieldMapping("channel_id");//添加忽略字段
//
//
//		/**
//		 * 为每条记录添加额外的字段和值
//		 * 可以为基本数据类型，也可以是复杂的对象
//		 */
//		importBuilder.addFieldValue("testF1","f1value");
//		importBuilder.addFieldValue("testInt",0);
//		importBuilder.addFieldValue("testDate",new Date());
//		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
//		TestObject testObject = new TestObject();
//		testObject.setId("testid");
//		testObject.setName("jackson");
//		importBuilder.addFieldValue("testObject",testObject);
		importBuilder.addFieldValue("author","作者");
//		final AtomicInteger s = new AtomicInteger(0);
		/**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				//可以根据条件定义是否丢弃当前记录
				//context.setDrop(true);return;
//				if(s.incrementAndGet() % 2 == 0) {
//					context.setDrop(true);
//					return;
//				}


				context.addFieldValue("author","duoduo");
				context.addFieldValue("title","解放");
				context.addFieldValue("subtitle","中国人民解放了");
				context.addFieldValue("collecttime",new Date());//

//				context.addIgnoreFieldMapping("title");
				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//				context.addIgnoreFieldMapping("author");

//				//修改字段名称title为新名称newTitle，并且修改字段的值
//				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
				context.addIgnoreFieldMapping("subtitle");
				/**
				 * 获取ip对应的运营商和区域信息
				 */
				IpInfo ipInfo = context.getIpInfoByIp("113.12.192.230");
				if(ipInfo != null)
					context.addFieldValue("ipinfo", SimpleStringUtil.object2json(ipInfo));
				else{
					context.addFieldValue("ipinfo", "");
				}
				DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
				Date optime = context.getDateValue("logOpertime",dateFormat);
				context.addFieldValue("logOpertime",optime);
				context.addFieldValue("collecttime",new Date());
				/**
				 //关联查询数据,单值查询
				 Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
				 "select * from head where billid = ? and othercondition= ?",
				 context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
				 //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
				 context.addFieldValue("headdata",headdata);
				 //关联查询数据,多值查询
				 List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
				 "select * from facedata where billid = ?",
				 context.getIntegerValue("billid"));
				 //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
				 context.addFieldValue("facedatas",facedatas);
				 */
			}
		});
		//映射和转换配置结束

		/**
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
//		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在调试需要的时候才打开，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度
		importBuilder.setPrintTaskLog(true);
		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false

		/**
		 * 执行es数据导入数据库表操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作
	}

}
```



## 3.2 jdk timer同步器demo

 https://gitee.com/bboss/db2es-booter/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBScrollDemo.java 

 https://gitee.com/bboss/db2es-booter/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBScrollTimestampDemo.java 

 https://gitee.com/bboss/db2es-booter/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBSliceScrollResultCallbackDemo.java 

## 3.3 quartz同步器demo

 https://gitee.com/bboss/db2es-booter/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzES2DBImportTask.java 

## 3.4 xxl-job同步器demo

 https://gitee.com/bbossgroups/db-elasticsearch-xxjob/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/jobhandler/XXJobES2DBImportTask.java 

# 4 Mongodb-Elasticsearch数据同步使用方法

Mongodb-Elasticsearch数据同步案例工程

https://github.com/bbossgroups/mongodb-elasticsearch

工程基于gradle管理，可以参考文档配置gradle环境：

https://esdoc.bbossgroups.com/#/bboss-build

mongodb-elasticseach数据同步使用方法和DB-Elasticsearch、Elasticsearch-DB数据同步的使用方法类似，支持全量、增量定时同步功能， 内置jdk timer同步器，支持quartz、xxl-job任务调度引擎 ，这里就不具体举例说明，大家可以下载demo研究即可，mongodb-elasticseach数据同步基本和DB-Elasticsearch同步的参数配置差不多，这里介绍一下mongodb-elasticseach同步特有的参数：

```java
        //mongodb的相关配置参数
		importBuilder.setName("session")
				.setDb("sessiondb")
				.setDbCollection("sessionmonitor_sessions")
				.setConnectTimeout(10000)
				.setWriteConcern("JOURNAL_SAFE")
				.setReadPreference("")
				.setMaxWaitTime(10000)
				.setSocketTimeout(1500).setSocketKeepAlive(true)
				.setConnectionsPerHost(100)
				.setThreadsAllowedToBlockForConnectionMultiplier(6)
				.setServerAddresses("127.0.0.1:27017\n127.0.0.1:27018")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
				// mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
				//String database,String userName,String password,String mechanism
				//https://www.iteye.com/blog/yin-bp-2064662
//				.buildClientMongoCredential("sessiondb","bboss","bboss","MONGODB-CR")
//				.setOption("")
				.setAutoConnectRetry(true);
        importBuilder.setFetchSize(10); //按批从mongodb拉取数据的大小
```

一个完整的jdk timer同步器demo：根据session最后访问时间将保存在mongodb中的session数据，根据一定的时间间隔增量同步到Elasitcsearch中，如需调试同步功能，直接运行和调试main方法即可，elasticsearch的配置在resources/application.properties中进行配置：

 https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/resources/application.properties 

```java
public class Mongodb2ESdemo {
	private static Logger logger = LoggerFactory.getLogger(Mongodb2ESdemo.class);
	public static void main(String args[]){
		Mongodb2ESdemo dbdemo = new Mongodb2ESdemo();
		boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值

		dbdemo.scheduleTimestampImportData(dropIndice);
	}



	/**
	 * elasticsearch地址和数据库地址都从外部配置文件application.properties中获取，加载数据源配置和es配置
	 */
	public void scheduleTimestampImportData(boolean dropIndice){
		MongoDB2ESExportBuilder importBuilder = MongoDB2ESExportBuilder.newInstance();
		//增量定时任务不要删表，但是可以通过删表来做初始化操作
		if(dropIndice) {
			try {
				//清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
				String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("mongodbdemo");
				System.out.println(repsonse);
			} catch (Exception e) {
			}
		}


		//mongodb的相关配置参数

		importBuilder.setName("session") 
				.setDb("sessiondb")
				.setDbCollection("sessionmonitor_sessions")
				.setConnectTimeout(10000)
				.setWriteConcern("JOURNAL_SAFE")
				.setReadPreference("")
				.setMaxWaitTime(10000)
				.setSocketTimeout(1500).setSocketKeepAlive(true)
				.setConnectionsPerHost(100)
				.setThreadsAllowedToBlockForConnectionMultiplier(6)
				.setServerAddresses("127.0.0.1:27017\n127.0.0.1:27018")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
				// mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
				//String database,String userName,String password,String mechanism
				//https://www.iteye.com/blog/yin-bp-2064662
//				.buildClientMongoCredential("sessiondb","bboss","bboss","MONGODB-CR")
//				.setOption("")
				.setAutoConnectRetry(true);
       importBuilder.setFetchSize(10); //按批从mongodb拉取数据的大小
        
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("mongodbdemo") //必填项，索引名称
				.setIndexType("mongodbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
				.setBatchSize(10);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束
//
//		//设置任务执行拦截器，可以添加多个，定时任务每次执行的拦截器
//		importBuilder.addCallInterceptor(new CallInterceptor() {
//			@Override
//			public void preCall(TaskContext taskContext) {
//				System.out.println("preCall");
//			}
//
//			@Override
//			public void afterCall(TaskContext taskContext) {
//				System.out.println("afterCall");
//			}
//
//			@Override
//			public void throwException(TaskContext taskContext, Exception e) {
//				System.out.println("throwException");
//			}
//		}).addCallInterceptor(new CallInterceptor() {
//			@Override
//			public void preCall(TaskContext taskContext) {
//				System.out.println("preCall 1");
//			}
//
//			@Override
//			public void afterCall(TaskContext taskContext) {
//				System.out.println("afterCall 1");
//			}
//
//			@Override
//			public void throwException(TaskContext taskContext, Exception e) {
//				System.out.println("throwException 1");
//			}
//		});
//		//设置任务执行拦截器结束，可以添加多个
		//增量配置开始
		importBuilder.setNumberLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
//		importBuilder.setDateLastValueColumn("log_id");//手动指定日期增量查询字段
		importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
//		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型,ImportIncreamentConfig.TIMESTAMP_TYPE为时间类型
        //设置增量查询的起始值lastvalue
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date.getTime());
		}
		catch (Exception e){
			e.printStackTrace();
		}
		// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
		//增量配置结束

		//映射和转换配置开始
//		/**
//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//		 *
//		 */
//		importBuilder.addFieldMapping("document_id","docId")
//				.addFieldMapping("docwtime","docwTime")
//				.addIgnoreFieldMapping("channel_id");//添加忽略字段
//
//
//		/**
//		 * 为每条记录添加额外的字段和值
//		 * 可以为基本数据类型，也可以是复杂的对象
//		 */
//		importBuilder.addFieldValue("testF1","f1value");
//		importBuilder.addFieldValue("testInt",0);
//		importBuilder.addFieldValue("testDate",new Date());
//		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
//		TestObject testObject = new TestObject();
//		testObject.setId("testid");
//		testObject.setName("jackson");
//		importBuilder.addFieldValue("testObject",testObject);
//
//		/**
//		 * 重新设置导入es数据结构,默认情况下，除了_id字段，其他所有的mongodb字段都会被同步到Elasticsearch中，可以通过DataRefactor来进行相关调整和处理数据，然后再导入es中。
//		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				String id = context.getStringValue("_id");
				//根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中
				if(id.equals("5dcaa59e9832797f100c6806"))
					context.setDrop(true);
				//添加字段extfiled2到记录中，值为2
				context.addFieldValue("extfiled2",2);
				//添加字段extfiled到记录中，值为1
				context.addFieldValue("extfiled",1);
				boolean httpOnly = context.getBooleanValue("httpOnly");
				boolean secure = context.getBooleanValue("secure");
				//空值处理
				String userAccount = context.getStringValue("userAccount");
				if(userAccount == null)
					context.addFieldValue("userAccount","");
				//空值处理
				String testVO = context.getStringValue("testVO");
				if(testVO == null)
					context.addFieldValue("testVO","");
				//空值处理
				String privateAttr = context.getStringValue("privateAttr");
				if(privateAttr == null)
					context.addFieldValue("privateAttr","");
				//空值处理
				String local = context.getStringValue("local");
				if(local == null)
					context.addFieldValue("local","");
				//将long类型的lastAccessedTime字段转换为日期类型
				long lastAccessedTime = context.getLongValue("lastAccessedTime");
				context.addFieldValue("lastAccessedTime",new Date(lastAccessedTime));
				//将long类型的creationTime字段转换为日期类型
				long creationTime = context.getLongValue("creationTime");
				context.addFieldValue("creationTime",new Date(creationTime));
                //并将IpInfo添加到Elasticsearch文档中
				String referip = context.getStringValue("referip");
				if(referip != null){
					IpInfo ipInfo = context.getIpInfoByIp(referip);
					if(ipInfo != null)
						context.addFieldValue("ipInfo",ipInfo);
				}
				 //除了通过context接口获取mongodb的记录字段，还可以直接获取当前的mongodb记录，可自行利用里面的值进行相关处理
				DBObject record = (DBObject) context.getRecord();
				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//				context.addIgnoreFieldMapping("author");
//				context.addIgnoreFieldMapping("title");
//				context.addIgnoreFieldMapping("subtitle");
			}
		});
		//映射和转换配置结束

		/**
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
		importBuilder.setEsIdField("_id");//设置文档主键，不设置，则自动产生文档id,直接将mongodb的ObjectId设置为Elasticsearch的文档_id
//		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在调试需要的时候才打开，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度

		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
		importBuilder.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
		//设置任务处理结果回调接口
		importBuilder.setExportResultHandler(new ExportResultHandler<Object,String>() {
			@Override
			public void success(TaskCommand<Object,String> taskCommand, String result) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
			}

			@Override
			public void error(TaskCommand<Object,String> taskCommand, String result) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
			}

			@Override
			public void exception(TaskCommand<Object,String> taskCommand, Exception exception) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
			}

			@Override
			public int getMaxRetry() {
				return 0;
			}
		});
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
		 * 构建DataStream，执行mongodb数据到es的同步操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行同步操作

		System.out.println();
	}

}
```

# 5 Database-Database数据同步使用方法

 https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Db2DBdemo.java 

# 6 Kafka1x-Elasticsearch数据同步使用方法

https://github.com/bbossgroups/kafka1x-elasticsearch

# 7 Kafka2x-Elasticsearch数据同步使用方法

https://github.com/bbossgroups/kafka2x-elasticsearch

# 8 Elasticsearch-Elasticsearch数据同步使用方法

https://github.com/bbossgroups/elasticsearch-elasticsearch

# 9 数据同步调优

数据同步是一个非常耗资源（内存、cpu、io）的事情，所以如何充分利用系统资源，确保高效的数据同步作业长时间稳定运行，同时又不让同步服务器、Elasticsearch/数据库负荷过高，是一件很有挑战意义的事情，这里结合bboss的实践给出一些建议：

## 9.1 内存调优

内存溢出很大一个原因是jvm配置少了，这个处理非常简单，修改jvm.option文件，适当调大内存即可，设置作业运行需要的jvm内存，按照比例调整Xmx和MaxNewSize参数：

```properties
-Xms1g
-Xmx1g
-XX:NewSize=512m
-XX:MaxNewSize=512m
```

Xms和Xmx保持一样，NewSize和MaxNewSize保持一样，Xmx和MaxNewSize大小保持的比例可以为3:1或者2:1

影响内存使用情况的其他关键参数:

- 并发线程数（threadCount）：每个线程都会把正在处理的数据放到内存中

- 线程缓冲队列数（queue）：工作线程全忙的情况下，后续的数据处理请求会放入

- batchSize（批量写入记录数）：决定了每批记录的大小，假如并发线程数和线程缓冲队列数全满，那么占用内存的换算方法：

$$
threadCount * batchSize * 每条记录的size + queue * batchSize   * 每条记录的size
$$

- jdbcFetchSize/fetchSize：从数据源按批拉取记录数，拉取过来的数据会临时放入本地内存中

这些参数设置得越大，占用的内存越大，处理的速度就越快，典型的空间换时间的场景，所以需要根据同步服务器的主机内存来进行合理配置，避免由于资源不足出现jvm内存溢出的问题，影响同步的稳定性。

##   9.2 采用分布式作业调度引擎

需要同步的数据量很大，单机的处理能力有限，可以基于分布式作业调度引擎来实现数据分布式分片数据同步处理，参考文档：

https://esdoc.bbossgroups.com/#/db-es-tool?id=_26-%e5%9f%ba%e4%ba%8exxjob-%e5%90%8c%e6%ad%a5db-elasticsearch%e6%95%b0%e6%8d%ae

  

# 10 开发交流

完整的数据导入demo工程

github：[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)

码云：<https://gitee.com/bboss/db2es-booter>



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 11 支持我们

<div align="left"></div>
<img src="images/alipay.png"  height="200" width="200">

