# API接口导入Prompt - 用于将第三方系统接口导入IATMS测试平台

## 背景说明
IATMSII是一个API接口测试管理平台，我们需要将**被测系统**的接口文档/接口信息导入到这个平台的数据库中，以便使用本系统进行接口测试管理。

## 数据库核心表结构说明

### 1. projects（项目表）- 必须先创建
```sql
INSERT INTO `projects` VALUES (
  项目ID,                                    -- 自增主键
  '项目名称',                                 -- name
  '项目描述',                                 -- description  
  创建人ID,                                  -- creator_id (关联users表)
  创建时间,                                   -- created_at
  更新时间,                                   -- updated_at
  是否删除(0),                                -- is_deleted
  删除时间,                                   -- deleted_at
  删除人ID,                                   -- deleted_by
  '项目编码(唯一)',                           -- project_code
  'API',                                     -- project_type (WEB/MOBILE/API/DESKTOP/HYBRID)
  'ACTIVE',                                  -- status (ACTIVE/INACTIVE/ARCHIVED)
  项目头像URL                                 -- avatar_url
);
```

### 2. modules（模块表）- 依赖于projects
```sql
INSERT INTO `modules` VALUES (
  模块ID,                                    -- module_id 自增
  '模块编码(项目内唯一)',                     -- module_code
  项目ID,                                    -- project_id 关联projects表
  父模块ID,                                  -- parent_module_id (可NULL)
  '模块名称',                                 -- name
  '模块描述',                                 -- description
  排序号,                                    -- sort_order 默认0
  'active',                                  -- status (active/inactive/archived)
  模块负责人ID,                               -- owner_id (关联users表)
  JSON数组标签,                              -- tags 如'[]'
  创建人ID,                                  -- created_by (关联users表)
  更新人ID,                                  -- updated_by
  创建时间,                                   -- created_at
  更新时间,                                   -- updated_at
  是否删除(0),                                -- is_deleted
  删除时间,                                   -- deleted_at
  删除人ID                                   -- deleted_by
);
```

### 3. apis（接口信息表）- 依赖于modules
```sql
INSERT INTO `apis` VALUES (
  接口ID,                                    -- api_id 自增
  '接口编码(模块内唯一)',                     -- api_code 如 'AUTH001'
  模块ID,                                    -- module_id 关联modules表
  '接口名称',                                 -- name 如 '用户登录'
  '请求方法',                                 -- method (GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS)
  '接口路径',                                 -- path 如 '/api/user/login'
  '基础URL',                                 -- base_url 如 'http://localhost:8085'
  自动生成,                                   -- full_url (由base_url+path自动生成)
  JSON数组查询参数,                          -- request_parameters 如 '[]'
  JSON数组路径参数,                           -- path_parameters 如 '[]'
  JSON数组请求头,                             -- request_headers 如'[{"name":"Content-Type","value":"application/json"}]'
  请求体内容,                                 -- request_body
  '请求体类型',                               -- request_body_type (json/form-data/x-www-form-urlencoded/xml/raw)
  '响应体类型',                               -- response_body_type (json/xml/html/text)
  '接口描述',                                 -- description
  'active',                                  -- status (active/inactive/deprecated)
  '1.0',                                     -- version
  超时秒数,                                  -- timeout_seconds 默认30
  '认证类型',                                 -- auth_type (none/basic/bearer/api_key/oauth2)
  JSON认证配置,                              -- auth_config
  JSON数组标签,                              -- tags 如'[]'
  JSON数组示例,                              -- examples 如'[]'
  创建人ID,                                  -- created_by
  更新人ID,                                  -- updated_by
  创建时间,                                   -- created_at
  更新时间,                                   -- updated_at
  是否删除(0),                                -- is_deleted
  删除时间,                                   -- deleted_at
  删除人ID                                   -- deleted_by
);
```

### 4. apipreconditions（前置条件表）- 依赖于apis
```sql
INSERT INTO `apipreconditions` VALUES (
  前置条件ID,                                -- precondition_id 自增
  接口ID,                                    -- api_id 关联apis表
  前置接口ID,                                -- precondition_api_id (可选，关联另一个api_id)
  '前置条件名称',                             -- name
  '前置条件类型',                             -- type (auth/setup/data/environment)
  JSON配置,                                  -- config 如'{"token_field":"token","token_location":"header"}'
  '描述',                                    -- description
  是否必需(1),                               -- is_required
  创建人ID,                                  -- created_by
  更新人ID,                                  -- updated_by
  创建时间,                                   -- created_at
  更新时间,                                   -- updated_at
  是否删除(0),                                -- is_deleted
  删除时间,                                   -- deleted_at
  删除人ID                                   -- deleted_by
);
```

### 5. testcases（测试用例表）- 依赖于apis
```sql
INSERT INTO `testcases` VALUES (
  用例ID,                                    -- case_id 自增
  '用例编码(接口内唯一)',                     -- case_code 如 'TC_AUTH001_001'
  接口ID,                                    -- api_id 关联apis表
  '用例名称',                                 -- name
  '用例描述',                                 -- description
  '测试类型',                                 -- test_type (functional/performance/security/compatibility/smoke/regression)
  '优先级',                                   -- priority (P0/P1/P2/P3)
  '严重程度',                                 -- severity (critical/high/medium/low)
  JSON数组标签,                              -- tags 如'[]'
  JSON前置条件,                              -- pre_conditions 如'{"username":"test","password":"123456"}'
  JSON测试步骤,                              -- test_steps 如'[]'
  JSON请求覆盖,                              -- request_override
  预期HTTP状态码,                            -- expected_http_status 如200
  JSON预期响应Schema,                        -- expected_response_schema
  预期响应体,                                 -- expected_response_body
  JSON断言规则,                              -- assertions 如'[{"type":"status_code","expected":200}]'
  JSON提取器,                                -- extractors 如'[]'
  JSON验证器,                                -- validators 如'[]'
  是否启用(1),                               -- is_enabled
  是否模板(0),                               -- is_template
  模板用例ID,                                -- template_id
  '1.0',                                     -- version
  创建人ID,                                  -- created_by
  更新人ID,                                  -- updated_by
  创建时间,                                   -- created_at
  更新时间,                                   -- updated_at
  是否删除(0),                                -- is_deleted
  删除时间,                                   -- deleted_at
  删除人ID                                   -- deleted_by
);
```

### 6. testsuites（测试套件表）- 依赖于projects和modules
```sql
INSERT INTO `testsuites` VALUES (
  套件ID,                                    -- suite_id 自增
  '套件名称',                                 -- suite_name
  '套件编码(项目内唯一)',                     -- suite_code
  项目ID,                                    -- project_id 关联projects表
  模块ID,                                    -- module_id 关联modules表(可NULL)
  '套件描述',                                 -- description
  '1.0',                                     -- version
  'active',                                  -- status (active/inactive/archived)
  创建人ID,                                  -- created_by
  更新人ID,                                  -- updated_by
  创建时间,                                   -- created_at
  更新时间,                                   -- updated_at
  是否删除(0),                                -- is_deleted
  删除时间,                                   -- deleted_at
  删除人ID,                                   -- deleted_by
  父套件ID,                                  -- parent_suite_id (可NULL)
  '套件类型',                                 -- suite_type (MODULE/FEATURE/EPIC/PACKAGE)
  套件顺序,                                  -- suite_order
  总用例数,                                  -- total_cases
  通过用例数,                                -- passed_cases
  失败用例数,                                -- failed_cases
  跳过用例数,                                -- skipped_cases
  成功率,                                    -- success_rate
  JSON标签,                                 -- tags
  JSON自定义属性                             -- custom_properties
);
```

## 输入要求

请提供被测系统的以下信息：

### 1. 项目基本信息
- 项目名称：
- 项目描述：
- 项目编码（英文简写，唯一）：
- 项目类型：
- 创建人ID（默认填1）：

### 2. 模块信息列表
请按以下格式提供每个模块的信息：
```
模块名称 | 模块编码 | 父模块(可选) | 模块描述 | 排序号
```

### 3. 接口信息列表
请按以下格式提供每个接口的信息：
```
接口名称 | 接口编码 | 模块编码 | 请求方法 | 接口路径 | 基础URL | 请求体类型 | 响应体类型 | 认证类型 | 接口描述 | Content-Type请求头(可选)
```

### 4. 前置条件（可选）
如有前置条件（如登录获取Token），请提供：
```
所属接口编码 | 前置条件名称 | 前置条件类型(auth/setup/data/environment) | 配置(JSON格式) | 描述
```

### 5. 测试用例（可选）
如有需要为接口创建测试用例，请提供：
```
接口编码 | 用例编码 | 用例名称 | 用例描述 | 测试类型 | 优先级 | 严重程度 | 前置条件(JSON) | 预期HTTP状态码 | 断言规则(JSON)
```

### 5.1 测试用例字段详细说明

#### test_type（测试类型）
- `functional` - 功能测试
- `performance` - 性能测试
- `security` - 安全测试
- `compatibility` - 兼容性测试
- `smoke` - 冒烟测试
- `regression` - 回归测试

#### priority（优先级）
- `P0` - 最高优先级，必须测试
- `P1` - 高优先级
- `P2` - 中优先级
- `P3` - 低优先级

#### severity（严重程度）
- `critical` - 致命问题
- `high` - 严重问题
- `medium` - 一般问题
- `low` - 轻微问题

#### pre_conditions（前置条件JSON）
```json
{"username": "testuser", "password": "123456"}
```
或从环境变量：
```json
{"username": "${env.TEST_USERNAME}", "password": "${env.TEST_PASSWORD}"}
```

#### assertions（断言规则JSON）- 重要！
支持以下断言类型：

1. **状态码断言**
```json
{"type": "status_code", "expected": 200}
```

2. **JSON路径断言**
```json
{"type": "json_path", "path": "$.code", "expected": 0}
{"type": "json_path", "path": "$.data.token", "expected": "*"}
{"type": "json_path", "path": "$.msg", "expected": "success"}
```

3. **响应头断言**
```json
{"type": "header", "path": "Content-Type", "expected": "application/json"}
```

4. **响应时间断言**
```json
{"type": "response_time", "expected": 1000, "comparison": "less_than"}
```

5. **包含断言**
```json
{"type": "contains", "expected": "success"}
```

6. **正则表达式断言**
```json
{"type": "regex", "path": "$.data.token", "expected": "^[A-Za-z0-9-_]+$"}
```

**复合断言示例：**
```json
[
  {"type": "status_code", "expected": 200},
  {"type": "json_path", "path": "$.code", "expected": 0},
  {"type": "json_path", "path": "$.msg", "expected": "登录成功"},
  {"type": "json_path", "path": "$.data.username", "expected": "testuser"}
]
```

#### extractors（提取器JSON）- 用于提取响应数据供后续使用
```json
[
  {"type": "json_path", "from": "$.data.token", "to": "auth_token"},
  {"type": "json_path", "from": "$.data.userId", "to": "user_id"}
]
```

#### expected_response_body（预期响应体示例）
```json
{"code": 0, "msg": "登录成功", "data": {"userId": 123, "username": "testuser", "token": "*"}}
```
其中 `*` 表示匹配任意值

### 5.2 测试用例完整示例

假设有"用户登录"接口 `POST /api/auth/login`

**测试用例1：正常登录**
```
接口编码: LOGIN001
用例编码: TC_LOGIN001_001
用例名称: 正常登录测试
用例描述: 使用正确的用户名和密码登录
测试类型: functional
优先级: P0
严重程度: critical
前置条件: {}
预期HTTP状态码: 200
断言规则: [{"type":"status_code","expected":200},{"type":"json_path","path":"$.code","expected":0},{"type":"json_path","path":"$.msg","expected":"success"}]
```

**测试用例2：密码错误**
```
接口编码: LOGIN001
用例编码: TC_LOGIN001_002
用例名称: 密码错误登录测试
用例描述: 使用错误密码登录
测试类型: functional
优先级: P0
严重程度: critical
前置条件: {}
预期HTTP状态码: 200
断言规则: [{"type":"status_code","expected":200},{"type":"json_path","path":"$.code","expected":401},{"type":"json_path","path":"$.msg","expected":"密码错误"}]
```

**测试用例3：参数缺失**
```
接口编码: LOGIN001
用例编码: TC_LOGIN001_003
用例名称: 用户名缺失测试
用例描述: 用户名为空时登录
测试类型: functional
优先级: P1
严重程度: high
前置条件: {}
预期HTTP状态码: 400
断言规则: [{"type":"status_code","expected":400},{"type":"json_path","path":"$.code","expected":400}]
```

### 5.3 suitecasemappings（测试套件-用例关联表）
将测试用例添加到测试套件中：
```sql
INSERT INTO `suitecasemappings` VALUES (
  映射ID,                                    -- mapping_id 自增
  套件ID,                                    -- suite_id 关联testsuites表
  用例ID,                                    -- case_id 关联testcases表
  用例顺序,                                  -- case_order 默认0
  创建时间                                    -- created_at
);
```

## 输出要求

请生成以下内容：

1. **完整的SQL INSERT语句**：按照上述表结构生成可直接执行的SQL
2. **导入顺序说明**：按照依赖关系说明导入顺序
3. **注意事项**：如有不支持的字段或需要调整的地方请说明

## 示例

假设被测系统是一个简单的用户管理系统，包含以下接口：
- POST /api/user/login - 用户登录
- GET /api/user/info - 获取用户信息
- POST /api/user/register - 用户注册

请根据以上格式生成完整的导入SQL。

