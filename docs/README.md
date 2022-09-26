# 搜索引擎的 ORM 库 Elasticsearch Bboss 

**The best elasticsearch highlevel java rest api-----bboss** 

 bboss是一套基于query dsl语法操作和访问分布式搜索引擎Elasticsearch/Opensearch的o/r mapping高性能开发库，底层直接基于 http 协议操作和访问Elasticsearch、Opensearch。基于bboss，可以快速编写出访问和操作Elasticsearch/Opensearch的程序代码，简单、高效、可靠、安全。

# 快速开始bboss

https://esdoc.bbossgroups.com/#/quickstart

# bboss兼容性

Elasticsearch、Spring boot兼容性

| bboss | Elasticsearch | spring boot |
| ----- | ------------- | ----------- |
| all   | 1.x           | 1.x,2.x,3.x |
| all   | 2.x           | 1.x,2.x,3.x |
| all   | 5.x           | 1.x,2.x,3.x |
| all   | 6.x           | 1.x,2.x,3.x |
| all   | 7.x           | 1.x,2.x,3.x |
| all   | 8.x           | 1.x,2.x,3.x |

jdk兼容性：jdk 1.8+

# 主要功能特色

1. ORM和DSL二者兼顾，类mybatis方式操作ElasticSearch,提供丰富的开发[API](https://esdoc.bbossgroups.com/#/document-crud)和[开发Demo](https://esdoc.bbossgroups.com/#/Elasticsearch-demo)

2. 采用[XML文件配置和管理检索dsl脚本](https://esdoc.bbossgroups.com/#/development?id=_53-dsl%e9%85%8d%e7%bd%ae%e8%a7%84%e8%8c%83)，简洁而直观；提供丰富的逻辑判断语法,在dsl脚本中可以使用变量、脚本片段、foreach循环、逻辑判断、注释；基于[可扩展DSL配置管理机制](https://esdoc.bbossgroups.com/#/db-dsl)可以非常方便地实现数据库、redis等方式管理dsl;配置管理的dsl语句支持在线修改、自动热加载，开发和调试非常方便

3. 提供Elasticsearch集群节点自动负载均衡和容灾恢复机制，Elasticsearch节点断连恢复后可自动重连，高效可靠

4. 提供Elasticsearch集群节点[自动发现机制](https://esdoc.bbossgroups.com/#/development?id=_23-%e9%9b%86%e7%be%a4%e8%8a%82%e7%82%b9%e8%87%aa%e5%8a%a8%e5%8f%91%e7%8e%b0discover%e6%8e%a7%e5%88%b6%e5%bc%80%e5%85%b3)：自动发现Elasticsearch服务端节点增加和下线操作并变更客户端集群可用节点地址清单

5. 提供[http 连接池管理](https://esdoc.bbossgroups.com/#/development?id=_26-http%e5%8d%8f%e8%ae%ae%e9%85%8d%e7%bd%ae)功能，提供精细化的http连接池参数配置管理

6. 支持在应用中[访问和操作多个Elasticsearch集群](https://esdoc.bbossgroups.com/#/development?id=_52-%e5%a4%9aelasticsearch%e6%9c%8d%e5%8a%a1%e5%99%a8%e9%9b%86%e7%be%a4%e6%94%af%e6%8c%81)，每个Elasticsearch集群的版本可以不同

7. 支持基于[X-Pack](https://www.elastic.co/cn/products/x-pack)和searchguard两种[安全认证机制](https://esdoc.bbossgroups.com/#/development?id=_21-es%e6%9c%8d%e5%8a%a1%e5%99%a8%e8%b4%a6%e5%8f%b7%e5%92%8c%e5%8f%a3%e4%bb%a4%e9%85%8d%e7%bd%ae)

8. 支持[Elasticsearch-SQL-ORM](https://esdoc.bbossgroups.com/#/Elasticsearch-SQL-ORM)和[Elasticsearch-JDBC](https://esdoc.bbossgroups.com/#/Elasticsearch-JDBC)

9. 提供高效的[BulkProcessor处理机制](https://esdoc.bbossgroups.com/#/bulkProcessor)

10. 提供快速而高效的数据同步导入ES工具，**支持增、删、改数据同步**,支持[DB到Elasticsearch](https://esdoc.bbossgroups.com/#/db-es-tool)，[Elasticsearch到DB](https://esdoc.bbossgroups.com/#/db-es-tool?id=_3-elasticsearch-db%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bd%bf%e7%94%a8%e6%96%b9%e6%b3%95),[MongoDB到Elastisearch数据同步](https://esdoc.bbossgroups.com/#/mongodb-elasticsearch)，[HBase到Elasticsearch数据同步](https://esdoc.bbossgroups.com/#/hbase-elasticsearch)，[Kafka到Elasticsearch数据同步](https://esdoc.bbossgroups.com/#/db-es-tool?id=_7-kafka2x-elasticsearch%e6%95%b0%e6%8d%ae%e5%90%8c%e6%ad%a5%e4%bd%bf%e7%94%a8%e6%96%b9%e6%b3%95)，[DB到DB之间数据同步](https://esdoc.bbossgroups.com/#/db-es-tool?id=_5-database-database数据同步使用方法)，[http/https](https://esdoc.bbossgroups.com/#/datatran-http),[文本文件/Excel文件](https://esdoc.bbossgroups.com/#/filelog-guide)，[本地文件/Ftp/Sftp](https://esdoc.bbossgroups.com/#/elasticsearch-sftp)等多种数据源之间相互同步，可扩展支持更多的数据源

11. 提供按时间日期[ES历史数据清理工具](https://esdoc.bbossgroups.com/#/elasticsearch-indexclean-task)

12. APM开源产品pinpoint官方Elasticsearch bboss 客户端性能监控插件，插件地址：
https://github.com/naver/pinpoint/tree/master/plugins/elasticsearch-bboss
    
    
    

# 源码地址：

https://github.com/bbossgroups/bboss-elasticsearch

从源码构建

https://esdoc.bbossgroups.com/#/bboss-build

# Elasticsearch Demo
https://esdoc.bbossgroups.com/#/Elasticsearch-demo

# 快速集成和应用 

- 所有类型项目：[common-project-with-bboss](common-project-with-bboss.md) 
- spring boot 项目：[spring-booter-with-bboss](spring-booter-with-bboss.md) 

bboss es环境搭建及开发视频教程（依赖的版本以最新的[maven中央库版本](https://search.maven.org/search?q=g:com.bbossgroups.plugins)为准）：[下载](https://pan.baidu.com/s/1kXjAOKn)

# 联系我们

**Elasticsearch技术交流群：21220580,166471282**

**Elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



