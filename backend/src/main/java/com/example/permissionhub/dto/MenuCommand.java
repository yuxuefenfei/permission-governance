package com.example.permissionhub.dto;

import com.example.permissionhub.enums.MenuType;
import jakarta.validation.constraints.*;
import java.util.List;

/** 菜单写入命令；目录与页面具有不同的字段规则，由应用服务校验。 */
public record MenuCommand(Long parentId, @NotBlank @Size(max=100) String displayName,
                          String routePath, String icon, @NotNull MenuType menuType,
                          @NotNull Integer sortOrder, @NotNull Boolean enabled,
                          @NotNull List<String> permissionCodes) { }
