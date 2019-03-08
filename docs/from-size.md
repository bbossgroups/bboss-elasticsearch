# ElasticSearch From-Size分页案例

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 



# 1.定义from-size检索的dsl

定义from-size检索的dsl，里面包含外部需要传入的检索条件参数变量和分页参数变量： 

```
<!--
       一个简单的检索dsl,中有四个变量
       applicationName1
       applicationName2
       startTime
       endTime
       通过map传递变量参数值

       变量语法参考文档：
   -->
    <property name="searchPagineDatas">
        <![CDATA[{
            "query": {
                "bool": {
                    "filter": [
                        {  ## 多值检索，查找多个应用名称对应的文档记录
                            "terms": {
                                "applicationName.keyword": [#[applicationName1],#[applicationName2]]
                            }
                        },
                        {   ## 时间范围检索，返回对应时间范围内的记录，接受long型的值
                            "range": {
                                "agentStarttime": {
                                    "gte": #[startTime],##统计开始时间
                                    "lt": #[endTime]  ##统计截止时间
                                }
                            }
                        }
                    ]
                }
            },
            ## 分页起点
            "from":#[from],
            ## 最多返回size条记录
            "size":#[size]
        }]]>
    </property>
```

# 2.加载dsl所在配置文件，并执行from-size分页检索

```
    /**
	 * 分页检索文档
	 * @throws ParseException
	 */
	public void testPagineSearch() throws ParseException {
		//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/demo.xml");
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
		 
		ESDatas<Demo> esDatas = null;//返回的文档封装对象类型
		//保存总记录数
		long totalSize = 0;
		//保存每页结果对象列表，最多返回1000条记录
		List<Demo> demos = null;
		int i = 0; //页码
		do{//遍历获取每页的记录
			//设置分页参数
			params.put("from",i * 1000);//分页起点
			params.put("size",1000);//每页返回1000条
			i ++;//往前加页码
			//执行查询，demo为索引表，_search为检索操作action
			 esDatas =  //ESDatas包含当前检索的记录集合，最多1000条记录，由dsl中的size属性指定
					clientUtil.searchList("demo/_search",//demo为索引表，_search为检索操作action
							"searchPagineDatas",//esmapper/demo.xml中定义的dsl语句
							params,//变量参数
							Demo.class);//返回的文档封装对象类型
			demos = esDatas.getDatas();//每页结果对象列表，最多返回1000条记录
			totalSize = esDatas.getTotalSize();//总记录数
			if(i * 1000 > totalSize)
				break;
		}while(true);

//		String json = clientUtil.executeRequest("demo/_search",//demo为索引表，_search为检索操作action
//				"searchDatas",//esmapper/demo.xml中定义的dsl语句
//				params);

//		String json = com.frameworkset.util.SimpleStringUtil.object2json(demos);

		System.out.println(totalSize);
	}
```



# 3.完整的demo实例工程

<https://github.com/bbossgroups/eshelloword-booter>

<https://gitee.com/bbossgroups/eshelloword-booter>



# 4 开发交流

**elasticsearch技术交流群：166471282**

**elasticsearch微信公众号：**

**![bboss微信公众号：bbossgroups](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)**