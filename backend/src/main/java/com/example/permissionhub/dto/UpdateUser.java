package com.example.permissionhub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record UpdateUser(@NotBlank @Size(max=100) String displayName,
                         @NotNull Boolean enabled, @NotNull List<Long> roleIds) { }
