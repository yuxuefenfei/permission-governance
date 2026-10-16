package com.example.permissionhub.repository;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.MenuCommand;
import com.example.permissionhub.entity.SysRole;
import com.example.permissionhub.entity.SysUser;
import com.example.permissionhub.entity.UserRole;
import com.example.permissionhub.enums.MenuType;
import com.example.permissionhub.mapper.SysRoleMapper;
import com.example.permissionhub.mapper.SysMenuMapper;
import com.example.permissionhub.mapper.SysUserMapper;
import com.example.permissionhub.mapper.UserRoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import static com.example.permissionhub.entity.table.SysRoleTableDef.SYS_ROLE;

/** 本地演示种子数据，使用 Mapper 回填主键，仅在空表初始化。 */
@Repository
@RequiredArgsConstructor
public class DemoDataRepository {
    private final SysRoleMapper roles;
    private final SysUserMapper users;
    private final UserRoleMapper userRoles;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final SysMenuMapper menus;

    public void seedRolesAndMenus() {
        if (roles.selectCountByQuery(QueryWrapper.create()) == 0) {
            long operator = role("ROLE_OPERATOR", "消防设备查看员", "仅查看消防设备");
            long maintainer = role("ROLE_MAINTAINER", "消防设备维护员", "查看与新增消防设备");
            roleRepository.replaceCodes(operator, List.of(PermissionCodes.DEVICE_READ));
            roleRepository.replaceCodes(maintainer, List.of(PermissionCodes.DEVICE_READ, PermissionCodes.DEVICE_CREATE));
        }
        if (menus.selectCountByQuery(QueryWrapper.create()) == 0) {
            long safety = directory(null, "消防安全", "Monitor", 10);
            page(safety, "消防设备", "/devices", "FireExtinguisher", 10, PermissionCodes.DEVICE_READ);
            long admin = directory(null, "权限治理", "Setting", 20);
            page(admin, "权限资源", "/permissions", "Key", 10, PermissionCodes.PERMISSION_READ);
            page(admin, "菜单配置", "/menus", "Menu", 20, PermissionCodes.MENU_MANAGE);
            page(admin, "角色授权", "/roles", "UserRoundCheck", 30, PermissionCodes.ROLE_MANAGE);
            page(admin, "用户管理", "/users", "Users", 40, PermissionCodes.USER_MANAGE);
            page(admin, "审计日志", "/audits", "ScrollText", 50, PermissionCodes.AUDIT_READ);
            page(null, "工作台", "/dashboard", "LayoutDashboard", 0, PermissionCodes.DASHBOARD_READ);
        }
    }

    public boolean hasUsers() {
        return users.selectCountByQuery(QueryWrapper.create()) > 0;
    }

    public void seedUsers(String adminPasswordHash, String operatorPasswordHash) {
        user("admin", "平台管理员", adminPasswordHash, true);
        long operator = user("operator", "设备查看员", operatorPasswordHash, false);
        SysRole role = roles.selectOneByCondition(SYS_ROLE.ROLE_CODE.eq("ROLE_OPERATOR"));
        UserRole binding = new UserRole();
        binding.setUserId(operator);
        binding.setRoleId(role.getId());
        userRoles.insertSelectiveWithPk(binding);
    }

    private long role(String code, String name, String description) {
        SysRole role = new SysRole();
        role.setRoleCode(code);
        role.setDisplayName(name);
        role.setDescription(description);
        role.setBuiltIn(true);
        roles.insertSelective(role);
        return role.getId();
    }

    private long user(String username, String name, String passwordHash, boolean superAdmin) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setDisplayName(name);
        user.setPasswordHash(passwordHash);
        user.setEnabled(true);
        user.setSuperAdmin(superAdmin);
        users.insertSelective(user);
        return user.getId();
    }

    private long directory(Long parent, String name, String icon, int order) {
        return menuRepository.insert(new MenuCommand(parent, name, "", icon, MenuType.DIRECTORY, order, true, List.of()));
    }

    private void page(Long parent, String name, String route, String icon, int order, String code) {
        long id = menuRepository.insert(new MenuCommand(parent, name, route, icon, MenuType.PAGE, order, true, List.of(code)));
        menuRepository.bindCodes(id, List.of(code));
    }
}
