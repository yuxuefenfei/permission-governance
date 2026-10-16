package com.example.permissionhub.controller;

import com.example.permissionhub.service.AuditService;
import com.example.permissionhub.dto.AuditEntry;
import com.example.permissionhub.dto.ApiResponse;
import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 审计列表保留最近 200 条；演示场景足够，生产建议分页和归档。 */
@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService audit;

    @GetMapping
    @RequirePermission(code=PermissionCodes.AUDIT_READ,name="查看操作审计",module="权限治理",kind=PermissionKind.ADMIN)
    public ApiResponse<List<AuditEntry>> list() { return ApiResponse.ok(audit.recent()); }
}
