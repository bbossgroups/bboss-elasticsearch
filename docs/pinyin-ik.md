# 中文拼音混合检索案例

# 1.准备工作

从ik和拼音官方github地址下载并安装ik和拼音插件

ik:
https://github.com/medcl/elasticsearch-analysis-ik

pinyin:
https://github.com/medcl/elasticsearch-analysis-pinyin



# 2.定义拼音和ik分词机制的索引mapping

## 创建定义mapping 结构的dsl配置文件

创建xml文件-esmapper/estrace/pinyin.xml，定义名称为createDemoIndice的dsl配置：

```xml
<property name="createDemoIndice">
    <![CDATA[{
        "settings": {
            "number_of_shards": 6,
            "index.refresh_interval": "5s",
            "analysis" : {
                "analyzer" : {
                    "pinyin_analyzer" : {
                        "tokenizer" : "my_pinyin"
                        }
                },
                "tokenizer" : {
                    "my_pinyin" : {
                        "type" : "pinyin",
                        "keep_separate_first_letter" : false,
                        "keep_full_pinyin" : true,
                        "keep_original" : true,
                        "limit_first_letter_length" : 16,
                        "lowercase" : true,
                        "remove_duplicated_term" : true
                    }
                }
            }
        },
        "mappings": {
            "demo": {
                "properties": {
                    "contentbody": {
                        "type": "text",
                        "term_vector": "with_positions_offsets",
                        "analyzer": "ik_max_word",
                        "search_analyzer": "ik_max_word",
                        "fields": {
                            "keyword": {
                                "type": "keyword"

                            }
                        }
                    },
                    "agentStarttime": {
                        "type": "date",
                        "format":"yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                    },
                    "applicationName": {
                        "type": "text",
                        "fields": {
                            "keyword": {
                                "type": "keyword"
                            }
                        }
                    },
                    "name": {
                        "type": "text",
                        "fields": {
                            "pinyin": { ## 定义内置pinyin字段，采用拼音分词器
                                "type": "text",
                                "store": false,
                                "term_vector": "with_offsets",
                                "analyzer": "pinyin_analyzer",
                                "boost": 10
                            }
                        }
                    }
                }
            }
        }
    }]]>
</property>
```

## 加载配置文件并创建demo索引结构

```java
public void testCreateDemoMapping(){

      ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/pinyin.xml");
      try {
         //可以先删除索引mapping，重新初始化数据
         clientUtil.dropIndice("demo");
      } catch (ElasticSearchException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }


//
      //创建索引表结构
      String response = clientUtil.createIndiceMapping("demo","createDemoIndice");
//       获取并打印创建的索引表结构
      System.out.println(clientUtil.getIndice("demo"));

   }
```

# 3.添加测试数据

```java
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
List<Demo> demos = new ArrayList<>();
Demo demo = new Demo();
demo.setDemoId(2l);
demo.setAgentStarttime(new Date());
demo.setApplicationName("blackcatdemo2");
demo.setContentbody("this is content body2");
demo.setName("刘德华");
demos.add(demo);

demo = new Demo();
demo.setDemoId(3l);
demo.setAgentStarttime(new Date());
demo.setApplicationName("blackcatdemo3");
demo.setContentbody("四大天王，这种文化很好，中华人民共和国");
demo.setName("张学友");
demos.add(demo);

//批量添加文档
String response = clientUtil.addDocuments("demo",//索引表
      "demo",//索引类型
      demos);

System.out.println("addDocuments-------------------------");
System.out.println(response);

//验证创建的两条索引记录
response = clientUtil.getDocument("demo",//索引表
      "demo",//索引类型
      "2");
System.out.println("getDocument-------------------------");
System.out.println(response);

demo = clientUtil.getDocument("demo",//索引表
      "demo",//索引类型
      "3",//文档id
      Demo.class);
```

# 4.拼音检索

## 定义拼音检索dsl

在之前定义的pinyin.xml文件中新增dsl配置-searchPinyinDemo

```xml
<property name="searchPinyinDemo"><![CDATA[{
    "size": 100,
    "query": {
        "bool": {
            "must": [
                {
                      "match_phrase_prefix" : {
                            "name.pinyin" : {
                                "query" : #[name],
                                "max_expansions" : 10
                            }
                        }

                }
            ]
        }
    }
}]]></property>
```

## 执行拼音检索操作

```java
@Test
public void searchPinyinDemo(){
   ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/estrace/pinyin.xml");
   Map<String,String> params = new HashMap<String,String>();
   params.put("name","zhang学友");//设置中文拼音混合检索条件
      ESDatas<Map> esDatas = clientUtil.searchList("demo/_search","searchPinyinDemo",params,Map.class);
      List<Map> datas = esDatas.getDatas();
      long totalSize = esDatas.getTotalSize();
}
```

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>

<img src="images/alipay.png"  height="200" width="200">

