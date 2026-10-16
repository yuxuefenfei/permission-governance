package com.example.permissionhub.repository;

import com.example.permissionhub.dto.CurrentUser;
import com.example.permissionhub.entity.SysUser;
import com.example.permissionhub.mapper.SysUserMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import static com.example.permissionhub.entity.table.SysUserTableDef.SYS_USER;
import static com.example.permissionhub.entity.table.UserRoleTableDef.USER_ROLE;
import static com.example.permissionhub.entity.table.RolePermissionTableDef.ROLE_PERMISSION;
import static com.example.permissionhub.entity.table.PermissionDefinitionTableDef.PERMISSION_DEFINITION;
import static com.mybatisflex.core.query.QueryMethods.distinct;

/** 认证与实时授权查询，凭据实体只在内部使用。 */
@Repository
@RequiredArgsConstructor
public class SecurityRepository {
    private final SysUserMapper users;

    public Optional<SysUser> findAccount(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        return Optional.ofNullable(users.selectOneByCondition(SYS_USER.USERNAME.eq(username)));
    }

    public CurrentUser currentUser(String username) {
        if (username == null || username.isBlank()) throw new UsernameNotFoundException("用户名或密码错误");
        SysUser user = users.selectOneByQuery(QueryWrapper.create()
                .select(SYS_USER.USERNAME, SYS_USER.DISPLAY_NAME, SYS_USER.SUPER_ADMIN)
                .where(SYS_USER.USERNAME.eq(username)));
        if (user == null) throw new UsernameNotFoundException("用户名或密码错误");
        return new CurrentUser(user.getUsername(), user.getDisplayName(), user.getSuperAdmin());
    }

    public Boolean enabledSuperAdmin(String username) {
        if (username == null || username.isBlank()) return null;
        SysUser user = users.selectOneByQuery(QueryWrapper.create().select(SYS_USER.SUPER_ADMIN)
                .where(SYS_USER.USERNAME.eq(username)).and(SYS_USER.ENABLED.eq(true)));
        return user == null ? null : user.getSuperAdmin();
    }

    public List<String> activeCodes(String username) {
        if (username == null || username.isBlank()) return List.of();
        return users.selectObjectListByQueryAs(grantsQuery(username)
                .select(distinct(ROLE_PERMISSION.PERMISSION_CODE)).orderBy(ROLE_PERMISSION.PERMISSION_CODE.asc()),
                String.class);
    }

    public boolean hasActivePermission(String username, String code) {
        return username != null && !username.isBlank() && code != null && !code.isBlank()
                && users.selectCountByQuery(grantsQuery(username)
                .and(PERMISSION_DEFINITION.CODE.eq(code))) > 0;
    }

    private QueryWrapper grantsQuery(String username) {
        return QueryWrapper.create().from(SYS_USER)
                .innerJoin(USER_ROLE).on(USER_ROLE.USER_ID.eq(SYS_USER.ID))
                .innerJoin(ROLE_PERMISSION).on(ROLE_PERMISSION.ROLE_ID.eq(USER_ROLE.ROLE_ID))
                .innerJoin(PERMISSION_DEFINITION).on(PERMISSION_DEFINITION.CODE.eq(ROLE_PERMISSION.PERMISSION_CODE))
                .where(SYS_USER.USERNAME.eq(username)).and(SYS_USER.ENABLED.eq(true))
                .and(PERMISSION_DEFINITION.STATUS.eq("ACTIVE"));
    }
}
