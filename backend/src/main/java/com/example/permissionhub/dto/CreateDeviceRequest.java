package com.example.permissionhub.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceRequest(@NotBlank String name, @NotBlank String location) {
}
