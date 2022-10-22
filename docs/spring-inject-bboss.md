# Spring配置文件注入Elasticsearch Bboss组件方法

在Spring配置文件中注入Elasticsearch Bboss组件实例是一件非常简单的事情，下面介绍如何在spring配置文件中注入bboss单es集群实例和多集群实例组件。

# 1.配置默认Elasticsearch集群实例

```xml
<bean id="clientInterface" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getRestClientUtil">    
 </bean>
```
# 2.配置指定Elasticsearch集群实例

```xml
<bean id="clientInterfaceWood" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getRestClientUtil">
     <constructor-arg value="wood"></constructor-arg>
 </bean>
```



# 3.配置默认Elasticsearch集群mapper实例

```xml
<bean id="clientInterfaceMapper" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getConfigRestClient">    
    <constructor-arg value="esmapper/demo.xml"></constructor-arg>
 </bean>
```

# 4.配置指定Elasticsearch集群mapper实例

```xml
<bean id="clientInterfaceMapperWood" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getConfigRestClient">
     <constructor-arg value="wood"></constructor-arg>
    <constructor-arg value="esmapper/demo.xml"></constructor-arg>
 </bean>
```

# 5.管理bulkprocess

本节介绍如何通过spring factory模式创建两个异步批处理组件：detailBulkProcessor和metricsBulkProcessor

## 5.1 定义bulkprocessorfactory

```java
package com.test.util;

import org.frameworkset.elasticsearch.bulk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BulkProcessorFactory {
    private static Logger logger = LoggerFactory.getLogger(BulkProcessorFactory.class);
    public BulkProcessor buildDetailBulkProcessor(){
        int bulkSize = PropertiesUtil.getDetailBulkSize();
        int workThreads = PropertiesUtil.getDetailBulkWorkThreads();
        int workThreadQueue = PropertiesUtil.getDetailBulkWorkThreadQueue();

        //定义BulkProcessor批处理组件构建器
        BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();
        bulkProcessorBuilder.setBlockedWaitTimeout(-1)//指定bulk工作线程缓冲队列已满时后续添加的bulk处理排队等待时间，如果超过指定的时候bulk将被拒绝处理，单位：毫秒，默认为0，不拒绝并一直等待成功为止

                .setBulkSizes(bulkSize)//按批处理数据记录数
                .setFlushInterval(5000)//强制bulk操作时间，单位毫秒，如果自上次bulk操作flushInterval毫秒后，数据量没有满足BulkSizes对应的记录数，但是有记录，那么强制进行bulk处理

                .setWarnMultsRejects(1000)//由于没有空闲批量处理工作线程，导致bulk处理操作出于阻塞等待排队中，BulkProcessor会对阻塞等待排队次数进行计数统计，bulk处理操作被每被阻塞排队WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息
                .setWorkThreads(workThreads)//bulk处理工作线程数
                .setWorkThreadQueue(workThreadQueue)//bulk处理工作线程池缓冲队列大小
                .setBulkProcessorName("detail_bulkprocessor")//工作线程名称，实际名称为BulkProcessorName-+线程编号
                .setBulkRejectMessage("detail bulkprocessor")//bulk处理操作被每被拒绝WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息提示前缀
                .setElasticsearch("detail")//指定明细Elasticsearch集群数据源名称，bboss可以支持多数据源
                .setFilterPath(BulkConfig.ERROR_FILTER_PATH)
                .addBulkInterceptor(new BulkInterceptor() {
                    public void beforeBulk(BulkCommand bulkCommand) {

                    }

                    public void afterBulk(BulkCommand bulkCommand, String result) {
                       if(logger.isDebugEnabled()){
                           logger.debug(result);
                       }
                    }

                    public void exceptionBulk(BulkCommand bulkCommand, Throwable exception) {
                        if(logger.isErrorEnabled()){
                            logger.error("exceptionBulk",exception);
                        }
                    }
                    public void errorBulk(BulkCommand bulkCommand, String result) {
                        if(logger.isWarnEnabled()){
                            logger.warn(result);
                        }
                    }
                })//添加批量处理执行拦截器，可以通过addBulkInterceptor方法添加多个拦截器
        ;
        /**
         * 构建BulkProcessor批处理组件，一般作为单实例使用，单实例多线程安全，可放心使用
         */
        BulkProcessor bulkProcessor = bulkProcessorBuilder.build();//构建批处理作业组件
        return bulkProcessor;
    }

    public BulkProcessor buildMetricsBulkProcessor(){
        int bulkSize = PropertiesUtil.getBulkSize();
        int workThreads = PropertiesUtil.getBulkWorkThreads();
        int workThreadQueue = PropertiesUtil.getBulkWorkThreadQueue();

        //定义BulkProcessor批处理组件构建器
        BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();
        bulkProcessorBuilder.setBlockedWaitTimeout(-1)//指定bulk工作线程缓冲队列已满时后续添加的bulk处理排队等待时间，如果超过指定的时候bulk将被拒绝处理，单位：毫秒，默认为0，不拒绝并一直等待成功为止

                .setBulkSizes(bulkSize)//按批处理数据记录数
                .setFlushInterval(5000)//强制bulk操作时间，单位毫秒，如果自上次bulk操作flushInterval毫秒后，数据量没有满足BulkSizes对应的记录数，但是有记录，那么强制进行bulk处理

                .setWarnMultsRejects(1000)//由于没有空闲批量处理工作线程，导致bulk处理操作出于阻塞等待排队中，BulkProcessor会对阻塞等待排队次数进行计数统计，bulk处理操作被每被阻塞排队WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息
                .setWorkThreads(workThreads)//bulk处理工作线程数
                .setWorkThreadQueue(workThreadQueue)//bulk处理工作线程池缓冲队列大小
                .setBulkProcessorName("metrics_bulkprocessor")//工作线程名称，实际名称为BulkProcessorName-+线程编号
                .setBulkRejectMessage("metrics bulkprocessor")//bulk处理操作被每被拒绝WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息提示前缀
                .setElasticsearch("default")//指定指标Elasticsearch集群数据源名称，bboss可以支持多数据源
                .setFilterPath(BulkConfig.ERROR_FILTER_PATH)
                .addBulkInterceptor(new BulkInterceptor() {
                    public void beforeBulk(BulkCommand bulkCommand) {

                    }

                    public void afterBulk(BulkCommand bulkCommand, String result) {
                        if(logger.isDebugEnabled()){
                            logger.debug(result);
                        }
                    }

                    public void exceptionBulk(BulkCommand bulkCommand, Throwable exception) {
                        if(logger.isErrorEnabled()){
                            logger.error("exceptionBulk",exception);
                        }
                    }
                    public void errorBulk(BulkCommand bulkCommand, String result) {
                        if(logger.isWarnEnabled()){
                            logger.warn(result);
                        }
                    }
                })//添加批量处理执行拦截器，可以通过addBulkInterceptor方法添加多个拦截器
        ;
        /**
         * 构建BulkProcessor批处理组件，一般作为单实例使用，单实例多线程安全，可放心使用
         */
        BulkProcessor bulkProcessor = bulkProcessorBuilder.build();//构建批处理作业组件
        return bulkProcessor;
    }
}
```

## 5.2 声明工厂组件

```xml
<bean id="bulkProcessorFactory" 
     class="com.test.util.BulkProcessorFactory">
 </bean>
```

## 5.3 通过工厂组件创建BulkProcessor

```xml
  <bean id="detailBulkProcessor" 
     factory-bean="bulkProcessorFactory"
     factory-method="buildDetailBulkProcessor">
 </bean>
   <bean id="metricsBulkProcessor" 
     factory-bean="bulkProcessorFactory"
     factory-method="buildMetricsBulkProcessor">
 </bean>
```



# 6.参考资料

- 所有类型项目：[common-project-with-bboss](common-project-with-bboss.md) 
- spring boot 项目：[spring-booter-with-bboss](spring-booter-with-bboss.md)

# 7.开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



