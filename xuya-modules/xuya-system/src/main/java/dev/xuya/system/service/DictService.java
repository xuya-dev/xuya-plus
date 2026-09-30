package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.translate.DictCacheService;
import dev.xuya.core.translate.TranslateExecutor;
import dev.xuya.system.domain.SysDictData;
import dev.xuya.system.domain.SysDictType;
import dev.xuya.system.mapper.SysDictDataMapper;
import dev.xuya.system.mapper.SysDictTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
/**
 * DictService 接口（阿里巴巴分层规范：Service 层接口 + 实现分离）
 */
public interface DictService {

    void createType(SysDictType type);

    void updateType(SysDictType type);

    void deleteTypes(List<Long> ids);

    /**
     * 前端下拉框：某类型的启用字典项
     */
    List<SysDictData> listByType(String dictType);

    /**
     * 立即刷新字典缓存并清空翻译结果缓存
     */
    void refreshCache();
}