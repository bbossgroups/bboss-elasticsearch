# Elasticsearch历史数据清理

# 1.环境要求

JDK requirement: JDK 1.7+

Elasticsearch version requirements: 1.X,2.X,5.X,6.X,+

Spring booter 1.x,2.x,+

# 2.功能介绍

基于quartz 2.3.0，定期清理es索引数据，支持多个elasticsearch集群数据清理
索引要求：只支持按日期时间分索引的索引数据清理，

数据保留时间：通过data.livetime指定需要保留多少天的数据

数据清理方式：采用直接删除已经过期的索引的方式清理数据，通过crontab来定期触发数据清理操作

# 3.作业配置
修改resources/application.properties文件,elasticsearch服务器地址，支持多个集群配置，参考application.properties配置文件内容，主要的配置内容如下

## 3.1 rest协议配置
集群需要配置多个，逗号分隔，单机只需配置一个即可

elasticsearch.rest.hostNames=10.1.236.85:9200,10.1.236.88:9200,10.1.236.86:9200

## 3.2 elasticsearch访问账号
elasticsearch访问账号和口令，没有则配置为空，开启x-pack认证机制或者searchguard情况下有用

elasticUser=elastic

elasticPassword=changeme

## 3.3 定时任务配置

数据有效期,以天为单位

data.livetime=30

con time，定时扫描时间点

crontime=0/10 * * * * ?

索引表对应的日期格式

elasticsearch.dateFormat=yyyy.MM.dd

## 3.4 quartz任务配置

```xml
<!-- 
   任务调度
-->
<properties>
   <config file="application.properties"/>
   <property name="quartz.config">
      <map>
         <property name="org.quartz.scheduler.instanceName" value="DefaultQuartzScheduler111" />
         <property name="org.quartz.scheduler.rmi.export" value="false" />
         <property name="org.quartz.scheduler.rmi.proxy" value="false" />
         <property name="org.quartz.scheduler.wrapJobExecutionInUserTransaction" value="false" />
         <property name="org.quartz.threadPool.class" value="org.quartz.simpl.SimpleThreadPool" />
         <property name="org.quartz.threadPool.threadCount" value="10" />
         <property name="org.quartz.threadPool.threadPriority" value="5" />
         <property name="org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread" value="true" />
         <property name="org.quartz.jobStore.misfireThreshold" value="6000" />
         <property name="org.quartz.jobStore.class" value="org.quartz.simpl.RAMJobStore" />
      </map>
      <!-- for cluster -->
      
   </property>

   <property name="taskconfig" enable="true">
      <list>
         <property name="定时任务执行器" taskid="default"
            class="org.frameworkset.task.DefaultScheduleService" used="true">
            <!--
               可执行的任务项
               属性说明：
               name：任务项名称
               id:任务项标识
               action:具体的任务执行处理程序,实现org.frameworkset.task.Execute接口
               cron_time： cron格式的时间表达式，用来管理任务执行的生命周期，相关的规则请参照日期管理控件quartz的说明文档
               基本格式 : [参数间必须使用空格隔开]
               *　　*　　*　　*　　*　　command
               分　时　日　月　周　命令

               第1列表示分钟1～59 每分钟用*或者 */1表示
               第2列表示小时1～23（0表示0点）
               第3列表示日期1～31
               第4列表示月份1～12
               第5列标识号星期0～6（0表示星期天）
               第6列要运行的命令
               shouldRecover:集群环境下属性必须设置为 true，当Quartz服务被中止后，再次启动或集群中其他机器接手任务时会尝试恢复执行之前未完成的所有任务。
               used 是否使用
               true 加载，缺省值
               false 不加载    
               子元素说明：
               parameter:设置任务执行的参数，name标识参数名称，value指定参数的值
            -->
            <list>
               <property name="workbroker199" jobid="workbroker199"
                       bean-name="EsscanTask199"
                       method="scanIndex"
                  cronb_time="${crontime}" used="true"
                  shouldRecover="false"
                  />

            </list>
         </property>
      </list>
   </property>

   <property name="EsscanTask199" class="org.frameworkset.elasticsearch.job.EsscanTask"
           f:dateformat="${elasticsearch.dateFormat}"
           f:elasticDataLivetime="${data.livetime}"          
           init-method="init"/>


</properties>
```

# 4. 构建运行
## 4.1 前提

1. 安装和配置好最新的gradle版本

   gradle安装和配置参考文档：https://esdoc.bbossgroups.com/#/bboss-build

2. 下载源码

   github: https://github.com/bbossgroups/elasticsearch-task

   码云: https://gitee.com/bboss/elktask

## 4.2 利用gradle构建发布版本
利用gradle构建发布版本，在下载的源码根目录的cmd下运行下面的指令:

gradle releaseVersion

## 4.3 运行作业
gradle构建成功后，在build/distributions目录下会生成可以运行的zip包，解压后启动和运行quartz作业即可：


linux：

chmod +x startup.sh

./startup.sh

停止：
./stop.sh

重启

./restart.sh

windows: 

startup.bat 

stop.bat

restart.bat

# 5. 快速集成和应用 bboss elasticsearch
https://esdoc.bbossgroups.com/#/development

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>

<img src="images/alipay.png"  height="200" width="200">

