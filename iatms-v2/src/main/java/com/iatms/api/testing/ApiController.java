package com.iatms.api.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.testing.ApiCommandService;
import com.iatms.application.testing.ApiQueryService;
import com.iatms.application.testing.dto.command.CreateApiRequestCmd;
import com.iatms.application.testing.dto.query.ApiQuery;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.entity.TestResult;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.vo.ApiDetailVO;
import com.iatms.domain.model.vo.ApiSummaryVO;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.infrastructure.persistence.mapper.TestResultMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API 管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/apis")
@RequiredArgsConstructor
public class ApiController {

    private final ApiCommandService apiCommandService;
    private final ApiQueryService apiQueryService;
    private final TestResultMapper testResultMapper;
    private final TestCaseMapper testCaseMapper;

    @PostMapping
    @RequirePermission(value = ProjectPermission.API_CREATE, requireProjectId = false)
    public ApiResponse<ApiDetailVO> createApi(
            @RequestBody @Valid CreateApiRequestCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("创建API: name={}, userId={}", cmd.getName(), userId);
        ApiDetailVO result = apiCommandService.createApi(cmd, userId);
        return ApiResponse.success(result);
    }

    @PutMapping("/{apiId}")
    @RequirePermission(value = ProjectPermission.API_EDIT, requireProjectId = false)
    public ApiResponse<ApiDetailVO> updateApi(
            @PathVariable Long apiId,
            @RequestBody @Valid CreateApiRequestCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("更新API: apiId={}, userId={}", apiId, userId);
        ApiDetailVO result = apiCommandService.updateApi(apiId, cmd, userId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{apiId}")
    @RequirePermission(value = ProjectPermission.API_DELETE, requireProjectId = false)
    public ApiResponse<Void> deleteApi(
            @PathVariable Long apiId,
            @RequestAttribute("userId") Long userId) {

        log.info("删除API: apiId={}, userId={}", apiId, userId);
        apiCommandService.deleteApi(apiId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{apiId}")
    @RequirePermission(value = ProjectPermission.API_VIEW, requireProjectId = false)
    public ApiResponse<ApiDetailVO> getApiDetail(@PathVariable Long apiId) {
        ApiDetailVO result = apiQueryService.getApiDetail(apiId);
        return ApiResponse.success(result);
    }

    @GetMapping
    @RequirePermission(value = ProjectPermission.API_VIEW, requireProjectId = false)
    public ApiResponse<ApiResponse.PageResult<ApiSummaryVO>> queryApis(
            ApiQuery query,
            @RequestAttribute("userId") Long userId) {
        ApiResponse.PageResult<ApiSummaryVO> result = apiQueryService.queryApis(query, userId);
        return ApiResponse.pageSuccess(result);
    }

    @GetMapping("/tree/{projectId}")
    @RequirePermission(value = ProjectPermission.API_VIEW)
    public ApiResponse<List<?>> getApiTree(@PathVariable Long projectId) {
        List<?> tree = apiQueryService.getApiTree(projectId);
        return ApiResponse.success(tree);
    }

    /**
     * 获取API关联的最新执行结果
     */
    @GetMapping("/{apiId}/latest-execution")
    @RequirePermission(value = ProjectPermission.CASE_VIEW, requireProjectId = false)
    public ApiResponse<Map<String, Object>> getLatestExecution(@PathVariable Long apiId) {
        // 查找该API关联的任意一个测试用例
        TestCase testCase = testCaseMapper.selectOne(
                new LambdaQueryWrapper<TestCase>()
                        .eq(TestCase::getApiId, apiId.intValue())
                        .eq(TestCase::getDeleted, false)
                        .last("LIMIT 1")
        );

        if (testCase == null) {
            return ApiResponse.success(Map.of("hasResult", false, "message", "该API下没有关联的测试用例"));
        }

        // 查询该用例的最新执行结果
        TestResult latestResult = testResultMapper.selectLatestByCaseId(Math.toIntExact(testCase.getId()));

        if (latestResult == null) {
            return ApiResponse.success(Map.of("hasResult", false, "message", "该用例还没有执行记录"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasResult", true);
        result.put("caseId", testCase.getId());
        result.put("caseName", testCase.getName());
        result.put("status", latestResult.getStatus());
        result.put("duration", latestResult.getDuration());
        result.put("startTime", latestResult.getStartTime());
        result.put("endTime", latestResult.getEndTime());
        result.put("failureMessage", latestResult.getFailureMessage());
        result.put("failureType", latestResult.getFailureType());
        result.put("environment", latestResult.getEnvironment());
        result.put("retryCount", latestResult.getRetryCount());

        // 解析 steps_json 获取详细的响应数据
        if (latestResult.getStepsJson() != null && !latestResult.getStepsJson().isEmpty()) {
            try {
                JSONObject steps = JSON.parseObject(latestResult.getStepsJson());
                // 响应体
                if (steps.containsKey("responseBody")) {
                    result.put("body", steps.get("responseBody"));
                }
                // 状态码
                if (steps.containsKey("statusCode")) {
                    result.put("statusCode", steps.getInteger("statusCode"));
                } else if (steps.containsKey("httpStatus")) {
                    result.put("statusCode", steps.getInteger("httpStatus"));
                }
                // 响应头
                if (steps.containsKey("responseHeaders")) {
                    result.put("headers", steps.get("responseHeaders"));
                }
                // 断言结果
                if (steps.containsKey("assertions")) {
                    result.put("assertions", steps.get("assertions"));
                } else if (steps.containsKey("assertionResults")) {
                    result.put("assertions", steps.get("assertionResults"));
                }
            } catch (Exception e) {
                log.warn("解析steps_json失败: {}", e.getMessage());
            }
        }

        // 解析 parameters_json 获取请求数据
        if (latestResult.getParametersJson() != null && !latestResult.getParametersJson().isEmpty()) {
            try {
                JSONObject params = JSON.parseObject(latestResult.getParametersJson());
                if (params.containsKey("requestBody")) {
                    result.put("requestBody", params.get("requestBody"));
                }
            } catch (Exception e) {
                log.warn("解析parameters_json失败: {}", e.getMessage());
            }
        }

        return ApiResponse.success(result);
    }

    /**
     * 获取API的测试历史
     */
    @GetMapping("/{apiId}/test-history")
    @RequirePermission(value = ProjectPermission.CASE_VIEW, requireProjectId = false)
    public ApiResponse<List<Map<String, Object>>> getTestHistory(
            @PathVariable Long apiId,
            @RequestParam(defaultValue = "7days") String period) {

        // 查找该API关联的所有测试用例
        List<TestCase> testCases = testCaseMapper.selectList(
                new LambdaQueryWrapper<TestCase>()
                        .eq(TestCase::getApiId, apiId.intValue())
                        .eq(TestCase::getDeleted, false)
        );

        if (testCases.isEmpty()) {
            return ApiResponse.success(List.of());
        }

        // 收集用例ID
        List<Integer> caseIds = testCases.stream()
                .map(tc -> tc.getId().intValue())
                .collect(Collectors.toList());

        // 计算时间范围
        LocalDateTime startTime = null;
        if (!"all".equals(period)) {
            int days = switch (period) {
                case "30days" -> 30;
                case "90days" -> 90;
                default -> 7; // 7days
            };
            startTime = LocalDateTime.now().minusDays(days);
        }

        // 查询这些用例的执行结果（取最新的一些）
        LambdaQueryWrapper<TestResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TestResult::getCaseId, caseIds);
        if (startTime != null) {
            wrapper.ge(TestResult::getCreatedAt, startTime);
        }
        wrapper.orderByDesc(TestResult::getCreatedAt);
        wrapper.last("LIMIT 100");

        List<TestResult> results = testResultMapper.selectList(wrapper);

        // 按执行时间分组，转换为前端需要的格式
        Map<String, Map<String, Object>> executionGroups = new LinkedHashMap<>();
        for (TestResult tr : results) {
            String timeKey = tr.getCreatedAt() != null ? tr.getCreatedAt().toLocalDate().toString() : "unknown";
            String execKey = timeKey + "-" + tr.getCaseId();

            if (!executionGroups.containsKey(execKey)) {
                TestCase tc = testCases.stream()
                        .filter(c -> c.getId().equals(tr.getCaseId()))
                        .findFirst()
                        .orElse(null);

                Map<String, Object> group = new HashMap<>();
                group.put("recordId", tr.getId());
                group.put("executor", tr.getExecutedBy());
                group.put("executorInfo", Map.of("name", tr.getExecutedBy() != null ? "用户" + tr.getExecutedBy() : "未知"));
                group.put("executionType", tr.getTaskType() != null ? tr.getTaskType() : "test_case");
                group.put("environment", tr.getEnvironment() != null ? tr.getEnvironment() : "testing");
                group.put("startTime", tr.getStartTime());
                group.put("endTime", tr.getEndTime());
                group.put("totalCases", 1);
                group.put("passedCases", "passed".equals(tr.getStatus()) ? 1 : 0);
                group.put("failedCases", "failed".equals(tr.getStatus()) ? 1 : 0);
                group.put("successRate", "passed".equals(tr.getStatus()) ? 1.0 : 0.0);
                group.put("testTime", tr.getStartTime());
                group.put("status", tr.getStatus());
                group.put("caseName", tc != null ? tc.getName() : "用例" + tr.getCaseId());
                group.put("duration", tr.getDuration());

                executionGroups.put(execKey, group);
            }
        }

        return ApiResponse.success(new java.util.ArrayList<>(executionGroups.values()));
    }
}
