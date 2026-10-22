package com.example.permissionhub.service;

import com.example.permissionhub.dto.CreateUser;
import com.example.permissionhub.dto.ResetPassword;
import com.example.permissionhub.dto.UpdateUser;
import com.example.permissionhub.dto.UserView;
import com.example.permissionhub.exception.BusinessException;
import com.example.permissionhub.repository.RoleRepository;
import com.example.permissionhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/**
 * 用户与角色关系维护，demo 超级管理员账号不可在后台禁用或重置。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public List<UserView> all() {
        return repo.all();
    }

    @Transactional
    public long create(CreateUser body) {
        verifyRoles(body.roleIds());
        long id = repo.insert(body, encoder.encode(body.password()));
        repo.replaceRoles(id, body.roleIds());
        audit.record("CREATE", "USER", String.valueOf(id), body.username());
        return id;
    }

    @Transactional
    public void update(long id, UpdateUser body) {
        verifyRoles(body.roleIds());
        if (repo.update(id, body) == 0) throw new BusinessException(404, "用户不存在或超级管理员不可编辑");
        repo.replaceRoles(id, body.roleIds());
        audit.record("UPDATE", "USER", String.valueOf(id), "修改用户及角色");
    }

    @Transactional
    public void resetPassword(long id, ResetPassword body) {
        if (repo.resetPassword(id, encoder.encode(body.newPassword())) == 0)
            throw new BusinessException(404, "用户不存在或超级管理员不可重置");
        audit.record("RESET_PASSWORD", "USER", String.valueOf(id), "重置密码（不记录密码内容）");
    }

    private void verifyRoles(List<Long> ids) {
        if (ids.size() != new HashSet<>(ids).size()) throw new BusinessException(400, "角色不可重复");
        for (Long id : ids) {
            if (id == null || !roles.exists(id))
                throw new BusinessException(400, "角色不存在：" + id);
        }
    }
}
