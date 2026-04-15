package com.iatms.application.testing.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始执行命令
 */
@Data
public class StartExecutionCmd {

    /**
     * 执行类型：TEST_CASE-单个用例，TEST_SUITE-套件，PROJECT-项目
     */
    @NotNull(message = "执行类型不能为空")
    private String executionType;

    /**
     * 目标ID（用例/套件/项目ID）
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 执行环境ID
     */
    private Long environmentId;

    /**
     * 环境变量（KV格式）
     */
    private java.util.Map<String, String> variables;

    /**
     * 触发类型
     */
    private String triggerType = "MANUAL";
}
