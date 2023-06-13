# **Mysql binlog输入插件使用指南**

# 1.概述

bboss-datatran采用标准的输入输出异步管道来处理数据，输入插件和输出插件可以自由组合，输入插件从数据源采集数据，经过数据异步并行流批一体化处理后，输出插件将处理后的数据、指标数据输出到目标地。

<img src="images\datasyn-inout-now.png" alt="图片" style="zoom:75%;" />

<img src="images\datasyn.png" alt="图片" style="zoom:75%;" />

bboss插件参考文档：

https://esdoc.bbossgroups.com/#/datatran-plugins

bboss mysql binlog数据采集插件原理图如下：

<img src="images\mysql-binlog-arch.png" alt="图片" style="zoom:75%;" />



Mysql binlog插件通过配置对应的mysql master ip和端口、数据库账号和口令、监听的数据库表以及binlog文件路径等信息，实时采集mysql增删改数据，支持以下三种数据采集模式：

**模式1** 直接读取binlog文件,采集文件中的增删改数据

**模式2** 监听mysql master slave ip和端口，作业重启从binlog最新位置采集数据

**模式3** 监听mysql master slave ip和端口，启用故障容灾配置，每次重启作业从上次采集结束的位置开始采集数据

模式1适用一次性离线数据采集场景，模式2和模式3适用于实时采集场景。源表本来就有数据需要同步+实时同步,原来的数据可以基于模式1采集binlog文件，如果没有binlog文件，可以直接用数据库输入插件，直接一次性采集全表数据，然后再用模式3实现增量采集。

本文介绍mysql binlog插件的使用方法，以实时同步Mysql Binlog增删改数据到Elasticsearch作为案例来讲解。

# **2.采集作业实现**

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
        mySQLBinlogConfig.setTables("cityperson");//监控增量表名称
        mySQLBinlogConfig.setDatabase("bboss");//监控增量表名称
       
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
mySQLBinlogConfig.setDatabase("bboss");//监控增量表名称
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
        
        importBuilder.setInputConfig(mySQLBinlogConfig);
        importBuilder.setPrintTaskLog(true);

		//启用模式3 故障容灾机制配置       
//        importBuilder.setStatusDbname("testStatus");//指定增量状态数据源名称
      importBuilder.setLastValueStorePath("binlog2db_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
        importBuilder.setLastValueStoreTableName("binlog");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
```

通过setEnableIncrement方法启用模式3：

mySQLBinlogConfig.setEnableIncrement(true);//启用模式3

## 2.2 Elasticsearch输出插件配置**

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



可以直接参考以下文档章节：【2.8.10 灵活控制文档数据结构】

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

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/binlog/Binlog2EleasticsearchOutput.java

更多案例，可以参考文档：

[mysql binlog数据采集案例](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo?id=_20-mysql-binlog%e6%95%b0%e6%8d%ae%e9%87%87%e9%9b%86%e6%a1%88%e4%be%8b)

## 2.6 视频教程

**mysql binlog数据采集作业开发调测发布部署视频教程：**

https://www.bilibili.com/video/BV1ko4y1M7My/

# 3 开发交流

**Elasticsearch技术交流群：21220580,166471282**

**Elasticsearch微信公众号：**

![img](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)