# 监控日志API使用介绍

## 1. 概述

bboss数据处理作业提供监控日志收集模块：

1. 日志级别分为：debug,info,warn,error,不输出日志
2. 可以在作业初始化、数据处理接口方法中调用日志接口方法记录和上报日志;
3. 通过MetricsLogReport接口输出和保存日志
4. 可以在按需开发作业可视化监控功能，查看记录的作业和作业任务日志；
5. 日志可以采用bboss提供的[通用异步批处理组件](https://esdoc.bbossgroups.com/#/bulkProcessor-common)和[Elasticsearch异步批处理组件](https://esdoc.bbossgroups.com/#/bulkProcessor)入库，二不影响作业加工处理性能和速度；

监控日志收集模块可以和[数据同步任务执行统计信息获取模块](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2817-%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e7%bb%9f%e8%ae%a1%e4%bf%a1%e6%81%af%e8%8e%b7%e5%8f%96)配合一起记录作业的全流程监控日志。

下面介绍监控日志收集模块具体的使用方法

## 2.日志记录

可以通过Context接口和TaskContext、JobContext提供的方法记录日志

### 2.1 基于Context记录监控日志

可以在具有context参数的地方使用以下方法记录日志

```java

                
                 * 记录作业处理过程中的异常日志
                    context.reportJobMetricErrorLog( String msg, Throwable e) 
                 * 记录作业处理过程中的日志
                    context.reportJobMetricLog(String msg)                  
                 * 记录作业处理过程中的告警日志
                    context.reportJobMetricWarn(  String msg) 
                    
                 * 记录作业处理过程中的debug日志
                    context.reportJobMetricDebug(  msg);   
                 
                 * 记录作业任务处理过程中的异常日志
                    context.reportTaskMetricErrorLog(String msg, Throwable e)                  
                 * 记录作业任务处理过程中的日志
                    context.reportTaskMetricLog( String msg) 
                 * 记录作业任务处理过程中的告警日志     
                    context.reportTaskMetricWarn(String msg) 
                    
                 * 记录作业任务处理过程中的debug日志
                    context.reportTaskMetricDebug(  msg);  
            
```

### 2.2 基于TaskContext记录监控日志

可以在具有TaskContext参数的地方使用以下方法记录日志：

```java

                  
                    * 记录作业处理过程中的异常日志
                    taskContext.reportJobMetricErrorLog(   msg, e);  
                    * 记录作业处理过程中的日志
                    taskContext.reportJobMetricLog( msg);
                 
                    * 记录作业处理过程中的告警日志
                    taskContext.reportJobMetricWarn(  msg);
                    
                    * 记录作业处理过程中的debug日志
                      taskContext.reportJobMetricDebug(String msg)  
                 
```

### 2.3 基于JobContext记录监控日志

使用jobContext输出监控日志：

```java
 * 记录作业处理过程中的异常日志
                 
                jobContext.reportJobMetricErrorLog( String msg, Throwable e)  
            
                 
                 * 记录作业处理过程中的日志
                 
                jobContext.reportJobMetricLog(  String msg) 
            
                 
                 * 记录作业处理过程中的告警日志
                 
                jobContext.reportJobMetricWarn( String msg)  
                
                * 记录作业处理过程中的debug日志
                      jobContext.reportJobMetricDebug(String msg)   
```

## 3.日志输出和保存

通过org.frameworkset.tran.metrics.MetricsLogReport接口输出和保存日志

### 3.1 接口定义

```java
/**
     * 记录作业处理过程中的异常日志
     * @param taskContext
     * @param msg
     * @param e
     */
    public void reportJobMetricErrorLog(TaskContext taskContext, String msg, Throwable e);

    /**
     * 记录作业处理过程中的一般日志
     * @param taskContext
     * @param msg
     */
    public void reportJobMetricLog(  TaskContext taskContext,String msg);

    /**
     * 记录作业处理过程中的告警日志
     * @param taskContext
     * @param msg
     */
      public void reportJobMetricWarn(TaskContext taskContext,String msg);

    /**
     * 记录作业处理过程中的异常日志
     * @param taskMetrics
     * @param msg
     * @param e
     */
    public void reportTaskMetricErrorLog(TaskMetrics taskMetrics, String msg, Throwable e);

    /**
     * 记录作业处理过程中的一般日志
     * @param taskMetrics
     * @param msg
     */
    public void reportTaskMetricLog(TaskMetrics taskMetrics,String msg);

    /**
     * 记录作业处理过程中的告警日志
     * @param msg
     */
     public void reportTaskMetricWarn( TaskMetrics taskMetrics, String msg);

    /**
     * 记录作业处理过程中的debug日志
     * @param taskContext
     * @param msg
     */
    void reportJobMetricDebug(TaskContext taskContext, String msg);

    /**
     * 记录作业子任务处理过程中的debug日志
     * @param taskMetrics
     * @param msg
     */
    void reportTaskMetricDebug(TaskMetrics taskMetrics, String msg);
}
```

### 3.2 参数说明

通过TaskContext可以获取到作业调度执行的相关数据，便于保存记录日志数据时和作业进行关联保存

```java
taskContext.getJobNo(); //每次调度生成的任务号
taskContext.getJobId(); //作业全局id
taskContext.getJobName(); //作业名称
Date jobStartTime = taskContext.getJobTaskMetrics() != null ?taskContext.getJobTaskMetrics().getJobStartTime():null;//获取任务调度开始时间
```

通过TaskMetrics可以获取到作业调度执行的相关数据，便于保存记录日志数据时和作业进行关联保存

```java
taskMetrics.getJobStartTime();//获取任务调度开始时间
          taskMetrics.getTaskStartTime();//子任务开始时间
          dataMiningTaskMetrics.setTaskEndTime(taskMetrics.getTaskEndTime();//子任务结束时间
           
          taskMetrics.getTaskNo();//子任务号
          taskMetrics.getJobNo();//每次调度生成的任务号
          taskMetrics.getJobId();//作业全局id
          taskMetrics.getJobName();//作业名称
          taskMetrics.getElapsed();//子任务执行耗时
```

### 3.3 接口配置

通过ImportBuilder来设置MetricsLogReport接口，使用案例：

```java
importBuilder.setMetricsLogReport(new MetricsLogReport() {
    /**
     * 记录作业处理过程中的异常日志
     *
     * @param taskContext
     * @param msg
     * @param e
     */
    @Override
    public void reportJobMetricErrorLog(TaskContext taskContext, String msg, Throwable e) {
       DataMiningMetricsLogUtil.reportJobMetricsLog(taskContext,jobExecutorContext,msg,e, metricsType_EXCEPTION);
    }
     
    
     
 
    /**
     * 记录作业处理过程中的一般日志
     *
     * @param taskContext
     * @param msg
     */
    @Override
    public void reportJobMetricLog(TaskContext taskContext, String msg) {
       DataMiningMetricsLogUtil.reportJobMetricsLog(taskContext,jobExecutorContext,msg,null, metricsType_LOG);
    }
    
    /**
     * 记录作业处理过程中的告警日志
     *
     * @param taskContext
     * @param msg
     */
    @Override
    public void reportJobMetricWarn(TaskContext taskContext, String msg) {
       DataMiningMetricsLogUtil.reportJobMetricsLog(taskContext,jobExecutorContext,msg,null, metricsType_WARN);
    }
    
    /**
     * 记录作业处理过程中的异常日志
     *
     * @param taskMetrics
     * @param msg
     * @param e
     */
    @Override
    public void reportTaskMetricErrorLog(TaskMetrics taskMetrics, String msg, Throwable e) {
       DataMiningMetricsLogUtil.reportTaskMetricsLog(jobExecutorContext,taskMetrics, metricsType_EXCEPTION,e, msg);
    }
    
    /**
     * 记录作业处理过程中的一般日志
     *
     * @param taskMetrics
     * @param msg
     */
    @Override
    public void reportTaskMetricLog(TaskMetrics taskMetrics, String msg) {
       DataMiningMetricsLogUtil.reportTaskMetricsLog(jobExecutorContext,taskMetrics, metricsType_LOG,null, msg);
    }
    
    /**
     * 记录作业处理过程中的告警日志
     *
     * @param taskMetrics
     * @param msg
     */
    @Override
    public void reportTaskMetricWarn(TaskMetrics taskMetrics, String msg) {
       DataMiningMetricsLogUtil.reportTaskMetricsLog(jobExecutorContext,taskMetrics, metricsType_WARN,null, msg);
    }
    
    /**
     * 记录作业处理过程中的debug日志
     *
     * @param taskContext
     * @param msg
     */
    @Override
    public void reportJobMetricDebug(TaskContext taskContext, String msg) {
       DataMiningMetricsLogUtil.reportJobMetricsLog(taskContext,jobExecutorContext,msg,null, metricsType_DEBUG);
    }
    
    /**
     * 记录作业子任务处理过程中的debug日志
     *
     * @param taskMetrics
     * @param msg
     */
    @Override
    public void reportTaskMetricDebug(TaskMetrics taskMetrics, String msg) {
       DataMiningMetricsLogUtil.reportTaskMetricsLog(jobExecutorContext,taskMetrics, metricsType_DEBUG,null, msg);
    }
});
```

其中的DataMiningMetricsLogUtil是实际业务系统的日志记录存储组件，可以替换为自己的实现

### 3.4 日志存储

日志存储需要业务系统自行实现，可以基于Clickhouse和Elasticsearch、MongoDB等分布式数据存储日志数据

## 4.日志级别设置
作业日志级别定义如下：
```java
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
可以在作业配置的时候指定初始日志级别

```java
importBuilder.setMetricsLogLevel(basicInfo.getMetricsLogLevel());
```

亦可以在作业运行时调整日志级别,无需重启作业

```java
dataStream.resetMetricsLogLevel(newMetricsLogLevel);
   
```

## 5.日志可视化

日志可视化需要自行额外开发，以下是一个日志监控可视化实例：
基础信息

![image-20241102114412179](images\log\log1.png)
任务日志列表
![image-20241102114715837](images\log\log3.png)
子任务日志列表
![image-20241102114831937](images\log\log4.png)
异常信息
![image-20241102114548272](images\log\log2.png)
