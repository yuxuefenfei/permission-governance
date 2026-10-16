package com.example.permissionhub.security;

import com.example.permissionhub.enums.PermissionKind;
import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

/**
 * 一个注解同时表达授权规则和可发现的权限元数据。
 * 仅允许标在方法上，避免类级/方法级同时检查导致扫描与执行语义不一致。
 * 注意：需由 SecurityConfig 启用 Spring Security 注解模板支持。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@permissionChecker.hasPermission('{code}')")
public @interface RequirePermission {
    String code();
    String name();
    String module();
    PermissionKind kind() default PermissionKind.ACTION;
    String description() default "";
}
