# HBase-Elasticsearch数据同步

同步案例源码地址：

https://github.com/bbossgroups/hbase-elasticsearch


Bboss is a good elasticsearch Java rest client. It operates and accesses elasticsearch in a way similar to mybatis.

# 1.Environmental requirements

JDK requirement: JDK 1.7+

Elasticsearch version requirements: 1.x,2.X,5.X,6.X,7.x,+

Spring booter 1.x,2.x,+

hbase 1.x,hbase 2.x

# 2.HBase-Elasticsearch 数据同步工具demo
使用本demo所带的应用程序运行容器环境，可以快速编写，打包发布可运行的数据导入工具，包含现成的示例如下：
## 2.1 jdk timer定时全量同步
[org.frameworkset.elasticsearch.imp.HBase2ESFullDemo](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemo.java)

## 2.2 jdk timer定时增量同步
[org.frameworkset.elasticsearch.imp.HBase2ESScrollTimestampDemo](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESScrollTimestampDemo.java)

## 2.3 jdk timer定时带条件同步
[org.frameworkset.elasticsearch.imp.HBase2ESFullDemoWithFilter](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/HBase2ESFullDemoWithFilter.java)

## 2.4 quartz定时全量同步
[org.frameworkset.elasticsearch.imp.QuartzHBase2ESImportTask](https://github.com/bbossgroups/hbase-elasticsearch/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzHBase2ESImportTask.java)

# 3.支持的数据库：
HBase 到elasticsearch数据同步
## 4.支持的Elasticsearch版本
Elasticsearch 1.x,2.x,5.x,6.x,7.x,+

## 5.支持海量PB级数据同步导入功能

[使用参考文档](https://esdoc.bbossgroups.com/#/db-es-tool)

# 6.导入maven坐标


```xml
<dependency>
  <groupId>com.bbossgroups.plugins</groupId>
  <artifactId>bboss-elasticsearch-rest-hbase</artifactId>
  <version>6.0.0</version>
  <scope>compile</scope>
</dependency>
<!--
hbase shaded client的版本号与hbase的版本相关，请根据hbase的版本调整hbase shaded client的版本号
-->
<dependency>
  <groupId>org.apache.hbase</groupId>
  <artifactId>hbase-shaded-client</artifactId>
  <version>2.2.3</version>
</dependency>
```
本案例基于hbase 1.3.0版本开发，所以选择的是1.2.4的客户端，具体的client版本号可以根据hbase版本自行选择：

https://search.maven.org/artifact/org.apache.hbase/hbase-shaded-client
```
compile([group: 'org.apache.hbase', name: 'hbase-shaded-client', version: "1.2.4", transitive: true])
```
# 7.hbase-elasticsearch同步关键代码和配置

## 7.1hbase参数配置

```java
HBaseExportBuilder importBuilder = new HBaseExportBuilder();
      importBuilder.setBatchSize(1000) //设置批量写入目标Elasticsearch记录数
            .setFetchSize(10000); //设置批量从源Hbase中拉取的记录数,HBase-0.98 默认值为为 100，HBase-1.2 默认值为 2147483647，即 Integer.MAX_VALUE。Scan.next() 的一次 RPC 请求 fetch 的记录条数。配置建议：这个参数与下面的setMaxResultSize配合使用，在网络状况良好的情况下，自定义设置不宜太小， 可以直接采用默认值，不配置。

//    importBuilder.setHbaseBatch(100) //配置获取的列数，假如表有两个列簇 cf，info，每个列簇5个列。这样每行可能有10列了，setBatch() 可以控制每次获取的最大列数，进一步从列级别控制流量。配置建议：当列数很多，数据量大时考虑配置此参数，例如100列每次只获取50列。一般情况可以默认值（-1 不受限），如果设置了scan filter也不需要设置
//          .setMaxResultSize(10000l);//客户端缓存的最大字节数，HBase-0.98 无该项配置，HBase-1.2 默认值为 210241024，即 2M。Scan.next() 的一次 RPC 请求 fetch 的数据量大小，目前 HBase-1.2 在 Caching 为默认值(Integer Max)的时候，实际使用这个参数控制 RPC 次数和流量。配置建议：如果网络状况较好（万兆网卡），scan 的数据量非常大，可以将这个值配置高一点。如果配置过高：则可能 loadCache 速度比较慢，导致 scan timeout 异常
      // 参考文档：https://blog.csdn.net/kangkangwanwan/article/details/89332536


      /**
       * hbase参数配置
       */
      importBuilder.addHbaseClientProperty("hbase.zookeeper.quorum","192.168.137.133")  //hbase客户端连接参数设置，参数含义参考hbase官方客户端文档
            .addHbaseClientProperty("hbase.zookeeper.property.clientPort","2183")
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
## 7.2 Elasticsearch参数配置
```java

/**
 * es相关配置
 可以通过addElasticsearchProperty方法添加Elasticsearch客户端配置，
 也可以直接读取application.properties文件中设置的es配置
 */
importBuilder.addElasticsearchProperty("elasticsearch.rest.hostNames","192.168.137.1:9200");//设置es服务器地址
importBuilder.setTargetElasticsearch("default");//设置目标Elasticsearch集群数据源名称，和源elasticsearch集群一样都在application.properties文件中配置

importBuilder.setIndex("hbase2esdemo") //全局设置要目标elasticsearch索引名称
      .setIndexType("hbase2esdemo"); //全局设值目标elasticsearch索引类型名称，如果是Elasticsearch 7以后的版本不需要配置
```

更多配置参数文档：[Elasticsearch参数配置](https://esdoc.bbossgroups.com/#/mongodb-elasticsearch?id=_5242-elasticsearch%e5%8f%82%e6%95%b0%e9%85%8d%e7%bd%ae)



## 7.3 Elasticsearch文档_id生成机制配置

```java
// 设置Elasticsearch索引文档_id
      /**
       * 如果指定rowkey为文档_id,那么需要指定前缀meta:，如果是其他数据字段就不需要
       * 例如：
       * meta:rowkey 行key byte[]
       * meta:timestamp  记录时间戳
       */
//    importBuilder.setEsIdField("meta:rowkey");
      // 设置自定义id生成机制
      //如果指定EsIdGenerator，则根据下面的方法生成文档id，
      // 否则根据setEsIdField方法设置的字段值作为文档id，
      // 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id
      importBuilder.setEsIdGenerator(new EsIdGenerator(){

         @Override
         public Object genId(Context context) throws Exception {
               Object id = context.getMetaValue("rowkey");
               String agentId = BytesUtils.safeTrim(BytesUtils.toString((byte[]) id, 0, PinpointConstants.AGENT_NAME_MAX_LEN));
               return agentId;
         }
      });
```

## 7.3 hbase检索条件设置

```java
//FilterList和filter二选一，只需要设置一种
/**
 * 设置hbase检索filter
 */
SingleColumnValueFilter scvf= new SingleColumnValueFilter(Bytes.toBytes("Info"), Bytes.toBytes("i"),

      CompareOperator.EQUAL,"wap".getBytes());

scvf.setFilterIfMissing(true); //默认为false， 没有此列的数据也会返回 ，为true则只返回name=lisi的数据

importBuilder.setFilter(scvf);

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
importBuilder.setFilterList(list);

//设置同步起始行和终止行key条件
importBuilder.setStartRow(startRow);
importBuilder.setEndRow(endRow);
设置记录起始时间搓（>=）和截止时间搓(<),如果是基于时间范围的增量同步，则不需要指定下面两个参数
importBuilder.setStartTimestamp(startTimestam);
importBuilder.setEndTimestamp(endTimestamp);
```

## 7.4 HBase数据处理

必须通过DataRefactor接口处理Hbase数据并将数据添加到Elasticsearch文档中，代码示例如下：

```java
      /**
       * 设置es数据结构
       */
      importBuilder.setDataRefactor(new DataRefactor() {
         public void refactor(Context context) throws Exception  {
            //可以根据条件定义是否丢弃当前记录
            //context.setDrop(true);return;
//          if(s.incrementAndGet() % 2 == 0) {
//             context.setDrop(true);
//             return;
//          }
            //获取原始的hbase记录Result对象
            HBaseRecord hBaseRecord = (HBaseRecord) context.getRecord();
            Result result = (Result) hBaseRecord.getData();
            
            // 直接获取行key，对应byte[]类型，自行提取和分析保存在其中的数据
            byte[] rowKey = (byte[])context.getMetaValue("rowkey");
            String agentId = BytesUtils.safeTrim(BytesUtils.toString(rowKey, 0, PinpointConstants.AGENT_NAME_MAX_LEN));
            context.addFieldValue("agentId",agentId);
            long reverseStartTime = BytesUtils.bytesToLong(rowKey, HBaseTables.AGENT_NAME_MAX_LEN);
            long startTime = TimeUtils.recoveryTimeMillis(reverseStartTime);
            context.addFieldValue("startTime",new Date(startTime));
            // 通过context.getValue方法获取hbase 列的原始值byte[],方法参数对应hbase表中列名，由"列族:列名"组成
            byte[] serializedAgentInfo = (byte[]) context.getValue("Info:i");
            byte[] serializedServerMetaData = (byte[]) context.getValue("Info:m");
            byte[] serializedJvmInfo = (byte[]) context.getValue("Info:j");
            // 通过context提供的一系列getXXXValue方法，从hbase列族中获取相应类型的数据：int,string,long,double,float,date
          String agentName = context.getStringValue("Info:agentName");
             context.addFieldValue("agentName",agentName);
            final AgentInfoBo.Builder agentInfoBoBuilder = createBuilderFromValue(serializedAgentInfo);
            agentInfoBoBuilder.setAgentId(agentId);
            agentInfoBoBuilder.setStartTime(startTime);

            if (serializedServerMetaData != null) {
               agentInfoBoBuilder.setServerMetaData(new ServerMetaDataBo.Builder(serializedServerMetaData).build());
            }
            if (serializedJvmInfo != null) {
               agentInfoBoBuilder.setJvmInfo(new JvmInfoBo(serializedJvmInfo));
            }
            AgentInfo agentInfo = new AgentInfo(agentInfoBoBuilder.build());
            context.addFieldValue("agentInfo",agentInfo);
            context.addFieldValue("author","duoduo");
            context.addFieldValue("title","解放");
            context.addFieldValue("subtitle","小康");

//          context.addIgnoreFieldMapping("title");
            //上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//          context.addIgnoreFieldMapping("author");

//          //修改字段名称title为新名称newTitle，并且修改字段的值
//          context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
            context.addIgnoreFieldMapping("subtitle");
          /**
           * 获取ip对应的运营商和区域信息
           */
				IpInfo ipInfo = context.getIpInfo("Info:agentIp");
				if(ipInfo != null)
					context.addFieldValue("ipinfo", 		SimpleStringUtil.object2json(ipInfo));
				else{
					context.addFieldValue("ipinfo", "");
				}
//          DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
//          Date optime = context.getDateValue("logOpertime",dateFormat);
//          context.addFieldValue("logOpertime",optime);
//          context.addFieldValue("collecttime",new Date());

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
```

关键点：

- 获取原始的hbase记录Result对象：

 HBaseRecord hBaseRecord = (HBaseRecord) context.getRecord();
 Result result = (Result) hBaseRecord.getData();

- 获取列族中列byte[]

 byte[] serializedAgentInfo = (byte[]) context.getValue("Info:i");

- 获取具体类型列族数据，并将数据添加到Elasticsearch文档中

 // 通过context提供的一系列getXXXValue方法，从hbase列族中获取相应类型的数据：int,string,long,double,float,date
          String agentName = context.getStringValue("Info:agentName");
         context.addFieldValue("agentName",agentName);

- 获取ip对应的运营商和区域信息

  ```java
            /**
             * 获取ip对应的运营商和区域信息
             */
  				IpInfo ipInfo = context.getIpInfo("Info:agentIp");
  				if(ipInfo != null)
  					context.addFieldValue("ipinfo", 		SimpleStringUtil.object2json(ipInfo));
  				else{
  					context.addFieldValue("ipinfo", "");
  				}
  ```

  

## 7.5 定时任务配置

```java
       //定时任务配置，
      importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//              .setScheduleDate(date) //指定任务开始执行时间：日期
            .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
            .setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
      //定时任务配置结束
```

## 7.6 时间范围增量配置

```java
       //hbase表中列名，由"列族:列名"组成
//    //设置任务执行拦截器结束，可以添加多个
//    //增量字段配置
////      importBuilder.setNumberLastValueColumn("Info:id");//指定数字增量查询字段变量名称
//    importBuilder.setDateLastValueColumn("Info:logOpertime");//手动指定日期增量查询字段变量名称
      importBuilder.setFromFirst(false);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
//    importBuilder.setLastValueStorePath("hbase2esdemo_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
      //指定增量字段类型为日期类型，如果没有指定增量字段名称,则按照hbase记录时间戳进行timerange增量检索
      importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);
      // ImportIncreamentConfig.NUMBER_TYPE 数字类型
//    // ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
      //设置增量查询的起始值时间起始时间
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      try {

         Date date = format.parse("2000-01-01");
         importBuilder.setLastValue(date);
      }
      catch (Exception e){
         e.printStackTrace();
      }
      //增量配置结束
```

## 7.7  并行任务设置

```java
/**
 * 作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能
 */
importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回
```

通过Parallel参数关闭和开启并行作业机制

## 7.8 任务执行明细统计设置

```java
/**
 * 设置任务执行情况回调接口
 */
importBuilder.setExportResultHandler(new ExportResultHandler<String,String>() {
   @Override
   public void success(TaskCommand<String,String> taskCommand, String result) {
      TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
      logger.info(taskMetrics.toString());
   }

   @Override
   public void error(TaskCommand<String,String> taskCommand, String result) {
      TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
      logger.info(taskMetrics.toString());
   }

   @Override
   public void exception(TaskCommand<String,String> taskCommand, Exception exception) {
      TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
      logger.info(taskMetrics.toString());
   }

   @Override
   public int getMaxRetry() {
      return 0;
   }
});
```

## 7.9 其他配置

```java
importBuilder.setPrintTaskLog(true); //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
```

## 7.10 启动和执行作业

```java
/**
 * 执行es数据导入数据库表操作
 */
DataStream dataStream = importBuilder.builder();
dataStream.execute();//执行导入操作
```

# 8.构建部署

## 8.1 准备工作
需要通过gradle构建发布版本,gradle安装配置参考文档：

https://esdoc.bbossgroups.com/#/bboss-build

## 8.2 下载源码工程-基于gradle
<https://github.com/bbossgroups/hbase-elasticsearch>

从上面的地址下载源码工程，然后导入idea或者eclipse，根据自己的需求，修改导入程序逻辑

org.frameworkset.elasticsearch.imp.HBase2ESFullDemo

如果需要测试和调试导入功能，运行HBase2ESFullDemo的main方法即可即可：


```java
public class HBase2ESFullDemo {
	public static void main(String args[]){

		HBase2ESFullDemo dbdemo = new HBase2ESFullDemo();
        		boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值
        
        		dbdemo.scheduleTimestampImportData(dropIndice);

	}
    .....
}
```

修改es配置-hbase-elasticsearch\src\main\resources\application.properties



修改完毕配置后，就可以进行功能调试了。


测试调试通过后，就可以构建发布可运行的版本了：进入命令行模式，在源码工程根目录hbase-elasticsearch下运行以下gradle指令打包发布版本

release.bat

## 8.3 运行作业
gradle构建成功后，在build/distributions目录下会生成可以运行的zip包，解压运行导入程序

linux：

chmod +x restart.sh

./restart.sh

windows: restart.bat

## 8.4 作业jvm配置
修改jvm.options，设置内存大小和其他jvm参数

-Xms1g

-Xmx1g



 

# 9.作业参数配置

在使用[hbase-elasticsearch](https://github.com/bbossgroups/hbase-elasticsearch)时，为了避免调试过程中不断打包发布数据同步工具，可以将部分控制参数配置到启动配置文件resources/application.properties中,然后在代码中通过以下方法获取配置的参数：

```ini
#工具主程序
mainclass=org.frameworkset.elasticsearch.imp.HBase2ESFullDemo

# 参数配置
# 在代码中获取方法：CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值false
dropIndice=false
```

在代码中获取参数dropIndice方法：

```java
boolean dropIndice = CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值false
```

另外可以在resources/application.properties配置控制作业执行的一些参数，例如工作线程数，等待队列数，批处理size等等：

```
queueSize=50
workThreads=10
batchSize=20
```

在作业执行方法中获取并使用上述参数：

```java
int batchSize = CommonLauncher.getIntProperty("batchSize",10);//同时指定了默认值
int queueSize = CommonLauncher.getIntProperty("queueSize",50);//同时指定了默认值
int workThreads = CommonLauncher.getIntProperty("workThreads",10);//同时指定了默认值
importBuilder.setBatchSize(batchSize);
importBuilder.setQueue(queueSize);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(workThreads);//设置批量导入线程池工作线程数量
```

 

# 10.开发交流

## elasticsearch技术交流群:166471282 

## elasticsearch微信公众号:bbossgroup   
![GitHub Logo](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)


