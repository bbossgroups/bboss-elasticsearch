# 基于quartz调度数据同步作业案例

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

 4.发布和运行quartz定时任务：参考章节【2.4.10 发布版本】
 
# 二、原生quartz同步作业demo
 下面看一个完整quartz原生数据同步作业程序
 
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

   Logger logger = LoggerFactory.getLogger(this.getClass());

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ImportDataJob(){
        init();
        BaseApplicationContext.addShutdownHook(new Runnable() {
            @Override
            public void run() {
                destroy();
            }
        });
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            lock.lock();
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

    public void init(){
        externalScheduler = new ExternalScheduler();
        externalScheduler.dataStream((Object params)->{
            DB2DBExportBuilder importBuilder = DB2DBExportBuilder.newInstance();
            String insertsql = "INSERT INTO cetc ( age, name, create_time, update_time)\n" +
                    "VALUES ( #[age],  ## 来源dbdemo索引中的 operModule字段\n" +
                    "#[name], ## 通过datarefactor增加的字段\n" +
                    "#[create_time], ## 来源dbdemo索引中的 logContent字段\n" +
                    "#[update_time]) ## 通过datarefactor增加的地理位置信息字段";


            //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
            // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
            // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
            // log_id和数据库对应的字段一致,就不需要设置setNumberLastValueColumn和setNumberLastValueColumn信息，
            // 但是需要设置setLastValueType告诉工具增量字段的类型

//		importBuilder.setSql("select * from td_sm_log where log_id > #[log_id]");
//		importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
            /**
             * 源db相关配置
             */
            importBuilder.setSql("select * from batchtest");
            importBuilder
//                .setSqlFilepath("sql.xml")
//                .setSqlName("demoexport")
                    .setUseLowcase(false)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
                    .setPrintTaskLog(true); //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
            //项目中target数据源是配置是从application文件中加入的
            importBuilder.setTargetDbName("target")
                    .setTargetDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
                    .setTargetDbUrl("jdbc:mysql://127.0.0.1:3306/qrtz?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
                    .setTargetDbUser("root")
                    .setTargetDbPassword("123456")
                    .setTargetValidateSQL("select 1")
                    .setTargetUsePool(true)//是否使用连接池
                    .setInsertSql(insertsql); //可选项,批量导入db的记录数，默认为-1，逐条处理，> 0时批量处理
            //源数据源是从jobdatamap中传参进来的
            importBuilder.setDbName("seconde")
                    .setDbDriver("com.mysql.jdbc.Driver")
                    .setDbUrl("jdbc:mysql://127.0.0.1:3306/insertsql?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC")
                    .setDbUser("root")
                    .setDbPassword("123456")
                    .setUsePool(true);
            //定时任务配置，
            importBuilder.setFixedRate(false);//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
//                .setDeyLay(1000L); // 任务延迟执行deylay毫秒后执行
//                .setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
            //定时任务配置结束
//
            //设置任务执行拦截器，可以添加多个，定时任务每次执行的拦截器
            importBuilder.addCallInterceptor(new CallInterceptor() {
                @Override
                public void preCall(TaskContext taskContext) {
                    System.out.println("preCall");
                }

                @Override
                public void afterCall(TaskContext taskContext) {
                    System.out.println("afterCall");
                }

                @Override
                public void throwException(TaskContext taskContext, Exception e) {
                    System.out.println("throwException");
                }

            }).addCallInterceptor(new CallInterceptor() {
                @Override
                public void preCall(TaskContext taskContext) {
                    System.out.println("preCall 1");
                }

                @Override
                public void afterCall(TaskContext taskContext) {
                    System.out.println("afterCall 1");
                }

                @Override
                public void throwException(TaskContext taskContext, Exception e) {
                    System.out.println("throwException 1");
                }
            });
            importBuilder.setFromFirst(true);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
//        //setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据

            //映射和转换配置结束
            /**
             * 一次、作业创建一个内置的线程池，实现多线程并行数据导入elasticsearch功能，作业完毕后关闭线程池
             */
            importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
            importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
            importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
            importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
            importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回

            importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
            importBuilder.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false

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
            return importBuilder;
        });

    }

}

```
下面的代码将作业ImportDataJob注册到quartz引擎中：
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