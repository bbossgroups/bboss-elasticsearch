# 基于工作流的文件定期归档清理介绍

bboss工作流提供了文件下载类型节点，具备文件下载、zip文件解压（包括加密zip文件），以及ftp、oss以及本地文件定期归档清理功能;可以将文件下载类型节点编排到工作流中任意位置，同时可以通过流程上下文与其他流程节点进行通信。本文以实际案例，介绍基于工作流的文件归档清理功能。

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

### 4.5 文件筛选过滤配置

通过设置归档文件有效时间和文件目录，可以定期归档清理目录下的过期文件，同时还可以配置文件筛选过滤规则，实现特定文件的归档清理。

注意：如果通过setLifecycle设置归档标记为true，这样将会将文件下载节点设置为文件归档节点，请务必指定文件保存时长或者指定归档清理文件过滤器任何一种，否则将抛出异常，二者可以任意指定一种（只要满足即可归档文件），也可以同时指定（二者将同时起作用，必须同时满足，文件才会被归档清理）

1）设置归档文件保存时间

通过设置归档文件保存时间，如果没有额外设置文件筛选过滤配置，则只要检测到目录下文件超过过期时间，将直接被归档清理，否则还需要结合过滤器执行结果判断文件是否会被规则（过滤器返回true归档，false不归档）

```java
downloadfileConfig.setFileLiveTime(7 * 24 * 60 * 60 * 1000L) //设置归档文件保存时间，超过7天则归档
```

2) 文件筛选过滤配置

如果没有设置归档文件保存时间，那么流程就会按照JobFileFilter来控制文件是否被归档。

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

**方法1** 指定归档的文件名称正则，匹配的文件才会被归档，内部会转换成JobFileFilter实现

相对简单的规则可以采用这种方法，可以结合文件保存时间一起使用

```java
downloadfileConfig.setFileNameRegular(".*\\.csv")//可以指定归档的文件名称正则，匹配的文件才会被归档
```

**方法2** 自定义文件过滤器

相对复杂的规则配置可以采用自定义文件过滤器，如果没有指定文件保存时间,则可以在接口中调用fileInfo.getLastModified()获取文件最后修改时间（支持ftp文件、oss文件和本地文件），自行判断是否过期；如果指定了文件保存时间，则过滤器无需判断有效期时间，只需设置其他规则即可，以下是一个示例：

```java
downloadfileConfig.setJobFileFilter(new JobFileFilter() {

    /**
     * 判断是否归档文件，返回true标识归档，false 不归档
     *
     * @param fileInfo
     * @param jobFlowNodeExecuteContext
     * @return
     */
    @Override
    public boolean accept(FilterFileInfo fileInfo, JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                        String name = fileInfo.getFileName();
                        //判断是否归档文件，返回true标识采集，false 不归档
                        boolean nameMatch = name.startsWith("data_file_");
                        if(nameMatch){

                            /**
                             * 归档7天之前文件
                             */
                            long lastModifyTime = fileInfo.getLastModified();
                             
                            long interval = System.currentTimeMillis() - lastModifyTime;
                            if(interval > 7 * 24 * 60 * 60 * 1000L){
                                return true;//超过7天，归档文件
                            }
                            else{
                                return false;//不归档文件
                            }
                            
                        } else {
                            return false;//不归档文件
                        }
                    }
    }
});
```

## 5.案例源码

Ftp文件归档案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowFileLifecycleClean.java

OSS文件归档案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowOSSFileLifecycleClean.java

本地文件归档案例

https://gitee.com/bboss/bboss-datatran-demo/tree/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowLocalFileLifecycleClean.java