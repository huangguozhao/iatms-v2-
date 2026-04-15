package com.iatms.application.testing.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.iatms.application.testing.TestExecutionCommandService;
import com.iatms.application.testing.dto.command.StartExecutionCmd;
import com.iatms.common.constant.RedisKeys;
import com.iatms.domain.model.entity.*;
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
            switch (cmd.getExecutionType()) {
                case "TEST_CASE" -> executeSingleCase(context);
                case "TEST_SUITE" -> executeTestSuite(context);
                case "PROJECT" -> executeProject(context);
                default -> throw new IllegalArgumentException("不支持的执行类型: " + cmd.getExecutionType());
            }
        } catch (Exception e) {
            log.error("执行失败: executionId={}", executionId, e);
            updateExecutionStatus(executionId, ExecutionStatus.FAILED.name(), e.getMessage());
        }

        // 移除上下文
        executionContexts.remove(executionId);
        removeCachedContext(executionId);

        return executionId;
    }

    @Override
    @Async("testExecutionExecutor")
    public void startExecutionAsync(StartExecutionCmd cmd, Long userId) {
        String executionId = generateExecutionId();

        log.info("开始异步执行: executionId={}, type={}, targetId={}, userId={}",
                executionId, cmd.getExecutionType(), cmd.getTargetId(), userId);

        // 创建执行记录
        TestExecution execution = createExecutionRecord(executionId, cmd, userId);
        testExecutionMapper.insert(execution);

        // 初始化上下文
        ExecutionContext context = new ExecutionContext(executionId, cmd, execution.getId());
        executionContexts.put(executionId, context);
        cacheExecutionContext(executionId, context);

        // 异步执行
        CompletableFuture.runAsync(() -> {
            try {
                switch (cmd.getExecutionType()) {
                    case "TEST_CASE" -> executeSingleCase(context);
                    case "TEST_SUITE" -> executeTestSuite(context);
                    case "PROJECT" -> executeProject(context);
                    default -> throw new IllegalArgumentException("不支持的执行类型: " + cmd.getExecutionType());
                }
            } catch (Exception e) {
                log.error("异步执行失败: executionId={}", executionId, e);
                updateExecutionStatus(executionId, ExecutionStatus.FAILED.name(), e.getMessage());
            } finally {
                executionContexts.remove(executionId);
                removeCachedContext(executionId);
            }
        }, testExecutionExecutor);
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
        execution.setExecutionType(cmd.getExecutionType());
        execution.setTargetId(cmd.getTargetId());
        execution.setEnvironmentId(cmd.getEnvironmentId());
        execution.setTriggerType(cmd.getTriggerType());
        execution.setExecutedBy(userId);
        execution.setStatus(ExecutionStatus.PENDING.name());
        execution.setProgress(0);
        execution.setTotalCases(0);
        execution.setCompletedCases(0);
        execution.setPassedCases(0);
        execution.setFailedCases(0);
        execution.setStartedAt(LocalDateTime.now());
        execution.setCreatedBy(userId);
        execution.setUpdatedBy(userId);
        return execution;
    }

    private void executeSingleCase(ExecutionContext context) {
        context.setStatus(ExecutionStatus.RUNNING.name());
        updateExecutionStatus(context.getExecutionId(), ExecutionStatus.RUNNING.name(), null);

        TestCase testCase = testCaseMapper.selectById(context.getCmd().getTargetId());
        if (testCase == null) {
            throw new RuntimeException("测试用例不存在: " + context.getCmd().getTargetId());
        }

        context.setTotalCases(1);
        context.setCurrentCaseName(testCase.getName());

        TestResult result = executeTestCase(testCase, context);

        context.setCompletedCases(1);
        if ("PASSED".equals(result.getStatus())) {
            context.setPassedCases(1);
            updateExecutionStatus(context.getExecutionId(), ExecutionStatus.COMPLETED.name(), null);
        } else {
            context.setFailedCases(1);
            updateExecutionStatus(context.getExecutionId(), ExecutionStatus.FAILED.name(), result.getErrorMessage());
        }
        context.setProgress(100);
        context.setStatus(ExecutionStatus.COMPLETED.name());
    }

    private void executeTestSuite(ExecutionContext context) {
        context.setStatus(ExecutionStatus.RUNNING.name());
        updateExecutionStatus(context.getExecutionId(), ExecutionStatus.RUNNING.name(), null);

        List<TestSuiteRequest> suiteRequests = testSuiteRequestMapper.selectByTestSuiteId(context.getCmd().getTargetId());
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
                if ("PASSED".equals(result.getStatus())) {
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
            List<TestSuiteRequest> suiteRequests = testSuiteRequestMapper.selectByTestSuiteId(suite.getId());
            if (suiteRequests != null) {
                totalCases += suiteRequests.size();
            }
        }
        context.setTotalCases(totalCases);

        // 逐个执行套件
        for (TestSuite suite : suites) {
            if (context.isCancelled()) break;

            context.setCurrentCaseName(suite.getSuiteName());

            List<TestSuiteRequest> suiteRequests = testSuiteRequestMapper.selectByTestSuiteId(suite.getId());
            for (TestSuiteRequest sr : suiteRequests) {
                if (context.isCancelled()) break;

                TestCase testCase = testCaseMapper.selectById(sr.getApiRequestId());
                if (testCase != null) {
                    TestResult result = executeTestCase(testCase, context);
                    context.setCompletedCases(context.getCompletedCases() + 1);
                    if ("PASSED".equals(result.getStatus())) {
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
        long startTime = System.currentTimeMillis();
        TestResult result = new TestResult();
        result.setCaseId(testCase.getId());
        result.setCaseName(testCase.getName());
        result.setExecutionId(context.getExecutionId());
        result.setStartedAt(LocalDateTime.now());

        try {
            // 获取 API 信息
            ApiRequest api = null;
            if (testCase.getApiId() != null) {
                api = apiRequestMapper.selectById(testCase.getApiId());
            }

            if (api == null) {
                result.setStatus("FAILED");
                result.setErrorMessage("API定义不存在");
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

            result.setResponseTime(response.getResponseTime());
            result.setResponseData(response.getBody());

            // 执行断言
            if (testCase.getAssertions() != null && !testCase.getAssertions().isEmpty()) {
                TestExecutionDomainService.ApiResponse apiResp = new TestExecutionDomainService.ApiResponse();
                apiResp.setStatusCode(response.getStatusCode());
                apiResp.setBody(response.getBody());
                apiResp.setResponseTime(response.getResponseTime());

                TestExecutionDomainService.AssertionResult assertionResult =
                        executionDomainService.executeAssertions(testCase.getAssertions(), apiResp);

                result.setStatus(assertionResult.isPassed() ? "PASSED" : "FAILED");
                result.setAssertionResults(JSON.toJSONString(assertionResult.getItems()));
                if (!assertionResult.isPassed()) {
                    result.setErrorMessage(assertionResult.getMessage());
                }
            } else {
                result.setStatus(response.isSuccess() ? "PASSED" : "FAILED");
                if (!response.isSuccess()) {
                    result.setErrorMessage("HTTP状态码: " + response.getStatusCode());
                }
            }

            // 提取变量
            if (testCase.getExtractors() != null && !testCase.getExtractors().isEmpty()) {
                TestExecutionDomainService.ApiResponse apiResp = new TestExecutionDomainService.ApiResponse();
                apiResp.setBody(response.getBody());
                Map<String, Object> extracted = executionDomainService.extractVariables(testCase.getExtractors(), apiResp);
                extracted.forEach(context::setVariable);
            }

        } catch (Exception e) {
            log.error("执行测试用例失败: caseId={}", testCase.getId(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setCompletedAt(LocalDateTime.now());

        // 保存执行结果
        saveTestResult(result);

        return result;
    }

    private String buildUrl(ApiRequest api, ExecutionContext context) {
        String url = api.getUrl();
        if (url == null || url.isEmpty()) {
            url = "http://localhost:8080";
        }
        return url;
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

    private void saveTestResult(TestResult result) {
        try {
            TestResult existing = testResultMapper.selectByCaseAndExecution(
                    result.getExecutionId(), result.getCaseId());
            if (existing != null) {
                result.setId(existing.getId());
                testResultMapper.updateByCaseAndExecution(result);
            } else {
                testResultMapper.insertResult(result);
            }
        } catch (Exception e) {
            log.error("保存测试结果失败", e);
        }
    }

    private void updateExecutionStatus(String executionId, String status, String errorMessage) {
        try {
            TestExecution execution = testExecutionMapper.selectByExecutionId(executionId);
            if (execution != null) {
                execution.setStatus(status);
                if (errorMessage != null) {
                    execution.setErrorMessage(errorMessage);
                }
                if (ExecutionStatus.COMPLETED.name().equals(status) ||
                        ExecutionStatus.FAILED.name().equals(status) ||
                        ExecutionStatus.CANCELLED.name().equals(status) ||
                        ExecutionStatus.PAUSED.name().equals(status)) {
                    execution.setCompletedAt(LocalDateTime.now());
                    if (execution.getStartedAt() != null) {
                        execution.setDuration(
                                (int) java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).getSeconds()
                        );
                    }
                }
                testExecutionMapper.updateById(execution);
            }
        } catch (Exception e) {
            log.error("更新执行状态失败: executionId={}", executionId, e);
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
        return ExecutionProgressVO.builder()
                .executionId(execution.getId())
                .executionIdStr(execution.getExecutionId())
                .status(execution.getStatus())
                .progress(execution.getProgress())
                .totalCases(execution.getTotalCases())
                .completedCases(execution.getCompletedCases())
                .passedCases(execution.getPassedCases())
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
