package com.xuya.plus.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.translate.DictCacheService;
import dev.xuya.core.translate.TranslateExecutor;
import com.xuya.plus.system.entity.SysDictData;
import com.xuya.plus.system.entity.SysDictType;
import com.xuya.plus.system.mapper.SysDictDataMapper;
import com.xuya.plus.system.mapper.SysDictTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典写服务：变更后立即全量刷新框架字典缓存 + 翻译缓存（缓存优先，不回源）
 */
@Service
public class DictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final DictCacheService dictCacheService;
    private final TranslateExecutor translateExecutor;

    public DictService(SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper,
                       DictCacheService dictCacheService, TranslateExecutor translateExecutor) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
        this.dictCacheService = dictCacheService;
        this.translateExecutor = translateExecutor;
    }

    @Transactional
    public void createType(SysDictType type) {
        checkTypeUnique(type.getDictType(), null);
        if (type.getStatus() == null) {
            type.setStatus(0);
        }
        dictTypeMapper.insert(type);
        refreshCache();
    }

    @Transactional
    public void updateType(SysDictType type) {
        if (type.getId() == null) {
            throw new QuickDevException("字典类型 ID 不能为空");
        }
        SysDictType exists = requireType(type.getId());
        checkTypeUnique(type.getDictType(), type.getId());
        type.setDelFlag(null);
        dictTypeMapper.updateById(type);
        // 类型编码变化：级联同步字典数据
        if (type.getDictType() != null && !type.getDictType().equals(exists.getDictType())) {
            dictDataMapper.update(null, new LambdaUpdateWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, exists.getDictType())
                    .set(SysDictData::getDictType, type.getDictType()));
        }
        refreshCache();
    }

    @Transactional
    public void deleteTypes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new QuickDevException("请选择要删除的字典类型");
        }
        for (Long id : ids) {
            SysDictType type = requireType(id);
            Long used = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, type.getDictType()));
            if (used != null && used > 0) {
                throw new QuickDevException("字典[" + type.getDictName() + "]下存在数据，不允许删除");
            }
        }
        dictTypeMapper.deleteBatchIds(ids);
        refreshCache();
    }

    /**
     * 前端下拉框：某类型的启用字典项
     */
    public List<SysDictData> listByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getDictSort));
    }

    /**
     * 立即刷新字典缓存并清空翻译结果缓存
     */
    public void refreshCache() {
        dictCacheService.refresh();
        translateExecutor.clearCache();
    }

    private void checkTypeUnique(String dictType, Long excludeId) {
        SysDictType exists = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType));
        if (exists != null && !exists.getId().equals(excludeId)) {
            throw new QuickDevException("字典类型已存在: " + dictType);
        }
    }

    private SysDictType requireType(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new QuickDevException("字典类型不存在或已被删除");
        }
        return type;
    }
}
