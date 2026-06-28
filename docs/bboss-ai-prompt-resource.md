# bboss AI 外部资源提示词加载

在构建提示词工程（Prompt Engineering）时，通常需要将提示词内容与业务代码解耦，以便于独立维护、动态更新和复用。bboss AI 框架提供了灵活多样的提示词加载机制，支持从**变量**和**外部资源**两种方式加载提示词内容。

---

## 一、从变量加载提示词

最直接的方式是将提示词内容直接定义在代码变量中。这种方式适用于提示词内容较短、固定且不经常变化的场景。

### 使用方式

将提示词文本直接赋值给消息对象的 `prompt` 或 `systemPrompt` 属性：

```java
ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
chatAgentMessage.setModel("deepseek-chat");

// 直接通过变量设置提示词
String message = "当前OS为windows，帮忙查找占用端口808的进程，如果存在对应进程，则关闭进程，如果不存在相关进程，则无需处理。\n"
        + "# 工具调用要求：只执行一次工具，执行后只分析结果，不要再返回工具调用信息和工具参数\n"
        + "# 结果输出要求：直接返回脚本及脚本执行结果";
chatAgentMessage.setPrompt(message);

AIAgent aiAgent = new AIAgent();
ServerEvent response = aiAgent.chat("deepseek", chatAgentMessage);
```

### 提示词变量插值

在提示词字符串中，可以通过 `#[变量名]` 的语法嵌入动态变量。框架在执行时会将占位符替换为对应变量的实际值，从而实现提示词模板与动态数据的解耦。

#### 示例

```java
// 定义行程查询智能体，在提示词中嵌入用户输入变量 input.query
AINodeAgent hotelAgent = new AINodeAgent(
        "请根据用户的行程需求:#[input.query]，只查询并推荐合适的酒店，千万不要查询航班。" +
                "需要考虑：地理位置、价格区间、用户评分、配套设施等因素。" +
                "给出至少3个推荐选项，并说明理由。如未匹配到工具，请返回\"未找到匹配的酒店查询工具\"")
        .setAgentId("hotelAgent")
        .setAgentName("酒店查询智能体");
```

#### 变量作用域 `scope`

在变量占位符中，可以通过 `scope` 属性指定变量的取值范围，以明确变量从哪个上下文中解析。常用格式如下：

```
#[变量名,scope=作用域]
```

`scope` 支持以下三种取值：

| 取值 | 说明 | 适用场景 |
| :--- | :--- | :--- |
| `flow` | **流程级别**：从整个工作流（流程）上下文中获取变量值。变量在整个流程执行期间全局共享，各节点均可访问。默认级别 | 需要在多个节点间传递全局参数，如用户会话信息、全局配置等。 |
| `container` | **容器级别**：从容器上下文中获取变量值。变量在容器范围内共享，适用于容器内多个组件的协作场景。 | 在同一容器内共享中间计算结果、容器级配置等。 |
| `node` | **节点级别**：从**当前执行节点**的上下文中获取变量值。变量仅作用于当前节点内部。 | 引用当前节点的局部变量、节点输入参数或上游节点传入的节点级数据。 |

##### 示例

以下示例展示了在评估智能体中，使用 `scope=node` 引用当前节点内的 `input.query` 和 `answer` 变量：

```java
// 评估智能体：在提示词中引用当前节点内的 input.query 和 answer 变量
planAgent.addAgent(new AIJudgeAgent(
        "请评估问题答案是否处理了用户提出的问题,处理则返回输出：是，如果没有查到进程则返回：是，否则仅返回输出：否\n"
                + "#用户问题:\n#[input.query,scope=node]\n"
                + "# 问题答案：\n#[answer,scope=node]")
        .setAgentId("judgeAgent")
        .setAgentName("评估智能体"));
```

#### 使用要点

- 变量占位符格式为 `#[变量名]`，如 `#[input.query]`、`#[user.name]` 等；可通过 `scope` 属性限定变量来源，如 `#[input.query,scope=node]`、`#[sessionId,scope=flow]`。
- 变量插值可与静态文本混合使用，构建动态提示词模板。
- 变量值通常由运行时上下文、用户输入或上游节点输出提供。
- 变量插值功能同样适用于外部资源加载的提示词内容（在资源文件内也可使用 `#[变量名]` 语法）。

### 适用场景

- 提示词内容简短、固定
- 快速验证和调试
- 不需要频繁修改的静态提示词
- 需要在提示词中嵌入动态变量值的场景

---

## 二、从外部资源加载提示词

对于复杂的提示词模板、需要频繁维护或多人协作的场景，建议将提示词内容抽取到外部资源中。bboss AI 框架支持通过统一的 `#[...]` 语法从多种外部资源加载提示词内容。

> **说明**：从外部资源加载的提示词内容，通常作为**整体提示词工程的一部分**来使用。实际应用中，可将外部加载的提示词模板与变量插值（`#[变量名]`）、静态文本组合搭配，共同构建完整、动态且易于维护的提示词工程。

### 2.1 语法说明

外部资源加载采用以下统一格式：

```
#[资源路径,type=资源类型,charset=字符编码]
```

在代码中，只需将上述格式的字符串赋值给 `prompt` 或 `systemPrompt` 属性，框架会在运行时自动解析并加载对应的资源内容。

### 2.2 加载 classpath 下的提示词文件（type=resource）

当提示词文件打包在项目的 `resources` 目录下时，可以使用 `type=resource` 进行加载。框架会从当前线程的类路径（classpath）中查找并读取文件。

#### 示例

项目结构：

```
src/main/resources/
└── prompt.txt
```

代码中使用：

```java
ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
chatAgentMessage.setModel("deepseek-chat");

// 指定字符编码加载 classpath 资源
String message = "#[prompt.txt,type=resource,charset=UTF-8]";
chatAgentMessage.setPrompt(message);

AIAgent aiAgent = new AIAgent();
ServerEvent response = aiAgent.chat("deepseek", chatAgentMessage);
```

若不指定字符编码，可简写为：

```java
String message = "#[prompt.txt,type=resource]";
```

> 实际应用中，`type=resource` 加载的外部资源通常作为整体提示词工程的一部分。例如，将包含变量插值的核心模板放在 `hotel-prompt.txt` 中，再与静态文本组合：

```java
// 静态角色设定 + 外部资源模板（内含 #[input.query] 变量） + 静态输出要求
String message = "你是一个专业的旅行顾问。\n"
        + "#[hotel-prompt.txt,type=resource,charset=UTF-8]\n"
        + "给出至少3个推荐选项，并说明理由。如未匹配到工具，请返回\"未找到匹配的酒店查询工具\"";
chatAgentMessage.setPrompt(message);
```

### 2.3 加载外部文件系统中的提示词（type=file）

当提示词文件存放在项目外部的文件系统路径（如本地磁盘、共享目录）时，可以使用 `type=file` 进行加载。此时需要提供文件的绝对路径或相对路径。

#### 示例

```java
ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
chatAgentMessage.setModel("deepseek-chat");

// 加载外部文件系统中的提示词文件
String message = "#[C:\\workspace\\bbossgroups\\bboss-ai\\bboss-ai-flow\\src\\test\\resources\\prompt.txt,type=file,charset=UTF-8]";
chatAgentMessage.setPrompt(message);

AIAgent aiAgent = new AIAgent();
ServerEvent response = aiAgent.chat("deepseek", chatAgentMessage);
```

> 外部文件同样可作为提示词工程的一部分，与静态文本组合使用：

```java
// 静态文本 + 外部文件资源 + 静态输出要求
String message = "请基于以下规则进行评估。\n"
        + "#[D:/prompts/eval-rule.txt,type=file,charset=UTF-8]\n"
        + "最终以 JSON 格式输出评估结果。";
chatAgentMessage.setPrompt(message);
```

### 2.4 加载 URL 提供的提示词（type=url）

当提示词内容托管在远程 HTTP 服务上（如配置中心、文档服务器、OSS 等），可以使用 `type=url` 进行加载。框架会发起 HTTP 请求获取远程提示词内容。

#### 示例

```java
ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
chatAgentMessage.setModel("deepseek-chat");

// 从远程 URL 加载提示词内容
String message = "#[http://localhost:85/prompt.txt,type=url,charset=UTF-8]";
chatAgentMessage.setPrompt(message);

AIAgent aiAgent = new AIAgent();
ServerEvent response = aiAgent.chat("deepseek", chatAgentMessage);
```

> 从 URL 加载的远程提示词也可嵌入到完整的提示词工程中，与本地静态文本协同：

```java
// 静态上下文 + 远程提示词模板 + 静态约束
String message = "你是一名资深代码审查专家。\n"
        + "#[http://config-server/prompts/code-review.txt,type=url,charset=UTF-8]\n"
        + "请用中文输出审查意见，并给出改进建议。";
chatAgentMessage.setPrompt(message);
```

### 2.5 属性说明

| 属性 | 是否必填 | 说明 |
| :--- | :--- | :--- |
| `资源路径` | 是 | 根据 `type` 类型不同，分别表示 classpath 下的文件路径、本地文件绝对/相对路径、或 HTTP/HTTPS URL 地址。 |
| `type` | 是 | 资源加载类型。可选值：<br>- `resource`：从 classpath 加载<br>- `file`：从外部文件系统加载<br>- `url`：从远程 URL 加载 |
| `charset` | 否 | 指定读取资源时的字符编码，例如 `UTF-8`、`GBK` 等。若不指定，则使用系统默认编码。 |

---

## 三、缓存机制

从外部资源（classpath、文件系统、URL）加载的提示词内容**具备缓存功能**：

- **首次加载**：框架在第一次访问外部资源时，会读取文件或请求远程内容，并将提示词文本缓存到内存中。
- **后续访问**：当再次使用相同资源路径和参数的提示词时，框架直接从缓存中获取内容，避免重复的文件 I/O 或网络请求，从而提升运行效率。

> **提示**：如果在应用运行期间修改了外部提示词文件的内容，需要重启应用才能使新内容生效。

---

## 四、综合示例

以下示例展示了在同一个智能体调用中，分别使用变量和外部资源加载不同的提示词：

```java
public class PromptResourceExample {
    public static void main(String[] args) {
        HttpRequestProxy.startHttpPools("application-stream.properties");

        ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
        chatAgentMessage.setModel("deepseek-chat");

        // 方式1：变量方式设置系统提示词
        chatAgentMessage.setSystemPrompt("你是一个资深的系统运维专家，擅长编写和诊断 Shell/Bat 脚本。");

        // 方式2：从 classpath 加载提示词
        chatAgentMessage.setPrompt("#[prompt.txt,type=resource,charset=UTF-8]");

        // 方式3：从外部文件加载提示词（根据实际需要选择其中一种方式即可）
        // chatAgentMessage.setPrompt("#[D:/prompts/sys-check.txt,type=file,charset=UTF-8]");

        // 方式4：从远程 URL 加载提示词
        // chatAgentMessage.setPrompt("#[http://config-server/prompts/sys-check.txt,type=url,charset=UTF-8]");

        AIAgent aiAgent = new AIAgent();
        ServerEvent response = aiAgent.chat("deepseek", chatAgentMessage);
        System.out.println(response.getData());
    }
}
```

---

## 五、最佳实践

1. **静态短提示词**：优先使用变量方式，简单直接。
2. **复杂提示词模板**：建议抽取到 `src/main/resources` 下，使用 `type=resource` 加载，便于版本管理和多环境复用。
3. **动态更新需求**：若提示词需要频繁调整且不便于重新打包，可存放在外部文件系统（`type=file`）或托管到远程配置服务（`type=url`）。
4. **字符编码**：建议始终显式指定 `charset=UTF-8`，以避免因系统默认编码不一致导致的中文乱码问题。
