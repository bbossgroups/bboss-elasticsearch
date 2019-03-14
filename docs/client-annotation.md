# ElasticSearch客户端注解使用介绍

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

*The best elasticsearch highlevel java rest api-----[bboss](README.md)* 



# 1.ElasticSearch客户端bboss提供了一系列注解

@ESId  用于标识实体对象中作为docid的属性，该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，默认为true-保存，false不保存，字段名称为属性名称;readSet属性：默认false，设置为true时，检索的时候会将文档id设置到被注解的对象属性中。ESId可用于添加和修改文档

@ESParentId 用于标识实体对象中作为parentid的属性，该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，默认为true-保存，false不保存，字段名称为属性名称;readSet属性：默认false，设置为true时，检索的时候会将文档parentid设置到被注解的对象属性中。ESParentId可用于添加和修改文档

@ESVersion 用于标识实体对象中作为文档版本信息的属性，该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，true-保存，默认为false不保存，字段名称为属性名称。ESVersion可用于添加/修改文档操作

@ESVersionType 用于标识实体对象中作为文档版本类型信息的属性，该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，true-保存，默认为false不保存，字段名称为属性名称。ESVersionType可用于添加/修改文档操作

@ESRetryOnConflict 用于标识实体对象中作为文档修改操作版本冲突重试次数信息的属性，数字类型。该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，true-保存，默认为false不保存，字段名称为属性名称。ESRetryOnConflict可用于添加/修改文档操作

@ESRouting  用于标识实体对象中作为文档添加/修改操作路由信息的属性，字符串或者数字类型。该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，true-保存，默认为false不保存，字段名称为属性名称。ESRouting可用于添加/修改文档操作

@ESDocAsUpsert 用于标识实体对象中控制文档修改操作时，文档不存在时是否添加文档的信息的属性，布尔值。该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，true-保存，默认为false不保存，字段名称为属性名称。ESDocAsUpsert可用于添加/修改文档操作

@ESSource 用于标识实体对象中控制文档修改操作时，返回值是否包含文档source数据的控制变量的属性，布尔值。该注解只有一个persistent 布尔值属性，用于控制被本注解标注的字段属性是否作为普通文档属性保存，true-保存，默认为false不保存，字段名称为属性名称。ESSource可用于修改文档操作

@Column 该注解用于指定日期格式，和JsonFormat属性结合一起使用：

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
@Column(dataformat = "yyyy-MM-dd HH:mm:ss.SSS")
```

@JsonIgnore 标注实体属性不作为es字段保存，使用示例：

```java
@JsonIgnore
private Integer sqlEndElapsed;
```



## @JsonProperty注解使用

当elasticsearch索引表字段名称和java bean的字段名称不一致的情况下，采用@JsonProperty注解用来定义elasticsearch和java bean的field名称转换映射关系，使用实例如下：

```java
@JsonProperty("max_score")
private Double maxScore;
```



# 2.注解的使用示例

```java
@ESId(persistent = false,readSet=true)
protected String id;
@ESParentId(persistent = false,readSet=true)
protected String parentId;
@ESVersion
protected int version;
@ESVersionType
protected String versionType;
@ESRetryOnConflict
protected int retryOnConflict;
@ESRouting
protected String routing;
@ESDocAsUpsert
protected boolean docAsUpsert;
@ESSource
protected boolean returnSource;
```



# 3.结合控制注解的批量文档修改操作

```java
    @Test
	public void testOrmBulk(){
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		List<OnlineGoodsInfoUpdateParams> onlineGoodsInfoUpdateParamss = new ArrayList<>();
		OnlineGoodsInfoUpdateParams onlineGoodsInfoUpdateParams = new OnlineGoodsInfoUpdateParams();
		onlineGoodsInfoUpdateParams.setId("aa");
		onlineGoodsInfoUpdateParams.setParentId("ppaa");
		onlineGoodsInfoUpdateParams.setType("tt");
		onlineGoodsInfoUpdateParams.setIndex("ddd");

		onlineGoodsInfoUpdateParams.setGoodsName("dddd");
		onlineGoodsInfoUpdateParams.setDocAsUpsert(true);
		onlineGoodsInfoUpdateParams.setRetryOnConflict(3);
		onlineGoodsInfoUpdateParams.setReturnSource(true);
		onlineGoodsInfoUpdateParams.setRouting("test");
		onlineGoodsInfoUpdateParams.setVersion(1);
		onlineGoodsInfoUpdateParams.setVersionType(ClientInterface.VERSION_TYPE_INTERNAL);

		onlineGoodsInfoUpdateParamss.add(onlineGoodsInfoUpdateParams);

		onlineGoodsInfoUpdateParams = new OnlineGoodsInfoUpdateParams();
		onlineGoodsInfoUpdateParams.setId("aa");
		onlineGoodsInfoUpdateParams.setParentId("ppaa");
		onlineGoodsInfoUpdateParams.setType("tt");
		onlineGoodsInfoUpdateParams.setIndex("ddd");

		onlineGoodsInfoUpdateParams.setGoodsName("dddd");

		/**
		 * 设置更新文档控制变量
		 */
		onlineGoodsInfoUpdateParams.setDocAsUpsert(true);
		onlineGoodsInfoUpdateParams.setRetryOnConflict(3);
		onlineGoodsInfoUpdateParams.setReturnSource(true);
		onlineGoodsInfoUpdateParams.setRouting("test");
		onlineGoodsInfoUpdateParams.setVersion(1);
		onlineGoodsInfoUpdateParams.setVersionType(ClientInterface.VERSION_TYPE_INTERNAL);
		onlineGoodsInfoUpdateParamss.add(onlineGoodsInfoUpdateParams);
		String response = clientUtil.updateDocuments("aa","tt",onlineGoodsInfoUpdateParamss);
		System.out.println(response);
	}
```

 我们可以看到执行updateDocuments方法时，生成的原生bulk报文如下，注意其中的控制参数信息：

```json
{ "update" : { "_index" : "aa", "_type" : "tt", "_id" : "aa", "parent" : "ppaa", "_routing" : "test","retry_on_conflict":3,"_version":1,"_version_type":"internal" } }
{"doc":{"parentId":"ppaa","type":"tt","index":"ddd","routing":"test","goodsName":"dddd","brandId":0,"brandName":null,"shopCustomCategoryId":0,"goodsType":null,"quantityOfPacking":null,"freePostage":null,"postage":null,"goodsDescription":null,"packingDescription":null,"salesUnit":null,"minimumUnit":null,"minSalesPrice":0.0},"doc_as_upsert":true,"_source":true}
{ "update" : { "_index" : "aa", "_type" : "tt", "_id" : "aa", "parent" : "ppaa", "_routing" : "test","retry_on_conflict":3,"_version":1,"_version_type":"internal" } }
{"doc":{"parentId":"ppaa","type":"tt","index":"ddd","routing":"test","goodsName":"dddd","brandId":0,"brandName":null,"shopCustomCategoryId":0,"goodsType":null,"quantityOfPacking":null,"freePostage":null,"postage":null,"goodsDescription":null,"packingDescription":null,"salesUnit":null,"minimumUnit":null,"minSalesPrice":0.0},"doc_as_upsert":true,"_source":true}
```

reponse报文这里不做介绍，如果被标准的returnSource属性为true，那么在response中将包含文档的source字段信息。



# 4.参考资料

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-bulk.html>

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-update.html>

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-index_.html>

<https://my.oschina.net/bboss/blog/1556866>

<https://my.oschina.net/bboss/blog/1801273>



## 开发交流

**elasticsearch技术交流群：166471282**

**elasticsearch微信公众号：**

**![bboss微信公众号：bbossgroups](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)**

 