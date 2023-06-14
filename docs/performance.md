# bboss性能基准测试（仅供参考）



- 批量添加2万条数据耗时1.5秒
- 对大索引表文档统计计数耗时10毫秒
- 获取一条json文档耗时2毫秒
- 获取一条Object文档耗时3毫秒
- scroll查询并处理2万条记录：0.6s
- scrollSlice 并行查询并处理2万条记录：0.1s

# 测试环境

elasticsearch： 6.9.9 单机，内存配置1G

jdk 1.8

测试服务器：联想ThinkPAD S5笔记本电脑，64位 Intel i7-6700HQ 4核 16G内存，1T硬盘



# 性能测试代码

[DocumentCRUD](https://github.com/bbossgroups/eshelloword-booter/blob/master/src/main/java/org/bboss/elasticsearchtest/crud/DocumentCRUD.java)

```java
    /**
    * 批量导入20002条数据
    */
   public void testBulkAddDocuments() {
      //创建批量创建文档的客户端对象，单实例多线程安全
      ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/scroll.xml");
      List<Demo> demos = new ArrayList<Demo>();
      Demo demo = null;
      long start = System.currentTimeMillis();
      for(int i = 0 ; i < 20002; i ++) {
         demo = new Demo();//定义第一个对象
         demo.setDemoId((long)i);
         demo.setAgentStarttime(new Date());
         demo.setApplicationName("blackcatdemo"+i);
         demo.setContentbody("this is content body"+i);
         if(i % 2 == 0) {
            demo.setName("刘德华喜欢唱歌" + i);
         }
         else{
            demo.setName("张学友不喜欢唱歌" + i);
         }

         demo.setOrderId("NFZF15045871807281445364228");
         demo.setContrastStatus(2);
         demos.add(demo);//添加第一个对象到list中
      }
      //批量添加或者修改2万个文档，将两个对象添加到索引表demo中，批量添加2万条记录耗时1.8s，
      String response = clientUtil.addDocuments("demo",//索引表
            "demo",//索引类型
            demos,"refresh=true");//为了测试效果,启用强制刷新机制，实际线上环境去掉最后一个参数"refresh=true"
      long end = System.currentTimeMillis();
      System.out.println("BulkAdd 20002 Documents elapsed:"+(end - start)+"毫秒");
      start = System.currentTimeMillis();
      //scroll查询2万条记录：0.6s，参考文档：https://my.oschina.net/bboss/blog/1942562
      ESDatas<Demo> datas = clientUtil.scroll("demo/_search","{\"size\":1000,\"query\": {\"match_all\": {}}}","1m",Demo.class);
      end = System.currentTimeMillis();
      System.out.println("scroll SearchAll 20002 Documents elapsed:"+(end - start)+"毫秒");
      int max = 6;
      Map params = new HashMap();
      params.put("sliceMax", max);//最多6个slice，不能大于share数
      params.put("size", 1000);//每页1000条记录

      datas = clientUtil.scrollSlice("demo/_search","scrollSliceQuery", params,"1m",Demo.class);
      //scroll上下文有效期1分钟
      //scrollSlice 并行查询2万条记录：0.1s，参考文档：https://my.oschina.net/bboss/blog/1942562
      start = System.currentTimeMillis();
      datas = clientUtil.scrollSliceParallel("demo/_search","scrollSliceQuery", params,"1m",Demo.class);
      end = System.currentTimeMillis();
      System.out.println("scrollSlice SearchAll 20002 Documents elapsed:"+(end - start)+"毫秒");
      if(datas != null){
         System.out.println("scrollSlice SearchAll datas.getTotalSize():"+datas.getTotalSize());
         if(datas.getDatas() != null)
            System.out.println("scrollSlice SearchAll datas.getDatas().size():"+datas.getDatas().size());
      }
      long count = clientUtil.countAll("demo");

      System.out.println("addDocuments-------------------------" +count);
      //System.out.println(response);
      //获取第一个文档
      response = clientUtil.getDocument("demo",//索引表
            "demo",//索引类型
            "2");//w
//    System.out.println("getDocument-------------------------");
//    System.out.println(response);
      //获取第二个文档
      demo = clientUtil.getDocument("demo",//索引表
            "demo",//索引类型
            "3",//文档id
            Demo.class);
   }
```

# 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



