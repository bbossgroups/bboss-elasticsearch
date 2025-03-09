# 基于Kerberos认证对接华为云Elasticsearch

可以通过华为官方提供的Elasticsearch Java客户端（基于Elasticsearch官方版本改造），实现基于Kerberos认证访问和操作华为云Elasticsearch；亦可以使用更加通用的开源Elasticsearch Java客户端bboss，实现[基于Kerberos认证访问和操作华为云Elasticsearch](https://esdoc.bbossgroups.com/#/development?id=_212-kerberos%E8%AE%A4%E8%AF%81%E9%85%8D%E7%BD%AE)。

本文介绍使用bboss实现基于Kerberos认证访问和操作华为云Elasticsearch的方法。
## 1. bboss介绍  
bboss是一个高性能高兼容性的Elasticsearch java客户端框架：

![](images\client-Elasticsearch.png)

更多bboss介绍，可以访问文档了解：[https://esdoc.bbossgroups.com/#/README](https://esdoc.bbossgroups.com/#/README)

## 2. 准备工作
### 2.1 准备Kerberos认证配置文件
获取Kerberos配置文件 ：从华为云获取 krb5.conf 和 jaas.conf 文件，这些文件由华为云Elasticsearch提供。

获取keytab文件 ：从华为云获取 keytab 文件，该文件包含了客户端的凭据信息，文件由华为云Elasticsearch提供。
### 2.2 集成bboss
集成bboss非常简单，将bboss maven坐标导入项目即可：
```xml
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-datatran-jdbc</artifactId>
    <version>7.3.6</version>
</dependency>
```

最新bboss版本号可以浏览文档获取：
[https://esdoc.bbossgroups.com/#/changelog](https://esdoc.bbossgroups.com/#/changelog)

## 3. 配置和使用Elasticsearch数据源
将bboss maven坐标导入项目，并准备好Kerberos认证相关的材料后，就可以实现基于Kerberos认证对接华为云Elasticsearch的功能，直接上代码。

通过以下代码定义和初始化Elasticsearch数据源，并通过ClientInterface接口操作和访问华为云Elasticsearch，验证集成是否成功：

```java
		Map properties = new HashMap();
        /**
         * 配置Elasticsearch数据源参数，这里只设置必须的配置项，更多配置参考文件：
         * https://gitee.com/bboss/elasticsearchdemo/blob/master/src/main/resources/application.properties
         */
        //定义Elasticsearch数据源名称：esDS，后续通过esDS获取对应数据源的客户端API操作和访问Elasticsearch
        properties.put("elasticsearch.serverNames","esDS");
        //es服务器地址和端口，多个用逗号分隔
        //properties.put("esDS.elasticsearch.rest.hostNames","192.168.137.1:8200");

		//开启https协议，华为云Elasticsearch一般会启用https,在bboss中，配置开启https协议的Elasticsearch节点地址时，需带上https://协议头；
		//更多https配置，可浏览后面参考资料中的【高性能elasticsearch ORM开发库使用介绍】了解
        properties.put("esDS.elasticsearch.useHttps","true");
        properties.put("esDS.elasticsearch.rest.hostNames","https://202.280.211.227:9280,https://202.280.211.227:9281,https://202.280.211.227:9282");
 
        //是否在控制台打印dsl语句，log4j组件日志级别为INFO或者DEBUG
        properties.put("esDS.elasticsearch.showTemplate","true");
        //集群节点自动发现,关闭服务发现机制
        properties.put("esDS.elasticsearch.discoverHost","false");
      
        //Kerberos安全认证配置--开始
        
        properties.put("esDS.http.kerberos.serverRealmPath","/elasticsearch/serverrealm");//配置华为云Elasticsearch服务端Princpal查询服务地址
        properties.put("esDS.http.kerberos.useSubjectCredsOnly","false");
        //华为云Elasticsearch krb5.conf文件，由华为提供
        properties.put("esDS.http.kerberos.krb5Location","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/krb5.conf");
        //华为云Elasticsearch jaas.conf文件，由华为提供
        properties.put("esDS.http.kerberos.loginConfig","C:/environment/es/8.13.2/elasticsearch-8.13.2/config/jaas.conf");

        //配置登录模块名称，与华为云Elasticsearch jaas.conf文件中的模块名称一致
        properties.put("esDS.http.kerberos.loginContextName","ESClient");
        
        //配置是否debug Kerberos认证详细日志
        properties.put("esDS.http.kerberos.debug","true");

        //Kerberos安全认证配置--结束
        
        //启动和初始化Elasticsearch数据源
        ElasticSearchBoot.boot(properties);
        
        //通过Elasticsearch数据源名称esDS获取对应数据源的客户端API，操作和访问Elasticsearch
        //可以反复根据数据源名称esDS，调用下面的方法获取ClientInterface接口实例，始终返回单实例多线程安全的ClientInterface对象
        ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil("esDS");
        
        //验证客户端：通过Elasticsearch rest服务获取ES集群信息
        String result = clientInterface.executeHttp("/?pretty", ClientInterface.HTTP_GET);
        logger.info(result);
        
        //验证客户端：通过API获取ES集群配置参数
        logger.info(clientInterface.getClusterSettings());

        //验证客户端：通过API判断索引demo是否存在
        boolean exist = clientInterface.existIndice("demo");

        logger.info(exist+"");
        //验证客户端：通过API从索引demo获取文档id为1的文档数据（String报文）
        String doc = clientInterface.getDocument("demo","1");

        logger.info(doc);

        //验证客户端：通过API从索引demo获取文档id为1的文档数据（or mapping示例：返回Map结构的数据，亦可以转换为PO对象）
        Map mapdoc = clientInterface.getDocument("demo","1",Map.class);
```

基于配置Kerberos认证实现代码非常简洁，只需在平常数据源参数配置的基础上，增加Kerberos认证相关的参数配置。上述代码中涉及的华为云Kerberos配置文件krb5.conf和jaas.conf，由华为云Elasticsearch提供，这里不单独介绍，需要注意一下：http.kerberos.loginContextName参数对应的值需与jaas.conf配置文件中认证模块名称一致，这里是ESClient。

下面是一个jaas.conf配置内容样例：

```json
ESClient {
  com.sun.security.auth.module.Krb5LoginModule required
  useKeyTab=true
  keyTab="C:/environment/es/8.13.2/elasticsearch-8.13.2/config/elastic.keytab"
  principal="elastic/admin@BBOSSGROUPS.COM"
  useTicketCache=false
  storeKey=true
  debug=false;
};
```
其中的elastic.keytab文件由华为云Elasticsearch提供。更多ClientInterface api使用方法，可以访问下面参考资料中提供的链接了解。

## 4. 运行案例

本文对应的代码源码工程下载地址：

码云 [https://gitee.com/bboss/eshelloword-booter](https://gitee.com/bboss/eshelloword-booter)

Github [https://github.com/bbossgroups/eshelloword-booter](https://github.com/bbossgroups/eshelloword-booter)

对应的Kerberos认证Java Demo [CustormInitAndBootKerberosAuth.java](https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/custominit/CustormInitAndBootKerberosAuth.java)

可下载案例源码工程，参考前面章节内容，修改Elasticsearch地址和Kerberos相关配置为本地配置，然后执行案例代码，如输出如下信息，说明集成成功：

```json
11:36:07.976 [main] INFO  org.frameworkset.elasticsearch.client.ElasticSearchRestClient - Elasticsearch Server Info:
{
  "name" : "node@202.280.211.227",
  "cluster_name" : "elasticsearch_cluster",
  "cluster_uuid" : "3veeeeeeeeeeeeeeeee4Q",
  "version" : {
    "number" : "7.10.2",
    "build_flavor" : "oss",
    "build_type" : "tar",
    "build_hash" : "unknown",
    "build_date" : "unknown",
    "build_snapshot" : true,
    "lucene_version" : "8.7.0",
    "minimum_wire_compatibility_version" : "6.7.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```

可观看bboss环境搭建视频，搭建bboss开发和运行环境：
[https://mp.weixin.qq.com/s/RoJdxiPw_mnuhQpkqzY9QQ](https://mp.weixin.qq.com/s/RoJdxiPw_mnuhQpkqzY9QQ)

## 5. 参考资料
Elasticsearch文档增删改查操作介绍 [https://esdoc.bbossgroups.com/#/document-crud](https://esdoc.bbossgroups.com/#/document-crud)

高性能elasticsearch ORM开发库使用介绍 [https://esdoc.bbossgroups.com/#/development](https://esdoc.bbossgroups.com/#/development)

快速开始bboss  [https://esdoc.bbossgroups.com/#/quickstart](https://esdoc.bbossgroups.com/#/quickstart)

开发交流 [https://esdoc.bbossgroups.com/#/supportus](https://esdoc.bbossgroups.com/#/supportus)