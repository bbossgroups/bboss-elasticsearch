# Elasticsearch 5.x-父子关系维护和检索

本次分享包括两篇文章

- 父子关系维护检索实战一 Elasticsearch 5.x 父子关系维护检索实战
- 父子关系维护检索实战二 [Elasticsearch 6.x 父子关系维护检索实战](elasticsearch6-parent-child.md)

本文是其中第一篇

\- Elasticsearch 5.x 父子关系维护检索实战，涵盖以下部分内容：

1. Elasticsearch 5.x 中父子关系mapping结构设计
2. Elasticsearch 5.x 中维护父子关系数据
3. Elasticsearch 5.x 中has_child和has_parent查询的基本用法
4. Elasticsearch 5.x 中如何在检索中同时返回父子数据

# 1.案例说明

以一个体检记录相关的数据来介绍本文涉及的相关功能，体检数据包括客户基本信息basic和客户医疗记录medical、客户体检记录exam、客户体检结果分析记录diagnosis，它们之间的关系图如下：

[![parent.png](https://elasticsearch.cn/uploads/article/20181211/e360beb09460d3de4d66303f8642c528.png)](https://elasticsearch.cn/uploads/article/20181211/e360beb09460d3de4d66303f8642c528.png)

我们采用Elasticsearch java客户端 

bboss-elastic

 来实现本文相关功能。

# 2.准备工作

参考文档《[高性能elasticsearch ORM开发库使用介绍](development.md)》导入和配置bboss客户端

# 3.定义ES 5.x 父子关系mapping结构

Elasticsearch 5.x中一个indice mapping支持多个mapping type，通过在子类型mapping中指定父类型的mapping type名字来设置父子关系，例如：

父类型

"basic": {

....

}

子类型：

"medical": { 

​      "_parent": { "type": "basic" },

​     .................

}

新建dsl配置文件-esmapper/Client_Info.xml，定义完整的mapping结构：createClientIndice

```
<properties>
  
    <!--
   创建客户信息索引索引表
   -->
    <property name="createClientIndice">
        <![CDATA[{
            "settings": {
                "number_of_shards": 6,
                "index.refresh_interval": "5s"
            },
            "mappings": {
                "basic": {  ##基本信息
                    "properties": {
                        "party_id": {
                            "type": "keyword"
                        },
                        "sex": {
                            "type": "keyword"
                        },
                        "mari_sts": {
                            "type": "keyword"
                        },
                        "ethnic": {
                            "type": "text"
                        },
                        "prof": {
                            "type": "text"
                        },
                        "province": {
                            "type": "text"
                        },
                        "city": {
                            "type": "text"
                        },
                        "client_type": {
                            "type": "keyword"
                        },
                        "client_name": {
                            "type": "text"
                        },
                        "age": {
                            "type": "integer"
                        },
                        "id_type": {
                            "type": "keyword"
                        },
                        "idno": {
                            "type": "keyword"
                        },
                        "education": {
                            "type": "text"
                        },
                        "created_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "birth_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "last_modified_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "etl_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        }
                    }
                },
                "diagnosis": { ##结果分析
                    "_parent": {
                        "type": "basic"
                    },
                    "properties": {
                        "party_id": {
                            "type": "keyword"
                        },
                        "provider": {
                            "type": "text"
                        },
                        "subject": {
                            "type": "text"
                        },
                        "diagnosis_type": {
                            "type": "text"
                        },
                        "icd10_code": {                           
                            "type": "keyword"
                        },
                        "sd_disease_name": {
                            "type": "text"                          
                        },
                        "created_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "last_modified_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "etl_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        }
                    }
                },
                "medical": { ##医疗情况
                    "_parent": {
                        "type": "basic"
                    },
                    "properties": {
                        "party_id": {
                            "type": "keyword"
                        },
                        "hos_name_yb": {
                            "type": "text"
                        },
                        "eivisions_name": {
                            "type": "text"
                        },
                        "medical_type": {
                            "type": "text"
                        },
                        "medical_common_name": {
                            "type": "text"
                        },
                        "medical_sale_name": {
                            "type": "text"
                        },
                        "medical_code": {
                            "type": "text"
                        },
                        "specification": {
                            "type": "text"
                        },
                        "usage_num": {
                            "type": "text"
                        },
                        "unit": {
                            "type": "text"
                        },
                        "usage_times": {
                            "type": "text"
                        },
                        "created_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "last_modified_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "etl_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        }
                    }
                },
                "exam": { ##检查结果
                    "_parent": {
                        "type": "basic"
                    },
                    "properties": {
                        "party_id": {
                            "type": "keyword"
                        },
                        "hospital": {
                            "type": "text"
                        },
                        "dept": {
                            "type": "text"
                        },
                        "is_ok": {
                            "type": "text"
                        },
                        "exam_result": {
                            "type": "text"
                        },
                        "fld1": {
                            "type": "text"
                        },
                        "fld2": {
                            "type": "text"
                        },
                        "fld3": {
                            "type": "text"
                        },
                        "fld4": {
                            "type": "text"
                        },
                        "fld5": {
                            "type": "text"
                        },
                        "fld901": {
                            "type": "text"
                        },
                        "fld6": {
                            "type": "text"
                        },
                        "fld902": {
                            "type": "text"
                        },
                        "fld14": {
                            "type": "text"
                        },
                        "fld20": {
                            "type": "text"
                        },
                        "fld21": {
                            "type": "text"
                        },
                        "fld23": {
                            "type": "text"
                        },
                        "fld24": {
                            "type": "text"
                        },
                        "fld65": {
                            "type": "text"
                        },
                        "fld66": {
                            "type": "text"
                        },
                        "fld67": {
                            "type": "text"
                        },
                        "fld68": {
                            "type": "text"
                        },
                        "created_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "last_modified_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        },
                        "etl_date": {
                            "type": "date",
                            "format": "yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
                        }
                    }
                }
            }
        }]]>
    </property>
</properties>
```

这个mapping中定义了4个索引类型：basic,exam,medical,diagnosis,其中basic是其他类型的父类型。

通过bboss客户端创建名称为client_info 的索引：

```
	public void createClientIndice(){
		//定义客户端实例，加载上面建立的dsl配置文件
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/Client_Info.xml");
		try {
			//client_info存在返回true，不存在返回false
			boolean exist = clientUtil.existIndice("client_info");

			//如果索引表client_info已经存在先删除mapping
			if(exist) {//先删除mapping client_info
				clientUtil.dropIndice("client_info");
			}
		} catch (ElasticSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//创建mapping client_info
		clientUtil.createIndiceMapping("client_info","createClientIndice");
		String client_info = clientUtil.getIndice("client_info");//获取最新建立的索引表结构client_info
		System.out.println("after createClientIndice clientUtil.getIndice(\"client_info\") response:"+client_info);
	}
```

# 4.维护Elasticsearch 5.x父子关系数据

- ## **定义对象**

首先定义四个对象，分别对应mapping中的四个索引类型，篇幅关系只列出主要属性

- Basic
- Medical
- Exam
- Diagnosis

通过注解@ESId指定基本信息文档_id

```
public class Basic extends ESBaseData {
    /**
     *  索引_id
     */
    @ESId
    private String party_id;
    private String sex;                     // 性别
    ......
}    
通过注解@ESParentId指定Medical关联的基本信息文档_id，Medical文档_id由ElasticSearch自动生成
public class Medical extends ESBaseData {
    @ESParentId
    private String party_id;          //父id
    private String hos_name_yb;         //就诊医院
    ...
}
通过注解@ESParentId指定Exam关联的基本信息文档_id，Exam文档_id由ElasticSearch自动生成
public class Exam extends ESBaseData {
    @ESParentId
    private String party_id;          //父id
    private String  hospital;           // 就诊医院
    ....
}    
通过注解@ESParentId指定Diagnosis关联的基本信息文档_id，Diagnosis文档_id由ElasticSearch自动生成
public class Diagnosis extends ESBaseData {
    @ESParentId
    private String party_id;          //父id
    private String provider;            //诊断医院
    private String subject;             //科室
    ......
}    
```

- ## **通过api维护测试数据**

对象定义好了后，通过bboss客户数据到之前建立好的索引client_info中。

```
	/**
	 * 录入体检医疗信息
	 */
	public void importClientInfoDataFromBeans()  {
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();

		//导入基本信息,并且实时刷新，测试需要，实际环境不要带refresh
		List<Basic> basics = buildBasics();
		clientUtil.addDocuments("client_info","basic",basics,"refresh");

		//导入医疗信息,并且实时刷新，测试需要，实际环境不要带refresh
		List<Medical> medicals = buildMedicals();
		clientUtil.addDocuments("client_info","medical",medicals,"refresh");

		//导入体检结果数据,并且实时刷新，测试需要，实际环境不要带refresh
		List<Exam> exams = buildExams();
		clientUtil.addDocuments("client_info","exam",exams,"refresh");

		//导入结果诊断数据,并且实时刷新，测试需要，实际环境不要带refresh
		List<Diagnosis> diagnosiss = buildDiagnosiss();
		clientUtil.addDocuments("client_info","diagnosis",diagnosiss,"refresh");
	}
	//构建基本信息集合
	private List<Basic> buildBasics() {
		List<Basic> basics = new ArrayList<Basic>();
		Basic basic = new Basic();
		basic.setParty_id("1");
		basic.setAge(60);
		basics.add(basic);
		//继续添加其他数据
		return basics;

	}
	//
构建医疗信息集合
	private List<Medical> buildMedicals() {
		List<Medical> medicals = new ArrayList<Medical>();
		Medical medical = new Medical();
		medical.setParty_id("1");//设置父文档id-基本信息文档_id
		medical.setCreated_date(new Date());
		medicals.add(medical);
		//继续添加其他数据
		return medicals;

	}
	//构建体检结果数据集合
	private List<Exam> buildExams() {
		List<Exam> exams = new ArrayList<Exam>();
		Exam exam = new Exam();
		exam.setParty_id("1");//设置父文档id-基本信息文档_id
		exams.add(exam);
		//继续添加其他数据
		return exams;
	}
	//构建结果诊断数据集合
	private List<Diagnosis> buildDiagnosiss() {
		List<Diagnosis> diagnosiss = new ArrayList<Diagnosis>();
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setParty_id("1");//设置父文档id-基本信息文档_id
		diagnosiss.add(diagnosis);
		//继续添加其他数据
		return diagnosiss;
	}
```

- ## **通过json报文批量导入测试数据**

除了通过addDocuments录入数据，还可以通过json报文批量导入数据

在配置文件esmapper/Client_Info.xml增加以下内容：

```
    <!--
   导入基本信息：
   -->
    <property name="bulkImportBasicData" trim="false">
        <![CDATA[
            { "index": { "_id": "1" }}
            {  "party_id":"1", "sex":"男", "mari_sts":"不详", "ethnic":"蒙古族", "prof":"放牧","birth_date":"1966-2-14 00:00:00", "province":"内蒙古", "city":"赤峰市","client_type":"1", "client_name":"安", "age":52,"id_type":"1", "idno":"1", "education":"初中","created_date":"2013-04-24 00:00:00","last_modified_date":"2013-04-24 00:00:00", "etl_date":"2013-04-24 00:00:00"}
            { "index": { "_id": "2" }}
            { "party_id":"2", "sex":"女", "mari_sts":"已婚", "ethnic":"汉族", "prof":"公务员","birth_date":"1986-07-06 00:00:00", "province":"广东", "city":"深圳","client_type":"1", "client_name":"彭", "age":32,"id_type":"1", "idno":"2", "education":"本科", "created_date":"2013-05-09 15:49:47","last_modified_date":"2013-05-09 15:49:47", "etl_date":"2013-05-09 15:49:47"}
            { "index": { "_id": "3" }}
            { "party_id":"3", "sex":"男", "mari_sts":"未婚", "ethnic":"汉族", "prof":"无业","birth_date":"2000-08-15 00:00:00", "province":"广东", "city":"佛山","client_type":"1", "client_name":"浩", "age":18,"id_type":"1", "idno":"3", "education":"高中", "created_date":"2014-09-01 09:49:27","last_modified_date":"2014-09-01 09:49:27", "etl_date":"2014-09-01 09:49:27" }
             { "index": { "_id": "4" }}
             { "party_id":"4", "sex":"女", "mari_sts":"未婚", "ethnic":"满族", "prof":"工人","birth_date":"1996-03-14 00:00:00", "province":"江苏", "city":"扬州","client_type":"1", "client_name":"慧", "age":22,"id_type":"1", "idno":"4", "education":"高中", "created_date":"2014-09-16 09:30:37","last_modified_date":"2014-09-16 09:30:37", "etl_date":"2014-09-16 09:30:37" }
            { "index": { "_id": "5" }}
            { "party_id":"5", "sex":"女", "mari_sts":"已婚", "ethnic":"汉族", "prof":"教师","birth_date":"1983-08-14 00:00:00", "province":"宁夏", "city":"灵武","client_type":"1", "client_name":"英", "age":35,"id_type":"1", "idno":"5", "education":"本科", "created_date":"2015-09-16 09:30:37","last_modified_date":"2015-09-16 09:30:37", "etl_date":"2015-09-16 09:30:37" }
            { "index": { "_id": "6" }}
            { "party_id":"6", "sex":"女", "mari_sts":"已婚", "ethnic":"汉族", "prof":"工人","birth_date":"1959-07-04 00:00:00", "province":"山东", "city":"青岛","client_type":"1", "client_name":"岭", "age":59,"id_type":"1", "idno":"6", "education":"小学", "created_date":"2015-09-01 09:49:27","last_modified_date":"2015-09-01 09:49:27", "etl_date":"2015-09-01 09:49:27" }
            { "index": { "_id": "7" }}
            { "party_id":"7", "sex":"女", "mari_sts":"未婚", "ethnic":"汉族", "prof":"学生","birth_date":"1999-02-18 00:00:00", "province":"山东", "city":"青岛","client_type":"1", "client_name":"欣", "age":19,"id_type":"1", "idno":"7", "education":"高中", "created_date":"2016-12-01 09:49:27","last_modified_date":"2016-12-01 09:49:27", "etl_date":"2016-12-01 09:49:27" }
            { "index": { "_id": "8" }}
            { "party_id":"8", "sex":"女", "mari_sts":"未婚", "ethnic":"汉族", "prof":"学生","birth_date":"2007-11-18 00:00:00", "province":"山东", "city":"青岛","client_type":"1", "client_name":"梅", "age":10,"id_type":"1", "idno":"8", "education":"小学", "created_date":"2016-11-21 09:49:27","last_modified_date":"2016-11-21 09:49:27", "etl_date":"2016-11-21 09:49:27" }
            { "index": { "_id": "9" }}
            { "party_id":"9", "sex":"男", "mari_sts":"不详", "ethnic":"回族", "prof":"个体户","birth_date":"1978-03-29 00:00:00", "province":"北京", "city":"北京","client_type":"1", "client_name":"磊", "age":40,"id_type":"1", "idno":"9", "education":"高中", "created_date":"2017-09-01 09:49:27","last_modified_date":"2017-09-01 09:49:27", "etl_date":"2017-09-01 09:49:27" }
            { "index": { "_id": "10" }}
            { "party_id":"10", "sex":"男", "mari_sts":"已婚", "ethnic":"汉族", "prof":"农民","birth_date":"1970-11-14 00:00:00", "province":"浙江", "city":"台州","client_type":"1", "client_name":"强", "age":47,"id_type":"1", "idno":"10", "education":"初中", "created_date":"2018-09-01 09:49:27","last_modified_date":"2018-09-01 09:49:27", "etl_date":"2018-09-01 09:49:27" }
        ]]>
    </property>
    <!--
  导入诊断信息
  -->
    <property name="bulkImportDiagnosisData" trim="false">
        <![CDATA[
            { "index": { "parent": "1" }}
            { "party_id":"1", "provider":"内蒙古医院", "subject":"","diagnosis_type":"","icd10_code":"J31.0", "sd_disease_name":"鼻炎","created_date":"2013-07-23 20:56:44", "last_modified_date":"2013-07-23 20:56:44", "etl_date":"2013-07-23 20:56:44" }

            { "index": { "parent": "1" }}
            { "party_id":"1", "provider":"内蒙古医院", "subject":"","diagnosis_type":"","icd10_code":"M47.8", "sd_disease_name":"颈椎病","created_date":"2013-09-23 20:56:44", "last_modified_date":"2013-09-23 20:56:44", "etl_date":"2013-09-23 20:56:44" }

            { "index": { "parent": "1" }}
            { "party_id":"1", "provider":"内蒙古医院", "subject":"","diagnosis_type":"","icd10_code":"E78.1", "sd_disease_name":"甘油三脂增高","created_date":"2018-09-20 09:27:44", "last_modified_date":"2018-09-20 09:27:44", "etl_date":"2018-09-20 09:27:44" }

            { "index": { "parent": "4" }}
            { "party_id":"4", "provider":"江苏医院", "subject":"","diagnosis_type":"","icd10_code":"J00", "sd_disease_name":"感冒","created_date":"2011-05-19 15:52:55", "last_modified_date":"2011-05-19 15:52:55", "etl_date":"2011-05-19 15:52:55" }

            { "index": { "parent": "6" }}
            { "party_id":"6", "provider":"山东医院", "subject":"","diagnosis_type":"","icd10_code":"H44", "sd_disease_name":"眼疾","created_date":"2016-04-08 10:42:18", "last_modified_date":"2016-04-08 10:42:18", "etl_date":"2016-04-08 10:42:18" }

            { "index": { "parent": "6" }}
            { "party_id":"6", "provider":"山东医院", "subject":"","diagnosis_type":"","icd10_code":"M47.8", "sd_disease_name":"颈椎病","created_date":"2016-04-08 10:42:18", "last_modified_date":"2016-04-08 10:42:18", "etl_date":"2016-04-08 10:42:18" }

            { "index": { "parent": "7" }}
            { "party_id":"7", "provider":"山东医院", "subject":"","diagnosis_type":"","icd10_code":"J00", "sd_disease_name":"感冒","created_date":"2017-04-08 10:42:18", "last_modified_date":"2017-04-08 10:42:18", "etl_date":"2017-04-08 10:42:18" }

            { "index": { "parent": "8" }}
            { "party_id":"8", "provider":"山东医院", "subject":"","diagnosis_type":"","icd10_code":"J00", "sd_disease_name":"感冒","created_date":"2018-04-08 10:42:18", "last_modified_date":"2018-04-08 10:42:18", "etl_date":"2018-04-08 10:42:18" }

            { "index": { "parent": "9" }}
            { "party_id":"9", "provider":"朝阳医院", "subject":"","diagnosis_type":"","icd10_code":"A03.901", "sd_disease_name":"急性细菌性痢疾","created_date":"2015-06-08 10:42:18", "last_modified_date":"2015-06-08 10:42:18", "etl_date":"2015-06-08 10:42:18" }
        ]]>
    </property>

    <!--
 导入医疗信息
 -->
    <property name="bulkImportMedicalData" trim="false">
        <![CDATA[
                { "index": { "parent": "1" }}
                { "party_id":"1", "hos_name_yb":"内蒙古医院", "eivisions_name":"", "medical_type":"","medical_common_name":"氟化钠", "medical_sale_name":"", "medical_code":"A01AA01", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

                { "index": { "parent": "1" }}
                { "party_id":"1", "hos_name_yb":"内蒙古医院", "eivisions_name":"", "medical_type":"","medical_common_name":"四环素", "medical_sale_name":"", "medical_code":"A01AB13", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2016-05-31 00:00:00", "last_modified_date":"2016-05-31 00:00:00", "etl_date":"2016-05-31 00:00:00" }

                { "index": { "parent": "1" }}
                { "party_id":"1", "hos_name_yb":"内蒙古医院", "eivisions_name":"", "medical_type":"","medical_common_name":"", "medical_sale_name":"盐酸多西环素胶丸", "medical_code":"A01AB22", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2016-03-18 00:00:00", "last_modified_date":"2016-03-18 00:00:00", "etl_date":"2016-03-18 00:00:00" }

                { "index": { "parent": "1" }}
                { "party_id":"1", "hos_name_yb":"内蒙古医院", "eivisions_name":"", "medical_type":"","medical_common_name":"盐酸多西环素分散片", "medical_sale_name":"", "medical_code":"A01AB22", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2013-07-23 20:56:44", "last_modified_date":"2013-07-23 20:56:44", "etl_date":"2013-07-23 20:56:44" }

                { "index": { "parent": "1" }}
                { "party_id":"1", "hos_name_yb":"内蒙古医院", "eivisions_name":"", "medical_type":"","medical_common_name":"地塞米松", "medical_sale_name":"", "medical_code":"A01AC02", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2013-09-23 20:56:44", "last_modified_date":"2013-09-23 20:56:44", "etl_date":"2013-09-23 20:56:44" }

                { "index": { "parent": "1" }}
                { "party_id":"1", "hos_name_yb":"内蒙古医院", "eivisions_name":"", "medical_type":"","medical_common_name":"肾上腺素", "medical_sale_name":"", "medical_code":"A01AD01", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2018-09-20 09:27:44", "last_modified_date":"2018-09-20 09:27:44", "etl_date":"2018-09-20 09:27:44" }

                 { "index": { "parent": "4" }}
                { "party_id":"4", "hos_name_yb":"江苏医院", "eivisions_name":"", "medical_type":"","medical_common_name":"地塞米松", "medical_sale_name":"", "medical_code":"A01AC02", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2011-05-19 15:52:55", "last_modified_date":"2011-05-19 15:52:55", "etl_date":"2011-05-19 15:52:55" }

                { "index": { "parent": "4" }}
                { "party_id":"4", "hos_name_yb":"江苏医院", "eivisions_name":"", "medical_type":"","medical_common_name":"四环素", "medical_sale_name":"", "medical_code":"A01AB13", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2018-04-08 10:42:18", "last_modified_date":"2018-04-08 10:42:18", "etl_date":"2018-04-08 10:42:18" }

                { "index": { "parent": "4" }}
                { "party_id":"4", "hos_name_yb":"江苏医院", "eivisions_name":"", "medical_type":"","medical_common_name":"诺氟沙星胶囊", "medical_sale_name":"", "medical_code":"A01AD01", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2015-06-08 10:42:18", "last_modified_date":"2015-06-08 10:42:18", "etl_date":"2015-06-08 10:42:18" }

                { "index": { "parent": "6" }}
                { "party_id":"6", "hos_name_yb":"山东医院", "eivisions_name":"", "medical_type":"","medical_common_name":"盐酸异丙肾上腺素片", "medical_sale_name":"", "medical_code":"A01AD01", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2014-01-23 20:56:44", "last_modified_date":"2014-01-23 20:56:44", "etl_date":"2014-01-23 20:56:44" }

                { "index": { "parent": "6" }}
                { "party_id":"6", "hos_name_yb":"山东医院", "eivisions_name":"", "medical_type":"","medical_common_name":"甲硝唑栓", "medical_sale_name":"", "medical_code":"A01AB17", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2018-06-08 10:42:18", "last_modified_date":"2018-06-08 10:42:18", "etl_date":"2018-06-08 10:42:18" }

                { "index": { "parent": "9" }}
                { "party_id":"9", "hos_name_yb":"朝阳医院", "eivisions_name":"", "medical_type":"","medical_common_name":"复方克霉唑乳膏", "medical_sale_name":"", "medical_code":"A01AB18", "specification":"","usage_num":"", "unit":"", "usage_times":"","created_date":"2014-01-23 20:56:44", "last_modified_date":"2014-01-23 20:56:44", "etl_date":"2014-01-23 20:56:44"}
         ]]>
    </property>

    <!--
     导入体检信息
-->
    <property name="bulkImportExamData" trim="false">
        <![CDATA[
            { "index": { "parent": "1" }}
            { "party_id":"1", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"高血压","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "2" }}
            { "party_id":"2", "hospital":"", "dept":"", "is_ok":"Y", "exam_result":"轻度脂肪肝","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "3" }}
            { "party_id":"3", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"急性细菌性痢疾","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "4" }}
            { "party_id":"4", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"感冒","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "5" }}
            { "party_id":"5", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"感冒","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "6" }}
            { "party_id":"6", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"感冒","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "7" }}
            { "party_id":"7", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"颈椎病","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "8" }}
            { "party_id":"1", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"颈椎病","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

            { "index": { "parent": "9" }}
                { "party_id":"9", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"颈椎病","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }

                { "index": { "parent": "10" }}
                { "party_id":"10", "hospital":"", "dept":"", "is_ok":"N", "exam_result":"颈椎病","fld1":"158", "fld2":"63", "fld3":"94", "fld4":"85", "fld5":"131", "fld901":"89", "fld6":"4.9","fld902":"4.8","fld14":"78", "fld21":"78", "fld23":"", "fld24":"5.5", "fld65":"5.5", "fld66":"1.025","fld67":"", "fld68":"","created_date":"2014-03-18 00:00:00", "last_modified_date":"2014-03-18 00:00:00", "etl_date":"2014-03-18 00:00:00" }
        ]]>
    </property>
```

通过bboss提供的通用api，导入上面定义的数据：

```
	/**
	 * 通过读取配置文件中的dsl json数据导入医疗数据
	 */
	public void importClientInfoFromJsonData(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/Client_Info.xml");

		clientUtil.executeHttp("client_info/basic/_bulk?refresh","bulkImportBasicData",ClientUtil.HTTP_POST);
		clientUtil.executeHttp("client_info/diagnosis/_bulk?refresh","bulkImportDiagnosisData",ClientUtil.HTTP_POST);
		clientUtil.executeHttp("client_info/medical/_bulk?refresh","bulkImportMedicalData",ClientUtil.HTTP_POST);
		clientUtil.executeHttp("client_info/exam/_bulk?refresh","bulkImportExamData",ClientUtil.HTTP_POST);
```

## 统计导入的数据

```
		long basiccount = clientUtil.countAll("client_info/basic");
		System.out.println(basiccount);
		long medicalcount = clientUtil.countAll("client_info/medical");
		System.out.println(medicalcount);
		long examcount = clientUtil.countAll("client_info/exam");
		System.out.println(examcount);
		long diagnosiscount = clientUtil.countAll("client_info/diagnosis");
		System.out.println(diagnosiscount);
	}
```

# 5.父子关系查询

Elasticsearch 5.x 中has_child和has_parent查询的基本用法

- ## **根据父查子-通过客户名称信息查询客户端体检结果**

在配置文件esmapper/Client_Info.xml增加dsl语句：queryExamSearchByClientName

```
   <!--根据客户名称查询客户体检报告-->
    <property name="queryExamSearchByClientName">
        <![CDATA[
            {
              ## 最多返回size变量对应的记录条数
              "size":#[size], 
              "query": {
                
                "has_parent": {
                  "type": "basic",
                  "query": {
                    "match": {
                      "client_name": #[clientName] ## 通过变量clientName设置客户名称
                    }
                  }
                }
              }
            }
        ]]>
    </property>
```

 执行查询，通过bboss的searchList 方法获取符合条件的体检报告以及总记录数据，返回size对应的1000条数据

```
	/**
	 * 根据客户名称查询客户体检报告
	 */
	public void queryExamSearchByClientName(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/Client_info.xml");
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("clientName","张三");
		params.put("size",1000);
		ESDatas<Exam> exams = clientUtil.searchList("client_info/exam/_search","queryExamSearchByClientName",params,Exam.class);
		List<Exam> examList = exams.getDatas();//获取符合条件的体检数据
		long totalSize = exams.getTotalSize();//符合条件的总记录数据
	}
```

 

- ## **根据子查父数据**

  通过医疗信息编码查找客户基本数据

在配置文件esmapper/Client_Info.xml增加查询dsl语句：queryClientInfoByMedicalName

```
    <!--通过医疗信息编码查找客户基本数据-->
    <property name="queryClientInfoByMedicalName">
        <![CDATA[
            {
                ## 最多返回size变量对应的记录条数
                "size":#[size],            
              "query": {

                "has_child": {
                  "type":       "medical",
                  "score_mode": "max",
                  "query": {
                    "match": {
                      "medical_code": #[medicalCode] ## 通过变量medicalCode设置医疗编码
                    }
                  }
                }
              }
            }
        ]]>
    </property>
```

执行查询，通过bboss的searchList 方法获取符合条件的客户端基本信息以及总记录数据

```
	/**
	 * 通过医疗信息编码查找客户基本数据
	 */
	public void queryClientInfoByMedicalName(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/Client_info.xml");
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("medicalCode","A01AA01"); //通过变量medicalCode设置医疗编码
		params.put("size",1000); //最多返回size变量对应的记录条数
		ESDatas<Basic> bascis = clientUtil.searchList("client_info/basic/_search","queryClientInfoByMedicalName",params,Basic.class);
		List<Basic> bascisList = bascis.getDatas();//获取符合条件的客户信息
		long totalSize = bascis.getTotalSize();
	}
```

5.同时返回父子数据-Elasticsearch 5.x 中如何在检索中同时返回父子数据

这一节中我们介绍同时返回父子数据的玩法 ：inner_hits的妙用

- ## **根据父条件查询所有子数据集合并返回父数据，根据客户名称查询所有体检数据，同时返回客户信息**

在配置文件esmapper/Client_Info.xml增加检索dsl-queryDiagnosisByClientName

```
    <!--根据客户名称获取客户体检诊断数据，并返回客户信息-->
    <property name="queryDiagnosisByClientName">
        <![CDATA[
            {
            ## 最多返回size变量对应的记录条数
                            "size":#[size],
              "query": {
                
                "has_parent": {
                  "type": "basic",
                  "query": {
                    "match": {
                      "client_name": #[clientName] ## 通过变量clientName设置客户名称
                    }
                  },
                  "inner_hits": {}  ## 通过变量inner_hits表示要返回对应的客户信息
                }
              }
            }
        ]]>
    </property>
```

执行检索并遍历结果

```
	/**
	 * 根据客户名称获取客户体检诊断数据，并返回客户数据
	 */
	public void queryDiagnosisByClientName(){

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/Client_info.xml");
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("clientName","张三");
		params.put("size",1000);

		try {
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences(Basic.class);//指定inner查询结果对应的客户基本信息类型,Basic只有一个文档类型，索引不需要显示指定basic对应的mapping type名称
			ESDatas<Diagnosis> diagnosiss = clientUtil.searchList("client_info/diagnosis/_search",
					"queryDiagnosisByClientName",params,Diagnosis.class);
			List<Diagnosis> diagnosisList = diagnosiss.getDatas();//获取符合条件的体检报告数据
			long totalSize = diagnosiss.getTotalSize();
			//遍历诊断报告信息，并查看报告对应的客户基本信息
			for(int i = 0;  diagnosisList != null && i < diagnosisList.size(); i ++) {
				Diagnosis diagnosis = diagnosisList.get(i);
				List<Basic> basics = ResultUtil.getInnerHits(diagnosis.getInnerHits(), "basic");
				if(basics != null) {
					System.out.println(basics.size());
				}
			}
		}
		finally{
			ESInnerHitSerialThreadLocal.clean();//清空inner查询结果对应的客户基本信息类型
		}
	}
```

- ##  **根据子条件查询父数据并返回符合条件的父的子数据集合，查询客户信息，同时返回客户对应的所有体检报告、医疗记录、诊断记录**

在配置文件esmapper/Client_Info.xml增加检索dsl-queryClientAndAllSons

```
    <!--查询客户信息，同时返回客户对应的所有体检报告、医疗记录、诊断记录-->
    <property name="queryClientAndAllSons">
        <![CDATA[
        {
          "query": {
            "bool": {
              "should": [
                {
                    "match_all":{}
                }
              ]
              ,"must": [
                {
                  "has_child": {
                    "score_mode": "none",
                    "type": "diagnosis"
                    ,"query": {
                      "bool": {
                        "must": [
                          {
                            "term": {
                              "icd10_code": {
                                "value": "J00"
                              }
                            }
                          }
                        ]
                      }
                    },"inner_hits":{}
                  }
                }
                ]
              ,"should": [
                  {
                  "has_child": {
                    "score_mode": "none",
                    "type": "medical"
                    ,"query": {
                      "match_all": {}

                    },"inner_hits":{}
                  }
                }
              ]
              ,"should": [
                {
                  "has_child": {
                    "type": "exam",
                    "query": {
                      "match_all": {}
                    },"inner_hits":{}
                  }
                }
              ]
            }
          }
        }
        ]]>
    </property>
```

执行查询：

```
	/**
	 * 查询客户信息，同时返回客户对应的所有体检报告、医疗记录、诊断记录
	 */
	public void queryClientAndAllSons(){
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/Client_Info.xml");
		Map<String,Object> params = null;//没有检索条件，构造一个空的参数对象

		try {
			//设置子文档的类型和对象映射关系
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences("exam",Exam.class);//指定inner查询结果对于exam类型和对应的对象类型Exam
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences("diagnosis",Diagnosis.class);//指定inner查询结果对于diagnosis类型和对应的对象类型Diagnosis
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences("medical",Medical.class);//指定inner查询结果对于medical类型和对应的对象类型Medical
			ESDatas<Basic> escompanys = clientUtil.searchList("client_info/basic/_search",
					"queryClientAndAllSons",params,Basic.class);
			//String response = clientUtil.executeRequest("client_info/basic/_search","queryClientAndAllSons",params);直接获取原始的json报文
//			escompanys = clientUtil.searchAll("client_info",Basic.class);
			long totalSize = escompanys.getTotalSize();
			List<Basic> clientInfos = escompanys.getDatas();//获取符合条件的数据
			//查看公司下面的雇员信息（符合检索条件的雇员信息）
			for (int i = 0; clientInfos != null && i < clientInfos.size(); i++) {
				Basic clientInfo = clientInfos.get(i);
				List<Exam> exams = ResultUtil.getInnerHits(clientInfo.getInnerHits(), "exam");
				if(exams != null)
					System.out.println(exams.size());
				List<Diagnosis> diagnosiss = ResultUtil.getInnerHits(clientInfo.getInnerHits(), "diagnosis");
				if(diagnosiss != null)
					System.out.println(diagnosiss.size());
				List<Medical> medicals = ResultUtil.getInnerHits(clientInfo.getInnerHits(), "medical");
				if(medicals != null)
					System.out.println(medicals.size());

			}
		}
		finally{
			ESInnerHitSerialThreadLocal.clean();//清空inner查询结果对于各种类型信息
		}
	}
```

最后我们按顺序执行所有方法，验证功能：

```
	@Test
	public void testMutil(){
		this.createClientIndice();//创建indice client_info
//		this.importClientInfoDataFromBeans(); //通过api添加测试数据
		this.importClientInfoFromJsonData();//导入测试数据
		this.queryExamSearchByClientName(); //根据客户端名称查询提交报告
		this.queryClientInfoByMedicalName();//通过医疗信息编码查找客户基本数据
		this.queryDiagnosisByClientName();//根据客户名称获取客户体检诊断数据，并返回客户数据
		this.queryClientAndAllSons();//查询客户信息，同时返回客户对应的所有体检报告、医疗记录、诊断记录
	}
```

可以下载完整的demo工程运行本文中的测试用例方法，地址见相关资料。

到此Elasticsearch 5.x 父子关系维护检索实战介绍完毕，谢谢大家！

# 6.相关资料

完整demo工程  

https://github.com/bbossgroups/eshelloword-booter

对应的类文件和配置文件

org.bboss.elasticsearchtest.parentchild.ParentChildTest

esmapper/Client_Info.xml

 

# 开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />


交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">




