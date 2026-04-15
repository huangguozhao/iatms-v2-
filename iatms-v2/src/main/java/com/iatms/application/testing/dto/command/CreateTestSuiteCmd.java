package com.iatms.application.testing.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建测试套件命令
 */
@Data
public class CreateTestSuiteCmd {

    @NotBlank(message = "套件名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    private Long environmentId;

    private List<Long> requestIds;

    private String status = "ACTIVE";
}
