package com.example.permissionhub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record CreateUser(@NotBlank @Pattern(regexp="^[a-zA-Z][a-zA-Z0-9_]{2,31}$") String username,
                         @NotBlank @Size(max=100) String displayName,
                         @NotBlank @Size(min=8,max=72) String password,
                         @NotNull List<Long> roleIds) { }
