# bboss-elastic

不错的elasticsearch客户端工具包,bboss es开发套件采用类似于mybatis的方式操作elasticsearch

maven坐标
```
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-plugin-elasticsearch</artifactId>
    <version>5.0.3.7</version>
</dependency>
```
gradle坐标
```
compile "com.bbossgroups.plugins:bboss-plugin-elasticsearch:5.0.3.7"
```
# elastic search配置
对应的配置文件为conf/elasticsearch.properties

## tcp 地址和端口配置，
集群用逗号分隔
127.0.0.1:9300,127.0.0.1:9301,127.0.0.1:9302

elasticsearch.transport.hostNames=127.0.0.1:9300

## http地址和端口配置
集群用逗号分隔：127.0.0.1:9200,127.0.0.1:9201,127.0.0.1:9202

elasticsearch.rest.hostNames=127.0.0.1:9200

## 每天产生的索引日期格式
elasticsearch.dateFormat=yyyy.MM.dd

# http连接池配置
配置文件conf/elasticsearch.properties

## 总共允许的最大连接数
http.maxTotal = 400

## 每个地址运行的最大连接数
http.defaultMaxPerRoute = 200

# 使用示例

## 配置es查询dsl
在resources下创建配置文件estrace/ESTracesqlMapper.xml，内容如下：
```
<properties>
   <property name="queryServiceByCondition">
        <![CDATA[
        {
            "sort": [
                {
                    "startTime": {
                        "order": "desc"
                    }
                }
            ],
            #if($lastStartTime > 0)//分页查询起点
            "search_after": [$lastStartTime],
            #end
            "size": 100,
            "query": {
                "bool":{
                    "filter": [
                        {"term": {
                            "applicationName": "$application"
                        }}
                        #if($queryStatus.equals("success"))
                          ,
                          {"term": {

                               "err": 0
                          }}
                        #elseif($queryStatus.equals("error"))
                          ,
                          {"term": {

                               "err": 1
                          }}
                        #end
                    ],
                    "must": {
                        "range": {
                            "startTime": {
                                "gte": $startTime,
                                "lt": $endTime
                            }
                        }
                    }
                    #if($queryCondition && !$queryCondition.equals(""))
                     ,
                     "must" : {
                        "multi_match" : {
                          "query" : "$queryCondition",

                          "fields" : [ "agentId", "applicationName" ,"endPoint","params","remoteAddr","rpc","exceptionInfo"]
                        }
                     }
                    #end
                }
            }
        }]]>
    </property>

</properties>
```
bboss es开发套件采用类似于mybatis的方式操作elasticsearch

## ormapping操作示例
加载query dsl文件,并执行查询操作

```
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientUtil;
//加载配置文件，创建es客户端工具包
ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesqlMapper.xml");

//构建查询条件对象
TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
traceExtraCriteria.setApplication("testweb88");
DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
traceExtraCriteria.setStartTime(dateFormat.parse("2017-09-02 00:00:00").getTime());
traceExtraCriteria.setEndTime(dateFormat.parse("2017-09-13 00:00:00").getTime());



// 检索条件
String queryCondition = (request.getParameter("queryCondition"));
// 设置检索条件
traceExtraCriteria.setQueryCondition(queryCondition);
// 查询状态：all 全部 success 处理成功 fail 处理失败
String queryStatus = (request.getParameter("queryStatus"));
traceExtraCriteria.setQueryStatus(queryStatus);
//设置分页数据起点，以时间为起点
String lastStartTimeStr = request.getParameter("lastStartTime");
if(lastStartTimeStr != null && !lastStartTimeStr.equals("")) {
	Long lastStartTime = Long.parseLong(lastStartTimeStr);
	traceExtraCriteria.setLastStartTime(lastStartTime);
}

//执行查询操作
ESDatas<Traces> data //ESDatas为查询结果集对象，封装了返回的当前查询的List<Traces>结果集和符合条件的总记录数totalSize
			= clientUtil.searchList"trace-*/_search",//查询操作，查询indices trace-*中符合条件的数据
								"queryServiceByCondition",//配置在配置文件中的query dsl语句
								traceExtraCriteria,//查询条件封装对象
								Traces.class);//指定返回的po对象类型，po对象中的属性与indices表中的文档filed名称保持一致

```

# 进阶
初始化bboss elasticsearch组件ClientUtil时，可以指定elasticsearch服务器，支持在指定的elasticsearch服务器集群进行操作,例如：
```
ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("traceElasticSearch",//可以指定elasticSearch服务器
                                                                    "estrace/ESTracesqlMapper.xml");
```
traceElasticSearch服务器组件，在conf/elasticsearch.xml文件中配置：
```
	    <!--
        其他elasticSearch组件，对应另外一个elasticsearch服务器
        -->
    	<property name="traceElasticsearchPropes">
    		<propes>
    
    			<property name="elasticsearch.client" value="${trace.elasticsearch.client:restful}">
    				<description> <![CDATA[ 客户端类型:transport，restful ]]></description>
    			</property>
    
    			<property name="elasticUser" value="${trace.elasticUser:}">
    				<description> <![CDATA[ 认证用户 ]]></description>
    			</property>
    
    			<property name="elasticPassword" value="${trace.elasticPassword:}">
    				<description> <![CDATA[ 认证口令 ]]></description>
    			</property>
    			<!--<property name="elasticsearch.hostNames" value="${trace.elasticsearch.hostNames}">
    				<description> <![CDATA[ tcp协议地址 ]]></description>
    			</property>-->
    
    			<property name="elasticsearch.rest.hostNames" value="${trace.elasticsearch.rest.hostNames}">
    				<description> <![CDATA[ rest协议地址 ]]></description>
    			</property>
    
    			<property name="elasticsearch.transport.hostNames" value="${trace.elasticsearch.transport.hostNames}">
    				<description> <![CDATA[ tcp协议地址 ]]></description>
    			</property>
    
    			<property name="elasticsearch.clusterName" value="${trace.elasticsearch.clusterName:}">
    				<description> <![CDATA[ es集群名称]]></description>
    			</property>
    			<property name="elasticsearch.dateFormat" value="${trace.elasticsearch.dateFormat}">
    				<description> <![CDATA[ 索引日期格式]]></description>
    			</property>
    			<property name="elasticsearch.timeZone" value="${trace.elasticsearch.timeZone}">
    				<description> <![CDATA[ 时区信息]]></description>
    			</property>
    			<property name="elasticsearch.indexName" value="${trace.elasticsearch.indexName}">
    				<description> <![CDATA[ 默认索引名称]]></description>
    			</property>
    			<property name="elasticsearch.indexType" value="${trace.elasticsearch.indexType}">
    				<description> <![CDATA[ 默认索引类型]]></description>
    			</property>
    			<property name="elasticsearch.ttl" value="${elasticsearch.ttl}">
    				<description> <![CDATA[ ms(毫秒) s(秒) m(分钟) h(小时) d(天) w(星期)]]></description>
    			</property>
    
    			<property name="elasticsearch.showTemplate" value="${trace.elasticsearch.showTemplate:false}">
    				<description> <![CDATA[ ms(毫秒) s(秒) m(分钟) h(小时) d(天) w(星期)]]></description>
    			</property>
    			<property name="elasticsearch.serialize" value="${trace.elasticsearch.serialize:}">
    				<description> <![CDATA[ elasticsearch.serializer=org.frameworkset.elasticsearch.ElasticSearchJSONEventSerializer]]></description>
    			</property>
    			<property name="elasticsearch.httpPool" value="${trace.elasticsearch.httpPool:default}">
    				<description> <![CDATA[ http连接池逻辑名称，在conf/httpclient.xml中配置]]></description>
    			</property>
    
    		</propes>
    	</property>
	<property name="traceElasticSearch"
			  class="org.frameworkset.elasticsearch.ElasticSearch"
			  init-method="configure"
			  destroy-method="stop"
			  f:elasticsearchPropes="attr:traceElasticsearchPropes"/>
```
elasticsearch的服务属性配置在文件conf/elasticsearch.properties中配置:
```
trace.elasticsearch.client=rest
trace.elasticUser=elastic
trace.elasticPassword=changeme
trace.elasticsearch.transport.hostNames=192.168.0.2:9300
trace.elasticsearch.rest.hostNames=192.168.0.2:9200
trace.elasticsearch.dateFormat=yyyy.MM.dd
trace.elasticsearch.timeZone=Asia/Shanghai
trace.elasticsearch.ttl=2d
trace.elasticsearch.showTemplate=true
```
# 完整的demo
https://github.com/bbossgroups/elasticsearchdemo

# bboss elastic特点
https://www.oschina.net/p/bboss-elastic