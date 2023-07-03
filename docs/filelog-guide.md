# 超级棒！文件&日志采集插件使用指南

本文介绍基于java语言的文件&日志数据采集插件,插件主要特色如下:

1. 支持全量和增量采集两种模式；采集-转换-清洗-[流计算一体化融合](https://esdoc.bbossgroups.com/#/etl-metrics)处理
2. 实时采集本地/FTP日志文件、excel文件数据到kafka/elasticsearch/database/自定义处理器
3. 支持多线程并行下载和处理远程数据文件
4. 支持本地/ftp/sftp子目录下文件数据采集；
5. 支持备份采集完毕日志文件功能，可以指定备份文件保存时长，定期清理过期文件；
6. 支持自动清理下载完毕后ftp服务器上的文件;
7. 支持大量文件采集场景下的流控处理机制，通过设置同时并行采集最大文件数量，控制并行采集文件数量，避免资源过渡消耗，保证数据的平稳采集。当并行文件采集数量达到阈值时，启用流控机制，当并行采集文件数量低于最大并行采集文件数量时，继续采集后续文件。

使用案例：

源码工程 https://gitee.com/bboss/filelog-elasticsearch

   1. [采集本地日志数据并写入数据库](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2DBDemo.java)
   2. [采集本地日志数据并写入Elasticsearch](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2ESDemo.java)  
   3. [采集本地日志数据并发送到Kafka](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaDemo.java)
   4. [采集ftp日志文件写入Elasticsearch-基于通用调度机制](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESETLScheduleDemo.java)
   5. [采集ftp日志文件写入Elasticsearch-基于日志采集插件自带调度机制](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESDemo.java)
   6. [采集sftp日志文件写入Elasticsearch-基于通用调度机制](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2ESETLScheduleDemo.java)
   7. [采集sftp日志文件写入Elasticsearch-基于日志采集插件自带调度机制](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2ESDemo.java)
      8. [采集日志文件自定义处理案例](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2CustomDemo.java)

![](images\datasyn.png)



filelog插件工作原理图

![](images\filelog-es.jpg)

借助bboss可以非常方便地将ftp和本地文件目录下的文件数据导入到不同的数据库表中，亦可以将数据导入到不同的elasticsearch索引中。

使用文件采集插件，需要在相关项目中，导入文件采集插件：

maven坐标
```xml
<dependency>
  <groupId>com.bbossgroups.plugins</groupId>
  <artifactId>bboss-datatran-fileftp</artifactId>
  <version>7.0.2</version>
</dependency>
```
gradle坐标
```xml
api 'com.bbossgroups.plugins:bboss-datatran-fileftp:7.0.2'
```

如果是spring boot项目还需导入其他相关坐标，参考文档：

https://esdoc.bbossgroups.com/#/quickstart



下面具体介绍kafka/elasticsearch/database三个案例。

# 1.日志采集插件属性说明

ImportBuilder用于采集作业基础属性配置

FileInputConfig用于指定日志采集插件全局配置

FileConfig用于指定文件级别配置

| 属性名称                                 | 类型                                                         | 默认值  |
| ---------------------------------------- | ------------------------------------------------------------ | ------- |
| importBuilder.flushInterval              | long 设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制 | 8秒     |
| importBuilder.fetchSize                  | int,设置按批读取文件行数                                     |         |
| importBuilder.batchSize                  | int ，设置批量入库的记录数                                   |         |
| FileInputConfig.jsondata                | 布尔类型，标识文本记录是json格式的数据，true 将值解析为json对象，false - 不解析，这样值将作为一个完整的message字段存放到上报数据中 |         |
| FileInputConfig.rootLevel               | jsondata = true时，自定义的数据是否和采集的数据平级，true则直接在原先的json串中存放数据 false则定义一个json存放数据，若不是json则是message |         |
| FileInputConfig.scanNewFileInterval | long 单位：毫秒，扫描新文件时间间隔                          | 5000L   |
| FileInputConfig.registLiveTime          | Long ,已完成文件增量记录保留时间，超过指定的时间后将会迁入历史表中，为null时不处理 | null    |
| FileInputConfig.checkFileModifyInterval | long，扫描文件内容改变时间间隔                               | 3000L   |
| FileInputConfig.charsetEncode           | String,日志内容字符集                                        | UTF-8   |
| FileInputConfig.enableMeta              | boolean，是否将日志文件信息补充到日志记录中，                | true    |
| FileInputConfig.cleanCompleteFiles | boolean,清理采集完毕文件,backupSuccessFiles为false时，配置后起作用 | false |
| FileInputConfig.fileLiveTime | long,cleanCompleteFiles为true时且fileLiveTime大于0时，对于采集完毕的文件如果超过有效期后进行清理 | 0 |
| FileConfig.enableInode                   | boolean,是否启用inode文件标识符机制来识别文件重命名操作，linux环境下起作用，windows环境下不起作用（enableInode强制为false）  linux环境下，在不存在重命名的场景下可以关闭inode文件标识符机制，windows环境下强制关闭inode文件标识符机制 | true    |
| FileConfig.sourcePath                    | String,数据文件存放目录，或者远程文件下载目录                           |         |
| FileConfig.ftpConfig | FtpConfig,可选，封装ftp/sftp配置信息，[设置ftpConfig](https://esdoc.bbossgroups.com/#/filelog-guide?id=_7ftp%e9%87%87%e9%9b%86%e9%85%8d%e7%bd%ae)，代表作业从ftp和sftp服务器下载数据文件并采集处理，否则就是本地数据文件采集处理 | |
| FileConfig.scanChild | 是否检测子目录 ,如果扫描子目录，则inode机制强制关闭,默认false，适用于本地目录/ftp目录/sftp目录 | |
| FileConfig.renameFileSourcePath          | String,重命名文件监听路径：一些日志组件会指定将滚动日志文件放在与当前日志文件不同的目录下，需要通过renameFileSourcePath指定这个不同的目录地址，以便可以追踪到未采集完毕的滚动日志文件，从而继续采集文件中没有采集完毕的日志本路径只有在inode机制有效并且启用的情况下才起作用,默认与sourcePath一致，参考案例：https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/VOPSTestdevLog2ESNew.java | null    |
| FileConfig.fileNameRegular               | String,日志文件名称正则表达式，fileNameRegular和fileFilter只能指定一个 |         |
| FileConfig.fileFilter                    | FileFilter类型，用于筛选需要采集的日志文件，fileNameRegular和fileFilter只能指定一个 |         |
| FileConfig.addField                      | 方法，为对应的日志文件记录添加字段和值，例如：FileConfig.addField("tag","elasticsearch")//添加字段tag到记录中，其他记录级别或全局添加字段，可以参考文档[5.2.4.5 数据加工处理](https://esdoc.bbossgroups.com/#/mongodb-elasticsearch?id=_5245-数据加工处理) |         |
| FileConfig.fileHeadLineRegular           | 行记录开头标识正则表达式，用来区分一条日志包含多行的情况，行记录以什么开头,正则匹配，不指定时，不区分多行记录，例如：^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\] |         |
| FileConfig.includeLines                  | String[],需包含记录的内容表达式数组，需要包含的记录条件,正则匹配 |         |
| FileConfig.excludeLines                  | String[],需排除记录的内容表达式数组，If both include_lines and exclude_lines are defined, bboss executes include_lines first and then executes exclude_lines. The order in which the two options are defined doesn’t matter.  The include_lines option will always be executed before the exclude_lines option,  even if exclude_lines appears before include_lines in the config file. |         |
| FileConfig.includeLineMatchType          | 文件记录包含条件匹配类型  REGEX_MATCH("REGEX_MATCH"),REGEX_CONTAIN("REGEX_CONTAIN"),STRING_CONTAIN("STRING_CONTAIN"), STRING_EQUALS("STRING_EQUALS"),STRING_PREFIX("STRING_PREFIX"),STRING_END("STRING_END");  默认值REGEX_CONTAIN，使用案例：config.addConfig(new FileConfig(logPath,//指定目录    fileName+".log",//指定文件名称，可以是正则表达式   startLabel)//指定多行记录的开头识别标记，正则表达式    .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化    .addField("tag",fileName.toLowerCase())//添加字段tag到记录中    .setEnableInode(true)    .setIncludeLines(levelArr, LineMatchType.STRING_CONTAIN)``` |         |
| FileConfig.excludeLineMatchType          | 文件记录排除条件匹配类型  REGEX_MATCH("REGEX_MATCH"),REGEX_CONTAIN("REGEX_CONTAIN"),STRING_CONTAIN("STRING_CONTAIN"), STRING_EQUALS("STRING_EQUALS"),STRING_PREFIX("STRING_PREFIX"),STRING_END("STRING_END");  默认值REGEX_CONTAIN，使用案例：```config.addConfig(new FileConfig(logPath,//指定目录    fileName+".log",//指定文件名称，可以是正则表达式    startLabel)//指定多行记录的开头识别标记，正则表达式    .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化    .addField("tag",fileName.toLowerCase())//添加字段tag到记录中    .setEnableInode(true)    .setExcludeLines(levelArr, LineMatchType.STRING_CONTAIN)``` |         |
| FileConfig.maxBytes                      | 字符串maxBytes为0或者负数时忽略长度截取，The maximum number of bytes that a single log message can have. All bytes after max_bytes are discarded and not sent. * This setting is especially useful for multiline log messages, which can get large. The default is 1MB (1048576) | 1048576 |
| FileConfig.startPointer                  | long ,指定采集的日志文件内容开始位置                         | 0       |
| FileConfig.ignoreOlderTime               | Long类型，If this option is enabled, bboss ignores any files that were modified before the specified timespan. Configuring ignore_older can be especially useful if you keep log files for a long time. For example, if you want to start bboss, but only want to send the newest files and files from last week, you can configure this option. You can use time strings like 2h (2 hours) and 5m (5 minutes). The default is null,  which disables the setting. Commenting out the config has the same effect as setting it to null.如果为null忽略该机制 | null    |
| FileConfig.ignoreFileAssert              | ignoreFileAssert类型，如果指定了ignoreOlderTime，但是有些文件是特例不能不忽略，那么可以通过指定IgnoreFileAssert来检查静默时间达到ignoreOlderTime的文件是否需要被关闭，参考案例：https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/VOPSTestdevLog2ESNew.java | null    |
| FileConfig.closeEOF                      | 布尔类型，true 采集到日志文件末尾后关闭本文件采集通道，后续不再采集； false不关闭，适用于文件只采集一次的场景 | false   |
| FileConfig.closeRenameEOF                | 布尔类型，重命名后的文件采集完毕后，是否要被关闭,默认true，true 采集到日志文件末尾后关闭本文件采集通道，后续不再采集； false不关闭，适用于文件存在重命名场景 | true   |
| FileConfig.deleteEOFFile                 | 布尔类型，如果deleteEOFFile为true,则删除采集完数据的文件，适用于文件只采集一次的场景 | false   |
| FileConfig.closeOlderTime                | Long类型，启用日志文件采集探针closeOlderTime配置，允许文件内容静默最大时间，单位毫秒，如果在idleMaxTime访问内一直没有数据更新，认为文件是静默文件，将不再采集静默文件数据，关闭文件对应的采集线程，作业重启后也不会采集，0或者null不起作用 | null    |
| FileConfig.closeOldedFileAssert          | CloseOldedFileAssert类型，如果指定了closeOlderTime，但是有些文件是特例不能不关闭，那么可以通过指定CloseOldedFileAssert来检查静默时间达到closeOlderTime的文件是否需要被关闭，参考案例：https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/VOPSTestdevLog2ESNew.java | null    |
| FileConfig.fieldBuilder                  | FieldBuilder类型，根据文件信息动态为不同的日志文件添加固定的字段，参考案例：https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/VOPSTestdevLog2ESNew.java | null    |
| FileConfig.skipHeaderLines | 指定忽略前几行记录 |  |
| FileInputConfig.backupSuccessFiles | 备份采集完成文件  true 备份  false 不备份 | false |
| FileInputConfig.backupSuccessFileDir | 文件备份目录 |  |
| FileInputConfig.backupSuccessFileInterval | 备份文件清理线程执行时间间隔，单位：毫秒  默认每隔10秒执行一次 | 10000ms |
| FileInputConfig.backupSuccessFileLiveTime | 备份文件保留时长，单位：秒  默认保留7天 | 7天 |
| FileInputConfig.useETLScheduleForScanNewFile | 设置是否采用外部新文件扫描调度机制：jdk timer,quartz,xxl-job ,      true 采用，false 不采用，默认false | false |
| FileInputConfig.sleepAwaitTimeAfterFetch | long,单位：毫秒  ,从文件采集（fetch）一个batch的数据后，休息一会，避免cpu占用过高，在大量文件同时采集时可以设置，大于0有效，默认值0 | 0 |
| FileInputConfig.sleepAwaitTimeAfterCollect | long,单位：毫秒  ，从文件采集完成一个任务后，休息一会，避免cpu占用过高，在大量文件同时采集时可以设置，大于0有效，默认值0 | 0 |
| FileInputConfig.disableScanNewFiles | boolean类型，一次性扫描导入文件功能（导入完毕后作业不会自行关闭，可以手动执行datastream.destory(true)进行关闭，通过属性disableScanNewFiles进行控制：true 一次性扫描导入目录下的文件，false 持续监听新文件（默认值false） | false |
| FileInputConfig.disableScanNewFilesCheckpoint | 一次性文件全量采集的处理，添加是否禁止记录文件采集状态控制开关，false 不禁止，true 禁止，启用记录状态情况情况下作业重启，已经采集过的文件不会再采集，未采集完的文件，从上次采集截止的位置开始采集。默认true，禁止增量状态标记：fileInputConfig.setDisableScanNewFilesCheckpoint(false);//启用增量状态Checkpoint机制 |  |
| FileInputConfig.maxFilesThreshold | int,设置最多允许同时采集的文件数量，> 0起作用，应用场景：ftp或者本地一次性采集大量文件时需要进行控制，默认值 -1（不控制） |  |

添加采集配置示例

```java
config.addConfig(new FileConfig()//指定多行记录的开头识别标记，正则表达式
                  .setSourcePath("D:\\logs\\sale_data").setFileFilter(new FileFilter() {
                     @Override
                     public boolean accept(FilterFileInfo filterFileInfo, //包含Ftp文件名称，文件父路径、是否为目录标识
                                           FileConfig fileConfig) {
                         String name = filterFileInfo.getFileName();
                        return name.endsWith(".txt");
                     }
                  })//指定文件过滤器.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
                 .setSkipHeaderLines(1) //指定忽略前几行记录，忽略第一行
                  .setEnableInode(true).setCloseEOF(true)
//                .setCharsetEncode("GB2312") //文件集级别配置
//          .setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
            //.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
      );
	/**
				 * 单位：毫秒
				 * 从文件采集（fetch）一个batch的数据后，休息一会，避免cpu占用过高，在大量文件同时采集时可以设置，大于0有效，默认值0
				 */
				config.setSleepAwaitTimeAfterFetch(0l);
				/**
				 * 单位：毫秒
				 * 从文件采集完成一个任务后，休息一会，避免cpu占用过高，在大量文件同时采集时可以设置，大于0有效，默认值0
				 */
				config.setSleepAwaitTimeAfterCollect(60l);
```

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

## 1.1 excel文件采集配置和案例

```java
//配置excel文件列与导出字段名称映射关系
config.addConfig(new ExcelFileConfig()
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
            .setSourcePath("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\excelfiles")//指定目录
            .setFileFilter(new FileFilter() {
               @Override
               public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
                  //判断是否采集文件数据，返回true标识采集，false 不采集
                  return fileInfo.getFileName().equals("cityperson.xlsx");
               }
            })//指定文件过滤器
              .setSkipHeaderLines(1) //忽略excel第一行
);
```

完整的案例

```java
ImportBuilder importBuilder = new ImportBuilder();
		importBuilder.setBatchSize(500)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
		importBuilder.setFlushInterval(10000l);
//":null,"jdbcFetchSize":-2147483648,"dbDriver":"com.mysql.cj.jdbc.Driver","dbUrl":"jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false","dbUser":"root","dbPassword":"123456","initSize":100,"minIdleSize":100,"maxSize":100,"showSql":true,"usePool":true,"dbtype":null,"dbAdaptor":null,"columnLableUpperCase":false,"enableDBTransaction":false,"validateSQL":"select 1","dbName":"test"},"statusDbname":null,"statusTableDML":null,"fetchSize":10,"flushInterval":0,"ignoreNullValueField":false,"Elasticsearch":"default","sourceElasticsearch":"default","clientOptions":null,"geoipConfig":null,"sortLastValue":true,"useBatchContextIndexName":false,"discardBulkResponse":true,"debugResponse":false,"scheduleConfig":{"scheduleDate":null,"deyLay":1000,"period":10000,"fixedRate":false,"externalTimer":false},"importIncreamentConfig":{"lastValueColumn":"logOpertime","lastValue":null,"lastValueType":1,"lastValueStorePath":"es2dbdemo_import","lastValueStoreTableName":null,"lastValueDateType":true,"fromFirst":true,"statusTableId":null},"externalTimer":false,"printTaskLog":true,"applicationPropertiesFile":null,"configs":null,"batchSize":2,"parallel":true,"threadCount":50,"queue":10,"asyn":false,"continueOnError":true,"asynResultPollTimeOut":1000,"useLowcase":null,"scheduleBatchSize":null,"index":null,"indexType":null,"useJavaName":null,"exportResultHandlerClass":null,"locale":null,"timeZone":null,"esIdGeneratorClass":"org.frameworkset.tran.DefaultEsIdGenerator","dataRefactorClass":"org.frameworkset.elasticsearch.imp.ES2DBScrollTimestampDemo$3","pagine":false,"scrollLiveTime":"10m","queryUrl":"dbdemo/_search","dsl2ndSqlFile":"dsl2ndSqlFile.xml","dslName":"scrollQuery","sliceQuery":false,"sliceSize":0,"Index":null,"IndexType":null}
		FileInputConfig config = new FileInputConfig();
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

		config.addConfig(new FileConfig().setSourcePath("D:\\logs")//指定目录
						.setFileHeadLineRegular("^\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
						.setFileFilter(new FileFilter() {
							@Override
							public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
								//判断是否采集文件数据，返回true标识采集，false 不采集
								return fileInfo.getFileName().equals("metrics-report.log");
							}
						})//指定文件过滤器
						.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
						.addField("tag","elasticsearch")//添加字段tag到记录中
						.setEnableInode(false)
				//				.setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
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
		importBuilder.setInputConfig(config);
		//指定elasticsearch数据源名称，在application.properties文件中配置，default为默认的es数据源名称

//导出到数据源配置
		DBOutputConfig dbOutputConfig = new DBOutputConfig();
		dbOutputConfig
				.setSqlFilepath("sql-dbtran.xml")

				.setDbName("test")//指定目标数据库，在application.properties文件中配置
//				.setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
//				.setDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
//				.setDbUser("root")
//				.setDbPassword("123456")
//				.setValidateSQL("select 1")
//				.setUsePool(true)//是否使用连接池
				.setInsertSqlName("insertSql")//指定新增的sql语句名称，在配置文件中配置：sql-dbtran.xml

				/**
				 * 是否在批处理时，将insert、update、delete记录分组排序
				 * true：分组排序，先执行insert、在执行update、最后执行delete操作
				 * false：按照原始顺序执行db操作，默认值false
				 * @param optimize
				 * @return
				 */
				.setOptimize(true);//指定查询源库的sql语句，在配置文件中配置：sql-dbtran.xml
		importBuilder.setOutputConfig(dbOutputConfig);
		//增量配置开始
		importBuilder.setFromFirst(true);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
		importBuilder.setLastValueStorePath("filelogdb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//增量配置结束

		//映射和转换配置开始
//		/**
//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
//		 */

		importBuilder.addFieldMapping("@message","message");

		importBuilder.addFieldMapping("@timestamp","optime");

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
				if(context.getValue("id") == null){
					context.addFieldValue("id", SimpleStringUtil.getUUID());
				}

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
				long pointer = (long)context.getMetaValue("pointer");
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
		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
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
		logger.info("job started.");
```

# 2.定时调度机制切换配置

内部扫描新文件调度配置

```java
FileInputConfig config = new FileInputConfig();
config.setScanNewFileInterval(1*60*1000l);//每隔半1分钟扫描ftp目录下是否有最新ftp文件信息，采集完成或已经下载过的文件不会再下载采集
```
外部扫描新文件调度配置-以jdk timer为案例（还可以支持[quartz](https://esdoc.bbossgroups.com/#/datasyn-quartz)和[xxl-job](https://esdoc.bbossgroups.com/#/xxljobdatasyn)）
```java
FileInputConfig config = new FileInputConfig();
      /**
       *  设置是否采用外部新文件扫描调度机制：jdk timer,quartz,xxl-job
       *      true 采用，false 不采用，默认false
       */
      config.setUseETLScheduleForScanNewFile(true);
      //定时任务配置，
      importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//              .setScheduleDate(date) //指定任务开始执行时间：日期
            .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
            .setPeriod(1*60*1000l); //每隔period毫秒执行，如果不设置，只执行一次
      //定时任务配置结束
```

# 3.采集日志数据并写入Elasticsearch

可以从以下地址下载“日志数据采集并写入Elasticsearch作业”开发工程环境（基于gradle）

https://github.com/bbossgroups/filelog-elasticsearch

https://gitee.com/bboss/filelog-elasticsearch

基于组件ImportBuilder、FileInputConfig、ElasticsearchOutputConfig实现日志数据采集并写入Elasticsearch作业

```java
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.config.ImportBuilder;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.input.file.FileConfig;
import org.frameworkset.tran.input.file.FileFilter;
import org.frameworkset.tran.input.file.FileTaskContext;
import org.frameworkset.tran.input.file.FilterFileInfo;
import org.frameworkset.tran.plugin.es.output.ElasticsearchOutputConfig;
import org.frameworkset.tran.plugin.file.input.FileInputConfig;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.TaskContext;
import org.frameworkset.tran.task.TaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <p>Description: 从日志文件采集日志数据并保存到elasticsearch</p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2021/2/1 14:39
 * @author biaoping.yin
 * @version 1.0
 */
public class FileLog2ESDemo {
	private static Logger logger = LoggerFactory.getLogger(FileLog2ESDemo.class);
	public static void main(String[] args){

//		Pattern pattern = Pattern.compile("(?!.*(endpoint)).*");
//		logger.info(""+pattern.matcher("xxxxsssssssss").find());
//		logger.info(""+pattern.matcher("xxxxsssendpointssssss").find());
		try {
//			ElasticSearchHelper.getRestClientUtil().getDocumentByField("xxxx-*","requestId","xxxx");
			//清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
//			String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("errorlog");
			String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("metrics-report");
			logger.info(repsonse);
		} catch (Exception e) {
		}
		ImportBuilder importBuilder = new ImportBuilder();
		importBuilder.setBatchSize(40)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
		importBuilder.setFlushInterval(10000l);
//		importBuilder.setSplitFieldName("@message");
//		importBuilder.setSplitHandler(new SplitHandler() {
//			@Override
//			public List<KeyMap<String, Object>> splitField(TaskContext taskContext,
//														   Record record, Object splitValue) {
//				Map<String,Object > data = (Map<String, Object>) record.getData();
//				List<KeyMap<String, Object>> splitDatas = new ArrayList<>();
//				//模拟将数据切割为10条记录
//				for(int i = 0 ; i < 10; i ++){
//					KeyMap<String, Object> d = new KeyMap<String, Object>();
//					d.put("message",i+"-"+(String)data.get("@message"));
////					d.setKey(SimpleStringUtil.getUUID());//如果是往kafka推送数据，可以设置推送的key
//					splitDatas.add(d);
//				}
//				return splitDatas;
//			}
//		});
		importBuilder.addFieldMapping("@message","message");
		FileInputConfig config = new FileInputConfig();
		config.setCharsetEncode("GB2312");
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

//		config.addConfig(new FileConfig("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\",//指定目录
//				"es.log",//指定文件名称，可以是正则表达式
//				"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//				.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
//				.addField("tag","elasticsearch")//添加字段tag到记录中
//				.setEnableInode(false)
////				.setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
//				//.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
//		);
//		config.addConfig(new FileConfig("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\",//指定目录
//						new FileFilter() {
//							@Override
//							public boolean accept(File dir, String name, FileConfig fileConfig) {
//								//判断是否采集文件数据，返回true标识采集，false 不采集
//								return name.equals("es.log");
//							}
//						},//指定文件过滤器
//						"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//						.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
//						.addField("tag","elasticsearch")//添加字段tag到记录中
//						.setEnableInode(false)
////				.setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
//				//.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
//		);


		config.addConfig(new FileConfig().setSourcePath("D:\\logs")//指定目录
										.setFileHeadLineRegular("^\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
										.setFileFilter(new FileFilter() {
											@Override
											public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
												//判断是否采集文件数据，返回true标识采集，false 不采集
												return fileInfo.getFileName().equals("metrics-report.log");
											}
										})//指定文件过滤器
										.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
										.addField("tag","elasticsearch")//添加字段tag到记录中
										.setEnableInode(false)
				//				.setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
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
		elasticsearchOutputConfig.setIndex("metrics-report");
		//指定索引类型，这里采用的是elasticsearch 7以上的版本进行测试，不需要指定type
		//elasticsearchOutputConfig.setIndexType("idxtype");
		importBuilder.setOutputConfig(elasticsearchOutputConfig);
		//增量配置开始
		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
		importBuilder.setLastValueStorePath("fileloges_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
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
//				if(filePath.endsWith("metrics-report.log")) {
//					context.setIndex("metrics-report");
//				}
//				else if(filePath.endsWith("es.log")){
//					 context.setIndex("eslog");
//				}


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
		importBuilder.setExportResultHandler(new ExportResultHandler<String,String>() {
			@Override
			public void success(TaskCommand<String,String> taskCommand, String o) {
				logger.info("result:"+o);
			}

			@Override
			public void error(TaskCommand<String,String> taskCommand, String o) {
				logger.warn("error:"+o);
			}

			@Override
			public void exception(TaskCommand<String,String> taskCommand, Throwable exception) {
				logger.warn("error:",exception);
			}


		});
		/**
		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
		importBuilder.setPrintTaskLog(true);

		importBuilder.addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {

			}

			@Override
			public void afterCall(TaskContext taskContext) {
				if(taskContext != null) {
					FileTaskContext fileTaskContext = (FileTaskContext)taskContext;
					logger.info("文件{}导入情况:{}",fileTaskContext.getFileInfo().getOriginFilePath(),taskContext.getJobTaskMetrics().toString());
				}
			}

			@Override
			public void throwException(TaskContext taskContext, Throwable e) {
				if(taskContext != null) {
					FileTaskContext fileTaskContext = (FileTaskContext)taskContext;
					logger.info("文件{}导入情况:{}",fileTaskContext.getFileInfo().getOriginFilePath(),taskContext.getJobTaskMetrics().toString());
				}
			}
		});
		/**
		 * 构建作业
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//启动采集作业
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
#elasticsearch.rest.hostNames=127.0.2.1:9200
#elasticsearch.rest.hostNames=10.21.20.168:9200
elasticsearch.rest.hostNames=192.168.137.1:9200
```

构建发布可运行的作业部署包：进入命令行模式，在源码工程根目录filelog-elasticsearch下运行以下gradle指令打包发布版本

```
release.bat
```

更多作业配置和运行资料参考：[帮助文档](https://gitee.com/bboss/filelog-elasticsearch/blob/main/README.md)

# 4.采集日志数据并写入数据库

可以从以下地址下载“日志数据采集并写入数据库作业”开发工程环境（基于gradle）

https://github.com/bbossgroups/filelog-elasticsearch

https://gitee.com/bboss/filelog-elasticsearch

基于组件ImportBuilder、FileInputConfig、DBOutputConfig实现日志数据采集并写入数据库作业

浏览完整的案例代码：
https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2DBDemo.java

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

## 4.1 不同文件数据写入不同的表

借助bboss可以非常方便地将ftp和本地文件目录下的文件数据导入到不同的数据库表中。在同时采集多个文件日志数据时，需要将每个文件的数据写入特定的表，通过在文件任务上下文指定不同的insertsql/updatesql/deletesql即可.

```java

		importBuilder.addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				FileTaskContext fileTaskContext = (FileTaskContext)taskContext;
				String filePath = fileTaskContext.getFileInfo().getOriginFilePath();
				/**
				 * 根据文件名称指定不同的insert sql语句
				 */
				if(filePath.endsWith("metrics-report.log")) {
					DBConfigBuilder dbConfigBuilder = new DBConfigBuilder();
					dbConfigBuilder.setInsertSqlName("insertSql");//指定新增的sql语句名称，在配置文件中配置：sql-dbtran.xml

					taskContext.setDbmportConfig(dbConfigBuilder.buildDBImportConfig());
				}
                else{
                    DBConfigBuilder dbConfigBuilder = new DBConfigBuilder();
					dbConfigBuilder.setInsertSqlName("insertOtherSql");//指定新增的sql语句名称，在配置文件中配置：sql-dbtran.xml

					taskContext.setDbmportConfig(dbConfigBuilder.buildDBImportConfig());
                }
			}

			@Override
			public void afterCall(TaskContext taskContext) {

			}

			@Override
			public void throwException(TaskContext taskContext, Exception e) {

			}
		});
```





# 5.采集日志数据并发送到kafka

可以从以下地址下载“日志数据采集并发送到kafka作业”开发工程环境（基于gradle）

https://github.com/bbossgroups/kafka2x-elasticsearch

https://gitee.com/bboss/kafka2x-elasticsearch

基于组件ImportBuilder、FileInputConfig、**Kafka2OutputConfig** 实现日志数据采集并发送到kafka作业

浏览完整的案例源码

https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaDemo.java

在工程中调试好作业后，修改application.properties文件中的mainclass配置，将作业类调整为新开发的作业程序Filelog2KafkaDemo

```properties
mainclass=org.frameworkset.elasticsearch.imp.Filelog2KafkaDemo
```

构建发布可运行的作业部署包：进入命令行模式，在源码工程根目录filelog-elasticsearch下运行以下gradle指令打包发布版本

```
release.bat
```

更多作业配置和运行资料参考：[帮助文档](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/README.md)



# 6.inode机制说明

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

# 7.Ftp采集配置

ftp采集的一些特性：

1. 如果文件没有下载完，不会被采集，如果在下载的过程中作业停了，下次启动作业后未下载完成的文件会接着下载
2. 文件只有下载完成后，才会被采集，否则不被采集，不会再次下载采集已经采集过的文件，如果在文件采集过程中，作业停了，作业下次启动后会继续采集未采集完成的日志文件
3. 已经下载过的文件不会再次下载采集（除非删除了作业增量状态库文件）
4. 支持多线程并行下载和处理远程数据文件
5. 支持ftp/sftp子目录下数据文件采集
6. 支持数据文件校验机制，可以根据需要实现对数据文件进行有效性和数据完整性校验，比如md5签名校验

第一节介绍了采集日志文件的通用配置，通用配置同样适用于从ftp下载的日志文件数据采集，特殊之处：

ftp下载的日志文件强制关闭inode机制，强制开启closeEOF机制，下面介绍FTP特有的属性

| 属性                                  | 说明                                                         |
| ------------------------------------- | ------------------------------------------------------------ |
| FtpConfig.ftpIp                       | ftpIP地址                                                    |
| FtpConfig.ftpPort                     | ftp端口号                                                    |
| FtpConfig.ftpUser                     | ftp用户                                                      |
| FtpConfig.ftpPassword                 | ftp口令                                                      |
| FtpConfig.remoteFileDir               | ftp目录                                                      |
| FtpConfig.ftpFileFilter               | ftp文件筛选器                                                |
| FtpConfig.addScanNewFileTimeRange     | filelog插件支持内置定时监听ftp目录新增文件功能，可以为内置定时器 添加扫码新文件的时间段，每天扫描新文件时间段，优先级高于不扫码时间段，先计算是否在扫描时间段，如果是则扫描，不是则不扫码 * timeRange必须是以下三种类型格式 * 11:30-12:30  每天在11:30和12:30之间运行 * 11:30-    每天11:30开始执行,到23:59结束 * -12:30    每天从00:00开始到12:30 |
| FtpConfig.addSkipScanNewFileTimeRange | filelog插件支持内置定时监听ftp目录新增文件功能，可以为内置定时器添加不扫码新文件的时间段， timeRange必须是以下三种类型格式：                                                                    * 11:30-12:30  每天在11:30和12:30之间运行                              * 11:30-    每天11:30开始执行,到23:59结束                                 * -12:30    每天从00:00开始到12:30 |
| FtpConfig.sourcePath                  | 指定下钻到本地日志文件目录                                   |
| FtpConfig.transferProtocol            | bboss 支持ftp和sftp两种协议类型：FtpConfig.TRANSFER_PROTOCOL_FTP  FtpConfig.TRANSFER_PROTOCOL_SFTP                                默认值：FtpConfig.TRANSFER_PROTOCOL_SFTP |
| FtpConfig.deleteRemoteFile            | 控制是否删除下载完毕的ftp文件，true 删除，false 不删除，默认值false |
| FtpConfig.downloadWorkThreads         | 设置并行下载线程数，默认为3个，如果设置为0代表串行下载       |
| FtpConfig.remoteFileValidate          | [远程数据文件校验机制](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo?id=_11-从sftp服务器采集excel文件写入redis案例)，以实现对数据文件md5签名校验、记录数校验等功能 |
| keepAliveTimeout          | 可选，long类型，单位毫秒，ftp/sftp通讯协议连接存活超时时间   |
| socketTimeout             | 可选，long类型，单位毫秒，ftp/sftp数据读取超时时间，避免socketTimeout异常 |
| connectTimeout            | 可选，long类型，单位毫秒，sftp数据连接建立超时时间           |

ftp配置案例

```java
ImportBuilder importBuilder = new ImportBuilder();
		importBuilder.setBatchSize(5000)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
FileInputConfig config = new FileInputConfig();
		config.setScanNewFileInterval(1*60*1000l);//每隔半1分钟扫描ftp目录下是否有最新ftp文件信息，采集完成或已经下载过的文件不会再下载采集
		/**
		 * 备份采集完成文件
		 * true 备份
		 * false 不备份
		 */
		config.setBackupSuccessFiles(true);
		/**
		 * 备份文件目录
		 */
		config.setBackupSuccessFileDir("d:/ftpbackup");
		/**
		 * 备份文件清理线程执行时间间隔，单位：毫秒
		 * 默认每隔10秒执行一次
		 */
		config.setBackupSuccessFileInterval(20000l);
		/**
		 * 备份文件保留时长，单位：秒
		 * 默认保留7天
		 */
		config.setBackupSuccessFileLiveTime( 10 * 60l);
//		config.setCharsetEncode("GB2312");
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

//		config.addConfig(new FileConfig("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\",//指定目录
//				"es.log",//指定文件名称，可以是正则表达式
//				"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//				.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
//				.addField("tag","elasticsearch")//添加字段tag到记录中
//				.setEnableInode(false)
////				.setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
//				//.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
//		);
//		config.addConfig(new FileConfig("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\",//指定目录
//						new FileFilter() {
//							@Override
//							public boolean accept(File dir, String name, FileConfig fileConfig) {
//								//判断是否采集文件数据，返回true标识采集，false 不采集
//								return name.equals("es.log");
//							}
//						},//指定文件过滤器
//						"^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//						.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
//						.addField("tag","elasticsearch")//添加字段tag到记录中
//						.setEnableInode(false)
////				.setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
//				//.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
//		);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date _startDate = null;
		try {
			_startDate = format.parse("20201211");//下载和采集2020年12月11日以后的数据文件
		} catch (ParseException e) {
			logger.error("",e);
		}
		final Date startDate = _startDate;
		FtpConfig ftpConfig = new FtpConfig().setFtpIP("127.0.2.1").setFtpPort(5322)
				.setFtpUser("1111").setFtpPassword("111@123")
				.setRemoteFileDir("/home/ecs/failLog")
				.setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_SFTP) ;//采用sftp协议
		config.addConfig(new FileConfig().setFtpConfig(ftpConfig)
										.setFileFilter(new FileFilter() {//指定ftp文件筛选规则
											@Override
											public boolean accept(FilterFileInfo fileInfo, //Ftp文件名称
																  FileConfig fileConfig) {
												String name = fileInfo.getFileName();
												//判断是否采集文件数据，返回true标识采集，false 不采集
												boolean nameMatch = name.startsWith("731_tmrt_user_login_day_");
												if(nameMatch){
													String day = name.substring("731_tmrt_user_login_day_".length());
													SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
													try {
														Date fileDate = format.parse(day);
														if(fileDate.after(startDate))//下载和采集2020年12月11日以后的数据文件
															return true;
													} catch (ParseException e) {
														logger.error("",e);
													}


												}
												return false;
											}
										})
										.addScanNewFileTimeRange("12:37-20:30")
//										.addSkipScanNewFileTimeRange("11:30-13:00")
										.setSourcePath("D:/ftplogs")//指定目录
										.addField("tag","elasticsearch")//添加字段tag到记录中
						);

		config.setEnableMeta(true);
//		config.setJsondata(true);
		importBuilder.setInputConfig(config);
```

# 8.采集子目录案例

通过设置setScanChild(true)来控制采集子目录下的日志文件，采集子目录时，还需要在Filter中这对目录进行额外处理：

```java
if(filterFileInfo.isDirectory())//由于要采集子目录下的文件，所以如果是目录则直接返回true，当然也可以根据目录名称决定哪些子目录要采集
                                 return true;
```

以sftp为案例进行说明，本地目录和ftp设置方式类似：

```java
		FtpConfig ftpConfig = new  FtpConfig().setFtpIP("127.0.2.1").setFtpPort(222)
				.setFtpUser("test").setFtpPassword("123456")
				.setRemoteFileDir("/").setDeleteRemoteFile(false)//
				//.setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_FTP); //采用ftp协议
				.setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_SFTP) ;//采用sftp协议
		config.addConfig(new FileConfig().setFtpConfig(ftpConfig)
                        .setFileFilter(new FileFilter() {//指定ftp文件筛选规则
                           @Override
                           public boolean accept(FilterFileInfo filterFileInfo, //包含Ftp文件名称，文件父路径、是否为目录标识
                                            FileConfig fileConfig) {
                              if(filterFileInfo.isDirectory())//由于要采集子目录下的文件，所以如果是目录则直接返回true，当然也可以根据目录名称决定哪些子目录要采集
                                 return true;
                              String name = filterFileInfo.getFileName();
                              //判断是否采集文件数据，返回true标识采集，false 不采集
                              boolean nameMatch = name.startsWith("731_tmrt_user_login_day_");
                              if(nameMatch){
                                 String day = name.substring("731_tmrt_user_login_day_".length());
                                 SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                                 try {
                                    Date fileDate = format.parse(day);
                                    if(fileDate.after(startDate))//下载和采集2020年12月11日以后的数据文件
                                       return true;
                                 } catch (ParseException e) {
                                    logger.error("",e);
                                 }


                              }
                              return false;
                           }
                        })
                        .setScanChild(true)//采集子目录下的日志文件
                        .setSourcePath("D:/sftplogs")//指定目录
                        .addField("tag","elasticsearch")//添加字段tag到记录中
            );
```

具体的子目录采集案例 ：

本地文件

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileSubDirLog2ESDemo.java

ftp

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpSubdirLog2ESDemo.java

sftp

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpSubdirLog2ESETLScheduleDemo.java

# 9.文件采集任务状态跟踪

如果文件closeEof为true，那么一个文件采集完毕后，会调用CallInterceptor拦截器afterCall方法，跟踪任务执行状态,例如：

```java
importBuilder.addCallInterceptor(new CallInterceptor() {
   @Override
   public void preCall(TaskContext taskContext) {

   }

   @Override
   public void afterCall(TaskContext taskContext) {
      if(taskContext != null) {
         FileTaskContext fileTaskContext = (FileTaskContext)taskContext;
         logger.info("afterCall ---- 文件{}导入情况:{}",fileTaskContext.getFileInfo().getOriginFilePath(),taskContext.getJobTaskMetrics().toString());
      }
   }

   @Override
   public void throwException(TaskContext taskContext, Exception e) {
      if(taskContext != null) {
         taskContext.await();//等待数据异步处理完成
         FileTaskContext fileTaskContext = (FileTaskContext)taskContext;
         logger.info("文件{}导入情况:{}",fileTaskContext.getFileInfo().getOriginFilePath(),taskContext.getJobTaskMetrics().toString());
      }
   }
});
```

更多任务执行监控介绍，可以参考文档：

[数据同步任务执行统计信息获取](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2317-%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%9f%e8%ae%a1%e4%bf%a1%e6%81%af%e8%8e%b7%e5%8f%96)

# 10.一次性采集控制策略

文件采集插件支持实时增量持续采集文件数据，也可以一次性采集文件数据，采集完毕即刻关闭文件采集通道，本节介绍一次性采集文件的几种控制策略,其他情况归纳为持续实时增量持续采集。

## 策略一 disableScanNewFiles

disableScanNewFiles--一次性采集目录下的所有文件，不监听新文件，文件采集完毕后，是否关闭对应文件采集通道参考策略二和策略三，

作业配置举例如下：

```java
一次性采集完目录下的excel文件：
ExcelFileInputConfig config = new ExcelFileInputConfig();
      config.setDisableScanNewFiles(true);
```

一次性文件全量采集的处理，可以通过控制开关disableScanNewFilesCheckpoint禁止和启用文件采集状态记录功能，false 启用，true 禁止（默认值）；启用记录状态情况下，作业重启，已经采集过的文件不会再采集，未采集完的文件，从上次采集截止的位置开始采集。

```java
fileInputConfig.setDisableScanNewFilesCheckpoint(false);//启用增量状态Checkpoint机制
```

完整案例：[ExcelFile2DBDemo](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ExcelFile2DBDemo1.java)

## 策略二 closeOlderTime/ignoreOlderTime

文件采集达到末尾后，文件内容超过closeOlderTime/ignoreOlderTime指定的时间没有变化，则关闭文件采集通道

```java
FileConfig fileConfig = new FileConfig();
fileConfig.setCloseOlderTime(10000L);//setIgnoreOlderTime

```

完整案例：[SFtpLog2ESClearComplete2ndCloseOlderTimeDemo](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2ESClearComplete2ndCloseOlderTimeDemo.java)

## 策略三 closeEOF

采集文件达到结尾后，立马结束关闭文件采集通道

```java
FileConfig fileConfig = new FileConfig();
fileConfig.setCloseEOF(true);//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
```

完整案例：[FileSubDirLog2ESDemo](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileSubDirLog2ESDemo.java)

# 11.文件备份和清理

提供了采集完毕文件备份清理策略和有效期后清理策略

备份清理策略优先级高于直接清理策略

## 策略一 备份清理策略

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2ESClearComplete2ndBackupDemo.java

```java
FileInputConfig config = new FileInputConfig();
     
        config.setMaxFilesThreshold(2);//允许同时采集2个文件
/**
 * 备份采集完成文件
 * true 备份
 * false 不备份
 */
        config.setBackupSuccessFiles(true);
        /**
         * 备份文件目录
         */
        config.setBackupSuccessFileDir("d:/ftpbackup");
        /**
         * 备份文件清理线程执行时间间隔，单位：毫秒
         * 默认每隔10秒执行一次
         */
        config.setBackupSuccessFileInterval(20000l);
        /**
         * 备份文件保留时长，单位：秒
         * 默认保留7天
         */
        config.setBackupSuccessFileLiveTime( 10 * 60l);
```

## 策略二 有效期后清理策略

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2ESClearComplete2ndCloseOlderTimeDemo.java

```java
FileInputConfig config = new FileInputConfig();
config.setCleanCompleteFiles(true);//清理采集完成的文件
config.setFileLiveTime(1000000L);//文件存活有效期，过期文件才会被清理，单位：微秒
```

# 12.并行采集文件数量控制-流控

大量文件采集场景下需要用到流控处理机制，通过设置同时并行采集最大文件数量，控制并行采集文件数量，避免资源过渡消耗，保证数据的平稳采集。当并行文件采集数量达到阈值时，启用流控机制，当并行采集文件数量低于最大并行采集文件数量时，继续采集后续文件

```java
FileInputConfig config = new FileInputConfig();
config.setScanNewFileInterval(1*60*1000l);//每隔半1分钟扫描ftp目录下是否有最新ftp文件信息，采集完成或已经下载过的文件不会再下载采集
      config.setMaxFilesThreshold(2);//允许同时采集2个文件
```

# 13.流计算融合处理

文件&日志数据采集插件除了支持传统的采集-转换-清洗-入库处理，还提供了强大的采集-转换-清洗-[流计算一体化融合](https://esdoc.bbossgroups.com/#/etl-metrics)处理能力，详见案例介绍：

[采集文件数据并进行统计处理](https://esdoc.bbossgroups.com/#/etl-metrics?id=_47-采集文件数据并进行统计处理)

# 14.基于Filelog插件采集大量日志文件导致jvm heap溢出踩坑记

基于Filelog插件采集大量日志文件导致jvm heap溢出踩坑记

https://esdoc.bbossgroups.com/#/filelog-oom