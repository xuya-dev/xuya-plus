package com.xuya.plus.system.controller;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.NoRepeatSubmit;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import com.xuya.plus.system.entity.SysDictType;
import com.xuya.plus.system.service.DictService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 字典类型管理：查询由 @QuickCrud 生成，写接口变更后立即刷新字典/翻译缓存。
 */
@RestController
@RequestMapping("/sys-dict-type")
@QuickCrud(entity = SysDictType.class, permission = "sys:dict",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.DETAIL})
public class SysDictTypeController {

    private final DictService dictService;

    public SysDictTypeController(DictService dictService) {
        this.dictService = dictService;
    }

    @NoRepeatSubmit(interval = 2000)
    @RequiresPerm("sys:dict:add")
    @QuickLog(module = "字典管理", description = "新增字典类型")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysDictType type) {
        dictService.createType(type);
        return R.ok();
    }

    @RequiresPerm("sys:dict:edit")
    @QuickLog(module = "字典管理", description = "修改字典类型")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysDictType type) {
        dictService.updateType(type);
        return R.ok();
    }

    @RequiresPerm("sys:dict:remove")
    @QuickLog(module = "字典管理", description = "删除字典类型")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        try {
            dictService.deleteTypes(java.util.Arrays.stream(ids.split(","))
                    .map(String::trim).map(Long::valueOf).toList());
        } catch (NumberFormatException e) {
            throw new QuickDevException("非法的 ID 列表: " + ids);
        }
        return R.ok();
    }
}
