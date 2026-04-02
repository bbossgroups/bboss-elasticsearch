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
- **多轮会话**：内置会话记忆管理，支持多轮对话
- **多智能体协同**：配合bboss graph提供的工作流和有限循环图，实现多智能体协同，快速构建多智能体系统

---

## 二、环境准备

### 2.1 Maven 依赖

```xml
<dependency>
    <groupId>com.bbossgroups</groupId>
    <artifactId>bboss-ai</artifactId>
    <version>6.5.3</version>
</dependency>
```

### 2.2 Gradle 依赖

```groovy
implementation 'com.bbossgroups:bboss-ai:6.5.3'
```

### 2.3 配置文件

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

visualops.http.hosts=10.13.6.4:8128
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

### 3.1 初始化配置

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

### 3.2 同步聊天（非流式）

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

### 3.3 流式聊天

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

### 3.4 多轮会话

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

        // 设置保存路径
        audioMsg.setStoreFilePathFunction(url -> {
            return "audio/" + System.currentTimeMillis() + ".wav";
        });

        AIAgent aiAgent = new AIAgent();
        AudioEvent result = aiAgent.genAudio("qwenvlplus", audioMsg);

        System.out.println("音频文件路径: " + result.getAudioUrl());
    }
}
```

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

```java
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

```java
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

## 十四、技术支持

- **技术交流群**：21220580、166471282

- **参考文档**：https://esdoc.bbossgroups.com/#/bboss-ai

- **源码地址**：https://gitee.com/bboss/bboss-ai

- **多模态智能问答web demo**：https://gitee.com/bboss/bbootdemo
  关键代码：

  流式问答控制器 https://gitee.com/bboss/bbootdemo/blob/master/src/main/java/org/frameworkset/web/react/ReactorController.java

  前端网页 https://gitee.com/bboss/bbootdemo/blob/master/WebRoot/chatBackuppressSession.html


- **后端多模态演示案例**：https://gitee.com/bboss/bboss-ai

  关键代码：https://gitee.com/bboss/bboss-ai/blob/main/bboss-ai/src/test/java/org/frameworkset/spi/ai/StreamTest.java
  
- **bboss rag参考文档**：https://esdoc.bbossgroups.com/#/Elasticsearch-embedding

