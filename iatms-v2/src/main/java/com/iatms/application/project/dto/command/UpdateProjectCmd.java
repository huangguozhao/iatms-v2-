package com.iatms.application.project.dto.command;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新项目命令
 */
@Data
public class UpdateProjectCmd {

    @Size(max = 200, message = "项目名称不能超过200字符")
    private String name;

    private String description;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private String iconColor;
}
