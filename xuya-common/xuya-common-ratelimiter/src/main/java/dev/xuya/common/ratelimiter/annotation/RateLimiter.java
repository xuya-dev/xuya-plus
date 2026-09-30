package dev.xuya.common.ratelimiter.annotation;

import dev.xuya.common.ratelimiter.enumeration.LimitType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流：time 秒内最多 count 次，超出返回 400（"访问过于频繁，请稍后再试"）。
 * 基于 Redis 计数器实现，集群生效；Redis 不可用时放行（fail-open）。
 *
 * <pre>
 * &#64;RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
 * &#64;PostMapping("/send")
 * public R&lt;Void&gt; send(...) { ... }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /** 时间窗口（秒） */
    long time() default 60;

    /** 窗口内最大次数 */
    long count() default 100;

    /** 限流维度 */
    LimitType limitType() default LimitType.DEFAULT;

    /** 提示语 */
    String message() default "访问过于频繁，请稍后再试";
}
