## Elasticsearch地理位置维护及检索

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

Elasticsearch地理位置信息维护及检索/排序案例分享



# 1.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置es客户端到工程



# 2.定义和创建带地理位置类型的mapping

创建一个city索引表结构,字段location的 类型为geo_point,并且定义一个检索的dsl语句

在resources目录下创建文件esmapper/address.xml,内容如下：

```
<properties>
<property name="createCityIndice"><![CDATA[{
  "settings": {
         "number_of_shards": 6,
         "index.refresh_interval": "5s"
  },
  "mappings": {
                "city": {
                    "properties": {
                        "standardAddrId":{
                            "type":"keyword"
                        },
                        "detailName": {
                            "type": "text",
                             
                            "fields": {
                                "keyword": {
                                    "type": "keyword"
                                }
                            }
                        },
                        "cityName":{
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword"
                                }
                            }
                        },
                        "countyName":{
                            "type": "text",
                            "fields": {
                                "keyword": {
                                    "type": "keyword"
                                }
                            }
                        },
                        "location":{
                            "type": "geo_point"
                        }

                    }
                }
            }
}]]></property>
<property name="locationSearch"><![CDATA[{
        "size": 100,
        "sort": [
            {
                "_geo_distance": { ##按离指定地理坐标对应的地理位置远近距离升序排序
                    "unit": "km",
                    "order": "asc",
                    "location": { ##指定参考地理坐标位置
                        "lon": #[lon],
                        "lat": #[lat]
                    }
                }
            },
            {
                "totalSaleNum": {
                    "order": "desc"
                }
            }
        ],
        "query": {
            "bool": {
                "must": [
                    {
                          "match_phrase_prefix" : {
                                "detailName" : {
                                    "query" : #[detailName]
                                }
                            }

                    },
                    {
                        "geo_distance": {
                            "distance": #[distance],
                            "location": {
                                "lon": #[lon],
                                "lat": #[lat]
                            }
                        }
                    }
                ]
            }
        }
    }]]></property>
</properties>
```

创建索引表

```
//创建加载配置文件的客户端工具，单实例多线程安全，第一次运行要预加载，有点慢
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/address.xml");
		try {
			//先删除名称为city的mapping
			clientUtil.dropIndice("city");
		} catch (ElasticSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//再创建mapping
		clientUtil.createIndiceMapping("city",//索引表名称
				"createCityIndice");//索引表mapping dsl脚本名称，在esmapper/address.xml中定义createCityIndice
        String mapping = clientUtil.getIndice("city");//获取刚才创建的索引结构
        System.out.println(mapping);
```



# 3.添加索引文档

```
Map<String,String> params = new HashMap<String,String>();
		params.put("cityName","潭市");
		params.put("standardAddrId","38130122");
		params.put("detailName","XX市花园办事处YY路四冶生活区4-11栋33单元1层1010");
		params.put("location","28.292781,117.238963");
		params.put("countyName","中国");
ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();				
clientUtil.addDocument("city",//索引名称
                       "city",//索引类型
                        params);//索引数据对象
                        "refresh");//强制刷新索引数据，让插入数据实时生效，如果考虑性能需要，可以去掉refresh参数
```



# 4.地理位置检索 

```
ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/address.xml");
		Map<String,String> params = new HashMap<String,String>();
		params.put("detailName","海域香廷EE栋1单元3层302室");
		params.put("distance","0.5km");
		params.put("lon","115.824994");
		params.put("lat","28.666162");
//返回map对象列表，也可以返回其他实体对象列表
		ESDatas<Map> datas = clientUtil.searchList("city/_search","locationSearch",params,Map.class);
//返回json报文
		System.out.print(clientUtil.executeRequest("city/_search","locationSearch",params));
```

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



