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



# 配置默认Elasticsearch集群mapper实例

```xml
<bean id="clientInterfaceMapper" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getConfigRestClient">    
    <constructor-arg value="esmapper/demo.xml"></constructor-arg>
 </bean>
```

# 配置指定Elasticsearch集群mapper实例

```xml
<bean id="clientInterfaceMapperWood" 
     class="org.frameworkset.elasticsearch.ElasticSearchHelper"
     factory-method="getConfigRestClient">
     <constructor-arg value="wood"></constructor-arg>
    <constructor-arg value="esmapper/demo.xml"></constructor-arg>
 </bean>
```



# 参考资料

- 所有类型项目：[common-project-with-bboss](common-project-with-bboss.md) 
- spring boot 项目：[spring-booter-with-bboss](spring-booter-with-bboss.md)

# 开发交流



bboss elasticsearch交流QQ群：21220580,166471282,3625720,154752521,166471103,166470856

**bboss elasticsearch微信公众号：**

<img src="images/qrcode.jpg"  height="200" width="200">



