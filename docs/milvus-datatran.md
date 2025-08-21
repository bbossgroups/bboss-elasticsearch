# Milvus向量数据库数据迁移指南

基于bboss提供的Milvus输入和输出插件，可以非常方便地实现向量数据库Milvus数据迁移功能，本文介绍Milvus数据迁移作业的实现、配置以及运行。

![](images/datasyn.png)

## 1.内容摘要

1)  同一个Milvus数据库中两张表之间进行数据迁移，如果目标向量表目标表不存在则进行创建
	如需在两个不同数据源之间进行迁移，则定义两个不同的Milvus数据源，然后在输入和输出插件分别配置输入源和输出源，亦可以直接在Milvus输入/输出插件上定义和配置不同的数据源

2)  基于Query数据迁移

3)  基于Search数据迁移：基于Xinference向量模型对检索文本进行向量化处理，可以设定向量检索算法和检索参数

4）批量记录拉取和写入配置：通过设置批量从Milvus拉取数据记录数和批量写入Milvus记录数，提升和调节数据采集作业的执行性能和效率

## **2. 准备工作**

导入bboss Milvus输入输出插件

```xml
<dependency>
<groupId>com.bbossgroups.plugins</groupId>
<artifactId>bboss-datatran-milvus</artifactId>
<version>7.5.0</version>
</dependency>
```

导入增量数据库sqlite驱动：

```xml
<dependency>      
<groupId>org.xerial</groupId>      
<artifactId>sqlite-jdbc</artifactId>      
<version>3.47.1.0</version>  
</dependency>
```

## **3. Milvus数据源定义、目标表创建以及初始化向量模型服务**

通过StartAction和EndAction：

1）注册Milvus数据源、检测和创建目标表以及释放Milvus数据源

```java
String targetCollectionName = "targetdemo";
importBuilder.setImportStartAction(new ImportStartAction() {
    @Override
    public void startAction(ImportContext importContext) {
        importContext.addResourceStart(new ResourceStart() {
            @Override
            public ResourceStartResult startResource() {
                //初始化milvus数据源服务，用来操作向量数据库
                MilvusConfig milvusConfig = new MilvusConfig();
                milvusConfig.setName("ucr_chan");//数据源名称
                milvusConfig.setDbName("ucr_chan");//Milvus数据库名称
                milvusConfig.setUri("http://172.24.176.18:19530");//Milvus数据库地址
                milvusConfig.setToken("");//认证token：root:xxxx
                ResourceStartResult resourceStartResult =  MilvusHelper.init(milvusConfig);//加载配置初始化Milvus数据源
                //如果向量表不存在，则创建向量表targetCollectionName
                MilvusHelper.executeRequest("ucr_chan", new MilvusFunction<Void>() {
                    @Override
                    public Void execute(MilvusClientV2 milvusClientV2) {
                        if(!milvusClientV2.hasCollection(HasCollectionReq.builder()
                                .collectionName(targetCollectionName)
                                .build())) {
                            ;
                            // create a collection with schema, when indexParams is specified, it will create index as well
                            CreateCollectionReq.CollectionSchema collectionSchema = milvusClientV2.createSchema();
                            collectionSchema.addField(AddFieldReq.builder().fieldName("log_id").dataType(DataType.Int64).isPrimaryKey(Boolean.TRUE)
                                    .autoID(Boolean.FALSE).build());//主键
                            collectionSchema.addField(AddFieldReq.builder().fieldName("content").dataType(DataType.FloatVector).dimension(1024).build());//日志内容对应的向量值
                            collectionSchema.addField(AddFieldReq.builder().fieldName("collecttime").dataType(DataType.Int64).build());//日志采集时间
                            collectionSchema.addField(AddFieldReq.builder().fieldName("log_content").dataType(DataType.VarChar).build());//日志内容原始值
                            IndexParam indexParam = IndexParam.builder()
                                    .fieldName("content")
                                    .metricType(IndexParam.MetricType.COSINE)
                                    .build();
                            CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                                    .collectionName(targetCollectionName)
                                    .collectionSchema(collectionSchema)
                                    .indexParams(Collections.singletonList(indexParam))
                                    .build();
                            milvusClientV2.createCollection(createCollectionReq);
                        }
                        return null;
                    }
                });
                return resourceStartResult;
            }
        });
    }

});
//作业结束后销毁初始化阶段自定义的向量模型服务数据源和向量数据库数据源
importBuilder.setImportEndAction(new ImportEndAction() {
    @Override
    public void endAction(ImportContext importContext, Exception e) {

        //销毁初始化阶段自定义的数据源
        importContext.destroyResources(new ResourceEnd() {
            @Override
            public void endResource(ResourceStartResult resourceStartResult) {                        
                //销毁初始化阶段自定义的向量数据库数据源
                if(resourceStartResult instanceof MilvusStartResult){
                    MilvusHelper.shutdown((MilvusStartResult) resourceStartResult);
                }
            }
        });
    }
});
```

2）初始化Xinference向量模型服务：用于基于Search进行数据迁移的文本条件向量化，基于Query无需初始化Xinference向量模型服务

```java
String targetCollectionName = "vectorsearchdemo";
importBuilder.setImportStartAction(new ImportStartAction() {
    @Override
    public void startAction(ImportContext importContext) {
        importContext.addResourceStart(new ResourceStart() {
            @Override
            public ResourceStartResult startResource() {
                //初始化向量模型服务
                Map properties = new HashMap();

                //embedding_model为的向量模型服务数据源名称
                properties.put("http.poolNames","embedding_model");

                properties.put("embedding_model.http.hosts","172.24.176.18:9997");///设置向量模型服务地址(这里调用的xinference发布的模型服务),多个地址逗号分隔，可以实现点到点负载和容灾

                properties.put("embedding_model.http.timeoutSocket","60000");
                properties.put("embedding_model.http.timeoutConnection","40000");
                properties.put("embedding_model.http.connectionRequestTimeout","70000");
                properties.put("embedding_model.http.maxTotal","100");
                properties.put("embedding_model.http.defaultMaxPerRoute","100");
                return HttpRequestProxy.startHttpPools(properties);

            }
        }).addResourceStart(new ResourceStart() {
            @Override
            public ResourceStartResult startResource() {
                //初始化milvus数据源服务，用来操作向量数据库
                篇幅关系，去掉这部分代码，与上面注册Milvus数据源、检测和创建目标表
                代码一致;
            }
        });
    }

});
//作业结束后销毁初始化阶段自定义的向量模型服务数据源和向量数据库数据源
importBuilder.setImportEndAction(new ImportEndAction() {
    @Override
    public void endAction(ImportContext importContext, Exception e) {

        //销毁初始化阶段自定义的数据源
        importContext.destroyResources(new ResourceEnd() {
            @Override
            public void endResource(ResourceStartResult resourceStartResult) {

                //销毁初始化阶段自定义的向量模型服务数据源
                if (resourceStartResult instanceof HttpResourceStartResult) {
                    HttpResourceStartResult httpResourceStartResult = (HttpResourceStartResult) resourceStartResult;
                    HttpRequestProxy.stopHttpClients(httpResourceStartResult);
                }
                //销毁初始化阶段自定义的向量数据库数据源
                else if(resourceStartResult instanceof MilvusStartResult){
                    MilvusHelper.shutdown((MilvusStartResult) resourceStartResult);
                }
            }
        });
    }
});
```

## **4. 输入源配置**

分别介绍基于Search检索的输入源配置和基于Query查询的输入源配置，可以根据实际迁移场景，选取其中一种即可。

### 4.1 基于Search检索的输入源配置

基于MilvusVectorInputConfig实现Search检索的输入配置：

```java
		/**
		 * 源Milvus相关配置，这里用与目标库相同的Milvus数据源ucr_chan_fqa（在startaction中初始化）
		 */
        String[] array = {"log_id","collecttime","log_content","content"};//定义要返回的字段清单        
       
		MilvusVectorInputConfig milvusInputConfig = new MilvusVectorInputConfig();
		milvusInputConfig.setVectorFieldName("content")//设置向量字段 
                .setBuildMilvusVectorDataFunction(() -> { //注册检索文本条件向量化转换函数
                    Map eparams = new HashMap();
                    eparams.put("input", "新增了机构");//查询条件文本内容 
                    eparams.put("model", "custom-bge-large-zh-v1.5");//指定Xinference向量模型名称
                    //调用的 xinference 发布的向量模型模型服务，将查询条件转换为向量
                    XinferenceResponse result = HttpRequestProxy.sendJsonBody("embedding_model", eparams,
                            "/v1/embeddings", XinferenceResponse.class);
                    float[] embedding = result.embedding();
                    return Collections.singletonList(new FloatVec(embedding));
                })
                .setSearchParams("{\"radius\": 0.85}") //返回content与查询条件相似度为0.85以上的记录
                .setMetricType(IndexParam.MetricType.COSINE) //采用余弦相似度算法
                .setConsistencyLevel(ConsistencyLevel.BOUNDED)
                .setName("ucr_chan_fqa")  //使用之前定义的向量数据库数据源，无需设置向量数据库地址和名称以及token等信息
//                             .setDbName("ucr_chan_fqa")
                            .setExpr("log_id < 100000")//指定过滤条件，可以进行条件组合，具体参考文档：https://milvus.io/api-reference/java/v2.4.x/v2/Vector/search.md
//                             .setUri("http://172.24.176.18:19530").setToken("")
                            .setOutputFields(Arrays.asList(array))  //指定返回字段清单                          
                             .setCollectionName("demo");//指定源表名称
		importBuilder.setInputConfig(milvusInputConfig);

```

向量检索时，除了要配置数据源参数、源表名称、过滤条件表达式以及返回字段清单，还需额外指定：

1）检索向量字段

2）字段向量值：通过BuildMilvusVectorDataFunction函数，调用Xinference向量模型服务，将文本转换为向量

3）向量检索参数：返回content与查询条件相似度为0.85以上的记录

importBuilder.setSearchParams("{\"radius\": 0.85}")

4）向量检索算法：本文采用余弦相似度算法COSINE

```java
// Only for float vectors
        L2,
        IP,
        COSINE,

        // Only for binary vectors
        HAMMING,
        JACCARD,

        // Only for sparse vector with BM25
        BM25
```

### 4.2 基于Query查询的输入源配置

基于MilvusInputConfig 实现Search检索的输入配置：

```java
		/**
		 * 源Milvus相关配置，这里用与目标库相同的Milvus数据源ucr_chan_fqa（在startaction中初始化）
		 */
        String[] array = {"log_id","collecttime","log_content","content"};//定义要返回的字段清单
		MilvusInputConfig milvusInputConfig = new MilvusInputConfig();
		milvusInputConfig.setName("ucr_chan_fqa")  //使用之前定义的向量数据库数据源，无需设置向量数据库地址和名称以及token等信息
//                             .setDbName("ucr_chan_fqa")
                            .setExpr("log_id < 100000")//指定过滤条件，可以进行条件组合，具体参考文档：https://milvus.io/api-reference/java/v2.4.x/v2/Vector/search.md
//                             .setUri("http://172.24.176.18:19530").setToken("")
                            .setOutputFields(Arrays.asList(array))  //设置返回字段清单                           
                             .setCollectionName("demo");//指定源表名称
		importBuilder.setInputConfig(milvusInputConfig);

```

相较Search检索，Query查询的输入配置就要简单很多，只需配置数据源参数、源表名称、过滤条件表达式以及返回字段清单即可。

## **5. 输出配置**

通过Milvus输出插件配置Milvus输出源和目标表：

```java
        /**
         * 目标Milvus配置，这里用与源相同的Milvus数据源ucr_chan_fqa（在startaction中初始化）
         */
        MilvusOutputConfig milvusOutputConfig = new MilvusOutputConfig();
        milvusOutputConfig.setName("ucr_chan_fqa")  //使用之前定义的向量数据库数据源，无需设置向量数据库地址和名称以及token等信息
//                             .setDbName("ucr_chan_fqa")
//                             .setUri("http://172.24.176.18:19530")
//                             .setToken("")
                .setCollectionName(targetCollectionName) //指标目标表名称
                //预加载目标表结构（表结构不会变化时设置为true），如果不预加载，每批次插入数据时，都会从Milvus获取一次目标表结构
                .setLoadCollectionSchema(true)  
                .setUpsert(true);//设置为true，记录存在更新，不存在则插入
        importBuilder.setOutputConfig(milvusOutputConfig);
```

从源库同步的向量数据表输出字段与目标表字段名称和类型都一致，会自动完成字段映射处理，所以为没有做进一步的datarefactor处理，如果需要进行字段数据类型转换和映射处理，可以参考文档：

[数据加工处理](https://esdoc.bbossgroups.com/#/db-es-tool?id=_2810-%e6%95%b0%e6%8d%ae%e5%8a%a0%e5%b7%a5%e5%a4%84%e7%90%86)

## 6. 增量迁移和定时执行策略配置

```java
		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束

        //增量配置开始
        importBuilder.setLastValueColumn("log_id");//指定数字增量查询字段
        //setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
        //setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
        importBuilder.setFromFirst(false);

        importBuilder.setLastValueStorePath("MilvusVectorSearch2MilvusDemo_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
        //importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
        importBuilder.setLastValueType(ImportIncreamentConfig.NUMBER_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
        // 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
        importBuilder.setLastValue(-100000);//增量起始值
        //增量配置结束
```

## 7.批量记录拉取和写入配置

可以通过调整批量拉取和写入记录数，来提升和调节数据采集作业的执行性能和效率：

```java
importBuilder.setFetchSize(1000); // 批量从Milvus拉取记录数
importBuilder.setBatchSize(50); //可选项,批量输出Milvus记录数
```

## 8.作业执行

完成作业配置后，通过importBuilder构建DataStream，然后执行即可启动运行数据迁移作业：

```java
/**
		 * 执行Milvus数据迁移作业
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作
```

## **9. 总结**

上面介绍了向量数据库Milvus数据迁移作业的数据源配置、数据条件向量化处理、Milvus输入插件配置（基于Search和基于Query两种模式）、Milvus输出插件配置、增量采集配置、作业定时策略配置以及作业的运行等内容。

完整的作业代码，从bboss的案例库中获取：

1）基于Search案例代码

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/milvus/MilvusVectorSearch2MilvusDemo.java

2）基于Query案例代码

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/milvus/Milvus2MilvusDemo.java

3）案例源码工程

可以下载源码工程，构建、运行和发布作业：

码云

https://gitee.com/bboss/bboss-datatran-demo

Github

https://github.com/bbossgroups/bboss-datatran-demo

## 9.参考资料

bboss轻量级微服务框架使用指南

https://esdoc.bbossgroups.com/#/httpproxy

向量数据库Milvus客户端组件使用介绍

https://doc.bbossgroups.com/#/Milvus

Milvus使用参考文档

https://milvus.io/api-reference/java/v2.5.x/v2/Vector/queryIterator.md

https://milvus.io/api-reference/java/v2.5.x/v2/Vector/searchIterator.md