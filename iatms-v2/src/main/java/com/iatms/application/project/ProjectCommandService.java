package com.iatms.application.project;

import com.iatms.application.project.dto.command.CreateProjectCmd;
import com.iatms.application.project.dto.command.UpdateProjectCmd;
import com.iatms.domain.model.vo.ProjectDetailVO;

/**
 * 项目命令服务接口
 */
public interface ProjectCommandService {

    /**
     * 创建项目
     */
    ProjectDetailVO createProject(CreateProjectCmd cmd, Long userId);

    /**
     * 更新项目
     */
    ProjectDetailVO updateProject(Long projectId, UpdateProjectCmd cmd, Long userId);

    /**
     * 删除项目
     */
    void deleteProject(Long projectId, Long userId);

    /**
     * 添加项目成员
     */
    void addMember(Long projectId, Long userId, String role, Long operatorId);

    /**
     * 移除项目成员
     */
    void removeMember(Long projectId, Long userId, Long operatorId);
}
