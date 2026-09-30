package dev.xuya.system.service;

import dev.xuya.system.domain.SysOssConfig;

import java.util.List;

/**
 * OSS 配置服务接口
 *
 * @see SysOssConfigServiceImpl
 */
public interface SysOssConfigService {

    /**
     * 新增配置
     */
    void createConfig(SysOssConfig config);

    /**
     * 修改配置（secretKey 留空表示沿用原值）
     */
    void updateConfig(SysOssConfig config);

    /**
     * 批量删除配置
     */
    void deleteConfigs(List<Long> ids);

    /**
     * 切换配置状态：置为"使用中"(0) 时其余配置全部转备用
     */
    void changeStatus(Long id, Integer status);
}
