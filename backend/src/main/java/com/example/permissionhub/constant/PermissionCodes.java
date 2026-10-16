package com.example.permissionhub.constant;

/**
 * 权限码唯一的 Java 定义位置；不要在枚举、SQL、Controller 中再次手写同一权限码。
 * @RequirePermission 提供中文名称等业务元数据，扫描后进入权限资源池。
 */
public final class PermissionCodes {
    private PermissionCodes() { }

    public static final String DASHBOARD_READ = "system:dashboard:read";
    public static final String PERMISSION_READ = "system:permission:read";
    public static final String PERMISSION_SYNC = "system:permission:sync";
    public static final String MENU_MANAGE = "system:menu:manage";
    public static final String ROLE_MANAGE = "system:role:manage";
    public static final String USER_MANAGE = "system:user:manage";
    public static final String AUDIT_READ = "system:audit:read";
    public static final String DEVICE_READ = "sop:fireSafety:deviceInfo";
    public static final String DEVICE_CREATE = "sop:fireSafety:deviceInfo:create";
}
