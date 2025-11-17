# Zip数据文件并行下载解压和采集

bboss工作流提供了文件下载类型节点，具备文件下载、zip文件解压（包括加密zip文件），以及ftp、oss以及本地文件定期归档清理功能;可以将文件下载类型节点编排到工作流中任意位置，同时可以通过流程上下文与其他流程节点进行通信。

bboss支持两种zip文件下载解压和采集模式：

**串行模式**----下载和采集串行方式运行，所有文件下载完毕后，再采集下载解压的文件数据

**并行模式**-----下载和采集并行方式运行，所有文件下载解压的同时，开始采集下载解压的文件数据，效率更高

串行模式参考文档：https://esdoc.bbossgroups.com/#/jobflow-remotefile

本文介绍基于工作流的Zip文件下载与数据并行采集模式。

## 1.内容摘要

- **并行执行**：下载与采集同时进行，提升效率
- **状态同步**：通过共享的“下载完成状态标记”协调两个子节点的结束时机
- **防重复采集**：记录已采集文件，避免重复处理
- **周期性调度**：工作流可配置为周期性执行，持续处理新增文件

## 2. 场景说明

Zip文件下载解压采集工作流：工作流只包含了一个并行节点，并行节点中包含两个子节点

![image-20251116120733345](images\workflow\remote-parrel.png)

Zip文件下载解压采集工作流只包含了一个并行节点，通过并行节点组装远程文件下载节点和文件采集作业，工作流运行时，同时启动文件下载和数据采集作业，实现并行模式；通过并行节点上下文设置解压目录，并共享给下载节点和和文件采集节点；并行节点启动时，在并行节点上下文中增加文件下载完成状态标记（初始值为false）

- 数据来源：保存在ftp服务器（也可以保存在OSS对象数据库）上的加密zip文件，其中包含一系列CSV文件，作为工作流下载采集的数据来源
- 文件下载：工作流作业每隔30秒扫描ftp或者oss服务器上的新增的zip文件，开启4个线程并行下载新增的zip文件，并将下载的zip文件解压到指定的本地数据目录；当所有zip文件下载完毕，更新下载完成状态标记为true
- 文件采集：启动文件下载的同时，启动一次性CSV文件采集作业，扫描并采集解压目录下新增的CSV数据文件，并记录采集完毕的CSV文件，避免工作流下一次启动或者调度时，文件再次被采集；当所有新增的CSV文件都被采集完，判断zip是否都已经下载完毕（文件下载完成状态标记为true），如果下载完毕，则结束CSV文件采集作业，等待继续下一轮工作流调度执行，否则等待5秒（可以调整），再次扫描新下载解压的文件，直到下载完毕。



案例源码地址：https://gitee.com/bboss/bboss-datatran-demo

工作流Ftp并行下载解压和采集案例

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowTParrelxtZipFileDownload.java

数据交换节点对应的作业定义类

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/ParrelTxtUserBehaviorImport.java

下面详细介绍。

## 🧩 一、工作流结构

工作流中只包含一个**并行节点**，该并行节点下包含两个**子节点**：

1. **Zip文件下载与解压子节点**
2. **CSV文件采集与处理子节点**

---

## 🔁 二、工作流执行流程

### 1. **开始**
- 工作流被触发（周期性调度或手动执行）

### 2. **并行节点启动**
- 初始化流程上下文
- 设置**下载完成状态标记**（初始值为 `false`）
- 设置**解压目录**，并共享给两个子节点

---

### 3. **并行执行两个子节点**

#### ✅ 子节点1：Zip文件下载与解压
- **扫描**：扫描任务服务器或OSS上的新增加密Zip文件
- **下载**：开启4个线程并行下载Zip文件
- **解压**：将Zip文件解密并解压到指定本地目录
- **更新状态**：当所有Zip文件下载并解压完成后，将下载完成状态标记更新为 `true`

#### ✅ 子节点2：CSV文件采集与处理
- **扫描**：持续扫描解压目录下的新增CSV文件
- **采集**：读取CSV文件内容，进行数据加工处理
- **入库**：将处理后的数据存入数据库
- **记录状态**：记录已采集的CSV文件，避免重复采集
- **判断结束条件**：
  - 若所有CSV文件已采集完成，且下载完成状态标记为 `true`，则结束本子节点
  - 否则等待5秒后再次扫描，直到满足结束条件

---

### 4. **并行节点结束**
- 当两个子节点都执行完毕后，并行节点结束
- 工作流进入等待状态，等待下一次调度执行

---

## 三、工作流实现

首先从gitee下载案例源码工程：https://gitee.com/bboss/bboss-datatran-demo

源码工程基于gradle管理，环境搭建可参考文档：https://esdoc.bbossgroups.com/#/bboss-build

### 1.定义工作流以及流程调度策略

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

### 2.定义并行节点

定义并行节点并添加到工作流中

```java
ParrelJobFlowNodeBuilder parrelJobFlowNodeBuilder = new ParrelJobFlowNodeBuilder();
parrelJobFlowNodeBuilder.addJobFlowNodeListener(new JobFlowNodeListener() {
    @Override
    public void beforeExecute(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
        //添加文件下载节点执行完毕标记，用于判断文件下载任务是否完成，文件下载节点下载完成时设置为true
        //并行采集任务依赖该状态标记决定是否结束一次性采集任务
        jobFlowNodeExecuteContext.addContextData("downloadNodeComplete",false);
        jobFlowNodeExecuteContext.addContextData("csvfilepath","c:/data/unzipfile");//设置解压数据文件目录，下载节点和数据采集节点从参数中获取对应的文件目录路径
    }

    @Override
    public void afterExecute(JobFlowNodeExecuteContext jobFlowNodeExecuteContext, Throwable throwable) {
        //添加文件下载节点执行完毕标记，用于判断文件下载任务是否完成，文件下载节点下载完成时设置为true
        //并行采集任务依赖该状态标记决定是否结束一次性采集任务
        jobFlowNodeExecuteContext.addContextData("downloadNodeComplete",true);
    }

    @Override
    public void afterEnd(JobFlowNode jobFlowNode) {

    }
});
jobFlowBuilder.addJobFlowNodeBuilder(parrelJobFlowNodeBuilder);
```

在并行节点上下文中初始化下载完成状态标记downloadNodeComplete为false,设置设置解压数据文件目录csvfilepath，下载节点和数据采集节点从参数中获取对应的文件解压目录路径和采集目录路径。

### 3.定义远程文件下载解压节点

定义远程文件下载节点，并添加到并行节点中：

```java
/**
         * 2.构建文件下载任务节点：Zip文件下载解压节点
         */
        RemoteFileInputJobFlowNodeBuilder remoteFileInputJobFlowNodeBuilder = new RemoteFileInputJobFlowNodeBuilder() ;
        Map downloadedFileRecorder = new ConcurrentHashMap<String,Object>();
        Object o = new Object();
        /**
         * 2.1 设置载情况跟踪记录器
         */
        remoteFileInputJobFlowNodeBuilder.setDownloadedFileRecorder(new DownloadedFileRecorder() {
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
                    //获取从当前压缩文件中解压的文件数量并判断是否大于0，则将解压文件数量保存到流程上下文数据中，并行作业中这个变量暂时没有用）
                    if(downloadFileMetrics.getFiles() > 0)
                        jobFlowNodeExecuteContext.addJobFlowContextData("unzipFiles",downloadFileMetrics.getFiles());
                    downloadedFileRecorder.put(downloadFileMetrics.getRemoteFilePath(), o);
                }
            }
        });
        /**
         * 2.2 设置Zip下载远程参数
         */
        remoteFileInputJobFlowNodeBuilder.setBuildDownloadConfigFunction(jobFlowNodeExecuteContext -> {
            FtpConfig ftpConfig = new FtpConfig().setFtpIP("172.24.176.18").setFtpPort(22)
                    .setFtpUser("wsl").setFtpPassword("123456").setDownloadWorkThreads(4).setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_SFTP)
                    .setRemoteFileDir("/mnt/c/data/1000").setSocketTimeout(600000L)
                    .setConnectTimeout(600000L)
                    .setDownloadWorkThreads(5)
                    .setSourcePath("c:/data/zipfile")//zip文件下载目录
                    .setUnzip(true)
                    .setUnzipDir((String)jobFlowNodeExecuteContext.getContainerJobFlowNodeContextData("csvfilepath"))//zip文件解压目录，从并行任务节点（当前节点的父节点）执行上下文中获取解压数据文件目录
//                    .setZipFilePassward("123456")
                    .setZipFilePasswordFunction(new ZipFilePasswordFunction() {
                        /**
                         * 根据zip文件路径获取密码
                         * @param jobFlowNodeExecuteContext 流程节点执行上下文对象
                         * @param remoteFile 远程zip文件路径
                         * @param localFilePath 本地zip文件路径
                         * @return
                         */
                        @Override
                        public String getZipFilePassword(JobFlowNodeExecuteContext jobFlowNodeExecuteContext, String remoteFile, String localFilePath) {
                            return "aaaaa.zip";
                        }
                    })
                    .setDeleteZipFileAfterUnzip(false);
            DownloadfileConfig downloadfileConfig = new DownloadfileConfig();
            downloadfileConfig
                    .setFtpConfig(ftpConfig)
                    .setScanChild(true)
                    .setJobFileFilter(new JobFileFilter() {
                        /**
                         * 判断是否采集文件数据或者归档文件，返回true标识采集/归档，false 不采集/归档
                         *
                         * @param fileInfo
                         * @param jobFlowNodeExecuteContext
                         * @return
                         */
                        @Override
                        public boolean accept(FilterFileInfo fileInfo, JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                            return fileInfo.getFileName().startsWith("behavior_");
                        }
                    })
                     ;
            return downloadfileConfig;
        });
        remoteFileInputJobFlowNodeBuilder.addJobFlowNodeListener(new JobFlowNodeListener() {
            /**
             * 作业工作流节点调度执行前拦截方法
             *
             * @param jobFlowNodeExecuteContext
             */
            @Override
            public void beforeExecute(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                
            }

            /**
             * 作业工作流节点调度执行完毕后执行方法
             *
             * @param jobFlowNodeExecuteContext
             * @param throwable
             */
            @Override
            public void afterExecute(JobFlowNodeExecuteContext jobFlowNodeExecuteContext, Throwable throwable) {
                //文件下载完毕后，修改父节点状态标记：文件下载完成状态标记为true设置
                jobFlowNodeExecuteContext.addContainerJobFlowNodeContextData("downloadNodeComplete",true);
            }

            /**
             * 作业工作流节点结束时拦截方法
             *
             * @param jobFlowNode
             */
            @Override
            public void afterEnd(JobFlowNode jobFlowNode) {

            }
        });

        /**
         * 3.将文件下载节点添加到并行工作流节点构建器
         */
        parrelJobFlowNodeBuilder.addJobFlowNodeBuilder(remoteFileInputJobFlowNodeBuilder);
```

### 4.定义文件数据采集节点

定义文件数据采集节点，并添加到并行节点中：

```java
/**
 * 4.构建第二个任务节点：数据采集作业节点
 */
DatatranJobFlowNodeBuilder datatranJobFlowNodeBuilder = new DatatranJobFlowNodeBuilder();


/**
 * 4.1设置数据采集作业构建函数
 */
datatranJobFlowNodeBuilder.setImportBuilderFunction(jobFlowNodeExecuteContext -> {
    ParrelTxtUserBehaviorImport csvUserBehaviorImport = new ParrelTxtUserBehaviorImport();
    ImportBuilder importBuilder = csvUserBehaviorImport.buildImportBuilder(jobFlowNodeExecuteContext);
    return importBuilder;
});

/**
 * 5 将第二个节点添加到并行工作流节点构建器
 */
parrelJobFlowNodeBuilder.addJobFlowNodeBuilder(datatranJobFlowNodeBuilder);
```

具体的文件数据采集作业源码地址：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/ParrelTxtUserBehaviorImport.java

文件采集作业关键点说明：

1）设置作业未一次性采集作业并记录作业增量状态：

```java
FileInputConfig fileInputConfig = new FileInputConfig();
fileInputConfig.setDisableScanNewFiles( true);
fileInputConfig.setDisableScanNewFilesCheckpoint(false);
```

2）文件采集扫描目录和采集完毕后关闭文件采集通道配置

```java
FileConfig fileConfig = new FileConfig();
fileConfig.setFileFilter(new FileFilter() {//指定文件筛选规则
            @Override
            public boolean accept(FilterFileInfo fileInfo, //文件信息
                                  FileConfig fileConfig) {
                String name = fileInfo.getFileName();
                return true;
            }
        }).setSkipHeaderLines(1)
        .setCloseOlderTime(1000L)//setIgnoreOlderTime
        .setSourcePath((String)jobFlowNodeExecuteContext.getContainerJobFlowNodeContextData("csvfilepath"));//从并行任务节点（当前节点的父节点）执行上下文中获取解压数据文件目录
fileInputConfig.addConfig(fileConfig);
```

文件采集完毕后关闭文件采集通道：通过setCloseOlderTime设置文件关闭机制，详情阅读[文档](https://esdoc.bbossgroups.com/#/filelog-guide?id=%e7%ad%96%e7%95%a5%e4%ba%8c-closeoldertimeignoreoldertime)了解。

通过以下方法从并行任务节点（当前节点的父节点）执行上下文中获取解压数据文件目录

```java
fileConfig.setSourcePath((String)jobFlowNodeExecuteContext.getContainerJobFlowNodeContextData("csvfilepath"));
```

阅读文档了解更多数据文件采集功能介绍：https://esdoc.bbossgroups.com/#/filelog-guide

3）无需设置节点触发器：串行模式下，可以为数据采集作业节点设置触发器来根据是否有新的下载解压文件来决定是否启动文件采集作业节点；在并行模式下，下载节点和采集节点会同时启动，所以无需设置节点触发器。

### 5.构建和启动工作流

```java
/**
 * 6 构建并启动工作流
 */
JobFlow jobFlow = jobFlowBuilder.build();
jobFlow.start();
```

### 6.工作流发布和部署

首先修改源码工程中的resources/application.properties文件：

```properties
mainclass=org.frameworkset.datatran.imp.file.FTPFileLog2DBDemo
```

然后运行打包指令：运行源码目录下指令，打包发布版本

windows环境：release.bat

linux/Mac环境：release.sh

打包成功后，在源码工程build/distributions目录下会生成可以运行的zip包，上传到部署服务器解压后，找到解压目录下运行指令，启动和运行demo：

linux：

sh startup.sh

sh restartup.sh

windows: 

startup.bat

restartup.bat

修改JVM参数：

打开jvm.options文件，可以设置jvm相关参数

调整内存：

```properties
-Xms1g
-Xmx1g

-XX:NewSize=512m
-XX:MaxNewSize=512m
-Xss256k
```






