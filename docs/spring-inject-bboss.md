# Spring配置文件注入Elasticsearch Bboss组件方法

在Spring配置文件中注入Elasticsearch Bboss组件实例是一件非常简单的事情，下面介绍如何在spring配置文件中注入bboss单es集群实例和多集群实例组件。

# 配置默认Elasticsearch集群实例

```xml
<bean id="clientInterface" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getRestClientUtil">    
 </bean>
```
# 配置指定Elasticsearch集群实例

```xml
<bean id="clientInterfaceWood" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getRestClientUtil">
     <constructor-arg value="wood"></constructor-arg>
 </bean>
```

