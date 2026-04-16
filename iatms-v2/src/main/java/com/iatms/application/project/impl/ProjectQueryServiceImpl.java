package com.iatms.application.project.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.project.ProjectQueryService;
import com.iatms.application.project.dto.query.ProjectQuery;
import com.iatms.domain.model.entity.ApiRequest;
import com.iatms.domain.model.entity.Module;
import com.iatms.domain.model.entity.Project;
import com.iatms.domain.model.entity.ProjectMember;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.entity.TestExecution;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.ProjectDetailVO;
import com.iatms.domain.model.vo.ProjectMemberVO;
import com.iatms.domain.model.vo.ProjectSummaryVO;
import com.iatms.infrastructure.persistence.mapper.ApiRequestMapper;
import com.iatms.infrastructure.persistence.mapper.ModuleMapper;
import com.iatms.infrastructure.persistence.mapper.ProjectMapper;
import com.iatms.infrastructure.persistence.mapper.ProjectMemberMapper;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.infrastructure.persistence.mapper.TestExecutionMapper;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectQueryServiceImpl implements ProjectQueryService {

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ModuleMapper moduleMapper;
    private final ApiRequestMapper apiRequestMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper testExecutionMapper;

    @Override
    public ApiResponse.PageResult<ProjectSummaryVO> queryProjects(ProjectQuery query, Long userId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Project::getName, query.getKeyword())
                    .or().like(Project::getProjectCode, query.getKeyword())
                    .or().like(Project::getDescription, query.getKeyword()));
        }

        // 状态过滤
        if (query.getStatus() != null) {
            wrapper.eq(Project::getStatus, query.getStatus());
        }

        // 项目类型过滤
        if (query.getProjectType() != null) {
            wrapper.eq(Project::getProjectType, query.getProjectType());
        }

        // 负责人过滤
        if (query.getOwnerId() != null) {
            wrapper.eq(Project::getOwnerId, query.getOwnerId());
        }

        // 只查询未删除的
        wrapper.eq(Project::getDeleted, false);

        // 排序
        String sortBy = query.getSortBy();
        boolean isDesc = "DESC".equalsIgnoreCase(query.getSortOrder());

        if ("name".equalsIgnoreCase(sortBy)) {
            if (isDesc) {
                wrapper.orderByDesc(Project::getName);
            } else {
                wrapper.orderByAsc(Project::getName);
            }
        } else if ("updatedAt".equalsIgnoreCase(sortBy)) {
            if (isDesc) {
                wrapper.orderByDesc(Project::getUpdatedAt);
            } else {
                wrapper.orderByAsc(Project::getUpdatedAt);
            }
        } else {
            // 默认按创建时间排序
            if (isDesc) {
                wrapper.orderByDesc(Project::getCreatedAt);
            } else {
                wrapper.orderByAsc(Project::getCreatedAt);
            }
        }

        // 分页查询
        IPage<Project> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Project> result = projectMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<ProjectSummaryVO> voList = convertToSummaryVO(result.getRecords());

        return ApiResponse.PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public ProjectDetailVO getProjectDetail(Long projectId, Long userId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.PROJECT_NOT_FOUND.getCode(),
                    ErrorCode.PROJECT_NOT_FOUND.getMessage());
        }

        User owner = userMapper.selectById(project.getOwnerId());

        // 查询项目成员列表
        List<ProjectMember> membersList = projectMemberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getStatus, "active")
        );

        // 获取成员用户信息
        List<Integer> memberUserIds = membersList.stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());
        Map<Long, User> memberUserMap;
        if (memberUserIds.isEmpty()) {
            memberUserMap = Map.of();
        } else {
            memberUserMap = userMapper.selectBatchIds(memberUserIds).stream()
                    .collect(Collectors.toMap(u -> u.getId().longValue(), u -> u));
        }

        List<ProjectDetailVO.ProjectMemberVO> members = membersList.stream()
                .map(m -> {
                    User user = memberUserMap.get(m.getUserId().longValue());
                    return ProjectDetailVO.ProjectMemberVO.builder()
                            .userId(Long.valueOf(m.getUserId()))
                            .userName(user != null ? user.getName() : null)
                            .displayName(user != null ? user.getDisplayName() : null)
                            .avatar(user != null ? user.getAvatarUrl() : null)
                            .role(m.getProjectRole())
                            .joinedAt(m.getJoinTime())
                            .build();
                })
                .collect(Collectors.toList());

        // 统计模块数量
        Integer totalModules = moduleMapper.selectCount(
                new LambdaQueryWrapper<Module>()
                        .eq(Module::getProjectId, projectId.intValue())
                        .eq(Module::getIsDeleted, false)
        ).intValue();

        // 统计接口数量 - 通过模块关联
        List<Module> projectModules = moduleMapper.selectList(
                new LambdaQueryWrapper<Module>()
                        .eq(Module::getProjectId, projectId.intValue())
                        .eq(Module::getIsDeleted, false)
        );
        List<Long> moduleIds = projectModules.stream()
                .filter(m -> m.getModuleId() != null)
                .map(m -> m.getModuleId().longValue())
                .collect(Collectors.toList());

        Integer totalApis = 0;
        if (!moduleIds.isEmpty()) {
            totalApis = apiRequestMapper.selectCount(
                    new LambdaQueryWrapper<ApiRequest>()
                            .in(ApiRequest::getModuleId, moduleIds.stream().map(Long::intValue).collect(Collectors.toList()))
                            .eq(ApiRequest::getDeleted, false)
            ).intValue();
        }

        // 统计测试用例数量 - 通过接口关联
        List<ApiRequest> moduleApis = moduleIds.isEmpty() ? List.of() :
                apiRequestMapper.selectList(
                        new LambdaQueryWrapper<ApiRequest>()
                                .in(ApiRequest::getModuleId, moduleIds.stream().map(Long::intValue).collect(Collectors.toList()))
                                .eq(ApiRequest::getDeleted, false)
                );
        List<Long> apiIds = moduleApis.stream()
                .filter(a -> a.getId() != null)
                .map(a -> a.getId().longValue())
                .collect(Collectors.toList());

        Integer totalTestCases = 0;
        Integer passedCount = 0;
        Integer failedCount = 0;
        Integer notExecutedCount = 0;

        if (!apiIds.isEmpty()) {
            totalTestCases = testCaseMapper.selectCount(
                    new LambdaQueryWrapper<TestCase>()
                            .in(TestCase::getApiId, apiIds.stream().map(Long::intValue).collect(Collectors.toList()))
                            .eq(TestCase::getDeleted, false)
            ).intValue();

            // 统计执行情况 - 获取每个用例的最新执行记录
            List<TestCase> testCases = testCaseMapper.selectList(
                    new LambdaQueryWrapper<TestCase>()
                            .in(TestCase::getApiId, apiIds.stream().map(Long::intValue).collect(Collectors.toList()))
                            .eq(TestCase::getDeleted, false)
            );

            notExecutedCount = totalTestCases; // 初始化为全部未执行

            for (TestCase tc : testCases) {
                if (tc.getId() == null) continue;

                // 查询该用例的最新执行记录
                TestExecution latestExecution = null;
                List<TestExecution> executions = testExecutionMapper.selectList(
                        new LambdaQueryWrapper<TestExecution>()
                                .eq(TestExecution::getExecutionScope, "test_case")
                                .eq(TestExecution::getRefId, tc.getId().intValue())
                                .orderByDesc(TestExecution::getUpdatedAt)
                                .last("LIMIT 1")
                );

                if (!executions.isEmpty()) {
                    latestExecution = executions.get(0);
                    notExecutedCount--; // 有执行记录的用例，从未执行中扣除

                    // 根据执行状态统计
                    String status = latestExecution.getStatus();
                    if ("completed".equals(status) || "passed".equals(status) || "success".equals(status)) {
                        passedCount++;
                    } else if ("failed".equals(status) || "error".equals(status)) {
                        failedCount++;
                    }
                    // running、cancelled 等状态不计入通过或失败
                }
            }

            // 确保不会变成负数
            notExecutedCount = Math.max(0, notExecutedCount);
        }

        return ProjectDetailVO.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .projectType(project.getProjectType())
                .status(project.getStatus())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .ownerId(project.getOwnerId())
                .ownerName(owner != null ? owner.getDisplayName() : null)
                .iconColor(project.getIconColor())
                .members(members)
                .totalModules(totalModules)
                .totalApis(totalApis)
                .totalTestCases(totalTestCases)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .notExecutedCount(notExecutedCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .creatorName(owner != null ? owner.getDisplayName() : null)
                .build();
    }

    @Override
    public List<ProjectSummaryVO> getRecentProjects(Long userId, int limit) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getDeleted, false)
                .orderByDesc(Project::getUpdatedAt)
                .last("LIMIT " + limit);

        List<Project> projects = projectMapper.selectList(wrapper);
        return convertToSummaryVO(projects);
    }

    @Override
    public List<ProjectSummaryVO> getUserProjects(Long userId) {
        // 查询用户作为所有者的项目
        LambdaQueryWrapper<Project> ownerWrapper = new LambdaQueryWrapper<>();
        ownerWrapper.eq(Project::getDeleted, false)
                .eq(Project::getOwnerId, userId);

        // 查询用户作为成员的项目
        List<ProjectMember> memberships = projectMemberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getUserId, userId.intValue())
                        .eq(ProjectMember::getStatus, "active")
        );
        List<Long> memberProjectIds = memberships.stream()
                .map(m -> Long.valueOf(m.getProjectId()))
                .collect(Collectors.toList());

        List<Project> ownerProjects = projectMapper.selectList(ownerWrapper);

        // 合并并去重
        List<Project> allProjects = new ArrayList<>(ownerProjects);
        if (!memberProjectIds.isEmpty()) {
            LambdaQueryWrapper<Project> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(Project::getDeleted, false)
                    .in(Project::getId, memberProjectIds);
            List<Project> memberProjects = projectMapper.selectList(memberWrapper);
            for (Project p : memberProjects) {
                if (!allProjects.contains(p)) {
                    allProjects.add(p);
                }
            }
        }

        return convertToSummaryVO(allProjects);
    }

    private List<ProjectSummaryVO> convertToSummaryVO(List<Project> projects) {
        if (projects.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有负责人信息
        List<Long> ownerIds = projects.stream()
                .map(Project::getOwnerId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> ownerMap = userMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return projects.stream()
                .map(p -> {
                    User owner = ownerMap.get(p.getOwnerId());
                    return ProjectSummaryVO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .code(p.getCode())
                            .projectType(p.getProjectType())
                            .status(p.getStatus())
                            .startDate(p.getStartDate())
                            .endDate(p.getEndDate())
                            .ownerId(p.getOwnerId())
                            .ownerName(owner != null ? owner.getDisplayName() : null)
                            .iconColor(p.getIconColor())
                            .createdAt(p.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
