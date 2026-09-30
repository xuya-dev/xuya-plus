package dev.xuya.common.ratelimiter.enumeration;

/**
 * 限流维度
 */
public enum LimitType {

    /** 按方法（全局共享一个计数器） */
    DEFAULT,

    /** 按调用方 IP */
    IP,

    /** 按当前登录用户（匿名退化为 IP） */
    USER
}
