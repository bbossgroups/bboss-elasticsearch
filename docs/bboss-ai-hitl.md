# 人工介入 Hitl（Human-in-the-Loop）功能使用文档

## 1. 功能概述

Hitl（Human-in-the-Loop）人工介入功能允许在智能体执行过程中，当遇到需要人工决策的关键节点时，暂停智能体执行并等待人工介入处理。该功能适用于以下场景：

| 适用场景 | 说明 |
|---------|------|
| 复杂问题需要人类专业判断 | AI无法独立完成，需要领域专家介入 |
| 敏感操作需要人工确认 | 如代码修改、数据删除等高危操作 |
| 任务执行结果不符合预期 | 需要人工调整后继续执行 |
| 超出AI权限范围的操作 | 需要人工授权或审批 |

 人工反馈数据和智能体之间数据交互机制：

 \- 智能体采用单节点单进程模式部署时，用户和智能体检通过内存共享数据，进行智能体中断和唤醒处理；

 \- 智能体采用集群模式部署时，用户和智能体之间数据交互通过redis发布/订阅模式实现数据共享，进行智能体中断和唤醒处理

 \- 如果接收人工提交数据的节点就是中断智能体所在的节点时，无需通过redis发布/订阅模式进行数据交互，直接通过内存共享数据即可

 \- 采用内置人工介入工具HitlTaskcallTool，实现Hitl功能，亦可以自定义人工介入工具，实现自定义Hitl功能



## 2. 核心概念

### 2.1 核心组件

| 组件 | 职责 | 位置 |
|-----|------|------|
| `HitlTaskHelper` | 人工任务管理核心类，负责任务创建、存储、处理和状态管理 | `hitl/HitlTaskHelper.java` |
| `HitlTaskcallTool` | 内置人工介入工具，供LLM调用触发人工介入 | `tools/HitlTaskcallTool.java` |
| `HitlCallTask` | 人工介入任务实体，包含任务状态、内容、结果等信息 | `hitl/HitlCallTask.java` |
| `HitlCallObject` | 任务执行上下文，封装任务对象和同步机制 | `hitl/HitlCallObject.java` |
| `RedisHitlTaskCallListener` | 集群模式下的Redis订阅监听器 | `hitl/cluster/RedisHitlTaskCallListener.java` |
| `RedisHitlTaskCallNotifier` | 集群模式下的Redis发布通知器 | `hitl/cluster/RedisHitlTaskCallNotifier.java` |

### 2.2 任务存储和任务状态

人工任务数据会保存到关系数据库中，根据人工介入处理进展，实时更新任务状态

| 状态码 | 状态名称 | 说明 |
|-------|---------|------|
| `0` | 待处理 | 任务已创建，等待人工处理 |
| `1` | 已处理 | 人工已完成处理并同意继续 |
| `2` | 已拒绝 | 人工拒绝该任务 |
| `3` | 超时忽略 | 任务等待超时，自动跳过 |
| `4` | 销毁任务 | 任务被主动销毁 |
| `5` | 已结束 | 任务正常结束 |

人工任务处理完毕（处理或者拒绝）或者超时忽略以及销毁后，自动结束，可以定时清理已结束任务。

### 2.3 数据交互机制

根据智能体部署模式的不同，Hitl采用不同的数据交互机制：

```
┌─────────────────────────────────────────────────────────────────┐
│                    Hitl 数据交互架构                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────┐         ┌───────────────┐                   │
│  │   单节点模式   │         │   集群模式     │                   │
│  ├───────────────┤         ├───────────────┤                   │
│  │ 内存共享数据   │         │ Redis 发布/订阅│                   │
│  │ 直接中断唤醒   │         │ 跨节点消息传递 │                   │
│  └───────────────┘         └───────────────┘                   │
│         │                          │                           │
│         └──────────┬───────────────┘                           │
│                    ▼                                           │
│         ┌───────────────────┐                                  │
│         │   智能体中断/唤醒   │                                  │
│         └───────────────────┘                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**智能体中断与唤醒流程：**

```
智能体执行 → 调用HitlTaskcallTool → 创建HitlCallTask → 持久化到数据库
    ↓
推送任务到客户端 → 智能体阻塞等待 → 人工处理任务 → 任务状态更新
    ↓
状态变更通知 → 智能体唤醒继续执行
```

## 3. 部署模式

### 3.1 单节点模式

适用于智能体单进程部署场景，通过内存共享数据实现人工介入：

```java
// 单节点模式初始化
AgentSessionService agentSessionService = new AgentSessionServiceImpl();
agentSessionService.setDatasource("visualops");
agentSessionService.setHitlDatasource("visualops");

HitlTaskHelper.getHitlTaskHelper()
        .setAgentSessionService(agentSessionService)
        .init();
```

### 3.2 集群模式

适用于智能体集群部署场景，通过Redis发布/订阅模式实现跨节点通信：

```java
// 集群模式初始化（需先配置Redis）
AgentSessionService agentSessionService = new AgentSessionServiceImpl();
agentSessionService.setDatasource("visualops");

HitlTaskHelper.getHitlTaskHelper()
        .setAgentSessionService(agentSessionService)
        .setRedisChannel("test", RedisHitlTaskCallListener.DEFAULT_CHANNEL) //设置redis数据源test，以及消息通道
        .init();
```

### 3.3 Redis配置示例

```java
RedisConfig redisConfig = new RedisConfig();
redisConfig.setName("test")
        .setAuth("password")
        .setServers("101.13.6.7:6381,102.13.6.7:6382,103.13.6.7:6383")
        .setMode(RedisDB.mode_cluster)
        .setConnectionTimeout(10000)
        .setSocketTimeout(10000)
        .setPoolMaxWaitMillis(2000)
        .setPoolMaxTotal(50);
RedisFactory.builRedisDB(redisConfig);
```

定义名称为test的redis数据源，用于人工接入任务中，人与智能体之间传递和共享数据。

## 4. 快速开始

### 4.1 依赖配置

确保项目已引入 `bboss-ai` 依赖：

```xml
<dependency>
    <groupId>com.bbossgroups</groupId>
    <artifactId>bboss-ai</artifactId>
    <version>6.5.6</version>
</dependency>
```

### 4.2 完整使用示例

```java
// 1. 初始化数据源
SQLUtil.startPool("visualops",
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://localhost:3306/bboss",
        "root", "password",
        "select 1");

// 2. 配置AgentSessionService
AgentSessionService agentSessionService = new AgentSessionServiceImpl();
agentSessionService.setDatasource("visualops");

// 3. 初始化HitlTaskHelper（单节点模式）
HitlTaskHelper.getHitlTaskHelper()
        .setAgentSessionService(agentSessionService)
        .init();

// 4. 创建智能体并注册Hitl工具
AIAgent agent = new AIAgent();
agent.setEnableLoopToolCall(true);
agent.setMaxLoopToolCalls(80);
agent.registTools(new SkillsToolRegist()
                .addClasspathSkills("skills"))
        .registBeanTool(new HitlTaskcallTool());  // 注册人工介入工具

// 5. 构建消息并调用
ChatAgentMessage message = new ChatAgentMessage();
message.setMaas("deepseek")
        .setModel("deepseek-v4-pro")
        .setPrompt("请评审并修复Java代码问题")
        .setStoreContext(new StoreContext()
                .setUserId("user123")
                .setRequestId("request123")
                .setStoreType(StoreContext.STORE_TYPE_DB)
                .setDataSource("visualops"));

// 6. 流式调用并处理Hitl事件
Flux<ServerEvent> flux = agent.streamChat(message);

flux.doOnNext(chunk -> {
    if (chunk.isHitl()) {
        // 收到人工介入消息
        String hitlTaskId = chunk.getHitlTaskId();
        if (hitlTaskId != null) {
            // 模拟人工处理：通过
            Map<String, Object> hitlTaskData = new LinkedHashMap<>();
            hitlTaskData.put("confirm", "确认修改文件");
            hitlTaskData.put("otherData", "用户补充意见");
            HitlTaskHelper.handleHitlCallTask(hitlTaskData, null, hitlTaskId);
        }
    }
}).subscribe();
```

## 5. API 参考

### 5.1 HitlTaskHelper 核心方法

| 方法 | 功能说明 | 参数 |
|-----|---------|------|
| `getHitlTaskHelper()` | 获取单例实例 | 无 |
| `setAgentSessionService()` | 设置会话服务 | `agentSessionService` |
| `setRedisChannel()` | 配置Redis通道（集群模式） | `redisName`, `channel` |
| `init()` | 初始化Hitl服务 | 无 |
| `createHitlCallTask()` | 创建人工介入任务 | `hitlTaskReason`, `chatObject` |
| `handleHitlCallTask()` | 处理人工任务（同意） | `hitlTaskData`, `throwable`, `hitlTaskId` |
| `refuseHitlCallTask()` | 拒绝人工任务 | `hitlTaskData`, `throwable`, `hitlTaskId` |
| `getHitlCallTask()` | 获取任务详情 | `hitlTaskId` |
| `destory()` | 销毁Hitl服务 | 无 |

### 5.2 HitlTaskcallTool 工具定义

```java
@Tool(name = "hitlTaskTool", description = "人工介入工具...")
public Map<String,Object> hitlTaskTool(
    @ToolParam(name = "hitlTaskReason", required = true, 
        description = "人工介入原因，需包含：1.任务背景与已执行步骤 2.当前卡住的具体原因 3.建议关注的关键点 4.期望人类提供的具体帮助")
    String hitlTaskReason
)
```

### 5.3 ServerEvent Hitl 相关属性

| 属性 | 类型 | 说明 |
|-----|------|------|
| `isHitl()` | boolean | 是否为Hitl事件 |
| `getHitlTaskId()` | String | 人工任务ID |
| `getType()` | String | 事件类型，Hitl事件为 `TYPE_HITL` |
| `getData()` | String | 任务原因描述 |

## 6. 配置说明

### 6.1 数据库连接配置

Hitl任务需要持久化到数据库，需配置数据源：

```java
AgentSessionService agentSessionService = new AgentSessionServiceImpl();
agentSessionService.setDatasource("datasourceName");      // 会话存储数据源
agentSessionService.setHitlDatasource("hitlDatasource");  // Hitl任务数据源
```

### 6.2 超时配置

通过 `AIAgent` 设置Hitl任务超时时间：单位毫秒，默认-1，表示无限期等待

```java
AIAgent agent = new AIAgent();
agent.setHitlTaskTimeout(300000L);  // 设置超时时间（毫秒），默认无限等待
```

可以直接在HitlTaskcallTool 上设置超时机制：

```java
new HitlTaskcallTool().setHitlTaskTimeout(300000L);  // 设置超时时间（毫秒），默认无限等待
```

设置超时默认处理机制：

```java
new HitlTaskcallTool().setTimeoutAction(HitlTaskcallTool.TIMEOUT_ACTION_REJECTED)
new HitlTaskcallTool().setTimeoutAction(HitlTaskcallTool.TIMEOUT_ACTION_CONTINUE)    
```

### 6.3 StoreContext 配置

```java
StoreContext storeContext = new StoreContext()
        .setUserId("user123")
        .setRequestId("request123")
        .setSessionSize(100)
        .setStoreType(StoreContext.STORE_TYPE_DB)
        .setDataSource("visualops")
        .setHitlDatasource("visualops");  // Hitl任务专属数据源
```

## 7. 测试案例解析

### 7.1 单节点模式测试 - ChecklistCodeViewAgentHitlTest

**核心配置：**

```java
// 第53-61行：单节点模式配置
AgentSessionService agentSessionService = new AgentSessionServiceImpl();
agentSessionService.setDatasource("visualops");
agentSessionService.setHitlDatasource("visualops");

HitlTaskHelper.getHitlTaskHelper()
        .setAgentSessionService(agentSessionService)
        .init();  // 无需配置Redis
```

**人工处理模拟（第120-140行）：**

```java
if (chunk.isHitl()) {
    String hitlTaskId = chunk.getHitlTaskId();
    if (hitlTaskId != null) {
        // 拒绝任务示例
        Map<String, Object> hitlTaskData = new LinkedHashMap<>();
        hitlTaskData.put("confirm", "不要修改文件");
        hitlTaskData.put("otherData", "暂不修复");
        HitlTaskHelper.refuseHitlCallTask(hitlTaskData, null, hitlTaskId);
    }
}
```

### 7.2 集群模式测试 - ChecklistCodeViewAgentHitlResisTest

**核心配置差异（第57-60行）：**

```java
HitlTaskHelper.getHitlTaskHelper()
        .setAgentSessionService(agentSessionService)
        .setRedisChannel("test", RedisHitlTaskCallListener.DEFAULT_CHANNEL)
        .init();  // 配置Redis通道实现跨节点通信
```

**Redis初始化（第67-91行）：**

```java
RedisConfig redisConfig = new RedisConfig();
redisConfig.setName("test")
        .setAuth("ecs123456")
        .setServers("10.13.6.7:6381,10.13.6.7:6382,...")
        .setMode(RedisDB.mode_cluster)
        // ...其他配置
RedisFactory.builRedisDB(redisConfig);
```

**人工处理模拟（第143-154行）：**

```java
if (chunk.isHitl()) {
    String hitlTaskId = chunk.getHitlTaskId();
    if (hitlTaskId != null) {
        // 同意任务示例
        Map<String, Object> hitlTaskData = new LinkedHashMap<>();
        hitlTaskData.put("confirm", "确认修改文件");
        hitlTaskData.put("otherData", "用户补充意见：各个问题都符合要求，可以整改");
        HitlTaskHelper.handleHitlCallTask(hitlTaskData, null, hitlTaskId);
    }
}
```

## 8. 最佳实践

### 8.1 hitlTaskReason 编写规范

人工介入原因应包含以下三要素，让人类在3秒内快速理解：

```
【任务背景】已完成代码审查，发现5个问题
【卡住原因】需要修改核心配置文件，属于敏感操作
【决策要点】是否确认执行以下修改：
  - 修改AIAgent.java第45行的超时配置
  - 增加异常处理逻辑
【期望帮助】请确认是否执行修改
```

### 8.2 任务数据结构

```java
Map<String, Object> hitlTaskData = new LinkedHashMap<>();
hitlTaskData.put("confirm", "确认修改");  // 必填：确认/拒绝标识
hitlTaskData.put("otherData", "补充说明"); // 可选：用户反馈意见
hitlTaskData.put("reviewer", "张三");      // 可选：处理人信息
hitlTaskData.put("timestamp", System.currentTimeMillis());
```

### 8.3 异常处理

```java
try {
    HitlTaskHelper.handleHitlCallTask(hitlTaskData, null, hitlTaskId);
} catch (HitlCallException e) {
    logger.error("处理Hitl任务失败: {}", hitlTaskId, e);
    // 可以选择重试或标记任务失败
}
```

### 8.4 资源清理

应用关闭时应主动销毁Hitl服务：

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    HitlTaskHelper.destory();
}));
```

## 9. 自定义人工介入工具

bboss AI 支持两种方式实现 Hitl 功能：
1. **使用内置工具**：直接使用 `HitlTaskcallTool`，快速启用人工介入能力
2. **自定义工具**：实现符合业务需求的定制化人工介入逻辑

### 9.1 自定义工具的适用场景

| 场景 | 说明 |
|-----|------|
| 复杂审批流程 | 需要多级审批、角色校验的业务场景 |
| 定制化任务格式 | 任务内容需要特定格式或业务字段 |
| 额外业务逻辑 | 需要在任务创建/处理前后执行额外操作 |
| 集成外部系统 | 需要与OA、工单系统等外部系统对接 |
| 自定义权限控制 | 需要实现精细的权限校验逻辑 |

### 9.2 自定义工具实现步骤

#### 步骤1：创建自定义工具类

```java
import org.frameworkset.spi.ai.hitl.HitlTaskHelper;
import org.frameworkset.spi.ai.model.ChatObject;
import org.frameworkset.spi.ai.model.annotation.Tool;
import org.frameworkset.spi.ai.model.annotation.ToolParam;
import org.frameworkset.spi.ai.tool.AgentTraceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * 自定义人工介入工具示例
 * 扩展内置工具，添加业务特定逻辑
 */
public class CustomHitlTaskTool {
    private static Logger logger = LoggerFactory.getLogger(CustomHitlTaskTool.class);

    @Tool(name = "customHitlTaskTool", 
          description = "自定义人工介入工具：支持业务特定的人工审批流程，包含多级审批、权限校验等功能")
    public Map<String, Object> customHitlTaskTool(
            @ToolParam(name = "hitlTaskReason", required = true,
                    description = "人工介入原因")
            String hitlTaskReason,
            @ToolParam(name = "approvalLevel", required = false,
                    description = "审批级别：1-普通审批，2-中级审批，3-高级审批",
                    defaultValue = "1")
            Integer approvalLevel,
            @ToolParam(name = "requiredRoles", required = false,
                    description = "需要的审批角色，逗号分隔")
            String requiredRoles) {
        
        // 1. 参数校验
        if (hitlTaskReason == null || hitlTaskReason.trim().isEmpty()) {
            logger.warn("customHitlTaskTool called with null or empty hitlTaskReason");
            return Collections.singletonMap("error", "hitlTaskReason must not be null or empty");
        }

        // 2. 获取对话上下文
        ChatObject chatObject = AgentTraceHolder.getChatObject();
        if (chatObject == null) {
            logger.warn("customHitlTaskTool called without ChatObject context");
            return Collections.singletonMap("error", "No chat context available");
        }

        try {
            // 3. 构建增强的任务原因（添加业务信息）
            StringBuilder enhancedReason = new StringBuilder();
            enhancedReason.append("[审批级别: ").append(approvalLevel).append("] ");
            if (requiredRoles != null && !requiredRoles.isEmpty()) {
                enhancedReason.append("[需要角色: ").append(requiredRoles).append("] ");
            }
            enhancedReason.append(hitlTaskReason);

            // 4. 调用HitlTaskHelper创建任务
            Map<String, Object> hitlTaskResult = 
                    HitlTaskHelper.getHitlTaskHelper()
                            .createHitlCallTask(enhancedReason.toString(), chatObject);

            // 5. 处理结果（可添加业务后处理逻辑）
            if (hitlTaskResult == null) {
                logger.warn("createHitlCallTask returned null");
                return Collections.singletonMap("message", "任务创建成功，等待人工处理");
            }

            // 6. 可以在这里添加结果校验或增强
            hitlTaskResult.put("approvalLevel", approvalLevel);
            hitlTaskResult.put("processedByCustomTool", true);
            
            return hitlTaskResult;

        } catch (Exception e) {
            logger.error("customHitlTaskTool failed", e);
            return Collections.singletonMap("error", "Failed to execute custom HITL task: " + e.getMessage());
        }
    }
}
```

#### 步骤2：注册自定义工具

```java
AIAgent agent = new AIAgent();
agent.setEnableLoopToolCall(true);
agent.setMaxLoopToolCalls(80);

// 注册自定义Hitl工具（替代或补充内置工具）
agent.registBeanTool(new CustomHitlTaskTool());

// 如果需要同时使用内置工具和自定义工具
// agent.registBeanTool(new HitlTaskcallTool());
// agent.registBeanTool(new CustomHitlTaskTool());
```

#### 步骤3：配置系统提示词引导使用

```java
String systemPrompt = "你是一个代码审查助手，具有以下规则：\n" +
        "- 当需要修改核心配置文件或敏感操作时，调用 customHitlTaskTool\n" +
        "- 普通确认操作可调用 hitlTaskTool\n" +
        "- 涉及数据库变更需要高级审批，设置 approvalLevel=3，requiredRoles=DBA,TechLead\n" +
        "- 涉及配置变更需要中级审批，设置 approvalLevel=2，requiredRoles=DevOps\n" +
        "- 审批原因需要清晰说明变更内容和风险";

chatAgentMessage.setSystemPrompt(systemPrompt);
```

### 9.3 高级自定义：扩展HitlTaskHelper

对于更复杂的场景，可以扩展或包装 `HitlTaskHelper`：

```java
/**
 * 业务特定的Hitl任务助手
 * 封装通用业务逻辑
 */
public class BusinessHitlTaskHelper {

    /**
     * 创建数据库变更审批任务
     */
    public static Map<String, Object> createDBA ApprovalTask(String taskReason, 
                                                              String dbName, 
                                                              String sqlContent) {
        ChatObject chatObject = AgentTraceHolder.getChatObject();
        if (chatObject == null) {
            throw new IllegalStateException("No chat context available");
        }

        // 构建标准化的审批原因
        String reason = String.format(
            "[数据库变更审批]\n" +
            "数据库: %s\n" +
            "变更内容:\n%s\n" +
            "风险评估: %s\n" +
            "详细说明: %s",
            dbName, sqlContent, assessRisk(sqlContent), taskReason
        );

        return HitlTaskHelper.getHitlTaskHelper()
                .createHitlCallTask(reason, chatObject);
    }

    /**
     * 风险评估（示例）
     */
    private static String assessRisk(String sqlContent) {
        if (sqlContent.toUpperCase().contains("DROP") || 
            sqlContent.toUpperCase().contains("TRUNCATE")) {
            return "高风险 - 需要DBA确认";
        } else if (sqlContent.toUpperCase().contains("ALTER")) {
            return "中风险 - 需要评审";
        }
        return "低风险";
    }
}
```

### 9.4 与外部审批系统集成

```java
/**
 * 集成外部OA审批系统的Hitl工具
 */
public class OAApprovalHitlTool {
    
    @Tool(name = "oaApprovalTool", description = "集成OA系统进行人工审批")
    public Map<String, Object> oaApprovalTool(
            @ToolParam(name = "title", required = true, description = "审批标题")
            String title,
            @ToolParam(name = "content", required = true, description = "审批内容")
            String content,
            @ToolParam(name = "approver", required = true, description = "审批人账号")
            String approver) {
        
        ChatObject chatObject = AgentTraceHolder.getChatObject();
        
        try {
            // 1. 调用外部OA系统创建审批单
            String oaTicketId = createOATicket(title, content, approver);
            logger.info("OA审批单已创建: {}", oaTicketId);

            // 2. 构建Hitl任务，关联OA单据
            String hitlReason = String.format(
                "OA审批待处理\n审批单号: %s\n标题: %s\n审批人: %s\n请在OA系统中完成审批",
                oaTicketId, title, approver
            );

            // 3. 创建Hitl任务等待结果
            Map<String, Object> result = HitlTaskHelper.getHitlTaskHelper()
                    .createHitlCallTask(hitlReason, chatObject);

            // 4. 关联OA单号到结果
            result.put("oaTicketId", oaTicketId);
            return result;

        } catch (Exception e) {
            logger.error("OA审批集成失败", e);
            return Collections.singletonMap("error", "OA审批创建失败: " + e.getMessage());
        }
    }

    private String createOATicket(String title, String content, String approver) {
        // 调用OA系统API创建审批单
        // 实际实现需要根据OA系统接口调整
        return "OA-" + System.currentTimeMillis();
    }
}
```

### 9.5 省份信息提取和人工补充工具

#### 9.5.1 工具定义

ProvinceAppendHitlTool

```java
public class ProvinceAppendHitlTool extends BaseHitlTaskTool<ProvinceAppendHitlTool> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ProvinceAppendHitlTool.class);
 
    
    public ProvinceAppendHitlTool(Auditor auditor) {
       super(auditor);
    }
    
    public ProvinceAppendHitlTool(){
       
    }
  
    
    /**
     * Human-in-the-Loop，人工介入工具表单
     * @param hitlTaskReason 人工介入原因描述
     * @return 人工介入任务执行结果，包含状态信息，提交的表单数据
     */
    @Tool(name = "provinceAppendTool", description = "当需要处理省份信息，或者需要通过人工介入补充省份信息时，调用本工具方法")
    public Map<String,Object> provinceAppendTool(@ToolParam(name = "hitlTaskReason",required = false, description = "人工介入原因，需包含：1.任务背景与已执行步骤 2.当前卡住的具体原因（信息缺失等）3.建议人类关注的关键点或待决策事项 4.期望人类提供的具体帮助；格式清晰，精简聚焦，便于人类快速理解。") 
                                            String hitlTaskReason,
                                      @ToolParam(name = "provinces",required = false,description = "从问题中提取的省份信息，多个以逗号分隔。" ) String provinces,
                                      @ToolParam(name = "feishuProvinces",required = false,description = "从问题中提取的需要创建飞书文档省份信息，多个以逗号分隔。") String feishuProvinces, 
                                      @ToolParam(name = "allCreateFeishudoc",required = false,description = "标识用户是否要求将补充的省份介绍创建为飞书文档。" ) boolean allCreateFeishudoc
    ){
       ToolCallContext toolCallContext = new ToolCallContext();
       toolCallContext.addParam("hitlTaskReason", hitlTaskReason);
       toolCallContext.addParam("provinces", provinces);
       toolCallContext.addParam("feishuProvinces", feishuProvinces);
       toolCallContext.addParam("allCreateFeishudoc", allCreateFeishudoc);
       // 参数 null/空白校验：防止空任务传递给人工
       if(provinces != null && !provinces.equals("")){
          Map<String,Object> result = new LinkedHashMap<>();
          String[] provincesArray = provinces.split(",");
          List<String> provincesList = Arrays.asList(provincesArray);
          result.put("provinces", provincesList);
          if(feishuProvinces != null && !feishuProvinces.equals("")) {
             String[] feishuProvincesArray = feishuProvinces.split(",");
             result.put("feishuProvinces", Arrays.asList(feishuProvincesArray));
          }
          else if(allCreateFeishudoc){
             result.put("feishuProvinces", provincesList);
          }
          
          if(hitlAssistant != null){
             hitlAssistant.handleHumanSubbmitDatas(result, toolCallContext);
          }
          return result;
       }
       Map<String,Object> result = super.innerHitlTaskTool(hitlTaskReason, toolCallContext);
       
       
       return result;
       
    }
}
```

#### 9.5.2 工具注册和使用

大模型通过工具向客户端传递人工辅助信息provinces（省份清单）和hitlTaskType，借助HitlAssistant接口的getHumanAssistantDatas方法来获取需要向客户端传递的人工辅助信息：

```java
AISequenceAgent chinaProvinceSequenceAgent = new AISequenceAgent(planAgent).setAgentId("chinaProvinceSequenceAgent").setAgentName("中国省份介绍介绍分支智能体");
AINodeAgent chinaProvinceAgent = new AINodeAgent( "根据用户提交的问题提取或者补充省份信息:#[input.query]\n" +
       "如果问题中已经包含省份信息，则直接提取出来，多个省份以逗号分隔，亦可以收集需要创建飞书文档的省份信息，并将提取出来的省份信息和需要创建飞书文档的省份信息一起交给工具provinceAppendTool处理；" +
       "如果没有包含，则调用工具provinceAppendTool，要求用户补充省份信息，同时需要告诉工具用户是否要求将补充的省份介绍创建为飞书文档；如果用户未补充任何信息，需继续提示用户进行补充。\n" +
       
       "输出要求：输出提取的省份信息或者用户补充的省份信息,并告诉用户介绍即将通过后续省份介绍助手介绍相关省份信息。" )
       .setSystemPrompt("你是一个人工信息助手，可以提取用户问题中的省份信息或者补充用户问题中缺失的省份信息。注意：你只需要将提取的信息交给工具provinceAppendTool处理，或者通过provinceAppendTool补充省份信息，无需回答用户问题。")
       .setAgentName("中国省份信息提取和补充智能体")
       .setAgentId("中国省份信息提取和补充智能体");
String[] provinces = new String[]{"北京", "上海", "天津", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西",
       "山东", "河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "西藏", "宁夏", "新疆"};
chinaProvinceAgent.setEnableLoopToolCall(true);//启用智能体多次调用工具机制
chinaProvinceAgent.setMaxLoopToolCalls(80);//限定最大论次
chinaProvinceAgent.registBeanTool(new ProvinceAppendHitlTool().setHitlAssistant(new HitlAssistant() {
    @Override
    public Map<String, Object> getHumanAssistantDatas(ToolCallContext toolCallContext) {
       Map<String, Object> hitlAssistantDatas = new LinkedHashMap<>();
       hitlAssistantDatas.put("provinces", provinces);
       hitlAssistantDatas.put("hitlTaskType", "provinceAppend");
       return hitlAssistantDatas;
    }
    
    @Override
    public void handleHumanSubbmitDatas(Map<String, Object> humanSubbmitDatas,ToolCallContext toolCallContext) {
       List<String> selectProvinces = (List<String>) humanSubbmitDatas.get("provinces");
       List<String> feishuProvinces = (List<String>) humanSubbmitDatas.get("feishuProvinces");
       if(selectProvinces != null && selectProvinces.size() > 0){
          // chatObject null 检查：防御非对话上下文调用
          ChatObject chatObject = AgentTraceHolder.getChatObject();
          JobFlowNodeExecuteContext jobFlowNodeExecuteContext = chatObject.getChatContext().getJobFlowNodeExecuteContext();
          jobFlowNodeExecuteContext.addContainerJobFlowNodeContextData("provinces", selectProvinces);
          if(feishuProvinces != null && feishuProvinces.size() > 0) {
             jobFlowNodeExecuteContext.addContainerJobFlowNodeContextData("feishuProvinces", feishuProvinces);
          }
          else {
             Boolean allCreateFeishudoc = toolCallContext.getBooleanParam("allCreateFeishudoc");
             if(allCreateFeishudoc != null && allCreateFeishudoc) {
                jobFlowNodeExecuteContext.addContainerJobFlowNodeContextData("feishuProvinces", selectProvinces);
             }
          }
       }
       
    }
}));

```

#### 9.5.3 人工任务参数传递和任务结果处理

大模型通过工具向客户端传递人工辅助信息provinces（省份清单）和hitlTaskType，借助HitlAssistant接口的getHumanAssistantDatas方法来获取需要向客户端传递的人工辅助信息：

用户提交人工任务后，工具会将人工操作的结果或者补充的信息返回给大模型，如果设置了HitlAssistant，则会将这些数据交给HitlAssistant接口的handleHumanSubbmitDatas方法来处理，可以将相关数据作为流程上下文数据变量数据；

#### 9.5.4  人工任务结果应用案例

例如，可以根据用户提交的省份数据和需要创建飞书文档的省份，动态构建并行子智能体任务：

```java
    chinaProvinceSequenceAgent.addAgent(chinaProvinceAgent);
          
          //需要提供一个动态并行智能体类型，根据提供的省份信息，动态生成并行智能体
          //构建并行智能体
          AIParrelAgent aiParrelAgent = new AIParrelAgent(planAgent).setAgentId("provincesParrelAgent").setAgentName("中国省份介绍并行智能体");
          //动态创建并行子智能体
          aiParrelAgent.setDynamicNodeBuilder(new DynamicNodeBuilder() {
           
             private boolean needCreateFeishu(String province,List<String> feishuProvinces){
                if(feishuProvinces == null || feishuProvinces.size() == 0)
                   return false;
                for(String feishuProvince: feishuProvinces) {
                   if (province.equals(feishuProvince))
                      return true;
                }
                return false;
             }
             @Override
             public void nodeBuilder( JobFlowNodeExecuteContext jobFlowNodeExecuteContext){
//              ChatObject chatObject = AgentTraceHolder.getChatObject();
                List<String> provinces = (List<String>) jobFlowNodeExecuteContext.getContainerJobFlowNodeContextData("provinces");
                List<String> feishuProvinces = (List<String>) jobFlowNodeExecuteContext.getContainerJobFlowNodeContextData("feishuProvinces");
                for(String province: provinces){
                   StringBuilder prompt = new StringBuilder();
                   prompt.append("请用500字介绍").append(province).append("。");
                   if(!needCreateFeishu( province, feishuProvinces)) {
                      aiParrelAgent.addAgent(new UserNodeAgent(prompt.toString()).setAgentId("介绍" + province).setAgentName("介绍" + province));
                   }
                   else{
                      AISequenceAgent hunanSequenceAgent = new AISequenceAgent(planAgent).setAgentId(province+"介绍及报告任务").setAgentName(province+"介绍及报告任务");
                      
                      hunanSequenceAgent.addAgent(new UserNodeAgent(prompt.toString()).setAgentId("介绍" + province).setAgentName("用500字介绍" + province));
                      
                      AINodeAgent feishuAgent = new AINodeAgent( "根据"+province+"介绍，创建一份详细的飞书报告。请用清晰的中文输出。约束：创建飞书文档前需要调用工具hitlTaskTool，经人工确认后才能创建，否则不能创建。" )
                            .setAgentName("创建"+province+"介绍报告")
                            .setAgentId("create"+province+"FeishuDoc")
                            .registBeanTool(new HitlTaskcallTool().setTimeoutAction(HitlTaskcallTool.TIMEOUT_ACTION_CONTINUE).setHitlTaskTimeout(60000L))
                            .setToolsRegist(feishuMcpToolsRegist).setToolSearcher(new KeywordToolSearcher("人工介入工具","创建飞书云文档"));
                      feishuAgent.setEnableLoopToolCall(true);//启用智能体多次调用工具机制
                      feishuAgent.setMaxLoopToolCalls(80);//限定最大论次
                      hunanSequenceAgent.addAgent(feishuAgent);
                      aiParrelAgent.addAgent(hunanSequenceAgent);
                   }
                }
                
             }
          });
  
          chinaProvinceSequenceAgent.addAgent(aiParrelAgent, new TriggerScriptAPI() {
             @Override
             public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                List<String> provinces = (List<String>)nodeTriggerContext.getContainerContextData("provinces");
                if(provinces != null && provinces.size() > 0)
                   return true;
                return false;
             }
          });
          
          planAgent.addRouteChoiceAgent(chinaProvinceSequenceAgent);
```

通过接口DynamicNodeBuilder来构建并行子智能体的动态并行任务分支。

### 9.6 自定义工具最佳实践

| 实践要点 | 说明 |
|---------|------|
| 参数校验 | 必须对输入参数进行校验，避免空值或非法输入 |
| 上下文获取 | 从 `AgentTraceHolder` 获取 `ChatObject` 时需要做空值检查 |
| 异常处理 | 捕获所有异常并返回友好的错误信息，避免智能体中断 |
| 日志记录 | 添加详细的日志便于问题排查和审计 |
| 返回结果 | 返回 `Map<String, Object>` 格式，包含必要的业务字段 |
| 工具描述 | `@Tool` 注解的 description 要清晰描述工具用途和适用场景 |

## 10. 故障排查

### 10.1 常见问题

| 问题 | 可能原因 | 解决方法 |
|-----|---------|---------|
| 任务创建失败 | 数据库连接未配置 | 检查 `AgentSessionService` 数据源配置 |
| 集群模式下任务不触发 | Redis未配置或通道不匹配 | 确认Redis配置和通道名称一致 |
| 任务超时 | 人工未及时处理 | 调整 `hitlTaskTimeout` 配置 |
| 跨节点消息不传递 | Redis发布/订阅通道不一致 | 确保所有节点使用相同通道名 |
| 自定义工具不被调用 | 工具描述不清晰或参数不匹配 | 检查 `@Tool` 注解配置和系统提示词 |
| 上下文获取失败 | 调用时机不在智能体执行上下文 | 确保工具在智能体调用期间被触发 |

### 10.2 日志调试

开启Debug日志查看详细流程：

```xml
<!-- log4j2.xml 配置 -->
<Logger name="org.frameworkset.spi.ai.hitl" level="DEBUG"/>
<Logger name="org.frameworkset.spi.ai.store" level="DEBUG"/>
<Logger name="org.frameworkset.spi.ai.tool" level="DEBUG"/>
```

## 11. 技能、文件操作工具与Hitl的结合应用

在实际业务场景中，Hitl 经常与技能（Skill）和文件操作工具（FileFunctionTool）配合使用，形成完整的"审查-确认-执行"工作流。

### 11.1 典型应用场景

| 场景 | 说明 |
|-----|------|
| 代码审查与修复 | 读取代码→审查问题→人工确认→修复代码 |
| 文档审核与发布 | 读取文档→内容审查→人工审批→发布上线 |
| 配置变更管理 | 读取配置→风险评估→多级审批→应用变更 |
| 数据清理操作 | 识别数据→确认范围→人工确认→执行清理 |

### 11.2 完整工作流示例：代码审查与修复

以下是从测试用例 `ChecklistCodeViewAgentHitlTest` 提取的完整工作流：

```java
// 1. 初始化数据源和Hitl服务
AgentSessionService agentSessionService = new AgentSessionServiceImpl();
agentSessionService.setDatasource("visualops");
agentSessionService.setHitlDatasource("visualops");

HitlTaskHelper.getHitlTaskHelper()
        .setAgentSessionService(agentSessionService)
        .init();

// 2. 创建智能体并注册多种工具
AIAgent agent = new AIAgent();
agent.setEnableLoopToolCall(true);    // 启用循环工具调用
agent.setMaxLoopToolCalls(80);        // 设置最大调用次数

// 注册技能工具（代码审查技能）
agent.registTools(new SkillsToolRegist()
        .addClasspathSkills("skills"))  // 加载classpath下的skills目录

// 注册人工介入工具（Hitl）
.registBeanTool(new HitlTaskcallTool());

// 注册文件操作工具（限制操作目录）
.registBeanTool(new FileFunctionTool("C:\\data\\ai\\code")
        .addBaseDirectory("C:\\workspace\\bbossgroups\\bboss-ai\\out\\test\\resources\\skills\\code-review-skill\\"));

// 3. 配置任务参数
ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
chatAgentMessage.setMaas("deepseek")
        .setModel("deepseek-v4-pro")
        .setPrompt("请评审Java文件中的代码并修复问题,java文件路径：C:\\data\\ai\\code\\AIAgent.java")
        .setSystemPrompt("你是一个 Java 代码审查助手...")
        .setStoreContext(new StoreContext()
                .setUserId("user123")
                .setRequestId("request123")
                .setStoreType(StoreContext.STORE_TYPE_DB)
                .setDataSource("visualops")
                .setHitlDatasource("visualops"));

// 4. 执行流式调用并处理Hitl事件
Flux<ServerEvent> flux = agent.streamChat(chatAgentMessage);

flux.doOnNext(chunk -> {
    if (chunk.isHitl()) {
        // 收到人工介入消息，提示用户处理
        String hitlTaskId = chunk.getHitlTaskId();
        if (hitlTaskId != null) {
            // 模拟人工确认：同意修复
            Map<String, Object> hitlTaskData = new LinkedHashMap<>();
            hitlTaskData.put("confirm", "确认修改文件");
            hitlTaskData.put("otherData", "用户补充意见：各个问题都符合要求，可以整改");
            HitlTaskHelper.handleHitlCallTask(hitlTaskData, null, hitlTaskId);
        }
    } else if (chunk.getData() != null) {
        // 普通响应数据
        System.out.print(chunk.getData());
    }
}).subscribe();
```

### 11.3 技能中定义Hitl触发逻辑

技能文件（如 `code-review-skill/SKILL.md`）中可以定义何时触发Hitl。通过自然语言描述触发条件，LLM 会自动检测已注册的工具并决定是否调用。

**标准声明格式**：

```markdown
---
name: code-review-skill
description: 按固定流程审查 Java 代码，优先发现 bug、安全风险...经人工确认后才能修复问题。
---

# Java Code Review Skill

## step 5: 问题修复
- 请生成修复后的代码
- 调用文件处理工具保存修改后的代码
- 如果存在人工介入工具hitlTaskTool，则调用人工介入任务工具hitlTaskTool，通知人工介入确认后，才能保存修复后的代码到原始文件，否则忽略保存代码到原始文件。
- 修复问题时，要指出问题的位置和原因。
- 修复后，要检查是否解决了问题。
```

**hitlTaskReason 参数模板**：

```markdown
## hitlTaskReason 内容规范
**任务背景**：[已完成的工作内容，如：代码审查任务已完成，发现并生成了修复方案]
**已执行步骤**：
1. [步骤1]
2. [步骤2]
**待确认操作**：[具体要执行的敏感操作，如：将修复后的代码保存到原始文件]
**建议关注**：[需要人工注意的关键点，如：修复涉及业务逻辑变更，请确认修复方案正确]
**期望决策**：[明确的决策请求，如：是否确认保存修复代码？（是/否）]
```

**工具检测与调用流程**：

| 步骤 | 组件 | 说明 |
|------|------|------|
| 1 | LLM 解析 | 读取技能内容，识别到 `hitlTaskTool` 工具名 |
| 2 | 工具列表检查 | 查询已注册的 `FunctionToolDefine` 列表 |
| 3 | 名称匹配 | 通过 `KeywordToolSearcher` 或名称精确匹配查找工具 |
| 4 | 条件判断 | 根据"如果存在...则调用"的描述执行条件判断 |
| 5 | 生成调用请求 | 如果找到匹配工具，生成 `FunctionTool` 调用对象 |
| 6 | 执行工具调用 | `BeanToolFunctionCall.call()` 执行实际的工具方法 |
| 7 | 结果处理 | 根据人工确认结果继续或跳过后续操作 |

**注意事项**：
1. **工具名称一致性**：技能中提到的工具名必须与 `@Tool(name="xxx")` 注解中声明的名称完全一致
2. **条件调用逻辑**："如果存在...则调用" 是自然语言描述，由 LLM 理解并执行条件判断
3. **参数完整性**：调用 `hitlTaskTool` 时必须提供 `hitlTaskReason` 参数，否则工具会返回错误
4. **异常处理**：`HitlTaskcallTool` 内部已实现多层异常保护，调用失败会返回友好提示，不会中断智能体执行

### 11.4 工具协作流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        工具协作工作流                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  用户请求                                                                    │
│     │                                                                        │
│     ▼                                                                        │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                   │
│  │ Skills工具    │───▶│ FileFunction │───▶│ HitlTaskcall │                   │
│  │ (技能执行)     │    │ Tool         │    │ Tool         │                   │
│  │               │    │ (文件读写)    │    │ (人工确认)    │                   │
│  └──────────────┘    └──────────────┘    └──────────────┘                   │
│         │                   │                   │                           │
│         │   读取代码         │   读取/Save代码    │   等待人工确认             │
│         ▼                   ▼                   ▼                           │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                   │
│  │ 代码审查逻辑  │    │ 文件系统      │    │  人工审批系统  │                  │
│  │               │    │               │    │               │                   │
│  └──────────────┘    └──────────────┘    └──────────────┘                   │
│         │                                    │                               │
│         │                                    ▼                               │
│         │                              审批结果                               │
│         │                                    │                               │
│         └──────────────────┬─────────────────┘                               │
│                            ▼                                                 │
│                    继续/拒绝执行                                              │
│                            │                                                 │
│                            ▼                                                 │
│                    执行结果反馈                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 11.5 工具注册顺序与优先级

| 注册顺序 | 工具类型 | 说明 |
|---------|---------|------|
| 1 | `SkillsToolRegist` | 技能工具，定义工作流程和决策逻辑 |
| 2 | `HitlTaskcallTool` | 人工介入工具，在关键节点暂停等待确认 |
| 3 | `FileFunctionTool` | 文件操作工具，执行实际的文件读写操作 |

**注意**：工具注册顺序不影响调用优先级，优先级由LLM根据工具描述和当前上下文决定。

### 11.6 配置最佳实践

#### 11.6.1 文件操作目录限制

```java
FileFunctionTool fileTool = new FileFunctionTool("C:\\data\\ai\\code")
        .addBaseDirectory("C:\\workspace\\skills\\")
        .addBaseDirectory("D:\\temp\\output\\");
```

**安全建议**：
- 始终配置 `baseDirectory` 限制文件操作范围
- 避免配置根目录或系统敏感目录
- 生产环境建议通过配置文件管理允许的目录

#### 11.6.2 技能加载方式

```java
// 方式1：从classpath加载
new SkillsToolRegist().addClasspathSkills("skills");

// 方式2：从文件系统加载
new SkillsToolRegist().addDirectorySkills("/opt/app/skills/");
```

#### 11.6.3 智能体循环调用配置

```java
AIAgent agent = new AIAgent();
agent.setEnableLoopToolCall(true);   // 必须启用
agent.setMaxLoopToolCalls(50);       // 防止无限循环
```

## 12. 注意事项

1. **数据库表管理**：Hitl任务表需要定期归档，避免数据量过大
2. **Redis高可用**：集群模式下建议配置Redis哨兵或集群保证高可用
3. **超时策略**：根据业务场景合理设置超时时间，避免无限期等待
4. **并发安全**：HitlTaskHelper采用ConcurrentHashMap保证线程安全
5. **资源释放**：应用关闭时务必调用 `HitlTaskHelper.destory()` 释放资源
6. **文件操作安全**：通过 `baseDirectory` 严格限制文件操作范围，防止路径穿越攻击
7. **技能版本管理**：技能文件建议纳入版本控制，便于追溯和回滚

## 附录：核心类关系图

```
┌──────────────────────────────────────────────────────────────────┐
│                        Hitl 核心类关系                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────┐     ┌─────────────────┐                     │
│  │   AIAgent       │     │  ChatAgentMessage│                    │
│  └────────┬────────┘     └────────┬────────┘                     │
│           │                       │                              │
│           ▼                       ▼                              │
│  ┌─────────────────┐     ┌─────────────────┐                     │
│  │HitlTaskcallTool │────▶│ HitlTaskHelper  │◀── 单例             │
│  └─────────────────┘     └────────┬────────┘                     │
│                                   │                              │
│           ┌───────────────────────┼───────────────────────┐      │
│           ▼                       ▼                       ▼      │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────┐│
│  │  HitlCallTask   │     │  HitlCallObject │     │AgentSession ││
│  │  (任务实体)      │     │  (执行上下文)    │     │  Service    ││
│  └─────────────────┘     └─────────────────┘     └─────────────┘│
│                                   │                              │
│           ┌───────────────────────┼───────────────────────┐      │
│           ▼                       ▼                             │
│  ┌─────────────────┐     ┌─────────────────┐                     │
│  │RedisHitlTask    │     │RedisHitlTask    │                    │
│  │CallListener     │     │CallNotifier     │                    │
│  │  (订阅监听)      │     │  (发布通知)      │                    │
│  └─────────────────┘     └─────────────────┘                     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

*文档版本：1.0*  
*最后更新：2026-07-19*  
*适用版本：bboss-ai 6.5.6+*
