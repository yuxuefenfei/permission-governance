package com.example.permissionhub.repository;

import com.example.permissionhub.dto.RoleRequest;
import com.example.permissionhub.dto.RoleView;
import com.example.permissionhub.entity.RolePermission;
import com.example.permissionhub.entity.SysRole;
import com.example.permissionhub.mapper.RolePermissionMapper;
import com.example.permissionhub.mapper.SysRoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import static com.example.permissionhub.entity.table.RolePermissionTableDef.ROLE_PERMISSION;
import static com.example.permissionhub.entity.table.SysRoleTableDef.SYS_ROLE;

/** 角色持久化，角色权限独立于菜单绑定。 */
@Repository
@RequiredArgsConstructor
public class RoleRepository {
    private final SysRoleMapper roles;
    private final RolePermissionMapper bindings;

    public List<RoleView> all() {
        Map<Long, List<String>> grouped = new HashMap<>();
        bindings.selectListByQuery(QueryWrapper.create().orderBy(ROLE_PERMISSION.PERMISSION_CODE.asc()))
                .forEach(binding -> grouped.computeIfAbsent(binding.getRoleId(), ignored -> new ArrayList<>())
                        .add(binding.getPermissionCode()));
        return roles.selectListByQuery(QueryWrapper.create().orderBy(SYS_ROLE.ID.asc())).stream()
                .map(role -> new RoleView(role.getId(), role.getRoleCode(), role.getDisplayName(),
                        role.getDescription(), role.getBuiltIn(), grouped.getOrDefault(role.getId(), List.of()))).toList();
    }

    public boolean exists(long id) {
        return roles.selectCountByCondition(SYS_ROLE.ID.eq(id)) > 0;
    }

    public List<String> codes(long roleId) {
        return bindings.selectObjectListByQueryAs(QueryWrapper.create().select(ROLE_PERMISSION.PERMISSION_CODE)
                .where(ROLE_PERMISSION.ROLE_ID.eq(roleId)).orderBy(ROLE_PERMISSION.PERMISSION_CODE.asc()), String.class);
    }

    public void replaceCodes(long id, List<String> codes) {
        bindings.deleteByCondition(ROLE_PERMISSION.ROLE_ID.eq(id));
        for (String code : codes) {
            RolePermission binding = new RolePermission();
            binding.setRoleId(id);
            binding.setPermissionCode(code);
            bindings.insertSelectiveWithPk(binding);
        }
    }

    public long insert(RoleRequest body) {
        SysRole role = from(body);
        role.setBuiltIn(false);
        roles.insertSelective(role);
        return role.getId();
    }

    public int update(long id, RoleRequest body) {
        return roles.updateByCondition(from(body), SYS_ROLE.ID.eq(id).and(SYS_ROLE.BUILT_IN.eq(false)));
    }

    public int delete(long id) {
        return roles.deleteByCondition(SYS_ROLE.ID.eq(id).and(SYS_ROLE.BUILT_IN.eq(false)));
    }

    private SysRole from(RoleRequest body) {
        SysRole role = new SysRole();
        role.setRoleCode(body.roleCode());
        role.setDisplayName(body.displayName());
        role.setDescription(body.description() == null ? "" : body.description());
        return role;
    }
}
