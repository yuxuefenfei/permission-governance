package com.example.permissionhub.service;

import com.example.permissionhub.constant.SecurityConstants;
import com.example.permissionhub.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/** 登录时装载传统 Authority 快照，迁移旧 @PreAuthorize 不受影响。 */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    private final SecurityRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var account = repository.findAccount(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户名或密码错误"));
        var codes = new ArrayList<>(repository.activeCodes(username));
        if (account.getSuperAdmin()) codes.add(SecurityConstants.LEGACY_SUPER_AUTHORITY);
        return new User(account.getUsername(), account.getPasswordHash(), account.getEnabled(), true, true, true,
                codes.stream().map(SimpleGrantedAuthority::new).toList());
    }
}
