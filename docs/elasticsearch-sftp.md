# Elasticsearch/DB到SFTP/FTP数据同步

通过bboss数据同步工具，可以非常高效快速方便地将Elasticsearch和Database中的数据实时导出到文件并上传到SFTP/FTP服务器，本文通过案例来详细介绍。

![](images\datasyn.png)

# 1.案例源码工程

https://github.com/bbossgroups/elasticsearch-file2ftp

# 2.案例功能说明

1. 串行将数据导出到文件并上传ftp和sftp

2. 串行批量将数据导出到文件并上传ftp和sftp
3. 并行将数据批量导出到文件并上传ftp和sftp

特别关注点

除了bboss同步工具通用特性（增量/全量同步、异步/同步、增删改查同步），需额外说明一下本案例中特定的特色：

1. 支持上传失败文件重传功能
2. 支持上传成功文件备份功能
3. 支持按记录条数切割生成文件
4. 优雅解决elasticsearch异步延迟写入特性可能导致增量同步遗漏步数据问题

本文只介绍elasticsearch数据同步上传到sftp案例

https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpBatchSplitFileDemo.java

其他案例直接查看源码：

elasticsearch数据同步上传到ftp案例代码地址

https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ftp/ES2FileFtpBatchDemo.java

数据库同步上传到sftp案例代码地址

https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2FileFtpDemo.java

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
import org.frameworkset.tran.output.fileftp.FileFtpOupputConfig;
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
      ES2FileFtpExportBuilder importBuilder = new ES2FileFtpExportBuilder();
      importBuilder.setBatchSize(500).setFetchSize(1000);
      String ftpIp = CommonLauncher.getProperty("ftpIP","10.13.6.127");//同时指定了默认值
      FileFtpOupputConfig fileFtpOupputConfig = new FileFtpOupputConfig();

      fileFtpOupputConfig.setFtpIP(ftpIp);
      fileFtpOupputConfig.setFileDir("D:\\workdir");
      fileFtpOupputConfig.setFtpPort(5322);
      fileFtpOupputConfig.addHostKeyVerifier("2a:da:5a:6a:cf:7d:65:e5:ac:ff:d3:73:7f:2c:55:c9");
      fileFtpOupputConfig.setFtpUser("ecs");
      fileFtpOupputConfig.setFtpPassword("ecs@123");
      fileFtpOupputConfig.setRemoteFileDir("/home/ecs/failLog");
      fileFtpOupputConfig.setKeepAliveTimeout(100000);
      fileFtpOupputConfig.setTransferEmptyFiles(true);
      fileFtpOupputConfig.setFailedFileResendInterval(-1);
      fileFtpOupputConfig.setBackupSuccessFiles(true);

      fileFtpOupputConfig.setSuccessFilesCleanInterval(5000);
      fileFtpOupputConfig.setFileLiveTime(86400);//设置上传成功文件备份保留时间，默认2天
      fileFtpOupputConfig.setMaxFileRecordSize(1000);//每千条记录生成一个文件
      //自定义文件名称
      fileFtpOupputConfig.setFilenameGenerator(new FilenameGenerator() {
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
      fileFtpOupputConfig.setReocordGenerator(new ReocordGenerator() {
         @Override
         public void buildRecord(Context taskContext, CommonRecord record, Writer builder) {
             //直接将记录按照json格式输出到文本文件中
            SerialUtil.normalObject2json(record.getDatas(),//获取记录中的字段数据
                                         builder);
            String data = (String)taskContext.getTaskContext().getTaskData("data");//从任务上下文中获取本次任务执行前设置时间戳
//          System.out.println(data);

         }
      });
      importBuilder.setFileFtpOupputConfig(fileFtpOupputConfig);
      importBuilder.setIncreamentEndOffset(300);//单位秒
      //vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27
      importBuilder
            .setDsl2ndSqlFile("dsl2ndSqlFile.xml")
            .setDslName("scrollQuery")
            .setScrollLiveTime("10m")
//          .setSliceQuery(true)
//          .setSliceSize(5)
//          .setQueryUrl("dbdemo/_search")
            //通过简单的示例，演示根据实间范围计算queryUrl,以当前时间为截止时间，后续版本6.2.8将增加lastEndtime参数作为截止时间（在设置了IncreamentEndOffset情况下有值）
			.setQueryUrlFunction((TaskContext taskContext,Date lastStartTime,Date lastEndTime)->{
					String formate = "yyyy.MM.dd";
					SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
					String startTime = dateFormat.format(lastTime);
					Date endTime = new Date();
					String endTimeStr = dateFormat.format(endTime);
					return "dbdemo-"+startTime+ ",dbdemo-"+endTimeStr+"/_search";
//					return "vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27/_search";
			})
            .addParam("fullImport",false)
//          //添加dsl中需要用到的参数及参数值
            .addParam("var1","v1")
            .addParam("var2","v2")
            .addParam("var3","v3");

      //定时任务配置，
      importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//              .setScheduleDate(date) //指定任务开始执行时间：日期
            .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
            .setPeriod(30000L); //每隔period毫秒执行，如果不设置，只执行一次
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
//    //设置任务执行拦截器结束，可以添加多个
      //增量配置开始
      importBuilder.setLastValueColumn("collecttime");//手动指定日期增量查询字段变量名称
      importBuilder.setFromFirst(true);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
      //setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
      importBuilder.setLastValueStorePath("es2fileftp_batchsplitimport");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//    importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
      importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
      // 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
//    importBuilder.setLastValue(new Date());
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
      importBuilder.addFieldValue("author","张无忌");
//    importBuilder.addFieldMapping("operModule","OPER_MODULE");
//    importBuilder.addFieldMapping("logContent","LOG_CONTENT");
//    importBuilder.addFieldMapping("logOperuser","LOG_OPERUSER");


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
             * 获取ip对应的运营商和区域信息
             */
            Map ipInfo = (Map)context.getValue("ipInfo");
            if(ipInfo != null)
               context.addFieldValue("ipinfo", SimpleStringUtil.object2json(ipInfo));
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
      //映射和转换配置结束

      /**
       * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
       */
      importBuilder.setParallel(false);//设置为多线程并行批量导入,false串行
      importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
      importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
      importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
      importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回

      importBuilder.setPrintTaskLog(true);
    
      /**
       * 执行es数据导出和sftp上传作业
       */
      DataStream dataStream = importBuilder.builder();
      dataStream.execute();//执行导入操作
   }
}
```

## 3.2 作业配置-ES2FileFtpExportBuilder 

  通过org.frameworkset.tran.input.fileftp.es.ES2FileFtpExportBuilder类来构建导出elasticsearch数据到sftp/ftp文件上传同步作业。bboss还提供了org.frameworkset.tran.input.fileftp.db.DB2FileFtpImportBuilder类来构建导出各种关系数据库（oracle、mysql等）到文件并上传到sftp/ftp服务器同步作业。

## 3.3 设置批量写文件的记录条数-BatchSize和批量从elasticsearch获取记录条数fetchSize

 importBuilder.setBatchSize(500).setFetchSize(1000);

## 3.4 SFTP/FTP配置

通过FileFtpOupputConfig类来设置sftp和ftp上传的的相关配置：

| 参数名称                  | 描述                                                         | 默认值               | 对应协议 |
| ------------------------- | ------------------------------------------------------------ | -------------------- | -------- |
| ftpIP                     | 必填，String类型，ftp/sftp服务器ip地址                       | 空                   | ftp/sftp |
| ftpPort                   | 必填，int类型，ftp/sftp服务器端口                            | 空                   | ftp/sftp |
| ftpUser                   | 必填，String类型，ftp/sftp服务器用户账号                     | 空                   | ftp/sftp |
| ftpPassword               | 必填，String类型，ftp/sftp服务器用户口令                     | 空                   | ftp/sftp |
| remoteFileDir             | 必填，String类型，指定ftp/sftp服务器目录地址，用户存放上传的文件 | 空                   | ftp/sftp |
| fileDir                   | 必填，String类型，指定导出数据生成的文件存放的本地目录、上传成功文件备份目录、上传失败文件存放目录（便于定时重传） | 空                   | ftp/sftp |
| keepAliveTimeout          | 必填，long类型，单位毫秒，ftp/sftp通讯协议连接存活超时时间   | 0                    | ftp/sftp |
| transferEmptyFiles        | 必填，boolean类型，是否上传空文件，true上传，false不上传     | true                 | ftp/sftp |
| backupSuccessFiles        | 必填，boolean类型，是备份上传成功文件，true备份，false不备份 | false                | ftp/sftp |
| failedFileResendInterval  | 必填，long类型，失败文件重传时间间隔，-1或者0不重传，单位：毫秒 | 5000                 | ftp/sftp |
| successFilesCleanInterval | 必填，long类型，扫描需要清理上传成功文件时间间隔，单位：毫秒 | 5000                 | ftp/sftp |
| fileLiveTime              | 必填，int类型，上传成功文件保留时间，单位：秒                | 2天                  | ftp/sftp |
| maxFileRecordSize         | 必填，int类型，切割文件时，指定每个文件保存的记录条数，>0时启用文件切割机制；按记录条数切割文件机制对并行导出数据不起作用 | -1                   | ftp/sftp |
| filenameGenerator         | 必填，FilenameGenerator接口类型，用于自定义生成文件的名称    | 无                   | ftp/sftp |
| hostKeyVerifier           | 必填，适用于sftp协议，如果sftp协议需要指定，可以先不设置，然后将运行报错日志中打印出来字符串设置即可 | 无                   | sftp     |
| reocordGenerator          | 可选，ReocordGenerator接口类型，用来定义生成的记录格式，如果不设置默认为json格式 | JsonReocordGenerator | ftp/sftp |

示例代码如下：

```java
String ftpIp = CommonLauncher.getProperty("ftpIP","10.13.6.127");//同时指定了默认值
      FileFtpOupputConfig fileFtpOupputConfig = new FileFtpOupputConfig();

      fileFtpOupputConfig.setFtpIP(ftpIp);
      fileFtpOupputConfig.setFileDir("D:\\workdir");
      fileFtpOupputConfig.setFtpPort(5322);
      fileFtpOupputConfig.addHostKeyVerifier("2a:da:5a:6a:cf:7d:65:e5:ac:ff:d3:73:7f:2c:55:c9");
      fileFtpOupputConfig.setFtpUser("ecs");
      fileFtpOupputConfig.setFtpPassword("ecs@123");
      fileFtpOupputConfig.setRemoteFileDir("/home/ecs/failLog");
      fileFtpOupputConfig.setKeepAliveTimeout(100000);
      fileFtpOupputConfig.setTransferEmptyFiles(true);
      fileFtpOupputConfig.setFailedFileResendInterval(-1);
      fileFtpOupputConfig.setBackupSuccessFiles(true);

      fileFtpOupputConfig.setSuccessFilesCleanInterval(5000);
      fileFtpOupputConfig.setFileLiveTime(86400);//设置上传成功文件备份保留时间，默认2天
      fileFtpOupputConfig.setMaxFileRecordSize(1000);//每千条记录生成一个文件
      //自定义文件名称
      fileFtpOupputConfig.setFilenameGenerator(new FilenameGenerator() {
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
      fileFtpOupputConfig.setReocordGenerator(new ReocordGenerator() {
         @Override
         public void buildRecord(Context taskContext, CommonRecord record, Writer builder) {
             //直接将记录按照json格式输出到文本文件中
            SerialUtil.normalObject2json(record.getDatas(),//获取记录中的字段数据
                                         builder);
            String data = (String)taskContext.getTaskContext().getTaskData("data");//从任务上下文中获取本次任务执行前设置时间戳
//          System.out.println(data);

         }
      });
      importBuilder.setFileFtpOupputConfig(fileFtpOupputConfig);
```

## 3.5 elasticsearch增量导出截止时间偏移量设置

elasticsearch增量导出截止时间偏移量设置-IncreamentEndOffset，由于elasticsearch异步写入数据的特性，如果采用原有的增量时间戳机制（起始时间>lastImporttime，没有截止时间）,会导致遗漏部分未落盘数据，因此需要指定基于当前时间往前偏移IncreamentEndOffset对应的时间作为数据导出截止时间，单位：秒

示例如下：

```java
importBuilder.setIncreamentEndOffset(300);//单位秒，同步从上次同步截止时间当前时间前5分钟的数据，下次继续从上次截止时间开始同步数据
```

## 3.6 从elasticsearch检索数据配置

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

配置代码示例：

```java
importBuilder
            .setDsl2ndSqlFile("dsl2ndSqlFile.xml")
            .setDslName("scrollQuery")
            .setScrollLiveTime("10m")
//          .setSliceQuery(true)
//          .setSliceSize(5)
//          .setQueryUrl("dbdemo/_search")
            //通过简单的示例，演示根据实间范围计算queryUrl,以当前时间为截止时间，后续版本6.2.8将增加lastEndtime参数作为截止时间（在设置了IncreamentEndOffset情况下有值）
			.setQueryUrlFunction((TaskContext taskContext,Date lastStartTime,Date lastEndTime)->{
					String formate = "yyyy.MM.dd";
					SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
					String startTime = dateFormat.format(lastTime);
					Date endTime = new Date();
					String endTimeStr = dateFormat.format(endTime);
					return "dbdemo-"+startTime+ ",dbdemo-"+endTimeStr+"/_search";
//					return "vops-chbizcollect-2020.11.26,vops-chbizcollect-2020.11.27/_search";
			})
            .addParam("fullImport",false)
//          //添加dsl中需要用到的参数及参数值
            .addParam("var1","v1")
            .addParam("var2","v2")
            .addParam("var3","v3");
      //指定elasticsearch数据源名称
      importBuilder.setSourceElasticsearch("default");
```

## 3.7 定时任务配置

```java
    //定时任务配置，
      importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//              .setScheduleDate(date) //指定任务开始执行时间：日期
            .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
            .setPeriod(30000L); //每隔period毫秒执行，如果不设置，只执行一次
```

## 3.8 定义和获取任务每次调度执行时需要的全局参数

在一些特定场景下需要在任务每次调度执行前时定义全局参数，并在任务执行时获取全局参数。

通过TaskContext对象的addTaskData方法来添加全局参数，通过TaskContext对象的getTaskData方法来获取全局参数.

### 3.8.1 全局参数定义

全局参数定义-通过CallInterceptor接口的preCall的来往TaskContext对象来添加全局参数

```java
@Override
public void preCall(TaskContext taskContext) {
   String formate = "yyyyMMddHHmmss";
   //HN_BOSS_TRADE00001_YYYYMMDDHHMM_000001.txt
   SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
   String time = dateFormat.format(new Date());
   taskContext.addTaskData("time",time);//定义全局时间戳参数time
}
```

完整代码-全局参数定义

```java
       //设置任务执行拦截器，可以添加多个
      importBuilder.addCallInterceptor(new CallInterceptor() {
         @Override
         public void preCall(TaskContext taskContext) {
            String formate = "yyyyMMddHHmmss";
            //HN_BOSS_TRADE00001_YYYYMMDDHHMM_000001.txt
            SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
            String time = dateFormat.format(new Date());
            taskContext.addTaskData("time",time);//定义全局时间戳参数time
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

### 3.8.2 全局参数获取

在生成文件名称的接口方法中获取全局参数

```java
fileFtpOupputConfig.setFilenameGenerator(new FilenameGenerator() {
   @Override
   public String genName( TaskContext taskContext,int fileSeq) {

      String time = (String)taskContext.getTaskData("time");//获取全局时间戳参数time
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

在生成文件中的记录内容时获取全局参数

```java
fileFtpOupputConfig.setReocordGenerator(new ReocordGenerator() {
         @Override
         public void buildRecord(Context context, CommonRecord record, Writer builder) {
            //SerialUtil.normalObject2json(record.getDatas(),builder);
            String data = (String)context.getTaskContext().getTaskData("data");//获取全局参数
//          System.out.println(data);

         }
      });
```

在datarefactor方法中获取全局参数

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

## 3.9 调整记录数据内容

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
             * 获取ip对应的运营商和区域信息
             */
            Map ipInfo = (Map)context.getValue("ipInfo");
            if(ipInfo != null)
               context.addFieldValue("ipinfo", SimpleStringUtil.object2json(ipInfo));
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

## 3.10 增量同步配置

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

## 3.11 并行同步配置

```java
importBuilder.setParallel(false);//设置为多线程并行批量导入,true并行，false串行
importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
```

## 3.12 同步任务日志打印开关

```java
importBuilder.setPrintTaskLog(true);// true打印，false不打印
```

## 3.13  同步作业执行

```java
/**
 * 启动es数据导入文件并上传sftp/ftp作业
 */
DataStream dataStream = importBuilder.builder();
dataStream.execute();//启动同步作业
```

## 3.14 同步作业调试、发布和部署运行

下载elasticsearcch/database-sftp/ftp同步作业[样板工程](https://github.com/bbossgroups/elasticsearch-file2ftp)，定义好自己的作业后，可以按照以下文档调试、发布和部署运行同步作业

作业调试：[参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_8-%e6%b5%8b%e8%af%95%e4%bb%a5%e5%8f%8a%e8%b0%83%e8%af%95%e5%90%8c%e6%ad%a5%e4%bb%a3%e7%a0%81)

查看任务执行详细日志: [参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_9-%e6%9f%a5%e7%9c%8b%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e8%af%a6%e7%bb%86%e6%97%a5%e5%bf%97)

作业运行jvm内存配置：[参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_11-%e4%bd%9c%e4%b8%9a%e8%bf%90%e8%a1%8cjvm%e5%86%85%e5%ad%98%e9%85%8d%e7%bd%ae)

作业发布和部署：[参考文档](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_12-%e5%8f%91%e5%b8%83%e7%89%88%e6%9c%ac)

# 3.15开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">