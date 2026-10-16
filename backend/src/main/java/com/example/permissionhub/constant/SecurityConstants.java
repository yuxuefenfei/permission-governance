package com.example.permissionhub.constant;

/** 安全相关固定值；超级管理员权限不应散落在 Controller 注解中。 */
public final class SecurityConstants {
    private SecurityConstants() { }
    public static final String LEGACY_SUPER_AUTHORITY = "*:*:*";
}
