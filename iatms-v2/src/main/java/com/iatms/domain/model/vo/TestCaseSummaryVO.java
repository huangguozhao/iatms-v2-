package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试用例概要 VO
 */
@Data
@Builder
public class TestCaseSummaryVO {

    private Long id;
    private String caseCode;
    private String name;
    private String priority;
    private String status;
    private Long moduleId;
    private String moduleName;
    private Long apiId;
    private String apiName;
    private String creatorName;
    private Long createdBy;
    private LocalDateTime createdAt;
}
