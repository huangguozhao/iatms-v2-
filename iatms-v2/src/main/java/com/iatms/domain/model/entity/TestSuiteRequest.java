package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试套件与请求的关联表
 * 核心设计：同一请求在套件中可以有独立的断言配置
 */
@Data
@TableName("test_suite_request")
public class TestSuiteRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long testSuiteId;

    private Long apiRequestId;

    private Integer sortOrder;

    private String overrideHeaders;

    private String overrideBody;

    private String overrideAssertions;

    private LocalDateTime createdAt;

    private Boolean enabled = true;
}
