# Elasticsearch实现同段和同句搜索

不同于 term、match等条件查询；同句搜索要求搜索多个关键词时，返回的文章不只要包含关键词，而且这些关键词必须在同一句中。同段搜素类似，只是范围为同一段落。而实现同段/同句搜索，就需要使用Span Query。

# 1.Span Query

案例源码工程:

https://github.com/rookieygl/bboss-wiki

本案例以Elasticsearch开源java rest client客户端bboss开发：

https://esdoc.bbossgroups.com/#/README

## 1.1.Span Query介绍

对于上述搜索场景，Elasticsearch提供了 SpanQuery，官方文档中如下的介绍：

> Span queries are low-level positional queries which provide expert control over the order and proximity of the specified terms. These are typically used to implement very specific queries on legal documents or patents.

上面提到，SpanQuery 常常应用在法律或专利的特定搜索。这些领域，常常提供同段 /同句搜索 。
下面我们看一下三种类型的 SpanQuery，能否实现我们的需求。

## 1.2.Span Query使用案例

### 1.2.1.案例准备工作

本文以一个article索引检索作为案例来介绍Span Query的一些具体用法。

开始之前要先创建DSL的配置文件，位置在resources/esmapper/span_query.xml（Git地址：[https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/span_query.xml](https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/field_collapsing.xml）。)）。本文涉及到的DSL都会放到该配置文件。

#### 1.2.1.1.创建索引

在配置文件中添加创建索引的mapping，名称为createArticleIndice，索引名称在代码中指定。

```java
  <!--SpanQuery测试案例-->

    <!--创建SpanQuery测试索引-->
    <property name="createArticleIndice">
        <![CDATA[{
            "settings" : {
                "number_of_shards" : 1,
                "number_of_replicas" : 1
            },
            "mappings": {
                "properties": {
                    "maincontent": {
                        "type": "text"
                    }
                }
            }
        }]]>
    </property>
```

Bboss执行上面的DSL:

```java
	private Logger logger = LoggerFactory.getLogger(FunctionScoreTest.class);//日志

	@Autowired
	private BBossESStarter bbossESStarter;//bboss启动器

	private ClientInterface clientInterface;//bboss dsl工具

	/**
	 * 创建article索引
	 */
	@Test
	public void dropAndCreateArticleIndice() {
		try {
            //创建一个加载配置文件esmapper/span_query.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/span_query.xml");//bboss读取xml
			/*检查索引是否存在，存在就删除重建*/
			if (clientInterface.existIndice("article")) {
				clientInterface.dropIndice("article");
			}
			clientInterface.createIndiceMapping("article", "createArticleIndice");
			logger.info("创建索引 article 成功");
		} catch (ElasticSearchException e) {
			logger.error("创建索引 article 失败", e);
		}
	}
```

上述为Bboss测试方法，未展示测试类类名，可自行创建。 执行成功，可在ES中查询到对应的索引。

#### 1.2.1.2.添加索引数据

将准备的JSON数据写入到配置文件，Bboss可以将JSON数据插入到ES。数据DSL如下：

```java
 <!--添加索引数据-->
    <property name="bulkInsertArticleData">
        <![CDATA[
            {"index" : {"_index" : "article" }}
            {"maincontent":"the quick red fox jumps over the sleepy cat"}
            {"index" : {"_index" : "article" }}
            {"maincontent":"the quick brown fox jumps over the lazy dog"}
        ]]>
    </property>
```

添加数据使用Bboss的ESInfo工具类，可以直接解析JSON数据转为ES文档实例，非常方便和高效。

Bboss执行上面的DSL:

```java
/**
	 * 添加article索引数据
	 */
	@Test
	public void insertIndiceData() {
		try {
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/span_query.xml");//bboss读取xml
			ClientInterface restClient = ElasticSearchHelper.getRestClientUtil();//插入数据用RestClient
			ESInfo esInfo = clientInterface.getESInfo("bulkInsertArticleData");//获取插入数据
			StringBuilder recipedata = new StringBuilder();
			recipedata.append(esInfo.getTemplate().trim())
					.append("\n");//换行符不能省
			restClient.executeHttp("article/_bulk?refresh", recipedata.toString(), ClientUtil.HTTP_POST);
		} catch (ElasticSearchException e) {
			logger.error("article 插入数据失败", e);
		}
		long recipeCount = clientInterface.countAll("article");
		logger.info("article 当前条数：{}", recipeCount);
	}
```

执行后，就能在索引中看到对应的数据。

#### 1.2.1.3.数据导入推荐

上述插入数据使用_bulk接口可以快速插入数据，对于大数据迁移Bboss封装了bulkProcessor，支持多线程导入数据，性能非常可观。详情请参考

https://esdoc.bbossgroups.com/#/bulkProcessor

## 1.3.SpanTermQuery

SpanTermQuery 和 Term Query 类似, 只会返回包含指定条件的文档，查询DSL如下：

```java
<!--测试SpanTermQuery-->
    <property name="testSpanTermQuery">
        <![CDATA[{
            "query": {
                "span_term": {
                    "maincontent": {
                        "value": #[spanTermValue]
                    }
                }
            }
        }]]>
    </property>
```

Bboss执行上面的DSL：

```java
/**
	 * 测试SpanTermQuery
	 */
	@Test
	public void testSpanTermQuery() {
		try {
            //创建一个加载配置文件esmapper/span_query.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/span_query.xml");
			//封装请求参数
			Map<String, String> queryParams = new HashMap<>(5);
			queryParams.put("spanTermValue", "red");
			//Bboss执行查询DSL
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("article/_search?search_type=dfs_query_then_fetch",
					"testSpanTermQuery",//DSL模板ID
					queryParams,//查询参数
					MetaMap.class);//文档信息

			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{}", metaMap);
			});
		} catch (ElasticSearchException e) {
			logger.error("testSpanTermQuery 执行失败 ", e);
		}
	}
```

返回文档内容如下：

```java
文档_source:{maincontent=the quick red fox jumps over the sleepy cat}
```

## 1.4.SpanNearQuery

SpanNearQuery表示邻近搜索，查找多个 term 是否邻近。同时具有以下属性

- slop：可以设置邻近距离。如果设置为0，那么代表两个term的词是挨着的，相当于 match_phrase。 
- in_order：代表文档中的 term 和查询设置的 term 保持相同的顺序。

查询DSL如下：

```java
  <!--测试SpanTermQuery-->
    <property name="testSpanNearQuery">
        <![CDATA[{
            "query": {
                "span_near": {
                "clauses": [
                    {
                        "span_term": {
                            "maincontent": {
                                "value": #[spanTermValue1]
                            }
                        }
                    },
                    {
                        "span_term": {
                            "maincontent": {
                              "value": #[spanTermValue2]
                            }
                        }
                    }
                ],
                "slop": #[slop],
                "in_order": true
                }
            }
        }]]>
    </property>
```

Bboss执行上面的DSL：

```java
/**
	 * 测试SpanNearQuery
	 */
	@Test
	public void testSpanNearQuery() {
		try {
            //创建一个加载配置文件esmapper/span_query.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/span_query.xml");
			//封装请求参数
			Map<String, String> queryParams = new HashMap<>(5);
			queryParams.put("spanTermValue1", "quick");
			queryParams.put("spanTermValue2", "brown");
			queryParams.put("slop", "0");

			//Bboss执行查询DSL
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("article/_search?search_type=dfs_query_then_fetch",
					"testS_panNearQuery",//DSL模板ID
					queryParams,//查询参数
					MetaMap.class);//文档信息
			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{}" + metaMap);
			});
		} catch (ElasticSearchException e) {
			logger.error("testSpanTermQuery 执行失败", e);
		}
	}
```

返回文档内容如下：

```java
文档_source:{maincontent=the quick brown fox jumps over the lazy dog}
```

## 1.5.SpanNotQuery

SpanNotQuery 非常重要，它要求两个 SpanQuery 的跨度，不能够重合。
看下面的例子:

- include：匹配的 SpanQuery，例子为需要一个包含 quick 和 fox 两个词的邻近搜索。
- exclude：设置被排除SpanQuery，类似must_not。

查询DSL如下：

```java
 <!--测试SpanNotQuery-->
    <property name="testSpanNotQuery ">
        <![CDATA[{
          "query": {
            "span_not": {
              "include": {
                "span_near": {
                  "clauses": [
                    {
                      "span_term": {
                        "maincontent": {
                          "value": #[spanTermValue1]
                        }
                      }
                    },
                    {
                      "span_term": {
                        "maincontent": {
                          "value": #[spanTermValue2]
                        }
                      }
                    }
                  ],
                  "slop": #[slop],
                  "in_order": true
                }
              },
              "exclude": {
                "span_term": {
                  "maincontent": {
                    "value": #[spanNotValue]
                  }
                }
              }
            }
          }
        }]]>
    </property>
```

Bboss执行上面的DSL：

```java
/**
	 * 测试SpanNotQuery
	 */
	@Test
	public void testSpanNotQuery() {
		try {
            //创建一个加载配置文件esmapper/span_query.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/span_query.xml");
			//封装请求参数
			Map<String, String> queryParams = new HashMap<>(5);
			queryParams.put("spanTermValue1", "quick");
			queryParams.put("spanTermValue2", "fox");
			queryParams.put("slop", "1");
			queryParams.put("spanNotValue", "red");

			//Bboss执行查询DSL
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("article/_search?search_type=dfs_query_then_fetch",
					"testSpanNotQuery",//DSL模板ID
					queryParams,//查询参数
					MetaMap.class);//文档信息
			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{}", metaMap);
			});
		} catch (ElasticSearchException e) {
			logger.error("testSpanNotQuery 执行失败", e);
		}
	}
```

返回文档内容如下：

```java
文档_source: {maincontent=the quick brown fox jumps over the lazy dog}
```

# 2.同段/同句搜索实现

## 2.1.同句/同段搜索原理

同句搜索，就是搜索词不能够跨句。再进一步，就是搜索词之间不能够有。 、？、！等其他标点符号。

示例DSL如下，下面会有实际案例。

```java

GET article/_search
{
  "query": {
    "span_not": {
      "include": {
        "span_near": {
          "clauses": [
            {
              "span_term": {
                "maincontent": {
                  "value": "word1"
                }
              }
            },
            {
              "span_term": {
                "maincontent": {
                  "value": "word2"
                }
              }
            }
          ],
          "slop": 1,
          "in_order": true
        }
      },
      "exclude": {
        "span_term": {
          "maincontent": {
            "value": "。/？/！"
          }
        }
      }
    }
  }
}
```

同段搜素类似，对应分隔符变为 \n,<p> ,</p>等。

## 2.2.IK Analyzer分词器

同段/同句搜索在导入数据时需要使用IK Analyzer分词器。开始案例之前先安装分词器。

分析器版本一定要低于或者等于当前ES版本

### 2.2.1.在线安装

使用ESelasticsearch-plugin命令

/ES_HOMEW/bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.7.0/elasticsearch-analysis-ik-7.7.0.zip。

执行完成重启ES即可

### 2.2.2.离线安装

打开IK的github地址：https://github.com/medcl/elasticsearch-analysis-ik/releases

选择相应的分词器版本，上传到ES的plugins目录下，解压分词器安装包。

一定要删除IK分词器的安装包，只保留解压文件；然后启动ES即可

## 2.3.HTML数据同段搜索

### 2.3.1.案例准备工作

开始之前要现在工作中创建Bboss的DSL配置文件，位置在resources/esmapper/paragraph_words.xml（Git地址：[https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/paragraph_words.xml](https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/field_collapsing.xml）。)）。本文涉及到的DSL都会放到该配置文件。

#### 2.3.1.1.创建索引

在配置文件中添加创建索引的mapping，名称为createSample1Indice，索引名称在代码中指定。在该索引的mapping中指定了一个自定义html数据分词器。

#### 2.3.1.2.Html分词器预期效果

simple1_mainContent分词器的期望是：替换p , h1 , h2标签为统一的分段符：paragraph；

替换中英文 ！ , ？ , 。 标点符号为统一的分页符：sentence。每个词元想个距离是1，用span_term可以判断两个词元是否存在paragraph（同段）、sentence（同句）。

注意：分词器的script要用@进行转义，否则url校验不通过。DSL如下：

```java
<!--创建html分词索引-->
    <property name="createSample1Indice">
        <![CDATA[{
            "settings": {
                "number_of_replicas": 0,
                "number_of_shards": 1,
                "analysis": {
                  "analyzer": {
                    "simple1_mainContent": {
                      "type": "custom",
                      "char_filter": [
                        "sentence_paragrah_mapping",
                        "html_strip"
                      ],
                      "tokenizer": "ik_max_word"
                    }
                  },
                  "char_filter": {
                    "sentence_paragrah_mapping": {
                      "type": "mapping",
                      "mappings": [
                        ## script要用@进行转义，否则url校验不通过
                        @"""<h1> => \u0020paragraph\u0020""",
                        @"""</h1> => \u0020sentence\u0020paragraph\u0020""",
                        @"""<h2> => \u0020paragraph\u0020""",
                        @"""</h2> => \u0020sentence\u0020paragraph\u0020""",
                        @"""<p> => \u0020paragraph\u0020""",
                        @"""</p> => \u0020sentence\u0020paragraph\u0020""",
                        @"""! => \u0020sentence\u0020""",
                        @"""? => \u0020sentence\u0020""",
                        @"""。=> \u0020sentence\u0020""",
                        @"""？=> \u0020sentence\u0020""",
                        @"""！=> \u0020sentence\u0020"""
                      ]
                    }
                  }
                }
                },
                "mappings": {
                    "properties": {
                      "mainContent": {
                        "type": "text",
                        "analyzer": "simple1_mainContent",
                        "search_analyzer": "ik_smart"
                     }
                }
            }
        }]]>
    </property>
```

Bboss执行上面的DSL:

```java
/**
	 * 创建simple1索引
	 */
	@Test
	public void dropAndCreateSample1Indice() {
		try {
            //创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");//bboss读取xml
			/*检查索引是否存在，存在就删除重建*/
			if (clientInterface.existIndice("sample1")) {
				clientInterface.dropIndice("sample1");
			}

			/*传参，创建索引*/
			clientInterface.createIndiceMapping("sample1", "createSample1Indice");
			logger.info("创建索引 sample1 成功");
		} catch (ElasticSearchException e) {
			logger.error("创建索引 sample1 faild", e);
		}
	}
```

执行成功，可在ES中查询到对应的索引和索引的分词器。

#### 2.3.1.3.添加数据

将准备的JSON数据写入到配置文件，Bboss可以将JSON数据插入到ES。数据DSL如下：

```java
    <!--添加索引数据-->
    <property name="bulkSample1Data">
        <![CDATA[
            {"index" : {"_index" : "sample1" }}
            {"mainContent":"<p>java python javascript</p><p>oracle mysql sqlserver</p>"}
        ]]>
    </property>
```

Bboss执行上面的DSL:

```java
	/**
	 * 添加simp1数据
	 */
	@Test
	public void insertSimple1IndiceData() {
		try {
            创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");//bboss读取xml
			ClientInterface restClient = ElasticSearchHelper.getRestClientUtil();//插入数据用RestClient
			ESInfo esInfo = clientInterface.getESInfo("bulkSample1Data");//获取插入数据
			StringBuilder recipedata = new StringBuilder();
			recipedata.append(esInfo.getTemplate().trim());
			recipedata.append("\n");
			restClient.executeHttp("sample1" + "/_bulk?refresh", String.valueOf(recipedata), ClientUtil.HTTP_POST);
		} catch (ElasticSearchException e) {
			logger.error("sample1 插入数据失败 {}", e);
		}
		long recipeCount = clientInterface.countAll("sample1");
		logger.info("sample1 当前条数:{}", recipeCount);
	}
```

执行后，就能在索引中看到对应的数据。

### 2.3.2.测试分词

为了保证同段搜索的效果，先测试下上述定义的分词器效果。如果出现sentence，paragraph词元，证明分词成功，可以进行同段搜索。DSL如下：

```java
 <!--测试html分词效果-->
    <property name="testHtmlAnalyze">
        <![CDATA[{
          "text": ["<p>java python javascript</p><p>oracle mysql sqlserver</p>"],
          "analyzer": "simple1_mainContent"
        }]]>
    </property>
```

Bboss执行上面的DSL:

```java
/**
	 * 测试html分词
	 */
	@Test
	public void testHtmlAnalyze() {
		try {
            创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");//bboss读取xml
			String analyzeResult = clientInterface.executeHttp("sample1" + "/_analyze", "testHtmlAnalyze", ClientUtil.HTTP_POST);
			//分词结果
			logger.info("分词结果:{}", analyzeResult);
		} catch (ElasticSearchException e) {
			logger.error("testHtmlAnalyze 执行失败", e);
		}
	}
```

返回结果内容如下：

```java
分词效果:
{
"tokens":[{"token":"paragraph","start_offset":1,"end_offset":2,"type":"ENGLISH","position":0},{"token":"java","start_offset":3,"end_offset":7,"type":"ENGLISH","position":1},{"token":"python","start_offset":8,"end_offset":14,"type":"ENGLISH","position":2},{"token":"javascript","start_offset":15,"end_offset":25,"type":"ENGLISH","position":3},{"token":"sentence","start_offset":26,"end_offset":28,"type":"ENGLISH","position":4},{"token":"paragraph","start_offset":28,"end_offset":28,"type":"ENGLISH","position":5},{"token":"paragraph","start_offset":30,"end_offset":31,"type":"ENGLISH","position":6},{"token":"oracle","start_offset":32,"end_offset":38,"type":"ENGLISH","position":7},{"token":"mysql","start_offset":39,"end_offset":44,"type":"ENGLISH","position":8},{"token":"sqlserver","start_offset":45,"end_offset":54,"type":"ENGLISH","position":9},{"token":"sentence","start_offset":55,"end_offset":57,"type":"ENGLISH","position":10},{"token":"paragraph","start_offset":57,"end_offset":57,"type":"ENGLISH","position":11}]
}
```

如果词元中包括sentence、paragraph代表分词器创建成功，否则无法进行同段同句搜索。

### 2.3.3.同段查询

得到分词后，就能进行同段查询。DSL如下：

```java
 <!--同段搜索 html和text为同一个查询-->
    <property name="testParagraphQuery">
        <![CDATA[{
            "query": {
                "span_not": {
                  "include": {
                    "span_near": {
                      "clauses": [
                        {
                          "span_term": {
                            "mainContent": {
                              "value": #[spanTermValue1]
                            }
                          }
                        },
                        {
                          "span_term": {
                            "mainContent": {
                              "value": #[spanTermValue2]
                            }
                          }
                        }
                      ],
                      "slop": #[slop],
                      "in_order": false
                    }
                  },
                  "exclude": {
                    "span_term": {
                      "mainContent": {
                        "value": #[queryType]
                      }
                    }
                  }
                }
            }
        }]]>
    </property>
```

Bboss执行上面的DSL:

```java
/**
	 * 测试html同段搜索
	 */
	@Test
	public void testHtmlParagraphQuery() {
		try {
            //创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");//bboss读取xml
			//封装请求参数
			Map<String, String> queryParams = new HashMap<>(5);
			queryParams.put("spanTermValue1", "java");
			queryParams.put("spanTermValue2", "javascript");
			queryParams.put("slop", "3");
			queryParams.put("queryType", "paragraph");

			//Bboss执行查询DSL
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("sample1" + "/_search?search_type=dfs_query_then_fetch",
					"testParagraphQuery",//DSL模板ID
					queryParams,//查询参数
					MetaMap.class);//文档信息
			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{}", metaMap);
			});
		} catch (ElasticSearchException e) {
			logger.error("testParagraphQuery 执行失败",e);
		}
	}
```

返回文档内容如下：

```java
文档_source:{mainContent=<p>java python javascript</p><p>oracle mysql sqlserver</p>}
```

如果将上述spanTermValue2属性的值改为oracle，或者其他不在同段的两个词组，将不会返回文档。可以结合测试实例自行测试。

## 2.4.纯文本数据同段搜索

#### 2.4.1.案例准备工作

开始之前要现在工作中创建Bboss的DSL配置文件，位置在resources/esmapper/paragraph_words.xml（Git地址：[https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/paragraph_words.xml](https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/field_collapsing.xml）。)）。本文涉及到的DSL都会放到该配置文件。

#### 2.4.2创建索引

在配置文件中添加创建索引的mapping，名称为createSample2Indice。在该索引的mapping中指定了一个自定义文本分词器。

##### 分词器预期效果

simple1_mainContent分词器的期望是：替换p , h1 , h2标签为统一的分段符：paragraph；

替换中英文 ！ , ？ , 。 标点符号为统一的分页符：sentence。每个词元想个距离是1，用span_term可以判断两个词元是否存在paragraph（同段）、sentence（同句）。

注意：分词器的script要用@进行转义，否则url校验不通过

DSL如下：

```java
<!--创建text分词索引-->
    <property name="createSample2Indice">
        <![CDATA[{
            "settings": {
                "number_of_replicas": 0,
                "number_of_shards": 1,
                "analysis": {
                  "analyzer": {
                    "simple2_mainContent": {
                      "type": "custom",
                      "char_filter": [
                        "sentence_paragrah_mapping"
                      ],
                      "tokenizer": "ik_max_word"
                    }
                  },
                  "char_filter": {
                    "sentence_paragrah_mapping": {
                      "type": "mapping",
                      "mappings": [
                        ## script要用@进行转义，否则url校验不通过
                        @"""\n => \u0020sentence\u0020paragraph\u0020 """,
                        @"""! => \u0020sentence\u0020 """,
                        @"""? => \u0020sentence\u0020 """,
                        @"""。=> \u0020sentence\u0020 """,
                        @"""？=> \u0020sentence\u0020 """,
                        @"""！=> \u0020sentence\u0020"""
                      ]
                    }
                  }
                }
                },
                "mappings": {
                "properties": {
                  "mainContent": {
                    "type": "text",
                    "analyzer": "simple2_mainContent",
                    "search_analyzer": "ik_smart"
                  }
                }
            }
        }]]>
    </property>
```

Bboss执行上面的DSL:

```java
	/**
	 * 创建simple2索引
	 */
	@Test
	public void dropAndCreateSample2Indice() {
		try {
            //创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");//bboss读取xml
			/*检查索引是否存在，存在就删除重建*/
			if (clientInterface.existIndice("sample2")) {
				clientInterface.dropIndice("sample2");
			}
			/*传参，创建索引*/
			clientInterface.createIndiceMapping("sample2", "createSample2Indice");
			logger.info("创建索引 sample2 成功");
		} catch (ElasticSearchException e) {
			logger.error("创建索引 sample2 失败", e);
		}
	}
```

执行成功，可在ES中查询到对应的索引和索引的分词器。

#### 2.4.3.添加数据

将准备的JSON数据写入到配置文件，Bboss可以将JSON数据插入到ES。数据DSL如下：

```java
 <!--添加索引数据-->
    <property name="bulkSample2Data">
        <![CDATA[
            {"index" : {"_index" : "sample2" }}
            {"mainContent":"java python javascript\noracle mysql sqlserver"}
        ]]>
    </property>
```

Bboss执行上面的DSL:

```java
/**
	 * 添加simple2数据
	 */
	@Test
	public void insertSimple2IndiceData() {
		try {
            //创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");//bboss读取xml
			ClientInterface restClient = ElasticSearchHelper.getRestClientUtil();//插入数据用RestClient
			ESInfo esInfo = clientInterface.getESInfo("bulkSample2Data");//获取插入数据
			StringBuilder recipedata = new StringBuilder();
			recipedata.append(esInfo.getTemplate().trim())
					.append("\n");
			restClient.executeHttp("sample2" + "/_bulk?refresh", recipedata.toString(), ClientUtil.HTTP_POST);
		} catch (ElasticSearchException e) {
			logger.error("sample2 插入数据失败，请检查错误日志",e);
		}
		long recipeCount = clientInterface.countAll("sample2");
		logger.info("sample2 当前条数：{}", recipeCount);
	}
```

执行后，就能在索引中看到对应的数据。

#### 2.4.4.测试分词

为了保证同段搜索的效果，先测试下上述定义的分词器效果。如果出现sentence，paragraph词元，证明分词成功，可以进行同段搜索。DSL如下：

```java
 <!--测试Text分词器-->
    <property name="testTextAnalyze">
        <![CDATA[{
          "text": ["ava python javascript\noracle mysql sqlserver"],
          "analyzer": "simple2_mainContent"
        }]]>
    </property>
```

Bboss执行上面的DSL:

```java
/**
	 * 测试分词
	 */
	@Test
	public void testTextAnalyze() {
		try {
            //创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");//bboss读取xml
			String analyzeResult = clientInterface.executeHttp("sample1" + "/_analyze", "testTextAnalyze", ClientUtil.HTTP_POST);
			//分词结果
			logger.info("分词结果:{}", analyzeResult);
		} catch (ElasticSearchException e) {
			logger.error("testTextAnalyze 执行失败", e);
		}
	}
```

如果词元中包括sentence、paragraph代表分词器创建成功，否则无法进行同段同句搜索。

#### 2.4.5.同段查询

得到分词后，就能进行同段查询。查询DSL仍为testParagraphQuery。

```java
/**
	 * 测试text同段搜索
	 */
	@Test
	public void testTextParagraphQuery() {
		try {
            //创建一个加载配置文件esmapper/sentence_paragrah.xml的rest client接口实例
			clientInterface = bbossESStarter.getConfigRestClient("esmapper/sentence_paragrah.xml");
			//封装请求参数
			Map<String, String> queryParams = new HashMap<>(5);
			queryParams.put("spanTermValue1", "java");
			queryParams.put("spanTermValue2", "javascript");
			queryParams.put("slop", "3");
			queryParams.put("queryType", "paragraph");

			//Bboss执行查询DSL
			ESDatas<MetaMap> metaMapESDatas = clientInterface.searchList("sample2" + "/_search?search_type=dfs_query_then_fetch",
					"testParagraphQuery",//DSL模板ID
					queryParams,//查询参数
					MetaMap.class);//文档信息
			//ES返回结果遍历
			metaMapESDatas.getDatas().forEach(metaMap -> {
				logger.info("\n文档_source:{}", metaMap);
			});
		} catch (ElasticSearchException e) {
			logger.error("testParagraphQuery 执行失败", e);
		}
	}
```

返回文档内容如下：文档有换行符，两句话会换行输出。

```java
文档_source:{mainContent=java python javascript
oracle mysql sqlserver}
```

如果将上述spanTermValue2属性的值改为oracle，或者其他不在同段的两个词组，将不会返回文档。可以结合测试实例自行测试。

# 3.相关资料

SpanQuery官方文档

https://www.elastic.co/guide/en/elasticsearch/client/net-api/current/span-queries.html

同段搜索相关文档

https://mp.weixin.qq.com/s/BnRzz3fXeA42P2t7fCBmOA

# 4.开发交流

bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">

