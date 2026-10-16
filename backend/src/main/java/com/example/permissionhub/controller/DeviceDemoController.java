package com.example.permissionhub.controller;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.ApiResponse;
import com.example.permissionhub.dto.CreateDeviceRequest;
import com.example.permissionhub.dto.Device;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import com.example.permissionhub.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 业务端示例：查看与新增分别通过权限注解鉴权。 */
@RestController
@RequestMapping("/api/demo/devices")
@RequiredArgsConstructor
public class DeviceDemoController {
    private final DeviceService service;

    @GetMapping
    @RequirePermission(code=PermissionCodes.DEVICE_READ,name="查看消防设备",module="消防安全",kind=PermissionKind.PAGE,
            description="消防设备页面访问与列表查询")
    public ApiResponse<List<Device>> list() {
        return ApiResponse.ok(service.list());
    }

    @PostMapping
    @RequirePermission(code=PermissionCodes.DEVICE_CREATE,name="新增消防设备",module="消防安全",kind=PermissionKind.ACTION)
    public ApiResponse<Void> create(@Valid @RequestBody CreateDeviceRequest body) {
        service.create(body);
        return ApiResponse.ok();
    }
}
