package com.example.permissionhub.dto;

import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.enums.PermissionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/** 权限资源池只读模型，端点是一对多关系。 */
@Getter
@Builder
public class PermissionView {
    private String code;
    private String displayName;
    private String moduleName;
    private PermissionKind kind;
    private PermissionStatus status;
    private String description;
    private String serviceName;
    private LocalDateTime lastSeenAt;
    @Setter
    private List<EndpointView> endpoints;

}
