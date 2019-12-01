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

mongodb-elasticsearch另一个显著的特色就是直接基于java语言来编写数据同步作业程序，基于强大的java语言和第三方工具包，能够非常方便地加工和处理需要同步的源数据，然后将最终的数据保存到目标库（Elasticsearch或者数据库）；同时也可以非常方便地在idea或者eclipse中调试和运行同步作业程序，调试无误后，通过mongodb-elasticsearch提供的gradle脚本，即可构建和发布出可部署到生产环境的同步作业包。因此，对广大的java程序员来说，mongodb-elasticsearch无疑是一个轻易快速上手的数据同步利器。

​	下面我们通过一个案例来介绍mongodb-elasticsearch的使用方法，你会发现整个过程下来，开发一个同步作业，其实就是在用大家熟悉的方式做一个简单的开发编程的事情。

# 2.同步案例介绍-session数据同步

同步保存在mongodb中的session数据到Elasticsearch场景比较简单，采用web应用session最后访问时间作为增量同步字段，将保存在mongodb中的session数据定时增量同步到Elasitcsearch中。

我们在idea中开发和调试数据同步作业，利用gradle构建和发布同步作业包，运行作业，然后启动一个往mongodb中写入session数据的web应用，打开多个浏览器访问web应用，产生和修改session数据，然后观察同步作业的同步效果，演示两种调度机制效果：
- 基于jdk timer

- 基于xxl-job来调度作业

  下面结合session数据同步案例，正式切入本文主题。

# 3.环境准备

**开发环境**

在windows环境开发和调试同步作业程序，需要在电脑上安装以下软件

- jdk 1.8或以上
- idea 2019
- gradle最新版本  https://gradle.org/releases/ 
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

详细gradle安装和配置参考文档： https://esdoc.bbossgroups.com/#/bboss-build 

​		**运行环境**

​      jdk1.8即可

# 4.同步作业开发环境搭建

我们无需从0开始搭建开发环境，可以到以下地址下载已经配置好的Mongodb-Elasticsearch开发环境：

 https://github.com/bbossgroups/mongodb-elasticsearch 

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

另外一个配置文件就是：

src/main/resources/application.properties

这个文件是同步作业的主配置文件，包括es和mongodb的相关参数都可以这这个里面配置

到此数据同步作业工程已经导入idea，接下来进入同步作业实现、调试开发环节。

# 5.同步作业程序开发和调试

## 5.1 案例说明

同步mongodb中的session数据到Elasticsearch场景比较简单，采用web应用session最后访问时间作为增量同步字段，将保存在mongodb中的session数据定时增量同步到Elasitcsearch中。

我们在idea中开发和调试数据同步作业，利用gradle构建和发布同步作业包，运行作业，然后启动一个往mongodb中写入session数据的web应用，打开多个浏览器访问web应用，产生和修改session数据，然后观察同步作业的同步效果，演示两种调度机制效果：

- 基于jdk timer

- 基于xxl-job来调度作业

  

jdk timer调度作业对应的mongodb session表结构和elasticsearch索引表结构映射关系如下：（首先以默认的索引结构，然后可以自定义索引结构或者索引模板）

mongodb对应的db：sessiondb

mongodb对应的dbcollection：sessionmonitor_sessions

elasticsearch 索引名称：mongodbdemo 索引类型：mongodbdemo

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

#### 5.2.4.2 通过importBuilder设置mongodb参数：

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

#### 5.2.4.3 导入elasticsearch参数配置

导入elasticsearch参数配置（索引名称和索引类型、按日期动态索引名称），首先介绍相关参数

| 参数名称                      | 参数类型 | 参数说明                                                     |
| ----------------------------- | -------- | ------------------------------------------------------------ |
| index                         | String   | 索引名称，支持固定的索引名称和动态索引名称，动态索引名称命名规范如下：demowithesindex-{dateformat=yyyy.MM.dd}  按照日期滚动索引名称，日期格式根据自己的需要指定即可                                                                                                     indexname-{field=fieldName} 按照字段值来动态设置索引名称                            dbclobdemo-{agentStarttime,yyyy.MM.dd} 按照字段值来动态设置索引名称,如果字段对应的值是个日期类型，可以指定日期类型的格式，本案例为：mongodbdemo |
| indexType                     | String   | 索引类型，es 7以后的版本不需要设置indexType（或者直接设置为_doc），es7以前的版本必需设置indexType，可以动态指定indexType,例如：索引类型为typeFieldName字段对应的值，{field=typeFieldName}或者{typeFieldName}，本案例直接指定为：mongodbdemo |
| batchSize                     | int      | 批量导入elasticsearch的记录大小                              |
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
				.setContinueOnError(true); // 忽略任务执行异常，任务执行过程抛出异常不中断任务执行
```

####  5.2.4.4 jdk timer定时任务时间配置

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

#### 5.2.4.5 并行任务配置

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

#### 5.2.4.6 数据加工处理

可以非常方便地对同步数据进行映射、加工和处理，下面列出几种常用的处理类型：

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

本案例通过全局方式添加数据分片号到elasticsearch的session表中：

importBuilder.addFieldValue("shardNo",0);     

其他的的数据处理转换都是记录级别的。

session数据转换处理的代码如下（可根据上表中的数据处理类型，自行实现自己的转换处理功能）：

```java
        // 全局记录配置：添加数据分片号到elasticsearch的session记录中
        importBuilder.addFieldValue("shardNo",0);
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

设置同步作业结果回调处理函数

关键参数配置：jvm内存）





默认自动进行映射导入elasticsearch、通过datarefactor修改默认关系

调试jdk-timer调度作业、观察作业执行情况和日志

新建一个基于xxl-job的数据同步作业类：定义main方法和同步方法（参数配置和数据转换处理与jdk timer一样，只是任务调度采用外部分布式调度引擎xxl-job）

调试xxl-job调度作业（分片同步数据机制）、观察作业执行情况和日志

配置和发布作业/提取参数到配置文件中

集成同步功能到自己的项目中（代码和maven坐标）



# 6.同步作业发布和部署



# 7.总结

增量状态管理

默认采用sqlite管理增量同步状态，如果针对xxl-job分布式调度器需要采用外部数据库管理增量状态

