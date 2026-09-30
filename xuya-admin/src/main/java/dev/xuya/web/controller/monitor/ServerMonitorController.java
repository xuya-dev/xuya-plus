package dev.xuya.web.controller.monitor;

import dev.xuya.core.auth.RequiresPerm;
import dev.xuya.core.common.R;
import lombok.RequiredArgsConstructor;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务监控：CPU / 内存 / JVM / 磁盘 / 系统信息（oshi）
 */
@RestController
@RequestMapping("/sys-server")
@RequiredArgsConstructor
public class ServerMonitorController {

    private final SystemInfo systemInfo = new SystemInfo();

    @RequiresPerm("sys:server:list")
    @GetMapping
    public R<Map<String, Object>> info() {
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();
        CentralProcessor processor = hardware.getProcessor();
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(ticks) * 100;

        var memory = hardware.getMemory();
        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("cpuNum", processor.getLogicalProcessorCount());
        cpu.put("total", 100.0);
        cpu.put("used", round(cpuLoad));
        cpu.put("free", round(100 - cpuLoad));

        long memTotal = memory.getTotal();
        long memUsed = memTotal - memory.getAvailable();
        Map<String, Object> mem = new LinkedHashMap<>();
        mem.put("total", toGB(memTotal));
        mem.put("used", toGB(memUsed));
        mem.put("free", toGB(memTotal - memUsed));
        mem.put("usageRate", round(memTotal == 0 ? 0 : memUsed * 100.0 / memTotal));

        var jvmMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("home", System.getProperty("java.home"));
        jvm.put("max", toGB(jvmMem.getMax()));
        jvm.put("total", toGB(jvmMem.getCommitted()));
        jvm.put("free", toGB(jvmMem.getCommitted() - jvmMem.getUsed()));
        jvm.put("usageRate", round(jvmMem.getCommitted() == 0 ? 0
                : jvmMem.getUsed() * 100.0 / jvmMem.getCommitted()));
        jvm.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());

        List<Map<String, Object>> disks = new ArrayList<>();
        for (File file : File.listRoots()) {
            if (file.getTotalSpace() == 0) {
                continue;
            }
            long total = file.getTotalSpace();
            long free = file.getFreeSpace();
            Map<String, Object> disk = new LinkedHashMap<>();
            disk.put("dirName", file.getPath());
            disk.put("sysTypeName", "local");
            disk.put("total", toGB(total));
            disk.put("free", toGB(free));
            disk.put("used", toGB(total - free));
            disk.put("usageRate", round(total == 0 ? 0 : (total - free) * 100.0 / total));
            disks.add(disk);
        }

        Map<String, Object> sys = new LinkedHashMap<>();
        sys.put("computerName", "server");
        sys.put("osName", os.getFamily() + " " + os.getVersionInfo().getVersion());
        sys.put("computerIp", localIp());
        sys.put("osArch", System.getProperty("os.arch"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cpu", cpu);
        result.put("mem", mem);
        result.put("jvm", jvm);
        result.put("sys", sys);
        result.put("disks", disks);
        return R.ok(result);
    }

    private double round(double v) {
        return Math.round(v * 100) / 100.0;
    }

    private double toGB(long bytes) {
        return Math.round(bytes / 1024.0 / 1024.0 / 1024.0 * 100) / 100.0;
    }

    private String localIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
