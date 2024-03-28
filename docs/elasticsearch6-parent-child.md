# Elasticsearch 6.x-父子关系维护和检索

本次分享包括两篇文章

- 父子关系维护检索实战一 [Elasticsearch 5.x 父子关系维护检索实战](elasticsearch5-parent-child.md)
- 父子关系维护检索实战二 Elasticsearch 6.x 父子关系维护检索实战

本文是其中第二篇,适用于Elasticsearch 6.x,7.x

\- Elasticsearch 6.x 父子关系维护检索实战，涵盖以下部分内容：

1. Elasticsearch 6.x 中父子关系mapping结构设计
2. Elasticsearch 6.x 中维护父子关系数据
3. Elasticsearch 6.x 中has_child和has_parent查询的基本用法
4. Elasticsearch 6.x 中如何在检索中同时返回父子数据

Elasticsearch 6.x 案例源码


GitHub：https://github.com/bbossgroups/elasticsearch-example/tree/master/src/test/java/org/bboss/elasticsearchtest/jointype

# dsl配置

创建pager join_type父子关系dsl、导入测试数据dsl、父子关系检索dsl配置如下：

https://github.com/bbossgroups/eshelloword-booter/blob/master/src/main/resources/esmapper/joinparentchild.xml

```xml
<properties>
    <!--
    本案例适用于es 6.x,7.x
    创建包问卷索引表
    https://www.elastic.co/guide/en/elasticsearch/reference/6.2/parent-join.html
    https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-inner-hits.html
    https://www.elastic.co/guide/en/elasticsearch/reference/6.2/query-dsl-parent-id-query.html
    -->
    <property name="createPagerIndice">
        <![CDATA[{
            "settings": {
                "number_of_shards": 6,
                "index.refresh_interval": "5s"
            },
          "mappings": {
            "pagertype": {
              "properties": {
                "name": { ## 问题或者答案、评论名称
                    "type": "text",
                     "fields": { ##dsl注释 定义精确查找的内部keyword字段
                        "keyword": {
                            "type": "keyword"
                        }
                    }
                },

                "content": { ## 问题或者答案、评论内容
                    "type": "text",
                    "fields": { ##dsl注释 定义精确查找的内部keyword字段
                        "keyword": {
                            "type": "keyword"
                        }
                    }
                },
                "datatype": { ## 问题或者答案、评论类型 0 问题 1 答案 2 评论
                    "type": "integer"
                },
                "person": { ## 问题或者答案、评论创建人
                    "type": "text",
                     "fields": { ##dsl注释 定义精确查找的内部keyword字段
                        "keyword": {
                            "type": "keyword"
                        }
                    }
                },
                "created_date": { ## 注册日期
                    "type": "date",
                    "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                },
                "question_join": {
                  "type": "join",
                  "relations": {
                    "question": ["answer","comment"] ## 问题答案，问题评论，两个children
                  }
                }
              }
            }
          }

        }]]>
    </property>
    <!--
    导入问题信息：
    -->
    <property name="bulkImportQuestionData"  trim="false">
        <![CDATA[
            { "index": { "_id": "1" }}
            {"name": "q1","content": "This is a question 1","person": "john","datatype": 0,"created_date":"2013-05-28 00:00:00","question_join": {"name": "question"}}
            { "index": { "_id": "2" }}
            {"name": "q2","content": "This is a question 2","person": "john","datatype": 0,"created_date":"2013-04-25 00:00:00","question_join": {"name": "question"}}
            { "index": { "_id": "3" }}
            {"name": "q3","content": "This is a question 3","person": "john","datatype": 0,"created_date":"2013-04-26 00:00:00","question_join": {"name": "question"}}
            { "index": { "_id": "4" }}
            {"name": "q4","content": "This is a question 4","person": "john","datatype": 0,"created_date":"2015-05-28 00:00:00","question_join": {"name": "question"}}
            { "index": { "_id": "5" }}
            {"name": "q5","content": "This is a question 5","person": "john","datatype": 0,"created_date":"2017-05-28 00:00:00","question_join": {"name": "question"}}
        ]]>
    </property>
    <!--
   导入答案信息：
   -->
    <property name="bulkImportAnswerData"  trim="false">
        <![CDATA[
            { "index": { "_id": "6" ,"_routing":"1"}}
            {"name": "a1","content": "This is a answer 1","person": "john","datatype": 1,"created_date":"2013-05-28 00:00:00","question_join": {"name": "answer","parent": "1"}}
            { "index": { "_id": "7" ,"_routing":"1"}}
            {"name": "a2","content": "This is a answer 2","person": "john","datatype": 1,"created_date":"2014-05-28 00:00:00","question_join": {"name": "answer","parent": "1"}}
            { "index": { "_id": "8" ,"_routing":"2"}}
            {"name": "a3","content": "This is a answer 3","person": "john","datatype": 1,"created_date":"2016-05-28 00:00:00","question_join": {"name": "answer","parent": "2"}}
            { "index": { "_id": "9" ,"_routing":"2"}}
            {"name": "a4","content": "This is a answer 4","person": "john","datatype": 1,"created_date":"2015-05-28 00:00:00","question_join": {"name": "answer","parent": "2"}}
            { "index": { "_id": "10" ,"_routing":"3"}}
            {"name": "a5","content": "This is a answer 5","person": "john","datatype": 1,"created_date":"2017-05-28 00:00:00","question_join": {"name": "answer","parent": "3"}}
            { "index": { "_id": "11" ,"_routing":"3"}}
            {"name": "a6","content": "This is a answer 6","person": "john","datatype": 1,"created_date":"2018-05-28 00:00:00","question_join": {"name": "answer","parent": "3"}}
            { "index": { "_id": "12" ,"_routing":"4"}}
            {"name": "a7","content": "This is a answer 7","person": "john","datatype": 1,"created_date":"2019-05-28 00:00:00","question_join": {"name": "answer","parent": "4"}}
            { "index": { "_id": "13" ,"_routing":"4"}}
            {"name": "a8","content": "This is a answer 8","person": "john","datatype": 1,"created_date":"2012-05-28 00:00:00","question_join": {"name": "answer","parent": "4"}}
            { "index": { "_id": "14" ,"_routing":"5"}}
            {"name": "a9","content": "This is a answer 9","person": "john","datatype": 1,"created_date":"2011-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
            { "index": { "_id": "15" ,"_routing":"5"}}
            {"name": "a10","content": "This is a answer 10","person": "john","datatype": 1,"created_date":"2014-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
            { "index": { "_id": "16" ,"_routing":"5"}}
            {"name": "a11","content": "This is a answer 11","person": "john","datatype": 1,"created_date":"2016-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
            { "index": { "_id": "17" ,"_routing":"5"}}
            {"name": "a12","content": "This is a answer 12","person": "john","datatype": 1,"created_date":"2018-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
        ]]>
    </property>
    <!--
  导入评论信息：
  -->
    <property name="bulkImportCommentData"  trim="false">
        <![CDATA[
            { "index": { "_id": "18" ,"_routing":"1"}}
            {"name": "c1","content": "This is a comment 1","person": "john","datatype": 2,"created_date":"2013-05-28 00:00:00","question_join": {"name": "comment","parent": "1"}}
            { "index": { "_id": "19" ,"_routing":"1"}}
            {"name": "c2","content": "This is a comment 2","person": "john","datatype": 2,"created_date":"2014-05-28 00:00:00","question_join": {"name": "comment","parent": "1"}}
            { "index": { "_id": "20" ,"_routing":"2"}}
            {"name": "c3","content": "This is a comment 3","person": "john","datatype": 2,"created_date":"2015-05-28 00:00:00","question_join": {"name": "comment","parent": "2"}}
            { "index": { "_id": "21" ,"_routing":"2"}}
            {"name": "c4","content": "This is a comment 4","person": "john","datatype": 2,"created_date":"2016-05-28 00:00:00","question_join": {"name": "comment","parent": "2"}}
            { "index": { "_id": "22" ,"_routing":"3"}}
            {"name": "c5","content": "This is a comment 5","person": "john","datatype": 2,"created_date":"2017-05-28 00:00:00","question_join": {"name": "comment","parent": "3"}}
            { "index": { "_id": "23" ,"_routing":"3"}}
            {"name": "c6","content": "This is a comment 6","person": "john","datatype": 2,"created_date":"2018-05-28 00:00:00","question_join": {"name": "comment","parent": "3"}}
            { "index": { "_id": "24" ,"_routing":"4"}}
            {"name": "c7","content": "This is a comment 7","person": "john","datatype": 2,"created_date":"2014-05-28 00:00:00","question_join": {"name": "comment","parent": "4"}}
            { "index": { "_id": "25" ,"_routing":"4"}}
            {"name": "c8","content": "This is a comment 8","person": "john","datatype": 2,"created_date":"2016-05-28 00:00:00","question_join": {"name": "comment","parent": "4"}}
            { "index": { "_id": "26" ,"_routing":"5"}}
            {"name": "c9","content": "This is a comment 9","person": "john","datatype": 2,"created_date":"2013-05-28 00:00:00","question_join": {"name": "comment","parent": "5"}}
            { "index": { "_id": "27" ,"_routing":"5"}}
            {"name": "c10","content": "This is a comment 10","person": "john","datatype": 2,"created_date":"2016-05-28 00:00:00","question_join": {"name": "comment","parent": "5"}}
            { "index": { "_id": "28" ,"_routing":"5"}}
            {"name": "c11","content": "This is a comment 11","person": "john","datatype": 2,"created_date":"2018-05-28 00:00:00","question_join": {"name": "comment","parent": "5"}}
            { "index": { "_id": "29" ,"_routing":"5"}}
            {"name": "c12","content": "This is a comment 12","person": "john","datatype": 2,"created_date":"2019-05-28 00:00:00","question_join": {"name": "comment","parent": "5"}}
        ]]>
    </property>



    <!--根据问题名称，检索答案和评论信息-->
    <property name="hasParentSearchByName">
        <![CDATA[
            {
              "query": {
                "has_parent": {
                  "parent_type": "question",
                  "query": {
                    "match": {## 这是注释
                      "name": #[name]
                    }
                  }
                }
              }
            }
        ]]>
    </property>


    <!--根据问题名称，检索答案信息
    https://www.elastic.co/guide/en/elasticsearch/reference/6.2/query-dsl-parent-id-query.html
    -->
    <property name="hasParentIdSearch">
        <![CDATA[
            {##这是注释
              "query": {##这是注释
                "parent_id": {##这是注释
                  "type": "answer",##这是注释
                  "id": #[id]##这是注释
                }##这是注释
              }##这是注释
            }##这是注释
        ]]>
    </property>

    <!--根据问题名称检索答案和评论信息，同时返回问题及答案和评论信息-->
    <property name="hasParentSearchByQuestionNameReturnParent2ndChildren">
        <![CDATA[
            {
              "query": {
                "bool": {
                    "must": [
                        {
                            "has_parent": {
                              "parent_type": "question",
                              "query": {
                                "match": {
                                   "name": #[name]
                                }
                              },
                              "inner_hits": {}
                            }
                        }
                    ],
                    "filter": [
                      {
                        "term": {
                          "datatype": {
                            "value": 1
                          }
                        }
                      }
                    ]
                }
              }
            }
        ]]>
    </property>

    <!--根据公司所在的国家信息检索员工信息和子文档信息，同时返回员工所属的公司信息-->
    <property name="hasParentSearchByNameReturnAnswerAndComment">
        <![CDATA[
        {
          "query": {
            "bool": {
              "should": [
                {
                    "match_all":{}
                },
                {
                  "has_child": {
                    "score_mode": "none",
                    "type": "comment"
                    ,"query": {
                      "match_all": {}

                    },"inner_hits":{}
                  }
                }
              ]
              ,"must": [
                {
                  "has_child": {
                    "score_mode": "none",
                    "type": "answer"
                    ,"query": {
                      "bool": {
                        "filter": [
                          {
                            "term": {
                              "datatype": {
                                "value": 1
                              }
                            }
                          }
                        ]
                      }
                    },"inner_hits":{}
                  }
                }
                ]
            }
          }
        }
        ]]>
    </property>

</properties>
```

# 建立索引表

上面的mapping中定义了question_join类型，表示question和answer、comment之间的父子关系：

```
		"question_join": {
		  "type": "join",
		  "relations": {
			"question": ["answer","comment"] ## 问题答案，问题评论，两个children
		  }
		}
```
通过bboss客户端创建名称为pager的索引：

```
	/**
	 * 创建join父子关系indice ：pager
	 */
	private void createPagerIndice() {
		try {
			clientInterface.dropIndice("pager");
		} catch (Exception e) {

		}
		String response = clientInterface.createIndiceMapping("pager", "createPagerIndice");
		System.out.println(response);
		String mapping = clientInterface.getIndexMapping("pager");
		System.out.println(mapping);

	}
```

# 测试数据导入和执行检索

```java
package org.bboss.elasticsearchtest.jointype;


import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.elasticsearch.client.ResultUtil;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.serial.ESInnerHitSerialThreadLocal;
import org.frameworkset.elasticsearch.template.ESInfo;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JoinTypeTest {
   private ClientInterface clientInterface = ElasticSearchHelper.getConfigRestClientUtil("esmapper/joinparentchild.xml");

   /**
    * 创建join父子关系indice ：pager
    */
   private void createPagerIndice() {
      try {
         clientInterface.dropIndice("pager");
      } catch (Exception e) {

      }
      String response = clientInterface.createIndiceMapping("pager", "createPagerIndice");
      System.out.println(response);
      String mapping = clientInterface.getIndexMapping("pager");
      System.out.println(mapping);

   }

   /**
    * 导入测试数据
    * 6.x对bulk的处理更加严格，所以从配置文件中获取到要导入的数据，trim掉空格，补上换行符
    */
   private void importDatas() {


      ClientInterface restClient = ElasticSearchHelper.getRestClientUtil();
      //导入数据,并且实时刷新，测试需要，实际环境不要带refresh
      ESInfo esInfo = clientInterface.getESInfo("bulkImportQuestionData");
      StringBuilder data = new StringBuilder();
      data.append(esInfo.getTemplate().trim());
      data.append("\n");
      restClient.executeHttp("pager/pagertype/_bulk?refresh", data.toString(), ClientUtil.HTTP_POST);
      //导入数据,并且实时刷新，测试需要，实际环境不要带refresh
      data.setLength(0);
      esInfo = clientInterface.getESInfo("bulkImportAnswerData");
      data = new StringBuilder();
      data.append(esInfo.getTemplate().trim());
      data.append("\n");
      restClient.executeHttp("pager/pagertype/_bulk?refresh", data.toString(), ClientUtil.HTTP_POST);
      //导入数据,并且实时刷新，测试需要，实际环境不要带refresh
      data.setLength(0);
      esInfo = clientInterface.getESInfo("bulkImportCommentData");
      data = new StringBuilder();
      data.append(esInfo.getTemplate().trim());
      data.append("\n");
      restClient.executeHttp("pager/pagertype/_bulk?refresh", data.toString(), ClientUtil.HTTP_POST);
      long companycount = clientInterface.countAll("pager/pagertype");
      System.out.println(companycount);

   }

   /**
    * 根据问题名称检索答案和评论信息，同时返回问题及答案和评论信息
    */
   public void hasParentSearchByCountryReturnParent2ndMultiChildren() {
      Map<String, Object> params = new HashMap<String, Object>();//没有检索条件，构造一个空的参数对象
//    params.put("name","Alice Smith");

      try {
         //设置子文档的类型和对象映射关系
         ESInnerHitSerialThreadLocal.setESInnerTypeReferences("answer", Answer.class);//指定inner查询结果对于answer类型和对应的对象类型Answer
         ESInnerHitSerialThreadLocal.setESInnerTypeReferences("comment", Comment.class);//指定inner查询结果对于comment类型和对应的对象类型Comment
//       String response = clientInterface.executeRequest("pager/pagertype/_search", "hasParentSearchByNameReturnAnswerAndComment", params);
         ESDatas<Question> escompanys = clientInterface.searchList("pager/pagertype/_search",
               "hasParentSearchByNameReturnAnswerAndComment", params, Question.class);
//       escompanys = clientUtil.searchAll("client_info",Basic.class);
         long totalSize = escompanys.getTotalSize();
         List<Question> clientInfos = escompanys.getDatas();//获取符合条件的数据
         //查看问题信息以及对应的答案和评论信息
         for (int i = 0; clientInfos != null && i < clientInfos.size(); i++) {
            Question question = clientInfos.get(i);
            List<Answer> answers = ResultUtil.getInnerHits(question.getInnerHits(), "answer");
            if (answers != null)
               System.out.println(answers.size());
            List<Comment> comments = ResultUtil.getInnerHits(question.getInnerHits(), "comment");
            if (comments != null)
               System.out.println(comments.size());


         }
      } finally {
         ESInnerHitSerialThreadLocal.clean();//清空inner查询结果对于雇员类型
      }
   }

   /**
    * 根据问题名称，检索答案和评论信息以及对应的问题信息
    */
   public void hasParentSearchByCountryReturnParent2ndChildren() {

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("name", "q4");

      try {
         ESInnerHitSerialThreadLocal.setESInnerTypeReferences(Question.class);//指定inner查询结果对于问题类型,问题只有一个文档类型，索引不需要显示指定Question类型信息
         ESDatas<Answer> escompanys = clientInterface.searchList("pager/pagertype/_search",
               "hasParentSearchByQuestionNameReturnParent2ndChildren", params, Answer.class);
         List<Answer> employeeList = escompanys.getDatas();
         long totalSize = escompanys.getTotalSize();
         //查看答案和评论信息以及对应的问题信息
         for (int i = 0; i < employeeList.size(); i++) {
            Answer employee = employeeList.get(i);
            List<Question> companies = ResultUtil.getInnerHits(employee.getInnerHits(), "question");
            System.out.println(companies.size());
         }
      } finally {
         ESInnerHitSerialThreadLocal.clean();//清空inner查询结果对于公司类型
      }
   }

   /**
    * 根据问题名称，检索答案信息
    */

   public void hasParentSearchByName() {

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("name", "q4");


      ESDatas<Answer> escompanys = clientInterface.searchList("pager/pagertype/_search",
            "hasParentSearchByName", params, Answer.class);
      List<Answer> employeeList = escompanys.getDatas();
      long totalSize = escompanys.getTotalSize();
      //查看符合条件的问题对应的答案信息
      for (int i = 0; i < employeeList.size(); i++) {
         Answer employee = employeeList.get(i);
         System.out.println(employee.getDatatype());
      }


   }

   /**
    * 根据问题id，检索答案信息
    */
   public void hasParentIdSearch() {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("id", "1");


      ESDatas<Answer> escompanys = clientInterface.searchList("pager/pagertype/_search",
            "hasParentIdSearch", params, Answer.class);
      List<Answer> employeeList = escompanys.getDatas();//根据问题id，检索答案信息
      long totalSize = escompanys.getTotalSize();
      //查看答案信息
      for (int i = 0; i < employeeList.size(); i++) {
         Answer employee = employeeList.get(i);
         System.out.println(employee.getDatatype());
      }


   }

   /**
    * 运行demo的junit测试方法
    */
   @Test
   public void testJoin() {
      createPagerIndice();
      importDatas();
      hasParentIdSearch();
      hasParentSearchByName();
      hasParentSearchByCountryReturnParent2ndChildren();
      hasParentSearchByCountryReturnParent2ndMultiChildren();


   }
}
```

# 开发交流



bboss elasticsearch交流QQ群：21220580,166471282,3625720,154752521,166471103,166470856

**bboss elasticsearch微信公众号：**

<img src="images/qrcode.jpg"  height="200" width="200">



