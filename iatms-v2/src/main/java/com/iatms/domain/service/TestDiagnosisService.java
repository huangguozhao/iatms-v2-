package com.iatms.domain.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.iatms.domain.model.entity.*;
import com.iatms.domain.model.entity.Module;
import com.iatms.infrastructure.persistence.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 测试诊断服务 - 整合测试执行相关数据构建AI诊断prompt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestDiagnosisService {

    private final TestResultMapper testResultMapper;
    private final TestCaseMapper testCaseMapper;
    private final ApiRequestMapper apiRequestMapper;
    private final TestExecutionMapper testExecutionMapper;
    private final ModuleMapper moduleMapper;
    private final ProjectMapper projectMapper;
    private final EnvironmentConfigMapper environmentConfigMapper;
    private final UserMapper userMapper;

    /**
     * 诊断请求结果
     */
    public static class DiagnosisContext {
        // 执行记录信息
        public String executionId;
        public String executionScope;
        public String scopeName;
        public String environment;
        public String status;
        public String startTime;
        public String endTime;
        public Integer durationSeconds;
        public Integer totalCases;
        public Integer passedCases;
        public Integer failedCases;
        public String executorName;
        public String errorMessage;

        // 项目/模块信息
        public String projectName;
        public String moduleName;

        // 测试结果列表
        public List<CaseResultDetail> caseResults = new ArrayList<>();

        // 汇总统计
        public int totalResults;
        public int passedCount;
        public int failedCount;
    }

    /**
     * 单个用例的诊断详情
     */
    public static class CaseResultDetail {
        public Long resultId;
        public Integer caseId;
        public String caseName;
        public String caseCode;
        public String status;
        public Long duration;
        public String startTime;
        public String endTime;
        public String failureMessage;
        public String failureType;
        public String severity;
        public String priority;

        // API信息
        public Integer apiId;
        public String apiName;
        public String apiPath;
        public String apiMethod;
        public String baseUrl;
        public String requestHeaders;
        public String requestBody;
        public String responseBody;
        public Integer responseStatusCode;
        public Long responseTime;

        // 断言信息
        public String assertions;
        public String assertionResults;
        public boolean assertionPassed;

        // 测试类型
        public String testType;
        public String testLayer;
    }

    /**
     * 根据executionId构建完整的诊断上下文
     */
    public DiagnosisContext buildDiagnosisContext(String executionId) {
        DiagnosisContext context = new DiagnosisContext();
        context.executionId = executionId;

        // 1. 查询执行记录
        TestExecution execution = testExecutionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            log.warn("未找到执行记录: executionId={}", executionId);
            return context;
        }

        // 填充执行记录基本信息
        context.executionScope = execution.getExecutionScope();
        context.scopeName = execution.getScopeName();
        context.environment = execution.getEnvironment();
        context.status = execution.getStatus();
        context.startTime = execution.getStartTime() != null ? execution.getStartTime().toString() : null;
        context.endTime = execution.getEndTime() != null ? execution.getEndTime().toString() : null;
        context.durationSeconds = execution.getDurationSeconds();
        context.totalCases = execution.getTotalCases();
        context.passedCases = execution.getPassedCases();
        context.failedCases = execution.getFailedCases();
        context.errorMessage = execution.getErrorMessage();

        // 获取执行人名称
        if (execution.getExecutedBy() != null) {
            User user = userMapper.selectById(execution.getExecutedBy().longValue());
            if (user != null) {
                context.executorName = user.getName();
            }
        }

        // 2. 查询所有测试结果
        List<TestResult> testResults = testResultMapper.selectByExecutionId(executionId);
        context.totalResults = testResults != null ? testResults.size() : 0;

        if (testResults != null) {
            for (TestResult result : testResults) {
                CaseResultDetail detail = buildCaseResultDetail(result);
                context.caseResults.add(detail);

                if ("passed".equalsIgnoreCase(result.getStatus())) {
                    context.passedCount++;
                } else if ("failed".equalsIgnoreCase(result.getStatus())) {
                    context.failedCount++;
                }
            }
        }

        // 3. 获取项目/模块信息
        if (!context.caseResults.isEmpty()) {
            CaseResultDetail firstCase = context.caseResults.get(0);
            if (firstCase.caseId != null) {
                TestCase testCase = testCaseMapper.selectById(firstCase.caseId.longValue());
                if (testCase != null) {
                    // 获取API信息
                    if (testCase.getApiId() != null) {
                        ApiRequest api = apiRequestMapper.selectById(testCase.getApiId());
                        if (api != null) {
                            context.moduleName = api.getModuleId() != null ?
                                    moduleMapper.selectById(api.getModuleId()) != null ?
                                            moduleMapper.selectById(api.getModuleId()).getName() : null : null;

                            // 获取项目信息
                            if (api.getModuleId() != null) {
                                Module module = moduleMapper.selectById(api.getModuleId());
                                if (module != null) {
                                    Project project = projectMapper.selectById(module.getProjectId().longValue());
                                    if (project != null) {
                                        context.projectName = project.getName();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return context;
    }

    /**
     * 构建单个用例结果详情
     */
    private CaseResultDetail buildCaseResultDetail(TestResult result) {
        CaseResultDetail detail = new CaseResultDetail();
        detail.resultId = result.getId();
        detail.caseId = result.getCaseId();
        detail.status = result.getStatus();
        detail.duration = result.getDuration();
        detail.startTime = result.getStartTime() != null ? result.getStartTime().toString() : null;
        detail.endTime = result.getEndTime() != null ? result.getEndTime().toString() : null;
        detail.failureMessage = result.getFailureMessage();
        detail.failureType = result.getFailureType();
        detail.severity = result.getSeverity();
        detail.priority = result.getPriority();

        // 解析stepsJson获取请求/响应详情
        String stepsJson = result.getStepsJson();
        if (stepsJson != null && !stepsJson.isEmpty()) {
            try {
                JSONObject steps = JSON.parseObject(stepsJson);

                // request
                JSONObject request = steps.getJSONObject("request");
                if (request != null) {
                    detail.apiMethod = request.getString("method");
                    detail.apiPath = request.getString("url");
                    detail.requestHeaders = request.getString("headers");
                    detail.requestBody = request.getString("body");
                }

                // response
                JSONObject response = steps.getJSONObject("response");
                if (response != null) {
                    detail.responseStatusCode = response.getInteger("statusCode");
                    detail.responseBody = response.getString("body");
                    detail.responseTime = response.getLong("time");
                }

                // assertion
                JSONObject assertion = steps.getJSONObject("assertion");
                if (assertion != null) {
                    detail.assertionPassed = assertion.getBoolean("passed");
                    detail.assertionResults = JSON.toJSONString(assertion.get("items"));
                }
            } catch (Exception e) {
                log.warn("解析stepsJson失败: resultId={}, error={}", result.getId(), e.getMessage());
            }
        }

        // 获取用例信息
        if (result.getCaseId() != null) {
            TestCase testCase = testCaseMapper.selectById(result.getCaseId().longValue());
            if (testCase != null) {
                detail.caseName = testCase.getName();
                detail.caseCode = testCase.getCaseCode();
                detail.testType = testCase.getTestType();
                detail.assertions = testCase.getAssertions();

                // 获取API信息
                if (testCase.getApiId() != null) {
                    ApiRequest api = apiRequestMapper.selectById(testCase.getApiId());
                    if (api != null) {
                        detail.apiId = Math.toIntExact(api.getId());
                        detail.apiName = api.getName();
                        if (detail.apiPath == null) {
                            detail.apiPath = api.getPath();
                        }
                        if (detail.apiMethod == null) {
                            detail.apiMethod = api.getMethod();
                        }
                        detail.baseUrl = api.getBaseUrl();
                    }
                }
            }
        }

        return detail;
    }

    /**
     * 构建AI诊断的完整prompt
     */
    public String buildDiagnosisPrompt(DiagnosisContext context) {
        StringBuilder prompt = new StringBuilder();

        // 执行概述
        prompt.append("【测试执行概述】\n");
        prompt.append(String.format("执行ID: %s\n", context.executionId));
        prompt.append(String.format("执行范围: %s - %s\n", context.executionScope, context.scopeName));
        prompt.append(String.format("项目: %s\n", context.projectName != null ? context.projectName : "未知"));
        prompt.append(String.format("模块: %s\n", context.moduleName != null ? context.moduleName : "未知"));
        prompt.append(String.format("环境: %s\n", context.environment != null ? context.environment : "未知"));
        prompt.append(String.format("执行人: %s\n", context.executorName != null ? context.executorName : "未知"));
        prompt.append(String.format("执行时间: %s ~ %s\n",
                context.startTime != null ? context.startTime : "未知",
                context.endTime != null ? context.endTime : "未知"));
        prompt.append(String.format("执行状态: %s\n", context.status != null ? context.status : "未知"));
        prompt.append(String.format("耗时: %s秒\n", context.durationSeconds != null ? context.durationSeconds : "未知"));
        prompt.append(String.format("用例统计: 总数=%d, 通过=%d, 失败=%d\n",
                context.totalResults, context.passedCount, context.failedCount));

        if (context.errorMessage != null && !context.errorMessage.isEmpty()) {
            prompt.append(String.format("执行错误: %s\n", context.errorMessage));
        }

        // 失败的用例详情
        if (!context.caseResults.isEmpty()) {
            prompt.append("\n【失败用例详情】\n");
            int index = 1;
            for (CaseResultDetail caseDetail : context.caseResults) {
                if ("failed".equalsIgnoreCase(caseDetail.status) || "broken".equalsIgnoreCase(caseDetail.status)) {
                    prompt.append(String.format("\n--- 失败用例 %d ---\n", index++));
                    prompt.append(String.format("用例ID: %d\n", caseDetail.caseId));
                    prompt.append(String.format("用例名称: %s\n", caseDetail.caseName != null ? caseDetail.caseName : "未知"));
                    prompt.append(String.format("用例编码: %s\n", caseDetail.caseCode != null ? caseDetail.caseCode : "未知"));
                    prompt.append(String.format("API: %s %s\n",
                            caseDetail.apiMethod != null ? caseDetail.apiMethod : "未知",
                            caseDetail.apiPath != null ? caseDetail.apiPath : "未知"));
                    prompt.append(String.format("基础URL: %s\n", caseDetail.baseUrl != null ? caseDetail.baseUrl : "未知"));
                    prompt.append(String.format("执行状态: %s\n", caseDetail.status));
                    prompt.append(String.format("失败类型: %s\n", caseDetail.failureType != null ? caseDetail.failureType : "未知"));
                    prompt.append(String.format("严重程度: %s\n", caseDetail.severity != null ? caseDetail.severity : "未知"));
                    prompt.append(String.format("优先级: %s\n", caseDetail.priority != null ? caseDetail.priority : "未知"));
                    prompt.append(String.format("耗时: %d毫秒\n", caseDetail.duration != null ? caseDetail.duration : 0));
                    prompt.append(String.format("失败信息: %s\n", caseDetail.failureMessage != null ? caseDetail.failureMessage : "无"));

                    // 请求信息
                    prompt.append("\n请求信息:\n");
                    prompt.append(String.format("  方法: %s\n", caseDetail.apiMethod != null ? caseDetail.apiMethod : "未知"));
                    prompt.append(String.format("  URL: %s\n", caseDetail.apiPath != null ? caseDetail.apiPath : "未知"));
                    if (caseDetail.requestHeaders != null && !caseDetail.requestHeaders.isEmpty()) {
                        prompt.append(String.format("  请求头: %s\n", caseDetail.requestHeaders));
                    }
                    if (caseDetail.requestBody != null && !caseDetail.requestBody.isEmpty()) {
                        prompt.append(String.format("  请求体: %s\n", caseDetail.requestBody));
                    }

                    // 响应信息
                    prompt.append("\n响应信息:\n");
                    prompt.append(String.format("  状态码: %d\n", caseDetail.responseStatusCode != null ? caseDetail.responseStatusCode : 0));
                    prompt.append(String.format("  响应时间: %d毫秒\n", caseDetail.responseTime != null ? caseDetail.responseTime : 0));
                    if (caseDetail.responseBody != null && !caseDetail.responseBody.isEmpty()) {
                        // 限制响应体长度，避免prompt过长
                        String responseBody = caseDetail.responseBody;
                        if (responseBody.length() > 2000) {
                            responseBody = responseBody.substring(0, 2000) + "...(已截断)";
                        }
                        prompt.append(String.format("  响应体: %s\n", responseBody));
                    }

                    // 断言信息
                    prompt.append("\n断言结果:\n");
                    prompt.append(String.format("  断言通过: %s\n", caseDetail.assertionPassed ? "是" : "否"));
                    if (caseDetail.assertionResults != null && !caseDetail.assertionResults.isEmpty()) {
                        prompt.append(String.format("  断言详情: %s\n", caseDetail.assertionResults));
                    }

                    prompt.append("\n");
                }
            }

            // 全部用例汇总
            if (context.caseResults.size() > 1) {
                prompt.append("【全部用例执行结果汇总】\n");
                for (int i = 0; i < context.caseResults.size(); i++) {
                    CaseResultDetail caseDetail = context.caseResults.get(i);
                    prompt.append(String.format("%d. %s - %s (%s)\n",
                            i + 1,
                            caseDetail.caseName != null ? caseDetail.caseName : "未知用例",
                            caseDetail.apiMethod != null ? caseDetail.apiMethod : "?",
                            caseDetail.apiPath != null ? caseDetail.apiPath : "?",
                            caseDetail.status != null ? caseDetail.status : "?"
                    ));
                }
            }
        }

        // 诊断要求
        prompt.append("\n【诊断要求】\n");
        prompt.append("请根据以上测试执行数据，进行全面分析：\n");
        prompt.append("1. 分析所有失败用例的根本原因\n");
        prompt.append("2. 识别共性问题和模式\n");
        prompt.append("3. 评估影响范围（哪些模块/功能受影响）\n");
        prompt.append("4. 给出具体的修复建议和解决方案\n");
        prompt.append("5. 提供后续测试建议\n");
        prompt.append("6. 评估本次测试对整体质量的影响\n");
        prompt.append("\n请用中文回复，结构化输出。\n");

        return prompt.toString();
    }
}
