package com.xuya.plus.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import com.xuya.plus.framework.security.ConfigCacheService;
import com.xuya.plus.system.entity.SysConfig;
import com.xuya.plus.system.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 参数配置写服务：变更后立即刷新内存缓存
 */
@Service
public class ConfigService {

    private final SysConfigMapper configMapper;
    private final ConfigCacheService configCacheService;

    public ConfigService(SysConfigMapper configMapper, ConfigCacheService configCacheService) {
        this.configMapper = configMapper;
        this.configCacheService = configCacheService;
    }

    @Transactional
    public void createConfig(SysConfig config) {
        checkKeyUnique(config.getConfigKey(), null);
        configMapper.insert(config);
        configCacheService.refresh();
    }

    @Transactional
    public void updateConfig(SysConfig config) {
        if (config.getId() == null) {
            throw new QuickDevException("参数 ID 不能为空");
        }
        requireConfig(config.getId());
        checkKeyUnique(config.getConfigKey(), config.getId());
        configMapper.updateById(config);
        configCacheService.refresh();
    }

    @Transactional
    public void deleteConfigs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new QuickDevException("请选择要删除的参数");
        }
        for (Long id : ids) {
            SysConfig config = requireConfig(id);
            if ("Y".equals(config.getConfigType())) {
                throw new QuickDevException("内置参数[" + config.getConfigName() + "]不允许删除");
            }
        }
        configMapper.deleteBatchIds(ids);
        configCacheService.refresh();
    }

    /**
     * 按键名取值（供业务/前端使用，走内存缓存零查库）
     */
    public String getValue(String key) {
        return configCacheService.getValue(key, null);
    }

    private void checkKeyUnique(String key, Long excludeId) {
        SysConfig exists = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key));
        if (exists != null && !exists.getId().equals(excludeId)) {
            throw new QuickDevException("参数键名已存在: " + key);
        }
    }

    private SysConfig requireConfig(Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new QuickDevException("参数不存在或已被删除");
        }
        return config;
    }
}
