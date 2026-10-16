package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/** sys_menus 持久化映射，不作为公开 API 响应。 */
@Getter
@Setter
@Table("sys_menus")
public class SysMenu {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long parentId;
    private String displayName;
    private String routePath;
    private String icon;
    private String menuType;
    private Integer sortOrder;
    private Boolean enabled;
}
