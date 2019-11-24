- 	*如何快速将保存在 MongoDB 中的海量数据同步到 Elasticshearch 中是一件非常具有挑战意义的事情，本话题分享基于分布式任务调度引擎、多线程高并发技术快速将保存在 MongoDB 中的海量数据同步到 Elasticshearch 中实战技术和经验。* 

  # 1.数据同步概述

  先介绍一下本次实践中需要使用的数据同步工具-mongodb-elasticsearch![bboss数据同步工具](https://esdoc.bbossgroups.com/images/datasyn.png)
  通过mongodb-elasticsearch，可以非常方便地实现：

   - 将数据库表数据同步到Elasticsearch
   - 将数据库表数据同步到数据库表
   - 将Elasticsearch数据同步到数据库表
   - 将mongodb数据同步到Elasticsearch
   - 将mongodb数据同步到数据库表
   - 从kafka接收数据导入elasticsearch（支持kafka_2.12-0.10.2.0和kafka_2.12-2.3.0 系列版本）

  支持的导入方式
   - 逐条数据导入
   - 批量数据导入
   - 批量数据多线程并行导入
   - 定时全量（串行/并行）数据导入
   - 定时增量（串行/并行）数据导入

  支持的数据库： mysql,maridb，postgress,oracle ,sqlserver,db2,tidb,hive，mongodb等
  支持的Elasticsearch版本： 1.x,2.x,5.x,6.x,7.x,+
  支持将ip转换为对应的运营商和城市地理位置信息
  支持多种定时任务执行引擎：
   - jdk timer （内置）
  - quartz
  - xxl-job分布式调度引擎，基于分片调度机制实现海量数据快速同步能力

  mongodb-elasticsearch另一个显著的特色就是直接基于java语言来编写数据同步作业程序，基于强大的java语言和第三方工具包，能够非常方便地加工和处理需要同步的源数据，然后将最终的数据保存到目标库（Elasticsearch或者数据库）；同时也可以非常方便地在idea或者eclipse中调试和运行同步作业程序，调试无误后，通过mongodb-elasticsearch提供的gradle脚本，即可构建和发布出可部署到生产环境的同步作业包。因此，对广大的java程序员来说，mongodb-elasticsearch无疑是一款能够轻易快速上手的数据同步利器。

  # 2.同步案例介绍-同步mongodb中的session数据到Elasticsearch

  场景比较简单，采用web应用session最后访问时间作为增量同步字段，将保存在mongodb中的session数据定时增量同步到Elasitcsearch中。

  我们在idea中开发和调试数据同步作业，利用gradle构建和发布同步作业包，运行作业，然后启动一个往mongodb中写入session数据的web应用，打开多个浏览器访问web应用，产生和修改session数据，然后观察同步作业的同步效果，演示两种调度机制效果：
  - 基于jdk timer
  
  - 基于xxl-job来调度作业

    

    下面正式切入本文主题。

  # 3.环境准备

  **开发环境**
  
  在windows环境开发和调试同步作业程序，需要在电脑上安装以下软件
  
  - jdk 1.8或以上
  - idea 2019
  - gradle最新版本  https://gradle.org/releases/ 
  - mongodb最新版本 
  - elasticsearch最新版本
  - 一个基于mongodb存储session数据的web应用(如有需要，可线下找我提供)
  - mongodb-elasticsearch工具工程（基于gradle）
  - xxl-job分布式定时任务引擎

自行安装好上述软件，这里着重说明一下gradle配置，需要配置三个个环境变量：

GRADLE_HOME: 指定gradle安装目录

GRADLE_USER_HOME: 指定gradle从maven中央库下载依赖包本地存放目录

 M2_HOME: maven安装目录（可选，如果有需要或者使用gradle过程中有问题就加上）

![](E:\workspace\bbossgroups\bboss-elastic\docs\images\env.png)

![](E:\workspace\bbossgroups\bboss-elastic\docs\images\env1.png)

详细gradle安装和配置参考文档： https://esdoc.bbossgroups.com/#/bboss-build 

​		**运行环境**

​      jdk1.8即可

# 4.Mongodb-Elasticsearch同步作业开发环境搭建

我们无需从0开始搭建开发环境，可以到以下地址下载已经配置好的Mongodb-Elasticsearch开发环境：

 https://github.com/bbossgroups/mongodb-elasticsearch 

idea gradle配置

将工程导入idea、调整gradle配置、熟悉idea中使用gradle



# 5.Mongodb-Elasticsearch同步作业实现、调试和发布

新建一个基于jdk timer的数据同步作业类：定义main方法和同步方法

mongodb表结构和elasticsearch表结构

同步前创建elasticsearch index mapping(可选)

mongdodb主要参数配置

elasticsearch主要参数配置（索引名称和索引类型、按日期动态索引名称）

mongodb数据检索条件

指定mongodb返回的字段

定时任务时间配置

关键参数配置：es地址、mongodb地址、jvm内存、线程数、队列数、fetchsize、batchsize）

设置同步作业结果回调处理函数

数据映射、加工和处理（添加字段、修改字段值、值类型转换、过滤记录、ip地理位置信息转换）

默认自动进行映射导入elasticsearch、通过datarefactor修改默认关系

调试jdk-timer调度作业、观察作业执行情况和日志

新建一个基于xxl-job的数据同步作业类：定义main方法和同步方法（参数配置和数据转换处理与jdk timer一样，只是任务调度采用外部分布式调度引擎xxl-job）

调试xxl-job调度作业（分片同步数据机制）、观察作业执行情况和日志

配置和发布作业/提取参数到配置文件中

# 6.Mongodb-Elasticsearch同步作业发布和部署



# 7.总结