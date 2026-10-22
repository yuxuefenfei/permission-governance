package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * permission_endpoints 持久化映射，不作为公开 API 响应。
 */
@Getter
@Setter
@Table("permission_endpoints")
public class PermissionEndpoint {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String permissionCode;
    private String httpMethod;
    private String urlPath;
    private String handler;
}
