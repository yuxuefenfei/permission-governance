package com.example.permissionhub.service;

import com.example.permissionhub.dto.ScannedPermission;
import com.example.permissionhub.dto.ScannedEndpoint;
import com.example.permissionhub.enums.PermissionKind;
import com.example.permissionhub.security.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 使用 Spring MVC 已解析的路由表，而不是猜测 @GetMapping 与父级路径的拼接。
 * 若相同 code 的名称/模块/类型不一致，拒绝注册，防止发生“静默覆盖”。
 */
@Service
@RequiredArgsConstructor
public class PermissionScanService {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*(?::[a-zA-Z0-9]+){2,6}$");
    private final RequestMappingHandlerMapping handlerMapping;

    public List<ScannedPermission> scan() {
        Map<String, MutablePermission> registry = new TreeMap<>();
        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            // 只扫描方法：@RequirePermission 的 @Target 已明确不允许类级声明。
            RequirePermission annotation = AnnotatedElementUtils.findMergedAnnotation(
                    BridgeMethodResolver.findBridgedMethod(handler.getMethod()), RequirePermission.class);
            if (annotation == null) return;
            if (!CODE_PATTERN.matcher(annotation.code()).matches()) {
                throw new IllegalStateException("非法权限码：" + annotation.code());
            }
            MutablePermission value = registry.computeIfAbsent(annotation.code(), k ->
                    new MutablePermission(annotation.code(), annotation.name(), annotation.module(),
                            annotation.kind(), annotation.description()));
            value.assertCompatible(annotation);
            var patterns = mapping.getPathPatternsCondition() == null
                    ? mapping.getPatternsCondition().getPatterns()
                    : mapping.getPathPatternsCondition().getPatternValues();
            var methods = mapping.getMethodsCondition().getMethods().isEmpty()
                    ? Set.of(RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                            RequestMethod.PATCH, RequestMethod.DELETE)
                    : mapping.getMethodsCondition().getMethods();
            String handlerName = handler.getBeanType().getSimpleName() + "#" + handler.getMethod().getName();
            for (String pattern : patterns) {
                for (RequestMethod method : methods) {
                    value.endpoints.add(new ScannedEndpoint(method.name(), pattern, handlerName));
                }
            }
        });
        if (registry.isEmpty()) throw new IllegalStateException("未发现任何 @RequirePermission，请勿执行空快照同步");
        return registry.values().stream().map(MutablePermission::toImmutable).toList();
    }

    private static final class MutablePermission {
        final String code, name, module, description;
        final PermissionKind kind;
        final Set<ScannedEndpoint> endpoints = new LinkedHashSet<>();
        MutablePermission(String code, String name, String module,
                          PermissionKind kind, String description) {
            this.code=code; this.name=name; this.module=module; this.kind=kind; this.description=description;
        }
        void assertCompatible(RequirePermission annotation) {
            if (!name.equals(annotation.name()) || !module.equals(annotation.module())
                    || kind != annotation.kind() || !description.equals(annotation.description())) {
                throw new IllegalStateException("同一权限码的元数据不一致：" + code);
            }
        }
        ScannedPermission toImmutable() {
            return new ScannedPermission(code, name, module, kind, description,
                    endpoints.stream().sorted(Comparator.comparing(ScannedEndpoint::path)
                            .thenComparing(ScannedEndpoint::method)).toList());
        }
    }
}
