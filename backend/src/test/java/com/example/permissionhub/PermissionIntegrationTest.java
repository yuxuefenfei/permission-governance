package com.example.permissionhub;

import com.example.permissionhub.exception.BusinessException;
import com.example.permissionhub.constant.PermissionCodes;
import com.example.permissionhub.service.MenuService;
import com.example.permissionhub.dto.MenuCommand;
import com.example.permissionhub.enums.MenuType;
import com.example.permissionhub.service.PermissionRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import com.mybatisflex.core.row.Db;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 关键闭环测试：扫描幂等、真实权限判断、前端菜单规则、非法父级。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private PermissionRegistryService registry;
    @Autowired private MenuService menus;

    @Test
    void scannerShouldDiscoverCodesAndActualUrls() {
        var result = registry.synchronize(false);
        assertTrue(result.discovered() >= 9);
        var device = registry.get(PermissionCodes.DEVICE_READ);
        assertTrue(device.getEndpoints().stream().anyMatch(e ->
                e.httpMethod().equals("GET") && e.urlPath().equals("/api/demo/devices")));
    }

    @Test
    void permissionListShouldIncludeAllEndpoints() {
        var permissions = registry.list(null, null, null);
        assertTrue(permissions.size() >= 9);
        for (var permission : permissions) {
            assertEquals(registry.get(permission.getCode()).getEndpoints(), permission.getEndpoints());
        }
        long expected = ((Number) Db.selectObject("SELECT COUNT(*) FROM permission_endpoints")).longValue();
        assertEquals(expected, permissions.stream().mapToLong(p -> p.getEndpoints().size()).sum());
    }

    @Test
    void repeatedSyncShouldNotClearRoleGrants() {
        long before = ((Number) Db.selectObject("SELECT COUNT(*) FROM role_permissions")).longValue();
        registry.synchronize(false);
        registry.synchronize(false);
        long after = ((Number) Db.selectObject("SELECT COUNT(*) FROM role_permissions")).longValue();
        assertEquals(before,after);
    }

    @Test
    void operatorCanReadButCannotCreateDevice() throws Exception {
        mvc.perform(get("/api/demo/devices").with(SecurityMockMvcRequestPostProcessors.user("operator")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/demo/devices")
                        .with(SecurityMockMvcRequestPostProcessors.user("operator"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试设备\",\"location\":\"A座\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanCreateDevice() throws Exception {
        mvc.perform(post("/api/demo/devices")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试设备\",\"location\":\"A座\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/demo/devices").with(SecurityMockMvcRequestPostProcessors.user("operator")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name").value(org.hamcrest.Matchers.hasItem("测试设备")));
    }

    @Test
    @Transactional
    void grantingPermissionShouldTakeEffectWithoutRelogin() throws Exception {
        Long roleId = ((Number) Db.selectObject("SELECT id FROM sys_roles WHERE role_code='ROLE_OPERATOR'")).longValue();
        Db.updateBySql("INSERT INTO role_permissions(role_id,permission_code) VALUES(?,?)",
                roleId, PermissionCodes.DEVICE_CREATE);
        mvc.perform(post("/api/demo/devices")
                        .with(SecurityMockMvcRequestPostProcessors.user("operator"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"授权生效\",\"location\":\"机房\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void loginShouldKeepSessionAndReturnOnlyPublicUserFields() throws Exception {
        var result = mvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operator\",\"password\":\"Operator@123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("operator"))
                .andExpect(jsonPath("$.data.superAdmin").value(false))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("operator"));
        mvc.perform(get("/api/auth/grants").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0]").value(PermissionCodes.DEVICE_READ));
    }

    @Test
    void loginShouldRejectInvalidPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operator\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void dashboardShouldReturnDatabaseCounts() throws Exception {
        mvc.perform(get("/api/dashboard").with(SecurityMockMvcRequestPostProcessors.user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissionCount").value(
                        ((Number) Db.selectObject("SELECT COUNT(*) FROM permission_definitions")).intValue()))
                .andExpect(jsonPath("$.data.deprecatedCount").value(
                        ((Number) Db.selectObject("SELECT COUNT(*) FROM permission_definitions WHERE status='DEPRECATED'")).intValue()))
                .andExpect(jsonPath("$.data.roleCount").value(
                        ((Number) Db.selectObject("SELECT COUNT(*) FROM sys_roles")).intValue()))
                .andExpect(jsonPath("$.data.userCount").value(
                        ((Number) Db.selectObject("SELECT COUNT(*) FROM sys_users")).intValue()))
                .andExpect(jsonPath("$.data.menuCount").value(
                        ((Number) Db.selectObject("SELECT COUNT(*) FROM sys_menus")).intValue()));
    }

    @Test
    @Transactional
    void synchronizationShouldRecordAuditAndExposeItInHistory() throws Exception {
        long before = ((Number) Db.selectObject("SELECT COUNT(*) FROM audit_logs WHERE action='SYNC'")).longValue();
        mvc.perform(post("/api/permissions/sync")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.discovered").value(registry.list(null, null, null).size()));
        assertEquals(before + 1, ((Number) Db.selectObject("SELECT COUNT(*) FROM audit_logs WHERE action='SYNC'")).longValue());
        mvc.perform(get("/api/audits").with(SecurityMockMvcRequestPostProcessors.user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].action").value("SYNC"));
    }

    @Test
    @Transactional
    void disabledUserShouldLoseAccessImmediately() throws Exception {
        Db.updateBySql("UPDATE sys_users SET enabled=FALSE WHERE username='operator'");
        mvc.perform(get("/api/demo/devices").with(SecurityMockMvcRequestPostProcessors.user("operator")))
                .andExpect(status().isForbidden());
    }

    @Test
    void menuShouldRejectUnknownPermission() {
        var body = new MenuCommand(null,"错误配置","/devices","Document",MenuType.PAGE,10,true,
                List.of("sop:unknown:fake"));
        assertThrows(BusinessException.class,()->menus.create(body));
    }
}
