package com.iatms.application.testing.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建 API 请求命令
 */
@Data
public class CreateApiRequestCmd {

    @NotBlank(message = "API名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "所属集合不能为空")
    private Long collectionId;

    private String requestType = "HTTP";

    @NotBlank(message = "HTTP方法不能为空")
    private String httpMethod;

    @NotBlank(message = "请求URL不能为空")
    private String url;

    private String headers;

    private String queryParams;

    private String requestBody;

    private String authConfig;

    private String preScript;

    private String postScript;

    private String assertions;

    private Integer orderNum = 0;

    private String status = "DRAFT";
}
