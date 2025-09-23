# 基于工作流的Zip文件下载与数据采集方法

本文介绍基于bboss工作流的Zip文件下载与数据采集方法。

## 1.内容摘要

1.利用bboss远程文件采集节点从ftp服务器或者OSS下载Zip文件

2.解压Zip文件，将Zip文件中包含的数据文件存放到指定的目录，支持解压加密Zip文件

3.利用bboss数据交换节点采集解压后的数据文件

## 2. 场景说明

![](images\workflow\zipcsv.jpg)

保存在ftp服务器（也可以保存在OSS对象数据库）上的zip文件中包含一系列CSV文件，工作流作业每隔30秒扫描ftp服务器上的新增的zip文件，开启4个线程并行下载新增的zip文件，并将下载的zip文件解压到指定的本地数据目录；增量的zip文件下载解压完成后，启动一次性CSV文件采集作业，采集新解压的所有CSV文件中的数据，并记录采集完毕的CSV文件，避免工作流下一次调度时，文件再次被采集；当所有新增的CSV文件都采集完毕，结束CSV文件采集作业，等待继续下一轮工作流调度执行。

案例源码：

Ftp下载案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowZipFileDownload.java

OSS下载案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowOSSZipFileDownload.java

数据交换节点对应的作业定义类

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/CSVUserBehaviorImport.java

下面详细介绍。

## 3.案例实现

### 3.1 定义工作流以及流程调度策略

定义工作流以及流程调度策略：流程启动后，延后5秒后开始执行，每隔30秒周期性调度执行

```java
JobFlowBuilder jobFlowBuilder = new JobFlowBuilder();
        jobFlowBuilder.setJobFlowName("测试流程")
                .setJobFlowId("测试id");
        JobFlowScheduleConfig jobFlowScheduleConfig = new JobFlowScheduleConfig();
//        jobFlowScheduleConfig.setScheduleDate(TimeUtil.addDateHours(new Date(),2));//2小时后开始执行
        jobFlowScheduleConfig.setScheduleDate(TimeUtil.addDateSeconds(new Date(),5));//5秒后开始执行
//        jobFlowScheduleConfig.setScheduleEndDate(TimeUtil.addDates(new Date(),10));//10天后结束
//        jobFlowScheduleConfig.setScheduleEndDate(TimeUtil.addDateMinitues(new Date(),10));//2分钟后结束
        jobFlowScheduleConfig.setPeriod(30000L);
//        jobFlowScheduleConfig.setExecuteOneTime(true);
//        jobFlowScheduleConfig.setExecuteOneTimeSyn(true);
        jobFlowBuilder.setJobFlowScheduleConfig(jobFlowScheduleConfig);
```

### 3.2 远程文件下载流程节点

RemoteFileInputJobFlowNodeBuilder：构建远程文件下载、解压流程节点配置类，用于构建文件下载解压工作流节点。

**远程参数设置**

Ftp下载参数，通过FtpConfig配置Ftp服务器参数、远程文件目录、本地下载目录、zip文件存放目录、解压文件目录、zip文件口令等参数

OSS下载参数，通过OSSFileInputConfig配置OSS服务器参数、远程文件目录、本地下载目录、zip文件存放目录、解压文件目录、zip文件口令等参数

**下载情况跟踪记录器设置**

DownloadedFileRecorder 提供两个方法，裁决是否需要

```java
/**
 * 通过本方法记录下载文件信息，同时亦可以判断文件是否已经下载过，如果已经下载过则返回false，忽略下载，否则返回true允许下载
 * @param downloadFileMetrics
 * @param jobFlowNodeExecuteContext
 * @return
 */


public boolean recordBeforeDownload(DownloadFileMetrics downloadFileMetrics, JobFlowNodeExecuteContext jobFlowNodeExecuteContext)

 /**
             * 文件下载完毕或者错误时调用本方法记录已完成或者下载失败文件信息，可以自行存储下载文件信息，以便下次下载时判断文件是否已经下载过             *
             * @param downloadFileMetrics
             * @param jobFlowNodeExecuteContext
             * @param exception 
             */
public void recordAfterDownload(DownloadFileMetrics downloadFileMetrics, JobFlowNodeExecuteContext jobFlowNodeExecuteContext, Throwable exception)    
```

远程文件下载节点定义：

```java
/**
 * 2.构建第一个任务节点：Zip文件下载解压节点
 */
RemoteFileInputJobFlowNodeBuilder jobFlowNodeBuilder = new RemoteFileInputJobFlowNodeBuilder() ;
Map downloadedFileRecorder = new ConcurrentHashMap<String,Object>();
Object o = new Object();
/**
 * 2.1 设置载情况跟踪记录器
 */
jobFlowNodeBuilder.setDownloadedFileRecorder(new DownloadedFileRecorder() {
    /**
     * 通过本方法记录下载文件信息，同时亦可以判断文件是否已经下载过，如果已经下载过则返回false，忽略下载，否则返回true允许下载
     * @param downloadFileMetrics
     * @param jobFlowNodeExecuteContext
     * @return
     */
    @Override
    
    public boolean recordBeforeDownload(DownloadFileMetrics downloadFileMetrics, JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
        //如果文件已经下载过，则返回false，忽略下载,一般会持久化保存到数据库中
        if(downloadedFileRecorder.get(downloadFileMetrics.getRemoteFilePath()) != null)
            return false;
        return true;
    }

    /**
     * 文件下载完毕或者错误时调用本方法记录已完成或者下载失败文件信息，可以自行存储下载文件信息，以便下次下载时判断文件是否已经下载过             *
     * @param downloadFileMetrics
     * @param jobFlowNodeExecuteContext
     * @param exception 
     */
    @Override
    public void recordAfterDownload(DownloadFileMetrics downloadFileMetrics, JobFlowNodeExecuteContext jobFlowNodeExecuteContext, Throwable exception) {
        //如果文件成功，则记录下载信息
        if(exception == null)
            downloadedFileRecorder.put(downloadFileMetrics.getRemoteFilePath(),o);
    }
});
/**
 * 2.2 设置Zip下载远程参数
 */
jobFlowNodeBuilder.setBuildDownloadConfigFunction(jobFlowNodeExecuteContext -> {
    FtpConfig ftpConfig = new FtpConfig().setFtpIP("172.24.176.18").setFtpPort(22)
            .setFtpUser("wsl").setFtpPassword("123456").setDownloadWorkThreads(4).setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_SFTP)
            .setRemoteFileDir("/mnt/c/xxxx/公司项目/xxxx/数据分析/zip") //远程ftp文件目录
            .setSocketTimeout(600000L)
            .setConnectTimeout(600000L)
            .setUnzip(true) //下载后解压 true 解压  false 不接呀
            .setUnzipDir("c:/data/unzipfile")//zip文件解压目录
            .setZipFilePassward("123456")//设置解压口令，如果zip文件没加密则忽略
            .setDeleteZipFileAfterUnzip(false)//解压后是否删除zip文件，true 删除  false不删除
            .setSourcePath("c:/data/zipfile");//zip文件下载存放目录，
    //向后续数据采集作业传递数据文件存放目录
    jobFlowNodeExecuteContext.addJobFlowContextData("csvfilepath",ftpConfig.getUnzipDir());
    DownloadfileConfig downloadfileConfig = new DownloadfileConfig();
    downloadfileConfig
            .setFtpConfig(ftpConfig)
            .setScanChild(true) //是否扫描子目录
            .setFileNameRegular(".*\\.zip");//指定要下载的文件名规则，复杂的可以设定JobFileFilter
    return downloadfileConfig;
});
/**
 * 3.将第一个节点添加到工作流构建器
 */
jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);
```

### 3.3 数据交换流程节点

DatatranJobFlowNodeBuilder：配置和构建数据交换流程节点配置类，用配置和构建数据交换流程节点

```java
/**
 * 4.构建第二个任务节点：数据采集作业节点
 */
DatatranJobFlowNodeBuilder datatranJobFlowNodeBuilder = new DatatranJobFlowNodeBuilder();


/**
 * 4.1设置作业构建函数
 */
datatranJobFlowNodeBuilder.setImportBuilderFunction(jobFlowNodeExecuteContext -> {
    CSVUserBehaviorImport csvUserBehaviorImport = new CSVUserBehaviorImport();
    return csvUserBehaviorImport.buildImportBuilder(jobFlowNodeExecuteContext);
});

        /**
         * 5 将第二个节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(datatranJobFlowNodeBuilder);
```

CSVUserBehaviorImport的实现类：

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/CSVUserBehaviorImport.java

### 3.4 构建和启动作业

构建和启动作业

```java
/**
 * 6 构建并启动工作流
 */
JobFlow jobFlow = jobFlowBuilder.build();
jobFlow.start();
```

## 4. 总结

本文介绍了基于bboss工作流的Zip文件下载与数据采集完整解决方案，该方案具有以下特点：

1. **自动化流程**：通过工作流调度机制，实现从远程服务器下载Zip文件、解压、数据采集的全自动化处理流程。

2. **灵活的远程文件支持**：支持FTP/SFTP和OSS等多种远程存储方式，满足不同环境下的文件获取需求。

3. **并发处理能力**：支持多线程并行下载，提高大文件或多个文件的下载效率。

4. **安全可靠的解压机制**：支持加密Zip文件的解压，并可配置解压后是否删除源文件。

5. **防止重复处理**：通过DownloadedFileRecorder记录器跟踪已处理文件，避免重复采集。

6. **增量处理机制**：结合工作流调度，实现增量文件的识别与处理。

7. **易于扩展**：采用模块化设计，可轻松扩展支持其他类型的文件处理和数据源。

该方案适用于需要定期从远程服务器获取压缩数据包并进行数据处理的场景，如日志收集、数据同步等业务场景。
