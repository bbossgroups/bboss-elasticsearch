# Elasticsearch 7 API介绍

本文介绍内容

1. bboss中对es7.0及以上版本的支持API

2. bboss中基于es 7.0的案例：

   - 增删改查案例

   - 索引mapping管理案例

   - 索引模板管理案例
   - script使用案例
   - 父子jointype关系维护和关联检索案例



# Elasticsearch 7.0.0新特性

Elasticsearch 7.0.0与之前版本的差别，衔接ES 6和ES 8的中间版本，ES 7默认关闭indexType的支持，这样索引Index将不允许定义和使用indexType，ES 8完全去掉indexType的支持。

bboss作为一款高性能的Elasticsearch Java Rest客户端，几乎支持Elasticsearch的所有版本（1.x,2.x,4.x,5.x,6.x,7.x,8.x.....）。如果需要向下兼容ES 6的type类型，使得带indexType的索引mapping和索引Template能够在ES 7上面创建成功，bboss 提供了额外的控制属性来开启elasticsearch 7.x的type特性：

```properties
## 设置为true，兼容ES 6的indexType
## 设置为false（默认值），不能再index mapping和index Template中包含indexType
elasticsearch.includeTypeName = true
## spring boot中对应的配置项为
# spring.elasticsearch.bboss.elasticsearch.includeTypeName = true
```

在需要的时候开启，不需要的时候关闭,默认关闭。除了通过这个属性进行控制，同时也可以在请求参数上面指定参数进行控制：

https://www.elastic.co/guide/en/elasticsearch/reference/7.0/removal-of-types.html

```json
PUT index?include_type_name=false
{
  "mappings": {
    "properties": { 
      "foo": {
        "type": "keyword"
      }
    }
  }
}
```

```js
PUT index-1-01?include_type_name=true
{
  "mappings": {
    "type": {
      "properties": {
        "bar": {
          "type": "long"
        }
      }
    }
  }
}

PUT index-2-01
{
  "mappings": {
    "properties": {
      "bar": {
        "type": "long"
      }
    }
  }
}
```

Elasticsearch 7.0.0下载地址

https://www.elastic.co/cn/downloads/elasticsearch



# ES 7+ API清单

在bboss 的ClientInterface接口中新增了以下方法，不带索引type参数，以便提供对Elasticsearch 7和Elasticsearch 8以上版本的支持：

```java
/**
 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public String addDocumentWithParentId(String indexName,   Object bean, Object parentId) throws ElasticSearchException;

public String addDocumentWithParentId(String indexName,   Object bean, Object parentId, String refreshOption) throws ElasticSearchException;

public String addDateDocumentWithParentId(String indexName, Object bean, Object parentId) throws ElasticSearchException;

public String addDateDocumentWithParentId(String indexName,   Object bean, Object parentId, String refreshOption) throws ElasticSearchException;


/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_update/1
 * @param id
 * @param params
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,  Object id, Map params) throws ElasticSearchException;

/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *            test/_update/1
 * @param id
 * @param params
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index , Object id, Object params) throws ElasticSearchException;


/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_update/1
 * @param id
 * @param params
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 *
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,  Object id, Map params, String refreshOption) throws ElasticSearchException;

/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_doc/1/_update
 * @param id
 * @param params
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 *
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,  Object id, Object params, String refreshOption) throws ElasticSearchException;

/**
 *
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param ids
 * @return
 * @throws ElasticSearchException
 */
public String deleteDocumentsNew(String indexName,  String... ids) throws ElasticSearchException;
/**
 *
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.

 * @param ids
 * @return
 * @throws ElasticSearchException
 */
public abstract String deleteDocumentsWithrefreshOptionNew(String indexName,   String refreshOption, String... ids) throws ElasticSearchException;


/**
 * 获取索引表
 * For Elasticsearch 7 and 7+
 * @param index
 * @return
 * @throws ElasticSearchException
 */
public List<IndexField> getIndexMappingFields(String index ) throws ElasticSearchException;

/***************************读取模板文件添加或者修改文档开始************************************/
/**
 * 批量创建索引
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param addTemplate
 * @param beans
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocumentsNew(String indexName,   String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException;
public abstract String addDocumentsNew(String indexName,  String addTemplate, List<?> beans) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param addTemplate
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocumentNew(String indexName,  String addTemplate, Object bean) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param addTemplate
 * @param bean
 * @param refreshOption
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocumentNew(String indexName,   String addTemplate, Object bean, String refreshOption) throws ElasticSearchException;

public abstract String updateDocumentsNew(String indexName,   String updateTemplate, List<?> beans) throws ElasticSearchException;

/**
 *
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param updateTemplate
 * @param beans
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String updateDocumentsNew(String indexName , String updateTemplate, List<?> beans, String refreshOption) throws ElasticSearchException;

/***************************读取模板文件添加或者修改文档结束************************************/

/***************************添加或者修改文档开始************************************/
/**
 * 批量创建索引
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param beans
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocuments (String indexName,  List<?> beans, String refreshOption) throws ElasticSearchException;
public abstract String addDocuments (String indexName,   List<?> beans) throws ElasticSearchException;


/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocument (String indexName,  Object bean) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocument (String indexName,  Object bean, ClientOptions clientOptions) throws ElasticSearchException;
/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocument (String indexName,  Object bean, ClientOptions clientOptions) throws ElasticSearchException;
/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addMapDocument (String indexName,  Map bean, ClientOptions clientOptions) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateMapDocument (String indexName,  Map bean) throws ElasticSearchException;
/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addMapDocument (String indexName , Map bean) throws ElasticSearchException;
/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateMapDocument (String indexName,   Map bean, ClientOptions clientOptions) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocument (String indexName, Object bean, String refreshOption) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @param docId
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocumentWithId (String indexName, Object bean, Object docId) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @param docId
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocumentWithId (String indexName,  Object bean, Object docId, Object parentId) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @param docId
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocument (String indexName, Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException;

/**
 * 创建或者更新索引文档
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param bean
 * @param docId
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocument (String indexName,  Object bean, Object docId, String refreshOption) throws ElasticSearchException;

/**
 *
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param beans
 * @param clientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
 * @return
 * @throws ElasticSearchException
 */
public abstract String updateDocuments(String indexName,   List<?> beans, ClientOptions clientOptions) throws ElasticSearchException;
public abstract String updateDocuments(String indexName, List<?> beans) throws ElasticSearchException;
public abstract String updateDocumentsWithIdKey(String indexName,  List<Map> beans, String docIdKey) throws ElasticSearchException;
public abstract String updateDocumentsWithIdKey(String indexName,   List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException;
/**
 *
 * @param indexName
 * @param beans
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String updateDocuments(String indexName, List<?> beans, String refreshOption) throws ElasticSearchException;
public abstract String updateDocuments(String indexName,   List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException;
public abstract String updateDocuments(String indexName,  List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException;

/***************************添加或者修改文档结束************************************/
/**
 * 获取json格式文档
 * @param indexName
 * @param documentId
 * @return
 * @throws ElasticSearchException
 */
public abstract String getDocument(String indexName,  String documentId) throws ElasticSearchException;
/**
 * 获取json格式文档，通过options设置获取文档的参数
 * @param indexName
 * @param documentId
 * @param options
 * @return
 * @throws ElasticSearchException
 */
public abstract String getDocument(String indexName,   String documentId, Map<String, Object> options) throws ElasticSearchException;






/**
 * 获取文档,返回类型可以继承ESBaseData(这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
 * @param indexName
 * @param documentId
 * @return
 * @throws ElasticSearchException
 */
public abstract <T> T getDocument(String indexName, String documentId, Class<T> beanType) throws ElasticSearchException;

/**
 * 获取文档，通过options设置获取文档的参数，返回类型可以继承ESBaseData(这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
 * @param indexName
 * @param documentId
 * @param options
 * @return
 * @throws ElasticSearchException
 */
public abstract <T> T getDocument(String indexName,  String documentId, Map<String, Object> options, Class<T> beanType) throws ElasticSearchException;



/**
 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
 * @param indexName
 * @param documentId
 * @param options
 * @return
 * @throws ElasticSearchException
 */
public abstract MapSearchHit getDocumentHit(String indexName,   String documentId, Map<String, Object> options) throws ElasticSearchException;

/**
 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
 * @param indexName
 * @param documentId
 * @return
 * @throws ElasticSearchException
 */
public abstract MapSearchHit getDocumentHit(String indexName,  String documentId) throws ElasticSearchException;

/**************************************创建或者修改文档开始**************************************************************/
/**
 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocument(String indexName,   Object bean) throws ElasticSearchException;

/**
 *
 * @param indexName
 * @param bean
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocument(String indexName,  Object bean, String refreshOption) throws ElasticSearchException;

/**
 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentWithId(String indexName,  Object bean, Object docId) throws ElasticSearchException;

/**
 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
 * @param indexName
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentWithId(String indexName,   Object bean, Object docId, Object parentId) throws ElasticSearchException;

/**
 *
 * @param indexName
 * @param bean
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocument(String indexName,  Object bean, Object docId, String refreshOption) throws ElasticSearchException;

/**
 *
 * @param indexName
 * @param bean
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocument(String indexName,  Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException;

/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocuments(String indexName,   List<?> beans) throws ElasticSearchException;



/**
 *
 * @param indexName
 * @param beans
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocuments(String indexName,  List<?> beans, String refreshOption) throws ElasticSearchException;

/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdKey map中作为文档id的Key
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocuments(String indexName,   List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException;
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdKey map中作为文档id的Key
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsWithIdKey(String indexName,  List<Map> beans, String docIdKey) throws ElasticSearchException;
public abstract String addDocuments(String indexName,  List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException;
public abstract String addDocumentsWithIdKey(String indexName,  List<Map> beans, String docIdKey) throws ElasticSearchException;


/**********************/
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdKey map中作为文档id的Key
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocuments(String indexName,   List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException;
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdKey map中作为文档id的Key
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsWithIdKey(String indexName,   List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException;
public abstract String addDocuments(String indexName,  List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException;
public abstract String addDocumentsWithIdKey(String indexName,  List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException;

/**
 * 指定对象集合的文档id字段
 */
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdField 对象中作为文档id的Field
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsWithIdOptions(String indexName,   List<Object> beans, String docIdField, String refreshOption) throws ElasticSearchException;
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdField 对象中作为文档id的Key
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsWithIdField(String indexName, List<Object> beans, String docIdField) throws ElasticSearchException;
public abstract String addDocumentsWithIdField(String indexName,  List<Object> beans, String docIdField, String refreshOption) throws ElasticSearchException;
public abstract String addDocumentsWithIdField(String indexName,   List<Object> beans, String docIdField) throws ElasticSearchException;


/**********************/
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdField 对象中作为文档id的Key
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsWithIdField(String indexName,   List<Object> beans, String docIdField, String parentIdField, String refreshOption) throws ElasticSearchException;
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * @param indexName
 * @param beans
 * @param docIdField 对象中作为文档id的Key
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsWithIdField(String indexName , List<Object> beans, String docIdField, String parentIdField) throws ElasticSearchException;
public abstract String addDocumentsWithIdField(String indexName , List<Object> beans, String docIdField, String parentIdField, String refreshOption) throws ElasticSearchException;
public abstract String addDocumentsWithIdParentField(String indexName , List<Object> beans, String docIdField, String parentIdField) throws ElasticSearchException;

/**
 *
 * @param indexName
 * @param beans
 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocuments(String indexName,   List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException;

/**
 *
 * @param indexName
 * @param beans
 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDocuments(String indexName, List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException;
/**************************************创建或者修改文档结束**************************************************************/


/**************************************基于query dsl配置文件脚本创建或者修改文档开始**************************************************************/
/**
 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
 * @param indexName
 * @param addTemplate
 * @param bean
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentNew(String indexName,   String addTemplate, Object bean) throws ElasticSearchException;

/**
 *
 * @param indexName
 * @param addTemplate
 * @param bean
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentNew(String indexName, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException;
/**
 * 批量创建索引,根据时间格式建立新的索引表
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param addTemplate
 * @param beans
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsNew(String indexName,   String addTemplate, List<?> beans) throws ElasticSearchException;

/**
 * For Elasticsearch 7 and 7+
 *
 * @param indexName
 * @param addTemplate
 * @param beans
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String addDateDocumentsNew(String indexName,   String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException;

/**************************************基于query dsl配置文件脚本创建或者修改文档结束**************************************************************/
/**
 *
 * For Elasticsearch 7 and 7+
 * @param indexName
 * @param id
 * @return
 * @throws ElasticSearchException
 */
public abstract String deleteDocumentNew(String indexName,  String id) throws ElasticSearchException;

/**
 * For Elasticsearch 7 and 7+
 *
 * @param indexName
 * @param id
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 * @return
 * @throws ElasticSearchException
 */
public abstract String deleteDocumentNew(String indexName,   String id, String refreshOption) throws ElasticSearchException;




/**
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
 * @param index _mget
 *             test/_mget
 *             test/type/_mget
 *             test/type/_mget?stored_fields=field1,field2
 *             _mget?routing=key1
 * @param type
 * @param ids
 * @param <T>
 * @return
 * @throws ElasticSearchException
 */
public <T> List<T> mgetDocuments(String index, Class<T> type, Object... ids)  throws ElasticSearchException;
/**
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
 * @param index _mget
 *             test/_mget
 *             test/type/_mget
 *             test/type/_mget?stored_fields=field1,field2
 *             _mget?routing=key1
 * @param ids
 * @return
 * @throws ElasticSearchException
 */
public String mgetDocumentsNew(String index,  Object... ids)  throws ElasticSearchException;



/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_doc/1/_update
 * @param id
 * @param params
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,  Object id, Object params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;

/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_doc/1/_update
 * @param id
 * @param params
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,  Object id, Map params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;


/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_doc/1/_update
 * @param id
 * @param params
 * @param refreshOption
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 *
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,   Object id, Map params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;

/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_doc/1/_update
 * @param id
 * @param params
 * @param refreshOption
 * @param detect_noop default null
 * @param doc_as_upsert default null
 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 *
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,   Object id, Object params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;


/**
 * 根据路径更新文档
 * For Elasticsearch 7 and 7+
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
 * @param index test/_doc/1
 *             test/_doc/1/_update
 * @param params
 * @param updateOptions 指定更新的相关参数

 *    refresh=wait_for
 *    refresh=false
 *    refresh=true
 *    refresh
 *    Empty string or true
Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
wait_for
Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
false (the default)
Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
 *
 * @return
 * @throws ElasticSearchException
 */
public String updateDocument(String index,  Object params, UpdateOptions updateOptions) throws ElasticSearchException;

/**
* 判断es是否是ES 7或者高于es 7的版本
*/
public boolean isVersionUpper7();
```

# ES 7 API案例

下面的案例对应的maven工程：

https://github.com/bbossgroups/elasticsearch-example/



## ES 7增删改查、索引mapping、索引模板管理使用案例

java

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/crud/DocumentCRUD7Test.java

对应的dsl：

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/demo7.xml

spring boot

https://github.com/bbossgroups/elasticsearch-springboot-example/blob/master/src/main/java/org/bboss/elasticsearchtest/springboot/crud/DocumentCRUD7.java

https://github.com/bbossgroups/elasticsearch-springboot-example/blob/master/src/main/resources/esmapper/demo7.xml

## ES 7父子关系jointype使用案例

java

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/jointype/JoinTypeTest7.java

对应的dsl

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/joinparentchild7.xml

## ES 7script脚本使用案例

Java

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/test/java/org/bboss/elasticsearchtest/script/ScriptImpl7Test.java

对应的dsl

https://github.com/bbossgroups/elasticsearch-example/blob/master/src/main/resources/esmapper/demo7.xml

# 从源码构建Elasticsearch BBoss

First Get source code from https://github.com/bbossgroups/bboss-elasticsearch

Then change to cmd window under directory bboss-elasticsearch and run gradle build command：

```
gradle publishToMavenLocal
```

Gradle environmenet install and config document: https://esdoc.bbossgroups.com/#/bboss-build

# 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



