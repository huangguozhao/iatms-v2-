package com.iatms.application.testing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.testing.TestCaseQueryService;
import com.iatms.domain.model.entity.ApiRequest;
import com.iatms.domain.model.entity.Module;
import com.iatms.domain.model.entity.Project;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.entity.TestExecution;
import com.iatms.domain.model.vo.ProjectTreeVO;
import com.iatms.domain.model.vo.TestCaseDetailVO;
import com.iatms.domain.model.vo.TestCaseSummaryVO;
import com.iatms.infrastructure.persistence.mapper.ApiRequestMapper;
import com.iatms.infrastructure.persistence.mapper.ModuleMapper;
import com.iatms.infrastructure.persistence.mapper.ProjectMapper;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.infrastructure.persistence.mapper.TestExecutionMapper;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
    public ApiResponse.PageResult<TestCaseSummaryVO> queryTestCases(
            Long projectId, Long moduleId, String keyword, Integer pageNum, Integer pageSize) {

        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(TestCase::getProjectId, projectId);
        }

        if (moduleId != null) {
            wrapper.eq(TestCase::getModuleId, moduleId);
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

        return TestCaseDetailVO.builder()
                .id(testCase.getId())
                .caseCode(testCase.getCaseCode())
                .name(testCase.getName())
                .description(testCase.getDescription())
                .projectId(testCase.getProjectId())
                .moduleId(testCase.getModuleId())
                .apiId(testCase.getApiId() != null ? testCase.getApiId().longValue() : null)
                .testType(testCase.getTestType())
                .priority(testCase.getPriority())
                .status(testCase.getStatus())
                .preconditions(testCase.getPreconditions())
                .testSteps(testCase.getTestSteps())
                .testData(testCase.getTestData())
                .headers(testCase.getHeaders())
                .requestParams(testCase.getRequestParams())
                .requestBody(testCase.getRequestBody())
                .assertions(testCase.getAssertions())
                .expectedResponse(testCase.getExpectedResponse())
                .extractors(testCase.getExtractors())
                .createdAt(testCase.getCreatedAt())
                .updatedAt(testCase.getUpdatedAt())
                .createdBy(testCase.getCreatedBy())
                .build();
    }

    @Override
    public String executeTestCase(Long caseId, Long userId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        // 创建执行记录
        String executionId = "EXEC-" + UUID.randomUUID().toString();

        TestExecution execution = new TestExecution();
        execution.setExecutionId(executionId);
        execution.setExecutionScope("test_case");
        execution.setRefId(caseId.intValue());
        execution.setStatus("pending");
        execution.setTotalCases(1);
        execution.setFailedCases(0);
        execution.setExecutionType("manual");
        execution.setExecutedBy(userId);
        execution.setCreatedBy(userId);

        executionMapper.insert(execution);

        log.info("创建测试执行记录: executionId={}, caseId={}, userId={}", executionId, caseId, userId);

        // TODO: 异步执行测试用例

        return executionId;
    }

    @Override
    public List<ProjectTreeVO> getProjectTree(Long projectId) {
        List<Project> projects;

        if (projectId != null) {
            Project project = projectMapper.selectById(projectId);
            if (project == null || project.getDeleted()) {
                return new ArrayList<>();
            }
            projects = List.of(project);
        } else {
            // 查询用户有权限的所有未删除项目
            LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Project::getDeleted, false);
            wrapper.orderByAsc(Project::getName);
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
}
