package dev.xuya.web.controller.monitor;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import dev.xuya.system.domain.SysJobLog;
import dev.xuya.system.mapper.SysJobLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务调度日志：分页/列表/详情/删除由 @QuickCrud 生成
 */
@RestController
@RequiredArgsConstructor
@QuickCrud(entity = SysJobLog.class, permission = "sys:job-log",
        includes = {CrudOp.PAGE, CrudOp.LIST, CrudOp.DETAIL, CrudOp.REMOVE})
public class SysJobLogController {

    private final SysJobLogMapper jobLogMapper;

    @RequiresPerm("sys:job-log:remove")
    @QuickLog(module = "任务调度", description = "清空调度日志")
    @DeleteMapping("/sys-job-log/clean")
    public R<Void> clean() {
        jobLogMapper.delete(null);
        return R.ok();
    }
}
