package com.example.permissionhub.repository;

import com.example.permissionhub.dto.DashboardOverview;
import com.example.permissionhub.mapper.PermissionDefinitionMapper;
import com.example.permissionhub.mapper.SysMenuMapper;
import com.example.permissionhub.mapper.SysRoleMapper;
import com.example.permissionhub.mapper.SysUserMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.example.permissionhub.entity.table.PermissionDefinitionTableDef.PERMISSION_DEFINITION;

/**
 * 使用 Mapper 的数据库 COUNT 查询，避免加载列表后统计。
 */
@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final PermissionDefinitionMapper permissions;
    private final SysRoleMapper roles;
    private final SysUserMapper users;
    private final SysMenuMapper menus;

    public DashboardOverview overview() {
        return new DashboardOverview(Math.toIntExact(permissions.selectCountByQuery(QueryWrapper.create())),
                Math.toIntExact(permissions.selectCountByCondition(PERMISSION_DEFINITION.STATUS.eq("DEPRECATED"))),
                Math.toIntExact(roles.selectCountByQuery(QueryWrapper.create())),
                Math.toIntExact(users.selectCountByQuery(QueryWrapper.create())),
                Math.toIntExact(menus.selectCountByQuery(QueryWrapper.create())));
    }
}
