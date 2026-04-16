package com.iatms.application.project.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.project.ProjectCommandService;
import com.iatms.application.project.dto.command.CreateProjectCmd;
import com.iatms.application.project.dto.command.UpdateProjectCmd;
import com.iatms.application.project.dto.query.ProjectQuery;
import com.iatms.domain.model.entity.Project;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.ProjectDetailVO;
import com.iatms.domain.model.vo.ProjectMemberVO;
import com.iatms.domain.model.vo.ProjectSummaryVO;
import com.iatms.infrastructure.persistence.mapper.ProjectMapper;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import com.iatms.common.exception.BusinessException;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.domain.model.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目命令服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectCommandServiceImpl implements ProjectCommandService {

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDetailVO createProject(CreateProjectCmd cmd, Long userId) {
        // 检查项目编号唯一性
        LambdaQueryWrapper<Project> codeCheck = new LambdaQueryWrapper<>();
        codeCheck.eq(Project::getProjectCode, cmd.getCode());
        if (projectMapper.selectCount(codeCheck) > 0) {
            throw new BusinessException(ErrorCode.PROJECT_CODE_EXISTS.getCode(),
                    ErrorCode.PROJECT_CODE_EXISTS.getMessage());
        }

        // 检查项目名称唯一性
        LambdaQueryWrapper<Project> nameCheck = new LambdaQueryWrapper<>();
        nameCheck.eq(Project::getName, cmd.getName());
        if (projectMapper.selectCount(nameCheck) > 0) {
            throw new BusinessException(ErrorCode.PROJECT_NAME_EXISTS.getCode(),
                    ErrorCode.PROJECT_NAME_EXISTS.getMessage());
        }

        // 创建项目
        Project project = new Project();
        project.setName(cmd.getName());
        project.setCode(cmd.getCode());
        project.setDescription(cmd.getDescription());
        project.setProjectType(cmd.getProjectType());
        project.setStatus(cmd.getStatus());
        project.setStartDate(cmd.getStartDate());
        project.setEndDate(cmd.getEndDate());
        project.setIconColor(cmd.getIconColor());
        project.setCreatorId(userId.intValue());
        project.setUpdatedBy(userId);

        projectMapper.insert(project);

        log.info("创建项目成功: id={}, name={}, creatorId={}", project.getId(), project.getName(), userId);

        return getProjectDetail(project.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDetailVO updateProject(Long projectId, UpdateProjectCmd cmd, Long userId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException(ErrorCode.PROJECT_NOT_FOUND.getCode(),
                    ErrorCode.PROJECT_NOT_FOUND.getMessage());
        }

        // 更新字段
        if (cmd.getName() != null) {
            project.setName(cmd.getName());
        }
        if (cmd.getDescription() != null) {
            project.setDescription(cmd.getDescription());
        }
        if (cmd.getStatus() != null) {
            project.setStatus(cmd.getStatus());
        }
        if (cmd.getStartDate() != null) {
            project.setStartDate(cmd.getStartDate());
        }
        if (cmd.getEndDate() != null) {
            project.setEndDate(cmd.getEndDate());
        }
        if (cmd.getIconColor() != null) {
            project.setIconColor(cmd.getIconColor());
        }

        project.setUpdatedBy(userId);
        projectMapper.updateById(project);

        log.info("更新项目成功: id={}, updaterId={}", projectId, userId);

        return getProjectDetail(projectId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId, Long userId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException(ErrorCode.PROJECT_NOT_FOUND.getCode(),
                    ErrorCode.PROJECT_NOT_FOUND.getMessage());
        }

        // 检查权限（只有创建人可以删除）
        if (project.getCreatorId() == null || !project.getCreatorId().equals(userId.intValue())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "只有项目创建人可以删除项目");
        }

        // 逻辑删除 - 使用 deleteById 触发 @TableLogic 注解，将 is_deleted 设为 1
        projectMapper.deleteById(projectId);

        log.info("删除项目成功: id={}, operatorId={}", projectId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMember(Long projectId, Long userId, String role, Long operatorId) {
        // TODO: 实现添加项目成员的逻辑
        log.info("添加项目成员: projectId={}, userId={}, role={}, operatorId={}",
                projectId, userId, role, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long projectId, Long userId, Long operatorId) {
        // TODO: 实现移除项目成员的逻辑
        log.info("移除项目成员: projectId={}, userId={}, operatorId={}", projectId, userId, operatorId);
    }

    private ProjectDetailVO getProjectDetail(Long projectId, Long userId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException(ErrorCode.PROJECT_NOT_FOUND.getCode(),
                    ErrorCode.PROJECT_NOT_FOUND.getMessage());
        }

        User owner = userMapper.selectById(Long.valueOf(project.getCreatorId()));

        return ProjectDetailVO.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .projectType(project.getProjectType())
                .status(project.getStatus())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .ownerId(Long.valueOf(project.getCreatorId()))
                .ownerName(owner != null ? owner.getDisplayName() : null)
                .iconColor(project.getIconColor())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
