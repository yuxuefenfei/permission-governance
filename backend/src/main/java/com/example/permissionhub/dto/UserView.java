package com.example.permissionhub.dto;

import java.util.List;

public record UserView(long id, String username, String displayName, boolean enabled,
                       boolean superAdmin, List<Long> roleIds) {
}
