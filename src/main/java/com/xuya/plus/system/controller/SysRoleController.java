package com.xuya.plus.system.controller;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.NoRepeatSubmit;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import com.xuya.plus.system.entity.SysRole;
import com.xuya.plus.system.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 角色管理：查询/详情/导出由 @QuickCrud 生成，授权写（菜单/数据权限）走 RoleService。
 */
@RestController
@RequestMapping("/sys-role")
@QuickCrud(entity = SysRole.class, permission = "sys:role",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.COUNT, CrudOp.DETAIL, CrudOp.EXPORT})
public class SysRoleController {

    private final RoleService roleService;

    public SysRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @NoRepeatSubmit(interval = 2000)
    @RequiresPerm("sys:role:add")
    @QuickLog(module = "角色管理", description = "新增角色")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysRole role) {
        roleService.createRole(role);
        return R.ok();
    }

    @RequiresPerm("sys:role:edit")
    @QuickLog(module = "角色管理", description = "修改角色")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysRole role) {
        roleService.updateRole(role);
        return R.ok();
    }

    @RequiresPerm("sys:role:remove")
    @QuickLog(module = "角色管理", description = "删除角色")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        try {
            roleService.deleteRoles(Arrays.stream(ids.split(","))
                    .map(String::trim).map(Long::valueOf).toList());
        } catch (NumberFormatException e) {
            throw new QuickDevException("非法的 ID 列表: " + ids);
        }
        return R.ok();
    }

    @RequiresPerm("sys:role:edit")
    @QuickLog(module = "角色管理", description = "修改角色状态")
    @PutMapping("/{id}/status/{status}")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        roleService.changeStatus(id, status);
        return R.ok();
    }

    /**
     * 角色已授权菜单 ID（授权树回显）
     */
    @RequiresPerm("sys:role:detail")
    @GetMapping("/{id}/menu-ids")
    public R<List<Long>> menuIds(@PathVariable Long id) {
        return R.ok(roleService.getMenuIds(id));
    }

    /**
     * 角色自定义数据权限的部门 ID（部门树回显）
     */
    @RequiresPerm("sys:role:detail")
    @GetMapping("/{id}/dept-ids")
    public R<List<Long>> deptIds(@PathVariable Long id) {
        return R.ok(roleService.getDeptIds(id));
    }
}
