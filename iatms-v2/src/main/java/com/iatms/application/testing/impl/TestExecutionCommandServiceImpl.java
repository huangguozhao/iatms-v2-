package com.iatms.application.testing.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.iatms.application.testing.TestExecutionCommandService;
import com.iatms.application.testing.dto.command.StartExecutionCmd;
import com.iatms.common.constant.RedisKeys;
import com.iatms.domain.model.entity.*;
import com.iatms.domain.model.entity.Module;
import com.iatms.domain.model.enums.ExecutionStatus;
import com.iatms.domain.model.vo.ExecutionProgressVO;
import com.iatms.domain.service.TestExecutionDomainService;
import com.iatms.infrastructure.http.ApiClient;
import com.iatms.infrastructure.persistence.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 测试执行命令服务实现 - 核心执行引擎
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutionCommandServiceImpl implements TestExecutionCommandService {

    private final TestExecutionMapper testExecutionMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestSuiteMapper testSuiteMapper;
    private final TestSuiteRequestMapper testSuiteRequestMapper;
    private final ApiRequestMapper apiRequestMapper;
    private final TestResultMapper testResultMapper;
    private final ProjectMapper projectMapper;
    private final ModuleMapper moduleMapper;
    private final EnvironmentConfigMapper environmentConfigMapper;
    private final TestExecutionDomainService executionDomainService;
    private final ApiClient apiClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Executor testExecutionExecutor;

    /**
     * 执行中的任务缓存（内存 + Redis）
     */
    private final Map<String, ExecutionContext> executionContexts = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startExecution(StartExecutionCmd cmd, Long userId) {
        String executionId = generateExecutionId();

        log.info("开始同步执行: executionId={}, type={}, targetId={}, userId={}",
                executionId, cmd.getExecutionType(), cmd.getTargetId(), userId);

        // 创建执行记录
        TestExecution execution = createExecutionRecord(executionId, cmd, userId);
        testExecutionMapper.insert(execution);

        // 初始化上下文
        ExecutionContext context = new ExecutionContext(executionId, cmd, execution.getId());
        executionContexts.put(executionId, context);
        cacheExecutionContext(executionId, context);

        // 根据执行类型执行
        try {
            log.info("startExecution 开始执行: executionId={}, executionType={}", executionId, cmd.getExecutionType());
            switch (cmd.getExecutionType()) {
                case "TEST_CASE" -> executeSingleCase(context);
                case "TEST_SUITE" -> executeTestSuite(context);
                case "PROJECT" -> executeProject(context);
                default -> throw new IllegalArgumentException("不支持的执行类型: " + cmd.getExecutionType());
            }
            log.info("startExecution 执行完成: executionId={}", executionId);
        } catch (Exception e) {
            log.error("startExecution 捕获异常: executionId={}, error={}, stackTrace={}",
                    executionId, e.getMessage(), e.getStackTrace());
            updateExecutionStatus(executionId, ExecutionStatus.FAILED.name(), e.getMessage());
        }

        // 移除上下文
        executionContexts.remove(executionId);
        removeCachedContext(executionId);

        return executionId;
    }

    @Override
    public CompletableFuture<String> startExecutionAsync(StartExecutionCmd cmd, Long userId) {
        String executionId = generateExecutionId();

        log.info("开始异步执行: executionId={}, type={}, targetId={}, userId={}",
                executionId, cmd.getExecutionType(), cmd.getTargetId(), userId);

        CompletableFuture.runAsync(() -> {
            try {
                // 创建执行记录
                TestExecution execution = createExecutionRecord(executionId, cmd, userId);
                testExecutionMapper.insert(execution);

                // 初始化上下文
                ExecutionContext context = new ExecutionContext(executionId, cmd, execution.getId());
                executionContexts.put(executionId, context);
                cacheExecutionContext(executionId, context);

                // 执行
                switch (cmd.getExecutionType()) {
                    case "TEST_CASE" -> executeSingleCase(context);
                    case "TEST_SUITE" -> executeTestSuite(context);
                    case "PROJECT" -> executeProject(context);
                    default -> throw new IllegalArgumentException("不支持的执行类型: " + cmd.getExecutionType());
                }
            } catch (Exception e) {
                log.error("异步执行失败: executionId={}, error={}", executionId, e.getMessage(), e);
                updateExecutionStatus(executionId, ExecutionStatus.FAILED.name(), e.getMessage());
            } finally {
                executionContexts.remove(executionId);
                removeCachedContext(executionId);
            }
        }, testExecutionExecutor);

        return CompletableFuture.completedFuture(executionId);
    }

    @Override
    public void cancelExecution(String executionId, Long userId) {
        log.info("取消执行: executionId={}, userId={}", executionId, userId);

        ExecutionContext context = executionContexts.get(executionId);
        if (context == null) {
            context = loadCachedContext(executionId);
        }

        if (context != null) {
            context.setCancelled(true);
            updateExecutionStatus(executionId, ExecutionStatus.CANCELLED.name(), "用户取消");
            cacheExecutionContext(executionId, context);
        }
    }

    @Override
    public void pauseExecution(String executionId, Long userId) {
        log.info("暂停执行: executionId={}, userId={}", executionId, userId);

        ExecutionContext context = executionContexts.get(executionId);
        if (context == null) {
            context = loadCachedContext(executionId);
        }

        if (context != null) {
            context.setPaused(true);
            updateExecutionStatus(executionId, ExecutionStatus.PAUSED.name(), "用户暂停");
            cacheExecutionContext(executionId, context);
        }
    }

    @Override
    public void resumeExecution(String executionId, Long userId) {
        log.info("恢复执行: executionId={}, userId={}", executionId, userId);

        ExecutionContext context = executionContexts.get(executionId);
        if (context == null) {
            context = loadCachedContext(executionId);
        }

        if (context != null) {
            context.setPaused(false);
            synchronized (context) {
                context.notifyAll();
            }
            updateExecutionStatus(executionId, ExecutionStatus.RUNNING.name(), "继续执行");
        }
    }

    @Override
    public ExecutionProgressVO getExecutionProgress(String executionId) {
        // 尝试从内存获取
        ExecutionContext context = executionContexts.get(executionId);
        if (context == null) {
            // 尝试从 Redis 获取
            context = loadCachedContext(executionId);
        }

        if (context == null) {
            // 从数据库获取
            TestExecution execution = testExecutionMapper.selectByExecutionId(executionId);
            if (execution != null) {
                return buildProgressFromExecution(execution);
            }
            return null;
        }

        return ExecutionProgressVO.builder()
                .executionIdStr(executionId)
                .status(context.getStatus())
                .progress(context.getProgress())
                .totalCases(context.getTotalCases())
                .completedCases(context.getCompletedCases())
                .passedCases(context.getPassedCases())
                .failedCases(context.getFailedCases())
                .skippedCases(context.getSkippedCases())
                .currentCaseName(context.getCurrentCaseName())
                .duration(context.getDuration())
                .startedAt(context.getStartTime())
                .build();
    }

    // ========== 私有方法 ==========

    private String generateExecutionId() {
        return "EXE-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TestExecution createExecutionRecord(String executionId, StartExecutionCmd cmd, Long userId) {
        TestExecution execution = new TestExecution();
        execution.setExecutionId(executionId);

        // 映射执行类型: TEST_CASE -> test_case, TEST_SUITE -> test_suite, PROJECT -> project
        String scope = mapExecutionTypeToScope(cmd.getExecutionType());
        execution.setExecutionScope(scope);
        execution.setRefId(cmd.getTargetId().intValue());

        // 设置执行范围名称
        String scopeName = getScopeName(cmd.getExecutionType(), cmd.getTargetId());
        execution.setScopeName(scopeName);

        // 设置执行类型: manual/scheduled/triggered
        String execType = mapTriggerType(cmd.getTriggerType());
        execution.setExecutionType(execType);

        // 设置环境 (数据库中是环境名称字符串)
        execution.setEnvironment(cmd.getEnvironmentId() != null ? "env-" + cmd.getEnvironmentId() : "default");

        execution.setExecutedBy(userId);
        execution.setStatus(toDbStatus(ExecutionStatus.RUNNING.name()));
        execution.setTotalCases(0);
        execution.setFailedCases(0);
        execution.setStartTime(LocalDateTime.now());
        execution.setCreatedBy(userId);
        return execution;
    }

    /**
     * 根据执行类型和目标ID获取范围名称
     */
    private String getScopeName(String executionType, Long targetId) {
        if (executionType == null || targetId == null) {
            return "未知";
        }
        return switch (executionType.toUpperCase()) {
            case "TEST_CASE" -> {
                TestCase testCase = testCaseMapper.selectById(targetId);
                yield testCase != null ? testCase.getName() : "用例-" + targetId;
            }
            case "TEST_SUITE" -> {
                TestSuite testSuite = testSuiteMapper.selectById(targetId);
                yield testSuite != null ? testSuite.getSuiteName() : "套件-" + targetId;
            }
            case "PROJECT" -> {
                Project project = projectMapper.selectById(targetId);
                yield project != null ? project.getName() : "项目-" + targetId;
            }
            case "MODULE" -> {
                Module module = moduleMapper.selectById(targetId);
                yield module != null ? module.getName() : "模块-" + targetId;
            }
            case "API" -> {
                ApiRequest api = apiRequestMapper.selectById(targetId);
                yield api != null ? api.getName() : "接口-" + targetId;
            }
            default -> "范围-" + targetId;
        };
    }

    private String mapExecutionTypeToScope(String executionType) {
        if (executionType == null) return "test_case";
        return switch (executionType.toUpperCase()) {
            case "TEST_CASE" -> "test_case";
            case "TEST_SUITE" -> "test_suite";
            case "PROJECT" -> "project";
            case "MODULE" -> "module";
            case "API" -> "api";
            default -> "test_case";
        };
    }

    private String mapTriggerType(String triggerType) {
        if (triggerType == null) return "manual";
        return switch (triggerType.toUpperCase()) {
            case "SCHEDULED" -> "scheduled";
            case "TRIGGERED" -> "triggered";
            default -> "manual";
        };
    }

    /**
     * 将Java枚举状态转换为数据库状态（小写）
     */
    private String toDbStatus(String status) {
        if (status == null) return null;
        return status.toLowerCase();
    }

    private void executeSingleCase(ExecutionContext context) {
        log.info("executeSingleCase 开始执行: executionId={}, targetId={}", context.getExecutionId(), context.getCmd().getTargetId());

        context.setStatus(ExecutionStatus.RUNNING.name());
        updateExecutionStatus(context.getExecutionId(), ExecutionStatus.RUNNING.name(), null);

        TestCase testCase = testCaseMapper.selectById(context.getCmd().getTargetId());
        log.info("executeSingleCase 查询用例: executionId={}, caseId={}, testCase={}",
                context.getExecutionId(), context.getCmd().getTargetId(), testCase);

        if (testCase == null) {
            log.error("executeSingleCase 测试用例不存在: caseId={}", context.getCmd().getTargetId());
            throw new RuntimeException("测试用例不存在: " + context.getCmd().getTargetId());
        }

        context.setTotalCases(1);
        context.setCurrentCaseName(testCase.getName());

        log.info("executeSingleCase 开始调用 executeTestCase: caseId={}, caseName={}, apiId={}",
                testCase.getId(), testCase.getName(), testCase.getApiId());

        TestResult result = executeTestCase(testCase, context);

        log.info("executeSingleCase executeTestCase 返回: caseId={}, resultStatus={}, errorMsg={}",
                testCase.getId(), result.getStatus(), result.getErrorMessage());

        context.setCompletedCases(1);
        int passedCases = 0;
        if ("passed".equalsIgnoreCase(result.getStatus())) {
            passedCases = 1;
            context.setPassedCases(passedCases);
            log.info("executeSingleCase 测试通过: caseId={}, 更新状态为 COMPLETED", testCase.getId());
            updateExecutionStatus(context.getExecutionId(), ExecutionStatus.COMPLETED.name(), null);
        } else {
            context.setFailedCases(1);
            log.info("executeSingleCase 测试失败: caseId={}, 更新状态为 FAILED, errorMsg={}",
                    testCase.getId(), result.getErrorMessage());
            updateExecutionStatus(context.getExecutionId(), ExecutionStatus.FAILED.name(), result.getErrorMessage());
        }

        // 更新统计信息
        updateExecutionStats(context.getExecutionId(), 1, passedCases, context.getFailedCases());

        context.setProgress(100);
        // 设置最终状态（与 updateExecutionStatus 保持一致）
        context.setStatus("passed".equalsIgnoreCase(result.getStatus()) ? ExecutionStatus.COMPLETED.name() : ExecutionStatus.FAILED.name());

        log.info("executeSingleCase 执行完成: executionId={}, finalStatus={}", context.getExecutionId(), context.getStatus());
    }

    private void executeTestSuite(ExecutionContext context) {
        context.setStatus(ExecutionStatus.RUNNING.name());
        updateExecutionStatus(context.getExecutionId(), ExecutionStatus.RUNNING.name(), null);

        List<TestSuiteRequest> suiteRequests = testSuiteRequestMapper.selectByTestSuiteId(context.getCmd().getTargetId().intValue());
        if (suiteRequests == null || suiteRequests.isEmpty()) {
            log.warn("测试套件为空: suiteId={}", context.getCmd().getTargetId());
            context.setTotalCases(0);
            updateExecutionStatus(context.getExecutionId(), ExecutionStatus.COMPLETED.name(), null);
            return;
        }

        context.setTotalCases(suiteRequests.size());

        // 收集所有用例
        List<TestCase> allCases = new ArrayList<>();
        for (TestSuiteRequest sr : suiteRequests) {
            TestCase tc = testCaseMapper.selectById(sr.getApiRequestId());
            if (tc != null) {
                allCases.add(tc);
            }
        }

        // 使用线程池并发执行（最多20个并发）
        List<CompletableFuture<TestResult>> futures = new ArrayList<>();
        for (TestCase testCase : allCases) {
            if (context.isCancelled()) {
                break;
            }
            while (context.isPaused()) {
                synchronized (context) {
                    try {
                        context.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            CompletableFuture<TestResult> future = CompletableFuture.supplyAsync(
                    () -> executeTestCase(testCase, context),
                    testExecutionExecutor
            );
            futures.add(future);
        }

        // 等待所有任务完成
        for (int i = 0; i < futures.size(); i++) {
            try {
                TestResult result = futures.get(i).get(30, TimeUnit.MINUTES);
                context.setCompletedCases(i + 1);
                if ("passed".equalsIgnoreCase(result.getStatus())) {
                    context.setPassedCases(context.getPassedCases() + 1);
                } else {
                    context.setFailedCases(context.getFailedCases() + 1);
                }
                context.setProgress((context.getCompletedCases() * 100) / context.getTotalCases());
                updateExecutionProgress(context);
            } catch (Exception e) {
                log.error("等待任务完成失败: index={}", i, e);
                context.setFailedCases(context.getFailedCases() + 1);
            }
        }

        // 更新最终状态
        String finalStatus;
        if (context.isCancelled()) {
            finalStatus = ExecutionStatus.CANCELLED.name();
        } else if (context.getFailedCases() > 0) {
            finalStatus = ExecutionStatus.FAILED.name();
        } else {
            finalStatus = ExecutionStatus.COMPLETED.name();
        }
        context.setStatus(finalStatus);
        updateExecutionStatus(context.getExecutionId(), finalStatus, null);
    }

    private void executeProject(ExecutionContext context) {
        context.setStatus(ExecutionStatus.RUNNING.name());
        updateExecutionStatus(context.getExecutionId(), ExecutionStatus.RUNNING.name(), null);

        // 获取项目下所有测试套件
        List<TestSuite> suites = testSuiteMapper.selectByProjectId(context.getCmd().getTargetId());
        if (suites == null || suites.isEmpty()) {
            log.warn("项目下没有测试套件: projectId={}", context.getCmd().getTargetId());
            context.setTotalCases(0);
            updateExecutionStatus(context.getExecutionId(), ExecutionStatus.COMPLETED.name(), null);
            return;
        }

        int totalCases = 0;
        for (TestSuite suite : suites) {
            List<TestSuiteRequest> suiteRequests = testSuiteRequestMapper.selectByTestSuiteId(suite.getId().intValue());
            if (suiteRequests != null) {
                totalCases += suiteRequests.size();
            }
        }
        context.setTotalCases(totalCases);

        // 逐个执行套件
        for (TestSuite suite : suites) {
            if (context.isCancelled()) break;

            context.setCurrentCaseName(suite.getSuiteName());

            List<TestSuiteRequest> suiteRequests = testSuiteRequestMapper.selectByTestSuiteId(suite.getId().intValue());
            for (TestSuiteRequest sr : suiteRequests) {
                if (context.isCancelled()) break;

                TestCase testCase = testCaseMapper.selectById(sr.getApiRequestId());
                if (testCase != null) {
                    TestResult result = executeTestCase(testCase, context);
                    context.setCompletedCases(context.getCompletedCases() + 1);
                    if ("passed".equalsIgnoreCase(result.getStatus())) {
                        context.setPassedCases(context.getPassedCases() + 1);
                    } else {
                        context.setFailedCases(context.getFailedCases() + 1);
                    }
                    context.setProgress((context.getCompletedCases() * 100) / context.getTotalCases());
                    updateExecutionProgress(context);
                }
            }
        }

        String finalStatus = context.isCancelled() ? ExecutionStatus.CANCELLED.name() :
                (context.getFailedCases() > 0 ? ExecutionStatus.FAILED.name() : ExecutionStatus.COMPLETED.name());
        context.setStatus(finalStatus);
        updateExecutionStatus(context.getExecutionId(), finalStatus, null);
    }

    private TestResult executeTestCase(TestCase testCase, ExecutionContext context) {
        long startTimeMs = System.currentTimeMillis();
        TestResult result = new TestResult();
        result.setCaseId(testCase.getId().intValue());
        result.setRefId(testCase.getId().intValue());  // 关联ID
        result.setCaseName(testCase.getName());  // 内部使用
        result.setTaskType("test_case");  // 任务类型
        result.setExecutionRecordId(context.getDbRecordId());  // 关联执行记录ID
        result.setExecutionIdStr(context.getExecutionId());  // 存储字符串执行ID
        result.setStartTime(LocalDateTime.now());

        log.info("executeTestCase 开始: caseId={}, caseName={}, apiId={}, assertions={}, extractors={}",
                testCase.getId(), testCase.getName(), testCase.getApiId(),
                testCase.getAssertions(), testCase.getExtractors());

        try {
            // 获取 API 信息
            ApiRequest api = null;
            if (testCase.getApiId() != null) {
                api = apiRequestMapper.selectById(testCase.getApiId());
            }

            if (api == null) {
                result.setStatus("failed");
                result.setErrorMessage("API定义不存在");
                result.setEndTime(LocalDateTime.now());
                saveTestResult(result, context);
                return result;
            }

            // 构建请求
            String url = buildUrl(api, context);
            Map<String, String> headers = parseJson(testCase.getHeaders());
            String body = testCase.getRequestBody();

            // 解析变量
            if (context.getVariables() != null && !context.getVariables().isEmpty()) {
                url = executionDomainService.resolveVariables(url, new HashMap<>(context.getVariables()));
                body = executionDomainService.resolveVariables(body, new HashMap<>(context.getVariables()));
            }

            // 发送请求
            ApiClient.ApiResponse response = apiClient.send(
                    testCase.getMethod() != null ? testCase.getMethod() : "GET",
                    url,
                    headers,
                    body
            );

            log.info("executeTestCase 原始响应: caseId={}, responseStatusCode={}", testCase.getId(), response.getStatusCode());
            result.setDuration((long) response.getResponseTime());
            result.setResponseData(response.getBody());  // 内部使用

            // 执行断言
            // 注意：空数组 [] 视为无断言，使用 HTTP 状态码判断
            String assertions = testCase.getAssertions();
            boolean hasValidAssertions = assertions != null && !assertions.isEmpty() && !"[]".equals(assertions.trim());

            log.info("executeTestCase 断言判断: caseId={}, assertions={}, hasValidAssertions={}, isSuccess={}, statusCode={}",
                    testCase.getId(), assertions, hasValidAssertions, response.isSuccess(), response.getStatusCode());

            if (hasValidAssertions) {
                log.info("executeTestCase 存在断言: caseId={}, assertions={}", testCase.getId(), assertions);
                TestExecutionDomainService.ApiResponse apiResp = new TestExecutionDomainService.ApiResponse();
                apiResp.setStatusCode(response.getStatusCode());
                apiResp.setBody(response.getBody());
                apiResp.setResponseTime(response.getResponseTime());

                TestExecutionDomainService.AssertionResult assertionResult =
                        executionDomainService.executeAssertions(assertions, apiResp);

                log.info("executeTestCase 断言结果: caseId={}, isPassed={}, message={}", testCase.getId(), assertionResult.isPassed(), assertionResult.getMessage());
                result.setStatus(assertionResult.isPassed() ? "passed" : "failed");
                result.setAssertionResults(JSON.toJSONString(assertionResult.getItems()));  // 内部使用
                // 存储到stepsJson作为详细结果
                result.setStepsJson(buildStepsJson(testCase, response, assertionResult));
                if (!assertionResult.isPassed()) {
                    result.setFailureMessage(assertionResult.getMessage());
                }
            } else {
                boolean success = response.isSuccess();
                log.info("executeTestCase HTTP结果: caseId={}, statusCode={}, isSuccess={}", testCase.getId(), response.getStatusCode(), success);
                result.setStatus(success ? "passed" : "failed");
                if (!success) {
                    result.setFailureMessage("HTTP状态码: " + response.getStatusCode());
                }
            }

            // 提取变量
            if (testCase.getExtractors() != null && !testCase.getExtractors().isEmpty()) {
                TestExecutionDomainService.ApiResponse apiResp = new TestExecutionDomainService.ApiResponse();
                apiResp.setBody(response.getBody());
                Map<String, Object> extracted = executionDomainService.extractVariables(testCase.getExtractors(), apiResp);
                extracted.forEach(context::setVariable);
                result.setExtractedVariables(JSON.toJSONString(extracted));  // 内部使用
            }

        } catch (Exception e) {
            log.error("执行测试用例失败: caseId={}", testCase.getId(), e);
            result.setStatus("failed");
            result.setFailureMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());

        log.info("executeTestCase 执行完成，准备保存结果: caseId={}, status={}", testCase.getId(), result.getStatus());

        // 保存执行结果
        saveTestResult(result, context);

        log.info("executeTestCase 返回结果: caseId={}, status={}", testCase.getId(), result.getStatus());

        return result;
    }

    private String buildStepsJson(TestCase testCase, ApiClient.ApiResponse response,
                                  TestExecutionDomainService.AssertionResult assertionResult) {
        Map<String, Object> steps = new HashMap<>();
        steps.put("request", Map.of(
                "method", testCase.getMethod() != null ? testCase.getMethod() : "GET",
                "url", response.getUrl() != null ? response.getUrl() : "",
                "headers", testCase.getHeaders() != null ? testCase.getHeaders() : "{}",
                "body", testCase.getRequestBody() != null ? testCase.getRequestBody() : ""
        ));
        steps.put("response", Map.of(
                "statusCode", response.getStatusCode(),
                "body", response.getBody() != null ? response.getBody() : "",
                "time", response.getResponseTime()
        ));
        steps.put("assertion", Map.of(
                "passed", assertionResult.isPassed(),
                "message", assertionResult.getMessage() != null ? assertionResult.getMessage() : "",
                "items", assertionResult.getItems() != null ? assertionResult.getItems() : Collections.emptyList()
        ));
        return JSON.toJSONString(steps);
    }

    private String buildUrl(ApiRequest api, ExecutionContext context) {
        // 优先使用环境配置的 base_url，否则使用接口的 base_url
        String baseUrl = null;
        if (context.getCmd().getEnvironmentId() != null) {
            EnvironmentConfig env = environmentConfigMapper.selectById(context.getCmd().getEnvironmentId());
            if (env != null && env.getBaseUrl() != null && !env.getBaseUrl().isEmpty()) {
                baseUrl = env.getBaseUrl();
            }
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = api.getBaseUrl();
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        // 拼接接口路径
        String path = api.getPath();
        if (path == null) {
            path = "";
        }

        // 确保 baseUrl 结尾和 path 开头没有多余的斜杠
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        return baseUrl + "/" + path;
    }

    private Map<String, String> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return JSON.parseObject(json, Map.class);
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", json);
            return new HashMap<>();
        }
    }

    private void saveTestResult(TestResult result, ExecutionContext context) {
        log.info("saveTestResult 开始保存: caseId={}, refId={}, taskType={}, status={}, executionRecordId={}, executionIdStr={}",
                result.getCaseId(), result.getRefId(), result.getTaskType(), result.getStatus(),
                result.getExecutionRecordId(), result.getExecutionIdStr());

        try {
            // 先通过executionIdStr查找所有结果
            List<TestResult> existingList = testResultMapper.selectByExecutionId(context.getExecutionId());
            log.info("saveTestResult 查询结果: executionIdStr={}, 找到 {} 条记录", context.getExecutionId(), existingList.size());

            TestResult existing = existingList.stream()
                    .filter(r -> r.getCaseId() != null && r.getCaseId().equals(result.getCaseId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                result.setId(existing.getId());
                log.info("更新已存在的测试结果: resultId={}, caseId={}", existing.getId(), existing.getCaseId());
                int updateCount = testResultMapper.updateResult(result);
                log.info("更新结果: updateCount={}", updateCount);
            } else {
                log.info("插入新的测试结果到 testcaseresults 表");
                log.info("saveTestResult INSERT 参数: caseId={}, refId={}, taskType={}, status={}, executionRecordId={}, executionIdStr={}",
                        result.getCaseId(), result.getRefId(), result.getTaskType(), result.getStatus(),
                        result.getExecutionRecordId(), result.getExecutionIdStr());
                int insertCount = testResultMapper.insertResult(result);
                log.info("插入结果: insertCount={}, generatedId={}", insertCount, result.getId());
            }
            log.info("saveTestResult 完成保存");
        } catch (Exception e) {
            log.error("保存测试结果失败: caseId={}, executionIdStr={}, error={}, stackTrace={}",
                    result.getCaseId(), context.getExecutionId(), e.getMessage(), e.getStackTrace());
        }
    }

    private void updateExecutionStatus(String executionId, String status, String errorMessage) {
        try {
            String dbStatus = toDbStatus(status);
            LocalDateTime now = LocalDateTime.now();

            // 使用原生 UPDATE 语句直接更新，不依赖实体状态
            if (ExecutionStatus.COMPLETED.name().equals(status) ||
                    ExecutionStatus.FAILED.name().equals(status) ||
                    ExecutionStatus.CANCELLED.name().equals(status) ||
                    ExecutionStatus.PAUSED.name().equals(status)) {
                // 更新状态和结束时间
                testExecutionMapper.updateStatusByExecutionId(executionId, dbStatus, errorMessage);
                log.info("updateExecutionStatus 使用原生SQL更新: executionId={}, status={}, errorMessage={}",
                        executionId, dbStatus, errorMessage);
            } else {
                // 只更新状态
                testExecutionMapper.updateStatusByExecutionId(executionId, dbStatus, errorMessage);
                log.info("updateExecutionStatus 使用原生SQL更新: executionId={}, status={}",
                        executionId, dbStatus);
            }

            // 验证更新是否成功
            TestExecution verification = testExecutionMapper.selectByExecutionId(executionId);
            if (verification != null) {
                log.info("updateExecutionStatus 验证: executionId={}, 数据库中的status={}",
                        executionId, verification.getStatus());
            }
        } catch (Exception e) {
            log.error("updateExecutionStatus 更新失败: executionId={}, status={}, error={}, stackTrace={}",
                    executionId, status, e.getMessage(), e.getStackTrace());
        }
    }

    /**
     * 更新执行记录的统计信息
     */
    private void updateExecutionStats(String executionId, int totalCases, int passedCases, int failedCases) {
        try {
            TestExecution execution = testExecutionMapper.selectByExecutionId(executionId);
            if (execution != null) {
                execution.setTotalCases(totalCases);
                execution.setFailedCases(failedCases);
                // 设置成功率
                if (totalCases > 0) {
                    java.math.BigDecimal rate = java.math.BigDecimal.valueOf((double) passedCases / totalCases * 100);
                    execution.setSuccessRate(rate);
                }
                testExecutionMapper.updateById(execution);
            }
        } catch (Exception e) {
            log.error("更新执行统计失败: executionId={}", executionId, e);
        }
    }

    private void updateExecutionProgress(ExecutionContext context) {
        cacheExecutionContext(context.getExecutionId(), context);
    }

    private void cacheExecutionContext(String executionId, ExecutionContext context) {
        try {
            redisTemplate.opsForValue().set(
                    RedisKeys.executionStatus(executionId),
                    context,
                    30,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("缓存执行上下文失败: executionId={}", executionId, e);
        }
    }

    private void removeCachedContext(String executionId) {
        try {
            redisTemplate.delete(RedisKeys.executionStatus(executionId));
        } catch (Exception e) {
            log.error("删除缓存的执行上下文失败: executionId={}", executionId, e);
        }
    }

    private ExecutionContext loadCachedContext(String executionId) {
        try {
            Object obj = redisTemplate.opsForValue().get(RedisKeys.executionStatus(executionId));
            if (obj instanceof ExecutionContext) {
                return (ExecutionContext) obj;
            }
        } catch (Exception e) {
            log.error("加载缓存的执行上下文失败: executionId={}", executionId, e);
        }
        return null;
    }

    private ExecutionProgressVO buildProgressFromExecution(TestExecution execution) {
        // 计算 passedCases：如果数据库没有存储，则通过 totalCases - failedCases 计算
        Integer passedCases = execution.getPassedCases();
        if (passedCases == null && execution.getTotalCases() != null && execution.getFailedCases() != null) {
            passedCases = execution.getTotalCases() - execution.getFailedCases();
        }
        // completedCases 等于 totalCases（假设所有用例都已处理）
        Integer completedCases = execution.getTotalCases();

        return ExecutionProgressVO.builder()
                .executionId(execution.getId())
                .executionIdStr(execution.getExecutionId())
                .status(execution.getStatus())
                .progress(execution.getProgress() != null ? execution.getProgress() : 100)
                .totalCases(execution.getTotalCases())
                .completedCases(completedCases)
                .passedCases(passedCases)
                .failedCases(execution.getFailedCases())
                .startedAt(execution.getStartedAt())
                .duration(execution.getDuration())
                .errorMessage(execution.getErrorMessage())
                .build();
    }

    // ========== 内部类 ==========

    /**
     * 执行上下文
     */
    @lombok.Data
    public static class ExecutionContext implements java.io.Serializable {
        private String executionId;
        private StartExecutionCmd cmd;
        private Long dbRecordId;
        private String status;
        private int progress;
        private int totalCases;
        private int completedCases;
        private int passedCases;
        private int failedCases;
        private int skippedCases;
        private String currentCaseName;
        private LocalDateTime startTime;
        private int duration;
        private boolean cancelled;
        private boolean paused;
        private final Map<String, Object> variables = new HashMap<>();

        public ExecutionContext(String executionId, StartExecutionCmd cmd, Long dbRecordId) {
            this.executionId = executionId;
            this.cmd = cmd;
            this.dbRecordId = dbRecordId;
            this.status = ExecutionStatus.PENDING.name();
            this.startTime = LocalDateTime.now();
        }

        public void setVariable(String key, Object value) {
            variables.put(key, value);
        }
    }
}
