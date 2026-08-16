# bboss-ai 智能体工作流与多智能体自由组合

bboss-ai 提供了两种多智能体协同方式：**自由组合**和**工作流编排**。前者灵活直接，后者结构化且支持调度。

可以下载本文案例源码工程，在本地运行和体验多智能体自由组合和工作流编排功能：

https://gitee.com/bboss/bboss-datatran-demo

## 一、准备工作
在工程resources下创建application.properties文件，添加如下内容：需将apiKeyId替换为自己申请的deepseek apiKey

```properties
http.poolNames = deepseek
##Deepseek模型服务配置：在代码中引用服务的名称为deepseek
# 服务连接池参数
deepseek.http.maxTotal = 200
deepseek.http.defaultMaxPerRoute = 200
deepseek.http.timeoutConnection = 5000
deepseek.http.timeoutSocket = 600000
deepseek.http.connectionRequestTimeout = 5000
# Deepseek模型服务地址
deepseek.http.hosts=https://api.deepseek.com
#基于apiKeyId认证配置（主要用于各种大模型服务对接认证），替换为自己申请的deepseek apikey即可
deepseek.http.apiKeyId = sk-555bc866517f
# 模型类型：AI智能体工具通过模型类型查找智能体模型对接适配器
deepseek.http.modelType = deepseek
```

初始化模型maas服务，注意：只需要在应用启动时初始化一次即可
```java
HttpRequestProxy.startHttpPools("application.properties");
```
## 二、多智能体自由组合

直接创建多个 `AINodeAgent`，按业务顺序手动串联调用，适合简单的线性协同场景。

```java

// 配置模型参数与内存会话
ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
chatAgentMessage.setModel("deepseek-chat")
    .setMaas("deepseek")
    .setStoreContext(new StoreContext()
        .setStoreType(StoreContext.STORE_TYPE_MEMORY)
        .setSessionSize(100));

// 智能体1：写诗
AINodeAgent nodeAgent = new AINodeAgent("模仿李白的风格写一首七律.飞机!")
    .setSystemPrompt("你是一位唐代诗人.")
    .setAgentId("1")
    .setAgentName("写诗智能体")
    .setAgentOutput(event -> logger.info("--------诗歌内容---------\n{}", event.getData()));
nodeAgent.chat(chatAgentMessage);

// 智能体2：评价诗歌（自动继承上一轮的会话上下文）
AINodeAgent nodeAgent2 = new AINodeAgent("帮忙评估上述诗词的意境")
    .setAgentId("2")
    .setAgentName("评诗智能体")
    .setAgentOutput(event -> logger.info("--------诗歌评价---------\n{}", event.getData()));
nodeAgent2.chat(chatAgentMessage);
```

**要点：**
- 每个 `AINodeAgent` 代表一个独立的智能体节点
- 通过共享 `ChatAgentMessage` 中的会话上下文，实现多轮对话的自动衔接
- 执行顺序由代码调用顺序决定，完全由开发者控制

## 三、智能体工作流编排

使用 `AIPlanAgent` 将多个 `AINodeAgent` 组装成一个结构化流程，支持统一管理和定时调度。

```java
// 创建流程编排器，管理会话上下文
AIPlanAgent planAgent = new AIPlanAgent(
    new StoreContext().setStoreType(StoreContext.STORE_TYPE_MEMORY).setSessionSize(100));
planAgent.setAgentName("写诗-评诗流程").setAgentId("flow-1")
    .setAgentMessage(chatAgentMessage);

// 配置定时调度（可选）
HolidayJobFlowScheduleConfig scheduleConfig = new HolidayJobFlowScheduleConfig();
scheduleConfig.setDelay(1000L);
scheduleConfig.setFixedRate(true);
scheduleConfig.setPeriod(30000L);           // 每30秒执行一次
scheduleConfig.addCustomHoliday("2026-06-10"); // 指定日期不执行
scheduleConfig.setSkipSunday(true);
scheduleConfig.setSkipSaturday(true);
planAgent.setJobFlowScheduleConfig(scheduleConfig);

// 添加流程节点
planAgent.addAgent(new AINodeAgent("模仿李白的风格写一首七律.飞机!")
    .setSystemPrompt("你是一位唐代诗人.")
    .setAgentId("1").setAgentName("写诗智能体")
    .setAgentOutput(event -> logger.info("--------诗歌内容---------\n{}", event.getData())));

planAgent.addAgent(new AINodeAgent("帮忙评估上述诗词的意境")
    .setAgentId("2").setAgentName("评诗智能体")
    .setAgentOutput(event -> logger.info("--------诗歌评价---------\n{}", event.getData())));

// 启动流程
planAgent.chat();
```

**要点：**
- `AIPlanAgent` 作为流程容器，统一管理节点顺序和会话上下文
- 内置调度能力：支持固定间隔执行、周末跳过、自定义假日跳过
- 一次 `chat()` 调用即可驱动整个流程按序执行所有节点

## 四、两种方式对比

| 维度 | 自由组合 | 工作流编排 |
|------|---------|-----------|
| 控制粒度 | 代码级，手动控制调用顺序 | 声明式，由流程引擎自动编排 |
| 会话管理 | 开发者自行传递上下文 | `AIPlanAgent` 统一管理 |
| 定时调度 | 不支持 | 内置 `JobFlowScheduleConfig` |
| 适用场景 | 简单线性流程、临时组合 | 结构化流程、周期性任务 |

两种方式可以按需选用，也可以混合使用——在工作流节点内部嵌套自由组合的智能体调用，兼顾灵活性与规范性。
