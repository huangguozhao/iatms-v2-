package com.iatms.common.annotation;

import com.iatms.domain.model.enums.ProjectPermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 标注在 Controller 方法上，标识该方法需要的权限
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限
     * 多个权限时，默认需要全部满足（AND）
     */
    ProjectPermission[] value();

    /**
     * 多个权限时的校验逻辑
     * AND = 需要全部满足
     * OR = 满足其一即可
     */
    Logical logical() default Logical.AND;

    enum Logical {
        AND,
        OR
    }
}
