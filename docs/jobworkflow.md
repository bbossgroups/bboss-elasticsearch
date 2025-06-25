# 通用工作流使用介绍

​		bboss jobflow **通用分布式作业调度工作流**，提供通用轻量级、高性能流程编排模型，可将各种各样、不同类型的任务编排成工作流，进行统一调度执行，譬如数据采集作业任务、流批处理作业任务、业务办理任务、充值缴费任务以及大模型推理任务等按顺序编排成工作流。

![](images\workflow\jobworkflow.png)

## 1. 工作流概述

​		通用工作流由各种作业节点构成，通过流程调度控制流程执行生命周期，可以一次性执行，亦可以周期性执行。

### 1.1 引入bboss jobflow		

​      导入一个maven坐标，即刻拥有bboss jobflow：

```xml
    <dependency>
        <groupId>com.bbossgroups.plugins</groupId>
        <artifactId>bboss-datatran-jdbc</artifactId>
        <version>7.3.9</version>
    </dependency>
```
## 2. 功能特色

工作流特性说明：

  - **节点类型**， 基础节点默认提供了数据交换和流批处理作业节点、通用函数节点以及复合类型节点（串/并行执行），可以按需自定义扩展新的流程节点、远程服务执行节点，内置节点说明：

    ​       1）数据交换节点：指定作业ImportBuilder，可以指定条件触发器，控制节点是否执行
    ​       2）通用节点：指定节点执行函数，自定义节点执行业务逻辑，可以指定条件触发器，控制节点是否执行
    ​       3）并行任务节点：多个并行分支构成的复合节点，各分支可以由多种节点类型组成，各分支并行运行，			可以指定条件触发器，控制整个并行任务节点是否执行，各分支节点及内部子节点都可以指定条件触发			器
    ​       4）串行任务节点：多个节点构成的复合节点，各节点按照顺序执行，可以指定条件触发器，控制整个串			行节点是否执行；可以为子节点设置触发器，如果子节点不满足条件，则子节点及对应节点后续节点都			不会执行

  - **条件触发器**，可以为流程节点设置条件触发器，控制流程节点是否执行，可以采用触发器接口和触发器脚本（Groovy）实现条件判断，控制节点是否执行

  - **流程上下文**，通过流程上下文和节点上下文在节点间传递和共享参数；

  - **流程监控**，通过设置流程执行和节点执行监听器，可以更新和维护流程和节点执行参数，采集和获取流程、节点执行监控指标以及执行异常信息。

  - **流程控制**，通过启动、停止、暂停、恢复流程控制API，控制和管理流程执行生命周期

  - **调度策略**，可以一次性执行，亦可以周期性执行,自带灵活定时调度策略，支持xxl-job和quartz两种外部调度机制，可以非常方便地扩展支持其他调度引擎

##   3. 关键组件

提供以下8个关键组件来支撑工作流的具体功能：

  - **JobFlowBuilder** 工作流构建器组件，为工作流设置id和名称，添加和配置流程节点、流程调度策略、流程执行监听器，配置完毕后即可创建JobFlow对象

  - **JobFlow**  工作流对象组件，通过JobFlowBuilder的build方法创建，用于管理调度和执行工作流，jobflow提供以下方法来管理和控制工作流的启动、停止、暂停、恢复

      - start
      - stop
      - pause
      - consume

  - **TimerScheduleConfig** 内置流程调度执行策略配置组件，用于设置流程调度执行策略，包括：

      - 一次性执行策略，只启动执行一次，执行完毕后关闭工作流实例
      - 周期性执行策略，可以设置流程执行开始时间、结束时间，执行时间段、忽略执行时间段，执行时间间隔
        - 默认策略
        - xxl-job策略
        - quartz策略

  - **NodeTrigger**  流程节点执行触发条件配置组件，配置节点是否执行控制接口或控制脚本，可以为流程节点设置条件触发器，控制流程节点是否执行，可以采用触发器接口和触发器脚本（Groovy）实现条件判断，控制节点是否执行

  - **JobFlowNodeBuilder**  流程节点抽象构建器，提供以下四种默认实现

      - **DatatranJobFlowNodeBuilder** 数据交换节点构建器，用于指定数据交换、流批处理作业构建器ImportBuilder，指定条件触发器控制节点是否执行，**配置到工作流中的数据交换、流批处理作业都必须是一次性执行的作业**。

      - **ParrelJobFlowNodeBuilder** 并行任务节点构建器，配置由多个并行分支构成的复合节点，各分支可以由多种节点类型组成，各分支并行运行；可以指定条件触发器，控制整个并行任务节点是否执行，各分支节点及内部子节点都可以指定条件触发器。

      - **SequenceJobFlowNodeBuilder** 串行任务节点构建器，配置由多个节点构成的串行复合节点，各节点按照顺序串行执行，可以指定条件触发器，控制整个串行节点是否执行；亦可以为子节点设置触发器，如果子节点不满足条件，则子节点及对应节点后续节点都不会执行

      - **CommonJobFlowNodeBuilder**   通用自定义节点构建器，指定节点执行函数，自定义节点执行业务逻辑，可以指定条件触发器，控制节点是否执行

        通过扩展基础构建器SimpleJobFlowNodeBuilder，可以实现其他类型节点构建器

  - **JobFlowNodeFunction** 自定义流程节点执行函数组件，一个节点只保存JobFlowNodeFunction的一个实例，因此一次调度执行完毕后，需要通过reset重置状态，release释放资源

  - **JobFlowListener**  工作流执行拦截器， 可以更新和维护流程执行参数，采集和获取流程执行监控指标以及执行异常信息 

  - **JobFlowNodeListener**  工作流节点执行拦截器，可以更新和维护流程和流程节点执行参数，采集和获取流程、流程节点执行监控指标以及执行异常信息

下面结合实际的案例来说明工作流的使用。

## 4.  工作流案例
以下是一个完整的工作流示例，展示了如何使用 JobFlowBuilder 构建包含多种节点类型（单任务、并行任务、串行任务）的工作流，并通过 JobFlow 控制其生命周期。

### 4.1 案例说明

该案例演示了一个数据采集与处理流程：

1. **第一个节点**：从 Excel 文件中读取数据并写入数据库。
2. **第二个节点**：并行执行多个任务：
   - 数据库到自定义输出的任务。
   - 子任务组合（嵌套串行和并行任务）。
3. **第三个节点**：再次执行数据库到自定义输出的任务。

每个节点可以配置触发器，决定是否执行。整个流程支持启动、停止、暂停等控制操作。

### 4.2 核心代码解析

#### 4.2.1 构建工作流

```java
JobFlowBuilder jobFlowBuilder = new JobFlowBuilder();
jobFlowBuilder.setJobFlowName("测试流程")
              .setJobFlowId("测试id");
```

创建工作流构建器并设置名称和 ID。

#### 4.2.2 设置调度策略

周期性执行策略，可以设置流程执行开始时间、结束时间，执行时间段、忽略执行时间段，执行时间间隔

```java
JobFlowScheduleConfig jobFlowScheduleConfig = new JobFlowScheduleConfig();
jobFlowScheduleConfig.setScheduleDate(TimeUtil.addDateMinitues(new Date(), 10));//十分钟后开始执行
jobFlowScheduleConfig.setScheduleEndDate(TimeUtil.addDates(new Date(), 10));//十天后结束运行
jobFlowScheduleConfig.setPeriod(100000L);//每100秒运行一次
jobFlowScheduleConfig.setExecuteOneTime(true);//启用一次性执行策略后，其他定时配置将不起作用
jobFlowBuilder.setJobFlowScheduleConfig(jobFlowScheduleConfig);

```

配置一次性执行策略，设定结束时间和周期。

#### 4.2.3 添加第一个任务节点（单任务）

```java
DatatranJobFlowNodeBuilder jobFlowNodeBuilder = new DatatranJobFlowNodeBuilder("1", "DatatranJobFlowNode");
NodeTrigger nodeTrigger = new NodeTrigger();

String script = new StringBuilder()
        .append("[import]")
        .append(" //import org.frameworkset.tran.jobflow.context.StaticContext; ")
        .append("[/import]")
        .append("StaticContext staticContext = nodeTriggerContext.getPreJobFlowStaticContext();")
        .append("if(staticContext != null && staticContext.getExecuteException() != null)")
        .append("    return false;")
        .append("else{")
        .append("    return true;")
        .append("}").toString();
nodeTrigger.setTriggerScript(script);

jobFlowNodeBuilder.setImportBuilder(buildFile2DB()).setNodeTrigger(nodeTrigger);
jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);

```

数据交换作业节点，该节点从 Excel 文件导入数据并插入数据库，配置了条件触发器脚本，只有前序节点成功时才执行。

#### 4.2.4 添加第二个任务节点（并行任务）

```java
ParrelJobFlowNodeBuilder parrelJobFlowNodeBuilder = new ParrelJobFlowNodeBuilder("2", "ParrelJobFlowNode");
//为并行任务节点添加触发器
NodeTrigger parrelnewNodeTrigger = new NodeTrigger();
        parrelnewNodeTrigger.setTriggerScriptAPI(new TriggerScriptAPI() {
            @Override
            public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                
                return true;
            }
        });
parrelJobFlowNodeBuilder.setNodeTrigger(parrelnewNodeTrigger);

parrelJobFlowNodeBuilder.addJobFlowNodeBuilder(
    new DatatranJobFlowNodeBuilder("ParrelJobFlowNode-DatatranJobFlowNode-2-1", "ParrelJobFlowNode-DatatranJobFlowNode-2")
        .setImportBuilder(buildDB2Custom(1))
        .setNodeTrigger(nodeTrigger));

parrelJobFlowNodeBuilder.addJobFlowNodeBuilder(
    new DatatranJobFlowNodeBuilder("ParrelJobFlowNode-DatatranJobFlowNode-2-2", "ParrelJobFlowNode-DatatranJobFlowNode-2")
        .setImportBuilder(buildDB2Custom(2)));

SequenceJobFlowNodeBuilder comJobFlowNodeBuilder = new SequenceJobFlowNodeBuilder("ParrelJobFlowNode-2-3", "SequenceJobFlowNode");
comJobFlowNodeBuilder.addJobFlowNodeBuilder(
    new DatatranJobFlowNodeBuilder("ParrelJobFlowNode-2-3-1", "SequenceJobFlowNode-SequenceJobFlowNode")
        .setImportBuilder(buildDB2Custom(3)));
comJobFlowNodeBuilder.addJobFlowNodeBuilder(
    new DatatranJobFlowNodeBuilder("ParrelJobFlowNode-2-3-2", "SequenceJobFlowNode-SequenceJobFlowNode")
        .setImportBuilder(buildDB2Custom(4)));
comJobFlowNodeBuilder.addJobFlowNodeBuilder(
    new CommonJobFlowNodeBuilder("ParrelJobFlowNode-2-3-3", "SequenceJobFlowNode-SequenceJobFlowNode", new JobFlowNodeFunctionTest(false))
        .setNodeTrigger(nodeTrigger));

parrelJobFlowNodeBuilder.addJobFlowNodeBuilder(comJobFlowNodeBuilder);

ParrelJobFlowNodeBuilder subParrelJobFlowNodeBuilder = new ParrelJobFlowNodeBuilder("ParrelJobFlowNode-2-4", "ParrelJobFlowNode");
subParrelJobFlowNodeBuilder.addJobFlowNodeBuilder(
    new DatatranJobFlowNodeBuilder("ParrelJobFlowNode-2-4-1", "ParrelJobFlowNode-SequenceJobFlowNode")
        .setImportBuilder(buildDB2Custom(5)));
subParrelJobFlowNodeBuilder.addJobFlowNodeBuilder(
    new DatatranJobFlowNodeBuilder("ParrelJobFlowNode-2-4-2", "ParrelJobFlowNode-SequenceJobFlowNode")
        .setImportBuilder(buildDB2Custom(6)));

parrelJobFlowNodeBuilder.addJobFlowNodeBuilder(subParrelJobFlowNodeBuilder);
jobFlowBuilder.addJobFlowNode(parrelJobFlowNodeBuilder);
```

此节点为复合结构，包含三个子任务：

- 两个独立的数据库到自定义输出任务。
- 一个串行任务节点，包含两个数据库任务和一个通用函数任务。
- 一个并行任务节点，包含两个数据库任务。

#### 4.2.5 添加第三个任务节点（单任务）
```java
jobFlowNodeBuilder = new DatatranJobFlowNodeBuilder("3", "DatatranJobFlowNode");
jobFlowNodeBuilder.setImportBuilder(buildDB2Custom(7)).setNodeTrigger(nodeTrigger);
jobFlowBuilder.addJobFlowNode(jobFlowNodeBuilder);
```

最后一个节点继续执行数据库到自定义输出的任务。



#### 4.2.6 启动工作流

```java
JobFlow jobFlow = jobFlowBuilder.build();
jobFlow.start();
```

构建并启动工作流。

#### 4.2.7 工作流控制 API

你可以使用以下方法控制工作流的生命周期：

```java
jobFlow.stop();   // 停止
jobFlow.pause();  // 暂停
jobFlow.consume(); // 继续执行
```



#### 4.2.8 工作流控制服务实现

基于spring boot3的工作流控制web服务案例

https://gitee.com/bboss/springboot3-elasticsearch-webservice/blob/main/src/main/java/com/example/esbboss/jobflow/JobFlowDemo.java



https://gitee.com/bboss/springboot3-elasticsearch-webservice/blob/main/src/main/java/com/example/esbboss/controller/ScheduleControlDataTranController.java

### 4.3 案例小结

本案例展示了如何使用 BBoss 的通用工作流组件构建复杂的任务流程，包括：

- 多种类型的节点（单任务、串行、并行）。
- 条件触发器控制节点执行。
- 内置线程池实现并行处理。
- 灵活的调度策略（一次性或周期性执行）。
- 流程控制接口（启动、停止、暂停、恢复）。

bboss通用工作流模型适用于各种业务场景，如数据采集、批处理、业务流程编排等。

由于篇幅关系，文中涉及的两个创建数据交换作业ImportBuilder方法buildFile2DB和buildDB2Custom访问完整的案例代码了解：

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowTest.java

通用通用函数任务实现通过以下代码了解：

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowNodeFunctionTest.java





## 5. 流程监控

BBoss 提供了两种类型的监听器接口，用于监控和干预工作流及其节点的执行过程：

- `JobFlowListener`：**流程级监听器**，用于监听整个工作流的生命周期事件。
- `JobFlowNodeListener`：**节点级监听器**，用于监听每个节点的执行状态。

这些监听器非常适合用于以下场景：

- 日志记录与审计
- 异常捕获与报警
- 性能监控与统计
- 流程状态持久化
- 动态调整流程参数

---

### 5.1 JobFlowListener（流程监听器）

该接口用于监听整个工作流的执行生命周期事件，包括：

| 方法 | 触发时机 |
|------|----------|
| `beforeStart(JobFlow jobFlow)` | 流程开始前调用 |
| `beforeExecute(JobFlowExecuteContext context)` | 流程调度执行前调用 |
| `afterExecute(JobFlowExecuteContext context, Throwable throwable)` | 流程调度执行后调用（可获取执行结果或异常信息） |
| `afterEnd(JobFlow jobFlow)` | 流程结束时调用 |

#### 5.1.1 示例代码

```java
import org.frameworkset.tran.jobflow.JobFlow;
import org.frameworkset.tran.jobflow.context.JobFlowExecuteContext;
import org.frameworkset.tran.jobflow.listener.JobFlowListener;

public class MyJobFlowListener implements JobFlowListener {

    @Override
    public void beforeStart(JobFlow jobFlow) {
        System.out.println("【流程监听器】流程即将启动");
    }

    @Override
    public void beforeExecute(JobFlowExecuteContext context) {
        System.out.println("【流程监听器】流程调度执行前");
    }

    @Override
    public void afterExecute(JobFlowExecuteContext context, Throwable throwable) {
        if (throwable != null) {
            System.err.println("【流程监听器】流程执行发生异常：" + throwable.getMessage());
        } else {
            System.out.println("【流程监听器】流程执行完成");
        }
    }

    @Override
    public void afterEnd(JobFlow jobFlow) {
        System.out.println("【流程监听器】流程已结束");
    }
}
```


#### 5.1.2 注册监听器到流程

```java
jobFlowBuilder.addJobFlowListener(new MyJobFlowListener());
```

---

### 5.2 JobFlowNodeListener（节点监听器）

该接口用于监听每个节点的执行状态，包括：

| 方法 | 触发时机 |
|------|----------|
| `beforeExecute(JobFlowNodeExecuteContext context)` | 节点执行前调用 |
| `afterExecute(JobFlowNodeExecuteContext context, Throwable throwable)` | 节点执行后调用（可获取执行结果或异常信息） |
| `afterEnd(JobFlowNode node)` | 节点结束时调用 |

#### 5.2.1 示例代码

```java
import org.frameworkset.tran.jobflow.JobFlowNode;
import org.frameworkset.tran.jobflow.context.JobFlowNodeExecuteContext;
import org.frameworkset.tran.jobflow.listener.JobFlowNodeListener;

public class MyJobFlowNodeListener implements JobFlowNodeListener {

    @Override
    public void beforeExecute(JobFlowNodeExecuteContext context) {
        System.out.println("【节点监听器】节点 [" + context.getNodeId() + "] 即将执行");
    }

    @Override
    public void afterExecute(JobFlowNodeExecuteContext context, Throwable throwable) {
        if (throwable != null) {
            System.err.println("【节点监听器】节点 [" + context.getNodeId() + "] 执行异常：" + throwable.getMessage());
        } else {
            System.out.println("【节点监听器】节点 [" + context.getNodeId() + "] 执行完成");
        }
    }

    @Override
    public void afterEnd(JobFlowNode node) {
        System.out.println("【节点监听器】节点 [" + node.getNodeId() + "] 已结束");
    }
}
```


#### 5.2.2 注册监听器到节点

```java
DatatranJobFlowNodeBuilder jobFlowNodeBuilder = new DatatranJobFlowNodeBuilder("1", "DatatranJobFlowNode")
        .setImportBuilder(buildFile2DB())
        .setNodeTrigger(nodeTrigger)
        .addJobFlowNodeListener(new MyJobFlowNodeListener()); // 添加节点监听器
```

---

### 5.3 流程监控小结

通过 `JobFlowListener` 和 `JobFlowNodeListener`，你可以实现对整个流程及其各个节点的精细化控制与监控。这些监听器非常适合用于以下场景：

- 日志记录与审计
- 异常捕获与报警
- 性能监控与统计
- 流程状态持久化
- 动态调整流程参数

结合 BBoss 的流程编排能力，开发者可以构建高度可观察、可管理的分布式作业流程调度系统。

## 6.自定义节点使用

借助bboss工作流**自定义函数流程任务节点**，可以非常地实现复合各种业务场景的工作流任务,参考文档：

https://esdoc.bbossgroups.com/#/jobflow-customnode

## 7.流程节点间传递和共享参数

在 BBoss 工作流框架中，支持在流程执行过程中动态地**添加、更新和获取上下文参数**。这些参数可以在不同的任务节点之间共享，并且具有明确的生命周期控制（本次流程执行期间有效，执行结束后自动清理），使用参考文档：

https://esdoc.bbossgroups.com/#/jobflow-nodeparam

## 8 为流程节点添加条件触发器

可以为各种类型的流程节点添加条件触发器，控制流程节点是否执行：

- 单任务节点
- 串行节点
- 并行任务节点

支持两种类型的触发器：

- 基于groovy脚本的触发器
- 基于api接口的触发器

### 8.1 定义触发器

#### 8.1.1 脚本触发器定义

脚本触发器在作业第一次初始化时，会被编译转化为一个api接口触发器，避免每次动态解析脚本，提升性能。

```java
NodeTrigger nodeTrigger = new NodeTrigger();
//定义一段条件脚本
String script = new StringBuilder()
        .append("[import]")
        .append("//导入脚本中需要引用的java类\r\n")
        .append(" //import org.frameworkset.tran.jobflow.context.StaticContext; ")
        .append("[/import]")
        .append("StaticContext staticContext = nodeTriggerContext.getPreJobFlowStaticContext();")
        .append("//前序节点执行异常结束，则忽略当前节点执行\r\n")
        .append("if(staticContext != null && staticContext.getExecuteException() != null)")
        .append("    return false;")
        .append("else{")
        .append("    return true;")
        .append("}").toString();
//设置脚本
nodeTrigger.setTriggerScript(script);
```

#### 8.1.2 api接口触发器

```java
//为并行任务节点添加触发器
NodeTrigger parrelnewNodeTrigger = new NodeTrigger();
parrelnewNodeTrigger.setTriggerScriptAPI(new TriggerScriptAPI() {
    @Override
    public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
        if(staticContext != null && staticContext.getExecuteException() != null)
        	return false;
        else
            return true;
        
    }
});
```

### 8.2 将触发器添加到节点

参考一下代码，将条件触发器添加到节点即可：

```java
ParrelJobFlowNodeBuilder parrelJobFlowNodeBuilder = new ParrelJobFlowNodeBuilder("2","ParrelJobFlowNode");
parrelJobFlowNodeBuilder.setNodeTrigger(parrelnewNodeTrigger);
```


## 9. 总结
使用通用工作流框架bboss jobflow，开发者可以快速构建复杂而灵活的数据交换、流批处理以及业务作业流程，适用于：

- 多源异构数据采集任务编排
- 批处理任务编排
- 业务流程自动化编排
- 大模型推理链路编排

bboss jobflow具备良好的扩展性和可维护性，能够满足企业级应用中对流程调度、控制和监控的需求。