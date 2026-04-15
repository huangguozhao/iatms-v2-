package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试套件与用例的关联表
 * 对应数据库表: suitecasemappings
 */
@Data
@TableName("suitecasemappings")
public class TestSuiteRequest {

    /**
     * 映射ID
     */
    @TableId(value = "mapping_id", type = IdType.AUTO)
    private Long id;

    /**
     * 测试套件ID
     */
    private Integer suiteId;

    /**
     * 用例ID
     */
    private Integer caseId;

    /**
     * 关联类型
     */
    private String relationType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ========== 兼容性别名（用于旧代码）==========

    /**
     * 测试套件ID别名
     */
    @TableField(exist = false)
    private Long testSuiteId;

    public Long getTestSuiteId() {
        return this.suiteId != null ? this.suiteId.longValue() : null;
    }

    public void setTestSuiteId(Long testSuiteId) {
        this.suiteId = testSuiteId != null ? testSuiteId.intValue() : null;
    }

    /**
     * API请求ID别名
     */
    @TableField(exist = false)
    private Long apiRequestId;

    public Long getApiRequestId() {
        return this.caseId != null ? this.caseId.longValue() : null;
    }

    public void setApiRequestId(Long apiRequestId) {
        this.caseId = apiRequestId != null ? apiRequestId.intValue() : null;
    }

    /**
     * 排序号（数据库中不存在，仅内存使用）
     */
    @TableField(exist = false)
    private Integer sortOrder;

    /**
     * 覆盖请求头（数据库中不存在）
     */
    @TableField(exist = false)
    private String overrideHeaders;

    /**
     * 覆盖请求体（数据库中不存在）
     */
    @TableField(exist = false)
    private String overrideBody;

    /**
     * 覆盖断言（数据库中不存在）
     */
    @TableField(exist = false)
    private String overrideAssertions;

    /**
     * 是否启用（数据库中不存在）
     */
    @TableField(exist = false)
    private Boolean enabled = true;
}
