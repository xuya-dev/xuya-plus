package com.xuya.plus.system.controller;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.NoRepeatSubmit;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import com.xuya.plus.system.entity.SysDept;
import com.xuya.plus.system.service.DeptService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 部门管理：树/列表/详情由 @QuickCrud 生成，写接口维护 ancestors 祖级路径。
 */
@RestController
@RequestMapping("/sys-dept")
@QuickCrud(entity = SysDept.class, permission = "sys:dept",
        includes = {CrudOp.TREE, CrudOp.LIST, CrudOp.DETAIL})
public class SysDeptController {

    private final DeptService deptService;

    public SysDeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @NoRepeatSubmit(interval = 2000)
    @RequiresPerm("sys:dept:add")
    @QuickLog(module = "部门管理", description = "新增部门")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysDept dept) {
        deptService.createDept(dept);
        return R.ok();
    }

    @RequiresPerm("sys:dept:edit")
    @QuickLog(module = "部门管理", description = "修改部门")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysDept dept) {
        deptService.updateDept(dept);
        return R.ok();
    }

    @RequiresPerm("sys:dept:remove")
    @QuickLog(module = "部门管理", description = "删除部门")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        try {
            for (Long id : Arrays.stream(ids.split(",")).map(String::trim).map(Long::valueOf).toList()) {
                deptService.deleteDept(id);
            }
        } catch (NumberFormatException e) {
            throw new QuickDevException("非法的 ID 列表: " + ids);
        }
        return R.ok();
    }
}
