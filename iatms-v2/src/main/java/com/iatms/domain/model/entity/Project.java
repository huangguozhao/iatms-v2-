package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体
 * 对应数据库表: projects
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

    /**
     * 版本号
     */
    private Integer version;

    // ========== 兼容性别名（用于旧代码使用creatorId/ownerId）==========

    /**
     * 获取创建人ID（兼容方法，返回Integer）
     */
    public Integer getCreatorId() {
        Long createdBy = getCreatedBy();
        return createdBy != null ? createdBy.intValue() : null;
    }

    /**
     * 设置创建人ID（兼容方法）
     */
    public void setCreatorId(Integer creatorId) {
        setCreatedBy(creatorId != null ? creatorId.longValue() : null);
    }

    /**
     * 获取更新人ID（兼容方法，返回Integer）
     */
    public Integer getUpdaterId() {
        Long updatedBy = getUpdatedBy();
        return updatedBy != null ? updatedBy.intValue() : null;
    }

    /**
     * 设置更新人ID（兼容方法）
     */
    public void setUpdaterId(Integer updaterId) {
        setUpdatedBy(updaterId != null ? updaterId.longValue() : null);
    }

    // ========== 虚拟字段（数据库中不存在）==========

    /**
     * 项目编号别名
     */
    @TableField(exist = false)
    private String code;

    public String getCode() {
        return this.projectCode;
    }

    public void setCode(String code) {
        this.projectCode = code;
    }

    /**
     * 开始日期（数据库中不存在）
     */
    @TableField(exist = false)
    private LocalDate startDate;

    /**
     * 结束日期（数据库中不存在）
     */
    @TableField(exist = false)
    private LocalDate endDate;

    /**
     * 负责人ID（数据库中不存在）
     */
    @TableField(exist = false)
    private Long ownerId;

    /**
     * 图标颜色（数据库中不存在）
     */
    @TableField(exist = false)
    private String iconColor;
}
