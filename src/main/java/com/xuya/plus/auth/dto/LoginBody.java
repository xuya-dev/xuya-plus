package com.xuya.plus.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求体
 */
@Data
public class LoginBody {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码（sys.account.captchaEnabled=true 时必填）
     */
    private String code;

    /**
     * 验证码唯一标识
     */
    private String uuid;
}
