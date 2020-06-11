# Elasticsearch：TF-IDF，BM25和相关度的控制

ES 5.0 之前，默认的相关性算分采用的是 TF-IDF，而之后则默认采用 BM25。对于相关度有以下三个问题：

1. 什么是相关性/相关度？Lucene 是如何计算相关度的？
2. TF-IDF 和 BM25 究竟是什么？
3. 相关度控制的方式有哪些？各自都有什么特点？

本文从相关性概念入手，到 TF-IDF 和 BM25 讲解和数学公式学习，再到详细介绍多种常用的相关度控制方式。相信对你一定有用！

# 前言

案例源码工程:

https://github.com/rookieygl/bboss-wiki

本案例以Elasticsearch开源java rest client客户端bboss开发：

https://esdoc.bbossgroups.com/#/README

本案例以Elasticsearch6.8.9版本，bboss6.1.5单元测试时，建议版本选择不要太低

DSL的配置文件[resources/esmapper/doc_relevancy.xml](https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/doc_relevancy.xml)，本文涉及到的DSL都会放到该配置文件，本案例测试代码[DocRelevancy](https://github.com/rookieygl/bboss-wiki/blob/master/src/test/java/com/ygl/dsldo/DocRelevancy.java)。

# 1.文档相关性

相关性描述的是⼀个⽂档和查询语句匹配的程度。ES 会对每个匹配查询条件的结果进⾏算分 的\_score。_score评分越高，相关度越高。

对于信息检索工具，衡量其性能有3大指标：

- **查准率 Precision**：尽可能返回较少的无关文档；
- **查全率 Recall**：尽可能返回较多的相关文档；
- **排序 Ranking**：是否能按相关性排序。

前两者更多与分词匹配相关，而后者则与相关性的判断与算分相关。本文将详细介绍相关性系列知识点。

# 2.相似度理论

 Elasticsearch使用布尔模型（Boolean model）查找匹配文档，并用一个名为实用评分函数（practical scoring function）的公式来计算相关度。这个公式借鉴了 词频/逆向（TF/TDF）文档频率和向量空间模型（vector space model），同时也加入了一些新特性，如协调因子（coordination factor），字段长度归一化（field length normalization），以及词或查询语句权重提升。

[*向量空间模型（Boolean model）*](https://www.elastic.co/guide/cn/elasticsearch/guide/current/scoring-theory.html#scoring-theory) 和[*协调因子*](https://www.elastic.co/guide/cn/elasticsearch/guide/current/practical-scoring-function.html)这里不再介绍，详情请参考ES官网资料。

## 2.1.布尔模型

布尔模型（Boolean Model）只是在查询中使用 AND、 OR和 NOT（与、或和非）这样的条件来查找匹配的文档，以下查询：

```java
full AND text AND search AND (elasticsearch OR lucene)
```

会将所有包括词 `full` 、 `text` 和 `search` ，以及 `elasticsearch` 或 `lucene` 的文档作为结果集。这个过程简单且快速，它将所有可能不匹配的文档排除在外。

这就是term查询，只返回符合的文档，多个条件一视同仁，文档得分完全由BM(25)决定。

## 2.2.词频 TF（Term Frequency）

检索词在文档中出现的频度是多少？出现频率越高，相关性也越高。关于TF的数学表达式，参考ES官网，如下：

```java
tf(t in d) = √frequency
```

词 t 在文档 d 的词频（ tf ）是该词在文档中出现次数的平方根。

**概念理解**：比如说我们检索关键字`es`，`es`在文档A中出现了10次，在文档B中只出现了1次。我们认为文档A与`es`的相关性更高。

### 2.2.1.关闭词频

如果不在意词在某个字段中出现的频次，而只在意是否出现过，则可以在字段映射中禁用词频统计。DSL如下：

```xml
<property name="closeTF" desc = "关闭词频TF">
        <![CDATA[{
            "mappings": {
                "properties": {
                  "text": {
                    "type": "keyword",
                    "index_options": "docs"
                  }
                }
            }
        }]]>
    </property>
```

bboss执行上述模板：

```java
 private Logger logger = LoggerFactory.getLogger(FunctionScoreTest.class);//日志

    @Autowired
    private BBossESStarter bbossESStarter;//bboss启动器

    private ClientInterface clientInterface;//bboss dsl工具 
/**
     * 关闭词频TF
     */
    @Test
    public void closeTF(){
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");//bboss读取xml
            /*检查索引是否存在，存在就删除重建*/
            if (clientInterface.existIndice("close_tf")) {
                clientInterface.dropIndice("close_tf");
            }
            clientInterface.createIndiceMapping("close_tf", "closeTF");
            logger.info("创建索引 close_tf 成功");
        } catch (ElasticSearchException e) {
            logger.error("创建索引 close_tf 执行失败", e);
        }
    }
```

将字段 `index_options` 设置为 `docs` 可以禁用词频统计及词频位置，这个映射的字段不会计算词的出现次数，对于短语或近似查询也不可用。字段`index`设置为 `not_analyzed` 字符串字段会默认使用该设置。

### 2.2.2.注意事项

目前，Elasticsearch 不支持更改已有字段的相似度算法mapping（映射），只能通过为数据重新建立索引来达到目的。请谨慎设置您的mapping。

## 2.3.逆向⽂档频率 IDF（Inverse Document Frequency）

关于 IDF 的数学表达式，参考ES官网，如下：

```java
idf(t) = 1 + log ( numDocs / (docFreq + 1))
```

词 t 的逆向文档频率（ idf ）是：索引中文档数量（numDocs）除以包含该词的文档数（docFreq），然后求其对数。

**注意: 这里的log是指以e为底的对数,不是以10为底的对数。**

**概念理解：**比如说检索词“学习ES”，按照Ik分词会得到两个Token【学习】【ES】，假设在当前索引下有100个文档包含Token“学习”，只有10个文档包含Token“ES”。那么对于【学习】【ES】这两个Token来说，出现次数较少的 Token【ES】就可以帮助我们快速缩小范围找到我们想要的文档，所以说此时“ES”的权重就比“学习”的权重要高。

## 2.4 字段长度归一值 Norm

字段长度归一值之前也称为**字段长度准则 field-length norm**

字段的长度是多少？字段越短，字段的权重越高。检索词出现在一个内容短的 title 要比同样的词出现在一个内容长的 content 字段权重更大。关于 norm 的数学表达式，参考ES官网，如下：

```java
norm(d) = 1 / √numTerms 
```

 字段长度归一值（ norm ）是字段中词数平方根的倒数。

### 2.4.1. 关闭归一值

字段长度的归一值对全文搜索非常重要，许多其他字段不需要有归一值。无论文档是否包括这个字段，索引中每个文档的每个 `string` 字段都大约占用 1 个 byte 的空间。对于 `not_analyzed` 字符串字段的归一值默认是禁用的，而对于 `analyzed` 字段也可以通过修改字段映射禁用归一值。DSL如下：

```xml
 <property name="closeNorms" desc = "关闭字段长度归一值">
        <![CDATA[{
            "mappings": {
              "properties": {
                "text": {
                  "type": "text",
                  "norms":  false
                }
              }

            }
        }]]>
    </property>
```

bboss执行上述模板：

```java
/**
     * 关闭字段长度归一值
     */
    @Test
    public void closeNorms(){
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");//bboss读取xml
            /*检查索引是否存在，存在就删除重建*/
            if (clientInterface.existIndice("close_orms")) {
                clientInterface.dropIndice("close_orms");
            }
            clientInterface.createIndiceMapping("close_orms", "closeTF");
            logger.info("创建索引 close_orms 成功");
        } catch (ElasticSearchException e) {
            logger.error("创建索引 close_orms 执行失败", e);
        }
    }
```

禁用归一值的字段，长字段和短字段会以相同长度计算评分。

对于有些应用场景如日志，归一值不是很有用，要关心的只是字段是否包含特殊的错误码，字段的长度对结果没有影响，禁用归一值可以节省大量内存空间。

## 2.5.结合使用

词频（term frequency）、逆向文档频率（inverse document frequency）和字段长度归一值（field-length norm）是在索引时计算并存储的。最后将它们结合在一起计算单个词在特定文档中的 权重 。

## 2.6.Lucene中的 评分公式

对于多词查询，Lucene 使用布尔模型（Boolean model） 、 TF/IDF以及向量空间模型（vector space model），然后将它们组合到单个高效的文档集合里并进行评分计算。

评分公式参考自官网：

```java
score(q,d)  =  
            queryNorm(q)  
          · coord(q,d)    
          · ∑ (           
                tf(t in d)   
              · idf(t)²      
              · t.getBoost() 
              · norm(t,d)    
            ) (t in q)    
```

1. score(q,d) 是文档 d 与查询 q 的相关度评分总分。

2. queryNorm(q)是 [*查询归一化* 因子](https://www.elastic.co/guide/cn/elasticsearch/guide/current/practical-scoring-function.html#query-norm) （新）。
3. 
   coord(q,d) 是 协调 因子 （新）。

4. 
   查询 q 中每个词 t 对于文档 d 的权重和。

5. 
   tf(t in d) 是词 t 在文档 d 中的词频 。

6. 
   idf(t) 是词 t 的 逆向文档频率 。

7. 
   t.getBoost() 是查询中使用的 [*boost*](https://www.elastic.co/guide/cn/elasticsearch/guide/current/query-time-boosting.html)（新）。

8. norm(t,d) 是 字段长度归一值 ，与 [*索引时字段层 boost*](https://www.elastic.co/guide/cn/elasticsearch/guide/current/practical-scoring-function.html#index-boost) （如果存在）的和（新）。

本文只讨论score、tf（词频）、idf（逆词频）和norm（字段长度归一值）。

## 2.7.BM25：可更改的相似度

BM25官方成为是可拔插的相似度，可以修改`k1`和`b`的值进行相似度修改。具体修改方法会在下面介绍。

### 2.7.1.BM25公式

关于BM25公式，倒不如将关注点放在BM25所能带来的实际好处上。BM25同样使用词频，逆向文档频率以及长度长归一化，但是每个因素的定义都有细微区别。

![](images\bm25_function.png)

<center>BM25公式图</center>
**该公式`.`的前部分就是 IDF 的算法，后部分就是 DF和字段长度归一值Norm的综合公式**。该公式可以简化为:
```java
_score=idf*f(df,norm)
```


### 2.7.2.TF/IDF与BM25的词频饱和度

TF-IDF算法评分：TF（t）部分的值，随着文档里的某个词出现次数增多，导致整个公式返回的值越大。

BM25就针对这点进行来优化，转换TF（t）的逐步增大，该算法的返回值会趋于一个数值。整体而言BM25就是对TF-IDF算法的平滑改进。

![](images\tif-bm25.png)

<center>TF / IDF与BM25的词频饱和度曲线图</center>
值得一提的是，不像TF / IDF，BM25有一个比较好的特性就是它提供了两个可调参数：

**`k1`**

这个参数控制着词频结果在词频饱和度中的上升速度。默认值为 `1.2` 。值越小饱和度变化越快，值越大饱和度变化越慢。

**`b`**

这个参数控制着字段长归一值所起的作用， `0.0` 会禁用归一化， `1.0` 会启用完全归一化。默认值为 `0.75` 。

### 2.7.3.指定BM25相似度

similarity默认属性有三种

- BM25：[Okapi BM25 algorithm](https://en.wikipedia.org/wiki/Okapi_BM25)
- classic：[TF/IDF algorithm](https://en.wikipedia.org/wiki/Tf–idf)
- boolean：简单布尔相似度，匹配分数为1，不匹配为0。

es7x版本之前版本similarity默认值为`classic`，在7x移除该值并默认为`BM25`。详情参考官网[*similarity属性*](https://www.elastic.co/guide/en/elasticsearch/reference/current/similarity.html)。

当然我们可以自定义相关度，指定similarity为我们自定义相关度算法，下面会有详细介绍。

```xml
<property name="bm25Index" desc = "创建索引，指定字段为BM25评分算法">
        <![CDATA[{
            "mappings": {
            "properties": {
              "title": {
                "type": "text",
                "similarity": "BM25"
              },
              "body": {
                "type": "text",
                "similarity": "boolean"
              }
            }
            }
        }]]>
    </property>
```

bboss执行上述模板：

```java
 /**
     * 创建索引，指定字段为BM25评分算法
     */
    @Test
    public void bm25Index(){
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");//bboss读取xml
            /*检查索引是否存在，存在就删除重建*/
            if (clientInterface.existIndice("bm25_index")) {
                clientInterface.dropIndice("bm25_index");
            }
            clientInterface.createIndiceMapping("bm25_index", "bm25Index");
            logger.info("创建索引 bm25_index 成功");
        } catch (ElasticSearchException e) {
            logger.error("创建索引 bm25_index 执行失败", e);
        }
    }
```

# 3.explain：ES执行计划

使用 explain查看搜索相关性分数的计算过程。这非常有助于我们理解ES的相关度计算过程。下面通过示例来学习：

## 3.1.创建索引

创建索引DSL如下：

```xml
<property name="createExplainIndex" desc = "创建explain测试索引">
        <![CDATA[{
            "mappings": {
              "properties": {
                "id": {
                  "type": "integer"
                },
                "author": {
                  "type": "keyword"
                },
                "title": {
                  "type": "text",
                  "analyzer": "ik_smart"
                },
                "content": {
                  "type": "text",
                  "analyzer": "ik_max_word",
                  "search_analyzer": "ik_smart"
                },
                "tag": {
                  "type": "keyword"
                },
                "influence": {
                  "type": "integer"
                },
                "createAt": {
                  "type": "date",
                  "format": "yyyy-MM-dd HH:mm:ss"
                }
              }
            }
        }]]>
    </property>
```

bboss执行上述模板：

```java
  /**
     * 创建explain测试索引
     */
    @Test
    public void createExplainIndex(){
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");//bboss读取xml
            /*检查索引是否存在，存在就删除重建*/
            if (clientInterface.existIndice("explain_index")) {
                clientInterface.dropIndice("explain_index");
            }
            clientInterface.createIndiceMapping("explain_index", "createExplainIndex");
            logger.info("创建索引 explain_index 成功");
        } catch (ElasticSearchException e) {
            logger.error("创建索引 explain_index 执行失败", e);
        }
    }
```

## 3.2.导入测试数据

**一定要保证_bluk DSL的格式,一行索引，一行数据，不能换行，多行。**数据导入DSL如下：

```xml
  <property name="blukExplainIndex" desc = "导入ExplainI索引数据">
        <![CDATA[
            {"index":{"_index":"explain_index","_id":"1"}}
            {"id":1,"author":"bboss开源引擎","title":"es的相关度","content":"这是关于es的相关度的文章","tag":[1,2,3],"influence":{"gte":10,"lte":12},"createAt":"2020-05-24 10:56:00"}
            {"index":{"_index":"explain_index","_id":"2"}}
            {"id":2,"author":"bboss开源引擎","title":"相关度","content":"这是关于相关度的文章","tag":[2,3,4],"influence":{"gte":12,"lte":15},"createAt":"2020-05-23 10:56:00"}
            {"index":{"_index":"explain_index","_id":"3"}}
            {"id":3,"author":"bboss开源引擎","title":"es","content":"这是关于关于es和编程的必看文章","tag":[2,3,4],"influence":{"gte":12,"lte":15},"createAt":"2020-05-22 10:56:00"}
            {"index":{"_index":"explain_index","_id":"4"}}
            {"id":4,"author":"bboss开源","title":"关注boss，系统学习es","content":"这是关于es的文章，介绍了一点相关度的知识","tag":[1,2,3],"influence":{"gte":10,"lte":15},"createAt":"2020-05-24 10:56:00"}
        ]]>
```

bboss执行上述模板：

```java
    /**
     * 添加explain索引数据
     */
    @Test
    public void blukExplainIndex() {
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");//bboss读取xml
            ClientInterface restClient = ElasticSearchHelper.getRestClientUtil();//插入数据用RestClient
            ESInfo esInfo = clientInterface.getESInfo("blukExplainIndex");//获取插入数据
            StringBuilder recipedata = new StringBuilder();
            recipedata.append(esInfo.getTemplate().trim())
                    .append("\n");//换行符不能省
            //插入数据
            restClient.executeHttp("explain_index/_bulk?refresh", recipedata.toString(), ClientUtil.HTTP_POST);

            //统计当前索引数据
            long recipeCount = clientInterface.countAll("explain_index");
            logger.info("explain_index 当前条数：{}", recipeCount);
        } catch (ElasticSearchException e) {
            logger.error("explain_index 插入数据失败", e);
        }
    }
```

执行完上述两个测试用例，你就用这个创建好的索引和索引数据，进行下一步的测试。

## 3.3.数据导入推荐

使用_bulk接口可以快速插入数据，对于大数据插入Bboss封装了bulkProcessor，支持多线程导入数据，性能非常可观。详情请参考

https://esdoc.bbossgroups.com/#/bulkProcessor

## 3.4 使用explain

先创建一个使用explain查询的DSL：

```xml
    <property name="testExplain" desc = "测试explain查看ES查询执行计划">
        <![CDATA[{
             "explain": true,
              "query": {
                "match": {
                  "title": "es的相关度"
                }
              }
        }]]>
    </property>
```

bboss执行上述模板：

```java
 /**
     * 测试explain查看ES查询执行计划
     */
    @Test
    public void testExplain() {
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");

            ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("explain_index/_search?search_type=dfs_query_then_fetch",
                    "testExplain",//DSL模板ID
                    MetaMap.class);//文档信息

            //ES返回结果遍历

            metaMapESDatas.getDatas().forEach(metaMap -> {
                logger.info("\n文档_source:{} \n_explanation:\n{}", metaMap,
                        SimpleStringUtil.object2json(metaMap.getExplanation())
                );
            });
        } catch (ElasticSearchException e) {
            logger.error("testSpanTermQuery 执行失败", e);
        }
    }
```

根据explain分析结果，我们简单分析下文档1的相关性算分过程，去理解ES的相关性算分：

上述查询DSL中： **"title": "es的相关度"**这个查询条件，根据我们采用的是**ik_smart**分词器，会被分词为**es**、**的**、**相关**、**度**四个词元去查询，四个词元的总分就是该查询条件的总分。我们以**es**词元来讲解explain评分结果。

### 3.4.1.explain结果

返回结果如下,以排序第一 的文档为例：

```java
文档_source:{author=bboss开源引擎, id=1, tag=[1, 2, 3], title=es的相关度, content=这是关于es的相关度的文章, createAt=2020-05-24 10:56:00, influence={gte=10, lte=12}} 
_explanation:
{
    "value" : 2.5933092,
    "description" : "sum of:",
    "details" : [
        {
            "value" : 0.31387398,
            "description" : "weight(title:es in 0) [PerFieldSimilarity], result of:",
            "details" : [
                {
                    "value" : 0.31387398,
                    "description" : "score(freq=1.0), product of:",
                    "details" : [
                        {
                            "value" : 2.2,
                            "description" : "boost",
                            "details" : [ ]
                        },
                        {
                            "value" : 0.35667494,
                            "description" : "idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:",
                            "details" : [
                                {
                                    "value" : 3,
                                    "description" : "n, number of documents containing term",
                                    "details" : [ ]
                                },
                                {
                                    "value" : 4,
                                    "description" : "N, total number of documents with field",
                                    "details" : [ ]
                                }
                            ]
                        },
                        {
                            "value" : 0.4,
                            "description" : "tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:",
                            "details" : [
                                {
                                    "value" : 1.0,
                                    "description" : "freq, occurrences of term within document",
                                    "details" : [ ]
                                },
                                {
                                    "value" : 1.2,
                                    "description" : "k1, term saturation parameter",
                                    "details" : [ ]
                                },
                                {
                                    "value" : 0.75,
                                    "description" : "b, length normalization parameter",
                                    "details" : [ ]
                                },
                                {
                                    "value" : 4.0,
                                    "description" : "dl, length of field",
                                    "details" : [ ]
                                },
                                {
                                    "value" : 3.0,
                                    "description" : "avgdl, average length of field",
                                    "details" : [ ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
        ...
	]
}
```

### 3.4.2.打分计算方式

每一层都包含了三个元素`value`、`description`、`details`，

value：最终得分

description：描述details元素的组合方式，最外层一般是 `"sum of:"`，即details每个元素相加为value

details：是一个数组，记录value的得分细节。比如BM25得分就是由idf和tf两个元素相乘得出。

### 3.4.3.词元得分

**`es`词元得分分析**：

1. boost得分

boost得分是权重得分，基数2.2；指定boost数值，那boost得分就是2.2*boost，第四章节会重点介绍

2. idf得分

```java
{
     "value" : 0.44183275,
     "description" : "idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:",
     "details" : [
         {
             "value" : 4,
             "description" : "n, number of documents containing term",
             "details" : [ ]
         },
         {
             "value" : 6,
             "description" : "N, total number of documents with field",
             "details" : [ ]
         }
      ]
}
```

根据idf公式，结合details信息得出：n（docFreq 包含该单词的文档数）= 3，N（numDocs文档总数） = 4，底数为e。计算出 
```java
_score(idf) = log(1+(4-3+0.5)/3+0.5)= ln(1.42) = 0.35667494
```

这就是**es**词元的idf得分

3. tf得分

```java
 {
     "value" : 0.4,
     "description" : "tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:",
     "details" : [
         {
             "value" : 1.0,
             "description" : "freq, occurrences of term within document",
             "details" : [ ]
         },
         {
             "value" : 1.2,
             "description" : "k1, term saturation parameter",
             "details" : [ ]
         },
         {
             "value" : 0.75,
             "description" : "b, length normalization parameter",
             "details" : [ ]
         },
         {
             "value" : 4.0,
             "description" : "dl, length of field",
             "details" : [ ]
         },
         {
             "value" : 3.0,
             "description" : "avgdl, average length of field",
             "details" : [ ]
         }
     ]
 }
```

根据tf公式，结合details的信息，计算出
```java
_score(tf)=1/(1+1.2*(1-0.75+0.75*4/3 ) = 0.4
```

这就是**es**词元的tf得分。这里的tf是**df**和**字段归一值norm**的综合得分

4. BM25得分

根据BM25公式，结合details的信息，计算出
```java
_score(BM25) = idf * tf = 0.35667494*0.4 = 0.142669976‬
```

5. 词元总分

**es**词元的总分为details里所有项的乘积，除了tf/idf得分，还有一个boost因子，因此**es**词元的得分是：

```java
_score = boost * idf * tf = 2.2*0.35667494*0.4 = 0.38881284
```

6. 文档总分

根据文档1得分描述，各个词元的得分组合计算方式为 **"sum of:"**，那么文档的总分为词元总分相加，即为2.5933092。

# 4.相关度控制

通过上面的学习，我们已经知道了什么是TF/IDF，什么是BM25，同时通过explain大致了解了ES的相关性算分过程。那么如果ES默认的相关性算分不符合我们的使用需求，我们可以通过哪些方式去改变或控制相关度评分呢？

一般我们有以下四种策略:

- boost参数（权重因子）
- 搜索评分算法
- rescore结果集重新评分
- 更改BM25参数k1和b的值

## 4.1.boost 参数

我们检索博客时，我们一般会认为标题 title 的权重应该比内容 content 的权重大，那么这个时候我们就可以使用boost参数进行控制。测试DSL如下

```xml
<property name="testBoost" desc="boost 测试字段权重">
        <![CDATA[
            {
                "explain": true,
                "query": {
                    "bool": {
                      "must": [
                        {
                          "match": {
                            "title": {
                              "query": #[title],
                              "boost": #[boost]
                            }
                          }
                        },
                        {
                          "match": {
                            "content": #[title]
                          }
                        }
                      ]
                    }
                }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
 /**
	 * boost 测试字段权重
	 */
	@Test
	public void testBoost() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");
			//查询参数
			Map<String, Object> queryParamsMap = new HashMap<>();
			queryParamsMap.put("title", "es");
			queryParamsMap.put("boost", 2);
			queryParamsMap.put("content", "es");
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("explain_index/_search?search_type=dfs_query_then_fetch",
					"testBoost",//DSL模板ID
					queryParamsMap,//查询参数
					MetaMap.class);//文档信息

			//ES返回结果遍历 结果集不能为空，否则会报空指针
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{} \n_explanation:\n{}", metaMap,
						SimpleStringUtil.object2json(metaMap.getExplanation())
				);
			});
		} catch (ElasticSearchException e) {
			logger.error("testBoost 执行失败", e);
		}
	}
```

返回结果如下,以排序第一 的文档为例：

```java
文档_source:{author=bboss开源引擎, id=3, tag=[2, 3, 4], title=es, content=这是关于关于es和编程的必看文章, createAt=2020-05-22 10:56:00, influence={gte=12, lte=15}}
_explanation:
 {
     "value" : 1.3491198,
     "description" : "sum of:",
     "details" : [
         {
             {
              "value" : 0.9808561,
              "description" : "weight(title:es in 2) [PerFieldSimilarity], result of:",
              "details" : [
                {
                  "value" : 0.9808561,
                  "description" : "score(freq=1.0), product of:",
                  "details" : [
                    {
                      "value" : 4.4,
                      "description" : "boost",
                      "details" : [ ]
                    }
                    ...
                  ]
                }
              ]
            },
            {
              "value" : 0.36826366,
              "description" : "weight(content:es in 2) [PerFieldSimilarity], result of:",
              "details" : [
                {
                  "value" : 0.36826366,
                  "description" : "score(freq=1.0), product of:",
                  "details" : [
                    {
                      "value" : 2.2,
                      "description" : "boost",
                      "details" : [ ]
                    }
                    ...
                ]
              ]
            }
         }
}
```

根据结果，我们可以看到：**title:es**词元的boost得分由默认的2,2，变成4.4,而**content:es**词元的boost得分仍然是默认值2.2。这样我们就给不同的字段，设置不同的权重，从而改变文档的相关度。

### 4.1.1.boost 参数值范围

- boost>1 相关度相对性提升
- 0<boost<1，相对性降低
- boost<0，贡献负分

**注意：**

1. boost 可用于任何查询语句
2. 这种提升或降低并不一定是线性的，新的评分 _score 会在应用权重提升之后被归一化 ，每种类型的查询都有自己的归一算法。

## 4.2.搜索评分算法

ES是天然的搜索引擎，因此提供了很多搜索算法和评分算法的API，本文简要介绍以下4种打分方式。[Bboss文档社区](https://esdoc.bbossgroups.com/#/quickstart)对这四种打分方式也做了单独文档介绍，下文会提供对应文档链接。

### 4.2.1.constant_score

constant_score：常量打分。嵌套一个filter查询，为任意一个匹配的文档指定一个常量评分，常量值为boost 的参数值(默认值为1) ，忽略 TF-IDF 信息。

查询DSL如下：

```xml
<property name="testConstantScore" desc="constant_score 指定分数打分测试">
        <![CDATA[
            {
                "explain": true,
                "query": {
                    "constant_score": {
                      "filter": {
                        "term": {
                          "title": #[title]
                        }
                      },
                      "boost": #[boost]
                    }
                }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
	/**
	 * constant_score 指定分数打分测试
	 */
	@Test
	public void testConstantScore() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");


			//查询参数
			Map<String, Object> queryParamsMap = new HashMap<>();
			queryParamsMap.put("title", "es");
			queryParamsMap.put("boost", 1.2);

			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("explain_index/_search?search_type=dfs_query_then_fetch",
					"testConstantScore",//DSL模板ID
					queryParamsMap,//查询参数
					MetaMap.class);//文档信息

			//ES返回结果遍历 结果集不能为空，否则会报空指针
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{} \n_explanation:\n{}", metaMap,
						SimpleStringUtil.object2json(metaMap.getExplanation())
				);
			});
		} catch (ElasticSearchException e) {
			logger.error("testConstantScore 执行失败", e);
		}
	}
```

返回结果如下,以排序第一 的文档为例：

```java
文档_source:{author=bboss开源引擎, id=3, tag=[2, 3, 4], title=es, content=这是关于关于es和编程的必看文章, createAt=2020-05-22 10:56:00, influence={gte=12, lte=15}} 
_explanation:
{
    "value":1.2,
    "description":"ConstantScore(title:es)^1.2","details":[]
}
```

可以看到，包含**es**的文档得分已经变成了我们指定的1.2分，而不受BM25等相关度算法的影响。

### 4.2.2.function_score

FunctionScore：函数打分。在使用时，我们必须定义一个查询和一个或多个函数，每个函数为查询返回的每个文档计算一个新分数。再由FunctionScore指定方式综合计算文档总分。详细案例参考Bboss文档社区[**通过Function Score Query优化Elasticsearch搜索结果(综合排序)**](https://esdoc.bbossgroups.com/#/function_score?id=通过function-score-query优化elasticsearch搜索结果综合排序)。

查询DSL如下：

```xml
<property name="testFunctionScore" desc="FunctionScore 函数评分测试">
        <![CDATA[
            {
              "explain": true,
              "query": {
                "function_score": {
                  "query": {
                    "match_all": {}
                  },
                  "functions": [
                    {
                      "filter": {
                        "match": {
                          "title": #[title]
                        }
                      },
                      "weight": 23
                    },
                    {
                      "filter": {
                        "match": {
                          "title": #[weightTitle]
                        }
                      },
                      "weight": 42
                    }
                  ],
                  "boost": #[boost],
                  "score_mode": "max",
                  "boost_mode": "sum",
                  "max_boost": 42,
                  "min_score": 10
                }
              }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
	/**
	 * FunctionScore 函数评分测试
	 */
	@Test
	public void testFunctionScore() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");

			//查询参数
			Map<String, Object> queryParamsMap = new HashMap<>();
			queryParamsMap.put("title", "es");
			queryParamsMap.put("weightTitle", "相关度");
			queryParamsMap.put("boost", 5);

			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("explain_index/_search?search_type=dfs_query_then_fetch",
					"testFunctionScore",//DSL模板ID
					queryParamsMap,//查询参数
					MetaMap.class);//文档信息

			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{} \n_explanation:\n{}", metaMap,
						SimpleStringUtil.object2json(metaMap.getExplanation())
				);
			});
		} catch (ElasticSearchException e) {
			logger.error("testFunctionScore 执行失败", e);
		}
	}

```

返回结果如下,以排序第一 的文档为例：

```java
文档_source:{author=bboss开源引擎, id=1, tag=[1, 2, 3], title=es的相关度, content=这是关于es的相关度的文章, createAt=2020-05-24 10:56:00, influence={gte=10, lte=12}} 
_explanation:
{
    "value": 47,
    "description": "sum of",
    "details": [
        {
            "value": 5,
            "description": "*:*^5.0",
            "details": []
        },
        {
            "value": 42,
            "description": "min of:",
            "details": [
                {
                    "value": 42,
                    "description": "function score, score mode [max]",
                    "details": [
                        {
                            "value": 23,
                            "description": "function score, product of:",
                            "details": [...]
                        },
                        {
                            "value": 42,
                            "description": "function score, product of:",
                            "details": [...]
                        }
                    ]
                },
                {
                    "value": 42,
                    "description": "maxBoost",
                    "details": []
                }
            ]
        }
    ]
}
```

根据explain信息和查询DSL，简要解释下FunctionScore。

#### function_score参数

1. functions部分

	根据query得到的文档，在functions进行二次打分，而filter过滤是布尔查询，满足条件的分值为1，而我们给**es**，**相关度**两个词元的权重分别是23，和42，那么这两个词元的得分乘以filter得分也是23，和42。而**score_mode**指定了functions内部只取最大值，那么functions整体的得分就是42。

2. 参数解释

	**max_boost**：functions内部单个函数查询的最大分（这里以两个filter举例），超过这个最大分文档将被丢弃。

	**min_score**：同上，小于这个分值的文档将被丢弃。

	**score_mode**：functions内部单个函数查询的取值方式。

	- multiply: 函数结果会相乘(默认行为)
	- sum：函数结果会累加
	- avg：得到所有函数结果的平均值
	- max：得到最大的函数结果
	- min：得到最小的函数结果
	- first：只使用第一个函数的结果，该函数可以有过滤器，也可以没有

	**boost_mode**：functions得分和和functions外部查询得分的结合方式。

	- multiply：_score乘以函数结果(默认情况)
	- sum：_score加上函数结果
	- min：_score和函数结果的较小值
	- max：_score和函数结果的较大值
	- replace：将_score替换成函数结果

根据上述查询DSL：boost_mode指定为sum，而functions外部还存在一个boost得分，那么文档最终得分就是functions得分+boost得分=47分。

### 4.2.3.dis_max

dis_max：最佳字段查询。可以通过参数 tie_breaker（默认值为0），控制其他字段的分数对_score 的影响。

**注意**

-  考虑所有匹配语句，通过tie_breaker值大小决定最佳匹配字段的权重。

- tie_breaker可以是浮点数，其中默认值0表示普通查询，类似terms查询, 1表示所有匹配语句同等重要。

查询DSL如下：

```xml
<property name="testDisMax" desc="dis_max 最佳字段得分测试">
        <![CDATA[
            {
              "explain": true,
              "query": {
                "dis_max": {
                  "queries": [
                    {
                      "term": {
                        "content": #[content1]
                      }
                    },
                    {
                      "match": {
                        "content": #[content2]
                      }
                    }
                  ],
                  "tie_breaker": #[tie_breaker]
                }
              }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
/**
	 * dis_max 最佳字段得分测试
	 */
	@Test
	public void testDisMax() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");
			//查询参数
			Map<String, Object> queryParamsMap = new HashMap<>();
			queryParamsMap.put("content1", "es");
			queryParamsMap.put("content2", "相关度");
			queryParamsMap.put("tie_breaker", 0.5);
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("explain_index/_search?search_type=dfs_query_then_fetch",
					"testDisMax",//DSL模板ID
					queryParamsMap,//查询参数
					MetaMap.class);//文档信息

			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{} \n_explanation:\n{}", metaMap,
						SimpleStringUtil.object2json(metaMap.getExplanation())
				);
			});
		} catch (ElasticSearchException e) {
			logger.error("testDisMax 执行失败", e);
		}
	}
```

返回结果如下,以排序第一 的文档为例：

```java
文档_source:{author=bboss开源引擎, id=1, tag=[1, 2, 3], title=es的相关度, content=这是关于es的相关度的文章, createAt=2020-05-24 10:56:00, influence={gte=10, lte=12}} 
_explanation:
{
    "value": 0.9623494,
    "description": "max plus 0.5 times others of:",
    "details": [
        {
            "value": 0.38493976,
            "description": "weight(content:es in 0) [PerFieldSimilarity], result of:",
            "details": [
                {
                    "value": 0.38493976,
                    "description": "score(freq=1.0), product of:",
                    "details": [...]
                }
            ]
        },
        {
            "value": 0.7698795,
            "description": "sum of:",
            "details": [...]
        }
    ]
}
```

dis_max 计算公式：
```java
_score(dis_max) = max(BM25) + ∑other(BM25) * tie_breaker
```

结合dis_max公式和_explanation详情，我们就可以计算出文档总分。

### 4.2.4.boosting

boosting：结果集字段权重评分。查询可以实现对文档结果集的二次权重打分，提升或者降低指定词元的相关度。

**参数解释：**

- positive：查询条件
- negative：对positive查询结果进行相关度调整
- negative_boost：调整参数，升权(>1), 降权(>0 and <1)，和negative相乘为最终得分。

不同于boost，只是在搜索是设置权重，分数过低的文档会被丢弃，boosting仍会选择指定词元的文档，但可以修改其总体得分。

查询DSL如下：

```xml
 <property name="testBoosting" desc="boosting 结果集权重测试">
        <![CDATA[
            {
              "explain": true,
              "query": {
                "boosting": {
                  "positive": {
                    "bool": {
                      "should": [
                        {
                          "term": {
                            "title": #[positive1]
                          }
                        },
                        {
                          "term": {
                            "title": #[positive2]
                          }
                        }
                      ]
                    }
                  },
                  "negative": {
                    "term": {
                      "content": #[negative]
                    }
                  },
                  "negative_boost": #[boostNum]
                }
              }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
	/**
	 * boosting 结果集权重测试
	 */
	@Test
	public void testBoosting() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");

			//查询参数
			Map<String, Object> queryParmsMap = new HashMap<>();
			queryParmsMap.put("positive1", "es");
			queryParmsMap.put("positive2", "相关性");
			queryParmsMap.put("negative", "编程");
			queryParmsMap.put("boostNum", 0.2);
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("explain_index/_search?search_type=dfs_query_then_fetch",
					"testBoosting",//DSL模板ID
					queryParmsMap,//查询参数
					MetaMap.class);//文档信息

			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{} \n_explanation:\n{}", metaMap,
						SimpleStringUtil.object2json(metaMap.getExplanation())
				);
			});
		} catch (ElasticSearchException e) {
			logger.error("testBoosting 执行失败", e);
		}
	}
```

返回结果如下,以包含negative词元的文档为例：

```java
文档_source:{author=bboss开源引擎, id=3, tag=[2, 3, 4], title=es, content=这是关于关于es和编程的必看文章, createAt=2020-05-22 10:56:00, influence={gte=12, lte=15}} 
_explanation:
{
    "value" : 0.09808561352022904,
    "description" : "weight(FunctionScoreQuery(title:es title:相关性, scored by boost(queryboost(score(content:编程))^0.2))), result of:",
    "details" : [
        {
            "value" : 0.09808561352022904,
            "description" : "product of:",
            "details" : [
                {
                    "value" : 0.49042806,
                    "description" : "sum of:",
                    "details" : []
                },
                {
                    "value" : 0.2,
                    "description" : "Matched boosting query score(content:编程)",
                    "details" : [ ]
                }
            ]
        }
    ]
}
```

boosting 计算公式：
```java
_score = positive * negative_boost
```

结合boosting公式和_explanation详情，可以看出文档boosting查询的positive得分是0.49042806,由于命中了negative指定词元，总分变成0.49042806(positive)*0.2(negative_boost)。

### 4.2.5.rescore

rescore：结果集重新评分。先query，再在结果集基础上 rescore。query目前唯一支持的重新打分算法。参数window_size 是每一分片进行重新评分的顶部文档数量。

rescore 和 上面的 Boosting Query 是比较相似的，都是在 query 结果集的基础上重新修改相关性得分。但是修改的算法是不一样的，根据场景需求，选择即可。

**参数解释：**

- window_size：需要重新打分的文档数，从返回的第一个文档开始计算。
- query_weight：rescore以外query得分的权重。
- rescore_query_weight：rescore得分的权重。

查询DSL如下：

```xml
 <property name="testRescore" desc="rescore 结果集重新打分">
        <![CDATA[
            {
              "explain": true,
              "query": {
                "bool": {
                  "should": [
                    {
                      "match": {
                        "content": {
                          "query": #[content]
                        }
                      }
                    },
                    {
                      "match": {
                        "title": {
                          "query": #[title]
                        }
                      }
                    }
                  ]
                }
              },
              "rescore": {
                "window_size": #[window_size],
                "query": {
                  "rescore_query": {
                    "match_phrase": {
                      "content": {
                        "query": #[rescore_query],
                        "slop": 50
                      }
                    }
                  },
                  "query_weight": #[query_weight],
                  "rescore_query_weight": #[rescore_query_weight]
                }
              }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
/**
	 * rescore 结果集重新打分
	 */
	@Test
	public void testRescore() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");

			//查询参数
			Map<String, Object> queryParmsMap = new HashMap<>();
			queryParmsMap.put("content", "es的相关度");
			queryParmsMap.put("title", "es");
			queryParmsMap.put("rescore_query", "es的相关度");
			queryParmsMap.put("window_size", 2);
			queryParmsMap.put("query_weight", 0.7);
			queryParmsMap.put("rescore_query_weight", 1.2);
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("explain_index/_search?search_type=dfs_query_then_fetch",
					"testRescore",//DSL模板ID
					queryParmsMap,//查询参数
					MetaMap.class);//文档信息

			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{} \n_explanation:\n{}", metaMap,
						SimpleStringUtil.object2json(metaMap.getExplanation())
				);
			});
		} catch (ElasticSearchException e) {
			logger.error("testRescore 执行失败", e);
		}
	}
```

返回结果如下,以排序第一 的文档为例：

```java
文档_source:{author=bboss开源引擎, id=1, tag=[1, 2, 3], title=es的相关度, content=这是关于es的相关度的文章, createAt=2020-05-24 10:56:00, influence={gte=10, lte=12}} 
_explanation:
{
    "value": 2.6571212,
    "description": "sum of:",
    "details": [
        {
            "value": 1.1348861,
            "description": "product of:",
            "details": [
                {
                    "value": 1.621266,
                    "description": "sum of:",
                    "details": []
                },
                {
                    "value": 0.7,
                    "description": "primaryWeight",
                    "details": []
                }
            ]
        },
        {
            "value": 1.522235,
            "description": "product of:",
            "details": [
                {
                    "value": 1.2685292,
                    "description" : """weight(content:"es 的 相关 度"~50 in 0) [PerFieldSimilarity], result of:""",
                    "details": []
                },
                {
                    "value": 1.2,
                    "description": "secondaryWeight",
                    "details": []
                }
            ]
        }
    ]
}
```

rescore 计算公式：
```java
_score = score(BM25) * query_weight + score(rescore) * rescore_query_weight
```

结合rescore 公式和_explanation详情，我们就可以计算出文档总分。

### 4.2.6.boolean query

布尔查询可以参考ES社区的一篇文章[Bool query](http://mp.weixin.qq.com/s?__biz=MzIxMjE3NjYwOQ==&mid=2247483976&idx=1&sn=f9fc58f7f38ef79d4a652a9578ce1181&chksm=974b59c6a03cd0d036f9e1cc9d211b999c9d3acdd664f4a250a1573089fdfe747c7784191066&scene=21#wechat_redirect)

## 4.3.更改BM25 参数 k1 和 b 的值

### 4.3.1.关于修改相关度

在第二章节，我们知道了ES提供了几种文档相关度算法，ES也提供了修改相关度参数的API。

在ES官方文档中对修改相关度参数称为`The rabbit hole`(兔子洞)，是一个无尽的循环，官方建议通过用户行为和搜索算法去优化搜索结果，而不是一味的修改相关度算法。

### 4.3.2.BM25更改方法

在介绍BM25算法时，我们知道 k1 参数（默认值1.2）控制着词频结果在词频饱和度中的上升速度。b 参数（默认值0.75）控制着字段长归一值所起的作用。那么我们就可以通过手动定义这两个参数的值，从而去改变相关性算分。

修改BM25，只能通过字段similarity属性指定自定义相关度实现。一般有两种实现方式：

- 创建mapping时指定similarity为自定义相关度
- 关闭索引，修改similarity为自定义相关度

修改BM25DSL如下：

```xml
<property name="setBM25" desc="设置BM25的参数">
        <![CDATA[
            {
                "settings": {
                    "similarity": {
                      #[my_bm25]: {
                        "type": "BM25",
                        "k1": #[k1],
                        "b": #[b]
                      }
                    }
                },
                "mappings": {
                    "properties": {
                      "title": {
                        "type": "text",
                        "similarity": #[my_bm25]
                      },
                      "body": {
                        "type": "text",
                        "similarity": "BM25"
                      }
                    }
                }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
	/**
	 * 设置BM25的参数
	 */
	@Test
	public void setBM25() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");//bboss读取xml
			/*检查索引是否存在，存在就删除重建*/
			if (clientInterface.existIndice("set_bm25_index")) {
				clientInterface.dropIndice("set_bm25_index");
			}
			Map<String,Object> indexParms = new HashMap<>();
			indexParms.put("my_bm25","my_bm25");
			indexParms.put("k1",2);
			indexParms.put("b",0);

			clientInterface.createIndiceMapping("set_bm25_index", "setBM25",indexParms);
			logger.info("创建索引 set_bm25_index 成功");
		} catch (ElasticSearchException e) {
			logger.error("创建索引 set_bm25_index 执行失败", e);
		}
	}
```

更改完相似度，就可以用explain查看BM25评分公司的改变。

# 5.相关度不准的疑问

## 5.1.被破坏的相关度

每个分片都会根据该分片内的所有文档计算一个IDF评分。当数据量很少时，这会导致打分偏离。

相关性算分的IDF 在分⽚之间是相互独⽴。当⽂档总数很少的情况下，主分⽚数越多 ，相关性算分会越不准。

### 5.1.1.现象示例

重建explain_index索引，修改分片数。

**注意：**

**副本可以通过关闭索引修改，但是分片数在索引创建后无法被修改，建立索引前一定要预估好数据量和分片的关系。**

重建DSL如下：

```xml
<property name="rebuildExplainIndex" desc="重建explain测试索引">
        <![CDATA[
            {
              "settings": {
                "index": {
                  "number_of_shards": #[number_of_shards],
                  "number_of_replicas": #[number_of_replicas]
                }
              },
              "mappings": {
                "properties": {
                  "id": {
                    "type": "integer"
                  },
                  "author": {
                    "type": "keyword"
                  },
                  "title": {
                    "type": "text",
                    "analyzer": "ik_smart"
                  },
                  "content": {
                    "type": "text",
                    "analyzer": "ik_max_word",
                    "search_analyzer": "ik_smart"
                  },
                  "tag": {
                    "type": "keyword"
                  },
                  "influence": {
                    "type": "integer_range"
                  },
                  "createAt": {
                    "type": "date",
                    "format": "yyyy-MM-dd HH:mm:ss"
                  }
                }
              }
            }
        ]]>
    </property>
```

bboss执行上述模板：

```java
/**
	 * 重建explain测试索引
	 */
	@Test
	public void rebuildExplainIndex() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/doc_relevancy.xml");//bboss读取xml
			/*检查索引是否存在，存在就删除重建*/
			if (clientInterface.existIndice("explain_index")) {
				clientInterface.dropIndice("explain_index");
			}
			Map<String, Object> indexParms = new HashMap<>();
			indexParms.put("number_of_shards", 10);
			indexParms.put("number_of_replicas", 2);

			clientInterface.createIndiceMapping("explain_index", "rebuildExplainIndex", indexParms);
			logger.info("重建索引 explain_index 成功");
		} catch (ElasticSearchException e) {
			logger.error("重建索引 explain_index 执行失败", e);
		}
	}
```

执行第三章节的导入测试数据，重新导入数据，再次执行第四章节的相关度控制测试用例，就能发现，新建索引10个分片的搜索结果和默认一个分片时的结果并不一致。

### 5.1.2.两种方式解决

- 当数据量不大时，将主分片数设置为1，这也是ES默认的配置
- search_type：指定搜索方式。搜索的URL 中指定参数 `/_search?search_type=dfs_query_then_fetch`。在第四章节我们搜索的搜索用例都使用了这种方式。默认是`query then fetch`，可以自行度娘。

在实际应用中，这并不是一个问题，本地和全局的IDF的差异会随着索引里文档数的增多渐渐消失，在生产环境，局部的 IDF 会被迅速均化，所以上述问题并不是相关度被破坏所导致的，而是由于数据太少。

# 6.相关度搜索建议

1. 理解评分过程是非常重要的，这样就可以根据具体的业务对评分结果进行调试、调节、减弱和定制。

2. 本文介绍的3种相关度控制方案，建议结合实践，根据自己的业务需求，多动手调试练习。

3. 最相关 这个概念是一个难以触及的模糊目标，通常不同人对文档排序又有着不同的想法，这很容易使人陷入持续反复调整而没有明显进展的怪圈。**强烈建议不要去追求最相关，而要监控测量搜索结果。**

4. **评价搜索结果与用户之间相关程度的指标。**如果查询能返回高相关的文档，用户会选择前五中的一个，得到想要的结果，然后离开。不相关的结果会让用户来回点击并尝试新的搜索条件。

5. 要想物尽其用并将搜索结果提高到极高的水平，唯一途径就是需要具备能评价度量用户行为的强大能力。

# 7.相关资料

相关度官方文档

https://www.elastic.co/guide/cn/elasticsearch/guide/current/controlling-relevance.html

相关度控制文档

https://mp.weixin.qq.com/s?__biz=MzIxMjE3NjYwOQ==&mid=2247483997&idx=1&sn=fb27712c41806adaea934b30d215faac&scene=19#wechat_redirect

# 8.开发交流

bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">