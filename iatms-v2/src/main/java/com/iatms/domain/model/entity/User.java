package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
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
    private Integer departmentId;

    /**
     * 员工工号
     */
    private String employeeId;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 最后登录时间
     */
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

    // ========== 额外字段（代码中使用）==========

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
}
