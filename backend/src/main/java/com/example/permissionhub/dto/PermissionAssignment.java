package com.example.permissionhub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record PermissionAssignment(@NotNull List<String> permissionCodes) { }
