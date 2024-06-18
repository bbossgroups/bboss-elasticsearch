# **Mysql binlog输入插件使用指南**

# 1.概述

bboss-datatran采用标准的输入输出异步管道来处理数据，输入插件和输出插件可以自由组合，输入插件从数据源采集数据，经过数据异步并行流批一体化处理后，输出插件将处理后的数据、指标数据输出到目标地。

<img src="images\datasyn-inout-now.png" alt="图片" style="zoom:75%;" />

<img src="images\datasyn.png" alt="图片" style="zoom:75%;" />

bboss插件参考文档：

https://esdoc.bbossgroups.com/#/datatran-plugins

## 1.1 工作机制

bboss mysql binlog数据采集插件原理图如下：

<img src="images\mysql-binlog-arch.png" alt="图片" style="zoom:75%;" />

## 1.2 同步模式

Mysql binlog插件通过配置对应的mysql master ip和端口、数据库账号和口令、监听的数据库表以及binlog文件路径等信息，非常方便地实现：

1）Mysql增删改数据实时采集同步，源库到多个目标库数据同步

2）Mysql数据库ddl操作实时同步，源库到多个目标库ddl同步

Mysql binlog插件支持以下三种数据采集模式：

**模式1** 直接读取binlog文件,采集文件中的增删改数据

**模式2** 监听mysql master slave ip和端口，作业重启从binlog最新位置采集数据

**模式3** 监听mysql master slave ip和端口，启用故障容灾配置，每次重启作业从上次采集结束的位置开始采集数据

模式1适用一次性离线数据采集场景，模式2和模式3适用于实时采集场景。源表本来就有数据需要同步+实时同步,原来的数据可以基于模式1采集binlog文件，如果没有binlog文件，可以直接用数据库输入插件，直接一次性采集全表数据，然后再用模式3实现增量采集。

## 1.3 同步案例

本文通过两个案例来讲解介绍mysql binlog插件的使用方法：

1）实时同步Mysql Binlog增删改数据到Elasticsearch作为案例

2）多库多表数据同步到多目标库案例

## 1.4 注意事项

通过mysql binlog插件同步插入、修改和删除数据时，目标表需要避免使用自增主键字段

## 1.5 视频教程

[实时采集Mysql binlog增删改数据教程（db-db单表多表）](https://www.bilibili.com/video/BV1ko4y1M7My)

[实战：基于bboss cdc实时同步mysql增删改数据到Elasticsearch](https://www.bilibili.com/video/BV1aW4y1f73c)



# 2.Mysql增删改数据同步到Elasticsearch

## **2.1 binlog输入插件配置**

### 2.1.1 模式1案例

模式1 直接读取binlog文件,采集文件中的增删改数据

```java

 ImportBuilder importBuilder = new ImportBuilder();
        importBuilder.setBatchSize(1000);//设置批量入Elasticsearch库的记录数
        //binlog插件配置开始
        MySQLBinlogConfig mySQLBinlogConfig = new MySQLBinlogConfig();
        mySQLBinlogConfig.setHost("192.168.137.1");
        mySQLBinlogConfig.setPort(3306);
        mySQLBinlogConfig.setDbUser("root");
        mySQLBinlogConfig.setDbPassword("123456");
        //如果直接监听文件则设置binlog文件路径，否则不需要配置文件路径
        mySQLBinlogConfig.setFileNames("F:\\6_environment\\mysql\\binlog.000107,F:\\6_environment\\mysql\\binlog.000127");
        mySQLBinlogConfig.setTables("cityperson");//监控增量表名称，多个表以逗号分隔：cityperson,batchest
        mySQLBinlogConfig.setDatabase("bboss");//监控数据库名称,多个库以逗号分隔：bboss,pinpoint
       
        //binlog插件配置结束
        importBuilder.setInputConfig(mySQLBinlogConfig);
```

### 2.1.2 模式2案例

**模式2** 监听mysql master slave ip和端口，作业重启从binlog最新位置采集删改数据

```java
//binlog插件配置开始
MySQLBinlogConfig mySQLBinlogConfig = new MySQLBinlogConfig();
mySQLBinlogConfig.setHost("192.168.137.1");
mySQLBinlogConfig.setPort(3306);
mySQLBinlogConfig.setDbUser("root");
mySQLBinlogConfig.setDbPassword("123456");

mySQLBinlogConfig.setTables("cityperson");//监控增量表名称
mySQLBinlogConfig.setDatabase("bboss");//监控数据库名称
mySQLBinlogConfig.setServerId(65536L);//模拟slave节点ID
//binlog插件配置结束
importBuilder.setInputConfig(mySQLBinlogConfig);
```

### 2.1.3 **模式3**案例

监听mysql master slave ip和端口，启用故障容灾配置，每次重启作业从上次采集结束的位置开始采集数据

```java
        MySQLBinlogConfig mySQLBinlogConfig = new MySQLBinlogConfig();
        mySQLBinlogConfig.setHost("192.168.137.1");
        mySQLBinlogConfig.setPort(3306);
        mySQLBinlogConfig.setDbUser("root");
        mySQLBinlogConfig.setDbPassword("123456");
        mySQLBinlogConfig.setServerId(100000L);
        mySQLBinlogConfig.setTables("cityperson,batchtest");//
        mySQLBinlogConfig.setDatabase("bboss");
        mySQLBinlogConfig.setEnableIncrement(true);//启用模式3
       // mysql binlog插件增加异步启动机制，JoinToConnectTimeOut大于0生效，否则是同步启动，启用方法：
		mySQLBinlogConfig.setJoinToConnectTimeOut(20000L);
        importBuilder.setInputConfig(mySQLBinlogConfig);
        importBuilder.setPrintTaskLog(true);
		int batchSize = 500;//批量入库记录数
       
        importBuilder.setBatchSize(batchSize);//设置批量入库的记录数
        importBuilder.setFlushInterval(10000L);//如果10秒内没有达到500条数据，但是有数据，则强制输出数据
		//启用模式3 故障容灾机制配置       
//        importBuilder.setStatusDbname("testStatus");//指定增量状态数据源名称
      importBuilder.setLastValueStorePath("binlog2db_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
        importBuilder.setLastValueStoreTableName("binlog");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
```

通过setEnableIncrement方法启用模式3：

mySQLBinlogConfig.setEnableIncrement(true);//启用模式3

## 2.2 Elasticsearch输出插件配置

通过ElasticsearchOutputConfig 配置Elasticsearch服务器地址及连接参数、索引表、文档Id字段等信息

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
                .setIndex("binlogdemo") //设置全局索引表
                .setEsIdField("rowNo");//设置文档主键，不设置，则自动产生文档id,设置好id后，binlog采集的增删改数据，会自动同步到Elasticsearch
        importBuilder.setOutputConfig(elasticsearchOutputConfig);
```

设置索引表时，可以直接指定索引名称，也可以指定按天分表的动态索引名称：直接指定索引名称

### **2.2.1 全局索引表配置**

elasticsearchOutputConfig.setIndex("binlogdemo") //设置索引表

按天动态分表索引名称

elasticsearchOutputConfig.setIndex("binlogdemo-{dateformat=yyyy.MM.dd}") //设置索引表，当前日期按天分表

elasticsearchOutputConfig.setIndex("binlogdemo-{field=agentStarttime,dateformat=yyyy.MM.dd}") //设置索引表，根据日期字段agentStarttime对应的日期按天分表

### **2.2.2 记录级别索引名称设置**

如果通过mysql binlog插件采集了多张表的数据，并且需要给每张表指定定义的索引名称，则通过以下方式进行配置：
```java

 importBuilder.setDataRefactor(new DataRefactor() {
            @Override
            public void refactor(Context context) throws Exception {
                //根据表名称指定不同的Elasticsearch索引表
                String table = (String)context.getMetaValue("table");
                if(table.equals("cityperson"))
                    context.setIndex("cityperson-{dateformat=yyyy.MM.dd}");
                else
                    context.setIndex("batchtest-{dateformat=yyyy.MM.dd}");

            }
        });
```



### **2.2.3 Elasticsearch文档Id设置**

设置Elasticsearch文档主键，不设置，则自动产生文档id,设置好id后，binlog采集的删除和修改数据，才会自动同步到Elasticsearch，设置方法如下：

elasticsearchOutputConfig.setEsIdField("rowNo");*//设置文档主键，不设置，则自动产生文档id,设置好id后，binlog采集的增删改数据，会自动同步到Elasticsearch*

## **2.3 文档数据加工和处理**

通过setDataRefactor接口来处理同步的数据记录

```java

 importBuilder.setDataRefactor(new DataRefactor() {
            @Override
            public void refactor(Context context) throws Exception {
                //根据表名称指定不同的Elasticsearch索引表
                String table = (String)context.getMetaValue("table");
                if(table.equals("cityperson"))
                    context.setIndex("cityperson-{dateformat=yyyy.MM.dd}");
                else
                    context.setIndex("batchtest-{dateformat=yyyy.MM.dd}");

            }
        });
```



可以直接参考以下文档章节：【2.8.10 数据加工处理】

https://esdoc.bbossgroups.com/#/db-es-tool

## **2.4 执行作业**

配置好输入输出插件后，通过importBuilder构建DataStream 对象，然后执行execute方法即可启动运行binlog数据采集作业

```java
DataStream dataStream = importBuilder.builder();       
dataStream.execute();
```

## **2.5 完整的作业源码**

源码工程地址：https://gitee.com/bboss/bboss-datatran-demo

案例作业代码文件：

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/binlog/Binlog2EleasticsearchOutput.java

更多案例，可以参考文档：

[mysql binlog数据采集案例](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo?id=_20-mysql-binlog%e6%95%b0%e6%8d%ae%e9%87%87%e9%9b%86%e6%a1%88%e4%be%8b)

# 3 多库多表数据同步到多目标库

通过mysql binlog插件与数据库输出插件相结合，可以非常方便地实现多库多表数据同步到多目标库。

## 3.1 表数据多对多同步

### 3.1.1 Mysql binlog输入插件配置

Mysql binlog插件通过以下方法来实现多库：

**1) 默认多表和多库配置**

通过mySQLBinlogConfig的setTables设置多个表名称，setDatabase设置多个数据库，例如：

```java
mySQLBinlogConfig.setTables("cityperson,batchest");//监控增量表名称，多个表以逗号分隔：
mySQLBinlogConfig.setDatabase("bboss,pinpoint");//监控数据库名称,多个库以逗号分隔：bboss,pinpoint
```

通过以上配置，实现以下数据库表的实时数据采集：

```java
bboss.cityperson
bboss.batchest
pinpoint.cityperson
pinpoint.batchest
```

**2) 表名称直接指定数据库名称**

通过mySQLBinlogConfig的setTables设置多个表名称，表前添加数据库名称，例如：

```java
mySQLBinlogConfig.setTables("bboss.cityperson,bboss.batchest,pinpoint.t1,pinpoint.t2");//监控增量表名称，多个表以逗号分隔
```

**3）混合模式配置**

1）和2）相结合配置多库多表

```java
mySQLBinlogConfig.setTables("bboss.cityperson,batchest,logtable,apm.agent,pinpoint.cityperson");//监控增量表名称，多个表以逗号分隔：
mySQLBinlogConfig.setDatabase("terminal,ecs,bboss");//监控数据库名称,多个库以逗号分隔：bboss,pinpoint
```

通过以上配置，实现以下数据库表的实时数据采集：

```
bboss.cityperson
terminal.batchest
terminal.logtable
ecs.batchest
ecs.logtable
bboss.batchest
bboss.logtable
apm.agent
pinpoint.cityperson
```

setTables方法中batchest,logtable两张表没有指定数据库名称，同时通过setDatabase设置了三个数据库:

```java
terminal,ecs,bboss
```

以上配置除了采集明确配置了数据库的表数据：bboss.cityperson，apm.agent,pinpoint.cityperson

还会采集terminal,ecs,bboss三个数据库中两张表数据：batchest,logtable

```
terminal.batchest
terminal.logtable
ecs.batchest
ecs.logtable
bboss.batchest
bboss.logtable
```

**4) 数据库及表名称解析器配置**

通过数据库及表名称解析器SqlConfResolver接口的实现类DatabaseTableSqlConfResolver来配置目标库表配置查找规则：

```java
dbOutputConfig.setSqlConfResolver(new DatabaseTableSqlConfResolver());
```

### 3.1.2 数据库输出插件配置

需要借助数据库输出插件来实现将mysql binlog插件采集的数据同步到目标数据库，需要进行以下配置

1）通过SQLConf对象的setTargetDbName方法来设置数据库表对应的目标数据源清单，如果不设置数据源，则默认采用dbOutputConfig.setDbName设置的数据源名称；

2）通过SQLConf的setInsertSqlName设置目标表的数据insert语句，直接指定sql配置文件中的sql配置名称即可

3）通过SQLConf的setUpdateSqlName设置目标表的数据update语句，直接指定sql配置文件中的sql配置名称即可

4）通过SQLConf的setDeleteSqlName设置目标表的数据delete语句，直接指定sql配置文件中的sql配置名称即可

5）通过dbOutputConfig.addSQLConf方法添加表的增删改数据同步sql和目标数据源到作业配置中，有多少张表数据需要同步则添加多少次即可

增删改sql语句可以根据实际需要选择配置即可，可以全部配置也可以配置其中的一条或者2条sql。

addSQLConf参数说明：

dbOutputConfig.addSQLConf("bboss.cityperson",sqlConf);//数据库加表名称保存sql配置：

第一个参数：bboss.cityperson，源库名称.表名称，用来保存对应库表的同步sql配置

6）通过数据库及表名称解析器SqlConfResolver接口的实现类DatabaseTableSqlConfResolver来配置目标库表配置查找规则

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
dbOutputConfig.setSqlFilepath("dsl2ndSqlFile.xml");//sql语句配置文件路径

//设置不同表对应的增删改sql语句
SQLConf sqlConf = new SQLConf();
sqlConf.setInsertSqlName("insertcitypersonSQL");//对应sql配置文件dsl2ndSqlFile.xml配置的sql语句insertcitypersonSQL
sqlConf.setUpdateSqlName("citypersonUpdateSQL");//可选
sqlConf.setDeleteSqlName("citypersonDeleteSQL");//可选
sqlConf.setTargetDbName("test,ddlsyn");//为不同的库表sql配置指定对应的目标数据源，多个用逗号分隔，如果不指定就采用dbOutputConfig.setDbName方法设置的数据源
dbOutputConfig.addSQLConf("bboss.cityperson",sqlConf);//数据库加表名称保存sql配置，对应的sql在sqlconf指定的数据源test上执行

sqlConf = new SQLConf();
sqlConf.setInsertSqlName("insertbatchtest1SQL");//对应sql配置文件dsl2ndSqlFile.xml配置的sql语句insertbatchtestSQL
sqlConf.setUpdateSqlName("batchtest1UpdateSQL");//可选
sqlConf.setDeleteSqlName("batchtest1DeleteSQL");//可选
sqlConf.setTargetDbName("test,ddlsyn");//多个用逗号分隔
dbOutputConfig.addSQLConf("visualops.batchtest",sqlConf);
dbOutputConfig.setSqlConfResolver(new DatabaseTableSqlConfResolver());
```

## 3.2 数据库ddl同步配置

通过mysql binglog输入插件启用ddl操作同步功能，同时配置需要同步ddl的数据库清单

### 3.2.1 Mysql binlog输入插件配置

```java
//ddl同步配置，将bboss和visualops两个数据库的ddl操作在test和ddlsyn数据源上进行回放
mySQLBinlogConfig.setDdlSyn(true);//启用ddl操作同步功能
mySQLBinlogConfig.setDdlSynDatabases("bboss,visualops");//同步ddl的数据库清单
```

### 3.2.2 数据库输出插件配置

通过数据库输出插件的dbOutputConfig.addDDLConf方法配置ddl同步的源库与目标库数据源映射关系,可以添加不同数据库的同步目标数据源配置：

```java
DDLConf ddlConf = new DDLConf();
ddlConf.setDatabase("visualops");
ddlConf.setTargetDbName("ddlsyn,test");//database visualops的ddl同步目标数据源，多个用逗号分隔

dbOutputConfig.addDDLConf(ddlConf);
ddlConf = new DDLConf();
ddlConf.setDatabase("bboss");
ddlConf.setTargetDbName("ddlsyn,test");//database bboss的ddl同步目标数据源，多个用逗号分隔
dbOutputConfig.addDDLConf(ddlConf);
```

在建表或者添加字段ddl操作时，如果目标数据库已经存在相应的表或者字段，同步过程中就会报错，这样就会影响ddl操作的同步，因此需要忽略这种回放异常：

```java
dbOutputConfig.setIgnoreDDLSynError(true);//忽略ddl回放异常，如果ddl已经执行过，可能会报错，忽略sql执行异常
```

### 3.2.3 有效ddl语句筛选

一般我们只需要同步建表、修改字段、删表之类的ddl语句，因此可以在作业构建器的datarefactor接口对有效的ddl进行筛选：

```java
importBuilder.setDataRefactor(new DataRefactor() {
            @Override
            public void refactor(Context context) throws Exception {
                int action = (int)context.getMetaValue("action");
//                if(context.isUpdate() || context.isDelete())
//                    context.setDrop(true); //丢弃修改和删除数据
                String database = (String)context.getMetaValue("database");
                if( context.isDDL()) {
                    String ddl = context.getStringValue("ddl").trim().toLowerCase();
                    logger.info(context.getStringValue("ddl"));
                    logger.info(context.getStringValue("errorCode"));
                    logger.info(context.getStringValue("executionTime"));
                    boolean isddl = ddl.indexOf("create ") > 0 || ddl.indexOf("alter ") > 0 || ddl.indexOf("drop ") > 0;
                    if(!isddl){
                        context.setDrop(true);//过滤无效ddl语句
                    }


                }
                logger.info("database:{}",(String)context.getMetaValue("database"));
//                int action1 = (int)context.getMetaValue("action1");
            }
        });
```

## 3.3 作业依赖数据配置

本作业需要初始化和销毁ddl和数据同步的目标数据源test和ddlsyn

### 3.3.1 test数据源初始化和销毁

```java
DBOutputConfig dbOutputConfig = new DBOutputConfig();
dbOutputConfig
        .setDbName("test")
        .setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
        .setDbUrl("jdbc:mysql://192.168.137.1:3306/apm?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
        .setDbUser("root")
        .setDbPassword("123456")
        .setValidateSQL("select 1")
        .setUsePool(true)
        .setDbInitSize(5)
        .setDbMinIdleSize(5)
        .setDbMaxSize(10)
        .setShowSql(true)//是否使用连接池;
        .setSqlFilepath("dsl2ndSqlFile.xml");//sql语句配置文件路径
```

test数据会在作业结束时自动销毁

### 3.3.2 ddlsyn数据源初始化和销毁

ddlsyn数据源初始化

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
```

ddlsyn数据源销毁

```java
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

## 3.4 完整案例

可以访问以下地址了解多库多表数据同步到多目标库完整案例，在案例基础上适当调整即可实现所需的多库多表数据同步到多目标库功能：

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/binlog/MasterSlaveBinlog2TargetDBDBOutput.java

# 5 视频教程

**mysql binlog数据采集作业开发调测发布部署视频教程：**

https://www.bilibili.com/video/BV1ko4y1M7My/

# 6 开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />


交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">



# 7.支持我们

如果您正在使用bboss，或是想支持我们继续开发，您可以通过如下方式支持我们：

1.Star并向您的朋友推荐或分享

[bboss elasticsearch client](https://gitee.com/bboss/bboss-elastic)🚀

[数据采集&流批一体化处理](https://gitee.com/bboss/bboss-elastic-tran)🚀

2.通过[爱发电 ](https://afdian.net/a/bbossgroups)直接捐赠，或者扫描下面二维码进行一次性捐款赞助，请作者喝一杯咖啡☕️





<img src="images/alipay.png"  height="200" width="200">

<img src="images/wchat.png" style="zoom:50%;" />

非常感谢您对开源精神的支持！❤您的捐赠将用于bboss社区建设、QQ群年费、网站云服务器租赁费用。