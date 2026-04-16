package com.iatms.application.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.application.system.PermissionService;
import com.iatms.domain.model.entity.Project;
import com.iatms.domain.model.entity.ProjectMember;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.enums.ProjectRole;
import com.iatms.infrastructure.persistence.mapper.ProjectMapper;
import com.iatms.infrastructure.persistence.mapper.ProjectMemberMapper;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 * 按照论文 3.2.1 用户权限管理功能 的权限控制机制实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;

    @Override
    public boolean hasProjectPermission(Long userId, Long projectId, ProjectPermission permission) {
        if (userId == null || projectId == null || permission == null) {
            return false;
        }

        // 1. 系统管理员拥有所有权限
        if (isSystemAdmin(userId)) {
            log.debug("系统管理员拥有所有权限: userId={}, permission={}", userId, permission);
            return true;
        }

        // 2. 项目创建人拥有项目所有权限
        if (isProjectOwner(userId, projectId)) {
            log.debug("项目创建人拥有项目所有权限: userId={}, projectId={}", userId, projectId);
            return true;
        }

        // 3. 检查项目成员权限
        ProjectMember member = getProjectMember(userId, projectId);
        if (member == null) {
            log.debug("用户不是项目成员: userId={}, projectId={}", userId, projectId);
            return false;
        }

        // 检查成员状态
        if (!"active".equals(member.getStatus())) {
            log.debug("用户不是项目有效成员: userId={}, projectId={}, status={}",
                    userId, projectId, member.getStatus());
            return false;
        }

        // 根据 project_role 获取该角色拥有的权限
        ProjectRole role = ProjectRole.fromCode(member.getProjectRole());
        Set<ProjectPermission> rolePermissions = ProjectPermission.getPermissionsByRole(role);

        boolean hasPermission = rolePermissions.contains(permission);
        log.debug("权限检查: userId={}, projectId={}, role={}, permission={}, hasPermission={}",
                userId, projectId, role, permission, hasPermission);

        return hasPermission;
    }

    @Override
    public boolean isProjectOwner(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }

        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeleted()) {
            return false;
        }

        // 项目创建人 (creator_id) 相当于 owner
        return project.getCreatorId() != null
                && project.getCreatorId().equals(userId.intValue());
    }

    @Override
    public boolean isSystemAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        User user = userMapper.selectById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    @Override
    public ProjectRole getUserProjectRole(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return null;
        }

        // 系统管理员在所有项目中相当于 owner
        if (isSystemAdmin(userId)) {
            return ProjectRole.owner;
        }

        // 项目创建人相当于 owner
        if (isProjectOwner(userId, projectId)) {
            return ProjectRole.owner;
        }

        ProjectMember member = getProjectMember(userId, projectId);
        if (member == null) {
            return null;
        }

        return ProjectRole.fromCode(member.getProjectRole());
    }

    @Override
    public List<Long> getAccessibleProjectIds(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<Long> projectIds = new ArrayList<>();

        // 系统管理员可访问所有未删除项目
        if (isSystemAdmin(userId)) {
            LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Project::getDeleted, false);
            List<Project> allProjects = projectMapper.selectList(wrapper);
            return allProjects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());
        }

        // 自己创建的项目
        LambdaQueryWrapper<Project> createdWrapper = new LambdaQueryWrapper<>();
        createdWrapper.eq(Project::getCreatedBy, userId)
                      .eq(Project::getDeleted, false);
        List<Project> createdProjects = projectMapper.selectList(createdWrapper);
        projectIds.addAll(createdProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList()));

        // 作为成员的项目
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getUserId, userId.intValue())
                    .eq(ProjectMember::getStatus, "active");
        List<ProjectMember> memberships = projectMemberMapper.selectList(memberWrapper);

        if (!memberships.isEmpty()) {
            List<Long> memberProjectIds = memberships.stream()
                    .map(m -> m.getProjectId().longValue())
                    .collect(Collectors.toList());

            LambdaQueryWrapper<Project> memberProjWrapper = new LambdaQueryWrapper<>();
            memberProjWrapper.in(Project::getId, memberProjectIds)
                             .eq(Project::getDeleted, false);
            List<Project> memberProjects = projectMapper.selectList(memberProjWrapper);
            projectIds.addAll(memberProjects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList()));
        }

        // 去重并返回
        return projectIds.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public boolean canAccessProject(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }

        // 系统管理员可以访问所有项目
        if (isSystemAdmin(userId)) {
            return true;
        }

        // 项目创建人可以访问项目
        if (isProjectOwner(userId, projectId)) {
            return true;
        }

        // 项目成员可以访问项目
        ProjectMember member = getProjectMember(userId, projectId);
        return member != null && "active".equals(member.getStatus());
    }

    /**
     * 获取用户在项目中的成员信息
     */
    private ProjectMember getProjectMember(Long userId, Long projectId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .eq(ProjectMember::getUserId, userId.intValue());
        return projectMemberMapper.selectOne(wrapper);
    }
}
