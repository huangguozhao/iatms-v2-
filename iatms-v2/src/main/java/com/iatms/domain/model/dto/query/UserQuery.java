package com.iatms.domain.model.dto.query;

import lombok.Data;

/**
 * 用户查询条件
 */
@Data
public class UserQuery {

    private String keyword;
    private String status;
    private String position;
    private Integer departmentId;
    private String startDate;
    private String endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
