package com.iatms.application.system;

import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.enums.ProjectRole;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 检查用户是否拥有指定项目的指定权限
     *
     * @param userId     用户ID
     * @param projectId  项目ID
     * @param permission 权限
     * @return true=有权限, false=无权限
     */
    boolean hasProjectPermission(Long userId, Long projectId, ProjectPermission permission);

    /**
     * 检查用户是否是项目创建人/负责人
     *
     * @param userId    用户ID
     * @param projectId  项目ID
     * @return true=是创建人, false=不是创建人
     */
    boolean isProjectOwner(Long userId, Long projectId);

    /**
     * 检查用户是否是系统管理员
     *
     * @param userId 用户ID
     * @return true=是管理员, false=不是管理员
     */
    boolean isSystemAdmin(Long userId);

    /**
     * 获取用户在项目中的角色
     *
     * @param userId    用户ID
     * @param projectId  项目ID
     * @return 角色，如果用户不是项目成员则返回null
     */
    ProjectRole getUserProjectRole(Long userId, Long projectId);

    /**
     * 获取用户所有可访问的项目ID列表
     * 包括：自己创建的项目 + 作为成员的项目
     *
     * @param userId 用户ID
     * @return 项目ID列表
     */
    List<Long> getAccessibleProjectIds(Long userId);

    /**
     * 检查用户是否可以访问指定项目
     *
     * @param userId    用户ID
     * @param projectId 项目ID
     * @return true=可访问, false=不可访问
     */
    boolean canAccessProject(Long userId, Long projectId);
}
