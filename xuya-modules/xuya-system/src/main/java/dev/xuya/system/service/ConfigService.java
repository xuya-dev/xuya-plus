package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.system.framework.security.ConfigCacheService;
import dev.xuya.system.domain.SysConfig;
import dev.xuya.system.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
/**
 * ConfigService 接口（阿里巴巴分层规范：Service 层接口 + 实现分离）
 */
public interface ConfigService {

    void createConfig(SysConfig config);

    void updateConfig(SysConfig config);

    void deleteConfigs(List<Long> ids);

    /**
     * 按键名取值（供业务/前端使用，走内存缓存零查库）
     */
    String getValue(String key);
}