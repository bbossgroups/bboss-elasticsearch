# 数据同步工具在spring boot的应用

可以在spring boot中使用各种数据到Elasticsearch数据同步功能，这里以db-elasticsearch定时增量数据同步为例进行说明，其他数据源方法类似。

我们以spring boot web demo工程来介绍spring boot中使用数据同步功能,工程源码地址:

github

https://github.com/bbossgroups/springboot-elasticsearch-webservice

码云

https://gitee.com/bboss/springboot-elasticsearch

# 1.导入maven坐标

基于spring boot的数据同步功能需要导入如下maven坐标：



```xml
<dependency>
    <groupId>com.bbossgroups</groupId>
    <artifactId>bboss-spring-boot-starter</artifactId>
    <version>6.0.0</version> 			
</dependency>
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-datatran-jdbc</artifactId>
    <version>6.7.0</version>
</dependency>
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
    <version>6.7.0</version>
</dependency>
```



# 2 关键点说明

必须在同步组件中引用BBossESStarter组件，主要用于加载spring boot配置文件中的elasticsearch配置参数：

```java
@Autowired
private BBossESStarter bbossESStarter;
```

如果需要自己配置增量status表对应的数据库，还需要在同步组件中导入：

```java
	@Autowired
	private BBossStarter bbossStarter;
```
并且设置：
```java
    importBuilder.setStatusDbname("default");//default是一个数据库datasource的名称，具体配置参考application.properties文件内容
```
default是一个数据库datasource的名称，具体配置参考[application.properties](https://github.com/bbossgroups/springboot-elasticsearch-webservice/blob/master/src/main/resources/application.properties)文件内容
另外在定义DB2ESImportBuilder和DataStream两个类级变量,

```java
private ImportBuilder db2ESImportBuilder;
private DataStream dataStream;
```

通过db2ESImportBuilder和DataStream来启动和停止作业。

# 3 数据同步实现

## 3.1 增加DataTran作业组件

[DataTran](https://gitee.com/bboss/springboot-elasticsearch/blob/master/src/main/java/com/example/esbboss/service/DataTran.java)

```java
package com.example.esbboss.service;
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

import com.example.esbboss.agent.AgentInfoBo;
import com.example.esbboss.agent.Buffer;
import com.example.esbboss.agent.FixedBuffer;
import org.apache.hadoop.hbase.util.Bytes;
import org.frameworkset.elasticsearch.boot.BBossESStarter;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.config.ImportBuilder;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.metrics.TaskMetrics;
import org.frameworkset.tran.plugin.db.input.DBInputConfig;
import org.frameworkset.tran.plugin.es.output.ElasticsearchOutputConfig;
import org.frameworkset.tran.plugin.hbase.input.HBaseInputConfig;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.ImportIncreamentConfig;
import org.frameworkset.tran.schedule.TaskContext;
import org.frameworkset.tran.task.TaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/5 12:03
 * @author biaoping.yin
 * @version 1.0
 */
@Service
public class DataTran {
	private Logger logger = LoggerFactory.getLogger(DataTran.class);
	@Autowired
	private BBossESStarter bbossESStarter;
	private ImportBuilder db2ESImportBuilder;
	private DataStream dataStream;
//	@Autowired
//	private BBossStarter bbossStarter;

	public String stopDB2ESJob(){
		if(dataStream != null) {
			synchronized (this) {
				if (dataStream != null) {
					dataStream.destroy(true);
					dataStream = null;
					db2ESImportBuilder = null;
					return "db2ESImport job stopped.";
				} else {
					return "db2ESImport job has stopped.";
				}
			}
		}
		else {
			return "db2ESImport job has stopped.";
		}
	}

	private AgentInfoBo.Builder createBuilderFromValue(byte[] serializedAgentInfo) {
		final Buffer buffer = new FixedBuffer(serializedAgentInfo);
		final AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
		builder.setHostName(buffer.readPrefixedString());
		builder.setIp(buffer.readPrefixedString());
		builder.setPorts(buffer.readPrefixedString());
		builder.setApplicationName(buffer.readPrefixedString());
		builder.setServiceTypeCode(buffer.readShort());
		builder.setPid(buffer.readInt());
		builder.setAgentVersion(buffer.readPrefixedString());
		builder.setStartTime(buffer.readLong());
		builder.setEndTimeStamp(buffer.readLong());
		builder.setEndStatus(buffer.readInt());
		// FIXME - 2015.09 v1.5.0 added vmVersion (check for compatibility)
		if (buffer.hasRemaining()) {
			builder.setVmVersion(buffer.readPrefixedString());
		}
		return builder;
	}
	public  String scheduleDB2ESJob(){
		if (db2ESImportBuilder == null) {
			synchronized (this) {
				if (db2ESImportBuilder == null) {
					ImportBuilder importBuilder = ImportBuilder.newInstance();
					//增量定时任务不要删表，但是可以通过删表来做初始化操作
//			if(dropIndice) {
//				try {
//					//清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
//					String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("dbdemo");
//					System.out.println(repsonse);
//				} catch (Exception e) {
//				}
//			}
//					//数据源相关配置，可选项，可以在外部启动数据源
//					importBuilder.setDbName("test")
//							.setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
//							//mysql stream机制一 通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
////					.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false")
////					.setJdbcFetchSize(3000)//启用mysql stream机制1，设置jdbcfetchsize大小为3000
//							//mysql stream机制二  jdbcFetchSize为Integer.MIN_VALUE即可，url中不需要设置useCursorFetch=true参数，这里我们使用机制二
//							.setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false")
//							.setJdbcFetchSize(Integer.MIN_VALUE)//启用mysql stream机制二,设置jdbcfetchsize大小为Integer.MIN_VALUE
//							.setDbUser("root")
//							.setDbPassword("123456")
//							.setValidateSQL("select 1")
//							.setUsePool(false);//是否使用连接池
					DBInputConfig dbInputConfig = new DBInputConfig();
					dbInputConfig.setDbName("test");//这里只需要指定dbname，具体的数据源配置在application.properties文件中指定

					//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
					// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
					// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
					// log_id和数据库对应的字段一致,就不需要设置setLastValueColumn信息，
					// 但是需要设置setLastValueType告诉工具增量字段的类型

					dbInputConfig.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
					importBuilder.setInputConfig(dbInputConfig);
					/**
					 * es相关配置
					 */
					ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
					elasticsearchOutputConfig.setTargetElasticsearch("default");
					elasticsearchOutputConfig
							.setIndex("dbdemo") //必填项
							.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id

					/**
					 elasticsearchOutputConfig.setEsIdGenerator(new EsIdGenerator() {
					 //如果指定EsIdGenerator，则根据下面的方法生成文档id，
					 // 否则根据setEsIdField方法设置的字段值作为文档id，
					 // 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id

					 @Override public Object genId(Context context) throws Exception {
					 return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
					 }
					 });
					 */
//					.setIndexType("dbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
					importBuilder.setOutputConfig(elasticsearchOutputConfig)
							.setUseJavaName(false) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
							.setUseLowcase(false)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
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
			}

			@Override
			public void afterCall(TaskContext taskContext) {
				if(taskContext != null)
					logger.info(taskContext.getJobTaskMetrics().toString());
			}

			@Override
			public void throwException(TaskContext taskContext, Exception e) {
				if(taskContext != null)
					logger.info(taskContext.getJobTaskMetrics().toString(),e);
			}
		});
//		.addCallInterceptor(new CallInterceptor() {
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
//		importBuilder.setLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
					importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
					importBuilder.setStatusDbname("logtable");
					importBuilder.setLastValueStorePath("logtable_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
					importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
//					importBuilder.setStatusDbname("default");//default是一个数据库datasource的名称，具体配置参考application.properties文件内容：
					importBuilder.setAsynFlushStatusInterval(10000);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					try {
						Date date = format.parse("2000-01-01");
						importBuilder.setLastValue(date);//增量起始值配置
					} catch (Exception e) {
						e.printStackTrace();
					}
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
					 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
					 */
					importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
					importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
					importBuilder.setThreadCount(6);//设置批量导入线程池工作线程数量
					importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
					importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回

					importBuilder.setExportResultHandler(new ExportResultHandler<String, String>() {
						@Override
						public void success(TaskCommand<String, String> taskCommand, String result) {
							TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
							logger.info(taskMetrics.toString());
							logger.info(result);
						}

						@Override
						public void error(TaskCommand<String, String> taskCommand, String result) {
							TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
							logger.info(taskMetrics.toString());
							logger.info(result);
						}

						@Override
						public void exception(TaskCommand<String, String> taskCommand, Exception exception) {
							TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
							logger.info(taskMetrics.toString());
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
					db2ESImportBuilder = importBuilder;
					this.dataStream = dataStream;
					return "db2ESImport job started.";
				}
				else{
					return "db2ESImport job has started.";
				}
			}
		}
		else{
			return "db2ESImport job has started.";
		}

	}
	private ImportBuilder hBaseExportBuilder;
	private DataStream hbase2esDataStream;
	public String stopHBase2ESJob() {
		if(hbase2esDataStream != null) {
			synchronized (this) {
				if (hbase2esDataStream != null) {
					hbase2esDataStream.destroy(true);
					hbase2esDataStream = null;
					hBaseExportBuilder = null;
					return "HBase2ES job stopped.";
				} else {
					return "HBase2ES job has stopped.";
				}
			}
		}
		else {
			return "HBase2ES job has stopped.";
		}
	}

	public String scheduleHBase2ESJob() {
		if (hBaseExportBuilder == null) {
			synchronized (this) {
				if (hBaseExportBuilder == null) {
					ImportBuilder importBuilder = new ImportBuilder();
					importBuilder.setBatchSize(1000) //设置批量写入目标Elasticsearch记录数
							.setFetchSize(10000); //设置批量从源Hbase中拉取的记录数,HBase-0.98 默认值为为 100，HBase-1.2 默认值为 2147483647，即 Integer.MAX_VALUE。Scan.next() 的一次 RPC 请求 fetch 的记录条数。配置建议：这个参数与下面的setMaxResultSize配合使用，在网络状况良好的情况下，自定义设置不宜太小， 可以直接采用默认值，不配置。

//		importBuilder.setHbaseBatch(100) //配置获取的列数，假如表有两个列簇 cf，info，每个列簇5个列。这样每行可能有10列了，setBatch() 可以控制每次获取的最大列数，进一步从列级别控制流量。配置建议：当列数很多，数据量大时考虑配置此参数，例如100列每次只获取50列。一般情况可以默认值（-1 不受限）
//				.setMaxResultSize(10000l);//客户端缓存的最大字节数，HBase-0.98 无该项配置，HBase-1.2 默认值为 210241024，即 2M。Scan.next() 的一次 RPC 请求 fetch 的数据量大小，目前 HBase-1.2 在 Caching 为默认值(Integer Max)的时候，实际使用这个参数控制 RPC 次数和流量。配置建议：如果网络状况较好（万兆网卡），scan 的数据量非常大，可以将这个值配置高一点。如果配置过高：则可能 loadCache 速度比较慢，导致 scan timeout 异常
					// 参考文档：https://blog.csdn.net/kangkangwanwan/article/details/89332536


					/**
					 * hbase参数配置
					 */
					HBaseInputConfig hBaseInputConfig = new HBaseInputConfig();
//					hBaseInputConfig.addHbaseClientProperty("hbase.zookeeper.quorum","192.168.137.133")  //hbase客户端连接参数设置，参数含义参考hbase官方客户端文档
//							.addHbaseClientProperty("hbase.zookeeper.property.clientPort","2183")
					hBaseInputConfig.addHbaseClientProperty("hbase.zookeeper.quorum","10.13.11.12")  //hbase客户端连接参数设置，参数含义参考hbase官方客户端文档
							.addHbaseClientProperty("hbase.zookeeper.property.clientPort","2185")
							.addHbaseClientProperty("zookeeper.znode.parent","/hbase")
							.addHbaseClientProperty("hbase.ipc.client.tcpnodelay","true")
							.addHbaseClientProperty("hbase.rpc.timeout","1000000")
							.addHbaseClientProperty("hbase.client.operation.timeout","1000000")
							.addHbaseClientProperty("hbase.ipc.client.socket.timeout.read","2000000")
							.addHbaseClientProperty("hbase.ipc.client.socket.timeout.write","3000000")

							.setHbaseClientThreadCount(100)  //hbase客户端连接线程池参数设置
							.setHbaseClientThreadQueue(100)
							.setHbaseClientKeepAliveTime(10000l)
							.setHbaseClientBlockedWaitTimeout(10000l)
							.setHbaseClientWarnMultsRejects(1000)
							.setHbaseClientPreStartAllCoreThreads(true)
							.setHbaseClientThreadDaemon(true)

							.setHbaseTable("AgentInfo") //指定需要同步数据的hbase表名称
					;
					//FilterList和filter二选一，只需要设置一种
//		/**
//		 * 设置hbase检索filter
//		 */
//		SingleColumnValueFilter scvf= new SingleColumnValueFilter(Bytes.toBytes("Info"), Bytes.toBytes("i"),
//
//				CompareOperator.EQUAL,"wap".getBytes());
//
//		scvf.setFilterIfMissing(true); //默认为false， 没有此列的数据也会返回 ，为true则只返回name=lisi的数据
//
//		hBaseInputConfig.setFilter(scvf);

					/**
					 * 设置hbase组合条件FilterList
					 * FilterList 代表一个过滤器链，它可以包含一组即将应用于目标数据集的过滤器，过滤器间具有“与” FilterList.Operator.MUST_PASS_ALL 和“或” FilterList.Operator.MUST_PASS_ONE 关系
					 */

//		FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ONE); //数据只要满足一组过滤器中的一个就可以
//
//		SingleColumnValueFilter filter1 = new SingleColumnValueFilter(Bytes.toBytes("Info"), Bytes.toBytes("i"),
//
//				CompareOperator.EQUAL,"wap".getBytes());
//
//		list.addFilter(filter1);
//
//		SingleColumnValueFilter filter2 = new SingleColumnValueFilter(Bytes.toBytes("Info"), Bytes.toBytes("i"),
//
//				CompareOperator.EQUAL,Bytes.toBytes("my other value"));
//
//		list.addFilter(filter2);
//		hBaseInputConfig.setFilterList(list);

//		//设置同步起始行和终止行key条件
//		hBaseInputConfig.setStartRow(startRow);
//		hBaseInputConfig.setEndRow(endRow);
					//设置记录起始时间搓（>=）和截止时间搓(<),如果是基于时间范围的增量同步，则不需要指定下面两个参数
//		hBaseInputConfig.setStartTimestamp(startTimestam);
//		hBaseInputConfig.setEndTimestamp(endTimestamp);
					importBuilder.setInputConfig(hBaseInputConfig);
					/**
					 * es相关配置
					 * 可以通过addElasticsearchProperty方法添加Elasticsearch客户端配置，
					 * 也可以直接读取application.properties文件中设置的es配置,两种方式都可以，案例中采用application.properties的方式
					 */
					ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
//		elasticsearchOutputConfig.addElasticsearchProperty("elasticsearch.rest.hostNames","192.168.137.1:9200");//设置es服务器地址，更多配置参数文档：https://esdoc.bbossgroups.com/#/mongodb-elasticsearch?id=_5242-elasticsearch%e5%8f%82%e6%95%b0%e9%85%8d%e7%bd%ae
					elasticsearchOutputConfig.setTargetElasticsearch("default");//设置目标Elasticsearch集群数据源名称，和源elasticsearch集群一样都在application.properties文件中配置

					elasticsearchOutputConfig.setIndex("hbase233esdemo") ;//全局设置要目标elasticsearch索引名称
//							.setIndexType("hbase233esdemo"); //全局设值目标elasticsearch索引类型名称，如果是Elasticsearch 7以后的版本不需要配置
// 设置Elasticsearch索引文档_id
					/**
					 * 如果指定rowkey为文档_id,那么需要指定前缀meta:，如果是其他数据字段就不需要
					 * 例如：
					 * meta:rowkey 行key byte[]
					 * meta:timestamp  记录时间戳
					 */
					elasticsearchOutputConfig.setEsIdField("meta:rowkey");
					// 设置自定义id生成机制
					//如果指定EsIdGenerator，则根据下面的方法生成文档id，
					// 否则根据setEsIdField方法设置的字段值作为文档id，
					// 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id
//		elasticsearchOutputConfig.setEsIdGenerator(new EsIdGenerator(){
//
//			@Override
//			public Object genId(Context context) throws Exception {
//					Object id = context.getMetaValue("rowkey");
//					String agentId = BytesUtils.safeTrim(BytesUtils.toString((byte[]) id, 0, PinpointConstants.AGENT_NAME_MAX_LEN));
//					return agentId;
//			}
//		});
				  importBuilder.setOutputConfig(elasticsearchOutputConfig);


					//定时任务配置，
					importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
							.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
							.setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
					//定时任务配置结束

					//hbase表中列名，由"列族:列名"组成
//		//设置任务执行拦截器结束，可以添加多个
//		//增量配置开始
////		importBuilder.setLastValueColumn("Info:id");//指定数字增量查询字段变量名称
					importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
					importBuilder.setStatusDbname("hbase233esdemo");
					importBuilder.setLastValueStorePath("hbase233esdemo_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
					//指定增量字段类型为日期类型，如果没有指定增量字段名称,则按照hbase记录时间戳进行timerange增量检索
					importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);
					// ImportIncreamentConfig.NUMBER_TYPE 数字类型
//		// ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
					//设置增量查询的起始值时间起始时间
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					try {

						Date date = format.parse("2000-01-01");
						importBuilder.setLastValue(date);
					}
					catch (Exception e){
						e.printStackTrace();
					}
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
//					importBuilder.addFieldValue("author","作者");

					/**
					 * 设置es数据结构
					 */
					importBuilder.setDataRefactor(new DataRefactor() {
						public void refactor(Context context) throws Exception  {
							//可以根据条件定义是否丢弃当前记录
							//context.setDrop(true);return;
//				if(s.incrementAndGet() % 2 == 0) {
//					context.setDrop(true);
//					return;
//				}
							//获取原始的hbase记录Result对象
//				Result result = (Result)  context.getRecord();

							// 直接获取行key，对应byte[]类型，自行提取和分析保存在其中的数据
							String agentId = Bytes.toString((byte[])context.getMetaValue("rowkey"));
							context.addFieldValue("agentId",agentId);
							Date startTime = (Date)context.getMetaValue("timestamp");
							context.addFieldValue("startTime",startTime);
							// 通过context.getValue方法获取hbase 列的原始值byte[],方法参数对应hbase表中列名，由"列族:列名"组成
							String serializedAgentInfo =  context.getStringValue("Info:i");
							String serializedServerMetaData =  context.getStringValue("Info:m");
							String serializedJvmInfo =  context.getStringValue("Info:j");

							context.addFieldValue("serializedAgentInfo",serializedAgentInfo);
							context.addFieldValue("serializedServerMetaData",serializedServerMetaData);
							context.addFieldValue("serializedJvmInfo",serializedJvmInfo);
							context.addFieldValue("subtitle","小康");
							context.addFieldValue("collectTime",new Date());


//				/**
//				 * 获取ip对应的运营商和区域信息
//				 */
//				IpInfo ipInfo = context.getIpInfo("Info:agentIp");
//				if(ipInfo != null)
//					context.addFieldValue("ipinfo", SimpleStringUtil.object2json(ipInfo));
//				else{
//					context.addFieldValue("ipinfo", "");
//				}
//				DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
//				Date optime = context.getDateValue("logOpertime",dateFormat);
//				context.addFieldValue("logOpertime",optime);
//				context.addFieldValue("collecttime",new Date());

						}
					});
					//映射和转换配置结束

					/**
					 * 作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能
					 */
					importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
					importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
					importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
					importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
					importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
					importBuilder.setPrintTaskLog(true); //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false

					/**
					 * 设置任务执行情况回调接口
					 */
					importBuilder.setExportResultHandler(new ExportResultHandler<String,String>() {
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
					/**
					 * 执行es数据导入数据库表操作
					 */
					DataStream dataStream = importBuilder.builder();
					dataStream.execute();//执行导入操作
					hBaseExportBuilder = importBuilder;
					this.hbase2esDataStream = dataStream;
					return "HBase2ES job started.";
				}
				else{
					return "HBase2ES job has started.";
				}
			}
		}
		else{
			return "HBase2ES job has started.";
		}
	}
}
```

## 3.2 增加作业执行和停止控制器

[DataTranController](https://github.com/bbossgroups/springboot-elasticsearch-webservice/blob/master/src/main/java/com/example/esbboss/controller/DataTranController.java)

```java
package com.example.esbboss.controller;

import com.example.esbboss.service.DataTran;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataTranController {
	@Autowired
	private DataTran dataTran;

	/**
	 * 启动作业
	 * @return
	 */
	@RequestMapping("/scheduleDB2ESJob")
	public @ResponseBody
	String scheduleDB2ESJob(){
		return dataTran.scheduleDB2ESJob();
	}

	/**
	 * 停止作业
	 * @return
	 */
	@RequestMapping("/stopDB2ESJob")
	public @ResponseBody String stopDB2ESJob(){
		return dataTran.stopDB2ESJob();
	}
}
```
# 4 作业服务构建和运行
## 4.1 修改配置
修改配置文件中数据库信息
src/main/java/com/example/esbboss/service/DataTran.java
```java
 //数据源相关配置，可选项，可以在外部启动数据源
 					DBInputConfig dbInputConfig = new DBInputConfig();
                    dbInputConfig.setDbName("test");//这里只需要指定dbname，具体的数据源配置在application.properties文件中指定

                    //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
                    // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
                    // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
                    // log_id和数据库对应的字段一致,就不需要设置setLastValueColumn信息，
                    // 但是需要设置setLastValueType告诉工具增量字段的类型

                    dbInputConfig.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
                    importBuilder.setInputConfig(dbInputConfig);
```

修改src/main/resources/application.properties中的elasticsearch地址：

```properties
spring.elasticsearch.bboss.elasticsearch.rest.hostNames=192.168.137.1:9200
```

然后参考以下步骤构建和运行、停止作业。
## 4.2 构建和启动spring boot web服务

Firstbuild spring boot web，then run elasticsearch and run the demo:

```java
mvn clean install
cd target
java -jar springboot-elasticsearch-webservice-0.0.1-SNAPSHOT.jar

```

## 4.4 run the db-elasticsearch data tran job

Enter the following address in the browser to run the db-elasticsearch data tran job:

http://localhost:808/scheduleDB2ESJob

Return the following results in the browser to show successful execution:

作业启动成功
```json
db2ESImport job started.
```

作业已经启动
```json
db2ESImport job has started.
```
## 4.5 stop the db-elasticsearch data tran job
Enter the following address in the browser to stop the db-elasticsearch data tran job:

http://localhost:808/stopDB2ESJob

Return the following search results in the browser to show successful execution:
作业停止成功
```json
db2ESImport job stopped.
```
作业已经停止
```json
db2ESImport job has stopped.
```

# 5.参考文档

[数据库和Elasticsearch同步工具](https://esdoc.bbossgroups.com/#/db-es-tool)

[spring boot db-db数据同步案例工程](https://github.com/bbossgroups/db-db-job)