package com.example.permissionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPassword(@NotBlank @Size(min = 8, max = 72) String newPassword) {
}
