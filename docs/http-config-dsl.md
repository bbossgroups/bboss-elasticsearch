# 配置化DSL微服务调用框架使用手册

bboss 配置化DSL微服务调用框架基于 [bboss http/http5 负载均衡器](https://esdoc.bbossgroups.com/#/httpproxy) 构建，通过外部XML配置文件管理HTTP请求报文模板（DSL），实现业务参数与请求报文的解耦。框架支持动态模板渲染、多集群负载均衡、服务健康检查、自动容灾恢复等特性，可广泛应用于微服务RPC调用、RESTful API对接、大模型服务交互等场景。

## 1. 框架概述

配置化DSL微服务调用框架的核心价值：

- **报文模板化**：将HTTP请求报文（JSON/XML等）抽取到独立的DSL配置文件中，便于统一维护与热更新
- **参数动态注入**：通过 `#[变量名]` 语法将运行时参数注入DSL模板，无需拼接字符串
- **多集群支持**：可指定不同的HTTP连接池（集群）执行请求，天然支持负载均衡与容灾
- **类型安全**：支持将响应结果直接映射为Java对象、List、Set、Map等多种类型
- **与bboss生态无缝集成**：可复用bboss http/http5的连接池、认证、服务发现、流式调用等全部能力

## 2. 核心组件

| 组件 | 类路径 | 说明 |
|------|--------|------|
| 工厂类 | `org.frameworkset.spi.remote.http.ConfigHttpRequestProxyHelper` | 用于创建和管理 `ConfigHttpRequestProxy` 实例 |
| 代理类 | `org.frameworkset.spi.remote.http.ConfigHttpRequestProxy` | 提供基于DSL模板的HTTP请求发送能力 |
| DSL工具 | `org.frameworkset.spi.remote.http.ConfigHolder` / `ConfigDSLUtil` | 负责DSL配置文件的加载、缓存与模板渲染 |

## 3. 导入依赖

### Maven

```xml
<dependency>
   <groupId>com.bbossgroups</groupId>
   <artifactId>bboss-http</artifactId>
   <version>6.5.3</version>
</dependency>
```

> 如需使用HTTP/2、流式AI对话等能力，建议导入 [bboss-http5](https://esdoc.bbossgroups.com/#/httpproxy5)：
> ```xml
> <dependency>
>    <groupId>com.bbossgroups</groupId>
>    <artifactId>bboss-http5</artifactId>
>    <version>6.5.3</version>
> </dependency>
> ```

### Gradle

```groovy
api 'com.bbossgroups:bboss-http:6.5.3'
```

## 4. DSL配置文件规范

DSL配置文件采用XML格式，默认存放在项目的 `resources` 目录下（也可放在其他classpath路径）。

### 4.1 文件格式

```xml
<?xml version="1.0" encoding='UTF-8'?>
<properties>
    <description>
        <![CDATA[
            配置数据导入的http服务queryDsl
        ]]>
    </description>

    <!-- 定义一个名为 requestBody 的DSL模板 -->
    <property name="requestBody">
        <![CDATA[
        {
            "automatic_fields": false,
            "field_names": [
                "日志ID","用户ID","发送状态","发送内容","发送时间"
            ],
            "filter": {
                "conjunction": "and",
                "conditions": [
                    {
                        "field_name": "发送时间戳",
                        "operator": "isGreater",
                        "value": [
                            "#[发送时间戳]"
                        ]
                    },
                    {
                        "field_name": "发送时间戳",
                        "operator": "isLessEqual",
                        "value": [
                            "#[发送时间戳__endTime]"
                        ]
                    }
                ]
            },
            "sort": [],
            "view_id": "vewFoeaJxt"
        }
        ]]>
    </property>

    <!-- 可定义多个DSL模板 -->
    <property name="requestBodyPerson">
        <![CDATA[
        {
            "automatic_fields": false,
            "field_names": [],
            "filter": {
                "conjunction": "and",
                "conditions": []
            },
            "sort": [],
            "view_id": "vewfx91OoV"
        }
        ]]>
    </property>
</properties>
```

### 4.2 变量语法

在DSL模板中，使用 `#[变量名]` 占位符声明需要运行时注入的参数：

| 语法 | 说明 | 示例 |
|------|------|------|
| `#[name]` | 注入基本类型或字符串变量 | `"value": "#[发送时间戳]"` |
| `#[name__endTime]` | 常用于时间范围截止条件 | `"#[logTime__endTime]"` |

运行时传入的 `params` 中需包含同名key，框架会自动完成值替换。若变量值为 `null`，则替换为空字符串。

更加详细的变量语法和dsl动态配置，参考文档：https://esdoc.bbossgroups.com/#/development?id=_53-dsl%e9%85%8d%e7%bd%ae%e8%a7%84%e8%8c%83

### 4.3 配置文件热加载

DSL配置文件支持热加载，修改后无需重启应用即可生效。可通过以下方式配置扫描间隔（在HTTP主配置文件中）：

```properties
# dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
http.dslfile.refreshInterval=5000
```

Spring Boot项目：
```properties
spring.bboss.http.dslfile.refreshInterval=5000
```

## 5. HTTP连接池初始化

在使用 `ConfigHttpRequestProxy` 之前，必须先初始化bboss HTTP连接池。

### 5.1 配置文件方式

```java
// 加载classpath下的application.properties，启动负载均衡器
HttpRequestProxy.startHttpPools("application.properties");
```

配置示例（`application.properties`）：

```properties
http.poolNames = default,feishu

## default 连接池配置
http.maxTotal = 200
http.defaultMaxPerRoute = 200
http.hosts = 192.168.137.1:8080
http.health = /health
http.healthCheckInterval = 3000

## feishu 连接池配置
feishu.http.hosts = https://open.feishu.cn
feishu.http.maxTotal = 100
feishu.http.defaultMaxPerRoute = 100
feishu.http.authorTokenFunction = org.frameworkset.spi.feishu.FeishuAuthorTokenFunction
feishu.http.authorTokenExpiredTime = 1500000
feishu.http.extendConfigs.appId = cli_xxx
feishu.http.extendConfigs.appSecret = xxx
```

### 5.2 代码方式（Map配置）

```java
Map<String, Object> configs = new HashMap<>();
configs.put("http.poolNames", "feishu");
configs.put("feishu.http.hosts", "https://open.feishu.cn");
configs.put("feishu.http.maxTotal", 100);
configs.put("feishu.http.defaultMaxPerRoute", 100);
configs.put("feishu.http.authorTokenFunction", "org.frameworkset.spi.feishu.FeishuAuthorTokenFunction");
configs.put("feishu.http.authorTokenExpiredTime", 25 * 60 * 1000L);
configs.put("feishu.http.showDsl", false);

HttpRequestProxy.startHttpPools(configs);
```

更多配置方式（Apollo、Nacos、Spring Boot等）请参考：[bboss http负载均衡器使用指南](https://esdoc.bbossgroups.com/#/httpproxy)。

## 6. ConfigHttpRequestProxy 初始化与使用

### 6.1 获取代理实例

通过 `ConfigHttpRequestProxyHelper` 工厂类获取代理实例，实例会被缓存，相同参数多次获取为同一实例：

```java
// 使用默认连接池（default）
ConfigHttpRequestProxy client = ConfigHttpRequestProxyHelper
        .getHttpConfigClientProxy("feishudsl.xml");

// 指定连接池名称（推荐用于多集群场景）
ConfigHttpRequestProxy client = ConfigHttpRequestProxyHelper
        .getHttpConfigClientProxy("feishu", "feishudsl.xml");
```

| 方法签名 | 说明 |
|----------|------|
| `getHttpConfigClientProxy(String configDSLFile)` | 使用默认连接池 `default` |
| `getHttpConfigClientProxy(String poolName, String configDSLFile)` | 指定连接池名称和DSL文件 |
| `getHttpConfigClientProxy(BaseTemplateContainerImpl templateContainer)` | 基于编程式模板容器（高级） |
| `getHttpConfigClientProxy(String poolName, BaseTemplateContainerImpl templateContainer)` | 指定连接池+模板容器（高级） |

### 6.2 基础调用示例

以下案例演示调用飞书API查询多维表格数据，完整代码参考：
`bboss-elasticsearch-rest/src/test/java/org/frameworkset/spi/remote/http/ConfigHttpRequestProxyHelperTest.java`

```java
public class ConfigHttpRequestProxyHelperTest {
    private static Logger logger = LoggerFactory.getLogger(ConfigHttpRequestProxyHelperTest.class);

    public static void initFeishu() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("http.poolNames", "feishu");
        configs.put("feishu.http.hosts", "https://open.feishu.cn");
        configs.put("feishu.http.maxTotal", 100);
        configs.put("feishu.http.defaultMaxPerRoute", 100);
        configs.put("feishu.http.authorTokenFunction", "org.frameworkset.spi.feishu.FeishuAuthorTokenFunction");
        configs.put("feishu.http.authorTokenExpiredTime", 25 * 60 * 1000L);
        configs.put("feishu.http.showDsl", false);
        HttpRequestProxy.startHttpPools(configs);
    }

    public static void main(String[] args) {
        initFeishu();

        // 获取DSL代理：指定连接池feishu，DSL文件feishudsl.xml
        ConfigHttpRequestProxy client = ConfigHttpRequestProxyHelper
                .getHttpConfigClientProxy("feishu", "feishudsl.xml");

        // 构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("发送时间戳", 0);
        params.put("发送时间戳__endTime", 2222222222222222222L);

        // 调用API：url、DSL名称、参数
        String url = "/open-apis/bitable/v1/apps/xxx/tables/xxx/records/search";
        String data = client.sendJsonBody(url, "requestBody", params);
        logger.info(data);
    }
}
```

对应的DSL配置文件 `feishudsl.xml`（片段）：

```xml
<property name="requestBody">
    <![CDATA[
    {
        "automatic_fields": false,
        "field_names": [
            "日志ID","用户ID","卡片消息类型","发送状态","发送内容","返回结果","分析结果","建议","发送时间","发送时间戳"
        ],
        "filter": {
            "conjunction": "and",
            "conditions": [
                {
                    "field_name": "发送时间戳",
                    "operator": "isGreater",
                    "value": [ "#[发送时间戳]" ]
                },
                {
                    "field_name": "发送时间戳",
                    "operator": "isLessEqual",
                    "value": [ "#[发送时间戳__endTime]" ]
                }
            ]
        },
        "sort": [],
        "view_id": "vewFoeaJxt"
    }
    ]]>
</property>
```

## 7. API方法详解

`ConfigHttpRequestProxy` 提供了丰富的HTTP请求方法，所有方法均遵循以下执行流程：

1. 根据 `queryDslName` 从DSL配置文件中读取模板
2. 使用运行时 `params` 渲染模板，替换 `#[变量]` 占位符
3. 如果连接池配置了 `showDsl=true`，则将渲染后的报文打印到日志
4. 通过底层 `HttpRequestProxy` 发送HTTP请求，并返回结果

### 7.1 发送JSON Body请求

**返回字符串：**

```java
// 使用默认连接池
String data = client.sendJsonBody(url, "requestBody", params);

// 指定连接池
String data = client.sendJsonBody("feishu", url, "requestBody", params);
```

**返回指定类型对象：**

```java
// 返回Map类型
Map result = client.sendJsonBody(url, "requestBody", params, Map.class);

// 指定连接池+返回类型
Map result = client.sendJsonBody("feishu", url, "requestBody", params, Map.class);
```

**带请求头：**

```java
Map<String, String> headers = new HashMap<>();
headers.put("Content-Type", "application/json; charset=utf-8");
headers.put("X-Custom-Header", "value");

Map result = client.sendJsonBody("feishu", url, "requestBody", params, headers, Map.class);
String data  = client.sendJsonBody("feishu", url, "requestBody", params, headers);
```

### 7.2 返回集合类型

**返回List：**

```java
List<Map> list = client.sendJsonBodyForList(url, "requestBody", params, Map.class);
List<Map> list = client.sendJsonBodyForList("feishu", url, "requestBody", params, Map.class);
List<Map> list = client.sendJsonBodyForList("feishu", url, "requestBody", params, headers, Map.class);
```

**返回Set：**

```java
Set<String> set = client.sendJsonBodyForSet(url, "requestBody", params, String.class);
Set<String> set = client.sendJsonBodyForSet("feishu", url, "requestBody", params, String.class);
```

**返回Map（键值对）：**

```java
Map<String, Map> map = client.sendJsonBodyForMap(
    url, "requestBody", params, String.class, Map.class);

Map<String, Map> map = client.sendJsonBodyForMap(
    "feishu", url, "requestBody", params, String.class, Map.class);
```

### 7.3 返回包装类型对象

当接口返回的数据结构为 `{ "code": 0, "data": { ... } }` 这类包装对象时，可使用：

```java
// containerType: 外层包装类，resultType: 内部数据类
WrapperData<Map> result = client.sendJsonBodyTypeObject(
    url, "requestBody", params, WrapperData.class, Map.class);

WrapperData<Map> result = client.sendJsonBodyTypeObject(
    "feishu", url, "requestBody", params, WrapperData.class, Map.class);
```

### 7.4 自定义响应处理器

如需对HTTP响应进行自定义解析（如解密、签名校验、特殊格式转换），可使用 `HttpClientResponseHandler`：

```java
String data = client.sendJsonBody("feishu", url, "requestBody", params,
    new HttpClientResponseHandler<String>() {
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            // 自定义解析逻辑
            return EntityUtils.toString(response.getEntity());
        }
    });
```

### 7.5 使用 InvokeContext

`InvokeContext` 可在单次请求中动态覆盖部分连接池配置（如超时时间、路由规则等）：

```java
InvokeContext invokeContext = new InvokeContext();
invokeContext.setTimeoutSocket(10000);

Map result = client.sendJsonBody(url, "requestBody", params, invokeContext, Map.class);
```

## 8. 进阶用法

### 8.1 多集群调用

在多微服务架构中，可能需要同时对接多个不同的服务集群：

```java
// 初始化两个连接池
Map<String, Object> configs = new HashMap<>();
configs.put("http.poolNames", "order,user");
configs.put("order.http.hosts", "http://order-service:8080");
configs.put("user.http.hosts", "http://user-service:8080");
HttpRequestProxy.startHttpPools(configs);

// 获取不同集群的DSL代理
ConfigHttpRequestProxy orderClient = ConfigHttpRequestProxyHelper
        .getHttpConfigClientProxy("order", "order-dsl.xml");
ConfigHttpRequestProxy userClient = ConfigHttpRequestProxyHelper
        .getHttpConfigClientProxy("user", "user-dsl.xml");

// 分别调用
String orderResult = orderClient.sendJsonBody("/api/orders", "queryOrder", params);
String userResult = userClient.sendJsonBody("/api/users", "queryUser", params);
```

### 8.2 直接使用 ClientConfiguration

除了通过连接池名称，还可以直接传入 `ClientConfiguration` 对象：

```java
ClientConfiguration clientConfiguration = ClientConfiguration.getClientConfiguration("feishu");
Map result = client.sendJsonBody(clientConfiguration, url, "requestBody", params, Map.class);
```

### 8.3 与流式AI服务结合

> 实现中

### 8.4 资源释放

应用退出时，建议释放DSL配置缓存：

```java
// 清除所有ConfigHttpRequestProxy实例及DSL缓存
ConfigHttpRequestProxyHelper.destroy();
```

## 9. Spring Boot 集成

### 9.1 添加依赖

```xml
<dependency>
   <groupId>com.bbossgroups</groupId>
   <artifactId>bboss-spring-boot-starter</artifactId>
   <version>6.5.3</version>
</dependency>
```

> Spring Boot 3.x 使用 `bboss-spring-boot3-starter`

### 9.2 配置文件

```properties
spring.bboss.http.name=default
spring.bboss.http.maxTotal=200
spring.bboss.http.defaultMaxPerRoute=200
spring.bboss.http.hosts=http://localhost:8080
spring.bboss.http.health=/health
spring.bboss.http.healthCheckInterval=3000
```

### 9.3 代码中使用

```java
@Service
public class OrderService {

    @Autowired
    private BBossStarter bbossStarter;

    public void queryOrder() {
        ConfigHttpRequestProxy client = ConfigHttpRequestProxyHelper
                .getHttpConfigClientProxy("default", "order-dsl.xml");

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", "123456");

        String result = client.sendJsonBody("/api/order/detail", "queryOrder", params);
        System.out.println(result);
    }
}
```

## 10. 常见问题

### Q1: DSL变量注入后格式不对（如字符串缺少引号）？

A: `#[变量]` 是纯文本替换，若目标JSON字段需要字符串，请在DSL模板中自行添加双引号：

```json
"field": "#[value]"
```

若变量本身可能包含需要转义的特殊字符，建议通过Java代码预处理，或使用bboss提供的高级模板语法。

### Q2: 如何查看最终渲染后的请求报文？

A: 将对应连接池的 `showDsl` 设为 `true`：

```properties
http.showDsl=true
```

或在Map配置中：

```java
configs.put("feishu.http.showDsl", true);
```

渲染后的报文将以INFO级别输出到日志。

### Q3: DSL配置文件修改后如何热加载？

A: 确保配置了 `dslfile.refreshInterval`（默认5000毫秒），框架会定时扫描文件变更并自动重新加载。若未生效，检查配置前缀是否正确，以及文件是否位于classpath下可被扫描到。

### Q4: 连接池未初始化就调用会报什么错？

A: 会抛出异常提示对应的 `ClientConfiguration` 不存在。请确保先调用 `HttpRequestProxy.startHttpPools(...)` 完成连接池初始化，再使用 `ConfigHttpRequestProxyHelper` 获取代理实例。

## 11. 完整案例参考

| 案例 | 路径 | 说明 |
|------|------|------|
| 飞书API DSL调用 | `bboss-elasticsearch-rest/src/test/java/org/frameworkset/spi/remote/http/ConfigHttpRequestProxyHelperTest.java` | 演示Map方式初始化连接池、DSL文件定义、参数注入与API调用 |
| 飞书DSL配置 | `bboss-elasticsearch-rest/src/test/resources/feishudsl.xml` | 包含多个查询模板的DSL配置文件 |
| HTTP数据采集 | [datatran-http.md](https://esdoc.bbossgroups.com/#/datatran-http) | 在数据同步场景中通过DSL从HTTP服务获取数据 |

## 12. 开发交流

- **QQ交流群**：21220580, 166471282, 3625720, 154752521, 166471103, 166470856
- **源码地址**：https://gitee.com/bboss/bboss-elastic
- **文档地址**：https://esdoc.bbossgroups.com
