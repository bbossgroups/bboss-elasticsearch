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

配置Mysql binlog对应的mysql master slave ip和端口、数据库账号和口令、监听的数据库表以及binlog文件路径等，本插件支持直接监听mysql master slave ip和端口和读取binlog文件两种模式采集mysql增删改数据

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
                    Map<String,Object> data = record.getDatas();
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

扫码免费观看mysql binlog插件使用视频教程：介绍采集作业开发、调测、构建配置部署实际操作过程

<img src="images\mysql-binlog-vidio.png" style="zoom:50%;" />

### 1.3.3 输出到数据库案例

详见章节：[输出到数据库案例](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_212-mysql-binlog%e7%9b%91%e5%90%ac%e5%a4%9a%e8%a1%a8%e5%9c%ba%e6%99%af)

## 1.4 文件采集插件

[FileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/FileInputConfig.java)

内容补充中。。。。。。

## 1.5 Excel文件采集插件

[ExcelFileInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-fileftp/src/main/java/org/frameworkset/tran/plugin/file/input/ExcelFileInputConfig.java)

内容补充中。。。。。。

## 1.6 HBase采集插件

[HBaseInputConfig](https://gitee.com/bboss/bboss-elastic-tran/blob/master/bboss-datatran-hbase/src/main/java/org/frameworkset/tran/plugin/hbase/input/HBaseInputConfig.java)

内容补充中....

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