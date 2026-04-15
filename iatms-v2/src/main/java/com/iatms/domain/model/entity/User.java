package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 * 对应数据库表: users
 */
@Data
@TableName("users")
public class User {

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户密码（加密存储）
     */
    private String password;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Integer departmentId;

    /**
     * 员工工号
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 创建人ID
     */
    @TableField("creator_id")
    private Integer creatorId;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 职位
     */
    private String position;

    /**
     * 备注/描述
     */
    private String description;

    /**
     * 用户状态：active, inactive, pending
     */
    private String status;

    /**
     * 用户角色：admin-管理员，user-普通用户
     */
    private String role;

    /**
     * 账户创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @TableLogic(delval = "1", value = "0")
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    private Integer deletedBy;

    // ========== 兼容性别名（用于旧代码）==========

    /**
     * 用户名（代码中使用，等同于name）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 显示名称（代码中使用，等同于name）
     */
    @TableField(exist = false)
    private String displayName;

    /**
     * 是否为管理员
     */
    @TableField(exist = false)
    private Boolean isAdmin;

    public String getUsername() {
        return this.name;
    }

    public void setUsername(String username) {
        this.name = username;
    }

    public String getDisplayName() {
        return this.name;
    }

    public void setDisplayName(String displayName) {
        this.name = displayName;
    }

    public Boolean getIsAdmin() {
        return "admin".equals(this.role);
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
