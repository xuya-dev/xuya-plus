package dev.xuya.system.framework.oss;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.common.oss.LocalStorage;
import dev.xuya.common.oss.S3Storage;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.domain.SysOssConfig;
import dev.xuya.system.mapper.SysOssConfigMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象存储工厂：按 config_key 构建并缓存存储实例（实现来自 xuya-common-oss）；
 * "使用中"（status=0）配置经 getActive() 获取，配置变更/切换后调用 refresh() 重建。
 */
@Component
public class OssFactory {

    private final SysOssConfigMapper configMapper;
    private final Map<String, dev.xuya.common.oss.OssStorage> cache = new ConcurrentHashMap<>();
    private volatile String activeKey;

    public OssFactory(SysOssConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    /** 当前使用中的存储 */
    public synchronized dev.xuya.common.oss.OssStorage getActive() {
        if (activeKey != null) {
            dev.xuya.common.oss.OssStorage cached = cache.get(activeKey);
            if (cached != null) {
                return cached;
            }
        }
        SysOssConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysOssConfig>()
                .eq(SysOssConfig::getStatus, 0)
                .last("limit 1"));
        if (config == null) {
            throw new QuickDevException("未配置使用中的对象存储，请前往 OSS 配置页启用");
        }
        activeKey = config.getConfigKey();
        return cache.computeIfAbsent(activeKey, k -> build(config));
    }

    /** 按存储标识取存储实例（历史文件的删除/下载不随配置切换漂移） */
    public synchronized dev.xuya.common.oss.OssStorage getByKey(String configKey) {
        if (configKey == null || configKey.isBlank()) {
            return getActive();
        }
        dev.xuya.common.oss.OssStorage cached = cache.get(configKey);
        if (cached != null) {
            return cached;
        }
        SysOssConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysOssConfig>()
                .eq(SysOssConfig::getConfigKey, configKey)
                .last("limit 1"));
        if (config == null) {
            throw new QuickDevException("存储配置不存在: " + configKey);
        }
        return cache.computeIfAbsent(configKey, k -> build(config));
    }

    /** 配置变更/切换后清空缓存重建 */
    public synchronized void refresh() {
        cache.clear();
        activeKey = null;
    }

    private dev.xuya.common.oss.OssStorage build(SysOssConfig config) {
        if ("local".equals(config.getConfigKey())) {
            return new LocalStorage(config.getStoragePath());
        }
        if (config.getBucketName() == null || config.getBucketName().isBlank()
                || config.getEndpoint() == null || config.getEndpoint().isBlank()) {
            throw new QuickDevException("S3 存储配置缺少桶名或端点: " + config.getConfigKey());
        }
        return new S3Storage(config.getConfigKey(), config.getAccessKey(), config.getSecretKey(),
                config.getBucketName(), config.getEndpoint(), config.getDomain(), config.getRegion());
    }
}
