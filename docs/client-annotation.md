# ElasticSearch客户端注解使用介绍

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

*The best elasticsearch highlevel java rest api-----[bboss](README.md)* 

bboss提供了两大类PO对象属性注解：

1. 控制参数注解
2. 元数据注解

下面分别介绍。

# 1.ElasticSearch客户端控制参数注解

## 1.1 整体介绍

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



## 1.2 @JsonProperty注解使用

当elasticsearch索引表字段名称和java bean的字段名称不一致的情况下，采用@JsonProperty注解用来定义elasticsearch和java bean的field名称转换映射关系，使用实例如下：

com.fasterxml.jackson.annotation.JsonProperty

```java
@JsonProperty("max_score")
private Double maxScore;
```



## 1.3 使用示例

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



## 1.4 结合控制注解的批量文档修改操作

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

## 1.5 将属性值为null的字段忽略掉
com.fasterxml.jackson.annotation.JsonInclude
在对象上面添加注解@JsonInclude(Include.NON_NULL),例如:

```java
@JsonInclude(Include.NON_NULL) 
public class Order {
  ....
}
```

# 2.元数据注解

## 2.1 元数据注解介绍

元数据注解主要用于在检索文档时，将一系列元数据信息注入到返回的文档PO对象中，用来替代ESBaseData和ESId两个抽象类的作用。po对象可以继承ESBaseData和ESId这两个抽象类，这样查询后元数据信息自动设置到抽象类的属性中，但是索引文档中的字段可能和抽象类中的元数据字段名称冲突，这样就可以通过元数据注解来处理这种问题。

元数据注解以及对应的注解字段的类型说明如下：

```java
   @ESMetaId
   private String docid;//文档_id
   @ESMetaType  //文档对应索引类型信息
	private String  type;
	@ESMetaFields //文档对应索引字段信息
	private Map<String, List<Object>> fields;
	@ESMetaVersion  //文档对应版本信息
	private long version; 
	@ESMetaIndex //文档对应的索引名称
	private String index;
	@ESMetaHighlight //文档对应的高亮检索信息
	private Map<String,List<Object>> highlight;
	@ESMetaSort //文档对应的排序信息
	private Object[] sort;
	@ESMetaScore //文档对应的评分信息
	private Double  score;
	@ESMetaParentId //文档对应的父id
	private Object parent;
	@ESRouting //文档对应的路由信息
	private Object routing;
	@ESMetaFound //文档对应的是否命中信息
	private boolean found;
	@ESMetaNested //文档对应的nested检索信息
	private Map<String,Object> nested;
	@ESMetaInnerHits //文档对应的innerhits信息
	private Map<String,Map<String, InnerSearchHits>> innerHits;
	@ESMetaShard //文档对应的索引分片号
	private String shard;
	@ESMetaNode //文档对应的elasticsearch集群节点名称
	private String node;
	@ESMetaExplanation //文档对应的打分规则信息
	private Explanation explanation;
```

## 2.2 一个完成的PO对象

```java
package org.bboss.elasticsearchtest.crud;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import com.frameworkset.orm.annotation.*;
import org.frameworkset.elasticsearch.entity.Explanation;
import org.frameworkset.elasticsearch.entity.InnerSearchHits;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 测试实体，可以从ESBaseData对象继承meta属性，检索时会将文档的一下meta属性设置到对象实例中
 */
public class MetaDemo  {
	private Object dynamicPriceTemplate;
	//设定文档标识字段
	@ESMetaId
	private Long demoId;
	private String contentbody;
	/**  当在mapping定义中指定了日期格式时，则需要指定以下两个注解,例如
	 *
	 "agentStarttime": {
	 "type": "date",###指定多个日期格式
	 "format":"yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
	 }
	 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	 @Column(dataformat = "yyyy-MM-dd HH:mm:ss.SSS")
	 */

	protected Date agentStarttime;
	private String applicationName;
	private String orderId;
	private int contrastStatus;
	@ESMetaType
	private String  type;
	@ESMetaFields
	private Map<String, List<Object>> fields;
	@ESMetaVersion
	private long version;
	@ESMetaIndex
	private String index;//"_index": "trace-2017.09.01",
	@ESMetaHighlight
	private Map<String,List<Object>> highlight;
	@ESMetaSort
	private Object[] sort;
	@ESMetaScore
	private Double  score;
	@ESMetaParentId
	private Object parent;
	@ESRouting
	private Object routing;
	@ESMetaFound
	private boolean found;
	@ESMetaNested
	private Map<String,Object> nested;
	@ESMetaInnerHits
	private Map<String,Map<String, InnerSearchHits>> innerHits;
	@ESMetaShard
	private String shard;//"_index": "trace-2017.09.01",
	@ESMetaNode
	private String node;//"_index": "trace-2017.09.01",
	@ESMetaExplanation
	private Explanation explanation;//"_index": "trace-2017.09.01",
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name;

	public String getContentbody() {
		return contentbody;
	}

	public void setContentbody(String contentbody) {
		this.contentbody = contentbody;
	}

	public Date getAgentStarttime() {
		return agentStarttime;
	}

	public void setAgentStarttime(Date agentStarttime) {
		this.agentStarttime = agentStarttime;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Long getDemoId() {
		return demoId;
	}

	public void setDemoId(Long demoId) {
		this.demoId = demoId;
	}

	public Object getDynamicPriceTemplate() {
		return dynamicPriceTemplate;
	}

	public void setDynamicPriceTemplate(Object dynamicPriceTemplate) {
		this.dynamicPriceTemplate = dynamicPriceTemplate;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getContrastStatus() {
		return contrastStatus;
	}

	public void setContrastStatus(int contrastStatus) {
		this.contrastStatus = contrastStatus;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, List<Object>> getFields() {
		return fields;
	}

	public void setFields(Map<String, List<Object>> fields) {
		this.fields = fields;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public Map<String, List<Object>> getHighlight() {
		return highlight;
	}

	public void setHighlight(Map<String, List<Object>> highlight) {
		this.highlight = highlight;
	}

	public Object[] getSort() {
		return sort;
	}

	public void setSort(Object[] sort) {
		this.sort = sort;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	public Object getRouting() {
		return routing;
	}

	public void setRouting(Object routing) {
		this.routing = routing;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}

	public Map<String, Object> getNested() {
		return nested;
	}

	public void setNested(Map<String, Object> nested) {
		this.nested = nested;
	}

	public Map<String, Map<String, InnerSearchHits>> getInnerHits() {
		return innerHits;
	}

	public void setInnerHits(Map<String, Map<String, InnerSearchHits>> innerHits) {
		this.innerHits = innerHits;
	}

	public String getShard() {
		return shard;
	}

	public void setShard(String shard) {
		this.shard = shard;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Explanation getExplanation() {
		return explanation;
	}

	public void setExplanation(Explanation explanation) {
		this.explanation = explanation;
	}
}

```

## 2.3 元数据示例

单文档检索

```java
MetaDemo metaDemo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"3",//文档id
				MetaDemo.class);
```

列表检索

```java
//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
		//设定查询条件,通过map传递变量参数值,key对于dsl中的变量名称
		//dsl中有四个变量
		//        applicationName1
		//        applicationName2
		//        startTime
		//        endTime
		Map<String,Object> params = new HashMap<String,Object>();
		//设置applicationName1和applicationName2两个变量的值
		params.put("applicationName1","blackcatdemo2");
		params.put("applicationName2","blackcatdemo3");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//设置时间范围,时间参数接受long值
		params.put("startTime",dateFormat.parse("2017-09-02 00:00:00"));
		params.put("endTime",new Date());
		//执行查询，demo为索引表，_search为检索操作action
		ESDatas<MetaDemo> esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
				clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
				"searchDatas",//esmapper/demo.xml中定义的dsl语句
				params,//变量参数
						MetaDemo.class);//返回的文档封装对象类型


		//获取结果对象列表，最多返回1000条记录
		List<MetaDemo> demos = esDatas.getDatas();

		for(int i = 0; demos != null && i < demos.size(); i ++){
			MetaDemo demo = demos.get(i);
			//获取索引元数据
			Double score = demo.getScore();//文档评分
			String indexName = demo.getIndex();//索引名称
			String indexType = demo.getType();//索引type
			Map<String,Object> nested = demo.getNested();//文档neste信息
			Map<String,Map<String, InnerSearchHits>> innerHits = demo.getInnerHits();//文档父子查询数据
			Map<String,List<Object>> highlight = demo.getHighlight();//高亮检索数据
			Map<String,List<Object>> fields = demo.getFields();//检索字段信息
			long version = demo.getVersion();//文档版本号
			Object parent = demo.getParent();//文档父docId
			Object routing = demo.getRouting();//文档路由信息
			Long id = demo.getDemoId();//文档docId
			Object[] sort = demo.getSort();//排序信息
		}
```


# 3.参考资料

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-bulk.html>

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-update.html>

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-index_.html>

<https://my.oschina.net/bboss/blog/1556866>

<https://my.oschina.net/bboss/blog/1801273>



# 4.开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 5.支持我们

<div align="left"></div>
<img src="images/alipay.png"  height="200" width="200">



 