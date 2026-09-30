import { localStg } from '@/utils/storage';
import type { RequestInstanceState } from './type';

/** 获取 Authorization 头（xuya-plus 后端直接用 token 值，无 Bearer 前缀） */
export function getAuthorization() {
  return localStg.get('token') || null;
}

/** token 过期处理：xuya-plus 无刷新机制，直接重置登录 */
export async function handleExpiredRequest(_state: RequestInstanceState) {
  const { useAuthStore } = await import('@/store/modules/auth');
  const authStore = useAuthStore();
  authStore.resetStore();
  return false;
}

export function showErrorMsg(state: RequestInstanceState, message: string) {
  if (!state.errMsgStack?.length) {
    state.errMsgStack = [];
  }

  const isExist = state.errMsgStack.includes(message);

  if (!isExist) {
    state.errMsgStack.push(message);

    window.$message?.error(message, {
      onLeave: () => {
        state.errMsgStack = state.errMsgStack.filter(msg => msg !== message);

        setTimeout(() => {
          state.errMsgStack = [];
        }, 5000);
      }
    });
  }
}
