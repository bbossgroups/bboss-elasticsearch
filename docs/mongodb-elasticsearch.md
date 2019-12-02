*如何快速将保存在 MongoDB 中的海量数据同步到 Elasticshearch 中是一件非常具有挑战意义的事情，本话题分享基于分布式任务调度引擎、多线程高并发技术快速将保存在 MongoDB 中的海量数据同步到 Elasticshearch 中实战技术和经验。* 

# 1.数据同步概述

先介绍一下本次实践中需要使用的数据同步工具-基于java语言的mongodb-elasticsearch数据同步工具![bboss数据同步工具](https://esdoc.bbossgroups.com/images/datasyn.png)
与logstash类似，通过mongodb-elasticsearch，可以非常方便地实现：

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

mongodb-elasticsearch另一个显著的特色就是直接基于java语言来编写数据同步作业程序，基于强大的java语言和第三方工具包(本文就涉及到使用第三方库将保存在session中的xml报文序列化为java对象案例)，能够非常方便地加工和处理需要同步的源数据，然后将最终的数据保存到目标库（Elasticsearch或者数据库）；同时也可以非常方便地在idea或者eclipse中调试和运行同步作业程序，调试无误后，通过mongodb-elasticsearch提供的gradle脚本，即可构建和发布出可部署到生产环境的同步作业包。因此，对广大的java程序员来说，mongodb-elasticsearch无疑是一个轻易快速上手的数据同步利器。

​	下面我们通过一个案例来介绍mongodb-elasticsearch的使用方法，你会发现整个过程下来，开发一个同步作业，其实就是在用大家熟悉的方式做一个简单的开发编程的事情。

# 2.同步案例介绍-session数据同步

本文以一个session数据同步案例来介绍mongodb到Elasticsearch数据同步功能。场景比较简单：

​	用web应用session最后访问时间作为增量同步字段，将保存在mongodb中的session数据定时增量同步到Elasitcsearch中,在处理session中数据时，使用第三方库将保存在session中的xml报文序列化为java对象。我们在idea中开发和调试数据同步作业，利用gradle构建和发布同步作业包，运行作业。

​	事先运行一个往mongodb中写入session数据的web应用，然后启动增量同步作业，打开多个浏览器访问web应用，不断产生和更新session数据，观察增量同步作业的同步效果，演示两种调度机制同步效果：

- 基于jdk timer

- 基于xxl-job来调度作业演示数据分片同步功能（内容过多，下一个主题进行介绍）

下面结合session数据同步案例，正式切入本文主题。

# 3.环境准备

**开发环境**

在windows环境开发和调试同步作业程序，需要在电脑上安装以下软件

- jdk 1.8或以上
- idea 2019
- gradle最新版本  [https://gradle.org/releases/](https://gradle.org/releases/) 
- mongodb 4.2.1 
- elasticsearch版本6.5.0，亦可以采用最新的版本
- 一个基于mongodb存储session数据的web应用(如有需要，可线下找我提供)
- mongodb-elasticsearch工具工程（基于gradle）
- xxl-job分布式定时任务引擎

自行安装好上述软件，这里着重说明一下gradle配置，需要配置三个个环境变量：

GRADLE_HOME: 指定gradle安装目录

GRADLE_USER_HOME: 指定gradle从maven中央库下载依赖包本地存放目录

 M2_HOME: maven安装目录（可选，如果有需要或者使用gradle过程中有问题就加上）

![](https://esdoc.bbossgroups.com/images/env.png)

![](https://esdoc.bbossgroups.com/images/env1.png)

详细gradle安装和配置参考文档： [https://esdoc.bbossgroups.com/#/bboss-build](https://esdoc.bbossgroups.com/#/bboss-build) 

​		**运行环境**

​      jdk1.8即可

# 4.同步作业开发环境搭建

我们无需从0开始搭建开发环境，可以到以下地址下载已经配置好的Mongodb-Elasticsearch开发环境：

 [https://github.com/bbossgroups/mongodb-elasticsearch](https://github.com/bbossgroups/mongodb-elasticsearch) 

![down](https://esdoc.bbossgroups.com/images/downmongodb2es.png)

下载后解压到目录：

![image-20191124223658972](https://esdoc.bbossgroups.com/images/mongodbdir.png)

参考下面的向导将工程导入idea、调整gradle配置、熟悉idea中使用gradle

第一步 导入工程

![newproject](https://esdoc.bbossgroups.com/images/mongodb/newproject.png)

![image-20191124233037071](https://esdoc.bbossgroups.com/images/mongodb/selectproject.png)

![image-20191124233257671](https://esdoc.bbossgroups.com/images/mongodb/selectgradle.png)

![image-20191124233257671](https://esdoc.bbossgroups.com/images/mongodb/newwindow.png)

![image-20191124233712833](https://esdoc.bbossgroups.com/images/mongodb/importcomplete.png)

进入setting，设置工程的gradle配置：

![](https://esdoc.bbossgroups.com/images/mongodb/settingprojectgradle.png)

设置完毕后，进入gradle面板

![](https://esdoc.bbossgroups.com/images/mongodb/importsuccess.png)

可以选择gradle相关的任务进行clean和install构建操作：

![image-20191124234308907](https://esdoc.bbossgroups.com/images/mongodb/install.png)

工程采用典型的类似maven项目的目录结构管理源码：

mongodb-elasticsearch-master

|--lib  同步作业需要依赖的第三方jar存放到lib目录（在maven中央库或者本地maven库中没有的jar文件）

|--runfiles 同步作业运行和停止的windows、linux、unix指令模板，根据指令模板生成最终的运行指令

|--src/main/java  存放作业源码类文件

|--src/main/resources  存放作业运行配置文件（es配置、参数配置）

|--build.gradle   构建和发布作业的gradle构建脚本

|--gradle.properties   gradle属性配置文件，这些属性在build.gradle文件中引用

|--release.bat   构建和发布版本的指令（针对windows环境）



在此工程中已经有3个同步案例类：

```java
org.frameworkset.elasticsearch.imp.Mongodb2DBdemo --基于jdk timer mongodb到数据库同步案例
org.frameworkset.elasticsearch.imp.Mongodb2ESdemo  --基于jdk timer mongodb到elasticsearch同步案例（本文详细讲解）
org.frameworkset.elasticsearch.imp.QuartzImportTask --mongodb到elasticsearch quartz定时任务同步案例

```

关键配置文件：

src/main/resources/application.properties

这个文件是同步作业的主配置文件，es和mongodb的相关参数都在这里配置。

数据同步作业工程导入idea后，即可进入同步作业开发、调试环节。

# 5.同步作业程序开发调试发布

## 5.1 案例说明

同步mongodb中的session数据到Elasticsearch场景比较简单，采用web应用session最后访问时间作为增量同步字段，将保存在mongodb中的session数据定时增量同步到Elasitcsearch中。

我们在idea中开发和调试数据同步作业，利用gradle构建和发布同步作业包，运行作业，然后启动一个往mongodb中写入session数据的web应用，打开多个浏览器访问web应用，产生和修改session数据，然后观察同步作业的同步效果，演示两种调度机制效果：

- 基于jdk timer

- 基于xxl-job来调度作业

  

jdk timer调度作业对应的mongodb session表结构和elasticsearch索引表结构映射关系如下：（首先以默认的索引结构，然后可以自定义索引结构或者索引模板）

mongodb对应的db：sessiondb

mongodb对应的dbcollection：sessionmonitor_sessions

elasticsearch 索引名称：mongodbdemo 索引类型：mongodbdemo

### 5.1.1 字段映射关系

对应的字段映射关系如下（后面做xxl-job分片数据同步时还会补充shardNo字段到Elasticsearch和mongodb属性中）：

| mongodb             | 字段类型 | elasticsearch        | 字段类型 | 说明                                                         |
| ------------------- | -------- | -------------------- | -------- | ------------------------------------------------------------ |
| _id                 | String   | _id                  | text     | 文档唯一id                                                   |
| userAccount         | String   | userAccount          | text     | session关联的用户账号                                        |
| testVO              | xml      | testVO               | json     | session的对象属性数据,在datafactor进行类型转换               |
| privateAttr         | xml      | privateAttr          | json     | session中的对象属性数据,在datafactor进行类型转换             |
| referip             | String   | referip              | text     | session对应的客户端ip                                        |
| requesturi          | String   | requesturi           | text     | 创建session对应的客户端请求url                               |
| secure              | boolean  | secure               | boolean  | session是否启用https安全机制                                 |
| sessionid           | String   | sessionid            | text     | session id                                                   |
| host                | String   | host                 | text     | 创建session的服务器ip                                        |
| httpOnly            | boolean  | httpOnly             | boolean  | session是否采用httpOly机制                                   |
| lastAccessedHostIP  | String   | lastAccessedHostIP   | text     | 最近接收统一session请求的服务器ip                            |
| lastAccessedTime    | long     | **lastAccessedTime** | Date     | session最近访问时间,在datafactor进行类型转换，会作为增量同步字段 |
| lastAccessedUrl     | String   | lastAccessedUrl      | text     | 最近使用session的url                                         |
| local               | String   | local                | text     | session中存放的local语言代码属性数据                         |
| maxInactiveInterval | long     | maxInactiveInterval  | long     | session有效期                                                |
| appKey              | String   | appKey               | text     | session关联的appKey                                          |
| creationTime        | long     | creationTime         | Date     | session创建时间,在datafactor进行类型转换                     |
|                     |          | extfiled             | int      | 在datafactor中添加的字段                                     |
|                     |          | extfiled2            | int      | 在datafactor中添加的字段                                     |
|                     |          | ipInfo               | json     | 在datafactor中添加的字段,根据referip计算出来的客户端ip地址信息（省市、地区、运营商、地理经纬度坐标等） |
| shardNo             | int      | shardNo              | int      | session数据分片号，用于xxl-job同步作业分片同步数据功能       |

## 5.2 建立同步作业类-Mongodb2DB

我们先新建一个基于jdk timer的数据同步作业类Mongodb2DB，定义main方法和同步方法scheduleImportData，后面的xxl-job的作业在此基础上进行改进即可。

org.frameworkset.elasticsearch.imp.Mongodb2DB

![image-20191125223652299](https://esdoc.bbossgroups.com/images/mongodb/mongodb2db.png)

接下来在scheduleImportData方法中定义同步处理逻辑。

## 5.2 同步作业方法代码实现

### 5.2.1 清理Elasticsearch索引表mongodbdemo(可选步骤)

在scheduleImportData方法的开始处添加删除Elasticsearch索引表mongodbdemo的代码

```java
//从application.properties配置文件中读取dropIndice属性，
		// 是否清除elasticsearch索引，true清除，false不清除，指定了默认值false
		boolean dropIndice = CommonLauncher.getBooleanAttribute("dropIndice",false);
		//增量定时任务不要删表，但是可以通过删表来做初始化操作
		if(dropIndice) {
			try {
				//清除测试表,导入的时候自动重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
				String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("mongodbdemo");
				System.out.println(repsonse);
			} catch (Exception e) {
			}
		}
```

代码作用：根据配置的boolean属性dropIndice，控制是否在启动作业时删除Elasticsearch中的索引表

![image-20191126221535070](https://esdoc.bbossgroups.com/images/mongodb/dropindice.png)

### 5.2.2 创建elasticsearch index mapping(可选)

如果elasticsearch索引不存在，那么可以手动创建索引mongodbdemo：

新建mapping定义文件-src\main\resources\dsl.xml，内容如下：

```xml
<?xml version="1.0" encoding='UTF-8'?>
<properties>
    <description>
        <![CDATA[
            配置dsl和mapping的xml文件
         ]]>
    </description>
    <!--创建索引mongoddbdemo mapping定义-->
    <property name="createMongodbdemoIndice">
        <![CDATA[{
            "settings": {
                "number_of_shards": 6,
                "index.refresh_interval": "5s"
            },
            "mappings": {
                "mongodbdemo": {
                    "properties": {
                        "_validate": {
                            "type": "boolean"
                        },
                        "appKey": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "creationTime": {
                            "type": "date"
                        },
                        "extfiled": {
                            "type": "long"
                        },
                        "extfiled2": {
                            "type": "long"
                        },
                        "host": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "httpOnly": {
                            "type": "boolean"
                        },
                        "lastAccessedHostIP": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "lastAccessedTime": {
                            "type": "date"
                        },
                        "lastAccessedUrl": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "local": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "maxInactiveInterval": {
                            "type": "long"
                        },
                        "privateAttr": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "referip": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "requesturi": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "secure": {
                            "type": "boolean"
                        },
                        "sessionid": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "testVO": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "userAccount": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        }
                    }
                }
            }
        }]]>
    </property>

</properties>

```

在scheduleImportData方法的开始添加判断索引表mongodbdemo是否存在并创建mongodbdemo的代码：

```java
//判断mongodbdemo是否存在，如果不存在则创建mongodbdemo
		boolean indiceExist = clientInterface.existIndice("mongodbdemo");
		if(!indiceExist){
			ClientInterface configClientInterface = ElasticSearchHelper.getConfigRestClientUtil("dsl.xml");
			configClientInterface.createIndiceMapping("mongodbdemo","createMongoddbdemoIndice");
		}
```

### 5.2.3 创建elasticsearch index template（可选）

如果我们采用按照时间动态滚动索引表，如果需要定制索引结构，则需要创建索引模板(IndexTemplate):

同样在src\main\resources\dsl.xml文件中定义一个indexTemplate的createMongoddbdemoTemplate：

```xml
<property name="createMongodbdemoTemplate">
        <![CDATA[{
            "index_patterns": "mongodbdemo-*", ## 5.x版本中请使用语法："template": "mongodbdemo-*"
            "settings": {
                "number_of_shards": 30,
                "number_of_replicas" : 1,
                "index.refresh_interval": "5s"
            },
            "mappings": {
                "mongodbdemo": {
                    "properties": {
                        "_validate": {
                            "type": "boolean"
                        },
                        "appKey": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "creationTime": {
                            "type": "date"
                        },
                        "extfiled": {
                            "type": "long"
                        },
                        "extfiled2": {
                            "type": "long"
                        },
                        "host": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "httpOnly": {
                            "type": "boolean"
                        },
                        "lastAccessedHostIP": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "lastAccessedTime": {
                            "type": "date"
                        },
                        "lastAccessedUrl": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "local": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "maxInactiveInterval": {
                            "type": "long"
                        },
                        "privateAttr": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "referip": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "requesturi": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "secure": {
                            "type": "boolean"
                        },
                        "sessionid": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "testVO": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        },
                        "userAccount": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        }
                    }
                }
            }
        }]]>
    </property>
```

mongodbdemo-开头的索引都会按照模板建立特定索引结构,例如mongodbdemo-2019.11.26，定义好createMongoddbdemoTemplate，就可以在scheduleImportData方法中添加判断名称为mongodbdemoTemplate是否存在并创建模板mongodbdemoTemplate的代码：

```java
	String template = clientInterface.getTempate("mongodbdemoTemplate");
		if(template == null){
			configClientInterface.createTempate("mongodbdemoTemplate","createMongoddbdemoTemplate");
		}
	}
```

5.2.1和5.2.2/5.2.3都是准备工作，其中5.2.2/5.2.3可以选择一个进行操作，接下来进入同步作业代码编写阶段。

### 5.2.4 编写同步代码

同步组件：

MongoDB2ESExportBuilder importBuilder = MongoDB2ESExportBuilder.newInstance();

#### 5.2.4.1 设置mongodb参数

我们会通过同步组件设置mongodb数据源的相关参数，首先介绍一下同步作业可以使用的mongdodb主要参数

| 参数名称                                     | 参数类型      | 参数说明                                                     |
| -------------------------------------------- | ------------- | ------------------------------------------------------------ |
| name                                         | String        | mongodb数据源名称，自定义命名即可                            |
| db                                           | String        | mongodb数据库名称                                            |
| dbCollection                                 | String        | mongodb数据库表名称                                          |
| connectTimeout                               | int           | 建立mongodb服务器连接超时时间，单位毫秒                      |
| writeConcern                                 | String        | REPLICA_ACKNOWLEDGED(n),其中的数字n代表需要几个集群节点确认写入后返回，如果n为0则不需等待节点确认；  JOURNALED：所有节点确认写入才返回 |
| readPreference                               | String        | 读数据模式：PRIMARY  SECONDARY SECONDARY_PREFERRED PRIMARY_PREFERRED NEAREST |
| maxWaitTime                                  | int           | 从连接池中获取mongodb连接的最大等待时间，单位：毫秒          |
| socketTimeout                                | int           | 从mongodb拉取数据socket超时时间，单位:毫秒                   |
| socketKeepAlive                              | boolean       | socketKeepAlive:true false                                   |
| connectionsPerHost                           | int           | 每个节点连接池保持少个连接数                                 |
| threadsAllowedToBlockForConnectionMultiplier | int           | threads Allowed To Block For Connection Multiplier           |
| serverAddresses                              | String        | 服务器地址列表，换行符分隔：127.0.0.1:27017\n127.0.0.1:27018 |
| clientMongoCredential                        | String...     | 认证参数配置：数组方式设置mongodb数据库的、账号、口令、认证机制，例如："sessiondb","bboss","bboss","MONGODB-CR" |
| option                                       | String        | 回车换行符\r\n分隔的通讯协议可选参数:QUERYOPTION_SLAVEOK\r\nQUERYOPTION_NOTIMEOUT,值可以参考com.mongodb.Bytes |
| autoConnectRetry                             | boolean       | 是否启用连接重试机制                                         |
| query                                        | BasicDBObject | 可选，设置mongodb业务检索条件，不设置则进行全量检索或者按照增量字段进行检索 |
| fetchFields                                  | BasicDBObject | 可选，设置mongodb检索返回字段列表，不设置则，返回所有字段    |

通过importBuilder设置mongodb参数：

```java
//mongodb的相关配置参数

		importBuilder.setName("session")
				.setDb("sessiondb")
				.setDbCollection("sessionmonitor_sessions")
				.setConnectTimeout(10000)
				.setWriteConcern("JOURNAL_SAFE")
				.setReadPreference("")
				.setMaxWaitTime(10000)
				.setSocketTimeout(1500).setSocketKeepAlive(true)
				.setConnectionsPerHost(100)
				.setThreadsAllowedToBlockForConnectionMultiplier(6)
				.setServerAddresses("127.0.0.1:27017")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
				// mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
				//String database,String userName,String password,String mechanism
				//https://www.iteye.com/blog/yin-bp-2064662
//				.buildClientMongoCredential("sessiondb","bboss","bboss","MONGODB-CR")
//				.setOption("")
				.setAutoConnectRetry(true);

        //定义mongodb数据查询条件对象
		BasicDBObject query = new BasicDBObject();
        // 设定检索mongdodb session数据时间范围条件
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date start_date = format.parse("1099-01-01");
			Date end_date = format.parse("2999-01-01");
			query.append("creationTime",
					new BasicDBObject("$gte", start_date.getTime()).append(
							"$lte", end_date.getTime()));
		}
		catch (Exception e){
			e.printStackTrace();
		}

		// 设置按照host字段值进行正则匹配查找session数据条件
		String host = "169.254.252.194-DESKTOP-U3V5C85";
		Pattern hosts = Pattern.compile("^" + host + ".*$",
				Pattern.CASE_INSENSITIVE);
		query.append("host", new BasicDBObject("$regex",hosts));
        importBuilder.setQuery(query);
        
        //设定需要返回的session数据字段信息
		BasicDBObject fetchFields = new BasicDBObject();
		fetchFields.put("appKey", 1);
		fetchFields.put("sessionid", 1);
		fetchFields.put("creationTime", 1);
		fetchFields.put("lastAccessedTime", 1);
		fetchFields.put("maxInactiveInterval", 1);
		fetchFields.put("referip", 1);
		fetchFields.put("_validate", 1);
		fetchFields.put("host", 1);
		fetchFields.put("requesturi", 1);
		fetchFields.put("lastAccessedUrl", 1);
		fetchFields.put("secure",1);
		fetchFields.put("httpOnly", 1);
		fetchFields.put("lastAccessedHostIP", 1);

		fetchFields.put("userAccount",1);
		fetchFields.put("testVO", 1);
		fetchFields.put("privateAttr", 1);
		fetchFields.put("local", 1);
		importBuilder.setFetchFields(fetchFields);

```

#### 5.2.4.2 导入elasticsearch参数配置

导入elasticsearch参数配置（索引名称和索引类型、按日期动态索引名称），首先介绍相关参数

| 参数名称                      | 参数类型 | 参数说明                                                     |
| ----------------------------- | -------- | ------------------------------------------------------------ |
| index                         | String   | 索引名称，支持固定的索引名称和动态索引名称，动态索引名称命名规范如下：demowithesindex-{dateformat=yyyy.MM.dd}  按照日期滚动索引名称，日期格式根据自己的需要指定即可                                                                                                     indexname-{field=fieldName} 按照字段值来动态设置索引名称                            dbclobdemo-{agentStarttime,yyyy.MM.dd} 按照字段值来动态设置索引名称,如果字段对应的值是个日期类型，可以指定日期类型的格式，本案例为：mongodbdemo |
| indexType                     | String   | 索引类型，es 7以后的版本不需要设置indexType（或者直接设置为_doc），es7以前的版本必需设置indexType，可以动态指定indexType,例如：索引类型为typeFieldName字段对应的值，{field=typeFieldName}或者{typeFieldName}，本案例直接指定为：mongodbdemo |
| batchSize                     | int      | 批量导入elasticsearch的记录大小                              |
| esIdField                     | String   | 指定作为elasticsearch文档_id标识的mongodb表字段，如果不指定，并且没有指定EsIdGenerator插件，则由Elasticsearch默认为索引文档提供id字段 |
| refreshOption                 | String   | 是否强制刷新索引数据，导入后立马生效，值可以设置为：refresh或者refresh=true，或者null（表示不实时刷新（默认值）），看测试效果可以设置强制刷新，正式运行不要配置 |
| fetchSize                     | int      | 按批次从mongodb拉取数据的大小                                |
| elasticUser                   | String   | 可选，认证账号（x-pack或者searchguard),在application.properties文件中配置 |
| elasticPassword               | String   | 可选，认证口令（x-pack或者searchguard）,在application.properties文件中配置 |
| elasticsearch.rest.hostNames  | String   | 指定elasticsearch服务器http地址和http端口，多个用逗号分隔，如果启用了https协议，那么必须带https://协议头，例如： http:     10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282  https:                                                                                                  https://10.180.211.27:9280,https://10.180.211.27:9281,https://10.180.211.27:9282                                                                  在application.properties文件中配置 |
| elasticsearch.showTemplate    | boolean  | 是否打印导入的数据语句，true 打印 false不打印（默认值）,调试代码的时候可以设置为true，在application.properties文件中配置 |
| elasticsearch.discoverHost    | boolean  | 是否启用elasticsearch集群节点自动发现机制，true 启用 false关闭，启用后可以自动发现es集群中新加入的节点和去掉的节点,在application.properties文件中配置 |
| http.timeoutConnection        | long     | http连接建立超时时间,在application.properties文件中配置      |
| http.timeoutSocket            | long     | http socket通讯超时时间,在application.properties文件中配置   |
| http.connectionRequestTimeout | long     | 获取http连接池连接等待超时时间,在application.properties文件中配置 |
| http.retryTime                | int      | 请求失败，重试次数设置,在application.properties文件中配置    |
| http.maxTotal                 | int      | http连接池最大连接数,在application.properties文件中配置      |
| http.defaultMaxPerRoute       | int      | 每个http节点对应的最大连接数,在application.properties文件中配置 |
| printTaskLog                  | boolean  | 是否打印任务执行统计信息日志，true 打印 false不打印          |
| continueOnError               | boolean  | true 忽略任务执行异常，任务执行过程抛出异常不中断任务执行 false 中断任务 |

在application.properties文件中配置的参数，无需在代码中指定和设置，其他参数通过同步组件importBuilder在代码中进行设置：


```java
/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("mongodbdemo") //必填项，索引名称
				.setIndexType("mongodbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
				.setBatchSize(10)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setFetchSize(100)  //按批从mongodb拉取数据的大小
                .setEsIdField("_id")//设置文档主键，不设置，则自动产生文档id,直接将mongodb的ObjectId设置为Elasticsearch的文档_id
				.setContinueOnError(true); // 忽略任务执行异常，任务执行过程抛出异常不中断任务执行
```

在application.properties文件中配置的参数示例（两部分：elasticsearch配置和http连接池配置）

```properties
# elasticsearch配置
##x-pack或者searchguard账号和口令
elasticUser=elastic
elasticPassword=changeme

#elasticsearch.rest.hostNames=10.1.236.88:9200
#elasticsearch.rest.hostNames=127.0.0.1:9200
#elasticsearch.rest.hostNames=10.21.20.168:9200
elasticsearch.rest.hostNames=192.168.137.1:9200
#elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
 
#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
elasticsearch.showTemplate=true
elasticsearch.discoverHost=false

##default连接池配置
http.timeoutConnection = 5000
http.timeoutSocket = 50000
http.connectionRequestTimeout=10000
http.retryTime = 1
http.maxLineLength = -1
http.maxHeaderCount = 200
http.maxTotal = 200
http.defaultMaxPerRoute = 100
http.soReuseAddress = false
http.soKeepAlive = false
http.timeToLive = 3600000
http.keepAlive = 3600000
http.keystore =
http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
http.hostnameVerifier =
```



####  5.2.4.3 jdk timer定时任务时间配置

默认提供的jdk timer定时机制配置参数如下：

| 参数名称     | 参数类型 | 参数说明                                   |
| ------------ | -------- | ------------------------------------------ |
| fixedRate    | boolean  | 参考jdk timer task文档对fixedRate的说明    |
| scheduleDate | Date     | 可选，任务开始执行日期时间                 |
| deyLay       | long     | 任务延迟执行deylay毫秒后执行               |
| period       | long     | 每隔period毫秒执行，如果不设置，只执行一次 |

通过同步组件importBuilder设置上述参数：

```java
		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
```

#### 5.2.4.4 并行任务配置

如果不指定并行任务执行参数，默认串行执行同步导入数据，可以通过以下两种方式提升导入速度：

- 并行（本节介绍）

- 并行和分布式分片机制相结合（基于分布式任务调度引擎实现，后续章节介绍）

本节介绍并行任务执行功能，相关参数如下：

| 参数名称    | 参数类型 | 参数说明                                                     |
| ----------- | -------- | ------------------------------------------------------------ |
| parallel    | boolean  | 是否启用并行执行任务机制，true 启用 false不启用(默认值)      |
| queue       | int      | 任务并行执行等待队列大小，如果工作线程全忙，允许排队等待的任务数，队列满了后，阻塞后续新任务加入，直到有空闲的位置出来，根据同步服务器资源进行合理配置 |
| threadCount | int      | 任务并行执行线程数，根据同步服务器资源和elasticsearch处理能力进行合理设置 |
| asyn        | boolean  | 任务并行执行后，是否同步等待每批次任务执行完成后再返回调度程序，true 不等待所有导入作业任务结束，方法快速返回；false（默认值） 等待所有导入作业任务结束，所有作业结束后方法才返回;保持默认值即可，定时任务场景下必须设置为false |

通过importBuilder组件设置并行任务执行参数：

```java
        importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//是否同步等待每批次任务执行完成后再返回调度程序，true 不等待所有导入作业任务结束，方法快速返回；false（默认值） 等待所有导入作业任务结束，所有作业结束后方法才返回

```

#### 5.2.4.5 数据加工处理

如果不设置数据加工处理的相关机制，那么同步作业默认自动将mongodb的中表字段映射为es字段（自动忽略_id字段，因为elasticsearch的\_id字段是自动维护的），然后将数据导入elasticsearch。我们通过importBuilder组件全局设置数据映射关系，也可以通过Datarefactor接口进行记录级的数据转换处理，这样就可以非常方便地对同步数据进行映射、加工和处理，下面列出几种常用的处理类型：

| 数据处理类型             | 全局处理 | 记录级别 | 举例(全局通过importBuilder组件实现，记录级别通过context接口实现) |
| ------------------------ | -------- | -------- | ------------------------------------------------------------ |
| 添加字段                 | 支持     | 支持     | 全局处理：importBuilder.addFieldValue("testF1","f1value");                                             记录级别：context.addFieldValue("testF1","f1value"); |
| 删除字段                 | 支持     | 支持     | 全局处理：importBuilder.addIgnoreFieldMapping("testInt");                                           记录级别：context.addIgnoreFieldMapping("testInt"); |
| 映射字段名称             | 支持     | 不支持   | 全局处理：importBuilder.addFieldMapping("document_id","docId"); |
| 映射字段名称并修改字段值 | 不支持   | 支持     | String oldValue = context.getStringValue("axx");                                                           String newvalue = oldValue+" new value";                context.newName2ndData("axx","newname",newvalue); |
| 修改字段值               | 不支持   | 支持     | //空值处理                                                                                                                            String local = context.getStringValue("local");if(local == null)   context.addFieldValue("local",""); |
| 值类型转换               | 不支持   | 支持     | //将long类型的creationTime字段转换为日期类型                                                             long creationTime = context.getLongValue("creationTime");          context.addFieldValue("creationTime",new Date(creationTime)); |
| 过滤记录                 | 不支持   | 支持     | String id = context.getStringValue("_id");//根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中                                           if(id.equals("5dcaa59e9832797f100c6806"))   context.setDrop(true); |
| ip地理位置信息转换       | 不支持   | 支持     | //根据session访问客户端ip，获取对应的客户地理位置经纬度信息、运营商信息、省地市信息IpInfo对象,并将IpInfo添加到Elasticsearch文档中                                                   String referip = context.getStringValue("referip");                                                                 if(referip != null){   IpInfo ipInfo = context.getIpInfoByIp(referip);                           if(ipInfo != null)      context.addFieldValue("ipInfo",ipInfo);} |
| 其他转换                 | 不支持   | 支持     | 在DataRefactor接口中对记录中的数据根据特定的要求进行相关转换和处理，然后使用上面列出的对应的处理方式将处理后的数据添加到记录中 |
| 获取原始记录对象         | 不支持   | 支持     | //除了通过context接口获取mongodb的记录字段，还可以直接获取当前的mongodb记录，可自行利用里面的值进行相关处理                                                                      DBObject record = (DBObject) context.getRecord(); |

本案例全局记录配置：打tag，标识数据来源于jdk timer
		importBuilder.addFieldValue("fromTag","jdk timer");  

其他的的数据处理转换都是记录级别的。

session数据转换处理的代码，通过importBuilder组件的setDataRefactor方法设置DataRefactor接口（可根据上表中的数据处理类型，自行实现自己的转换处理功能）：

```java
        // 全局记录配置：打tag，标识数据来源于jdk timer
		importBuilder.addFieldValue("fromTag","jdk timer");
        // 数据记录级别的转换处理
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				String id = context.getStringValue("_id");
				//根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中
				if(id.equals("5dcaa59e9832797f100c6806"))
					context.setDrop(true);
				//添加字段extfiled2到记录中，值为2
				context.addFieldValue("extfiled2",2);
				//添加字段extfiled到记录中，值为1
				context.addFieldValue("extfiled",1);
				boolean httpOnly = context.getBooleanValue("httpOnly");
				boolean secure = context.getBooleanValue("secure");
				//空值处理
				String userAccount = context.getStringValue("userAccount");
				if(userAccount == null)
					context.addFieldValue("userAccount","");
				//空值处理
				String testVO = context.getStringValue("testVO");
				if(testVO == null)
					context.addFieldValue("testVO","");
				//空值处理
				String privateAttr = context.getStringValue("privateAttr");
				if(privateAttr == null)
					context.addFieldValue("privateAttr","");
				//空值处理
				String local = context.getStringValue("local");
				if(local == null)
					context.addFieldValue("local","");
				//将long类型的lastAccessedTime字段转换为日期类型
				long lastAccessedTime = context.getLongValue("lastAccessedTime");
				context.addFieldValue("lastAccessedTime",new Date(lastAccessedTime));
				//将long类型的creationTime字段转换为日期类型
				long creationTime = context.getLongValue("creationTime");
				context.addFieldValue("creationTime",new Date(creationTime));
				//根据session访问客户端ip，获取对应的客户地理位置经纬度信息、运营商信息、省地市信息IpInfo对象
				//并将IpInfo添加到Elasticsearch文档中
				String referip = context.getStringValue("referip");
				if(referip != null){
					IpInfo ipInfo = context.getIpInfoByIp(referip);
					if(ipInfo != null)
						context.addFieldValue("ipInfo",ipInfo);
				}
				//除了通过context接口获取mongodb的记录字段，还可以直接获取当前的mongodb记录，可自行利用里面的值进行相关处理
				DBObject record = (DBObject) context.getRecord();

			}
		});
```

#### 5.2.4.6 IP地理位置信息库配置

ip地址转换为地址位置信息时，还需要在application.properties文件中配置好geolite2地址库的路径：

```properties
# IP地理位置信息库配置
# 缓存配置
ip.cachesize = 2000000
# 库下载地址https://dev.maxmind.com/geoip/geoip2/geolite2/
# geoip数据库文件地址配置
ip.database = E:/workspace/hnai/terminal/geolite2/GeoLite2-City.mmdb
ip.asnDatabase = E:/workspace/hnai/terminal/geolite2/GeoLite2-ASN.mmdb
```

数据库文件会定期更新，因此需要定期到以下地址下载最新的geolite2数据库文件：

[https://dev.maxmind.com/geoip/geoip2/geolite2/](https://dev.maxmind.com/geoip/geoip2/geolite2/)

#### 5.2.4.7 设置同步作业结果回调处理函数

同步作业任务执行结果回调处理即可ExportResultHandler提供了4个接口方法

- success方法：任务执行成功时调用，包含任务command对象（任务对应数据和任务信息，任务执行详细情况统计信息）和result（elasticsearch批量导入返回的response报文）两个参数
- error方法：任务执行成功但是es有部分记录没有处理成功时调用，包含任务command对象（任务对应数据和任务信息，任务执行详细情况统计信息）和result（elasticsearch批量导入返回的response报文,包含错误记录信息）两个参数
- exception方法：任务执行抛出异常时调用，包含任务command对象（任务对应数据和任务信息，任务执行详细情况统计信息）和exception两个参数
- getMaxRetry方法：返回当任务执行出错重试次数，一般返回0或者-1即可

通过importBuilder组件的setExportResultHandler方法设置同步作业任务执行结果回调处理函数，非常简单，只打印任务执行情况：

```java
        //设置任务处理结果回调接口
		importBuilder.setExportResultHandler(new ExportResultHandler<Object,String>() {
			@Override
			public void success(TaskCommand<Object,String> taskCommand, String result) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
			}

			@Override
			public void error(TaskCommand<Object,String> taskCommand, String result) {			 
                System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
				/**
				//分析result，提取错误数据修改后重新执行,
				Object datas = taskCommand.getDatas();
				Object errorDatas = ... //分析result,从datas中提取错误数据，并设置到command中，通过execute重新执行任务
				taskCommand.setDatas(errorDatas);
				taskCommand.execute();
				 */
			}

			@Override
			public void exception(TaskCommand<Object,String> taskCommand, Exception exception) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
			}

			@Override
			public int getMaxRetry() {
				return 0;
			}
		});
```

#### 5.2.4.8 自定义Elasticsearch索引文档id生成机制

同步作业支持三种方式设置索引文档id

- 1.Elasticsearch默认机制

- 2.指定esIdField字段（5.2.4.3 导入elasticsearch参数配置中有介绍）

- 3.自定义Elasticsearch索引文档id生成机制（本节介绍）

第2种和第3中只能设置一种，2，3都不设置就是第1种情况，下面介绍自定义Elasticsearch索引文档id生成机制，非常简单实现并设置EsIdGenerator接口即可：

```java
        //自定义Elasticsearch索引文档id生成机制
		importBuilder.setEsIdGenerator(new EsIdGenerator() {
			//如果指定EsIdGenerator，则根据下面的方法生成文档id，
			// 否则根据setEsIdField方法设置的字段值作为文档id，
			// 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id

			@Override
			public Object genId(Context context) throws Exception {
				return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
			}
		});
```




#### 5.2.4.9 设置增量字段信息

在增量同步时必须指定增量同步字段，如果不指定那么将是全量同步；增量字段必须是可以递增的值，目前支持两种数据类型作为增量字段：数字和日期

| 增量类型 | 设置案例                                                     |
| -------- | ------------------------------------------------------------ |
| 数字     | importBuilder.setNumberLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE); // 指定类型importBuilder.setLastValue(-1); |
| 日期     | importBuilder.setDateLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE); // 指定类型SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");                                                  try {                                                                                                                                                                                         Date date = format.parse("2000-01-01");   importBuilder.setLastValue(date);                                }catch (Exception e){   e.printStackTrace();} |

其他增量同步参数

| 参数名称                | 参数类型 | 参数说明                                                     |
| ----------------------- | -------- | ------------------------------------------------------------ |
| fromFirst               | boolean  | 作业进程重启时是否需要从头开始同步数据，true 从头开始，false从上次成功结束的值开始同步（默认值） |
| lastValueStorePath      | String   | 指定sqlite数据库文件地址，默认采用sqlite数据库来保存增量同步状态 |
| lastValueStoreTableName | String   | 记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab |
| config.db.name          | String   | 参数值在application.properties文件中配置                                   保存增量数据同步状态的表对应的数据源名称，当采用分布式任务调度引擎时需要设定和配置本参数，jdk timer和quartz调度机制不需要配置；可以指定一个已经有的数据源名称，比如数据库同步时的源数据源或者目标数据源名称，亦可以在application.properties文件中定义一个新的数据源，然后将新的数据源名称设置为config.db.name即可，例如： |

保存增量同步状态的外部数据源配置

在application.properties文件中配置完整的保存增量同步状态的config.db数据源：

```properties
config.db.name = testconfig
config.db.user = root
config.db.password = 123456
config.db.driver = com.mysql.jdbc.Driver
config.db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
config.db.usePool = true
config.db.validateSQL = select 1
config.db.showsql = true
```

增量数据同步对应的增量字段lastAccessedTime为long类型，是一个数字值，通过importBuilder同步组件设置相关增量参数的代码如下：

```java
        importBuilder.setNumberLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
//		importBuilder.setDateLastValueColumn("log_id");//手动指定日期增量查询字段
		importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
//		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型,ImportIncreamentConfig.TIMESTAMP_TYPE为时间类型
		//设置增量查询的起始值lastvalue
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date);
		}
		catch (Exception e){
			e.printStackTrace();
		}
```



#### 5.2.4.10 添加执行作业代码

到此同步作业的配置基本完成，接下来通过importBuilder组件构建一个DataStream对象，调用其execute方法启动同步作业任务：

```java
		/**
		 * 构建DataStream，执行mongodb数据到es的同步操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行同步操作
```

添加上述两行代码后，整个同步作业方法就编写完成了。

#### 5.2.4.12 同步作业主程序配置

需要将编写的同步作业类配置到application.properties文件中：

```properties
#同步作业主程序配置
mainclass=org.frameworkset.elasticsearch.imp.Mongodb2DB
```



#### 5.2.4.11 完整的同步作业类和作业配置文件

来看看完整的数据同步作业类：

```java
package org.frameworkset.elasticsearch.imp;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.imp.session.TestVO;
import org.frameworkset.runtime.CommonLauncher;
import org.frameworkset.soa.ObjectSerializable;
import org.frameworkset.spi.geoip.IpInfo;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.mongodb.input.es.MongoDB2ESExportBuilder;
import org.frameworkset.tran.task.TaskCommand;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/25 22:36
 * @author biaoping.yin
 * @version 1.0
 */
public class Mongodb2DB {
	/**
	 * 启动运行同步作业主方法
	 * @param args
	 */
	public static void main(String[] args){

		Mongodb2DB dbdemo = new Mongodb2DB();
		dbdemo.scheduleImportData();
	}

	/**
	 * 同步作业实现和运行方法
	 */
	public void scheduleImportData(){
		// 5.2.1 清理Elasticsearch索引表mongodbdemo(可选步骤)
		//从application.properties配置文件中读取dropIndice属性，
		// 是否清除elasticsearch索引，true清除，false不清除，指定了默认值false
		boolean dropIndice = CommonLauncher.getBooleanAttribute("dropIndice",true);
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		//增量定时任务不要删表，但是可以通过删表来做初始化操作
		if(dropIndice) {
			try {
				//清除测试表,导入的时候自动重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
				String repsonse = clientInterface.dropIndice("mongodbdemo");
				System.out.println(repsonse);
			} catch (Exception e) {
			}
		}
		// 5.2.1 创建elasticsearch index mapping(可选步骤)
		//判断mongodbdemo是否存在，如果不存在则创建mongodbdemo
		boolean indiceExist = clientInterface.existIndice("mongodbdemo");
		ClientInterface configClientInterface = ElasticSearchHelper.getConfigRestClientUtil("dsl.xml");
		if(!indiceExist){

			configClientInterface.createIndiceMapping("mongodbdemo","createMongodbdemoIndice");
		}
		// 5.2.3 创建elasticsearch index template(可选步骤)
		String template = clientInterface.getTempate("mongodbdemo_template");
		if(template == null){
			configClientInterface.createTempate("mongodbdemo_template","createMongodbdemoTemplate");
		}
		// 5.2.4 编写同步代码
		//定义Mongodb到Elasticsearch数据同步组件
		MongoDB2ESExportBuilder importBuilder = MongoDB2ESExportBuilder.newInstance();

		// 5.2.4.1 设置mongodb参数
		importBuilder.setName("session")
				.setDb("sessiondb")
				.setDbCollection("sessionmonitor_sessions")
				.setConnectTimeout(10000)
				.setWriteConcern("JOURNAL_SAFE")
				.setReadPreference("")
				.setMaxWaitTime(10000)
				.setSocketTimeout(1500).setSocketKeepAlive(true)
				.setConnectionsPerHost(100)
				.setThreadsAllowedToBlockForConnectionMultiplier(6)
				.setServerAddresses("127.0.0.1:27017")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
				// mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
				//String database,String userName,String password,String mechanism
				//https://www.iteye.com/blog/yin-bp-2064662
//				.buildClientMongoCredential("sessiondb","bboss","bboss","MONGODB-CR")
//				.setOption("")
				.setAutoConnectRetry(true);

		//定义mongodb数据查询条件对象（可选步骤，全量同步可以不需要做条件配置）
		BasicDBObject query = new BasicDBObject();
		// 设定检索mongdodb session数据时间范围条件
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date start_date = format.parse("1099-01-01");
			Date end_date = format.parse("2999-01-01");
			query.append("creationTime",
					new BasicDBObject("$gte", start_date.getTime()).append(
							"$lte", end_date.getTime()));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		/**
		// 设置按照host字段值进行正则匹配查找session数据条件（可选步骤，全量同步可以不需要做条件配置）
		String host = "169.254.252.194-DESKTOP-U3V5C85";
		Pattern hosts = Pattern.compile("^" + host + ".*$",
				Pattern.CASE_INSENSITIVE);
		query.append("host", new BasicDBObject("$regex",hosts));*/
		//importBuilder.setQuery(query);

		//设定需要返回的session数据字段信息（可选步骤，同步全部字段时可以不需要做下面配置）
		BasicDBObject fetchFields = new BasicDBObject();
		fetchFields.put("appKey", 1);
		fetchFields.put("sessionid", 1);
		fetchFields.put("creationTime", 1);
		fetchFields.put("lastAccessedTime", 1);
		fetchFields.put("maxInactiveInterval", 1);
		fetchFields.put("referip", 1);
		fetchFields.put("_validate", 1);
		fetchFields.put("host", 1);
		fetchFields.put("requesturi", 1);
		fetchFields.put("lastAccessedUrl", 1);
		fetchFields.put("secure",1);
		fetchFields.put("httpOnly", 1);
		fetchFields.put("lastAccessedHostIP", 1);

		fetchFields.put("userAccount",1);
		fetchFields.put("testVO", 1);
		fetchFields.put("privateAttr", 1);
		fetchFields.put("local", 1);
		fetchFields.put("shardNo", 1);

		importBuilder.setFetchFields(fetchFields);
		// 5.2.4.3 导入elasticsearch参数配置
		importBuilder
				.setIndex("mongodbdemo") //必填项，索引名称
				.setIndexType("mongodbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
				.setBatchSize(10)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
				.setFetchSize(100)  //按批从mongodb拉取数据的大小
		        .setEsIdField("_id")//设置文档主键，不设置，则自动产生文档id,直接将mongodb的ObjectId设置为Elasticsearch的文档_id
				.setContinueOnError(true); // 忽略任务执行异常，任务执行过程抛出异常不中断任务执行

		// 5.2.4.4 jdk timer定时任务时间配置（可选步骤，可以不需要做以下配置）
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次

		// 5.2.4.5 并行任务配置（可选步骤，可以不需要做以下配置）
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//是否同步等待每批次任务执行完成后再返回调度程序，true 不等待所有导入作业任务结束，方法快速返回；false（默认值） 等待所有导入作业任务结束，所有作业结束后方法才返回

		// 5.2.4.6 数据加工处理（可选步骤，可以不需要做以下配置）
		// 全局记录配置：打tag，标识数据来源于jdk timer
		importBuilder.addFieldValue("fromTag","jdk timer");
		// 数据记录级别的转换处理（可选步骤，可以不需要做以下配置）
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				String id = context.getStringValue("_id");
				//根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中
				if(id.equals("5dcaa59e9832797f100c6806"))
					context.setDrop(true);
				//添加字段extfiled2到记录中，值为2
				context.addFieldValue("extfiled2",2);
				//添加字段extfiled到记录中，值为1
				context.addFieldValue("extfiled",1);
				boolean httpOnly = context.getBooleanValue("httpOnly");
				boolean secure = context.getBooleanValue("secure");
				String shardNo = context.getStringValue("shardNo");
				if(shardNo != null){
					//利用xml序列化组件将xml报文序列化为一个Integer
					context.addFieldValue("shardNo", ObjectSerializable.toBean(shardNo,Integer.class));
				}
				else{
					context.addFieldValue("shardNo", 0);
				}
				//空值处理
				String userAccount = context.getStringValue("userAccount");
				if(userAccount == null)
					context.addFieldValue("userAccount","");
				else{
					//利用xml序列化组件将xml报文序列化为一个String
					context.addFieldValue("userAccount", ObjectSerializable.toBean(userAccount,String.class));
				}
				//空值处理
				String testVO = context.getStringValue("testVO");
				if(testVO == null)
					context.addFieldValue("testVO","");
				else{
					//利用xml序列化组件将xml报文序列化为一个TestVO
					TestVO testVO1 = ObjectSerializable.toBean(userAccount, TestVO.class);
					context.addFieldValue("testVO", testVO1);
				}
				//空值处理
				String privateAttr = context.getStringValue("privateAttr");
				if(privateAttr == null) {
					context.addFieldValue("privateAttr", "");
				}
				else{
					//利用xml序列化组件将xml报文序列化为一个String
					context.addFieldValue("privateAttr", ObjectSerializable.toBean(privateAttr, String.class));
				}
				//空值处理
				String local = context.getStringValue("local");
				if(local == null)
					context.addFieldValue("local","");
				else{
					//利用xml序列化组件将xml报文序列化为一个String
					context.addFieldValue("local", ObjectSerializable.toBean(local, String.class));
				}
				//将long类型的lastAccessedTime字段转换为日期类型
				long lastAccessedTime = context.getLongValue("lastAccessedTime");
				context.addFieldValue("lastAccessedTime",new Date(lastAccessedTime));
				//将long类型的creationTime字段转换为日期类型
				long creationTime = context.getLongValue("creationTime");
				context.addFieldValue("creationTime",new Date(creationTime));
				//根据session访问客户端ip，获取对应的客户地理位置经纬度信息、运营商信息、省地市信息IpInfo对象
				//并将IpInfo添加到Elasticsearch文档中
				String referip = context.getStringValue("referip");
				if(referip != null){
					IpInfo ipInfo = context.getIpInfoByIp(referip);
					if(ipInfo != null)
						context.addFieldValue("ipInfo",ipInfo);
				}
				//除了通过context接口获取mongodb的记录字段，还可以直接获取当前的mongodb记录，可自行利用里面的值进行相关处理
				DBObject record = (DBObject) context.getRecord();
			}
		});

		// 5.2.4.7 设置同步作业结果回调处理函数（可选步骤，可以不需要做以下配置）
		//设置任务处理结果回调接口
		importBuilder.setExportResultHandler(new ExportResultHandler<Object,String>() {
			@Override
			public void success(TaskCommand<Object,String> taskCommand, String result) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
			}

			@Override
			public void error(TaskCommand<Object,String> taskCommand, String result) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
				/**
				//分析result，提取错误数据修改后重新执行,
				Object datas = taskCommand.getDatas();
				Object errorDatas = ... //分析result,从datas中提取错误数据，并设置到command中，通过execute重新执行任务
				taskCommand.setDatas(errorDatas);
				taskCommand.execute();
				 */
			}

			@Override
			public void exception(TaskCommand<Object,String> taskCommand, Exception exception) {
				System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
			}

			@Override
			public int getMaxRetry() {
				return 0;
			}
		});

		/**
		// 5.2.4.8 自定义Elasticsearch索引文档id生成机制（可选步骤，可以不需要做以下配置）
		//自定义Elasticsearch索引文档id生成机制
		importBuilder.setEsIdGenerator(new EsIdGenerator() {
			//如果指定EsIdGenerator，则根据下面的方法生成文档id，
			// 否则根据setEsIdField方法设置的字段值作为文档id，
			// 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id

			@Override
			public Object genId(Context context) throws Exception {
				return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
			}
		});*/

		// 5.2.4.9 设置增量字段信息（可选步骤，全量同步不需要做以下配置）
		//增量配置开始
		importBuilder.setNumberLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
		importBuilder.setFromFirst(true);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//设置增量查询的起始值lastvalue
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date.getTime());
		}
		catch (Exception e){
			e.printStackTrace();
		}

		// 5.2.4.10 执行作业
		/**
		 * 构建DataStream，执行mongodb数据到es的同步操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行同步操作
	}
}


```

完整的application.properties配置文件：

```properties
#同步作业主程序配置
mainclass=org.frameworkset.elasticsearch.imp.Mongodb2DB

# Elasticsearch配置
##x-pack或者searchguard账号和口令
elasticUser=elastic
elasticPassword=changeme


elasticsearch.rest.hostNames=192.168.137.1:9200
#elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282

#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
elasticsearch.showTemplate=true
elasticsearch.discoverHost=false

##http连接池配置
http.timeoutConnection = 5000
http.timeoutSocket = 50000
http.connectionRequestTimeout=10000
http.retryTime = 1
http.maxLineLength = -1
http.maxHeaderCount = 200
http.maxTotal = 200
http.defaultMaxPerRoute = 100
http.soReuseAddress = false
http.soKeepAlive = false
http.timeToLive = 3600000
http.keepAlive = 3600000
http.keystore =
http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
http.hostnameVerifier =

# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
dslfile.refreshInterval = 3000

# IP地理位置信息库配置
ip.cachesize = 2000
# 库下载地址https://dev.maxmind.com/geoip/geoip2/geolite2/
ip.database = E:/workspace/hnai/terminal/geolite2/GeoLite2-City.mmdb
ip.asnDatabase = E:/workspace/hnai/terminal/geolite2/GeoLite2-ASN.mmdb


```



## 5.3 调试并观察作业执行情况和日志

本节介绍在idea中调试运行之前编写的同步作业的方法,看看jdk timer调度作业的执行效果。

分两个步骤调试作业

步骤一 准备工作：首先保证mongodb和elasticsearch、以及kibana是运行状态，同时启动好sessionweb应用

步骤二 调试同步作业

### 5.3.1 准备工作

#### 5.3.1.1 启动mongodb

![](https://esdoc.bbossgroups.com/images\mongodb\startmongodb.png)

#### 5.3.1.2 启动elasticsearch

![](https://esdoc.bbossgroups.com/images\mongodb\startelasticsearch.png)

#### 5.3.1.3 启动kibana

![](https://esdoc.bbossgroups.com/images\mongodb\startkibana.png)

#### 5.4.1.4 启动session共享web应用

首先从以下地址下载session共享应用gradle源码工程，参考章节【4.同步作业开发环境搭建】导入session工程到idea：

[https://github.com/bbossgroups/sessiondemo](https://github.com/bbossgroups/sessiondemo)

导入idea后，配置和运行sessionmonitor这个web应用

![](https://esdoc.bbossgroups.com/images\mongodb\configsessionmonitor1.png)
![](https://esdoc.bbossgroups.com/images\mongodb\configsessionmonitor2.png)
![](https://esdoc.bbossgroups.com/images\mongodb\configsessionmonitor3.png)
![](https://esdoc.bbossgroups.com/images\mongodb\configsessionmonitor4.png)
![](https://esdoc.bbossgroups.com/images\mongodb\configsessionmonitor5.png)

查看session数据:

[http://localhost:9090/sessionmonitor/session/sessionManager/sessionManager.page](http://localhost:9090/sessionmonitor/session/sessionManager/sessionManager.page)

![](https://esdoc.bbossgroups.com/images\mongodb\configsessionmonitor6.png)

### 5.3.2 调试同步作业

在作业类中需要调试的代码处添加断点，然后启动调试程序即可：

![](https://esdoc.bbossgroups.com/images\mongodb\debugjob.png)
调试过滤记录功能
![](https://esdoc.bbossgroups.com/images\mongodb\debugjobfilter.png)

### 5.3.3 查看同步作业任务执行日志
第一次调度执行作业执行日志查看：第一次有10条数据进行同步单是被过滤掉一条日志
![](https://esdoc.bbossgroups.com/images\mongodb\debugjoblogs.png)

增量调度执行作业日志查看：
![](https://esdoc.bbossgroups.com/images\mongodb\debugincrtjoblogs.png)


## 5.4 同步作业参数提取/发布/部署

数据同步作业开发调试完毕后，接下介绍如何将同步作业发布成一个可以部署运行的作业包。

### 5.4.1 参数提取

在发布版本之前，我们可以对代码做些调整，将需要根据实际情况进行调整的参数从代码中提取到配置文件application.properties文件中，例如：

1.mongodb相关参数（mongodb服务器地址、mongodb数据库和collection等）

2.线程池和线程队列数

3.增量状态起始值(lastValue)和增量状态保存路径（lastValueStorePath）等等

4.fetchSize和batchSize

参数提取出来后，需要通过工具类CommonLauncher提供的相关方法获取参数值，例如：

在application.propterties添加以下参数：

```properties
batchSize=10
fetchSize=10000
queueSize=10
workThreads=10
mongodb.name=session
mongodb.db=sessiondb
mongodb.collection=sessionmonitor_sessions
mongodb.connectTimeout=10000
mongodb.writeConcern=JOURNAL_SAFE
mongodb.readPreference=
mongodb.maxWaitTime=10000
mongodb.socketTimeout=10000
mongodb.socketKeepAlive=true
mongodb.autoConnectRetry=true
mongodb.serverAddresses=127.0.0.1:27017
mongodb.connectionsPerHost=100
mongodb.threadsAllowedToBlockForConnectionMultiplier=6
```

同步作业类中获取和设置参数代码:

```java
int batchSize = CommonLauncher.getIntProperty("batchSize",10);//必须同时指定了默认值,因为开发调试的时候会用默认值
int queueSize = CommonLauncher.getIntProperty("queueSize",50);//必须同时指定了默认值,因为开发调试的时候会用默认值
int workThreads = CommonLauncher.getIntProperty("workThreads",10);//必须同时指定了默认值,因为开发调试的时候会用默认值
importBuilder.setBatchSize(batchSize);
importBuilder.setQueue(queueSize);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(workThreads);//设置批量导入线程池工作线程数量
String mongodbName = CommonLauncher.getProperty("mongodb.name","session");
String mongodbDB = CommonLauncher.getProperty("mongodb.db","sessiondb");
String mongodbCollection = CommonLauncher.getProperty("mongodb.collection","sessionmonitor_sessions");
int mongodbtConnectTimeout = CommonLauncher.getIntProperty("mongodb.connectTimeout",10000);
String mongodbWriteConcern = CommonLauncher.getProperty("mongodb.writeConcern","");
String mongodbReadPreference = CommonLauncher.getProperty("mongodb.readPreference","");
int mongodbMaxWaitTime = CommonLauncher.getIntProperty("mongodb.maxWaitTime",10000);
int mongodbSocketTimeout = CommonLauncher.getIntProperty("mongodb.socketTimeout",10000);
int mongodbSocketKeepAlive = CommonLauncher.getBooleanAttribute("mongodb.socketKeepAlive",true);
boolean mongodbAutoConnectRetry = CommonLauncher.getBooleanAttribute("mongodb.autoConnectRetry",true);
String mongodbServerAddresses = CommonLauncher.getProperty("mongodb.serverAddresses","127.0.0.1:27017");
int mongodbConnectionsPerHost = CommonLauncher.getIntProperty("mongodb.connectionsPerHost",100);
int mongodbThreadsAllowedToBlockForConnectionMultiplier = CommonLauncher.getIntProperty("mongodb.threadsAllowedToBlockForConnectionMultiplier",6);
importBuilder.setName(mongodbName)
				.setDb(mongodbDB)
				.setDbCollection(mongodbCollection)
				.setConnectTimeout(mongodbtConnectTimeout)
				.setWriteConcern(mongodbWriteConcern)
				.setReadPreference(mongodbReadPreference)
				.setMaxWaitTime(mongodbMaxWaitTime)
				.setSocketTimeout(mongodbSocketTimeout)
                .setSocketKeepAlive(mongodbSocketKeepAlive)
				.setConnectionsPerHost(mongodbConnectionsPerHost)
				.setThreadsAllowedToBlockForConnectionMultiplier(mongodbThreadsAllowedToBlockForConnectionMultiplier)
				.setServerAddresses(mongodbServerAddresses)//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
				.setAutoConnectRetry(mongodbAutoConnectRetry);
```

### 5.4.2 发布作业

参数提取梳理完毕后，打包发布版本，同步作业方法发布作业非常简单，直接在工程根目录下点击运行release.bat指令即可：

![](https://esdoc.bbossgroups.com/images\mongodb\release.png)

在命令行提升**build successful**说明打包发布成功：

![](https://esdoc.bbossgroups.com/images\mongodb\releasesuccess.png)

运行包所在目录：build\distributions

![](https://esdoc.bbossgroups.com/images\mongodb\releasezip.png)

### 5.4.3 运行和停止作业

将zip包分发到服务器解压即可，运行方法见图示：

![](https://esdoc.bbossgroups.com/images\mongodb\runjob.png)

###  5.4.4 运行效果

同步作业启动后可以查看同步日志文件中的日志：es.log

![](https://esdoc.bbossgroups.com/images\mongodb\restartjob.png)

可以在kibana中查看同步到elasticsearch中的session数据：

![](https://esdoc.bbossgroups.com/images\mongodb\kibanasessiondatas.png)

可以通过修改application.properties的配置来关闭dsl调试功能：

```properties
elasticsearch.showTemplate=false
```

### 5.4.5 作业jvm内存调整

根据服务器资源情况，可以适当调整jvm内存，修改jvm.options,设置作业运行需要的jvm内存，按照比例调整Xmx和MaxNewSize参数：

```properties
-Xms1g
-Xmx1g
-XX:NewSize=512m
-XX:MaxNewSize=512m
```

Xms和Xmx保持一样，NewSize和MaxNewSize保持一样，Xmx和MaxNewSize大小保持的比例可以为3:1或者2:1

## 5.5 基于xxl-job的数据同步作业 

xxl-job调度的作业类实现和jdk timer调度的作业稍微有点不同，只需要构建一个importbuilder组件即可不需要添加作业执行代码，下面举例说明。

xxl-job同步作业类需要依赖xxl-job组件，所以需要修改工程build.gradle文件添加xx-job依赖包：

```groovy
compile 'com.xuxueli:xxl-job-core:2.0.2'
```

### 5.5.1编写作业类

基于xxl-job的数据同步作业类，参数配置和数据转换处理与jdk timer一样，只是任务调度采用外部分布式调度引擎xxl-job；只需要构建一个externalScheduler组件即可；不需要添加作业执行代码；同时在代码中把xxl-job的分片号作为数据查询条件；去掉jdk timer定时任务相关配置代码。作业类需要继承抽象类，实现抽象方法init即可

```java
org.frameworkset.tran.schedule.xxjob.AbstractXXLJobHandler
```

添加分片号检索条件代码:采用xml序列化组件ObjectSerializable将分片号index序列化为一个xml报文，将该报文作为检索条件进行检索（因为session中的数据都是采用xml报文进行存储）

```java
            //定义mongodb数据查询条件对象（可选步骤，全量同步可以不需要做条件配置）
			BasicDBObject query = new BasicDBObject();
            // 提取集群节点分片号，将分片号作为检索同步数据的条件,实现分片同步功能
			ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
			int index = 0;
			if(shardingVO != null) {
				index = shardingVO.getIndex();
				logger.info("index:>>>>>>>>>>>>>>>>>>>" + shardingVO.getIndex());
				logger.info("total:>>>>>>>>>>>>>>>>>>>" + shardingVO.getTotal());
			}
			// 采用xml序列化组件将index序列化为一个xml报文，将改报文作为检索条件进行检索
			String idxStr = ObjectSerializable.toXML(index);
			query.append("shardNo",idxStr );
```

完整的同步作业类XXJobMongodb2ESImportTask：

```java
package org.frameworkset.elasticsearch.imp;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.xxl.job.core.util.ShardingUtil;
import org.frameworkset.elasticsearch.imp.session.TestVO;
import org.frameworkset.soa.ObjectSerializable;
import org.frameworkset.spi.geoip.IpInfo;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.mongodb.input.es.MongoDB2ESExportBuilder;
import org.frameworkset.tran.schedule.ExternalScheduler;
import org.frameworkset.tran.schedule.xxjob.AbstractXXLJobHandler;
import org.frameworkset.tran.task.TaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class XXJobMongodb2ESImportTask extends AbstractXXLJobHandler {
	private static Logger logger = LoggerFactory.getLogger(XXJobMongodb2ESImportTask.class);
	public void init(){
		// 可参考Sample示例执行器中的示例任务"ShardingJobHandler"了解试用

		externalScheduler = new ExternalScheduler();
		externalScheduler.dataStream((Object params)->{

			logger.info("params:>>>>>>>>>>>>>>>>>>>" + params);
			// 5.2.4 编写同步代码
			//定义Mongodb到Elasticsearch数据同步组件
			MongoDB2ESExportBuilder importBuilder = MongoDB2ESExportBuilder.newInstance();

			// 5.2.4.1 设置mongodb参数
			importBuilder.setName("session")
					.setDb("sessiondb")
					.setDbCollection("sessionmonitor_sessions")
					.setConnectTimeout(10000)
					.setWriteConcern("JOURNAL_SAFE")
					.setReadPreference("")
					.setMaxWaitTime(10000)
					.setSocketTimeout(1500).setSocketKeepAlive(true)
					.setConnectionsPerHost(100)
					.setThreadsAllowedToBlockForConnectionMultiplier(6)
					.setServerAddresses("127.0.0.1:27017")//多个地址用回车换行符分割：127.0.0.1:27017\n127.0.0.1:27018
					// mechanism 取值范围：PLAIN GSSAPI MONGODB-CR MONGODB-X509，默认为MONGODB-CR
					//String database,String userName,String password,String mechanism
					//https://www.iteye.com/blog/yin-bp-2064662
//				.buildClientMongoCredential("sessiondb","bboss","bboss","MONGODB-CR")
//				.setOption("")
					.setAutoConnectRetry(true);

			//定义mongodb数据查询条件对象（可选步骤，全量同步可以不需要做条件配置）
			BasicDBObject query = new BasicDBObject();

			// 提取集群节点分片号，将分片号作为检索同步数据的条件,实现分片同步功能
			ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
			int index = 0;
			if(shardingVO != null) {
				index = shardingVO.getIndex();
				logger.info("index:>>>>>>>>>>>>>>>>>>>" + shardingVO.getIndex());
				logger.info("total:>>>>>>>>>>>>>>>>>>>" + shardingVO.getTotal());
			}
			try {
				String idxStr = ObjectSerializable.toXML(index);
				query.append("shardNo",idxStr );
			} catch (Exception e) {
				e.printStackTrace();
			}


			// 设定检索mongdodb session数据时间范围条件
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date start_date = format.parse("1099-01-01");
				Date end_date = format.parse("2999-01-01");
				query.append("creationTime",
						new BasicDBObject("$gte", start_date.getTime()).append(
								"$lte", end_date.getTime()));
			}
			catch (Exception e){
				e.printStackTrace();
			}
			/**
			 // 设置按照host字段值进行正则匹配查找session数据条件（可选步骤，全量同步可以不需要做条件配置）
			 String host = "169.254.252.194-DESKTOP-U3V5C85";
			 Pattern hosts = Pattern.compile("^" + host + ".*$",
			 Pattern.CASE_INSENSITIVE);
			 query.append("host", new BasicDBObject("$regex",hosts));*/
			importBuilder.setQuery(query);

			//设定需要返回的session数据字段信息（可选步骤，同步全部字段时可以不需要做下面配置）
			BasicDBObject fetchFields = new BasicDBObject();
			fetchFields.put("appKey", 1);
			fetchFields.put("sessionid", 1);
			fetchFields.put("creationTime", 1);
			fetchFields.put("lastAccessedTime", 1);
			fetchFields.put("maxInactiveInterval", 1);
			fetchFields.put("referip", 1);
			fetchFields.put("_validate", 1);
			fetchFields.put("host", 1);
			fetchFields.put("requesturi", 1);
			fetchFields.put("lastAccessedUrl", 1);
			fetchFields.put("secure",1);
			fetchFields.put("httpOnly", 1);
			fetchFields.put("lastAccessedHostIP", 1);

			fetchFields.put("userAccount",1);
			fetchFields.put("testVO", 1);
			fetchFields.put("privateAttr", 1);
			fetchFields.put("local", 1);
            fetchFields.put("shardNo", 1);
            
			importBuilder.setFetchFields(fetchFields);
			// 5.2.4.3 导入elasticsearch参数配置
			importBuilder
					.setIndex("mongodbdemo") //必填项，索引名称
					.setIndexType("mongodbdemo") //es 7以后的版本不需要设置indexType或者设置为_doc，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
					.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
					.setBatchSize(10)  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
					.setFetchSize(100)  //按批从mongodb拉取数据的大小
					.setEsIdField("_id")//设置文档主键，不设置，则自动产生文档id,直接将mongodb的ObjectId设置为Elasticsearch的文档_id
					.setContinueOnError(true); // 忽略任务执行异常，任务执行过程抛出异常不中断任务执行

			// 5.2.4.5 并行任务配置（可选步骤，可以不需要做以下配置）
			importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
			importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
			importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
			importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
			importBuilder.setAsyn(false);//是否同步等待每批次任务执行完成后再返回调度程序，true 不等待所有导入作业任务结束，方法快速返回；false（默认值） 等待所有导入作业任务结束，所有作业结束后方法才返回

			// 5.2.4.6 数据加工处理（可选步骤，可以不需要做以下配置）
			// 全局记录配置：打tag，标识数据来源于xxljob
			 importBuilder.addFieldValue("fromTag","xxljob");
			// 数据记录级别的转换处理（可选步骤，可以不需要做以下配置）
			importBuilder.setDataRefactor(new DataRefactor() {
				public void refactor(Context context) throws Exception  {
					String id = context.getStringValue("_id");
					//根据字段值忽略对应的记录，这条记录将不会被同步到elasticsearch中
					if(id.equals("5dcaa59e9832797f100c6806"))
						context.setDrop(true);
					//添加字段extfiled2到记录中，值为2
					context.addFieldValue("extfiled2",2);
					//添加字段extfiled到记录中，值为1
					context.addFieldValue("extfiled",1);
					boolean httpOnly = context.getBooleanValue("httpOnly");
					boolean secure = context.getBooleanValue("secure");
					String shardNo = context.getStringValue("shardNo");
					if(shardNo != null){
						context.addFieldValue("shardNo", ObjectSerializable.toBean(shardNo,Integer.class));
					}
					else{
						context.addFieldValue("shardNo", 0);
					}
					//空值处理
					String userAccount = context.getStringValue("userAccount");
					if(userAccount == null)
						context.addFieldValue("userAccount","");
					else{
						context.addFieldValue("userAccount", ObjectSerializable.toBean(userAccount,String.class));
					}
					//空值处理
					String testVO = context.getStringValue("testVO");
					if(testVO == null)
						context.addFieldValue("testVO","");
					else{
						context.addFieldValue("testVO", ObjectSerializable.toBean(userAccount, TestVO.class));
					}
					//空值处理
					String privateAttr = context.getStringValue("privateAttr");
					if(privateAttr == null) {
						context.addFieldValue("privateAttr", "");
					}
					else{
						context.addFieldValue("privateAttr", ObjectSerializable.toBean(privateAttr, String.class));
					}
					//空值处理
					String local = context.getStringValue("local");
					if(local == null)
						context.addFieldValue("local","");
					else{
						context.addFieldValue("local", ObjectSerializable.toBean(local, String.class));
					}
					//将long类型的lastAccessedTime字段转换为日期类型
					long lastAccessedTime = context.getLongValue("lastAccessedTime");
					context.addFieldValue("lastAccessedTime",new Date(lastAccessedTime));
					//将long类型的creationTime字段转换为日期类型
					long creationTime = context.getLongValue("creationTime");
					context.addFieldValue("creationTime",new Date(creationTime));
					//根据session访问客户端ip，获取对应的客户地理位置经纬度信息、运营商信息、省地市信息IpInfo对象
					//并将IpInfo添加到Elasticsearch文档中
					String referip = context.getStringValue("referip");
					if(referip != null){
						IpInfo ipInfo = context.getIpInfoByIp(referip);
						if(ipInfo != null)
							context.addFieldValue("ipInfo",ipInfo);
					}
					//除了通过context接口获取mongodb的记录字段，还可以直接获取当前的mongodb记录，可自行利用里面的值进行相关处理
					DBObject record = (DBObject) context.getRecord();
				}
			});

			// 5.2.4.7 设置同步作业结果回调处理函数（可选步骤，可以不需要做以下配置）
			//设置任务处理结果回调接口
			importBuilder.setExportResultHandler(new ExportResultHandler<Object,String>() {
				@Override
				public void success(TaskCommand<Object,String> taskCommand, String result) {
					System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
				}

				@Override
				public void error(TaskCommand<Object,String> taskCommand, String result) {
					System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
					/**
					 //分析result，提取错误数据修改后重新执行,
					 Object datas = taskCommand.getDatas();
					 Object errorDatas = ... //分析result,从datas中提取错误数据，并设置到command中，通过execute重新执行任务
					 taskCommand.setDatas(errorDatas);
					 taskCommand.execute();
					 */
				}

				@Override
				public void exception(TaskCommand<Object,String> taskCommand, Exception exception) {
					System.out.println(taskCommand.getTaskMetrics());//打印任务执行情况
				}

				@Override
				public int getMaxRetry() {
					return 0;
				}
			});

			// 5.2.4.9 设置增量字段信息（可选步骤，全量同步不需要做以下配置）
			//增量配置开始
			importBuilder.setNumberLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
			importBuilder.setFromFirst(false);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
			//设置增量查询的起始值lastvalue
			try {
				Date date = format.parse("2000-01-01");
				importBuilder.setLastValue(date.getTime());
			}
			catch (Exception e){
				e.printStackTrace();
			}
			// 直接返回importBuilder组件
			return importBuilder;
		});

	}


}


```

调整application.properties文件，添加xxl-job作业相关的配置

```properties
# xxjob分布式作业任务配置

### xxl-job admin address list, such as "http://address" or "http://address01,http://address02"
xxl.job.admin.addresses=http://127.0.0.1:18001/xxl-job-admin

### xxl-job executor address
xxl.job.executor.appname=mongodb-elasticsearch-xxjob
# 作业执行服务器ip
xxl.job.executor.ip=192.168.137.1
# 作业执行服务器端口
xxl.job.executor.port=9993

### xxl-job, access token
xxl.job.accessToken=

### xxl-job log path
xxl.job.executor.logpath=d:/xxl-job/
### xxl-job log retention days
xxl.job.executor.logretentiondays=-1
##
# 作业任务配置
# xxl.job.task为前置配置多个数据同步任务，后缀XXJobImportTask和OtherTask将xxjob执行任务的名称
# 作业程序都需要继承抽象类org.frameworkset.tran.schedule.xxjob.AbstractXXLJobHandler
# public void init(){
#		externalScheduler = new ExternalScheduler();
#		externalScheduler.dataStream(()->{
#         DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
#              编写导入作业任务配置逻辑，参考文档：https://esdoc.bbossgroups.com/#/db-es-tool
#         return    importBuilder;
#       }
# }
#

# 配置mongodb-elasticsearch作业程序
xxl.job.task.XXJobMongodb2ESImportTask = org.frameworkset.elasticsearch.imp.XXJobMongodb2ESImportTask
## xxl.job.task.otherTask = org.frameworkset.elasticsearch.imp.jobhandler.OtherTask

#配置运行xxl-job作业主程序，由同步框架提供不需要自己编写
mainclass=org.frameworkset.tran.schedule.xxjob.XXJobApplication
```

### 5.2.4 保存增量同步状态的外部数据源配置

采用xxl-job调度作业任务时，必须在application.properties文件中配置完整的保存增量同步状态的config.db数据源：

```properties
config.db.name = testconfig
config.db.user = root
config.db.password = 123456
config.db.driver = com.mysql.jdbc.Driver
config.db.url = jdbc:mysql://192.168.137.1:3306/bboss?useCursorFetch=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
config.db.usePool = true
config.db.validateSQL = select 1
config.db.showsql = true
```



### 5.5.3 调试xxl-job调度作业（分片同步数据机制）、观察作业执行情况和日志

前提：事先运行xxl-job-admin

http://127.0.0.1:18001/xxl-job-admin

在工程目录添加文件：

src\test\java\org\frameworkset\elasticsearch\imp\XXLJobApplicationTest.java，添加一个main方法来启动xxl-job执行器来调试作业：

```java
package org.frameworkset.elasticsearch.imp;

import org.frameworkset.tran.schedule.xxjob.XXJobApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xuxueli 2018-10-31 19:05:43
 */
public class XXLJobApplicationTest {
    private static Logger logger = LoggerFactory.getLogger(XXLJobApplicationTest.class);

    public static void main(String[] args) {

        XXJobApplication.main(args);

    }

}

```

为了查看分片数据同步效果，需要在idea中通过运行XXLJobApplicationTest启动两个作业进程，先在9993端口启动一个XXLJobApplicationTest,启动后修改xxl.job.executor.port为不同的端口9994，然后再启动一个XXLJobApplicationTest进程

```
# 作业执行服务器ip
xxl.job.executor.ip=192.168.137.1
# 作业执行服务器端口
xxl.job.executor.port=9994
```

两个进程对应的分片号分别为0和1，然后访问打开多个浏览器，访问session应用，将会随机生成shardNo属性值为0和1的session记录数据，就可以观察到两个xxl-job作业执行器中的数据同步作业会分别同步shardNo属性值为0和1的session记录数据。

任务详细执行情况和日志与jdk timer调度执行类似，也可以登录xxl-job管理界面查看作业执行情况，控制和启动作业，也可以想jdk timer作业一样通过stop指令停止作业，restart指令重启作业，这里不做过多介绍。

同步作业参数提取/发布/部署/jvm配置与jdk timer类似，这里也不做过多介绍。更多的内容可以参考文档：

[基于xxjob-同步db-elasticsearch数据](https://esdoc.bbossgroups.com/#/db-es-tool?id=_26-基于xxjob-同步db-elasticsearch数据)

## 5.6 集成同步功能到自己的项目中（代码和maven坐标）

前面介绍开发单独调度执行同步作业的方法，我们也可以将作业方法里面的代码整合到自己的项目中运行，只需要将maven坐标导入自己的项目即可：

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
            <version>5.9.5</version>
        </dependency>
```

如果是spring boot项目，再多导入一个maven坐标：

```xml
       <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>5.9.5</version>
        </dependency>
```



# 6.总结

本案例介绍了mongodb-elasticsearch作业同步功能，重点介绍通过jdk timer调度同步作业，xxl-job分布式调度机制由于篇幅关系没有深入介绍，实现方法与jdk timer类似，更多的内容可以参考文档：

[基于xxjob-同步db-elasticsearch数据](https://esdoc.bbossgroups.com/#/db-es-tool?id=_26-基于xxjob-同步db-elasticsearch数据)



# 7.参考文档

## 7.1 Session共享应用部署和使用文档

[https://my.oschina.net/bboss/blog/758871](https://my.oschina.net/bboss/blog/758871)

## 7.2 Elasticsearch java highlevel rest client bboss 

[https://esdoc.bbossgroups.com/](https://esdoc.bbossgroups.com/)

## 7.3 xxl-job官方文档和源码地址

[https://github.com/xuxueli/xxl-job](https://github.com/xuxueli/xxl-job)

