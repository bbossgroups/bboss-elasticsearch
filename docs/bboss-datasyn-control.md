# 作业调度控制

通过bboss创建的同步作业，可以非常方便地进行作业启动、暂停、继续、停止控制，本文详细介绍。

## 15.1 启动作业

定时同步作业创建后，可以调用execute方法启动作业，会默认按照指定的定时器，周期性调度执行，例如

```java
/**
 * 创建一个数据同步作业对象
 */
DataStream dataStream = importBuilder.builder();
dataStream.execute();//启动作业
```

## 15.2 停止作业

作业启动后，如果需要停止作业，可以调用DataStream的destroy方法停止作业

```java
//waitTranStopped true 等待同步作业处理完成后停止作业 false 不等待
dataStream.destroy(true);
```

## 15.3 暂停调度/继续调度作业

bboss提供了暂停调度/继续调度作业的能力，前提是我们要创建具备暂停调度/继续调度能力的作业

通过以下方式创建的作业具备人工暂停调度/人工继续调度能力：

```java
//作业启动后持续运行，需要人工手动暂停才能暂停作业，当暂停后需执行resume作业才能继续调度执行
dataStream = importBuilder.builder(new DefaultScheduleAssert());
```

通过以下方式创建的作业具备自动暂停调度/人工继续调度能力：

```java
/**
 * 创建具备自动暂停功能的数据同步作业，控制调度执行后将作业自动进入为暂停状态，等待下一个人工resumeShedule指令才继续允许作业调度执行，
 */
dataStream = importBuilder.builder(true);
```

作业启动后可以调用dataStream.pauseSchedule()方法暂停调度作业，适用于人工暂停作业，自动暂停作业无效

```java
boolean ret = dataStream.pauseSchedule();//如果db2es作业采用的是调度后自动暂停机制，所以ret始终返回false
if(ret) {
   return "db2ESImport job schedule paused.";
}
else{
   return "db2ESImport job schedule is not scheduled, Ignore pauseScheduleJob command.";
}
```

暂停调度后的作业，可以调用dataStream.resumeSchedule()重新继续调度作业（适用于控制人工暂停和自动暂停后的作业继续调度）

```java
boolean ret = dataStream.resumeSchedule();
if(ret) {
   return "db2ESImport job schedule resume to continue.";
}
else{
   return "db2ESImport job schedule is not paused, Ignore resumeScheduleJob command.";
}
```

**暂停pauseSchedule操作对已经暂停状态或者停止状态的作业不起作用，如果作业正处于执行数据采集处理状态，暂停操作只会对本次采集处理完毕后的后续调度起作用**

**resumeSchedule继续调度操作对停止作业不起作用，不会影响正处于执行数据采集处理过程中的作业，只会对本次采集处理完毕后的后续暂停状态的作业起作用**

**pauseSchedule暂停/resumeSchedule继续操作对以下方式创建的作业不起作业**

```java
DataStream dataStream = importBuilder.builder();
```

文件采集插件需要注意以下情况：

**1.文件采集插件如果存在closeEOF为false的情况，那么将关闭自动暂停功能，人工暂停功能可以继续使用，也就是说自动暂停只对文件一次性采集完毕既关闭文件采集通道的作业（closeEOF为true）起作用**

**2.人工暂停时，将暂停扫描目录下的新文件，同时也会暂停已经在采集中的文件的后续采集动作；在继续调度后，会重新扫描目录下的新文件，暂停采集的文件也会继续开始采集，并且暂停之后没有被采集的数据都会被采集**



## 15.4 作业启停、暂停、继续调度案例

我们通过一个spring boot web服务来提供一个在线启动、暂停、继续、停止作业的案例，包含三个数据同步作业

- DB-Elasticsearch数据同步
- Hbase-Elasticsearch数据同步（基于hbase 2.2.3开发，如果需要对接其他版本，需要调整pom.xml中的hbase-shaded-client maven坐标版本号）
```xml
	    <dependency>
			<groupId>org.apache.hbase</groupId>
			<artifactId>hbase-shaded-client</artifactId>
			<version>2.2.3</version>
		</dependency>
```
- File-Elasticsearch数据采集

下载案例maven工程
https://git.oschina.net/bboss/springboot-elasticsearch

### 15.4.1 案例对应源码

作业服务

https://gitee.com/bboss/springboot-elasticsearch/blob/master/src/main/java/com/example/esbboss/service/AutoschedulePauseDataTran.java

作业web控制器

https://gitee.com/bboss/springboot-elasticsearch/blob/master/src/main/java/com/example/esbboss/controller/ScheduleControlDataTranController.java

### 15.4.2 案例运行演示

#### 启动spring boot web服务

First run elasticsearch 5 or elasticsearch 6 or elasticsearch 7，then run hbase and database for hbase-elasticsearch and database-elasticsearch. 

start run spring boot web demo at port 808:

```java
mvn clean install
cd target

java -jar es_bboss_web-0.0.1-SNAPSHOT.jar
```

#### 1) 运行基本db-elasticsearch作业
##### 1.1) run the db-elasticsearch data tran job
Enter the following address in the browser to run the db-elasticsearch data tran job:

http://localhost:808/scheduleDB2ESJob

Return the following results in the browser to show successful execution:

作业启动成功
```json
db2ESImport job started.
```

作业已经启动
```json
db2ESImport job has started.
```
##### 1.2) stop the db-elasticsearch data tran job
Enter the following address in the browser to stop the db-elasticsearch data tran job:

http://localhost:808/stopDB2ESJob

Return the following search results in the browser to show successful execution:
作业停止成功
```json
db2ESImport job started.
```
作业已经停止
```json
db2ESImport job has been stopped.
```
#### 2) 运行基本hbase-elasticsearch作业
##### 2.1) run the hbase-elasticsearch data tran job
Enter the following address in the browser to run the hbase-elasticsearch data tran job:

http://localhost:808/scheduleHBase2ESJob

Return the following results in the browser to show successful execution:

作业启动成功
```json
HBase2ES job started.
```

作业已经启动
```json
HBase2ES job has started.
```
##### 2.2) stop the db-elasticsearch data tran job
Enter the following address in the browser to stop the hbase-elasticsearch data tran job:

http://localhost:808/stopHBase2ESJob

Return the following search results in the browser to show successful execution:
作业停止成功
```json
HBase2ES job started.
```
作业已经停止
```json
HBase2ES job has been stopped.
```

#### 3) 运行控制作业调度的db-elasticsearch作业
##### 3.1) run the db-elasticsearch data tran job
Enter the following address in the browser to run the db-elasticsearch data tran job:
创建需要人工手动暂停才能暂停作业，作业启动后持续运行，当暂停后需执行resume作业才能继续调度执行
http://localhost:808/schedulecontrol/scheduleDB2ESJob?autoPause=false

创建具备暂停功能的数据同步作业，调度执行后将作业自动标记为暂停状态，等待下一个resumeShedule指令才继续允许作业调度执行，执行后再次自动暂停
http://localhost:808/schedulecontrol/scheduleDB2ESJob?autoPause=true

Return the following results in the browser to show successful execution:

作业启动成功
```json
db2ESImport job started.
```

作业已经启动
```json
db2ESImport job has started.
```
##### 3.2) stop the db-elasticsearch data tran job
Enter the following address in the browser to stop the db-elasticsearch data tran job:

http://localhost:808/schedulecontrol/stopDB2ESJob

Return the following search results in the browser to show successful execution:
作业停止成功
```json
db2ESImport job stopped.
```
作业已经停止
```json
db2ESImport job has been stopped.
```
##### 3.3) Pause schedule the db-elasticsearch data tran job
Enter the following address in the browser to Pause the db-elasticsearch data tran job:

http://localhost:808/schedulecontrol/pauseScheduleDB2ESJob

Return the following search results in the browser to show successful execution:
作业暂停成功
```json
db2ESImport job schedule paused.
```
作业已经暂停
```json
b2ESImport job schedule is not scheduled, Ignore pauseScheduleJob command.
```
作业已经停止
```json
db2ESImport job has been stopped.
```

##### 3.4) Resume schedule the db-elasticsearch data tran job
Enter the following address in the browser to Resume the db-elasticsearch data tran job:

http://localhost:808/schedulecontrol/resumeScheduleDB2ESJob

Return the following search results in the browser to show successful execution:
作业继续调度成功
```json
db2ESImport job schedule resume to continue.
```
作业已经在调度执行提示
```json
db2ESImport job schedule is not paused, Ignore resumeScheduleJob command.
```
作业已经停止
```json
db2ESImport job has been stopped.
```

#### 4) 运行控制作业调度的基本hbase-elasticsearch作业
##### 4.1) run the hbase-elasticsearch data tran job
Enter the following address in the browser to run the hbase-elasticsearch data tran job:

http://localhost:808/schedulecontrol/scheduleHBase2ESJob

Return the following results in the browser to show successful execution:

作业启动成功
```json
HBase2ES job started.
```

作业已经启动
```json
HBase2ES job has started.
```
##### 4.2) stop the hbase-elasticsearch data tran job
Enter the following address in the browser to stop the hbase-elasticsearch data tran job:

http://localhost:808/schedulecontrol/stopHBase2ESJob

Return the following search results in the browser to show successful execution:
作业停止成功
```json
HBase2ES job started.
```
作业已经停止
```json
HBase2ES job has been stopped.
```
##### 4.3) Pause schedule the hbase-elasticsearch data tran job
Enter the following address in the browser to Pause the hbase-elasticsearch data tran job:

http://localhost:808/schedulecontrol/pauseScheduleHBase2ESJob

Return the following search results in the browser to show successful execution:
作业暂停成功
```json
HBase2ES job schedule paused.
```
作业已经暂停
```json
HBase2ES job schedule is not scheduled, Ignore pauseScheduleJob command.
```
作业已经停止
```json
HBase2ES job has been stopped.
```

##### 4.4) Resume schedule the hbase-elasticsearch data tran job
Enter the following address in the browser to Resume the hbase-elasticsearch data tran job:

http://localhost:808/schedulecontrol/resumeScheduleHBase2ESJob

Return the following search results in the browser to show successful execution:
作业继续调度成功
```json
HBase2ES job schedule resume to continue.
```
作业已经在调度执行
```json
HBase2ES job schedule is not paused, Ignore resumeScheduleJob command.
```
作业已经停止
```json
HBase2ES job has been stopped.
```

#### 5) 运行控制作业调度的基本file-elasticsearch作业
##### 5.1) run the file-elasticsearch data tran job
Enter the following address in the browser to run the file-elasticsearch data tran job:

http://localhost:808/schedulecontrol/startfile2es

或者创建带有自动暂停的作业

Return the following results in the browser to show successful execution:

作业启动成功
```json
file2ES job started.
```

作业已经启动
```json
file2ES job has started.
```
##### 5.2) stop the file-elasticsearch data tran job
Enter the following address in the browser to stop the file-elasticsearch data tran job:

http://localhost:808/schedulecontrol/stopfile2es

Return the following search results in the browser to show successful execution:
作业停止成功
```json
file2ES job started.
```
作业已经停止
```json
file2ES job has been stopped.
```
##### 5.3) Pause schedule the file-elasticsearch data tran job
Enter the following address in the browser to Pause the file-elasticsearch data tran job:

http://localhost:808/schedulecontrol/pauseFile2es

Return the following search results in the browser to show successful execution:
作业暂停成功
```json
file2ES job schedule paused.
```
作业已经暂停
```json
file2ES job schedule is not scheduled, Ignore pauseScheduleJob command.
```
作业已经停止
```json
file2ES job has been stopped.
```

##### 5.4) Resume schedule the file-elasticsearch data tran job
Enter the following address in the browser to Resume the file-elasticsearch data tran job:

http://localhost:808/schedulecontrol/resumeFile2es

Return the following search results in the browser to show successful execution:
作业继续调度成功
```json
file2ES job schedule resume to continue.
```
作业已经在调度执行
```json
file2ES job schedule is not paused, Ignore resumeScheduleJob command.
```
作业已经停止
```json
file2ES job has been stopped.
```



