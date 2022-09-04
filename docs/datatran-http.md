# Http/Https插件使用指南

本文中涉及的Http/Https输入输出插件案例工程下载地址：

https://gitee.com/bboss/bboss-datatran-demo

输入插件案例：

[Http2ESFullQueryDslDemo](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/Http2ESFullQueryDslDemo.java)

输出插件案例：

1. [ES2HttpDemo](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/ES2HttpDemo.java) 从Elasticsearch采集数据直接推送http服务
2. [LocalLog2FullfeatureHttpDslDemo](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/LocalLog2FullfeatureHttpDslDemo.java)  从本地日志文件采集数据基于dsl脚本推送http服务
3. [SFtpLog2FullfeatureHttpDslDemo](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2FullfeatureHttpDslDemo.java)  从ftp服务器采集日志文件基于dsl脚本推送http服务
4. [SFtpLog2HttpDemo](https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2HttpDemo.java) 从ftp服务器采集日志文件直接推送http服务
5. [其他案例](https://gitee.com/bboss/db-elasticsearch-tool/tree/master/src/main/java/org/frameworkset/elasticsearch/imp/http)

通过bboss http输入/输出插件，可以从http服务采集数据，也可以从其他数据源采集的数据推送给http服务，插件特性如下：

1. 支持增量、全量数据采集同步，
2. 支持分页模式采集数据
3. 支持http服务高可用负载及容错机制，可以配置服务健康检查机制
4. 支持post和put两种http method
5. 支持添加静态值的http head和动态值的http head
6. http输入插件，采用类似于Elasticsearch rest服务的dsl查询脚本语言，来传递http数据查询服务所需的参数、增量条件、分页条件
7. http输出插件，可以直接推送数据集合，亦可以采用基于dsl脚本语言动态组装数据后再推送到服务端
8. http输入插件：支持为dsl脚本语言设置静态值输入参数和动态值输入参数
9. http输出插件：支持为dsl脚本语言设置静态值输出参数和动态值输出参数
10. http服务安全：支持http服务 basic认证以及基于jwt  token安全认证，通过动态header实现jwt token认证功能、可以基于http服务组件直接实现[basic认证](https://esdoc.bbossgroups.com/#/httpproxy?id=_8%e5%ae%89%e5%85%a8%e8%ae%a4%e8%af%81)以及[设置ssl证书](https://esdoc.bbossgroups.com/#/development?id=_265-https%e5%8d%8f%e8%ae%ae%e9%85%8d%e7%bd%ae)。
11. 支持对发送数据签名以及接收数据签名解析


bboss 输入/输出插件涉及三个作业配置组件

1. ImportBuilder：数据同步作业构建器，用来进行作业基本配置，包括设置InputConfig、OutputConfig、数据转换处理配置、增量状态管理配置、定时器配置、任务监控配置、任务拦截器配置、并行处理线程池和队列配置、容错配置、提取数据条件配置等

2. HttpInputConfig：http输入插件配置组件，包括http服务地址（多个用逗号分隔）、http连接池配置、http method配置、ssl证书配置、http链接池名称配置、提取数据dsl脚本设置等

3. HttpOutputConfig：http输出插件配置组件，包括http服务地址（多个用逗号分隔）、http连接池配置、http method配置、ssl证书配置、http链接池名称配置、提取数据dsl脚本设置等

http数据同步作业包含作业配置态和作业运行态，具体看如下示意图

![](images\datasyn-inout.png)
**配置态：**指通过作业构建器ImportBuilder配置http数据采集作业的基础配置、输入和输出配置

**运行态：**通过ImportBuilder构建一个Datatream，加载作业配置，启动作业并执行数据采集、数据处理、数据输出，从而实现整个数据采集同步功能。

下面具体介绍http插件使用方法

# 1.作业基础配置

```java
创建一个作业构建器
ImportBuilder importBuilder = new ImportBuilder() ;
```
可以通过importBuilder设置作业基础参数，输入插件配置、输出插件配置，具体的基础参数说明，参考文档：[作业基础配置](https://esdoc.bbossgroups.com/#/db-es-tool?id=%E4%BD%9C%E4%B8%9A%E5%9F%BA%E7%A1%80%E9%85%8D%E7%BD%AE)


# 2.http输入插件

http输入插件采用类似于Elasticsearch rest服务的dsl查询脚本语言，来传递http数据查询所需的参数、增量条件、分页条件;插件可以接收以下两种数据格式，参考后面的案例介绍：

- 基本集合结构List\\<Map\\>
- 包含List\\<Map\\>数据的复杂结构（需要通过HttpResultParser接口提取数据）

接收的数据如果经过数据加密或者数据签名，亦可以通过HttpResultParser进行解密或者签名校验。



插件初始化：

```java
//创建输入插件Config实例
HttpInputConfig httpInputConfig = new HttpInputConfig();
importBuilder.setInputConfig(httpInputConfig);
```

插件属性说明如下

| 属性名称              | 类型    | 说明                                                         |
| --------------------- | ------- | ------------------------------------------------------------ |
| sourceHttpPool        | String  | 源http连接池服务组名称                                       |
| addHttpInputConfig    | 方法    | 添加http服务参数、服务地址、监控检查机制,例如: httpInputConfig.setQueryUrl("/httpservice/getData.api") .addSourceHttpPoolName("http.poolNames","datatran") .addHttpInputConfig("datatran.http.health","/health") .addHttpInputConfig("datatran.http.hosts","192.168.137.1:808") .addHttpInputConfig("datatran.http.timeoutConnection","5000") .addHttpInputConfig("datatran.http.timeoutSocket","50000") .addHttpInputConfig("datatran.http.connectionRequestTimeout","50000") .addHttpInputConfig("datatran.http.maxTotal","200") .addHttpInputConfig("datatran.http.defaultMaxPerRoute","100") .addHttpInputConfig("datatran.http.failAllContinue","true");                                参考文档：https://esdoc.bbossgroups.com/#/httpproxy |
| addSourceHttpPoolName | 方法    | 添加http服务组属性参数：httpInputConfig.addSourceHttpPoolName("http.poolNames","datatran") |
| addHttpHeader | 方法    | 添加http头属性 |
| addHttpHeaders | 方法    | 批量添加http头属性，可用于设置基于jwt等认证机制的头部token |
| addDynamicHeader | 方法 | 添加动态http头属性，可用于设置基于jwt等认证机制的头部token（具备生命周期，失效后重新申请） |
| showDsl               | boolean | 控制作业执行时，是否打印查询的dsl脚本，true 打印，false 不打印，默认值false，控制是否将dsl打印到日志文件或者控制台，要求log级别为info级别 |
| QueryUrl              | String  | 获取数据的http服务地址，相对路径，对应的服务器对应清单有属性datatran.http.hosts指定，多个地址逗号分隔，示例：httpInputConfig.setQueryUrl("/httpservice/getData.api") |
| dslFile               | String  | querydsl脚本配置文件路径，在classes路径下                    |
| queryDslName          | String  | querydsl脚本名称，脚本配置规范，可以参考文档：https://esdoc.bbossgroups.com/#/development  章节【[5.3 dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl配置规范)】 |
| queryDsl              | String  | 直接设置queryDsl脚本，脚本配置规范，可以参考文档：https://esdoc.bbossgroups.com/#/development  章节【[5.3 dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl配置规范)】 |
| httpMethod            | String  | http请求method，支持两种：put，post                          |
| pageSize              | int     | 无需显示指定，按批获取数据记录数大小，通过importBuilder.setFetchSize(5000)设置 |
| pagine                | boolean | 分页查询控制变量，false 不分页，true 分页，默认值false，     |
| pagineFromKey        | String     | 设置分页查询起始位置key名称，默认值httpPagineFrom，其值保存了分页起始位置，在查询dsl中使用，pagineFrom默认从0开始，如果服务支持分页获取增量或者全量数据，设置分页起始位置,httpInputConfig.setPagineFromKey("httpPagineFrom") |
| pagineSizeKey        | String     | 设置分页查询每页记录数key名称，默认值httpPagineSize，其值保存了分页记录数，在查询dsl中使用，如果服务支持分页获取增量或者全量数据，设置每页记录数，如果实际返回的记录数小于httpPagineSize或者为0，则表示本次分页获取数据结束，对应参数fetchSize配置的值,httpInputConfig.setPagineFromKey("httpPagineSize") |
| httpResultParser        | HttpResultParser     | 接口类型，用来自定义解析返回报文 |

带分页的querydsl脚本案例：

```xml
<property name="queryPagineDsl">
        <![CDATA[
{
    "logTime":#[logTime],## 传递增量时间起始条件
    "logTimeEndTime":#[logTime__endTime],## 传递增量时间截止时间条件，必须指定IncreamentEndOffset偏移时间量才能设置增量截止时间
    "from":#[httpPagineFrom], ## 如果服务支持分页获取增量或者全量数据，设置分页起始位置
    "size":#[httpPagineSize],  ## 如果服务支持分页获取增量或者全量数据，设置每页记录数，如果实际返回的记录数小于httpPagineSize或者为0，则表示本次分页获取数据结束，对应参数fetchSize配置的值
    "otherParam": #[otherParam] ## 其他服务参数
}
              ]]></property>
```
加载query dsl：

将上面的dsl放入xml文件httpdsl.xml，将文件地址以及dsl脚本名称设置到httpInputConfig即可

```java
  httpInputConfig.setDslFile("httpdsl.xml")
        .setQueryDslName("queryPagineDsl")
        .setQueryUrl("/httpservice/getPagineData.api")
        .setPagine(true)
```
也可以直接将query dsl设置到httpInputConfig，无需xml配置文件，参考后面的[案例3-基于query-dsl脚本从http服务获取数据，写入elasticsearch](https://esdoc.bbossgroups.com/#/datatran-http?id=%e6%a1%88%e4%be%8b3-%e5%9f%ba%e4%ba%8equery-dsl%e8%84%9a%e6%9c%ac%e4%bb%8ehttp%e6%9c%8d%e5%8a%a1%e8%8e%b7%e5%8f%96%e6%95%b0%e6%8d%ae%ef%bc%8c%e5%86%99%e5%85%a5elasticsearch)

httpResultParsers使用案例：可以自定义返回报文解析机制，从报文中提取数据和签名识别校验等操作

```java
httpInputConfig.setHttpResultParser(new HttpResultParser<Map>() {
					@Override
                    					public void parserHttpResult(HttpResult<Map> httpResult, HttpResultParserContext httpResultParserContext) throws Exception{
                    						HttpResponse httpResponse = httpResult.getResponse();
                    						HttpEntity entity = httpResponse.getEntity();
                    						if(entity == null)
                    							return;
                    						String datas = EntityUtils.toString(entity);
                    						//可以自行对返回值进行处理，比如解密，或者签名校验，但是最终需要将包含在datas里面的采集的数据集合转换为List<Map>结构，便于后续对数据进行加工处理
                    						//这里由于数据本身就是List<Map>结构，所以只需要做简单的序列化处理操作即可，这个也是默认的操作
                    						List<Map> _datas = SimpleStringUtil.json2ListObject(datas, Map.class);
                    						httpResult.setDatas(_datas);//必须将得到的集合设置到httpResult中，否则无法对数据进行后续处理
                    						httpResult.setParseredObject(datas);//设置原始数据
                    					}
				})
```


# 3.http输出插件

bboss可以直接将不同插件从数据源采集的数据推送到http服务，也可以通过dsl脚本语言，重新组装需发送数据报文结构，非常灵活方便，可以通过后面的[案例](https://esdoc.bbossgroups.com/#/datatran-http?id=%e6%a1%88%e4%be%8b2-%e5%8a%a8%e6%80%81header%e5%92%8c%e5%8a%a8%e6%80%81%e5%8f%82%e6%95%b0%e6%a1%88%e4%be%8b)来了解。

创建输出组件配置对象：

```java
HttpOutputConfig httpOutputConfig = new HttpOutputConfig();
importBuilder.setOutputConfig(httpOutputConfig);
```

| 属性名称              | 类型            | 说明                                                         |
| --------------------- | --------------- | ------------------------------------------------------------ |
| targetHttpPool        | String          | 目标http连接池服务组名称                                     |
| serviceUrl            | String          | 上报数据的http服务地址，相对路径，对应的服务器对应清单有属性datatran.http.hosts指定，多个地址逗号分隔，示例：httpOutputConfig.setServiceUrl("/httpservice/sendData.api") |
| httpMethod            | String          | http请求method，支持两种：put，post                          |
| lineSeparator         | String          | 设置数据记录分行符，默认为回车换行符                         |
| recordGenerator       | RecordGenerator | 自定义每条记录的数据格式，默认为json格式输出每条记录         |
| addTargetHttpPoolName | 方法            | 添加目标http连接池服务组名称httpOutputConfig.addTargetHttpPoolName("http.poolNames","datatran") |
| addHttpOutputConfig   | 方法            | 添加http服务连接池参数，httpOutputConfig    .addHttpOutputConfig("datatran.http.health","/health")       .addHttpOutputConfig("datatran.http.hosts","192.168.137.1:808")       .addHttpOutputConfig("datatran.http.timeoutConnection","5000")       .addHttpOutputConfig("datatran.http.timeoutSocket","50000")       .addHttpOutputConfig("datatran.http.connectionRequestTimeout","50000")       .addHttpOutputConfig("datatran.http.maxTotal","200")       .addHttpOutputConfig("datatran.http.defaultMaxPerRoute","100")       .addHttpOutputConfig("datatran.http.failAllContinue","true"); |
| addDynamicHeader      | 方法            | 添加动态http头属性，可用于设置基于jwt等认证机制的头部token（具备生命周期，失效后重新申请） |
| addHttpHeader         | 方法            | 添加http头属性                                               |
| addHttpHeaders        | 方法            | 批量添加http头属性，可用于设置基于jwt等认证机制的头部token   |
| showDsl               | boolean         | 控制作业执行时，是否打印查询的dsl脚本，true 打印，false 不打印，默认值false，控制是否将dsl打印到日志文件或者控制台，要求log级别为info级别 |
| json                  | boolean         | 控制输出数据是否采用标准的json集合格式输出，true 使用，false不使用，默认值true |
| dataKey               | String          | 设置输出数据到dsl的变量名称，用来保持输出数据：httpOutputConfig.setDataKey("httpDatas")                                                         在dsl中应用变量："datas":  #[httpDatas,quoted=false,escape=false], ## datas,发送的数据源 |
| DslFile               | String          | dsl脚本配置文件路径，在classes路径下                         |
| DataDslName           | String          | dsl脚本名称，脚本配置规范，可以参考文档：https://esdoc.bbossgroups.com/#/development  章节【[5.3 dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl配置规范)】 |
| dataDsl               | String          | 直接设置输出数据的Dsl脚本，脚本配置规范，可以参考文档：https://esdoc.bbossgroups.com/#/development  章节【[5.3 dsl配置规范](https://esdoc.bbossgroups.com/#/development?id=_53-dsl配置规范)】 |



# 4.数据转换处理

通过设置DataRefactor接口来实现记录级别的数据处理和转换，例如数据类型转换，从原始记录中获取HttpResponse对象，提取http请求头相关信息。 

```java
importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				long logTime = context.getLongValue("logTime");
				context.addFieldValue("logTime",new Date(logTime));
				long oldLogTime = context.getLongValue("oldLogTime");
				context.addFieldValue("oldLogTime",new Date(oldLogTime));
				long oldLogTimeEndTime = context.getLongValue("oldLogTimeEndTime");
				context.addFieldValue("oldLogTimeEndTime",new Date(oldLogTimeEndTime));
//				Date date = context.getDateValue("LOG_OPERTIME");

				HttpRecord record = (HttpRecord) context.getCurrentRecord();
				HttpResponse response = record.getResponse();//可以从httpresponse中获取head之类的信息
				context.addFieldValue("collecttime",new Date());//添加采集时间

			}
		});
```

# 5.jwt token认证设置

通过动态header设置jwt 认证token

```java
httpInputConfig.addDynamicHeader("Authorization", new DynamicHeader() {
   @Override
   public String getValue(String header, DynamicHeaderContext dynamicHeaderContext) throws Exception {
      //判断服务token是否过期，如果过期则需要重新调用token服务申请token
      TokenInfo tokenInfo = tokenManager.getTokenInfo();
      String token = "Bearer " + tokenInfo.getAccess_token();//"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZWZhdWx0XzYxNTE4YjlmM2UyYmM3LjEzMDI5OTkxIiwiaWF0IjoxNjMyNzM0MTExLCJuYmYiOjE2MzI3MzQxMTEsImV4cCI6MTYzMjc0MTMxMSwiZGV2aWNlX2lkIjoiYXBwMDMwMDAwMDAwMDAwMSIsImFwcF9pZCI6ImFwcDAzIiwidXVpZCI6ImFkZmRhZmFkZmFkc2ZlMzQxMzJmZHNhZHNmYWRzZiIsInNlY3JldCI6ImFwcDAzMVEyVzNFd29ybGQxMzU3OVBhc3NBU0RGIiwiaXNzdWVfdGltZSI6MTYzMjczNDExMSwiand0X3NjZW5lIjoiZGVmYXVsdCJ9.mSl-JBUV7gTUapn9yV-VLfoU7dm-gxC7pON62DnD-9c";
      return token;
   }
})
```

# 6.自定义数据记录输出格式

http输出插件输出的数据记录默认采用json格式输出，采用换行符分割多条记录：

```json
{"id":"1","name":"duoduo","sex":"F","class":"师大附中初一班"}
{"id":"2","name":"xiaoli","sex":"M","class":"师大附中初二班"}
```

bboss同时也可以自定义http输出插件输出的数据记录格式，示例如下：

首先可以通过设置分割多条记录规则（默认采用换行符）：

```java
httpOutputConfig.setLineSeparator("^");
```

自定义记录输出格式：使用^作为行分隔符，|作为记录字段值分隔符，由于不是json格式，所以设置json为false

```java
httpOutputConfig.setJson(false)
      .setLineSeparator("^")
      .setRecordGenerator(new RecordGenerator() {
         @Override
         public void buildRecord(Context taskContext, CommonRecord record, Writer builder) throws Exception {
            Map<String, Object> datas = record.getDatas();
            try {
               Map<String,String> chanMap = (Map<String,String>)taskContext.getTaskContext().getTaskData("chanMap");

               String phoneNumber = (String) datas.get("phoneNumber");//手机号码
               if(phoneNumber==null){
                  phoneNumber="";
               }
               builder.write(phoneNumber);
               builder.write("|");

               String chanId = (String) datas.get("chanId");//办理渠道名称 通过Id获取名称
               String chanName = null;
               if(chanId==null){
                  chanName="";
               }else{
                  chanName=chanMap.get(chanId);
                  if(chanName == null){
                     chanName = chanId;
                  }
               }
               builder.write(chanName);
               builder.write("|");

               String startTime = "";//办理开始时间(时间戳)
               if( datas.get("startTime")!=null){
                  startTime=datas.get("startTime")+"";
               }
               builder.write(startTime);
               builder.write("|");

               String endTime = "";//办理结束时间(时间戳)
               if( datas.get("endTime")!=null){
                  endTime=datas.get("endTime")+"";
               }
               builder.write(endTime);
               builder.write("|");

               String ydCodeLv1 = (String) datas.get("ydCodeLv1");//业务一级分类编码（取目前的业务大类编码）
               if(ydCodeLv1==null){
                  ydCodeLv1="";
               }
               builder.write(ydCodeLv1);
               builder.write("|");

               String ydNameLv1 = (String) datas.get("ydNameLv1");//业务一级分类名称（取目前的业务大类名称）
               if(ydNameLv1==null){
                  ydNameLv1="";
               }
               builder.write(ydNameLv1);
               builder.write("|");

               String ydCodeLv2 = (String) datas.get("ydCodeLv2");//业务二级分类编码（取目前的业务小类编码）
               if(ydCodeLv2==null){
                  ydCodeLv2="";
               }
               builder.write(ydCodeLv2);
               builder.write("|");

               String ydNameLv2 = (String) datas.get("ydNameLv2");//、业务二级分类名称（取目前的业务小类名称）
               if(ydNameLv2==null){
                  ydNameLv2="";
               }
               builder.write(ydNameLv2);
               builder.write("|");

               String ydCodeLv3 = (String) datas.get("ydCodeLv3");//业务三级分类编码（取目前的产品编码）
               if(ydCodeLv3==null){
                  ydCodeLv3="";
               }
               builder.write(ydCodeLv3);
               builder.write("|");

               String ydNameLv3 = (String) datas.get("ydNameLv3");//业务三级分类名称（取目前的产品名称）
               if(ydNameLv3==null){
                  ydNameLv3="";
               }
               builder.write(ydNameLv3);
               builder.write("|");

               String goodsName = (String) datas.get("goodsName");//资费档次名称
               if(goodsName==null){
                  goodsName="";
               }
               builder.write(goodsName);
               builder.write("|");

               String goodsCode = (String) datas.get("goodsCode");//资费档次编码
               if(goodsCode==null){
                  goodsCode="";
               }
               builder.write(goodsCode);
               builder.write("|");

               String bossErrorCode = (String) datas.get("bossErrorCode");//BOSS错误码
               if(bossErrorCode==null){
                  bossErrorCode="";
               }
               builder.write(bossErrorCode);
               builder.write("|");

               String bossErrorDesc = (String) datas.get("bossErrorDesc");//BOSS错误码描述
               if(bossErrorDesc==null){
                  bossErrorDesc="";
               }else{
                  bossErrorDesc = bossErrorDesc.replace("|","__").replace("\r\n","");
               }
               builder.write(bossErrorDesc);

            } catch (IOException e) {
               throw new DataImportException("RecordGenerator failed:",e);
            }
         }
      })
```

# 7.案例

## 7.1 http输入插件案例

### 案例1 调用http服务获取数据，写入elasticsearch

query dsl维护在配置文件httpdsl.xml中，QueryDslName为queryDsl

```java
ImportBuilder importBuilder = new ImportBuilder() ;
      importBuilder.setFetchSize(50).setBatchSize(10);
      HttpInputConfig httpInputConfig = new HttpInputConfig();
      //指定导入数据的dsl语句，必填项，可以设置自己的提取逻辑，
      // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：


      httpInputConfig.setDslFile("httpdsl.xml")
            .setQueryDslName("queryDsl")
            .setQueryUrl("/httpservice/getData.api")
            .addSourceHttpPoolName("http.poolNames","datatran")
            .addHttpInputConfig("datatran.http.health","/health")
            .addHttpInputConfig("datatran.http.hosts","192.168.137.1:808")
            .addHttpInputConfig("datatran.http.timeoutConnection","5000")
            .addHttpInputConfig("datatran.http.timeoutSocket","50000")
            .addHttpInputConfig("datatran.http.connectionRequestTimeout","50000")
            .addHttpInputConfig("datatran.http.maxTotal","200")
            .addHttpInputConfig("datatran.http.defaultMaxPerRoute","100")
            .addHttpInputConfig("datatran.http.failAllContinue","true");

      importBuilder.setInputConfig(httpInputConfig);
      importBuilder.addParam("otherParam","陈雨菲2:0战胜戴资颖");


      ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
      elasticsearchOutputConfig.setTargetElasticsearch("default")
            .setIndex("https2es")
            .setEsIdField("log_id")//设置文档主键，不设置，则自动产生文档id
            .setDebugResponse(false)//设置是否将每次处理的reponse打印到日志文件中，默认false
            .setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
     
      importBuilder.setOutputConfig(elasticsearchOutputConfig);
     
      /**
       * 执行http服务数据导入es作业
       */
      DataStream dataStream = importBuilder.builder();
      dataStream.execute();//执行导入操作
```

完整的案例地址：

https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/http/Http2ESDemo.java

### 案例2 调用分页http服务获取数据，写入elasticsearch

query dsl维护在配置文件httpdsl.xml中，QueryDslName为queryPagineDsl

```java
ImportBuilder importBuilder = new ImportBuilder() ;
      importBuilder.setFetchSize(50).setBatchSize(10);
      HttpInputConfig httpInputConfig = new HttpInputConfig();
      //指定导入数据的dsl语句，必填项，可以设置自己的提取逻辑，
      // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：


      httpInputConfig.setDslFile("httpdsl.xml")
            .setQueryDslName("queryPagineDsl")
            .setQueryUrl("/httpservice/getPagineData.api")
            .setPagine(true)
            .addSourceHttpPoolName("http.poolNames","datatran")
            .addHttpInputConfig("datatran.http.health","/health")
            .addHttpInputConfig("datatran.http.hosts","192.168.137.1:808")
            .addHttpInputConfig("datatran.http.timeoutConnection","5000")
            .addHttpInputConfig("datatran.http.timeoutSocket","50000")
            .addHttpInputConfig("datatran.http.connectionRequestTimeout","50000")
            .addHttpInputConfig("datatran.http.maxTotal","200")
            .addHttpInputConfig("datatran.http.defaultMaxPerRoute","100")
            .addHttpInputConfig("datatran.http.failAllContinue","true");

      importBuilder.setInputConfig(httpInputConfig);

      importBuilder.addParam("otherParam","陈雨菲2:0战胜戴资颖");

      ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
      elasticsearchOutputConfig.setTargetElasticsearch("default")
            .setIndex("httppagein2es")
            .setEsIdField("log_id")//设置文档主键，不设置，则自动产生文档id
            .setDebugResponse(false)//设置是否将每次处理的reponse打印到日志文件中，默认false
            .setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
  

      importBuilder.setOutputConfig(elasticsearchOutputConfig);
  
      /**
       * 执行作业
       */
      DataStream dataStream = importBuilder.builder();
      dataStream.execute();//执行导入操作
```

完整的案例地址

https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/http/Http2ESPagineDemo.java

案例对应的query dsl脚本：

```xml
<?xml version="1.0" encoding='UTF-8'?>
<properties>
    <description>
        <![CDATA[
   配置数据导入的http服务queryDsl
 ]]>
    </description>
    <property name="queryPagineDsl">
        <![CDATA[
        {
            "logTime":#[logTime],## 传递增量时间起始条件
            "logTimeEndTime":#[logTime__endTime],## 传递增量时间截止时间条件，必须指定IncreamentEndOffset偏移时间量才能设置增量截止时间
            "from":#[httpPagineFrom], ## 如果服务支持分页获取增量或者全量数据，设置分页起始位置
            "size":#[httpPagineSize],  ## 如果服务支持分页获取增量或者全量数据，设置每页记录数，如果实际返回的记录数小于httpPagineSize或者为0，则表示本次分页获取数据结束，对应参数fetchSize配置的值
            "otherParam": #[otherParam] ## 其他服务参数
        }
        ]]></property>

    <property name="queryDsl">
        <![CDATA[
        {
            "logTime":#[logTime],## 传递增量时间起始条件
            "logTimeEndTime":#[logTime__endTime],## 传递增量时间截止时间条件，必须指定IncreamentEndOffset偏移时间量才能设置增量截止时间
            "otherParam": #[otherParam] ## 其他服务参数
        }
        ]]></property>

</properties>
```

### 案例3 基于query dsl脚本从http服务获取数据，写入elasticsearch

```java
ImportBuilder importBuilder = new ImportBuilder() ;
      importBuilder.setFetchSize(50).setBatchSize(10);
      HttpInputConfig httpInputConfig = new HttpInputConfig();
      //指定导入数据的dsl语句
     
      String queryDsl = " {\n" +
            "            \"logTime\":#[logTime],## 传递增量时间起始条件\n" +
            "            \"logTimeEndTime\":#[logTime__endTime],## 传递增量时间截止时间条件，必须指定IncreamentEndOffset偏移时间量才能设置增量截止时间\n" +
            "            \"otherParam\": #[otherParam] ## 其他服务参数\n" +
            "        }";

      httpInputConfig.setQueryDsl(queryDsl)
            .setQueryUrl("/httpservice/getData.api").setShowDsl(true)
            .addSourceHttpPoolName("http.poolNames","datatran")
            .addHttpInputConfig("datatran.http.health","/health")
            .addHttpInputConfig("datatran.http.hosts","192.168.137.1:808")
            .addHttpInputConfig("datatran.http.timeoutConnection","5000")
            .addHttpInputConfig("datatran.http.timeoutSocket","50000")
            .addHttpInputConfig("datatran.http.connectionRequestTimeout","50000")
            .addHttpInputConfig("datatran.http.maxTotal","200")
            .addHttpInputConfig("datatran.http.defaultMaxPerRoute","100")
            .addHttpInputConfig("datatran.http.failAllContinue","true");

      importBuilder.setInputConfig(httpInputConfig);
      importBuilder.addParam("otherParam","陈雨菲2:0战胜戴资颖");


      ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
      elasticsearchOutputConfig.setTargetElasticsearch("default")
            .setIndex("https2esdsl")
            .setEsIdField("log_id")//设置文档主键，不设置，则自动产生文档id
            .setDebugResponse(false)//设置是否将每次处理的reponse打印到日志文件中，默认false
            .setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
     

      importBuilder.setOutputConfig(elasticsearchOutputConfig);
     
      /**
       * 执行作业
       */
      DataStream dataStream = importBuilder.builder();
      dataStream.execute();//执行导入操作
```

完整的案例地址

https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/http/Http2ESQueryDslDemo.java

### 案例4 动态参数和动态header案例

```java
HttpInputConfig httpInputConfig = new HttpInputConfig();


httpInputConfig.setDslFile("httpdsl.xml")
      .setQueryDslName("queryPagineDsl")
      .setQueryUrl("/httpservice/getPagineData.api")
      .setPagine(true)
      .setShowDsl(true)
      .setPagineFromKey("httpPagineFrom")
      .setPagineSizeKey("httpPagineSize")
      .addHttpHeader("testHeader","xxxxx")
      .addDynamicHeader("Authorization", new DynamicHeader() {
         @Override
         public String getValue(String header, DynamicHeaderContext dynamicHeaderContext) throws Exception {
            //判断服务token是否过期，如果过期则需要重新调用token服务申请token
            String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZWZhdWx0XzYxNTE4YjlmM2UyYmM3LjEzMDI5OTkxIiwiaWF0IjoxNjMyNzM0MTExLCJuYmYiOjE2MzI3MzQxMTEsImV4cCI6MTYzMjc0MTMxMSwiZGV2aWNlX2lkIjoiYXBwMDMwMDAwMDAwMDAwMSIsImFwcF9pZCI6ImFwcDAzIiwidXVpZCI6ImFkZmRhZmFkZmFkc2ZlMzQxMzJmZHNhZHNmYWRzZiIsInNlY3JldCI6ImFwcDAzMVEyVzNFd29ybGQxMzU3OVBhc3NBU0RGIiwiaXNzdWVfdGltZSI6MTYzMjczNDExMSwiand0X3NjZW5lIjoiZGVmYXVsdCJ9.mSl-JBUV7gTUapn9yV-VLfoU7dm-gxC7pON62DnD-9c";
            return token;
         }
      })
      .setHttpResultParser(new HttpResultParser<Map>() {
         @Override
         public void parserHttpResult(HttpResult<Map> httpResult, HttpResultParserContext httpResultParserContext) throws Exception{
            HttpResponse httpResponse = httpResult.getResponse();
            HttpEntity entity = httpResponse.getEntity();
            if(entity == null)
               return;
            String datas = EntityUtils.toString(entity);
            //可以自行对返回值进行处理，比如解密，或者签名校验，但是最终需要将包含在datas里面的采集的数据集合转换为List<Map>结构，便于后续对数据进行加工处理
            //这里由于数据本身就是List<Map>结构，所以只需要做简单的序列化处理操作即可，这个也是默认的操作
            List<Map> _datas = SimpleStringUtil.json2ListObject(datas, Map.class);
            httpResult.setDatas(_datas);//必须将得到的集合设置到httpResult中，否则无法对数据进行后续处理
            httpResult.setParseredObject(datas);//设置原始数据
         }
      })
      .addSourceHttpPoolName("http.poolNames","datatran")
      .addHttpInputConfig("datatran.http.health","/health")
      .addHttpInputConfig("datatran.http.hosts","192.168.137.1:808")
      .addHttpInputConfig("datatran.http.timeoutConnection","5000")
      .addHttpInputConfig("datatran.http.timeoutSocket","50000")
      .addHttpInputConfig("datatran.http.connectionRequestTimeout","50000")
      .addHttpInputConfig("datatran.http.maxTotal","200")
      .addHttpInputConfig("datatran.http.defaultMaxPerRoute","100")
      .addHttpInputConfig("datatran.http.failAllContinue","true");


importBuilder.setInputConfig(httpInputConfig);
importBuilder.addJobInputParam("otherParam","陈雨菲2:0战胜戴资颖")
          .addJobInputParam("device_id","app03001")
           .addJobInputParam("app_id","app03")
.addJobDynamicInputParam("signature", new DynamicParam() {//根据数据动态生成签名参数
   @Override
   public Object getValue(String paramName, DynamicParamContext dynamicParamContext) {

      //可以根据自己的算法对数据进行签名
      String signature = "1b3bb71f6ebae2f52b7a238c589f3ff9";//signature =md5(datas)
      return signature;
   }
});
```

完整的案例地址

https://gitee.com/bboss/bboss-datatran-demo/blob/6.7.3/src/main/java/org/frameworkset/elasticsearch/imp/Http2ESFullQueryDslDemo.java

dsl语句：https://gitee.com/bboss/bboss-datatran-demo/blob/6.7.3/src/main/resources/httpdsl.xml

```xml
<property name="queryPagineDsl">
    <![CDATA[
    {
        "device_id": #[device_id], ## device_id,通过addJobInputParam赋值
        "app_id": #[app_id], ## app_id,通过addJobInputParam赋值
        "logTime":#[logTime],## 传递增量时间起始条件
        "logTimeEndTime":#[logTime__endTime],## 传递增量时间截止时间条件，必须指定IncreamentEndOffset偏移时间量才能设置增量截止时间
        "from":#[httpPagineFrom], ## 如果服务支持分页获取增量或者全量数据，设置分页起始位置
        "size":#[httpPagineSize],  ## 如果服务支持分页获取增量或者全量数据，设置每页记录数，如果实际返回的记录数小于httpPagineSize或者为0，则表示本次分页获取数据结束，对应参数fetchSize配置的值
        "otherParam": #[otherParam] ## 其他服务参数otherParam,通过addJobInputParam赋值
    }
    ]]></property>
```

## 7.2 http输出插件案例

### 案例1 从elasticsearch获取数据，直接将数据推送到http服务

```java
      ImportBuilder importBuilder = new ImportBuilder() ;
      importBuilder.setFetchSize(50).setBatchSize(10);
      ElasticsearchInputConfig elasticsearchInputConfig = new ElasticsearchInputConfig();
      elasticsearchInputConfig.setDslFile("dsl2ndSqlFile.xml")//配置dsl和sql语句的配置文件
            .setDslName("scrollQuery") //指定从es查询索引文档数据的dsl语句名称，配置在dsl2ndSqlFile.xml中
            .setScrollLiveTime("10m") //scroll查询的scrollid有效期

//              .setSliceQuery(true)
//               .setSliceSize(5)
            .setQueryUrl("https2es/_search") ;//查询索引表demo中的文档数据

//          //添加dsl中需要用到的参数及参数值
//          importBuilder.addParam("var1","v1")
//          .addParam("var2","v2")
//          .addParam("var3","v3");

      importBuilder.setInputConfig(elasticsearchInputConfig);
      HttpOutputConfig httpOutputConfig = new HttpOutputConfig();
      //指定导入数据的dsl语句，必填项，可以设置自己的提取逻辑，
      // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：


      httpOutputConfig
            .setServiceUrl("/httpservice/sendData.api")
            .setHttpMethod("post")
            .addTargetHttpPoolName("http.poolNames","datatran")
            .addHttpOutputConfig("datatran.http.health","/health")
            .addHttpOutputConfig("datatran.http.hosts","192.168.137.1:808")
            .addHttpOutputConfig("datatran.http.timeoutConnection","5000")
            .addHttpOutputConfig("datatran.http.timeoutSocket","50000")
            .addHttpOutputConfig("datatran.http.connectionRequestTimeout","50000")
            .addHttpOutputConfig("datatran.http.maxTotal","200")
            .addHttpOutputConfig("datatran.http.defaultMaxPerRoute","100")
            .addHttpOutputConfig("datatran.http.failAllContinue","true");

      importBuilder.setOutputConfig(httpOutputConfig);


      /**
       * 执行数据库表数据导入es操作
       */
      DataStream dataStream = importBuilder.builder();
      dataStream.execute();//执行导入操作
```

完整的案例地址：

https://gitee.com/bboss/db-elasticsearch-tool/blob/master/src/main/java/org/frameworkset/elasticsearch/imp/http/ES2HttpDemo.java

### 案例2 动态header和动态参数jwt认证和数据签名案例

 动态header和动态参数jwt认证和数据签名必须通过http输出插件的dsl脚本来实现
 本例通过动态header 参数设置jwt token认证Authorization Bearer ,判断服务token是否过期，如果过期则需要重新调用token服务申请token

通过动态job output参数设置数据签名signature，根据数据动态生成签名参数。

```java
//http输出插件配置
      HttpOutputConfig httpOutputConfig = new HttpOutputConfig();
      //指定导入数据的dsl语句，必填项，可以设置自己的提取逻辑


      httpOutputConfig
            .setJson(true)
            .setShowDsl(true)
            .setDslFile("httpdsl.xml")
            .setDataDslName("sendData")
            .setDataKey("httpDatas")
            .setServiceUrl("/httpservice/sendData.api")
            .setHttpMethod("post")
            .addHttpHeader("testHeader","xxxxx")
            .addDynamicHeader("Authorization", new DynamicHeader() {
               @Override
               public String getValue(String header, DynamicHeaderContext dynamicHeaderContext) throws Exception {
                  //判断服务token是否存在或者过期，如果token不存在或者已经过期则需要重新调用token服务申请token
                  TokenInfo tokenInfo = tokenManager.getTokenInfo();
                  String token = "Bearer " + tokenInfo.getAccess_token();//"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZWZhdWx0XzYxNTE4YjlmM2UyYmM3LjEzMDI5OTkxIiwiaWF0IjoxNjMyNzM0MTExLCJuYmYiOjE2MzI3MzQxMTEsImV4cCI6MTYzMjc0MTMxMSwiZGV2aWNlX2lkIjoiYXBwMDMwMDAwMDAwMDAwMSIsImFwcF9pZCI6ImFwcDAzIiwidXVpZCI6ImFkZmRhZmFkZmFkc2ZlMzQxMzJmZHNhZHNmYWRzZiIsInNlY3JldCI6ImFwcDAzMVEyVzNFd29ybGQxMzU3OVBhc3NBU0RGIiwiaXNzdWVfdGltZSI6MTYzMjczNDExMSwiand0X3NjZW5lIjoiZGVmYXVsdCJ9.mSl-JBUV7gTUapn9yV-VLfoU7dm-gxC7pON62DnD-9c";
                  return token;
               }
            })
//          .addTargetHttpPoolName("http.poolNames","datatran,jwtservice")//初始化多个http服务集群时，就不要用addTargetHttpPoolName方法，使用以下方法即可
            .setTargetHttpPool("datatran")
            .addHttpOutputConfig("http.poolNames","datatran,jwtservice")
//          .addHttpOutputConfig("datatran.http.health","/health")//服务监控检查地址
            .addHttpOutputConfig("datatran.http.hosts","192.168.137.1:808")//服务地址清单，多个用逗号分隔
            .addHttpOutputConfig("datatran.http.timeoutConnection","5000")
            .addHttpOutputConfig("datatran.http.timeoutSocket","50000")
            .addHttpOutputConfig("datatran.http.connectionRequestTimeout","50000")
            .addHttpOutputConfig("datatran.http.maxTotal","200")
            .addHttpOutputConfig("datatran.http.defaultMaxPerRoute","100")
            .addHttpOutputConfig("datatran.http.failAllContinue","true")
            //设置token申请和更新服务配置jwtservice，在TokenManager中使用jwtservice申请和更新token
//          .addHttpOutputConfig("jwtservice.http.health","/health") //服务监控检查地址
            .addHttpOutputConfig("jwtservice.http.hosts","192.168.137.1:808") //服务地址清单，多个用逗号分隔，192.168.0.100:9501
            .addHttpOutputConfig("jwtservice.http.timeoutConnection","5000")
            .addHttpOutputConfig("jwtservice.http.timeoutSocket","50000")
            .addHttpOutputConfig("jwtservice.http.connectionRequestTimeout","50000")
            .addHttpOutputConfig("jwtservice.http.maxTotal","200")
            .addHttpOutputConfig("jwtservice.http.defaultMaxPerRoute","100")
            .addHttpOutputConfig("jwtservice.http.failAllContinue","true")

      ;

      importBuilder.addJobOutputParam("device_id","app03001")
                .addJobOutputParam("app_id","app03")
                .addJobDynamicOutputParam("signature", new DynamicParam() {//根据数据动态生成签名参数
                   @Override
                   public Object getValue(String paramName, DynamicParamContext dynamicParamContext) {
                       String datas = (String) dynamicParamContext.getDatas();
                       //可以根据自己的算法对数据进行签名
                       String signature = "1b3bb71f6ebae2f52b7a238c589f3ff9";//signature =md5(datas)
                      return signature;
                   }
                });
      importBuilder.setOutputConfig(httpOutputConfig);
```

TokenManager是一个简单jwt token管理的组件：

```java
package org.frameworkset.elasticsearch.imp;


import org.frameworkset.spi.remote.http.HttpRequestProxy;
import org.frameworkset.tran.DataImportException;
import org.frameworkset.util.TimeUtil;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class TokenManager {
	private TokenInfo tokenInfo;

	/**
	 * 如果token不存在或者token过期，则调用jwtservice /api/auth/v1.0/getToken申请token
	 * @return
	 */
	public synchronized TokenInfo getTokenInfo(){
		if(tokenInfo == null || expired()){//没有token或者token过期
			Map params = new LinkedHashMap();
			/**
			 *  "device_id": "app03001",
			 *     "app_id": "app03",
			 *     "signature": "1b3bb71f6ebae2f52b7a238c589f3ff9",
			 *     "uuid": "adfdafadfadsfe34132fdsadsfadsf"
			 */
			params.put("device_id","app03001");
			params.put("app_id","app03");
			params.put("signature","1b3bb71f6ebae2f52b7a238c589f3ff9");
			params.put("uuid","adfdafadfadsfe34132fdsadsfadsf");
			Map datas = HttpRequestProxy.sendJsonBody("jwtservice",params,"/api/auth/v1.0/getToken.api", Map.class);//调用jwtservice对应的jwt token服务，获取jwt token信息
			if(datas != null){
				int code = (int)datas.get("code");
				if(code == 200) {
					Map<String, Object> tokens = (Map<String, Object>) datas.get("data");
					TokenInfo tokenInfo = new TokenInfo();//将获取jwt token信息转换为对象
					tokenInfo.setTokenTimestamp(new Date());//直接将当前时间作为token的生产时间戳，实际情况需从jwt token中提取对应的时间
					tokenInfo.setAccess_token((String)tokens.get("access_toke"));
					tokenInfo.setExpires_time((int)tokens.get("expires_time"));
					tokenInfo.setExpiredTimestamp(TimeUtil.addDateSeconds(tokenInfo.getTokenTimestamp(),tokenInfo.getExpires_time()));
					this.tokenInfo = tokenInfo;
				}


			}
			if(tokenInfo == null){
				throw new DataImportException("get token failed: token info is null");
			}
			return tokenInfo;
		}
		else{
			return tokenInfo;
		}
	}

	private boolean expired(){
		return tokenInfo.getExpiredTimestamp().before(new Date());
	}
}

```
发送数据的dsl脚本：

```xml
<property name="sendData">
    <![CDATA[
    {
        "device_id": #[device_id], ## device_id,通过addJobInputParam赋值
        "app_id": #[app_id], ## app_id,通过addJobInputParam赋值
        "datas":  #[httpDatas,quoted=false,escape=false], ## datas,发送的数据源，关闭自动加双引号和自动对数据特殊字符转义功能，因为httpDatas是一个标准的json集合，如果不是则去掉控制参数，直接设置#[httpDatas]即可，具体看config的json变量值，json=false时，需要去掉相关控制参数
        "signature": #[signature]
    }
    ]]></property>
```
对应的dsl配置文件

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/resources/httpdsl.xml

完整的案例地址：

采集本地文件数据推送到http服务   

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/LocalLog2FullfeatureHttpDslDemo.java

采集ftp文件数据推送到http服务

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/elasticsearch/imp/SFtpLog2FullfeatureHttpDslDemo.java



## 7.3 案例发布运行

案例工程下载：下载到本地目db-elasticsearch-tool

https://gitee.com/bboss/db-elasticsearch-tool

修改application.properties文件中的mainclass为要执行的作业类路径,例如

```properties
mainclass=org.frameworkset.elasticsearch.imp.http.Http2ESDemo
```

调整好作业后，执行db-elasticsearch-tool目录下指令，构建和发布作业

windows环境

release.bat

linux环境

release.sh

完整的作业发布视频教程：

https://www.bilibili.com/video/BV1xf4y1Z7xu

# 8.参考文档

本插件底层基于bboss httpproxy组件实现，参考文档：

https://esdoc.bbossgroups.com/#/development?id=_26-http%e5%8d%8f%e8%ae%ae%e9%85%8d%e7%bd%ae



https://esdoc.bbossgroups.com/#/httpproxy