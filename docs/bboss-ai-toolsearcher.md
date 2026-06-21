# 工具搜索功能使用指南

## 概述

工具搜索功能允许智能体根据用户输入动态筛选相关工具，而不是将所有注册的工具都传递给大模型。这有助于：

- 减少大模型的上下文负担
- 提高工具调用的准确性
- 优化API调用成本
- 增强系统的可扩展性

## 核心接口

### ToolSearcher 接口

```java
public interface ToolSearcher {
    /**
     * @param allTools 注册的全部工具
     * @param query    用户输入/提示词
     * @return 命中的工具列表，返回 null 或空列表表示不启用过滤（全部返回）
     */
    List<FunctionToolDefine> search(List<FunctionToolDefine> allTools, String query);
}
```

## 内置实现

### KeywordToolSearcher - 关键词匹配检索器

基于关键词匹配的简单实现，当工具名称或描述中包含指定关键词时即命中。

```java
// 创建关键词检索器，支持多个关键词
ToolSearcher keywordSearcher = new KeywordToolSearcher("酒店", "航班", "机票");

// 使用示例
List<FunctionToolDefine> matchedTools = keywordSearcher.search(allTools, userQuery);
```

**匹配规则：**
- 工具名称或描述中包含任意一个关键词即命中
- 匹配过程忽略大小写
- 如果没有匹配到任何工具，则返回全部工具

## 在智能体中使用工具搜索

### 1. 基础使用方式

通过 `setToolSearcher()` 方法为智能体设置工具检索器：

```java
AINodeAgent agent = new AINodeAgent("请根据用户需求查询相关信息")
    .setAgentId("searchAgent")
    .setAgentName("查询智能体")
    .setToolSearcher(new KeywordToolSearcher("酒店"))  // 设置工具检索器
    .setToolsRegist(toolsRegist);
```

### 2. 完整工作流示例

参考 `BookingToolSearcherStreamTest` 中的酒店预订工作流：

```java
// 定义工具注册
ToolsRegist toolsRegist = new BeanToolsRegist(new PreOrderTool());

// 酒店查询智能体 - 只搜索与"酒店"相关的工具
planAgent.addRouteChoiceAgent(new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，只查询并推荐合适的酒店...")
    .setAgentId("hotelAgent")
    .setAgentName("酒店查询智能体")
    .setToolSearcher(new KeywordToolSearcher("酒店"))  // 关键词检索
    .setToolsRegist(toolsRegist));

// 机票查询智能体 - 只搜索与"航班"相关的工具  
planAgent.addRouteChoiceAgent(new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，只查询并推荐合适的航班和机票...")
    .setAgentId("flightAgent")
    .setAgentName("机票查询智能体")
    .setToolSearcher(new KeywordToolSearcher("航班"))  // 关键词检索
    .setToolsRegist(toolsRegist));
```

### 3. 并行查询场景

在并行查询智能体中同样可以使用工具搜索：

```java
AIParrelAgent bothAgent = new AIParrelAgent(planAgent)
    .setAgentId("bothAgent")
    .setAgentName("并行查询智能体");

// 并行酒店查询 - 限定搜索"酒店"相关工具
bothAgent.addAgent(new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，查询并推荐合适的酒店...")
    .setAgentId("parrelHotelAgent")
    .setAgentName("并行酒店查询")
    .setToolSearcher(new KeywordToolSearcher("酒店"))
    .setToolsRegist(toolsRegist));

// 并行机票查询 - 限定搜索"航班"相关工具
bothAgent.addAgent(new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，查询并推荐合适的航班...")
    .setAgentId("parrelFlightAgent")
    .setAgentName("并行机票查询")
    .setToolSearcher(new KeywordToolSearcher("航班"))
    .setToolsRegist(toolsRegist));
```

### 4.mcp服务中使用工具检索

通过KeywordToolSearcher指定只向大模型传递创建飞书云文档的工具方法，忽略其他方法，可大幅降低context上下文大小：

```java
ToolsRegist mcpToolsRegist = new FeishuMcpRegist("feishumcp");
planAgent.addAgent(new AINodeAgent(
        "请根据用户的问题:#[input.query]，以及前面的汇总建议，创建一份详细的飞书报告。" +
                "报告需要包含：1)推荐的酒店及理由 2)推荐的航班及理由 3)总预算估算 4)最终操作建议。" +
                "请用清晰的中文输出。")
        .setAgentId("feishudocAgent").setAgentName("飞书文档智能体").setToolsRegist(mcpToolsRegist).setToolSearcher(new KeywordToolSearcher("创建飞书云文档")) );
```

## 高级用法

### 1. 自定义工具检索器

您可以实现自己的 `ToolSearcher` 来支持更复杂的检索逻辑：

```java
public class SemanticToolSearcher implements ToolSearcher {
    
    private EmbeddingService embeddingService; // 假设您有嵌入服务
    
    @Override
    public List<FunctionToolDefine> search(List<FunctionToolDefine> allTools, String query) {
        if (allTools == null || allTools.isEmpty() || query == null || query.trim().isEmpty()) {
            return allTools;
        }
        
        // 实现语义匹配逻辑
        // 1. 计算查询的向量表示
        // 2. 计算每个工具描述的向量表示  
        // 3. 计算相似度并返回高分工具
        // 4. 设置阈值过滤
        
        return matchedTools;
    }
}
```

### 2. 组合检索策略

可以创建组合检索器来支持多种检索策略：

```java
public class CompositeToolSearcher implements ToolSearcher {
    private List<ToolSearcher> searchers;
    
    public CompositeToolSearcher(ToolSearcher... searchers) {
        this.searchers = Arrays.asList(searchers);
    }
    
    @Override
    public List<FunctionToolDefine> search(List<FunctionToolDefine> allTools, String query) {
        Set<FunctionToolDefine> result = new HashSet<>();
        for (ToolSearcher searcher : searchers) {
            List<FunctionToolDefine> matches = searcher.search(allTools, query);
            if (matches != null) {
                result.addAll(matches);
            }
        }
        return new ArrayList<>(result);
    }
}
```

### 3. 带权重的检索器

为不同检索策略分配权重：

```java
public class WeightedToolSearcher implements ToolSearcher {
    private Map<ToolSearcher, Double> weightedSearchers;
    
    // 实现带权重的匹配逻辑
}
```

## 内部工作机制

在 `AIAgent` 中，工具搜索通过以下方法实现：

```java
public List<FunctionToolDefine> getToolsByToolSearch(AgentMessage agentMessage) {
    String query = null;
    List<FunctionToolDefine> allTools = getTools();
    if (toolSearcher != null && allTools != null && !allTools.isEmpty()) {
        query = this.evalPrompt(agentMessage);  // 获取评估后的提示词作为查询
        if(query != null && !query.trim().isEmpty()) {
            return toolSearcher.search(allTools, query);  // 执行搜索
        }
    }
    return allTools;  // 如果没有设置检索器或查询为空，返回全部工具
}
```

## 最佳实践

### 1. 选择合适的检索策略

- **关键词检索**：适用于工具功能明确、关键词清晰的场景
- **语义检索**：适用于需要理解用户意图的复杂场景
- **混合检索**：结合多种策略提高召回率

### 2. 工具命名和描述规范

为了提高检索准确性，建议：
- 工具名称应简洁明了，包含核心功能关键词
- 工具描述应详细说明功能、使用场景和参数要求
- 保持命名和描述的一致性

### 3. 性能考虑

- 对于大量工具，考虑缓存检索结果
- 实现异步检索避免阻塞主流程
- 设置合理的超时机制

### 4. 错误处理

- 当检索失败时，应有降级策略（如返回全部工具）
- 记录检索日志以便调试和优化
- 监控检索效果指标

## 注意事项

1. `KeywordToolSearcher` 目前是简单的字符串包含匹配，可能需要根据业务需求优化
2. 工具检索是在每次智能体调用时动态执行的，确保检索逻辑的性能
3. 如果检索器返回空列表，系统会fallback到返回所有工具
4. 检索器可以使用经过变量替换后的提示词作为查询条件

## 扩展方向

未来可以考虑实现：
- 基于向量相似度的语义检索
- 基于机器学习的学习型检索器
- 支持工具分类和层级的检索
- 支持用户偏好的个性化检索
- 支持多语言的检索器