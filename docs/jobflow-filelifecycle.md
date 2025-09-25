# 基于工作流的文件定期归档清理介绍

bboss工作流提供了文件下载类型节点，具备文件下载、zip文件解压（包括加密zip文件），以及ftp、oss以及本地文件定期归档清理功能。本文以实际案例，介绍基于工作流的文件归档清理功能。

## 1.内容摘要

bboss工作流提供了基于工作流的文件归档清理功能，支持三种文件归档类型：
- **FTP文件归档**：定期归档FTP服务器上的文档
- **OSS文件归档**：定期归档OSS服务器上的文档  
- **本地文件归档**：定期归档本地服务器上的文档

## 2.核心特性

### 2.1. 统一的工作流调度机制
- 使用`JobFlowBuilder`构建工作流
- 通过`JobFlowScheduleConfig`配置定时执行策略
- 支持延时执行和周期性调度执行

### 2.2. 灵活的归档配置
所有归档类型都支持以下配置选项：
- 文件有效期设置（`setFileLiveTime`）
- 文件名称正则匹配（`setFileNameRegular`）
- 归档标记设置（`setLifecycle`）
- 子目录扫描（`setScanChild`）

### 2.3. 不同归档类型的特定配置
- **FTP归档**：配置FTP服务器IP、端口、用户名、密码等参数
- **OSS归档**：配置OSS访问密钥、endpoint、bucket等参数
- **本地归档**：配置本地文件路径（`setSourcePath`）

## 3.实现方式
通过`RemoteFileInputJobFlowNodeBuilder`构建文件归档节点，结合`DownloadfileConfig`进行具体参数配置，最终通过`JobFlow`启动执行。整个过程采用声明式配置，易于维护和扩展。

## 4.案例介绍

### 4.1文件归档定期策略配置

三种文件归档类型都采用bboss的工作流定期执行策略：

```java
/**
         * 1.定义工作流以及流程调度策略：流程启动后，延后5秒后开始执行，每隔30秒周期性调度执行
         */
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

定义好流程定时执行策略，接下来分别介绍三种文件归档类型。

### 4.2 ftp文件归档

ftp文件归档可以定期归档ftp服务器上的文档，通过指定ftp服务器参数、ftp远程文件目录、最长文件有效期、文件筛选规则，从而归档清理过期的ftp文件：

```java
/**
         * 2.构建第一个任务节点：远程文件归档节点
         */
        RemoteFileInputJobFlowNodeBuilder jobFlowNodeBuilder = new RemoteFileInputJobFlowNodeBuilder() ;
        
        /**
         * 2.2 FTP文件归档远程参数
         */
        jobFlowNodeBuilder.setBuildDownloadConfigFunction(jobFlowNodeExecuteContext -> {
            //指定ftp服务器参数以及归档的远程目录
            FtpConfig ftpConfig = new FtpConfig().setFtpIP("172.24.176.18").setFtpPort(22)
                    .setFtpUser("wsl").setFtpPassword("123456").setDownloadWorkThreads(4)
                    .setTransferProtocol(FtpConfig.TRANSFER_PROTOCOL_SFTP)
                    .setRemoteFileDir("/mnt/c/data/1000")//指定需归档的FTP远程文件目录，定期归档其下面的过期文件
                    .setSocketTimeout(600000L)
                    .setConnectTimeout(600000L); 
            DownloadfileConfig downloadfileConfig = new DownloadfileConfig();
            downloadfileConfig
                    .setFtpConfig(ftpConfig)
                    .setScanChild(true)
                    .setFileLiveTime(7 * 24 * 60 * 60 * 1000L)//设置归档文件保存时间，超过7天则归档
                    .setLifecycle(true)//设置归档标记为true
                    .setFileNameRegular(".*\\.csv")//可以指定归档的文件名称正则，匹配的文件才会被归档
                    ;
            return downloadfileConfig;
        });     

        /**
         * 3.将第一个节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);
```

构建和启动ftp文件归档流程：

```java
/**
 * 4 构建并启动工作流
 */
JobFlow jobFlow = jobFlowBuilder.build();
jobFlow.start();
```

### 4.3 OSS文件归档

OSS文件归档可以定期归档OSS服务器上的文档，通过指定OSS服务器参数、OSS Bulket、最长文件有效期、文件筛选规则，从而归档清理过期的OSS文件：

```java
        /**
         * 2.构建第一个任务节点：远程文件归档节点
         */
        RemoteFileInputJobFlowNodeBuilder jobFlowNodeBuilder = new RemoteFileInputJobFlowNodeBuilder() ;
        
        /**
         * 2.2 设置OSS文件归档远程参数
         */
        jobFlowNodeBuilder.setBuildDownloadConfigFunction(jobFlowNodeExecuteContext -> {
            //指定OSS服务器参数以及归档的远程目录
            OSSFileInputConfig ossFileInputConfig = new OSSFileInputConfig()
                    .setName("miniotest")
                    .setAccessKeyId("N3XNZFqSZfpthypuoOzL")
                    .setSecretAccesskey("2hkDSEll1Z7oYVfhr0uLEam7r0M4UWT8akEBqO97").setRegion("east-r-a1")
                    .setEndpoint("http://172.24.176.18:9000")
                    .setDownloadWorkThreads(4)
                    .setBucket("zipfile")//指定需归档的OSS Bucket目录，定期归档其下面的过期文件
                    .setSocketTimeout(600000L)
                    .setConnectTimeout(600000L)
                    ;
            DownloadfileConfig downloadfileConfig = new DownloadfileConfig();
            downloadfileConfig
                    .setOssFileInputConfig(ossFileInputConfig)
                    .setScanChild(true)
                    .setFileLiveTime(1 * 24 * 60 * 60 * 1000L)//设置归档文件保存时间，超过7天则归档
                    .setLifecycle(true)//设置归档标记为true
                    .setFileNameRegular(".*\\.zip")//可以指定归档的文件名称正则，匹配的文件才会被归档
                    ;
            return downloadfileConfig;
        });    

        /**
         * 3.将第一个节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);
```

构建和启动OSS文件归档流程：

```java
/**
 * 4 构建并启动工作流
 */
JobFlow jobFlow = jobFlowBuilder.build();
jobFlow.start();
```

### 4.4 本地文件归档

文件本地归档可以定期归档本地服务器上的文档，通过指定归档目录、最长文件有效期、文件筛选规则，从而归档清理过期的本地文件：

```java
        /**
         * 2.构建第一个任务节点：本地文件归档节点
         */
        RemoteFileInputJobFlowNodeBuilder jobFlowNodeBuilder = new RemoteFileInputJobFlowNodeBuilder() ;
        
        /**
         * 2.2 设置本地文件归档参数
         */
        jobFlowNodeBuilder.setBuildDownloadConfigFunction(jobFlowNodeExecuteContext -> {
            //指定本地文件有效期和对应的目录         
            DownloadfileConfig downloadfileConfig = new DownloadfileConfig();
            downloadfileConfig
                    .setSourcePath("c:/data/1000")//指定文件目录
                    .setScanChild(true)
                    .setFileLiveTime(7 * 24 * 60 * 60 * 1000L) //设置归档文件保存时间，超过7天则归档
                    .setLifecycle(true) //设置归档标记为true
                    .setFileNameRegular(".*\\.csv")//可以指定归档的文件名称正则，匹配的文件才会被归档
                    ;
            return downloadfileConfig;
        });     

        /**
         * 3.将第一个节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);

```

构建和启动本地文件归档流程：

```java
/**
 * 4 构建并启动工作流
 */
JobFlow jobFlow = jobFlowBuilder.build();
jobFlow.start();
```

## 5.案例源码

Ftp文件归档案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowFileLifecycleClean.java

OSS文件归档案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowOSSFileLifecycleClean.java

本地文件归档案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowLocalFileLifecycleClean.java