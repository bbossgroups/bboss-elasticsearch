# 多智能体协同：轻松搞定智能体工具调用

在上篇文档中介绍了[通过bboss jobflow实现基于Deepseek的多智能体流程排功能](https://esdoc.bbossgroups.com/#/jobflow-deepseek)，本文在上文中案例基础上继续扩展流程功能，实现智能体工具调用功能：将用户问题和工具清单发送给 Deepseek，并由 Deepseek 匹配对应工具并提取参数，通过工具调用节点调用工具，最后通过生成查询结果和建议节点调用Deepseek分析工具调用结果数据，生成并输出最终问题答案。

![](images\workflow\jobworkflow-toolcall.png)

完整案例源码地址：

[https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlow2ndDeepseekTest.java](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlow2ndDeepseekTest.java)

以下介绍具体技术实现：

---

## ✅1.  功能目标

将用户的问题（如“查询杭州天气”）与定义好的工具清单（如 `get_weather`）一起提交给 Deepseek 的 `/chat/completions` 接口，让 Deepseek 自动识别需要调用哪个工具，并提取出对应的参数（如 `location: "杭州"`）。

---

## 🧩2.  核心步骤解析

### 2.1. 构建请求内容

你需要构建一个包含以下信息的 `DeepseekMessages` 对象：
- **messages**：历史对话记录（包括用户提问）
- **tools**：可调用的工具描述列表
- **model**：使用的模型名称（如 `"deepseek-chat"`）
- **stream**：是否启用流式响应
- **max_tokens**：最大输出 token 数量

```java
DeepseekMessages deepseekMessages = new DeepseekMessages();
deepseekMessages.setMessages(deepseekMessageList); // 历史对话记录
deepseekMessages.setModel(model); // 模型名
deepseekMessages.setStream(stream); // 是否流式输出
deepseekMessages.setMax_tokens(this.max_tokens); // 最大 token 数量
deepseekMessages.setTools(tools); // 工具清单
```


### 2.2. 定义工具清单（JSON 格式）

你使用 JSON 字符串定义了一个工具 `get_weather`，它接受一个 `location` 参数：

```json
[
    {
        "type": "function",
        "function": {
            "name": "get_weather",
            "description": "Get weather of an location, the user should supply a location first",
            "parameters": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA"
                    }
                },
                "required": ["location"]
            }
        }
    }
]
```


> 💡 注意：确保 JSON 格式正确，否则 Deepseek 可能无法解析工具描述。

### 2.3. 发送请求到 Deepseek API

使用 `HttpRequestProxy.sendJsonBody()` 向 Deepseek 的 `/chat/completions` 接口发送请求：

```java
Map response = HttpRequestProxy.sendJsonBody(this.getDeepseekService(), deepseekMessages, "/chat/completions", Map.class);
```


返回结果中会包含匹配的工具信息及参数，例如：

```json
{
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "",
        "tool_calls": [
          {
            "id": "call_abc123",
            "type": "function",
            "function": {
              "name": "get_weather",
              "arguments": "{\"location\": \"杭州\"}"
            }
          }
        ]
      }
    }
  ]
}
```


### 2.4. 提取工具调用信息

从响应中提取工具调用详情：

```java
List<Map> toolcalls = (List<Map>) message.get("tool_calls");
Map tool = toolcalls.get(0);

String toolId = (String) tool.get("id");
String functionName = (String) ((Map) tool.get("function")).get("name");
String functionArguments = (String) ((Map) tool.get("function")).get("arguments");

Map arguments = SimpleStringUtil.json2Object(functionArguments, Map.class);
String location = (String) arguments.get("location"); // 提取出城市名："杭州"
```


### 2.5. 调用工具并反馈结果

模拟调用工具函数并构造响应消息：

```java
logger.info("模拟调用函数：{}(\"{}\")，返回值为：24℃", functionName, location);

// 构造 tool 角色的消息
DeepseekMessage deepseekMessage = new DeepseekMessage();
deepseekMessage.setRole("tool");
deepseekMessage.setContent("24℃");
deepseekMessage.setTool_call_id(toolId);
deepseekMessageList.add(deepseekMessage);
```


### 2.6. 再次调用 Deepseek 获取最终回答

将工具结果作为上下文再次传入 Deepseek，生成自然语言的回答：

```java
deepseekMessages = new DeepseekMessages();
deepseekMessages.setMessages(deepseekMessageList);
deepseekMessages.setModel(model);
deepseekMessages.setStream(stream);
deepseekMessages.setMax_tokens(this.max_tokens);

response = HttpRequestProxy.sendJsonBody(this.getDeepseekService(), deepseekMessages, "/chat/completions", Map.class);
//提取最终回答并记录消息记录到历史记录清单
choices = (List) response.get("choices");
message = (Map) ((Map) choices.get(0)).get("message");
deepseekMessage = new DeepseekMessage();
deepseekMessage.setRole("assistant");
deepseekMessage.setContent((String) message.get("content"));
//将第二个问题答案添加到工作流上下文中，保存Deepseek通话记录
deepseekMessageList.add(deepseekMessage);
//输出查询杭州天气结果以及饮食、衣着及出行建议
logger.info(deepseekMessage.getContent());
```


---



## ✅ 3. 示例完整调用逻辑

```java
		/**
         * 5.构建第三个任务节点：单任务节点 调用工具查询杭州天气
         */
        jobFlowNodeBuilder = new DeepseekJobFlowNodeBuilder("3", "Deepseek-chat-天气查询", new DeepseekJobFlowNodeFunction() {
            @Override
            public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) throws Exception {
                //从工作流上下文中，获取Deepseek历史通话记录
                List<DeepseekMessage> deepseekMessageList = (List<DeepseekMessage>) jobFlowNodeExecuteContext.getJobFlowContextData("messages");
                if(deepseekMessageList == null){
                    deepseekMessageList = new ArrayList<>();
                    jobFlowNodeExecuteContext.addJobFlowContextData("messages",deepseekMessageList);
                }
                //用户查询杭州天气
                DeepseekMessage deepseekMessage = new DeepseekMessage();

                deepseekMessage.setRole("user");
                // 用户问题
                deepseekMessage.setContent("查询杭州天气，并根据天气给出穿衣、饮食以及出行建议");

                // 构建请求对象
                DeepseekMessages deepseekMessages = new DeepseekMessages();
                deepseekMessages.setMessages(deepseekMessageList);
                deepseekMessages.setModel("deepseek-chat");
                deepseekMessages.setTools(tools);

                // 发起请求
                Map response = HttpRequestProxy.sendJsonBody("deepseek", deepseekMessages, "/chat/completions", Map.class);

                // 解析响应中的工具调用
                List<Map> toolcalls = (List<Map>) ((Map) ((Map) response.get("choices")).get(0)).get("message").get("tool_calls");
                Map tool = toolcalls.get(0);
                String location = (String) SimpleStringUtil.json2Object((String) ((Map) tool.get("function")).get("arguments"), Map.class).get("location");

                // 模拟调用工具
                logger.info("调用 get_weather({})", location);

                // 构造 tool 返回消息
                DeepseekMessage toolResponse = new DeepseekMessage();
                toolResponse.setRole("tool");
                //设置工具返回的杭州天气温度
                toolResponse.setContent("24℃");
                toolResponse.setTool_call_id((String) tool.get("id"));
                deepseekMessageList.add(toolResponse);

                // 再次调用 Deepseek 生成最终回答
                deepseekMessages.setMessages(deepseekMessageList);
                response = HttpRequestProxy.sendJsonBody("deepseek", deepseekMessages, "/chat/completions", Map.class);
                choices = (List) response.get("choices");
                message = (Map) ((Map) choices.get(0)).get("message");
                deepseekMessage = new DeepseekMessage();
                deepseekMessage.setRole("assistant");
                deepseekMessage.setContent((String) message.get("content"));
                //将第二个问题答案添加到工作流上下文中，保存Deepseek通话记录
                deepseekMessageList.add(deepseekMessage);
                //输出查询杭州天气结果以及饮食、衣着及出行建议
                logger.info(deepseekMessage.getContent());
                
                return response;
            }

        }).setDeepseekService("deepseek").setModel("deepseek-chat").setMax_tokens(4096);

        /**
         * 4 将第工具调用节点添加到工作流构建器
         */
        jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);
```


---
## 📌 4. 总结

本文通过实际案例代码，详细地介绍了AI智能体工具调用功能：将用户问题和工具清单发送给 Deepseek，并由 Deepseek 匹配对应工具并提取参数，通过工具调用节点调用工具，最后通过生成查询结果和建议节点调用Deepseek分析工具调用结果数据，生成并输出最终问题答案。

| 步骤 | 目的 |
|------|------|
| 构建 `DeepseekMessages` | 准备请求数据 |
| 设置 `tools` 属性 | 提供可用工具描述 |
| 发送请求到 `/chat/completions` | 让 Deepseek 解析用户意图并选择工具 |
| 解析 `tool_calls` | 提取匹配的工具及参数 |
| 模拟调用工具 | 执行实际业务逻辑获取结果 |
| 再次调用 Deepseek | 结合原始问题与工具结果生成自然语言回复 |

---