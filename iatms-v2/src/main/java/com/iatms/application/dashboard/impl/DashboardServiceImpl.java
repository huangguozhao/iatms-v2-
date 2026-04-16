package com.iatms.application.dashboard.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iatms.application.dashboard.DashboardService;
import com.iatms.application.system.PermissionService;
import com.iatms.domain.model.entity.*;
import com.iatms.domain.model.entity.Module;
import com.iatms.domain.model.vo.DashboardStatisticsVO;
import com.iatms.domain.model.vo.RecentActivityVO;
import com.iatms.infrastructure.persistence.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectMapper projectMapper;
    private final ApiRequestMapper apiRequestMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper testExecutionMapper;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final ModuleMapper moduleMapper;

    @Override
    public DashboardStatisticsVO getStatistics(Long userId) {
        // 获取用户可访问的项目ID列表
        List<Long> accessibleProjectIds = permissionService.getAccessibleProjectIds(userId);
        boolean isAdmin = permissionService.isSystemAdmin(userId);

        // 统计项目数量
        Long projectCount;
        if (isAdmin) {
            projectCount = projectMapper.selectCount(
                    new LambdaQueryWrapper<Project>().eq(Project::getDeleted, false)
            ).longValue();
        } else {
            projectCount = (long) accessibleProjectIds.size();
        }

        // 统计 API 数量（仅计算可访问项目下的API）
        Long apiCount;
        if (isAdmin) {
            apiCount = apiRequestMapper.selectCount(
                    new LambdaQueryWrapper<ApiRequest>().eq(ApiRequest::getDeleted, false)
            ).longValue();
        } else {
            // 通过项目下的模块获取API数量
            Set<Integer> moduleIds = getModuleIdsByProjects(accessibleProjectIds);
            apiCount = apiRequestMapper.selectCount(
                    new LambdaQueryWrapper<ApiRequest>()
                            .eq(ApiRequest::getDeleted, false)
                            .in(!moduleIds.isEmpty(), ApiRequest::getModuleId, moduleIds)
            ).longValue();
        }

        // 统计测试用例数量
        Long testCaseCount;
        if (isAdmin) {
            testCaseCount = testCaseMapper.selectCount(
                    new LambdaQueryWrapper<TestCase>().eq(TestCase::getDeleted, false)
            ).longValue();
        } else {
            // 通过API间接获取用例数量
            Set<Integer> apiIds = getApiIdsByModuleIds(getModuleIdsByProjects(accessibleProjectIds));
            testCaseCount = testCaseMapper.selectCount(
                    new LambdaQueryWrapper<TestCase>()
                            .eq(TestCase::getDeleted, false)
                            .in(!apiIds.isEmpty(), TestCase::getApiId, apiIds)
            ).longValue();
        }

        // 统计测试执行总数（仅统计可访问项目的执行）
        Long executionCount;
        if (isAdmin) {
            executionCount = testExecutionMapper.selectCount(null).longValue();
        } else {
            executionCount = countExecutionsByProjectIds(accessibleProjectIds);
        }

        // 统计今日执行次数
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        Long todayExecutionCount;
        if (isAdmin) {
            todayExecutionCount = testExecutionMapper.selectCount(
                    new LambdaQueryWrapper<TestExecution>()
                            .ge(TestExecution::getUpdatedAt, todayStart)
            ).longValue();
        } else {
            todayExecutionCount = countExecutionsByProjectIdsAndTime(accessibleProjectIds, todayStart);
        }

        // 统计本周执行次数
        LocalDateTime weekStart = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).with(LocalTime.MIN);
        Long weekExecutionCount;
        if (isAdmin) {
            weekExecutionCount = testExecutionMapper.selectCount(
                    new LambdaQueryWrapper<TestExecution>()
                            .ge(TestExecution::getUpdatedAt, weekStart)
            ).longValue();
        } else {
            weekExecutionCount = countExecutionsByProjectIdsAndTime(accessibleProjectIds, weekStart);
        }

        // 计算成功率
        LambdaQueryWrapper<TestExecution> completedWrapper = new LambdaQueryWrapper<TestExecution>()
                .eq(TestExecution::getStatus, "completed");
        Long completedCount;
        LambdaQueryWrapper<TestExecution> failedWrapper = new LambdaQueryWrapper<TestExecution>()
                .eq(TestExecution::getStatus, "failed");
        Long failedCount;

        if (isAdmin) {
            completedCount = testExecutionMapper.selectCount(completedWrapper).longValue();
            failedCount = testExecutionMapper.selectCount(failedWrapper).longValue();
        } else {
            completedCount = countExecutionsByProjectIdsAndStatus(accessibleProjectIds, "completed");
            failedCount = countExecutionsByProjectIdsAndStatus(accessibleProjectIds, "failed");
        }

        Double successRate = 0.0;
        if (executionCount > 0) {
            successRate = Math.round((double) completedCount / executionCount * 100 * 100.0) / 100.0;
        }

        return DashboardStatisticsVO.builder()
                .projectCount(projectCount)
                .apiCount(apiCount)
                .testCaseCount(testCaseCount)
                .executionCount(executionCount)
                .todayExecutionCount(todayExecutionCount)
                .weekExecutionCount(weekExecutionCount)
                .successRate(successRate)
                .build();
    }

    @Override
    public List<RecentActivityVO> getRecentActivities(int limit, Long userId) {
        // 获取用户可访问的项目ID列表
        List<Long> accessibleProjectIds = permissionService.getAccessibleProjectIds(userId);
        boolean isAdmin = permissionService.isSystemAdmin(userId);

        List<TestExecution> executions;
        if (isAdmin) {
            LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<TestExecution>()
                    .orderByDesc(TestExecution::getUpdatedAt)
                    .last("LIMIT " + limit);
            executions = testExecutionMapper.selectList(wrapper);
        } else {
            // 仅获取可访问项目的执行记录
            List<TestExecution> allExecutions = testExecutionMapper.selectList(
                    new LambdaQueryWrapper<TestExecution>()
                            .orderByDesc(TestExecution::getUpdatedAt)
                            .last("LIMIT 1000")  // 先获取足够多的记录
            );
            // 过滤出可访问项目的执行
            Set<Long> projectIdSet = new HashSet<>(accessibleProjectIds);
            executions = allExecutions.stream()
                    .filter(exec -> isExecutionAccessible(exec, projectIdSet))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        if (executions.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取执行人信息
        List<Long> executorIds = new ArrayList<>();
        for (TestExecution exec : executions) {
            Long executedBy = exec.getExecutedBy();
            if (executedBy != null) {
                executorIds.add(executedBy);
            }
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!executorIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(executorIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }

        List<RecentActivityVO> activities = new ArrayList<>();
        for (TestExecution exec : executions) {
            User executor = null;
            Long executedBy = exec.getExecutedBy();
            if (executedBy != null) {
                executor = userMap.get(executedBy);
            }
            String refName = getRefName(exec);

            RecentActivityVO vo = RecentActivityVO.builder()
                    .executionId(exec.getExecutionId())
                    .scope(exec.getExecutionScope())
                    .refName(refName)
                    .status(exec.getStatus())
                    .executionType(exec.getExecutionType())
                    .executedBy(executedBy)
                    .executedByName(executor != null ? executor.getDisplayName() : null)
                    .startTime(exec.getStartTime() != null ? exec.getStartTime() : exec.getUpdatedAt())
                    .successRate(exec.getSuccessRate() != null ? exec.getSuccessRate().doubleValue() : null)
                    .totalCases(exec.getTotalCases())
                    .failedCases(exec.getFailedCases())
                    .build();
            activities.add(vo);
        }
        return activities;
    }

    /**
     * 检查执行记录是否可访问
     */
    private boolean isExecutionAccessible(TestExecution exec, Set<Long> accessibleProjectIds) {
        String scope = exec.getExecutionScope();
        Integer refId = exec.getRefId();

        if (scope == null || refId == null) {
            return false;
        }

        return switch (scope) {
            case "project" -> accessibleProjectIds.contains(refId.longValue());
            case "test_case" -> {
                TestCase tc = testCaseMapper.selectById(refId.longValue());
                if (tc != null && tc.getApiId() != null) {
                    ApiRequest api = apiRequestMapper.selectById(tc.getApiId().longValue());
                    if (api != null && api.getModuleId() != null) {
                        Module module = moduleMapper.selectById(api.getModuleId());
                        yield module != null && module.getProjectId() != null
                                && accessibleProjectIds.contains(module.getProjectId().longValue());
                    }
                }
                yield false;
            }
            case "api" -> {
                ApiRequest api = apiRequestMapper.selectById(refId.longValue());
                if (api != null && api.getModuleId() != null) {
                    Module module = moduleMapper.selectById(api.getModuleId());
                    yield module != null && module.getProjectId() != null
                            && accessibleProjectIds.contains(module.getProjectId().longValue());
                }
                yield false;
            }
            default -> false;
        };
    }

    /**
     * 根据项目ID列表统计执行记录数
     */
    private Long countExecutionsByProjectIds(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return 0L;
        }

        // 获取可访问的模块ID和API ID
        Set<Integer> moduleIds = getModuleIdsByProjects(projectIds);
        if (moduleIds.isEmpty()) {
            return 0L;
        }
        Set<Integer> apiIds = getApiIdsByModuleIds(moduleIds);
        if (apiIds.isEmpty()) {
            return 0L;
        }
        Set<Integer> caseIds = getCaseIdsByApiIds(apiIds);

        // 构建查询条件
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(TestExecution::getExecutionScope, "project")
                .in(TestExecution::getRefId, projectIds)
                .or()
                .eq(TestExecution::getExecutionScope, "test_case")
                .in(TestExecution::getRefId, caseIds)
                .or()
                .eq(TestExecution::getExecutionScope, "api")
                .in(TestExecution::getRefId, apiIds)
        );

        return testExecutionMapper.selectCount(wrapper).longValue();
    }

    /**
     * 根据项目ID列表和时间统计执行记录数
     */
    private Long countExecutionsByProjectIdsAndTime(List<Long> projectIds, LocalDateTime since) {
        if (projectIds.isEmpty()) {
            return 0L;
        }

        Set<Integer> moduleIds = getModuleIdsByProjects(projectIds);
        if (moduleIds.isEmpty()) {
            return 0L;
        }
        Set<Integer> apiIds = getApiIdsByModuleIds(moduleIds);
        if (apiIds.isEmpty()) {
            return 0L;
        }
        Set<Integer> caseIds = getCaseIdsByApiIds(apiIds);

        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<TestExecution>()
                .ge(TestExecution::getUpdatedAt, since)
                .and(w -> w
                        .eq(TestExecution::getExecutionScope, "project")
                        .in(TestExecution::getRefId, projectIds)
                        .or()
                        .eq(TestExecution::getExecutionScope, "test_case")
                        .in(TestExecution::getRefId, caseIds)
                        .or()
                        .eq(TestExecution::getExecutionScope, "api")
                        .in(TestExecution::getRefId, apiIds)
                );

        return testExecutionMapper.selectCount(wrapper).longValue();
    }

    /**
     * 根据项目ID列表和状态统计执行记录数
     */
    private Long countExecutionsByProjectIdsAndStatus(List<Long> projectIds, String status) {
        if (projectIds.isEmpty()) {
            return 0L;
        }

        Set<Integer> moduleIds = getModuleIdsByProjects(projectIds);
        if (moduleIds.isEmpty()) {
            return 0L;
        }
        Set<Integer> apiIds = getApiIdsByModuleIds(moduleIds);
        if (apiIds.isEmpty()) {
            return 0L;
        }
        Set<Integer> caseIds = getCaseIdsByApiIds(apiIds);

        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<TestExecution>()
                .eq(TestExecution::getStatus, status)
                .and(w -> w
                        .eq(TestExecution::getExecutionScope, "project")
                        .in(TestExecution::getRefId, projectIds)
                        .or()
                        .eq(TestExecution::getExecutionScope, "test_case")
                        .in(TestExecution::getRefId, caseIds)
                        .or()
                        .eq(TestExecution::getExecutionScope, "api")
                        .in(TestExecution::getRefId, apiIds)
                );

        return testExecutionMapper.selectCount(wrapper).longValue();
    }

    /**
     * 根据API ID列表获取用例ID列表
     */
    private Set<Integer> getCaseIdsByApiIds(Set<Integer> apiIds) {
        if (apiIds.isEmpty()) {
            return Set.of();
        }
        List<TestCase> cases = testCaseMapper.selectList(
                new LambdaQueryWrapper<TestCase>()
                        .eq(TestCase::getDeleted, false)
                        .in(TestCase::getApiId, apiIds)
        );
        return cases.stream()
                .map(tc -> tc.getId() != null ? tc.getId().intValue() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 获取项目下的模块ID集合
     */
    private Set<Integer> getModuleIdsByProjects(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Set.of();
        }
        List<Module> modules = moduleMapper.selectList(
                new LambdaQueryWrapper<Module>()
                        .in(Module::getProjectId, projectIds)
        );
        return modules.stream()
                .map(Module::getModuleId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取模块ID集合下的API ID集合
     */
    private Set<Integer> getApiIdsByModuleIds(Set<Integer> moduleIds) {
        if (moduleIds.isEmpty()) {
            return Set.of();
        }
        List<ApiRequest> apis = apiRequestMapper.selectList(
                new LambdaQueryWrapper<ApiRequest>()
                        .in(ApiRequest::getModuleId, moduleIds)
        );
        return apis.stream()
                .map(ApiRequest::getId)
                .map(Number::intValue)
                .collect(Collectors.toSet());
    }

    /**
     * 获取执行引用的名称
     */
    private String getRefName(TestExecution exec) {
        if (exec.getScopeName() != null && !exec.getScopeName().isEmpty()) {
            return exec.getScopeName();
        }

        // 根据 scope 类型获取对应名称
        String scope = exec.getExecutionScope();
        Integer refId = exec.getRefId();

        if (scope == null || refId == null) {
            return "未知";
        }

        return switch (scope) {
            case "project" -> {
                Project project = projectMapper.selectById(refId.longValue());
                yield project != null ? project.getName() : "项目#" + refId;
            }
            case "test_case" -> {
                TestCase testCase = testCaseMapper.selectById(refId.longValue());
                yield testCase != null ? testCase.getName() : "用例#" + refId;
            }
            case "api" -> {
                ApiRequest api = apiRequestMapper.selectById(refId.longValue());
                yield api != null ? api.getName() : "API#" + refId;
            }
            default -> scope + "#" + refId;
        };
    }
}
