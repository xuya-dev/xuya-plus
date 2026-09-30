package com.xuya.plus.auth.service;

import dev.xuya.core.auth.AuthContext;

/**
 * 取当前请求 token 的小工具（AuthContext 在鉴权拦截器中已填充）
 */
public final class AuthTokenHolder {

    private AuthTokenHolder() {
    }

    public static String currentToken() {
        return AuthContext.getToken();
    }
}
