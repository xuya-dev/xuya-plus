package com.xuya.plus.monitor.controller;

import dev.xuya.core.auth.RequiresLogin;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import dev.xuya.core.translate.DictCacheService;
import dev.xuya.core.translate.TranslateExecutor;
import com.xuya.plus.framework.monitor.OnlineUserService;
import com.xuya.plus.framework.security.ConfigCacheService;
import com.xuya.plus.framework.security.RbacCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 缓存管理：查看 RBAC/字典/参数/在线用户缓存状态，手动刷新（缓存优先策略的运维入口）。
 */
@RestController
@RequestMapping("/sys-cache")
public class SysCacheController {

    private final RbacCacheService rbacCacheService;
    private final ConfigCacheService configCacheService;
    private final DictCacheService dictCacheService;
    private final TranslateExecutor translateExecutor;
    private final OnlineUserService onlineUserService;

    public SysCacheController(RbacCacheService rbacCacheService,
                              ConfigCacheService configCacheService,
                              DictCacheService dictCacheService,
                              TranslateExecutor translateExecutor,
                              OnlineUserService onlineUserService) {
        this.rbacCacheService = rbacCacheService;
        this.configCacheService = configCacheService;
        this.dictCacheService = dictCacheService;
        this.translateExecutor = translateExecutor;
        this.onlineUserService = onlineUserService;
    }

    /**
     * 缓存状态总览
     */
    @RequiresLogin
    @GetMapping
    public R<Map<String, Object>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("rbac", Map.of("menus", rbacCacheService.menuSize(),
                "loadedAt", String.valueOf(rbacCacheService.getLoadedAt())));
        info.put("dict", Map.of("entries", dictCacheService.size(),
                "loadedAt", String.valueOf(dictCacheService.loadedAt())));
        info.put("config", Map.of("entries", configCacheService.size(),
                "loadedAt", String.valueOf(configCacheService.getLoadedAt())));
        info.put("online", Map.of("sessions", onlineUserService.count()));
        return R.ok(info);
    }

    /**
     * 手动刷新：name = rbac / dict / config / all
     */
    @RequiresPerm("sys:cache:refresh")
    @QuickLog(module = "缓存管理", description = "刷新缓存")
    @PostMapping("/refresh/{name}")
    public R<Map<String, Object>> refresh(@PathVariable String name) {
        switch (name) {
            case "rbac" -> rbacCacheService.refresh();
            case "dict" -> {
                dictCacheService.refresh();
                translateExecutor.clearCache();
            }
            case "config" -> configCacheService.refresh();
            case "all" -> {
                rbacCacheService.refresh();
                dictCacheService.refresh();
                translateExecutor.clearCache();
                configCacheService.refresh();
            }
            default -> throw new IllegalArgumentException("未知缓存: " + name);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("refreshed", name);
        result.put("at", LocalDateTime.now().toString());
        return R.ok("缓存已刷新", result);
    }
}
