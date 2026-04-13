# AI诊断简洁流程图

## 核心流程

```mermaid
flowchart TD
    subgraph AI诊断核心流程
        A([开始]) --> B[Controller接收请求]
        B --> C[解析参数<br/>failureMessage/status/apiPath]
        
        C --> D{有executionId?}
        D -->|是| E[查询测试结果]
        D -->|否| F[使用caseResults]
        
        E --> G[规则引擎诊断]
        F --> G
        G --> H{错误类型分析}
        
        H -->|timeout| I[超时诊断]
        H -->|500| J[服务端错误]
        H -->|401/403| K[认证错误]
        H -->|404| L[路径错误]
        H -->|其他| M[通用诊断]
        
        I --> N
        J --> N
        K --> N
        L --> N
        M --> N
        
        N[生成diagnosisId] --> O[设置aiStatus=processing]
        O --> P[异步调用DeepSeek API]
        P --> Q[返回diagnosisId]
        Q --> R([结束])
        
        P -.-> S[DeepSeek返回诊断结果]
        S --> T[更新缓存aiStatus=completed]
    end

    style A fill:#e1f5fe
    style R fill:#c8e6c9
```

## 查询诊断结果

```mermaid
flowchart TD
    A([开始]) --> B[查询请求<br/>/result/{diagnosisId}]
    B --> C{缓存存在?}
    C -->|是| D[获取诊断结果]
    C -->|否| E[返回null]
    D --> F{AI完成?}
    F -->|是| G[aiCompleted=true]
    F -->|否| H[aiCompleted=false]
    G --> I[返回结果]
    H --> I
    I --> J([结束])
    
    style A fill:#e1f5fe
    style J fill:#c8e6c9
```

## 关键数据结构

```mermaid
flowchart TD
    A[诊断结果] --> B[diagnosisId]
    A --> C[severity严重程度]
    A --> D[rootCause根因]
    A --> E[issues问题列表]
    A --> F[suggestions建议]
    A --> G[aiStatus状态]
```

## 流程要点

1. **双层诊断**: 规则引擎 → DeepSeek AI
2. **异步处理**: 立即返回diagnosisId，AI后台处理
3. **缓存机制**: 诊断结果和状态分开缓存
4. **错误分类**: timeout/500/401/403/404 等
