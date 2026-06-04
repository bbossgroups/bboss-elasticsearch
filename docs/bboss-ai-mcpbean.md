# MCP Bean Tool 服务使用文档

本文档介绍如何通过 bboss 框架，将基于注解定义的 Java Bean 工具发布为 MCP（Model Context Protocol）服务，并通过 bboss MCP 客户端注册到 AI 智能体工作流中实现远程调用。

将基于注解定义的 Java Bean 工具直接注册到智能体工作流使用参考文档：https://esdoc.bbossgroups.com/#/bboss-ai-toolannotation

## 一、整体架构流程

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│  注解定义工具    │  -->  │  发布为MCP服务    │  -->  │ 智能体远程调用   │
│  PreOrderTool   │      │ MCPServerController│      │  MCPToolsRegist │
└─────────────────┘      └──────────────────┘      └─────────────────┘
```


1. **定义工具**：在普通 Java Bean 的方法上使用 `@Tool` 和 `@ToolParam` 注解声明工具及参数。
2. **发布服务**：通过 `MCPApiKeyServiceImpl` 注册 Bean 工具，由 `MCPToolServiceImpl` 对外提供 SSE / Streamable / Message 协议端点。
3. **客户端注册**：在 `mcpserver.properties` 中配置 MCP 服务地址和认证信息。
4. **智能体调用**：通过 `MCPToolsRegist` 将远程 MCP 服务绑定到 `AINodeAgent`，供 AI 工作流自动识别和调用。

---

## 二、通过注解定义 Bean 工具

### 2.1 核心注解说明

| 注解 | 作用 | 关键属性 |
|---|---|---|
| `@Tool` | 声明一个 MCP 工具方法 | `name`：工具名称；`description`：功能描述 |
| `@ToolParam` | 声明工具参数 | `name`：参数名；`description`：参数描述；`required`：是否必填 |

### 2.2 工具类示例

参考文件：[PreOrderTool.java](https://gitee.com/bboss/bbootdemo/blob/master/src/main/java/org/frameworkset/service/PreOrderTool.java)

```java
package org.frameworkset.service;

import org.frameworkset.spi.ai.model.annotation.Tool;
import org.frameworkset.spi.ai.model.annotation.ToolParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PreOrderTool {

    @Tool(name = "hotelQuery", description = "根据用户的行程需求，查询合适的酒店。")
    public List<Map> hotelQuery(
            @ToolParam(name = "startDay", description = "入驻时间,例如：5月25日", required = true) String startDay,
            @ToolParam(name = "endDay", description = "离房时间,例如：5月28日", required = true) String endDay) {
        
        List<Map> hotels = new ArrayList<>();
        Map hotelData = new LinkedHashMap();
        hotelData.put("name", "迁移山水酒店");
        hotelData.put("price", "300$");
        hotelData.put("score", 80);
        hotelData.put("devices", "配套设施：健身房、保龄球");
        hotelData.put("position", "位于市中心，交通便利");
        hotels.add(hotelData);
        // ... 更多酒店数据
        return hotels;
    }

    @Tool(name = "flightQuery", description = "根据用户的行程需求，查询合适的航班机票。")
    public List<Map> flightQuery(
            @ToolParam(name = "bookDay", description = "出发时间,例如：5月25日", required = true) String bookDay,
            @ToolParam(name = "arriveDay", description = "到达时间,例如：5月28日", required = true) String arriveDay,
            @ToolParam(name = "fromStation", description = "出发地,例如：长沙", required = true) String fromStation,
            @ToolParam(name = "toStation", description = "到达地,例如：北京", required = true) String toStation) {
        
        List<Map> flights = new ArrayList<>();
        Map flightData = new LinkedHashMap();
        flightData.put("name", "国航6678");
        flightData.put("price", "300$");
        flightData.put("score", 80);
        flightData.put("devices", "波音777");
        flightData.put("leaveTime", "14点30分");
        flightData.put("arrivedTime", "17点30分");
        flightData.put("description", "宽体大飞机，准点率99%");
        flights.add(flightData);
        // ... 更多航班数据
        return flights;
    }
}
```


**要点说明**：
- 工具类是一个普通 Java Bean，无需继承特定接口。
- `@Tool` 的 `name` 将成为大模型识别和调用该工具的标识。
- `@ToolParam` 的 `description` 会作为提示词的一部分传递给大模型，帮助其正确填充参数。

---

## 三、将 Bean 工具发布为 MCP 服务

### 3.1 服务端 Controller 实现

参考文件：[MCPServerController.java](https://gitee.com/bboss/bbootdemo/blob/master/src/main/java/org/frameworkset/web/mcp/MCPServerController.java)

```java
package org.frameworkset.web.mcp;

import org.frameworkset.service.PreOrderTool;
import org.frameworkset.spi.InitializingBean;
import org.frameworkset.spi.ai.mcp.tools.server.MCPApiKeyServiceImpl;
import org.frameworkset.spi.ai.mcp.tools.server.MCPToolService;
import org.frameworkset.spi.ai.mcp.tools.server.MCPToolServiceImpl;
import org.frameworkset.spi.remote.http.HttpRequestProxy;
import org.frameworkset.util.annotations.RequestBody;
import org.frameworkset.util.annotations.RequestHeader;
import org.frameworkset.util.annotations.ResponseBody;
import reactor.core.publisher.Flux;

public class MCPServerController implements InitializingBean {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MCPServerController.class);

    private MCPToolService mcpService;

    /** SSE 协议端点（MCP 标准协议） */
    public @ResponseBody Flux<String> sse(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        String apiKey = HttpRequestProxy.extractApiKeyFromBearer(authorizationHeader);
        return mcpService.sse(apiKey);
    }

    /** Message 协议端点 */
    public @ResponseBody String message(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            String sessionId,
            @RequestBody String requestBody) {
        String apiKey = HttpRequestProxy.extractApiKeyFromBearer(authorizationHeader);
        return mcpService.message(apiKey, sessionId, requestBody);
    }

    /** Streamable HTTP 协议端点 */
    public @ResponseBody Object streamable(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody String requestBody) {
        String apiKey = HttpRequestProxy.extractApiKeyFromBearer(authorizationHeader);
        return mcpService.streamable(apiKey, requestBody);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 1. 创建 API Key 服务
        MCPApiKeyServiceImpl mcpApiKeyService = new MCPApiKeyServiceImpl();
        
        // 2. 注册 Bean 工具（apiKey 为 123456，工具实例为 PreOrderTool）,可以多次调用registMcpBeanTool方法，为同一个apiKey或者多个apiKey注册多个工具实例
        mcpApiKeyService.registMcpBeanTool("123456", new PreOrderTool());
        
        // 3. 创建 MCP 服务实现并关联 API Key 服务
        MCPToolServiceImpl mcpService = new MCPToolServiceImpl();
        mcpService.setMcpApiKeyService(mcpApiKeyService);
        
        this.mcpService = mcpService;
    }
}
```


### 3.2 发布流程说明

1. **初始化 `MCPApiKeyServiceImpl`**：负责管理 API Key 与工具的映射关系。
2. **注册 Bean 工具**：调用 `registMcpBeanTool(apiKey, beanInstance)` 将带有 `@Tool` 注解的 Bean 实例注册到指定 API Key 下。
3. **注册多个Bean 工具**可以多次调用registMcpBeanTool方法，为同一个apiKey或者多个apiKey注册多个工具实例
3. **初始化 `MCPToolServiceImpl`**：核心服务实现，负责处理 MCP 协议请求。
4. **暴露 HTTP 端点**：通过 `sse()`、`message()`、`streamable()` 三种协议对外提供服务。

---

## 四、配置 MCP 客户端连接

参考文件：[mcpserver.properties](https://gitee.com/bboss/bboss-ai/blob/main/bboss-ai-flow/src/test/resources/mcpserver.properties)

在客户端项目的资源目录下创建 `mcpserver.properties`，配置远程 MCP 服务连接信息：

```properties
## beanmcp mcp模型服务配置：在代码中引用服务的名称为beanmcp
# 服务连接池参数
beanmcp.http.maxTotal = 200
beanmcp.http.defaultMaxPerRoute = 200

# beanmcp 服务地址
beanmcp.http.hosts=127.0.0.1:808

# 基于 apiKeyId 认证配置（需与服务端注册的 apiKey 一致）
beanmcp.http.apiKeyId = 123456

# Streamable HTTP 协议端点
beanmcp.http.extendConfigs.streamableendpoint = /demoproject/mcp/streamable.api

# SSE 协议端点（如使用 SSE 协议）
# beanmcp.http.extendConfigs.sseendpoint = /demoproject/mcp/sse.api
```


**配置要点**：

| 配置项 | 说明                                            |
|---|-----------------------------------------------|
| `beanmcp.http.hosts` | MCP 服务地址和端口，多个地址用逗号分隔（集群部署场景）                 |
| `beanmcp.http.apiKeyId` | 认证密钥，必须与 `registMcpBeanTool` 时使用的 `apiKey` 一致 |
| `streamableendpoint` | Streamable HTTP 端点路径                          |
| `sseendpoint` | SSE 端点路径（可选）                                  |

---

## 五、在智能体工作流中注册和调用 MCP 工具

流式调用参考文件：[McpBookingStreamTest.java](https://gitee.com/bboss/bboss-ai/blob/main/bboss-ai-flow/src/test/java/org/frameworkset/spi/ai/tools/McpBookingStreamTest.java)


### 5.1 初始化 HTTP 连接池

```java
HttpRequestProxy.startHttpPools("application-stream.properties"); // 大模型服务配置，启动大模型服务
HttpRequestProxy.startHttpPools("mcpserver.properties");          // MCP 服务配置，启动mcp client服务
```


### 5.2 注册 MCP 工具到智能体

```java
import org.frameworkset.spi.ai.mcp.tools.MCPToolsRegist;
import org.frameworkset.spi.ai.tool.BeanToolsRegist;
import org.frameworkset.spi.ai.flow.AINodeAgent;

// 定义 MCP 工具注册器（"beanmcp" 对应 mcpserver.properties 中的服务名称）
ToolsRegist toolsRegist = new MCPToolsRegist("beanmcp");

// 将 MCP 工具注册器绑定到智能体节点
planAgent.addRouteChoiceAgent(new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，只查询并推荐合适的酒店..." +
        "如未匹配到工具，请返回\"未找到匹配的酒店查询工具\"")
        .setAgentId("hotelAgent")
        .setAgentName("酒店查询智能体")
        .setToolsRegist(toolsRegist));  // 绑定 MCP 工具
```


### 5.3 工作流中的关键片段解析

```java
// ====================  路由智能体（判断用户意图） ====================
planAgent.addAgent(new AIRouteAgent()
        .setAgentId("bookingRouter").setAgentName("预定路由智能体")
        .setSystemPrompt("你是一个行程预定路由智能体。请分析用户的问题，判断用户需要预定什么...")
        .addRoutingChoice("hotelAgent", "用户只需要预定酒店")
        .addRoutingChoice("flightAgent", "用户只需要预定机票")
        .addRoutingChoice("bothAgent", "用户需要同时预定酒店和机票")
);

// ==================== 阶段2：分支查询智能体 ====================
// 酒店查询智能体（当路由到 hotelAgent 时执行）
planAgent.addRouteChoiceAgent(new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，只查询并推荐合适的酒店...")
        .setAgentId("hotelAgent")
        .setAgentName("酒店查询智能体")
        .setToolsRegist(toolsRegist));  // 使用同一个 MCP 工具注册器

// 机票查询智能体（当路由到 flightAgent 时执行）
planAgent.addRouteChoiceAgent(new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，只查询并推荐合适的航班和机票...")
        .setAgentId("flightAgent")
        .setAgentName("机票查询智能体")
        .setToolsRegist(toolsRegist));

// ==================== 阶段3：并行查询智能体 ====================
AIParrelAgent bothAgent = new AIParrelAgent(planAgent)
        .setAgentId("bothAgent").setAgentName("并行查询智能体");

bothAgent.addAgent(new AINodeAgent("...")
        .setAgentId("parrelHotelAgent")
        .setToolsRegist(toolsRegist));

bothAgent.addAgent(new AINodeAgent("...")
        .setAgentId("parrelFlightAgent")
        .setToolsRegist(toolsRegist));

planAgent.addRouteChoiceAgent(bothAgent);
```


### 5.4 执行工作流

```java
// 创建会话消息
ChatAgentMessage chatAgentMessage = new ChatAgentMessage()
        .setModel("qwen3.7-plus")
        .setStream(true)
        .setMaas("qwenvlplus")
        .setPrompt("我计划5月25日到5月28日从长沙去北京出差，帮我预定酒店和机票");

// 定义工作流智能体
AIPlanAgent planAgent = new AIPlanAgent(new StoreContext()
        .setSessionId(sessionId).setUserId("user123")
        .setStoreType(StoreContext.STORE_TYPE_DB)
        .setDataSource("visualops"))
        .setAgentMessage(chatAgentMessage)
        .setAgentName("预定工作流智能体")
        .setAgentId("bookingWorkflowAgent");

// ... 添加路由和工具节点 ...

// 流式执行
Flux<ServerEvent> flux = planAgent.chatStream();
flux.doOnNext(event -> {
    if (event.getData() != null) {
        System.out.print(event.getData());
    }
    if (event.isToolCallsType()) {
        System.out.println("开始执行工具：");
    }
}).subscribe();
```


---

## 六、完整调用链路总结

| 步骤 | 组件 | 关键动作 |
|---|---|---|
| **1. 定义** | `PreOrderTool` | 使用 `@Tool` / `@ToolParam` 注解声明工具和参数 |
| **2. 注册** | `MCPApiKeyServiceImpl` | `registMcpBeanTool("123456", new PreOrderTool())` |
| **3. 发布** | `MCPServerController` | 暴露 SSE / Message / Streamable HTTP 端点 |
| **4. 配置** | `mcpserver.properties` | 配置 `beanmcp.http.hosts`、`apiKeyId`、`streamableendpoint` |
| **5. 加载** | `MCPToolsRegist` | `new MCPToolsRegist("beanmcp")` 加载远程工具列表 |
| **6. 绑定** | `AINodeAgent` | `setToolsRegist(toolsRegist)` 将工具绑定到智能体 |
| **7. 调用** | `AIPlanAgent` | 大模型根据提示词自动决策调用 `hotelQuery` 或 `flightQuery` |

---

## 七、注意事项

1. **API Key 一致性**：服务端 `registMcpBeanTool` 注册的 `apiKey` 必须与客户端 `mcpserver.properties` 中的 `apiKeyId` 保持一致，否则认证失败。
2. **协议选择**：bboss 支持 SSE 和 Streamable HTTP 两种 MCP 传输协议，根据实际场景在 `mcpserver.properties` 中配置对应的 `sseendpoint` 或 `streamableendpoint`。
3. **服务名称对应**：`MCPToolsRegist("beanmcp")` 中的名称必须和 `mcpserver.properties` 配置前缀一致（如 `beanmcp.http.hosts`）。
4. **工具描述质量**：`@Tool` 和 `@ToolParam` 的 `description` 直接影响大模型对工具的理解和参数填充的准确性，建议描述清晰、包含示例。