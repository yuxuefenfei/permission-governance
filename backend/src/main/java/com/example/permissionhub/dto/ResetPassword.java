package com.example.permissionhub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record ResetPassword(@NotBlank @Size(min=8,max=72) String newPassword) { }
