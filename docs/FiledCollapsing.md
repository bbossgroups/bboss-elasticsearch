# 通过Field Collapsing优化elasticsearch搜索结果(字段折叠)

字段折叠就是按特定字段进行合并去重,比如我们有一个菜谱搜索，我希望按菜谱的“菜系”字段进行折叠，即返回结果每个菜系都返回一个结果，也就是按菜系去重，我搜索关键字“鱼”，要去返回的结果里面各种菜系都有，有湘菜，有粤菜等，别全是湘菜，通过按特定字段折叠之后，来丰富搜索结果的多样性。

本文涉及到的程序和配置文件对应的完整可运行的java工程源码地址：

https://github.com/rookie-ygl/bboss-wiki

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

本文以一个菜谱检索作为案例来介绍field_collapsing的具体用法。在开始之前先建立bboss的dsl xml配置文件：resources/esmapper/field_collapsing.xml本文中涉及的dsl配置都会加到这个配置文件里面。

### 2.1.1创建菜谱索引

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
/**
* 创建菜谱索引
*/
@Test
public void dropAndRecipesIndice() {
        String ecipesPoIndiceName = "recipes";
        ClientInterface clientInterface = ElasticSearchHelper.getConfigRestClientUtil("esmapper/field_collapsing.xml");
        if (clientInterface.existIndice(ecipesPoIndiceName)) {
            clientInterface.dropIndice(ecipesPoIndiceName);
        }
        clientInterface.createIndiceMapping(ecipesPoIndiceName, "createRecipesIndice");
}
```

具体数据可参看上述mapping自行添加

### 2.1.2添加菜品数据



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
public void testQueryByField(){
        ClientInterface clientUtil = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
        Map<String,Object> queryMap = new HashMap<>();
        // 指定商品类目作为过滤器
        queryMap.put("name","鱼");

        // 设置分页
        queryMap.put("from",0);
        queryMap.put("size",10);

        // testFieldValueFactor 就是上文定义的dsl模板名，queryMap 为查询条件，Item为实体类
        ESDatas<Item> esDatast = clientUtil.searchList("recipes/_search?search_type=dfs_query_then_fetch", "testQueryByField", queryMap, Item.class);
        List<Item> esCrmOrderStudentList = esDatast.getDatas();
        logger.debug(esCrmOrderStudentList.toString());
        System.out.println(esCrmOrderStudentList.toString());
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
public void testSortField() {
    ClientInterface clientUtil = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
    Map<String, Object> queryMap = new HashMap<>();
    // 指定商品类目作为过滤器
    queryMap.put("name", "鱼");
    queryMap.put("sortField", "rating");
    // 设置分页
    queryMap.put("from", 0);
    queryMap.put("size", 3);

    // testFieldValueFactor 就是上文定义的dsl模板名，queryMap 为查询条件，Item为实体类
    ESDatas<RecipesPo> esDatast = clientUtil.searchList("recipes/_search?search_type=dfs_query_then_fetch", "testQueryByField", queryMap, RecipesPo.class);
    List<RecipesPo> esCrmOrderStudentList = esDatast.getDatas();
    logger.debug(esCrmOrderStudentList.toString());
    System.out.println(esCrmOrderStudentList.toString());
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
public void testQueryAllType() {
    ClientInterface clientUtil = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
    Map<String, Object> queryMap = new HashMap<>();
    // 指定商品类目作为过滤器
    queryMap.put("name", "鱼");

    // 设置分页
    queryMap.put("from", 0);
    queryMap.put("size", 100);

    // testFieldValueFactor 就是上文定义的dsl模板名，queryMap 为查询条件，Item为实体类
    ESDatas<RecipesPo> esDatast = clientUtil.searchList("recipes/_search?search_type=dfs_query_then_fetch", "testQueryAllType", queryMap, RecipesPo.class);
    List<RecipesPo> esCrmOrderStudentList = esDatast.getDatas();
    logger.debug(esCrmOrderStudentList.toString());
	System.out.println(esCrmOrderStudentList.toString());
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
<property name="testQueryAllType">
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
        ClientInterface clientUtil = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
        Map<String, Object> queryMap = new HashMap<>();
        // 指定商品类目作为过滤器
        queryMap.put("recipeName", "鱼");
        queryMap.put("sortField", "rating");
        queryMap.put("collapseField", "type");
        // 设置分页
        queryMap.put("from", 0);
        queryMap.put("size", 3);

        // testFieldValueFactor 就是上文定义的dsl模板名，queryMap 为查询条件，Item为实体类
        ESDatas<RecipesPo> esDatast = clientUtil.searchList("recipes/_search?search_type=dfs_query_then_fetch", "testQueryAllType", queryMap, RecipesPo.class);
        List<RecipesPo> esCrmOrderStudentList = esDatast.getDatas();
        logger.debug(esCrmOrderStudentList.toString());
        System.out.println(esCrmOrderStudentList.toString());
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
        ClientInterface clientInterface = bbossESStarter.getConfigRestClient("esmapper/field_collapsing.xml");
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

        ESInnerHitSerialThreadLocal.setESInnerTypeReferences(RecipesPo.class);
        ESDatas<RecipesPo> esDatast = clientInterface.searchList("recipes/_search?search_type=dfs_query_then_fetch", "testFieldCollapsingInnerHits", queryMap, RecipesPo.class);
        List<RecipesPo> recipesPoList = esDatast.getDatas();
        recipesPoList.forEach(recipesPo -> {
            List innerHitsRecipesPoList = ResultUtil.getInnerHits(recipesPo.getInnerHitsRecipesPo(), collapseInnerHitsName);
            if (innerHitsRecipesPoList != null && innerHitsRecipesPoList.size() > 0) {
                innerHitsRecipesPoList.forEach(innerHitsRecipesPo -> {
                    System.out.println(innerHitsRecipesPo.toString());
                });
            }
        });

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

