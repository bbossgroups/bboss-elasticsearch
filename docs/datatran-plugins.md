# bboss-datatran插件清单

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

# 概述

bboss-datatran由 [bboss ](https://www.bbossgroups.com)开源的数据采集同步ETL工具，提供数据采集、数据清洗转换处理和数据入库以及[数据指标统计计算流批一体化](https://esdoc.bbossgroups.com/#/etl-metrics)处理功能。

bboss-datatran采用标准的输入输出异步管道来处理数据，输入插件和输出插件可以自由组合，输入插件从数据源采集数据，经过数据异步并行流批一体化处理后，输出插件将处理后的数据、指标数据输出到目标地。

![](images\datasyn-inout-now.png)

## 导入插件

通过maven坐标直接将插件引入作业工程，参考文档：[插件maven坐标](https://esdoc.bbossgroups.com/#/db-es-tool?id=_11-%e5%9c%a8%e5%b7%a5%e7%a8%8b%e4%b8%ad%e5%af%bc%e5%85%a5bboss-maven%e5%9d%90%e6%a0%87)

本文介绍bboss-datatran提供各种输入输出插件以及配置说明，使用过程中，可以根据实际情况和应用场景自由组合输入和输出插件，同时也可以参考数据同步案例大全，使用各种插件实现各种数据采集作业：

[bboss数据采集ETL案例大全](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo?id=bboss数据采集etl案例大全)

[视频：bboss 流批一体化计算入门教程](https://www.bilibili.com/video/BV1o44y1w7VP)

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

增量采集，可以设置增量截止时间偏移量，参考文档：[偏移量配置方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2854-%e6%97%b6%e9%97%b4%e6%88%b3%e5%a2%9e%e9%87%8f%e5%af%bc%e5%87%ba%e6%88%aa%e6%ad%a2%e6%97%b6%e9%97%b4%e5%81%8f%e7%a7%bb%e9%87%8f%e9%85%8d%e7%bd%ae)

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
      .setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false") 
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

### 1.2.3 全量采集

全量采集：sql中不需要设置增量字段

```java
 dbInputConfig.setSql("select * from td_sm_log")
```



### 1.2.4 并行数据加工处理

除数据库输入插件，其他输入插件采用并行模式执行数据加工方法datarefactor。

数据库默认采用串行模式执行，可以通过以下配置将串行模式切换为并行执行模式：

```java
dbInputConfig.setParallelDatarefactor(true)
```

并行加工处理模式只有在并行作业任务模式才起作用，参考章节【[4.3 串行执行和并行执行](https://esdoc.bbossgroups.com/#/db-es-tool?id=_43-串行执行和并行执行)】

为了支持并行处理数据，需要设置RecordBuidler接口：

```java
public interface RecordBuidler<T> {
    Map<String,Object> build(RecordBuidlerContext<T> recordBuidlerContext) throws DataImportException; 
    
}
```

当setParallelDatarefactor(true)时，默认使用DBRecordBuilder类来构建当前result记录为Map<String,Object>，用查询返回的字段名称作为map的key值，字段值作为map的value值，
这种场景下，在设置增量sql的变量名称和增量字段名称以及esid等信息时，都需要使用查询返回的实际字段名称。

实现代码如下：

```java
public class DBRecordBuilder implements RecordBuidler<ResultSet> {

    @Override
    public Map<String, Object> build(RecordBuidlerContext<ResultSet> recordBuidlerContext) throws DataImportException {
        DBRecordBuilderContext dbRecordBuilderContext = (DBRecordBuilderContext)recordBuidlerContext;
        try {
            return   ResultMap.buildValueObject(  dbRecordBuilderContext.getResultSet(),
                    LinkedHashMap.class,
                    dbRecordBuilderContext.getStatementInfo()) ;
        } catch (SQLException e) {
            throw ImportExceptionUtil.buildDataImportException(dbRecordBuilderContext.getImportContext(),e);
        }
    }
}
```

如果想自定义构建记录对象Map<String,Object>，则可以定义自己的RecordBuidler，例如：CustomDBRecordBuilder

```java
public class CustomDBRecordBuilder implements RecordBuidler<ResultSet> {

    @Override
    public Map<String, Object> build(RecordBuidlerContext<ResultSet> recordBuidlerContext) throws DataImportException {
        DBRecordBuilderContext dbRecordBuilderContext = (DBRecordBuilderContext)recordBuidlerContext;
        try {
            Map record = new LinkedHashMap();
            ResultSet resultSet = dbRecordBuilderContext.getResultSet();
            record.put("name",resultSet.getString("name"));
            .........
            return   record ;
        } catch (SQLException e) {
            throw ImportExceptionUtil.buildDataImportException(dbRecordBuilderContext.getImportContext(),e);
        }
    }
}
```

然后将定义好的CustomDBRecordBuilder设置到inputConfig中：

```java
dbInputConfig.setRecordBuidler(new CustomDBRecordBuilder());
```

### 1.2.5 引用第三方DataSource

在集成环境中开发作业时，可以直接引用第三方数据源：

```java
DBInputConfig dbInputConfig = new DBInputConfig();
dbInputConfig.setDbName("secondds1");
dbInputConfig.setDataSource(DBUtil.getDataSource("secondds"))//直接设置DataSource
              .setDbtype(DBFactory.DBMMysql)//需指定数据库类型      
```

设置dbname，datasource以及dbtype（数据源对应的数据库类型）三个参数即可。

## 1.3 Mysql binlog输入插件

Mysql binlog输入插件配置类：[MySQLBinlogConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-binlog/src/main/java/org/frameworkset/tran/plugin/mysqlbinlog/input/MySQLBinlogConfig.java)

Mysql binlog插件通过配置对应的mysql master ip和端口、数据库账号和口令、监听的数据库表以及binlog文件路径等信息，实时采集mysql增删改数据，支持以下三种数据采集模式：

**模式1** 直接读取binlog文件,采集文件中的增删改数据，可以配置binlog文件清单，亦可以查询mysql master status自动获取存量binlog文件清单

**模式2** 监听mysql master slave ip和端口，作业重启从binlog最新位置采集数据

**模式3** 监听mysql master slave ip和端口，启用故障容灾配置，每次重启作业从上次采集结束的位置开始采集数据

模式1适用一次性离线数据采集场景，模式2和模式3适用于实时采集场景。

<img src="images\mysql-binlog-arch.png" style="zoom:50%;" />

源表本来就有数据需要同步+实时同步,原来的数据可以基于模式1采集binlog文件，如果没有binlog文件，可以直接用数据库输入插件，直接一次性采集全表数据，然后再用模式3实现增量采集

mysql binlog插件使用文档：

https://esdoc.bbossgroups.com/#/mysql-binlog

视频教程

[实时采集Mysql binlog增删改数据教程（db-db单表多表）](https://www.bilibili.com/video/BV1ko4y1M7My)

[实战：基于bboss cdc实时同步mysql增删改数据到Elasticsearch](https://www.bilibili.com/video/BV1aW4y1f73c)

Mysql binlog输入插件配置参数和配置实例，更多介绍，浏览上面的文档。

### 1.3.1 插件配置案例

配置binlog文件清单

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

查询mysql master status自动获取存量binlog文件清单

```java
        MySQLBinlogConfig mySQLBinlogConfig = new MySQLBinlogConfig();
        mySQLBinlogConfig.setHost("localhost");
        mySQLBinlogConfig.setPort(3306);
        mySQLBinlogConfig.setDbUser("root");
        mySQLBinlogConfig.setDbPassword("123456");
        mySQLBinlogConfig.setServerId(100000L);
        mySQLBinlogConfig.setTables("cityperson,batchtest");//
        mySQLBinlogConfig.setDatabase("bboss");
        mySQLBinlogConfig.setCollectMasterHistoryBinlog(true);
        mySQLBinlogConfig.setBinlogDir("C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Data");

        importBuilder.setInputConfig(mySQLBinlogConfig);
```

需要设置BinlogDir，指定binlog文件存放的服务器目录，通过设置CollectMasterHistoryBinlog为true，自动查询获取mysql存量binlog文件清单。

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

### 1.3.4 多库多表数据同步到多目标库

多库多表数据同步到多目标库可是参考以下使用文档：

[多库多表数据同步到多目标库](https://esdoc.bbossgroups.com/#/mysql-binlog?id=_3-多库多表数据同步到多目标库)

### 1.3.5 参考文档

https://esdoc.bbossgroups.com/#/mysql-binlog

## 1.4 文件采集插件

文件采集插件

[FileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/FileInputConfig.java) 配置文件数据采集基本参数、Ftp服务器参数配置、文件过滤器配置等

插件主要特色如下:

1. 支持全量和增量采集两种模式；采集-转换-清洗-[流计算一体化融合](https://esdoc.bbossgroups.com/#/etl-metrics)处理
2. 实时采集本地/FTP日志文件、excel文件数据到kafka/elasticsearch/database/自定义处理器
3. 支持多线程并行下载和处理远程数据文件
4. 支持本地/ftp/sftp子目录下文件数据采集；
5. 支持备份采集完毕日志文件功能，可以指定备份文件保存时长，定期清理过期文件；
6. 支持自动清理下载完毕后ftp服务器上的文件;
7. 支持大量文件采集场景下的流控处理机制，通过设置同时并行采集最大文件数量，控制并行采集文件数量，避免资源过渡消耗，保证数据的平稳采集。当并行文件采集数量达到阈值时，启用流控机制，当并行采集文件数量低于最大并行采集文件数量时，继续采集后续文件。

### 1.4.1 配置案例

本地文件采集：提供记录切割设置，如果不需要可以去掉

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

如需从Ftp采集数据，增加FtpConfig即可：

```java
 FtpConfig ftpConfig = new FtpConfig().setFtpIP("127.0.0.1").setFtpPort(5322)
                .setFtpUser("1111").setFtpPassword("111@123")
                .setRemoteFileDir("/home/ecs/failLog")
                .setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_SFTP) ;//采用sftp协议，支持两种协议：ftp和sftp
 config.addConfig(new FileConfig().setFtpConfig(ftpConfig) 
                  。。。。。。。
```

文件过滤筛选配置：采集子目录文件

```java
config.addConfig(new FileConfig()
                .setSourcePath(data_dir)//指定目录
                .setFileHeadLineRegular("")//指定多行记录的开头识别标记，正则表达式
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {

                        if (fileInfo.isDirectory())//由于要采集子目录下的文件，所以如果是目录则直接返回true，当然也可以根据目录名称决定哪些子目录要采集
                            return true;
                        String fileName = fileInfo.getFileName();//获取文件名称
                        //判断是否采集文件数据，返回true标识采集，false 不采集
						if(fileName.equals("nginx.log"))
                    		return true;
                        return false;
                    }
                })//指定文件过滤器
                .setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
                //.addField("tag","elasticsearch")//添加字段tag到记录中
                .setEnableInode(false)
        //          .setIncludeLines(new String[]{".*ERROR.*"})//采集包含ERROR的日志
        //.setExcludeLines(new String[]{".*endpoint.*"}))//采集不包含endpoint的日志
);
```

### 1.4.2 一次性扫描采集文件目录下所有文件

```java
FileInputConfig config = new FileInputConfig();
      config.setCharsetEncode("GB2312");
        config.setDisableScanNewFiles(true);//设置一次性扫描采集
        config.setMaxFilesThreshold(10);//文件采集流控
      config.addConfig(new FileConfig().setSourcePath("D:\\oncelogs")//指定目录
                              .setFileHeadLineRegular("^\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]")//指定多行记录的开头识别标记，正则表达式
                              .setFileFilter(new FileFilter() {
                                 @Override
                                 public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
                                    //判断是否采集文件数据，返回true标识采集，false 不采集
                                    boolean r = fileInfo.getFileName().startsWith("metrics-report");
                                    return r;
                                 }
                              })//指定文件过滤器
                                        .setDeleteEOFFile(true) //删除采集完毕的文件
//                                        .setCloseOlderTime(10000L)
                                        .setCloseEOF(true) //关闭采集完毕的文件通道
                              .addField("tag","elasticsearch")//添加字段tag到记录中
                  );
```

关键配置说明：

FileInputConfig.setDisableScanNewFiles(true);//设置一次性扫描采集

FileInputConfig.setMaxFilesThreshold(10);//文件采集流控，控制同时采集文件数量，避免资源消耗过多

FileConfig .setDeleteEOFFile(true) //删除采集完毕的文件

FileConfig .setCloseEOF(true) //关闭采集完毕的文件通道，excel文件强制CloseEOF为true

参考文档：[一次性采集控制策略](https://esdoc.bbossgroups.com/#/filelog-guide?id=_10一次性采集控制策略)

### 1.4.3 元数据说明

```java


启用元数据config.setEnableMeta(true); 后，将以下两个包含元数据信息的字段附带到记录中：@filemeta，@timestamp
         
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

@timestamp  日期类型，记录采集时间
```

子目录文件采集、清理本地/远程文件、备份本地文件配置等更多介绍，访问文档：https://esdoc.bbossgroups.com/#/filelog-guide

### 1.4.4 使用案例

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

插件配置 [ExcelFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/ExcelFileInputConfig.java)(FileInputConfig子类)和ExcelFileConfig（FileConfig子类）结合

通过ExcelFileConfig设置excel列与字段的映射关系，excel忽略行配置等，除了excel需要的配置，其他配置和文件采集插件配置一致

### 1.5.1 配置案例

```java
ExcelFileInputConfig config = new ExcelFileInputConfig();
		FileConfig excelFileConfig = new ExcelFileConfig();
		excelFileConfig
				.addCellMapping(0,"shebao_org")//0 代表第一个单元格，shebao_org 映射字段名称
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
				.setFileFilter(new FileFilter() { //设置过滤器
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

### 1.5.2 一次性采集并清除文件案例

通过设置FileInputConfig.setDisableScanNewFiles为true，控制插件采集完毕sourcePath目录下所有的文件后就结束采集作业；

通过设置FileConfig.setDeleteEOFFile(true)，可以控制删除采集完毕的文件

```java
ExcelFileInputConfig config = new ExcelFileInputConfig();
      config.setDisableScanNewFiles(true);
//shebao_org,person_no, name, cert_type,cert_no,zhs_item  ,zhs_class ,zhs_sub_class,zhs_year  , zhs_level
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
      .addCellMapping(9,"zhs_level","1");
excelFileConfig.setSourcePath("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\excelfiles")//指定目录
      .setFileFilter(new FileFilter() {
         @Override
         public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
            //判断是否采集文件数据，返回true标识采集，false 不采集
            return fileInfo.getFileName().endWith(".xlsx");
         }
      })//指定文件过滤器
      .setDeleteEOFFile(true)
      .setSkipHeaderLines(1);//忽略第一行
config.addConfig(excelFileConfig);
config.setEnableMeta(true);
importBuilder.setInputConfig(config);
```

本案例实用与文件采集插件和Excel文件采集插件。

## 1.6 HBase采集插件

插件配置类：[HBaseInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-hbase/src/main/java/org/frameworkset/tran/plugin/hbase/input/HBaseInputConfig.java)，配置hbase服务器连接参数和数据检索条件等

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

### 1.6.2 参考文档

https://esdoc.bbossgroups.com/#/hbase-elasticsearch

## 1.7 MongoDB采集插件

MongoDB输入插件配置类：[MongoDBInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongodb/input/MongoDBInputConfig.java)，配置mongodb数据库参数、数据库表、各种连接控制参数/服务器地址等

### 1.7.1 配置案例

全量同步配置

```java
		ImportBuilder importBuilder = new ImportBuilder();
		// 5.2.4.1 设置mongodb参数
		MongoDBInputConfig mongoDBInputConfig = new MongoDBInputConfig();
		mongoDBInputConfig.setName("session")
				.setDb("sessiondb")
				.setDbCollection("sessionmonitor_sessions")
				.setConnectTimeout(10000)
				.setWriteConcern("JOURNAL_SAFE")
				.setReadPreference("")
				.setMaxWaitTime(10000)
				.setSocketTimeout(1500).setSocketKeepAlive(true)
				.setConnectionsPerHost(100)
				.setServerAddresses("127.0.0.1:27017")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
				// mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
				//String database,String userName,String password,String mechanism
				//https://www.iteye.com/blog/yin-bp-2064662
;

		importBuilder.setInputConfig(mongoDBInputConfig);
```

带条件并指定返回字段同步配置

```java
//定义Mongodb到Elasticsearch数据同步组件
      ImportBuilder importBuilder = new ImportBuilder();

      // 5.2.4.1 设置mongodb参数
      MongoDBInputConfig mongoDBInputConfig = new MongoDBInputConfig();
      mongoDBInputConfig.setName("session")
            .setDb("sessiondb")
            .setDbCollection("sessionmonitor_sessions")
            .setConnectTimeout(10000)
            .setWriteConcern("JOURNAL_SAFE")
            .setReadPreference("")
            .setMaxWaitTime(10000)
            .setSocketTimeout(1500).setSocketKeepAlive(true)
            .setConnectionsPerHost(100)
            .setServerAddresses("127.0.0.1:27017")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
            // mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
            //String database,String userName,String password,String mechanism
            //https://www.iteye.com/blog/yin-bp-2064662

            ;

      //定义mongodb数据查询条件对象（可选步骤，全量同步可以不需要做条件配置）
      BasicDBObject query = new BasicDBObject();
      // 设定检索mongdodb session数据时间范围条件
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      try {
         Date start_date = format.parse("1099-01-01");
         Date end_date = format.parse("2999-01-01");
         query.append("creationTime",
               new BasicDBObject("$gte", start_date.getTime()).append(
                     "$lte", end_date.getTime()));
      }
      catch (Exception e){
         e.printStackTrace();
      }
      /**
      // 设置按照host字段值进行正则匹配查找session数据条件（可选步骤，全量同步可以不需要做条件配置）
      String host = "169.254.252.194-DESKTOP-U3V5C85";
      Pattern hosts = Pattern.compile("^" + host + ".*$",
            Pattern.CASE_INSENSITIVE);
      query.append("host", new BasicDBObject("$regex",hosts));*/
      mongoDBInputConfig.setQuery(query);

      //设定需要返回的session数据字段信息（可选步骤，同步全部字段时可以不需要做下面配置）
      List<String> fetchFields = new ArrayList<>();
        fetchFields.add("appKey");
        fetchFields.add("sessionid");
        fetchFields.add("creationTime");
        fetchFields.add("lastAccessedTime");
        fetchFields.add("maxInactiveInterval");
        fetchFields.add("referip");
        fetchFields.add("_validate");
        fetchFields.add("host");
        fetchFields.add("requesturi");
        fetchFields.add("lastAccessedUrl");
        fetchFields.add("secure");
        fetchFields.add("httpOnly");
        fetchFields.add("lastAccessedHostIP");

        fetchFields.add("userAccount");
        fetchFields.add("testVO");
        fetchFields.add("privateAttr");
        fetchFields.add("local");
        fetchFields.add("shardNo");

      mongoDBInputConfig.setFetchFields(fetchFields);
      importBuilder.setInputConfig(mongoDBInputConfig);
```

### 1.7.2 自定义构建MongoDB客户端

MongoDB客户端自定义构建接口，方便业务侧自定义MongoDB客户端配置，使用案例：

```java
   mongoDBInputConfig.setCustomSettingBuilder(new CustomSettingBuilder() {
   @Override
   public void customSettingBuilder(MongoClientSettings.Builder builder, MongoDBConfig mongoDBConfig) {
   //自定义ssl配置
   /**             clientBuilder.applyToSslSettings(builder -> {
   *                 builder.invalidHostNameAllowed(true);
   *                 builder.enabled(true);
   *                 builder.context(sscontext);     
   *              });
   **/
   logger.info("Come to customSettingBuilder.....");

                    }
                })

```

### 1.7.3 增量采集配置

指定时间戳增量字段，并设置增量查询起始值

```java
// 5.2.4.9 设置增量字段信息（可选步骤，全量同步不需要做以下配置）
//增量配置开始
importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
importBuilder.setFromFirst(false);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//设置增量查询的起始值lastvalue
try {
   Date date = format.parse("2000-01-01");
   importBuilder.setLastValue(date.getTime());
}
catch (Exception e){
   e.printStackTrace();
}
```

增量采集，可以设置增量截止时间偏移量，参考文档：[偏移量配置方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2854-%e6%97%b6%e9%97%b4%e6%88%b3%e5%a2%9e%e9%87%8f%e5%af%bc%e5%87%ba%e6%88%aa%e6%ad%a2%e6%97%b6%e9%97%b4%e5%81%8f%e7%a7%bb%e9%87%8f%e9%85%8d%e7%bd%ae)



### 1.7.4 加工和处理数据

通过DataRefactor接口，可以非常方便地加工和处理数据

```java
importBuilder.setDataRefactor(new DataRefactor() {
         public void refactor(Context context) throws Exception  {
            String id = context.getStringValue("_id");
            //根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中
            if(id.equals("5dcaa59e9832797f100c6806"))
               context.setDrop(true);
            //添加字段extfiled2到记录中，值为2
            context.addFieldValue("extfiled2",2);
            //添加字段extfiled到记录中，值为1
            context.addFieldValue("extfiled",1);
            boolean httpOnly = context.getBooleanValue("httpOnly");
            boolean secure = context.getBooleanValue("secure");
            //空值处理
            String userAccount = context.getStringValue("userAccount");
            if(userAccount == null)
               context.addFieldValue("userAccount","");
            //空值处理
            String testVO = context.getStringValue("testVO");
            if(testVO == null)
               context.addFieldValue("testVO","");
            //空值处理
            String privateAttr = context.getStringValue("privateAttr");
            if(privateAttr == null)
               context.addFieldValue("privateAttr","");
            //空值处理
            String local = context.getStringValue("local");
            if(local == null)
               context.addFieldValue("local","");
            //将long类型的lastAccessedTime字段转换为日期类型
            long lastAccessedTime = context.getLongValue("lastAccessedTime");
            context.addFieldValue("lastAccessedTime",new Date(lastAccessedTime));
            //将long类型的creationTime字段转换为日期类型
            long creationTime = context.getLongValue("creationTime");
            context.addFieldValue("creationTime",new Date(creationTime));
            //根据session访问客户端ip，获取对应的客户地理位置经纬度信息、运营商信息、省地市信息IpInfo对象
            //并将IpInfo添加到Elasticsearch文档中
            String referip = context.getStringValue("referip");
            if(referip != null){
               IpInfo ipInfo = context.getIpInfoByIp(referip);
               if(ipInfo != null)
                  context.addFieldValue("ipInfo",ipInfo);
            }
            /**
            String oldValue = context.getStringValue("axx");
            String newvalue = oldValue+" new value";
            context.newName2ndData("axx","newname",newvalue);
             */
             //除了通过context接口获取mongodb的记录字段，还可以直接获取当前的mongodb记录，可自行利用里面的值进行相关处理
             Document record = (Document) context.getRecord();
            //不需要输出的字段，可以忽略掉这些属性
//          context.addIgnoreFieldMapping("author");
//          context.addIgnoreFieldMapping("title");
//          context.addIgnoreFieldMapping("subtitle");

         }
      });
```

### 1.7.5 更多案例及文档

https://gitee.com/bboss/mongodb-elasticsearch

https://esdoc.bbossgroups.com/#/mongodb-elasticsearch

## 1.8 MongoDB CDC插件

基于MongoDB Data ChangeStream，实时采集MongoDB增、删、改以及替换数据，同步到其他数据库。

MongoDB CDC输入插件配置类：[MongoCDCInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongocdc/MongoCDCInputConfig.java)，配置mongodb数据库参数、监听的数据库以及表清单、各种连接控制参数/服务器地址等。

可以非常方便地实现重启恢复点采集机制。

### 1.8.1 关键参数

name   MongoDB数据源名称

enableIncrement 控制重启后是否从上次结束位置继续采集数据，true 启用 false 重启后从最新位置采集数据

updateLookup 返回完整的修改记录数据 true 返回全部记录信息 false 返回修改信息

includePreImage 是否包含修改前/替代前的数据 true 返回 false 不返回

dbIncludeList  要监听的MongoDB数据库清单，多个用逗号分隔，不指定监听所有

collectionIncludeList 要监听的表清单，多个用逗号分隔，不指定监听所有

监听的不同库表数据，可以参考文档，输出到不同目标地：

数据库输出插件：[2.1.4 数据输出到多个目标库](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_214-数据输出到多个目标库)

MongoDB输出插件：[2.8.2 多表输出配置案例](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_282-多表输出配置案例)

其他连接参数和认证配置方法，参考下面的插件配置案例。

同步修改数据时，同步记录会包含以下两个字段

```java
mongodb.updateDescription  修改描述对象，类型为UpdateDescription，包含修改、删除描述信息和操作数据
mongodb.update.removedFields ---List<String>类型，包含删除的字段列表，如果没有删除字段则为空
```

### 1.8.2 插件配置案例

```java
MongoCDCInputConfig mongoCDCInputConfig = new MongoCDCInputConfig();
        mongoCDCInputConfig.setName("session");
        mongoCDCInputConfig.setEnableIncrement(true);//启用重启恢复点采集机制
        //设置要采集的操作类型，不设置都采集
		mongoCDCInputConfig.addIncludeOperation(Record.RECORD_INSERT);//采集新增数据
        mongoCDCInputConfig.addIncludeOperation(Record.RECORD_UPDATE);//采集新增修改/替换数据
        mongoCDCInputConfig.addIncludeOperation(Record.RECORD_DELETE);//采集删除数据
        mongoCDCInputConfig.setIncludePreImage(true)
              .setUpdateLookup(true)
                .setDbIncludeList("sessiondb")
              .setCollectionIncludeList("sessionmonitor_sessions,session_sessions")
                .setConnectString("mongodb://192.168.137.1:27017,192.168.137.1:27018,192.168.137.1:27019/?replicaSet=rs0")
                .setConnectTimeout(10000)
                .setMaxWaitTime(10000)
                .setSocketTimeout(1500).setSocketKeepAlive(true)
                .setConnectionsPerHost(100)
//                .setServerAddresses("127.0.0.1:27017")//多个地址用回车换行符或者逗号分割：127.0.0.1:27017\n127.0.0.1:27018
                // mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
                //String database,String userName,String password,String mechanism
                //https://www.iteye.com/blog/yin-bp-2064662
//          .setUserName("bboss")
//            .setPassword("bboss")
//            .setMechanism("MONGODB-CR")
//            .setAuthDb("sessiondb")
                ;


        importBuilder.setInputConfig(mongoCDCInputConfig);
```

### 1.8.3 完整的案例

同步数据到MongoDB案例

https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/cdc/MongodbCDC2MongoDBDemo.java

自定义数据输出案例

https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/cdc/MongodbCDCDemo.java

### 1.8.4 参考资料

https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/crud/read-operations/change-streams/

https://www.mongodb.com/docs/manual/changeStreams/

## 1.9 Kafka输入插件

Kafka输入插件配置类：[Kafka2InputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka2x/src/main/java/org/frameworkset/tran/plugin/kafka/input/Kafka2InputConfig.java)，可以配置kafka消费端参数，包括kafka服务器地址、消费组id、自动提交机制、offset机制、消费线程数量、消息处理工作线程数、线程队列长度、topic、消息序列化机制等。

案例工程：https://gitee.com/bboss/kafka2x-elasticsearch

### 1.9.1 插件配置案例

```java
// kafka服务器参数配置
      // kafka 2x 客户端参数项及说明类：org.apache.kafka.clients.consumer.ConsumerConfig
      Kafka2InputConfig kafka2InputConfig = new Kafka2InputConfig();
      kafka2InputConfig//.addKafkaConfig("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer")
            //.addKafkaConfig("key.deserializer","org.apache.kafka.common.serialization.LongDeserializer")
            .addKafkaConfig("group.id","trandbtest") // 消费组ID
            .addKafkaConfig("session.timeout.ms","30000")
            .addKafkaConfig("auto.commit.interval.ms","5000")
            .addKafkaConfig("auto.offset.reset","latest")
//          .addKafkaConfig("bootstrap.servers","192.168.137.133:9093")
            .addKafkaConfig("bootstrap.servers","127.0.0.1:9092")
            .addKafkaConfig("enable.auto.commit","false")//一般不要开启自动提交
            .addKafkaConfig("max.poll.records","500") // The maximum number of records returned in a single call to poll().
            .setKafkaTopic("xinkonglog") // kafka topics，多个用逗号分隔,例如：xinkonglog,xinkonglog1
            .setConsumerThreads(5) // 并行消费线程数，建议与topic partitions数一致
            .setKafkaWorkQueue(10)//每个消费线程对应的工作等待队列长度
            .setKafkaWorkThreads(2)//每个消费线程对应的工作线程数量

            .setPollTimeOut(1000) // 从kafka consumer poll(timeout)参数
            .setValueCodec(CODEC_JSON)
            .setKeyCodec(CODEC_LONG);
      importBuilder.setInputConfig(kafka2InputConfig);
```

目前只接收String类型的消息，关键配置说明：

kafka2InputConfig.setValueCodec(CODEC_JSON)     json消息反序列化，将kafka中json字符串自动转换为map对象，如果消息是json格式，可以指定这个解码器，如果不指定默认就是String解码器

kafka2InputConfig.setKeyCodec(CODEC_LONG)  如果key是long类型，采用long反序列化消息key，这是一个可选配置

Codec目录可以支持以下类型：

CODEC_TEXT 

CODEC_TEXT_SPLIT

CODEC_JSON 

CODEC_LONG 

CODEC_INTEGER 

CODEC_BYTE（byte数组）

如果配置了value.deserializer，那么setValueCodec将不起作用

如果配置了key.deserializer，那么setKeyCodec将不起作用

如果消息是其他格式，则可以设置CODEC_TEXT 类型，然后自行在datarefactor中进行处理，亦可以按照分割字符进行解析，然后进行字段映射处理。

### 1.9.2 自定义消息转换处理

非格式化的字符串消息，可以自行在datarefactor中进行灵活的转换处理：

```java
kafka2InputConfig.setValueCodec(CODEC_TEXT );
importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {

				 // 获取原始的Kafka记录
				 KafkaStringRecord record = (KafkaStringRecord) context.getCurrentRecord().getRecord();
				 if(record.getKey() == null)
				 	System.out.println("key is null!");
                 String message = (String)record.getData();
                 String[] datas = message.split("|");//举个简单的处理实例：按指定消息字段分割符，将消息切割为字符串数组
                //指定数组元素与字段名称的映射配置
                context.addFieldValue("logOperTime",datas[0]);
                context.addFieldValue("operModule",datas[1]);
                context.addFieldValue("logOperuser",datas[2]);
			}
		});
```

如果消息是按照特定分割字符拼接而成，那么可以直接按照特定分隔符进行解析，然后进行字段映射处理，valuecodec必须设置为CODEC_TEXT_SPLIT

```java
kafka2InputConfig .setValueCodec(CODEC_TEXT_SPLIT);

Kafka2InputConfig kafka2InputConfig = new Kafka2InputConfig();
kafka2InputConfig.setFieldSplit(";");//指定消息字段分割符，按照分隔符将消息切割为字符串数组
//指定数组元素与字段名称的映射配置，
kafka2InputConfig.addCellMapping(0, "logOperTime");
kafka2InputConfig.addCellMapping(1, "operModule");
kafka2InputConfig.addCellMapping(2, "logOperuser");
```

通过上述的处理，可以非常灵活地处理各种格式的字符串kafka消息，然后将处理后的数据通过各种输出插件进行输出。

### 1.9.3 消费多个topic主体消息配置

kafka输入插件可以同时消费多个topic主体消息，多个topic之间用逗号分隔，配置实例如下：

```java
kafka2InputConfig.setKafkaTopic("xinkonglog,xinkonglog1") // kafka topics，多个用逗号分隔,例如：xinkonglog,xinkonglog1
```

### 1.9.4 参考文档

[2.8.7.2 kafka输入插件拦截器设置说明](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2872-kafka输入插件拦截器设置说明)



## 1.10 Http输入插件

Http输入插件配置类：[HttpInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/http/input/HttpInputConfig.java)，配置http服务连接池参数、query dsl文件路径、dsl名称、query url、是否分页查询、http请求头（动态/静态两种类型）、返回结果解析器等等；同时可以在dsl中引用通过以下两个方法添加的参数：

importBuilder.addJobInputParam  静态参数

importBuilder.addJobDynamicInputParam   动态参数

### 1.10.1 插件配置案例

```java
ImportBuilder importBuilder = new ImportBuilder() ;
importBuilder.setFetchSize(50) //指定分页获取记录数
    .setBatchSize(10);
HttpInputConfig httpInputConfig = new HttpInputConfig();


httpInputConfig.setDslFile("httpdsl.xml")
      .setQueryDslName("queryPagineDsl")
      .setQueryUrl("/httpservice/getPagineData.api")
      .setPagine(true)
      .setShowDsl(true)
      .setPagineFromKey("httpPagineFrom")
      .setPagineSizeKey("httpPagineSize")
      .addHttpHeader("testHeader","xxxxx")
      .addDynamicHeader("Authorization", new DynamicHeader() {
         @Override
         public String getValue(String header, DynamicHeaderContext dynamicHeaderContext) throws Exception {
            //判断服务token是否过期，如果过期则需要重新调用token服务申请token
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZWZhdWx0XzYxNTE4YjlmM2UyYmM3LjEzMDI5OTkxIiwiaWF0IjoxNjMyNzM0MTExLCJuYmYiOjE2MzI3MzQxMTEsImV4cCI6MTYzMjc0MTMxMSwiZGV2aWNlX2lkIjoiYXBwMDMwMDAwMDAwMDAwMSIsImFwcF9pZCI6ImFwcDAzIiwidXVpZCI6ImFkZmRhZmFkZmFkc2ZlMzQxMzJmZHNhZHNmYWRzZiIsInNlY3JldCI6ImFwcDAzMVEyVzNFd29ybGQxMzU3OVBhc3NBU0RGIiwiaXNzdWVfdGltZSI6MTYzMjczNDExMSwiand0X3NjZW5lIjoiZGVmYXVsdCJ9.mSl-JBUV7gTUapn9yV-VLfoU7dm-gxC7pON62DnD-9c";
            return token;
         }
      })
      .setHttpResultParser(new HttpResultParser<Map>() {
         @Override
         public void parserHttpResult(HttpResult<Map> httpResult, HttpResultParserContext httpResultParserContext) throws Exception{
            HttpResponse httpResponse = httpResult.getResponse();
            HttpEntity entity = httpResponse.getEntity();
            if(entity == null)
               return;
            String datas = EntityUtils.toString(entity);
            //可以自行对返回值进行处理，比如解密，或者签名校验，但是最终需要将包含在datas里面的采集的数据集合转换为List<Map>结构，便于后续对数据进行加工处理
            //这里由于数据本身就是List<Map>结构，所以只需要做简单的序列化处理操作即可，这个也是默认的操作
            List<Map> _datas = SimpleStringUtil.json2ListObject(datas, Map.class);
            httpResult.setDatas(_datas);//必须将得到的集合设置到httpResult中，否则无法对数据进行后续处理
            httpResult.setParseredObject(datas);//设置原始数据
         }
      })
      .addSourceHttpPoolName("http.poolNames","datatran")
      .addHttpInputConfig("datatran.http.health","/health")
      .addHttpInputConfig("datatran.http.hosts","192.168.137.1:808")
      .addHttpInputConfig("datatran.http.timeoutConnection","5000")
      .addHttpInputConfig("datatran.http.timeoutSocket","50000")
      .addHttpInputConfig("datatran.http.connectionRequestTimeout","50000")
      .addHttpInputConfig("datatran.http.maxTotal","200")
      .addHttpInputConfig("datatran.http.defaultMaxPerRoute","100")
      .addHttpInputConfig("datatran.http.failAllContinue","true");


importBuilder.setInputConfig(httpInputConfig);
importBuilder.addJobInputParam("otherParam","陈雨菲2:0战胜戴资颖")
          .addJobInputParam("device_id","app03001")
           .addJobInputParam("app_id","app03")
.addJobDynamicInputParam("signature", new DynamicParam() {//根据数据动态生成签名参数
   @Override
   public Object getValue(String paramName, DynamicParamContext dynamicParamContext) {

      //可以根据自己的算法对数据进行签名
      String signature = "1b3bb71f6ebae2f52b7a238c589f3ff9";//signature =md5(datas)
      return signature;
   }
});
```

httpdsl.xml中配置的queryPagineDsl：

```xml
<property name="queryPagineDsl">
    <![CDATA[
    {
        "device_id": #[device_id], ## device_id,通过addJobInputParam赋值
        "app_id": #[app_id], ## app_id,通过addJobInputParam赋值
        "logTime":#[logTime],## 传递增量时间起始条件
        "logTimeEndTime":#[logTime__endTime],## 传递增量时间截止时间条件，必须指定IncreamentEndOffset偏移时间量才能设置增量截止时间
        "from":#[httpPagineFrom], ## 如果服务支持分页获取增量或者全量数据，设置分页起始位置
        "size":#[httpPagineSize],  ## 如果服务支持分页获取增量或者全量数据，设置每页记录数，如果实际返回的记录数小于httpPagineSize或者为0，则表示本次分页获取数据结束，对应参数fetchSize配置的值
        "otherParam": #[otherParam] ## 其他服务参数otherParam,通过addJobInputParam赋值
    }
    ]]></property>
```

### 1.10.2 参考文档

http输入插件更具体的介绍：[Http/Https插件使用指南](https://esdoc.bbossgroups.com/#/datatran-http?id=httphttps插件使用指南)

## 1.11 Word文件采集插件

插件配置 [WordFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/WordFileInputConfig.java)(FileInputConfig子类)和WordFileConfig（FileConfig子类）结合

通过WordFileConfig设置WordExtractor自定义word文件内容提取逻辑，如果不设置setWordExtractor，默认将文件内容放置到wordContent字段中，除了word文件采集需要的配置，其他配置和文件采集插件配置一致

### 1.11.1 配置案例

```java
WordFileInputConfig wordFileInputConfig = new WordFileInputConfig();
      wordFileInputConfig.setDisableScanNewFiles(true);
      wordFileInputConfig.setDisableScanNewFilesCheckpoint(false);
//shebao_org,person_no, name, cert_type,cert_no,zhs_item  ,zhs_class ,zhs_sub_class,zhs_year  , zhs_level
//配置excel文件列与导出字段名称映射关系
      WordFileConfig wordFileConfig = new WordFileConfig();
      /**
       * 如果不设置setWordExtractor，默认将文件内容放置到wordContent字段中
       */
      wordFileConfig.setWordExtractor(new WordExtractor() {
          @Override
          public void extractor(RecordExtractor<XWPFWordExtractor> recordExtractor) throws Exception {
              Map record = new LinkedHashMap();
              if(recordExtractor.getDataObject() != null)
                  record.put("text",recordExtractor.getDataObject().getText());
              else
                  record.put("text","");
              recordExtractor.addRecord(record);
          }


      });
wordFileConfig.setSourcePath("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\wordfiles")//指定目录
      .setFileFilter(new FileFilter() {
         @Override
         public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
            //判断是否采集文件数据，返回true标识采集，false 不采集
            return true;
         }
      });//指定文件过滤器
wordFileInputConfig.addConfig(wordFileConfig);


wordFileInputConfig.setEnableMeta(true);
importBuilder.setInputConfig(wordFileInputConfig);
```

### 1.11.2 一次性采集并清除文件案例

通过设置FileInputConfig.setDisableScanNewFiles为true，控制插件采集完毕sourcePath目录下所有的文件后就结束采集作业；

通过设置FileConfig.setDeleteEOFFile(true)，可以控制删除采集完毕的文件
https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/word/WordFile2CustomDemoOnce.java

## 1.12 PDF文件采集插件

插件配置 [PDFFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/PDFFileInputConfig.java)(FileInputConfig子类)和PDFFileConfig（FileConfig子类）结合

通过PDFFileConfig设置PDFExtractor自定义pdf文件内容提取逻辑，如果不设置setPdfExtractor，默认将文件内容放置到pdfContent字段中，除了pdf文件采集需要的配置，其他配置和文件采集插件配置一致

### 1.12.1 配置案例

```java
ImportBuilder importBuilder = new ImportBuilder();
        importBuilder.setJobId("PDFFile2CustomDemoOnce");//作业给一个唯一标识，避免和其他同类作业任务冲突
		importBuilder.setBatchSize(500)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
		importBuilder.setFlushInterval(10000l);
		PDFFileInputConfig pdfFileInputConfig = new PDFFileInputConfig();
        pdfFileInputConfig.setDisableScanNewFiles(true);
        pdfFileInputConfig.setDisableScanNewFilesCheckpoint(false);
		//shebao_org,person_no, name, cert_type,cert_no,zhs_item  ,zhs_class ,zhs_sub_class,zhs_year  , zhs_level
		//配置excel文件列与导出字段名称映射关系
        PDFFileConfig pdfFileConfig = new PDFFileConfig();
        /**
         * 如果不设置setPdfExtractor，默认将文件内容放置到pdfContent字段中
         */
        pdfFileConfig.setPdfExtractor(new PDFExtractor() {
            @Override
            public void extractor(RecordExtractor<PDDocument> recordExtractor) throws Exception {
                Map record = new LinkedHashMap();
                if(recordExtractor.getDataObject() != null) {
                    PDFTextStripper pdfStripper = new PDFTextStripper();


                    //Retrieving text from PDF document

                    String text = pdfStripper.getText(recordExtractor.getDataObject());
                    record.put("wordContent", text);
                }
                else{


                    record.put("wordContent", "");
                }
                recordExtractor.addRecord(record);
            }




        });
		pdfFileConfig.setSourcePath("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\pdffiles")//指定目录
				.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
						//判断是否采集文件数据，返回true标识采集，false 不采集
						return true;
					}
				});//指定文件过滤器
		pdfFileInputConfig.addConfig(pdfFileConfig);


		pdfFileInputConfig.setEnableMeta(true);
		importBuilder.setInputConfig(pdfFileInputConfig);
```

### 1.12.2 一次性采集并清除文件案例

通过设置FileInputConfig.setDisableScanNewFiles为true，控制插件采集完毕sourcePath目录下所有的文件后就结束采集作业；

通过设置FileConfig.setDeleteEOFFile(true)，可以控制删除采集完毕的文件
https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/pdf/PDFFile2CustomDemoOnce.java

## 1.13 其他类型文件采集插件

插件配置 [CommonFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/CommonFileInputConfig.java)(FileInputConfig子类)和CommonFileConfig（FileConfig子类）结合

通过CommonFileConfig设置CommonFileExtractor自定义其他类型（图片、视频等）文件内容提取逻辑，必须通过setCommonFileExtractor设置CommonFileExtractor，提取文件内容，除了其他类型（图片、视频等）文件采集需要的配置，其他配置和文件采集插件配置一致

### 1.13.1 配置案例

以采集图片文件二进制内容并转换为base64编码为案例进行说明

```java
ImportBuilder importBuilder = new ImportBuilder();
		importBuilder.setBatchSize(500)//设置批量入库的记录数
				.setFetchSize(1000);//设置按批读取文件行数
        importBuilder.setJobId("PictureFile2CustomDemoOnce");//作业给一个唯一标识，避免和其他同类作业任务冲突
		//设置强制刷新检测空闲时间间隔，单位：毫秒，在空闲flushInterval后，还没有数据到来，强制将已经入列的数据进行存储操作，默认8秒,为0时关闭本机制
		importBuilder.setFlushInterval(10000l);
		CommonFileInputConfig commonFileInputConfig = new CommonFileInputConfig();
        commonFileInputConfig.setDisableScanNewFiles(true);
        commonFileInputConfig.setDisableScanNewFilesCheckpoint(false);
		//shebao_org,person_no, name, cert_type,cert_no,zhs_item  ,zhs_class ,zhs_sub_class,zhs_year  , zhs_level
		//配置excel文件列与导出字段名称映射关系
        CommonFileConfig commonFileConfig = new CommonFileConfig();
        commonFileConfig.setCommonFileExtractor(new CommonFileExtractor() {
            @Override
            public void extractor(RecordExtractor<File> recordExtractor) throws Exception {
                Map record = new LinkedHashMap();
                if(recordExtractor.getDataObject() != null) {
                    File pic = recordExtractor.getDataObject();
                    record.put("picContent", Base64.encode(FileUtil.readFully(pic)));
                }
                else
                    record.put("picContent","");
                recordExtractor.addRecord(record);
            }


        });
		commonFileConfig.setSourcePath("D:\\workspace\\bbossesdemo\\filelog-elasticsearch\\picfiles")//指定目录
				.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(FilterFileInfo fileInfo, FileConfig fileConfig) {
						//判断是否采集文件数据，返回true标识采集，false 不采集
						return true;
					}
				});//指定文件过滤器
		commonFileInputConfig.addConfig(commonFileConfig);


		commonFileInputConfig.setEnableMeta(true);
		importBuilder.setInputConfig(commonFileInputConfig);
```

### 1.13.2 一次性采集并清除文件案例

通过设置FileInputConfig.setDisableScanNewFiles为true，控制插件采集完毕sourcePath目录下所有的文件后就结束采集作业；

通过设置FileConfig.setDeleteEOFFile(true)，可以控制删除采集完毕的文件,图片文件内容采集实例：
https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/pic/PictureFile2CustomDemoOnce.java

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
				.setDbUrl("jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") 
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

可以通过datarefactor接口进行人工设置，如果是mysql binlog cdc和MongoDB cdc则会自动标记增、删、改状态

参考文档：[人工标记记录为增删改状态](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2819-%e5%90%8c%e6%ad%a5%e5%a2%9e%e5%88%a0%e6%94%b9%e6%95%b0%e6%8d%ae%e5%88%b0es)

### 2.1.2 sql语句设置的补充说明

正常情况下，我们只需要设置一组sql语句即可（增删改），但是在与mysql binlog输入插件或者其他多类型输入插件（比如文件输入插件）对接时，需要根据不同表，或者不同的文件路径，设置各自对应的一组sql语句（增删改），因此采用以下方法来应对各种情况。

### 2.1.3 mysql binlog监听多表场景

参考文档：[mysql binlog插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_13-mysql-binlog%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)

场景源码：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/binlog/Binlog2DBOutput.java

 mySQLBinlogConfig.setTables("cityperson,batchtest");//这里指定了要监听两张表

那么通过以下方式设置cityperson,batchtest表各自的sql语句

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
        dbOutputConfig
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

### 2.1.4 监听多个不同结构文件场景

场景源码：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/MultiFileLog2DBbatchDemo.java

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
        dbOutputConfig
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

### 2.1.5 数据输出到多个目标库

为不同的库表sql配置指定对应的目标数据源，多个用逗号分隔，如果不指定就采用dbOutputConfig.setDbName方法设置的数据源

```java
sqlConf.setTargetDbName("test,ddlsyn");//多个用逗号分隔
```

参考文档：[多库多表数据同步到多目标库](https://esdoc.bbossgroups.com/#/mysql-binlog?id=_3-多库多表数据同步到多目标库)

### 2.1.6 Sql语句配置参考

Sql语句配置参考：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/resources/dsl2ndSqlFile.xml

```xml
 <!--
    插入数据sql
    -->
    <property name="insertSQLnew">
        <![CDATA[INSERT INTO batchtest ( name, author, content, title, optime, oper, subtitle, collecttime,ipinfo)
                VALUES ( #[operModule],  ## 来源dbdemo索引中的 operModule字段
                         #[author], ## 通过datarefactor增加的字段
                         #[logContent], ## 来源dbdemo索引中的 logContent字段
                         #[title], ## 通过datarefactor增加的字段
                         #[logOpertime], ## 来源dbdemo索引中的 logOpertime字段
                         #[logOperuser],  ## 来源dbdemo索引中的 logOperuser字段
                         #[subtitle], ## 通过datarefactor增加的字段
                         #[collecttime], ## 通过datarefactor增加的字段
                         #[ipinfo]) ## 通过datarefactor增加的地理位置信息字段
]]>
    </property>
```

其中的#[ipinfo]为bboss sql语句变量语法:#[fieldName] ,其中的fieldName为数据采集记录中字段名称;

以下为bboss注释语法

单行：

```properties
## 通过datarefactor增加的字段
```

多行：

```properties
#*
注释内容
*#
```

bboss 持久层sql管理和配置参考文档：https://doc.bbossgroups.com/#/persistent/SqlXml

除了通过配置文件设置sql，还可以直接在dboutputconfig或者sqlconf直接指定sql语句，例如：

DBOutputConfig.setInsertSql/setUpdateSql/setDeleteSql

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
dbOutputConfig.setDbName("target")
      .setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
      .setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
      .setDbUser("root")
      .setDbPassword("123456")
      .setValidateSQL("select 1")
      .setUsePool(true)//是否使用连接池
      .setInsertSql("INSERT INTO batchtest ( name, author, content, title, optime, oper, subtitle, collecttime,ipinfo)\n" +
            "                VALUES ( #[OPER_MODULE],  ## 来源dbdemo索引中的 operModule字段\n" +
            "                         #[author], ## 通过datarefactor增加的字段\n" +
            "                         #[LOG_CONTENT], ## 来源dbdemo索引中的 logContent字段\n" +
            "                         #[title], ## 通过datarefactor增加的字段\n" +
            "                         #[logOpertime], ## 来源dbdemo索引中的 logOpertime字段\n" +
            "                         #[LOG_OPERUSER],  ## 来源dbdemo索引中的 logOperuser字段\n" +
            "                         #[subtitle], ## 通过datarefactor增加的字段\n" +
            "                         #[collecttime], ## 通过datarefactor增加的字段\n" +
            "                         #[ipinfo]) ## 通过datarefactor增加的地理位置信息字段");
```

sqlconf配置

sqlConf.setInsertSql/setUpdateSql/setDeleteSql

```java
sqlConf.setInsertSql("INSERT INTO batchtest ( name, author, content, title, optime, oper, subtitle, collecttime,ipinfo)\n" +
        "                VALUES ( #[OPER_MODULE],  ## 来源dbdemo索引中的 operModule字段\n" +
                "                         #[author], ## 通过datarefactor增加的字段\n" +
                "                         #[LOG_CONTENT], ## 来源dbdemo索引中的 logContent字段\n" +
                "                         #[title], ## 通过datarefactor增加的字段\n" +
                "                         #[logOpertime], ## 来源dbdemo索引中的 logOpertime字段\n" +
                "                         #[LOG_OPERUSER],  ## 来源dbdemo索引中的 logOperuser字段\n" +
                "                         #[subtitle], ## 通过datarefactor增加的字段\n" +
                "                         #[collecttime], ## 通过datarefactor增加的字段\n" +
                "                         #[ipinfo]) ## 通过datarefactor增加的地理位置信息字段");
```

### 2.1.7 引用第三方DataSource

在集成环境中开发作业时，可以直接引用第三方数据源：

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
		dbOutputConfig.setDbName("firstds1")//指定目标数据库，在application.properties文件中配置
                .setDataSource(DBUtil.getDataSource("firstds"))//直接设置DataSource
                .setDbtype(DBFactory.DBMMysql)//需指定数据库类型    
```

设置dbname，datasource以及dbtype（数据源对应的数据库类型）三个参数即可。

## 2.2 Elasticsearch输出插件

Elasticsearch输出插件配置类：[ElasticsearchOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/es/output/ElasticsearchOutputConfig.java)，配置Elasticsearch集群配置、http连接池参数配置、输出索引配置、索引类型配置，可以指定动态索引名称和固定索引名称，配置索引id生成规则，同时还可以将数据同步到多个Elasticsearch集群。

### 2.2.1 同步到一个Elasticsearch案例

同步到一个Elasticsearch：通过elasticsearch.serverNames指定和配置了一个数据源default，只会同步到这个Default对应的数据源

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
//          .setIndexType("dbdemo") ;//es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType;
//          .setRefreshOption("refresh")//可选项，null表示不实时刷新，elasticsearchOutputConfig.setRefreshOption("refresh");表示实时刷新
      

      importBuilder.setOutputConfig(elasticsearchOutputConfig);
```

### 2.2.2 同步到多个Elasticsearch案例

同步到多个Elasticsearch：通过elasticsearch.serverNames定义了两个需要输出的es数据源default,test

```java
ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
      elasticsearchOutputConfig
            .addTargetElasticsearch("elasticsearch.serverNames","default,test")
            //default 集群配置
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
          //test 集群配置
          .addElasticsearchProperty("test.elasticsearch.rest.hostNames","192.168.137.2:9200")
            .addElasticsearchProperty("test.elasticsearch.showTemplate","true")
            .addElasticsearchProperty("test.elasticUser","elastic")
            .addElasticsearchProperty("test.elasticPassword","changeme")
            .addElasticsearchProperty("test.elasticsearch.failAllContinue","true")
            .addElasticsearchProperty("test.http.timeoutSocket","60000")
            .addElasticsearchProperty("test.http.timeoutConnection","40000")
            .addElasticsearchProperty("test.http.connectionRequestTimeout","70000")
            .addElasticsearchProperty("test.http.maxTotal","200")
            .addElasticsearchProperty("test.http.defaultMaxPerRoute","100")
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
//          .setIndexType("dbdemo") ;//es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType;
//          .setRefreshOption("refresh")//可选项，null表示不实时刷新，elasticsearchOutputConfig.setRefreshOption("refresh");表示实时刷新
      /**
       * es相关配置
       */
      elasticsearchOutputConfig.setTargetElasticsearch("default,test");//将数据源数据同步数据到两个es集群default,test

      importBuilder.setOutputConfig(elasticsearchOutputConfig);
```



### 2.2.3 插件参考文档

[设置ES数据导入控制参数](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2816-设置es数据导入控制参数)

[灵活指定索引名称和索引类型](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2813-灵活指定索引名称和索引类型)

[设置索引文档id机制](https://esdoc.bbossgroups.com/#/db-es-tool?id=_284-设置文档id机制)

## 2.3 文件输出插件

文件输出插件配置类：[FileOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/output/FileOutputConfig.java)，配置输出目录、记录输出格式、输出文件名称生成规则、文件记录切割记录数；结合FtpOutConfig对象，设置文件上传ftp参数。

### 2.3.1 导出到本地文件

导出数据到本地文件

```java
ImportBuilder importBuilder = new ImportBuilder();
      importBuilder.setBatchSize(5).setFetchSize(5);
      FileOutputConfig fileOupputConfig = new FileOutputConfig();
      fileOupputConfig.setMaxFileRecordSize(1000);//每次任务调度执行时，每千条记录生成一个文件
      fileOupputConfig.setFileDir("D:\\workdir");
      //设置文件名称生成规则
      fileOupputConfig.setFilenameGenerator(new FilenameGenerator() {
         @Override
         public String genName( TaskContext taskContext,int fileSeq) {
            String formate = "yyyyMMddHHmmss";
            //HN_BOSS_TRADE00001_YYYYMMDDHHMM_000001.txt
            SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
            String time = dateFormat.format(new Date());
          
            return "metrics-report_"+time +"_" + fileSeq+".txt";
         }
      });
      fileOupputConfig.setRecordGenerator(new RecordGenerator() {
			@Override
			public void buildRecord(TaskContext taskContext, CommonRecord record, Writer writer) {
                //record.getDatas()方法返回当前记录，Map类型，key/value ，key代表字段名称，Value代表值；
                //可以将当前记录构建为需要的格式，写入到writer对象即可,这里直接将记录转换为json输出
				SerialUtil.normalObject2json(record.getDatas(),writer);
                //获取记录对应的元数据信息
                Map<String, Object> metadatas = record.getMetaDatas();

			}
		});
      importBuilder.setOutputConfig(fileOupputConfig);
```

### 2.3.2 导出并上传ftp

导出文件并上传ftp，只要添加ftp配置到FileOutputConfig即可：

```java
FtpOutConfig ftpOutConfig = new FtpOutConfig()//是否定义ftp配置
ftpOutConfig.setBackupSuccessFiles(true);//发送完毕后备份文件
ftpOutConfig.setTransferEmptyFiles(true);//是否发送空文件
//以下是ftp服务参数配置
ftpOutConfig.setFtpIP("127.0.0.1");
ftpOutConfig.setFtpPort(5322);
ftpOutConfig.setFtpUser("xxx");
ftpOutConfig.setFtpPassword("xxx@123");
ftpOutConfig.setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_SFTP) ;//采用sftp协议，支持两种协议：ftp和sftp
//上传目录
ftpOutConfig.setRemoteFileDir("/home/xxx/failLog");
ftpOutConfig.setKeepAliveTimeout(100000L);//ftp链接保活时间
ftpOutConfig.setFailedFileResendInterval(1000L);//上传失败文件重发时间间隔，单位：毫秒

fileOupputConfig.setFtpOutConfig(ftpOutConfig);//设置ftp配置到文件导出配置
```

### 2.3.3 写入空闲时间阈值配置

**maxForceFileThreshold** 单位：秒，设置文件数据写入空闲时间阈值，如果空闲时间内没有数据到来，则进行文件切割或者flush数据到文件处理。文件切割记录规则：达到最大记录数或者空闲时间达到最大空闲时间阈值，进行文件切割 。 如果不切割文件，达到最大最大空闲时间阈值，当切割文件标识为false时，只执行flush数据操作，不关闭文件也不生成新的文件，否则生成新的文件。本属性适用于文件输出插件与kafka、mysql binlog 、fileinput等事件监听型的输入插件配合使用，其他类型输入插件无需配置。

切割文件配置实例：

```java
fileOutputConfig.setMaxFileRecordSize(100);//达到最大记录数100,切割文件
fileOutputConfig.setMaxForceFileThreshold(60);//默认每5秒扫描，上次写入数据后，是否已经超过60秒没有新数据写入，如果没有并且缓存中有数据，则切割生成文件，否则不做任何处理
```

flush数据配置实例：

```java
//fileOutputConfig.setMaxFileRecordSize(100);//注释掉切割文件功能，达到最大记录数100,切割文件
fileOutputConfig.setMaxForceFileThreshold(60);//默认每5秒扫描，上次写入数据后，是否已经超过60秒没有新数据写入，如果没有并且缓存中有数据，则flush数据，否则不做处理
```

Excel输出插件不支持仅flush功能，如果设置了**maxForceFileThreshold**时，必须设置MaxFileRecordSize，进行文件切割。

### 2.3.4 参考文档

https://esdoc.bbossgroups.com/#/elasticsearch-sftp

## 2.4 Excel文件输出插件

Excel文件输出插件配置类：[ExcelFileOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/output/ExcelFileOutputConfig.java)，FileOutputConfig的子类，继承其所有特性，增加excel单元格和源记录字段映射关系、输出excel标题、sheetname等配置

### 2.4.1 导出本地excel文件

```java
ExcelFileOutputConfig fileOupputConfig = new ExcelFileOutputConfig();
        fileOupputConfig.setTitle("师大2021年新生医保（2021年）申报名单");//excel标题
        fileOupputConfig.setSheetName("2021年新生医保申报单");

        fileOupputConfig.addCellMapping(0,"shebao_org","社保经办机构（建议填写）")
                .addCellMapping(1,"person_no","人员编号")
                .addCellMapping(2,"name","*姓名")
                .addCellMapping(3,"cert_type","*证件类型")

                .addCellMapping(4,"cert_no","*证件号码","")
                .addCellMapping(5,"zhs_item","*征收项目")

                .addCellMapping(6,"zhs_class","*征收品目")
                .addCellMapping(7,"zhs_sub_class","征收子目")
                .addCellMapping(8,"zhs_year","*缴费年度","2022")
                .addCellMapping(9,"zhs_level","*缴费档次","1");
        fileOupputConfig.setFileDir("D:\\excelfiles\\hebin");//数据生成目录

        fileOupputConfig.setFilenameGenerator(new FilenameGenerator() {
            @Override
            public String genName(TaskContext taskContext, int fileSeq) {


                return "师大2021年新生医保（2021年）申报名单-合并.xlsx";
            }
        });

        importBuilder.setOutputConfig(fileOupputConfig);
```

关键配置说明：

fileOupputConfig.addCellMapping(0,"shebao_org","社保经办机构（建议填写）")

addCellMapping方法参数：第一个参数为excel单元格编号，从0开始，第二个参数源字段名称，第三个参数对应单元格excel标题行名称

### 2.4.2 上传ftp配置

参考【[2.3.2 导出并上传ftp](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_232-%e5%af%bc%e5%87%ba%e5%b9%b6%e4%b8%8a%e4%bc%a0ftp)】

### 2.4.3 写入空闲时间阈值配置

参考章节：[2.3.3 写入空闲时间阈值配置](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_233-写入空闲时间阈值配置)

### 2.4.4 参考文档

https://esdoc.bbossgroups.com/#/elasticsearch-sftp

## 2.5 Kafka输出插件

Kafka输出插件配置类：[Kafka2OutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-kafka2x/src/main/java/org/frameworkset/tran/plugin/kafka/output/Kafka2OutputConfig.java)，可以配置kafka生产端参数，包括数据格式化生成器、kafka服务器地址、topic、消息序列化机制、消息发送统计监控配置等

### 2.5.1 插件配置案例

```java
ImportBuilder importBuilder = new ImportBuilder();
      importBuilder.setFetchSize(300);
      importBuilder.setLogsendTaskMetric(10000l);//可以设定打印日志最大时间间隔，当打印日志到日志文件或者控制台时，判断是否满足最大时间间隔，满足则输出，不满足则不输出日志
  
      // kafka服务器参数配置:具体配置项，可以参考kafka官方资料
      // kafka 2x 客户端参数项及说明类：org.apache.kafka.clients.consumer.ConsumerConfig
      Kafka2OutputConfig kafkaOutputConfig = new Kafka2OutputConfig();
      kafkaOutputConfig.setTopic("es2kafka");//设置kafka主题名称
      kafkaOutputConfig.addKafkaProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
      kafkaOutputConfig.addKafkaProperty("key.serializer","org.apache.kafka.common.serialization.LongSerializer");
      kafkaOutputConfig.addKafkaProperty("compression.type","gzip");
      kafkaOutputConfig.addKafkaProperty("bootstrap.servers","127.0.0.1:9092");
//    kafkaOutputConfig.addKafkaProperty("bootstrap.servers","127.0.0.1:9092");

      kafkaOutputConfig.addKafkaProperty("batch.size","10");
//    kafkaOutputConfig.addKafkaProperty("linger.ms","10000");
//    kafkaOutputConfig.addKafkaProperty("buffer.memory","10000");
      kafkaOutputConfig.setKafkaAsynSend(true);//异步发送
      kafkaOutputConfig.setEnableMetricsAgg(true);//启用发送统计数据聚合计算机制
      kafkaOutputConfig.setMetricsAggWindow(60);//设定聚合时间窗口，单位：秒
//指定文件中每条记录格式化生成器，不指定默认为json格式输出
      kafkaOutputConfig.setRecordGenerator(new RecordGenerator() {
         @Override
         public void buildRecord(TaskContext taskContext, CommonRecord record, Writer builder) throws IOException {
            //record.setRecordKey("xxxxxx"); //指定记录key

            //直接将记录按照json格式输出到文本文件中
            SerialUtil.normalObject2json(record.getDatas(),//获取记录中的字段数据并转换为json格式
                  builder);
//          String data = (String)taskContext.getTaskData("data");//从任务上下文中获取本次任务执行前设置时间戳
//
////          System.out.println(data);
//
//          /**
//           * 自定义格式输出数据到消息builder中
//           */
//          /**
//          Map<String,Object > datas = record.getDatas();
//          StringBuilder temp = new StringBuilder();
//          for(Map.Entry<String, Object> entry:datas.entrySet()){
//             if(temp.length() > 0)
//                temp.append(",").append(entry.getValue());
//             else
//                temp.append(entry.getValue());
//          }
//          builder.write(temp.toString());
//          */
//          //更据字段拆分多条记录
//          Map<String,Object > datas = record.getDatas();
//          Object value = datas.get("content");
//          String value_ = String.valueOf(value);
//          if(value_.startsWith("[") && value_.endsWith("]")) {
//             List<Map> list = SimpleStringUtil.json2ListObject(value_, Map.class);
//
//             for(int i = 0; i < list.size(); i ++){
//                Map data_ = list.get(i);
//                StringBuilder temp = new StringBuilder();
//                Iterator<Map.Entry> iterator = data_.entrySet().iterator();
//                while(iterator.hasNext()){
//                   Map.Entry entry = iterator.next();
//                   if (temp.length() > 0)
//                      temp.append(",").append(entry.getValue());
//                   else
//                      temp.append(entry.getValue());
//
//                }
//                if(i > 0)
//                   builder.write(TranUtil.lineSeparator);
//                builder.write(temp.toString());
//
//             }
//
//          }
//          else {
//             StringBuilder temp = new StringBuilder();
//             for(Map.Entry<String, Object> entry:datas.entrySet()){
//
//                   if (temp.length() > 0)
//                      temp.append(",").append(entry.getValue());
//                   else
//                      temp.append(entry.getValue());
//
//             }
//
//             builder.write(temp.toString());
//          }


         }
      });
      importBuilder.setOutputConfig(kafkaOutputConfig);
```

### 2.5.2 参考文档

https://esdoc.bbossgroups.com/#/es-kafka

## 2.6 Http输出插件

Http输出插件配置类：[HttpOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/http/output/HttpOutputConfig.java)，配置http输出服务器地址配置、服务器ip和端口清单配置、http ssl配置、http连接池配置(可以初始化多个)、http method、请求头（静态和动态）、输出dsl及dsl配置文件配置、认证和token配置、记录生成器配置等等

### 2.6.1 直接输出数据配置案例

```java
//http输出插件配置
      HttpOutputConfig httpOutputConfig = new HttpOutputConfig();
      //指定导入数据的dsl语句，必填项，可以设置自己的提取逻辑，
      // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：


      httpOutputConfig
            .setJson(false)
            .setServiceUrl("/httpservice/sendData.api")
            .setHttpMethod("post")
            .addHttpHeader("testHeader","xxxxx")
            .addDynamicHeader("Authorization", new DynamicHeader() {
               @Override
               public String getValue(String header, DynamicHeaderContext dynamicHeaderContext) throws Exception {
                  //判断服务token是否过期，如果过期则需要重新调用token服务申请token
                  TokenInfo tokenInfo = tokenManager.getTokenInfo();
                  String token = "Bearer " + tokenInfo.getAccess_token();//"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZWZhdWx0XzYxNTE4YjlmM2UyYmM3LjEzMDI5OTkxIiwiaWF0IjoxNjMyNzM0MTExLCJuYmYiOjE2MzI3MzQxMTEsImV4cCI6MTYzMjc0MTMxMSwiZGV2aWNlX2lkIjoiYXBwMDMwMDAwMDAwMDAwMSIsImFwcF9pZCI6ImFwcDAzIiwidXVpZCI6ImFkZmRhZmFkZmFkc2ZlMzQxMzJmZHNhZHNmYWRzZiIsInNlY3JldCI6ImFwcDAzMVEyVzNFd29ybGQxMzU3OVBhc3NBU0RGIiwiaXNzdWVfdGltZSI6MTYzMjczNDExMSwiand0X3NjZW5lIjoiZGVmYXVsdCJ9.mSl-JBUV7gTUapn9yV-VLfoU7dm-gxC7pON62DnD-9c";
                  return token;
               }
            })
//          .addTargetHttpPoolName("http.poolNames","datatran,jwtservice")//初始化多个http服务集群时，就不要用addTargetHttpPoolName方法，使用以下方法即可
            .setTargetHttpPool("datatran")
            .addHttpOutputConfig("http.poolNames","datatran,jwtservice")
//          .addHttpOutputConfig("datatran.http.health","/health")//服务监控检查地址
            .addHttpOutputConfig("datatran.http.hosts","192.168.137.1:808")//服务地址清单，多个用逗号分隔
            .addHttpOutputConfig("datatran.http.timeoutConnection","5000")
            .addHttpOutputConfig("datatran.http.timeoutSocket","50000")
            .addHttpOutputConfig("datatran.http.connectionRequestTimeout","50000")
            .addHttpOutputConfig("datatran.http.maxTotal","200")
            .addHttpOutputConfig("datatran.http.defaultMaxPerRoute","100")
            .addHttpOutputConfig("datatran.http.failAllContinue","true")
            //设置token申请和更新服务配置jwtservice，在TokenManager中使用jwtservice申请和更新token
//          .addHttpOutputConfig("jwtservice.http.health","/health") //服务监控检查地址
            .addHttpOutputConfig("jwtservice.http.hosts","192.168.137.1:808,192.168.0.100:9501") //服务地址清单，多个用逗号分隔，192.168.0.100:9501
            .addHttpOutputConfig("jwtservice.http.timeoutConnection","5000")
            .addHttpOutputConfig("jwtservice.http.timeoutSocket","50000")
            .addHttpOutputConfig("jwtservice.http.connectionRequestTimeout","50000")
            .addHttpOutputConfig("jwtservice.http.maxTotal","200")
            .addHttpOutputConfig("jwtservice.http.defaultMaxPerRoute","100")
            .addHttpOutputConfig("jwtservice.http.failAllContinue","true")

      ;

      importBuilder.setOutputConfig(httpOutputConfig);
```

### 2.6.2 通过dsl输出数据配置案例

配置dslFile路径（相对于classpath）、dslName名称，数据key、dsl中使用的其他参数（静态和动态参数）等

```java
//http输出插件配置
      HttpOutputConfig httpOutputConfig = new HttpOutputConfig();
      //指定导入数据的dsl语句，必填项，可以设置自己的提取逻辑


      httpOutputConfig
            .setJson(true)
            .setShowDsl(true)
            .setDslFile("httpdsl.xml")
            .setDataDslName("sendData")
            .setDataKey("httpDatas")
            .setServiceUrl("/httpservice/sendData.api")
            .setHttpMethod("post")
            .addHttpHeader("testHeader","xxxxx")
            .addDynamicHeader("Authorization", new DynamicHeader() {
               @Override
               public String getValue(String header, DynamicHeaderContext dynamicHeaderContext) throws Exception {
                  //判断服务token是否过期，如果过期则需要重新调用token服务申请token
                  TokenInfo tokenInfo = tokenManager.getTokenInfo();
                  String token = "Bearer " + tokenInfo.getAccess_token();//"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZWZhdWx0XzYxNTE4YjlmM2UyYmM3LjEzMDI5OTkxIiwiaWF0IjoxNjMyNzM0MTExLCJuYmYiOjE2MzI3MzQxMTEsImV4cCI6MTYzMjc0MTMxMSwiZGV2aWNlX2lkIjoiYXBwMDMwMDAwMDAwMDAwMSIsImFwcF9pZCI6ImFwcDAzIiwidXVpZCI6ImFkZmRhZmFkZmFkc2ZlMzQxMzJmZHNhZHNmYWRzZiIsInNlY3JldCI6ImFwcDAzMVEyVzNFd29ybGQxMzU3OVBhc3NBU0RGIiwiaXNzdWVfdGltZSI6MTYzMjczNDExMSwiand0X3NjZW5lIjoiZGVmYXVsdCJ9.mSl-JBUV7gTUapn9yV-VLfoU7dm-gxC7pON62DnD-9c";
                  return token;
               }
            })
//          .addTargetHttpPoolName("http.poolNames","datatran,jwtservice")//初始化多个http服务集群时，就不要用addTargetHttpPoolName方法，使用以下方法即可
            .setTargetHttpPool("datatran")
            .addHttpOutputConfig("http.poolNames","datatran,jwtservice")
//          .addHttpOutputConfig("datatran.http.health","/health")//服务监控检查地址
            .addHttpOutputConfig("datatran.http.hosts","192.168.137.1:808")//服务地址清单，多个用逗号分隔
            .addHttpOutputConfig("datatran.http.timeoutConnection","5000")
            .addHttpOutputConfig("datatran.http.timeoutSocket","50000")
            .addHttpOutputConfig("datatran.http.connectionRequestTimeout","50000")
            .addHttpOutputConfig("datatran.http.maxTotal","200")
            .addHttpOutputConfig("datatran.http.defaultMaxPerRoute","100")
            .addHttpOutputConfig("datatran.http.failAllContinue","true")
            //设置token申请和更新服务配置jwtservice，在TokenManager中使用jwtservice申请和更新token
//          .addHttpOutputConfig("jwtservice.http.health","/health") //服务监控检查地址
            .addHttpOutputConfig("jwtservice.http.hosts","192.168.137.1:808") //服务地址清单，多个用逗号分隔，192.168.0.100:9501
            .addHttpOutputConfig("jwtservice.http.timeoutConnection","5000")
            .addHttpOutputConfig("jwtservice.http.timeoutSocket","50000")
            .addHttpOutputConfig("jwtservice.http.connectionRequestTimeout","50000")
            .addHttpOutputConfig("jwtservice.http.maxTotal","200")
            .addHttpOutputConfig("jwtservice.http.defaultMaxPerRoute","100")
            .addHttpOutputConfig("jwtservice.http.failAllContinue","true")

      ;

      importBuilder.addJobOutputParam("device_id","app03001")
                .addJobOutputParam("app_id","app03")
                .addJobDynamicOutputParam("signature", new DynamicParam() {//根据数据动态生成签名参数
                   @Override
                   public Object getValue(String paramName, DynamicParamContext dynamicParamContext) {
                       String datas = (String) dynamicParamContext.getDatas();
                       //可以根据自己的算法对数据进行签名
                       String signature = "1b3bb71f6ebae2f52b7a238c589f3ff9";//signature =md5(datas)
                      return signature;
                   }
                });
      importBuilder.setOutputConfig(httpOutputConfig);
```

dsl文件和dsl配置sendData：

```xml
<property name="sendData">
    <![CDATA[
    {
        "device_id": #[device_id], ## device_id,通过addJobInputParam赋值
        "app_id": #[app_id], ## app_id,通过addJobInputParam赋值
        "datas":  #[httpDatas,quoted=false,escape=false], ## datas,发送的数据源
        "signature": #[signature]
    }
    ]]></property>
```

### 2.6.3 自定义数据记录输出格式

参考文档：[自定义数据记录输出格式](https://esdoc.bbossgroups.com/#/datatran-http?id=_6自定义数据记录输出格式)

### 2.6.4 参考文档

[http输出插件](https://esdoc.bbossgroups.com/#/datatran-http?id=_3http输出插件)

## 2.7 自定义输出插件

自定义输出插件配置：[CustomOupputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/custom/output/CustomOupputConfig.java)，顾名思义，自定义数据输出功能，自行实现数据输出到各种存储介质、或者推送到各种消息中间件等，结合CustomOutPut接口实现数据自定义处理：

```java
org.frameworkset.tran.plugin.custom.output.CustomOutPut
public void handleData(TaskContext taskContext,List<CommonRecord> datas);
```

### 2.7.1 配置案例

简单示例

```java
 //自己处理数据，可以咨询实现
        CustomOutputConfig customOutputConfig = new CustomOutputConfig();
        customOutputConfig.setCustomOutPut(new CustomOutPut() {
            @Override
            public void handleData(TaskContext taskContext, List<CommonRecord> datas) {

                //You can do any thing here for datas
                for(CommonRecord record:datas){
                    Map<String,Object> data = record.getDatas();//获取原始数据记录，key/vlaue，代表字段和值
                    int action = (int)record.getMetaValue("action");//获取元数据,记录类型，可以是新增（默认类型）/修改/删除/其他类型
                    String table = (String)record.getMetaValue("table");//获取元数据中的表名称
                    logger.info("data:{},action:{},record action type:insert={},update={},delete={}",data,action,record.isInsert(),record.isUpdate(),record.isDelete());

                    logger.info(SimpleStringUtil.object2json(record.getMetaDatas()));//获取并打印所有元数据信息
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
                String table = (String)context.getMetaValue("table");
//                int action1 = (int)context.getMetaValue("action1");
            }
        });
        importBuilder.setOutputConfig(customOutputConfig);
```

将数据同时写入两个redis集群：

https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/Elasticsearch2CustomRedisDemo.java

```java
//自己处理数据
      CustomOutputConfig customOutputConfig = new CustomOutputConfig();
      customOutputConfig.setCustomOutPut(new CustomOutPut() {
         @Override
         public void handleData(TaskContext taskContext, List<CommonRecord> datas) {

            //You can do any thing here for datas
            //同时将数据写入两个redis集群：单笔记录处理
            RedisHelper redisHelper = null;
            RedisHelper redisHelper1 = null;
            try {
               redisHelper = RedisFactory.getRedisHelper();//第一个集群
               redisHelper1 = RedisFactory.getRedisHelper("redis1");//第二个集群

               for (CommonRecord record : datas) {
                  Map<String, Object> data = record.getDatas();
                  String LOG_ID =String.valueOf(data.get("LOG_ID"));
//             logger.info(SimpleStringUtil.object2json(data));
                  String valuedata = SimpleStringUtil.object2json(data);
                  logger.debug("LOG_ID:{}",LOG_ID);
//             logger.info(SimpleStringUtil.object2json(data));
                  redisHelper.hset("xingchenma", LOG_ID, valuedata);//将数据写入第一个集群
                  redisHelper1.hset("xingchenma", LOG_ID, valuedata);//将数据写入第一个集群
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
      importBuilder.setOutputConfig(customOutputConfig);
```

## 2.8 MongoDB输出插件

MongoDB输出插件配置类：[MongoDBOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-mongodb/src/main/java/org/frameworkset/tran/plugin/mongodb/output/MongoDBOutputConfig.java)，配置mongodb数据库参数、数据库表、各种连接控制参数/服务器地址等；支持新增、修改、替换和删除记录同步，支持多表、多库、多数据源数据同步 

通过文档id字段实现记录的新增、修改、替换和删除操作同步。

### 2.8.1 简单配置案例

数据库连接地址、连接参数、安全认证机制配置

```java
// 5.2.4.1 设置mongodb参数
		MongoDBOutputConfig mongoDBOutputConfig = new MongoDBOutputConfig();
		mongoDBOutputConfig.setName("testes2mg")//mongodb数据源名称
				.setDb("testdb")
				.setDbCollection("db2mongodemo")
				.setConnectTimeout(10000)
				.setWriteConcern("JOURNAL_SAFE")

				.setMaxWaitTime(10000)
				.setSocketTimeout(1500).setSocketKeepAlive(true)
				.setConnectionsPerHost(100)
				
				.setServerAddresses("127.0.0.1:27017")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
				// mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
				//String database,String userName,String password,String mechanism
				//https://www.iteye.com/blog/yin-bp-2064662

				.setUserName("bboss")
		        .setPassword("bboss")
		        .setMechanism("MONGODB-CR")
		        .setAuthDb("sessiondb")
				;

		importBuilder.setOutputConfig(mongoDBOutputConfig);
```

### 2.8.2 多表输出配置案例

默认在MongoDBOutputConfig配置MongoDB的目标数据库、目标表、目标数据源：

```java
	mongoDBOutputConfig.setName("testes2mg")//mongodb目标数据源名称
			.setDb("testdb")//目标库
			.setDbCollection("db2mongodemo");//目标表
```
可以在datarefactor接口中为记录设置不同的输入数据源、目标库和目标表：

```java
String table = (String)context.getMetaValue("table");//记录来源collection，默认输出表
String database = (String)context.getMetaValue("database");//记录来源db
TableMapping tableMapping = new TableMapping();
tableMapping.setTargetDatabase("testdb");//目标库
tableMapping.setTargetCollection("testcdc");//目标表
tableMapping.setTargetDatasource("testes2mg");//指定MongoDB数据源名称，对应一个MongoDB集群

context.setTableMapping(tableMapping);
```

### 2.8.3 设置文档_id字段

默认采用记录中的\_id字段值作为MongoDB文档id标识，亦可以通过MongoDBOutputConfig全局设置输出记录的id对应的字段名称：

```java
mongoDBOutputConfig.setObjectIdField("sessionid");
```

亦可以在datarefactor接口中为记录设置id对应的字段名称：

```java
context.setRecordKeyField("_id");//记录级别指定文档_id值对应的字段
```

### 2.8.3 MongoDB数据源定义和关闭

如何自定义和关闭MongoDB数据源，参考文档：[MongoDB数据源定义和使用](https://esdoc.bbossgroups.com/#/MongoDBDatasource)

### 2.8.4 更多案例

https://gitee.com/bboss/mongodb-elasticsearch



## 2.9 HBase输出插件

HBase输出插件配置类：[HBaseOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-hbase/src/main/java/org/frameworkset/tran/plugin/hbase/output/HBaseOutputConfig.java)，配置hbase服务器连接参数/服务器地址/表名称/表列簇名称/列影射配置等参数

### 2.9.1 基础配置案例

```java
// 5.2.4.1 设置hbase参数
HBaseOutputConfig hBaseOutputConfig = new HBaseOutputConfig();
hBaseOutputConfig.setName("targethbase");//hbase数据源名称
hBaseOutputConfig.setFamiliy("info")//指定需要同步数据的hbase表列簇名称;
              .setHbaseTable("demo") ;//指定需要同步数据的hbase表名称;
hBaseOutputConfig.setRowKeyField("LOG_ID")
      .addHbaseClientProperty("hbase.zookeeper.quorum","192.168.137.133")  //hbase客户端连接参数设置，参数含义参考hbase官方客户端文档
      .addHbaseClientProperty("hbase.zookeeper.property.clientPort","2183")
      .addHbaseClientProperty("zookeeper.znode.parent","/hbase")
      .addHbaseClientProperty("hbase.ipc.client.tcpnodelay","true")
      .addHbaseClientProperty("hbase.rpc.timeout","10000")
      .addHbaseClientProperty("hbase.client.operation.timeout","10000")
      .addHbaseClientProperty("hbase.ipc.client.socket.timeout.read","20000")
      .addHbaseClientProperty("hbase.ipc.client.socket.timeout.write","30000")
      .addHbaseClientProperty("hbase.client.async.enable","true")
      .addHbaseClientProperty("hbase.client.async.in.queuesize","10000")
      .setHbaseClientThreadCount(100)  //hbase客户端连接线程池参数设置
      .setHbaseClientThreadQueue(100)
      .setHbaseClientKeepAliveTime(10000l)
      .setHbaseClientBlockedWaitTimeout(10000l)
      .setHbaseClientWarnMultsRejects(1000)
      .setHbaseClientPreStartAllCoreThreads(true)
      .setHbaseClientThreadDaemon(true);

importBuilder.setOutputConfig(hBaseOutputConfig);
```

### 2.9.2 列簇映射配置

```java
// 5.2.4.1 设置hbase参数
HBaseOutputConfig hBaseOutputConfig = new HBaseOutputConfig();
hBaseOutputConfig.setName("targethbase");//hbase数据源名称
hBaseOutputConfig.setHbaseTable("customdemo") ;//指定需要同步数据的hbase表名称;
hBaseOutputConfig.addFamilyColumnMapping("info","LOG_ID","logId");//列簇及其中列映射关系
hBaseOutputConfig.addFamilyColumnMapping("info","LOG_OPERUSER","logOperUser");
hBaseOutputConfig.addFamilyColumnMapping("info","OPER_MODULE","operModule");
hBaseOutputConfig.addFamilyColumnMapping("info","testint");
hBaseOutputConfig.setRowKeyField("LOG_ID")
      .addHbaseClientProperty("hbase.zookeeper.quorum","192.168.137.133")  //hbase客户端连接参数设置，参数含义参考hbase官方客户端文档
      .addHbaseClientProperty("hbase.zookeeper.property.clientPort","2183")
      .addHbaseClientProperty("zookeeper.znode.parent","/hbase")
      .addHbaseClientProperty("hbase.ipc.client.tcpnodelay","true")
      .addHbaseClientProperty("hbase.rpc.timeout","10000")
      .addHbaseClientProperty("hbase.client.operation.timeout","10000")
      .addHbaseClientProperty("hbase.ipc.client.socket.timeout.read","20000")
      .addHbaseClientProperty("hbase.ipc.client.socket.timeout.write","30000")
      .addHbaseClientProperty("hbase.client.async.enable","true")
      .addHbaseClientProperty("hbase.client.async.in.queuesize","10000")
      .setHbaseClientThreadCount(100)  //hbase客户端连接线程池参数设置
      .setHbaseClientThreadQueue(100)
      .setHbaseClientKeepAliveTime(10000l)
      .setHbaseClientBlockedWaitTimeout(10000l)
      .setHbaseClientWarnMultsRejects(1000)
      .setHbaseClientPreStartAllCoreThreads(true)
      .setHbaseClientThreadDaemon(true);

importBuilder.setOutputConfig(hBaseOutputConfig);
```

映射列簇-源字段-列名称关系方法说明：

```java
/**
 * 指定自定义列簇与源字段映射关系，只有映射过的源字段值才会保存到hbase表
 * 如果没有指定自定义列簇映射关系将采用全局列簇名称
 * @param family hbase列簇名称
 * @param field 源字段名称，同时也是列的名称
 * @return
 */
public HBaseOutputConfig addFamilyColumnMapping(String family,String field){
   return addFamilyColumnMapping( family, field,field);
}

/**
 * 指定自定义列簇与源字段映射关系，只有映射过的源字段值才会保存到hbase表
 * 如果没有指定自定义列簇映射关系将采用全局列簇名称
 * @param family hbase列簇名称
 * @param field  源字段名称
 * @param column  hbase列名称 ，field对应的值作为列的值
 * @return
 */
public HBaseOutputConfig addFamilyColumnMapping(String family,String field,String column)
```

## 2.10 指标结果输出插件

指标结果输出插件配置类：[MetricsOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/metrics/output/MetricsOutputConfig.java)，指定指标计算器，对采集的数据进行流处理指标计算

### 2.10.1 配置案例

定义指标计算器 ：ETLMetrics和类型Metrics.MetricsType_KeyTimeMetircs

```java
ETLMetrics keyMetrics = new ETLMetrics(Metrics.MetricsType_KeyTimeMetircs){
   @Override
   public void builderMetrics(){
      //指标1 按操作模块统计模块操作次数
      addMetricBuilder(new MetricBuilder() {
         @Override
         public String buildMetricKey(MapData mapData){
                      CommonRecord data = (CommonRecord) mapData.getData();
                      String operModule = (String) data.getData("operModule");
                      if(operModule == null || operModule.equals("")){
                          operModule = "未知模块";
                      }
            return operModule;
         }
         @Override
         public KeyMetricBuilder metricBuilder(){
            return new KeyMetricBuilder() {
               @Override
               public KeyMetric build() {
                  return new LoginModuleMetric();
               }
            };
         }
      });

      //指标2 按照用户统计操作次数
      addMetricBuilder(new MetricBuilder() {
         @Override
         public String buildMetricKey(MapData mapData){
                      CommonRecord data = (CommonRecord) mapData.getData();
                      String logUser = (String) data.getData("logOperuser");//
                      if(logUser == null || logUser.equals("")){
                          logUser = "未知用户";
                      }
            return logUser;
         }
         @Override
         public KeyMetricBuilder metricBuilder(){
            return new KeyMetricBuilder() {
               @Override
               public KeyMetric build() {
                  return new LoginUserMetric();
               }
            };
         }
      });
      // key metrics中包含两个segment(S0,S1)
      setSegmentBoundSize(5000000);
      setTimeWindows(60 );//统计时间窗口
              this.setTimeWindowType(MetricsConfig.TIME_WINDOW_TYPE_MINUTE);
   }

          /**
           * 存储指标计算结果
           * @param metrics
           */
   @Override
   public void persistent(Collection< KeyMetric> metrics) {
      metrics.forEach(keyMetric->{
         if(keyMetric instanceof LoginModuleMetric) {
                      LoginModuleMetric testKeyMetric = (LoginModuleMetric) keyMetric;
            Map esData = new HashMap();
            esData.put("dataTime", testKeyMetric.getDataTime());
            esData.put("hour", testKeyMetric.getDayHour());
            esData.put("minute", testKeyMetric.getMinute());
            esData.put("day", testKeyMetric.getDay());
            esData.put("metric", testKeyMetric.getMetric());
            esData.put("operModule", testKeyMetric.getOperModule());
            esData.put("count", testKeyMetric.getCount());
            bulkProcessor.insertData("vops-loginmodulemetrics", esData);
         }
         else if(keyMetric instanceof LoginUserMetric) {
                      LoginUserMetric testKeyMetric = (LoginUserMetric) keyMetric;
            Map esData = new HashMap();
            esData.put("dataTime", testKeyMetric.getDataTime());
            esData.put("hour", testKeyMetric.getDayHour());
            esData.put("minute", testKeyMetric.getMinute());
            esData.put("day", testKeyMetric.getDay());
            esData.put("metric", testKeyMetric.getMetric());
            esData.put("logUser", testKeyMetric.getLogUser());
            esData.put("count", testKeyMetric.getCount());
            bulkProcessor.insertData("vops-loginusermetrics", esData);
         }

      });

   }
};
```

### 2.10.2 关键配置说明

#### 2.10.2.1 指标计算定义

实现builderMetrics方法，通过MetricBuilder添加多个指标，每个指标包括指标metricKey和指标计算器、计算控制参数，指标key是由维度字段组合的形成指标的唯一键值，指标计算器包含指标维度字段和指标计算逻辑。

通过buildMetricKey方法生成指标唯一键值key：这里的键值比较简单，只一由个字段logUser构成

```java
 @Override
         public String buildMetricKey(MapData mapData){
                      CommonRecord data = (CommonRecord) mapData.getData();
                      String logUser = (String) data.getData("logOperuser");//
                      if(logUser == null || logUser.equals("")){
                          logUser = "未知用户";
                      }
            return logUser;
         }
```

指标计算器：LoginUserMetric

```java
 public KeyMetricBuilder metricBuilder(){
            return new KeyMetricBuilder() {
               @Override
               public KeyMetric build() {
                  return new LoginUserMetric();
               }
            };
         }
```

LoginUserMetric实现如下：必须在指标计算器中定义各个维度字段（logUser，这里只有一个字段），并通过init对初始化维度字段的值

```java
public class LoginUserMetric extends TimeMetric {
    private String logUser;
    @Override
    public void init(MapData firstData) {
        CommonRecord data = (CommonRecord) firstData.getData();
        logUser = (String) data.getData("logOperuser");
        if(logUser == null || logUser.equals("")){
            logUser = "未知用户";
        }
    }

    @Override
    public void incr(MapData data) {
        count ++;//指标计算
    }

    public String getLogUser() {
        return logUser;//返回维度字段
    }
}
```

#### 2.10.2.2 计算控制参数

```java
// key metrics中包含两个segment(S0,S1)
      setSegmentBoundSize(5000000);
      setTimeWindows(60 );//统计时间窗口
              this.setTimeWindowType(MetricsConfig.TIME_WINDOW_TYPE_MINUTE);
```

#### 2.10.2.3 指标存储定义

```java
  /**
           * 存储指标计算结果
           * @param metrics
           */
   @Override
   public void persistent(Collection< KeyMetric> metrics) {
      metrics.forEach(keyMetric->{
         if(keyMetric instanceof LoginModuleMetric) {
                      LoginModuleMetric testKeyMetric = (LoginModuleMetric) keyMetric;
            Map esData = new HashMap();
            esData.put("dataTime", testKeyMetric.getDataTime());
            esData.put("hour", testKeyMetric.getDayHour());
            esData.put("minute", testKeyMetric.getMinute());
            esData.put("day", testKeyMetric.getDay());
            esData.put("metric", testKeyMetric.getMetric());
            esData.put("operModule", testKeyMetric.getOperModule());
            esData.put("count", testKeyMetric.getCount());
            bulkProcessor.insertData("vops-loginmodulemetrics", esData);
         }
         else if(keyMetric instanceof LoginUserMetric) {
                      LoginUserMetric testKeyMetric = (LoginUserMetric) keyMetric;
            Map esData = new HashMap();
            esData.put("dataTime", testKeyMetric.getDataTime());
            esData.put("hour", testKeyMetric.getDayHour());
            esData.put("minute", testKeyMetric.getMinute());
            esData.put("day", testKeyMetric.getDay());
            esData.put("metric", testKeyMetric.getMetric());
            esData.put("logUser", testKeyMetric.getLogUser());
            esData.put("count", testKeyMetric.getCount());
            bulkProcessor.insertData("vops-loginusermetrics", esData);
         }

      });

   }
```

通过persistent方法，将达到时间窗口的的指标存储到指定的存储介质，这里通过异步批处理组件：

bulkProcessor，将指标计算结果保存到对应的Elasticsearch索引表中，亦可以根据实际需要保存到其它地方

### 2.10.3 设置指标输出插件

将指标计算器添加到指标输出插件即可

```java
 MetricsOutputConfig metricsOutputConfig = new MetricsOutputConfig();

      metricsOutputConfig.setDataTimeField("logOpertime");//指定指标时间维度字段
      metricsOutputConfig.addMetrics(keyMetrics);//添加指标计算器,可以添加多个指标计算器

importBuilder.setOutputConfig(metricsOutputConfig);
```

### 2.10.4 参考文档

https://esdoc.bbossgroups.com/#/etl-metrics

## 2.11 Dummy输出插件

Dummy输出插件:[DummyOutputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-core/src/main/java/org/frameworkset/tran/plugin/dummy/output/DummyOutputConfig.java),本插件只做一个简单的事情，将数据输出到控制台或者日志文件,可以定义输出数据格式，用来辅助调试其他输入插件功能

### 2.11.1 配置案例

```java
DummyOutputConfig dummyOupputConfig = new DummyOutputConfig();
dummyOupputConfig.setRecordGenerator(new RecordGenerator() {
   @Override
   public void buildRecord(TaskContext taskContext, CommonRecord record, Writer builder) throws Exception{
      SimpleStringUtil.object2json(record.getDatas(),builder);//自定义数据输出格式

   }
}).setPrintRecord(true);
importBuilder.setOutputConfig(dummyOupputConfig);
```

# 3.参考文档

[bboss数据采集ETL工具使用指南](https://esdoc.bbossgroups.com/#/db-es-tool?id=bboss数据采集etl工具使用指南)

[数据采集&流批一体化处理使用指南](https://esdoc.bbossgroups.com/#/etl-metrics?id=数据采集amp流批一体化处理使用指南)


