package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.domain.vo.RouterVo;
import dev.xuya.system.framework.security.RbacCacheService;
import dev.xuya.system.domain.SysMenu;
import dev.xuya.system.domain.SysRoleMenu;
import dev.xuya.system.mapper.SysMenuMapper;
import dev.xuya.system.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单树/前端动态路由构建 + 菜单写服务（变更后刷新 RBAC 缓存）
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final RbacCacheService rbacCacheService;

    public MenuServiceImpl(SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper,
                       RbacCacheService rbacCacheService) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.rbacCacheService = rbacCacheService;
    }

    @Transactional
    @Override
    public void createMenu(SysMenu menu) {
        requireParent(menu.getParentId());
        if (menu.getStatus() == null) {
            menu.setStatus(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible("0");
        }
        menuMapper.insert(menu);
        rbacCacheService.refresh();
    }

    @Transactional
    @Override
    public void updateMenu(SysMenu menu) {
        if (menu.getId() == null) {
            throw new QuickDevException("菜单 ID 不能为空");
        }
        requireMenu(menu.getId());
        if (menu.getId().equals(menu.getParentId())) {
            throw new QuickDevException("上级菜单不能为自己");
        }
        requireParent(menu.getParentId());
        menuMapper.updateById(menu);
        rbacCacheService.refresh();
    }

    @Transactional
    @Override
    public void deleteMenu(Long id) {
        requireMenu(id);
        Long children = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new QuickDevException("存在下级菜单，不允许删除");
        }
        Long assigned = roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getMenuId, id));
        if (assigned != null && assigned > 0) {
            throw new QuickDevException("菜单已分配给角色，不允许删除");
        }
        menuMapper.deleteById(id);
        rbacCacheService.refresh();
    }

    private void requireParent(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (menuMapper.selectById(parentId) == null) {
            throw new QuickDevException("上级菜单不存在");
        }
    }

    private void requireMenu(Long id) {
        if (menuMapper.selectById(id) == null) {
            throw new QuickDevException("菜单不存在或已被删除");
        }
    }

    /**
     * 将扁平菜单构建为 RuoYi 风格动态路由（仅 M 目录 / C 菜单，F 按钮不参与路由）
     */
    @Override
    public List<RouterVo> buildRouters(List<SysMenu> menus) {
        List<SysMenu> routable = menus.stream()
                .filter(m -> "M".equals(m.getMenuType()) || "C".equals(m.getMenuType()))
                .toList();
        Map<Long, List<SysMenu>> byParent = routable.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        return buildChildren(0L, byParent);
    }

    private List<RouterVo> buildChildren(Long parentId, Map<Long, List<SysMenu>> byParent) {
        List<SysMenu> children = byParent.getOrDefault(parentId, List.of()).stream()
                .sorted(Comparator.comparing(SysMenu::getOrderNum,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<RouterVo> result = new ArrayList<>();
        for (SysMenu menu : children) {
            RouterVo router = new RouterVo();
            router.setName(capitalize(menu.getPath()));
            router.setPath(parentId == 0L && menu.getPath() != null && !menu.getPath().startsWith("/")
                    ? "/" + menu.getPath() : menu.getPath());
            router.setHidden("1".equals(menu.getVisible()));
            router.setComponent(resolveComponent(menu, parentId));
            router.setMeta(new RouterVo.Meta(menu.getMenuName(), menu.getIcon()));
            List<RouterVo> grandChildren = buildChildren(menu.getId(), byParent);
            router.setChildren(grandChildren.isEmpty() ? null : grandChildren);
            result.add(router);
        }
        return result;
    }

    private String resolveComponent(SysMenu menu, Long parentId) {
        if ("M".equals(menu.getMenuType())) {
            return parentId == 0L ? "Layout" : "ParentView";
        }
        return menu.getComponent();
    }

    private String capitalize(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
}
