## AI 工具注解功能使用教程

本教程介绍如何通过 `@Tool` 和 `@ToolParam` 注解，将普通的 Java 方法快速定义为 AI 可调用的工具（Tool/Function Calling），并在 bboss AI 工作流智能体中使用。

---

### 一、核心注解概览

#### 1.1 @Tool 注解

`@Tool` 标注在方法上，用于声明该方法是一个 AI 可调用的工具。

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
    String name() default "";           // 工具名称，默认为方法名
    String description();                // 工具描述，AI 根据此描述判断何时调用该工具
    String type() default "function";    // 工具类型，默认为 function
    boolean strict() default true;       // 是否启用严格模式校验参数
    boolean additionalProperties() default false; // 是否允许额外属性
}
```


| 属性 | 说明 |
|------|------|
| `name` | 工具的标识名称。若留空，默认使用方法名。 |
| `description` | **关键字段**。AI 模型通过该描述理解工具的用途，决定是否需要调用。 |
| `type` | 工具类型，通常保持默认 `function` 即可。 |
| `strict` | 是否对参数进行严格校验。 |
| `additionalProperties` | 参数对象是否允许传入未定义的属性。 |

#### 1.2 @ToolParam 注解

`@ToolParam` 标注在方法的参数上，用于描述每个参数的类型、含义和约束，最终会被转换为 JSON Schema 供 AI 模型识别。

```java
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolParam {
    String type() default "object";     // 参数类型：string/integer/number/array/object 等
    String name();                       // 参数名称
    String description();                // 参数描述，帮助 AI 理解如何填写该参数
    boolean bean() default false;        // 若参数为 Bean，是否递归解析其字段作为子参数
    String format() default "";          // 格式校验：email、hostname、ipv4、uuid 等
    String pattern() default "";         // 正则表达式约束字符串格式
    String arrayItemType() default "";   // 数组元素类型
    String arrayItemDescription() default "";
    String[] enumValues() default {};    // 枚举值约束
    // 数值类型约束
    String minimum() default "";
    String maximum() default "";
    String defaultValue() default "";
    boolean required() default false;    // 是否必填
}
```


| 属性 | 说明 |
|------|------|
| `name` | 参数在 JSON Schema 中的名称。 |
| `description` | 参数的业务含义描述。 |
| `type` | 参数的数据类型，如 `string`、`integer`、`array`。 |
| `required` | 是否必填。 |
| `format` | 字符串格式校验，如 `email`、`ipv4`。 |
| `pattern` | 正则表达式，如 `^\d{6}$` 校验邮编。 |
| `enumValues` | 枚举值列表，限制参数只能取指定值。 |
| `bean` | 若参数是复杂对象，设置为 `true` 可自动展开其内部字段。 |

---

### 二、定义工具类

通过注解，任何一个普通的 Java 类都可以被声明为 AI 工具集。

#### 示例：酒店与机票预订工具

```java
package org.frameworkset.spi.ai.tools;

import org.frameworkset.spi.ai.model.annotation.Tool;
import org.frameworkset.spi.ai.model.annotation.ToolParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

 
public class PreOrderTool {
    @Tool(name="hotelQuery",description = "根据用户的行程需求，查询合适的酒店。"
    )
    public List<Map> hotelQuery(@ToolParam(name="startDay",description = "入驻时间,例如：5月25日",required = true) String startDay,
                                @ToolParam(name="endDay",description = "离房时间,例如：5月28日",required = true) String endDay){
        List<Map> hotels = new ArrayList<>();
        Map hotelData = new LinkedHashMap();
        hotelData.put("name","迁移山水酒店");
        hotelData.put("price","300$");
        hotelData.put("score",80);
        hotelData.put("devices","配套设施：健身房、保龄球");
        hotelData.put("position","位于市中心，交通便利");
        hotels.add(hotelData);

        hotelData = new LinkedHashMap();
        hotelData.put("name","俊逸酒店");
        hotelData.put("price","400$");
        hotelData.put("score",90);
        hotelData.put("devices","配套设施：健身房、保龄球、羽毛球");
        hotelData.put("position","位于郊区，环境优雅");
        hotels.add(hotelData);


        hotelData = new LinkedHashMap();
        hotelData.put("name","华天大酒店");
        hotelData.put("price","500$");
        hotelData.put("score",95);
        hotelData.put("devices","配套设施：健身房、保龄球、羽毛球、游泳池");
        hotelData.put("position","位于郊区，环境优雅，五星级环境");
        hotels.add(hotelData);
        return hotels;

    }

    @Tool(name="flightQuery",description = "根据用户的行程需求，查询合适的航班机票。" )
    public List<Map> flightQuery(@ToolParam(name="bookDay",description = "出发时间,例如：5月25日",required = true) String bookDay,
                                 @ToolParam(name="arriveDay",description = "到达时间,例如：5月28日",required = true) String arriveDay,
                                 @ToolParam(name="fromStation",description = "出发地,例如：长沙",required = true) String fromStation,
                                 @ToolParam(name="toStation",description = "到达地,例如：北京",required = true) String toStation){
        List<Map> hotels = new ArrayList<>();
        Map hotelData = new LinkedHashMap();
        hotelData.put("name","国航6678");
        hotelData.put("price","300$");
        hotelData.put("score",80);
        hotelData.put("devices","波音777");

        hotelData.put("leaveTime","14点30分");
        hotelData.put("arrivedTime","17点30分");
        hotelData.put("description","宽体大飞机，准点率99%");
        hotels.add(hotelData);

        hotelData = new LinkedHashMap();
        hotelData.put("name","南航5578");
        hotelData.put("price","400$");
        hotelData.put("score",70);
        hotelData.put("devices","空壳380");

        hotelData.put("leaveTime","15点30分");
        hotelData.put("arrivedTime","18点30分");
        hotelData.put("description","宽体大飞机，准点率90%");
        hotels.add(hotelData);


        hotelData = new LinkedHashMap();
        hotelData.put("name","厦门航空3378");
        hotelData.put("price","300$");
        hotelData.put("score",80);
        hotelData.put("devices","波音730");

        hotelData.put("leaveTime","16点30分");
        hotelData.put("arrivedTime","18点30分");
        hotelData.put("description","宽体大飞机，准点率100%");
        hotels.add(hotelData);
        return hotels;

    }

    


}
```


**关键点：**
- `@Tool` 的 `description` 决定了 AI 模型在什么场景下会选择调用该方法。
- `@ToolParam` 的 `description` 和 `required` 帮助 AI 模型正确提取和填充参数。

---

### 三、在工作流中注册和使用工具

定义好工具类后，需要将其注册到 AI 智能体工作流中。

#### 3.1 注册工具

使用 `BeanToolsRegist` 将工具类的实例注册为工具集：

```java
// 1. 实例化工具类
PreOrderTool preOrderTool = new PreOrderTool();

// 2. 创建工具注册器
ToolsRegist toolsRegist = new BeanToolsRegist(preOrderTool);
```


#### 3.2 将工具绑定到智能体节点

在 `AINodeAgent` 中通过 `setToolsRegist()` 绑定工具：

```java
planAgent.addRouteChoiceAgent(
    new AINodeAgent("请根据用户的行程需求查询并推荐合适的酒店...")
        .setAgentId("hotelAgent")
        .setAgentName("酒店查询智能体")
        .setToolsRegist(toolsRegist)   // 绑定工具
);
```


**说明：** 当该智能体节点执行时，如果 AI 模型判断需要调用外部工具，就会根据 `@Tool` 的描述自动选择 `hotelBook` 或 `flightBook` 方法，并提取用户问题中的参数进行调用。

---

### 四、完整工作流示例（非流式）

以下是一个完整的酒店+机票预订智能体工作流，演示了从路由判断到工具调用的完整流程：

```java
public class BookingTest {
        public static void main(String[] args) throws InterruptedException, IOException {
        // 初始化HTTP连接池
        HttpRequestProxy.startHttpPools("application-stream.properties");
        HttpRequestProxy.startHttpPools("mcpserver.properties");

        // 场景1：只查询酒店
//        bookingWorkflowStream("kimi", "帮我预定北京市中心5月25日到5月28日的五星级酒店", "kimi-k2.6", null);

        // 场景2：只查询机票
//        bookingWorkflowStream("kimi", "帮我预定5月25日上海到北京的机票，要上午的航班", "kimi-k2.6", null);

        // 场景3：酒店和机票都要（路由到并行查询）
//        bookingWorkflowStream("kimi", "我5月25日到5月28日要去北京出差，帮我预定酒店和机票", "kimi-k2.6", null);

        bookingWorkflow("qwenvlplus", "我5月25日到5月28日要去北京出差，帮我预定酒店和机票", "qwen3.6-plus", null);
    }
    public static void bookingWorkflow(String maas, String prompt, String model, String sessionId) throws InterruptedException {
        // 1. 定义会话实体：设置模型、maas平台，用户问题，开启流式输
        ChatAgentMessage chatAgentMessage = new ChatAgentMessage()
            .setModel(model)
            .setMaas(maas)
            .setPrompt(prompt);

        //定义一个输出接口，可以用于输出各个智能体的执行结果
       AgentOutput agentOutput = new AgentOutput() {
            @Override
            public void output(ServerEvent message) {
                System.out.println("............................");
                if(message.getData() != null) {
                    System.out.println(message.getData());
                }
                else {

                    System.out.println(message.getFullStreamData());
                }
            }
        };
        // 定义工作流智能体，设置会话存储机制为DB
        AIPlanAgent planAgent = new AIPlanAgent(new StoreContext()
                .setSessionId(sessionId).setUserId("user123").setSessionSize(100)
                .setStoreType(StoreContext.STORE_TYPE_DB)
                .setDataSource("visualops"))
                .setAgentMessage(chatAgentMessage)
                .setAgentName("预定工作流智能体").setAgentId("bookingWorkflowAgent");

        // ====================  路由智能体（判断用户意图） ====================
        
        // 路由智能体判断用户意图：酒店、机票、都要
        planAgent.addAgent(new AIRouteAgent()
                .setAgentId("bookingRouter").setAgentName("预定路由智能体")
                .setSystemPrompt("你是一个行程预定路由智能体。请分析用户的问题，判断用户需要预定什么，注意你不需要直接回答用户的问题，只需要做路由判断。")
                .addRoutingChoice("hotelAgent", "用户只需要预定酒店")
                .addRoutingChoice("flightAgent", "用户只需要预定机票")
                .addRoutingChoice("bothAgent", "用户需要同时预定酒店和机票")
        );
        // 定义注册工具
        ToolsRegist toolsRegist = new BeanToolsRegist(new PreOrderTool());
        // ==================== 阶段2：分支查询智能体 ====================
        // 酒店查询智能体（当路由到hotelAgent时执行）
        planAgent.addRouteChoiceAgent(new AINodeAgent(
                "请根据用户的行程需求:#[input.query]，查询并推荐合适的酒店。" +
                        "需要考虑：地理位置、价格区间、用户评分、配套设施等因素。" +
                        "给出至少3个推荐选项，并说明理由。")
                .setAgentId("hotelAgent")
                .setAgentName("酒店查询智能体")
                
                .setToolsRegist(toolsRegist));

        // 机票查询智能体（当路由到flightAgent时执行）
        planAgent.addRouteChoiceAgent(new AINodeAgent(
                "请根据用户的行程需求:#[input.query]，查询并推荐合适的航班。" +
                        "需要考虑：出发时间、到达时间、航空公司、价格、准点率等因素。" +
                        "给出至少3个推荐选项，并说明理由。")
                .setAgentId("flightAgent").setAgentName("机票查询智能体") 
                .setToolsRegist(toolsRegist));

        // ==================== 阶段3：并行查询智能体（都要的场景） ====================
        // 当用户同时需要酒店和机票时，并行执行查询
        AIParrelAgent bothAgent = new AIParrelAgent(planAgent)
                .setAgentId("bothAgent").setAgentName("并行查询智能体");

        bothAgent.addAgent(new AINodeAgent(
                "请根据用户的行程需求:#[input.query]，查询并推荐合适的酒店。" +
                        "需要考虑：地理位置（尽量靠近市中心或商务区）、价格区间、用户评分、配套设施等因素。" +
                        "给出至少3个推荐选项，并说明理由。")
                .setAgentId("parrelHotelAgent").setAgentName("并行酒店查询")
                .setToolsRegist(toolsRegist));

        bothAgent.addAgent(new AINodeAgent(
                "请根据用户的行程需求:#[input.query]，查询并推荐合适的航班。" +
                        "需要考虑：出发时间、到达时间、航空公司、价格、准点率等因素。" +
                        "给出至少3个推荐选项，并说明理由。")
                .setAgentId("parrelFlightAgent").setAgentName("并行机票查询")
                .setToolsRegist(toolsRegist));

//        bothAgent.setAgentOutput(agentOutput);

        planAgent.addRouteChoiceAgent(bothAgent);

        // ==================== 阶段4：默认智能体 ====================
        // 当路由匹配不上时，直接回答用户问题
        planAgent.addDefaultRouteChoiceAgent(new AINodeAgent(
                "请根据用户的问题:#[input.query]，提供有帮助的行程和预定相关建议。")
                .setAgentId("defaultAgent").setAgentName("默认智能体") );

        // ==================== 阶段5：汇总智能体 ====================
        // 汇总前面所有节点的结果，给出最终的预定建议
        planAgent.addAgent(new AINodeAgent(
                "请综合前面的查询结果，为用户提供一份完整的预定建议报告。" +
                        "报告需要包含：1)推荐的酒店及理由 2)推荐的航班及理由 3)总预算估算 4)最终操作建议。" +
                        "请用清晰的中文输出。")
                .setAgentId("summaryAgent").setAgentName("汇总建议智能体").setOutputVaribleName("aaaa", AIFlowConst.AIFLOW_VAR_SCOPE_FLOW) );

        // 通过飞书mcp接口，将汇总智能体结果创建为飞书文档
        ToolsRegist mcpToolsRegist = new FeishuMcpRegist("feishumcp");
        planAgent.addAgent(new AINodeAgent(
                "请根据用户的问题:#[input.query]，以及前面的汇总建议，创建一份详细的飞书报告。" +
                        "报告需要包含：1)推荐的酒店及理由 2)推荐的航班及理由 3)总预算估算 4)最终操作建议。" +
                        "请用清晰的中文输出。")
                .setAgentId("feishudocAgent").setAgentName("飞书文档智能体").setToolsRegist(mcpToolsRegist) );
        // 9. 执行工作流
        LastSessionMessage result = planAgent.chat();
        System.out.println(result.getData());
    }
}
```


---

### 五、流式输出版本

如果需要在工具执行过程中实时看到 AI 的推理过程和结果，可以使用流式版本：

```java
public class BookingStreamTest {
        public static void main(String[] args) throws InterruptedException, IOException {
        // 初始化HTTP连接池
        HttpRequestProxy.startHttpPools("application-stream.properties");
        HttpRequestProxy.startHttpPools("mcpserver.properties");

        // 场景1：只查询酒店
//        bookingWorkflowStream("kimi", "帮我预定北京市中心5月25日到5月28日的五星级酒店", "kimi-k2.6", null);

        // 场景2：只查询机票
//        bookingWorkflowStream("kimi", "帮我预定5月25日上海到北京的机票，要上午的航班", "kimi-k2.6", null);

        // 场景3：酒店和机票都要（路由到并行查询）
//        bookingWorkflowStream("kimi", "我5月25日到5月28日要去北京出差，帮我预定酒店和机票", "kimi-k2.6", null);

        bookingWorkflowStream("qwenvlplus", "我5月25日到5月28日要去北京出差，帮我预定酒店和机票", "qwen3.6-plus", null);
    }
    public static void bookingWorkflowStream(String maas, String prompt, String model, String sessionId) throws InterruptedException {
        ChatAgentMessage chatAgentMessage = new ChatAgentMessage()
            .setModel(model)
            .setStream(true)   // 开启流式输出
            .setMaas(maas)
            .setPrompt(prompt);

        AIPlanAgent planAgent = new AIPlanAgent(...)
            .setAgentMessage(chatAgentMessage);

        // ... 工作流定义与非流式相同 ...

        // 流式执行
        Flux<ServerEvent> flux = planAgent.chatStream();
        flux.doOnNext(event -> {
            if (event.getData() != null) {
                System.out.print(event.getData());  // 实时输出内容
            }
            if (event.isToolCallsType()) {
                System.out.println("开始执行工具："); // 捕获工具调用事件
            }
        }).subscribe();
    }
}
```


**流式优势：**
- 实时展示 AI 思考过程和工具调用状态。
- 通过 `event.isToolCallsType()` 可以感知工具开始执行。
- 通过 `event.isDone()` 可以捕获会话结束事件。

---

### 六、常见问题与最佳实践

1. **description 怎么写？**
    - `@Tool` 的 `description` 应清晰描述工具的用途、适用场景、返回内容，这是 AI 判断是否调用的唯一依据。
    - `@ToolParam` 的 `description` 应说明参数的业务含义、格式示例。

2. **工具方法返回值**
    - 推荐返回 `Map`、`List<Map>` 或 JSON 字符串，便于 AI 模型理解和后续处理。

3. **多个工具类**
    - 可以创建多个工具类，分别用 `BeanToolsRegist` 注册，并绑定到不同的智能体节点。

4. **参数为复杂对象**
    - 保留参数，暂未实现，如果参数是自定义 Bean，在 `@ToolParam` 上设置 `bean = true`，框架会自动递归解析 Bean 的字段生成 JSON Schema。

5. **参数校验**
    - 利用 `format`、`pattern`、`enumValues`、`minimum`、`maximum` 等属性，可以生成符合 JSON Schema 规范的参数约束，让 AI 输出更准确的参数值。

---

### 七、总结

通过 `@Tool` 和 `@ToolParam` 注解，开发者无需编写复杂的 JSON Schema，只需以自然的 Java 注解方式定义方法，即可让 AI 模型具备调用业务系统的能力。结合 `AIPlanAgent` 工作流，可以实现路由判断、并行查询、工具调用、结果汇总等复杂的智能体协作场景。