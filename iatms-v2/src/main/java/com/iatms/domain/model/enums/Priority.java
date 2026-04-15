package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优先级
 */
@Getter
@AllArgsConstructor
public enum Priority {
    P0("P0", "阻塞", 0),
    P1("P1", "高", 1),
    P2("P2", "中", 2),
    P3("P3", "低", 3);

    private final String code;
    private final String description;
    private final int level;
}
