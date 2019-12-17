# BulkProcessor异步批处理组件使用

# 1.BulkProcessor介绍

BulkProcessor异步批处理组件支持Elasticsearch各个版本的Bulk操作，可以同时将不同索引的文档增加、删除、修改操作添加到BulkProcessor中，BulkProcessor对这些数据进行统一异步批量处理并保存到Elasticsearch，BulkProcessor提供三类api来支撑异步批处理功能：

1. insertData/insertDatas(可以直接添加需要插入的记录集合)
2. updateData/updateDatas(可以直接添加需要修改的记录集合)
3. deleteData/deleteDatas(可以直接添加需要删除的记录id集合)

使用BulkProcessor api处理索引文档时，如果是Elasticsearch 7以上的版本就无需传递indexType参数，Elasticsearch7以前的版本带上indexType参数，bulk中的每个操作都可以通过ClientOptions来指定文档添加、修改删除的控制参数，ClientOptions控制参数设置方法可以参考文档：

[基于ClientOption指定添加修改文档控制参数](https://esdoc.bbossgroups.com/#/development?id=_482-基于clientoptionupdateoption指定添加修改文档控制参数)

# 2.BulkProcessor案例
用一个简单的demo来介绍上述功能：

```java
package org.bboss.elasticsearchtest.bulkprocessor;

import org.frameworkset.elasticsearch.bulk.BulkCommand;
import org.frameworkset.elasticsearch.bulk.BulkInterceptor;
import org.frameworkset.elasticsearch.bulk.BulkProcessor;
import org.frameworkset.elasticsearch.bulk.BulkProcessorBuilder;
import org.frameworkset.elasticsearch.client.ClientOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestBulkProcessor {
	/**
	 * BulkProcessor批处理组件，一般作为单实例使用，单实例多线程安全，可放心使用
	 */
	private BulkProcessor bulkProcessor;
	public static void main(String[] args){
		TestBulkProcessor testBulkProcessor = new TestBulkProcessor();
		testBulkProcessor.buildBulkProcessor();//构建BulkProcessor批处理组件
		testBulkProcessor.testBulkDatas();//采用上面构建的BulkProcessor进行不同索引的索引文档增删改查异步批处理操作

	}
	public void buildBulkProcessor(){
		//定义BulkProcessor批处理组件构建器
		BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();
		bulkProcessorBuilder.setBlockedWaitTimeout(10000)//指定bulk数据缓冲队列已满时后续添加的bulk数据排队等待时间，如果超过指定的时候数据将被拒绝处理，单位：毫秒，默认为0，不拒绝并一直等待成功为止
				.setBulkFailRetry(1)//如果处理失败，重试次数，暂时不起作用
				.setBulkQueue(1000)//bulk数据缓冲队列大小，越大处理速度越快，根据实际服务器内存资源配置，用户提交的数据首先进入这个队列，然后通过多个工作线程从这个队列中拉取数据进行处理
				.setBulkSizes(10)//按批处理数据记录数
				.setFlushInterval(5000)//强制bulk操作时间，单位毫秒，如果自上次bulk操作flushInterval毫秒后，数据量没有满足BulkSizes对应的记录数，但是有记录，那么强制进行bulk处理
				.setRefreshOption("refresh")//数据bulk操作结果强制refresh入elasticsearch，便于实时查看数据，测试环境可以打开，生产不要设置
				.setWarnMultsRejects(1000)//由于没有空闲批量处理工作线程，导致bulk处理操作出于阻塞等待排队中，BulkProcessor会对阻塞等待排队次数进行计数统计，bulk处理操作被每被阻塞排队WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息
				.setWorkThreads(100)//bulk处理工作线程数
				.setWorkThreadQueue(100)//bulk处理工作线程池缓冲队列大小
				.setBulkProcessorName("test_bulkprocessor")//工作线程名称，实际名称为BulkProcessorName-+线程编号
				.setBulkRejectMessage("Reject test bulkprocessor")//bulk处理操作被每被拒绝WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息提示前缀
				.setElasticsearch("default")//指定Elasticsearch集群数据源名称，bboss可以支持多数据源
				.addBulkInterceptor(new BulkInterceptor() {
					public void beforeBulk(BulkCommand bulkCommand) {
						System.out.println("beforeBulk");
					}

					public void afterBulk(BulkCommand bulkCommand, String result) {
						System.out.println("afterBulk："+result);
					}

					public void errorBulk(BulkCommand bulkCommand, Throwable exception) {
						System.out.println("errorBulk：");
						exception.printStackTrace();
					}
				});//添加批量处理执行拦截器，可以通过addBulkInterceptor方法添加多个拦截器
		/**
		 * 构建BulkProcessor批处理组件，一般作为单实例使用，单实例多线程安全，可放心使用
		 */
		bulkProcessor = bulkProcessorBuilder.build();//构建批处理作业组件
	}
    /**
    * 采用上面构建的BulkProcessor进行不同索引的索引文档增删改查异步批处理操作
    * 本示例针对Elasticsearch 7以上的版本，如果针对elasticsearch 6及以下的版本，只要在对应的方法上加上indexType参数即可
    * 我们可以通过Map传递要处理的数据，也可以用PO对象传递需要bulk处理的数据
    */
	public void testBulkDatas(){
		System.out.println("testBulkDatas");
		ClientOptions clientOptions = new ClientOptions();
		clientOptions.setIdField("id");//通过clientOptions指定map中的key为id的字段值作为文档_id
        //添加单条记录到BulkProcessor中，BulkProcessor将异步执行bulk新增操作
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("name","duoduo1");
		data.put("id","1");
		bulkProcessor.insertData("bulkdemo",data,clientOptions);//es 7 api，往索引表bulkdemo中添加数据，通过clientOptions指定id的值作为文档_id
        //bulkProcessor.insertData("bulkdemo","bulkdemotype",data,clientOptions);//es 6及以下版本 api，往索引表bulkdemo对应的bulkdemotype中添加数据，通过clientOptions指定id的值作为文档_id
		data = new HashMap<String,Object>();
		data.put("name","duoduo2");
		data.put("id","2");
		bulkProcessor.insertData("bulkdemo",data,clientOptions);//es 7 api，往索引表bulkdemo中添加数据，通过clientOptions指定id的值作为文档_id
		data = new HashMap<String,Object>();
		data.put("name","duoduo3");
		data.put("id","3");
		bulkProcessor.insertData("bulkdemo",data,clientOptions);//es 7 api，往索引表bulkdemo中添加数据，通过clientOptions指定id的值作为文档_id
		data = new HashMap<String,Object>();
		data.put("name","duoduo4");
		data.put("id","4");
		bulkProcessor.insertData("bulkdemo",data,clientOptions);//es 7 api，往索引表bulkdemo中添加数据，通过clientOptions指定id的值作为文档_id
		data = new HashMap<String,Object>();
		data.put("name","duoduo5");
		data.put("id","5");
 
		bulkProcessor.insertData("bulkdemo",data,clientOptions);//es 7 api，往索引表bulkdemo中添加数据，通过clientOptions指定id的值作为文档_id
        //es 7 api 删除id为1的数据，BulkProcessor将异步执行bulk delete操作
		bulkProcessor.deleteData("bulkdemo","1");
		List<Object> datas = new ArrayList<Object>();
		for(int i = 6; i < 106; i ++) {
			data = new HashMap<String,Object>();
			data.put("name","duoduo"+i);
			data.put("id",""+i);
			datas.add(data);
		}
        //es 7 api,为索引表bulkdemo2一次性添加多条记录，BulkProcessor将对这些记录按批执行异步bulk新增操作
		bulkProcessor.insertDatas("bulkdemo2",datas,clientOptions);
		data = new HashMap<String,Object>();
		data.put("name","updateduoduo5");
		data.put("id","5");
        //es 7 api,添加修改bulkdemo索引表中id为5的数据到BulkProcessor中，BulkProcessor将异步执行bulk更新update操作
		bulkProcessor.updateData("bulkdemo",data,clientOptions);

	}

}

```

