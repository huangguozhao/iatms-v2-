package com.iatms.application.testing.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建测试用例命令
 */
@Data
public class CreateTestCaseCmd {

    @NotBlank(message = "用例名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    private Long moduleId;

    private Long apiId;

    private String testType = "FUNCTIONAL";

    private String priority = "P2";

    private String preconditions;

    private String testSteps;

    private String testData;

    private String headers;

    private String requestParams;

    private String requestBody;

    private String assertions;

    private String expectedResponse;

    private String extractors;

    private String status = "DRAFT";

    private Boolean isEnabled;

    private Integer expectedHttpStatus;

    private String requestOverride;

    private String expectedResponseSchema;

    private String expectedResponseBody;
}
