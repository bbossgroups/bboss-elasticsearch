# 智能问答和会话管理

本文介绍通过bboss ai与spring boot集成实现智能体问答功能。

## 1 后端控制器

```java
@RequestMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE, method = {RequestMethod.POST, RequestMethod.GET})
public SseEmitter stream(@RequestBody ChatRequest request, HttpServletResponse response) {
    response.setHeader("X-Accel-Buffering", "no");
    response.setHeader("Cache-Control", "no-cache");
    if (request == null || request.getSessionId() == null || request.getSessionId().isBlank()) {
        throw new BusinessException(400, "sessionId is required");
    }
    if (request.getQuestion() == null || request.getQuestion().isBlank()) {
        throw new BusinessException(400, "question is required");
    }
    SseEmitter emitter = new SseEmitter(600000L);

    String model = request.getModel();
    log.info("Received stream request: id={}, sessionId={}, model={}",
            request.getId(), request.getSessionId(), model == null ? "<default>" : model);

    try {
        if (request.getDomain() == null) {
            request.setDomain(AgentConstants.RAGKN_AGENT_DOMAIN);
        }
        ragQAService.streamFluxAdapted(request.getId(),
                request.getSessionId(),
                request.getUserId(),
                request.getQuestion(),
                model, request.getDomain(),request.isThinking(), new FluxRagStreamCallback(emitter, request.getId()));
    } catch (AIRuntimeException e) {
        log.error("[RAG-V2] stream unexpected failure id={}", request.getId(), e);
        emitter.completeWithError(e);
        throw e;
    } catch (Exception e) {
        log.error("[RAG-V2] stream unexpected failure id={}", request.getId(), e);
        emitter.completeWithError(e);
    }
    return emitter;
}
```

## 2 智能体流程编排代码

```java
public void streamFluxAdapted(String requestId, String sessionId, String userId, String question, String model, String domain,boolean thinking, FluxStreamCallback callback) {
 
    
       
       try {
          //1.定义会话存储机制
          StoreContext storeContext = new StoreContext()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setRequestId(requestId)
                .setSessionSize(500)
                .setStoreType(StoreContext.STORE_TYPE_DB)
                .setDataSource("sessionds").setDomain(domain);
          
          //2.定义大模型相关参数：maas平台
          ChatAgentMessage chatAgentMessage = new ChatAgentMessage();
          chatAgentMessage.setThinking(thinking);
//        chatAgentMessage.setEffort("medium");
          chatAgentMessage
//         .setModel("jiutian-lan-comv3")
//         .setModel("deepseek/deepseek-v4-flash")
//         .setMaas("jiutian1")
                .setMaas("deepseek")//maas平台
                .setModel("deepseek-v4-pro")//大模型标识
//              .setMaas("qwenvlplus")
//              .setMaas("qwentokenplan")        
//              .setModel("qwen3.7-plus")
//              .setMaas("jiutian")
//              .setModel("jiutian_75b")
                .setStream( true)
                .setRetry(3).setRetryInterval(1000L) //定义重试机制
                .setTemperature(0.3)  //定义温度
                .setPrompt(question); //设置用户原始问题
          AIPlanAgent planAgent = new AIPlanAgent(storeContext)
                .setAgentMessage(chatAgentMessage)
                .setAgentName("知识检索和问答工作流").setAgentId("ragQAFlow");
 
     
 
          planAgent.addAgent(new AIFlowNodeVoid("init","初始化节点"){
             
             @Override
             public void call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                // ===== 2. 加载历史会话消息，如果会话不存在，则创建 =====
                planAgent.loadSessionMemory( question, domain);
                TraceMessage traceMessage = new TraceMessage();       
                //记录用户输入的原始问题
                traceMessage.setMessage(Map.of("question", question,"role",SessionMessage.MESSAGE_TYPE_USER_INPUTMESSAGE_NAME));
                //其他用户上传的附件材料信息可以放到metaData中,也可以直接放到上面的消息中
//              traceMessage.setMetaData(Map.of("documents", new ArrayList<>()));
                traceMessage.setStartTime(System.currentTimeMillis());
//              traceMessage.setEndTime(System.currentTimeMillis());
                recordTraceMessage(traceMessage);
 
                
                
             }
          });
          
          //构建串行智能体
          AISequenceAgent sequenceAgent = new AISequenceAgent(planAgent).setAgentId("sequenceAgent").setAgentName("技术问答串行任务节点");
          
          // ===== 4. 读历史并改写追问 =====
          //直接使用用户问题改写智能体触发器中设置的ragQuestion变量对应的改写提示词，生成改写后的rag问题
          //StandaloneAgent:用户问题改写智能体不会参与整个工作流的上下文记忆，但是会持久化改下消息
          sequenceAgent.addConditionFlowNode(true,new StandaloneAgent("#[ragQuestion]").setDisableStream(true)
                .setAgentName("用户问题改写智能体")
                .setAgentId("userQuestionRewriter")
//              .setToolsRegist(configMcpServerToolsRegist) //注册mcp工具
                .setOutputVaribleName("retrievalQuestion", AIFlowConst.AIFLOW_VAR_SCOPE_FLOW), new TriggerScriptAPI() {
             @Override
             public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                String ragQuestion = null;
                
                List<Map<String,Object>> sessionMemory = planAgent.getSessionMemory();
                
                if(CollectionUtils.isNotEmpty(sessionMemory)) {
                   
                   String historyText = buildRewriteMemory(sessionMemory);
                   if(SimpleStringUtil.isNotEmpty(historyText)) {
                      ragQuestion = """
                            请根据历史对话，把用户最新问题改写成一个可以独立检索知识库的完整问题。
                            要求：
                            1. 只输出改写后的问题，不要解释。
                            2. 不要回答问题。
                            3. 如果最新问题本身已经完整，原样返回。
                            4. 保留关键技术名词、产品名、配置项、错误信息。
                                  
                            历史对话：
                            %s
                                  
                            最新问题：
                            %s
                            """.formatted(historyText, question);
                   }
                   nodeTriggerContext.getJobFlowExecuteContext().addContextData("ragQuestion", ragQuestion);
                   return true;
                }
                
                return false;
             }
          });
          // ===== 5. 混合检索（向量 + BM25 + RRF） =====
          sequenceAgent.addAgent(new AIFlowNodeVoid(){
             
             /**
              * 由子类继承和实现
              *
              * @param jobFlowNodeExecuteContext
              */
             @Override
             public void call(JobFlowNodeExecuteContext jobFlowNodeExecuteContext) {
                long startTime = System.currentTimeMillis();
                String retrievalQuestion = (String) jobFlowNodeExecuteContext.getJobFlowContextData("retrievalQuestion");
                if(retrievalQuestion == null){
                   retrievalQuestion = question;
                }
                List<RerankedDocument> rerankedDocuments = knowledgeEmbeddingService.searchVectorAndRerank(retrievalQuestion,sequenceAgent);
                double confidence = 0.0d;
                if(rerankedDocuments != null && rerankedDocuments.size() > 0) {
                   RerankedDocument rerankedDocument = rerankedDocuments.get(0);
                   confidence = rerankedDocument.getRelevanceScore();
                }
                List<Citation> citations = ragCitationExtractor.extractFlux( rerankedDocuments);

                if (SimpleStringUtil.isEmpty(citations) ) {
                   String msg = ragProperties.getRefusal().getMessage();
 
                   TraceMessage traceMessage = new TraceMessage();
                   
                   traceMessage.setMessage(Map.of("refuse", msg, "confidence", confidence,"role",SessionMessage.MESSAGE_TYPE_REFUSE_MESSAGE_NAME,"input",retrievalQuestion));
                   traceMessage.setStartTime(startTime);
                   traceMessage.setEndTime(System.currentTimeMillis());
                   recordTraceMessage(traceMessage);//记录拒答消息
                   ServerEvent serverEvent = new ServerEvent();//向客户端推送拒答信息
                   serverEvent.setType(ServerEvent.TYPE_REFUSAL);
                   serverEvent.setData(msg);
                   serverEvent.setConfidence(confidence);
                   serverEvent.setDone(true);
                   this.getAgentFluxSink().next(serverEvent);
 
                   jobFlowNodeExecuteContext.addJobFlowContextData("isShouldRefuse",true);
//                 try { callback.onComplete(); } catch (Exception ignored) {}
                }
                else{
                   
                   //记录 citations到trace中
//                 jobFlowNodeExecuteContext.addJobFlowContextData("citations",citations);
//                 objectHolder.setObject(citations);
                   // ===== 7. 上下文增强：把 reranked 的 top-K 注入 prompt =====
                   StringBuilder ragDocuments = new StringBuilder();
                   for (Citation document:citations){
                      ragDocuments.append(document.getEvidence()).append(System.lineSeparator());
                   }
                   String ragDocuments_ = ragDocuments.toString();
                   jobFlowNodeExecuteContext.addJobFlowContextData("ragDocuments",ragDocuments_);
                   ServerEvent serverEvent = new ServerEvent();
                   serverEvent.setType(ServerEvent.TYPE_RAG_KNOWLEDGE);
                   serverEvent.setConfidence(confidence);
//                 serverEvent.setData(ragDocuments_);
                   serverEvent.setRagKnowledge(citations);
                   this.getAgentFluxSink().next(serverEvent);
                   TraceMessage traceMessage = new TraceMessage();
                   traceMessage.setMessage(Map.of("rag", citations, "confidence", confidence,"role", SessionMessage.MESSAGE_TYPE_RAG_MESSAGE_NAME,"input",retrievalQuestion));
                   traceMessage.setStartTime(startTime);
                   traceMessage.setEndTime(System.currentTimeMillis());
                   recordTraceMessage(traceMessage);
                }
                
                
             }
          });
          String userPrompt = """
                # 用户问题
                
                #[input.query]
                
                # 问题知识
                
                #[ragDocuments]
                
                """;
//        UserNodeAgent userNodeAgent = new UserNodeAgent(userPrompt).setAgentId("answerQuestionAgent").setAgentName("答案生成智能体");
          // ===== 8. 答案生成智能体:如果问题已近拒答，则不生成答案 =====
          sequenceAgent.addAgent(new AINodeAgent(userPrompt).setAgentId("answerQuestionAgent").setAgentName("答案生成智能体")
//                    .setToolsRegist(configMcpServerToolsRegist) //注册mcp工具
                , new TriggerScriptAPI() {
                   @Override
                   public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                      Boolean isShouldRefuse = (Boolean) nodeTriggerContext.getFlowContextData("isShouldRefuse");
                      if(isShouldRefuse != null){
                         return !isShouldRefuse;
                      }
                      return true;
                   }
                });
          
          planAgent.addConditionFlowNode(sequenceAgent,true);
          
          AISequenceAgent vopsSequenceAgent = new AISequenceAgent(planAgent).setAgentId("vopsSequenceAgent").setAgentName("运维串行任务节点");
          AINodeAgent scan2ndClosePortProcessAgent = new AINodeAgent("#[prompts/rag-looptoolcall-prompt.txt,type=resource]")
                .setSystemPrompt("你是一个专家，可以根据用户要求获取系统信息，生成符合要求的、完整的、可执行的shell脚本" +
                      "，并将生成的脚本交由工具执行，输出执行结果。注意事项：通过Java Process调用cmd或者sh来执行脚本，确保脚本在目标操作系统上能够正常运行。")
                .setAgentId("scan2ndClosePortProcessAgent").setAgentName("扫描并关闭端口进程");
          scan2ndClosePortProcessAgent.setEnableLoopToolCall(true);//启用智能体多次调用工具机制
          scan2ndClosePortProcessAgent.setMaxLoopToolCalls(100);
          //注册获取当前操作系统OS信息工具：框架内置工具
          scan2ndClosePortProcessAgent.registBeanTool(new GetOSFunctionTool(60));
        //注册脚本执行工具，会根据获取到的OS信息，生成对应的OS环境命令行脚本进行执行：框架内置工具
          scan2ndClosePortProcessAgent.registBeanTool(new CLIShellFunctionTool(60));
          vopsSequenceAgent.addAgent(scan2ndClosePortProcessAgent);
          vopsSequenceAgent.addAgent(new AINodeAgent( "请根据用户的问题:#[input.query]，以及前面的回复，创建一份详细的飞书报告。请用清晰的中文输出。" )
                .setAgentName("飞书文档创建结果")
                .setAgentId("createFeishuDoc")
                .setToolsRegist(feishuMcpToolsRegist).setToolSearcher(new KeywordToolSearcher("创建飞书云文档")));
          
          planAgent.addConditionFlowNode(vopsSequenceAgent, new TriggerScriptAPI() {
             @Override
             public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                if(question.contains("OS信息查询"))
 
                   return true;
                return false;
             }
          });
          
          AISequenceAgent hitlSequenceAgent = new AISequenceAgent(planAgent).setAgentId("hitlSequenceAgent").setAgentName("人工干预串行任务节点");
          
//        String prompt = "请评审代码并修复问题,java文件路径：C:\\data\\ai\\code\\AIAgent.java";
          String prompt = "请评审代码并修复问题,java文件路径：" + agentBootrap.getHitlCodeReviewJavaFile();
          AINodeAgent hitlAgent = new AINodeAgent( prompt );
 
          hitlAgent.setSystemPrompt("你是一个 Java 代码审查助手，可以审查 Java 代码并给出修改建议，还可以将修复后的代码保存到源文件中。 长期规则： - 如果用户提交 Java 代码并要求审查，先调用 Skill 工具加载 code-review-skill。 - 保存修改代码前请调用工具hitlTaskTool进行人工确认。" +
                      "- 加载技能书后，再按照技能书里的审查顺序审查java代码。 - 优先指出 bug、安全风险、边界条件、异常处理和缺失测试。 " +
                      "- 如果信息不足，要说明缺少哪些上下文，不要编造项目背景。 - 不要输出与代码审查无关的泛泛建议。 输出要求： - 用中文回答。 " +
                      "- 使用 Markdown。 - 先给总体结论，再列主要问题，最后给测试建议和下一步。");
          hitlAgent.setEnableLoopToolCall(true);//启用智能体多次调用工具机制
          hitlAgent.setMaxLoopToolCalls(80);
          hitlAgent.registTools(new SkillsToolRegist()
                      .addClasspathSkills("skills"))
                .registBeanTool(new HitlTaskcallTool());//注册人工介入任务调用工具，用于人工介入任务的调用
          hitlAgent.setHitlTaskTimeout(60000L);
          //注册文件操作工具，用于读取文件,需要设置运行文件工具操作的目录清单，禁止文件工具操作清单之外的目录
          String[] baseDirs = agentBootrap.getHitlFileToolDasedirs();
          FileFunctionTool fileFunctionTool = new FileFunctionTool( );
          if(baseDirs != null && baseDirs.length > 0){
             fileFunctionTool.addBaseDirectory(baseDirs);
          }
          hitlAgent.registBeanTool(fileFunctionTool);
          
          hitlSequenceAgent.addAgent(hitlAgent);
//        hitlSequenceAgent.addAgent(new AINodeAgent( "请根据用户的问题:#[input.query]，以及评审结果及修复情况，创建一份详细的飞书报告。请用清晰的中文输出。" )
//              .setAgentName("创建代码评审飞书文档")
//              .setAgentId("createCodereviewFeishuDoc")
//              .setToolsRegist(feishuMcpToolsRegist).setToolSearcher(new KeywordToolSearcher("创建飞书云文档")));
          
          planAgent.addConditionFlowNode(hitlSequenceAgent, new TriggerScriptAPI() {
             @Override
             public boolean needTrigger(NodeTriggerContext nodeTriggerContext) throws Exception {
                if(question.contains("代码评审"))
 
                   return true;
                return false;
             }
          });
 
 
          Flux<ServerEvent> flux = planAgent.chatStream();
          
          
          Disposable disposable =  flux
 
                .doOnNext(event -> {
                   if(event.isHitl()){
                      log.info("=== 智能问答工作流进入人工干预信号来咯 ===,hitlTaskId={}", event.getHitlTaskId());
                   }
                   // 答案前后都可以添加链接和标题，实现相关知识资料链接
                   callback.onEvent(JsonUtil.object2json(event));
                }).doOnComplete(() -> {
                   log.info("\n=== 智能问答工作流执行完成 ===");
                   callback.onComplete();
 
                })
                .doOnError(error -> {
                   log.error("智能问答工作流执行错误: " + error.getMessage(), error);
                   callback.onError(error);
 
                   
                })
                .subscribe();
          callback.registerDisposable(disposable, planAgent, requestId);
 
          
          
       } catch (Exception e) {
          log.error("Rag stream failed sessionId={},requestId={}", sessionId,requestId, e);
          throw new AIRuntimeException("Rag stream failed", e);
 
       } finally {
        
       }
    }
```

## 3 spring sse消息发送器接口

```java
public class FluxRagStreamCallback implements FluxStreamCallback {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(FluxRagStreamCallback.class);
    private final SseEmitter emitter;
    /** 请求方传入的题目 ID（评测题原样回传），随 trace 事件一并推给前端 */
    private final String requestId;

    public FluxRagStreamCallback(SseEmitter emitter, String requestId) {
        this.emitter = emitter;
        this.requestId = requestId;
    }
    
    /**
     * 错误：以错误终止 emitter。前端 EventSource 会触发 onerror 回调。
     */
    @Override
    public void onError(Throwable error) {
       emitter.completeWithError(error);
    }

    /**
     * 推送 LLM 流式 token。SSE 默认事件（不指定 event 名）。
     */
    @Override
    public void onEvent(String token) {
        try {
            emitter.send(SseEmitter.event().data(token));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    
    /**
     * 流结束：发 done 事件并 close emitter。
     */
    @Override
    public void onComplete() {
        try {
//            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    
    
    public void registerDisposable(Disposable disposable, AIPlanAgent planAgent,String requestId){
       // 使用 AtomicReference 安全地在不同线程间传递 Disposable
     
       // 注册 SseEmitter 监听器，在连接结束或异常时取消底层 Flux
       emitter.onCompletion(() -> {
          Disposable d = disposable;
          if (d != null && !d.isDisposed()) {
             planAgent.shutdown();
             d.dispose();
             log.info("Client completed/disconnected, disposed flux subscription id={}", requestId);
          }
          
          
       });
       emitter.onTimeout(() -> {
          Disposable d = disposable;
          if (d != null && !d.isDisposed()) {
             planAgent.shutdown();
             d.dispose();
             log.info("SSE timeout, disposed flux subscription id={}", requestId);
          }
       });
       emitter.onError((e) -> {
          Disposable d = disposable;
          if (d != null && !d.isDisposed()) {
             planAgent.shutdown();
             d.dispose();
             
             log.info("SSE error or client abort, disposed flux subscription id={}", requestId);
          }
       });
    }
}
```

## 4 禁用nginx sse缓存

注意：nginx会自动缓存sse stream消息，如果后续无消息到来时，导致浏览器端迟迟收不到缓存中的消息数据，可通过以下配置禁用nginx对sse服务的缓存：

```java
response.setHeader("X-Accel-Buffering", "no");
    response.setHeader("Cache-Control", "no-cache");
```

## 5 问答和会话处理

问答时，后端通过ServerEvent向前端传递流式消息，前端需进行正确的消息类型处理和展示；打开会话历史，前端接收到后端传递过来的会话历史消息数据，同样需进行正确消息类型处理和展示，并且能够进行续问续答；问答和打开历史会话，需正确的呈现问答过程的执行轨迹。

### 5.1 问答

![image-20260726194950925](images\agent\qa.png)

#### 5.1.1 提交stream请求：

```typescript
stream: (
  id: string,
  question: string,
  sessionId: string | undefined,
  userId: string | undefined,
  model: string | undefined,
  onChunk: (text: string, contentType?: number) => void,
  onCitations?: (citations: any) => void,
  onTrace?: (trace: ChatStreamTraceEvent) => void,
  // onRefusalText?: (payload: ChatStreamRefusalTextEvent | string) => void,
  onRefusalFlag?: (payload: ChatStreamRefusalFlagEvent) => void,
  // onConfidence?: (payload: ChatStreamConfidenceEvent ) => void,
  onReasoning?: (reasoningText: string, contentType?: number) => void,
  onHitl?: (hitlData: string, hitlTaskId: string, contentType?: number) => void,
  onStep?: () => void,
  domain?: string,
  signal?: AbortSignal,
  thinking?: boolean
): Promise<void> => {
  return fetch(`${BASE}/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      id,
      question,
      sessionId,
      userId,
      domain,
      ...(model ? { model } : {}),
      ...(thinking ? { thinking } : {})
    }),
    signal,
  }).then(res => {
    const reader = res.body!.getReader()
    const decoder = new TextDecoder()
    let lineBuffer = ''
      // let isDeepseekBegin = false;
      let finishReason: string | null = null;
      // let accumulatedContent = '';
      const removeDataprefix = (line: string) => {
          return line.startsWith('data:') ? line.slice(5).trimStart() : line;
      }
      const terminalIntercept = (finishReason: string | null) => {
          if(finishReason && finishReason.toLowerCase() != 'stop' && finishReason != 'null'){
              //对话异常终止
              return  true;
          }
          return false;
      }
      const handleRefuse = (refuse: any) => {
          
             
          // onRefusalText?.(refuse.data)    
          onRefusalFlag?.(refuse)
          // onConfidence?.(refuse.confidence)
              
           

      }
      const handleEventBlock = (lineData: StreamEventData) => {

          if(lineData.type === StreamConstants.TYPE_RAG_KNOWLEDGE){
              onCitations?.(lineData)
          }
          else if(lineData.contentType === StreamConstants.CONTENT_TYPE_REASONING_CONTENT){
              if(lineData.data){
                  onReasoning?.(lineData.data, lineData.contentType);
              }
          }
          else if(lineData.type === StreamConstants.TYPE_STEP){
             //当出现TYPE_STEP类型消息时，表示开始一个新的步骤，输出步骤号
             onStep?.();
          }
          else if(lineData.type === StreamConstants.TYPE_DATA){
              if(lineData.data){
                  onChunk(lineData.data, lineData.contentType);
              }
          }
          else if(lineData.type === StreamConstants.TYPE_HITL){
              let hitlTaskId = lineData?.hitlTaskId;
              console.log('hitlTaskId', hitlTaskId);
              if(lineData.data && hitlTaskId){
                  onHitl?.(lineData.data, hitlTaskId, lineData.contentType);
              } else if(lineData.data){
                  onChunk(lineData.data, lineData.contentType);
              }
          }
          else if(lineData.type === StreamConstants.TYPE_AGENT){
              if(lineData.data){
                  onChunk(lineData.data, lineData.contentType);
              }
          }
          else if(lineData.type === StreamConstants.TYPE_REFUSAL){
              handleRefuse(lineData)
          }
          else if(lineData.type === StreamConstants.TYPE_TRACE){
              onTrace?.(lineData.data)
          }
          else{
              if(lineData.data )
                  onChunk(lineData.data, lineData.contentType);
          }
          

          if(!finishReason && lineData.finishReason){
              finishReason = lineData.finishReason;
          }
          if(terminalIntercept(finishReason)){
              //对话异常终止
              
              onChunk(`....对话异常终止,终止原因：`+finishReason);
              finishReason = null;
          }
      }
     

    const read = (): Promise<void> =>
      reader.read().then(({ done, value }) => {
          if (done) {
              // 处理缓冲区中剩余的数据
              if (lineBuffer.trim()) {
                  try {
                      // 按行分割数据
                      const lines = lineBuffer.split('\n');
                      
                      lines.forEach(line => {
                          
                          line = removeDataprefix(line);
                           
                          const lineData = JSON.parse(line.trim());
                          handleEventBlock(lineData);
                          
                           
                      });
                      
                  } catch (e) {
                      // 如果不是JSON格式，作为普通文本处理
                      onChunk('处理数据报错：'+lineBuffer +'<br/>'+ e);
                  }
              }
              
              return;
          }

          // 解码当前chunk
          const chunk = decoder.decode(value, { stream: true });

          
          // 将新数据添加到行缓冲区
          if(lineBuffer) {
              lineBuffer += chunk;
          }
          else{
              lineBuffer = chunk;
          }

          // 按行分割数据
          const lines = lineBuffer.split('\n');

          // 保留最后一行（可能是不完整的）在缓冲区中
          lineBuffer = lines.pop() || '';

          // 处理完整的行
          lines.forEach(line => {
              line = removeDataprefix(line);
              if(line.trim().length == 0)
                  return;
 
              try {
                  const jsonData = JSON.parse(line);
                  if(jsonData.length) {
                      for (let i = 0; i < jsonData.length; i++) {
                          const lineData = jsonData[i];
                          handleEventBlock(lineData);

                      }
                  }
                  else{
                      handleEventBlock(jsonData);
                  }
              } catch (e) {
                  // 如果不是JSON格式，作为普通文本处理
                  // accumulatedContent += line;
                  // handleEventBlock( accumulatedContent);
                  onChunk(line);
              }
              
          });

          // 继续读取
          return read();
      })
    return read()
  })
},
```

#### 5.1.2 处理问答消息示例

```typescript
if(lineData.type === StreamConstants.TYPE_RAG_KNOWLEDGE){ //rag知识类型
    onCitations?.(lineData)
}
else if(lineData.contentType === StreamConstants.CONTENT_TYPE_REASONING_CONTENT){
    if(lineData.data){
        onReasoning?.(lineData.data, lineData.contentType);
    }
}
else if(lineData.type === StreamConstants.TYPE_STEP){
   //当出现TYPE_STEP类型消息时，表示开始一个新的步骤，输出步骤号
   onStep?.();
}
else if(lineData.type === StreamConstants.TYPE_DATA){
    if(lineData.data){
        onChunk(lineData.data, lineData.contentType);
    }
}
else if(lineData.type === StreamConstants.TYPE_HITL){
    let hitlTaskId = lineData?.hitlTaskId;
    console.log('hitlTaskId', hitlTaskId);
    if(lineData.data && hitlTaskId){
        onHitl?.(lineData.data, hitlTaskId, lineData.contentType);
    } else if(lineData.data){
        onChunk(lineData.data, lineData.contentType);
    }
}
else if(lineData.type === StreamConstants.TYPE_AGENT){
    if(lineData.data){
        onChunk(lineData.data, lineData.contentType);
    }
}
else if(lineData.type === StreamConstants.TYPE_REFUSAL){
    handleRefuse(lineData)
}
else if(lineData.type === StreamConstants.TYPE_TRACE){
    onTrace?.(lineData.data)
}
else{
    if(lineData.data )
        onChunk(lineData.data, lineData.contentType);
}


if(!finishReason && lineData.finishReason){
    finishReason = lineData.finishReason;
}
if(terminalIntercept(finishReason)){
    //对话异常终止
    
    onChunk(`....对话异常终止,终止原因：`+finishReason);
    finishReason = null;
}
```

#### 5.1.3 消息类型常量

```typescript
export const StreamConstants = {
      /**答案内容*/
      CONTENT_TYPE_CONTENT: 0,
      /** 思维链内容 */
      CONTENT_TYPE_REASONING_CONTENT: 1, 
      
    
      /** 数据消息 */
      TYPE_DATA: 0,
     
    
      /** trace信息，traceId */
      TYPE_TRACE: 2,
      /** 拒绝消息 */
      TYPE_REFUSAL: 3,
    
      /** 知识库资料消息 */
      TYPE_RAG_KNOWLEDGE: 5,
    
      /** 专门用于提示有新步骤的消息 */
      TYPE_STEP: 6,
        /** 智能体补充的消息 */
        TYPE_AGENT: 7,
    /**
     * 人工介入消息
     */
    TYPE_HITL:8
} as const;
```

### 5.2 历史会话

![](images\agent\session.png)

#### 5.2.1 历史会话处理代码

```typescript
/**
 * 切换会话
 */
const handleSwitchSession = async (sid: string) => {
  console.log('Switching session to:', sid)
  sessionStore.switchSession(sid)
  answerHistory.value = []

  // 从后端加载该会话的历史消息
  try {
    const history = await sessionStore.loadHistory(sid)
    if (history && history.length > 0) {
      // 按 requestId 分组合并同一问答对的消息（一个问题 + 多轮答案）
      const groups = new Map<string, {
        question: string
        rounds: Array<{
          reasoningContent: string
          answer: string
          reasoningVisible: boolean
          isHitl?: boolean
          hitlTaskId?: string
          hitlResponded?: boolean
        }>
        citations: Citation[]
        confidence: number
        isRefusal: boolean
        time: string
        msgId: string
      }>()
      const requestOrder: string[] = []

      for (const msg of history) {
        const rid = msg.requestId || msg.msgId
        if (!groups.has(rid)) {
          requestOrder.push(rid)
          groups.set(rid, {
            question: '',
            rounds: [],
            citations: [],
            confidence: 0,
            isRefusal: false,
            time: msg.createTime || '',
            msgId: msg.msgId
          })
        }
        const group = groups.get(rid)!

        if (msg.messageType === '6') { // 6: rag（知识数据）
          group.citations = normalizeCitations(msg.message?.rag)
          group.confidence = msg.message?.confidence || 0
        } else if (msg.messageType === '8') { // 8: 用户输入原始问题
          group.question = msg.message?.question || ''
          group.msgId = msg.msgId
        }
        else if (msg.messageType === '20') { // 20: 智能体人工介入消息：大模型提供输入内容，需要人工介入处理
          const hitlTaskReason = msg.message?.hitlTaskReason || ''
          const hitlTaskId = msg.message?.hitlTaskId || ''

          group.rounds.push({
            reasoningContent: '',
            answer: hitlTaskReason,
            reasoningVisible: false,
            isHitl: true,
            hitlTaskId: hitlTaskId,
            hitlResponded: true
          })
          group.msgId = msg.msgId
        }
        else if (msg.messageType === '7') { // 7: refuse(拒答)
          group.rounds.push({
            reasoningContent: '',
            answer: msg.refuse || '',
            reasoningVisible: false
          })
          group.isRefusal = true
          group.confidence = msg.confidence || 0
        } else if (msg.messageType === '15') { // 15: toolsearch（含思维链+内容）
          const reasoningData = msg.tokenMetrics?.reasoningData
          const content = msg.message?.content
          if (reasoningData || content) {
            // 避免与上一轮完全重复
            const lastRound = group.rounds[group.rounds.length - 1]
            if (lastRound && lastRound.answer === content && lastRound.reasoningContent === (reasoningData ? String(reasoningData) : '')) {
              // 重复，跳过
            } else {
              group.rounds.push({
                reasoningContent: reasoningData ? String(reasoningData) : '',
                answer: content || '',
                reasoningVisible: !!reasoningData
              })
            }
          }
        }
        else if (msg.messageType === '5') { // 5: trace（智能体生成消息：Routing未匹配到智能体提示消息，Tool检索召回没有返回LLM消息由智能体产生一个提示消息等）
           
          const content = msg.message?.text
          if (  content) {
            // 避免与上一轮完全重复
            const lastRound = group.rounds[group.rounds.length - 1]
            if (lastRound && lastRound.answer === content ) {
              // 重复，跳过
            } else {
              group.rounds.push({
                reasoningContent:  '',
                answer: content || '',
                reasoningVisible: false
              })
            }
          }
        }
        else if (msg.messageType === '1') { // 1: 智能体输出结果消息
          if(msg.agentNodeType === 'sequence'){
            console.log('Sequence node:', msg)
          }
          else{
            const content = msg.message?.content || ''
            const reasoningData = msg.tokenMetrics?.reasoningData
            // 避免与上一轮完全重复
            const lastRound = group.rounds[group.rounds.length - 1]
            if (lastRound && lastRound.answer === content && lastRound.reasoningContent === (reasoningData ? String(reasoningData) : '')) {
              // 重复，跳过
            } else if (reasoningData || content) {
              group.rounds.push({
                reasoningContent: reasoningData ? String(reasoningData) : '',
                answer: content,
                reasoningVisible: !!reasoningData
              })
            }
            group.msgId = msg.msgId
          }
          
        }
      }

      const messages: AnswerItem[] = requestOrder.map(rid => {
        const g = groups.get(rid)!

        // 如果没有提取到任何 round，创建一个空的兼容 round
        if (g.rounds.length === 0) {
          g.rounds.push({
            reasoningContent: '',
            answer: '',
            reasoningVisible: false
          })
        }

        const flatAnswer = g.rounds.map(r => r.answer).filter(Boolean).join('\n\n')
        const flatReasoning = g.rounds.map(r => r.reasoningContent).filter(Boolean).join('\n\n')
        const hasReasoning = g.rounds.some(r => r.reasoningContent.trim().length > 0)

        return {
          id: g.msgId,
          question: g.question,
          answer: flatAnswer,
          citations: g.citations,
          isRefusal: g.isRefusal,
          confidence: g.confidence,
          time: g.time,
          streaming: false,
          hasAnswer: !g.isRefusal && !!flatAnswer.trim(),
          traceData: null,
          reasoningContent: flatReasoning,
          hasReasoning: hasReasoning,
          reasoningVisible: hasReasoning,
          rounds: g.rounds
        }
      })

      answerHistory.value = messages
    }
  } catch (e) {
    console.error('加载历史消息失败:', e)
  }

  await nextTick()
  scrollToBottom()
}
```

### 5.3 执行轨迹

执行轨迹：呈现一个会话的执行轨迹，包括用户输入，提示词，技能，工具调用，mcp调用，人工干预执行情况以及token消耗情况；消息原始报文，一般按照会话的请求轮次组织消息。

![image-20260726195137619](images\agent\trace1.png)

![](images\agent\trace2.png)

### 5.4 会话管理组件

#### 5.4.1 初始化会话管理组件

```java
AgentSessionService agentSessionService = new AgentSessionServiceImpl();
agentSessionService.setDatasource("sessionds"); //设置会话管理数据源，可以是数据库或者Clickhouse
agentSessionService.setHitlDatasource("sessionds"); //设置人工任务管理数据源，可以是数据库，不能是Clickhouse
PropertiesContainer propertiesContainer = PropertiesUtil.getPropertiesContainer("bootstrap.properties");
if(propertiesContainer.getBooleanProperty("enable.hitl.task",false)){
    //初始化人工任务调用监听器
    HitlTaskHelper.getHitlTaskHelper()
          .setAgentSessionService(agentSessionService)
          .setRedisChannel("test", RedisHitlTaskCallListener.DEFAULT_CHANNEL) //设置redis消息通道
          .init();
}
```

初始化数据源：支持mysql、Postgresql、Oracle、dm、sqlite、sqlserver、Clickhouse、磐维等关系数据库

mysql

```java
SQLUtil.startPool("sessionds",//数据源名称
       "com.mysql.cj.jdbc.Driver",//mysql驱动
       "jdbc:mysql://192.168.137.1:3306/bboss?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true",//mysql链接串
       "root", "123456",//数据库账号和口令
       "select 1 " //数据库连接校验sql
);
```

Clickhouse

```java
DBConf tempConf = new DBConf();
tempConf.setPoolname("sessionds");
tempConf.setDriver("com.clickhouse.jdbc.ClickHouseDriver");
tempConf.setJdbcurl("jdbc:clickhouse:http://101.13.6.4:28123,101.13.6.7:28123,101.13.4.6:28123/visualops?b.enableBalance=true&b.balance=roundbin");
tempConf.setUsername("default");
tempConf.setPassword("123456");
tempConf.setValidationQuery("select 1 ");
//tempConf.setTxIsolationLevel("READ_COMMITTED");
tempConf.setJndiName("jndi-visualops" );
tempConf.setInitialConnections(10);
tempConf.setMinimumSize(10);
tempConf.setMaximumSize(20);
tempConf.setUsepool(true);

tempConf.setShowsql(true);
SQLManager. startPool(tempConf);
```

#### 5.4.2 会话查询

查询会话列表

```java
String[] domains = agentSessionCondition.getDomains(); //会话的业务领域条件
if(domains == null){
    domains = new String[]{AgentConstants.ECOP_CONFIG_AGENT_DOMAIN};
    agentSessionCondition.setDomains(domains);
}
String title = agentSessionCondition.getTitle(); //会话标题
if(StringUtils.isNotEmpty(title)){
    agentSessionCondition.setTitle("%"+title+"%");
}
agentSessionCondition.setSortKey("createTime"); //会话查询排序字段
agentSessionCondition.setSortDesc(true); //排序顺序
List<AgentSession> sessions = agentSessionService.queryListAgentSessions(agentSessionCondition); //查询会话列表

```

分页列表查询接口：

```java
/**
    * 分页查询会话记录
    * @param conditions
    * @param offset
    * @param pagesize
    * @return
    * @throws AgentSessionException
    */
ListInfo queryListInfoAgentSessions(AgentSessionCondition conditions, long offset, int pagesize)
       throws AgentSessionException
```

#### 5.4.3 删除会话

```java
@DeleteMapping("/sessions/{sessionId}")
public ResponseEntity<ApiResponse<String>> deleteSession(
        @PathVariable("sessionId") String sessionId) {
    agentSessionService.deleteAgentSession(sessionId);
    return ResponseEntity.ok(ApiResponse.ok(sessionId));
}
```

批量删除会话：

```java
void deleteBatchAgentSession(String... sessionids) throws AgentSessionException
```

#### 5.4.4 获取会话消息

```java
/**
 * 获取会话历史消息
 * <p>
 * GET /api/chat/history/{sessionId}
 */
@Operation(summary = "获取会话历史消息", description = "根据会话ID查询历史消息记录")
@GetMapping("/history/{sessionId}")
public ResponseEntity<ApiResponse<List<SessionMessage>>> getHistory(
        @PathVariable("sessionId") String sessionId) {
    List<SessionMessage> messages = agentSessionService.queryListSessionMessages(sessionId);
    return ResponseEntity.ok(ApiResponse.ok(messages));
}
```

以下是一个的会话消息报文示例：

```json
[
  {
    "msgId": "afa8fb52232b467ab5cec74c42558a10",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:26:56.213",
    "sessionId": "session-1785158793814",
    "agentId": "init",
    "parentAgentId": "ragQAFlow",
    "messageType": "8",
    "seqNo": 1,
    "message": {
      "role": "userinput",
      "question": "代码评审和问题修复"
    },
    "tokenMetrics": {
      "totalTokens": 0,
      "promptTokens": 0,
      "promptCachedTokens": 0,
      "promptCacheHitTokens": 0,
      "promptCacheMissTokens": 0,
      "promptTextTokens": 0,
      "completionTokens": 0,
      "completionReasoningTokens": 0,
      "completionTextTokens": 0,
      "startTime": 1785158816212
    },
    "role": "userinput"
  },
  {
    "msgId": "10ef26bc7a854a3183ad97ca403e6f85",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:26:56.218",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "3",
    "seqNo": 2,
    "agentNodeType": "standard",
    "message": {
      "role": "system",
      "content": "你是一个 Java 代码审查助手，可以审查 Java 代码并给出修改建议，还可以将修复后的代码保存到源文件中。 长期规则： - 如果用户提交 Java 代码并要求审查，先调用 Skill 工具加载 code-review-skill。 - 保存修改代码前请调用工具hitlTaskTool进行人工确认。- 加载技能书后，再按照技能书里的审查顺序审查java代码。 - 优先指出 bug、安全风险、边界条件、异常处理和缺失测试。 - 如果信息不足，要说明缺少哪些上下文，不要编造项目背景。 - 不要输出与代码审查无关的泛泛建议。 输出要求： - 用中文回答。 - 使用 Markdown。 - 先给总体结论，再列主要问题，最后给测试建议和下一步。"
    },
    "role": "system"
  },
  {
    "msgId": "c3299655edd24187be1f2df27590dfd6",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:26:56.219",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "2",
    "seqNo": 3,
    "agentNodeType": "standard",
    "message": {
      "role": "user",
      "content": "请评审代码并修复问题,java文件路径：/home/ecs/ai/knowledge-base-demo/hitl/HitlTaskcallTool.java"
    },
    "role": "user"
  },
  {
    "msgId": "97d567fe526c45feb479f8f99d91c236",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:26:56.22",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "9",
    "seqNo": 4,
    "agentNodeType": "standard",
    "message": {
      "input": {
        "stream": true,
        "messages": [
          {
            "role": "system",
            "content": "你是一个 Java 代码审查助手，可以审查 Java 代码并给出修改建议，还可以将修复后的代码保存到源文件中。 长期规则： - 如果用户提交 Java 代码并要求审查，先调用 Skill 工具加载 code-review-skill。 - 保存修改代码前请调用工具hitlTaskTool进行人工确认。- 加载技能书后，再按照技能书里的审查顺序审查java代码。 - 优先指出 bug、安全风险、边界条件、异常处理和缺失测试。 - 如果信息不足，要说明缺少哪些上下文，不要编造项目背景。 - 不要输出与代码审查无关的泛泛建议。 输出要求： - 用中文回答。 - 使用 Markdown。 - 先给总体结论，再列主要问题，最后给测试建议和下一步。"
          },
          {
            "role": "user",
            "content": "请评审代码并修复问题,java文件路径：/home/ecs/ai/knowledge-base-demo/hitl/HitlTaskcallTool.java"
          }
        ],
        "tool_choice": "auto",
        "model": "glm-5.2",
        "stream_options": {
          "include_usage": true
        },
        "thinking": {
          "type": "disabled"
        },
        "tools": [
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "Skill",
              "description": "Execute a skill within the main conversation\r\n\r\n<skills_instructions>\r\nWhen users ask you to perform tasks, check if any of the available skills below can help complete the task more effectively. Skills provide specialized capabilities and domain knowledge.\r\n\r\nHow to use skills:\r\n- Invoke skills using this tool with the skill name only (no arguments)\r\n- When you invoke a skill, you will see <command-message>The \"{name}\" skill is loading</command-message>\r\n- The skill's prompt will expand and provide detailed instructions on how to complete the task\r\n\r\nNOTE: Response always starts start with the base directory of the skill execution environment. You can use this to retrieve additional files of call shell commands.\r\nSkill description follows after the base directory line.\r\n\r\nImportant:\r\n- Only use skills listed in <available_skills> below\r\n- Do not invoke a skill that is already running\r\n</skills_instructions>\r\n\r\n<available_skills>\r\n<skill>\n\t<name>code-review-skill</name>\n\t<description>按固定流程先审查Java代码，优先发现 bug、安全风险、边界条件、可维护性问题和缺失测试;审查完毕后，修复发现的问题,经人工确认后保存修复代码到原始文件。当用户要求 review、审查、检查 Java 代码或判断代码有没有风险以及修复问题时使用。</description>\n</skill>\n\r\n</available_skills>",
              "parameters": {
                "type": "object",
                "properties": {
                  "skillName": {
                    "type": "string",
                    "description": "The skill name (no arguments). E.g., \"pdf\" or \"xlsx\""
                  }
                },
                "required": [
                  "skillName"
                ]
              }
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "hitlTaskTool",
              "description": "人工介入工具：当AI无法独立完成任务、遇到关键决策点、需要人工审批或验证时调用；适用于：1.复杂问题需要人类专业判断 2.敏感操作需要人工确认 3.任务执行结果不符合预期需要人工介入调整 4.超出AI权限范围的操作；5.需要人工审核的操作；6.需要人工确认的操作。上下文内容要求：精简聚焦，包含三要素——已执行步骤、卡住原因、建议关注要点，让人类在3秒内快速理解并做出决策。",
              "parameters": {
                "type": "object",
                "properties": {
                  "hitlTaskReason": {
                    "type": "string",
                    "description": "人工介入原因，需包含：1.任务背景与已执行步骤 2.当前卡住的具体原因（技术障碍/权限限制/信息缺失等）3.建议人类关注的关键点或待决策事项 4.期望人类提供的具体帮助；格式清晰，精简聚焦，便于人类快速理解。"
                  }
                },
                "required": [
                  "hitlTaskReason"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "copyFile",
              "description": "拷贝文件或目录到目标路径，支持文件和目录的拷贝，目标父目录不存在时自动创建",
              "parameters": {
                "type": "object",
                "properties": {
                  "source": {
                    "type": "string",
                    "description": "源文件或目录路径"
                  },
                  "target": {
                    "type": "string",
                    "description": "目标文件或目录路径"
                  },
                  "overwrite": {
                    "type": "boolean",
                    "description": "目标已存在时是否覆盖，默认false"
                  }
                },
                "required": [
                  "source",
                  "target"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "fileExists",
              "description": "检查指定路径的文件或目录是否存在",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "文件或目录路径"
                  }
                },
                "required": [
                  "path"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "detectFileEncoding",
              "description": "识别文件内容的字符编码，支持通过BOM和字节特征检测UTF-8、UTF-16、GBK等常见编码",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "文件路径"
                  }
                },
                "required": [
                  "path"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "readDirectoryFiles",
              "description": "遍历指定目录，读取目录下所有文件的内容，返回文件路径与内容的集合",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "目录路径"
                  },
                  "recursive": {
                    "type": "boolean",
                    "description": "是否递归遍历子目录，默认false"
                  },
                  "charset": {
                    "type": "string",
                    "description": "字符编码，为空则逐个自动识别"
                  }
                },
                "required": [
                  "path"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "getFileAttributes",
              "description": "获取文件或目录的属性信息，包括大小、修改时间、创建时间、是否隐藏、是否可读可写等",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "文件或目录路径"
                  }
                },
                "required": [
                  "path"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "deleteFile",
              "description": "删除指定的文件或目录，支持递归删除目录及其子内容",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "文件或目录路径"
                  },
                  "recursive": {
                    "type": "boolean",
                    "description": "删除目录时是否递归删除子文件，默认false"
                  }
                },
                "required": [
                  "path"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "createFile",
              "description": "新建文件或目录，若父目录不存在则自动创建",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "文件或目录路径"
                  },
                  "isDirectory": {
                    "type": "boolean",
                    "description": "是否创建目录，true创建目录，false创建文件，默认false"
                  }
                },
                "required": [
                  "path"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "readFile",
              "description": "读取指定文件的内容，支持指定字符编码，未指定时自动识别编码",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "文件路径"
                  },
                  "charset": {
                    "type": "string",
                    "description": "字符编码，如UTF-8、GBK等，为空则自动识别"
                  }
                },
                "required": [
                  "path"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          {
            "type": "function",
            "inputType": null,
            "function": {
              "name": "writeFile",
              "description": "将内容写入到指定文件，支持指定字符编码和追加模式,如果文件不存在，会自动创建",
              "parameters": {
                "type": "object",
                "properties": {
                  "path": {
                    "type": "string",
                    "description": "文件路径"
                  },
                  "content": {
                    "type": "string",
                    "description": "要写入的文件内容"
                  },
                  "charset": {
                    "type": "string",
                    "description": "字符编码，默认UTF-8"
                  },
                  "append": {
                    "type": "boolean",
                    "description": "是否追加写入，默认false覆盖写入"
                  }
                },
                "required": [
                  "path",
                  "content"
                ],
                "additionalProperties": false
              },
              "strict": true
            }
          }
        ]
      },
      "role": "llminput"
    },
    "tokenMetrics": {
      "totalTokens": 0,
      "promptTokens": 0,
      "promptCachedTokens": 0,
      "promptCacheHitTokens": 0,
      "promptCacheMissTokens": 0,
      "promptTextTokens": 0,
      "completionTokens": 0,
      "completionReasoningTokens": 0,
      "completionTextTokens": 0
    },
    "role": "llminput"
  },
  {
    "msgId": "93083152d91146d3a6bbc4e561935a10",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:27:01.58",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "5",
    "seqNo": 5,
    "agentNodeType": "standard",
    "message": {
      "text": "匹配到工具：Skill，准备执行工具。",
      "role": "trace"
    },
    "tokenMetrics": {
      "totalTokens": 0,
      "promptTokens": 0,
      "promptCachedTokens": 0,
      "promptCacheHitTokens": 0,
      "promptCacheMissTokens": 0,
      "promptTextTokens": 0,
      "completionTokens": 0,
      "completionReasoningTokens": 0,
      "completionTextTokens": 0
    },
    "role": "trace"
  },
  {
    "msgId": "f3064bb109f14676bf04a9db49466699",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:27:01.581",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "15",
    "seqNo": 6,
    "agentNodeType": "standard",
    "message": {
      "role": "toolsearch",
      "content": "匹配到工具：Skill，准备执行工具。"
    },
    "tokenMetrics": {
      "maas": "zhipu",
      "model": "glm-5.2",
      "totalTokens": 1830,
      "promptTokens": 1816,
      "promptCachedTokens": 0,
      "promptCacheHitTokens": 0,
      "promptCacheMissTokens": 0,
      "promptTextTokens": 0,
      "completionTokens": 14,
      "completionReasoningTokens": 0,
      "completionTextTokens": 0
    },
    "role": "toolsearch"
  },
  {
    "msgId": "ff9fc0a798714fe7bb8caf523972fa2a",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:27:01.583",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "0",
    "seqNo": 7,
    "agentNodeType": "standard",
    "message": {
      "role": "assistant",
      "tool_calls": [
        {
          "id": "call_ec91467b2f714d46961dd620",
          "index": 0,
          "type": "function",
          "function": {
            "name": "Skill",
            "arguments": "{\"skillName\":\"code-review-skill\"}"
          }
        }
      ]
    },
    "role": "assistant"
  },
  {
    "msgId": "e6962d268fbc4628b0d6a3dc49bc09c9",
    "elapsed": 1,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:27:01.586",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "17",
    "seqNo": 8,
    "agentNodeType": "standard",
    "message": {
      "toolName": "Skill",
      "id": "call_ec91467b2f714d46961dd620",
      "type": "function",
      "index": 0,
      "toolCallArgs": {
        "skillName": "code-review-skill"
      },
      "role": "toolcall",
      "toolCallResponse": "Base directory for this skill: /home/ecs/ai/knowledge-base-demo/ai-knowledge-agent-backend/resources/skills/code-review-skill\n\n# Java Code Review Skill\n这个 Skill 用来审查 Java 代码、修复问题，生成问题修复代码，经人工确认后再保存到原始文件。\n\n它不是简单评价“代码好不好”，而是要求 Agent 按固定顺序检查：\n1. 明显 bug\n2. 空值、空集合、非法输入等边界条件\n3. 安全风险，例如硬编码密钥、敏感信息泄露、权限绕过\n4. 异常处理和日志\n5. 可维护性问题\n6. 缺失的测试场景\n\n## 工作流程\n\n### Step 1：先判断代码场景\n\n先识别代码属于哪一类：\n- Controller/API 入参处理\n- Service 业务逻辑\n- Repository/数据访问\n- 工具类\n- 配置类\n- 测试代码\n不同类型代码的审查重点不同。\n\n### Step 2：按风险优先级审查\n\n优先输出会导致线上问题的内容。\n\n输出顺序：\n1. 高风险问题\n2. 中风险问题\n3. 低风险建议\n4. 建议补充的测试\n\n不要把格式问题放在 bug 前面。\n\n### Step 3：调用代码审查工具\n\n当用户提供 Java 代码时，当存在 `reviewJavaCode` 工具时，才调用 `reviewJavaCode` 工具。\n\n工具返回的是基础审查报告。你可以在报告基础上补充解释，但不要改写成空泛建议，也不要删除审查路径。\n\n\n## step 4:问题修复\n\n- 请生成修复后的代码\n- 调用文件处理工具保存修改后的代码\n- 修复问题时，要指出问题的位置和原因。\n- 修复后，要检查是否解决了问题。\n- 保存代码到原文件前，需要调用人工介入工具hitlTaskTool，进行人工审核确认，确认后再调用文件操作工具保存代码到原始文件，否则不要保存代码到原文件。\n\n### Step 5：输出格式\n\n输出结构：\n- 代码审查报告\n- 审查路径\n- 总体结论\n- 主要问题\n- 建议补充的测试\n- 修改结果\n\n## 边界\n\n- 不确定的问题要说明“不确定”，不要编造上下文。\n- 没有看到完整工程时，不要断言一定会出问题。\n- 涉及安全、权限、数据删除、支付、订单状态流转时，要提高风险级别。\n- 如果代码片段太短，要指出还需要哪些上下文。\n- 如果信息不足，要说明缺少哪些上下文，不要编造项目背景。 \n- 不要输出与代码审查无关的泛泛建议。\n\n\n## 输出要求：\n- 用中文回答\n- 使用 Markdown。\n\n## 参考资料\n\n读取文件 `references/checklist.md`补充更细的检查项。"
    },
    "tokenMetrics": {
      "totalTokens": 0,
      "promptTokens": 0,
      "promptCachedTokens": 0,
      "promptCacheHitTokens": 0,
      "promptCacheMissTokens": 0,
      "promptTextTokens": 0,
      "completionTokens": 0,
      "completionReasoningTokens": 0,
      "completionTextTokens": 0,
      "startTime": 1785158821585,
      "endTime": 1785158821586
    },
    "role": "toolcall"
  },
  {
    "msgId": "50a6955e672047abaf6fa53e11c5671c",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:27:01.587",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "4",
    "seqNo": 9,
    "agentNodeType": "standard",
    "message": {
      "role": "tool",
      "tool_call_id": "call_ec91467b2f714d46961dd620",
      "content": "Base directory for this skill: /home/ecs/ai/knowledge-base-demo/ai-knowledge-agent-backend/resources/skills/code-review-skill\n\n# Java Code Review Skill\n这个 Skill 用来审查 Java 代码、修复问题，生成问题修复代码，经人工确认后再保存到原始文件。\n\n它不是简单评价“代码好不好”，而是要求 Agent 按固定顺序检查：\n1. 明显 bug\n2. 空值、空集合、非法输入等边界条件\n3. 安全风险，例如硬编码密钥、敏感信息泄露、权限绕过\n4. 异常处理和日志\n5. 可维护性问题\n6. 缺失的测试场景\n\n## 工作流程\n\n### Step 1：先判断代码场景\n\n先识别代码属于哪一类：\n- Controller/API 入参处理\n- Service 业务逻辑\n- Repository/数据访问\n- 工具类\n- 配置类\n- 测试代码\n不同类型代码的审查重点不同。\n\n### Step 2：按风险优先级审查\n\n优先输出会导致线上问题的内容。\n\n输出顺序：\n1. 高风险问题\n2. 中风险问题\n3. 低风险建议\n4. 建议补充的测试\n\n不要把格式问题放在 bug 前面。\n\n### Step 3：调用代码审查工具\n\n当用户提供 Java 代码时，当存在 `reviewJavaCode` 工具时，才调用 `reviewJavaCode` 工具。\n\n工具返回的是基础审查报告。你可以在报告基础上补充解释，但不要改写成空泛建议，也不要删除审查路径。\n\n\n## step 4:问题修复\n\n- 请生成修复后的代码\n- 调用文件处理工具保存修改后的代码\n- 修复问题时，要指出问题的位置和原因。\n- 修复后，要检查是否解决了问题。\n- 保存代码到原文件前，需要调用人工介入工具hitlTaskTool，进行人工审核确认，确认后再调用文件操作工具保存代码到原始文件，否则不要保存代码到原文件。\n\n### Step 5：输出格式\n\n输出结构：\n- 代码审查报告\n- 审查路径\n- 总体结论\n- 主要问题\n- 建议补充的测试\n- 修改结果\n\n## 边界\n\n- 不确定的问题要说明“不确定”，不要编造上下文。\n- 没有看到完整工程时，不要断言一定会出问题。\n- 涉及安全、权限、数据删除、支付、订单状态流转时，要提高风险级别。\n- 如果代码片段太短，要指出还需要哪些上下文。\n- 如果信息不足，要说明缺少哪些上下文，不要编造项目背景。 \n- 不要输出与代码审查无关的泛泛建议。\n\n\n## 输出要求：\n- 用中文回答\n- 使用 Markdown。\n\n## 参考资料\n\n读取文件 `references/checklist.md`补充更细的检查项。"
    },
    "role": "tool"
  },
  
  {
    "msgId": "8603d3c4f9794d3b8fd4a04206740c34",
    "elapsed": 0,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:27:12.442",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "15",
    "seqNo": 19,
    "agentNodeType": "standard",
    "message": {
      "role": "toolsearch",
      "content": "匹配到工具：fileExists，准备执行工具。"
    },
    "tokenMetrics": {
      "maas": "zhipu",
      "model": "glm-5.2",
      "totalTokens": 4248,
      "promptTokens": 4216,
      "promptCachedTokens": 2432,
      "promptCacheHitTokens": 0,
      "promptCacheMissTokens": 0,
      "promptTextTokens": 0,
      "completionTokens": 32,
      "completionReasoningTokens": 0,
      "completionTextTokens": 0
    },
    "role": "toolsearch"
  },
  {
    "msgId": "bd18b1e983d1402abfe4f750813e4a8c",
    "elapsed": 9614,
    "requestId": "q_req_1785158814712_uckmqb7g",
    "createTime": "2026-07-27T21:28:01.51",
    "sessionId": "session-1785158793814",
    "agentId": "hitlSequenceAgent-1",
    "parentAgentId": "hitlSequenceAgent",
    "messageType": "1",
    "seqNo": 31,
    "agentNodeType": "standard",
    "message": {
      "role": "assistant",
      "content": "人工确认结果为**拒绝**，修复代码不会保存到原文件。\n\n---\n\n## 最终结论\n\n审查已完成，共发现 **2 个中风险问题** 和 **2 个低风险建议**：\n\n| 优先级 | 问题 | 状态 |\n|--------|------|------|\n| 🔴 中风险 | `createHitlCallTask` 返回 null 时 status 错误标记为 `\"success\"`，可能导致任务静默丢失 | 已给出修复方案，**人工拒绝保存** |\n| 🔴 中风险 | catch 块中拼接 `e.getMessage()` 到返回结果，存在信息泄露风险 | 已给出修复方案，**人工拒绝保存** |\n| 🟡 低风险 | `buildResult` 的 status 参数缺少合法值校验 | 建议后续改进 |\n| 🟡 低风险 | 缺少单元测试覆盖 | 建议补充 |\n\n### 下一步建议\n\n1. **如果对修复方案有疑问**，可以针对具体问题讨论调整方案后再重新确认保存。\n2. **如果希望部分修复**（例如只修问题1不修问题2），可以告诉我，我会调整修复内容后重新提交确认。\n3. **建议补充单元测试**，覆盖参数为空、chatObject 为 null、helper 为 null、返回值为 null、异常等分支场景。"
    },
    "tokenMetrics": {
      "maas": "zhipu",
      "model": "glm-5.2",
      "totalTokens": 7244,
      "promptTokens": 6966,
      "promptCachedTokens": 4224,
      "promptCacheHitTokens": 0,
      "promptCacheMissTokens": 0,
      "promptTextTokens": 0,
      "completionTokens": 278,
      "completionReasoningTokens": 0,
      "completionTextTokens": 0,
      "startTime": 1785158871896,
      "endTime": 1785158881510
    },
    "role": "assistant"
  }
]
```



#### 5.4.5 会话创建

智能体会自动创建会话

```java
// ===== 2. 加载历史会话消息，如果会话不存在，则创建 =====
planAgent.loadSessionMemory( question, domain);
```

需先设置会话相关信息到智能体

```java
StoreContext storeContext = new StoreContext()
       .setSessionId(sessionId)
       .setUserId(userId)
       .setRequestId(requestId)
       .setSessionSize(500)
       .setStoreType(StoreContext.STORE_TYPE_DB)
       .setDataSource("sessionds") //设置会话存储数据源
    .setDomain(domain);

AIPlanAgent planAgent = new AIPlanAgent(storeContext);//构建智能体工作流时，设置存储上下文
```

#### 5.4.6 判断会话存在

```java
/**
 * 判断会话是否存在
 * @param sessionid
 * @return
 * @throws AgentSessionException
 */
boolean existAgentSession(String sessionid) throws AgentSessionException;
```

#### 5.4.7 人工任务处理

```java
/**
 * 处理人工任务
 * @param hitlTaskData
 * @param hitlTaskId
 */
String handledHitlCallTask(Object  hitlTaskData,Throwable throwable,String hitlTaskId);

/**
 * 拒绝人工任务
 * @param hitlTaskData
 * @param hitlTaskId
 */
String refusedHitlCallTask(Object  hitlTaskData,Throwable throwable,String hitlTaskId);
```

人工任务处理使用参考文档：https://esdoc.bbossgroups.com/#/bboss-ai-hitl