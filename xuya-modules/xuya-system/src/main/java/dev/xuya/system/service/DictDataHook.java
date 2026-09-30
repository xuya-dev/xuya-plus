package dev.xuya.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.crud.CrudHook;
import dev.xuya.system.domain.SysDictData;
import dev.xuya.system.domain.SysDictType;
import dev.xuya.system.mapper.SysDictTypeMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 字典数据写流程钩子（CrudHook 示例）：
 * 让 @QuickCrud 生成的字典数据写接口具备"类型校验 + 缓存刷新"的聚合逻辑，
 * 无需为字典数据手写 Controller 写接口。
 *
 * <ul>
 *   <li>beforeSave/beforeUpdate：事务内校验 dictType 必须存在，异常回滚写操作</li>
 *   <li>afterSave/afterUpdate/afterRemove：提交后刷新字典内存缓存（失败仅告警，
 *       定时刷新兜底），前端立即可见新字典</li>
 * </ul>
 */
@Component
public class DictDataHook implements CrudHook {

    private final SysDictTypeMapper dictTypeMapper;
    private final DictService dictService;

    public DictDataHook(SysDictTypeMapper dictTypeMapper, DictService dictService) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictService = dictService;
    }

    @Override
    public Class<?> entityType() {
        return SysDictData.class;
    }

    @Override
    public void beforeSave(Object entity) {
        requireType(((SysDictData) entity).getDictType());
    }

    @Override
    public void beforeUpdate(Object entity) {
        String dictType = ((SysDictData) entity).getDictType();
        if (dictType != null) {
            requireType(dictType);
        }
    }

    @Override
    public void afterSave(Object entity) {
        dictService.refreshCache();
    }

    @Override
    public void afterUpdate(Object entity) {
        dictService.refreshCache();
    }

    @Override
    public void afterRemove(List<Object> ids) {
        dictService.refreshCache();
    }

    private void requireType(String dictType) {
        Long count = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType));
        if (count == null || count == 0) {
            throw new QuickDevException("字典类型不存在: " + dictType);
        }
    }
}
