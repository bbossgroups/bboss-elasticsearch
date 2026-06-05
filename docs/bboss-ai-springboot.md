# MCP Bean Tool 服务与 Spring Boot 集成使用文档

## 1. 概述

本文档介绍如何在 Spring Boot 项目中基于 **bboss AI** 框架集成 MCP（Model Context Protocol）Bean Tool 服务，涵盖：

- **服务端开发**：将业务方法注册为 MCP 工具并暴露标准接口
- **客户端调用**：在 AI 智能体工作流中引用远程 MCP 工具

> **案例场景**：`ai-bboss-biz-backend` 作为 MCP 服务端，提供酒店查询与航班查询工具；`ai-config-agent-backend` 作为 MCP 客户端，在 RAG 问答工作流中调用这些工具增强 AI 能力。

---

## 2. 核心依赖

本集成基于 bboss AI 框架提供的 MCP 工具包，需在对应模块中引入相关依赖（由项目 `build.gradle` 统一管理）：

```groovy 
api (
        [group: 'com.bbossgroups', name: 'bboss-ai-flow', version: '6.5.3', transitive: true],
)
```


---

## 3. MCP 服务端开发（Server）

服务端负责定义工具、注册服务并对外暴露 MCP 标准接口。

### 3.1 定义工具类

使用 `@Tool` 和 `@ToolParam` 注解标注 Bean 方法，使其成为可被大模型识别和调用的 MCP 工具。

```java
package com.bboss.agent.mcp;

import lombok.extern.slf4j.Slf4j;
import org.frameworkset.spi.ai.model.annotation.Tool;
import org.frameworkset.spi.ai.model.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class Hotel2ndFlightBookTool {

    @Tool(name = "hotelQuery", description = "根据用户的行程需求，查询合适的酒店。")
    public List<Map> hotelQuery(
            @ToolParam(name = "startDay", description = "入驻时间,例如：5月25日", required = true) String startDay,
            @ToolParam(name = "endDay", description = "离房时间,例如：5月28日", required = true) String endDay) {
        // 业务逻辑：返回酒店列表
        List<Map> hotels = new ArrayList<>();
        // ...
        return hotels;
    }

    @Tool(name = "flightQuery", description = "根据用户的行程需求，查询合适的航班机票。")
    public List<Map> flightQuery(
            @ToolParam(name = "bookDay", description = "出发时间,例如：5月25日", required = true) String bookDay,
            @ToolParam(name = "arriveDay", description = "到达时间,例如：5月28日", required = true) String arriveDay,
            @ToolParam(name = "fromStation", description = "出发地,例如：长沙", required = true) String fromStation,
            @ToolParam(name = "toStation", description = "到达地,例如：北京", required = true) String toStation) {
        // 业务逻辑：返回航班列表
        List<Map> flights = new ArrayList<>();
        // ...
        return flights;
    }
}
```


**注解说明**：

| 注解 | 作用 | 必填项 |
|------|------|--------|
| `@Tool` | 声明该方法为 MCP 工具，`name` 为工具标识，`description` 供大模型理解工具用途 | `name`、`description` |
| `@ToolParam` | 声明工具参数，`name` 为参数名，`description` 供大模型理解参数含义，`required` 是否必填 | `name`、`description` |

### 3.2 注册并暴露 MCP 服务

通过 `InitializingBean` 在应用启动时注册工具，并通过 Controller 暴露 SSE / message / streamable 标准接口。

```java
package com.bboss.agent.controller;

import com.bboss.agent.mcp.Hotel2ndFlightBookTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.frameworkset.spi.ai.mcp.tools.server.MCPApiKeyServiceImpl;
import org.frameworkset.spi.ai.mcp.tools.server.MCPToolService;
import org.frameworkset.spi.ai.mcp.tools.server.MCPToolServiceImpl;
import org.frameworkset.spi.remote.http.HttpRequestProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class MCPServerController implements org.springframework.beans.factory.InitializingBean {

    @Autowired
    private Hotel2ndFlightBookTool hotel2ndFlightBookTool;
    private MCPToolService mcpService;

    /** SSE 流式接口：Header 传 Authorization: Bearer <apiKey> */
    @GetMapping("/sse")
    public @ResponseBody Flux<String> sse(@RequestHeader(name = "Authorization") String authorizationHeader) {
        String apiKey = HttpRequestProxy.extractApiKeyFromBearer(authorizationHeader);
        return mcpService.sse(apiKey);
    }

    /** SSE 流式接口（URL 传 apiKey，调试使用） */
    @GetMapping("/sse_v1")
    public @ResponseBody Flux<String> sse_v1(@RequestParam(name = "apiKey") String apiKey) {
        return mcpService.sse(apiKey);
    }

    /** Message 接口 */
    @PostMapping("/message")
    public @ResponseBody String message(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "sessionId") String sessionId,
            @RequestBody String requestBody) {
        String apiKey = HttpRequestProxy.extractApiKeyFromBearer(authorizationHeader);
        return mcpService.message(apiKey, sessionId, requestBody);
    }

    /** Streamable 统一接口（推荐） */
    @PostMapping("/streamable")
    public @ResponseBody Object streamable(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody String requestBody) {
        String apiKey = HttpRequestProxy.extractApiKeyFromBearer(authorizationHeader);
        return mcpService.streamable(apiKey, requestBody);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MCPApiKeyServiceImpl mcpApiKeyService = new MCPApiKeyServiceImpl();

        // 注册工具 Bean，同时可调用 registMcpBeanTool 注册多个组件
        log.info("registering MCP tools: hotel2ndFlightBookTool use apiKey 123456.");
        mcpApiKeyService.registMcpBeanTool("123456", hotel2ndFlightBookTool);

        MCPToolServiceImpl mcpService = new MCPToolServiceImpl();
        mcpService.setMcpApiKeyService(mcpApiKeyService);
        this.mcpService = mcpService;
    }
}
```


**关键点**：

- `registMcpBeanTool("123456", hotel2ndFlightBookTool)` 将 Bean 中所有 `@Tool` 方法注册到指定 `apiKey` 下
- 客户端必须携带匹配的 `apiKey` 才能调用，否则拒绝服务
- 支持 `sse`、`message`、`streamable` 三种标准 MCP 交互模式

---

## 4. MCP 客户端调用（Client）

客户端通过配置远程 MCP 服务端地址，在 AI 智能体工作流中注入工具能力。

### 4.1 配置服务端连接

在 `resources/mcpserver.properties` 中定义连接池与服务端地址：

```properties
http.poolNames = bboss_mcp_server

# 服务连接池参数
bboss_mcp_server.http.maxTotal = 200
bboss_mcp_server.http.defaultMaxPerRoute = 200

# MCP 服务端地址（对应 ai-bboss-biz-backend 服务）
bboss_mcp_server.http.hosts = 127.0.0.1:8889

# 客户端调用时使用的 apiKey（必须与服务端注册的一致）
bboss_mcp_server.http.apiKeyId = 123456

# 自定义 streamable 端点路径
bboss_mcp_server.http.extendConfigs.streamableendpoint = /bboss-biz-srv/mcp/streamable
```


**配置说明**：

| 配置项 | 说明                                     |
|--------|----------------------------------------|
| `http.poolNames` | 连接池名称列表，需与下方前缀一致                       |
| `bboss_mcp_server.http.hosts` | MCP 服务端地址与端口，多个地址用逗号分隔，支持负载均衡和故障容灾、熔断  |
| `bboss_mcp_server.http.apiKeyId` | 调用凭证，与服务端 `registMcpBeanTool` 的 key 一致 |
| `extendConfigs.streamableendpoint` | 指定 streamable 模式调用的 HTTP 端点            |

### 4.2 注册客户端 Bean

创建 `@Configuration` 类，将 `MCPToolsRegist` 注册为 Spring Bean，名称需与 `mcpserver.properties` 中的连接池名称一致。

```java
package com.bboss.agent.mcp;

import org.frameworkset.spi.ai.mcp.tools.MCPToolsRegist;
import org.frameworkset.spi.ai.tools.ToolsRegist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class bbossAgentMcpClientFactory {

    @Bean("bbossConfigMcpServerToolsRegist")
    public ToolsRegist buildBbossConfigMcpServerToolsRegist() {
        // "bboss_mcp_server" 对应 mcpserver.properties 中的连接池名称
        return new MCPToolsRegist("bboss_mcp_server");
    }
}
```
名称为bbossConfigMcpServerToolsRegist的ToolsRegist在应用中只需要声明一次，就可以在AI工作流或者智能体中注入这个MCP工具实例。

实例可以到处复用，无需重复声明。

### 4.3 在 AI 工作流中使用

在智能体（Agent）节点中通过 `setToolsRegist()` 注入 MCP 工具，大模型即可在推理过程中自动识别并调用远程工具。

```java
// 注入客户端 ToolsRegist
@Autowired
@Qualifier("bbossConfigMcpServerToolsRegist")
private ToolsRegist bbossConfigMcpServerToolsRegist;

// 在用户问题改写节点中注册 MCP 工具
planAgent.addConditionFlowNode(true, new StandaloneAgent("#[ragQuestion]")
        .setAgentName("用户问题改写智能体")
        .setAgentId("userQuestionRewriter")
        .setToolsRegist(bbossConfigMcpServerToolsRegist) // <-- 注册 MCP 工具
        .setOutputVaribleName("retrievalQuestion", AIFlowConst.AIFLOW_VAR_SCOPE_FLOW),
        new TriggerScriptAPI() {
            @Override
            public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                // 触发逻辑...
                return true;
            }
        });

// 在答案生成节点中同样可注册
planAgent.addAgent(new AINodeAgent(userPrompt)
        .setAgentId("answerQuestionAgent")
        .setAgentName("答案生成智能体")
        .setToolsRegist(bbossConfigMcpServerToolsRegist)); // <-- 注册 MCP 工具
```


**效果**：当大模型判断当前问题需要查询酒店或航班时，会自动构造参数调用 `hotelQuery` / `flightQuery`，并将结果融入后续推理。

---

## 5. 接口清单

服务端 `MCPServerController` 暴露以下接口：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/mcp/sse` | SSE 流式连接，Header 传 `Authorization: Bearer {apiKey}` |
| GET | `/mcp/sse_v1` | SSE 流式连接，URL 参数传 `apiKey`（调试用） |
| POST | `/mcp/message` | 普通消息交互，需传 `sessionId` |
| POST | `/mcp/streamable` | Streamable 统一流式交互（推荐） |

## 6. Vue 前端流式调用（Stream 问答）

本章节结合 `ai-config-agent-backend` 的 `SseEmitter` 流式接口与 `ai-config-agent-front` 的 `fetch` 流式消费实现，说明前后端如何协同完成基于 bboss AI 工作流的 SSE 流式问答。

### 6.1 整体交互架构

```
┌─────────────────┐      POST /api/chat/stream      ┌──────────────────────────┐
│   Vue 前端      │  ─────────────────────────────>  │   ChatController         │
│  (fetch+Stream) │                                  │  (SseEmitter 120s)       │
└─────────────────┘                                  └──────────────────────────┘
       ^                                                        │
       │                                                        ▼
  解析 JSON 事件                                           RagQAService
  (type/data/...)                                     (AIPlanAgent + Flux)
       ^                                                        │
       │                              ┌─────────────────────────┘
       └──────────────────────────────┤ FluxRagStreamCallback
                                      │  emitter.send(data)
                                      └──────────────────────────┘
```


### 6.2 后端流式接口设计

后端采用 **Spring MVC `SseEmitter`** 暴露 SSE 端点，由 `FluxRagStreamCallback` 将 bboss `Flux<ServerEvent>` 的每个事件转发给前端。

#### 6.2.1 Controller 入口

```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @RequestMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE, 
                    method = {RequestMethod.POST, RequestMethod.GET})
    public SseEmitter stream(@RequestBody ChatRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            throw new BusinessException(400, "sessionId is required");
        }
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new BusinessException(400, "question is required");
        }
        SseEmitter emitter = new SseEmitter(120_000L);  // 120s 超时
        
        String model = normalizeModel(request.getModel());
        log.info("Received stream request: id={}, sessionId={}, model={}",
                request.getId(), request.getSessionId(), model);

        try {
            // streamFluxAdapted 内部启动 bboss AIPlanAgent 工作流
            ragQAService.streamFluxAdapted(
                    request.getId(), request.getSessionId(), request.getUserId(),
                    request.getQuestion(), model, request.getDomain(),
                    new FluxRagStreamCallback(emitter, request.getId()));
        } catch (Exception e) {
            log.error("[RAG-V2] stream unexpected failure id={}", request.getId(), e);
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
```


#### 6.2.2 SSE 适配器：FluxRagStreamCallback

`FluxRagStreamCallback` 实现了 `FluxStreamCallback` 接口，职责是将 bboss 工作流产生的异步事件 **映射到 `SseEmitter`**，并在连接断开时 **取消底层 Flux 订阅**。

```java
public class FluxRagStreamCallback implements FluxStreamCallback {
    private final SseEmitter emitter;
    private final String requestId;

    public FluxRagStreamCallback(SseEmitter emitter, String requestId) {
        this.emitter = emitter;
        this.requestId = requestId;
    }

    @Override
    public void onToken(String token) {
        try {
            emitter.send(SseEmitter.event().data(token));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    @Override
    public void onComplete() {
        try { emitter.complete(); } 
        catch (Exception e) { emitter.completeWithError(e); }
    }

    @Override
    public void onError(Throwable error) {
        emitter.completeWithError(error);
    }

    // 注册 emitter 生命周期监听：前端断连/超时/错误时，主动 shutdown Agent 并 dispose Flux
    public void registerDisposable(Disposable disposable, AIPlanAgent planAgent, String requestId) {
        Runnable cleanup = () -> {
            if (disposable != null && !disposable.isDisposed()) {
                planAgent.shutdown();
                disposable.dispose();
                log.info("Disposed flux subscription id={}", requestId);
            }
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((e) -> cleanup.run());
    }
}
```


#### 6.2.3 工作流内部事件推送

在 `RagQAService.streamFluxAdapted()` 中，bboss `AIPlanAgent` 的每个工作流节点通过 `Flux<ServerEvent>` 向下游推送事件。`FluxRagStreamCallback` 收到 JSON 序列化后的 `ServerEvent`，直接透传至前端。

```java
Flux<ServerEvent> flux = planAgent.chatStream();

Disposable disposable = flux
    .doOnNext(event -> {
        // 将 ServerEvent 序列化为 JSON 字符串推给前端
        callback.onToken(JsonUtil.object2json(event));
    })
    .doOnComplete(() -> {
        log.info("=== 智能问答工作流执行完成 ===");
        callback.onComplete();
    })
    .doOnError(error -> {
        log.error("智能问答工作流执行错误", error);
        callback.onError(error);
    })
    .subscribe();

callback.registerDisposable(disposable, planAgent, requestId);
```


### 6.3 前端流式消费实现

前端使用原生 **`fetch` + `ReadableStream` + `TextDecoder`** 手动消费 SSE 流，相比 `EventSource` 的优势是：**支持 POST 请求、自定义请求体、携带 AbortSignal 主动中断**。

#### 6.3.1 API 层：chat.ts

```typescript
// chat.ts
export const StreamConstants = {
  TYPE_DATA: 0,          // 正文 token
  TYPE_ERROR: 1,         // 异常
  TYPE_TRACE: 2,         // traceId 推送
  TYPE_REFUSAL: 3,       // 拒答
  TYPE_RAG_KNOWLEDGE: 5, // 知识库引用
  TYPE_STEP: 6,          // 步骤消息
} as const;

export const chatApi = {
  stream: (
    id: string,
    question: string,
    sessionId: string | undefined,
    userId: string | undefined,
    model: string | undefined,
    onChunk: (text: string) => void,         // 普通 token
    onCitations?: (citations: any) => void,  // RAG 引用
    onTrace?: (trace: ChatStreamTraceEvent) => void,
    onRefusalFlag?: (payload: ChatStreamRefusalFlagEvent) => void,
    domain?: string,
    signal?: AbortSignal
  ): Promise<void> => {
    return fetch(`/api/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, question, sessionId, userId, domain, ...(model ? { model } : {}) }),
      signal,  // 支持调用方中断请求
    }).then(res => {
      const reader = res.body!.getReader();
      const decoder = new TextDecoder();
      let lineBuffer = '';
      let finishReason: string | null = null;

      const removeDataprefix = (line: string) =>
        line.startsWith('data:') ? line.slice(5).trimStart() : line;

      // 核心事件分发
      const handleEventBlock = (lineData: any) => {
        if (lineData.type === StreamConstants.TYPE_RAG_KNOWLEDGE) {
          onCitations?.(lineData);
        } else if (lineData.type === StreamConstants.TYPE_DATA) {
          if (lineData.data) onChunk(lineData.data);
        } else if (lineData.type === StreamConstants.TYPE_REFUSAL) {
          onRefusalFlag?.(lineData);
        } else if (lineData.type === StreamConstants.TYPE_TRACE) {
          onTrace?.(lineData.data);
        } else if (lineData.type === StreamConstants.TYPE_STEP) {
          // 步骤消息（扩展用）
        } else {
          if (lineData.data) onChunk(lineData.data);
        }

        // 检测异常终止（finishReason 非 stop）
        if (!finishReason && lineData.finishReason) {
          finishReason = lineData.finishReason;
        }
        if (finishReason && finishReason.toLowerCase() !== 'stop' && finishReason !== 'null') {
          onChunk(`....对话异常终止,终止原因：${finishReason}`);
          finishReason = null;
        }
      };

      const read = (): Promise<void> =>
        reader.read().then(({ done, value }) => {
          if (done) {
            // 处理缓冲区剩余数据
            if (lineBuffer.trim()) {
              const lines = lineBuffer.split('\n');
              lines.forEach(line => {
                line = removeDataprefix(line);
                try { handleEventBlock(JSON.parse(line.trim())); } 
                catch (e) { onChunk('处理数据报错：' + lineBuffer + '<br/>' + e); }
              });
            }
            return;
          }

          // 流式解码并逐行解析
          lineBuffer += decoder.decode(value, { stream: true });
          const lines = lineBuffer.split('\n');
          lineBuffer = lines.pop() || '';  // 保留未完整的一行

          lines.forEach(line => {
            line = removeDataprefix(line);
            if (!line.trim()) return;
            try {
              const jsonData = JSON.parse(line);
              if (jsonData.length) {
                jsonData.forEach((item: any) => handleEventBlock(item));
              } else {
                handleEventBlock(jsonData);
              }
            } catch (e) {
              onChunk(line);  // 非 JSON 按纯文本展示
            }
          });

          return read();  // 递归读取下一块
        });

      return read();
    });
  }
};
```


#### 6.3.2 调用示例（Vue 组件中）

```typescript
import { chatApi } from '@/api/chat';

const controller = new AbortController();

chatApi.stream(
  'q001', '如何配置 Kubernetes 集群？', 'session-001', 'user-001', 'deepseek-v4-pro',
  (text) => { console.log('token:', text); },           // onChunk
  (citations) => { console.log('引用:', citations); },  // onCitations
  (trace) => { console.log('traceId:', trace.traceId); }, // onTrace
  (refusal) => { console.log('拒答:', refusal); },       // onRefusal
  'kubernetes',
  controller.signal
);

// 用户点击"停止生成"时中断
// controller.abort();
```


### 6.4 协议约定：ServerEvent 事件类型

后端 `RagQAService.streamFluxAdapted` 中通过 `ServerEvent` 封装各类消息，序列化为 JSON 后推送。前端按 `type` 字段路由：

| 类型常量 | 值 | 含义 | 示例 `data` |
|---------|---|------|-----------|
| `TYPE_DATA` | `0` | LLM 生成的文本 token | `"这是答案的第一句"` |
| `TYPE_ERROR` | `1` | 执行异常 | — |
| `TYPE_TRACE` | `2` | 链路追踪 ID | `{"traceId":"abc123","id":"q001"}` |
| `TYPE_REFUSAL` | `3` | 拒答事件 | `{"data":"根据当前文档无法回答..."}` |
| `TYPE_RAG_KNOWLEDGE` | `5` | 检索到的知识库引用 | `{"ragKnowledge":[...],"confidence":0.92}` |
| `TYPE_STEP` | `6` | 工作流步骤标记（扩展） | — |

> **注意**：由于后端通过 `JsonUtil.object2json(event)` 将整个 `ServerEvent` 对象序列化后作为 SSE `data` 字段发送，前端实际收到的是包含 `type`、`data`、`confidence`、`finishReason` 等字段的完整 JSON 对象，而非纯文本 token。

### 6.5 异常处理与资源清理

| 场景 | 后端行为 | 前端行为 |
|------|---------|---------|
| 前端正常关闭 | `emitter.onCompletion` 触发 → `planAgent.shutdown()` + `disposable.dispose()` | `controller.abort()` 或直接关闭页面 |
| 120s 超时 | `emitter.onTimeout` 触发 → 同上清理 | `fetch` 自动结束，`reader.read()` 返回 `done: true` |
| 工作流异常 | `callback.onError()` → `emitter.completeWithError(e)` | `fetch` Promise reject，可捕获弹窗提示 |
| 后端服务重启 | TCP 断开，emitter 清理 | 前端需兜底重连或提示用户刷新 |

---

## 7. 集成要点与注意事项

1. **ApiKey 一致性**：服务端 `registMcpBeanTool(apiKey, bean)` 与客户端 `mcpserver.properties` 中的 `apiKeyId` 必须一致，否则客户端无法调用。
2. **Bean 可见性**：工具类需标注 `@Service`（或 `@Component`）并被 Spring 管理，才能在 `MCPServerController` 中注入注册。
3. **多工具注册**：可多次调用 `mcpApiKeyService.registMcpBeanTool()` 将不同 Bean 注册到同一或不同 `apiKey` 下。
4. **线程安全**：工具类方法应保证无状态或线程安全，避免并发问题。
5. **网络配置**：若服务端与客户端部署在不同机器，需将 `mcpserver.properties` 中的 `hosts` 改为实际可访问的 IP 或域名。
6. **端点路径**：若服务端有上下文路径（如 `/bboss-biz-srv`），需在客户端通过 `extendConfigs.streamableendpoint` 指定完整端点。

