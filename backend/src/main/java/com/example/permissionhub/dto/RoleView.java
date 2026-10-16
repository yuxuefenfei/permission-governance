package com.example.permissionhub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record RoleView(long id,String roleCode,String displayName,String description,
                       boolean builtIn,List<String> permissionCodes) { }
