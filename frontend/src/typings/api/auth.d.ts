declare namespace Api {
  namespace Auth {
    /** xuya-plus 登录响应 */
    interface LoginToken {
      tokenName: string;
      tokenValue: string;
      expireIn: number;
    }

    interface UserInfo {
      userId: string;
      userName: string;
      roles: string[];
      buttons: string[];
    }
  }
}
