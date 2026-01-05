package com.makura.dashboard.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for permission-based access control
 * Usage: @RequiresPermission("routes:create")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    /**
     * The permission(s) required to access this resource
     * Examples: "routes:create", "users:manage", "metrics:view"
     */
    String[] value();

    /**
     * If true, user must have ALL specified permissions
     * If false, user needs ANY of the specified permissions
     */
    boolean requireAll() default false;
}



