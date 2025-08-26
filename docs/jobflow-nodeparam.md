# 工作流节点间共享和传递参数使用文档

在 BBoss 工作流框架中，支持在流程执行过程中动态地**添加、更新和获取上下文参数**。这些参数可以在不同的任务节点之间共享，并且具有明确的生命周期控制（本次流程执行期间有效，执行结束后自动清理）。

## 1. 自定义函数流程任务节点参数使用

本文档介绍如何在自定义函数流程任务节点中实现参数的**设置与获取**，并说明如何在工作流节点、**复合节点（串行/并行）**中实现子节点间的参数共享。

---

### 1.1、参数作用域分类

| 参数类型           | 设置方式                                                     | 获取方式(可以指定默认值)                                     | 生效范围                   | 生命周期         |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | -------------------------- | ---------------- |
| **流程级参数**     | `jobFlowNodeExecuteContext.addJobFlowContextData(key, value)` | `context.getJobFlowContextData(key[,defaultValue])`          | 整个工作流的所有后续节点   | 流程执行结束     |
| **复合节点级参数** | `jobFlowNodeExecuteContext.addContainerJobFlowNodeContextData(key, value)` | `context.getContainerJobFlowNodeContextData(key[,defaultValue])` | 同一复合节点下的所有子节点 | 复合节点执行结束 |
| **当前节点级参数** | `jobFlowNodeExecuteContext.addContextData(key, value)`       | `context.getContextData(key[,defaultValue])`                 | 当前节点及其后续节点       | 节点执行结束     |

---

### 1.2、添加或更新参数示例代码

以下是在 `call()` 方法中添加或更新上下文参数的完整示例：

```java
@Override
public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) throws Exception {
    // 更新或添加流程级参数，向整个流程后续节点传递参数
    jobFlowNodeExecuteContext.getJobFlowExecuteContext().addContextData("flowParam", "paramValue");

    // 如果节点包含在串行或者并行复合节点中，可以向复合节点上下文中添加参数，供其子节点共享
    if (jobFlowNodeExecuteContext.getContainerJobFlowNodeExecuteContext() != null) {
        jobFlowNodeExecuteContext.getContainerJobFlowNodeExecuteContext()
            .addContextData("containerNodeParam", "paramValue");
    }

    // 直接在当前节点上下文中添加参数，仅当前节点及其后续节点可用
    jobFlowNodeExecuteContext.addContextData("nodeParam", "paramValue");

    // 标记节点完成
    jobFlowNode.nodeComplete(null);

    return null;
}
```

---

### 1.3、获取上下文参数示例代码

在后续节点中，可以通过以下方式获取之前设置的参数：

```java
@Override
public Object call(JobFlowNodeExecuteContext context) throws Exception {
    // 获取流程级参数
    Object flowParam = context.getJobFlowContextData("flowParam");
    System.out.println("流程级参数: " + flowParam);
    flowParam = context.getJobFlowContextData("flowParam","defaultValue");//指定默认值

    // 获取复合节点级参数（如果存在）
    JobFlowNodeExecuteContext containerContext = context.getContainerJobFlowNodeExecuteContext();
    if (containerContext != null) {
        Object containerParam = containerContext.getContextData("containerNodeParam");
        System.out.println("复合节点级参数: " + containerParam);
        containerParam = containerContext.getContextData("containerNodeParam","defaultValue");  //指定默认值
    }

    // 获取当前节点级参数
    Object nodeParam = context.getContextData("nodeParam");
    System.out.println("当前节点级参数: " + nodeParam);
    nodeParam = context.getContextData("flowParam","defaultValue");  //指定默认值

    // 标记节点完成
    jobFlowNode.nodeComplete(null);

    return null;
}
```

---

### 1.4、参数生命周期管理

所有通过 `addContextData()` 添加的参数均为**临时参数**，具有以下特点：

- **只在本次流程执行中有效**
- **执行完成后自动清除**
- 不会持久化到磁盘或影响下一次流程执行

---

### 1.5、最佳实践建议

| 场景               | 建议做法                                                     |
| ------------------ | ------------------------------------------------------------ |
| 需要跨节点共享数据 | 使用 `getJobFlowExecuteContext()` 添加流程级参数             |
| 子节点间共享数据   | 使用 `getContainerJobFlowNodeExecuteContext()` 添加复合节点级参数 |
| 只需当前节点使用   | 使用 `addContextData()` 添加节点级参数                       |
| 避免命名冲突       | 使用唯一 key 名，如 `com.example.paramName`                  |
| 日志记录           | 在添加/获取参数时打印日志，便于调试                          |

---

## 2. 数据交换作业中流程上下文参数的添加、更新与获取

本节详细说明如何在以下场景中操作流程上下文参数：

- **数据交换作业任务执行前添加参数**
- **数据交换作业任务执行成功后添加/更新参数**
- **数据交换作业任务执行异常后添加错误信息到上下文**
- **在数据交换作业任务处理过程中获取参数**

---

### 2.1、流程上下文参数作用域说明

| 参数作用域                    | 获取方式                                          | 添加方式              | 生命周期             |
| ----------------------------- | ------------------------------------------------- | --------------------- | -------------------- |
| 流程级上下文                  | `taskContext，context.getJobFlowExecuteContext()` | `addContextData(...)` | 整个流程执行期间     |
| 节点级上下文                  | `taskContext，context.getJobFlowNodeExecuteContext()`                                    | `addContextData(...)` | 当前节点及其后续节点 |
| 复合节点级上下文（串行/并行） | `taskContext，context.getContainerJobFlowNodeExecuteContext()` | `addContextData(...)` | 复合节点及其子节点   |

---

### 2.2、添加或更新流程上下文参数的典型场景

#### 2.2.1. **任务执行前添加参数**

通常用于初始化流程所需的基础参数。

示例代码：

```java
// 在 JobFlowTest.java 中第70-76行示例
importBuilder.addCallInterceptor(new CallInterceptor() {
    @Override
    public void preCall(TaskContext taskContext) {
        // 向流程上下文中添加参数
        taskContext.getJobFlowExecuteContext().addContextData("test", "测试");
    }

    @Override
    public void afterCall(TaskContext taskContext) {
    }

    @Override
    public void throwException(TaskContext taskContext, Throwable e) {
    }
});
```

说明：

- 使用 `preCall()` 方法，在任务开始前插入上下文参数。
- 通过 `taskContext.getJobFlowExecuteContext()` 获取流程上下文。
- 此处设置的 `"test"` 参数在整个流程中都可访问。

---

#### 2.2.2 **任务执行成功后添加/更新参数**

在任务成功执行完成后，可以向上下文中写入结果参数，供后续节点使用。

示例代码：

```java
// 在 JobFlowTest.java 中第81-86行示例
@Override
public Object call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) throws Exception {
    if (this.throwError) {
        jobFlowNode.nodeComplete(new Exception("测试异常"));
    } else {
        // 成功完成时添加参数
        jobFlowNodeExecuteContext.getJobFlowExecuteContext().addContextData("resultParam", "successValue");
        jobFlowNode.nodeComplete(null);
    }
    return null;
}
```

说明：

- 在 `call()` 方法中判断是否执行成功。
- 使用 `getJobFlowExecuteContext().addContextData()` 向流程上下文写入结果参数。
- 可用于记录任务输出、状态等信息。

---

#### 2.2.3. **任务执行异常后添加错误信息到上下文**

当任务发生异常时，可以通过上下文传递错误信息给后续节点进行处理。

示例代码：

```java
// 在 JobFlowTest.java 中第93-97行示例
@Override
public void throwException(TaskContext taskContext, Throwable e) {
    // 向流程上下文中添加错误信息
    taskContext.getJobFlowExecuteContext().addContextData("error", e.getMessage());
}
```

说明：

- 在 `throwException()` 回调中捕获异常。
- 将错误信息存入流程上下文，供后续节点读取并做相应处理。
- 可用于日志记录、失败重试机制等。

---

### 2.3、从流程上下文中获取参数

在流程的任意节点中都可以根据需要读取之前设置的上下文参数。

示例代码：

```java
// 在 JobFlowTest.java 中获取参数示例
Object flowParam = context.getJobFlowExecuteContext().getContextData("flowParam");
System.out.println("流程级参数: " + flowParam);

JobFlowNodeExecuteContext containerContext = context.getContainerJobFlowNodeExecuteContext();
if (containerContext != null) {
    Object containerParam = containerContext.getContextData("containerNodeParam");
    System.out.println("复合节点级参数: " + containerParam);
}

Object nodeParam = context.getContextData("nodeParam");
System.out.println("当前节点级参数: " + nodeParam);
```

说明：

- 使用 `getContextData(key)` 获取指定 key 的值。
- 支持三种作用域：流程级、复合节点级、节点级。
- 如果 key 不存在，返回 `null`。

---

### 2.4、注意事项

| 注意项           | 建议                                                    |
| ---------------- | ------------------------------------------------------- |
| **参数命名**     | 避免重复 key，建议采用命名空间格式如 `module.paramName` |
| **线程安全**     | 流程上下文获取、添加、修改参数是多线程安全的            |
| **生命周期控制** | 所有参数只在本次流程执行期间有效，执行完毕自动清理      |
| **对象类型**     | 可以传递任意对象，但建议使用不可变对象避免副作用        |
| **日志记录**     | 在添加/获取参数时打印日志，便于调试跟踪                 |

---



## 3、总结

BBoss 提供了灵活的上下文参数管理机制，支持在不同粒度上进行参数共享和传递：

- **流程级参数**：适用于工作流所有节点之间全局共享。
- **复合节点级参数**：适用于串行/并行子节点之间的数据共享。
- **节点级参数**：适用于当前节点内部使用。
- **多线程安全：**流程上下文获取、添加、修改参数是多线程安全的。

通过合理使用这些功能，你可以轻松构建复杂的工作流逻辑，并确保节点之间高效、安全地进行数据交互。