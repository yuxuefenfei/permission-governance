package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/** sys_users 持久化映射，不作为公开 API 响应。 */
@Getter
@Setter
@Table("sys_users")
public class SysUser {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;
    private Boolean enabled;
    private Boolean superAdmin;
}
