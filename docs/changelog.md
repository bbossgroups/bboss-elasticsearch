

# BBOSS版本变更记录-v7.3.2 发布

[bboss](https://esdoc.bbossgroups.com/#/README)基于Apache License开源协议，由开源社区bboss发起和维护，主要由以下三部分构成：

- **Elasticsearch Highlevel Java Restclient** ， 一个高性能高兼容性的Elasticsearch/Opensearch java客户端框架
- **数据采集同步ETL** ，一个基于java语言实现数据采集作业的强大ETL工具，提供丰富的输入插件和输出插件，支撑将数据同时同步到多个数据源，可以基于插件规范轻松扩展新的输入插件和输出插件
- **流批一体化计算框架**，提供灵活的数据指标统计计算流批一体化处理功能的简易框架，可以结合数据采集同步ETL工具，实现数据流处理和批处理计算，亦可以独立使用；计算结果可以保存到各种关系数据库、分布式数据仓库Elasticsearch、Clickhouse等，特别适用于数据体量和规模不大的企业级数据分析计算场景，具有成本低、见效快、易运维等特点，助力企业降本增效。

详细介绍参考：[bboss简介](https://esdoc.bbossgroups.com/#/README)

# 快速导入bboss

一般项目导入下面的maven坐标即可：

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-datatran-jdbc</artifactId>
            <version>7.3.2</version>
        </dependency>
```

如果是spring boot1.x,2.x项目还需要导入下面的maven坐标：

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>7.3.2</version>
        </dependency>
```
如果是spring boot 3.x 项目还需要导入下面的maven坐标：

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot3-starter</artifactId>
            <version>7.3.2</version>
        </dependency>
```

ETL插件依赖的maven坐标，参考文档：[在工程中导入插件maven坐标](https://esdoc.bbossgroups.com/#/db-es-tool?id=_11-在工程中导入bboss-maven坐标)

# v7.3.3 功能改进-20250208
1. Elasticsearch客户端改进：增加对apiKey认证机制的支持,设置方法：
```properties
   #基于apiKeyId和apiKeySecret认证配置（主要用于Elasticsearch认证）
   http.apiKeyId = aaaa
   http.apiKeySecret = bbbbbb
```
2. Elasticsearch客户端增加Kerberos认证支持，使用参考文档：
   
      [Elasticsearch Kerberos认证配置](https://esdoc.bbossgroups.com/#/development?id=_212-kerberos认证配置)
      
3. http-proxy微服务框架增加Kerberos认证支持，使用参考文档：
   
  [Http Kerberos认证配置](https://esdoc.bbossgroups.com/#/httpproxy?id=_82-kerberos认证)
   
4. 多输出插件改进：为多输出插件添加记录过滤器,实现根据不同的输出插件对记录集进行过滤功能
     使用案例：

```java
   importBuilder.setOutputRecordsFilter((config, records) -> {
        if(config instanceof ElasticsearchOutputConfig) {
        return records;
            }
                    else{
//最多只返回前两条记录
List<CommonRecord> newRecords = new ArrayList<>();
                for(int i = 0; i < records.size() ; i ++) {
        newRecords.add(records.get(i));
        if(i == 2)
        break;
        }
        return  newRecords;
            }
                    });
```

# v7.3.2 功能改进-20250118
1. 数据采集功能扩展：增加多输出插件，支持将采集的数据同时同步到[多个数据源](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fesdoc.bbossgroups.com%2F%23%2Fdatatran-plugins%3Fid%3D_214-%e5%a4%9a%e6%ba%90%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)
2. 数据采集功能改进：优化[文件输出插件](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fesdoc.bbossgroups.com%2F%23%2Felasticsearch-sftp)文件切割机制，优化输出记录数据 buffer 机制，提升数据文件生成性能
3. 数据采集功能改进：作业任务完成回调处理配置管理优化
4. 数据采集功能改进：优化[作业停止逻辑](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fesdoc.bbossgroups.com%2F%23%2Fbboss-datasyn-control)
5. [Kafka 客户端](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fdoc.bbossgroups.com%2F%23%2Fkafka)组件改进：优化消费组件事务管理机制
6. Json 组件改进：增加不关闭 writer 的 json 序列化方法，提供更加优雅的数据序列化功能，并提升序列化性能

# v7.3.1 功能改进-20250102
1. 升级Velocity模版引擎版本1.7到2.5

升级注意事项：新版本去除模版变量$velocityCount，升级时，将脚本中的$velocityCount变量调整为$foreach.index即可，例如：

```java
#foreach($include in $includes)
	 #if($velocityCount > 0),#end
#end
调整为：
#foreach($include in $includes)
	 #if($foreach.index > 0),#end
#end
```

另外，在foreach循环中，可以通过$foreach.hasNext判断是否有记录。

如果不想升级velocity版本，可以参考文档降级velocity2.5到1.7：

[降级velocity2.5到1.7方法](https://esdoc.bbossgroups.com/#/question-answer?id=问题9-降级velocity25到17方法)

2. 升级jackson版本到2.18.2

# v7.3.0 功能改进-20241215
1. Milvus输入插件改进：新增通过向量search检索条件采集Milvus向量数据功能，并添加[相关案例](https://esdoc.bbossgroups.com/#/milvus-datatran)
2. 问题修复：修复引用外部Milvus数据源异常问题
3. 问题修复：修复Milvus输入插件没有配置expr的情况下增量查询报错的问题 
4. 增加Milvus到Milvus同步的[案例](https://esdoc.bbossgroups.com/#/milvus-datatran) 
5. 升级Milvus客户端驱动版本为2.5.2
6. 去除框架中对log4j的依赖，调整为log4j2

# v7.2.9 功能改进-20241126
1. 新增[Rocketmq输入插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_114-rocketmq%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)：从Rocketmq接收数据，支持同时设置多个topic主题，指定消息消费位置等参数；数据通过加工处理后，通过其他输出插件进行输出。
2. 新增[Rocketmq输出插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_213-rocketmq%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)：将数据源采集的数据，进行加工处理后，通过Rocketmq输出插件将处理后的数据发送到Rocketmq
3. 增加[Milvus输入插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_115-%e5%90%91%e9%87%8f%e6%95%b0%e6%8d%ae%e5%ba%93milvus%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)：支持全量或者增量从向量数据库Milvus采集同步数据到其他Milvus库，或者其他数据源 
4. 完善[Kafka输入](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_19-kafka%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)输出插件：完善kafk输入[输出插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_25-kafka%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)，初始化kafka客户端时不自动注册jvm shutdown hook 
5. 完善kafka输入插件：在数据中[设置kafka消息元数据](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_194-%e8%8e%b7%e5%8f%96%e6%b6%88%e6%81%af%e5%85%83%e6%95%b0%e6%8d%ae),包括topic、消息offset等信息
6. 调小http链接池健康检查数据源连接池大小 
7. 指标计算改进：新增指标key对象MetricKey,将相关接口参数String metricKey调整为MetricKey metricKey；通过在MetricKey中设置指标key类型，可以根据指标key类型，为指标设置不同的时间维度字段或者进行其他处理
8. 项目和案例源码jdk 18+兼容性调整，兼容jdk 18+版本 
9. Gradle构建脚本兼容性调整，兼容gradle 8+版本 
10. 增加Rocketmq客户端组件，使用参考文档：https://doc.bbossgroups.com/#/Rocketmq
11. 增量采集改进：增加将数字类型增量字段值标记为时间戳配置，如果标记为时间戳，那么increamentEndOffset配置将起作用,为时间戳增量查询增加一个查询截止时间条件：
```java
        importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);    
        importBuilder.setNumberTypeTimestamp(true);
        与以下方法配合一起使用（如果不设置increamentEndOffset，标识将不起作用）：       
         *  对于有延迟的数据源，指定增量截止时间与当前时间的偏移量
         *  增量查询截止时间为：System.currenttime - increamentEndOffset
         *  对应的变量名称：getLastValueVarName()+"__endTime" 对应的值类型为long
         *  单位：秒     
        importBuilder.setIncreamentEndOffset(10);
```
   使用参考案例：[Milvus时间戳增量同步](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/milvus/Milvus2CustomNumerTimestampDemo.java)

# v7.2.8 功能改进-20241102
1. 数据交换功能扩展：增加向量数据库Milvus输出插件，支持在数据处理时，调用向量模型服务，对数据进行向量化处理，通过向量库Milvus输出插件保存向量化处理结果。
   
    使用参考文档：[milvus向量数据库输出插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_212-milvus%e5%90%91%e9%87%8f%e6%95%b0%e6%8d%ae%e5%ba%93%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)
    
    使用案例：
    
    调用的Langchain-Chatchat封装的xinference发布的模型服务 

    https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/milvus/Db2Milvusdemo.java
    
    调用的xinference发布的模型服务 

    https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/milvus/Db2MilvusXinferencedemo.java
    
2. 完善文件输出插件：修复重传失败文件到minio oss对象存储抛出的空指针问题

3. 新增作业监控日志收集模块，日志级别分为：debug,info,warn,error,不输出日志，可以在脚本中调用对应的日志api，记录和上报日志，可以在作业监控查看记录的作业和作业任务日志；
    日志采用异步批处理模式入库，不影响作业加工处理性能和速度；通过输出日志MetricsLogReport接口输出和记录相关配置，通过ImportBuilder进行设置,在所有的作业初始化、数据处理接口方法中调用日志接口方法记录和上报日志，使用参考文档：

  https://esdoc.bbossgroups.com/#/metrics-logs

4. Datastream改进：增加运行时调整监控日志级别方法
```java
    dataStream.resetMetricsLogLevel(newMetricsLogLevel);
    日志级别定义如下：
   MetricsLogLevel {
   public static final int DEBUG = 1;
   public static final int INFO = 2;
   public static final int WARN = 3;
   public static final int ERROR = 4;

   /**
    * 忽略所有日志
      */
      public static final int NO_LOG = 5;
   }
```
5. 优化作业异常处理

6. 增加一系列新接口
   
   [RecordGeneratorV1](https://esdoc.bbossgroups.com/#/datatran-plugins?id=%e5%9f%ba%e4%ba%8erecordgeneratorv1) 接口参数调整为RecordGeneratorContext recordGeneratorContext，封装需要处理的数据和其他作业上下文信息
   
   [HeaderRecordGeneratorV1](https://esdoc.bbossgroups.com/#/datatran-plugins?id=%e5%9f%ba%e4%ba%8erecordgeneratorv1) 接口参数调整为RecordGeneratorContext recordGeneratorContext，封装需要处理的数据和其他作业
   
   [CustomOutPutV1](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_272-customoutputv1-%e6%a1%88%e4%be%8b) 接口参数调整为CustomOutPutContext customOutPutContext 封装需要处理的数据和其他作业上下文信息
7. 增加Milvus客户端组件，使用参考文档：https://doc.bbossgroups.com/#/Milvus

# v7.2.7 功能改进-20240901
1. http服务框架改进：http-proxy增加[nacos配置中心支持以及基于nacos服务发现功能](https://esdoc.bbossgroups.com/#/httpproxy?id=_4%ef%bc%89%e5%8a%a0%e8%bd%bdnacos%e9%85%8d%e7%bd%ae%e5%90%af%e5%8a%a8httpproxy)

3. es客户端改进：增加[nacos配置中心支持以及基于nacos的es节点发现功能](https://esdoc.bbossgroups.com/#/nacos-config?id=_1elasticsearch%e5%ae%a2%e6%88%b7%e7%ab%af%e4%b8%8enacos%e5%af%b9%e6%8e%a5)

4. es客户端改进：es数据源停止后，相应的ClientInterface api抛出es数据源停止异常；数据源重启后，相应的ClientInterface api即可恢复正常调用，提供相应的测试用例[CustormInitAndBoot1](https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/custominit/CustormInitAndBoot1.java)

5. 基础框架改进：属性配置管理增加nacos插件支持，参考Apollo配置中心插件，支持基于nacos管理配置，从nacos加载ioc配置、属性配置 

   参考文档：https://esdoc.bbossgroups.com/#/nacos-config

6. 问题修复：修复apollo和nacos配置管理属性不能热加载问题

# v7.2.6 功能改进-20240811
1. 问题修复：修复部分Postgresql分页查询失败问题
2. 功能改进：将框架中部分缓存功能中使用的HashMap调整为ConcurrentHashMap,消除可能存在多线程安全隐患
3. 文件输出插件改进：增加将文件写入oss数据库minio功能，案例地址：抽取Elasticsearch数据生成文件，并写入oss数据库minio

https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2FileMinioDemo.java


# v7.2.5 功能改进-20240716
1. 问题修复：解决大数据量excel文件采集失败问题

# v7.2.3 功能改进-20240707
1. 持久层改进：优化持久层查询元数据缓冲机制和结构，去除不必要的缓冲和冗余信息，节约内存，提升性能
2. 持久层改进：映射记录为Map类型时，默认关闭将查询列名称转换为大写值作为map key功能，如果需要开启(不建议开启)，则进行相应设置即可：db.columnLableUpperCase=true


# v7.2.2 功能改进-20240628
1. 文件采集插件改进：为文本文件采集增加内存缓冲区机制，大幅提升文本采集性能
2. 并行数据采集改进：增加判断记录集是否为空逻辑，解决可能存在输入内容为空异常
3. http proxy改进：将3xx状态响应当做正常响应处理

# v7.2.1 功能改进-20240616

1. 增加tdengine数据库的适配器
2. 数据库输出插件改进：同步sql 输出到log日志
2. 数据库[输入](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_125-%e5%bc%95%e7%94%a8%e7%ac%ac%e4%b8%89%e6%96%b9datasource)、[输出](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_217-%e5%bc%95%e7%94%a8%e7%ac%ac%e4%b8%89%e6%96%b9datasource)插件改进：增加直接引用第三方Datasource功能

# v7.2.0 功能改进

1. 数据采集同步改进：增加并行模式执行数据加工方法datarefactor

   除[数据库输入插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_124-%e5%b9%b6%e8%a1%8c%e6%95%b0%e6%8d%ae%e5%8a%a0%e5%b7%a5%e5%a4%84%e7%90%86)，其他输入插件采用并行模式执行数据加工方法datarefactor。

   数据库输入插件默认采用串行模式执行，可以通过dbInputConfig.setParallelDatarefactor(true)切换为[并行执行模式](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_124-%e5%b9%b6%e8%a1%8c%e6%95%b0%e6%8d%ae%e5%8a%a0%e5%b7%a5%e5%a4%84%e7%90%86)

   并行加工处理模式只有在并行作业任务模式才起作用，参考章节【[4.3 串行执行和并行执行](https://esdoc.bbossgroups.com/#/db-es-tool?id=_43-%e4%b8%b2%e8%a1%8c%e6%89%a7%e8%a1%8c%e5%92%8c%e5%b9%b6%e8%a1%8c%e6%89%a7%e8%a1%8c)】
2. 修复上一个版本导致的Elasticsearch输出插件日期转换空指针异常 
3. 数据采集同步改进：文件输入插件从ftp下载文件，重启作业时，文件过滤器检测状态表中文件异常时将异常抛出去，而不是认为文件不存在，避免不可以预知的异常

# v7.1.9 功能改进
1. RecordGenerator接口方法参数由Context调整为TaskContext，简化接口设计
```java
   public void buildRecord(Context taskContext, CommonRecord record, Writer builder) throws Exception;
    调整为：
   public void buildRecord(TaskContext taskContext, CommonRecord record, Writer builder) throws Exception;
```
2. kafka输出插件改进，增加批量并行消息输出能力，提升插件性能
3. 处理采用sqlserver数据库管理增量同步状态管理表无法创建问题
4. 支持http请求拦截器功能，[配置HttpRequestInterceptor](https://esdoc.bbossgroups.com/#/httpproxy?id=_9%e9%85%8d%e7%bd%aehttprequestinterceptor)
5. 增加对Clickhouse jdbc官方驱动的支持（使用http端口）,参考文档：https://doc.bbossgroups.com/#/persistent/datasource-cluster
6. 插件改进：优化http输出插件、文本文件输出插件、dummy输出插件、Elasticsearch输出插件，大幅提升并行批处理任务性能
7. 流处理指标计算改进：调整etl和流处理混合模式中的流处理功能到批处理任务中执行 
8. 简化回调处理结果和参数对象泛型结构
   TaskCommand<DATA,RESULT> --- TaskCommand<RESULT>
   BaseTaskCommand<List<CommonRecord>, Object>  ---BaseTaskCommand< Object>
   DefualtExportResultHandler<String,String> ---DefualtExportResultHandler<String>
   ExportResultHandler<DATA,RESULT> ---- ExportResultHandler<RESULT>
8. 添加增量采集配置正确性校验功能
9. 增加统一异常信息构建工具 
10. 处理作业过程中指标分析器提示metricspersistent已经停止问题

# v7.1.8 功能改进
1. Elasticsearch客户端改进：处理Elasticsearch 8以上版本_type兼容性问题
2. Elasticsearch客户端改进：处理Elasticsearch 7.x,8.x 版本sql api兼容性问题
# v7.1.7 功能改进
1. 持久层改进：修复无法获取Clickhouse元数据问题
2. 完善MongoDB CDC插件：解决新版MongoDB场景下修改数据不完整的场景下空指针异常问题
3. 数据同步改进：tranresultset部分与记录数据处理相关的职能迁移到Record对象
4. 增加MongoDB客户端自定义构建接口，方便业务侧自定义MongoDB客户端配置,使用案例
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

# v7.1.6 功能改进

1. Elasticsearch Client增加对spring boot3的支持，并提供相关案例工程：

   https://gitee.com/bboss/springboot3-elasticsearch-webservice

   https://gitee.com/bboss/eshelloword-spring-boot3-starter

   https://gitee.com/bboss/db-db-job3

2. 升级若干第三方依赖开源包版本

# v7.1.5 功能改进
1. 数据处理作业改进：[设置增量状态ID生成策略](https://esdoc.bbossgroups.com/#/db-es-tool?id=_28511-%e5%a2%9e%e9%87%8f%e7%8a%b6%e6%80%81id%e7%94%9f%e6%88%90%e7%ad%96%e7%95%a5%e9%85%8d%e7%bd%ae)，在设置jobId的情况下起作用

```java
ImportIncreamentConfig.STATUSID_POLICY_JOBID 采用jobType+jobId作为增量状态id
ImportIncreamentConfig.STATUSID_POLICY_JOBID_QUERYSTATEMENT 采用[jobType]+[jobId]+[作业查询语句/文件路径等信息的hashcode]，作为增量id作为增量状态id
默认值ImportIncreamentConfig.STATUSID_POLICY_JOBID_QUERYSTATEMENT
```

2. 扩展kafka输出插件，可以根据实际需求，设置记录级别kafka发送主题功能，使用案例：
```java
   //设置记录级别的kafka主题
   context.setKafkaTopic("es2kafka1");
```
3. context接口增加一组工具方法，用户一次性将bean或者map中的数据添加到记录中

```java
/**
     * 将对象中的所有字段和值作为字段添加到记录中，忽略空值字段
     * @param bean
     * @return
     */
    Context addFieldValues( Object bean);

/**
     * 将对象中的所有字段和值作为字段添加到记录中
     * 根据参数ignoreNullField控制是否忽略空值字段 true 忽略  false 不忽略
     * @param bean
     * @param ignoreNullField
     * @return
     */
    Context addFieldValues(Object bean,boolean ignoreNullField);

/**
     * 将map中的所有键值对作为字段添加到记录中，忽略空值字段
     * @param values
     * @return
     */
    Context addMapFieldValues( Map<String,Object> values);

/**
     * 将map中的所有键值对作为字段添加到记录中
     *  根据参数ignoreNullField控制是否忽略空值字段 true 忽略  false 不忽略
     * @param values
     * @param ignoreNullField
     * @return
     */
    Context addMapFieldValues( Map<String,Object> values,boolean ignoreNullField);
```

# v7.1.3 功能改进
1. 为Clickhouse数据源增加负载均衡机制，解决Clickhouse-native-jdbc驱动只有容灾功能而没有负载均衡功能的缺陷，使用方法如下：

   在jdbc url地址后面增加b.balance和b.enableBalance参数
```java
   jdbc:clickhouse://101.13.6.4:29000,101.13.6.7:29000,101.13.6.6:29000/visualops?b.balance=roundbin&b.enableBalance=true
```
b.enableBalance为true时启用负载均衡机制，并具备原有容灾功能，否则只具备容灾功能

b.balance 指定负载均衡算法，目前支持random（随机算法，不公平机制）和roundbin(轮询算法，公平机制)两种算法，默认random算法

另外也可以在DBConf上进行设置，例如：
```java
BConf tempConf = new DBConf();
tempConf.setPoolname(ds.getDbname());
tempConf.setDriver(ds.getDbdriver());
tempConf.setJdbcurl( ds.getDburl());
tempConf.setUsername(ds.getDbuser());
tempConf.setPassword(ds.getDbpassword());
tempConf.setValidationQuery(ds.getValidationQuery());
//tempConf.setTxIsolationLevel("READ_COMMITTED");
tempConf.setJndiName("jndi-"+ds.getDbname());
PropertiesContainer propertiesContainer = PropertiesUtil.getPropertiesContainer();
int initialConnections = propertiesContainer.getIntProperty("initialConnections",5);
tempConf.setInitialConnections(initialConnections);
int minimumSize = propertiesContainer.getIntProperty("minimumSize",5);
tempConf.setMinimumSize(minimumSize);
int maximumSize = propertiesContainer.getIntProperty("maximumSize",10);
tempConf.setMaximumSize(maximumSize);
tempConf.setUsepool(true);
tempConf.setExternal(false);
tempConf.setEncryptdbinfo(false);
boolean showsql = propertiesContainer.getBooleanProperty("showsql",true);
tempConf.setShowsql(showsql);
tempConf.setQueryfetchsize(null);
tempConf.setEnableBalance(true);
tempConf.setBalance(DBConf.BALANCE_RANDOM);
return SQLManager.startPool(tempConf);
```
持久层使用案例：

https://gitee.com/bboss/bestpractice/blob/master/persistent/src/com/frameworkset/sqlexecutor/TestClickHouseDB.java

ETL DB输出插件案例（DB输入插件类似）：

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/clickhouse/Db2Clickhousedemo.java
2. 优化停止db数据源机制，修复当停止数据源并移除数据源信息时，没有执行停止数据源操作
3. 处理sqlite数据库创建statement兼容性问题
4. Clickhouse-native-jdbc驱动兼容性问题修复处理
# v7.1.2 功能改进
1. 优化jvm推出机制：默认关闭jvm退出时注销ioc容器及相关资源
   在启用自动关闭的情况下，才可以在jvm退出时自动关闭和释放资源，否则需要手动调用ShutdownUtil.shutdown()方法释放资源，启用自动释放资源方法：
   
   ```java
   jvm命令行参数
   	-DenableShutdownHook=true
   环境变量
   	enableShutdownHook=true
   默认关闭：
       enableShutdownHook=false
   ```
   
   
   
2. 文件输出插件文件序号在kafka、mysql cdc、MongoDB cdc等场景下序号滚动机制完善   

3. 完善持久层报错日志：数据源不存在时给出友好的提示信息

4. 优化jackson对localdatetime类型的处理，如果没有引入jackson-datatype-jsr310插件，忽略加载localdatetime处理插件异常

5. 优化基于消息流处理事件上下文重置机制 

6. 去重兼容老版本的maven坐标，兼容版本对应关系：
   
   
   
   | 老版本坐标                        | 新版本坐标             |
   | --------------------------------- | ---------------------- |
   | bboss-elasticsearch-rest-file2ftp | bboss-datatran-fileftp |
   | bboss-elasticsearch-rest-file | bboss-datatran-fileftp |
   | bboss-elasticsearch-rest-hbase    | bboss-datatran-hbase   |
   | bboss-elasticsearch-rest-jdbc     | bboss-datatran-jdbc    |
   | bboss-elasticsearch-rest-kafka1x  | bboss-datatran-kafka1x |
   | bboss-elasticsearch-rest-kafka2x  | bboss-datatran-kafka2x |
   | bboss-elasticsearch-rest-mongodb  | bboss-datatran-mongodb |
   
   参考上面的对应关系将老版本迁移到新版本的坐标即可

# v7.1.1 功能改进
1. 处理获取Oracle Date类型字段值，字段精度丢失问题（时分秒），采用Timestamp进行处理
2. 增加Context接口方法getValue(String fieldName, java.sql.Types),在处理关系数据库数据时，获取字段对应类型的原始值：
```java
   Object value = context.getValue("ACTIVE_TIME", Types.TIMESTAMP);
```
3. 增加MongoDB CDC输入插件：可以增量模式采集MongoDB 增、删、改数据，也可每次作业重启从最新位置采集MongoDB 增、删、改数据
    参考案例： https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/cdc/MongodbCDCDemo.java

4. 优化作业生命周期管理机制：importcotext增加终止作业执行方法，方便在异步作业处理的任何地方终止作业的执行

5. 更换MongoDB驱动包为mongodb-driver-sync

6. [mysql cdc优化：兼容高版本mysql jdbc驱动](https://gitee.com/bboss/bboss-elastic-tran/commit/b00383c800e8d2bfe707ac8c6ae9c86cbc2c3fef)

7. MongoDB[输出插件改进](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_28-mongodb%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)：支持修改和删除记录同步，支持多表、多库、多数据源数据同步
   为记录指定数据源和表,案例  :https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/cdc/MongodbCDC2MongoDBDemo.java

8. 数据库输出插件未指定修改或者删除sql语句，但是存在修改、删除状态记录时，给出明确异常提示，建议忽略对应的修改和删除状态的记录，参考文档：[2.8.10.3 过滤记录](https://esdoc.bbossgroups.com/#/db-es-tool?id=_28103-过滤记录)

   

# v7.1.0 功能改进

1. 流批一体化改进：框架增加了添加和获取用于指标计算处理等的临时数据到记录，不会对临时数据进行持久化处理
使用案例：
```java
   //添加用于指标计算处理等的临时数据到记录，不会对临时数据进行持久化处理，
   context.addTempData("name","ddd");
```
```java
   //获取用于指标计算处理等的临时数据到记录，不会对临时数据进行持久化处理，
   CommonRecord data = (CommonRecord) mapData.getData();
  
   String name = (String)data.getTempData("name");
```
2. 修复指标分析器设置时间格式空指针异常
3. 修复指标分析器设置时间窗口类型空指针异常
4. Elasticsearch客户端改进：添加文档时，如果数据采用Map封装，控制是否保存文档id字段到记录中，true 保存 false 不保存，默认值true
使用案例：
```java
   //创建创建/修改/获取/删除文档的客户端对象，单实例多线程安全
   ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
   //构建一个对象，日期类型，字符串类型属性演示
   Map demo = new LinkedHashMap();
   demo.put("demoId","2");//文档id，唯一标识，@PrimaryKey注解标示,如果demoId已经存在做修改操作，否则做添加文档操作
   demo.put("agentStarttime",new Date());
   demo.put("applicationName","blackcatdemo2");
   demo.put("contentbody","this-is content body2");
   demo.put("agentStarttime",new Date());
   demo.put("name","|刘德华");
   demo.put("orderId","NFZF15045871807281445364228");
   demo.put("contrastStatus",2);
   demo.put("localDateTime", LocalDateTime.now());

        //强制刷新
        ClientOptions addOptions = new ClientOptions();
        addOptions.setIdField("orderId");
        addOptions.setPersistMapDocId(false);
        //如果orderId对应的文档已经存在则更新，不存在则插入新增
        String response = clientUtil.addDocument("demonoid",//索引表
                demo,addOptions);
```
5. 流处理机制改进：根据时间窗口类型配置日期格式和相应的时间维度字段
6. 流处理机制改进：useDefaultMapData调整为false

# v7.0.9 功能改进

1. [Mysql binlog插件插件改造：支持ddl同步，使用案例](https://gitee.com/bboss/bboss-elastic-tran/commit/b3b50860414a1f2bc2b1557f62c4af86746071c0)
2. [Mysql binlog插件和db输出插件改造：支持采集多个数据库表数据，同步到多个数据库的不同表，使用案例](https://gitee.com/bboss/bboss-elastic-tran/commit/248835d652d9c55ce2e3c3acad09a03fac42b05d):

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/elasticsearch/imp/binlog/MasterSlaveBinlog2TargetDBDBOutput.java

​     参考文档：[多库多表数据同步到多目标库](https://esdoc.bbossgroups.com/#/mysql-binlog?id=_3-多库多表数据同步到多目标库)

# v7.0.8 功能改进
1. 数据库输入输出插件改进：增加配置db connection property配置方法addConnectionProperty，使用案例
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
   .setSqlName("demoexport")
   //.setDbAdaptor("org.frameworkset.elasticsearch.imp.DruidAdaptor")
   .addConnectionProperty("aaaaa","bbbbbb");
```
2. 持久层改进：增加druid数据库适配支持
# v7.0.7 功能改进
1. 数据库输入插件改进：增加enableLocalDate控制开关，是否启用Localdate和LocalDateTime类型，true 启用 false 不启用，默认为false；如果不启用则将Localdate和LocalDateTime类型的值统一转换为Date类型处理

2. 增量状态管理改进：[设置已完成记录增量状态过期清理机制](https://esdoc.bbossgroups.com/#/filelog-guide?id=_14%e8%ae%be%e7%bd%ae%e5%b7%b2%e5%ae%8c%e6%88%90%e8%ae%b0%e5%bd%95%e5%a2%9e%e9%87%8f%e7%8a%b6%e6%80%81%e8%bf%87%e6%9c%9f%e6%b8%85%e7%90%86%e6%9c%ba%e5%88%b6)，设置采集完毕文件状态记录有效期，过期后迁移到历史表，同时清理内存中的记录，添加相关案例：
    fileInputConfig.setCleanCompleteFiles(true);//删除已完成文件

  fileInputConfig.setFileLiveTime(30 * 1000L);//已采集完成文件存活时间，超过这个时间的文件就会根据CleanCompleteFiles标记，进行清理操作，单位：毫秒

  fileInputConfig.setRegistLiveTime(60 * 1000L);//已完成文件状态记录有效期，单位：毫秒

  fileInputConfig.setScanOldRegistRecordInterval(30 * 1000L);//扫描过期已完成文件状态记录时间间隔，默认为1天，单位：毫秒

3. 增加ObjectHolder类，保持对象，用来在作业各组件之间传递其保持的对象

# v7.0.6 功能改进

1. 文件采集插件改进：增加[word](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_110-word%e6%96%87%e4%bb%b6%e9%87%87%e9%9b%86%e6%8f%92%e4%bb%b6)、[pdf](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_111-pdf%e6%96%87%e4%bb%b6%e9%87%87%e9%9b%86%e6%8f%92%e4%bb%b6)、[图片、视频等](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_112-%e5%85%b6%e4%bb%96%e7%b1%bb%e5%9e%8b%e6%96%87%e4%bb%b6%e9%87%87%e9%9b%86%e6%8f%92%e4%bb%b6)类型文件采集功能
2. 文件输出插件改进：增加文件输入插件[数据写入空闲时间阈值配置](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_233-%e5%86%99%e5%85%a5%e7%a9%ba%e9%97%b2%e6%97%b6%e9%97%b4%e9%98%88%e5%80%bc%e9%85%8d%e7%bd%ae)
3. [优化完成文件清理机制：在扫描新文件过程中检测完成文件并清理](https://gitee.com/bboss/bboss-elastic-tran/commit/d05fe9a83752433c29ad557cdf8cb2839b443fa9)
4. [作业停止机制优化：异常情况停止时清理队列中的数据](https://gitee.com/bboss/bboss-elastic-tran/commit/9ce46be4dbac82a8df1fece7c8b149ef0b52fff5)
5. http proxy微服务框架改进：httpproxy增加一组httpGetforObjectWithParams/httpGetforStringWithParams/httpGetforStringWithParamsHeaders/httpPostforStringWithHeader方法
6. [优化http proxy rpc api方法结构，消除多态方法冲突问题](https://gitee.com/bboss/bboss-http/commit/2e7d4c16ad57ebce5da9efb231618a40a2e86dd3)
7. [增加一组sendJsonBodyfortypeobject方法，支持非标准容器类行和元素类型rpc服务调用](https://gitee.com/bboss/bboss-http/commit/7ffa8b8c73027249ced1b0ca9db2328e914d4488)



# v7.0.5 功能改进

1. Elasticsearch客户端改进：将原来默认加载conf/elasticsearch.properties,application.properties,config/application.properties三个配置文件，调整为只默认加载application.properties文件。如需加载其他文件，可以参考文档：

https://esdoc.bbossgroups.com/#/Elasticsearch-bboss-custom-init

2. Ioc容器改进：全部走PropertiesUtil加载config配置文件，统一加载模式,避免重复加载属性文件

3. Metrics指标计算改进：日期维度字段类型为LocalDate或者Localdatetime时，自动转换为Date类型

4. 数据转换改进：优化数据转换处理异常处理

5. 文件输出插件问题修复：修复增量状态管理不一致问题，文件名不规范时，创建文件失败，无法写入数据，但是增量状态已经flush，导致增量状态管理不正确

6. 文件输出插件改进：处理因初始化文件失败导致写入数据空指针问题

7. 处理异步传输通道改进：由于异常退出作业任务时，未清理队列中的脏数据，导致输入插件推送数据到异步通道队列阻塞问题，在退出任务时，增加清理队列脏数据功能，解决异常退出阻塞问题

8. Elasticsearch客户端改进：优化scroll和slice scroll并行查询异常处理机制

9. http proxy模块扩展：post/get/put等方法支持po对象传递请求参数，从而支持map和po两种方式传递服务参数

10. bboss基础框架改进：父配置文件中存在的配置不会被引用配置文件中的配置参数覆盖，支持应用参数个性化配置

11. bboss基础框架改进：增加日期格式化和解析工具方法

# v7.0.3 功能改进
1. kafka输入插件改进：处理字符集名称不正确的问题

# v7.0.2 功能改进
1. 数据采集改进：处理增量状态类型转换异常
2. 优化增量状态管理：锁机制优化
# v7.0.1 功能改进
1. 文件采集插件改进：一次性文件全量采集的处理，可以通过控制开关[disableScanNewFilesCheckpoint](https://esdoc.bbossgroups.com/#/filelog-guide?id=%e7%ad%96%e7%95%a5%e4%b8%80-disablescannewfiles)禁止和启用文件采集状态记录功能，false 启用，true 禁止（默认值）；启用记录状态情况下，作业重启，已经采集过的文件不会再采集，未采集完的文件，从上次采集截止的位置开始采集。
2. 优化用户自定义dsl输出机制：用户自己实现决定输出哪些日志，但是之前提供了一个慢日志的默认功能，二选一，不需要两个同时做，自定义的优先，没有自定义就判断是否设置需要打印慢dsl，如果需要则调用慢日志输出组件输出，注意：开启自定义dsl输出后，要关闭showTemplate，否则会重复输出日志
3. 状态管理info日志调整为debug级别日志
4. 处理mysql binlog 插件flushInterval机制不起作用问题,   优化数据处理管道flushInterval机制
5. mysql binlog插件增加异步启动机制，[joinToConnectTimeOut](https://esdoc.bbossgroups.com/#/mysql-binlog?id=_213-%e6%a8%a1%e5%bc%8f3%e6%a1%88%e4%be%8b)大于0生效，否则是同步启动，启用方法：

```java
MySQLBinlogConfig mySQLBinlogConfig = new MySQLBinlogConfig();
mySQLBinlogConfig.setJoinToConnectTimeOut(20000L);
```
# v7.0.0 功能改进

1. 细化数据处理管道停止状态：正常停止和异常停止，正常停止情况下需要继续处理完数据，异常停止情况下不处理后续数据

# v6.9.9 功能改进

1.  增加作业运行结束监听器（异步/同步），可以通过监听器识别作业是异常结束还是正常结束

   使用参考文档：[作业关闭事件监听器](https://esdoc.bbossgroups.com/#/db-es-tool?id=_216-作业关闭事件监听器)

2. [优化作业task异常处理功能](https://gitee.com/bboss/bboss-elastic-tran/commit/688215ba220bbf48de5a668a338f0d5984488239)

3. [修复初始化增量状态处理异常](https://gitee.com/bboss/bboss-elastic-tran/commit/8edb4d8c5288e8ba78ee8c69384d572f8cd374f7)

# v6.9.8 功能改进

1. [Elasticsearch 异步批处理拦截器参数bulkcommand对象增加获取当前记录集方法](https://gitee.com/bboss/bboss-elastic/commit/a1db37171d7d35ce0e188e3cae4452c0563adcc0)

2. [禁用postgresql数据源全局jdbcFetchSize配置，只能做查询级别通过DBOptions设置jdbcFetchSize](https://gitee.com/bboss/bboss-elastic/commit/0a06e492e3e8af77605d8cfb2dc15d574522dd8f)

3. 扩展[属性管理组件](https://doc.bbossgroups.com/#/aop/IntroduceIoc)，支持属性文件引用功能，文件路径相对于classpath根目录即可，使用案例如下：多个文件用逗号分隔

   include.files=elasticsearch.properties,kafka.properties 
   
   include.files=elasticsearch.properties

# v6.9.7 功能改进

1. 增量状态改进：支持复杂类型增量状态值管理，例如mysql binlog master slave对应的binlog文件及binlog position组合增量状态管理
2. 优化异常情况下作业退出管理功能

# v6.9.6 功能改进

1. [mysql binlog插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_13-mysql-binlog%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)改进完善

2. [数据库输出插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_21-db%e8%be%93%e5%87%ba%e6%8f%92%e4%bb%b6)改进，支持根据规则设置不同的增删改sql语句

3. CommonRecord支持元数据api getMetaDatas，参考案例如下：

   ```java
   CustomOutputConfig customOutputConfig = new CustomOutputConfig();
   customOutputConfig.setCustomOutPut(new CustomOutPut() {
       @Override
       public void handleData(TaskContext taskContext, List<CommonRecord> datas) {
   
           //You can do any thing here for datas
           for(CommonRecord record:datas){
               Map<String,Object> data = record.getDatas();
   
               logger.info(SimpleStringUtil.object2json(record.getMetaDatas()));
   
           }
       }
   });
   ```

4. Elasticsearch Client LocalDateTime/LocalDate/LocalTime json序列化支持

5. Elasticsearch输入插件增量采集支持LocalDateTime兼容性支持，支持纳秒级时间增量粒度：

   ```java
   importBuilder.setLastValueType(ImportIncreamentConfig.LOCALDATETIME_TYPE);//NUMBER_TYPE TIMESTAMP_TYPE LOCALDATETIME_TYPE
   ```

# v6.9.3 功能改进

1. 优化kafka组件：增加[弹性动态调整kafka消费线程](https://doc.bbossgroups.com/#/kafka?id=_4-%e5%bc%b9%e6%80%a7%e6%89%a9%e5%b1%95%e5%92%8c%e7%bc%a9%e5%87%8fkafka-consumer%e6%b6%88%e8%b4%b9%e7%ba%bf%e7%a8%8b)功能
2. 增加[mysql binlog输入采集插件](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_13-mysql-binlog%e8%be%93%e5%85%a5%e6%8f%92%e4%bb%b6)，支持两种模式：监听master-slave模式和直接采集binlog日志文件模式
3. Elasticsearch client增加[msearch api](https://esdoc.bbossgroups.com/#/document-crud?id=_16msearch%e6%a3%80%e7%b4%a2)
4. 处理持久层框架postgresql兼容性问题：设置预编译参数值可能导致的死循环问题

# v6.9.2 功能改进
1. 调整增量状态管理数据库sqlite默认口令为复杂口令，同时支持设置sqlite状态数据库口令
2. Elasticsearch客户端增加elasticsearch.healthPath配置，用来配置故障节点健康检查服务地址，默认为/,可以自行调整为其他可用的服务地址
3. 一次性任务结束时，自动释放作业资源，自动关闭作业
4. 优化任务结束状态检查管理机制

# v6.8.9 功能改进
1. 文件采集插件改进：大量文件采集场景下的[流控处理机制](https://esdoc.bbossgroups.com/#/filelog-guide?id=_12%e5%b9%b6%e8%a1%8c%e9%87%87%e9%9b%86%e6%96%87%e4%bb%b6%e6%95%b0%e9%87%8f%e6%8e%a7%e5%88%b6-%e6%b5%81%e6%8e%a7)，通过设置同时并行采集最大文件数量，控制并行采集文件数量，避免资源过渡消耗，保证数据的平稳采集。当并行文件采集数量达到阈值时，启用流控机制，当并行采集文件数量低于最大并行采集文件数量时，继续采集后续文件
2. 改进[ftp/sftp文件下载采集机制](ftp/sftp文件下载采集机制)：采集/定时调度采集时sftp/ftp文件时，等待所有的文件采集都加入采集通道后再返回，继续下一次调度采集（定时调度）/或者再继续后续的处理（一次性采集）
3. bulkprocessor改进：线程池关闭后等待所有任务处理完成再退出
4. 改进[通用bulk批处理器](https://esdoc.bbossgroups.com/#/bulkProcessor-common)和[Elasticsearch bulk批处理器](https://esdoc.bbossgroups.com/#/bulkProcessor)：优化flush线程及锁管理和shutdown机制
5. 改进kafka消费组件：将工作线程改为daemon=false

# v6.8.8 功能改进
1. Elasticsearch客户端改进：改进[ElasticsearchBulkProcessor](https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/bulkprocessor/TestBulkProcessor7x.java)，增加maxMemSize参数，设置批量记录占用内存最大值，以字节为单位，达到最大值时，执行一次bulk操作

      可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作
	
      maxMemSize参数默认值为0，不起作用，只有>0才起作用
	
	  使用参考文档：[Eleasticsearch BulkProcessor异步批处理](https://esdoc.bbossgroups.com/#/bulkProcessor)
	
2. 数据同步工具改进：DB数据源链接超时时间、获取连接池链接等待超时不起作用问题修复
3. kafka插件改进：增加[启动消费端方法和手动消费消费端方法](https://doc.bbossgroups.com/#/kafka)，启动ioc配置对应的容器中管理的kafka消费程序，通过addShutdownHook控制是否注册消费程序销毁hook，以便在jvm退出时自动关闭消费程序 true 注册，false不注册

    false 情况下需要手动调用shutdownConsumers(String applicationContextIOC)方法或者shutdownAllConsumers()方法销毁对应的消费程序 	  
	
# v6.8.7 功能改进
1. 扩展http输入插件：增加[http并行查询](https://esdoc.bbossgroups.com/#/datatran-http?id=_25-%e5%8f%82%e6%95%b0%e5%88%86%e7%bb%84%e5%b9%b6%e8%a1%8c%e6%9f%a5%e8%af%a2)功能，可以根据需要划分多个参数组，实现http服务数据的并行查询功能，从而获得更好的数据采集同步性能
# v6.8.6 功能改进

1. 完善[文件采集插件](https://esdoc.bbossgroups.com/#/filelog-guide)：增加一次性扫描导入本地文件/FTP文件功能，通过属性disableScanNewFiles进行控制：true 一次性扫描导入本地/FTP目录下的文件，false 持续监听本地/FTP目录下的新文件（默认值false）

   private boolean disableScanNewFiles

# v6.8.5 功能改进
1. http输入插件改进：增加对http get请求的支持，http post、http put请求增加对request参数模式的支持（非请求体dsl报文模式）
2. http输入插件、Elasticsearch输入插件、数据库输入插件改进：分别在httpproxy访问异常、在Elasticsearch客户端访问异常、数据库客户端访问异常信息中包含http dsl、Elasticsearch dsl、db sql信息，以便快速发现和定位问题，提升排错效率

# v6.8.3 功能改进
bug修复版本：一次性执行timekey类型指标统计作业forceflush metrics时抛出空指针问题修复

# v6.8.2 功能改进
1. FTP文件[输入](https://esdoc.bbossgroups.com/#/filelog-guide?id=_7ftp%e9%87%87%e9%9b%86%e9%85%8d%e7%bd%ae)/[输出](https://esdoc.bbossgroups.com/#/elasticsearch-sftp?id=_34-sftpftp%e9%85%8d%e7%bd%ae)插件改进：ftp/sftp协议增加socketTimeout配置，sftp协议增加connectionTimeout配置

2. FTP输出插件改进：增加生成文件[异步上传FTP机制](https://esdoc.bbossgroups.com/#/elasticsearch-sftp?id=_34-sftpftp%e9%85%8d%e7%bd%ae)，默认同步发送。数据量比较多，同时切割文件的情况下，启用异步发送文件，会显著提升数据采集同步性能

3. 数据采集重大功能扩展：增加[指标计算输出插件](https://esdoc.bbossgroups.com/#/etl-metrics)，在采集-清洗-转换处理的同时，提供数据流处理统计分析功能，支持两种模式的数据流处理指标计算：
   
   
   
   1)  在采集和处理数据时，同时对数据进行大数据指标统计聚合计算，最终将加工后的数据和指标计算结果进行持久化处理
   
   
   
   2）只对采集的数据进行指标计算，最终将指标计算结果进行持久化处理
   
   
   
   可以将聚合计算结果保存的各种指标数据库：Eleasticsearch/Mongodb/HBase/Clickhouse/Doris/DB(Oracle、Mysql、postgresql、sqlserver等主流关系数据库)
   
# v6.8.0 功能改进
1. 去除Elasticsearch和http数据源jvm退出机制，避免因jvm退出时，数据源获取空指针问题
2. 将Elasticsearch和http数据源状态监控和服务发现线程调整为daemon线程
# v6.7.9 功能改进

1. 增加hbase和MongoDB输出插件，使用案例：

[Elasticsearch-HBase同步案例](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2HBaseDemo.java)

[Database-HBase同步案例](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DB2HbaseDemo.java)

[Database-HBase自定义列簇映射同步案例](https://gitee.com/bboss/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DB2HbaseWithFamilyColumnDemo.java)

[Elasticsearch-MongoDB同步案例](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2MongodbDemo.java)

[Database-MongoDB同步案例](https://gitee.com/bboss/mongodb-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DB2MongodbDemo.java)
2. 数据库数据源增加enableShutdownHook属性，控制是否在jvm退出时自动关闭数据源，默认为false 不关闭 true关闭
3. 修复数据库数据源在jvm退出时的空指针问题
4. 处理增量状态类型值为bigdecimal时的兼容性问题

# v6.7.8 功能改进

1.http输入输出插件改进：修复动态请求头空指针问题

# v6.7.7 功能改进

1. 增加轻量级但功能强大的大数据指标分析计算模块：

可以非常方便地实现基于时间窗口维度的实时指标计算和离线指标计算功能，适用于有限维度指标key和无限维度指标key，同时可以非常方便地将指标分析计算结果存储到各种数据库，以极低成本快速构建企业级大数据分析作业

使用案例

https://gitee.com/bboss/bboss-elastic-tran/tree/master/bboss-datatran-core/src/test/java/org/frameworkset/tran/metrics

2. 增量采集状态表increament_tab结构调整，增加以下字段：

   jobType varchar(500)，用于保存输入插件类型，不同的插件类型对应的值见下面的表格说明，避免不同类型作业加载增量状态时，互相干扰

   

   jobId varchar(500)，用于保存外部设置的作业id，相同的进程内启动多个同类型的输入插件的作业时，必须指定每个作业的jobId,避免各个作业加载增量状态时，互相干扰

   | 插件类型                         | jobType                          | jobId |
   | -------------------------------- | -------------------------------- | ----- |
   | HttpInputDataTranPlugin          | HttpInputDataTranPlugin          | 空    |
   | DBInputDataTranPlugin            | DBInputDataTranPlugin            | 空    |
   | ElasticsearchInputDataTranPlugin | ElasticsearchInputDataTranPlugin | 空    |
   | FileInputDataTranPlugin          | FileInputDataTranPlugin          | 空    |
   | HBaseInputDatatranPlugin         | HBaseInputDatatranPlugin         | 空    |
   | Kafka2InputDatatranPlugin        | Kafka2InputDatatranPlugin        | 空    |
   | MongoDBInputDatatranPlugin       | MongoDBInputDatatranPlugin       | 空    |

   **升级注意事项：**升级前，需要手动修改increament_tab表中的状态记录，根据作业输入插件类型，填写正确的jobType，然后再启动作业，否则作业无法正常工作

3. 优化增量状态管理机制
4. [增加全局JobContext，用于存放在作业中使用的初始化数据](https://gitee.com/bboss/bboss-elastic-tran/commit/54a28c36c56c63d4d164ef2163e5344c9b717eef)
5. 优化kafka输出插件任务状态记录管理功能，采用指标分析模块对发送记录统计信息，按照指定的时间窗口进行聚合计算后在回调任务处理success方法，taskMetrics信息为聚合后的统计信息，可以通过开关控制是否进行预聚合功能

```java
kafkaOutputConfig.setEnableMetricsAgg(true);//启用预聚合功能
kafkaOutputConfig.setMetricsAggWindow(60);//指定统计时间窗口，单位：秒，默认值60秒
```

6. 优化kafka输入插件拦截器功能：定时记录统计插件消费kafka数据记录情况，并调用任务拦截器的aftercall方法输出统计jobMetrics信息，可以指定统计时间间隔：

```java
kafka2InputConfig.setMetricsInterval(300 * 1000L);//300秒做一次任务拦截调用，默认值
```

7. 增加字段映射功能，涉及插件：日志采集插件、excel采集插件、生成日志/excel文件插件、kafka输入插件

文件采集插件字段映射配置示例：

```java
FileInputConfig fileInputConfig = new FileInputConfig();
_fileInputConfig = fileInputConfig;
FileConfig fileConfig = new FileConfig();
    fileConfig.setFieldSplit(";");//指定日志记录字段分割符
  //指定字段映射配置
    fileConfig.addDateCellMapping(0, excelCellMapping.getFieldName(), cellType, excelCellMapping.getDefaultValue(), excelCellMapping.getDataFormat());
               
    fileConfig.addNumberCellMapping(1, excelCellMapping.getFieldName(), cellType, excelCellMapping.getDefaultValue(), excelCellMapping.getDataFormat());
    fileConfig.addCellMappingWithType(2, excelCellMapping.getFieldName(), cellType, excelCellMapping.getDefaultValue());
           
```

kafka映射配置示例：

```java
Kafka2InputConfig kafka2InputConfig = new Kafka2InputConfig();
 
    kafka2InputConfig.setFieldSplit(";");//指定kafka记录字段分割符
  //指定字段映射配置
    kafka2InputConfig.addDateCellMapping(0, //记录切割得到的字段列表位置索引，从0开始
    			excelCellMapping.getFieldName(), //映射的字段名称
                                          cellType, //字段值类型
                                          excelCellMapping.getDefaultValue(), //字段默认值
                                         excelCellMapping.getDataFormat());//字段格式：日期格式或者数字格式
               
    kafka2InputConfig.addNumberCellMapping(1, excelCellMapping.getFieldName(), cellType, excelCellMapping.getDefaultValue(), excelCellMapping.getDataFormat());
    kafka2InputConfig.addCellMappingWithType(2, excelCellMapping.getFieldName(), cellType, excelCellMapping.getDefaultValue());
```

cellType取值范围：

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

8. [增加全局JobContext，用于存放在作业中使用的初始化数据](https://gitee.com/bboss/bboss-elastic-tran/commit/54a28c36c56c63d4d164ef2163e5344c9b717eef)
9. 作业拦截器异常方法参数Exception类型调整为Throwable类型，任务拦截器异常方法参数Exception类型调整为Throwable类型

更多变更，参考提交记录：https://gitee.com/bboss/bboss-elastic-tran/commits/master

# v6.7.6 功能改进

1. 异步批处理增加scriptField功能，通过其指定操作的dsl脚本，使用案例：
```java
   		data = new HashMap<String,Object>();
   		data.put("id",1000);
   		data.put("script","{\"name\":\"duoduo104\",\"goodsid\":104}");
   		clientOptions = new ClientOptions();
   		clientOptions.setIdField("id");
   		clientOptions.setScriptField("script");
   		bulkProcessor.insertData("bulkdemo",data,clientOptions);
   
   		data = new HashMap<String,Object>();
   		data.put("id",1000);
   		data.put("script","{\"name\":\"updateduoduo104\",\"goodsid\":1104}");
   		clientOptions = new ClientOptions();
   		clientOptions.setIdField("id");
   		clientOptions.setScriptField("script");
   		bulkProcessor.updateData("bulkdemo",data,clientOptions);
```
2. https协议支持Elasticsearch官方的三种ssl证书，参考文档：[https协议配置](https://esdoc.bbossgroups.com/#/development?id=_265-https%e5%8d%8f%e8%ae%ae%e9%85%8d%e7%bd%ae)
3. 优化客户端工具类索引字段管理方法，将内置嵌套结构也增加到字段清单中
4. 调整JobTaskMetrics中作业开始时间、作业id、作业名称的设置机制，避免作业执行异常时未正确设置作业信息
5. 修复Elasticsearch输入插件createBaseDataTran方法被重复调用两次问题
6. 文件输入输出插件改进：ftp发送失败文件重试扫描机制、备份文件清理扫描机制改进
7. 文件输入输出插件改进：将生成的文件信息（本地文件路径、ftp文件路径）添加到作业jobmetrics中，获取方法：
```java
   /**
    * 文件导出时特定的文件类型任务上下文，包含了导出文件清单信息
    */
 public void afterCall(TaskContext taskContext) {
   JobTaskMetrics taskMetrics = taskContext.getJobTaskMetrics();
   List<GenFileInfo> genFileInfos = (List<GenFileInfo>) taskMetrics.readJobExecutorData(FileOutputConfig.JobExecutorDatas_genFileInfos);
 }
```

# v6.7.5 功能改进
1. 修复opensearch兼容性 bug
2. 处理文件输入插件，taskContext设置参数不起作用，以及获取不到fileinfo信息问题
3. 处理mvc框架json报文长度为0处理机制
4. 优化数据同步context.getStringValue对数据库clob和blob字段的处理机制

# v6.7.3 功能改进

1. Elasticsearch客户端改进：处理flushsync方法在Elasticsearch8的兼容性问题 ，增加获取elasticsearch版本号方法
2. 框架改进：处理处理jdk 17兼容性问题
3. 框架改进：改进ioc注入机制，避免使用mangerimport嵌套导入其他ioc配置文件存在的死循环问题
4. Elasticsearch客户端改进：增加ESMatchedQueries注解，用于绑定返回_name指定的命名匹配条件数组String[]，参考文档：_[元数据注解](https://esdoc.bbossgroups.com/#/client-annotation?id=_2%e5%85%83%e6%95%b0%e6%8d%ae%e6%b3%a8%e8%a7%a3)

# v6.7.2 功能改进

1. 数据同步bug修复： 执行destroy方法销毁作业时空指针异常问题修复
2. 数据同步改进：优化作业销毁机制
3. 数据同步改进：优化filelog插件日志采集多行识别处理增量采集机制和未结束多行记录回滚机制
4. 数据同步改进：优化kafka输入插件并行消息处理机制

# v6.7.1 功能改进
1. 作业配置增加[输入输出参数配置](https://esdoc.bbossgroups.com/#/db-es-tool?id=%e4%bd%9c%e4%b8%9a%e5%9f%ba%e7%a1%80%e9%85%8d%e7%bd%ae)（动态和静态） 
2. [http输入输出插件](https://esdoc.bbossgroups.com/#/datatran-http)增加http header设置功能（静态header和动态header），
3. http输入输出插件增加jwt服务认证以及对数据签名功能
4. http输出插件 增加对dsl脚本的支持

# v6.7.0 功能改进
1. 数据同步改造：DB导出插件改进，可以为sql语句额外指定同步条件进行全量或者定时增量导入

定时按特定条件导入数据
```java
importBuilder.setSql("select * from batchtest1 where optime >= #[start_optime] and optime < #[end_optime]");

		importBuilder.addParam("start_optime", TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2018-03-21 00:27:21"))
				.addParam("end_optime",TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2019-12-30 00:27:21"));
```

定时按特定条件增量导入数据
```java
importBuilder.setSql("select * from batchtest1 where optime >= #[start_optime] and optime < #[end_optime] and collecttime > #[collecttime]");
		importBuilder.setLastValueColumn("collecttime");
		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);

		importBuilder.addParam("start_optime", TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2018-03-21 00:27:21"))
				.addParam("end_optime",TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2019-12-30 00:27:21"));
```
使用参考文档：

https://esdoc.bbossgroups.com/#/db-es-datasyn

2. 数据同步改进：完善作业监控输出日志信息，完善es 数据采集dsl扩展参数管理机制，提升到所有插件的参数配置
3. 数据同步架构重构：去掉所有源到目标builder，统一使用ImportBuilder构建器+InputConfig+OutputConfig来构建数据同步作业
4. 一次性作业支持延时时间配置和开始时间配置
5. jdk timer作业支持设置作业运行截止时间，截止时间对quartz和xxl-job调度作业不起作用,对kafka作业、文件采集探针不起作用
6. 数据同步改进：增加http输入和输出插件，使用参考文档：
    https://esdoc.bbossgroups.com/#/datatran-http
7. 增加数据同步作业开发gradle模板工程
    https://gitee.com/bboss/bboss-datatran-demo

由于bboss6.7.0版本对整个数据同步架构做了很大的改进调整，去掉旧版本中的“源-目标builder”作业构建器，统一采用“ImportBuilder构建器+InputConfig+OutputConfig“架构来构建数据同步作业，特制作了系列升级教程，帮助大家将旧版本开发的作业升级到最新版本。



版本升级视频教程访问地址：
https://space.bilibili.com/2036297302/


# v6.6.0 功能改进
1. 数据同步改造：ExcelFileOupputConfig增加flushRows属性，设置excel临时文件记录大小，达到flushRows时，将内存中的excel记录写入临时文件，默认5000条记录
2. 数据同步改造：处理Excel poi包升级后的兼容性问题


# v6.5.9 功能改进
1. 数据同步改造：增加暂停任务调度和继续开始调度功能，使用文档：[作业调度控制](https://esdoc.bbossgroups.com/#/bboss-datasyn-control)
2. 优化文件采集插件性能，增加sleepAwaitTimeAfterCollect和sleepAwaitTimeAfterFetch配置 

sleepAwaitTimeAfterFetch	long,单位：毫秒 ,从文件采集（fetch）一个batch的数据后，休息一会，避免cpu占用过高，在大量文件同时采集时可以设置，大于0有效，默认值0	

sleepAwaitTimeAfterCollect  long,单位：毫秒 ，从文件采集完成一个任务后，休息一会，避免cpu占用过高，在大量文件同时采集时可以设置，大于0有效，默认值0
3. 修复6.5.8导致的包依赖问题

# v6.5.8 功能改进
1. 采用外部数据源管理增量状态时，停止作业后重启作业失败问题处理
2. 优化同时向多个elasticsearch写入数据功能
3. sqlite增量管理机制改进：bboss默认采用sqlite保存增量状态，通过setLastValueStorePath方法设置sqlite数据库文件路径

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
4. 优化IP地址解析性能
5. 改进批量bulk操作filter_path配置，默认不设置filter_path，如果需要设置，可以参考文档中涉及批处理内容

https://esdoc.bbossgroups.com/#/document-crud

https://esdoc.bbossgroups.com/#/bulkProcessor

# v6.5.7 功能改进
1. 数据同步改进：JobTaskMetrics和TaskMetrics增加lastValue属性，用于存放任务执行完毕后的增量状态
2. 全局属性改进：增加属性配置解析拦截器PropertiesInterceptor，通过PropertiesInterceptor对加载后的属性值进行自定义处理，比如加密属性解密处理
3. 优化属性配置变量解析机制

# v6.5.6 功能改进
1. 数据同步改进：增加elasticsearch数据同步到自定义处理器功能
2. 增加增加elasticsearch数据同步到redis案例（批处理和单条处理）

# v6.5.5 功能改进
1. 数据同步机制优化：各插件tran逻辑复用优化
2. ftp/sftp文件下载锁优化，大幅提升文件采集插件性能
3. 增加ftp/sftp文件并行下载机制，通过setDownloadWorkThreads实现并行下载线程数，默认为3个，如果设置为0代表串行下载
```java
FtpConfig ftpConfig = new FtpConfig().setFtpIP("127.0.0.1").setFtpPort(21)
				.setFtpUser("ecsftp").setFtpPassword("ecsftp").setDownloadWorkThreads(4)//设置4个线程并行下载文件，可以允许最多4个文件同时下载
				.setRemoteFileDir("xcm").setRemoteFileValidate(new RemoteFileValidate() {
					/**
					 * 校验数据文件合法性和完整性接口

					 * @param validateContext 封装校验数据文件信息
					 *     dataFile 待校验零时数据文件，可以根据文件名称获取对应文件的md5签名文件名、数据量稽核文件名称等信息，
					 *     remoteFile 通过数据文件对应的ftp/sftp文件路径，计算对应的目录获取md5签名文件、数据量稽核文件所在的目录地址
					 *     ftpContext ftp配置上下文对象
					 *     然后通过remoteFileAction下载md5签名文件、数据量稽核文件，再对数据文件进行校验即可
					 *     redownload 标记校验来源是否是因校验失败重新下载文件导致的校验操作，true 为重下后 文件校验，false为第一次下载校验
					 * @return int
					 * 文件内容校验成功
					 * 	RemoteFileValidate.FILE_VALIDATE_OK = 1;
					 * 	校验失败不处理文件
					 * 	RemoteFileValidate.FILE_VALIDATE_FAILED = 2;
					 * 	文件内容校验失败并备份已下载文件
					 * 	RemoteFileValidate.FILE_VALIDATE_FAILED_BACKUP = 3;
					 * 	文件内容校验失败并删除已下载文件
					 * 	RemoteFileValidate.FILE_VALIDATE_FAILED_DELETE = 5;
					 */
					public Result validateFile(ValidateContext validateContext) {
//						if(redownload)
//							return Result.default_ok;
////						return Result.default_ok;
//						Result result = new Result();
//						result.setValidateResult(RemoteFileValidate.FILE_VALIDATE_FAILED_REDOWNLOAD);
//						result.setRedownloadCounts(3);
//						result.setMessage("MD5校验"+remoteFile+"失败，重试3次");//设置校验失败原因信息
//						//根据remoteFile的信息计算md5文件路径地址，并下载，下载务必后进行签名校验
//						//remoteFileAction.downloadFile("remoteFile.md5","dataFile.md5");
//						return result;
						return Result.default_ok;
					}
				})
```
4. [完善数据同步作业任务监控指标统计信息](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2317-%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%9f%e8%ae%a1%e4%bf%a1%e6%81%af%e8%8e%b7%e5%8f%96)
5. 增加数据批量/串行同步写入[redis案例](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo?id=_11-%e4%bb%8esftp%e6%9c%8d%e5%8a%a1%e5%99%a8%e9%87%87%e9%9b%86excel%e6%96%87%e4%bb%b6%e5%86%99%e5%85%a5redis%e6%a1%88%e4%be%8b)
6. 增加[远程数据文件校验机制](https://esdoc.bbossgroups.com/#/bboss-datasyn-demo?id=_11-%e4%bb%8esftp%e6%9c%8d%e5%8a%a1%e5%99%a8%e9%87%87%e9%9b%86excel%e6%96%87%e4%bb%b6%e5%86%99%e5%85%a5redis%e6%a1%88%e4%be%8b)，以实现对数据文件md5签名校验、记录数校验等功能
7. 完善数据加工处理：context getValue方法可以获取解析后的日志文件记录字段值
8. 对作业启动日志中的数据源口令进行脱敏处理



# v6.5.2 功能改进
1. 数据同步改进：可以指定日期增量字段日期格式，当增量字段为日期类型且日期格式不是默认的
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
2. 大数据同步：postgresql fetchsize机制支持改进

3. 优化es客户端多数据源管理功能：spring boot多数据源启动加载顺序问题处理

4. sql模板变量#[xxxxx]支持设置数据字段sql类型，通过type属性设置数值类型，使用案例：

   ```sql
   #[collecttime,type=timestamp]
   ```
完整的sql
   ```xml   
       <property name="insertSQLpostgresql">
           <![CDATA[INSERT INTO batchtest1 (id, name, author, content, title, optime, oper, subtitle, collecttime,testInt,ipinfo)
                   VALUES ( #[id],#[operModule],  ## 来源dbdemo索引中的 operModule字段
                            #[author], ## 通过datarefactor增加的字段
                            #[logContent], ## 来源dbdemo索引中的 logContent字段
                            #[title], ## 通过datarefactor增加的字段
                            #[logOpertime], ## 来源dbdemo索引中的 logOpertime字段
                            #[logOperuser],  ## 来源dbdemo索引中的 logOperuser字段
                            #[subtitle], ## 通过datarefactor增加的字段
                            #[collecttime,type=timestamp], ## 通过datarefactor增加的字段
                            #[testInt], ## 通过datarefactor增加的字段
                            #[ipinfo]) ## 通过datarefactor增加的地理位置信息字段
   ]]></property>  
       
   ```

   type属性值范围：

   ```java
         string,int,long,double,float,short,date,timestamp,bigdecimal,boolean,byte,blobfile,blob,clobfile,clob,object
   ```

   

# v6.5.1 功能改进

1. 处理增量时间状态值写入mysql管理的增量状态数据库失败问题
2. 时间转换优化:localdatetime和localdate向date类型转换，避免出错误
3. 数据同步作业调度增加对xxl-job 2.3.0的支持，需将原maven坐标bboss-elasticsearch-rest-jdbc或者bboss-datatran-jdbc调整为bboss-datatran-schedule-xxljob

示例如下：
xxl-job 2.3.0以下版本采用的maven坐标

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-datatran-jdbc</artifactId>
            <version>7.3.2</version>
        </dependency>
```
调整为xxl-job 2.3.0及更高版本采用的maven坐标：
```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-datatran-schedule-xxljob</artifactId>
            <version>7.3.2</version>
        </dependency>
```
xxl job 低版本案例工程

https://github.com/bbossgroups/db-elasticsearch-xxjob

xxl job 2x案例工程

https://github.com/bbossgroups/db-elasticsearch-xxjob2x
4. 文件采集插件改进：FileConfig/FtpFileConfig增加忽略文件开始行数设置,0或者小于0不起作用                        
   
       private int skipHeaderLines;
   
5. 写入文件插件支持添加标题行功能：在生成csv文件或者其他需要头部行的文件可以使用，参考案例：

      

https://gitee.com/bboss/csv-dbhandle/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/DB2CSVFile.java

6. 增加excel文件采集功能:可以采集本地excel文件，亦可以从ftp/sftp服务器下载excel文件并采集，参考案例：

   

   https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/ExcelFile2DBDemo.java

7. 增加生成excel文件功能：生成excel，亦可以将生成的excel文件上传到ftp服务器  

生成excel 文件

https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2ExcelFile.java



生成excel文件并上传ftp服务器      


https://gitee.com/bboss/elasticsearch-file2ftp/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/db/DB2ExcelFile2Ftp.java               

# v6.5.0 功能改进

1. filelog插件添加子目录/ftp子目录/sftp子目录下日志文件采集功能
2. 对filelog插件文件选择过滤器FileFilter接口方法accept进行了重构，增加目录和文件区分标识对象FilterFileInfo，以适配本地目录、ftp和sftp三种场景，调整如下

   重构前

```java
 public boolean accept(String parentDir,String fileName, FileConfig fileConfig)
```

   重构后

```java
 public boolean accept(FilterFileInfo filterFileInfo, //包含Ftp文件名称，文件父路径、是否为目录标识
                                            FileConfig fileConfig)
```

使用案例

```java
fileConfit.setFileFilter(new FileFilter() {//指定ftp文件筛选规则
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
```

**因此升级到7.3.2时需要对采集作业的FileFilter接口方法accept进行相应调整**

3. db管理dsl mysql无法创建加载dsl问题处理
4. log4j2版本升级2.17.1、slfj版本升级1.7.32
5. 修复空行处理器Record问题：关闭key大写机制后，根据字段名称获取数据失效
6. 忽略mysql stream机制情况下获取rowid失败异常
7. 增加excel csv文件采集案例

https://github.com/bbossgroups/csv-dbhandle

https://gitee.com/bboss/csv-dbhandle
8. 优化运行容器工具，增加从环境变量、jvm属性配置检索mainclass功能

 默认使用org.frameworkset.elasticsearch.imp.DB2CSVFile作为作业主程序，

 如果设置了环境变量mainclassevn，则使用mainclassevn作为作业主程序

 环境变量名称不能和属性名称一致，否则报循环引用异常，并将原始值返回

  mainclass=#[mainclassevn:org.frameworkset.elasticsearch.imp.DB2CSVFile]

使用参考文档：

https://my.oschina.net/bboss/blog/469411

9. 升级mysql驱动版本号为8.0.28

10. 增加通用异步批处理组件

使用案例：

https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/bulkprocessor/PersistentBulkProcessor.java

使用文档

https://esdoc.bbossgroups.com/#/bulkProcessor-common



# v6.3.9 功能改进
1. 修复db-es数据同步时，指定了任务拦截器，但是处理任务上下文中没有指定任务级别的sql语句时空指针问题
2. bboss安全过滤器改造：增加xss攻击和敏感词攻击策略配置
3. 运行容器工具改进：完善[运行容器工具](https://my.oschina.net/bboss/blog/469411)，增加启动bootrap类，负责运行、停止、重启mainclass，并将mainclass运行、停止、重启过程中的日志、异常输出到log日志文件


  导入微服务容器组件包：由bboss-rt调整为bboss-bootstrap-rt

  **gradle坐标**

  Java代码

  ```java
  group: 'com.bbossgroups', name: 'bboss-bootstrap-rt', version: "6.2.8",transitive: true 
  ```

  **maven坐标**

  Xml代码

  ```xml
  <dependency>  
      <groupId>com.bbossgroups</groupId>  
      <artifactId>bboss-bootstrap-rt</artifactId>  
      <version>6.2.8</version>  
  </dependency>  
  ```
4. 运行容器工具改进：停止进程时需等待进程停止完毕再退出

# v6.3.8 功能改进
1. 日志完善：对httpproxy和elasticsearch客户端输出日志中的用户口令信息进行脱敏处理
2. 兼容老版本升级到最新的数据同步框架：自动创建增量状态表和增量状态历史表中新增的字段
3. 修复httpproxy问题：停止默认连接池时，没有清空默认配置对象
4. 完善数据同步异常处理机制：捕获插件初始化异常并输出到日志文件

# v6.3.7 功能改进

1. elasticsearch客户端改进：多数据源支持数据源引用功能，如果两个数据源都指向同一个数据源，则可以将第二个数据源指向第一个数据源，配置示例：

普通项目

```properties
elasticsearch.referExternal=default
```

spring boot项目

```properties
spring.elasticsearch.bboss.elasticsearch.referExternal=default
```



2. 数据源同步改进：增加自定义定时同步调度机制，可以指定作业执行的时间段（支持指定多个时间段）和忽略执行时间段（支持指定多个时间段），使用案例：

```java
		//定时任务配置，
		importBuilder.setScheduleSelf()//使用bboss自带的定时器,bboss timer
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(1*60*1000l)//每隔period毫秒执行，如果不设置，只执行一次
				.addScanNewFileTimeRange("12:37-23:59");//添加每天调度执行的时间段，可以调用多次addScanNewFileTimeRange方法添加多个时间段
				//添加每天排除的时间段（不调度执行作业），可以调用多次addSkipScanNewFileTimeRange方法添加多个时间段,设置addScanNewFileTimeRange，则SkipScanNewFileTimeRange不起作用
//				.addSkipScanNewFileTimeRange("11:30-13:00");
		//定时任务配置结束
```

如果是Filelog插件，还需要额外指定：
```java
		FileImportConfig config = new FileImportConfig();
		/**
        * 设置是否采用外部新文件扫描调度机制：bboss timer,jdk timer,quartz,xxl-job
        * true 采用，false 不采用，默认false
        */
        config.setUseETLScheduleForScanNewFile(true);		
```
3. 在任务CallInterceptor.preCall中，可以根据taskContext中对应的不同的文件指定不同数据库添加、修改、删除sql,使用参考案例：
```java
//导出到数据源配置
		DBConfigBuilder dbConfigBuilder = new DBConfigBuilder();
		dbConfigBuilder
				.setSqlFilepath("sql-dbtran.xml")//指定sql配置文件地址
				.setTargetDbName("test");//指定目标数据库，在application.properties文件中配置

		importBuilder.setOutputDBConfig(dbConfigBuilder.buildDBImportConfig());
		importBuilder.addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				FileTaskContext fileTaskContext = (FileTaskContext)taskContext;
				String filePath = fileTaskContext.getFileInfo().getOriginFilePath();
				/**
				 * 根据文件名称指定插入数据库的sql语句
				 */
				if(filePath.endsWith("metrics-report.log")) {
					DBConfigBuilder dbConfigBuilder = new DBConfigBuilder();
					dbConfigBuilder.setInsertSqlName("insertSql");//指定新增的sql语句名称，在配置文件中配置：sql-dbtran.xml

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
4. 修改客户端方法过载不正确问题：getDocumentByField/getDocumentByFieldLike/searchListByField/searchListByFieldLike

# v6.3.6 功能改进
1. 数据同步改进：增加记录切割功能，可以将指定的字段拆分为多条新记录，新产生的记录会自动继承原记录其他字段数据，亦可以指定覆盖原记录字段值
使用案例：

```java
        importBuilder.setSplitFieldName("@message");
		importBuilder.setSplitHandler(new SplitHandler() {
			/**
			 * 如果方法返回null，则继续将原记录写入目标库
			 * @param taskContext
			 * @param record
			 * @param splitValue
			 * @return List<KeyMap<String, Object>> KeyMap是LinkedHashMap的子类，定义key字段，如果是往kafka推送数据，可以设置推送的key
			 */
			@Override
			public List<KeyMap<String, Object>> splitField(TaskContext taskContext,//调度任务上下文
														   Record record,//原始记录对象
														   Object splitValue) {//待切割的字段值
//				Map<String,Object > data = (Map<String, Object>) record.getData();//获取原始记录中包含的数据对象
				List<KeyMap<String, Object>> splitDatas = new ArrayList<>();
				//模拟将数据切割为10条记录
				for(int i = 0 ; i < 10; i ++){
					KeyMap<String, Object> d = new KeyMap<String, Object>();//创建新记录对象
					d.put("id",i+"-" + record.getValue("id"));//用新的id值覆盖原来的唯一标识id字段的值
					d.put("message",i+"-"+splitValue);//我们只切割splitValue到message字段，继承原始记录中的其他字段
//					d.setKey(SimpleStringUtil.getUUID());//如果是往kafka推送数据，可以设置推送的key
					splitDatas.add(d);
				}
				return splitDatas;
			}
		});
		importBuilder.addFieldMapping("@message","message");
```
2. 数据同步功能：扩展filelog插件，增加对ftp日志文件下载采集支持，支持实时监听下载ftp目录下生成的日志文件，
       
   将ftp文件中的数据采集写入elasticsearch、数据库、推送kafka、写入新的日志文件，参考案例：
[FtpLog2ESETLScheduleDemo.java](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/FtpLog2ESETLScheduleDemo.java)
[FtpLog2ESDemo](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/FtpLog2ESDemo.java)
3. 数据同步功能：支持备份采集完毕日志文件功能，可以指定备份文件保存时长，定期清理超过时长文件
4. 数据同步功能：提供自定义处理采集数据功能，可以自行将采集的数据按照自己的要求进行处理到目的地，支持数据来源包括：database，elasticsearch，kafka，mongodb，hbase，file，ftp等，想把采集的数据保存到什么地方，有自己实现CustomOutPut接口处理即可

```java
FileLog2DummyExportBuilder importBuilder = new FileLog2DummyExportBuilder();
//自己处理数据
importBuilder.setCustomOutPut(new CustomOutPut() {
   @Override
   public void handleData(TaskContext taskContext, List<CommonRecord> datas) {

      //You can do any thing here for datas
      for(CommonRecord record:datas){
         Map<String,Object> data = record.getDatas();
         logger.info(SimpleStringUtil.object2json(data));
      }
   }
});
```

自定义处理采集数据功能典型的应用场景就是对接大数据流处理，直接将采集的数据交给一些流处理框架，譬如与我们内部自己开发的大数据流处理框架对接，效果简直不要不要的，哈哈。

[采集日志文件自定义处理案例](https://gitee.com/bboss/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/FileLog2CustomDemo.java)
5. Elasticsearch客户端：ClientInterface增加一组指定elasticsearch datasource名称的方法，详情如下：

https://gitee.com/bboss/bboss-elastic/blob/master/bboss-elasticsearch-rest/src/main/java/org/frameworkset/elasticsearch/client/ClientInterfaceWithESDatasource.java

# v6.3.5 功能改进

1. 数据同步改进：filelog插码优化

# v6.3.3 功能改进
1. 数据同步改进：处理异步更新状态可能导致的死锁问题
2. 数据同步改进：处理在closeEOF为true情况下filelog插件重启后不采集数据问题和filelog插件不采集新增文件数据问题
3. 数据同步改进：优化作业停止资源处理机制
4. 数据同步改进：优化作业状态管理机制
5. 数据同步改进：filelog插件增加FileFilter机制，自定义筛选需要采集日志的文件

# v6.3.2 功能改进

1. 数据同步改进：启用日志文件采集探针closeOlderTime配置，允许文件内容静默最大时间，单位毫秒，如果在idleMaxTime访问内一直没有数据更新，认为文件是静默文件，将不再采集静默文件数据，关闭文件对应的采集线程，作业重启后也不会采集
2. 数据同步改进：日志文件采集插件增加对CallInterceptor的支持，采集文件任务新增/结束时会调用拦截器方法，可以在refactor方法中获取拦截器设置的数据，文件采集完毕后释放
3. 数据同步工具完善：修复同步数据到kafka productor初始化问题
4. 数据同步工具完善：修复停止filelog作业报错问题
5. 数据同步工具改进：发送kafka控件改进，设置发送多少条消息后打印发送统计信息

# v6.3.1 功能改进

1. elasticsearch rest client改进：使用params中的参数变量，解析配置文件中dslName对应的dsl语句，并返回解析结果

```java
        ClientInterface util = (ConfigRestClientUtil) ElasticSearchHelper.getConfigRestClientUtil("demo7.xml");
		Map params = new HashMap();
		params.put("aaa","_&/+\"\\.");
		System.out.println(util.evalConfigDsl("testesencode",params));
```
2. 数据同步工具改进：日志采集探针，字符串maxBytes为0或者负数时忽略长度截取
3. 日志采集探针，增加忽略条件匹配类型：文件记录包含与排除条件匹配类型
       REGEX_MATCH("REGEX_MATCH"),REGEX_CONTAIN("REGEX_CONTAIN"),STRING_CONTAIN("STRING_CONTAIN"),
       	STRING_EQUALS("STRING_EQUALS"),STRING_PREFIX("STRING_PREFIX"),STRING_END("STRING_END");
       	使用案例：

```java  	
  				config.addConfig(new FileConfig(logPath,//指定目录
    							fileName+".log",//指定文件名称，可以是正则表达式
    							startLabel)//指定多行记录的开头识别标记，正则表达式
    							.setCloseEOF(false)//已经结束的文件内容采集完毕后关闭文件对应的采集通道，后续不再监听对应文件的内容变化
    							.addField("tag",fileName.toLowerCase())//添加字段tag到记录中
    							.setEnableInode(true)
    							.setIncludeLines(levelArr, LineMatchType.STRING_CONTAIN)
```
4. 数据同步工具改进：默认采用异步机制保存增量同步数据状态，提升数据同步效率，可以通过以下机制关闭异步机制：

importBuilder.setAsynFlushStatus(false);

5. 客户端改进：增加dsl输出组件logDslCallback
   通过组件logDslCallback，通过回调接口方法可以自定义采集dsl的执行信息：

```java
public void logDsl(LogDsl logDsl);
```
参数LogDsl封装了以下信息

 ```java
    /**
	 * 慢dsl输出阈值
	 */
	private  int slowDslThreshold;
	
	/**
	 * elasticsearch rest http服务请求地址
	 */
	private String url;
	/**
	 * http request method：post,get,put,delete
	 */
	private String action;
	/**
	 * request handle elapsed ms
	 */
	private long time;
	/**
	 * elasticsearch dsl
	 */
	private  String dsl;
	/**
	 * request handle begin time.
	 */
	private Date startTime;
	/**
	 * request handle end time.
	 */
	private Date endTime;

	/**
	 * 0 - dsl执行成功
	 * 1 - dsl执行异常
	 */
	private int resultCode;
 ```

使用方法：
组件LogDslCallback实现接口org.frameworkset.elasticsearch.client.LogDslCallback

然后在配置文件中进行配置：

非spring boot项目

elasticsearch.logDslCallback=org.frameworkset.elasticsearch.client.LoggerDslCallback

springboot项目

spring.elasticsearch.bboss.elasticsearch.logDslCallback=org.frameworkset.elasticsearch.client.LoggerDslCallback

6. 客户端改造：将SlowDslCallback和LogDslCallback两个接口合并，保留接口LogDslCallback，dsl信息采集


# v6.3.0 功能改进
1. elasticsearch rest client改进：优化批处理性能，执行批处理bulk操作后，默认只返回三个信息：took,errors,items.*.error，既耗时、错误标记、错误记录信息
2. 数据同步功能改进：日志文件采集插件添加控制是否删除采集完的文件控制变量，默认false 不删除，true 删除
3. 数据同步功能bug修复：修复hbase数据导出因columns信息为空导致的导出异常
4. 数据同步功能bug修复：修改es2db导出时存在targetdb空指针问题
5. 数据同步功能改进：增加采集日志文件数据，导出到文件并上传ftp/sftp服务器功能
6. 数据同步功能改进：从kafka接收数据，处理后按照固定记录条数导出到文件并上传ftp/sftp服务器功能
7. 数据同步功能改进：增加hbase数据导出到文件并上传ftp/sftp服务器功能
8. 数据同步功能改进：增加mongodb数据导出到文件并上传ftp/sftp服务器功能
9. 数据同步功能改进：增加hbase、mongodb到dummy/logger的输出功能
10. 数据同步功能改进：增加日志文件数据采集到dummy/logger的输出功能
11. 数据同步功能改进：增加kafka到dummy/logger输出功能
12. 数据同步工具改进：增加kafka、hbase、mongodb到kafka的数据抽取同步功能
14. 数据同步功能改进：增加hbase到database数据同步功能
15. 数据同步功能改进：增加数据库/elasticsearch数据导出（增量/全量）到log4j日志文件dummy插件

**说明：**数据同步功能新增的dummy插件，便于调试采集数据作业，将采集的数据打印到控制台，观察数据的正确性



# v6.2.9 功能改进
1. 数据同步改进：完善ip2region和geoip数据库热加载机制

2. 升级httpcliet组件版本到最新的官方版本4.5.13

3. 升级fastxml jackson databind版本2.9.10.8

4. 增加对pit机制的支持，参考用例：
   
   testPitId方法
   
   https://gitee.com/bboss/eshelloword-spring-boot-starter/blob/master/src/test/java/org/bboss/elasticsearchtest/springboot/SimpleBBossESStarterTestCase.java
   
5. 数据同步工具扩展：增加日志文件采集插件，支持全量和增量采集两种模式，实时采集日志文件数据到kafka/elasticsearch/database
   
   使用文档：https://esdoc.bbossgroups.com/#/filelog-guide

   日志文件采集插件使用案例：
   1. [采集日志数据并写入数据库](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/FileLog2DBDemo.java)
   2. [采集日志数据并写入Elasticsearch](https://github.com/bbossgroups/filelog-elasticsearch/blob/main/src/main/java/org/frameworkset/datatran/imp/FileLog2ESDemo.java)  
   3. [采集日志数据并发送到Kafka](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Filelog2KafkaDemo.java)
   
   升级6.3.2注意事项，需手动修改增量同步状态表结构，增加下面三个字段：
   
   ```
   status number(1) ,  //数据采集完成状态：0-采集中（默认值）  1-完成  适用于文件日志采集 默认值 0
   filePath varchar(500)  //日志文件路径，默认值""
   fileId varchar(500)  //日志文件indoe标识，默认值""
   ```

6. 设每个elasticsearch数据源默认版本兼容性为7，为了处理启动时无法连接es的情况，可以根据连接的es来配置和调整每个elasticsearch数据源的配置，示例如下：
   elasticsearch.version=7.12.0
   
   如果正常连上elasticsearch，侧会使用elasticsearch实际的版本信息，忽略配置的版本信息
   
7. 调整gradle构建脚本语法，保持与gradle 7的兼容性   

8. elasticsearch节点自动发现和故障节点健康检查后台线程模型调整为daemon模式

9. http-proxy节点自动发现和故障节点健康检查后台线程模型调整为daemon模式

# v6.2.8 功能改进
1. 数据同步工具改进：Elasticsearch-File-Ftp/Sftp数据同步时，全局配置/记录级别添加的自定义字段不起作用问题修复
2. 数据同步工具扩展：增加elasticsearch数据导出发送到kafka模块，使用案例：
   https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/ES2KafkaDemo.java
3. 数据同步工具扩展：增加关系数据库数据导出发送到kafka模块，使用案例：
   https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/DB2KafkaDemo.java

# v6.2.7 功能改进
1. 数据同步工具改进：增加Elasticsearch-File-Ftp/Sftp数据同步上传功能
2. 数据同步工具改进：增加Database-File-Ftp/Sftp数据同步上传功能

​    es和数据库数据导出到文件并上传到ftp和sftp案例：

https://github.com/bbossgroups/elasticsearch-file2ftp

# v6.2.6 功能改进

1. 数据同步工具功能改进：database到database数据同步增加修改和删除数据的同步，参考案例：[Db2DBdemoWithStatusConfigDB](https://github.com/bbossgroups/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemoWithStatusConfigDB.java)
2. 数据同步工具功能改进：增加kafka到database数据同步插件，支持增加、修改、删除操作同步,参考案例：[Kafka2DBdemo](https://github.com/bbossgroups/kafka2x-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/Kafka2DBdemo.java)
3. 持久层orm时，针对map类型时，通过columnLableUpperCase控制列名不进行大写转换处理，默认true，转换列名为大写，false 不转换
4. 数据同步工具改进：优化数据同步机制，剔除冗余逻辑

# v6.2.5 功能改进
1. 改进Elasticsearch rest client负载均衡调度机制：如果所有节点都被标记为不可用时，可以通过控制开关设置返回故障节点用于处理请求，如果请求能够被正常处理则将节点标记为正常节点
默认值true
非spring boot项目配置
```properties
        elasticsearch.failAllContinue = true
```
spring boot配置项
```properties
        spring.elasticsearch.bboss.elasticsearch.failAllContinue = true
```

2. 改进http-proxy负载均衡调度机制：如果所有节点都被标记为不可用时，可以通过控制开关设置返回故障节点用于处理请求，如果请求能够被正常处理则将节点标记为正常节点
    默认值true
非spring boot项目配置
```properties
        http.failAllContinue = true
```
spring boot配置项
```properties
        spring.bboss.http.failAllContinue = true
```

还需进一步优化http-proxy:代码去重，如果失败节点能够正常处理请求，则需要将故障节点状态设置为正常节点状态
# v6.2.3 功能改进
1. 数据同步模块改进：增加对开源ip地址库ip2region的支持，使用参考文档
 [IP-地区运营商经纬度坐标转换](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2311-ip-地区运营商经纬度坐标转换)
2. 完善http 响应报文处理机制，增加响应报文为空时的判断和处理 
3. 完善http-proxy rpc异常信息，添加rpc url信息到异常消息中

# v6.2.2 功能改进
1. 处理问题：执行_sql?format=txt，格式为txt时，返回结果中文乱码问题，json格式没有问题
   
   处理方法如下：增加ESStringResponseHandler参数，设置字符编码为UTF-8,列如
   ```java
   ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
   String json = clientUtil.executeHttp("_sql?format=txt",
   				"{\"query\": \"SELECT * FROM dbclobdemo\"}",
   				ClientInterface.HTTP_POST,new ESStringResponseHandler("UTF-8")
   				);
   System.out.println(data);
   ```
2. 完善http 响应报文处理机制，增加响应报文为空时的判断和处理   
# v6.2.1 功能改进
1. 数据同步改进：增加原始quartz调度作业基础类org.frameworkset.tran.schedule.quartz.BaseQuartzDatasynJob
2. dsl模板变量增加esEncode属性，说明如下：
 **esEncode** boolean 类型，默认值false（不转义elasticsearch操作符），true（转义elasticsearch操作符），用于在query_string中对应不要作为elasticsearch操作符字符（\+ - = && || ! ( ) { } [ ] ^ " ~ * ? : \ /）进行转义处理，例如：

  ```json
  {
    "query" : {
      "query_string" : {
        "query" : "kimchy\\!",
        "fields"  : ["user"]
      }
    }
  }
  ```

​      使用bboss时只需要传入变量值kimchy!，然后用#[xxx,esEncode=true]可控制操作符转义处理：

```json
{
  "query" : {
    "query_string" : {
      "query" : #[condition,esEncode=true],   ## condition变量传入的值为kimchy!
      "fields"  : ["user"]
    }
  }
}
```


# v6.2.0 功能改进
1. es客户端改进：可以通过apollo配置中心设置elasticsearch节点自动发现和动态切换Dsl日志打印开关监听器，参考文档：https://esdoc.bbossgroups.com/#/apollo-config
2. es客户端改进：增加动态发现es节点方法，参考文档：[Elasticsearch节点被动发现模式](https://esdoc.bbossgroups.com/#/development?id=_232-被动发现模式)
3. es客户端改进：增加动态设置打印dsl控制方法，参考文档：[动态切换dsl日志打印](https://esdoc.bbossgroups.com/#/development?id=_243-动态切换dsl日志打印)
4. es客户端改进：dsl片段支持多行sql脚本和多行script脚本：通过在片段property上指定escapeQuoted="false"来实现：

```xml
   <!--
           通用sql字段列表，可以被其他sql引用 
   
   -->
   <property name="sqlPianduan" escapeQuoted="false">
       <![CDATA[
         #"""
           channelId,
           application,
           applicationName,
           address,
           timeDate,
           day
         """
       ]]>
   </property>
   <!--
       分页sql query
       每页显示 fetch_size对应的记录条数
   
   -->
   <property name="sqlPagineQueryUsePianduan">
       <![CDATA[
        {
        ## 指示sql语句中的回车换行符会被替换掉开始符,注意dsl注释不能放到sql语句中，否则会有问题，因为sql中的回车换行符会被去掉，导致回车换行符后面的语句变道与注释一行
        ##  导致dsl模板解析的时候部分sql段会被去掉
           "query": #"""
                   SELECT
                   @{sqlPianduan}
                   FROM dbclobdemo
                   where channelId=#[channelId]
            """,
            ## 指示sql语句中的回车换行符会被替换掉结束符
           "fetch_size": #[fetchSize]
        }
       ]]>
   </property>

```

5. httpproxy改进：增加路由规则动态切换api，可以监听路由规则动态变化

6. httpproxy改进：增加http proxy api监听路由变化和节点变化

7. httpproxy改进使用参考文档：[服务发现机制的两种工作模式](https://esdoc.bbossgroups.com/#/httpproxy?id=_4服务发现机制的两种工作模式)

   

# v6.2.0 功能改进
1. 优化bulkproccessor：jvm退出时，同时关闭bulkprocessor flush线程
2. 完善dsl打印机制:打印dsl的时候，会同时把接收dsl的elasticsearch 节点url地址，重试次数打印出来
3. 非spring boot项目支持通过apollo来管理客户端配置，只需要将maven坐标做如下处理即可

```xml
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-datatran-jdbc</artifactId>
    <version>7.3.2</version>
    <!--排除bboss-elasticsearch-rest-booter包-->
    <exclusions>
        <exclusion>
            <artifactId>bboss-elasticsearch-rest-booter</artifactId>
            <groupId>com.bbossgroups.plugins</groupId>
        </exclusion>
    </exclusions>
</dependency>
<!--导入bboss apollo插件-->
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-plugin-apollo</artifactId>
    <version>5.7.8</version>
</dependency>
```

在应用resources/META-INF目录下面增加app.properties文件，内容如下：

```properties
# apollo应用id
app.id=visualops
# apollo应用地址
apollo.meta=http://127.0.0.1:8080
```

在resources/conf下新增文件elasticsearch-boot-config.xml，内容如下：

```xml
<properties>
    <!--
       指定apollo属性配置namespace
    -->

    <config apolloNamespace="application"/>
 </properties>
```

在C:\opt\settings（windows）或者/opt/settings(linux)新增文件server.properties，内容如下：

```properties
env=PRO
#集群编号
idc=XJ-dpq-a
```



# v6.1.8 功能改进

1. 优化http重试机制：禁用重试后，不再重试

2. 优化http负载轮询机制：client protocol协议异常轮询下一节点

3. http负载均衡器优化：所有节点失败后，将实际的异常抛出到应用

4. bulkprocessor增加异常重试机制：重试次数，重试时间间隔，是否需要重试的异常类型判断：

   ```java
   // 重试配置
   				BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();
   		bulkProcessorBuilder.setBulkRetryHandler(new BulkRetryHandler() { //设置重试判断策略，哪些异常需要重试
   					public boolean neadRetry(Exception exception, BulkCommand bulkCommand) { //判断哪些异常需要进行重试
   						if (exception instanceof HttpHostConnectException     //NoHttpResponseException 重试
   								|| exception instanceof ConnectTimeoutException //连接超时重试
   								|| exception instanceof UnknownHostException
   								|| exception instanceof NoHttpResponseException
   //              				|| exception instanceof SocketTimeoutException    //响应超时不重试，避免造成业务数据不一致
   						) {
   
   							return true;//需要重试
   						}
   
   						if(exception instanceof SocketException){
   							String message = exception.getMessage();
   							if(message != null && message.trim().equals("Connection reset")) {
   								return true;//需要重试
   							}
   						}
   
   						return false;//不需要重试
   					}
   				})
   				.setRetryTimes(3) // 设置重试次数，默认为0，设置 > 0的数值，会重试给定的次数，否则不会重试
   				.setRetryInterval(1000l) // 可选，默认为0，不等待直接进行重试，否则等待给定的时间再重试
   
   ```

   
# v6.1.7 功能改进
1. bug修复：ES到db数据同步类型转换异常处理

2. 功能改进：ES到db数据同步，忽略字段设置和变量名和es字段名称映射设置不起作用


# v6.1.6 功能改进
1. 改进节点发现机制和健康检查机制：节点发现机制和健康检查机制分配独立的http连接池根据正式连接池配置决定是否启用失败重试机制
2. 功能改进：当没有可用节点时，没有将导致节点不可用的实际Exception抛出

# v6.1.5 功能改进
1. 改进节点发现机制和健康检查机制：为节点发现机制和健康检查机制分配独立的http连接池，与正式的连接池隔离，避免相互影响

# v6.1.3 功能改进
1. 添加http.backoffAuth属性：
    向后兼容的basic安全签名机制，v6.1.5以及之后的版本默认采用http组件内置的basic签名认证机制，但是有些http服务端对安全认证
   的实现不是很规范，会导致http basic security机制不能正常工作，因此通过设置http.backoffAuth兼容老版本安全认证方式
   true:向老版本兼容，false（默认值）：不向老版本兼容
   http.backoffAuth=true

2. 添加http.encodedAuthCharset属性，用于指定basic认证编码账号和口令的字符集，默认为：US-ASCII

3. 修复bug: v6.1.2版本引入的问题，在Elasticsearch没有启动的情况下，运行es客户端应用，因获取版本信息失败，es数据源健康检查进程不能正常初始化启动，在es启动后，无法将正常恢复elasticsearch连接    

# v6.1.2 功能改进

1. 功能扩展：增加停止elasticsearch数据源方法，使用示例：

```java
ElasticSearchHelper.stopElasticsearch("default");
```

2. 功能扩展：增加自定义httpclient机制，方便自定义httpclient，实现[Kerberos认证](https://github.com/bbossgroups/bboss-elasticsearch/issues/23)和与aws认证机制等功能，使用参考文档：[集成aws-elasticsearch](aws-elasticsearch-config.md)

3. 功能改进：调整认证机制，不再支持conf/elasticsearch.xml配置方式的认证机制，因此在需要认证的场景，可以调整为以下方式配置bboss客户端：

   [直接在applciation.properties中配置elasticsearch相关参数](common-project-with-bboss.md)

   [在spring boot配置文件中配置elasticsearch相关参数](spring-booter-with-bboss.md)

   [自定义初始化bboss es](Elasticsearch-bboss-custom-init.md)
   
4. 优化客户端Elasticsearch集群节点健康检查机制，优化httpclient连接池管理机制

# v6.1.1 功能改进

1. 修复bug：关闭indice后，获取索引状态方法不能正常工作：
```java
        List<ESIndice> indices = clientInterface.getIndexes();
```

2. 修复bug：获取indexField字段信息时boost属性类型转换异常
3. 修复bug: 修复非DB-ES数据同步时设置增量字段名称不起作用bug
4. 修复bug：修复spring boot数据源初始化bug
5. 功能扩展：支持将数据同步到多个目标elasticsearch集群,使用方法：    
```java
        importBuilder.setTargetElasticsearch("default,test");
```
6. 功能扩展：数据同步工具[增加指定外部定义状态数据库配置功能](https://github.com/bbossgroups/bboss-elastic-tran/commit/a9266c2e3581278fd21dfee07678de96569b1398)，使用方法：

   ```java
   importBuilder.setStatusDbname("secondds");
   ```

   案例地址：

   [Db2DBdemoWithStatusConfigDB.java](https://github.com/bbossgroups/db-db-job/blob/master/src/main/java/com/frameworkset/sqlexecutor/Db2DBdemoWithStatusConfigDB.java)

   https://github.com/bbossgroups/db-db-job/blob/master/src/main/resources/application.properties

7. 功能扩展：增加一组便捷查询工具方法，使用示例：

```java

        @Test
        
        	/**
        	 * 根据属性精确查找获取文档json报文
        	 * @param indexName
        	 * @param fieldName
        	 * @param blackcatdemo2
        	 * @return
        	 * @throws ElasticSearchException
        	 */
        	public void getDocumentByField() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		String document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2");
        	}
			/**
        	 * 根据属性精确查找获取文档json报文,通过options参数传递Elasticsearch查询控制参数
        	 */
        	@Test
        	public void getDocumentByField1() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map<String,Object> options = new HashMap<String, Object>();
        		String document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2",options);
        	}
        
        	/**
        	 * 根据属性全文检索获取文档json报文
        	 * @return
        	 * @throws ElasticSearchException
        	 */
        	@Test
        	public void getDocumentByFieldLike3() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		String document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2");
        	}
			/**
        	 * 根据属性全文检索获取文档json报文,通过options参数传递Elasticsearch查询控制参数
        	 */        
        	@Test
        	public void getDocumentByFieldLike1(){
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map<String,Object> options = new HashMap<String, Object>();
        		String document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2",options);
        	}
        	@Test
        	public void getDocumentByField2() throws ElasticSearchException{
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2",Map.class);
        	}
        	/**
        	 * 根据属性获取type类型文档对象
        
        	 * @return
        	 * @throws ElasticSearchException
        	 */
        	@Test
        	public void getDocumentByField3() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map<String,Object> options = new HashMap<String, Object>();
        		Map document = clientInterface.getDocumentByField("demo","applicationName.keyword","blackcatdemo2",Map.class,options);
        	}
        
        	@Test
        	public void getDocumentByFieldLike() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,null);
        	}
        
        	/**
        	 * 根据属性获取type类型文档对象
        
        	 * @return
        	 * @throws ElasticSearchException
        	 */
        	@Test
        	public void getDocumentByFieldLike2() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map<String,Object> options = new HashMap<String, Object>();
        		Map document = clientInterface.getDocumentByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,null);
        	}
        
        
        
        
        	@Test
        	public void searchListByField() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		ESDatas<Map> documents = clientInterface.searchListByField("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10);
        	}
        
        	/**
        	 * 根据属性获取type类型文档对象
        
        	 * @return
        	 * @throws ElasticSearchException
        	 */
        	@Test
        	public void searchListByField1() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map<String,Object> options = new HashMap<String, Object>();
        		ESDatas<Map> documents = clientInterface.searchListByField("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10,options);
        	}
        
        
        	@Test
        	public void searchListByFieldLike1() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		ESDatas<Map> documents = clientInterface.searchListByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10);
        	}
        
        	/**
        	 * 根据属性获取type类型文档对象
        
        	 * @return
        	 * @throws ElasticSearchException
        	 */
        	@Test
        	public void searchListByFieldLike() {
        		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
        		Map<String,Object> options = new HashMap<String, Object>();
        		ESDatas<Map> documents = clientInterface.searchListByFieldLike("demo","applicationName.keyword","blackcatdemo2",Map.class,0,10,options);
        	}
        

```

# v6.1.1 功能改进
1. 数据同步工具改进

  如果在程序里面配置的增量字段类型发生改变，要把增量状态表对应的类型调整为最新的字段类型

  设置了类型后，就按照类型来，不再按照设置的日期和数字字段名称来区分：
    
  增加setLastValueColumn方法，废弃setDateLastValueColumn和setNumberLastValueColumn两个方法
2. DB-DB数据同步工具：增加在作业中直接指定sql语句的功能
3. 修复数据同步bug：数据库到数据库跨库同步无效

# v6.1.0 功能改进
1.升级jackson版本号为2.9.10

2.持久层支持将sql配置文件存放位置设置为外部目录

3.调整jdbcfetchsize默认值为数据库jdbc驱动自带默认值

# v6.0.8 功能改进 
1.数据同步模块：可以通过ImportBuilder组件设置geoip数据库地址，使用案例：
```java
	importBuilder.setGeoipDatabase("d:/geolite2/GeoLite2-City.mmdb");
	importBuilder.setGeoipAsnDatabase("d:/geolite2/GeoLite2-ASN.mmdb");
```
2.增加bboss 持久层和httpproxy的spring boot start模块：
maven坐标：

```xml
    <dependency>
      <groupId>com.bbossgroups</groupId>
      <artifactId>bboss-spring-boot-starter</artifactId>
      <version>6.3.6</version>
     
    </dependency>
```
gradle坐标：
```xml
[group: 'com.bbossgroups', name: 'bboss-spring-boot-starter', version: "6.3.6", transitive: true]
```
使用案例：
<https://github.com/bbossgroups/bestpractice/tree/master/springboot-starter>

3.数据同步增加db-db数据同步spring boot案例工程：

https://github.com/bbossgroups/db-db-job

4.可以指定dsl配置文件存放到外部目录，配置方法：

dsl配置文件默认在classpath路径下查找，可以通过参数dslfile.dslMappingDir指定dsl配置文件的存放目录：

```properties
dslfile.dslMappingDir=D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources
```

spring boot对应配置：

```properties
spring.elasticsearch.bboss.dslfile.dslMappingDir=D:/workspace/bbossesdemo/eshelloword-booter/src/main/resources
```



# v6.0.7 功能改进 

1.修复按时间字段增量数据同步时，设置默认起始时间1970-01-01 00:00:00，在没有开始同步数据之前停止作业，再重启报错的问题

2.spring boot中执行数据同步作业时，可以在application.properties中配置db数据源参数

```properties
spring.elasticsearch.bboss.db.name = test
spring.elasticsearch.bboss.db.user = root
spring.elasticsearch.bboss.db.password = 123456
spring.elasticsearch.bboss.db.driver = com.mysql.jdbc.Driver
#db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
spring.elasticsearch.bboss.db.url = jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false
spring.elasticsearch.bboss.db.usePool = true

spring.elasticsearch.bboss.db.initSize=100
spring.elasticsearch.bboss.db.minIdleSize=100
spring.elasticsearch.bboss.db.maxSize=100


spring.elasticsearch.bboss.db.validateSQL = select 1

spring.elasticsearch.bboss.db.jdbcFetchSize = -2147483648
spring.elasticsearch.bboss.db.showsql = true
```
3.增量数据同步时，对增量状态值进行合法性校验

4.ds配置文件中xml节点property元素增加cacheDsl属性:指示框架是否启用dsl语法结构缓存机制，如果启用则只在第一次对dsl进行硬解析dsl语法结构，后续从缓冲中读取解析好的语法结构；如果禁用，则每次都硬解析dsl语法结构，默认true

# v6.0.6 功能改进 

1.修复bug：调用接口修改文档
public String updateDocument(Object documentId,Object params) throws ElasticSearchException; 

报了{"error":"no handler found for uri [/classroom/_doc/_update/3] and method [POST]"} 

2.调整异常信息：去掉异常信息中的url地址拼接操作，并以debug级别输出异常对应的url地址到日志文件中

# v6.0.5 功能改进 

1.修复bug：
动态通过ESIndex设置索引名称和索引type不起作用bug
数据同步中context动态设置索引名称和索引type不起作用bug

2.@ESIndex注解改进：增加useBatchContextIndexName属性

```java
 /* 当ESIndex的name只是用当前时间作为后缀，那么useBatchContextIndexName可以设置为true，提升解析动态索引名称性能，否则保持默认值false
 */
//@ESIndex(name="demowithesindex-{agentStarttime,yyyy.MM.dd}",type="demowithesindex")
@ESIndex(name="demowithesindex-{dateformat=yyyy.MM.dd}",useBatchContextIndexName = true)
```

# v6.0.4 功能改进 
1.http组件改进: 在异常信息中包含服务请求完整url地址信息

2.http proxy组件改进：如果http服务池没有配置health状态检查地址，启用被动的服务健康检查机制，在没有正常节点的情况下，返回异常节点，如果操作成功则将异常节点标注为正常节点

3.http组件改造：增加automaticRetriesDisabled开关，没有指定重试机制的情况下，如果automaticRetriesDisabled为false，在通讯则失败时自动重试3次，否则不重试

# v6.0.3 功能改进 
kafka2x-elasticsearch数据同步改进：kafka2x改进，提升同步性能

# v6.0.2 功能改进 

1.spring boot starter组件bbossEsstarter增加非配置文件管理dsl加载方法

# v6.0.1 功能改进
1.修复低版本jackson兼容性问题：Conflicting property name definitions: '_source'

2.数据同步工具：importbuilder组件增加Elasticsearch数据源代码配置功能，对应API

```java
    /**
	 * 添加es客户端配置属性，具体的配置项参考文档：
	 * https://esdoc.bbossgroups.com/#/development?id=_2-elasticsearch%e9%85%8d%e7%bd%ae
	 *
	 * 如果在代码中指定配置项，就不会去加载application.properties中指定的数据源配置，如果没有配置则去加载applciation.properties中的对应数据源配置
	 */
    public BaseImportBuilder addElasticsearchProperty(String name,String value) 
```

3.数据同步工具:增加HBase数据同步功能，支持增量同步和全量同步，增量同步可以根据记录时间戳范围、数字列、日期列增量同步

同步工具案例

https://github.com/bbossgroups/hbase-elasticsearch

4.https协议改进：支持pem ssl证书和 keystore and truststore证书

可以支持以下三种方式配置ssl证书

4.1 Using PEM certificates：

| 参数名称                | 说明                                               |
| ----------------------- | -------------------------------------------------- |
| http.pemCert            | pem证书路径，String                                |
| http.pemtrustedCA       | trustedHTTPCertificates证书路径，String            |
| http.supportedProtocols | ssl协议版本，String，默认值：TLSv1.2,TLSv1.1,TLSv1 |
| http.pemkeyPassword     | 私钥pem证书口令，String                            |
| http.pemKey             | 私钥pem证书路径，String                            |

4.2 Using the keystore and truststore file：

| 参数名称                | 说明                                                     |
| ----------------------- | -------------------------------------------------------- |
| http.keystoreAlias      | 可选，String                                             |
| http.trustAlias         | 可选，String                                             |
| http.supportedProtocols | 可选，ssl协议版本，String，默认值：TLSv1.2,TLSv1.1,TLSv1 |
| http.truststore         | truststore证书文件路径，证书类型为JKS                    |
| http.trustPassword      | truststore证书口令，String                               |
| http.keystore           | keystore证书路径，证书类型为JKS                          |
| http.keyPassword        | keystore证书口令                                         |

4.3 Using the keystore :

| 参数名称                | 说明                                                     |
| ----------------------- | -------------------------------------------------------- |
| http.supportedProtocols | ssl协议版本，可选，String，默认值：TLSv1.2,TLSv1.1,TLSv1 |
| http.keystore           | keystore证书路径，证书类型为JKS                          |
| http.keyPassword        | keystore证书口令                                         |

5.rest client 增加elasticsearch.useHttps配置参数

elasticsearch.useHttps true 自动发现节点，采用https协议进行通讯,false 采用http协议通讯，默认false

6.扩展dsl配置管理机制：支持数据库、redis等第三方机制管理和配置dsl语句，支持热加载机制

参考文档：https://esdoc.bbossgroups.com/#/db-dsl

基于数据库配置和管理dsl的示例

Elasticsearch 6及以下版本：[TestThirdDslContainer.java](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/thirddslcontainer/TestThirdDslContainer.java)

Elasticsearch 7及以上版本：[TestThirdDslContainer7.java](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/thirddslcontainer/TestThirdDslContainer7.java)


# v6.0.0 功能改进
1.数据同步工具改进：可以指定导入的target Elasticsearch和source Elasticsearch数据源名称

2.BulkCommand增加处理开始时间和结束时间字段

3.BulkProcessor改进:由被动bulk模式调整为主动bulk模式，减少内存占用，处理速度更快

4.ClientInterface接口增加获取Elasticsearch集群版本号和集群信息的api

# v5.9.9 功能改进

1.改进BulkProcessor shutdown机制:调用shutDown停止方法后，BulkProcessor不会接收新的请求，但是会处理完所有已经进入bulk队列的数据

参考文档：https://esdoc.bbossgroups.com/#/bulkProcessor

2.改进BulkProcessor bulk任务处理结果回调机制：增加对error和exception的bulk任务回调方法

参考文档：https://esdoc.bbossgroups.com/#/bulkProcessor

```java
    /**
	 * 有数据处理失败回调方法
	 * @param bulkCommand
	 * @param result Elasticsearch返回的response报文,包含的失败记录情况
	 */
	public void errorBulk(BulkCommand bulkCommand,String result);
	
    /**
     * 处理异常回调方法
     * @param bulkCommand
     * @param exception
     */
    public void exceptionBulk(BulkCommand bulkCommand,Throwable exception);
```

3.BulkProcessor增加timeout/masterTimeout/refresh/waitForActiveShards/routing/pipeline控制参数,

参数含义参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

参数使用参考文档：
https://esdoc.bbossgroups.com/#/bulkProcessor				

//下面的参数都是bulk url请求的参数：RefreshOption和其他参数只能二选一，配置了RefreshOption（类似于refresh=true&&aaaa=bb&cc=dd&zz=ee这种形式，将相关参数拼接成合法的url参数格式）就不能配置其他参数， 其中的refresh参数控制bulk操作结果强制refresh入elasticsearch，便于实时查看数据，测试环境可以打开，生产不要设置
```java
BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();

				bulkProcessorBuilder .setTimeout("100s")
			.setMasterTimeout("50s")
				.setRefresh("true")
				.setWaitForActiveShards(2)
			.setRouting("1") //(Optional, string) Target the specified primary shard.
				.setPipeline("1") // (Optional, string) ID of the pipeline to use to preprocess incoming documents.
```
4.客户端API改进：

addDocuments/updateDocuments ：与bulkprocessor处理逻辑整合

addDocument/updateDocument ：改进ClientOptions对象，增加一系列控制参数

```java
	private String pipeline;
	private String opType;
	private Boolean returnSource;
	private Long ifSeqNo;
	private Long ifPrimaryTerm;

	private List<String> sourceUpdateExcludes;
	private List<String> sourceUpdateIncludes;
	private String timeout;
	private String masterTimeout ;
	private Integer waitForActiveShards;
	private String refresh;
	/**单文档操作：文档id*/
	private Object id;
	/**单文档操作：文档id*/
	private Object parentId;
```

参数对应的作用和使用场景，参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

使用示例：更新文档

```java
ClientOptions updateOptions = new ClientOptions();
		List<String> sourceUpdateIncludes = new ArrayList<String>();
		sourceUpdateIncludes.add("name");
		updateOptions.setSourceUpdateIncludes(sourceUpdateIncludes);//es 7不起作用
		updateOptions.setDetectNoop(false)
				.setDocasupsert(false)
				.setReturnSource(true)
//				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("demoId")
//				.setVersion(2).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
//				.setIfPrimaryTerm(2l)
//				.setIfSeqNo(3l)
//				.setPipeline("1")
				.setEsRetryOnConflict(2)
				.setTimeout("100s")
		.setWaitForActiveShards(1)
		.setRefresh("true");
				//.setMasterTimeout("10s")
				;
		//更新文档
		response = clientUtil.updateDocument("demo",//索引表
				"demo",//索引类型
				demo
		,updateOptions);
```

批量添加文档

```java
ClientOptions clientOptions = new ClientOptions();
		clientOptions.setRefreshOption("refresh=true");
		clientOptions.setIdField("demoId");
		String response = clientUtil.addDocuments(
				demos,clientOptions);//为了测试效果,启用强制刷新机制，实际线上环境去掉最后一个参数"refresh=true"
```

添加文档

```java
ClientOptions addOptions = new ClientOptions();

		addOptions
				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("demoId")
//				.setVersion(2).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
//				.setIfPrimaryTerm(1l)
//				.setIfSeqNo(13l)
//				.setPipeline("1")
				.setTimeout("100s")
				.setWaitForActiveShards(1)
				.setRefresh("true")
				.setRouting(1);
		//.setMasterTimeout("10s")
		;
		String response = clientUtil.addDocument("demo",//索引表
				demo,addOptions);
```

在bulk记录中使用控制参数

```java
		ClientOptions clientOptions = new ClientOptions();
		clientOptions.setIdField("id")//通过clientOptions指定map中的key为id的字段值作为文档_id，
		          .setEsRetryOnConflict(1)
//							.setPipeline("1")

				.setOpType("index")
				.setIfPrimaryTerm(2l)
				.setIfSeqNo(3l)
		;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("name","duoduo1");
		data.put("id",1);
		bulkProcessor.insertData("bulkdemo","bulkdemo",data,clientOptions);
		ClientOptions deleteclientOptions = new ClientOptions();
		 

		deleteclientOptions.setEsRetryOnConflict(1);
		//.setPipeline("1")
		bulkProcessor.deleteData("bulkdemo","bulkdemo","1",deleteclientOptions);、
		
				ClientOptions updateOptions = new ClientOptions();
//		List<String> sourceUpdateExcludes = new ArrayList<String>();
//		sourceUpdateExcludes.add("name");
		//					updateOptions.setSourceUpdateExcludes(sourceUpdateExcludes); //es 7不起作用
		List<String> sourceUpdateIncludes = new ArrayList<String>();
		sourceUpdateIncludes.add("name");

		/**
		 * ersion typesedit
		 * In addition to the external version type, Elasticsearch also supports other types for specific use cases:
		 *
		 * internal
		 * Only index the document if the given version is identical to the version of the stored document.
		 * external or external_gt
		 * Only index the document if the given version is strictly higher than the version of the stored document or if there is no existing document. The given version will be used as the new version and will be stored with the new document. The supplied version must be a non-negative long number.
		 * external_gte
		 * Only index the document if the given version is equal or higher than the version of the stored document. If there is no existing document the operation will succeed as well. The given version will be used as the new version and will be stored with the new document. The supplied version must be a non-negative long number.
		 * The external_gte version type is meant for special use cases and should be used with care. If used incorrectly, it can result in loss of data. There is another option, force, which is deprecated because it can cause primary and replica shards to diverge.
		 */
		updateOptions.setSourceUpdateIncludes(sourceUpdateIncludes);//es 7不起作用
		updateOptions.setDetectNoop(false)
				.setDocasupsert(false)
				.setReturnSource(true)
//				.setEsRetryOnConflict(1) // elasticsearch不能同时指定EsRetryOnConflict和version
				.setIdField("id")
				//.setVersion(10).setVersionType("internal")  //使用IfPrimaryTerm和IfSeqNo代替version
				.setIfPrimaryTerm(2l)
				.setIfSeqNo(3l)
				.setPipeline("1")
		;
		bulkProcessor.updateData("bulkdemo","bulkdemo",data,updateOptions);
```

在数据同步中使用全局控制参数

```java
ClientOptions clientOptions = new ClientOptions();
//		clientOptions.setPipeline("1");
		clientOptions.setRefresh("true");
//		routing
//				(Optional, string) Target the specified primary shard.
		clientOptions.setRouting("2");
		clientOptions.setTimeout("50s");
		clientOptions.setWaitForActiveShards(2);
		importBuilder.setClientOptions(clientOptions);
```

在数据同步datarefactor中使用记录级控制参数

```java
ClientOptions clientOptions = new ClientOptions();
					clientOptions
							.setEsRetryOnConflict(1)
//							.setPipeline("1")

							.setOpType("index")
							.setIfPrimaryTerm(2l)
							.setIfSeqNo(3l)
					;//create or index
					context.setClientOptions(clientOptions);
```

5.数据同步改进：

5.1 数据同步到Elasticsearch，增加增、删、改数据的同步，Context接口添加以下三个方法来控制增、删、改数据的同步

context.markRecoredInsert();//添加，默认值

context.markRecoredUpdate();//修改

context.markRecoredDelete();//delete

5.2 可以在记录级指定每条记录对应的index和indexType

使用示例：这里根据随机值指定记录操作，可以根据实际的值进行控制

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
					context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
				}
				else if(r == 0) {
					ClientOptions clientOptions = new ClientOptions();

					clientOptions.setDetectNoop(false)
							.setDocasupsert(false)
							.setReturnSource(true)
							.setEsRetryOnConflict(3)
					;//设置文档主键，不设置，则自动产生文档id;
					context.setClientOptions(clientOptions);
					context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
					context.markRecoredUpdate();
				}
				else if(r == 2){
					ClientOptions clientOptions = new ClientOptions();
					clientOptions.setEsRetryOnConflict(2);
//							.setPipeline("1");
					context.setClientOptions(clientOptions);
					context.markRecoredDelete();
					context.setIndex("contextdbdemo-{dateformat=yyyy.MM.dd}");
				}

			}
		});
```



# v5.9.7 功能改进

1.完善数据同步功能：增加flushInterval参数，单位毫秒，值大于0时，对于异步消息处理组件数据长时间没有达到指定的batchSize记录条数时，强制将已经接收到的数据进行入库处理

2.增加BulkProcessor：异步收集增、删、改请求，并进行排队，达到一定的请求数量后，进行bulk批量入库处理，可以根据flushInterval参数(单位毫秒)，值大于0时，对于异步消息处理组件数据长时间没有达到指定的batchSize记录条数时，强制将已经接收到的数据进行bulk入库处理，参考文档：

https://esdoc.bbossgroups.com/#/bulkProcessor

3.增加elasticsearch-elasticsearch数据同步功能，Demo地址：

https://github.com/bbossgroups/elasticsearch-elasticsearch

4.Elasticsearch同步功能改进：增加ignoreNullValueField控制参数，true是忽略null值存入elasticsearch，false是存入（默认值）

importBuilder.setIgnoreNullValueField(true); 

5.Client Api改进：http连接池增加 evictExpiredConnections配置，true 控制HttpClient实例使用后台线程主动地从连接池中驱逐过期连接，默认值为true

6.bug修复：ElasticSearchHelper.getElasticSearchSink(String elasticSearch)方法传入default数据源名称时，后台报异常信息

# v5.9.6 功能改进

1.修复数据同步bug：application.properties文件中不配置db相关的选项时，同步作业报错

2.完善数据同步任务统计信息记录

3.解决mongodb-elasticsearch增量数据同步增量状态记录主键没有正确生成的问题

# v5.9.5 功能改进
1.修改bug：slice scroll parral和scroll parrel查询有个bug，变量名称写错了，手误导致，但是问题很严重，会导致数据重复，请升级版本到5.9.7！

2.数据同步模块扩展：增加数据库到数据库的数据同步功能

# v5.9.3 功能改进

  1.将数据同步模块从elasticsearch模块剥离，单独形成gradle工程 ，github地址： 

  https://github.com/bbossgroups/bboss-elastic-tran

  2.数据同步工具功能扩展：

-   增加mongodb-db同步模块,支持mongodb各个版本，各种主流数据库，案例：


  https://github.com/bbossgroups/mongodb-elasticsearch

-   增加kafka1x-elasticsearch同步模块兼容kafka_2.12-0.10.2.0系列版本,elasticsearch各个版本：案例


  https://github.com/bbossgroups/kafka1x-elasticsearch

-   增加kafka2x-elasticsearch同步模块兼容kafka_2.12-2.3.0 系列版本,elasticsearch各个版本，案例：


  https://github.com/bbossgroups/kafka2x-elasticsearch

3. 调整同步程序包路径，api兼容旧版本



# v5.9.2 功能改进

1.增加MetaMap类：MetaMap继承HashMap，为map 增加meta元数据相关的属性信息，参考示例：

```java
//创建批量创建文档的客户端对象，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		MetaMap newDemo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"1",//文档id
				MetaMap.class
		);
		System.out.println(newDemo);
		System.out.println("getId:"+newDemo.getId());
		System.out.println("getIndex:"+newDemo.getIndex());
		System.out.println("getNode:"+newDemo.getNode());
		System.out.println("getShard:"+newDemo.getShard());
		System.out.println("getType:"+newDemo.getType());
		System.out.println("getExplanation:"+newDemo.getExplanation());
		System.out.println("getFields:"+newDemo.getFields());
		System.out.println("getHighlight:"+newDemo.getHighlight());
		System.out.println("getInnerHits:"+newDemo.getInnerHits());
		System.out.println("getNested:"+newDemo.getNested());
		System.out.println("getPrimaryTerm:"+newDemo.getPrimaryTerm());
		System.out.println("getScore:"+newDemo.getScore());
		System.out.println("getSeqNo:"+newDemo.getSeqNo());
		System.out.println("getVersion:"+newDemo.getVersion());
		System.out.println("getParent:"+newDemo.getParent());
		System.out.println("getRouting:"+newDemo.getRouting());
		System.out.println("getSort:"+newDemo.getSort());
		System.out.println("isFound:"+newDemo.isFound());
```
2.修改自定义启动客户端bug：设置数字参数和boolean参数不起作用问题修改

3.数据同步工具：增加mongodb到elasticsearch同步功能，工具demo：

https://github.com/bbossgroups/mongodb-elasticsearch

 参考文档：[mongodb-elasticsearch数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_4-mongodb-elasticsearch数据同步使用方法) 

4.数据同步工具改进：如果同步字段中包含名称为_id的属性，则忽略之，否则会导致数据无法正常导入Elasticsearch

5.增加慢请求服务调用日志信息记录，增加慢dsl时间条件参数配置

  slowDslThreshold单位：ms 毫秒

```properties
  elasticsearch.slowDslThreshold=10000
```

  spring boot配置项  

```properties
spring.elasticsearch.bboss.elasticsearch.slowDslThreshold=10000
```

  需要截取掉过长的dsl:超过2048的dsl将被截取掉超过的内容，替换为.....

  如果没有指定回调处理接口，将直接打印警告信息到日志文件，WARN级别
  否则调用SlowDslCallback接口方法slowDslHandle传递慢dsl的相关信息：

```java
  public interface SlowDslCallback {
      	void slowDslHandle( SlowDsl slowDsl);
      }
```

​      slowDslCallback配置： 

```properties
  elasticsearch.slowDslCallback=org.bboss.elasticsearchtest.crud.TestSlowDslCallback
```

​     spring boot配置项     

```properties
spring.elasticsearch.bboss.elasticsearch.slowDslCallback=org.bboss.elasticsearchtest.crud.TestSlowDslCallback
```



# v5.9.1 功能改进
修复bug：按时间增量同步问题,导致任务重启后同步报错

# v5.9.0 功能改进
1.数据同步工具改进：完善增量数据同步机制，增量字段无需排序即可实现增量同步功能，提升同步性能

改进后部分类（比如Context）包路径做了调整，但是api完全兼容，可以到以下地址下载改进后最新的工具:

- jdk timer和quartz demo工程
  https://github.com/bbossgroups/db-elasticsearch-tool

- xxl-job demo工程
  https://github.com/bbossgroups/db-elasticsearch-xxjob


2.数据同步工具bug修复：解决增量同步状态更新可能存在的不正确问题

3.数据同步工具改进：增加数据同步监控指标数据的采集，参考文档：[数据同步任务执行统计信息获取](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2317-%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%9f%e8%ae%a1%e4%bf%a1%e6%81%af%e8%8e%b7%e5%8f%96)

4.数据同步工具新增功能：elasticsearch到db的数据同步功能，支持全量、增量定时同步，内置jdk timer同步器，支持quartz、xxl-job任务调度引擎，使用参考：

 [elasticsearch-db数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_3-elasticsearch-db%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bd%bf%e7%94%a8%e6%96%b9%e6%b3%95) 

5.简化数据同步程序开发调试工作：可以直接在同步程序的main中方法进行同步功能开发和调试

6.Scroll/Slice scroll检索功能改进：支持在hanlder结果回调处理函数中中断scroll/slice croll的执行，参考文档：

 https://esdoc.bbossgroups.com/#/Scroll-SliceScroll-api 

7.json库fastjackson升级到2.10.0 

# v5.8.9 功能改进

1.改进检索Meta数据：增加seqNo和primaryTerm属性。

2.includeTypeName配置默认设置为false

3.BUG fixed: sql查询日期处理问题 [#11](https://github.com/bbossgroups/bboss-elasticsearch/issues/11) 


# v5.8.8 功能改进

1.改进检索Meta数据：增加explanation属性。

2.增加一组meta注解，用于在对象中注入检索元数据，使用参考PO对象

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/MetaDemo.java

![](/images/metaanno.png)

# v5.8.6 功能改进

1.新增bboss-elasticsearch-rest-entity模块，方便bboss相关的实体bean被第三方项目引用。

2.兼容性完善：支持最新的spring boot版本2.1.6.RELEASE

# v5.8.5 功能改进

1.httpproxy负载均衡器：Map配置代码方式启动httpproxy，增加对环境变量的支持，参考文档

2.数据同步工具：解决oracle时间Timestamp问题

3.数据同步工具：解决可能存在的数据序列化问题

# v5.8.4 功能改进

1.增加URL参数检索API，参考文档：[通过URL参数检索文档](https://esdoc.bbossgroups.com/#/document-crud?id=通过url参数检索文档)

# v5.8.1 功能改进

1. 升级bboss框架到5.5.2

2. 升级bboss http到5.5.3

3. 属性配置支持从jvm system环境参数和OS 环境变量取值，例如：

   ```properties
   #引用环境变量job_executor_ip
   
   xxl.job.executor.ip=#[job_executor_ip]
   ```

   

4. 增加script函数管理api（新增、删除、获取脚本函数）

5. 完善http负载均衡组件


# v5.8.0 功能改进

1. 升级框架到5.5.2
2. 增加forcemerge方法
3. Elasticsearch响应报文长度为0情况处理
4. http负载均衡组件增加主备路由功能

# v5.7.8 功能改进

1. 升级bboss版本为5.5.0
2. 增加http负载均衡组件，参考文档：https://esdoc.bbossgroups.com/#/httpproxy
3. 完善http组件，升级httpcomponents client版本为4.5.9
4. ClientOption/UpdateOption 添加属性：version/versionType/routing/Docasupsert/DetectNoop/EsRetryOnConflict

# v5.7.5 功能改进

1. 支持第三方用途多数据源配置和加载，可以同步数据过程中加载这些数据，通过这些数据源查找数据，组合同步到es中
2. 持久层消除对jackson json包的依赖 
3. 持久层增加对es jdbc 6.4.x,6.5.x,6.6.x,7.x的支持
4. 对于默认的持久层不能识别的driver，采用DBNone默认适配器并给出警告信息，而不是抛出异常

```java
if(log.isWarnEnabled()){
				log.warn("Unknown JDBC driver: {}: Adapter DBNonewill be used or define one and resitry it to bboss.",driver);
			}
```



# v5.7.3 功能改进

1. 改进searchAllParrel方法：增加对es 2.x的兼容性处理
2. 改进数据库同步到es的db事务机制：增加是否启用在datarefactor中开启db事务


# v5.7.2 功能改进

1. 同步mysql大数据表到Elasticsearch，增加[mysql内置流处理机制](https://esdoc.bbossgroups.com/#/db-es-tool?id=_513-mysql-resultset-stream%e6%9c%ba%e5%88%b6%e8%af%b4%e6%98%8e)的支持

# v5.7.1 功能改进

1. Fixed addDocumentsWithIdKey null point exception since 5.6.8
2. Spring booter start module support set retryInterval(timeunit:ms) parameter

# v5.7.0 功能改进

1. 修复健康检查不起作用的bug: 应用启动时es没有启动，当es起来后，客户端一直提示es不可用
2. 同步数据工具支持达梦数据库到elasticsearch数据同步

# v5.6.9 功能改进

1. 修改v5.6.8数据同步bug：索引type类型多了一个双引号 

# v5.6.8 功能改进

1. 更新jackson版本号为2.9.8 
2. 更新bboss版本号为5.3.1
3. 增加[ESIndex注解](https://esdoc.bbossgroups.com/#/document-crud?id=esindex%e6%b3%a8%e8%a7%a3%e4%bd%bf%e7%94%a8)，用于配置bean的动态索引名称和索引类型
4. 如果http端口被错误配置为transport 9300端口，给出相应的出错提示

# v5.6.7 功能改进

1. 改进scroll并行查询机制，支撑Elasticsearch pinpoint apm插件

# v5.6.6 功能改进

1. 数据同步工具改进：改进xxjob的支持，增加shard分片任务执行机制
2. 完善故障节点检测日志信息

# v5.6.5 功能改进

1. 数据同步工具改进：增加dbAdaptor属性配置，通过定制自己的dbAdaptor可以非常方便地实现bboss本身不支持的数据库的数据同步工作

2. 数据同步工具改进：支持xxjob分布式定时任务引擎来调度同步作业任务

3. 数据同步工具改进：支持quartz定时任务引擎来调度同步作业任务

4. 数据同步工具改进：过滤器Context增加修改字段名称title为新名称newTitle并且修改字段的值api，使用方法，

   ```java
   //修改字段名称title为新名称newTitle，并且修改字段的值
   context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
   ```

5. 更新bboss框架版本5.3.0

# v5.6.3 功能改进

1. 调整可变长数组为固定长度数组
2. 更新bboss框架版本5.2.9

# v5.6.2 功能改进

1. 增加地理信息库模块，同步数据时，可以将ip字段对应的ip转换为对应的运营商、城市国家、地理坐标信息
2. 改进增量数据同步功能：增加配置增量状态存储到外部数据库，以便分布式任务调度执行时共享增量同步状态信息

# v5.6.1 功能改进

1. Elasticsearch 7.0.0兼容性改造：[提供一组不带索引类型的API](Elasticsearch-7-API.md)，涉及批处理api和数据同步工具
2. Elasticsearch 7.0.0兼容性改造：处理hits.total类型为Object的问题，涉及获取文档api和检索api

   3.Elasticsearch 7.0.0兼容性改造：处理bulk处理时routing字段名称变更问题，涉及批处理api和数据同步工具

# v5.6.0 功能改进

1.修改bboss框架版本号为6.1.2

2.http连接池超时，sockettimeout，connectiontimeout异常信息添加超时时间信息

3.修改数据同步任务TaskCall中的空指针异常

# v5.5.9 功能改进

解决从http连接池获取连接超时，将服务器标注为不可用问题

# v5.5.8 功能改进

1. 增加更新索引配置通用方法
2. 增加更新集群setting通用api 
3. 增加synflush indice api
4. 增加获取表明日期格式的方法
5. 数据同步工具bug修复：oracle.sql.TIMESTAMP类型的增量字段取值转换不正确

# v5.5.7 功能改进：
1.修复bug：处理构建文档时，可能存在的格式不正确的问题

2.修复bug：scrollParallel查询时，没有数据记录报任务存在空指针问题 

3.api改进：增加管理索引副本的方法

4.数据同步：处理持久层检索map对象时，clob或者blob对象自动转换为String的功能

5.数据同步：数据同步工具开启全局db connection共享事务，在数据导入任务执行过程中，开启共享连接功能，避免重复从连接池中申请连接，提升导入性能

# v5.5.6 功能改进：

1. agg统计：将Integer类型key强制转换String类型错误bug

2. dsl中片段变量，可以引用外部文件中定义的片段

   片段引用测试用例：[TestPianduan.java](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/dsl/TestPianduan.java)  [pianduan.xml](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/pianduan.xml)  [outpianduanref.xml](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/outpianduanref.xml)

3. 增加数据导入任务异常处理回调方法:[参考文档](https://esdoc.bbossgroups.com/#/db-es-tool?id=_47-%E8%AE%BE%E7%BD%AE%E4%BB%BB%E5%8A%A1%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C%E5%9B%9E%E8%B0%83%E5%A4%84%E7%90%86%E5%87%BD%E6%95%B0)

# **v5.5.3 功能改进：**

1. 完善orm保存对象到es序列化机制，避免非ESBaseData和ESId对象的相关属性被忽略掉

2. 数据同步工具支持mysql8,tidb,hive

3. 完善http组件：自定义重试机制 
   http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.ConnectionResetHttpRequestRetryHandler
   空闲连接校验频率，单位毫秒，>0起作用http.validateAfterInactivity=3000
   每次获取connection时校验连接，true，校验，默认false
   http.staleConnectionCheckEnabled=false 
   
4. 依赖的http组件版本升级：
   httpclient, version: '4.5.6'
   httpcore, version: '4.4.11'
   httpmime, version: '4.5.6'
   
5. 完善框架打印的日志信息

6. 修复释放资源时抛出空指针异常

7. 数据同步工具改进：可以按日期时间自动分表，使用方法importBuilder
   .setIndex("dbdemo-{yyyy.MM.dd}") //通过{yyyy.MM.dd}设置按日期分索引表
   
8. 数据同步工具改进：解决忽略字段名称小写时不起作用的问题

9. 新增GeoPoint和GeoShape两个对象

10. 增加单值聚合查询的api和测试用例
    参考文档：<https://esdoc.bbossgroups.com/#/agg>
    
11. 增加open/close index方法

12. 批量修改/添加文档api完善：增加指定对象字段名称对应的值作为文档id和文档父id

13. 增加一组通用api：通过ClientOptions/UpdateOptions指定控制参数对应的对象字段，替代原有的@ESId,@ESParentId等注解

14. 优化dsl配置文件热加载机制：解决jar中dsl配置文件热加载问题

15. 新增一个基于spring boot2的web demo
    <https://github.com/bbossgroups/es_bboss_web>
    
16. 修复数据同步工具bug：解决增量同步线程池重复创建问题，建议大家将版本升级到5.5.3

    本问题会导致长时间运行抛出以下错误：

    ![img](images\oom.png)

17. 完善对elasticsearch 1.x版本的支持,searchallparallel方法支持es 1.x版本

18. 数据同步工具：elasticsearch同步到dbes增加scroll parallel导出功能

19. 数据导出工具: 任务执行结果处理接口，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务进行相应处理，参考文档：
    <https://esdoc.bbossgroups.com/#/db-es-tool>

20. 数据同步工具：规范并修改相关类的名称

21. sclice scroll检索内部采用异步方式执行每个scroll查询结果

22. scroll检索增加异步处理每个scroll查询结果的功能

23. 数据同步工具：增加在过滤器中过滤记录功能 

24. Innerhit检索时层级超过2级的对象（继承ESBaseData对象）中没有设置文档id等信息问题修复

更多功能改进请浏览：[commit](https://github.com/bbossgroups/bboss-elasticsearch/commits/master)

# **bboss elasticsearch 使用参考文档** 

[https://esdoc.bbossgroups.com](https://esdoc.bbossgroups.com/)  



# 相关链接

- bboss-elastic 的详细介绍：[点击查看](README.md)
- bboss-elastic 的下载地址：[点击下载](https://github.com/bbossgroups/bboss-elasticsearch)

# 开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images/wxbboss.png" style="zoom:50%;" />
交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">



# 支持我们

如果您正在使用bboss，或是想支持我们继续开发，您可以通过如下方式支持我们：

1.Star并向您的朋友推荐或分享

[bboss elasticsearch client](https://gitee.com/bboss/bboss-elastic)🚀

[数据采集&流批一体化处理](https://gitee.com/bboss/bboss-elastic-tran)🚀

2.通过[爱发电 ](https://afdian.net/a/bbossgroups)直接捐赠，或者扫描下面二维码进行一次性捐款赞助，请作者喝一杯咖啡☕️





<img src="images/alipay.png"  height="200" width="200">

<img src="images/wchat.png" style="zoom:50%;" />

非常感谢您对开源精神的支持！❤您的捐赠将用于bboss社区建设、QQ群年费、网站云服务器租赁费用。


