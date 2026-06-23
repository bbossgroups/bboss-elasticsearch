# 多模态AI智能体开发框架使用教程

## 一、概述

bboss AI 是一个轻量级多模态 Java 大模型智能体客户端，基于 HttpClient5、HttpCore5 以及 Reactor 构建。它支持同步和流式两种调用模式，能够快速集成各大主流 AI 模型平台，实现智能问答、图片识别/生成、语音识别/生成、视频识别/生成等功能。

同时可以通过编写自定义的适配器，快速适配和对接遵循openai规范的本地私有Maas平台。

![](images\bboss-ai.png)

### 核心特性

- **多模型支持**：兼容 DeepSeek、Kimi、智谱、阿里百炼通义千问、字节豆包火山引擎、MiniMax、腾讯混元、中国移动九天等主流 MaaS 平台；通过简单的适配和扩展即可支持私有化模型平台

- **工具能力**：支持工具调用和 MCP 服务发现，提供 MCP SSE 和 Streamable 两种通讯协议，同时提供mcp server协议实现

- **多模态支持**：支持文本、图片、音频、视频等多种模态的识别与生成

- **流式响应**：基于 Reactor 的响应式编程模型，支持背压控制

- **多轮会话**：内置会话记忆管理，支持多轮对话；支持内存和数据库两种会话持久化方式

- **智能体工作流编排**：基于有向循环图实现多智能体协同工作流，支持串行、并行、路由、条件分支、裁判评估等丰富的流程编排能力，快速构建复杂多智能体系统

  ![](images\workflow\jobworkflow.png)

  

  ![](images\workflow\bbossgraph.png)

  多智能体编排工作流底层基于[bboss jobflow](https://esdoc.bbossgroups.com/#/jobworkflow)实现（一套数据交换作业编排工作流）

- **向量模型支持**：内置文本向量化（Embedding）和重排序（Rerank）能力，支持知识库检索增强生成（RAG）

- **多智能体协同**：配合bboss graph提供的工作流和有限循环图，实现多智能体协同，快速构建多智能体系统

---

### 项目源码

bboss ai 源码地址：

码云    https://gitee.com/bboss/bboss-ai

github  https://github.com/bbossgroups/bboss-ai

源码构建参考文档：https://esdoc.bbossgroups.com/#/bboss-build

demo工程：https://gitee.com/bboss/bbootdemo

## 二、环境准备

### 2.1 Maven 依赖

```xml
<dependency>
    <groupId>com.bbossgroups</groupId>
    <artifactId>bboss-ai-flow</artifactId>
    <version>6.5.3</version>
</dependency>
```

### 2.2 Gradle 依赖

```groovy
implementation 'com.bbossgroups:bboss-ai-flow:6.5.3'
```

### 2.3 maas服务配置

创建配置文件 `application-stream.properties`，配置各模型服务：使用时，需将apiKey替换为实际apiKey

```properties
http.poolNames = deepseek,qwenvlplus,volcengine,kimi,zhipu,minimax,hunyuan

## DeepSeek 配置
deepseek.http.maxTotal = 200
deepseek.http.defaultMaxPerRoute = 200
deepseek.http.hosts=https://api.deepseek.com
deepseek.http.apiKeyId = sk-xxxxx
deepseek.http.modelType = deepseek

## 阿里百炼通义千问配置
qwenvlplus.http.maxTotal = 200
qwenvlplus.http.defaultMaxPerRoute = 200
qwenvlplus.http.hosts=https://dashscope.aliyuncs.com
qwenvlplus.http.apiKeyId = sk-xxxxx
qwenvlplus.http.modelType = qwen

## 火山引擎豆包配置
volcengine.http.maxTotal = 200
volcengine.http.defaultMaxPerRoute = 200
volcengine.http.hosts=https://ark.cn-beijing.volces.com
volcengine.http.apiKeyId = xxxxx
volcengine.http.modelType = doubao

## Kimi 配置
kimi.http.maxTotal = 200
kimi.http.defaultMaxPerRoute = 200
kimi.http.hosts=https://api.moonshot.cn
kimi.http.apiKeyId = sk-xxxxx
kimi.http.modelType = kimi

## 智谱配置
zhipu.http.maxTotal = 200
zhipu.http.defaultMaxPerRoute = 200
zhipu.http.hosts=https://open.bigmodel.cn
zhipu.http.apiKeyId = xxxxx

## MiniMax 配置
minimax.http.maxTotal = 200
minimax.http.defaultMaxPerRoute = 200
minimax.http.hosts=https://api.minimaxi.com
minimax.http.apiKeyId = sk-xxxxx
minimax.http.modelType = minimax

## 腾讯混元配置
hunyuan.http.maxTotal = 200
hunyuan.http.defaultMaxPerRoute = 200
hunyuan.http.hosts=https://api.hunyuan.cloud.tencent.com
hunyuan.http.apiKeyId = sk-xxxxx
```

### 2.4 MCP 服务配置

如需使用 MCP 工具服务，配置 `mcpserver.properties`：使用时，需将apiKey替换为实际apiKey

```properties
http.poolNames = aliyun,gaotie,amap,WebParser,visualops,12306,shuqi,feishumcp
#分析文档，编写一篇简洁实用的bboss ai实用教程
##aliyun mcp模型服务配置：在代码中引用服务的名称为aliyun
# 服务连接池参数
aliyun.http.maxTotal = 200
aliyun.http.defaultMaxPerRoute = 200
# aliyun模型服务地址
aliyun.http.hosts=https://dashscope.aliyuncs.com
#基于apiKeyId认证配置（主要用于各种大模型服务对接认证）
aliyun.http.apiKeyId = sk-469f01dbb572310d6d6db4fb2f4
# sse端点：高德地图用户key申请地址，https://console.amap.com/dev/key/app
aliyun.http.extendConfigs.sseendpoint = /api/v1/mcps/amap-maps/sse?key=793b8ba3f45a22fa74cba764a8

##WebParser mcp模型服务配置：在代码中引用服务的名称为WebParser
# 服务连接池参数
WebParser.http.maxTotal = 200
WebParser.http.defaultMaxPerRoute = 200
# WebParser模型服务地址
WebParser.http.hosts=https://dashscope.aliyuncs.com
#基于apiKeyId认证配置（主要用于各种大模型服务对接认证）
WebParser.http.apiKeyId = sk-469f01e6db310d6d6db4fb2f4
# sse端点：高德地图用户key申请地址，https://console.amap.com/dev/key/app
WebParser.http.extendConfigs.sseendpoint = /api/v1/mcps/WebParser/sse


##gaode mcp模型服务配置：在代码中引用服务的名称为amap
# 服务连接池参数
amap.http.maxTotal = 200
amap.http.defaultMaxPerRoute = 200
# amap模型服务地址
amap.http.hosts=https://mcp.amap.com
#基于apiKeyId认证配置（主要用于各种大模型服务对接认证）
#amap.http.apiKeyId = sk-469f6db310d6d6db4fb2f4
# sse端点：高德地图用户key申请地址，https://console.amap.com/dev/key/app
amap.http.extendConfigs.sseendpoint = /sse?key=793b8b9bab15a22fa74cba764a8


##gaotie mcp模型服务配置：在代码中引用服务的名称为gaotie
# 服务连接池参数
gaotie.http.maxTotal = 200
gaotie.http.defaultMaxPerRoute = 200
#  gaotie模型服务地址
gaotie.http.hosts=127.0.0.1:8000
gaotie.http.extendConfigs.sseendpoint = /sse

##12306 mcp模型服务配置：在代码中引用服务的名称为12306
# 服务连接池参数
12306.http.maxTotal = 200
12306.http.defaultMaxPerRoute = 200
# 12306服务地址
12306.http.hosts=https://dashscope.aliyuncs.com
12306.http.apiKeyId = sk-469f01db6db310d6d6db4fb2f4
12306.http.extendConfigs.sseendpoint = /api/v1/mcps/china-railway/sse

##shuqi mcp模型服务配置：在代码中引用服务的名称为shuqi
# 服务连接池参数
shuqi.http.maxTotal = 200
shuqi.http.defaultMaxPerRoute = 200
# shuqi服务地址
shuqi.http.hosts=https://dashscope.aliyuncs.com
shuqi.http.apiKeyId = sk-469f01dbe6db310d6d6db4fb2f4
shuqi.http.extendConfigs.streamableendpoint = /api/v1/mcps/market-cmapi00072981/mcp
#shuqi.http.httpResponseInterceptors=org.frameworkset.spi.ai.HttpResponseInterceptorDemo

##visualops mcp模型服务配置：在代码中引用服务的名称为visualops
# 服务连接池参数
visualops.http.maxTotal = 200
visualops.http.defaultMaxPerRoute = 200
# visualops服务地址

visualops.http.hosts=127.0.0.1:8080
visualops.http.apiKeyId = 17689048891086XsDsJVgwiQcmKhOdh23DX4NT
#visualops.http.extendConfigs.sseendpoint = /mcp/sse.api
visualops.http.extendConfigs.streamableendpoint = /mcp/streamable.api

##feishumcp mcp模型服务配置：在代码中引用服务的名称为feishumcp
# 服务连接池参数
feishumcp.http.maxTotal = 200
feishumcp.http.defaultMaxPerRoute = 200
# feishumcp服务地址
feishumcp.http.hosts=https://mcp.feishu.cn
feishumcp.http.extendConfigs.streamableendpoint = /mcp



```

---

## 三、基础使用

### 3.1 智能体消息类型

#### 3.1.1 编码定义

消息类型messageType，对应agent_session_message表中的messageType字段

```java
/**
 * 智能体用户输入消息:包括用户输入的原始问题、用户上传文件、用户图片描述等
 */
public static final String MESSAGE_TYPE_USERINPUTMESSAGE = "8";
/**
 * 智能体输出消息:ASSISTANT中的一种
 */
public static final String MESSAGE_TYPE_AGENTRESULTMESSAGE = "1";
/**
 * 智能体用户输入消息：提交给大模型或者其他多模态模型
 */
public static final String MESSAGE_TYPE_USER_MESSAGE = "2";

/**
 * 智能体辅助消息
 */
public static final String MESSAGE_TYPE_ASSISTANT_MESSAGE = "0";


/**
 * 智能体系统消息
 */
public static final String MESSAGE_TYPE_SYSTEM_MESSAGE = "3";



/**
 * 智能体跟踪消息
 */
public static final String MESSAGE_TYPE_TRACE_MESSAGE = "5";

/**
 * 智能体RAG知识消息
 */
public static final String MESSAGE_TYPE_RAG_MESSAGE = "6";

/**
 * 智能体拒答消息
 */
public static final String MESSAGE_TYPE_REFUSE_MESSAGE = "7";
    /**
     * LLM输入消息
     */
    public static final String MESSAGE_TYPE_LLMINPUTMESSAGE = "9";


```

#### 3.1.2 名称定义

消息类型messageType对应的角色名称:

```java

/**
 * 智能体用户输入消息:包括用户输入的原始问题、用户上传文件、用户图片描述等
 */
public static final String MESSAGE_TYPE_USERINPUTMESSAGE_NAME = "userinput";
/**
 * 智能体输出消息:ASSISTANT中的一种
 */
public static final String MESSAGE_TYPE_AGENTRESULTMESSAGE_NAME = "agentresult";
/**
 * 智能体用户输入消息
 */
public static final String MESSAGE_TYPE_USER_MESSAGE_NAME = "user";

/**
 * 智能体辅助消息
 */
public static final String MESSAGE_TYPE_ASSISTANT_MESSAGE_NAME = "assistant";


/**
 * 智能体系统消息
 */
public static final String MESSAGE_TYPE_SYSTEM_MESSAGE_NAME = "system";



/**
 * 智能体跟踪消息
 */
public static final String MESSAGE_TYPE_TRACE_MESSAGE_NAME = "trace";

/**
 * 智能体RAG知识消息
 */
public static final String MESSAGE_TYPE_RAG_MESSAGE_NAME = "rag";

/**
 * 智能体拒答消息
 */
public static final String MESSAGE_TYPE_REFUSE_MESSAGE_NAME = "refuse";
    /**
     * LLM输入消息
     */
    public static final String MESSAGE_TYPE_LLMINPUTMESSAGE_NAME = "llminput";
```

#### 3.1.3 扩展消息类型

AgentMessageTypeConvertor提供以上内置角色名称到消息类型的转换，如果用户需要扩展自己的消息角色和编码，可以继承AgentMessageTypeConvertor类并重写convertMessageType方法，0-100为系统内置编码，如果用户自定义编码请从101开始，示例如下：

```java
public class CustomAgentMessageTypeConvertor extends AgentMessageTypeConvertor {
    /**
     * 将角色转换为消息类型messageType，对应agent_session_message表中的messageType字段    
     *
     * @param role
     * @return
     */
    @Override
    public String convertMessageType(String role) {
        if("custom".equals(role)){
            return "101";
        }
        return super.convertMessageType(role);
    }
}
```

使用自定义转换器：

```java
StoreContext storeContext = new StoreContext();
storeContext.setAgentMessageTypeConvertor(new CustomAgentMessageTypeConvertor());
```

#### 3.1.3 使用场景

消息类型使用场景：在智能体编排流程中设置消息角色类型

```java
    TraceMessage traceMessage = new TraceMessage();       
                //记录用户输入的原始问题
                traceMessage.setMessage(Map.of("question", question,"role",SessionMessage.MESSAGE_TYPE_USERINPUTMESSAGE_NAME));
                //其他用户上传的附件材料信息可以放到metaData中,也可以直接放到上面的消息中

                traceMessage.setStartTime(System.currentTimeMillis());

                planAgent.recordTraceMessage(traceMessage);
```

#### 3.1.4 ServerEvent结构

```java
contentType：主要用来区分答案正文内容和思维链内容，0表示答案内容，1表示思维链内容, 2 表示工具调用，3 表示mcp服务调用，5 表示监控对象，默认值为0

 type 字段：标记消息报文类型 
    /**
     * 数据报文类型:0 数据消息，1表示异常消息,2 trace信息，traceId 3 拒答消息 5 知识库资料消息 6 步骤消息
     * 默认值为0     
     */
type = TYPE_DATA;
数据报文类型常量说明
 /**
     * type：数据消息
     */
    public static final int TYPE_DATA = 0;
    /**
     * type：异常消息
     */
    public static final int TYPE_ERROR = 1;

    /**
     * type：trace信息，traceId
     */
    public static final int TYPE_TRACE = 2;
    /**
     * type：拒绝消息：
     */
    public static final int TYPE_REFUSAL = 3;
    
    /**
     * type：知识库资料消息：
     */
    public static final int TYPE_RAG_KNOWLEDGE = 5;

    /**
     * type：步骤消息：
     */
    public static final int TYPE_STEP = 6;
```

#### 3.1.5 属性说明

| 参数           | 说明             |
| -------------- | ---------------- |
| `agentId`      | 智能体唯一标识   |
| `agentName`    | 智能体名称       |
| `prompt`       | 用户提示词       |
| `systemPrompt` | 系统提示词       |
| `model`        | 模型名称         |
| `maas`         | MaaS平台服务名   |
| `stream`       | 是否开启流式响应 |
| `thinking`     | 是否开启思考过程 |

---

### 

### 3.2 初始化配置

在应用启动时加载配置文件：

```java
import org.frameworkset.spi.remote.http.HttpRequestProxy;

public class Application {
    public void init() {
        // 加载模型服务配置文件
        HttpRequestProxy.startHttpPools("application-stream.properties");
        
        // 加载 MCP 服务配置文件
        HttpRequestProxy.startHttpPools("mcpserver.properties");
    }
}
```

### 3.3 同步聊天（非流式）

```java
import org.frameworkset.spi.ai.AIAgent;
import org.frameworkset.spi.ai.model.ChatAgentMessage;
import org.frameworkset.spi.ai.model.ServerEvent;

public class ChatExample {
    public static void main(String[] args) {
        // 初始化配置
        HttpRequestProxy.startHttpPools("application-stream.properties");
        
        // 创建消息对象
        ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
        chatAgentMessage.setModel("deepseek-chat");  // 模型名称
        chatAgentMessage.setPrompt("介绍一下 Java Reactor 编程模式");
        chatAgentMessage.setSystemPrompt("你是一个编程大师");
        chatAgentMessage.setTemperature(0.7);         // 温度参数
        chatAgentMessage.setMaxTokens(8192);          // 最大输出 Token
        
        // 创建 AIAgent 并调用
        AIAgent aiAgent = new AIAgent();
        ServerEvent response = aiAgent.chat("deepseek", chatAgentMessage);
        
        // 输出结果
        System.out.println(response.getData());
    }
}
```

### 3.4 流式聊天

```java
import reactor.core.publisher.Flux;
import java.util.concurrent.CountDownLatch;

public class StreamChatExample {
    public static void main(String[] args) throws InterruptedException {
        // 初始化配置
        HttpRequestProxy.startHttpPools("application-stream.properties");

        // 创建消息对象，设置 stream=true
        ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
        chatAgentMessage.setModel("deepseek-chat");
        chatAgentMessage.setPrompt("介绍一下 Java Reactor 编程模式");
        chatAgentMessage.setStream(true);
        chatAgentMessage.setTemperature(0.7);
        chatAgentMessage.setMaxTokens(8192);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        AIAgent aiAgent = new AIAgent();
        Flux<ServerEvent> flux = aiAgent.streamChat("deepseek", chatAgentMessage);

        flux.doOnSubscribe(subscription -> System.out.println("开始订阅流..."))
                .doOnNext(chunk -> {
                    if (chunk.getData() != null) {
                        System.out.print(chunk.getData());
                    }
                })
                .doOnComplete(() -> {
                    System.out.println("\n=== 流完成 ===");
                    countDownLatch.countDown();
                })
                .doOnError(error -> {
                    error.printStackTrace();
                    countDownLatch.countDown();
                })
                .subscribe();

        // 等待异步完成
        countDownLatch.await();
    }
}
```

### 3.5 多轮会话

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionChatExample {
    // 静态会话记忆列表
    static List<Map<String, Object>> sessionMemory = new ArrayList<>();

    public static void main(String[] args) {
        HttpRequestProxy.startHttpPools("application-stream.properties");

        AIAgent aiAgent = new AIAgent();

        // 第一轮对话
        ChatAgentMessage message1 = new ChatAgentMessage();
        message1.setModel("deepseek-chat");
        message1.setPrompt("我叫张三");
        message1.setSessionMemory(sessionMemory);  // 传入会话记忆
        message1.setSessionSize(50);                // 保留最近50条消息

        ServerEvent response1 = aiAgent.chat("deepseek", message1);
        System.out.println("AI: " + response1.getData());

        // 第二轮对话，AI 会记住用户名字
        ChatAgentMessage message2 = new ChatAgentMessage();
        message2.setModel("deepseek-chat");
        message2.setPrompt("我叫什么名字？");
        message2.setSessionMemory(sessionMemory);

        ServerEvent response2 = aiAgent.chat("deepseek", message2);
        System.out.println("AI: " + response2.getData());

        // 重置会话
        sessionMemory.clear();
    }
}
```

---

多智能体协同编排，访问章节：[多智能体协同编排](https://esdoc.bbossgroups.com/#/bboss-ai?id=%e5%8d%81%e5%9b%9b%e3%80%81%e5%a4%9a%e6%99%ba%e8%83%bd%e4%bd%93%e7%bc%96%e6%8e%92%e5%b7%a5%e4%bd%9c%e6%b5%81)

### 3.6 失败重试

如果模型服务不是太稳定，则需要设置重试机制，通过以下参数设置重试机制：

**retry** 设置重试次数，大于0时，当调用大模型失败时，会自动重试，否则不重试，默认值0

**retryInterval** 每次重试时，需等待特定时间后再重试，单位毫秒，大于0时才进行等待，默认值：500毫秒

```java
ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
chatAgentMessage.setModel("deepseek-v4-pro")
       .setStream( true)
       .setRetry(3).setRetryInterval(1000L)
       .setMaas("deepseek").setTemperature(0.3)
       .setPrompt(question);
```

## 四、图片识别（视觉大模型）

### 4.1 单张图片识别

```java
import org.frameworkset.spi.ai.model.ImageVLAgentMessage;
import com.frameworkset.util.FileUtil;

public class ImageRecognitionExample {
    public static void main(String[] args) {
        HttpRequestProxy.startHttpPools("application-stream.properties");

        ImageVLAgentMessage imageMsg = new ImageVLAgentMessage();
        imageMsg.setModel("qwen3-vl-plus");
        imageMsg.setPrompt("介绍图片内容");

        // 方式1：使用图片 URL
        imageMsg.addImageUrl("https://example.com/image.jpg");

        // 方式2：使用 Base64 编码
        String base64 = FileUtil.getBase64Content("/path/to/image.jpg");
        imageMsg.addImageUrl(base64);

        // 可选参数
        imageMsg.addParameter("enable_thinking", true);   // 开启思考过程
        imageMsg.addParameter("thinking_budget", 81920);  // 推理 Token 数

        AIAgent aiAgent = new AIAgent();
        ServerEvent response = aiAgent.imageParser("qwenvlplus", imageMsg);

        System.out.println(response.getData());
    }
}
```

### 4.2 流式图片识别

```java
public static void streamImageRecognition() throws InterruptedException {
    ImageVLAgentMessage imageMsg = new ImageVLAgentMessage();
    imageMsg.setModel("qwen3-vl-plus");
    imageMsg.setPrompt("介绍图片内容");
    imageMsg.addImageUrl("https://example.com/image.jpg");
    imageMsg.setStream(true);

    CountDownLatch latch = new CountDownLatch(1);

    AIAgent aiAgent = new AIAgent();
    Flux<ServerEvent> flux = aiAgent.streamImageParser("qwenvlplus", imageMsg);

    flux.doOnNext(chunk -> {
                if (!chunk.isDone() && chunk.getData() != null) {
                    System.out.print(chunk.getData());
                }
            })
            .doOnComplete(() -> latch.countDown())
            .subscribe();

    latch.await();
}
```

### 4.3 多张图片对比

```java
public static void compareImages() {
    ImageVLAgentMessage imageMsg = new ImageVLAgentMessage();
    imageMsg.setModel("qwen3-vl-plus");
    imageMsg.setPrompt("对比两张图片的相似度，以 JSON 格式返回结果");

    imageMsg.addImageUrl("https://example.com/image1.jpg");
    imageMsg.addImageUrl("https://example.com/image2.jpg");

    AIAgent aiAgent = new AIAgent();
    ServerEvent response = aiAgent.imageParser("qwenvlplus", imageMsg);

    System.out.println(response.getData());
}
```

---

## 五、图片生成

### 5.1 文本生成图片

```java
import org.frameworkset.spi.ai.model.ImageAgentMessage;
import org.frameworkset.spi.ai.model.ImageEvent;

public class ImageGenerationExample {
    public static void main(String[] args) {
        HttpRequestProxy.startHttpPools("application-stream.properties");

        ImageAgentMessage request = new ImageAgentMessage();
        request.setPrompt("一只可爱的橘猫，坐在窗台上晒太阳，油画风格");
        request.setModel("qwen-image-plus");

        // 可选参数
        request.addParameter("size", "1328*1328");      // 图片尺寸
        request.addParameter("watermark", false);       // 是否添加水印
        request.addParameter("prompt_extend", true);    // 提示词扩展

        // 设置图片保存路径
        request.setStoreFilePathFunction(imageUrl -> {
            return "image/" + System.currentTimeMillis() + ".jpg";
        });

        AIAgent aiAgent = new AIAgent();
        ImageEvent result = aiAgent.genImage("qwenvlplus", request);

        System.out.println("生成的图片 URL: " + result.getGenImageUrl());
        System.out.println("本地保存路径: " + result.getImageUrl());
    }
}
```

### 5.2 火山引擎豆包图片生成

```java
public static void doubaoImageGen() {
    ImageAgentMessage request = new ImageAgentMessage();
    request.setPrompt("一只可爱的小狗，卡通风格");
    request.setModel("doubao-seedream-5-0-260128");

    // 豆包特有参数
    request.addParameter("response_format", "url");
    request.addParameter("size", "2k");
    request.addParameter("watermark", true);
    request.addParameter("sequential_image_generation", "auto");  // 多图生成
    request.addMapParameter("sequential_image_generation_options",
            "max_images", 3);

    AIAgent aiAgent = new AIAgent();
    ImageEvent result = aiAgent.genImage("volcengine", request);

    System.out.println("生成图片数量: " + result.getImageUrls().size());
}
```

### 5.3 图文生图（修图）

```java
String selectedModel = (String)questions.get("selectedModel");
boolean generateMultipleImages  = questions != null?(boolean)questions.get("generateMultipleImages"):false;
String message  = null;
message = questions != null?(String)questions.get("message"):null;
        if(SimpleStringUtil.isEmpty( message)){
message = "生成一颗桂花树";
        }
ImageAgentMessage request = new ImageAgentMessage();
        request.setStoreFilePathFunction(new StoreFilePathFunction() {
    @Override
    public String getStoreFilePath(String imageUrl) {
        return "image/"+SimpleStringUtil.getUUID32() +".jpg";
    }
});

List<String> imagesBase64  = (List)questions.get("imagesBase64");
String imageUrl = (String)questions.get("imageUrl");
        if(imageUrl != null) {
imageUrl = imageUrl.trim();
        }




                if(SimpleStringUtil.isNotEmpty(imageUrl)) {
        request.addImageUrl(imageUrl);
        }
                if(SimpleStringUtil.isNotEmpty(imagesBase64)) {
        for(String tmp:imagesBase64) {
        request.addImageUrl(tmp);
//                Map<String, Object> requestMap = new HashMap<>();
//                requestMap.put("model", "LLMImage2Text");
//                requestMap.put("image",tmp);
//                requestMap.put("prompt", message);
//                requestMap.put("stream", true);
//                String rsp = HttpRequestProxy.httpPostforString("jiutian", "/largemodel/moma/api/v3/image/text", requestMap);
//                logger.info(rsp);
            }


                    }
                    request.setPrompt( message);
ImageEvent data = null;
AIAgent aiAgent = new AIAgent();
//        String completionsUrl = null;
        if(selectedModel.equals("volcengine")){
        //字节火山引擎
        request.setModel( "doubao-seedream-4-5-251128");
            if(!generateMultipleImages) {
        request.addParameter("sequential_image_generation", "disabled");//生成单图
            }
                    else {
                    request.addParameter("sequential_image_generation", "auto");//生成多图
                request.addMapParameter("sequential_image_generation_options","max_images",5);
            }
                    request.addParameter("response_format", "url");
            request.addParameter("size", "2k");
            request.addParameter("watermark", true);
//            completionsUrl = "/api/v3/images/generations";
        }
                else if(selectedModel.equals("jiutian")){
        //字节火山引擎
        request.setModel( "cntxt2image");
//            completionsUrl = "/largemodel/moma/api/v3/images/generations";
        }
                else{

                //阿里百炼
                //通过在http.modelType指定全局模型适配器类型，亦可以在ImageAgentMessage对象设置请求级别modelType模型适配器类型（优先级高于全局模型适配器类型）
                request.setModel( "qwen-image-edit-max-2026-01-16");
            request.addParameter("n",5);
            request.addParameter("prompt_extend",true);
            request.addParameter("watermark",false);
            request.addParameter("size","1536*1024");
///api/v1/services/aigc/multimodal-generation/generation
//            completionsUrl = "/api/v1/services/aigc/multimodal-generation/generation";

        }
ImageEvent imagedata = aiAgent.genImage(selectedModel,request);
System.out.println("生成图片数量: " + result.getImageUrls().size());
```

---

## 六、语音识别（STT）

```java
import org.frameworkset.spi.ai.model.AudioSTTAgentMessage;

public class SpeechRecognitionExample {
    public static void main(String[] args) throws IOException {
        HttpRequestProxy.startHttpPools("application-stream.properties");

        AudioSTTAgentMessage audioMsg = new AudioSTTAgentMessage();
        audioMsg.setModel("qwen3-asr-flash");
        audioMsg.setPrompt("介绍音频内容");

        // 方式1：设置音频文件
        File audioFile = new File("/path/to/audio.wav");
        audioMsg.setAudio(audioFile);
        audioMsg.setContentType("audio/wav");

        // 方式2：设置音频 URL
        // audioMsg.setAudio("https://example.com/audio.mp3");

        // 可选参数
        audioMsg.addMapParameter("asr_options", "enable_itn", true);
        audioMsg.addParameter("incremental_output", true);
        audioMsg.setResultFormat("message");

        AIAgent aiAgent = new AIAgent();
        ServerEvent result = aiAgent.audioParser("qwenvlplus", audioMsg);

        System.out.println("识别结果: " + result.getData());
    }
}
```

---

## 七、语音合成（TTS）

### 7.1 同步语音合成

通义千问语音生成：

```java
import org.frameworkset.spi.ai.model.AudioAgentMessage;
import org.frameworkset.spi.ai.model.AudioEvent;

public class TextToSpeechExample {
    public static void main(String[] args) {
        HttpRequestProxy.startHttpPools("application-stream.properties");

        AudioAgentMessage audioMsg = new AudioAgentMessage();
        audioMsg.setModel("qwen3-tts-flash");
        audioMsg.setPrompt("诗歌朗诵：床前明月光，疑似地上霜。");

        // 可选参数
        audioMsg.addParameter("voice", "Cherry");           // 音色
        audioMsg.addParameter("language_type", "Chinese");  // 语言

        // 设置保存路径，通过StoreFilePathFunction保存音频文件到本地
        audioMsg.setStoreFilePathFunction(url -> {
            return "audio/" + System.currentTimeMillis() + ".wav";
        });

        AIAgent aiAgent = new AIAgent();
        AudioEvent result = aiAgent.genAudio("qwenvlplus", audioMsg);

        System.out.println("音频文件路径: " + result.getAudioUrl());
    }
}
```

智谱语音生成：

```java
   AudioAgentMessage audioMsg = new AudioAgentMessage();
            //https://docs.bigmodel.cn/api-reference/%E6%A8%A1%E5%9E%8B-api/%E6%96%87%E6%9C%AC%E8%BD%AC%E8%AF%AD%E9%9F%B3
             
 			audioMsg.setModel("glm-tts");
        	audioMsg.setPrompt("诗歌朗诵：床前明月光，疑似地上霜。");
            audioAgentMessage.addParameter("voice", "female")
                    .addParameter("response_format", "wav")
                    .addParameter("speed", 1.0)
                    .addParameter("volume", 1.0)            
                    ;
 // 设置保存路径,智谱语音模型默认返回对应音频文件格式的base64字符串。
        audioMsg.setStoreFilePathFunction(new ReponseStoreFilePathFunction() {
                @Override
                public String getStoreFilePath(String imageUrl) {
                    return "audio/"+SimpleStringUtil.getUUID32() +".wav";
                }
            });            
       
        
        AIAgent aiAgent = new AIAgent();
        AudioEvent audioEvent = aiAgent.genAudio(selectedModel,audioAgentMessage,storeFilePathFunction);
        return audioEvent;
```

StoreFilePathFunction和ReponseStoreFilePathFunction区别：

StoreFilePathFunction：当模型将生成的语音通过url方式返回时，采用StoreFilePathFunction保存到本地音频文件

ReponseStoreFilePathFunction：当模型将生成的语音通过base64码方式返回时，采用ReponseStoreFilePathFunction直接将base64编码音频内容保存到本地音频文件

### 7.2 流式语音合成

```java
public static void streamTextToSpeech() throws InterruptedException {
    AudioAgentMessage audioMsg = new AudioAgentMessage();
    audioMsg.setModel("qwen3-tts-flash");
    audioMsg.setPrompt("欢迎使用 bboss AI 语音合成服务");
    audioMsg.addParameter("voice", "Cherry");
    audioMsg.setStream(true);

    CountDownLatch latch = new CountDownLatch(1);

    AIAgent aiAgent = new AIAgent();
    Flux<ServerEvent> flux = aiAgent.streamAudioGen("qwenvlplus", audioMsg);

    flux.doOnNext(chunk -> {
                if (!chunk.isDone() && chunk.getData() != null) {
                    // 处理音频数据块
                    byte[] audioData = Base64.getDecoder().decode(chunk.getData());
                    // 播放或保存音频数据
                }
            })
            .doOnComplete(() -> latch.countDown())
            .subscribe();

    latch.await();
}
```

---

## 八、工具调用（Function Calling）

基于bboss发布工具函数参考文档：https://esdoc.bbossgroups.com/#/bboss-ai-toolannotation

高效工具检索功能参考使用文档：https://esdoc.bbossgroups.com/#/bboss-ai-toolsearcher

### 8.1 本地工具调用

```java
import org.frameworkset.spi.ai.model.FunctionToolDefine;
import org.frameworkset.spi.ai.model.ToolFunctionCall;

public class ToolCallExample {
    public static void main(String[] args) {
        ChatAgentMessage chatMsg = new ChatAgentMessage();
        chatMsg.setModel("deepseek-chat");
        chatMsg.setPrompt("查询杭州的天气，并给出穿衣建议");

        // 定义天气查询工具
        FunctionToolDefine weatherTool = new FunctionToolDefine();
        weatherTool.funtionName2ndDescription("weather_info_query","天气查询服务，根据城市查询当地温度和天气信息")
//                            .putParametersType("object")
                .requiredParameters("location")
                .addSubParameter("params","location","string","城市或者地州, 例如：上海市")
                .setFunctionCall(new ToolFunctionCall() );

        chatMsg.registTool(weatherTool);

        AIAgent aiAgent = new AIAgent();
        ServerEvent response = aiAgent.chat("deepseek", chatMsg);
        System.out.println(response.getData());
    }


}
```

ToolFunctionCall工具调用实现：

```java
import org.frameworkset.spi.ai.model.FunctionCall;
import org.frameworkset.spi.ai.model.FunctionCallException;
import org.frameworkset.spi.ai.model.FunctionTool;
import org.frameworkset.spi.remote.http.HttpRequestProxy;

import java.util.List;
import java.util.Map;


public class ToolFunctionCall implements FunctionCall {
    private String toolDatasource = "tool";

    public ToolFunctionCall(String toolDatasource) {
        this.toolDatasource = toolDatasource;
    }
    public ToolFunctionCall( ) {
    }


    @Override
    public Object call(FunctionTool functionTool) throws FunctionCallException {
        Map response = HttpRequestProxy.sendJsonBody(toolDatasource,functionTool.getArguments(),  "/openapi/"+functionTool.getFunctionName() + ".api", Map.class);
        List data = (List)response.get("data");
        if(data == null || data.size() == 0){
            return "没有找到对应数据";
        }
        return data;
//        return "20.22℃";
    }
}
```



### 8.2 远程工具调用

```java
chatMsg.setToolsRegist(new ToolsRegist() {
    @Override
    public List<FunctionToolDefine> registTools() {
        // 从远程服务获取工具定义
        Map<String, Object> params = new HashMap<>();
        params.put("apiKey", "your-api-key");
        return HttpRequestProxy.sendJsonBodyForList("tool", params,
                "/function/tools.api", FunctionToolDefine.class);
    }

    @Override
    public FunctionCall getFunctionCall(String functionName) {
        // 返回远程工具调用器
        return new ToolFunctionCall("tool");
    }
});
```

---

## 九、MCP 服务集成

基于bboss发布mcp服务参考文档：https://esdoc.bbossgroups.com/#/bboss-ai-mcpbean

基于bboss发布mcp服务并与spring boot集成参考文档：https://esdoc.bbossgroups.com/#/bboss-ai-springboot

### 9.1 使用 MCP 工具

```java
import org.frameworkset.spi.ai.mcp.tools.MCPToolsRegist;

public class MCPExample {
    public static void main(String[] args) {
        ChatAgentMessage chatMsg = new ChatAgentMessage();
        chatMsg.setModel("deepseek-chat");
        chatMsg.setPrompt("帮我查一下明天北京到上海的高铁");

        // 注册 12306高铁MCP 工具服务，
        chatMsg.setToolsRegist(new MCPToolsRegist("12306"));  // 对应配置文件中的服务名

        AIAgent aiAgent = new AIAgent();
        ServerEvent response = aiAgent.chat("deepseek", chatMsg);
        System.out.println(response.getData());
    }
}
```

### 9.2 流式 MCP 调用

```java
public static void streamChatWithMcpTools(String maas, String mcpServer,
                                          String model, String prompt)
        throws InterruptedException {

    List<Map<String, Object>> session = new ArrayList<>();
    ChatAgentMessage chatMsg = new ChatAgentMessage()
            .setPrompt(prompt)
            .setSessionSize(50)
            .setSessionMemory(session)
            .setModel(model)
            .setStream(true)
            .setMaxTokens(4096);

    chatMsg.setToolsRegist(new MCPToolsRegist(mcpServer));

    CountDownLatch latch = new CountDownLatch(1);
    AIAgent aiAgent = new AIAgent();

    aiAgent.streamChat(maas, chatMsg)
            .doOnNext(chunk -> {
                if (!chunk.isDone() && chunk.getData() != null) {
                    System.out.print(chunk.getData());
                } else if (chunk.isToolCallsType()) {
                    System.out.println("\n开始执行工具...");
                }
            })
            .doOnComplete(() -> latch.countDown())
            .subscribe();

    latch.await();
}
```

---

### 9.3 飞书MCP服务集成调用

飞书MCP服务实现与标准MCP服务有所差别：需要通过http header设置临时AccessToken和工具清单配置

飞书开发者调用远程 MCP 服务文档：

https://open.feishu.cn/document/mcp_open_tools/developers-call-remote-mcp-server

下面是bboss对接和使用飞书Mcp服务的案例代码：feishumcp server配置，参考前文的mcp服务配置，基于streamable 访问飞书Mcp服务

```java
            BaseFeishuConfig baseFeishuConfig = new BaseFeishuConfig();
//            设置bboss应用id和应用token，用于申请访问飞书Mcp服务的AccessToken
            baseFeishuConfig.setFeishuAppId("cli_a9d43b8f89cd0")
                    .setFeishAppSecret("gIhy0EbVfgQGlpNBN8rqMKMnYCJs");
//定义用于获取飞书Mcp服务认证Token的http数据源：feishu，用例访问开发平台获取飞书MCP服务认证Token
            baseFeishuConfig.addHttpConfig("http.poolNames", "feishu")
                    .addHttpConfig("feishu.http.hosts", "https://open.feishu.cn")
                    .addHttpConfig("feishu.http.maxTotal", 100)
                    .addHttpConfig("feishu.http.defaultMaxPerRoute", 100)
                .addHttpConfig("feishu.http.authorTokenFunction", "org.frameworkset.spi.ai.mcp.feishu.FeishuMCPAuthorTokenFunction")
                feishumcp.http.authorTokenFunction = 
// 25分钟自动刷新token
// tenant_access_token 的最大有效期是 2 小时。
// 剩余有效期小于 30 分钟时，调用本接口会返回一个新的 tenant_access_token，这会同时存在两个有效的 tenant_access_token。
// 剩余有效期大于等于 30 分钟时，调用本接口会返回原有的 tenant_access_token
feishumcp.http.authorTokenExpiredTime = 1500000
                
                .addHttpConfig("feishu.http.authorTokenExpiredTime", 1500000)
                    .setMcpTools("search-user,get-user,fetch-file,search-doc,create-doc,fetch-doc,update-doc,list-docs,get-comments,add-comments");//需要设置飞书Mcp工具清单，否则大模型无法识别Mcp工具并调用
;
            chatAgentMessage.setToolsRegist(new FeishuMcpRegist("feishumcp",baseFeishuConfig));

CountDownLatch latch = new CountDownLatch(1);
AIAgent aiAgent = new AIAgent();

    aiAgent.streamChat(maas, chatMsg)
            .doOnNext(chunk -> {
        if (!chunk.isDone() && chunk.getData() != null) {
        System.out.print(chunk.getData());
        } else if (chunk.isToolCallsType()) {
        System.out.println("\n开始执行工具...");
                }
                        })
                        .doOnComplete(() -> latch.countDown())
        .subscribe();

    latch.await();
```

## 十、视频识别与生成

### 10.1 视频内容识别

```java
public static void videoRecognition() {
    VideoVLAgentMessage videoMsg = new VideoVLAgentMessage();
    videoMsg.setModel("kimi-k2.5");
    videoMsg.setPrompt("识别视频内容");

    // 使用 Base64 编码的视频文件
    String base64 = FileUtil.getBase64Video("/path/to/video.mp4");
    videoMsg.addVideoUrl(base64);

    // 可选参数
    videoMsg.addMapParameter("thinking", "type", "disabled");

    AIAgent aiAgent = new AIAgent();
    ServerEvent response = aiAgent.videoParser("kimi", videoMsg);
    System.out.println(response.getData());
}
```

### 10.2 提交视频生成任务

```
public static void submitVideoTask() {
    VideoAgentMessage videoMsg = new VideoAgentMessage();
    videoMsg.setModel("wan2.6-t2v");
    videoMsg.setPrompt("一只可爱的小猫在花园里玩耍");
    
    // 可选参数
    videoMsg.addParameter("size", "1280*720");
    videoMsg.addParameter("duration", 10);
    videoMsg.addParameter("prompt_extend", true);
    videoMsg.addParameter("watermark", true);
    
    AIAgent aiAgent = new AIAgent();
    VideoTask task = aiAgent.submitVideoTask("qwenvlplus", videoMsg);
    
    System.out.println("任务 ID: " + task.getTaskId());
    System.out.println("任务状态: " + task.getTaskStatus());
}
```

### 10.3 查询视频生成结果

```
public static void getVideoTaskResult(String taskId) {
    VideoStoreAgentMessage queryMsg = new VideoStoreAgentMessage();
    queryMsg.setTaskId(taskId);
    queryMsg.setStoreFilePathFunction(url -> {
        return "video/" + System.currentTimeMillis() + ".mp4";
    });
    
    AIAgent aiAgent = new AIAgent();
    VideoGenResult result = aiAgent.getVideoTaskResult("qwenvlplus", queryMsg);
    
    System.out.println("任务状态: " + result.getTaskStatus());
    System.out.println("视频 URL: " + result.getVideoGenUrl());
    System.out.println("本地保存路径: " + result.getVideoUrl());
}
```

---
## 十一、私有Maas平台对接

bboss默认做了对国内主流公有云maas平台的兼容和支持，包括DeepSeek、Kimi、智谱、阿里百炼通义千问、字节豆包火山引擎、MiniMax、腾讯混元、中国移动九天等主流 MaaS 平台。

如果需要对接遵循openai规范的本地私有Maas平台，只需编写自定义的适配器，并进行注册即可，本节介绍私有Maas平台对接方法。

### 11.1 编写自定义适配器

首先，编写自定义适配器，示例如下：直接继承已有通义千问适配器QwenAgentAdapter或者其他现有适配器，重载各种多模态能力相对地址获取方法即可

```java
package org.frameworkset.spi.ai;

import org.frameworkset.spi.ai.adapter.QwenAgentAdapter;
import org.frameworkset.spi.ai.model.*;


public class CustomAgentAdapter extends QwenAgentAdapter {
    //LLM文本对话地址
    @Override
    public String getChatCompletionsUrl(ChatAgentMessage chatAgentMessage) {
        return "/compatible-mode/v1/chat/completions";
    }
    //提交生成视频任务地址
    @Override
    public String getSubmitVideoTaskUrl(VideoAgentMessage videoAgentMessage){
        if(videoAgentMessage.getFirstFrameUrl() != null) {
            return "/api/v1/services/aigc/image2video/video-synthesis";
        }
        else {
            return "/api/v1/services/aigc/video-generation/video-synthesis";
        }
    }
    //语音生成地址
    @Override
    public String getGenAudioCompletionsUrl(AudioAgentMessage audioAgentMessage){
        return "/api/v1/services/aigc/multimodal-generation/generation";
    }

    /**
     * maas平台音频识别服务地址
     * @param audioSTTAgentMessage
     * @return
     */
    @Override

    public String getAudioSTTCompletionsUrl(AudioSTTAgentMessage audioSTTAgentMessage){
        return "/api/v1/services/aigc/multimodal-generation/generation";
    }
    //视频识别地址
    @Override
    public String getVideoVLCompletionsUrl(VideoVLAgentMessage videoVLAgentMessage) {
        return "/v1/chat/completions";
    }
    //图像识别地址
    @Override
    public String getImageVLCompletionsUrl(ImageVLAgentMessage imageVLAgentMessage) {
        return "/compatible-mode/v1/chat/completions";
    }
    //图像生成地址
    @Override
    public String getGenImageCompletionsUrl(ImageAgentMessage imageAgentMessage) {
        return "/api/v1/services/aigc/multimodal-generation/generation";
    }
    //查询和获取视频任务执行结果地址，执行任务后返回结果中，包含生成的视频url（用于下载），已下载视频本地访问地址
    @Override
    public String getVideoTaskResultUrl(VideoStoreAgentMessage videoStoreAgentMessage){
        return "/api/v1/tasks/"+videoStoreAgentMessage.getTaskId();
    }
}

```

如果有涉及到相关模型协议规范的适配调整，可以重载父类的其他方法。

### 11.2 注册适配器

可以通过以下两种方法注册自定义适配器：

#### 方法1 配置文件方式（推荐）

```properties
http.poolNames = deepseek,guiji,qwenvlplus,volcengine,jiutian,kimi,zhipu,minimax,hunyuan,custom

custom.http.maxTotal = 200
custom.http.defaultMaxPerRoute = 200
# 配置maas平台地址
custom.http.hosts=https://dashscope.aliyuncs.com
#基于apiKeyId认证配置（主要用于各种大模型服务对接认证）

custom.http.apiKeyId = sk-469f01d6d6db4fb2f4
# 模型类型：AI智能体工具通过模型类型查找智能体模型对接适配器
custom.http.modelType = custom
# 模型对接适配器配置
custom.http.agentAdapter=org.frameworkset.spi.ai.CustomAgentAdapter

```

说明：

1. 定义一个名称为custom（名称可以根据实际情况定）的maas服务，并添加到http.poolNames中

   ```properties
   http.poolNames = deepseek,guiji,qwenvlplus,volcengine,jiutian,kimi,zhipu,minimax,hunyuan,custom
   ```



2.  配置maas平台地址和apiKey

   ```properties
   custom.http.hosts=https://dashscope.aliyuncs.com
   ```

custom.http.apiKeyId = sk-469f01dbb5724e6dbb2f4
   ```



3. 指定模型平台类型(可选)

   ```properties
   custom.http.modelType = custom
   ```

   可以不指定，这时会使用hosts（maas平台地址）作为模型类型

4. 模型对接适配器配置，配置定义好的自定义适配器类路径

   ```properties
   custom.http.agentAdapter=org.frameworkset.spi.ai.CustomAgentAdapter
   ```

#### 方法2 代码API配置（不推荐）

首先，还是要在配置文件中配置maas平台服务：只是不需要指定模型平台适配器

```properties
http.poolNames = deepseek,guiji,qwenvlplus,volcengine,jiutian,kimi,zhipu,minimax,hunyuan,custom

custom.http.maxTotal = 200
custom.http.defaultMaxPerRoute = 200
# 配置maas平台地址
custom.http.hosts=https://dashscope.aliyuncs.com
#基于apiKeyId认证配置（主要用于各种大模型服务对接认证）

custom.http.apiKeyId = sk-469f01d6d6db4fb2f4
# 模型类型：AI智能体工具通过模型类型查找智能体模型对接适配器
custom.http.modelType = custom
```

然后在加载maas平台服务时，注册模型适配器：

```java
AgentAdapterFactory.registerAgentAdapter("custom",CustomAgentAdapter.class);//注册模型平台适配器
HttpRequestProxy.startHttpPools("application-stream.properties");
```
## 十二、背压与缓冲控制

```java
Flux<ServerEvent> flux = aiAgent.streamChat("deepseek", chatMsg)
    .limitRate(5)        // 限制请求速率，每次最多 5 个元素
    .buffer(3);          // 每 3 个元素缓冲一次

flux.subscribe(bufferedEvents -> {
    for (ServerEvent event : bufferedEvents) {
        // 批量处理事件
    }
});
```

---

## 十三、完整示例：多轮智能问答助手

```java
import org.frameworkset.spi.ai.AIAgent;
import org.frameworkset.spi.ai.model.*;
import org.frameworkset.spi.remote.http.HttpRequestProxy;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class IntelligentAssistant {
    private static List<Map<String, Object>> sessionMemory = new ArrayList<>();
    private static AIAgent aiAgent = new AIAgent();
    
    static {
        HttpRequestProxy.startHttpPools("application-stream.properties");
    }
    
    public static void chat(String message, boolean reset) throws InterruptedException {
        if (reset) {
            sessionMemory.clear();
            System.out.println("会话已重置");
        }
        
        ChatAgentMessage chatMsg = new ChatAgentMessage();
        chatMsg.setModel("deepseek-chat");
        chatMsg.setPrompt(message);
        chatMsg.setStream(true);
        chatMsg.setSessionMemory(sessionMemory);
        chatMsg.setSessionSize(50);
        chatMsg.setMaxTokens(8192);
        chatMsg.setTemperature(0.7);
        
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder answer = new StringBuilder();
        
        aiAgent.streamChat("deepseek", chatMsg)
            .doOnNext(chunk -> {
                if (!chunk.isDone() && chunk.getData() != null) {
                    System.out.print(chunk.getData());
                    answer.append(chunk.getData());
                }
            })
            .doOnComplete(() -> {
                // 保存回答到会话记忆
                if (answer.length() > 0) {
                    chatMsg.addSessionMessage(answer.toString());
                }
                System.out.println();
                latch.countDown();
            })
            .doOnError(error -> {
                System.err.println("错误: " + error.getMessage());
                latch.countDown();
            })
            .subscribe();
        
        latch.await();
    }
    
    public static void main(String[] args) throws InterruptedException {
        chat("你好，我叫小明", false);
        chat("我叫什么名字？", false);
        chat("重置会话", true);
        chat("我叫什么名字？", false);
    }
}
```

---

## 十四、多智能体编排工作流

bboss-ai-flow 模块提供了一套强大的智能体工作流编排能力，基于有向循环图实现多智能体协同。支持串行、并行、路由、条件分支、裁判评估等丰富的流程编排模式，能够快速构建复杂的多智能体系统。

![](images\workflow\bbossgraph.png)

多智能体编排工作流底层基于[bboss jobflow](https://esdoc.bbossgroups.com/#/jobworkflow)实现（一套数据交换作业编排工作流）

### 14.1 工作流核心组件

| 组件 | 说明 |
|------|------|
| `AIPlanAgent` | 工作流编排主控制器，负责构建和执行整个智能体工作流 |
| `AINodeAgent` | 普通AI节点智能体，执行具体的AI任务 |
| `UserNodeAgent` | 用户智能体，不引用上游父智能体的会话记忆，适合处理独立任务 |
| `AIParrelAgent` | 并行智能体容器，内部多个子智能体并发执行 |
| `AISequenceAgent` | 串行智能体容器，内部多个子智能体按顺序执行 |
| `AIRouteAgent` | 基于LLM路由智能体，根据用户问题自动决策后续执行路径 |
| AIKeywordsRouteAgent | 基于关键词数组路由智能体，根据用户问题和关键词自动决策后续执行路径 |
| `AIJudgeAgent` | 裁判智能体，评估执行结果是否满足预期，内置节点及变量：#[input.query,scope=node]<br/>#[answer,scope=node] |
| `StoreContext` | 会话存储上下文，支持内存和数据库两种持久化方式 |
| StandaloneAgent | 独立智能体，不会接受上游消息,也不会向下游推送消息 |
| `AIFlowNode` | 通用工作流节点，执行纯Java自定义逻辑，不调用大模型，可在工作流中加工和传递数据、记录流程过程数据<br/>,接口方法call带返回值 |
| AIFlowNodeVoid | 通用工作流节点，执行纯Java自定义逻辑，不调用大模型，可在工作流中加工和传递数据、记录流程过程数据<br/>,接口方法call不需要返回值 |

#### 14.1.1 会话存储配置

工作流支持将会话记录持久化到数据库或内存中：

```java
import org.frameworkset.spi.ai.store.StoreContext;

// 数据库存储方式
StoreContext storeContext = new StoreContext()
    .setSessionId("session_001")           // 会话ID
    .setUserId("user123")                  // 用户ID
    .setRequestId("request123")            // 请求ID
    .setSessionSize(100)                   // 会话窗口大小
    .setStoreType(StoreContext.STORE_TYPE_DB)  // 数据库存储
    .setDataSource("visualops");           // 数据源名称

// 内存存储方式（默认）
StoreContext memoryContext = new StoreContext()
    .setSessionSize(50)
    .setStoreType(StoreContext.STORE_TYPE_MEMORY);
```

#### 14.1.2 工作流基本结构

```java
import org.frameworkset.spi.ai.flow.*;
import org.frameworkset.spi.ai.model.ChatAgentMessage;
import org.frameworkset.spi.ai.model.LastSessionMessage;
import org.frameworkset.spi.ai.store.StoreContext;

// 1. 定义会话消息
ChatAgentMessage chatAgentMessage = new ChatAgentMessage()
    .setModel("qwen3.6-plus")
    .setMaas("qwenvlplus")
    .setPrompt("介绍省份智能体");

// 2. 定义工作流智能体
AIPlanAgent aiPlanAgent = new AIPlanAgent(storeContext)
    .setAgentMessage(chatAgentMessage)
    .setAgentName("工作流智能体")
    .setAgentId("workflowAgent");

// 3. 添加工作流节点...

// 4. 执行工作流
LastSessionMessage result = aiPlanAgent.chat();
```



### 14.2 串行任务编排

串行智能体（`AISequenceAgent`）将多个子智能体按顺序执行，前一个智能体的输出会作为后续智能体的上下文输入。

#### 14.2.1 同步串行工作流

```java
// 构建工作流智能体
AIPlanAgent aiPlanAgent = new AIPlanAgent(new StoreContext()
        .setSessionId(sessionId).setUserId("user123").setSessionSize(100)
        .setStoreType(StoreContext.STORE_TYPE_DB).setRequestId("request123")
        .setDataSource("visualops"))
    .setAgentMessage(chatAgentMessage)
    .setAgentName("工作流智能体").setAgentId("workflowAgent");

// 添加第一个普通节点
AIBaseNodeAgent introduceProvinces = new AINodeAgent("用200字介绍中国有多少个省份和直辖市")
    .setAgentName("介绍中国省份和直辖市")
    .setAgentId("introduceProvinces");
aiPlanAgent.addAgent(introduceProvinces);

// 构建串行智能体
AISequenceAgent sequenceAgent = new AISequenceAgent(aiPlanAgent)
    .setAgentId("sequenceAgent")
    .setAgentName("串行任务节点");

// 按顺序添加子智能体
sequenceAgent.addAgent(new AINodeAgent("用50字介绍湖南，并且和介绍中国省份和直辖市内容合并输出")
    .setAgentId("jieshaohunan")
    .setAgentName("用50字介绍湖南"));
sequenceAgent.addAgent(new AINodeAgent("用50字介绍湖北")
    .setAgentId("jieshaohubei")
    .setAgentName("用50字介绍湖北"));
sequenceAgent.addAgent(new AINodeAgent("用50字介绍江西")
    .setAgentId("jieshaojiangxi")
    .setAgentName("用50字介绍江西"));
sequenceAgent.addAgent(new AINodeAgent("将下面的文字翻译为英文（不要回答问题）：用50字介绍江西")
    .setAgentId("translate")
    .setAgentName("将文字翻译为英文"));

// 将串行智能体加入主工作流
aiPlanAgent.addAgent(sequenceAgent);

// 执行工作流
LastSessionMessage lastSessionMessage = aiPlanAgent.chat();
if (lastSessionMessage != null) {
    logger.info("结果: {}", lastSessionMessage.getData());
}
```

#### 14.2.2 流式串行工作流

流式模式与同步模式结构一致，只需将 `chat()` 替换为 `chatStream()`：

```java
Flux<ServerEvent> flux = aiPlanAgent.chatStream();

StringBuilder completeAnswer = new StringBuilder();
CountDownLatch countDownLatch = new CountDownLatch(1);

flux.doOnNext(event -> {
    if (event.getData() != null) {
        System.out.print(event.getData());
    }
    if (!event.isDone() && event.getData() != null) {
        completeAnswer.append(event.getData());
    } else {
        if (completeAnswer.length() > 0) {
            chatAgentMessage.addAgentResultSessionMessage(
                completeAnswer.toString(), event.getAgent());
            completeAnswer.setLength(0);
        }
    }
})
.doOnComplete(() -> {
    logger.info("\n=== 流完成 ===");
    countDownLatch.countDown();
})
.doOnError(error -> {
    logger.error("错误: " + error.getMessage(), error);
    countDownLatch.countDown();
})
.subscribe();

countDownLatch.await();
```

#### 14.2.3 条件分支节点

可以为节点添加条件触发器，控制节点是否执行：

```java
import org.frameworkset.tran.jobflow.script.TriggerScriptAPI;
import org.frameworkset.tran.jobflow.context.NodeTriggerContext;

IntegerCount integerCount = new IntegerCount();

// 为 introduceProvinces 节点添加条件：仅第一次执行时触发
aiPlanAgent.addConditionFlowNode(introduceProvinces, new TriggerScriptAPI() {
    @Override
    public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
        int i = integerCount.increament();
        return i == 1;
    }
});
```

### 14.3 并行任务编排

并行智能体（`AIParrelAgent`）将多个子智能体并发执行，所有子任务完成后自动聚合结果。

#### 14.3.1 同步并行工作流

```java
// 构建工作流智能体
AIPlanAgent aiPlanAgent = new AIPlanAgent(storeContext)
    .setAgentMessage(chatAgentMessage)
    .setAgentName("工作流智能体").setAgentId("workflowAgent");

// 添加初始节点
aiPlanAgent.addAgent(new AINodeAgent("用200字介绍中国有多少个省份和直辖市")
    .setAgentName("介绍中国省份和直辖市")
    .setAgentId("introduceProvinces"));

// 构建并行智能体
AIParrelAgent aiParrelAgent = new AIParrelAgent(aiPlanAgent)
    .setAgentId("aiParrelAgent")
    .setAgentName("共享任务节点");

// 添加多个并行子智能体
aiParrelAgent.addAgent(new AINodeAgent("用50字介绍湖南")
    .setAgentId("jieshaohunan")
    .setAgentName("用50字介绍湖南"));
aiParrelAgent.addAgent(new UserNodeAgent("用50字介绍湖北")
    .setAgentId("jieshaohubei")
    .setAgentName("用50字介绍湖北"));
aiParrelAgent.addAgent(new UserNodeAgent("用50字介绍江西")
    .setAgentId("jieshaojiangxi")
    .setAgentName("用50字介绍江西"));

// 将并行智能体加入主工作流
aiPlanAgent.addAgent(aiParrelAgent);

// 执行
LastSessionMessage result = aiPlanAgent.chat();
```

#### 14.3.2 流式并行工作流

```java
Flux<ServerEvent> flux = aiPlanAgent.chatStream();

StringBuilder completeAnswer = new StringBuilder();
CountDownLatch countDownLatch = new CountDownLatch(1);

flux.doOnSubscribe(subscription -> logger.info("开始订阅流..."))
    .doOnNext(event -> {
        // 首条消息可添加扩展链接
        if (event.isFirst() && !event.isToolCallResponse()) {
            event.addExtendData("url", "https://www.bbossgroups.com");
            event.addExtendData("title", "bboss官网");
        }

        if (event.isDone()) {
            event.addExtendData("url", "https://www.bbossgroups.com");
            event.addExtendData("title", "bboss官网");
        }

        if (event.getData() != null) {
            System.out.print(event.getData());
        }

        if (event.isToolCallsType()) {
            System.out.println("\n开始执行工具：");
        }

        if (event.isDone() || event.finished()) {
            System.out.println();
        }

        if (!event.isDone()) {
            if (event.getData() != null) {
                completeAnswer.append(event.getData());
            }
        } else {
            if (completeAnswer.length() > 0) {
                chatAgentMessage.addAgentResultSessionMessage(
                    completeAnswer.toString(), event.getAgent());
                completeAnswer.setLength(0);
            }
        }
    })
    .doOnComplete(() -> {
        logger.info("\n=== 流完成 ===");
        countDownLatch.countDown();
    })
    .doOnError(error -> {
        logger.error("错误: " + error.getMessage(), error);
        countDownLatch.countDown();
    })
    .subscribe();

countDownLatch.await();
```

#### 14.3.3 并行结果聚合

并行智能体执行完毕后，会自动调用 `buildResult()` 方法聚合所有子智能体的结果。默认格式为：

```java
agentId1:子智能体1的结果
agentId2:子智能体2的结果
```

如需自定义聚合格式，可重载 `buildResult()` 方法：

```java
AIParrelAgent aiParrelAgent = new AIParrelAgent(aiPlanAgent) {
    @Override
    public String buildResult(List<LastSessionMessage> lastSessionMessages) {
        StringBuilder builder = new StringBuilder();
        for (LastSessionMessage msg : lastSessionMessages) {
            if (builder.length() > 0) {
                builder.append("\n---\n");
            }
            builder.append("[").append(msg.getMsgAgentId()).append("]\n")
                   .append(msg.getData());
        }
        return builder.toString();
    }
};
```

### 14.4 条件任务节点和触发器

通过条件任务节点，可以在工作流或者并行/串行分支中，添加多个候选条件任务分支节点以及一个默认分支节点（可行）,每个条件任务分支节点需指定特定的条件触发器。

流程调度执行时，会从所以条件分支任务节点选择满足触发条件的任务分支节点执行，如果没有满足的节点，则会选择默认分支节点执行；如果既没有满足条件的条件分支节点，也没有设置默认分支节点，则会根据控制参数allCondtionNodeMatchedfailedContinue的值，来判别是否继续执行条件节点后面的任务节点（如果有），true，则执行，false不执行，直接终止当前条件节点所在流程或者并行/串行分支的执行。

基于条件任务节点，可以实现：

1. if-elseif-else条件决策树编排
2. 有限循环图智能体流程编排
3. 路由智能体编排

#### 14.4.1 简单条件任务分支

如果选择的智能体id为docAgent，则执行飞书文档智能体

如果选择的智能体id为logAgent，则执行日志分析智能体

如果没有选择具体的智能体，则执行默认智能体，直接回答用户问题

```java
//添加一个简单的决策智能体节点，在流程上下文中添加路由的agentId
planAgent.addAgent(new AIFlowNode() {
            /**
             * 由子类继承和实现
             *
             * @param jobFlowNodeExecuteContext
             * @return
             */
            @Override
            public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                jobFlowNodeExecuteContext.addJobFlowContextData("routeChoice","docAgent");//模拟设置后续节点id
                return null;
            }
        });
        ToolsRegist feishuMcp = new FeishuMcpRegist("feishumcp");
        planAgent.addConditionFlowNode(new UserNodeAgent(feishuMcp).setAgentId("docAgent").setAgentName("飞书文档智能体"),
                nodeTriggerContext -> {
                    String agentId = (String) nodeTriggerContext.getFlowContextData("routeChoice",true);
                    
                    if(agentId != null && agentId.equals("docAgent")){
                        return true;
                    }
                    return false;
                });
        planAgent.addConditionFlowNode(new UserNodeAgent().setAgentId("logAgent").setAgentName("日志分析智能体"),
                nodeTriggerContext -> {
                    String agentId = (String) nodeTriggerContext.getFlowContextData("routeChoice",true);
                    
                    if(agentId != null && agentId.equals("logAgent")){
                        return true;
                    }
                    return false;
                });
//添加默认条件分支节点
        planAgent.addConditionFlowNode(new UserNodeAgent( ).setAgentId("defaultAgent").setAgentName("默认智能体直接回答问题"),true);
//开始对话，执行对话流程，并返回会话结果
        Flux<ServerEvent> flux = planAgent.chatStream();
```

#### 14.4.2 条件分支后开启新条件分支

可以通过以下系列addAnotherConditionJobFlowNodeAgent方法，在当前条件分支后开启一个新的条件分支：

```java
public String addAnotherConditionJobFlowNodeAgent(AppendToParentAgent baseNodeAgent, NodeTrigger conditionNodeTrigger)
public String addAnotherConditionJobFlowNodeAgent(AppendToParentAgent baseNodeAgent, NodeTrigger conditionNodeTrigger,boolean defaultConditionNode)    
public String addAnotherConditionJobFlowNodeAgent(boolean allCondtionNodeMatchedfailedContinue,AppendToParentAgent baseNodeAgent, TriggerScriptAPI conditionNodeTrigger)
public String addAnotherConditionJobFlowNodeAgent(boolean allCondtionNodeMatchedfailedContinue,AppendToParentAgent baseNodeAgent, TriggerScriptAPI conditionNodeTrigger,boolean defaultConditionNode)    
    
```

注意调用addAnotherConditionJobFlowNodeAgent方法后，表示开启新的条件任务节点，并添加第一个分支，后续条件分支节点则通过addConditionFlowNode方法添加即可。

#### 14.4.3 实现有向循环图

基于条件分支节点，在通过addConditionFlowNode方法和addAnotherConditionJobFlowNodeAgent方法添加条件分支节点时，可以指定为当前流程或者并行、串行分支流程的前序节点（案例中为introduceProvinces）或者后序节点，从而实现有向循环图流程编排功能：

**跳转到前序节点**

```java
AIPlanAgent planAgent = new AIPlanAgent(new StoreContext()
        .setSessionId(sessionId).setUserId("user123").setSessionSize(100)                 
        .setStoreType(StoreContext.STORE_TYPE_DB).setRequestId("request123")
        .setDataSource("visualops"))
        .setAgentMessage(chatAgentMessage)
        .setAgentName("工作流智能体").setAgentId("workflowAgent")
         ;
AIBaseNodeAgent introduceProvinces = new AINodeAgent("用200字介绍中国有多少个省份和直辖市").setAgentName("介绍中国省份和直辖市").setAgentId("introduceProvinces");
planAgent.addAgent(introduceProvinces);
//构建并行智能体
AIParrelAgent aiParrelAgent = new AIParrelAgent(planAgent).setAgentId("aiParrelAgent").setAgentName("并行智能体");
aiParrelAgent.addAgent(new AINodeAgent("用50字介绍湖南，并且和介绍中国省份和直辖市内容合并输出").setAgentId("jieshaohunan").setAgentName("用50字介绍湖南"));
aiParrelAgent.addAgent(new UserNodeAgent("用50字介绍湖北").setAgentId("jieshaohubei").setAgentName("用50字介绍湖北"));
aiParrelAgent.addAgent(new UserNodeAgent("用50字介绍江西").setAgentId("jieshaojiangxi").setAgentName("用50字介绍江西"));
aiParrelAgent.addAgent(new UserNodeAgent("将下面的文字翻译为英文（不要回答问题）：用50字介绍江西").setAgentId("translate").setAgentName("将文字翻译为英文"));
planAgent.addAgent(aiParrelAgent);
IntegerCount integerCount = new IntegerCount();
//直接指向前序节点introduceProvinces，实现有条件循环，多循环一次，就结束循环，第一个参数为true，表示循环结束后继续执行流程后续节点
planAgent.addConditionFlowNode(true,introduceProvinces, new TriggerScriptAPI() {
    @Override
    public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
        int i = integerCount.increament();
        if(i == 1) {
            return true;
        }
        else{
            return false;
        }
    }
});
//下面给流程增加了两个通用节点
planAgent.addAgent(new AIFlowNode() {
            @Override
            public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
                logger.info("call 自定义节点1。");
                jobFlowNodeExecuteContext.addJobFlowContextData("customNode", "customNodeData");
                return null;
            }
        });

        planAgent.addAgent(new AIFlowNode() {
            @Override
            public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
                logger.info("call 自定义节点2。");
                logger.info("call 自定义节点2。customNode:{}", jobFlowNodeExecuteContext.getJobFlowContextData("customNode"));
                return null;
            }
        });
```

**跳转到后续节点**

```java
//定义会话实体：设置模型、maas平台，用户问题
ChatAgentMessage chatAgentMessage = new ChatAgentMessage()                            
        .setModel(model).setThinking(false)
        .setMaas(maas).setPrompt(prompt);

//定义工作流智能体，设置会话存储机制为DB，设置DB数据源、当前会id以及用户id
// 设置短期会话窗口
AIPlanAgent planAgent = new AIPlanAgent(new StoreContext()
        .setSessionId(sessionId).setUserId("user123").setSessionSize(100)                 
        .setStoreType(StoreContext.STORE_TYPE_DB).setRequestId("request123")
        .setDataSource("visualops"))
        .setAgentMessage(chatAgentMessage)
        .setAgentName("工作流智能体").setAgentId("workflowAgent")
         ;
AIBaseNodeAgent introduceProvinces = new AINodeAgent("用200字介绍中国有多少个省份和直辖市").setAgentName("介绍中国省份和直辖市").setAgentId("introduceProvinces");
planAgent.addAgent(introduceProvinces);
planAgent.addAgent(new AIFlowNode() {
    /**
     * 由子类继承和实现
     *
     * @param jobFlowNodeExecuteContext
     * @return
     */
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
        //生成一个10以内的随机整数，如果随机数是偶数则触发节点
        int randomInt = (int) (Math.random() * 10);
        logger.info("randomInt:{}", randomInt);
        jobFlowNodeExecuteContext.addJobFlowContextData("randomInt", randomInt);
        return null;
    }
});
//1.定义一个后序条件跳转节点
AIFlowNode aiFlowNode = new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
        logger.info("call 自定义节点customNode（满足条件时，会直接跳转到这个节点执行，绕过aiParrelAgent和循环跳转节点introduceProvinces，直接执行aiFlowNode后续流程节点）。");
        jobFlowNodeExecuteContext.addJobFlowContextData("customNode", "customNodeData");
        return null;
    }
};
//2.添加后序条件跳转节点
planAgent.addConditionFlowNode(true,aiFlowNode, new TriggerScriptAPI() {
    @Override
    public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
        int randomInt = (int) nodeTriggerContext.getFlowContextData("randomInt");
        
        if (randomInt % 2 == 0) {
            return true;
        }
        return false;
    }
});

AIFlowNode aiFlowNode1 = new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
        logger.info("call 自定义节点customNode1。");
        jobFlowNodeExecuteContext.addJobFlowContextData("customNode", "customNodeData1");
        return null;
    }
};
planAgent.addConditionFlowNode(aiFlowNode1, new TriggerScriptAPI() {
    @Override
    public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
        //生成一个10以内的随机整数，如果随机数是奇数则触发节点
        int randomInt = (int) nodeTriggerContext.getFlowContextData("randomInt");
        if (randomInt % 2 != 0) {
            return true;
        }
        return false;
    }
});

AIFlowNode aiFlowNode2 = new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
        logger.info("call 自定义节点customNode2。");
        jobFlowNodeExecuteContext.addJobFlowContextData("customNode", "customNodeData2");
        return null;
    }
};
planAgent.addConditionFlowNode(aiFlowNode2, true);
//构建并行子智能体
AIParrelAgent aiParrelAgent = new AIParrelAgent(planAgent).setAgentId("aiParrelAgent").setAgentName("并行智能体");
aiParrelAgent.addAgent(new AINodeAgent("用50字介绍湖南，并且和介绍中国省份和直辖市内容合并输出").setAgentId("jieshaohunan").setAgentName("用50字介绍湖南"));
aiParrelAgent.addAgent(new UserNodeAgent("用50字介绍湖北").setAgentId("jieshaohubei").setAgentName("用50字介绍湖北"));
aiParrelAgent.addAgent(new UserNodeAgent("用50字介绍江西").setAgentId("jieshaojiangxi").setAgentName("用50字介绍江西"));
aiParrelAgent.addAgent(new UserNodeAgent("将下面的文字翻译为英文（不要回答问题）：用50字介绍江西").setAgentId("translate").setAgentName("将文字翻译为英文"));
planAgent.addAgent(aiParrelAgent);
IntegerCount integerCount = new IntegerCount();
planAgent.addConditionFlowNode(true,introduceProvinces, new TriggerScriptAPI() {
    @Override
    public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
        int i = integerCount.increament();
        if(i == 1) {
            return true;
        }
        else{
            return false;
        }
    }
});
//3.添加后序条件跳转节点到主流程中，前面的条件分支节点会直接跳转的本节点
planAgent.addAgent(aiFlowNode);
planAgent.addAgent(new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
        logger.info("call 自定义节点3。");
        jobFlowNodeExecuteContext.addJobFlowContextData("customNode", "customNodeData3");
        return null;
    }
});

planAgent.addAgent(new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
        logger.info("call 自定义节点4。customNode:{}", jobFlowNodeExecuteContext.getJobFlowContextData("customNode"));
        return null;
    }
});


//开始对话，执行对话流程，并返回会话结果   
LastSessionMessage lastSessionMessage = planAgent.chat();

//输出会话结果        
if(lastSessionMessage != null) {
    String data = lastSessionMessage.getData();
    logger.info("serverEvent:{}", data);
}
```

说明：通过条件跳转到流程或者并行分支/串行分支后序节点，可以成功绕过部分中间节点

```java
//1.定义一个后序条件跳转节点
AIFlowNode aiFlowNode = new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext)   {
        logger.info("call 自定义节点customNode（满足条件时，会直接跳转到这个节点执行，绕过aiParrelAgent和循环跳转节点introduceProvinces，直接执行aiFlowNode后续流程节点）。");
        jobFlowNodeExecuteContext.addJobFlowContextData("customNode", "customNodeData");
        return null;
    }
};

//2.添加后序条件跳转节点
planAgent.addConditionFlowNode(true,aiFlowNode, new TriggerScriptAPI() {
    @Override
    public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
        int randomInt = (int) nodeTriggerContext.getFlowContextData("randomInt");
        
        if (randomInt % 2 == 0) {
            return true;
        }
        return false;
    }
});

。。。。。。中间节点。。。。。
    
//3.添加后序条件跳转节点到主流程中，前面的条件分支节点会直接跳转的本节点
planAgent.addAgent(aiFlowNode);
。。。。。。后续节点。。。。。
```

##### 14.4.3.1 关键说明

通过以下方法添加条件跳转节点，如果条件不成立，会自动终止执行条件节点的后续节点执行：

1）如果条件节点是在工作流也就是终止整个工作流

2）如果条件节点是在串行分支中，则会终止串行分支节点执行

```java
//2.添加后序条件跳转节点
        planAgent.addConditionFlowNode(AppendToParentAgent aiAgent , TriggerScriptAPI conditionNodeTrigger)
```

通过以下方法添加条件跳转节点，如果条件不成立，当allCondtionNodeMatchedfailedContinue为true时，会自动执行条件节点的后续节点，不会终止条件节点所在的主干流程或者串行分支流程：

```java
//2.添加后序条件跳转节点
        planAgent.addConditionFlowNode(boolean allCondtionNodeMatchedfailedContinue,AppendToParentAgent aiAgent , TriggerScriptAPI conditionNodeTrigger)
```

有限循环图可以基于条件节点来实现的关键点，就在于**allCondtionNodeMatchedfailedContinue**参数，当allCondtionNodeMatchedfailedContinue取值为true时，我们指定条件节点为流程中或者串行分支中之前的节点，如果conditionNodeTrigger触发器判定条件成立，则继续跳转到导之前的节点重复执行，形成循环；如果判定条件不成立，则结束循环，继续执行后续流程节点。

#### 14.4.4 注意事项

- 条件节点只能在当前主干流程节点间跳转，不能跳转到分支复合节点内部

- 条件节点只能在串行分支流程节点之间跳转，不能跳转分支复合节点内部或者分支外部

- 条件节点只能在并行分支中并行分支流程节点之间跳转，不能跳转分支复合节点内部或者分支外部

- 可以为条件节点设置条件触发器，节点可以加入到多个条件分支，每个分支都可以指定特定条件触发器，如果没有指定，则使用节点自身的条件触发器，条件节点必须指定自己的条件触发器

### 14.5 路由智能体

路由智能体（`AIRouteAgent`和AIKeywordsRouteAgent）基于条件任务节点实现，能够根据用户问题自动选择后续执行路径，实现智能分发。典型的**意图识别+路由分发**的AI架构模式，利用 LLM + 关键词规则，自动识别用户意图，然后决定走哪条处理分支，实现意图识别与路由分发机制。

#### 14.5.1 同步路由工作流

```java
AIPlanAgent aiPlanAgent = new AIPlanAgent(storeContext)
    .setAgentMessage(chatAgentMessage)
    .setAgentName("工作流智能体").setAgentId("workflowAgent");

// 构建路由规则智能体
aiPlanAgent.addAgent(new AIRouteAgent()
    .setAgentId("Router")
    .setAgentName("路由规则智能体")
    .setSystemPrompt("你是一个路由智能体。你的目标是将用户查询路由到正确的后续任务，注意你不需要回答用户的问题。")
    .addRoutingChoice("weatherAgent", "查询城市天气，并给出穿衣出行建议")
    .addRoutingChoice("docAgent", "操作飞书文档")
);

// 构建天气查询智能体
aiPlanAgent.addRouteChoiceAgent(new UserNodeAgent(new MCPToolsRegist("visualops"))
    .setAgentId("weatherAgent")
    .setAgentName("天气查询智能体"));

// 构建飞书文档操作智能体
ToolsRegist feishuMcp = new FeishuMcpRegist("feishumcp");
aiPlanAgent.addRouteChoiceAgent(new UserNodeAgent(feishuMcp)
    .setAgentId("docAgent")
    .setAgentName("飞书文档智能体"));

// 构建默认智能体：当用户问题匹配不上时执行
aiPlanAgent.addDefaultRouteChoiceAgent(
    new AINodeAgent().setAgentId("defaultAgent").setAgentName("默认智能体"));

// 构建裁判智能体：判断是否回答了问题
aiPlanAgent.addAgent(new AIJudgeAgent("评估结果是否回答了问题,回答请回复：是，否则回复：否")
    .setAgentId("judgeAgent")
    .setAgentName("评估智能体"));

// 构建条件节点：根据裁判结果决定是否创建飞书文档
aiPlanAgent.addAgent(
    new AINodeAgent("将结果创建为飞书文档", feishuMcp)
        .setAgentId("createDocAgent")
        .setAgentName("飞书文档创建智能体"),
    nodeTriggerContext -> {
        String judgeResult = (String) nodeTriggerContext
            .getFlowContextData("judgeAgent.judgeResult");
        return "是".equals(judgeResult);
    });

// 执行
LastSessionMessage result = aiPlanAgent.chat();
```

#### 14.5.2 流式路由工作流

流式路由与同步路由结构一致，只需调用 `chatStream()`：

```java
Flux<ServerEvent> flux = aiPlanAgent.chatStream();
// ... 流式处理逻辑与并行流式示例相同
```

#### 14.5.3 关键词路由智能体

```java
 planAgent.addAgent(new AIKeywordsRouteAgent()
				.addRoutingChoice("bothAgent", new String[]{"酒店和机票"}, "用户需要同时预定酒店和机票")
				.addRoutingChoice("hotelAgent", new String[]{"酒店"},"用户只需要预定酒店")
				.addRoutingChoice("flightAgent", new String[]{"航班","机票"}, "用户只需要预定机票")
				
                .setAgentId("bookingRouter").setAgentName("预定路由智能体")
                .setSystemPrompt("你是一个行程预定路由智能体。请分析用户的问题，判断用户需要预定什么，注意你不需要直接回答用户的问题，只需要做路由判断,使用json格式返回匹配的智能体信息")              
                
        );
```

AIKeywordsRouteAgent 是轻量级的关键词匹配方案，适合意图明确、关键词边界清晰的场景（如本例中的"酒店"、"机票"）。

### 14.6 通用流程节点

bboss提供了两种类型通用节点：AIFlowNode和AIFlowNodeVoid（二者区别：后者call方法无返回值，视情况选取），通用流程节点用于在工作流中执行纯 Java 代码逻辑，不调用大模型。适合数据转换、参数校验、外部接口调用、数据预处理、记录用户输入和创建用户会话等场景。

记录用户输入信息和创建会话：

```java
          planAgent.addAgent(new AIFlowNodeVoid(){
             
             @Override
             public void call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                // ===== 2. 加载历史会话消息，如果会话不存在，则创建 =====
                planAgent.loadSessionMemory( question, domain);
                TraceMessage traceMessage = new TraceMessage();       
                //记录用户输入的原始问题
                traceMessage.setMessage(Map.of("question", question,"role",SessionMessage.MESSAGE_TYPE_USERINPUTMESSAGE_NAME));
                //其他用户上传的附件材料信息可以放到metaData中,也可以直接放到上面的消息中
//              traceMessage.setMetaData(Map.of("documents", new ArrayList<>()));
                traceMessage.setStartTime(System.currentTimeMillis());
//              traceMessage.setEndTime(System.currentTimeMillis());
                planAgent.recordTraceMessage(traceMessage);
 
                
                
             }
          });
```

其他应用案例：

 

```java
import org.frameworkset.spi.ai.flow.AIFlowNode;
import org.frameworkset.tran.jobflow.context.JobFlowNodeExecuteContext;

aiPlanAgent.addAgent(new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
        logger.info("call 自定义节点1。");
        // 向工作流上下文写入数据
        jobFlowNodeExecuteContext.addJobFlowContextData("customNode", "customNodeData");
        return null;
    }
});

aiPlanAgent.addAgent(new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
        logger.info("call 自定义节点2。");
        // 从工作流上下文读取数据
        Object data = jobFlowNodeExecuteContext.getJobFlowContextData("customNode");
        logger.info("customNode:{}", data);
        return null;
    }
});

aiPlanAgent.addAgent(new AIFlowNodeVoid() {
    @Override
    public void call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
        logger.info("call 自定义节点2。");
        // 从工作流上下文读取数据
        Object data = jobFlowNodeExecuteContext.getJobFlowContextData("customNode");
        logger.info("customNode:{}", data);
        
});
```

通用流程节点通过 `JobFlowNodeExecuteContext` 实现与上下游节点的数据交互：

- `addJobFlowContextData(key, value)`：向工作流上下文写入键值对数据
- `getJobFlowContextData(key)`：从工作流上下文读取数据

### 14.7 智能体配置参数

### 14.8 工作流组合模式

串行和并行智能体可以相互嵌套，构建复杂的工作流：

```java
// 串行智能体中嵌入并行智能体
AISequenceAgent sequenceAgent = new AISequenceAgent(aiPlanAgent);

AIParrelAgent innerParrel = new AIParrelAgent(aiPlanAgent);
innerParrel.addAgent(new AINodeAgent("任务A"));
innerParrel.addAgent(new AINodeAgent("任务B"));

sequenceAgent.addAgent(innerParrel);  // 并行作为串行的一个步骤
sequenceAgent.addAgent(new AINodeAgent("任务C"));

aiPlanAgent.addAgent(sequenceAgent);

// 并行智能体中嵌入串行智能体
AIParrelAgent parrelAgent = new AIParrelAgent(aiPlanAgent);

AISequenceAgent innerSequence = new AISequenceAgent(aiPlanAgent);
innerSequence.addAgent(new AINodeAgent("步骤1"));
innerSequence.addAgent(new AINodeAgent("步骤2"));

parrelAgent.addAgent(innerSequence);
parrelAgent.addAgent(new AINodeAgent("独立任务"));

aiPlanAgent.addAgent(parrelAgent);
```

### 14.9工作流节点类型说明

| 节点类型 | 继承类 | 特点 |
|---------|--------|------|
| AI节点 | `AINodeAgent` | 标准AI智能体，自动引用父智能体会话记忆 |
| 用户节点 | `UserNodeAgent` | 不引用上游父智能体记忆，适合独立任务或工具调用 |
| 路由节点 | `AIRouteAgent` | 负责路由决策，不直接回答问题 |
| 裁判节点 | `AIJudgeAgent` | 评估结果质量，输出判断结论 |
| 通用流程节点 | `AIFlowNode` | 执行纯Java自定义逻辑，不调用大模型，可在工作流中加工和传递数据、记录流程过程数据 |

### 14.10 智能体工作流变量体系

智能体工作流支持丰富的变量传递机制，支持在智能体之间传递数据，并在 Prompt 中动态引用变量。

#### 14.10.1 变量概述

工作流中的变量用于实现智能体之间的数据共享和传递，主要包含以下使用场景：

- **输出变量**：将某个智能体的执行结果保存为变量，供后续节点使用
- **Prompt 变量引用**：在提示词中通过占位符动态注入其他节点的输出结果
- **内置变量**：框架自动注入的流程级变量，如用户输入、系统提示等
- **上下文变量**：通过 `JobFlowNodeExecuteContext` 或 `NodeTriggerContext` 在代码层面读写

#### 14.10.2 设置输出变量

通过 `setOutputVaribleName` 方法将智能体的输出结果保存为指定名称的变量，并可指定作用域：

```java
import org.frameworkset.spi.ai.model.AIFlowConst;

aiPlanAgent.addAgent(new AINodeAgent("用200字介绍中国有多少个省份和直辖市")
    .setOutputVaribleName("provinces", AIFlowConst.AIFLOW_VAR_SCOPE_FLOW)
    .setAgentName("介绍中国省份和直辖市")
    .setAgentId("introduceProvinces"));
```

| 参数 | 说明 |
|------|------|
| `varName` | 变量名称，后续通过该名称引用 |
| `scope` | 变量作用域，可选 `AIFlowConst.AIFLOW_VAR_SCOPE_FLOW`（全局）、`AIFLOW_VAR_SCOPE_NODE`（节点级）、`AIFLOW_VAR_SCOPE_CONTAINER`（容器级） |

#### 14.10.3 变量作用域

| 作用域常量 | 说明 |
|-----------|------|
| `AIFLOW_VAR_SCOPE_FLOW` | 全局作用域，整个工作流均可访问 |
| `AIFLOW_VAR_SCOPE_NODE` | 节点作用域，仅在当前节点内有效 |
| `AIFLOW_VAR_SCOPE_CONTAINER` | 容器作用域，仅在当前串行/并行容器内有效 |

#### 14.10.4 在 Prompt 中使用变量

在智能体的提示词中，通过 `#[变量名,scope=作用域]` 的语法引用已定义的变量：

```java
AIParrelAgent aiParrelAgent = new AIParrelAgent(aiPlanAgent)
    .setAgentId("aiParrelAgent")
    .setAgentName("共享任务节点");

// 在 Prompt 中引用 flow 作用域的 provinces 变量
aiParrelAgent.addAgent(new AINodeAgent(
    "同时结合中国省份特点：\r\n#[provinces,scope=flow],\r\n用300字介绍湖南")
    .setAgentId("jieshaohunan")
    .setAgentName("用50字介绍湖南"));
```

**变量引用语法：**

```
#[变量名,scope=作用域]
```

其中 `scope` 可选值为：`flow`、`node`、`container`，需与定义变量时指定的作用域一致。

#### 14.10.5 在代码中获取变量

在条件触发器或通用流程节点中，可通过上下文对象获取变量值：

**条件触发器中获取变量：**
```java
aiPlanAgent.addAgent(new AINodeAgent("将结果创建为飞书文档", feishuMcp)
        .setAgentId("createDocAgent")
        .setAgentName("飞书文档创建智能体"),
    nodeTriggerContext -> {
        // 获取 judgeAgent 智能体的裁判结果
        String judgeResult = (String) nodeTriggerContext
            .getFlowContextData("judgeAgent.judgeResult");
        return "是".equals(judgeResult);
    });
```

**通用流程节点中获取变量：**
```java
aiPlanAgent.addAgent(new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
        Object data = jobFlowNodeExecuteContext.getJobFlowContextData("customNode");
        return data;
    }
});
```

#### 14.10.6 内置变量

框架自动维护以下内置变量：

| 内置变量 | 说明 |
|---------|------|
| `input.query` | 用户输入的原始问题（Prompt） |
| `input.system` | 系统提示词（System Prompt） |
| `judgeAgent.judgeResult` | `AIJudgeAgent` 裁判节点的评估结果 |

#### 14.10.7 完整示例

以下示例演示了从变量定义、Prompt 引用到代码获取的完整流程：

```java
// 1. 定义输出变量：将省份介绍结果保存为 flow 级变量
aiPlanAgent.addAgent(new AINodeAgent("用200字介绍中国有多少个省份和直辖市")
    .setOutputVaribleName("provinces", AIFlowConst.AIFLOW_VAR_SCOPE_FLOW)
    .setAgentId("introduceProvinces"));

// 2. 在并行智能体中通过 Prompt 引用变量
AIParrelAgent aiParrelAgent = new AIParrelAgent(aiPlanAgent);
aiParrelAgent.addAgent(new AINodeAgent(
    "结合省份特点：#[provinces,scope=flow]，用300字介绍湖南")
    .setAgentId("jieshaohunan"));

// 3. 通用流程节点处理变量
aiPlanAgent.addAgent(new AIFlowNode() {
    @Override
    public Object call(JobFlowNodeExecuteContext ctx) {
        String provinces = (String) ctx.getJobFlowContextData("provinces");
        logger.info("获取到省份数据：{}", provinces);
        ctx.addJobFlowContextData("processed", true);
        return null;
    }
});
```

### 14.11 评估智能体

```java
//构建裁判智能体：判断是否回答了问题，可以指定评估提示词，通过变量一样问题和问题答案
aiPlanAgent.addAgent(new AIJudgeAgent("评估结果是否回答了问题:\n#[input.query,scope=node]\n# 问题答案：\n#[answer,scope=node],回答请回复：是，否则回复：否").setAgentId("judgeAgent").setAgentName("评估智能体"));

//构建裁判智能体：如果不指定提示词，则使用默认提示词

aiPlanAgent.addAgent(new AIJudgeAgent().setAgentId("judgeAgent").setAgentName("评估智能体"));
```

### 14.12 流程调度

bboss提供了智能体流程一次性执行和定期调度执行机制，可以设置节假日忽略执行策略，先看一个完整示例：

```java
private static void initDeepseekService(){
      
        HttpRequestProxy.startHttpPools("application.properties");//启动模型服务服务

    }
    public static void main(String[] args){
        //初始化Deepseek服务
        initDeepseekService();
        ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
        chatAgentMessage.setModel("deepseek-chat").setMaas("deepseek");

        //构建流程
        AIPlanAgent planAgent = new AIPlanAgent(new StoreContext().setStoreType(StoreContext.STORE_TYPE_MEMORY).setSessionSize(100));
        planAgent.setAgentName("Deepseek写诗-评价诗词流程")
                .setAgentId("测试id").setAgentMessage(chatAgentMessage);
        
        HolidayJobFlowScheduleConfig jobFlowScheduleConfig = new HolidayJobFlowScheduleConfig();
        jobFlowScheduleConfig.setDelay(1000L);//延期1秒执行
        jobFlowScheduleConfig.setFixedRate(true);
        jobFlowScheduleConfig.setPeriod(30000L);//每隔30秒执行
        jobFlowScheduleConfig.addCustomHoliday("2026-06-10");//设定某天不执行，可以调用多次添加多天不执行
        
        planAgent.setJobFlowScheduleConfig(jobFlowScheduleConfig);

        planAgent.addAgent(new AINodeAgent("模仿李白的风格写一首七律.飞机!").setSystemPrompt("你是一位唐代诗人.").setAgentId("1").setAgentName("Deepseek写诗-评价诗词流程").setAgentOutput(new AgentOutput() {
            @Override
            public void output(ServerEvent message) {
                logger.info("--------诗歌内容---------\n{}",message.getData());
            }
        }));

        planAgent.addAgent(new AINodeAgent("帮忙评估上述诗词的意境").setAgentId("2").setAgentName("Deepseek-chat-分析诗").setAgentOutput(new AgentOutput() {
            @Override
            public void output(ServerEvent message) {
                logger.info("--------诗歌评价---------\n{}",message.getData());
            }
        }));


        planAgent.chat();
         
        

    }
```

bboss默认采用一次性执行智能体编排工作流，如需周期性执行，则进行以下配置：


```java
    HolidayJobFlowScheduleConfig jobFlowScheduleConfig = new HolidayJobFlowScheduleConfig();
    jobFlowScheduleConfig.setDelay(1000L);//延期1秒执行
    jobFlowScheduleConfig.setFixedRate(true);
    jobFlowScheduleConfig.setPeriod(30000L);//每隔30秒执行
    jobFlowScheduleConfig.addCustomHoliday("2026-06-10");//设定某天不执行，可以调用多次添加多天不执行
    jobFlowScheduleConfig.setSkipSunday(true);
    planAgent.setJobFlowScheduleConfig(jobFlowScheduleConfig);
```
如果需要跳过节假日不执行，则进行如下设置：

```java
jobFlowScheduleConfig.addCustomHoliday("2026-06-10");//设定某天不执行，可以调用多次添加多天不执行
/**
 * 设置跳过星期六
 
 */
jobFlowScheduleConfig.skipSaturday() 

/**
 * 设置跳过星期天
 * @return
 */
jobFlowScheduleConfig.skipSunday() 

/**
 * 设置跳过周末（星期六和星期天）
 * @return
 */
jobFlowScheduleConfig.skipWeekends() 

/**
 * 设置跳过元旦（1月1日）
 * @return
 */
jobFlowScheduleConfig.skipNewYearsDay() 

/**
 * 设置跳过劳动节（5月1日）
 * @return
 */
jobFlowScheduleConfig.skipLaborDay() 

/**
 * 设置跳过端午节
 * @return
 */
jobFlowScheduleConfig.skipDragonBoatFestival() 

/**
 * 设置跳过中秋节
 * @return
 */
jobFlowScheduleConfig.skipMidAutumnFestival() 

/**
 * 设置跳过国庆节（10月1日）
 * @return
 */
jobFlowScheduleConfig.skipNationalDay() 

/**
 * 设置跳过春节
 * @return
 */
jobFlowScheduleConfig.skipSpringFestival() 

/**
 * 设置跳过所有内置节假日（元旦、劳动节、端午节、中秋节、国庆节、春节）及周末
 * @return
 */
jobFlowScheduleConfig.skipAllHolidays() 
```

### 14.13 自定义消息推送

```java
ServerEvent serverEvent = new ServerEvent();//向客户端推送拒答信息
serverEvent.setType(ServerEvent.TYPE_REFUSAL);
serverEvent.setData(msg);
serverEvent.setConfidence(confidence);
serverEvent.setDone(true);//告诉前端流已经输出完毕
getAgentFluxSink().next(serverEvent);
```

### 14.14 流程轨迹记录

```java
// ===== 2. 加载历史会话消息，如果会话不存在，则创建 =====
					planAgent.loadSessionMemory( question, domain);
					TraceMessage traceMessage = new TraceMessage();		
					//记录用户输入的原始问题
					traceMessage.setMessage(Map.of("question", question,"role",SessionMessage.MESSAGE_TYPE_USERINPUTMESSAGE_NAME));
					//其他用户上传的附件材料信息可以放到metaData中,也可以直接放到上面的消息中
//					traceMessage.setMetaData(Map.of("documents", new ArrayList<>()));
					traceMessage.setStartTime(System.currentTimeMillis());
//					traceMessage.setEndTime(System.currentTimeMillis());
					planAgent.recordTraceMessage(traceMessage);
```



### 14.15 停止智能体工作流

可以通过以下方法停止智能体工作流的执行：

```java
aiPlanAgent.shutdown();
```

## 十五、向量模型与Rerank

bboss AI 内置了文本向量化（Embedding）和重排序（Rerank）能力，可配合 Elasticsearch 等向量数据库实现检索增强生成（RAG）。

### 15.1 向量模型 Embedding

bboss-ai 内置了统一的向量模型调用能力，通过 `AIAgent.embedding(EmbeddingMessage)` 方法即可将文本转换为高维向量。

#### 基本用法

```java
import org.frameworkset.spi.ai.AIAgent;
import org.frameworkset.spi.ai.model.EmbeddingMessage;

public float[] text2embedding(String text) {
    EmbeddingMessage embeddingMessage = new EmbeddingMessage();
    embeddingMessage.setInput(text);          // 设置将要向量化的数据
    embeddingMessage.setModel("bge-m3");      // 指定向量模型名称
    embeddingMessage.setMaas("embedding_model"); // 指定 MaaS 平台服务名

    AIAgent agent = new AIAgent();
    float[] embedding = agent.embedding(embeddingMessage);
    if (embedding == null) {
        throw new AIRuntimeException("Embedding failed: response is null");
    }
    return embedding;
}
```

#### 关键参数说明

| 参数 | 说明 |
|------|------|
| `input` | 待向量化的文本内容 |
| `model` | 向量模型名称，如 `bge-m3`、`bge-large-zh-v1.5` 等 |
| `maas` | MaaS 平台服务配置名，对应 `application.properties` 中的服务定义 |

> **提示**：向量模型服务需在 `application.properties` 中预先配置好对应的 MaaS 服务地址和认证信息。

---

### 15.2 Rerank 重排序

在 RAG（检索增强生成）场景中，向量检索和 BM25 文本检索返回的结果往往存在排序差异。`AIAgent.rerank(RerankMessage)` 方法调用专业的 Rerank 模型对候选文档进行重新排序，显著提升检索结果的相关性。

#### 基本用法

```java
import org.frameworkset.spi.ai.AIAgent;
import org.frameworkset.spi.ai.model.RerankMessage;
import org.frameworkset.spi.ai.model.RerankDocument;
import org.frameworkset.spi.ai.model.RerankedDocument;

public List<RerankedDocument> rerankDocuments(String query, List<RerankDocument> candidates) {
    RerankMessage rerankMessage = new RerankMessage();
    rerankMessage.setMaas("agw");                       // MaaS 服务名
    rerankMessage.setModel("bge-reranker-v2-m3");  // Rerank 模型
    rerankMessage.setRerankDocuments(candidates);          // 候选文档列表
    rerankMessage.setQuery(query);                         // 用户查询
    rerankMessage.setReturnDocuments(false);               // 是否返回原始文本
    rerankMessage.setTopK(10);                             // 最多返回前 10 个最相关文档
    rerankMessage.setRelevanceScore(0.5d);                 // 只召回相似度大于该值的文档

    AIAgent aiAgent = new AIAgent();
    List<RerankedDocument> rerankedDocuments = aiAgent.rerank(rerankMessage);
    return rerankedDocuments;
}
```
#### RerankMessage 数据结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `maas` | `String` | MaaS 平台服务配置名，对应 `application.properties` 中的服务定义 |
| `model` | `String` | Rerank 模型名称，如 `bge-reranker-v2-m3` 等 |
| `query` | `String` | 用户查询语句，用于与候选文档计算相关性 |
| `rerankDocuments` | `List<RerankDocument>` | 候选文档列表，需预先通过向量检索或 BM25 检索构建 |
| `returnDocuments` | `boolean` | 是否在结果中返回原始文档内容；设为 `true` 可在结果中直接获取原始文本，设为 `false` 则仅返回分数和索引，减少数据传输。 |
| `topK` | `int` | 最多返回的文档数量，只召回相似度最高的前 `topK` 个文档 |
| `relevanceScore` | `double` | 相关性分数阈值，只召回相似度大于该值的文档 |

#### RerankDocument 数据结构

```java
RerankDocument doc = new RerankDocument();
doc.setDocument("文档原始文本内容");    // 设置文档内容
doc.setVectorScore(0.85f);               // 来自向量检索的分数（可选）
doc.setBm25Score(12.5f);                 // 来自 BM25 检索的分数（可选）
doc.setMetadata(metaMap);                // 附加元数据（可选）
```

#### RerankedDocument 返回结构

| 字段 | 说明 |
|------|------|
| `index` | 原始文档在候选列表中的位置 |
| `relevanceScore` | Rerank 模型计算的相关性分数 |
| `document` | 原始文档内容（当 `returnDocuments=true` 时返回） |


---

### 15.3 RAG 实战：混合检索 + Rerank

以下是一个完整的 RAG 检索流程示例，演示如何结合 Elasticsearch KNN 向量检索、BM25 文本检索和 Rerank 重排序，实现高质量的知识库问答。

#### 15.3.1 初始化与启动

```java
import org.frameworkset.elasticsearch.boot.ElasticSearchBoot;
import org.frameworkset.spi.remote.http.HttpRequestProxy;

// 启动 Elasticsearch 客户端和 HTTP 连接池
ElasticSearchBoot.boot("application-stream.properties");
HttpRequestProxy.startHttpPools("application-stream.properties");
```

#### 15.3.2 完整检索流程

```java
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.entity.MetaMap;
import org.frameworkset.spi.ai.AIAgent;
import org.frameworkset.spi.ai.model.*;

public void searchVectorAndRerank() {
    ClientInterface clientUtil = ElasticSearchHelper
            .getConfigRestClientUtil("esmapper/knowledge.xml");
    String query = "Spring AOP";

    // 1. 向量检索（KNN）
    Map vectorParams = new LinkedHashMap();
    vectorParams.put("embedding", text2embedding(query));
    vectorParams.put("k", 100);
    vectorParams.put("size", 50);
    vectorParams.put("similarity", 0.5);
    ESDatas<MetaMap> vectorDatas = clientUtil.searchList(
            "/knowledge_chunks/_search", "searchWithScore",
            vectorParams, MetaMap.class);
    List<MetaMap> vectorResults = vectorDatas.getDatas();

    // 2. BM25 文本检索
    Map bm25Params = new LinkedHashMap();
    bm25Params.put("query", query);
    bm25Params.put("size", 50);
    bm25Params.put("is_active", true);
    ESDatas<MetaMap> bm25Datas = clientUtil.searchList(
            "/knowledge_chunks/_search", "searchBM25",
            bm25Params, MetaMap.class);
    List<MetaMap> bm25Results = bm25Datas.getDatas();

    // 3. 合并结果（去重）
    Map<String, RerankDocument> mergedMap = new LinkedHashMap<>();
    if (vectorResults != null) {
        for (MetaMap metaMap : vectorResults) {
            String chunkId = (String) metaMap.get("chunk_id");
            RerankDocument doc = new RerankDocument();
            doc.setDocument((String) metaMap.get("content"));
            doc.setVectorScore(metaMap.getScore());
            doc.setMetadata(metaMap);
            mergedMap.put(chunkId, doc);
        }
    }
    if (bm25Results != null) {
        for (MetaMap metaMap : bm25Results) {
            String chunkId = (String) metaMap.get("chunk_id");
            RerankDocument doc = mergedMap.get(chunkId);
            if (doc == null) {
                doc = new RerankDocument();
                doc.setDocument((String) metaMap.get("content"));
                doc.setBm25Score(metaMap.getScore());
                doc.setMetadata(metaMap);
                mergedMap.put(chunkId, doc);
            } else {
                doc.setBm25Score(metaMap.getScore());
            }
        }
    }

    // 4. Rerank 重排序
    if (mergedMap != null && mergedMap.size() > 0) {
        List<RerankDocument> rerankDatas = new ArrayList<>(mergedMap.values());

        RerankMessage rerankMessage = new RerankMessage();
        rerankMessage.setMaas("agw");
        rerankMessage.setModel("bge-reranker-v2-m3");
        rerankMessage.setRerankDocuments(rerankDatas);
        rerankMessage.setQuery(query);
        rerankMessage.setReturnDocuments(false);
        rerankMessage.setTopK(3);                            // 最多返回前 3 个最相关文档
        rerankMessage.setRelevanceScore(0.8d);               // 只召回相似度大于 0.8 的文档

        AIAgent aiAgent = new AIAgent();
        List<RerankedDocument> rerankedDocuments = aiAgent.rerank(rerankMessage);

        // 按 relevanceScore 排序后的最终结果就是高质量检索结果
        logger.info("Rerank 结果: {}", JsonUtil.object2jsonPretty(rerankedDocuments));
    }
}
```

#### 流程说明

1. **向量检索**：通过 KNN 搜索获取语义相关的文档片段
2. **BM25 检索**：通过关键词匹配获取文本相关的文档片段
3. **结果合并**：以 `chunk_id` 为键去重合并，同时保留两种检索的分数
4. **Rerank 排序**：调用 Rerank 模型对合并后的候选集进行精细排序，返回按相关性排列的最终结果

> **混合检索方案**：Elasticsearch 8.x 也支持原生的混合检索（`knn` + `query` + `rank: { rrf }`），可通过 `searchHybrid` DSL 一步完成向量+BM25+RRF 融合，详见下面索引配置中的 `searchHybrid` 定义。

---

### 15.4 Elasticsearch 索引配置

以下是为知识库检索场景设计的 Elasticsearch 索引模板和检索 DSL 配置，需放置在 `esmapper/knowledge.xml` 中。

#### 15.4.1 索引结构与 Mapping

```xml
<property name="createKnowledgeChunksIndex">
    <![CDATA[{
      "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0,
        "analysis": {
          "filter": {
            "cn_stop": {
              "type": "stop",
              "stopwords": ["的", "了", "是", "在", "和", "或", "与", "对", "中", "也", "都", "就", "而"]
            }
          },
          "analyzer": {
            "cn_index_analyzer": {
              "type": "custom",
              "tokenizer": "ik_max_word",
              "filter": ["lowercase", "cn_stop"]
            },
            "cn_search_analyzer": {
              "type": "custom",
              "tokenizer": "ik_smart",
              "filter": ["lowercase", "cn_stop"]
            }
          }
        }
      },
      "mappings": {
        "properties": {
          "chunk_id":    { "type": "keyword" },
          "document_id": { "type": "keyword" },
          "batch_id":    { "type": "keyword" },
          "is_active":   { "type": "boolean" },
          "doc_path":    { "type": "keyword" },
          "anchor":      { "type": "keyword" },
          "domain":      { "type": "keyword" },
          "doc_type":    { "type": "keyword" },
          "heading_path": {
            "type": "text",
            "analyzer": "cn_index_analyzer",
            "search_analyzer": "cn_search_analyzer"
          },
          "content": {
            "type": "text",
            "analyzer": "cn_index_analyzer",
            "search_analyzer": "cn_search_analyzer",
            "fields": {
              "en": { "type": "text", "analyzer": "english" }
            }
          },
          "embedding": {
            "type": "dense_vector",
            "dims": 1024,
            "index": true,
            "similarity": "cosine"
          },
          "created_at": { "type": "date" }
        }
      }
    }]]>
</property>
```

#### 15.4.2 KNN 向量检索 DSL

```xml
<property name="searchWithScore">
    <![CDATA[{
       "size":#[size],
      "knn": {
        "field": "embedding",
        "query_vector": #[embedding,serialJson=true],
        "k": #[k],
        "similarity": #[similarity],
        "num_candidates": 100,
        "boost": 0.9
      }
    }]]>
</property>
```

#### 15.4.3 BM25 文本检索 DSL

```xml
<property name="searchBM25">
    <![CDATA[{
      "size": #[size],
      "query": {
        "bool": {
          "must": [
            {
              "multi_match": {
                "query": #[query],
                "fields": ["content^3", "heading_path^2"],
                "type": "best_fields"
              }
            }
          ],
          "filter": [
            { "term": { "is_active": #[is_active] } }
          ]
        }
      }
    }]]>
</property>
```

#### 15.4.4 混合检索（KNN + BM25 + RRF）DSL

```xml
<property name="searchHybrid">
    <![CDATA[{
      "size": #[size],
      "query": {
        "bool": {
          "must": [
            {
              "multi_match": {
                "query": #[query],
                "fields": ["content^3", "heading_path^2"],
                "type": "best_fields"
              }
            }
          ],
          "filter": [
            { "term": { "is_active": #[is_active] } }
          ]
        }
      },
      "knn": {
        "field": "embedding",
        "query_vector": #[embedding,serialJson=true],
        "k": #[k],
        "similarity": #[similarity],
        "num_candidates": 200,
        "boost": 0.5,
        "filter": [
          { "term": { "is_active": #[is_active] } }
        ]
      },
      "rank": {
        "rrf": {
          "window_size": 100,
          "rank_constant": 60
        }
      }
    }]]>
</property>
```

#### 索引配置要点

| 配置项 | 说明 |
|--------|------|
| `dense_vector` | `embedding` 字段类型，`dims: 1024` 对应 bge-m3 模型输出维度 |
| `similarity: cosine` | 向量相似度算法，可选 `cosine`、`dot_product`、`l2_norm` |
| `ik_max_word` / `ik_smart` | IK 分词器的索引模式和搜索模式 |
| `cn_stop` | 自定义中文停用词过滤 |
| `RRF` | Reciprocal Rank Fusion，Elasticsearch 原生多路检索融合算法 |

> **注意**：使用 `searchHybrid` 需要 Elasticsearch 8.x 及以上版本，且索引的 `dense_vector` 字段必须设置 `index: true`。

---

## 十六、技术支持

- **技术交流群**：21220580、166471282

- **参考文档**：https://doc.bbossgroups.com/#/mvc/chatstream

- **源码地址**：https://gitee.com/bboss/bboss-ai

- **多模态智能问答web demo**：https://gitee.com/bboss/bbootdemo
  关键代码：

  流式问答控制器 https://gitee.com/bboss/bbootdemo/blob/master/src/main/java/org/frameworkset/web/react/ReactorController.java

  前端网页 https://gitee.com/bboss/bbootdemo/blob/master/WebRoot/chatBackuppressSession.html


- **后端多模态演示案例**：https://gitee.com/bboss/bboss-ai

  关键代码：https://gitee.com/bboss/bboss-ai/blob/main/bboss-ai/src/test/java/org/frameworkset/spi/ai/StreamTest.java