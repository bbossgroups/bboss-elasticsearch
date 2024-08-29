# ElasticSearch DSL Script使用案例分享

<img src="images/qrcode.jpg" alt="bboss" style="zoom:10%;" />

 

**The best elasticsearch highlevel java rest api-----bboss**       

ElasticSearch DSL Script使用案例分享，涉及到的功能点：

- 脚本片段使用
- 多行文本使用
- 添加属性字段



# 前言

先看看elasticsearch官方的script dsl块文本的写法：通过一对"""   """来包含块文本

```json
{
  "query": {
    "function_score": {
      "script_score": {
        "script": {
          "lang": "painless",
          "source": """
            int total = 0;
            for (int i = 0; i < doc['goals'].length; ++i) {
              total += doc['goals'][i];
            }
            return total;
          """
        }
      }
    }
  }
}
```

对应的bboss script dsl块文本的写法：通过一对@"""   """来包含块文本

```json
{
  "query": {
    "function_score": {
      "script_score": {
        "script": {
          "lang": "painless",
          "source": @"""
            int total = 0;
            for (int i = 0; i < doc['goals'].length; ++i) {
              total += doc['goals'][i];
            }
            return total;
          """
        }
      }
    }
  }
}
```

bboss中管理的dsl块文本和elasticsearch官方的dsl中的块文本唯一的区别就是在开头的"""前面加了个@符号



# 1.定义dsl配置

在demo.xml文件中增加以下配置

```xml
    <property name="scriptPianduan">
        <![CDATA[
            "params": {
              "last": #[last],
              "nick": #[nick]
            }
        ]]>
    </property>
    <property name="scriptDsl">
        <![CDATA[{
          "script": {
            "lang": "painless",
            "source": @"""  ##块文本开始
              ctx._source.last = params.last;
              ctx._source.nick = params.nick
            """,##块文本结束
            @{scriptPianduan}
          }
        }]]>
    </property>
```



# 2.执行脚本处理

定义类ScriptImpl,增加方法updateDocumentByScriptPath来执行脚本

```java
package org.bboss.elasticsearchtest.script;/*
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

import org.bboss.elasticsearchtest.crud.DocumentCRUD;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;

import java.util.HashMap;
import java.util.Map;

public class ScriptImpl {
	private String mappath = "esmapper/demo.xml";

	public void updateDocumentByScriptPath(){
		//初始化数据，会创建type为demo的indice demo，并添加docid为2的文档
		DocumentCRUD documentCRUD = new DocumentCRUD();
		documentCRUD.testCreateIndice();
		documentCRUD.testBulkAddDocument();
		//创建加载配置文件的客户端工具，用来检索文档，单实例多线程安全
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(mappath);
		Map<String,Object> params = new HashMap<String,Object>();
		//为id为2的文档增加last和nick两个属性
		params.put("last","gaudreau");
		params.put("nick","hockey");
        //通过script脚本为文档id为2的文档增加last和nick两个属性，为了演示效果强制refresh，实际环境慎用
		//clientUtil.updateByPath("demo/demo/2/_update?refresh","scriptDsl",params);//Elasticsearch 6及以下版本需要设置索引类型
        clientUtil.updateByPath("demo/2/_update?refresh","scriptDsl",params);
        //获取更新后的文档，会看到新加的2个字段属性
		//String doc = clientUtil.getDocument("demo","demo","2");//Elasticsearch 6及以下版本需要设置索引类型
        String doc = clientUtil.getDocument("demo","2");
		System.out.println(doc);

	}
}
```



# 3.一个比较复杂Script的案例

```xml
<property name="updateStoreProductDynamicTemplate">
        <![CDATA[
        {
          "query": {
            "bool": {
              "must": [
                {
                  "term": {
                    "_id": #[id]
                  }
                }
              ]
            }
          },
          "script": {
            "lang": "painless",
            "source": @"""
                ctx._source.is_expired_dynamic_price=params.is_expired_dynamic_price;
                ctx._source.dynamic_price_template_id=params.dynamic_price_template_id;
                ctx._source.dynamic_price_template.id=params.dynamic_price_template_id;
                ctx._source.dynamic_price_template.code=params.dynamic_price_template_code;
                ctx._source.dynamic_price_template.name=params.dynamic_price_template.name;
                ctx._source.dynamic_price_template.count_products=params.dynamic_price_template.count_products;
                ctx._source.dynamic_price_template.is_deleted=params.dynamic_price_template.is_deleted;
                ctx._source.dynamic_price_template.user_id_update=params.dynamic_price_template.user_id_update;
                ctx._source.dynamic_price_template.rules=params.ctx._source.dynamic_price_template.rules;
                """,
            "params": {
              "is_expired_dynamic_price": #[isExpiredDynamicPrice],
              "dynamic_price_template_id": #[dynamicPriceTemplateId],
              "dynamic_price_template_code":#[dynamicPriceTemplate->code],
              "dynamic_price_template.name":#[dynamicPriceTemplate->name],
              "dynamic_price_template.count_products":#[dynamicPriceTemplate->countProducts],
              "dynamic_price_template.is_deleted":#[dynamicPriceTemplate->isDeleted],
              "dynamic_price_template.user_id_update":#[dynamicPriceTemplate->userIdUpdate],
              "dynamic_price_template.rules":
              [
                   #foreach($rule in $dynamicPriceTemplate.rules)
                       #if($velocityCount > 0),#end
                   {
                        "id": #[dynamicPriceTemplate->rules[$velocityCount].id],
                        "sort_num": #[dynamicPriceTemplate->rules[$velocityCount]->sortNum],
                        "act_expired_time_tick": #[dynamicPriceTemplate->rules[$velocityCount]->actExpiredTimeTick],
                        "act_expired_time_value": #[dynamicPriceTemplate->rules[$velocityCount]->actExpiredTimeValue],
                        "act_expired_time_unit": #[dynamicPriceTemplate->rules[$velocityCount]->actExpiredTimeUnit],
                        "price_new_type": #[dynamicPriceTemplate->rules[$velocityCount]->priceNewType],
                        "price_new_coefficient": #[dynamicPriceTemplate->rules[$velocityCount]->priceNewCoefficient],
                        "price_new_custom": #[dynamicPriceTemplate->rules[$velocityCount]->priceNewCustom],
                        "auto_set_commend": #[dynamicPriceTemplate->rules[$velocityCount].autoSetCommend],
                        "auto_set_tag": #[dynamicPriceTemplate->rules[$velocityCount]->autoSetTag]
                    }
                   #end
              ]
            }
          }
        }
        ]]>
    </property>
```



# 4 参考资料

https://www.elastic.co/guide/en/elasticsearch/painless/current/index.html

# 开发交流
QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />



交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">





