package com.xuya.plus.system.controller;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.RequiresLogin;
import dev.xuya.core.common.R;
import com.xuya.plus.system.entity.SysDictData;
import com.xuya.plus.system.service.DictService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典数据管理：全部 CRUD 由 @QuickCrud 生成（写接口的事务、类型校验与缓存刷新
 * 由 DictDataHook 提供）；此前的手写新增/修改/删除接口已由钩子方案取代。
 */
@RestController
@RequestMapping("/sys-dict-data")
@QuickCrud(entity = SysDictData.class, permission = "sys:dict",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.DETAIL, CrudOp.EXPORT,
                CrudOp.SAVE, CrudOp.UPDATE, CrudOp.REMOVE})
public class SysDictDataController {

    private final DictService dictService;

    public SysDictDataController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 前端下拉框：某字典类型的启用数据（仅要求登录）
     */
    @RequiresLogin
    @GetMapping("/type/{dictType}")
    public R<List<SysDictData>> listByType(@PathVariable String dictType) {
        return R.ok(dictService.listByType(dictType));
    }
}
