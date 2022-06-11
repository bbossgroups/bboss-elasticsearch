# Elasticsearch 7.x-父子关系维护和检索

本次分享包括两篇文章

- 父子关系维护检索实战一 [Elasticsearch 5.x 父子关系维护检索实战](elasticsearch5-parent-child.md)
- 父子关系维护检索实战二 Elasticsearch 7.x 父子关系维护检索实战

本文是其中第二篇,适用于Elasticsearch 7.x,7.x

 Elasticsearch 7.x 父子关系维护检索实战，涵盖以下部分内容：

1. Elasticsearch 7.x 中父子关系mapping结构设计
2. Elasticsearch 7.x 中维护父子关系数据
3. Elasticsearch 7.x 中has_child和has_parent查询的基本用法
4. Elasticsearch 7.x 中如何在检索中同时返回父子数据

Elasticsearch 7.x 案例源码


https://gitee.com/bboss/eshelloword-booter/blob/master/src/test/java/org/bboss/elasticsearchtest/jointype/JoinTypeTest7.java

# dsl配置

创建pager join_type父子关系dsl、导入测试数据dsl、父子关系检索dsl配置如下：

https://gitee.com/bboss/eshelloword-booter/blob/master/src/main/resources/esmapper/joinparentchild7.xml

```xml
<properties>
    <!--
    本案例适用于es 7.x
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
            { "index": { "_id": "6" ,"routing":"1"}}
            {"name": "a1","content": "This is a answer 1","person": "john","datatype": 1,"created_date":"2013-05-28 00:00:00","question_join": {"name": "answer","parent": "1"}}
            { "index": { "_id": "7" ,"routing":"1"}}
            {"name": "a2","content": "This is a answer 2","person": "john","datatype": 1,"created_date":"2014-05-28 00:00:00","question_join": {"name": "answer","parent": "1"}}
            { "index": { "_id": "8" ,"routing":"2"}}
            {"name": "a3","content": "This is a answer 3","person": "john","datatype": 1,"created_date":"2016-05-28 00:00:00","question_join": {"name": "answer","parent": "2"}}
            { "index": { "_id": "9" ,"routing":"2"}}
            {"name": "a4","content": "This is a answer 4","person": "john","datatype": 1,"created_date":"2015-05-28 00:00:00","question_join": {"name": "answer","parent": "2"}}
            { "index": { "_id": "10" ,"routing":"3"}}
            {"name": "a5","content": "This is a answer 5","person": "john","datatype": 1,"created_date":"2017-05-28 00:00:00","question_join": {"name": "answer","parent": "3"}}
            { "index": { "_id": "11" ,"routing":"3"}}
            {"name": "a6","content": "This is a answer 6","person": "john","datatype": 1,"created_date":"2018-05-28 00:00:00","question_join": {"name": "answer","parent": "3"}}
            { "index": { "_id": "12" ,"routing":"4"}}
            {"name": "a7","content": "This is a answer 7","person": "john","datatype": 1,"created_date":"2019-05-28 00:00:00","question_join": {"name": "answer","parent": "4"}}
            { "index": { "_id": "13" ,"routing":"4"}}
            {"name": "a8","content": "This is a answer 8","person": "john","datatype": 1,"created_date":"2012-05-28 00:00:00","question_join": {"name": "answer","parent": "4"}}
            { "index": { "_id": "14" ,"routing":"5"}}
            {"name": "a9","content": "This is a answer 9","person": "john","datatype": 1,"created_date":"2011-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
            { "index": { "_id": "15" ,"routing":"5"}}
            {"name": "a10","content": "This is a answer 10","person": "john","datatype": 1,"created_date":"2014-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
            { "index": { "_id": "16" ,"routing":"5"}}
            {"name": "a11","content": "This is a answer 11","person": "john","datatype": 1,"created_date":"2016-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
            { "index": { "_id": "17" ,"routing":"5"}}
            {"name": "a12","content": "This is a answer 12","person": "john","datatype": 1,"created_date":"2018-05-28 00:00:00","question_join": {"name": "answer","parent": "5"}}
        ]]>
    </property>
    <!--
  导入评论信息：
  -->
    <property name="bulkImportCommentData"  trim="false">
        <![CDATA[
            { "index": { "_id": "18" ,"routing":"1"}}
            {"name": "c1","content": "This is a comment 1","person": "john","datatype": 2,"created_date":"2013-05-28 00:00:00","question_join": {"name": "comment","parent": "1"}}
            { "index": { "_id": "19" ,"routing":"1"}}
            {"name": "c2","content": "This is a comment 2","person": "john","datatype": 2,"created_date":"2014-05-28 00:00:00","question_join": {"name": "comment","parent": "1"}}
            { "index": { "_id": "20" ,"routing":"2"}}
            {"name": "c3","content": "This is a comment 3","person": "john","datatype": 2,"created_date":"2015-05-28 00:00:00","question_join": {"name": "comment","parent": "2"}}
            { "index": { "_id": "21" ,"routing":"2"}}
            {"name": "c4","content": "This is a comment 4","person": "john","datatype": 2,"created_date":"2016-05-28 00:00:00","question_join": {"name": "comment","parent": "2"}}
            { "index": { "_id": "22" ,"routing":"3"}}
            {"name": "c5","content": "This is a comment 5","person": "john","datatype": 2,"created_date":"2017-05-28 00:00:00","question_join": {"name": "comment","parent": "3"}}
            { "index": { "_id": "23" ,"routing":"3"}}
            {"name": "c6","content": "This is a comment 6","person": "john","datatype": 2,"created_date":"2018-05-28 00:00:00","question_join": {"name": "comment","parent": "3"}}
            { "index": { "_id": "24" ,"routing":"4"}}
            {"name": "c7","content": "This is a comment 7","person": "john","datatype": 2,"created_date":"2014-05-28 00:00:00","question_join": {"name": "comment","parent": "4"}}
            { "index": { "_id": "25" ,"routing":"4"}}
            {"name": "c8","content": "This is a comment 8","person": "john","datatype": 2,"created_date":"2016-05-28 00:00:00","question_join": {"name": "comment","parent": "4"}}
            { "index": { "_id": "26" ,"routing":"5"}}
            {"name": "c9","content": "This is a comment 9","person": "john","datatype": 2,"created_date":"2013-05-28 00:00:00","question_join": {"name": "comment","parent": "5"}}
            { "index": { "_id": "27" ,"routing":"5"}}
            {"name": "c10","content": "This is a comment 10","person": "john","datatype": 2,"created_date":"2016-05-28 00:00:00","question_join": {"name": "comment","parent": "5"}}
            { "index": { "_id": "28" ,"routing":"5"}}
            {"name": "c11","content": "This is a comment 11","person": "john","datatype": 2,"created_date":"2018-05-28 00:00:00","question_join": {"name": "comment","parent": "5"}}
            { "index": { "_id": "29" ,"routing":"5"}}
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

public class JoinTypeTest7 {
	private ClientInterface clientInterface = ElasticSearchHelper.getConfigRestClientUtil("esmapper/joinparentchild7.xml");

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

	private List<Question> buildQuestions(){
		List<Question> questions = new ArrayList<Question>();
		Question question = new Question();
		question.setQid("1");
		question.setName("q1");
		question.setContent("This is a question 1");
		question.setPerson("john");
		question.setDatatype(0);//数据类型为：0 问题类型
		question.setCreatedDate(new Date());
		JoinSon joinSon = new JoinSon();
		joinSon.setName("question");
		question.setQuestionJoin(joinSon);
		questions.add(question);

		question = new Question();
		question.setQid("2");
		question.setName("q2");
		question.setContent("This is a question 2");
		question.setPerson("john");
		question.setDatatype(0);//数据类型为：0 问题类型
		question.setCreatedDate(new Date());
		joinSon = new JoinSon();
		joinSon.setName("question");
		question.setQuestionJoin(joinSon);
		questions.add(question);

		question = new Question();
		question.setQid("3");
		question.setName("q3");
		question.setContent("This is a question 3");
		question.setPerson("john");
		question.setDatatype(0);//数据类型为：0 问题类型
		question.setCreatedDate(new Date());
		joinSon = new JoinSon();
		joinSon.setName("question");
		question.setQuestionJoin(joinSon);
		questions.add(question);

		question = new Question();
		question.setQid("4");
		question.setName("q4");
		question.setContent("This is a question 4");
		question.setPerson("john");
		question.setDatatype(0);//数据类型为：0 问题类型
		question.setCreatedDate(new Date());
		joinSon = new JoinSon();
		joinSon.setName("question");
		question.setQuestionJoin(joinSon);
		questions.add(question);

		question = new Question();
		question.setQid("5");
		question.setName("q5");
		question.setContent("This is a question 5");
		question.setPerson("john");
		question.setDatatype(0);//数据类型为：0 问题类型
		question.setCreatedDate(new Date());
		joinSon = new JoinSon();
		joinSon.setName("question");
		question.setQuestionJoin(joinSon);
		questions.add(question);
		return questions;
	}

	private List<Comment> buildComments()  {
		try {
			List<Comment> comments = new ArrayList<Comment>();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Comment comment = new Comment();
			comment.setCid("18");
			comment.setRoutingId("1");
			comment.setName("c1");
			comment.setContent("This is a comment 1");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			JoinSon joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("1");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("19");
			comment.setRoutingId("1");
			comment.setName("c2");
			comment.setContent("This is a comment 2");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("1");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("20");
			comment.setRoutingId("2");
			comment.setName("c3");
			comment.setContent("This is a comment 3");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("2");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("21");
			comment.setRoutingId("2");
			comment.setName("c4");
			comment.setContent("This is a comment 4");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("2");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("22");
			comment.setRoutingId("3");
			comment.setName("c5");
			comment.setContent("This is a comment 5");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("3");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("23");
			comment.setRoutingId("3");
			comment.setName("c6");
			comment.setContent("This is a comment 6");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("3");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("24");
			comment.setRoutingId("4");
			comment.setName("c7");
			comment.setContent("This is a comment 7");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("4");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("25");
			comment.setRoutingId("4");
			comment.setName("c8");
			comment.setContent("This is a comment 8");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("4");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("26");
			comment.setRoutingId("5");
			comment.setName("c9");
			comment.setContent("This is a comment 9");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("5");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("27");
			comment.setRoutingId("5");
			comment.setName("c10");
			comment.setContent("This is a comment 10");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("5");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("28");
			comment.setRoutingId("5");
			comment.setName("c11");
			comment.setContent("This is a comment 11");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("5");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			comment = new Comment();
			comment.setCid("29");
			comment.setRoutingId("5");
			comment.setName("c12");
			comment.setContent("This is a comment 12");
			comment.setPerson("john");
			comment.setDatatype(2);//数据类型为：2 评论类型
			comment.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("comment");
			joinSon.setParent("5");
			comment.setQuestionJoin(joinSon);
			comments.add(comment);

			return comments;
		}
		catch (Exception e){
			return null;
		}
	}

	private List<Answer> buildAnswers()  {
		try {
			List<Answer> answers = new ArrayList<Answer>();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Answer answer = new Answer();
			answer.setAid("6");
			answer.setRoutingId("1");
			answer.setName("c1");
			answer.setContent("This is a answer 1");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			JoinSon joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("1");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("7");
			answer.setRoutingId("1");
			answer.setName("c2");
			answer.setContent("This is a answer 2");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("1");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("8");
			answer.setRoutingId("2");
			answer.setName("c3");
			answer.setContent("This is a answer 3");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("2");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("9");
			answer.setRoutingId("2");
			answer.setName("c4");
			answer.setContent("This is a answer 4");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("2");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("10");
			answer.setRoutingId("3");
			answer.setName("c5");
			answer.setContent("This is a answer 5");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("3");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("11");
			answer.setRoutingId("3");
			answer.setName("c6");
			answer.setContent("This is a answer 6");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("3");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("12");
			answer.setRoutingId("4");
			answer.setName("c7");
			answer.setContent("This is a answer 7");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("4");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("13");
			answer.setRoutingId("4");
			answer.setName("c8");
			answer.setContent("This is a answer 8");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("4");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("14");
			answer.setRoutingId("5");
			answer.setName("c9");
			answer.setContent("This is a answer 9");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("5");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("15");
			answer.setRoutingId("5");
			answer.setName("c10");
			answer.setContent("This is a answer 10");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("5");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("16");
			answer.setRoutingId("5");
			answer.setName("c11");
			answer.setContent("This is a answer 11");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("5");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			answer = new Answer();
			answer.setAid("17");
			answer.setRoutingId("5");
			answer.setName("c12");
			answer.setContent("This is a answer 12");
			answer.setPerson("john");
			answer.setDatatype(1);//数据类型为：1 答案类型
			answer.setCreatedDate(new Date());
			joinSon = new JoinSon();
			joinSon.setName("answer");
			joinSon.setParent("5");
			answer.setQuestionJoin(joinSon);
			answers.add(answer);

			return answers;
		}
		catch (Exception e){
			return null;
		}
	}

	/**
	 * 通过List集合导入雇员和公司数据
	 */
	public void importDataFromBeans()  {
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();

		//导入公司数据,并且实时刷新，测试需要，实际环境不要带refresh
		List<Question> questions = buildQuestions();
		clientUtil.addDocuments("pager",questions,"refresh");

		//导入雇员数据,并且实时刷新，测试需要，实际环境不要带refresh
		List<Comment> comments = buildComments();
		clientUtil.addDocuments("pager",comments,"refresh");
		List<Answer> answers = buildAnswers();
		clientUtil.addDocuments("pager",answers,"refresh");

	}
	/**
	 * 导入测试数据
	 * 7.x对bulk的处理更加严格，所以从配置文件中获取到要导入的数据，trim掉空格，补上换行符
	 */
	private void importDatas() {


		ClientInterface restClient = ElasticSearchHelper.getRestClientUtil();
		//导入数据,并且实时刷新，测试需要，实际环境不要带refresh
		ESInfo esInfo = clientInterface.getESInfo("bulkImportQuestionData");
		StringBuilder data = new StringBuilder();
		data.append(esInfo.getTemplate().trim());
		data.append("\n");
		restClient.executeHttp("pager/_bulk?refresh", data.toString(), ClientUtil.HTTP_POST);
		//导入数据,并且实时刷新，测试需要，实际环境不要带refresh
		data.setLength(0);
		esInfo = clientInterface.getESInfo("bulkImportAnswerData");
		data = new StringBuilder();
		data.append(esInfo.getTemplate().trim());
		data.append("\n");
		restClient.executeHttp("pager/_bulk?refresh", data.toString(), ClientUtil.HTTP_POST);
		//导入数据,并且实时刷新，测试需要，实际环境不要带refresh
		data.setLength(0);
		esInfo = clientInterface.getESInfo("bulkImportCommentData");
		data = new StringBuilder();
		data.append(esInfo.getTemplate().trim());
		data.append("\n");
		restClient.executeHttp("pager/_bulk?refresh", data.toString(), ClientUtil.HTTP_POST);
		long companycount = clientInterface.countAll("pager");
		System.out.println(companycount);

	}

	/**
	 * 根据问题名称检索答案和评论信息，同时返回问题及答案和评论信息
	 */
	public void hasParentSearchByCountryReturnParent2ndMultiChildren() {
		Map<String, Object> params = new HashMap<String, Object>();//没有检索条件，构造一个空的参数对象
//		params.put("name","Alice Smith");

		try {
			//设置子文档的类型和对象映射关系
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences("answer", Answer.class);//指定inner查询结果对于answer类型和对应的对象类型Answer
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences("comment", Comment.class);//指定inner查询结果对于comment类型和对应的对象类型Comment
//			String response = clientInterface.executeRequest("pager/_search", "hasParentSearchByNameReturnAnswerAndComment", params);
			ESDatas<Question> escompanys = clientInterface.searchList("pager/_search",
					"hasParentSearchByNameReturnAnswerAndComment", params, Question.class);
//			escompanys = clientUtil.searchAll("client_info",Basic.class);
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
			ESDatas<Answer> escompanys = clientInterface.searchList("pager/_search",
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


		ESDatas<Answer> escompanys = clientInterface.searchList("pager/_search",
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


		ESDatas<Answer> escompanys = clientInterface.searchList("pager/_search",
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
	/**
	 * 运行demo的junit测试方法
	 */
	@Test
	public void testJoinBean() {
		createPagerIndice();
		importDataFromBeans();
		hasParentIdSearch();
		hasParentSearchByName();
		hasParentSearchByCountryReturnParent2ndChildren();
		hasParentSearchByCountryReturnParent2ndMultiChildren();


	}
}
```

# 开发交流



bboss elasticsearch交流QQ群：21220580,166471282

**bboss elasticsearch微信公众号：**

<img src="https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg"  height="200" width="200">



