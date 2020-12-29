## Elasticsearch  SQL功能使用案例分享

**The best elasticsearch highlevel java rest api-----bboss**       

Elasticsearch 6.3及以上版本新增的SQL功能非常不错，本文以实际案例来介绍其使用方法：

- 通过sql实现检索功能（代码中直接操作sql，从配置中加载sql）
- 将sql转换为dsl功能
- 使用es jdbc
- 准备工作：[集成Elasticsearch Restful API](common-project-with-bboss.md)

另外一篇文章：[Elasticsearch SQL ORM查询案例](https://esdoc.bbossgroups.com/#/Elasticsearch-SQL-ORM)

# 1.代码中的sql检索

```java
    @Test
	public void testQuery(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		String json = clientUtil.executeHttp("/_xpack/sql?format=txt",
				"{\"query\": \"SELECT * FROM dbclobdemo\"}",
				ClientInterface.HTTP_POST
				);
		System.out.println(json);

		json = clientUtil.executeHttp("/_xpack/sql?format=json",
				"{\"query\": \"SELECT * FROM dbclobdemo\"}",
				ClientInterface.HTTP_POST
		);
		System.out.println(json);
	}
```

执行的结果在本文的最后给出。





# 2.sql转换为dsl

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





# 3.配置文件管理sql并实现sql检索

定义一个包含sql的dsl配置文件，sql语句中包含一个channelId检索条件：

```xml
<properties>
    <!--
        sql query
    -->
    <property name="sqlQuery">
        <![CDATA[
         {"query": "SELECT * FROM dbclobdemo where channelId=#[channelId] and name='#[name,quoted=false]'"} ##加上quoted=false属性，指示框架不要为字符串加""号，因为sql需要''号
        ]]>
    </property>
</properties>
```

加载配置文件并实现sql检索操作 ，从外部传入检索的条件channelId

```java
    public void testSQLQueryFromDSL(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/sql.xml");//初始化一个加载sql配置文件的es客户端接口
		//设置sql查询的参数
		Map params = new HashMap();
		params.put("channelId",1);
        params.put("name","乔丹");
		String json = clientUtil.executeHttp("/_xpack/sql","sqlQuery",params,
				ClientInterface.HTTP_POST
		);
		System.out.println(json);//打印检索结果

	}
```

输出检索的结果为：

```json
{
    "columns": [
        {
            "name": "auditflag",
            "type": "long"
        },
        {
            "name": "author",
            "type": "text"
        },
        {
            "name": "channelId",
            "type": "long"
        },
        {
            "name": "content",
            "type": "text"
        },
        {
            "name": "count",
            "type": "long"
        },
        {
            "name": "createtime",
            "type": "date"
        },
        {
            "name": "createuser",
            "type": "long"
        },
        {
            "name": "detailtemplateId",
            "type": "long"
        },
        {
            "name": "docClass",
            "type": "text"
        },
        {
            "name": "docLevel",
            "type": "long"
        },
        {
            "name": "docabstract",
            "type": "text"
        },
        {
            "name": "docsourceId",
            "type": "long"
        },
        {
            "name": "doctype",
            "type": "long"
        },
        {
            "name": "documentId",
            "type": "long"
        },
        {
            "name": "docwtime",
            "type": "date"
        },
        {
            "name": "flowId",
            "type": "long"
        },
        {
            "name": "isdeleted",
            "type": "long"
        },
        {
            "name": "isnew",
            "type": "long"
        },
        {
            "name": "keywords",
            "type": "text"
        },
        {
            "name": "mediapath",
            "type": "text"
        },
        {
            "name": "newpicPath",
            "type": "text"
        },
        {
            "name": "ordertime",
            "type": "date"
        },
        {
            "name": "parentDetailTpl",
            "type": "text"
        },
        {
            "name": "picPath",
            "type": "text"
        },
        {
            "name": "publishfilename",
            "type": "text"
        },
        {
            "name": "publishtime",
            "type": "date"
        },
        {
            "name": "secondtitle",
            "type": "text"
        },
        {
            "name": "seq",
            "type": "long"
        },
        {
            "name": "status",
            "type": "long"
        },
        {
            "name": "subtitle",
            "type": "text"
        },
        {
            "name": "title",
            "type": "text"
        },
        {
            "name": "titlecolor",
            "type": "text"
        },
        {
            "name": "version",
            "type": "long"
        }
    ],
    "rows": [
        [
            0,
            "不详",
            1,
            "asdfasdfasdfasdfsdf<img name=\"imgs\" src=\"../gencode7.png\" _ewebeditor_pa_src=\"http%3A%2F%2Flocalhost%2Fcms%2FsiteResource%2Ftest%2F_webprj%2Fgencode7.png\"><br>\r\nasdfasdf<img name=\"imgs\" src=\"content_files/20180505101457109.png\" _ewebeditor_pa_src=\"http%3A%2F%2Flocalhost%2Fcms%2FsiteResource%2Ftest%2F_webprj%2Fnews%2Fcontent_files%2F20180505101457109.png\"><br>\r\n<br>",
            0,
            "2018-04-12T14:16:02.000Z",
            1,
            1,
            "普通分类",
            1,
            "无asdfasdf",
            1,
            0,
            1,
            "2018-05-06T03:30:04.000Z",
            2,
            0,
            0,
            "news",
            "uploadfiles/201803/gencode4.png",
            "",
            "2018-04-12T14:06:45.000Z",
            "1",
            "uploadfiles/201803/gencode1.png",
            "asdf.html",
            "2018-04-14T14:36:12.000Z",
            "",
            0,
            11,
            "asdf",
            "adsf",
            "#000000",
            1
        ]
    ]
}
```





# 4 使用es jdbc

可以通过jdbc操作es，请访问文档：

# [Elasticsearch JDBC案例介绍](https://my.oschina.net/bboss/blog/2221868)



# 5.完整的demo

<https://github.com/bbossgroups/eshelloword-booter>


# 6.开发交流

elasticsearch sql官方文档：

<https://www.elastic.co/guide/en/elasticsearch/reference/current/xpack-sql.html>

bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



