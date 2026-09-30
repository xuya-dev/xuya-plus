package dev.xuya.web.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定时任务示例目标 Bean：invokeTarget 写法 JobDemoTask.sample() / JobDemoTask.work('参数')
 */
@Slf4j
@Component("jobDemoTask")
public class JobDemoTask {

    public void sample() {
        log.info("[定时任务示例] 系统信息采样完成：time={}", java.time.LocalDateTime.now());
    }

    public void work(String keyword) {
        log.info("[定时任务示例] 处理关键字: {}", keyword);
    }
}
