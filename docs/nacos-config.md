# Nacos对接与配置 (非spring boot项目)
本文介绍在非spring boot项目中，bboss与Nacos对接与配置方法。

## 1.elasticsearch客户端与Nacos对接

案例工程：

https://gitee.com/bboss/elasticsearch-example-nacos

### 1.1 添加Nacos和bbossEs相关依赖

maven项目pom.xml添加Nacos和bbossEs相关依赖
```xml
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-datatran-jdbc</artifactId>
    <version>7.3.1</version>
     <!--排除bboss-elasticsearch-rest-booter包-->
    <exclusions>
        <exclusion>
            <artifactId>bboss-elasticsearch-rest-booter</artifactId>
            <groupId>com.bbossgroups.plugins</groupId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-plugin-nacos</artifactId>
    <version>6.3.6</version>
</dependency>
```

 注意：一定要排除bboss-elasticsearch-rest-booter包

### 1.2 增加elasticsearch-boot-config.xml
在resources/conf下新增文件[elasticsearch-boot-config.xml](https://gitee.com/bboss/elasticsearch-example-nacos/blob/main/src/main/resources/conf/elasticsearch-boot-config.xml)，内容如下：

```xml
<properties>
   <config nacosNamespace="test" dataId="esclient"
            serverAddr="localhost:8848"
            group="DEFAULT_GROUP"
            timeOut="5000"
            remote-first="false"
            configChangeListener="org.frameworkset.nacos.ESNodeChangeListener"/>
 </properties>
```


### 1.5 Nacos配置
命名空间：test

es服务器的相关信息，那么就可以创建一个名为test的namespace，dataId为esclient，group为DEFAULT_GROUP，主要配置信息如下：
```properties
elasticsearch.rest.hostNames = ip:9200

## 自动按日期时间分表时指定日期格式，例如 按照下面格式生成的索引名称示例：indexname-2000.03.05
elasticsearch.dateFormat = yyyy.MM.dd
## 自动按日期时间分表时指定日期格式时区
elasticsearch.timeZone = Asia/Shanghai


#在控制台输出脚本调试开关showTemplate,false关闭，true打开，同时log4j至少是info级别

elasticsearch.showTemplate = true

elasticsearch.discoverHost = false

# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制

dslfile.refreshInterval = -1

##es client http连接池配置

http.timeoutConnection = 50000
http.timeoutSocket = 50000
http.connectionRequestTimeout=50000
http.retryTime = 3
http.automaticRetriesDisabled= false
#http.staleConnectionCheckEnabled=true
http.validateAfterInactivity=2000
http.evictExpiredConnections=false
http.timeToLive = 3600000
http.maxHeaderCount = 200
http.maxTotal = 600
http.defaultMaxPerRoute = 200

http.soReuseAddress = false

http.soKeepAlive = false


http.keepAlive = 3600000
```
![image-20200802133509704](images\nacos-config.jpg)

### 1.6 完成上述操作之后，就可以正常使用bbosses的api了

api使用案例：

https://gitee.com/bboss/elasticsearch-example-nacos/blob/main/src/test/java/org/bboss/elasticsearchtest/crud/DocumentCRUD7Test.java

## 2.bboss中使用nacos管理属性的其他案例
###  2.1 IOC与nacos集成并管理属性案例

https://gitee.com/bboss/bboss-plugins/blob/master/bboss-plugin-nacos/src/test/resources/redis.xml

```xml
<properties>
    <config nacosNamespace="test" dataId="redisconf"
            serverAddr="localhost:8848"
            group="DEFAULT_GROUP"
            timeOut="5000"
            remote-first="false"
            configChangeListener="org.frameworkset.nacos.ESNodeChangeListener"/>
    <property name="default" class="org.frameworkset.nosql.redis.RedisDB">
        <!-- redis.servers为apollo中配置的属性 -->
        <property name="servers">
            ${redis.servers}
        </property>
        <!-- single|cluster -->
        <property name="mode" value="cluster" />
        <property name="auth" value="${redis.auth:}" />
        <property name="poolMaxTotal" value="${redis.poolMaxTotal:10}"/>
        <property name="poolMaxWaitMillis" value="${redis.poolMaxWaitMillis:2000}"/>
    </property>
</properties>
```
config元素支持的属性说明

nacosNamespace指定nacos namespace，多个用逗号分隔

serverAddr: nacos配置中心地址

dataId: 配置数据id

group：数据组

timeOut：超时时间

remote-first: 远程优先

changeReload   指定是否热加载修改后的属性，true 加载， false不加载，热加载时IOC中的属性和组件都会重新初始化

configChangeListener 指定自己的apollo值变化监听器，继承父类：

```
org.frameworkset.nacos.PropertiesChangeListener
```



### 2.2 在代码中直接加载nacos中的配置

https://gitee.com/bboss/bboss-plugins/blob/master/bboss-plugin-nacos/src/test/java/org/frameworkset/apollo/NacosIOCTest.java

```java
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

 
public class NacosPropertiesFilePluginTest {
	private static Logger logger = LoggerFactory.getLogger(NacosPropertiesFilePluginTest.class);
	PropertiesContainer propertiesContainer ;
	@Before
	public void init(){
		propertiesContainer = new PropertiesContainer();
        /**
         * nacosNamespace="application" 
         *             serverAddr="localhost:8848" 
         *             dataId="redis" 
         *             group="DEFAULT_GROUP" 
         *             timeOut="5000" 
         *             changeReload="false"
         */
        Map<String,String> config = new HashMap<>();
        config.put("remote-first","false");

        config.put("auto-refresh","true");
        config.put("max-retry","10");
        config.put("username","nacos");
        config.put("password","nacos");
		propertiesContainer.addConfigPropertiesFromNacos("test","localhost:8848","dbinfo","DEFAULT_GROUP" ,5000L,"org.frameworkset.apollo.DemoTestListener",config);
		propertiesContainer.afterLoaded(propertiesContainer);
	}
	@Test
	public void test(){
		dbinfo("");
		dbinfo("ecop.");
		while(true){
			try {
//				synchronized (this) {
					Thread.sleep(1000l);
//				}
			}
			catch (Exception e){

			}
		}

	}

	private void dbinfo(String dbname){
		String dbName  = propertiesContainer.getProperty(dbname+"db.name");
		String dbUser  = propertiesContainer.getProperty(dbname+"db.user");
		String dbPassword  = propertiesContainer.getProperty(dbname+"db.password");
		String dbDriver  = propertiesContainer.getProperty(dbname+"db.driver");
		String dbUrl  = propertiesContainer.getProperty(dbname+"db.url");

		String showsql  = propertiesContainer.getProperty(dbname+"db.showsql");
		String validateSQL  = propertiesContainer.getProperty(dbname+"db.validateSQL");
		String dbInfoEncryptClass = propertiesContainer.getProperty(dbname+"db.dbInfoEncryptClass");
		System.out.println("dbName:"+dbName);
		System.out.println("dbUser:"+dbUser);
		System.out.println("dbPassword:"+dbPassword);
		System.out.println("dbDriver:"+dbDriver);
		System.out.println("dbUrl:"+dbUrl);
		System.out.println("showsql:"+showsql);
		System.out.println("validateSQL:"+validateSQL);
		System.out.println("dbInfoEncryptClass:"+dbInfoEncryptClass);
	}
}
```
### 2.3 http proxy中加载nacos中配置案例
bboss http proxy是一个轻量级的java http客户端负载均衡器，对应的配置可以通过nacos进行配置管理，同时亦可以通过nacos实现服务节点自动发现功能，
参考文档：https://esdoc.bbossgroups.com/#/httpproxy?id=_4%ef%bc%89%e5%8a%a0%e8%bd%bdnacos%e9%85%8d%e7%bd%ae%e5%90%af%e5%8a%a8httpproxy


## 3.基于nacos配置中心案例工程

maven工程-elasticsearch java client案例

https://github.com/bbossgroups/elasticsearch-example-nacos 

 

maven工程-http proxy案例

https://github.com/bbossgroups/httpproxy-nacos 



 db数据源管理案例



https://github.com/bbossgroups/db-db-job3-nacos

## 4.参考文档

[Spring boot整合Elasticsearch](https://esdoc.bbossgroups.com/#/spring-booter-with-bboss?id=spring-boot整合elasticsearch案例分享)

[httpproxy使用文档](https://esdoc.bbossgroups.com/#/httpproxy)