package com.xuya.plus.system.controller;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.NoRepeatSubmit;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import com.xuya.plus.system.entity.SysMenu;
import com.xuya.plus.system.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单管理：树/列表/详情由 @QuickCrud 生成，写接口变更后自动刷新 RBAC 缓存。
 */
@RestController
@RequestMapping("/sys-menu")
@QuickCrud(entity = SysMenu.class, permission = "sys:menu",
        includes = {CrudOp.TREE, CrudOp.LIST, CrudOp.DETAIL})
public class SysMenuController {

    private final MenuService menuService;

    public SysMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @NoRepeatSubmit(interval = 2000)
    @RequiresPerm("sys:menu:add")
    @QuickLog(module = "菜单管理", description = "新增菜单")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysMenu menu) {
        menuService.createMenu(menu);
        return R.ok();
    }

    @RequiresPerm("sys:menu:edit")
    @QuickLog(module = "菜单管理", description = "修改菜单")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return R.ok();
    }

    @RequiresPerm("sys:menu:remove")
    @QuickLog(module = "菜单管理", description = "删除菜单")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
