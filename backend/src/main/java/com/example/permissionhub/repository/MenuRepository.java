package com.example.permissionhub.repository;

import com.example.permissionhub.dto.MenuCommand;
import com.example.permissionhub.dto.MenuView;
import com.example.permissionhub.entity.MenuPermission;
import com.example.permissionhub.entity.SysMenu;
import com.example.permissionhub.enums.MenuType;
import com.example.permissionhub.mapper.MenuPermissionMapper;
import com.example.permissionhub.mapper.SysMenuMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permissionhub.entity.table.MenuPermissionTableDef.MENU_PERMISSION;
import static com.example.permissionhub.entity.table.SysMenuTableDef.SYS_MENU;

/**
 * 菜单与权限绑定的数据访问；批量加载绑定，避免逐菜单查询。
 */
@Repository
@RequiredArgsConstructor
public class MenuRepository {

    private final SysMenuMapper menus;
    private final MenuPermissionMapper bindings;

    public List<MenuView> findAll() {
        Map<Long, List<String>> grouped = new HashMap<>();
        bindings.selectListByQuery(QueryWrapper.create().orderBy(MENU_PERMISSION.PERMISSION_CODE.asc()))
                .forEach(binding -> grouped.computeIfAbsent(binding.getMenuId(), ignored -> new ArrayList<>())
                        .add(binding.getPermissionCode()));
        return menus.selectListByQuery(QueryWrapper.create().orderBy(SYS_MENU.SORT_ORDER.asc(), SYS_MENU.ID.asc()))
                .stream().map(menu -> new MenuView(menu.getId(), menu.getParentId(), menu.getDisplayName(),
                        menu.getRoutePath(), menu.getIcon(), MenuType.valueOf(menu.getMenuType()),
                        menu.getSortOrder(), menu.getEnabled(), grouped.getOrDefault(menu.getId(), List.of()))).toList();
    }

    public List<String> codes(long menuId) {
        return bindings.selectObjectListByQueryAs(QueryWrapper.create().select(MENU_PERMISSION.PERMISSION_CODE)
                .where(MENU_PERMISSION.MENU_ID.eq(menuId)).orderBy(MENU_PERMISSION.PERMISSION_CODE.asc()), String.class);
    }

    public long insert(MenuCommand body) {
        SysMenu menu = new SysMenu();
        apply(menu, body);
        menus.insertSelective(menu);
        return menu.getId();
    }

    public int update(long id, MenuCommand body) {
        // UpdateEntity 记录显式写入的 null，允许将子菜单恢复为顶级菜单。
        SysMenu menu = UpdateEntity.of(SysMenu.class, id);
        apply(menu, body);
        return menus.update(menu);
    }

    private void apply(SysMenu menu, MenuCommand body) {
        menu.setParentId(body.parentId());
        menu.setDisplayName(body.displayName().trim());
        menu.setRoutePath(body.routePath());
        menu.setIcon(body.icon());
        menu.setMenuType(body.menuType().name());
        menu.setSortOrder(body.sortOrder());
        menu.setEnabled(body.enabled());
    }

    public void bindCodes(long id, List<String> codes) {
        bindings.deleteByCondition(MENU_PERMISSION.MENU_ID.eq(id));
        for (String code : codes) {
            MenuPermission binding = new MenuPermission();
            binding.setMenuId(id);
            binding.setPermissionCode(code);
            bindings.insertSelectiveWithPk(binding);
        }
    }

    public int children(long id) {
        return Math.toIntExact(menus.selectCountByCondition(SYS_MENU.PARENT_ID.eq(id)));
    }

    public int delete(long id) {
        return menus.deleteById(id);
    }
}
