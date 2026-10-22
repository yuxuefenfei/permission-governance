package com.example.permissionhub;

import com.example.permissionhub.config.PermissionProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** 权限治理平台后端入口。 */
@SpringBootApplication
@MapperScan("com.example.permissionhub.mapper")
@EnableConfigurationProperties(PermissionProperties.class)
public class PermissionHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(PermissionHubApplication.class, args);
    }
}
