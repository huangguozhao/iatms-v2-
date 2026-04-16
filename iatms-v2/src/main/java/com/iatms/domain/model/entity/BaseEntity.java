package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体继承此类，包含审计字段、软删除标记、乐观锁
 */
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * 主键 - 自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic(delval = "1", value = "0")
    @TableField("is_deleted")
    private Boolean deleted = false;

    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;
}
