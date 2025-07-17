package com.rental.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限验证注解
 * 用于方法级别的权限控制
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 所需权限名称
     */
    String value();

    /**
     * 是否必须拥有所有权限（默认为true）
     * true: 必须拥有指定的所有权限
     * false: 拥有任意一个权限即可
     */
    boolean requireAll() default true;
}
