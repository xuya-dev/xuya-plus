package com.xuya.plus.framework.security;

import com.xuya.plus.system.entity.SysMenu;
import com.xuya.plus.system.entity.SysRole;
import com.xuya.plus.system.mapper.SysMenuMapper;
import com.xuya.plus.system.mapper.SysRoleMapper;
import com.xuya.plus.system.mapper.SysRoleMenuMapper;
import com.xuya.plus.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RBAC 全量内存缓存（缓存优先：任何请求都不为权限数据回源查库）。
 *
 * <ul>
 *   <li>userId -> 启用角色集合</li>
 *   <li>roleId -> roleKey / 权限码集合 / 授权菜单集合</li>
 *   <li>全量启用菜单列表（构建前端路由）</li>
 * </ul>
 * 刷新时机：启动加载、用户/角色/菜单/授权变更后主动 refresh()、
 * 定时全量重建兜底（xuya-plus.cache-auto-refresh-seconds，多实例最终一致）。
 */
@Service
public class RbacCacheService {

    /** 超级管理员角色 key：拥有 *:*:* 全部权限 */
    public static final String SUPER_ROLE_KEY = "admin";
    public static final String ALL_PERMISSION = "*:*:*";

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    @Value("${xuya-plus.cache-auto-refresh-seconds:60}")
    private long autoRefreshSeconds;

    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of(), Map.of(), Map.of(), List.of(), null);

    public RbacCacheService(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                            SysRoleMenuMapper roleMenuMapper, SysMenuMapper menuMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedDelayString = "${xuya-plus.cache-auto-refresh-seconds:60}", timeUnit = TimeUnit.SECONDS)
    public void scheduledRefresh() {
        if (autoRefreshSeconds > 0) {
            refresh();
        }
    }

    public synchronized void refresh() {
        // 仅加载启用角色（停用角色的授权立即失效）
        Map<Long, SysRole> enabledRoles = roleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 0)).stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));

        Map<Long, Set<Long>> userRoleIds = userRoleMapper.selectList(null).stream()
                .filter(ur -> enabledRoles.containsKey(ur.getRoleId()))
                .collect(Collectors.groupingBy(com.xuya.plus.system.entity.SysUserRole::getUserId,
                        Collectors.mapping(com.xuya.plus.system.entity.SysUserRole::getRoleId, Collectors.toSet())));

        Map<Long, Set<Long>> roleMenuIds = roleMenuMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(com.xuya.plus.system.entity.SysRoleMenu::getRoleId,
                        Collectors.mapping(com.xuya.plus.system.entity.SysRoleMenu::getMenuId, Collectors.toSet())));

        List<SysMenu> menus = menuMapper.selectList(
                        new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 0))
                .stream().sorted(Comparator.comparing(SysMenu::getOrderNum,
                        Comparator.nullsLast(Comparator.naturalOrder()))).toList();

        Map<Long, Set<String>> rolePerms = new java.util.HashMap<>();
        enabledRoles.keySet().forEach(roleId -> rolePerms.put(roleId, new LinkedHashSet<>()));
        for (SysMenu menu : menus) {
            if (menu.getPerms() == null || menu.getPerms().isBlank()) {
                continue;
            }
            roleMenuIds.forEach((roleId, menuIds) -> {
                if (menuIds.contains(menu.getId())) {
                    rolePerms.get(roleId).add(menu.getPerms());
                }
            });
        }

        Map<Long, String> roleKeys = enabledRoles.values().stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleKey));

        snapshot = new Snapshot(Map.copyOf(userRoleIds), Map.copyOf(roleKeys),
                Map.copyOf(rolePerms), Map.copyOf(roleMenuIds), menus, LocalDateTime.now());
    }

    public boolean isSuperAdmin(Long userId) {
        return userId != null && (userId == 1L || getRoleKeys(userId).contains(SUPER_ROLE_KEY));
    }

    public Set<Long> getUserRoleIds(Long userId) {
        return snapshot.userRoleIds.getOrDefault(userId, Set.of());
    }

    public Set<String> getRoleKeys(Long userId) {
        return getUserRoleIds(userId).stream()
                .map(snapshot.roleKeys::get)
                .filter(k -> k != null && !k.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getPerms(Long userId) {
        if (isSuperAdmin(userId)) {
            return Set.of(ALL_PERMISSION);
        }
        return getUserRoleIds(userId).stream()
                .flatMap(roleId -> snapshot.rolePerms.getOrDefault(roleId, Set.of()).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 全量启用菜单
     */
    public List<SysMenu> getAllMenus() {
        return snapshot.menus;
    }

    /**
     * 用户可见的目录/菜单（不含 F 按钮；超管看全部）
     */
    public List<SysMenu> getUserMenus(Long userId) {
        if (isSuperAdmin(userId)) {
            return snapshot.menus;
        }
        Set<Long> visibleMenuIds = getUserRoleIds(userId).stream()
                .flatMap(roleId -> snapshot.roleMenuIds.getOrDefault(roleId, Set.of()).stream())
                .collect(Collectors.toSet());
        return snapshot.menus.stream()
                .filter(m -> !"F".equals(m.getMenuType()))
                .filter(m -> visibleMenuIds.contains(m.getId()))
                .toList();
    }

    public LocalDateTime getLoadedAt() {
        return snapshot.loadedAt;
    }

    public int menuSize() {
        return snapshot.menus.size();
    }

    private record Snapshot(Map<Long, Set<Long>> userRoleIds,
                            Map<Long, String> roleKeys,
                            Map<Long, Set<String>> rolePerms,
                            Map<Long, Set<Long>> roleMenuIds,
                            List<SysMenu> menus,
                            LocalDateTime loadedAt) {
    }
}
