package dev.xuya.web.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求体
 */
@Data
public class RegisterBody {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 30, message = "用户名长度 2~30 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
