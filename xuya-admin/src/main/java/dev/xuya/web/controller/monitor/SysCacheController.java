package dev.xuya.web.controller.monitor;

import dev.xuya.core.auth.RequiresLogin;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import dev.xuya.core.translate.DictCacheService;
import dev.xuya.core.translate.TranslateExecutor;
import dev.xuya.common.redis.OnlineUserService;
import dev.xuya.system.framework.security.ConfigCacheService;
import dev.xuya.system.framework.security.RbacCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;
import java.util.ArrayList;
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
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    public SysCacheController(RbacCacheService rbacCacheService,
                              ConfigCacheService configCacheService,
                              DictCacheService dictCacheService,
                              TranslateExecutor translateExecutor,
                              OnlineUserService onlineUserService,
                              org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate) {
        this.rbacCacheService = rbacCacheService;
        this.configCacheService = configCacheService;
        this.dictCacheService = dictCacheService;
        this.translateExecutor = translateExecutor;
        this.onlineUserService = onlineUserService;
        this.stringRedisTemplate = stringRedisTemplate;
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
     * 缓存键管理：按模式列出键（默认前 200 个）
     */
    @RequiresPerm("sys:cache:list")
    @GetMapping("/keys")
    public R<List<String>> keys(@RequestParam(value = "pattern", defaultValue = "*") String pattern) {
        var keys = new ArrayList<>(stringRedisTemplate.keys(
                pattern.contains("*") ? pattern : pattern + "*"));
        java.util.Collections.sort(keys);
        if (keys.size() > 200) {
            keys = new ArrayList<>(keys.subList(0, 200));
        }
        return R.ok(keys);
    }

    /**
     * 缓存键详情：类型 / TTL / 值预览
     */
    @RequiresPerm("sys:cache:list")
    @GetMapping("/key")
    public R<Map<String, Object>> keyInfo(@RequestParam("name") String name) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("key", name);
        info.put("ttlSeconds", stringRedisTemplate.getExpire(name));
        var type = stringRedisTemplate.type(name);
        String typeName = type == null ? "none" : type.name().toLowerCase();
        info.put("type", typeName);
        switch (typeName) {
            case "string" -> info.put("value", stringRedisTemplate.opsForValue().get(name));
            case "list" -> info.put("value", stringRedisTemplate.opsForList().range(name, 0, 20));
            case "set" -> info.put("value", stringRedisTemplate.opsForSet().members(name));
            case "zset" -> info.put("value", stringRedisTemplate.opsForZSet().range(name, 0, 20));
            case "hash" -> info.put("value", stringRedisTemplate.opsForHash().entries(name));
            default -> info.put("value", null);
        }
        return R.ok(info);
    }

    /**
     * 删除缓存键
     */
    @RequiresPerm("sys:cache:remove")
    @QuickLog(module = "缓存管理", description = "删除缓存键")
    @DeleteMapping("/key/{name}")
    public R<Void> deleteKey(@PathVariable String name) {
        stringRedisTemplate.delete(name);
        return R.ok();
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
