# AI诊断流程图

## 1. AI诊断整体流程

```mermaid
flowchart TD
    subgraph AI诊断流程
        A1(["开始"]) --> A2[Controller接收请求<br/>/api/ai-diagnosis/execute]
        A2 --> A3[解析请求参数<br/>failureMessage/failureType<br/>responseStatus/responseBody<br/>apiPath/apiMethod/caseName]
        
        A3 --> A4{是否有executionId?}
        A4 -->|是| A5[查询测试结果<br/>testExecutionMapper<br/>.findTestCaseResultsByExecutionId]
        A4 -->|否| A6{是否有caseResults?}
        A6 -->|是| A7[convertToTestCaseResults<br/>转换前端传递的用例结果]
        A6 -->|否| A8[无测试结果数据]
        
        A5 --> A9[diagnoseWithRules<br/>规则引擎诊断]
        A7 --> A9
        A8 --> A9
        
        A9 --> A10[生成diagnosisId<br/>UUID]
        A10 --> A11[设置aiStatus=processing<br/>缓存诊断结果]
        A11 --> A12[callDeepSeekDiagnosisAsync<br/>异步调用DeepSeek]
        A12 --> A13[返回初步诊断结果<br/>包含diagnosisId]
        A13 --> A14([结束])
        
        A12 -.-> A15[异步执行DeepSeek API]
        A15 --> A16[构建系统提示词<br/>buildSystemPrompt]
        A16 --> A17[构建用户消息<br/>buildUserMessage]
        A17 --> A18[deepSeekUtils.chat<br/>调用DeepSeek API]
        A18 --> A19[parseAIResponse<br/>解析AI返回JSON]
        A19 --> A20[更新缓存<br/>aiStatus=completed]
    end

    style A1 fill:#e1f5fe
    style A14 fill:#c8e6c9
```

## 2. 规则引擎诊断流程

```mermaid
flowchart TD
    subgraph 规则引擎诊断
        B1[diagnoseWithRules] --> B2{是否有批量测试结果?}
        
        B2 -->|是| B3[统计批量结果<br/>passed/failed/broken/skipped]
        B3 --> B4[生成批量诊断信息<br/>分析失败用例详情]
        B4 --> B5[添加到分析列表analysis]
        
        B2 -->|否| B6{检查failureMessage}
        B6 --> B7{失败消息包含?}
        
        B7 -->|timeout| B8[生成超时诊断<br/>建议检查网络/服务]
        B7 -->|connection| B9[生成连接错误诊断<br/>建议检查服务地址]
        B7 -->|500| B10[生成服务器错误诊断<br/>建议检查服务端日志]
        B7 -->|401/403| B11[生成认证授权诊断<br/>建议检查Token/权限]
        B7 -->|404| B12[生成404诊断<br/>建议检查API路径]
        B7 -->|其他| B13[通用错误诊断]
        
        B8 --> B14
        B9 --> B14
        B10 --> B14
        B11 --> B14
        B12 --> B14
        B13 --> B14
        
        B14{检查responseStatus}
        
        B14 -->|200但业务失败| B15[检查responseBody业务码]
        B14 -->|非200| B16[根据状态码诊断]
        
        B15 --> B17[业务响应码分析<br/>生成业务错误诊断]
        B16 --> B18[HTTP状态码诊断<br/>添加到分析列表]
        
        B17 --> B19
        B18 --> B19[设置severity严重程度]
        B5 --> B19
        
        B19 --> B20[构建result返回<br/>包含analysis列表]
    end

    style B1 fill:#e1f5fe
```

## 3. DeepSeek API调用流程

```mermaid
flowchart TD
    subgraph DeepSeek API调用
        C1[callDeepSeekDiagnosis] --> C2[buildSystemPrompt<br/>构建系统提示词]
        
        C2 --> C3[设置诊断专家角色<br/>要求返回JSON格式<br/>包含severity/rootCause<br/>issues/suggestions/analysis]
        C3 --> C4[buildUserMessage<br/>构建用户消息]
        
        C4 --> C5[组装诊断信息]
        C5 --> C6[用例名称/API路径/请求方法]
        C6 --> C7[失败消息/失败类型/响应状态码]
        C7 --> C8[响应体内容]
        
        C8 --> C9{有批量测试结果?}
        C9 -->|是| C10[添加批量统计信息<br/>添加失败用例详情]
        C9 -->|否| C11
        
        C10 --> C12
        C11 --> C12[deepSeekUtils.chat<br/>调用DeepSeek API]
        
        C12 --> C13{返回结果?}
        C13 -->|是| C14[parseAIResponse<br/>解析JSON]
        C13 -->|否| C15[返回null]
        
        C14 --> C16[提取severity]
        C14 --> C17[提取rootCause]
        C14 --> C18[提取issues问题列表]
        C14 --> C19[提取suggestions建议列表]
        C14 --> C20[提取analysis分析过程]
        
        C16 --> C21[构建诊断结果Map]
        C17 --> C21
        C18 --> C21
        C19 --> C21
        C20 --> C21
        C21 --> C22([返回诊断结果])
        
        C15 --> C22
    end

    style C1 fill:#e1f5fe
    style C22 fill:#c8e6c9
```

## 4. 诊断结果查询流程

```mermaid
flowchart TD
    subgraph 诊断结果查询
        D1(["开始"]) --> D2[Controller接收请求<br/>/api/ai-diagnosis/result/{diagnosisId}]
        D2 --> D3[aiDiagnosisService<br/>.getDiagnosisResult]
        D3 --> D4{缓存中是否存在?}
        
        D4 -->|是| D5[获取缓存结果]
        D5 --> D6[判断AI是否完成<br/>diagnosisStatusCache]
        D6 --> D7[设置aiCompleted标志]
        D7 --> D8[返回诊断结果]
        D8 --> D9([结束])
        
        D4 -->|否| D10[返回null]
        D10 --> D9
    end

    style D1 fill:#e1f5fe
    style D9 fill:#c8e6c9
```

## 5. 核心组件关系

```mermaid
flowchart TD
    subgraph 核心组件
        E1[AIDiagnosisController]
        E2[AIDiagnosisService]
        E3[AIDiagnosisServiceImpl]
        E4[DeepSeekUtils]
        E5[DeepSeekConfig]
        E6[TestExecutionMapper]
    end
    
    subgraph 缓存
        E7[diagnosisResultCache<br/>诊断结果缓存]
        E8[diagnosisStatusCache<br/>AI完成状态]
    end
    
    subgraph DeepSeek API
        E9[DeepSeek API<br/>大模型服务]
    end

    E1 -->|请求| E2
    E2 -->|实现| E3
    E3 -->|调用| E4
    E4 -->|配置| E5
    E5 -->|调用| E9
    E3 -->|查询| E6
    E3 -->|缓存| E7
    E3 -->|缓存| E8
```

## 6. 诊断数据结构

```mermaid
flowchart TD
    subgraph 诊断结果结构
        F1[diagnosisId诊断ID]
        F2[severity严重程度<br/>high/medium/low]
        F3[rootCause根本原因]
        F4[issues问题列表<br/>title/severity/description]
        F5[suggestions建议列表<br/>title/content/priority]
        F6[analysis分析过程<br/>步骤记录]
        F7[aiStatus AI状态<br/>processing/completed/failed]
        F8[aiCompleted AI是否完成<br/>true/false]
        F9[batchInfo批量信息<br/>统计+失败用例]
    end
```

## 流程说明

### 1. AI诊断整体流程
1. Controller接收诊断请求，解析参数（失败消息、响应状态码、响应体等）
2. 根据executionId查询测试结果，或使用前端传递的caseResults
3. 先执行规则引擎诊断 `diagnoseWithRules()`，生成初步诊断结果
4. 生成diagnosisId，返回初步结果（aiStatus=processing）
5. 异步调用DeepSeek API进行深度诊断
6. AI诊断完成后更新缓存状态

### 2. 规则引擎诊断
- **批量测试**：统计通过/失败/异常/跳过数量，分析失败用例详情
- **失败消息分析**：根据错误消息关键词（timeout/connection/500/401/403/404）生成诊断
- **HTTP状态码**：根据响应状态码生成对应诊断
- **业务响应码**：解析响应体中的业务码，生成业务错误诊断

### 3. DeepSeek API调用
- **系统提示词**：设置诊断专家角色，要求返回JSON格式（severity/rootCause/issues/suggestions/analysis）
- **用户消息**：组装诊断信息，包括用例信息、API信息、失败信息、批量统计等
- **调用API**：通过DeepSeekUtils调用DeepSeek大模型
- **解析结果**：解析AI返回的JSON，提取各项诊断内容

### 4. 诊断结果查询
- 通过diagnosisId查询诊断结果
- 从缓存中获取结果
- 判断AI是否已完成（aiCompleted标志）
- 返回完整诊断结果

### 5. 返回数据结构
- `diagnosisId`: 诊断ID
- `severity`: 严重程度（high/medium/low）
- `rootCause`: 根本原因
- `issues`: 问题列表
- `suggestions`: 修复建议
- `analysis`: 分析过程记录
- `aiStatus`: AI处理状态（processing/completed/failed）
- `aiCompleted`: AI是否完成标志