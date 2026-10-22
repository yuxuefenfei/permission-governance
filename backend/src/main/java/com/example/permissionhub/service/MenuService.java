package com.example.permissionhub.service;

import com.example.permissionhub.dto.MenuCommand;
import com.example.permissionhub.dto.MenuView;
import com.example.permissionhub.enums.MenuType;
import com.example.permissionhub.exception.BusinessException;
import com.example.permissionhub.repository.MenuRepository;
import com.example.permissionhub.security.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单树不承担 API 授权，仅作为“前端导航与权限码”的关联关系。
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository repo;
    private final PermissionRegistryService registry;
    private final PermissionChecker checker;
    private final AuditService audit;

    public List<MenuView> all() {
        return repo.findAll();
    }

    public List<MenuView> mine() {
        List<MenuView> menus = repo.findAll().stream().filter(MenuView::enabled).toList();
        Map<Long, MenuView> byId = menus.stream().collect(Collectors.toMap(MenuView::id, m -> m));
        Set<Long> visible = new HashSet<>();
        for (MenuView page : menus) {
            if (page.menuType() != MenuType.PAGE || page.permissionCodes().isEmpty()
                    || page.permissionCodes().stream().noneMatch(checker::hasPermission)) continue;
            // 权限满足后，必须保证完整祖先链都存在且都是已启用目录。
            List<Long> ancestors = new ArrayList<>();
            Set<Long> visited = new HashSet<>();
            Long parentId = page.parentId();
            boolean valid = true;
            while (parentId != null) {
                MenuView parent = byId.get(parentId);
                if (!visited.add(parentId) || parent == null || parent.menuType() != MenuType.DIRECTORY) {
                    valid = false;
                    break;
                }
                ancestors.add(parentId);
                parentId = parent.parentId();
            }
            if (valid) {
                visible.add(page.id());
                visible.addAll(ancestors);
            }
        }
        return menus.stream().filter(m -> visible.contains(m.id())).toList();
    }

    @Transactional
    public long create(MenuCommand body) {
        validate(body, null);
        long id = repo.insert(normalize(body));
        repo.bindCodes(id, body.permissionCodes());
        audit.record("CREATE", "MENU", String.valueOf(id), body.displayName());
        return id;
    }

    @Transactional
    public void update(long id, MenuCommand body) {
        validate(body, id);
        if (repo.update(id, normalize(body)) == 0) throw new BusinessException(404, "菜单不存在");
        repo.bindCodes(id, body.permissionCodes());
        audit.record("UPDATE", "MENU", String.valueOf(id), body.displayName());
    }

    @Transactional
    public void delete(long id) {
        if (repo.children(id) > 0) throw new BusinessException(409, "请先删除子菜单");
        if (repo.delete(id) == 0) throw new BusinessException(404, "菜单不存在");
        audit.record("DELETE", "MENU", String.valueOf(id), "删除菜单");
    }

    private MenuCommand normalize(MenuCommand command) {
        return new MenuCommand(command.parentId(), command.displayName(),
                command.menuType() == MenuType.DIRECTORY ? "" : command.routePath().trim(),
                command.icon() == null ? "" : command.icon().trim(), command.menuType(),
                command.sortOrder(), command.enabled(), command.permissionCodes());
    }

    private void validate(MenuCommand body, Long editingId) {
        List<MenuView> all = repo.findAll();
        Map<Long, MenuView> index = all.stream().collect(Collectors.toMap(MenuView::id, x -> x));
        if (editingId != null && !index.containsKey(editingId)) throw new BusinessException(404, "菜单不存在");
        if (body.menuType() == MenuType.PAGE) {
            if (body.routePath() == null || !body.routePath().startsWith("/") || body.routePath().length() > 160)
                throw new BusinessException(400, "页面路由须以 / 开头且不能超过 160 字符");
            if (body.permissionCodes().isEmpty()) throw new BusinessException(400, "页面至少绑定一个权限");
            if (all.stream().anyMatch(m -> !Objects.equals(editingId, m.id()) && m.menuType() == MenuType.PAGE
                    && m.routePath().equals(body.routePath())))
                throw new BusinessException(409, "页面路由已被其他菜单使用");
        } else if (!body.permissionCodes().isEmpty()) {
            throw new BusinessException(400, "目录不能直接绑定 API 权限");
        }
        if (body.icon() != null && body.icon().length() > 60) throw new BusinessException(400, "图标名称过长");
        registry.verifyActiveCodes(body.permissionCodes());
        if (body.parentId() != null) {
            MenuView parent = index.get(body.parentId());
            if (parent == null || parent.menuType() != MenuType.DIRECTORY)
                throw new BusinessException(400, "上级菜单必须是已存在的目录");
            Set<Long> visited = new HashSet<>();
            Long cursor = body.parentId();
            while (cursor != null && visited.add(cursor)) {
                if (cursor.equals(editingId)) throw new BusinessException(400, "菜单不能将自身或子节点设置为父级");
                MenuView item = index.get(cursor);
                cursor = item == null ? null : item.parentId();
            }
        }
        if (editingId != null && body.menuType() == MenuType.PAGE && repo.children(editingId) > 0)
            throw new BusinessException(400, "包含子节点的目录不能修改成页面");
    }
}
