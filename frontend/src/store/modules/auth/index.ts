import { computed, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { defineStore } from 'pinia';
import { useLoading } from '@sa/hooks';
import { fetchGetUserInfo, fetchLogin } from '@/service/api';
import { useRouterPush } from '@/hooks/common/router';
import { localStg } from '@/utils/storage';
import { SetupStoreId } from '@/enum';
import { $t } from '@/locales';
import { useRouteStore } from '../route';
import { useTabStore } from '../tab';
import { clearAuthStorage, getToken } from './shared';

export const useAuthStore = defineStore(SetupStoreId.Auth, () => {
  const route = useRoute();
  const authStore = useAuthStore();
  const routeStore = useRouteStore();
  const tabStore = useTabStore();
  const { toLogin, redirectFromLogin } = useRouterPush(false);
  const { loading: loginLoading, startLoading, endLoading } = useLoading();

  const token = ref('');

  const userInfo: Api.Auth.UserInfo = reactive({
    userId: '',
    userName: '',
    roles: [],
    buttons: []
  });

  const isStaticSuper = computed(() => {
    const { VITE_AUTH_ROUTE_MODE, VITE_STATIC_SUPER_ROLE } = import.meta.env;
    return VITE_AUTH_ROUTE_MODE === 'static' && userInfo.roles.includes(VITE_STATIC_SUPER_ROLE);
  });

  const isLogin = computed(() => Boolean(token.value));

  async function resetStore() {
    recordUserId();
    clearAuthStorage();
    authStore.$reset();
    if (!route.meta.constant) {
      await toLogin();
    }
    tabStore.cacheTabs();
    routeStore.resetStore();
  }

  function recordUserId() {
    if (!userInfo.userId) {
      return;
    }
    localStg.set('lastLoginUserId', userInfo.userId);
  }

  function checkTabClear(): boolean {
    if (!userInfo.userId) {
      return false;
    }
    const lastLoginUserId = localStg.get('lastLoginUserId');
    if (!lastLoginUserId || lastLoginUserId !== userInfo.userId) {
      localStg.remove('globalTabs');
      tabStore.clearTabs();
      localStg.remove('lastLoginUserId');
      return true;
    }
    localStg.remove('lastLoginUserId');
    return false;
  }

  async function login(userName: string, password: string, redirect = true) {
    startLoading();
    const { data: loginToken, error } = await fetchLogin(userName, password);
    if (!error) {
      const pass = await loginByToken(loginToken);
      if (pass) {
        const isClear = checkTabClear();
        let needRedirect = redirect;
        if (isClear) {
          needRedirect = false;
        }
        await redirectFromLogin(needRedirect);
        window.$notification?.success({
          title: $t('page.login.common.loginSuccess'),
          content: $t('page.login.common.welcomeBack', { userName: userInfo.userName }),
          duration: 4500
        });
      }
    } else {
      resetStore();
    }
    endLoading();
  }

  async function loginByToken(loginToken: Api.Auth.LoginToken) {
    localStg.set('token', loginToken.tokenValue);
    const pass = await getUserInfo();
    if (pass) {
      token.value = loginToken.tokenValue;
      return true;
    }
    return false;
  }

  async function getUserInfo() {
    const { data: info, error } = await fetchGetUserInfo();
    if (!error && info) {
      // xuya-plus 返回格式: { user: {...}, roles: [...], permissions: [...] }
      const data = info as any;
      const user = data.user || {};
      userInfo.userId = String(user.id || '');
      userInfo.userName = user.nickname || user.username || '';
      userInfo.roles = data.roles || [];
      userInfo.buttons = data.permissions || [];
      return true;
    }
    return false;
  }

  async function initUserInfo() {
    const maybeToken = getToken();
    if (maybeToken) {
      token.value = maybeToken;
      const pass = await getUserInfo();
      if (!pass) {
        resetStore();
      }
    }
  }

  return {
    token,
    userInfo,
    isStaticSuper,
    isLogin,
    loginLoading,
    resetStore,
    login,
    initUserInfo
  };
});
