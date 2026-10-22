package com.example.permissionhub.service;

import com.example.permissionhub.constant.SecurityConstants;
import com.example.permissionhub.dto.CurrentUser;
import com.example.permissionhub.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 当前用户与实时权限查询。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SecurityRepository repository;

    public CurrentUser currentUser(String username) {
        return repository.currentUser(username);
    }

    public List<String> grants(String username) {
        if (currentUser(username).superAdmin()) return List.of(SecurityConstants.LEGACY_SUPER_AUTHORITY);
        return repository.activeCodes(username);
    }
}
