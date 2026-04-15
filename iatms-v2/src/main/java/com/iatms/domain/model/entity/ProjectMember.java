package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目成员实体
 */
@Data
@TableName("projectmembers")
public class ProjectMember {

    /**
     * 成员关系ID
     */
    @TableId(value = "member_id", type = IdType.AUTO)
    private Integer memberId;

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 权限级别：read, write, admin
     */
    private String permissionLevel;

    /**
     * 项目角色：owner, manager, developer, tester, viewer
     */
    private String projectRole;

    /**
     * 成员状态：active, inactive, removed
     */
    private String status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 离开时间
     */
    private LocalDateTime leaveTime;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 分配任务数
     */
    private Integer assignedTasks;

    /**
     * 完成任务数
     */
    private Integer completedTasks;

    /**
     * 附加角色信息
     */
    private String additionalRoles;

    /**
     * 自定义权限配置
     */
    private String customPermissions;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 创建人ID
     */
    private Integer createdBy;

    /**
     * 更新人ID
     */
    private Integer updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
