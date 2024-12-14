# Elasticsearch JDBC案例介绍

The best elasticsearch highlevel java rest api-----bboss     

Elasticsearch  6.3以后的版本可以通过jdbc操作es，该功能还在不断的完善当中，本文介绍es jdbc使用方法。

# 1.首先在工程中导入es jdbc maven坐标：

```xml
导入elasticsearch jdbc驱动和bboss持久层

<dependency> 
    <groupId>com.bbossgroups</groupId> 
    <artifactId>bboss-persistent</artifactId> 
    <version>6.2.7</version> 
</dependency> 

在pom中添加elastic maven库 

<repositories>
  <repository>
    <id>elastic.co</id>
    <url>https://artifacts.elastic.co/maven</url>
  </repository>
</repositories>
```

如果是Elasticsearch 6.3.x(版本号务必与elasticsearch版本号保持一致)，导入下面的坐标：

```xml
<dependency>
  <groupId>org.elasticsearch.plugin</groupId>
  <artifactId>jdbc</artifactId>
  <version>7.3.0</version>
</dependency>
```

如果是Elasticsearch 6.4.x.6.5.x,7.x(版本号务必与elasticsearch版本号保持一致)，导入以下坐标：

```xml
<dependency>
  <groupId>org.elasticsearch.plugin</groupId>
  <artifactId>x-pack-sql-jdbc</artifactId>
  <version>7.10.0</version>
</dependency>
```

# 2.通过jdbc驱动执行elasticsearch sql相关功能

- 启动es数据源
- 执行elasticsearch sql相关功能

直接看执行各种sql功能的代码[ESJdbcTest](https://github.com/bbossgroups/bestpractice/blob/master/persistent/src/com/frameworkset/sqlexecutor/ESJdbcTest.java)：

```java
package com.frameworkset.sqlexecutor;
/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.util.SQLUtil;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class ESJdbcTest {
    //启动数据源，初始化数据源
	@Before
	public void initDBSource(){
//		SQLUtil.startPool("es",//ES数据源名称 for 6.3.x
//				"org.elasticsearch.xpack.sql.jdbc.jdbc.JdbcDriver",//ES jdbc驱动
//				"jdbc:es://http://192.168.137.1:9200?timezone=UTC&page.size=250",//es链接串
//				"elastic","changeme",//es x-pack账号和口令
//				"SELECT 1 AS result" //数据源连接校验sql
//		);
//		SQLUtil.startPool("es",//ES数据源名称 for Elasticsearch 6.4.x,+
//				"org.elasticsearch.xpack.sql.jdbc.EsDriver",//ES jdbc驱动
//				"jdbc:es://http://192.168.137.1:9200?timezone=UTC&page.size=250",//es链接串
//				"elastic","changeme",//es x-pack账号和口令
//				null,//"false",
//				null,// "READ_UNCOMMITTED",
//				"SELECT 1 AS result", //数据源连接校验sql
//				 "es_jndi",
//				10,
//				10,
//				20,
//				true,
//				false,
//				null, true, false,10000,"es7","com.frameworkset.sqlexecutor.DBElasticsearch7"
//		);
		SQLUtil.startPool("es",//ES数据源名称 for Elasticsearch 6.4.x,+
				"org.elasticsearch.xpack.sql.jdbc.EsDriver",//ES jdbc驱动
				"jdbc:es://http://192.168.137.1:9200?timezone=UTC&page.size=250",//es链接串
				"elastic","changeme",//es x-pack账号和口令
				"SELECT 1 AS result" //数据源连接校验sql
		);
	}

	/**
	 * 执行一个查询
	 * @throws SQLException
	 */
	@Test
	public void testSelect() throws SQLException {
		 
		//执行查询，将结果映射为HashMap集合
		 List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SELECT SCORE() as score,* FROM dbclobdemo ");
		 System.out.println(data);

		data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SELECT SCORE() as score,* FROM dbclobdemo where detailtemplateId=?",1);
		System.out.println(data);
	}

	@Test
	public void testQuery() throws SQLException {
		initDBSource();//启动数据源
		//执行查询，将结果映射为HashMap集合,全文检索查询
		List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,
				"es","SELECT * FROM hawkeye-auth-service-web-api-index-2018-06-30 where match(url_group,'synchronize_info')");
		System.out.println(data);
		//关键词精确查找
		data =	SQLExecutor.queryListWithDBName(HashMap.class,
				"es","SELECT * FROM hawkeye-auth-service-web-api-index-2018-06-30 where url_group.keyword = ?","synchronize_info");
		System.out.println(data);
	}

	/**
	 * 进行模糊搜索，Elasticsearch 的搜索能力大家都知道，强！在 SQL 里面，可以用 match 关键字来写，如下：
	 * @throws SQLException
	 */
	@Test
	public void testMatchQuery() throws SQLException {
		 
		List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SELECT SCORE(), * FROM dbclobdemo WHERE match(content, '_ewebeditor_pa_src') ORDER BY documentId DESC");
		System.out.println(data);

		/**
		 *还能试试 SELECT 里面的一些其他操作，如过滤，别名，如下：
		 */
		data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SELECT SCORE() as score,title as myname FROM dbclobdemo  as mytable WHERE match(content, '_ewebeditor_pa_src') and (title.keyword = 'adsf' OR title.keyword ='elastic') limit 5 ");
		System.out.println(data);
	}
	/**
	 * 分组和函数计算
	 */
	@Test
	public void testGroupQuery() throws SQLException {
		 
		List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SELECT title.keyword,max(documentId) as max_id FROM dbclobdemo as mytable group by title.keyword limit 5");
		System.out.println(data);


	}


	/**
	 * 查看所有的索引表
	 * @throws SQLException
	 */
	@Test
	public void testShowTable() throws SQLException {
		 
		List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SHOW tables");
		System.out.println(data);
	}

	/**
	 * 如 dbclob 开头的索引，注意通配符只支持 %和 _，分别表示多个和单个字符（什么，不记得了，回去翻数据库的书去！）
	 * @throws SQLException
	 */
	@Test
	public void testShowTablePattern() throws SQLException {
		 
		List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SHOW tables 'dbclob_'");
		System.out.println(data);
		data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SHOW tables 'dbclob%'");
		System.out.println(data);
	}
	/**
	 * 查看索引的字段和元数据
	 * @throws SQLException
	 */
	@Test
	public void testDescTable() throws SQLException {
		 
		List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","DESC dbclobdemo");
		System.out.println(data);
		data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SHOW COLUMNS IN dbclobdemo");
		System.out.println(data);
	}

	/**
	 * 不记得 ES 支持哪些函数，只需要执行下面的命令，即可得到完整列表
	 * @throws SQLException
	 */
	@Test
	public void testShowFunctin() throws SQLException {
		 
		List<HashMap> data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SHOW FUNCTIONS");
		System.out.println(data);
		//同样支持通配符进行过滤：
		data =	SQLExecutor.queryListWithDBName(HashMap.class,"es","SHOW FUNCTIONS 'S__'");
		System.out.println(data);

	}
}

```

如果执行的时候报错：

![img](https://oscimg.oschina.net/oscnet/b84c48aeb829871a1b394a42027b8b1a52c.jpg)

可以采用正式的license或者在elasticsearch.yml文件最后添加以下配置即可：

**xpack.license.self_generated.type: trial**





# 3.bboss 针对es jdbc的替代解决方案

 

bboss 提供一组sql和fetchQuery API，可替代es jdbc模块；采用bboss即可拥有bboss的客户端自动发现和容灾能力、对es、jdk、spring boot的兼容性能力，又可以拥有es jdbc的所有功能，同时还解决了因为引入es jdbc导致项目对es版本的强依赖和兼容性问题，参考demo：

orm查询
<https://github.com/bbossgroups/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/sql/SQLOrmTest.java>
分页查询
<https://github.com/bbossgroups/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/sql/SQLPagineTest.java>





# 开发交流

elasticsearch sql官方文档：

<https://www.elastic.co/guide/en/elasticsearch/reference/current/xpack-sql.html>

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />



交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">





