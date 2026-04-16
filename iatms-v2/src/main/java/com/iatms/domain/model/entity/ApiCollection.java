package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 集合（树形结构）
 * 注意：此实体映射到 modules 表，因为数据库中没有 api_collection 表
 * API 通过 module_id 关联到模块（集合）
 */
@Data
@TableName("modules")
public class ApiCollection {

    /**
     * 模块ID（作为集合ID）
     */
    @TableId(value = "module_id", type = IdType.AUTO)
    private Integer moduleId;

    /**
     * 集合编码
     */
    @TableField("module_code")
    private String code;

    /**
     * 项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 父级集合ID
     */
    @TableField("parent_module_id")
    private Integer parentId;

    /**
     * 集合名称
     */
    private String name;

    /**
     * 集合描述
     */
    private String description;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer orderNum;

    /**
     * 标签信息（JSON）
     */
    private String tags;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Integer createdBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic(delval = "1", value = "0")
    @TableField("is_deleted")
    private Boolean deleted = false;

    // ========== 兼容性别名 ==========

    /**
     * ID别名
     */
    @TableField(exist = false)
    private Long id;

    public Long getId() {
        return this.moduleId != null ? this.moduleId.longValue() : null;
    }

    public void setId(Long id) {
        this.moduleId = id != null ? id.intValue() : null;
    }

    /**
     * 编码别名
     */
    @TableField(exist = false)
    private String collectionCode;

    public String getCollectionCode() {
        return this.code;
    }

    public void setCollectionCode(String collectionCode) {
        this.code = collectionCode;
    }

    /**
     * 创建时间（数据库中没有created_at用于ApiCollection，但Module有）
     */
    @TableField(exist = false)
    private LocalDateTime createdAt;

}
