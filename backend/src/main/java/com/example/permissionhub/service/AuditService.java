package com.example.permissionhub.service;

import com.example.permissionhub.dto.AuditEntry;
import com.example.permissionhub.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/** 将敏感业务变更写入数据库；密码、令牌等敏感值不得写入 detail。 */
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository repository;

    public void record(String action, String resourceType, String resourceId, String detail) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String actor = auth == null || !auth.isAuthenticated() ? "SYSTEM" : auth.getName();
        repository.insert(actor, action, resourceType, resourceId, detail);
    }

    public List<AuditEntry> recent() {
        return repository.recent();
    }
}
