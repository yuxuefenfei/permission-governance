package com.example.permissionhub.dto;

import com.example.permissionhub.enums.PermissionKind;

import java.util.List;

/**
 * 一次完整扫描得到的不可变权限定义，防止注册中心与 Web 层互相耦合。
 */
public record ScannedPermission(String code, String name, String module,
                                PermissionKind kind, String description,
                                List<ScannedEndpoint> endpoints) {
}
