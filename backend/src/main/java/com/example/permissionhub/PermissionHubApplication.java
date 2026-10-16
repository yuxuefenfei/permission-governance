package com.example.permissionhub;

import com.example.permissionhub.config.PermissionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 权限治理平台后端入口。 */
@SpringBootApplication
@MapperScan("com.example.permissionhub.mapper")
@EnableConfigurationProperties(PermissionProperties.class)
public class PermissionHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(PermissionHubApplication.class, args);
    }
}
