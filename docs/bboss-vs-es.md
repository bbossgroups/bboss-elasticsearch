# bboss es对比直接使用es客户端的优势

![bboss](https://static.oschina.net/uploads/user/47/94045_50.jpg?t=1386945037000)

 

   bboss es和直接使用es客户端的优点到底在哪里呢？下面做个简单的阐述：

​    es官方客户端有两种：restclient和transprortclient，前者是基于restful的，直接操作各种restful api和query dsl，比较简单，没有orm功能；后者是基于java api封装的orm框架，封装比较死板，不太灵活，兼容性差，不能像调用restful那样直接操作query dsl，所以也不能直接针对query dsl，在head插件或者kibana里面调试和调优query dsl，写出了query dsl还要想方设法转换成对应的java api的调用方式。

​    相比较而然，bboss es融合了es官方提供的两种方式（restful和transprortclient）的功能，涵盖两方面能力，提供了（orm和restful，直接使用query dsl），是一个综合型的es客户端,主要优势如下：

1. bboss es底层直接基于es 的http restful协议，因此支持所有的es的restful功能，采用连接池技术管理http连接，高效；
2. 支持x-pack安全认证；
3. 支持集群负载和容灾以及节点自动发现；
4. 提供了丰富的orm api（增删改查、批量增删改，聚合统计等），api简洁易用；
5. 基于xml配置文件管理query dsl脚本，在query dsl的基础上，提供了简单强大的动态控制语法结构，支持if/else,if/elseif/else,foreach循环控制结构，语法风格非常类似于mybatis管理sql语句的语法风格，但是更加简洁高效；
6. 将query dsl脚本从java代码剥离，提供query dsl热加载功能，实时修改实时生效，开发调试效率高，可以与es head和kibana的deptool配合使用；
7. 开发和配置也非常简单，只需要引入bboss es的maven坐标或者gradle坐标，无需依赖es官方的jar包，几乎兼容es的各个版本(向前、向后兼容，前提是编写query dsl脚本要兼容)；
8. bboss es即提供高阶的orm api，也提供了低阶原生restful以及java transport的支持，可以方便地根据自己的实际需要选用。
9. ​    原生的restful的使用，bboss es直接发http restful请求，可以指定http post，get，delete，put方法，返回json报文，有问题直接抛异常。相关示例在新闻《[高性能Elasticsearch ORM开发库bboss es 5.0.3.7.8发布](https://www.oschina.net/news/90641/bboss-es-5-0-3-7-8-released)》中都有介绍，而且除了返回string类型的json报文，还可以指定ResponseHandler回调处理接口，自行封装成自己想要的对象结构，例如：     

```
Map<String,Object> state = clientUtil.executeHttp("_cluster/state",ClientInterface.HTTP_GET,
                                       new MapResponseHandler());//返回map结构
```

​    10.对于响应的异常处理，如果restful返回异常报文，都会以ElasticsearchException抛出到应用端。

show me the code:

```
 //一个完整的批量添加和修改索引文档的案例  
SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		List<Demo> demos = new ArrayList<>();
		Demo demo = new Demo();
		demo.setDemoId(2l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo2");
		demo.setContentbody("this is content body2");
		demos.add(demo);

		demo = new Demo();
		demo.setDemoId(3l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo3");
		demo.setContentbody("this is content body3");
		demos.add(demo);

		//批量创建文档
		String response = clientUtil.addDateDocuments("demo",//索引表
				"demo",//索引类型
				demos);

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		//批量更新文档
		demo.setContentbody("updated");
		response = clientUtil.updateDocuments("demo-"+date,"demo",demos);
		System.out.println("updateDateDocument-------------------------");

		System.out.println(response);
        //获取索引文档，json格式
		response = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"2");//文档id
		System.out.println("getDocument-------------------------");
		System.out.println(response);
        //获取索引文档，返回Demo对象类型
		demo = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"3",//文档id
				Demo.class);
```

高性能elasticsearch ORM开发库使用介绍:
[开发指南](development.md)

基于bboss es开发的统计查询效果图：   
![img](https://static.oschina.net/uploads/img/201712/19104625_GiDw.jpg)

![img](https://static.oschina.net/uploads/img/201712/19104625_WiAf.png)

![img](https://static.oschina.net/uploads/img/201712/19104625_0s2d.jpg)

![img](https://static.oschina.net/uploads/img/201712/19104625_2DkP.png)

![img](https://static.oschina.net/uploads/img/201712/19104625_PsKL.png)

# 开发交流



bboss elasticsearch交流：166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



