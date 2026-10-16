package com.example.permissionhub.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import com.mybatisflex.annotation.Column;
import java.time.LocalDateTime;

/** permission_definitions 持久化映射，不作为公开 API 响应。 */
@Getter
@Setter
@Table("permission_definitions")
public class PermissionDefinition {
    @Id(keyType = KeyType.None)
    private String code;
    private String appCode;
    private String serviceName;
    private String displayName;
    private String moduleName;
    private String kind;
    private String description;
    private String source;
    private String status;
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime firstSeenAt;
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime lastSeenAt;
}
