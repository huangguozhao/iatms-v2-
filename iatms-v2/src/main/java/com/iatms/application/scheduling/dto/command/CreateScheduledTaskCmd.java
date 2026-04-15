package com.iatms.application.scheduling.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建定时任务命令
 */
@Data
public class CreateScheduledTaskCmd {

    @NotBlank(message = "任务名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    @NotBlank(message = "触发类型不能为空")
    private String triggerType;

    private String cronExpression;

    private Integer intervalSeconds;

    private LocalDateTime executeAt;

    @NotNull(message = "关联对象不能为空")
    private Long targetId;

    private Long environmentId;

    private Boolean notifyOnSuccess = false;

    private Boolean notifyOnFailure = true;
}
