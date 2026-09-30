package dev.xuya.web.controller.monitor;

import dev.xuya.core.annotation.CrudOp;
import dev.xuya.core.annotation.QuickCrud;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.core.common.R;
import dev.xuya.core.log.QuickLog;
import dev.xuya.system.domain.SysJob;
import dev.xuya.system.service.SysJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 任务调度：分页/列表/详情由 @QuickCrud 生成；新增/修改/删除/状态/执行一次走业务逻辑
 */
@RestController
@RequestMapping("/sys-job")
@RequiredArgsConstructor
@QuickCrud(entity = SysJob.class, permission = "sys:job",
        includes = {dev.xuya.core.annotation.CrudOp.PAGE, dev.xuya.core.annotation.CrudOp.LIST,
                dev.xuya.core.annotation.CrudOp.DETAIL})
public class SysJobController {

    private final SysJobService jobService;

    @RequiresPerm("sys:job:add")
    @QuickLog(module = "任务调度", description = "新增任务")
    @PostMapping
    public R<Void> add(SysJob job) {
        jobService.createJob(job);
        return R.ok();
    }

    @RequiresPerm("sys:job:edit")
    @QuickLog(module = "任务调度", description = "修改任务")
    @PutMapping
    public R<Void> edit(SysJob job) {
        jobService.updateJob(job);
        return R.ok();
    }

    @RequiresPerm("sys:job:remove")
    @QuickLog(module = "任务调度", description = "删除任务")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable String ids) {
        jobService.deleteJobs(Arrays.stream(ids.split(",")).map(String::trim)
                .map(Long::valueOf).toList());
        return R.ok();
    }

    @RequiresPerm("sys:job:edit")
    @QuickLog(module = "任务调度", description = "修改任务状态")
    @PutMapping("/changeStatus/{id}/{status}")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        jobService.changeStatus(id, status);
        return R.ok();
    }

    @RequiresPerm("sys:job:run")
    @QuickLog(module = "任务调度", description = "执行一次任务")
    @PostMapping("/run/{id}")
    public R<Void> run(@PathVariable Long id) {
        jobService.runOnce(id);
        return R.ok();
    }
}
