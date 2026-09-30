package com.xuya.plus.framework.translate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.translate.DictLoader;
import com.xuya.plus.system.entity.SysDictData;
import com.xuya.plus.system.mapper.SysDictDataMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 字典数据源：sys_dict_data 全量提供给框架 DictCacheService 驻留内存。
 * 框架负责懒加载/定时刷新；字典管理接口在变更后会主动调用 refresh()。
 */
@Component
public class DbDictLoader implements DictLoader {

    private final SysDictDataMapper dictDataMapper;

    public DbDictLoader(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    @Override
    public List<DictEntry> loadAll() {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getStatus, 0)
                        .orderByAsc(SysDictData::getDictType, SysDictData::getDictSort))
                .stream()
                .map(d -> new DictEntry(d.getDictType(), d.getDictValue(), d.getDictLabel()))
                .toList();
    }
}
