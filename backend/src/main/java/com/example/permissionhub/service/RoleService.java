package com.example.permissionhub.service;

import com.example.permissionhub.dto.PermissionAssignment;
import com.example.permissionhub.dto.RoleRequest;
import com.example.permissionhub.dto.RoleView;
import com.example.permissionhub.exception.BusinessException;
import com.example.permissionhub.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 内置角色不允许修改基础信息或删除，但仍可维护其授权集合。
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repo;
    private final PermissionRegistryService registry;
    private final AuditService audit;

    public List<RoleView> all() {
        return repo.all();
    }

    @Transactional
    public long create(RoleRequest body) {
        long id = repo.insert(body);
        audit.record("CREATE", "ROLE", String.valueOf(id), body.roleCode());
        return id;
    }

    @Transactional
    public void update(long id, RoleRequest body) {
        if (repo.update(id, body) == 0) throw new BusinessException(404, "角色不存在或属于内置角色");
        audit.record("UPDATE", "ROLE", String.valueOf(id), body.roleCode());
    }

    @Transactional
    public void grant(long id, PermissionAssignment body) {
        if (repo.all().stream().noneMatch(r -> r.id() == id)) throw new BusinessException(404, "角色不存在");
        registry.verifyActiveCodes(body.permissionCodes());
        repo.replaceCodes(id, body.permissionCodes());
        audit.record("GRANT", "ROLE", String.valueOf(id), "设置权限数量=" + body.permissionCodes().size());
    }

    @Transactional
    public void delete(long id) {
        if (repo.delete(id) == 0) throw new BusinessException(404, "角色不存在或属于内置角色");
        audit.record("DELETE", "ROLE", String.valueOf(id), "删除角色");
    }
}
