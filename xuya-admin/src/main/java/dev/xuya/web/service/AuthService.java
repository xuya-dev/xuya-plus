package dev.xuya.web.service;

import cn.dev33.satoken.stp.StpUtil;
import dev.xuya.core.auth.AuthContext;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.xuya.core.common.QuickDevException;
import dev.xuya.web.domain.LoginBody;
import dev.xuya.system.domain.vo.RouterVo;
import dev.xuya.system.framework.log.LoginLogService;
import dev.xuya.common.redis.OnlineUserService;
import dev.xuya.common.util.IpUtil;
import dev.xuya.system.framework.security.ConfigCacheService;
import dev.xuya.system.framework.security.RbacCacheService;
import dev.xuya.system.domain.SysUser;
import dev.xuya.system.mapper.SysUserMapper;
import dev.xuya.system.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 认证服务：验证码校验 -> BCrypt 密码校验 -> Sa-Token 登录 ->
 * 登录日志 + 在线用户注册。权限/角色数据由 RbacCacheService 提供。
 */
@Service
public class AuthService {

    public static final String CONFIG_CAPTCHA_ENABLED = "sys.account.captchaEnabled";

    private final SysUserMapper userMapper;
    private final CaptchaService captchaService;
    private final ConfigCacheService configCacheService;
    private final RbacCacheService rbacCacheService;
    private final LoginLogService loginLogService;
    private final OnlineUserService onlineUserService;
    private final MenuService menuService;

    public AuthService(SysUserMapper userMapper, CaptchaService captchaService,
                       ConfigCacheService configCacheService, RbacCacheService rbacCacheService,
                       LoginLogService loginLogService, OnlineUserService onlineUserService,
                       MenuService menuService) {
        this.userMapper = userMapper;
        this.captchaService = captchaService;
        this.configCacheService = configCacheService;
        this.rbacCacheService = rbacCacheService;
        this.loginLogService = loginLogService;
        this.onlineUserService = onlineUserService;
        this.menuService = menuService;
    }

    public boolean captchaEnabled() {
        return "true".equalsIgnoreCase(configCacheService.getValue(CONFIG_CAPTCHA_ENABLED, "true"));
    }

    public Map<String, Object> login(LoginBody body, HttpServletRequest request) {
        String username = body.getUsername().trim();
        String ip = IpUtil.getClientIp(request);
        if (captchaEnabled()) {
            captchaService.verify(body.getUuid(), body.getCode());
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null || !BCrypt.checkpw(body.getPassword(), user.getPassword())) {
            loginLogService.record(username, ip, "用户名或密码错误", false);
            throw new QuickDevException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 0) {
            loginLogService.record(username, ip, "账号已停用", false);
            throw new QuickDevException("账号已停用，请联系管理员");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        // 记录最后登录信息（只更新登录字段，避免覆盖并发修改）
        SysUser patch = new SysUser();
        patch.setId(user.getId());
        patch.setLoginIp(ip);
        patch.setLoginDate(LocalDateTime.now());
        userMapper.updateById(patch);

        onlineUserService.record(token, user.getId(), user.getUsername(), user.getNickname(),
                user.getDeptId(), ip);
        loginLogService.record(username, ip, "登录成功", true);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tokenName", StpUtil.getTokenName());
        result.put("tokenValue", token);
        result.put("expireIn", StpUtil.getTokenTimeout());
        return result;
    }

    public void logout() {
        String token = AuthContext.getToken();
        onlineUserService.remove(token);
        StpUtil.logout();
    }

    public Map<String, Object> getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new QuickDevException("用户不存在或已被删除");
        }
        Set<String> roles = rbacCacheService.getRoleKeys(userId);
        Set<String> permissions = rbacCacheService.getPerms(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("roles", roles);
        result.put("permissions", permissions);
        return result;
    }

    public List<RouterVo> getRouters() {
        return menuService.buildRouters(rbacCacheService.getUserMenus(StpUtil.getLoginIdAsLong()));
    }
}
