package com.iatms.domain.model.dto.command;

import lombok.Data;

/**
 * 更新用户命令
 */
@Data
public class UpdateUserCmd {

    private String name;
    private String email;
    private String phone;
    private String position;
    private Integer departmentId;
    private String employeeId;
    private String description;
    private String status;
    private String role;
}
