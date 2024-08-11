# MongoDB数据源定义和使用

本文介绍如何自定义和关闭和使用MongoDB数据源

## 1.关键属性

name   MongoDB数据源名称，通过name对应的数据源名称，来针对MongoDB集群执行各种MongoDB操作，通过name来关闭和释放MongoDB数据源资源

认证配置相关userName,password,authdb,mechanism具体可以参考MongoDB官方文档介绍

读写相关WriteConcern和ReadPreference，具体可以参考MongoDB官方介绍

链接参数maxWaitTime，socketTimeout，connectTimeout，socketKeepAlive

连接池相关参数 connectionsPerHost  连接池连接数大小

## 2.数据源配置和启动

首先需要再项目中导入bboss的MongoDB开发组件

```xml
<dependency>
    <groupId>com.bbossgroups</groupId>
    <artifactId>bboss-data</artifactId>
    <version>6.2.3</version>
</dependency>
```

然后通过MongoDBConfig类配置MongoDB数据源参数：对应的数据源名称name为testes2mg

```java
MongoDBConfig mongoDBConfig = new MongoDBConfig();
		mongoDBConfig.setName("testes2mg")
				.setUserName("bboss")
				.setPassword("bboss")
				.setAuthDb("sessions")
				.setMechanism("PLAIN")
//		.setServerAddresses(mongoDBInputConfig.getServerAddresses());
		.setWriteConcern("JOURNAL_SAFE")//private String writeConcern;
		/**
		 * if (readPreference.equals("PRIMARY"))
		 * 			return ReadPreference.primary();
		 * 		else if (readPreference.equals("SECONDARY"))
		 * 			return ReadPreference.secondary();
		 * 		else if (readPreference.equals("SECONDARY_PREFERRED"))
		 * 			return ReadPreference.secondaryPreferred();
		 * 		else if (readPreference.equals("PRIMARY_PREFERRED"))
		 * 			return ReadPreference.primaryPreferred();
		 * 		else if (readPreference.equals("NEAREST"))
		 * 			return ReadPreference.nearest();
		 */
		.setReadPreference("SECONDARY")//private String readPreference;

		.setConnectionsPerHost(100)//private int connectionsPerHost = 50;

		.setMaxWaitTime(120000)//private int maxWaitTime = 120000;
		.setSocketTimeout(120000)//private int socketTimeout = 0;
		.setConnectTimeout(120000)//private int connectTimeout = 15000;


		.setSocketKeepAlive(true)//private Boolean socketKeepAlive = false;

		.setConnectString("mongodb://192.168.137.1:27017,192.168.137.1:27018,192.168.137.1:27019/?replicaSet=rs0");
		boolean started = MongoDBHelper.init(mongoDBConfig);// started true标识数据源成功启动，false 标识数据源没有启动，可能已经启动过了，可能启动失败
```

通过以下方法来初始化和启动MongoDB数据源testes2mg：

```java
boolean started = MongoDBHelper.init(mongoDBConfig)；
```

返回布尔值started说明：

 true标识数据源成功启动

false 标识数据源没有启动，可能已经启动过了，可能启动失败

## 3.使用数据源

可以在MongoDB输出插件中指定将记录输出到数据源testes2mg：

[2.8.2 多表输出配置案例](https://esdoc.bbossgroups.com/#/datatran-plugins?id=_282-多表输出配置案例)

```java
TableMapping tableMapping = new TableMapping();
tableMapping.setTargetDatabase("testdb");//目标库
tableMapping.setTargetCollection("testcdc");//目标表
tableMapping.setTargetDatasource("testes2mg");//指定MongoDB数据源名称，对应一个MongoDB集群

context.setTableMapping(tableMapping);
```

## 4.关闭数据源

如果数据源启动成功，在作业或者应用退出时，可以通过以下方法关闭和释放MongoDB数据源testes2mg资源

```java
MongoDBHelper.closeDB("testes2mg");
```

## 5.数据源及工具包使用

MongoDB数据源及工具包使用案例：

https://gitee.com/bboss/bestpractice/blob/master/testmongo/src/com/timesontransfar/mfsp/util/MongoUtil.java