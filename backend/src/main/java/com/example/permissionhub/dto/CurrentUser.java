package com.example.permissionhub.dto;

import jakarta.validation.constraints.NotBlank;

public record CurrentUser(String username, String displayName, boolean superAdmin) { }
