package com.xuya.plus.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import com.xuya.plus.system.entity.SysOperLog;
import com.xuya.plus.system.mapper.SysOperLogMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志：分页/列表/详情/删除由 @QuickCrud 生成；清空走独立接口。
 * 数据来源：框架 @QuickLog 切面 -> DbOperationLogSink 异步写入。
 */
@RestController
@RequestMapping("/sys-oper-log")
@QuickCrud(entity = SysOperLog.class, permission = "sys:operlog",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.DETAIL, CrudOp.REMOVE})
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    public SysOperLogController(SysOperLogMapper operLogMapper) {
        this.operLogMapper = operLogMapper;
    }

    @RequiresPerm("sys:operlog:clean")
    @QuickLog(module = "操作日志", description = "清空操作日志")
    @PostMapping("/clean")
    public R<Void> clean() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
        return R.ok();
    }
}
