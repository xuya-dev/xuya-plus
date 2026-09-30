package dev.xuya.web.controller.system;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.NoRepeatSubmit;
import dev.xuya.core.auth.RequiresLogin;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import dev.xuya.system.domain.SysConfig;
import dev.xuya.system.service.ConfigService;
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
 * 参数配置管理：查询由 @QuickCrud 生成，写接口变更后立即刷新内存缓存。
 */
@RestController
@RequestMapping("/sys-config")
@QuickCrud(entity = SysConfig.class, permission = "sys:config",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.DETAIL})
public class SysConfigController {

    private final ConfigService configService;

    public SysConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @NoRepeatSubmit(interval = 2000)
    @RequiresPerm("sys:config:add")
    @QuickLog(module = "参数管理", description = "新增参数")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysConfig config) {
        configService.createConfig(config);
        return R.ok();
    }

    @RequiresPerm("sys:config:edit")
    @QuickLog(module = "参数管理", description = "修改参数")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysConfig config) {
        configService.updateConfig(config);
        return R.ok();
    }

    @RequiresPerm("sys:config:remove")
    @QuickLog(module = "参数管理", description = "删除参数")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        try {
            configService.deleteConfigs(Arrays.stream(ids.split(","))
                    .map(String::trim).map(Long::valueOf).toList());
        } catch (NumberFormatException e) {
            throw new QuickDevException("非法的 ID 列表: " + ids);
        }
        return R.ok();
    }

    /**
     * 按键名取参数值（业务侧零查库）
     */
    @RequiresLogin
    @GetMapping("/key/{configKey}")
    public R<String> value(@PathVariable String configKey) {
        return R.ok(configService.getValue(configKey));
    }
}
