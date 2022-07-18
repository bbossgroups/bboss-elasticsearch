# 基于Filelog插件采集日志jvm溢出踩坑记

本文涉及的filelog采集作业工程源码地址：

[https://gitee.com/bboss/filelog-elasticsearch](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/VOPSTestdevLog2ESNew.java)

作业文件地址：

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/VOPSTestdevLog2ESNew.java

# **1 问题背景**

用户行为分析系统服务器上部署了非常多的数据分析作业进程（近100个进程），每个进程通过log4j2输出日志到日志文件，考虑到bboss filelog日志采集插件的高度定制化和灵活性以及对linux inode机制的友好支持，因此我们采用bboss filelog插件来实时采集用户行为分析服务器上所有日志文件，并写入elasticsearch![img](https://esdoc.bbossgroups.com/images/filelog-es.jpg)

## 1.1 原始作业定义

采集作业定义初始定义如下：

```java
package org.frameworkset.elasticsearch.imp;

import org.frameworkset.spi.assemble.PropertiesUtil;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.input.file.FileConfig;
import org.frameworkset.tran.input.file.FileImportConfig;
import org.frameworkset.tran.input.file.LineMatchType;
import org.frameworkset.tran.output.es.FileLog2ESImportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VOPSTestdevLog2ES {
	private static Logger logger = LoggerFactory.getLogger(VOPSTestdevLog2ES.class);
	public static void main(String[] args){
PropertiesContainer propertiesContainer = PropertiesUtil.getPropertiesContainer();
		int threadCount = propertiesContainer .getIntSystemEnvProperty("log.threadCount",10);

		int threadQueue = propertiesContainer .getIntSystemEnvProperty("log.threadQueue",100);

		int batchSize = propertiesContainer .getIntSystemEnvProperty("log.batchSize",500);

		int fetchSize = propertiesContainer .getIntSystemEnvProperty("log.fetchSize",500);
		boolean printTaskLog = propertiesContainer .getBooleanSystemEnvProperty("log.printTaskLog",false);
		String logPath = propertiesContainer .getSystemEnvProperty("log.path","/home/log/visualops");//同时指定了默认值false
		String startLabel = propertiesContainer.getSystemEnvProperty("log.startLabel","^\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]");
		String charsetEncode = propertiesContainer .getSystemEnvProperty("log.charsetEncode","GB2312");
		String filelog_import = propertiesContainer .getSystemEnvProperty("log.filelog_import","filelog_import");
		String fileNames = propertiesContainer .getSystemEnvProperty("log.fileNames","business-handler,gateway-handler,metrics-warn-job,metrics-webdetector-node,smsdata-job,eccloginlog-handler,metrics-common-job,metrics-historydata-job,metrics-webdetector-handler,metrics-web,webpage-handler,metrics-report,metrics-webdetector-job,smsdata-handler");
		String levels = propertiesContainer .getSystemEnvProperty("log.levels","ERROR,WARN,INFO");
		String[] levelArr = levels.split(",");
		for (int i = 0; i < levelArr.length; i++){
			levelArr[i] = "["+levelArr[i]+"] ";

		}
		ImportBuilder importBuilder = new ImportBuilder();
		importBuilder.setBatchSize(batchSize)//设置批量入库的记录数
				.setFetchSize(fetchSize);//设置按批读取文件行数
		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
		importBuilder.setFlushInterval(10000l);

		/**
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(threadQueue);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(threadCount);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
		importBuilder.setPrintTaskLog(printTaskLog);
		
		FileInputConfig config = new FileInputConfig();
		config.setCharsetEncode(charsetEncode);

		String[] fileNameArr = fileNames.split(",");
		for (int i = 0; i < fileNameArr.length; i++){
			String fileName = fileNameArr[i];
			config.addConfig(new FileConfig(logPath,//指定目录
							fileName+".log",//指定文件名称，可以是正则表达式
							startLabel)//指定多行记录的开头识别标记，正则表达式
							.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
							.addField("tag",fileName.toLowerCase())//添加字段tag到记录中
							.setEnableInode(true)
							.setIncludeLines(levelArr, LineMatchType.STRING_CONTAIN)
//				.setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
					//.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
			);
		}
		/**
		 * 默认采用异步机制保存增量同步数据状态，提升同步性能，可以通过以下机制关闭异步机制：
		 * importBuilder.setAsynFlushStatus(false);
		 */
		importBuilder.setAsynFlushStatus(true);

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
         *     "@timestamp": 1617074824542, 
		 *     "@filemeta": {
		 *       "path": "D:\\ecslog\\error-2021-03-27-1.log",
		 *       "hostname": "",
		 *       "pointer": 3342583,
		 *       "hostip": "",		 *       
		 *       "fileId": "D:/ecslog/error-2021-03-27-1.log"
		 *     },
		 *     "message": "[18:04:40:161] [INFO] - org.frameworkset.tran.schedule.ScheduleService.externalTimeSchedule(ScheduleService.java:192) - Execute schedule job Take 3 ms"
		 *   }
		 * }
		 *
		 * true 开启 false 关闭
		 */
		config.setEnableMeta(true);
		importBuilder.setInputConfig(config);
		//指定elasticsearch数据源名称，在application.properties文件中配置，default为默认的es数据源名称
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
        		elasticsearchOutputConfig.setTargetElasticsearch("default");
        		//指定索引名称，这里采用的是elasticsearch 7以上的版本进行测试，不需要指定type
        		elasticsearchOutputConfig.setIndex("filelog");
        		//指定索引类型，这里采用的是elasticsearch 7以上的版本进行测试，不需要指定type
        		//elasticsearchOutputConfig.setIndexType("idxtype");
        		importBuilder.setOutputConfig(elasticsearchOutputConfig);

		//增量配置开始
		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
		importBuilder.setLastValueStorePath(filelog_import);//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//增量配置结束
		importBuilder.addFieldMapping("@timestamp","collectTime");
		//映射和转换配置开始


		/**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				String tag = context.getStringValue("tag");//更加tag指定日志索引表
				if(tag != null) {
					context.setIndex("vops-dev-"+tag+"-{dateformat=yyyy.MM}");
				}
				else {
					context.setIndex("vops-dev-{dateformat=yyyy.MM}");
				}

			}
		});
		//映射和转换配置结束

		/**
		 * 启动采集日志文件作业
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//启动同步作业
		logger.info("job started.");
	}
}
```

## 1.2 采集作业配置

单文件并行写elasticsearch线程数threadCount ：10个

单文件并行写elasticsearch线程队列数threadQueue：100

单文件采集日志缓冲记录数fetchSize：500

单文件并行批量写elasticsearch记录数batchSize：500

jvm分配内存大小：考虑服务器资源情况，为作业分配2g内存，thead stack大小1m

```
-Xms2g
-Xmx2g
# explicitly set the stack size
-Xss1m
```

作业运行一段时间后，出现filelog进程jvm内存全部耗尽溢出问题

# 2 问题分析

首先考虑filelog是否存在内存泄漏问题，通过eclipse mat内存分析工具查看dump出来的内存快照文件，分析截图如下

## 2.1 内存泄漏表为空，所以不存在内存泄漏问题

![img](https://oscimg.oschina.net/oscnet/up-c461634fa2cfab80aeddd2ecca9e7a35b0a.JPEG)

## 2.2 内存占用分布情况

下面是mat给出的filelog进去2g内存占用分布情况，并给出了三个占用内存比较大对象实例，问题定位还是非常明确。

![img](https://oscimg.oschina.net/oscnet/up-47ea3183ad6177cef014f025a8c3741839e.JPEG)

根据作业定义，每个日志文件单独配置一个采集配置，一次会分配一个采集作业线程一个和一个日志目录扫描线程；**存在大量采集完但是未关闭的日志文件也分配了采集线程**；elasticsearch并行异步写入线程池大小为10个线程；作业为每个线程会分配1m的的线程stack大小，所有线程实例占了将近1g的内存

![img](https://oscimg.oschina.net/oscnet/up-2eed6fed8c14bf17d5e3d8c97438ec8237c.JPEG)

每个日志文件对应一个异步采集通道对象AsynESOutPutDataTran，每个通道都包含了待写入elasticsearch的缓存数据，因此所有通道实例占用的内存将近600M

![img](https://oscimg.oschina.net/oscnet/up-0133f9a13136dcba6027bedc60cf19a60f4.JPEG)

每个日志文件中实时采集的日志数据会放入一个**FileResultSet对象，给**采集通道对象AsynESOutPutDataTran消费使用，所有**FileResultSet占用的内存为230M**

![img](https://oscimg.oschina.net/oscnet/up-67794649b2633557fc76278e0ba4062b2fc.JPEG)

上述三类对象占用内存将近1.8G，所以filelog采集作业启动后，内存很快就达到最大值2G，导致内存溢出问题发生，原因已经找到，接下来就可以来分析和解决问题了。

# 3 问题解决

根据上面的分析，可以通过以下途径解决问题：

1.调大jvm内存，以确保快速高效实时地采集日志文件中的日志数据

2.减少线程数量，减少线程stack大小

3.减少内存缓存大小

最简单的办法就是调大jvm内存，但是服务器资源有限，所以通过分配更多的内存给filelog就不可行，那么只剩下途径2和途径3了，减少线程数量、减少线程stack大小、减少内存缓存大小，牺牲部分性能，以换取日志采集的稳定性。以下是优化过程

## 优化1 减少日志目录监听线程

由于日志文件都在一个目录下面，索引可以将将以下代码

```
String[] fileNameArr = fileNames.split(",");
      for (int i = 0; i < fileNameArr.length; i++){
         String fileName = fileNameArr[i];
         config.addConfig(new FileConfig(logPath,//指定目录
                     fileName+".log",//指定文件名称，可以是正则表达式
                     startLabel)//指定多行记录的开头识别标记，正则表达式
                     .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
                     .addField("tag",fileName.toLowerCase())//添加字段tag到记录中
                     .setEnableInode(true)
                     .setIncludeLines(levelArr, LineMatchType.STRING_CONTAIN)
//          .setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
               //.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
         );
      }
```

调整为下面的代码：

```java
final String[] fileNameArr = fileNames.split(",");
config.addConfig(new FileConfig().setSourcePath(logPath)//指定目录
            .setFileFilter(new FileFilter() {//根据文件名称动态判断目录下的文件是否需要被采集
               @Override
               public boolean accept(File dir, String name, FileConfig fileConfig) {
                  for (int i = 0; i < fileNameArr.length; i++) {
                     String fileName = fileNameArr[i];
                     if(name.equals(fileName+".log"))
                        return true;
                  }
                  return false;
               }
            })
            .setFieldBuilder(new FieldBuilder() { //根据文件信息动态为不同的日志文件添加固定的字段
               @Override
               public void buildFields(FileInfo file, FieldManager fieldManager) {
                  String fileName = file.getFileName();
                  String tag = null;
                  for (int i = 0; i < fileNameArr.length; i++) {
                     String _fileName = fileNameArr[i];
                     if(fileName.startsWith(_fileName)) {
                        tag = _fileName;
                        break;
                     }
                  }
                  //添加tag标记，值为文件名称的小写，作为记录的索引名称
                  if(tag != null)
                     fieldManager.addField("tag",tag.toLowerCase());
               }
            })
            //.addField("tag",fileName.toLowerCase())//添加字段tag到记录中
            .setFileHeadLineRegular(startLabel)//指定多行记录的开头识别标记，正则表达式
            .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
            .setEnableInode(true)
            /**
             *重命名文件监听路径：一些日志组件会指定将滚动日志文件放在与当前日志文件不同的目录下，需要通过renameFileSourcePath指定这个不同的目录地址，以便
             * 可以追踪到未采集完毕的滚动日志文件，从而继续采集文件中没有采集完毕的日志
             * 本路径只有在inode机制有效并且启用的情况下才起作用,默认与sourcePath一致
             */
            .setRenameFileSourcePath(logPath)
            .setCloseOlderTime(closeOlderTime) //如果2天(172800000毫秒)内日志内容没变化，则不再采集对应的日志文件，重启作业也不会采集
            //如果指定了closeOlderTime，但是有些文件是特例不能不关闭，那么可以通过指定CloseOldedFileAssert来
            //检查静默时间达到closeOlderTime的文件是否需要被关闭
            .setCloseOldedFileAssert(new CloseOldedFileAssert() {
               @Override
               public boolean canClose(FileInfo fileInfo) {
                  String name = fileInfo.getFileName();//正文件不能被关闭，滚动生成的文件才需要被关闭
                  for (int i = 0; i < fileNameArr.length; i++) {
                     String fileName = fileNameArr[i];
                     if(name.equals(fileName+".log"))
                        return false;
                  }
                  return true;
               }
            })
            .setIncludeLines(levelArr, LineMatchType.STRING_CONTAIN)
);
```

调整后的作业将只配置一个目录的fileconfig，因此采集作业只会分配一个日志目录扫描线程，从而大幅减少日志目录扫描线程。

## 优化2 减少写入elasticsearch线程数量和队列大小

优化前

```java
int threadCount = PropertiesUtil.getPropertiesContainer().getIntSystemEnvProperty("log.threadCount",10);

int threadQueue = PropertiesUtil.getPropertiesContainer().getIntSystemEnvProperty("log.threadQueue",500);
```

优化后

```java
int threadCount = propertiesContainer.getIntSystemEnvProperty("log.threadCount",2);

int threadQueue = propertiesContainer.getIntSystemEnvProperty("log.threadQueue",50);
```

## 优化3 减少JVM内存和线程stack大小

修改jvm.options中的内存配置和Xss参数

```shell
-Xms512m 
-Xmx512m
-Xss256k
```

## 优化4 减少日志预读取和ES批处理记录数

```java
int batchSize = PropertiesUtil.getPropertiesContainer().getIntSystemEnvProperty("log.batchSize",10);

int fetchSize = PropertiesUtil.getPropertiesContainer().getIntSystemEnvProperty("log.fetchSize",10);
```

## 优化5 设置closeOlderTime，关闭静默日志文件

合理设置closeOlderTime，关闭长时间没有变化的日志文件，大幅减少文件采集作业线程

```java
setCloseOlderTime(2*24*60*60*1000l) //如果2天内日志内容没变化，则不再采集对应的日志文件，重启作业也不会采集
```

## 优化改造后的作业代码

优化改造后的作业代码如下：

```java
package org.frameworkset.elasticsearch.imp;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.input.file.*;
import org.frameworkset.tran.output.es.FileLog2ESImportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class VOPSTestdevLog2ESNew {
   private static Logger logger = LoggerFactory.getLogger(VOPSTestdevLog2ESNew.class);
   public static void main(String[] args){
      PropertiesContainer propertiesContainer = PropertiesUtil.getPropertiesContainer();
      		int threadCount = propertiesContainer.getIntSystemEnvProperty("log.threadCount",5);
      
      		int threadQueue = propertiesContainer.getIntSystemEnvProperty("log.threadQueue",50);
      
      		int batchSize = propertiesContainer.getIntSystemEnvProperty("log.batchSize",100);
      
      		int fetchSize = propertiesContainer.getIntSystemEnvProperty("log.fetchSize",10);
      		long closeOlderTime = propertiesContainer.getLongSystemEnvProperty("log.closeOlderTime",172800000);
      		boolean printTaskLog = propertiesContainer.getBooleanSystemEnvProperty("log.printTaskLog",false);
      		String logPath = propertiesContainer.getSystemEnvProperty("log.path","/home/log/visualops");//同时指定了默认值false
      		String startLabel = propertiesContainer.getSystemEnvProperty("log.startLabel","^\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]");
      		String charsetEncode = propertiesContainer.getSystemEnvProperty("log.charsetEncode","GB2312");
      		String filelog_import = propertiesContainer.getSystemEnvProperty("log.filelog_import","filelog_import");
      		String fileNames = propertiesContainer.getSystemEnvProperty("log.fileNames","business-handler,gateway-handler,metrics-warn-job,metrics-webdetector-node,smsdata-job,eccloginlog-handler,metrics-common-job,metrics-historydata-job,metrics-webdetector-handler,metrics-web,webpage-handler,metrics-report,metrics-webdetector-job,smsdata-handler");
      		String levels = propertiesContainer.getSystemEnvProperty("log.levels","ERROR,WARN,INFO");
      		String[] levelArr = levels.split(",");
      		for (int i = 0; i < levelArr.length; i++){
      			levelArr[i] = "["+levelArr[i]+"] ";
      
      		}
      		ImportBuilder importBuilder = new ImportBuilder();
      		importBuilder.setBatchSize(batchSize)//设置批量入库的记录数
      				.setFetchSize(fetchSize);//设置按批读取文件行数
      		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
      		importBuilder.setFlushInterval(10000l);
      
      		/**
      		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
      		 */
      		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
      		importBuilder.setQueue(threadQueue);//设置批量导入线程池等待队列长度
      		importBuilder.setThreadCount(threadCount);//设置批量导入线程池工作线程数量
      		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
      		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
      		importBuilder.setPrintTaskLog(printTaskLog);
      
      		FileInputConfig config = new FileInputConfig();
      		config.setCharsetEncode(charsetEncode);
      
      		final String[] fileNameArr = fileNames.split(",");
      		config.addConfig(new FileConfig().setSourcePath(logPath)//指定目录
      						.setFileFilter(new FileFilter() {//根据文件名称动态判断目录下的文件是否需要被采集
      							@Override
      							public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
      								String name = fileInfo.getFileName();
      								for (int i = 0; i < fileNameArr.length; i++) {
      									String fileName = fileNameArr[i];
      									if(name.equals(fileName+".log"))
      										return true;
      								}
      								return false;
      							}
      						})
      						.setFieldBuilder(new FieldBuilder() { //根据文件信息动态为不同的日志文件添加固定的字段
      							@Override
      							public void buildFields(FileInfo file, FieldManager fieldManager) {
      								String fileName = file.getFileName();
      								String tag = null;
      								for (int i = 0; i < fileNameArr.length; i++) {
      									String _fileName = fileNameArr[i];
      									if(fileName.startsWith(_fileName)) {
      										tag = _fileName;
      										break;
      									}
      								}
      								//添加tag标记，值为文件名称的小写，作为记录的索引名称
      								if(tag != null)
      									fieldManager.addField("tag",tag.toLowerCase());
      							}
      						})
      						//.addField("tag",fileName.toLowerCase())//添加字段tag到记录中
      						.setFileHeadLineRegular(startLabel)//指定多行记录的开头识别标记，正则表达式
      						.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
      						.setEnableInode(true)
      						/**
      						 *重命名文件监听路径：一些日志组件会指定将滚动日志文件放在与当前日志文件不同的目录下，需要通过renameFileSourcePath指定这个不同的目录地址，以便
      						 * 可以追踪到未采集完毕的滚动日志文件，从而继续采集文件中没有采集完毕的日志
      						 * 本路径只有在inode机制有效并且启用的情况下才起作用,默认与sourcePath一致
      						 */
      						.setRenameFileSourcePath(logPath)
      						.setCloseOlderTime(closeOlderTime) //如果2天(172800000毫秒)内日志内容没变化，则不再采集对应的日志文件，重启作业也不会采集
      						//如果指定了closeOlderTime，但是有些文件是特例不能不关闭，那么可以通过指定CloseOldedFileAssert来
      						//检查静默时间达到closeOlderTime的文件是否需要被关闭
      						.setCloseOldedFileAssert(new CloseOldedFileAssert() {
      							@Override
      							public boolean canClose(FileInfo fileInfo) {
      								String name = fileInfo.getFileName();//正文件不能被关闭，滚动生成的文件才需要被关闭
      								for (int i = 0; i < fileNameArr.length; i++) {
      									String fileName = fileNameArr[i];
      									if(name.equals(fileName+".log"))
      										return false;
      								}
      								return true;
      							}
      						})
      						.setIncludeLines(levelArr, LineMatchType.STRING_CONTAIN)
      		);
      
      		/**
      		 * 默认采用异步机制保存增量同步数据状态，提升同步性能，可以通过以下机制关闭异步机制：
      		 * importBuilder.setAsynFlushStatus(false);
      		 */
      		importBuilder.setAsynFlushStatus(true);
      
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
      		importBuilder.setInputConfig(config);
      		//指定elasticsearch数据源名称，在application.properties文件中配置，default为默认的es数据源名称
      		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
      		elasticsearchOutputConfig.setTargetElasticsearch("default");
      //		//指定索引名称，这里采用的是elasticsearch 7以上的版本进行测试，不需要指定type
      		elasticsearchOutputConfig.setIndex("filelog");
      //		//指定索引类型，这里采用的是elasticsearch 7以上的版本进行测试，不需要指定type
      //		//elasticsearchOutputConfig.setIndexType("idxtype");
      		importBuilder.setOutputConfig(elasticsearchOutputConfig);
      		//增量配置开始
      		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
      		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
      		importBuilder.setLastValueStorePath(filelog_import);//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
      		//增量配置结束
      		importBuilder.addFieldMapping("@timestamp","collectTime");
      		//映射和转换配置开始
      
      
      		/**
      		 * 重新设置es数据结构
      		 */
      		importBuilder.setDataRefactor(new DataRefactor() {
      			public void refactor(Context context) throws Exception  {
      				String tag = context.getStringValue("tag");
      				if(tag != null) {
      					context.setIndex("vops-dev-"+tag+"-{dateformat=yyyy.MM}");
      				}
      				else {
      					context.setIndex("vops-dev-{dateformat=yyyy.MM}");
      				}
      
      			}
      		});
      		//映射和转换配置结束
      
      
      		/**
      		 * 启动es数据导入文件并上传sftp/ftp作业
      		 */
      		DataStream dataStream = importBuilder.builder();
      		dataStream.execute();//启动同步作业
      		logger.info("job started.");
   }
}
```

## 重新构建发布作业

重新构建发布作业（[参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_12-%e5%8f%91%e5%b8%83%e7%89%88%e6%9c%ac)），启动新的filelog采集作业,日志采集功能恢复正常。

# 4 问题总结

1. jvm内存溢出问题得到解决

2. filelog进程本身消耗jvm内存也由2g缩小到512m，更加绿色环保。

# 5 相关文档

bboss日志采集插件使用参考文档

[https://esdoc.bbossgroups.com/#/filelog-guide](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fesdoc.bbossgroups.com%2F%23%2Ffilelog-guide)