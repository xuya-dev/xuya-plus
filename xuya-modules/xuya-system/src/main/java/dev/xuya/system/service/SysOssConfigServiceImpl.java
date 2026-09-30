package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.domain.SysOssConfig;
import dev.xuya.system.framework.oss.OssFactory;
import dev.xuya.system.mapper.SysOssConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OSS 配置服务实现：切换"使用中"配置时重建存储实例；secretKey 留空表示沿用原值
 */
@Service
@RequiredArgsConstructor
public class SysOssConfigServiceImpl implements SysOssConfigService {

    private final SysOssConfigMapper configMapper;
    private final OssFactory ossFactory;

    @Override
    @Transactional
    public void createConfig(SysOssConfig config) {
        checkKeyUnique(config.getConfigKey(), null);
        configMapper.insert(config);
        // 新配置即"使用中"则其余全部转备用，并重建存储实例
        if (config.getStatus() != null && config.getStatus() == 0) {
            activateOnly(config.getId());
        }
        ossFactory.refresh();
    }

    @Override
    @Transactional
    public void updateConfig(SysOssConfig config) {
        SysOssConfig exists = requireConfig(config.getId());
        checkKeyUnique(config.getConfigKey(), config.getId());
        // secretKey 留空：沿用原值（避免回显密钥带来的误清空）
        if (config.getSecretKey() == null || config.getSecretKey().isBlank()) {
            config.setSecretKey(exists.getSecretKey());
        }
        configMapper.updateById(config);
        if (config.getStatus() != null && config.getStatus() == 0) {
            activateOnly(config.getId());
        }
        ossFactory.refresh();
    }

    @Override
    @Transactional
    public void deleteConfigs(List<Long> ids) {
        for (Long id : ids) {
            SysOssConfig config = configMapper.selectById(id);
            if (config == null) {
                continue;
            }
            if (config.getStatus() != null && config.getStatus() == 0) {
                throw new QuickDevException("使用中的配置[" + config.getConfigName() + "]不允许删除，请先切换其他配置");
            }
        }
        configMapper.deleteBatchIds(ids);
        ossFactory.refresh();
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Integer status) {
        SysOssConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new QuickDevException("配置不存在");
        }
        if (status != null && status == 0) {
            activateOnly(id);
        } else {
            configMapper.updateById(SysOssConfig.builder().id(id).status(status).build());
        }
        ossFactory.refresh();
    }

    /** 全局唯一"使用中"：目标置 0，其余全部置 1 */
    private void activateOnly(Long activeId) {
        for (SysOssConfig other : configMapper.selectList(null)) {
            Integer target = other.getId().equals(activeId) ? 0 : 1;
            if (!target.equals(other.getStatus())) {
                SysOssConfig patch = SysOssConfig.builder().id(other.getId()).status(target).build();
                configMapper.updateById(patch);
            }
        }
    }

    private SysOssConfig requireConfig(Long id) {
        SysOssConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new QuickDevException("配置不存在");
        }
        return config;
    }

    private void checkKeyUnique(String configKey, Long excludeId) {
        SysOssConfig exists = configMapper.selectOne(new LambdaQueryWrapper<SysOssConfig>()
                .eq(SysOssConfig::getConfigKey, configKey));
        if (exists != null && !exists.getId().equals(excludeId)) {
            throw new QuickDevException("配置标识已存在: " + configKey);
        }
    }
}
