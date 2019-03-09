

**The best Elasticsearch Highlevel Rest  Client API-----bboss**   v5.5.3 发布。

# **主要功能特色**

1. ElasticSearch兼容性:1.x,2.x,5.x,6.x,+
2. JDK兼容性： jdk 1.7+
3. Spring boot兼容性:1.x,2.x
4. ORM和DSL二者兼顾，类mybatis方式操作ElasticSearch,
5. 支持[SQL](https://esdoc.bbossgroups.com/#/Elasticsearch-SQL-ORM)和[JDBC](https://my.oschina.net/bboss/blog/2221868)
6. 提供快速而高效的数据导入ES工具

# **v5.5.3 功能改进：**

1. 完善orm保存对象到es序列化机制，避免非ESBaseData和ESId对象的相关属性被忽略掉
2. 数据同步工具支持mysql8,tidb,hive
3. 完善http组件：自定义重试机制 
   http.customHttpRequestRetryHandler=org.frameworkset.spi.remote.http.ConnectionResetHttpRequestRetryHandler
   空闲连接校验频率，单位毫秒，>0起作用http.validateAfterInactivity=3000
   每次获取connection时校验连接，true，校验，默认false
   http.staleConnectionCheckEnabled=false 
4. 依赖的http组件版本升级：
   httpclient, version: '4.5.6'
   httpcore, version: '4.4.11'
   httpmime, version: '4.5.6'
5. 完善框架打印的日志信息
6. 修复释放资源时抛出空指针异常
7. 数据同步工具改进：可以按日期时间自动分表，使用方法importBuilder
   .setIndex("dbdemo-{yyyy.MM.dd}") //通过{yyyy.MM.dd}设置按日期分索引表
8. 数据同步工具改进：解决忽略字段名称小写时不起作用的问题
9. 新增GeoPoint和GeoShape两个对象
10. 增加单值聚合查询的api和测试用例
    参考文档：<https://esdoc.bbossgroups.com/#/agg>
11. 增加open/close index方法
12. 批量修改/添加文档api完善：增加指定对象字段名称对应的值作为文档id和文档父id
13. 增加一组通用api：通过ClientOptions/UpdateOptions指定控制参数对应的对象字段，替代原有的@ESId,@ESParentId等注解
14. 优化dsl配置文件热加载机制：解决jar中dsl配置文件热加载问题
15. 新增一个基于spring boot2的web demo
    <https://github.com/bbossgroups/es_bboss_web>
16. 修复数据同步工具bug：解决增量同步线程池重复创建问题，建议大家将版本升级到5.5.3
17.  完善对elasticsearch 1.x版本的支持,searchallparallel方法支持es 1.x版本
18.  数据同步工具：elasticsearch同步到dbes增加scroll parallel导出功能
19.  数据导出工具: 任务执行结果处理接口，对每次bulk任务的结果进行成功和失败反馈，然后针对失败的bulk任务进行相应处理，参考文档：
    <https://esdoc.bbossgroups.com/#/db-es-tool>
20. 数据同步工具：规范并修改相关类的名称
21. sclice scroll检索内部采用异步方式执行每个scroll查询结果
22. scroll检索增加异步处理每个scroll查询结果的功能
23. 数据同步工具：增加在过滤器中过滤记录功能 
24. Innerhit检索时层级超过2级的对象（继承ESBaseData对象）中没有设置文档id等信息问题修复

更多功能改进请浏览：[commit](https://gitee.com/bboss/bboss-elastic/commits/master)

# **bboss elasticsearch 使用参考文档** 

[https://esdoc.bbossgroups.com](https://esdoc.bbossgroups.com/)  



# 相关链接

- bboss-elastic 的详细介绍：[点击查看](README.md)
- bboss-elastic 的下载地址：[点击下载](https://gitee.com/bboss/bboss-elastic)