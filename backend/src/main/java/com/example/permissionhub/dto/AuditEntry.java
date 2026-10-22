package com.example.permissionhub.dto;

import java.time.LocalDateTime;

/**
 * 审计查询投影，不包含密码或令牌。
 */
public record AuditEntry(long id, String actor, String action, String resourceType,
                         String resourceId, String detail, LocalDateTime createdAt) {
}
