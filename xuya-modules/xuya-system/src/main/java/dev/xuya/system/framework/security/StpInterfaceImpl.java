package dev.xuya.system.framework.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源：全部委托 RbacCacheService 内存缓存，不回源查库。
 * 超级管理员返回 *:*:*（Sa-Token 通配）+ admin 角色。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final RbacCacheService rbacCacheService;

    public StpInterfaceImpl(RbacCacheService rbacCacheService) {
        this.rbacCacheService = rbacCacheService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.copyOf(rbacCacheService.getPerms(toLong(loginId)));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return List.copyOf(rbacCacheService.getRoleKeys(toLong(loginId)));
    }

    private Long toLong(Object loginId) {
        return loginId == null ? null : Long.valueOf(loginId.toString());
    }
}
