# Elasticsearch/DB到SFTP/FTP数据同步

通过bboss数据同步工具文件输出插件，可以非常高效快速方便地将Elasticsearch和Database中的数据实时导出（增量/全量）到文件并上传到SFTP/FTP服务器，本文通过案例来详细介绍。

![](https://esdoc.bbossgroups.com/images/datasyn.png)

# 1.案例源码工程

https://gitee.com/bboss/elasticsearch-file2ftp

https://github.com/bbossgroups/elasticsearch-file2ftp

# 2.案例功能说明

1. 串行将数据导出到文件并上传ftp和sftp
2. 串行批量将数据导出到文件并上传ftp和sftp
3. 并行将数据批量导出到文件并上传ftp和sftp
4. 通过设置disableftp为true，控制只生成数据文件，禁用文件上传sftp/ftp功能（生成的文件保留在fileDir对应的目录下）

**特别关注点**

除了bboss同步工具通用特性（增量/全量同步、异步/同步、增删改查同步），需额外说明一下本案例中特定的特色：

1. 支持上传失败文件重传功能
2. 支持上传成功文件备份功能，并可指定备份多长时间
3. 支持备份文件自动清理功能
4. 支持按记录条数切割文件
5. 优雅解决elasticsearch异步延迟写入特性可能导致增量同步遗漏步数据问题

本文只介绍elasticsearch数据同步上传到sftp案例

https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpBatchSplitFileDemo.java

其他案例直接查看源码：

elasticsearch数据同步上传到ftp案例代码地址

https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ftp/ES2FileFtpBatchDemo.java

数据库同步上传到sftp案例代码地址

https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2FileFtpDemo.java

# 3.案例讲解

   接下来以elasticsearch数据导出并上传sftp服务器为例进行介绍，

##    3.1建立同步作业处理类

   先看完整的同步作业处理类-[ES2FileFtpBatchSplitFileDemo](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpBatchSplitFileDemo.java)，然后再详细讲解。

```java
package org.frameworkset.elasticsearch.imp;

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.runtime.CommonLauncher;
import org.frameworkset.tran.CommonRecord;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.input.fileftp.es.ES2FileFtpExportBuilder;
import org.frameworkset.tran.output.fileftp.*;
import org.frameworkset.tran.output.fileftp.FilenameGenerator;
import org.frameworkset.tran.output.fileftp.ReocordGenerator;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.ImportIncreamentConfig;
import org.frameworkset.tran.schedule.TaskContext;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class ES2FileFtpBatchSplitFileDemo {
   public static void main(String[] args){
      ImportBuilder importBuilder = new ImportBuilder();
      		importBuilder.setBatchSize(1000).setFetchSize(2000);
      		String ftpIp = CommonLauncher.getProperty("ftpIP","127.0.0.1");//同时指定了默认值
      		FileOutputConfig FileOutputConfig = new FileOutputConfig();
      		FtpOutConfig ftpOutConfig = new FtpOutConfig();
      		FileOutputConfig.setFtpOutConfig(ftpOutConfig);
      
      		ftpOutConfig.setFtpIP(ftpIp);
      
      		ftpOutConfig.setFtpPort(5322);
      		ftpOutConfig.setFtpUser("1111");
      		ftpOutConfig.setFtpPassword("1111@123");
      		ftpOutConfig.setRemoteFileDir("/home/ecs/failLog");
      		ftpOutConfig.setKeepAliveTimeout(100000);
      		ftpOutConfig.setTransferEmptyFiles(true);
      		ftpOutConfig.setFailedFileResendInterval(-1);
      		ftpOutConfig.setBackupSuccessFiles(true);
      
      		ftpOutConfig.setSuccessFilesCleanInterval(5000);
      		ftpOutConfig.setFileLiveTime(86400);//设置上传成功文件备份保留时间，默认2天
      		FileOutputConfig.setMaxFileRecordSize(100000);//每千条记录生成一个文件
      		FileOutputConfig.setFileDir("D:\\workdir");
      		//自定义文件名称
      		FileOutputConfig.setFilenameGenerator(new FilenameGenerator() {
      			@Override
      			public String genName( TaskContext taskContext,int fileSeq) {
      				//fileSeq为切割文件时的文件递增序号
      				String time = (String)taskContext.getTaskData("time");//从任务上下文中获取本次任务执行前设置时间戳
      				String _fileSeq = fileSeq+"";
      				int t = 6 - _fileSeq.length();
      				if(t > 0){
      					String tmp = "";
      					for(int i = 0; i < t; i ++){
      						tmp += "0";
      					}
      					_fileSeq = tmp+_fileSeq;
      				}
      
      
      
      				return "HN_BOSS_TRADE"+_fileSeq + "_"+time +"_" + _fileSeq+".txt";
      			}
      		});
      		//指定文件中每条记录格式，不指定默认为json格式输出
      		FileOutputConfig.setRecordGenerator(new RecordGenerator() {
      			@Override
      			public void buildRecord(TaskContext taskContext, CommonRecord record, Writer builder) {
      				//直接将记录按照json格式输出到文本文件中
      				SerialUtil.normalObject2json(record.getDatas(),//获取记录中的字段数据
      						builder);
      				String data = (String)taskContext.getTaskData("data");//从任务上下文中获取本次任务执行前设置时间戳
      //          System.out.println(data);
      
      			}
      		});
      		importBuilder.setOutputConfig(FileOutputConfig);
      		importBuilder.setIncreamentEndOffset(300);//单位秒，同步从上次同步截止时间当前时间前5分钟的数据，下次继续从上次截止时间开始同步数据
      		//vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27
      		ElasticsearchInputConfig elasticsearchInputConfig = new ElasticsearchInputConfig();
      		elasticsearchInputConfig
      				.setDslFile("dsl2ndSqlFile.xml")
      				.setDslName("scrollQuery")
      				.setScrollLiveTime("10m")
      //				.setSliceQuery(true)
      //				.setSliceSize(5)
      				.setQueryUrl("kafkademo/_search")
      				/**
      				//通过简单的示例，演示根据实间范围计算queryUrl,以当前时间为截止时间，后续版本6.3.2将增加lastEndtime参数作为截止时间（在设置了IncreamentEndOffset情况下有值）
      				.setQueryUrlFunction((TaskContext taskContext,Date lastStartTime,Date lastEndTime)->{
      					String formate = "yyyy.MM.dd";
      					SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
      					String startTime = dateFormat.format(lastEndTime);
      //					Date lastEndTime = new Date();
      					String endTimeStr = dateFormat.format(lastEndTime);
      					return "dbdemo-"+startTime+ ",dbdemo-"+endTimeStr+"/_search";
      //					return "vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27/_search";
      //					return "dbdemo/_search";
      				})**/
      				.setSourceElasticsearch("default");
      		importBuilder.setInputConfig(elasticsearchInputConfig)
      				.addParam("fullImport",true)
      //				//添加dsl中需要用到的参数及参数值
      				.addParam("var1","v1")
      				.addParam("var2","v2")
      				.addParam("var3","v3");
      
      
      
      		//定时任务配置，
      		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
      //					 .setScheduleDate(date) //指定任务开始执行时间：日期
      				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
      				.setPeriod(300000L); //每隔period毫秒执行，如果不设置，只执行一次
      		//定时任务配置结束
      
      		//设置任务执行拦截器，可以添加多个
      		importBuilder.addCallInterceptor(new CallInterceptor() {
      			@Override
      			public void preCall(TaskContext taskContext) {
      				String formate = "yyyyMMddHHmmss";
      				//HN_BOSS_TRADE00001_YYYYMMDDHHMM_000001.txt
      				SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
      				String time = dateFormat.format(new Date());
      				taskContext.addTaskData("time",time);
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
      		importBuilder.setLastValueColumn("collecttime");//手动指定日期增量查询字段变量名称
      		importBuilder.setFromFirst(true);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
      		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
      		importBuilder.setLastValueStorePath("es2fileftp_batchsplitimport");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
      //		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
      		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
      		// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
      		//指定增量同步的起始时间
      //		importBuilder.setLastValue(new Date());
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
      		importBuilder.addFieldValue("author","张无忌");
      //		importBuilder.addFieldMapping("operModule","OPER_MODULE");
      //		importBuilder.addFieldMapping("logContent","LOG_CONTENT");
      //		importBuilder.addFieldMapping("logOperuser","LOG_OPERUSER");
      
      
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
      				String data = (String)context.getTaskContext().getTaskData("data");
      //				System.out.println(data);
      
      //				context.addFieldValue("author","duoduo");//将会覆盖全局设置的author变量
      				context.addFieldValue("title","解放");
      				context.addFieldValue("subtitle","小康");
      
      //				context.addIgnoreFieldMapping("title");
      				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
      //				context.addIgnoreFieldMapping("author");
      
      //				//修改字段名称title为新名称newTitle，并且修改字段的值
      //				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
      				/**
      				 * 获取ip对应的运营商和区域信息
      				 */
      				IpInfo ipInfo = (IpInfo) context.getIpInfo("logVisitorial");
      				if(ipInfo != null)
      					context.addFieldValue("ipinfo", ipInfo);
      				else{
      					context.addFieldValue("ipinfo", "");
      				}
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
      
      		/**
      		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
      		 */
      		importBuilder.setParallel(false);//设置为多线程并行批量导入,false串行
      		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
      		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
      		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
      		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
      		importBuilder.setPrintTaskLog(true);
      
      		/**
      		 * 启动es数据导入文件并上传sftp/ftp作业
      		 */
      		DataStream dataStream = importBuilder.builder();
      		dataStream.execute();//启动同步作业
   }
}
```

## 3.2 作业配置-ImportBuilder 

  通过ImportBuilder类来构建导出elasticsearch数据到sftp/ftp文件上传同步作业

## 3.3 设置批量写文件的记录条数-BatchSize和批量从elasticsearch获取记录条数fetchSize

 importBuilder.setBatchSize(500).setFetchSize(1000);

## 3.4 文件上传SFTP/FTP

### 3.4.1 SFTP/FTP配置

通过FileOutputConfig和FtpOutConfig两个类配合来设置sftp和ftp上传的的相关配置：

| 参数名称                  | 描述                                                                                                                                                                                                                                                        | 默认值               | 对应协议 |
| ------------------------- |-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| -------------------- | -------- |
| ftpIP                     | 必填，String类型，ftp/sftp服务器ip地址                                                                                                                                                                                                                               | 空                   | ftp/sftp |
| ftpPort                   | 必填，int类型，ftp/sftp服务器端口                                                                                                                                                                                                                                    | 空                   | ftp/sftp |
| ftpUser                   | 必填，String类型，ftp/sftp服务器用户账号                                                                                                                                                                                                                               | 空                   | ftp/sftp |
| ftpPassword               | 必填，String类型，ftp/sftp服务器用户口令                                                                                                                                                                                                                               | 空                   | ftp/sftp |
| remoteFileDir             | 必填，String类型，指定ftp/sftp服务器目录地址，用户存放上传的文件                                                                                                                                                                                                                   | 空                   | ftp/sftp |
| fileDir                   | 必填，String类型，指定导出数据生成的文件存放的本地目录、上传成功文件备份目录、上传失败文件存放目录（便于定时重传）                                                                                                                                                                                              | 空                   | ftp/sftp |
| keepAliveTimeout          | 可选，long类型，单位毫秒，ftp/sftp通讯协议连接存活超时时间                                                                                                                                                                                                                       | 0                    | ftp/sftp |
| socketTimeout             | 可选，long类型，单位毫秒，ftp/sftp数据读取超时时间，避免socketTimeout异常                                                                                                                                                                                                         | 0                    | ftp/sftp |
| connectTimeout            | 可选，long类型，单位毫秒，sftp数据连接建立超时时间                                                                                                                                                                                                                             | 0                    | sftp     |
| transferEmptyFiles        | 必填，boolean类型，是否上传空文件，true上传，false不上传                                                                                                                                                                                                                      | true                 | ftp/sftp |
| backupSuccessFiles        | 必填，boolean类型，是备份上传成功文件，true备份，false不备份                                                                                                                                                                                                                    | false                | ftp/sftp |
| failedFileResendInterval  | 必填，long类型，失败文件重传时间间隔，-1或者0不重传，单位：毫秒                                                                                                                                                                                                                       | 5000                 | ftp/sftp |
| successFilesCleanInterval | 必填，long类型，扫描需要清理上传成功文件时间间隔，单位：毫秒                                                                                                                                                                                                                          | 5000                 | ftp/sftp |
| fileLiveTime              | 必填，int类型，上传成功文件保留时间，单位：秒                                                                                                                                                                                                                                  | 2天                  | ftp/sftp |
| maxFileRecordSize         | 必填，int类型，切割文件时，指定每个文件保存的记录条数，>0时启用文件切割机制；按记录条数切割文件机制对并行导出数据不起作用                                                                                                                                                                                           | -1                   | ftp/sftp |
| maxForceFileThreshold     | 单位：秒，设置文件数据写入空闲时间阈值，如果空闲时间内没有数据到来，则进行文件切割或者flush数据到文件处理。文件切割记录规则：达到最大记录数或者空闲时间达到最大空闲时间阈值，进行文件切割 。 如果不切割文件，达到最大最大空闲时间阈值，当切割文件标识为false时，只执行flush数据操作，不关闭文件也不生成新的文件，否则生成新的文件。本属性适用于文件输出插件与Rocketmq、kafka、mysql binlog 、fileinput等事件监听型的输入插件配合使用，其他类型输入插件无需配置 | 0                    | ftp/sftp |
| filenameGenerator         | 必填，FilenameGenerator接口类型，用于自定义生成文件的名称                                                                                                                                                                                                                     | 无                   | ftp/sftp |
| hostKeyVerifier           | 可选，适用于sftp协议，如果sftp协议需要指定，可以先不设置，然后将运行报错日志中打印出来字符串设置即可                                                                                                                                                                                                    | 无                   | sftp     |
| reocordGenerator          | 可选，ReocordGenerator接口类型，用来定义生成的记录格式，如果不设置默认为json格式                                                                                                                                                                                                        | JsonReocordGenerator | ftp/sftp |
| sendFileAsyn              | 可选，boolean类型,设置是否异步发送文件， true 异步发送 false同步发送,默认同步发送。数据量比较多，同时切割文件的情况下，启用异步发送文件，会显著提升数据采集同步性能                                                                                                                                                              | false                | ftp/sftp |
| sendFileAsynWorkThreads   | 可选，int类型,设置异步发送文件线程数                                                                                                                                                                                                                                      | 10                   | ftp/sftp |

示例代码如下：

```java
String ftpIp = CommonLauncher.getProperty("ftpIP","192.168.137.1");//同时指定了默认值
      FileOupputConfig FileOutputConfig = new FileOupputConfig();
	   FtpOutConfig ftpOutConfig = new FtpOutConfig();
	  FileOutputConfig.setFtpOutConfig(ftpOutConfig);
       
      ftpOutConfig.setFtpIP(ftpIp);
     
      ftpOutConfig.setFtpPort(5322);

      ftpOutConfig.setFtpUser("2222");
      ftpOutConfig.setFtpPassword("2222@123");
      ftpOutConfig.setRemoteFileDir("/home/ecs/failLog");
      ftpOutConfig.setKeepAliveTimeout(100000);
      ftpOutConfig.setTransferEmptyFiles(true); //true 上传空文件，false 不上传
      ftpOutConfig.setFailedFileResendInterval(5000); //上传失败文件重传时间间隔，单位：毫秒，<=0时不重传
      ftpOutConfig.setBackupSuccessFiles(true);//true 备份上传成功文件，false不备份

      FileOutputConfig.setSuccessFilesCleanInterval(5000);//定期扫描清理过期备份文件时间间隔，单位：毫秒
      ftpOutConfig.setFileLiveTime(86400);//设置上传成功文件备份保留时间，默认2天
		//设置是否异步发送文件，true 异步发送 false同步发送,默认同步发送
		ftpOutConfig.setSendFileAsyn(true);
		//设置异步发送文件线程数
		ftpOutConfig.setSendFileAsynWorkThreads(5);

      FileOutputConfig.setFileDir("D:\\workdir");
      FileOutputConfig.setMaxFileRecordSize(1000);//设置切割文件记录数，每千条记录生成一个文件
		
      //自定义文件名称
      FileOutputConfig.setFilenameGenerator(new FilenameGenerator() {
         @Override
         public String genName( TaskContext taskContext,int fileSeq) {
		    //fileSeq为切割文件时的文件递增序号
            String time = (String)taskContext.getTaskData("time");//从任务上下文中获取本次任务执行前设置时间戳
            String _fileSeq = fileSeq+"";
            int t = 6 - _fileSeq.length();
            if(t > 0){
               String tmp = "";
               for(int i = 0; i < t; i ++){
                  tmp += "0";
               }
               _fileSeq = tmp+_fileSeq;
            }



            return "HN_BOSS_TRADE"+_fileSeq + "_"+time +"_" + _fileSeq+".txt";
         }
      });
      //指定文件中每条记录格式，不指定默认为json格式输出
      FileOutputConfig.setReocordGenerator(new ReocordGenerator() {
         @Override
         public void buildRecord(TaskContext taskContext, CommonRecord record, Writer builder) {
             //直接将记录按照json格式输出到文本文件中
            SerialUtil.normalObject2json(record.getDatas(),//获取记录中的字段数据
                                         builder);
            String data = (String)taskContext.getTaskData("data");//从任务上下文中获取本次任务执行前设置时间戳
//          System.out.println(data);

         }
      });
      importBuilder.setOutputConfig(fileFtpOupputConfig);
```

### 3.4.2 FTP异步发送文件

数据量比较多，同时切割文件的情况下，启用异步发送文件，会显著提升数据采集同步性能。

设置sendFileAsyn是否异步发送文件， true 异步发送 false同步发送,默认同步发送。

通过sendFileAsynWorkThreads设置异步发送文件线程数

配置示例如下：

```java
	//设置是否异步发送文件，true 异步发送 false同步发送,默认同步发送
	ftpOutConfig.setSendFileAsyn(true);
	//设置异步发送文件线程数
	ftpOutConfig.setSendFileAsynWorkThreads(5);
```

### 3.4.3 备份清理发送完毕文件

生成的文件发送ftp后，可以控制是否备份发送成功文件，同时可以设置备份文件有效期和过期文件清理时间间隔，超过有效期的文件将被删除清理掉：

```java
ftpOutConfig.setBackupSuccessFiles(true); //设置备份发送成功文件             

ftpOutConfig.setSuccessFilesCleanInterval(5000);  //设置过期文件清理时间间隔，单位：毫秒         

ftpOutConfig.setFileLiveTime(86400);//设置上传成功文件备份保留时间，默认2天，单位：毫秒       
```



### 3.4.4 发送ftp失败重传设置

通过FailedFileResendInterval设置失败文件重传时间间隔，-1或者0不重传，默认值5秒，单位：毫秒  

```java
ftpOutConfig.setFailedFileResendInterval(10000L);    
```



## 3.5 文件名称生成机制配置

必须通过FileOutputConfig对象的setFilenameGenerator方法设置文件名称生成接口FilenameGenerator，示例代码如下：

```java
 //自定义文件名称
      FileOutputConfig.setFilenameGenerator(new FilenameGenerator() {
         @Override
         public String genName( TaskContext taskContext,int fileSeq) {
		    //fileSeq为切割文件时的文件递增序号
            String time = (String)taskContext.getTaskData("time");//从任务上下文中获取本次任务执行前设置时间戳
            String _fileSeq = fileSeq+"";
            int t = 6 - _fileSeq.length();
            if(t > 0){
               String tmp = "";
               for(int i = 0; i < t; i ++){
                  tmp += "0";
               }
               _fileSeq = tmp+_fileSeq;
            }



            return "HN_BOSS_TRADE"+_fileSeq + "_"+time +"_" + _fileSeq+".txt";
         }
      });
```

接口方法genName带有两个参数：

**TaskContext taskContext**, 任务上下文对象，包含任务执行过程中需要的上下文数据，比如任务执行时间戳、其他任务执行过程中需要用到的数据

**int fileSeq**   文件序号，从1开始，自动递增，如果指定了每个文件保存的最大记录数，fileSeq就会被用到文件名称中，用来区分各种文件

重名文件替换设置：如果文件已经存在可以通过fileOupputConfig.setExistFileReplace(true)设置替换重名文件，如果不替换，就需要在genname方法返回带序号的文件名称：

```java
 fileOupputConfig.setExistFileReplace(true);//替换重名文件
 //如果不替换，就需要在genname方法返回带序号的文件名称
        fileOupputConfig.setFilenameGenerator(new FilenameGenerator() {
            @Override
            public String genName(TaskContext taskContext, int fileSeq) {


                return "师大2021年新生医保（2021年）申报名单-合并-"+fileSeq+".xlsx";
            }
        });
```

## 3.6 自定义记录输出格式

默认采用json格式输出每条记录到文件中，我们可以FileOutputConfig对象的setReocordGenerator方法设置自定义记录生成接口ReocordGenerator。

接口方法buildRecord参数说明：

**Context recordContext**, 记录处理上下文对象，可以通过recordContext.getTaskContext()获取任务执行上下文对象，并获取任务上下文数据

**CommonRecord record**, 处理当前记录对象，包含记录数据Map<key,value> 

**Writer builder**   记录数据写入器   

直接将record中的数据转换为json文本并输出到Writer builder中：

```java
 //指定文件中每条记录格式，不指定默认为json格式输出
      FileOutputConfig.setReocordGenerator(new ReocordGenerator() {
         @Override
         public void buildRecord(TaskContext recordContext, CommonRecord record, Writer builder) {
             //直接将记录按照json格式输出到文本文件中
            SerialUtil.normalObject2json(record.getDatas(),//获取记录中的字段数据
                                         builder);
            //String data = (String)recordContext.getTaskData("data");//从任务上下文中获取本次任务执行前设置时间戳
//          System.out.println(data);

         }
      });
```

竖线|分隔字段值：

```java
public class DataSendReocordGenerator implements ReocordGenerator {
    @Override
    public void buildRecord(TaskContext taskContext, CommonRecord record, Writer builder) {
            Map<String, Object> datas = record.getDatas();
            try {
                Map<String,String> chanMap = (Map<String,String>)taskContext.getTaskData("chanMap");//从任务上下文中获取渠道字典数据

                String phoneNumber = (String) datas.get("phoneNumber");//手机号码
                if(phoneNumber==null){
                    phoneNumber="";
                }
                builder.write(phoneNumber);//将字段内容输出到文件
                builder.write("|");//输出字段分隔符

                String chanId = (String) datas.get("chanId");//办理渠道名称 通过Id获取名称
                String chanName = null;
                if(chanId==null){
                    chanName="";
                }else{
                    chanName=chanMap.get(chanId);
                    if(chanName == null){
                        chanName = chanId;
                    }
                }
                builder.write(chanName);
                builder.write("|");

              
                builder.write(goodsName);
                builder.write("|");

                String goodsCode = (String) datas.get("goodsCode");//资费档次编码
                if(goodsCode==null){
                    goodsCode="";
                }
                builder.write(goodsCode);
                builder.write("|");

                String bossErrorCode = (String) datas.get("bossErrorCode");//错误码
                if(bossErrorCode==null){
                    bossErrorCode="";
                }
                builder.write(bossErrorCode);
                builder.write("|");

                String bossErrorDesc = (String) datas.get("bossErrorDesc");//错误码描述
                if(bossErrorDesc==null){
                    bossErrorDesc="";
                }else{
                    bossErrorDesc = bossErrorDesc.replace("|","\\|").replace("\r\n","\\\\r\\\\n"); //处理字段内容中包含的|字符和回车换行符
                }
                builder.write(bossErrorDesc);

            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
```

## 3.7 设置文件标题行

通过设置HeaderRecordGenerator类型的接口，可以在设置输出日志格式的同时，设置标题行，在生成csv文件时可以使用这个功能

```java
FileOutputConfig.setRecordGenerator(new HeaderRecordGenerator() {
    @Override
    public void buildHeaderRecord(Writer builder) throws Exception {
        builder.write("社保经办机构（建议填写）,人员编号,*姓名,*证件类型,*证件号码,*征收项目,*征收品目,征收子目,*缴费年度,*缴费档次");
    }

    @Override
    public void buildRecord(TaskContext context, CommonRecord record, Writer builder)throws Exception {
        Map<String,Object> datas = record.getDatas();
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(datas.get("shebao_org"))   ;
        strBuilder.append(",")   ;
        String person_no = (String)datas.get("person_no");
        if(person_no == null )
            strBuilder.append("");
        else {
            strBuilder.append("^").append(person_no);
        }
        strBuilder.append(",")   ;
        strBuilder.append(datas.get("name"))   ;
        strBuilder.append(",")   ;
        strBuilder.append(datas.get("cert_type"))   ;
        strBuilder.append(",^")   ;
        strBuilder.append(datas.get("cert_no"))   ;
        strBuilder.append(",")   ;
        strBuilder.append(datas.get("zhs_item"))   ;

        strBuilder.append(",")   ;
        strBuilder.append(datas.get("zhs_class"))   ;
        strBuilder.append(",")   ;
        strBuilder.append(datas.get("zhs_sub_class"))   ;
        strBuilder.append(",")   ;
        strBuilder.append(datas.get("zhs_year"))   ;
        strBuilder.append(",")   ;

        strBuilder.append(datas.get("zhs_level"))   ;
        builder.write(strBuilder.toString());
    }
});
```

## 3.8 elasticsearch增量导出截止时间偏移量设置

elasticsearch增量导出截止时间偏移量设置-IncreamentEndOffset，由于elasticsearch异步写入数据的特性，如果采用原有的增量时间戳机制（起始时间>lastImporttime，没有截止时间）,会导致遗漏部分未落盘数据，因此需要指定基于当前时间往前偏移IncreamentEndOffset对应的时间作为数据导出截止时间，单位：秒

示例如下：

```java
importBuilder.setIncreamentEndOffset(300);//单位秒，同步从上次同步截止时间当前时间前5分钟的数据，下次继续从上次截止时间开始同步数据
```

## 3.9 从elasticsearch检索数据配置

   下面介绍从Elasticsearch检索数据的相关配置参数

| 参数名称            | 描述                                                         | 默认值 |
| ------------------- | ------------------------------------------------------------ | ------ |
| dsl2ndSqlFile       | String类型，必填，检索dsl的xml配置文件路径，相对应于classpath路径，配置案例：[dsl2ndSqlFile.xml](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/resources/dsl2ndSqlFile.xml) | 无     |
| dslName             | String类型，必填，在xml配置文件中dsl的配置名称，dsl语句中的关键配置增量字段xxx和增加截止字段xxx\_\_endTime命名约定：截止字段为增量字段名称xxx自动加上\_\_endTime后缀，示例：                                                                                                                                         {   ## 增量检索范围，可以是时间范围，也可以是数字范围，这里采用的是数字增量字段     "range": {          #if($collecttime)         "collecttime": { ## 时间增量检索字段             "gt": #[collecttime],             "lte": #[collecttime__endTime]         }         #end     } } | 无     |
| scrollLiveTime      | String类型，必填，scroll上下文件有效期，根据实际情况设置，例如：10m | 无     |
| queryUrl            | String类型，可选，直接指定elasticsearch查询rest服务地址，格式：索引名称/\_search,例如：dbdemo/\_search |        |
| queryUrlFunction    | QueryUrlFunction接口类型，可选，public String queryUrl(Date lastTime)，根据接口方法同步数据参数lastTime来动态设置同步的索引名称和检索服务地址，适用于于按时间分索引的场景。 |        |
| sourceElasticsearch | String类型，可选，设置elasticsearch数据源（bboss支持[配置多个数据源](https://esdoc.bbossgroups.com/#/common-project-with-bboss?id=_22%e5%a4%9a%e9%9b%86%e7%be%a4%e9%85%8d%e7%bd%ae)），具体的配置在[application.properties](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/resources/application.properties)中配置 |        |
| addParam            | 方法类型，可选，通过name/value方式，添加dsl中的检索条件，例如：importBuilder.addParam("var1","v1");对应dsl中的写法：{     "term": {         "var1.keyword": #[var1]     } } |        |
| sliceQuery          | 可选，boolean 类型，标记查询是否是slicescroll 查询           | false  |
| sliceSize           | 可选，int类型，设置slice scroll并行查询的slicesize           |        |

### 3.9.1 使用配置文件中的Elasticsearch数据源

使用application.properties中配置的Elasticsearch数据源default

```java
ElasticsearchInputConfig elasticsearchInputConfig = new ElasticsearchInputConfig();
		elasticsearchInputConfig
				.setDslFile("dsl2ndSqlFile.xml")
				.setDslName("scrollQuery")
				.setScrollLiveTime("10m")
//				.setSliceQuery(true)
//				.setSliceSize(5)
				.setQueryUrl("kafkademo/_search")
				/**
				//通过简单的示例，演示根据实间范围计算queryUrl,以当前时间为截止时间，后续版本6.3.2将增加lastEndtime参数作为截止时间（在设置了IncreamentEndOffset情况下有值）
				.setQueryUrlFunction((TaskContext taskContext,Date lastStartTime,Date lastEndTime)->{
					String formate = "yyyy.MM.dd";
					SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
					String startTime = dateFormat.format(lastEndTime);
//					Date lastEndTime = new Date();
					String endTimeStr = dateFormat.format(lastEndTime);
					return "dbdemo-"+startTime+ ",dbdemo-"+endTimeStr+"/_search";
//					return "vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27/_search";
//					return "dbdemo/_search";
				})**/
				.setSourceElasticsearch("default");
		importBuilder.setInputConfig(elasticsearchInputConfig)
				.addParam("fullImport",true)
//				//添加dsl中需要用到的参数及参数值
				.addParam("var1","v1")
				.addParam("var2","v2")
				.addParam("var3","v3");
```

### 3.9.2 直接配置Elasticsearch数据源

可以通过ElasticsearchInputConfig直接配置Elasticsearch数据源

```java
ElasticsearchInputConfig elasticsearchInputConfig = new ElasticsearchInputConfig();
      elasticsearchInputConfig.setDslFile("dsl2ndSqlFile.xml")//配置dsl和sql语句的配置文件
            .setDslName("scrollQuery") //指定从es查询索引文档数据的dsl语句名称，配置在dsl2ndSqlFile.xml中
            .setScrollLiveTime("10m") //scroll查询的scrollid有效期

//              .setSliceQuery(true)
//               .setSliceSize(5)
            .setQueryUrl("https2es/_search")
            .addSourceElasticsearch("elasticsearch.serverNames","default")
            .addElasticsearchProperty("default.elasticsearch.rest.hostNames","192.168.137.1:9200")
            .addElasticsearchProperty("default.elasticsearch.showTemplate","true")
            .addElasticsearchProperty("default.elasticUser","elastic")
            .addElasticsearchProperty("default.elasticPassword","changeme")
            .addElasticsearchProperty("default.elasticsearch.failAllContinue","true")
            .addElasticsearchProperty("default.http.timeoutSocket","60000")
            .addElasticsearchProperty("default.http.timeoutConnection","40000")
            .addElasticsearchProperty("default.http.connectionRequestTimeout","70000")
            .addElasticsearchProperty("default.http.maxTotal","200")
            .addElasticsearchProperty("default.http.defaultMaxPerRoute","100");//查询索引表demo中的文档数据

//          //添加dsl中需要用到的参数及参数值
//          exportBuilder.addParam("var1","v1")
//          .addParam("var2","v2")
//          .addParam("var3","v3");

      importBuilder.setInputConfig(elasticsearchInputConfig);
```

### 3.9.3 配置Elasticsearch检索dsl语句

dsl配置文件dsl2ndSqlFile.xml和对应的dsl语句名称案例：

```xml
<?xml version="1.0" encoding='UTF-8'?>
<properties>
    <description>
        <![CDATA[
            配置数据导入的dsl
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
                        ## 可以设置同步数据的过滤参数条件，通过addParam方法添加var1变量值，下面的条件已经被注释掉
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
                        ## 根据fullImport参数控制是否设置增量检索条件，true 全量检索 false增量检索，通过addParam方法添加fullImport变量值
                        #if(!$fullImport)
                        {   ## 增量检索范围，可以是时间范围，也可以是数字范围，这里采用的是数字增量字段
                            "range": {

                                #if($collecttime)
                                "collecttime": { ## 时间增量检索字段
                                    "gt": #[collecttime],
                                    "lte": #[collecttime__endTime]
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
       简单的scroll query案例，复杂的条件修改query dsl即可
       -->
    <property name="scrollQuery">
        <![CDATA[
         {
            "size":#[size], ## size变量对应于作业定义时设置的fetchSize参数
            @{queryCondition}
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
                "id": #[sliceId], ## 必须使用sliceId作为变量名称，框架自动填充变量值
                "max": #[sliceMax] ## 必须使用sliceMax作为变量名称，对应于作业定义时设置的sliceSize参数值
            },
            "size":#[size], ## size变量对应于作业定义时设置的fetchSize参数
            @{queryCondition}
        }
        ]]>
    </property>

</properties>
```

scrollQuery为本案例对应的dsl，scrollSliceQuery为slice导出需要用到的dsl，他们共用了条件片段queryCondition，内部基于bboss开源的另外一个elasticsearch rest java client项目从elasticsearch检索数据，该客户端使用参考文档：

https://esdoc.bbossgroups.com/#/development

## 3.10 定时任务配置

```java
    //定时任务配置，
      importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//              .setScheduleDate(date) //指定任务开始执行时间：日期
            .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
            .setPeriod(30000L); //每隔period毫秒执行，如果不设置，只执行一次
```

上面的配置表示：同步作业任务延迟1秒执行，每隔30秒执行一次。

## 3.11 任务上下文数据定义和获取

在一些特定场景下，避免任务执行过程中重复加载数据，需要在任务每次调度执行前加载一些任务执行过程中不会变化的数据,放入任务上下文TaskContext；任务执行过程中，直接从任务上下文中获取数据即可。例如：将每次任务执行的时间戳放入任务执行上下文。

通过TaskContext对象的addTaskData方法来添加上下文数据，通过TaskContext对象的getTaskData方法来获取任务上下文数据.

### 3.11.1  定义任务上下文数据

 任务上下文数据定义-通过CallInterceptor接口的preCall的来往TaskContext对象来添加 任务上下文数据

```java
@Override
public void preCall(TaskContext taskContext) {
   String formate = "yyyyMMddHHmmss";
   //HN_BOSS_TRADE00001_YYYYMMDDHHMM_000001.txt
   SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
   String time = dateFormat.format(new Date());
   taskContext.addTaskData("time",time);//定义任务执行时时间戳参数time
}
```

完整代码- 任务上下文数据定义

```java
       //设置任务执行拦截器，可以添加多个
      importBuilder.addCallInterceptor(new CallInterceptor() {
         @Override
         public void preCall(TaskContext taskContext) {
            String formate = "yyyyMMddHHmmss";
            //HN_BOSS_TRADE00001_YYYYMMDDHHMM_000001.txt
            SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
            String time = dateFormat.format(new Date());
            taskContext.addTaskData("time",time);//定义任务执行时时间戳参数time
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
    //设置任务执行拦截器结束，可以添加多个
```

### 3.11.2 获取任务上下文数据

在生成文件名称的接口方法中获取任务上下文数据

```java
FileOutputConfig.setFilenameGenerator(new FilenameGenerator() {
   @Override
   public String genName( TaskContext taskContext,int fileSeq) {

      String time = (String)taskContext.getTaskData("time");//获取任务执行时间戳参数time
      String _fileSeq = fileSeq+"";
      int t = 6 - _fileSeq.length();
      if(t > 0){
         String tmp = "";
         for(int i = 0; i < t; i ++){
            tmp += "0";
         }
         _fileSeq = tmp+_fileSeq;
      }



      return "HN_BOSS_TRADE"+_fileSeq + "_"+time +"_" + _fileSeq+".txt";
   }
});
```

在生成文件中的记录内容时获取任务上下文数据

```java
FileOutputConfig.setReocordGenerator(new ReocordGenerator() {
         @Override
         public void buildRecord(TaskContext context, CommonRecord record, Writer builder) {
            //SerialUtil.normalObject2json(record.getDatas(),builder);
            String data = (String)context.getTaskData("data");//获取全局参数
//          System.out.println(data);

         }
      });
```

在datarefactor方法中获取任务上下文数据

```java
/**
       * 重新设置es数据结构
       */
      importBuilder.setDataRefactor(new DataRefactor() {
         public void refactor(Context context) throws Exception  {

            String data = (String)context.getTaskContext().getTaskData("data");

         }
      });
```

### 3.11.3 获取生成的文件信息

通过开启：

```java
fileOutputConfig.setEnableGenFileInfoMetric(true);
```

可以将生成的文件信息（本地文件路径、ftp文件路径）添加到作业jobmetrics中，这样就可以在任务执行拦截器方法中获取任务生成的文件信息：

```java
/**
 * 文件导出时特定的文件类型任务上下文，包含了导出文件清单信息
 */
public void afterCall(TaskContext taskContext) {
	JobTaskMetrics taskMetrics = taskContext.getJobTaskMetrics();
	List<GenFileInfo> genFileInfos = (List<GenFileInfo>)taskMetrics.readJobExecutorData(FileOutputConfig.JobExecutorDatas_genFileInfos);
}
```



## 3.12 设置IP地址信息库地址

我们通过以下代码设置IP地址信息库地址：

```java
//设置ip地址信息库地址，配置参考文档
		importBuilder.setGeoipDatabase("d:/geolite2/GeoLite2-City.mmdb");
		importBuilder.setGeoipAsnDatabase("d:/geolite2/GeoLite2-ASN.mmdb");
		importBuilder.setGeoip2regionDatabase("d:/geolite2/ip2region.db");
```

IP地址库配置详细参考文档：[设置IP地址信息库地址](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2311-ip-%e5%9c%b0%e5%8c%ba%e8%bf%90%e8%90%a5%e5%95%86%e7%bb%8f%e7%ba%ac%e5%ba%a6%e5%9d%90%e6%a0%87%e8%bd%ac%e6%8d%a2)

## 3.13 调整记录数据内容

可以通过datarefactor接口调整记录数据内容，示例代码如下：

```java
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
            String data = (String)context.getTaskContext().getTaskData("data");
//          System.out.println(data);

//          context.addFieldValue("author","duoduo");//将会覆盖全局设置的author变量
            context.addFieldValue("title","解放");
            context.addFieldValue("subtitle","小康");

//          context.addIgnoreFieldMapping("title");
            //上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//          context.addIgnoreFieldMapping("author");

//          //修改字段名称title为新名称newTitle，并且修改字段的值
//          context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
            /**
			* 获取ip对应的运营商和区域信息，字段logVisitorial值对应了一个ip地址
			*/
             IpInfo ipInfo = (IpInfo) context.getIpInfo("logVisitorial");
             if(ipInfo != null)
                 context.addFieldValue("ipinfo", ipInfo);
             else{
                 context.addFieldValue("ipinfo", "");
             }
            DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
//          Date optime = context.getDateValue("LOG_OPERTIME",dateFormat);
//          context.addFieldValue("logOpertime",optime);
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
```

## 3.14 增量同步配置

```java
       //增量配置开始
      importBuilder.setLastValueColumn("collecttime");//手动指定日期增量查询字段变量名称
      importBuilder.setFromFirst(true);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
      //setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
      importBuilder.setLastValueStorePath("es2fileftp_batchsplitimport");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//    importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
      importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
      // 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
      //指定增量同步的起始时间，默认中1970-01-01 00:00:00开始
//    importBuilder.setLastValue(new Date());
      //增量配置结束
```

## 3.15 并行同步配置

```java
importBuilder.setParallel(false);//设置为多线程并行批量导入,true并行，false串行
importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
```

## 3.16 同步任务日志打印开关

```java
importBuilder.setPrintTaskLog(true);// true打印，false不打印
```

## 3.17  同步作业执行

```java
/**
 * 启动es数据导入文件并上传sftp/ftp作业
 */
DataStream dataStream = importBuilder.builder();
dataStream.execute();//启动同步作业
```

## 3.18 生成excel文件

通过ExcelFileOutputConfig配置文件来设置excel导出配置，例如：

```java
     //配置excel列与来源字段映射关系、列对应的中文标题（如果没有设置，默认采用字段名称作为excel列标题）
       ExcelFileOutputConfig fileFtpOupputConfig = new ExcelFileOutputConfig();
             fileFtpOupputConfig.setTitle("师大2021年新生医保（2021年）申报名单");
             fileFtpOupputConfig.setSheetName("2021年新生医保申报单");
             fileFtpOupputConfig.setFlushRows(10000);
             fileFtpOupputConfig
                     .addCellMapping(0,"shebao_org","社保经办机构（建议填写）");
     //                .addCellMapping(1,"person_no","人员编号");
     //                .addCellMapping(2,"name","*姓名")
     //                .addCellMapping(3,"cert_type","*证件类型")
     //
     //                .addCellMapping(4,"cert_no","*证件号码","")
     //                .addCellMapping(5,"zhs_item","*征收项目")
     //
     //                .addCellMapping(6,"zhs_class","*征收品目")
     //                .addCellMapping(7,"zhs_sub_class","征收子目")
     //                .addCellMapping(8,"zhs_year","*缴费年度","2022")
     //                .addCellMapping(9,"zhs_level","*缴费档次","1");
             fileFtpOupputConfig.setFileDir("D:\\excelfiles\\hebin");//数据生成目录
     
             fileFtpOupputConfig.setFilenameGenerator(new FilenameGenerator() {
                 @Override
                 public String genName(TaskContext taskContext, int fileSeq) {
     
     
                     return "师大2021年新生医保（2021年）申报名单-合并.xlsx";
                 }
             });
     
             importBuilder.setOutputConfig(fileFtpOupputConfig);
```

只生成excel文件

```java
 ImportBuilder importBuilder = new ImportBuilder();
        importBuilder
                .setBatchSize(500)
                .setFetchSize(1000)
        ;


        ExcelFileOutputConfig fileFtpOupputConfig = new ExcelFileOutputConfig();
        fileFtpOupputConfig.setTitle("师大2021年新生医保（2021年）申报名单");
        fileFtpOupputConfig.setSheetName("2021年新生医保申报单");

        fileFtpOupputConfig.addCellMapping(0,"shebao_org","社保经办机构（建议填写）")
                .addCellMapping(1,"person_no","人员编号")
                .addCellMapping(2,"name","*姓名")
                .addCellMapping(3,"cert_type","*证件类型")

                .addCellMapping(4,"cert_no","*证件号码","")
                .addCellMapping(5,"zhs_item","*征收项目")

                .addCellMapping(6,"zhs_class","*征收品目")
                .addCellMapping(7,"zhs_sub_class","征收子目")
                .addCellMapping(8,"zhs_year","*缴费年度","2022")
                .addCellMapping(9,"zhs_level","*缴费档次","1");
        fileFtpOupputConfig.setFileDir("D:\\excelfiles\\hebin");//数据生成目录

        fileFtpOupputConfig.setFilenameGenerator(new FilenameGenerator() {
            @Override
            public String genName(TaskContext taskContext, int fileSeq) {


                return "师大2021年新生医保（2021年）申报名单-合并.xlsx";
            }
        });

        importBuilder.setOutputConfig(fileFtpOupputConfig);
//		importBuilder.setIncreamentEndOffset(300);//单位秒
        //vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27
        DBInputConfig dbInputConfig= new DBInputConfig();
        dbInputConfig
                .setSqlFilepath("sql.xml")
                .setSqlName("querynewmanrequests");
        importBuilder.setInputConfig(dbInputConfig);

//        //定时任务配置，
//        importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
////					 .setScheduleDate(date) //指定任务开始执行时间：日期
//                .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
//                .setPeriod(30000L); //每隔period毫秒执行，如果不设置，只执行一次
//        //定时任务配置结束

        //设置任务执行拦截器，可以添加多个
        importBuilder.addCallInterceptor(new CallInterceptor() {
            @Override
            public void preCall(TaskContext taskContext) {


            }

            @Override
            public void afterCall(TaskContext taskContext) {

            }

            @Override
            public void throwException(TaskContext taskContext, Exception e) {
                System.out.println("throwException 1");
            }
        });

        /**
         * 重新设置es数据结构
         */
        importBuilder.setDataRefactor(new DataRefactor() {
            public void refactor(Context context) throws Exception {

            }
        });
        //映射和转换配置结束

        /**
         * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
         */
        importBuilder.setParallel(false);//设置为多线程并行批量导入,false串行
        importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
        importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
        importBuilder.setPrintTaskLog(true);

        /**
         * 执行db数据导入csv操作
         */
        DataStream dataStream = importBuilder.builder();
        dataStream.execute();//执行导入操作
        logger.info("job started.");
```

生成excel文件并上传ftp服务器：

```java
 ImportBuilder importBuilder = new ImportBuilder();
         importBuilder
                 .setBatchSize(500)
                 .setFetchSize(1000);
 
 
         ExcelFileOutputConfig fileFtpOupputConfig = new ExcelFileOutputConfig();
 
         String ftpIp = CommonLauncher.getProperty("ftpIP","127.0.0.1");//同时指定了默认值
         FtpOutConfig ftpOutConfig = new FtpOutConfig();
         fileFtpOupputConfig.setFtpOutConfig(ftpOutConfig);
         ftpOutConfig.setBackupSuccessFiles(true);
         ftpOutConfig.setTransferEmptyFiles(true);
         ftpOutConfig.setFtpIP(ftpIp);
 
         ftpOutConfig.setFtpPort(5322);
         ftpOutConfig.setFtpUser("1111");
         ftpOutConfig.setFtpPassword("1111@123");
         ftpOutConfig.setRemoteFileDir("/home/ecs/failLog");
         ftpOutConfig.setKeepAliveTimeout(100000);
         ftpOutConfig.setFailedFileResendInterval(300000);
 
         fileFtpOupputConfig.setTitle("师大2021年新生医保（2021年）申报名单");
         fileFtpOupputConfig.setSheetName("2021年新生医保申报单");
         //配置excel列与来源字段映射关系、列对应的中文标题（如果没有设置，默认采用字段名称作为excel列标题）
         fileFtpOupputConfig.addCellMapping(0,"shebao_org","社保经办机构（建议填写）")
                 .addCellMapping(1,"person_no","人员编号")
                 .addCellMapping(2,"name","*姓名")
                 .addCellMapping(3,"cert_type","*证件类型")
 
                 .addCellMapping(4,"cert_no","*证件号码","")
                 .addCellMapping(5,"zhs_item","*征收项目")
 
                 .addCellMapping(6,"zhs_class","*征收品目")
                 .addCellMapping(7,"zhs_sub_class","征收子目")
 
                 .addCellMapping(8,"zhs_year","*缴费年度","2022")//指定了列默认值
                 .addCellMapping(9,"zhs_level","*缴费档次","1");//指定了列默认值
         fileFtpOupputConfig.setFileDir("D:\\excelfiles\\hebin");//数据生成目录
 
         fileFtpOupputConfig.setFilenameGenerator(new FilenameGenerator() {
             @Override
             public String genName(TaskContext taskContext, int fileSeq) {
 
 
                 return "师大2021年新生医保（2021年）申报名单-合并"+fileSeq+".xlsx";
             }
         });
 
         importBuilder.setOutputConfig(fileFtpOupputConfig);
 //		importBuilder.setIncreamentEndOffset(300);//单位秒
         //vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27
         DBInputConfig dbInputConfig= new DBInputConfig();
         dbInputConfig
                 .setSqlFilepath("sql.xml")
                 .setSqlName("querynewmanrequests");
         importBuilder.setInputConfig(dbInputConfig);
 
 //        //定时任务配置，
 //        importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
 ////					 .setScheduleDate(date) //指定任务开始执行时间：日期
 //                .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
 //                .setPeriod(30000L); //每隔period毫秒执行，如果不设置，只执行一次
 //        //定时任务配置结束
 
         //设置任务执行拦截器，可以添加多个
         importBuilder.addCallInterceptor(new CallInterceptor() {
             @Override
             public void preCall(TaskContext taskContext) {
 
 
             }
 
             @Override
             public void afterCall(TaskContext taskContext) {
 
             }
 
             @Override
             public void throwException(TaskContext taskContext, Exception e) {
                 System.out.println("throwException 1");
             }
         });
 
         /**
          * 重新设置es数据结构
          */
         importBuilder.setDataRefactor(new DataRefactor() {
             public void refactor(Context context) throws Exception {
 
             }
         });
         //映射和转换配置结束
 
         /**
          * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
          */
         importBuilder.setParallel(false);//设置为多线程并行批量导入,false串行
         importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
         importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
         importBuilder.setPrintTaskLog(true);
 
         /**
          * 执行db数据导入csv操作
          */
         DataStream dataStream = importBuilder.builder();
         dataStream.execute();//执行导入操作
         logger.info("job started.");
```

## 3.19 文件切割配置

如果单次调度采集的数据量非常大，可以按照记录数（每个文件最多保存的记录数量）切割成生成多个文件，文件切割非常简单，通过FileOutputConfig.setMaxFileRecordSize方法设置每个文件最多保存的记录数量即可，示例代码如下：

```java
  fileOutputConfig.setMaxFileRecordSize(1000);//设置切割文件记录数，每千条记录生成一个文件
```

除了设置文件最多保存的记录数量，还需要设置切割生成的文件的文件名称规则，一般采用规则：

```
“文件名”+“_时间戳”+“_切割序号”
```

可以通过FileOutputConfig对象的setFilenameGenerator方法设置文件名称生成接口FilenameGenerator，示例代码如下：

```java
 //自定义文件名称
      FileOutputConfig.setFilenameGenerator(new FilenameGenerator() {
         @Override
         public String genName( TaskContext taskContext,int fileSeq) {
                //HN_BOSS_TRADE_YYYYMMDDHHMM_1.txt
              String formate = "yyyyMMddHHmmss";

   			SimpleDateFormat dateFormat = new SimpleDateFormat(formate);	
             //获取本次任务执行时间戳
             String time = dateFormat.format(taskContext.getJobTaskMetrics().getJobStartTime());
		    //fileSeq为切割文件时的文件递增序号
            return "HN_BOSS_TRADE_"+time +"_" + _fileSeq+".txt";
         }
      });
```

接口方法genName带有两个参数：

**TaskContext taskContext**, 任务上下文对象，包含任务执行过程中需要的上下文数据，比如任务执行时间戳、其他任务执行过程中需要用到的数据

**int fileSeq**   文件序号，从1开始，自动递增，如果指定了每个文件保存的最大记录数，fileSeq就会被用到文件名称中，用来区分各种文件



## 3.20 同步作业调试、发布和部署运行

下载elasticsearcch/database-sftp/ftp同步作业[样板工程](https://github.com/bbossgroups/elasticsearch-file2ftp)，定义好自己的作业后，可以按照以下文档调试、发布和部署运行同步作业

作业调试：[参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_8-%e6%b5%8b%e8%af%95%e4%bb%a5%e5%8f%8a%e8%b0%83%e8%af%95%e5%90%8c%e6%ad%a5%e4%bb%a3%e7%a0%81)

查看任务执行详细日志: [参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_9-%e6%9f%a5%e7%9c%8b%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e8%af%a6%e7%bb%86%e6%97%a5%e5%bf%97)

作业运行jvm内存配置：[参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_11-%e4%bd%9c%e4%b8%9a%e8%bf%90%e8%a1%8cjvm%e5%86%85%e5%ad%98%e9%85%8d%e7%bd%ae)

作业发布和部署：[参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_12-%e5%8f%91%e5%b8%83%e7%89%88%e6%9c%ac)

## 3.21 开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />


交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">

