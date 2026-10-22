package com.example.permissionhub.controller;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.ApiResponse;
import com.example.permissionhub.dto.MenuCommand;
import com.example.permissionhub.dto.MenuView;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import com.example.permissionhub.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理 + 当前用户可访问的动态导航。
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService service;

    @GetMapping("/my")
    public ApiResponse<List<MenuView>> mine() {
        return ApiResponse.ok(service.mine());
    }

    @GetMapping
    @RequirePermission(
            code = PermissionCodes.MENU_MANAGE,
            name = "维护菜单配置",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<List<MenuView>> list() {
        return ApiResponse.ok(service.all());
    }

    @PostMapping
    @RequirePermission(
            code = PermissionCodes.MENU_MANAGE,
            name = "维护菜单配置",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Long> create(@Valid @RequestBody MenuCommand body) {
        return ApiResponse.ok(service.create(body));
    }

    @PutMapping("/{id}")
    @RequirePermission(
            code = PermissionCodes.MENU_MANAGE,
            name = "维护菜单配置",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Void> update(@PathVariable long id, @Valid @RequestBody MenuCommand body) {
        service.update(id, body);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission(
            code = PermissionCodes.MENU_MANAGE,
            name = "维护菜单配置",
            module = "权限治理",
            kind = PermissionKind.ADMIN
    )
    public ApiResponse<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
