package dev.xuya.web.controller.auth;

import dev.xuya.common.redis.annotation.RateLimiter;
import dev.xuya.common.redis.enumeration.LimitType;
import dev.xuya.core.auth.RequiresLogin;
import dev.xuya.core.auth.NoRepeatSubmit;
import dev.xuya.core.common.R;
import dev.xuya.web.domain.LoginBody;
import dev.xuya.web.domain.RegisterBody;
import dev.xuya.system.domain.vo.RouterVo;
import dev.xuya.web.service.AuthService;
import dev.xuya.web.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 认证接口（登录/登出/当前用户/动态路由）。
 * <p>登录与验证码开放访问（无鉴权注解即放行），其余要求登录。</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    /**
     * 获取图形验证码（base64 data-uri，可直接放 &lt;img src&gt;）
     */
    @GetMapping("/captcha")
    public R<Map<String, String>> captcha() {
        if (!authService.captchaEnabled()) {
            return R.ok("验证码已关闭", null);
        }
        return R.ok(captchaService.create());
    }

    /**
     * 登录：成功返回 tokenName/tokenValue/expireIn，后续请求放到 Authorization 头
     */
    @RateLimiter(time = 60, count = 30, limitType = LimitType.IP,
            message = "登录尝试过于频繁，请稍后再试")
    @NoRepeatSubmit(interval = 2000)
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginBody body, HttpServletRequest request) {
        return R.ok(authService.login(body, request));
    }

    /**
     * 注册新用户（sys.account.registerUser=true 时开放；默认绑定普通角色）
     */
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP,
            message = "注册尝试过于频繁，请稍后再试")
    @PostMapping("/register")
    public R<String> register(@RequestBody RegisterBody body) {
        authService.register(body);
        return R.ok("注册成功");
    }

    /**
     * 登出当前会话
     */
    @RequiresLogin
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    /**
     * 当前用户信息：user + roles + permissions（超管 permissions=[*:*:*]）
     */
    @RequiresLogin
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        return R.ok(authService.getInfo());
    }

    /**
     * 前端动态路由（按角色授权过滤，超管返回全部目录/菜单）
     */
    @RequiresLogin
    @GetMapping("/routers")
    public R<List<RouterVo>> routers() {
        return R.ok(authService.getRouters());
    }
}
