# Elasticsearch控制搜索相关度评分案例

*The best elasticsearch highlevel java rest api-----[bboss](README.md)* 

# 通过Painless脚本控制搜索评分

​	ES有多种方式控制对搜索结果评分，如果常规方式无法得到想要的评分结果，则可以脚本方式完全自己实现评分算法，以得到预期的评分结果。

​	通过脚本控制评分的原理是编写一个自定义的脚本，该脚本返回评分值，该分值与原分值进行加法等运算，从而完全控制了评分算法。我们以一个通讯录名单索引user-info案例来举例说明通过Painless脚本控制搜索评分功能。

​	案例涉及的dsl采用xml配置文件管理，操作Elasticsearch客户端采用-[bboss](README.md)来实现。

# 创建索引-通讯录名单索引user-info

新建dsl 配置文件-esmapper/score.xml，并在其中定义创建通讯录索引user-info mapping dsl，名称为createUserInfoIndice

```xml
<properties>
    <!--
    通过score函数计算相关度打分案例
	创建通讯录索引user-info mapping dsl
    -->
    <property name="createUserInfoIndice">
        <![CDATA[{
            "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0,
                "index.refresh_interval": "5s"
            },
            "mappings": {
                "user": {
                    "properties": {
                        "name":{
                            "type":"keyword"
                        }
                    }
                }
            }
        }]]>
    </property>
    
</properties>
```

加载配置文件并创建索引

```java
 ClientInterface clientInterface = ElasticSearchHelper.getConfigRestClientUtil("esmapper/score.xml");
private void createUserInfoIndice(){
   
   if(clientInterface.existIndice("user-info")){
      clientInterface.dropIndice("user-info");
   }
   clientInterface.createIndiceMapping("user-info","createUserInfoIndice");
}
```

# 批量添加通讯录数据

通过以下代码向user-info中添加7条name不同的测试数据：

```java
private void importUserInfoData(){
   List<UserInfo> userInfoList = new ArrayList<UserInfo>();
   UserInfo userInfo = new UserInfo();
   userInfo.setName("高 X");
   userInfo.setUserId("1");
   userInfoList.add(userInfo);
   userInfo = new UserInfo();
   userInfo.setName("高 XX");
   userInfo.setUserId("2");
   userInfoList.add(userInfo);
   userInfo = new UserInfo();
   userInfo.setName("X 高 X");
   userInfo.setUserId("3");
   userInfoList.add(userInfo);
   userInfo = new UserInfo();
   userInfo.setName("X 高 X");
   userInfo.setUserId("4");
   userInfoList.add(userInfo);
   userInfo = new UserInfo();
   userInfo.setName("XXX 高");
   userInfo.setUserId("5");
   userInfoList.add(userInfo);
   userInfo = new UserInfo();
   userInfo.setName("高 XXX");
   userInfo.setUserId("6");
   userInfoList.add(userInfo);
   userInfo = new UserInfo();
   userInfo.setName("XXX 高 X");
   userInfo.setUserId("7");
   userInfoList.add(userInfo);
    //强制refresh，以便能够实时执行后面的检索操作，生产环境去掉"refresh=true"
   clientInterface.addDocuments("user-info","user",userInfoList,"refresh=true");
}
```

# 通讯录检索

接下来实现通讯录的检索操作：**我们期待的返回顺序与两个原则有关，检索关键词出现的位置越靠前，排序应该越靠前；字段值越短，说明匹配度越高，排序应该越靠前**。

## 普通检索

首先做一个普通检索，看看返回结果能否符合上述要求。

在dsl 配置文件-esmapper/score.xml中增加名称为nameQuery的检索语句：

```xml
<!--
      简单的query案例，默认评分规则
普通检索dsl，查询条件通过name变量传入
  -->
  <property name="nameQuery">
      <![CDATA[
       {
		 "size": 20,
         "query": {
              "query_string":{
                  "query":"(name:(*#[name,quoted=false]*))" ## 查询条件通过name变量传入
              }
          }
      }
      ]]>
  </property>
```

执行上面的检索：

```java
//普通检索
Map<String,String> params = new HashMap<String, String>();
params.put("name","高");
ESDatas<UserInfo> datas = clientInterface.searchList("user-info/_search","nameQuery",params,UserInfo.class);
List<UserInfo> userInfos = datas.getDatas();
System.out.println("打印普通检索结果:");
System.out.println(userInfos);
System.out.println("总记录条数："+datas.getTotalSize());

System.out.println("普通检索结果______________________________________结束");
```

实际执行返回的结果顺序如下，每一项得分都是(score)都是1.0：

```json
打印普通检索结果:
[{"name":"高 X","userId":"1","score":1.0}
, {"name":"高 XX","userId":"2","score":1.0}
, {"name":"X 高 X","userId":"3","score":1.0}
, {"name":"X 高 X","userId":"4","score":1.0}
, {"name":"XXX 高","userId":"5","score":1.0}
, {"name":"高 XXX","userId":"6","score":1.0}
, {"name":"XXX 高 X","userId":"7","score":1.0}
]
总记录条数：7
普通检索结果______________________________________结束
```

上面的结果达不到我们的预期效果。

## 自定义评分脚本检索

我们创建一个简单的脚本，通过doc['name'].value获取通讯录中的用户名称值，然后根据值出现的位置和相似度分别计算评分，将结果乘以不同的权重再相加。

在dsl 配置文件-esmapper/score.xml中增加名称为userInfoScore的自定义评分脚本：

```xml
<!--
    简单的query案例：定义评分脚本
    https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
-->
<property name="userInfoScore">
    <![CDATA[
     {
       "script": {
          "lang": "painless",
          "source": @"""
                double position_score = 0;
                double similarity_score = 0;
                //params.keyword对应查询dsl中传入的名称为keyword的参数
                int pos = doc['name'].value.indexOf(params.keyword); 
                if(pos != -1)
                {
                    position_score = 10 - pos;
                    if(position_score < 0) position_score = 0; // 出现位置大于10的忽略其重要性
                }
                double similarity = Math.abs(1.0*doc['name'].value.length() - params.keyword.length());
                similarity_score = 10 - similarity;
                if(similarity_score < 0) similarity_score = 0; // 相似度差10个字符的忽略其重要性
                // 在下面调节各分值的权重
                return position_score*0.6 + similarity_score * 0.4;
          """
        }
    }
    ]]>
</property>
```

根据定义在elasticsearch中创建id为user_info_score的脚本：

```java
private void createUserInfoScoreScript(){
   try {
      clientInterface.executeHttp("_scripts/user_info_score", ClientInterface.HTTP_DELETE);//删除user_info_score
   }
   catch(Exception e){
      e.printStackTrace();

   }
   clientInterface.executeHttp("_scripts/user_info_score", "userInfoScore",
                        ClientInterface.HTTP_POST);//创建评分脚本函数user_info_score

   String user_info_score = clientInterface.executeHttp("_scripts/user_info_score",
         ClientInterface.HTTP_GET);//获取刚才创建评分脚本函数user_info_score
   System.out.println(user_info_score);

}
```

接下来在dsl配置文件中定义一条采用id为user_info_score的脚本来对检索结果评分的dsl语句：

```xml
 <!--
    简单的query案例
    https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
    https://elasticsearch.cn/question/1890
    评分相似度文档：https://blog.csdn.net/qq_19598855/article/details/50581945
-->
 <property name="nameScriptScoreQuery">
     <![CDATA[{
       "size": 20,
       "query": {
         #*
           function_score查询是用来控制评分的终极武器，
		   它允许每个与主查询匹配的文档应用一个内置或自定义函数，已达到改变原始查询评分_score的目的。
         *# 
         "function_score": {  
             "query": {
                 "query_string":{
                     "query":"(name:(*#[name,quoted=false]*))" ## 根据传入的关键字条件进行检索
                 }
             },
			 #*
               script_score用于指定自定义脚本。params指定作为变量传递到脚本中的参数
             *# 	
             "script_score": {
                 "script": {
                     "id": "user_info_score", ## 通过user_info_score引用上面定义的评分painless脚本
                     "params": {
                         "keyword":#[name]  ## 传入评分脚本的关键字条件
                     }
                 }
             },
             #*
				boost_mode字段用来指定新计算的分数与_score的结合方式，取值可以是：
					multiply 相乘
					replace 替换
					sum 相加
					avg 取平均值
					max 取最大值
					min 取最小值
             *#
             "boost_mode": "sum"  ## 将计算得出的评分值与原始评分值相加
         }
       }
     }]]>
 </property>
```

执行自定义评分检索：

```java
Map<String,String> params = new HashMap<String, String>();
params.put("name","高");
//自定义评分函数检索
ESDatas<UserInfo> datas = clientInterface.searchList("user-info/_search","nameScriptScoreQuery",params,UserInfo.class);
userInfos = datas.getDatas();
System.out.println("自定义评分函数检索结果:");
System.out.println(userInfos);
System.out.println("总记录条数:"+datas.getTotalSize());

System.out.println("自定义评分函数检索______________________________________结束");
```

打印的检索结果如下：

```
自定义评分函数检索:
[{"name":"高 X","userId":"1","score":10.2}
, {"name":"高 XX","userId":"2","score":9.8}
, {"name":"高 XXX","userId":"6","score":9.4}
, {"name":"X 高 X","userId":"3","score":8.2}
, {"name":"X 高 X","userId":"4","score":8.2}
, {"name":"XXX 高","userId":"5","score":7.0}
, {"name":"XXX 高 X","userId":"7","score":6.2}
]
总记录条数:7
自定义评分函数检索______________________________________结束
```

从打印的结果可以看出，这次查询返回了我们期望的结果。

## 完整的测试用例和demo工程

上述所有功能的测试用例方法

```java
	@Test
	public void testFunctionScriptScore(){
		this.createUserInfoIndice();//创建通讯录索引
		this.createUserInfoScoreScript();//创建自定义评分脚本
		this.importUserInfoData();//导入测试数据
		this.queryUserInfo(); //执行普票查询和自定义评分查询，并打印查询结果
	}
```

可以把测试用例对应的工程拉取下来，运行testFunctionScriptScore方法：

[elasticsearch-example](https://github.com/bbossgroups/elasticsearch-example)

运行之前先修改src/main/resources/application.properties文件中的es地址：

```
elasticsearch.rest.hostNames=192.168.137.1:9200
```

对应的测试java类

[FunctionScriptScoreTest.java](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/score/FunctionScriptScoreTest.java)

值对象

[UserInfo.java](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/score/UserInfo.java)

dsl配置文件

[score.xml](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/score.xml)

# 总结

​	如果常规方式无法得到想要的评分结果，则可以脚本方式完全自己实现评分算法，以得到预期的评分结果。

​	function_score查询是用来控制评分的终极武器，它允许每个与主查询匹配的文档应用一个内置或自定义函数，已达到改变原始查询评分_score的目的。

​	script_score用于指定自定义脚本。params指定作为变量传递到脚本中的参数。

​	boost_mode字段用来指定新计算的分数与_score的结合方式，取值可以是：

​				multiply 相乘
​				replace 替换
​				sum 相加
​				avg 取平均值
​				max 取最大值
​				min 取最小值



# 相关资料

https://esdoc.bbossgroups.com/#/ElasticSearch-DSL-Script

[bboss dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl%E9%85%8D%E7%BD%AE%E8%A7%84%E8%8C%83)

https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html

https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html

https://elasticsearch.cn/question/1890
评分相似度文档：https://blog.csdn.net/qq_19598855/article/details/50581945



# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>

<img src="images/alipay.png"  height="200" width="200">

