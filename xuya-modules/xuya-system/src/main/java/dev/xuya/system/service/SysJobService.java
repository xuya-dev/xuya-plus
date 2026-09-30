package dev.xuya.system.service;

import dev.xuya.system.domain.SysJob;

import java.util.List;

/**
 * 定时任务服务接口
 *
 * @see SysJobServiceImpl
 */
public interface SysJobService {

    /**
     * 新增任务（校验 cron 与调用目标，状态正常时自动调度）
     */
    void createJob(SysJob job);

    /**
     * 修改任务（重新调度）
     */
    void updateJob(SysJob job);

    /**
     * 批量删除任务（同时取消调度）
     */
    void deleteJobs(List<Long> ids);

    /**
     * 暂停 / 恢复任务
     */
    void changeStatus(Long id, Integer status);

    /**
     * 立即执行一次（异步，日志照常记录）
     */
    void runOnce(Long id);
}
