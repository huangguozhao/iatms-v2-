package com.iatms.application.dashboard.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.application.dashboard.DashboardService;
import com.iatms.domain.model.entity.*;
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

    @Override
    public DashboardStatisticsVO getStatistics() {
        // 统计项目数量
        Long projectCount = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, false)
        ).longValue();

        // 统计 API 数量
        Long apiCount = apiRequestMapper.selectCount(
                new LambdaQueryWrapper<ApiRequest>().eq(ApiRequest::getDeleted, false)
        ).longValue();

        // 统计测试用例数量
        Long testCaseCount = testCaseMapper.selectCount(
                new LambdaQueryWrapper<TestCase>().eq(TestCase::getDeleted, false)
        ).longValue();

        // 统计测试执行总数
        Long executionCount = testExecutionMapper.selectCount(null).longValue();

        // 统计今日执行次数
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        Long todayExecutionCount = testExecutionMapper.selectCount(
                new LambdaQueryWrapper<TestExecution>()
                        .ge(TestExecution::getUpdatedAt, todayStart)
        ).longValue();

        // 统计本周执行次数
        LocalDateTime weekStart = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).with(LocalTime.MIN);
        Long weekExecutionCount = testExecutionMapper.selectCount(
                new LambdaQueryWrapper<TestExecution>()
                        .ge(TestExecution::getUpdatedAt, weekStart)
        ).longValue();

        // 计算成功率
        LambdaQueryWrapper<TestExecution> completedWrapper = new LambdaQueryWrapper<TestExecution>()
                .eq(TestExecution::getStatus, "completed");
        Long completedCount = testExecutionMapper.selectCount(completedWrapper).longValue();

        LambdaQueryWrapper<TestExecution> failedWrapper = new LambdaQueryWrapper<TestExecution>()
                .eq(TestExecution::getStatus, "failed");
        Long failedCount = testExecutionMapper.selectCount(failedWrapper).longValue();

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
    public List<RecentActivityVO> getRecentActivities(int limit) {
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<TestExecution>()
                .orderByDesc(TestExecution::getUpdatedAt)
                .last("LIMIT " + limit);

        List<TestExecution> executions = testExecutionMapper.selectList(wrapper);

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
