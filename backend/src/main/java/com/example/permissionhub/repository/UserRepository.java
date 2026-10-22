package com.example.permissionhub.repository;

import com.example.permissionhub.dto.CreateUser;
import com.example.permissionhub.dto.UpdateUser;
import com.example.permissionhub.dto.UserView;
import com.example.permissionhub.entity.SysUser;
import com.example.permissionhub.entity.UserRole;
import com.example.permissionhub.mapper.SysUserMapper;
import com.example.permissionhub.mapper.UserRoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permissionhub.entity.table.SysUserTableDef.SYS_USER;
import static com.example.permissionhub.entity.table.UserRoleTableDef.USER_ROLE;

/**
 * 用户持久化；查询列表不加载密码散列。
 */
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SysUserMapper users;
    private final UserRoleMapper bindings;

    public List<UserView> all() {
        Map<Long, List<Long>> grouped = new HashMap<>();
        bindings.selectListByQuery(QueryWrapper.create().orderBy(USER_ROLE.ROLE_ID.asc()))
                .forEach(binding -> grouped.computeIfAbsent(binding.getUserId(), ignored -> new ArrayList<>())
                        .add(binding.getRoleId()));
        return users.selectListByQuery(QueryWrapper.create().select(SYS_USER.ID, SYS_USER.USERNAME,
                        SYS_USER.DISPLAY_NAME, SYS_USER.ENABLED, SYS_USER.SUPER_ADMIN).orderBy(SYS_USER.ID.asc()))
                .stream().map(user -> new UserView(user.getId(), user.getUsername(), user.getDisplayName(),
                        user.getEnabled(), user.getSuperAdmin(), grouped.getOrDefault(user.getId(), List.of()))).toList();
    }

    public List<Long> roleIds(long userId) {
        return bindings.selectObjectListByQueryAs(QueryWrapper.create().select(USER_ROLE.ROLE_ID)
                .where(USER_ROLE.USER_ID.eq(userId)).orderBy(USER_ROLE.ROLE_ID.asc()), Long.class);
    }

    public long insert(CreateUser body, String hashedPassword) {
        SysUser user = new SysUser();
        user.setUsername(body.username());
        user.setDisplayName(body.displayName());
        user.setPasswordHash(hashedPassword);
        user.setEnabled(true);
        user.setSuperAdmin(false);
        users.insertSelective(user);
        return user.getId();
    }

    public int update(long id, UpdateUser body) {
        SysUser user = new SysUser();
        user.setDisplayName(body.displayName());
        user.setEnabled(body.enabled());
        return users.updateByCondition(user, SYS_USER.ID.eq(id).and(SYS_USER.SUPER_ADMIN.eq(false)));
    }

    public int resetPassword(long id, String passwordHash) {
        SysUser user = new SysUser();
        user.setPasswordHash(passwordHash);
        return users.updateByCondition(user, SYS_USER.ID.eq(id).and(SYS_USER.SUPER_ADMIN.eq(false)));
    }

    public void replaceRoles(long userId, List<Long> roleIds) {
        bindings.deleteByCondition(USER_ROLE.USER_ID.eq(userId));
        for (Long roleId : roleIds) {
            UserRole binding = new UserRole();
            binding.setUserId(userId);
            binding.setRoleId(roleId);
            bindings.insertSelectiveWithPk(binding);
        }
    }
}
