package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import com.mybatisflex.annotation.Column;
import java.time.LocalDateTime;

/** audit_logs 持久化映射，不作为公开 API 响应。 */
@Getter
@Setter
@Table("audit_logs")
public class AuditLog {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String actor;
    private String action;
    private String resourceType;
    private String resourceId;
    private String detail;
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
