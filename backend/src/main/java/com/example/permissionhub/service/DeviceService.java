package com.example.permissionhub.service;

import com.example.permissionhub.dto.CreateDeviceRequest;
import com.example.permissionhub.dto.Device;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存设备演示，状态由服务持有，重启后恢复初始样例。
 */
@Service
public class DeviceService {

    private final List<Device> devices = new CopyOnWriteArrayList<>(List.of(
            new Device("消防泵 A-01", "一号厂房"), new Device("烟感探测器 B-12", "二楼走廊")));

    public List<Device> list() {
        return List.copyOf(devices);
    }

    public void create(CreateDeviceRequest body) {
        devices.add(new Device(body.name(), body.location()));
    }
}
