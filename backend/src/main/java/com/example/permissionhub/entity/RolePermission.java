package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/** role_permissions 持久化映射，不作为公开 API 响应。 */
@Getter
@Setter
@Table("role_permissions")
public class RolePermission {
    @Id(keyType = KeyType.None)
    private Long roleId;
    @Id(keyType = KeyType.None)
    private String permissionCode;
}
