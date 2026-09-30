package dev.xuya.system.service;

import dev.xuya.core.context.SpringContextHolder;
import dev.xuya.system.domain.SysJob;
import dev.xuya.system.domain.SysJobLog;
import dev.xuya.system.mapper.SysJobLogMapper;
import dev.xuya.system.mapper.SysJobMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时任务调度服务：DB 定义 cron 任务（invokeTarget 反射调用 Spring Bean 方法），
 * 启动加载 + 暂停/恢复/执行一次 + 每次执行落 sys_job_log。
 *
 * <p>invokeTarget 约定：beanName.method('字符串', 123, 1.5, true, null)——
 * 参数按字面量推断类型；目标 Bean 必须是 Spring 容器组件。</p>
 */
@Slf4j
@Service
public class SysJobServiceImpl implements SysJobService {

    private final SysJobMapper jobMapper;
    private final SysJobLogMapper jobLogMapper;
    private final ThreadPoolTaskScheduler scheduler;
    private final Map<Long, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    public SysJobServiceImpl(SysJobMapper jobMapper, SysJobLogMapper jobLogMapper) {
        this.jobMapper = jobMapper;
        this.jobLogMapper = jobLogMapper;
        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.setPoolSize(5);
        this.scheduler.setThreadNamePrefix("xuya-job-");
        this.scheduler.initialize();
        for (SysJob job : jobMapper.selectList(null)) {
            if (job.getStatus() != null && job.getStatus() == 0) {
                doSchedule(job);
            }
        }
        log.info("定时任务调度器已启动，加载启用任务 {} 个", runningFlags.size());
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    @Transactional
    public void createJob(SysJob job) {
        validateTarget(job.getInvokeTarget());
        validateCron(job.getCronExpression());
        if (job.getStatus() == null) {
            job.setStatus(0);
        }
        if (job.getConcurrent() == null) {
            job.setConcurrent(1);
        }
        jobMapper.insert(job);
        if (job.getStatus() == 0) {
            doSchedule(job);
        }
    }

    @Override
    @Transactional
    public void updateJob(SysJob job) {
        validateTarget(job.getInvokeTarget());
        validateCron(job.getCronExpression());
        cancel(job.getId());
        jobMapper.updateById(job);
        SysJob latest = jobMapper.selectById(job.getId());
        if (latest.getStatus() != null && latest.getStatus() == 0) {
            doSchedule(latest);
        }
    }

    @Override
    @Transactional
    public void deleteJobs(List<Long> ids) {
        for (Long id : ids) {
            cancel(id);
        }
        jobMapper.deleteBatchIds(ids);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysJob job = requireJob(id);
        SysJob patch = new SysJob();
        patch.setId(id);
        patch.setStatus(status);
        jobMapper.updateById(patch);
        cancel(id);
        if (status != null && status == 0) {
            doSchedule(jobMapper.selectById(id));
        }
    }

    @Override
    public void runOnce(Long id) {
        SysJob job = requireJob(id);
        scheduler.execute(() -> executeWithLog(job));
    }

    private void doSchedule(SysJob job) {
        if (job.getId() == null || job.getCronExpression() == null) {
            return;
        }
        try {
            CronExpression.parse(job.getCronExpression());
        } catch (IllegalArgumentException e) {
            log.warn("任务[{}] cron表达式不合法，跳过调度", job.getJobName());
            return;
        }
        runningFlags.computeIfAbsent(job.getId(), k -> new AtomicBoolean(false));
        scheduler.schedule(
                () -> executeWithLog(job),
                new CronTrigger(job.getCronExpression()));
        log.info("调度任务[{}]({}): {}", job.getJobName(), job.getId(), job.getCronExpression());
    }

    private void cancel(Long jobId) {
        runningFlags.remove(jobId);
    }

    private void executeWithLog(SysJob job) {
        AtomicBoolean flag = runningFlags.computeIfAbsent(job.getId(), k -> new AtomicBoolean(false));
        if (job.getConcurrent() != null && job.getConcurrent() == 1
                && !flag.compareAndSet(false, true)) {
            log.warn("任务[{}]上一次尚未结束，本次跳过（禁止并发）", job.getJobName());
            return;
        }
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobId(job.getId());
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        try {
            invoke(job.getInvokeTarget());
            jobLog.setStatus(0);
            jobLog.setJobMessage("执行成功");
        } catch (Exception e) {
            jobLog.setStatus(1);
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            jobLog.setJobMessage("执行失败");
            jobLog.setExceptionInfo(msg.length() > 1900 ? msg.substring(0, 1900) : msg);
            log.error("任务[{}]执行失败: {}", job.getJobName(), msg);
        } finally {
            flag.set(false);
            jobLog.setCreateTime(LocalDateTime.now());
            try {
                jobLogMapper.insert(jobLog);
            } catch (Exception e) {
                log.error("任务日志写入失败: {}", e.getMessage());
            }
        }
    }

    void invoke(String invokeTarget) {
        var pattern = java.util.regex.Pattern.compile("([\\w$]+)\\.([\\w$]+)\\((.*)\\)");
        var matcher = pattern.matcher(invokeTarget.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("调用目标格式非法: " + invokeTarget);
        }
        String beanName = matcher.group(1);
        String methodName = matcher.group(2);
        Object bean = SpringContextHolder.getContext().getBean(beanName);
        List<Object> args = new ArrayList<>();
        String csv = matcher.group(3).trim();
        if (!csv.isEmpty()) {
            for (String item : csv.split(",")) {
                args.add(convertLiteral(item.trim()));
            }
        }
        for (Method method : bean.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != args.size()) {
                continue;
            }
            Class<?>[] types = method.getParameterTypes();
            boolean match = true;
            for (int i = 0; i < types.length; i++) {
                if (args.get(i) != null && !types[i].isAssignableFrom(args.get(i).getClass())) {
                    match = false;
                    break;
                }
            }
            if (match) {
                try {
                    method.invoke(bean, args.toArray());
                    return;
                } catch (Exception e) {
                    throw new IllegalStateException("任务执行异常: " + e.getMessage(), e);
                }
            }
        }
        throw new IllegalArgumentException("调用目标方法不存在: " + invokeTarget);
    }

    private Object convertLiteral(String literal) {
        if ("null".equalsIgnoreCase(literal)) {
            return null;
        }
        if ((literal.startsWith("'") && literal.endsWith("'"))
                || (literal.startsWith("\"") && literal.endsWith("\""))) {
            return literal.substring(1, literal.length() - 1);
        }
        if ("true".equalsIgnoreCase(literal) || "false".equalsIgnoreCase(literal)) {
            return Boolean.parseBoolean(literal);
        }
        if (literal.matches("-?\\d+")) {
            return Long.parseLong(literal);
        }
        if (literal.matches("-?\\d+\\.\\d+")) {
            return Double.parseDouble(literal);
        }
        return literal;
    }

    private void validateTarget(String invokeTarget) {
        try {
            invoke(invokeTarget);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("调用目标执行失败: " + e.getMessage());
        }
    }

    private void validateCron(String cron) {
        try {
            CronExpression.parse(cron);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("cron表达式不合法: " + cron);
        }
    }

    private SysJob requireJob(Long id) {
        SysJob job = jobMapper.selectById(id);
        if (job == null) {
            throw new IllegalArgumentException("任务不存在或已被删除");
        }
        return job;
    }
}
