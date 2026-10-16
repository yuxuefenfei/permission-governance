package com.example.permissionhub.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** 权限注册配置，默认值集中在 application.yml。 */
@Validated
@ConfigurationProperties(prefix = "app.permission")
public record PermissionProperties(@NotBlank String applicationCode, @NotBlank String serviceName) { }
