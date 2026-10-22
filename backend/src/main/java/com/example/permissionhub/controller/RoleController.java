package com.example.permissionhub.controller;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.ApiResponse;
import com.example.permissionhub.dto.PermissionAssignment;
import com.example.permissionhub.dto.RoleRequest;
import com.example.permissionhub.dto.RoleView;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import com.example.permissionhub.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping
    @RequirePermission(code =
            PermissionCodes.ROLE_MANAGE,
            name = "管理角色权限",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<List<RoleView>> list() {
        return ApiResponse.ok(service.all());
    }

    @PostMapping
    @RequirePermission(
            code = PermissionCodes.ROLE_MANAGE,
            name = "管理角色权限",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Long> create(@Valid @RequestBody RoleRequest body) {
        return ApiResponse.ok(service.create(body));
    }

    @PutMapping("/{id}")
    @RequirePermission(
            code = PermissionCodes.ROLE_MANAGE,
            name = "管理角色权限",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Void> update(@PathVariable long id, @Valid @RequestBody RoleRequest body) {
        service.update(id, body);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/permissions")
    @RequirePermission(
            code = PermissionCodes.ROLE_MANAGE,
            name = "管理角色权限",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Void> grant(@PathVariable long id, @Valid @RequestBody PermissionAssignment body) {
        service.grant(id, body);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission(
            code = PermissionCodes.ROLE_MANAGE,
            name = "管理角色权限",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
