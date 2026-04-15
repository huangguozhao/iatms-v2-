package com.iatms.application.project.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建项目命令
 */
@Data
public class CreateProjectCmd {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 200, message = "项目名称不能超过200字符")
    private String name;

    @NotBlank(message = "项目编号不能为空")
    @Size(max = 100, message = "项目编号不能超过100字符")
    private String code;

    private String description;

    private String projectType = "HTTP";

    private String status = "NOT_STARTED";

    private LocalDate startDate;

    private LocalDate endDate;

    private String iconColor;
}
