package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 项目实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("projects")
public class Project extends BaseEntity {

    /**
     * 项目ID
     */
    @TableId(value = "project_id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目编码
     */
    private String projectCode;

    /**
     * 项目类型：WEB, MOBILE, API, DESKTOP, HYBRID
     */
    private String projectType;

    /**
     * 项目状态：ACTIVE, INACTIVE, ARCHIVED
     */
    private String status;

    /**
     * 项目头像URL
     */
    private String avatarUrl;

    // ========== 额外字段（代码中使用但数据库可能没有）==========

    /**
     * 项目编号（代码中使用，等同于projectCode）
     */
    @TableField(exist = false)
    private String code;

    /**
     * 开始日期
     */
    @TableField(exist = false)
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField(exist = false)
    private LocalDate endDate;

    /**
     * 负责人ID
     */
    @TableField(exist = false)
    private Long ownerId;

    /**
     * 图标颜色
     */
    @TableField(exist = false)
    private String iconColor;
}
