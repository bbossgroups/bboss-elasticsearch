# 基于工作流的Zip文件下载与数据采集方法

bboss工作流提供了文件下载类型节点，具备文件下载、zip文件解压（包括加密zip文件），以及ftp、oss以及本地文件定期归档清理功能;可以将文件下载类型节点编排到工作流中任意位置，同时可以通过流程上下文与其他流程节点进行通信。本文介绍基于工作流的Zip文件下载与数据采集方法。

## 1.内容摘要

1.利用bboss远程文件采集节点从ftp服务器或者OSS下载Zip文件

2.远程文件采集节点解压Zip文件，将Zip文件中包含的数据文件解压存放到指定的目录，支持解压加密Zip文件

3.远程文件采集节点通过流程上下文，将解压的数据目录路径传递给数据交换节点，通过流程上下文记录每次解压文件数量

4.bboss数据交换节点从流程上下文中获取解压数据目录路径，采集和处理解压到其中的文件数据

5.数据交换节点从流程上下文中获取解压文件数量，并判断解压文件数量，如果大于0，则执行数据交换采集作业，否则不执行

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

#### 3.2.1下载情况跟踪记录器

DownloadedFileRecorder 提供两个方法：

recordBeforeDownload可以裁决是否需要下载当前文件

recordAfterDownload 用于记录当前文件下载统计信息以及下载异常信息，可以相关统计信息作为流程参数添加到流程执行上下文中（流程、流程节点、流程容器节点上下文）

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

参数DownloadFileMetrics中包含了远程文件路径、本地存放路径以及下载、解压、校验耗时信息、解压得到的文件数量以及其他说明信息：

```java
/**
     * 下载耗时
     */
    private long elapsed;
    /**
     * 解压耗时
     */
    private long unzipElapsed;
    
    /**
     * 校验耗时
     */
    private long validateElapsed;

    /**
     * 从当前压缩文件中解压的文件数量
     */
    private int files;
    /**
     * 远程文件路径
     */
    private String remoteFilePath;
    /**
     * 本地存放路径
     */
    private String localFilePath;
    /**
     * 其他说明信息
     */
    private String message;
```

#### 3.2.2 远程文件下载节点定义

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
        //如果文件下载解压成功，则记录下载信息
                if(exception == null) {
                    //获取从当前压缩文件中解压的文件数量并判断是否大于0，则将解压文件数量保存到流程上下文数据中，用于作为数据采集作业节点的触发条件（只有当前解压文件数量大于0时，才触发下一个任务节点）
                    if(downloadFileMetrics.getFiles() > 0)
                        jobFlowNodeExecuteContext.addJobFlowContextData("unzipFiles",downloadFileMetrics.getFiles());
                    downloadedFileRecorder.put(downloadFileMetrics.getRemoteFilePath(), o);
                }
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

#### 3.2.2传递csv数据文件目录路径

通过流程上下文，向后续数据采集作业传递解压后的数据文件存放目录

```java
 jobFlowNodeExecuteContext.addJobFlowContextData("csvfilepath",ftpConfig.getUnzipDir());
```

#### 3.2.3传递解压文件数量

 获取从当前压缩文件中解压的文件数量并判断是否大于0，则将解压文件数量保存到流程上下文数据中，用于作为数据采集作业节点的触发条件（只有当前解压文件数量大于0时，才触发下一个任务节点）

```java
 if(downloadFileMetrics.getFiles() > 0){
                        jobFlowNodeExecuteContext.addJobFlowContextData("unzipFiles",downloadFileMetrics.getFiles());
                        }
```

#### 3.2.4 文件筛选过滤配置

可以配置文件筛选过滤规则，实现特定文件的过滤下载, 文件筛选过滤器接口JobFileFilter：

```java
public interface JobFileFilter  {

    /**
     * 判断是否采集文件数据或者归档文件，返回true标识采集/归档，false 不采集/归档
     * @param fileInfo
     * @param jobFlowNodeExecuteContext
     * @return
     */
    boolean accept(FilterFileInfo fileInfo, JobFlowNodeExecuteContext jobFlowNodeExecuteContext);
}
```

**方法1** 指定归档的文件名称正则，匹配的文件才会被下载，内部会转换成JobFileFilter实现

相对简单的规则可以采用这种方法

```java
downloadfileConfig.setFileNameRegular(".*\\.csv")//可以指定归档的文件名称正则，匹配的文件才会被归档
```

**方法2** 自定义文件过滤器

相对复杂的规则配置可以采用自定义文件过滤器，以下是一个示例：

```java
downloadfileConfig.setJobFileFilter(new JobFileFilter() {

    /**
     * 判断是否下载文件，返回true标识下载，false 不下载
     *
     * @param fileInfo
     * @param jobFlowNodeExecuteContext
     * @return
     */
    @Override
    public boolean accept(FilterFileInfo fileInfo, JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                        String name = fileInfo.getFileName();
                        //判断是否下载文件，返回true标识下载采集，false 不下载
                        boolean nameMatch = name.startsWith("data_file_");
                        if(nameMatch){

                            /**
                             * 采集1分钟之前生成的FTP文件,本地未采集完的文件继续采集
                             */
                            Object fileObject = fileInfo.getFileObject();
                            if(fileObject instanceof RemoteResourceInfo) {
                                RemoteResourceInfo remoteResourceInfo = (RemoteResourceInfo) fileObject;
                                long mtime = remoteResourceInfo.getAttributes().getMtime()*1000;
                                long interval = System.currentTimeMillis() - mtime;
                                if(interval > 100000){
                                    return true;
                                }
                                else{
                                    return false;
                                }
                            }
                            else{
                                return true;
                            }
                            
                        } else {
                            return false;//不采集文件
                        }
                    }
    }
});
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
//4.2 为数据采集作业任务节点添加触发器，当上个节点解压文件数量大于0时，则触发数据采集作业，否则不触发
        NodeTrigger parrelnewNodeTrigger = new NodeTrigger();
        parrelnewNodeTrigger.setTriggerScriptAPI(new TriggerScriptAPI() {
            @Override
            public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                Object unzipFiles = nodeTriggerContext.getJobFlowExecuteContext().getContextData("unzipFiles");
                //当上个节点解压文件数量大于0时，则触发数据采集作业，否则不触发
                if(unzipFiles == null || ((Integer)unzipFiles) == 0){
                    return false;
                }
                else {
                    return true;
                }
            }
        });
        datatranJobFlowNodeBuilder.setNodeTrigger(parrelnewNodeTrigger);
        /**
         * 5 将第二个节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(datatranJobFlowNodeBuilder);
```

CSVUserBehaviorImport的实现类：

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/CSVUserBehaviorImport.java

#### 3.3.1获取csv数据文件目录路径

CSV文件采集插件从流程执行上下文中获取并设置csv数据文件目录路径：

```java
csvFileConfig.setSourcePath((String)jobFlowNodeExecuteContext.getJobFlowContextData("csvfilepath"));//从流程执行上下文中获取csv文件目录
```

#### 3.3.2 数据交换采集作业触发条件

数据交换节点执行触发条件：通过设置触发器，从流程执行上下文节点中获取解压后的数据文件数量，如果数据文件数量大于0时，则执行数据采集作业，否则不执行。

```java
//4.2 为数据采集作业任务节点添加触发器，当上个节点解压文件数量大于0时，则触发数据采集作业，否则不触发
        NodeTrigger parrelnewNodeTrigger = new NodeTrigger();
        parrelnewNodeTrigger.setTriggerScriptAPI(new TriggerScriptAPI() {
            @Override
            public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                Object unzipFiles = nodeTriggerContext.getJobFlowExecuteContext().getContextData("unzipFiles");
                //当上个节点解压文件数量大于0时，则触发数据采集作业，否则不触发
                if(unzipFiles == null || ((Integer)unzipFiles) <= 0){
                    return false;
                }
                else {
                    return true;
                }
            }
        });
        datatranJobFlowNodeBuilder.setNodeTrigger(parrelnewNodeTrigger);
```



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
8. **节点间通讯**：通过流程上下文实现节点间数据传递，降低模块间耦合度
9. **节点条件触发器**：通过设置节点触发器，判断是当前调度轮次中是否有产生解压文件，有则执行后续数据采集作业，否则不执行

该方案适用于需要定期从远程服务器获取压缩数据包并进行数据处理的场景，如日志收集、数据同步等业务场景。
