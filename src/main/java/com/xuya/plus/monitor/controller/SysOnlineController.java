package com.xuya.plus.monitor.controller;

import cn.hutool.json.JSONObject;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import com.xuya.plus.framework.monitor.OnlineUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 在线用户：Redis 会话注册表，支持查询与强退。
 */
@RestController
@RequestMapping("/sys-online")
public class SysOnlineController {

    private final OnlineUserService onlineUserService;

    public SysOnlineController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @RequiresPerm("sys:online:list")
    @GetMapping("/list")
    public R<List<JSONObject>> list() {
        return R.ok(onlineUserService.list());
    }

    @RequiresPerm("sys:online:kick")
    @QuickLog(module = "在线用户", description = "强制下线")
    @DeleteMapping("/{token}")
    public R<Void> kick(@PathVariable String token) {
        onlineUserService.kick(token);
        return R.ok();
    }
}
