# bboss数据采集ETL案例大全

# 1.bboss数据采集ETL工具功能架构	

​		bboss数据同步可以方便地实现多种数据源之间的数据同步功能，**支持增、删、改数据同步**，支持各种主流数据库、各种es版本以及日志文件数据采集和同步、加工处理，支持从kafka接收数据；经过加工处理的数据亦可以发送到kafka；可以将加工后的数据写入File并上传到ftp/sftp服务器。

![img](images/datasyn.png)

​		通过bboss，可以非常方便地采集database/mongodb/Elasticsearch/kafka/hbase/日志文件源数据，经过数据转换处理后，再推送到目标库elasticsearch/database/file/ftp/kafka/dummy/logger。

​		bboss另一个显著的特色就是直接基于java语言来编写数据同步作业程序，基于强大的java语言和第三方工具包，能够非常方便地加工和处理需要同步的源数据，然后将最终的数据保存到目标库（Elasticsearch或者数据库）；同时也可以非常方便地在idea或者eclipse中调试和运行同步作业程序，调试无误后，通过bboss提供的gradle脚本，即可构建和发布出可部署到生产环境的同步作业包。因此，对广大的java程序员来说，bboss无疑是一个轻易快速上手的数据同步利器。

​		bboss同步功能非常丰富，为了方便快速使用上手bboss，本文将各种数据同步案例呈现给大家，可以根据实际情况选用合适的案例。

下面以目标库为主线，列举数据采集同步案例
# 2.导入Elasticsearch案例

## 2.1 Database到Elasticsearch数据同步
### 案例1 简单定时同步案例-数字增量/时间戳增量/全量同步
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java)

### 案例2 基于xml文件管理采集sql同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DbdemoFromSQLFile.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DbdemoFromSQLFile.java)

### 案例3 通过ClientOptions自定义Elasticsearch7客户端参数同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DbClientOptionsDemo7.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DbClientOptionsDemo7.java)

### 案例4 通过ClientOptions自定义Elasticsearch7以下版本客户端参数同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DbClientOptionsDemo.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DbClientOptionsDemo.java)

### 案例5 通过ESOutputConfig设置目标Elasticsearch同步参数案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/NewDbdemoFromSQLFile.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/NewDbdemoFromSQLFile.java)

### 案例6 基于数字字段增量同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/CMSDocumentImport.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/CMSDocumentImport.java)

### 案例7 通过Quartz调度定时-全量同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzImportTask.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzImportTask.java)

### 案例8 通过Quartz调度定时-基于时间字段增量同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzTimestampImportTask.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzTimestampImportTask.java)

### 案例9 原生quartz作业调度-基于时间字段增量同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/quartz/ImportDataJob.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/quartz/ImportDataJob.java)
### 案例10 基于spring-boot-web管理作业启动和停止数据同步案例
[github地址](https://github.com/bbossgroups/springboot-elasticsearch-webservice/blob/master/src/main/java/com/example/esbboss/service/DataTran.java)

[gitee地址](https://gitee.com/bboss/springboot-elasticsearch/blob/master/src/main/java/com/example/esbboss/service/DataTran.java)
### 案例11 基于xxl-job分布式调度引擎调度的数据同步案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-xxjob/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/jobhandler/XXJobImportTask.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-xxjob/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/jobhandler/XXJobImportTask.java)

### 案例12 基于apollo管理配置的同步作业工程案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-tool-apollo)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool-apollo)

### 案例13 基于apollo管理配置的xxl-job调度同步作业工程案例
[github地址](https://github.com/bbossgroups/db-elasticsearch-xxjob-apollo)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-xxjob-apollo)

## 2.2 接收Kafka数据写入Elasticsearch
[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2ESdemo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2ESdemo.java)


## 2.3 采集日志文件并写入Elasticsearch
[github地址](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2ESDemo.java)

[gitee地址](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2ESDemo.java)

## 2.4 Mongodb到Elasticsearch数据同步
### 案例1 基于时间戳增量同步案例
[github地址](https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2ESdemo.java)

[gitee地址](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2ESdemo.java)
### 案例2 基于时间戳增量同步案例-自定义mongodb检索数据条件
[github地址](https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2ES.java)

[gitee地址](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2ES.java)
### 案例3 基于quartz调度同步案例
[github地址](https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzImportTask.java)

[gitee地址](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzImportTask.java)
### 案例4 基于xxl-job同步案例
[github地址](https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/XXJobMongodb2ESImportTask.java)

[gitee地址](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/XXJobMongodb2ESImportTask.java)

## 2.5 Hbase到Elasticsearch数据同步
### 案例1 hbase定时全量同步案例
[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemo.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemo.java)
### 案例2 hbase定时全量同步案例-自定义hbase filter检索数据条件
[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemoWithFilter.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemoWithFilter.java)
### 案例3 基于时间戳hbase定时增量同步案例-自定义hbase filter检索数据条件
[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESScrollTimestampDemo.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESScrollTimestampDemo.java)
### 案例4 基于quartz调度同步案例-自定义hbase filter检索数据条件
[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzHBase2ESImportTask.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzHBase2ESImportTask.java)


## 2.6 Elasticsearch到Elasticsearch数据同步
### 案例1 全量同步案例
[github地址](https://github.com/bbossgroups/elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2ESScrollAllTimestampDemo.java)

[gitee地址](https://gitee.com/bboss//elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2ESScrollAllTimestampDemo.java)
### 案例2 基于时间戳定时增量同步案例
[github地址](https://github.com/bbossgroups/elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2ESScrollTimestampDemo.java)

[gitee地址](https://gitee.com/bboss//elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2ESScrollTimestampDemo.java)
### 案例3 基于ESOutputConfig配置目标Elasticsearch集群定时增量同步案例
[github地址](https://github.com/bbossgroups/elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/NewES2ESScrollTimestampDemo.java)

[gitee地址](https://gitee.com/bboss//elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/NewES2ESScrollTimestampDemo.java)
### 案例4 基于quartz调度的增量同步案例
[github地址](https://github.com/bbossgroups/elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzES2ESImportTask.java)

[gitee地址](https://gitee.com/bboss//elasticsearch-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzES2ESImportTask.java)

# 3 导入Database案例

## 3.1 elasticsearch导入database案例

### 案例1 基于数字字段增量导入案例

[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBScrollDemo.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBScrollDemo.java)

### 案例2 基于时间戳字段增量导入案例

[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBScrollTimestampDemo.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBScrollTimestampDemo.java)

### 案例3 基于数字字段slice增量导入案例

[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBSliceScrollDemo.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBSliceScrollDemo.java)

### 案例4 基于数字字段slice增量导入以及任务处理结果回调案例

[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBSliceScrollResultCallbackDemo.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2DBSliceScrollResultCallbackDemo.java)

### 案例5 基于apollo管理配置的同步作业工程案例

[github地址](https://github.com/bbossgroups/db-elasticsearch-tool-apollo)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool-apollo)

## 3.2 database导入database案例

### 案例1 基于数字字段增量导入案例

[github地址](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Db2DBdemo.java)

[gitee地址](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Db2DBdemo.java)

### 案例2 spring boot导入案例-基于配置文件管理sql

[github地址](https://github.com/bbossgroups/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemo.java)

[gitee地址](https://gitee.com/bboss/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemo.java)

### 案例3 spring boot导入案例-sql在代码中

[github地址](https://github.com/bbossgroups/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemoSQL.java)

[gitee地址](https://gitee.com/bboss/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemoSQL.java)

### 案例4 spring boot导入案例-基于配置数据库管理增量导入状态

[github地址](https://github.com/bbossgroups/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemoWithStatusConfigDB.java)

[gitee地址](https://gitee.com/bboss/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemoWithStatusConfigDB.java)

## 3.3 接收kafka消息导入database

[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2DBdemo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2DBdemo.java)

## 3.4 采集日志文件数据到database

[github地址](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2DBDemo.java)

[gitee地址](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2DBDemo.java)

## 3.4 采集hbase数据到database

[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2DBFullDemo.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2DBFullDemo.java)

## 3.5 采集mongodb数据到database

[github地址](https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2DBdemo.java)

[gitee地址](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2DBdemo.java)

# 4 生成File并上传Ftp案例

## 4.1 采集elasticsearch数据生成文件并上传ftp
案例1 ES2FileFtpDemo-sftp协议上传

[github地址](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpDemo.java)

[gitee地址](https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpDemo.java)

案例2 ES2FileFtpBatchDemo-sftp协议上传
[github地址](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpBatchDemo.java)

[gitee地址](https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpBatchDemo.java)

案例3 ES2FileFtpBatchSplitFileDemo-按照记录大写分割文件-sftp协议上传
[github地址](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpBatchSplitFileDemo.java)

[gitee地址](https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileFtpBatchSplitFileDemo.java)

案例3 ES2FileFtpBatchSplitFileDemo-按照记录大写分割文件-ftp协议上传
[github地址](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ftp/ES2FileFtpBatchDemo.java)

[gitee地址](https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ftp/ES2FileFtpBatchDemo.java)
## 4.2 采集日志文件数据，处理后生成文件并上传ftp

[github地址](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/filelog/Filelog2FileFtpDemo.java)

[gitee地址](https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/filelog/Filelog2FileFtpDemo.java)
## 4.4 采集database数据，处理后生成文件并上传ftp


[github地址](https://github.com/bbossgroups/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2FileFtpDemo.java)

[gitee地址](https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2FileFtpDemo.java)

## 4.4 接收kafka数据，处理后生成文件并上传ftp


[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2FileFtpDemo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2FileFtpDemo.java)

## 4.5 采集hbase数据，处理后生成文件并上传ftp

[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2FileFtpBatchSplitFileDemo.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2FileFtpBatchSplitFileDemo.java)

## 4.6 采集mongodb数据，处理后生成文件并上传ftp

[github地址](https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2FileFtp.java)

[gitee地址](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2FileFtp.java)


# 5 推送Kafka案例

## 5.1 采集elasticsearch数据推送kafka

[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2KafkaDemo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2KafkaDemo.java)
## 5.2 采集database数据推送kafka

[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DB2KafkaDemo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DB2KafkaDemo.java)
## 5.3 采集日志文件数据推送kafka

[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaDemo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaDemo.java)
## 5.4 接收kafka数据，经处理后推送kafka

[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2Kafkademo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2Kafkademo.java)
## 5.5 接收kafka数据，经处理后推送kafka

[github地址](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2Kafkademo.java)

[gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2Kafkademo.java)
## 5.6 采集mongodb数据，经处理后推送kafka

[github地址](https://github.com/bbossgroups/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2Kafka.java)

[gitee地址](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Mongodb2Kafka.java)
## 5.7 采集hbase数据，经处理后推送kafka

[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2KafkaFullDemo.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2KafkaFullDemo.java)

# 6 自定义处理器案例
[github地址](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2CustomDemo.java)

[gitee地址](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2CustomDemo.java)

# 7 Ftp文件下载采集案例
[Filelog插件调度采集案例 github地址](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESDemo.java)

[Filelog插件调度采集案例 gitee地址](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESDemo.java)


[ETL调度采集案例 github地址](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESETLScheduleDemo.java)

[ETL调度采集案例 gitee地址](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESETLScheduleDemo.java)

# 8 记录切割案例

[采集日志并切割推送kafka案例 github地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaSplitDemo.java)

[采集日志并切割推送kafka案例 gitee地址](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaSplitDemo.java)

# 9 生成和采集excel文件案例 

## 9.1 采集excel案例

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ExcelFile2DBDemo.java

## 9.2 从ftp采集excel案例

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpExcelFile2DBDemo.java

## 9.3 生成excel案例

https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2ExcelFile.java

## 9.4 生成excel上传ftp案例

https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2ExcelFile2Ftp.java

# 10 生成和采集csv文件案例 

https://gitee.com/bboss/csv-dbhandle

补充中。。。。。