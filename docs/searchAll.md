# 一组获取Elasticsearch 索引表所有文档API使用案例

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

*The best elasticsearch highlevel java rest api-----bboss* 

一组获取Elasticsearch 索引表所有文档API使用案例

- 统计文档总数量api
- 简单获取所有文档api
- 并行获取所有文档api

直接看案例：
[SearchAllTest](https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/searchall/SearchAllTest.java)
```java
package org.bboss.elasticsearchtest.searchall;


import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.scroll.HandlerInfo;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * <p>Description: 检索所有文档数据测试用例</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/10/14 20:07
 * @author biaoping.yin
 * @version 1.0
 */
public class SearchAllTest {
	/**
	 * 统计索引中有多少文档
	 */
	@Test
	public void testCountAll(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		long esDatas = clientInterface.countAll("demo");
		System.out.println("TotalSize:"+esDatas);

	}
	/**
	 * 简单的检索索引表所有文档数据，默认分5000条记录一批从es获取数据
	 */
	@Test
	public void testSearchAll(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAll("demo",Map.class);
		List<Map> dataList = esDatas.getDatas();
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
	/**
	 * 简单的检索索引表所有文档数据，按指定的10000条记录一批从es获取数据
	 */
	@Test
	public void testSearchAllFethchSize(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAll("demo",10000,Map.class);
		List<Map> dataList = esDatas.getDatas();
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
	/**
	 * 检索索引表所有文档数据，默认分5000条记录一批从es获取数据，分批获取的数据交个一ScrollHandler来处理
	 */
	@Test
	public void testSearchAllHandler(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAll("demo", new ScrollHandler<Map>() {
			public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
				List<Map> dataList = esDatas.getDatas();
				System.out.println("TotalSize:"+esDatas.getTotalSize());
				if(dataList != null) {
					System.out.println("dataList.size:" + dataList.size());
				}
				else
				{
					System.out.println("dataList.size:0");
				}
			}
		},Map.class);
		List<Map> dataList = esDatas.getDatas();//数据已经被行处理器处理，所以这里不会有数据返回
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
	/**
	 * 检索索引表所有文档数据，按指定的10000条记录一批从es获取数据，分批获取的数据交个一ScrollHandler来处理
	 */
	@Test
	public void testSearchAllFethchSizeHandler(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAll("demo",10000,new ScrollHandler<Map>() {
			public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
				List<Map> dataList = esDatas.getDatas();
				System.out.println("TotalSize:"+esDatas.getTotalSize());
				if(dataList != null) {
					System.out.println("dataList.size:" + dataList.size());
				}
				else
				{
					System.out.println("dataList.size:0");
				}
			}
		},Map.class);
		List<Map> dataList = esDatas.getDatas();//数据已经被行处理器处理，所以这里不会有数据返回
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
	/**
	 * 并行检索索引表所有文档数据，默认分5000条记录一批从es获取数据，指定了并行的线程数为6
	 */
	@Test
	public void testSearchAllParrrel(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAllParallel("demo",Map.class,6);
		List<Map> dataList = esDatas.getDatas();
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
	/**
	 * 并行检索索引表所有文档数据，按指定的10000条记录一批从es获取数据，指定了并行的线程数为6
	 */
	@Test
	public void testSearchAllFethchSizeParrrel(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAllParallel("demo",100,Map.class,6);
		List<Map> dataList = esDatas.getDatas();
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
	/**
	 * 并行检索索引表所有文档数据，默认分5000条记录一批从es获取数据，分批获取的数据交个一ScrollHandler来处理，指定了并行的线程数为6
	 */
	@Test
	public void testSearchAllHandlerParrrel(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAllParallel("demo", new ScrollHandler<Map>() {
			public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
				List<Map> dataList = esDatas.getDatas();
				System.out.println("TotalSize:"+esDatas.getTotalSize());
				if(dataList != null) {
					System.out.println("dataList.size:" + dataList.size());
				}
				else
				{
					System.out.println("dataList.size:0");
				}
			}
		},Map.class,6);
		List<Map> dataList = esDatas.getDatas();//数据已经被行处理器处理，所以这里不会有数据返回
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
	/**
	 * 并行检索索引表所有文档数据，按指定的10000条记录一批从es获取数据，分批获取的数据交个一ScrollHandler来处理，指定了并行的线程数为6
	 */
	@Test
	public void testSearchAllFethchSizeHandlerParrrel(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
		ESDatas<Map> esDatas = clientInterface.searchAllParallel("demo",10000,new ScrollHandler<Map>() {
			public void handle(ESDatas<Map> esDatas, HandlerInfo handlerInfo) throws Exception {
				List<Map> dataList = esDatas.getDatas();
				System.out.println("TotalSize:"+esDatas.getTotalSize());
				if(dataList != null) {
					System.out.println("dataList.size:" + dataList.size());
				}
				else
				{
					System.out.println("dataList.size:0");
				}
			}
		},Map.class,6);
		List<Map> dataList = esDatas.getDatas();//数据已经被行处理器处理，所以这里不会有数据返回
		System.out.println("TotalSize:"+esDatas.getTotalSize());
		if(dataList != null) {
			System.out.println("dataList.size:" + dataList.size());
		}
		else
		{
			System.out.println("dataList.size:0");
		}
	}
}


```



# 开发交流

完整的demo工程

<https://github.com/bbossgroups/elasticsearch-example>


QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />


交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">


