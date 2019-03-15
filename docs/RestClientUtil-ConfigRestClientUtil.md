# RestClientUtil和ConfigRestClientUtil区别说明

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

RestClientUtil and ConfigRestClientUtil have essentially the same API, with the following differences:

**RestClientUtil** directly executes the DSL defined in the code.

**ConfigRestClientUtil** gets the DSL defined in the configuration file by the DSL name and executes it. 

RestClientUtil example:

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();//define an instanceof RestClientUtil,It's single instance, multithreaded secure.  
List<Map> json = clientUtil.sql(Map.class,"{\\"query\\": \\"SELECT * FROM demo\\"}");  
```

**注意**：**bboss本身没有提供querybuilder工具，可以借助第三方工具构建好了后转换为dsl，然后调用RestClientUtil的api来执行即可**



ConfigRestClientUtil example:

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//define an instanceof ConfigRestClientUtil,It's single instance, multithreaded secure.  
Map params = new HashMap();  
params.put("channelId",1);  
List<Map> json = clientUtil.sql(Map.class,"sqlQuery",params);  
```

the dsl config file:sql.xml ,We can define a lot of DSLs in the configuration file

```xml
<properties>  
    <!--  
        sql query  
    -->  
    <property name="sqlQuery">  
        <![CDATA[  
         {"query": "SELECT * FROM dbclobdemo where channelId=#[channelId]"}  
        ]]>  
    </property>  
</properties>  
```