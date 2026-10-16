package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/** sys_roles 持久化映射，不作为公开 API 响应。 */
@Getter
@Setter
@Table("sys_roles")
public class SysRole {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String roleCode;
    private String displayName;
    private String description;
    private Boolean builtIn;
}
