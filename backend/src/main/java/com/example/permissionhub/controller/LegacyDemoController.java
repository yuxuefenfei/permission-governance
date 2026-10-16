package com.example.permissionhub.controller;

import com.example.permissionhub.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 迁移期的旧授权写法展示：原样保留，不强行替换现有项目的权限模型。
 * 注意：由于它没有业务元数据，默认不会进入新注解扫描器；可先用 inventory 脚本盘点。
 */
@RestController
public class LegacyDemoController {
    @GetMapping("/api/demo/legacy-permission-check")
    @PreAuthorize("hasAnyAuthority('sop:fireSafety:deviceInfo', '*:*:*')")
    public ApiResponse<String> check() {
        return ApiResponse.ok("已通过旧 @PreAuthorize 授权检查");
    }
}
