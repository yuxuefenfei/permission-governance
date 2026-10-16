package com.example.permissionhub.dto;

import com.example.permissionhub.enums.MenuType;
import java.util.List;

/** 菜单存储为邻接表，接口返回扁平列表，前端按 parentId 建树。 */
public record MenuView(long id, Long parentId, String displayName, String routePath,
                       String icon, MenuType menuType, int sortOrder, boolean enabled,
                       List<String> permissionCodes) { }
