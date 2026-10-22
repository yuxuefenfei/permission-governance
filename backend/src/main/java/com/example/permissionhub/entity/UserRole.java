package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * user_roles 持久化映射，不作为公开 API 响应。
 */
@Getter
@Setter
@Table("user_roles")
public class UserRole {
    @Id(keyType = KeyType.None)
    private Long userId;
    @Id(keyType = KeyType.None)
    private Long roleId;
}
