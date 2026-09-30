package com.xuya.plus.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import com.xuya.plus.system.entity.SysLogininfor;
import com.xuya.plus.system.mapper.SysLogininforMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志：分页/列表/详情/删除由 @QuickCrud 生成；清空走独立接口。
 */
@RestController
@RequestMapping("/sys-logininfor")
@QuickCrud(entity = SysLogininfor.class, permission = "sys:logininfor",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.DETAIL, CrudOp.REMOVE})
public class SysLogininforController {

    private final SysLogininforMapper logininforMapper;

    public SysLogininforController(SysLogininforMapper logininforMapper) {
        this.logininforMapper = logininforMapper;
    }

    @RequiresPerm("sys:logininfor:clean")
    @QuickLog(module = "登录日志", description = "清空登录日志")
    @PostMapping("/clean")
    public R<Void> clean() {
        logininforMapper.delete(new LambdaQueryWrapper<>());
        return R.ok();
    }
}
