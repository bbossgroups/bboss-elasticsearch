# Mongodb-Elasticsearch同步工具实践

如何快速将保存在 MongoDB 中的海量数据同步到 Elasticshearch 中是一件非常具有挑战意义的事情，本话题分享基于分布式任务调度引擎、多线程高并发技术快速将保存在 MongoDB 中的海量数据同步到 Elasticshearch 中实战技术和经验。* 

# 1.数据同步概述

在介绍MongoDB-Elasticshearch数据同步案例之前， 先了解一下基于java编写的数据同步工具-bboss![bboss数据同步工具](https://esdoc.bbossgroups.com/images/datasyn.png)

与logstash类似，bboss主要功能特点：

1. 支持多种类型数据源数据同步功能

 - 将数据库表数据同步到Elasticsearch
 - 将数据库表数据同步到数据库表
 - 将Elasticsearch数据同步到数据库表
 - 将Elasticsearch数据同步到Elasticsearch
 - 将mongodb数据同步到Elasticsearch
 - 将mongodb数据同步到数据库表
 - 将hbase数据同步到Elasticsearch
 - 从kafka接收数据导入elasticsearch（支持kafka_2.12-0.10.2.0和kafka_2.12-2.3.0 系列版本）

2. 支持的导入方式
	 - 逐条数据导入
	 - 批量数据导入
	 - 批量数据多线程并行导入
	 - 定时全量（串行/并行）数据导入
	 - 定时增量（串行/并行）数据导入

3. 支持的数据库： mysql,maridb，postgress,oracle ,sqlserver,db2,tidb,hive，mongodb等

4. 支持的Elasticsearch版本： 1.x,2.x,5.x,6.x,7.x,+

5. 支持将ip转换为对应的运营商和城市地理位置信息

6. 支持多种定时任务执行引擎：

	 - jdk timer （内置）
	 - quartz
	 - xxl-job分布式调度引擎，基于分片调度机制实现海量数据快速同步能力

bboss另一个显著的特色就是直接基于java语言来编写数据同步作业程序，基于强大的java语言和第三方工具包(本文就涉及到使用第三方库将保存在session中的xml报文序列化为java对象案例)，能够非常方便地加工和处理需要同步的数据，然后将处理过的数据同步到目标库（Elasticsearch或者数据库）；同时也可以非常方便地在idea或者eclipse中调试和运行同步作业程序，调试无误后，通过bboss提供的gradle脚本来构建和发布可部署到生产环境的同步作业包。因此，对广大的java程序员来说，bboss无疑是一个轻易快速上手的数据同步利器,本质上来讲bboss为大家提供了一种简单而熟悉的开发编程方式，通过这种方式既可以开发简单的单机版并行数据同步作业程序，也可以开发出强大而复杂的分布式数据并行同步作业程序，从而实现海量数据同步功能。

​	下面我们通过一个session数据同步案例来介绍mongodb-elasticsearch的数据同步功能。

# 2.同步案例介绍-session数据同步

​	案例描述：将保存在mongodb中的web应用session会话数据，根据session最后访问时间，定时增量同步到Elasitcsearch中;同步过程中，需要对session数据进行加工、转换和过滤处理，再存入Elasticsearch，例如使用第三方库将xml报文数据转换为java类型数据。

​	案例开发：我们在idea中开发和调试数据同步作业，利用gradle构建和发布同步作业包，基于bboss提供运行指令启动运行和停止作业。分别采用jdk timer和xxl-job两种调度机制来定时调度和运行作业。

​	案例演示：事先运行一个基于mongodb存储session数据的web应用，并启动增量同步作业，打开多个浏览器访问web应用，不断产生和更新session数据。然后在kibana和session监控界面，观察增量同步session数据的效果，演示两种调度机制同步效果：

1. 基于jdk timer

2. 基于xxl-job来调度作业演示数据分片同步功能

下面结合session数据同步案例，正式切入本文主题。

# 3.环境准备

## 3.1 开发环境

在windows环境开发和调试同步作业程序，需要在电脑上安装以下软件

1. jdk 1.8或以上

2. idea 2019

3. gradle最新版本  

  [https://gradle.org/releases/](https://gradle.org/releases/)

4. mongodb 4.2.1 

5. elasticsearch版本6.5.0，亦可以采用最新的版本

6. 一个基于mongodb存储session数据的web应用，如有需要，可线下找我提供，或者到以下地址下载：

  [https://github.com/bbossgroups/sessiondemo](https://github.com/bbossgroups/sessiondemo)

7. mongodb-elasticsearch工具工程（基于gradle）

8. xxl-job分布式定时任务引擎

自行安装好上述软件，这里着重说明一下gradle配置，需要配置三个个环境变量：

GRADLE_HOME: 指定gradle安装目录

GRADLE_USER_HOME: 指定gradle从maven中央库下载依赖包本地存放目录

 M2_HOME: maven安装目录（可选，如果有需要或者使用gradle过程中有问题就加上）

![](https://esdoc.bbossgroups.com/images/env.png)

![](https://esdoc.bbossgroups.com/images/env1.png)

详细gradle安装和配置参考文档： [https://esdoc.bbossgroups.com/#/bboss-build](https://esdoc.bbossgroups.com/#/bboss-build) 

## 3.2 运行环境

​    同步作业本身只依赖有jdk1.8即可部署运行，与线上mongodb和elasticsearch对接即可

# 4.同步作业开发环境搭建

## 4.1 下载开发环境工程

我们无需从0开始搭建开发环境，可以到以下地址下载已经配置好的Mongodb-Elasticsearch开发环境：
 [https://github.com/bbossgroups/mongodb-elasticsearch](https://github.com/bbossgroups/mongodb-elasticsearch) 

![down](https://esdoc.bbossgroups.com/images/downmongodb2es.png)

下载后解压到目录：

![image-20191124223658972](https://esdoc.bbossgroups.com/images/mongodbdir.png)
## 4.2 导入工程到Idea及gradle配置
参考下面的向导将工程导入idea、调整gradle配置、熟悉idea中使用gradle

第一步 导入工程

![newproject](https://esdoc.bbossgroups.com/images/mongodb/newproject.png)

![image-20191124233037071](https://esdoc.bbossgroups.com/images/mongodb/selectproject.png)

![image-20191124233257671](https://esdoc.bbossgroups.com/images/mongodb/selectgradle.png)

![image-20191124233257671](https://esdoc.bbossgroups.com/images/mongodb/newwindow.png)

![image-20191124233712833](https://esdoc.bbossgroups.com/images/mongodb/importcomplete.png)

第二步 进入setting，设置工程的gradle配置：

![](https://esdoc.bbossgroups.com/images/mongodb/settingprojectgradle.png)
第三步 验证工程
设置完毕后，进入gradle面板

![](https://esdoc.bbossgroups.com/images/mongodb/importsuccess.png)

可以选择gradle相关的任务进行clean和install构建操作：

![image-20191124234308907](https://esdoc.bbossgroups.com/images/mongodb/install.png)
工程导入完毕，下面介绍一下工程目录结构和关键文件

## 4.3 工程目录结构和配置文件

工程采用典型的类似maven项目的目录结构管理源码，工程目录文件说明如下：

mongodb-elasticsearch-master

1.lib 

同步作业需要依赖的第三方jar可以存放到lib目录（在maven中央库或者本地maven库中没有的jar文件）

2.runfiles 

存放同步作业运行和停止的windows、linux、unix指令模板，根据指令模板生成最终的运行指令

3.src/main/java 

存放作业源码类文件

4.src/main/resources 

存放作业运行配置文件（es配置、参数配置）

5.src/main/resources/application.properties 

application.properties是同步作业的主配置文件，包含es配置、参数配置、作业运行主程序配置，如果需要采用相应的调度机制，可以从对应的调度机制配置示例文件复制内容到application.properties即可

6.src/main/resources/application.properties.jdktimer 

jdktimer 调度作业运行关键配置文件示例（es配置、参数配置、作业运行主程序配置）

7.src/main/resources/application.properties.quartz 

quartz调度作业运行关键配置文件示例（es配置、参数配置、作业运行主程序配置）

8.src/main/resources/application.properties.xxljob 

 xxl-job调度作业运行关键配置文件示例（es配置、参数配置、作业运行主程序配置）

9.build.gradle  

构建和发布作业的gradle构建脚本

10.gradle.properties  

gradle属性配置文件，这些属性在build.gradle文件中引用

11.release.bat  

构建和发布版本的指令（针对windows环境）

## 4.4 工程附带同步案例

在此工程中已经有3个同步案例类：

```java
org.frameworkset.elasticsearch.imp.Mongodb2DBdemo --基于jdk timer mongodb到数据库同步案例
org.frameworkset.elasticsearch.imp.Mongodb2ESdemo  --基于jdk timer mongodb到elasticsearch同步案例（本文详细讲解）
org.frameworkset.elasticsearch.imp.QuartzImportTask --mongodb到elasticsearch quartz定时任务同步案例

```

本案例将开发两个新的作业程序：

```
org.frameworkset.elasticsearch.imp.Mongodb2ES --基于jdk timer mongodb到数据库同步案例
org.frameworkset.elasticsearch.imp.XXJobMongodb2ESImportTask  --基于xxl-job mongodb到elasticsearch同步案例
```

数据同步作业工程导入idea后，即可进入同步作业开发、调试环节。

# 5.同步作业程序开发调试发布

## 5.1 案例说明

session数据同步案例前面已经做了具体介绍，本节介绍mongodb session表结构和elasticsearch索引表结构映射关系：

mongodb对应的db：sessiondb

mongodb对应的dbcollection：sessionmonitor_sessions

elasticsearch 索引名称：mongodbdemo 索引类型：mongodbdemo

我们采用与mongodb session数据表一致的默认elasticsearch 索引结构，也可以根据实际情况自定义elasticsearch 索引结构或者elasticsearch 索引模板。

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

## 5.2 建立同步作业类-Mongodb2ES

我们先新建一个基于jdk timer的数据同步作业类Mongodb2ES，定义main方法和同步方法scheduleImportData，后面的xxl-job的作业在此基础上进行改进即可。

org.frameworkset.elasticsearch.imp.Mongodb2ES

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

代码作用：根据配置的boolean属性dropIndice，控制是否在启动作业时删除Elasticsearch中的索引表。
### 5.2.2 创建elasticsearch index mapping(可选)

可以手动创建索引mongodbdemo：

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

判断索引表mongodbdemo是否存在并创建mongodbdemo的代码：

```java
//判断mongodbdemo是否存在，如果不存在则创建mongodbdemo
		boolean indiceExist = clientInterface.existIndice("mongodbdemo");
		if(!indiceExist){
			ClientInterface configClientInterface = ElasticSearchHelper.getConfigRestClientUtil("dsl.xml");
			configClientInterface.createIndiceMapping("mongodbdemo","createMongoddbdemoIndice");
		}
```

### 5.2.3 创建elasticsearch index template（可选）

如果我们采用按照时间动态滚动索引表（按时间分表保存数据），并且需要定制索引结构，则需要创建索引模板(IndexTemplate):

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

mongodbdemo-开头的索引表都会按照模板建立索引结构,例如索引表mongodbdemo-2019.11.26

获取名称为mongodbdemoTemplate的索引模板，如果为null，则创建模板mongodbdemoTemplate：

```java
	String template = clientInterface.getTempate("mongodbdemoTemplate");
		if(template == null){
			configClientInterface.createTempate("mongodbdemoTemplate","createMongoddbdemoTemplate");
		}
	}
```

5.2.1和5.2.2/5.2.3都是管理索引表和索引模板的准备工作，可以根据实际情况选择一个自定义索引表结构还是定义索引模板结构，接下来进入同步作业代码编写阶段。

### 5.2.4 编写同步代码

同步组件：

MongoDB2ESExportBuilder importBuilder = MongoDB2ESExportBuilder.newInstance();

#### 5.2.4.1 Mongodb参数设置

我们会通过同步组件设置mongodb数据源的相关参数，首先介绍一下mongdodb主要参数

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
| connectionsPerHost                           | int           | Mongodb客户端连接池为每个Mongodb集群节点保持的最大连接数     |
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

#### 5.2.4.2 Elasticsearch参数配置

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

1. 并行（本节介绍）

2. 并行和分布式分片机制相结合（基于分布式任务调度引擎实现，后续章节介绍）

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

如果不设置数据加工处理的相关机制，那么同步作业默认自动将mongodb的中表字段映射为es字段（自动忽略_id字段，因为elasticsearch的\_id字段是自动维护的），然后将数据导入elasticsearch。可以通过importBuilder组件全局设置数据映射关系，也可以通过Datarefactor接口进行记录级的数据转换处理，从而实现对同步数据进行映射、加工和处理，下面列出几种常用的处理类型：

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
| 忽略null值               | 支持     | -        | true是忽略null值存入elasticsearch，false是存入（默认值）importBuilder.setIgnoreNullValueField(true); |

全局数据处理配置：打tag，标识数据来源于jdk timer还是xxl-job

```java
importBuilder.addFieldValue("fromTag","jdk timer");  //jdk timer调度作业设置

importBuilder.addFieldValue("fromTag","xxl-jobr");  //xxl-job调度作业设置
```

记录级别的转换处理参考下面的代码。

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
					TestVO testVO1 = ObjectSerializable.toBean(testVO, TestVO.class);
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
```

#### 5.2.4.6 IP地理位置信息库配置

ip转换为地址位置信息还需要在application.properties文件中配置开源ip信息库geolite2：

```properties
# IP地理位置信息库配置
# 缓存配置
ip.cachesize = 2000000
# 库下载地址https://dev.maxmind.com/geoip/geoip2/geolite2/
# geoip数据库文件地址配置
ip.database = E:/workspace/hnai/terminal/geolite2/GeoLite2-City.mmdb
ip.asnDatabase = E:/workspace/hnai/terminal/geolite2/GeoLite2-ASN.mmdb
```

geolite2数据库文件会定期更新，因此需要定期到以下地址下载最新的geolite2数据库文件：

[https://dev.maxmind.com/geoip/geoip2/geolite2/](https://dev.maxmind.com/geoip/geoip2/geolite2/)

#### 5.2.4.7 同步作业结果回调处理函数设置

通过任务执行结果回调接口ExportResultHandler，可以非常方便地跟踪数据同步任务执行的情况和任务执行结果，ExportResultHandler提供了4个接口方法：

1. success方法：任务执行成功时调用，包含任务command对象（任务对应数据和任务信息，任务执行详细情况统计信息）和result（elasticsearch批量导入返回的response报文）两个参数
2. error方法：任务执行成功但是es有部分记录没有处理成功时调用，包含任务command对象（任务对应数据和任务信息，任务执行详细情况统计信息）和result（elasticsearch批量导入返回的response报文,包含错误记录信息）两个参数
3. exception方法：任务执行抛出异常时调用，包含任务command对象（任务对应数据和任务信息，任务执行详细情况统计信息）和exception两个参数
4. getMaxRetry方法：返回当任务执行出错重试次数，一般返回0或者-1即可

通过importBuilder组件的setExportResultHandler方法设置同步作业任务执行结果回调处理接口，这里只打印任务执行情况：

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

Elasticsearch索引文档id支持三种设置方式

1. Elasticsearch默认机制

2. 指定esIdField字段（5.2.4.3 导入elasticsearch参数配置中有介绍）

3. 自定义Elasticsearch索引文档id生成机制（本节介绍）

第2种和第3中只能设置一种，2，3都不设置就是第1种情况，下面介绍自定义Elasticsearch索引文档id生成机制，设置EsIdGenerator接口即可：

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




#### 5.2.4.9 增量字段信息设置

增量同步时必须指定增量同步字段，如果不指定那么将是全量同步；增量字段必须是可以递增的值，目前支持两种数据类型作为增量字段：数字和日期

| 增量类型 | 设置案例                                                     |
| -------- | ------------------------------------------------------------ |
| 数字     | importBuilder.setLastValueColumn("lastAccessedTime");importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE); // 指定类型importBuilder.setLastValue(-1); |
| 日期     | importBuilder.setLastValueColumn("lastAccessedTime");importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE); // 指定类型SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");                                                  try {                                                                                                                                                                                         Date date = format.parse("2000-01-01");   importBuilder.setLastValue(date);                                }catch (Exception e){   e.printStackTrace();} |

其他增量同步参数说明

| 参数名称                | 参数类型 | 参数说明                                                     |
| ----------------------- | -------- | ------------------------------------------------------------ |
| fromFirst               | boolean  | 作业进程重启时是否需要从头开始同步数据，true 从头开始，false从上次成功结束的值开始同步（默认值） |
| lastValueStorePath      | String   | 指定sqlite数据库文件地址，默认采用sqlite数据库来保存增量同步状态 |
| lastValueStoreTableName | String   | 记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab |
| config.db.name          | String   | 在application.properties文件中配置                                               保存增量数据同步状态的表对应的数据源名称，当采用分布式任务调度引擎时必须配置本参数，jdk timer和quartz调度机制不需要配置（默认采用sqlite保存增量同步状态）；可以指定一个已经有的数据源名称，比如数据库同步时的源数据源或者目标数据源名称，亦可以在application.properties文件中定义一个新的数据源，然后将新的数据源名称设置为config.db.name即可，例如： |

在application.properties文件中配置config.db数据源：

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

Mongodb session表对应的增量字段lastAccessedTime为long类型，是一个数字值，通过importBuilder设置增量同步参数的代码如下：

```java
        importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
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

最后通过importBuilder组件构建一个DataStream对象，并调用dataStream.execute方法执行同步作业：

```java
		/**
		 * 构建DataStream，执行mongodb数据到es的同步操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行同步操作
```

添加上述两行代码后，整个同步作业方法就编写完成了。接下来完成同步作业的配置工作。

#### 5.2.4.11 同步作业主程序配置

需要将编写的同步作业类配置到application.properties文件中：

```properties
#同步作业主程序配置
mainclass=org.frameworkset.elasticsearch.imp.Mongodb2ES
```



#### 5.2.4.13 完整的同步作业类和作业配置文件

完整的数据同步作业类：

```java
package org.frameworkset.elasticsearch.imp;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.runtime.CommonLauncher;
import org.frameworkset.session.TestVO;
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


public class Mongodb2ES {
	/**
	 * 启动运行同步作业主方法
	 * @param args
	 */
	public static void main(String[] args){

		Mongodb2ES dbdemo = new Mongodb2ES();
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
					TestVO testVO1 = ObjectSerializable.toBean(testVO, TestVO.class);
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
		importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
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
mainclass=org.frameworkset.elasticsearch.imp.Mongodb2ES

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

## 5.3 数据同步模式控制

### 5.3.1 全量/增量导入

根据实际需求，有些场景需要全量导入数据，有些场景下需要增量导入数据，以session数据同步案例作业来讲解具体的控制方法

- 增量同步时加上下面的代码

```java
        importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段

		importBuilder.setFromFirst(false);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//设置增量查询的起始值lastvalue
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date);
		}
		catch (Exception e){
			e.printStackTrace();
		}
```

- 全量同步时，去掉或者注释掉上面的代码

```java
        /**
		importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段

		importBuilder.setFromFirst(false);//任务重启时，重新开始采集数据，true 重新开始，false不重新开始，适合于每次全量导入数据的情况，如果是全量导入，可以先删除原来的索引数据
		importBuilder.setLastValueStorePath("mongodb_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
		//设置增量查询的起始值lastvalue
		try {
			Date date = format.parse("2000-01-01");
			importBuilder.setLastValue(date);
		}
		catch (Exception e){
			e.printStackTrace();
		}*/
```



### 5.3.2 一次性执行和周期定时执行

根据实际需求，有些场景作业启动后只需执行一次，有些场景需要周期性定时执行，以session数据同步案例作业来讲解具体的控制方法

- 定时执行

  支持jdk timer和quartz以及xxl-job三种定时执行机制，以jdk timer为例，加上以下代码即可
```java
        //定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
               //.setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
```

- 一次性执行
一次性执行只需要将上面的代码注释即可
```java
        /**   
        //定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
               //.setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		*/		
```
### 5.3.3 串行执行和并行执行

根据实际需求，有些场景作业采用串行模式执行，有些场景需要并行执行，以session数据同步案例作业来讲解具体的控制方法

- 并行执行

  并行执行，加上以下代码即可
```java
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//是否同步等待每批次任务执行完成后再返回调度程序，true 不等待所有导入作业任务结束，方法快速返回；false（默认值） 等待所有导入作业任务结束，所有作业结束后方法才返回

```

- 串行执行
串行执行只需要将上面的代码注释即可
```java
        /**   
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行
		importBuilder.setAsyn(false);//是否同步等待每批次任务执行完成后再返回调度程序，true 不等待所有导入作业任务结束，方法快速返回；false（默认值） 等待所有导入作业任务结束，所有作业结束后方法才返回
		*/		
```




## 5.4 调试并观察作业执行情况

作业开发和配置完成后，就可以在idea中调试和运行这个基于jdk timer调度的同步作业,并观察作业的执行效果。

分两个步骤调试作业

步骤一 准备工作：首先保证mongodb和elasticsearch、以及kibana是运行状态，同时启动好sessionweb应用

步骤二 调试同步作业

### 5.4.1 准备工作

#### 5.4.1.1 启动mongodb

![](https://esdoc.bbossgroups.com/images\mongodb\startmongodb.png)

#### 5.4.1.2 启动elasticsearch

![](https://esdoc.bbossgroups.com/images\mongodb\startelasticsearch.png)

#### 5.4.1.3 启动kibana

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

### 5.4.2 调试同步作业

在作业类中需要调试的代码处添加断点，然后启动调试程序即可：

![](https://esdoc.bbossgroups.com/images\mongodb\debugjob.png)
调试过滤记录功能
![](https://esdoc.bbossgroups.com/images\mongodb\debugjobfilter.png)

### 5.4.3 查看同步作业任务执行日志
第一次调度执行作业执行日志查看：第一次有10条数据进行同步单是被过滤掉一条日志
![](https://esdoc.bbossgroups.com/images\mongodb\debugjoblogs.png)

增量调度执行作业日志查看：
![](https://esdoc.bbossgroups.com/images\mongodb\debugincrtjoblogs.png)


## 5.5 同步作业参数提取/发布/部署

数据同步作业开发调试完毕后，就可以将同步作业发布成一个可以部署运行的作业包。可以将程序中硬编码的参数和参数值提取到配置文件application.properties中，参数提取完毕后再构建发布同步作业。

### 5.5.1 参数提取

在发布版本之前，可以将程序中硬编码的参数和参数值提取到配置文件application.properties中，例如：

1.mongodb相关参数（mongodb服务器地址、mongodb数据库和collection等）

2.线程池和线程队列数

3.增量状态起始值(lastValue)和增量状态保存路径（lastValueStorePath）等等

4.fetchSize和batchSize



首先在application.propterties添加以下参数：

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

参数提取出来后，作业程序需要通过工具类CommonLauncher获取配置文件中的参数，获取和设置参数代码如下:

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

### 5.5.2 发布作业

参数提取梳理完毕后，打包发布版本，下点击运行工程根目录下的release.bat指令即可：

![](https://esdoc.bbossgroups.com/images\mongodb\release.png)

命令行提示build successful，说明打包发布成功：

![](https://esdoc.bbossgroups.com/images\mongodb\releasesuccess.png)

构建生成的作业包所在目录：build\distributions

![](https://esdoc.bbossgroups.com/images\mongodb\releasezip.png)

### 5.5.3 运行和停止作业

将zip包分发到服务器解压即可，运行方法见图示：

![](https://esdoc.bbossgroups.com/images\mongodb\runjob.png)

###  5.5.4 运行效果
#### 5.5.4.1同步作业启动后可以查看同步日志文件es.log中的日志
同步作业启动后可以查看同步日志文件中的日志：es.log

![](https://esdoc.bbossgroups.com/images\mongodb\restartjob.png)

正式环境可以通过修改application.properties的配置来关闭dsl调试功能：

```properties
elasticsearch.showTemplate=false
```

#### 5.5.4.2 同步前后数据对比

在sessionmonitor提供的监控界面查看mongodb中sessionid为c020296e-4f9b-4509-b482-b44c88a913af对应的session数据
![在这里插入图片描述](https://images.gitbook.cn/796c1bc0-171d-11ea-988c-fdda706d8b74)

在mongodb中查看sessionid为c020296e-4f9b-4509-b482-b44c88a913af对应的session数据

```json
{
    "_id": "5de6165e162d7a290d59c3b8",
    "sessionid": "c020296e-4f9b-4509-b482-b44c88a913af",
    "creationTime": 1575360094061,
    "maxInactiveInterval": 3600000,
    "lastAccessedTime": 1575360781659,
    "_validate": true,
    "appKey": "sessionmonitor",
    "referip": "127.0.0.1",
    "host": "169.254.252.194-DESKTOP-U3V5C85",
    "requesturi": "http://localhost:9090/sessionmonitor/",
    "lastAccessedUrl": "http://localhost:9090/sessionmonitor/session/sessionManager/viewSessionInfo.page?sessionid=c020296e-4f9b-4509-b482-b44c88a913af&appkey=sessionmonitor",
    "httpOnly": true,
    "secure": false,
    "lastAccessedHostIP": "169.254.252.194-DESKTOP-U3V5C85",
    "local": "<ps><p n=\"_dflt_\" mg=\"1\"><![CDATA[en]]></p></ps>",
    "testVO": "<ps><p n=\"_dflt_\" cs=\"org.frameworkset.session.TestVO\"><p n=\"id\" s:t=\"String\"><![CDATA[testvoidaaaaa,sessionmonitor modifiy id]]></p><p n=\"testVO1\" cs=\"org.frameworkset.session.TestVO1\"><p n=\"name\" s:t=\"String\"><![CDATA[hello,sessionmoitor test vo1]]></p></p></p></ps>",
    "privateAttr": "<ps><p n=\"_dflt_\" s:t=\"String\"><![CDATA[this sessionmonitor's private attribute.]]></p></ps>",
    "userAccount": "<ps><p n=\"_dflt_\" s:t=\"String\"><![CDATA[sessionmonitor 张三]]></p></ps>",
    "shardNo": "<ps><p n=\"_dflt_\" s:t=\"Integer\" v=\"1\"/></ps>"
}
```

在kibana中查看同步到elasticsearch中sessionid为c020296e-4f9b-4509-b482-b44c88a913af的session数据：  

```json
{
  "_index": "mongodbdemo",
  "_type": "mongodbdemo",
  "_id": "5de6165e162d7a290d59c3b8",
  "_score": 1,
  "_source": {
    "extfiled2": 2,
    "extfiled": 1,
    "shardNo": 1,
    "userAccount": "sessionmonitor 张三",
    "testVO": {
      "id": "testvoidaaaaa,sessionmonitor modifiy id",
      "testVO1": {
        "name": "hello,sessionmoitor test vo1"
      }
    },
    "privateAttr": "this sessionmonitor's private attribute.",
    "local": "en",
    "lastAccessedTime": "2019-12-03T08:13:01.659Z",
    "creationTime": "2019-12-03T08:01:34.061Z",
    "ipInfo": {
      "country": "未知",
      "countryId": "未知",
      "area": "",
      "areaId": "",
      "region": "未知",
      "regionId": "未知",
      "city": "未知",
      "cityId": "未知",
      "county": "未知",
      "countyId": "未知",
      "isp": "未知",
      "ispId": null,
      "ip": "127.0.0.1",
      "geoPoint": null
    },
    "fromTag": "jdk timer",
    "sessionid": "c020296e-4f9b-4509-b482-b44c88a913af",
    "maxInactiveInterval": 3600000,
    "_validate": true,
    "appKey": "sessionmonitor",
    "referip": "127.0.0.1",
    "host": "169.254.252.194-DESKTOP-U3V5C85",
    "requesturi": "http://localhost:9090/sessionmonitor/",
    "lastAccessedUrl": "http://localhost:9090/sessionmonitor/session/sessionManager/viewSessionInfo.page?sessionid=c020296e-4f9b-4509-b482-b44c88a913af&appkey=sessionmonitor",
    "httpOnly": true,
    "secure": false,
    "lastAccessedHostIP": "169.254.252.194-DESKTOP-U3V5C85"
  }
}
```
从Elasticsearch里面保存的数据可以发现，我们对导入的数据做了如下加工处理:

1. 通过DataRefactor添加的数据字段：extfiled1，extfiled2，ipInfo（根据referip中保存的客户端ip，调用地理位置服务转换生成，示例是本机127.0.0.1，不是公网ip，所以没有获取到对应的信息）
2. 通过DataRefactor 将xml报文转换为原始数据的字段：shardNo，userAccount，testVO，privateAttr，local
3. 通过importBuilder组件添加的全局tag字段:fromTag
4. mongodb中long类型的两个字段lastAccessedTime和creationTime已经被转换为Elasticsearch的日期Date类型

#### 5.5.4.3 通过kibana检索session数据

可以在kibana discover界面中按照条件检索同步到Elasticsearch中的session数据
![](https://esdoc.bbossgroups.com/images\mongodb\kibanasessiondatas.png)

### 5.5.5 调优：作业jvm内存和并行线程队列调整

根据服务器资源和作业运行情况，可以适当调整jvm内存，修改jvm.options,设置作业运行需要的jvm内存，按照比例调整Xmx和MaxNewSize参数：

```properties
-Xms1g
-Xmx1g
-XX:NewSize=512m
-XX:MaxNewSize=512m
```

Xms和Xmx保持一样，NewSize和MaxNewSize保持一样，Xmx和MaxNewSize大小保持的比例可以为3:1或者2:1

亦可以通过调整作业并行参数、批量读取和写入数据大小参数，来充分利用同步服务器资源和Elasticsearch能力，以提升数据同步速度。

```java
importBuilder.setFetchSize(1000); //设置从数据源分批读取数据记录数大小
importBuilder.setBatchSize(1000); // 设置批量写入数据记录数大小
importBuilder.setQueue(100);//设置批量导入线程池等待队列长度
importBuilder.setThreadCount(20);//设置批量导入线程池工作线程数量
```



## 5.6 基于xxl-job的数据同步作业 

### 5.6.1 xxl-job介绍
xxl-job是一个不错的开源分布式作业调度引擎，大致的原理如下：

![image-20191204104127272](https://esdoc.bbossgroups.com/images\mongodb\xxl-job.png)

运行xxl-job同步作业时依赖xxl-job组件，需要在build.gradle文件导入xx-job依赖包：

```groovy
compile 'com.xuxueli:xxl-job-core:2.1.1'
```

### 5.6.2 编写xxl-job作业

基于xxl-job的数据同步作业类需要继承抽象类AbstractXXLJobHandler，实现抽象方法init，参数配置和数据转换处理与jdk timer一样，采用分布式调度引擎xxl-job调度作业，因此不要在代码中添加jdk timer的定时配置，；实现AbstractXXLJobHandler的抽象方法init，在其中实例化一个externalScheduler组件，并设置DataStreamBuilder接口到externalScheduler中；基于xxl-job的节点分片号来以实现分布式分片数据同步功能。

```java
org.frameworkset.tran.schedule.xxjob.AbstractXXLJobHandler
```

将分片号作为mongodb数据查询条件:

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
			// 采用xml序列化组件将index序列化为一个xml报文，将该报文作为检索条件进行检索
            // （因为session中的数据都是采用xml报文进行存储）
			String idxStr = ObjectSerializable.toXML(index);
			query.append("shardNo",idxStr );
```

xxl-job的分片调度处理机制（下图来源于xxl-job官方文档）：

![image-20191204102007642](https://esdoc.bbossgroups.com/images\mongodb\xxl_jobshard.png)

完整的xxl-job同步作业类XXJobMongodb2ESImportTask：

```java
package org.frameworkset.elasticsearch.imp;


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
                // 采用xml序列化组件将index序列化为一个xml报文，将该报文作为检索条件进行检索
                // （因为session中的数据都是采用xml报文进行存储）
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
			importBuilder.setLastValueColumn("lastAccessedTime");//手动指定数字增量查询字段
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
xxl.job.executor.logretentiondays=10
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

# 配置mongodb-elasticsearch作业程序，xxl-job作业中需要使用XXJobMongodb2ESImportTask来注册作业任务
xxl.job.task.XXJobMongodb2ESImportTask = org.frameworkset.elasticsearch.imp.XXJobMongodb2ESImportTask
## xxl.job.task.otherTask = org.frameworkset.elasticsearch.imp.jobhandler.OtherTask

#配置运行xxl-job作业主程序，由同步框架提供不需要自己编写
mainclass=org.frameworkset.tran.schedule.xxjob.XXJobApplication
```

### 5.6.3 增量同步状态外部数据源配置

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



### 5.6.4 调试xxl-job调度作业（分片同步数据机制）、观察作业执行情况

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

为了查看分片数据同步效果，需要在idea中同时启动运行两个XXLJobApplicationTest作业进程，先在9993端口启动一个XXLJobApplicationTest,启动成功后，修改application.properties文件中的xxl.job.executor.port为端口9994，然后再启动一个XXLJobApplicationTest进程

```
# 作业执行服务器ip
xxl.job.executor.ip=192.168.137.1
# 作业执行服务器端口
xxl.job.executor.port=9994
```

两个进程对应的分片号分别为0和1，然后打开多个浏览器，访问session应用，将会随机生成shardNo属性值为0和1的session记录数据，就可以观察到两个数据同步作业会分别同步shardNo属性值为0和1的session记录数据。

xxl-job作业启动和运行其实只是启动了xxl-job的executor节点，并不会启动同步作业程序，需要在xxl-job-admin中添加xxl-job executor和对应的作业调度任务，然后在控制台启动作业任务，才能正式在executor中调度和执行同步作业程序：

1. xxl-job-admin中添加executor


![](https://esdoc.bbossgroups.com/images\mongodb\xxlnewexecutor.png)

其中的AppName必须与application.properties文件中配置项值保持一致

```properties
xxl.job.executor.appname=mongodb-elasticsearch-xxjob
```

2. xxl-job-admin中添加作业调度任务

![](https://esdoc.bbossgroups.com/images\mongodb\xxlnewtask.png)

3. xxl-job控制台启动作业调度任务

![](https://esdoc.bbossgroups.com/images\mongodb\xxltaskschedule.png)

任务详细执行情况和日志与jdk timer调度执行类似，也可以登录xxl-job管理界面查看作业执行情况、控制和启动作业，也可以像jdk timer作业一样，通过restart指令重启作业、stop指令停止作业。

下面给出正在运行的两个发布后的xxl-job作业窗口

![](https://esdoc.bbossgroups.com/images\mongodb\xxljob1.png)

![](https://esdoc.bbossgroups.com/images\mongodb\xxljob2.png)



同步作业参数提取/发布/部署/jvm配置与jdk timer类似，这里也不做过多介绍。更多的内容可以参考文档：

[基于xxjob-同步db-elasticsearch数据](https://esdoc.bbossgroups.com/#/db-es-tool?id=_26-基于xxjob-同步db-elasticsearch数据)

## 5.7 集成同步功能到自己的项目中（代码和maven坐标）

前面介绍了独立调度执行同步作业开发调试、构建发布和运行方法，我们也可以将作业方法里面的代码整合到自己的项目中运行，需要做到如下几点即可：

1.将application.properties文件放入项目resources目录（在里面配置es和http连接池相关参数）

2.将mongodb-elasticsearch同步工具包的maven坐标导入的项目：

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-mongodb</artifactId>
            <version>6.1.1</version>
        </dependency>
```

3.增量导入，需要导入sqlite驱动

```xml
<dependency>
      <groupId>org.xerial</groupId>
      <artifactId>sqlite-jdbc</artifactId>
      <version>3.23.1</version>
      <scope>compile</scope>
 </dependency>
```

4.如果使用xxl-job调度作业，需导入xxl-job的maven坐标


```xml
        <dependency>
            <groupId>com.xuxueli</groupId>
            <artifactId>xxl-job-core</artifactId>
            <version>2.1.1</version>
        </dependency>
```

5.如果是quartz调入作业，需导入maven坐标

```xml
        <dependency>
            <groupId>com.bbossgroups</groupId>
            <artifactId>bboss-schedule</artifactId>
            <version>5.7.0</version>
        </dependency>
```

6.如果是spring boot项目，导入一个maven坐标：

```xml
       <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-spring-boot-starter</artifactId>
            <version>6.1.1</version>
        </dependency>
```



# 6.总结

本文结合session数据增量同步案例，讲解了mongodb到elasticsearch数据同步功能，总结如下：

1.mongodb-elasticsearch数据同步功能（数据转换、数据过滤等）

2.增量并行数据同步功能

2.基于现有的同步作业工程搭建作业开发环境

3.在idea中开发和调试基于jdk timer调度同步作业

4.在idea中开发和调试xxl-job分布式调度同步作业

5.基于xxl-job分布式分片数据同步功能

6.如何配置、构建、发布和运行同步作业，查看作业运行日志和数据同步效果

7.如何调整jvm内存和作业并行机制，提升数据同步速度和数据处理性能

由于篇幅关系部分内容没有深入介绍，更多的内容可以参考文档：

[基于xxjob-同步db-elasticsearch数据](https://esdoc.bbossgroups.com/#/db-es-tool?id=_26-基于xxjob-同步db-elasticsearch数据)



# 7.参考文档

## 7.1 Session共享应用部署和使用文档

[https://my.oschina.net/bboss/blog/758871](https://my.oschina.net/bboss/blog/758871)

## 7.2 Elasticsearch java highlevel rest client bboss 

[https://esdoc.bbossgroups.com/](https://esdoc.bbossgroups.com/)

## 7.3 xxl-job官方文档和源码地址

[https://github.com/xuxueli/xxl-job](https://github.com/xuxueli/xxl-job)

## 7.4 相关文档

- [数据库和Elasticsearch同步工具](https://esdoc.bbossgroups.com/#/db-es-tool)
- [Spring boot与数据同步工具应用](https://esdoc.bbossgroups.com/#/usedatatran-in-spring-boot)
- [HBase-Elasticsearch同步工具](https://esdoc.bbossgroups.com/#/hbase-elasticsearch)
- [Database-Database数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_5-database-database数据同步使用方法)
- [Kafka1x-Elasticsearch数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_6-kafka1x-elasticsearch数据同步使用方法)
- [Kafka2x-Elasticsearch数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_7-kafka2x-elasticsearch数据同步使用方法)
- [Elasticsearch-Elasticsearch数据同步使用方法](https://esdoc.bbossgroups.com/#/db-es-tool?id=_8-elasticsearch-elasticsearch数据同步使用方法)

