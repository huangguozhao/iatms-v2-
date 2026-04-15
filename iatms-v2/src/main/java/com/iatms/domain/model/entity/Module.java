package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模块实体
 */
@Data
@TableName("modules")
public class Module {

    /**
     * 模块ID
     */
    @TableId(value = "module_id", type = IdType.AUTO)
    private Integer moduleId;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 父模块ID
     */
    private Integer parentModuleId;

    /**
     * 模块名称
     */
    private String name;

    /**
     * 模块描述
     */
    private String description;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 模块状态：active, inactive, archived
     */
    private String status;

    /**
     * 模块负责人
     */
    private Integer ownerId;

    /**
     * 标签信息
     */
    private String tags;

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

    /**
     * 是否删除
     */
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    private Integer deletedBy;
}
