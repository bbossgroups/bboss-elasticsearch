# 快速集成Elasticsearch Restful API案例分享

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

*The best elasticsearch highlevel java rest api-----[bboss](README.md)*

快速集成Elasticsearch Restful API案例分享,*本案例代码可用于非spring项目和spring项目，兼容spring boot 1.x,2.x,兼容Elasticserch 1.x,2.x,5.x,6.x,7.x,以及后续版本。*

本文中讲述的方法同样适用于其他xxx boot类型项目集成bboss es。

# 从源码构建Elasticsearch BBoss

First Get source code from https://github.com/bbossgroups/bboss-elasticsearch

Then change to cmd window under directory bboss-elasticsearch and run gradle build command：

```
gradle publishToMavenLocal
```

Gradle environmenet install and config document: https://esdoc.bbossgroups.com/#/bboss-build



# 1.导入bboss elasticsearch



## maven工程

基于maven开发的工程，在pom.xml文件中导入以下maven坐标

maven坐标

```xml
        <dependency>
            <groupId>com.bbossgroups.plugins</groupId>
            <artifactId>bboss-elasticsearch-rest-jdbc</artifactId>
            <version>6.9.8</version>
        </dependency>
```



## gradle工程

基于gradle开发的工程，在build.gradle文件中导入以下gradle坐标

gradle坐标

```groovy
compile "com.bbossgroups.plugins:bboss-elasticsearch-rest-jdbc:6.9.8"
```



# 2.配置bboss elasticsearch

**极简配置，接近零配置集成bboss，**默认情况下，如果就是本机的elasticsearch服务器，导入bboss后不需要做任何配置就可以通过bboss rest api访问和操作elasticsearch。

bboss会在classpath下面查找并加载配置文件application.properties，文件不存在会忽略，那么就使用默认elasticsearch地址：

**127.0.0.1:9200**

bboss支持单集群配置和多集群配置。获取操作单集群clientinferface组件方法，获取操作多集群对应集群clientinferface组件方法，请参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》章节：【3 bboss es操作和访问elasticsearch模式】。



## 2.1 单集群配置

单个集群配置极简单，修改项目的application.properties文件，只需要加入以下内容即可：

```
elasticsearch.rest.hostNames=10.21.20.168:9200
## 集群地址用逗号分隔
#elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
```

如果需要更多的配置，可以将以下内容复制到项目的application.properties文件中：

```
#x-pack认证账号和口令
elasticUser=elastic
elasticPassword=changeme

#es服务器地址配置
elasticsearch.rest.hostNames=127.0.0.1:9200
#elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282

#动态索引表名称日期格式配置
elasticsearch.dateFormat=yyyy.MM.dd

elasticsearch.timeZone=Asia/Shanghai
elasticsearch.ttl=2d

#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
elasticsearch.showTemplate=true

#客户端动态发现es集群节点控制开关
elasticsearch.discoverHost=false

#http链接池配置
http.timeoutConnection = 50000
http.timeoutSocket = 50000
http.connectionRequestTimeout=50000
http.retryTime = 1
http.maxLineLength = -1
http.maxHeaderCount = 200
http.maxTotal = 400
http.defaultMaxPerRoute = 200

# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
dslfile.refreshInterval = -1
```

这些配置的含义，可以参考文档：《[高性能elasticsearch ORM开发库使用介绍](development.md)》章节2进行了解。

***其他各种boot框架配置的时候，也可自行创建application.properties配置文件，在其中配置需要的参数。***



## ***2.2多集群配置***

**通过**elasticsearch.serverNames = defualt,logs指定集群应用名称，名称作为es和http连接池的配置属性名的前缀即可：default是缺省前缀名称，可以忽略

```
##多集群配置
elasticsearch.serverNames = default,logs

##default集群配配置
default.elasticUser=elastic
default.elasticPassword=changeme

#elasticsearch.rest.hostNames=10.1.236.88:9200
default.elasticsearch.rest.hostNames=127.0.0.1:9200
#elasticsearch.rest.hostNames=10.21.20.168:9200
#elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
default.elasticsearch.dateFormat=yyyy.MM.dd
default.elasticsearch.timeZone=Asia/Shanghai
default.elasticsearch.ttl=2d
#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
default.elasticsearch.showTemplate=true
default.elasticsearch.discoverHost=false

##default连接池配置
default.http.timeoutConnection = 50000
default.http.timeoutSocket = 50000
default.http.connectionRequestTimeout=50000
default.http.retryTime = 1
default.http.maxLineLength = -1
default.http.maxHeaderCount = 200
default.http.maxTotal = 400
default.http.defaultMaxPerRoute = 200
default.http.keystore =
default.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
default.http.hostnameVerifier =

##logs集群配置
logs.elasticUser=elastic
logs.elasticPassword=changeme

#elasticsearch.rest.hostNames=10.1.236.88:9200
logs.elasticsearch.rest.hostNames=127.0.0.1:9200
#elasticsearch.rest.hostNames=10.21.20.168:9200
#elasticsearch.rest.hostNames=10.180.211.27:9280,10.180.211.27:9281,10.180.211.27:9282
logs.elasticsearch.dateFormat=yyyy.MM.dd
logs.elasticsearch.timeZone=Asia/Shanghai
logs.elasticsearch.ttl=2d
#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别
logs.elasticsearch.showTemplate=true
logs.elasticsearch.discoverHost=false

##logs集群对应的连接池配置
logs.http.timeoutConnection = 400000
logs.http.timeoutSocket = 400000
logs.http.connectionRequestTimeout=400000
logs.http.retryTime = 1
logs.http.maxLineLength = -1
logs.http.maxHeaderCount = 200
logs.http.maxTotal = 400
logs.http.defaultMaxPerRoute = 200
# https证书配置
logs.http.keystore =
logs.http.keyPassword =
# ssl 主机名称校验，是否采用default配置，
# 如果指定为default，就采用DefaultHostnameVerifier,否则采用 SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
logs.http.hostnameVerifier =
```



## 2.3 获取指定集群组件方法

```
//没有dsl配置文件
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil("logs");//指定集群名称logs
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();//默认组件方法
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil("default");//默认组件方法

//有dsl配置文件
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("logs",configFile);//指定集群名称logs
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(configFile);//默认组件方法
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("default",configFile);//默认组件方法
```



# 3.验证集成是否成功

完成前面两步工作后，就可以通过以下代码验证集成是否成功，如果正确打印elasticssearch集群状态，那说明集成成功：

```
        //创建es客户端工具，验证环境
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		//验证环境,获取es状态
		String response = clientUtil.executeHttp("_cluster/state?pretty",ClientInterface.HTTP_GET);
        System.out.println(response);
```



# 3.完整的demo实例工程

<https://github.com/bbossgroups/eshelloword-booter>


# 4.参考文档

[开发指南](development.md)



# 5 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



