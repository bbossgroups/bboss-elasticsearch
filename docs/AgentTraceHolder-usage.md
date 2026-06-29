# AgentTraceHolder 使用文档

## 一、概述

`AgentTraceHolder` 是 bboss-ai 框架提供的 **AI Agent 执行追踪与事件上报工具类**，用于在工具调用、MCP（Model Context Protocol）通信等关键环节记录执行轨迹、上报调试信息以及向客户端推送流式事件。

核心能力包括：
- **执行耗时追踪**：记录工具/MCP 调用的起止时间
- **调用链路上报**：将调用参数、响应结果、角色信息等封装为追踪消息
- **服务器事件推送**：在工具执行过程中实时向客户端发送流式数据

---

## 二、核心 API

### 2.1 `isToolTrace`

```java
public static boolean isToolTrace()
```

**作用**：判断当前是否开启了工具调用追踪模式。

**说明**：在记录追踪信息前，应先调用此方法进行判断，避免在追踪关闭时产生不必要的对象创建和性能开销。

---

### 2.2 `trace`

```java
public static void trace(TraceMessage traceMessage)
```

**作用**：将一条追踪消息上报到追踪系统。

**参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `traceMessage` | `TraceMessage` | 追踪消息对象，需包含起止时间、业务消息体 |

**`TraceMessage` 常用属性**：

| 属性 | 类型 | 说明 |
|------|------|------|
| `startTime` | `long` | 调用开始时间戳（毫秒） |
| `endTime` | `long` | 调用结束时间戳（毫秒） |
| `message` | `Map` | 业务消息体，可自定义键值对 |

---

### 2.3 `emitterServerEvent`

```java
public static void emitterServerEvent(ServerEvent serverEvent)
```

**作用**：向客户端发送服务器事件，常用于流式输出场景。

**参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `serverEvent` | `ServerEvent` | 服务器事件对象 |

**`ServerEvent` 常用属性**：

| 属性 | 类型 | 说明 |
|------|------|------|
| `type` | `String` | 事件类型，如 `ServerEvent.TYPE_DATA` |
| `data` | `String` | 事件数据内容 |

## 三、使用场景与示例

场景1、场景2、场景3做为内置逻辑；可以参考场景4在自定义工具方法内部调用trace和emitterServerEvent方法。

### 场景 1：本地 Bean 工具方法调用追踪

适用于通过 `@Tool` 注解标记的本地 Java 方法，记录方法入参和返回结果。

```java
TraceMessage traceMessage = null;
Map message = null;

// 1. 判断追踪是否开启
if (AgentTraceHolder.isToolTrace()) {
    traceMessage = new TraceMessage();
    traceMessage.setStartTime(System.currentTimeMillis());
    message = new HashMap();
    // 记录工具调用参数
    message.put("toolCallArgs", !isEmptyParameters() ? functionTool.getArguments() : null);
}

// 2. 执行实际的工具方法
Object result = toolMethod.invoke(toolBean, getArgs(functionTool));

// 3. 记录调用结果并上报
if (AgentTraceHolder.isToolTrace()) {
    traceMessage.setEndTime(System.currentTimeMillis());
    message.put("toolCallResponse", result);
    message.put("role", SessionMessage.MESSAGE_TYPE_TOOLCALL_MESSAGE_NAME);
    traceMessage.setMessage(message);
    AgentTraceHolder.trace(traceMessage);
}
```

**关键点**：
- 在调用**前**记录 `startTime` 和请求参数
- 在调用**后**记录 `endTime` 和响应结果
- `role` 标识消息类型为工具调用（`MESSAGE_TYPE_TOOLCALL_MESSAGE_NAME`）

---

### 场景 2：MCP SSE 调用追踪

适用于通过 SSE（Server-Sent Events）方式调用 MCP 服务的场景。

```java
TraceMessage traceMessage = null;

if (AgentTraceHolder.isToolTrace()) {
    traceMessage = new TraceMessage();
    traceMessage.setStartTime(System.currentTimeMillis());
}

try {
    HttpRequestProxy.sendJsonBody(mcpClient.getMcpServer(), mcpToolCallRequest,
            mcpClient.getMessagePath(), String.class);
} catch (Exception e) {
    this.removeMcpCallObject(requestId);
    throw new McpCallException(e);
}

MCPToolCallResponse mcpToolCallResponse = handleResponse(mcpCallObject);

if (AgentTraceHolder.isToolTrace()) {
    traceMessage.setEndTime(System.currentTimeMillis());
    Map message = new HashMap();
    message.put("mcpserver", mcpClient.getMcpServer());
    message.put("mcpToolCallRequest", mcpToolCallRequest);
    message.put("mcpToolCallResponse", mcpToolCallResponse);
    message.put("role", SessionMessage.MESSAGE_TYPE_MCPCALL_MESSAGE_NAME);
    traceMessage.setMessage(message);
    AgentTraceHolder.trace(traceMessage);
}
```

**关键点**：
- 即使 MCP 调用发生异常，也应在异常处理逻辑中考虑是否需要上报错误追踪（示例中异常时直接抛出，未记录异常追踪）
- `role` 标识为 MCP 调用（`MESSAGE_TYPE_MCPCALL_MESSAGE_NAME`）

---

### 场景 3：MCP 流式调用追踪

适用于流式（Streamable）MCP 客户端的直接 HTTP 调用。

```java
TraceMessage traceMessage = null;

if (AgentTraceHolder.isToolTrace()) {
    traceMessage = new TraceMessage();
    traceMessage.setStartTime(System.currentTimeMillis());
}

MCPToolCallResponse mcpToolCallResponse = HttpRequestProxy.sendJsonBody(
        getMcpServer(), mcpToolCallRequest, headers, streamablePath,
        MCPToolCallResponse.class);

if (AgentTraceHolder.isToolTrace()) {
    traceMessage.setEndTime(System.currentTimeMillis());
    Map message = new HashMap();
    message.put("mcpserver", getMcpServer());
    message.put("mcpToolCallRequest", mcpToolCallRequest);
    message.put("mcpToolCallResponse", mcpToolCallResponse);
    message.put("role", SessionMessage.MESSAGE_TYPE_MCPCALL_MESSAGE_NAME);
    traceMessage.setMessage(message);
    AgentTraceHolder.trace(traceMessage);
}
```

---

### 场景 4：工具执行过程中推送流式事件

适用于在工具方法内部需要向客户端实时推送中间结果（如查询到的酒店列表、处理进度等）。

```java
ServerEvent serverEvent = new ServerEvent();
serverEvent.setType(ServerEvent.TYPE_DATA);
serverEvent.setData("hotels:" + JsonUtil.object2jsonPretty(hotels));
AgentTraceHolder.emitterServerEvent(serverEvent);
```

**关键点**：
- 事件类型使用 `ServerEvent.TYPE_DATA` 表示数据类型事件
- `data` 为字符串内容，通常使用 JSON 序列化复杂对象
- 此方法可在工具执行的**任意中间阶段**调用，不限于开始或结束

---

## 四、最佳实践

| 实践建议 | 说明 |
|----------|------|
| **始终包裹在 `isToolTrace()` 判断中** | 避免追踪关闭时无意义的性能损耗 |
| **成对记录 `startTime` 和 `endTime`** | 确保耗时统计准确，建议紧邻业务代码前后 |
| **统一 `role` 标识** | 根据调用类型使用框架定义的常量，如 `MESSAGE_TYPE_TOOLCALL_MESSAGE_NAME`、`MESSAGE_TYPE_MCPCALL_MESSAGE_NAME` |
| **异常场景考虑补全追踪** | 在 `catch` 块中可补充记录异常信息并上报，避免调用失败时追踪链路断裂 |
| **流式事件与追踪消息区分使用** | `trace()` 用于后端链路追踪；`emitterServerEvent()` 用于向客户端推送实时数据 |
