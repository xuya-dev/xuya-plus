package dev.xuya.common.redis.aspect;

import cn.dev33.satoken.stp.StpUtil;
import dev.xuya.common.redis.annotation.RateLimiter;
import dev.xuya.common.redis.enumeration.LimitType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.List;

/**
 * 接口限流切面：Redis INCR + EXPIRE 原子计数，集群生效；
 * Redis 异常时放行（fail-open）并记录 WARN。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

    private static final String KEY_PREFIX = "xuya:rate:";
    /** 首次计数即设置 TTL 的 Lua 脚本（原子） */
    private static final DefaultRedisScript<Long> INCR_SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('incr', KEYS[1])\n"
            + "if c == 1 then redis.call('expire', KEYS[1], ARGV[1]) end\n"
            + "return c", Long.class);

    private final StringRedisTemplate redisTemplate;

    @Before("@annotation(limiter)")
    public void check(JoinPoint joinPoint, RateLimiter limiter) {
        try {
            String key = KEY_PREFIX + limiter.limitType() + ":" + joinPoint.getSignature().toShortString();
            if (limiter.limitType() == LimitType.IP || limiter.limitType() == LimitType.USER) {
                key += ":" + identity(limiter.limitType());
            }
            Long count = redisTemplate.execute(
                    INCR_SCRIPT, List.of(key), String.valueOf(limiter.time()));
            if (count != null && count > limiter.count()) {
                throw new dev.xuya.core.common.ParamException(limiter.message());
            }
        } catch (dev.xuya.core.common.ParamException e) {
            throw e;
        } catch (Exception e) {
            log.warn("限流计数异常（放行）: {}", e.getMessage());
        }
    }

    private String identity(LimitType limitType) {
        if (limitType == LimitType.USER) {
            try {
                Object loginId = cn.dev33.satoken.stp.StpUtil.getLoginIdDefaultNull();
                if (loginId != null) {
                    return "u:" + loginId;
                }
            } catch (Exception ignored) {
            }
        }
        return "ip:" + currentIp();
    }

    private String currentIp() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getRemoteAddr();
        }
        return "unknown";
    }
}
