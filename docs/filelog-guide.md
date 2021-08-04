# 日志采集插件使用指南

基于java语言的日志文件采集插件，支持全量和增量采集两种模式，实时采集日志文件数据到kafka/elasticsearch/database，使用案例：

   1. [采集日志数据并写入数据库](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2DBDemo.java)
   2. [采集日志数据并写入Elasticsearch](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2ESDemo.java)  
   3. [采集日志数据并发送到Kafka](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaDemo.java)

![](images\datasyn.png)

直接通过java开发数据采集作业的业界也就只有bboss能够做到，下面具体介绍kafka/elasticsearch/database三个案例。

# 1.日志采集插件属性说明

importBuilder（FileLog2ESImportBuilder/FileLog2DBImportBuilder/FileLog2KafkaImportBuilder)用于采集作业基础属性配置

FileImportConfig用于指定日志采集插件全局配置

FileConfig用于指定文件级别配置

| 属性名称                                 | 类型                                                         | 默认值  |
| ---------------------------------------- | ------------------------------------------------------------ | ------- |
| importBuilder.flushInterval              | long 设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制 | 8秒     |
| importBuilder.fetchSize                  | int,设置按批读取文件行数                                     |         |
| importBuilder.batchSize                  | int ，设置批量入库的记录数                                   |         |
| FileImportConfig.jsondata                | 布尔类型，标识文本记录是json格式的数据，true 将值解析为json对象，false - 不解析，这样值将作为一个完整的message字段存放到上报数据中 |         |
| FileImportConfig.rootLevel               | jsondata = true时，自定义的数据是否和采集的数据平级，true则直接在原先的json串中存放数据 false则定义一个json存放数据，若不是json则是message |         |
| FileImportConfig.interval                | long 单位：毫秒，扫描新文件时间间隔                          | 5000L   |
| FileImportConfig.registLiveTime          | Long ,已完成文件增量记录保留时间，超过指定的时间后将会迁入历史表中，为null时不处理 | null    |
| FileImportConfig.checkFileModifyInterval | long，扫描文件内容改变时间间隔                               | 3000L   |
| FileImportConfig.charsetEncode           | String,日志内容字符集                                        | UTF-8   |
| FileImportConfig.enableMeta              | boolean，是否将日志文件信息补充到日志记录中，                | true    |
| FileConfig.enableInode                   | boolean,是否启用inode文件标识符机制来识别文件重命名操作，linux环境下起作用，windows环境下不起作用（enableInode强制为false）  linux环境下，在不存在重命名的场景下可以关闭inode文件标识符机制，windows环境下强制关闭inode文件标识符机制 | true    |
| FileConfig.sourcePath                    | String,日志文件目录                                          |         |
| FileConfig.fileNameRegular               | String,日志文件名称正则表达式                                |         |
| FileConfig.addField                      | 方法，为对应的日志文件记录添加字段和值，例如：FileConfig.addField("tag","elasticsearch")//添加字段tag到记录中，其他记录级别或全局添加字段，可以参考文档[5.2.4.5 数据加工处理](https://esdoc.bbossgroups.com/#/mongodb-elasticsearch?id=_5245-数据加工处理) |         |
| FileConfig.fileHeadLineRegular           | 行记录开头标识正则表达式，用来区分一条日志包含多行的情况，行记录以什么开头,正则匹配，不指定时，不区分多行记录，例如：^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\] |         |
| FileConfig.includeLines                  | String[],需包含记录的内容表达式数组，需要包含的记录条件,正则匹配 |         |
| FileConfig.excludeLines                  | String[],需排除记录的内容表达式数组，If both include_lines and exclude_lines are defined, bboss executes include_lines first and then executes exclude_lines. The order in which the two options are defined doesn’t matter.  The include_lines option will always be executed before the exclude_lines option,  even if exclude_lines appears before include_lines in the config file. |         |
| FileConfig.includeLineMatchType          | 文件记录包含条件匹配类型  REGEX_MATCH("REGEX_MATCH"),REGEX_CONTAIN("REGEX_CONTAIN"),STRING_CONTAIN("STRING_CONTAIN"), STRING_EQUALS("STRING_EQUALS"),STRING_PREFIX("STRING_PREFIX"),STRING_END("STRING_END");  默认值REGEX_CONTAIN，使用案例：config.addConfig(new FileConfig(logPath,//指定目录    fileName+".log",//指定文件名称，可以是正则表达式   startLabel)//指定多行记录的开头识别标记，正则表达式    .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化    .addField("tag",fileName.toLowerCase())//添加字段tag到记录中    .setEnableInode(true)    .setIncludeLines(levelArr, LineMatchType.STRING_CONTAIN)``` |         |
| FileConfig.excludeLineMatchType          | 文件记录排除条件匹配类型  REGEX_MATCH("REGEX_MATCH"),REGEX_CONTAIN("REGEX_CONTAIN"),STRING_CONTAIN("STRING_CONTAIN"), STRING_EQUALS("STRING_EQUALS"),STRING_PREFIX("STRING_PREFIX"),STRING_END("STRING_END");  默认值REGEX_CONTAIN，使用案例：```config.addConfig(new FileConfig(logPath,//指定目录    fileName+".log",//指定文件名称，可以是正则表达式    startLabel)//指定多行记录的开头识别标记，正则表达式    .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化    .addField("tag",fileName.toLowerCase())//添加字段tag到记录中    .setEnableInode(true)    .setExcludeLines(levelArr, LineMatchType.STRING_CONTAIN)``` |         |
| FileConfig.maxBytes                      | 字符串maxBytes为0或者负数时忽略长度截取，The maximum number of bytes that a single log message can have. All bytes after max_bytes are discarded and not sent. * This setting is especially useful for multiline log messages, which can get large. The default is 1MB (1048576) | 1048576 |
| FileConfig.startPointer                  | long ,指定采集的日志文件内容开始位置                         | 0       |
| FileConfig.ignoreOlderTime               | Long类型，If this option is enabled, bboss ignores any files that were modified before the specified timespan. Configuring ignore_older can be especially useful if you keep log files for a long time. For example, if you want to start bboss, but only want to send the newest files and files from last week, you can configure this option. You can use time strings like 2h (2 hours) and 5m (5 minutes). The default is null,  which disables the setting. Commenting out the config has the same effect as setting it to null.如果为null忽略该机制 | null    |
| FileConfig.closeEOF                      | 布尔类型，true 采集到日志文件末尾后关闭本文件采集通道，后续不再采集； false不关闭，适用于文件只采集一次的场景 | false   |
| FileConfig.deleteEOFFile                 | 布尔类型，如果deleteEOFFile为true,则删除采集完数据的文件，适用于文件只采集一次的场景 | false   |
| FileConfig.closeOlderTime                | Long类型，启用日志文件采集探针closeOlderTime配置，允许文件内容静默最大时间，单位毫秒，如果在idleMaxTime访问内一直没有数据更新，认为文件是静默文件，将不再采集静默文件数据，关闭文件对应的采集线程，作业重启后也不会采集，0或者null不起作用 | null    |

enableMeta控制的信息如下

```
/**
		 * 启用元数据信息到记录中，元数据信息以map结构方式作为@filemeta字段值添加到记录中，文件插件支持的元信息字段如下：
		 * hostIp：主机ip
		 * hostName：主机名称
		 * filePath： 文件路径
		 * timestamp：采集的时间戳
		 * pointer：记录对应的截止文件指针,long类型
		 * fileId：linux文件号，windows系统对应文件路径
		 * 例如：
		 * {
		 *   "_index": "filelog",
		 *   "_type": "_doc",
		 *   "_id": "HKErgXgBivowv_nD0Jhn",
		 *   "_version": 1,
		 *   "_score": null,
		 *   "_source": {
		 *     "title": "解放",
		 *     "subtitle": "小康",
		 *     "ipinfo": "",
		 *     "newcollecttime": "2021-03-30T03:27:04.546Z",
		 *     "author": "张无忌",
		 *     "@filemeta": {
		 *       "path": "D:\\ecslog\\error-2021-03-27-1.log",
		 *       "hostname": "",
		 *       "pointer": 3342583,
		 *       "hostip": "",
		 *       "timestamp": 1617074824542,
		 *       "fileId": "D:/ecslog/error-2021-03-27-1.log"
		 *     },
		 *     "message": "[18:04:40:161] [INFO] - org.frameworkset.tran.schedule.ScheduleService.externalTimeSchedule(ScheduleService.java:192) - Execute schedule job Take 3 ms"
		 *   }
		 * }
		 *
		 * true 开启 false 关闭
		 */
```



# 2.采集日志数据并写入Elasticsearch

可以从以下地址下载“日志数据采集并写入Elasticsearch作业”开发工程环境（基于gradle）

https://github.com/bbossgroups/filelog-elasticsearch

https://gitee.com/bboss/filelog-elasticsearch

基于组件org.frameworkset.tran.output.es.FileLog2ESImportBuilder实现日志数据采集并写入Elasticsearch作业

```java
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.input.file.FileConfig;
import org.frameworkset.tran.input.file.FileImportConfig;
import org.frameworkset.tran.output.es.FileLog2ESImportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <p>Description: 从日志文件采集日志数据并保存到Elasticsearch</p>

 */
public class FileLog2ESDemo {
	private static Logger logger = LoggerFactory.getLogger(FileLog2ESDemo.class);
	public static void main(String[] args){


		FileLog2ESImportBuilder importBuilder = new FileLog2ESImportBuilder();
		importBuilder.setBatchSize(500)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
		importBuilder.setFlushInterval(10000l);

		FileImportConfig config = new FileImportConfig();
		//.*.txt.[0-9]+$
		//[17:21:32:388]
//		config.addConfig(new FileConfig("D:\\ecslog",//指定目录
//				"error-2021-03-27-1.log",//指定文件名称，可以是正则表达式
//				"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//				.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
//				.setMaxBytes(1048576)//控制每条日志的最大长度，超过长度将被截取掉
//				//.setStartPointer(1000l)//设置采集的起始位置，日志内容偏移量
//				.addField("tag","error") //添加字段tag到记录中
//				.setExcludeLines(new String[]{"\\[DEBUG\\]"}));//不采集debug日志

		config.addConfig(new FileConfig("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\",//指定目录
				"es.log",//指定文件名称，可以是正则表达式
				"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
				.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
				.addField("tag","elasticsearch")//添加字段tag到记录中
				.setEnableInode(false)
				//.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
		);

		/**
		 * 启用元数据信息到记录中，元数据信息以map结构方式作为@filemeta字段值添加到记录中，文件插件支持的元信息字段如下：
		 * hostIp：主机ip
		 * hostName：主机名称
		 * filePath： 文件路径
		 * timestamp：采集的时间戳
		 * pointer：记录对应的截止文件指针,long类型
		 * fileId：linux文件号，windows系统对应文件路径
		 * 例如：
		 * {
		 *   "_index": "filelog",
		 *   "_type": "_doc",
		 *   "_id": "HKErgXgBivowv_nD0Jhn",
		 *   "_version": 1,
		 *   "_score": null,
		 *   "_source": {
		 *     "title": "解放",
		 *     "subtitle": "小康",
		 *     "ipinfo": "",
		 *     "newcollecttime": "2021-03-30T03:27:04.546Z",
		 *     "author": "张无忌",
		 *     "@filemeta": {
		 *       "path": "D:\\ecslog\\error-2021-03-27-1.log",
		 *       "hostname": "",
		 *       "pointer": 3342583,
		 *       "hostip": "",
		 *       "timestamp": 1617074824542,
		 *       "fileId": "D:/ecslog/error-2021-03-27-1.log"
		 *     },
		 *     "message": "[18:04:40:161] [INFO] - org.frameworkset.tran.schedule.ScheduleService.externalTimeSchedule(ScheduleService.java:192) - Execute schedule job Take 3 ms"
		 *   }
		 * }
		 *
		 * true 开启 false 关闭
		 */
		config.setEnableMeta(true);
		importBuilder.setFileImportConfig(config);
		//指定elasticsearch数据源名称，在application.properties文件中配置，default为默认的es数据源名称
		importBuilder.setTargetElasticsearch("default");
		//指定索引名称，这里采用的是elasticsearch 7以上的版本进行测试，不需要指定type
		importBuilder.setIndex("filelog");
		//指定索引类型，这里采用的是elasticsearch 7以上的版本进行测试，不需要指定type
		//importBuilder.setIndexType("idxtype");

		//增量配置开始
		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
		importBuilder.setLastValueStorePath("filelog_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//增量配置结束

		//映射和转换配置开始

		importBuilder.addFieldValue("author","张无忌");

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
//				System.out.println(data);

//				context.addFieldValue("author","duoduo");//将会覆盖全局设置的author变量
				context.addFieldValue("title","解放");
				context.addFieldValue("subtitle","小康");
				
				//如果日志是普通的文本日志，非json格式，则可以自己根据规则对包含日志记录内容的message字段进行解析
				String message = context.getStringValue("@message");
				String[] fvs = message.split(" ");//空格解析字段
				/**
				 * //解析示意代码
				 * String[] fvs = message.split(" ");//空格解析字段
				 * //将解析后的信息添加到记录中
				 * context.addFieldValue("f1",fvs[0]);
				 * context.addFieldValue("f2",fvs[1]);
				 * context.addFieldValue("logVisitorial",fvs[2]);//包含ip信息
				 */
				//直接获取文件元信息
				Map fileMata = (Map)context.getValue("@filemeta");
				/**
				 * 文件插件支持的元信息字段如下：
				 * hostIp：主机ip
				 * hostName：主机名称
				 * filePath： 文件路径
				 * timestamp：采集的时间戳
				 * pointer：记录对应的截止文件指针,long类型
				 * fileId：linux文件号，windows系统对应文件路径
				 */
				String filePath = (String)context.getMetaValue("filePath");
				//可以根据文件路径信息设置不同的索引
				if(filePath.endsWith("error-2021-03-27-1.log")) {
					context.setIndex("errorlog");
				}
				else if(filePath.endsWith("es.log")){
					 context.setIndex("eslog");
				}


//				context.addIgnoreFieldMapping("title");
				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//				context.addIgnoreFieldMapping("author");

//				//修改字段名称title为新名称newTitle，并且修改字段的值
//				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
				/**
				 * 获取ip对应的运营商和区域信息
				 */
				/**
				IpInfo ipInfo = (IpInfo) context.getIpInfo(fvs[2]);
				if(ipInfo != null)
					context.addFieldValue("ipinfo", ipInfo);
				else{
					context.addFieldValue("ipinfo", "");
				}*/
				DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
//				Date optime = context.getDateValue("LOG_OPERTIME",dateFormat);
//				context.addFieldValue("logOpertime",optime);
				context.addFieldValue("newcollecttime",new Date());

				
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
		importBuilder.setPrintTaskLog(true);

		/**
		 * 启动日志数据采集并写入Elasticsearch作业
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//启动同步作业
		logger.info("job started.");
	}
}
```

在工程中调试好作业后，修改application.properties文件中的mainclass配置，将作业类调整为新开发的作业程序FileLog2ESDemo

```properties
mainclass=org.frameworkset.elasticsearch.imp.FileLog2ESDemo
```
亦可以修改application.properties中的Elasticsearch配置：

```properties
elasticUser=elastic
elasticPassword=changeme

#elasticsearch.rest.hostNames=10.1.236.88:9200
#elasticsearch.rest.hostNames=127.0.0.1:9200
#elasticsearch.rest.hostNames=10.21.20.168:9200
elasticsearch.rest.hostNames=192.168.137.1:9200
```

构建发布可运行的作业部署包：进入命令行模式，在源码工程根目录filelog-elasticsearch下运行以下gradle指令打包发布版本

```
release.bat
```

更多作业配置和运行资料参考：[帮助文档](https://gitee.com/bboss/filelog-elasticsearch/blob/main/README.md)

# 3.采集日志数据并写入数据库

可以从以下地址下载“日志数据采集并写入数据库作业”开发工程环境（基于gradle）

https://github.com/bbossgroups/filelog-elasticsearch

https://gitee.com/bboss/filelog-elasticsearch

基于组件org.frameworkset.tran.output.db.FileLog2DBImportBuilder实现日志数据采集并写入数据库作业

```java

import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.db.DBConfigBuilder;
import org.frameworkset.tran.input.file.FileConfig;
import org.frameworkset.tran.input.file.FileImportConfig;
import org.frameworkset.tran.output.db.FileLog2DBImportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * <p>Description: 从日志文件采集日志数据并保存到数据库</p>
 */
public class FileLog2DBDemo {
	private static Logger logger = LoggerFactory.getLogger(FileLog2DBDemo.class);
	public static void main(String[] args){


		FileLog2DBImportBuilder importBuilder = new FileLog2DBImportBuilder();
		importBuilder.setBatchSize(500)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
		importBuilder.setFlushInterval(10000l);

		FileImportConfig config = new FileImportConfig();
		//.*.txt.[0-9]+$
		//[17:21:32:388]
//		config.addConfig(new FileConfig("D:\\ecslog",//指定目录
//				"error-2021-03-27-1.log",//指定文件名称，可以是正则表达式
//				"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//				.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
////				.setMaxBytes(1048576)//控制每条日志的最大长度，超过长度将被截取掉
//				//.setStartPointer(1000l)//设置采集的起始位置，日志内容偏移量
//				.addField("tag","error") //添加字段tag到记录中
//				.setExcludeLines(new String[]{"\\[DEBUG\\]"}));//不采集debug日志

		config.addConfig(new FileConfig("D:\\ecslog",//指定目录
				"es.log",//指定文件名称，可以是正则表达式
				"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
				.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
				.addField("tag","elasticsearch")//添加字段tag到记录中
				/**
				 * 是否启用inode文件标识符机制来识别文件重命名操作，linux环境下起作用，windows环境下不起作用（enableInode强制为false）
				 * linux环境下，在不存在重命名的场景下可以关闭inode文件标识符机制，windows环境下强制关闭inode文件标识符机制
				 */
				.setEnableInode(false)
				.setExcludeLines(new String[]{".*endpoint.*"}));//采集不包含endpoint的日志

//		config.addConfig("E:\\ELK\\data\\data3",".*.txt","^[0-9]{4}-[0-9]{2}-[0-9]{2}");
		/**
		 * 启用元数据信息到记录中，元数据信息以map结构方式作为@filemeta字段值添加到记录中，文件插件支持的元信息字段如下：
		 * hostIp：主机ip
		 * hostName：主机名称
		 * filePath： 文件路径
		 * timestamp：采集的时间戳
		 * pointer：记录对应的截止文件指针,long类型
		 * fileId：linux文件号，windows系统对应文件路径
		 * 例如：
		 * {
		 *   "_index": "filelog",
		 *   "_type": "_doc",
		 *   "_id": "HKErgXgBivowv_nD0Jhn",
		 *   "_version": 1,
		 *   "_score": null,
		 *   "_source": {
		 *     "title": "解放",
		 *     "subtitle": "小康",
		 *     "ipinfo": "",
		 *     "newcollecttime": "2021-03-30T03:27:04.546Z",
		 *     "author": "张无忌",
		 *     "@filemeta": {
		 *       "path": "D:\\ecslog\\error-2021-03-27-1.log",
		 *       "hostname": "",
		 *       "pointer": 3342583,
		 *       "hostip": "",
		 *       "timestamp": 1617074824542,
		 *       "fileId": "D:/ecslog/error-2021-03-27-1.log"
		 *     },
		 *     "@message": "[18:04:40:161] [INFO] - org.frameworkset.tran.schedule.ScheduleService.externalTimeSchedule(ScheduleService.java:192) - Execute schedule job Take 3 ms"
		 *   }
		 * }
		 *
		 * true 开启 false 关闭
		 */
		config.setEnableMeta(true);
		importBuilder.setFileImportConfig(config);
		//指定elasticsearch数据源名称，在application.properties文件中配置，default为默认的es数据源名称

//导出到数据源配置
		DBConfigBuilder dbConfigBuilder = new DBConfigBuilder();
		dbConfigBuilder
				.setSqlFilepath("sql-dbtran.xml")

				.setTargetDbName("test")//指定目标数据库，在application.properties文件中配置
//				.setTargetDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
//				.setTargetDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
//				.setTargetDbUser("root")
//				.setTargetDbPassword("123456")
//				.setTargetValidateSQL("select 1")
//				.setTargetUsePool(true)//是否使用连接池
				.setInsertSqlName("insertSql")//指定新增的sql语句名称，在配置文件中配置：sql-dbtran.xml

				/**
				 * 是否在批处理时，将insert、update、delete记录分组排序
				 * true：分组排序，先执行insert、在执行update、最后执行delete操作
				 * false：按照原始顺序执行db操作，默认值false
				 * @param optimize
				 * @return
				 */
				.setOptimize(true);//指定查询源库的sql语句，在配置文件中配置：sql-dbtran.xml
		importBuilder.setOutputDBConfig(dbConfigBuilder.buildDBImportConfig());
		//增量配置开始
		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
		importBuilder.setLastValueStorePath("filelogdb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//增量配置结束

		//映射和转换配置开始
//		/**
//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
//		 */

		importBuilder.addFieldMapping("message","@message");
//


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
//				System.out.println(data);

//				context.addFieldValue("author","duoduo");//将会覆盖全局设置的author变量
				context.addFieldValue("author","duoduo");
				context.addFieldValue("title","解放");
				context.addFieldValue("subtitle","小康");

				context.addFieldValue("collecttime",new Date());

				
//				//如果日志是普通的文本日志，非json格式，则可以自己根据规则对包含日志记录内容的message字段进行解析
//				String message = context.getStringValue("@message");
//				String[] fvs = message.split(" ");//空格解析字段
				/**
				 * //解析示意代码
				 * String[] fvs = message.split(" ");//空格解析字段
				 * //将解析后的信息添加到记录中
				 * context.addFieldValue("f1",fvs[0]);
				 * context.addFieldValue("f2",fvs[1]);
				 * context.addFieldValue("logVisitorial",fvs[2]);//包含ip信息
				 */
				//直接获取文件元信息
//				Map fileMata = (Map)context.getValue("@filemeta");
				/**
				 * 文件插件支持的元信息字段如下：
				 * hostIp：主机ip
				 * hostName：主机名称
				 * filePath： 文件路径
				 * timestamp：采集的时间戳
				 * pointer：记录对应的截止文件指针,long类型
				 * fileId：linux文件号，windows系统对应文件路径
				 */
				String filePath = (String)context.getMetaValue("filePath");
				String hostIp = (String)context.getMetaValue("hostIp");
				String hostName = (String)context.getMetaValue("hostName");
				String fileId = (String)context.getMetaValue("fileId");
				Date optime = (Date) context.getMetaValue("timestamp");
				long pointer = (long)context.getMetaValue("pointer");
				context.addFieldValue("optime",optime);
				context.addFieldValue("filePath",filePath);
				context.addFieldValue("hostIp",hostIp);
				context.addFieldValue("hostName",hostName);
				context.addFieldValue("fileId",fileId);
				context.addFieldValue("pointer",pointer);
				context.addIgnoreFieldMapping("@filemeta");

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
		importBuilder.setPrintTaskLog(true);

		/**
		 * 启动日志数据采集并写入Elasticsearch作业
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//启动同步作业
		logger.info("job started.");
	}
}
```

在工程中调试好作业后，修改application.properties文件中的mainclass配置，将作业类调整为新开发的作业程序FileLog2DBDemo

```properties
mainclass=org.frameworkset.elasticsearch.imp.FileLog2DBDemo
```

亦可以修改application.properties中的数据库配置：

```properties
# 演示数据库数据导入elasticsearch源配置
db.name = test
db.user = root
db.password = 123456
db.driver = com.mysql.jdbc.Driver
#db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
db.url = jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false
db.usePool = true

db.initSize=100
db.minIdleSize=100
db.maxSize=100


db.validateSQL = select 1
#db.jdbcFetchSize = 10000
db.jdbcFetchSize = -2147483648
db.showsql = true
#db.dbtype = mysql -2147483648
#db.dbAdaptor = org.frameworkset.elasticsearch.imp.TestMysqlAdaptor
```

构建发布可运行的作业部署包：进入命令行模式，在源码工程根目录filelog-elasticsearch下运行以下gradle指令打包发布版本

```
release.bat
```

更多作业配置和运行资料参考：[帮助文档](https://gitee.com/bboss/filelog-elasticsearch/blob/main/README.md)

# 4.采集日志数据并发送到kafka

可以从以下地址下载“日志数据采集并发送到kafka作业”开发工程环境（基于gradle）

https://github.com/bbossgroups/kafka2x-elasticsearch

https://gitee.com/bboss/kafka2x-elasticsearch

基于组件org.frameworkset.tran.kafka.output.filelog.FileLog2KafkaImportBuilder实现日志数据采集并发送到kafka作业

```java

import org.apache.kafka.clients.producer.RecordMetadata;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.tran.CommonRecord;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.input.file.FileConfig;
import org.frameworkset.tran.input.file.FileImportConfig;
import org.frameworkset.tran.kafka.output.KafkaOutputConfig;
import org.frameworkset.tran.kafka.output.filelog.FileLog2KafkaImportBuilder;
import org.frameworkset.tran.metrics.TaskMetrics;
import org.frameworkset.tran.task.TaskCommand;
import org.frameworkset.tran.util.ReocordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <p>Description: 采集日志文件数据并发送kafka作业，如需调试同步功能，直接运行main方法</p>
 */
public class Filelog2KafkaDemo {
	private static Logger logger = LoggerFactory.getLogger(Filelog2KafkaDemo.class);
	public static void main(String args[]){

		Filelog2KafkaDemo dbdemo = new Filelog2KafkaDemo();

		dbdemo.scheduleTimestampImportData();
	}



	/**
	 * elasticsearch地址和数据库地址都从外部配置文件application.properties中获取，加载数据源配置和es配置
	 */
	public void scheduleTimestampImportData(){
		FileLog2KafkaImportBuilder importBuilder = new FileLog2KafkaImportBuilder();
		importBuilder.setFetchSize(300);
		//kafka相关配置参数
		/**
		 *
		 <property name="productorPropes">
		 <propes>

		 <property name="value.serializer" value="org.apache.kafka.common.serialization.StringSerializer">
		 <description> <![CDATA[ 指定序列化处理类，默认为kafka.serializer.DefaultEncoder,即byte[] ]]></description>
		 </property>
		 <property name="key.serializer" value="org.apache.kafka.common.serialization.LongSerializer">
		 <description> <![CDATA[ 指定序列化处理类，默认为kafka.serializer.DefaultEncoder,即byte[] ]]></description>
		 </property>

		 <property name="compression.type" value="gzip">
		 <description> <![CDATA[ 是否压缩，默认0表示不压缩，1表示用gzip压缩，2表示用snappy压缩。压缩后消息中会有头来指明消息压缩类型，故在消费者端消息解压是透明的无需指定]]></description>
		 </property>
		 <property name="bootstrap.servers" value="192.168.137.133:9093">
		 <description> <![CDATA[ 指定kafka节点列表，用于获取metadata(元数据)，不必全部指定]]></description>
		 </property>
		 <property name="batch.size" value="10000">
		 <description> <![CDATA[ 批处理消息大小：
		 the producer will attempt to batch records together into fewer requests whenever multiple records are being sent to the same partition. This helps performance on both the client and the server. This configuration controls the default batch size in bytes.
		 No attempt will be made to batch records larger than this size.

		 Requests sent to brokers will contain multiple batches, one for each partition with data available to be sent.

		 A small batch size will make batching less common and may reduce throughput (a batch size of zero will disable batching entirely). A very large batch size may use memory a bit more wastefully as we will always allocate a buffer of the specified batch size in anticipation of additional records.
		 ]]></description>
		 </property>

		 <property name="linger.ms" value="10000">
		 <description> <![CDATA[
		 <p>
		 * The producer maintains buffers of unsent records for each partition. These buffers are of a size specified by
		 * the <code>batch.size</code> config. Making this larger can result in more batching, but requires more memory (since we will
		 * generally have one of these buffers for each active partition).
		 * <p>
		 * By default a buffer is available to send immediately even if there is additional unused space in the buffer. However if you
		 * want to reduce the number of requests you can set <code>linger.ms</code> to something greater than 0. This will
		 * instruct the producer to wait up to that number of milliseconds before sending a request in hope that more records will
		 * arrive to fill up the same batch. This is analogous to Nagle's algorithm in TCP. For example, in the code snippet above,
		 * likely all 100 records would be sent in a single request since we set our linger time to 1 millisecond. However this setting
		 * would add 1 millisecond of latency to our request waiting for more records to arrive if we didn't fill up the buffer. Note that
		 * records that arrive close together in time will generally batch together even with <code>linger.ms=0</code> so under heavy load
		 * batching will occur regardless of the linger configuration; however setting this to something larger than 0 can lead to fewer, more
		 * efficient requests when not under maximal load at the cost of a small amount of latency.
		 * <p>
		 * The <code>buffer.memory</code> controls the total amount of memory available to the producer for buffering. If records
		 * are sent faster than they can be transmitted to the server then this buffer space will be exhausted. When the buffer space is
		 * exhausted additional send calls will block. The threshold for time to block is determined by <code>max.block.ms</code> after which it throws
		 * a TimeoutException.
		 * <p>]]></description>
		 </property>
		 <property name="buffer.memory" value="10000">
		 <description> <![CDATA[ 批处理消息大小：
		 The <code>buffer.memory</code> controls the total amount of memory available to the producer for buffering. If records
		 * are sent faster than they can be transmitted to the server then this buffer space will be exhausted. When the buffer space is
		 * exhausted additional send calls will block. The threshold for time to block is determined by <code>max.block.ms</code> after which it throws
		 * a TimeoutException.]]></description>
		 </property>

		 </propes>
		 </property>
		 */

		// kafka服务器参数配置
		// kafka 2x 客户端参数项及说明类：org.apache.kafka.clients.consumer.ConsumerConfig
		KafkaOutputConfig kafkaOutputConfig = new KafkaOutputConfig();
		kafkaOutputConfig.setTopic("es2kafka");//设置kafka主题名称
		kafkaOutputConfig.addKafkaProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
		kafkaOutputConfig.addKafkaProperty("key.serializer","org.apache.kafka.common.serialization.LongSerializer");
		kafkaOutputConfig.addKafkaProperty("compression.type","gzip");
		kafkaOutputConfig.addKafkaProperty("bootstrap.servers","192.168.137.133:9092");
		kafkaOutputConfig.addKafkaProperty("batch.size","10");
//		kafkaOutputConfig.addKafkaProperty("linger.ms","10000");
//		kafkaOutputConfig.addKafkaProperty("buffer.memory","10000");
		kafkaOutputConfig.setKafkaAsynSend(true);
//指定文件中每条记录格式，不指定默认为json格式输出
		kafkaOutputConfig.setReocordGenerator(new ReocordGenerator() {
			@Override
			public void buildRecord(Context taskContext, CommonRecord record, Writer builder) {
				//record.setRecordKey("xxxxxx"); //指定记录key
				//直接将记录按照json格式输出到文本文件中
				SerialUtil.normalObject2json(record.getDatas(),//获取记录中的字段数据并转换为json格式
						builder);
//          System.out.println(data);

			}
		});
		importBuilder.setKafkaOutputConfig(kafkaOutputConfig);
		//定时任务配置结束

		FileImportConfig config = new FileImportConfig();
		//.*.txt.[0-9]+$
		//[17:21:32:388]
//		config.addConfig(new FileConfig("D:\\ecslog",//指定目录
//				"error-2021-03-27-1.log",//指定文件名称，可以是正则表达式
//				"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//				.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
////				.setMaxBytes(1048576)//控制每条日志的最大长度，超过长度将被截取掉
//				//.setStartPointer(1000l)//设置采集的起始位置，日志内容偏移量
//				.addField("tag","error") //添加字段tag到记录中
//				.setExcludeLines(new String[]{"\\[DEBUG\\]"}));//不采集debug日志

		config.addConfig(new FileConfig("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\",//指定目录
						"es.log",//指定文件名称，可以是正则表达式
						"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
						.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
						.addField("tag","elasticsearch")//添加字段tag到记录中
				//.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
		);
//		config.addConfig("E:\\ELK\\data\\data3",".*.txt","^[0-9]{4}-[0-9]{2}-[0-9]{2}");
		/**
		 * 启用元数据信息到记录中，元数据信息以map结构方式作为@filemeta字段值添加到记录中，文件插件支持的元信息字段如下：
		 * hostIp：主机ip
		 * hostName：主机名称
		 * filePath： 文件路径
		 * timestamp：采集的时间戳
		 * pointer：记录对应的截止文件指针,long类型
		 * fileId：linux文件号，windows系统对应文件路径
		 * 例如：
		 * {
		 *   "_index": "filelog",
		 *   "_type": "_doc",
		 *   "_id": "HKErgXgBivowv_nD0Jhn",
		 *   "_version": 1,
		 *   "_score": null,
		 *   "_source": {
		 *     "title": "解放",
		 *     "subtitle": "小康",
		 *     "ipinfo": "",
		 *     "newcollecttime": "2021-03-30T03:27:04.546Z",
		 *     "author": "张无忌",
		 *     "@filemeta": {
		 *       "path": "D:\\ecslog\\error-2021-03-27-1.log",
		 *       "hostname": "",
		 *       "pointer": 3342583,
		 *       "hostip": "",
		 *       "timestamp": 1617074824542,
		 *       "fileId": "D:/ecslog/error-2021-03-27-1.log"
		 *     },
		 *     "@message": "[18:04:40:161] [INFO] - org.frameworkset.tran.schedule.ScheduleService.externalTimeSchedule(ScheduleService.java:192) - Execute schedule job Take 3 ms"
		 *   }
		 * }
		 *
		 * true 开启 false 关闭
		 */
		config.setEnableMeta(true);
		importBuilder.setFileImportConfig(config);

		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
		importBuilder.setLastValueStorePath("filelog2kafka");//记录上次采集的增量字段值的文件路


		//设置ip地址信息库地址
		importBuilder.setGeoipDatabase("E:/workspace/hnai/terminal/geolite2/GeoLite2-City.mmdb");
		importBuilder.setGeoipAsnDatabase("E:/workspace/hnai/terminal/geolite2/GeoLite2-ASN.mmdb");
		importBuilder.setGeoip2regionDatabase("E:/workspace/hnai/terminal/geolite2/ip2region.db");

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

				context.addFieldValue("title","解放");
				context.addFieldValue("subtitle","小康");

				//如果日志是普通的文本日志，非json格式，则可以自己根据规则对包含日志记录内容的message字段进行解析
				String message = context.getStringValue("@message");
				String[] fvs = message.split(" ");//空格解析字段
				/**
				 * //解析示意代码
				 * String[] fvs = message.split(" ");//空格解析字段
				 * //将解析后的信息添加到记录中
				 * context.addFieldValue("f1",fvs[0]);
				 * context.addFieldValue("f2",fvs[1]);
				 * context.addFieldValue("logVisitorial",fvs[2]);//包含ip信息
				 */
				//直接获取文件元信息
				Map fileMata = (Map)context.getValue("@filemeta");
				/**
				 * 文件插件支持的元信息字段如下：
				 * hostIp：主机ip
				 * hostName：主机名称
				 * filePath： 文件路径
				 * timestamp：采集的时间戳
				 * pointer：记录对应的截止文件指针,long类型
				 * fileId：linux文件号，windows系统对应文件路径
				 */
				String filePath = (String)context.getMetaValue("filePath");



//				context.addIgnoreFieldMapping("title");
				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//				context.addIgnoreFieldMapping("author");

//				//修改字段名称title为新名称newTitle，并且修改字段的值
//				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
				/**
				 * 获取ip对应的运营商和区域信息
				 */
				/**
				 IpInfo ipInfo = (IpInfo) context.getIpInfo(fvs[2]);
				 if(ipInfo != null)
				 context.addFieldValue("ipinfo", ipInfo);
				 else{
				 context.addFieldValue("ipinfo", "");
				 }*/
				DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
//				Date optime = context.getDateValue("LOG_OPERTIME",dateFormat);
//				context.addFieldValue("logOpertime",optime);
				context.addFieldValue("newcollecttime",new Date());

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
		importBuilder.setExportResultHandler(new ExportResultHandler<Object, RecordMetadata>() {
			@Override
			public void success(TaskCommand<Object,RecordMetadata> taskCommand, RecordMetadata result) {
				TaskMetrics taskMetric = taskCommand.getTaskMetrics();
				logger.debug("处理耗时："+taskCommand.getElapsed() +"毫秒");
				logger.debug(taskCommand.getTaskMetrics().toString());
			}

			@Override
			public void error(TaskCommand<Object,RecordMetadata> taskCommand, RecordMetadata result) {
				logger.warn(taskCommand.getTaskMetrics().toString());
			}

			@Override
			public void exception(TaskCommand<Object,RecordMetadata> taskCommand, Exception exception) {
				logger.warn(taskCommand.getTaskMetrics().toString(),exception);
			}

			@Override
			public int getMaxRetry() {
				return 0;
			}
		});

		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setPrintTaskLog(true);

		/**
		 * 构建和启动日志数据采集并发送到kafka作业作业
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();

	}

}
```

在工程中调试好作业后，修改application.properties文件中的mainclass配置，将作业类调整为新开发的作业程序Filelog2KafkaDemo

```properties
mainclass=org.frameworkset.elasticsearch.imp.Filelog2KafkaDemo
```

构建发布可运行的作业部署包：进入命令行模式，在源码工程根目录filelog-elasticsearch下运行以下gradle指令打包发布版本

```
release.bat
```

更多作业配置和运行资料参考：[帮助文档](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/README.md)



# 5.inode机制说明

通过开启inode机制（linux环境下支持）来识别文件重命名操作，避免漏采被重名文件中的没有采集完的日志数据。

```java
config.addConfig(new FileConfig("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\",//指定目录
      "es.log",//指定文件名称，可以是正则表达式
      "^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
      .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
      .addField("tag","elasticsearch")//添加字段tag到记录中
      .setEnableInode(true)
```

setEnableInode方法用来设置是否启用inode文件标识符机制来识别文件重命名操作，linux环境下起作用，windows环境下不起作用（enableInode强制为false）
 linux环境下，在不存在重命名的场景下可以关闭inode文件标识符机制，windows环境下强制关闭inode文件标识符机制

bboss扩展了log4j滚动切割文件插件org.apache.log4j.NormalRollingFileAppender，NormalRollingFileAppender可以实现按照日期时间格式向前命名滚动的日志文件和当前的日志文件（默认官方滚动插件不支持按日期格式命名当前文件）,同时也可以按照整数索引方式向前命名滚动的日志文件和当前的日志文件（默认官方滚动插件不支持按日期格式命名当前文件），在滚动日志文件的同时，不会重命名已经产生的日志名称（默认插件会重命名）。通过不重命名已有文件和生成新的带日期或者整数索引的日志文件，可以很好地解决logstash、filebeat、flume等日志数据采集工具在日志文件滚动切割的时候，漏掉正在切割文件中的日志数据，因为有可能数据还没采集完，文件已经被重命名了。参考文档：

https://doc.bbossgroups.com/#/log4j

基于bboss 扩展的log4j Appender可以实现滚动生成日志文件时，增量添加新文件而不重名之前的日志文件，这样就可以设置setEnableInode(false)来关闭inode机制。