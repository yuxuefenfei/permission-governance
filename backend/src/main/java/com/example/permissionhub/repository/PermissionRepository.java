package com.example.permissionhub.repository;

import com.example.permissionhub.dto.EndpointView;
import com.example.permissionhub.dto.PermissionView;
import com.example.permissionhub.dto.ScannedEndpoint;
import com.example.permissionhub.dto.ScannedPermission;
import com.example.permissionhub.entity.PermissionDefinition;
import com.example.permissionhub.entity.PermissionEndpoint;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.enums.PermissionStatus;
import com.example.permissionhub.mapper.PermissionDefinitionMapper;
import com.example.permissionhub.mapper.PermissionEndpointMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.example.permissionhub.entity.table.PermissionDefinitionTableDef.PERMISSION_DEFINITION;
import static com.example.permissionhub.entity.table.PermissionEndpointTableDef.PERMISSION_ENDPOINT;
import static com.mybatisflex.core.query.QueryMethods.lower;

/**
 * 权限定义与端点持久化；同步事务与业务校验由服务负责。
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepository {

    private final PermissionDefinitionMapper definitions;
    private final PermissionEndpointMapper endpoints;

    public int updateCodeDefinition(ScannedPermission definition, String appCode, String serviceName) {
        PermissionDefinition entity = from(definition, appCode, serviceName);
        entity = UpdateWrapper.of(entity)
                .setRaw(PERMISSION_DEFINITION.LAST_SEEN_AT, "CURRENT_TIMESTAMP").toEntity();
        return definitions.updateByCondition(entity, PERMISSION_DEFINITION.CODE.eq(definition.code())
                .and(PERMISSION_DEFINITION.APP_CODE.eq(appCode)).and(PERMISSION_DEFINITION.SOURCE.eq("CODE")));
    }

    public boolean codeExists(String code) {
        return code != null && !code.isBlank() && definitions.selectCountByCondition(PERMISSION_DEFINITION.CODE.eq(code)) > 0;
    }

    public void insertCodeDefinition(ScannedPermission definition, String appCode, String serviceName) {
        definitions.insertSelectiveWithPk(from(definition, appCode, serviceName));
    }

    private PermissionDefinition from(ScannedPermission definition, String appCode, String serviceName) {
        PermissionDefinition entity = new PermissionDefinition();
        entity.setCode(definition.code());
        entity.setAppCode(appCode);
        entity.setServiceName(serviceName);
        entity.setDisplayName(definition.name());
        entity.setModuleName(definition.module());
        entity.setKind(definition.kind().name());
        entity.setDescription(definition.description());
        entity.setSource("CODE");
        entity.setStatus("ACTIVE");
        return entity;
    }

    public void replaceEndpoints(ScannedPermission definition) {
        endpoints.deleteByCondition(PERMISSION_ENDPOINT.PERMISSION_CODE.eq(definition.code()));
        for (ScannedEndpoint endpoint : definition.endpoints()) {
            PermissionEndpoint entity = new PermissionEndpoint();
            entity.setPermissionCode(definition.code());
            entity.setHttpMethod(endpoint.method());
            entity.setUrlPath(endpoint.path());
            entity.setHandler(endpoint.handler());
            endpoints.insertSelective(entity);
        }
    }

    public List<String> activeCodeDefinitions(String appCode, String serviceName) {
        return definitions.selectObjectListByQueryAs(QueryWrapper.create().select(PERMISSION_DEFINITION.CODE)
                .where(PERMISSION_DEFINITION.APP_CODE.eq(appCode)).and(PERMISSION_DEFINITION.SERVICE_NAME.eq(serviceName))
                .and(PERMISSION_DEFINITION.SOURCE.eq("CODE")).and(PERMISSION_DEFINITION.STATUS.eq("ACTIVE")), String.class);
    }

    public void markDeprecated(String code) {
        PermissionDefinition entity = new PermissionDefinition();
        entity.setStatus("DEPRECATED");
        definitions.updateByCondition(entity, PERMISSION_DEFINITION.CODE.eq(code));
    }

    public boolean isActiveCode(String code, String appCode) {
        return code != null && !code.isBlank() && definitions.selectCountByCondition(PERMISSION_DEFINITION.CODE.eq(code)
                .and(PERMISSION_DEFINITION.APP_CODE.eq(appCode)).and(PERMISSION_DEFINITION.STATUS.eq("ACTIVE"))) > 0;
    }

    public Optional<PermissionView> find(String code, String appCode) {
        if (code == null || code.isBlank()) return Optional.empty();
        PermissionDefinition entity = definitions.selectOneByCondition(PERMISSION_DEFINITION.CODE.eq(code)
                .and(PERMISSION_DEFINITION.APP_CODE.eq(appCode)));
        if (entity == null) return Optional.empty();
        PermissionView view = view(entity);
        view.setEndpoints(endpoints.selectListByQuery(QueryWrapper.create()
                        .where(PERMISSION_ENDPOINT.PERMISSION_CODE.eq(code))
                        .orderBy(PERMISSION_ENDPOINT.URL_PATH.asc(), PERMISSION_ENDPOINT.HTTP_METHOD.asc()))
                .stream().map(this::endpointView).toList());
        return Optional.of(view);
    }

    public List<PermissionView> list(String keyword, String module, String status, String appCode) {
        QueryWrapper query = QueryWrapper.create().where(PERMISSION_DEFINITION.APP_CODE.eq(appCode));
        if (keyword != null && !keyword.isBlank()) {
            String term = keyword.trim().toLowerCase(Locale.ROOT);
            query.and(lower(PERMISSION_DEFINITION.CODE).like(term)
                    .or(lower(PERMISSION_DEFINITION.DISPLAY_NAME).like(term)));
        }
        if (module != null && !module.isBlank()) query.and(PERMISSION_DEFINITION.MODULE_NAME.eq(module));
        if (status != null && !status.isBlank()) query.and(PERMISSION_DEFINITION.STATUS.eq(status));
        query.orderBy(PERMISSION_DEFINITION.MODULE_NAME.asc(), PERMISSION_DEFINITION.CODE.asc());
        List<PermissionView> views = definitions.selectListByQuery(query).stream().map(this::view).toList();
        Map<String, List<EndpointView>> grouped = new HashMap<>();
        endpoints.selectListByQuery(QueryWrapper.create().select(PERMISSION_ENDPOINT.ALL_COLUMNS)
                        .from(PERMISSION_ENDPOINT).innerJoin(PERMISSION_DEFINITION)
                        .on(PERMISSION_DEFINITION.CODE.eq(PERMISSION_ENDPOINT.PERMISSION_CODE))
                        .where(PERMISSION_DEFINITION.APP_CODE.eq(appCode))
                        .orderBy(PERMISSION_ENDPOINT.URL_PATH.asc(), PERMISSION_ENDPOINT.HTTP_METHOD.asc()))
                .forEach(endpoint -> grouped.computeIfAbsent(endpoint.getPermissionCode(), ignored -> new ArrayList<>())
                        .add(endpointView(endpoint)));
        views.forEach(view -> view.setEndpoints(grouped.getOrDefault(view.getCode(), List.of())));
        return views;
    }

    private EndpointView endpointView(PermissionEndpoint entity) {
        return new EndpointView(entity.getHttpMethod(), entity.getUrlPath(), entity.getHandler());
    }

    private PermissionView view(PermissionDefinition entity) {
        return PermissionView.builder().code(entity.getCode()).displayName(entity.getDisplayName())
                .moduleName(entity.getModuleName()).kind(PermissionKind.valueOf(entity.getKind()))
                .status(PermissionStatus.valueOf(entity.getStatus())).description(entity.getDescription())
                .serviceName(entity.getServiceName()).lastSeenAt(entity.getLastSeenAt()).build();
    }
}
