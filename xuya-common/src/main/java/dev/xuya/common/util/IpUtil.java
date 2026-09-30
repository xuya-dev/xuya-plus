package dev.xuya.common.util;

import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 提取（支持反向代理头）
 */
public final class IpUtil {

    private IpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = JakartaServletUtil.getClientIP(request, "X-Forwarded-For", "X-Real-IP");
        return ip == null || ip.isBlank() ? "unknown" : ip;
    }
}
