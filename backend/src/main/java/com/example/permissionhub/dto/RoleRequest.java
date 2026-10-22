package com.example.permissionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,63}$", message = "须为大写字母、数字和下划线，长度 3-64")
        String roleCode, @NotBlank @Size(max = 100) String displayName,
        @Size(max = 255) String description) {
}
