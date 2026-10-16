package com.example.permissionhub;

import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.dto.*;
import com.example.permissionhub.enums.MenuType;
import com.example.permissionhub.exception.BusinessException;
import com.example.permissionhub.service.MenuService;
import com.example.permissionhub.service.PermissionRegistryService;
import com.example.permissionhub.service.RoleService;
import com.example.permissionhub.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.mybatisflex.core.row.Db;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 使用真实 H2 验证 Flex 主键回填、NULL 更新、关联表与查询语义。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PersistenceIntegrationTest {
    @Autowired private MenuService menus;
    @Autowired private RoleService roles;
    @Autowired private UserService users;
    @Autowired private PermissionRegistryService registry;
    @Autowired private UserDetailsService userDetails;
    @Autowired private PasswordEncoder encoder;

    @Test
    void menuCrudShouldClearParentAndReplaceBindings() {
        long parent = menus.create(new MenuCommand(null, "测试目录", "", "Folder", MenuType.DIRECTORY,
                10, true, List.of()));
        long child = menus.create(new MenuCommand(parent, "Flex 测试页面", "/flex-test", "Document", MenuType.PAGE,
                10, true, List.of(PermissionCodes.DEVICE_READ)));
        assertTrue(child > parent);
        menus.update(child, new MenuCommand(null, "Flex 测试页面", "/flex-test", "Document", MenuType.PAGE,
                20, true, List.of(PermissionCodes.DEVICE_CREATE)));
        MenuView view = menus.all().stream().filter(menu -> menu.id() == child).findFirst().orElseThrow();
        assertNull(view.parentId());
        assertEquals(List.of(PermissionCodes.DEVICE_CREATE), view.permissionCodes());
        assertEquals(20, view.sortOrder());
        menus.delete(child);
        menus.delete(parent);
        assertEquals(0L, ((Number) Db.selectObject("SELECT COUNT(*) FROM menu_permissions WHERE menu_id=?", child)).longValue());
    }

    @Test
    void roleAndUserCrudShouldPreserveGrantsPasswordsAndAdminProtection() {
        long roleId = roles.create(new RoleRequest("ROLE_FLEX_TEST", "Flex 测试角色", "测试"));
        roles.grant(roleId, new PermissionAssignment(List.of(PermissionCodes.DEVICE_READ)));
        long userId = users.create(new CreateUser("flex_test", "测试用户", "Flex@123456", List.of(roleId)));
        var account = userDetails.loadUserByUsername("flex_test");
        assertTrue(encoder.matches("Flex@123456", account.getPassword()));
        assertTrue(account.getAuthorities().stream().anyMatch(a -> PermissionCodes.DEVICE_READ.equals(a.getAuthority())));
        users.resetPassword(userId, new ResetPassword("Changed@123456"));
        assertTrue(encoder.matches("Changed@123456", userDetails.loadUserByUsername("flex_test").getPassword()));
        users.update(userId, new UpdateUser("停用测试用户", false, List.of(roleId)));
        assertFalse(userDetails.loadUserByUsername("flex_test").isEnabled());
        long adminId = ((Number) Db.selectObject("SELECT id FROM sys_users WHERE username='admin'")).longValue();
        assertThrows(BusinessException.class, () -> users.update(adminId, new UpdateUser("不可修改", false, List.of(roleId))));
        roles.update(roleId, new RoleRequest("ROLE_FLEX_RENAMED", "修改测试角色", null));
        users.update(userId, new UpdateUser("测试用户", true, List.of()));
        roles.delete(roleId);
        assertTrue(roles.all().stream().noneMatch(role -> role.id() == roleId));
        assertTrue(users.all().stream().filter(user -> user.id() == userId).findFirst().orElseThrow().roleIds().isEmpty());
    }

    @Test
    void permissionFiltersShouldRemainCaseInsensitiveAndRejectNullCodes() {
        var result = registry.list("FIRESAFETY", null, "ACTIVE");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(permission -> permission.getCode().contains("fireSafety")));
        assertThrows(BusinessException.class, () -> registry.verifyActiveCodes(Collections.singletonList(null)));
        assertThrows(BusinessException.class, () -> registry.verifyActiveCodes(List.of("")));
        assertThrows(UsernameNotFoundException.class, () -> userDetails.loadUserByUsername(""));
        assertThrows(UsernameNotFoundException.class, () -> userDetails.loadUserByUsername(null));
        assertThrows(BusinessException.class, () -> registry.get("missing:permission:code"));
    }

    @Test
    void reconciliationShouldKeepDeprecatedEndpointsAndGrants() {
        Db.updateBySql("""
                INSERT INTO permission_definitions(code,app_code,service_name,display_name,module_name,kind,source,status)
                VALUES('test:retired:read','sop','permission-governance','旧权限','迁移测试','PAGE','CODE','ACTIVE')
                """);
        Db.updateBySql("""
                INSERT INTO permission_endpoints(permission_code,http_method,url_path,handler)
                VALUES('test:retired:read','GET','/api/retired','OldController#list')
                """);
        long roleId = ((Number) Db.selectObject("SELECT id FROM sys_roles WHERE role_code='ROLE_OPERATOR'")).longValue();
        Db.updateBySql("INSERT INTO role_permissions(role_id,permission_code) VALUES(?,'test:retired:read')", roleId);
        var summary = registry.synchronize(true);
        assertEquals(1, summary.deprecated());
        assertEquals("DEPRECATED", registry.get("test:retired:read").getStatus().name());
        assertEquals(1, registry.get("test:retired:read").getEndpoints().size());
        assertEquals(1L, ((Number) Db.selectObject("SELECT COUNT(*) FROM role_permissions WHERE permission_code='test:retired:read'")).longValue());
    }
}
