package com.xuya.plus.framework.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuya.plus.system.entity.SysConfig;
import com.xuya.plus.system.mapper.SysConfigMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参数配置全量内存缓存（sys_config -> Map&lt;key,value&gt;）。
 * 变更接口主动 refresh()，定时全量重建兜底；业务方取值零查库。
 */
@Service
public class ConfigCacheService {

    private final SysConfigMapper configMapper;

    private volatile Map<String, String> cache = Map.of();
    private volatile LocalDateTime loadedAt;

    public ConfigCacheService(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedDelayString = "${xuya-plus.cache-auto-refresh-seconds:60}", timeUnit = TimeUnit.SECONDS)
    public void scheduledRefresh() {
        refresh();
    }

    public synchronized void refresh() {
        List<SysConfig> all = configMapper.selectList(null);
        cache = all.stream().collect(Collectors.toMap(
                SysConfig::getConfigKey, SysConfig::getConfigValue, (a, b) -> a));
        loadedAt = LocalDateTime.now();
    }

    public String getValue(String key, String defaultValue) {
        return cache.getOrDefault(key, defaultValue);
    }

    public Map<String, String> getAll() {
        return cache;
    }

    public LocalDateTime getLoadedAt() {
        return loadedAt;
    }

    public int size() {
        return cache.size();
    }
}
