目前实现本地快速阻断 + 异步 AI 深度审查的双重审查机制

**本地快速过滤服务 ( FastFilterService.java ):**

一个基于 DFA (确定性有限自动机) 算法的高性能敏感词匹配服务。

- 数据结构 : 使用 Trie 树 (字典树) 存储敏感词库，匹配速度只与文本长度有关，不受词库大小影响，时间复杂度为 O(N) 。
- 初始化 :
  - 优先尝试加载外部配置文件指定路径的词库 sensitive.keywords.path （目前没有配置）。
  - 如果外部文件不存在，自动回退加载 classpath 下的兜底词库 ( politics.txt , sex.txt )。
  - 当前敏感词库来源：[politics.txt](https://github.com/fwwdn/sensitive-stop-words/blob/master/%E6%94%BF%E6%B2%BB%E7%B1%BB.txt)、[sex.txt](https://github.com/fwwdn/sensitive-stop-words/blob/master/%E8%89%B2%E6%83%85%E7%B1%BB.txt)
- 匹配算法优化 :
  - 跳过特殊字符 : 自动忽略文本中的标点符号、Markdown 符号、空格等，防止通过插入符号绕过审查（例如 "敏-感-词" 仍会被识别）。
  - 忽略英文字母大小写。
- 使用场景 : 被 NoteService 在创建笔记时同步调用。如果命中敏感词，直接抛出异常阻断上传。

**智能审查服务 ( SensitiveWordServiceImpl.java )**
这是对接 LLM (大语言模型) 的核心业务类，实现了 SensitiveWordService 接口。

- 模型集成 :
  - 对接 ModelScope (魔搭社区) API，默认使用 Qwen/Qwen2.5-7B-Instruct 模型。
  - 通过精心设计的 Prompt (提示词)，让 LLM 扮演“严格的内容安全审查助手”，识别 profanity (辱骂), violence (暴力), politics (政治) 等类别。
- 处理流程 :
  1. 下载内容 : 根据 noteId 从 MinIO 对象存储下载笔记文件。
  2. 文本提取 : 提取全文或摘要。
  3. 分块并行 (Chunking) : 对于超长文本，按 4000 字符分块，使用线程池 ( ExecutorService ) 并行调用 LLM，最后聚合所有分块的审查结果（取最高风险等级和最高分）。
  4. 结果解析 : 解析 LLM 返回的 JSON 结果，提取风险等级、违规类别和具体违规片段。

接口列表

- **GET  /api/v1/admin/sensitive/pending** 

  - 获取待处理违规笔记

- **POST /api/v1/admin/sensitive/handle/{id}**

  - 处理审核记录

  - Path 参数： id （必填， Long ）

  - **请求体：JSON（ Map<String, String> ）**

  - action （可选， String ，默认值 "IGNORE" ；允许值： "BAN" 或 "IGNORE" ；暂时无注解校验）

  - comment （可选， String ；长度未在代码层校验；数据库字段 admin_comment 上限255字符）

  - 说明：**当 action=BAN 时会删除对应笔记**（调用 NoteService.deleteNote ），然后将该条审查记录标记为已处理，并写入管理员备注

  - 示例

  - ```json
    {
      "action": "BAN",
      "comment": "违规严重，删除"
    }
    ```

- **POST /api/v1/admin/sensitive/check/{noteId}**

  - Path 参数： noteId （必填， Long）

  - Query 参数：
    - full （可选， boolean ，默认 false ；是否进行全文扫描）
    - persist （可选， boolean ，默认 false ；是否持久化审查结果）

  

- **POST /api/v1/admin/sensitive/check/batch**

  - 请求体：JSON

  - noteIds （可选， List<Long> ；为 null 时返回空结果；无注解校验）

  - concurrency （可选， Integer ； <=0 或 null 时回退为 3 ；实际并发= min(defaultConcurrency, requested) ，其中 defaultConcurrency 来源配置 @Value("${sensitive.scan.concurrency:4}") ， AdminSensitiveWordController.java:41-42 ）

  - full （可选， Boolean ；是否全文扫描）

  - Query 参数：

  - persist （可选， boolean ，默认 false ）

  - 示例：

  - ```json
    {
      "noteIds": [
        11,12,13,14
      ],
      "concurrency": 3,
      "full": true
    }
    ```

- **POST /api/v1/admin/sensitive/check/text**

  - 直接审核原始文本，用于调试

    请求体：

    - text ： string ，必填

    - language ： string （可选），如 zh-CN

    - 示例

    - ```json
      {
        "text": "这里是需要审核的文本……",
        "language": "zh-CN"
      }
      ```



**响应数据：**

- 统一包装类型：StandardResponse<T> 

  - code : number （成功 200，错误 500）
  - message : string （成功 "success" 或自定义，错误为错误消息）
  - data : T （实际数据或 null ）

- 审核记录 NoteModerationDO 

  - id , noteId , status , riskLevel , score , categoriesJson , findingsJson , source , createdAt , isHandled , adminComment

- 敏感检查结果 SensitiveCheckResult 

  - status : SAFE | FLAGGED | ERROR
  - riskLevel : LOW | MEDIUM | HIGH
  - score : number （0–100）
  - categories : string[]
  - findings : Finding[] （ term/category/confidence/snippet/startOffset/endOffset ）
  - noteMeta : { noteId, title }
  - model , checkedAt , durationMs , tokenUsage （ promptTokens/completionTokens/totalTokens ）, message
  - **字段含义与类型**

  - status （ String ）：审查状态（ SAFE / FLAGGED / ERROR ）

  - riskLevel （ String ）：风险等级（ LOW / MEDIUM / HIGH ）

  - score （ Double ）：风险分数（0–100；实现中按 Double 处理，保存为 INT ）

  - categories （ List<String> ）：命中的类别键（如 profanity 、 violence 、 hate 、 sexual 、 politics 、 others ）

  - findings （ List<Finding> ）：命中详情（ term 、 category 、 confidence:Double 、 snippet:String 、 startOffset:int 、 endOffset:int ）

  - noteMeta （ NoteMeta ）：笔记元信息（ noteId:Long 、 title:String ）

  - model 、 checkedAt 、 durationMs 、 tokenUsage （可选，供审计）

  - message （ String ）：错误或提示信息（错误分支）

  - 示例：

  - ```json
    {
      "code": 200,
      "message": "success",
      "data": [
        {
          "id": 123,
          "noteId": 456,
          "status": "FLAGGED",
          "riskLevel": "HIGH",
          "score": 78,
          "categoriesJson": "[\"hate\",\"violence\"]",
          "findingsJson": "[{\"term\":\"...\",\"category\":\"hate\",\"confidence\":0.92,\"snippet\":\"...\",\"startOffset\":10,\"endOffset\":25}]",
          "source": "LLM",
          "createdAt": "2025-12-07T10:20:30",
          "isHandled": false,
          "adminComment": null
        }
      ]
    }
    ```

    



- GET /pending 获取待处理违规笔记

  - 描述: 返回状态为 FLAGGED 且 isHandled=false 的最新审核记录（ NoteModerationMapper.selectPendingFlagged ， NoteModerationMapper.java:16-31 ）

    

- POST /handle/{id} 处理审核结果

  - action 仅接受 BAN 或 IGNORE （大小写不敏感）
  - 删除失败被忽略（安全性：避免因重复删除导致请求失败）

  

**新增数据库表结构**

- 基本信息
  - 表名： note_moderation 
  - 主键： id BIGINT AUTO_INCREMENT 
  - 索引：
    - idx_note_moderation_note_id (note_id) 
    - idx_note_moderation_status_handled (status, is_handled) 
- 字段详情
  - id （ BIGINT ，PK，自增，非空）
  - note_id （ BIGINT ，非空；无外键约束；业务上关联笔记ID）
  - status （ VARCHAR(16) ，非空；审查状态： SAFE/FLAGGED/ERROR ）
  - risk_level （ VARCHAR(16) ，可空；风险等级： LOW/MEDIUM/HIGH ）
  - score （ INT ，可空；风险分数，服务层以 Double 计算后转为 Integer 保存）
  - categories_json （ TEXT ，可空；JSON数组字符串，命中类别列表）
  - findings_json （ TEXT ，可空；JSON数组字符串，命中详情列表）
  - source （ VARCHAR(16) ，默认 'LLM' ；来源标识）
  - created_at （ TIMESTAMP ，非空，默认 CURRENT_TIMESTAMP ；创建时间）
  - is_handled （ BOOLEAN ，默认 FALSE ；是否已处理）
  - admin_comment （ VARCHAR(255) ，可空；管理员备注）
