# bboss数据采集ETL案例大全

# 1.bboss数据采集ETL工具功能架构	

​		bboss数据同步可以方便地实现多种数据源之间的数据同步功能，**支持增、删、改数据同步**，支持各种主流数据库、各种es版本以及日志文件数据采集和同步、加工处理，支持从kafka接收数据；经过加工处理的数据亦可以发送到kafka；可以将加工后的数据写入File并上传到ftp/sftp服务器。

![img](images/datasyn.png)

​		通过bboss，可以非常方便地采集database/mongodb/Elasticsearch/kafka/hbase/日志文件源数据，经过数据转换处理后，再推送到目标库elasticsearch/database/file/ftp/kafka/dummy/logger。

​		bboss另一个显著的特色就是直接基于java语言来编写数据同步作业程序，基于强大的java语言和第三方工具包，能够非常方便地加工和处理需要同步的源数据，然后将最终的数据保存到目标库（Elasticsearch或者数据库）；同时也可以非常方便地在idea或者eclipse中调试和运行同步作业程序，调试无误后，通过bboss提供的gradle脚本，即可构建和发布出可部署到生产环境的同步作业包。因此，对广大的java程序员来说，bboss无疑是一个轻易快速上手的数据同步利器。

​		bboss同步功能非常丰富，为了方便快速使用上手bboss，本文将各种数据同步案例呈现给大家，可以根据实际情况选用合适的案例。

下面以目标库为主线，列举数据采集同步案例
# 2.目标库Elasticsearch

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

## 2.5 Hbase到Elasticsearch数据同步
### 案例1 hbase定时全量同步案例
[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemo.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemo.java)
### 案例2 hbase定时全量同步案例-自定义hbase filter检索数据条件
[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemoWithFilter.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemoWithFilter.java)
### 案例3 基于时间戳hbase定时增量量同步案例-自定义hbase filter检索数据条件
[github地址](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESScrollTimestampDemo.java)

[gitee地址](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESScrollTimestampDemo.java)
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

# 3 目标库Database

补充中
# 4 目标文件-File和Ftp

补充中
# 5 目标Kafka

补充中

# 6 目标Dummy/Logger

补充中
