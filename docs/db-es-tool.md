# 数据采集ETL工具使用指南

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

*The best elasticsearch highlevel java rest api and ETL and Data batch and Stream Processor -----[bboss](https://www.bbossgroups.com)* 

bboss-datatran --- **简化版Flink**

[视频：bboss 流批一体化计算入门教程](https://www.bilibili.com/video/BV1o44y1w7VP)

[视频：实时采集Mysql binlog增删改数据视频教程](https://www.bilibili.com/video/BV1ko4y1M7My)

数据同步作业开发调试工程源码地址：https://git.oschina.net/bboss/bboss-datatran-demo

数据同步案例大全：[bboss数据采集ETL案例大全](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo?id=bboss数据采集etl案例大全)

[文档：数据指标统计计算流批一体化使用指南](https://esdoc.bbossgroups.com/#/etl-metrics)

输入输出插件使用手册：https://esdoc.bbossgroups.com/#/datatran-plugins

**bboss-datatran源码工程**

https://gitee.com/bboss/bboss-elastic-tran



## 1. 工具特性

​	bboss-datatran由 [bboss ](https://www.bbossgroups.com)开源的数据采集同步ETL工具，提供数据采集、数据清洗转换处理和数据入库以及[数据指标统计计算流批一体化](https://esdoc.bbossgroups.com/#/etl-metrics)处理功能。	

​	bboss-datatran 数据同步作业直接采用java语言开发，小巧而精致，可以采用java提供的所有功能和现有组件框架，随心所欲地处理和加工海量历史存量数据、实时增量数据；在实现数据采集-清洗-转换处理的同时，实现数据流批一体化计算功能；可以根据数据规模及同步性能要求，按需配置和调整数据采集同步作业所需内存、工作线程、线程队列大小；可以将作业独立运行，亦可以将作业嵌入基于java开发的各种应用一起运行；提供了作业任务控制API、作业监控api，支持作业启动、暂停(pause)、继续（resume）、停止控制机制，可轻松定制一款属于自己的ETL管理工具。

![](images\datasyn.png)

支持将采集的数据同时输出到[多个输出插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_214-%e5%a4%9a%e6%ba%90%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)，采用标准的输入输出异步管道来处理数据：

![](images\datasyn-inout-now.png)

采用 [bboss jobflow](https://esdoc.bbossgroups.com/#/jobworkflow) **通用分布式作业调度工作流**（提供通用轻量级、高性能流程编排模型），轻松实现数据交换、流批处理作业的流程编排以及调度执行。

![img](images\workflow\jobworkflow.png)



如果您正在：

- 寻求 logstash、flume、filebeat 之类的开源工具无法满足复杂的、海量数据自定义加工处理场景的解决方案；
- 寻求调用企业现有服务和库来处理加工数据的解决方案；
- 因项目投入有限、进度紧，急需一款功能强大、上手快、实施简单的数据交换工具
- 寻求一款简单实用，且易于维护的，数据采集、处理、流批一体化以及任务流程编排的大数据工具

那么 [bboss-datatran](http://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fesdoc.bbossgroups.com%2F%23%2Fdb-es-tool) 将是一个不错的选择。

### 1.1 多种作业

工具可以灵活定制具备各种功能的数据采集统计作业

1) 只采集和处理数据作业

2) 采集和处理数据、指标统计计算混合作业

3) 采集数据只做指标统计计算作业

### 1.2 指标计算特点

1) 支持时间维度和非时间维度指标计算

2) 时间维度指标计算：支持指定统计时间窗口，单位到分钟级别

3) 一个指标支持多个维度和多个度量字段计算，多个维度字段值构造成指标的唯一指标key，支持有限基数key和无限基数key指标计算   

4) 一个作业可以支持多种类型的指标，每种类型指标支持多个指标计算

5）支持准实时指标统计计算和离线指标统计计算

6）可以从不同的数据输入来源获取需要统计的指标数据，亦可以将指标计算结果保存到各种不同的目标数据源

### 1.3 作业采集状态-增量采集-故障恢复

增量数据采集，默认基于sqlite数据库管理增量采集状态，可以配置到其他关系数据库管理增量采集状态，提供对多种不同数据来源增量采集机制：

1) 基于数字字段增量采集：各种关系数据库、Elasticsearch、MongoDB、Clickhouse、向量数据库Milvus等

2) 基于数字字段类型的时间戳增量采集：各种关系数据库、Elasticsearch、MongoDB、Clickhouse、向量数据库Milvus等，基于时间增量还可以设置一个截止时间偏移量，比如采集到当前时间前十秒的增量数据，避免漏数据

3) 基于时间字段增量采集：各种关系数据库、Elasticsearch、MongoDB、Clickhouse、HBase、向量数据库Milvus等，基于时间增量还可以设置一个截止时间偏移量，比如采集到当前时间前十秒的增量数据，避免漏数据，支持纳秒级精度数据同步

4) 基于文件内容位置偏移量：文本文件、日志文件基于采集位置偏移量做增量

5) 基于ftp文件增量采集：基于文件级别，下载采集完的文件就不会再采集

6) 支持[mysql binlog](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_13-mysql-binlog输入插件)，实现mysql增删改实时增量数据采集，实现mysql增删改实时增量数据采集，支持master/slave监听、binlog日志文件直接采集两种模式

7）MongoDB CDC，基于MongoDB Data ChangeStream，实时采集MongoDB增、删、改以及替换数据

### 1.4 主要功能

1）数据导入的方式

- 支持逐条数据导入
- 批量数据导入
- 批量数据多线程并行导入
- 定时全量（串行/并行）数据导入
- 定时增量（串行/并行）数据导入
- 支持记录切割功能

2）支持各种主流数据库、各种es版本以及本地/Ftp日志文件数据采集和同步、加工处理

数据源支持：支持在Elasticsearch、关系数据库、向量数据库Milvus、Mongodb、HBase、Hive、Kafka、Rocketmq、文本文件、excel/word/pdf/图片/视频等类型文件、SFTP/FTP、http/https、OSS对象存储等多种数据源之间进行海量数据采集同步；支持数据实时增量采集和全量采集；支持根据字段进行数据记录切割；支持多级文件路径(本地和FTP/SFTP)下不同文件数据采集写入不同的数据库表和其他数据源。

支持各种关系数据库： mysql,maridb，postgresql,oracle ,sqlserver,db2，derby,sqlite,informix,sybase,达梦，以及其他支持jdbc协议的数据库
支持分布式数仓：clickhouse，mongodb、HBase、tidb、hive、doris、druid、StarRocks

支持各种Elasticsearch版本： 1.x,2.x,5.x,6.x,7.x,8.x,+

3）支持数据向量化处理，并将向量数据保存到Milvus向量数据库

4) 支持[mysql binlog](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_13-mysql-binlog%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)，实现mysql增删改实时增量数据采集，支持master/slave监听、binlog日志文件直接采集两种模式

5）提供自定义处理采集数据功能，可以按照自己的要求将采集的数据处理到目的地，如需定制化将数据保存到特定的地方，可自行实现CustomOutPut接口处理即可。

6）支持从kafka、Rocketmq接收数据；经过加工处理的数据亦可以发送到kafka、Rocketmq；

7）支持将单条记录切割为多条记录；

8）可以将加工后的数据写入File并上传到ftp/sftp服务器；

9）可以将加工后的数据写入File并上传到OSS对象存储服务器Minio；

10）支持备份采集完毕日志文件功能，可以指定备份文件保存时长，定期清理超过时长文件；

11）支持自动清理下载完毕后ftp服务器上的文件;

12）支持excel/word/pdf/图片/视频等类型文件采集（本地和ftp/sftp）

13）支持导出数据到excel和csv文件,并支持上传到ftp/sftp服务器

14）支持海量PB级数据同步导入功能

15）支持将ip转换为对应的运营商和城市地理坐标位置信息

16）**支持设置数据bulk导入任务结果处理回调函数，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务通过error和exception方法进行相应处理**

17）支持以下三种作业调度机制：

- jdk timer （内置）
- quartz
- xxl-job分布式调度引擎，基于分片调度机制实现海量数据快速同步能力

18) 提供灵活的作业启动、暂停(pause)、继续（resume）、停止控制机制

19) 支持将数据同时输出到[多个输出插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_214-%e5%a4%9a%e6%ba%90%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)

20）基于 [bboss jobflow](https://esdoc.bbossgroups.com/#/jobworkflow)，轻松实现数据交换作业的灵活编排和调度执行

## 2. 插件清单

bboss通过输入、输出插件结合实现数据采集及流批一体化分析处理功能，各个插件需要对应的组件来支持，下面列出了现有的输入插件和输出插件清单及功能说明，同时也列出了插件对应的依赖maven坐标，对应的groupid统一为：

```java
com.bbossgroups.plugins
```

表格中给出了对应的artifactId，插件对应的最新版本号可以从实时更新的[版本发布公告](https://esdoc.bbossgroups.com/#/changelog)获取。如需在项目中使用插件对应的数据采集和流批一体化处理功能，导入对应的Maven坐标即可，插件依赖的其他第三方包，参考[具体的案例工程依赖管理文件](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo)获取。

详细输入输出插件使用手册：https://esdoc.bbossgroups.com/#/datatran-plugins

### 2.1 输入插件

| 插件                                                         | 插码名称                                                     | Maven坐标               | 功能说明                                                     |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ----------------------- | ------------------------------------------------------------ |
| [DBInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/db/input/DBInputConfig.java) | DB数据库输入插件                                             | bboss-datatran-jdbc     | 配置DB数据源、查询sql、查询sql文件路径及文件名称,支持各种关系数据库，hive |
| [ElasticsearchInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/es/input/ElasticsearchInputConfig.java) | elasticsearch输出插件                                        | bboss-datatran-jdbc     | 配置elasticsearch数据源、queryDsl、queryDsl配置文件路径等    |
| [HttpInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/http/input/HttpInputConfig.java) | Http输入插件                                                 | bboss-datatran-jdbc     | 配置http服务参数、服务地址、服务查询参数、ssl证书等          |
| [FileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/FileInputConfig.java) | 文件输入插件                                                 | bboss-datatran-fileftp  | 对应文本类数据文件数据采集配置，源文件目录、输入Ftp/sftp配置 |
| [ExcelFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/ExcelFileInputConfig.java) | excel文件输入插件                                            | bboss-datatran-fileftp  | excel文件采集映射配置（忽略行数、excel列号与目标字段名称映射、列默认值配置），包括excel源文件目录、输入Ftp/sftp配置 |
| [WordFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/WordFileInputConfig.java) | [Word文件采集插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_110-word文件采集插件) | bboss-datatran-fileftp  | 插件配置 [WordFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/WordFileInputConfig.java)(FileInputConfig子类)和WordFileConfig（FileConfig子类）结合，通过WordFileConfig设置WordExtractor自定义word文件内容提取逻辑，如果不设置setWordExtractor，默认将文件内容放置到wordContent字段中，除了word文件采集需要的配置，其他配置和文件采集插件配置一致 |
| [PDFFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/PDFFileInputConfig.java) | [PDF文件采集插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_111-pdf文件采集插件) | bboss-datatran-fileftp  | 插件配置 [PDFFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/PDFFileInputConfig.java)(FileInputConfig子类)和PDFFileConfig（FileConfig子类）结合，通过PDFFileConfig设置PDFExtractor自定义pdf文件内容提取逻辑，如果不设置setPdfExtractor，默认将文件内容放置到pdfContent字段中，除了pdf文件采集需要的配置，其他配置和文件采集插件配置一致 |
| [CommonFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/CommonFileInputConfig.java) | [其他类型文件采集插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_112-其他类型文件采集插件) | bboss-datatran-fileftp  | 插件配置 [CommonFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/CommonFileInputConfig.java)(FileInputConfig子类)和CommonFileConfig（FileConfig子类）结合,必须通过setCommonFileExtractor设置CommonFileExtractor，提取文件内容，除了其他类型（图片、视频等）文件采集需要的配置，其他配置和文件采集插件配置一致 |
| [HBaseInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-hbase/src/main/java/org/frameworkset/tran/plugin/hbase/input/HBaseInputConfig.java) | hbase输入插件                                                | bboss-datatran-hbase    | hbase连接配置、查询表配置、查询条件配置                      |
| [MongoDBInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongodb/input/MongoDBInputConfig.java) | mongodb输入插件                                              | bboss-datatran-mongodb  | mongodb连接配置、查询表配置、查询条件配置                    |
| [MongoCDCInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongocdc/MongoCDCInputConfig.java) | mongodb cdc插件                                              | bboss-datatran-mongodb  | 基于MongoDB Data ChangeStream，实时采集MongoDB增、删、改以及替换数据 |
| [Kafka2InputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka2x/src/main/java/org/frameworkset/tran/plugin/kafka/input/Kafka2InputConfig.java) | kafka输入插件                                                | bboss-datatran-kafka2x  | kafka消费端参数配置、主题配置、客户端消费组配置等            |
| [Kafka1InputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka1x/src/main/java/org/frameworkset/tran/plugin/kafka/input/Kafka1InputConfig.java) | 低版本kafka输入插件                                          | bboss-datatran-kafka1x  | 低版本kafka消费端参数配置、主题配置、客户端消费组配置等（不推荐使用，建议升级到kafka 2x版本） |
| [RocketmqInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-rocketmq/src/main/java/org/frameworkset/tran/plugin/rocketmq/input/RocketmqInputConfig.java) | Rocketmq输入插件                                             | bboss-datatran-rocketmq | Rocketmq消费端参数配置、主题配置、客户端消费组配置等         |
| [MilvusInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-milvus/src/main/java/org/frameworkset/tran/plugin/milvus/input/MilvusInputConfig.java) | Milvus输入插件-基于QueryIterator                             | bboss-datatran-milvus   | Milvus向量库输入插件，支持全量或者增量从Milvus采集同步数据到其他Milvus库，或者其他数据源,基于QueryIterator，通过指定过滤条件实现数据查询 |
| [MilvusVectorInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-milvus/src/main/java/org/frameworkset/tran/plugin/milvus/input/MilvusVectorInputConfig.java) | Milvus输入插件-基于SearchIterator                            | bboss-datatran-milvus   | Milvus向量库输入插件，支持全量或者增量从Milvus采集同步数据到其他Milvus库，或者其他数据源,基于SearchIterator，通过向量相似度检索和过滤条件相结合实现数据查询 |

### 2.2 输出插件

| 插件                                                         | 插码名称                 | Maven坐标               | 功能说明                                                     |
| ------------------------------------------------------------ | ------------------------ | ----------------------- | ------------------------------------------------------------ |
| [DBOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/db/output/DBOutputConfig.java) | 数据库输出插件           | bboss-datatran-jdbc     | 数据库地址配置、连接池配置、输出sql、更新sql、deletesql配置、sql文件路径配置 |
| [ElasticsearchOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/es/output/ElasticsearchOutputConfig.java) | Elasticsearch输出插件    | bboss-datatran-jdbc     | elasticsearch地址配置、http连接池配置、账号口令配置、elasticsearch连接参数配置、Elasticsearch输出表配置 |
| [HttpOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/http/output/HttpOutputConfig.java) | http/https输出插件       | bboss-datatran-jdbc     | http输出服务参数配置、连接参数配置、监控检查机制配置、ssl证书配置、输出服务地址配置 |
| [FileOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/output/FileOutputConfig.java) | 文本文件输出插件         | bboss-datatran-fileftp  | 文本文件输出配置、文件切割记录数配置、文件行分隔符配置、文件名称生成规则配置、记录标题行配置、发送Ftp/sftp配置、发送OSS对象数据库配置 |
| [ExcelFileOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/output/ExcelFileOutputConfig.java) | excel文件输出插件        | bboss-datatran-fileftp  | Excel文件输出配置、列号与字段映射配置、标题配置、sheet配置、列标题配置、文件切割记录数配置、文件行分隔符配置、文件名称生成规则配置、发送Ftp/sftp配置 |
| [Kafka2OutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka2x/src/main/java/org/frameworkset/tran/plugin/kafka/output/Kafka2OutputConfig.java) | kafka输出插件            | bboss-datatran-kafka2x  | kafka输出参数配置、主题配置、记录序列化机制配置、记录生成器配置 |
| [Kafka1OutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka1x/src/main/java/org/frameworkset/tran/plugin/kafka/output/Kafka1OutputConfig.java) | 低版本kafka输出插件      | bboss-datatran-kafka1x  | 低版本kafka输出参数配置、主题配置、记录序列化机制配置、记录生成器配置（不推荐使用，建议升级到kafka 2x版本） |
| [RocketmqOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-rocketmq/src/main/java/org/frameworkset/tran/plugin/rocketmq/output/RocketmqOutputConfig.java) | Rocketmq输出插件配置     | bboss-datatran-rocketmq | Rocketmq输出参数配置、主题配置、记录序列化机制配置、记录生成器配置 |
| [CustomOupputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/custom/output/CustomOupputConfig.java) | 自定义输出插件           | bboss-datatran-jdbc     | 提供自定义处理采集数据功能，可以按照自己的要求将采集的数据处理到目的地，如需定制化将数据保存到特定的地方，可自行实现CustomOutPut接口处理即可 |
| [MongoDBOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongodb/output/MongoDBOutputConfig.java) | MongoDB输出插件          | bboss-datatran-mongodb  | 提供MongoDB地址和连接参数配置，输出db和collection配置        |
| [HBaseOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-hbase/src/main/java/org/frameworkset/tran/plugin/hbase/output/HBaseOutputConfig.java) | HBase输出插件            | bboss-datatran-hbase    | HBase地址和连接参数配置，hbase输出表配置，hbase列簇和列及对应的源字段映射配置 |
| [MetricsOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/metrics/output/MetricsOutputConfig.java) | 指标统计插件             | bboss-datatran-jdbc     | 提供指标计算规则配置：ETLMetrics、时间维度字段配置等，具体参考[使用指南](https://esdoc.bbossgroups.com/#/etl-metrics) |
| [MilvusOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-milvus/src/main/java/org/frameworkset/tran/plugin/milvus/output/MilvusOutputConfig.java) | Milvus向量数据库输出插件 | bboss-datatran-milvus   | 配置Milvus服务器参数：数据源名称，uri，token，dbname，表名称，表分区，连接池参数、超时参数配置等 |
|                                                              |                          |                         |                                                              |
| [DummyOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/dummy/output/DummyOutputConfig.java) | dummy插件                | bboss-datatran-jdbc     | 调试作业使用，将采集的数据直接输出到控制台                   |

### 2.3 作业基础配置

作业基础配置，包含了作业调度、并行处理、任务监控、任务回调处理、作业初始化和作业结束回调处理配置、增量配置、数据转换处理以及指标计算配置等等，大致配置流程可以参考文档：[2.3 定义作业配置](https://esdoc.bbossgroups.com/#/db-es-tool?id=_23-定义作业配置)

```java
//创建一个作业构建器
ImportBuilder importBuilder = new ImportBuilder() ;
importBuilder.setJobId("job-123");
importBuilder.setJobName("流失数据挖掘");
.......
```

| 属性名称                 | 类型                | 说明                                                         |
| ------------------------ | ------------------- | ------------------------------------------------------------ |
| FetchSize                | int                 | 按批获取数据记录数大小，importBuilder.setFetchSize(5000)     |
| BatchSize                | int                 | 按批输出数据记录数待续，importBuilder.setBatchSize(1000)     |
| InputConfig              | InputConfig         | 设置输入插件配置，importBuilder.setInputConfig(httpInputConfig); |
| OutputConfig             | OutputConfig        | 设置输出插件配置，importBuilder.setOutputConfig(elasticsearchOutputConfig); |
| addJobInputParam         | 方法                | 为查询类作业添加额外的查询条件参数importBuilder.addJobInputParam("otherParam","陈雨菲2:0战胜戴资颖"); |
| addJobDynamicInputParam  | 方法                | 为查询类作业添加额外的动态查询条件参数importBuilder.addJobDynamicInputParam("signature", new DynamicParam() {//根据数据动态生成签名参数    @Override    public Object getValue(String paramName, DynamicParamContext dynamicParamContext) {        //可以根据自己的算法对数据进行签名       String signature =md5(datas)       return signature;    } }); |
| addJobOutputParam        | 方法                | 为输出插件作业dsl脚本或者sql添加额外的变量参数importBuilder.addJobOutputParam("otherParam","陈雨菲2:0战胜戴资颖"); |
| addJobDynamicOutputParam | 方法                | 为输出插件作业dsl脚本或者sql添加额外的动态变量参数importBuilder.addJobDynamicOutputParam("signature", new DynamicParam() {//根据数据动态生成签名参数    @Override    public Object getValue(String paramName, DynamicParamContext dynamicParamContext) {        //可以根据自己的算法对数据进行签名       String signature =md5(datas)       return signature;    } }); |
| useJavaName              | boolean             | 可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId |
| useLowcase               | boolean             | 可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用 |
| PrintTaskLog             | boolean             | 可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false |
| FixedRate                | boolean             | 参考jdk timer task文档对fixedRate的说明                      |
| DeyLay                   | long                | 任务延迟执行deylay毫秒后执行                                 |
| Period                   | long                | 每隔period毫秒执行，如果不设置，只执行一次                   |
| ScheduleDate             | date                | 指定任务开始执行时间：日期                                   |
| addCallInterceptor       | CallInterceptor     | 设置任务执行拦截器，可以添加多个，定时任务每次执行的拦截器   |
| LastValueColumn          | String              | 指定数字增量查询字段                                         |
| FromFirst                | boolean             | false 如果作业停了，作业重启后从上次截止位置开始采集数据，true 如果作业停了，作业重启后，重新开始采集数据 |
| StatusDbname             | String              | 设置增量状态数据源名称                                       |
| LastValueStorePath       | String              | 记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样 |
| LastValueStoreTableName  | String              | 记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab |
| LastValueType            | int                 | 指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型，ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型 |
| IncreamentEndOffset      | int                 | 单位：秒，日期类型增量导入，可以设置一个导入截止时间偏移量。引入IncreamentEndOffset配置，主要是增量导出时，考虑到elasticsearch、mongodb这种存在写入数据的延迟性的数据库，设置一个相对于当前时间偏移量导出的截止时间，避免增量导出时遗漏数据。 |
| addFieldMapping          | 方法                | 手动设置字段名称映射，将源字段名称映射为目标字段名称importBuilder.addFieldMapping("document_id","docId") |
| addIgnoreFieldMapping    | 方法                | 添加忽略字段，importBuilder.addIgnoreFieldMapping("channel_id"); |
| addFieldValue            | 方法                | 添加全局字段和值，为每条记录添加额外的字段和值，可以为基本数据类型，也可以是复杂的对象mportBuilder.addFieldValue("testF1","f1value"); |
| DataRefactor             | DataRefactor        | 通过DataRefactor，对数据记录进行数据转换、清洗、加工操作，亦可以对数据进行记录级别的处理，比如添加字段、去除字段、忽略记录、类型转换等 |
| Parallel                 | boolean             | 设置为多线程并行批量导入,false串行 true并行                  |
| Queue                    | int                 | 设置批量导入线程池等待队列长度                               |
| ThreadCount              | int                 | 设置批量导入线程池工作线程数量                               |
| ContinueOnError          | boolean             | 任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行 |
| ExportResultHandler      | ExportResultHandler | 设置任务执行结果以及异常回调处理函数，函数实现接口即可       |
| builder                  | 方法                | 构建DataStream 执行数据库表数据导入es操作  ： DataStream dataStream = importBuilder.builder(); dataStream.execute();//执行导入操作 |
| jobId                    | String              | 可选，设置作业唯一标识                                       |
| jobName                  | String              | 可选，设置作业名称                                           |

### 2.4 关键对象说明

Context --数据处理接口，存放源表字段和对应的值，中间临时变量和值，可以通过源表字段名称获取对应的字段数据。增量字段名称、sql或者dsl中的增量字段变量名称，都需与源表字段名称一致。context接口还提供了丢弃记录、添加字段、去除字段、字段名称映射规则配置功能、获取记录元数据功能

CommonRecord - 封装处理后的结果记录，字段名称是经过规范化处理后的字段名称，字段名称的转换和处理主要受作业控制参数useJavaName和useLowcase控制，同时也受字段名称映射规则配置影响:

importbuilder.addFieldMapping方法和context.addFieldMapping

### 2.5 作业依赖资源初始化和销毁

需要对作业执行过程中依赖其他的资源组件进行初始化，作业结束时需要释放和关闭作业执行时初始化的依赖资源组件，bboss通过以下两个接口来实现资源组件的初始化和销毁释放。

资源组件的初始化：

```java
/**
 * <p>Description: 导数据之前处理逻辑</p>
 */
public interface ImportStartAction {
   /**
    * 初始化之前执行的处理操作，比如后续初始化操作、数据处理过程中依赖的资源初始化
    * @param importContext
    */
   void startAction(ImportContext importContext);

   /**
    * 所有初始化操作完成后，导出数据之前执行的操作
    * @param importContext
    */
   void afterStartAction(ImportContext importContext);
}
```

资源组件销毁：

```java
/**
 * <p>Description: 任务结束处理逻辑</p> 
 */
public interface ImportEndAction {
   /**
    * 作业任务执行完毕后的处理操作
    * @param importContext 作业定义配置上下文
    * @param e  对应作业异常结束时的异常信息
    */
   void endAction(ImportContext importContext,Exception e);
}
```

使用案例

```java
//通过作业初始化配置，对作业运行过程中依赖的数据源等资源进行初始化
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

                ResourceStartResult resourceStartResult = null;

                DBConf tempConf = new DBConf();
                tempConf.setPoolname("ddlsyn");//用于验证ddl同步处理的数据源
                tempConf.setDriver("com.mysql.cj.jdbc.Driver");
                tempConf.setJdbcurl("jdbc:mysql://192.168.137.1:3306/pinpoint?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true");

                tempConf.setUsername("root");
                tempConf.setPassword("123456");
                tempConf.setValidationQuery("select 1");

                tempConf.setInitialConnections(5);
                tempConf.setMinimumSize(10);
                tempConf.setMaximumSize(10);
                tempConf.setUsepool(true);
                tempConf.setShowsql(true);
                tempConf.setJndiName("ddlsyn-jndi");
                //# 控制map中的列名采用小写，默认为大写
                tempConf.setColumnLableUpperCase(false);
                //启动数据源
                boolean result = SQLManager.startPool(tempConf);
                //记录启动的数据源信息，用户作业停止时释放数据源
                if(result){
                    if(resourceStartResult == null)
                        resourceStartResult = new DBStartResult();
                    resourceStartResult.addResourceStartResult("ddlsyn");
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

    }
});

//任务结束后销毁初始化阶段初始化的数据源等资源
importBuilder.setImportEndAction(new ImportEndAction() {
    @Override
    public void endAction(ImportContext importContext, Exception e) {
        //销毁初始化阶段自定义的数据源
        importContext.destroyResources(new ResourceEnd() {
            @Override
            public void endResource(ResourceStartResult resourceStartResult) {
                if(resourceStartResult instanceof DBStartResult) { //作业停止时，释放db数据源
                    DataTranPluginImpl.stopDatasources((DBStartResult) resourceStartResult);
                }
            }
        });
    }
});
```

接下来结合实际案例介绍bboss datatran的功能.

## 3. 准备工作

### 3.1 在工程中导入bboss maven坐标
Elasticsearch/Database/Http/Metrics(流批一体化插件)/Custom(自定义处理器)/Dummy插件坐标，其他插件可以参考插件清单获取maven坐标信息

```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-jdbc</artifactId>
<version>7.3.9</version>
</dependency>
```
kafka插件maven坐标
```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-kafka2x</artifactId>
<version>7.3.9</version>
</dependency>
```
rocketmq插件maven坐标
```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-rocketmq</artifactId>
<version>7.3.9</version>
</dependency>
```
日志文件/excel/csv//word/pdf/图片/视频等采集以及上传ftp/sftp插件maven坐标
```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-fileftp</artifactId>
<version>7.3.9</version>
</dependency>
```
hbase插件maven坐标
```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-hbase</artifactId>
<version>7.3.9</version>
</dependency>
```
mongodb插件maven坐标
```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-mongodb</artifactId>
<version>7.3.9</version>
</dependency>
```

mysqlbinlog插件maven坐标
```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-binlog</artifactId>
<version>7.3.9</version>
</dependency>
```
milvus插件maven坐标
```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-milvus</artifactId>
<version>7.3.9</version>
</dependency>
```

如果需要增量导入，还需要导入sqlite驱动：

```xml
<dependency>
      <groupId>org.xerial</groupId>
      <artifactId>sqlite-jdbc</artifactId>
      <version>3.36.0.3</version>
      <scope>compile</scope>
 </dependency>
```

如果需要使用xxjob来调度作业任务，需要导入坐标：

```xml
<dependency>
      <groupId>com.xuxueli</groupId>
      <artifactId>xxl-job-core</artifactId>
      <version>2.0.2</version>
      <scope>compile</scope>
 </dependency>
```

### 3.2 clickhouse对接配置

如果需要使用bboss的数据库输入输出插件从clickhouse导出或者输出数据,需要额外引入clickhouse jdbc驱动包，可以根据需要使用不同的驱动连接Clickhouse

基于TCP端口对接：com.github.housepower驱动

基于http端口对接：官方驱动包

#### 3.2.1 基于TCP端口对接

基于TCP端口对接，需要额外引入clickhouse jdbc驱动包com.github.housepower

```xml
<dependency>
      <groupId>com.github.housepower</groupId>
      <artifactId>clickhouse-native-jdbc</artifactId>
      <version>2.7.1</version>
      <scope>compile</scope>
 </dependency>
```

对应clickhouse的配置案例：

```properties
clickhousedm.db.user = default
clickhousedm.db.password =
clickhousedm.db.driver = com.github.housepower.jdbc.ClickHouseDriver
# DataSource singleDataSource = new BalancedClickhouseDataSource("jdbc:clickhouse://127.0.0.1:9000");
#
#DataSource dualDataSource = new BalancedClickhouseDataSource("jdbc:clickhouse://127.0.0.1:9000,127.0.0.1:9000");

clickhousedm.db.url = jdbc:clickhouse://10.103.6.4:29000,10.103.6.7:29000,10.103.6.6:29000/visualops
```

客户端驱动com.github.housepower使用tcp端口或者tcp ssl端口连接clickhouse，如果配置其他协议的端口连接Clickhouse会碰到异常以下异常：

Accept the id of response that is not recognized by Server

tcp端口详见clickhouse的config.xml配置文件：

```xml
<!-- Port for interaction by native protocol with:
     - clickhouse-client and other native ClickHouse tools (clickhouse-benchmark, clickhouse-copier);
     - clickhouse-server with other clickhouse-servers for distributed query processing;
     - ClickHouse drivers and applications supporting native protocol
     (this protocol is also informally called as "the TCP protocol");
     See also 'tcp_port_secure' for secure connections.
-->
<tcp_port>29000</tcp_port>
```
#### 3.2.2 基于http端口对接

基于Http端口对接，需要额外引入clickhouse 官方 jdbc驱动包

maven

```xml
		<dependency>
            <groupId>com.clickhouse</groupId>
            <artifactId>clickhouse-jdbc</artifactId>
            <version>0.6.5</version>
            <classifier>http</classifier>
        </dependency>
	    <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>5.4</version>   
            <exclusions>
                <exclusion>                    
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
	    <dependency>
            <groupId>org.apache.httpcomponents.core5</groupId>
            <artifactId>httpcore5</artifactId>
            <version>5.3</version>   
            <exclusions>
                <exclusion>                    
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                </exclusion>
            </exclusions>             
        </dependency>

```

gradle

```groovy
api 'com.clickhouse:clickhouse-jdbc:0.6.5:http'
    api (
            [group: 'org.apache.httpcomponents.client5', name: 'httpclient5', version: "5.4", transitive: true]
    )
    {
        exclude group: 'org.slf4j', module: 'slf4j-api'
    }
    api (
            [group: 'org.apache.httpcomponents.core5', name: 'httpcore5', version: "5.3", transitive: true]
    )
    {
        exclude group: 'org.slf4j', module: 'slf4j-api'
    }
```

对应clickhouse的配置案例：

```properties
clickhousedm.db.user = default
clickhousedm.db.password =
clickhousedm.db.driver = com.clickhouse.jdbc.ClickHouseDriver

clickhousedm.db.url = jdbc:ch://(http://10.103.6.4:28123),(http://10.103.6.7:28123),(http://10.103.6.6:28123)/visualops?failover=1&load_balancing_policy=random
```

bboss持久层Clickhouse客户端负载均衡和容灾功能使用参考文档：

https://doc.bbossgroups.com/#/persistent/datasource-cluster

## 4. 使用介绍

下面以数据库表数据导入到Elasticsearch为案例，介绍bboss的使用方法。

案例工程地址

https://github.com/bbossgroups/db-elasticsearch-tool

https://gitee.com/bboss/db-elasticsearch-tool

其他案例清单：

https://esdoc.bbossgroups.com/#/bboss-datasyn-demo

### 4.1. 案例对应的源码

一次性批量导入：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/Db2EleasticsearchOnceScheduleDateDemo.java

定时增量导入：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/Db2EleasticsearchDemo.java

### 4.2.索引表结构定义

Elasticsearch会在我们导入数据的情况下自动创建索引mapping结构，如果对mapping结构有特定需求或者自动创建的结构不能满足要求，可以自定义索引mapping结构，在导入数据之前创建好自定义的mapping结构或者mapping模板即可，具体定义和创建方法参考文档： [Elasticsearch索引表和索引表模板管理](index-indextemplate.md) 

### 4.3 定义作业配置主体流程

作业需要通过ImportBuilder来进行配置和构建，大致的流程如下：

```java
//创建作业构建器
ImportBuilder importBuilder = new ImportBuilder() ;
importBuilder.setJobId("job-123");
importBuilder.setJobName("流失数据挖掘");
//输入插件配置
importBuilder.setInputConfig(dbInputConfig);

//输出插件配置
importBuilder.setOutputConfig(elasticsearchOutputConfig);

//作业基础配置
importBuilder.setBatchSize(5000);

importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
//数据加工处理、清洗
importBuilder.setDataRefactor(new DataRefactor() {
    public void refactor(Context context) throws Exception  {
		context.addFieldValue("newCollecttime",new Date());//添加采集时间
    }
});
    
//作业执行
/**
 * 创建执行数据库表数据导入es作业DataStream对象
 */
DataStream dataStream = importBuilder.builder();
dataStream.execute();//启动运行作业

作业控制相关指令
//停止作业
dataStream.destroy();

//暂停作业
dataStream.pauseSchedule();

//继续作业
dataStream.resumeSchedule();
```

针对数据库和Elasticsearch插件的配置，bboss支持可以在application.properties文件中配置相关数据源，亦可以在插件上面直接配置数据源，下面文档中都有介绍。

更多的作业调度控制说明，可以参考文档：https://esdoc.bbossgroups.com/#/bboss-datasyn-control



### 4.4.配置DBInput输入参数

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

### 4.5.配置Elasticsearch输出参数

新建ElasticsearchOutputConfig配置对象：

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

		importBuilder.setOutputConfig(elasticsearchOutputConfig);
```

### 4.6 作业基本配置

```java

importBuilder.setBatchSize(5000);//可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理 
importBuilder
//
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
				.setUseLowcase(true)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
				.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
				;  

		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束
//增量配置开始
//		importBuilder.setStatusDbname("test");//设置增量状态数据源名称
		importBuilder.setLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
//		setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
		importBuilder.setStatusDbname("testStatus");//指定增量状态数据源名称
//		importBuilder.setLastValueStorePath("logtable_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
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
/**
		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
	

```

### 4.7 作业执行

```java
/**
 * 执行数据库表数据导入es操作
 */
DataStream dataStream = importBuilder.builder();
dataStream.execute();//执行导入操作
```

### 4.8 作业详解



#### 4.8.1同步批量导入

批量导入Elasicsearch记录数配置：

importBuilder.setBatchSize(5000)

数据库jdbcFetchSize设置，参考文档：[fetch机制配置](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2814-%e6%95%b0%e6%8d%ae%e5%ba%93resultset-stream%e6%9c%ba%e5%88%b6%e8%af%b4%e6%98%8e)

根据设置的SQL语句，同步批量一次性导入数据到Elasticsearch中，非定时执行。

```java
	public void testSimpleImportBuilder(){
		ImportBuilder importBuilder = new ImportBuilder() ;
		DBInputConfig dbInputConfig = new DBInputConfig();
		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// 需要设置setLastValueColumn信息log_id，
		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型

//		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
		dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
				.setDbName("test");
		importBuilder.setInputConfig(dbInputConfig);


//		importBuilder.addFieldMapping("LOG_CONTENT","message");
//		importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("default")
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
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();
        dataStream.destroy();//执行完毕后释放资源
	}
```

可以直接运行上述代码，查看数据导入效果。



#### **4.8.2 异步批量导入**

根据设置的SQL语句，异步批量一次性导入数据到Elasticsearch中，非定时执行。异步批量导入关键配置：

```java
        importBuilder.setParallel(true);//设置为多线程异步并行批量导入
		importBuilder.setQueue(100);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(200);//设置批量导入线程池工作线程数量
```

示例代码如下：

```java
	public void testSimpleLogImportBuilderFromExternalDBConfig(){
		ImportBuilder importBuilder = new ImportBuilder() ;
		DBInputConfig dbInputConfig = new DBInputConfig();
		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// 需要设置setLastValueColumn信息log_id，
		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型

//		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
		dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
				.setDbName("test");
		importBuilder.setInputConfig(dbInputConfig);


//		importBuilder.addFieldMapping("LOG_CONTENT","message");
//		importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("default")
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
		 * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入
		importBuilder.setQueue(100);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(200);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行 
		
		importBuilder.setRefreshOption("refresh"); // 为了实时验证数据导入的效果，强制刷新数据，生产环境请设置为null或者不指定
		
		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();
        dataStream.destroy();//执行完毕后释放资源
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

##### 4.8.2.1 异步ResultSet缓冲队列大小配置

```java
//数据异步同步通道缓存队列设置，默认为10
importBuilder.setTranDataBufferQueue(20);
```

bboss会将采集数据先放入异步结果迭代器resultset缓冲队列，队列长度对应的参数为tranDataBufferQueue；

如果目标写入比较慢，通过调整数据采集异步结果迭代器resultset数据缓冲队列tranDataBufferQueue大小，可以得到更好的数据采集性能，如果调大该参数会占用更多的jvm内存。

#### 4.8.3 一个有字段属性映射的稍微复杂案例实现

```java
	public void testImportBuilder(){
		ImportBuilder importBuilder = new ImportBuilder() ;
		DBInputConfig dbInputConfig = new DBInputConfig();
		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// 需要设置setLastValueColumn信息log_id，
		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型

//		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
		dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
				.setDbName("test");
		importBuilder.setInputConfig(dbInputConfig);


//		importBuilder.addFieldMapping("LOG_CONTENT","message");
//		importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("default")
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



#### 4.8.4 设置文档id机制

bboss充分利用elasticsearch的文档id生成机制，同步数据的时候提供了以下3种生成文档Id的机制：

1. 不指定文档ID机制：直接使用Elasticsearch自动生成文档ID

2. 指定表字段，对应的字段值作为Elasticsearch文档ID

   importBuilder.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id

3. 自定义文档id机制配置

```
elasticsearchOutputConfig.setEsIdGenerator(new EsIdGenerator() {
			//如果指定EsIdGenerator，则根据下面的方法生成文档id，
			// 否则根据setEsIdField方法设置的字段值作为文档id，
			// 如果既没有配置EsIdField也没有指定EsIdGenerator，则由es自动生成文档id

			@Override
			public Object genId(Context context) throws Exception {
				return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
			}
		});
```



#### 4.8.5 定时增量导入

bboss的增量数据采集，默认基于sqlite数据库管理增量采集状态，可以配置到其他关系数据库管理增量采集状态，提供对多种不同数据来源增量采集机制：

基于数字字段增量采集：各种关系数据库、Elasticsearch、MongoDB、Clickhouse等

基于时间字段增量采集：各种关系数据库、Elasticsearch、MongoDB、Clickhouse、HBase等，基于时间增量还可以设置一个截止时间偏移量，比如采集到当前时间前十秒的增量数据，避免漏数据

基于文件内容位置偏移量：文本文件、日志文件基于采集位置偏移量做增量

基于ftp文件增量采集：基于文件级别，下载采集完的文件就不会再采集

支持[mysql binlog](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_13-mysql-binlog%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)，实现mysql增删改实时增量数据采集

##### 4.8.5.1 定时机制配置

```java
//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束
```

更多设置参考章节：[4 数据同步模式控制](https://esdoc.bbossgroups.com/#/db-es-tool?id=_4-数据同步模式控制)

##### 4.8.5.1 数字增量同步

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

importBuilder.setLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
```

##### 4.8.5.2 日期时间戳增量同步

两种类型日期时间戳增量

精度毫秒级：ImportIncreamentConfig.TIMESTAMP_TYPE

[精度纳秒级（Elasticsearch同步有用）](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/ES2CustomDemo.java)：ImportIncreamentConfig.LOCALDATETIME_TYPE

sql语句格式：

```sql
select * from td_sm_log where collecttime > #[collecttime]
```

日期类型增量导入配置

```java
importBuilder.setLastValueColumn("collecttime");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段

importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//字段类型：ImportIncreamentConfig.TIMESTAMP_TYPE时间戳类型

importBuilder.setLastValueType(ImportIncreamentConfig.LOCALDATETIME_TYPE);//指定字段类型：ImportIncreamentConfig.LOCALDATETIME_TYPE 支持纳秒时间精度,只对从elasticsearch增量采集数据起作用
```

##### 4.8.5.3 日期类型增量字段日期格式配置

可以指定日期增量字段日期格式，当增量字段为日期类型且日期格式不是默认的
```java
yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
```

时，需要设置字段相对应的日期格式，例如：
```java
yyyy-MM-dd HH:mm:ss
```
如果是默认utc格式，则不需要手动设置指定

```java
  importBuilder.setLastValueDateformat("yyyy-MM-dd HH:mm:ss");
```

 lastValueDateformat只对从elasticsearch增量采集数据起作用，因为elasticsearch返回非UTC格式日期字符串时，需要通过指定对应的日期格式，才能将字符串形式的日期转换为增量字段状态管理需要的Date类型。

LOCALDATETIME_TYPE类型(只对从elasticsearch增量采集数据起作用)时，默认的时间格式：

```java
yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'
```



##### 4.8.5.4 时间戳增量导出截止时间偏移量配置

###### 日期类型增量字段

日期类型增量导入，还可以设置一个导入截止时间偏移量。引入IncreamentEndOffset配置，主要是增量导出时，考虑到elasticsearch、mongodb这种存在写入数据的延迟性的数据库，设置一个相对于当前时间偏移量导出的截止时间，避免增量导出时遗漏数据。

```java
importBuilder.setIncreamentEndOffset(300);//单位秒，同步从上次同步截止时间当前时间前5分钟的数据，下次继续从上次截止时间开始同步数据
```



bboss会自动增加一个内部变量collecttime\_\_endTime（增量字段名称后面加上\_\_endTime后缀），这样我们增量同步sql就可以写成如下方式：

```sql
select * from td_sm_log where collecttime > #[collecttime] and collecttime <= 
#[collecttime__endTime] 
```

看一个增量时间戳同步的elasticsearch dsl用法

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
                         {   ## 增量检索范围，可以是时间范围，也可以是数字范围，这里采用的是数字增量字段
                            "range": {                               
                                "collecttime": { ## 时间增量检索字段
                                    "gt": #[collecttime],
                                    "lte": #[collecttime__endTime]
                                }                               
                            }
                        }

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
            "size":#[size],
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
                "id": #[sliceId], ## 必须使用sliceId作为变量名称
                "max": #[sliceMax] ## 必须使用sliceMax作为变量名称
            },
            "size":#[size],
            @{queryCondition}
        }
        ]]>
    </property>


</properties>

```

###### 数字类型时间戳增量字段

数字类型增量字段，如果通过ImportBuilder将数字值标记为时间戳类型，那么可以通过increamentEndOffset为数字时间戳增量查询增加一个查询截止时间戳条件：

```java
public ImportBuilder setNumberTypeTimestamp(boolean numberTypeTimestamp)
与以下方法配合一起使用（如果不设置increamentEndOffset，标识将不起作用）：
/**
*  对于有延迟的数据源，指定增量截止时间与当前时间的偏移量
*  增量查询截止时间为：System.currenttime - increamentEndOffset
*  对应的变量名称：getLastValueVarName()+"__endTime" 对应的值类型为long
*  单位：秒
* @return
*/
  public ImportBuilder setIncreamentEndOffset(Integer increamentEndOffset)
```

使用参考案例：[Milvus时间戳增量同步](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/milvus/Milvus2CustomNumerTimestampDemo.java)

##### 4.8.5.5 控制重启作业是否重新开始同步数据

setFromFirst的使用

```java
setFromfirst(false)，如果作业停了，作业重启后从上次停止的位置开始采集数据，
setFromfirst(true) 如果作业停了，作业重启后，重新开始位置开始采集数据
```

详细的增量导入案例：

源码文件 <https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Dbdemo.java>

```java
	public void testSimpleLogImportBuilderFromExternalDBConfig(){
		ImportBuilder importBuilder = new ImportBuilder() ;
		DBInputConfig dbInputConfig = new DBInputConfig();
		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// 需要设置setLastValueColumn信息log_id，
		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型

//		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
		dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
				.setDbName("test");
		importBuilder.setInputConfig(dbInputConfig);


//		importBuilder.addFieldMapping("LOG_CONTENT","message");
//		importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("default")
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

		importBuilder.setOutputConfig(elasticsearchOutputConfig)
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(5000);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				     .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
					 .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
//		importBuilder.setLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
		importBuilder.setFromFirst(true);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
			//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
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
		
	
		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作

		

		
	}
```

##### 4.8.5.6 设置增量同步增量字段起始值

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



##### 4.8.5.7 排序设置

bboss 5.9.3及之前的版本需要注意：如果增量字段默认自带排序功能（比如采用主键id作为增量字段），则sql语句不需要显式对查询的数据进行排序，否则需要在sql语句中显式基于增量字段升序排序：

```java
importBuilder.setSql("select * from td_sm_log where update_date > #[log_id] order by update_date asc");
```

bboss 5.9.3及后续的版本已经内置了对增量字段值的排序功能，所以在sql或者dsl中不需要额外进行排序设置，可以提升导入性能(但是如果作业重启后，续接采集时，可能会存在部分数据丢失问题，这种情况下就需要进行排序)。

##### 4.8.5.8 增量状态存储数据库

bboss默认采用sqlite保存增量状态，通过setLastValueStorePath方法设置sqlite数据库文件路径

```java
importBuilder.setLastValueStorePath("/app/data/testdb");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点
```

/app/data/testdb代表/app/data/目录下的sqlite数据库文件testdb，如果在同一个进程中运行多个数据采集作业，并且采用sqlite作为增量状态管理，由于sqlite的单线程数据库限制，必须每个作业一个独立的sqlite数据库，因此除了设置不同的sqlite数据库文件路径，还需指定不同的statusDBname，例如：

作业1

```java
importBuilder.setStatusDbname("job1");
importBuilder.setLastValueStorePath("/app/data/job1");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点
```

作业2

```java
importBuilder.setStatusDbname("job2");
importBuilder.setLastValueStorePath("/app/data/job2");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点
```

sqlite作为一个本地单线程文件数据库，可能在一些场景下无法满足要求，譬如要做监控界面实时查看作业数据采集状态，尤其是在采用分布式作业调度引擎时，定时增量导入需要指定mysql等关系型增量状态存储数据库。

bboss支持将增量状态保存到其他关系数据库中（譬如mysql），具体的配置方法如下：

[保存增量状态的数据源配置](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_6-%e4%bf%9d%e5%ad%98%e5%a2%9e%e9%87%8f%e7%8a%b6%e6%80%81%e7%9a%84%e6%95%b0%e6%8d%ae%e6%ba%90%e9%85%8d%e7%bd%ae)

##### 4.8.5.9.已完成增量状态记录过期清理机制设置

本功能主要适用于文件数据采集插件，参考文档：[设置已完成记录增量状态过期清理机制](https://esdoc.bbossgroups.com/#/filelog-guide?id=_14设置已完成记录增量状态过期清理机制)

##### 4.8.5.10  增量字段配置注意事项

增量字段配置注意事项：

数据库：增量字段必须出现在查询sql语句召回字段中

Elasticsearch：增量字段必须出现在dsl召回字段中

MongoDB：增量字段必须出现fethField字段清单中

如果增量字段没有出现在查询返回的数据记录中，就会获取不到增量值，从而导致数据增量采集无法实现。

##### 4.8.5.11 增量状态ID生成策略配置

设置增量状态ID生成策略，在设置jobId的情况下起作用，目前提供了两种策略：

**策略1** 采用jobType+jobId作为增量状态id

ImportIncreamentConfig.STATUSID_POLICY_JOBID 

**策略2** 采用[jobType]+[jobId]+[作业查询语句/文件路径等信息的hashcode]，作为增量id作为增量状态id

ImportIncreamentConfig.STATUSID_POLICY_JOBID_QUERYSTATEMENT 

**默认策略**：ImportIncreamentConfig.STATUSID_POLICY_JOBID_QUERYSTATEMENT

**策略配置方法**：

```java

/**
         * 设置增量状态ID生成策略，在设置jobId的情况下起作用
         * ImportIncreamentConfig.STATUSID_POLICY_JOBID 采用jobType+jobId作为增量状态id
         * ImportIncreamentConfig.STATUSID_POLICY_JOBID_QUERYSTATEMENT 采用[jobType]+[jobId]+[作业查询语句/文件路径等信息的hashcode]，作为增量id作为增量状态id
         * 默认值ImportIncreamentConfig.STATUSID_POLICY_JOBID_QUERYSTATEMENT
         */
        importBuilder.setStatusIdPolicy(ImportIncreamentConfig.STATUSID_POLICY_JOBID);

```

文件采集插件强制直接使用ImportIncreamentConfig.STATUSID_POLICY_JOBID_QUERYSTATEMENT策略

#### 4.8.6 定时全量导入

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
		ImportBuilder importBuilder = new ImportBuilder() ;
		DBInputConfig dbInputConfig = new DBInputConfig();
		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// 需要设置setLastValueColumn信息log_id，
		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型

//		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
		dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
				.setDbName("test");
		importBuilder.setInputConfig(dbInputConfig);


//		importBuilder.addFieldMapping("LOG_CONTENT","message");
//		importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("default")
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

		importBuilder.setOutputConfig(elasticsearchOutputConfig)
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(5000);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
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
		
	
		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作

		

		
	}
```

#### 4.8.7 定时任务指定执行拦截器使用

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
			public void throwException(TaskContext taskContext, Throwable e) {
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
			public void throwException(TaskContext taskContext, Throwable e) {
				System.out.println("throwException 1");
			}
		});
```

拦截器常被应用于任务上下文数据的定义和获取，***任务拦截器对kafka-to-target类型的数据同步作业***不起作用。

##### 4.8.7.1 任务上下文数据定义和获取

在一些特定场景下，避免任务执行过程中重复加载数据，需要在任务每次调度执行前加载一些任务执行过程中不会变化的数据,放入任务上下文TaskContext；任务执行过程中，直接从任务上下文中获取数据即可。例如：将每次任务执行的时间戳放入任务执行上下文。

通过TaskContext对象的addTaskData方法来添加上下文数据，通过TaskContext对象的getTaskData方法来获取任务上下文数据.

###### 4.8.7.1.1  定义任务上下文数据

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
         public void throwException(TaskContext taskContext, Throwable e) {
            System.out.println("throwException 1");
         }
      });
    //设置任务执行拦截器结束，可以添加多个
```

###### 4.8.7.1.2 获取任务上下文数据

在生成文件名称的接口方法中获取任务上下文数据

```java
fileFtpOupputConfig.setFilenameGenerator(new FilenameGenerator() {
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
fileFtpOupputConfig.setReocordGenerator(new ReocordGenerator() {
         @Override
         public void buildRecord(TaskContext context, CommonRecord record, Writer builder) {
            //SerialUtil.object2jsonDisableCloseAndFlush(record.getDatas(),builder);
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

###### 4.8.7.2 kafka输入插件拦截器设置说明

kafka输入插件拦截器收集作业metrics信息时，定时记录统计插件消费kafka数据记录情况，并调用任务拦截器的aftercall方法输出统计jobMetrics信息，可以指定统计时间间隔：

```java
kafka2InputConfig.setMetricsInterval(300 * 1000L);//300秒做一次任务拦截调用，默认值
```

#### 4.8.8 定时任务调度说明

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
elasticsearchOutputConfig.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id
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

#### 4.8.9 增量导入注意事项

参考[定时增量导入](https://esdoc.bbossgroups.com/#/db-es-tool?id=_285-%e5%ae%9a%e6%97%b6%e5%a2%9e%e9%87%8f%e5%af%bc%e5%85%a5)



#### 4.8.10 数据加工处理
实现数据结构修改调整，增加、修改字段，过滤记录等功能。

记录并行加工处理说明：

- 除数据库输入插件，其他输入插件采用并行模式执行数据加工方法datarefactor。

- [数据库](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_124-%e5%b9%b6%e8%a1%8c%e6%95%b0%e6%8d%ae%e5%8a%a0%e5%b7%a5%e5%a4%84%e7%90%86)默认采用串行模式执行，可以通过dbInputConfig.setParallelDatarefactor(true)切换为并行执行模式
- 并行加工处理模式只有在并行作业任务模式才起作用，参考章节【[4.3 串行执行和并行执行](https://esdoc.bbossgroups.com/#/db-es-tool?id=_43-%e4%b8%b2%e8%a1%8c%e6%89%a7%e8%a1%8c%e5%92%8c%e5%b9%b6%e8%a1%8c%e6%89%a7%e8%a1%8c)】

通过记录并行加工处理，可大幅提升数据加工处理性能。

##### 4.8.10.1 全局处理

可以通过importBuilder全局扩展添加字段到es索引中：

```java
        importBuilder.addFieldValue("testF1","f1value");
		importBuilder.addFieldValue("testInt",0);
		importBuilder.addFieldValue("testDate",new Date());
		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
        importBuilder.addIgnoreFieldMapping("subtitle");//全局忽略字段
        importBuilder.addFieldMapping("dbcolumn","esFieldColumn");//全局添加字段名称映射
```

##### 4.8.10.2 记录级别处理

如果需要针对单条记录，bboss提供org.frameworkset.tran.DataRefactor接口和Context接口像结合来提供对数据记录的自定义处理功能，这样就可以灵活控制文档数据结构，通过context可以对当前记录做以下调整：

| 数据处理类型             | 全局处理 | 记录级别 | 举例(全局通过importBuilder组件实现，记录级别通过context接口实现) |
| ------------------------ | -------- | -------- | ------------------------------------------------------------ |
| 添加字段                 | 支持     | 支持     | 全局处理：importBuilder.addFieldValue("testF1","f1value");                                             记录级别：context.addFieldValue("testF1","f1value"); |
| 添加对象中所有字段到记录 | 不支持   | 支持     | 将对象中的所有字段和值作为字段添加到记录中，忽略空值字段 ： Context addFieldValues( Object bean); 将对象中的所有字段和值作为字段添加到记录中 根据参数ignoreNullField控制是否忽略空值字段 true 忽略  false 不忽略      Context addFieldValues(Object bean,boolean ignoreNullField); |
| 添加map中所有字段到记录  | 不支持   | 支持     | 将map中的所有键值对作为字段添加到记录中，忽略空值字段      Context addMapFieldValues( Map<String,Object> values); 将map中的所有键值对作为字段添加到记录中       根据参数ignoreNullField控制是否忽略空值字段 true 忽略  false 不忽略       Context addMapFieldValues( Map<String,Object> values,boolean ignoreNullField); |
| 删除字段                 | 支持     | 支持     | 全局处理：importBuilder.addIgnoreFieldMapping("testInt");                                           记录级别：context.addIgnoreFieldMapping("testInt"); |
| 映射字段名称             | 支持     | 不支持   | 全局处理：importBuilder.addFieldMapping("document_id","docId"); |
| 映射字段名称并修改字段值 | 不支持   | 支持     | String oldValue = context.getStringValue("axx");                                                           String newvalue = oldValue+" new value";                context.newName2ndData("axx","newname",newvalue); |
| 修改字段值               | 不支持   | 支持     | //空值处理                                                                                                                            String local = context.getStringValue("local");if(local == null)   context.addFieldValue("local",""); |
| 值类型转换               | 不支持   | 支持     | //将long类型的creationTime字段转换为日期类型                                                             long creationTime = context.getLongValue("creationTime");          context.addFieldValue("creationTime",new Date(creationTime)); |
| 过滤记录                 | 不支持   | 支持     | String id = context.getStringValue("_id");//根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中                                           if(id.equals("5dcaa59e9832797f100c6806"))   context.setDrop(true); |
| ip地理位置信息转换       | 不支持   | 支持     | //根据session访问客户端ip，获取对应的客户地理位置经纬度信息、运营商信息、省地市信息IpInfo对象,并将IpInfo添加到Elasticsearch文档中                                                   String referip = context.getStringValue("referip");                                                                 if(referip != null){   IpInfo ipInfo = context.getIpInfoByIp(referip);                           if(ipInfo != null)      context.addFieldValue("ipInfo",ipInfo);} |
| 其他转换                 | 不支持   | 支持     | 在DataRefactor接口中对记录中的数据根据特定的要求进行相关转换和处理，然后使用上面列出的对应的处理方式将处理后的数据添加到记录中 |
| 获取原始记录对象         | 不支持   | 支持     | //除了通过context接口获取mongodb的记录字段，还可以直接获取当前的mongodb记录，可自行利用里面的值进行相关处理                                                                      DBObject record = (DBObject) context.getRecord(); |
| 忽略null值               | 支持     | -        | true是忽略null值存入elasticsearch，false是存入（默认值）importBuilder.setIgnoreNullValueField(true); |

全局数据处理配置：打tag，标识数据来源于jdk timer还是xxl-job

```java
importBuilder.addFieldValue("fromTag","jdk timer");  //jdk timer调度作业设置

importBuilder.addFieldValue("fromTag","xxl-jobr");  //xxl-job调度作业设置
```

通过Context接口方法getValue(String fieldName, java.sql.Types),在处理关系数据库数据时，获取字段对应类型的原始值：

```java
Object value = context.getValue("ACTIVE_TIME", Types.TIMESTAMP)
```

记录级别的转换处理参考下面的代码,举例说明如下：

###### 示例 1 添加修改删除字段以及删除记录

Elasticsearch会根据文档id自动根据记录状态对数据进行增删改处理，关系数据需要设置相应的insert、update、delete语句，可以参考[数据库输出插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_21-db%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)使用文档进行设置。

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

         	 //如果使用的数据源是数据库输入插件或者数据库输出插件对应的数据源，可以直接从对应插件中获取指定的数据源名称，否则需要定义额外的数据源，定义方法可以在作业外部进行定义（作业execute之前定义），亦可以通过作业初始化回调接口进行托管定义
             DBOutputConfig dbOutputConfig = (DBOutputConfig)context.getImportContext().getOutputConfig();
             String dbname = dbOutputConfig.getTargetDbname();
             
                //关联查询数据,单值查询
            //sql中有多个条件用逗号分隔追加
				Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,dbname,
																"select * from head where billid = ? and othercondition= ?",
																context.getIntegerValue("billid"),"otherconditionvalue");
             
            //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("headdata",headdata);
            //关联查询数据,多值查询
            List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,dbname,
                  "select * from facedata where billid = ?",
                  context.getIntegerValue("billid"));
            //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
            context.addFieldValue("facedatas",facedatas);
         }
      });
      //映射和转换配置结束
```

****

作业外自定义数据库数据源方法（销毁需要自行处理）可以参考文档：[2.15 自定义启动db数据源案例](https://esdoc.bbossgroups.com/#/db-es-tool?id=_215-%e8%87%aa%e5%ae%9a%e4%b9%89%e5%90%af%e5%8a%a8db%e6%95%b0%e6%8d%ae%e6%ba%90%e6%a1%88%e4%be%8b)

作业托管定义和销毁数据库数据源定义方法参考文档：[作业依赖资源初始化和销毁](https://esdoc.bbossgroups.com/#/db-es-tool?id=%e4%bd%9c%e4%b8%9a%e4%be%9d%e8%b5%96%e8%b5%84%e6%ba%90%e5%88%9d%e5%a7%8b%e5%8c%96%e5%92%8c%e9%94%80%e6%af%81)

###### 示例 2 标记记录状态

根据条件判断记录是否存在，如果存在将记录标记为update，否则按照默认状态insert处理

```java
ConfigSQLExecutor configSQLExecutor = new ConfigSQLExecutor("sql.xml");
       /**"
        * 重新设置数据结构
        */
       importBuilder.setDataRefactor(new DataRefactor() {
          public void refactor(Context context) throws Exception  {

              
                //根据条件判断记录是否存在，如果存在将记录标记为update，否则按照默认状态insert处理
                
                DBOutputConfig dbOutputConfig = (DBOutputConfig)context.getImportContext().getOutputConfig();
              //直接执行sql
                Integer count = SQLExecutor.queryObjectWithDBName(Integer.class,dbOutputConfig.getTargetDbname(),
                        "select count(1) from head where billid = ? and othercondition= ?",
                        context.getIntegerValue("billid"),"otherconditionvalue");
                if(count != null && count > 0 ){
                    context.markRecoredUpdate();
                }
                //执行配置文件中的sql
                count = configSQLExecutor.queryObjectWithDBName(Integer.class,dbOutputConfig.getTargetDbname(),
                        "countSql",//在sql.xml文件中配置的sql语句名称
                        context.getIntegerValue("billid"),"otherconditionvalue");
                if(count != null && count > 0 ){
                    context.markRecoredUpdate();
                }
                 

             
          }
       });
```

处理标记状态后，还需要设置修改的sql语句，具体设置方法，参考db输出插件使用文档：[db输出插件使](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_21-db%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)

**注意：**

***1.内嵌的数据库查询会有性能损耗，在保证性能的前提下，尽量将内嵌的sql合并的外部查询数据的整体的sql中，或者采用缓存技术消除内部sql查询。***

**2.一定要注意全局级和记录级调整区别：在DataRefactor接口中只能用Context来调整数据字段映射和字段添加修改和移除操作**

![](images\datarefactor.png)

##### 4.8.10.3 过滤记录

如果需要根据情况过滤特定的记录，可以通过以下方法将记录标记为过滤记录即可：

```java
 context.setDrop(true);
```

例如

```java
String id = context.getStringValue("_id");//根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中 
if(id.equals("5dcaa59e9832797f100c6806")){
	context.setDrop(true);
}
```

##### 4.8.10.4 获取记录元数据

可以通过context.getMetaValue(metaName)获取记录的元数据，比如文件信息、elasticsearch文档元数据、hbase元数据，使用示例：

获取elasticsearch文档id

```java
String docId = (String)context.getMetaValue("_id");
```

elasticsearch元数据清单

```java

      /**文档id信息*/
       private String  _id;
      /**文档对应索引类型信息*/
      private String  type;
      /**文档对应索引字段信息*/
      private Map<String, List<Object>> fields;
  
    /**文档对应版本信息*/
    private long version;
      /**文档对应的索引名称*/
    private String index;
      /**文档对应的高亮检索信息*/
    private Map<String,List<Object>> highlight;
      /**文档对应的排序信息*/
    private Object[] sort;
      /**文档对应的评分信息*/
    private Double  score;
      /**文档对应的父id*/
    private Object parent;
      /**文档对应的路由信息*/
    private Object routing;
      /**文档对应的是否命中信息*/
    private boolean found;
      /**文档对应的nested检索信息*/
    private Map<String,Object> nested;
      /**文档对应的innerhits信息*/
    private Map<String,Map<String, InnerSearchHits>> innerHits;
      /**文档对应的索引分片号*/
    private String shard;
      /**文档对应的elasticsearch集群节点名称*/
    private String node;
      /**文档对应的打分规则信息*/
    private Explanation explanation;

    private long seqNo;
      
    private long primaryTerm;
 
```

hbase元数据清单

```java
byte[] rowkey
Date timestamp
```

文件元数据清单

```java
Date @timestamp
Map @filemeta
    
filemeta的数据字段定义如下：

common.put("hostIp", BaseSimpleStringUtil.getIp());
common.put("hostName",BaseSimpleStringUtil.getHostName());
common.put("filePath",FileInodeHandler.change(file.getAbsolutePath()));

common.put("pointer",pointer);
common.put("fileId",fileInfo.getFileId());
FtpConfig ftpConfig = this.fileConfig.getFtpConfig();
if(ftpConfig != null){
    common.put("ftpDir",ftpConfig.getRemoteFileDir());
    common.put("ftpIp",ftpConfig.getFtpIP());
    common.put("ftpPort",ftpConfig.getFtpPort());
    common.put("ftpUser",ftpConfig.getFtpUser() != null?ftpConfig.getFtpUser():"-");
    common.put("ftpProtocol",ftpConfig.getTransferProtocolName());
}
```

context获取全部元数据方法：

```java
Map<String, Object> getMetaDatas()
//使用实例：将全部元数据转换为json格式输出
logger.info(SimpleStringUtil.object2json(record.getMetaDatas()));
```

##### 4.8.10.5 默认的字段映射配置

使用 数据采集同步默认字段映射功能，可以自动将文本类型记录按照特定的字符切割成一个数组结构，然后通过设置数组下标位置与目标字段名称、默认值、目标字段类型、目标字段格式的映射关系，从而快速实现非结构化数据到结构化数据转换映射处理。

具备默认字段映射功能的插件有：文本文件采集插件、Excel文件采集插件、Excel文件输出插件、Kafka输入插件，下面分别举例说明。

1）文件采集插件字段映射配置

文件采集插件字段映射配置
```java
FileInputConfig fileInputConfig = new FileInputConfig();

FileConfig fileConfig = new FileConfig();
fileConfig.setFieldSplit(";");//指定日志记录字段分割符
//简单的映射，指定字段映射配置
fileConfig.addCellMapping(0, "logOperTime");
fileConfig.addCellMapping(1, "operModule");
fileConfig.addCellMapping(2, "logOperuser");
//指定字段映射配置--日期类型映射，自动将对应位置的数据按照指定的日期格式转换为日期类型
fileConfig.addDateCellMapping(0, "logOperTime", CELL_STRING, "2022-08-09 12:30:50", "yyyy-MM-dd HH:mm:ss");//指定数据类型，默认值，日期格式
fileConfig.addCellMappingWithType(1, "operModule", CELL_STRING );//指定数据类型
fileConfig.addCellMappingWithType(2, "logOperuser", CELL_NUMBER_INTEGER, 20);//指定数据类型和默认值
```
完整的文本文件采集映射配置案例

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/metrics/SFTPFileLog2ESWithMetricsDemo.java

2) kafka映射配置

kafka映射配置
```java
Kafka2InputConfig kafka2InputConfig = new Kafka2InputConfig();
kafka2InputConfig.setValueCodec(CODEC_TEXT_SPLIT);
kafka2InputConfig.setFieldSplit(";");//指定消息字段分割符，按照分隔符将消息切割为字符串数组
//指定数组元素与字段名称的映射配置，
kafka2InputConfig.addCellMapping(0, "logOperTime");
kafka2InputConfig.addCellMapping(1, "operModule");
kafka2InputConfig.addCellMapping(2, "logOperuser");
```
完整的kafka2Elasticsearch映射案例

https://gitee.com/bboss/kafka2x-elasticsearch/tree/master/src/main/java/org/frameworkset/elasticsearch/imp/KafkaStringMapping2ESdemo.java

3) Excel输入插件字段映射配置

Excel输入插件字段映射配置
```java
//配置excel文件列与导出字段名称映射关系
    FileConfig excelFileConfig = new ExcelFileConfig();
    excelFileConfig
        .addCellMapping(0,"shebao_org")
        .addCellMapping(1,"person_no")
        .addCellMapping(2,"name")
        .addCellMapping(3,"cert_type")

        .addCellMapping(4,"cert_no","")
        .addCellMapping(5,"zhs_item")

        .addCellMapping(6,"zhs_class")
        .addCellMapping(7,"zhs_sub_class")
        .addCellMapping(8,"zhs_year","2022")
        .addCellMapping(9,"zhs_level","1")
        .addCellMappingWithType(10,"xxx",CELL_DATE );
```
完整的Excel2DB案例

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/ExcelFile2DBDemo.java

4) Excel输出映射配置

Excel输出映射配置
```java
 ExcelFileOutputConfig fileOutputConfig = new ExcelFileOutputConfig();
        fileOutputConfig.setTitle("新闻详情数据");
        fileOutputConfig.setSheetName("2021年新闻");

        fileOutputConfig.addCellMapping(0,"title","文章标题")
                .addCellMapping(1,"subtitle","文章子标题")
                .addCellMapping(2,"newcollecttime","*新闻发布时间")
                .addCellMapping(3,"author","*作者")
                .addCellMapping(4,"operModule","*新闻板块")
                .addCellMapping(5,"logContent","*新闻内容")
                .addCellMapping(6,"logVisitorial","发布IP")
        ;
```
完整的Excel输出字段映射配置案例：

https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2ExcelFileFtpDemo.java

5) 映射类型常量CellType说明

```java
public static final int CELL_BOOLEAN = 5;
public static final int CELL_DATE = 3;

public static final int CELL_NUMBER = 2;
public static final int CELL_NUMBER_INTEGER = 6;
public static final int CELL_NUMBER_LONG = 7;
public static final int CELL_NUMBER_FLOAT = 8;
public static final int CELL_NUMBER_SHORT = 9;
public static final int CELL_STRING = 1;
```
##### 4.8.10.6 消息key设置

可以直接通过context设置kafka、Rocketmq输出插件的消息key，使用实例：

```java
/**
         * 设置消息可以
         */
        importBuilder.setDataRefactor(new DataRefactor() {
            public void refactor(Context context) throws Exception  { 
                //直接通过context设置kafka、Rocketmq输出插件的消息key
                context.setMessageKey("testKey"); 
            }
        });
```

### 4.8.11 IP-地区运营商经纬度坐标转换

与geolite2 和ip2region相结合，bboss 支持将ip地址转换为国家-省份-城市-运营商-经纬度坐标信息，我们在DataRefactor中，可以获取ip对应的运营商和地区信息，举例说明：

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

首先需下载最新的开源信息库

geolite2 ip库文件：

 https://dev.maxmind.com/geoip/geoip2/geolite2/#Downloads 

ip2region 库文件：

https://github.com/lionsoul2014/ip2region/blob/master/data/ip2region.db

ip地址库设置方式有两种：

- 方式1 在appliction.properties文件中配置ip地址信息库（可选，如果有elasticsearch数据源时有效，否则需采用方式2）

在application.properties文件中配置对应的ip信息库文件地址

```properties
ip.cachesize = 10000
# geoip的ip地址信息库下载地址https://dev.maxmind.com/geoip/geoip2/geolite2/
ip.database = E:/workspace/geolite2/GeoLite2-City.mmdb
ip.asnDatabase = E:/workspace/geolite2/GeoLite2-ASN.mmdb
ip.ip2regionDatabase=E:/workspace/ipdb/ip2region.db
```



- 方式2 代码中直接设置ip地址信息库

  ```java
  importBuilder.setGeoipDatabase("E:/workspace/geolite2/GeoLite2-City.mmdb");
  importBuilder.setGeoipAsnDatabase("E:/workspace/geolite2/GeoLite2-ASN.mmdb");
  	importBuilder.setGeoip2regionDatabase("d:/geolite2/ip2region.db");
  ```

### 4.8.12 设置任务执行结果回调处理函数

我们通过importBuilder的setExportResultHandler方法设置任务执行结果以及异常回调处理函数，函数实现接口即可：

org.frameworkset.tran.ExportResultHandler

```java
//设置数据bulk导入任务结果处理回调函数，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务通过error方法进行相应处理
importBuilder.setExportResultHandler(new ExportResultHandler() {
   @Override
   public void success(TaskCommand taskCommand, Object result) {
      String datas = taskCommand.getDatas();//执行的批量数据
      System.out.println(result);//打印成功结果
   }

   @Override
   public void error(TaskCommand taskCommand, Object result) {
      //具体怎么处理失败数据可以自行决定,下面的示例显示重新导入失败数据的逻辑：
      // 从result中分析出导入失败的记录，然后重新构建data，设置到taskCommand中，重新导入，
      // 支持的导入次数由getMaxRetry方法返回的数字决定
      // String failDatas = ...;
      //taskCommand.setDatas(failDatas);
      //taskCommand.execute();
      String datas = taskCommand.getDatas();//执行的批量数据
      System.out.println(result);//打印失败结果
   }
@Override
			public void exception(TaskCommand taskCommand, Throwable exception) {
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

#### 4.8.12.1 kafka输出插件任务状态记录说明

kafka输出插件任务状态记录管理功能，可以采用指标分析模块对发送记录统计信息，按照指定的时间窗口进行聚合计算后在回调任务处理success方法，taskMetrics信息为聚合后的统计信息，可以通过开关控制是否进行预聚合功能，避免频繁采集每条记录任务的metrics信息。

```java
kafkaOutputConfig.setEnableMetricsAgg(true);//启用预聚合功能
kafkaOutputConfig.setMetricsAggWindow(60);//指定统计时间窗口，单位：秒，默认值60秒
```

### 4.8.13 灵活指定索引名称和索引类型

可以全局通过importBuilder组件设置索引类型和索引名称，也可以通过Context接口为相关的数据记录指定索引类型和索引名称：

- 如果没有在记录级别指定索引名称则采用全局指定索引名称，如果在记录级别指定了索引名称则采用记录级别指定的索引名称

- 如果没有在记录级别指定索引类型则采用全局指定索引类型，如果在记录级别指定了索引类型则采用记录级别指定的索引类型

#### 4.8.13.1 importBuilder组件全局设置索引类型和索引名称

```java
ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
        elasticsearchOutputConfig.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //elasticsearch7之前必填项，之后的版本不需要指定
```

#### 4.8.13.2 通过Context接口设置记录索引类型和索引名称

```java
final Random random = new Random();
		importBuilder.setDataRefactor(new DataRefactor() {
			@Override
			public void refactor(Context context) throws Exception {
				int r = random.nextInt(3);
				if(r == 1) {
					
					context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
				}
				else if(r == 0) {
					
					context.setIndex("contextxxx-{dateformat=yyyy.MM.dd}");
					
				}
				else if(r == 2){
					
					context.setIndex("contextbbbbb-{dateformat=yyyy.MM.dd}");
				}

			}
		});
```

#### 4.8.13.3 动态设置index和type

```properties
索引名称由demowithesindex和日期类型字段agentStarttime通过yyyy.MM.dd格式化后的值拼接而成=
dbclobdemo-{agentStarttime,yyyy.MM.dd}=
 
索引名称由demowithesindex和当前日期通过yyyy.MM.dd格式化后的值拼接而成=
demowithesindex-{dateformat=yyyy.MM.dd}

索引名称由demowithesindex和日期类型字段agentStarttime通过yyyy.MM.dd格式化后的值拼接而成=
demowithesindex-{field=agentStarttime,dateformat=yyyy.MM.dd}

索引类型为typeFieldName字段对应的值=
{field=typeFieldName}
或者{typeFieldName}=
```

示例如下：

```java

ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
        elasticsearchOutputConfig.setIndex("demo-{dateformat=yyyy.MM.dd}"); //必填项
				//.setIndexType("dbclobdemo") //elasticsearch7之前必填项，之后的版本不需要指定
```

```java
ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
        elasticsearchOutputConfig.setIndex("demo-{agentStarttime,yyyy.MM.dd}"); //必填项
				//.setIndexType("dbclobdemo") //elasticsearch7之前必填项，之后的版本不需要指定
```

#### 4.8.13.4 设置routing的方法

在DataRefactor中指定routing值

```java
importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
			     
				org.frameworkset.tran.config.ClientOptions clientOptions = new org.frameworkset.tran.config.ClientOptions();
				clientOptions.setRouting("1");
				context.setClientOptions(clientOptions);
				
			}
		});
```

通过importBuilder全局指定routing field，将对应字段的值作为routing：

```java
org.frameworkset.tran.config.ClientOptions clientOptions = new org.frameworkset.tran.config.ClientOptions();
clientOptions.setRoutingField(new ESField("parentid"));
elasticsearchOutputConfig.setClientOptions(clientOptions);
```

#### 4.8.14 数据库ResultSet Stream机制说明

可以利用JDBC驱动的ResultSet Stream机制来同步数据库中的大表数据，从而避免因数据量过大导致jvm内存溢出等问题，ResultSet Stream机制设置方式如下：

```java
DBInputConfig dbInputConfig = new DBInputConfig();
//通过设置JdbcFetchSize来控制预fetch记录数
dbInputConfig.setJdbcFetchSize(2000);
```

部分数据库，例如oracle默认具备fetch机制，只需要在必要时setJdbcFetchSize即可，无需进行特殊处理，但是mysql和postgresql开启流处理机制比较特殊，说明如下。

##### 4.8.14.1 Mysql流处理机制

同步Mysql 大数据表到Elasticsearch或者其他数据源时，针对jdbc fetchsize（ResultSet Stream）的使用比较特殊，mysql提供了两种机制来处理：

**机制一** mysql 5以后的版本采用jdbc url串参数useCursorFetch=true以及配置fetchsize属性来实现，数据库url做如下配置即可：

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
        dbInputConfig.setDbName("test")
                .setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
                .setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false")//没有带useCursorFetch=true参数，jdbcFetchSize参数配置为-2147483648，否则不会生效  
                 .setJdbcFetchSize(-2147483648);
                .setDbUser("root")
                .setDbPassword("123456")
                .setValidateSQL("select 1")
                .setUsePool(true);//是否使用连接池
```

##### 4.8.14.2 Postgresql流处理机制

一般可以全局设置jdbcFetchSize开启jdbc流处理机制，postgresql通过jdbcFetchSize开启流处理机制必须在数据库连接上开启数据库事务才会生效，为了避免导致所有jdbc链接被自动设置为启用事务,bboss持久层针对postgresql查询操作，默认全局禁用jdbcFetchSize设置，当需要启用postgresq的流处理机制进行大数据量查询时，可通过设置jdbcFetchSize进行启用，配置方法如下:

```java
DBInputConfig dbInputConfig = new DBInputConfig();
//通过设置JdbcFetchSize来控制预fetch记录数
dbInputConfig.setJdbcFetchSize(2000);
```

另外bboss持久层查询api可通过DBOptions参数设置jdbcFetchSize，从而启用Postgresql流处理机制方法，实例如下：

```java
DBOptions dbOptions = null;
        Integer fetchSize = dbInputConfig.getFetchSize();
		if(fetchSize != null && fetchSize != 0) {
            dbOptions = new DBOptions();
            dbOptions.setFetchSize(fetchSize);
		}
	
if (executor == null) {
				SQLExecutor.queryBeanWithDBNameByNullRowHandler(dbOptions,resultSetHandler, sourceDBName, dbInputConfig.getSql(), dataTranPlugin.getParamValue(params));
			} else {
				executor.queryBeanWithDBNameByNullRowHandler(dbOptions,resultSetHandler, sourceDBName, dbInputConfig.getSqlName(), dataTranPlugin.getParamValue(params));

			}
```

#### 4.8.15 用配置文件来管理同步sql

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

#### 4.8.16 设置ES数据导入控制参数

数据同步工具可以全局设置Elasticsearch请求控制参数（基于importBuilder组件设置），也可以在记录级别设置Elasticsearch请求控制参数（基于Context接口设置），这里举例进行说明：

##### 4.8.16.1 全局设置Elasticsearch请求控制参数

可以通过elasticsearchOutputConfig直接提供的方法设置数据导入Elasticsearch的各种控制参数，例如routing,esid,parentid,refresh策略，版本信息等等：

```java
elasticsearchOutputConfig.setEsIdField("documentId")//可选项，es自动为文档产生id
				.setEsParentIdField("documentParentid") //可选项,如果不指定，文档父子关系父id对应的字段
				.setRoutingField("routingId") //可选项		importBuilder.setRoutingValue("1");
				.setEsDocAsUpsert(true)//可选项
				.setEsRetryOnConflict(3)//可选项
				.setEsReturnSource(false)//可选项
				.setEsVersionField(“versionNo”)//可选项
				.setEsVersionType("internal")//可选项
                .setRefreshOption("refresh=true&version=1");//可选项，通过RefreshOption可以通过url参数的方式任意组合各种控制参数
```

还可以通过ClientOptions对象来指定控制参数，使用示例：

```java
		elasticsearchOutputConfig.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id
		ClientOptions clientOptions = new ClientOptions();
//		clientOptions.setPipeline("1");
		clientOptions.setRefresh("true");
//		routing
//				(Optional, string) Target the specified primary shard.
		clientOptions.setRouting("2");
		clientOptions.setTimeout("50s");
		clientOptions.setWaitForActiveShards(2);
		elasticsearchOutputConfig.setClientOptions(clientOptions);
```

##### 4.8.16.2 记录级别设置Elasticsearch请求控制参数

基于Context接口，可以在记录级别设置Elasticsearch请求控制参数，记录级别会继承importBuilder设置的控制参数设置的控制参数,但是会覆盖通过elasticsearchOutputConfig设置的同名控制参数，记录级别控制参数使用示例：

```java
final Random random = new Random();
		importBuilder.setDataRefactor(new DataRefactor() {
			@Override
			public void refactor(Context context) throws Exception {
				int r = random.nextInt(3);
				if(r == 1) {
					ClientOptions clientOptions = new ClientOptions();
					clientOptions
							.setEsRetryOnConflict(1)
//							.setPipeline("1")

							.setOpType("index")
							.setIfPrimaryTerm(2l)
							.setIfSeqNo(3l)
					;//create or index
					context.setClientOptions(clientOptions);
					//context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
				}
				else if(r == 0) {
					ClientOptions clientOptions = new ClientOptions();

					clientOptions.setDetectNoop(false)
							.setDocasupsert(false)
							.setReturnSource(true)
							.setEsRetryOnConflict(3)
					;//设置文档主键，不设置，则自动产生文档id;
					context.setClientOptions(clientOptions);
					//context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
					context.markRecoredUpdate();
				}
				else if(r == 2){
					ClientOptions clientOptions = new ClientOptions();
					clientOptions.setEsRetryOnConflict(2);
//							.setPipeline("1");
					context.setClientOptions(clientOptions);
					context.markRecoredDelete();
					//context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
				}

			}
		});
```

参考文档：

 [基于refreshoption参数指定添加修改文档控制参数](https://esdoc.bbossgroups.com/#/development?id=_481-基于refreshoption参数指定添加修改文档控制参数) 

Elasticsearch控制参数参考文档：

 https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html 

#### 4.8.17 数据同步任务执行统计信息获取

##### 4.8.17.1 任务日志相关设置

```java
 importBuilder.setPrintTaskLog(true);//true 打印作业任务执行统计日志，false 不打印作业任务统计信息
importBuilder.setLogsendTaskMetric(5000l); //可以设定打印日志最大时间间隔，当打印日志到日志文件或者控制台时，判断是否满足最大时间间隔，满足则输出，不满足则不输出日志
```

对于Elasticsearch写入和查询dsl日志的控制，可以参考文档进行关闭和打开

DSL脚本调试日志开关，将showTemplate设置为true，同时将日志级别设置为INFO，则会将query dsl脚本输出到日志文件中：

```properties
    elasticsearch.showTemplate=true  ## true 打印dsl（logger必须设置info级别） false 不打印dsl
```

spring boot配置项

```properties
    spring.elasticsearch.bboss.elasticsearch.showTemplate=true  ## true 打印dsl（logger必须设置为info级别） false 不打印dsl
```

##### 4.8.17.2 任务级别统计信息

通过数据同步任务执行结果回调处理函数，可以获取到每个任务的详细执行统计信息：

```java
importBuilder.setExportResultHandler(new ExportResultHandler() {
			@Override
			public void success(TaskCommand taskCommand, Object result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
			}

			@Override
			public void error(TaskCommand taskCommand, Object result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
			}

			@Override
			public void exception(TaskCommand taskCommand, Throwable exception) {
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
    "jobStartTime": 2022-03-24 14:46:52, //作业开始时间，date类型，输出json时被转换为long值
    "taskStartTime": 2022-03-24 14:46:53,//当前任务开始时间，date类型，输出json时被转换为long值
    "taskEndTime": 2022-03-24 14:46:53,//当前任务结束时间，date类型，输出json时被转换为long值
    "totalRecords": 4, //作业处理总记录数
    "totalFailedRecords": 0,//作业处理总失败记录数
    "totalIgnoreRecords": 0,//作业处理总忽略记录数
    "totalSuccessRecords": 4,//作业处理总成功记录数
    "successRecords": 2,//当前任务处理总成功记录数
    "failedRecords": 0,//当前任务处理总失败记录数
    "ignoreRecords": 0,//当前任务处理总忽略记录数    
    "taskNo": 3,//当前任务编号
    "lastValue": 1998,//任务截止增量字段值或者增量时间戳    
    "jobNo": "eece3d34320b490a980d3f501cb7ae8c" //任务对应的作业编号，一个作业会被拆分为多个任务执行
}
```



##### 4.8.17.3 作业级别统计信息



通过CallInterceptor拦截器接口，在作业调度执行完成的时候，可以在方法afterCall和throwException中，通过taskContext.getJobTaskMetrics()获取作业任务统计信息，示例如下：

```java
//设置任务执行拦截器，可以添加多个
importBuilder.addCallInterceptor(new CallInterceptor() {
   @Override
   public void preCall(TaskContext taskContext) {

      String formate = "yyyyMMddHHmmss";
      //HN_BOSS_TRADE00001_YYYYMMDDHHMM_000001.txt
      SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
      String time = dateFormat.format(new Date());
      //可以在preCall方法中设置任务级别全局变量，然后在其他任务级别和记录级别接口中通过taskContext.getTaskData("time");方法获取time参数
      taskContext.addTaskData("time",time);

   }

   @Override
   public void afterCall(TaskContext taskContext) {
      logger.info(taskContext.getJobTaskMetrics().toString());
   }

   @Override
   public void throwException(TaskContext taskContext, Throwable e) {
      logger.info(taskContext.getJobTaskMetrics().toString(),e);
   }
});
```

打印的统计信息格式如下：

```properties
JobNo:558e370ae01041c4baf4835882fc6a77,JobStartTime:2022-03-24 14:46:52,JobEndTime:2022-03-24 14:46:53,Total Records:497,Total Success Records:497,Total Failed Records:0,Total Ignore Records:0,Total Tasks:50,lastValue: 1998//任务截止增量字段值或者增量时间戳
```

往kafka推送数据时，异步特性，因为任务全部提交完成后，数据还未发送完毕，回调afterCall方法时，作业级别统计信息可能不完整，如果需要完整的统计信息，可以调用方法来等待统计完成，例如：

```java
		@Override
			public void afterCall(TaskContext taskContext) {
				taskContext.await();
                //taskContext.await(100000l); //指定一个最长等待时间
				logger.info("afterCall ----------"+taskContext.getJobTaskMetrics().toString());
			}
```

##### 4.8.17.4 kafka输出插件设置

kafka输出插件任务状态记录管理功能，可以采用指标分析模块对发送记录统计信息，按照指定的时间窗口进行聚合计算后在回调任务处理success方法，taskMetrics信息为聚合后的统计信息，可以通过开关控制是否进行预聚合功能，避免频繁采集每条记录任务的metrics信息。

```java
kafkaOutputConfig.setEnableMetricsAgg(true);//启用预聚合功能
kafkaOutputConfig.setMetricsAggWindow(60);//指定统计时间窗口，单位：秒，默认值60秒
```

##### 4.8.17.5 事件型输入插件设置

kafka和mysql binlog属于监听型输入插件，其拦截器收集作业metrics信息时，定时记录统计插件消费数据记录情况，并调用任务拦截器的aftercall方法输出统计jobMetrics信息，可以指定统计时间间隔：

```java
kafka2InputConfig.setMetricsInterval(300 * 1000L);//30秒时间间隔做一次任务拦截器调用
```

#### 4.8.17.6 在作业处理过程手动记录和上报日志

bboss提供一个完备的日志记录模块，以便在作业处理过程中根据需要自行记录和输出作业和任务的日志和异常，具体使用，可以参考文档：

- [ETL作业监控日志使用介绍](https://esdoc.bbossgroups.com/#/metrics-logs)

#### 4.8.18 设置并行导入参数

代码里面加上下面参数，可以并行导入，导入速度会更快
```java
importBuilder.setParallel(true);//设置为多线程并行批量导入
importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
```

#### 4.8.19 同步增删改数据到ES

数据同步工具可以非常方便地将各种数据源（Elasticsearch、DB、Mongodb等）的增删改操作同步到Elasticsearch中。在DataRefactor接口中，通过Context接口提供的三个方法来标注记录的增、删、改数据状态，同步工具根据记录状态的来实现对Elasticsearch的新增、修改、删除同步操作：

```java
context.markRecoredInsert();//添加，默认值,如果不显示标注记录状态则默认为添加操作，对应Elasticsearch的index操作

context.markRecoredUpdate();//修改，对应Elasticsearch的update操作

context.markRecoredDelete();//删除，对应Elasticsearch的delete操作
```

使用示例：

```java
final Random random = new Random();
        importBuilder.setDataRefactor(new DataRefactor() {
            @Override
            public void refactor(Context context) throws Exception {
                int r = random.nextInt(3);
                if(r == 1) {
                    context.markRecoredInsert();
                    //context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
                }
                else if(r == 0) {
                   
                    //context.setIndex("contextdbdemo1-{dateformat=yyyy.MM.dd}");
                    context.markRecoredUpdate();
                }
                else if(r == 2){
                 
                    context.markRecoredDelete();
                    //context.setIndex("contextdbdemo2-{dateformat=yyyy.MM.dd}");
                }

            }
        });
```
可以从数据源直接获取增删改的数据：

![](images\direct-elasticsearch-crud.png)

也可以先将需要增删改的数据推送到kafka，同步工具从kafka接收增删改数据，再进行相应的处理：
![](images\kafka-elasticsearch-crud.png)

[Mysql binlog输入插件实现mysql增删改数据同步](https://esdoc.bbossgroups.com/#/mysql-binlog?id=mysql-binlog输入插件使用指南)

[MongoDB CDC插件实现MongoDB增删改数据同步](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_18-mongodb-cdc插件)

#### 4.8.20 同步数据到多个ES集群

bboss可以非常方便地将数据同步到多个ES集群，本小节介绍使用方法。

importBuilder组件指定多ES集群的方法如下：

```java
elasticsearchOutputConfig.setTargetElasticsearch("default,test");
```

多个集群数据源名称用逗号分隔，多ES集群数据源配置参考文档：

[5.2 多elasticsearch服务器集群支持](https://esdoc.bbossgroups.com/#/development?id=_52-多elasticsearch服务器集群支持)

#### 4.8.21 导入日期类型数据少8小时问题

在数据导入时，如果是时间类型，Elasticsearch默认采用UTC时区（而不是东八区时区）保存日期数据，如果通过json文本查看数据，会发现少8小时，这个是正常现象，通过bboss orm检索数据，日期类型数据就会自动将UTC时区转换为东八区时间（也就是中国时区，自动加8小时）

#### 4.8.22 记录切割

在数据导入时，有时需将单条记录切割为多条记录，通过设置切割字段以及SplitHandler接口来实现，可以将指定的字段拆分为多条新记录，新产生的记录会自动继承原记录其他字段数据，亦可以指定覆盖原记录字段值,示例代码如下：

```java
      importBuilder.setSplitFieldName("@message");
		importBuilder.setSplitHandler(new SplitHandler() {
			/**
			 * 将记录字段值splitValue切割为多条记录，如果方法返回null，则继续将原记录写入目标库
			 * @param taskContext
			 * @param record
			 * @param fieldValue
			 * @return List<KeyMap<String, Object>> KeyMap是LinkedHashMap的子类，添加key字段，如果是往kafka推送数据，可以设置推送的key
			 */
			@Override
			public List<KeyMap> splitField(TaskContext taskContext,//调度任务上下文
										   Record record,//原始记录对象
										   Object fieldValue) {//待切割的字段值
				//如果@message不是一个数组格式的json，那么就不要拆分原来的记录，直接返回null就可以了
				String data =  String.valueOf(fieldValue);
				if(!data.startsWith("["))
					return null;
				//把@message字段进行切割为一个List<Map>对象
				List<Map> datas = SimpleStringUtil.json2ListObject(data, Map.class);
				List<KeyMap> splitDatas =  new ArrayList<>(datas.size());
				for(int i = 0; i < datas.size(); i ++){
					Map map = datas.get(i);
					KeyMap keyMap = new KeyMap();
					keyMap.put("@message",map);//然后循环将map再放回新记录，作为新记录字段@message的值
					splitDatas.add(keyMap);
				}
				return splitDatas;
			}
		});
```

上面的列子是把@message字段进行切割为一个List<Map>对象，然后循环将map再放回新记录，作为新记录字段@message的值，需要注意的是如果@message不是一个数组格式的json，那么就不要拆分原来的记录，直接返回null就可以了：

```java
String data =  String.valueOf(fieldValue);
if(!data.startsWith("["))
   return null;//保留原记录，不切割
List<Map> datas = SimpleStringUtil.json2ListObject(data, Map.class);
```

最后一个注意事项，如果我们在最终的输出字段中，需要将@message名称变成名称message,那么只需加上以下代码即可：

//将@message名称映射转换为message

​		importBuilder.addFieldMapping("@message","message");

完整的代码：

```java
importBuilder.setSplitFieldName("@message");
		importBuilder.setSplitHandler(new SplitHandler() {
			/**
			 * 将记录字段值splitValue切割为多条记录，如果方法返回null，则继续将原记录写入目标库
			 * @param taskContext
			 * @param record
			 * @param fieldValue
			 * @return List<KeyMap<String, Object>> KeyMap是LinkedHashMap的子类，添加key字段，如果是往kafka推送数据，可以设置推送的key
			 */
			@Override
			public List<KeyMap> splitField(TaskContext taskContext,//调度任务上下文
										   Record record,//原始记录对象
										   Object fieldValue) {//待切割的字段值
				//如果@message不是一个数组格式的json，那么就不要拆分原来的记录，直接返回null就可以了
				String data =  String.valueOf(fieldValue);
				if(!data.startsWith("["))
					return null;
				//把@message字段进行切割为一个List<Map>对象
				List<Map> datas = SimpleStringUtil.json2ListObject(data, Map.class);
				List<KeyMap> splitDatas =  new ArrayList<>(datas.size());
				for(int i = 0; i < datas.size(); i ++){
					Map map = datas.get(i);
					KeyMap keyMap = new KeyMap();
					keyMap.put("@message",map);//然后循环将map再放回新记录，作为新记录字段@message的值
					splitDatas.add(keyMap);
				}
				return splitDatas;
			}
		});
		//将@message名称映射转换为message
		importBuilder.addFieldMapping("@message","message");
```

生成的正确记录如下：

```json
{"uuid":"7af4eee7-61d7-4ab8-8678-117fd6f37e24","message":{"userId":"123457","userName":"李四3","yearMonth":"202104","readTime":"20210401","payTime":"20210501","waterNum":"100","waterType":"工业用水"},"@timestamp":"2021-10-12T02:45:06.419Z","@filemeta":{"hostName":"DESKTOP-U3V5C85","pointer":1354,"hostIp":"169.254.252.194","filePath":"D:/workspace/bbossesdemo/kafka2x-elasticsearch/data/waterinfo_20210811211501009.json","fileId":"D:/workspace/bbossesdemo/kafka2x-elasticsearch/data/waterinfo_20210811211501009.json"}}
```



#### 4.8.23 自定义处理器

通过自定义处理采集数据功能，可以自行将采集的数据按照自己的要求进行处理到目的地，支持数据来源包括：database，elasticsearch，kafka，mongodb，hbase，file，ftp等，想把采集的数据保存到什么地方，有自己实现CustomOutPut接口处理即可，例如：

```java
ImportBuilder importBuilder = new ImportBuilder();
		importBuilder.setBatchSize(10)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
		/**
		 * es相关配置
		 */
		ElasticsearchInputConfig elasticsearchInputConfig = new ElasticsearchInputConfig();
		elasticsearchInputConfig
				.setDslFile("dsl2ndSqlFile.xml")
				.setDslName("scrollQuery")
				.setScrollLiveTime("10m")
//				.setSliceQuery(true)
//				.setSliceSize(5)
				.setQueryUrl("dbdemo/_search");
		importBuilder.setInputConfig(elasticsearchInputConfig)
				.setIncreamentEndOffset(5);

		//自己处理数据
		CustomOupputConfig customOupputConfig = new CustomOupputConfig();
		customOupputConfig.setCustomOutPut(new CustomOutPut() {
			@Override
			public void handleData(TaskContext taskContext, List<CommonRecord> datas) {

				//You can do any thing here for datas
				//单笔记录处理
				RedisHelper redisHelper = null;
				RedisHelper redisHelper1 = null;
				try {
					redisHelper = RedisFactory.getRedisHelper();
					redisHelper1 = RedisFactory.getRedisHelper("redis1");

					for (CommonRecord record : datas) {
						Map<String, Object> data = record.getDatas();
						String LOG_ID =String.valueOf(data.get("LOG_ID"));
//					logger.info(SimpleStringUtil.object2json(data));
						String valuedata = SimpleStringUtil.object2json(data);
						logger.debug("LOG_ID:{}",LOG_ID);
//					logger.info(SimpleStringUtil.object2json(data));
						redisHelper.hset("xingchenma", LOG_ID, valuedata);
						redisHelper.hset("xingchenma", LOG_ID, valuedata);
					}
				}
				finally {
					if(redisHelper != null)
						redisHelper.release();
					if(redisHelper1 != null)
						redisHelper1.release();
				}
			}
		});
		importBuilder.setOutputConfig(customOupputConfig);
```

自定义处理采集数据功能典型的应用场景就是对接大数据流处理，直接将采集的数据交给一些流处理框架，譬如与我们内部自己开发的大数据流处理框架对接，效果简直不要不要的，哈哈。

[采集日志文件自定义处理案例](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/FileLog2CustomDemo.java)

### 4.9 作业异常处理

作业在运行过程中出现异常，一般会自动退出作业执行，有些异常情况，不会自动退出，可以通过ContinueOnError标识进行控制：任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行

```java
importBuilder.setContinueOnError(true);//任务出现异常，继续执行作业
importBuilder.setContinueOnError(false);//任务出现异常，停止执行作业

```



### 4.10 作业参数配置

在使用[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)时，为了避免调试过程中不断打包发布数据同步工具，可以将需要调整的参数配置到启动配置文件src\test\resources\application.properties中,然后在代码中通过以下方法获取配置的参数：



```
#工具主程序
mainclass=org.frameworkset.elasticsearch.imp.Dbdemo

# 参数配置
# 在代码中获取方法：propertiesContainer.getBooleanSystemEnvProperty("dropIndice",false);//同时指定了默认值false
dropIndice=false
```

在代码中获取参数dropIndice方法：

```java
import org.frameworkset.spi.assemble.PropertiesUtil
```

```java
PropertiesContainer propertiesContainer = PropertiesUtil.getPropertiesContainer();
boolean dropIndice = propertiesContainer.getBooleanSystemEnvProperty("dropIndice",false);//同时指定了默认值false
```

另外可以在src\test\resources\application.properties配置控制作业执行的一些参数，例如工作线程数，等待队列数，批处理size等等：

```properties
queueSize=50
workThreads=10
batchSize=20
```

在作业执行方法中获取并使用上述参数：

```java
PropertiesContainer propertiesContainer = PropertiesUtil.getPropertiesContainer();
int batchSize = propertiesContainer.getIntSystemEnvProperty("batchSize",10);//同时指定了默认值
int queueSize = propertiesContainer.getIntSystemEnvProperty("queueSize",50);//同时指定了默认值
int workThreads = propertiesContainer.getIntSystemEnvProperty("workThreads",10);//同时指定了默认值
importBuilder.setBatchSize(batchSize);
importBuilder.setQueue(queueSize);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(workThreads);//设置批量导入线程池工作线程数量
```

### 4.11 基于xxjob 同步DB-Elasticsearch数据

bboss结合xxjob分布式定时任务调度引擎，可以非常方便地实现强大的shard分片分布式同步数据库数据到Elasticsearch功能，比如从一个10亿的数据表中同步数据，拆分为10个任务分片节点执行，每个节点同步1个亿，速度会提升10倍左右；同时提供了同步作业的故障迁移容灾能力。

参考文档：

[基于xxl-job数据同步作业调度](xxljobdatasyn.md)

### 4.12 spring boot中使用数据同步功能

可以在spring boot中使用数据同步功能，这里以db-elasticsearch定时增量数据同步为例进行说明，其他数据源方法类似。

参考文档：https://esdoc.bbossgroups.com/#/usedatatran-in-spring-boot

### 4.13 数据导入不完整原因分析及处理

如果在任务执行完毕后，发现es中的数据与数据库源表的数据不匹配，可能的原因如下：

#### 4.13.1.并行执行的过程中存在失败的任务

并行执行的过程中存在失败的任务（比如服务端超时），这种情况通过setExportResultHandler设置的exception监听方法进行定位分析

参考章节【[设置任务执行结果回调处理函数](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2812-%e8%ae%be%e7%bd%ae%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%93%e6%9e%9c%e5%9b%9e%e8%b0%83%e5%a4%84%e7%90%86%e5%87%bd%e6%95%b0)】

```java
 public void exception(TaskCommand taskCommand, Throwable exception) {
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

#### 4.13.2 存在es的bulk拒绝记录或者数据内容不合规

任务执行完毕，但是存在es的bulk拒绝记录或者数据内容不合规的情况，这种情况就通过setExportResultHandler设置的error监听方法进行定位分析

参考章节【[设置任务执行结果回调处理函数](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2812-%e8%ae%be%e7%bd%ae%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%93%e6%9e%9c%e5%9b%9e%e8%b0%83%e5%a4%84%e7%90%86%e5%87%bd%e6%95%b0)】

bulk拒绝记录解决办法：

a) 优化elasticsearch服务器配置(加节点，加内存和cpu等运算资源，调优网络性能等)

调整elasticsearch的相关线程和队列：调优elasticsearch配置参数

thread_pool.bulk.queue_size: 1000   es线程等待队列长度

thread_pool.bulk.size: 10   线程数量，与cpu的核数对应

b) 调整同步程序导入线程数、批处理batchSize参数，降低并行度。

数据内容不合规解决办法：拿到执行的原始批量数据，分析错误信息对应的数据记录，进行修改，然后重新导入失败的记录即可

```java
@Override
         public void error(TaskCommand taskCommand, Object result) {
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

#### 4.13.3 elasticsearch或者mongodb写入数据延迟性

从elasticsearch、mongodb这种存在写入数据的延迟性的数据库导出数据时，不设置截止时间戳偏移量时会存在遗漏数据的情况，解决方法参考文档：

[时间戳增量导出截止时间偏移量配置](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2854-%e6%97%b6%e9%97%b4%e6%88%b3%e5%a2%9e%e9%87%8f%e5%af%bc%e5%87%ba%e6%88%aa%e6%ad%a2%e6%97%b6%e9%97%b4%e5%81%8f%e7%a7%bb%e9%87%8f%e9%85%8d%e7%bd%ae)

### 4.14 跨库跨表数据同步

在同步数据库中数据到elasticsearch时，会存在支持跨多个数据库跨多张表同步的情况，bboss通过以下方式进行处理。

首先在application.properties文件中配置三个db数据源:db1,db2,db3

```properties
## 在数据导入过程可能需要使用的其他数据名称，需要在配置文件中定义相关名称的db配置
thirdDatasources = db1,db2,db3

db1.db.user = root
db1.db.password = 123456
db1.db.driver = com.mysql.jdbc.Driver
##db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
db1.db.url = jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false
db1.db.usePool = true
db1.db.validateSQL = select 1
##db.jdbcFetchSize = 10000
db1.db.jdbcFetchSize = -2147483648
db1.db.showsql = true
##db1.db.dbtype = mysql -2147483648
##db1.db.dbAdaptor = org.frameworkset.elasticsearch.imp.TestMysqlAdaptor

db2.db.user = root
db2.db.password = 123456
db2.db.driver = com.mysql.jdbc.Driver
##db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
db2.db.url = jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false
db2.db.usePool = true
db2.db.validateSQL = select 1
##db.jdbcFetchSize = 10000
db2.db.jdbcFetchSize = -2147483648
db2.db.showsql = true
##db2.db.dbtype = mysql -2147483648
##db2.db.dbAdaptor = org.frameworkset.elasticsearch.imp.TestMysqlAdaptor

db3.db.user = root
db3.db.password = 123456
db3.db.driver = com.mysql.jdbc.Driver
##db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
db3.db.url = jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false
db3.db.usePool = true
db3.db.validateSQL = select 1
##db.jdbcFetchSize = 10000
db3.db.jdbcFetchSize = -2147483648
db3.db.showsql = true
##db3.db.dbtype = mysql -2147483648
##db3.db.dbAdaptor = org.frameworkset.elasticsearch.imp.TestMysqlAdaptor
```

定义好三个数据源后，下面看看同步的代码

```java
//设置同步数据源db1，对应主表数据库
      importBuilder.setDbName("db1");

      //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
      // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
      // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
      // log_id和数据库对应的字段一致,就不需要设置setLastValueColumn信息，
      // 但是需要设置setLastValueType告诉工具增量字段的类型
      
      importBuilder.setSql("select * from td_cms_document ");

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
//          org.frameworkset.tran.config.ClientOptions clientOptions = new org.frameworkset.tran.config.ClientOptions();
//          clientOptions.setRouting("1");
//          context.setClientOptions(clientOptions);
            long testtimestamp = context.getLongValue("testtimestamp");//将long类型的时间戳转换为Date类型
            context.addFieldValue("testtimestamp",new Date(testtimestamp));//将long类型的时间戳转换为Date类型
            /**
             Date create_time = context.getDateValue("create_time");
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             context.addFieldValue("createTime",simpleDateFormat.format(create_time));
             context.addIgnoreFieldMapping("create_time");
             */
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
            
             //关联查询数据,单值查询，指定要查询的数据库为数据源db2
             Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,"db2",
             "select * from head where billid = ? and othercondition= ?",
             context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
             //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
             context.addFieldValue("headdata",headdata);
             //关联查询数据,多值查询，指定要查询的数据库为数据源db3
             List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,"db3",
             "select * from facedata where billid = ?",
             context.getIntegerValue("billid"));
             //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
             context.addFieldValue("facedatas",facedatas);
             
         }
      });
```

关键点说明：

1.首先需要指定主表对应的数据源

importBuilder.setDbName("db1");

2.然后在DataRefactor中跨库检索其他关联表的的数据封装到对象中

```java
 //关联查询数据,单值查询，指定要查询的数据库为数据源db2
             Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,"db2",
             "select * from head where billid = ? and othercondition= ?",
             context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
             //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
             context.addFieldValue("headdata",headdata);
             //关联查询数据,多值查询，指定要查询的数据库为数据源db3
             List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,"db3",
             "select * from facedata where billid = ?",
             context.getIntegerValue("billid"));
             //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
             context.addFieldValue("facedatas",facedatas);
```

### 4.15 自定义启动db数据源案例

如果在application.properties中配置了数据库数据源连接池visualops：

```properties
db.name = visualops
db.user = root
db.password = 123456
db.driver = com.mysql.jdbc.Driver
db.url = jdbc:mysql://100.13.11.5:3306/visualops?useUnicode=true&characterEncoding=utf-8&useSSL=false
db.validateSQL = select 1
db.initialSize = 5
db.minimumSize = 5
db.maximumSize = 5
db.showsql = true
# 控制map中的列名采用小写，默认为大写
db.columnLableUpperCase = false

```

那么我们可以通过代码来加载并启动对应的连接池

```java
PropertiesContainer propertiesContainer = new PropertiesContainer();
			propertiesContainer.addConfigPropertiesFile("application.properties");
		String dbName  = propertiesContainer.getProperty("db.name");
		String dbUser  = propertiesContainer.getProperty("db.user");
		String dbPassword  = propertiesContainer.getProperty("db.password");
		String dbDriver  = propertiesContainer.getProperty("db.driver");
		String dbUrl  = propertiesContainer.getProperty("db.url");

		String showsql  = propertiesContainer.getProperty("db.showsql");
		String validateSQL  = propertiesContainer.getProperty("db.validateSQL");
		String dbInfoEncryptClass = propertiesContainer.getProperty("db.dbInfoEncryptClass");

		DBConf tempConf = new DBConf();
		tempConf.setPoolname(dbName);
		tempConf.setDriver(dbDriver);
		tempConf.setJdbcurl(dbUrl);
		tempConf.setUsername(dbUser);
		tempConf.setPassword(dbPassword);
		tempConf.setValidationQuery(validateSQL);
		tempConf.setShowsql(showsql != null && showsql.equals("true"));
		//tempConf.setTxIsolationLevel("READ_COMMITTED");
		tempConf.setJndiName("jndi-"+dbName);
		tempConf.setDbInfoEncryptClass(dbInfoEncryptClass);
		String initialConnections  = propertiesContainer.getProperty("db.initialSize");
		int _initialConnections = 10;
		if(initialConnections != null && !initialConnections.equals("")){
			_initialConnections = Integer.parseInt(initialConnections);
		}
		String minimumSize  = propertiesContainer.getProperty("db.minimumSize");
		int _minimumSize = 10;
		if(minimumSize != null && !minimumSize.equals("")){
			_minimumSize = Integer.parseInt(minimumSize);
		}
		String maximumSize  = propertiesContainer.getProperty("db.maximumSize");
		int _maximumSize = 20;
		if(maximumSize != null && !maximumSize.equals("")){
			_maximumSize = Integer.parseInt(maximumSize);
		}
		tempConf.setInitialConnections(_initialConnections);
		tempConf.setMinimumSize(_minimumSize);
		tempConf.setMaximumSize(_maximumSize);
		tempConf.setUsepool(true);
		tempConf.setExternal(false);
		tempConf.setEncryptdbinfo(false);
		if(showsql != null && showsql.equalsIgnoreCase("true"))
			tempConf.setShowsql(true);
		else{
			tempConf.setShowsql(false);
		}
# 控制map中的列名采用小写，默认为大写
	    temConf.setColumnLableUpperCase(dbConfig.isColumnLableUpperCase());
		//启动数据源
		SQLManager.startPool(tempConf);
```

使用数据源visualops访问数据库示例代码：

```java
List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,"visualops",
             "select * from facedata where billid = ?",
             0);
```

更多的持久层使用文档访问：

https://doc.bbossgroups.com/#/persistent/tutorial

### 4.16 作业关闭事件监听器

通过作业关闭事件监听器，可以知道作业是正常结束关闭，还是异常结束导致关闭，同时可以做相应的处理工作，提供两种类型作业关闭事件监听器：同步执行和异步执行

使用案例如下：

同步执行

```java
importBuilder.setJobClosedListener(new JobClosedListener() {
    @Override
    public void jobClosed(ImportContext importContext, Throwable throwable) {
        if(throwable != null) {//作业异常关闭
            logger.info("Job Closed by exception:",throwable);
        }
        else{//作业正常关闭
            logger.info("Job Closed normal.");
        }

    }
});
```

异步执行

```java
//异步执行JobClosedListener
importBuilder.setJobClosedListener(new AsynJobClosedListener() {
    @Override
    public void jobClosed(ImportContext importContext, Throwable throwable) {
        if(throwable != null) {
            logger.info("Job Closed by exception:",throwable);
        }
        else{
            logger.info("Job Closed normal.");
        }

    }
});
```

## 5 数据同步调优

数据同步是一个非常耗资源（内存、cpu、io）的事情，所以如何充分利用系统资源，确保高效的数据同步作业长时间稳定运行，同时又不让同步服务器、Elasticsearch/数据库负荷过高，是一件很有挑战意义的事情，这里结合bboss的实践给出一些建议：

### 5.1 内存调优

内存溢出很大一个原因是jvm配置少了，这个处理非常简单，修改jvm.option文件，适当调大内存即可，设置作业运行需要的jvm内存，按照比例调整Xmx和MaxNewSize参数：

```properties
# Xms represents the initial size of total heap space
# Xmx represents the maximum size of total heap space

-Xms1g
-Xmx1g
-XX:NewSize=512m
-XX:MaxNewSize=512m
# explicitly set the stack size
-Xss1m
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

###   5.2 采用分布式作业调度引擎

需要同步的数据量很大，单机的处理能力有限，可以基于分布式作业调度引擎来实现数据分布式分片数据同步处理，参考文档：

https://esdoc.bbossgroups.com/#/db-es-tool?id=_26-%e5%9f%ba%e4%ba%8exxjob-%e5%90%8c%e6%ad%a5db-elasticsearch%e6%95%b0%e6%8d%ae

  

## 6 数据同步模式控制

### 6.1 全量/增量导入

根据实际需求，有些场景需要全量导入数据，有些场景下需要增量导入数据，具体的控制方法如下：

- 增量同步时加上下面的代码

```java
        importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
//setFromfirst(false)，如果作业停了，作业重启后从上次停止的位置开始采集数据，
//setFromfirst(true) 如果作业停了，作业重启后，重新开始位置开始采集数据
		importBuilder.setFromFirst(false);

		importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//设置增量查询的起始值lastvalue
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date);
		}
		catch (Exception e){
			e.printStackTrace();
		}
```

- 全量同步时，去掉或者注释掉上面的代码

```java
        /**
		importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
//setFromfirst(false)，如果作业停了，作业重启后从上次停止的位置开始采集数据，
//setFromfirst(true) 如果作业停了，作业重启后，重新开始位置开始采集数据
		importBuilder.setFromFirst(false);
		importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//设置增量查询的起始值lastvalue
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date);
		}
		catch (Exception e){
			e.printStackTrace();
		}*/
```



### 6.2 一次性执行和周期定时执行

根据实际需求，有些场景作业启动后只需执行一次，有些场景需要周期性定时执行，具体的控制方法如下：

- 定时执行

  支持jdk timer和quartz以及xxl-job三种定时执行机制，以jdk timer为例，加上以下代码即可

```java
        //定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
               //.setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
```

- 一次性执行
  一次性执行只需要将上面的代码setFixedRate、setDeyLay和setPeriod去掉即可

```java
        /**   
        //定时任务配置，
		importBuilder
               //.setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		*/
        
```

然后执行方法，例如：

```java
/**
         * 执行数据库表数据导入es操作
         */
        DataStream dataStream = importBuilder.builder();
        dataStream.execute();
       
```

- 一次性文件数据采集设置

文件数据采集默认会定时监听目录或者文件内容的变化，进行增量文件数据采集，如果只需要做一次性采集，在一次性配置的基础上，则通过FileInputConfig.disableScanNewFiles做以下设置即可：

通过属性disableScanNewFiles进行控制：true 一次性扫描导入目录下的文件，false 持续监听新文件（默认值false）

```java
 /**   
        //定时任务配置，
		importBuilder
               //.setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		*/
FileInputConfig config = new FileInputConfig();
config.setDisableScanNewFiles(true);
```

### 6.3 串行执行和并行执行

根据实际需求，有些场景作业采用串行模式执行，有些场景需要并行执行，具体的控制方法如下：

- 并行执行

  并行执行，加上以下代码即可

```java
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行

```

- 串行执行
  串行执行只需要将上面的代码注释即可

```java
        /**   
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		*/		
```

### 6.4 任务执行开始时间和结束时间设置

一次性导入和周期性导入，都可以设置任务导出的开始时间、延时执行时间和任务结束时间（只对jdk timer有效）

指定任务开始时间或者延迟时间

```java
    importBuilder.setScheduleDate(TimeUtil.addDateMinitues(new Date(),1)); //指定任务开始执行时间：日期，1分钟后开始
//          .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
```

同时指定任务开始时间和结束时间

```java
//定时任务配置，
      importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
                .setScheduleDate(TimeUtil.addDateMinitues(new Date(),1)) //指定任务开始执行时间：日期，1分钟后开始
//          .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
            .setScheduleEndDate(TimeUtil.addDateMinitues(new Date(),3))//3分钟后自动结束任务

            .setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
```

## 7 作业调度控制

参考文档：[作业调度控制](bboss-datasyn-control.md)

## 8.流程任务编排

采用 [bboss jobflow](https://esdoc.bbossgroups.com/#/jobworkflow) **通用分布式作业调度工作流**（提供通用轻量级、高性能流程编排模型），轻松实现数据交换、流批处理作业的流程编排以及调度执行。

使用参考文档：https://esdoc.bbossgroups.com/#/jobworkflow

## 9 开发交流

完整的数据导入demo工程

github：[db-elasticsearch-tool](https://github.com/bbossgroups/db-elasticsearch-tool)


QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />



交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">




