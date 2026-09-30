import { request } from '../request';

/** 登录 */
export function fetchLogin(userName: string, password: string) {
  return request<Api.Auth.LoginToken>({
    url: '/auth/login',
    method: 'post',
    data: { username: userName, password }
  });
}

/** 获取当前用户信息 */
export function fetchGetUserInfo() {
  return request<Api.Auth.UserInfo>({ url: '/auth/info' });
}

/** 登出 */
export function fetchLogout() {
  return request({ url: '/auth/logout', method: 'post' });
}

/** 获取验证码 */
export function fetchCaptcha() {
  return request<{ uuid: string; img: string }>({ url: '/auth/captcha' });
}
