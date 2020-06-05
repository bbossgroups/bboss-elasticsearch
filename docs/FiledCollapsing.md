# 通过Field Collapsing优化elasticsearch搜索结果(字段折叠)

字段折叠就是按特定字段进行合并去重,比如我们有一个菜谱搜索，我希望按菜谱的“菜系”字段进行折叠，即返回结果每个菜系都返回一个结果，也就是按菜系去重，我搜索关键字“鱼”，要去返回的结果里面各种菜系都有，有湘菜，有粤菜等，别全是湘菜，通过按特定字段折叠之后，来丰富搜索结果的多样性。

本文涉及到的程序和配置文件对应的完整可运行的Java工程源码地址：

https://github.com/rookieygl/bboss-wiki

# 1.field_collapsing介绍

字段折叠实现方式和效果：

1. 折叠+取 inner_hits 分两阶段执行（组合聚合的方式只有一个阶段），所以 top hits 永远是精确的。
2. 字段折叠只在 top hits 层执行，不需要每次都在完整的结果集上对为每个折叠主键计算实际的 doc values 值，只对 top hits 这小部分数据操作就可以，和 term agg 相比要节省很多内存。因为只在 top hits 上进行折叠，所以相比组合聚合的方式，速度要快很多。
3. 折叠 top docs 不需要使用全局序列（global ordinals）来转换 string，相比 agg 这也节省了很多内存。
4. 分页成为可能，和常规搜索一样，具有相同的局限，先获取 from+size 的内容，再合并。
5. search_after 和 scroll 暂未实现，不过具备可行性。
6. 折叠只影响搜索结果，不影响聚合，搜索结果的 total 是所有的命中纪录数，去重的结果数未知（无法计算）。

# 2. field_collapsing使用案例

## 2.1案例准备工作

本文以一个菜谱检索作为案例来介绍field_collapsing的具体用法。

在开始之前先在工程中创建Bboss的DSL配置文件，本文中涉及的配置都会加到里面：[resources/esmapper/field_collapsing.xml](https://github.com/rookieygl/bboss-wiki/blob/master/src/main/resources/esmapper/field_collapsing.xml)

而字段折叠的Java测试类则在[com/bboss/hellword/FieldCollapsing/FieldCollapsingTest](https://github.com/rookieygl/bboss-wiki/blob/master/src/test/java/com/bboss/hellword/FieldCollapsing/FieldCollapsingTest.java)

### 2.1.1创建菜谱索引

在配置文件中添加菜谱索引的mapping定义createRecipesIndice

```java
 <!--
     通过function_score函数计算相关度打分案例
     参考官方文档
  https://www.elastic.co/guide/en/elasticsearch/reference/7.5/search-request-body.html#request-body-search-collapse
    -->
    <!--
    创建商品索引items mappings dsl
    -->
 <property name="createRecipesIndice">
        <![CDATA[{
        "settings" : {
            "number_of_shards" : 1,
            "number_of_replicas" : 1
        },
        "mappings": {
            "properties": {
                "name":{
                    "type": "text"
                },
                "rating":{
                    "type": "float"
                },
                "type":{
                    "type": "keyword"
                }
            }
        }
        }]]>
   </property>
```

执行上面的DSL

```java
 private Logger logger = LoggerFactory.getLogger(FunctionScoreTest.class);//日志

    @Autowired
    private BBossESStarter bbossESStarter;//bboss依赖

    private ClientInterface clientInterface;//bboss dsl工具

    /**
     * 创建recipes索引
     */
    @Test
    public void dropAndRecipesIndice() {
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
            /*检查索引是否存在，存在就删除重建*/
            if (clientInterface.existIndice("recipes")) {
                logger.info("recipes" + "已存在，删除索引");
                clientInterface.dropIndice("recipes");
            }
            clientInterface.createIndiceMapping("recipes", "createRecipesIndice");
            logger.info("创建索引 recipes 成功");
        } catch (ElasticSearchException e) {
            logger.error("创建索引 recipes 执行失败", e);
        }
}
```

### 2.1.2添加菜品数据

添加数据使用es的_bulk接口，将准备的数据写入到配置文件，执行即可。数据DSL如下：

```java
<!--添加菜品数据-->
<property name="bulkImportRecipesData">
        <![CDATA[
            {"index" : {"_index" : "recipes" }}
            {"name":"清蒸鱼头","rating":1,"type":"湘菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"剁椒鱼头","rating":2,"type":"湘菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"红烧鲫鱼","rating":3,"type":"湘菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"鲫鱼汤（辣）","rating":3,"type":"湘菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"鲫鱼汤（微辣）","rating":4,"type":"湘菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"鲫鱼汤（变态辣）","rating":5,"type":"湘菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"广式鲫鱼汤","rating":5,"type":"粤菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"鱼香肉丝","rat2ing":2,"type":"川菜"}
            {"index" : {"_index" : "recipes" }}
            {"name":"奶油鲍鱼汤","rating":2,"type":"西菜"}
        ]]>
</property>
```

执行上面的DSL

```java
/**
     * 添加recipes索引数据
     */
    @Test
    public void insertRecipesData() {
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
            ClientInterface restClient = ElasticSearchHelper.getRestClientUtil();
            ESInfo esInfo = clientInterface.getESInfo("bulkImportRecipesData");
            StringBuilder recipedata = new StringBuilder();
            recipedata.append(esInfo.getTemplate().trim())
                    .append("\n");
            //插入数据
            restClient.executeHttp("recipes/_bulk?refresh", String.valueOf(recipedata), ClientUtil.HTTP_POST);

            //统计当前索引数据
            long recipeCount = clientInterface.countAll("recipes");
            logger.info("recipes 当前条数：{}", recipeCount);
        } catch (ElasticSearchException e) {
            e.printStackTrace();
        }
    }
```

## 数据导入推荐

使用_bulk接口可以快速插入数据，对于大数据插入Bboss封装了bulkProcessor，支持多线程导入数据，性能非常可观。详情请参考

https://esdoc.bbossgroups.com/#/bulkProcessor

##  2.2普通查询

想吃鱼,直接搜索鱼的关键字即可得到关于鱼的菜品。那么这条查询 DSL 可以是这样的：在esmapper/field_collapsing.xml定义一个名称为testQueryByField；指定菜品名为“鱼”即可；DSL如下：

```java
<!--搜索鱼食材-->
<property name="testQueryByField">
        <![CDATA[{
        ##
        "explain": true,
        "query": {
            "match": {
                "name": "鱼"
            }
        },
        "from": #[from],
        "size": #[size]
        }]]>
</property>
```

执行上面的DSL：

```java
  /**
     * 关键词查询
     */
    @Test
    public void testQueryRecipesPoByField() {
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
            Map<String, Object> queryMap = new HashMap<>();
            //查询条件
            queryMap.put("recipeName", "鱼");

            //设置分页
            queryMap.put("from", 0);
            queryMap.put("size", 5);

            //bboss执行查询DSL
            ESDatas<RecipesPo> poESData = clientInterface.searchList("recipes/_search?search_type=dfs_query_then_fetch",
                    "testQueryByField", //DSL id
                    queryMap, //查询条件
                    RecipesPo.class);
            logger.info(String.valueOf(poESData.getDatas()));
        } catch (ElasticSearchException e) {
            logger.error("testQueryByField 执行失败", e);
        }
    }
```

返回的结果如下：

```java
[RecipesPo{name='清蒸鱼头', rating=1.0, type='湘菜'}, 
RecipesPo{name='剁椒鱼头', rating=2.0, type='湘菜'}, 
RecipesPo{name='红烧鲫鱼', rating=3.0, type='湘菜'}]
```

#### 2.2.1.1好评排序

从上述结果来看，菜系过于单一，数据不是很理想，我们就可以借助排序，得到打分比较高的菜品。DSL如下：

```java
<!--搜索鱼食材，打分排序查询-->
<property name="testSortField">
    <![CDATA[{
    ##
    "explain": false,
    "query": {
        "match": {
        	"name": "鱼"
        },
    }
    "sort": [{
        #[sortField]: {
        	"order": "desc"
    	}
    }],
    "from": #[from],
    "size": #[size]
    }]]>
</property>
```

执行上面的DSL：

```java
/**
     * 关键词查询,加入字段排序
     */
    @Test
    public void testSortRecipesPoByField() {
        try {
            clientInterface = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
            Map<String, Object> queryMap = new HashMap<>();
            //查询条件
            queryMap.put("recipeName", "鱼");
            queryMap.put("sortField", "rating");

            //设置分页
            queryMap.put("from", 0);
            queryMap.put("size", 5);

            ESDatas<RecipesPo> esDadaist = clientInterface.searchList("recipes/_search?search_type=dfs_query_then_fetch", "testSortField", queryMap, RecipesPo.class);
            logger.info(String.valueOf(esDadaist.getDatas()));
        } catch (ElasticSearchException e) {
            logger.error("testSortField 执行失败", e);
        }
    }
```

返回的结果如下：

```java
[RecipesPo{name='鲫鱼汤（变态辣）', rating=5.0, type='湘菜'}, 
 RecipesPo{name='广式鲫鱼汤', rating=5.0, type='粤菜'}, 
 RecipesPo{name='鲫鱼汤（微辣）', rating=4.0, type='湘菜'}]
```

### 2.2.2Agg聚合桶查询

加入打分排序，我们能得到一些其他菜系的菜品，但是如果想看看该餐厅所有菜系，就需要Agg聚合查询后，使用top_hits得到所有菜系的评分高的菜品。DSL如下：

```java
<!--搜索所有菜系，返回菜系打分排名第一的菜品-->
<property name="testQueryAllType">
        <![CDATA[{
        ##
        "explain": false,
        "query": {
            "match": {
                "name": "鱼"
            }
        },
        "sort": [{
            "rating": {
                "order": "desc"
            }
        }],
        "aggs": {
            "type": {
                "terms": {
                    "field": "type",
                    "size": 10
                },
                "aggs": {
                    "rated": {
                        "top_hits": {
                            "sort": [{
                                "rating": {"order": "desc"}
                            }],
                            "size": 1
                        }
                    }
                }
            }
        },
        "from": #[from],
        "size": #[size]
        }]]>
</property>
```

执行上面的DSL：

```java
  /**
     * 查询所有菜系打分最高的鱼食材菜品，返回结果按照打分排序
     */
    @Test
    public void testQueryRecipesPoAllType() {
        try {
            clientInterface = ElasticSearchHelper.getConfigRestClientUtil("esmapper/field_collapsing.xml");
            Map<String, Object> queryMap = new HashMap<>();
            //查询条件
            queryMap.put("recipeName", "鱼");
            queryMap.put("sortField", "rating");

            //聚合参数
            String typeAggName = "all_type";
            String typeTopAggName = "recipes_top";
            queryMap.put("typeAggName", typeAggName);
            queryMap.put("typeTopAggName", typeTopAggName);
            queryMap.put("topHitsSortField", "rating");
            queryMap.put("topHitsSzie", 2);

            //设置分页
            queryMap.put("from", 0);
            //不能设置size，会返回多余数据
            queryMap.put("size", 0);

            //通过下面的方法先得到查询的json报文，然后再通过MapRestResponse查询遍历结果
            MapRestResponse restResponse = clientInterface.search("recipes/_search?search_type=dfs_query_then_fetch",
                    "testQueryAllType",
                    queryMap);

            //获取聚合桶,一次聚合只要一个桶,从桶中获取聚合信息和元数据
            AtomicReference<List<Map<String, Object>>> recipesAggs = new AtomicReference<>(restResponse.getAggBuckets(typeAggName, new ESTypeReference<List<Map<String, Object>>>() {
            }));

            //获取失败数和成功数
            Integer doc_count_error_upper_bound = restResponse.getAggAttribute(typeAggName, "doc_count_error_upper_bound", Integer.class);
            Integer sum_other_doc_count = restResponse.getAggAttribute(typeAggName, "sum_other_doc_count", Integer.class);
            System.out.println("doc_count_error_upper_bound:" + doc_count_error_upper_bound);
            System.out.println("sum_other_doc_count:" + sum_other_doc_count);

            //取出元数据
            recipesAggs.get().forEach(typeAggBucketsMap -> {
                //菜系名
                String recipesAggName = (String) typeAggBucketsMap.get("key");
                System.out.println("菜系名recipesAggName: " + recipesAggName);

                //解析json 获取菜品
                Map<String, ?> recipesTypeAggBucketsMap = (Map<String, ?>) typeAggBucketsMap.get(typeTopAggName);
                Map<String, ?> recipesRatedHitsMap = (Map<String, ?>) recipesTypeAggBucketsMap.get("hits");
                List<Map<String, ?>> recipesTophitsList = (List<Map<String, ?>>) recipesRatedHitsMap.get("hits");
                recipesTophitsList.forEach(recipePoMap -> {
                    logger.info(recipePoMap.get("_source").toString());
                });
            });
        } catch (ElasticSearchException e) {
            logger.error("testQueryAllType 执行失败", e);
        }
    }
```

返回的结果如下：

```java
菜系名recipesAggName: 湘菜
RecipesPo{name='鲫鱼汤（变态辣）', rating=5, type='湘菜'}
RecipesPo{name='鲫鱼汤（微辣）', rating=4, type='湘菜'}
菜系名recipesAggName: 川菜
RecipesPo{name='鱼香肉丝', rating=null, type='川菜'}
菜系名recipesAggName: 粤菜
RecipesPo{name='广式鲫鱼汤', rating=5, type='粤菜'}
菜系名recipesAggName: 西菜
RecipesPo{name='奶油鲍鱼汤', rating=2, type='西菜'}
```

现在我们就得到了所有菜系，还能从每种菜系得到几个评分高的菜品，但是DSL明显过长，不易实现；而字段折叠（field_collapsing）就是为了解决这类问题

### 2.2.3字段折叠查询

字段折叠的使用也很简单，属于一个独立的API，配合query等查询API使用即可，DSL如下：

```java
   <!--字段折叠-->
    <property name="testFieldCollapsing">
        <![CDATA[{
            "explain": false,
            "query": {
                "match": {
                    "name": #[recipeName]
                }
            },
            "collapse": {
                "field":#[collapseField]
            },
            "sort": [{
                #[sortField]: {
                    "order": "desc"
                }
            }],
            "from":#[from],
            "size":#[size]
        }]]>
    </property>
```

执行上面的DSL：

```java
/**
* 字段折叠
*/
@Test
public void testFieldCollapsing() {
         clientInterface = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
        Map<String, Object> queryMap = new HashMap<>();
        //查询条件
        queryMap.put("recipeName", "鱼");

        //字段折叠(field_collapsing)参数
        queryMap.put("collapseField", "type");
        queryMap.put("sortField", "rating");
        //设置分页
        queryMap.put("from", 0);
        queryMap.put("size", 10);

        //testFieldValueFactor 就是上文定义的dsl模板名，queryMap 为查询条件，Item为实体类
        ESDatas<RecipesPo> esDatast = clientInterface.searchList("recipes/_search?search_type=dfs_query_then_fetch", "testFieldCollapsing", queryMap, RecipesPo.class);
        List<RecipesPo> esRecipesPoList = esDatast.getDatas();
        logger.debug(esRecipesPoList.toString());
        System.out.println(esRecipesPoList.toString());
}
```

返回的结果如下：

```java
[RecipesPo{name='鲫鱼汤（变态辣）', rating=5, type='湘菜'}, 
 RecipesPo{name='广式鲫鱼汤', rating=5, type='粤菜'}, 
 RecipesPo{name='奶油鲍鱼汤', rating=2, type='西菜'}]
```

从返回结果可以看出这已经和上面聚合搜索的结果很接近了，但是field_collapsing默认一个组只返回一条数据，也就是form和size只能控制返回后的结果，那这样的搜索结果无疑是不够完整的，所以field_collapsing还可以对组内数据的控制，比如inner_hits可以解决每组返回数据的条数。

#### 2.2.3.1控制组内数据（inner_hits）

inner_hits可以对组内的数据再次聚合，指定排序、返回数据条数等，组数据再有collapse合并返回。 DSL如下

```java
<!--字段折叠 控制组内数据-->
<property name="testFieldCollapsingInnerHits">
        <![CDATA[{
            "explain": false,
            "query": {
                "match": {
                    "name": #[recipeName]
                }
            },
            "sort": [{
                #[sortField]: {
                    "order": "desc"
                }
            }],
            "collapse": {
                "field": #[collapseField],
                "inner_hits": {
                    "name": #[typeInnerHitsName],
                    "size": #[typeInnerHitsSize],
                    "sort": [{
                        #[collapseSortField]: "desc"
                    }]
                }
            },
            "from":#[from],
            "size":#[size]
        }]]>
</property>
```

执行上面的DSL：

```java
    /**
     * 字段折叠 控制组内数据
     */
    @Test
    public void testFieldCollapsingInnerHits() {
        clientInterface = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
        Map<String, Object> queryMap = new HashMap<>();
        //查询条件
        queryMap.put("recipeName", "鱼");
        queryMap.put("sortField", "rating");

        //字段折叠(field_collapsing)参数
        queryMap.put("collapseField", "type");
        queryMap.put("innerHitsName", "sort_rated");

        //innerHits参数
        String collapseInnerHitsName = "sort_rated";
        queryMap.put("typeInnerHitsName", collapseInnerHitsName);
        queryMap.put("typeInnerHitsSize", 2);
        queryMap.put("collapseSortField", "rating");

        //设置分页
        queryMap.put("from", 0);
        queryMap.put("size", 10);

        try {
            ESInnerHitSerialThreadLocal.setESInnerTypeReferences(RecipesPo.class);
            ESDatas<RecipesPo> esDatast = clientInterface.searchList("recipes/_search?search_type=dfs_query_then_fetch",
                    "testFieldCollapsingInnerHits",
                    queryMap,
                    RecipesPo.class);
            List<RecipesPo> recipesPoList = esDatast.getDatas();
            recipesPoList.forEach(recipesPo -> {
                List innerHitsRecipesPoList = ResultUtil.getInnerHits(recipesPo.getInnerHitsRecipesPo(), collapseInnerHitsName);
                if (innerHitsRecipesPoList != null && innerHitsRecipesPoList.size() > 0) {
                    innerHitsRecipesPoList.forEach(innerHitsRecipesPo -> {
                        System.out.println(innerHitsRecipesPo.toString());
                    });
                }
            });
        } catch (ElasticSearchException e) {
            logger.error("testFieldCollapsingInnerHits 执行失败", e);
        } finally {
            //清除缓存
            ESInnerHitSerialThreadLocal.clean();
        }
}
```

返回的结果如下：

```java
RecipesPo{name='鲫鱼汤（变态辣）', rating=5, type='湘菜', innerHitsRecipesPo=null}
RecipesPo{name='鲫鱼汤（微辣）', rating=4, type='湘菜', innerHitsRecipesPo=null}
RecipesPo{name='广式鲫鱼汤', rating=5, type='粤菜', innerHitsRecipesPo=null}
RecipesPo{name='奶油鲍鱼汤', rating=2, type='西菜', innerHitsRecipesPo=null}
RecipesPo{name='鱼香肉丝', rating=null, type='川菜', innerHitsRecipesPo=null}
```

这样我们就能得到和聚合评分查询一样的结果，并且不用写那么复杂的DSL。

## 3.相关资料

elasticsearch字段折叠官方文档

https://www.elastic.co/guide/en/elasticsearch/reference/7.5/search-request-body.html#request-body-search-collapse

字段折叠相关文档

https://blog.csdn.net/weixin_41997172/article/details/80484975

bboss聚合查询操作API

https://esdoc.bbossgroups.com/#/agg

# 4.开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">
