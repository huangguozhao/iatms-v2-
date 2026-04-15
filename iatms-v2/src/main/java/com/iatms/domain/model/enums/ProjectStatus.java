package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目状态
 */
@Getter
@AllArgsConstructor
public enum ProjectStatus {
    NOT_STARTED("NOT_STARTED", "未开始"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String description;
}
