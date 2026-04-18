package com.iatms.application.testing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.system.PermissionService;

import com.iatms.application.testing.TestCaseQueryService;
import com.iatms.application.testing.TestExecutionCommandService;
import com.iatms.application.testing.dto.command.StartExecutionCmd;
import com.iatms.application.testing.dto.result.ExecuteTestCaseResult;
import com.iatms.domain.model.entity.*;
import com.iatms.domain.model.entity.Module;
import com.iatms.domain.model.vo.ExecutionProgressVO;
import com.iatms.domain.model.vo.ProjectTreeVO;
import com.iatms.domain.model.vo.TestCaseDetailVO;
import com.iatms.domain.model.vo.TestCaseSummaryVO;
import com.iatms.infrastructure.persistence.mapper.ApiRequestMapper;
import com.iatms.infrastructure.persistence.mapper.ModuleMapper;
import com.iatms.infrastructure.persistence.mapper.ProjectMapper;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.infrastructure.persistence.mapper.TestExecutionMapper;
import com.iatms.infrastructure.persistence.mapper.TestResultMapper;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.common.exception.BusinessException;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试用例查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseQueryServiceImpl implements TestCaseQueryService {

    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper executionMapper;
    private final ProjectMapper projectMapper;
    private final ModuleMapper moduleMapper;
    private final ApiRequestMapper apiRequestMapper;
    private final PermissionService permissionService;
    private final TestResultMapper testResultMapper;
    private final TestExecutionCommandService executionCommandService;

    @Override
    public ApiResponse.PageResult<TestCaseSummaryVO> queryTestCases(
            Long projectId, Long moduleId, Long apiId, String keyword, Integer pageNum, Integer pageSize) {

        Set<Long> targetApiIds = null;

        // 直接指定了 apiId
        if (apiId != null) {
            targetApiIds = new HashSet<>();
            targetApiIds.add(apiId);
        }

        // projectId → modules → apis → apiIds
        if (projectId != null) {
            LambdaQueryWrapper<Module> modWrapper = new LambdaQueryWrapper<>();
            modWrapper.eq(Module::getProjectId, projectId.intValue());
            modWrapper.eq(Module::getIsDeleted, false);
            List<Module> modules = moduleMapper.selectList(modWrapper);

            if (modules.isEmpty()) {
                return ApiResponse.PageResult.of(List.of(), 0L, pageNum, pageSize);
            }

            List<Long> moduleIds = modules.stream()
                    .filter(m -> m.getModuleId() != null)
                    .map(m -> m.getModuleId().longValue())
                    .collect(Collectors.toList());

            LambdaQueryWrapper<ApiRequest> apiWrapper = new LambdaQueryWrapper<>();
            apiWrapper.in(ApiRequest::getModuleId, moduleIds.stream().map(Long::intValue).collect(Collectors.toList()));
            apiWrapper.eq(ApiRequest::getDeleted, false);
            List<ApiRequest> apis = apiRequestMapper.selectList(apiWrapper);

            if (apis.isEmpty()) {
                return ApiResponse.PageResult.of(List.of(), 0L, pageNum, pageSize);
            }

            Set<Long> projectApiIds = apis.stream()
                    .filter(a -> a.getId() != null)
                    .map(ApiRequest::getId)
                    .collect(Collectors.toSet());

            if (targetApiIds != null) {
                targetApiIds.retainAll(projectApiIds);
            } else {
                targetApiIds = projectApiIds;
            }

            if (targetApiIds.isEmpty()) {
                return ApiResponse.PageResult.of(List.of(), 0L, pageNum, pageSize);
            }
        }

        // moduleId → apis → apiIds
        if (moduleId != null) {
            LambdaQueryWrapper<ApiRequest> apiWrapper = new LambdaQueryWrapper<>();
            apiWrapper.eq(ApiRequest::getModuleId, moduleId.intValue());
            apiWrapper.eq(ApiRequest::getDeleted, false);
            List<ApiRequest> apis = apiRequestMapper.selectList(apiWrapper);

            if (apis.isEmpty()) {
                return ApiResponse.PageResult.of(List.of(), 0L, pageNum, pageSize);
            }

            Set<Long> moduleApiIds = apis.stream()
                    .filter(a -> a.getId() != null)
                    .map(ApiRequest::getId)
                    .collect(Collectors.toSet());

            if (targetApiIds != null) {
                targetApiIds.retainAll(moduleApiIds);
            } else {
                targetApiIds = moduleApiIds;
            }

            if (targetApiIds.isEmpty()) {
                return ApiResponse.PageResult.of(List.of(), 0L, pageNum, pageSize);
            }
        }

        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();

        if (targetApiIds != null && !targetApiIds.isEmpty()) {
            wrapper.in(TestCase::getApiId, targetApiIds.stream().map(Long::intValue).collect(Collectors.toList()));
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TestCase::getName, keyword)
                    .or().like(TestCase::getCaseCode, keyword));
        }

        wrapper.eq(TestCase::getDeleted, false);
        wrapper.orderByDesc(TestCase::getCreatedAt);

        IPage<TestCase> page = new Page<>(pageNum, pageSize);
        IPage<TestCase> result = testCaseMapper.selectPage(page, wrapper);

        return ApiResponse.PageResult.of(
                result.getRecords().stream().map(this::convertToSummaryVO).toList(),
                result.getTotal(),
                pageNum,
                pageSize
        );
    }

    @Override
    public TestCaseDetailVO getTestCaseDetail(Long caseId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        // Look up related data: project, module, api
        Long projectId = null;
        Long moduleId = null;
        String projectName = null;
        String moduleName = null;
        ApiRequest api = null;

        if (testCase.getApiId() != null) {
            api = apiRequestMapper.selectById(testCase.getApiId().longValue());
            if (api != null) {
                moduleId = api.getModuleId() != null ? api.getModuleId().longValue() : null;
                if (moduleId != null) {
                    Module module = moduleMapper.selectById(moduleId.intValue());
                    if (module != null) {
                        projectId = module.getProjectId() != null ? module.getProjectId().longValue() : null;
                        moduleName = module.getName();
                        if (projectId != null) {
                            Project project = projectMapper.selectById(projectId.intValue());
                            if (project != null) {
                                projectName = project.getName();
                            }
                        }
                    }
                }
            }
        }

        // Parse request_override JSON to extract headers, requestParams, requestBody
        String headers = null;
        String requestParams = null;
        String requestBody = null;
        String requestOverrideStr = testCase.getRequestOverride();
        if (requestOverrideStr != null && !requestOverrideStr.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.JsonNode overrideNode =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(requestOverrideStr);
                if (overrideNode.has("headers")) {
                    headers = overrideNode.get("headers").toString();
                }
                if (overrideNode.has("queryParams")) {
                    requestParams = overrideNode.get("queryParams").toString();
                }
                if (overrideNode.has("body")) {
                    requestBody = overrideNode.get("body").asText();
                }
            } catch (Exception e) {
                // If JSON parsing fails, keep original values
                log.warn("Failed to parse request_override JSON for case {}", caseId, e);
            }
        }

        return TestCaseDetailVO.builder()
                .id(testCase.getId())
                .caseCode(testCase.getCaseCode())
                .name(testCase.getName())
                .description(testCase.getDescription())
                .projectId(projectId)
                .projectName(projectName)
                .moduleId(moduleId)
                .moduleName(moduleName)
                .apiId(testCase.getApiId() != null ? testCase.getApiId().longValue() : null)
                .api(api != null ? com.iatms.domain.model.vo.ApiSummaryVO.builder()
                        .id(api.getId())
                        .name(api.getName())
                        .path(api.getPath())
                        .method(api.getMethod())
                        .build() : null)
                .testType(testCase.getTestType())
                .priority(testCase.getPriority())
                .severity(testCase.getSeverity())
                .tags(testCase.getTags())
                .status(testCase.getStatus())
                .preconditions(testCase.getPreconditions())
                .testSteps(testCase.getTestSteps())
                .testData(testCase.getTestData())
                .headers(headers)
                .requestParams(requestParams)
                .requestBody(requestBody)
                .assertions(testCase.getAssertions())
                .expectedResponse(testCase.getExpectedResponse())
                .extractors(testCase.getExtractors())
                .expectedHttpStatus(testCase.getExpectedHttpStatus())
                .expectedResponseSchema(testCase.getExpectedResponseSchema())
                .expectedResponseBody(testCase.getExpectedResponseBody())
                .requestOverride(testCase.getRequestOverride())
                .isEnabled(testCase.getIsEnabled())
                .createdAt(testCase.getCreatedAt())
                .updatedAt(testCase.getUpdatedAt())
                .createdBy(testCase.getCreatedBy())
                .build();
    }

    @Override
    public String executeTestCase(Long caseId, Boolean async, Long userId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        // 校验用户是否有权限执行该用例所属的项目
        if (testCase.getProjectId() != null && !permissionService.canAccessProject(userId, testCase.getProjectId().longValue())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权限执行该用例");
        }

        // 构建执行命令
        StartExecutionCmd cmd = new StartExecutionCmd();
        cmd.setExecutionType("TEST_CASE");
        cmd.setTargetId(caseId);
        cmd.setTriggerType("MANUAL");
        cmd.setEnvironmentId(null); // 使用默认环境

        String executionId;

        if (Boolean.FALSE.equals(async)) {
            // 同步执行：等待执行完成
            log.info("同步执行测试用例: caseId={}", caseId);
            executionId = executionCommandService.startExecution(cmd, userId);
            log.info("同步执行完成: caseId={}, executionId={}", caseId, executionId);
        } else {
            // 异步执行：立即返回executionId
            log.info("异步执行测试用例: caseId={}", caseId);
            executionId = executionCommandService.startExecutionAsync(cmd, userId).join();
            log.info("异步执行已触发: caseId={}, executionId={}", caseId, executionId);
        }

        return executionId;
    }

    @Override
    public ExecuteTestCaseResult executeTestCaseSync(Long caseId, Long userId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        // 校验用户是否有权限执行该用例所属的项目
        if (testCase.getProjectId() != null && !permissionService.canAccessProject(userId, testCase.getProjectId().longValue())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权限执行该用例");
        }

        // 构建执行命令
        StartExecutionCmd cmd = new StartExecutionCmd();
        cmd.setExecutionType("TEST_CASE");
        cmd.setTargetId(caseId);
        cmd.setTriggerType("MANUAL");
        cmd.setEnvironmentId(null);

        // 同步执行
        log.info("同步执行测试用例: caseId={}", caseId);
        String executionId = executionCommandService.startExecution(cmd, userId);
        log.info("同步执行完成: caseId={}, executionId={}", caseId, executionId);

        // 获取执行结果
        ExecutionProgressVO progress = executionCommandService.getExecutionProgress(executionId);

        // 转换为结果对象
        ExecuteTestCaseResult result = new ExecuteTestCaseResult();
        result.setExecutionId(executionId);
        result.setStatus(progress != null ? progress.getStatus() : "unknown");
        result.setDuration(progress != null && progress.getDuration() != null ? progress.getDuration().longValue() : 0L);
        result.setAssertionsPassed(progress != null ? progress.getPassedCases() : 0);
        result.setAssertionsFailed(progress != null ? progress.getFailedCases() : 0);
        result.setStartTime(progress != null && progress.getStartedAt() != null ? progress.getStartedAt().toString() : null);
        result.setEndTime(progress != null && progress.getStartedAt() != null ? java.time.LocalDateTime.now().toString() : null);
        result.setExecutor("当前用户");
        result.setErrorMessage(progress != null ? progress.getErrorMessage() : null);

        // 填充 AI 诊断所需字段
        result.setCaseName(testCase.getName());

        // 从 testcaseresults 查询详细信息
        List<TestResult> testResults = testResultMapper.selectByExecutionId(executionId);
        if (testResults != null && !testResults.isEmpty()) {
            TestResult testResult = testResults.get(0);
            result.setErrorMessage(testResult.getFailureMessage());

            // 解析 stepsJson 获取详细信息
            String stepsJson = testResult.getStepsJson();
            if (stepsJson != null && !stepsJson.isEmpty()) {
                result.setStepsJson(stepsJson);
                try {
                    JSONObject steps = JSON.parseObject(stepsJson);

                    // 解析 request 信息
                    JSONObject request = steps.getJSONObject("request");
                    if (request != null) {
                        result.setMethod(request.getString("method"));
                        result.setApiPath(request.getString("url"));
                    }

                    // 解析 response 信息
                    JSONObject response = steps.getJSONObject("response");
                    if (response != null) {
                        result.setResponseStatus(response.getInteger("statusCode"));
                        result.setResponseBody(response.getString("body"));
                        result.setActual(response.getString("body"));
                    }

                    // 解析 assertion 信息
                    JSONObject assertion = steps.getJSONObject("assertion");
                    if (assertion != null) {
                        result.setExpected(assertion.getString("message"));
                    }
                } catch (Exception e) {
                    log.warn("解析 stepsJson 失败: {}", e.getMessage());
                }
            }
        }

        return result;
    }

    @Override
    public List<ProjectTreeVO> getProjectTree(Long projectId, Long userId) {
        List<Project> projects;

        if (projectId != null) {
            Project project = projectMapper.selectById(projectId);
            if (project == null || project.getDeleted()) {
                return new ArrayList<>();
            }
            // 校验用户是否有权限访问该项目
            if (!permissionService.canAccessProject(userId, projectId)) {
                log.warn("用户无权限访问该项目: userId={}, projectId={}", userId, projectId);
                return new ArrayList<>();
            }
            projects = List.of(project);
        } else {
            // 获取用户有权限访问的所有项目ID
            List<Long> accessibleProjectIds = permissionService.getAccessibleProjectIds(userId);
            if (accessibleProjectIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 查询用户有权限的所有未删除项目，按创建时间倒序（最新的在前）
            LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Project::getDeleted, false);
            wrapper.in(Project::getId, accessibleProjectIds);
            wrapper.orderByDesc(Project::getCreatedAt);
            projects = projectMapper.selectList(wrapper);
        }

        if (projects.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        // 批量查询模块
        LambdaQueryWrapper<Module> moduleWrapper = new LambdaQueryWrapper<>();
        moduleWrapper.in(Module::getProjectId, projectIds.stream().map(Long::intValue).collect(Collectors.toList()));
        moduleWrapper.eq(Module::getIsDeleted, false);
        List<Module> allModules = moduleMapper.selectList(moduleWrapper);

        // 收集所有模块ID
        List<Long> moduleIds = allModules.stream()
                .filter(m -> m.getModuleId() != null)
                .map(m -> m.getModuleId().longValue())
                .collect(Collectors.toList());

        // 批量查询接口
        LambdaQueryWrapper<ApiRequest> apiWrapper = new LambdaQueryWrapper<>();
        if (!moduleIds.isEmpty()) {
            apiWrapper.in(ApiRequest::getModuleId, moduleIds.stream().map(Long::intValue).collect(Collectors.toList()));
        }
        apiWrapper.eq(ApiRequest::getDeleted, false);
        List<ApiRequest> allApis = apiRequestMapper.selectList(apiWrapper);

        // 收集所有接口ID
        List<Long> apiIds = allApis.stream()
                .filter(a -> a.getId() != null)
                .map(ApiRequest::getId)
                .collect(Collectors.toList());

        // 批量查询用例
        LambdaQueryWrapper<TestCase> caseWrapper = new LambdaQueryWrapper<>();
        if (!apiIds.isEmpty()) {
            caseWrapper.in(TestCase::getApiId, apiIds.stream().map(Long::intValue).collect(Collectors.toList()));
        }
        caseWrapper.eq(TestCase::getDeleted, false);
        List<TestCase> allCases = testCaseMapper.selectList(caseWrapper);

        // 构建树形结构
        return buildProjectTree(projects, allModules, allApis, allCases);
    }

    private List<ProjectTreeVO> buildProjectTree(List<Project> projects, List<Module> modules,
                                                 List<ApiRequest> apis, List<TestCase> cases) {
        // 按 projectId 分组模块
        Map<Long, List<Module>> modulesByProject = modules.stream()
                .collect(Collectors.groupingBy(m -> m.getProjectId().longValue()));

        // 按 parentModuleId 分组子模块（用于构建模块树）
        Map<Long, List<Module>> modulesByParentId = modules.stream()
                .filter(m -> m.getParentModuleId() != null)
                .collect(Collectors.groupingBy(m -> m.getParentModuleId().longValue()));

        // 按 moduleId 分组接口
        Map<Long, List<ApiRequest>> apisByModule = apis.stream()
                .filter(a -> a.getModuleId() != null)
                .collect(Collectors.groupingBy(a -> a.getModuleId().longValue()));

        // 按 apiId 分组用例
        Map<Long, List<TestCase>> casesByApi = cases.stream()
                .filter(c -> c.getApiId() != null)
                .collect(Collectors.groupingBy(c -> c.getApiId().longValue()));

        return projects.stream().map(project -> {
            ProjectTreeVO projectNode = ProjectTreeVO.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .type("project")
                    .projectId(project.getId())
                    .code(project.getCode())
                    .description(project.getDescription())
                    .status(project.getStatus())
                    .build();

            // 获取项目的根模块（parentModuleId 为 null 的模块）
            List<Module> rootModules = modulesByProject.getOrDefault(project.getId(), new ArrayList<>())
                    .stream().filter(m -> m.getParentModuleId() == null).toList();

            // 构建模块树
            projectNode.setChildren(buildModuleTree(rootModules, modulesByParentId, apisByModule, casesByApi, project.getId()));

            // 统计
            int totalApis = modulesByProject.getOrDefault(project.getId(), new ArrayList<>())
                    .stream().mapToInt(m -> apisByModule.getOrDefault(m.getModuleId().longValue(), List.of()).size()).sum();
            int totalCases = apis.stream()
                    .filter(a -> modulesByProject.getOrDefault(project.getId(), new ArrayList<>())
                            .stream().anyMatch(m -> m.getModuleId().equals(a.getModuleId())))
                    .mapToInt(a -> casesByApi.getOrDefault(a.getId(), List.of()).size()).sum();

            projectNode.setStats(ProjectTreeVO.Stats.builder()
                    .moduleCount(modulesByProject.getOrDefault(project.getId(), new ArrayList<>()).size())
                    .apiCount(totalApis)
                    .testCaseCount(totalCases)
                    .build());

            return projectNode;
        }).collect(Collectors.toList());
    }

    private List<ProjectTreeVO> buildModuleTree(List<Module> modules, Map<Long, List<Module>> modulesByParentId,
                                                Map<Long, List<ApiRequest>> apisByModule,
                                                Map<Long, List<TestCase>> casesByApi, Long projectId) {
        return modules.stream().map(module -> {
            Long moduleId = module.getModuleId().longValue();

            ProjectTreeVO moduleNode = ProjectTreeVO.builder()
                    .id(moduleId)
                    .name(module.getName())
                    .type("module")
                    .projectId(projectId)
                    .parentId(module.getParentModuleId() != null ? module.getParentModuleId().longValue() : null)
                    .code(module.getModuleCode())
                    .description(module.getDescription())
                    .status(module.getStatus())
                    .build();

            // 递归构建子模块
            List<Module> childModules = modulesByParentId.getOrDefault(moduleId, new ArrayList<>());
            moduleNode.setChildren(buildModuleTree(childModules, modulesByParentId, apisByModule, casesByApi, projectId));

            // 构建接口列表
            List<ApiRequest> moduleApis = apisByModule.getOrDefault(moduleId, new ArrayList<>());
            moduleNode.setApis(moduleApis.stream().map(api -> {
                Long apiId = api.getId();
                ProjectTreeVO apiNode = ProjectTreeVO.builder()
                        .id(apiId)
                        .name(api.getName())
                        .type("api")
                        .projectId(projectId)
                        .moduleId(moduleId)
                        .code(api.getApiCode())
                        .description(api.getDescription())
                        .status(api.getStatus())
                        .httpMethod(api.getMethod())
                        .path(api.getPath())
                        .build();

                // 构建用例列表
                List<TestCase> apiCases = casesByApi.getOrDefault(apiId, new ArrayList<>());
                apiNode.setTestCases(apiCases.stream().map(tc -> ProjectTreeVO.builder()
                        .id(tc.getId())
                        .name(tc.getName())
                        .type("testcase")
                        .projectId(projectId)
                        .moduleId(moduleId)
                        .apiId(apiId)
                        .code(tc.getCaseCode())
                        .description(tc.getDescription())
                        .status(tc.getIsEnabled() != null && tc.getIsEnabled() ? "ENABLED" : "DISABLED")
                        .priority(tc.getPriority())
                        .testType(tc.getTestType())
                        .testData(tc.getTestData())
                        .requestBody(tc.getRequestBody())
                        .expectedResult(tc.getExpectedResponseBody())
                        .expectedHttpStatus(tc.getExpectedHttpStatus())
                        .isEnabled(tc.getIsEnabled())
                        .build()).collect(Collectors.toList()));

                return apiNode;
            }).collect(Collectors.toList()));

            return moduleNode;
        }).collect(Collectors.toList());
    }

    private TestCaseSummaryVO convertToSummaryVO(TestCase testCase) {
        return TestCaseSummaryVO.builder()
                .id(testCase.getId())
                .caseCode(testCase.getCaseCode())
                .name(testCase.getName())
                .priority(testCase.getPriority())
                .status(testCase.getStatus())
                .moduleId(testCase.getModuleId())
                .apiId(testCase.getApiId() != null ? testCase.getApiId().longValue() : null)
                .createdAt(testCase.getCreatedAt())
                .createdBy(testCase.getCreatedBy())
                .build();
    }

    @Override
    public List<TestExecution> getTestCaseExecutionHistory(Long caseId, Integer limit) {
        List<TestExecution> records = executionMapper.selectByScopeAndRefId("test_case", caseId.intValue());
        if (records != null && limit != null && records.size() > limit) {
            return records.subList(0, limit);
        }
        return records != null ? records : List.of();
    }
}
