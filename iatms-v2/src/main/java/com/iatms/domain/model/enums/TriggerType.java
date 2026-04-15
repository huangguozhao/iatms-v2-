package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 触发类型
 */
@Getter
@AllArgsConstructor
public enum TriggerType {
    CRON("CRON", "Cron表达式"),
    INTERVAL("INTERVAL", "固定间隔"),
    ONCE("ONCE", "单次执行");

    private final String code;
    private final String description;
}
