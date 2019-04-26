# 搜索引擎的 ORM 库 Elasticsearch Bboss 

**The best elasticsearch highlevel java rest api-----bboss** 

 bboss elasticsearch是一套基于query dsl语法操作和访问分布式搜索引擎elasticsearch的o/r mapping高性能开发库，底层基于es restful api。基于bboss elasticsearch，可以快速编写出访问和操作elasticsearch的程序代码，简单、高效、可靠、安全。

​    **bboss elasticsearch以类似于mybatis的方式,使用xml文件管理elasticsearch的dsl脚本，在dsl脚本中可以使用变量、脚本片段、foreach循环、逻辑判断、注释**；配置文件支持在线修改、自动热加载，开发和调试非常方便。

# **bboss-elastic特色**

1. 采用类似于[mybatis](https://www.oschina.net/p/mybatis)的方式配置语法配置和管理访问es的qsl脚本，简洁而直观，支持配置文件热加载功能；提供丰富的逻辑判断语法；支持qsl脚本片段和片段引用功能；
2. 提供高效可定制的[db到elasticsearch数据导入能力](db-es-tool.md)
3. 支持[elasticsearch sql](Elasticsearch-SQL-ORM.md)，可替代es jdbc模块；引入bboss不仅可以拥有bboss的客户端自动发现和负载容灾能力、对es、jdk、spring boot的兼容性能力，还可以拥有es jdbc的所有功能，同时还解决了因为引入es jdbc导致项目对es版本的强依赖和兼容性问题，参考demo：
   orm
   <https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/sql/SQLOrmTest.java>
   分页
   <https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/sql/SQLPagineTest.java>
4. 支持ES JDBC，参考文档：[Elasticsearch JDBC案例介绍](https://my.oschina.net/bboss/blog/2221868)
5. 提供[创建和查询索引表配置模板api](index-indextemplate.md)；提供创建和查询索引表api；提供索引文档创建、修改、删除、获取基本功能;提供索引文档批量创建、批量修改、批量删除api；
6. 提供支持分页检索api（[from/size](from-size.md),[searchafter](search-after.md),[scroll](scroll.md),[slicescroll](Scroll-SliceScroll-api.md)）
7. 支持获取索引文档字段元数据
8. 提供简洁易用的全文检索api，[聚合检索和统计api](agg.md)；支持o/mapping功能，支持检索和聚合查询结果快速转换为java对象或者java对象列表；支持分页检索功能和[关键词高亮显示](highlight.md)；支持多索引表查询；支持[父子关系](elasticsearch5-parent-child.md)检索；支持[地理位置检索](Elasticsearch-geo.md)；提供检索和聚合查询结果的回调处理接口，可以自定义结果封装处理逻辑。
9. 支持[关键字自动联想和自动纠错的api](https://gitee.com/bboss/elasticsearchdemo/tree/master/src/test/java/org/frameworkset/elasticsearch/suggest)
10. 提供客户端自动负载均衡和容灾恢复机制，高效可靠
11. 内置http 连接池管理
12. 支持自动发现es服务端新加节点或者剔除节点
13. 支持基于[X-Pack](https://www.elastic.co/cn/products/x-pack)和searchguard两种安全认证机制
14. 除了提供高阶的o/r mapping API，还提供了简单易用的原生restful api和基于tcp的Transport api，可以根据实际需要使用合适的API  
15. ClientUtil组件可以指定elasticsearch服务器，支持多es集群，可以在指定的elasticsearch集群上执行操作
16. 提供自动清理历史索引数据的[工具](elasticsearch-indexclean-task.md)
17. **bboss es不依赖elasticsearch官方任何jar文件，兼容elasticsearch版本:1.x,2.x,5.x,6.x****,+**
18. **bboss es jdk兼容性： jdk 1.7+**
19. **bboss es兼容spring boot各个版本,零配置集成，提供spring booter es starter**
20. **一个快速生成bboss es pinpoint监控插件的工具,监控效果浏览**
21. bboss es环境搭建及开发视频教程（依赖的版本以最新的[maven中央库版本](https://search.maven.org/search?q=g:com.bbossgroups.plugins)为准）：[下载](https://pan.baidu.com/s/1kXjAOKn)

# 从源码构建Elasticsearch BBoss

First Get source code from https://github.com/bbossgroups/bboss-elasticsearch

Then change to cmd window under directory bboss-elasticsearch and run gradle build command：

```
gradle install
```

Gradle environmenet install and config document: https://esdoc.bbossgroups.com/#/bboss-build

# Elasticsearch Demo
spring booter必看demo1：

[spring booter web服务demo](https://github.com/bbossgroups/es_bboss_web)

spring booter必看demo2：

[eshelloword-spring-boot-starter](https://github.com/bbossgroups/elasticsearch-springboot-example) （maven）

其他项目参考demo1：

[eshelloworld-example](https://github.com/bbossgroups/elasticsearch-example) （maven）

其他项目参考demo2：

[elasticsearch-gradle-demo](https://github.com/bbossgroups/elasticsearch-gradle-example)  （gradle）

# 快速集成和应用 

- 所有类型项目：[common-project-with-bboss](common-project-with-bboss.md) 
- spring boot 项目：[spring-booter-with-bboss](spring-booter-with-bboss.md) 

github源码地址：

<https://github.com/bbossgroups/bboss-elasticsearch>

bboss elasticsearch开发库使用文档：

[How to use Elasticsearch BBoss](quickstart.md)

bboss与es官方客户端的对比：[bboss es对比直接使用es客户端的优势](bboss-vs-es.md)

# 联系我们

**bboss elasticsearch技术交流群：166471282**

**bboss elasticsearch微信公众号：**

**![bboss微信公众号：bbossgroups](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)**