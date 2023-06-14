# Elasticsearch SQL ORM查询案例

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 



bboss ES SQL是针对[es jdbc](Elasticsearch-JDBC.md)的替代解决方案

**the best elasticsearch highlevel java rest api-----bboss**     

bboss 提供一组sql和fetchQuery API，可替代官方es jdbc模块；采用bboss即可拥有bboss的客户端自动发现和容灾能力、对es、jdk、spring boot的兼容性能力，又可以拥有es jdbc的所有功能，同时还解决了因为引入es jdbc导致项目对es版本的强依赖和兼容性问题，参考demo：

orm查询
<https://github.com/bbossgroups/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/sql/SQLOrmTest.java>

分页查询
<https://github.com/bbossgroups/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/sql/SQLPagineTest.java>

Elasticsearch 目前有两个sql插件

- 官方:[xpack-sql插件](https://www.elastic.co/guide/en/elasticsearch/reference/current/xpack-sql.html)

- 第三方：[Elasticsearch-sql插件](https://github.com/NLPchina/elasticsearch-sql)


官方的ES-SQL功能必须Elasticsearch 6.3以上的版本才提供；Elasticsearch-SQL插件可以在不同的Elasticsearch版本上运行，可以根据实际情况进行选择。

下面结合案例介绍两个插件的使用方法：

- 集合orm查询
- 单文orm档查询
- fetchsize orm查询 
- [Elasticsearch-sql](https://github.com/NLPchina/elasticsearch-sql)查询



# 1 官方xpack-sql orm查询



## 1.1 定义orm查询的实体bean

```java
package org.bboss.elasticsearchtest.sql;

import com.frameworkset.orm.annotation.Column;

import java.util.Date;

public class DocObject {
	private int isnew;
	private Date createtime;
	private String content;
	private int documentId;
	private int channelId;

	/**
	 * 通过column指定索引文档和对象属性的映射关系
	 * 通过column注解还可以指定日期格式和时区信息
	 * @Column(name="docInfo.author",dataformat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",timezone = "Etc/UTC",locale = "zh")
	 *
	 */
	@Column(name="docInfo.author")
	private String docInfoAuthor;

	public int getIsnew() {
		return isnew;
	}

	public void setIsnew(int isnew) {
		this.isnew = isnew;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getDocumentId() {
		return documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public String getDocInfoAuthor() {
		return docInfoAuthor;
	}

	public void setDocInfoAuthor(String docInfoAuthor) {
		this.docInfoAuthor = docInfoAuthor;
	}
}
```

实体定义说明：

通过column指定索引文档和对象属性的映射关系，指定日期格式和时区信息,示例如下：

```java
@Column(name="docInfo.author")
private String docInfoAuthor;
```

指定属性的映射关系、日期格式和时区信息,示例如下：    

```java
 @Column(name="docInfo.author",dataformat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",timezone = "Etc/UTC",locale = "zh")
```



## 1.2 执行orm查询

以rest sql api为例来介绍es 6.9.9的sql orm查询功能

```java
package org.bboss.elasticsearchtest.sql;

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.sql.SQLRestResponse;
import org.frameworkset.elasticsearch.entity.sql.SQLRestResponseHandler;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 以rest sql api为例来介绍es 6.9.9的sql orm查询功能
 */
public class SQLOrmTest {

	/**
	 * 代码中的sql检索，返回Map类型集合，亦可以返回自定义的对象集合
	 */
	@Test
	public void testDemoQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		List<Map> json = clientUtil.sql(Map.class,"{\"query\": \"SELECT * FROM demo\"}");


		System.out.println(json);
	}

	/**
	 * 代码中的sql检索，返回Map类型集合，亦可以返回自定义的对象集合
	 */
	@Test
	public void testMapQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		List<Map> json = clientUtil.sql(Map.class,"{\"query\": \"SELECT * FROM dbclobdemo\"}");


		System.out.println(json);
	}
	/**
	 * 配置文件中的sql dsl检索,返回Map类型集合，亦可以返回自定义的对象集合
	 */
	@Test
	public void testMapSQLQueryFromDSL(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数
		Map params = new HashMap();
		params.put("channelId",1);
		List<Map> json = clientUtil.sql(Map.class,"sqlQuery",params);
		System.out.println(json);

	}

	/**
	 * 代码中的sql检索，返回Map类型对象，亦可以返回自定义的对象
	 */
	@Test
	public void testMapObjectQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		Map json = clientUtil.sqlObject(Map.class,"{\"query\": \"SELECT * FROM dbclobdemo\"}");


		System.out.println(json);
	}
	/**
	 * 配置文件中的sql dsl检索,返回Map类型对象，亦可以返回自定义的对象
	 */
	@Test
	public void testMapObjectSQLQueryFromDSL(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数
		Map params = new HashMap();
		params.put("channelId",1);
		Map json = clientUtil.sqlObject(Map.class,"sqlQuery",params);
		System.out.println(json);

	}


	/**
	 * 代码中的sql检索，返回DocObject 类型集合
	 */
	@Test
	public void testObjectListQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		List<DocObject> json = clientUtil.sql(DocObject.class,"{\"query\": \"SELECT * FROM dbclobdemo\"}");


		System.out.println(json);
	}
	/**
	 * 配置文件中的sql dsl检索,返回DocObject 类型集合
	 */
	@Test
	public void testObjectSQLQueryFromDSL(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数
		Map params = new HashMap();
		params.put("channelId",1);
		List<DocObject> json = clientUtil.sql(DocObject.class,"sqlQuery",params);
		System.out.println(json);

	}

	/**
	 * 代码中的sql检索，返回DocObject 类型对象
	 */
	@Test
	public void testObjectQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		DocObject json = clientUtil.sqlObject(DocObject.class,"{\"query\": \"SELECT * FROM dbclobdemo where documentId = 1\"}");
		System.out.println(json);
	}
	/**
	 * 配置文件中的sql dsl检索,返回DocObject 类型对象
	 */
	@Test
	public void testConditionObjectSQLQueryFromDSL(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数
		Map params = new HashMap();
		params.put("channelId",1);
		DocObject json = clientUtil.sqlObject(DocObject.class,"sqlQuery",params);
		System.out.println(json);

	}
	/**
	 * sql转换为dsl
	 */
	@Test
	public void testTranslate(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		String json = clientUtil.executeHttp("/_xpack/sql/translate",
				"{\"query\": \"SELECT * FROM dbclobdemo limit 5\",\"fetch_size\": 5}",
				ClientInterface.HTTP_POST
		);
		System.out.println(json);

	}

	/**
	 * 低阶的检索方法
	 */
	@Test
	public void testSQLRestResponse(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		SQLRestResponse sqlRestResponse = clientUtil.executeHttp("/_xpack/sql",
																	"{\"query\": \"SELECT * FROM dbclobdemo where documentId = 1\"}",
																	ClientInterface.HTTP_POST,
																		new SQLRestResponseHandler());
		System.out.println(sqlRestResponse);
	}


}
```

每个orm查询方法的都有对应的功能注释说明，可以根据需要使用相关的方法。

代码中用到的sql dsl脚本配置文件及内容：esmapper/sql.xml

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


    <property name="sqlQueryWithStringParam">
        <![CDATA[
         {"query": "SELECT * FROM dbclobdemo where channelId=#[channelId] and docTitle='#[channelId,quoted=false]'"} ## sql中特定的字符串参数语法
        ]]>
    </property>
    <!--
        分页sql query
        每页显示 fetch_size对应的记录条数

    -->
    <property name="sqlPagineQuery">
        <![CDATA[
         {
         ## 指示sql语句中的回车换行符会被替换掉开始符,注意dsl注释不能放到sql语句中，否则会有问题，因为sql中的回车换行符会被去掉，导致回车换行符后面的语句变道与注释一行
         ##  导致dsl模板解析的时候部分sql段会被去掉
            "query": #"""
                    SELECT * FROM dbclobdemo



                        where channelId=#[channelId]
             """,
             ## 指示sql语句中的回车换行符会被替换掉结束符
            "fetch_size": #[fetchSize]
         }
        ]]>
    </property>

</properties>
```

我们将配置文件放到工程resources目录下面即可。sql配置说明：

sql中特定的字符串参数语法

```json
{"query": "SELECT * FROM dbclobdemo where channelId=#[channelId] and docTitle='#[channelId,quoted=false]'"}
```

我们使用#[xxx]类型变量传递sql参数时，如果是字符串内容会自动在值的两边带上双引号，但是在sql语句是字符串值是用单引号'来标识的，所以通过qutoed=false来指示解析引擎不要在值的两边加双引号，然后在外部手动添加单引号：

'#[channelId,quoted=false]'

如果sql语句比较长，可能要换行，es暂时不支持多行sql语句的执行，bboss通过下面特定的语法，来包围多行sql，sql解析引擎在第一次解析sql的时候讲其中的多行sql解析为一行：

\#"""

...

...

"""

例如：

```json
{
## 指示sql语句中的回车换行符会被替换掉开始符,注意dsl注释不能放到sql语句中，否则会有问题，因为sql中的回车换行符会被去掉，导致回车换行符后面的语句变道与注释一行
##  导致dsl模板解析的时候部分sql段会被去掉
   "query": #"""
           SELECT * FROM dbclobdemo



               where channelId=#[channelId]
    """,
    ## 指示sql语句中的回车换行符会被替换掉结束符
   "fetch_size": #[fetchSize]
}
```



## 1.3 通过fetch_size实现分页查询

```java
package org.bboss.elasticsearchtest.sql;

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.sql.SQLResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLPagineTest {
	/**
	 * 代码中的sql检索，返回Map类型集合，亦可以返回自定义的对象集合
	 */
	@Test
	public void testMapQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		SQLResult<Map> sqlResult = clientUtil.fetchQuery(Map.class,"{\"query\": \"SELECT * FROM dbclobdemo\",\"fetch_size\": 1}");

		do{
			List<Map> datas = sqlResult.getDatas();
			if(datas == null || datas.size() == 0){
				break;
			}
			else{
				System.out.println(datas.size());//处理数据
				sqlResult = sqlResult.nextPage();//获取下一页数据

			}

		}while(true);



	}
	/**
	 * 配置文件中的sql dsl检索,返回Map类型集合，亦可以返回自定义的对象集合
	 */
	@Test
	public void testMapSQLQueryFromDSL(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数
		Map params = new HashMap();
		params.put("channelId",1);
		params.put("fetchSize",1);
		SQLResult<Map> sqlResult = clientUtil.fetchQuery(Map.class,"sqlPagineQuery",params);

		do{
			List<Map> datas = sqlResult.getDatas();
			if(datas == null || datas.size() == 0){
				break;
			}
			else{
				System.out.println(datas.size());//处理数据
				sqlResult = sqlResult.nextPage();//获取下一页数据

			}

		}while(true);

	}




	/**
	 * 代码中的sql检索，返回DocObject 类型集合 
	 */
	@Test
	public void testObjectListQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();

		SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"{\"query\": \"SELECT * FROM dbclobdemo\",\"fetch_size\": 1}");

		do{
			List<DocObject> datas = sqlResult.getDatas();
			if(datas == null || datas.size() == 0){
				break;
			}
			else{
				System.out.println(datas.size());//处理数据
				sqlResult = sqlResult.nextPage();//获取下一页数据

			}

		}while(true);


	}


	/**
	 * 配置文件中的sql dsl检索,返回DocObject 类型集合 
	 */
	@Test
	public void testObjectSQLQueryFromDSL(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数

		Map params = new HashMap();
		params.put("channelId",1);
		params.put("fetchSize",1);
		SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"sqlPagineQuery",params);

		do{
			List<DocObject> datas = sqlResult.getDatas();
			if(datas == null || datas.size() == 0){
				System.out.println(0);//处理数据
				break;
			}
			else{
				System.out.println(datas.size());//处理数据
				sqlResult = sqlResult.nextPage();//获取下一页数据

			}

		}while(true);

	}

	/**
	 * 配置文件中的sql dsl检索,返回DocObject 类型集合 
	 */
	@Test
	public void testObjectSQLQueryFromDSL1(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数

		Map params = new HashMap();
		params.put("channelId",1);
		params.put("fetchSize",1);
		SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"sqlPagineQuery",params);

		do{
			List<DocObject> datas = sqlResult.getDatas();
			if(datas == null || datas.size() == 0){
				System.out.println(0);//处理数据
				break;
			}
			else{
				System.out.println(datas.size());//处理数据
				sqlResult = clientUtil.fetchQueryByCursor(DocObject.class,sqlResult);//获取下一页数据,通过api获取下一页数据

			}

		}while(true);

	}

	/**
	 * 配置文件中的sql dsl检索,返回DocObject 类型集合 
	 * 测试没有返回数据的情况
	 */
	@Test
	public void testNodataSQLQueryFromDSL1(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数

		Map params = new HashMap();
		params.put("channelId",2);
		params.put("fetchSize",1);
		SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"sqlPagineQuery",params);


		do{
			List<DocObject> datas = sqlResult.getDatas();
			if(datas == null || datas.size() == 0){
				System.out.println(0);//处理数据
				break;
			}
			else{
				System.out.println(datas.size());//处理数据
				sqlResult = clientUtil.fetchQueryByCursor(DocObject.class,sqlResult);//获取下一页数据,通过api获取下一页数据

			}

		}while(true);

	}

	/**
	 * 配置文件中的sql dsl检索,返回DocObject 类型集合 
	 */
	@Test
	public void testObjectSQLQueryFromDSL2(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数

		Map params = new HashMap();
		params.put("channelId",1);
		params.put("fetchSize",1);
		SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"sqlPagineQuery",params);

		do{
			List<DocObject> datas = sqlResult.getDatas();
			if(datas == null || datas.size() == 0){
				System.out.println(0);//处理数据
				break;
			}
			else{
				System.out.println(datas.size());//处理数据
				sqlResult = clientUtil.fetchQueryByCursor(DocObject.class,sqlResult.getCursor(),sqlResult.getColumns());//获取下一页数据,通过api获取下一页数据

			}

		}while(true);

	}

	/**
	 * 代码中的sql检索，返回 DocObject类型集合
	 */
	@Test
	public void testCloseCursor(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();

		SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"{\"query\": \"SELECT * FROM dbclobdemo\",\"fetch_size\": 1}");
		List<DocObject> datas = sqlResult.getDatas();
		System.out.println(datas.size());//处理数据
		System.out.println(sqlResult.closeCursor());//只处理第一页数据，就主动关闭分页游标
	}

	/**
	 * 代码中的sql检索，返回DocObject类型集合
	 */
	@Test
	public void testCloseCursor1(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();

		SQLResult<DocObject> sqlResult = clientUtil.fetchQuery(DocObject.class,"{\"query\": \"SELECT * FROM dbclobdemo\",\"fetch_size\": 1}");
		List<DocObject> datas = sqlResult.getDatas();
		System.out.println(datas.size());//处理数据
		String ret = clientUtil.closeSQLCursor(sqlResult.getCursor());
		System.out.println(ret);//只处理第一页数据，就主动关闭分页游标

	}
}
```

## 1.4 sql转换为dsl

可以将sql转换为dsl语句

```java
   public void testTranslate(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		String json = clientUtil.executeHttp("/_xpack/sql/translate",
				"{\"query\": \"SELECT * FROM dbclobdemo\"}",
				ClientInterface.HTTP_POST
		);
		System.out.println(json);

	}
```

sql转换为dsl的结果：

```json
{
    "size": 1000,
    "_source": {
        "includes": [
            "author",
            "content",
            "docClass",
            "docabstract",
            "keywords",
            "mediapath",
            "newpicPath",
            "parentDetailTpl",
            "picPath",
            "publishfilename",
            "secondtitle",
            "subtitle",
            "title",
            "titlecolor"
        ],
        "excludes": []
    },
    "docvalue_fields": [
        "auditflag",
        "channelId",
        "count",
        "createtime",
        "createuser",
        "detailtemplateId",
        "docLevel",
        "docsourceId",
        "doctype",
        "documentId",
        "docwtime",
        "flowId",
        "isdeleted",
        "isnew",
        "ordertime",
        "publishtime",
        "seq",
        "status",
        "version"
    ],
    "sort": [
        {
            "_doc": {
                "order": "asc"
            }
        }
    ]
}
```

# 2 第三方插件Elasticsearch-sql查询

基于第三方[Elasticsearch-sql](https://github.com/NLPchina/elasticsearch-sql)插件的查询功能的使用方法和bboss提供的查询api使用方法一致，只是检索的rest服务换成/_sql服务即可。

如果需要使用本插件，请自行下载安装Elasticsearch-sql插件并安装，下载地址：

https://github.com/NLPchina/elasticsearch-sql

## 2.1 简单案例

```java
    /**
	 * Elasticsearch-SQL插件功能测试方法
	 */
	public void testESSQL(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas =  //ESDatas包含当前检索的记录集合，最多10条记录，由sql中的limit属性指定
				clientUtil.searchList("/_sql",//sql请求
						"select * from vem_order_index_2018 limit 0,10", //elasticsearch-sql支持的sql语句
						Map.class);//返回的文档封装对象类型
		//获取结果对象列表
		List<Map> demos = esDatas.getDatas();

		//获取总记录数
		long totalSize = esDatas.getTotalSize();
		System.out.println(totalSize);
	}
```

## 2.2 通过配置文件管理sql语句案例

```java
/**
 * Elasticsearch-SQL插件功能测试方法
 */
@Test
public void testESSQLFromConf(){
   ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");
   ESDatas<Map> esDatas =  //ESDatas包含当前检索的记录集合，最多10条记录，由sql中的limit属性指定
         clientUtil.searchList("/_sql",//sql请求
               "testESSQL", //elasticsearch-sql支持的sql语句
               Map.class);//返回的文档封装对象类型    
   
   //获取结果对象列表
   List<Map> demos = esDatas.getDatas();

   //获取总记录数
   long totalSize = esDatas.getTotalSize();
   System.out.println(totalSize);
}
```

对应的配置文件设置：

```xml
<!--for elasticsearch sqlplugin -->
<property name="testESSQL">
    <![CDATA[
     select operModule.keyword from dbdemo group by operModule.keyword
    ]]>
</property>
```

## 2.3 带参数的配置文件案例

```
/**
 * Elasticsearch-SQL插件功能测试方法，带参数sql
 */
@Test
public void testESSQLFromConfParams(){
   ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");
   Map params = new HashMap();
   params.put("operModule","xxxx");
   ESDatas<Map> esDatas =  //ESDatas包含当前检索的记录集合，最多10条记录，由sql中的limit属性指定
         clientUtil.searchList("/_sql",//sql请求
               "testESSQL", //elasticsearch-sql支持的sql语句
               params, //检索参数
               Map.class);//返回的文档封装对象类型
   //获取结果对象列表
   List<Map> demos = esDatas.getDatas();

   //获取总记录数
   long totalSize = esDatas.getTotalSize();
   System.out.println(totalSize);
}
```

对应的sql配置：

```xml
<!--for elasticsearch sqlplugin -->
<property name="testESSQLWithParams">
    <![CDATA[
     select operModule.keyword from dbdemo group by operModule.keyword where operModule.keyword=#[operModule]
    ]]>
</property>
```

## 2.4 Elasticsearch-SQL插件sql转dsl功能

```java
/**
 * Elasticsearch-SQL插件功能:sql转dsl
 */
@Test
public void testESSQLTranslate(){
   ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
   String dsl =  //将sql转换为dsl
         clientUtil.executeHttp("/_sql/_explain",//sql转dsl请求
               "select operModule.keyword from dbdemo group by operModule.keyword ",ClientInterface.HTTP_POST);//返回的转换的结果

   //获取总记录数
   System.out.println(dsl);
}
```





# 3 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



