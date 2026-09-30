package dev.xuya.web.controller.system;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.NoRepeatSubmit;
import dev.xuya.core.auth.RequiresLogin;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import cn.dev33.satoken.stp.StpUtil;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import dev.xuya.system.dto.SysUserForm;
import dev.xuya.system.domain.SysUser;
import dev.xuya.system.service.UserService;
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
import java.util.Map;

/**
 * 用户管理：查询/详情/导出由 @QuickCrud 生成，聚合写（角色/岗位绑定）走 UserService。
 */
@RestController
@RequestMapping("/sys-user")
@QuickCrud(entity = SysUser.class, permission = "sys:user",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.COUNT, CrudOp.DETAIL, CrudOp.EXPORT})
public class SysUserController {

    private final UserService userService;

    public SysUserController(UserService userService) {
        this.userService = userService;
    }

    @NoRepeatSubmit(interval = 2000)
    @RequiresPerm("sys:user:add")
    @QuickLog(module = "用户管理", description = "新增用户")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysUserForm form) {
        userService.createUser(form);
        return R.ok();
    }

    @RequiresPerm("sys:user:edit")
    @QuickLog(module = "用户管理", description = "修改用户")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysUserForm form) {
        userService.updateUser(form);
        return R.ok();
    }

    @RequiresPerm("sys:user:remove")
    @QuickLog(module = "用户管理", description = "删除用户")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        userService.deleteUsers(parseIds(ids));
        return R.ok();
    }

    @RequiresPerm("sys:user:edit")
    @QuickLog(module = "用户管理", description = "修改用户状态")
    @PutMapping("/{id}/status/{status}")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.changeStatus(id, status);
        return R.ok();
    }

    @RequiresPerm("sys:user:edit")
    @QuickLog(module = "用户管理", description = "重置用户密码")
    @PutMapping("/{id}/password")
    public R<Void> resetPwd(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.isBlank()) {
            throw new QuickDevException("新密码不能为空");
        }
        userService.resetPwd(id, password);
        return R.ok();
    }

    /**
     * 用户的角色/岗位 ID（编辑表单回显）
     */
    @RequiresPerm("sys:user:detail")
    @GetMapping("/{id}/role-ids")
    public R<Map<String, List<Long>>> roleAndPostIds(@PathVariable Long id) {
        return R.ok(userService.getRoleAndPostIds(id));
    }

    /**
     * 个人中心：修改自己的基础资料
     */
    @RequiresLogin
    @QuickLog(module = "个人中心", description = "修改资料")
    @PutMapping("/profile")
    public R<Void> profile(@RequestBody SysUserForm form) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, form);
        return R.ok();
    }

    /**
     * 个人中心：校验旧密码后修改自己的密码
     */
    @RequiresLogin
    @QuickLog(module = "个人中心", description = "修改密码")
    @PutMapping("/profile/password")
    public R<Void> profilePwd(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            throw new QuickDevException("新密码不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfilePwd(userId, oldPassword, newPassword);
        return R.ok();
    }

    private List<Long> parseIds(String ids) {
        try {
            return Arrays.stream(ids.split(",")).map(String::trim).map(Long::valueOf).toList();
        } catch (NumberFormatException e) {
            throw new QuickDevException("非法的 ID 列表: " + ids);
        }
    }
}
