# bboss http5使用指南

bboss http5,一个简单而功能强大的、基于httpclient5的、去中心化的http/https负载均衡器、http rpc框架以及java ai客户端;基于http/https协议实现的客户端-服务端点到点的负载均衡和集群容灾功能，可以基于post/get/put/requestbody等方法对接调用任何基于http协议开发的微服务，包括spring cloud、spring boot、spring mvc以及其他基于http协议开发的微服务；提供streamchat方法，轻松对接各种大模型服务，实现流式对话应用；完全支持http2协议；同时还可以非常方便地实现多个文件上传服务器。可基于apollo和nacos管理[服务配置参数和实现服务发现](https://esdoc.bbossgroups.com/#/nacos-config)功能。



![](images\client-server-httpclient5.jpg)

bboss http5完全继承了[bboss http](https://esdoc.bbossgroups.com/#/httpproxy)（基于httpclient4）的所有功能和api方法以及参数配置，在此基础上新增了响应式的stream流式api方法，本文介绍bboss http5新增功能使用方法以及升级的一些注意事项。

## AI多模态功能

bboss ai客户端支持多模态功能：

- **文本对话** 
- **图像识别** 
- **图像生成**
- **语音识别**
- **语音生成** 
- **视频生成**

正式介绍之前，先了解一下HttpClient 5 的新特性。

## 1.HttpClient 5与HttpClient 4

HttpClient 5 是 Apache 在 HttpClient 4 基础上的一次重大升级，带来了性能、灵活性和现代网络协议支持方面的显著提升，二者对比如下：

### 1.1并发模型与性能

这是最核心的差异。

- **HttpClient 4**：
  - **同步阻塞 I/O**： 当您执行一个 HTTP 请求时，发起请求的线程会一直阻塞，直到收到完整的响应。在高并发场景下，这需要大量的线程来支撑，线程上下文切换开销大。
  - 虽然有 `HttpAsyncClient`，但在 4.x 中它是一个独立的组件，且成熟度不如 5.x。

- **HttpClient 5**：
  - **异步/事件驱动 I/O**： 底层使用了更高效的 NIO（New I/O）。客户端线程发起请求后不会被阻塞，可以立即返回处理其他任务。当响应数据可用时，由 I/O 线程池回调处理。这使得单个线程可以管理成千上万的并发连接，极大地提升了资源利用率和吞吐量。
  - **HTTP/2 多路复用**： HTTP/2 允许在同一个 TCP 连接上并行交错地发送多个请求和响应，避免了 HTTP/1.1 的队头阻塞问题。HttpClient 5 原生支持这一点，使得在需要与同一服务器建立大量连接时，性能有数量级的提升。

### 1.2 HTTP/2 支持

- **HttpClient 4**： **完全不支持** HTTP/2。对于现代 Web 服务（如 gRPC、现代前端应用），这是一个致命的短板。
- **HttpClient 5**： **全面支持** HTTP/2，包括 ALPN（应用层协议协商），可以无缝地与支持 HTTP/2 的服务器（如 Nginx， Spring Boot 2.3+）进行高效通信。

## 2.项目源码

https://github.com/bbossgroups/bboss-http5

https://gitee.com/bboss/bboss-http5

httpproxy 案例：

基于apollo进行配置管理、节点自动发现、路由规则自动切换，源码地址

https://github.com/bbossgroups/httpproxy-apollo

https://gitee.com/bboss/httpproxy-apollo

基于nacos进行配置管理、节点自动发现、路由规则自动切换，源码地址

https://gitee.com/bboss/httpproxy-nacos

## 3.导入bboss http5

在工程中导入以下maven坐标即可

```xml
<dependency>
   <groupId>com.bbossgroups</groupId>
   <artifactId>bboss-http5</artifactId>
   <version> 6.5.3</version>
</dependency>
```

如果是gradle工程，导入方法如下：

```groovy
api 'com.bbossgroups:bboss-http5: 6.5.3'
```

## 4.基础功能使用文档

本文只介绍http5新引入的功能特性及其使用方法，原有功能和api方法以及参数配置参考文档：

https://esdoc.bbossgroups.com/#/httpproxy

## 5.流式方法使用介绍

以Deepseek官方模型服务为例来介绍流式api使用方法。

### 5.1 服务配置

在配置服务之前，需访问Deepseek官网申请服务的apiKeyId：https://platform.deepseek.com/api_keys

在工程resources目录下，新建配置文件application-stream.properties,添加以下内容

```properties
http.poolNames = default
##http连接池配置

http.maxTotal = 200
http.defaultMaxPerRoute = 200

# ha proxy 集群负载均衡地址配置,多个地址用逗号分隔
http.hosts=https://api.deepseek.com
# https服务必须带https://协议头,多个地址用逗号分隔
#http.hosts=https://192.168.137.1:808,https://192.168.137.1:809,https://192.168.137.1:810
#基于apiKeyId认证配置（主要用于各种大模型服务对接认证）
http.apiKeyId = sk-xxxxx 
```

### 5.2 服务启动

加载配置文件，启动负载均衡器,应用中只需要执行一次，启动后就可以使用流式api实现大模型服务的流式调用功能。

```java
//加载配置文件，启动负载均衡器,应用中只需要执行一次
HttpRequestProxy.startHttpPools("application-stream.properties");
```

### 5.3 流式api使用

#### 5.3.1 简单调用方法示例

```java
//定义问题变量
String message = "介绍一下bboss jobflow";
//设置模型调用参数，
Map<String, Object> requestMap = new HashMap<>();
requestMap.put("model", "deepseek-chat");//指定模型

List<Map<String, String>> messages = new ArrayList<>();
Map<String, String> userMessage = new HashMap<>();
userMessage.put("role", "user");
userMessage.put("content", message);
messages.add(userMessage);

requestMap.put("messages", messages);
requestMap.put("stream", true);
requestMap.put("max_tokens", 2048);
requestMap.put("temperature", 0.7);
//通过bboss httpproxy响应式异步交互接口，请求Deepseek模型服务，提交问题
HttpRequestProxy.streamChatCompletion("/chat/completions",requestMap)
        .doOnSubscribe(subscription -> logger.info("开始订阅流..."))
        .doOnNext(chunk -> System.out.print(chunk)) //打印流式调用返回的问题答案片段
        .doOnComplete(() -> logger.info("\n=== 流完成 ==="))
        .doOnError(error -> logger.error("错误: " + error.getMessage(),error))
        .subscribe();

// 等待异步操作完成，否则流式异步方法执行后会因为主线程的退出而退出，看不到后续响应的报文
Thread.sleep(100000000);
```

执行上述代码，可以在控制台看到流式输出Deepseek返回的答案：

![image-20251012131853298](images\deepseekstreamas.png)

#### 5.3.2 自定义返回片段解析处理逻辑

通过bboss httpproxy响应式异步交互接口，请求Deepseek模型服务，提交问题，可以自定义每次返回的片段解析方法
，处理数据行,如果数据已经返回完毕，则返回true，指示关闭对话，否则返回false

```java
//定义问题变量
String message = "介绍一下bboss jobflow";
//设置模型调用参数，
Map<String, Object> requestMap = new HashMap<>();
requestMap.put("model", "deepseek-chat");//指定模型

List<Map<String, String>> messages = new ArrayList<>();
Map<String, String> userMessage = new HashMap<>();
userMessage.put("role", "user");
userMessage.put("content", message);
messages.add(userMessage);

requestMap.put("messages", messages);
requestMap.put("stream", true);
requestMap.put("max_tokens", 2048);
requestMap.put("temperature", 0.7);
//通过bboss httpproxy响应式异步交互接口，请求Deepseek模型服务，提交问题，可以自定义每次返回的片段解析方法
//处理数据行,如果数据已经返回完毕，则返回true，指示关闭对话，否则返回false
HttpRequestProxy.streamChatCompletion("/chat/completions",requestMap,new StreamDataHandler<String>() {
            @Override
            public boolean handle(String line, FluxSink<String> sink) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();

                    if ("[DONE]".equals(data)) {
                        return true;
                    }
                    if (!data.isEmpty()) {
                        String content = ResponseUtil.parseStreamContentFromData(data);
                        if (content != null && !content.isEmpty()) {
                            sink.next(content);
                        }
                    }
                }
                else{
                    if(logger.isDebugEnabled()) {
                        logger.debug("streamChatCompletion: " + line);
                    }
                }
                return false;

            }
        })
        .doOnSubscribe(subscription -> logger.info("开始订阅流..."))
        .doOnNext(chunk -> System.out.print(chunk)) //打印流式调用返回的问题答案片段
        .doOnComplete(() -> logger.info("\n=== 流完成 ==="))
        .doOnError(error -> logger.error("错误: " + error.getMessage(),error))
        .subscribe();

// 等待异步操作完成，否则流式异步方法执行后会因为主线程的退出而退出，看不到后续响应的报文
Thread.sleep(100000000);
```

通过StreamDataHandler < T >和FluxSink < T >中的泛型类型，可以将Stream中的数据转换为特定的数据类型。

#### 5.3.3 AI智能问答示例

可以与[bboss mvc restful服务结合](https://doc.bbossgroups.com/#/mvc/chatstream)，轻松实现基于AI大模型、多模型的智能问答功能：

```java
/**
     * 背压案例 - 带会话记忆功能（完善版）
     * http://127.0.0.1/demoproject/chatBackuppressSession.html
     * @param questions
     * @return
     */
    public Flux<List<ServerEvent>> deepseekBackuppressSession(@RequestBody Map<String,Object> questions) {

        String selectedModel = (String)questions.get("selectedModel");
        Boolean reset = (Boolean) questions.get("reset");
        if(reset != null && reset){
            sessionMemory.clear();
        }
        String message = (String)questions.get("message");
        Map<String, Object> requestMap = new HashMap<>();
        if(selectedModel.equals("deepseek")) {
            requestMap.put("model", "deepseek-chat");
        }
        else {
            requestMap.put("model", "Qwen/Qwen3-Next-80B-A3B-Instruct");//指定模型
        }
    
        // 构建消息历史列表，包含之前的会话记忆
        List<Map<String, Object>> messages = new ArrayList<>(sessionMemory);
        
        // 添加当前用户消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);
    
        requestMap.put("messages", messages);
        requestMap.put("stream", true);
        requestMap.put("max_tokens", 2048);
        requestMap.put("temperature", 0.7);
        Flux<ServerEvent> flux = HttpRequestProxy.streamChatCompletionEvent(selectedModel,"/chat/completions",requestMap);
    
        // 用于累积完整的回答
        StringBuilder completeAnswer = new StringBuilder();
    
        return flux.doOnNext(chunk -> {
           
            if(!chunk.isDone()) {
                logger.info(chunk.getData());
            }
            
        })
        .limitRate(5) // 限制请求速率
        .buffer(3) // 每3个元素缓冲一次
        .doOnNext(bufferedEvents -> {
            // 处理模型响应并更新会话记忆
            for(ServerEvent event : bufferedEvents) {
                //答案前后都可以添加链接和标题
                if(event.isFirst() || event.isDone()){
                    event.addExtendData("url","https://www.bbossgroups.com");
                    event.addExtendData("title","bboss官网");
                }
                if(!event.isDone() ) {
                    // 累积回答内容
                    if(event.getData() != null) {
                        completeAnswer.append(event.getData());
                    }
                } else  {
                    
                    if( completeAnswer.length() > 0) {
                        // 当收到完成信号且有累积内容时，将完整回答添加到会话记忆
                        Map<String, Object> assistantMessage = new HashMap<>();
                        assistantMessage.put("role", "assistant");
                        assistantMessage.put("content", completeAnswer.toString());
                        sessionMemory.add(assistantMessage);

                        // 维护记忆窗口大小为20
                        if (sessionMemory.size() > 20) {
                            sessionMemory.remove(0);
                        }
                    }
                    
                    
                }
            }
        });
    }
```

服务接收message参数（用户提问），返回reactor Flux结果

案例源码工程地址：https://gitee.com/bboss/bbootdemo    

Deepseek和硅基流动模型服务配置文件（自行申请和修改相关服务的官方apiKey）：https://gitee.com/bboss/bbootdemo/blob/master/src/main/resources/application.properties

服务实现类：https://gitee.com/bboss/bbootdemo/blob/master/src/main/java/org/frameworkset/web/react/ReactorController.java

下载案例源码后，直接运行Main类启动服务：https://gitee.com/bboss/bbootdemo/blob/master/src/test/java/org/frameworkset/test/Main.java

服务启动后访问地址：http://127.0.0.1/demoproject/chatBackuppressSession.html

可以选择特定模型进行功能验证：

![](images\httpproxy\testpage.png)



## 6.升级bboss http5注意事项

从 HttpClient 4 迁移到 5 并不是简单的替换 JAR 包，因为很多包名发生了变化，从 `org.apache.http` 变更为 `org.apache.hc.client5.http`， `org.apache.hc.core5.http` 等，因此bboss中涉及的一些扩展接口也要做相应调整

### 6.1 HttpRequestInterceptor接口调整

请求拦截器HttpRequestInterceptor接口定义以及httpclient5包路径发生变化：升级时需做相应调整

```java
public class HttpRequestInterceptorDemo implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        request.addHeader("name","test");
    }
}
```

### 6.2 HttpClientBuilderCallback接口调整

HttpClientBuilderCallback接口包路径发生变化：升级时需做相应调整

```java
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.frameworkset.spi.remote.http.callback.HttpClientBuilderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientBuilderCallbackDemo implements HttpClientBuilderCallback {
    private static Logger logger = LoggerFactory.getLogger(HttpClientBuilderCallbackDemo.class);
    public HttpClientBuilder customizeHttpClient(HttpClientBuilder builder, ClientConfiguration clientConfiguration) {
      
       logger.info("HttpClientBuilderCallbackDemo--------------------------------");
       return builder;
    }
}
```

### 6.3 重试机制接口调整

重试机制接口ConnectionResetHttpRequestRetryHandler包路径也发生变化：升级时需做相应调整

```java
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.net.SocketException;


public class ConnectionResetHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler{


    /**
     * Determines if a method should be retried after an IOException
     * occurs during execution.
     *
     * @param exception      the exception that occurred

     * @return {@code true} if the method should be retried, {@code false}
     * otherwise
     */
    public boolean retryRequest(HttpRequest request, IOException exception, int executionCount, HttpContext context, ClientConfiguration configuration) {
       if (super.retryRequest(  request,  exception,   executionCount,   context,   configuration)) {
          return true;
       }
       else if(exception instanceof SocketException){
          String message = exception.getMessage();
          if(message != null && message.trim().equals("Connection reset")) {
             return true;
          }
       }
       return false;
    }
}
```

## 7.开发交流

QQ交流群：21220580,166471282,3625720,154752521,166471103,166470856

微信交流群：

<img src="images\wxbboss.png" style="zoom:50%;" />


交流社区：

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">



## 8.支持我们

如果您正在使用bboss，或是想支持我们继续开发，您可以通过如下方式支持我们：

1.Star并向您的朋友推荐或分享

[bboss http5 proxy](https://gitee.com/bboss/bboss-http5)🚀

[bboss elasticsearch client](https://gitee.com/bboss/bboss-elastic)🚀

[数据采集&流批一体化处理](https://gitee.com/bboss/bboss-elastic-tran)🚀

2.通过[爱发电 ](https://afdian.net/a/bbossgroups)直接捐赠，或者扫描下面二维码进行一次性捐款赞助，请作者喝一杯咖啡☕️

<img src="https://esdoc.bbossgroups.com/images/alipay.png"  height="200" width="200">

<img src="https://esdoc.bbossgroups.com/images/wchat.png"   height="200" width="200" />

非常感谢您对开源精神的支持！❤您的捐赠将用于bboss社区建设、QQ群年费、网站云服务器租赁费用。




