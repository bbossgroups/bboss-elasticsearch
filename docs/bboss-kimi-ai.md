PS：kimi是为数不多的，支持api方式调用的大模型，官方使用地址：

https://kimi.moonshot.cn/

本文通过一个简单的案例，介绍采用java微服务框架bboss httpproxy来调用MoonShot AI提供的大模型Kimi服务，实现自己的大模型gpt功能。

[bboss httpproxy](https://esdoc.bbossgroups.com/#/httpproxy)

[MoonShot AI](https://kimi.moonshot.cn/)

## 1.前言

了解kimi服务规范，通过bboss调用Moonshot AI的API来集成其大模型的功能到Java应用程序中，实现自己的智能chat功能。

通过api方式来调用，调用文档参考：
https://platform.moonshot.cn/docs；

1. 先注册一个账号 （可以手机号注册即可）
2. 增加一个api key，一个api key 可以理解成一个应用，外部调用的时候要用到这个api key，如下：

[![MoonShot大模型Kimi的使用及api调用评测](https://p3-sign.toutiaoimg.com/tos-cn-i-6w9my0ksvp/ca537dc668824afa8100b75d0230e757~noop.image?_iz=58558&from=article.pc_detail&lk3s=953192f4&x-expires=1709625074&x-signature=RRGeygYPm%2FK6KOcTEwJfTYdHr%2BY%3D)](https://p3-sign.toutiaoimg.com/tos-cn-i-6w9my0ksvp/ca537dc668824afa8100b75d0230e757~noop.image?_iz=58558&from=article.pc_detail&lk3s=953192f4&x-expires=1709625074&x-signature=RRGeygYPm%2FK6KOcTEwJfTYdHr%2BY%3D)

充值 tokens，kimi默认注册会给15元的额度，超过额度，可以跟小二联系充值；收费价格还可以接受，也是模型里面比较便宜的；

[![MoonShot大模型Kimi的使用及api调用评测](https://p3-sign.toutiaoimg.com/tos-cn-i-6w9my0ksvp/2b82bdb1c1774bb789acb24d8264be5d~noop.image?_iz=58558&from=article.pc_detail&lk3s=953192f4&x-expires=1709625074&x-signature=gC7SDy18X1MqOPwx2BxWChPvIcY%3D)](https://p3-sign.toutiaoimg.com/tos-cn-i-6w9my0ksvp/2b82bdb1c1774bb789acb24d8264be5d~noop.image?_iz=58558&from=article.pc_detail&lk3s=953192f4&x-expires=1709625074&x-signature=gC7SDy18X1MqOPwx2BxWChPvIcY%3D)

| 模型             | 计费单位    | 价格    |
| ---------------- | ----------- | ------- |
| moonshot-v1-8k   | 1000 tokens | 0.012元 |
| moonshot-v1-32k  | 1000 tokens | 0.024元 |
| moonshot-v1-128k | 1000 tokens | 0.06元  |

以下是一个简化的步骤说明，Java开发者如何使用Moonshot AI的API：

1. 注册并获取API Key：开发者需要访问Moonshot AI的开放平台（platform.moonshot.cn），创建账户并获取API Key。
2. 阅读API文档：了解Moonshot AI提供的API接口，包括如何发送请求、接收响应以及可能的参数和限制。
3. 在Java中编写代码：使用Java的网络编程库bboss httpproxy来发送HTTP请求到Moonshot AI的API。这通常涉及到构建请求体、设置请求头（包括API Key）以及处理响应。
4. 处理响应：API响应通常是JSON格式的数据，Java开发者可以使用bboss httpproxy提供的rpc服务方法自动解析这些数据，并将其整合到Java应用程序中。
5. 测试和调试：在集成过程中，开发者需要不断测试和调试以确保API调用正确无误，并且集成的功能符合预期。
6. 部署和维护：一旦集成测试完成，开发者可以将应用程序部署到生产环境，并根据需要进行维护和更新。

## 2.大模型服务调用

下载bboss微服务框架集成开发环境：gradle工程

https://gitee.com/bboss/httpproxy-apollo

在工程中增加一个MoonshotAI单元测试用例

```java
public class MoonshotAI {
    private static Logger logger = LoggerFactory.getLogger(StartHttpPoolFromFile.class);
    @Before
    public void test(){
        //启动连接池
        HttpRequestUtil.startHttpPools("application-ai.properties");
    }
    @Test
    public void testSendJsonBody(){


        /**
         * {
         *     "id": "cmpl-afcdbbecc9764d0d92a39d97ab89f6c2",
         *     "object": "chat.completion",
         *     "created": 3972399,
         *     "model": "moonshot-v1-8k",
         *     "choices": [
         *         {
         *             "index": 0,
         *             "message": {
         *                 "role": "assistant",
         *                 "content": " BBoss是一个开源的Java企业应用开发框架，它提供了丰富的功能，包括数据采集、数据处理、数据展示等。在数据采集方面，BBoss支持从多种数据源采集数据，包括数据库、Excel文件等。\n\n如果你想使用BBoss框架来采集Excel文件中的数据，你可能需要使用BBoss提供的Excel插件或者相关的API。这通常涉及到以下几个步骤：\n\n1. **添加依赖**：首先，你需要在你的项目中添加BBoss Excel插件的依赖。这通常通过在项目的`pom.xml`文件中添加相应的依赖项来实现。\n\n2. **读取Excel**：使用BBoss提供的API来读取Excel文件。这可能涉及到创建一个Excel文件的读取器，然后指定要读取的文件路径。\n\n3. **处理数据**：读取Excel文件后，你可以遍历文件中的数据，进行你需要的处理。BBoss可能提供了一些工具类来帮助你解析和处理Excel文件中的数据。\n\n4. **存储数据**：处理完数据后，你可以将数据存储到数据库或其他数据存储系统中。\n\n请注意，具体的API使用方法和步骤可能会随着BBoss版本的更新而有所变化。为了获取最准确的信息，建议查阅BBoss的官方文档或者查看相关的示例代码。如果你有具体的代码问题或者需要进一  步的帮助，可以提供更多的信息，我会尽力为你提供帮助。"
         *             },
         *             "finish_reason": "stop"
         *         }
         *     ],
         *     "usage": {
         *         "prompt_tokens": 88,
         *         "completion_tokens": 275,
         *         "total_tokens": 363
         *     }
         * }
         */

        Map<String,Object> headers = new LinkedHashMap<String,Object>();
        headers.put("Authorization","Bearer sk-NmYophVVNhAyq7Op25UNMKxQj3O1ht6i0hT8IZJnthNsnis2");
        String res = HttpRequestProxy.sendJsonBody("{\n" +
                "     \"model\": \"moonshot-v1-8k\",\n" +
                "     \"messages\": [\n" +
                "        {\"role\": \"system\", \"content\": \"你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一些涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。\"},\n" +
                "        {\"role\": \"user\", \"content\": \"https://esdoc.bbossgroups.com/#/filelog-guide\"},\n" +
                "        {\"role\": \"user\", \"content\": \"bboss采集excel\"}\n" +
                "     ],\n" +
                "     \"temperature\": 0.3\n" +
                "     ,\n" +
                "     \"stream\": false\n" +
                "   }","v1/chat/completions",headers);
        logger.info(res);
        



    }

    @Test
    public void testStream(){



        Map<String,Object> headers = new LinkedHashMap<String,Object>();
        headers.put("Authorization","Bearer sk-NmYophVVNhAyq7Op25UNMKxQj3O1ht6i0hT8IZJnthNsnis2");
        String res = HttpRequestProxy.sendJsonBody("{\n" +
                "     \"model\": \"moonshot-v1-8k\",\n" +
                "     \"messages\": [\n" +
                "        {\"role\": \"system\", \"content\": \"你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一些涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。\"},\n" +
                "        {\"role\": \"user\", \"content\": \"https://esdoc.bbossgroups.com/#/filelog-guide\"},\n" +
                "        {\"role\": \"user\", \"content\": \"bboss采集excel\"}\n" +
                "     ],\n" +
                "     \"temperature\": 0.3\n" +
                "     ,\n" +
                "     \"stream\": true\n" +
                "   }","v1/chat/completions",headers);
        logger.info(res);




    }

    @Test
    public void testListModel(){


        /**
         * {
         *     "data": [
         *         {
         *             "created": 1709149142,
         *             "id": "moonshot-v1-128k",
         *             "object": "model",
         *             "owned_by": "moonshot",
         *             "permission": [
         *                 {
         *                     "created": 0,
         *                     "id": "",
         *                     "object": "",
         *                     "allow_create_engine": false,
         *                     "allow_sampling": false,
         *                     "allow_logprobs": false,
         *                     "allow_search_indices": false,
         *                     "allow_view": false,
         *                     "allow_fine_tuning": false,
         *                     "organization": "public",
         *                     "group": "public",
         *                     "is_blocking": false
         *                 }
         *             ],
         *             "root": "",
         *             "parent": ""
         *         },
         *         {
         *             "created": 1709149142,
         *             "id": "moonshot-v1-8k",
         *             "object": "model",
         *             "owned_by": "moonshot",
         *             "permission": [
         *                 {
         *                     "created": 0,
         *                     "id": "",
         *                     "object": "",
         *                     "allow_create_engine": false,
         *                     "allow_sampling": false,
         *                     "allow_logprobs": false,
         *                     "allow_search_indices": false,
         *                     "allow_view": false,
         *                     "allow_fine_tuning": false,
         *                     "organization": "public",
         *                     "group": "public",
         *                     "is_blocking": false
         *                 }
         *             ],
         *             "root": "",
         *             "parent": ""
         *         },
         *         {
         *             "created": 1709149142,
         *             "id": "moonshot-v1-32k",
         *             "object": "model",
         *             "owned_by": "moonshot",
         *             "permission": [
         *                 {
         *                     "created": 0,
         *                     "id": "",
         *                     "object": "",
         *                     "allow_create_engine": false,
         *                     "allow_sampling": false,
         *                     "allow_logprobs": false,
         *                     "allow_search_indices": false,
         *                     "allow_view": false,
         *                     "allow_fine_tuning": false,
         *                     "organization": "public",
         *                     "group": "public",
         *                     "is_blocking": false
         *                 }
         *             ],
         *             "root": "",
         *             "parent": ""
         *         }
         *     ]
         * }
         */
        Map<String,Object> headers = new LinkedHashMap<String,Object>();
        headers.put("Authorization","Bearer sk-NmYophVVNhAyq7Op25UNMKxQj3O1ht6i0hT8IZJnthNsnis2");
        String res = HttpRequestProxy.httpGetforString("/v1/models",headers);
        logger.info(res);




    }
}
```



