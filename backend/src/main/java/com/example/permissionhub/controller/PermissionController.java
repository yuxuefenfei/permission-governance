package com.example.permissionhub.controller;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.ApiResponse;
import com.example.permissionhub.dto.PermissionView;
import com.example.permissionhub.dto.SyncSummary;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import com.example.permissionhub.service.PermissionRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限池只读；不可让管理端直接编辑代码扫描出的权限定义。
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionRegistryService service;

    @GetMapping
    @RequirePermission(
            code = PermissionCodes.PERMISSION_READ,
            name = "查看权限资源",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<List<PermissionView>> list(@RequestParam(defaultValue = "") String keyword,
                                                  @RequestParam(defaultValue = "") String module,
                                                  @RequestParam(defaultValue = "") String status) {
        return ApiResponse.ok(service.list(keyword, module, status));
    }

    @PostMapping("/sync")
    @RequirePermission(
            code = PermissionCodes.PERMISSION_SYNC,
            name = "同步权限定义",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<SyncSummary> sync() {
        return ApiResponse.ok(service.synchronize(true));
    }
}
