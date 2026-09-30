package com.xuya.plus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * XuYa Plus 启动入口。
 *
 * <p>绝大多数管理端 CRUD 接口由 Quick-Dev 的 @QuickCrud 在启动期动态注册；
 * 本工程只写：认证（/auth）、聚合写接口（用户/角色/菜单/部门/字典/参数）、
 * 监控（在线用户/缓存管理）与各类框架 SPI 实现（权限数据源、字典加载器、
 * 操作日志落地、数据权限）。</p>
 */
@EnableScheduling
@SpringBootApplication
@MapperScan("com.xuya.plus.**.mapper")
public class XuyaPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuyaPlusApplication.class, args);
    }
}
