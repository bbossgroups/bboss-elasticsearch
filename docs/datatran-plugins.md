# bboss-datatran插件清单

bboss-datatran由 [bboss ](https://www.bbossgroups.com)开源的数据采集同步ETL工具，提供数据采集、数据清洗转换处理和数据入库以及[数据指标统计计算流批一体化](https://esdoc.bbossgroups.com/#/etl-metrics)处理功能。

bboss-datatran采用标准的输入输出异步管道来处理数据，输入插件和输出插件可以自由组合，输入插件从数据源采集数据，经过数据异步并行流批一体化处理后，输出插件将处理后的数据、指标数据输出到目标地。

![](images\datasyn-inout-now.png)
通过maven坐标直接将插件引入作业工程，参考文档：[插件maven坐标](https://esdoc.bbossgroups.com/#/db-es-tool?id=_11-%e5%9c%a8%e5%b7%a5%e7%a8%8b%e4%b8%ad%e5%af%bc%e5%85%a5bboss-maven%e5%9d%90%e6%a0%87)

本文介绍bboss-datatran提供各种输入输出插件以及配置说明。

# 1.输入插件

## 1.1 Elasticsearch输入插件

Elasticsearch输入插件配置类：[ElasticsearchInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/es/input/ElasticsearchInputConfig.java)

配置elasticsearch数据源、queryDsl、queryDsl配置文件路径等

下面介绍从Elasticsearch输入插件配置参数和配置实例

### 1.1.1 插件属性

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

### 1.1.2 使用配置文件中的Elasticsearch数据源

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
				//通过简单的示例，演示根据实间范围计算queryUrl,以当前时间为截止时间，后续版本6.2.8将增加lastEndtime参数作为截止时间（在设置了IncreamentEndOffset情况下有值）
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

### 1.1.3 直接配置Elasticsearch数据源

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

### 1.1.4 配置Elasticsearch检索dsl语句

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

## 1.2 Database输入插件

Database输入插件配置类：[DBInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/db/input/DBInputConfig.java)

配置DB数据源、查询sql、查询sql文件路径及文件名称,支持各种关系数据库，hive，clickhouse数据库配置

下面介绍从Database输入插件配置参数和配置实例

### 1.2.1 基于sql配置文件的配置案例

数据库输入插件配置案例：

```java
DBInputConfig dbInputConfig = new DBInputConfig();
dbInputConfig.setDbName("source")
      .setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
      .setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
      .setDbUser("root")
      .setDbPassword("123456")
      .setValidateSQL("select 1")
      .setUsePool(true)//是否使用连接池
      .setSqlFilepath("sql.xml")
      .setSqlName("demoexport");
importBuilder.setInputConfig(dbInputConfig);
```

可以配置数据库连接、账号、口令、数据库连接池参数配置（详见DBInputConfig类的set方法）、查询sql语句配置/sql配置文件配置（可以是增量sql也可以是全量sql）。

sql.xml文件和对应的sql配置demoexport，直接访问以下地址查看：

[sql.xml](https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/resources/sql.xml)

### 1.2.2 直接设置增量sql案例

```java
DBInputConfig dbInputConfig = new DBInputConfig();
      //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
      // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
      // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
      // 需要设置setLastValueColumn信息log_id，
      // 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型

//    importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
      dbInputConfig.setSql("select * from td_sm_log where log_id > #[log_id]")
            .setDbName("test")
            .setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
            .setDbUrl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
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

sql配置了增量字段log_id字段条件：

select * from td_sm_log where log_id > #[log_id]

通过importbuilder设置增量其他参数，详细设置参考文档：[定时增量导入](https://esdoc.bbossgroups.com/#/db-es-tool?id=_285-%e5%ae%9a%e6%97%b6%e5%a2%9e%e9%87%8f%e5%af%bc%e5%85%a5)

```java
importBuilder.setLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
      importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
//    setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
      importBuilder.setStatusDbname("testStatus");//指定增量状态数据源名称
//    importBuilder.setLastValueStorePath("logtable_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
      importBuilder.setLastValueStoreTableName("logstable1");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
      importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
```



## 1.3 Mysql binlog输入插件

Mysql binlog输入插件配置类：[MySQLBinlogConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-binlog/src/main/java/org/frameworkset/tran/plugin/mysqlbinlog/input/MySQLBinlogConfig.java)

Mysql binlog插件通过配置对应的mysql master ip和端口、数据库账号和口令、监听的数据库表以及binlog文件路径等信息，实时采集mysql增删改数据，支持以下三种数据采集模式：

**模式1** 直接读取binlog文件,采集文件中的增删改数据

**模式2** 监听mysql master slave ip和端口，作业重启从binlog最新位置采集数据

**模式3** 监听mysql master slave ip和端口，启用故障容灾配置，每次重启作业从上次采集结束的位置开始采集数据

模式1适用一次性离线数据采集场景，模式2和模式3适用于实时采集场景。

<img src="images\mysql-binlog-arch.png" style="zoom:50%;" />

下面介绍Mysql binlog输入插件配置参数和配置实例

### 1.3.1 插件配置案例

```java
MySQLBinlogConfig mySQLBinlogConfig = new MySQLBinlogConfig();
mySQLBinlogConfig.setHost("192.168.137.1");
mySQLBinlogConfig.setPort(3306);
mySQLBinlogConfig.setDbUser("root");
mySQLBinlogConfig.setDbPassword("123456");
//直接监听binlog文件，多个用逗号分隔
mySQLBinlogConfig.setFileNames("F:\\6_environment\\mysql\\binlog.000107,F:\\6_environment\\mysql\\binlog.000127");
//监听的表清单，多个逗号分割
mySQLBinlogConfig.setTables("cityperson,batchtest");
//监听的数据库
mySQLBinlogConfig.setDatabase("bboss");
importBuilder.setInputConfig(mySQLBinlogConfig);
```

### 1.3.2 自定义输出插件结合案例

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/binlog/Binlog2CustomOutput.java

```java
ImportBuilder importBuilder = new ImportBuilder();
        importBuilder.setBatchSize(500);//设置批量入库的记录数

        MySQLBinlogConfig mySQLBinlogConfig = new MySQLBinlogConfig();
        mySQLBinlogConfig.setHost("192.168.137.1");
        mySQLBinlogConfig.setPort(3306);
        mySQLBinlogConfig.setDbUser("root");
        mySQLBinlogConfig.setDbPassword("123456");
        mySQLBinlogConfig.setFileNames("F:\\6_environment\\mysql\\binlog.000107,F:\\6_environment\\mysql\\binlog.000127");
        mySQLBinlogConfig.setTables("cityperson,batchtest");//
        mySQLBinlogConfig.setDatabase("bboss");
        importBuilder.setInputConfig(mySQLBinlogConfig);
        importBuilder.setPrintTaskLog(true);

        //自己处理数据
        CustomOutputConfig customOutputConfig = new CustomOutputConfig();
        customOutputConfig.setCustomOutPut(new CustomOutPut() {
            @Override
            public void handleData(TaskContext taskContext, List<CommonRecord> datas) {

                //You can do any thing here for datas
                for(CommonRecord record:datas){
                    Map<String,Object> data = record.getDatas();//获取记录数据，column/value结构
                    int action = (int)record.getMetaValue("action");
                    logger.info("action:{},record action type:insert={},update={},delete={}",action,record.isInsert(),record.isUpdate(),record.isDelete());

                    logger.info(SimpleStringUtil.object2json(record.getMetaDatas()));
                    if(record.isDelete()){
                        logger.info("record.isDelete");
                    }

                    if(record.isUpdate()){
                        logger.info("record.isUpate");
                        Map<String,Object> oldDatas = record.getUpdateFromDatas();
                    }
//                    logger.info(SimpleStringUtil.object2json(data));
                }
            }
        });
        importBuilder.setDataRefactor(new DataRefactor() {
            @Override
            public void refactor(Context context) throws Exception {
                int action = (int)context.getMetaValue("action");
//                int action1 = (int)context.getMetaValue("action1");
            }
        });
        importBuilder.setOutputConfig(customOutputConfig);
        DataStream dataStream = importBuilder.builder();
        dataStream.execute();
```

record.getMetaDatas()包含了记录对应的表及相关的元数据信息：

position,table,database,host,fileName,fileNames,port,action

可以通过以下方法获取每个元数据：

int action = (int)record.getMetaValue("action");

以下方法分别返回当前记录对应的数据库操作：

record.isInsert() 插入操作

record.isUpdate() 修改操作

record.isDelete() 删除操作

以下方法返回数据库操作对应的字段以及值

record.getDatas()  key/value  key为字段名称，value为字段值

record.getUpdateFromDatas() 返回修改之前的字段值和字段名称 key/value ，key为字段名称，value为字段值

**mysql binlog数据采集作业开发调测发布部署视频教程：**介绍采集作业开发、调测、构建配置部署实际操作过程

https://www.bilibili.com/video/BV1ko4y1M7My/

### 1.3.3 输出到数据库案例

详见章节：[输出到数据库案例](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_212-mysql-binlog%e7%9b%91%e5%90%ac%e5%a4%9a%e8%a1%a8%e5%9c%ba%e6%99%af)

## 1.4 文件采集插件

文件采集插件：[FileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/FileInputConfig.java)

插件主要特色如下:

1. 支持全量和增量采集两种模式；采集-转换-清洗-[流计算一体化融合](https://esdoc.bbossgroups.com/#/etl-metrics)处理
2. 实时采集本地/FTP日志文件、excel文件数据到kafka/elasticsearch/database/自定义处理器
3. 支持多线程并行下载和处理远程数据文件
4. 支持本地/ftp/sftp子目录下文件数据采集；
5. 支持备份采集完毕日志文件功能，可以指定备份文件保存时长，定期清理过期文件；
6. 支持自动清理下载完毕后ftp服务器上的文件;
7. 支持大量文件采集场景下的流控处理机制，通过设置同时并行采集最大文件数量，控制并行采集文件数量，避免资源过渡消耗，保证数据的平稳采集。当并行文件采集数量达到阈值时，启用流控机制，当并行采集文件数量低于最大并行采集文件数量时，继续采集后续文件。

### 1.4.1 配置案例

```java
ImportBuilder importBuilder = new ImportBuilder();
      importBuilder.setBatchSize(500)//设置批量入库的记录数
            .setFetchSize(1000);//设置按批读取文件行数
      //设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
      importBuilder.setFlushInterval(10000l);
      importBuilder.setSplitFieldName("@message");
      importBuilder.setSplitHandler(new SplitHandler() {
         /**
          * 将记录字段值splitValue切割为多条记录，如果方法返回null，则继续将原记录写入目标库
          * @param taskContext
          * @param record
          * @param splitValue
          * @return List<KeyMap> KeyMap是LinkedHashMap的子类，添加key字段，如果是往kafka推送数据，可以设置推送的key
          */
         @Override
         public List<KeyMap> splitField(TaskContext taskContext,//调度任务上下文
                                             Record record,//原始记录对象
                                             Object splitValue) {//待切割的字段值
//          Map<String,Object > data = (Map<String, Object>) record.getData();//获取原始记录中包含的数据对象
            List<KeyMap> splitDatas = new ArrayList<>();
            //模拟将数据切割为10条记录
            for(int i = 0 ; i < 10; i ++){
               KeyMap d = new KeyMap();//创建新记录对象
               d.put("id", SimpleStringUtil.getUUID());//用新的id值覆盖原来的唯一标识id字段的值
               d.put("message",i+"-"+splitValue);//我们只切割splitValue到message字段，继承原始记录中的其他字段
//             d.setKey(SimpleStringUtil.getUUID());//如果是往kafka推送数据，可以设置推送的key
               splitDatas.add(d);
            }
            return splitDatas;
         }
      });
      importBuilder.addFieldMapping("@message","message");
      importBuilder.addFieldMapping("@timestamp","optime");
      FileInputConfig config = new FileInputConfig();
      //.*.txt.[0-9]+$
      //[17:21:32:388]
//    config.addConfig(new FileConfig("D:\\ecslog",//指定目录
//          "error-2021-03-27-1.log",//指定文件名称，可以是正则表达式
//          "^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
//          .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
////            .setMaxBytes(1048576)//控制每条日志的最大长度，超过长度将被截取掉
//          //.setStartPointer(1000l)//设置采集的起始位置，日志内容偏移量
//          .addField("tag","error") //添加字段tag到记录中
//          .setExcludeLines(new String[]{"\\[DEBUG\\]"}));//不采集debug日志

      config.addConfig(new FileConfig()
                  .setSourcePath("D:\\logs")//指定目录
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
            //          .setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
            //.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
      );
//启用元数据，将元数据信息附带到记录中
      config.setEnableMeta(true);
      importBuilder.setInputConfig(config);
```

更多介绍，访问文档：https://esdoc.bbossgroups.com/#/filelog-guide

插件元数据说明：

```java
@filemeta  文件详细信息，map结构，包含以下信息
hostIp
hostName
filePath
pointer
fileId
ftpDir
ftpIp
ftpPort
ftpUser
ftpProtocol

@timestamp  记录采集时间
```

### 1.4.2 使用案例

源码工程 https://gitee.com/bboss/filelog-elasticsearch

1. [采集本地日志数据并写入数据库](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2DBDemo.java)
2. [采集本地日志数据并写入Elasticsearch](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2ESDemo.java)
3. [采集本地日志数据并发送到Kafka](https://gitee.com/bboss/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaDemo.java)
4. [采集ftp日志文件写入Elasticsearch-基于通用调度机制](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESETLScheduleDemo.java)
5. [采集ftp日志文件写入Elasticsearch-基于日志采集插件自带调度机制](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FtpLog2ESDemo.java)
6. [采集sftp日志文件写入Elasticsearch-基于通用调度机制](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2ESETLScheduleDemo.java)
7. 采集sftp日志文件写入Elasticsearch-基于日志采集插件自带调度机制
   1. [采集日志文件自定义处理案例](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/FileLog2CustomDemo.java)

## 1.5 Excel文件采集插件

插件配置 [ExcelFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/ExcelFileInputConfig.java)和ExcelFileConfig结合

通过ExcelFileConfig设置excel列与字段的映射关系，excel忽略行配置等

### 1.5.1 配置案例

```java
ExcelFileInputConfig config = new ExcelFileInputConfig();
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
				.addCellMapping(9,"zhs_level","1");
		excelFileConfig.setSourcePath("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\excelfiles")//指定目录
				.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
						//判断是否采集文件数据，返回true标识采集，false 不采集
						return fileInfo.getFileName().equals("cityperson.xlsx");
					}
				})//指定文件过滤器
				.setSkipHeaderLines(1);//忽略第一行
		//shebao_org,person_no, name, cert_type,cert_no,zhs_item  ,zhs_class ,zhs_sub_class,zhs_year  , zhs_level
		//配置excel文件列与导出字段名称映射关系
		config.addConfig(excelFileConfig		);

		//将文件元数据信息附带到记录
		config.setEnableMeta(true);
		importBuilder.setInputConfig(config);
```

元数据信息和文件插件一致

## 1.6 HBase采集插件

插件配置类：[HBaseInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-hbase/src/main/java/org/frameworkset/tran/plugin/hbase/input/HBaseInputConfig.java)

### 1.6.1 配置案例

基本配置

```java
ImportBuilder importBuilder = new ImportBuilder();
      importBuilder.setBatchSize(1000) //设置批量写入目标Elasticsearch记录数
            .setFetchSize(10000); //设置批量从源Hbase中拉取的记录数,HBase-0.98 默认值为为 100，HBase-1.2 默认值为 2147483647，即 Integer.MAX_VALUE。Scan.next() 的一次 RPC 请求 fetch 的记录条数。配置建议：这个参数与下面的setMaxResultSize配合使用，在网络状况良好的情况下，自定义设置不宜太小， 可以直接采用默认值，不配置。

//    importBuilder.setHbaseBatch(100) //配置获取的列数，假如表有两个列簇 cf，info，每个列簇5个列。这样每行可能有10列了，setBatch() 可以控制每次获取的最大列数，进一步从列级别控制流量。配置建议：当列数很多，数据量大时考虑配置此参数，例如100列每次只获取50列。一般情况可以默认值（-1 不受限），如果设置了scan filter也不需要设置
//          .setMaxResultSize(10000l);//客户端缓存的最大字节数，HBase-0.98 无该项配置，HBase-1.2 默认值为 210241024，即 2M。Scan.next() 的一次 RPC 请求 fetch 的数据量大小，目前 HBase-1.2 在 Caching 为默认值(Integer Max)的时候，实际使用这个参数控制 RPC 次数和流量。配置建议：如果网络状况较好（万兆网卡），scan 的数据量非常大，可以将这个值配置高一点。如果配置过高：则可能 loadCache 速度比较慢，导致 scan timeout 异常
      // 参考文档：https://blog.csdn.net/kangkangwanwan/article/details/89332536


      /**
       * hbase参数配置
       */
      HBaseInputConfig hBaseInputConfig = new HBaseInputConfig();
//    hBaseInputConfig.addHbaseClientProperty("hbase.zookeeper.quorum","192.168.137.133")  //hbase客户端连接参数设置，参数含义参考hbase官方客户端文档
//          .addHbaseClientProperty("hbase.zookeeper.property.clientPort","2183")

      hBaseInputConfig.addHbaseClientProperty("hbase.zookeeper.quorum","localhost:7001")  //hbase客户端连接参数设置，参数含义参考hbase官方客户端文档
            .addHbaseClientProperty("hbase.zookeeper.property.clientPort","2185")
            .addHbaseClientProperty("zookeeper.znode.parent","/hbase")
            .addHbaseClientProperty("hbase.ipc.client.tcpnodelay","true")
            .addHbaseClientProperty("hbase.rpc.timeout","10000")
            .addHbaseClientProperty("hbase.client.operation.timeout","10000")
            .addHbaseClientProperty("hbase.ipc.client.socket.timeout.read","20000")
            .addHbaseClientProperty("hbase.ipc.client.socket.timeout.write","30000")

            .setHbaseClientThreadCount(100)  //hbase客户端连接线程池参数设置
            .setHbaseClientThreadQueue(100)
            .setHbaseClientKeepAliveTime(10000l)
            .setHbaseClientBlockedWaitTimeout(10000l)
            .setHbaseClientWarnMultsRejects(1000)
            .setHbaseClientPreStartAllCoreThreads(true)
            .setHbaseClientThreadDaemon(true)

            .setHbaseTable("AgentInfo") //指定需要同步数据的hbase表名称
            ;
```

hbase过滤条件配置

```java
//FilterList和filter二选一，只需要设置一种
      /**
       * 设置hbase检索filter
       */
      SingleColumnValueFilter scvf= new SingleColumnValueFilter(Bytes.toBytes("Info"), Bytes.toBytes("i"),

            CompareOperator.EQUAL,"wap".getBytes());

      scvf.setFilterIfMissing(true); //默认为false， 没有此列的数据也会返回 ，为true则只返回name=lisi的数据

      hBaseInputConfig.setFilter(scvf);

      /**
       * 设置hbase组合条件FilterList
       * FilterList 代表一个过滤器链，它可以包含一组即将应用于目标数据集的过滤器，过滤器间具有“与” FilterList.Operator.MUST_PASS_ALL 和“或” FilterList.Operator.MUST_PASS_ONE 关系
       */

      FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ONE); //数据只要满足一组过滤器中的一个就可以

      SingleColumnValueFilter filter1 = new SingleColumnValueFilter(Bytes.toBytes("Info"), Bytes.toBytes("i"),

            CompareOperator.EQUAL,"wap".getBytes());

      list.addFilter(filter1);

      SingleColumnValueFilter filter2 = new SingleColumnValueFilter(Bytes.toBytes("Info"), Bytes.toBytes("i"),

            CompareOperator.EQUAL,Bytes.toBytes("my other value"));

      list.addFilter(filter2);
      hBaseInputConfig.setFilterList(list);

//    //设置同步起始行和终止行key条件
      hBaseInputConfig.setStartRow(startRow);
      hBaseInputConfig.setEndRow(endRow);
      //设置记录起始时间搓（>=）和截止时间搓(<),如果是基于时间范围的增量同步，则不需要指定下面两个参数
      hBaseInputConfig.setStartTimestamp(startTimestam);
      hBaseInputConfig.setEndTimestamp(endTimestamp);
```

## 1.7 MongoDB采集插件

[MongoDBInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongodb/input/MongoDBInputConfig.java)

内容补充中。。。。。。

## 1.8 Kafka输入插件

[Kafka2InputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka2x/src/main/java/org/frameworkset/tran/plugin/kafka/input/Kafka2InputConfig.java)

内容补充中。。。。。。

## 1.9 Http输入插件

[HttpInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/http/input/HttpInputConfig.java)

内容补充中。。。。。。

# 2.输出插件

## 2.1 DB输出插件

db输出插件配置类：[DBOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/db/output/DBOutputConfig.java)



### 2.1.1 插件配置案例

直接设置数据库连接串

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
		dbOutputConfig
				.setDbName("test")
				.setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(true)
				.setDbInitSize(5)
				.setDbMinIdleSize(5)
				.setDbMaxSize(10)
				.setShowSql(true)//是否使用连接池;

				.setSqlFilepath("dsl2ndSqlFile.xml")
				.setInsertSqlName("insertSQLnew")//指定新增的sql语句名称，在配置文件中配置：sql-dbtran.xml
//				.setUpdateSqlName("updateSql")//指定修改的sql语句名称，在配置文件中配置：sql-dbtran.xml
//				.setDeleteSqlName("deleteSql")//指定删除的sql语句名称，在配置文件中配置：sql-dbtran.xml
		/**
		 * 是否在批处理时，将insert、update、delete记录分组排序
		 * true：分组排序，先执行insert、在执行update、最后执行delete操作
		 * false：按照原始顺序执行db操作，默认值false
		 * @param optimize
		 * @return
		 */
//				.setOptimize(true);//指定查询源库的sql语句，在配置文件中配置：sql-dbtran.xml
		;
		importBuilder.setOutputConfig(dbOutputConfig);
```

配置说明：

1. 设置目标数据库参数：dbname、dburl、dbuser、dbPassword、是否使用连接池（usePool）、连接池参数设置、是否打印sql语句。如果使用外部数据源，则只需要设置dbname即可。
2. 基于配置文件输出sql语句配置：sql语句配置文件路径（xml文件）、新增语句名称（InsertSqlName）、修改语句名称（updateSqlName，可选）、修改语句名称（deleteSqlName，可选）
3. 可以直接设置增删改sql语句：新增语句（InsertSql）、修改语句（updateSql，可选）、修改语句（deleteSql，可选）

### 2.1.1 sql语句设置的补充说明

正常情况下，我们只需要设置一组sql语句即可（增删改），但是在与mysql binlog输入插件或者其他多类型输入插件（比如文件输入插件）对接时，需要根据不同表，或者不同的文件路径，设置各自对应的一组sql语句（增删改），因此采用以下方法来应对各种情况。

### 2.1.2 mysql binlog监听多表场景

参考文档：[mysql binlog插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_13-mysql-binlog%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)

场景源码：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/binlog/Binlog2DBOutput.java

 mySQLBinlogConfig.setTables("cityperson,batchtest");//这里指定了要监听两张表

那么通过以下方式设置cityperson,batchtest表各自的sql语句

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
        dbOutputConfig
                .setDbName("test")
                .setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
                .setDbUrl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
                .setDbUser("root")
                .setDbPassword("123456")
                .setValidateSQL("select 1")
                .setUsePool(true)
                .setDbInitSize(5)
                .setDbMinIdleSize(5)
                .setDbMaxSize(10)
                .setShowSql(true)//是否使用连接池;
                .setSqlFilepath("dsl2ndSqlFile.xml");//sql语句配置文件路径
        
        //设置不同表对应的增删改sql语句
        SQLConf sqlConf = new SQLConf();
        sqlConf.setInsertSqlName("insertcitypersonSQL");//对应sql配置文件dsl2ndSqlFile.xml配置的sql语句insertcitypersonSQL
//        sqlConf.setUpdateSqlName("insertcitypersonUpdateSQL");//可选
//        sqlConf.setDeleteSqlName("insertcitypersonDeleteSQL");//可选
        dbOutputConfig.addSQLConf("cityperson",sqlConf);

        sqlConf = new SQLConf();
        sqlConf.setInsertSqlName("insertbatchtestSQL");//对应sql配置文件dsl2ndSqlFile.xml配置的sql语句insertbatchtestSQL
//        sqlConf.setUpdateSqlName("insertbatchtestUpdateSQL");//可选
//        sqlConf.setDeleteSqlName("insertbatchtestDeleteSQL");//可选
        dbOutputConfig.addSQLConf("batchtest",sqlConf);
        importBuilder.setOutputConfig(dbOutputConfig);
```

### 2.1.3 监听多个不同结构文件场景

场景源码：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/MultiFileLog2DBbatchDemo.java

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
        dbOutputConfig
                .setDbName("test")
                .setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
                .setDbUrl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
                .setDbUser("root")
                .setDbPassword("123456")
                .setValidateSQL("select 1")
                .setUsePool(true)
                .setDbInitSize(5)
                .setDbMinIdleSize(5)
                .setDbMaxSize(10)
                .setShowSql(true)//是否使用连接池;
                .setSqlFilepath("dsl2ndSqlFile.xml");//sql语句配置文件路径
        
        //设置不同表对应的增删改sql语句
        SQLConf sqlConf = new SQLConf();
        sqlConf.setInsertSqlName("insertcitypersonSQL");//对应sql配置文件dsl2ndSqlFile.xml配置的sql语句insertcitypersonSQL
//        sqlConf.setUpdateSqlName("insertcitypersonUpdateSQL");//可选
//        sqlConf.setDeleteSqlName("insertcitypersonDeleteSQL");//可选
        dbOutputConfig.addSQLConf("cityperson.txt",sqlConf);//用文件名映射该文件对应的sql语句

        sqlConf = new SQLConf();
        sqlConf.setInsertSqlName("insertbatchtestSQL");//对应sql配置文件dsl2ndSqlFile.xml配置的sql语句insertbatchtestSQL
//        sqlConf.setUpdateSqlName("insertbatchtestUpdateSQL");//可选
//        sqlConf.setDeleteSqlName("insertbatchtestDeleteSQL");//可选
        dbOutputConfig.addSQLConf("batchtest.txt",sqlConf);//用文件名映射该文件对应的sql语句
     // mysql binlog输入插件对接时，默认使用表名称映射对应的sqlconf配置
     // 其他场景需要通过SQLConfResolver接口从当前记录中获取对应的字段值作为sqlconf配置对应的映射名称
        dbOutputConfig.setSqlConfResolver(new SQLConfResolver() {
            @Override
            public String resolver(TaskContext taskContext, CommonRecord record) {
                String filePath = (String)record.getData("filePath");
                if(filePath.endsWith("batchtest.txt"))
                    return "batchtest.txt";
                else if(filePath.endsWith("cityperson.txt"))
                    return "cityperson.txt";
                return "cityperson.txt";
            }
        });
        importBuilder.setOutputConfig(dbOutputConfig);
```

我们需要将filePath元数据放入到当前记录中：

```java
importBuilder.setDataRefactor(new DataRefactor() {
    public void refactor(Context context) throws Exception {
        String filePath = (String)context.getMetaValue("filePath");
        context.addFieldValue("filePath",filePath);
```

多组sql语句场景下，optimize参数将不起作用

## 2.2 Elasticsearch输出插件

[ElasticsearchOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/es/output/ElasticsearchOutputConfig.java)

内容补充中......

## 2.3 文件输出插件

[FileOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/output/FileOutputConfig.java)

## 2.4 Excel文件输出插件

[ExcelFileOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/output/ExcelFileOutputConfig.java)

内容补充中。。。。。。

## 2.5 Kafka输出插件

[Kafka2OutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka2x/src/main/java/org/frameworkset/tran/plugin/kafka/output/Kafka2OutputConfig.java)

内容补充中。。。。。。

## 2.6 Http输出插件

[HttpOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/http/output/HttpOutputConfig.java)

内容补充中。。。。。。

## 2.7 自定义输出插件

[CustomOupputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/custom/output/CustomOupputConfig.java)

内容补充中。。。。。。

## 2.8 MongoDB输出插件

[MongoDBOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongodb/output/MongoDBOutputConfig.java)

内容补充中。。。。。。

## 2.9 HBase输出插件

[HBaseOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-hbase/src/main/java/org/frameworkset/tran/plugin/hbase/output/HBaseOutputConfig.java)

内容补充中。。。。。。

## 2.10 指标结果输出插件

[MetricsOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/metrics/output/MetricsOutputConfig.java)

内容补充中。。。。。。

## 2.11 日志调试输出插件

[DummyOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/dummy/output/DummyOutputConfig.java)

内容补充中。。。。。。



# 3.参考文档

[bboss数据采集ETL工具使用指南](https://esdoc.bbossgroups.com/#/db-es-tool?id=bboss数据采集etl工具使用指南)

[数据采集&流批一体化处理使用指南](https://esdoc.bbossgroups.com/#/etl-metrics?id=数据采集amp流批一体化处理使用指南)