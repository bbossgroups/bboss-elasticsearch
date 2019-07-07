# ElasticSearch Indice mapping和Index Template管理

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

The best elasticsearch highlevel java rest api-----[bboss](README.md)

ElasticSearch客户端框架bboss的ClientInterface 接口提供了创建/修改、获取、删除索引Indice和IndexTemplate的方法，本文举例说明其使用方法。



# 1 准备工作

参考文档在项目中导入Elasticsearch客户端：[集成Elasticsearch Restful API案例分享](https://my.oschina.net/bboss/blog/1801273)

本文除了介绍索引Indice和Index Template的创建修改方法，还可以看到如何在dsl中添加注释的用法：

单行注释

```
## 注释内容
```

多行注释

```
#*

。。。。注释内容

。。。

*#
```

更多bboss dsl配置和定义的内容，参考文档：[高性能elasticsearch ORM开发库使用介绍](https://my.oschina.net/bboss/blog/1556866) 章节【**5.3 配置es查询dsl脚本语法**】



# 2 定义创建Indice的dsl脚本

在配置文件-[esmapper/demo.xml](https://gitee.com/bboss/eshelloword-booter/blob/master/src/main/resources/esmapper/demo.xml)中定义一个名称为createDemoIndice的dsl脚本：

```
    <!--
        创建demo需要的索引表结构
    -->
    <property name="createDemoIndice">
        <![CDATA[{
            "settings": {
                "number_of_shards": 6,
                "number_of_replicas": 1,
                "index.refresh_interval": "5s"
            },
            "mappings": {
                "demo": {
                    "properties": {
                        "demoId":{
                            "type":"long"
                        },
                        "contentbody": {
                            "type": "text" ##定义text类型的全文检索字段

                        },
                        "agentStarttime": {
                            "type": "date"
                             ## ,"format":"yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "applicationName": {
                            "type": "text",##定义text类型的全文检索字段
                            "fields": { ##定义精确查找的内部keyword字段
                                "keyword": {
                                    "type": "keyword"
                                }
                            }
                        },
                        "name": {
                            "type": "keyword"
                        }
                    }
                }
            }
        }]]>
    </property>
```



# 3 创建indice/判断indice是否存在/删除indice

根据上面定义的dsl脚本文件初始化ClientInterface对象，并创建索引表demo：

```
	public void testCreateIndice(){
	//创建加载配置文件的客户端工具，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo.xml");
		try {
			//判读索引表demo是否存在，存在返回true，不存在返回false
			boolean exist = clientUtil.existIndice("demo");

			//如果索引表demo已经存在先删除mapping
			if(exist)
				clientUtil.dropIndice("demo");
			//创建索引表demo
			clientUtil.createIndiceMapping("demo",//索引表名称
					"createDemoIndice");//索引表mapping dsl脚本名称，在esmapper/demo.xml中定义createDemoIndice
            //获取修改后的索引mapping结构
		    String mapping = clientUtil.getIndice("demo");
		    System.out.println(mapping);
		} catch (ElasticSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
```



# 4 定义索引Template dsl脚本

通过定义索引模板，定义表结构相同，但是索引表名称不同的索引表的建表模板，通过index_patterns中对应的模式名称来匹配索引模板适用的索引表：

```
   <property name="demoTemplate">
        <![CDATA[{
            "index_patterns": "demo-*", ## 5.x版本中请使用语法："template": "demo-*"
            "settings": {
                "number_of_shards": 30, ##定义分片数
                "number_of_replicas" : 2, ##定义副本数
                "index.refresh_interval": "5s" ## 定义索引写入刷新时间间隔
            },
            "mappings": {
                "demo": {
                    "properties": {
                        "contentbody": {
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
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
                                    "type": "keyword",
                                    "ignore_above": 256
                                }
                            }
                        }
                    }
                }
            }
        }]]>
    </property>
```



# 5 创建/获取/删除索引表模板

```
	public void testCreateTempate() throws ParseException{

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo.xml");
		//创建模板
		String response = clientUtil.createTempate("demotemplate_1",//模板名称
				"demoTemplate");//模板对应的脚本名称，在esmapper/demo.xml中配置
		System.out.println("createTempate-------------------------");
		System.out.println(response);//创建结果
		//获取模板
		/**
		 * 指定模板
		 * /_template/demoTemplate_1
		 * /_template/demoTemplate*
		 * 所有模板 /_template
		 *
		 */
		String template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET-------------------------");
		System.out.println(template);
        ElasticSearchHelper.getRestClientUtil().deleteTempate("demotemplate_1"); 

	}
```



# 6 修改和获取索引表结构

修改先前创建的demo表，为其中的type demo增加email关键字段

定义dsl结构-esmapper/demo.xml

```
    <!--
    修改demo 索引表的结构，增加email字段
    https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html
    -->
    <property name="updateDemoIndice">
        <![CDATA[{
          "properties": {
            "email": {
              "type": "keyword"
            }
          }
        }]]>
    </property>
```

修改和获取mapping结构的方法：

```
	public void updateDemoIndice(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
		//修改索引表demo中type为demo的mapping结构，增加email字段，对应的dsl片段updateDemoIndice定义在esmapper/demo.xml文件中
		String response = clientUtil.executeHttp("demo/_mapping/demo","updateDemoIndice",ClientUtil.HTTP_PUT);
		System.out.println(response);
		//获取修改后的索引mapping结构
		String mapping = clientUtil.getIndice("demo");
		System.out.println(mapping);
	}
```

# 7 获取所有的索引表清单

```java
//获取索引的索引表清单
public void testGetAllIndices(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   List<ESIndice> indices = clientInterface.getIndexes();
}
//获取demo索引中demo类型对应的索引字段信息
public void testGetIndice(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   List<IndexField> indexFields = clientInterface.getIndexMappingFields("demo","demo");
}
```

# 8 索引别名管理

```java
@Test
public void testAddAlias(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   String response  = clientInterface.addAlias("demo","demoalias");
   System.out.println(response);
   response = clientInterface.getIndexMapping("demoalias",true);
   System.out.println(response);
   long count  = clientInterface.countAll("demoalias");
   System.out.println(count);
}

@Test
public void testRemoveAlias(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   String response  = clientInterface.removeAlias("demo","demoalias");
   System.out.println(response);
   try {
      response = clientInterface.getIndexMapping("demoalias", true);
      System.out.println(response);
   }
   catch (Exception e){
      e.printStackTrace();
   }
   long count  = clientInterface.countAll("demo");
   System.out.println(count);
}
```

# 9 重建新索引

```java
@Test
public void reIndex(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   String response  = clientInterface.reindex("demo","newdemo");
   System.out.println(response);
   response = clientInterface.getIndexMapping("newdemo",true);
   System.out.println(response);
   long count  = clientInterface.countAll("newdemo");
   System.out.println(count);
}
```

# 10 打开和关闭索引

```java
ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
clientInterface.closeIndex("demo");//关闭索引
clientInterface.openIndex("demo");//打开索引
```

# 11 索引segment段合并

```java
ClientInterface clientInterface =  ElasticSearchHelper.getRestClientUtil();

		clientInterface.forcemerge();//强制合并所以索引的segment

		MergeOption mergeOption = new MergeOption();
		mergeOption.setFlush(true);
		mergeOption.setMaxnumSegments(1);
		mergeOption.setOnlyExpungeDeletes(false);

		clientInterface.forcemerge(mergeOption );//强制合并所以索引的segment，并指定合并参数选项

		clientInterface.forcemerge("indice1");//强制合并索引indice1的segment

		clientInterface.forcemerge("indice1,indice2");//强制合并索引indice1和indice2的segment

		mergeOption = new MergeOption();
		mergeOption.setFlush(false);
		mergeOption.setMaxnumSegments(1);
		mergeOption.setOnlyExpungeDeletes(false);

		clientInterface.forcemerge("indice1",mergeOption );//强制合并索引indice1的segment，并指定合并参数选项

		clientInterface.forcemerge("indice1,indice2",mergeOption );//强制合并索引indice1和indice2的segment，并指定合并参数选项
```

关于mergeoption的说明

Request Parameters

The force merge API accepts the following request parameters:

| `max_num_segments`     | The number of segments to merge to. To fully merge the index, set it to `1`. Defaults to simply checking if a merge needs to execute, and if so, executes it. |
| ---------------------- | ------------------------------------------------------------ |
| `only_expunge_deletes` | Should the merge process only expunge segments with deletes in it. In Lucene, a document is not deleted from a segment, just marked as deleted. During a merge process of segments, a new segment is created that does not have those deletes. This flag allows to only merge segments that have deletes. Defaults to `false`. Note that this won’t override the`index.merge.policy.expunge_deletes_allowed` threshold. |
| `flush`                | Should a flush be performed after the forced merge. Defaults to `true`. |

参考文档：

https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html

# 12 索引分片离线重分配延迟时间设置

```java
ClientInterface clientInterface =  ElasticSearchHelper.getRestClientUtil();
clientInterface.unassignedNodeLeftDelayedTimeout("2d"); //全局设置
clientInterface.unassignedNodeLeftDelayedTimeout("cms_document","3d");//直接设置cms_document索引
System.out.println(clientInterface.executeHttp("cms_document/_settings?pretty",ClientInterface.HTTP_GET));//获取索引cms_document配置
clientInterface.unassignedNodeLeftDelayedTimeout("cms_document","3d");//直接设置 
System.out.println(clientInterface.getIndiceSetting("cms_document","pretty"));//获取索引cms_document配置
```

# 13 管理索引副本数

```java
@Test
	public void updateNumberOfReplicas(){
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();

		clientInterface.updateNumberOfReplicas(1);//全局设置
		clientInterface.updateNumberOfReplicas("cms_document",2);//直接设置cms_document索引
		System.out.println(clientInterface.executeHttp("cms_document/_settings?pretty",ClientInterface.HTTP_GET));//获取索引cms_document配置
		clientInterface.updateNumberOfReplicas("cms_document",0);//直接设置cms_document索引
		System.out.println(clientInterface.getIndiceSetting("cms_document","pretty"));//获取索引cms_document配置

	}
```

# 14 禁用/启用shared迁移

```java
/**
    * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/rolling-upgrades.html
    */
   @Test
   public void enableShared(){
      ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
//    System.out.println(clientInterface.flushSynced("cms_document"));//https://www.elastic.co/guide/en/elasticsearch/reference/6.3/indices-synced-flush.html
      System.out.println(clientInterface.flushSynced());//https://www.elastic.co/guide/en/elasticsearch/reference/6.3/indices-synced-flush.html
      System.out.println(clientInterface.disableClusterRoutingAllocation());//禁用share allocation
      System.out.println(clientInterface.getClusterSettings(false));//获取人工设置的集群配置，看看刚才的修改是否生效
      System.out.println(clientInterface.enableClusterRoutingAllocation());//启用share allocation
      System.out.println(clientInterface.getClusterSettings(false));//获取人工设置的集群配置，看看刚才的修改是否生效

   }
```

# 15 通用修改索引配置的方法

```java
@Test
public void testSetting(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   clientInterface.updateIndiceSetting("cms_document","index.unassigned.node_left.delayed_timeout","1d");
   System.out.println(clientInterface.getIndiceSetting("cms_document","pretty"));//获取索引cms_document配置
   clientInterface.updateAllIndicesSetting("index.unassigned.node_left.delayed_timeout","2d");
   System.out.println(clientInterface.getIndiceSetting("cms_document","pretty"));//获取索引cms_document配置
   Map<String,Object> settings = new HashMap<String,Object>();
   settings.put("index.unassigned.node_left.delayed_timeout","5d");
   settings.put("index.number_of_replicas",5);
   clientInterface.updateAllIndicesSettings(settings);
   System.out.println(clientInterface.getIndiceSetting("cms_document","pretty"));//获取索引cms_document配置
   settings.put("index.unassigned.node_left.delayed_timeout","3d");
   settings.put("index.number_of_replicas",6);
   clientInterface.updateIndiceSettings("cms_document",settings);
   System.out.println(clientInterface.getIndiceSetting("cms_document","pretty"));//获取索引cms_document配置

}
```

# 16 通用修改集群配置的方法



```java
//简单修改
@Test
public void updateClusterSetting(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   ClusterSetting clusterSetting = new ClusterSetting();
   clusterSetting.setKey("indices.recovery.max_bytes_per_sec");
   clusterSetting.setValue("50mb");
   clusterSetting.setPersistent(true);
   clientInterface.updateClusterSetting(clusterSetting);
   System.out.println(clientInterface.getClusterSettings());
   clusterSetting = new ClusterSetting();
   clusterSetting.setKey("indices.recovery.max_bytes_per_sec");
   clusterSetting.setValue(null);
   clusterSetting.setPersistent(true);
   clientInterface.updateClusterSetting(clusterSetting);
   System.out.println(clientInterface.getClusterSettings());
}

//批量修改
@Test
public void updateClusterSettings(){
   ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
   List<ClusterSetting> clusterSettingList = new ArrayList<ClusterSetting>();

   ClusterSetting clusterSetting = new ClusterSetting();
   clusterSetting.setKey("indices.recovery.max_bytes_per_sec");
   clusterSetting.setValue("50mb");
   clusterSetting.setPersistent(true);
   clusterSettingList.add(clusterSetting);

   clusterSetting = new ClusterSetting();
   clusterSetting.setKey("xpack.monitoring.collection.enabled");
   clusterSetting.setValue("true");
   clusterSetting.setPersistent(true);
   clusterSettingList.add(clusterSetting);

   clusterSetting = new ClusterSetting();
   clusterSetting.setKey("xpack.monitoring.collection.enabled");
   clusterSetting.setValue("true");
   clusterSetting.setPersistent(false);
   clusterSettingList.add(clusterSetting);

   clusterSetting = new ClusterSetting();
   clusterSetting.setKey("indices.recovery.max_bytes_per_sec");
   clusterSetting.setValue("50mb");
   clusterSetting.setPersistent(false);
   clusterSettingList.add(clusterSetting);

   clientInterface.updateClusterSettings(clusterSettingList);
   System.out.println(clientInterface.getClusterSettings(false));
   clusterSettingList = new ArrayList<ClusterSetting>();

   clusterSetting = new ClusterSetting();
   clusterSetting.setKey("xpack.monitoring.collection.enabled");
   clusterSetting.setValue(null);
   clusterSetting.setPersistent(true);
   clusterSettingList.add(clusterSetting);

   clusterSetting = new ClusterSetting();
   clusterSetting.setKey("indices.recovery.max_bytes_per_sec");
   clusterSetting.setValue(null);
   clusterSetting.setPersistent(false);
   clusterSettingList.add(clusterSetting);

   clientInterface.updateClusterSettings(clusterSettingList);
   System.out.println(clientInterface.getClusterSettings(false));
}
```

# 17 案例源码工程下载

<https://github.com/bbossgroups/eshelloword-booter>

<https://gitee.com/bboss/eshelloword-booter>



# 18 开发交流

开发指南：https://esdoc.bbossgroups.com/#/README



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



# 支持我们

<div align="left"></div>
<img src="images/alipay.png"  height="200" width="200">

