package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 执行状态
 */
@Getter
@AllArgsConstructor
public enum ExecutionStatus {
    PENDING("PENDING", "待执行"),
    RUNNING("RUNNING", "执行中"),
    PAUSED("PAUSED", "已暂停"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "执行失败"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;
}
