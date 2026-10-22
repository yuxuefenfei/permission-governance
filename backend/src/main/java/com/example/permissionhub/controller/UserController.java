package com.example.permissionhub.controller;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.*;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import com.example.permissionhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    @RequirePermission(
            code = PermissionCodes.USER_MANAGE,
            name = "管理用户授权",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<List<UserView>> list() {
        return ApiResponse.ok(service.all());
    }

    @PostMapping
    @RequirePermission(
            code = PermissionCodes.USER_MANAGE,
            name = "管理用户授权",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Long> create(@Valid @RequestBody CreateUser body) {
        return ApiResponse.ok(service.create(body));
    }

    @PutMapping("/{id}")
    @RequirePermission(
            code = PermissionCodes.USER_MANAGE,
            name = "管理用户授权",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Void> update(@PathVariable long id, @Valid @RequestBody UpdateUser body) {
        service.update(id, body);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/password")
    @RequirePermission(
            code = PermissionCodes.USER_MANAGE,
            name = "管理用户授权",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Void> reset(@PathVariable long id, @Valid @RequestBody ResetPassword body) {
        service.resetPassword(id, body);
        return ApiResponse.ok();
    }
}
