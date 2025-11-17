# 自定义函数流程任务节点使用介绍

借助bboss工作流**自定义函数流程任务节点**，可以非常方便地实现满足各种业务场景的工作流任务。要实现一个**自定义函数流程任务节点**，关键在于理解并正确实现 `JobFlowNodeFunction` 接口中的 `call()` 方法，这个方法是任务节点执行逻辑的核心部分。

---

## ✅ 一、整体步骤概览

1. 实现 `JobFlowNodeFunction` 接口
2. 重点编写 `call(JobFlowNodeExecuteContext context)` 方法
3. 将该实现类用于构建任务节点
4. 添加到工作流中执行

## 📌 二、接口与核心方法说明

`JobFlowNodeFunction` 接口定义如下：

```java
public interface JobFlowNodeFunction {
    void init(JobFlowNode jobFlowNode);
    Object call(JobFlowNodeExecuteContext context) throws Exception;
    void reset();
    void release();
    void stop();
}
```


其中，`call()` 是实际执行任务逻辑的方法。

要实现一个自定义函数流程任务节点，可以按照以下步骤进行：

---

## **📌 三. 实现 `JobFlowNodeFunction` 接口**

该接口是构建自定义任务节点的核心。你需要实现其中的五个关键方法：init, `call`, reset, `release`, 和 stop。

### ✅ 3.1 实现代码
```java
package org.example.customjob;

import org.frameworkset.tran.jobflow.JobFlowNode;
import org.frameworkset.tran.jobflow.context.JobFlowNodeExecuteContext;

public class CustomJobFunction implements JobFlowNodeFunction {

    private JobFlowNode jobFlowNode;

    @Override
    public void init(JobFlowNode jobFlowNode) {
        this.jobFlowNode = jobFlowNode;
    }

    @Override
    public Object call(JobFlowNodeExecuteContext context) throws Exception {
        Throwable exception = null;
        try {
            //获取流程上下文参数functionParam的值，参数有效期为本次执行过程中有效，执行完毕后直接被清理
            Object flowParam = jobFlowNodeExecuteContext.getJobFlowExecuteContext().getContextData("flowParam");
            //如果节点包含在串行或者并行复合节点中，可以获取串行或者并行复合节点上下文中的参数，参数有效期为本次执行过程中有效，执行完毕后直接被清理
            if(jobFlowNodeExecuteContext.getContainerJobFlowNodeExecuteContext() != null) {
                Object containerNodeParam = jobFlowNodeExecuteContext.getContainerJobFlowNodeExecuteContext().getContextData("containerNodeParam");
            }
            //直接获取节点执行上下文中添加参数，参数有效期为本次执行过程中有效，执行完毕后直接被清理
            Object nodeParam =  jobFlowNodeExecuteContext.getContextData("nodeParam");
            //此处编写业务逻辑
            //.......
            if(this.throwError) {
                //模拟异常发生
                exception = new Exception("测试异常");
            }
            //更新或添加流程上下文参数functionParam的值，向流程流程后续节点传递参数，参数有效期为本次执行过程中有效，执行完毕后直接被清理
            jobFlowNodeExecuteContext.getJobFlowExecuteContext().addContextData("flowParam","paramValue");
            //如果节点包含在串行或者并行复合节点中，可以向串行或者并行复合节点上下文中添加参数，以便在复合节点的子节点间共享参数，参数有效期为本次执行过程中有效，执行完毕后直接被清理
            if(jobFlowNodeExecuteContext.getContainerJobFlowNodeExecuteContext() != null) {
                jobFlowNodeExecuteContext.getContainerJobFlowNodeExecuteContext().addContextData("containerNodeParam", "paramValue");
            }
            //直接在节点执行上下文中添加参数，参数有效期为本次执行过程中有效，执行完毕后直接被清理
            jobFlowNodeExecuteContext.addContextData("nodeParam", "paramValue");
            
            

        }        
        finally {
            //方法执行完毕后，务必调用jobFlowNode.nodeComplete方法，如果方法执行过程中产生异常，则作为参数传递给complete方法
            jobFlowNode.nodeComplete(exception);
        }
        //此处可以返回一个值，目前无用
        return null;
    }

    @Override
    public void reset() {
        // 可用于重置内部状态或计数器等
        System.out.println("重置任务状态");
    }

    @Override
    public void release() {
        // 在此释放资源，如关闭数据库连接、流等
        System.out.println("释放任务资源");
    }

    @Override
    public void stop() {
        // 停止任务执行
        System.out.println("任务已停止");
    }
}
```

---

### ✅ 3.2 **call 方法实现要点总结**

call方法实现具体任务业务逻辑， `call()` 方法的核心部分，通常包括以下内容：

- 数据转换或计算
- 调用外部系统（数据库、API）
- 文件读写操作
- 定时或异步任务

| 要点               | 描述                                                         |
| ------------------ | ------------------------------------------------------------ |
| **获取上下文数据** | 使用 `context.getContextData("key")` 获取全局变量或配置信息  |
| **执行核心逻辑**   | 在 `call()` 方法中编写实际的业务处理代码                     |
| **标记任务完成**   | 必须调用 `jobFlowNode.nodeComplete(Throwable)` 来通知框架任务已完成 |
| **异常处理**       | 如果发生错误，抛出异常或将异常传递给 `nodeComplete(Throwable)` |
| **资源管理**       | 在 `release()` 中释放资源，在reset方法中重置节点状态，在 `stop()` 中处理中断逻辑 |
| **线程安全**       | 如果任务可能并发执行，确保内部状态是线程安全的               |

---

### ✅ 3.3 **标记任务完成**

call方法处理完业务逻辑后，务必调用jobFlowNode.nodeComplete方法，每个节点必须在执行完毕后调用 `nodeComplete(Throwable)` 方法通知框架当前节点已完成：

- 正常完成：

  ```java
  jobFlowNode.nodeComplete(null);
  ```


- 异常完成：

  ```java
  jobFlowNode.nodeComplete(new RuntimeException("处理失败"));
  ```

⚠️ **注意**：如果不调用此方法，流程会卡住，无法继续往下执行！

### ✅ 3.4 资源释放和中断处理

需要确保资源释放和中断处理：

- 流程在执行call方法之前会检查是否已经中断执行，但是call方法中如果有长时间运行的操作，还是需要定期检查是否被中断：

  一种方式是自己维护stopped标记

  ```java
  private volatile boolean stopped = false;
  
  @Override
  public void stop() {
      this.stopped = true;
  }
  
  @Override
  public Object call(JobFlowNodeExecuteContext context) throws Exception {
      if (stopped) {
          return null;
      }
  
      // ... 执行逻辑 ...
  }
  ```

​     另外一种方式是调用以下方法判断作业是否已经停止或者正在停止中：

```java
jobFlowNodeExecuteContext.assertStopped().isTrue()
```


- 在 `release()` 中关闭连接、释放资源。
- 在reset重置函数中修改的节点执行状态

## 📌 四. 使用 `CommonJobFlowNodeBuilder` 创建任务节点

使用 `CommonJobFlowNodeBuilder` 将你实现的 `JobFlowNodeFunction` 包装成一个任务节点，并设置其 ID 和名称。

### ✅ 4.1 示例代码：
```java
// 创建触发器（可选）
NodeTrigger nodeTrigger = new NodeTrigger();
String script = "return true;"; // 简单的触发脚本，表示始终触发该节点
nodeTrigger.setTriggerScript(script);

// 创建任务节点构建器
CommonJobFlowNodeBuilder jobNodeBuilder = new CommonJobFlowNodeBuilder(
    "custom_node_001", 
    "自定义任务节点",
    new CustomJobFunction()
);
jobNodeBuilder.setNodeTrigger(nodeTrigger); // 设置触发器（如果不需要触发器，可以省略这一步）
```

---

## **📌 五. 将任务节点添加到工作流中**

通过 `JobFlowBuilder` 构建整个工作流，并将你的自定义任务节点加入其中。

### ✅ 5.1 示例代码：
```java
// 创建工作流构建器
JobFlowBuilder jobFlowBuilder = new JobFlowBuilder();
jobFlowBuilder.setJobFlowName("自定义任务工作流")
             .setJobFlowId("custom_workflow");

// 添加任务节点到工作流
jobFlowBuilder.addJobFlowNodeBuilder(jobNodeBuilder);

// 构建并启动工作流
JobFlow jobFlow = jobFlowBuilder.build();
jobFlow.start();  // 启动工作流
```

---

## **📌 六. 完整调用示例**

你可以将以上代码整合到一个主类中运行完整的任务流程：

```java
public class JobFlowMain {
    public static void main(String[] args) {
        // 创建工作流构建器
        JobFlowBuilder jobFlowBuilder = new JobFlowBuilder();
        jobFlowBuilder.setJobFlowName("自定义任务工作流")
                     .setJobFlowId("custom_workflow");

        // 创建任务节点构建器
        CommonJobFlowNodeBuilder jobNodeBuilder = new CommonJobFlowNodeBuilder(
            "custom_node_001", 
            "自定义任务节点",
            new CustomJobFunction()
        );

        // 添加任务节点到工作流
        jobFlowBuilder.addJobFlowNodeBuilder(jobNodeBuilder);

        // 构建并启动工作流
        JobFlow jobFlow = jobFlowBuilder.build();
        jobFlow.start();  // 启动工作流
    }
}
```
---

## 📌 七. **注意事项**

- **日志记录**：在关键操作处添加日志输出，便于调试和监控。
- **性能优化**：对于高频或大数据量任务，考虑使用批处理、缓存等方式提升效率。
- **上下文传递**：可以通过 `context.addContextData(key, value)` 在不同节点之间共享数据。

---

通过上述步骤，你可以轻松实现一个功能完整、结构清晰的自定义函数流程任务节点，适用于复杂的数据处理场景。


