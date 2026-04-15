package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HTTP 方法
 */
@Getter
@AllArgsConstructor
public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS");

    private final String code;
}
