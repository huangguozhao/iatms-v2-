package com.iatms.common.exception;

import lombok.Getter;

/**
 * 资源不存在异常
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceKey;

    public ResourceNotFoundException(String resourceKey, String message) {
        super(message);
        this.resourceKey = resourceKey;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceKey = "RESOURCE";
    }
}
