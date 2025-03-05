# DB-ES数据同步工具使用方法

bboss提供了一个数据库到Elasticsearch数据同步样板工程:[bboss/bboss-datatran-demo](https://gitee.com/bboss/bboss-datatran-demo)，用来将写好的同步代码打包发布成可以运行的二进制包上传到服务器运行，[bboss/bboss-datatran-demo](https://gitee.com/bboss/bboss-datatran-demo)提供了现成的运行指令和jvm配置文件。

# 1 环境准备

首先需要从Github或者gitee下载最新的工具源码：

https://github.com/bbossgroups/bboss-datatran-demo

https://gitee.com/bboss/bboss-datatran-demo

工具目录结构说明：

![](images/db-es.png)


[bboss/bboss-datatran-demo](https://gitee.com/bboss/bboss-datatran-demo)是一个gradle工程，因此需要安装最新版本的gradle并配置好gradle环境变量，gradle安装和配置参考文档：

https://esdoc.bbossgroups.com/#/bboss-build

安装和配置好gradle，就可以将bboss/bboss-datatran-demo工程导入idea或者eclipse，然后进行数据同步逻辑的开发、调试以及构建打包工作。

# 2 同步作业主程序定义

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
   		Db2EleasticsearchDemo db2EleasticsearchDemo = new Db2EleasticsearchDemo();
   		//从配置文件application.properties中获取参数值
   		boolean dropIndice = PropertiesUtil.getPropertiesContainer("application.properties").getBooleanSystemEnvProperty("dropIndice",true);
   //		dbdemo.fullImportData(  dropIndice);
   //		dbdemo.scheduleImportData(dropIndice);
   		db2EleasticsearchDemo.scheduleTimestampImportData(dropIndice);
   //		dbdemo.scheduleImportData(dropIndice);
   //		args[1].charAt(0) == args[2].charAt(0);
   	}
   
   	/**
   	 * elasticsearch地址和数据库地址都从外部配置文件application.properties中获取，加载数据源配置和es配置
   	 * 从配置文件application.properties中获取参数值方法
   	 * boolean dropIndice = PropertiesUtil.getPropertiesContainer().getBooleanSystemEnvProperty("dropIndice",true);
   	 * int threadCount = PropertiesUtil.getPropertiesContainer().getIntSystemEnvProperty("log.threadCount",2);
   	 */
   	public void scheduleTimestampImportData(boolean dropIndice){
   
   		ImportBuilder importBuilder = new ImportBuilder() ;
   		DBInputConfig dbInputConfig = new DBInputConfig();
   		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
   		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
   		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
   		// 需要设置setLastValueColumn信息log_id，
   		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型
   
   //		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
   		dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
   				.setDbName("test")
   				.setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
   				.setDbUrl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") 
   				.setDbUser("root")
   				.setDbPassword("123456")
   				.setValidateSQL("select 1")
   				.setUsePool(true)
   				.setDbInitSize(5)
   				.setDbMinIdleSize(5)
   				.setDbMaxSize(10)
   				.setShowSql(true);//是否使用连接池;
   		importBuilder.setInputConfig(dbInputConfig);
   
   
   
   
   //		importBuilder.addFieldMapping("LOG_CONTENT","message");
   //		importBuilder.addIgnoreFieldMapping("remark1");
   //		importBuilder.setSql("select * from td_sm_log ");
   		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
   		elasticsearchOutputConfig
   				.addTargetElasticsearch("elasticsearch.serverNames","default")
   				.addElasticsearchProperty("default.elasticsearch.rest.hostNames","192.168.137.1:9200")
   				.addElasticsearchProperty("default.elasticsearch.showTemplate","true")
   				.addElasticsearchProperty("default.elasticUser","elastic")
   				.addElasticsearchProperty("default.elasticPassword","changeme")
   				.addElasticsearchProperty("default.elasticsearch.failAllContinue","true")
   				.addElasticsearchProperty("default.http.timeoutSocket","60000")
   				.addElasticsearchProperty("default.http.timeoutConnection","40000")
   				.addElasticsearchProperty("default.http.connectionRequestTimeout","70000")
   				.addElasticsearchProperty("default.http.maxTotal","200")
   				.addElasticsearchProperty("default.http.defaultMaxPerRoute","100")
   				.setIndex("dbdemo")
   				.setEsIdField("log_id")//设置文档主键，不设置，则自动产生文档id
   				.setDebugResponse(false)//设置是否将每次处理的reponse打印到日志文件中，默认false
   				.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
   		/**
   		 elasticsearchOutputConfig.setEsIdGenerator(new EsIdGenerator() {
   		 //如果指定EsIdGenerator，则根据下面的方法生成文档id，
   		 // 否则根据setEsIdField方法设置的字段值作为文档id，
   		 // 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id
   
   		 @Override
   		 public Object genId(Context context) throws Exception {
   		 return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
   		 }
   		 });
   		 */
   //				.setIndexType("dbdemo") ;//es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType;
   //				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
   		/**
   		 * es相关配置
   		 */
   //		elasticsearchOutputConfig.setTargetElasticsearch("default,test");//同步数据到两个es集群
   
   		importBuilder.setOutputConfig(elasticsearchOutputConfig);
   
   		/**
   		 * 设置IP地址信息库
   		 */
   		importBuilder.setGeoipDatabase("d:/geolite2/GeoLite2-City.mmdb");
   		importBuilder.setGeoipAsnDatabase("d:/geolite2/GeoLite2-ASN.mmdb");
   		importBuilder.setGeoip2regionDatabase("d:/geolite2/ip2region.db");
   
   		importBuilder
   //
   				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
   				.setUseLowcase(true)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
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
   //		importBuilder.setStatusDbname("test");//设置增量状态数据源名称
   		importBuilder.setLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
   		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
   //		setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
   		importBuilder.setStatusDbname("logtable");
   		importBuilder.setLastValueStorePath("logtable_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
   		importBuilder.setLastValueStoreTableName("logstable");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
   		importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
   //		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
   //		try {
   //			Date date = format.parse("2000-01-01");
   //			importBuilder.setLastValue(date);//增量起始值配置
   //		}
   //		catch (Exception e){
   //			e.printStackTrace();
   //		}
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
   		/**
   		 * 重新设置es数据结构
   		 */
   		importBuilder.setDataRefactor(new DataRefactor() {
   			public void refactor(Context context) throws Exception  {
   //				Date date = context.getDateValue("LOG_OPERTIME");
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
            //关联查询数据,单值查询
            Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,"test",
                                                "select * from head where billid = ? and othercondition= ?",
                                                context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
            //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("headdata",headdata);
            //关联查询数据,多值查询
            List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,"test",
                  "select * from facedata where billid = ?",
                  context.getIntegerValue("billid"));
            //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("facedatas",facedatas);
             */
   				context.addFieldValue("collecttime",new Date());
   				IpInfo ipInfo = context.getIpInfoByIp("219.133.80.136");
   				if(ipInfo != null)
   					context.addFieldValue("ipInfo", SimpleStringUtil.object2json(ipInfo));
   			}
   		});
   		//映射和转换配置结束
   
   		/**
   		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
   		 */
   		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
   		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
   		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
   		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
   		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
   
   		importBuilder.setExportResultHandler(new ExportResultHandler<String>() {
   			@Override
   			public void success(TaskCommand<String>taskCommand, String result) {
   				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
   				logger.info(taskMetrics.toString());
   				logger.debug(result);
   			}
   
   			@Override
   			public void error(TaskCommand<String>taskCommand, String result) {
   				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
   				logger.info(taskMetrics.toString());
   				logger.debug(result);
   			}
   
   			@Override
   			public void exception(TaskCommand<String>taskCommand, Exception exception) {
   				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
   				logger.debug(taskMetrics.toString());
   			}
   
   			@Override
   			public int getMaxRetry() {
   				return 0;
   			}
   		});
   
   
   		/**
   		 * 执行数据库表数据导入es操作
   		 */
   		DataStream dataStream = importBuilder.builder();
   		dataStream.execute();//执行导入操作
   //		dataStream.destroy();//释放资源
   
   
   	}

  
}
```

Dbdemo完整的内容参考：

<https://gitee.com/bboss/bboss-datatran-demo/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java>

开发过程中可以直接运行Dbdemo中的main方法来调试作业程序。

## 2.1 数据同步条件设置

可以额外设置数据同步条件，实现有条件的定时/一次性全量同步或者定时增量导入。

通过importBuilder.addParam添加sql条件变量值，然后在sql语句中用 #[xxx]变量语法引用变量，xxx代表变量名称。举例说明如下： 

定时按特定条件导入数据

```java
dbInputConfig.setSql("select * from batchtest1 where optime >= #[start_optime] and optime < #[end_optime]");

		importBuilder.addParam("start_optime", TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2018-03-21 00:27:21"))
				.addParam("end_optime",TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2019-12-30 00:27:21"));
```

定时按特定条件增量导入数据
```java
dbInputConfig.setSql("select * from batchtest1 where optime >= #[start_optime] and optime < #[end_optime] and collecttime > #[collecttime]");
		importBuilder.setLastValueColumn("collecttime");
		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);

		importBuilder.addParam("start_optime", TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2018-03-21 00:27:21"))
				.addParam("end_optime",TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2019-12-30 00:27:21"));
```



# 3 es数据源配置

通过ElasticsearchOutputConfig设置Elasticsearch输出数据源参数：elasticsearch集群地址和连接参数，输出索引、索引文档id字段等

```java
ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
   		elasticsearchOutputConfig
   				.addTargetElasticsearch("elasticsearch.serverNames","default")
   				.addElasticsearchProperty("default.elasticsearch.rest.hostNames","192.168.137.1:9200")
   				.addElasticsearchProperty("default.elasticsearch.showTemplate","true")
   				.addElasticsearchProperty("default.elasticUser","elastic")
   				.addElasticsearchProperty("default.elasticPassword","changeme")
   				.addElasticsearchProperty("default.elasticsearch.failAllContinue","true")
   				.addElasticsearchProperty("default.http.timeoutSocket","60000")
   				.addElasticsearchProperty("default.http.timeoutConnection","40000")
   				.addElasticsearchProperty("default.http.connectionRequestTimeout","70000")
   				.addElasticsearchProperty("default.http.maxTotal","200")
   				.addElasticsearchProperty("default.http.defaultMaxPerRoute","100")
   				.setIndex("dbdemo")
   				.setEsIdField("log_id")//设置文档主键，不设置，则自动产生文档id
   				.setDebugResponse(false)//设置是否将每次处理的reponse打印到日志文件中，默认false
   				.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
   		/**
   		 elasticsearchOutputConfig.setEsIdGenerator(new EsIdGenerator() {
   		 //如果指定EsIdGenerator，则根据下面的方法生成文档id，
   		 // 否则根据setEsIdField方法设置的字段值作为文档id，
   		 // 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id
   
   		 @Override
   		 public Object genId(Context context) throws Exception {
   		 return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
   		 }
   		 });
   		 */
   //				.setIndexType("dbdemo") ;//es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType;
   //				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
   		/**
   		 * es相关配置
   		 */
   //		elasticsearchOutputConfig.setTargetElasticsearch("default,test");//同步数据到两个es集群
	elasticsearchOutputConfig.setFilterPath(BulkConfig.ERROR_FILTER_PATH_ONLY);//指定响应报文只返回错误信息
   
   		importBuilder.setOutputConfig(elasticsearchOutputConfig);
```

# 4 数据库数据源配置

通过DBInputConfig来配置输入DB数据源配置：数据库地址、连接池配置、查询sql及增量条件等

```java
DBInputConfig dbInputConfig = new DBInputConfig();
   		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
   		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
   		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
   		// 需要设置setLastValueColumn信息log_id，
   		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型
   
   //		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
   		dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
   				.setDbName("test")
   				.setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
   				.setDbUrl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") 
   				.setDbUser("root")
   				.setDbPassword("123456")
   				.setValidateSQL("select 1")
   				.setUsePool(true)
   				.setDbInitSize(5)
   				.setDbMinIdleSize(5)
   				.setDbMaxSize(10)
   				.setShowSql(true);//是否使用连接池;
   		importBuilder.setInputConfig(dbInputConfig);
```

# 5 自定义适配器数据源配置

不同的数据库需要指定特定的适配器，主流的数据库oracle、mysql、db2、sqlserver、hive、postgresql、达梦等都内置了适配器，如果是一个新的数据库，可以通过自定义适配器进行适配，以达梦数据库为例说明

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

在dbInputConfig指定自定义数据源适配器

```java
dbInputConfig.setDbAdaptor("org.frameworkset.elasticsearch.imp.DMAdaptor")
```



# 6 保存增量状态的数据源配置

如果不想采用默认的sqlite保存增量状态或者采用分布式作业调度引擎时，可以指定其他数据库来保存增量状态，可以通过配置文件指定增量状态数据源，也可以通过代码指定增量数据源

通过配置文件指定

修改配置文件src\main\resources\application.properties

```properties
# 增量导入状态存储数据源配置，默认采用sqlite，增量导入装存储到本地的sqlite数据库中，采用分布式的外部定时任务引擎时，
# 就不能将状态存储到本地，需要采用外部的数据库（mysql,oracle等）来存储增量导入状态。
# 如果做了config.db配置，则采用配置的的数据源，如果使用的数据库不是mysql、oracle、sql server，则必须指定创建statusTableName的建表语句，每种数据库对应的语法做适当调整
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

通过代码指定增量数据源名称：importBuilder.setStatusDbname("test")

```java
importBuilder.setStatusDbname("test");//设置增量状态数据源名称
```

名称为test数据源与输入数据源的名称一样，已经通过DBInputConfig进行定义，如果是一个未定义的数据源，则可以通过设置ImportStartAction来定义状态数据源testStatus，通过来在作业停止时释放数据源：

```java
 //在任务数据抽取之前做一些初始化处理，例如：通过删表来做初始化操作

		importBuilder.setImportStartAction(new ImportStartAction() {
			/**
			 * 初始化之前执行的处理操作，比如后续初始化操作、数据处理过程中依赖的资源初始化
			 * @param importContext
			 */
			@Override
			public void startAction(ImportContext importContext) {


				importContext.addResourceStart(new ResourceStart() {
					@Override
					public ResourceStartResult startResource() {
						DBConf tempConf = new DBConf();
						tempConf.setPoolname("testStatus");
						tempConf.setDriver("com.mysql.cj.jdbc.Driver");
						tempConf.setJdbcurl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true");

						tempConf.setUsername("root");
						tempConf.setPassword("123456");
						tempConf.setValidationQuery("select 1");

						tempConf.setInitialConnections(5);
						tempConf.setMinimumSize(10);
						tempConf.setMaximumSize(10);
						tempConf.setUsepool(true);
						tempConf.setShowsql(true);
						tempConf.setJndiName("testStatus-jndi");
						//# 控制map中的列名采用小写，默认为大写
						tempConf.setColumnLableUpperCase(false);
						//启动数据源
						boolean result = SQLManager.startPool(tempConf);
						ResourceStartResult resourceStartResult = null;
						//记录启动的数据源信息，用户作业停止时释放数据源
						if(result){
							resourceStartResult = new DBStartResult();
							resourceStartResult.addResourceStartResult("testStatus");
						}
						return resourceStartResult;
					}
				});

			}

			/**
			 * 所有初始化操作完成后，导出数据之前执行的操作
			 * @param importContext
			 */
			@Override
			public void afterStartAction(ImportContext importContext) {
				if(dropIndice) {
					try {
						//清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
						ElasticSearchHelper.getRestClientUtil().dropIndice("dbdemo");
					} catch (Exception e) {
						logger.error("Drop indice dbdemo failed:",e);
					}
				}
			}
		});
```
设置定义好的数据源名称testStatus即可：
```java
importBuilder.setStatusDbname("testStatus");//设置增量状态数据源名称
```



# 7 同步作业主程序配置

定义好同步作业主程序后，需要在application.properties文件中通过mainclass属性指定需要执行的作业主程序，这样bboss在启动作业的时候就会运行其中定义的main方法,执行数据同步功能：

```java
public static void main(String args[]){
   Dbdemo dbdemo = new Dbdemo();
   boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值
   dbdemo.scheduleFullImportData(  dropIndice);
}
```

在application.properties文件中通过mainclass属性指定需要执行的作业主程序：

```properties
#同步作业主程序
mainclass=org.frameworkset.elasticsearch.imp.Dbdemo
```

# 8 测试以及调试同步代码

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





# 9 查看任务执行详细日志

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



# 10 数据导入不完整原因分析及处理

如果在任务执行完毕后，发现es中的数据与数据库源表的数据不匹配，可能的原因如下：

**1.并行执行的过程中存在失败的任务（比如服务端超时），这种情况通过setExportResultHandler设置的exception监听方法进行定位分析**

参考章节【[设置任务执行结果回调处理函数](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2312-%e8%ae%be%e7%bd%ae%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%93%e6%9e%9c%e5%9b%9e%e8%b0%83%e5%a4%84%e7%90%86%e5%87%bd%e6%95%b0)】

```java
 public void exception(TaskCommand<String>taskCommand, Exception exception) {
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

参考章节【[设置任务执行结果回调处理函数](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2312-%e8%ae%be%e7%bd%ae%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%93%e6%9e%9c%e5%9b%9e%e8%b0%83%e5%a4%84%e7%90%86%e5%87%bd%e6%95%b0)】

bulk拒绝记录解决办法：

a) 优化elasticsearch服务器配置(加节点，加内存和cpu等运算资源，调优网络性能等)

调整elasticsearch的相关线程和队列：调优elasticsearch配置参数

thread_pool.bulk.queue_size: 1000   es线程等待队列长度

thread_pool.bulk.size: 10   线程数量，与cpu的核数对应

b) 调整同步程序导入线程数、批处理batchSize参数，降低并行度。

数据内容不合规解决办法：拿到执行的原始批量数据，分析错误信息对应的数据记录，进行修改，然后重新导入失败的记录即可

```java
@Override
         public void error(TaskCommand<String>taskCommand, String result) {
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

# 11 作业运行jvm内存配置

修改jvm.options,设置作业运行需要的jvm内存，按照比例调整Xmx和MaxNewSize参数：

```properties
-Xms1g
-Xmx1g
-XX:NewSize=512m
-XX:MaxNewSize=512m
```

Xms和Xmx保持一样，NewSize和MaxNewSize保持一样，Xmx和MaxNewSize大小保持的比例可以为3:1或者2:1

# 12 发布版本

代码写好并经过调试后，就可以执行gradle指令构建发布bboss/bboss-datatran-demo运行包，需要安装最新版本的gradle并配置好gradle环境变量。

gradle安装和配置参考文档：https://esdoc.bbossgroups.com/#/bboss-build

我们可以在cmd行，idea、eclipse中运行打包指令，以工程目录cmd窗口为例：

```gradle
先切换到工程的根目录
cd D:\workspace\bbossesdemo\bboss-datatran-demo
release.bat
```

构建成功后，将会在工程目录下面生成可部署的二进制包（其中x.x.x为bboss最新版本号）：

build/distributions/bboss-datatran-demo-x.x.x-released.zip

发布的zip包的目录结构如下：

![img](_images/db-es-dist.png)

解压bboss-datatran-demo-x.x.x-released.zip，运行对应的作业启动或者重启脚本，即可启动作业：

作业重启

windows: restart.bat 

linux: restart.sh

作业启动

windows: start.bat 

linux: start.sh

作业停止

windows: stop.bat 

linux: stop.sh



