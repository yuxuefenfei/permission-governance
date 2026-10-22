package com.example.permissionhub.security;

import com.example.permissionhub.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 实时从数据库读取授权；角色权限变更对新注解保护的接口立即生效。
 * 旧版 hasAnyAuthority 仍是登录时 authority 快照，变更后需重新登录。
 */
@Component("permissionChecker")
@RequiredArgsConstructor
public class PermissionChecker {

    private final SecurityRepository repository;

    public boolean hasPermission(String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) return false;
        String username = authentication.getName();
        Boolean superAdmin = repository.enabledSuperAdmin(username);
        if (superAdmin == null) return false;
        return superAdmin || repository.hasActivePermission(username, code);
    }
}
