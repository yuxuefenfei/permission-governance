package com.example.permissionhub.controller;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.ApiResponse;
import com.example.permissionhub.dto.DashboardOverview;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import com.example.permissionhub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 管理台概览真实读取数据库，不使用前端硬编码假指标。 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;

    @GetMapping
    @RequirePermission(code=PermissionCodes.DASHBOARD_READ,name="查看工作台",module="系统首页",kind=PermissionKind.PAGE)
    public ApiResponse<DashboardOverview> overview() {
        return ApiResponse.ok(service.overview());
    }
}
