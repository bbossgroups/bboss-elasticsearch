bboss-datatran由 [bboss ](https://www.bbossgroups.com)开源的数据采集同步ETL工具，提供数据采集、数据清洗转换处理和数据入库以及[数据指标统计计算流批一体化](https://esdoc.bbossgroups.com/#/etl-metrics)处理功能。

bboss-datatran采用标准的输入输出异步管道来处理数据

![](images\datasyn-inout-now.png)

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

### 1.2.1 插件属性

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

### 1.2.2 使用配置文件中的Database数据源

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

### 1.2.3 直接配置Database数据源

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

### 1.2.4 配置Database查询语句

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




# 2.输出插件

# 8.参考文档

本插件底层基于bboss httpproxy组件实现，参考文档：

https://esdoc.bbossgroups.com/#/development?id=_26-http%e5%8d%8f%e8%ae%ae%e9%85%8d%e7%bd%ae



https://esdoc.bbossgroups.com/#/httpproxy