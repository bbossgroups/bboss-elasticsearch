# Elasticsearch BulkProcessor异步批处理

# 1.Elasticsearch BulkProcessor介绍

![](images\bulkprocessor.png)

## 导入组件

参考文档导入Elasticsearch BulkProcessor组件Maven坐标

https://esdoc.bbossgroups.com/#/quickstart

## 1.1 API说明

Elasticsearch BulkProcessor异步批处理组件支持Elasticsearch各版本的Bulk异步批处理操作。通过BulkProcessor，可以将不同索引的增加、删除、修改文档操作添加到Bulk队列中，然后通过异步bulk方式快速完成数据批量处理功能，BulkProcessor提供三类api来支撑异步批处理功能：

1. insertData（每次加入一条记录到bulk队列中)，数据类型支持：PO，Map,String
2. insertDatas(每次可以加入待新增的多条记录到bulk队列中)
3. updateData（每次加入一条记录到bulk队列中)，数据类型支持：PO，Map,String
4. updateDatas(每次可以加入待修改的多条记录到bulk队列中)
5. deleteData（每次加入一条记录到bulk队列中）
6. deleteDatas(每次可以加入待删除的多条记录到bulk队列中)

## 1.2 触发批处理机制

Elasticsearch BulkProcessor异步批处理组件提供了三种触发批处理机制：

1. bulkSizes  按批处理数据记录数，达到BulkSizes对应的值时，执行一次bulk操作
2. maxMemSize 设置批量记录占用内存最大值，以字节为单位，达到最大值时，执行一次bulk操作， 可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作
3. flushInterval 强制bulk操作时间，单位毫秒，如果自上次往bulk中添加记录的时间后，空闲了flushInterval毫秒后一直没有数据到来，且数据量没有满足BulkSizes对应的记录数，或者没有满足maxMemSize，但是有记录，那么强制进行bulk处理

## 1.3 失败重试

BulkProcessor提供了失败重试机制，可以方便地设置重试次数，重试时间间隔，是否需要重试的异常类型判断：

   ```java
   // 重试配置
   				BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();
   		bulkProcessorBuilder.setBulkRetryHandler(new BulkRetryHandler() { //设置重试判断策略，哪些异常需要重试
   					public boolean neadRetry(Exception exception, BulkCommand bulkCommand) { //判断哪些异常需要进行重试
   						if (exception instanceof HttpHostConnectException     //NoHttpResponseException 重试
   								|| exception instanceof ConnectTimeoutException //连接超时重试
   								|| exception instanceof UnknownHostException
   								|| exception instanceof NoHttpResponseException
   //              				|| exception instanceof SocketTimeoutException    //响应超时不重试，避免造成业务数据不一致
   						) {
   
   							return true;//需要重试
   						}
   
   						if(exception instanceof SocketException){
   							String message = exception.getMessage();
   							if(message != null && message.trim().equals("Connection reset")) {
   								return true;//需要重试
   							}
   						}
   
   						return false;//不需要重试
   					}
   				})
   				.setRetryTimes(3) // 设置重试次数，默认为0，设置 > 0的数值，会重试给定的次数，否则不会重试
   				.setRetryInterval(1000l) // 可选，默认为0，不等待直接进行重试，否则等待给定的时间再重试
   
   ```
## 1.4 关键参数说明

Elasticsearch BulkProcessor组件关键参数说明：

bulkSizes  按批处理数据记录数，达到BulkSizes对应的值时，执行一次bulk操作

maxMemSize 设置批量记录占用内存最大值，以字节为单位，达到最大值时，执行一次bulk操作， 可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作

flushInterval 强制bulk操作时间，单位毫秒，如果自上次往bulk中添加记录的时间后，空闲了flushInterval毫秒后一直没有数据到来，且数据量没有满足BulkSizes对应的记录数，或者没有满足maxMemSize，但是有记录，那么强制进行bulk处理

workThreads bulk处理工作线程数

workThreadQueue bulk处理工作线程池缓冲队列大小

blockedWaitTimeout 指定bulk工作线程缓冲队列已满时后续添加的bulk处理排队等待时间，如果超过指定的时候bulk将被拒绝处理，单位：毫秒，默认为0，不拒绝并一直等待成功为止

elasticsearch 指定Elasticsearch集群数据源名称，bboss可以支持多数据源，默认值default

filterPath 为了提升性能，并没有把所有响应数据都返回，过滤掉了部分数据，可以自行设置FilterPath进行控制，例如可以设置为：took,errors,items.*.error

使用BulkProcessor api处理索引文档时，如果是Elasticsearch 7以上的版本就无需传递indexType参数，Elasticsearch7以前的版本带上indexType参数，bulk中的每个操作都可以通过ClientOptions来指定文档添加、修改删除的控制参数，ClientOptions控制参数设置方法可以参考文档：

[基于ClientOption指定添加修改文档控制参数](https://esdoc.bbossgroups.com/#/development?id=_482-基于clientoptionupdateoption指定添加修改文档控制参数)

## 1.5 批处理记录处理监测

可以通过以下api在批处理调用拦截器中获取批处理记录情况：

查看队列中追加的总记录数

CommonBulkCommand.getAppendRecords()

查看已经被处理成功的总记录数

CommonBulkCommand.getTotalSize()

查看处理失败的记录数

CommonBulkCommand.getTotalFailedSize()

# 2.BulkProcessor案例

用一个简单的demo来介绍Elasticsearch BulkProcessor异步批处理

参考以下文档在项目中快速导入Elasticsearch BulkProcessor

https://esdoc.bbossgroups.com/#/quickstart



普遍项目案例源码

https://github.com/bbossgroups/elasticsearch-example/tree/master/src/test/java/org/bboss/elasticsearchtest/bulkprocessor

spring boot案例源码

https://github.com/bbossgroups/elasticsearch-springboot-example/tree/master/src/main/java/org/bboss/elasticsearchtest/springboot/bulk

https://github.com/bbossgroups/elasticsearch-springboot-example/blob/master/src/test/java/org/bboss/elasticsearchtest/springboot/BulkProcessor7Test.java

https://github.com/bbossgroups/elasticsearch-springboot-example/blob/master/src/test/java/org/bboss/elasticsearchtest/springboot/BulkProcessorTest.java

通过BulkProcessorBuilder来构建bulkProcessor实例

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
        
		testBulkProcessor.shutdown(false);//调用shutDown停止方法后，BulkProcessor不会接收新的请求，但是会处理完所有已经进入bulk队列的数据


	}
	public void buildBulkProcessor(){
		//定义BulkProcessor批处理组件构建器
		BulkProcessorBuilder bulkProcessorBuilder = new BulkProcessorBuilder();
		bulkProcessorBuilder.setBlockedWaitTimeout(10000)//指定bulk工作线程缓冲队列已满时后续添加的bulk处理排队等待时间，如果超过指定的时候bulk将被拒绝处理，单位：毫秒，默认为0，不拒绝并一直等待成功为止				
				
				.setBulkSizes(1000)//按批处理数据记录数，达到BulkSizes对应的值时，执行一次bulk操作
                //设置批量记录占用内存最大值20M，以字节为单位，达到最大值时，执行一次bulk操作，
                // 可以根据实际情况调整maxMemSize参数，如果不设置maxMemSize，则按照按批处理数据记录数BulkSizes来判别是否执行执行一次bulk操作
                .setMaxMemSize(20*1024*1024)
                .setFlushInterval(5000)//强制bulk操作时间，单位毫秒，如果自上次往bulk中添加记录的时间后，空闲了flushInterval毫秒后一直没有数据到来，且数据量没有满足BulkSizes对应的记录数，或者没有满足maxMemSize，但是有记录，那么强制进行bulk处理

                .setWarnMultsRejects(1000)//由于没有空闲批量处理工作线程，导致bulk处理操作出于阻塞等待排队中，BulkProcessor会对阻塞等待排队次数进行计数统计，bulk处理操作被每被阻塞排队WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息
				.setWorkThreads(100)//bulk处理工作线程数
				.setWorkThreadQueue(100)//bulk处理工作线程池缓冲队列大小
				.setBulkProcessorName("test_bulkprocessor")//工作线程名称，实际名称为BulkProcessorName-+线程编号
				.setBulkRejectMessage("Reject test bulkprocessor")//bulk处理操作被每被拒绝WarnMultsRejects次（1000次），在日志文件中输出拒绝告警信息提示前缀
				.setElasticsearch("default")//指定Elasticsearch集群数据源名称，bboss可以支持多数据源
				//为了提升性能，并没有把所有响应数据都返回，过滤掉了部分数据，可以自行设置FilterPath进行控制
                .setFilterPath("took,errors,items.*.error")
                .addBulkInterceptor(new BulkInterceptor() {
					public void beforeBulk(BulkCommand bulkCommand) {
						//查看队列中追加的总记录数
                        logger.info("appendSize:"+bulkCommand.getAppendRecords());
                        //查看已经被处理成功的总记录数
                        logger.info("totalSize:"+bulkCommand.getTotalSize());
                        //查看处理失败的记录数
                        logger.info("totalFailedSize:"+bulkCommand.getTotalFailedSize());
					}

					public void afterBulk(BulkCommand bulkCommand, String result) {
						//查看队列中追加的总记录数
                        logger.info("appendSize:"+bulkCommand.getAppendRecords());
                        //查看已经被处理成功的总记录数
                        logger.info("totalSize:"+bulkCommand.getTotalSize());
                        //查看处理失败的记录数
                        logger.info("totalFailedSize:"+bulkCommand.getTotalFailedSize());
					}

					public void exceptionBulk(BulkCommand bulkCommand, Throwable exception) {
						//查看队列中追加的总记录数
                        logger.info("appendSize:"+bulkCommand.getAppendRecords());
                        //查看已经被处理成功的总记录数
                        logger.info("totalSize:"+bulkCommand.getTotalSize());
                        //查看处理失败的记录数
                        logger.info("totalFailedSize:"+bulkCommand.getTotalFailedSize());
						exception.printStackTrace();
					}
					public void errorBulk(BulkCommand bulkCommand, String result) {
						System.out.println("errorBulk："+result);
                        //查看队列中追加的总记录数
                        logger.info("appendSize:"+bulkCommand.getAppendRecords());
                        //查看已经被处理成功的总记录数
                        logger.info("totalSize:"+bulkCommand.getTotalSize());
                        //查看处理失败的记录数
                        logger.info("totalFailedSize:"+bulkCommand.getTotalFailedSize());
					}
				})//添加批量处理执行拦截器，可以通过addBulkInterceptor方法添加多个拦截器
				// https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html
				//下面的参数都是bulk url请求的参数：RefreshOption和其他参数只能二选一，配置了RefreshOption,就不能配置其他参数,refreshOption值格式：类似于refresh=true&&aaaa=bb&cc=dd&zz=ee这种形式，将相关参数拼接成合法的url参数格式
				// 其中的refresh参数控制bulk操作结果强制refresh入elasticsearch，便于实时查看数据，测试环境可以打开，生产不要设置
//				.setRefreshOption("refresh")
//				.setTimeout("100s")
				//.setMasterTimeout("50s")
				//.setRefresh("true")
//				.setWaitForActiveShards(2)
//				.setRouting("1") //(Optional, string) Target the specified primary shard.
//				.setPipeline("1") // (Optional, string) ID of the pipeline to use to preprocess incoming documents.
				.setBulkRetryHandler(new BulkRetryHandler() { //设置重试判断策略，哪些异常需要重试
					public boolean neadRetry(Exception exception, BulkCommand bulkCommand) { //判断哪些异常需要进行重试
						if (exception instanceof HttpHostConnectException     //NoHttpResponseException 重试
								|| exception instanceof ConnectTimeoutException //连接超时重试
								|| exception instanceof UnknownHostException
								|| exception instanceof NoHttpResponseException
//              				|| exception instanceof SocketTimeoutException    //响应超时不重试，避免造成业务数据不一致
						) {

							return true;//需要重试
						}

						if(exception instanceof SocketException){
							String message = exception.getMessage();
							if(message != null && message.trim().equals("Connection reset")) {
								return true;//需要重试
							}
						}

						return false;//不需要重试
					}
				})
				.setRetryTimes(3) // 设置重试次数，默认为0，设置 > 0的数值，会重试给定的次数，否则不会重试
				.setRetryInterval(1000l) // 可选，默认为0，不等待直接进行重试，否则等待给定的时间再重试
            ;
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
        
        //script field使用案例：通过ScriptField指定批处理数据信息
        data = new HashMap<String,Object>();
		data.put("id",1000);
        //直接制定文档需要新增的字段json串
		data.put("script","{\"name\":\"duoduo104\",\"goodsid\":104}");
		clientOptions = new ClientOptions();
		clientOptions.setIdField("id");
		clientOptions.setScriptField("script");
		bulkProcessor.insertData("bulkdemo",data,clientOptions);

		data = new HashMap<String,Object>();
		data.put("id",1000);
        //直接制定文档需要修改的字段json串，填充位置，替换其中的script内容   {"doc":script,......}
		data.put("script","{\"name\":\"updateduoduo104\",\"goodsid\":1104}");
		clientOptions = new ClientOptions();
		clientOptions.setIdField("id");
		clientOptions.setScriptField("script");
		bulkProcessor.updateData("bulkdemo",data,clientOptions);
        
        data = new HashMap<String,Object>();
		data.put("id",1000);
        //直接制定文档需要修改的字段json串，填充位置，替换其中的script内容   {"doc":script,......}
		data.put("script","{\"name\":\"updateduoduo104\",\"goodsid\":1104}");
		clientOptions = new ClientOptions();
		clientOptions.setIdField("id");
		clientOptions.setScriptField("script");
        clientOptions.setHaveScriptDoc(true);//如果script中已经包含了完整的修改json报文，无需框架自动补全doc和其他控制参数，则设置为true，默认值false
		bulkProcessor.updateData("bulkdemo",data,clientOptions);


	}
    
    public void shutdown(boolean asyn) {
		if(asyn) {
			Thread t = new Thread() {
				public void run() {
						bulkProcessor.shutDown();
				}
			};
			t.start();
		}
		else {
			bulkProcessor.shutDown();
		}

		
	}

}

```

# 3.Datastream bulk插入数据设置

如果需要通过bulkprocessor批量写入Datastream 管理的索引表，则需要明确指定op_type属性为create，示例如下：

```java
ClientOptions clientOptions = new ClientOptions();
clientOptions.setOpType("create");
detailBulkProcessor.insertData(index,  data,clientOptions);
```

