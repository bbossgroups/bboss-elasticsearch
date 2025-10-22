# **bboss jobflow 编排 Deepseek 对话流程使用文档**

本文介绍如何使用 bboss jobflow 编排 Deepseek 对话流程，包括功能简介、依赖配置、代码示例和运行说明。

## **1. 功能简介**
利用bboss`jobflow` 流程编排功能，结合 Deepseek 大模型 API，实现多轮对话推理任务。通过构建多个节点任务，可以完成如写诗、评价诗词等复杂交互流程。

- **核心组件**：
  - `JobFlowBuilder`：用于构建整个工作流。
  - `JobFlowNodeFunction`：定义每个节点的行为。
  - `DeepseekJobFlowNodeBuilder`：自定义 Deepseek 节点构造器。
  - `DeepseekMessages` 和 `DeepseekMessage`：封装请求参数与消息体。
  - `HttpRequestProxy`：调用 Deepseek API 接口。
- 案例源码地址 

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlow2ndDeepseekTest.java

---

## **2. 环境准备**
### **2.1 Maven 依赖**
确保项目中已引入bboss jobflow依赖：

```xml
<dependency>
    <groupId>com.bbossgroups.plugins</groupId>
    <artifactId>bboss-datatran-jdbc</artifactId>
    <version>7.5.2</version>
</dependency>

```
---

## **3. 核心类说明**
### **3.1 DeepseekMessage**
封装单条对话记录，包含角色、内容、工具调用等字段。

```java
public class DeepseekMessage {
    private String role;
    private String content;
    private String name;
    private boolean prefix;
    private String reasoning_content;
    private String tool_call_id;

    // Getter and Setter...
}
```


### **3.2 DeepseekMessages**
封装完整对话历史，用于发送给 Deepseek API。

```java
public class DeepseekMessages {
    private String model;
    private String deepseekService;
    private boolean stream;
    private int max_tokens;
    private List<DeepseekMessage> messages;

    // Getter and Setter...
}
```


### **3.3 DeepseekJobFlowNodeFunction**
抽象类，继承 `BaseJobFlowNodeFunction`，提供 Deepseek 调用基础参数。

```java
public abstract class DeepseekJobFlowNodeFunction extends BaseJobFlowNodeFunction {
    protected String model;
    protected String deepseekService;
    protected String helpfulAssistant;
    protected boolean stream;
    protected int max_tokens;

    // Getter and Setter...
}
```


### **3.4 DeepseekJobFlowNodeBuilder**
构建 Deepseek 节点，设置模型参数并绑定处理函数。

```java
public class DeepseekJobFlowNodeBuilder extends SimpleJobFlowNodeBuilder {
    private String model;
    private String deepseekService;
    private String helpfulAssistant;
    private boolean stream;
    private int max_tokens;
    private DeepseekJobFlowNodeFunction deepseekJobFlowNodeFunction;

    public DeepseekJobFlowNodeBuilder(String nodeId, String nodeName, DeepseekJobFlowNodeFunction deepseekJobFlowNodeFunction) {
        super(nodeId, nodeName);
        this.deepseekJobFlowNodeFunction = deepseekJobFlowNodeFunction;
        this.setAutoNodeComplete(true);
    }

    @Override
    protected JobFlowNodeFunction buildJobFlowNodeFunction() {
        deepseekJobFlowNodeFunction.setDeepseekService(deepseekService);
        deepseekJobFlowNodeFunction.setModel(model);
        deepseekJobFlowNodeFunction.setStream(stream);
        deepseekJobFlowNodeFunction.setHelpfulAssistant(helpfulAssistant);
        deepseekJobFlowNodeFunction.setMax_tokens(max_tokens);
        return deepseekJobFlowNodeFunction;
    }

    // Setters for configuration...
}
```

---

## **4. 示例代码**
### **4.1 初始化 Deepseek 服务**
```java
private static void initDeepseekService() {
    Map properties = new HashMap();
//deepseek为的Deepseek服务数据源名称
        properties.put("http.poolNames","deepseek");

        properties.put("deepseek.http.hosts","https://api.deepseek.com");///设置Deepseek服务地址
        properties.put("deepseek.http.httpRequestInterceptors","org.frameworkset.datatran.imp.jobflow.ApiKeyHttpRequestInterceptor");//设置apiKey
    properties.put("deepseek.http.timeoutSocket", "60000");
    properties.put("deepseek.http.timeoutConnection", "40000");
    properties.put("deepseek.http.connectionRequestTimeout", "70000");
    properties.put("deepseek.http.maxTotal", "100");
    properties.put("deepseek.http.defaultMaxPerRoute", "100");

    HttpRequestProxy.startHttpPools(properties);
}
```

通过拦截器org.frameworkset.datatran.imp.jobflow.ApiKeyHttpRequestInterceptor设置Deepseek apiKey：

```java
/**
 * 通过拦截器设置Deepseek apiKey
 * @author biaoping.yin
 * @Date 2025/6/29
 */
public class ApiKeyHttpRequestInterceptor implements HttpRequestInterceptor {

    /**
     * Processes a request.
     * On the client side, this step is performed before the request is
     * sent to the server. On the server side, this step is performed
     * on incoming messages before the message body is evaluated.
     *
     * @param request the request to preprocess
     * @param context the context for the request
     * @throws HttpException in case of an HTTP protocol violation
     * @throws IOException   in case of an I/O error
     */
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        //设置Deepseek Authorization apiKey（在Deepseek官网申请apiKey）
        request.addHeader("Authorization","Bearer sk-9fca9***********************fa2b");
    }
}
```

在Deepseek官网申请apiKey

https://platform.deepseek.com/api_keys

### **4.2 构建 JobFlow 并执行任务**

```java
public static void main(String[] args) {
//初始化Deepseek服务
        initDeepseekService();
        //构建流程
        JobFlowBuilder jobFlowBuilder = new JobFlowBuilder();
        jobFlowBuilder.setJobFlowName("Deepseek写诗-评价诗词流程")
                .setJobFlowId("测试id");
        JobFlowScheduleConfig jobFlowScheduleConfig = new JobFlowScheduleConfig();
        jobFlowScheduleConfig.setExecuteOneTime(true);
        jobFlowBuilder.setJobFlowScheduleConfig(jobFlowScheduleConfig);
        
         
        /**
         * 1.构建第一个任务节点：单任务节点 写诗
         */
        DeepseekJobFlowNodeBuilder jobFlowNodeBuilder = new DeepseekJobFlowNodeBuilder("1", "Deepseek-chat-写诗", new DeepseekJobFlowNodeFunction() {
            @Override
            public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) throws Exception {

       
                List<DeepseekMessage> deepseekMessageList = new ArrayList<>();
                DeepseekMessage deepseekMessage = new DeepseekMessage();
                deepseekMessage.setRole("system");
                deepseekMessage.setContent("你是一位唐代诗人.");
                deepseekMessageList.add(deepseekMessage);
                //将通话记录添加到工作流上下文中，保存Deepseek通话记录
                jobFlowNodeExecuteContext.addJobFlowContextData("messages", deepseekMessageList);
                
                deepseekMessage = new DeepseekMessage();
                deepseekMessage.setRole("user");
                deepseekMessage.setContent("模仿李白的风格写一首七律.飞机!");
                //将问题添加到工作流上下文中，保存Deepseek通话记录
                deepseekMessageList.add(deepseekMessage);
                
                DeepseekMessages deepseekMessages = new DeepseekMessages();
                deepseekMessages.setMessages(deepseekMessageList);
                deepseekMessages.setModel(model);
                deepseekMessages.setStream(stream);
                deepseekMessages.setMax_tokens(this.max_tokens);
                //调用Deepseek 对话api提问
                Map response = HttpRequestProxy.sendJsonBody(this.getDeepseekService(), deepseekMessages, "/chat/completions",Map.class);
                List choices = (List) response.get("choices");
                Map message = (Map) ((Map)choices.get(0)).get("message");
                deepseekMessage = new DeepseekMessage();
                deepseekMessage.setRole("assistant");
                deepseekMessage.setContent((String)message.get("content"));
                //将问题答案添加到工作流上下文中，保存Deepseek通话记录
                deepseekMessageList.add(deepseekMessage);
                logger.info(deepseekMessage.getContent());
                return response;
        }
 
        }).setDeepseekService("deepseek").setModel("deepseek-chat").setMax_tokens(4096);
         
        /**
         * 2 将第一个节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);

        /**
         * 3.构建第二个任务节点：单任务节点 分析诗
         */
        jobFlowNodeBuilder = new DeepseekJobFlowNodeBuilder("2", "Deepseek-chat-分析诗", new DeepseekJobFlowNodeFunction() {
            @Override
            public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) throws Exception {
                //从工作流上下文中，获取Deepseek历史通话记录
                List<DeepseekMessage> deepseekMessageList = (List<DeepseekMessage>) jobFlowNodeExecuteContext.getJobFlowContextData("messages");
                //将第二个问题添加到工作流上下文中，保存Deepseek通话记录
                DeepseekMessage deepseekMessage = new DeepseekMessage();

                deepseekMessage.setRole("user");
                deepseekMessage.setContent("帮忙评估上述诗词的意境");
                deepseekMessageList.add(deepseekMessage);
                DeepseekMessages deepseekMessages = new DeepseekMessages();
                deepseekMessages.setMessages(deepseekMessageList);
                deepseekMessages.setModel(model);
                deepseekMessages.setStream(stream);
                deepseekMessages.setMax_tokens(this.max_tokens);
                //调用Deepseek 对话api提问
                Map response = HttpRequestProxy.sendJsonBody(this.getDeepseekService(), deepseekMessages, "/chat/completions", Map.class);
                List choices = (List) response.get("choices");
                Map message = (Map) ((Map) choices.get(0)).get("message");
                deepseekMessage = new DeepseekMessage();
                deepseekMessage.setRole("assistant");
                deepseekMessage.setContent((String) message.get("content"));
                //将第二个问题答案添加到工作流上下文中，保存Deepseek通话记录
                deepseekMessageList.add(deepseekMessage);
                logger.info(deepseekMessage.getContent());
                return response;
            }

        }).setDeepseekService("deepseek").setModel("deepseek-chat").setMax_tokens(4096);

        /**
         * 4 将第二个节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);
        
        //构建和运行与Deepseek通话流程
        JobFlow jobFlow = jobFlowBuilder.build();
        jobFlow.start();
}
```

---

## **5. 执行结果**
程序运行后，将依次执行两个任务节点：
1. Deepseek 写一首七律诗。
2. Deepseek 分析并评估该诗的意境。

输出如下日志片段：

```
[INFO] 生成的诗句：飞翼穿云破晓光，长空万里任翱翔。山河壮丽英雄气，天地辽阔自由翔。
[INFO] 评估结果：这首诗描绘了飞行器穿越晨曦、翱翔天际的画面，语言豪放洒脱，充满想象力和浪漫主义色彩，体现了对自由与广阔世界的向往。
```

---

## **6. 注意事项**
- **API Key 设置**：需实现 `ApiKeyHttpRequestInterceptor` 类，注入 Deepseek 的 API Key。
- **超时控制**：根据实际网络情况调整 timeoutSocket、timeoutConnection等参数。
- **并发限制**：合理设置连接池大小，避免因并发过高导致服务不可用。
- **上下文管理**：确保每轮对话记录正确保存到 `JobFlowNodeExecuteContext` 中，以便后续节点使用。

---

## **7. 总结**
通过 `bboss jobflow`，我们可以轻松编排多阶段的 AI 推理任务，适用于写诗、分析、问答等多种场景。结合 Deepseek 大模型的强大能力，能够为业务系统提供更智能的决策支持和服务能力。

---