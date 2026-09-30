package com.xuya.plus.auth.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import dev.xuya.core.common.QuickDevException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图形验证码：hutool 生成 + Redis 存储校验值（2 分钟有效，校验后即焚）
 */
@Service
public class CaptchaService {

    private static final String KEY_PREFIX = "xuya:captcha:";
    private static final Duration TTL = Duration.ofMinutes(2);

    private final StringRedisTemplate redisTemplate;

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, String> create() {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(130, 48, 4, 20);
        String uuid = IdUtil.fastSimpleUUID();
        redisTemplate.opsForValue().set(KEY_PREFIX + uuid, captcha.getCode(), TTL);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("uuid", uuid);
        result.put("img", captcha.getImageBase64Data());
        return result;
    }

    public void verify(String uuid, String code) {
        if (uuid == null || uuid.isBlank()) {
            throw new QuickDevException("验证码标识不能为空");
        }
        if (code == null || code.isBlank()) {
            throw new QuickDevException("验证码不能为空");
        }
        String key = KEY_PREFIX + uuid;
        String cached = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        if (cached == null) {
            throw new QuickDevException("验证码已过期");
        }
        if (!cached.equalsIgnoreCase(code)) {
            throw new QuickDevException("验证码错误");
        }
    }
}
