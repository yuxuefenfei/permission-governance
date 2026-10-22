package com.example.permissionhub.dto;

import java.util.List;

public record RoleView(long id, String roleCode, String displayName, String description,
                       boolean builtIn, List<String> permissionCodes) {
}
