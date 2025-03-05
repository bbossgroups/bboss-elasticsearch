# 基于quartz调度数据同步作业案例

bboss quartz使用参考文档：

https://doc.bbossgroups.com/#/quartz/raider

# 一、基于bboss 管理的quartz同步作业demo

 https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/QuartzES2DBImportTask.java 

调试测试以及运行quatz作业同步功能方法，按如下配置进行操作：

 1.在配置文件中添加quartz作业任务配置-[resources/org/frameworkset/task/quarts-task.xml](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/resources/org/frameworkset/task/quarts-task.xml)相关内容

```xml
<list>
	<property name="QuartzImportTask" jobid="QuartzImportTask"
					  bean-name="QuartzImportTask"
					  method="execute"
					  cronb_time="${quartzImportTask.crontime:*/20 * * * * ?}" used="true"
					  shouldRecover="false"
			/>
</list>
<!-- 作业组件配置-->
<property name="QuartzImportTask" class="org.frameworkset.elasticsearch.imp.QuartzImportTask"
		  destroy-method="destroy"
		  init-method="init"
/>
```

 2.添加一个带main方法的作业运行

```java
 public class QuartzTest {
 	public static void main(String[] args){
 		TaskService.getTaskService().startService();
        }
 }
```

 然后运行main方法即可



 3.实际运行和发布作业方法， 使用quartz定时器运行导入数据作业时，先参考第一步做quartz作业任务配置，然后将application.properties文件中的mainclass设置为如下值即可：

 

```properties
mainclass=org.frameworkset.task.Main
```

 4.发布和运行quartz定时任务：参考章节[【发布版本】](https://esdoc.bbossgroups.com/#/db-es-datasyn?id=_12-%e5%8f%91%e5%b8%83%e7%89%88%e6%9c%ac)

# 二、原生quartz同步作业demo
 下面看一个完整quartz原生数据同步作业程序
## quartz作业处理类 
quartz作业处理类[ImportDataJob.java](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/quartz/ImportDataJob.java)

 ```java
package org.frameworkset.elasticsearch.imp.quartz;

import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.db.input.db.DB2DBExportBuilder;
import org.frameworkset.tran.metrics.TaskMetrics;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.ExternalScheduler;
import org.frameworkset.tran.schedule.TaskContext;
import org.frameworkset.tran.task.TaskCommand;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 原生的数据同步quartz作业调度任务
 */
public class ImportDataJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ImportDataJob.class);
    private boolean inited ;

    public ImportDataJob(){

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            lock.lock();
            if(!inited){
                try {
                    init(context.getJobDetail().getJobDataMap());
                }
                finally {
                    inited = true;
                }
            }
            externalScheduler.execute(null);

        }
        finally {
            lock.unlock();
        }
    }
    protected ExternalScheduler externalScheduler;
    private Lock lock = new ReentrantLock();

    public void destroy(){
        if(externalScheduler != null){
            externalScheduler.destroy();
        }
    }

    public void init(JobDataMap jobDataMap){
        externalScheduler = new ExternalScheduler();
                externalScheduler.dataStream((Object params)->{
                    JobExecutionContext context = (JobExecutionContext)params;
                    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
                    Object data = jobDataMap.get("aa");
                    ImportBuilder importBuilder = ImportBuilder.newInstance();
        
        
        
                    //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
                    // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
                    // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
                    // 需要设置setLastValueColumn信息log_id，
                    // 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型
        
        
                    /**
                     * 源db相关配置
                     */
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
        
                    DBOutputConfig dbOutputConfig = new DBOutputConfig();
                    dbOutputConfig.setDbName("target")
                            .setDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
                            .setDbUrl("jdbc:mysql://localhost:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&rewriteBatchedStatements=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
                            .setDbUser("root")
                            .setDbPassword("123456")
                            .setValidateSQL("select 1")
                            .setUsePool(true)//是否使用连接池
                            .setSqlFilepath("sql.xml")
                            .setInsertSqlName("insertSql");
                    importBuilder.setOutputConfig(dbOutputConfig);
        
                    importBuilder.setBatchSize(10); //可选项,批量导入db的记录数，默认为-1，逐条处理，> 0时批量处理
                    //定时任务配置，
                    importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
                            //					 .setScheduleDate(date) //指定任务开始执行时间：日期
                            .setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
                            .setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
                    //定时任务配置结束
                    //
                    //		//设置任务执行拦截器，可以添加多个，定时任务每次执行的拦截器
                    //		importBuilder.addCallInterceptor(new CallInterceptor() {
                    //			@Override
                    //			public void preCall(TaskContext taskContext) {
                    //				System.out.println("preCall");
                    //			}
                    //
                    //			@Override
                    //			public void afterCall(TaskContext taskContext) {
                    //				System.out.println("afterCall");
                    //			}
                    //
                    //			@Override
                    //			public void throwException(TaskContext taskContext, Exception e) {
                    //				System.out.println("throwException");
                    //			}
                    //		}).addCallInterceptor(new CallInterceptor() {
                    //			@Override
                    //			public void preCall(TaskContext taskContext) {
                    //				System.out.println("preCall 1");
                    //			}
                    //
                    //			@Override
                    //			public void afterCall(TaskContext taskContext) {
                    //				System.out.println("afterCall 1");
                    //			}
                    //
                    //			@Override
                    //			public void throwException(TaskContext taskContext, Exception e) {
                    //				System.out.println("throwException 1");
                    //			}
                    //		});
                    //		//设置任务执行拦截器结束，可以添加多个
                    //增量配置开始
                    //		importBuilder.setLastValueColumn("log_id");//手动指定数字增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
                    //		importBuilder.setDateLastValueColumn("log_id");//手动指定日期增量查询字段，默认采用上面设置的sql语句中的增量变量名称作为增量查询字段的名称，指定以后就用指定的字段
                    importBuilder.setFromFirst(true);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
                    //setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
                    importBuilder.setLastValueStorePath("logdb2db_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
                    //		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
                    importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
                    // 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
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
                    //
                    final AtomicInteger s = new AtomicInteger(0);
                    importBuilder.setGeoipDatabase("d:/geolite2/GeoLite2-City.mmdb");
                    importBuilder.setGeoipAsnDatabase("d:/geolite2/GeoLite2-ASN.mmdb");
                    importBuilder.setGeoip2regionDatabase("d:/geolite2/ip2region.db");
                    /**
                     * 重新设置数据结构
                     */
                    importBuilder.setDataRefactor(new DataRefactor() {
                        public void refactor(Context context) throws Exception  {
                            //可以根据条件定义是否丢弃当前记录
                            //context.setDrop(true);return;
                            //				if(s.incrementAndGet() % 2 == 0) {
                            //					context.setDrop(true);
                            //					return;
                            //				}
        
        
                            context.addFieldValue("author","duoduo");
                            context.addFieldValue("title","解放");
                            context.addFieldValue("subtitle","小康");
                            context.addFieldValue("collecttime",new Date());//
        
                            //				context.addIgnoreFieldMapping("title");
                            //上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
                            //				context.addIgnoreFieldMapping("author");
        
                            //				//修改字段名称title为新名称newTitle，并且修改字段的值
                            //				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
                            context.addIgnoreFieldMapping("subtitle");
                            /**
                             * 获取ip对应的运营商和区域信息
                             */
                            IpInfo ipInfo = context.getIpInfo("LOG_VISITORIAL");
                            if(ipInfo != null)
                                context.addFieldValue("ipinfo", SimpleStringUtil.object2json(ipInfo));
                            else{
                                context.addFieldValue("ipinfo", "");
                            }
                            DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
                            Date optime = context.getDateValue("LOG_OPERTIME",dateFormat);
                            context.addFieldValue("logOpertime",optime);
                            context.addFieldValue("collecttime",new Date());
                            //				对数据进行格式化
                            //				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            //				Date optime = context.getDateValue("LOG_OPERTIME");
                            //
                            //				context.addFieldValue("logOpertime",dateFormat.format(optime));
        
                            /**
                             //关联查询数据,单值查询
                             Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,"test",
                             "select * from head where billid = ? and othercondition= ?",
                             context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
                             //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
                             context.addFieldValue("headdata",headdata);
                             //关联查询数据,多值查询
                             List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,"test",
                             "select * from facedata where billid = ?",
                             context.getIntegerValue("billid"));
                             //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
                             context.addFieldValue("facedatas",facedatas);
                             */
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
        
                    importBuilder.setUseLowcase(false)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
                            .setPrintTaskLog(true); //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
                    importBuilder.setExportResultHandler(new ExportResultHandler<String>() {
                        @Override
                        public void success(TaskCommand<String>taskCommand, String result) {
                            TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
                            logger.info(taskMetrics.toString());
                        }
        
                        @Override
                        public void error(TaskCommand<String>taskCommand, String result) {
                            TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
                            logger.info(taskMetrics.toString());
                        }
        
                        @Override
                        public void exception(TaskCommand<String>taskCommand, Exception exception) {
                            TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
                            logger.info(taskMetrics.toString());
                        }
        
                        @Override
                        public int getMaxRetry() {
                            return 0;
                        }
                    });
                   return importBuilder;        
                });

    }

}

 ```

## 调度执行quartz作业
下面的代码将作业ImportDataJob注册到quartz引擎中并调度执行：
[Bootstrap.java](https://github.com/bbossgroups/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/quartz/Bootstrap.java)
```java
 public  void quartz(Scheduler scheduler) throws InterruptedException, SchedulerException {
        //2020.8.19在这定义加载的任务类,然后业务逻辑是每一次都在
        JobDetail jobDetail = JobBuilder.newJob(ImportDataJob.class)

                .withIdentity("test1", "group1").build();
        // 3、构建Trigger实例,每隔1s执行一次
        Trigger trigger = newTrigger()
                .withIdentity("test1", "group1")
                .withSchedule(cronSchedule("0/30 * * * * ? "))
                .forJob("test1", "group1")
                .build();
        //4、执行
        scheduler.scheduleJob(jobDetail, trigger);
        System.out.println("--------scheduler start ! ------------");
        scheduler.start();
        //睡眠
//        TimeUnit.MINUTES.sleep(2);
//        scheduler.shutdown();
        System.out.println("--------scheduler shutdown ! ------------");
    }
```