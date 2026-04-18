package com.iatms.domain.model.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户命令
 */
@Data
public class CreateUserCmd {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度需在2-50个字符之间")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(min = 6, message = "密码长度至少6位")
    private String password;

    private String phone;

    private String position;

    private Integer departmentId;

    private String employeeId;

    private String description;

    private String status;

    private String role;
}
