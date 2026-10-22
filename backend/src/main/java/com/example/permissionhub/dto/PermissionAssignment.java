package com.example.permissionhub.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PermissionAssignment(@NotNull List<String> permissionCodes) {
}
