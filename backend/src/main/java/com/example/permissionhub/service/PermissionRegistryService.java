package com.example.permissionhub.service;

import com.example.permissionhub.config.PermissionProperties;
import com.example.permissionhub.dto.PermissionView;
import com.example.permissionhub.dto.ScannedPermission;
import com.example.permissionhub.dto.SyncSummary;
import com.example.permissionhub.exception.BusinessException;
import com.example.permissionhub.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限同步与校验；同步只更新代码托管的元数据，不触碰角色与菜单授权。
 */
@Service
@RequiredArgsConstructor
public class PermissionRegistryService {

    private final PermissionRepository repository;
    private final PermissionScanService scanner;
    private final PermissionProperties properties;
    private final AuditService audit;

    @Transactional
    public SyncSummary synchronize(boolean reconcileMissing) {
        List<ScannedPermission> found = scanner.scan();
        String appCode = properties.applicationCode();
        String serviceName = properties.serviceName();
        Set<String> activeCodes = new HashSet<>();
        int countEndpoints = 0;
        for (ScannedPermission definition : found) {
            activeCodes.add(definition.code());
            if (repository.updateCodeDefinition(definition, appCode, serviceName) == 0) {
                // 其他 app / 手工资源使用相同 code 时必须报错，不允许抢占编码。
                if (repository.codeExists(definition.code()))
                    throw new BusinessException(409, "权限码已被其他来源占用：" + definition.code());
                repository.insertCodeDefinition(definition, appCode, serviceName);
            }
            repository.replaceEndpoints(definition);
            countEndpoints += definition.endpoints().size();
        }
        int deprecated = 0;
        if (reconcileMissing) {
            for (String code : repository.activeCodeDefinitions(appCode, serviceName)) {
                if (!activeCodes.contains(code)) {
                    // 保留旧端点及历史授权，不物理删除。
                    repository.markDeprecated(code);
                    deprecated++;
                }
            }
            audit.record("SYNC", "PERMISSION", "ALL", "权限同步：" + found.size() + " 项");
        }
        return new SyncSummary(found.size(), countEndpoints, deprecated);
    }

    public List<PermissionView> list(String keyword, String module, String status) {
        return repository.list(keyword, module, status, properties.applicationCode());
    }

    public PermissionView get(String code) {
        return repository.find(code, properties.applicationCode())
                .orElseThrow(() -> new BusinessException(404, "权限不存在"));
    }

    public void verifyActiveCodes(List<String> codes) {
        if (codes == null) throw new BusinessException(400, "权限集合不可为空");
        if (codes.size() != new HashSet<>(codes).size()) throw new BusinessException(400, "权限码不允许重复");
        for (String code : codes) {
            if (!repository.isActiveCode(code, properties.applicationCode()))
                throw new BusinessException(400, "权限不存在或已废弃：" + code);
        }
    }
}
