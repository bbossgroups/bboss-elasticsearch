# Elasticsearch向量检索

Elasticsearch向量检索案例分享，本文介绍如何采用向量模型将数据转化为向量数据，存入Elasticsearch向量表，并实现向量检索功能，内容摘要：

1. 向量表创建：创建Elasticsearch向量索引表，用于存储向量数据
2. Xinference向量模型服务初始化：基于Xinference部署向量模型，并提供数据向量化服务
3. 向量数据存储：调用Xinference向量模型服务，将数据转换为向量数据，并批量写入Elasticsearch
4. 向量数据检索：基于[Elasticsearch KNN](https://www.elastic.co/docs/solutions/search/vector/knn#knn-search-filter-example)和高性能Elasticsearch java客户端bboss实现向量检索，调用Xinference向量模型服务，将查询条件转化为向量条件，实现向量检索，亦可以和普通条件结合，实现混合检索

## 1.准备工作

准备Elasticsearch 8以上的版本（下载Elasticsearch官方最新版本即可，bboss可自行适配Elasticsearch最新版本），本案例基于Elasticsearch 9.0.1实现。

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》引入和配置es客户端bboss

准备Xinference向量模型处理服务：bge-large-zh-v1.5,参考[Xinference官方文档](https://github.com/xorbitsai/inference)部署向量模型

![](images\xinference.png)



本文涉及的DSL文件（包含创建索引表Dsl和向量检索Dsl语句）：https://gitee.com/bboss/eshelloword-booter/blob/master/src/main/resources/esmapper/textembedding.xml

在resources目录下创建文件esmapper/textembedding.xml即可。

完整的案例源码文件：https://gitee.com/bboss/eshelloword-booter/blob/master/src/main/java/org/bboss/elasticsearchtest/textembedding/TextEmbedding.java

可运行测试用例：https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/textembedding/TestTextEmbedding.java

## 2.向量索引表创建

创建向量索引表collection-with-embeddings。

在textembedding.xml中定义对应dsl语句createTextEmbeddingIndex

```xml
<property name="createTextEmbeddingIndex">
        <![CDATA[
        {
          "mappings": {
            "properties": {
              ## 向量字段
              "text_embedding": {
                "type": "dense_vector",
                "dims": 1024,                        
                "index": true,
                "similarity": "cosine"
              },
              ## 原始文本内容
              "text": {
                "type": "text"
              },
              ## 定义一个混合检索字段
              "key": {
                "type": "keyword"
              }
            }
          }
        }]]>
    </property>
```

删除和创建索引表

```java
//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
        ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/textembedding.xml");
        clientUtil.dropIndice("collection-with-embeddings");//删除索引表
        clientUtil.createIndiceMapping("collection-with-embeddings","createTextEmbeddingIndex");//创建索引表
```



## 3.初始化Xinference向量模型服务

基于[bboss httpproxy微服务框架](https://esdoc.bbossgroups.com/#/httpproxy)调用Xinference数据向量化服务,定义名称为embedding_model的服务组：

```java
Map properties = new HashMap();

        //embedding_model为的向量模型服务数据源名称
        properties.put("http.poolNames","embedding_model");

        properties.put("embedding_model.http.hosts","172.24.176.18:9997");///设置向量模型服务地址(这里调用的xinference发布的模型服务),多个地址逗号分隔，可以实现点到点负载和容灾

        properties.put("embedding_model.http.timeoutSocket","60000");
        properties.put("embedding_model.http.timeoutConnection","40000");
        properties.put("embedding_model.http.connectionRequestTimeout","70000");
        properties.put("embedding_model.http.maxTotal","100");
        properties.put("embedding_model.http.defaultMaxPerRoute","100");
        HttpRequestProxy.startHttpPools(properties);//启动服务
```

## 4.数据向量化处理

定义数据和查询条件向量化方法：

```java
    /**
     * 数据向量化处理方法
     * @param text
     * @return
     */
    private float[] text2embedding(String text){
        Map params = new HashMap();
        params.put("input", text);//设置将要向量化的数据
        params.put("model", "custom-bge-large-zh-v1.5");//指定Xinference向量模型id        

        //调用Xinference向量服务，对数据进行向量化
        XinferenceResponse result = HttpRequestProxy.sendJsonBody("embedding_model", params, "/v1/embeddings", XinferenceResponse.class);
        if (result != null) {
            float[] embedding = result.embedding();
            return embedding;
        } else {
            throw new DataImportException("change LOG_CONTENT to vector failed:XinferenceResponse is null");
        }
    }
```

## 5.向量数据批量写入Elasticsearch 

批量写入100条数据：

```java
public void bulkdata(){
        List<Map> datas = new ArrayList<>();
        for(int i =0 ; i < 100; i ++) {
            
            Map data = new LinkedHashMap();
            String content = null;
            if(i % 5 == 0){
                content = "bboss是什么呢";
                data.put("key","bboss");
            }
            else if(i % 5 == 1){
                content = "中国五月名山是什么呢";
                data.put("key","china");
            }
            else if(i % 5 == 2){
                content = "能不能写首诗呢";
                data.put("key","document");
            }
            else if(i % 5 == 3){
                content = "梅花几月份开呢";
                data.put("key","flower");
            }
            else if(i % 5 == 4){
                content = "数据交换开源项目bboss";
                data.put("key","etl");
            }
            else{
                content = "elasticsearch knn searcher";
                data.put("key","knn");
            }
            data.put("text",content);
          
            //数据向量化处理
            float[] embedding = text2embedding(  content);
            if (embedding != null){
                data.put("text_embedding", embedding);//设置向量数据
            }
           
            datas.add(data);
            
        }
        //将向量数据批量写入向量表
        ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
        clientUtil.addDocuments("collection-with-embeddings",datas);
        
    }
```

可通过kibana查看数据是否写入成功：

![](images\embedding-kibana.png)

## 6.实现向量检索

Elasticsearch KNN检索参考文档：https://www.elastic.co/docs/solutions/search/vector/knn

参考上面的文档了解knn检索的各种参数设置。

### 6.1 简单向量检索

定义knn向量检索的dsl语句search

```xml
<property name="search">
    <![CDATA[
     
    {
      "retriever": {
        "knn": {
          "field": "text_embedding",
          "query_vector": #[condition,serialJson=true],
          "k": 2,
          "num_candidates": 5
        }
      }
    }]]>
</property>
```

执行向量检索：

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/textembedding.xml");
Map params = new LinkedHashMap();
//设置查询条件，调用数据向量化方法，将检索文本bboss转化为向量数据，默认最多返回10条数据
params.put("condition",text2embedding("bboss"));       
//返回MetaMap类型，为LinkHashMap的子类，但是包含索引记录元数据，元数据参考文档:https://esdoc.bbossgroups.com/#/document-crud?id=_62-%e5%b8%a6%e5%85%83%e6%95%b0%e6%8d%ae%e7%9a%84map%e5%af%b9%e8%b1%a1metamap%e4%bd%bf%e7%94%a8
ESDatas<MetaMap> datas = clientUtil.searchList("/collection-with-embeddings/_search","search1",params, MetaMap.class);
logger.info("datas.getTotalSize():"+datas.getTotalSize());//匹配条件的总记录数
List<MetaMap> metaMaps = datas.getDatas();//返回的结果数据
for(int i = 0; i < metaMaps.size(); i ++){
    MetaMap metaMap = metaMaps.get(i);
    logger.info("score: {}",metaMap.getScore());//相似度分数
    logger.info("text: {}",metaMap.get("text"));//检索的原始文本
    logger.info("key: {}",metaMap.get("key"));//检索的key字段值

}
```

### 6.2 指定K参数和Size参数

定义knn向量检索的dsl语句search1，指定K参数和Size参数

```xml
<property name="search1">
    <![CDATA[{
       "size":#[size],
      "knn": {
        "field": "text_embedding",
        "query_vector": #[condition,serialJson=true],
        "k": #[k],
        "num_candidates": 100,
        "boost": 0.6
      } 
    }]]>
</property>
```

执行向量检索：

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/textembedding.xml");
Map params = new LinkedHashMap();
//设置查询条件，调用数据向量化方法，将检索文本bboss转化为向量数据
params.put("condition",text2embedding("bboss"));
//设置返回top k条数据
params.put("k",50);
//设置Elasticsearch query size：最多返回记录数，需大于k参数值
params.put("size",100);
//返回MetaMap类型，为LinkHashMap的子类，但是包含索引记录元数据，元数据参考文档:https://esdoc.bbossgroups.com/#/document-crud?id=_62-%e5%b8%a6%e5%85%83%e6%95%b0%e6%8d%ae%e7%9a%84map%e5%af%b9%e8%b1%a1metamap%e4%bd%bf%e7%94%a8
ESDatas<MetaMap> datas = clientUtil.searchList("/collection-with-embeddings/_search","search1",params, MetaMap.class);
logger.info("datas.getTotalSize():"+datas.getTotalSize());//匹配条件的总记录数
List<MetaMap> metaMaps = datas.getDatas();//返回的结果数据
for(int i = 0; i < metaMaps.size(); i ++){
    MetaMap metaMap = metaMaps.get(i);
    logger.info("score: {}",metaMap.getScore());//相似度分数
    logger.info("text: {}",metaMap.get("text"));//检索的原始文本
    logger.info("key: {}",metaMap.get("key"));//检索的key字段值

}
```

### 6.3 指定向量检索Similarity阈值

定义knn向量检索的dsl语句searchWithScore，指定K参数和Size参数、Similarity阈值

```xml
<property name="searchWithScore">
    <![CDATA[{
       "size":#[size],
      "knn": {
        "field": "text_embedding",
        "query_vector": #[condition,serialJson=true],
        "k": #[k],
        "similarity": #[similarity],
        "num_candidates": 100,
        "boost": 0.9
      } 
    }]]>
</property>
```

执行向量检索：

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/textembedding.xml");
        Map params = new LinkedHashMap();
        //设置向量查询条件，调用数据向量化方法，将检索文本bboss转化为向量数据
        params.put("condition",text2embedding("bboss"));
        //设置返回top k条数据
        params.put("k",50);
        //设置Elasticsearch query size：最多返回记录数，需大于k参数值
        params.put("size",100);
        //指定向量相似度阈值，不会返回向量相似度低于similarity值的记录
        params.put("similarity",0.5);
        ESDatas<MetaMap> datas = clientUtil.searchList("/collection-with-embeddings/_search","searchWithScore",params, MetaMap.class);
        logger.info("datas.getTotalSize():"+datas.getTotalSize());
//        logger.info("datas.getDatas():"+ SimpleStringUtil.object2json(datas.getDatas()));
        List<MetaMap> metaMaps = datas.getDatas();
        for(int i = 0; i < metaMaps.size(); i ++){
            MetaMap metaMap = metaMaps.get(i);
            logger.info("score: {}",metaMap.getScore());//相似度分数
            logger.info("text: {}",metaMap.get("text"));//检索的原始文本
            logger.info("key: {}",metaMap.get("key"));//检索的key字段值

        }
```

### 6.4 混合检索

定义knn向量和关键字混合检索的dsl语句searchWithFilter，指定K参数和Size参数、Similarity阈值以及混合检索条件key

```xml
<property name="searchWithFilter">
    <![CDATA[{
       "size":#[size],
      "knn": {
        "field": "text_embedding",
        "query_vector": #[condition,serialJson=true],
        "k": #[k],
        "similarity": #[similarity],
        "filter": {
          "term": {
            "key": #[key]
          }
        },
        "num_candidates": 100,
        "boost": 0.9
      } 
    }]]>
</property>
```

执行向量检索

```java
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/textembedding.xml");
Map params = new LinkedHashMap();
//设置向量查询条件，调用数据向量化方法，将检索文本bboss转化为向量数据
params.put("condition",text2embedding("bboss"));
//设置返回top k条数据
params.put("k",50);
//设置Elasticsearch query size：最多返回记录数，需大于k参数值
params.put("size",100);
//指定向量相似度阈值，不会返回向量检索相似度低于similarity值的记录
params.put("similarity",0.5);
//设置混合检索条件：指定key字段值条件
params.put("key","bboss");
ESDatas<MetaMap> datas = clientUtil.searchList("/collection-with-embeddings/_search","searchWithFilter",params, MetaMap.class);
logger.info("datas.getTotalSize():"+datas.getTotalSize());
List<MetaMap> metaMaps = datas.getDatas();
for(int i = 0; i < metaMaps.size(); i ++){
    MetaMap metaMap = metaMaps.get(i);
    logger.info("score: {}",metaMap.getScore());//相似度分数
    logger.info("text: {}",metaMap.get("text"));//检索的原始文本
    logger.info("key: {}",metaMap.get("key"));//检索的key字段值

}
```

## 7.检索结果Rerank

可以对向量检索召回的结果，采用Rerank模型，根据语义相关度进行精排序，从而提取与问题语义相关度最高的记录：

实现代码：

```java
public void searchVectorAndRerank(){
        ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/textembedding.xml");
        Map params = new LinkedHashMap();
        //设置向量查询条件，调用数据向量化方法，将检索文本bboss转化为向量数据
        params.put("condition",text2embedding("bboss介绍"));
        //设置返回top k条数据
        params.put("k",50);
        //设置Elasticsearch query size：最多返回记录数，需大于k参数值
        params.put("size",100);
        //指定向量相似度阈值，不会返回向量检索相似度低于similarity值的记录
        params.put("similarity",0.5);
        ESDatas<MetaMap> datas = clientUtil.searchList("/collection-with-embeddings/_search","searchWithScore",params, MetaMap.class);
        logger.info("datas.getTotalSize():"+datas.getTotalSize());
//        logger.info("datas.getDatas():"+ SimpleStringUtil.object2json(datas.getDatas()));
        List<MetaMap> metaMaps = datas.getDatas();
        List rerankDatas = new ArrayList();
        for(int i = 0; i < metaMaps.size(); i ++){
            MetaMap metaMap = metaMaps.get(i);
            logger.info("score: {}",metaMap.getScore());//相似度分数
            logger.info("text: {}",metaMap.get("text"));//检索的原始文本
            rerankDatas.add(metaMap.get("text"));
            logger.info("key: {}",metaMap.get("key"));//检索的key字段值

        }
        
        //对检索结果进行Rerank处理
        Map rerankParams = new LinkedHashMap();
        rerankParams.put("model","bge-reranker-base");//指定基于Xinference部署的Rerank模型
        rerankParams.put("documents",rerankDatas);//根据问题进行向量检索返回的数据
        rerankParams.put("query","bboss介绍");//问题

        //调用Xinference Rerank服务，对问题的各个答案进行语义相关度排序
        Map reponse = HttpRequestProxy.sendJsonBody("embedding_model",rerankParams,"/v1/rerank",Map.class);
        logger.info(SimpleStringUtil.object2json(reponse));
    }
```
运行结果

```json
{"id":"913a8c4c-66cb-11f0-be3b-00155d8e1b16","results":[{"index":0,"relevance_score":0.9707212448120117,"document":null},{"index":10,"relevance_score":0.9707212448120117,"document":null},{"index":18,"relevance_score":0.9707212448120117,"document":null},{"index":17,"relevance_score":0.9707212448120117,"document":null},{"index":16,"relevance_score":0.9707212448120117,"document":null},{"index":15,"relevance_score":0.9707212448120117,"document":null},{"index":14,"relevance_score":0.9707212448120117,"document":null},{"index":13,"relevance_score":0.9707212448120117,"document":null},{"index":12,"relevance_score":0.9707212448120117,"document":null},{"index":11,"relevance_score":0.9707212448120117,"document":null},{"index":9,"relevance_score":0.9707212448120117,"document":null},{"index":8,"relevance_score":0.9707212448120117,"document":null},{"index":7,"relevance_score":0.9707212448120117,"document":null},{"index":6,"relevance_score":0.9707212448120117,"document":null},{"index":5,"relevance_score":0.9707212448120117,"document":null},{"index":4,"relevance_score":0.9707212448120117,"document":null},{"index":3,"relevance_score":0.9707212448120117,"document":null},{"index":2,"relevance_score":0.9707212448120117,"document":null},{"index":1,"relevance_score":0.9707212448120117,"document":null},{"index":19,"relevance_score":0.9707212448120117,"document":null},{"index":48,"relevance_score":0.9630429744720459,"document":null},{"index":49,"relevance_score":0.9630429744720459,"document":null},{"index":47,"relevance_score":0.9630429744720459,"document":null},{"index":46,"relevance_score":0.9630429744720459,"document":null},{"index":45,"relevance_score":0.9630429744720459,"document":null},{"index":44,"relevance_score":0.9630429744720459,"document":null},{"index":43,"relevance_score":0.9630429744720459,"document":null},{"index":42,"relevance_score":0.9630429744720459,"document":null},{"index":41,"relevance_score":0.9630429744720459,"document":null},{"index":40,"relevance_score":0.9630429744720459,"document":null},{"index":27,"relevance_score":0.828909158706665,"document":null},{"index":26,"relevance_score":0.828909158706665,"document":null},{"index":24,"relevance_score":0.828909158706665,"document":null},{"index":30,"relevance_score":0.828909158706665,"document":null},{"index":29,"relevance_score":0.828909158706665,"document":null},{"index":25,"relevance_score":0.828909158706665,"document":null},{"index":31,"relevance_score":0.828909158706665,"document":null},{"index":28,"relevance_score":0.828909158706665,"document":null},{"index":23,"relevance_score":0.828909158706665,"document":null},{"index":22,"relevance_score":0.828909158706665,"document":null},{"index":21,"relevance_score":0.828909158706665,"document":null},{"index":20,"relevance_score":0.828909158706665,"document":null},{"index":32,"relevance_score":0.8289089202880859,"document":null},{"index":33,"relevance_score":0.8289089202880859,"document":null},{"index":39,"relevance_score":0.8289089202880859,"document":null},{"index":38,"relevance_score":0.8289089202880859,"document":null},{"index":37,"relevance_score":0.8289089202880859,"document":null},{"index":36,"relevance_score":0.8289089202880859,"document":null},{"index":34,"relevance_score":0.8289089202880859,"document":null},{"index":35,"relevance_score":0.8289089202880859,"document":null}],"meta":{"api_version":null,"billed_units":null,"tokens":null,"warnings":null}}
```



## 8.运行案例

定义测试用例-[TestTextEmbedding](https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/textembedding/TestTextEmbedding.java)

```java
@Test
public void testEmbedding(){
    //Elasticsearch KNN search参考文档：https://www.elastic.co/docs/solutions/search/vector/knn#knn-search-filter-example
    TextEmbedding textEmbedding = new TextEmbedding();
    textEmbedding.init();
    textEmbedding.testCreateTextEmbeddingIndex();
    textEmbedding.bulkdata();
    textEmbedding.search();
    textEmbedding.search1();
    textEmbedding.searchWithFilter();
    textEmbedding.searchWithScore();
    textEmbedding.searchVectorAndRerank();
}
```

执行测试用例，运行成功后，会输出以下日志：

![](images\embedding-result.png)

## 9.参考资料和开发交流

### 9.1参考资料

Elasticsearch KNN  https://www.elastic.co/docs/solutions/search/vector/knn

bboss增删改查参考文档  https://esdoc.bbossgroups.com/#/document-crud

bboss httpproxy微服务框架 https://esdoc.bbossgroups.com/#/httpproxy

Xinference官方文档  https://github.com/xorbitsai/inference

本文对应的源码工程 https://gitee.com/bboss/eshelloword-booter

### 9.2开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />



交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">





