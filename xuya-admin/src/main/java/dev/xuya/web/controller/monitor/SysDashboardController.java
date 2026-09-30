package dev.xuya.web.controller.monitor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.common.redis.OnlineUserService;
import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import dev.xuya.system.domain.SysJob;
import dev.xuya.system.domain.SysOss;
import dev.xuya.system.domain.SysUser;
import dev.xuya.system.mapper.SysJobMapper;
import dev.xuya.system.mapper.SysOssMapper;
import dev.xuya.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首页看板：用户/在线/文件/定时任务/磁盘 用量统计
 */
@RestController
@RequestMapping("/sys-dashboard")
@RequiredArgsConstructor
public class SysDashboardController {

    private final SysUserMapper userMapper;
    private final SysOssMapper ossMapper;
    private final SysJobMapper jobMapper;
    private final OnlineUserService onlineUserService;

    @RequiresPerm("sys:dashboard")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("userTotal", userMapper.selectCount(null));
        result.put("onlineTotal", onlineUserService.count());
        result.put("ossTotal", ossMapper.selectCount(null));
        result.put("jobTotal", jobMapper.selectCount(
                new LambdaQueryWrapper<SysJob>().eq(SysJob::getStatus, 0)));

        // 磁盘
        File root = new File(".");
        long total = root.getTotalSpace();
        long free = root.getFreeSpace();
        Map<String, Object> disk = new LinkedHashMap<>();
        disk.put("totalGB", Math.round(total / 1073741824.0 * 100) / 100.0);
        disk.put("freeGB", Math.round(free / 1073741824.0 * 100) / 100.0);
        result.put("disk", disk);

        // 今日
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        result.put("ossToday", ossMapper.selectCount(
                new LambdaQueryWrapper<SysOss>().ge(SysOss::getCreateTime, todayStart)));

        return R.ok(result);
    }
}
