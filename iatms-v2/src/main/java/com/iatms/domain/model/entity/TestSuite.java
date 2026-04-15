package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试套件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("testsuites")
public class TestSuite extends BaseEntity {

    /**
     * 套件ID
     */
    @TableId(value = "suite_id", type = IdType.AUTO)
    private Long id;

    /**
     * 套件名称
     */
    private String suiteName;

    /**
     * 套件编码
     */
    private String suiteCode;

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 模块ID
     */
    private Integer moduleId;

    /**
     * 套件描述
     */
    private String description;

    /**
     * 版本号（套件版本，如1.0, 2.1）
     */
    private String suiteVersion;

    /**
     * 状态：active, inactive, archived
     */
    private String status;

    /**
     * 父套件ID
     */
    private Integer parentSuiteId;

    /**
     * 套件类型：MODULE, FEATURE, EPIC, PACKAGE
     */
    private String suiteType;

    /**
     * 套件顺序
     */
    private Integer suiteOrder;

    /**
     * 总用例数
     */
    private Integer totalCases;

    /**
     * 通过用例数
     */
    private Integer passedCases;

    /**
     * 失败用例数
     */
    private Integer failedCases;

    /**
     * 跳过用例数
     */
    private Integer skippedCases;

    /**
     * 成功率
     */
    private java.math.BigDecimal successRate;

    /**
     * 标签（JSON）
     */
    private String tags;

    /**
     * 自定义属性（JSON）
     */
    private String customProperties;
}
