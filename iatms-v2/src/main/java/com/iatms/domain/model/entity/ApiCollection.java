package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API 集合（树形结构）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_collection")
public class ApiCollection extends BaseEntity {

    /**
     * 集合名称
     */
    private String name;

    /**
     * 集合描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 父级集合ID（支持树形）
     */
    private Long parentId;

    /**
     * 创建人ID
     */
    private Long createdBy;
}
