import { create } from 'zustand';
import { apiClient } from '@/lib/backend/apiV1/client';
// 타입 단언으로 api 프로퍼티가 있다고 명시
const typedApiClient = apiClient as typeof apiClient & {
  api: import('@/lib/backend/apiV1/api').Api<any>['api'];
};
import { Api } from '@/lib/backend/apiV1/api';

interface AdminAuthStore {
  isAuthenticated: boolean;
  isAuthChecked: boolean;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  checkAuth: () => void;
}

export const useAdminAuthStore = create<AdminAuthStore>(set => ({
  isAuthenticated: false,
  isAuthChecked: false,
  login: async (username, password) => {
    try {
      const response = await typedApiClient.api.login({
        email: username,
        password,
      });
      const authHeader = response.headers['authorization'];
      if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.substring(7);
        localStorage.setItem('accessToken', token);
        set({ isAuthenticated: true, isAuthChecked: true });
        return true;
      }
      set({ isAuthenticated: false, isAuthChecked: true });
      return false;
    } catch (e) {
      set({ isAuthenticated: false, isAuthChecked: true });
      return false;
    }
  },
  logout: () => {
    localStorage.removeItem('accessToken');
    set({ isAuthenticated: false, isAuthChecked: true });
  },
  checkAuth: async () => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        set({ isAuthenticated: false, isAuthChecked: true });
        return;
      }
      try {
        // 서버에 토큰 검증 요청 (예: getMe API)
        await typedApiClient.api.getMe();
        set({ isAuthenticated: true, isAuthChecked: true });
      } catch (e) {
        // 토큰이 유효하지 않으면 로그아웃 처리
        localStorage.removeItem('accessToken');
        set({ isAuthenticated: false, isAuthChecked: true });
      }
    }
  },
}));
