# http/https插件使用指南

通过bboss http输入/输出插件，可以从http服务采集数据，也可以从其他数据源采集的数据推送给http服务：

1. 支持增量、全量数据采集同步，
2. 支持分页模式采集数据
3. 支持http服务高可用负载及容错机制，可以配置服务健康检查机制
4. 支持post和put两种http method
5. http输入插件，采用类似于Elasticsearch rest服务的dsl查询脚本语言，来传递http数据查询服务所需的参数、增量条件、分页条件

bboss 输入/输出插件设计到三个主要的作业配置组件：

1. ImportBuilder：数据同步作业构建器，用来进行作业基本配置，包括设置InputConfig、OutputConfig、数据转换处理配置、增量状态管理配置、定时器配置、任务监控配置、任务拦截器配置、并行处理线程池和队列配置、容错配置、提取数据条件配置等

2. HttpInputConfig：http输入插件配置组件，包括http服务地址（多个用逗号分隔）、http连接池配置、http method配置、ssl证书配置、http链接池名称配置、提取数据dsl脚本设置等

3. HttpOutputConfig：http输出插件配置组件，包括http服务地址（多个用逗号分隔）、http连接池配置、http method配置、ssl证书配置、http链接池名称配置、提取数据dsl脚本设置等

http数据同步作业包含作业配置态和作业运行态，具体看如下示意图

![](images\datasyn-inout.png)
**配置态：**指通过作业构建器ImportBuilder配置http数据采集作业的基础配置、输入和输出配置

**运行态：**通过ImportBuilder构建一个Datatream，加载作业配置，启动作业并执行数据采集、数据处理、数据输出，从而实现整个数据采集同步功能。

下面具体介绍http插件使用方法

# 1.作业基础配置

```java
创建一个作业构建器
ImportBuilder importBuilder = new ImportBuilder() ;
```

| 属性名称                | 类型                | 说明                                                         |
| ----------------------- | ------------------- | ------------------------------------------------------------ |
| FetchSize               | int                 | 按批获取数据记录数大小，importBuilder.setFetchSize(5000)     |
| BatchSize               | int                 | 按批输出数据记录数待续，importBuilder.setBatchSize(1000)     |
| InputConfig             | InputConfig         | 设置输入插件配置，importBuilder.setInputConfig(httpInputConfig); |
| OutputConfig            | OutputConfig        | 设置输出插件配置，importBuilder.setOutputConfig(elasticsearchOutputConfig); |
| addParam                | 方法                | 为查询类作业添加额外的查询条件参数importBuilder.addParam("otherParam","陈雨菲2:0战胜戴资颖"); |
| UseJavaName             | boolean             | 可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId |
| UseLowcase              | boolean             | 可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用 |
| PrintTaskLog            | boolean             | 可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false |
| FixedRate               | boolean             | 参考jdk timer task文档对fixedRate的说明                      |
| DeyLay                  | long                | 任务延迟执行deylay毫秒后执行                                 |
| Period                  | long                | 每隔period毫秒执行，如果不设置，只执行一次                   |
| ScheduleDate            | date                | 指定任务开始执行时间：日期                                   |
| addCallInterceptor      | CallInterceptor     | 设置任务执行拦截器，可以添加多个，定时任务每次执行的拦截器   |
| LastValueColumn         | String              | 指定数字增量查询字段                                         |
| FromFirst               | boolean             | false 如果作业停了，作业重启后从上次截止位置开始采集数据，true 如果作业停了，作业重启后，重新开始采集数据 |
| StatusDbname            | String              | 设置增量状态数据源名称                                       |
| LastValueStorePath      | String              | 记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样 |
| LastValueStoreTableName | String              | 记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab |
| LastValueType           | int                 | 指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型，ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型 |
| IncreamentEndOffset     | int                 | 单位：秒，日期类型增量导入，可以设置一个导入截止时间偏移量。引入IncreamentEndOffset配置，主要是增量导出时，考虑到elasticsearch、mongodb这种存在写入数据的延迟性的数据库，设置一个相对于当前时间偏移量导出的截止时间，避免增量导出时遗漏数据。 |
| addFieldMapping         | 方法                | 手动设置字段名称映射，将源字段名称映射为目标字段名称importBuilder.addFieldMapping("document_id","docId") |
| addIgnoreFieldMapping   | 方法                | 添加忽略字段，importBuilder.addIgnoreFieldMapping("channel_id"); |
| addFieldValue           | 方法                | 添加全局字段和值，为每条记录添加额外的字段和值，可以为基本数据类型，也可以是复杂的对象mportBuilder.addFieldValue("testF1","f1value"); |
| DataRefactor            | DataRefactor        | 通过DataRefactor，对数据记录进行数据转换、清洗、加工操作，亦可以对数据进行记录级别的处理，比如添加字段、去除字段、忽略记录、类型转换等 |
| Parallel                | boolean             | 设置为多线程并行批量导入,false串行 true并行                  |
| Queue                   | int                 | 设置批量导入线程池等待队列长度                               |
| ThreadCount             | int                 | 设置批量导入线程池工作线程数量                               |
| ContinueOnError         | boolean             | 任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行 |
| Asyn                    | boolean             | true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回 |
| ExportResultHandler     | ExportResultHandler | 设置任务执行结果以及异常回调处理函数，函数实现接口即可       |
| builder                 | 方法                | 构建DataStream 执行数据库表数据导入es操作  ： DataStream dataStream = importBuilder.builder(); dataStream.execute();//执行导入操作 |



# 2.http输入插件

http输入插件采用类似于Elasticsearch rest服务的dsl查询脚本语言，来传递http数据查询服务所需的参数、增量条件、分页条件，属性说明如下：

```java
//创建输入插件Config实例
HttpInputConfig httpInputConfig = new HttpInputConfig();
```



| 属性名称              | 类型    | 说明                                                         |
| --------------------- | ------- | ------------------------------------------------------------ |
| sourceHttpPool        | String  | 源http连接池服务组名称                                       |
| addHttpInputConfig    | 方法    | 添加http服务参数、服务地址、监控检查机制,例如: httpInputConfig.setQueryUrl("/httpservice/getData.api") .addSourceHttpPoolName("http.poolNames","datatran") .addHttpInputConfig("datatran.http.health","/health") .addHttpInputConfig("datatran.http.hosts","192.168.137.1:808") .addHttpInputConfig("datatran.http.timeoutConnection","5000") .addHttpInputConfig("datatran.http.timeoutSocket","50000") .addHttpInputConfig("datatran.http.connectionRequestTimeout","50000") .addHttpInputConfig("datatran.http.maxTotal","200") .addHttpInputConfig("datatran.http.defaultMaxPerRoute","100") .addHttpInputConfig("datatran.http.failAllContinue","true");                                参考文档：https://esdoc.bbossgroups.com/#/httpproxy |
| addSourceHttpPoolName | 方法    | 添加http服务组属性参数：httpInputConfig.addSourceHttpPoolName("http.poolNames","datatran") |
| showDsl               | boolean | 控制作业执行时，是否打印查询的dsl脚本，true 打印，false 不打印，默认值false |
| dslFile               | String  | querydsl脚本配置文件路径，在classes路径下                    |
| queryDslName          | String  | querydsl脚本名称，脚本配置规范，可以参考文档：https://esdoc.bbossgroups.com/#/development  章节【[5.3 dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl配置规范)】 |
| queryDsl              | String  | 直接设置queryDsl脚本，脚本配置规范，可以参考文档：https://esdoc.bbossgroups.com/#/development  章节【[5.3 dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl配置规范)】 |
| httpMethod            | String  | http请求method，支持两种：put，post                          |
| pageSize              | int     | 无需显示指定，按批获取数据记录数大小，通过importBuilder.setFetchSize(5000)设置 |
| httpPagineFrom        | int     | 分页查询起始位置，默认从0开始，如果服务支持分页获取增量或者全量数据，设置分页起始位置 |
| pagine                | boolean | 分页查询控制变量，false 不分页，true 分页，默认值false，     |
| httpPagineSize        | int     | 如果服务支持分页获取增量或者全量数据，设置每页记录数，如果实际返回的记录数小于httpPagineSize或者为0，则表示本次分页获取数据结束，对应参数fetchSize配置的值 |

带分页的querydsl脚本案例：

```json
{
    "logTime":#[logTime],## 传递增量时间起始条件
    "logTimeEndTime":#[logTime__endTime],## 传递增量时间截止时间条件，必须指定IncreamentEndOffset偏移时间量才能设置增量截止时间
    "from":#[httpPagineFrom], ## 如果服务支持分页获取增量或者全量数据，设置分页起始位置
    "size":#[httpPagineSize],  ## 如果服务支持分页获取增量或者全量数据，设置每页记录数，如果实际返回的记录数小于httpPagineSize或者为0，则表示本次分页获取数据结束，对应参数fetchSize配置的值
    "otherParam": #[otherParam] ## 其他服务参数
}
```

# 3.http输出插件



# 4.案例

## 4.1 http输入插件案例

## 4.2 http输出插件案例



## 4.3 案例发布运行