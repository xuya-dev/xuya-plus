package dev.xuya.common.redis;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.xuya.core.common.QuickDevException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 在线用户（Redis 注册表）：登录时写入 xuya:online:{token}，TTL 与会话一致；
 * 列表/强退均零查库。
 */
@Service
public class OnlineUserService {

    private static final String KEY_PREFIX = "xuya:online:";

    private final StringRedisTemplate redisTemplate;

    public OnlineUserService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void record(String token, Long userId, String username, String nickname,
                       Long deptId, String ip) {
        JSONObject json = new JSONObject();
        json.set("token", token);
        json.set("userId", userId);
        json.set("username", username);
        json.set("nickname", nickname);
        json.set("deptId", deptId);
        json.set("ip", ip);
        json.set("loginTime", LocalDateTime.now().toString());
        long timeout = Math.max(1, StpUtil.getTokenTimeout());
        redisTemplate.opsForValue().set(KEY_PREFIX + token, json.toString(),
                Duration.ofSeconds(timeout));
    }

    public List<JSONObject> list() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        List<JSONObject> result = new ArrayList<>();
        if (keys == null) {
            return result;
        }
        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                result.add(JSONUtil.parseObj(value));
            }
        }
        result.sort(Comparator.comparing(o -> o.getStr("loginTime", ""),
                Comparator.reverseOrder()));
        return result;
    }

    public int count() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        return keys == null ? 0 : keys.size();
    }

    public void kick(String token) {
        Boolean removed = redisTemplate.delete(KEY_PREFIX + token);
        StpUtil.logoutByTokenValue(token);
        if (!Boolean.TRUE.equals(removed)) {
            throw new QuickDevException("会话不存在或已过期");
        }
    }

    public void remove(String token) {
        if (token != null && !token.isBlank()) {
            redisTemplate.delete(KEY_PREFIX + token);
        }
    }
}
