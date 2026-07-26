# 工具方法执行审核

## 1.1 功能说明

工具方法执行审核机制允许在工具方法**实际执行前**，对工具调用请求进行拦截和审核。通过实现 `Auditor` 接口，可实现敏感信息拦截、操作权限控制、审计日志记录等能力，审核失败时通过 `nextAction` 引导大模型调整后续行为。

所有继承 `BaseAuditorTool` 的内置工具（`FileFunctionTool`、`CLIShellFunctionTool`、`CodeExecuteFunctionTool`、`GetOSFunctionTool`、`HitlTaskcallTool`）均支持审核。

## 1.2 核心 API

**Auditor 接口**：`AuditResult audit(AuditContext auditContext)` — 实现审核逻辑，返回 `AuditResult`。

**AuditContext** 审核上下文，提供以下信息：

| 字段         | 类型         | 说明                                                    |
| ------------ | ------------ | ------------------------------------------------------- |
| `toolName`   | `String`     | 当前被调用的工具方法名（如 `readFile`、`writeFile`）    |
| `content`    | `String`     | 工具调用内容（单参数工具使用，多参数工具为 `null`）     |
| `toolInfo`   | `Map`        | 工具调用完整参数（多参数工具使用，单参数工具为 `null`） |
| `chatObject` | `ChatObject` | 当前智能体会话对象                                      |

**AuditResult** 审核结果：

| 字段         | 类型      | 说明                                                      |
| ------------ | --------- | --------------------------------------------------------- |
| `success`    | `boolean` | `true` 通过，工具正常执行；`false` 拒绝，工具直接返回失败 |
| `message`    | `String`  | 审核结果描述（拒绝原因）                                  |
| `nextAction` | `String`  | 阻断后的下一步操作指引，返回给大模型引导后续行为          |

## 1.3 使用案例
为工具设置审核器，在工具方法执行前自动拦截。以下案例对文件操作进行审核，涉及敏感信息时拒绝操作：

```java
AIAgent agent = new AIAgent();
agent.setEnableLoopToolCall(true);
agent.setMaxLoopToolCalls(80);

// 注册文件操作工具，并设置审核器
agent.registBeanTool(new FileFunctionTool("C:\\data\\ai\\code")
        .setAuditor(new Auditor() {
	@Override
	public AuditResult audit(AuditContext auditContext) {
		AuditResult auditResult = new AuditResult();
		// 通过 auditContext.getToolName() 获取工具方法名
		// 通过 auditContext.getContent() / auditContext.getToolInfo() 获取调用参数
		auditResult.setSuccess(false);
		auditResult.setMessage("文件内容涉及敏感信息");
		auditResult.setNextAction("取消后续操作！");
		return auditResult;
	}
}));
```

审核拒绝时，大模型收到的返回结果：

```json
{
  "auditResult": "rejected",
  "message": "audit toolName readFile, failed for reason: 文件内容涉及敏感信息",
  "nextAction": "取消后续操作！"
}
```

## 1.4 自定义工具集成审核
自定义工具继承 `BaseAuditorTool`，在工具方法中调用 `audit` 方法即可集成审核：

```java
public class MyCustomTool extends BaseAuditorTool<MyCustomTool> {
	
	@Tool(name = "myMethod", description = "自定义工具方法")
	public Map myMethod(@ToolParam(name = "param1") String param1) {
		Map result = new HashMap();
		// 审核：传入工具名和内容，返回非 null 表示拒绝
		Map<String, Object> auditResult = audit("myMethod", param1);
		if (auditResult != null)
			return auditResult;
		// 审核通过，执行业务逻辑
		result.put("success", true);
		return result;
	}
}
```

`BaseAuditorTool` 提供三个 `audit` 重载方法：

| 方法签名                                 | 适用场景   |
| ---------------------------------------- | ---------- |
| `audit(String toolName)`                 | 无参数工具 |
| `audit(String toolName, String content)` | 单参数工具 |
| `audit(String toolName, Map toolInfo)`   | 多参数工具 |

## 1.5 注意事项
1. 未设置 `Auditor` 时（默认），审核逻辑自动跳过，不影响性能。
2. 审核在路径校验之后执行，路径越权时直接抛异常，不进入审核。
3. 审核失败时输出 WARN 日志（内容超 300 字符截断）。
4. `nextAction` 建议始终设置，帮助大模型理解后续操作方向。
5. `Auditor` 实例可能被多次调用，自定义实现应确保线程安全。