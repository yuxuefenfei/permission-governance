package com.example.permissionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateUser(@NotBlank @Size(max = 100) String displayName,
                         @NotNull Boolean enabled, @NotNull List<Long> roleIds) {
}
