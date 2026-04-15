package com.iatms.application.project.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.project.ProjectQueryService;
import com.iatms.application.project.dto.query.ProjectQuery;
import com.iatms.domain.model.entity.Project;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.ProjectDetailVO;
import com.iatms.domain.model.vo.ProjectSummaryVO;
import com.iatms.infrastructure.persistence.mapper.ProjectMapper;
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

    @Override
    public ApiResponse.PageResult<ProjectSummaryVO> queryProjects(ProjectQuery query, Long userId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Project::getName, query.getKeyword())
                    .or().like(Project::getCode, query.getKeyword())
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
        if ("DESC".equalsIgnoreCase(query.getSortOrder())) {
            wrapper.orderByDesc(Project::getCreatedAt);
        } else {
            wrapper.orderByAsc(Project::getCreatedAt);
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

        // TODO: 查询项目成员列表
        List<ProjectDetailVO.ProjectMemberVO> members = new ArrayList<>();

        // TODO: 统计模块、接口、用例数量
        Integer totalModules = 0;
        Integer totalApis = 0;
        Integer totalTestCases = 0;

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
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getDeleted, false)
                .and(w -> w.eq(Project::getOwnerId, userId))  // TODO: 还要查询作为成员的
                .orderByDesc(Project::getCreatedAt);

        List<Project> projects = projectMapper.selectList(wrapper);
        return convertToSummaryVO(projects);
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
